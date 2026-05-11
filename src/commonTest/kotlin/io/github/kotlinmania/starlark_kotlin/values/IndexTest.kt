<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/IndexTest.kt
// port-lint: source tests:src/values/index.rs
package io.github.kotlinmania.starlark.values
=======
// port-lint: tests src/values/index.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/IndexTest.kt

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

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.int.InlineInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IndexTest {
    @Test
    fun testConvertIndex() {
        // convert_index(Value::testing_new_int(6), 7) == Ok(6)
        assertEquals(6, convertIndex(Value.testingNewInt(6), 7).getOrThrow())

        // convert_index(Value::testing_new_int(-1), 7) == Ok(6)
        assertEquals(6, convertIndex(Value.testingNewInt(-1), 7).getOrThrow())

        // convert_slice_indices(7, Some(6), None, None) == Ok((6, 7, 1))
        assertEquals(
            Triple(6, 7, 1),
            convertSliceIndices(7, Value.testingNewInt(6), null, null).getOrThrow(),
        )

        // convert_slice_indices(7, Some(-1), None, Some(-1)) == Ok((6, -1, -1))
        assertEquals(
            Triple(6, -1, -1),
            convertSliceIndices(
                7,
                Value.testingNewInt(-1),
                null,
                Value.testingNewInt(-1),
            ).getOrThrow(),
        )

        // convert_slice_indices(7, Some(-1), Some(10), None) == Ok((6, 7, 1))
        assertEquals(
            Triple(6, 7, 1),
            convertSliceIndices(
                7,
                Value.testingNewInt(-1),
                Value.testingNewInt(10),
                null,
            ).getOrThrow(),
        )

        // Errors
        // convert_index(Value::testing_new_int(8), 7).is_err() — 8 >= 7 = len
        assertTrue(convertIndex(Value.testingNewInt(8), 7).isFailure)

        // convert_index(Value::testing_new_int(-8), 7).is_err() — -8 + 7 = -1 < 0
        assertTrue(convertIndex(Value.testingNewInt(-8), 7).isFailure)
    }

    @Test
    fun testApplySlice() {
        val s = listOf(0, 1, 2, 3, 4, 5, 6)

        // apply_slice(s, Some(-1), None, Some(-1)) == [6, 5, 4, 3, 2, 1, 0]
        assertEquals(
            listOf(6, 5, 4, 3, 2, 1, 0),
            applySlice(
                s,
                Value.newInt(InlineInt.MINUS_ONE),
                null,
                Value.newInt(InlineInt.MINUS_ONE),
            ).getOrThrow(),
        )

        // apply_slice(s, Some(0), Some(3), Some(2)) == [0, 2]
        assertEquals(
            listOf(0, 2),
            applySlice(
                s,
                Value.testingNewInt(0),
                Value.testingNewInt(3),
                Value.testingNewInt(2),
            ).getOrThrow(),
        )

        // apply_slice(s, Some(5), Some(2), Some(-2)) == [5, 3]
        assertEquals(
            listOf(5, 3),
            applySlice(
                s,
                Value.testingNewInt(5),
                Value.testingNewInt(2),
                Value.testingNewInt(-2),
            ).getOrThrow(),
        )

        // apply_slice(s, Some(-1), Some(-5), Some(-1)) == [6, 5, 4, 3]
        assertEquals(
            listOf(6, 5, 4, 3),
            applySlice(
                s,
                Value.testingNewInt(-1),
                Value.testingNewInt(-5),
                Value.testingNewInt(-1),
            ).getOrThrow(),
        )

        // apply_slice(s, Some(-1), Some(0), Some(-1)) == [6, 5, 4, 3, 2, 1]
        assertEquals(
            listOf(6, 5, 4, 3, 2, 1),
            applySlice(
                s,
                Value.testingNewInt(-1),
                Value.testingNewInt(0),
                Value.testingNewInt(-1),
            ).getOrThrow(),
        )

        // apply_slice([1, 2, 3], Some(0), Some(-2), Some(-1)) == []
        assertEquals(
            emptyList(),
            applySlice(
                listOf(1, 2, 3),
                Value.testingNewInt(0),
                Value.testingNewInt(-2),
                Value.testingNewInt(-1),
            ).getOrThrow(),
        )
    }
}
