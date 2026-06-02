// port-lint: tests tests/derive/unpack_value.rs
package io.github.kotlinmania.starlark.tests.derive

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

import io.github.kotlinmania.starlark.values.EitherTypeRepr
import io.github.kotlinmania.starlark.values.StringTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import io.github.kotlinmania.starlark.values.types.int.i32StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.typing.StarlarkNever
import kotlin.test.Test
import kotlin.test.assertEquals

sealed class EmptyEnum {
    companion object
}

sealed class JustInt {
    data class Int(
        val value: kotlin.Int,
    ) : JustInt()

    companion object
}

sealed class IntOrStr {
    data class Int(
        val value: kotlin.Int,
    ) : IntOrStr()

    data class Str(
        val value: String,
    ) : IntOrStr()

    companion object
}

sealed class WithLifetime {
    data class Int(
        val value: kotlin.Int,
    ) : WithLifetime()

    data class Str(
        val value: String,
    ) : WithLifetime()

    companion object
}

data class TransparentIntOrStr(
    val value: IntOrStr,
) {
    companion object
}

@Test
fun testStarlarkTypeRepr() {
    assertEquals(StarlarkNever.starlarkTypeRepr(), EmptyEnum.starlark_type_repr())
    assertEquals(i32StarlarkTypeRepr(), JustInt.starlark_type_repr())

    assertEquals(
        EitherTypeRepr(I32TypeRepr, StringTypeRepr).starlarkTypeRepr(),
        IntOrStr.starlark_type_repr(),
    )

    assertEquals(
        EitherTypeRepr(I32TypeRepr, StringTypeRepr).starlarkTypeRepr(),
        WithLifetime.starlark_type_repr(),
    )

    assertEquals(
        IntOrStr.starlark_type_repr(),
        TransparentIntOrStr.starlark_type_repr(),
    )
}

@Test
fun testUnpackValue() {
    assertEquals(
        JustInt.Int(17),
        JustInt.unpack_value(Value.testingNewInt(17)).getOrThrow(),
    )

    assertEquals(
        IntOrStr.Int(19),
        IntOrStr.unpack_value(Value.testingNewInt(19)).getOrThrow(),
    )
    assertEquals(
        IntOrStr.Str("abc"),
        IntOrStr.unpack_value(constFrozenString("abc").toValue()).getOrThrow(),
    )

    assertEquals(
        WithLifetime.Int(23),
        WithLifetime.unpack_value(Value.testingNewInt(23)).getOrThrow(),
    )

    assertEquals(
        WithLifetime.Str("def"),
        WithLifetime.unpack_value(constFrozenString("def").toValue()).getOrThrow(),
    )

    assertEquals(
        TransparentIntOrStr(IntOrStr.Int(19)),
        TransparentIntOrStr.unpack_value(Value.testingNewInt(19)).getOrThrow(),
    )
}
