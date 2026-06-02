// port-lint: tests src/docs/tests/rustdocs.rs
package io.github.kotlinmania.starlark.docs.tests

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.deriverefs.NativeCallableComponents
import io.github.kotlinmania.starlark.deriverefs.NativeCallableParam
import io.github.kotlinmania.starlark.deriverefs.NativeCallableParamDefaultValue
import io.github.kotlinmania.starlark.deriverefs.NativeCallableParamSpec
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
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
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.starlarkvalueastype.StarlarkValueAsType

private class InputTypeRepr :
    StarlarkValue,
    StarlarkTypeRepr {
    override val TYPE: String get() = "input"

    override fun toString(): String = "input"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

private class OutputTypeRepr :
    StarlarkValue,
    StarlarkTypeRepr {
    override val TYPE: String get() = "output"

    override fun toString(): String = "output"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

private fun globals(builder: GlobalsBuilder) {
    builder.set("Input", StarlarkValueAsType.new(InputTypeRepr()))
    builder.set("Output", StarlarkValueAsType.new(OutputTypeRepr()))
    val inputTy = Ty.starlarkValue(TyStarlarkValue.new("input"))
    val outputTy = Ty.starlarkValue(TyStarlarkValue.new("output"))
    val boolIntTupleTy = Ty.tuple(listOf(Ty.bool(), Ty.int()))
    val noOp = { _: Evaluator, _: ParametersSpec<FrozenValue>, _: Arguments -> Result.success(Value.newNone()) }

    fun param(
        name: String,
        ty: Ty,
        required: NativeCallableParamDefaultValue? = null,
    ): NativeCallableParam = NativeCallableParam(name, ty, required)

    fun register(
        name: String,
        paramSpec: NativeCallableParamSpec,
        runtimeSpec: ParametersSpec<FrozenValue>,
        returnType: Ty,
    ) {
        builder.setFunction(
            name = name,
            components =
                NativeCallableComponents(
                    speculativeExecSafe = false,
                    rustDocstring = null,
                    paramSpec = paramSpec,
                    returnType = returnType,
                ),
            sig = runtimeSpec,
            asType = null,
            ty = null,
            specialBuiltinFunction = null,
            f = noOp,
        )
    }

    fun spec(
        name: String,
        configure: io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpecBuilder<FrozenValue>.() -> Unit,
    ): ParametersSpec<FrozenValue> =
        ParametersSpec
            .withCapacity<FrozenValue>(name)
            .apply(configure)
            .finish()

    register(
        name = "simple",
        paramSpec =
            NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed =
                    listOf(
                        param("arg_int", Ty.int()),
                        param("arg_bool", Ty.bool()),
                        param("arg_vec", Ty.list(Ty.string())),
                        param("arg_dict", Ty.dict(Ty.string(), boolIntTupleTy)),
                    ),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
        runtimeSpec =
            spec("simple") {
                noMorePositionalOnlyArgs()
                required("arg_int")
                required("arg_bool")
                required("arg_vec")
                required("arg_dict")
            },
        returnType = Ty.none(),
    )

    register(
        name = "default_arg",
        paramSpec =
            NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed =
                    listOf(
                        param("arg1", Ty.any(), NativeCallableParamDefaultValue.Optional),
                        param("arg2", Ty.any(), NativeCallableParamDefaultValue.Value(FrozenValue.newNone())),
                    ),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
        runtimeSpec =
            spec("default_arg") {
                noMorePositionalOnlyArgs()
                optional("arg1")
                defaulted("arg2", FrozenValue.newNone())
            },
        returnType = Ty.list(Ty.string()),
    )

    register(
        name = "args_kwargs",
        paramSpec =
            NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = NativeCallableParam.args("args", Ty.any()),
                namedOnly = emptyList(),
                kwargs = NativeCallableParam.kwargs("kwargs", Ty.any()),
            ),
        runtimeSpec =
            spec("args_kwargs") {
                args()
                kwargs()
            },
        returnType = Ty.none(),
    )

    register(
        name = "custom_types",
        paramSpec =
            NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed =
                    listOf(
                        param("arg1", Ty.string()),
                        param("arg2", inputTy),
                    ),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
        runtimeSpec =
            spec("custom_types") {
                noMorePositionalOnlyArgs()
                required("arg1")
                required("arg2")
            },
        returnType = outputTy,
    )

    register(
        name = "pos_named",
        paramSpec =
            NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = listOf(param("arg1", Ty.int())),
                args = null,
                namedOnly = listOf(param("arg2", Ty.int())),
                kwargs = null,
            ),
        runtimeSpec =
            spec("pos_named") {
                noMorePositionalOnlyArgs()
                required("arg1")
                noMorePositionalArgs()
                required("arg2")
            },
        returnType = Ty.int(),
    )

    register(
        name = "with_arguments",
        paramSpec = NativeCallableParamSpec.forArguments(),
        runtimeSpec =
            spec("with_arguments") {
                args()
                kwargs()
            },
        returnType = Ty.int(),
    )
}

/** Test that a Rust starlark_module produces the right documentation. */

internal fun testRustdoc() {
    val got = GlobalsBuilder.new().with(::globals).build()
    val a = Assert()
    a.globalsAdd(::globals)
    val expected =
        a.passModule(
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
            // `Option<Foo>` args in native functions are special magic and have behavior that can't
            // be replicated with normal functions
            val memberItem = item as? DocItem.Member ?: error("unreachable")
            val funcItem = (memberItem.member as? DocMember.Function) ?: error("unreachable")
            val firstParam =
                funcItem.function.params
                    .docParamsMut()
                    .next()!!
            firstParam.defaultValue = PARAM_FMT_OPTIONAL
        }
        // Comparing one at a time produces more useful error messages
        check(item == gotMembers.get(name))
    }
}

internal class Obj : StarlarkValue {
    override val TYPE: String get() = "obj"

    override fun toString(): String = "obj"

    override fun getMethods(): Methods? = objMethodsStatic.methods(::objectMethods)

    companion object {
        private val objMethodsStatic = MethodsStatic()
    }
}

/** These are where the module docs go */
private fun objectMethods(builder: MethodsBuilder) {
    /** Docs for func1 */
    builder.setMethod("func1") { _eval: Evaluator, _this: Value, _sig: ParametersSpec<FrozenValue>, _args: Arguments ->
        Result.success(Value.newNone())
    }
}

internal fun innerObjectFunctionsHaveDocs() {
    Heap.temp { heap ->
        val obj = heap.allocSimple(Obj())
        val item =
            obj
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

private fun moduleFunctions(builder: GlobalsBuilder) {
    builder.setConst("MAGIC", 42)

    /** Docs for func1 */
    // fn func1(foo: String) -> anyhow::Result<String>
    builder.setFunction("func1") { _args: Arguments, _eval: Evaluator ->
        Value.newNone()
    }
}

internal fun innerModuleFunctionsHaveDocs() {
    val item =
        GlobalsBuilder
            .new()
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
