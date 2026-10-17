// port-lint: source src/stdlib/json.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.serdejson.JsonMap
import io.github.kotlinmania.serdejson.JsonNumber
import io.github.kotlinmania.serdejson.fromStr
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStr
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.DictGen
import io.github.kotlinmania.starlark.values.types.dict.FrozenDict
import io.github.kotlinmania.starlark.values.types.dict.FrozenDictData
import io.github.kotlinmania.starlark.values.types.dict.allocValue
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import kotlin.native.HiddenFromObjC
import io.github.kotlinmania.serdejson.Value as JsonValue

// ---- StarlarkTypeRepr for JsonNumber ----

/** [StarlarkTypeRepr] implementation for [JsonNumber]. */
internal object JsonNumberTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty =
        Ty.union2(
            Ty.int(),
            Ty.float(),
        )
}

// ---- AllocValue for StarlarkInt ----

private fun allocStarlarkInt(starlarkInt: StarlarkInt, heap: Heap): Value =
    when (starlarkInt) {
        is StarlarkInt.Small -> {
            Value.newInt(starlarkInt.value)
        }

        is StarlarkInt.Big -> {
            starlarkInt.value.allocValue(heap)
        }
    }

private fun allocFrozenStarlarkInt(starlarkInt: StarlarkInt, heap: FrozenHeap): FrozenValue =
    when (starlarkInt) {
        is StarlarkInt.Small -> {
            FrozenValue.newInt(starlarkInt.value)
        }

        is StarlarkInt.Big -> {
            starlarkInt.value.allocFrozenValue(heap)
        }
    }

/**
 * Allocate a [JsonNumber] as a Starlark [Value] on the heap.
 *
 * Tries to represent the number in the most compact form:
 * unsigned 64-bit integer, then signed 64-bit integer, then f64,
 * then falls back to BigInt for arbitrarily large integers.
 */
internal fun allocJsonNumber(number: JsonNumber, heap: Heap): Value {
    number.asU64()?.let {
        return allocStarlarkInt(
            StarlarkInt.from(it),
            heap,
        )
    }
    number.asI64()?.let {
        return allocStarlarkInt(
            StarlarkInt.from(it),
            heap,
        )
    }
    number.asF64()?.let {
        return heap.allocSimple(
            StarlarkFloat(
                it,
            ),
        )
    }
    val bigStr = number.toString()
    val big =
        StarlarkInt.fromStrRadix(bigStr, 10)
    if (big.isSuccess) return allocStarlarkInt(big.getOrThrow(), heap)
    error("Unrepresentable number: $number")
}

// ---- AllocFrozenValue for JsonNumber ----

/**
 * Allocate a [JsonNumber] as a frozen Starlark value.
 *
 * Same conversion logic as [allocJsonNumber], but on a [FrozenHeap].
 */
internal fun allocFrozenJsonNumber(number: JsonNumber, heap: FrozenHeap): FrozenValue {
    number.asU64()?.let {
        return allocFrozenStarlarkInt(
            StarlarkInt.from(it),
            heap,
        )
    }
    number.asI64()?.let {
        return allocFrozenStarlarkInt(
            StarlarkInt.from(it),
            heap,
        )
    }
    number.asF64()?.let {
        return heap.allocSimple(
            StarlarkFloat(
                it,
            ),
        )
    }
    val bigStr = number.toString()
    val big =
        StarlarkInt.fromStrRadix(bigStr, 10)
    if (big.isSuccess) return allocFrozenStarlarkInt(big.getOrThrow(), heap)
    error("Unrepresentable number: $number")
}

// ---- StarlarkTypeRepr for JsonValue ----

/** [StarlarkTypeRepr] implementation for [JsonValue]. */
internal object JsonValueTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty =
        Ty.any()
}

// ---- AllocValue for JsonValue ----

/**
 * Allocate a [JsonValue] as a Starlark [Value] on the heap.
 *
 * Recursively converts JSON structures into their Starlark equivalents:
 * - `null` becomes `None`
 * - booleans become Starlark bools
 * - numbers are allocated via [allocJsonNumber]
 * - strings are heap-allocated
 * - arrays become Starlark lists
 * - objects become Starlark dicts
 */
internal fun allocJsonValue(json: JsonValue, heap: Heap): Value =
    when (json) {
        is JsonValue.Null -> {
            Value.newNone()
        }

        is JsonValue.Bool -> {
            Value.newBool(json.value)
        }

        is JsonValue.Number -> {
            allocJsonNumber(json.value, heap)
        }

        is JsonValue.Str -> {
            heap.allocStr(json.value)
        }

        is JsonValue.Array -> {
            heap.allocListIter(json.value.map { allocJsonValue(it, heap) })
        }

        is JsonValue.Object -> {
            val sm = SmallMap.withCapacity<Value, Value>(json.value.len())
            for ((k, v) in json.value) {
                val keyValue = heap.allocStr(k)
                sm.insertHashed(
                    keyValue.getHashed().getOrThrow(),
                    allocJsonValue(v, heap),
                )
            }
            Dict.new(sm).allocValue(heap)
        }
    }

// ---- AllocFrozenValue for JsonValue ----

/**
 * Allocate a [JsonValue] as a frozen Starlark value.
 *
 * Same recursive conversion as [allocJsonValue], but on a [FrozenHeap].
 */
internal fun allocFrozenJsonValue(json: JsonValue, heap: FrozenHeap): FrozenValue =
    when (json) {
        is JsonValue.Null -> {
            FrozenValue.newNone()
        }

        is JsonValue.Bool -> {
            FrozenValue.newBool(json.value)
        }

        is JsonValue.Number -> {
            allocFrozenJsonNumber(json.value, heap)
        }

        is JsonValue.Str -> {
            heap.allocStr(json.value).toFrozenValue()
        }

        is JsonValue.Array -> {
            heap.allocListIter(json.value.map { allocFrozenJsonValue(it, heap) })
        }

        is JsonValue.Object -> {
            val sm = SmallMap.withCapacity<FrozenValue, FrozenValue>(json.value.len())
            for ((k, v) in json.value) {
                val keyFrozen = heap.allocStr(k).toFrozenValue()
                val keyHash = keyFrozen.toValue().getHash().getOrThrow()
                sm.insertHashed(
                    Hashed.newUnchecked(keyHash, keyFrozen),
                    allocFrozenJsonValue(v, heap),
                )
            }
            heap.allocSimple(
                FrozenDict(
                    DictGen(
                        FrozenDictData(sm),
                    ),
                ),
            )
        }
    }

// ---- StarlarkTypeRepr for JSON Map ----

/** [StarlarkTypeRepr] implementation for JSON maps (dict type). */
internal object JsonMapTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty =
        Ty.dict(
            Ty.string(),
            Ty.any(),
        )
}

// ---- AllocValue for JSON Map ----

/** Allocate a JSON map as a Starlark dict value. */
@HiddenFromObjC
internal fun allocJsonMap(map: JsonMap, heap: Heap): Value {
    val sm = SmallMap.withCapacity<Value, Value>(map.len())
    for ((k, v) in map) {
        val keyValue = heap.allocStr(k)
        sm.insertHashed(keyValue.getHashed().getOrThrow(), allocJsonValue(v, heap))
    }
    return Dict.new(sm).allocValue(heap)
}

// ---- AllocFrozenValue for JSON Map ----

/** Allocate a JSON map as a frozen Starlark dict value. */
@HiddenFromObjC
internal fun allocFrozenJsonMap(map: JsonMap, heap: FrozenHeap): FrozenValue {
    val sm = SmallMap.withCapacity<FrozenValue, FrozenValue>(map.len())
    for ((k, v) in map) {
        val keyFrozen = heap.allocStr(k).toFrozenValue()
        val keyHash = keyFrozen.toValue().getHash().getOrThrow()
        sm.insertHashed(
            Hashed.newUnchecked(keyHash, keyFrozen),
            allocFrozenJsonValue(v, heap),
        )
    }
    return heap.allocSimple(
        FrozenDict(
            DictGen(
                FrozenDictData(sm),
            ),
        ),
    )
}

/**
 * Parse a JSON string into a [JsonValue].
 *
 * Uses `serde-json-kotlin` for robust, standards-compliant parsing.
 */
internal fun parseJsonValue(input: String): JsonValue =
    fromStr(input).getOrThrow()

// ---- json.encode / json.decode ----

/**
 * `json.encode`: Encode a Starlark value to a JSON string.
 *
 * Delegates to [Value.toJson].
 */
fun jsonEncode(x: Value): Result<String> = x.toJson()

/**
 * `json.decode`: Decode a JSON string to a Starlark value.
 *
 * Parses the input string as JSON and allocates the resulting value on the given heap.
 */
fun jsonDecode(x: String, heap: Heap): Result<Value> =
    try {
        val parsed = parseJsonValue(x)
        Result.success(allocJsonValue(parsed, heap))
    } catch (e: Exception) {
        Result.failure(e)
    }

// ---- Module registration ----

private fun jsonMembers(globals: GlobalsBuilder) {
    globals.setFunction("encode") { args, eval ->
        val x = args.positional<Value>(0)
        eval.heap().allocStr(jsonEncode(x).getOrThrow())
    }

    globals.setFunction("decode") { args, eval ->
        val x = args.positional<String>(0)
        jsonDecode(x, eval.heap()).getOrThrow()
    }
}

// Copying Bazel's json module: https://bazel.build/rules/lib/json
// or starlark-go json module:
// https://github.com/google/starlark-go/blob/d1966c6b9fcd6631f48f5155f47afcd7adcc78c2/lib/json/json.go#L28

/**
 * Register the `json` module on a [GlobalsBuilder].
 *
 * Provides `json.encode` and `json.decode` following Bazel's json module specification.
 */
internal fun registerJson(globals: GlobalsBuilder) {
    globals.namespace("json", ::jsonMembers)
}
