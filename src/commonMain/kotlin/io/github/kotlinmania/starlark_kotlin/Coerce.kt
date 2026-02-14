// port-lint: source src/coerce.rs
package io.github.kotlinmania.starlark_kotlin

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

/** A trait to represent zero-cost conversions. */

/**
 * A marker interface such that the existence of `From: Coerce<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Particularly useful for containers, e.g. `List<From>` can be treated as
 * `List<To>` in _O(1)_. If such an instance is available,
 * you can use [coerce] to perform the conversion.
 *
 * Importantly, you must make sure the runtime does not change the type representation
 * between the different types, and it must be safe for the `From` to be treated as `To`,
 * namely same (or less restrictive) alignment, no additional invariants,
 * value can be dropped as `To`.
 */
interface Coerce<To>

/**
 * A marker interface such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided [hashCode],
 * [equals], and [Comparable] traits must give identical results
 * on the `From` and `To` values.
 *
 * This interface is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
interface CoerceKey<To> : Coerce<To>

// Blanket implementations for references, slices, containers:
//
// unsafe impl<From: Coerce<To>, To> Coerce<&To> for &From
// unsafe impl<From: CoerceKey<To>, To> CoerceKey<&To> for &From
//
// unsafe impl<From: Coerce<To>, To> Coerce<List<To>> for List<From>
// unsafe impl<From: CoerceKey<To>, To> CoerceKey<List<To>> for List<From>
//
// unsafe impl<From: Coerce<To>, To> Coerce<MutableList<To>> for MutableList<From>
// unsafe impl<From: CoerceKey<To>, To> CoerceKey<MutableList<To>> for MutableList<From>
//
// unsafe impl<From: Coerce<To>, To> Coerce<Set<To>> for Set<From>
//
// unsafe impl<FromK: CoerceKey<ToK>, FromV: Coerce<ToV>, ToK, ToV>
//     Coerce<Map<ToK, ToV>> for Map<FromK, FromV>
//
// unsafe impl<From1: Coerce<To1>, To1> Coerce<Pair<To1, Unit>> for Pair<From1, Unit>
// unsafe impl<From1: CoerceKey<To1>, To1> CoerceKey<Pair<To1, Unit>> for Pair<From1, Unit>
//
// unsafe impl<From1: Coerce<To1>, From2: Coerce<To2>, To1, To2>
//     Coerce<Pair<To1, To2>> for Pair<From1, From2>
// unsafe impl<From1: CoerceKey<To1>, From2: CoerceKey<To2>, To1, To2>
//     CoerceKey<Pair<To1, To2>> for Pair<From1, From2>
//
// unsafe impl<From: Coerce<To>, To> Coerce<Array<To>> for Array<From>
// unsafe impl<From: CoerceKey<To>, To> CoerceKey<Array<To>> for Array<From>

// We can't define a blanket Coerce<T> for T because that conflicts with the specific traits above.
// Therefore, we define instances where we think they might be useful, rather than trying to do every concrete type.
// unsafe impl Coerce<String> for String
// unsafe impl CoerceKey<String> for String
// unsafe impl Coerce<Unit> for Unit
// unsafe impl CoerceKey<Unit> for Unit

// unsafe impl<FromK, FromV, ToK, ToV> Coerce<SmallMap<ToK, ToV>> for SmallMap<FromK, FromV>
//     where FromK: CoerceKey<ToK>, FromV: Coerce<ToV>
// unsafe impl<From, To> Coerce<SmallSet<To>> for SmallSet<From> where From: Coerce<To>

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<FromType, ToType>(x)`.
 */
@Suppress("UNCHECKED_CAST")
inline fun <From, To> coerce(x: From): To {
    return x as To
}
