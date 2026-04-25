// port-lint: source src/docs/tests/markdown.rs
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

import io.github.kotlinmania.starlark.deriverefs.NativeCallableComponents
import io.github.kotlinmania.starlark.deriverefs.NativeCallableParam
import io.github.kotlinmania.starlark.deriverefs.NativeCallableParamDefaultValue
import io.github.kotlinmania.starlark.deriverefs.NativeCallableParamSpec
import io.github.kotlinmania.starlark.deriverefs.NativeSigArg
import io.github.kotlinmania.starlark.deriverefs.parameterSpec
import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocModuleInfo
import io.github.kotlinmania.starlark.docs.DocType
import io.github.kotlinmania.starlark.docs.markdown.renderDocItemNoLink
import io.github.kotlinmania.starlark.docs.renderMarkdownMultipage
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.NativeFuncFn
import io.github.kotlinmania.starlark.values.types.NativeMethFn
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.values.types.list.UnpackList
import io.github.kotlinmania.starlark.values.types.starlarkvalueastype.StarlarkValueAsType
import io.github.kotlinmania.starlark.values.types.tuple.UnpackTuple
import starlarkmap.smallmap.SmallMap

// fn docs_golden_test(test_file_name: &str, doc: DocItem) -> String
private fun docsGoldenTest(testFileName: String, doc: DocItem): String {
    check(testFileName.endsWith(".golden.md"))
    check(!testFileName.contains('/'))

    val output = renderDocItemNoLink("name", doc)

    goldenTestTemplate("src/docs/tests/golden/$testFileName", output)

    return output
}

// const STARLARK_CODE: &str = r#"..."#
private val STARLARK_CODE = """
${"\"\"\""}
This is the summary of the module's docs

Some extra details can go here,
    and indentation is kept as expected
${"\"\"\""}

def f1(a, b: str, c: int = 5, *, d: str = "some string", **kwargs) -> list[str]:
    ${"\"\"\""}
    Summary line goes here

    Args:
        a: The docs for a
        b: The docs for b
        c: The docs for c, but these
           go onto two lines
        **kwargs: Docs for the keyword args

    Returns:
        A string repr of the args
    ${"\"\"\""}
    return [str((a, b, c, d, repr(kwargs)))]

def f2(a, *args: list[str]):
    ${"\"\"\""}
    This is a function with *args, and no return type

    Args:
        *args: Only doc this arg
    ${"\"\"\""}
    return None

def f3(a: str) -> str:
    return a

def f4(a: str) -> str:
    ${"\"\"\""} This is a docstring with no 'Args:' section ${"\"\"\""}
    return a

# Not public, so shouldn't show up
def _do_not_export():
    pass
"""

// #[derive(Debug, Display, ProvidesStaticType, Allocative, NoSerialize)]
// struct Magic;
private class Magic : StarlarkValue, StarlarkTypeRepr {
    override val TYPE: String get() = "magic"
    override fun toString(): String = "magic"
    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

// #[derive(ProvidesStaticType, Debug, Display, Allocative, Serialize)]
// struct Obj;
private class Obj : StarlarkValue, StarlarkTypeRepr {
    override val TYPE: String get() = "obj"
    override fun toString(): String = "obj"
    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    override fun getMethods(): Methods? {
        return objMethodsStatic.methods(::objectMethods)
    }

    companion object {
        private val objMethodsStatic = MethodsStatic()
    }
}

/** These are where the module docs go */
// #[starlark_module]
// fn module(builder: &mut GlobalsBuilder)
private fun moduleFunctions(builder: GlobalsBuilder) {
    // const MAGIC: i32 = 42
    builder.setConst("MAGIC", 42)

    // const Obj: StarlarkValueAsType<Obj> = StarlarkValueAsType::new()
    builder.set("Obj", StarlarkValueAsType.new(Obj()))

    // fn func1(foo: String) -> anyhow::Result<String>
    /**
     * Docs for func1
     *
     * # Arguments
     *     * `foo`: Docs for foo
     *
     * # Returns
     * The string 'func1'
     */
    fun func1(foo: String): Result<String> {
        val _ignore = foo
        return Result.success("func1")
    }

    builder.setFunction(
        name = "func1",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = """
Docs for func1

# Arguments
    * `foo`: Docs for foo

# Returns
The string 'func1'
""".trimIndent(),
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = listOf(NativeCallableParam("foo", Ty.string(), null)),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.string(),
        ),
        sig = parameterSpec(
            name = "func1",
            posOnly = emptyList(),
            posOrNamed = listOf(NativeSigArg.Required("foo")),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(eval.heap().allocStr("func1").toValue())
        },
    )

    // fn func2() -> anyhow::Result<String>
    fun func2(): Result<String> {
        return Result.success("func2")
    }

    builder.setFunction(
        name = "func2",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.string(),
        ),
        sig = parameterSpec(
            name = "func2",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(eval.heap().allocStr("func2").toValue())
        },
    )

    // #[starlark(as_type = Magic)]
    // fn Magic(a1: i32, a2: Option<i32>, step: i32) -> anyhow::Result<String>
    fun Magic(a1: Int, a2: Int?, step: Int): Result<String> {
        val _unused = Triple(a1, a2, step)
        return Result.success("func3")
    }

    // fn with_defaults(...)
    fun withDefaults(
        explicitDefault: UnpackList<String>,
        hiddenDefault: UnpackList<String>?,
        stringDefault: String,
    ): Result<NoneType> {
        val _unused = Triple(explicitDefault, hiddenDefault, stringDefault)
        return Result.success(NoneType)
    }

    // fn pos_either_named(a: i32, b: i32, c: i32) -> anyhow::Result<Magic>
    fun posEitherNamed(a: Int, b: Int, c: Int): Result<Magic> {
        val _unused = Triple(a, b, c)
        return Result.success(Magic())
    }

    builder.setFunction(
        name = "Magic",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = """
A function with only positional arguments.

And a slightly longer description. With some example code:

```python
Magic(1)
```

And some assertions:

```rust
# starlark::assert::all_true(r#"
1 == 1
# "#);
```
""".trimIndent(),
            paramSpec = NativeCallableParamSpec(
                posOnly = listOf(
                    NativeCallableParam("a1", Ty.int(), null),
                    NativeCallableParam("a2", Ty.union2(Ty.int(), Ty.none()), null),
                    NativeCallableParam(
                        "step",
                        Ty.int(),
                        NativeCallableParamDefaultValue.Value(FrozenValue.testingNewInt(1)),
                    ),
                ),
                posOrNamed = emptyList(),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.string(),
        ),
        sig = parameterSpec(
            name = "Magic",
            posOnly = listOf(
                NativeSigArg.Required("a1"),
                NativeSigArg.Required("a2"),
                NativeSigArg.Defaulted("step", FrozenValue.testingNewInt(1)),
            ),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = Pair(
            Ty.starlarkValue(TyStarlarkValue.new("magic")),
            DocType.fromStarlarkValue(Magic()),
        ),
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(eval.heap().allocStr("func3").toValue())
        },
    )

    val myDefault = builder.frozenHeap().allocStrIntern("my_default").toFrozenValue()

    builder.setFunction(
        name = "with_defaults",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = listOf(
                    NativeCallableParam(
                        "explicit_default",
                        Ty.list(Ty.string()),
                        NativeCallableParamDefaultValue.Value(FrozenValue.newEmptyList()),
                    ),
                    NativeCallableParam(
                        "hidden_default",
                        Ty.union2(Ty.list(Ty.string()), Ty.none()),
                        NativeCallableParamDefaultValue.Optional,
                    ),
                    NativeCallableParam(
                        "string_default",
                        Ty.string(),
                        NativeCallableParamDefaultValue.Value(myDefault),
                    ),
                ),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.none(),
        ),
        sig = parameterSpec(
            name = "with_defaults",
            posOnly = emptyList(),
            posOrNamed = listOf(
                NativeSigArg.Defaulted("explicit_default", FrozenValue.newEmptyList()),
                NativeSigArg.Optional("hidden_default"),
                NativeSigArg.Defaulted("string_default", myDefault),
            ),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(Value.newNone())
        },
    )

    builder.setFunction(
        name = "pos_either_named",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = listOf(NativeCallableParam("a", Ty.int(), null)),
                posOrNamed = listOf(NativeCallableParam("b", Ty.int(), null)),
                args = null,
                namedOnly = listOf(NativeCallableParam("c", Ty.int(), null)),
                kwargs = null,
            ),
            returnType = Ty.starlarkValue(TyStarlarkValue.new("magic")),
        ),
        sig = parameterSpec(
            name = "pos_either_named",
            posOnly = listOf(NativeSigArg.Required("a")),
            posOrNamed = listOf(NativeSigArg.Required("b")),
            args = false,
            namedOnly = listOf(NativeSigArg.Required("c")),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(eval.heap().allocSimple(Magic()))
        },
    )
}

// #[starlark_module]
// fn submodule(builder: &mut GlobalsBuilder)
private fun submoduleFunctions(builder: GlobalsBuilder) {
    // fn notypes(a: Value) -> anyhow::Result<Value>
    fun notypes(a: Value): Result<Value> {
        return Result.success(a)
    }

    // fn starlark_args(#[starlark(args)] args: UnpackTuple<String>) -> anyhow::Result<NoneType>
    fun starlarkArgs(args: UnpackTuple<String>): Result<NoneType> {
        val _ignore = args
        return Result.success(NoneType)
    }

    // fn starlark_kwargs(#[starlark(kwargs)] kwargs: SmallMap<String, u32>) -> anyhow::Result<NoneType>
    fun starlarkKwargs(kwargs: SmallMap<String, UInt>): Result<NoneType> {
        val _ignore = kwargs
        return Result.success(NoneType)
    }

    // fn new_obj() -> anyhow::Result<Obj>
    fun newObj(): Result<Obj> {
        return Result.success(Obj())
    }

    builder.setFunction(
        name = "notypes",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = listOf(NativeCallableParam("a", Ty.any(), null)),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.any(),
        ),
        sig = parameterSpec(
            name = "notypes",
            posOnly = emptyList(),
            posOrNamed = listOf(NativeSigArg.Required("a")),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(Value.newNone())
        },
    )

    builder.setFunction(
        name = "starlark_args",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = NativeCallableParam.args("args", Ty.tupleOf(Ty.string())),
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.none(),
        ),
        sig = parameterSpec(
            name = "starlark_args",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = true,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(Value.newNone())
        },
    )

    builder.setFunction(
        name = "starlark_kwargs",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = null,
                namedOnly = emptyList(),
                kwargs = NativeCallableParam.kwargs("kwargs", Ty.dict(Ty.string(), Ty.int())),
            ),
            returnType = Ty.none(),
        ),
        sig = parameterSpec(
            name = "starlark_kwargs",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = true,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(Value.newNone())
        },
    )

    builder.setFunction(
        name = "new_obj",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.starlarkValue(TyStarlarkValue.new("obj")),
        ),
        sig = parameterSpec(
            name = "new_obj",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        asType = null,
        ty = null,
        specialBuiltinFunction = null,
        f = NativeFuncFn { eval: Evaluator, _sig, args: Arguments ->
            Result.success(eval.heap().allocSimple(Obj()))
        },
    )
}

// fn get_globals() -> Globals
private fun getGlobals(): Globals {
    return GlobalsBuilder.new()
        .with(::moduleFunctions)
        .withNamespace("submod", ::submoduleFunctions)
        .build()
}

/** These are where the module docs go */
// #[starlark_module]
// fn object(builder: &mut MethodsBuilder)
private fun objectMethods(builder: MethodsBuilder) {
    /** Docs for attr1 */
    // #[starlark(attribute)]
    // fn attr1(this: Value) -> starlark::Result<String>
    fun attr1(thisValue: Value): Result<String> {
        val _unused = thisValue
        return Result.success("attr1")
    }

    // #[starlark(attribute)]
    // fn attr2(this: Value) -> starlark::Result<String>
    fun attr2(thisValue: Value): Result<String> {
        val _unused = thisValue
        return Result.success("attr2")
    }

    builder.setAttributeFn(
        name = "attr1",
        speculativeExecSafe = false,
        docstring = "Docs for attr1",
        typ = Ty.string(),
        f = { _frozen: FrozenValue?, thisValue: Value, heap: Heap ->
            val res = attr1(thisValue)
            Result.success(heap.allocStr(res.getOrThrow()).toValue())
        },
    )

    builder.setAttributeFn(
        name = "attr2",
        speculativeExecSafe = false,
        docstring = null,
        typ = Ty.string(),
        f = { _frozen: FrozenValue?, thisValue: Value, heap: Heap ->
            val res = attr2(thisValue)
            Result.success(heap.allocStr(res.getOrThrow()).toValue())
        },
    )

    /** Docs for func1 */
    // fn func1(this: Value, foo: String) -> anyhow::Result<String>
    fun func1(thisValue: Value, foo: String): Result<String> {
        val _ignore = Pair(thisValue, foo)
        return Result.success("func1")
    }

    // fn func2(this: Value) -> anyhow::Result<String>
    fun func2(thisValue: Value): Result<String> {
        val _unused = thisValue
        return Result.success("func2")
    }

    /** Needs to be escaped when rendered in markdown. */
    // fn __exported__(this: Value) -> anyhow::Result<NoneType>
    fun __exported__(thisValue: Value): Result<NoneType> {
        val _unused = thisValue
        return Result.success(NoneType)
    }
    builder.setMethod(
        name = "func1",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = """
Docs for func1

# Arguments
    * `foo`: Docs for foo

# Returns
The string 'func1'
""".trimIndent(),
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = listOf(NativeCallableParam("foo", Ty.string(), null)),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.string(),
        ),
        sig = parameterSpec(
            name = "func1",
            posOnly = emptyList(),
            posOrNamed = listOf(NativeSigArg.Required("foo")),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        f = NativeMethFn { eval: Evaluator, _thisValue: Value, _sig, args: Arguments ->
            Result.success(eval.heap().allocStr("func1").toValue())
        },
    )

    builder.setMethod(
        name = "func2",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = null,
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.string(),
        ),
        sig = parameterSpec(
            name = "func2",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        f = NativeMethFn { eval: Evaluator, _thisValue: Value, _sig, args: Arguments ->
            Result.success(eval.heap().allocStr("func2").toValue())
        },
    )

    builder.setMethod(
        name = "__exported__",
        components = NativeCallableComponents(
            speculativeExecSafe = false,
            rustDocstring = "Needs to be escaped when rendered in markdown.",
            paramSpec = NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = null,
                namedOnly = emptyList(),
                kwargs = null,
            ),
            returnType = Ty.none(),
        ),
        sig = parameterSpec(
            name = "__exported__",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = false,
            namedOnly = emptyList(),
            kwargs = false,
        ),
        f = NativeMethFn { eval: Evaluator, _thisValue: Value, _sig, args: Arguments ->
            Result.success(Value.newNone())
        },
    )
}

// fn test_globals_docs_render(with_linked_type: bool, render_signature_at_bottom: bool)
private fun testGlobalsDocsRender(withLinkedType: Boolean, renderSignatureAtBottom: Boolean) {
    val global = getGlobals().documentation()
    val modulesInfo = DocModuleInfo(
        module = global,
        name = "globals",
        pagePath = "",
    )
    val linkedTyMapper: ((String, String) -> String)? = if (withLinkedType) {
        { path, typeName -> "<a to=\"/path/to/$path\">$typeName</a>" }
    } else {
        null
    }
    val res = renderMarkdownMultipage(
        listOf(modulesInfo),
        linkedTyMapper,
        renderSignatureAtBottom,
    )
    val subfolderName = when {
        withLinkedType && renderSignatureAtBottom -> "multipage_linked_type_and_render_signature_at_bottom"
        withLinkedType && !renderSignatureAtBottom -> "multipage_linked_type"
        !withLinkedType && renderSignatureAtBottom -> "multipage_render_signature_at_bottom"
        else -> "multipage"
    }
    val expectedKeys = listOf("", "Magic", "Obj", "submod")
    check(res.keys.sorted() == expectedKeys)
    for ((k, v) in res) {
        val key = if (k.isEmpty()) "globals" else k
        goldenTestTemplate(
            "src/docs/tests/golden/$subfolderName/$key.golden.md",
            v,
        )
    }
}

// #[test]
// fn golden_docs_starlark()
internal fun goldenDocsStarlark() {
    val res = docsGoldenTest(
        "starlark.golden.md",
        DocItem.Module(Assert.passModule(STARLARK_CODE).documentation()),
    )
    check(!res.contains("_do_not_export"))
}

// #[test]
// fn native_docs_module()
internal fun nativeDocsModule() {
    val res = docsGoldenTest(
        "native.golden.md",
        DocItem.Module(getGlobals().documentation()),
    )
    check(!res.contains("starlark::assert::all_true"))
    check(res.contains("string_default: str = \"my_default\""))
}

// #[test]
// fn globals_render_default()
internal fun globalsRenderDefault() {
    testGlobalsDocsRender(false, false)
}

// #[test]
// fn globals_render_default_with_linked_type()
internal fun globalsRenderDefaultWithLinkedType() {
    testGlobalsDocsRender(true, false)
}

// #[test]
// fn globals_render_signature_at_bottom()
internal fun globalsRenderSignatureAtBottom() {
    testGlobalsDocsRender(false, true)
}

// #[test]
// fn globals_render_signature_at_bottom_with_linked_type()
internal fun globalsRenderSignatureAtBottomWithLinkedType() {
    testGlobalsDocsRender(true, true)
}

// #[test]
// fn golden_docs_object()
internal fun goldenDocsObject() {
    val docs = DocType.fromStarlarkValue(Obj())
    val res = docsGoldenTest("object.golden.md", DocItem.Type(docs))

    check(res.contains("name.__exported__"))
}
