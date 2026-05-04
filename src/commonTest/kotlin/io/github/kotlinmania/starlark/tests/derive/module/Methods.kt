// port-lint: source tests/derive/module/methods.rs
package io.github.kotlinmania.starlark.tests.derive.module

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
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import kotlin.test.Test

private class Applaud(
    val value: Int,
) : StarlarkValue, AllocFrozenValue {
    override fun toString(): String = "Applaud(value=$value)"

    override val TYPE: String get() = "applaud"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    override fun getMethods(): Methods? {
        return Companion.methodsStatic.methods(::methods)
    }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }

    companion object {
        private val methodsStatic = MethodsStatic()
    }
}

private fun methods(builder: MethodsBuilder) {
    fun testMethod(receiver: Value, thisInt: Int): Result<Int> {
        val applaud = receiver.downcastRef<Applaud>()!!
        return Result.success(applaud.value + thisInt)
    }

    builder.setMethod("test_method") { eval, receiver, _, args ->
        val thisParam = args.positional1(eval.heap()).getOrThrow()
        val thisInt = thisParam.unpackI32() ?: return@setMethod Result.failure(
            IllegalArgumentException("Expected int, got ${thisParam.toRepr()}")
        )
        testMethod(receiver, thisInt).map { it.allocValue(eval.heap()) }
    }
}

class MethodsTests {
    @Test
    fun testReceiverCanBeNamedAnything() {
        val a = Assert()
        a.globalsAdd { g -> g.set("x", Applaud(value = 10)) }
        a.eq("13", "x.test_method(this=3)")
    }
}
