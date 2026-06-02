// port-lint: source tests:src/values/types/any_complex.rs
package io.github.kotlinmania.starlark.values.types

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

import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.StringValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import io.github.kotlinmania.starlark.values.types.list.AllocList
import kotlin.test.Test
import kotlin.test.assertEquals

class AnyComplexTest {
    @Test
    fun testAnyComplex() {
        class UnfrozenData(
            val string: StringValue,
            val other: Value,
        ) : Freeze<FrozenData> {
            override fun freeze(freezer: Freezer): Result<FrozenData> =
                Result.success(
                    FrozenData(
                        string = string.freeze(freezer).getOrThrow(),
                        other = freezer.freeze(other).getOrThrow(),
                    ),
                )
        }

        Module.withTempHeap { module ->
            val data =
                module.heap().alloc(
                    StarlarkAnyComplex.new(
                        UnfrozenData(
                            string = module.heap().allocStr("aaa"),
                            other = module.heap().alloc(AllocList(listOf(1, 2))),
                        ),
                    ),
                )

            assertEquals(
                constFrozenString("aaa"),
                StarlarkAnyComplex.getErr<UnfrozenData>(data).getOrThrow().string,
            )

            module.setExtraValue(data)

            val frozenModule = module.freeze().getOrThrow()

            val frozenData = frozenModule.extraValue()!!
            assertEquals(
                constFrozenString("aaa"),
                StarlarkAnyComplex.getErr<FrozenData>(frozenData.toValue()).getOrThrow().string,
            )
        }
    }

    private class FrozenData(
        val string: io.github.kotlinmania.starlark.values.layout.FrozenStringValue,
        @Suppress("UNUSED") val other: FrozenValue,
    )
}
