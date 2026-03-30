// port-lint: source src/values/freeze_error.rs
package io.github.kotlinmania.starlark_kotlin.values.freeze_error

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

//! Error type for `Freeze` implementations. Freeze errors should only contain error messages
//! and error contexts as strings and no metadatas.
//! Conversion from anyhow is disallowed by design in order to enforce the above.

/// Alias for std::result::Result<T, FreezeError>
// pub type FreezeResult<T> = std::result::Result<T, FreezeError>;
typealias FreezeResult<T> = Result<T>

/// freeze_error type, only carries the original error message and potentially an array of contexts
// #[derive(Debug)]
// pub struct FreezeError {
//     pub err_msg: String,
//     pub contexts: Vec<String>,
// }
class FreezeError(
    /// The base error message
    val errMsg: String,
    /// The error contexts that are added to the error message
    val contexts: MutableList<String> = mutableListOf(),
) : Exception(errMsg) {

    // impl From<FreezeError> for anyhow::Error
    // #[cold]
    // fn from(e: FreezeError) -> Self
    fun toException(): Exception {
        val sb = StringBuilder(errMsg)
        // for context in e.contexts.into_iter().rev()
        for (context in contexts.reversed()) {
            sb.append("\n  context: $context")
        }
        return Exception(sb.toString(), this)
    }

    // impl From<FreezeError> for starlark_syntax::Error
    // #[cold]
    // fn from(e: FreezeError) -> Self
    //     starlark_syntax::Error::new_kind(starlark_syntax::ErrorKind::Freeze(e.into()))
    // Kotlin: FreezeError already extends Exception.

    // impl FreezeError

    /// Create a new freeze_error type
    // pub fn new(err_msg: String) -> Self
    companion object {
        fun new(errMsg: String): FreezeError {
            return FreezeError(errMsg)
        }
    }

    /// Add error contexts to freeze_error
    // pub fn context(mut self, context: &str) -> Self
    fun context(context: String): FreezeError {
        contexts.add(context)
        return this
    }

    override fun toString(): String {
        val sb = StringBuilder(errMsg)
        for (ctx in contexts.reversed()) {
            sb.append("\n  context: $ctx")
        }
        return sb.toString()
    }
}

/// Provides the `context` method for `FreezeResult`.
///
/// This is designed to only be called on `FreezeResult` types due to the nature of freeze_error.
/// This is to prevent callers from accidentally expecting context to carry metadata.
// pub trait FreezeErrorContext<T>: Sealed
interface FreezeErrorContext<T> : Sealed {
    /// Add a string error context to an existing `FreezeResult` type.
    // #[track_caller]
    // fn freeze_error_context(self, context: &str) -> FreezeResult<T>;
    fun freezeErrorContext(context: String): FreezeResult<T>
}

/// Protects against downstream implementations
// pub trait Sealed: Sized {}
sealed interface Sealed

// impl<T> Sealed for std::result::Result<T, FreezeError> {}

// impl<T> FreezeErrorContext<T> for std::result::Result<T, FreezeError>
// fn freeze_error_context(self, c: &str) -> FreezeResult<T>
fun <T> Result<T>.freezeErrorContext(context: String): FreezeResult<T> {
    return when {
        // Ok(x) => Ok(x),
        isSuccess -> this
        // Err(e) => Err(e.context(c)),
        else -> {
            val e = exceptionOrNull()
            if (e is FreezeError) {
                Result.failure(e.context(context))
            } else {
                this
            }
        }
    }
}
