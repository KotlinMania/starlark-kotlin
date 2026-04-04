// port-lint: source values/freeze.rs
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
import io.github.kotlinmania.starlark_kotlin.collections.small_set.SmallSet
import io.github.kotlinmania.starlark_kotlin.syntax.slice_vec_ext.intoTryMap
import io.github.kotlinmania.starlark_kotlin.util.boxed.Box
import io.github.kotlinmania.starlark_kotlin.util.cell.OnceCell
import io.github.kotlinmania.starlark_kotlin.util.cell.UnsafeCell
import io.github.kotlinmania.starlark_kotlin.util.refcell.RefCell
import io.github.kotlinmania.starlark_kotlin.util.scalar.Usize
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

typealias FreezeResult<T> = Result<T>

// Convenience wrappers used by many transliterations.
// These correspond to Rust's blanket `Freeze` impls (Vec/Option/SmallMap/SmallSet) but are called
// as free functions from some ports to preserve Rust call sites.

fun <T, TFrozen> freezeList(
    list: List<T>,
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<List<TFrozen>> {
    return list.freeze(freezer, freeze)
}

fun <T, TFrozen> freezeNullable(
    value: T?,
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen?> {
    return value.freeze(freezer, freeze)
}

fun <K, V, KFrozen, VFrozen> freezeSmallMap(
    map: SmallMap<K, V>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<KFrozen>,
    freezeValue: (V, Freezer) -> FreezeResult<VFrozen>,
): FreezeResult<SmallMap<KFrozen, VFrozen>> {
    return map.freeze(freezer, freezeKey, freezeValue)
}

fun <T, TFrozen> freezeSmallSet(
    set: SmallSet<T>,
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<SmallSet<TFrozen>> {
    return set.freeze(freezer, freeze)
}

/**
 * Minimal stand-in for Rust's `marker::PhantomData<T>`.
 */
data class PhantomData<T>(val unit: Unit = Unit) {
    companion object {
        fun <T> new(): PhantomData<T> = PhantomData()
    }
}

/** Kotlin equivalent of Rust's 1-tuple `(A,)`. */
data class Tuple1<T>(val value0: T)

/** Kotlin equivalent of Rust's 4-tuple `(A, B, C, D)`. */
data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/** Kotlin equivalent of Rust's 5-tuple `(A, B, C, D, E)`. */
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
 *     val data: AdditionalData,
 * ) : Freeze<MyType<F, F>> {
 *     override fun freeze(freezer: Freezer): Result<MyType<F, F>> {
 *         return Result.success(MyType(value.freeze(freezer).getOrThrow(), data))
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

// --- small Rust-shape helpers (line-by-line ports) ---

// impl Freeze for String
fun String.freeze(_freezer: Freezer): FreezeResult<String> {
    return Result.success(this)
}

// Used by some derived-freeze tests (mirrors Rust `Freeze` for String).
fun freezeString(value: String, freezer: Freezer): FreezeResult<String> {
    return value.freeze(freezer)
}

// impl Freeze for i32
fun Int.freeze(_freezer: Freezer): FreezeResult<Int> {
    return Result.success(this)
}

// impl Freeze for u32
fun UInt.freeze(_freezer: Freezer): FreezeResult<UInt> {
    return Result.success(this)
}

// impl Freeze for i64
fun Long.freeze(_freezer: Freezer): FreezeResult<Long> {
    return Result.success(this)
}

// impl Freeze for u64
fun ULong.freeze(_freezer: Freezer): FreezeResult<ULong> {
    return Result.success(this)
}

// impl Freeze for usize
fun Usize.freeze(_freezer: Freezer): FreezeResult<Usize> {
    return Result.success(this)
}

// impl Freeze for bool
fun Boolean.freeze(_freezer: Freezer): FreezeResult<Boolean> {
    return Result.success(this)
}

// Used by some derived-freeze tests (mirrors Rust `Freeze` for bool).
fun freezeBoolean(value: Boolean, freezer: Freezer): FreezeResult<Boolean> {
    return value.freeze(freezer)
}

// impl Freeze for marker::PhantomData<&'v T>
fun <T> PhantomData<T>.freeze(_freezer: Freezer): FreezeResult<PhantomData<T>> {
    return Result.success(PhantomData())
}

// impl<T> Freeze for Vec<T>
fun <T, TFrozen> List<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<List<TFrozen>> {
    return this.intoTryMap { v -> freeze(v, freezer) }
}

// impl<T> Freeze for RefCell<T>
internal fun <T, TFrozen> RefCell<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen> {
    return freeze(this.getMut(), freezer)
}

// impl<T> Freeze for UnsafeCell<T>
fun <T, TFrozen> UnsafeCell<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<UnsafeCell<TFrozen>> {
    val frozen = freeze(this.getMut(), freezer)
    if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
    return Result.success(UnsafeCell(frozen.getOrThrow()))
}

// impl<T> Freeze for OnceCell<T>
fun <T, TFrozen> OnceCell<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen?> {
    val v = this.getMut()
    return if (v == null) {
        Result.success(null)
    } else {
        freeze(v, freezer).map { it }
    }
}

// impl<T> Freeze for Box<T>
fun <T, TFrozen> Box<T>.freeze(freezer: Freezer, freeze: (T, Freezer) -> FreezeResult<TFrozen>): FreezeResult<Box<TFrozen>> {
    val frozen = freeze(this.asMut(), freezer)
    if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
    return Result.success(Box(frozen.getOrThrow()))
}

// impl<T> Freeze for Box<[T]>
fun <T, TFrozen> Box<List<T>>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<Box<List<TFrozen>>> {
    return this.asMut()
        .intoTryMap { v -> freeze(v, freezer) }
        .map { v -> Box(v) }
}

// impl<T> Freeze for Option<T>
fun <T, TFrozen> T?.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen?> {
    return if (this == null) {
        Result.success(null)
    } else {
        freeze(this, freezer).map { it }
    }
}

// impl<K: Freeze> Freeze for Hashed<K>
fun <K, KFrozen> Hashed<K>.freeze(
    freezer: Freezer,
    freeze: (K, Freezer) -> FreezeResult<KFrozen>,
): FreezeResult<Hashed<KFrozen>> {
    val key = this.intoKey()
    val frozenKey = freeze(key, freezer)
    if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
    return Result.success(Hashed.newUnchecked(this.hash(), frozenKey.getOrThrow()))
}

// impl<K, V> Freeze for SmallMap<K, V>
fun <K, V, KFrozen, VFrozen> SmallMap<K, V>.freeze(
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<KFrozen>,
    freezeValue: (V, Freezer) -> FreezeResult<VFrozen>,
): FreezeResult<SmallMap<KFrozen, VFrozen>> {
    val new = SmallMap.withCapacity<KFrozen, VFrozen>(this.len())
    for ((key, value) in this.intoIterHashed()) {
        val hash = key.hash()
        val frozenKeyResult = freezeKey(key.intoKey(), freezer)
        if (frozenKeyResult.isFailure) return Result.failure(frozenKeyResult.exceptionOrNull()!!)
        val hashedFrozenKey = Hashed.newUnchecked(hash, frozenKeyResult.getOrThrow())
        val frozenValueResult = freezeValue(value, freezer)
        if (frozenValueResult.isFailure) return Result.failure(frozenValueResult.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(hashedFrozenKey, frozenValueResult.getOrThrow())
    }
    return Result.success(new)
}

// impl<T> Freeze for SmallSet<T>
fun <T, TFrozen> SmallSet<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<SmallSet<TFrozen>> {
    val new = SmallSet.withCapacity<TFrozen>(this.len())
    for (value in this.intoIterHashed()) {
        val value = value.freeze(freezer, freeze)
        if (value.isFailure) return Result.failure(value.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(value.getOrThrow())
    }
    return Result.success(new)
}

/** Freeze implementation for [Value]. Delegates to [Freezer.freeze]. */
fun Value.freeze(freezer: Freezer): FreezeResult<FrozenValue> {
    return freezer.freeze(this)
}

/** Freeze implementation for [FrozenValue]. Identity freeze — already frozen. */
fun FrozenValue.freeze(_freezer: Freezer): FreezeResult<FrozenValue> {
    return Result.success(this)
}

/** Freeze implementation for [Unit] (Rust `()`). Identity freeze. */
fun Unit.freeze(_freezer: Freezer): FreezeResult<Unit> {
    return Result.success(Unit)
}

/** Freeze implementation for [Tuple1] (Rust 1-tuple `(A,)`). */
fun <A, AFrozen> Tuple1<A>.freeze(
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<AFrozen>,
): FreezeResult<Tuple1<AFrozen>> {
    val fa = freezeA(this.value0, freezer)
    if (fa.isFailure) return Result.failure(fa.exceptionOrNull()!!)
    return Result.success(Tuple1(fa.getOrThrow()))
}

/** Freeze implementation for [Pair] (Rust 2-tuple `(A, B)`). */
fun <A, B, AFrozen, BFrozen> Pair<A, B>.freeze(
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<AFrozen>,
    freezeB: (B, Freezer) -> FreezeResult<BFrozen>,
): FreezeResult<Pair<AFrozen, BFrozen>> {
    val a = freezeA(this.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(this.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    return Result.success(Pair(a.getOrThrow(), b.getOrThrow()))
}

/** Freeze implementation for [Triple] (Rust 3-tuple `(A, B, C)`). */
fun <A, B, C, AFrozen, BFrozen, CFrozen> Triple<A, B, C>.freeze(
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<AFrozen>,
    freezeB: (B, Freezer) -> FreezeResult<BFrozen>,
    freezeC: (C, Freezer) -> FreezeResult<CFrozen>,
): FreezeResult<Triple<AFrozen, BFrozen, CFrozen>> {
    val a = freezeA(this.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(this.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(this.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    return Result.success(Triple(a.getOrThrow(), b.getOrThrow(), c.getOrThrow()))
}

/** Freeze implementation for [Tuple4] (Rust 4-tuple `(A, B, C, D)`). */
fun <A, B, C, D, AFrozen, BFrozen, CFrozen, DFrozen> Tuple4<A, B, C, D>.freeze(
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<AFrozen>,
    freezeB: (B, Freezer) -> FreezeResult<BFrozen>,
    freezeC: (C, Freezer) -> FreezeResult<CFrozen>,
    freezeD: (D, Freezer) -> FreezeResult<DFrozen>,
): FreezeResult<Tuple4<AFrozen, BFrozen, CFrozen, DFrozen>> {
    val a = freezeA(this.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(this.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(this.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    val d = freezeD(this.fourth, freezer)
    if (d.isFailure) return Result.failure(d.exceptionOrNull()!!)
    return Result.success(Tuple4(a.getOrThrow(), b.getOrThrow(), c.getOrThrow(), d.getOrThrow()))
}

/** Freeze implementation for [Tuple5] (Rust 5-tuple `(A, B, C, D, E)`). */
fun <A, B, C, D, E, AFrozen, BFrozen, CFrozen, DFrozen, EFrozen> Tuple5<A, B, C, D, E>.freeze(
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<AFrozen>,
    freezeB: (B, Freezer) -> FreezeResult<BFrozen>,
    freezeC: (C, Freezer) -> FreezeResult<CFrozen>,
    freezeD: (D, Freezer) -> FreezeResult<DFrozen>,
    freezeE: (E, Freezer) -> FreezeResult<EFrozen>,
): FreezeResult<Tuple5<AFrozen, BFrozen, CFrozen, DFrozen, EFrozen>> {
    val a = freezeA(this.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(this.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(this.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    val d = freezeD(this.fourth, freezer)
    if (d.isFailure) return Result.failure(d.exceptionOrNull()!!)
    val e = freezeE(this.fifth, freezer)
    if (e.isFailure) return Result.failure(e.exceptionOrNull()!!)
    return Result.success(Tuple5(a.getOrThrow(), b.getOrThrow(), c.getOrThrow(), d.getOrThrow(), e.getOrThrow()))
}
