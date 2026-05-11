<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/tests/derive/module/SpecialParams.kt
// port-lint: source tests/derive/module/special_params.rs
package io.github.kotlinmania.starlark.tests.derive.module
=======
// port-lint: tests tests/derive/module/special_params.rs
package io.github.kotlinmania.starlark_kotlin.tests.derive.module
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/tests/derive/module/SpecialParams.kt

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
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_.allocStrConcat

// #[starlark_module]
// fn functions(builder: &mut GlobalsBuilder)
private fun functions(builder: GlobalsBuilder) {
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/tests/derive/module/SpecialParams.kt
    fun nonStandardHeapName(heap: String, starlarkHeap: io.github.kotlinmania.starlark.values.layout.heap.Heap): Result<io.github.kotlinmania.starlark.values.layout.Value> =
        Result.success(starlarkHeap.allocStrConcat(heap, "!").toValue())

=======
    // fn non_standard_heap_name(heap: &str, starlark_heap: Heap) -> Result<StringValue>
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/tests/derive/module/SpecialParams.kt
    builder.setFunction("non_standard_heap_name") { args, eval ->
        nonStandardHeapName(args.positional<String>(0), eval.heap())
    }
}

// #[test]
// fn test_non_standard_param_names()
internal fun testNonStandardParamNames() {
    val a = Assert()
    a.globalsAdd(::functions)
    a.eq("'x!'", "non_standard_heap_name('x')")
}
