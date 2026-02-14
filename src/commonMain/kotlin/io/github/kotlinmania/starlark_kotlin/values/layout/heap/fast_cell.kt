// port-lint: source src/values/layout/heap/fast_cell.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.fast_cell

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

/// Faster but less safe alternative to `RefCell<T>`:
/// all operations except `borrow` are `unsafe` and may lead to undefined behavior.
// #[derive(Debug)]
// pub(crate) struct FastCell<T> {
//     value: UnsafeCell<MaybeUninit<T>>,
//     init: Cell<bool>,
// }
// Kotlin: GC handles memory. We use a nullable wrapper to simulate the init/uninit state.
internal class FastCell<T>(
    private var value: T?,
) {
    // Whether the cell contains a value.
    // Kotlin: tracked implicitly by value != null.
    private var init: Boolean = value != null

    companion object {
        // impl Default for FastCell (T: Default)
        fun <T> default(defaultValue: T): FastCell<T> {
            return FastCell(defaultValue)
        }
    }

    /// Get a reference to the value.
    ///
    /// This operation is safe under assumption that other `unsafe` operations
    /// do not leave self in invalid state.
    // pub(crate) fn borrow(&self) -> &T
    fun borrow(): T {
        check(init) { "FastCell: borrow on uninitialized cell" }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    // pub(crate) fn try_borrow(&self) -> Option<&T>
    fun tryBorrow(): T? {
        return if (init) {
            borrow()
        } else {
            null
        }
    }

    /// Get a mutable reference to the value.
    // pub(crate) unsafe fn get_mut(&self) -> *mut T
    // Kotlin: returns the value directly; caller modifies in-place or via set.
    fun getMut(): T {
        check(init) { "FastCell: get_mut on uninitialized cell" }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    /// Take the value out of the cell.
    // pub(crate) unsafe fn take(&self) -> T
    fun take(): T {
        check(init) { "FastCell: take on uninitialized cell" }
        init = false
        @Suppress("UNCHECKED_CAST")
        val v = value as T
        value = null
        return v
    }

    /// Put the value into the cell.
    // pub(crate) unsafe fn set(&self, value: T)
    fun set(newValue: T) {
        check(!init) { "FastCell: set on already initialized cell" }
        init = true
        value = newValue
    }

    override fun toString(): String {
        return "FastCell(init=$init, value=$value)"
    }
}
