// port-lint: source src/values/types/ellipsis.rs
package io.github.kotlinmania.starlark_kotlin.values.types.ellipsis

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

import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue

// #[derive(Allocative, NoSerialize, Debug, derive_more::Display, ProvidesStaticType)]
// #[display("Ellipsis")]
// pub(crate) struct Ellipsis;
internal object Ellipsis : StarlarkValue, AllocFrozenValue {

    override fun toString(): String = "Ellipsis"

    // pub(crate) static VALUE_ELLIPSIS: AllocStaticSimple<Ellipsis> = AllocStaticSimple::alloc(Ellipsis);
    private val VALUE_ELLIPSIS: FrozenValue = io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple.alloc(Ellipsis).toFrozenValue()

    // #[starlark_value(type = "ellipsis")]
    // impl<'v> StarlarkValue<'v> for Ellipsis {}
    // Kotlin: inherits default StarlarkValue methods.

    // impl Ellipsis
    // pub(crate) fn new_value() -> FrozenValue
    fun newValue(): FrozenValue = VALUE_ELLIPSIS

    // impl AllocFrozenValue for Ellipsis
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = newValue()
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
