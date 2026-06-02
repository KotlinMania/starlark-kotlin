// port-lint: source tests:src/stdlib.rs
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
import io.github.kotlinmania.starlark.environment.MethodsBuilder
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
        // The Kotlin port wires the two attribute methods through MethodsBuilder
        // by name; the Bool2 starlark value lives behind globalsAdd for parity.

        fun methods(builder: MethodsBuilder) {
            fun invert1(thisVal: io.github.kotlinmania.starlark.values.layout.Value): Result<io.github.kotlinmania.starlark.values.layout.Value> =
                Result.success(
                    io.github.kotlinmania.starlark.values.layout.Value
                        .newBool(!thisVal.unpackBool()!!),
                )

            fun invert2(thisVal: io.github.kotlinmania.starlark.values.layout.Value): Result<io.github.kotlinmania.starlark.values.layout.Value> =
                Result.success(
                    io.github.kotlinmania.starlark.values.layout.Value
                        .newBool(!thisVal.unpackBool()!!),
                )

            builder.setAttribute("invert1") { thisVal, _ -> invert1(thisVal) }
            builder.setMethod("invert2") { eval, thisVal, _, _ -> invert2(thisVal) }
        }

        fun globals(builder: GlobalsBuilder) {
            builder.setConst(
                "True2",
                io.github.kotlinmania.starlark.values.layout.Value
                    .newBool(true),
            )
            builder.setConst(
                "False2",
                io.github.kotlinmania.starlark.values.layout.Value
                    .newBool(false),
            )
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
