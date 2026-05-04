// port-lint: source tests/derive/module/specialParams.rs
package io.github.kotlinmania.starlark.tests.derive.module

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
import io.github.kotlinmania.starlark.eval.runtime.positional
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStrConcat
import kotlin.test.Test

private fun functions(builder: GlobalsBuilder) {
    fun nonStandardHeapName(heap: String, starlarkHeap: io.github.kotlinmania.starlark.values.layout.heap.Heap): Result<io.github.kotlinmania.starlark.values.layout.Value> =
        Result.success(starlarkHeap.allocStrConcat(heap, "!").toValue())

    builder.setFunction("non_standard_heap_name") { args, eval ->
        nonStandardHeapName(args.positional<String>(0), eval.heap())
    }
}

class SpecialParamsTests {
    @Test
    fun testNonStandardParamNames() {
        val a = Assert()
        a.globalsAdd(::functions)
        a.eq("'x!'", "non_standard_heap_name('x')")
    }
}
