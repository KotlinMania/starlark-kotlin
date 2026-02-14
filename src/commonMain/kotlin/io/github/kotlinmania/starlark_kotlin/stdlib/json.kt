// port-lint: source src/stdlib/json.rs
package io.github.kotlinmania.starlark_kotlin.stdlib

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Value {
    fun toJson(): Result<String> = Result.success("")
    companion object {
        fun newNone(): Value = Value()
        fun newBool(b: Boolean): Value = Value()
        fun starlarkTypeRepr(): Ty = Ty()
    }
}
class FrozenValue {
    companion object {
        fun newNone(): FrozenValue = FrozenValue()
        fun newBool(b: Boolean): FrozenValue = FrozenValue()
        fun starlarkTypeRepr(): Ty = Ty()
    }
}
class Heap {
    fun alloc(value: Any?): Value = Value()
}
class FrozenHeap {
    fun alloc(value: Any?): FrozenValue = FrozenValue()
}
class SmallMap<K, V>
class Ty
class GlobalsBuilder {
    fun namespace(name: String, init: (GlobalsBuilder) -> Unit) {}
    fun set(name: String, value: Any) {}
}
class StarlarkInt {
    companion object {
        fun from(value: Long): StarlarkInt = StarlarkInt()
    }
}

/// JSON number representation, analogous to serde_json::Number.
/// Represents a JSON number that can be an integer (i64/u64) or floating point (f64).
class JsonNumber(private val raw: String) {
    fun asU64(): Long? {
        val v = raw.toLongOrNull()
        return if (v != null && v >= 0) v else null
    }

    fun asI64(): Long? = raw.toLongOrNull()

    fun asF64(): Double? = raw.toDoubleOrNull()

    override fun toString(): String = raw

    companion object {
        fun fromString(s: String): JsonNumber? {
            // Validate it's a valid JSON number
            if (s.toDoubleOrNull() != null || s.toLongOrNull() != null) {
                return JsonNumber(s)
            }
            return null
        }
    }
}

/// Allocate a JsonNumber as a Starlark Value on the heap.
fun allocJsonNumber(number: JsonNumber, heap: Heap): Value {
    val u = number.asU64()
    if (u != null) return heap.alloc(u)
    val i = number.asI64()
    if (i != null) return heap.alloc(i)
    val f = number.asF64()
    if (f != null) return heap.alloc(f)
    val big = number.toString().toBigIntegerOrNull()
    if (big != null) return heap.alloc(StarlarkInt.from(0))
    error("Unrepresentable number: $number")
}

/// Allocate a JsonNumber as a frozen Starlark value.
fun allocFrozenJsonNumber(number: JsonNumber, heap: FrozenHeap): FrozenValue {
    val u = number.asU64()
    if (u != null) return heap.alloc(u)
    val i = number.asI64()
    if (i != null) return heap.alloc(i)
    val f = number.asF64()
    if (f != null) return heap.alloc(f)
    val big = number.toString().toBigIntegerOrNull()
    if (big != null) return heap.alloc(StarlarkInt.from(0))
    error("Unrepresentable number: $number")
}

/// JSON value representation, analogous to serde_json::Value.
sealed class JsonValue {
    data object Null : JsonValue()
    data class Bool(val value: Boolean) : JsonValue()
    data class Number(val value: JsonNumber) : JsonValue()
    data class Str(val value: String) : JsonValue()
    data class Array(val value: List<JsonValue>) : JsonValue()
    data class Object(val value: Map<String, JsonValue>) : JsonValue()
}

/// Allocate a JsonValue as a Starlark Value on the heap.
fun allocJsonValue(json: JsonValue, heap: Heap): Value {
    return when (json) {
        is JsonValue.Null -> Value.newNone()
        is JsonValue.Bool -> Value.newBool(json.value)
        is JsonValue.Number -> allocJsonNumber(json.value, heap)
        is JsonValue.Str -> heap.alloc(json.value)
        is JsonValue.Array -> heap.alloc(json.value.map { allocJsonValue(it, heap) })
        is JsonValue.Object -> heap.alloc(json.value.mapValues { allocJsonValue(it.value, heap) })
    }
}

/// Allocate a JsonValue as a frozen Starlark value.
fun allocFrozenJsonValue(json: JsonValue, heap: FrozenHeap): FrozenValue {
    return when (json) {
        is JsonValue.Null -> FrozenValue.newNone()
        is JsonValue.Bool -> FrozenValue.newBool(json.value)
        is JsonValue.Number -> allocFrozenJsonNumber(json.value, heap)
        is JsonValue.Str -> heap.alloc(json.value)
        is JsonValue.Array -> heap.alloc(json.value.map { allocFrozenJsonValue(it, heap) })
        is JsonValue.Object -> heap.alloc(json.value.mapValues { allocFrozenJsonValue(it.value, heap) })
    }
}

/// Allocate a JSON map as a Starlark dict value.
fun allocJsonMap(map: Map<String, JsonValue>, heap: Heap): Value {
    return heap.alloc(map.mapValues { allocJsonValue(it.value, heap) })
}

/// Allocate a JSON map as a frozen Starlark dict value.
fun allocFrozenJsonMap(map: Map<String, JsonValue>, heap: FrozenHeap): FrozenValue {
    return heap.alloc(map.entries.associate { (k, v) -> k to allocFrozenJsonValue(v, heap) })
}

/// Starlark type repr for JSON number (int | float).
fun jsonNumberStarlarkTypeRepr(): Ty = Ty()

/// Starlark type repr for JSON map.
fun jsonMapStarlarkTypeRepr(): Ty = Ty()

/// Starlark type repr for JSON value (any).
fun jsonValueStarlarkTypeRepr(): Ty = Value.starlarkTypeRepr()

/// json.encode: Encode a Starlark value to a JSON string.
fun jsonEncode(x: Value): Result<String> {
    return x.toJson()
}

/// json.decode: Decode a JSON string to a Starlark value.
fun jsonDecode(x: String, heap: Heap): Result<Value> {
    return try {
        val parsed = parseJsonValue(x)
        Result.success(allocJsonValue(parsed, heap))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/// Register the json module.
// Copying Bazel's json module: https://bazel.build/rules/lib/json
// or starlark-go json module:
// https://github.com/google/starlark-go/blob/d1966c6b9fcd6631f48f5155f47afcd7adcc78c2/lib/json/json.go#L28
fun json(globals: GlobalsBuilder) {
    fun jsonMembers(globals: GlobalsBuilder) {
        globals.set("encode", object : Any() {
            fun invoke(x: Value): Result<String> = jsonEncode(x)
        })
        globals.set("decode", object : Any() {
            fun invoke(x: String, heap: Heap): Result<Value> = jsonDecode(x, heap)
        })
    }

    globals.namespace("json", ::jsonMembers)
}

/// Simple JSON parser returning a JsonValue.
/// This is a placeholder for a full JSON parser integration.
private fun parseJsonValue(input: String): JsonValue {
    val trimmed = input.trim()
    return when {
        trimmed == "null" -> JsonValue.Null
        trimmed == "true" -> JsonValue.Bool(true)
        trimmed == "false" -> JsonValue.Bool(false)
        trimmed.startsWith("\"") -> {
            JsonValue.Str(parseJsonString(trimmed))
        }
        trimmed.startsWith("[") -> {
            JsonValue.Array(parseJsonArray(trimmed))
        }
        trimmed.startsWith("{") -> {
            JsonValue.Object(parseJsonObject(trimmed))
        }
        else -> {
            // Try number
            val num = JsonNumber.fromString(trimmed)
            if (num != null) {
                JsonValue.Number(num)
            } else {
                // Fallback for very large numbers that don't fit in Double
                JsonValue.Number(JsonNumber(trimmed))
            }
        }
    }
}

/// Parse a JSON string literal (with surrounding quotes).
private fun parseJsonString(input: String): String {
    // Simple unescape: strip surrounding quotes and handle basic escapes
    val inner = input.substring(1, input.length - 1)
    val sb = StringBuilder()
    var i = 0
    while (i < inner.length) {
        if (inner[i] == '\\' && i + 1 < inner.length) {
            when (inner[i + 1]) {
                '"' -> { sb.append('"'); i += 2 }
                '\\' -> { sb.append('\\'); i += 2 }
                '/' -> { sb.append('/'); i += 2 }
                'b' -> { sb.append('\b'); i += 2 }
                'n' -> { sb.append('\n'); i += 2 }
                'r' -> { sb.append('\r'); i += 2 }
                't' -> { sb.append('\t'); i += 2 }
                'u' -> {
                    val hex = inner.substring(i + 2, i + 6)
                    sb.append(hex.toInt(16).toChar())
                    i += 6
                }
                else -> { sb.append(inner[i]); i++ }
            }
        } else {
            sb.append(inner[i])
            i++
        }
    }
    return sb.toString()
}

/// Parse a JSON array.
private fun parseJsonArray(input: String): List<JsonValue> {
    val inner = input.substring(1, input.length - 1).trim()
    if (inner.isEmpty()) return emptyList()
    val elements = splitJsonElements(inner)
    return elements.map { parseJsonValue(it) }
}

/// Parse a JSON object.
private fun parseJsonObject(input: String): Map<String, JsonValue> {
    val inner = input.substring(1, input.length - 1).trim()
    if (inner.isEmpty()) return emptyMap()
    val entries = splitJsonElements(inner)
    val result = linkedMapOf<String, JsonValue>()
    for (entry in entries) {
        val colonIdx = findColonInEntry(entry)
        val key = parseJsonString(entry.substring(0, colonIdx).trim())
        val value = parseJsonValue(entry.substring(colonIdx + 1).trim())
        result[key] = value
    }
    return result
}

/// Split JSON elements at top-level commas.
private fun splitJsonElements(input: String): List<String> {
    val elements = mutableListOf<String>()
    var depth = 0
    var inString = false
    var escape = false
    var start = 0
    for (i in input.indices) {
        val c = input[i]
        if (escape) {
            escape = false
            continue
        }
        if (c == '\\') {
            escape = true
            continue
        }
        if (c == '"') {
            inString = !inString
            continue
        }
        if (inString) continue
        when (c) {
            '[', '{' -> depth++
            ']', '}' -> depth--
            ',' -> if (depth == 0) {
                elements.add(input.substring(start, i))
                start = i + 1
            }
        }
    }
    if (start < input.length) {
        elements.add(input.substring(start))
    }
    return elements
}

/// Find the colon separating key from value in a JSON object entry.
private fun findColonInEntry(entry: String): Int {
    var inString = false
    var escape = false
    for (i in entry.indices) {
        val c = entry[i]
        if (escape) {
            escape = false
            continue
        }
        if (c == '\\') {
            escape = true
            continue
        }
        if (c == '"') {
            inString = !inString
            continue
        }
        if (!inString && c == ':') return i
    }
    error("No colon found in JSON object entry: $entry")
}
