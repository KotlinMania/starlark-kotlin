// port-lint: source src/values/types/set/methods.rs
package io.github.kotlinmania.starlark.values.types.set

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

/**
 * Methods for the `set` type.
 */

import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.values.ValueError
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.smallset.SmallSet

private sealed class SetFromValue {
    data class Set(val set: SmallSet<Value>) : SetFromValue()
    data class Ref(val ref: SetRef) : SetFromValue()

    companion object {
        fun fromValue(
            value: Value,
            heap: Heap
        ): Result<SetFromValue> {
            return when (val setRef = SetRef.unpackValueOpt(value)) {
                null -> {
                    val set = SmallSet<Value>()
                    for (elem in value.iterate(heap).getOrElse { return Result.failure(it) }) {
                        set.insertHashed(elem.getHashed().getOrElse { return Result.failure(it) })
                    }
                    Result.success(Set(set))
                }
                else -> Result.success(Ref(setRef))
            }
        }
    }

    fun get(): SmallSet<Value> {
        return when (this) {
            is Set -> this.set
            is Ref -> this.ref.content
        }
    }

    fun intoSet(): SmallSet<Value> {
        return when (this) {
            is Set -> this.set
            is Ref -> {
                // Clone: create a new SmallSet with the same entries
                val clone = SmallSet<Value>()
                for (h in this.ref.content.iterHashed()) {
                    clone.insertHashedUniqueUnchecked(h)
                }
                clone
            }
        }
    }

    fun isEmpty(): Boolean {
        return get().isEmpty()
    }

    fun containsHashed(value: Hashed<Value>): Boolean {
        return get().containsHashed(value)
    }
}

/**
 * Register set methods.
 *
 */
internal fun setMethods(builder: MethodsBuilder) {
    // Methods are registered through the MethodsBuilder
}

internal fun clear(thisValue: Value): Result<NoneType> {
    val thisSet = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    thisSet.aref.data.clear()
    return Result.success(NoneType)
}

/**
 * Return a new set with elements from the set and all others.
 * Unlike Python does not support variable number of arguments.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.union(y) == set([1, 2, 3, 4, 5])
 * # "#);
 * ```
 */
internal fun union(
    thisSet: SetRef,
    other: Value,
    heap: Heap
): Result<SetData> {
    if (thisSet.content.isEmpty()) {
        val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }
        val result = SetData()
        result.content.addAll(otherSet.intoSet().iterHashed().asIterable())
        return Result.success(result)
    }
    val result = SetData()
    for (h in thisSet.content.iterHashed()) {
        result.content.insertHashedUniqueUnchecked(h)
    }
    for (elem in other.iterate(heap).getOrElse { return Result.failure(it) }) {
        val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
        result.content.insertHashed(hashed)
    }
    return Result.success(result)
}

/**
 * Return a new set with elements common to the set and all others.
 * Unlike Python does not support variable number of arguments.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.intersection(y) == set([3])
 * # "#);
 * ```
 */
internal fun intersection(
    thisSet: SetRef,
    other: Value,
    heap: Heap
): Result<SetData> {
    val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }
    val data = SetData()
    if (otherSet.isEmpty()) {
        return Result.success(data)
    }

    for (hashed in thisSet.content.iterHashed()) {
        if (otherSet.containsHashed(hashed.copied())) {
            data.content.insertHashedUniqueUnchecked(hashed.copied())
        }
    }
    return Result.success(data)
}

/**
 * Returns a new set with elements in either the set or the specified iterable but not both.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.symmetricDifference(y) == set([1, 2, 4, 5])
 * # "#);
 * ```
 */
internal fun symmetricDifference(
    thisSet: SetRef,
    other: Value,
    heap: Heap
): Result<SetData> {
    val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }

    if (otherSet.isEmpty()) {
        val clone = SetData()
        for (h in thisSet.content.iterHashed()) {
            clone.content.insertHashedUniqueUnchecked(h)
        }
        return Result.success(clone)
    }

    // NOTE(romanp) add symmetricDifference to small set and import it here and in xor
    if (thisSet.content.isEmpty()) {
        val result = SetData()
        result.content.addAll(otherSet.intoSet().iterHashed().asIterable())
        return Result.success(result)
    }

    val data = SetData()
    for (elem in thisSet.content.iterHashed()) {
        if (!otherSet.containsHashed(elem.copied())) {
            data.addHashed(elem.copied())
        }
    }

    for (elem in otherSet.get().iter()) {
        val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
        if (!thisSet.content.containsHashed(hashed.asRef())) {
            data.addHashed(hashed)
        }
    }
    return Result.success(data)
}

/**
 * Add an item to the set.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * x.add(4)
 * x == set([1, 2, 3, 4])
 * # "#);
 * ```
 */
internal fun add(
    thisValue: Value,
    value: Value
): Result<NoneType> {
    val thisSet = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = value.getHashed().getOrElse { return Result.failure(it) }
    thisSet.aref.data.addHashed(hashed)
    return Result.success(NoneType)
}

/**
 * Update the set by adding items from an iterable.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 3, 2])
 * x.update([4, 3])
 * list(x) == [1, 3, 2, 4]
 * # "#);
 * ```
 */
internal fun update(
    thisValue: Value,
    other: Value,
    heap: Heap
): Result<NoneType> {
    val isSelfPtr = other.ptrEq(thisValue)
    val thisSet = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    if (isSelfPtr) {
        return Result.success(NoneType)
    }

    if (thisSet.aref.data.content.isEmpty()) {
        val otherSet = SetFromValue.fromValue(other, heap)
            .getOrElse { return Result.failure(it) }
        val newContent = otherSet.intoSet()
        thisSet.aref.data.content.addAll(newContent.iterHashed().asIterable())
    } else {
        for (elem in other.iterate(heap).getOrElse { return Result.failure(it) }) {
            val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
            thisSet.aref.data.addHashed(hashed)
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
 * # starlark::assert::isTrue(r#"
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
internal fun remove(
    thisValue: Value,
    value: Value
): Result<NoneType> {
    val set = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = value.getHashed().getOrElse { return Result.failure(it) }
    return if (set.aref.data.removeHashed(hashed.asRef())) {
        Result.success(NoneType)
    } else {
        Result.failure(ValueError.KeyNotFound("`$value` not found in `$thisValue`"))
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
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * x.discard(2)
 * x == set([1, 3])
 * # "#)
 * ```
 * A subsequent call to `x.discard(2)` would do nothing.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * x.discard(2)
 * x.discard(2)
 * x == set([1, 3])
 * # "#);
 * ```
 */
internal fun discard(
    thisValue: Value,
    value: Value
): Result<NoneType> {
    val set = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = value.getHashed().getOrElse { return Result.failure(it) }
    set.aref.data.removeHashed(hashed.asRef())
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
 * # starlark::assert::isTrue(r#"
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
internal fun pop(thisValue: Value): Result<Value> {
    val set = SetMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val content = set.aref.data.content
    return if (content.isEmpty()) {
        Result.failure(ValueError.KeyNotFound("pop from an empty set"))
    } else {
        // Pop the last element - get it then remove it
        val last = content.iterHashed().last()
        content.shiftRemoveHashed(last)
        Result.success(last.key())
    }
}

/**
 * Returns a new set with elements unique the set when compared to the specified iterable.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * y = [3, 4, 5]
 * x.difference(y) == set([1, 2])
 * # "#);
 * ```
 */
internal fun difference(
    thisSet: SetRef,
    other: Value,
    heap: Heap
): Result<SetData> {
    if (thisSet.content.isEmpty()) {
        other.iterate(heap).getOrElse { return Result.failure(it) }
        return Result.success(SetData())
    }

    val otherSet = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }

    if (otherSet.isEmpty()) {
        val clone = SetData()
        for (h in thisSet.content.iterHashed()) {
            clone.content.insertHashedUniqueUnchecked(h)
        }
        return Result.success(clone)
    }

    val data = SetData()
    for (elem in thisSet.content.iterHashed()) {
        if (!otherSet.containsHashed(elem.copied())) {
            data.addHashed(elem.copied())
        }
    }
    return Result.success(data)
}

/**
 * Test whether every element other iterable is in the set.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * y = [1, 3]
 * x.issuperset(y) == True
 * # "#);
 * ```
 */
internal fun issuperset(
    thisSet: SetRef,
    other: Value,
    heap: Heap
): Result<Boolean> {
    val otherSetRef = SetRef.unpackValueOpt(other)
    if (otherSetRef != null) {
        if (thisSet.content.len() < otherSetRef.content.len()) {
            return Result.success(false)
        }
        for (hashed in otherSetRef.content.iterHashed()) {
            if (!thisSet.content.containsHashed(hashed.copied())) {
                return Result.success(false)
            }
        }
    } else {
        val iter = other.iterate(heap).getOrElse { return Result.failure(it) }
        for (elem in iter) {
            val hashed = elem.getHashed().getOrElse { return Result.failure(it) }
            if (!thisSet.content.containsHashed(hashed)) {
                return Result.success(false)
            }
        }
    }
    return Result.success(true)
}

/**
 * Test whether every element in the set is in other iterable.
 * ```
 * # starlark::assert::isTrue(r#"
 * x = set([1, 2, 3])
 * y = [3, 1, 2]
 * x.issubset(y)
 * # "#);
 * ```
 */
internal fun issubset(
    thisSet: SetRef,
    other: Value,
    heap: Heap
): Result<Boolean> {
    if (thisSet.content.isEmpty()) {
        other.iterate(heap).getOrElse { return Result.failure(it) }
        return Result.success(true)
    }
    val rhs = SetFromValue.fromValue(other, heap).getOrElse { return Result.failure(it) }
    if (thisSet.content.len() > rhs.get().len()) {
        return Result.success(false)
    }
    for (elem in thisSet.content.iterHashed()) {
        if (!rhs.containsHashed(elem.copied())) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}
