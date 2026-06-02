// port-lint: tests src/values/types/namespace/value.rs
package io.github.kotlinmania.starlark_kotlin.values.types.namespace

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import kotlin.test.Test

internal class ValueTest {
    @Test
    fun testRepr() {
        Assert.eq("repr(namespace(a=1, b=[]))", "'namespace(a=1, b=[])'")
        Assert.eq("str(namespace(a=1, b=[]))", "'namespace(a=1, b=[])'")
    }

    @Test
    fun testReprCycle() {
        Assert.eq(
            "l = []; s = namespace(f=l); l.append(s); repr(s)",
            "'namespace(f=[namespace(...)])'",
        )
        Assert.eq(
            "l = []; s = namespace(f=l); l.append(s); str(s)",
            "'namespace(f=[namespace(...)])'",
        )
    }

    @Test
    fun testToJsonCycle() {
        Assert.fail(
            "l = []; s = namespace(f=l); l.append(s); json.encode(s)",
            "Cycle detected when serializing value of type `namespace` to JSON",
        )
    }

    @Test
    fun testKwargs() {
        Assert.eq(
            "d = {'b': 2}; s = namespace(a=1, **d); str(s)",
            "'namespace(a=1, b=2)'",
        )
    }
}
