package io.github.kotlinmania.starlark_kotlin.values

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.IntValue
import io.github.kotlinmania.starlark_kotlin.values.layout.BoolValue
import io.github.kotlinmania.starlark_kotlin.values.layout.NoneValue

/**
 * Extension functions to mimic Rust's ToValue trait.
 * Provides conversions from raw Kotlin primitives into Starlark values.
 */

fun String.toValue(): Value = StringValue(this)

fun Int.toValue(): Value = IntValue(this)

fun Boolean.toValue(): Value = if (this) BoolValue.TRUE else BoolValue.FALSE

fun Value.toValue(): Value = this
