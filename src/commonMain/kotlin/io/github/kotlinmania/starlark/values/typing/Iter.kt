// port-lint: source values/typing/iter.rs
package io.github.kotlinmania.starlark.values.typing

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.typing.Ty

/** `StarlarkTypeRepr` for iterable types. */
class StarlarkIter<T : StarlarkTypeRepr> private constructor() {
    companion object {
        fun starlarkTypeRepr(inner: Ty): Ty {
            return Ty.iter(inner)
        }
    }
}

internal class TypingIterable : StarlarkValue, AllocFrozenValue {
    override val TYPE: String get() = TYPE_NAME
    override val hasEvalType: Boolean get() = true

    override fun toString(): String = TYPE_NAME

    override fun evalType(): Ty? = Ty.iter(Ty.any())

    override fun starlarkTypeRepr(): Ty = Ty.iter(Ty.any())

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return ANY.toFrozenValue()
    }

    companion object {
        const val TYPE_NAME: String = "typing.Iterable"

        private val ANY = AllocStaticSimple.alloc(TypingIterable())
    }
}

internal fun testIterableRuntime() {
    Assert.isTrue("isinstance([1, 2, 3], typing.Iterable)")
    Assert.isTrue("isinstance((1, 2, 3), typing.Iterable)")
    Assert.isTrue("isinstance(range(10), typing.Iterable)")
    Assert.isFalse("isinstance('', typing.Iterable)")
    Assert.isFalse("isinstance(1, typing.Iterable)")
}

internal fun testIterableCompileTimePass() {
    Assert.pass(
        """
def foo(x: typing.Iterable):
    pass

def bar():
    foo([1, 2, 3])
""",
    )
}

internal fun testIterableCompileTimeFail() {
    Assert.fail(
        """
def foo(x: typing.Iterable):
    pass

def bar():
    foo(1)
""",
        "Expected type `typing.Iterable`",
    )
}
