// port-lint: source tests:src/eval/runtime/rust_loc.rs
package io.github.kotlinmania.starlark.eval.runtime

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
import io.github.kotlinmania.starlark.eval.runtime.rustloc.rustLoc
import io.github.kotlinmania.starlark.values.layout.Value
import kotlin.test.Test
import kotlin.test.assertTrue

class RustLocTest {
    private fun rustLocGlobals(globals: GlobalsBuilder) {
        fun invoke(f: Value, eval: Evaluator): Result<Value> =
            f.invokeWithLoc(rustLoc("src/eval/runtime/rust_loc.rs", 24), Arguments.default(), eval)

        globals.setFunction("invoke") { args, eval ->
            invoke(args.positional<Value>(0), eval)
        }
    }

    @Test
    fun testRustLoc() {
        val a = Assert()
        a.globalsAdd(::rustLocGlobals)
        val err = a.fail("invoke(fail)", "")
        val errStr = err.toString()
        // Stack trace should contain invocation in `invoke`.
        assertTrue(
            // Make test compatible with Windows.
            errStr
                .replace('\\', '/')
                .contains("src/eval/runtime/rust_loc.rs"),
            "output: $errStr",
        )
        assertTrue(errStr.contains("<native>"), "output: $errStr")
    }
}
