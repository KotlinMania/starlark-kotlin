// port-lint: source src/typing/error.rs
package io.github.kotlinmania.starlark.typing

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span

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

/** Evaluation exception with source location diagnostics attached. */
class EvalException(
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    companion object {
        fun new(error: StarlarkError, span: Span, codemap: CodeMap): EvalException = EvalException("${error.message} at $span in $codemap")

        fun newAnyhow(error: Throwable, span: Span, codemap: CodeMap): EvalException = EvalException("${error.message} at $span in $codemap", error)

        fun parserError(message: String, span: Span, codemap: CodeMap): EvalException = EvalException("$message at $span in $codemap")

        fun internalError(message: String, span: Span, codemap: CodeMap): EvalException = new(StarlarkError("Internal: $message"), span, codemap)

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

    fun intoInternalError(): EvalException = EvalException("Internal: $message", cause)
}

open class StarlarkError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    fun intoInternalError(): StarlarkError = StarlarkError("Internal: $message", cause)
}

class WithDiagnostic<T>(
    val value: T,
    val span: Span,
    val codemap: CodeMap,
) {
    fun <R> map(f: (T) -> R): WithDiagnostic<R> = WithDiagnostic(f(value), span, codemap)
}

/** Internal typechecker error. */
class InternalError(
    private val exception: EvalException,
) : Exception(exception.message, exception) {
    companion object {
        fun msg(message: Any, span: Span, codemap: CodeMap): InternalError =
            InternalError(
                EvalException.new(
                    StarlarkError(message.toString()),
                    span,
                    codemap,
                ),
            )

        fun fromDiagnostic(d: WithDiagnostic<Any>): InternalError {
            val internal =
                d.map { m ->
                    StarlarkError(m.toString())
                }
            return InternalError(
                EvalException.new(internal.value, internal.span, internal.codemap),
            )
        }

        fun fromEvalException(e: EvalException): InternalError = InternalError(e.intoInternalError())

        fun fromError(e: StarlarkError, span: Span, codemap: CodeMap): InternalError {
            val internalErr = e.intoInternalError()
            return InternalError(EvalException.new(internalErr, span, codemap))
        }
    }

    fun intoError(): StarlarkError = exception.intoError()

    fun intoEvalException(): EvalException = exception
}

/** Typechecker error with source location diagnostics attached. */
class TypingError(
    private val exception: EvalException,
) {
    companion object {
        fun msg(message: Any, span: Span, codemap: CodeMap): TypingError =
            TypingError(
                EvalException.newAnyhow(
                    Exception(message.toString()),
                    span,
                    codemap,
                ),
            )

        fun new(error: StarlarkError, span: Span, codemap: CodeMap): TypingError = TypingError(EvalException.new(error, span, codemap))

        fun newAnyhow(error: Throwable, span: Span, codemap: CodeMap): TypingError = TypingError(EvalException.newAnyhow(error, span, codemap))

        fun fromEvalException(e: EvalException): TypingError = TypingError(e)
    }

    fun intoError(): StarlarkError = exception.intoError()

    fun intoEvalException(): EvalException = exception
}

/** Typechecker error without source context. */
object TypingNoContextError : Exception("typing error (no context)")

/** Either an ordinary typing error or an internal typechecker error. */
sealed class TypingOrInternalError(
    message: String,
    cause: Throwable,
) : Exception(message, cause) {
    class Typing(
        val error: TypingError,
    ) : TypingOrInternalError(error.intoEvalException().message, error.intoEvalException())

    class Internal(
        val error: InternalError,
    ) : TypingOrInternalError(error.intoEvalException().message, error.intoEvalException())

    companion object {
        fun from(e: TypingError): TypingOrInternalError = Typing(e)

        fun from(e: InternalError): TypingOrInternalError = Internal(e)
    }
}

sealed class TypingNoContextOrInternalError : Exception() {
    data object Typing : TypingNoContextOrInternalError()

    class Internal(
        val error: InternalError,
    ) : TypingNoContextOrInternalError()

    companion object {
        fun from(
            @Suppress("UNUSED_PARAMETER") e: TypingNoContextError,
        ): TypingNoContextOrInternalError = Typing

        fun from(e: InternalError): TypingNoContextOrInternalError = Internal(e)
    }
}
