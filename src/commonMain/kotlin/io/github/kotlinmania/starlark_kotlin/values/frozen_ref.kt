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

// use std::borrow::Borrow
// use std::cmp::Ordering
// use std::fmt
// use std::fmt::Display
// use std::fmt::Formatter
// use std::hash::Hash
// use std::hash::Hasher
// use std::ops::Deref
// use std::ptr
// use std::sync::atomic

// use allocative::Allocative
// use dupe::Clone_
// use dupe::Copy_
// use dupe::Dupe_

// use crate::values::Freeze
// use crate::values::FreezeResult
// use crate::values::Freezer
// use crate::values::Trace
// use crate::values::Tracer

import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * A [FrozenRef] is essentially a [FrozenValue],
 * and has the same memory and access guarantees as it.
 * However, this keeps the type of the type `T` of the actual
 * [FrozenValue] as a reference, allowing manipulation of the actual typed data.
 */
// #[derive(Clone_, Dupe_, Copy_, Debug, Allocative)]
// #[allocative(skip)] // Data is owned by heap.
// pub struct FrozenRef<'fv, T: 'fv + ?Sized>
class FrozenRef<T>(
    internal val value: T,
) : Trace, Freeze<FrozenRef<T>> {

    // impl<'fv, T: 'fv + ?Sized> FrozenRef<'fv, T>

    // pub(crate) const fn new(value: &'fv T) -> FrozenRef<'fv, T>
    companion object {
        fun <T> new(value: T): FrozenRef<T> {
            return FrozenRef(value)
        }

        // impl<'fv, T> Default for FrozenRef<'fv, [T]>
        fun <T> default(): FrozenRef<List<T>> {
            return FrozenRef(emptyList())
        }
    }

    // pub fn as_ref(self) -> &'fv T
    /** Returns a reference to the underlying value. */
    fun asRef(): T {
        return value
    }

    // pub fn map<F, U: 'fv + ?Sized>(self, f: F) -> FrozenRef<'fv, U>
    // where
    //     for<'v> F: FnOnce(&'v T) -> &'v U,
    /** Converts `self` into a new reference that points at something reachable from the previous. */
    fun <U> map(f: (T) -> U): FrozenRef<U> {
        return FrozenRef(f(value))
    }

    // pub fn try_map_result<F, U: 'fv + ?Sized, E>(self, f: F) -> Result<FrozenRef<'fv, U>, E>
    // where
    //     for<'v> F: FnOnce(&'v T) -> Result<&'v U, E>,
    /** Fallible map the reference to another one. */
    fun <U> tryMapResult(f: (T) -> Result<U>): Result<FrozenRef<U>> {
        return f(value).map { FrozenRef(it) }
    }

    // pub fn try_map_option<F, U: 'fv + ?Sized>(self, f: F) -> Option<FrozenRef<'fv, U>>
    // where
    //     for<'v> F: FnOnce(&'v T) -> Option<&'v U>,
    /** Optionally map the reference to another one. */
    fun <U> tryMapOption(f: (T) -> U?): FrozenRef<U>? {
        val mapped = f(value) ?: return null
        return FrozenRef(mapped)
    }

    // impl<'fv, T: ?Sized + Display> Display for FrozenRef<'fv, T>
    // fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    override fun toString(): String {
        return value.toString()
    }

    // impl<'fv, T: ?Sized> Deref for FrozenRef<'fv, T>
    // type Target = T
    // fn deref(&self) -> &T
    fun deref(): T {
        return value
    }

    // impl<'fv, T: 'fv + ?Sized> Borrow<T> for FrozenRef<'fv, T>
    // fn borrow(&self) -> &T
    fun borrow(): T {
        return value
    }

    // impl<'fv, T: 'fv + ?Sized> Borrow<T> for FrozenRef<'fv, Box<T>>
    // fn borrow(&self) -> &T
    // Kotlin: Box has no equivalent; the borrow() above already returns the inner value.

    // unsafe impl<'v, 'fv, T: 'fv + ?Sized> Trace<'v> for FrozenRef<'fv, T>
    // fn trace(&mut self, _: &Tracer<'v>)
    override fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
        // Do nothing, because `FrozenRef` can only point to frozen value.
    }

    // impl<'fv, T: 'fv + ?Sized> PartialEq for FrozenRef<'fv, T>
    // where T: PartialEq,
    // fn eq(&self, other: &Self) -> bool
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenRef<*>) return false
        return value == other.value
    }

    // impl<'fv, T: 'fv + ?Sized> Eq for FrozenRef<'fv, T> where T: Eq {}

    // impl<'fv, T: 'fv + ?Sized> Hash for FrozenRef<'fv, T>
    // where T: Hash,
    // fn hash<H: Hasher>(&self, state: &mut H)
    override fun hashCode(): Int {
        return value.hashCode()
    }

    // impl<'fv, T: 'fv + ?Sized> Freeze for FrozenRef<'fv, T>
    // type Frozen = Self
    // fn freeze(self, _freezer: &Freezer) -> FreezeResult<Self::Frozen>
    override fun freeze(@Suppress("UNUSED_PARAMETER") freezer: Freezer): FreezeResult<FrozenRef<T>> {
        return Result.success(this)
    }
}

// impl<'fv, T: 'fv + ?Sized> PartialOrd for FrozenRef<'fv, T>
// where T: PartialOrd,
// fn partial_cmp(&self, other: &Self) -> Option<Ordering>
fun <T : Comparable<T>> FrozenRef<T>.partialCmp(other: FrozenRef<T>): Int? {
    return value.compareTo(other.value)
}

// impl<'fv, T: 'fv + ?Sized> Ord for FrozenRef<'fv, T>
// where T: Ord,
// fn cmp(&self, other: &Self) -> Ordering
fun <T : Comparable<T>> FrozenRef<T>.cmp(other: FrozenRef<T>): Int {
    return value.compareTo(other.value)
}

/**
 * `Atomic<Option<FrozenRef<T>>>`.
 */
// pub(crate) struct AtomicFrozenRefOption<T>(atomic::AtomicPtr<T>);
@OptIn(ExperimentalAtomicApi::class)
internal class AtomicFrozenRefOption<T>(
    initial: FrozenRef<T>?,
) : Trace {

    private val ref_: AtomicReference<T?> = AtomicReference(initial?.asRef())

    // unsafe impl<'v, T> Trace<'v> for AtomicFrozenRefOption<T>
    // fn trace(&mut self, _: &Tracer<'v>)
    override fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {
        // Do nothing, because `AtomicFrozenRefOption` holds `FrozenRef`.
    }

    // pub(crate) fn new(module: Option<FrozenRef<T>>) -> AtomicFrozenRefOption<T>
    companion object {
        fun <T> new(module: FrozenRef<T>?): AtomicFrozenRefOption<T> {
            return AtomicFrozenRefOption(module)
        }
    }

    // pub(crate) fn load_relaxed(&self) -> Option<FrozenRef<'static, T>>
    fun loadRelaxed(): FrozenRef<T>? {
        // Note this is relaxed load which is cheap.
        val ptr = ref_.load()
        return if (ptr != null) {
            FrozenRef.new(ptr)
        } else {
            null
        }
    }

    // pub(crate) fn store_relaxed(&self, module: FrozenRef<T>)
    fun storeRelaxed(module: FrozenRef<T>) {
        ref_.store(module.asRef())
    }
}
