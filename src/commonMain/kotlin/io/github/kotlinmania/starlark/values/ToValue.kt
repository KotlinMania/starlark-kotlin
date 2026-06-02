package io.github.kotlinmania.starlark.values

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.int.InlineInt

/**
 * Conversion helpers for primitive Kotlin values that have direct Starlark value representations.
 *
 * Note: String -> Value requires a Heap (use Heap.allocStr instead).
 */

fun Int.toValue(): Value = Value.newInt(InlineInt(this))

fun Boolean.toValue(): Value = Value.newBool(this)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun Value.toValue(): Value = this
