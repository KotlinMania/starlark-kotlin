package io.github.kotlinmania.starlark.util.cell

/**
 * Minimal stand-ins for Rust's `std::cell` types used by line-by-line ports.
 *
 * These are intentionally lightweight and provide only the APIs needed by
 * translated code (e.g. `getMut` for blanket `Trace` impls).
 */

class Cell<T>(
    private var value: T,
) {
    fun getMut(): T = value
}

class OnceCell<T>(
    private var value: T? = null,
) {
    fun getMut(): T? = value
}

class UnsafeCell<T>(
    private var value: T,
) {
    fun getMut(): T = value
}

