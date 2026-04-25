// port-lint: source src/values/types/starlark_value_as_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.starlark_value_as_type

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

//! Convert a value implementing [`StarlarkValue`] into a type usable in type expression.

// use std::fmt;
// use std::fmt::Debug;
// use std::fmt::Display;
// use std::fmt::Formatter;
// use std::marker::PhantomData;

// use allocative::Allocative;
// use starlark_derive::NoSerialize;
// use starlark_derive::starlark_value;

// use crate as starlark;
// use crate::any::ProvidesStaticType;
// use crate::docs::DocItem;
// use crate::docs::DocMember;
// use crate::docs::DocProperty;
// use crate::docs::DocType;
// use crate::typing::Ty;
// use crate::values::AllocFrozenValue;
// use crate::values::AllocStaticSimple;
// use crate::values::AllocValue;
// use crate::values::FrozenHeap;
// use crate::values::FrozenValue;
// use crate::values::Heap;
// use crate::values::StarlarkValue;
// use crate::values::Value;
// use crate::values::type_repr::StarlarkTypeRepr;
// use crate::values::typing::TypeType;
// use crate::values::typing::ty::AbstractType;

import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocProperty
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.typing.ty.AbstractType

// #[derive(Debug, NoSerialize, Allocative, ProvidesStaticType)]
// struct StarlarkValueAsTypeStarlarkValue(fn() -> Ty, fn() -> DocItem);
@PublishedApi
internal class StarlarkValueAsTypeStarlarkValue(
    private val tyFn: () -> Ty,
    private val docFn: () -> DocItem,
) : StarlarkValue {

    // #[starlark_value(type = "type")]
    // impl<'v> StarlarkValue<'v> for StarlarkValueAsTypeStarlarkValue {
    //     type Canonical = AbstractType;
    override val TYPE: String get() = "type"

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return tyFn()
    }

    // fn documentation(&self) -> DocItem
    override fun documentation(): DocItem {
        return docFn()
    }

    // impl Display for StarlarkValueAsTypeStarlarkValue
    //     fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result
    override fun toString(): String {
        return tyFn().toString()
    }
}

/// Utility to declare a value usable in type expression.
///
/// # Example
///
/// ```
/// use starlark::values::starlark_value_as_type::StarlarkValueAsType;
/// const Temperature: StarlarkValueAsType<Temperature> = StarlarkValueAsType::new();
/// ```
// pub struct StarlarkValueAsType<T: StarlarkTypeRepr>(
//     &'static AllocStaticSimple<StarlarkValueAsTypeStarlarkValue>,
//     PhantomData<fn(&T)>,
// );
class StarlarkValueAsType<T : StarlarkTypeRepr> @PublishedApi internal constructor(
    @PublishedApi internal val inner: StarlarkValueAsTypeStarlarkValue,
    private val tyRepr: () -> Ty,
) : StarlarkTypeRepr, AllocValue, AllocFrozenValue {

    // impl<T: StarlarkTypeRepr> StarlarkValueAsType<T>
    companion object {
        /// Constructor.
        ///
        /// Use [`new_no_docs`](Self::new_no_docs) if `T` is not a `StarlarkValue`.
        // pub const fn new() -> Self where T: StarlarkValue<'static>
        inline fun <reified T> new(instance: T): StarlarkValueAsType<T>
            where T : StarlarkTypeRepr, T : StarlarkValue
        {
            val tyFn: () -> Ty = { instance.getTypeStarlarkRepr() }
            val docFn: () -> DocItem = { DocItem.Type(DocType.fromStarlarkValue(instance)) }
            return StarlarkValueAsType(
                inner = StarlarkValueAsTypeStarlarkValue(tyFn, docFn),
                tyRepr = tyFn,
            )
        }

        /// Constructor.
        // pub const fn new_no_docs() -> Self
        inline fun <reified T : StarlarkTypeRepr> newNoDocs(instance: T): StarlarkValueAsType<T> {
            val tyFn: () -> Ty = { instance.starlarkTypeRepr() }
            val docFn: () -> DocItem = {
                DocItem.Member(
                    DocMember.Property(
                        DocProperty(
                            docs = null,
                            typ = AbstractType.starlarkTypeRepr(),
                        )
                    )
                )
            }
            return StarlarkValueAsType(
                inner = StarlarkValueAsTypeStarlarkValue(tyFn, docFn),
                tyRepr = tyFn,
            )
        }
    }

    override fun toString(): String {
        return tyRepr().toString()
    }

    // impl<T: StarlarkTypeRepr> StarlarkTypeRepr for StarlarkValueAsType<T>
    //     fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty {
        return AbstractType.starlarkTypeRepr()
    }

    // impl<'v, T: StarlarkTypeRepr> AllocValue<'v> for StarlarkValueAsType<T>
    //     fn alloc_value(self, _heap: Heap<'v>) -> Value<'v>
    override fun allocValue(heap: Heap): Value {
        return heap.allocSimple(inner)
    }

    // impl<T: StarlarkTypeRepr> AllocFrozenValue for StarlarkValueAsType<T>
    //     fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(inner)
    }
}

// #[cfg(test)]
// mod tests {
//     fn test_pass()
//     fn test_fail_compile_time()
//     fn test_fail_runtime()
// }
