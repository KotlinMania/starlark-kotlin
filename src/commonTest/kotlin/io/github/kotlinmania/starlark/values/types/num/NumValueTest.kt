// port-lint: tests src/values/types/num/value.rs
package io.github.kotlinmania.starlark.values.types.num

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

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.float.StarlarkFloat
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NumValueTest {
    @Test
    fun testFromValue() {
        assertNull(NumRef.unpackValue(Value.newBool(true)).getOrThrow())
        assertNull(NumRef.unpackValue(Value.newBool(false)).getOrThrow())
        assertNull(NumRef.unpackValue(Value.newEmptyString()).getOrThrow())
        assertNull(NumRef.unpackValue(Value.newNone()).getOrThrow())

        assertEquals(0, NumRef.unpackValue(Value.testingNewInt(0)).getOrThrow()!!.asInt())
        assertEquals(42, NumRef.unpackValue(Value.testingNewInt(42)).getOrThrow()!!.asInt())
        assertEquals(-42, NumRef.unpackValue(Value.testingNewInt(-42)).getOrThrow()!!.asInt())
    }

    @Test
    fun testConversionToFloat() {
        assertEquals(0.0, NumRef.Int(StarlarkIntRef.Small(InlineInt.ZERO)).asFloat())
        assertEquals(InlineInt.MAX.toF64(), NumRef.Int(StarlarkIntRef.Small(InlineInt.MAX)).asFloat())
        assertEquals(InlineInt.MIN.toF64(), NumRef.Int(StarlarkIntRef.Small(InlineInt.MIN)).asFloat())

        assertEquals(0.0, NumRef.Float(StarlarkFloat(0.0)).asFloat())
        assertTrue(NumRef.Float(StarlarkFloat(Double.NaN)).asFloat().isNaN())
    }

    @Test
    fun testConversionToInt() {
        assertEquals(0, NumRef.Int(StarlarkIntRef.Small(InlineInt.testingNew(0))).asInt())
        assertEquals(42, NumRef.Int(StarlarkIntRef.Small(InlineInt.testingNew(42))).asInt())
        assertEquals(-42, NumRef.Int(StarlarkIntRef.Small(InlineInt.testingNew(-42))).asInt())

        assertEquals(0, NumRef.Float(StarlarkFloat(0.0)).asInt())
        assertEquals(42, NumRef.Float(StarlarkFloat(42.0)).asInt())
        assertEquals(-42, NumRef.Float(StarlarkFloat(-42.0)).asInt())

        assertEquals(Int.MIN_VALUE, NumRef.Float(StarlarkFloat(Int.MIN_VALUE.toDouble())).asInt())
        assertEquals(Int.MAX_VALUE, NumRef.Float(StarlarkFloat(Int.MAX_VALUE.toDouble())).asInt())

        assertNull(NumRef.Float(StarlarkFloat(42.75)).asInt())
        assertNull(NumRef.Float(StarlarkFloat(-42.75)).asInt())
        assertNull(NumRef.Float(StarlarkFloat(Double.NaN)).asInt())
        assertNull(NumRef.Float(StarlarkFloat(Double.POSITIVE_INFINITY)).asInt())
        assertNull(NumRef.Float(StarlarkFloat(Double.NEGATIVE_INFINITY)).asInt())
    }

    @Test
    fun testHashing() {
        assertEquals(
            NumRef.Int(StarlarkIntRef.Small(InlineInt.testingNew(0))).getHash64(),
            NumRef.Float(StarlarkFloat(0.0)).getHash64(),
        )
        assertEquals(
            NumRef.Int(StarlarkIntRef.Small(InlineInt.testingNew(42))).getHash64(),
            NumRef.Float(StarlarkFloat(42.0)).getHash64(),
        )

        assertEquals(
            NumRef.Float(StarlarkFloat(Double.POSITIVE_INFINITY + Double.NEGATIVE_INFINITY)).getHash64(),
            NumRef.Float(StarlarkFloat(Double.NaN)).getHash64(),
        )
        assertEquals(
            NumRef.Float(StarlarkFloat("0.25".toDouble())).getHash64(),
            NumRef.Float(StarlarkFloat("25e-2".toDouble())).getHash64(),
        )

        val x = 1L shl 55
        assertEquals(x, x.toDouble().toLong(), "Self-check")
        assertEquals(
            NumRef.Float(StarlarkFloat(x.toDouble())).getHash64(),
            NumRef.Int(StarlarkInt.from(BigInteger.fromLong(x)).asRef()).getHash64(),
        )
    }

    @Test
    fun testEq() {
        assertEquals(
            NumRef.Float(StarlarkFloat(Double.NaN)),
            NumRef.Float(StarlarkFloat(Double.NaN)),
        )
        assertEquals(
            NumRef.Float(StarlarkFloat(Double.POSITIVE_INFINITY)),
            NumRef.Float(StarlarkFloat(Double.POSITIVE_INFINITY)),
        )
        assertEquals(
            NumRef.Int(StarlarkIntRef.Small(InlineInt.testingNew(10))),
            NumRef.Float(StarlarkFloat(10.0)),
        )
    }
}
