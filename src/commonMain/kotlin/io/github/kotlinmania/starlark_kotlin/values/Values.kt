// port-lint: source src/values.rs
package io.github.kotlinmania.starlark_kotlin.values

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

typealias Heap<V_> = io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap<V_>
typealias FrozenHeap = io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
typealias FrozenHeapRef = io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeapRef
typealias Tracer<V_> = io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer<V_>

typealias Freezer = io.github.kotlinmania.starlark_kotlin.values.layout.Freezer

typealias Value<V_> = io.github.kotlinmania.starlark_kotlin.values.layout.Value<V_>
typealias FrozenValue = io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
