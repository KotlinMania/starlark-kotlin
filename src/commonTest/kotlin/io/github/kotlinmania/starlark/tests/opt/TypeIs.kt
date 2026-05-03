// port-lint: source src/tests/opt/typeIs.rs
package io.github.kotlinmania.starlark.tests.opt

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

/** Test for type-is optimizations. */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.compiler.DefGen
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.eval.compiler.InlineDefBody
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.values.layout.Value
import kotlin.test.Test

private fun globals(builder: GlobalsBuilder) {
    fun returnsTypeIs(value: Value): Result<Boolean> {
        val defGen = value.downcastRef<DefGen<*>>()
        return if (defGen != null) {
            Result.success(defGen.defInfo.inlineDefBody is InlineDefBody.ReturnTypeIs)
        } else {
            error("not def")
        }
    }

    builder.setFunction("returns_type_is") { args, _ ->
        val value = args.positional<Value>(0)
        returnsTypeIs(value).getOrThrow()
    }
}

class TypeIsTests {
    @Test
    fun returnsTypeIs() {
        val a = Assert()
        a.globalsAdd(::globals)

        a.module(
            "types.star",
            """
    def is_list(x):
      return type(x) == type([])
    """,
        )

        a.pass(
            """
    load('types.star', 'is_list')
    assert_true(returns_type_is(is_list))
    assert_true(is_list([]))
    assert_false(is_list({}))
    """,
        )
    }

    @Test
    fun doesNotReturnTypeIs() {
        val a = Assert()
        a.globalsAdd(::globals)
        a.pass(
            """
    def is_not_list(x):
      return type(x) != type([])

    def something_else(x, y):
      return type(x) == type([])

    assert_false(returns_type_is(is_not_list))
    assert_false(returns_type_is(something_else))
    """,
        )
    }
}
