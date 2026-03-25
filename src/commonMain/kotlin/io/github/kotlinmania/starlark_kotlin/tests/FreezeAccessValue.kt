// port-lint: source src/tests/freeze_access_value.rs
package io.github.kotlinmania.starlark_kotlin.tests

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

import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.string.unpackNum
import io.github.kotlinmania.starlark_kotlin.fromValue
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.asInt

// struct Test<V> { field: V }
private class TestFreeze(var field: Value) : Freeze<TestFrozen> {
    // impl<'v> Freeze for Test<Value<'v>>
    override fun freeze(freezer: Freezer): Result<TestFrozen> {
        val frozenField = field.freeze(freezer).getOrElse { return Result.failure(it) }
        val test = TestFrozen(frozenField)
        val members = ListRef.fromValue(test.field.toValue())!!
        check(members[0].unpackNum()!!.asInt()!! == 1)
        check(members[1].unpackNum()!!.asInt()!! == 2)
        return Result.success(test)
    }
}

private class TestFrozen(val field: FrozenValue)

// #[test]
// fn test() -> anyhow::Result<()>
internal fun testFreezeAccessValue() {
    Heap.temp { heap ->
        val list = heap.alloc(listOf(1, 2))

        val t = TestFreeze(list)

        val frozenHeap = FrozenHeap()
        val freezer = Freezer(frozenHeap)
        list.freeze(freezer).getOrThrow()
        t.freeze(freezer).getOrThrow()

        Result.success(Unit)
    }.getOrThrow()
}
