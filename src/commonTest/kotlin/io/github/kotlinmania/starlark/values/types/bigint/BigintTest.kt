// port-lint: source src/values/types/bigint.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values.types.bigint

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

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import starlark_map.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.types.float.StarlarkFloat
import kotlin.test.Test
import kotlin.test.assertEquals

class BigintTest {

    // #[test]
    // fn test_parse()
    @Test
    fun testParse() {
        Assert.eq(
            "'1234567890112233445566778899'",
            "str(1234567890112233445566778899)",
        )
        Assert.eq(
            "'1234567890112233445566778899'",
            "str(0x3fd35eb6d519aff76f50e13)",
        )
        Assert.eq(
            "'1234567890112233445566778899'",
            "str(0o776465726665214657756675207023)",
        )
        Assert.eq(
            "'1234567890112233445566778899'",
            "str(0b11111111010011010111101011011011010101000" +
                "1100110101111111101110110111101010000111000010011)",
        )
    }

    // #[test]
    // fn test_str()
    @Test
    fun testStr() {
        Assert.eq(
            "'1234567890112233445566778899'",
            "str(1234567890112233445566778899)",
        )
    }

    // #[test]
    // fn test_repr()
    @Test
    fun testRepr() {
        Assert.eq(
            "'1234567890112233445566778899'",
            "repr(1234567890112233445566778899)",
        )
    }

    // #[test]
    // fn test_equals()
    @Test
    fun testEquals() {
        Assert.eq("10000000000000000000000", "10000000000000000000000")
        Assert.eq("10000000000000000000000", "10000000000000000000000.0")
        Assert.eq("10000000000000000000000.0", "10000000000000000000000")
    }

    // #[test]
    // fn test_plus()
    @Test
    fun testPlus() {
        Assert.eq("10000000000000000000000", "+10000000000000000000000")
    }

    // #[test]
    // fn test_compare_big_big()
    @Test
    fun testCompareBigBig() {
        Assert.isTrue("10000000000000000000000 < 20000000000000000000000")
        Assert.isTrue("-20000000000000000000000 < -10000000000000000000000")
        Assert.isTrue("20000000000000000000000 > 10000000000000000000000")
        Assert.isTrue("-10000000000000000000000 > -20000000000000000000000")
    }

    // #[test]
    // fn test_compare_big_small()
    @Test
    fun testCompareBigSmall() {
        Assert.isTrue("1 < 10000000000000000000000")
        Assert.isTrue("-1 < 10000000000000000000000")
        Assert.isTrue("1 > -10000000000000000000000")
        Assert.isTrue("-1 > -10000000000000000000000")
        Assert.isTrue("10000000000000000000000 > 1")
        Assert.isTrue("10000000000000000000000 > -1")
        Assert.isTrue("-10000000000000000000000 < 1")
        Assert.isTrue("-10000000000000000000000 < -1")
    }

    // #[test]
    // fn test_compare_big_float()
    @Test
    fun testCompareBigFloat() {
        Assert.isTrue("1.0 < 10000000000000000000000")
        Assert.isTrue("-1.0 < 10000000000000000000000")
        Assert.isTrue("1.0 > -10000000000000000000000")
        Assert.isTrue("-1.0 > -10000000000000000000000")
        Assert.isTrue("10000000000000000000000 > 1.0")
        Assert.isTrue("10000000000000000000000 > -1.0")
        Assert.isTrue("-10000000000000000000000 < 1.0")
        Assert.isTrue("-10000000000000000000000 < -1.0")
    }

    // #[test]
    // fn test_add_big()
    @Test
    fun testAddBig() {
        Assert.eq(
            "300000000000000000009",
            "100000000000000000004 + 200000000000000000005",
        )
        Assert.eq("7", "100000000000000000007 + -100000000000000000000")
        Assert.eq(
            "200000000000000000005",
            "300000000000000000009 - 100000000000000000004",
        )
        Assert.eq("7", "100000000000000000007 - 100000000000000000000")
    }

    // #[test]
    // fn test_add_big_small()
    @Test
    fun testAddBigSmall() {
        Assert.eq("100000000000000000017", "100000000000000000000 + 17")
        Assert.eq("100000000000000000017", "17 + 100000000000000000000")
        Assert.eq("100000000000000000000", "100000000000000000017 - 17")
        Assert.eq("-100000000000000000017", "17 - 100000000000000000034")
    }

    // #[test]
    // fn test_add_big_float()
    @Test
    fun testAddBigFloat() {
        Assert.eq("2e20", "100000000000000000000 + 1e20")
        Assert.eq("2e20", "1e20 + 100000000000000000000")
        Assert.eq("2e20", "300000000000000000000 - 1e20")
        Assert.eq("2e20", "3e20 - 100000000000000000000")
    }

    // #[test]
    // fn test_mul_big()
    @Test
    fun testMulBig() {
        Assert.eq(
            "60000000000000000000000000000000000000000",
            "200000000000000000000 * 300000000000000000000",
        )
    }

    // #[test]
    // fn test_mul_big_small()
    @Test
    fun testMulBigSmall() {
        Assert.eq("600000000000000000000", "200000000000000000000 * 3")
        Assert.eq("600000000000000000000", "3 * 200000000000000000000")
    }

    // #[test]
    // fn test_mul_big_float()
    @Test
    fun testMulBigFloat() {
        Assert.eq("6e20", "200000000000000000000 * 3.0")
        Assert.eq("6e20", "3.0 * 200000000000000000000")
    }

    // #[test]
    // fn test_div_big()
    @Test
    fun testDivBig() {
        Assert.eq(
            "2e20",
            "60000000000000000000000000000000000000000 / 300000000000000000000",
        )
    }

    // #[test]
    // fn test_div_big_small()
    @Test
    fun testDivBigSmall() {
        Assert.eq("2e20", "600000000000000000000 / 3")
        Assert.eq("2e-20", "6 / 300000000000000000000")
    }

    // #[test]
    // fn test_div_big_float()
    @Test
    fun testDivBigFloat() {
        Assert.eq("2e20", "600000000000000000000 / 3.0")
        Assert.eq("2e-20", "6.0 / 300000000000000000000")
    }

    // #[test]
    // fn test_floor_div_big()
    @Test
    fun testFloorDivBig() {
        Assert.eq("2", "600000000000000000000 // 300000000000000000000")
    }

    // #[test]
    // fn test_floor_div_big_small()
    @Test
    fun testFloorDivBigSmall() {
        Assert.eq("200000000000000000000", "600000000000000000000 // 3")
        Assert.eq("0", "3 // 600000000000000000000")
    }

    // #[test]
    // fn test_floor_div_big_float()
    @Test
    fun testFloorDivBigFloat() {
        Assert.eq("2e20", "600000000000000000000 / 3.0")
        Assert.eq("2e-20", "6.0 / 300000000000000000000")
    }

    // #[test]
    // fn test_percent_big()
    @Test
    fun testPercentBig() {
        Assert.eq("7", "600000000000000000007 % 200000000000000000000")
    }

    // #[test]
    // fn test_percent_big_small()
    @Test
    fun testPercentBigSmall() {
        Assert.eq("7", "600000000000000000007 % 20")
        Assert.eq("3", "3 % 600000000000000000000")
    }

    // #[test]
    // fn test_percent_big_float()
    @Test
    fun testPercentBigFloat() {
        Assert.eq("1e20", "100000000000000000000 % 1e50")
        Assert.eq("10.0", "10.0 % 100000000000000000000")
    }

    // #[test]
    // fn test_bit_and_big()
    @Test
    fun testBitAndBig() {
        Assert.eq(
            "0x10000000000000000000000",
            "0x30000000000000000000000 & 0x90000000000000000000000",
        )
    }

    // #[test]
    // fn test_bit_and_big_small()
    @Test
    fun testBitAndBigSmall() {
        Assert.eq("1", "0x60000000000000000000003 & 0x9")
        Assert.eq("1", "0x9 & 0x60000000000000000000003")
    }

    // #[test]
    // fn test_bit_and_float()
    @Test
    fun testBitAndFloat() {
        Assert.failSkipTypecheck("0x60000000000000000000000 & 1.0", "not supported")
        Assert.failSkipTypecheck("1.0 & 0x60000000000000000000000", "not supported")
        Assert.fail(
            "def f(): 0x60000000000000000000000 & 1.0",
            "is not available on the types",
        )
        Assert.fail(
            "def f(): 1.0 & 0x60000000000000000000000",
            "is not available on the types",
        )
    }

    // #[test]
    // fn test_bit_or_big()
    @Test
    fun testBitOrBig() {
        Assert.eq(
            "0x70000000000000000000000",
            "0x30000000000000000000000 | 0x50000000000000000000000",
        )
    }

    // #[test]
    // fn test_bit_or_big_small()
    @Test
    fun testBitOrBigSmall() {
        Assert.eq(
            "0x60000000000000000000009",
            "0x60000000000000000000000 | 0x9",
        )
        Assert.eq(
            "0x60000000000000000000009",
            "0x9 | 0x60000000000000000000000",
        )
    }

    // #[test]
    // fn test_bit_or_float()
    @Test
    fun testBitOrFloat() {
        Assert.failSkipTypecheck("0x60000000000000000000000 | 1.0", "not supported")
        Assert.failSkipTypecheck("1.0 | 0x60000000000000000000000", "not supported")
        Assert.fail(
            "def f(): 0x60000000000000000000000 | 1.0",
            "is not available on the types",
        )
        Assert.fail(
            "def f(): 1.0 | 0x60000000000000000000000",
            "is not available on the types",
        )
    }

    // #[test]
    // fn test_bit_xor_big()
    @Test
    fun testBitXorBig() {
        Assert.eq(
            "0x60000000000000000000000",
            "0x30000000000000000000000 ^ 0x50000000000000000000000",
        )
    }

    // #[test]
    // fn test_bit_xor_big_small()
    @Test
    fun testBitXorBigSmall() {
        Assert.eq(
            "0x60000000000000000000000",
            "0x60000000000000000000009 ^ 0x9",
        )
        Assert.eq(
            "0x60000000000000000000000",
            "0x9 ^ 0x60000000000000000000009",
        )
    }

    // #[test]
    // fn test_bit_xor_float()
    @Test
    fun testBitXorFloat() {
        Assert.failSkipTypecheck("0x60000000000000000000000 ^ 1.0", "not supported")
        Assert.failSkipTypecheck("1.0 ^ 0x60000000000000000000000", "not supported")
        Assert.fail(
            "def f(): 0x60000000000000000000000 ^ 1.0",
            "Binary operator `^` is not available",
        )
        Assert.fail(
            "def f(): 1.0 ^ 0x60000000000000000000000",
            "Binary operator `^` is not available",
        )
    }

    // #[test]
    // fn test_bit_not()
    @Test
    fun testBitNot() {
        Assert.eq(
            "-0x10000000000000000000000000000001",
            "~0x10000000000000000000000000000000",
        )
    }

    // #[test]
    // fn test_left_shift()
    @Test
    fun testLeftShift() {
        Assert.fail(
            "0x10000000000000000000000000000000 << 0x10000000000000000000000000000000",
            "Integer overflow",
        )
        Assert.fail(
            "0x10000000000000000000000000000000 << -0x10000000000000000000000000000000",
            "Negative left shift",
        )
    }

    // #[test]
    // fn test_left_shift_small()
    @Test
    fun testLeftShiftSmall() {
        Assert.eq(
            "0x20000000000000000000000000000000",
            "0x10000000000000000000000000000000 << 1",
        )
        Assert.fail(
            "0x10000000000000000000000000000000 << -1",
            "Negative left shift",
        )
        Assert.fail(
            "1 << 0x10000000000000000000000000000000",
            "Integer overflow",
        )
        Assert.fail(
            "1 << -0x10000000000000000000000000000000",
            "Negative left shift",
        )
        Assert.eq("0", "0 << 0x10000000000000000000000000000000")
        Assert.eq("1267650600228229401496703205376", "1 << 100")
        Assert.eq("-1267650600228229401496703205376", "-1 << 100")
    }

    // #[test]
    // fn test_left_shift_float()
    @Test
    fun testLeftShiftFloat() {
        Assert.failSkipTypecheck("0x10000000000000000000000000000000 << 1.0", "not supported")
        Assert.failSkipTypecheck("1.0 << 0x10000000000000000000000000000000", "not supported")
        Assert.fail(
            "def f(): 0x10000000000000000000000000000000 << 1.0",
            "is not available",
        )
        Assert.fail(
            "def f(): 1.0 << 0x10000000000000000000000000000000",
            "is not available",
        )
    }

    // #[test]
    // fn test_right_shift()
    @Test
    fun testRightShift() {
        Assert.eq(
            "0",
            "0x20000000000000000000000000000000 >> 0x20000000000000000000000000000000",
        )
        Assert.eq(
            "-1",
            "-0x20000000000000000000000000000000 >> 0x20000000000000000000000000000000",
        )
        Assert.fail(
            "0x20000000000000000000000000000000 >> -0x20000000000000000000000000000000",
            "Negative right shift",
        )
    }

    // #[test]
    // fn test_right_shift_small()
    @Test
    fun testRightShiftSmall() {
        Assert.eq(
            "0x10000000000000000000000000000000",
            "0x20000000000000000000000000000000 >> 1",
        )
        Assert.fail(
            "0x20000000000000000000000000000000 >> -1",
            "Negative right shift",
        )
        Assert.eq("0", "1 >> 0x20000000000000000000000000000000")
        Assert.eq("-1", "-1 >> 0x20000000000000000000000000000000")
        Assert.fail(
            "1 >> -0x10000000000000000000000000000000",
            "Negative right shift",
        )
    }

    // #[test]
    // fn test_right_shift_float()
    @Test
    fun testRightShiftFloat() {
        Assert.failSkipTypecheck("0x20000000000000000000000000000000 >> 1.0", "not supported")
        Assert.failSkipTypecheck("1.0 >> 0x20000000000000000000000000000000", "not supported")
        Assert.fail(
            "def f(): 0x20000000000000000000000000000000 >> 1.0",
            "is not available",
        )
        Assert.fail(
            "def f(): 1.0 >> 0x20000000000000000000000000000000",
            "is not available",
        )
    }

    // #[test]
    // fn test_int_function()
    @Test
    fun testIntFunction() {
        Assert.eq(
            "123456789012345678901234567890",
            "int(123456789012345678901234567890)",
        )
    }

    // #[test]
    // fn test_hash()
    @Test
    fun testHash() {
        val hash1 = StarlarkHasher()
        val hash2 = StarlarkHasher()
        StarlarkFloat(1e20).writeHash(hash1).getOrThrow()
        StarlarkBigInt.uncheckedNew(BigInteger.TEN.pow(20L))
            .writeHash(hash2).getOrThrow()
        assertEquals(hash1.finish(), hash2.finish())
    }

    // #[test]
    // fn test_int_type_matches_bigint()
    @Test
    fun testIntTypeMatchesBigint() {
        Assert.isTrue("isinstance(1 << 100, int)")
    }
}
