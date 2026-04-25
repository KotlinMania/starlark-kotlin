package io.github.kotlinmania.starlark_kotlin.tests

import io.github.kotlinmania.starlark_kotlin.tests.testContextCaptured as testContextCapturedImpl
import io.github.kotlinmania.starlark_kotlin.tests.testDefFreeze as testDefFreezeImpl
import io.github.kotlinmania.starlark_kotlin.tests.testDoubleCaptureAndFreeze as testDoubleCaptureAndFreezeImpl
import io.github.kotlinmania.starlark_kotlin.tests.testFrozenLambda as testFrozenLambdaImpl
import io.github.kotlinmania.starlark_kotlin.tests.testFrozenLambdaNest as testFrozenLambdaNestImpl
import io.github.kotlinmania.starlark_kotlin.tests.testLambda as testLambdaImpl
import io.github.kotlinmania.starlark_kotlin.tests.testLambdaCaptureFromDef as testLambdaCaptureFromDefImpl
import io.github.kotlinmania.starlark_kotlin.tests.testLambdaCaptureFromModule as testLambdaCaptureFromModuleImpl
import io.github.kotlinmania.starlark_kotlin.tests.testLambdaCaptureReassignedFromDef as testLambdaCaptureReassignedFromDefImpl
import io.github.kotlinmania.starlark_kotlin.tests.testLambdaErrors as testLambdaErrorsImpl
import io.github.kotlinmania.starlark_kotlin.tests.testLambdaErrorsNested as testLambdaErrorsNestedImpl
import io.github.kotlinmania.starlark_kotlin.tests.testNestedDef1 as testNestedDef1Impl
import io.github.kotlinmania.starlark_kotlin.tests.testNestedDef2 as testNestedDef2Impl
import io.github.kotlinmania.starlark_kotlin.tests.testNestedDef3 as testNestedDef3Impl
import kotlin.test.Test

class DefTest {
    @Test
    fun testLambda() = testLambdaImpl()

    @Test
    fun testFrozenLambda() = testFrozenLambdaImpl()

    @Test
    fun testNestedDef1() = testNestedDef1Impl()

    @Test
    fun testNestedDef2() = testNestedDef2Impl()

    @Test
    fun testNestedDef3() = testNestedDef3Impl()

    @Test
    fun testLambdaCaptureFromModule() = testLambdaCaptureFromModuleImpl()

    @Test
    fun testLambdaCaptureFromDef() = testLambdaCaptureFromDefImpl()

    @Test
    fun testLambdaCaptureReassignedFromDef() = testLambdaCaptureReassignedFromDefImpl()

    @Test
    fun testDefFreeze() = testDefFreezeImpl()

    @Test
    fun testFrozenLambdaNest() = testFrozenLambdaNestImpl()

    @Test
    fun testContextCaptured() = testContextCapturedImpl()

    @Test
    fun testLambdaErrors() = testLambdaErrorsImpl()

    @Test
    fun testLambdaErrorsNested() = testLambdaErrorsNestedImpl()

    @Test
    fun testDoubleCaptureAndFreeze() = testDoubleCaptureAndFreezeImpl()
}
