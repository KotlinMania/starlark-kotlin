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

/**
 * Error type for `Freeze` implementations. Freeze errors should only contain error messages
 * and error contexts as strings and no metadata.
 * Conversion from generic exceptions is disallowed by design in order to enforce the above.
 */

/** Alias for `Result<T, FreezeError>`. */
typealias FreezeResult<T> = Result<T>

/**
 * Freeze error type, only carries the original error message and potentially an array of contexts.
 *
 * @property errMsg The base error message.
 * @property contexts The error contexts that are added to the error message.
 */
class FreezeError(
    val errMsg: String,
    val contexts: MutableList<String> = mutableListOf(),
) : Exception(errMsg) {

    companion object {
        /** Create a new freeze error type. */
        fun new(errMsg: String): FreezeError {
            return FreezeError(errMsg)
        }
    }

    /**
     * Convert this [FreezeError] to an [Exception] with contexts applied.
     *
     * Mirrors `From<FreezeError> for anyhow::Error` in the Rust implementation.
     * Contexts are applied in reverse order to match Rust behavior.
     */
    fun toException(): Exception {
        val sb = StringBuilder(errMsg)
        for (context in contexts.reversed()) {
            sb.append("\n  context: $context")
        }
        return Exception(sb.toString(), this)
    }

    /** Add error contexts to freeze error. */
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

/** Protects against downstream implementations. */
sealed interface Sealed

/**
 * Provides the `context` method for `FreezeResult`.
 *
 * This is designed to only be called on `FreezeResult` types due to the nature of freeze error.
 * This is to prevent callers from accidentally expecting context to carry metadata.
 */
interface FreezeErrorContext<T> : Sealed {
    /** Add a string error context to an existing `FreezeResult` type. */
    fun freezeErrorContext(context: String): FreezeResult<T>
}

/** Extension implementing [FreezeErrorContext] for `Result<T>` containing [FreezeError]. */
fun <T> Result<T>.freezeErrorContext(context: String): FreezeResult<T> {
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
