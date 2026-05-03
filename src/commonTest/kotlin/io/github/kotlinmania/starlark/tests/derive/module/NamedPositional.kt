// port-lint: source src/tests/derive/module/namedPositional.rs
package io.github.kotlinmania.starlark.tests.derive.module

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.values.types.tuple.UnpackTuple
import kotlin.test.Test

private fun namedPositionalFunctions(globals: GlobalsBuilder) {
    globals.setFunction("positional") { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }

    globals.setFunction("named") { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }

    globals.setFunction("named_only") { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }

    fun namedAfterArgs(starArgs: UnpackTuple<Int>, x: Int): Result<Int> =
        Result.success(x + starArgs.items.sum())

    // Same as above, but with explicit redundant annotation.
    fun namedAfterArgsExplicitlyMarked(args: UnpackTuple<Int>, x: Int): Result<Int> =
        Result.success(x + args.items.sum())

    globals.setFunction("named_after_args") { args, _ ->
        namedAfterArgs(args.positional(0), args.positional(1))
    }

    globals.setFunction("named_after_args_explicitly_marked") { args, _ ->
        namedAfterArgsExplicitlyMarked(args.positional(0), args.positional(1))
    }
}

class NamedPositionalTests {
    @Test
    fun testPositionalOnly() {
        val a = Assert()
        a.globalsAdd(::namedPositionalFunctions)
        a.eq("17", "positional(17)")
        a.fail("noop(positional)(x=19)", "extra named parameter")
    }

    @Test
    fun testNamedCanBeCalledAsBothNamedAndPositional() {
        val a = Assert()
        a.globalsAdd(::namedPositionalFunctions)
        a.eq("23", "named(x=23)")
        a.eq("29", "named(29)")
    }

    @Test
    fun testNamedOnly() {
        val a = Assert()
        a.globalsAdd(::namedPositionalFunctions)
        a.eq("31", "named_only(x=31)")
        a.fail("noop(named_only)(37)", "Missing named-only parameter")
    }

    @Test
    fun testNamedAfterArgs() {
        val a = Assert()
        a.globalsAdd(::namedPositionalFunctions)
        a.eq("13", "named_after_args(1, 2, x=10)")
        a.fail(
            "noop(named_after_args)(1, 2, 3)",
            "Missing named-only parameter",
        )
    }

    @Test
    fun testNamedAfterArgsExplicitlyMarked() {
        val a = Assert()
        a.globalsAdd(::namedPositionalFunctions)
        a.eq("13", "named_after_args_explicitly_marked(1, 2, x=10)")
        a.fail(
            "noop(named_after_args_explicitly_marked)(1, 2, 3)",
            "Missing named-only parameter",
        )
    }
}
