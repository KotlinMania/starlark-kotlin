// port-lint: tests src/stdlib/json.rs (tests)
package io.github.kotlinmania.starlark_kotlin.stdlib

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
import kotlin.test.Test

// #[cfg(test)]
// mod tests
class JsonTest {

    // #[test]
    // fn test_json_encode()
    @Test
    fun testJsonEncode() {
        val a = Assert()
        a.eq("'[10]'", "json.encode([10])")

        // Test small integer
        a.eq("'1'", "json.encode(1)")

        // Test 63-bit integer (largest 63-bit signed integer)
        a.eq("'9223372036854775807'", "json.encode(9223372036854775807)")
    }

    // #[test]
    // fn test_json_decode()
    @Test
    fun testJsonDecode() {
        val a = Assert()
        a.eq(
            "[10, None, False, {\"k\": \"v\"}]",
            "json.decode('[10, null, false, {\"k\": \"v\"}]')",
        )

        a.eq("3.142", "json.decode('3.142')")
        a.eq(
            "123456789123456789123456789",
            "json.decode('123456789123456789123456789')",
        )
    }

    // #[test]
    // fn test_json_very_large_int()
    @Test
    fun testJsonVeryLargeInt() {
        val a = Assert()

        // Test edge cases: numbers at the boundary of different ranges

        // u64::MAX - should be encoded as unquoted number
        a.eq(
            "'18446744073709551615'",
            "json.encode(18446744073709551615)",
        )
        a.eq(
            "18446744073709551615",
            "json.decode(json.encode(18446744073709551615))",
        )

        // i64::MIN - should be encoded as unquoted number
        a.eq(
            "'-9223372036854775808'",
            "json.encode(-9223372036854775808)",
        )
        a.eq(
            "-9223372036854775808",
            "json.decode(json.encode(-9223372036854775808))",
        )

        // Test decoding of very large numbers from JSON strings
        a.eq(
            "18446744073709551616",
            "json.decode('18446744073709551616')",
        )
        a.eq(
            "-9223372036854775809",
            "json.decode('-9223372036854775809')",
        )
    }

    // #[test]
    // fn test_json_128bit_and_beyond()
    @Test
    fun testJson128bitAndBeyond() {
        val a = Assert()

        // Test 128-bit boundary cases

        // 2^64 (u64::MAX + 1) - first number requiring more than 64 bits
        a.eq(
            "18446744073709551616",
            "json.decode('18446744073709551616')",
        )
        a.eq(
            "18446744073709551616",
            "json.decode(json.encode(18446744073709551616))",
        )

        // 2^100 - large 128-bit number
        a.eq(
            "1267650600228229401496703205376",
            "json.decode('1267650600228229401496703205376')",
        )
        a.eq(
            "1267650600228229401496703205376",
            "json.decode(json.encode(1267650600228229401496703205376))",
        )

        // 2^128 - 1 (largest 128-bit unsigned integer)
        a.eq(
            "340282366920938463463374607431768211455",
            "json.decode('340282366920938463463374607431768211455')",
        )
        a.eq(
            "340282366920938463463374607431768211455",
            "json.decode(json.encode(340282366920938463463374607431768211455))",
        )

        // 2^128 (first number requiring more than 128 bits)
        a.eq(
            "340282366920938463463374607431768211456",
            "json.decode('340282366920938463463374607431768211456')",
        )
        a.eq(
            "340282366920938463463374607431768211456",
            "json.decode(json.encode(340282366920938463463374607431768211456))",
        )

        // Beyond 128-bit: 2^200
        a.eq(
            "1606938044258990275541962092341162602522202993782792835301376",
            "json.decode('1606938044258990275541962092341162602522202993782792835301376')",
        )
        a.eq(
            "1606938044258990275541962092341162602522202993782792835301376",
            "json.decode(json.encode(1606938044258990275541962092341162602522202993782792835301376))",
        )

        // 2^256 - extremely large number
        a.eq(
            "115792089237316195423570985008687907853269984665640564039457584007913129639936",
            "json.decode('115792089237316195423570985008687907853269984665640564039457584007913129639936')",
        )
        a.eq(
            "115792089237316195423570985008687907853269984665640564039457584007913129639936",
            "json.decode(json.encode(115792089237316195423570985008687907853269984665640564039457584007913129639936))",
        )

        // Test negative 128-bit and beyond numbers

        // -(2^100)
        a.eq(
            "-1267650600228229401496703205376",
            "json.decode('-1267650600228229401496703205376')",
        )
        a.eq(
            "-1267650600228229401496703205376",
            "json.decode(json.encode(-1267650600228229401496703205376))",
        )

        // -(2^128)
        a.eq(
            "-340282366920938463463374607431768211456",
            "json.decode('-340282366920938463463374607431768211456')",
        )
        a.eq(
            "-340282366920938463463374607431768211456",
            "json.decode(json.encode(-340282366920938463463374607431768211456))",
        )

        // -(2^200)
        a.eq(
            "-1606938044258990275541962092341162602522202993782792835301376",
            "json.decode('-1606938044258990275541962092341162602522202993782792835301376')",
        )
        a.eq(
            "-1606938044258990275541962092341162602522202993782792835301376",
            "json.decode(json.encode(-1606938044258990275541962092341162602522202993782792835301376))",
        )

        // -(2^256)
        a.eq(
            "-115792089237316195423570985008687907853269984665640564039457584007913129639936",
            "json.decode('-115792089237316195423570985008687907853269984665640564039457584007913129639936')",
        )
        a.eq(
            "-115792089237316195423570985008687907853269984665640564039457584007913129639936",
            "json.decode(json.encode(-115792089237316195423570985008687907853269984665640564039457584007913129639936))",
        )

        // Test extremely large numbers beyond typical use cases

        // 2^512 - cryptocurrency/cryptographic scale number
        val large512bit = "13407807929942597099574024998205846127479365820592393377723561443721764030073546976801874298166903427690031858186486050853753882811946569946433649006084096"
        a.eq(large512bit, "json.decode('$large512bit')")
        a.eq(
            large512bit,
            "json.decode(json.encode($large512bit))",
        )

        // Test large decimal with many digits (308 digits - close to f64 limit but still manageable)
        val largeNumber = "10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
        a.eq(largeNumber, "json.decode('$largeNumber')")
        a.eq(
            largeNumber,
            "json.decode(json.encode($largeNumber))",
        )
    }
}
