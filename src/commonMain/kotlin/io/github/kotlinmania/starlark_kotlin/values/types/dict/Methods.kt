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

/** Methods for the `dict` type. */

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackList
import io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

internal fun dictMethods(registry: MethodsBuilder) {
    registry.method("clear") { thisValue: Value -> clear(thisValue) }
    registry.method("get") { thisRef: DictRef, key: Value, default: Value? -> get(thisRef, key, default) }
    registry.method("items") { thisRef: DictRef, heap: Heap -> items(thisRef, heap) }
    registry.method("keys") { thisRef: DictRef, heap: Heap -> keys(thisRef, heap) }
    registry.method("pop") { thisValue: Value, key: Value, default: Value? -> pop(thisValue, key, default) }
    registry.method("popitem") { thisValue: Value -> popitem(thisValue) }
    registry.method("setdefault") { thisValue: Value, key: Value, default: Value? -> setdefault(thisValue, key, default) }
    registry.method("update") { thisValue: Value, pairs: ValueOfUnchecked<Either<DictRef, StarlarkIter<Pair<Value, Value>>>>?, kwargs: DictRef, heap: Heap -> update(thisValue, pairs, kwargs, heap) }
    registry.method("values") { thisRef: DictRef, heap: Heap -> values(thisRef, heap) }
}

/**
 * [dict.clear](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-clear):
 * clear a dictionary.
 *
 * `D.clear()` removes all the entries of dictionary D and returns `None`.
 * It fails if the dictionary is frozen or if there are active iterators.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.clear()
 * x == {}
 * ```
 */
internal fun clear(thisValue: Value): Result<NoneType> {
    val mut = dictMutFromValue(thisValue).getOrElse { return Result.failure(it) }
    mut.aref.value.clear()
    return Result.success(NoneType)
}

/**
 * [dict.get](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-get):
 * return an element from the dictionary.
 *
 * `D.get(key[, default])` returns the dictionary value corresponding to
 * the given key. If the dictionary contains no such value, `get`
 * returns `None`, or the value of the optional `default` parameter if
 * present.
 *
 * `get` fails if `key` is unhashable.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.get("one") == 1
 * x.get("three") == None
 * x.get("three", 0) == 0
 * ```
 */
internal fun get(
    thisRef: DictRef,
    key: Value,
    default: Value? = null
): Result<Value> {
    val dict = thisRef.deref()
    return when (val result = dict.get(key).getOrElse { return Result.failure(it) }) {
        null -> Result.success(default ?: Value.newNone())
        else -> Result.success(result)
    }
}

/**
 * [dict.items](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-items):
 * get list of (key, value) pairs.
 *
 * `D.items()` returns a new list of key/value pairs, one per element in
 * dictionary D, in the same order as they would be returned by a `for` loop.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.items() == [("one", 1), ("two", 2)]
 * ```
 */
internal fun items(
    thisRef: DictRef,
    heap: Heap
): Result<ValueOfUnchecked<UnpackList<Pair<Value, Value>>>> =
    Result.success(heap.allocTypedUnchecked(AllocList(thisRef.deref().iter())).cast())

/**
 * [dict.keys](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-keys):
 * get the list of keys of the dictionary.
 *
 * `D.keys()` returns a new list containing the keys of dictionary D, in
 * the same order as they would be returned by a `for` loop.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.keys() == ["one", "two"]
 * ```
 */
internal fun keys(
    thisRef: DictRef,
    heap: Heap
): Result<ValueOfUnchecked<ListRef>> =
    Result.success(ValueOfUnchecked.new(heap.alloc(AllocList(thisRef.deref().keys()))))

/**
 * [dict.pop](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-pop):
 * return an element and remove it from a dictionary.
 *
 * `D.pop(key[, default])` returns the value corresponding to the specified
 * key, and removes it from the dictionary. If the dictionary contains no
 * such value, and the optional `default` parameter is present, `pop`
 * returns that value; otherwise, it fails.
 *
 * `pop` fails if `key` is unhashable, or the dictionary is frozen or has
 * active iterators.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.pop("one") == 1
 * x == {"two": 2}
 * x.pop("three", 0) == 0
 * x.pop("three", None) == None
 * ```
 *
 * Failure:
 * ```
 * {'one': 1}.pop('four')   # error: not found
 * ```
 */
internal fun pop(
    thisValue: Value,
    key: Value,
    default: Value? = null
): Result<Value> {
    val me = dictMutFromValue(thisValue).getOrElse { return Result.failure(it) }
    val hashed = key.getHashed().getOrElse { return Result.failure(it) }
    return when (val x = me.aref.value.removeHashed(hashed)) {
        null -> when (default) {
            null -> Result.failure(
                IllegalArgumentException("Key `${key.toRepr()}` not found in dictionary `${thisValue.toRepr()}`")
            )
            else -> Result.success(default)
        }
        else -> Result.success(x)
    }
}

/**
 * [dict.popitem](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-popitem):
 * returns and removes the first key/value pair of a dictionary.
 *
 * `D.popitem()` returns the first key/value pair, removing it from the dictionary.
 *
 * `popitem` fails if the dictionary is empty, frozen, or has active iterators.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.popitem() == ("one", 1)
 * x.popitem() == ("two", 2)
 * x == {}
 * ```
 *
 * Failure:
 * ```
 * {}.popitem()   # error: empty dict
 * ```
 */
internal fun popitem(thisValue: Value): Result<Pair<Value, Value>> {
    val me = dictMutFromValue(thisValue).getOrElse { return Result.failure(it) }
    // This implementation is O(N).
    return when (val result = me.aref.value.content.shiftRemoveIndex(0)) {
        null -> Result.failure(IllegalArgumentException("Cannot .popitem() on an empty dictionary"))
        else -> Result.success(result)
    }
}

/**
 * [dict.setdefault](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-setdefault):
 * get a value from a dictionary, setting it to a new value if not present.
 *
 * `D.setdefault(key[, default])` returns the dictionary value
 * corresponding to the given key. If the dictionary contains no such
 * value, `setdefault`, like `get`, returns `None` or the value of the
 * optional `default` parameter if present; `setdefault` additionally
 * inserts the new key/value entry into the dictionary.
 *
 * `setdefault` fails if the key is unhashable or if the dictionary is frozen.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.setdefault("one") == 1
 * x.setdefault("three", 0) == 0
 * x == {"one": 1, "two": 2, "three": 0}
 * x.setdefault("four") == None
 * x == {"one": 1, "two": 2, "three": 0, "four": None}
 * ```
 */
internal fun setdefault(
    thisValue: Value,
    key: Value,
    default: Value? = null
): Result<Value> {
    val me = dictMutFromValue(thisValue).getOrElse { return Result.failure(it) }
    val keyHashed = key.getHashed().getOrElse { return Result.failure(it) }
    return when (val entry = me.aref.value.content.entryHashed(keyHashed)) {
        is SmallMap.Entry.Occupied -> Result.success(entry.get())
        is SmallMap.Entry.Vacant -> {
            val d = default ?: Value.newNone()
            entry.insert(d)
            Result.success(d)
        }
    }
}

/**
 * [dict.update](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-update):
 * update values in the dictionary.
 *
 * `D.update([pairs][, name=value[, ...])` makes a sequence of key/value
 * insertions into dictionary D, then returns `None.`
 *
 * If the positional argument `pairs` is present, it must be `dict`, or
 * some other iterable. If it is another `dict`, then its key/value pairs
 * are inserted into D. If it is an iterable, it must provide a sequence
 * of pairs (or other iterables of length 2), each of which is treated as
 * a key/value pair to be inserted into D.
 *
 * For each `name=value` argument present, the name is converted to a
 * string and used as the key for an insertion into D, with its
 * corresponding value being `value`.
 *
 * `update` fails if the dictionary is frozen.
 *
 * ```
 * x = {}
 * x.update([("a", 1), ("b", 2)], c=3)
 * x.update({"d": 4})
 * x.update(e=5)
 * x == {"a": 1, "b": 2, "c": 3, "d": 4, "e": 5}
 * ```
 */
internal fun update(
    thisValue: Value,
    pairs: ValueOfUnchecked<Either<DictRef, StarlarkIter<Pair<Value, Value>>>>? = null,
    kwargs: DictRef,
    heap: Heap
): Result<NoneType> {
    val pairsValue = if (pairs?.get()?.ptrEq(thisValue) == true) {
        // someone has done `x.update(x)` - that isn't illegal, but we will have issues
        // with trying to iterate over x while holding x for mutation, and it doesn't do
        // anything useful, so just change pairs back to null
        null
    } else {
        pairs?.get()
    }

    val me = dictMutFromValue(thisValue).getOrElse { return Result.failure(it) }
    if (pairsValue != null) {
        val dictRef = dictRefFromValue(pairsValue)
        if (dictRef != null) {
            for ((k, v) in dictRef.deref().iterHashed()) {
                me.aref.value.insertHashed(k, v)
            }
        } else {
            for (v in pairsValue.iterate(heap).getOrElse { return Result.failure(it) }) {
                val it = v.iterate(heap).getOrElse { return Result.failure(it) }
                // StarlarkIterator is fused.
                val k = it.next()
                val v2 = it.next()
                val end = it.next()
                if (k == null || v2 == null || end != null) {
                    return Result.failure(IllegalArgumentException(
                        "dict.update expect a list of pairs or a dictionary as first argument, got a list of non-pairs."
                    ))
                }
                me.aref.value.insertHashed(k.getHashed().getOrElse { return Result.failure(it) }, v2)
            }
        }
    }

    for ((k, v) in kwargs.deref().iterHashed()) {
        me.aref.value.insertHashed(k, v)
    }
    return Result.success(NoneType)
}

/**
 * [dict.values](https://github.com/bazelbuild/starlark/blob/master/spec.md#dict-values):
 * get the list of values of the dictionary.
 *
 * `D.values()` returns a new list containing the dictionary's values, in
 * the same order as they would be returned by a `for` loop over the
 * dictionary.
 *
 * ```
 * x = {"one": 1, "two": 2}
 * x.values() == [1, 2]
 * ```
 */
internal fun values(
    thisRef: DictRef,
    heap: Heap
): Result<ValueOfUnchecked<ListRef>> =
    Result.success(ValueOfUnchecked.new(heap.allocListIter(thisRef.deref().values())))

private fun DictRef.deref(): Dict = when (val ref = aref) {
    is Either.Left -> ref.value.value
    is Either.Right -> ref.value
}
