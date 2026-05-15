// port-lint: source tests:src/stdlib/internal.rs
package io.github.kotlinmania.starlark.stdlib

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
import kotlin.test.Test

class InternalTest {

    @Test
    fun testTyOfValueDebug() {
        Assert.pass("print(starlark_rust_internal.ty_of_value_debug(1))")
    }
}
