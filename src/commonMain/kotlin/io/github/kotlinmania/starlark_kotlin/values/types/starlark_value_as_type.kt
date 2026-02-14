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

import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocProperty
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.type_repr.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.typing.TypeType
import io.github.kotlinmania.starlark_kotlin.values.typing.ty.AbstractType

// #[derive(Debug, NoSerialize, Allocative, ProvidesStaticType)]
// struct StarlarkValueAsTypeStarlarkValue(fn() -> Ty, fn() -> DocItem);
private class StarlarkValueAsTypeStarlarkValue(
    private val tyFn: () -> Ty,
    private val docFn: () -> DocItem,
) : StarlarkValue {

    // #[starlark_value(type = "type")]
    // impl StarlarkValue for StarlarkValueAsTypeStarlarkValue
    override val TYPE: String get() = "type"

    // type Canonical = AbstractType;

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return tyFn()
    }

    // fn documentation(&self) -> DocItem
    override fun documentation(): DocItem {
        return docFn()
    }

    // impl Display for StarlarkValueAsTypeStarlarkValue
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
///
/// const Temperature: StarlarkValueAsType<Temperature> = StarlarkValueAsType::new();
/// ```
// pub struct StarlarkValueAsType<T: StarlarkTypeRepr>(
//     &'static AllocStaticSimple<StarlarkValueAsTypeStarlarkValue>,
//     PhantomData<fn(&T)>,
// );
// Kotlin: holds a lazily-allocated frozen StarlarkValue that acts as a type marker.
class StarlarkValueAsType<T : StarlarkTypeRepr> private constructor(
    private val inner: StarlarkValueAsTypeStarlarkValue,
    private val tyRepr: () -> Ty,
) : StarlarkTypeRepr, AllocValue, AllocFrozenValue {

    companion object {
        // pub const fn new() -> Self
        // where T: StarlarkValue<'static>
        /// Constructor.
        ///
        /// Use [newNoDocs] if `T` is not a `StarlarkValue`.
        inline fun <reified T> new(): StarlarkValueAsType<T>
            where T : StarlarkTypeRepr, T : StarlarkValue
        {
            val tyFn = { StarlarkTypeRepr.typeReprOf<T>() }
            val docFn = { DocItem.Type(DocType.fromStarlarkValue<T>()) }
            return StarlarkValueAsType(
                inner = StarlarkValueAsTypeStarlarkValue(tyFn, docFn),
                tyRepr = tyFn,
            )
        }

        // pub const fn new_no_docs() -> Self
        /// Constructor.
        inline fun <reified T : StarlarkTypeRepr> newNoDocs(): StarlarkValueAsType<T> {
            val tyFn = { StarlarkTypeRepr.typeReprOf<T>() }
            val docFn = {
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

    // impl Debug for StarlarkValueAsType
    // fn fmt(&self, f: &mut Formatter) -> fmt::Result
    // impl Display for StarlarkValueAsType
    override fun toString(): String {
        return tyRepr().toString()
    }

    // impl StarlarkTypeRepr for StarlarkValueAsType
    // type Canonical = <TypeType as StarlarkTypeRepr>::Canonical;
    // fn starlark_type_repr() -> Ty
    override fun starlarkTypeRepr(): Ty {
        return TypeType.starlarkTypeRepr()
    }

    // impl AllocValue for StarlarkValueAsType
    // fn alloc_value(self, _heap: Heap) -> Value
    override fun allocValue(heap: Heap): Value {
        return heap.allocSimple(inner)
    }

    // impl AllocFrozenValue for StarlarkValueAsType
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(inner)
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
