// port-lint: source tests:src/environment/methods.rs
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
import io.github.kotlinmania.starlark.values.StarlarkValue
import kotlin.test.Test

class MethodsTest {
    private class Magic : StarlarkValue {
        override val TYPE: String get() = "magic"

        override fun toString(): String = "Magic"

        override fun getMethods(): Methods? =
            MethodsStatic.new().methods { x ->
                x.setAttribute("my_type", "magic", null)
                x.setAttribute("my_value", 42, null)
            }
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
