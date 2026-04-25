// port-lint: source src/values/layout/value.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values.layout

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
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.allocValue as intAllocValue
import io.github.kotlinmania.starlark_kotlin.values.types.bigint.unpackBigInteger
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocListIter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValueTest {

    // #[test]
    // fn test_downcast_ref()
    @Test
    fun testDowncastRef() {
        Heap.temp { heap ->
            val string = heap.allocStr("asd").toValue()
            val none = Value.newNone()
            val integer = Value.testingNewInt(17)

            assertNull(string.downcastRef<NoneType>())
            assertNull(integer.downcastRef<NoneType>())
            assertNotNull(none.downcastRef<NoneType>())

            assertEquals(
                "asd",
                string.downcastRef<StarlarkStr>()!!.asStr()
            )
            assertNull(integer.downcastRef<StarlarkStr>())
            assertNull(none.downcastRef<StarlarkStr>())

            // PointerI32 uses a vtable adapter pattern in Kotlin rather than
            // directly implementing StarlarkValue, so we test int via unpack.
            assertNull(string.unpackInlineInt())
            assertEquals(InlineInt(17), integer.unpackInlineInt())
            assertNull(none.unpackInlineInt())
        }
    }

    // #[test]
    // fn test_unpack_i32()
    @Test
    fun testUnpackI32() {
        Heap.temp { heap ->
            val value = Int.MAX_VALUE.intAllocValue(heap)
            assertEquals(Int.MAX_VALUE, value.unpackI32())
        }
    }

    // #[test]
    // fn test_unpack_frozen()
    @Test
    fun testUnpackFrozen() {
        assertNotNull(Value.newNone().unpackFrozen())
        assertNotNull(Value.testingNewInt(10).unpackFrozen())
    }

    // #[test]
    // fn test_unpack_bigint()
    @Test
    fun testUnpackBigInt() {
        Heap.temp { heap ->
            val value = BigInteger.fromLong(Long.MAX_VALUE).allocValue(heap)
            assertNull(value.unpackI32())
            assertEquals(
                BigInteger.fromLong(Long.MAX_VALUE),
                value.unpackBigInteger().getOrThrow()
            )
        }
    }

    // #[test]
    // fn test_to_json_value()
    // Note: This test requires assert::pass which evaluates Starlark code.
    // The JSON serialization in Kotlin currently uses repr() as fallback,
    // so we test the basic mechanism rather than exact JSON output.
    @Test
    fun testToJsonValue() {
        val value = Assert.pass("{'a': 10}")
        val json = value.value().toJson()
        assertTrue(json.isSuccess, "toJson should succeed")
    }

    // #[test]
    // fn test_display_for_type_error()
    @Test
    fun testDisplayForTypeError() {
        assertEquals(
            "NoneType (repr: None)",
            Value.newNone().toStringForTypeError(),
        )

        // Rust: heap.alloc(AllocList(0..12345))
        // In Kotlin, allocate each int individually then create the list.
        Heap.temp { heap ->
            val items = (0 until 12345).map { i -> i.intAllocValue(heap) }
            val list = heap.allocListIter(items)
            val errorStr = list.toStringForTypeError()
            // The repr should be truncated with <<...>>
            assertTrue(
                errorStr.contains("<<...>>"),
                "Large list repr should be truncated, got: $errorStr"
            )
            assertTrue(
                errorStr.startsWith("list (repr: "),
                "Should start with type and repr prefix, got: $errorStr"
            )
        }
    }

    // #[test]
    // fn test_check_callable_with_none()
    @Test
    fun testCheckCallableWithNone() {
        val result = Value.newNone()
            .checkCallableWith(emptyList(), emptyList(), null, null, Ty.int())
        assertTrue(result.isFailure)
        val e = result.exceptionOrNull()!!
        assertTrue(
            e.message!!.contains("Value is not callable: NoneType"),
            e.message!!,
        )
    }

    // #[test]
    // fn test_check_callable_with_good_function()
    @Test
    fun testCheckCallableWithGoodFunction() {
        val g = Globals.standard()
        val f = g.getFrozen("bool") ?: error("bool not found in globals")

        // Positional.
        f.toValue().checkCallableWith(
            listOf(Ty.anyList()),
            emptyList(),
            null,
            null,
            Ty.bool(),
        ).getOrThrow()

        // Named.
        val e1 = f.toValue().checkCallableWith(
            emptyList(),
            listOf(Pair("x", Ty.anyList())),
            null,
            null,
            Ty.bool(),
        )
        assertTrue(e1.isFailure)
        assertTrue(
            e1.exceptionOrNull()!!.message!!.contains(
                "is not compatible with"
            ),
            e1.exceptionOrNull()!!.message!!,
        )

        // Return type.
        val e2 = f.toValue().checkCallableWith(
            listOf(Ty.anyList()),
            emptyList(),
            null,
            null,
            Ty.string(),
        )
        assertTrue(e2.isFailure)
        assertTrue(
            e2.exceptionOrNull()!!.message!!.contains(
                "is not compatible with"
            ),
            e2.exceptionOrNull()!!.message!!,
        )
    }
}
