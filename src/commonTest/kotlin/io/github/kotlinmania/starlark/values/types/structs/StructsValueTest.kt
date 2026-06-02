// port-lint: tests src/values/types/structs/value.rs
package io.github.kotlinmania.starlark.values.types.structs

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

class StructsValueTest {
    @Test
    fun testRepr() {
        Assert.eq("repr(struct(a=1, b=[]))", "'struct(a=1, b=[])'")
        Assert.eq("str(struct(a=1, b=[]))", "'struct(a=1, b=[])'")
    }

    @Test
    fun testReprCycle() {
        Assert.eq(
            "l = []; s = struct(f=l); l.append(s); repr(s)",
            "'struct(f=[struct(...)])'",
        )
        Assert.eq(
            "l = []; s = struct(f=l); l.append(s); str(s)",
            "'struct(f=[struct(...)])'",
        )
    }

    @Test
    fun testToJsonCycle() {
        Assert.fail(
            "l = []; s = struct(f=l); l.append(s); json.encode(s)",
            "Cycle detected when serializing value of type `struct` to JSON",
        )
    }

    @Test
    fun testToJson() {
        Assert.allTrue(
            """
json.encode(struct(key = None)) == '{"key":null}'
json.encode(struct(key = True)) == '{"key":true}'
json.encode(struct(key = False)) == '{"key":false}'
json.encode(struct(key = 42)) == '{"key":42}'
json.encode(struct(key = 'value')) == '{"key":"value"}'
json.encode(struct(key = 'value"')) == '{"key":"value\\\""}'
json.encode(struct(key = 'value\\')) == '{"key":"value\\\\"}'
json.encode(struct(key = 'value/')) == '{"key":"value/"}'
json.encode(struct(key = 'value')) == '{"key":"value\\b"}'
json.encode(struct(key = 'value')) == '{"key":"value\\f"}'
json.encode(struct(key = 'value\n')) == '{"key":"value\\n"}'
json.encode(struct(key = 'value\r')) == '{"key":"value\\r"}'
json.encode(struct(key = 'value\t')) == '{"key":"value\\t"}'
json.encode(struct(foo = 42, bar = "some")) == '{"foo":42,"bar":"some"}'
json.encode(struct(foo = struct(bar = "some"))) == '{"foo":{"bar":"some"}}'
json.encode(struct(foo = ["bar/", "some"])) == '{"foo":["bar/","some"]}'
json.encode(struct(foo = [struct(bar = "some")])) == '{"foo":[{"bar":"some"}]}'
""",
        )
    }

    @Test
    fun testComparisonBug() {
        // TODO(nga): this should be false, because `a < b`,
        //   and comparisons are usually lexicographic.
        // TODO(nga): Also, since structs are ordered, but not sorted,
        //   comparisons on structs should be forbidden
        //   (because it is too expensive to sort keys on each comparison).
        Assert.isTrue("struct(b=1) < struct(a=1, x=1)")
    }
}
