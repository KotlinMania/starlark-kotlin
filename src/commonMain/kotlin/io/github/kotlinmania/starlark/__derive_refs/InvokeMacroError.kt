// port-lint: source src/__derive_refs/invoke_macro_error.rs
package io.github.kotlinmania.starlark.__derive_refs

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

import io.github.kotlinmania.starlark_kotlin.Error as StarlarkError

// pub trait InvokeMacroError {
//     fn into_starlark_error(self) -> crate::Error;
// }
/**
 * Trait used to convert error returned from native function into [StarlarkError].
 *
 * In Kotlin, this is an interface that throwables can implement to provide
 * custom conversion to a Starlark error.
 */
interface InvokeMacroError {
    fun intoStarlarkError(): StarlarkError
}

// impl InvokeMacroError for anyhow::Error
/**
 * Default conversion: wraps any [Throwable] as a native [StarlarkError].
 *
 * Starlark native functions should not return generic exceptions;
 * this exists as a fallback for external integrations.
 */
fun Throwable.intoStarlarkError(): io.github.kotlinmania.starlark_kotlin.Error {
    return io.github.kotlinmania.starlark_kotlin.Error.newNative(this)
}
