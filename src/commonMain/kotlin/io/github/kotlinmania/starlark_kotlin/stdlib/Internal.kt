// port-lint: source src/stdlib/internal.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.internal

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

/// Expose starlark-rust internals in starlark.
///
/// None of this code is meant to be used in production. Can be changed any time.

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

// #[starlark_module]
// fn starlark_rust_internal_members(globals: &mut GlobalsBuilder)
private fun starlarkRustInternalMembers(globals: GlobalsBuilder) {
    // fn ty_of_value_debug(#[starlark(require = pos)] value: Value) -> anyhow::Result<String>
    globals.setFunction("ty_of_value_debug") { args, _ ->
        val value: Value = args.full.pos.firstOrNull() ?: Value.newNone()
        Ty.ofValue(value).toString()
    }
}

// pub(crate) fn register_internal(globals: &mut GlobalsBuilder)
fun registerInternal(globals: GlobalsBuilder) {
    globals.namespaceNoDocs("starlark_rust_internal") { s ->
        starlarkRustInternalMembers(s)
    }
}
