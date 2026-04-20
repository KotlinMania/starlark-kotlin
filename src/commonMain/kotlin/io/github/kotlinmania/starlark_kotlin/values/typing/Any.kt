// port-lint: source src/values/typing/any.rs
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
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple

// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub(crate) struct TypingAny
internal class TypingAny : StarlarkValue, AllocFrozenValue {

    // #[starlark_value(type = "typing.Any")]
    override val TYPE: String get() = Companion.TYPE

    companion object {
        /** Constant type name, equivalent to Rust's `TypingAny::TYPE`. */
        const val TYPE: String = "typing.Any"
    }

    override fun toString(): String = TYPE

    // impl StarlarkTypeRepr for TypingAny
    override fun starlarkTypeRepr(): Ty = Ty.any()

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return Ty.any()
    }

    // impl AllocFrozenValue for TypingAny
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }
}

// #[cfg(test)]
// mod tests

// #[test]
// fn test_any_runtime()
internal fun testAnyRuntime() {
    Assert.isTrue("isinstance(1, typing.Any)")
}

// #[test]
// fn test_any_compile_time()
internal fun testAnyCompileTime() {
    Assert.pass(
        """
def f(x: typing.Any):
    pass

f(1)
""",
    )
}
