// port-lint: source src/eval/runtime/frame_span.rs
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
import io.github.kotlinmania.starlark_kotlin.codemap.DEFAULT

/** Span of the call frame (including inlined call frames). */
internal data class FrameSpan(
    val span: FrozenFileSpan,
    /** Parent frames. */
    val inlinedFrames: InlinedFrames,
) {
    constructor(span: FrozenFileSpan) : this(
        span = span,
        inlinedFrames = InlinedFrames(frames = null),
    )

    fun endSpan(): FrameSpan {
        return FrameSpan(
            span = span.endSpan(),
            inlinedFrames = inlinedFrames,
        )
    }

    fun merge(other: FrameSpan): FrameSpan {
        return FrameSpan(
            span = span.merge(other.span),
            inlinedFrames = inlinedFrames,
        )
    }

    override fun toString(): String {
        return span.toString()
    }

    companion object {
        fun new(span: FrozenFileSpan): FrameSpan {
            return FrameSpan(span)
        }

        val DEFAULT = FrameSpan(
            span = FrozenFileSpan.DEFAULT,
            inlinedFrames = InlinedFrames(frames = null),
        )
    }
}
