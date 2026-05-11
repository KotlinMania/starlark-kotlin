// port-lint: source tests:src/values/types/string/methods.rs
package io.github.kotlinmania.starlark.values.types.string

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
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

class StringMethodsTest {

    @Test
    fun testErrorCodes() {
        Assert.fail(""""bonbon".index("on", 2, 5)""", "not found in")
        Assert.fail("""("banana".replace("a", "o", -2))""", "negative")
        Assert.fail(""""bonbon".rindex("on", 2, 5)""", "not found in")
    }

    @Test
    fun testCount() {
        Assert.eq("'abc'.count('a', 10, -10)", "0")
    }

    @Test
    fun testFind() {
        Assert.eq("'Троянская война окончена'.find('война')", "10")
    }

    @Test
    fun testOpaqueIterator() {
        Assert.isTrue("type('foo'.elems()) != type([])")
        Assert.isTrue("type('foo'.codepoints()) != type([])")
    }
}
