// port-lint: source src/values/types/bool/alloc.rs
package io.github.kotlinmania.starlark_kotlin.values.types.bool

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

import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Implementation of [AllocValue] for [Boolean].
 *
 * In Kotlin, we use an extension function to provide the equivalent of Rust's trait implementation.
 */
fun <V> Boolean.allocValue(heap: Heap<V>): Value<V> {
    return Value.newBool(this)
}

/**
 * Implementation of [AllocFrozenValue] for [Boolean].
 *
 * In Kotlin, we use an extension function to provide the equivalent of Rust's trait implementation.
 */
fun Boolean.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return FrozenValue.newBool(this)
}
