// port-lint: source src/values/owned_frozen_ref.rs
package io.github.kotlinmania.starlark.values.ownedfrozenref

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

import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.layout.heap.Heap

/** A reference to a value stored in a frozen heap with a reference to the heap. */
//     owner: &'f FrozenHeapRef,
// Kotlin: GC handles lifetimes; owner kept for heap reference tracking.
class OwnedRefFrozenRef<T : Any>(
    private val owner: FrozenHeapRef,
    private val value: FrozenRef<T>,
) : AutoCloseable {

    override fun close() {
        owner.close()
    }
    companion object {
        fun <T : Any> newUnchecked(value: T, owner: FrozenHeapRef): OwnedRefFrozenRef<T> = OwnedRefFrozenRef(owner = owner, value = FrozenRef.new(value))
    }

    /** Owner heap. */
    fun owner(): FrozenHeapRef = owner

    /** Return a reference to the underlying value. */
    fun asRef(): T = value.asRef()

    /** Add a reference to a new heap, and return the pointer with the lifetime of the new heap. */
    fun addHeapRef(heap: FrozenHeap): T {
        heap.addReference(owner)
        return value.asRef()
    }

    /** Like `addHeapRef`, but for an unfrozen heap. */
    fun addUnfrozenHeapRef(heap: Heap): T {
        heap.addReference(owner)
        return value.asRef()
    }

    /** Convert heap pointer to an owned one. */
    fun toOwned(): OwnedFrozenRef<T> = OwnedFrozenRef.newUnchecked(value.asRef(), owner.clone())

    /** Fallible map the reference to another one. */
    fun <U : Any> tryMapResult(f: (T) -> Result<U>): Result<OwnedRefFrozenRef<U>> =
        f(value.asRef()).map { u ->
            OwnedRefFrozenRef(owner = owner.clone(), value = FrozenRef.new(u))
        }

    /** Apply a function to the underlying value. Projection operation. */
    fun <U : Any> map(f: (T) -> U): OwnedRefFrozenRef<U> = OwnedRefFrozenRef(owner = owner.clone(), value = FrozenRef.new(f(value.asRef())))

    /** Optionally map the reference to another one. */
    fun <U : Any> tryMapOption(f: (T) -> U?): OwnedRefFrozenRef<U>? {
        val result = f(value.asRef()) ?: return null
        return OwnedRefFrozenRef(owner = owner.clone(), value = FrozenRef.new(result))
    }
}

/**
 * Same as a `FrozenRef`, but it keeps itself alive by storing a reference to the owning heap.
 *
 * Usually constructed from an `OwnedFrozenValueTyped`.
 */
//     owner: FrozenHeapRef,
class OwnedFrozenRef<T : Any>(
    private val owner: FrozenHeapRef,
    private val value: FrozenRef<T>,
) : AutoCloseable {

    override fun close() {
        owner.close()
    }
    companion object {
        fun <T : Any> newUnchecked(value: T, owner: FrozenHeapRef): OwnedFrozenRef<T> = OwnedFrozenRef(owner = owner, value = FrozenRef.new(value))
    }

    /** Borrow. */
    fun asOwnedRefFrozenRef(): OwnedRefFrozenRef<T> = OwnedRefFrozenRef(owner = owner.clone(), value = value)

    /** Returns a reference to the underlying value. */
    fun asRef(): T = value.asRef()

    /** Converts `self` into a new reference that points at something reachable from the previous. */
    fun <U : Any> map(f: (T) -> U): OwnedFrozenRef<U> = OwnedFrozenRef(owner = owner.clone(), value = value.map(f))

    /** Fallible map the reference to another one. */
    fun <U : Any> tryMapResult(f: (T) -> Result<U>): Result<OwnedFrozenRef<U>> =
        value.tryMapResult(f).map { mapped ->
            OwnedFrozenRef(owner = owner.clone(), value = mapped)
        }

    /** Optionally map the reference to another one. */
    fun <U : Any> tryMapOption(f: (T) -> U?): OwnedFrozenRef<U>? {
        val mapped = value.tryMapOption(f) ?: return null
        return OwnedFrozenRef(owner = owner.clone(), value = mapped)
    }

    /** Get a reference to the owning frozen heap. */
    fun owner(): FrozenHeapRef = owner

    // Kotlin: direct access via asRef()

    override fun toString(): String = value.toString()
}
