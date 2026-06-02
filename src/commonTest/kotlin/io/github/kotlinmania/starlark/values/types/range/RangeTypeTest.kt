// port-lint: tests src/values/types/range/range_type.rs
@file:Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
package io.github.kotlinmania.starlark.values.types.range

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.bigint.unpackInt
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private fun range(start: Int, stop: Int, step: Int): Range =
    Range(
        start,
        stop,
        requireNotNull(NonZeroI32.new(step)) { "range step must be non-zero, got $step" },
    )

private fun rangeStartStop(start: Int, stop: Int): Range = range(start, stop, 1)

private fun rangeStop(stop: Int): Range = rangeStartStop(0, stop)

internal class RangeTypeTest {
    @Test
    fun testRangeRejectsZeroStep() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                range(0, 1, 0)
            }
        assertEquals("range step must be non-zero, got 0", error.message)
    }

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
                val iter = heap.allocSimple(x).iterate(heap).getOrThrow()
                val full =
                    iter
                        .asSequence()
                        .map {
                            val unpacked = it.unpackInt().getOrThrow()
                            requireNotNull(unpacked) { "Expected unpackInt() to produce a non-null Int" }
                        }.toList()
                iter.close()
                assertEquals(x.length().getOrThrow(), full.size)
                for ((index, value) in full.withIndex()) {
                    assertEquals(x.start + x.step.get() * index, value)
                }
            }

            for (x in ranges) {
                for (y in ranges) {
                    val leftIter = heap.allocSimple(x).iterate(heap).getOrThrow()
                    val left = leftIter.asSequence().toList()
                    leftIter.close()

                    val rightIter = heap.allocSimple(y).iterate(heap).getOrThrow()
                    val right = rightIter.asSequence().toList()
                    rightIter.close()
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

    @Test
    fun testSliceStepOverflowIncludesOperationContext() {
        val error =
            assertFailsWith<ValueError.Runtime> {
                Heap.temp { heap ->
                    range(0, Int.MAX_VALUE, Int.MAX_VALUE).slice(
                        start = null,
                        stop = null,
                        stride = 2.allocValue(heap),
                        heap = heap,
                    )
                }
            }
        val message = error.message!!
        assertTrue(message.contains("Integer overflow in InlineInt multiplication"), message)
        assertTrue(message.contains("Int(2) * Int(${Int.MAX_VALUE})"), message)
    }

    @Test
    fun testSliceStopOverflowIncludesAdditionContext() {
        val error =
            assertFailsWith<ValueError.Runtime> {
                Heap.temp { heap ->
                    range(2, Int.MAX_VALUE, Int.MAX_VALUE - 1).slice(
                        start = null,
                        stop = null,
                        stride = null,
                        heap = heap,
                    )
                }
            }
        val message = error.message!!
        assertTrue(message.contains("Integer overflow in addition"), message)
        assertTrue(message.contains("2 + ${Int.MAX_VALUE - 1}"), message)
    }
}
