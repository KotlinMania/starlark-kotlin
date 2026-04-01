
// port-lint: source src/tests/derive/unpack_value.rs
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
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.toValue
import kotlin.test.assertEquals

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
    assertEquals(StarlarkNever.starlarkTypeRepr(), EmptyEnum.starlarkTypeRepr())
    assertEquals(Ty.int(), JustInt.starlarkTypeRepr())
    assertEquals(Ty.union2(Ty.int(), Ty.string()), IntOrStr.starlarkTypeRepr())
    assertEquals(Ty.union2(Ty.int(), Ty.string()), WithLifetime.starlarkTypeRepr())
    assertEquals(IntOrStr.starlarkTypeRepr(), TransparentIntOrStr.starlarkTypeRepr())
}

// #[test]
internal fun testUnpackValue() {
    val r1 = JustInt.unpackValue(Value.testingNewInt(17)).getOrThrow()
    val r2 = IntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow()
    val r3 = IntOrStr.unpackValue(constFrozenString("abc").toValue()).getOrThrow()
    val r4 = WithLifetime.unpackValue(Value.testingNewInt(23)).getOrThrow()
    val r5 = WithLifetime.unpackValue(constFrozenString("def").toValue()).getOrThrow()
    val r6 = TransparentIntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow()
    assertEquals(JustInt.Int(17), r1)
    assertEquals(IntOrStr.Int(19), r2)
    assertEquals(IntOrStr.Str("abc"), r3)
    assertEquals(WithLifetime.Int(23), r4)
    assertEquals(WithLifetime.Str("def"), r5)
    assertEquals(TransparentIntOrStr(IntOrStr.Int(19)), r6)
}
