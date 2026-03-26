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

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.constFrozenString
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum EmptyEnum {}
sealed class EmptyEnum

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum JustInt { Int(i32) }
sealed class JustInt
data class JustIntVariantInt(val value: Int) : JustInt()

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum IntOrStr { Int(i32), Str(String) }
sealed class IntOrStr
data class IntOrStrVariantInt(val value: Int) : IntOrStr()
data class IntOrStrVariantStr(val value: String) : IntOrStr()

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum WithLifetime<'v> { Int(i32), Str(&'v str) }
sealed class WithLifetime
data class WithLifetimeVariantInt(val value: Int) : WithLifetime()
data class WithLifetimeVariantStr(val value: String) : WithLifetime()

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// struct TransparentIntOrStr(IntOrStr)
data class TransparentIntOrStr(val inner: IntOrStr)

// #[test]
fun testStarlarkTypeRepr() {
    assert(StarlarkNever.starlarkTypeRepr() == EmptyEnum.starlarkTypeRepr())
    assert(kotlin.Int.starlarkTypeRepr() == JustInt.starlarkTypeRepr())
    assert(Either.starlarkTypeRepr<kotlin.Int, String>() == IntOrStr.starlarkTypeRepr())
    assert(Either.starlarkTypeRepr<kotlin.Int, String>() == WithLifetime.starlarkTypeRepr())
    assert(IntOrStr.starlarkTypeRepr() == TransparentIntOrStr.starlarkTypeRepr())
}

// #[test]
fun testUnpackValue() {
    assert(JustInt.unpackValue(Value.testingNewInt(17)).getOrThrow() == JustIntVariantInt(17))
    assert(IntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow() == IntOrStrVariantInt(19))
    assert(IntOrStr.unpackValue(constFrozenString("abc").toValue()).getOrThrow() == IntOrStrVariantStr("abc"))
    assert(WithLifetime.unpackValue(Value.testingNewInt(23)).getOrThrow() == WithLifetimeVariantInt(23))
    assert(WithLifetime.unpackValue(constFrozenString("def").toValue()).getOrThrow() == WithLifetimeVariantStr("def"))
    assert(TransparentIntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow() == TransparentIntOrStr(IntOrStrVariantInt(19)))
}
