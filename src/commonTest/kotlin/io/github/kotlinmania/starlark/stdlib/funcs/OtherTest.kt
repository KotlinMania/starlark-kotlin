// port-lint: tests src/stdlib/funcs/other.rs
package io.github.kotlinmania.starlark.stdlib.funcs

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
import io.github.kotlinmania.starlark.assert.allTrue
import io.github.kotlinmania.starlark.assert.eq
import io.github.kotlinmania.starlark.assert.fail
import io.github.kotlinmania.starlark.assert.isTrue
import kotlin.test.Test

class OtherTest {
    // #[test]
    @Test
    fun testAbs() {
        eq("1", "abs(1)")
        eq("1", "abs(-1)")
        eq("2147483647", "abs(2147483647)")
        eq("2147483648", "abs(-2147483648)")
        eq("2147483648000", "abs(2147483648000)")
        eq("2147483648000", "abs(-2147483648000)")
        eq("1.23", "abs(-1.23)")
        eq("2.3", "abs(2.3)")
        isTrue("isinstance(abs(1), int)")
    }

    // #[test]
    @Test
    fun testConstants() {
        isTrue("not None")
        isTrue("not False")
        isTrue("True")
    }

    // #[test]
    @Test
    fun testChr() {
        fail("chr(0x110000)", "not a valid UTF-8")
        fail("chr(-1)", "negative")
    }

    // #[test]
    @Test
    fun testHash() {
        eq("0", "hash('')")
        eq("97", "hash('a')")
        eq("3105", "hash('ab')")
        eq("96354", "hash('abc')")
        eq("2987074", "hash('abcd')")
        eq("92599395", "hash('abcde')")
        eq("-1424385949", "hash('abcdef')")
        allTrue(
            """
hash("te") == hash("te")
hash("te") != hash("st")
x = "test"; y = "te" + "st"; hash(y) == hash(y)
""",
        )
        fail("noop(hash)(None)", "doesn't match")
        fail("noop(hash)(True)", "doesn't match")
        fail("noop(hash)(1)", "doesn't match")
        fail("noop(hash)([])", "doesn't match")
        fail("noop(hash)({})", "doesn't match")
        fail("noop(hash)(range(1))", "doesn't match")
        fail("noop(hash)((1, 2))", "doesn't match")
        fail(
            """
def foo():
    pass
noop(hash)(foo)
""",
            "doesn't match",
        )
    }

    // #[test]
    @Test
    fun testInt() {
        eq("2147483647", "int('2147483647')")
        eq("-2147483647 - 1", "int('-2147483648')")
        eq("0", "int('0')")
        eq("0", "int('-0')")
        eq(
            "999999999999999945322333868247445125709646570021247924665841614848",
            "int(1e66)",
        )
        eq("2147483648", "int('2147483648')")
        eq("-2147483649", "int('-2147483649')")
    }

    // #[test]
    @Test
    fun testTuple() {
        val a = Assert()
        // TODO(nga): fix and enable.
        a.disableStaticTypechecking()
        a.eq("(1, 2)", "tuple((1, 2))")
        a.eq("(1, 2)", "tuple([1, 2])")
    }
}
