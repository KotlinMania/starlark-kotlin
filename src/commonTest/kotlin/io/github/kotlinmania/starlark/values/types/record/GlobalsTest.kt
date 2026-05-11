// port-lint: source tests:src/values/types/record/globals.rs
package io.github.kotlinmania.starlark.values.types.record

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

class GlobalsTest {

    @Test
    fun testRecordPass() {
        Assert.pass(
            """
rec_type = record(host=str, port=int)
rec1 = rec_type(host = "test", port=80)
rec2 = rec_type(host = "test", port=90)
assert_eq(rec1, rec1)
assert_eq(rec1 == rec2, False)
assert_eq(rec1.host, "test")
assert_eq(rec1.port, 80)
assert_eq(dir(rec1), ["host", "port"])
""",
        )
    }

    @Test
    fun testRecordFail0() {
        Assert.fails(
            """
rec_type = record(host=str, port=int)
rec_type(host=1, port=80)
""",
            listOf(
                "Value `1` of type `int` does not match the type annotation `str` for argument `host`",
            ),
        )
    }

    @Test
    fun testRecordFail1() {
        Assert.fails(
            """
rec_type = record(host=str, port=int)
rec_type(port=80)
""",
            listOf("Missing named-only parameter", "`host`"),
        )
    }

    @Test
    fun testRecordFail2() {
        Assert.fails(
            """
rec_type = record(host=str, port=int)
rec_type(host="localhost", port=80, mask=255)
""",
            listOf("extra named", "mask"),
        )
    }

    @Test
    fun testRecordFail3() {
        Assert.pass(
            """
rec_type = record(host=str, port=int)
def foo(x: rec_type) -> rec_type:
    return x
foo(rec_type(host="localhost", port=80))""",
        )
    }

    @Test
    fun testRecordFail4() {
        Assert.pass(
            """
v = [record(host=str, port=int)]
v_0 = v[0]
def foo(y: v_0) -> v_0:
    # fails at compile time.
    return noop(y)
foo(v[0](host="localhost", port=80))""",
        )
    }

    @Test
    fun testRecordFail5() {
        Assert.pass(
            """
rec_type = record(host=str, port=field(int, 80), mask=int)
assert_eq(rec_type(host="localhost", mask=255), rec_type(host="localhost", port=80, mask=255))""",
        )
        // Make sure the default value is heap allocated (used to fail with a GC issue)
        Assert.pass(
            """
heap_string = "test{}".format(42)
rec_type = record(test_gc=field(str, heap_string))
assert_eq(rec_type().test_gc, "test42")""",
        )
    }

    @Test
    fun testRecordEquality() {
        Assert.pass(
            """
rec_type = record(host=str, port=field(int, 80))
assert_eq(rec_type(host="s"), rec_type(host="s"))
assert_eq(rec_type(host="s"), rec_type(host="s", port=80))
assert_ne(rec_type(host="s"), rec_type(host="t"))
""",
        )

        var a = Assert()
        a.module(
            "m",
            """
rec_type = record(host=str, port=field(int, 80))
rec_val = rec_type(host="s")
""",
        )
        a.pass(
            """
load('m', 'rec_type', 'rec_val')
assert_eq(rec_val, rec_type(host="s"))
assert_ne(rec_val, rec_type(host="t"))
""",
        )

        a = Assert()
        a.module(
            "m",
            """
rt = record(host=str)
""",
        )
        a.pass(
            """
load('m', r1='rt')
rt = record(host=str)
diff = record(host=str)
assert_ne(r1(host="test"), rt(host="test"))
assert_ne(r1(host="test"), diff(host="test"))
""",
        )
    }

    @Test
    fun testFieldInvalid() {
        Assert.fails(
            "field(str, None)",
            listOf("does not match the type", "`default`"),
        )
        Assert.fails("field(True)", listOf("`True`", "not a valid type"))
    }
}
