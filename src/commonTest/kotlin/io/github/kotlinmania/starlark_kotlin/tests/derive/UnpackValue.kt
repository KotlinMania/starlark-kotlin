// port-lint: tests tests/derive/unpack_value.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive

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

import io.github.kotlinmania.starlark_kotlin.values.layout.constFrozenString
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.EitherTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StringTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.toValue
import kotlin.test.assertEquals

private fun <T> Some(value: T): T? = value

private fun <T> Result<T>.unwrap(): T = getOrThrow()

private fun <T> assert_eq(expected: T, actual: T) {
    assertEquals(expected, actual)
}

private fun String.to_owned(): String = this

private fun const_frozen_string(value: String) = constFrozenString(value)

private fun Value.Companion.testing_new_int(value: Int): Value = Value.testingNewInt(value)

private fun StarlarkTypeRepr.starlark_type_repr(): Ty = starlarkTypeRepr()

private fun <T> UnpackValue<T>.unpack_value(value: Value): Result<T?> = unpackValue(value)

private object i32 : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.int()
}

private fun Either_starlark_type_repr(): Ty =
    EitherTypeRepr(i32, StringTypeRepr).starlark_type_repr()

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
private sealed class EmptyEnum {
    companion object : UnpackValue<EmptyEnum> {
        override fun starlarkTypeRepr(): Ty = StarlarkNever.starlarkTypeRepr()
        override fun unpackValueImpl(value: Value): Result<EmptyEnum?> = Result.success(null)
    }
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
private sealed class JustInt {
    data class Int(val value: kotlin.Int) : JustInt()

    companion object : UnpackValue<JustInt> {
        override fun starlarkTypeRepr(): Ty = Ty.int()
        override fun unpackValueImpl(value: Value): Result<JustInt?> =
            Result.success(value.unpackI32()?.let { JustInt.Int(it) })
    }
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
private sealed class IntOrStr {
    data class Int(val value: kotlin.Int) : IntOrStr()
    data class Str(val value: kotlin.String) : IntOrStr()

    companion object : UnpackValue<IntOrStr> {
        override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.string())
        override fun unpackValueImpl(value: Value): Result<IntOrStr?> =
            Result.success(value.unpackI32()?.let { IntOrStr.Int(it) } ?: value.unpackStr()?.let { IntOrStr.Str(it) })
    }
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
private sealed class WithLifetime {
    data class Int(val value: kotlin.Int) : WithLifetime()
    data class Str(val value: kotlin.String) : WithLifetime()

    companion object : UnpackValue<WithLifetime> {
        override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.string())
        override fun unpackValueImpl(value: Value): Result<WithLifetime?> =
            Result.success(value.unpackI32()?.let { WithLifetime.Int(it) } ?: value.unpackStr()?.let { WithLifetime.Str(it) })
    }
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
private data class TransparentIntOrStr(val value: IntOrStr) {
    companion object : UnpackValue<TransparentIntOrStr> {
        override fun starlarkTypeRepr(): Ty = IntOrStr.starlarkTypeRepr()
        override fun unpackValueImpl(value: Value): Result<TransparentIntOrStr?> =
            IntOrStr.unpackValue(value).map { it?.let(::TransparentIntOrStr) }
    }
}

// #[test]
internal fun testStarlarkTypeRepr() {
    assert_eq(
        StarlarkNever.starlark_type_repr(),
        EmptyEnum.starlark_type_repr(),
    )

    assert_eq(
        i32.starlark_type_repr(),
        JustInt.starlark_type_repr(),
    )

    assert_eq(
        Either_starlark_type_repr(),
        IntOrStr.starlark_type_repr(),
    )

    assert_eq(
        Either_starlark_type_repr(),
        WithLifetime.starlark_type_repr(),
    )

    assert_eq(
        IntOrStr.starlark_type_repr(),
        TransparentIntOrStr.starlark_type_repr(),
    )
}

// #[test]
internal fun testUnpackValue() {
    assert_eq(
        Some(JustInt.Int(17)),
        JustInt.unpack_value(Value.testing_new_int(17)).unwrap(),
    )

    assert_eq(
        Some(IntOrStr.Int(19)),
        IntOrStr.unpack_value(Value.testing_new_int(19)).unwrap(),
    )
    assert_eq(
        Some(IntOrStr.Str("abc".to_owned())),
        IntOrStr.unpack_value(const_frozen_string("abc").toValue()).unwrap(),
    )

    assert_eq(
        Some(WithLifetime.Int(23)),
        WithLifetime.unpack_value(Value.testing_new_int(23)).unwrap(),
    )

    assert_eq(
        Some(WithLifetime.Str("def")),
        WithLifetime.unpack_value(const_frozen_string("def").toValue()).unwrap(),
    )

    assert_eq(
        Some(TransparentIntOrStr(IntOrStr.Int(19))),
        TransparentIntOrStr.unpack_value(Value.testing_new_int(19)).unwrap(),
    )
}
