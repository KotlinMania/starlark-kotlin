// port-lint: source __derive_refs/invokeMacroError.rs
package io.github.kotlinmania.starlark.deriverefs

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

import io.github.kotlinmania.starlark.Error as StarlarkError

/** Trait used to convert error returned from native function into [StarlarkError]. */
interface InvokeMacroError {
    fun intoStarlarkError(): StarlarkError
}

/**
 * Default conversion: wraps any [Throwable] as a native [StarlarkError].
 *
 * Starlark native functions should not return generic exceptions;
 * this exists as a fallback for external integrations.
 */
fun Throwable.intoStarlarkError(): io.github.kotlinmania.starlark.Error {
    return io.github.kotlinmania.starlark.Error.newNative(this)
}
