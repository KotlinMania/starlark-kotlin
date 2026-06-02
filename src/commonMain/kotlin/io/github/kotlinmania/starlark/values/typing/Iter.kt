// port-lint: source src/values/typing/iter.rs
package io.github.kotlinmania.starlark.values.typing

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap

/** `StarlarkTypeRepr` for iterable types. */
// PhantomData<T> + NonInstantiable → uninhabited generic marker type
class StarlarkIter<T : StarlarkTypeRepr> private constructor() {
    companion object {
        fun starlarkTypeRepr(inner: Ty): Ty = Ty.iter(inner)
    }
}

internal class TypingIterable :
    StarlarkValue,
    AllocFrozenValue {
    override val TYPE: String get() = TYPE_NAME
    override val HAS_eval_type: Boolean get() = true

    override fun toString(): String = TYPE_NAME

    override fun evalType(): Ty = Ty.iter(Ty.any())

    override fun starlarkTypeRepr(): Ty = Ty.iter(Ty.any())

    override fun allocFrozenValue(
        @Suppress("unused") heap: FrozenHeap,
    ): FrozenValue = ANY.toFrozenValue()

    companion object {
        const val TYPE_NAME: String = "typing.Iterable"

        // static ANY: AllocStaticSimple<TypingIterable> = AllocStaticSimple::alloc(TypingIterable)
        private val ANY = AllocStaticSimple.alloc(TypingIterable())
    }
}
