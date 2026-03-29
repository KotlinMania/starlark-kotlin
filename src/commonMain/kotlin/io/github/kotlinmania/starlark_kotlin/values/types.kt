// port-lint: source src/values/types.rs
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

/**
 * Built-in Starlark value types.
 *
 * Submodules:
 * - [any][io.github.kotlinmania.starlark_kotlin.values.types.any] - any type
 * - [anyArray][io.github.kotlinmania.starlark_kotlin.values.types.any] - any array type
 * - [anyComplex][io.github.kotlinmania.starlark_kotlin.values.types.any] - any complex type
 * - [array][io.github.kotlinmania.starlark_kotlin.values.types.array] - array type
 * - [bigint][io.github.kotlinmania.starlark_kotlin.values.types.bigint] - big integer type
 * - [bool][io.github.kotlinmania.starlark_kotlin.values.types.bool] - boolean type
 * - [dict][io.github.kotlinmania.starlark_kotlin.values.types.dict] - dictionary type
 * - [ellipsis][io.github.kotlinmania.starlark_kotlin.values.types.ellipsis] - ellipsis type
 * - [enumeration][io.github.kotlinmania.starlark_kotlin.values.types.enumeration] - enumeration type
 * - [float][io.github.kotlinmania.starlark_kotlin.values.types.float] - float type
 * - [function][io.github.kotlinmania.starlark_kotlin.values.types.function] - function types
 * - [int][io.github.kotlinmania.starlark_kotlin.values.types.int] - integer type
 * - [knownMethods] - known methods registry
 * - [list][io.github.kotlinmania.starlark_kotlin.values.types.list] - list type
 * - [listOrTuple][io.github.kotlinmania.starlark_kotlin.values.types.list] - list or tuple union
 * - [namespace][io.github.kotlinmania.starlark_kotlin.values.types.namespace] - namespace type
 * - [none][io.github.kotlinmania.starlark_kotlin.values.types.none] - none type
 * - [num][io.github.kotlinmania.starlark_kotlin.values.types.num] - numeric helpers
 * - [range][io.github.kotlinmania.starlark_kotlin.values.types.range] - range type
 * - [record][io.github.kotlinmania.starlark_kotlin.values.types.record] - record type
 * - [set][io.github.kotlinmania.starlark_kotlin.values.types.set] - set type
 * - [starlarkValueAsType] - starlark value as type
 * - [string][io.github.kotlinmania.starlark_kotlin.values.types.string] - string type
 * - [structs][io.github.kotlinmania.starlark_kotlin.values.types.structs] - struct type
 * - [tuple][io.github.kotlinmania.starlark_kotlin.values.types.tuple] - tuple type
 * - [typeInstanceId] - type instance identifiers
 * - [unbound] - unbound values
 */
