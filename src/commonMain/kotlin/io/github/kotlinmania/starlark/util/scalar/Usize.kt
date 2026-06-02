package io.github.kotlinmania.starlark.util.scalar

/**
 * Minimal stand-in for Rust's `usize`.
 *
 * Most Kotlin code uses `Int` directly for pointer-sized integers, but some line-by-line
 * ports need a distinct type to preserve the original Rust shape.
 */
data class Usize(
    val value: Int,
)
