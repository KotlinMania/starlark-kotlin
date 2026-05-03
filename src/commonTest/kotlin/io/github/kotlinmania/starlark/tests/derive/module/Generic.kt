// port-lint: source src/tests/derive/module/generic.rs
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
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.types.none.NoneType
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import kotlin.test.Test

private fun <T, U> globalBuilder(
    globals: GlobalsBuilder,
    defaultT: () -> T,
    defaultU: () -> U,
) {
    globals.setConst("MY_STR", defaultU().toString())
}

private class CustomNone<T> : StarlarkTypeRepr, AllocValue {
    companion object : StarlarkTypeRepr {
        override fun starlarkTypeRepr(): Ty = NoneType.starlarkTypeRepr()
    }

    override fun starlarkTypeRepr(): Ty = Companion.starlarkTypeRepr()

    override fun allocValue(heap: Heap): Value = Value.newNone()
}

private fun <T, U> methodBuilder(
    builder: MethodsBuilder,
    defaultT: () -> T,
    defaultU: () -> U,
) {
    // Just check that this compiles
    fun testAttribute(thisU32: UInt): Result<CustomNone<T>> {
        // Mirrors Rust `fn test_attribute(this: u32) -> Result<CustomNone<T>>` — `this`
        // is consumed by the unpacker for type-binding only.
        thisU32.toLong()
        defaultU().toString().length
        defaultT()
        return Result.success(CustomNone())
    }

    builder.setAttribute("test_attribute") { _this, heap ->
        // The `this` parameter is u32 in Rust; the helper consumes it for type-binding only.
        Result.success(testAttribute(0u).getOrThrow().allocValue(heap))
    }
}

private fun <T, U> globalBuilderForFunc(
    globals: GlobalsBuilder,
    defaultT: () -> T,
    defaultU: () -> U,
) {
    fun makeMyStr(): Result<String> {
        defaultT()
        return Result.success(defaultU().toString())
    }

    globals.setFunction("make_my_str") { _, _ -> makeMyStr() }
}

class GenericTests {
    @Test
    fun testGenericBuilder() {
        val a = Assert()
        a.globalsAdd { g ->
            globalBuilder<UByte, UByte>(g, { 0u.toUByte() }, { 0u.toUByte() })
            globalBuilderForFunc<UByte, UByte>(g, { 0u.toUByte() }, { 0u.toUByte() })
        }
        a.eq("\"0\"", "MY_STR")
        a.eq("\"0\"", "make_my_str()")
    }
}
