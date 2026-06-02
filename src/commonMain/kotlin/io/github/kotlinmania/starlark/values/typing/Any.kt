// port-lint: source src/values/typing/any.rs
package io.github.kotlinmania.starlark.values.typing

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap

// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub(crate) struct TypingAny
internal class TypingAny :
    StarlarkValue,
    AllocFrozenValue {
    // #[starlark_value(type = "typing.Any")]
    override val TYPE: String get() = Companion.TYPE
    override val HAS_eval_type: Boolean get() = true

    companion object {
        /** Constant type name, equivalent to Rust's `TypingAny::TYPE`. */
        const val TYPE: String = "typing.Any"
    }

    override fun toString(): String = TYPE

    // impl StarlarkTypeRepr for TypingAny
    override fun starlarkTypeRepr(): Ty = Ty.any()

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? = Ty.any()

    // impl AllocFrozenValue for TypingAny
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)
}
