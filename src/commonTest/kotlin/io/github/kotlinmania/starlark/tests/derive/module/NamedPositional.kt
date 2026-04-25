// port-lint: source src/tests/derive/module/named_positional.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.module

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
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.runtime.positional
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.UnpackTuple

// #[starlark_module]
// fn named_positional_functions(globals: &mut GlobalsBuilder)
private fun namedPositionalFunctions(globals: GlobalsBuilder) {
    // fn positional(#[starlark(require = pos)] x: i32) -> anyhow::Result<i32>
    globals.setFunction("positional") { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }

    // fn named(x: i32) -> anyhow::Result<i32>
    globals.setFunction("named") { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }

    // fn named_only(#[starlark(require = named)] x: i32) -> anyhow::Result<i32>
    globals.setFunction("named_only") { args, _ ->
        val x = args.positional<Int>(0)
        Result.success(x)
    }

    // fn named_after_args(
    //     #[starlark(args)] star_args: UnpackTuple<i32>,
    //     x: i32,
    // ) -> anyhow::Result<i32>
    globals.setFunction("named_after_args") { args, _ ->
        val starArgs = args.positional<UnpackTuple<Int>>(0)
        val x = args.positional<Int>(1)
        Result.success(x + starArgs.items.sum())
    }

    // Same as above, but with explicit redundant annotation.
    // fn named_after_args_explicitly_marked(
    //     #[starlark(args)] args: UnpackTuple<i32>,
    //     #[starlark(require = named)] x: i32,
    // ) -> anyhow::Result<i32>
    globals.setFunction("named_after_args_explicitly_marked") { args, _ ->
        val starArgs = args.positional<UnpackTuple<Int>>(0)
        val x = args.positional<Int>(1)
        Result.success(x + starArgs.items.sum())
    }
}

// #[test]
// fn test_positional_only()
internal fun testPositionalOnly() {
    val a = Assert()
    a.globalsAdd(::namedPositionalFunctions)
    a.eq("17", "positional(17)")
    a.fail("noop(positional)(x=19)", "extra named parameter")
}

// #[test]
// fn test_named_can_be_called_as_both_named_and_positional()
internal fun testNamedCanBeCalledAsBothNamedAndPositional() {
    val a = Assert()
    a.globalsAdd(::namedPositionalFunctions)
    a.eq("23", "named(x=23)")
    a.eq("29", "named(29)")
}

// #[test]
// fn test_named_only()
internal fun testNamedOnly() {
    val a = Assert()
    a.globalsAdd(::namedPositionalFunctions)
    a.eq("31", "named_only(x=31)")
    a.fail("noop(named_only)(37)", "Missing named-only parameter")
}

// #[test]
// fn test_named_after_args()
internal fun testNamedAfterArgs() {
    val a = Assert()
    a.globalsAdd(::namedPositionalFunctions)
    a.eq("13", "named_after_args(1, 2, x=10)")
    a.fail(
        "noop(named_after_args)(1, 2, 3)",
        "Missing named-only parameter",
    )
}

// #[test]
// fn test_named_after_args_explicitly_marked()
internal fun testNamedAfterArgsExplicitlyMarked() {
    val a = Assert()
    a.globalsAdd(::namedPositionalFunctions)
    a.eq("13", "named_after_args_explicitly_marked(1, 2, x=10)")
    a.fail(
        "noop(named_after_args_explicitly_marked)(1, 2, 3)",
        "Missing named-only parameter",
    )
}
