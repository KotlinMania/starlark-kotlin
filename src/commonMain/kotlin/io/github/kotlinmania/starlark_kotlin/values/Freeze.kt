// port-lint: source src/values/freeze.rs
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

// use std::cell::OnceCell;
// use std::cell::RefCell;
// use std::cell::UnsafeCell;
// use std::marker;
// use std::marker::PhantomData;

import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import starlark_map.Hashed
import starlark_map.small_map.SmallMap
import starlark_map.small_set.SmallSet

// use crate::values::FreezeResult;
// use crate::values::Freezer;
// use crate::values::FrozenValue;
// use crate::values::Value;

/**
 * A zero-sized marker type used for type-level tracking without runtime overhead.
 *
 * Kotlin equivalent of Rust's `PhantomData<T>`.
 * In Rust, `PhantomData<T>` is used to indicate ownership or variance without
 * storing a value of type `T`.
 */
data class PhantomData<T> private constructor(val unit: Unit = Unit) {
    companion object {
        fun <T> new(): PhantomData<T> = PhantomData()
    }
}

/**
 * Kotlin equivalent of Rust's 1-tuple `(A,)`.
 */
data class Tuple1<T>(val value0: T)

/**
 * Kotlin equivalent of Rust's 4-tuple `(A, B, C, D)`.
 */
data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Kotlin equivalent of Rust's 5-tuple `(A, B, C, D, E)`.
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
 *     // This field does not implement Freeze, but we can use it as is for freeze.
 *     val data: AdditionalData,
 * ) : Freeze<MyType<F, F>> {
 *     override fun freeze(freezer: Freezer): FreezeResult<MyType<F, F>> {
 *         return Result.success(MyType(value.freeze(freezer).getOrThrow(), data))
 *     }
 * }
 * ```
 */
interface Freeze<Frozen> {
    /** When type is frozen, it is frozen into this type. */
    // type Frozen;

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

// impl Freeze for String {
//     type Frozen = String;
/** Freeze implementation for [String]. Identity freeze. */
fun freezeString(self: String, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<String> {
    return Result.success(self)
}

// impl Freeze for i32 {
//     type Frozen = i32;
/** Freeze implementation for [Int] (i32). Identity freeze. */
fun freezeInt(self: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Int> {
    return Result.success(self)
}

// impl Freeze for u32 {
//     type Frozen = u32;
/** Freeze implementation for [UInt] (u32). Identity freeze. */
fun freezeUInt(self: UInt, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<UInt> {
    return Result.success(self)
}

// impl Freeze for i64 {
//     type Frozen = i64;
/** Freeze implementation for [Long] (i64). Identity freeze. */
fun freezeLong(self: Long, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Long> {
    return Result.success(self)
}

// impl Freeze for u64 {
//     type Frozen = u64;
/** Freeze implementation for [ULong] (u64). Identity freeze. */
fun freezeULong(self: ULong, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<ULong> {
    return Result.success(self)
}

// impl Freeze for usize {
//     type Frozen = usize;
/** Freeze implementation for usize (mapped to [Int]). Identity freeze. */
fun freezeUSize(self: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Int> {
    return Result.success(self)
}

// impl Freeze for bool {
//     type Frozen = bool;
/** Freeze implementation for [Boolean]. Identity freeze. */
fun freezeBoolean(self: Boolean, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Boolean> {
    return Result.success(self)
}

// impl<'v, T: 'static> Freeze for marker::PhantomData<&'v T> {
//     type Frozen = PhantomData<&'static T>;
/** Freeze implementation for [PhantomData]. Returns a new phantom. */
fun <T> freezePhantomData(
    @Suppress("UNUSED_PARAMETER") self: PhantomData<T>,
    @Suppress("UNUSED_PARAMETER") freezer: Freezer,
): FreezeResult<PhantomData<T>> {
    return Result.success(PhantomData.new())
}

// impl<T> Freeze for Vec<T>
// where
//     T: Freeze,
// {
//     type Frozen = Vec<T::Frozen>;
/** Freeze implementation for [List] (Vec). Freezes each element. */
fun <T, F> freezeList(
    self: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<List<F>> {
    val result = mutableListOf<F>()
    for (v in self) {
        val frozen = freezeElement(v, freezer)
        if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
        result.add(frozen.getOrThrow())
    }
    return Result.success(result)
}

// impl<T> Freeze for RefCell<T>
// where
//     T: Freeze,
// {
//     type Frozen = T::Frozen;
/**
 * Freeze implementation for RefCell (no Kotlin equivalent -- unwraps and freezes inner value).
 * In Rust, `RefCell<T>` freezes to `T::Frozen` by calling `into_inner().freeze(freezer)`.
 */
fun <T, F> freezeRefCell(
    self: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F> {
    return freezeInner(self, freezer)
}

// impl<T> Freeze for UnsafeCell<T>
// where
//     T: Freeze,
// {
//     type Frozen = UnsafeCell<T::Frozen>;
/**
 * Freeze implementation for UnsafeCell (no Kotlin equivalent -- freezes inner value and wraps).
 * In Rust, `UnsafeCell<T>` freezes to `UnsafeCell<T::Frozen>`.
 */
fun <T, F> freezeUnsafeCell(
    self: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
    wrapResult: (F) -> F,
): FreezeResult<F> {
    val frozen = freezeInner(self, freezer)
    if (frozen.isFailure) return frozen
    return Result.success(wrapResult(frozen.getOrThrow()))
}

// impl<T> Freeze for OnceCell<T>
// where
//     T: Freeze,
// {
//     type Frozen = Option<T::Frozen>;
/**
 * Freeze implementation for OnceCell (maps to nullable in Kotlin).
 * In Rust, `OnceCell<T>` freezes to `Option<T::Frozen>` via `into_inner().freeze(freezer)`.
 */
fun <T, F> freezeOnceCell(
    self: T?,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F?> {
    return freezeNullable(self, freezer, freezeInner)
}

// impl<T> Freeze for Box<T>
// where
//     T: Freeze,
// {
//     type Frozen = Box<T::Frozen>;
/**
 * Freeze implementation for Box (no Kotlin equivalent -- freezes inner value).
 * In Rust, `Box<T>` freezes to `Box<T::Frozen>`.
 */
fun <T, F> freezeBox(
    self: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F> {
    return freezeInner(self, freezer)
}

// impl<T> Freeze for Box<[T]>
// where
//     T: Freeze,
// {
//     type Frozen = Box<[T::Frozen]>;
/**
 * Freeze implementation for boxed slice (maps to [List] in Kotlin).
 * In Rust, `Box<[T]>` freezes to `Box<[T::Frozen]>`.
 */
fun <T, F> freezeBoxSlice(
    self: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<List<F>> {
    return freezeList(self, freezer, freezeElement)
}

// impl<T> Freeze for Option<T>
// where
//     T: Freeze,
// {
//     type Frozen = Option<T::Frozen>;
/**
 * Freeze implementation for nullable (Option).
 * In Rust, `Option<T>` freezes to `Option<T::Frozen>`.
 */
fun <T, F> freezeNullable(
    self: T?,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F?> {
    if (self == null) return Result.success(null)
    return freezeElement(self, freezer).map { it }
}

// impl<K: Freeze> Freeze for Hashed<K> {
//     type Frozen = Hashed<K::Frozen>;
/**
 * Freeze implementation for [Hashed].
 * In Rust, `Hashed<K>` freezes to `Hashed<K::Frozen>`.
 */
fun <K, FK> freezeHashed(
    self: Hashed<K>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<FK>,
): FreezeResult<Hashed<FK>> {
    // `freeze` must not change hash.
    val frozenKey = freezeKey(self.intoKey(), freezer)
    if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
    return Result.success(Hashed.newUnchecked(self.hash(), frozenKey.getOrThrow()))
}

// impl<K, V> Freeze for SmallMap<K, V>
// where
//     K: Freeze,
//     V: Freeze,
// {
//     type Frozen = SmallMap<K::Frozen, V::Frozen>;
/**
 * Freeze implementation for [SmallMap].
 * In Rust, `SmallMap<K, V>` freezes to `SmallMap<K::Frozen, V::Frozen>`.
 */
fun <K, V, FK, FV> freezeSmallMap(
    self: SmallMap<K, V>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<FK>,
    freezeValue: (V, Freezer) -> FreezeResult<FV>,
): FreezeResult<SmallMap<FK, FV>> {
    val new = SmallMap.withCapacity<FK, FV>(self.len())
    for ((key, value) in self.intoIterHashed()) {
        val hash = key.hash()
        val frozenKey = freezeKey(key.intoKey(), freezer)
        if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
        // TODO(nga): verify hash unchanged after freeze.
        val hashedKey = Hashed.newUnchecked(hash, frozenKey.getOrThrow())
        val frozenValue = freezeValue(value, freezer)
        if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(hashedKey, frozenValue.getOrThrow())
    }
    return Result.success(new)
}

// impl<T> Freeze for SmallSet<T>
// where
//     T: Freeze,
// {
//     type Frozen = SmallSet<T::Frozen>;
/**
 * Freeze implementation for [SmallSet].
 * In Rust, `SmallSet<T>` freezes to `SmallSet<T::Frozen>`.
 */
fun <T, F> freezeSmallSet(
    self: SmallSet<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<SmallSet<F>> {
    val new = SmallSet.withCapacity<F>(self.len())
    for (value in self.intoIterHashed()) {
        val frozenValue = freezeHashed(value, freezer, freezeElement)
        if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
        // TODO(nga): verify hash unchanged after freeze.
        new.insertHashedUniqueUnchecked(frozenValue.getOrThrow())
    }
    return Result.success(new)
}

// impl<'v> Freeze for Value<'v> {
//     type Frozen = FrozenValue;
/** Freeze implementation for [Value]. Delegates to [Freezer.freeze]. */
fun Value.freeze(freezer: Freezer): FreezeResult<FrozenValue> {
    return freezer.freeze(this)
}

// impl Freeze for FrozenValue {
//     type Frozen = FrozenValue;
/** Freeze implementation for [FrozenValue]. Identity freeze -- already frozen. */
fun FrozenValue.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<FrozenValue> {
    return Result.success(this)
}

// impl Freeze for () {
//     type Frozen = ();
/** Freeze implementation for [Unit] (Rust `()`). Identity freeze. */
fun freezeUnit(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Unit> {
    return Result.success(Unit)
}

// impl<A: Freeze> Freeze for (A,) {
//     type Frozen = (A::Frozen,);
/** Freeze implementation for [Tuple1] (Rust 1-tuple `(A,)`). */
fun <A, FA> freezeTuple1(
    self: Tuple1<A>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
): FreezeResult<Tuple1<FA>> {
    val fa = freezeA(self.value0, freezer)
    if (fa.isFailure) return Result.failure(fa.exceptionOrNull()!!)
    return Result.success(Tuple1(fa.getOrThrow()))
}

// impl<A: Freeze, B: Freeze> Freeze for (A, B) {
//     type Frozen = (A::Frozen, B::Frozen);
/** Freeze implementation for [Pair] (Rust 2-tuple `(A, B)`). */
fun <A, B, FA, FB> freezePair(
    self: Pair<A, B>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
): FreezeResult<Pair<FA, FB>> {
    val a = freezeA(self.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(self.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    return Result.success(Pair(a.getOrThrow(), b.getOrThrow()))
}

// impl<A: Freeze, B: Freeze, C: Freeze> Freeze for (A, B, C) {
//     type Frozen = (A::Frozen, B::Frozen, C::Frozen);
/** Freeze implementation for [Triple] (Rust 3-tuple `(A, B, C)`). */
fun <A, B, C, FA, FB, FC> freezeTriple(
    self: Triple<A, B, C>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
    freezeC: (C, Freezer) -> FreezeResult<FC>,
): FreezeResult<Triple<FA, FB, FC>> {
    val a = freezeA(self.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(self.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(self.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    return Result.success(Triple(a.getOrThrow(), b.getOrThrow(), c.getOrThrow()))
}

// impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze> Freeze for (A, B, C, D) {
//     type Frozen = (A::Frozen, B::Frozen, C::Frozen, D::Frozen);
/** Freeze implementation for [Tuple4] (Rust 4-tuple `(A, B, C, D)`). */
fun <A, B, C, D, FA, FB, FC, FD> freezeTuple4(
    self: Tuple4<A, B, C, D>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
    freezeC: (C, Freezer) -> FreezeResult<FC>,
    freezeD: (D, Freezer) -> FreezeResult<FD>,
): FreezeResult<Tuple4<FA, FB, FC, FD>> {
    val a = freezeA(self.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(self.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(self.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    val d = freezeD(self.fourth, freezer)
    if (d.isFailure) return Result.failure(d.exceptionOrNull()!!)
    return Result.success(Tuple4(a.getOrThrow(), b.getOrThrow(), c.getOrThrow(), d.getOrThrow()))
}

// impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze, E: Freeze> Freeze for (A, B, C, D, E) {
//     type Frozen = (A::Frozen, B::Frozen, C::Frozen, D::Frozen, E::Frozen);
/** Freeze implementation for [Tuple5] (Rust 5-tuple `(A, B, C, D, E)`). */
fun <A, B, C, D, E, FA, FB, FC, FD, FE> freezeTuple5(
    self: Tuple5<A, B, C, D, E>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
    freezeC: (C, Freezer) -> FreezeResult<FC>,
    freezeD: (D, Freezer) -> FreezeResult<FD>,
    freezeE: (E, Freezer) -> FreezeResult<FE>,
): FreezeResult<Tuple5<FA, FB, FC, FD, FE>> {
    val a = freezeA(self.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(self.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(self.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    val d = freezeD(self.fourth, freezer)
    if (d.isFailure) return Result.failure(d.exceptionOrNull()!!)
    val e = freezeE(self.fifth, freezer)
    if (e.isFailure) return Result.failure(e.exceptionOrNull()!!)
    return Result.success(Tuple5(a.getOrThrow(), b.getOrThrow(), c.getOrThrow(), d.getOrThrow(), e.getOrThrow()))
}
