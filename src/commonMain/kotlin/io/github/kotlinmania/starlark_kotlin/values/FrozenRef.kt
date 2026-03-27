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

import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.intoInner
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult

/**
 * A [FrozenRef] is essentially a [FrozenValue],
 * and has the same memory and access guarantees as it.
 * However, this keeps the type of the type `T` of the actual
 * [FrozenValue] as a reference, allowing manipulation of the actual typed data.
 */
// #[derive(Clone_, Dupe_, Copy_, Debug, Allocative)]
// #[allocative(skip)] // Data is owned by heap.
// pub struct FrozenRef<'fv, T: 'fv + ?Sized>
// TODO: stub - FrozenRef needs real import
class FrozenRef<T>(
    internal val value: T,
) : Trace {
    // impl<'fv, T> Default for FrozenRef<'fv, [T]>
    // Kotlin: use companion object factory for default empty list ref.

    // impl<'fv, T: 'fv + ?Sized> FrozenRef<'fv, T>

    // pub fn as_ref(self) -> &'fv T
    /** Returns a reference to the underlying value. */
    fun asRef(): T {
        return value
    }

    fun deref(): T {
        return value
    }

    // pub fn map<F, U: 'fv + ?Sized>(self, f: F) -> FrozenRef<'fv, U>
    /** Converts `self` into a new reference that points at something reachable from the previous. */
    fun <U> map(f: (T) -> U): FrozenRef<U> {
        return FrozenRef.new(
            value = f(value),
        )
    }

    // pub fn try_map_result<F, U: 'fv + ?Sized, E>(self, f: F) -> Result<FrozenRef<'fv, U>, E>
    /** Fallible map the reference to another one. */
    fun <U, E> tryMapResult(f: (T) -> Result<U>): Result<FrozenRef<U>> {
        val mapped = f(value)
        return mapped.map { FrozenRef.new(it) }
    }

    // pub fn try_map_option<F, U: 'fv + ?Sized>(self, f: F) -> Option<FrozenRef<'fv, U>>
    /** Optionally map the reference to another one. */
    fun <U> tryMapOption(f: (T) -> U?): FrozenRef<U>? {
        val mapped = f(value) ?: return null
        return FrozenRef.new(mapped)
    }

    // impl<'fv, T: ?Sized + Display> Display for FrozenRef<'fv, T>
    override fun toString(): String {
        return value.toString()
    }

    // impl<'fv, T: ?Sized> Deref for FrozenRef<'fv, T>
    // Kotlin: no Deref equivalent. Use `value` field directly or `asRef()`.

    // impl<'fv, T: 'fv + ?Sized> Borrow<T> for FrozenRef<'fv, T>
    // Kotlin: no Borrow trait. Use `asRef()`.

    // impl<'fv, T: 'fv + ?Sized> Borrow<T> for FrozenRef<'fv, Box<T>>
    // Kotlin: no Box/Borrow distinction needed.

    // unsafe impl<'v, 'fv, T: 'fv + ?Sized> Trace<'v> for FrozenRef<'fv, T>
    /** FrozenRef trace is a no-op because it can only point to frozen values. */
    override fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
        // Do nothing, because FrozenRef can only point to frozen value.
    }

    // impl<'fv, T: 'fv + ?Sized> PartialEq for FrozenRef<'fv, T>
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenRef<*>) return false
        return value == other.value
    }

    // impl<'fv, T: 'fv + ?Sized> Hash for FrozenRef<'fv, T>
    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        fun <T> new(value: T): FrozenRef<T> {
            return FrozenRef(value)
        }

        // impl<'fv, T> Default for FrozenRef<'fv, [T]>
        /** Default for FrozenRef of a list: empty list. */
        fun <T> default(): FrozenRef<List<T>> {
            return FrozenRef.new(emptyList())
        }

        fun <T> defaultList(): FrozenRef<List<T>> {
            return default()
        }
    }
}

fun <T> FrozenRef<T>.borrow(): T {
    return deref()
}

fun <T> FrozenRef<Box<T>>.borrow(): T {
    return deref().intoInner()
}

// impl<'fv, T: 'fv + ?Sized> PartialOrd for FrozenRef<'fv, T>
// impl<'fv, T: 'fv + ?Sized> Ord for FrozenRef<'fv, T>
/** Compare two [FrozenRef]s when the contained type is [Comparable]. */
fun <T : Comparable<T>> FrozenRef<T>.compareTo(other: FrozenRef<T>): Int {
    return value.compareTo(other.value)
}

fun <T : Comparable<T>> FrozenRef<T>.partialCmp(other: FrozenRef<T>): Int? {
    return compareTo(other)
}

fun <T : Comparable<T>> FrozenRef<T>.cmp(other: FrozenRef<T>): Int {
    return compareTo(other)
}

// impl<'fv, T: 'fv + ?Sized> Freeze for FrozenRef<'fv, T>
/** Freeze a [FrozenRef]. FrozenRef is already frozen, so this is a no-op. */
fun <T> FrozenRef<T>.freeze(
    @Suppress("UNUSED_PARAMETER") freezer: Freezer,
): FreezeResult<FrozenRef<T>> {
    return FreezeResult.success(this)
}

/**
 * `Atomic<Option<FrozenRef<T>>>`.
 *
 * An atomic optional reference to a frozen value.
 */
// pub(crate) struct AtomicFrozenRefOption<T>(atomic::AtomicPtr<T>);
internal class AtomicFrozenRefOption<T>(
    initial: FrozenRef<T>?,
) : Trace {
    private val ref_ = atomic(initial?.asRef())

    // unsafe impl<'v, T> Trace<'v> for AtomicFrozenRefOption<T>
    /** Trace is a no-op because AtomicFrozenRefOption holds FrozenRef. */
    override fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
        // Do nothing, because AtomicFrozenRefOption holds FrozenRef.
    }

    constructor() : this(null)

    // pub(crate) fn new(module: Option<FrozenRef<T>>) -> AtomicFrozenRefOption<T>
    companion object {
        fun <T> new(module: FrozenRef<T>?): AtomicFrozenRefOption<T> {
            return AtomicFrozenRefOption(module)
        }
    }

    // pub(crate) fn load_relaxed(&self) -> Option<FrozenRef<'static, T>>
    /** Load the value with relaxed ordering. */
    fun loadRelaxed(): FrozenRef<T>? {
        // Note this is relaxed load which is cheap.
        return ref_.value?.let { FrozenRef.new(it) }
    }

    // pub(crate) fn store_relaxed(&self, module: FrozenRef<T>)
    /** Store a value with relaxed ordering. */
    fun storeRelaxed(module: FrozenRef<T>) {
        val value: T = module.asRef()
        ref_.value = value
    }
}
