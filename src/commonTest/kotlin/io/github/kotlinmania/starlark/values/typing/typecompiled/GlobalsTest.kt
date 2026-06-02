// port-lint: source tests:src/values/typing/type_compiled/globals.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

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

class GlobalsTest {
    @Test
    fun testTypechecking() {
        Assert.fail(
            """
def test():
    isinstance(1, "")
""",
            "Expected type `type` but got `str`",
        )
    }
}
