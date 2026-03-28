// port-lint: source src/values/layout/avalues/static_.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalues

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.newRepr
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueVTable

/// For types which are only allocated statically (never in heap).
/// Technically we can use `AValueSimple` for these, but this is more explicit and safe.
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

    // unsafe fn heap_freeze(me, freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        error("Basic types don't appear in the heap")
    }

    // unsafe fn heap_copy(me, tracer) -> Value
    override fun heapCopy(tracer: Tracer): Value {
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

/// Allocate simple value statically.
// pub struct AllocStaticSimple<T: StarlarkValue<'static>>(AValueRepr<AValueImpl<AValueBasic<T>>>)
class AllocStaticSimple<T : StarlarkValue> internal constructor(
    private val repr: AValueRepr<AValueImpl<AValueBasic<T>>>,
) {
    companion object {
        /// Allocate a value statically.
        // pub const fn alloc(value: T) -> Self
        fun <T : StarlarkValue> alloc(value: T): AllocStaticSimple<T> {
            return AllocStaticSimple(
                AValueRepr.withMetadata(
                    AValueVTable.new<AValueBasic<T>>(),
                    AValueImpl(AValueBasic(), value),
                )
            )
        }
    }

    /// Get the value.
    // pub fn unpack(&'static self) -> FrozenValueTyped<'static, T>
    fun unpack(): FrozenValueTyped<T> {
        return FrozenValueTyped.newRepr(repr)
    }

    /// Get the value.
    // pub fn to_frozen_value(&'static self) -> FrozenValue
    fun toFrozenValue(): FrozenValue {
        return unpack().toFrozenValue()
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest.

// #[test]
// fn test_alloc_static_simple()
internal fun testAllocStaticSimple() {
    // In Rust this uses #[derive] and #[starlark_value] macros.
    // In Kotlin we define a simple StarlarkValue manually.
    class MySimpleValue(val value: UInt) : StarlarkValue {
        override fun toString(): String = "MySimpleValue"
    }

    val allocated = AllocStaticSimple.alloc(MySimpleValue(17u))
    check(17u == allocated.unpack().asRef().value)
}
