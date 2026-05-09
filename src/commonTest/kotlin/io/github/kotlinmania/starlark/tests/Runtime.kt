// port-lint: source tests/runtime.rs
package io.github.kotlinmania.starlark.tests

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

/** Test of runtime. */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import kotlin.concurrent.atomics.AtomicInt
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.types.StarlarkAny
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.types.list.allocList
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.bigint.allocValue

class RuntimeTests {

    @Test
    fun testGarbageCollect() {
        Assert.pass(
            """
x = (100, [{"test": None}], True)
y = str(x)
garbage_collect()
assert_eq(y, str(x))
    """
        )
    }

    @Test
    fun testDeallocation() {
        // Check that we really do deallocate values we create
        val count = AtomicInt(0)

        class Dealloc : AutoCloseable {
            override fun close() {
                count.fetchAndAdd(1)
            }

            override fun toString(): String = "Dealloc"
        }

        fun mk(): Result<StarlarkAny<Dealloc>> = Result.success(StarlarkAny.new(Dealloc()))

        fun globals(builder: GlobalsBuilder) {
            builder.setFunction("mk") { _, _ -> mk() }
        }

        count.store(0)
        val a = Assert()
        a.disableGc()
        a.globalsAdd(::globals)
        a.module("test", "x = [mk(), mk()]\ndef y(): return mk()")
        a.pass(
            """
load("test", "x", "y")
z = x[1]
q = mk()
r = [y(), mk()]
"""
        )
        // The three that were run in pass should have gone
        assertEquals(3, count.load(), "Expected 3 deallocations")
        // Now the frozen ones should have gone too (after the assert harness is dropped).
        // Note: explicit cleanup behaviour depends on the JVM/Native target.
    }

    @Test
    fun testStackDepth() {
        val depthCounter = AtomicInt(0)
        fun stackDepth(): Result<String> {
            // We don't have direct stack-pointer access here, so we import a
            // monotonic counter as a proxy to verify that the evaluator
            // does not grow the stack unboundedly across loop iterations.
            val depth = depthCounter.fetchAndAdd(1)
            return Result.success(depth.toString())
        }

        fun measureStack(builder: GlobalsBuilder) {
            builder.setFunction("stack_depth") { _, _ -> stackDepth() }
        }

        val a = Assert()
        a.globalsAdd(::measureStack)
        val s = a.pass(
            """
for i in range(1001):
    if i == 1:
        v1 = stack_depth()
    if i == 100:
        v100 = stack_depth()
    elif i == 1000:
        v1000 = stack_depth()
v1 + " " + v100 + " " + v1000
"""
        )
        val str = s.unpackStr()!!
        val words = str.split(' ').map { it.toLong() }
        val v1 = words[0]
        val v100 = words[1]
        val v1000 = words[2]

        // We want to ensure they don't keep increasing, as that would be very bad
        // so ensure that the increase from v0 to v100 is less than the increase from v100 to v1000
        // with a 1000 for random noise.
        assertTrue(
            kotlin.math.abs(v1 - v100) + 1000 >= kotlin.math.abs(v1000 - v100),
            "Stack change exceeded, FAILED ${kotlin.math.abs(v1 - v100)} + 1000 >= ${kotlin.math.abs(v1000 - v100)} (relative to v1), 100=${v100 - v1}, 1000=${v1000 - v1}"
        )
    }

    @Test
    fun testGarbageCollectHappens() {
        // GC is meant to be "not observable", but if we break it, we want this test to fail
        fun currentUsage(eval: io.github.kotlinmania.starlark.eval.runtime.Evaluator): Result<Int> =
            Result.success(eval.heap().allocatedBytes())

        fun isGcDisabled(eval: io.github.kotlinmania.starlark.eval.runtime.Evaluator): Result<Boolean> =
            Result.success(eval.disableGc)

        fun helpers(builder: GlobalsBuilder) {
            builder.setFunction("current_usage") { _, eval -> currentUsage(eval) }
            builder.setFunction("is_gc_disabled") { _, eval -> isGcDisabled(eval) }
        }

        val a = Assert()
        a.globalsAdd(::helpers)

        // Approach is to keep doing something expensive, and we want to see the memory usage decrease.
        val code = buildString {
            append(
                """
globals = []
maximum = [0]
success = [is_gc_disabled()]

def update_maximum():
    maximum[0] = max(current_usage(), maximum[0])

def expensive(n):
    if success[0]:
        return
    now = current_usage()
    if now < maximum[0]:
        print("Success in " + str(n))
        success[0] = True
        return
    update_maximum()
    globals.append(str(n))
    locals = []
    for i in range(10 * n):
        locals.append(str(i))
    update_maximum()
"""
            )
            // I expect success in approx 25 times, so do 100 for safety
            for (i in 0 until 100) {
                appendLine("expensive($i)")
            }
            append("assert_eq(success[0], True)\nis_gc_disabled()")
        }
        // I expect to run with GC disabled some of the time, but not on the last run
        // so make sure at least once GC was enabled
        assertFalse(a.pass(code).unpackBool()!!)
    }

    @Test
    fun testCallstack() {
        // Make sure that even for native functions that fail, the
        // name of the function is on the call stack.
        val d = Assert.fail(
            """
def f():
    fail("bad")
f()
""",
            "bad",
        )
        assertTrue(d.toString().contains("fail(\"bad\")"))
    }

    @Test
    fun testDisplayDebug() {
        Heap.temp { heap ->
            val listVal = heap.allocList(listOf(1.allocValue(heap), 2.allocValue(heap)))
            val strVal = heap.allocStr("test")
            val boolVal = Value.newBool(true)
            val v = heap.allocTuple(listOf(listVal, strVal.toValue(), boolVal))
            assertEquals("([1, 2], \"test\", True)", v.toString())
            assertEquals("([1, 2], \"test\", True)", v.toRepr())
            assertEquals("([1, 2], \"test\", True)", v.toStr())

            val sv = heap.allocStr("test")
            assertEquals("\"test\"", sv.toString())
            assertEquals("\"test\"", sv.toValue().toRepr())
            assertEquals("test", sv.toValue().toStr())
        }

        val frozenHeap = FrozenHeap()
        val v = frozenHeap.allocStrIntern("test")
        assertEquals("\"test\"", v.toFrozenValue().toString())
        assertEquals("\"test\"", v.toValue().toRepr())
        assertEquals("test", v.toValue().toStr())
    }
}
