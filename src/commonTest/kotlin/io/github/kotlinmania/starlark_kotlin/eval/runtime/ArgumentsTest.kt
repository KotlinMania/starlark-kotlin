// port-lint: tests src/eval/runtime/arguments.rs (tests)
package io.github.kotlinmania.starlark_kotlin.eval.runtime

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

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
import io.github.kotlinmania.starlark_kotlin.values.types.dict.allocValue
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun testingNewInt(x: Int): Value = Value.testingNewInt(x)

private fun stringValue(heap: Heap, value: String): StringValue {
    return StringValue.new(heap.allocStr(value)) ?: error("Expected string value")
}

private fun parameterUnpackCase(
    heap: Heap,
    totalCount: Int,
    op: (Arguments) -> Unit,
) {
    for (i in 0..totalCount) {
        val p = Arguments.default()
        val pos = (0 until i).map(::testingNewInt)
        val args = (i until totalCount).map(::testingNewInt)
        val emptyArgs = args.isEmpty()
        p.full.pos = pos
        p.full.args = heap.allocListIter(args)
        op(p)
        if (emptyArgs) {
            p.full.args = null
            op(p)
        }
        assertEquals(totalCount, p.len().getOrThrow())
    }
}

class ArgumentsTest {
    @Test
    fun testParameterUnpack() {
        Heap.temp { heap ->
            parameterUnpackCase(heap = heap, totalCount = 0, op = { p ->
                assertEquals(emptyList<Value>(), p.positional(0, heap).getOrThrow())
                assertTrue(p.positional(1, heap).isFailure)
                assertTrue(p.positional(2, heap).isFailure)
                assertEquals(
                    Pair(emptyList<Value>(), listOf<Value?>(null)),
                    p.optional(0, 1, heap).getOrThrow(),
                )
                assertTrue(p.optional(1, 1, heap).isFailure)
                assertEquals(
                    Pair(emptyList<Value>(), listOf<Value?>(null, null)),
                    p.optional(0, 2, heap).getOrThrow(),
                )
            })
            parameterUnpackCase(heap = heap, totalCount = 1, op = { p ->
                assertTrue(p.positional(0, heap).isFailure)
                assertEquals(listOf(testingNewInt(0)), p.positional(1, heap).getOrThrow())
                assertTrue(p.positional(2, heap).isFailure)
                assertEquals(
                    Pair(emptyList<Value>(), listOf<Value?>(testingNewInt(0))),
                    p.optional(0, 1, heap).getOrThrow(),
                )
                assertEquals(
                    Pair(listOf(testingNewInt(0)), listOf<Value?>(null)),
                    p.optional(1, 1, heap).getOrThrow(),
                )
                assertEquals(
                    Pair(emptyList<Value>(), listOf<Value?>(testingNewInt(0), null)),
                    p.optional(0, 2, heap).getOrThrow(),
                )
            })
            parameterUnpackCase(heap = heap, totalCount = 2, op = { p ->
                assertTrue(p.positional(0, heap).isFailure)
                assertTrue(p.positional(1, heap).isFailure)
                assertEquals(
                    listOf(testingNewInt(0), testingNewInt(1)),
                    p.positional(2, heap).getOrThrow(),
                )
                assertTrue(p.optional(0, 1, heap).isFailure)
                assertEquals(
                    Pair(
                        listOf(testingNewInt(0)),
                        listOf<Value?>(testingNewInt(1)),
                    ),
                    p.optional(1, 1, heap).getOrThrow(),
                )
                assertEquals(
                    Pair(
                        emptyList<Value>(),
                        listOf<Value?>(testingNewInt(0), testingNewInt(1)),
                    ),
                    p.optional(0, 2, heap).getOrThrow(),
                )
            })
            parameterUnpackCase(heap = heap, totalCount = 3, op = { p ->
                assertTrue(p.positional(0, heap).isFailure)
                assertTrue(p.positional(1, heap).isFailure)
                assertTrue(p.positional(2, heap).isFailure)
                assertTrue(p.optional(0, 1, heap).isFailure)
                assertTrue(p.optional(1, 1, heap).isFailure)
                assertTrue(p.optional(0, 2, heap).isFailure)
            })
        }
    }

    @Test
    fun testParameterNoNamed() {
        Heap.temp { heap ->
            val p = Arguments.default()
            assertTrue(p.noNamedArgs().isSuccess)
            assertEquals(0, p.len().getOrThrow())

            p.full.kwargs = Value.newNone()
            assertTrue(p.noNamedArgs().isFailure)

            p.full.kwargs = Dict.new(SmallMap.new<Value, Value>()).allocValue(heap)
            assertTrue(p.noNamedArgs().isSuccess)
            assertEquals(0, p.len().getOrThrow())

            val sm = SmallMap.new<Value, Value>()
            val test = stringValue(heap, "test")
            sm.insertHashed(test.getHashedValue(), Value.newNone())
            p.full.kwargs = Dict.new(sm).allocValue(heap)
            assertTrue(p.noNamedArgs().isFailure)
            assertEquals(1, p.len().getOrThrow())

            p.full.kwargs = null
            p.full.named = listOf(Value.newNone())
            p.full.names = ArgNames.newCheckUnique(listOf(Symbol.new("test") to stringValue(heap, "test"))).getOrThrow()
            assertTrue(p.noNamedArgs().isFailure)
            assertEquals(1, p.len().getOrThrow())
        }
    }

    @Test
    fun testNamesMapRepeatedNameInArgNames() {
        Heap.temp { heap ->
            val a = stringValue(heap, "a")
            val names = listOf(
                Symbol.new("a") to a,
                Symbol.new("a") to a,
            )
            assertTrue(ArgNames.newCheckUnique(names).isFailure)
        }
    }
}
