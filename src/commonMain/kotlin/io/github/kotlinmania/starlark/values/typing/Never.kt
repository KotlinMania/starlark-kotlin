// port-lint: source src/values/typing/never.rs
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
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.typing.Ty

internal class TypingNever : StarlarkValue, AllocFrozenValue {
    override val TYPE: String get() = TYPE_NAME
    override val hasEvalType: Boolean get() = true

    override fun toString(): String = TYPE_NAME

    override fun starlarkTypeRepr(): Ty = Ty.never()

    override fun evalType(): Ty? = Ty.never()

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return NEVER.toFrozenValue()
    }

    companion object {
        const val TYPE_NAME: String = "typing.Never"

        private val NEVER = AllocStaticSimple.alloc(TypingNever())
    }
}

/** Never type, can be used as native function return type. */
// An uninhabited enum in Rust — no instances can be created.
sealed class StarlarkNever : StarlarkTypeRepr, AllocValue {
    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty = Ty.never()
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun allocValue(heap: Heap): Value {
        error("StarlarkNever is uninhabited")
    }
}

internal fun testNeverRuntime() {
    Assert.isTrue("not isinstance(1, typing.Never)")
}

internal fun testNeverCompileTime() {
    Assert.pass(
        """
def f() -> typing.Never:
    return fail()
""",
    )
}
