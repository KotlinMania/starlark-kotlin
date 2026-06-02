// port-lint: source src/eval/runtime/frozen_file_span.rs
package io.github.kotlinmania.starlark.eval.runtime.frozenfilespan

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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.FileSpanRef
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.values.FrozenRef

//     span: Span,

/**
 * A file span that references a frozen (immutable) [CodeMap] and a [Span] within it.
 *
 * This is the frozen counterpart of [FileSpan], used after the module has been frozen.
 * The [file] and [span] are both immutable once created.
 */
@ConsistentCopyVisibility
data class FrozenFileSpan private constructor(
    /** The frozen code map this span belongs to. */
    private val file: FrozenRef<CodeMap>,
    /** The span within [file]. */
    private val span: Span,
) {
    override fun toString(): String = toFileSpan().toString()

    companion object {
        /** A default empty [CodeMap] singleton, equivalent to Rust's `CodeMap::empty_static()`. */
        private val EMPTY_CODEMAP: CodeMap = CodeMap("", "")

        /**
         * Returns a default [FrozenFileSpan] with an empty code map and default span.
         */
        val DEFAULT: FrozenFileSpan = newUnchecked(FrozenRef(EMPTY_CODEMAP), Span.DEFAULT)

        /** Convenience function equivalent to [DEFAULT], mirroring Rust's `Default` impl. */
        fun default(): FrozenFileSpan = DEFAULT

        /**
         * Creates a new [FrozenFileSpan] without validating that [span] is within [file].
         */
        fun newUnchecked(file: FrozenRef<CodeMap>, span: Span): FrozenFileSpan = FrozenFileSpan(file, span)

        /**
         * Creates a new [FrozenFileSpan], validating that [span] is valid within [file].
         *
         * Throws if the span is not valid for the given code map.
         */
        fun new(file: FrozenRef<CodeMap>, span: Span): FrozenFileSpan {
            // Check the span is valid: this will panic if the span is not valid.
            file.asRef().sourceSpan(span)
            return newUnchecked(file, span)
        }
    }

    /** Returns the frozen code map reference. */
    internal fun file(): FrozenRef<CodeMap> = file

    /** Returns the span within the code map. */
    internal fun span(): Span = span

    /**
     * Returns a new [FrozenFileSpan] pointing to the end of this span.
     */
    internal fun endSpan(): FrozenFileSpan = FrozenFileSpan(file, span.endSpan())

    /**
     * Converts this frozen span to a [FileSpanRef].
     */
    internal fun fileSpanRef(): FileSpanRef = FileSpanRef(file.asRef(), span)

    /**
     * Converts this frozen span to an owned [FileSpan].
     */
    internal fun toFileSpan(): FileSpan = FileSpan(file.asRef(), span)

    /**
     * Merges this span with [other]. If both reference the same file, returns a span
     * covering both. Otherwise returns `this`.
     */
    internal fun merge(other: FrozenFileSpan): FrozenFileSpan =
        if (file == other.file) {
            FrozenFileSpan(file, span.merge(other.span))
        } else {
            // We need to pick something if we merge two spans from different files.
            this
        }
}
