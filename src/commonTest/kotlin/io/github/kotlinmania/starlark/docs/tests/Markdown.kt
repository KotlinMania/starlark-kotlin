// port-lint: tests src/docs/tests/markdown.rs
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
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocFrozenValue
import io.github.kotlinmania.starlark.values.types.starlarkvalueastype.StarlarkValueAsType

private fun docsGoldenTest(testFileName: String, doc: DocItem): String {
    check(testFileName.endsWith(".golden.md"))
    check(!testFileName.contains('/'))

    val output = renderDocItemNoLink("name", doc)

    goldenTestTemplate("src/docs/tests/golden/$testFileName", output)

    return output
}

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

private class Magic :
    StarlarkValue,
    StarlarkTypeRepr {
    override val TYPE: String get() = "magic"

    override fun toString(): String = "magic"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))
}

private class MarkdownObj :
    StarlarkValue,
    StarlarkTypeRepr {
    override val TYPE: String get() = "obj"

    override fun toString(): String = "obj"

    override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

    override fun getMethods(): Methods? = objMethodsStatic.methods(::objectMethods)

    companion object {
        private val objMethodsStatic = MethodsStatic()
    }
}

/** These are where the module docs go */
private fun moduleFunctions(builder: GlobalsBuilder) {
    builder.setInner("MAGIC", 42.allocFrozenValue(builder.frozenHeap()), false)

    builder.set("Obj", StarlarkValueAsType.new(MarkdownObj()))

    /**
     * Docs for func1
     *
     * # Arguments
     *     * `foo`: Docs for foo
     *
     * # Returns
     * The string 'func1'
     */
    builder.setFunction("func1") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    builder.setFunction("func2") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    /** A function with only positional arguments. */
    builder.setFunction("Magic") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    builder.setFunction("with_defaults") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    builder.setFunction("pos_either_named") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }
}

private fun submoduleFunctions(builder: GlobalsBuilder) {
    builder.setFunction("notypes") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    builder.setFunction("starlarkargs") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    builder.setFunction("starlark_kwargs") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }

    builder.setFunction("new_obj") { args: Arguments, eval: Evaluator ->
        Result.success(Value.newNone())
    }
}

private fun getGlobals(): Globals =
    GlobalsBuilder
        .new()
        .with(::moduleFunctions)
        .withNamespace("submod", ::submoduleFunctions)
        .build()

/** These are where the module docs go */
private fun objectMethods(builder: MethodsBuilder) {
    /** Docs for attr1 */
    builder.setAttribute("attr1", docstring = "Docs for attr1") { thisvalue: Value, heap: Heap ->
        Result.success(Value.newNone())
    }

    builder.setAttribute("attr2") { thisvalue: Value, heap: Heap ->
        Result.success(Value.newNone())
    }

    /** Docs for func1 */
    builder.setMethod("func1") { eval: Evaluator, thisvalue: Value, sig, args ->
        Result.success(Value.newNone())
    }

    builder.setMethod("func2") { eval: Evaluator, thisvalue: Value, sig, args ->
        Result.success(Value.newNone())
    }

    /** Needs to be escaped when rendered in markdown. */
    builder.setMethod("__exported__") { eval: Evaluator, thisvalue: Value, sig, args ->
        Result.success(Value.newNone())
    }
}

private fun testGlobalsDocsRender(withLinkedType: Boolean, renderSignatureAtBottom: Boolean) {
    val global = getGlobals().documentation()
    val modulesInfo =
        DocModuleInfo(
            module = global,
            name = "globals",
            pagePath = "",
        )
    val linkedTyMapper: ((String, String) -> String)? =
        if (withLinkedType) {
            { path, typeName -> "<a to=\"/path/to/$path\">$typeName</a>" }
        } else {
            null
        }
    val res =
        renderMarkdownMultipage(
            listOf(modulesInfo),
            linkedTyMapper,
            renderSignatureAtBottom,
        )
    val subfolderName =
        when {
            withLinkedType && renderSignatureAtBottom -> "multipage_linked_type_and_rendersignature_at_bottom"
            withLinkedType && !renderSignatureAtBottom -> "multipage_linked_type"
            !withLinkedType && renderSignatureAtBottom -> "multipage_rendersignature_at_bottom"
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

internal fun goldenDocsStarlark() {
    val res =
        docsGoldenTest(
            "starlark.golden.md",
            DocItem.Module(Assert.passModule(STARLARK_CODE).documentation()),
        )
    check(!res.contains("_do_not_export"))
}

internal fun nativeDocsModule() {
    val res =
        docsGoldenTest(
            "native.golden.md",
            DocItem.Module(getGlobals().documentation()),
        )
    check(!res.contains("starlark::assert::all_true"))
    check(res.contains("string_default: str = \"my_default\""))
}

internal fun globalsRenderDefault() {
    testGlobalsDocsRender(false, false)
}

internal fun globalsRenderDefaultWithLinkedType() {
    testGlobalsDocsRender(true, false)
}

internal fun globalsRenderSignatureAtBottom() {
    testGlobalsDocsRender(false, true)
}

internal fun globalsRenderSignatureAtBottomWithLinkedType() {
    testGlobalsDocsRender(true, true)
}

internal fun goldenDocsObject() {
    val docs = DocType.fromStarlarkValue(MarkdownObj())
    val res = docsGoldenTest("object.golden.md", DocItem.Type(docs))

    check(res.contains("name.__exported__"))
}
