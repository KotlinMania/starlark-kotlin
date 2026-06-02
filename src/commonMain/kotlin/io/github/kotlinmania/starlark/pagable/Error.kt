// port-lint: source src/pagable/error.rs
package io.github.kotlinmania.starlark.pagable

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

import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.ErrorKind

/** Errors that can occur during pagable serialization/deserialization. */
sealed class PagableError(
    message: String,
) : Exception(message) {
    /** The type was not registered in the vtable registry. */
    class TypeNotRegistered(
        val typeId: String,
    ) : PagableError("Type `$typeId` was not registered for deserialization.")
}

// impl From<PagableError> for crate::Error
fun PagableError.toError(): Error = Error.newKind(ErrorKind.Other(this))
