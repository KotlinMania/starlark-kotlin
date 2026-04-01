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

import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.small_set.SmallSet

/**
 * A zero-sized marker type used for type-level tracking without runtime overhead.
 *
 * Kotlin equivalent of Rust's `PhantomData<T>`.
 * In Rust, `PhantomData<T>` is used to indicate ownership or variance without
 * storing a value of type `T`.
 */
@ConsistentCopyVisibility
data class PhantomData<T> private constructor(val unit: Unit = Unit) {
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

/** Freeze implementation for [String]. Identity freeze. */
fun freezeString(self: String, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<String> {
    return Result.success(self)
}

/** Freeze implementation for [Int] (i32). Identity freeze. */
fun freezeInt(self: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Int> {
    return Result.success(self)
}

/** Freeze implementation for [UInt] (u32). Identity freeze. */
fun freezeUInt(self: UInt, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<UInt> {
    return Result.success(self)
}

/** Freeze implementation for [Long] (i64). Identity freeze. */
fun freezeLong(self: Long, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Long> {
    return Result.success(self)
}

/** Freeze implementation for [ULong] (u64). Identity freeze. */
fun freezeULong(self: ULong, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<ULong> {
    return Result.success(self)
}

/** Freeze implementation for usize (mapped to [Int]). Identity freeze. */
fun freezeUSize(self: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Int> {
    return Result.success(self)
}

/** Freeze implementation for [Boolean]. Identity freeze. */
fun freezeBoolean(self: Boolean, @Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Boolean> {
    return Result.success(self)
}

/** Freeze implementation for [PhantomData]. Returns a new phantom. */
fun <T> freezePhantomData(
    @Suppress("UNUSED_PARAMETER") self: PhantomData<T>,
    @Suppress("UNUSED_PARAMETER") freezer: Freezer,
): Result<PhantomData<T>> {
    return Result.success(PhantomData.new())
}

/** Freeze implementation for [List] (Vec). Freezes each element. */
fun <T, F> freezeList(
    self: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> Result<F>,
): Result<List<F>> {
    val result = mutableListOf<F>()
    for (v in self) {
        val frozen = freezeElement(v, freezer)
        if (frozen.isFailure) return Result.failure(frozen.exceptionOrNull()!!)
        result.add(frozen.getOrThrow())
    }
    return Result.success(result)
}

/** Freeze implementation for RefCell — unwraps and freezes inner value. */
fun <T, F> freezeRefCell(
    self: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> Result<F>,
): Result<F> {
    return freezeInner(self, freezer)
}

/** Freeze implementation for UnsafeCell — freezes inner value and wraps. */
fun <T, F> freezeUnsafeCell(
    self: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> Result<F>,
    wrapResult: (F) -> F,
): Result<F> {
    val frozen = freezeInner(self, freezer)
    if (frozen.isFailure) return frozen
    return Result.success(wrapResult(frozen.getOrThrow()))
}

/** Freeze implementation for OnceCell — maps to nullable in Kotlin. */
fun <T, F> freezeOnceCell(
    self: T?,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> Result<F>,
): Result<F?> {
    return freezeNullable(self, freezer, freezeInner)
}

/** Freeze implementation for Box — freezes inner value. */
fun <T, F> freezeBox(
    self: T,
    freezer: Freezer,
    freezeInner: (T, Freezer) -> Result<F>,
): Result<F> {
    return freezeInner(self, freezer)
}

/** Freeze implementation for boxed slice — maps to [List] in Kotlin. */
fun <T, F> freezeBoxSlice(
    self: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> Result<F>,
): Result<List<F>> {
    return freezeList(self, freezer, freezeElement)
}

/** Freeze implementation for nullable (Option). */
fun <T, F> freezeNullable(
    self: T?,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> Result<F>,
): Result<F?> {
    if (self == null) return Result.success(null)
    return freezeElement(self, freezer).map { it }
}

/** Freeze implementation for [Hashed]. */
fun <K, FK> freezeHashed(
    self: Hashed<K>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> Result<FK>,
): Result<Hashed<FK>> {
    // `freeze` must not change hash.
    val frozenKey = freezeKey(self.intoKey(), freezer)
    if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
    return Result.success(Hashed.newUnchecked(self.hash(), frozenKey.getOrThrow()))
}

/** Freeze implementation for [SmallMap]. */
fun <K, V, FK, FV> freezeSmallMap(
    self: SmallMap<K, V>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> Result<FK>,
    freezeValue: (V, Freezer) -> Result<FV>,
): Result<SmallMap<FK, FV>> {
    val new = SmallMap.withCapacity<FK, FV>(self.len())
    for ((key, value) in self.intoIterHashed()) {
        val hash = key.hash()
        val frozenKey = freezeKey(key.intoKey(), freezer)
        if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
        val hashedKey = Hashed.newUnchecked(hash, frozenKey.getOrThrow())
        val frozenValue = freezeValue(value, freezer)
        if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(hashedKey, frozenValue.getOrThrow())
    }
    return Result.success(new)
}

/** Freeze implementation for [SmallSet]. */
fun <T, F> freezeSmallSet(
    self: SmallSet<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> Result<F>,
): Result<SmallSet<F>> {
    val new = SmallSet.withCapacity<F>(self.len())
    for (value in self.intoIterHashed()) {
        val frozenValue = freezeHashed(value, freezer, freezeElement)
        if (frozenValue.isFailure) return Result.failure(frozenValue.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(frozenValue.getOrThrow())
    }
    return Result.success(new)
}

/** Freeze implementation for [Value]. Delegates to [Freezer.freeze]. */
fun Value.freeze(freezer: Freezer): Result<FrozenValue> {
    return freezer.freeze(this)
}

/** Freeze implementation for [FrozenValue]. Identity freeze — already frozen. */
fun FrozenValue.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<FrozenValue> {
    return Result.success(this)
}

/** Freeze implementation for [Unit] (Rust `()`). Identity freeze. */
fun freezeUnit(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<Unit> {
    return Result.success(Unit)
}

/** Freeze implementation for [Tuple1] (Rust 1-tuple `(A,)`). */
fun <A, FA> freezeTuple1(
    self: Tuple1<A>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> Result<FA>,
): Result<Tuple1<FA>> {
    val fa = freezeA(self.value0, freezer)
    if (fa.isFailure) return Result.failure(fa.exceptionOrNull()!!)
    return Result.success(Tuple1(fa.getOrThrow()))
}

/** Freeze implementation for [Pair] (Rust 2-tuple `(A, B)`). */
fun <A, B, FA, FB> freezePair(
    self: Pair<A, B>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> Result<FA>,
    freezeB: (B, Freezer) -> Result<FB>,
): Result<Pair<FA, FB>> {
    val a = freezeA(self.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(self.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    return Result.success(Pair(a.getOrThrow(), b.getOrThrow()))
}

/** Freeze implementation for [Triple] (Rust 3-tuple `(A, B, C)`). */
fun <A, B, C, FA, FB, FC> freezeTriple(
    self: Triple<A, B, C>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> Result<FA>,
    freezeB: (B, Freezer) -> Result<FB>,
    freezeC: (C, Freezer) -> Result<FC>,
): Result<Triple<FA, FB, FC>> {
    val a = freezeA(self.first, freezer)
    if (a.isFailure) return Result.failure(a.exceptionOrNull()!!)
    val b = freezeB(self.second, freezer)
    if (b.isFailure) return Result.failure(b.exceptionOrNull()!!)
    val c = freezeC(self.third, freezer)
    if (c.isFailure) return Result.failure(c.exceptionOrNull()!!)
    return Result.success(Triple(a.getOrThrow(), b.getOrThrow(), c.getOrThrow()))
}

/** Freeze implementation for [Tuple4] (Rust 4-tuple `(A, B, C, D)`). */
fun <A, B, C, D, FA, FB, FC, FD> freezeTuple4(
    self: Tuple4<A, B, C, D>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> Result<FA>,
    freezeB: (B, Freezer) -> Result<FB>,
    freezeC: (C, Freezer) -> Result<FC>,
    freezeD: (D, Freezer) -> Result<FD>,
): Result<Tuple4<FA, FB, FC, FD>> {
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

/** Freeze implementation for [Tuple5] (Rust 5-tuple `(A, B, C, D, E)`). */
fun <A, B, C, D, E, FA, FB, FC, FD, FE> freezeTuple5(
    self: Tuple5<A, B, C, D, E>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> Result<FA>,
    freezeB: (B, Freezer) -> Result<FB>,
    freezeC: (C, Freezer) -> Result<FC>,
    freezeD: (D, Freezer) -> Result<FD>,
    freezeE: (E, Freezer) -> Result<FE>,
): Result<Tuple5<FA, FB, FC, FD, FE>> {
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
