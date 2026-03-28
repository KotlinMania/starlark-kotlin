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
import starlark_map.vec_map.insertHashedUniqueUnchecked
import starlark_map.intoKey
import io.github.kotlinmania.starlark_kotlin.eval.bc.withCapacity
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

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
    return FreezeResult.success(value)
}

// impl Freeze for i32
fun freezeInt(value: Int, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Int> {
    return FreezeResult.success(value)
}

// impl Freeze for u32
fun freezeUInt(value: UInt, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<UInt> {
    return FreezeResult.success(value)
}

// impl Freeze for i64
fun freezeLong(value: Long, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Long> {
    return FreezeResult.success(value)
}

// impl Freeze for u64
fun freezeULong(value: ULong, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<ULong> {
    return FreezeResult.success(value)
}

// impl Freeze for usize
// (usize maps to Int in Kotlin for most practical purposes)

// impl Freeze for bool
fun freezeBoolean(value: Boolean, @Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Boolean> {
    return FreezeResult.success(value)
}

// impl Freeze for Vec<T> where T: Freeze
fun <T, F> freezeList(
    list: List<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<List<F>> {
    val result = mutableListOf<F>()
    for (element in list) {
        val frozen = freezeElement(element, freezer)
        if (frozen.isFailure) return FreezeResult.failure(frozen.exceptionOrNull()!!)
        result.add(frozen.get())
    }
    return FreezeResult.success(result)
}

// impl Freeze for Option<T> where T: Freeze
fun <T, F> freezeNullable(
    value: T?,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<F?> {
    if (value == null) return FreezeResult.success(null)
    return freezeElement(value, freezer).map { it }
}

// impl Freeze for Hashed<K> where K: Freeze
fun <K, FK> freezeHashed(
    hashed: Hashed<K>,
    freezer: Freezer,
    freezeKey: (K, Freezer) -> FreezeResult<FK>,
): FreezeResult<Hashed<FK>> {
    // `freeze` must not change hash.
    val frozenKey = freezeKey(hashed.intoKey(), freezer)
    if (frozenKey.isFailure) return FreezeResult.failure(frozenKey.exceptionOrNull()!!)
    return FreezeResult.success(Hashed.newUnchecked(hashed.hash(), frozenKey.get()))
}

// impl Freeze for SmallMap<K, V> where K: Freeze, V: Freeze
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
        if (frozenKey.isFailure) return FreezeResult.failure(frozenKey.exceptionOrNull()!!)
        val hashedKey = Hashed.newUnchecked(hash, frozenKey.get())
        val frozenValue = freezeValue(value, freezer)
        if (frozenValue.isFailure) return FreezeResult.failure(frozenValue.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(hashedKey, frozenValue.get())
    }
    return FreezeResult.success(new)
}

// impl Freeze for SmallSet<T> where T: Freeze
fun <T, F> freezeSmallSet(
    set: SmallSet<T>,
    freezer: Freezer,
    freezeElement: (T, Freezer) -> FreezeResult<F>,
): FreezeResult<SmallSet<F>> {
    val new = SmallSet.withCapacity<F>(set.len())
    for (value in set.intoIterHashed()) {
        val frozenValue = freezeElement(value, freezer)
        if (frozenValue.isFailure) return FreezeResult.failure(frozenValue.exceptionOrNull()!!)
        new.insertHashedUniqueUnchecked(frozenValue.get())
    }
    return FreezeResult.success(new)
}

// impl Freeze for Value
fun Value.freeze(freezer: Freezer): FreezeResult<FrozenValue> {
    return freezer.freeze(this)
}

// impl Freeze for FrozenValue
fun FrozenValue.freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<FrozenValue> {
    return FreezeResult.success(this)
}

// impl Freeze for ()
fun freezeUnit(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<Unit> {
    return FreezeResult.success(Unit)
}

// impl<A: Freeze> Freeze for (A,)
fun <A, FA> freezeTuple1(
    a: A,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
): FreezeResult<FA> {
    return freezeA(a, freezer)
}

// impl<A: Freeze, B: Freeze> Freeze for (A, B)
fun <A, B, FA, FB> freezePair(
    pair: Pair<A, B>,
    freezer: Freezer,
    freezeA: (A, Freezer) -> FreezeResult<FA>,
    freezeB: (B, Freezer) -> FreezeResult<FB>,
): FreezeResult<Pair<FA, FB>> {
    val a = freezeA(pair.first, freezer)
    if (a.isFailure) return FreezeResult.failure(a.exceptionOrNull()!!)
    val b = freezeB(pair.second, freezer)
    if (b.isFailure) return FreezeResult.failure(b.exceptionOrNull()!!)
    return FreezeResult.success(Pair(a.get(), b.get()))
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
    if (a.isFailure) return FreezeResult.failure(a.exceptionOrNull()!!)
    val b = freezeB(triple.second, freezer)
    if (b.isFailure) return FreezeResult.failure(b.exceptionOrNull()!!)
    val c = freezeC(triple.third, freezer)
    if (c.isFailure) return FreezeResult.failure(c.exceptionOrNull()!!)
    return FreezeResult.success(Triple(a.get(), b.get(), c.get()))
}

// impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze> Freeze for (A, B, C, D)
// Kotlin does not have 4-tuples natively; use a data class or list
// impl<A: Freeze, B: Freeze, C: Freeze, D: Freeze, E: Freeze> Freeze for (A, B, C, D, E)
// Kotlin does not have 5-tuples natively; use a data class or list
