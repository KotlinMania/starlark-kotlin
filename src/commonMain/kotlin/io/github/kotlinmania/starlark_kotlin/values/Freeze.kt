// port-lint: source src/values/freeze.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

// use std::marker::PhantomData
// use std::cell::OnceCell
// use std::cell::RefCell
// use std::cell::UnsafeCell

/**
 * Kotlin equivalent of Rust's `PhantomData<T>`.
 *
 * A zero-sized marker type used for type-level tracking without runtime overhead.
 * In Rust, `PhantomData<T>` is used to indicate ownership or variance without
 * storing a value of type `T`. In Kotlin, this is a singleton data class.
 */
data class PhantomData<T> private constructor(val unit: Unit = Unit) {
    companion object {
        /** Create a new [PhantomData] instance. */
        fun <T> new(): PhantomData<T> = PhantomData()
    }
}

/**
 * Kotlin equivalent of Rust's 1-tuple `(T,)`.
 *
 * Rust's `(A,)` syntax represents a single-element tuple. Since Kotlin has no
 * native 1-tuple syntax, we use this data class as the direct translation.
 */
data class Tuple1<T>(val value0: T)

/**
 * Kotlin equivalent of Rust's 4-tuple `(A, B, C, D)`.
 *
 * Kotlin's [Triple] only goes up to 3 elements. This data class represents the
 * 4-element tuple used in `impl<A, B, C, D> Freeze for (A, B, C, D)`.
 */
data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Kotlin equivalent of Rust's 5-tuple `(A, B, C, D, E)`.
 *
 * Kotlin's [Triple] only goes up to 3 elements. This data class represents the
 * 5-element tuple used in `impl<A, B, C, D, E> Freeze for (A, B, C, D, E)`.
 */
data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

/**
 * Need to be implemented for non-simple `StarlarkValue`.
 *
 * This is called on freeze of the heap. Must produce a replacement object to place
 * in the frozen heap.
 *
 * For relatively simple cases it can be implemented with delegation:
 *
 * ```kotlin
 * class MyType<V : Freeze<F>, F>(
 *     val value: V,
 *     val data: AdditionalData,  // This field does not implement Freeze, but we can use it as is
 * ) : Freeze<MyType<F, F>> {
 *     override fun freeze(freezer: Freezer): FreezeResult<MyType<F, F>> {
 *         return Ok(MyType(value.freeze(freezer).get(), data))
 *     }
 * }
 * ```
 */
interface Freeze<Frozen> {
    /**
     * Freeze a value. The frozen value _must_ be equal to the original,
     * and produce the same hash.
     *
     * Note during freeze, [Value] objects in `this` might be already special forward-objects,
     * trying to unpack these objects will crash the process.
     * So the function is only allowed to access [Value] objects after it froze them.
     */
    fun freeze(freezer: Freezer): FreezeResult<Frozen>
}

// impl Freeze for String
fun freezeString(value: String, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<String> {
    return Result.success(value)
}

// impl Freeze for i32
fun freezeInt(value: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Int> {
    return Result.success(value)
}

// impl Freeze for u32
fun freezeUInt(value: UInt, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<UInt> {
    return Result.success(value)
}

// impl Freeze for i64
fun freezeLong(value: Long, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Long> {
    return Result.success(value)
}

// impl Freeze for u64
fun freezeULong(value: ULong, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<ULong> {
    return Result.success(value)
}

// impl Freeze for usize
// (usize maps to Int in Kotlin for most practical purposes)
fun freezeUSize(value: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Int> {
    return Result.success(value)
}

// impl Freeze for bool
fun freezeBoolean(value: Boolean, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Boolean> {
    return Result.success(value)
}

// impl<'v, T: 'static> Freeze for marker::PhantomData<&'v T>
// type Frozen = PhantomData<&'static T>
fun <T> freezePhantomData(
    @Suppress("UNUSED_PARAMETER") value: PhantomData<T>,
    @Suppress("UNUSED_PARAMETER") freezer: Freezer,
): FreezeResult<PhantomData<T>> {
    return Result.success(PhantomData.new())
}

// impl<T> Freeze for Vec<T> where T: Freeze
fun <T, F> freezeList(
    list: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<List<F>> {
    val result = mutableListOf<F>()
    for (element in list) {
        val frozen = freezeElement(element, freezer)
        if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
        result.add(frozen.getOrThrow())
    }
    return Result.success(result)
}

// impl<T> Freeze for RefCell<T> where T: Freeze
// type Frozen = T::Frozen
// RefCell has no direct Kotlin equivalent; we represent it as a mutable holder
// whose inner value is accessed and frozen directly.
fun <T, F> freezeRefCell(
    inner: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F> {
    return freezeInner(inner, freezer)
}

// impl<T> Freeze for UnsafeCell<T> where T: Freeze
// type Frozen = UnsafeCell<T::Frozen>
// UnsafeCell has no direct Kotlin equivalent; the inner value is frozen and
// returned wrapped in the same holder type via the provided wrapper function.
fun <T, F> freezeUnsafeCell(
    inner: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
    wrapResult: (F) -> F,
): FreezeResult<F> {
    val frozen = freezeInner(inner, freezer)
    if (frozen.isFailure) return frozen
    return Result.success(wrapResult(frozen.getOrThrow()))
}

// impl<T> Freeze for OnceCell<T> where T: Freeze
// type Frozen = Option<T::Frozen>
// OnceCell<T> holds either nothing or a value; freezing yields an Option<T::Frozen>.
fun <T, F> freezeOnceCell(
    inner: T?,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F?> {
    return freezeNullable(inner, freezer, freezeInner)
}

// impl<T> Freeze for Box<T> where T: Freeze
// type Frozen = Box<T::Frozen>
// In Kotlin, Box<T> is just a thin wrapper; the inner value is frozen directly.
fun <T, F> freezeBox(
    inner: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F> {
    return freezeInner(inner, freezer)
}

// impl<T> Freeze for Box<[T]> where T: Freeze
// type Frozen = Box<[T::Frozen]>
// Box<[T]> is a boxed slice; in Kotlin this maps to a List<T>.
fun <T, F> freezeBoxSlice(
    slice: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<List<F>> {
    return freezeList(slice, freezer, freezeElement)
}

// impl<T> Freeze for Option<T> where T: Freeze
fun <T, F> freezeNullable(
    value: T?,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F?> {
    if (value == null) return Result.success(null)
    return freezeElement(value, freezer).map { it }
}

// impl<K: Freeze> Freeze for Hashed<K>
fun <K, FK> freezeHashed(
    hashed: Hashed<K>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<FK>,
): FreezeResult<Hashed<FK>> {
    // `freeze` must not change hash.
    val frozenKey = freezeKey(hashed.intoKey(), freezer)
    if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
    return Result.success(Hashed.newUnchecked(hashed.hash(), frozenKey.getOrThrow()))
}

// impl<K, V> Freeze for SmallMap<K, V> where K: Freeze, V: Freeze
fun <K, V, FK, FV> freezeSmallMap(
    map: SmallMap<K, V>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<FK>,
    freezeValue: (V, Freezer) -> FreezeResult<FV>,
): FreezeResult<SmallMap<FK, FV>> {
    val new = SmallMap.withCapacity<FK, FV>(map.len())
    for ((key, value) in map.intoIterHashed()) {
        val hash = key.hash()
        val frozenKey = freezeKey(key.intoKey(), freezer)
        if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
        // rust: TODO(nga): verify hash unchanged after freeze.
        val hashedKey = Hashed.newUnchecked(hash, frozenKey.getOrThrow())
        val frozenValue = freezeValue(value, freezer)
        if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(hashedKey, frozenValue.getOrThrow())
    }
    return Result.success(new)
}

// impl<T> Freeze for SmallSet<T> where T: Freeze
fun <T, F> freezeSmallSet(
    set: SmallSet<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<SmallSet<F>> {
    val new = SmallSet.withCapacity<F>(set.len())
    for (hashedValue in set.intoIterHashed()) {
        val frozenHashed = freezeHashed(hashedValue, freezer, freezeElement)
        if (frozenHashed.isFailure) return Result.failure(frozenHashed.exceptionOrNull()!!)
        // rust: TODO(nga): verify hash unchanged after freeze.
        new.insertHashedUniqueUnchecked(frozenHashed.getOrThrow())
    }
    return Result.success(new)
}

// impl Freeze for Value
fun Value.freeze(freezer: Freezer): FreezeResult<FrozenValue> {
    return freezer.freeze(this)
}

// impl Freeze for FrozenValue
fun FrozenValue.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<FrozenValue> {
    return Result.success(this)
}

// impl Freeze for ()
fun freezeUnit(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Unit> {
    return Result.success(Unit)
}

// impl<A: Freeze> Freeze for (A,)
fun <A, FA> freezeTuple1(
    a: Tuple1<A>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
): FreezeResult<Tuple1<FA>> {
    val fa = freezeA(a.value0, freezer)
    if (fa.isFailure) return Result.failure(fa.exceptionOrNull()!!)
    return Result.success(Tuple1(fa.getOrThrow()))
}

// impl<A: Freeze, B: Freeze> Freeze for (A, B)
fun <A, B, FA, FB> freezePair(
    pair: Pair<A, B>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
): FreezeResult<Pair<FA, FB>> {
    val a = freezeA(pair.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(pair.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    return Result.success(Pair(a.getOrThrow(), b.getOrThrow()))
}

// impl<A: Freeze, B: Freeze, C: Freeze> Freeze for (A, B, C)
fun <A, B, C, FA, FB, FC> freezeTriple(
    triple: Triple<A, B, C>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
    freezeC: (C, Freezer) -> FreezeResult<FC>,
): FreezeResult<Triple<FA, FB, FC>> {
    val a = freezeA(triple.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(triple.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(triple.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    return Result.success(Triple(a.getOrThrow(), b.getOrThrow(), c.getOrThrow()))
}

// impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze> Freeze for (A, B, C, D)
fun <A, B, C, D, FA, FB, FC, FD> freezeTuple4(
    tuple: Tuple4<A, B, C, D>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
    freezeC: (C, Freezer) -> FreezeResult<FC>,
    freezeD: (D, Freezer) -> FreezeResult<FD>,
): FreezeResult<Tuple4<FA, FB, FC, FD>> {
    val a = freezeA(tuple.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(tuple.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(tuple.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    val d = freezeD(tuple.fourth, freezer)
    if (d.isFailure) return Result.failure(d.exceptionOrNull()!!)
    return Result.success(Tuple4(a.getOrThrow(), b.getOrThrow(), c.getOrThrow(), d.getOrThrow()))
}

// impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze, E: Freeze> Freeze for (A, B, C, D, E)
fun <A, B, C, D, E, FA, FB, FC, FD, FE> freezeTuple5(
    tuple: Tuple5<A, B, C, D, E>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
    freezeC: (C, Freezer) -> FreezeResult<FC>,
    freezeD: (D, Freezer) -> FreezeResult<FD>,
    freezeE: (E, Freezer) -> FreezeResult<FE>,
): FreezeResult<Tuple5<FA, FB, FC, FD, FE>> {
    val a = freezeA(tuple.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(tuple.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(tuple.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    val d = freezeD(tuple.fourth, freezer)
    if (d.isFailure) return Result.failure(d.exceptionOrNull()!!)
    val e = freezeE(tuple.fifth, freezer)
    if (e.isFailure) return Result.failure(e.exceptionOrNull()!!)
    return Result.success(Tuple5(a.getOrThrow(), b.getOrThrow(), c.getOrThrow(), d.getOrThrow(), e.getOrThrow()))
}
