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
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.toValue

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
sealed class EmptyEnum { companion object }
fun EmptyEnum.Companion.starlarkTypeRepr(): Ty = StarlarkNever.starlarkTypeRepr()
fun EmptyEnum.Companion.unpackValue(value: Value): Result<EmptyEnum?> = Result.success(null)

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
sealed class JustInt { companion object }
data class JustIntInt(val value: Int) : JustInt()
fun JustInt.Companion.starlarkTypeRepr(): Ty = Ty.int()
fun JustInt.Companion.unpackValue(value: Value): Result<JustInt?> =
    Result.success(value.unpackI32()?.let(::JustIntInt))

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
sealed class IntOrStr { companion object }
data class IntOrStrInt(val value: Int) : IntOrStr()
data class IntOrStrStr(val value: String) : IntOrStr()
fun IntOrStr.Companion.starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.string())
fun IntOrStr.Companion.unpackValue(value: Value): Result<IntOrStr?> =
    Result.success(value.unpackI32()?.let(::IntOrStrInt) ?: value.unpackStr()?.let(::IntOrStrStr))

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
sealed class WithLifetime { companion object }
data class WithLifetimeInt(val value: Int) : WithLifetime()
data class WithLifetimeStr(val value: String) : WithLifetime()
fun WithLifetime.Companion.starlarkTypeRepr(): Ty = Ty.union2(Ty.int(), Ty.string())
fun WithLifetime.Companion.unpackValue(value: Value): Result<WithLifetime?> =
    Result.success(value.unpackI32()?.let(::WithLifetimeInt) ?: value.unpackStr()?.let(::WithLifetimeStr))

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
data class TransparentIntOrStr(val inner: IntOrStr) { companion object }
fun TransparentIntOrStr.Companion.starlarkTypeRepr(): Ty = IntOrStr.starlarkTypeRepr()
fun TransparentIntOrStr.Companion.unpackValue(value: Value): Result<TransparentIntOrStr?> =
    IntOrStr.unpackValue(value).map { it?.let(::TransparentIntOrStr) }

// #[test]
fun testStarlarkTypeRepr() {
    val never = StarlarkNever.starlarkTypeRepr()
    val empty = EmptyEnum.starlarkTypeRepr()
    val intTy = Ty.int()
    val justIntTy = JustInt.starlarkTypeRepr()
    val union = Ty.union2(Ty.int(), Ty.string())
    val intOrStrTy = IntOrStr.starlarkTypeRepr()
    val withLifetimeTy = WithLifetime.starlarkTypeRepr()
    val transparentTy = TransparentIntOrStr.starlarkTypeRepr()
    assert(never == empty)
    assert(intTy == justIntTy)
    assert(union == intOrStrTy)
    assert(union == withLifetimeTy)
    assert(intOrStrTy == transparentTy)
}

// #[test]
fun testUnpackValue() {
    val r1 = JustInt.unpackValue(Value.testingNewInt(17)).getOrThrow()
    val r2 = IntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow()
    val r3 = IntOrStr.unpackValue(constFrozenString("abc").toValue()).getOrThrow()
    val r4 = WithLifetime.unpackValue(Value.testingNewInt(23)).getOrThrow()
    val r5 = WithLifetime.unpackValue(constFrozenString("def").toValue()).getOrThrow()
    val r6 = TransparentIntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow()
    assert(r1 == JustIntInt(17))
    assert(r2 == IntOrStrInt(19))
    assert(r3 == IntOrStrStr("abc"))
    assert(r4 == WithLifetimeInt(23))
    assert(r5 == WithLifetimeStr("def"))
    assert(r6 == TransparentIntOrStr(IntOrStrInt(19)))
}
