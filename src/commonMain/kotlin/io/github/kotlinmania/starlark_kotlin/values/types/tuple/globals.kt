// port-lint: source src/values/types/tuple/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.function.SpecialBuiltinFunction
import io.github.kotlinmania.starlark_kotlin.values.tuple.AllocTuple
import io.github.kotlinmania.starlark_kotlin.values.tuple.TupleRef
import io.github.kotlinmania.starlark_kotlin.values.tuple.value.FrozenTuple
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter

/// [tuple](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#tuple
/// ): returns a tuple containing the elements of the iterable x.
///
/// With no arguments, `tuple()` returns the empty tuple.
///
/// ```
/// tuple() == ()
/// tuple([1,2,3]) == (1, 2, 3)
/// ```
fun tuple(
    a: ValueOfUnchecked<StarlarkIter<Value>>? = null,
    heap: Heap,
): Result<ValueOfUnchecked<TupleRef>> {
    if (a != null) {
        if (TupleRef.fromValue(a.get()) != null) {
            return Result.success(ValueOfUnchecked.new(a.get()))
        }

        val it = a.get().iterate(heap)
        return Result.success(ValueOfUnchecked.new(heap.allocTupleIter(it)))
    } else {
        return Result.success(ValueOfUnchecked.new(heap.alloc(AllocTuple.EMPTY)))
    }
}

fun registerTuple(globals: GlobalsBuilder) {
    globals.set("tuple", ::tuple)
}
