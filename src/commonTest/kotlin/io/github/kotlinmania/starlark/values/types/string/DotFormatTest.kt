// port-lint: source tests:src/values/types/string/dot_format.rs
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
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.dict.Dict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DotFormatTest {
    private fun formatCaptureForTest(
        capture: String,
        conv: FormatConv,
        args: FormatArgs<Iterator<Value>>,
        kwargs: Dict,
    ): Result<String> {
        val result = StringBuilder()
        return formatCapture(capture, conv, args, kwargs, result).map { result.toString() }
    }

    @Test
    fun testFormatCapture() {
        Heap.temp { heap ->
            val originalArgs = listOf(heap.allocStr("1"), heap.allocStr("2"), heap.allocStr("3"))
            var args = FormatArgs.new(originalArgs.iterator())
            val kwargsMap = SmallMap.new<Value, Value>()

            kwargsMap.insertHashed(heap.allocStr("a").getHashed().getOrThrow(), heap.allocStr("x"))
            kwargsMap.insertHashed(heap.allocStr("b").getHashed().getOrThrow(), heap.allocStr("y"))
            kwargsMap.insertHashed(heap.allocStr("c").getHashed().getOrThrow(), heap.allocStr("z"))
            val kwargs = Dict.new(kwargsMap)
            assertEquals(
                "1",
                formatCaptureForTest("", FormatConv.Str, args, kwargs).getOrThrow(),
            )
            assertEquals(
                "2",
                formatCaptureForTest("", FormatConv.Str, args, kwargs).getOrThrow(),
            )
            assertEquals(
                "\"3\"",
                formatCaptureForTest("", FormatConv.Repr, args, kwargs).getOrThrow(),
            )
            assertEquals(
                "\"x\"",
                formatCaptureForTest("a", FormatConv.Repr, args, kwargs).getOrThrow(),
            )
            assertEquals(
                "x",
                formatCaptureForTest("a", FormatConv.Str, args, kwargs).getOrThrow(),
            )
            assertTrue(formatCaptureForTest("1", FormatConv.Str, args, kwargs).isFailure)
            args = FormatArgs.new(originalArgs.iterator())
            assertEquals(
                "2",
                formatCaptureForTest("1", FormatConv.Str, args, kwargs).getOrThrow(),
            )
            assertTrue(formatCaptureForTest("", FormatConv.Str, args, kwargs).isFailure)
        }
    }

    @Test
    fun testFormat() {
        Assert.eq("'a{x}b{y}c{}'.format(1, x=2, y=3)", "'a2b3c1'")
        Assert.eq("'a{x}b{{y}}c{}'.format(1, x=2)", "'a2b{y}c1'")
    }

    @Test
    fun testParseFormatOne() {
        assertEquals(Pair("abc", "def"), parseFormatOne("abc{}def"))
        assertEquals(Pair("abc", "def"), parseFormatOne("abc{!s}def"))
        assertNull(parseFormatOne("abc{!r}def"))
        assertEquals(Pair("a{b", "c}d{"), parseFormatOne("a{{b{}c}}d{{"))
        assertNull(parseFormatOne("a{"))
        assertNull(parseFormatOne("a{}{}"))
        assertNull(parseFormatOne("{x}"))
    }
}
