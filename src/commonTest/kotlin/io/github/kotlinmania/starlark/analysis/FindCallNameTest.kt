// port-lint: tests src/analysis/find_call_name.rs
package io.github.kotlinmania.starlark.analysis

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

import io.github.kotlinmania.starlark.codemap.ResolvedPos
import io.github.kotlinmania.starlark.codemap.ResolvedSpan
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FindCallNameTest {
    @Test
    fun findsFunctionCallsWithNameKwarg() {
        val contents = """
foo(name = "foo_name")
bar("bar_name")
baz(name = "baz_name")

utils.foo(name = "dot_name")

def x(name = "foo_name"):
    pass
"""

        val module =
            AstModule
                .parse(
                    "foo.star",
                    contents,
                    Dialect.AllOptionsInternal,
                ).getOrThrow()

        assertEquals(
            ResolvedSpan(
                begin = ResolvedPos(line = 1, column = 0),
                end = ResolvedPos(line = 1, column = 3),
            ),
            module.findFunctionCallWithName("foo_name")?.let { span ->
                module.codemap().resolveSpan(span)
            },
        )
        assertNull(module.findFunctionCallWithName("bar_name"))
        assertEquals(
            ResolvedSpan(
                begin = ResolvedPos(line = 5, column = 0),
                end = ResolvedPos(line = 5, column = 9),
            ),
            module.findFunctionCallWithName("dot_name")?.let { span ->
                module.codemap().resolveSpan(span)
            },
        )
    }
}
