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

/// Like `panic!`, but aborts the process instead of unwinding.
///
/// Although we compile buck2 with `panic=abort`, this is safer because
/// others may copy-paste code.
// macro_rules! rtabort { ... }
// Kotlin: function that throws Error after printing to stderr.

// #[cold]
// pub(crate) fn rtabort_impl_fixed_string(file: &str, line: u32, message: &str) -> !
internal fun rtabortImplFixedString(file: String, line: Int, message: String): Nothing {
    rtabortImpl(file, line, message)
}

// #[cold]
// pub(crate) fn rtabort_impl(file: &str, line: u32, msg: Arguments) -> !
internal fun rtabortImpl(file: String, line: Int, msg: String): Nothing {
    val formatted = "$file:$line: abort: $msg"
    // Rust: io::Write::write_fmt(&mut io::stderr(), ...).ok(); process::abort();
    // Kotlin: print to stderr and throw Error.
    println(formatted)
    throw Error(formatted)
}

/// Convenience function matching the `rtabort!` macro invocation pattern.
// macro_rules! rtabort
internal fun rtabort(message: String, file: String = "", line: Int = 0): Nothing {
    rtabortImpl(file, line, message)
}
