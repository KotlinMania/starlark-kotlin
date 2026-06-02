// port-lint: tests src/stdlib.rs
package io.github.kotlinmania.starlark

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
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test
import kotlin.test.assertNotNull

class StdlibTest {
    @Test
    fun testNoArg() {
        fun global(builder: GlobalsBuilder) {
            fun nop(): Result<NoneType> = Result.success(NoneType)

            builder.setFunction("nop") { _, _ -> nop() }
        }

        val env = GlobalsBuilder.new().with(::global).build()
        assertNotNull(env.get("nop"))
    }

    @Test
    fun testValueAttributes() {
        // Mirrors the Rust upstream pattern: a tiny `Bool2` value type with two
        // attribute methods, then a battery of dir/hasattr/getattr assertions.

        fun globals(builder: GlobalsBuilder) {
            builder.setConst("True2", Bool2(true))
            builder.setConst("False2", Bool2(false))
        }

        val a = Assert()
        a.globalsAdd(::globals)
        a.allTrue(
            """
True2 == True2
True2 != False2
True2.invert1 == False2
False2.invert1 == True2
False2.invert2() == True2
hasattr(True2, "invert1") == True
hasattr(True2, "invert2") == True
hasattr(True2, "invert3") == False
dir(False2) == ["invert1","invert2"]
getattr(False2, "invert1") == True2
getattr(True2, "invert1") == False2
getattr(True2, "invert2")() == False2
""",
        )
    }
}

private class Bool2(
    val value: Boolean,
) : StarlarkValue,
    AllocFrozenValue {
    override val TYPE: String get() = "bool2"
    override val HAS_equals: Boolean get() = true

    override fun toString(): String = if (value) "True2" else "False2"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    override fun getMethods(): Methods? = Companion.methodsStatic.methods(::methods)

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)

    override fun equals(other: Value): Result<Boolean> {
        val otherBool = other.downcastRef<Bool2>() ?: return Result.success(false)
        return Result.success(otherBool.value == value)
    }

    companion object {
        private val methodsStatic = MethodsStatic()

        fun methods(builder: MethodsBuilder) {
            fun invert1(thisVal: Value, heap: Heap): Result<Value> =
                Result.success(
                    heap.allocSimple(Bool2(!thisVal.downcastRef<Bool2>()!!.value)),
                )

            fun invert2(thisVal: Value, heap: Heap): Result<Value> =
                Result.success(
                    heap.allocSimple(Bool2(!thisVal.downcastRef<Bool2>()!!.value)),
                )

            builder.setAttribute("invert1") { thisVal, heap -> invert1(thisVal, heap) }
            builder.setMethod("invert2") { eval, thisVal, _, _ -> invert2(thisVal, eval.heap()) }
        }
    }
}
