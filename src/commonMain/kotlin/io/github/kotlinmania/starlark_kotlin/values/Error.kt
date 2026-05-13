// port-lint: source src/values/error.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.ErrorKind
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/** Common errors returned by Starlark evaluation. */
sealed class ValueError(override val message: String) : Exception(message) {
    data class OperationNotSupported(
        val op: String,
        val typ: String,
    ) : ValueError("Operation `$op` not supported on type `$typ`")

    data class OperationNotSupportedBinary(
        val op: String,
        val left: String,
        val right: String,
    ) : ValueError("Operation `$op` not supported for types `$left` and `$right`")

    data object DivisionByZero : ValueError("Cannot divide by zero")

    data object IntegerOverflow : ValueError("Integer overflow")

    data class Runtime(
        val details: String,
    ) : ValueError(details)

    data object NegativeShiftCount : ValueError("Negative shift count")

    data object IncorrectParameterType : ValueError("Type of parameters mismatch")

    data class IncorrectParameterTypeNamed(
        val name: String,
    ) : ValueError("Type of parameter `$name` doesn't match")

    data object MissingThis : ValueError("Missing this parameter")

    data class MissingRequired(
        val name: String,
    ) : ValueError("Missing required parameter `$name`")

    data class IndexOutOfBound(
        val index: Int,
    ) : ValueError("Index `$index` is out of bound")

    data class KeyNotFound(
        val key: String,
    ) : ValueError("Key `$key` was not found")

    data object CannotMutateImmutableValue : ValueError("Immutable")

    data object MutationDuringIteration :
        ValueError("This operation mutates an iterable for an iterator while iterating.")

    data class NoAttr(
        val typeName: String,
        val attr: String,
    ) : ValueError("Object of type `$typeName` has no attribute `$attr`")

    data class NoAttrDidYouMean(
        val typeName: String,
        val attr: String,
        val suggestion: String,
    ) : ValueError("Object of type `$typeName` has no attribute `$attr`, did you mean `$suggestion`?")

    companion object {
        internal fun <T> unsupportedOwned(
            left: String,
            op: String,
            right: String?,
        ): Result<T> {
            return when (right) {
                null -> Result.failure(
                    OperationNotSupported(op = op, typ = left)
                )
                else -> Result.failure(
                    OperationNotSupportedBinary(op = op, left = left, right = right)
                )
            }
        }

        /** Helper to create an [OperationNotSupported] error. */
        fun <T> unsupported(leftType: String, op: String): Result<T> {
            return unsupportedOwned(leftType, op, null)
        }

        internal fun <T> unsupportedType(left: Value, op: String): Result<T> {
            return unsupportedOwned(left.getType(), op, null)
        }

        /** Helper to create an [OperationNotSupportedBinary] error. */
        fun <T> unsupportedWith(leftType: String, op: String, right: Value): Result<T> {
            return unsupportedOwned(leftType, op, right.getType())
        }
    }
}

/** Convert a [ValueError] to a starlark [io.github.kotlinmania.starlark_kotlin.Error]. */
fun ValueError.toStarlarkError(): io.github.kotlinmania.starlark_kotlin.Error {
    return io.github.kotlinmania.starlark_kotlin.Error.newKind(ErrorKind.Value(this))
}

internal sealed class ControlError(override val message: String) : Exception(message) {
    data class NotHashableValue(
        val typeName: String,
    ) : ControlError("Value of type `$typeName` is not hashable")

    data object TooManyRecursionLevel : ControlError("Too many recursion levels")
}
