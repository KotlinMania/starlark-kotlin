// port-lint: source src/util/refcell.rs
package io.github.kotlinmania.starlark_kotlin.util

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

/**
 * "Unleak" previously leaked `RefCell` borrow (which is `Ref`).
 *
 * In Rust, this unsafely drops a borrow reference twice to decrement
 * the RefCell borrow counter, recovering from a `mem::forget`-ed borrow.
 *
 * In Kotlin, there is no RefCell / borrow-checker mechanism.
 * This function is a no-op placeholder preserving the API surface
 * for callers that need to "unleak" a borrow in the Rust port.
 */
// #[inline]
// pub(crate) unsafe fn unleak_borrow<T: ?Sized>(ref_cell: &RefCell<T>)
internal fun <T> unleakBorrow(@Suppress("UNUSED_PARAMETER") refCell: Any) {
    // Note this call contains a runtime assertion that the `RefCell` is borrowed.
    // let r = ref_cell.borrow();
    // unsafe {
    //     // Drop `b` twice to decrement the borrow counter.
    //     drop::<Ref<T>>(ptr::read(&r as *const Ref<T>));
    //     drop::<Ref<T>>(r);
    // }
    //
    // Kotlin: No borrow-counting mechanism to manage. No-op.
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
