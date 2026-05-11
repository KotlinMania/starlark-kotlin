// port-lint: source src/values/frozen_ref.rs
package io.github.kotlinmania.starlark_kotlin.values

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * A [FrozenRef] is essentially a [FrozenValue], and has the same memory and
 * access guarantees as it. However, this keeps the type `T` of the actual
 * [FrozenValue] as a reference, allowing manipulation of the actual typed data.
 */
class FrozenRef<T> internal constructor(
    internal val value: T,
) : Trace, Freeze<FrozenRef<T>> {

    companion object {
        internal fun <T> new(value: T): FrozenRef<T> {
            return FrozenRef(value)
        }

        fun <T> default(): FrozenRef<List<T>> {
            return FrozenRef(emptyList())
        }
    }

    /** Returns a reference to the underlying value. */
    fun asRef(): T {
        return value
    }

    /** Converts `self` into a new reference that points at something reachable from the previous. */
    fun <U> map(f: (T) -> U): FrozenRef<U> {
        return FrozenRef(value = f(value))
    }

    /** Fallible map the reference to another one. */
    fun <U> tryMapResult(f: (T) -> Result<U>): Result<FrozenRef<U>> {
        return f(value).map { FrozenRef(value = it) }
    }

    /** Optionally map the reference to another one. */
    fun <U> tryMapOption(f: (T) -> U?): FrozenRef<U>? {
        val mapped = f(value) ?: return null
        return FrozenRef(value = mapped)
    }

    // impl Display for FrozenRef
    override fun toString(): String = value.toString()

    // Rust: impl Display for FrozenRef: fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    @Suppress("UNUSED_PARAMETER")
    fun fmt(f: Any?): String {
        f?.hashCode()
        return value.toString()
    }

    // impl Deref for FrozenRef
    fun deref(): T {
        return value
    }

    // impl Borrow<T> for FrozenRef<T>
    fun borrow(): T {
        return value
    }

    // Rust has a second `Borrow` impl for `FrozenRef<Box<T>>` (same method name).
    @Suppress("UNUSED_PARAMETER")
    fun borrow(boxed: Any?): T {
        boxed?.hashCode()
        return value
    }

    override fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
        // Do nothing, because `FrozenRef` can only point to frozen value.
    }

    // impl PartialEq/Eq for FrozenRef
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenRef<*>) return false
        return value == other.value
    }

    // Rust: impl PartialEq for FrozenRef: fn eq(&self, other: &Self) -> bool
    fun eq(other: FrozenRef<T>): Boolean {
        return value == other.value
    }

    // impl Hash for FrozenRef
    override fun hashCode(): Int {
        return value.hashCode()
    }

    // Rust: impl Hash for FrozenRef: fn hash<H: Hasher>(&self, state: &mut H)
    fun hash(state: StarlarkHasher) {
        // Best-effort for generic T: hashCode parity across freeze is required by the Rust contract.
        state.writeU64(value.hashCode().toULong())
    }

    // impl Freeze for FrozenRef
    override fun freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): Result<FrozenRef<T>> {
        return Result.success(this)
    }
}

// impl PartialOrd for FrozenRef
fun <T : Comparable<T>> FrozenRef<T>.partialCmp(other: FrozenRef<T>): Int? {
    return value.compareTo(other.value)
}

// impl Ord for FrozenRef
fun <T : Comparable<T>> FrozenRef<T>.cmp(other: FrozenRef<T>): Int {
    return value.compareTo(other.value)
}

/** `Atomic<Option<FrozenRef<T>>>`. */
@OptIn(ExperimentalAtomicApi::class)
internal class AtomicFrozenRefOption<T>(
    initial: FrozenRef<T>?,
) : Trace {

    private val ptr: AtomicReference<T?> = AtomicReference(initial?.asRef())

    override fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
        // Do nothing, because `AtomicFrozenRefOption` holds `FrozenRef`.
    }

    companion object {
        fun <T> new(module: FrozenRef<T>?): AtomicFrozenRefOption<T> {
            return AtomicFrozenRefOption(module)
        }
    }

    fun loadRelaxed(): FrozenRef<T>? {
        // Note this is relaxed load which is cheap.
        val loaded = ptr.load()
        if (loaded == null) {
            return null
        }
        return FrozenRef.new(loaded)
    }

    fun storeRelaxed(module: FrozenRef<T>) {
        ptr.store(module.asRef())
    }
}
