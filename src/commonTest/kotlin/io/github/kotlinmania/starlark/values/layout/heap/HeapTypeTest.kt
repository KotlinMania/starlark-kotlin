// port-lint: source tests:src/values/layout/heap/heap_type.rs
package io.github.kotlinmania.starlark.values.layout.heap

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
import io.github.kotlinmania.starlark.values.layout.StringValue
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HeapTypeTest {
    @Test
    fun testSendSync() {
        // Mirrors upstream `fn test_send_sync() where FrozenHeapRef: Send + Sync {}`.
        // The companion runtime channel guard `testFrozenHeapRefSendSync` lives in
        // commonMain. This anchor exists for the Rust mod-tests function name.
        testFrozenHeapRefSendSync()
    }

    @Test
    fun testStringReallocatedOnHeap() {
        Heap.temp { heap ->
            val first = heap.allocStr("xx")
            val second = heap.allocStr("xx")
            assertFalse(
                first.toValue().ptrEq(second.toValue()),
                "Plain allocations should recreate values. Note assertion negation.",
            )
        }
    }

    @Test
    fun testInternedStringEqual() {
        Heap.temp { heap ->
            val first = heap.allocStrIntern("xx")
            val second = heap.allocStrIntern("xx")
            assertTrue(
                first.toValue().ptrEq(second.toValue()),
                "Interned allocations should be equal.",
            )
        }
    }

    private fun validateStrInterning(globals: GlobalsBuilder) {
        fun appendX(str: StringValue, heap: Heap): Result<StringValue> = Result.success(heap.allocStrIntern(str.asStr() + "x"))

        globals.setFunction("append_x") { args, eval ->
            val str = args.positional<StringValue>(0)
            appendX(str, eval.heap())
        }
    }

    @Test
    fun testInternedStrStarlark() {
        val a = Assert()
        a.globalsAdd(::validateStrInterning)

        a.pass(
            """
x = append_x("foo")
assert_eq(x, "foox")
garbage_collect()
assert_eq(x, "foox")
        """,
        )
    }
}
