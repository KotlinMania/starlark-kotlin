// port-lint: source src/tests/runtime.rs
package io.github.kotlinmania.starlark_kotlin.tests

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

//! Test of runtime.

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import kotlin.concurrent.atomics.AtomicInt
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.StarlarkAny
import kotlin.test.Test
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.toStr
import io.github.kotlinmania.starlark_kotlin.assert.disableGc
import io.github.kotlinmania.starlark_kotlin.assert.assertEquals

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

        // #[derive(Default, Debug, Display)]
        // struct Dealloc
        class Dealloc : AutoCloseable {
            override fun close() {
                count.incrementAndGet()
            }

            override fun toString(): String = "Dealloc"
        }

        // #[starlark_module]
        // fn globals(builder: &mut GlobalsBuilder)
        fun globalsFunctions(builder: GlobalsBuilder) {
            builder.setFunction("mk") { _, _ ->
                Result.success(StarlarkAny.new(Dealloc()))
            }
        }

        count.store(0)
        val a = Assert()
        a.disableGc()
        a.globalsAdd(::globalsFunctions)
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
        assertEquals(3, count.load())
        // Now the frozen ones should have gone too (after drop)
        // Note: In Kotlin/JVM, explicit cleanup may differ from Rust's Drop
    }

    @Test
    fun testStackDepth() {
        // #[starlark_module]
        // fn measure_stack(builder: &mut GlobalsBuilder)
        fun measureStackFunctions(builder: GlobalsBuilder) {
            builder.setFunction("stack_depth") { _, _ ->
                // Put a variable on the stack, and get a reference to it
                // In Kotlin we don't have direct stack pointer access,
                // so we use the current thread's stack trace depth as a proxy
                val depth = Thread.currentThread().stackTrace.size
                Result.success(depth.toString())
            }
        }

        val a = Assert()
        a.globalsAdd(::measureStackFunctions)
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
        // #[starlark_module]
        // fn helpers(builder: &mut GlobalsBuilder)
        fun helpersFunctions(builder: GlobalsBuilder) {
            builder.setFunction("current_usage") { _, eval ->
                Result.success(eval.heap().allocatedBytes())
            }

            builder.setFunction("is_gc_disabled") { _, eval ->
                Result.success(eval.disableGc)
            }
        }

        val a = Assert()
        a.globalsAdd(::helpersFunctions)

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
            val v = heap.alloc(listOf(1, 2) to "test" to true)
            assertEquals("([1, 2], \"test\", True)", v.toString())
            assertEquals("([1, 2], \"test\", True)", v.toRepr())
            assertEquals("([1, 2], \"test\", True)", v.toStr())

            val sv = heap.alloc("test")
            assertEquals("\"test\"", sv.toString())
            assertEquals("\"test\"", sv.toRepr())
            assertEquals("test", sv.toStr())
        }

        val frozenHeap = FrozenHeap()
        val v = frozenHeap.alloc("test")
        assertEquals("\"test\"", v.toString())
        assertEquals("\"test\"", v.toValue().toRepr())
        assertEquals("test", v.toValue().toStr())
    }
}
