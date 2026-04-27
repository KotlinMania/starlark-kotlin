// port-lint: source tests:src/values/types/enumeration/enumType.rs
// Also covers tests originating in src/values/types/enumeration/globals.rs.
package io.github.kotlinmania.starlark.values.types.enumeration

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

class EnumTypeTest {

    // ----------------------------------------------------------------
    // Tests from src/values/types/enumeration/globals.rs
    // ----------------------------------------------------------------

    @Test
    fun testEnum() {
        Assert.pass(
            """
            enum_type = enum("option1", "option2", "option3")
            x = enum_type("option1")
            assert_eq(x.value, "option1")
            assert_eq(enum_type("option3").value, "option3")
            assert_eq(enum_type.type, "enum_type")
            """.trimIndent()
        )
        Assert.fails(
            """
            enum_type = enum("option1", "option2", "option3")
            enum_type(False)
            """.trimIndent(),
            listOf("Unknown enum element", "`False`", "option1"),
        )
        Assert.fails(
            """
            enum_type = enum("option1", "option2", "option3")
            enum_type("option4")
            """.trimIndent(),
            listOf("Unknown enum element", "`option4`"),
        )
        Assert.fails(
            """
            enum_type = enum("option1", "option1")
            enum_type("option3")
            """.trimIndent(),
            listOf("distinct", "option1"),
        )
        Assert.pass(
            """
            enum_type = enum("option1","option2")
            def foo(x: enum_type) -> enum_type:
                return x
            foo(enum_type("option1"))
            """.trimIndent()
        )
        Assert.pass(
            """
            v = [enum("option1","option2")]
            v_0 = v[0]
            def foo(y: v_0) -> v_0:
                # TODO(nga): fails at compile time.
                return noop(y)
            foo(v[0]("option1"))
            """.trimIndent()
        )
        Assert.pass(
            """
            enum_type = enum("option1","option2")
            assert_eq(enum_type.values(), ["option1","option2"])
            assert_eq([enum_type[i].value for i in range(len(enum_type))], ["option1","option2"])
            assert_eq(enum_type("option2").index, 1)
            assert_eq([x.value for x in enum_type], ["option1","option2"])
            """.trimIndent()
        )
        Assert.pass(
            """
            enum_type = enum("option1","option2")
            x = enum_type("option1")
            assert_eq(str(enum_type), "enum(\"option1\", \"option2\")")
            assert_eq(str(x), "enum_type(\"option1\")")
            """.trimIndent()
        )
        Assert.pass(
            """
            enum_type = enum("option1","option2")
            repr(enum_type) # Check it is finite
            """.trimIndent()
        )
    }

    @Test
    fun testEnumEquality() {
        Assert.pass(
            """
            enum_type = enum("option1", "option2", "option3")
            assert_eq(enum_type("option1"), enum_type("option1"))
            assert_eq(enum_type("option3"), enum_type("option3"))
            assert_ne(enum_type("option1"), enum_type("option3"))
            """.trimIndent()
        )

        var a = io.github.kotlinmania.starlark.assert.Assert()
        a.module(
            "m",
            """
            enum_type = enum("option1", "option2", "option3")
            enum_val = enum_type("option1")
            """.trimIndent()
        )
        a.pass(
            """
            load('m', 'enum_type', 'enum_val')
            assert_eq(enum_val, enum_type("option1"))
            assert_ne(enum_val, enum_type("option3"))
            """.trimIndent()
        )

        a = io.github.kotlinmania.starlark.assert.Assert()
        a.module(
            "m1",
            """
            rt = enum("one")
            """.trimIndent()
        )
        a.module(
            "m2",
            """
            rt = enum("one", "two")
            """.trimIndent()
        )
        a.pass(
            """
            load('m1', r1='rt')
            load('m2', r2='rt')
            rt = enum("one")
            diff = enum("one")
            assert_ne(r1("one"), rt("one"))
            assert_ne(rt("one"), r2("one"))
            assert_ne(rt("one"), diff("one"))
            """.trimIndent()
        )
    }

    @Test
    fun testEnumRepr() {
        Assert.pass(
            """
            enum_type = enum("option1", "option2")
            assert_eq("enum_type(\"option1\")", repr(enum_type("option1")))
            assert_eq("enum()(\"option1\")", repr(enum("option1", "option2")("option1")))
            """.trimIndent()
        )
    }

    // ----------------------------------------------------------------
    // Tests from src/values/types/enumeration/enumType.rs
    // ----------------------------------------------------------------

    @Test
    fun testEnumTypeAsTypePass() {
        Assert.pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def f_pass(x: Color):
                pass

            def g_pass(x: Color):
                f_pass(x)
            """.trimIndent()
        )
    }

    @Test
    fun testEnumTypeFailRuntime() {
        Assert.fail(
            """
            Color = enum("RED", "GREEN", "BLUE")
            Season = enum("SPRING", "SUMMER", "AUTUMN", "WINTER")

            def f(x: Color):
                pass

            def g(x):
                f(x)

            g(Season[0])
            """.trimIndent(),
            "Value `Season(\"SPRING\")` of type `enum` does not match the type annotation `Color` for argument `x`",
        )
    }

    @Test
    fun testEnumTypeFailCompileTime() {
        Assert.fail(
            """
            Color = enum("RED", "GREEN", "BLUE")
            Season = enum("SPRING", "SUMMER", "AUTUMN", "WINTER")

            def f(x: Color):
                pass

            def g(x: Season):
                f(x)
            """.trimIndent(),
            "Expected type `Color` but got `Season`",
        )
    }

    @Test
    fun testEnumIsCallable() {
        Assert.pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def foo(x: typing.Callable):
                pass

            def bar():
                foo(Color)
            """.trimIndent()
        )
    }

    @Test
    fun testEnumValueIndex() {
        // Test `.index` is available at both compile and runtime.
        Assert.pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def test():
                for c in Color:
                    if c.index == 1:
                        pass

            test()
            """.trimIndent()
        )
    }

    @Test
    fun testEnumValueIndexCorrectType() {
        Assert.fail(
            """
            Fruit = enum("APPLE", "BANANA", "ORANGE")

            def expect_str(s: str):
                pass

            def test():
                for f in Fruit:
                    expect_str(f.index)
            """.trimIndent(),
            "Expected type `str` but got `int`",
        )
    }

    @Test
    fun testEnumIndex() {
        Assert.pass(
            """
            Mood = enum("HAPPY", "SAD")

            def test() -> Mood:
                return Mood[0]

            test()
            """.trimIndent()
        )
    }

    @Test
    fun testEnumIndexFail() {
        Assert.fail(
            """
            Shape = enum("SQUARE", "CIRCLE")

            def accept_str(s: str):
                pass

            def test():
                accept_str(Shape[0])
            """.trimIndent(),
            "Expected type `str` but got `Shape`",
        )
    }

    @Test
    fun testEnumCall() {
        Assert.fail(
            """
            Currency = enum("GBP", "USD", "EUR")

            def accept_str(s: str):
                pass

            def test():
                accept_str(Currency("GBP"))
            """.trimIndent(),
            "Expected type `str` but got `Currency`",
        )
    }

    @Test
    fun testEnumAttributeAccess() {
        Assert.pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def test():
                red = Color.RED
                green = Color.GREEN
                blue = Color.BLUE

                assert_eq(red, Color("RED"))
                assert_eq(green, Color("GREEN"))
                assert_eq(blue, Color("BLUE"))

                assert_eq(red.value, "RED")
                assert_eq(green.value, "GREEN")
                assert_eq(blue.value, "BLUE")

            test()
            """.trimIndent()
        )
    }

    @Test
    fun testEnumAttributeAccessInvalid() {
        Assert.fail(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def test():
                purple = Color.PURPLE

            test()
            """.trimIndent(),
            "Object of type `function` has no attribute `PURPLE`",
        )
    }

    @Test
    fun testEnumAttributeAccessType() {
        Assert.fail(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def foo() -> str:
                return Color.RED
            """.trimIndent(),
            "Expected type `str` but got `Color`",
        )
    }
}
