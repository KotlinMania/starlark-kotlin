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
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocSimple

// #[derive(Debug, Display, ProvidesStaticType, NoSerialize, Allocative)]
// struct Applaud { value: i32 }
private class Applaud(
    val value: Int,
) : StarlarkValue, AllocFrozenValue {
    override fun toString(): String = "Applaud(value=$value)"

    // #[starlark_value(type = "applaud")]
    override val TYPE: String get() = "applaud"

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
    builder.setMethod("test_method") { receiver: Value, thisParam: Int ->
        val applaud = receiver.downcastRef<Applaud>()!!
        Result.success(applaud.value + thisParam)
    }
}

// #[test]
// fn test_receiver_can_be_named_anything()
internal fun testReceiverCanBeNamedAnything() {
    val a = Assert()
    a.globalsAdd { g -> g.set("x", g.alloc(Applaud(value = 10))) }
    a.eq("13", "x.test_method(this=3)")
}
