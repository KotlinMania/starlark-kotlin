// port-lint: tests src/values/types/bigint/convert.rs
package io.github.kotlinmania.starlark.values.types.bigint

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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.values.types.int.I32UnpackValue
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test

class ConvertTest {
    @Test
    fun testUnpackIntError() {
        fun module(globals: GlobalsBuilder) {
            fun takesI32(i: Int): Result<NoneType> {
                // is consumed only for type-binding by the unpacker.
                i.toLong()
                return Result.success(NoneType)
            }

            fun takesI64(i: Long): Result<NoneType> {
                i.toString()
                return Result.success(NoneType)
            }

            globals.setFunction("takes_i32") { args, _ ->
                takesI32(I32UnpackValue.unpackNamedParam(args.positionalAll()[0], "_i"))
            }

            globals.setFunction("takes_i64") { args, _ ->
                takesI64(I64UnpackValue.unpackNamedParam(args.positionalAll()[0], "_i"))
            }
        }

        val a = Assert()
        a.globalsAdd(::module)
        a.fails(
            "takes_i32(1 << 100)",
            listOf(
                "Integer value is too big to fit in i32: 1267650600228229401496703205376",
                "Error unpacking value for parameter `_i`",
            ),
        )
        a.fails(
            "takes_i64(1 << 100)",
            listOf(
                "Integer value is too big to fit in i64: 1267650600228229401496703205376",
                "Error unpacking value for parameter `_i`",
            ),
        )
    }
}
