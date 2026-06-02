// port-lint: tests src/stdlib/extra.rs (tests)
package io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.assert.pass
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtraTest {
    @Test
    fun testFilter() {
        val a = Assert()
        a.disableStaticTypechecking()
        a.pass(
            """
def contains_hello(s):
    if "hello" in s:
        return True
    return False

def positive(i):
    return i > 0

assert_eq([], filter(positive, []))
assert_eq([1, 2, 3], filter(positive, [1, 2, 3]))
assert_eq([], filter(positive, [-1, -2, -3]))
assert_eq([1, 2, 3], filter(positive, [-1, 1, 2, -2, -3, 3]))
assert_eq(["hello world!"], filter(contains_hello, ["hello world!", "goodbye"]))
""",
        )
    }

    @Test
    fun testMap() {
        val a = Assert()
        a.disableStaticTypechecking()
        a.pass(
            """
def double(x):
    return x + x

assert_eq([], map(int, []))
assert_eq([1,2,3], map(int, ["1","2","3"]))
assert_eq(["0","1","2"], map(str, range(3)))
assert_eq(["11",8], map(double, ["1",4]))
""",
        )
    }

    @Test
    fun testDebug() {
        pass(
            """assert_eq(
                debug([1,2]),
                "Value(ListGen(ListData { content: Cell { value: ValueTyped(Value(Array { len: 2, capacity: 2, iter_count: 0, content: [Value(1), Value(2)] })) } }))"
                )""",
        )
    }

    @Test
    fun testPrint() {
        var captured = ""
        val printHandler =
            object : io.github.kotlinmania.starlark.stdlib.PrintHandler {
                override fun println(text: String): Result<Unit> {
                    captured = text
                    return Result.success(Unit)
                }
            }
        val a = Assert()
        a.setPrintHandler(printHandler)
        a.pass("print('hw')")
        assertEquals("hw", captured)
    }

    @Test
    fun testPstr() {
        pass(
            """
assert_eq(pstr([]), "[]")
assert_eq(pstr([1,2,[]]), ${"\"\"\""}[
  1,
  2,
  []
]${"\"\"\""})
assert_eq(pstr("abcd"), "abcd")
""",
        )
    }

    @Test
    fun testPrepr() {
        pass(
            """
assert_eq(prepr([]), "[]")
assert_eq(prepr([1,2,[]]), ${"\"\"\""}[
  1,
  2,
  []
]${"\"\"\""})
assert_eq(prepr("abcd"), "\"abcd\"")
""",
        )
    }
}
