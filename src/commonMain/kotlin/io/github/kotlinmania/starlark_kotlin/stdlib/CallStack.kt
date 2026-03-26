// port-lint: source src/stdlib/call_stack.rs
package io.github.kotlinmania.starlark_kotlin.stdlib

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

/** A frame of the call-stack. */
internal data class StackFrame(
    /** The name of the entry on the call-stack. */
    val name: String,
    /** The location of the definition, or `null` for native Rust functions. */
    val location: FileSpan?,
) : StarlarkValue, AllocValue {

    /** Get the methods for StackFrame values. */
    fun getMethods(): Methods? =
        RES.methods(::stackFrameMethods)

    /** Allocate this value on a heap. */
    override fun allocValue(heap: Heap): Value =
        heap.allocComplexNoFreeze(this)

    /** Display for StackFrame. */
    override fun toString(): String = "<StackFrame ...>"

    companion object {
        const val TYPE: String = "StackFrame"
        private val RES: MethodsStatic = MethodsStatic()
    }
}

/** Returns the name of the entry on the call-stack. */
private fun funcName(thisRef: StackFrame): String =
    thisRef.name

/** Returns a path of the module, or null for native Rust functions. */
private fun modulePath(thisRef: StackFrame): NoneOr<String> =
    if (thisRef.location != null)
        NoneOr.Other(thisRef.location.file.filename)
    else NoneOr.None

/** Define attribute methods on StackFrame values. */
private fun stackFrameMethods(builder: MethodsBuilder) {
    builder.attribute("func_name") { thisValue -> funcName(thisValue as StackFrame) }
    builder.attribute("module_path") { thisValue -> modulePath(thisValue as StackFrame) }
}

/**
 * Get a textual representation of the call stack.
 *
 * This is intended only for debugging purposes to display to a human and
 * should not be considered stable or parseable.
 *
 * strip_frames will pop N frames from the top of the call stack, which can
 * be useful to hide non-interesting lines - for example, strip_frames=1
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
 * With `n=0` returns `call_stack_frame` itself.
 * Returns `None` if `n` is greater than or equal to the stack size.
 */
private fun callStackFrame(n: Int, eval: Evaluator): NoneOr<StackFrame> {
    val stack = eval.callStack()
    if (n >= stack.frames.size) {
        return NoneOr.None
    }
    val frame = stack.frames.getOrNull(stack.frames.size - n - 1)
        ?: return NoneOr.None
    return NoneOr.Other(StackFrame(name = frame.name, location = frame.location))
}

/** Register call_stack and call_stack_frame globals. */
internal fun callStackGlobal(builder: GlobalsBuilder) {
    builder.function("call_stack") { args, eval ->
        callStack(args.namedOptional("strip_frames") ?: 0, eval)
    }
    builder.function("call_stack_frame") { args, eval ->
        callStackFrame(args.positional(0), eval)
    }
}

/** Tests for the call_stack module. */
internal class CallStackTests {
    /** Test basic call_stack. */
    fun testSimple() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue("def foo():\n    return bar()\n\ndef bar():\n    s = call_stack()\n    return all([\n        \"foo()\" in s,\n        \"bar()\" in s,\n        \"call_stack()\" in s,\n    ])\n\nfoo()")
    }

    /** Test strip_frames=1. */
    fun testStripOne() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue("def foo():\n    return bar()\n\ndef bar():\n    s = call_stack(strip_frames=1)\n    return all([\n        \"foo()\" in s,\n        \"bar()\" in s,\n        \"call_stack()\" not in s,\n    ])\n\nfoo()")
    }

    /** Test strip_frames removes all. */
    fun testStripAll() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue("def foo():\n    return bar()\n\ndef bar():\n    s = call_stack(strip_frames=10)\n    return not bool(s)\n\nfoo()")
    }

    /** Test call_stack_frame struct. */
    fun testCallStackFrame() {
        val a = Assert()
        a.globalsAdd(::callStackGlobal)
        a.isTrue("def foo():\n    return bar()\n\ndef bar():\n    return all([\n            \"call_stack_frame\" == call_stack_frame(0).func_name,\n            \"assert.bzl\" == call_stack_frame(0).module_path,\n            \"bar\" == call_stack_frame(1).func_name,\n            \"assert.bzl\" == call_stack_frame(1).module_path,\n            \"foo\" == call_stack_frame(2).func_name,\n            \"assert.bzl\" == call_stack_frame(2).module_path,\n            None == call_stack_frame(3),\n            None == call_stack_frame(4),\n        ])\n\nfoo()")
    }
}
