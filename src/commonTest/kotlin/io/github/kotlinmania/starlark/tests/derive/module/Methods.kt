// port-lint: source src/tests/derive/module/methods.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.module

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
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue

// #[derive(Debug, Display, ProvidesStaticType, NoSerialize, Allocative)]
// struct Applaud { value: i32 }
private class Applaud(
    val value: Int,
) : StarlarkValue, AllocFrozenValue {
    override fun toString(): String = "Applaud(value=$value)"

    // #[starlark_value(type = "applaud")]
    override val TYPE: String get() = "applaud"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    // impl StarlarkValue for Applaud
    // fn get_methods() -> Option<&'static Methods>
    override fun getMethods(): Methods? {
        return Companion.methodsStatic.methods(::methods)
    }

    // impl AllocFrozenValue for Applaud
    // fn alloc_frozen_value(self, heap: &FrozenHeap) -> FrozenValue
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }

    companion object {
        private val methodsStatic = MethodsStatic()
    }
}

// #[starlark_module]
// fn methods(builder: &mut MethodsBuilder)
private fun methods(builder: MethodsBuilder) {
    // fn test_method(#[starlark(this)] receiver: Value, this: i32) -> anyhow::Result<i32>
    builder.setMethod("test_method") { eval, receiver, _, args ->
        val thisParam = args.positional1(eval.heap()).getOrThrow()
        val thisInt = thisParam.unpackI32() ?: return@setMethod Result.failure(
            IllegalArgumentException("Expected int, got ${thisParam.toRepr()}")
        )
        val applaud = receiver.downcastRef<Applaud>()!!
        Result.success((applaud.value + thisInt).allocValue(eval.heap()))
    }
}

// #[test]
// fn test_receiver_can_be_named_anything()
internal fun testReceiverCanBeNamedAnything() {
    val a = Assert()
    a.globalsAdd { g -> g.set("x", Applaud(value = 10)) }
    a.eq("13", "x.test_method(this=3)")
}
