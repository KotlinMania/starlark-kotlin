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

sealed class EmptyEnum

sealed class JustInt { data class Int(val value: kotlin.Int) : JustInt() }

sealed class IntOrStr {
    data class Int(val value: kotlin.Int) : IntOrStr()
    data class Str(val value: String) : IntOrStr()
}

sealed class WithLifetime {
    data class Int(val value: kotlin.Int) : WithLifetime()
    data class Str(val value: String) : WithLifetime()
}

data class TransparentIntOrStr(val inner: IntOrStr)

fun testStarlarkTypeRepr() {
    assert(StarlarkNever.starlarkTypeRepr() == EmptyEnum.starlarkTypeRepr())
    assert(kotlin.Int.starlarkTypeRepr() == JustInt.starlarkTypeRepr())
    assert(Either.starlarkTypeRepr<kotlin.Int, String>() == IntOrStr.starlarkTypeRepr())
    assert(Either.starlarkTypeRepr<kotlin.Int, String>() == WithLifetime.starlarkTypeRepr())
    assert(IntOrStr.starlarkTypeRepr() == TransparentIntOrStr.starlarkTypeRepr())
}

fun testUnpackValue() {
    assert(JustInt.unpackValue(Value.testingNewInt(17)).getOrThrow() == JustInt.Int(17))
    assert(IntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow() == IntOrStr.Int(19))
    assert(IntOrStr.unpackValue(constFrozenString("abc").toValue()).getOrThrow() == IntOrStr.Str("abc"))
    assert(WithLifetime.unpackValue(Value.testingNewInt(23)).getOrThrow() == WithLifetime.Int(23))
    assert(WithLifetime.unpackValue(constFrozenString("def").toValue()).getOrThrow() == WithLifetime.Str("def"))
    assert(TransparentIntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow() == TransparentIntOrStr(IntOrStr.Int(19)))
}
