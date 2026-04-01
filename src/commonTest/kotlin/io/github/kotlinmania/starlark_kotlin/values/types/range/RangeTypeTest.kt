// port-lint: tests src/values/types/range/range_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.range

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.unpackInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import kotlin.test.Test
import kotlin.test.assertEquals

private fun range(start: Int, stop: Int, step: Int): Range =
    Range(start, stop, NonZeroI32.new(step)!!)

private fun rangeStartStop(start: Int, stop: Int): Range = range(start, stop, 1)

private fun rangeStop(stop: Int): Range = rangeStartStop(0, stop)

internal class RangeTypeTest {
    @Test
    fun testLengthStop() {
        assertEquals(0, rangeStop(0).length().getOrThrow())
        assertEquals(17, rangeStop(17).length().getOrThrow())
    }

    @Test
    fun testLengthStartStop() {
        assertEquals(20, rangeStartStop(10, 30).length().getOrThrow())
        assertEquals(0, rangeStartStop(10, -30).length().getOrThrow())
        assertEquals(Int.MAX_VALUE, rangeStartStop(0, Int.MAX_VALUE).length().getOrThrow())
        assertEquals(true, rangeStartStop(-1, Int.MAX_VALUE).length().isFailure)
    }

    @Test
    fun testLengthStartStopStep() {
        assertEquals(5, range(0, 10, 2).length().getOrThrow())
        assertEquals(5, range(0, 9, 2).length().getOrThrow())
        assertEquals(0, range(0, 10, -2).length().getOrThrow())
        assertEquals(5, range(10, 0, -2).length().getOrThrow())
        assertEquals(5, range(9, 0, -2).length().getOrThrow())
        assertEquals(1, range(4, 14, 10).length().getOrThrow())
    }

    @Test
    fun testEq() {
        assertEquals(rangeStop(0), range(2, 1, 3))
    }

    @Test
    fun testRangeExhaustive() {
        val ranges = mutableListOf<Range>()
        for (start in -3..3) {
            for (stop in -3..3) {
                for (step in -3..2) {
                    val adjustedStep = if (step >= 0) step + 1 else step
                    ranges.add(range(start, stop, adjustedStep))
                }
            }
        }
        assertEquals(294, ranges.size)

        Heap.temp { heap ->
            for (x in ranges) {
                val full = x.iterate(heap).getOrThrow().asSequence().map { it.unpackInt().getOrThrow()!! }.toList()
                assertEquals(x.length().getOrThrow(), full.size)
                for ((index, value) in full.withIndex()) {
                    assertEquals(x.start + x.step.get() * index, value)
                }
            }

            for (x in ranges) {
                for (y in ranges) {
                    val left = x.iterate(heap).getOrThrow().asSequence().toList()
                    val right = y.iterate(heap).getOrThrow().asSequence().toList()
                    assertEquals(x == y, left == right)
                }
            }
        }
    }

    @Test
    fun testMaxLen() {
        Assert.eq(
            InlineInt.MAX.toString(),
            "len(range(${InlineInt.MAX}))",
        )
        Assert.eq(
            InlineInt.MAX.toString(),
            "len(range(${InlineInt.MIN}, -1))",
        )
    }
}
