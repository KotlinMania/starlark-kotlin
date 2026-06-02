// port-lint: source src/values/typing/callable.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark.values.typing

import kotlin.native.HiddenFromObjC

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

import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueStarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.list.UnpackListUnpackValue
import io.github.kotlinmania.starlark.values.typing.callable.StarlarkCallableParamAny
import io.github.kotlinmania.starlark.values.typing.callable.StarlarkCallableParamSpec
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

// Submodules:

internal class TypingCallable :
    StarlarkValue,
    AllocFrozenValue {
    override val TYPE: String get() = "typing.Callable"
    override val HAS_eval_type: Boolean get() = true

    override fun toString(): String = TYPE

    override fun starlarkTypeRepr(): Ty = StarlarkCallable.starlarkTypeRepr()

    override fun evalType(): Ty = StarlarkCallable.starlarkTypeRepr()

    override fun at2(index0: Value, index1: Value, heap: Heap): Result<Value> =
        runCatching {
            val unpacker =
                UnpackListUnpackValue<Value>(
                    object : UnpackValue<Value> {
                        override fun starlarkTypeRepr(): Ty = Ty.any()

                        override fun unpackValueImpl(value: Value): Result<Value?> = Result.success(value)
                    },
                )
            val paramTypesList = unpacker.unpackValueErr(index0)
            val retTy = TypeCompiled.new(index1, heap).asTy()
            val paramTys = mutableListOf<Ty>()
            for (p in paramTypesList.items) {
                val ty = TypeCompiled.new(p, heap).asTy()
                paramTys.add(ty)
            }

            heap.allocSimple(
                TypingCallableAt2(
                    callable = TyCallable.new(ParamSpec.posOnly(paramTys, emptyList()), retTy),
                ),
            )
        }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)
}

internal class TypingCallableAt2(
    val callable: TyCallable,
) : StarlarkValue {
    override val TYPE: String get() = "typing.Callable"
    override val HAS_eval_type: Boolean get() = true

    override fun toString(): String = callable.toString()

    override fun evalType(): Ty = Ty.basic(TyBasic.Callable(callable))
}

/**
 * Marker for a callable value. Can be used in function signatures
 * for better documentation and type checking.
 */
@HiddenFromObjC
class StarlarkCallable<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: Value,
) : StarlarkTypeRepr,
    UnpackValue<StarlarkCallable<P, R>>,
    AllocValue {
    companion object {
        /** Wrap the value. */
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> uncheckedNew(value: Value): StarlarkCallable<P, R> = StarlarkCallable(value)

        fun starlarkTypeRepr(): Ty = Ty.callable(StarlarkCallableParamAny.params(), FrozenValueStarlarkTypeRepr.starlarkTypeRepr())

        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> starlarkTypeRepr(
            paramSpec: P,
            returnTypeRepr: R,
        ): Ty = Ty.callable(paramSpec.params(), returnTypeRepr.starlarkTypeRepr())
    }

    /** Convert to `FrozenValue` version. */
    fun unpackFrozen(): FrozenStarlarkCallable<P, R>? {
        val frozen = value.unpackFrozen() ?: return null
        return FrozenStarlarkCallable.uncheckedNew(frozen)
    }

    /** Erase parameter and return types. */
    fun erase(): StarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> = uncheckedNew(value)

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<StarlarkCallable<P, R>?> =
        if (value.vtable().hasInvoke) {
            Result.success(uncheckedNew(value))
        } else {
            Result.success(null)
        }

    override fun allocValue(heap: Heap): Value = value
}

/** Marker for a callable value. */
@HiddenFromObjC
class FrozenStarlarkCallable<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: FrozenValue,
) : StarlarkTypeRepr,
    AllocFrozenValue {
    companion object {
        /** Wrap the value. */
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> uncheckedNew(value: FrozenValue): FrozenStarlarkCallable<P, R> = FrozenStarlarkCallable(value)
    }

    override fun toString(): String = "FrozenStarlarkCallable($value)"

    /** Erase parameter and return types. */
    fun erase(): FrozenStarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> = uncheckedNew(value)

    override fun starlarkTypeRepr(): Ty = StarlarkCallable.starlarkTypeRepr()

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = value

    /** Convert to `Value`-version. */
    fun toCallable(): StarlarkCallable<P, R> = StarlarkCallable.uncheckedNew(value.toValue())
}

@HiddenFromObjC
fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> StarlarkCallable<P, R>.freeze(
    freezer: Freezer,
): Result<FrozenStarlarkCallable<P, R>> {
    val frozenValue = freezer.freeze(value).getOrElse { return Result.failure(it) }
    return Result.success(FrozenStarlarkCallable.uncheckedNew(frozenValue))
}

/**
 * More strict version of [`StarlarkCallable`].
 *
 * This checks not only that the value is callable,
 * but also that it is a callable with the correct signature.
 *
 * The implementation uses starlark-rust typechecker with all its limitations.
 * For example, if there are optional parameters in both value-def and this signature,
 * signature matching is ignored at the time of writing.
 *
 * Unpacking with this type is expensive:
 * usually it is OK to use it for code executed once at top-level scope (like `rule()`),
 * but not for code executed many times (like `partial()`).
 */
@HiddenFromObjC
class StarlarkCallableChecked<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: Value,
) : StarlarkTypeRepr,
    UnpackValue<StarlarkCallableChecked<P, R>>,
    AllocValue {
    override fun toString(): String = "StarlarkCallableChecked($value)"

    /** Convert to [`StarlarkCallable`]. */
    fun toUnchecked(): StarlarkCallable<P, R> = StarlarkCallable.uncheckedNew(value)

    override fun starlarkTypeRepr(): Ty = StarlarkCallable.starlarkTypeRepr()

    override fun unpackValueImpl(value: Value): Result<StarlarkCallableChecked<P, R>?> {
        // Check it is a callable first.
        if (!value.vtable().hasInvoke) {
            return Result.success(null)
        }

        // We need generic statics to cache this.
        val ty = StarlarkCallable.starlarkTypeRepr()

        return Ty.ofValue(value).checkIntersects(ty).map { intersects ->
            if (intersects) StarlarkCallableChecked(value) else null
        }
    }

    override fun allocValue(heap: Heap): Value = value
}

@HiddenFromObjC
class StarlarkCallableCheckedUnpackValue<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val paramSpec: P,
    val returnTypeRepr: R,
) : UnpackValue<StarlarkCallableChecked<P, R>> {
    override fun starlarkTypeRepr(): Ty =
        StarlarkCallable.starlarkTypeRepr(paramSpec, returnTypeRepr)

    override fun unpackValueImpl(value: Value): Result<StarlarkCallableChecked<P, R>?> {
        // Check it is a callable first.
        if (!value.vtable().hasInvoke) {
            return Result.success(null)
        }

        val ty = starlarkTypeRepr()

        return Ty.ofValue(value).checkIntersects(ty).map { intersects ->
            if (intersects) StarlarkCallableChecked(value) else null
        }
    }
}
