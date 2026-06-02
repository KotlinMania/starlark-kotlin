// port-lint: tests src/values/types/tuple/unpack.rs
package io.github.kotlinmania.starlark.values.types.tuple

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

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.string.allocValue
import io.github.kotlinmania.starlark.values.types.tuple.unpack.UnpackTuple
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnpackTest {
    @Test
    fun testUnpack() {
        Heap.temp { heap ->
            val v =
                heap.allocTuple(
                    listOf(
                        "a".allocValue(heap),
                        "b".allocValue(heap),
                    ),
                )
            fun tupleFromValue(value: Any): List<Any>? =
                (value as? Value)?.let { Tuple.fromValue(it)?.content() }

            assertEquals(
                listOf("a", "b"),
                UnpackTuple.unpackValueImpl(v, ::tupleFromValue) { (it as? Value)?.unpackStr() }!!.items,
            )
            assertNull(UnpackTuple.unpackValueImpl(v, ::tupleFromValue) { (it as? Value)?.unpackInlineInt() })
            assertNull(UnpackTuple.unpackValueImpl(1.allocValue(heap), ::tupleFromValue) { (it as? Value)?.unpackStr() })
        }
    }
}
