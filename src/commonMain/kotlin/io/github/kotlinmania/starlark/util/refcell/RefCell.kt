// port-lint: source src/util/refcell.rs
package io.github.kotlinmania.starlark.util.refcell

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

/** A shared borrow guard for [RefCell], equivalent to Rust's `std::cell::Ref`. */
internal class Ref<T> internal constructor(
    private val refCell: RefCell<T>,
    val value: T,
) {
    private var active: Boolean = true

    fun get(): T {
        check(active) { "Ref is no longer active" }
        return value
    }

    fun close() {
        if (active) {
            active = false
            refCell.releaseBorrow()
        }
    }

    /** Equivalent of `mem::forget` on a `Ref`. */
    fun leak() {
        active = false
    }
}

/** A mutable borrow guard for [RefCell], equivalent to Rust's `std::cell::RefMut`. */
internal class RefMut<T> internal constructor(
    private val refCell: RefCell<T>,
    val value: T,
) {
    private var active: Boolean = true

    fun get(): T {
        check(active) { "RefMut is no longer active" }
        return value
    }

    fun close() {
        if (active) {
            active = false
            refCell.releaseBorrowMut()
        }
    }
}

/** Interior mutability with runtime borrow checking, equivalent to Rust's `std::cell::RefCell`. */
internal class RefCell<T>(
    private val value: T,
) {
    private var borrowCount: Int = 0
    private var borrowedMut: Boolean = false

    fun borrow(): Ref<T> {
        check(!borrowedMut) { "RefCell is mutably borrowed" }
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

    fun getMut(): T = value

    internal fun releaseBorrow() {
        check(borrowCount > 0) { "RefCell is not borrowed" }
        borrowCount -= 1
    }

    internal fun releaseBorrowMut() {
        check(borrowedMut) { "RefCell is not mutably borrowed" }
        borrowedMut = false
    }
}

/** "Unleak" a previously leaked [RefCell] borrow (decrement the borrow count by one). */
internal fun <T> unleakBorrow(refCell: RefCell<T>) {
    val r = refCell.borrow()
    refCell.releaseBorrow()
    r.close()
}
