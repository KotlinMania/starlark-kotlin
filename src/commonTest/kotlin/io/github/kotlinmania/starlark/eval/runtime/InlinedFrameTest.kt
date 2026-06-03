// port-lint: tests src/eval/runtime/inlined_frame.rs
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
import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.eval.runtime.frozenfilespan.FrozenFileSpan
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStr
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.types.allocAny
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InlinedFrameTest {
    @Test
    fun testInlineInto() {
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
            val frameStrs =
                frames.map { frame ->
                    val spanStr = frame.location?.sourceSpan() ?: ""
                    val name = frame.name.trim('"')
                    "$spanStr in $name"
                }
            assertEquals(expected, frameStrs)
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
}
