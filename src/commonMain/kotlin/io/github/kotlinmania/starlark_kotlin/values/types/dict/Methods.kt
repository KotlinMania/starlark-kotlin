// port-lint: source src/values/types/dict/methods.rs
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

/**
 * Methods for the `dict` type.
 */

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackList
import io.github.kotlinmania.starlark_kotlin.values.types.list.NoneType
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef
import io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.Entry
import io.github.kotlinmania.starlark_kotlin.values.types.set.aref
import io.github.kotlinmania.starlark_kotlin.values.types.list.ptrEq
import io.github.kotlinmania.starlark_kotlin.values.iterate
import io.github.kotlinmania.starlark_kotlin.fromValue
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.string.toRepr
import io.github.kotlinmania.starlark_kotlin.values.types.set.iterHashed
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.values
import io.github.kotlinmania.starlark_kotlin.values.next
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark_kotlin.analysis.keys
import io.github.kotlinmania.starlark_kotlin.values.types.record.values

/**
 * Register methods for the `dict` type.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 */
internal fun dictMethods(registry: MethodsBuilder) {
    registry.registerMethod(
        name = "clear",
        receiver = Value::class,
        implementation = ::clear
    )

    registry.registerMethod(
        name = "get",
        receiver = DictRef::class,
        speculativeExecSafe = true,
        implementation = ::get
    )

    registry.registerMethod(
        name = "items",
        receiver = DictRef::class,
        implementation = ::items
    )

    registry.registerMethod(
        name = "keys",
        receiver = DictRef::class,
        speculativeExecSafe = true,
        implementation = ::keys
    )

    registry.registerMethod(
        name = "pop",
        receiver = Value::class,
        implementation = ::pop
    )

    registry.registerMethod(
        name = "popitem",
        receiver = Value::class,
        implementation = ::popitem
    )

    registry.registerMethod(
        name = "setdefault",
        receiver = Value::class,
        implementation = ::setdefault
    )

    registry.registerMethod(
        name = "update",
        receiver = Value::class,
        implementation = ::update
    )

    registry.registerMethod(
        name = "values",
        receiver = DictRef::class,
        speculativeExecSafe = true,
        implementation = ::values
    )
}

/**
 * [dict.clear](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·clear
 * ): clear a dictionary
 *
 * `D.clear()` removes all the entries of dictionary D and returns `None`.
 * It fails if the dictionary is frozen or if there are active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * x.clear()
 * x == {}
 * # "#);
 * ```
 */
internal fun <V> clear(thisValue: Value<V>): Result<NoneType> {
    val mut = DictMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    mut.aref.clear()
    return Result.success(NoneType)
}

/**
 * [dict.get](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·get
 * ): return an element from the dictionary.
 *
 * `D.get(key[, default])` returns the dictionary value corresponding to
 * the given key. If the dictionary contains no such value, `get`
 * returns `None`, or the value of the optional `default` parameter if
 * present.
 *
 * `get` fails if `key` is unhashable.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * # (
 * x.get("one") == 1
 * # and
 * x.get("three") == None
 * # and
 * x.get("three", 0) == 0
 * # )"#);
 * ```
 */
internal fun <V> get(
    thisRef: DictRef<V>,
    key: Value<V>,
    default: Value<V>? = null
): Result<Value<V>> {
    return when (val result = thisRef.get(key).getOrElse { return Result.failure(it) }) {
        null -> Result.success(default ?: Value.newNone())
        else -> Result.success(result)
    }
}

/**
 * [dict.items](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·items
 * ): get list of (key, value) pairs.
 *
 * `D.items()` returns a new list of key/value pairs, one per element in
 * dictionary D, in the same order as they would be returned by a `for`
 * loop.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * x.items() == [("one", 1), ("two", 2)]
 * # "#);
 * ```
 */
internal fun <V> items(
    thisRef: DictRef<V>,
    heap: Heap<V>
): Result<ValueOfUnchecked<V, UnpackList<Pair<Value<V>, Value<V>>>>> {
    return Result.success(
        heap.allocTypedUnchecked(AllocList(thisRef.iter())).cast()
    )
}

/**
 * [dict.keys](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·keys
 * ): get the list of keys of the dictionary.
 *
 * `D.keys()` returns a new list containing the keys of dictionary D, in
 * the same order as they would be returned by a `for` loop.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * x.keys() == ["one", "two"]
 * # "#);
 * ```
 */
internal fun <V> keys(
    thisRef: DictRef<V>,
    heap: Heap<V>
): Result<ValueOfUnchecked<V, ListRef<V>>> {
    return Result.success(
        ValueOfUnchecked.new(io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList(thisRef.keys()).allocValue(heap))
    )
}

/**
 * [dict.pop](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·pop
 * ): return an element and remove it from a dictionary.
 *
 * `D.pop(key[, default])` returns the value corresponding to the specified
 * key, and removes it from the dictionary.  If the dictionary contains no
 * such value, and the optional `default` parameter is present, `pop`
 * returns that value; otherwise, it fails.
 *
 * `pop` fails if `key` is unhashable, or the dictionary is frozen or has
 * active iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * # (
 * x.pop("one") == 1
 * # and
 * x == {"two": 2}
 * # and
 * x.pop("three", 0) == 0
 * # and
 * x.pop("three", None) == None
 * # )"#);
 * ```
 *
 * Failure:
 *
 * ```
 * # starlark::assert::fail(r#"
 * {'one': 1}.pop('four')   # error: not found
 * # "#, "not found");
 * ```
 */
internal fun <V> pop(
    thisValue: Value<V>,
    key: Value<V>,
    default: Value<V>? = null
): Result<Value<V>> {
    val mut = DictMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    return when (val result = mut.aref.removeHashed(key.getHashed().getOrElse { return Result.failure(it) })) {
        null -> {
            when (default) {
                null -> {
                    Result.failure(
                        IllegalArgumentException(
                            "Key `${key.toRepr()}` not found in dictionary `${thisValue.toRepr()}`"
                        )
                    )
                }
                else -> Result.success(default)
            }
        }
        else -> Result.success(result)
    }
}

/**
 * [dict.popitem](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·popitem
 * ): returns and removes the first key/value pair of a dictionary.
 *
 * `D.popitem()` returns the first key/value pair, removing it from the
 * dictionary.
 *
 * `popitem` fails if the dictionary is empty, frozen, or has active
 * iterators.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * # (
 * x.popitem() == ("one", 1)
 * # and
 * x.popitem() == ("two", 2)
 * # and
 * x == {}
 * # )"#);
 * ```
 *
 * Failure:
 *
 * ```
 * # starlark::assert::fail(r#"
 * {}.popitem()   # error: empty dict
 * # "#, "empty dict");
 * ```
 */
internal fun <V> popitem(thisValue: Value<V>): Result<Pair<Value<V>, Value<V>>> {
    val mut = DictMut.fromValue(thisValue).getOrElse { return Result.failure(it) }

    // This implementation is O(N).
    // https://github.com/bazelbuild/starlark/issues/286

    return when (val result = mut.aref.content.shiftRemoveIndex(0)) {
        null -> Result.failure(IllegalArgumentException("Cannot .popitem() on an empty dictionary"))
        else -> Result.success(result)
    }
}

/**
 * [dict.setdefault](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·setdefault
 * ): get a value from a dictionary, setting it to a new value if not
 * present.
 *
 * `D.setdefault(key[, default])` returns the dictionary value
 * corresponding to the given key. If the dictionary contains no such
 * value, `setdefault`, like `get`, returns `None` or the value of the
 * optional `default` parameter if present; `setdefault` additionally
 * inserts the new key/value entry into the dictionary.
 *
 * `setdefault` fails if the key is unhashable or if the dictionary is
 * frozen.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * # (
 * x.setdefault("one") == 1
 * # and
 * x.setdefault("three", 0) == 0
 * # and
 * x == {"one": 1, "two": 2, "three": 0}
 * # and
 * x.setdefault("four") == None
 * # and
 * x == {"one": 1, "two": 2, "three": 0, "four": None}
 * # )"#)
 * ```
 */
internal fun <V> setdefault(
    thisValue: Value<V>,
    key: Value<V>,
    default: Value<V>? = null
): Result<Value<V>> {
    val mut = DictMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    val keyHashed = key.getHashed().getOrElse { return Result.failure(it) }
    return when (val entry = mut.aref.content.entryHashed(keyHashed)) {
        is SmallMap.Entry.Occupied -> Result.success(entry.get())
        is SmallMap.Entry.Vacant -> {
            val defaultValue = default ?: Value.newNone()
            entry.insert(defaultValue)
            Result.success(defaultValue)
        }
    }
}

/**
 * [dict.update](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·update
 * ): update values in the dictionary.
 *
 * `D.update([pairs][, name=value[, ...])` makes a sequence of key/value
 * insertions into dictionary D, then returns `None.`
 *
 * If the positional argument `pairs` is present, it must be
 * another `dict`, or some other iterable.
 * If it is another `dict`, then its key/value pairs are inserted into D.
 * If it is an iterable, it must provide a sequence of pairs (or other
 * iterables of length 2), each of which is treated as a key/value pair
 * to be inserted into D.
 *
 * For each `name=value` argument present, the name is converted to a
 * string and used as the key for an insertion into D, with its
 * corresponding value being `value`.
 *
 * `update` fails if the dictionary is frozen.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {}
 * x.update([("a", 1), ("b", 2)], c=3)
 * x.update({"d": 4})
 * x.update(e=5)
 * x == {"a": 1, "b": 2, "c": 3, "d": 4, "e": 5}
 * # "#);
 * ```
 */
internal fun <V> update(
    thisValue: Value<V>,
    pairs: ValueOfUnchecked<V, Either<DictRef<V>, StarlarkIter<Pair<Value<V>, Value<V>>>>>? = null,
    kwargs: DictRef<V>,
    heap: Heap<V>
): Result<NoneType> {
    val pairsValue = if (pairs?.get()?.ptrEq(thisValue) == true) {
        // someone has done `x.update(x)` - that isn't illegal, but we will have issues
        // with trying to iterate over x while holding x for mutation, and it doesn't do
        // anything useful, so just change pairs back to null
        null
    } else {
        pairs?.get()
    }

    val mut = DictMut.fromValue(thisValue).getOrElse { return Result.failure(it) }
    if (pairsValue != null) {
        when (val dictRef = DictRef.fromValue(pairsValue)) {
            null -> {
                for (v in pairsValue.iterate(heap).getOrElse { return Result.failure(it) }) {
                    val it = v.iterate(heap).getOrElse { return Result.failure(it) }
                    // `StarlarkIterator` is fused.
                    val k = it.next()
                    val v2 = it.next()
                    val end = it.next()
                    if (k == null || v2 == null || end != null) {
                        return Result.failure(
                            IllegalArgumentException(
                                "dict.update expect a list of pairs or a dictionary as first argument, got a list of non-pairs."
                            )
                        )
                    }
                    mut.aref.insertHashed(k.getHashed().getOrElse { return Result.failure(it) }, v2)
                }
            }
            else -> {
                for ((k, v) in dictRef.iterHashed()) {
                    mut.aref.insertHashed(k, v)
                }
            }
        }
    }

    for ((k, v) in kwargs.iterHashed()) {
        mut.aref.insertHashed(k, v)
    }
    return Result.success(NoneType)
}

/**
 * [dict.values](
 * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict·values
 * ): get the list of values of the dictionary.
 *
 * `D.values()` returns a new list containing the dictionary's values, in
 * the same order as they would be returned by a `for` loop over the
 * dictionary.
 *
 * ```
 * # starlark::assert::is_true(r#"
 * x = {"one": 1, "two": 2}
 * x.values() == [1, 2]
 * # "#);
 * ```
 */
internal fun <V> values(
    thisRef: DictRef<V>,
    heap: Heap<V>
): Result<ValueOfUnchecked<V, ListRef<V>>> {
    return Result.success(
        ValueOfUnchecked.new(heap.allocListIter(thisRef.values()))
    )
}

// Note: Tests would be ported to the commonTest directory following Kotlin Multiplatform conventions
// The original Rust tests from methods.rs would go in:
// src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/dict/MethodsTest.kt
