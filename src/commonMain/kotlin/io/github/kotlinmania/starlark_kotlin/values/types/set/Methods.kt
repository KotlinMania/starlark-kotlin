// port-lint: source src/values/types/set/methods.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set

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

/**
 * Methods for the `set` type.
 */

import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.set.refs.SetMut
import io.github.kotlinmania.starlark_kotlin.values.set.refs.SetRef
import io.github.kotlinmania.starlark_kotlin.values.set.value.SetData
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import starlark_map.Hashed
import starlark_map.small_set.SmallSet

private sealed class SetFromValue<V_> {
    data class Set<V_>(val set: SmallSet<Value<V_>>) : SetFromValue<V_>()
    data class Ref<V_>(val ref: SetRef<V_>) : SetFromValue<V_>()

    companion object {
        fun <V_> fromValue(
            value: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
            heap: Heap<V_>
        ): Result<SetFromValue<V_>> {
            return when (val setRef = SetRef.unpackValueOpt(value.get())) {
                null -> {
                    val set = SmallSet.default<Value<V_>>()
                    for (elem in value.get().iterate(heap).getOrElse { return Result.failure(it) }) {
                        set.insertHashed(elem.getHashed().getOrElse { return Result.failure(it) })
                    }
                    Result.success(Set(set))
                }
                else -> Result.success(Ref(setRef))
            }
        }
    }

    fun get(): SmallSet<Value<V_>> {
        return when (this) {
            is Set -> this.set
            is Ref -> this.ref.aref.content
        }
    }

    fun intoSet(): SmallSet<Value<V_>> {
        return when (this) {
            is Set -> this.set
            is Ref -> this.ref.aref.content.clone()
        }
    }

    fun isEmpty(): Boolean {
        return get().isEmpty()
    }

    fun containsHashed(value: Hashed<Value<V_>>): Boolean {
        return get().containsHashed(value.asRef())
    }
}

/**
 * Register set methods.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 */
internal fun setMethods(builder: MethodsBuilder) {
    // Methods are registered through the MethodsBuilder
}

internal fun <V_> clear(thisValue: Value<V_>): Result<NoneType> {
    val thisSet = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    thisSet.aref.clear()
    return Result.success(NoneType)
}

/**
 * Return a new set with elements from the set and all others.
 * Unlike Python does not support variable number of arguments.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.union(y) == set([1, 2, 3, 4, 5])
 * # "#);
 * ```
 */
internal fun <V_> union(
    thisSet: SetRef<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<SetData<V_>> {
    if (thisSet.aref.content.isEmpty()) {
        val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }
        return Result.success(SetData(content = otherSet.intoSet()))
    }
    val data = thisSet.aref.content.clone()
    for (elem in other.get().iterate(heap).getOrElse { return Result.failure(it) }) {
        val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
        data.insertHashed(hashed)
    }
    return Result.success(SetData(content = data))
}

/**
 * Return a new set with elements common to the set and all others.
 * Unlike Python does not support variable number of arguments.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.intersection(y) == set([3])
 * # "#);
 * ```
 */
internal fun <V_> intersection(
    thisSet: SetRef<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<SetData<V_>> {
    val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }
    val data = SetData.default<V_>()
    if (otherSet.isEmpty()) {
        return Result.success(data)
    }

    for (hashed in thisSet.aref.content.iterHashed()) {
        if (otherSet.containsHashed(hashed.copied())) {
            data.content.insertHashedUniqueUnchecked(hashed.copied())
        }
    }
    return Result.success(data)
}

/**
 * Returns a new set with elements in either the set or the specified iterable but not both.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.symmetric_difference(y) == set([1, 2, 4, 5])
 * # "#);
 * ```
 */
internal fun <V_> symmetricDifference(
    thisSet: SetRef<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<SetData<V_>> {
    val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }

    if (otherSet.isEmpty()) {
        return Result.success(SetData(content = thisSet.aref.content.clone()))
    }

    // TODO(romanp) add symmetric_difference to small set and use it here and in xor
    if (thisSet.aref.content.isEmpty()) {
        return Result.success(SetData(content = otherSet.intoSet()))
    }

    val data = SetData.default<V_>()
    for (elem in thisSet.aref.content.iterHashed()) {
        if (!otherSet.containsHashed(elem.copied())) {
            data.addHashed(elem.copied())
        }
    }

    for (elem in otherSet.get()) {
        val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
        if (!thisSet.aref.content.containsHashed(hashed.asRef())) {
            data.addHashed(hashed)
        }
    }
    return Result.success(data)
}

/**
 * Add an item to the set.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * x.add(4)
 * x == set([1, 2, 3, 4])
 * # "#);
 * ```
 */
internal fun <V_> add(
    thisValue: Value<V_>,
    value: Value<V_>
): Result<NoneType> {
    val thisSet = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = value.getHashed().getOrElse { return Result.failure(it) }
    thisSet.aref.addHashed(hashed)
    return Result.success(NoneType)
}

/**
 * Update the set by adding items from an iterable.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 3, 2])
 * x.update([4, 3])
 * list(x) == [1, 3, 2, 4]
 * # "#);
 * ```
 */
internal fun <V_> update(
    thisValue: Value<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<NoneType> {
    val isSelfPtr = other.get().ptrEq(thisValue)
    val thisSet = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    if (isSelfPtr) {
        return Result.success(NoneType)
    }

    if (thisSet.aref.content.isEmpty()) {
        thisSet.aref.content = SetFromValue.fromValue(other, heap)
            .getOrElse { return Result.failure(it) }
            .intoSet()
    } else {
        for (elem in other.get().iterate(heap).getOrElse { return Result.failure(it) }) {
            val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
            thisSet.aref.addHashed(hashed)
        }
    }

    return Result.success(NoneType)
}

/**
 * Remove the item from the set. It raises an error if there is no such item.
 *
 * `remove` fails if the key is unhashable or if the dictionary is
 * frozen.
 * Time complexity of this operation is *O(N)* where *N* is the number of entries in the set.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * x.remove(2)
 * x == set([1, 3])
 * # "#)
 * ```
 * A subsequent call to `x.remove(2)` would yield an error because the
 * element won't be found.
 * ```
 * # starlark::assert::fail(r#"
 * x = set([1, 2, 3])
 * x.remove(2)
 * x.remove(2) # error: not found
 * # "#, "not found");
 * ```
 */
internal fun <V_> remove(
    thisValue: Value<V_>,
    value: Value<V_>
): Result<NoneType> {
    val set = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = value.getHashed().getOrElse { return Result.failure(it) }
    return if (set.aref.removeHashed(hashed.asRef())) {
        Result.success(NoneType)
    } else {
        Result.failure(ValueError("`$value` not found in `$thisValue`"))
    }
}

/**
 * Remove the item from the set. It does nothing if there is no such item.
 *
 * `discard` fails if the key is unhashable or if the dictionary is
 * frozen.
 * Time complexity of this operation is *O(N)* where *N* is the number of entries in the set.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * x.discard(2)
 * x == set([1, 3])
 * # "#)
 * ```
 * A subsequent call to `x.discard(2)` would do nothing.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * x.discard(2)
 * x.discard(2)
 * x == set([1, 3])
 * # "#);
 * ```
 */
internal fun <V_> discard(
    thisValue: Value<V_>,
    value: Value<V_>
): Result<NoneType> {
    val set = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = value.getHashed().getOrElse { return Result.failure(it) }
    set.aref.removeHashed(hashed.asRef())
    return Result.success(NoneType)
}

/**
 * Removes and returns the **last** element of a set.
 *
 * `S.pop()` removes and returns the last element of the set S.
 *
 * `pop` fails if the set is empty, or if the set is frozen or has active iterators.
 * Time complexity of this operation is *O(1)*.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * # (
 * x.pop() == 3
 * # and
 * x.pop() == 2
 * # and
 * x == set([1])
 * # )"#);
 * ```
 */
internal fun <V_> pop(thisValue: Value<V_>): Result<Value<V_>> {
    val set = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    return when (val x = set.aref.content.pop()) {
        null -> Result.failure(ValueError("pop from an empty set"))
        else -> Result.success(x)
    }
}

/**
 * Returns a new set with elements unique the set when compared to the specified iterable.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.difference(y) == set([1, 2])
 * # "#);
 * ```
 */
internal fun <V_> difference(
    thisSet: SetRef<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<SetData<V_>> {
    if (thisSet.aref.content.isEmpty()) {
        other.get().iterate(heap).getOrElse { return Result.failure(it) }
        return Result.success(SetData.default())
    }

    val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }

    if (otherSet.isEmpty()) {
        return Result.success(SetData(content = thisSet.aref.content.clone()))
    }

    val data = SetData.default<V_>()
    for (elem in thisSet.aref.content.iterHashed()) {
        if (!otherSet.containsHashed(elem.copied())) {
            data.addHashed(elem.copied())
        }
    }
    return Result.success(data)
}

/**
 * Test whether every element other iterable is in the set.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * y = [1, 3]
 * x.issuperset(y) == True
 * # "#);
 * ```
 */
internal fun <V_> issuperset(
    thisSet: SetRef<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<Boolean> {
    val otherVar: SetRef<V_>?
    val otherIter = when (val setRef = SetRef.unpackValueOpt(other.get())) {
        null -> {
            other.get().iterate(heap)
                .getOrElse { return Result.failure(it) }
                .map { it.getHashed() }
        }
        else -> {
            if (thisSet.aref.content.size < setRef.aref.content.size) {
                return Result.success(false)
            }
            otherVar = setRef
            otherVar.aref.content.iterHashed().map { Result.success(it.copied()) }
        }
    }

    for (elem in otherIter) {
        val hashed = elem.getOrElse { return Result.failure(it) }
        if (!thisSet.aref.containsHashed(hashed)) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

/**
 * Test whether every element in the set is in other iterable.
 * ```
 * # starlark::assert::is_true(r#"
 * x = set([1, 2, 3])
 * y = [3, 1, 2]
 * x.issubset(y)
 * # "#);
 * ```
 */
internal fun <V_> issubset(
    thisSet: SetRef<V_>,
    other: ValueOfUnchecked<V_, StarlarkIter<Value<V_>>>,
    heap: Heap<V_>
): Result<Boolean> {
    if (thisSet.aref.content.isEmpty()) {
        other.get().iterate(heap).getOrElse { return Result.failure(it) }
        return Result.success(true)
    }
    val rhs = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }
    if (thisSet.aref.content.size > rhs.get().size) {
        return Result.success(false)
    }
    for (elem in thisSet.aref.content.iterHashed()) {
        if (!rhs.containsHashed(elem.copied())) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}
