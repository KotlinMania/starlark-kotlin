// port-lint: tests src/values/types/string/interpolation.rs
package io.github.kotlinmania.starlark.values.types.string

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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InterpolationTest {
    @Test
    fun testIncompleteFormat() {
        Assert.fail("'%' % ()", "Incomplete format")
    }

    @Test
    fun testUnsupportedFormatCharacter() {
        Assert.fail("'xx%qxx' % (1,)", "Unsupported format character: 'q'")
    }

    @Test
    fun testParsePercentSOne() {
        assertEquals(Pair("abc", "def"), parsePercentSOne("abc%sdef"))
        assertEquals(Pair("a%b", "c%d%"), parsePercentSOne("a%%b%sc%%d%%"))
        assertNull(parsePercentSOne("a%"))
        assertNull(parsePercentSOne("a%s%"))
        assertNull(parsePercentSOne("a%s%s"))
        assertNull(parsePercentSOne("%d"))
    }

    @Test
    fun testTypeSupportD() {
        Assert.eq("'%d' % (-123,)", "'-123'")
        Assert.eq("'%d' % (-12345678901234567890,)", "'-12345678901234567890'")
        Assert.eq("'%d' % (-123.0,)", "'-123'")

        Assert.fail(
            "'%d' % (True,)",
            "Operation `format(%d)` not supported on type `bool`",
        )
        Assert.fail(
            "'%d' % ('abc',)",
            "Operation `format(%d)` not supported on type `string`",
        )
        Assert.fail(
            "'%d' % ([],)",
            "Operation `format(%d)` not supported on type `list`",
        )
    }

    @Test
    fun testTypeSupportO() {
        Assert.eq("'%o' % (-123,)", "'-173'")
        Assert.eq(
            "'%o' % (-12345678901234567890,)",
            "'-1255245230635307605322'",
        )

        Assert.fail(
            "'%o' % (-123.0,)",
            "Operation `format(%o)` not supported on type `float`",
        )
        Assert.fail(
            "'%o' % (True,)",
            "Operation `format(%o)` not supported on type `bool`",
        )
        Assert.fail(
            "'%o' % ('abc',)",
            "Operation `format(%o)` not supported on type `string`",
        )
        Assert.fail(
            "'%o' % ([],)",
            "Operation `format(%o)` not supported on type `list`",
        )
    }

    @Test
    fun testTypeSupportX() {
        Assert.eq("'%x' % (-123,)", "'-7b'")
        Assert.eq("'%x' % (-12345678901234567890,)", "'-ab54a98ceb1f0ad2'")

        Assert.fail(
            "'%x' % (-123.0,)",
            "Operation `format(%x)` not supported on type `float`",
        )
        Assert.fail(
            "'%x' % (True,)",
            "Operation `format(%x)` not supported on type `bool`",
        )
        Assert.fail(
            "'%x' % ('abc',)",
            "Operation `format(%x)` not supported on type `string`",
        )
        Assert.fail(
            "'%x' % ([],)",
            "Operation `format(%x)` not supported on type `list`",
        )
    }

    @Test
    fun testTypeSupportE() {
        Assert.eq("'%e' % (-123,)", "'-1.230000e+02'")
        Assert.eq("'%e' % (-12345678901234567890,)", "'-1.234568e+19'")
        Assert.eq("'%e' % (-123.0,)", "'-1.230000e+02'")

        Assert.fail(
            "'%e' % (True,)",
            "Type of parameters mismatch, expected `float | int`, actual `bool (repr: True)`",
        )
        Assert.fail(
            "'%e' % ('abc',)",
            "Type of parameters mismatch, expected `float | int`, actual `string (repr:",
        )
        Assert.fail(
            "'%e' % ([],)",
            "Type of parameters mismatch, expected `float | int`, actual `list (repr",
        )
    }

    @Test
    fun testIntMin() {
        // 2147483647 is `Int.MIN_VALUE + 1`, it has corresponding positive value in Int.
        // 2147483648 is `Int.MIN_VALUE`, it has no corresponding positive value in Int.
        Assert.eq("'%o' % (-2147483647,)", "'-17777777777'")
        Assert.eq("'%o' % (-2147483648,)", "'-20000000000'")

        Assert.eq("'%x' % (-2147483647,)", "'-7fffffff'")
        Assert.eq("'%x' % (-2147483648,)", "'-80000000'")
    }
}
