// port-lint: source src/eval/runtime/cheap_call_stack.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime

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

import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.CallStack
import io.github.kotlinmania.starlark_kotlin.Frame
import io.github.kotlinmania.starlark_kotlin.eval.runtime.InlinedFrames
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan

// A value akin to Frame, but can be created cheaply, since it doesn't resolve
// anything in advance.
// #[derive(Clone, Copy, Dupe)]
// struct CheapFrame<'v>
private class CheapFrame(
    var function: Value,
    var span: FrozenRef<FrameSpan>?,
) {
    // fn location(&self) -> Option<FileSpan>
    fun location(): FileSpan? {
        return span?.asRef()?.span?.toFileSpan()
    }

    // fn extend_frames(&self, frames: &mut Vec<Frame>)
    fun extendFrames(frames: MutableList<Frame>) {
        span?.let { s ->
            s.asRef().inlinedFrames.extendFrames(frames)
        }
        frames.add(toFrame())
    }

    // fn to_frame(&self) -> Frame
    fun toFrame(): Frame {
        return Frame(
            name = function.nameForCallStack(),
            location = location(),
        )
    }

    override fun toString(): String {
        return "Frame(function=$function, span=$span)"
    }
}

// #[derive(Debug, thiserror::Error)]
// enum CallStackError
private sealed class CallStackError(override val message: String) : Exception(message) {
    // Requested {0}-th top frame, but stack size is {1} (internal error)
    class StackIsTooShallowForNthTopFrame(n: Int, count: Int) :
        CallStackError("Requested $n-th top frame, but stack size is $count (internal error)")
    // Starlark call stack overflow
    class Overflow : CallStackError("Starlark call stack overflow")
    // Starlark call stack is already allocated
    class AlreadyAllocated : CallStackError("Starlark call stack is already allocated")
}

/** Starlark call stack. */
// #[derive(Debug)]
// pub(crate) struct CheapCallStack<'v>
// Kotlin: no lifetime parameter.
internal class CheapCallStack {
    private var count: Int = 0
    private var stack: Array<CheapFrame> = emptyArray()

    // unsafe impl Trace for CheapCallStack
    fun trace(tracer: Tracer) {
        for (i in 0 until count) {
            val holder = ValueHolder(stack[i].function)
            tracer.trace(holder)
            stack[i].function = holder.value
        }
        // Blank out unused frames (good practice).
        for (i in count until stack.size) {
            stack[i].function = Value.newNone()
            stack[i].span = null
        }
    }

    // pub(crate) fn alloc_if_needed(&mut self, max_size: usize) -> anyhow::Result<()>
    fun allocIfNeeded(maxSize: Int) {
        if (stack.isNotEmpty()) {
            if (stack.size == maxSize) {
                return
            } else {
                throw CallStackError.AlreadyAllocated()
            }
        }

        stack = Array(maxSize) {
            CheapFrame(
                function = Value.newNone(),
                span = null,
            )
        }
    }

    /**
     * Push an element to the stack. It is important the each `push` is paired
     * with a `pop`.
     */
    // pub(crate) fn push(&mut self, function, span) -> crate::Result<()>
    fun push(
        function: Value,
        span: FrozenRef<FrameSpan>?,
    ) {
        if (count >= stack.size) {
            throw CallStackError.Overflow()
        }
        stack[count] = CheapFrame(function = function, span = span)
        count += 1
    }

    /** Remove the top element from the stack. Called after `push`. */
    // pub(crate) fn pop(&mut self)
    fun pop() {
        check(count >= 1) { "CheapCallStack.pop: stack is empty" }
        count -= 1
    }

    /** Current size (in frames) of the stack. */
    // pub(crate) fn count(&self) -> usize
    fun count(): Int = count

    /**
     * The frame at the top of the stack. May be `None` if
     * either there the stack is empty, or the top of the stack lacks location
     * information (e.g. called from Rust).
     */
    // pub(crate) fn top_frame(&self) -> Option<Frame>
    fun topFrame(): Frame? {
        if (stack.isEmpty()) return null
        return stack.lastOrNull()?.toFrame()
    }

    /** The location at the top of the stack. */
    // pub(crate) fn top_location(&self) -> Option<FileSpan>
    fun topLocation(): FileSpan? {
        if (count == 0) return null
        return stack[count - 1].location()
    }

    // pub(crate) fn nth_location(&self, n: usize) -> Option<FileSpan>
    fun nthLocation(n: Int): FileSpan? {
        if (n >= count) return null
        return stack[count - 1 - n].location()
    }

    /** `n`-th element from the top of the stack. */
    // pub(crate) fn top_nth_function(&self, n: usize) -> anyhow::Result<Value<'v>>
    fun topNthFunction(n: Int): Value {
        return topNthFunctionOpt(n)
            ?: throw CallStackError.StackIsTooShallowForNthTopFrame(n, count)
    }

    // pub(crate) fn top_nth_function_opt(&self, n: usize) -> Option<Value<'v>>
    fun topNthFunctionOpt(n: Int): Value? {
        val index = (count - 1 - n)
        if (index < 0) return null
        return stack[index].function
    }

    // pub(crate) fn to_diagnostic_frames(&self, inlined_frames) -> CallStack
    internal fun toDiagnosticFrames(inlinedFrames: InlinedFrames): CallStack {
        // The first entry is just the entire module, so skip it
        val frames = mutableListOf<Frame>()
        for (i in 1 until count) {
            stack[i].extendFrames(frames)
        }
        inlinedFrames.extendFrames(frames)
        return CallStack(frames = frames)
    }

    /** List the entries on the stack as values. */
    // pub(crate) fn to_function_values(&self) -> Vec<Value<'v>>
    fun toFunctionValues(): List<Value> {
        return (1 until count).map { stack[it].function }
    }

    override fun toString(): String {
        return "CheapCallStack(count=$count, stack=${stack.take(count)})"
    }
}
