// port-lint: tests src/values/types/dict/value.rs
package io.github.kotlinmania.starlark.values.types.dict

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
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DictValueTest {
    @Test
    fun testMutateDict() {
        Assert.isTrue(
            """
x = {1: 2, 2: 4}
b1 = str(x) == "{1: 2, 2: 4}"
x[2] = 3
b2 = str(x) == "{1: 2, 2: 3}"
x[(3,4)] = 5
b3 = str(x) == "{1: 2, 2: 3, (3, 4): 5}"
b1 and b2 and b3
""",
        )
    }

    @Test
    fun testGetStr() {
        Heap.temp { heap ->
            val k1 = heap.allocStr("hello").getHashed()
            val k2 = heap.allocStr("world").getHashed()
            val sm = SmallMap.new<Value, Value>()
            sm.insertHashed(k1.getOrThrow(), Value.testingNewInt(12))
            sm.insertHashed(k2.getOrThrow(), Value.testingNewInt(56))
            val d = Dict.new(sm)

            assertEquals(12, d.get(heap.allocStr("hello")).getOrThrow()!!.unpackI32())
            assertNull(d.get(heap.allocStr("foo")).getOrThrow())
            assertEquals(12, d.getStr("hello")!!.unpackI32())
            assertNull(d.getStr("foo"))
        }
    }

    @Test
    fun testReprCycle() {
        Assert.eq("d = {}; d[17] = d; repr(d)", "'{17: {...}}'")
        Assert.eq("d = {}; d[17] = d; str(d)", "'{17: {...}}'")
    }
}
