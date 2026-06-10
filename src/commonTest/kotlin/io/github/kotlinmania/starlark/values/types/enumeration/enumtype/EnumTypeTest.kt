// port-lint: tests src/values/types/enumeration/enum_type.rs
package io.github.kotlinmania.starlark.values.types.enumeration.enumtype

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

class EnumTypeTest {
    @Test
    fun testEnumTypeAsTypePass() {
        Assert().pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def f_pass(x: Color):
                pass

            def g_pass(x: Color):
                f_pass(x)
            """.trimIndent(),
        )
    }

    @Test
    fun testEnumTypeFailRuntime() {
        Assert().fail(
            """
            Color = enum("RED", "GREEN", "BLUE")
            Season = enum("SPRING", "SUMMER", "AUTUMN", "WINTER")

            def f(x: Color):
                pass

            def g(x):
                f(x)

            g(Season[0])
            """.trimIndent(),
            "does not match the type annotation",
        )
    }

    @Test
    fun testEnumTypeFailCompileTime() {
        Assert().fail(
            """
            Color = enum("RED", "GREEN", "BLUE")
            Season = enum("SPRING", "SUMMER", "AUTUMN", "WINTER")

            def f(x: Color):
                pass

            def g(x: Season):
                f(x)
            """.trimIndent(),
            "Expected type",
        )
    }

    @Test
    fun testEnumIsCallable() {
        Assert().pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def foo(x: typing.Callable):
                pass

            def bar():
                foo(Color)
            """.trimIndent(),
        )
    }

    @Test
    fun testEnumValueIndex() {
        Assert().pass(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def test():
                for c in Color:
                    if c.index == 1:
                        pass

            test()
            """.trimIndent(),
        )
    }

    @Test
    fun testEnumValueIndexCorrectType() {
        Assert().fail(
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
        Assert().pass(
            """
            Mood = enum("HAPPY", "SAD")

            def test() -> Mood:
                return Mood[0]

            test()
            """.trimIndent(),
        )
    }

    @Test
    fun testEnumIndexFail() {
        Assert().fail(
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
        Assert().fail(
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
        Assert().pass(
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
            """.trimIndent(),
        )
    }

    @Test
    fun testEnumAttributeAccessInvalid() {
        Assert().fail(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def test():
                purple = Color.PURPLE

            test()
            """.trimIndent(),
            "PURPLE",
        )
    }

    @Test
    fun testEnumAttributeAccessType() {
        Assert().fail(
            """
            Color = enum("RED", "GREEN", "BLUE")

            def foo() -> str:
                return Color.RED
            """.trimIndent(),
            "Expected type `str` but got `Color`",
        )
    }
}