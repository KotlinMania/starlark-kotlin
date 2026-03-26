// port-lint: source src/stdlib/call_stack.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.call_stack

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

/** Implementation of `call_stack` function. */

import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneOr
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import kotlin.test.Test

/** A frame of the call-stack. */
internal data class StackFrame(
    /** The name of the entry on the call-stack. */
    val name: String,
    /** The location of the definition, or `null` for native functions. */
    val location: FileSpan?,
) : StarlarkValue, AllocValue {

    /** Get the methods for this value. */
    fun getMethods(): Methods? {
        return RES.methods(::stackFrameMethods)
    }

    /** Allocate this value on a heap. */
    override fun allocValue(heap: Heap): Value =
        heap.allocComplexNoFreeze(this)

    /** Display representation. */
    override fun toString(): String = "<StackFrame ...>"

    companion object {
        /** The Starlark type name. */
        const val TYPE: String = "StackFrame"
        private val RES: MethodsStatic = MethodsStatic()
    }
}

/** Returns the name of the entry on the call-stack. */
private fun funcName(thisRef: StackFrame): String =
    thisRef.name

/** Returns a path of the module, or `null` for native functions. */
private fun modulePath(thisRef: StackFrame): NoneOr<String> =
    when (val location = thisRef.location) {
        null -> NoneOr.None
        else -> NoneOr.Other(location.file.filename)
    }

/** Attribute methods for [StackFrame]. */
private fun stackFrameMethods(builder: MethodsBuilder) {
    /** Returns the name of the entry on the call-stack. */
    builder.attribute("func_name") { thisValue ->
        funcName(thisValue as StackFrame)
    }
    /** Returns a path of the module, or `null` for native functions. */
    builder.attribute("module_path") { thisValue ->
        modulePath(thisValue as StackFrame)
    }
}

/**
 * Get a textual representation of the call stack.
 *
 * This is intended only for debugging purposes to display to a human and
 * should not be considered stable or parseable.
 *
 * [stripFrames] will pop N frames from the top of the call stack, which can
 * be useful to hide non-interesting lines - for example, stripFrames=1
 * will hide the call to and location of `call_stack()` itself.
 */
private fun callStack(stripFrames: Int, eval: Evaluator): String {
    val stack = eval.callStack()
    stack.frames.subList(
        maxOf(0, stack.frames.size - stripFrames),
        stack.frames.size,
    ).clear()
    return stack.toString()
}

/**
 * Get a structural representation of the n-th call stack frame.
 *
 * With `n=0` returns `callStackFrame` itself.
 * Returns `None` if `n` is greater than or equal to the stack size.
 */
private fun callStackFrame(n: Int, eval: Evaluator): NoneOr<StackFrame> {
    val stack = eval.callStack()
    if (n >= stack.frames.size) {
        return NoneOr.None
    }
    return when (val frame = stack.frames.getOrNull(stack.frames.size - n - 1)) {
        null -> NoneOr.None
        else -> NoneOr.Other(
            StackFrame(
                name = frame.name,
                location = frame.location,
            )
        )
    }
}

/** Register the call_stack globals. */
internal fun global(builder: GlobalsBuilder) {
    builder.function("call_stack") { args, eval ->
        val stripFrames: Int = args.namedOptional("strip_frames") ?: 0
        callStack(stripFrames, eval)
    }

    builder.function("call_stack_frame") { args, eval ->
        val n: Int = args.positional(0)
        callStackFrame(n, eval)
    }
}

/** Tests for the call_stack module. */
internal class CallStackTests {
    /** Test basic call stack. */
    @Test
    fun testSimple() {
        val a = Assert()
        a.globalsAdd(::global)
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

    /** Test strip one frame. */
    @Test
    fun testStripOne() {
        val a = Assert()
        a.globalsAdd(::global)
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

    /** Test strip all frames. */
    @Test
    fun testStripAll() {
        val a = Assert()
        a.globalsAdd(::global)
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

    /** Test call_stack_frame. */
    @Test
    fun testCallStackFrame() {
        val a = Assert()
        a.globalsAdd(::global)
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
