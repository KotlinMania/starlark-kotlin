// port-lint: source src/typing/error.rs
package io.github.kotlinmania.starlark.typing

// use std::fmt::Display;

// use starlark_syntax::diagnostic::WithDiagnostic;
// use starlark_syntax::eval_exception::EvalException;

// use crate::codemap::CodeMap;
// use crate::codemap::Span;

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.CodeMap

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

// Kotlin: EvalException is defined in starlark_syntax in Rust.
// We define it here for now.
class EvalException(override val message: String, cause: Throwable? = null) : Exception(message, cause) {
    companion object {
        // fn new(error: crate::Error, span: Span, codemap: &CodeMap) -> EvalException
        fun new(error: StarlarkError, span: Span, codemap: CodeMap): EvalException {
            return EvalException("${error.message} at ${span} in ${codemap}")
        }

        fun newAnyhow(error: Throwable, span: Span, codemap: CodeMap): EvalException {
            return EvalException("${error.message} at ${span} in ${codemap}", error)
        }

        // fn parser_error(error: impl Display, span: Span, codemap: &CodeMap) -> EvalException
        fun parserError(message: String, span: Span, codemap: CodeMap): EvalException {
            return EvalException("$message at $span in $codemap")
        }

        // fn internal_error(error: impl Display, span: Span, codemap: &CodeMap) -> EvalException
        fun internalError(message: String, span: Span, codemap: CodeMap): EvalException {
            return new(StarlarkError("Internal: $message"), span, codemap)
        }

        // EvalException::new_with_callstack(e, span, file, || frames)
        fun newWithCallStack(
            error: Throwable,
            span: Span,
            file: CodeMap,
            callStackFrames: () -> List<Any>,
        ): EvalException {
            val frames = callStackFrames()
            val framesStr = if (frames.isNotEmpty()) "\n  ${frames.joinToString("\n  ")}" else ""
            return EvalException("${error.message} at $span in $file$framesStr", error)
        }
    }

    fun intoError(): StarlarkError = StarlarkError(message, this)

    fun intoInternalError(): EvalException {
        return EvalException("Internal: $message", cause)
    }
}

open class StarlarkError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    fun intoInternalError(): StarlarkError {
        return StarlarkError("Internal: $message", cause)
    }
}


class WithDiagnostic<T>(val value: T, val span: Span, val codemap: CodeMap) {
    fun <R> map(f: (T) -> R): WithDiagnostic<R> {
        return WithDiagnostic(f(value), span, codemap)
    }
}

/// Internal error, bug in the typechecker.
// #[derive(Debug)]
// pub struct InternalError(EvalException);
class InternalError(private val exception: EvalException) : Exception(exception.message, exception) {
    // impl InternalError
    companion object {
        // #[cold]
        // pub(crate) fn msg(message: impl Display, span: Span, codemap: &CodeMap) -> InternalError
        fun msg(message: Any, span: Span, codemap: CodeMap): InternalError {
            return InternalError(
                EvalException.new(
                    StarlarkError(message.toString()),
                    span,
                    codemap
                )
            )
        }

        // #[cold]
        // pub(crate) fn from_diagnostic(d: WithDiagnostic<impl Display>) -> InternalError
        fun fromDiagnostic(d: WithDiagnostic<Any>): InternalError {
            val internal = d.map { m ->
                StarlarkError(m.toString())
            }
            return InternalError(
                EvalException.new(internal.value, internal.span, internal.codemap)
            )
        }

        // #[cold]
        // pub(crate) fn from_eval_exception(e: EvalException) -> InternalError
        fun fromEvalException(e: EvalException): InternalError {
            return InternalError(e.intoInternalError())
        }

        // #[cold]
        // pub(crate) fn from_error(e: crate::Error, span: Span, codemap: &CodeMap) -> InternalError
        fun fromError(e: StarlarkError, span: Span, codemap: CodeMap): InternalError {
            val internalErr = e.intoInternalError()
            return InternalError(EvalException.new(internalErr, span, codemap))
        }
    }

    // #[cold]
    // pub(crate) fn into_error(self) -> crate::Error
    fun intoError(): StarlarkError {
        return exception.intoError()
    }

    // #[cold]
    // pub(crate) fn into_eval_exception(self) -> EvalException
    fun intoEvalException(): EvalException {
        return exception
    }
}

/// Errors used in typechecker API. Error has a span.
// pub struct TypingError(EvalException);
class TypingError(private val exception: EvalException) {
    // impl TypingError
    companion object {
        // #[cold]
        // pub(crate) fn msg(message: impl Display, span: Span, codemap: &CodeMap) -> TypingError
        fun msg(message: Any, span: Span, codemap: CodeMap): TypingError {
            return TypingError(
                EvalException.newAnyhow(
                    Exception(message.toString()),
                    span,
                    codemap
                )
            )
        }

        // #[cold]
        // pub(crate) fn new(error: crate::Error, span: Span, codemap: &CodeMap) -> TypingError
        fun new(error: StarlarkError, span: Span, codemap: CodeMap): TypingError {
            return TypingError(EvalException.new(error, span, codemap))
        }

        // #[cold]
        // pub(crate) fn new_anyhow(error: anyhow::Error, span: Span, codemap: &CodeMap) -> TypingError
        fun newAnyhow(error: Throwable, span: Span, codemap: CodeMap): TypingError {
            return TypingError(EvalException.newAnyhow(error, span, codemap))
        }

        // #[cold]
        // pub(crate) fn from_eval_exception(e: EvalException) -> TypingError
        fun fromEvalException(e: EvalException): TypingError {
            return TypingError(e)
        }
    }

    // #[cold]
    // pub(crate) fn into_error(self) -> crate::Error
    fun intoError(): StarlarkError {
        return exception.intoError()
    }

    // #[cold]
    // pub(crate) fn into_eval_exception(self) -> EvalException
    fun intoEvalException(): EvalException {
        return exception
    }
}

/// Like [`TypingError`], but without a message or span.
// pub struct TypingNoContextError;
object TypingNoContextError : Exception("typing error (no context)")

/// Either a typing error or an internal error.
/// * Typing error means, types are not compatible.
/// * Internal error means, bug in the typechecker.
// pub enum TypingOrInternalError {
//     Typing(TypingError),
//     Internal(InternalError),
// }
sealed class TypingOrInternalError : Exception() {
    class Typing(val error: TypingError) : TypingOrInternalError()
    class Internal(val error: InternalError) : TypingOrInternalError()

    companion object {
        // impl From<TypingError> for TypingOrInternalError
        fun from(e: TypingError): TypingOrInternalError = Typing(e)
        // impl From<InternalError> for TypingOrInternalError
        fun from(e: InternalError): TypingOrInternalError = Internal(e)
    }
}

// pub enum TypingNoContextOrInternalError {
//     Typing,
//     Internal(InternalError),
// }
sealed class TypingNoContextOrInternalError : Exception() {
    data object Typing : TypingNoContextOrInternalError()
    class Internal(val error: InternalError) : TypingNoContextOrInternalError()

    companion object {
        // impl From<TypingNoContextError> for TypingNoContextOrInternalError
        fun from(@Suppress("UNUSED_PARAMETER") e: TypingNoContextError): TypingNoContextOrInternalError = Typing
        // impl From<InternalError> for TypingNoContextOrInternalError
        fun from(e: InternalError): TypingNoContextOrInternalError = Internal(e)
    }
}
