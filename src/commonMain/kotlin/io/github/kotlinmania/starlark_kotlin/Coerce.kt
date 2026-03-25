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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet

/**
 * A trait to represent zero-cost conversions.
 *
 * Kotlin commonMain cannot derive or verify raw memory layouts the way Rust can. This file keeps the
 * Rust proof surface and uses unchecked casts for the actual reinterpretation sites.
 */

// `pub use starlark_derive::Coerce;`
// Kotlin has no direct equivalent of the Rust derive macro in commonMain.

/**
 * A marker interface such that the existence of `From: Coerce<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Particularly useful for containers, e.g. `Vec<From>` can be treated as
 * `Vec<To>` in _O(1)_. If such an instance is available,
 * you can use [coerce] to perform the conversion.
 *
 * Importantly, you must make sure the runtime does not change the type representation
 * between the different types, and it must be safe for the `From` to be treated as `To`,
 * namely same (or less restrictive) alignment, no additional invariants,
 * and a value can be dropped as `To`.
 */
interface Coerce<To, From> {
    fun coerce(value: From): To
}

/**
 * A marker interface such that the existence of `From: CoerceKey<To>` implies
 * that `From` can be treated as `To` without any data manipulation.
 * Furthermore, above and beyond [Coerce], any provided `Hash`, `Eq`, `PartialEq`,
 * `Ord`, and `PartialOrd` behaviour must give identical results on the `From`
 * and `To` values.
 *
 * This interface is mostly expected to be a requirement for the keys of associative-map
 * containers, hence the `Key` in the name.
 */
interface CoerceKey<To, From> : Coerce<To, From>

/** Kotlin stand-in for Rust references used in the blanket impls below. */
@JvmInline
value class Borrowed<T>(
    val value: T,
)

/** Kotlin stand-in for `ManuallyDrop<T>`. */
class ManuallyDrop<T> private constructor(
    private val value: T,
) {
    fun deref(): T = value

    companion object {
        fun <T> new(value: T): ManuallyDrop<T> = ManuallyDrop(value)
    }
}

/** Kotlin stand-in for `Layout::new::<T>()`. */
object CoerceLayout {
    inline fun <reified T> new(): CoerceLayout = this
}

@Suppress("UNCHECKED_CAST")
private fun <To, From> cast(value: From): To = value as To

class BorrowedCoerce<To, From>(
    private val inner: Coerce<To, From>,
) : Coerce<Borrowed<To>, Borrowed<From>> {
    override fun coerce(value: Borrowed<From>): Borrowed<To> = cast(value)
}

class BorrowedCoerceKey<To, From>(
    private val inner: CoerceKey<To, From>,
) : CoerceKey<Borrowed<To>, Borrowed<From>> {
    override fun coerce(value: Borrowed<From>): Borrowed<To> = cast(value)
}

class SliceCoerce<To, From>(
    private val inner: Coerce<To, From>,
) : Coerce<List<To>, List<From>> {
    override fun coerce(value: List<From>): List<To> = cast(value)
}

class SliceCoerceKey<To, From>(
    private val inner: CoerceKey<To, From>,
) : CoerceKey<List<To>, List<From>> {
    override fun coerce(value: List<From>): List<To> = cast(value)
}

class VecCoerce<To, From>(
    private val inner: Coerce<To, From>,
) : Coerce<MutableList<To>, MutableList<From>> {
    override fun coerce(value: MutableList<From>): MutableList<To> = cast(value)
}

class VecCoerceKey<To, From>(
    private val inner: CoerceKey<To, From>,
) : CoerceKey<MutableList<To>, MutableList<From>> {
    override fun coerce(value: MutableList<From>): MutableList<To> = cast(value)
}

class BoxCoerce<To, From>(
    private val inner: Coerce<To, From>,
) : Coerce<Box<To>, Box<From>> {
    override fun coerce(value: Box<From>): Box<To> = cast(value)
}

class BoxCoerceKey<To, From>(
    private val inner: CoerceKey<To, From>,
) : CoerceKey<Box<To>, Box<From>> {
    override fun coerce(value: Box<From>): Box<To> = cast(value)
}

class HashSetCoerce<To, From>(
    private val inner: CoerceKey<To, From>,
) : Coerce<Set<To>, Set<From>> {
    override fun coerce(value: Set<From>): Set<To> = cast(value)
}

class HashMapCoerce<ToK, ToV, FromK, FromV>(
    private val keyCoerce: CoerceKey<ToK, FromK>,
    private val valueCoerce: Coerce<ToV, FromV>,
) : Coerce<Map<ToK, ToV>, Map<FromK, FromV>> {
    override fun coerce(value: Map<FromK, FromV>): Map<ToK, ToV> = cast(value)
}

class Tuple1Coerce<To1, From1>(
    private val firstCoerce: Coerce<To1, From1>,
) : Coerce<Tuple1<To1>, Tuple1<From1>> {
    override fun coerce(value: Tuple1<From1>): Tuple1<To1> = cast(value)
}

class Tuple1CoerceKey<To1, From1>(
    private val firstCoerce: CoerceKey<To1, From1>,
) : CoerceKey<Tuple1<To1>, Tuple1<From1>> {
    override fun coerce(value: Tuple1<From1>): Tuple1<To1> = cast(value)
}

class PairCoerce<To1, To2, From1, From2>(
    private val firstCoerce: Coerce<To1, From1>,
    private val secondCoerce: Coerce<To2, From2>,
) : Coerce<Pair<To1, To2>, Pair<From1, From2>> {
    override fun coerce(value: Pair<From1, From2>): Pair<To1, To2> = cast(value)
}

class PairCoerceKey<To1, To2, From1, From2>(
    private val firstCoerce: CoerceKey<To1, From1>,
    private val secondCoerce: CoerceKey<To2, From2>,
) : CoerceKey<Pair<To1, To2>, Pair<From1, From2>> {
    override fun coerce(value: Pair<From1, From2>): Pair<To1, To2> = cast(value)
}

class ArrayCoerce<To, From>(
    private val inner: Coerce<To, From>,
) : Coerce<Array<To>, Array<From>> {
    override fun coerce(value: Array<From>): Array<To> = cast(value)
}

class ArrayCoerceKey<To, From>(
    private val inner: CoerceKey<To, From>,
) : CoerceKey<Array<To>, Array<From>> {
    override fun coerce(value: Array<From>): Array<To> = cast(value)
}

class PhantomDataCoerce<To, From> : Coerce<PhantomData<To>, PhantomData<From>> {
    override fun coerce(value: PhantomData<From>): PhantomData<To> = cast(value)
}

object StringCoerce : Coerce<String, String>, CoerceKey<String, String> {
    override fun coerce(value: String): String = value
}

object StrCoerce : Coerce<CharSequence, CharSequence>, CoerceKey<CharSequence, CharSequence> {
    override fun coerce(value: CharSequence): CharSequence = value
}

object UnitCoerce : Coerce<Unit, Unit>, CoerceKey<Unit, Unit> {
    override fun coerce(value: Unit): Unit = value
}

class SmallMapCoerce<ToK, ToV, FromK, FromV>(
    private val keyCoerce: CoerceKey<ToK, FromK>,
    private val valueCoerce: Coerce<ToV, FromV>,
) : Coerce<SmallMap<ToK, ToV>, SmallMap<FromK, FromV>> {
    override fun coerce(value: SmallMap<FromK, FromV>): SmallMap<ToK, ToV> = cast(value)
}

class SmallSetCoerce<To, From>(
    private val inner: Coerce<To, From>,
) : Coerce<SmallSet<To>, SmallSet<From>> {
    override fun coerce(value: SmallSet<From>): SmallSet<To> = cast(value)
}

/**
 * Safely convert between types which have a [Coerce] relationship.
 * Often the second type argument will need to be given explicitly,
 * e.g. `coerce<FromType, ToType>(x)`.
 */
inline fun <reified From, reified To> coerce(x: From): To {
    check(CoerceLayout.new<From>() == CoerceLayout.new<To>())
    val dropped = ManuallyDrop.new(x)
    return cast(dropped.deref())
}
