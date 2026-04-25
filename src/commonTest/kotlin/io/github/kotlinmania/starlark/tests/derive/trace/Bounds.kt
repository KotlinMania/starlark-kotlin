// port-lint: source src/tests/derive/trace/bounds.rs
package io.github.kotlinmania.starlark.tests.derive.trace

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

// Only check it compiles.

import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer

// #[derive(Trace)]
// #[trace(bound = "A: Trace<'v>, B: 'static")]
// struct TestTraceWithBounds<A, B>
// Kotlin: A doesn't need to be Trace here; the struct itself derives Trace
// with #[trace(static)] on b and the bound says A: Trace, B: 'static.
// In Kotlin, we make the struct implement Trace and trace `a` if it is Trace.
@Suppress("unused")
private class TestTraceWithBounds<A, B>(
    val a: A,
    // #[trace(static)]
    val b: B,
) : Trace {
    override fun trace(tracer: Tracer) {
        if (a is Trace) {
            a.trace(tracer)
        }
        // b is #[trace(static)], so not traced
    }
}

// struct NotTrace;
private class NotTrace

// fn assert_trace<'v, T: Trace<'v>>() {}
private inline fun <reified T : Trace> assertTrace() {}

// fn test()
@Suppress("unused")
private fun test() {
    assertTrace<TestTraceWithBounds<String, NotTrace>>()
}
