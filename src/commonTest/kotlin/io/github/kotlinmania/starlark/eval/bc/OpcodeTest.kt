// port-lint: source tests:src/eval/bc/opcode.rs
package io.github.kotlinmania.starlark.eval.bc

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

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OpcodeTest {
    @Test
    fun opcodeCount() {
        for (i in 0 until 10000) {
            if (i < BcOpcode.COUNT) {
                assertNotNull(BcOpcode.byNumber(i))
            } else {
                assertNull(BcOpcode.byNumber(i))
            }
        }
    }
}
