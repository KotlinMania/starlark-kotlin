<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/Unpack.kt
// port-lint: source values/unpack.rs
package io.github.kotlinmania.starlark.values
=======
// port-lint: source src/values/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/Unpack.kt

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

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/Unpack.kt
/**
 * Parameter conversion utilities for `starlarkModule` definitions.
 *
 * This file defines the machinery for converting a Starlark [Value] into a Kotlin type when
 * calling host-provided functions.
 */
=======
/** Parameter conversion utilities for `starlark_module` macros. */
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/Unpack.kt

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.Error
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/** Error that can be returned by [UnpackValue]. */
// pub trait UnpackValueError: Debug + Send + Sync + 'static
interface UnpackValueError {
    /** Convert into a crate error. */
    // fn into_error(this: Self) -> crate::Error
    fun intoError(): Error

    companion object {
        fun intoError(this_: UnpackValueError): Error = this_.intoError()
    }
}

/** [UnpackValueError] impl for [Error]. */
// impl UnpackValueError for crate::Error
fun Error.asUnpackValueError(): UnpackValueError = object : UnpackValueError {
    override fun intoError(): Error = this@asUnpackValueError
}

/** Never error. */
// pub trait UnpackValueErrorInfallible: UnpackValueError
interface UnpackValueErrorInfallible : UnpackValueError {
    /** Convert into a never type. */
    // fn into_infallible(this: Self) -> !
    fun intoInfallible(): Nothing

    companion object {
        fun intoInfallible(this_: UnpackValueErrorInfallible): Nothing = this_.intoInfallible()
    }
}

/** [UnpackValueError] impl for [Either]. */
// impl<A: UnpackValueError, B: UnpackValueError> UnpackValueError for Either<A, B>
class EitherUnpackValueError<A : UnpackValueError, B : UnpackValueError>(
    private val either: Either<A, B>,
) : UnpackValueError {
    override fun intoError(): Error = when (either) {
        is Either.Left -> either.value.intoError()
        is Either.Right -> either.value.intoError()
    }
}

/** [UnpackValueErrorInfallible] impl for [Either]. */
// impl<A: UnpackValueErrorInfallible, B: UnpackValueErrorInfallible> UnpackValueErrorInfallible for Either<A, B>
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
 * a starlark_module definition.
 *
 * Given a [Value], try and unpack it into the given type,
 * which may involve some element of conversion.
 */
<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/values/Unpack.kt
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
=======
// pub trait UnpackValue<'v>: Sized + StarlarkTypeRepr
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/values/Unpack.kt
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
    // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
    fun unpackValueImpl(value: Value): Result<T?>

    /**
     * Given a [Value], try and unpack it into the given type,
     * which may involve some element of conversion.
     *
     * Return `null` if the value is not of expected type,
     * and throw if conversion cannot be performed.
     */
    // fn unpack_value(value: Value<'v>) -> Result<Option<Self>, crate::Error>
    fun unpackValue(value: Value): Result<T?> = unpackValueImpl(value)

    /** Unpack a value if unpacking is infallible. */
    // fn unpack_value_opt(value: Value<'v>) -> Option<Self>
    fun unpackValueOpt(value: Value): T? = unpackValueImpl(value).getOrThrow()

    /** Unpack a value, but return error instead of `null` if unpacking fails. */
    // fn unpack_value_err(value: Value<'v>) -> crate::Result<Self>
    fun unpackValueErr(value: Value): T {
        val result = unpackValue(value).getOrThrow()
        if (result != null) return result
        throw error(value, ::starlarkTypeRepr, UnpackParamErrorKind.IncorrectType)
    }

    /** Unpack value, but instead of `null` return error about incorrect argument type. */
    // fn unpack_param(value: Value<'v>) -> crate::Result<Self>
    fun unpackParam(value: Value): T {
        val result = unpackValue(value).getOrThrow()
        if (result != null) return result
        throw error(value, ::starlarkTypeRepr)
    }

    /** Unpack value, but instead of `null` return error about incorrect named argument type. */
    // fn unpack_named_param(value: Value<'v>, param_name: &str) -> crate::Result<Self>
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

/** [UnpackValue] impl for [Value] (identity). */
// impl<'v> UnpackValue<'v> for Value<'v>
object ValueUnpackValue : UnpackValue<Value> {
    override fun unpackValueImpl(value: Value): Result<Value?> = Result.success(value)

    override fun starlarkTypeRepr(): Ty = Ty.any()
}

/** [UnpackValue] impl for [Either]. */
// impl<'v, TLeft: UnpackValue<'v>, TRight: UnpackValue<'v>> UnpackValue<'v> for Either<TLeft, TRight>
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
