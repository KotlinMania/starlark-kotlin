// port-lint: source src/typing/tests.rs
package io.github.kotlinmania.starlark.typing

/*
 * Copyright 2019 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark.environment.FrozenModule
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.eval.runtime.fileloader.ReturnOwnedFileLoader
import io.github.kotlinmania.starlark.typing.ParamIsRequired
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.typing.Interface
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.tests.trimRustBacktrace
import io.github.kotlinmania.starlark.eval.evalModule
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.values.typing.callable.StarlarkCallableParamSpec
import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import kotlin.test.Test

// Submodules:

internal class TypeCheck(
    private val expectTypes: MutableList<String> = mutableListOf(),
    private val loads: MutableMap<String, Pair<Interface, FrozenModule>> = mutableMapOf(),
) {
    fun ty(name: String): TypeCheck {
        expectTypes.add(name)
        return this
    }

    fun load(file: String, interface_: Interface, module: FrozenModule): TypeCheck {
        loads[file] = Pair(interface_, module)
        return this
    }

    private fun mkFileLoader(): ReturnOwnedFileLoader {
        val modules = loads.map { (name, pair) -> Pair(name, pair.second) }.toMap()
        return ReturnOwnedFileLoader(modules)
    }

    fun check(testName: String, code: String): Pair<Interface, FrozenModule> {
        val globals = GlobalsBuilder.extended()
            .with(::registerTypecheckGlobals)
            .build()
        val ast = AstModule.parse("filename", code, Dialect.AllOptionsInternal).getOrThrow()
        val (errors, typemap, interface_, approximations) = ast.typecheck(
            globals,
            loads.map { (name, pair) -> Pair(name, pair.first) }.toMap(),
        )

        val output = StringBuilder()
        output.appendLine("Code:")
        output.appendLine(code.trim())
        if (errors.isEmpty()) {
            output.appendLine()
            output.appendLine("No errors.")
        } else {
            for (error in errors) {
                output.appendLine()
                output.appendLine("Error:")
                // Note we are using `:#` here instead of `:?` because
                // `:?` includes rust backtrace.
                // The issue: https://github.com/dtolnay/anyhow/issues/300
                output.appendLine(error.toString().trimEnd())
            }
        }

        if (approximations.isNotEmpty()) {
            output.appendLine()
            output.appendLine("Approximations:")
            for (approx in approximations) {
                output.appendLine(approx.toString())
            }
        }

        if (expectTypes.isNotEmpty()) {
            output.appendLine()
            output.appendLine("Types:")
            for (k in expectTypes) {
                val types = typemap.findBindingsByName(k)
                when {
                    types.size == 1 -> output.appendLine("$k: ${types[0]}")
                    types.isEmpty() -> error("Type not found for $k")
                    else -> error("Multiple types found for $k")
                }
            }
        }

        val loader = mkFileLoader()
        val module = run {
            output.appendLine()
            output.appendLine("Compiler typechecker (eval):")
            Module.withTempHeap { module ->
                val eval = Evaluator(module)

                eval.setLoader(loader)

                eval.enableStaticTypechecking(true)
                val evalResult = eval.evalModule(ast, globals)
                if (evalResult.isSuccess != errors.isEmpty()) {
                    output.appendLine("Compiler typechecker and eval results mismatch.")
                    output.appendLine()
                }

                // Additional writes must happen above this line otherwise it might be erased by trimRustBacktrace
                evalResult.fold(
                    onSuccess = { output.appendLine("No errors.") },
                    onFailure = { err -> output.appendLine(err.toString()) },
                )

                module.freeze()
            }.getOrThrow()
        }

        goldenTestTemplate(
            "src/typing/tests/golden/$testName.golden",
            trimRustBacktrace(output.toString()),
        )

        return Pair(interface_, module)
    }
}

private object NamedXy : StarlarkCallableParamSpec {
    override fun params(): ParamSpec {
        return ParamSpec.newParts(
            namedOnly = listOf(
                Triple("x", ParamIsRequired.Yes, Ty.string()),
                Triple("y", ParamIsRequired.Yes, Ty.int()),
            )
        )
    }
}

private fun registerTypecheckGlobals(globals: GlobalsBuilder) {
    globals.setFunction("accepts_iterable") { args: Arguments, eval: Evaluator ->
        NoneType
    }

    globals.setFunction("accepts_typed_kwargs") { args: Arguments, eval: Evaluator ->
        NoneType
    }

    globals.setFunction("accepts_callable_named_xy") { args: Arguments, eval: Evaluator ->
        NoneType
    }
}

class TypingTests {
    @Test
    fun testSuccess() {
        TypeCheck().ty("y").check(
            "success",
            """
    def foo(x: str) -> str:
        return x.removeprefix("test")

    def bar():
        y = hash(foo("magic"))
    """,
        )
    }

    @Test
    fun testFailure() {
        TypeCheck().check(
            "failure",
            """
    def test():
        hash(1)
    """,
        )
    }

    @Test
    fun testLoad() {
        val (interface_, module) = TypeCheck().check(
            "load_0",
            """
    def foo(x: list[bool]) -> str:
        return "test"
       """,
        )
        TypeCheck()
            .load("foo.bzl", interface_, module)
            .ty("res")
            .check(
                "load_1",
                """
    load("foo.bzl", "foo")
    def test():
        res = [foo([])]
    """,
            )
    }

    /** Test things that have previous claimed incorrectly they were type errors */
    @Test
    fun testFalseNegative() {
        TypeCheck().check(
            "false_negative",
            """
    def test():
        fail("Expected variable expansion in string: `{}`".format("x"))
    """,
        )
    }

    @Test
    fun testDotType() {
        TypeCheck().check(
            "dot_type_0",
            """
    def foo(x: list) -> bool:
        return type(x) == type(list)

    def bar():
        foo([1,2,3])
    """,
        )
        TypeCheck().check(
            "dot_type_1",
            """
    def foo(x: list) -> bool:
        return type(x) == []

    def bar():
        foo(True)
    """,
        )
    }

    @Test
    fun testAcceptsIterable() {
        TypeCheck().check(
            "accepts_iterable",
            """
    def test():
        accepts_iterable([1, ()])
    """,
        )

        val a = Assert()
        a.globalsAdd(::registerTypecheckGlobals)
        a.pass("accepts_iterable([1, ()])")
    }

    @Test
    fun testDictBug() {
        // NOTE(nga): figure out how to fix it.
        //   Type of `y` should be inferred to `str`.
        TypeCheck().ty("y").check(
            "dict_bug",
            """
    def test():
        x = {}
        x.setdefault(33, "x")
        y = x[44]
    """,
        )
    }

    @Test
    fun testDictLookupByNever() {
        TypeCheck().check(
            "dict_never_key",
            """
    # We use `typing.Never` when expression is an error,
    # or it is a type parameter of empty list for example.
    # Dict lookup by never should not be an error.
    def test(d: dict[typing.Any, str], x: typing.Never):
        y = d[x]
    """,
        )
    }

    @Test
    fun testNewListDictSyntax() {
        TypeCheck().ty("x").check(
            "new_list_dict_syntax",
            """
    def new_list_dict_syntax(d: dict[str, int]) -> list[str]:
        return list(d.keys())

    def test():
        # Check type is properly parsed from the function return type.
        x = new_list_dict_syntax({"a": 1, "b": 2})
    """,
        )
    }

    @Test
    fun testNewListDictSyntaxAsValue() {
        // NOTE(nga): fix.
        TypeCheck().ty("x").ty("y").check(
            "new_list_dict_syntax_as_value",
            """
    def test():
        x = list[str]
        y = dict[int, bool]
    """,
        )
    }

    @Test
    fun testIntPlusFloat() {
        TypeCheck().ty("x").check(
            "int_plus_float",
            """
    def test():
        x = 1 + 1.0
    """,
        )
    }

    @Test
    fun testIntBitorFloat() {
        TypeCheck().ty("x").check(
            "int_bitor_float",
            """
    def test():
        x = 0x60000000000000000000000 | 1.0
    """,
        )
    }

    @Test
    fun testUnOp() {
        TypeCheck().ty("x").ty("y").ty("z").check(
            "un_op",
            """
    def test():
        # Good.
        x = -1
        # Bad.
        y = ~True
        # Union good and bad.
        z = -(1 if True else "")
    """,
        )
    }

    @Test
    fun testUnion() {
        TypeCheck().check(
            "union",
            """
    def func_which_returns_union(p) -> str | int:
        if p == 56:
            return "a"
        elif p == 57:
            return 1
        else:
            return []
    """,
        )
    }

    @Test
    fun testMethodsWorkForTyStarlarkValue() {
        TypeCheck().ty("x").check(
            "methods_work_for_ty_starlark_value",
            """
    def test(s: str):
        x = s.startswith("a")
    """,
        )
    }

    @Test
    fun testBitOrReturnInt() {
        TypeCheck().check(
            "bit_or_return_int",
            """
    test = int | 3

    def foo() -> test:
        pass
    """,
        )
    }

    @Test
    fun testBitOrReturnList() {
        TypeCheck().check(
            "bit_or_return_list",
            """
    test = int | list[3]

    def foo() -> test:
        pass
    """,
        )
    }

    @Test
    fun testBitOrWithLoad() {
        val (interface_, module) = TypeCheck().check(
            "test_bit_or_with_load_foo",
            """
    def foo() -> str:
        return "test"
    """,
        )
        TypeCheck().load("foo.bzl", interface_, module).check(
            "test_bit_or_with_load",
            """
    load("foo.bzl", "foo")
    test = int | foo()
    def test() -> test:
        pass
    """,
        )
    }
}
