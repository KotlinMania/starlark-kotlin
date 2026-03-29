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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.AllocStaticSimple
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.typing.Ty

/// `StarlarkTypeRepr` for iterable types.
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

    override fun toString(): String = TYPE_NAME

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? = Ty.iter(Ty.any())

    override fun starlarkTypeRepr(): Ty = Ty.iter(Ty.any())

    // impl AllocFrozenValue for TypingIterable
    // fn alloc_frozen_value(self, _heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return ANY.toFrozenValue()
    }

    companion object {
        const val TYPE_NAME: String = "typing.Iterable"

        // static ANY: AllocStaticSimple<TypingIterable> = AllocStaticSimple::alloc(TypingIterable)
        private val ANY = AllocStaticSimple.alloc(TypingIterable())
    }
}

// #[cfg(test)]
// mod tests

// #[test]
// fn test_iterable_runtime()
internal fun testIterableRuntime() {
    Assert.isTrue("isinstance([1, 2, 3], typing.Iterable)")
    Assert.isTrue("isinstance((1, 2, 3), typing.Iterable)")
    Assert.isTrue("isinstance(range(10), typing.Iterable)")
    Assert.isFalse("isinstance('', typing.Iterable)")
    Assert.isFalse("isinstance(1, typing.Iterable)")
}

// #[test]
// fn test_iterable_compile_time_pass()
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

// #[test]
// fn test_iterable_compile_time_fail()
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
