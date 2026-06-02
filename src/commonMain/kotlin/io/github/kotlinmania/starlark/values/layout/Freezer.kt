// port-lint: source src/values/layout/freezer.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.eval.compiler.FrozenDefPostFreeze
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.AValueOrForwardUnpack
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.arena.Reservation

/** Used to `freeze` values by [Freeze.freeze][io.github.kotlinmania.starlark.values.Freeze.freeze]. */
class Freezer internal constructor(
    /** Freezing into this heap. */
    // pub(crate) heap: &'fv FrozenHeap,
    internal val heap: FrozenHeap,
) {
    /** Defs frozen by this freezer. */
    // pub(crate) frozen_defs: RefCell<Vec<FrozenRef<'static, FrozenDef>>>,
    internal val frozenDefs: MutableList<FrozenRef<FrozenDefPostFreeze>> = mutableListOf()

    companion object {
        internal fun new(heap: FrozenHeap): Freezer = Freezer(heap = heap)
    }

    /** Allocate a new value while freezing. Usually not a great idea. */
    fun <T : AllocFrozenValue> alloc(`val`: T): FrozenValue = `val`.allocFrozenValue(heap)

    //     T: AValue<'v2, ExtraElem = ()>,
    //     T::StarlarkValue: HeapSendable<'v2>,
    //     T::StarlarkValue: HeapSyncable<'v2>,
    internal fun <T : AValue> reserve(): Pair<FrozenValue, Reservation<T>> {
        val (fv, r, extra) = heap.reserveWithExtra<T>(0)
        check(extra == Unit) // debug_assert!(extra.is_empty())
        return Pair(fv, r)
    }

    /** Freeze a nested value while freezing yourself. */
    fun freeze(value: Value): Result<FrozenValue> {
        // Case 1: We have our value encoded in our pointer
        val x = value.unpackFrozen()
        if (x != null) {
            return Result.success(x)
        }

        // Case 2: We have already been replaced with a forwarding, or need to freeze
        val value = value.ptr.unpackPtrOpt()!!
        val header = AValueHeader.fromIndex(value)
        val repr = header.asRepr<StarlarkValue>()
        val unpacked: AValueOrForwardUnpack =
            if (repr.overwritten != null) {
                AValueOrForwardUnpack.Forward(repr.overwritten!!)
            } else {
                AValueOrForwardUnpack.Header(header)
            }
        return when (unpacked) {
            is AValueOrForwardUnpack.Forward -> Result.success(unpacked.forward.forwardPtr().unpackFrozenValue())
            is AValueOrForwardUnpack.Header -> unpacked.header.unpack().heapFreeze(repr, this)
        }
    }

    /**
     * Frozen heap where the values are frozen to.
     *
     * Can be used to allocate additional values while freezing.
     */
    fun frozenHeap(): FrozenHeap = heap
}
