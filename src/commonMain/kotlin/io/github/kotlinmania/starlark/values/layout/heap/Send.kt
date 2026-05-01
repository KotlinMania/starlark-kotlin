// port-lint: source src/values/layout/heap/send.rs
package io.github.kotlinmania.starlark.values.layout.heap

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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

import io.github.kotlinmania.starlark.any.ProvidesStaticType
import io.github.kotlinmania.starlark.values.Trace
import kotlin.reflect.KClass

/**
 * A trait for handling the unusual sendness requirements of starlark values.
 *
 * Semantically, the send and sync impls in starlark exist in support of the following goals,
 * from more obvious to less obvious:
 *
 *  1. Frozen heaps and values should be fully thread safe — parallel starlark evaluations must
 *     be able to depend on the same frozen heap.
 *  2. Unfrozen values should support non-thread-safe interior mutability such as a borrow
 *     checked cell; in other words, they must support being non-sync.
 *  3. Unfrozen heaps should be sendable. Concretely, it should be possible to hold them across
 *     a suspension point so as to interleave starlark evaluation with other async work.
 *
 * Since a value is semantically a reference to an unfrozen value, an immediate consequence
 * of (2) is that a value reference cannot itself be sent. Given that starlark values need to
 * contain other values, this makes the third condition weird. However, soundness is achieved
 * by requiring a combination of two properties:
 *
 *  a. Unfrozen starlark values must be send up to any value reference inside them. In other
 *     words, they can contain a list of value references or whatever, but not arbitrary
 *     non-sendable interior state.
 *  b. When a heap is sent to another thread, the heap is sent "in its entirety", together
 *     with any references into it.
 *
 * To be able to check these requirements, the heap allocation functions have a signature that
 * conceptually looks like this:
 *
 * ```
 * fun <T> Heap.alloc(value: T): Value
 *     where T : StarlarkValue,
 *           T : ProvidesStaticType,
 *           T.staticType : HeapSendable
 * ```
 *
 * The first bound is obvious. The second pins `T` to the same heap as its contained value
 * references via [ProvidesStaticType]; together with branding, this guarantees that any
 * value references that `T` holds point back into the same heap. The third bound replaces a
 * direct thread-shareability requirement (which would prevent `T` from holding a value
 * reference) with one on `T.staticType` — the heap-detached static representative of `T`.
 * That is *almost* as good as direct thread-shareability, while remaining satisfied for a
 * `T` that contains a value reference, which is what we want.
 */
interface HeapSendable : ProvidesStaticType

/**
 * The sync analogue of [HeapSendable].
 *
 * Mostly see the docs on [HeapSendable], which is slightly more interesting — this one is
 * just needed on frozen heaps.
 */
interface HeapSyncable : ProvidesStaticType
