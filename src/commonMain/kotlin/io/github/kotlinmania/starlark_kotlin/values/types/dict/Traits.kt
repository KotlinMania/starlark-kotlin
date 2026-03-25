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
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.tests.derive.starlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.fromValue
import io.github.kotlinmania.starlark_kotlin.values.value_of.unpackValueImpl
import io.github.kotlinmania.starlark_kotlin.values.types.array.len
import io.github.kotlinmania.starlark_kotlin.values.unpack_and_discard.unpackValueImpl

/**
 * Either type for representing dual error types.
 * Corresponds to `either::Either` in Rust.
 */
sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
}

// SmallMap

/**
 * AllocValue implementation for SmallMap<K, V>.
 * Corresponds to: impl<V_, K: AllocValue<V_>, V: AllocValue<V_>> AllocValue<V_> for SmallMap<K, V>
 */
fun <V, K, T> SmallMap<K, T>.allocValueSmallMap(heap: Heap<V>): Value<V>
    where K : AllocValue<V>,
          T : AllocValue<V> {
    return AllocDict(this).allocValue(heap)
}

/**
 * AllocFrozenValue implementation for SmallMap<K, V>.
 * Corresponds to: impl<K: AllocFrozenValue, V: AllocFrozenValue> AllocFrozenValue for SmallMap<K, V>
 */
fun <K, V> SmallMap<K, V>.allocFrozenValueSmallMap(heap: FrozenHeap): FrozenValue
    where K : AllocFrozenValue,
          V : AllocFrozenValue {
    return AllocDict(this).allocFrozenValue(heap)
}

/**
 * AllocValue implementation for &SmallMap<K, V>.
 * Corresponds to: impl<A_, V_, K: 'a + StarlarkTypeRepr, V: 'a + StarlarkTypeRepr> AllocValue<V_> for &'a SmallMap<K, V>
 */
fun <V, K, T> SmallMap<K, T>.allocValueSmallMapRef(heap: Heap<V>): Value<V>
    where K : StarlarkTypeRepr,
          T : StarlarkTypeRepr {
    return AllocDict(this).allocValue(heap)
}

/**
 * AllocFrozenValue implementation for &SmallMap<K, V>.
 * Corresponds to: impl<A_, K: 'a + StarlarkTypeRepr, V: 'a + StarlarkTypeRepr> AllocFrozenValue for &'a SmallMap<K, V>
 */
fun <K, V> SmallMap<K, V>.allocFrozenValueSmallMapRef(heap: FrozenHeap): FrozenValue
    where K : StarlarkTypeRepr,
          V : StarlarkTypeRepr {
    return AllocDict(this).allocFrozenValue(heap)
}

/**
 * StarlarkTypeRepr for &SmallMap<K, V>.
 * Corresponds to: impl<A_, K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for &'a SmallMap<K, V>
 */
object SmallMapRefStarlarkTypeRepr {
    inline fun <reified K, reified V> starlarkTypeRepr(): Ty
        where K : StarlarkTypeRepr,
              V : StarlarkTypeRepr {
        return DictType.starlarkTypeRepr<K, V>()
    }
}

/**
 * StarlarkTypeRepr for SmallMap<K, V>.
 * Corresponds to: impl<K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for SmallMap<K, V>
 */
object SmallMapStarlarkTypeRepr {
    inline fun <reified K, reified V> starlarkTypeRepr(): Ty
        where K : StarlarkTypeRepr,
              V : StarlarkTypeRepr {
        return DictType.starlarkTypeRepr<K, V>()
    }
}

/**
 * UnpackValue implementation for SmallMap<K, V>.
 * Corresponds to: impl<V_, K: UnpackValue<V_> + Hash + Eq, V: UnpackValue<V_>> UnpackValue<V_> for SmallMap<K, V>
 */
object SmallMapUnpackValue {
    fun <V, K, T> unpackValueImpl(value: Value<V>): Result<SmallMap<K, T>?>
        where K : UnpackValue<V>,
              T : UnpackValue<V> {
        val dict = DictRef.fromValue(value) ?: return Result.success(null)

        val it = dict.iter()
        val r = SmallMap.withCapacity<K, T>(it.len())

        for ((k, v) in it) {
            val unpackedK = K.unpackValueImpl(k).getOrElse { error ->
                return Result.failure(Either.Left(error) as Throwable)
            }

            if (unpackedK == null) {
                return Result.success(null)
            }

            val unpackedV = T.unpackValueImpl(v).getOrElse { error ->
                return Result.failure(Either.Right(error) as Throwable)
            }

            if (unpackedV == null) {
                return Result.success(null)
            }

            r.insert(unpackedK, unpackedV)
        }

        return Result.success(r)
    }
}

// BTreeMap

/**
 * AllocValue implementation for BTreeMap<K, V>.
 * Corresponds to: impl<V_, K: AllocValue<V_>, V: AllocValue<V_>> AllocValue<V_> for BTreeMap<K, V>
 * Note: In Kotlin, BTreeMap is represented by sorted map types.
 */
fun <V, K, T> Map<K, T>.allocValueBTreeMap(heap: Heap<V>): Value<V>
    where K : Comparable<K>,
          K : AllocValue<V>,
          T : AllocValue<V> {
    return AllocDict(this.entries).allocValue(heap)
}

/**
 * AllocFrozenValue implementation for BTreeMap<K, V>.
 * Corresponds to: impl<K: AllocFrozenValue, V: AllocFrozenValue> AllocFrozenValue for BTreeMap<K, V>
 */
fun <K, V> Map<K, V>.allocFrozenValueBTreeMap(heap: FrozenHeap): FrozenValue
    where K : Comparable<K>,
          K : AllocFrozenValue,
          V : AllocFrozenValue {
    return AllocDict(this.entries).allocFrozenValue(heap)
}

/**
 * AllocValue implementation for &BTreeMap<K, V>.
 * Corresponds to: impl<A_, V_, K: 'a + StarlarkTypeRepr, V: 'a + StarlarkTypeRepr> AllocValue<V_> for &'a BTreeMap<K, V>
 */
fun <V, K, T> Map<K, T>.allocValueBTreeMapRef(heap: Heap<V>): Value<V>
    where K : Comparable<K>,
          K : StarlarkTypeRepr,
          T : StarlarkTypeRepr {
    return AllocDict(this.entries).allocValue(heap)
}

/**
 * AllocFrozenValue implementation for &BTreeMap<K, V>.
 * Corresponds to: impl<A_, K: 'a + StarlarkTypeRepr, V: 'a + StarlarkTypeRepr> AllocFrozenValue for &'a BTreeMap<K, V>
 */
fun <K, V> Map<K, V>.allocFrozenValueBTreeMapRef(heap: FrozenHeap): FrozenValue
    where K : Comparable<K>,
          K : StarlarkTypeRepr,
          V : StarlarkTypeRepr {
    return AllocDict(this.entries).allocFrozenValue(heap)
}

/**
 * StarlarkTypeRepr for &BTreeMap<K, V>.
 * Corresponds to: impl<A_, K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for &'a BTreeMap<K, V>
 */
object BTreeMapRefStarlarkTypeRepr {
    inline fun <reified K, reified V> starlarkTypeRepr(): Ty
        where K : Comparable<K>,
              K : StarlarkTypeRepr,
              V : StarlarkTypeRepr {
        return DictType.starlarkTypeRepr<K, V>()
    }
}

/**
 * StarlarkTypeRepr for BTreeMap<K, V>.
 * Corresponds to: impl<K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for BTreeMap<K, V>
 */
object BTreeMapStarlarkTypeRepr {
    inline fun <reified K, reified V> starlarkTypeRepr(): Ty
        where K : Comparable<K>,
              K : StarlarkTypeRepr,
              V : StarlarkTypeRepr {
        return DictType.starlarkTypeRepr<K, V>()
    }
}

/**
 * UnpackValue implementation for BTreeMap<K, V>.
 * Corresponds to: impl<V_, K: UnpackValue<V_> + Ord, V: UnpackValue<V_>> UnpackValue<V_> for BTreeMap<K, V>
 */
object BTreeMapUnpackValue {
    fun <V, K, T> unpackValueImpl(value: Value<V>): Result<MutableMap<K, T>?>
        where K : Comparable<K>,
              K : UnpackValue<V>,
              T : UnpackValue<V> {
        val dict = DictRef.fromValue(value) ?: return Result.success(null)

        val r = sortedMapOf<K, T>()

        for ((k, v) in dict.iter()) {
            val unpackedK = K.unpackValueImpl(k).getOrElse { error ->
                return Result.failure(Either.Left(error) as Throwable)
            }

            if (unpackedK == null) {
                return Result.success(null)
            }

            val unpackedV = T.unpackValueImpl(v).getOrElse { error ->
                return Result.failure(Either.Right(error) as Throwable)
            }

            if (unpackedV == null) {
                return Result.success(null)
            }

            r[unpackedK] = unpackedV
        }

        return Result.success(r)
    }
}
