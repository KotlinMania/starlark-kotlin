// port-lint: source src/coerce.rs
package io.github.kotlinmania.starlark

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
 * A marker interface to represent zero-cost conversions.
 *
 * This is a transliteration of Rust's `unsafe trait Coerce<To: ?Sized> {}`.
 *
 * A marker interface such that the existence of `From: Coerce<To>` implies that `From` can be treated
 * as `To` without any data manipulation. Particularly useful for containers, e.g. `Vec<From>` can be
 * treated as `Vec<To>` in _O(1)_. If such an instance is available, you can use [coerce] to perform
 * the conversion.
 *
 * Rust safety notes (from the original):
 * - Rust must not change the type representation between the different types (typically using a `repr` directive).
 * - It must be safe for the `From` to be treated as `To`: same (or less restrictive) alignment, no additional
 *   invariants, and the value can be dropped as `To`.
 *
 * Kotlin does not expose Rust-style representation controls, nor does it offer a general-purpose `transmute`.
 * The [coerce] function is therefore implemented as an unchecked cast, and this interface primarily exists
 * to document intent and mirror the Rust structure.
 *
 * Rust defined a number of blanket `unsafe impl` conversions (references, slices, `Vec`, `Box`, `HashMap`,
 * `HashSet`, tuples, arrays, `PhantomData`, `String`, `str`, `()`, `SmallMap`, `SmallSet`). Kotlin relies on
 * erased generics and explicit casts at call sites rather than expressing those blanket impls directly.
 */
interface Coerce<To>

/**
 * A marker interface such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided [hashCode],
 * [equals], [Comparable] implementations must give identical results
 * on the `From` and `To` values.
 *
 * This interface is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
interface CoerceKey<To> : Coerce<To>

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<FromType, ToType>(x)`.
 *
 * This is a transliteration of Rust's:
 *
 * `assert_eq!(Layout::new::<From>(), Layout::new::<To>());`
 * `let x = ManuallyDrop::new(x);`
 * `unsafe { ptr::read(x.deref() as *const From as *const To) }`
 *
 * Kotlin cannot check or enforce layout equality for generic types. This function is therefore an unchecked cast,
 * which is the closest available analogue to Rust's "zero-cost" coercion in this port.
 */
fun <From, To> coerce(x: From): To {
    @Suppress("UNCHECKED_CAST")
    return x as To
}
