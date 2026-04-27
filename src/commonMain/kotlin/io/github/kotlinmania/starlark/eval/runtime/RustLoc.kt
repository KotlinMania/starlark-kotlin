// port-lint: source src/eval/runtime/rustLoc.rs
package io.github.kotlinmania.starlark.eval.runtime.rustloc

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.eval.runtime.frozenfilespan.FrozenFileSpan
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan

private const val NATIVE_SOURCE = "<native>"
private val NATIVE_FULL_SPAN = Span(Pos(0), Pos(NATIVE_SOURCE.length))

/// Initialize `loc` to `FrozenRef<FrameSpan>` with file and line number.
// Kotlin has no macros, so callers pass file/line/column explicitly.
internal fun rustLoc(file: String, line: Int, column: Int = 0): FrozenRef<FrameSpan> {
    val codeMap = CodeMap("$file:$line:$column", NATIVE_SOURCE)
    val frozenFileSpan = FrozenFileSpan.newUnchecked(FrozenRef.new(codeMap), NATIVE_FULL_SPAN)
    val frameSpan = FrameSpan.new(frozenFileSpan)
    return FrozenRef.new(frameSpan)
}
