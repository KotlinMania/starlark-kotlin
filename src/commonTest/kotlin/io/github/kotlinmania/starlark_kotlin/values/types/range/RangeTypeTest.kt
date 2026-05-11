<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/range/RangeTypeTest.kt
// port-lint: source tests:src/values/types/range/range_type.rs
package io.github.kotlinmania.starlark.values.types.range

/*
 * Copyright 2018 The Starlark in Rust Authors.
=======
// port-lint: tests src/values/types/range/range_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.range

/*
 * Copyright 2019 The Starlark in Rust Authors.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/range/RangeTypeTest.kt
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/range/RangeTypeTest.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/range/RangeTypeTest.kt
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

<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/range/RangeTypeTest.kt
import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.int.inline.InlineInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RangeTypeTest {

    private fun range(start: Int, stop: Int, step: Int): Range {
        return Range(start = start, stop = stop, step = step)
    }

    private fun rangeStartStop(start: Int, stop: Int): Range {
        return range(start, stop, 1)
    }

    private fun rangeStop(stop: Int): Range {
        return rangeStartStop(0, stop)
    }

    @Test
    fun lengthStop() {
        assertEquals(0, rangeStop(0).length().getOrNull())
        assertEquals(17, rangeStop(17).length().getOrNull())
    }

    @Test
    fun lengthStartStop() {
        assertEquals(20, rangeStartStop(10, 30).length().getOrNull())
        assertEquals(0, rangeStartStop(10, -30).length().getOrNull())
        assertEquals(Int.MAX_VALUE, rangeStartStop(0, Int.MAX_VALUE).length().getOrNull())
        assertTrue(rangeStartStop(-1, Int.MAX_VALUE).length().isFailure)
    }

    @Test
    fun lengthStartStopStep() {
        assertEquals(5, range(0, 10, 2).length().getOrNull())
        assertEquals(5, range(0, 9, 2).length().getOrNull())
        assertEquals(0, range(0, 10, -2).length().getOrNull())
        assertEquals(5, range(10, 0, -2).length().getOrNull())
        assertEquals(5, range(9, 0, -2).length().getOrNull())
        assertEquals(1, range(4, 14, 10).length().getOrNull())
    }

    @Test
    fun eq() {
=======
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
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
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/range/RangeTypeTest.kt
        assertEquals(rangeStop(0), range(2, 1, 3))
    }

    @Test
    fun testRangeExhaustive() {
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/range/RangeTypeTest.kt
        // The range implementation is fairly hairy. Lots of corner cases etc.
        // Especially around equality, length.
        // Therefore, generate ranges exhaustively over a very small range
        // and test lots of properties about them.
        val ranges = ArrayList<Range>(294)
        for (start in -3..3) {
            for (stop in -3..3) {
                for (stepIn in -3..2) {
                    val step = if (stepIn >= 0) stepIn + 1 else stepIn
                    ranges.add(range(start, stop, step))
                }
            }
        }
        assertEquals(294, ranges.size) // Assert we don't accidentally take too long

        Heap.temp { heap ->
            for (x in ranges) {
                val xv = heap.allocSimple(x)
                val full: List<Value> = xv.iterate(heap).getOrThrow().toList()
                assertEquals(full.size, xv.length().getOrThrow())
                for ((i, v) in full.withIndex()) {
                    assertEquals(v, xv.at(heap.alloc(i), heap).getOrThrow())
                }
            }

            // Takes 294^2 steps - but completes instantly
            for (x in ranges) {
                for (y in ranges) {
                    val xv = heap.allocSimple(x)
                    val yv = heap.allocSimple(y)
                    assertEquals(
                        xv == yv,
                        xv.iterate(heap).getOrThrow().toList() == yv.iterate(heap).getOrThrow().toList(),
                    )
=======
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
                val full = iter.asSequence().map { it.unpackInt().getOrThrow()!! }.toList()
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
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/range/RangeTypeTest.kt
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
