// port-lint: tests src/values/layout/typed.rs
package io.github.kotlinmania.starlark.values.layout

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
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.tests.TestComplexValue
import io.github.kotlinmania.starlark.values.types.none.NoneType
import kotlin.test.Test
import kotlin.test.assertEquals

class TypedTest {
    @Test
    fun int() {
        val v = FrozenValue.testingNewInt(17).toValue().unpackIntValue()!!
        assertEquals(17, v.asRef().get().toI32())
    }

    @Test
    fun testUnpackValueForFrozenValueTyped() {
        fun module(globals: GlobalsBuilder) {
            fun mutable(): Result<TestComplexValue> =
                Result.success(TestComplexValue(Value.newNone()))

            fun takesFrozenValueTyped(value: FrozenValueTyped<TestComplexValue>): Result<NoneType> {
                value.toString()
                return Result.success(NoneType)
            }

            globals.setConst("FROZEN", TestComplexValue(FrozenValue.newNone().toValue()))
            globals.setFunction("mutable") { _, _ -> mutable() }
            globals.setFunction("takes_frozen_value_typed") { args, _ ->
                val v = args.positionalAll().getOrNull(0) ?: throw IllegalArgumentException("Missing parameter")
                try {
                    takesFrozenValueTyped(args.positional<FrozenValueTyped<TestComplexValue>>(0))
                } catch (e: IllegalArgumentException) {
                    val msg = e.message ?: ""
                    if (msg.contains("Expected frozen value, got")) {
                        throw IllegalArgumentException("Expected frozen value", e)
                    } else if (msg.contains("Expected frozen value of type")) {
                        throw IllegalArgumentException(
                            "Type of parameter `value` doesn't match, expected `TestComplexValue`, actual `${v.toStringForTypeError()}`",
                            e
                        )
                    } else {
                        throw e
                    }
                }
            }
        }

        val a = Assert()
        a.globalsAdd(::module)

        a.pass("takes_frozen_value_typed(FROZEN)")
        a.fail("takes_frozen_value_typed(1)", "Type of parameter `value` doesn't match, expected `TestComplexValue`, actual `int (repr: 1)`")
        a.fail(
            "takes_frozen_value_typed(mutable())",
            "Expected frozen value",
        )
    }
}
