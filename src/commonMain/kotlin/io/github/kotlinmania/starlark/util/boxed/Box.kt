package io.github.kotlinmania.starlark.util.boxed

/**
 * Minimal stand-in for Rust's `Box<T>`.
 *
 * Kotlin doesn't need boxing for ownership, but ports occasionally need a distinct
 * type to preserve the original shape of the Rust code (e.g. blanket impls).
 */
class Box<T>(
    private var value: T,
) {
    fun asMut(): T = value
}

