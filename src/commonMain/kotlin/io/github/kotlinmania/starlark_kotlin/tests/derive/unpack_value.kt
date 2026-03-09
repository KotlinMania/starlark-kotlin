// port-lint: source tests/derive/unpack_value.rs
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

import io.github.kotlinmania.starlark_kotlin.values.EitherTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StringTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.constFrozenString
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.IntTypeReprCanonical
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum EmptyEnum {}
internal sealed class EmptyEnum : StarlarkTypeRepr, UnpackValue<EmptyEnum> {
    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty = StarlarkNever.starlarkTypeRepr()

        fun unpackValue(value: Value): Result<EmptyEnum?> {
            return Result.success(null)
        }
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun unpackValue(value: Value): Result<EmptyEnum?> = Companion.unpackValue(value)
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum JustInt { Int(i32) }
internal sealed class JustInt : StarlarkTypeRepr, UnpackValue<JustInt> {
    data class Int(val value: kotlin.Int) : JustInt()

    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty = IntTypeReprCanonical.starlarkTypeRepr()

        fun unpackValue(value: Value): Result<JustInt?> {
            val i = value.unpackInt()
            if (i != null) return Result.success(Int(i))
            return Result.success(null)
        }
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun unpackValue(value: Value): Result<JustInt?> = Companion.unpackValue(value)
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum IntOrStr { Int(i32), Str(String) }
internal sealed class IntOrStr : StarlarkTypeRepr, UnpackValue<IntOrStr> {
    data class Int(val value: kotlin.Int) : IntOrStr()
    data class Str(val value: String) : IntOrStr()

    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty =
            EitherTypeRepr(IntTypeReprCanonical, StringTypeRepr).starlarkTypeRepr()

        fun unpackValue(value: Value): Result<IntOrStr?> {
            val i = value.unpackInt()
            if (i != null) return Result.success(Int(i))
            val s = value.unpackStr()
            if (s != null) return Result.success(Str(s))
            return Result.success(null)
        }
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun unpackValue(value: Value): Result<IntOrStr?> = Companion.unpackValue(value)
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// enum WithLifetime<'v> { Int(i32), Str(&'v str) }
internal sealed class WithLifetime : StarlarkTypeRepr, UnpackValue<WithLifetime> {
    data class Int(val value: kotlin.Int) : WithLifetime()
    data class Str(val value: String) : WithLifetime()

    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty =
            EitherTypeRepr(IntTypeReprCanonical, StringTypeRepr).starlarkTypeRepr()

        fun unpackValue(value: Value): Result<WithLifetime?> {
            val i = value.unpackInt()
            if (i != null) return Result.success(Int(i))
            val s = value.unpackStr()
            if (s != null) return Result.success(Str(s))
            return Result.success(null)
        }
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun unpackValue(value: Value): Result<WithLifetime?> = Companion.unpackValue(value)
}

// #[derive(StarlarkTypeRepr, UnpackValue, Eq, PartialEq, Debug)]
// struct TransparentIntOrStr(IntOrStr);
internal data class TransparentIntOrStr(
    val inner: IntOrStr,
) : StarlarkTypeRepr, UnpackValue<TransparentIntOrStr> {
    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty = IntOrStr.starlarkTypeRepr()

        fun unpackValue(value: Value): Result<TransparentIntOrStr?> {
            val inner = IntOrStr.unpackValue(value).getOrElse { return Result.failure(it) }
                ?: return Result.success(null)
            return Result.success(TransparentIntOrStr(inner))
        }
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun unpackValue(value: Value): Result<TransparentIntOrStr?> =
        Companion.unpackValue(value)
}

// #[test]
// fn test_starlark_type_repr()
internal fun testStarlarkTypeRepr() {
    check(StarlarkNever.starlarkTypeRepr() == EmptyEnum.starlarkTypeRepr())

    check(IntTypeReprCanonical.starlarkTypeRepr() == JustInt.starlarkTypeRepr())

    check(
        EitherTypeRepr(IntTypeReprCanonical, StringTypeRepr).starlarkTypeRepr()
            == IntOrStr.starlarkTypeRepr()
    )

    check(
        EitherTypeRepr(IntTypeReprCanonical, StringTypeRepr).starlarkTypeRepr()
            == WithLifetime.starlarkTypeRepr()
    )

    check(IntOrStr.starlarkTypeRepr() == TransparentIntOrStr.starlarkTypeRepr())
}

// #[test]
// fn test_unpack_value()
internal fun testUnpackValue() {
    check(
        JustInt.unpackValue(Value.testingNewInt(17)).getOrThrow()
            == JustInt.Int(17)
    )

    check(
        IntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow()
            == IntOrStr.Int(19)
    )
    check(
        IntOrStr.unpackValue(constFrozenString("abc").toValue()).getOrThrow()
            == IntOrStr.Str("abc")
    )

    check(
        WithLifetime.unpackValue(Value.testingNewInt(23)).getOrThrow()
            == WithLifetime.Int(23)
    )

    check(
        WithLifetime.unpackValue(constFrozenString("def").toValue()).getOrThrow()
            == WithLifetime.Str("def")
    )

    check(
        TransparentIntOrStr.unpackValue(Value.testingNewInt(19)).getOrThrow()
            == TransparentIntOrStr(IntOrStr.Int(19))
    )
}
