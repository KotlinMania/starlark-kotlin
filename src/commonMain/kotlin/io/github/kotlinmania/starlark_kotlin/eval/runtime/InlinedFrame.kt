// port-lint: source src/eval/runtime/inlined_frame.rs
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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.Frame
import io.github.kotlinmania.starlark_kotlin.values.types.allocAny
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStr

/// When a function `a` is inlined into `b`, this struct contains
/// the inlined frame for expressions in `a` which now reside in `b`.
// #[derive(Debug, PartialEq)]
// pub(crate) struct InlinedFrame
data class InlinedFrame(
    val span: FrameSpan,
    val funValue: FrozenValue,
) {
    /// Recursively collect frames.
    ///
    /// Resulting frames are ordered bottom-to-top, same order as in `CallStack`.
    // pub(crate) fn extend_frames(&self, frames: &mut Vec<Frame>)
    fun extendFrames(frames: MutableList<Frame>) {
        frames.add(Frame(
            name = funValue.toValue().nameForCallStack(),
            location = span.span.toFileSpan(),
        ))
        span.inlinedFrames.extendFrames(frames)
    }
}

/// Stack of inlined frames (maybe empty).
// #[derive(Copy, Clone, Dupe, Debug, Default)]
// pub(crate) struct InlinedFrames
data class InlinedFrames(
    /// Linked list.
    var frames: FrozenRef<InlinedFrame>? = null,
) {
    /// Collect frames, bottom-to-top, same order as in `CallStack`.
    // pub(crate) fn extend_frames(self, frames: &mut Vec<Frame>)
    fun extendFrames(frames: MutableList<Frame>) {
        this.frames?.let { f ->
            f.value.extendFrames(frames)
        }
    }

    // fn to_inlined_frames(self) -> Vec<FrozenRef<'static, InlinedFrame>>
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

    /// Inline this stack into given span.
    ///
    /// E. g. when inlining `def a(): return {}` into `def b(): a()`,
    /// self is empty stack for expression `{}`, `span` is `a()` and `fun` is `a`.
    // pub(crate) fn inline_into(&mut self, span: FrameSpan, fun: FrozenValue, span_alloc: &mut InlinedFrameAlloc)
    fun inlineInto(
        span: FrameSpan,
        funValue: FrozenValue,
        spanAlloc: InlinedFrameAlloc,
    ) {
        frames = spanAlloc.allocFrame(InlinedFrame(
            span = FrameSpan(
                span = span.span,
                inlinedFrames = this.copy(),
            ),
            funValue = funValue,
        ))
        for (f in span.inlinedFrames.toInlinedFrames().reversed()) {
            frames = spanAlloc.allocFrame(InlinedFrame(
                span = FrameSpan(
                    span = f.value.span.span,
                    inlinedFrames = this.copy(),
                ),
                funValue = f.value.funValue,
            ))
        }
    }
}

/// Heap allocator for `InlinedFrame` which attempts to reuse previous allocation.
// pub(crate) struct InlinedFrameAlloc<'f>
class InlinedFrameAlloc(
    private val frozenHeap: FrozenHeap,
) {
    private var lastAlloc: FrozenRef<InlinedFrame>? = null

    companion object {
        // pub(crate) fn new(frozen_heap: &'f FrozenHeap) -> Self
        fun new(frozenHeap: FrozenHeap): InlinedFrameAlloc = InlinedFrameAlloc(frozenHeap)
    }

    // pub(crate) fn alloc_frame(&mut self, frame: InlinedFrame) -> FrozenRef<'static, InlinedFrame>
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

// --- Tests ---

// #[test] fn test_inline_into()
internal fun testInlineInto() {
    // Test frame inlining with this code:
    //
    // def a(): return {}
    // def b(): return a()
    // def c(): return b()
    //
    // def d(): return c()
    // def e(): return d()
    // def f(): return e()
    //
    // If `a` is inlined into `b` and then inlined into `c`,
    // then if `d` is inlined into `e` and then inlined into `f`,
    // and then `c` is inlined into `d` (which is already inlined into `f`),
    // the resulting stack trace should be `f`, `e`, `d`, `c`, `b`, `a`.

    val frozenHeap = FrozenHeap()

    fun makeSpan(heap: FrozenHeap, text: String): FrameSpan {
        val codemap = CodeMap("$text.bzl", text)
        val codemapRef = heap.allocAny(codemap)
        return FrameSpan(
            span = FrozenFileSpan.new(codemapRef, codemapRef.value.fullSpan()),
            inlinedFrames = InlinedFrames(),
        )
    }

    val spanAlloc = InlinedFrameAlloc.new(frozenHeap)

    fun assertStack(expected: List<String>, span: FrameSpan) {
        val frames = mutableListOf<Frame>()
        span.inlinedFrames.extendFrames(frames)
        val frameStrs = frames.map { f ->
            val spanStr = f.location?.sourceSpan() ?: ""
            val name = f.name.trim('"')
            "$spanStr in $name"
        }
        check(expected == frameStrs)
    }

    val a = makeSpan(frozenHeap, "{}")
    val b = makeSpan(frozenHeap, "a()")
    val c = makeSpan(frozenHeap, "b()")
    a.inlinedFrames.inlineInto(
        b,
        frozenHeap.allocStr("b").toFrozenValue(),
        spanAlloc,
    )
    a.inlinedFrames.inlineInto(
        c,
        frozenHeap.allocStr("c").toFrozenValue(),
        spanAlloc,
    )

    assertStack(listOf("b() in c", "a() in b"), a)

    val d = makeSpan(frozenHeap, "c()")
    val e = makeSpan(frozenHeap, "d()")
    val f = makeSpan(frozenHeap, "e()")

    d.inlinedFrames.inlineInto(
        e,
        frozenHeap.allocStr("e").toFrozenValue(),
        spanAlloc,
    )
    d.inlinedFrames.inlineInto(
        f,
        frozenHeap.allocStr("f").toFrozenValue(),
        spanAlloc,
    )

    assertStack(listOf("e() in f", "d() in e"), d)

    a.inlinedFrames.inlineInto(
        d,
        frozenHeap.allocStr("d").toFrozenValue(),
        spanAlloc,
    )

    assertStack(
        listOf("e() in f", "d() in e", "c() in d", "b() in c", "a() in b"),
        a,
    )
}
