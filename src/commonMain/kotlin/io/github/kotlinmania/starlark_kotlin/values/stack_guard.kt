// port-lint: source src/values/stack_guard.rs
package io.github.kotlinmania.starlark_kotlin.values.stack_guard

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

//! Guard to check we don't recurse too deeply with nested operations like Equals.

import io.github.kotlinmania.starlark_kotlin.values.error.ControlError

// Maximum recursion level for comparison
// TODO(dmarting): those are rather short, maybe make it configurable?
// #[cfg(debug_assertions)]
// const MAX_RECURSION: u32 = 200;
// #[cfg(not(debug_assertions))]
// const MAX_RECURSION: u32 = 3000;
private const val MAX_RECURSION: Int = 3000

// A thread-local counter is used to detect too deep recursion.
//
// Thread-local is chosen instead of explicit function "recursion" parameter
// for two reasons:
// * It's possible to propagate stack depth across external functions like
//   `Display::to_string` where passing a stack depth parameter is hard
// * We need to guarantee that stack depth is not lost in complex invocation
//   chains like function calls compare which calls native function which calls
//   starlark function which calls to_str. We could change all evaluation stack
//   signatures to accept some "context" parameters, but passing it as
//   thread-local is easier.
// thread_local! { static STACK_DEPTH: Cell<u32> = const { Cell::new(0) }; }
// Kotlin: simple mutable var; single-threaded Starlark evaluation.
private var stackDepth: Int = 0

/// Stored previous stack depth before calling `try_inc`.
///
/// Stores that previous stack depths back to thread-local on drop.
// #[must_use]
// pub struct StackGuard { prev_depth: u32 }
class StackGuard internal constructor(
    private val prevDepth: Int,
) : AutoCloseable {
    // impl Drop for StackGuard
    // fn drop(&mut self)
    override fun close() {
        stackDepth = prevDepth
    }
}

/// Increment stack depth.
// fn inc() -> StackGuard
private fun inc(): StackGuard {
    val prevDepth = stackDepth
    stackDepth = prevDepth + 1
    return StackGuard(prevDepth)
}

/// Check stack depth does not exceed configured max stack depth.
// fn check() -> anyhow::Result<()>
private fun check() {
    if (stackDepth >= MAX_RECURSION) {
        throw ControlError.TooManyRecursionLevel.toException()
    }
}

/// Try increment stack depth.
///
/// Return opaque object which resets stack to previous value
/// on [close][StackGuard.close].
///
/// If stack depth exceeds configured limit, throw error.
// pub(crate) fn stack_guard() -> anyhow::Result<StackGuard>
internal fun stackGuard(): StackGuard {
    check()
    return inc()
}
