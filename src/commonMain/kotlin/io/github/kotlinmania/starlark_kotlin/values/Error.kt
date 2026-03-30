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

//! Define a common set of errors.

// use thiserror::Error;

// use crate::values::StarlarkValue;
// use crate::values::Value;

import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/// Common errors returned by Starlark evaluation.
// #[derive(Debug, Error)]
// #[allow(missing_docs)]
// pub enum ValueError
sealed class ValueError(override val message: String) : Exception(message) {
    // #[error("Operation `{op}` not supported on type `{typ}`")]
    data class OperationNotSupported(
        val op: String,
        val typ: String,
    ) : ValueError("Operation `$op` not supported on type `$typ`")

    // #[error("Operation `{op}` not supported for types `{left}` and `{right}`")]
    data class OperationNotSupportedBinary(
        val op: String,
        val left: String,
        val right: String,
    ) : ValueError("Operation `$op` not supported for types `$left` and `$right`")

    // #[error("Cannot divide by zero")]
    data object DivisionByZero : ValueError("Cannot divide by zero")

    // #[error("Integer overflow")]
    data object IntegerOverflow : ValueError("Integer overflow")

    // #[error("Negative shift count")]
    data object NegativeShiftCount : ValueError("Negative shift count")

    // #[error("Type of parameters mismatch")]
    data object IncorrectParameterType : ValueError("Type of parameters mismatch")

    // #[error("Type of parameter `{0}` doesn't match")]
    data class IncorrectParameterTypeNamed(
        val name: String,
    ) : ValueError("Type of parameter `$name` doesn't match")

    // #[error("Missing this parameter")]
    data object MissingThis : ValueError("Missing this parameter")

    // #[error("Missing required parameter `{0}`")]
    data class MissingRequired(
        val name: String,
    ) : ValueError("Missing required parameter `$name`")

    // #[error("Index `{0}` is out of bound")]
    data class IndexOutOfBound(
        val index: Int,
    ) : ValueError("Index `$index` is out of bound")

    // #[error("Key `{0}` was not found")]
    data class KeyNotFound(
        val key: String,
    ) : ValueError("Key `$key` was not found")

    // #[error("Immutable")]
    data object CannotMutateImmutableValue : ValueError("Immutable")

    // #[error("This operation mutates an iterable for an iterator while iterating.")]
    data object MutationDuringIteration :
        ValueError("This operation mutates an iterable for an iterator while iterating.")

    // #[error("Object of type `{0}` has no attribute `{1}`")]
    data class NoAttr(
        val typeName: String,
        val attr: String,
    ) : ValueError("Object of type `$typeName` has no attribute `$attr`")

    // #[error("Object of type `{0}` has no attribute `{1}`, did you mean `{2}`?")]
    data class NoAttrDidYouMean(
        val typeName: String,
        val attr: String,
        val suggestion: String,
    ) : ValueError("Object of type `$typeName` has no attribute `$attr`, did you mean `$suggestion`?")

    // impl ValueError

    companion object {
        // #[cold]
        // pub(crate) fn unsupported_owned<T>(left: &str, op: &str, right: Option<&str>) -> crate::Result<T>
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

        /// Helper to create an [OperationNotSupported] error.
        // #[cold]
        // pub fn unsupported<'v, T, V: StarlarkValue<'v>>(_left: &V, op: &str) -> crate::Result<T>
        fun <T> unsupported(leftType: String, op: String): Result<T> {
            return unsupportedOwned(leftType, op, null)
        }

        // #[cold]
        // pub(crate) fn unsupported_type<T>(left: Value, op: &str) -> crate::Result<T>
        internal fun <T> unsupportedType(left: Value, op: String): Result<T> {
            return unsupportedOwned(left.getType(), op, null)
        }

        /// Helper to create an [OperationNotSupportedBinary] error.
        // #[cold]
        // pub fn unsupported_with<'v, T, V: StarlarkValue<'v>>(_left: &V, op: &str, right: Value) -> crate::Result<T>
        fun <T> unsupportedWith(leftType: String, op: String, right: Value): Result<T> {
            return unsupportedOwned(leftType, op, right.getType())
        }
    }
}

// impl From<ValueError> for crate::Error
// fn from(e: ValueError) -> Self
//     crate::Error::new_kind(crate::ErrorKind::Value(anyhow::Error::new(e)))
// Kotlin: ValueError already extends Exception, so it can be thrown directly.

// #[derive(Debug, Error)]
// pub(crate) enum ControlError
internal sealed class ControlError(override val message: String) : Exception(message) {
    // #[error("Value of type `{0}` is not hashable")]
    data class NotHashableValue(
        val typeName: String,
    ) : ControlError("Value of type `$typeName` is not hashable")

    // #[error("Too many recursion levels")]
    data object TooManyRecursionLevel : ControlError("Too many recursion levels")
}
