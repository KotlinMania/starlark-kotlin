// port-lint: tests src/values/types/list/unpack.rs
package io.github.kotlinmania.starlark.values.types.list

/*
 * Copyright 2019 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import io.github.kotlinmania.starlark.values.types.int.InlineIntUnpackValue
import io.github.kotlinmania.starlark.values.types.string.allocValue
import io.github.kotlinmania.starlark.values.types.string.unpackValueImplOwnedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private object TestStringUnpackValue : UnpackValue<String> {
    override fun starlarkTypeRepr(): Ty = Ty.string()

    override fun unpackValueImpl(value: Value): Result<String?> = unpackValueImplOwnedString(value)
}

class UnpackTest {
    @Test
    fun testUnpack() {
        Heap.temp { heap ->
            val v =
                heap.allocList(
                    listOf(
                        "a".allocValue(heap),
                        "b".allocValue(heap),
                    ),
                )
            assertEquals(
                listOf("a", "b"),
                UnpackListUnpackValue(TestStringUnpackValue).unpackValue(v).getOrThrow()!!.items,
            )
            assertNull(UnpackListUnpackValue(InlineIntUnpackValue).unpackValue(v).getOrThrow())
            assertNull(UnpackListUnpackValue(TestStringUnpackValue).unpackValue(1.allocValue(heap)).getOrThrow())
        }
    }
}
