// port-lint: source src/stdlib/json.rs
package io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStr
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.DictGen
import io.github.kotlinmania.starlark.values.types.dict.FrozenDictData
import io.github.kotlinmania.starlark.values.types.dict.allocValue
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.smallmap.SmallMap

// ---- JsonNumber: analogous to serdeJson::Number ----

/**
 * JSON number representation, analogous to `serdeJson::Number`.
 *
 * Represents a JSON number that can be an integer (i64/u64) or floating point (f64),
 * or an arbitrarily large integer stored as its string representation.
 */
class JsonNumber(private val raw: String) {

    /** Try to interpret this number as an unsigned 64-bit integer. */
    fun asU64(): Long? {
        val v = raw.toLongOrNull()
        return if (v != null && v >= 0) v else null
    }

    /** Try to interpret this number as a signed 64-bit integer. */
    fun asI64(): Long? = raw.toLongOrNull()

    /** Try to interpret this number as a 64-bit float. */
    fun asF64(): Double? = raw.toDoubleOrNull()

    override fun toString(): String = raw

    companion object {
        fun fromString(s: String): JsonNumber? {
            // Validate that s is a valid JSON number representation.
            // Accept anything parseable as Long or Double, or a pure integer string
            // (potentially very large).
            if (s.toLongOrNull() != null || s.toDoubleOrNull() != null) {
                return JsonNumber(s)
            }
            // Check for very large integer strings: optional leading minus, then digits.
            if (s.matches(Regex("-?[0-9]+"))) {
                return JsonNumber(s)
            }
            return null
        }
    }
}

// ---- StarlarkTypeRepr for JsonNumber ----

// Canonical = Either<i32, f64>
// In Kotlin, we represent this as returning int | float type.

/** [StarlarkTypeRepr] implementation for [JsonNumber]. */
object JsonNumberTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.float())
}

// ---- AllocValue for JsonNumber ----

/**
 * Allocate a [StarlarkInt] as a Starlark [Value] on the heap.
 *
 * For small ints that fit in an [InlineInt], no heap allocation is needed.
 * For big ints, the value is allocated on the heap via [StarlarkBigInt.allocValue].
 */
private fun allocStarlarkInt(starlarkInt: StarlarkInt, heap: Heap): Value {
    return when (starlarkInt) {
        is StarlarkInt.Small -> Value.newInt(starlarkInt.value)
        is StarlarkInt.Big -> starlarkInt.value.allocValue(heap)
    }
}

/**
 * Allocate a [StarlarkInt] as a frozen Starlark value.
 *
 * Same dispatch as [allocStarlarkInt], but on a [FrozenHeap].
 */
private fun allocFrozenStarlarkInt(starlarkInt: StarlarkInt, heap: FrozenHeap): FrozenValue {
    return when (starlarkInt) {
        is StarlarkInt.Small -> FrozenValue.newInt(starlarkInt.value)
        is StarlarkInt.Big -> starlarkInt.value.allocFrozenValue(heap)
    }
}

/**
 * Allocate a [JsonNumber] as a Starlark [Value] on the heap.
 *
 * Tries to represent the number in the most compact form:
 * unsigned 64-bit integer, then signed 64-bit integer, then f64,
 * then falls back to BigInt for arbitrarily large integers.
 */
fun allocJsonNumber(number: JsonNumber, heap: Heap): Value {
    number.asU64()?.let { return allocStarlarkInt(StarlarkInt.from(it), heap) }
    number.asI64()?.let { return allocStarlarkInt(StarlarkInt.from(it), heap) }
    number.asF64()?.let { return heap.allocSimple(StarlarkFloat(it)) }
    val bigStr = number.toString()
    val big = StarlarkInt.fromStrRadix(bigStr, 10)
    if (big.isSuccess) return allocStarlarkInt(big.getOrThrow(), heap)
    error("Unrepresentable number: $number")
}

// ---- AllocFrozenValue for JsonNumber ----

/**
 * Allocate a [JsonNumber] as a frozen Starlark value.
 *
 * Same conversion logic as [allocJsonNumber], but on a [FrozenHeap].
 */
fun allocFrozenJsonNumber(number: JsonNumber, heap: FrozenHeap): FrozenValue {
    number.asU64()?.let { return allocFrozenStarlarkInt(StarlarkInt.from(it), heap) }
    number.asI64()?.let { return allocFrozenStarlarkInt(StarlarkInt.from(it), heap) }
    number.asF64()?.let { return heap.allocSimple(StarlarkFloat(it)) }
    val bigStr = number.toString()
    val big = StarlarkInt.fromStrRadix(bigStr, 10)
    if (big.isSuccess) return allocFrozenStarlarkInt(big.getOrThrow(), heap)
    error("Unrepresentable number: $number")
}

// ---- JsonValue: analogous to serdeJson::Value ----

/**
 * JSON value representation, analogous to `serdeJson::Value`.
 *
 * A sealed class hierarchy representing the possible JSON value types:
 * null, boolean, number, string, array, and object.
 */
sealed class JsonValue {
    data object Null : JsonValue()
    data class Bool(val value: Boolean) : JsonValue()
    data class Number(val value: JsonNumber) : JsonValue()
    data class Str(val value: String) : JsonValue()
    data class Array(val value: List<JsonValue>) : JsonValue()
    data class Object(val value: Map<String, JsonValue>) : JsonValue()
}

// ---- StarlarkTypeRepr for JsonValue ----

// Canonical is any — Value::starlarkTypeRepr()

/** [StarlarkTypeRepr] implementation for [JsonValue]. */
object JsonValueTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.any()
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
fun allocJsonValue(json: JsonValue, heap: Heap): Value {
    return when (json) {
        is JsonValue.Null -> Value.newNone()
        is JsonValue.Bool -> Value.newBool(json.value)
        is JsonValue.Number -> allocJsonNumber(json.value, heap)
        is JsonValue.Str -> heap.allocStr(json.value).toValue()
        is JsonValue.Array -> heap.allocListIter(json.value.map { allocJsonValue(it, heap) })
        is JsonValue.Object -> allocJsonMapOnHeap(
            json.value.mapValues { allocJsonValue(it.value, heap) },
            heap,
        )
    }
}

// ---- AllocFrozenValue for JsonValue ----

/**
 * Allocate a [JsonValue] as a frozen Starlark value.
 *
 * Same recursive conversion as [allocJsonValue], but on a [FrozenHeap].
 */
fun allocFrozenJsonValue(json: JsonValue, heap: FrozenHeap): FrozenValue {
    return when (json) {
        is JsonValue.Null -> FrozenValue.newNone()
        is JsonValue.Bool -> FrozenValue.newBool(json.value)
        is JsonValue.Number -> allocFrozenJsonNumber(json.value, heap)
        is JsonValue.Str -> heap.allocStr(json.value).toFrozenValue()
        is JsonValue.Array -> heap.allocListIter(json.value.map { allocFrozenJsonValue(it, heap) })
        is JsonValue.Object -> allocFrozenJsonMapOnHeap(
            json.value.mapValues { allocFrozenJsonValue(it.value, heap) },
            heap,
        )
    }
}

// ---- StarlarkTypeRepr for JSON Map ----

/** [StarlarkTypeRepr] implementation for JSON maps (dict type). */
object JsonMapTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.dict(Ty.string(), Ty.any())
}

// ---- AllocValue for JSON Map ----

/** Allocate a JSON map as a Starlark dict value. */
fun allocJsonMap(map: Map<String, JsonValue>, heap: Heap): Value {
    val converted = map.mapValues { allocJsonValue(it.value, heap) }
    return allocJsonMapOnHeap(converted, heap)
}

// ---- AllocFrozenValue for JSON Map ----

/** Allocate a JSON map as a frozen Starlark dict value. */
fun allocFrozenJsonMap(map: Map<String, JsonValue>, heap: FrozenHeap): FrozenValue {
    val converted = map.mapValues { allocFrozenJsonValue(it.value, heap) }
    return allocFrozenJsonMapOnHeap(converted, heap)
}

// ---- Internal helpers: Map<String, Value> -> Starlark dict ----

/**
 * Build a Starlark dict [Value] from a [Map] of string keys to already-allocated [Value]s.
 *
 * Allocates each string key on the heap, hashes it, builds a [SmallMap], and wraps in a [Dict].
 */
private fun allocJsonMapOnHeap(map: Map<String, Value>, heap: Heap): Value {
    val sm = SmallMap.withCapacity<Value, Value>(map.size)
    for ((k, v) in map) {
        val keyValue = heap.allocStr(k)
        sm.insertHashed(keyValue.getHashedValue(), v)
    }
    return Dict.new(sm).allocValue(heap)
}

/**
 * Build a frozen Starlark dict from a [Map] of string keys to already-allocated [FrozenValue]s.
 *
 * Allocates each string key on the frozen heap, hashes it, builds a [SmallMap],
 * and wraps in a [FrozenDictData] + [DictGen].
 */
private fun allocFrozenJsonMapOnHeap(map: Map<String, FrozenValue>, heap: FrozenHeap): FrozenValue {
    val sm = SmallMap.withCapacity<FrozenValue, FrozenValue>(map.size)
    for ((k, v) in map) {
        val keyFrozen = heap.allocStr(k).toFrozenValue()
        val keyHash = keyFrozen.toValue().getHash().getOrThrow()
        sm.insertHashed(Hashed.newUnchecked(keyHash, keyFrozen), v)
    }
    return heap.allocSimple(DictGen(FrozenDictData(sm)))
}

// ---- JSON Parsing via kotlinx.serialization.json ----

/** The kotlinx.serialization.json instance used for parsing, configured to be lenient. */
private val jsonParser = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

/**
 * Convert a [kotlinx.serialization.json.JsonElement] into a [JsonValue].
 *
 * This bridges from the kotlinx.serialization JSON representation to our
 * Starlark-oriented [JsonValue] sealed class hierarchy.
 */
private fun jsonElementToJsonValue(element: JsonElement): JsonValue {
    return when (element) {
        is JsonNull -> JsonValue.Null
        is JsonPrimitive -> {
            if (element.isString) {
                JsonValue.Str(element.content)
            } else {
                // Boolean check
                element.booleanOrNull?.let { return JsonValue.Bool(it) }
                // Number: preserve the raw string content for precise big integer handling.
                // We import element.content which gives us the raw text representation.
                JsonValue.Number(JsonNumber(element.content))
            }
        }
        is JsonArray -> JsonValue.Array(element.map { jsonElementToJsonValue(it) })
        is JsonObject -> JsonValue.Object(
            element.entries.associate { (k, v) -> k to jsonElementToJsonValue(v) }
        )
    }
}

/**
 * Parse a JSON string into a [JsonValue].
 *
 * Uses `kotlinx.serialization.json` for robust, standards-compliant parsing.
 */
internal fun parseJsonValue(input: String): JsonValue {
    val element = jsonParser.parseToJsonElement(input)
    return jsonElementToJsonValue(element)
}

// ---- Module registration ----

private fun jsonMembers(globals: GlobalsBuilder) {
    fun encode(x: Value): Result<String> {
        return x.toJson()
    }

    fun decode(x: String, heap: Heap): Result<Value> {
        return try {
            val parsed = parseJsonValue(x)
            Result.success(allocJsonValue(parsed, heap))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    globals.setFunction("encode") { args, eval ->
        val x = args.positional<Value>(0)
        eval.heap().allocStr(encode(x).getOrThrow())
    }

    globals.setFunction("decode") { args, eval ->
        val x = args.positional<String>(0)
        decode(x, eval.heap()).getOrThrow()
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
internal fun json(globals: GlobalsBuilder) {
    globals.namespace("json", ::jsonMembers)
}
