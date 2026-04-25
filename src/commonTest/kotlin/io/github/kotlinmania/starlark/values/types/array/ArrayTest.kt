// port-lint: source src/values/types/array.rs (tests)
package io.github.kotlinmania.starlark.values.types.array

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

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocArray
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.int.StarlarkInt
import io.github.kotlinmania.starlark.values.types.int.allocValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ArrayTest {

    // #[test]
    // fn debug()
    @Test
    fun debug() {
        Heap.temp { heap ->
            val array = heap.allocArray(10).asRef()
            array.push(StarlarkInt.from(23).allocValue(heap))
            // Just check it does not crash.
            array.toString()
        }
    }

    // #[test]
    // fn display()
    @Test
    fun display() {
        Heap.temp { heap ->
            val array = heap.allocArray(10).asRef()
            array.push(StarlarkInt.from(29).allocValue(heap))
            array.push(Value.newNone())
            assertEquals("array([29, None], cap=10)", array.toString())
        }
    }

    // #[test]
    // fn push()
    @Test
    fun push() {
        Heap.temp { heap ->
            val array = heap.allocArray(10).asRef()
            array.push(StarlarkInt.from(17).allocValue(heap))
            array.push(StarlarkInt.from(19).allocValue(heap))
            assertEquals(StarlarkInt.from(19).allocValue(heap), array.content()[1])
        }
    }
}
