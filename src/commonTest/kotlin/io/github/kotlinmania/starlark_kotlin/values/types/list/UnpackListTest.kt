// port-lint: tests src/values/types/list/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineIntUnpackValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.allocValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.unpackValueImplOwnedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private object StringUnpackValue : UnpackValue<String> {
    override fun starlarkTypeRepr(): Ty = Ty.string()

    override fun unpackValueImpl(value: Value): Result<String?> = unpackValueImplOwnedString(value)
}

internal class UnpackListTest {
    @Test
    fun testUnpack() {
        Heap.temp { heap ->
            val list = heap.allocList(
                listOf(
                    "a".allocValue(heap),
                    "b".allocValue(heap),
                ),
            )

            val unpackedResult: Result<UnpackList<String>?> =
                UnpackListUnpackValue<String>(StringUnpackValue).unpackValue(list)
            val unpacked: UnpackList<String>? = unpackedResult.getOrThrow()
            assertEquals(listOf("a", "b"), unpacked?.items?.toList())

            val wrongTypeResult: Result<UnpackList<InlineInt>?> =
                UnpackListUnpackValue<InlineInt>(InlineIntUnpackValue).unpackValue(list)
            val wrongType: UnpackList<InlineInt>? = wrongTypeResult.getOrThrow()
            assertNull(wrongType)

            val nonListResult: Result<UnpackList<String>?> =
                UnpackListUnpackValue<String>(StringUnpackValue).unpackValue(1.allocValue(heap))
            val nonList: UnpackList<String>? = nonListResult.getOrThrow()
            assertNull(nonList)
        }
    }
}
