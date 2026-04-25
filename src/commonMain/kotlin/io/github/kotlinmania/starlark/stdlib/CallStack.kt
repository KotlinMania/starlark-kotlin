// port-lint: source src/stdlib/call_stack.rs
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

/** Implementation of `call_stack` function. */

import io.github.kotlinmania.starlark.CallStack
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.namedOptional
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplexNoFreeze
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.none.NoneOr
import io.github.kotlinmania.starlark.values.types.none.allocValue
import io.github.kotlinmania.starlark.assert.Assert

/** A frame of the call-stack. */
internal data class StackFrame(
    /** The name of the entry on the call-stack. */
    val name: String,
    /** The location of the definition, or `null` for native functions. */
    val location: FileSpan?,
) : StarlarkValue, AllocValue {

    override val TYPE: String get() = Companion.TYPE

    override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()

    /** Get the methods for StackFrame values. */
    override fun getMethods(): Methods? =
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

/** Returns a path of the module, or `null` for native functions. */
private fun modulePath(thisRef: StackFrame): NoneOr<String> =
    when (val location = thisRef.location) {
        null -> NoneOr.None
        else -> NoneOr.Other(location.file.filename)
    }

/** Define attribute methods on StackFrame values. */
private fun stackFrameMethods(builder: MethodsBuilder) {
    builder.setAttribute("func_name") { thisValue, heap ->
        val frame = thisValue.downcastRefUnchecked<StackFrame>()
        Result.success(heap.allocStr(funcName(frame)).toValue())
    }
    builder.setAttribute("module_path") { thisValue, heap ->
        val frame = thisValue.downcastRefUnchecked<StackFrame>()
        val result = modulePath(frame)
        Result.success(when (result) {
            is NoneOr.None -> Value.newNone()
            is NoneOr.Other -> heap.allocStr(result.value).toValue()
        })
    }
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
    val truncatedSize = maxOf(0, stack.frames.size - stripFrames)
    val truncated = CallStack(frames = stack.frames.subList(0, truncatedSize))
    return truncated.toString()
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

/** Register call_stack and call_stack_frame globals. */
internal fun callStackGlobal(builder: GlobalsBuilder) {
    builder.setFunction("call_stack") { args, eval ->
        eval.heap().allocStr(callStack(args.namedOptional("strip_frames") ?: 0, eval))
    }
    builder.setFunction("call_stack_frame") { args, eval ->
        callStackFrame(args.positional(0), eval).allocValue(eval.heap())
    }
}
