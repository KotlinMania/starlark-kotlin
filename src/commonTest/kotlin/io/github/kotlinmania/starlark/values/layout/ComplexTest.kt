// port-lint: source tests:src/values/layout/complex.rs
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
import io.github.kotlinmania.starlark.tests.util.TestComplexValue
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import kotlin.test.Test

class ComplexTest {
    private fun testModule(globals: GlobalsBuilder) {
        fun testUnpack(v: ValueTypedComplex<TestComplexValue<Value>>): Result<String> =
            when (val unpacked = v.unpack()) {
                is io.github.kotlinmania.starlark.util.Either.Left ->
                    Result.success(unpacked.value.value.unpackStr() ?: error("not a string"))
                is io.github.kotlinmania.starlark.util.Either.Right ->
                    Result.success(
                        unpacked.value.value
                            .toValue()
                            .unpackStr() ?: error("not a string"),
                    )
            }
        globals.setFunction("test_unpack") { args, eval ->
            val v = args.positional<ValueTypedComplex<TestComplexValue<Value>>>(0)
            testUnpack(v).map { eval.heap().allocStr(it).toValue() }
        }
    }

    @Test
    fun testUnpack() {
        val a = Assert()
        a.globalsAdd(::testModule)
        a.setupEval { eval ->
            val s = eval.heap().alloc("test1")
            val x = eval.heap().alloc(TestComplexValue(s))
            val y =
                eval.frozenHeap().alloc(
                    TestComplexValue(constFrozenString("test2").toFrozenValue()),
                )
            eval.module().set("x", x)
            eval.module().set("y", y.toValue())
        }
        a.eq("'test1'", "test_unpack(x)")
        a.eq("'test2'", "test_unpack(y)")
    }
}
