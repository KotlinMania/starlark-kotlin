// port-lint: tests src/environment/methods.rs
package io.github.kotlinmania.starlark.environment

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
 */

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import kotlin.test.Test

class MethodsTest {
    private class Magic :
        StarlarkValue,
        AllocFrozenValue {
        override val TYPE: String get() = "magic"

        override fun toString(): String = "Magic"

        override fun getMethods(): Methods? =
            MethodsStatic().methods { x ->
                x.setAttribute("my_type") { _, heap -> Result.success(heap.allocStr("magic")) }
                x.setAttribute("my_value") { _, _ -> Result.success(Value.testingNewInt(42)) }
            }

        override fun starlarkTypeRepr(): Ty = Ty.starlarkValue(TyStarlarkValue.new(TYPE))

        override fun allocFrozenValue(heap: FrozenHeap): FrozenValue = heap.allocSimple(this)
    }

    @Test
    fun testSetAttribute() {
        val a = Assert()
        a.globalsAdd { x -> x.set("magic", Magic()) }
        a.pass(
            """
assert_eq(magic.my_type, "magic")
assert_eq(magic.my_value, 42)""",
        )
    }
}
