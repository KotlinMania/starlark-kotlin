// port-lint: source src/tests/derive/unpackValue.rs
package io.github.kotlinmania.starlark.tests.derive

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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlark.values.typing.StarlarkNever
import kotlin.test.Test
import kotlin.test.assertEquals

sealed interface EmptyEnum {
    companion object : UnpackValue<EmptyEnum> {
        override fun starlarkTypeRepr(): Ty = StarlarkNever.starlarkTypeRepr()

        override fun unpackValueImpl(value: Value): Result<EmptyEnum?> = Result.success(null)
    }
}

sealed interface JustInt {
    companion object : UnpackValue<JustInt> {
        override fun starlarkTypeRepr(): Ty = Ty.int()

        override fun unpackValueImpl(value: Value): Result<JustInt?> =
            Result.success(value.unpackI32()?.let(JustInt::Int))
    }

    data class Int(val value: kotlin.Int) : JustInt
}

sealed interface IntOrStr {
    companion object : UnpackValue<IntOrStr> {
        override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.string())

        override fun unpackValueImpl(value: Value): Result<IntOrStr?> =
            Result.success(value.unpackI32()?.let(IntOrStr::Int) ?: value.unpackStr()?.let(IntOrStr::Str))
    }

    data class Int(val value: kotlin.Int) : IntOrStr

    data class Str(val value: String) : IntOrStr
}

sealed interface WithLifetime {
    companion object : UnpackValue<WithLifetime> {
        override fun starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.string())

        override fun unpackValueImpl(value: Value): Result<WithLifetime?> =
            Result.success(value.unpackI32()?.let(WithLifetime::Int) ?: value.unpackStr()?.let(WithLifetime::Str))
    }

    data class Int(val value: kotlin.Int) : WithLifetime

    data class Str(val value: String) : WithLifetime
}

data class TransparentIntOrStr(
    val inner: IntOrStr,
) {
    companion object : UnpackValue<TransparentIntOrStr> {
        override fun starlarkTypeRepr(): Ty = IntOrStr.starlarkTypeRepr()

        override fun unpackValueImpl(value: Value): Result<TransparentIntOrStr?> =
            IntOrStr.unpackValue(value).map { it?.let(::TransparentIntOrStr) }
    }
}

class UnpackValueTests {
    @Test
    fun testStarlarkTypeRepr() {
        assertEquals(
            StarlarkNever.starlarkTypeRepr(),
            EmptyEnum.starlarkTypeRepr(),
        )

        assertEquals(
            Ty.int(),
            JustInt.starlarkTypeRepr(),
        )

        val either = Ty.union2(Ty.int(), Ty.string())
        assertEquals(
            either,
            IntOrStr.starlarkTypeRepr(),
        )

        assertEquals(
            either,
            WithLifetime.starlarkTypeRepr(),
        )

        assertEquals(
            IntOrStr.starlarkTypeRepr(),
            TransparentIntOrStr.starlarkTypeRepr(),
        )
    }

    @Test
    fun testUnpackValue() {
        assertEquals(
            JustInt.Int(17),
            JustInt.unpackValue(Value.testingNewInt(17)).getOrThrow(),
        )

        assertEquals(
            IntOrStr.Int(19),
            IntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow(),
        )
        assertEquals(
            IntOrStr.Str("abc"),
            IntOrStr.unpackValue(constFrozenString("abc").toValue()).getOrThrow(),
        )

        assertEquals(
            WithLifetime.Int(23),
            WithLifetime.unpackValue(Value.testingNewInt(23)).getOrThrow(),
        )

        assertEquals(
            WithLifetime.Str("def"),
            WithLifetime.unpackValue(constFrozenString("def").toValue()).getOrThrow(),
        )

        assertEquals(
            TransparentIntOrStr(IntOrStr.Int(19)),
            TransparentIntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow(),
        )
    }
}
