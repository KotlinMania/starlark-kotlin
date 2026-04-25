package io.github.kotlinmania.starlark_kotlin.values

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt

/**
 * Extension functions to mimic Rust's ToValue trait.
 * Provides conversions from raw Kotlin primitives into Starlark values.
 *
 * Note: String -> Value requires a Heap (use Heap.allocStr instead).
 */

fun Int.toValue(): Value = Value.newInt(InlineInt(this))

fun Boolean.toValue(): Value = Value.newBool(this)

fun Value.toValue(): Value = this
