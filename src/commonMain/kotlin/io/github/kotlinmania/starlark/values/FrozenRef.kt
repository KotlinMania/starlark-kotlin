// port-lint: source values/frozen_ref.rs
package io.github.kotlinmania.starlark.values

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * A [FrozenRef] is essentially a [FrozenValue], and has the same memory and
 * access guarantees as it. However, this keeps the type `T` of the actual
 * [FrozenValue] as a reference, allowing manipulation of the actual typed data.
 */
class FrozenRef<T>(
    internal val value: T,
) : Trace, Freeze<FrozenRef<T>> {

    companion object {
        fun <T> new(value: T): FrozenRef<T> {
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
        return FrozenRef(f(value))
    }

    /** Fallible map the reference to another one. */
    fun <U> tryMapResult(f: (T) -> Result<U>): Result<FrozenRef<U>> {
        return f(value).map { FrozenRef(it) }
    }

    /** Optionally map the reference to another one. */
    fun <U> tryMapOption(f: (T) -> U?): FrozenRef<U>? {
        val mapped = f(value) ?: return null
        return FrozenRef(mapped)
    }

    override fun toString(): String {
        return value.toString()
    }

    fun deref(): T {
        return value
    }

    override fun trace(tracer: Tracer) {
        // Do nothing, because `FrozenRef` can only point to frozen value.
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenRef<*>) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun freeze(freezer: Freezer): Result<FrozenRef<T>> {
        return Result.success(this)
    }
}

fun <T> FrozenRef<T>.borrow(): T {
    return value
}

fun <T : Comparable<T>> FrozenRef<T>.partialCmp(other: FrozenRef<T>): Int? {
    return value.compareTo(other.value)
}

fun <T : Comparable<T>> FrozenRef<T>.cmp(other: FrozenRef<T>): Int {
    return value.compareTo(other.value)
}

/** Atomic, optional [FrozenRef]. */
@OptIn(ExperimentalAtomicApi::class)
internal class AtomicFrozenRefOption<T>(
    initial: FrozenRef<T>?,
) : Trace {

    private val ref_: AtomicReference<T?> = AtomicReference(initial?.asRef())

    override fun trace(tracer: Tracer) {
        // Do nothing, because `AtomicFrozenRefOption` holds `FrozenRef`.
    }

    companion object {
        fun <T> new(module: FrozenRef<T>?): AtomicFrozenRefOption<T> {
            return AtomicFrozenRefOption(module)
        }
    }

    fun loadRelaxed(): FrozenRef<T>? {
        // Note this is relaxed load which is cheap.
        val ptr = ref_.load()
        return if (ptr != null) {
            FrozenRef.new(ptr)
        } else {
            null
        }
    }

    fun storeRelaxed(module: FrozenRef<T>) {
        ref_.store(module.asRef())
    }
}
