// port-lint: source src/values/layout/const_type_id.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

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

// use std::any::TypeId;
// use std::fmt;
// use std::fmt::Debug;
// use std::fmt::Formatter;
// use std::hash::Hash;
// use std::hash::Hasher;

// use allocative::Allocative;
// use dupe::Dupe;

import kotlin.reflect.KClass

/// `TypeId` wrapper/provider until `const_type_id` feature is stabilized.
// #[derive(Copy, Clone, Dupe, Allocative)]
// #[allocative(skip)]
// pub(crate) struct ConstTypeId {
//     #[cfg(rust_nightly)]
//     type_id: TypeId,
//     #[cfg(not(rust_nightly))]
//     type_id_fn: fn() -> TypeId,
// }
data class ConstTypeId(
    // Kotlin: Uses KClass for type identification instead of Rust's TypeId.
    private val klass: KClass<*>,
) {

    // impl Debug for ConstTypeId
    // fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    //     f.debug_struct("ConstTypeId").field("type_id", &self.get()).finish()
    override fun toString(): String {
        return "ConstTypeId(type_id=${get()})"
    }

    // impl PartialEq for ConstTypeId
    // #[inline]
    // fn eq(&self, other: &Self) -> bool
    //     self.get() == other.get()
    // Handled by data class equals()

    // impl Eq for ConstTypeId {}
    // Handled by data class equals()

    // impl Hash for ConstTypeId
    // fn hash<H: Hasher>(&self, state: &mut H)
    //     self.get().hash(state)
    // Handled by data class hashCode()

    // impl ConstTypeId

    // pub(crate) const fn of<T: ?Sized + 'static>() -> ConstTypeId
    companion object {
        inline fun <reified T : Any> of(): ConstTypeId = ConstTypeId(T::class)

        fun of(klass: KClass<*>): ConstTypeId = ConstTypeId(klass)
    }

    // #[inline]
    // pub(crate) fn get(self) -> TypeId
    fun get(): KClass<*> = klass
}
