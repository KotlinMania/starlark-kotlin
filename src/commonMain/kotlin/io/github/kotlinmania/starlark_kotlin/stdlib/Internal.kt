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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class InternalValue {
    companion object {
        fun tyOfValue(value: InternalValue): InternalTy = InternalTy()
    }
}
class InternalTy {
    override fun toString(): String = "Ty(..)"
}
class InternalGlobalsBuilder {
    fun set(name: String, value: Any) {}
    fun namespaceNoDocs(name: String, init: (InternalGlobalsBuilder) -> Unit) {}
}

private fun starlarkRustInternalMembers(globals: InternalGlobalsBuilder) {
    globals.set("ty_of_value_debug", object : Any() {
        fun invoke(value: InternalValue): Result<String> {
            return Result.success(InternalValue.tyOfValue(value).toString())
        }
    })
}

fun registerInternal(globals: InternalGlobalsBuilder) {
    globals.namespaceNoDocs("starlark_rust_internal") { s ->
        starlarkRustInternalMembers(s)
    }
}
