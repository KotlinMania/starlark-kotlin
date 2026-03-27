// port-lint: source src/util/refcell.rs
package io.github.kotlinmania.starlark_kotlin.util.refcell

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

// TODO: stub - RefCell needs real import
internal class RefCell<T>(
    private val value: T,
) {
    private var borrowCount: Int = 0
    private var borrowedMut: Boolean = false

    fun borrow(): Ref<T> {
        if (borrowedMut) {
            error("RefCell is mutably borrowed")
        }
        borrowCount += 1
        return Ref(this, value)
    }

    fun tryBorrowMut(): RefMut<T>? {
        if (borrowedMut || borrowCount != 0) {
            return null
        }
        borrowedMut = true
        return RefMut(this, value)
    }

    internal fun releaseBorrow() {
        if (borrowCount <= 0) {
            error("RefCell is not borrowed")
        }
        borrowCount -= 1
    }

    internal fun releaseBorrowMut() {
        if (!borrowedMut) {
            error("RefCell is not mutably borrowed")
        }
        borrowedMut = false
    }

    internal fun unleakBorrow() {
        releaseBorrow()
    }
}

// TODO: stub - Ref needs real import
internal class Ref<T> internal constructor(
    private val refCell: RefCell<T>,
    private val value: T,
) {
    private var active: Boolean = true

    fun get(): T {
        return value
    }

    fun close() {
        if (active) {
            active = false
            refCell.releaseBorrow()
        }
    }

    fun leak() {
        active = false
    }
}

internal class RefMut<T> internal constructor(
    private val refCell: RefCell<T>,
    private val value: T,
) {
    private var active: Boolean = true

    fun get(): T {
        return value
    }

    fun close() {
        if (active) {
            active = false
            refCell.releaseBorrowMut()
        }
    }
}

/// "Unleak" previously leaked `RefCell` borrow (which is `Ref`).
// #[inline]
// pub(crate) unsafe fn unleak_borrow<T: ?Sized>(ref_cell: &RefCell<T>)
internal fun unleakBorrow(refCell: RefCell<*>) {
    // Note this call contains a runtime assertion that the `RefCell` is borrowed.
    val r = refCell.borrow()
    // Drop `r` twice to decrement the borrow counter.
    refCell.unleakBorrow()
    r.close()
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
