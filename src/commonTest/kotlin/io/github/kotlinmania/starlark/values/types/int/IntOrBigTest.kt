// port-lint: tests src/values/types/int/int_or_big.rs
package io.github.kotlinmania.starlark.values.types.int

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

import kotlin.test.Test
import kotlin.test.assertEquals

class IntOrBigTest {
    private fun int(s: String): StarlarkInt = StarlarkInt.fromStrRadix(s, 10).getOrThrow()

    private fun floorDiv(a: String, b: String): String =
        int(a)
            .asRef()
            .floorDiv(int(b).asRef())
            .getOrThrow()
            .toString()

    private fun percent(a: String, b: String): String =
        int(a)
            .asRef()
            .percent(int(b).asRef())
            .getOrThrow()
            .toString()

    // #[test]
    @Test
    fun testFloorDivBig() {
        assertEquals(
            "2",
            floorDiv("600000000000000000005", "300000000000000000000"),
        )
        assertEquals(
            "-3",
            floorDiv("600000000000000000005", "-300000000000000000000"),
        )
        assertEquals(
            "-3",
            floorDiv("-600000000000000000005", "300000000000000000000"),
        )
        assertEquals(
            "2",
            floorDiv("-600000000000000000005", "-300000000000000000000"),
        )
    }

    // #[test]
    @Test
    fun testFloorDivBigSmall() {
        assertEquals(
            "200000000000000000001",
            floorDiv("600000000000000000005", "3"),
        )
        assertEquals(
            "-200000000000000000002",
            floorDiv("600000000000000000005", "-3"),
        )
        assertEquals(
            "-200000000000000000002",
            floorDiv("-600000000000000000005", "3"),
        )
        assertEquals(
            "200000000000000000001",
            floorDiv("-600000000000000000005", "-3"),
        )
    }

    // #[test]
    @Test
    fun testFloorDivSmallBig() {
        assertEquals("0", floorDiv("3", "600000000000000000000"))
        assertEquals("0", floorDiv("-3", "-600000000000000000000"))
        assertEquals("-1", floorDiv("3", "-600000000000000000000"))
        assertEquals("-1", floorDiv("-3", "600000000000000000000"))
    }

    // #[test]
    @Test
    fun testFloorDivSmall() {
        assertEquals("4", floorDiv("13", "3"))
        assertEquals("-5", floorDiv("13", "-3"))
        assertEquals("-5", floorDiv("-13", "3"))
        assertEquals("4", floorDiv("-13", "-3"))
    }

    // #[test]
    @Test
    fun testPercentBig() {
        assertEquals(
            "7",
            percent("600000000000000000007", "200000000000000000000"),
        )
        assertEquals(
            "-199999999999999999993",
            percent("600000000000000000007", "-200000000000000000000"),
        )
        assertEquals(
            "199999999999999999993",
            percent("-600000000000000000007", "200000000000000000000"),
        )
        assertEquals(
            "-7",
            percent("-600000000000000000007", "-200000000000000000000"),
        )
    }

    // #[test]
    @Test
    fun testPercentBigSmall() {
        assertEquals("7", percent("600000000000000000007", "20"))
        assertEquals("-13", percent("600000000000000000007", "-20"))
        assertEquals("13", percent("-600000000000000000007", "20"))
        assertEquals("-7", percent("-600000000000000000007", "-20"))
    }

    // #[test]
    @Test
    fun testPercentSmallBig() {
        assertEquals("3", percent("3", "600000000000000000001"))
        assertEquals(
            "-599999999999999999998",
            percent("3", "-600000000000000000001"),
        )
        assertEquals(
            "599999999999999999998",
            percent("-3", "600000000000000000001"),
        )
        assertEquals("-3", percent("-3", "-600000000000000000001"))
    }

    // #[test]
    @Test
    fun testPercentSmall() {
        assertEquals("2", percent("5", "3"))
        assertEquals("-1", percent("5", "-3"))
        assertEquals("1", percent("-5", "3"))
        assertEquals("-2", percent("-5", "-3"))
    }
}
