// port-lint: source src/values/layout/heap/send.rs
package io.github.kotlinmania.starlark.values.layout.heap

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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

/**
 * A trait for handling the unusual sendness requirements of starlark values.
 *
 * Semantically, the send and sync impls in starlark exist in support of the
 * following goals, from more obvious to less obvious:
 *
 *  1. Frozen heaps and values should be fully thread safe — parallel starlark
 *     evaluations must be able to depend on the same frozen heap.
 *  2. Unfrozen values should support non-thread-safe interior mutability such
 *     as a borrow-checked cell; in other words, they must support being
 *     non-sync.
 *  3. Unfrozen heaps should be sendable. Concretely, it should be possible to
 *     hold them across a suspension point so as to interleave starlark
 *     evaluation with other async work.
 *
 * Since a value is semantically a reference to an unfrozen value, an immediate
 * consequence of (2) is that an unfrozen value reference cannot itself be
 * sent. Given that starlark values need to contain other values, this makes
 * the third condition weird. However, soundness is achieved by requiring a
 * combination of two properties:
 *
 *  a. Unfrozen starlark values must be send up to any value reference inside
 *     them. In other words, they can contain a list of value references or
 *     similar, but not arbitrary non-sendable interior state.
 *  b. When a heap is sent to another thread, the heap is sent "in its
 *     entirety", together with any references into it.
 *
 * To be able to check these requirements, the heap allocation functions have
 * a signature that conceptually looks like:
 *
 * ```
 * fun <T> Heap.alloc(value: T): Value
 *     where T : StarlarkValue,
 *           T : ProvidesStaticType,
 *           T.StaticType : Send
 * ```
 *
 * The first bound is obvious. The second says that all lifetimes appearing on
 * `T` must match the heap's lifetime; together with branding, this guarantees
 * that any value references that `T` holds point back into the same heap. The
 * third bound replaces a direct `T : Send` requirement (which would prevent
 * `T` from holding a value reference) with one on `T.StaticType` — `T` with
 * its lifetime parameter replaced by a static lifetime. That is *almost* as
 * good as `T : Send`, while remaining satisfied for a `T` that contains an
 * unfrozen value reference, which is what we want.
 */
interface HeapSendable : SealedSend

/** Sealing supertype: only types in this module may declare [HeapSendable]. */
interface SealedSend

/**
 * The sync analogue of [HeapSendable].
 *
 * Mostly see the docs on [HeapSendable], which are slightly more interesting —
 * this one is just needed on frozen heaps.
 */
interface HeapSyncable : SealedSync

/** Sealing supertype: only types in this module may declare [HeapSyncable]. */
interface SealedSync

/**
 * A helper to pass the send-if-static property through dynamic dispatch.
 *
 * Unfortunately, the property of starlark values that they are send when the
 * heap lifetime is replaced by a static one does not cleanly pass through an
 * open-interface field; concretely, it's not possible to make an arbitrary
 * `MyTrait` reference sendable just because the underlying concrete type is.
 *
 * This type acts as a wrapper to recover the send implementation; where previously you
 * might have written
 *
 * ```
 * interface MyTrait : Trace
 *
 * class MyValue {
 *     val field: MyTrait
 * }
 * ```
 *
 * Now write instead:
 *
 * ```
 * interface MyTrait : Trace, HeapSendable
 *
 * class MyValue {
 *     val field: DynStarlark<MyTrait>
 * }
 * ```
 */
class DynStarlark<T>(private val value: T) {
    companion object {
        /** Create a new [DynStarlark] containing the value. */
        fun <T> new(v: T): DynStarlark<T> = DynStarlark(v)
    }

    /** Extract the contained value. */
    fun intoInner(): T = value

    /** Borrow the contained value. */
    fun deref(): T = value

    /** Borrow the contained value mutably. */
    fun derefMut(): T = value

    override fun toString(): String = value.toString()
}
