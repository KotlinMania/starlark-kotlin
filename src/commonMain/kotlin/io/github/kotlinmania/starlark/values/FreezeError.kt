// port-lint: source values/freeze_error.rs
package io.github.kotlinmania.starlark.values.freezeerror

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

import io.github.kotlinmania.starlark.ErrorKind

/**
 * Freeze error type, only carries the original error message and potentially an array of contexts.
 *
 * Conversion from arbitrary exceptions is disallowed by design in order to enforce
 * that freeze errors only contain error messages and contexts as strings, no metadata.
 */
class FreezeError(
    /** The base error message. */
    val errMsg: String,
    /** The error contexts that are added to the error message. */
    val contexts: MutableList<String> = mutableListOf(),
) : Exception(errMsg) {

    /** Convert to an exception with layered context messages (mirrors `From<FreezeError> for anyhow::Error`). */
    fun toException(): Exception {
        val sb = StringBuilder(errMsg)
        for (context in contexts.reversed()) {
            sb.append("\n  context: $context")
        }
        return Exception(sb.toString(), this)
    }

    /** Convert to a starlark [io.github.kotlinmania.starlark.Error] (mirrors `From<FreezeError> for starlarkSyntax::Error`). */
    fun toStarlarkError(): io.github.kotlinmania.starlark.Error {
        return io.github.kotlinmania.starlark.Error.newKind(ErrorKind.Freeze(this.toException()))
    }

    companion object {
        /** Create a new freeze error. */
        fun new(errMsg: String): FreezeError {
            return FreezeError(errMsg)
        }
    }

    /** Add error context to this freeze error. */
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

/**
 * Provides the `context` method for [Result].
 *
 * This is designed to only be called on `Result<T>` types due to the nature of freeze errors.
 * This is to prevent callers from accidentally expecting context to carry metadata.
 */
interface FreezeErrorContext<T> : Sealed {
    fun freezeErrorContext(context: String): Result<T>
}

/** Protects against downstream implementations. */
sealed interface Sealed

/** Extension to add context to a `Result<T>` returned from freeze operations. */
fun <T> Result<T>.freezeErrorContext(context: String): Result<T> {
    return when {
        isSuccess -> this
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
