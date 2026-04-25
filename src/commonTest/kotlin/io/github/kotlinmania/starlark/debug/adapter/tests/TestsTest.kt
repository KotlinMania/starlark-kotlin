package io.github.kotlinmania.starlark.debug.adapter.tests

import kotlin.test.Test

class TestsTest {
    @Test
    fun testBreakpointTest() {
        t.testBreakpoint()
    }

    @Test
    fun testBreakpointWithFailingConditionTest() {
        t.testBreakpointWithFailingCondition()
    }

    @Test
    fun testBreakpointWithPassingConditionTest() {
        t.testBreakpointWithPassingCondition()
    }

    @Test
    fun testStepOverTest() {
        t.testStepOver()
    }

    @Test
    fun testStepIntoTest() {
        t.testStepInto()
    }

    @Test
    fun testStepOutTest() {
        t.testStepOut()
    }

    @Test
    fun testLocalVariablesTest() {
        t.testLocalVariables()
    }

    @Test
    fun testInspectVariablesTest() {
        t.testInspectVariables()
    }

    @Test
    fun testEvaluateExpressionTest() {
        t.testEvaluateExpression()
    }

    @Test
    fun testTruncateStringTest() {
        t.testTruncateString()
    }
}

