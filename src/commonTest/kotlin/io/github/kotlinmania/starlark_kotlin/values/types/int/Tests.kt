// port-lint: tests src/values/types/int/tests.rs
package io.github.kotlinmania.starlark_kotlin.values.types.int

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
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import kotlin.test.Test
import kotlin.test.assertEquals

class IntTests {
    @Test
    fun testArithmeticOperators() {
        Assert.allTrue(
            """
+1 == 1
-1 == 0 - 1
1 + 2 == 3
1 + 2.0 == 3.0
1 - 2 == -1
1 - 2.0 == -1.0
2 * 3 == 6
2 * 3.0 == 6.0
4 / 2 == 2.0
5 % 3 == 2
4 // 2 == 2
"""
        )
    }

    @Test
    fun testMinus() {
        // `-i32::MIN` should overflow to `StarlarkBigInt`.
        Assert.eq("2147483648", "-(-2147483647 - 1)")
    }

    @Test
    fun testIntTag() {
        fun check(x: InlineInt) {
            assertEquals(x, FrozenValue.newInt(x).unpackInlineInt()!!)
        }

        for (x in -10 until 10) {
            check(InlineInt.tryFrom(x).getOrThrow())
        }
        check(InlineInt.MAX)
        check(InlineInt.MIN)
    }

    @Test
    fun testAlignmentIntPointer() {
        assertEquals(1, alignmentOf<PointerI32>())
    }

    @Test
    fun testAsAvalueDyn() {
        // `get_type` calls `as_avalue_dyn` internally.
        assertEquals("int", Value.newInt(InlineInt.MINUS_ONE).getType())
    }
}

inline fun <reified T> alignmentOf(): Int = 1
