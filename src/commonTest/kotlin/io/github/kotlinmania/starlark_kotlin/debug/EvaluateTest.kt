<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/debug/EvaluateTest.kt
// port-lint: source tests:src/debug/evaluate.rs
package io.github.kotlinmania.starlark.debug
=======
// port-lint: tests src/debug/evaluate.rs
package io.github.kotlinmania.starlark_kotlin.debug
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/debug/EvaluateTest.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/debug/EvaluateTest.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/debug/EvaluateTest.kt
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

<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/debug/EvaluateTest.kt
import kotlin.test.Test

class EvaluateTest {

    @Test
    fun testDebugEvaluate() {
        io.github.kotlinmania.starlark.debug.testDebugEvaluate()
=======
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.isWasm
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import kotlin.test.Test

private fun debuggerFunctions(builder: GlobalsBuilder) {
    builder.setFunction("debug_evaluate") { args, eval ->
        val code = args.positional<String>(0)
        val ast = AstModule.parse("interactive", code, Dialect.AllOptionsInternal).getOrThrow()
        eval.evalStatements(ast).getOrThrow()
    }
}

internal class EvaluateTest {
    @Test
    fun testDebugEvaluate() {
        if (isWasm()) {
            return
        }

        val a = Assert()
        a.disableStaticTypechecking()
        a.globalsAdd(::debuggerFunctions)
        val check = """
assert_eq(debug_evaluate("1+2"), 3)
x = 10
assert_eq(debug_evaluate("x"), 10)
assert_eq(debug_evaluate("x = 5"), None)
assert_eq(x, 5)
y = [20]
debug_evaluate("y.append(30)")
assert_eq(y, [20, 30])
"""
        a.pass(check)
        a.pass(
            "def local():\n" +
                check.lines().joinToString("\n") { "    $it" } +
                "\nlocal()",
        )

        a.pass(
            """
def foo(x, y, z):
    return bar(y)
def bar(x):
    return debug_evaluate("x")
assert_eq(foo(1, 2, 3), 2)
""",
        )

        a.pass(
            """
x = 7
def bar(y):
    return debug_evaluate("x + y")
assert_eq(bar(4), 4 + 7)
""",
        )

        a.module(
            "test",
            """
x = 7
z = 2
def bar(y):
    assert_eq(x, 7)
    debug_evaluate("x = 20")
    assert_eq(x, 7) # doesn't work for frozen variables
    return debug_evaluate("x + y + z")
""",
        )
        a.pass("load('test', 'bar'); assert_eq(bar(4), 4 + 7 + 2)")
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/debug/EvaluateTest.kt
    }
}
