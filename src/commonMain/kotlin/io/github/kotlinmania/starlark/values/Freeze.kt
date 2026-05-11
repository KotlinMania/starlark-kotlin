// port-lint: source values/freeze.rs
package io.github.kotlinmania.starlark.values

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlarkmap.smallset.SmallSet

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


fun String.freeze(freezer: Freezer): Result<String> {
    return Result.success(this)
}

fun Int.freeze(freezer: Freezer): Result<Int> {
    return Result.success(this)
}

fun UInt.freeze(freezer: Freezer): Result<UInt> {
    return Result.success(this)
}

fun Long.freeze(freezer: Freezer): Result<Long> {
    return Result.success(this)
}

fun ULong.freeze(freezer: Freezer): Result<ULong> {
    return Result.success(this)
}

fun Boolean.freeze(freezer: Freezer): Result<Boolean> {
    return Result.success(this)
}

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

fun <T : Freeze<TFrozen>, TFrozen> List<T>.freeze(freezer: Freezer): Result<List<TFrozen>> {
    val result = ArrayList<TFrozen>(this.size)
    for (v in this) {
        val frozen = v.freeze(freezer).getOrElse { return Result.failure(it) }
        result.add(frozen)
    }
    return Result.success(result)
}

fun <T, TFrozen> T?.freeze(
    freezer: Freezer,
    freezeElement: (T) -> Result<TFrozen>,
): Result<TFrozen?> {
    if (this == null) return Result.success(null)
    return freezeElement(this).map { it }
}

fun <T : Freeze<TFrozen>, TFrozen> T?.freeze(freezer: Freezer): Result<TFrozen?> {
    return freeze(freezer) { v -> v.freeze(freezer) }
}

fun <K, KFrozen> Hashed<K>.freeze(
    freezer: Freezer,
    freezeKey: (K) -> Result<KFrozen>,
): Result<Hashed<KFrozen>> {
    // `freeze` must not change hash.
    val frozenKey = freezeKey(this.intoKey()).getOrElse { return Result.failure(it) }
    return Result.success(Hashed.newUnchecked(this.hash(), frozenKey))
}

fun <K : Freeze<KFrozen>, KFrozen> Hashed<K>.freeze(freezer: Freezer): Result<Hashed<KFrozen>> {
    val frozenKey = this.intoKey().freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Hashed.newUnchecked(this.hash(), frozenKey))
}

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

fun <K : Freeze<KFrozen>, V : Freeze<VFrozen>, KFrozen, VFrozen> SmallMap<K, V>.freeze(
    freezer: Freezer,
): Result<SmallMap<KFrozen, VFrozen>> {
    return freeze(freezer, freezeKey = { k -> k.freeze(freezer) }, freezeValue = { v -> v.freeze(freezer) })
}

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

fun freezeUnit(freezer: Freezer): Result<Unit> {
    return Result.success(Unit)
}


fun <A : Freeze<AFrozen>, B : Freeze<BFrozen>, AFrozen, BFrozen> Pair<A, B>.freeze(
    freezer: Freezer,
): Result<Pair<AFrozen, BFrozen>> {
    val a = this.first.freeze(freezer).getOrElse { return Result.failure(it) }
    val b = this.second.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Pair(a, b))
}

fun <A : Freeze<AFrozen>, B : Freeze<BFrozen>, C : Freeze<CFrozen>, AFrozen, BFrozen, CFrozen> Triple<A, B, C>.freeze(
    freezer: Freezer,
): Result<Triple<AFrozen, BFrozen, CFrozen>> {
    val a = this.first.freeze(freezer).getOrElse { return Result.failure(it) }
    val b = this.second.freeze(freezer).getOrElse { return Result.failure(it) }
    val c = this.third.freeze(freezer).getOrElse { return Result.failure(it) }
    return Result.success(Triple(a, b, c))
}
