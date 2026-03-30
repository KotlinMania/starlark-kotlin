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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue

internal object Ellipsis : StarlarkValue, AllocFrozenValue {

    override val TYPE: String get() = "ellipsis"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new("ellipsis"))

    override fun toString(): String = "Ellipsis"

    private val VALUE_ELLIPSIS: FrozenValue =
        io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple.alloc(Ellipsis).toFrozenValue()

    fun newValue(): FrozenValue = VALUE_ELLIPSIS

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = newValue()
}
