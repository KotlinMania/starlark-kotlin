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
import io.github.kotlinmania.starlark_kotlin.values.owned.default
import io.github.kotlinmania.starlark_kotlin.codemap.sourceSpan
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.merge
import io.github.kotlinmania.starlark_kotlin.codemap.endSpan
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.values.default

// #[derive(Debug, Copy, Clone, Dupe, PartialEq, Eq)]
// pub(crate) struct FrozenFileSpan {
//     file: FrozenRef<'static, CodeMap>,
//     span: Span,
// }
internal data class FrozenFileSpan(
    // file: FrozenRef<'static, CodeMap>
    private val file: FrozenRef<CodeMap>,
    // span: Span
    private val span: Span,
) {
    // impl Display for FrozenFileSpan
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
    override fun toString(): String = toFileSpan().toString()

    // impl FrozenFileSpan

    companion object {
        // impl Default for FrozenFileSpan
        // fn default() -> FrozenFileSpan
        fun default(): FrozenFileSpan {
            return FrozenFileSpan(FrozenRef(CodeMap.emptyStatic()), Span.default())
        }
    }

    // pub(crate) const fn new_unchecked(file: FrozenRef<'static, CodeMap>, span: Span) -> FrozenFileSpan
    // Kotlin: Use primary constructor directly for unchecked creation.

    // pub(crate) fn new(file: FrozenRef<'static, CodeMap>, span: Span) -> FrozenFileSpan
    /** Create a new [FrozenFileSpan], validating the span against the file. */
    constructor(file: FrozenRef<CodeMap>, span: Span, @Suppress("UNUSED_PARAMETER") validate: Boolean = true) : this(file, span) {
        // Check the span is valid: this will panic if the span is not valid.
        file.get().sourceSpan(span)
    }

    // pub(crate) fn file(&self) -> FrozenRef<'static, CodeMap>
    internal fun file(): FrozenRef<CodeMap> = file

    // pub(crate) fn span(&self) -> Span
    internal fun span(): Span = span

    // pub(crate) fn end_span(&self) -> FrozenFileSpan
    internal fun endSpan(): FrozenFileSpan {
        return FrozenFileSpan(file, span.endSpan())
    }

    // pub(crate) fn file_span_ref(&self) -> FileSpanRef<'static>
    internal fun fileSpanRef(): FileSpanRef {
        return FileSpanRef(file.get(), span)
    }

    // pub(crate) fn to_file_span(&self) -> FileSpan
    internal fun toFileSpan(): FileSpan {
        return FileSpan(file.get(), span)
    }

    // pub(crate) fn merge(&self, other: &FrozenFileSpan) -> FrozenFileSpan
    internal fun merge(other: FrozenFileSpan): FrozenFileSpan {
        return if (file == other.file) {
            FrozenFileSpan(file, span.merge(other.span))
        } else {
            // We need to pick something if we merge two spans from different files.
            this
        }
    }
}
