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
 * A trait to represent zero-cost conversions.
 */

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet

// pub use starlark_derive::Coerce;

/**
 * A marker trait such that the existence of `From: Coerce<To>` implies
 * that `From` can be treat as `To` without any data manipulation.
 * Particularly useful for containers, e.g. `Vec<From>` can be treated as
 * `Vec<To>` in _O(1)_. If such an instance is available,
 * you can use [coerce] to perform the conversion.
 *
 * Importantly, you must make sure Rust does not change the type representation
 * between the different types (typically using a `repr` directive),
 * and it must be safe for the `From` to be treated as `To`, namely same (or less restrictive) alignment,
 * no additional invariants, value can be dropped as `To`.
 *
 * If you only need [coerce] on newtype references,
 * then the [ref-cast crate](https://crates.io/crates/ref-cast)
 * provides that, along with automatic derivations (no `unsafe` required).
 */
// pub unsafe trait Coerce<To: ?Sized> {}
interface Coerce<To>

/**
 * A marker trait such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treat as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided [hashCode],
 * [equals], [Comparable] traits must give identical results
 * on the `From` and `To` values.
 *
 * This trait is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
// pub unsafe trait CoerceKey<To: ?Sized>: Coerce<To> {}
interface CoerceKey<To> : Coerce<To>

// unsafe impl<'a, From: ?Sized, To: ?Sized> Coerce<&'a To> for &'a From where From: Coerce<To> {}
// unsafe impl<'a, From: ?Sized, To: ?Sized> CoerceKey<&'a To> for &'a From where From: CoerceKey<To> {}
// In Kotlin, reference coercion is implicit via type erasure.

// unsafe impl<From, To> Coerce<[To]> for [From] where From: Coerce<To> {}
// unsafe impl<From, To> CoerceKey<[To]> for [From] where From: CoerceKey<To> {}
// In Kotlin, List<From> can be treated as List<To> via unchecked cast.

// unsafe impl<From, To> Coerce<Vec<To>> for Vec<From> where From: Coerce<To> {}
// unsafe impl<From, To> CoerceKey<Vec<To>> for Vec<From> where From: CoerceKey<To> {}
// In Kotlin, MutableList<From> can be treated as MutableList<To> via unchecked cast.

// unsafe impl<From: ?Sized, To: ?Sized> CoerceKey<Box<To>> for Box<From> where From: CoerceKey<To> {}
// unsafe impl<From: ?Sized, To: ?Sized> Coerce<Box<To>> for Box<From> where From: Coerce<To> {}
// In Kotlin, Box is just a reference; coercion is an unchecked cast.

// unsafe impl<From, To> Coerce<HashSet<To>> for HashSet<From> where From: CoerceKey<To> {}
// In Kotlin, Set<From> can be treated as Set<To> via unchecked cast.

// unsafe impl<FromK, FromV, ToK, ToV> Coerce<HashMap<ToK, ToV>> for HashMap<FromK, FromV>
// where
//     FromK: CoerceKey<ToK>,
//     FromV: Coerce<ToV>,
// {}
// In Kotlin, Map<FromK, FromV> can be treated as Map<ToK, ToV> via unchecked cast.

// unsafe impl<From1: Coerce<To1>, To1> Coerce<(To1,)> for (From1,) {}
// unsafe impl<From1: CoerceKey<To1>, To1> CoerceKey<(To1,)> for (From1,) {}
// Kotlin has no single-element tuple; this is a direct cast.

// unsafe impl<From1: Coerce<To1>, From2: Coerce<To2>, To1, To2> Coerce<(To1, To2)>
//     for (From1, From2)
// {}
// unsafe impl<From1: CoerceKey<To1>, From2: CoerceKey<To2>, To1, To2> CoerceKey<(To1, To2)>
//     for (From1, From2)
// {}
// In Kotlin, Pair<From1, From2> can be treated as Pair<To1, To2> via unchecked cast.

// unsafe impl<From: Coerce<To>, To, const N: usize> Coerce<[To; N]> for [From; N] {}
// unsafe impl<From: CoerceKey<To>, To, const N: usize> CoerceKey<[To; N]> for [From; N] {}
// In Kotlin, Array<From> can be treated as Array<To> via unchecked cast.

// unsafe impl<From, To> Coerce<PhantomData<To>> for PhantomData<From> {}
// PhantomData has no Kotlin equivalent; it is a zero-sized type marker.

// We can't define a blanket `Coerce<T> for T` because that conflicts with the specific traits above.
// Therefore, we define instances where we think they might be useful, rather than trying to do every concrete type.
// unsafe impl Coerce<String> for String {}
// unsafe impl CoerceKey<String> for String {}

// unsafe impl Coerce<str> for str {}
// unsafe impl CoerceKey<str> for str {}

// unsafe impl Coerce<()> for () {}
// unsafe impl CoerceKey<()> for () {}

// unsafe impl<FromK, FromV, ToK, ToV> Coerce<SmallMap<ToK, ToV>> for SmallMap<FromK, FromV>
// where
//     FromK: CoerceKey<ToK>,
//     FromV: Coerce<ToV>,
// {}

// unsafe impl<From, To> Coerce<SmallSet<To>> for SmallSet<From> where From: Coerce<To> {}

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<FromType, ToType>(x)`.
 */
@Suppress("UNCHECKED_CAST")
inline fun <From, reified To> coerce(x: From): To {
    // In Rust this asserts Layout equality and does a raw pointer read through ManuallyDrop.
    // In Kotlin, type erasure means this is an unchecked cast, which is the zero-cost
    // equivalent of Rust's transmute-style coercion.
    return x as To
}
