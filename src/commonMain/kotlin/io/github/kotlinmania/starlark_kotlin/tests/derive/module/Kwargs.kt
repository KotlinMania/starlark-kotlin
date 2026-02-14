// port-lint: source src/tests/derive/module/kwargs.rs
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
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder

// #[starlark_module]
// fn test_kwargs_module(globals: &mut GlobalsBuilder)
private fun testKwargsModule(globals: GlobalsBuilder) {
    // fn pos_kwargs(
    //     #[starlark(require = pos)] a: u32,
    //     #[starlark(require = pos)] b: bool,
    //     #[starlark(kwargs)] kwargs: SmallMap<String, u64>,
    // ) -> anyhow::Result<String>
    globals.setFunction("pos_kwargs") { a: UInt, b: Boolean, kwargs: SmallMap<String, ULong> ->
        Result.success("a=$a b=$b kwargs=$kwargs")
    }

    // fn pos_named_kwargs(
    //     #[starlark(require = pos)] a: u32,
    //     #[starlark(require = named)] b: bool,
    //     #[starlark(kwargs)] kwargs: SmallMap<String, u64>,
    // ) -> anyhow::Result<String>
    globals.setFunction("pos_named_kwargs") { a: UInt, b: Boolean, kwargs: SmallMap<String, ULong> ->
        Result.success("a=$a b=$b kwargs=$kwargs")
    }
}

// #[test]
// fn test_kwargs()
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
