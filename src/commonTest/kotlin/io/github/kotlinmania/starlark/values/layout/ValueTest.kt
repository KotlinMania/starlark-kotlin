// port-lint: source src/values/layout/value.rs (tests)
package io.github.kotlinmania.starlark.values.layout

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.kotlinmania.starlark.assert.pass
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.StarlarkBigInt
import io.github.kotlinmania.starlark.values.types.dict.DictGen
import io.github.kotlinmania.starlark.values.types.int.InlineInt
import io.github.kotlinmania.starlark.values.types.int.PointerI32
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.list.AllocList
import io.github.kotlinmania.starlark.values.types.list.allocValue
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.values.types.string.StarlarkStr
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValueTest {

    @Test
    fun testDowncastRef() {
        Heap.temp { heap ->
            val string = heap.allocStr("asd").toValue()
            val none = Value.newNone()
            val integer = Value.testingNewInt(17)

            assertNull(string.downcastRef<NoneType>())
            assertNull(integer.downcastRef<NoneType>())
            assertNotNull(none.downcastRef<NoneType>())

            assertEquals("asd", string.downcastRef<StarlarkStr>()!!.asStr())
            assertNull(integer.downcastRef<StarlarkStr>())
            assertNull(none.downcastRef<StarlarkStr>())

            assertNull(string.downcastRef<PointerI32>())
            assertEquals(17, integer.downcastRef<PointerI32>()!!.get().toI32())
            assertNull(none.downcastRef<PointerI32>())
        }
    }

    @Test
    fun testUnpackI32() {
        Heap.temp { heap ->
            val value = heap.alloc(InlineInt.testingNew(Int.MAX_VALUE))
            assertEquals(Int.MAX_VALUE, value.unpackI32())
        }
    }

    @Test
    fun testUnpackFrozen() {
        assertNotNull(Value.newNone().unpackFrozen())
        assertNotNull(Value.testingNewInt(10).unpackFrozen())
    }

    @Test
    fun testUnpackBigint() {
        Heap.temp { heap ->
            val big = StarlarkInt.from(BigInteger.fromLong(Long.MAX_VALUE))
            val value = when (big) {
                is StarlarkInt.Small -> heap.alloc(big.value)
                is StarlarkInt.Big -> heap.allocSimple(big.value)
            }
            assertNull(value.unpackI32())
            assertEquals(BigInteger.fromLong(Long.MAX_VALUE), StarlarkIntRef.unpackValueOpt(value)!!.toBig())
        }
    }

    @Test
    fun testToJsonValue() {
        val value = pass("{'a': 10}")
        assertEquals(
            JsonObject(mapOf("a" to JsonPrimitive(10))),
            value.value().toJsonValue().getOrThrow(),
        )
    }

    @Test
    fun testDisplayForTypeError() {
        assertEquals(
            "NoneType (repr: None)",
            Value.newNone().toStringForTypeError(),
        )

        Heap.temp { heap ->
            val list = AllocList((0 until 12345).map { InlineInt.testingNew(it) }).allocValue(heap)
            assertEquals(
                "list (repr: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,<<...>>42, 12343, 12344])",
                list.toStringForTypeError(),
            )
        }
    }

    @Test
    fun testCheckCallableWithNone() {
        val e = Value.newNone().checkCallableWith(emptyList(), emptyList(), null, null, Ty.int()).exceptionOrNull()
        assertNotNull(e)
        assertContains(e.toString(), "Value is not callable: NoneType")
    }

    @Test
    fun testCheckCallableWithGoodFunction() {
        val g = Globals.standard()
        val f = g.getOwned("bool")!!.value()

        // Positional.
        f.checkCallableWith(listOf(Ty.anyList()), emptyList(), null, null, Ty.bool()).getOrThrow()

        // Named.
        val e1 =
            f.checkCallableWith(emptyList(), listOf(Pair("x", Ty.anyList())), null, null, Ty.bool()).exceptionOrNull()
        assertNotNull(e1)
        assertContains(e1.toString(), "Value `function (repr: bool)` is not compatible with")

        // Return type.
        val e2 = f.checkCallableWith(listOf(Ty.anyList()), emptyList(), null, null, Ty.string()).exceptionOrNull()
        assertNotNull(e2)
        assertContains(e2.toString(), "Value `function (repr: bool)` is not compatible with")
    }
}

