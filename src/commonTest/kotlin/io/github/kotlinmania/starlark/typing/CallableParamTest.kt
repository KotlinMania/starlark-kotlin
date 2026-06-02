// port-lint: tests src/typing/callable_param.rs
package io.github.kotlinmania.starlark.typing

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
 */

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.goldentesttemplate.goldenTestTemplate
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import kotlin.test.Test

class CallableParamTest {
    @Test
    fun testParamSpecDisplay() {
        val functions = """
def simple(x, y, z): pass
def default_value(x, y=1, z=2): pass
def param_type(x: int, y: str, z: int, w: list): pass
def named_only_a(x, *, y): pass
def named_only_b(*, y): pass
def pos_only_a(x, /, y): pass
def pos_only_b(x, /, *, y): pass
def pos_only_c(x, /, *args): pass
def pos_only_d(x, /, *args, **kwargs): pass
"""
        val out = StringBuilder()
        var first = true

        for (rawTest in functions.lines()) {
            val test = rawTest.trim()
            if (test.isEmpty()) continue

            val ast =
                AstModule
                    .parse(
                        "test_param_spec_display.star",
                        test,
                        Dialect.AllOptionsInternal,
                    ).getOrThrow()
            val (errors, typemap, _, approximations) =
                ast.typecheck(Globals.standard(), HashMap())
            errors.firstOrNull()?.let { error("Error: $it") }
            check(approximations.isEmpty())
            val def = typemap.findFirstBinding()!!

            if (first) {
                first = false
            } else {
                out.appendLine()
            }
            out
                .append(test)
                .append('\n')
                .append(def)
                .append('\n')
        }

        goldenTestTemplate(
            "src/typing/callable_param_test_param_spec_display.golden",
            out.toString(),
        )
    }
}
