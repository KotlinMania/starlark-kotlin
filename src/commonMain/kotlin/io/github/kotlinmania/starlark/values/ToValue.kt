package io.github.kotlinmania.starlark.values

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.int.InlineInt

/**
 * Conversion helpers for primitive Kotlin values that have direct Starlark value representations.
 *
 * Note: String -> Value requires a Heap (use Heap.allocStr instead).
 */

internal fun Int.toValue(): Value = Value.newInt(InlineInt(this))

internal fun Boolean.toValue(): Value = Value.newBool(this)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal fun Value.toValue(): Value = this
