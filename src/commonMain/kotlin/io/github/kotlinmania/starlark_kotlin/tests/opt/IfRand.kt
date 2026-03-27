// port-lint: source src/tests/opt/if_rand.rs
package io.github.kotlinmania.starlark_kotlin.tests.opt

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

//! Permutations tests for if condition evaluation.

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Module
import kotlin.random.Random
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.assert.parse
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.extra
import io.github.kotlinmania.starlark_kotlin.values.owned.unpackBool
import io.github.kotlinmania.starlark_kotlin.eval.evalModule
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

/// Count side effects. For example, in expression like:
///
/// ```text
/// false() and true()
/// ```
///
/// `true()` should not be evaluated. After evaluation, the counter should be 1.
// #[derive(Debug, ProvidesStaticType, Default, PartialEq)]
// struct CountCalls
private class CountCalls {
    var calls: Int = 0
}

// #[starlark_module]
// fn bool_fns(globals: &mut GlobalsBuilder)
private fun boolFns(builder: GlobalsBuilder) {
    /// Return `true` and record side effect.
    // fn r#true(eval: &mut Evaluator) -> anyhow::Result<bool>
    builder.setFunction("true") { _, eval ->
        val calls = eval.extra as CountCalls
        calls.calls += 1
        Result.success(true)
    }

    // fn r#false(eval: &mut Evaluator) -> anyhow::Result<bool>
    builder.setFunction("false") { _, eval ->
        val calls = eval.extra as CountCalls
        calls.calls += 1
        Result.success(false)
    }
}

// #[derive(Display, Debug, Copy, Clone, Dupe)]
// enum TestBinOp
private enum class TestBinOp(val display: String) {
    // #[display("and")]
    And("and"),
    // #[display("or")]
    Or("or");

    // fn eval(self, x: bool, y: impl FnOnce() -> bool) -> bool
    fun eval(x: Boolean, y: () -> Boolean): Boolean {
        return when (this) {
            And -> x && y()
            Or -> x || y()
        }
    }

    override fun toString(): String = display
}

// #[derive(Clone, Debug)]
// enum TestExpr
private sealed class TestExpr {
    /// `True` or `False`.
    // Const(bool)
    class Const(val value: Boolean) : TestExpr()

    /// `true()` or `false()`.
    // Count(bool)
    class Count(val value: Boolean) : TestExpr()

    /// Binary operation.
    // BinOp(TestBinOp, Box<(TestExpr, TestExpr)>)
    class BinOp(val op: TestBinOp, val lhs: TestExpr, val rhs: TestExpr) : TestExpr()

    /// `not` operation.
    // Not(Box<TestExpr>)
    class Not(val expr: TestExpr) : TestExpr()

    /// Evaluate the expression the same way Starlark would evaluate it.
    // fn eval(&self, count: &CountCalls) -> bool
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

    // impl Display for TestExpr
    override fun toString(): String {
        return when (this) {
            is Const -> if (value) "True" else "False"
            is Count -> if (value) "true()" else "false()"
            is BinOp -> "($lhs $op $rhs)"
            is Not -> "(not $expr)"
        }
    }
}

/// Evaluate the program.
///
/// * Return the result which is expected to be `bool`.
/// * Count side effects.
// fn eval_program(program: &str) -> (bool, CountCalls)
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

// fn eval_if_else_with_starlark(expr: &TestExpr) -> (bool, CountCalls)
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

// fn eval_if_with_starlark(expr: &TestExpr) -> (bool, CountCalls)
private fun evalIfWithStarlark(expr: TestExpr): Pair<Boolean, CountCalls> {
    val program = """
r = False
if $expr:
    r = True
r
"""
    return evalProgram(program)
}

// fn eval_expr_result(expr: &TestExpr) -> (bool, CountCalls)
private fun evalExprResult(expr: TestExpr): Pair<Boolean, CountCalls> {
    return evalProgram(expr.toString())
}

// fn eval_manually(expr: &TestExpr) -> (bool, CountCalls)
private fun evalManually(expr: TestExpr): Pair<Boolean, CountCalls> {
    val counts = CountCalls()
    val r = expr.eval(counts)
    return Pair(r, counts)
}

// fn test_if_else(expr: &TestExpr)
private fun testIfElse(expr: TestExpr) {
    val expected = evalManually(expr)
    val actual = evalIfElseWithStarlark(expr)
    check(expected.first == actual.first && expected.second.calls == actual.second.calls) {
        "expression: $expr, expected: $expected, actual: $actual"
    }
}

// fn test_if(expr: &TestExpr)
private fun testIf(expr: TestExpr) {
    val expected = evalManually(expr)
    val actual = evalIfWithStarlark(expr)
    check(expected.first == actual.first && expected.second.calls == actual.second.calls) {
        "expression: $expr, expected: $expected, actual: $actual"
    }
}

// fn test_expr_result(expr: &TestExpr)
private fun testExprResult(expr: TestExpr) {
    val expected = evalManually(expr)
    val actual = evalExprResult(expr)
    check(expected.first == actual.first && expected.second.calls == actual.second.calls) {
        "expression: $expr, expected: $expected, actual: $actual"
    }
}

// fn test_ifs(expr: &TestExpr)
private fun testIfs(expr: TestExpr) {
    testIf(expr)
    testIfElse(expr)
    // If condition expression compilation is different from compilation of the expression,
    // so we explicitly test both cases.
    testExprResult(expr)
}

// fn bool_values() -> [bool; 2]
private fun boolValues(): List<Boolean> = listOf(true, false)

// fn basic_bool_exprs() -> impl Iterator<Item = TestExpr>
private fun basicBoolExprs(): List<TestExpr> {
    return boolValues().flatMap { x ->
        listOf(TestExpr.Count(x), TestExpr.Const(x))
    }
}

// #[test]
// fn test_basic()
internal fun testBasic() {
    testIfs(TestExpr.Const(true))
    testIfs(TestExpr.Const(false))
    testIfs(TestExpr.Count(true))
    testIfs(TestExpr.Count(false))
    testIfs(TestExpr.Not(TestExpr.Const(true)))
    testIfs(TestExpr.Not(TestExpr.Const(false)))
    testIfs(TestExpr.Not(TestExpr.Count(true)))
    testIfs(TestExpr.Not(TestExpr.Count(false)))
}

// #[test]
// fn test_and()
internal fun testAnd() {
    for (lhs in basicBoolExprs()) {
        for (rhs in basicBoolExprs()) {
            testIfs(TestExpr.BinOp(TestBinOp.And, lhs, rhs))
        }
    }
}

// #[test]
// fn test_or()
internal fun testOr() {
    for (lhs in basicBoolExprs()) {
        for (rhs in basicBoolExprs()) {
            testIfs(TestExpr.BinOp(TestBinOp.Or, lhs, rhs))
        }
    }
}

// #[test]
// fn test_and_or_not()
internal fun testAndOrNot() {
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

// const RANDOM_ITERATIONS: usize = 100;
private const val RANDOM_ITERATIONS = 100

// fn max_depth_for_iter(i: usize) -> usize
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

// fn random_expr(rng: &mut SmallRng, max_depth: usize) -> TestExpr
private fun randomExpr(rng: Random, maxDepth: Int): TestExpr {
    // fn random_simple_expr(rng: &mut SmallRng) -> TestExpr
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

// #[test]
// fn test_if_random()
internal fun testIfRandom() {
    val rng = Random(17)
    for (i in 0 until RANDOM_ITERATIONS) {
        val maxDepth = maxDepthForIter(i)
        val expr = randomExpr(rng, maxDepth)
        testIf(expr)
    }
}

// #[test]
// fn test_if_else_random()
internal fun testIfElseRandom() {
    val rng = Random(17)
    for (i in 0 until RANDOM_ITERATIONS) {
        val maxDepth = maxDepthForIter(i)
        val expr = randomExpr(rng, maxDepth)
        testIfElse(expr)
    }
}

// #[test]
// fn test_expr_random()
internal fun testExprRandom() {
    val rng = Random(17)
    for (i in 0 until RANDOM_ITERATIONS) {
        val maxDepth = maxDepthForIter(i)
        val expr = randomExpr(rng, maxDepth)
        testExprResult(expr)
    }
}
