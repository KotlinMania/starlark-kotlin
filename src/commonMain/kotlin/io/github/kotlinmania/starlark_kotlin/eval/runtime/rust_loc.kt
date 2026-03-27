// port-lint: source src/eval/runtime/rust_loc.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.rust_loc

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.eval.bc.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.newUnchecked

/** Initialize a `FrozenRef<FrameSpan>` with Kotlin file and line number. */
// macro_rules! rust_loc { ... }
// Kotlin: macro replaced with a function that creates a native code location.
// Callers should cache the result in a companion `val` for static-like behavior.
internal fun rustLoc(file: String, line: Int, column: Int = 0): FrozenRef<FrameSpan> {
    val nativeCodeMap = NativeCodeMap.new(file, line, column)
    val codeMap = nativeCodeMap.toCodeMap()
    val frozenFileSpan = FrozenFileSpan.newUnchecked(
        FrozenRef.new(codeMap),
        NativeCodeMap.FULL_SPAN,
    )
    val frameSpan = FrameSpan.new(frozenFileSpan)
    return FrozenRef.new(frameSpan)
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
