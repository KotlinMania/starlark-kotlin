package io.github.kotlinmania.starlark.util.atomics

/**
 * Minimal multiplatform stand-ins for Rust's `std::sync::atomic::*` types.
 *
 * These types exist to support faithful Rust→Kotlin transliterations where the
 * Rust source uses atomics in type signatures. The Kotlin port currently uses
 * platform-agnostic patterns elsewhere; most of these are only needed for type
 * shape parity and for `Trace`/`Freeze` blanket impls.
 */

class AtomicBool

class AtomicI8

class AtomicU8

class AtomicI16

class AtomicU16

class AtomicI32

class AtomicU32

class AtomicI64

class AtomicU64

class AtomicIsize

class AtomicUsize
