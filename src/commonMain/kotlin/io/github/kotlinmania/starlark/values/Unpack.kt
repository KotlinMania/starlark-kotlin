// port-lint: source src/values/unpack.rs
package io.github.kotlinmania.starlark.values

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

/** Parameter conversion utilities for `starlark_module` macros. */

import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Value

/** Error that can be returned by [UnpackValue]. */
interface UnpackValueError {
    /** Convert into a crate error. */
    fun intoError(): Error
}

/** [UnpackValueError] impl for [Error]. */
internal fun Error.asUnpackValueError(): UnpackValueError =
    object : UnpackValueError {
        override fun intoError(): Error = this@asUnpackValueError
    }

/** Never error. */
interface UnpackValueErrorInfallible : UnpackValueError {
    /** Convert into a never type. */
    fun intoInfallible(): Nothing
}

/** [UnpackValueError] impl for [Either]. */
class EitherUnpackValueError<A : UnpackValueError, B : UnpackValueError>(
    private val either: Either<A, B>,
) : UnpackValueError {
    override fun intoError(): Error =
        when (either) {
            is Either.Left -> either.value.intoError()
            is Either.Right -> either.value.intoError()
        }
}

/** [UnpackValueErrorInfallible] impl for [Either]. */
class EitherUnpackValueErrorInfallible<A : UnpackValueErrorInfallible, B : UnpackValueErrorInfallible>(
    private val either: Either<A, B>,
) : UnpackValueErrorInfallible {
    override fun intoError(): Error =
        when (either) {
            is Either.Left -> either.value.intoError()
            is Either.Right -> either.value.intoError()
        }

    override fun intoInfallible(): Nothing =
        when (either) {
            is Either.Left -> either.value.intoInfallible()
            is Either.Right -> either.value.intoInfallible()
        }
}

/**
 * How to convert a [Value] to a Kotlin type. Required for all arguments in
 * a starlark_module definition.
 *
 * Given a [Value], try and unpack it into the given type,
 * which may involve some element of conversion.
 */
interface UnpackValue<T> : StarlarkTypeRepr {
    /**
     * Given a [Value], try and unpack it into the given type,
     * which may involve some element of conversion.
     *
     * Return `null` if the value is not of expected type (as described by [StarlarkTypeRepr]),
     * and throw if the value is of expected type, but conversion cannot be performed.
     */
    fun unpackValueImpl(value: Value): Result<T?>

    /**
     * Given a [Value], try and unpack it into the given type,
     * which may involve some element of conversion.
     *
     * Return `null` if the value is not of expected type,
     * and throw if conversion cannot be performed.
     */
    fun unpackValue(value: Value): Result<T?> = unpackValueImpl(value)

    /** Unpack a value if unpacking is infallible. */
    fun unpackValueOpt(value: Value): T? = unpackValueImpl(value).getOrThrow()

    /** Unpack a value, but return error instead of `null` if unpacking fails. */
    fun unpackValueErr(value: Value): T {
        val result = unpackValue(value).getOrThrow()
        if (result != null) return result
        throw Error.newValue(
            IllegalArgumentException(
                "Expected `${starlarkTypeRepr()}`, but got `${value.toStringForTypeError()}`",
            ),
        )
    }

    /** Unpack value, but instead of `null` return error about incorrect argument type. */
    fun unpackParam(value: Value): T {
        val result = unpackValue(value).getOrThrow()
        if (result != null) return result
        throw Error.newValue(
            IllegalArgumentException(
                "Type of parameters mismatch, expected `${starlarkTypeRepr()}`, actual `${value.toStringForTypeError()}`",
            ),
        )
    }

    /** Unpack value, but instead of `null` return error about incorrect named argument type. */
    fun unpackNamedParam(value: Value, paramName: String): T {
        val unpacked =
            try {
                unpackValue(value).getOrThrow()
            } catch (e: Exception) {
                val detail = e.message
                throw Error.newValue(
                    IllegalArgumentException(
                        buildString {
                            append("Error unpacking value for parameter `$paramName` of type `${starlarkTypeRepr()}`")
                            if (!detail.isNullOrBlank()) {
                                append(": ")
                                append(detail)
                            }
                        },
                        e,
                    ),
                )
            }
        if (unpacked != null) return unpacked
        throw Error.newValue(
            IllegalArgumentException(
                "Type of parameter `$paramName` doesn't match, expected `${starlarkTypeRepr()}`, actual `${value.toStringForTypeError()}`",
            ),
        )
    }
}

/** [UnpackValue] impl for [Value] (identity). */
object ValueUnpackValue : UnpackValue<Value> {
    override fun unpackValueImpl(value: Value): Result<Value?> = Result.success(value)

    override fun starlarkTypeRepr(): Ty = Ty.any()
}

/** [UnpackValue] impl for [Either]. */
class EitherUnpackValue<TLeft, TRight>(
    private val left: UnpackValue<TLeft>,
    private val right: UnpackValue<TRight>,
) : UnpackValue<Either<TLeft, TRight>> {
    override fun unpackValueImpl(value: Value): Result<Either<TLeft, TRight>?> {
        val leftResult =
            left.unpackValueImpl(value).getOrElse { e ->
                return Result.failure(e)
            }
        if (leftResult != null) {
            return Result.success(Either.Left(leftResult))
        }
        val rightResult =
            right.unpackValueImpl(value).getOrElse { e ->
                return Result.failure(e)
            }
        return Result.success(rightResult?.let { Either.Right(it) })
    }

    override fun starlarkTypeRepr(): Ty =
        Ty.union2(left.starlarkTypeRepr(), right.starlarkTypeRepr())
}
