// port-lint: tests src/tests/util.rs
package io.github.kotlinmania.starlark_kotlin.tests

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.ComplexValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex

// #[derive(Trace, Freeze, Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// #[display("TestComplexValue<{}>", _0)]
// pub(crate) struct TestComplexValue<V: ValueLifetimeless>(pub(crate) V)
internal class TestComplexValue(
    val inner: Value,
) : ComplexValue, Trace, AllocValue, AllocFrozenValue {

    // #[starlark_value(type = "TestComplexValue")]
    override val TYPE: String get() = "TestComplexValue"

    override fun starlarkTypeRepr(): Ty = getTypeStarlarkRepr()

    override fun toString(): String = "TestComplexValue<$inner>"

    // #[derive(Trace)] — traces the inner Value field.
    override fun trace(tracer: Tracer) {
        val holder = ValueHolder(inner)
        tracer.trace(holder)
    }

    // impl AllocValue for TestComplexValue<Value>
    override fun allocValue(heap: Heap): Value {
        return heap.allocComplex(this)
    }

    // impl AllocFrozenValue for TestComplexValue<FrozenValue>
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }
}

/**
 * There's no anyhow API to print error without rust backtrace
 * ([issue](https://github.com/dtolnay/anyhow/issues/300)).
 */
// pub(crate) fn trim_rust_backtrace(error: &str) -> &str
internal fun trimRustBacktrace(error: String): String {
    val pos = error.indexOf("\nStack backtrace:")
    return if (pos >= 0) {
        error.substring(0, pos).trimEnd()
    } else {
        error.trimEnd()
    }
}
