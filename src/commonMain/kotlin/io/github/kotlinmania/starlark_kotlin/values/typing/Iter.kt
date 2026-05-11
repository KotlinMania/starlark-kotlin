// port-lint: source src/values/typing/iter.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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

import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.typing.Ty

/** `StarlarkTypeRepr` for iterable types. */
// pub struct StarlarkIter<T: StarlarkTypeRepr>(PhantomData<T>, NonInstantiable)
// PhantomData<T> + NonInstantiable → uninhabited generic marker type
class StarlarkIter<T : StarlarkTypeRepr> private constructor() {
    companion object {
        // impl<T: StarlarkTypeRepr> StarlarkTypeRepr for StarlarkIter<T>
        // fn starlark_type_repr() -> Ty
        fun starlarkTypeRepr(inner: Ty): Ty {
            return Ty.iter(inner)
        }
    }
}

// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub(crate) struct TypingIterable;
internal class TypingIterable : StarlarkValue, AllocFrozenValue {
    // #[starlark_value(type = "typing.Iterable")]
    override val TYPE: String get() = TYPE_NAME
    override val HAS_eval_type: Boolean get() = true

    override fun toString(): String = TYPE_NAME

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? = Ty.iter(Ty.any())

    override fun starlarkTypeRepr(): Ty = Ty.iter(Ty.any())

    // impl AllocFrozenValue for TypingIterable
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(@Suppress("unused") heap: FrozenHeap): FrozenValue {
        return ANY.toFrozenValue()
    }

    companion object {
        const val TYPE_NAME: String = "typing.Iterable"

        // static ANY: AllocStaticSimple<TypingIterable> = AllocStaticSimple::alloc(TypingIterable)
        private val ANY = AllocStaticSimple.alloc(TypingIterable())
    }
}
