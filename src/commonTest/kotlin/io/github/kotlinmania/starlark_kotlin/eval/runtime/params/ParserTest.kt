// port-lint: tests src/eval/runtime/params/parser.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.params

/*
 * Copyright 2019 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark_kotlin.docs.DocParam
import io.github.kotlinmania.starlark_kotlin.docs.DocParams
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.docs.fromDocstring
import io.github.kotlinmania.starlark_kotlin.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import kotlin.test.Test

internal class ParserTest {
    @Test
    fun testDocumentation() {
        val p = ParametersSpec.newParts<FrozenValue>(
            functionName = "f",
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            args = true,
            namedOnly = listOf(
                Pair("a", ParametersSpecParam.Optional),
                Pair("b", ParametersSpecParam.Optional),
            ),
            kwargs = false,
        )

        val expected = DocParams(
            args = DocParam(
                name = "args",
                docs = null,
                typ = Ty.any(),
                defaultValue = null,
            ),
            namedOnly = listOf(
                DocParam(
                    name = "a",
                    docs = null,
                    typ = Ty.int(),
                    defaultValue = PARAM_FMT_OPTIONAL,
                ),
                DocParam(
                    name = "b",
                    docs = DocString.fromDocstring(DocStringKind.Rust, "param b docs"),
                    typ = Ty.any(),
                    defaultValue = PARAM_FMT_OPTIONAL,
                ),
            ),
            posOnly = emptyList(),
            posOrNamed = emptyList(),
            kwargs = null,
        )
        val types = listOf(Ty.any(), Ty.int(), Ty.any())
        val docs = mutableMapOf<String, DocString?>(
            "a" to null,
            "b" to DocString.fromDocstring(DocStringKind.Rust, "param b docs"),
        )

        val params = p.documentation(types, docs)
        check(expected == params)
    }

    @Test
    fun testParametersStr() {
        fun test(sig: String) {
            val a = Assert()
            val f = a
                .passModule("def f($sig): pass")
                .get("f").getOrThrow()
            check(sig == f.value().parametersSpec()!!.parametersStr())
        }

        test("")
        test("a, b, c, d, e, f, g, h, *args, **kwargs")
        test("*, a")
        test("x, *, a")
        test("*args, a")
        test("x, *args, a")
        test("**kwargs")
        test("a, **kwargs")
    }

    @Test
    fun testCanFillWithArgs() {
        fun test(sig: String, pos: Int, names: List<String>, expected: Boolean) {
            val a = Assert()
            val module = a.passModule("def f($sig): pass")
            val f = module.get("f").getOrThrow().downcast<FrozenDef>().getOrThrow()
            val parametersSpec = f.asRef().parameters
            check(expected == parametersSpec.canFillWithArgs(pos, names))
        }

        test("", 0, emptyList(), true)
        test("", 1, emptyList(), false)
        test("", 0, listOf("a"), false)

        test("a", 1, emptyList(), true)
        test("a", 0, listOf("a"), true)
        test("a", 1, listOf("a"), false)
        test("a", 0, listOf("x"), false)

        test("a, b = 1", 1, emptyList(), true)
        test("a, b = 1", 2, emptyList(), true)
        test("a, b = 1", 0, listOf("a"), true)
        test("a, b = 1", 0, listOf("b"), false)
        test("a, b = 1", 0, listOf("a", "b"), true)

        test("*, a", 0, emptyList(), false)
        test("*, a", 1, emptyList(), false)
        test("*, a", 0, listOf("a"), true)

        test("a, *args", 0, emptyList(), false)
        test("a, *args", 1, emptyList(), true)
        test("a, *args", 10, emptyList(), true)

        test("*args, b", 0, emptyList(), false)
        test("*args, b", 1, emptyList(), false)
        test("*args, b", 0, listOf("b"), true)

        test("**kwargs", 0, emptyList(), true)
        test("**kwargs", 0, listOf("a"), true)
        test("**kwargs", 1, emptyList(), false)
    }
}
