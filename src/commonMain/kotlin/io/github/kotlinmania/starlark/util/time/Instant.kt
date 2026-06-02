package io.github.kotlinmania.starlark.util.time

/**
 * Minimal stand-in for Rust's `std::time::Instant`.
 *
 * The Kotlin port uses a dedicated profiler time type elsewhere; this exists to
 * preserve type shape for blanket impls when transliterating Rust code.
 */
class Instant

