// port-lint: source src/values/layout/heap.rs
@file:Suppress("unused")

package io.github.kotlinmania.starlark_kotlin.values.layout

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

/**
 * Starlark heap implementation.
 *
 * This module organizes the heap subsystem into the following submodules:
 *
 * - **allocator** -- Low-level arena allocators (bump allocation, chunk management).
 *   Package: `values.layout.heap.allocator`
 *   Files: `Allocator.kt`, `Api.kt`, `Bumpalo.kt`, and `allocator/alloc/`
 *
 * - **arena** -- Arena-based value storage with ordered/unordered iteration and GC support.
 *   Package: `values.layout.heap.arena`
 *   File: `Arena.kt`
 *
 * - **branding** -- Documentation-only module explaining Rust's lifetime branding pattern
 *   and how it maps to Kotlin's GC-based ownership model.
 *   Package: `values.layout.heap`
 *   File: `Branding.kt`
 *
 * - **call_enter_exit** -- Marker objects ([CallEnter], [CallExit]) allocated on the heap
 *   to track function call boundaries for profiling.
 *   Package: `values.layout.heap`
 *   File: `CallEnterExit.kt`
 *
 * - **fast_cell** -- A faster but less safe alternative to `RefCell`, used for
 *   interior mutability of heap-internal state.
 *   Package: `values.layout.heap`
 *   File: `FastCell.kt`
 *
 * - **heap_type** -- Core heap types: [Heap][io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap]
 *   (unfrozen, mutable), [FrozenHeap][io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap],
 *   [FrozenHeapRef][io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeapRef],
 *   [Tracer][io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer] (for GC),
 *   and [HeapKind][io.github.kotlinmania.starlark_kotlin.values.layout.heap.HeapKind].
 *   Package: `values.layout.heap`
 *   File: `HeapType.kt`
 *
 * - **maybe_uninit_slice_util** -- Utility for safely populating arrays from iterators
 *   with exception-safe fallback writes.
 *   Package: `values.layout.heap.maybe_uninit_slice_util`
 *   File: `MaybeUninitSliceUtil.kt`
 *
 * - **profile** -- Heap profiling: allocation summaries, flame graphs, per-function
 *   aggregation, and string interning for profile output.
 *   Package: `values.layout.heap.profile`
 *   Files: `Profile.kt`, `Aggregated.kt`, `AllocCounts.kt`, `ByType.kt`,
 *   `StringIndex.kt`, `SummaryByFunction.kt`
 *
 * - **repr** -- Low-level heap object representation:
 *   [AValueHeader][io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader],
 *   [AValueRepr][io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueRepr],
 *   [AValueOrForward][io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueOrForward],
 *   [ForwardPtr][io.github.kotlinmania.starlark_kotlin.values.layout.heap.ForwardPtr],
 *   and GC forwarding structures.
 *   Package: `values.layout.heap`
 *   File: `Repr.kt`
 *
 * - **send** -- Thread-safety marker interfaces
 *   ([HeapSendable][io.github.kotlinmania.starlark_kotlin.values.layout.heap.HeapSendable],
 *   [HeapSyncable][io.github.kotlinmania.starlark_kotlin.values.layout.heap.HeapSyncable]) and
 *   [DynStarlark][io.github.kotlinmania.starlark_kotlin.values.layout.heap.DynStarlark] wrapper.
 *   In Kotlin, these are largely no-op markers since the JVM memory model handles
 *   cross-thread visibility.
 *   Package: `values.layout.heap`
 *   File: `Send.kt`
 */

// Submodules:
// pub(crate) mod allocator -> values.layout.heap.allocator (Allocator.kt, Api.kt, Bumpalo.kt, alloc/)
// pub(crate) mod arena      -> values.layout.heap.arena (Arena.kt)
// mod branding              -> values.layout.heap (Branding.kt) [private]
// pub(crate) mod call_enter_exit -> values.layout.heap (CallEnterExit.kt)
// mod fast_cell             -> values.layout.heap (FastCell.kt) [private]
// pub(crate) mod heap_type  -> values.layout.heap (HeapType.kt)
// pub(crate) mod maybe_uninit_slice_util -> values.layout.heap.maybe_uninit_slice_util (MaybeUninitSliceUtil.kt)
// pub(crate) mod profile    -> values.layout.heap.profile (Profile.kt, Aggregated.kt, etc.)
// pub(crate) mod repr       -> values.layout.heap (Repr.kt)
// pub(crate) mod send       -> values.layout.heap (Send.kt)
