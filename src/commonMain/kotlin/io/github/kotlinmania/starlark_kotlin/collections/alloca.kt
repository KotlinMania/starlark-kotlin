// port-lint: source src/collections/alloca.rs
package io.github.kotlinmania.starlark_kotlin.collections.alloca

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

/// We'd love to use the real `alloca`, but don't want to blow through the stack space,
/// so define our own wrapper.
/// We use a single continuous buffer. When it needs upgrading, we double it and keep the old one around.
// pub(crate) struct Alloca {
//     alloc: Cell<*mut Align>,
//     end: Cell<*mut Align>,
//     buffers: RefCell<Vec<Buffer>>,
// }
// Kotlin: GC handles all memory allocation. The Alloca API is preserved
// using simple array/list-based allocation for API compatibility.
class Alloca {
    companion object {
        // const INITIAL_SIZE: usize = 1000000;
        private const val INITIAL_SIZE: Int = 1000000
    }

    constructor() : this(INITIAL_SIZE)

    // pub fn with_capacity(size_bytes: usize) -> Self
    constructor(@Suppress("UNUSED_PARAMETER") sizeBytes: Int) {
        // Kotlin: capacity hint is unused; GC manages memory.
    }

    /// Note that the `Drop` for the `T` will not be called. That's safe if there is no `Drop`,
    /// or you call it yourself.
    // pub fn alloca_uninit<T, R>(&self, len: usize, k: impl FnOnce(&mut [MaybeUninit<T>]) -> R) -> R
    // Kotlin: uses Array<Any?> with nulls as "uninitialized"
    @Suppress("UNCHECKED_CAST")
    fun <T, R> allocaUninit(len: Int, k: (Array<Any?>) -> R): R {
        val data = arrayOfNulls<Any>(len)
        return k(data)
    }

    // pub fn alloca_init<T, R>(&self, len, init, k) -> R
    @Suppress("UNCHECKED_CAST")
    fun <T, R> allocaInit(len: Int, init: () -> T, k: (MutableList<T>) -> R): R {
        val data = MutableList(len) { init() }
        return k(data)
    }

    // pub fn alloca_fill<T: Copy, R>(&self, len, fill, k) -> R
    fun <T, R> allocaFill(len: Int, fill: T, k: (MutableList<T>) -> R): R {
        return allocaInit(len, { fill }, k)
    }

    /// Concat two slices and invoke the callback with the result.
    /// Use either slice as is if the other is empty,
    /// otherwise clone the elements into a temporary slice.
    // pub(crate) fn alloca_concat<T: Clone, R, F>(&self, x: &[T], y: &[T], k: F) -> R
    fun <T, R> allocaConcat(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        return if (x.isEmpty()) {
            k(y)
        } else if (y.isEmpty()) {
            k(x)
        } else {
            allocaConcatSlow(x, y, k)
        }
    }

    // fn alloca_concat_slow<T: Clone, R, F>(&self, x: &[T], y: &[T], k: F) -> R
    private fun <T, R> allocaConcatSlow(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        val xy = ArrayList<T>(x.size + y.size)
        xy.addAll(x)
        xy.addAll(y)
        return k(xy)
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
