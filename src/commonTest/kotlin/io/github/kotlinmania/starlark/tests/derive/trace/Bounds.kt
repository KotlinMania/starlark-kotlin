// port-lint: source src/tests/derive/trace/bounds.rs
package io.github.kotlinmania.starlark.tests.derive.trace

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

// Only check it compiles.

import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer

// Kotlin: A doesn't need to be Trace here; the struct itself derives Trace
// In Kotlin, we make the struct implement Trace and trace `a` if it is Trace.
@Suppress("unused")
private class TestTraceWithBounds<A, B>(
    val a: A,
    val b: B,
) : Trace {
    override fun trace(tracer: Tracer) {
        if (a is Trace) {
            a.trace(tracer)
        }
    }
}

private class NotTrace

private inline fun <reified T : Trace> assertTrace() {}

@Suppress("unused")
private fun test() {
    assertTrace<TestTraceWithBounds<String, NotTrace>>()
}
