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

/**
 * A trait to represent zero-cost conversions.
 */

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet

// pub use starlark_derive::Coerce;

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
 *
 * If you only need [coerce] on newtype references, then ref-cast provides that.
 */
interface Coerce<To>

/**
 * A marker interface such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided [hashCode],
 * [equals], and [Comparable] implementations must give identical results
 * on the `From` and `To` values.
 *
 * This interface is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
interface CoerceKey<To> : Coerce<To>

// --- Blanket coercion implementations ---
// In Rust these are unsafe impl blocks that serve as compile-time guarantees.
// In Kotlin we implement the same conversions as typed functions.

/** unsafe impl<'a, From, To> Coerce<&'a To> for &'a From where From: Coerce<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceRef(x: From): To = x as To

/** unsafe impl<'a, From, To> CoerceKey<&'a To> for &'a From where From: CoerceKey<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceKeyRef(x: From): To = x as To

/** unsafe impl<From, To> Coerce<[To]> for [From] where From: Coerce<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceSlice(x: List<From>): List<To> = x as List<To>

/** unsafe impl<From, To> CoerceKey<[To]> for [From] where From: CoerceKey<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceKeySlice(x: List<From>): List<To> = x as List<To>

/** unsafe impl<From, To> Coerce<Vec<To>> for Vec<From> where From: Coerce<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceVec(x: MutableList<From>): MutableList<To> = x as MutableList<To>

/** unsafe impl<From, To> CoerceKey<Vec<To>> for Vec<From> where From: CoerceKey<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceKeyVec(x: MutableList<From>): MutableList<To> = x as MutableList<To>

/** unsafe impl<From, To> Coerce<Box<To>> for Box<From> where From: Coerce<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceBox(x: From): To = x as To

/** unsafe impl<From, To> CoerceKey<Box<To>> for Box<From> where From: CoerceKey<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceKeyBox(x: From): To = x as To

/** unsafe impl<From, To> Coerce<HashSet<To>> for HashSet<From> where From: CoerceKey<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceHashSet(x: Set<From>): Set<To> = x as Set<To>

/** unsafe impl<FromK, FromV, ToK, ToV> Coerce<HashMap<ToK, ToV>> for HashMap<FromK, FromV>
 *  where FromK: CoerceKey<ToK>, FromV: Coerce<ToV> */
@Suppress("UNCHECKED_CAST")
fun <FromK, FromV, ToK, ToV> coerceHashMap(x: Map<FromK, FromV>): Map<ToK, ToV> = x as Map<ToK, ToV>

/** unsafe impl<From1: Coerce<To1>, To1> Coerce<(To1,)> for (From1,) */
@Suppress("UNCHECKED_CAST")
fun <From1, To1> coerceSingle(x: From1): To1 = x as To1

/** unsafe impl<From1: CoerceKey<To1>, To1> CoerceKey<(To1,)> for (From1,) */
@Suppress("UNCHECKED_CAST")
fun <From1, To1> coerceKeySingle(x: From1): To1 = x as To1

/** unsafe impl<From1: Coerce<To1>, From2: Coerce<To2>, To1, To2> Coerce<(To1, To2)> for (From1, From2) */
@Suppress("UNCHECKED_CAST")
fun <From1, From2, To1, To2> coercePair(x: Pair<From1, From2>): Pair<To1, To2> = x as Pair<To1, To2>

/** unsafe impl<From1: CoerceKey<To1>, From2: CoerceKey<To2>, To1, To2> CoerceKey<(To1, To2)> for (From1, From2) */
@Suppress("UNCHECKED_CAST")
fun <From1, From2, To1, To2> coerceKeyPair(x: Pair<From1, From2>): Pair<To1, To2> = x as Pair<To1, To2>

/** unsafe impl<From: Coerce<To>, To, const N: usize> Coerce<[To; N]> for [From; N] */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceArray(x: Array<From>): Array<To> = x as Array<To>

/** unsafe impl<From: CoerceKey<To>, To, const N: usize> CoerceKey<[To; N]> for [From; N] */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceKeyArray(x: Array<From>): Array<To> = x as Array<To>

/** unsafe impl<From, To> Coerce<PhantomData<To>> for PhantomData<From> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coercePhantomData(x: From): To = x as To

// We can't define a blanket Coerce<T> for T because that conflicts with the specific traits above.
// Therefore, we define instances where we think they might be useful, rather than trying to do every concrete type.

/** unsafe impl Coerce<String> for String */
fun coerceString(x: String): String = x

/** unsafe impl CoerceKey<String> for String */
fun coerceKeyString(x: String): String = x

/** unsafe impl Coerce<str> for str */
fun coerceStr(x: String): String = x

/** unsafe impl CoerceKey<str> for str */
fun coerceKeyStr(x: String): String = x

/** unsafe impl Coerce<()> for () */
fun coerceUnit(x: Unit): Unit = x

/** unsafe impl CoerceKey<()> for () */
fun coerceKeyUnit(x: Unit): Unit = x

/** unsafe impl<FromK, FromV, ToK, ToV> Coerce<SmallMap<ToK, ToV>> for SmallMap<FromK, FromV>
 *  where FromK: CoerceKey<ToK>, FromV: Coerce<ToV> */
@Suppress("UNCHECKED_CAST")
fun <FromK, FromV, ToK, ToV> coerceSmallMap(x: SmallMap<FromK, FromV>): SmallMap<ToK, ToV> = x as SmallMap<ToK, ToV>

/** unsafe impl<From, To> Coerce<SmallSet<To>> for SmallSet<From> where From: Coerce<To> */
@Suppress("UNCHECKED_CAST")
fun <From, To> coerceSmallSet(x: SmallSet<From>): SmallSet<To> = x as SmallSet<To>

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<FromType, ToType>(x)`.
 */
@Suppress("UNCHECKED_CAST")
inline fun <From, reified To> coerce(x: From): To {
    // In Rust: assert_eq!(Layout::new::<From>(), Layout::new::<To>());
    // Layout assertions are not available in Kotlin Multiplatform.
    // let x = ManuallyDrop::new(x);
    // unsafe { ptr::read(x.deref() as *const From as *const To) }
    return x as To
}
