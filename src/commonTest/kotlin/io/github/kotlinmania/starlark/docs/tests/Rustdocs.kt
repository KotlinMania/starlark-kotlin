// port-lint: source src/docs/tests/rustdocs.rs
package io.github.kotlinmania.starlark.docs.tests

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
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocParam
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.params.PARAM_FMT_OPTIONAL
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.starlarkvalueastype.StarlarkValueAsType
import kotlin.test.Test

private class InputTypeRepr : StarlarkValue, StarlarkTypeRepr {
    override val TYPE: String get() = "input"
    override fun toString(): String = "input"
    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

private class OutputTypeRepr : StarlarkValue, StarlarkTypeRepr {
    override val TYPE: String get() = "output"
    override fun toString(): String = "output"
    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

private fun globals(builder: GlobalsBuilder) {
    builder.set("Input", StarlarkValueAsType.new(InputTypeRepr()))
    builder.set("Output", StarlarkValueAsType.new(OutputTypeRepr()))

    builder.setFunction("simple") { args: Arguments, eval: Evaluator ->
        error("unimplemented")
    }

    builder.setFunction("default_arg") { args: Arguments, eval: Evaluator ->
        error("unimplemented")
    }

    builder.setFunction("args_kwargs") { args: Arguments, eval: Evaluator ->
        error("unimplemented")
    }

    builder.setFunction("custom_types") { args: Arguments, eval: Evaluator ->
        error("unimplemented")
    }

    builder.setFunction("pos_named") { args: Arguments, eval: Evaluator ->
        error("unimplemented")
    }

    builder.setFunction("with_arguments") { args: Arguments, eval: Evaluator ->
        error("unimplemented")
    }
}

class RustdocsTest {
    /** Test that a Rust starlarkModule produces the right documentation. */
    @Test
    fun testRustdoc() {
        val got = GlobalsBuilder.new().with(::globals).build()
        val a = Assert()
        a.globalsAdd(::globals)
        val expected = a.passModule(
            """
def args_kwargs(*args, **kwargs: typing.Any) -> None: pass
def custom_types(arg1: str, arg2: Input) -> Output: pass
def default_arg(arg1 = "_", arg2: typing.Any = None) -> list[str]: pass
def pos_named(arg1: int, *, arg2: int) -> int: pass
def simple(arg_int: int, arg_bool: bool, arg_vec: list[str], arg_dict: dict[str, (bool, int)]) -> None: pass
def with_arguments(*args, **kwargs) -> int: pass
""",
        )

        val expectedMembers = expected.documentation().members
        val gotMembers = got.documentation().members

        gotMembers.shiftRemove("Input")
        gotMembers.shiftRemove("Output")

        check(expectedMembers.len() == gotMembers.len())
        for ((name, expectedItem) in expectedMembers) {
            var item = expectedItem
            if (name == "default_arg") {
                val memberItem = item as? DocItem.Member ?: error("unreachable")
                val funcItem = (memberItem.member as? DocMember.Function) ?: error("unreachable")
                val firstParam = funcItem.function.params.docParamsMut().next()
                firstParam.defaultValue = PARAM_FMT_OPTIONAL
            }
            check(item == gotMembers.get(name))
        }
    }

    @Test
    fun innerObjectFunctionsHaveDocs() {
        Heap.temp { heap ->
            val obj = heap.allocSimple(Obj())
            val item = obj
                .getAttr("func1", heap)
                .getOrThrow()!!
                .documentation()

            when (item) {
                is DocItem.Member -> {
                    val funcItem = item.member as DocMember.Function
                    check(funcItem.function.docs!!.summary == "Docs for func1")
                }
                else -> error("Expected function: $item")
            }
        }
    }

    @Test
    fun innerModuleFunctionsHaveDocs() {
        val item = GlobalsBuilder.new()
            .with(::moduleFunctions)
            .build()
            .getOwned("func1")!!
            .value()
            .documentation()

        when (item) {
            is DocItem.Member -> {
                val funcItem = item.member as DocMember.Function
                check(funcItem.function.docs!!.summary == "Docs for func1")
            }
            else -> error("Expected function: $item")
        }
    }
}

internal class Obj : StarlarkValue {
    override val TYPE: String get() = "obj"
    override fun toString(): String = "obj"

    override fun getMethods(): Methods? {
        return objMethodsStatic.methods(::objectMethods)
    }

    companion object {
        private val objMethodsStatic = MethodsStatic()
    }
}

/** These are where the module docs go */
private fun objectMethods(builder: MethodsBuilder) {
    /** Docs for func1 */
    builder.setMethod("func1") { eval: Evaluator, _this: Value, _sig: ParametersSpec<FrozenValue>, args: Arguments ->
        Result.success(Value.newNone())
    }
}

private fun moduleFunctions(builder: GlobalsBuilder) {
    builder.setConst("MAGIC", 42)

    /** Docs for func1 */
    builder.setFunction("func1") { args: Arguments, eval: Evaluator ->
        Value.newNone()
    }
}
