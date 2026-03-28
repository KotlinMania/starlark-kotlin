// port-lint: source src/values/types/string/iter.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTupleIter

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

// Implementation of iterators for string type.

/** An opaque iterator over a string, produced by elems/codepoints */
internal data class StringIterableGen<V : ValueLike<V>>(
    val string: StringValue,
    val produceChar: Boolean // if not char, then int
) : ProvidesStaticType {
    override fun toString(): String = "iterator"
}

internal fun iterateChars(
    string: StringValue,
    heap: Heap
): ValueOfUnchecked<StarlarkIter<String>> {
    return ValueOfUnchecked.new(heap.allocComplex(StringIterableGen<Value>(
        string,
        true
    )))
}

internal fun iterateCodepoints(
    string: StringValue,
    heap: Heap
): ValueOfUnchecked<StarlarkIter<String>> {
    return ValueOfUnchecked.new(heap.allocComplex(StringIterableGen<Value>(
        string,
        false
    )))
}

// StarlarkValue implementation for StringIterableGen
internal fun <V : ValueLike<V>> StringIterableGen<V>.iterate(
    me: Value,
    heap: Heap
): Result<Value> {
    // Lazy implementation: we allocate a tuple and then iterate over it.
    val iter = if (this.produceChar) {
        heap.allocTupleIter(this.string.asStr().asSequence().map { c -> c.toString().allocStringValue(heap) })
    } else {
        heap.allocTupleIter(this.string.asStr().asSequence().map { c -> io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt.of(c.code).allocValue(heap) })
    }
    return Result.success(iter)
}

// Real types should be imported from their respective packages
