// port-lint: source src/values/layout/heap/fast_cell.rs
package io.github.kotlinmania.starlark.values.layout.heap

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

// / Faster but less safe alternative to `RefCell<T>`:
// / all operations except `borrow` are `unsafe` and may lead to undefined behavior.
//     value: UnsafeCell<MaybeUninit<T>>,
//     init: Cell<bool>,
// }
internal class FastCell<T>(
    // / The value.
    private var value: T?,
) {
    // / Whether the cell contains a value or zeros.
    // init: Cell<bool>,
    private var init: Boolean = value != null

    // Kotlin: GC handles drop. No explicit destructor needed.

    companion object {
        fun <T> default(defaultValue: T): FastCell<T> = FastCell(defaultValue)
    }

    // impl FastCell<T>

    // / Get a reference to the value.
    // /
    // / This operation is safe under assumption that other `unsafe` operations
    // / do not leave self in invalid state.
    fun borrow(): T {
        // debug_assert!(self.init.get());
        check(init) { "FastCell: borrow on uninitialized cell" }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    fun tryBorrow(): T? =
        if (init) {
            // Some(self.borrow())
            borrow()
        } else {
            null
        }

    // / Get a mutable reference to the value.
    // /
    // / This function is unsafe because it's caller responsibility to guarantee
    // / there are no other references to the value, and nobody is going
    // / to obtain references to value while mutable reference exists.
    fun getMut(): T {
        // debug_assert!(self.init.get());
        check(init) { "FastCell: getMut on uninitialized cell" }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    // / Take the value out of the cell.
    fun take(): T {
        // assert!(self.init.get());
        check(init) { "FastCell: take on uninitialized cell" }
        // self.init.set(false);
        init = false
        @Suppress("UNCHECKED_CAST")
        val v = value as T
        // Replace the `value` field with zeros so that accessing it will crash.
        value = null
        return v
    }

    // / Put the value into the cell.
    fun set(value: T) {
        // assert!(!self.init.get());
        check(!init) { "FastCell: set on already initialized cell" }
        // self.init.set(true);
        init = true
        // *self.value.get() = MaybeUninit::new(value);
        this.value = value
    }

    override fun toString(): String = "FastCell(init=$init, value=$value)"
}
