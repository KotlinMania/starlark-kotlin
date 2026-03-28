// port-lint: source src/tests/derive/module/unpack_value.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.module

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.value_of.ValueOf
import io.github.kotlinmania.starlark_kotlin.values.types.dict.UnpackDictEntries
import io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackList
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.owned.typed
import io.github.kotlinmania.starlark_kotlin.values.value_of.typed

private sealed class Either<out L, out R> {
    class Left<L>(val value: L) : Either<L, Nothing>()
    class Right<R>(val value: R) : Either<Nothing, R>()
}

// TODO(nmj): Figure out default values here. ValueOf<i32> = 5 should work.
// #[starlark_module]
// fn validate_module(builder: &mut GlobalsBuilder)
private fun validateModule(builder: GlobalsBuilder) {
    // fn with_int(v: ValueOf<i32>) -> Result<(Value, String)>
    builder.setFunction("with_int") { args, _ ->
        val v = args.positional<ValueOf<Int>>(0)
        Result.success(listOf(v.value, v.typed.toString()))
    }

    // fn with_int_list(v: ValueOf<UnpackList<i32>>) -> Result<(Value, String)>
    builder.setFunction("with_int_list") { args, _ ->
        val v = args.positional<ValueOf<UnpackList<Int>>>(0)
        val repr = v.typed.items.joinToString(", ")
        Result.success(listOf(v.value, repr))
    }

    // fn with_list_list(v: ValueOf<UnpackList<ValueOf<UnpackList<i32>>>>) -> Result<(Value, String)>
    builder.setFunction("with_list_list") { args, _ ->
        val v = args.positional<ValueOf<UnpackList<ValueOf<UnpackList<Int>>>>>(0)
        val repr = v.typed.items.joinToString(" + ") { l ->
            l.typed.items.joinToString(", ")
        }
        Result.success(listOf(v.value, repr))
    }

    // fn with_dict_list(v: ValueOf<UnpackList<UnpackDictEntries<i32, i32>>>) -> Result<(Value, String)>
    builder.setFunction("with_dict_list") { args, _ ->
        val v = args.positional<ValueOf<UnpackList<UnpackDictEntries<Int, Int>>>>(0)
        val repr = v.typed.items.joinToString(" + ") { l ->
            l.entries.joinToString(", ") { (k, v2) -> "$k: $v2" }
        }
        Result.success(listOf(v.value, repr))
    }

    // fn with_int_dict(v: ValueOf<UnpackDictEntries<i32, i32>>) -> Result<(Value, String)>
    builder.setFunction("with_int_dict") { args, _ ->
        val v = args.positional<ValueOf<UnpackDictEntries<Int, Int>>>(0)
        val repr = v.typed.entries.joinToString(" + ") { (k, v2) -> "$k: $v2" }
        Result.success(listOf(v.value, repr))
    }

    // fn with_list_dict(v: ValueOf<UnpackDictEntries<i32, ValueOf<UnpackList<i32>>>>) -> Result<(Value, String)>
    builder.setFunction("with_list_dict") { args, _ ->
        val v = args.positional<ValueOf<UnpackDictEntries<Int, ValueOf<UnpackList<Int>>>>>(0)
        val repr = v.typed.entries.joinToString(" + ") { (k, v2) ->
            "$k: ${v2.typed.items.joinToString(", ")}"
        }
        Result.success(listOf(v.value, repr))
    }

    // fn with_dict_dict(v: ValueOf<UnpackDictEntries<i32, UnpackDictEntries<i32, i32>>>) -> Result<(Value, String)>
    builder.setFunction("with_dict_dict") { args, _ ->
        val v = args.positional<ValueOf<UnpackDictEntries<Int, UnpackDictEntries<Int, Int>>>>(0)
        val repr = v.typed.entries.joinToString(" + ") { (k, v2) ->
            val innerRepr = v2.entries.joinToString(", ") { (k2, v3) -> "$k2:$v3" }
            "$k: $innerRepr"
        }
        Result.success(listOf(v.value, repr))
    }

    // fn with_either(v: Either<i32, Either<String, ValueOf<UnpackList<i32>>>>) -> Result<String>
    builder.setFunction("with_either") { args, _ ->
        val v = args.positional<Either<Int, Either<String, ValueOf<UnpackList<Int>>>>>(0)
        val result = when (v) {
            is Either.Left -> v.value.toString()
            is Either.Right -> when (val nested = v.value) {
                is Either.Left -> nested.value
                is Either.Right -> nested.value.value.toRepr()
                else -> throw IllegalStateException("Unexpected Either: $nested")
            }
            else -> throw IllegalStateException("Unexpected Either: $v")
        }
        Result.success(result)
    }
}

// The standard error these raise on incorrect types
// const BAD: &str = "Type of parameter";
private const val BAD = "Type of parameter"

// #[test]
// fn test_value_of()
internal fun testValueOf() {
    val a = Assert()
    a.globalsAdd(::validateModule)
    a.eq("(1, '1')", "with_int(1)")
    a.fail("with_int(noop(None))", BAD)
}

// #[test]
// fn test_list_of()
internal fun testListOf() {
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

// #[test]
// fn test_dict_of()
internal fun testDictOf() {
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

// #[test]
// fn test_either_of()
internal fun testEitherOf() {
    val a = Assert()
    a.globalsAdd(::validateModule)
    a.eq("'2'", "with_either(2)")
    a.eq("'[2, 3]'", "with_either([2,3])")
    a.eq("'s'", "with_either('s')")
    a.fail("with_either(noop(None))", BAD)
    a.fail("with_either(noop({}))", BAD)
}
