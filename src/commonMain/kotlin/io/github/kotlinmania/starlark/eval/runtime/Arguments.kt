// port-lint: source src/eval/runtime/arguments.rs
package io.github.kotlinmania.starlark.eval.runtime

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import starlarkmap.Hashed
import starlarkmap.smallmap.SmallMap
import starlarkmap.smallset.SmallSet
import starlarkmap.StarlarkHashValue
import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.coerce
import io.github.kotlinmania.starlark.values.StarlarkIterator
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.DictRef
import io.github.kotlinmania.starlark.values.types.dict.Either as DictEither
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue

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

// Kotlin: FunctionError already extends Exception, convertible via standard mechanisms.

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
    ): Int? {
        return paramIndex
    }

    override fun smallHash(): StarlarkHashValue {
        return hash
    }
}

// Kotlin: no Coerce equivalent needed.

class ArgNames<S : ArgSymbol>(
    /** Names are guaranteed to be unique here. */
    private val names_: List<Pair<S, StringValue>>,
) {
    constructor() : this(emptyList())

    fun names(): List<Pair<S, StringValue>> {
        return names_
    }

    companion object {
        fun <S : ArgSymbol> default(): ArgNames<S> {
            return ArgNames()
        }

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
interface ArgumentsImpl<S : ArgSymbol> {
    // type ArgSymbol: ArgSymbol
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
        return when (kwargsVal) {
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
            else -> {
                if (full.names().names().isEmpty()) {
                    val downcast = kwargsVal.downcastRefKeyString()
                    if (downcast != null) {
                        val cloned = SmallMap.withCapacity<StringValue, Value>(downcast.len())
                        for ((k, v) in downcast.iterHashed()) {
                            cloned.insertHashedUniqueUnchecked(k, v)
                        }
                        Result.success(cloned)
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
                        val s = Arguments.unpackKwargsKeyAsValue(k.key())
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
    internal fun names(): Result<Dict> {
        val mapResult = namesMap()
        if (mapResult.isFailure) return Result.failure(mapResult.exceptionOrNull()!!)
        return Result.success(Dict.new(coerce(mapResult.getOrThrow())))
    }

    /**
     * Unpack all positional parameters into an iterator.
     */
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

    /**
     * Produce error if there are any positional arguments.
     */
    fun noPositionalArgs(heap: Heap): Result<Unit> {
        positionalN(0, heap).getOrElse { return Result.failure(it) }
        return Result.success(Unit)
    }

    /**
     * Produce error if there are any named (i.e. non-positional) arguments.
     */
    fun noNamedArgs(): Result<Unit> {
        fun bad(x: Arguments): Result<Unit> {
            // We might have a empty kwargs dictionary, but probably have an error
            val extra = mutableListOf<String>()
            extra.addAll(x.full.names.names().map { it.second.asStr() })
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
                    )
                )
            }
        }

        return if (full.named.isEmpty() && full.kwargs == null) {
            Result.success(Unit)
        } else {
            bad(this)
        }
    }

    /**
     * Collect exactly `n` positional arguments from the [Arguments],
     * failing if there are too many/few arguments. Ignores named arguments.
     */
    internal fun positionalN(n: Int, heap: Heap): Result<List<Value>> {
        val (required, optional) = optional(n, 0, heap).let {
            if (it.isFailure) return Result.failure(it.exceptionOrNull()!!)
            it.getOrThrow()
        }
        return Result.success(required)
    }

    internal fun positional(n: Int, heap: Heap): Result<List<Value>> = positionalN(n, heap)

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
        fun rare(
            x: Arguments,
            heap: Heap,
        ): Result<Pair<List<Value>, List<Value?>>> {
            // Very sad that we allocate into a list, but I expect calling into a small positional argument
            // with a *args is very rare.
            val argsIter: Iterator<Value> = when (val a = x.full.args) {
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
                    )
                )
            }
        }

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
        return rare(this, heap)
    }

    /**
     * Collect 1 positional arguments from the [Arguments], failing if there are too many/few
     * arguments. Ignores named arguments.
     */
    fun positional1(heap: Heap): Result<Value> {
        // Could be implemented more directly, let's see if profiling shows it up
        val result = positionalN(1, heap)
        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        return Result.success(result.getOrThrow()[0])
    }

    /**
     * Collect up to 1 optional arguments from the [Arguments], failing if there are too many
     * arguments. Ignores named arguments.
     */
    internal fun optional1(heap: Heap): Result<Value?> {
        // Could be implemented more directly, let's see if profiling shows it up
        val (_, opt) = optional(0, 1, heap).let {
            if (it.isFailure) return Result.failure(it.exceptionOrNull()!!)
            it.getOrThrow()
        }
        return Result.success(opt[0])
    }

    fun frozenToV(): Arguments {
        return this
    }

    companion object {
        fun default(): Arguments {
            return Arguments()
        }

        /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
        fun unpackKwargsKeyAsValue(k: Value): Result<StringValue> {
            val sv = StringValue.new(k)
                ?: return Result.failure(FunctionError.ArgsValueIsNotString)
            return Result.success(sv)
        }

        /** Confirm that a key in the `kwargs` field is indeed a string, or error. */
        fun unpackKwargsKey(k: Value): Result<String> {
            return unpackKwargsKeyAsValue(k).map { it.asStr() }
        }
    }
}

// Kotlin: No lifetime erasure needed. Arguments does not have a lifetime parameter.

private fun DictRef.dict(): Dict {
    return when (val ref = aref) {
        is DictEither.Left -> ref.value.value
        is DictEither.Right -> ref.value
    }
}

private fun DictRef.len(): Int {
    return dict().len()
}

private fun DictRef.iterHashed(): Sequence<Pair<Hashed<Value>, Value>> {
    return dict().iterHashed()
}

private fun DictRef.keys(): Sequence<Value> {
    return dict().keys()
}

private fun DictRef.downcastRefKeyString(): SmallMap<StringValue, Value>? {
    return dict().downcastRefKeyString()
}

// Tests are in commonTest, not here.
