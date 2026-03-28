// port-lint: source src/eval/runtime/arguments.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.coerce
import io.github.kotlinmania.starlark_kotlin.values.StarlarkIterator
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.values.types.dict.DictRef
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Either as DictEither
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.dict.dictRefFromValue

// #[derive(Debug, Clone, Error)]
// pub(crate) enum FunctionError
sealed class FunctionError(
    private val text: String,
) : Exception() {
    override val message: String
        get() = text

    // #[error("Found {count} extra positional argument(s) for call to {function}")]
    data class ExtraPositionalArg(
        val count: Int,
        val function: String,
    ) : FunctionError("Found $count extra positional argument(s) for call to $function")

    // #[error("Found `{}` extra named parameter(s) for call to {function}", .names.join("` `"))]
    data class ExtraNamedArg(
        val names: List<String>,
        val function: String,
    ) : FunctionError("Found `${names.joinToString("` `")}` extra named parameter(s) for call to $function")

    // #[error("Argument `{name}` occurs more than once")]
    data class RepeatedArg(
        val name: String,
    ) : FunctionError("Argument `$name` occurs more than once")

    // #[error("The argument provided for *args is not an identifier")]
    data object ArgsValueIsNotString :
        FunctionError("The argument provided for *args is not an identifier")

    // #[error("The argument provided for *args is not iterable")]
    data object ArgsArrayIsNotIterable :
        FunctionError("The argument provided for *args is not iterable")

    // #[error("The argument provided for **kwargs is not a dictionary")]
    data object KwArgsIsNotDict :
        FunctionError("The argument provided for **kwargs is not a dictionary")

    // #[error("Wrong number of positional arguments, expected {}, got {got}",
    //     if min == max {min.to_string()} else {format!("between {min} and {max}")})]
    data class WrongNumberOfArgs(
        val min: Int,
        val max: Int,
        val got: Int,
    ) : FunctionError(
        "Wrong number of positional arguments, expected ${
            if (min == max) min.toString() else "between $min and $max"
        }, got $got"
    )
}

// impl From<FunctionError> for crate::Error
// Kotlin: FunctionError already extends Exception, convertible via standard mechanisms.

/** An object accompanying argument name for faster argument resolution. */
// pub(crate) trait ArgSymbol: Debug + Coerce<Self> + 'static
interface ArgSymbol {
    // fn get_index_from_param_spec<'v, V: ValueLike<'v>>(&self, ps: &ParametersSpec<V>) -> Option<usize>
    fun <V> getIndexFromParamSpec(ps: ParametersSpec<V>): Int?

    // fn small_hash(&self) -> StarlarkHashValue
    fun smallHash(): StarlarkHashValue
}

/**
 * `Symbol` resolved to function parameter index.
 */
// #[derive(Debug)]
// pub(crate) struct ResolvedArgName
data class ResolvedArgName(
    /** Hash of the argument name. */
    val hash: StarlarkHashValue,
    /** Parameter index or `null` if the argument should go to kwargs. */
    val paramIndex: Int?,
) : ArgSymbol {
    // impl ArgSymbol for ResolvedArgName
    override fun <V> getIndexFromParamSpec(
        ps: ParametersSpec<V>,
    ): Int? {
        return paramIndex
    }

    override fun smallHash(): StarlarkHashValue {
        return hash
    }
}

// unsafe impl Coerce<ResolvedArgName> for ResolvedArgName {}
// Kotlin: no Coerce equivalent needed.

// #[derive(Debug, Clone_, Dupe_)]
// pub(crate) struct ArgNames<'a, 'v, S: ArgSymbol>
class ArgNames<S : ArgSymbol>(
    /** Names are guaranteed to be unique here. */
    private val names_: List<Pair<S, StringValue>>,
) {
    // impl<'a, 'v, S: ArgSymbol> Default for ArgNames<'a, 'v, S>
    constructor() : this(emptyList())

    fun names(): List<Pair<S, StringValue>> {
        return names_
    }

    companion object {
        fun <S : ArgSymbol> default(): ArgNames<S> {
            return ArgNames()
        }

        // pub(crate) fn new_unique(names: &'a [(S, StringValue<'v>)]) -> ArgNames<'a, 'v, S>
        /**
         * Names must be unique.
         * String in `Symbol` must be equal to the `StringValue`,
         * it is caller responsibility to ensure that.
         *
         * When this invariant is violated, it is memory safe,
         * but behavior will be incorrect (errors in wrong places, missing errors, panics, etc.)
         */
        fun <S : ArgSymbol> newUnique(names: List<Pair<S, StringValue>>): ArgNames<S> {
            return ArgNames(names)
        }

        // pub(crate) fn new_check_unique(names: &'a [(S, StringValue<'v>)]) -> crate::Result<ArgNames<'a, 'v, S>>
        fun <S : ArgSymbol> newCheckUnique(
            names: List<Pair<S, StringValue>>,
        ): Result<ArgNames<S>> {
            val set = SmallSet.withCapacity<String>(names.size)
            for ((s, name) in names) {
                if (!set.insertHashed(Hashed.newUnchecked(s.smallHash(), name.asStr()))) {
                    return Result.failure(
                        FunctionError.RepeatedArg(name = name.asStr())
                    )
                }
            }
            return Result.success(newUnique(names))
        }
    }
}

/** Either full arguments, or short arguments for positional-only calls. */
// pub(crate) trait ArgumentsImpl<'v, 'a>: Debug
interface ArgumentsImpl<S : ArgSymbol> {
    // type ArgSymbol: ArgSymbol
    // fn pos(&self) -> &[Value<'v>]
    fun pos(): List<Value>
    // fn named(&self) -> &[Value<'v>]
    fun named(): List<Value>
    // fn names(&self) -> ArgNames<'a, 'v, Self::ArgSymbol>
    fun names(): ArgNames<S>
    // fn args(&self) -> Option<Value<'v>>
    fun args(): Value?
    // fn kwargs(&self) -> Option<Value<'v>>
    fun kwargs(): Value?
}

/**
 * Arguments object is passed from the starlark interpreter to function implementation
 * when evaluation function or method calls.
 */
// #[derive(Clone_, Dupe_, Debug)]
// pub(crate) struct ArgumentsFull<'v, 'a, S: ArgSymbol>
class ArgumentsFull<S : ArgSymbol>(
    /** Positional arguments. */
    var pos: List<Value> = emptyList(),
    /** Named arguments. */
    var named: List<Value> = emptyList(),
    /**
     * Names of named arguments.
     *
     * `named` length must be equal to `names` length.
     */
    var names: ArgNames<S> = ArgNames(),
    /** `*args` argument. */
    var args: Value? = null,
    /** `**kwargs` argument. */
    var kwargs: Value? = null,
) : ArgumentsImpl<S> {
    // impl<'v, 'a, S: ArgSymbol> Default for ArgumentsFull<'v, 'a, S>
    // Handled by default parameter values above.

    // impl<'v, 'a, S: ArgSymbol> ArgumentsImpl<'v, 'a> for ArgumentsFull<'v, 'a, S>
    override fun pos(): List<Value> = pos
    override fun named(): List<Value> = named
    override fun names(): ArgNames<S> = names
    override fun args(): Value? = args
    override fun kwargs(): Value? = kwargs
}

/**
 * Positional-only arguments, smaller and faster than [ArgumentsFull].
 */
// #[derive(Debug)]
// pub(crate) struct ArgumentsPos<'v, 'a, S: ArgSymbol>
class ArgumentsPos<S : ArgSymbol>(
    val pos: List<Value>,
) : ArgumentsImpl<S> {
    // impl<'a, 'v, S: ArgSymbol> ArgumentsImpl<'v, 'a> for ArgumentsPos<'v, 'a, S>
    override fun pos(): List<Value> = pos
    override fun named(): List<Value> = emptyList()
    override fun names(): ArgNames<S> = ArgNames()
    override fun args(): Value? = null
    override fun kwargs(): Value? = null
}

/**
 * Arguments object is passed from the starlark interpreter to function implementation
 * when evaluation function or method calls.
 */
// #[derive(Default, Clone, Dupe_)]
// pub struct Arguments<'v, 'a>(pub(crate) ArgumentsFull<'v, 'a, Symbol>);
class Arguments(
    internal val full: ArgumentsFull<Symbol> = ArgumentsFull(),
) {
    internal val inner: ArgumentsFull<Symbol>
        get() = full

    /** Unwrap all named arguments (both explicit and in `**kwargs`) into a map.
     *
     * This operation fails if named argument names are not unique.
     */
    // pub fn names_map(&self) -> crate::Result<SmallMap<StringValue<'v>, Value<'v>>>
    fun namesMap(): Result<SmallMap<StringValue, Value>> {
        val kwargsResult = unpackKwargs()
        if (kwargsResult.isFailure) {
            return Result.failure(kwargsResult.exceptionOrNull()!!)
        }
        val kwargsVal = kwargsResult.getOrNull()
        // match self.unpack_kwargs()?
        return when (kwargsVal) {
            // None =>
            null -> {
                val result = SmallMap.withCapacity<StringValue, Value>(full.names.names().size)
                for ((i, kv) in full.names.names().withIndex()) {
                    val (s, stringVal) = kv
                    result.insertHashedUniqueUnchecked(
                        Hashed.newUnchecked(s.smallHash(), stringVal),
                        full.named[i],
                    )
                }
                Result.success(result)
            }
            // Some(kwargs) =>
            else -> {
                if (full.names().names().isEmpty()) {
                    val downcast = kwargsVal.downcastRefKeyString()
                    if (downcast != null) {
                        Result.success(downcast)
                    } else {
                        Result.failure(FunctionError.ArgsValueIsNotString)
                    }
                } else {
                    // We have to insert the names before the kwargs since the iteration order is observable
                    val result = SmallMap.withCapacity<StringValue, Value>(
                        full.names.names().size + kwargsVal.len()
                    )
                    for ((i, kv) in full.names.names().withIndex()) {
                        val (s, stringVal) = kv
                        result.insertHashedUniqueUnchecked(
                            Hashed.newUnchecked(s.smallHash(), stringVal),
                            full.named[i],
                        )
                    }
                    for ((k, v) in kwargsVal.iterHashed()) {
                        val s = unpackKwargsKeyAsValue(k.key())
                        if (s.isFailure) return Result.failure(s.exceptionOrNull()!!)
                        val sVal = s.getOrThrow()
                        val hk = Hashed.newUnchecked(k.hash(), sVal)
                        val old = result.insertHashed(hk, v)
                        if (old != null) {
                            return Result.failure(
                                FunctionError.RepeatedArg(name = sVal.asStr())
                            )
                        }
                    }
                    Result.success(result)
                }
            }
        }
    }

    /**
     * The number of arguments, where those inside a args/kwargs are counted as multiple arguments.
     *
     * This operation fails if the `kwargs` is not a dictionary, or `args` does not support `len`.
     */
    // pub fn len(&self) -> crate::Result<usize>
    fun len(): Result<Int> {
        val argsLen = when (val a = full.args) {
            null -> 0
            else -> {
                val lenResult = a.length()
                if (lenResult.isFailure) return Result.failure(lenResult.exceptionOrNull()!!)
                lenResult.getOrThrow()
            }
        }
        val kwargsResult = unpackKwargs()
        if (kwargsResult.isFailure) return Result.failure(kwargsResult.exceptionOrNull()!!)
        val kwargsLen = kwargsResult.getOrNull()?.len() ?: 0
        return Result.success(full.pos.size + full.named.size + argsLen + kwargsLen)
    }

    /**
     * Unwrap all named arguments (both explicit and in `**kwargs`) into a dictionary.
     *
     * This operation fails if named argument names are not unique.
     */
    // pub(crate) fn names(&self) -> crate::Result<Dict<'v>>
    internal fun names(): Result<Dict> {
        val mapResult = namesMap()
        if (mapResult.isFailure) return Result.failure(mapResult.exceptionOrNull()!!)
        return Result.success(Dict.new(coerce(mapResult.getOrThrow())))
    }

    /**
     * Unpack all positional parameters into an iterator.
     */
    // pub fn positions<'b>(&'b self, heap: Heap<'v>) -> crate::Result<impl Iterator<Item = Value<'v>> + 'b>
    fun positions(heap: Heap): Result<Iterator<Value>> {
        val tail: Iterator<Value> = when (val a = full.args) {
            null -> StarlarkIterator.empty(heap)
            else -> {
                val iterResult = a.iterate(heap)
                if (iterResult.isFailure) return Result.failure(iterResult.exceptionOrNull()!!)
                iterResult.getOrThrow()
            }
        }
        return Result.success((full.pos.asSequence() + tail.asSequence()).iterator())
    }

    /**
     * Examine the `kwargs` field, converting it to a [DictRef] or failing.
     * Note that even if this operation succeeds, the keys in the kwargs
     * will _not_ have been validated to be strings (as they must be).
     * The arguments may also overlap with named, which would be an error.
     */
    // pub(crate) fn unpack_kwargs(&self) -> crate::Result<Option<DictRef>>
    internal fun unpackKwargs(): Result<DictRef?> {
        return when (val kw = full.kwargs) {
            null -> Result.success(null)
            else -> {
                val dictRef = dictRefFromValue(kw)
                if (dictRef == null) {
                    Result.failure(FunctionError.KwArgsIsNotDict)
                } else {
                    Result.success(dictRef)
                }
            }
        }
    }

    /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
    // pub(crate) fn unpack_kwargs_key_as_value(k: Value<'v>) -> crate::Result<StringValue<'v>>
    internal fun unpackKwargsKeyAsValue(k: Value): Result<StringValue> {
        val sv = StringValue.new(k) ?: return Result.failure(FunctionError.ArgsValueIsNotString)
        return Result.success(sv)
    }

    /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
    // pub(crate) fn unpack_kwargs_key(k: Value<'v>) -> crate::Result<&'v str>
    internal fun unpackKwargsKey(k: Value): Result<String> {
        return unpackKwargsKeyAsValue(k).map { it.asStr() }
    }

    /**
     * Produce error if there are any positional arguments.
     */
    // pub fn no_positional_args(&self, heap: Heap<'v>) -> crate::Result<()>
    fun noPositionalArgs(heap: Heap): Result<Unit> {
        val result = positional(0, heap)
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val list = result.getOrThrow()
        if (list.isNotEmpty()) {
            return Result.failure(
                FunctionError.WrongNumberOfArgs(min = 0, max = 0, got = list.size)
            )
        }
        return Result.success(Unit)
    }

    /**
     * Produce error if there are any named (i.e. non-positional) arguments.
     */
    // pub fn no_named_args(&self) -> crate::Result<()>
    fun noNamedArgs(): Result<Unit> {
        if (full.named.isEmpty() && full.kwargs == null) {
            return Result.success(Unit)
        }
        // #[cold] fn bad(x: &Arguments) -> crate::Result<()>
        val extra = mutableListOf<String>()
        extra.addAll(full.names.names().map { it.second.asStr() })
        val kwargsResult = unpackKwargs()
        if (kwargsResult.isFailure) return Result.failure(kwargsResult.exceptionOrNull()!!)
        val kwargsVal = kwargsResult.getOrNull()
        if (kwargsVal != null) {
            for (k in kwargsVal.keys()) {
                val keyResult = unpackKwargsKey(k)
                if (keyResult.isFailure) return Result.failure(keyResult.exceptionOrNull()!!)
                extra.add(keyResult.getOrThrow())
            }
        }
        return if (extra.isEmpty()) {
            Result.success(Unit)
        } else {
            // Would be nice to give a better name here, but it's in the call stack, so no big deal
            Result.failure(
                FunctionError.ExtraNamedArg(
                    names = extra,
                    function = "function",
                )
            )
        }
    }

    /**
     * Collect exactly `n` positional arguments from the [Arguments],
     * failing if there are too many/few arguments. Ignores named arguments.
     */
    // pub(crate) fn positional<const N: usize>(&self, heap: Heap<'v>) -> crate::Result<[Value<'v>; N]>
    internal fun positional(n: Int, heap: Heap): Result<List<Value>> {
        val (required, optional) = optional(n, 0, heap).let {
            if (it.isFailure) return Result.failure(it.exceptionOrNull()!!)
            it.getOrThrow()
        }
        return Result.success(required)
    }

    /**
     * Collect exactly `required` positional arguments, plus at most `optional` positional arguments
     * from the [Arguments], failing if there are too many/few arguments. Ignores named arguments.
     * The optional list will never have a non-null after a null.
     */
    // pub(crate) fn optional<const REQUIRED: usize, const OPTIONAL: usize>(...)
    internal fun optional(
        required: Int,
        optional: Int,
        heap: Heap,
    ): Result<Pair<List<Value>, List<Value?>>> {
        if (full.args == null
            && full.pos.size >= required
            && full.pos.size <= required + optional
        ) {
            val requiredList = full.pos.subList(0, required)
            val optionalList = MutableList<Value?>(optional) { null }
            val remaining = full.pos.subList(required, full.pos.size)
            for ((i, v) in remaining.withIndex()) {
                optionalList[i] = v
            }
            return Result.success(Pair(requiredList, optionalList))
        }
        // Rare path: need to iterate *args
        // Very sad that we allocate into a list, but I expect calling into a small positional argument
        // with a *args is very rare.
        val argsIter: Iterator<Value> = when (val a = full.args) {
            null -> StarlarkIterator.empty(heap)
            else -> {
                val iterResult = a.iterate(heap)
                if (iterResult.isFailure) return Result.failure(iterResult.exceptionOrNull()!!)
                iterResult.getOrThrow()
            }
        }
        val xs = full.pos.toMutableList()
        argsIter.forEach { xs.add(it) }
        return if (xs.size >= required && xs.size <= required + optional) {
            val requiredList = xs.subList(0, required)
            val optionalList = MutableList<Value?>(optional) { null }
            val remaining = xs.subList(required, xs.size)
            for ((i, v) in remaining.withIndex()) {
                optionalList[i] = v
            }
            Result.success(Pair(requiredList.toList(), optionalList))
        } else {
            Result.failure(
                FunctionError.WrongNumberOfArgs(
                    min = required,
                    max = required + optional,
                    got = xs.size,
                )
            )
        }
    }

    /**
     * Collect 1 positional arguments from the [Arguments], failing if there are too many/few
     * arguments. Ignores named arguments.
     */
    // pub fn positional1(&self, heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun positional1(heap: Heap): Result<Value> {
        // Could be implemented more directly, let's see if profiling shows it up
        val result = positional(1, heap)
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        return Result.success(result.getOrThrow()[0])
    }

    /**
     * Collect up to 1 optional arguments from the [Arguments], failing if there are too many
     * arguments. Ignores named arguments.
     */
    // pub(crate) fn optional1(&self, heap: Heap<'v>) -> crate::Result<Option<Value<'v>>>
    internal fun optional1(heap: Heap): Result<Value?> {
        // Could be implemented more directly, let's see if profiling shows it up
        val (_, opt) = optional(0, 1, heap).let {
            if (it.isFailure) return Result.failure(it.exceptionOrNull()!!)
            it.getOrThrow()
        }
        return Result.success(opt[0])
    }

    internal fun optional(
        heap: Heap,
        required: Int,
        optional: Int,
    ): Result<Pair<List<Value>, List<Value?>>> {
        return optional(required, optional, heap)
    }

    fun frozenToV(): Arguments {
        return this
    }

    // ---- Convenience accessors for starlark_module-style argument extraction ----

    /**
     * Get all positional arguments as a list.
     */
    fun positionalAll(): List<Value> = full.pos

    /**
     * Get a single positional argument by 0-based index, unpacking it to type [T].
     * Supports [Value], [String], [Int], and [Boolean] directly.
     * For other types performs an unchecked cast of the underlying [Value].
     * When T is inferred as [Value] (default), returns the raw [Value].
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> positional(index: Int): T {
        val v = full.pos[index]
        return unpackValueAs<T>(v)
    }

    /**
     * Get an optional positional argument by 0-based index, unpacking it to type [T],
     * or null if the index is out of range.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> optionalPositional(index: Int): T? {
        val v = full.pos.getOrNull(index) ?: return null
        return unpackValueAs<T>(v)
    }

    /**
     * Get an optional named argument by name, unpacking it to type [T],
     * or null if the argument is not present.
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> optionalNamed(name: String): T? {
        val idx = full.names.names().indexOfFirst { it.second.asStr() == name }
        if (idx < 0) return null
        return unpackValueAs<T>(full.named[idx])
    }

    /**
     * Get an optional named argument by name, unpacking it to type [T],
     * or null if the argument is not present. Alias for [optionalNamed].
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> namedOptional(name: String): T? = optionalNamed<T>(name)

    companion object {
        fun default(): Arguments {
            return Arguments()
        }

        // pub(crate) fn unpack_kwargs_key_as_value(k: Value<'v>) -> crate::Result<StringValue<'v>>
        /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
        fun unpackKwargsKeyAsValue(k: Value): Result<StringValue> {
            val sv = StringValue.new(k)
                ?: return Result.failure(FunctionError.ArgsValueIsNotString)
            return Result.success(sv)
        }

        // pub(crate) fn unpack_kwargs_key(k: Value<'v>) -> crate::Result<&'v str>
        /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
        fun unpackKwargsKey(k: Value): Result<String> {
            return unpackKwargsKeyAsValue(k).map { it.asStr() }
        }
    }
}

// impl<'a> Arguments<'static, 'a>
// pub(crate) fn frozen_to_v<'v>(&self) -> &Arguments<'v, 'a>
// Kotlin: No lifetime erasure needed. Arguments does not have a lifetime parameter.

private fun DictRef.dict(): Dict {
    return when (val ref = aref) {
        is DictEither.Left -> ref.value.value
        is DictEither.Right -> ref.value
        else -> throw IllegalStateException("Unexpected DictEither: $ref")
    }
}

private fun DictRef.len(): Int {
    return dict().len()
}

private fun DictRef.iterHashed(): Sequence<Pair<Hashed<Value>, Value>> {
    @Suppress("UNCHECKED_CAST")
    return dict().iterHashed() as Sequence<Pair<Hashed<Value>, Value>>
}

private fun DictRef.keys(): Sequence<Value> {
    @Suppress("UNCHECKED_CAST")
    return dict().keys() as Sequence<Value>
}

private fun DictRef.downcastRefKeyString(): SmallMap<StringValue, Value>? {
    @Suppress("UNCHECKED_CAST")
    return dict().downcastRefKeyString() as SmallMap<StringValue, Value>?
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.

/**
 * Unpack a [Value] to type [T]. Used by [Arguments] convenience accessors.
 * Handles [Value], [String], [Int], and [Boolean] directly.
 * For other types performs an unchecked cast of the underlying [Value].
 */
@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> unpackValueAs(v: Value): T {
    return when (T::class) {
        Value::class -> v as T
        String::class -> v.unpackStrErr().getOrThrow() as T
        Int::class -> (v.unpackI32()
            ?: throw IllegalArgumentException("Expected Int, got ${v.toStringForTypeError()}")) as T
        Boolean::class -> v.toBool() as T
        else -> v as T
    }
}
