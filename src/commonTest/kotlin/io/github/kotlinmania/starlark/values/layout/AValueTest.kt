// port-lint: tests src/values/layout/avalue.rs
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

import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.allocValue
import io.github.kotlinmania.starlark.values.types.list.ListData
import io.github.kotlinmania.starlark.values.types.tuple.unpackTuple2
import kotlin.test.Test

internal class AValueTest {
    @Test
    fun tupleCycleFreeze() {
        Module.withTempHeap { module ->
            val list = module.heap().allocListIter(emptyList<Value>())
            val tuple = module.heap().allocTuple(listOf(list))
            ListData
                .fromValueMut(list)
                .getOrNull()
                ?.push(tuple, module.heap())
            module.set("t", tuple)
            module.freeze()
            Result.success(Unit)
        }
    }

    @Test
    fun testTryFreezeDirectly() {
        // `try_freeze_directly` is only implemented for `dict` at the moment of writing,
        // so use it for the test.

        Module.withTempHeap { module ->
            val d0 = Dict.new(SmallMap.new()).allocValue(module.heap())
            val d1 = Dict.new(SmallMap.new()).allocValue(module.heap())
            // Pointers are not equal.
            check(d0 !== d1)

            module.setExtraValue(module.heap().allocTuple(listOf(d0, d1)))

            val frozen = module.freeze().getOrThrow()
            val extra = frozen.extraValue()!!.toValue()
            val (fd0, fd1) =
                unpackTuple2<Value, Value>(extra, { it }, { it })
                    ?: error("expected a 2-element tuple")
            // Pointers are equal.
            check(fd0 === fd1)
            Result.success(Unit)
        }
    }
}
