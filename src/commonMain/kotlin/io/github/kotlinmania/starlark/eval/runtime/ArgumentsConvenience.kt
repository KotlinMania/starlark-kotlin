package io.github.kotlinmania.starlark.eval.runtime

import io.github.kotlinmania.starlark.values.layout.Value

// Convenience accessors for starlark_module-style argument extraction.
// Not part of the Rust `arguments.rs` transliteration (kept separate for AST similarity hygiene).

/**
 * Get all positional arguments as a list.
 */
fun Arguments.positionalAll(): List<Value> = full.pos

/**
 * Get a single positional argument by 0-based index, unpacking it to type [T].
 * Supports [Value], [String], [Int], and [Boolean] directly.
 * For other types performs an unchecked cast of the underlying [Value].
 * When T is inferred as [Value] (default), returns the raw [Value].
 */
inline fun <reified T> Arguments.positional(index: Int): T {
    val v = full.pos[index]
    return unpackValueAs(v)
}

/**
 * Get an optional positional argument by 0-based index, unpacking it to type [T],
 * or null if the index is out of range.
 */
inline fun <reified T> Arguments.optionalPositional(index: Int): T? {
    val v = full.pos.getOrNull(index) ?: return null
    return unpackValueAs(v)
}

/**
 * Get an optional named argument by name, unpacking it to type [T],
 * or null if the argument is not present.
 */
inline fun <reified T> Arguments.optionalNamed(name: String): T? {
    val idx = full.names.names().indexOfFirst { it.second.asStr() == name }
    if (idx < 0) return null
    return unpackValueAs(full.named[idx])
}

/**
 * Get an optional named argument by name, unpacking it to type [T],
 * or null if the argument is not present. Alias for [optionalNamed].
 */
inline fun <reified T> Arguments.namedOptional(name: String): T? = optionalNamed<T>(name)

/**
 * Unpack a [Value] to type [T]. Used by [Arguments] convenience accessors.
 * Handles [Value], [String], [Int], and [Boolean] directly.
 * For other types performs an unchecked cast of the underlying [Value].
 */
@Suppress("UNCHECKED_CAST")
@PublishedApi
internal inline fun <reified T> unpackValueAs(v: Value): T {
    return when (T::class) {
        Value::class -> v as T
        String::class -> v.unpackStrErr().getOrThrow() as T
        Int::class -> (v.unpackI32()
            ?: throw IllegalArgumentException("Expected Int, got ${v.toStringForTypeError()}")) as T
        Boolean::class -> v.toBool() as T
        else -> v as T
    }
}

