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

// use std::cell::Ref;
// use std::cell::RefCell;
// use std::ptr;

// Kotlin equivalent of std::cell::Ref - a shared borrow guard for RefCell.
internal class Ref<T> internal constructor(
    private val refCell: RefCell<T>,
    val value: T,
) {
    private var active: Boolean = true

    fun get(): T {
        check(active) { "Ref is no longer active" }
        return value
    }

    // Equivalent of Rust Drop for Ref.
    fun close() {
        if (active) {
            active = false
            refCell.releaseBorrow()
        }
    }

    // Equivalent of mem::forget on a Ref.
    fun leak() {
        active = false
    }
}

// Kotlin equivalent of std::cell::RefMut - a mutable borrow guard for RefCell.
internal class RefMut<T> internal constructor(
    private val refCell: RefCell<T>,
    val value: T,
) {
    private var active: Boolean = true

    fun get(): T {
        check(active) { "RefMut is no longer active" }
        return value
    }

    // Equivalent of Rust Drop for RefMut.
    fun close() {
        if (active) {
            active = false
            refCell.releaseBorrowMut()
        }
    }
}

// Kotlin equivalent of std::cell::RefCell - interior mutability with runtime borrow checking.
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

    internal fun releaseBorrow() {
        check(borrowCount > 0) { "RefCell is not borrowed" }
        borrowCount -= 1
    }

    internal fun releaseBorrowMut() {
        check(borrowedMut) { "RefCell is not mutably borrowed" }
        borrowedMut = false
    }
}

/// "Unleak" previously leaked `RefCell` borrow (which is `Ref`).
// #[inline]
// pub(crate) unsafe fn unleak_borrow<T: ?Sized>(ref_cell: &RefCell<T>)
internal fun <T> unleakBorrow(refCell: RefCell<T>) {
    // Note this call contains a runtime assertion that the `RefCell` is borrowed.
    val r = refCell.borrow()
    // unsafe {
    //     // Drop `b` twice to decrement the borrow counter.
    //     drop::<Ref<T>>(ptr::read(&r as *const Ref<T>));
    //     drop::<Ref<T>>(r);
    // }
    // In Kotlin we directly release the borrow once (for the leaked Ref)
    // and then close `r` (for the borrow we just took), netting -1 to the count.
    refCell.releaseBorrow()
    r.close()
}

// #[cfg(test)]
// mod tests {
//     use std::cell::RefCell;
//     use std::mem;
//
//     #[test]
//     fn test_unleak_borrow() {
//         let ref_cell = RefCell::new(1);
//         assert!(ref_cell.try_borrow_mut().is_ok());
//         mem::forget(ref_cell.borrow());
//         assert!(
//             ref_cell.try_borrow_mut().is_err(),
//             "RefCell is borrowed, so we cannot borrow it mutably"
//         );
//         unsafe { super::unleak_borrow(&ref_cell) };
//         assert!(
//             ref_cell.try_borrow_mut().is_ok(),
//             "Borrow is unleaked, so we can borrow it mutably"
//         );
//
//         // Now do the same twice.
//
//         mem::forget(ref_cell.borrow());
//         mem::forget(ref_cell.borrow());
//
//         assert!(
//             ref_cell.try_borrow_mut().is_err(),
//             "RefCell is borrowed, so we cannot borrow it mutably"
//         );
//         unsafe { super::unleak_borrow(&ref_cell) };
//         assert!(
//             ref_cell.try_borrow_mut().is_err(),
//             "RefCell is still borrowed"
//         );
//         unsafe { super::unleak_borrow(&ref_cell) };
//         assert!(ref_cell.try_borrow_mut().is_ok());
//     }
// }
