// port-lint: source src/eval/runtime/inlined_frame.rs
package io.github.kotlinmania.starlark.eval.runtime

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

import io.github.kotlinmania.starlark.Frame
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.types.allocAny

/**
 * When a function `a` is inlined into `b`, this struct contains
 * the inlined frame for expressions in `a` which now reside in `b`.
 */
data class InlinedFrame(
    val span: FrameSpan,
    val funValue: FrozenValue,
) {
    /**
     * Recursively collect frames.
     *
     * Resulting frames are ordered bottom-to-top, same order as in `CallStack`.
     */
    fun extendFrames(frames: MutableList<Frame>) {
        frames.add(
            Frame(
                name = funValue.toValue().nameForCallStack(),
                location = span.span.toFileSpan(),
            ),
        )
        span.inlinedFrames.extendFrames(frames)
    }
}

/** Stack of inlined frames (maybe empty). */
data class InlinedFrames(
    /** Linked list. */
    var frames: FrozenRef<InlinedFrame>? = null,
) {
    /** Collect frames, bottom-to-top, same order as in `CallStack`. */
    fun extendFrames(frames: MutableList<Frame>) {
        this.frames?.let { f ->
            f.value.extendFrames(frames)
        }
    }

    private fun toInlinedFrames(): List<FrozenRef<InlinedFrame>> {
        val r = mutableListOf<FrozenRef<InlinedFrame>>()
        var framesIter = this
        while (true) {
            val f = framesIter.frames ?: break
            r.add(f)
            framesIter = f.value.span.inlinedFrames
        }
        return r
    }

    /**
     * Inline this stack into given span.
     *
     * E. g. when inlining `def a(): return {}` into `def b(): a()`,
     * self is empty stack for expression `{}`, `span` is `a()` and `fun` is `a`.
     */
    fun inlineInto(
        span: FrameSpan,
        funValue: FrozenValue,
        spanAlloc: InlinedFrameAlloc,
    ) {
        frames =
            spanAlloc.allocFrame(
                InlinedFrame(
                    span =
                        FrameSpan(
                            span = span.span,
                            inlinedFrames = this.copy(),
                        ),
                    funValue = funValue,
                ),
            )
        for (f in span.inlinedFrames.toInlinedFrames().reversed()) {
            frames =
                spanAlloc.allocFrame(
                    InlinedFrame(
                        span =
                            FrameSpan(
                                span = f.value.span.span,
                                inlinedFrames = this.copy(),
                            ),
                        funValue = f.value.funValue,
                    ),
                )
        }
    }
}

/** Heap allocator for `InlinedFrame` which attempts to reuse previous allocation. */
class InlinedFrameAlloc(
    private val frozenHeap: FrozenHeap,
) {
    private var lastAlloc: FrozenRef<InlinedFrame>? = null

    companion object {
        fun new(frozenHeap: FrozenHeap): InlinedFrameAlloc = InlinedFrameAlloc(frozenHeap)
    }

    fun allocFrame(frame: InlinedFrame): FrozenRef<InlinedFrame> {
        lastAlloc?.let { last ->
            if (last.value == frame) {
                return last
            }
        }
        val allocated = frozenHeap.allocAny(frame)
        lastAlloc = allocated
        return allocated
    }
}
