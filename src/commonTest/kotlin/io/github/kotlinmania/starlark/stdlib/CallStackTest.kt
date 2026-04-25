// port-lint: source src/stdlib/call_stack.rs (tests)
package io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

/** Tests for the call_stack module. */
class CallStackTests {
    /** Test basic call_stack. */
    @Test
    fun testSimple() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue(
            """
def foo():
    return bar()

def bar():
    s = call_stack()
    return all([
        "foo()" in s,
        "bar()" in s,
        "call_stack()" in s,
    ])

foo()
            """.trimIndent()
        )
    }

    /** Test strip_frames=1. */
    @Test
    fun testStripOne() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue(
            """
def foo():
    return bar()

def bar():
    s = call_stack(strip_frames=1)
    return all([
        "foo()" in s,
        "bar()" in s,
        "call_stack()" not in s,
    ])

foo()
            """.trimIndent()
        )
    }

    /** Test strip_frames removes all. */
    @Test
    fun testStripAll() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue(
            """
def foo():
    return bar()

def bar():
    s = call_stack(strip_frames=10)
    return not bool(s)

foo()
            """.trimIndent()
        )
    }

    /** Test call_stack_frame returns correct frame data. */
    @Test
    fun testCallStackFrame() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue(
            """
def foo():
    return bar()

def bar():
    return all([
            "call_stack_frame" == call_stack_frame(0).func_name,
            "assert.bzl" == call_stack_frame(0).module_path,
            "bar" == call_stack_frame(1).func_name,
            "assert.bzl" == call_stack_frame(1).module_path,
            "foo" == call_stack_frame(2).func_name,
            "assert.bzl" == call_stack_frame(2).module_path,
            None == call_stack_frame(3),
            None == call_stack_frame(4),
        ])

foo()
            """.trimIndent()
        )
    }
}

