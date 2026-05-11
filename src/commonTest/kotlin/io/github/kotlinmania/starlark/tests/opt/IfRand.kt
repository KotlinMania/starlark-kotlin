// port-lint: source tests/opt/if_rand.rs
package io.github.kotlinmania.starlark.tests.opt

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

/** Permutations tests for if condition evaluation. */

import io.github.kotlinmania.starlark.any.AnyLifetime
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Module
import kotlin.random.Random
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.syntax.AstModule
import kotlin.test.Test

/**
 * Count side effects. For example, in expression like:
 *
 * ```text
 * false() and true()
 * ```
 *
 * `true()` should not be evaluated. After evaluation, the counter should be 1.
 */
private class CountCalls : AnyLifetime {
    var calls: Int = 0
    override fun staticTypeId() = CountCalls::class
    override fun staticTypeOf() = CountCalls::class
}

private fun boolFns(builder: GlobalsBuilder) {
    /** Return `true` and record side effect. */
    fun `true`(eval: io.github.kotlinmania.starlark.eval.runtime.Evaluator): Result<Boolean> {
        val calls = eval.extra as CountCalls
        calls.calls += 1
        return Result.success(true)
    }

    fun `false`(eval: io.github.kotlinmania.starlark.eval.runtime.Evaluator): Result<Boolean> {
        val calls = eval.extra as CountCalls
        calls.calls += 1
        return Result.success(false)
    }

    builder.setFunction("true") { _, eval -> `true`(eval) }
    builder.setFunction("false") { _, eval -> `false`(eval) }
}

private enum class TestBinOp(val display: String) {
    And("and"),
    Or("or");

    fun eval(x: Boolean, y: () -> Boolean): Boolean {
        return when (this) {
            And -> x && y()
            Or -> x || y()
        }
    }

    override fun toString(): String = display
}

private sealed class TestExpr {
    /** `True` or `False`. */
    class Const(val value: Boolean) : TestExpr()

    /** `true()` or `false()`. */
    class Count(val value: Boolean) : TestExpr()

    /** Binary operation. */
    class BinOp(val op: TestBinOp, val lhs: TestExpr, val rhs: TestExpr) : TestExpr()

    /** `not` operation. */
    class Not(val expr: TestExpr) : TestExpr()

    /** Evaluate the expression the same way Starlark would evaluate it. */
    fun eval(count: CountCalls): Boolean {
        return when (this) {
            is Const -> value
            is Count -> {
                // Record side effect.
                count.calls += 1
                value
            }
            is BinOp -> {
                op.eval(lhs.eval(count)) { rhs.eval(count) }
            }
            is Not -> !expr.eval(count)
        }
    }

    override fun toString(): String {
        return when (this) {
            is Const -> if (value) "True" else "False"
            is Count -> if (value) "true()" else "false()"
            is BinOp -> "($lhs $op $rhs)"
            is Not -> "(not $expr)"
        }
    }
}

/**
 * Evaluate the program.
 *
 * * Return the result which is expected to be `bool`.
 * * Count side effects.
 */
private fun evalProgram(program: String): Pair<Boolean, CountCalls> {
    val ast = AstModule.parse("t.star", program, Dialect.AllOptionsInternal)
        .getOrThrow()

    val globalsBuilder = GlobalsBuilder.standard()
    boolFns(globalsBuilder)
    val globals = globalsBuilder.build()

    val counts = CountCalls()
    val r = Module.withTempHeap { module ->
        val eval = Evaluator(module)
        eval.extra = counts
        val r = eval.evalModule(ast, globals).getOrThrow()
        Result.success(r.unpackBool()!!)
    }.getOrThrow()
    return Pair(r, counts)
}

private fun evalIfElseWithStarlark(expr: TestExpr): Pair<Boolean, CountCalls> {
    val program = """
if $expr:
    r = True
else:
    r = False
r
"""
    return evalProgram(program)
}

private fun evalIfWithStarlark(expr: TestExpr): Pair<Boolean, CountCalls> {
    val program = """
r = False
if $expr:
    r = True
r
"""
    return evalProgram(program)
}

private fun evalExprResult(expr: TestExpr): Pair<Boolean, CountCalls> {
    return evalProgram(expr.toString())
}

private fun evalManually(expr: TestExpr): Pair<Boolean, CountCalls> {
    val counts = CountCalls()
    val r = expr.eval(counts)
    return Pair(r, counts)
}

private fun testIfElse(expr: TestExpr) {
    val expected = evalManually(expr)
    val actual = evalIfElseWithStarlark(expr)
    check(expected.first == actual.first && expected.second.calls == actual.second.calls) {
        "expression: $expr, expected: $expected, actual: $actual"
    }
}

private fun testIf(expr: TestExpr) {
    val expected = evalManually(expr)
    val actual = evalIfWithStarlark(expr)
    check(expected.first == actual.first && expected.second.calls == actual.second.calls) {
        "expression: $expr, expected: $expected, actual: $actual"
    }
}

private fun testExprResult(expr: TestExpr) {
    val expected = evalManually(expr)
    val actual = evalExprResult(expr)
    check(expected.first == actual.first && expected.second.calls == actual.second.calls) {
        "expression: $expr, expected: $expected, actual: $actual"
    }
}

private fun testIfs(expr: TestExpr) {
    testIf(expr)
    testIfElse(expr)
    // If condition expression compilation is different from compilation of the expression,
    // so we explicitly test both cases.
    testExprResult(expr)
}

private fun boolValues(): List<Boolean> = listOf(true, false)

private fun basicBoolExprs(): List<TestExpr> {
    return boolValues().flatMap { x ->
        listOf(TestExpr.Count(x), TestExpr.Const(x))
    }
}

class IfRandTests {
    @Test
    fun testBasic() {
        testIfs(TestExpr.Const(true))
        testIfs(TestExpr.Const(false))
        testIfs(TestExpr.Count(true))
        testIfs(TestExpr.Count(false))
        testIfs(TestExpr.Not(TestExpr.Const(true)))
        testIfs(TestExpr.Not(TestExpr.Const(false)))
        testIfs(TestExpr.Not(TestExpr.Count(true)))
        testIfs(TestExpr.Not(TestExpr.Count(false)))
    }

    @Test
    fun testAnd() {
        for (lhs in basicBoolExprs()) {
            for (rhs in basicBoolExprs()) {
                testIfs(TestExpr.BinOp(TestBinOp.And, lhs, rhs))
            }
        }
    }

    @Test
    fun testOr() {
        for (lhs in basicBoolExprs()) {
            for (rhs in basicBoolExprs()) {
                testIfs(TestExpr.BinOp(TestBinOp.Or, lhs, rhs))
            }
        }
    }

    @Test
    fun testAndOrNot() {
        for (lhs in basicBoolExprs()) {
            for (rhs in basicBoolExprs()) {
                for (negateLhs in listOf(false, true)) {
                    for (negateRhs in listOf(false, true)) {
                        for (binOp in listOf(TestBinOp.And, TestBinOp.Or)) {
                            val l = if (negateLhs) TestExpr.Not(lhs) else lhs
                            val r = if (negateRhs) TestExpr.Not(rhs) else rhs
                            testIfs(TestExpr.BinOp(binOp, l, r))
                        }
                    }
                }
            }
        }
    }

    private const val RANDOM_ITERATIONS = 100

    private fun maxDepthForIter(i: Int): Int {
        return if (i < 5) {
            0
        } else if (i < RANDOM_ITERATIONS / 50) {
            1
        } else if (i < RANDOM_ITERATIONS / 25) {
            2
        } else if (i < RANDOM_ITERATIONS / 10) {
            3
        } else if (i < RANDOM_ITERATIONS / 3) {
            4
        } else if (i < RANDOM_ITERATIONS / 2) {
            5
        } else {
            20
        }
    }

    private fun randomExpr(rng: Random, maxDepth: Int): TestExpr {
        fun randomSimpleExpr(): TestExpr {
            return when (rng.nextInt(4)) {
                0 -> TestExpr.Const(true)
                1 -> TestExpr.Const(false)
                2 -> TestExpr.Count(true)
                3 -> TestExpr.Count(false)
                else -> error("unreachable")
            }
        }

        return if (maxDepth == 0) {
            randomSimpleExpr()
        } else {
            when (rng.nextInt(4)) {
                0 -> randomSimpleExpr()
                1 -> TestExpr.Not(randomExpr(rng, maxDepth - 1))
                2 -> TestExpr.BinOp(
                    TestBinOp.And,
                    randomExpr(rng, maxDepth - 1),
                    randomExpr(rng, maxDepth - 1),
                )
                3 -> TestExpr.BinOp(
                    TestBinOp.Or,
                    randomExpr(rng, maxDepth - 1),
                    randomExpr(rng, maxDepth - 1),
                )
                else -> error("unreachable")
            }
        }
    }

    @Test
    fun testIfRandom() {
        val rng = Random(17)
        for (i in 0 until RANDOM_ITERATIONS) {
            val maxDepth = maxDepthForIter(i)
            val expr = randomExpr(rng, maxDepth)
            testIf(expr)
        }
    }

    @Test
    fun testIfElseRandom() {
        val rng = Random(17)
        for (i in 0 until RANDOM_ITERATIONS) {
            val maxDepth = maxDepthForIter(i)
            val expr = randomExpr(rng, maxDepth)
            testIfElse(expr)
        }
    }

    @Test
    fun testExprRandom() {
        val rng = Random(17)
        for (i in 0 until RANDOM_ITERATIONS) {
            val maxDepth = maxDepthForIter(i)
            val expr = randomExpr(rng, maxDepth)
            testExprResult(expr)
        }
    }
}
