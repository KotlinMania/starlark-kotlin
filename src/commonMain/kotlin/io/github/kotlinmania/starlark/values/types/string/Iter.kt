// port-lint: source src/values/types/string/iter.rs
package io.github.kotlinmania.starlark.values.types.string

import io.github.kotlinmania.starlark.any.ProvidesStaticType
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.allocTupleIter
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import kotlin.reflect.KClass

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

// Implementation of iterators for string type.

/** An opaque iterator over a string, produced by elems/codepoints. */
internal class StringIterableGen(
    val string: StringValue,
    val produceChar: Boolean // if not char, then int
) : ComplexValue, Trace, ProvidesStaticType {

    override fun toString(): String = "iterator"

    override val TYPE: String get() = "iterator"
    override val hasIterate: Boolean get() = true

    override val staticType: KClass<*> get() = StringIterableGen::class

    override fun iterate(_me: Value, heap: Heap): Result<Value> {
        // Lazy implementation: we allocate a tuple and then iterate over it.
        val iter = if (this.produceChar) {
            heap.allocTupleIter(this.string.asStr().map { c -> heap.allocStr(c.toString()).toValue() })
        } else {
            heap.allocTupleIter(this.string.asStr().map { c ->
                StarlarkInt.from(c.code).allocValue(heap)
            })
        }
        return Result.success(iter)
    }

    override fun trace(tracer: Tracer) {
        string.trace(tracer)
    }
}

internal fun iterateChars(
    string: StringValue,
    heap: Heap
): Value {
    return heap.allocComplex(StringIterableGen(
        string,
        true
    ))
}

internal fun iterateCodepoints(
    string: StringValue,
    heap: Heap
): Value {
    return heap.allocComplex(StringIterableGen(
        string,
        false
    ))
}
