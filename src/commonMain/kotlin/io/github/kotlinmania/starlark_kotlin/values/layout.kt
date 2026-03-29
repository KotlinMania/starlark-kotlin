// port-lint: source src/values/layout.rs
package io.github.kotlinmania.starlark_kotlin.values

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

// Possible optimisations:
// Encoding none, bool etc in the pointer of frozen value

/**
 * Layout module for Starlark values.
 *
 * Submodules:
 * - [alignedSize][io.github.kotlinmania.starlark_kotlin.values.layout.AlignedSize] - aligned size calculations
 * - [avalue][io.github.kotlinmania.starlark_kotlin.values.layout.Avalue] - abstract value representation
 * - [avalues][io.github.kotlinmania.starlark_kotlin.values.layout.avalues] - concrete value implementations
 * - [complex][io.github.kotlinmania.starlark_kotlin.values.layout.Complex] - complex value wrapper
 * - [constFrozenString][io.github.kotlinmania.starlark_kotlin.values.layout.ConstFrozenString] - constant frozen strings
 * - [constTypeId][io.github.kotlinmania.starlark_kotlin.values.layout.ConstTypeId] - constant type IDs
 * - [freezer][io.github.kotlinmania.starlark_kotlin.values.layout.Freezer] - value freezing
 * - [heap][io.github.kotlinmania.starlark_kotlin.values.layout.heap] - heap management
 * - [identity][io.github.kotlinmania.starlark_kotlin.values.layout.Identity] - value identity
 * - [pointer][io.github.kotlinmania.starlark_kotlin.values.layout.Pointer] - value pointers
 * - [staticString][io.github.kotlinmania.starlark_kotlin.values.layout.StaticString] - static string allocation
 * - [typed][io.github.kotlinmania.starlark_kotlin.values.layout.ValueTyped] - typed value wrappers
 * - [value][io.github.kotlinmania.starlark_kotlin.values.layout.Value] - core value type
 * - [valueAllocSize][io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize] - allocation size tracking
 * - [valueCaptured][io.github.kotlinmania.starlark_kotlin.values.layout.ValueCaptured] - captured value tracking
 * - [valueLifetimeless][io.github.kotlinmania.starlark_kotlin.values.layout.ValueLifetimeless] - lifetimeless value abstraction
 * - [valueNotSpecial][io.github.kotlinmania.starlark_kotlin.values.layout.ValueNotSpecial] - non-special value marker
 * - [vtable][io.github.kotlinmania.starlark_kotlin.values.layout.Vtable] - virtual dispatch table
 */
