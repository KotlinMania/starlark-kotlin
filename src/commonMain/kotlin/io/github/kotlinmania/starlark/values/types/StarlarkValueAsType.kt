// port-lint: source src/values/types/starlark_value_as_type.rs
package io.github.kotlinmania.starlark.values.types.starlarkvalueastype

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

import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocProperty
import io.github.kotlinmania.starlark.docs.DocType
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.typing.ty.AbstractType

/** Backing Starlark `type` value for a Kotlin value type representation. */
@PublishedApi
internal class StarlarkValueAsTypeStarlarkValue(
    private val tyFn: () -> Ty,
    private val docFn: () -> DocItem,
) : StarlarkValue {
    override val TYPE: String get() = "type"
    override val HAS_eval_type: Boolean get() = true

    override fun evalType(): Ty = tyFn()

    override fun documentation(): DocItem = docFn()

    override fun toString(): String = tyFn().toString()
}

/** Utility to declare a value usable in Starlark type expressions. */
class StarlarkValueAsType<T : StarlarkTypeRepr>
    @PublishedApi
    internal constructor(
        @PublishedApi internal val inner: StarlarkValueAsTypeStarlarkValue,
        private val tyRepr: () -> Ty,
    ) : StarlarkTypeRepr,
        AllocValue,
        AllocFrozenValue {
        companion object {
            /** Constructor for value types with full Starlark documentation. */
            inline fun <reified T> new(instance: T): StarlarkValueAsType<T>
            where T : StarlarkTypeRepr, T : StarlarkValue {
                val tyFn: () -> Ty = { instance.getTypeStarlarkRepr() }
                val docFn: () -> DocItem = { DocItem.TypeDoc(DocType.fromStarlarkValue(instance)) }
                return StarlarkValueAsType(
                    inner = StarlarkValueAsTypeStarlarkValue(tyFn, docFn),
                    tyRepr = tyFn,
                )
            }

            /** Constructor for value types that expose only a type expression. */
            inline fun <reified T : StarlarkTypeRepr> newNoDocs(instance: T): StarlarkValueAsType<T> {
                val tyFn: () -> Ty = { instance.starlarkTypeRepr() }
                val docFn: () -> DocItem = {
                    DocItem.Member(
                        DocMember.Property(
                            DocProperty(
                                typ = AbstractType.starlarkTypeRepr(),
                            ),
                        ),
                    )
                }
                return StarlarkValueAsType(
                    inner = StarlarkValueAsTypeStarlarkValue(tyFn, docFn),
                    tyRepr = tyFn,
                )
            }
        }

        override fun toString(): String = tyRepr().toString()

        override fun starlarkTypeRepr(): Ty = AbstractType.starlarkTypeRepr()

        override fun allocValue(heap: Heap): Value = heap.allocSimple(inner)

        override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(inner)
    }
