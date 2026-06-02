// port-lint: source src/eval/compiler/scope/scope_resolver_globals.rs
package io.github.kotlinmania.starlark.eval.compiler.scope

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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.constFrozenString

internal class ScopeResolverGlobals(
    /** None if unknown. */
    val globals: FrozenRef<Globals>?,
) {
    companion object {
        fun unknown(): ScopeResolverGlobals = ScopeResolverGlobals(globals = null)
    }

    fun getGlobal(name: String): FrozenValue? =
        when (val g = globals) {
            null -> constFrozenString("unknown-global").toFrozenValue()
            else -> g.value.getFrozen(name)
        }

    fun names(): List<String>? =
        globals
            ?.value
            ?.names()
            ?.asSequence()
            ?.map { s -> s.asStr() }
            ?.toList()
}
