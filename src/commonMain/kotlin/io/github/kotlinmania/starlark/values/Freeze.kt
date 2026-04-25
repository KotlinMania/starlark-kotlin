// port-lint: source src/values/freeze.rs
package io.github.kotlinmania.starlark.values

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

import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import starlarkmap.Hashed
import starlarkmap.smallmap.SmallMap
import starlarkmap.smallset.SmallSet

/**
 * Kotlin equivalent of Rust's `PhantomData<T>`.
 *
 * A zero-sized marker type used for type-level tracking without runtime overhead.
 */
class PhantomData<T> private constructor() {
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
    fun freeze(freezer: Freezer): Result<Frozen>
}

// ---- impl Freeze for primitive/simple types ----

/** Rust `impl Freeze for String`. */
fun String.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<String> {
    return Result.success(this)
}

/** Rust `impl Freeze for i32` (Kotlin [Int]). */
fun Int.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Int> {
    return Result.success(this)
}

/** Rust `impl Freeze for u32` (Kotlin [UInt]). */
fun UInt.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<UInt> {
    return Result.success(this)
}

/** Rust `impl Freeze for i64` (Kotlin [Long]). */
fun Long.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Long> {
    return Result.success(this)
}

/** Rust `impl Freeze for u64` (Kotlin [ULong]). */
fun ULong.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<ULong> {
    return Result.success(this)
}

/** Rust `impl Freeze for bool` (Kotlin [Boolean]). */
fun Boolean.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Boolean> {
    return Result.success(this)
}

/** Rust `impl Freeze for PhantomData<&'v T>`. */
fun <T> PhantomData<T>.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<PhantomData<T>> {
    return Result.success(PhantomData.new())
}

// ---- impl Freeze for container types ----

/** Rust `impl<T: Freeze> Freeze for Vec<T>`. */
fun <T, TFrozen> List<T>.freeze(
    freezer: Freezer,
    freezeElement: (T) -> Result<TFrozen>,
): Result<List<TFrozen>> {
    val result = ArrayList<TFrozen>(this.size)
    for (v in this) {
        val frozen = freezeElement(v).getOrElse { return Result.failure(it) }
        result.add(frozen)
    }
    return Result.success(result)
}

/** Rust `impl<T: Freeze> Freeze for Vec<T>` (when `T` implements [Freeze]). */
fun <T : Freeze<TFrozen>, TFrozen> List<T>.freeze(freezer: Freezer): Result<List<TFrozen>> {
    val result = ArrayList<TFrozen>(this.size)
    for (v in this) {
        val frozen = v.freeze(freezer).getOrElse { return Result.failure(it) }
        result.add(frozen)
    }
    return Result.success(result)
}

/** Rust `impl<T: Freeze> Freeze for Option<T>` (Kotlin nullable). */
fun <T, TFrozen> T?.freeze(
    freezer: Freezer,
    freezeElement: (T) -> Result<TFrozen>,
): Result<TFrozen?> {
    if (this == null) return Result.success(null)
    return freezeElement(this).map { it }
}

/** Rust `impl<T: Freeze> Freeze for Option<T>` (when `T` implements [Freeze]). */
fun <T : Freeze<TFrozen>, TFrozen> T?.freeze(freezer: Freezer): Result<TFrozen?> {
    return freeze(freezer) { v -> v.freeze(freezer) }
}

/** Rust `impl<T: Freeze> Freeze for Box<T>`. */
fun <T, TFrozen> Box<T>.freeze(
    freezer: Freezer,
    freezeInner: (T) -> Result<TFrozen>,
): Result<Box<TFrozen>> {
    val frozen = freezeInner(this.value).getOrElse { return Result.failure(it) }
    return Result.success(Box(frozen))
}

/** Rust `impl<T: Freeze> Freeze for Box<T>` (when `T` implements [Freeze]). */
fun <T : Freeze<TFrozen>, TFrozen> Box<T>.freeze(freezer: Freezer): Result<Box<TFrozen>> {
    val frozen = this.value.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Box(frozen))
}

/** Rust `impl<K: Freeze> Freeze for Hashed<K>`. */
fun <K, KFrozen> Hashed<K>.freeze(
    freezer: Freezer,
    freezeKey: (K) -> Result<KFrozen>,
): Result<Hashed<KFrozen>> {
    // `freeze` must not change hash.
    val frozenKey = freezeKey(this.intoKey()).getOrElse { return Result.failure(it) }
    return Result.success(Hashed.newUnchecked(this.hash(), frozenKey))
}

/** Rust `impl<K: Freeze> Freeze for Hashed<K>` (when `K` implements [Freeze]). */
fun <K : Freeze<KFrozen>, KFrozen> Hashed<K>.freeze(freezer: Freezer): Result<Hashed<KFrozen>> {
    val frozenKey = this.intoKey().freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Hashed.newUnchecked(this.hash(), frozenKey))
}

/** Rust `impl<K: Freeze, V: Freeze> Freeze for SmallMap<K, V>`. */
fun <K, V, KFrozen, VFrozen> SmallMap<K, V>.freeze(
    freezer: Freezer,
    freezeKey: (K) -> Result<KFrozen>,
    freezeValue: (V) -> Result<VFrozen>,
): Result<SmallMap<KFrozen, VFrozen>> {
    val result = SmallMap.withCapacity<KFrozen, VFrozen>(this.len())
    for ((key, value) in this.intoIterHashed()) {
        val hash = key.hash()
        val frozenKey = freezeKey(key.intoKey()).getOrElse { return Result.failure(it) }
        val hashedKey = Hashed.newUnchecked(hash, frozenKey)
        val frozenValue = freezeValue(value).getOrElse { return Result.failure(it) }
        result.insertHashedUniqueUnchecked(hashedKey, frozenValue)
    }
    return Result.success(result)
}

/** Rust `impl<K: Freeze, V: Freeze> Freeze for SmallMap<K, V>` (when both implement [Freeze]). */
fun <K : Freeze<KFrozen>, V : Freeze<VFrozen>, KFrozen, VFrozen> SmallMap<K, V>.freeze(
    freezer: Freezer,
): Result<SmallMap<KFrozen, VFrozen>> {
    return freeze(freezer, freezeKey = { k -> k.freeze(freezer) }, freezeValue = { v -> v.freeze(freezer) })
}

/** Rust `impl<T: Freeze> Freeze for SmallSet<T>`. */
fun <T, TFrozen> SmallSet<T>.freeze(
    freezer: Freezer,
    freezeElement: (T) -> Result<TFrozen>,
): Result<SmallSet<TFrozen>> {
    val result = SmallSet.withCapacity<TFrozen>(this.len())
    for (value in this.intoIterHashed()) {
        val hash = value.hash()
        val frozen = freezeElement(value.intoKey()).getOrElse { return Result.failure(it) }
        val hashed = Hashed.newUnchecked(hash, frozen)
        result.insertHashedUniqueUnchecked(hashed)
    }
    return Result.success(result)
}

/** Rust `impl<T: Freeze> Freeze for SmallSet<T>` (when `T` implements [Freeze]). */
fun <T : Freeze<TFrozen>, TFrozen> SmallSet<T>.freeze(freezer: Freezer): Result<SmallSet<TFrozen>> {
    val result = SmallSet.withCapacity<TFrozen>(this.len())
    for (value in this.intoIterHashed()) {
        val hash = value.hash()
        val frozen = value.intoKey().freeze(freezer).getOrElse { return Result.failure(it) }
        val hashed = Hashed.newUnchecked(hash, frozen)
        result.insertHashedUniqueUnchecked(hashed)
    }
    return Result.success(result)
}

/** Freeze implementation for [Unit] (Rust `()`). Identity freeze. */
fun freezeUnit(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Unit> {
    return Result.success(Unit)
}

// ---- impl Freeze for tuples ----

/** Rust `impl<A: Freeze> Freeze for (A,)`. */
fun <A : Freeze<AFrozen>, AFrozen> Tuple1<A>.freeze(freezer: Freezer): Result<Tuple1<AFrozen>> {
    val frozen0 = this.value0.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Tuple1(frozen0))
}

/** Rust `impl<A: Freeze, B: Freeze> Freeze for (A, B)`. */
fun <A : Freeze<AFrozen>, B : Freeze<BFrozen>, AFrozen, BFrozen> Pair<A, B>.freeze(
    freezer: Freezer,
): Result<Pair<AFrozen, BFrozen>> {
    val a = this.first.freeze(freezer).getOrElse { return Result.failure(it) }
    val b = this.second.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Pair(a, b))
}

/** Rust `impl<A: Freeze, B: Freeze, C: Freeze> Freeze for (A, B, C)`. */
fun <A : Freeze<AFrozen>, B : Freeze<BFrozen>, C : Freeze<CFrozen>, AFrozen, BFrozen, CFrozen> Triple<A, B, C>.freeze(
    freezer: Freezer,
): Result<Triple<AFrozen, BFrozen, CFrozen>> {
    val a = this.first.freeze(freezer).getOrElse { return Result.failure(it) }
    val b = this.second.freeze(freezer).getOrElse { return Result.failure(it) }
    val c = this.third.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Triple(a, b, c))
}

/** Rust `impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze> Freeze for (A, B, C, D)`. */
fun <
    A : Freeze<AFrozen>,
    B : Freeze<BFrozen>,
    C : Freeze<CFrozen>,
    D : Freeze<DFrozen>,
    AFrozen,
    BFrozen,
    CFrozen,
    DFrozen,
    > Tuple4<A, B, C, D>.freeze(freezer: Freezer): Result<Tuple4<AFrozen, BFrozen, CFrozen, DFrozen>> {
    val a = this.first.freeze(freezer).getOrElse { return Result.failure(it) }
    val b = this.second.freeze(freezer).getOrElse { return Result.failure(it) }
    val c = this.third.freeze(freezer).getOrElse { return Result.failure(it) }
    val d = this.fourth.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Tuple4(a, b, c, d))
}

/** Rust `impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze, E: Freeze> Freeze for (A, B, C, D, E)`. */
fun <
    A : Freeze<AFrozen>,
    B : Freeze<BFrozen>,
    C : Freeze<CFrozen>,
    D : Freeze<DFrozen>,
    E : Freeze<EFrozen>,
    AFrozen,
    BFrozen,
    CFrozen,
    DFrozen,
    EFrozen,
    > Tuple5<A, B, C, D, E>.freeze(
    freezer: Freezer,
): Result<Tuple5<AFrozen, BFrozen, CFrozen, DFrozen, EFrozen>> {
    val a = this.first.freeze(freezer).getOrElse { return Result.failure(it) }
    val b = this.second.freeze(freezer).getOrElse { return Result.failure(it) }
    val c = this.third.freeze(freezer).getOrElse { return Result.failure(it) }
    val d = this.fourth.freeze(freezer).getOrElse { return Result.failure(it) }
    val e = this.fifth.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Tuple5(a, b, c, d, e))
}
