package io.github.kotlinmania.starlark.util.sync

/**
 * Minimal stand-ins for Rust's `Arc<T>` and `Mutex<T>`.
 *
 * The Kotlin port avoids shared-memory concurrency in `commonMain` for now, but
 * some translated signatures still refer to these container types.
 */

class Arc<T>(
    private val value: T,
) {
    fun getMut(): T = value
}

class Mutex<T>(
    private val value: T,
) {
    fun lock(): T = value
}

