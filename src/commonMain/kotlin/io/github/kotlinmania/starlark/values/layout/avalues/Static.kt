// port-lint: source src/values/layout/avalues/static_.rs
package io.github.kotlinmania.starlark.values.layout.avalues

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

import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.ConstTypeId
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId

/**
 * For types which are only allocated statically (never in heap).
 * Technically we can use `AValueSimple` for these, but this is more explicit and safe.
 */
// pub(crate) struct AValueBasic<T>(PhantomData<T>);
internal class AValueBasic<T : StarlarkValue> : AValue {
    // fn extra_len(_value: &T) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        error("Basic types don't appear in the heap")
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int {
        error("Basic types don't appear in the heap")
    }

    // unsafe fn heap_freeze(me, freezer) -> Result<FrozenValue>
    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        error("Basic types don't appear in the heap")
    }

    // unsafe fn heap_copy(me, tracer) -> Value
    override fun heapCopy(tracer: Tracer): Value {
        error("Basic types don't appear in the heap")
    }

    // fn unpack() -> StarlarkValue
    override fun unpack(): StarlarkValue {
        error("Basic types don't appear in the heap")
    }

    // fn total_memory_for_profile(_value: &Self::StarlarkValue) -> usize
    override fun totalMemoryForProfile(value: StarlarkValue): Int {
        // This avalue is always statically allocated so don't charge anyone for the memory.
        //
        // The fact that we need this at all is a bit weird - it comes about only because of the way
        // we do retained heap profiling. We first freeze the heap and then walk the *unfrozen* heap
        // looking for all the forwards. Since some non-statically allocated values freeze into
        // statically allocated ones (list, dict), that might point here
        return 0
    }
}

/** Allocate simple value statically. */
// pub struct AllocStaticSimple<T: StarlarkValue<'static>>(AValueRepr<AValueImpl<AValueBasic<T>>>)
class AllocStaticSimple<T : StarlarkValue> internal constructor(
    private val repr: AValueRepr<AValueImpl<AValueBasic<T>>>,
) {
    companion object {
        /**
         * Allocate a value statically.
         * The vtable carries the actual [StarlarkValue] so that
         * `Value.getRef().downcastRef<T>()` and method dispatch work correctly.
         */
        // pub const fn alloc(value: T) -> Self
        fun <T : StarlarkValue> alloc(value: T): AllocStaticSimple<T> {
            val typeId = ConstTypeId.of(value::class)
            val vtable =
                AValueVTable(
                    staticTypeOfValue = typeId,
                    starlarkTypeId = StarlarkTypeId.fromTypeId(typeId),
                    typeName = value.TYPE,
                    isStr = false,
                    memorySizeFn = { _ -> ValueAllocSize.new(AlignedSize.newBytes(16)) },
                    heapFreezeFn = { _, _, _ -> error("AllocStaticSimple: heapFreeze not supported") },
                    heapCopyFn = { _, _ -> error("AllocStaticSimple: heapCopy not supported") },
                    starlarkValue = value,
                    hasInvoke = value.HAS_invoke,
                    hasEvalType = value.HAS_eval_type,
                    hasIterate = value.HAS_iterate,
                    hasEquals = value.HAS_equals,
                )
            return AllocStaticSimple(
                AValueRepr(
                    AValueHeader(vtable),
                    AValueImpl.new<AValueBasic<T>>(value),
                ),
            )
        }
    }

    /** Get the value. */
    // pub fn unpack(&'static self) -> FrozenValueTyped<'static, T>
    fun unpack(): FrozenValueTyped<T> = FrozenValueTyped.newRepr(repr)

    /** Get the value. */
    // pub fn to_frozen_value(&'static self) -> FrozenValue
    fun toFrozenValue(): FrozenValue = unpack().toFrozenValue()
}
