// port-lint: source src/typing/error.rs
package io.github.kotlinmania.starlark_kotlin.typing

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

/** Internal error, bug in the typechecker. */
class InternalError private constructor(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    companion object {
        fun msg(message: String): InternalError = InternalError(message)

        fun fromDiagnostic(message: String): InternalError = InternalError(message)

        fun fromEvalException(e: Exception): InternalError =
            InternalError(e.message ?: "Internal error", e)

        fun fromError(e: Exception): InternalError =
            InternalError(e.message ?: "Internal error", e)
    }

    fun intoError(): Exception = this

    fun intoEvalException(): Exception = this
}

/** Errors used in typechecker API. Error has a span. */
class TypingError private constructor(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    companion object {
        // TODO(nga): some errors we create, we ignore later. For example, when typechecking a union,
        //   if either variant is good, we ignore the other variant errors.
        //   So we pay for expensive error creation we ignore. Make this function cheap.
        fun msg(message: String): TypingError = TypingError(message)

        fun new(error: Exception): TypingError =
            TypingError(error.message ?: "Typing error", error)

        fun newAnyhow(error: Exception): TypingError =
            TypingError(error.message ?: "Typing error", error)

        fun fromEvalException(e: Exception): TypingError =
            TypingError(e.message ?: "Typing error", e)
    }

    fun intoError(): Exception = this

    fun intoEvalException(): Exception = this
}

/** Like [TypingError], but without a message or span. */
class TypingNoContextError : Exception("Typing error (no context)")

/**
 * Either a typing error or an internal error.
 * * Typing error means, types are not compatible.
 * * Internal error means, bug in the typechecker.
 */
sealed class TypingOrInternalError {
    data class Typing(val error: TypingError) : TypingOrInternalError()
    data class Internal(val error: InternalError) : TypingOrInternalError()

    companion object {
        fun fromTyping(error: TypingError): TypingOrInternalError = Typing(error)
        fun fromInternal(error: InternalError): TypingOrInternalError = Internal(error)
    }
}

sealed class TypingNoContextOrInternalError {
    data object Typing : TypingNoContextOrInternalError()
    data class Internal(val error: InternalError) : TypingNoContextOrInternalError()

    companion object {
        fun fromNoContext(@Suppress("UNUSED_PARAMETER") error: TypingNoContextError): TypingNoContextOrInternalError = Typing
        fun fromInternal(error: InternalError): TypingNoContextOrInternalError = Internal(error)
    }
}
