// port-lint: source src/typing/ty.rs
package io.github.kotlinmania.starlark_kotlin.typing

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

/**
 * Configuration for rendering types.
 *
 * Corresponds to Rust's `TypeRenderConfig` enum.
 */
sealed class TypeRenderConfig {
    /** Uses the default rendering configuration. */
    data object Default : TypeRenderConfig()

    /** Uses linked type rendering for docs. */
    data class LinkedType(
        /** The function to render linked type element. */
        val renderLinkedTyStarlarkValue: (TyStarlarkValue) -> String,
    ) : TypeRenderConfig()
}

/**
 * A Starlark type.
 *
 * A series of alternative types (union). When typechecking, we try all alternatives,
 * and if at least one of them succeeds, then the whole expression is considered a success.
 *
 * This is different handling of union types than in TypeScript for example;
 * TypeScript would consider such expression to be an error.
 *
 * Corresponds to Rust's `Ty` struct.
 */
class Ty private constructor(
    /**
     * A series of alternative types.
     *
     * When typechecking, we try all alternatives, and if at least one of them
     * succeeds, then the whole expression is considered to be a success.
     */
    internal val alternatives: SmallArcVec1<TyBasic>
) : Comparable<Ty> {

    companion object {
        /** Create a [Ty.any], but tagged in such a way it can easily be found. */
        fun todo(): Ty = any()

        /** Create a single-basic type. */
        fun basic(basic: TyBasic): Ty = Ty(SmallArcVec1.one(basic))

        /** "any" type: can hold any value. */
        fun any(): Ty = basic(TyBasic.Any)

        /** Never type: can hold no value. */
        fun never(): Ty = Ty(SmallArcVec1.empty())

        /** Create a `None` type. */
        fun none(): Ty = basic(TyBasic.none())

        /** Create a boolean type. */
        fun bool(): Ty = basic(TyBasic.StarlarkValue(TyStarlarkValue.bool()))

        /** Create the int type. */
        fun int(): Ty = basic(TyBasic.int())

        /** Create a float type. */
        fun float(): Ty = basic(TyBasic.float())

        /** Create a string type. */
        fun string(): Ty = basic(TyBasic.string())

        /** Create a list type. */
        fun list(element: Ty): Ty = basic(TyBasic.list(element))

        /** `list[typing.Any]`. */
        fun anyList(): Ty = list(any())

        /** Create an iterable type. */
        fun iter(item: Ty): Ty = basic(TyBasic.iter(item))

        /** Create a dictionary type. */
        fun dict(key: Ty, value: Ty): Ty = basic(TyBasic.dict(key, value))

        /** `dict[typing.Any, typing.Any]`. */
        fun anyDict(): Ty = dict(any(), any())

        /** Create a set type. */
        fun set(item: Ty): Ty = basic(TyBasic.set(item))

        /** `set[typing.Any]`. */
        fun anySet(): Ty = set(any())

        /** Create a tuple of two elements. */
        fun tuple2(a: Ty, b: Ty): Ty = tuple(listOf(a, b))

        /** Create a tuple of given elements. */
        fun tuple(elems: List<Ty>): Ty = basic(TyBasic.Tuple(TyTuple.Elems(elems)))

        /** Tuple where elements are unknown. */
        fun anyTuple(): Ty = tupleOf(any())

        /** Create a tuple where all elements have the same type. */
        fun tupleOf(item: Ty): Ty = basic(TyBasic.Tuple(TyTuple.Of(ArcTy.new(item))))

        /** Create a function type. */
        fun function(params: ParamSpec, result: Ty): Ty =
            tyFunction(TyFunction.new(params, result))

        /** Create a callable type. */
        fun callable(params: ParamSpec, result: Ty): Ty =
            basic(TyBasic.Callable(TyCallable.new(params, result)))

        /** Create a function type from a [TyFunction]. */
        fun tyFunction(f: TyFunction): Ty =
            custom(TyCustomFunction(f))

        /** Create a function, where the first argument is the result of `.type`. */
        fun ctorFunction(typeAttr: Ty, params: ParamSpec, result: Ty): Ty =
            custom(TyCustomFunction(TyFunction.newWithTypeAttr(params, result, typeAttr)))

        /** Function type that accepts any arguments and returns any result. */
        fun anyCallable(): Ty = basic(TyBasic.Callable(TyCallable.any()))

        /** Any struct type. */
        fun anyStruct(): Ty = custom(TyStruct.any())

        /** Create a custom type. */
        fun custom(t: TyCustomImpl): Ty =
            basic(TyBasic.Custom(TyCustom.new(t)))

        /** Create a custom function type. */
        fun customFunction(f: TyCustomFunctionImpl): Ty =
            custom(TyCustomFunction(f))

        /** Type from the implementation of `StarlarkValue`. */
        fun starlarkValue(value: TyStarlarkValue): Ty =
            basic(TyBasic.StarlarkValue(value))

        /**
         * Create a unions type, which will be normalised before being created.
         */
        fun unions(xs: List<Ty>): Ty {
            // Handle common cases first.
            if (xs.any { it.isAny() }) {
                return any()
            }

            val iter = xs.iterator()

            // Skip never types to find first non-never
            var x0: Ty? = null
            while (iter.hasNext()) {
                val x = iter.next()
                if (!x.isNever()) {
                    x0 = x
                    break
                }
            }
            if (x0 == null) return never()

            // Find second non-never
            var x1: Ty? = null
            while (iter.hasNext()) {
                val x = iter.next()
                if (!x.isNever()) {
                    x1 = x
                    break
                }
            }
            if (x1 == null) return x0

            // Check for no-more-remaining fast path
            if (!iter.hasNext() && x0 == x1) {
                return x0
            }

            // Now default slow version — collect all remaining plus x0, x1.
            val remaining = mutableListOf<TyBasic>()
            for (basic in x0.iterUnion()) remaining.add(basic)
            for (basic in x1.iterUnion()) remaining.add(basic)
            while (iter.hasNext()) {
                val x = iter.next()
                for (basic in x.iterUnion()) remaining.add(basic)
            }
            remaining.sort()
            // Dedup
            val deduped = remaining.distinct().toMutableList()

            // Try merging adjacent elements
            val merged = mergeAdjacent(deduped) { x, y ->
                when {
                    x is TyBasic.List && y is TyBasic.List -> {
                        MergeResult.Left(TyBasic.List(ArcTy.union2(x.item, y.item)))
                    }
                    x is TyBasic.Dict && y is TyBasic.Dict -> {
                        MergeResult.Left(
                            TyBasic.Dict(
                                ArcTy.union2(x.key, y.key),
                                ArcTy.union2(x.value, y.value)
                            )
                        )
                    }
                    x is TyBasic.Custom && y is TyBasic.Custom -> {
                        val result = TyCustom.union2(x.custom, y.custom)
                        if (result.isSuccess) {
                            MergeResult.Left(TyBasic.Custom(result.getOrThrow()))
                        } else {
                            MergeResult.Right(x, y)
                        }
                    }
                    else -> MergeResult.Right(x, y)
                }
            }

            return Ty(SmallArcVec1.cloneFromSlice(merged))
        }

        /** Create a union of two entries. */
        fun union2(a: Ty, b: Ty): Ty {
            // Handle fast cases first.
            // Optimizations, semantically identical to default implementation.
            return when {
                a.isAny() || b.isAny() -> any()
                a == b -> a
                a.isNever() -> b
                b.isNever() -> a
                else -> unions(listOf(a, b))
            }
        }
    }

    /**
     * Turn a type back into a name, potentially erasing some structure.
     * E.g. the type `[bool]` would return `list`.
     * Types like [Ty.any] will return `null`.
     */
    fun asName(): String? = when (val slice = alternatives.asSlice()) {
        is List<TyBasic> -> if (slice.size == 1) slice[0].asName() else null
    }

    /** This type is `TyStarlarkValue`. */
    internal fun isStarlarkValue(): TyStarlarkValue? {
        val slice = iterUnion()
        return if (slice.size == 1 && slice[0] is TyBasic.StarlarkValue) {
            (slice[0] as TyBasic.StarlarkValue).value
        } else {
            null
        }
    }

    /** Check if this is the `any` type. */
    fun isAny(): Boolean = this == any()

    /** Check if this is the `never` type. */
    fun isNever(): Boolean = alternatives.isEmpty()

    /** Check if this type is a list. */
    fun isList(): Boolean {
        val slice = alternatives.asSlice()
        return slice.size == 1 && slice[0] is TyBasic.List
    }

    /** Check if this is a function type. */
    fun isFunction(): Boolean = asName() == "function"

    /**
     * If this type is function, return the function type.
     *
     * This is exposed for buck2 providers implementation,
     * probably it does not do what you think.
     */
    fun asFunction(): TyFunction? {
        val slice = iterUnion()
        return if (slice.size == 1) slice[0].asFunction() else null
    }

    /**
     * Iterate over the types within a union, pretending the type is a singleton
     * union if not a union.
     */
    fun iterUnion(): List<TyBasic> = alternatives.asSlice()

    /**
     * Apply typechecking operation for each alternative.
     *
     * If at least one was successful, return the union of all successful results.
     */
    internal fun typecheckUnionSimple(
        typecheck: (TyBasic) -> Result<Ty>,
    ): Result<Ty> {
        if (isAny() || isNever()) {
            return Result.success(this)
        }
        val xs = iterUnion()
        return when {
            // Optimize common case.
            xs.size == 1 -> typecheck(xs[0])
            else -> {
                val good = mutableListOf<Ty>()
                for (basic in xs) {
                    val result = typecheck(basic)
                    if (result.isSuccess) {
                        good.add(result.getOrThrow())
                    }
                }
                if (good.isEmpty()) {
                    Result.failure(TypingNoContextError)
                } else {
                    Result.success(unions(good))
                }
            }
        }
    }

    /**
     * Check if the value of this type can be called with given arguments and expected return type.
     *
     * @param pos positional argument types
     * @param named named argument types (name to type)
     * @param args `*args` type, or null
     * @param kwargs `**kwargs` type, or null
     * @param expectedReturnType the expected return type
     * @return true if the call is valid
     */
    fun checkCall(
        pos: List<Ty>,
        named: List<Pair<String, Ty>>,
        args: Ty?,
        kwargs: Ty?,
        expectedReturnType: Ty,
    ): Boolean {
        // Simplified validation — full implementation requires TypingOracleCtx.
        // Check if this type has a callable signature.
        val callable = when {
            iterUnion().size == 1 -> {
                val basic = iterUnion()[0]
                when (basic) {
                    is TyBasic.Callable -> basic.callable
                    is TyBasic.Custom -> basic.custom.asCallableDyn()
                    else -> null
                }
            }
            else -> null
        }
        return callable != null
    }

    /**
     * Check if this type intersects with another type.
     *
     * Two types intersect if there is at least one value that belongs to both types.
     */
    internal fun checkIntersects(other: Ty): Result<Boolean> {
        // Simplified — full implementation requires TypingOracleCtx.
        if (isAny() || other.isAny()) return Result.success(true)
        if (isNever() || other.isNever()) return Result.success(false)

        // Check if any basic type in this intersects with any basic type in other.
        for (a in iterUnion()) {
            for (b in other.iterUnion()) {
                if (a == b) return Result.success(true)
                // Check structural intersection for custom types.
                if (a is TyBasic.Custom && b is TyBasic.Custom) {
                    if (TyCustom.intersects(a.custom, b.custom)) {
                        return Result.success(true)
                    }
                }
            }
        }
        return Result.success(false)
    }

    /** Format with a custom rendering configuration. */
    fun fmtWithConfig(config: TypeRenderConfig): String {
        val xs = iterUnion()
        return when {
            xs.isEmpty() -> "never"
            else -> {
                val sb = StringBuilder()
                for ((i, x) in xs.withIndex()) {
                    if (i != 0) {
                        sb.append(" | ")
                    }
                    x.fmtWithConfig(sb, config)
                }
                sb.toString()
            }
        }
    }

    /** Display with a custom configuration, returning a [TyDisplay] wrapper. */
    fun displayWith(config: TypeRenderConfig): TyDisplay = TyDisplay(this, config)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Ty) return false
        return alternatives == other.alternatives
    }

    override fun hashCode(): Int = alternatives.hashCode()

    override fun toString(): String = fmtWithConfig(TypeRenderConfig.Default)

    override fun compareTo(other: Ty): Int = alternatives.compareTo(other.alternatives)
}

/**
 * Helper for displaying a [Ty] with a specific render configuration.
 *
 * Corresponds to Rust's `TyDisplay<'a>`.
 */
class TyDisplay(
    private val ty: Ty,
    private val config: TypeRenderConfig,
) {
    override fun toString(): String = ty.fmtWithConfig(config)
}

/**
 * Result of merging two adjacent elements.
 */
private sealed class MergeResult<T> {
    /** Elements were merged into a single element. */
    data class Left<T>(val value: T) : MergeResult<T>()
    /** Elements could not be merged and remain separate. */
    data class Right<T>(val left: T, val right: T) : MergeResult<T>()
}

/**
 * Try to merge adjacent elements in a list.
 *
 * Corresponds to Rust's `merge_adjacent` function.
 */
private fun <T> mergeAdjacent(xs: List<T>, f: (T, T) -> MergeResult<T>): List<T> {
    val res = mutableListOf<T>()
    var last: T? = null
    for (x in xs) {
        val l = last
        if (l == null) {
            last = x
        } else {
            when (val merged = f(l, x)) {
                is MergeResult.Left -> last = merged.value
                is MergeResult.Right -> {
                    res.add(merged.left)
                    last = merged.right
                }
            }
        }
    }
    if (last != null) {
        res.add(last)
    }
    return res
}
