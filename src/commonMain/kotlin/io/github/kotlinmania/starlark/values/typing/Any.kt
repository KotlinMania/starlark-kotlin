// port-lint: source values/typing/any.rs
package io.github.kotlinmania.starlark.values.typing

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple

internal class TypingAny : StarlarkValue, AllocFrozenValue {

    override val TYPE: String get() = Companion.TYPE

    companion object {
        /** Constant type name. */
        const val TYPE: String = "typing.Any"
    }

    override fun toString(): String = TYPE

    override fun starlarkTypeRepr(): Ty = Ty.any()

    override fun evalType(): Ty? {
        return Ty.any()
    }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }
}

internal fun testAnyRuntime() {
    Assert.isTrue("isinstance(1, typing.Any)")
}

internal fun testAnyCompileTime() {
    Assert.pass(
        """
def f(x: typing.Any):
    pass

f(1)
""",
    )
}
