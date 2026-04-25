// port-lint: source build.rs
package io.github.kotlinmania.starlark

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
 * Build configuration for starlark-kotlin.
 *
 * The Rust source `build.rs` is a Cargo build script that detects whether
 * the Rust compiler is nightly or stable, and sets the `rust_nightly` cfg flag.
 *
 * In Kotlin Multiplatform, build configuration is handled by Gradle rather
 * than a runtime script. This module provides equivalent build information
 * as compile-time constants.
 */
internal object BuildConfig {
    /**
     * Whether nightly/unstable features are enabled.
     *
     * Corresponds to the Rust `#[cfg(rust_nightly)]` flag set by build.rs.
     * In Kotlin, this is always false since there is no nightly/stable distinction.
     */
    const val NIGHTLY: Boolean = false
}
