package io.github.kotlinmania.starlark.typing.tests

import kotlin.test.Test

class TypingTestsTest {
    @Test
    fun testTypeKwargs() = io.github.kotlinmania.starlark.typing.tests.testTypeKwargs()

    @Test
    fun testTypesOfArgsKwargs() = io.github.kotlinmania.starlark.typing.tests.testTypesOfArgsKwargs()

    @Test
    fun testKwargsInNativeCode() = io.github.kotlinmania.starlark.typing.tests.testKwargsInNativeCode()

    @Test
    fun testCallCallable() = io.github.kotlinmania.starlark.typing.tests.testCallCallable()

    @Test
    fun testCallNotCallable() = io.github.kotlinmania.starlark.typing.tests.testCallNotCallable()

    @Test
    fun testCallCallableOrNotCallable() = io.github.kotlinmania.starlark.typing.tests.testCallCallableOrNotCallable()

    @Test
    fun testCalls() = io.github.kotlinmania.starlark.typing.tests.testCalls()

    @Test
    fun testNeverCallBug() = io.github.kotlinmania.starlark.typing.tests.testNeverCallBug()

    @Test
    fun testCallPosOnly() = io.github.kotlinmania.starlark.typing.tests.testCallPosOnly()

    @Test
    fun testCallableWithArgs() = io.github.kotlinmania.starlark.typing.tests.testCallableWithArgs()

    @Test
    fun testCallableNamed() = io.github.kotlinmania.starlark.typing.tests.testCallableNamed()

    @Test
    fun testIntMulList() = io.github.kotlinmania.starlark.typing.tests.testIntMulList()

    @Test
    fun testListAppend() = io.github.kotlinmania.starlark.typing.tests.testListAppend()

    @Test
    fun testListAppendBug() = io.github.kotlinmania.starlark.typing.tests.testListAppendBug()

    @Test
    fun testListFunction() = io.github.kotlinmania.starlark.typing.tests.testListFunction()

    @Test
    fun testListLess() = io.github.kotlinmania.starlark.typing.tests.testListLess()

    @Test
    fun testListBinOp() = io.github.kotlinmania.starlark.typing.tests.testListBinOp()

    @Test
    fun testSpecialFunctionStruct() = io.github.kotlinmania.starlark.typing.tests.testSpecialFunctionStruct()

    @Test
    fun testSpecialFunctionZip() = io.github.kotlinmania.starlark.typing.tests.testSpecialFunctionZip()

    @Test
    fun testTuple() = io.github.kotlinmania.starlark.typing.tests.testTuple()

    @Test
    fun testTupleEllipsis() = io.github.kotlinmania.starlark.typing.tests.testTupleEllipsis()

    @Test
    fun testTypeAlias() = io.github.kotlinmania.starlark.typing.tests.testTypeAlias()

    @Test
    fun testIncorrectTypeDot() = io.github.kotlinmania.starlark.typing.tests.testIncorrectTypeDot()

    @Test
    fun testFunctionAsTypeBitOr() = io.github.kotlinmania.starlark.typing.tests.testFunctionAsTypeBitOr()
}
