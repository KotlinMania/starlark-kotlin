// port-lint: source src/coerce.rs
package io.github.kotlinmania.starlark_kotlin

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
 * A marker interface such that the existence of `From: Coerce<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Particularly useful for containers, e.g. `List<From>` can be treated as
 * `List<To>` in _O(1)_. If such an instance is available,
 * you can use [coerce] to perform the conversion.
 */
interface Coerce<To>

/**
 * A marker interface such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided [equals], [hashCode],
 * and comparison behavior must match between `From` and `To`.
 *
 * This interface is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
interface CoerceKey<To> : Coerce<To>

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<FromType, ToType>(x)`.
 */
@Suppress("UNCHECKED_CAST")
inline fun <From, reified To> coerce(x: From): To = x as To
