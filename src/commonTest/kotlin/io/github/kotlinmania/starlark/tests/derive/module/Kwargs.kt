// port-lint: tests tests/derive/module/kwargs.rs
package io.github.kotlinmania.starlark.tests.derive.module

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
import io.github.kotlinmania.starlark.environment.GlobalsBuilder

// #[starlark_module]
private fun testKwargsModule(globals: GlobalsBuilder) {
    //     #[starlark(require = pos)] a: u32,
    //     #[starlark(require = pos)] b: bool,
    //     #[starlark(kwargs)] kwargs: SmallMap<String, u64>,
    // ) -> anyhow::Result<String>
    globals.setFunction("pos_kwargs") { args, _ ->
        val a = args.positional<UInt>(0)
        val b = args.positional<Boolean>(1)
        // Remaining named args are kwargs
        val kwargsEntries = args.namesMap().getOrThrow()
        val kwargsStr =
            kwargsEntries.iter().joinToString(", ") { (k, v) ->
                "\"${k.asStr()}\": ${v.unpackI32()}"
            }
        Result.success("a=$a b=$b kwargs={$kwargsStr}")
    }

    //     #[starlark(require = pos)] a: u32,
    //     #[starlark(require = named)] b: bool,
    //     #[starlark(kwargs)] kwargs: SmallMap<String, u64>,
    // ) -> anyhow::Result<String>
    globals.setFunction("pos_named_kwargs") { args, _ ->
        val a = args.positional<UInt>(0)
        val b = args.optionalNamed<Boolean>("b") ?: error("b is required")
        // Remaining named args (excluding "b") are kwargs
        val kwargsEntries = args.namesMap().getOrThrow()
        val kwargsStr =
            kwargsEntries
                .iter()
                .filter { (k, _) -> k.asStr() != "b" }
                .joinToString(", ") { (k, v) ->
                    "\"${k.asStr()}\": ${v.unpackI32()}"
                }
        Result.success("a=$a b=$b kwargs={$kwargsStr}")
    }
}

// #[test]
internal fun testKwargs() {
    val a = Assert()
    a.globalsAdd(::testKwargsModule)
    a.eq(
        "'a=1 b=true kwargs={\"x\": 3}'",
        "pos_kwargs(1, True, x=3)",
    )
    a.eq(
        "'a=1 b=true kwargs={\"x\": 3}'",
        "pos_named_kwargs(1, b=True, x=3)",
    )
}
