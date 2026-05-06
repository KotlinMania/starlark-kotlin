// port-lint: source ../starlark_syntax/src/error.rs
package io.github.kotlinmania.starlark

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

import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.FileSpan as FileSpan
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlarksyntax.frame.Frame as Frame

/**
 * An error produced by starlark.
 *
 * This error is composed of an error kind, together with some diagnostic information indicating
 * where it occurred.
 *
 * Port of `starlarkSyntax::Error` (re-exported as `starlark::Error`).
 */
class Error private constructor(
    private val kind: ErrorKind,
    private var fileSpan: FileSpan? = null,
    private var callStackFrames: List<Frame> = emptyList(),
) : Exception(kind.toString()) {

    companion object {
        /** Create a new error. */
        fun newKind(kind: ErrorKind): Error {
            return Error(kind)
        }

        /** Create a new error with a span. */
        fun newSpanned(kind: ErrorKind, span: Span, codemap: CodeMap): Error {
            val fileSpan = codemap.fileSpan(span)
            return Error(kind, fileSpan)
        }

        /** Create a new error with no diagnostic and of kind [ErrorKind.Other]. */
        fun newOther(e: Throwable): Error {
            return Error(ErrorKind.Other(e))
        }

        /** Create a new error with no diagnostic and of kind [ErrorKind.Native]. */
        fun newNative(e: Throwable): Error {
            return Error(ErrorKind.Native(e))
        }

        /** Create a new error with no diagnostic and of kind [ErrorKind.Value]. */
        fun newValue(e: Throwable): Error {
            return Error(ErrorKind.Value(e))
        }
    }

    /** The kind of this error. */
    fun kind(): ErrorKind = kind

    /** Convert the error into the underlying kind. */
    fun intoKind(): ErrorKind = kind

    /** Whether this error has diagnostic information attached. */
    fun hasDiagnostic(): Boolean {
        return fileSpan != null || callStackFrames.isNotEmpty()
    }

    /**
     * Returns the error kind, which can be used to format this error without including the
     * diagnostic information.
     */
    fun withoutDiagnostic(): ErrorKind = kind

    /** The span of this error, if available. */
    fun span(): FileSpan? = fileSpan

    /** The call stack frames, if available. */
    fun callStack(): List<Frame> = callStackFrames

    /** Set the span, unless it's already been set. */
    fun setSpan(span: Span, codemap: CodeMap) {
        if (fileSpan == null) {
            fileSpan = codemap.fileSpan(span)
        }
    }

    /** Set the call stack, unless it's already been set. */
    fun setCallStack(callStack: () -> List<Frame>) {
        if (callStackFrames.isEmpty()) {
            callStackFrames = callStack()
        }
    }

    /** Change error kind to internal error. */
    fun intoInternalError(): Error {
        return if (kind is ErrorKind.Internal) {
            this
        } else {
            Error(kind.intoInternalError(), fileSpan, callStackFrames)
        }
    }

    override fun toString(): String {
        return if (hasDiagnostic()) {
            val spanStr = fileSpan?.let { " at $it" } ?: ""
            "${kind}${spanStr}"
        } else {
            kind.toString()
        }
    }
}

/** The different kinds of errors that can be produced by starlark. */
sealed class ErrorKind {
    /** An explicit `fail` invocation. */
    class Fail(val error: Throwable) : ErrorKind()

    /** Starlark call stack overflow. */
    class StackOverflow(val error: Throwable) : ErrorKind()

    /**
     * An error approximately associated with a value.
     * Includes unsupported operations, missing attributes, things of that sort.
     */
    class Value(val error: Throwable) : ErrorKind()

    /** Errors relating to the way a function is called (wrong number of args, etc.). */
    class Function(val error: Throwable) : ErrorKind()

    /** Out of scope variables and similar. */
    class Scope(val error: Throwable) : ErrorKind()

    /** Syntax error. */
    class Parser(val error: Throwable) : ErrorKind()

    /** Freeze errors. Should have no metadata attached. */
    class Freeze(val error: Throwable) : ErrorKind()

    /** Indicates a logic bug in starlark. */
    class Internal(val error: Throwable) : ErrorKind()

    /**
     * Error from user provided native function
     * (but not from native functions provided by starlark crate).
     */
    class Native(val error: Throwable) : ErrorKind()

    /**
     * Fallback option.
     * For errors produced by starlark which have not yet been assigned their own kind.
     */
    class Other(val error: Throwable) : ErrorKind()

    /** The source of the error, akin to [Throwable.cause]. */
    fun source(): Throwable? {
        return when (this) {
            is Fail -> null
            is StackOverflow -> null
            is Value -> null
            is Function -> null
            is Scope -> null
            is Freeze -> null
            is Parser -> null
            is Internal -> null
            is Native -> error.cause
            is Other -> error.cause
        }
    }

    /** Change type to [Internal]. */
    internal fun intoInternalError(): ErrorKind {
        val e = when (this) {
            is Internal -> error
            is Fail -> error
            is Value -> error
            is Function -> error
            is Scope -> error
            is Freeze -> error
            is Parser -> error
            is StackOverflow -> error
            is Native -> error
            is Other -> error
        }
        return Internal(e)
    }

    /** The inner error. */
    fun innerError(): Throwable = when (this) {
        is Fail -> error
        is StackOverflow -> error
        is Value -> error
        is Function -> error
        is Scope -> error
        is Parser -> error
        is Freeze -> error
        is Internal -> error
        is Native -> error
        is Other -> error
    }

    override fun toString(): String = when (this) {
        is Fail -> "fail:${error.message}"
        is StackOverflow -> error.message ?: "stack overflow"
        is Value -> error.message ?: "value error"
        is Function -> error.message ?: "function error"
        is Scope -> error.message ?: "scope error"
        is Parser -> error.message ?: "parser error"
        is Freeze -> error.message ?: "freeze error"
        is Internal -> "Internal error: ${error.message}"
        is Native -> error.message ?: "native error"
        is Other -> error.message ?: "error"
    }
}
