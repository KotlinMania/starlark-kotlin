// port-lint: source src/values/layout/heap/branding.rs
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

/**
 * Documentation-only module to provide an overview of branding in starlark.
 *
 * Starlark values are tied to the heap they are allocated in. Starlark uses branded
 * accesses to the heap to ensure that starlark values cannot escape the heap they
 * are tied to, without needing to reference count access to the heap on each
 * starlark value. This module explains how that works.
 *
 * The most important factor in understanding branding is understanding the scope
 * tag that appears on [Value], [Heap], and similar types. Unusually, this scope
 * tag should not be understood as representing the heap reachability window
 * in a traditional sense. Instead, the scope tag functions as a unique identifier
 * of a particular heap. In other words, the contract is that if there are two
 * [Value]s with the same scope tag, the values are allocated in the same heap.
 *
 * To see how this works in practice, it is best to consider an example of how
 * this might fail. Consider code like this:
 *
 * ```
 * Heap.temp { heap1 ->
 *     Heap.temp { heap2 ->
 *         val s1: Value = heap1.allocStr("abc").toValue()
 *         val v: Value = heap2.alloc(AllocTuple(listOf(s1)))
 *     }
 * }
 * ```
 *
 * This code attempts to first allocate a string value in `heap1` and then allocate
 * a value in `heap2` that references that value. This is an example of the exact
 * kind of "cross-heap confusion" that we want to disallow. The invariant is
 * enforced through the scope of [Heap.temp] together with values that, by
 * convention, do not escape the closure body.
 *
 * ### Implementation
 *
 * "Branding" is a searchable term that will yield lots of online discussion about
 * how to make use of this pattern, but in general implementing this behavior
 * consists of only two parts:
 *
 *  1. [Value], [Heap], and all similar types must be invariant in their scope
 *     tag. This ensures that it is never possible to convert a [Value] from one
 *     heap to a [Value] of another.
 *  2. The "root" of all accesses to heaps must be via "branded closures"
 *     such as [Heap.temp].
 *
 * That is it; combined, this means that user code can never prove that a value
 * coming from one branded closure is the same as a value coming from another.
 *
 * ### References to frozen heaps
 *
 * To support references to values in frozen heaps, we expand the above contract
 * somewhat. Instead of requiring that a [Value] must be allocated in a [Heap]
 * matching its scope tag, we require that it must be allocated in such a [Heap]
 * *or in any frozen heap that the [Heap] depends on.*
 *
 * From the perspective of object reachability this is obviously sane (the [Heap]
 * will keep the dependent frozen heaps alive). We make use of the expanded
 * contract by then providing an API like this:
 *
 * ```
 * fun Heap.accessOwnedFrozenValue(v: OwnedFrozenValue): Value
 * ```
 *
 * `accessOwnedFrozenValue` adds the heap the frozen value is associated with as
 * a dependency of the current heap, and then returns a [Value]; the scope tag
 * in the return value essentially acts as a proof/endorsement that the given
 * value is sound to use "within the context of the current heap."
 */
package io.github.kotlinmania.starlark.values.layout.heap
