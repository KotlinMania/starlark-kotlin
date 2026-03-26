// port-lint: source src/eval/runtime/frozen_file_span.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span

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
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.FileSpanRef
import io.github.kotlinmania.starlark_kotlin.codemap.sourceSpan
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.endSpan
import io.github.kotlinmania.starlark_kotlin.codemap.Span

/**
 * A file span that references a frozen (immutable) [CodeMap] and a [Span] within it.
 *
 * This is the frozen counterpart of [FileSpan], used after the module has been frozen.
 * The [file] and [span] are both immutable once created.
 *
 * Rust: `FrozenFileSpan` — `#[derive(Debug, Copy, Clone, Dupe, PartialEq, Eq)]`
 */
internal data class FrozenFileSpan(
    /** The frozen code map this span belongs to. */
    private val file: FrozenRef<CodeMap>,
    /** The span within [file]. */
    private val span: Span,
) {
    // Rust: impl Display for FrozenFileSpan
    override fun toString(): String = toFileSpan().toString()

    companion object {
        /**
         * Returns a default [FrozenFileSpan] with an empty code map and default span.
         *
         * Rust: `impl Default for FrozenFileSpan`
         */
        fun default(): FrozenFileSpan {
            return FrozenFileSpan(FrozenRef(CodeMap.emptyStatic()), Span.default())
        }
    }

    // The primary constructor serves as the unchecked factory (Rust: `new_unchecked`).

    /**
     * Creates a new [FrozenFileSpan], validating that [span] is valid within [file].
     *
     * Throws if the span is not valid for the given code map.
     *
     * Rust: `fn new(file, span) -> FrozenFileSpan`
     *
     * @param validate Disambiguates from the primary constructor; always `true`.
     */
    constructor(
        file: FrozenRef<CodeMap>,
        span: Span,
        @Suppress("UNUSED_PARAMETER") validate: Boolean = true,
    ) : this(file, span) {
        // Check the span is valid: this will panic if the span is not valid.
        file.get().sourceSpan(span)
    }

    /** Returns the frozen code map reference. */
    // Rust: fn file(&self) -> FrozenRef<'static, CodeMap>
    internal fun file(): FrozenRef<CodeMap> = file

    /** Returns the span within the code map. */
    // Rust: fn span(&self) -> Span
    internal fun span(): Span = span

    /**
     * Returns a new [FrozenFileSpan] pointing to the end of this span.
     *
     * Rust: `fn end_span(&self) -> FrozenFileSpan`
     */
    internal fun endSpan(): FrozenFileSpan {
        return FrozenFileSpan(file, span.endSpan())
    }

    /**
     * Converts this frozen span to a [FileSpanRef].
     *
     * Rust: `fn file_span_ref(&self) -> FileSpanRef<'static>`
     */
    internal fun fileSpanRef(): FileSpanRef {
        return FileSpanRef(file.get(), span)
    }

    /**
     * Converts this frozen span to an owned [FileSpan].
     *
     * Rust: `fn to_file_span(&self) -> FileSpan`
     */
    internal fun toFileSpan(): FileSpan {
        return FileSpan(file.get(), span)
    }

    /**
     * Merges this span with [other]. If both reference the same file, returns a span
     * covering both. Otherwise returns `this`.
     *
     * Rust: `fn merge(&self, other: &FrozenFileSpan) -> FrozenFileSpan`
     */
    internal fun merge(other: FrozenFileSpan): FrozenFileSpan {
        return if (file == other.file) {
            FrozenFileSpan(file, span.merge(other.span))
        } else {
            // We need to pick something if we merge two spans from different files.
            this
        }
    }
}
