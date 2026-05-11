// port-lint: source values/unpack.rs
package io.github.kotlinmania.starlark.values

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

/**
 * Parameter conversion utilities for `starlarkModule` definitions.
 *
 * This file defines the machinery for converting a Starlark [Value] into a Kotlin type when
 * calling host-provided functions.
 */

import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Value

/** Error that can be returned by [UnpackValue]. */
interface UnpackValueError {
    /** Convert into a crate error. */
    fun intoError(): Error

    companion object {
        fun intoError(this_: UnpackValueError): Error = this_.intoError()
    }
}

/** [UnpackValueError] implementation for [Error]. */
fun Error.asUnpackValueError(): UnpackValueError = object : UnpackValueError {
    override fun intoError(): Error = this@asUnpackValueError
}

/** Never error. */
interface UnpackValueErrorInfallible : UnpackValueError {
    /** Convert into a never type. */
    fun intoInfallible(): Nothing

    companion object {
        fun intoInfallible(this_: UnpackValueErrorInfallible): Nothing = this_.intoInfallible()
    }
}

/** [UnpackValueError] implementation for [Either]. */
class EitherUnpackValueError<A : UnpackValueError, B : UnpackValueError>(
    private val either: Either<A, B>,
) : UnpackValueError {
    override fun intoError(): Error = when (either) {
        is Either.Left -> either.value.intoError()
        is Either.Right -> either.value.intoError()
    }
}

/** [UnpackValueErrorInfallible] implementation for [Either]. */
class EitherUnpackValueErrorInfallible<A : UnpackValueErrorInfallible, B : UnpackValueErrorInfallible>(
    private val either: Either<A, B>,
) : UnpackValueErrorInfallible {
    override fun intoError(): Error = when (either) {
        is Either.Left -> either.value.intoError()
        is Either.Right -> either.value.intoError()
    }

    override fun intoInfallible(): Nothing = when (either) {
        is Either.Left -> either.value.intoInfallible()
        is Either.Right -> either.value.intoInfallible()
    }
}

private enum class UnpackParamErrorKind {
    IncorrectType,
    IncorrectParameterType,
}

private fun error(value: Value, ty: () -> Ty, kind: UnpackParamErrorKind): Error {
    val message = when (kind) {
        UnpackParamErrorKind.IncorrectType ->
            "Expected `${ty()}`, but got `${value.toStringForTypeError()}`"
        UnpackParamErrorKind.IncorrectParameterType ->
            "Type of parameters mismatch, expected `${ty()}`, actual `${value.toStringForTypeError()}`"
    }
    return Error.newValue(IllegalArgumentException(message))
}

private fun error(value: Value, ty: () -> Ty): Error {
    return Error.newValue(
        IllegalArgumentException(
            "Type of parameters mismatch, expected `${ty()}`, actual `${value.toStringForTypeError()}`"
        )
    )
}

private fun error(value: Value, paramName: String, ty: () -> Ty): Error {
    return Error.newValue(
        IllegalArgumentException(
            "Type of parameter `$paramName` doesn't match, expected `${ty()}`, actual `${value.toStringForTypeError()}`"
        )
    )
}

/**
 * How to convert a [Value] to a Kotlin type. Required for all arguments in
 * a starlarkModule definition.
 *
 * Given a [Value], try and unpack it into the given type,
 * which may involve some element of conversion.
 */
/**
 * How to convert a [Value] to a Kotlin type. Required for all arguments in a `starlarkModule`
 * definition.
 *
 * Note for simple references it often can be implemented by making the value implement
 * [StarlarkValue] and registering the type with the `starlarkSimpleValue`/`starlarkComplexValue`
 * helpers. For example:
 *
 * ```kotlin
 * class MySimpleValue : StarlarkValue
 *
 * // In your module init:
 * // starlarkSimpleValue(
 * //     type = MySimpleValue::class,
 * //     allocValue = { v, heap -> heap.allocSimple(v) },
 * //     allocFrozenValue = { v, heap -> heap.allocSimple(v) },
 * //     fromValue = { value -> value.getUnderlyingPtr() as? MySimpleValue },
 * // )
 * ```
 *
 * Whereas for types that aren't also [StarlarkValue] you can define a Kotlin wrapper type and
 * implement [StarlarkTypeRepr] and [UnpackValue]. For example:
 *
 * ```kotlin
 * class BoolOrInt(val value: Int)
 *
 * object BoolOrIntUnpack : UnpackValue<BoolOrInt> {
 *     override fun unpackValueImpl(value: Value): Result<BoolOrInt?> {
 *         val b = value.unpackBool()
 *         if (b != null) return Result.success(BoolOrInt(if (b) 1 else 0))
 *
 *         val x = value.unpackI32() ?: return Result.success(null)
 *         return Result.success(BoolOrInt(x))
 *     }
 *
 *     override fun starlarkTypeRepr(): Ty =
 *         EitherTypeRepr(BoolStarlarkTypeRepr, I32TypeRepr).starlarkTypeRepr()
 * }
 * ```
 */
interface UnpackValue<T> : StarlarkTypeRepr {
    /**
     * Given a [Value], try and unpack it into the given type,
     * which may involve some element of conversion.
     *
     * Return `null` if the value is not of expected type (as described by [StarlarkTypeRepr]),
     * and return a failed [Result] if the value is of expected type, but conversion cannot be performed.
     * For example, when unpacking an integer to `String`, return `null`,
     * and when unpacking a large integer to `Int`, return a failure.
     *
     * This function needs to be implemented, but usually not meant to be called directly.
     * Consider using [unpackValue], [unpackValueErr], or [unpackValueOpt] instead.
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
        throw error(value, ::starlarkTypeRepr, UnpackParamErrorKind.IncorrectType)
    }

    /** Unpack value, but instead of `null` return error about incorrect argument type. */
    fun unpackParam(value: Value): T {
        val result = unpackValue(value).getOrThrow()
        if (result != null) return result
        throw error(value, ::starlarkTypeRepr)
    }

    /** Unpack value, but instead of `null` return error about incorrect named argument type. */
    fun unpackNamedParam(value: Value, paramName: String): T {
        val unpacked = try {
            unpackValue(value).getOrThrow()
        } catch (e: Exception) {
            throw Error.newValue(
                IllegalArgumentException(
                    "Error unpacking value for parameter `$paramName` of type `${starlarkTypeRepr()}`",
                    e,
                )
            )
        }
        if (unpacked != null) return unpacked
        throw error(value, paramName, ::starlarkTypeRepr)
    }
}

/** [UnpackValue] implementation for [Value] (identity). */
object ValueUnpackValue : UnpackValue<Value> {
    override fun unpackValueImpl(value: Value): Result<Value?> = Result.success(value)

    override fun starlarkTypeRepr(): Ty = Ty.any()
}

/** [UnpackValue] implementation for [Either]. */
class EitherUnpackValue<TLeft, TRight>(
    private val left: UnpackValue<TLeft>,
    private val right: UnpackValue<TRight>,
) : UnpackValue<Either<TLeft, TRight>> {
    override fun unpackValueImpl(value: Value): Result<Either<TLeft, TRight>?> {
        val leftResult = left.unpackValueImpl(value).getOrElse { e ->
            return Result.failure(e)
        }
        if (leftResult != null) {
            return Result.success(Either.Left(leftResult))
        }
        val rightResult = right.unpackValueImpl(value).getOrElse { e ->
            return Result.failure(e)
        }
        return Result.success(rightResult?.let { Either.Right(it) })
    }

    override fun starlarkTypeRepr(): Ty =
        Ty.union2(left.starlarkTypeRepr(), right.starlarkTypeRepr())
}
