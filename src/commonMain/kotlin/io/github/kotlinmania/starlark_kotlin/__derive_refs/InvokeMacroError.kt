// port-lint: source src/__derive_refs/invoke_macro_error.rs
package io.github.kotlinmania.starlark_kotlin.__derive_refs

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

/// Trait used to convert error returned from native function into `starlark::Error`.
// pub trait InvokeMacroError
interface InvokeMacroError {
    // fn into_starlark_error(self) -> crate::Error
    fun intoStarlarkError(): StarlarkError
}

/// This implementation should not be used by starlark itself:
/// starlark native functions should not return `anyhow::Error`,
/// and should not convert to `ErrorKind::Native`.
// impl InvokeMacroError for anyhow::Error
fun Exception.intoStarlarkError(): StarlarkError {
    return StarlarkError.newNative(this)
}

// impl InvokeMacroError for crate::Error
fun StarlarkError.intoStarlarkError(): StarlarkError {
    return this
}
