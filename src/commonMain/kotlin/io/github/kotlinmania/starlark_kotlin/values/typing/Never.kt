// port-lint: source src/values/typing/never.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.typing.Ty

// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub(crate) struct TypingNever;
internal class TypingNever : StarlarkValue, AllocFrozenValue {
    // #[starlark_value(type = "typing.Never")]
    override val TYPE: String get() = TYPE_NAME
    override val HAS_eval_type: Boolean get() = true

    override fun toString(): String = TYPE_NAME

    // impl StarlarkTypeRepr for TypingNever
    override fun starlarkTypeRepr(): Ty = Ty.never()

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? = Ty.never()

    // impl AllocFrozenValue for TypingNever
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(@Suppress("unused") heap: FrozenHeap): FrozenValue {
        return NEVER.toFrozenValue()
    }

    companion object {
        const val TYPE_NAME: String = "typing.Never"

        // static NEVER: AllocStaticSimple<TypingNever> = AllocStaticSimple::alloc(TypingNever)
        private val NEVER = AllocStaticSimple.alloc(TypingNever())
    }
}

/** Never type, can be used as native function return type. */
// pub enum StarlarkNever {}
// An uninhabited enum in Rust — no instances can be created.
sealed class StarlarkNever : StarlarkTypeRepr, AllocValue {
    companion object : StarlarkTypeRepr {
        // impl StarlarkTypeRepr for StarlarkNever
        // fn starlark_type_repr() -> Ty
        override fun starlarkTypeRepr(): Ty = Ty.never()
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    // impl AllocValue for StarlarkNever
    // fn alloc_value(self, _heap: Heap) -> Value
    override fun allocValue(@Suppress("unused") heap: Heap): Value {
        error("StarlarkNever is uninhabited")
    }
}

// #[cfg(test)]
// mod tests

// #[test]
// fn test_never_runtime()
internal fun testNeverRuntime() {
    Assert.isTrue("not isinstance(1, typing.Never)")
}

// #[test]
// fn test_never_compile_time()
internal fun testNeverCompileTime() {
    Assert.pass(
        """
def f() -> typing.Never:
    return fail()
""",
    )
}
