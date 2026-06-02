// port-lint: source src/coerce.rs
package io.github.kotlinmania.starlark

import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.smallset.SmallSet
import io.github.kotlinmania.starlark.util.boxed.Box
import io.github.kotlinmania.starlark.values.PhantomData
import io.github.kotlinmania.starlark.values.Tuple1

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

/** A trait to represent zero-cost conversions. */

/**
 * A marker interface such that the existence of `From: Coerce<To>` implies
 * that `From` can be treat as `To` without any data manipulation.
 *
 * Particularly useful for containers, e.g. `Vec<From>` can be treated as `Vec<To>` in _O(1)_.
 * If such an instance is available, you can use [coerce] to perform the conversion.
 *
 * Importantly, you must make sure Rust does not change the type representation between the
 * different types (typically using a `repr` directive), and it must be safe for the `From` to be
 * treated as `To` (same or less restrictive alignment, no additional invariants, value can be
 * dropped as `To`).
 */
// pub unsafe trait Coerce<To: ?Sized> {}
interface Coerce<To : Any?>

/**
 * A marker interface such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 *
 * Furthermore, above and beyond [Coerce], any provided `Hash`, `Eq`, `PartialEq`, `Ord` and
 * `PartialOrd` traits must give identical results on the `From` and `To` values.
 *
 * This interface is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
// pub unsafe trait CoerceKey<To: ?Sized>: Coerce<To> {}
interface CoerceKey<To : Any?> : Coerce<To>

/**
 * Safely convert between types which have a `Coerce` relationship.
 *
 * In Rust this checks `Layout` equality and then reinterprets the bytes. In Kotlin Multiplatform
 * we do not have a stable, cross-platform notion of data layout for generics, so this is an
 * unchecked cast.
 */
// pub fn coerce<From, To>(x: From) -> To where From: Coerce<To> { ... }
fun <From, To> coerce(x: From): To = x as To

// ---- Marker instances (transliteration of Rust `unsafe impl`s) ----
//
// Kotlin does not have Rust-style trait impls. We model each impl as a distinct (unused) class
// with type constraints, which preserves the shape of the Rust file and keeps the set of intended
// coercions explicit without introducing extra functions (which would harm function-name parity).

// Rust `Vec<T>` and `[T]` (slice) equivalents used in this file.
private typealias Vec<T> = MutableList<T>
private typealias Slice<T> = List<T>

// unsafe impl<'a, From: ?Sized, To: ?Sized> Coerce<&'a To> for &'a From where From: Coerce<To> {}
private class CoerceRefImpl<From, To> : Coerce<To> where From : Coerce<To>

// unsafe impl<'a, From: ?Sized, To: ?Sized> CoerceKey<&'a To> for &'a From where From: CoerceKey<To> {}
private class CoerceKeyRefImpl<From, To> : CoerceKey<To> where From : CoerceKey<To>

// unsafe impl<From, To> Coerce<[To]> for [From] where From: Coerce<To> {}
private class CoerceSliceImpl<From, To> : Coerce<Slice<To>> where From : Coerce<To>

// unsafe impl<From, To> CoerceKey<[To]> for [From] where From: CoerceKey<To> {}
private class CoerceKeySliceImpl<From, To> : CoerceKey<Slice<To>> where From : CoerceKey<To>

// unsafe impl<From, To> Coerce<Vec<To>> for Vec<From> where From: Coerce<To> {}
private class CoerceVecImpl<From, To> : Coerce<Vec<To>> where From : Coerce<To>

// unsafe impl<From, To> CoerceKey<Vec<To>> for Vec<From> where From: CoerceKey<To> {}
private class CoerceKeyVecImpl<From, To> : CoerceKey<Vec<To>> where From : CoerceKey<To>

// unsafe impl<From: ?Sized, To: ?Sized> CoerceKey<Box<To>> for Box<From> where From: CoerceKey<To> {}
private class CoerceKeyBoxImpl<From, To> : CoerceKey<Box<To>> where From : CoerceKey<To>

// unsafe impl<From: ?Sized, To: ?Sized> Coerce<Box<To>> for Box<From> where From: Coerce<To> {}
private class CoerceBoxImpl<From, To> : Coerce<Box<To>> where From : Coerce<To>

// unsafe impl<From, To> Coerce<HashSet<To>> for HashSet<From> where From: CoerceKey<To> {}
private class CoerceHashSetImpl<From, To> : Coerce<HashSet<To>> where From : CoerceKey<To>

// unsafe impl<FromK, FromV, ToK, ToV> Coerce<HashMap<ToK, ToV>> for HashMap<FromK, FromV> where ... {}
private class CoerceHashMapImpl<FromK, FromV, ToK, ToV> : Coerce<HashMap<ToK, ToV>>
    where FromK : CoerceKey<ToK>, FromV : Coerce<ToV>

// unsafe impl<From1: Coerce<To1>, To1> Coerce<(To1,)> for (From1,) {}
private class CoerceTuple1Impl<From1, To1> : Coerce<Tuple1<To1>> where From1 : Coerce<To1>

// unsafe impl<From1: CoerceKey<To1>, To1> CoerceKey<(To1,)> for (From1,) {}
private class CoerceKeyTuple1Impl<From1, To1> : CoerceKey<Tuple1<To1>> where From1 : CoerceKey<To1>

// unsafe impl<From1: Coerce<To1>, From2: Coerce<To2>, To1, To2> Coerce<(To1, To2)> for (From1, From2) {}
private class CoerceTuple2Impl<From1, From2, To1, To2> : Coerce<Pair<To1, To2>>
    where From1 : Coerce<To1>, From2 : Coerce<To2>

// unsafe impl<From1: CoerceKey<To1>, From2: CoerceKey<To2>, To1, To2> CoerceKey<(To1, To2)> for (From1, From2) {}
private class CoerceKeyTuple2Impl<From1, From2, To1, To2> : CoerceKey<Pair<To1, To2>>
    where From1 : CoerceKey<To1>, From2 : CoerceKey<To2>

// unsafe impl<From: Coerce<To>, To, const N: usize> Coerce<[To; N]> for [From; N] {}
private class CoerceArrayImpl<From, To> : Coerce<Array<To>> where From : Coerce<To>

// unsafe impl<From: CoerceKey<To>, To, const N: usize> CoerceKey<[To; N]> for [From; N] {}
private class CoerceKeyArrayImpl<From, To> : CoerceKey<Array<To>> where From : CoerceKey<To>

// unsafe impl<From, To> Coerce<PhantomData<To>> for PhantomData<From> {}
private class CoercePhantomDataImpl<From, To> : Coerce<PhantomData<To>>

// unsafe impl Coerce<String> for String {}
private class CoerceStringImpl : Coerce<String>

// unsafe impl CoerceKey<String> for String {}
private class CoerceKeyStringImpl : CoerceKey<String>

// unsafe impl Coerce<str> for str {}
private class CoerceStrImpl : Coerce<String>

// unsafe impl CoerceKey<str> for str {}
private class CoerceKeyStrImpl : CoerceKey<String>

// unsafe impl Coerce<()> for () {}
private class CoerceUnitImpl : Coerce<Unit>

// unsafe impl CoerceKey<()> for () {}
private class CoerceKeyUnitImpl : CoerceKey<Unit>

// unsafe impl<FromK, FromV, ToK, ToV> Coerce<SmallMap<ToK, ToV>> for SmallMap<FromK, FromV> where ... {}
private class CoerceSmallMapImpl<FromK, FromV, ToK, ToV> : Coerce<SmallMap<ToK, ToV>>
    where FromK : CoerceKey<ToK>, FromV : Coerce<ToV>

// unsafe impl<From, To> Coerce<SmallSet<To>> for SmallSet<From> where From: Coerce<To> {}
private class CoerceSmallSetImpl<From, To> : Coerce<SmallSet<To>> where From : Coerce<To>
