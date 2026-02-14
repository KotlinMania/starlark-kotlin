// port-lint: source src/docs/tests/markdown.rs
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
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.docs.markdown.renderDocItemNoLink
import io.github.kotlinmania.starlark_kotlin.docs.multipage.DocModuleInfo
import io.github.kotlinmania.starlark_kotlin.docs.multipage.renderMarkdownMultipage
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.syntax.golden_test_template.goldenTestTemplate
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.list.UnpackList
import io.github.kotlinmania.starlark_kotlin.values.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.starlark_value_as_type.StarlarkValueAsType
import io.github.kotlinmania.starlark_kotlin.values.tuple.UnpackTuple

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
private class Magic : StarlarkValue {
    override fun starlarkType(): String = "magic"
    override fun toString(): String = "magic"
}

// #[derive(ProvidesStaticType, Debug, Display, Allocative, Serialize)]
// struct Obj;
private class Obj : StarlarkValue {
    override fun starlarkType(): String = "obj"
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
// fn module(builder: &mut GlobalsBuilder)
private fun moduleFunctions(builder: GlobalsBuilder) {
    // const MAGIC: i32 = 42
    builder.setConst("MAGIC", 42)

    // const Obj: StarlarkValueAsType<Obj> = StarlarkValueAsType::new()
    builder.setConst("Obj", StarlarkValueAsType<Obj>())

    /// Docs for func1
    ///
    /// # Arguments
    ///     * `foo`: Docs for foo
    ///
    /// # Returns
    /// The string 'func1'
    // fn func1(foo: String) -> anyhow::Result<String>
    builder.setFunction("func1") { foo: String ->
        val _ignore = foo
        Result.success("func1")
    }

    // fn func2() -> anyhow::Result<String>
    builder.setFunction("func2") {
        Result.success("func2")
    }

    /// A function with only positional arguments.
    // #[starlark(as_type = Magic)]
    // fn Magic(a1: i32, a2: Option<i32>, step: i32) -> anyhow::Result<String>
    builder.setFunction("Magic") { a1: Int, a2: Int?, step: Int? ->
        val _unused = Triple(a1, a2, step ?: 1)
        Result.success("func3")
    }

    // fn with_defaults(...)
    builder.setFunction("with_defaults") { explicitDefault: UnpackList<String>?, hiddenDefault: UnpackList<String>?, stringDefault: String? ->
        val _unused = Triple(explicitDefault ?: UnpackList(), hiddenDefault, stringDefault ?: "my_default")
        Result.success(NoneType)
    }

    // fn pos_either_named(a: i32, b: i32, c: i32) -> anyhow::Result<Magic>
    builder.setFunction("pos_either_named") { a: Int, b: Int, c: Int ->
        val _unused = Triple(a, b, c)
        Result.success(Magic())
    }
}

// #[starlark_module]
// fn submodule(builder: &mut GlobalsBuilder)
private fun submoduleFunctions(builder: GlobalsBuilder) {
    // fn notypes(a: Value) -> anyhow::Result<Value>
    builder.setFunction("notypes") { a: Value ->
        Result.success(a)
    }

    // fn starlark_args(#[starlark(args)] args: UnpackTuple<String>) -> anyhow::Result<NoneType>
    builder.setFunction("starlark_args") { args: UnpackTuple<String> ->
        val _ignore = args
        Result.success(NoneType)
    }

    // fn starlark_kwargs(#[starlark(kwargs)] kwargs: SmallMap<String, u32>) -> anyhow::Result<NoneType>
    builder.setFunction("starlark_kwargs") { kwargs: SmallMap<String, UInt> ->
        val _ignore = kwargs
        Result.success(NoneType)
    }

    // fn new_obj() -> anyhow::Result<Obj>
    builder.setFunction("new_obj") {
        Result.success(Obj())
    }
}

// fn get_globals() -> Globals
private fun getGlobals(): Globals {
    return GlobalsBuilder()
        .with(::moduleFunctions)
        .withNamespace("submod", ::submoduleFunctions)
        .build()
}

/// These are where the module docs go
// #[starlark_module]
// fn object(builder: &mut MethodsBuilder)
private fun objectMethods(builder: MethodsBuilder) {
    /// Docs for attr1
    // #[starlark(attribute)]
    // fn attr1(this: Value) -> starlark::Result<String>
    builder.setAttribute("attr1") { _this: Value ->
        Result.success("attr1")
    }

    // #[starlark(attribute)]
    // fn attr2(this: Value) -> starlark::Result<String>
    builder.setAttribute("attr2") { _this: Value ->
        Result.success("attr2")
    }

    /// Docs for func1
    // fn func1(this: Value, foo: String) -> anyhow::Result<String>
    builder.setMethod("func1") { _this: Value, foo: String ->
        val _ignore = foo
        Result.success("func1")
    }

    // fn func2(this: Value) -> anyhow::Result<String>
    builder.setMethod("func2") { _this: Value ->
        Result.success("func2")
    }

    /// Needs to be escaped when rendered in markdown.
    // fn __exported__(this: Value) -> anyhow::Result<NoneType>
    builder.setMethod("__exported__") { _this: Value ->
        Result.success(NoneType)
    }
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
    val docs = DocType.fromStarlarkValue<Obj>()
    val res = docsGoldenTest("object.golden.md", DocItem.Type(docs))

    check(res.contains("name.__exported__"))
}
