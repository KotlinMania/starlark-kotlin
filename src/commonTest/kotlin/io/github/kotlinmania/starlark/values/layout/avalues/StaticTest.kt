// port-lint: tests src/values/layout/avalues/static_.rs
package io.github.kotlinmania.starlark.values.layout.avalues

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

import io.github.kotlinmania.starlark.values.StarlarkValue
import kotlin.test.Test

internal class StaticTest {
    @Test
    fun testAllocStaticSimple() {
        class MySimpleValue(
            val value: UInt,
        ) : StarlarkValue {
            override val TYPE: String get() = "my_simple_value"
            override fun toString(): String = "MySimpleValue"
        }

        val allocated = AllocStaticSimple.alloc(MySimpleValue(17u))
        check(17u == allocated.unpack().asRef().value)
    }
}
