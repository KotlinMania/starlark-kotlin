// port-lint: source coerce.rs
package io.github.kotlinmania.starlark

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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

/**
 * A marker trait such that the existence of `From: Coerce<To>` implies
 * that `From` can be treat as `To` without any data manipulation.
 * Particularly useful for containers, e.g. `List<From>` can be treated as
 * `List<To>` in _O(1)_. If such an instance is available,
 * you can use [coerce] to perform the conversion.
 *
 * Importantly, you must make sure the type representation does not change
 * between the different types, and it must be safe for the `From` to be
 * treated as `To`, namely same (or less restrictive) alignment,
 * no additional invariants, value can be dropped as `To`.
 */
interface Coerce<To>

/**
 * A marker trait such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treat as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided [hashCode],
 * [equals], or [Comparable] implementations must give identical results
 * on the `From` and `To` values.
 *
 * This trait is mostly expected to be a requirement for the keys of
 * associative-map containers, hence the `Key` in the name.
 */
interface CoerceKey<To> : Coerce<To>

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<_, ToType>(x)`.
 */
fun <From, To> coerce(x: From): To = x as To
