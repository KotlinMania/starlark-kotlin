// port-lint: source src/values/types/dict/traits.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr

// SmallMap

/** AllocValue for SmallMap<K, V>. */
fun <K : AllocValue, T : AllocValue> SmallMap<K, T>.allocValue(heap: Heap): Value =
    AllocDict(this.iter().asIterable()).allocValue(heap)

/** AllocFrozenValue for SmallMap<K, V>. */
fun <K : AllocFrozenValue, V : AllocFrozenValue> SmallMap<K, V>.allocFrozenValue(heap: FrozenHeap): FrozenValue =
    AllocDict(this.iter().asIterable()).allocFrozenValue(heap)

/** AllocValue for &SmallMap<K, V>. */
fun <K : StarlarkTypeRepr, T : StarlarkTypeRepr> SmallMap<K, T>.allocValueRef(heap: Heap): Value
    where K : AllocValue, T : AllocValue =
    AllocDict(this.iter().asIterable()).allocValue(heap)

/** AllocFrozenValue for &SmallMap<K, V>. */
fun <K : StarlarkTypeRepr, V : StarlarkTypeRepr> SmallMap<K, V>.allocFrozenValueRef(heap: FrozenHeap): FrozenValue
    where K : AllocFrozenValue, V : AllocFrozenValue =
    AllocDict(this.iter().asIterable()).allocFrozenValue(heap)

/** StarlarkTypeRepr for &SmallMap<K, V>. */
object SmallMapRefStarlarkTypeRepr {
    inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty =
        DictType.starlarkTypeRepr<K, V>()
}

/** StarlarkTypeRepr for SmallMap<K, V>. */
object SmallMapStarlarkTypeRepr {
    inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty =
        DictType.starlarkTypeRepr<K, V>()
}

/** UnpackValue for SmallMap<K, V> where K: UnpackValue + Hash + Eq, V: UnpackValue. */
object SmallMapUnpackValue {
    fun <K : Any, T : Any> unpackValueImpl(value: Value): Result<SmallMap<K, T>?> {
        val dict = dictRefFromValue(value) ?: return Result.success(null)
        val it = dict.deref().iter()
        val r = SmallMap.withCapacity<K, T>(dict.deref().len())
        for ((k, v) in it) {
            @Suppress("UNCHECKED_CAST")
            val unpackedK = (k as? K) ?: return Result.success(null)
            @Suppress("UNCHECKED_CAST")
            val unpackedV = (v as? T) ?: return Result.success(null)
            r.insert(unpackedK, unpackedV)
        }
        return Result.success(r)
    }
}

// BTreeMap (sorted map in Kotlin)

/** AllocValue for BTreeMap<K, V>. */
fun <K, T> Map<K, T>.allocValueBTreeMap(heap: Heap): Value
    where K : Comparable<K>, K : AllocValue, T : AllocValue =
    AllocDict(this.entries.map { (k, v) -> k to v }).allocValue(heap)

/** AllocFrozenValue for BTreeMap<K, V>. */
fun <K, V> Map<K, V>.allocFrozenValueBTreeMap(heap: FrozenHeap): FrozenValue
    where K : Comparable<K>, K : AllocFrozenValue, V : AllocFrozenValue =
    AllocDict(this.entries.map { (k, v) -> k to v }).allocFrozenValue(heap)

/** AllocValue for &BTreeMap<K, V>. */
fun <K, T> Map<K, T>.allocValueBTreeMapRef(heap: Heap): Value
    where K : Comparable<K>, K : StarlarkTypeRepr, K : AllocValue, T : StarlarkTypeRepr, T : AllocValue =
    AllocDict(this.entries.map { (k, v) -> k to v }).allocValue(heap)

/** AllocFrozenValue for &BTreeMap<K, V>. */
fun <K, V> Map<K, V>.allocFrozenValueBTreeMapRef(heap: FrozenHeap): FrozenValue
    where K : Comparable<K>, K : StarlarkTypeRepr, K : AllocFrozenValue, V : StarlarkTypeRepr, V : AllocFrozenValue =
    AllocDict(this.entries.map { (k, v) -> k to v }).allocFrozenValue(heap)

/** StarlarkTypeRepr for &BTreeMap<K, V>. */
object BTreeMapRefStarlarkTypeRepr {
    inline fun <reified K, reified V> starlarkTypeRepr(): Ty
        where K : Comparable<K>, K : StarlarkTypeRepr, V : StarlarkTypeRepr =
        DictType.starlarkTypeRepr<K, V>()
}

/** StarlarkTypeRepr for BTreeMap<K, V>. */
object BTreeMapStarlarkTypeRepr {
    inline fun <reified K, reified V> starlarkTypeRepr(): Ty
        where K : Comparable<K>, K : StarlarkTypeRepr, V : StarlarkTypeRepr =
        DictType.starlarkTypeRepr<K, V>()
}

/** UnpackValue for BTreeMap<K, V> where K: UnpackValue + Ord, V: UnpackValue. */
object BTreeMapUnpackValue {
    fun <K : Comparable<K>, T : Any> unpackValueImpl(value: Value): Result<MutableMap<K, T>?> {
        val dict = dictRefFromValue(value) ?: return Result.success(null)
        val r = mutableMapOf<K, T>()
        for ((k, v) in dict.deref().iter()) {
            @Suppress("UNCHECKED_CAST")
            val unpackedK = (k as? K) ?: return Result.success(null)
            @Suppress("UNCHECKED_CAST")
            val unpackedV = (v as? T) ?: return Result.success(null)
            r[unpackedK] = unpackedV
        }
        return Result.success(r)
    }
}

private fun DictRef.deref(): Dict = when (val ref = aref) {
    is Either.Left -> ref.value.value
    is Either.Right -> ref.value
}
