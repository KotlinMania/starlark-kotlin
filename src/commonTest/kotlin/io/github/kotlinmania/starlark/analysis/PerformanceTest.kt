// port-lint: source tests:src/analysis/performance.rs
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

import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test
import kotlin.test.assertEquals

class PerformanceTest {

    private fun module(x: String): AstModule {
        return AstModule.parse("bad.bzl", x, Dialect.AllOptionsInternal).getOrThrow()
    }

    @Test
    fun testLintMatchesDictIssue() {
        val res = mutableListOf<LintT<Performance>>()
        checkCallExpr(
            module(
                """
def foo(extra, **kwargs):
    x = dict(**kwargs)
    y = dict(extra)
    return (x,y)
"""
            ),
            res,
        )
        assertEquals(
            listOf(
                "bad.bzl:3:9-23: Dict copy `dict(**kwargs)` is more efficient as `dict(kwargs)`"
            ),
            res.map { it.toString() },
        )
    }

    @Test
    fun testLintMatchesAnyFunction() {
        val res = mutableListOf<LintT<Performance>>()
        checkCallExpr(
            module(
                """
def foo(items):
    a = all(items)
    b = all([item for item in items])
    c = any([item for item in items])
    d = all({"a": a for a in []})
    e = any(list({}))
    f = all(dict([]))
    return (a,b,c,d,e,f)
"""
            ),
            res,
        )
        assertEquals(
            listOf(
                "bad.bzl:4:9-38: `all` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop.",
                "bad.bzl:5:9-38: `any` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop.",
                "bad.bzl:6:9-34: `all` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop.",
                "bad.bzl:7:9-22: `any(list({}))` allocates a new list for the results. Prefer using a for-loop.",
                "bad.bzl:8:9-22: `all(dict([]))` allocates a new dict for the results. Prefer using a for-loop.",
            ),
            res.map { it.toString() },
        )
    }
}
