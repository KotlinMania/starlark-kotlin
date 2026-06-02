// port-lint: source src/stdlib/internal.rs
package io.github.kotlinmania.starlark.stdlib.internal

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

/**
 * Expose starlark-rust internals in starlark.
 *
 * None of this code is meant to be used in production. Can be changed any time.
 */

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Value
import kotlin.toString

// #[starlark_module]
private fun starlarkRustInternalMembers(globals: io.github.kotlinmania.starlark.environment.GlobalsBuilder) {
    globals.setFunction("ty_of_value_debug") { args, eval ->
        val value: io.github.kotlinmania.starlark.values.layout.Value =
            args.full.pos.firstOrNull() ?: io.github.kotlinmania.starlark.values.layout.Value.Companion
                .newNone()
        eval.heap().allocStr(
            io.github.kotlinmania.starlark.typing.Ty.Companion
                .ofValue(value)
                .toString(),
        )
    }
}

fun registerInternal(globals: io.github.kotlinmania.starlark.environment.GlobalsBuilder) {
    globals.namespaceNoDocs("starlark_rust_internal") { s ->
        starlarkRustInternalMembers(s)
    }
}
