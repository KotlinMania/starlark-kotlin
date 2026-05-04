// port-lint: source typing/error.rs
package io.github.kotlinmania.starlark.typing

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlarksyntax.diagnostic.WithDiagnostic
import io.github.kotlinmania.starlarksyntax.evalexception.EvalException

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

// CLEANUP NEEDED: StarlarkError is a stubby duplicate of upstream `starlark_syntax/src/error.rs`.
// The proper port (`class Error`) lives in starlark-syntax-kotlin's `error/Error.kt`.
// Callers should be migrated to that, then this stub deleted.
open class StarlarkError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    fun intoInternalError(): StarlarkError {
        return StarlarkError("Internal: $message", cause)
    }
}

/** Internal error, bug in the typechecker. */
class InternalError(private val exception: EvalException) : Exception(exception.message, exception) {
    companion object {
        fun msg(message: Any, span: Span, codemap: CodeMap): InternalError {
            return InternalError(
                EvalException.new(
                    StarlarkError(message.toString()),
                    span,
                    codemap
                )
            )
        }

        fun fromDiagnostic(d: WithDiagnostic<Any>): InternalError {
            val internal = d.map { m ->
                StarlarkError(m.toString())
            }
            return InternalError(
                EvalException.new(internal.value, internal.span, internal.codemap)
            )
        }

        fun fromEvalException(e: EvalException): InternalError {
            return InternalError(e.intoInternalError())
        }

        fun fromError(e: StarlarkError, span: Span, codemap: CodeMap): InternalError {
            val internalErr = e.intoInternalError()
            return InternalError(EvalException.new(internalErr, span, codemap))
        }
    }

    fun intoError(): StarlarkError {
        return exception.intoError()
    }

    fun intoEvalException(): EvalException {
        return exception
    }
}

/** Errors used in typechecker API. Error has a span. */
class TypingError(private val exception: EvalException) {
    companion object {
        fun msg(message: Any, span: Span, codemap: CodeMap): TypingError {
            return TypingError(
                EvalException.newAnyhow(
                    Exception(message.toString()),
                    span,
                    codemap
                )
            )
        }

        fun new(error: StarlarkError, span: Span, codemap: CodeMap): TypingError {
            return TypingError(EvalException.new(error, span, codemap))
        }

        fun newAnyhow(error: Throwable, span: Span, codemap: CodeMap): TypingError {
            return TypingError(EvalException.newAnyhow(error, span, codemap))
        }

        fun fromEvalException(e: EvalException): TypingError {
            return TypingError(e)
        }
    }

    fun intoError(): StarlarkError {
        return exception.intoError()
    }

    fun intoEvalException(): EvalException {
        return exception
    }
}

/** Like [`TypingError`], but without a message or span. */
object TypingNoContextError : Exception("typing error (no context)")

/**
 * Either a typing error or an internal error.
 * * Typing error means, types are not compatible.
 * * Internal error means, bug in the typechecker.
 */
sealed class TypingOrInternalError : Exception() {
    class Typing(val error: TypingError) : TypingOrInternalError()
    class Internal(val error: InternalError) : TypingOrInternalError()

    companion object {
        fun from(e: TypingError): TypingOrInternalError = Typing(e)
        fun from(e: InternalError): TypingOrInternalError = Internal(e)
    }
}

sealed class TypingNoContextOrInternalError : Exception() {
    data object Typing : TypingNoContextOrInternalError()
    class Internal(val error: InternalError) : TypingNoContextOrInternalError()

    companion object {
        fun from(e: TypingNoContextError): TypingNoContextOrInternalError = Typing
        fun from(e: InternalError): TypingNoContextOrInternalError = Internal(e)
    }
}
