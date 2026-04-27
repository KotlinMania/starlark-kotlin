// port-lint: source src/tests/derive/module/unpackValue.rs
package io.github.kotlinmania.starlark.tests.derive.module

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
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
import io.github.kotlinmania.starlark.Either
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.values.BoolUnpackValue
import io.github.kotlinmania.starlark.values.IntUnpackValue
import io.github.kotlinmania.starlark.values.StringUnpackValue
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.valueof.ValueOfUnpackValue
import io.github.kotlinmania.starlark.values.valueof.ValueOf
import io.github.kotlinmania.starlark.values.types.dict.UnpackDictEntriesUnpackValue
import io.github.kotlinmania.starlark.values.types.dict.UnpackDictEntries
import io.github.kotlinmania.starlark.values.types.list.UnpackListUnpackValue
import io.github.kotlinmania.starlark.values.types.list.UnpackList
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import kotlin.test.Test

class UnpackValueTests {
    // NOTE(nmj): Figure out default values here. ValueOf<i32> = 5 should work.
    private fun validateModule(builder: GlobalsBuilder) {
        builder.setFunction("with_int") { args, eval ->
            val heap = eval.heap()
            val unpacker = ValueOfUnpackValue(IntUnpackValue)
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            heap.allocTuple(listOf(v.value, heap.allocStr(v.typed.toString()).toValue()))
        }

        builder.setFunction("with_int_list") { args, eval ->
            val heap = eval.heap()
            val unpacker = ValueOfUnpackValue(UnpackListUnpackValue(IntUnpackValue))
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val repr = v.typed.items.joinToString(", ")
            heap.allocTuple(listOf(v.value, heap.allocStr(repr).toValue()))
        }

        builder.setFunction("with_list_list") { args, eval ->
            val heap = eval.heap()
            val inner = ValueOfUnpackValue(UnpackListUnpackValue(IntUnpackValue))
            val unpacker = ValueOfUnpackValue(UnpackListUnpackValue(inner))
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val repr = v.typed.items.joinToString(" + ") { l ->
                l.typed.items.joinToString(", ")
            }
            heap.allocTuple(listOf(v.value, heap.allocStr(repr).toValue()))
        }

        builder.setFunction("with_dict_list") { args, eval ->
            val heap = eval.heap()
            val dict = UnpackDictEntriesUnpackValue(IntUnpackValue, IntUnpackValue)
            val unpacker = ValueOfUnpackValue(UnpackListUnpackValue(dict))
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val repr = v.typed.items.joinToString(" + ") { l ->
                l.entries.joinToString(", ") { (k, v2) -> "$k: $v2" }
            }
            heap.allocTuple(listOf(v.value, heap.allocStr(repr).toValue()))
        }

        builder.setFunction("with_int_dict") { args, eval ->
            val heap = eval.heap()
            val unpacker = ValueOfUnpackValue(UnpackDictEntriesUnpackValue(IntUnpackValue, IntUnpackValue))
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val repr = v.typed.entries.joinToString(" + ") { (k, v2) -> "$k: $v2" }
            heap.allocTuple(listOf(v.value, heap.allocStr(repr).toValue()))
        }

        builder.setFunction("with_list_dict") { args, eval ->
            val heap = eval.heap()
            val list = ValueOfUnpackValue(UnpackListUnpackValue(IntUnpackValue))
            val dict = UnpackDictEntriesUnpackValue(IntUnpackValue, list)
            val unpacker = ValueOfUnpackValue(dict)
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val repr = v.typed.entries.joinToString(" + ") { (k, v2) ->
                "$k: ${v2.typed.items.joinToString(", ")}"
            }
            heap.allocTuple(listOf(v.value, heap.allocStr(repr).toValue()))
        }

        builder.setFunction("with_dict_dict") { args, eval ->
            val heap = eval.heap()
            val inner = UnpackDictEntriesUnpackValue(IntUnpackValue, IntUnpackValue)
            val unpacker = ValueOfUnpackValue(UnpackDictEntriesUnpackValue(IntUnpackValue, inner))
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val repr = v.typed.entries.joinToString(" + ") { (k, v2) ->
                val innerRepr = v2.entries.joinToString(", ") { (k2, v3) -> "$k2:$v3" }
                "$k: $innerRepr"
            }
            heap.allocTuple(listOf(v.value, heap.allocStr(repr).toValue()))
        }

        builder.setFunction("with_either") { args, eval ->
            val heap = eval.heap()
            val nested = io.github.kotlinmania.starlark.values.EitherUnpackValue(
                StringUnpackValue,
                ValueOfUnpackValue(UnpackListUnpackValue(IntUnpackValue)),
            )
            val unpacker = io.github.kotlinmania.starlark.values.EitherUnpackValue(
                IntUnpackValue,
                nested,
            )
            val v = unpacker.unpackParam(args.positional1(heap).getOrThrow())
            val result = when (v) {
                is Either.Left -> v.value.toString()
                is Either.Right -> when (val nested2 = v.value) {
                    is Either.Left -> nested2.value
                    is Either.Right -> nested2.value.value.toRepr()
                }
            }
            heap.allocStr(result)
        }
    }

    @Test
    fun testValueOf() {
        val a = Assert()
        a.globalsAdd(::validateModule)
        a.eq("(1, '1')", "with_int(1)")
        a.fail("with_int(noop(None))", BAD)
    }

    @Test
    fun testListOf() {
        val a = Assert()
        a.globalsAdd(::validateModule)
        a.eq("([1, 2, 3], '1, 2, 3')", "with_int_list([1, 2, 3])")
        a.fail("with_int_list(noop(1))", BAD)
        a.fail("with_int_list(noop([1, 'foo']))", BAD)
        a.fail("with_int_list(noop([[]]))", BAD)

        a.eq(
            "([[1, 2], [3]], '1, 2 + 3')",
            "with_list_list([[1, 2], [3]])",
        )

        val expected = """([{1: 2, 3: 4}, {5: 6}], "1: 2, 3: 4 + 5: 6")"""
        val test = """with_dict_list([{1: 2, 3: 4}, {5: 6}])"""
        a.eq(expected, test)
    }

    @Test
    fun testDictOf() {
        val a = Assert()
        a.globalsAdd(::validateModule)
        a.eq("({1: 2}, '1: 2')", "with_int_dict({1: 2})")
        a.fail("""with_int_dict(noop(1))""", BAD)
        a.fail("""with_int_dict(noop({1: "str"}))""", BAD)
        a.fail("""with_int_dict(noop({1: {}}))""", BAD)

        val expected = """({1: [2, 3], 4: [5]}, "1: 2, 3 + 4: 5")"""
        val test = """with_list_dict({1: [2, 3], 4: [5]})"""
        a.eq(expected, test)

        val expected2 = """({1: {2: 3, 4: 5}, 6: {7: 8}}, "1: 2:3, 4:5 + 6: 7:8")"""
        val test2 = """with_dict_dict({1: {2: 3, 4: 5}, 6: {7: 8}})"""
        a.eq(expected2, test2)
    }

    @Test
    fun testEitherOf() {
        val a = Assert()
        a.globalsAdd(::validateModule)
        a.eq("'2'", "with_either(2)")
        a.eq("'[2, 3]'", "with_either([2,3])")
        a.eq("'s'", "with_either('s')")
        a.fail("with_either(noop(None))", BAD)
        a.fail("with_either(noop({}))", BAD)
    }

    companion object {
        // The standard error these raise on incorrect types
        private const val BAD = "Type of parameter"
    }
}
