// port-lint: source src/values/freeze.rs
package io.github.kotlinmania.starlark.values

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.smallset.SmallSet
import io.github.kotlinmania.starlark.syntax.slicevecext.intoTryMap
import io.github.kotlinmania.starlark.util.boxed.Box
import io.github.kotlinmania.starlark.util.cell.OnceCell
import io.github.kotlinmania.starlark.util.cell.UnsafeCell
import io.github.kotlinmania.starlark.util.refcell.RefCell
import io.github.kotlinmania.starlark.util.scalar.Usize
import io.github.kotlinmania.starlark.values.layout.Freezer

typealias FreezeResult<T> = Result<T>

// Convenience wrappers used by many transliterations.
// These correspond to Rust's blanket `Freeze` impls (Vec/Option/SmallMap/SmallSet) but are called
// as free functions from some ports to preserve Rust call sites.

fun <T, TFrozen> freezeList(
    list: List<T>,
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<List<TFrozen>> = list.freeze(freezer, freeze)

fun <T, TFrozen> freezeNullable(
    value: T?,
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen?> = value.freeze(freezer, freeze)

fun <K, V, KFrozen, VFrozen> freezeSmallMap(
    map: SmallMap<K, V>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<KFrozen>,
    freezeValue: (V, Freezer) -> FreezeResult<VFrozen>,
): FreezeResult<SmallMap<KFrozen, VFrozen>> = map.freeze(freezer, freezeKey, freezeValue)

fun <T, TFrozen> freezeSmallSet(
    set: SmallSet<T>,
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<SmallSet<TFrozen>> = set.freeze(freezer, freeze)

/**
 * Minimal stand-in for Rust's `marker::PhantomData<T>`.
 */
data class PhantomData<T>(
    val unit: Unit = Unit,
) {
    companion object {
        fun <T> new(): PhantomData<T> = PhantomData()
    }
}

/** Kotlin equivalent of Rust's 1-tuple `(A,)`. */
data class Tuple1<T>(
    val value0: T,
)

/** Kotlin equivalent of Rust's 4-tuple `(A, B, C, D)`. */
data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

/** Kotlin equivalent of Rust's 5-tuple `(A, B, C, D, E)`. */
data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)

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

internal fun String.freeze(freezer: Freezer): FreezeResult<String> = Result.success(this)

// Used by some derived-freeze tests (mirrors Rust `Freeze` for String).
fun freezeString(value: String, freezer: Freezer): FreezeResult<String> = value.freeze(freezer)

internal fun Int.freeze(freezer: Freezer): FreezeResult<Int> = Result.success(this)

internal fun UInt.freeze(freezer: Freezer): FreezeResult<UInt> = Result.success(this)

internal fun Long.freeze(freezer: Freezer): FreezeResult<Long> = Result.success(this)

internal fun ULong.freeze(freezer: Freezer): FreezeResult<ULong> = Result.success(this)

internal fun Usize.freeze(freezer: Freezer): FreezeResult<Usize> = Result.success(this)

internal fun Boolean.freeze(freezer: Freezer): FreezeResult<Boolean> = Result.success(this)

// Used by some derived-freeze tests (mirrors Rust `Freeze` for bool).
fun freezeBoolean(value: Boolean, freezer: Freezer): FreezeResult<Boolean> = value.freeze(freezer)

internal fun <T> PhantomData<T>.freeze(freezer: Freezer): FreezeResult<PhantomData<T>> = Result.success(PhantomData())

internal fun <T, TFrozen> List<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<List<TFrozen>> = this.intoTryMap { v -> freeze(v, freezer) }

internal fun <T, TFrozen> RefCell<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen> = freeze(this.getMut(), freezer)

internal fun <T, TFrozen> UnsafeCell<T>.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<UnsafeCell<TFrozen>> {
    val frozen = freeze(this.getMut(), freezer)
    if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
    return Result.success(UnsafeCell(frozen.getOrThrow()))
}

internal fun <T, TFrozen> OnceCell<T>.freeze(
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

internal fun <T, TFrozen> Box<T>.freeze(freezer: Freezer, freeze: (T, Freezer) -> FreezeResult<TFrozen>): FreezeResult<Box<TFrozen>> {
    val frozen = freeze(this.asMut(), freezer)
    if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
    return Result.success(Box(frozen.getOrThrow()))
}

// Distinct Kotlin name (not `freeze`) to avoid a JVM signature clash with the
// parameter on Box, so both extensions would compile down to the same
// `freeze(Box, Freezer, Function2)` JVM signature. Per the kotlinmania
// JVM-clash workaround we use a distinct Kotlin name rather than @JvmName.
internal fun <T, TFrozen> Box<List<T>>.freezeListBox(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<Box<List<TFrozen>>> =
    this
        .asMut()
        .intoTryMap { v -> freeze(v, freezer) }
        .map { v -> Box(v) }

internal fun <T, TFrozen> T?.freeze(
    freezer: Freezer,
    freeze: (T, Freezer) -> FreezeResult<TFrozen>,
): FreezeResult<TFrozen?> =
    if (this == null) {
        Result.success(null)
    } else {
        freeze(this, freezer).map { it }
    }

internal fun <K, KFrozen> Hashed<K>.freeze(
    freezer: Freezer,
    freeze: (K, Freezer) -> FreezeResult<KFrozen>,
): FreezeResult<Hashed<KFrozen>> {
    val key = this.intoKey()
    val frozenKey = freeze(key, freezer)
    if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
    return Result.success(Hashed.newUnchecked(this.hash(), frozenKey.getOrThrow()))
}

internal fun <K, V, KFrozen, VFrozen> SmallMap<K, V>.freeze(
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

internal fun <T, TFrozen> SmallSet<T>.freeze(
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

/** Freeze implementation for [Unit] (Rust `()`). Identity freeze. */
internal fun Unit.freeze(freezer: Freezer): FreezeResult<Unit> = Result.success(Unit)

/** Freeze implementation for [Tuple1] (Rust 1-tuple `(A,)`). */
internal fun <A, AFrozen> Tuple1<A>.freeze(
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<AFrozen>,
): FreezeResult<Tuple1<AFrozen>> {
    val fa = freezeA(this.value0, freezer)
    if (fa.isFailure) return Result.failure(fa.exceptionOrNull()!!)
    return Result.success(Tuple1(fa.getOrThrow()))
}

/** Freeze implementation for [Pair] (Rust 2-tuple `(A, B)`). */
internal fun <A, B, AFrozen, BFrozen> Pair<A, B>.freeze(
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
internal fun <A, B, C, AFrozen, BFrozen, CFrozen> Triple<A, B, C>.freeze(
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
internal fun <A, B, C, D, AFrozen, BFrozen, CFrozen, DFrozen> Tuple4<A, B, C, D>.freeze(
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
internal fun <A, B, C, D, E, AFrozen, BFrozen, CFrozen, DFrozen, EFrozen> Tuple5<A, B, C, D, E>.freeze(
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
