// port-lint: source src/values/layout/freezer.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForward
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForwardUnpack
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.Reservation
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

/** Used to `freeze` values by [Freeze.freeze][io.github.kotlinmania.starlark_kotlin.values.Freeze.freeze]. */
class Freezer internal constructor(
    /** Freezing into this heap. */
    internal val heap: FrozenHeap,
) {
    /** Defs frozen by this freezer. */
    internal val frozenDefs: MutableList<FrozenRef<FrozenDef>> = mutableListOf()

    /** Allocate a new value while freezing. Usually not a great idea. */
    fun <T : AllocFrozenValue> alloc(value: T): FrozenValue {
        return value.allocFrozenValue(heap)
    }

    internal fun <T : AValue> reserve(): Pair<FrozenValue, Reservation<T>> {
        val (fv, r, _) = heap.reserveWithExtra<T>(0)
        return Pair(fv, r)
    }

    /** Freeze a nested value while freezing yourself. */
    fun freeze(value: Value): FreezeResult<FrozenValue> {
        // Case 1: We have our value encoded in our pointer
        val unpacked = value.unpackFrozen()
        if (unpacked != null) {
            return Result.success(unpacked)
        }

        // Case 2: We have already been replaced with a forwarding, or need to freeze
        val ptrIndex = value.ptr.unpackPtrOpt()!!
        val header = AValueHeader.fromIndex(ptrIndex)
        val aValueOrForward = AValueOrForward.Header(header)
        return when (val result = aValueOrForward.unpack()) {
            is AValueOrForwardUnpack.Forward -> {
                Result.success(result.forward.forwardPtr().unpackFrozenValue())
            }
            is AValueOrForwardUnpack.Header -> {
                result.header.unpack().heapFreeze(this)
            }
        }
    }

    /**
     * Frozen heap where the values are frozen to.
     *
     * Can be used to allocate additional values while freezing.
     */
    fun frozenHeap(): FrozenHeap {
        return heap
    }
}
