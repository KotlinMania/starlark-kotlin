// port-lint: source src/values/owned_frozen_ref.rs
package io.github.kotlinmania.starlark.values.owned_frozen_ref

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

import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/** A reference to a value stored in a frozen heap with a reference to the heap. */
// #[derive(Copy_, Clone_, Dupe_)]
// pub struct OwnedRefFrozenRef<'f, T: ?Sized + 'static> {
//     owner: &'f FrozenHeapRef,
//     value: FrozenRef<'f, T>,
// }
// Kotlin: GC handles lifetimes; owner kept for heap reference tracking.
class OwnedRefFrozenRef<T : Any>(
    private val owner: FrozenHeapRef,
    private val value: FrozenRef<T>,
) {
    companion object {
        // pub unsafe fn new_unchecked(value: &T, owner: &FrozenHeapRef) -> OwnedRefFrozenRef<T>
        fun <T : Any> newUnchecked(value: T, owner: FrozenHeapRef): OwnedRefFrozenRef<T> {
            return OwnedRefFrozenRef(owner = owner, value = FrozenRef.new(value))
        }
    }

    /** Owner heap. */
    // pub fn owner(&self) -> &FrozenHeapRef
    fun owner(): FrozenHeapRef = owner

    /** Return a reference to the underlying value. */
    // pub fn as_ref(self) -> &T
    fun asRef(): T = value.asRef()

    /** Add a reference to a new heap, and return the pointer with the lifetime of the new heap. */
    // pub fn add_heap_ref(self, heap: &FrozenHeap) -> &T
    fun addHeapRef(heap: FrozenHeap): T {
        heap.addReference(owner)
        return value.asRef()
    }

    /** Like `addHeapRef`, but for an unfrozen heap. */
    // pub fn add_unfrozen_heap_ref(self, heap: Heap) -> &T
    fun addUnfrozenHeapRef(heap: Heap): T {
        heap.addReference(owner)
        return value.asRef()
    }

    /** Convert heap pointer to an owned one. */
    // pub fn to_owned(self) -> OwnedFrozenRef<T>
    fun toOwned(): OwnedFrozenRef<T> {
        return OwnedFrozenRef.newUnchecked(value.asRef(), owner)
    }

    /** Fallible map the reference to another one. */
    // pub fn try_map_result<F, U, E>(self, f: F) -> Result<OwnedRefFrozenRef<U>, E>
    fun <U : Any> tryMapResult(f: (T) -> Result<U>): Result<OwnedRefFrozenRef<U>> {
        return f(value.asRef()).map { u ->
            OwnedRefFrozenRef(owner = owner, value = FrozenRef.new(u))
        }
    }

    /** Apply a function to the underlying value. Projection operation. */
    // pub fn map<F, U>(self, f: F) -> OwnedRefFrozenRef<U>
    fun <U : Any> map(f: (T) -> U): OwnedRefFrozenRef<U> {
        return OwnedRefFrozenRef(owner = owner, value = FrozenRef.new(f(value.asRef())))
    }

    /** Optionally map the reference to another one. */
    // pub fn try_map_option<F, U>(self, f: F) -> Option<OwnedRefFrozenRef<U>>
    fun <U : Any> tryMapOption(f: (T) -> U?): OwnedRefFrozenRef<U>? {
        val result = f(value.asRef()) ?: return null
        return OwnedRefFrozenRef(owner = owner, value = FrozenRef.new(result))
    }
}

/**
 * Same as a `FrozenRef`, but it keeps itself alive by storing a reference to the owning heap.
 *
 * Usually constructed from an `OwnedFrozenValueTyped`.
 */
// #[derive(Clone, Dupe, Allocative)]
// pub struct OwnedFrozenRef<T: ?Sized + 'static> {
//     owner: FrozenHeapRef,
//     value: FrozenRef<'static, T>,
// }
class OwnedFrozenRef<T : Any>(
    private val owner: FrozenHeapRef,
    private val value: FrozenRef<T>,
) {
    companion object {
        // pub unsafe fn new_unchecked(value: &'static T, owner: FrozenHeapRef) -> OwnedFrozenRef<T>
        fun <T : Any> newUnchecked(value: T, owner: FrozenHeapRef): OwnedFrozenRef<T> {
            return OwnedFrozenRef(owner = owner, value = FrozenRef.new(value))
        }
    }

    /** Borrow. */
    // pub fn as_owned_ref_frozen_ref(&self) -> OwnedRefFrozenRef<T>
    fun asOwnedRefFrozenRef(): OwnedRefFrozenRef<T> {
        return OwnedRefFrozenRef(owner = owner, value = value)
    }

    /** Returns a reference to the underlying value. */
    // pub fn as_ref(&self) -> &T
    fun asRef(): T = value.asRef()

    /** Converts `self` into a new reference that points at something reachable from the previous. */
    // pub fn map<F, U>(self, f: F) -> OwnedFrozenRef<U>
    fun <U : Any> map(f: (T) -> U): OwnedFrozenRef<U> {
        return OwnedFrozenRef(owner = owner, value = value.map(f))
    }

    /** Fallible map the reference to another one. */
    // pub fn try_map_result<F, U, E>(self, f: F) -> Result<OwnedFrozenRef<U>, E>
    fun <U : Any> tryMapResult(f: (T) -> Result<U>): Result<OwnedFrozenRef<U>> {
        return value.tryMapResult(f).map { mapped ->
            OwnedFrozenRef(owner = owner, value = mapped)
        }
    }

    /** Optionally map the reference to another one. */
    // pub fn try_map_option<F, U>(self, f: F) -> Option<OwnedFrozenRef<U>>
    fun <U : Any> tryMapOption(f: (T) -> U?): OwnedFrozenRef<U>? {
        val mapped = value.tryMapOption(f) ?: return null
        return OwnedFrozenRef(owner = owner, value = mapped)
    }

    /** Get a reference to the owning frozen heap. */
    // pub fn owner(&self) -> &FrozenHeapRef
    fun owner(): FrozenHeapRef = owner

    // impl Deref for OwnedFrozenRef
    // fn deref(&self) -> &T
    // Kotlin: direct access via asRef()

    // impl Debug for OwnedFrozenRef
    // impl Display for OwnedFrozenRef
    override fun toString(): String = value.toString()
}
