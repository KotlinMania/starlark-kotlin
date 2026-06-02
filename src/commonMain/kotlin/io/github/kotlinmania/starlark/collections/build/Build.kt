// port-lint: source build.rs
@file:Suppress("unused")

package io.github.kotlinmania.starlark.collections.build

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

/**
 * In Rust, `build.rs` detects whether the compiler is nightly and sets the
 * `rust_nightly` cfg flag, which enables SIMD-accelerated hash lookups in `vec_map/simd.rs`.
 *
 * Kotlin has no equivalent of nightly-specific features or build-script cfg flags.
 * The SIMD code path is not applicable to the Kotlin port; the scalar fallback
 * is always used (see [io.github.kotlinmania.starlark.collections.vecmap.simd.findHashInArray]).
 *
 * This file exists solely to maintain file-level parity with the Rust crate.
 */
internal const val RUST_NIGHTLY: Boolean = false
