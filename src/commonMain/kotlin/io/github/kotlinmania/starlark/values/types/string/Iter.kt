// port-lint: source src/values/types/string/iter.rs
package io.github.kotlinmania.starlark.values.types.string

import io.github.kotlinmania.starlark.any.ProvidesStaticType
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.allocTupleIter
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.allocValue
import kotlin.reflect.KClass

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
//     string: V::String,
//     produce_char: bool,
internal class StringIterableGen(
    val string: StringValue,
    val produceChar: Boolean, // if not char, then int
) : ComplexValue,
    Trace,
    ProvidesStaticType,
    Freeze<StarlarkValue> {
    override fun toString(): String = "iterator"

    override val TYPE: String get() = "iterator"
    override val HAS_iterate: Boolean get() = true

    override val staticType: KClass<*> get() = StringIterableGen::class

    override fun iterate(me: Value, heap: Heap): Result<Value> {
        // Lazy implementation: we allocate a tuple and then iterate over it.
        val iter =
            if (this.produceChar) {
                heap.allocTupleIter(this.string.asStr().map { c -> heap.allocStr(c.toString()) })
            } else {
                heap.allocTupleIter(
                    this.string.asStr().map { c ->
                        StarlarkInt.from(c.code).allocValue(heap)
                    },
                )
            }
        return Result.success(iter)
    }

    override fun freeze(freezer: Freezer): Result<StarlarkValue> {
        val frozenStr = string.freeze(freezer).getOrElse { return Result.failure(it) }
        return Result.success(StringIterableGen(frozenStr.toStringValue(), produceChar))
    }

    override fun trace(
        @Suppress("unused") tracer: Tracer,
    ) {
        // In Rust, Trace is derived. The StringValue's inner Value
        // would be traced. Since Kotlin's GC handles memory, this is a no-op.
    }
}

internal fun iterateChars(
    string: StringValue,
    heap: Heap,
): Value {
    // Rust returns ValueOfUnchecked<StarlarkIter<String>> but the Kotlin port
    // cannot represent this phantom type annotation because StarlarkIter does not
    // implement StarlarkTypeRepr yet. Callers only use .get() on the result anyway.
    return heap.allocComplex(
        StringIterableGen(
            string,
            true,
        ),
    )
}

internal fun iterateCodepoints(
    string: StringValue,
    heap: Heap,
): Value {
    // Rust returns ValueOfUnchecked<StarlarkIter<String>> but the Kotlin port
    // cannot represent this phantom type annotation because StarlarkIter does not
    // implement StarlarkTypeRepr yet. Callers only use .get() on the result anyway.
    return heap.allocComplex(
        StringIterableGen(
            string,
            false,
        ),
    )
}
