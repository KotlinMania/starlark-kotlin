// port-lint: source src/collections/alloca.rs
package io.github.kotlinmania.starlark_kotlin.collections

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

// In Rust, Alloca is a custom arena allocator that manages raw memory using
// pointer arithmetic and manual allocation/deallocation. It maintains a
// contiguous buffer that grows by doubling when capacity is exceeded.
//
// In Kotlin, the garbage collector handles all memory management. We preserve
// the Alloca API surface using simple list/array-based allocation for
// compatibility with code that depends on the alloca pattern (allocate a
// temporary slice, use it in a callback, then release).

// Rust: const INITIAL_SIZE: usize = 1000000; // ~ 1Mb
private const val INITIAL_SIZE: Int = 1000000

// Rust: type Align = u64;
// Rust: const ALIGN: usize = mem::size_of::<Align>();
// Not needed in Kotlin — alignment is managed by the runtime.

// Rust: struct Buffer { ptr: NonNull<u8>, layout: Layout }
// Not needed in Kotlin — GC manages allocations.

/**
 * A reusable arena-style allocator for temporary slices.
 *
 * In Rust, this manages a contiguous memory buffer with bump-pointer allocation,
 * doubling the buffer when capacity is exceeded. Allocations are scoped to
 * callbacks: the memory is logically "freed" when the callback returns.
 *
 * In Kotlin, the GC handles memory, so this class provides API-compatible
 * wrappers that create temporary collections for callback-scoped use.
 * The capacity parameter is accepted for API compatibility but does not
 * affect behavior.
 */
internal class Alloca {

    companion object {
        /** Default initial capacity in bytes (~1MB). Unused in Kotlin but preserved for API parity. */
        private const val DEFAULT_SIZE: Int = INITIAL_SIZE
    }

    /**
     * Create a new [Alloca] with the default capacity.
     */
    constructor() : this(DEFAULT_SIZE)

    /**
     * Create a new [Alloca] with the given capacity hint.
     *
     * In Rust, this pre-allocates a contiguous buffer of [sizeBytes] bytes.
     * In Kotlin, the capacity hint is accepted for API compatibility but
     * does not affect behavior since the GC manages memory.
     */
    constructor(@Suppress("UNUSED_PARAMETER") sizeBytes: Int) {
        // Kotlin: capacity hint is unused; GC manages memory.
    }

    // Rust: fn assert_state(&self)
    // Not needed in Kotlin — no pointer invariants to check.

    // Rust: fn allocate_more(&self, len: usize, one: Layout)
    // Not needed in Kotlin — GC handles growth.

    // Rust: fn rem_in_words_to_rem_in_t<T>(rem_in_words: usize) -> usize
    // Not needed in Kotlin — no word-level capacity tracking.

    // Rust: fn len_in_to_to_len_in_words<T>(len: usize) -> usize
    // Not needed in Kotlin — no word-level capacity tracking.

    /**
     * Allocate an uninitialized array of [len] elements and pass it to [k].
     *
     * In Rust, this returns a `&mut [MaybeUninit<T>]` — a slice of potentially
     * uninitialized memory. The caller is responsible for initializing elements
     * before reading them, and `Drop` is not called on the elements.
     *
     * In Kotlin, all values are initialized by construction. We use an
     * `Array<Any?>` filled with nulls to represent the "uninitialized" state.
     * The callback receives the array and returns a result.
     *
     * @param len Number of elements to allocate.
     * @param k Callback that receives the uninitialized array and produces a result.
     * @return The result produced by [k].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T, R> allocaUninit(len: Int, k: (Array<Any?>) -> R): R {
        val data = arrayOfNulls<Any>(len)
        return k(data)
    }

    /**
     * Allocate and initialize an array of [len] elements, then pass it to [k].
     *
     * Each element is initialized by calling [init]. The resulting mutable list
     * is passed to the callback.
     *
     * In Rust, this calls `alloca_uninit` internally and writes each element
     * using `MaybeUninit::write`, then transmutes to an initialized slice.
     *
     * @param len Number of elements to allocate.
     * @param init Factory function called once per element to produce its initial value.
     * @param k Callback that receives the initialized mutable list and produces a result.
     * @return The result produced by [k].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T, R> allocaInit(len: Int, init: () -> T, k: (MutableList<T>) -> R): R {
        val data = MutableList(len) { init() }
        return k(data)
    }

    /**
     * Allocate an array of [len] elements, all set to [fill], then pass it to [k].
     *
     * This is a convenience wrapper around [allocaInit] for copyable values.
     *
     * @param len Number of elements to allocate.
     * @param fill The value to fill every element with.
     * @param k Callback that receives the filled mutable list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaFill(len: Int, fill: T, k: (MutableList<T>) -> R): R {
        return allocaInit(len, { fill }, k)
    }

    /**
     * Concatenate two lists and invoke [k] with the result.
     *
     * If either list is empty, the other is passed directly to [k] without copying.
     * Otherwise, the elements are cloned into a new temporary list.
     *
     * @param x First list.
     * @param y Second list.
     * @param k Callback that receives the concatenated list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaConcat(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        return if (x.isEmpty()) {
            k(y)
        } else if (y.isEmpty()) {
            k(x)
        } else {
            allocaConcatSlow(x, y, k)
        }
    }

    /**
     * Slow path for [allocaConcat]: both lists are non-empty, so we must
     * clone elements into a new combined list.
     *
     * In Rust, this uses `alloca_uninit` to allocate space for `x.len() + y.len()`
     * elements, then clones both slices into the uninitialized memory with
     * explicit drop guards. In Kotlin, we simply create an ArrayList and
     * add both lists.
     */
    private fun <T, R> allocaConcatSlow(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        val xy = ArrayList<T>(x.size + y.size)
        xy.addAll(x)
        xy.addAll(y)
        return k(xy)
    }
}
