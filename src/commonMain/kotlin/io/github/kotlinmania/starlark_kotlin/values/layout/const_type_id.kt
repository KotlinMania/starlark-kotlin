// port-lint: source src/values/layout/const_type_id.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.const_type_id

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

import kotlin.reflect.KClass

/// `TypeId` wrapper/provider until `const_type_id` feature is stabilized.
// #[derive(Copy, Clone, Dupe, Allocative)]
// pub(crate) struct ConstTypeId {
//     #[cfg(rust_nightly)]
//     type_id: TypeId,
//     #[cfg(not(rust_nightly))]
//     type_id_fn: fn() -> TypeId,
// }
// Kotlin: KClass serves as the type identifier.
internal class ConstTypeId(
    private val typeId: KClass<*>,
) {
    companion object {
        // pub(crate) const fn of<T: ?Sized + 'static>() -> ConstTypeId
        inline fun <reified T : Any> of(): ConstTypeId {
            return ConstTypeId(T::class)
        }
    }

    // #[inline]
    // pub(crate) fn get(self) -> TypeId
    fun get(): KClass<*> = typeId

    // impl Debug for ConstTypeId
    override fun toString(): String {
        return "ConstTypeId(type_id=$typeId)"
    }

    // impl PartialEq for ConstTypeId
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConstTypeId) return false
        return typeId == other.typeId
    }

    // impl Hash for ConstTypeId
    override fun hashCode(): Int = typeId.hashCode()
}
