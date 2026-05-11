// port-lint: source tests/util.rs
package io.github.kotlinmania.starlark.tests

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex

internal class TestComplexValue(
    val inner: Value,
) : ComplexValue, Trace, AllocValue, AllocFrozenValue {

    override val TYPE: String get() = "TestComplexValue"

    override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()

    override fun toString(): String = "TestComplexValue<$inner>"

    override fun trace(tracer: Tracer) {
        // inner is a Value, traced by the GC
    }

    override fun allocValue(heap: Heap): Value {
        return heap.allocComplex(this)
    }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }
}

/**
 * There's no anyhow API to print error without rust backtrace
 * ([issue](https://github.com/dtolnay/anyhow/issues/300)).
 */
internal fun trimRustBacktrace(error: String): String {
    val pos = error.indexOf("\nStack backtrace:")
    return if (pos >= 0) {
        error.substring(0, pos).trimEnd()
    } else {
        error.trimEnd()
    }
}
