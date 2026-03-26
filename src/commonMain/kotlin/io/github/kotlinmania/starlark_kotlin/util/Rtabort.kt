// port-lint: source src/util/rtabort.rs
package io.github.kotlinmania.starlark_kotlin.util.rtabort

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
 * Like `panic!`, but aborts the process instead of unwinding.
 *
 * Although we compile buck2 with `panic=abort`, this is safer because
 * others may copy-paste code.
 *
 * In Kotlin, there is no unwinding — exceptions propagate normally.
 * This module provides an abort-style error that terminates the process
 * similar to Rust's `process::abort()`.
 */

// macro_rules! rtabort { ... }
// Kotlin: We use a function instead of a macro.

/**
 * Abort the process with an error message.
 *
 * Prints the message to stderr and terminates the process.
 */
// pub(crate) fn rtabort_impl(file: &str, line: u32, msg: Arguments) -> !
internal fun rtabortImpl(file: String, line: Int, msg: String): Nothing {
    // In Rust, this prints to stderr then calls process::abort().
    // In Kotlin Multiplatform, we throw an Error (not Exception) to signal
    // an unrecoverable condition, similar to process abort.
    throw Error("$file:$line: abort: $msg")
}

// pub(crate) fn rtabort_impl_fixed_string(file: &str, line: u32, message: &str) -> !
internal fun rtabortImplFixedString(file: String, line: Int, message: String): Nothing {
    rtabortImpl(file, line, message)
}

/**
 * Abort the process with a formatted message.
 *
 * In Rust this is the `rtabort!` macro. In Kotlin, call this function directly.
 */
internal fun rtabort(message: String): Nothing {
    rtabortImpl("<unknown>", 0, message)
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
