// port-lint: tests src/stdlib/partial.rs (tests)
package io.github.kotlinmania.starlark.stdlib

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
import kotlin.test.Test

// #[cfg(test)]
// mod tests
class PartialTest {
    // fn eq(expected: &str, expr: &str)
    private fun eq(expected: String, expr: String) {
        val sum = """
def sum(a, b, *args, **kwargs):
    # print("a=%s b=%s args=%s kwargs=%s" % (a, b, args, kwargs))
    # TODO(nga): fix typecheck.
    args = noop((a, b)) + args
    return [args, kwargs]
"""
        val a = Assert()
        a.disableStaticTypechecking()
        a.eq(expected, "$sum$expr")
    }

    // #[test]
    // fn test_simple()
    @Test
    fun testSimple() {
        eq(
            "[(1, 2, 3), {\"other\": True, \"third\": None}]",
            "(partial(sum, 1, other=True))(2, 3, third=None)",
        )
    }

    // #[test]
    // fn test_star_to_partial()
    @Test
    fun testStarToPartial() {
        eq(
            "[(1, 2, 3), {\"other\": True, \"third\": None}]",
            "(partial(sum, *[1], **{\"other\": True}))(2, 3, third=None)",
        )
    }

    // #[test]
    // fn test_start_to_returned_func()
    @Test
    fun testStartToReturnedFunc() {
        eq(
            "[(1, 2, 3), {\"other\": True, \"third\": None}]",
            "(partial(sum, other=True))(*[1, 2, 3], **{\"third\": None})",
        )
    }

    // #[test]
    // fn test_no_args_to_partial()
    @Test
    fun testNoArgsToPartial() {
        eq(
            "[(1, 2, 3), {\"other\": True, \"third\": None}]",
            "(partial(sum))(1, 2, 3, third=None, **{\"other\": True})",
        )
    }

    // #[test]
    // fn test_typecheck_bug()
    @Test
    fun testTypecheckBug() {
        Assert.pass(
            """
def accept_callable(f: typing.Callable): pass

def test():
    accept_callable(partial(list, []))
""",
        )
    }
}
