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

import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableComponents
import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableParamSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple

internal fun dictMethods(registry: MethodsBuilder) {
    val components = NativeCallableComponents(
        speculativeExecSafe = false,
        rustDocstring = null,
        paramSpec = NativeCallableParamSpec.forArguments(),
        returnType = Ty.any(),
    )

    fun setMethod(
        name: String,
        sig: ParametersSpec<FrozenValue>,
        f: (Evaluator, Value, Arguments) -> Result<Value>,
    ) {
        registry.setMethod(
            name = name,
            components = components,
            sig = sig,
            f = { eval, thisValue, _, args -> f(eval, thisValue, args) },
        )
    }

    registry.setDocstring("Methods for the `dict` type.")

    // fn clear(this: Value) -> Result<NoneType>
    setMethod(
        "clear",
        ParametersSpec.newParts(
            functionName = "clear",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { _, thisValue, _ ->
        clear(thisValue).map { Value.newNone() }
    }

    // fn get(this: DictRef, key: Value, default: Option<Value>) -> Result<Value>
    setMethod(
        "get",
        ParametersSpec.newParts(
            functionName = "get",
            posOnly = listOf(
                Pair("key", ParametersSpecParam.Required),
                Pair("default", ParametersSpecParam.Optional),
            ),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { _, thisValue, args ->
        val thisRef = dictRefFromValue(thisValue) ?: return@setMethod Result.failure(
            IllegalArgumentException("Value is not a dict")
        )
        val key = args.positional<Value>(0)
        val defaultValue = args.optionalPositional<Value>(1)
        val default = if (defaultValue == null || defaultValue.isNone()) null else defaultValue
        get(thisRef, key, default)
    }

    // fn items(this: DictRef, heap: Heap) -> Result<Value>
    setMethod(
        "items",
        ParametersSpec.newParts(
            functionName = "items",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { eval, thisValue, _ ->
        val thisRef = dictRefFromValue(thisValue) ?: return@setMethod Result.failure(
            IllegalArgumentException("Value is not a dict")
        )
        items(thisRef, eval.heap())
    }

    // fn keys(this: DictRef, heap: Heap) -> Result<Value>
    setMethod(
        "keys",
        ParametersSpec.newParts(
            functionName = "keys",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { eval, thisValue, _ ->
        val thisRef = dictRefFromValue(thisValue) ?: return@setMethod Result.failure(
            IllegalArgumentException("Value is not a dict")
        )
        keys(thisRef, eval.heap())
    }

    // fn pop(this: Value, key: Value, default: Option<Value>) -> Result<Value>
    setMethod(
        "pop",
        ParametersSpec.newParts(
            functionName = "pop",
            posOnly = listOf(
                Pair("key", ParametersSpecParam.Required),
                Pair("default", ParametersSpecParam.Optional),
            ),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { _, thisValue, args ->
        val key = args.positional<Value>(0)
        val defaultValue = args.optionalPositional<Value>(1)
        val default = if (defaultValue == null || defaultValue.isNone()) null else defaultValue
        pop(thisValue, key, default)
    }

    // fn popitem(this: Value) -> Result<(Value, Value)>
    setMethod(
        "popitem",
        ParametersSpec.newParts(
            functionName = "popitem",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { eval, thisValue, _ ->
        popitem(thisValue).map { (k, v) ->
            eval.heap().allocTuple(listOf(k, v))
        }
    }

    // fn setdefault(this: Value, key: Value, default: Option<Value>) -> Result<Value>
    setMethod(
        "setdefault",
        ParametersSpec.newParts(
            functionName = "setdefault",
            posOnly = listOf(
                Pair("key", ParametersSpecParam.Required),
                Pair("default", ParametersSpecParam.Optional),
            ),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { _, thisValue, args ->
        val key = args.positional<Value>(0)
        val defaultValue = args.optionalPositional<Value>(1)
        val default = if (defaultValue == null || defaultValue.isNone()) null else defaultValue
        setdefault(thisValue, key, default)
    }

    // fn update(this: Value, pairs: Option<Value>, **kwargs) -> Result<NoneType>
    setMethod(
        "update",
        ParametersSpec.newParts(
            functionName = "update",
            posOnly = listOf(Pair("pairs", ParametersSpecParam.Optional)),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = true,
        ),
    ) { eval, thisValue, args ->
        val pairsValue = args.optionalPositional<Value>(0)?.takeIf { !it.isNone() }
        val kwargsRef = args.unpackKwargs()
            .getOrElse { return@setMethod Result.failure(it) }
            ?: dictRefFromValue(FrozenValue.newEmptyDict().toValue())
            ?: return@setMethod Result.failure(IllegalStateException("Failed to construct empty kwargs dict"))
        update(thisValue, pairsValue, kwargsRef, eval.heap()).map { Value.newNone() }
    }

    // fn values(this: DictRef, heap: Heap) -> Result<Value>
    setMethod(
        "values",
        ParametersSpec.newParts(
            functionName = "values",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
    ) { eval, thisValue, _ ->
        val thisRef = dictRefFromValue(thisValue) ?: return@setMethod Result.failure(
            IllegalArgumentException("Value is not a dict")
        )
        values(thisRef, eval.heap())
    }
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
    val dict = derefDict(thisRef)
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
): Result<Value> {
    val tuples = derefDict(thisRef).iter().map { (k, v) ->
        heap.allocTuple(listOf(k, v))
    }.toList()
    return Result.success(heap.allocListIter(tuples))
}

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
): Result<Value> =
    Result.success(heap.allocListIter(derefDict(thisRef).keys().toList()))

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
    val content = me.aref.value.content
    if (content.isEmpty()) {
        return Result.failure(IllegalArgumentException("Cannot .popitem() on an empty dictionary"))
    }
    val first = content.getIndex(0)!!
    content.entries.removeAt(0)
    return Result.success(first)
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
    val existing = me.aref.value.content.getHashedByValue(keyHashed)
    return if (existing != null) {
        Result.success(existing)
    } else {
        val d = default ?: Value.newNone()
        me.aref.value.content.insertHashed(keyHashed, d)
        Result.success(d)
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
    pairs: Value? = null,
    kwargs: DictRef,
    heap: Heap
): Result<NoneType> {
    val pairsValue = if (pairs?.ptrEq(thisValue) == true) {
        // someone has done `x.update(x)` - that isn't illegal, but we will have issues
        // with trying to iterate over x while holding x for mutation, and it doesn't do
        // anything useful, so just change pairs back to null
        null
    } else {
        pairs
    }

    val me = dictMutFromValue(thisValue).getOrElse { return Result.failure(it) }
    if (pairsValue != null) {
        val dictRef = dictRefFromValue(pairsValue)
        if (dictRef != null) {
            for ((k, v) in derefDict(dictRef).iterHashed()) {
                me.aref.value.insertHashed(k, v)
            }
        } else {
            for (v in pairsValue.iterate(heap).getOrElse { return Result.failure(it) }) {
                val iter = v.iterate(heap).getOrElse { return Result.failure(it) }
                // StarlarkIterator is fused.
                if (!iter.hasNext()) {
                    return Result.failure(IllegalArgumentException(
                        "dict.update expect a list of pairs or a dictionary as first argument, got a list of non-pairs."
                    ))
                }
                val k = iter.next()
                if (!iter.hasNext()) {
                    return Result.failure(IllegalArgumentException(
                        "dict.update expect a list of pairs or a dictionary as first argument, got a list of non-pairs."
                    ))
                }
                val v2 = iter.next()
                if (iter.hasNext()) {
                    return Result.failure(IllegalArgumentException(
                        "dict.update expect a list of pairs or a dictionary as first argument, got a list of non-pairs."
                    ))
                }
                me.aref.value.insertHashed(k.getHashed().getOrElse { return Result.failure(it) }, v2)
            }
        }
    }

    for ((k, v) in derefDict(kwargs).iterHashed()) {
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
): Result<Value> =
    Result.success(heap.allocListIter(derefDict(thisRef).values().toList()))

private fun derefDict(dictRef: DictRef): Dict = when (val ref = dictRef.aref) {
    is Either.Left -> ref.value.value
    is Either.Right -> ref.value
}
