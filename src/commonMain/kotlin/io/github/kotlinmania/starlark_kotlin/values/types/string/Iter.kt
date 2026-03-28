// port-lint: source src/values/types/string/iter.rs
package io.github.kotlinmania.starlark_kotlin.values.types.string

import io.github.kotlinmania.starlark_kotlin.any.ProvidesStaticType
import io.github.kotlinmania.starlark_kotlin.values.ComplexValue
import io.github.kotlinmania.starlark_kotlin.values.Trace
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTupleIter
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.allocValue
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

/// An opaque iterator over a string, produced by elems/codepoints
// #[derive(Debug, Trace, Coerce, Display, Freeze, NoSerialize, ProvidesStaticType, Allocative)]
// #[display("iterator")]
// #[repr(C)]
// struct StringIterableGen<'v, V: ValueLike<'v>> {
//     string: V::String,
//     produce_char: bool,
// }
internal class StringIterableGen(
    val string: StringValue,
    val produceChar: Boolean // if not char, then int
) : ComplexValue, Trace, ProvidesStaticType {

    // #[display("iterator")]
    override fun toString(): String = "iterator"

    // #[starlark_value(type = "iterator")]
    override val TYPE: String get() = "iterator"

    override val staticType: KClass<*> get() = StringIterableGen::class

    // unsafe fn iterate(&self, _me: Value<'v>, heap: Heap<'v>) -> crate::Result<Value<'v>>
    override fun iterate(me: Value, heap: Heap): Result<Value> {
        // Lazy implementation: we allocate a tuple and then iterate over it.
        val iter = if (this.produceChar) {
            heap.allocTupleIter(this.string.asStr().map { c -> heap.allocStr(c.toString()) })
        } else {
            heap.allocTupleIter(this.string.asStr().map { c ->
                StarlarkInt.from(c.code).allocValue(heap)
            })
        }
        return Result.success(iter)
    }

    // unsafe impl Trace for StringIterableGen
    override fun trace(tracer: Tracer) {
        // In Rust, Trace is derived. The StringValue's inner Value
        // would be traced. Since Kotlin's GC handles memory, this is a no-op.
    }
}

// pub(crate) fn iterate_chars<'v>(
//     string: StringValue<'v>,
//     heap: Heap<'v>,
// ) -> ValueOfUnchecked<'v, StarlarkIter<String>>
internal fun iterateChars(
    string: StringValue,
    heap: Heap
): Value {
    // Rust returns ValueOfUnchecked<StarlarkIter<String>> but the Kotlin port
    // cannot represent this phantom type annotation because StarlarkIter does not
    // implement StarlarkTypeRepr yet. Callers only use .get() on the result anyway.
    return heap.allocComplex(StringIterableGen(
        string,
        true
    ))
}

// pub(crate) fn iterate_codepoints<'v>(
//     string: StringValue<'v>,
//     heap: Heap<'v>,
// ) -> ValueOfUnchecked<'v, StarlarkIter<String>>
internal fun iterateCodepoints(
    string: StringValue,
    heap: Heap
): Value {
    // Rust returns ValueOfUnchecked<StarlarkIter<String>> but the Kotlin port
    // cannot represent this phantom type annotation because StarlarkIter does not
    // implement StarlarkTypeRepr yet. Callers only use .get() on the result anyway.
    return heap.allocComplex(StringIterableGen(
        string,
        false
    ))
}
