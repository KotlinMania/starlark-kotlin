// port-lint: source src/eval/runtime/frozenFileSpan.rs
package io.github.kotlinmania.starlark.eval.runtime.frozenfilespan

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.codemap.FileSpanRef
import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.values.FrozenRef

@ConsistentCopyVisibility
data class FrozenFileSpan private constructor(
    private val file: FrozenRef<CodeMap>,
    private val span: Span,
) {

    override fun toString(): String = toFileSpan().toString()

    companion object {

        private val EMPTY_CODEMAP: CodeMap = CodeMap("", "")

        val DEFAULT: FrozenFileSpan = newUnchecked(FrozenRef(EMPTY_CODEMAP), Span.DEFAULT)

        fun default(): FrozenFileSpan = DEFAULT


        fun newUnchecked(file: FrozenRef<CodeMap>, span: Span): FrozenFileSpan {
            return FrozenFileSpan(file, span)
        }

        fun new(file: FrozenRef<CodeMap>, span: Span): FrozenFileSpan {
            // Check the span is valid: this will panic if the span is not valid.
            file.asRef().sourceSpan(span)
            return newUnchecked(file, span)
        }
    }

    internal fun file(): FrozenRef<CodeMap> = file

    internal fun span(): Span = span

    internal fun endSpan(): FrozenFileSpan {
        return FrozenFileSpan(file, span.endSpan())
    }

    internal fun fileSpanRef(): FileSpanRef {
        return FileSpanRef(file.asRef(), span)
    }

    internal fun toFileSpan(): FileSpan {
        return FileSpan(file.asRef(), span)
    }

    internal fun merge(other: FrozenFileSpan): FrozenFileSpan {
        return if (file == other.file) {
            FrozenFileSpan(file, span.merge(other.span))
        } else {
            // We need to pick something if we merge two spans from different files.
            this
        }
    }
}
