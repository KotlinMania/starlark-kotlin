// port-lint: source src/docs/tests/rustdocs.rs
package io.github.kotlinmania.starlark_kotlin.docs.tests

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocParam
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.PARAM_FMT_OPTIONAL
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.starlark_value_as_type.StarlarkValueAsType

// struct InputTypeRepr;
private class InputTypeRepr : StarlarkValue, StarlarkTypeRepr {
    override val TYPE: String get() = "input"
    override fun toString(): String = "input"
    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

// struct OutputTypeRepr;
private class OutputTypeRepr : StarlarkValue, StarlarkTypeRepr {
    override val TYPE: String get() = "output"
    override fun toString(): String = "output"
    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

// #[starlark_module]
// fn globals(builder: &mut GlobalsBuilder)
private fun globals(builder: GlobalsBuilder) {
    // const Input: StarlarkValueAsType<InputTypeRepr> = StarlarkValueAsType::new()
    builder.set("Input", StarlarkValueAsType.new<InputTypeRepr>())
    // const Output: StarlarkValueAsType<OutputTypeRepr> = StarlarkValueAsType::new()
    builder.set("Output", StarlarkValueAsType.new<OutputTypeRepr>())

    // fn simple(arg_int: i32, arg_bool: bool, arg_vec: UnpackList<&str>, arg_dict: SmallMap<String, (bool, i32)>) -> anyhow::Result<NoneType>
    builder.setFunction("simple") { _args: Arguments, _eval: Evaluator ->
        error("unimplemented")
    }

    // fn default_arg(arg1: Option<Value>, arg2: Value, eval: &mut Evaluator) -> anyhow::Result<Vec<String>>
    builder.setFunction("default_arg") { _args: Arguments, _eval: Evaluator ->
        error("unimplemented")
    }

    // fn args_kwargs(args: UnpackTuple<Value>, kwargs: Value) -> anyhow::Result<NoneType>
    builder.setFunction("args_kwargs") { _args: Arguments, _eval: Evaluator ->
        error("unimplemented")
    }

    // fn custom_types(arg1: StringValue, arg2: ValueOfUnchecked<InputTypeRepr>, heap: Heap) -> anyhow::Result<ValueOfUnchecked<OutputTypeRepr>>
    builder.setFunction("custom_types") { _args: Arguments, _eval: Evaluator ->
        error("unimplemented")
    }

    // fn pos_named(arg1: i32, #[starlark(require = named)] arg2: i32) -> anyhow::Result<i32>
    builder.setFunction("pos_named") { _args: Arguments, _eval: Evaluator ->
        error("unimplemented")
    }

    // fn with_arguments(args: &Arguments) -> anyhow::Result<i32>
    builder.setFunction("with_arguments") { _args: Arguments, _eval: Evaluator ->
        error("unimplemented")
    }
}

/// Test that a Rust starlark_module produces the right documentation.

// #[test]
// fn test_rustdoc()
internal fun testRustdoc() {
    val got = GlobalsBuilder.new().with(::globals).build()
    val a = Assert()
    a.globalsAdd(::globals)
    val expected = a.passModule("""
def args_kwargs(*args, **kwargs: typing.Any) -> None: pass
def custom_types(arg1: str, arg2: Input) -> Output: pass
def default_arg(arg1 = "_", arg2: typing.Any = None) -> list[str]: pass
def pos_named(arg1: int, *, arg2: int) -> int: pass
def simple(arg_int: int, arg_bool: bool, arg_vec: list[str], arg_dict: dict[str, (bool, int)]) -> None: pass
def with_arguments(*args, **kwargs) -> int: pass
""")

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
            val firstParam = funcItem.function.params.docParamsMut().next()!!
            firstParam.defaultValue = PARAM_FMT_OPTIONAL
        }
        // Comparing one at a time produces more useful error messages
        check(item == gotMembers.get(name))
    }
}

// struct Obj;
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

/// These are where the module docs go
// #[starlark_module]
// fn object(builder: &mut MethodsBuilder)
private fun objectMethods(builder: MethodsBuilder) {
    /// Docs for func1
    // fn func1(this: Value, foo: String) -> anyhow::Result<String>
    builder.setMethod("func1") { _eval: Evaluator, _this: Value, _sig: ParametersSpec<FrozenValue>, _args: Arguments ->
        Result.success(Value.newNone())
    }
}

// #[test]
// fn inner_object_functions_have_docs()
internal fun innerObjectFunctionsHaveDocs() {
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

// #[starlark_module]
// fn module(builder: &mut GlobalsBuilder)
private fun moduleFunctions(builder: GlobalsBuilder) {
    // const MAGIC: i32 = 42
    builder.setConst("MAGIC", 42)

    /// Docs for func1
    // fn func1(foo: String) -> anyhow::Result<String>
    builder.setFunction("func1") { _args: Arguments, _eval: Evaluator ->
        Value.newNone()
    }
}

// #[test]
// fn inner_module_functions_have_docs()
internal fun innerModuleFunctionsHaveDocs() {
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
