// port-lint: source src/read_line.rs
package io.github.kotlinmania.starlark_kotlin.read_line

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

// This is not public API, but it is used by Starlark command line utility.
// #![doc(hidden)]

/**
 * Wrapper for readline functionality.
 *
 * In Rust, this wraps rustyline for non-wasm32 targets and provides a stub for wasm32.
 * In Kotlin Multiplatform, we use expect/actual for platform-specific readline.
 * The commonMain implementation provides the interface.
 */
// pub struct ReadLine
expect class ReadLine(histfileEnv: String) {
    /**
     * Read a line with the given prompt.
     * Returns `null` on EOF or interrupt.
     */
    // pub fn read_line(&mut self, prompt: &str) -> anyhow::Result<Option<String>>
    fun readLine(prompt: String): String?
}
