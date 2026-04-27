// port-lint: source src/values/layout/heap/fastCell.rs
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
 * Faster but less safe alternative to a borrow-checked cell: all operations
 * except [borrow] are unchecked and may lead to undefined behaviour if used
 * incorrectly.
 */
internal class FastCell<T>(initialValue: T? = null) {
    /**
     * The value.
     *
     * The idea is this: when we take the value out of the cell,
     * we clear the [value] field. Accessing it afterwards will fail loudly,
     * which is worse than a proper panic but carries no per-borrow runtime cost.
     */
    private var value: T? = initialValue

    /** Whether the cell currently contains a value. */
    private var init: Boolean = initialValue != null

    /** Releases the held value. */
    fun drop() {
        if (init) {
            value = null
            init = false
        }
    }

    /**
     * Get a reference to the value.
     *
     * This operation is safe under the assumption that other unchecked operations
     * do not leave [this] in an invalid state.
     */
    fun borrow(): T {
        check(init)
        return value as T
    }

    fun tryBorrow(): T? {
        return if (init) {
            borrow()
        } else {
            null
        }
    }

    /**
     * Get a mutable reference to the value.
     *
     * It is the caller's responsibility to guarantee there are no other references
     * to the value, and that nobody will obtain a reference while the mutable
     * reference is alive.
     */
    fun getMut(): T {
        check(init)
        return value as T
    }

    /** Take the value out of the cell. */
    fun take(): T {
        check(init)
        init = false
        val v = value as T
        // Clear the [value] field so that accessing it will crash.
        value = null
        return v
    }

    /** Put the value into the cell. */
    fun set(value: T) {
        check(!init)
        init = true
        this.value = value
    }

    companion object {
        /** Constructs a [FastCell] holding [value]. */
        fun <T> default(value: T): FastCell<T> = FastCell(value)
    }
}
