// port-lint: source src/eval/runtime/arguments.rs
package io.github.kotlinmania.starlark.eval.runtime

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

import io.github.kotlinmania.starlark.ErrorKind
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.smallset.SmallSet
import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.StarlarkIterator
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueTyped
import io.github.kotlinmania.starlark.values.layout.ValueTypedComplex
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.bigint.unpackLong
import io.github.kotlinmania.starlark.values.types.bigint.unpackUInt
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.DictRef
import io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark.values.types.int.unpackValueI32
import io.github.kotlinmania.starlark.values.typing.TypeType
import io.github.kotlinmania.starlark.Error as StarlarkError
import io.github.kotlinmania.starlark.values.types.dict.Either as DictEither

sealed class FunctionError(
    private val text: String,
) : Exception() {
    override val message: String
        get() = text

    data class ExtraPositionalArg(
        val count: Int,
        val function: String,
    ) : FunctionError("Found $count extra positional argument(s) for call to $function")

    data class ExtraNamedArg(
        val names: List<String>,
        val function: String,
    ) : FunctionError("Found `${names.joinToString("` `")}` extra named parameter(s) for call to $function")

    data class RepeatedArg(
        val name: String,
    ) : FunctionError("Argument `$name` occurs more than once")

    data object ArgsValueIsNotString :
        FunctionError("The argument provided for *args is not an identifier")

    data object ArgsArrayIsNotIterable :
        FunctionError("The argument provided for *args is not iterable")

    data object KwArgsIsNotDict :
        FunctionError("The argument provided for **kwargs is not a dictionary")

    //     if min == max {min.to_string()} else {format!("between {min} and {max}")})]
    data class WrongNumberOfArgs(
        val min: Int,
        val max: Int,
        val got: Int,
    ) : FunctionError(
            "Wrong number of positional arguments, expected ${
                if (min == max) min.toString() else "between $min and $max"
            }, got $got",
        )
}

/** Convert a [FunctionError] into a [StarlarkError] wrapping it as [ErrorKind.Function]. */
fun from(e: FunctionError): StarlarkError = StarlarkError.newKind(ErrorKind.Function(e))

/** An object accompanying argument name for faster argument resolution. */
interface ArgSymbol {
    fun <V> getIndexFromParamSpec(ps: ParametersSpec<V>): Int?

    fun smallHash(): StarlarkHashValue
}

/**
 * `Symbol` resolved to function parameter index.
 */
data class ResolvedArgName(
    /** Hash of the argument name. */
    val hash: StarlarkHashValue,
    /** Parameter index or `null` if the argument should go to kwargs. */
    val paramIndex: Int?,
) : ArgSymbol {
    override fun <V> getIndexFromParamSpec(
        ps: ParametersSpec<V>,
    ): Int? = paramIndex

    override fun smallHash(): StarlarkHashValue = hash
}

// Kotlin: no Coerce equivalent needed.

class ArgNames<S : ArgSymbol>(
    /** Names are guaranteed to be unique here. */
    private val namedArguments: List<Pair<S, StringValue>>,
) {
    constructor() : this(emptyList())

    fun names(): List<Pair<S, StringValue>> = namedArguments

    companion object {
        fun <S : ArgSymbol> default(): ArgNames<S> = ArgNames()

        /**
         * Names must be unique.
         * String in `Symbol` must be equal to the `StringValue`,
         * it is caller responsibility to ensure that.
         *
         * When this invariant is violated, it is memory safe,
         * but behavior will be incorrect (errors in wrong places, missing errors, panics, etc.)
         */
        fun <S : ArgSymbol> newUnique(names: List<Pair<S, StringValue>>): ArgNames<S> = ArgNames(names)

        fun <S : ArgSymbol> newCheckUnique(
            names: List<Pair<S, StringValue>>,
        ): Result<ArgNames<S>> {
            val set = SmallSet.withCapacity<String>(names.size)
            for ((s, name) in names) {
                if (!set.insertHashed(Hashed.newUnchecked(s.smallHash(), name.asStr()))) {
                    return Result.failure(
                        FunctionError.RepeatedArg(name = name.asStr()),
                    )
                }
            }
            return Result.success(newUnique(names))
        }
    }
}

/** Either full arguments, or short arguments for positional-only calls. */
interface ArgumentsImpl<S : ArgSymbol> {
    fun pos(): List<Value>

    fun named(): List<Value>

    fun names(): ArgNames<S>

    fun args(): Value?

    fun kwargs(): Value?
}

/**
 * Arguments object is passed from the starlark interpreter to function implementation
 * when evaluation function or method calls.
 */
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
    // Handled by default parameter values above.

    override fun pos(): List<Value> = pos

    override fun named(): List<Value> = named

    override fun names(): ArgNames<S> = names

    override fun args(): Value? = args

    override fun kwargs(): Value? = kwargs
}

/**
 * Positional-only arguments, smaller and faster than [ArgumentsFull].
 */
class ArgumentsPos<S : ArgSymbol>(
    val pos: List<Value>,
) : ArgumentsImpl<S> {
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
class Arguments(
    @PublishedApi internal val full: ArgumentsFull<Symbol> = ArgumentsFull(),
) {
    internal val inner: ArgumentsFull<Symbol>
        get() = full

    /** Unwrap all named arguments (both explicit and in `**kwargs`) into a map.
     *
     * This operation fails if named argument names are not unique.
     */
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
                    val result =
                        SmallMap.withCapacity<StringValue, Value>(
                            full.names.names().size + kwargsVal.len(),
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
                                FunctionError.RepeatedArg(name = sVal.asStr()),
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
    fun len(): Result<Int> {
        val argsLen =
            when (val a = full.args) {
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
    internal fun names(): Result<Dict> {
        val mapResult = namesMap()
        if (mapResult.isFailure) return Result.failure(mapResult.exceptionOrNull()!!)
        val names = mapResult.getOrThrow()
        val values = SmallMap.withCapacity<Value, Value>(names.len())
        for ((key, value) in names.iterHashed()) {
            values.insertHashedUniqueUnchecked(
                Hashed.newUnchecked(key.hash(), key.key().toValue()),
                value,
            )
        }
        return Result.success(Dict.new(values))
    }

    /**
     * Unpack all positional parameters into an iterator.
     */
    fun positions(heap: Heap): Result<Iterator<Value>> {
        val tail: Iterator<Value> =
            when (val a = full.args) {
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
    internal fun unpackKwargs(): Result<DictRef?> =
        when (val kw = full.kwargs) {
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

    /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
    internal fun unpackKwargsKeyAsValue(k: Value): Result<StringValue> {
        val sv = StringValue.new(k) ?: return Result.failure(FunctionError.ArgsValueIsNotString)
        return Result.success(sv)
    }

    /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
    internal fun unpackKwargsKey(k: Value): Result<String> = unpackKwargsKeyAsValue(k).map { it.asStr() }

    /**
     * Produce error if there are any positional arguments.
     */
    fun noPositionalArgs(heap: Heap): Result<Unit> {
        val result = positional(0, heap)
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val list = result.getOrThrow()
        if (list.isNotEmpty()) {
            return Result.failure(
                FunctionError.WrongNumberOfArgs(min = 0, max = 0, got = list.size),
            )
        }
        return Result.success(Unit)
    }

    /**
     * Produce error if there are any named (i.e. non-positional) arguments.
     */
    fun noNamedArgs(): Result<Unit> {
        if (full.named.isEmpty() && full.kwargs == null) {
            return Result.success(Unit)
        }
        return bad(this)
    }

    /**
     * Collect exactly `n` positional arguments from the [Arguments],
     * failing if there are too many/few arguments. Ignores named arguments.
     */
    internal fun positional(n: Int, heap: Heap): Result<List<Value>> {
        val (required, optional) =
            optional(n, 0, heap).let {
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
    internal fun optional(
        required: Int,
        optional: Int,
        heap: Heap,
    ): Result<Pair<List<Value>, List<Value?>>> {
        if (full.args == null &&
            full.pos.size >= required &&
            full.pos.size <= required + optional
        ) {
            val requiredList = full.pos.subList(0, required)
            val optionalList = MutableList<Value?>(optional) { null }
            val remaining = full.pos.subList(required, full.pos.size)
            for ((i, v) in remaining.withIndex()) {
                optionalList[i] = v
            }
            return Result.success(Pair(requiredList, optionalList))
        }
        return rare(this, required, optional, heap)
    }

    /**
     * Collect 1 positional arguments from the [Arguments], failing if there are too many/few
     * arguments. Ignores named arguments.
     */
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
    internal fun optional1(heap: Heap): Result<Value?> {
        // Could be implemented more directly, let's see if profiling shows it up
        val (_, opt) =
            optional(0, 1, heap).let {
                if (it.isFailure) return Result.failure(it.exceptionOrNull()!!)
                it.getOrThrow()
            }
        return Result.success(opt[0])
    }

    internal fun optional(
        heap: Heap,
        required: Int,
        optional: Int,
    ): Result<Pair<List<Value>, List<Value?>>> = optional(required, optional, heap)

    fun frozenToV(): Arguments = this

    // ---- Convenience accessors for starlark_module-style argument extraction ----

    /**
     * Get all positional arguments as a list.
     */
    fun positionalAll(): List<Value> = full.pos

    /**
     * Get a single positional argument by 0-based index, unpacking it to type [T].
     * Supports [Value], [String], [Int], and [Boolean] directly.
     * When T is inferred as [Value] (default), returns the raw [Value].
     */
    inline fun <reified T> positional(index: Int): T {
        val v = full.pos[index]
        return unpackValueAs<T>(v)
    }

    /**
     * Get an optional positional argument by 0-based index, unpacking it to type [T],
     * or null if the index is out of range.
     */
    inline fun <reified T> optionalPositional(index: Int): T? {
        val v = full.pos.getOrNull(index) ?: return null
        return unpackValueAs<T>(v)
    }

    /**
     * Get an optional named argument by name, unpacking it to type [T],
     * or null if the argument is not present.
     */
    inline fun <reified T> optionalNamed(name: String): T? {
        val idx = full.names.names().indexOfFirst { it.second.asStr() == name }
        if (idx < 0) return null
        return unpackValueAs<T>(full.named[idx])
    }

    /**
     * Get an optional named argument by name, unpacking it to type [T],
     * or null if the argument is not present. Alias for [optionalNamed].
     */
    inline fun <reified T> namedOptional(name: String): T? = optionalNamed<T>(name)

    inline fun <reified T : ComplexValue, reified F : StarlarkValue> positionalComplex(
        index: Int,
    ): ValueTypedComplex<T, F> =
        ValueTypedComplex.newErr<T, F>(full.pos[index]).getOrThrow()

    companion object {
        fun default(): Arguments = Arguments()

        /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
        fun unpackKwargsKeyAsValue(k: Value): Result<StringValue> {
            val sv =
                StringValue.new(k)
                    ?: return Result.failure(FunctionError.ArgsValueIsNotString)
            return Result.success(sv)
        }

        /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
        fun unpackKwargsKey(k: Value): Result<String> = unpackKwargsKeyAsValue(k).map { it.asStr() }
    }
}

// Kotlin: No lifetime erasure needed. Arguments does not have a lifetime parameter.

/**
 * Cold path for [Arguments.noNamedArgs]: collects extra named argument names
 * and produces an error if any are found.
 */
private fun bad(x: Arguments): Result<Unit> {
    // We might have an empty kwargs dictionary, but probably have an error
    val extra = mutableListOf<String>()
    extra.addAll(
        x.full.names
            .names()
            .map { it.second.asStr() },
    )
    val kwargsResult = x.unpackKwargs()
    if (kwargsResult.isFailure) return Result.failure(kwargsResult.exceptionOrNull()!!)
    val kwargsVal = kwargsResult.getOrNull()
    if (kwargsVal != null) {
        for (k in kwargsVal.keys()) {
            val keyResult = Arguments.unpackKwargsKey(k)
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
            ),
        )
    }
}

/**
 * Cold path for [Arguments.optional]: handles the rare case where `*args` is present
 * and needs to be iterated and combined with positional arguments.
 */
private fun rare(
    x: Arguments,
    required: Int,
    optional: Int,
    heap: Heap,
): Result<Pair<List<Value>, List<Value?>>> {
    // Very sad that we allocate into a list, but I expect calling into a small positional argument
    // with a *args is very rare.
    val argsIter: Iterator<Value> =
        when (val a = x.full.args) {
            null -> StarlarkIterator.empty(heap)
            else -> {
                val iterResult = a.iterate(heap)
                if (iterResult.isFailure) return Result.failure(iterResult.exceptionOrNull()!!)
                iterResult.getOrThrow()
            }
        }
    val xs = x.full.pos.toMutableList()
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
            ),
        )
    }
}

private fun DictRef.dict(): Dict =
    when (val ref = aref) {
        is DictEither.Left -> ref.value.value
        is DictEither.Right -> ref.value
    }

private fun DictRef.len(): Int = dict().len()

private fun DictRef.iterHashed(): Sequence<Pair<Hashed<Value>, Value>> =
    dict().iterHashed()

private fun DictRef.keys(): Sequence<Value> =
    dict().keys()

private fun DictRef.downcastRefKeyString(): SmallMap<StringValue, Value>? =
    dict().downcastRefKeyString()

// Tests are in commonTest, not here.

/** Unpack a value to type [T]. */
@PublishedApi
internal inline fun <reified T> unpackValueAs(v: Value): T {
    val unpacked: Any? =
        when (T::class) {
            Value::class -> v
            StringValue::class ->
                StringValue.new(v)
                    ?: throw IllegalArgumentException("Expected StringValue, got ${v.toStringForTypeError()}")
            String::class -> v.unpackStrErr().getOrThrow()
            Int::class ->
                unpackValueI32(v).getOrThrow()
                    ?: throw IllegalArgumentException("Expected Int, got ${v.toStringForTypeError()}")
            UInt::class ->
                v.unpackUInt().getOrThrow()
                    ?: throw IllegalArgumentException("Expected UInt, got ${v.toStringForTypeError()}")
            Long::class ->
                v.unpackLong().getOrThrow()
                    ?: throw IllegalArgumentException("Expected Long, got ${v.toStringForTypeError()}")
            Boolean::class -> v.toBool()
            ValueTyped::class -> {
                val valueClassifier =
                    kotlin.reflect
                        .typeOf<T>()
                        .arguments
                        .firstOrNull()
                        ?.type
                        ?.classifier as? kotlin.reflect.KClass<*>
                if (valueClassifier != null && valueClassifier != StarlarkValue::class) {
                    @Suppress("UNCHECKED_CAST")
                    if (v.downcastRef(valueClassifier as kotlin.reflect.KClass<out StarlarkValue>) == null) {
                        throw IllegalArgumentException("Expected value of type ${valueClassifier.simpleName}, got: ${v.toStringForTypeError()}")
                    }
                }
                ValueTyped.newUnchecked<StarlarkValue>(v)
            }
            FrozenValueTyped::class -> {
                val frozen =
                    v.unpackFrozen()
                        ?: throw IllegalArgumentException("Expected frozen value, got: ${v.toStringForTypeError()}")
                val frozenClassifier =
                    kotlin.reflect
                        .typeOf<T>()
                        .arguments
                        .firstOrNull()
                        ?.type
                        ?.classifier as? kotlin.reflect.KClass<*>
                if (frozenClassifier != null && frozenClassifier != StarlarkValue::class) {
                    @Suppress("UNCHECKED_CAST")
                    if (frozen.downcastRef(frozenClassifier as kotlin.reflect.KClass<out StarlarkValue>) == null) {
                        throw IllegalArgumentException("Expected frozen value of type ${frozenClassifier.simpleName}, got: ${v.toStringForTypeError()}")
                    }
                }
                FrozenValueTyped.newUnchecked<StarlarkValue>(frozen)
            }
            TypeType::class ->
                TypeType.unpackValue(v)
                    ?: throw IllegalArgumentException("Expected TypeType, got: ${v.toStringForTypeError()}")
            ValueTypedComplex::class ->
                throw IllegalArgumentException("ValueTypedComplex arguments require positionalComplex with mutable and frozen types")
            else -> v.downcastRef(StarlarkValue::class)!!
        }

    if (unpacked is T) {
        return unpacked
    }
    throw IllegalArgumentException("Expected ${T::class.simpleName}, got ${v.toStringForTypeError()}")
}
