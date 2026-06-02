// port-lint: tests src/values/types/tuple/value.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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
import io.github.kotlinmania.starlark.assert.allTrue
import io.github.kotlinmania.starlark.assert.isFalse
import io.github.kotlinmania.starlark.assert.isTrue
import kotlin.test.Test

class ValueTest {
    @Test
    fun testToStr() {
        allTrue(
            """
str((1, 2, 3)) == "(1, 2, 3)"
str((1, (2, 3))) == "(1, (2, 3))"
str((1,)) == "(1,)"
"""
        )
    }

    @Test
    fun testReprCycle() {
        val a = Assert()
        a.disableStaticTypechecking()
        a.eq("l = []; t = (l,); l.append(t); repr(t)", "'([(...)],)'")
        a.eq("l = []; t = (l,); l.append(t); str(t)", "'([(...)],)'")
    }

    @Test
    fun testTupleEllipsisRuntime() {
        isTrue("isinstance((), tuple[int, ...])")
        isTrue("isinstance((1, ), tuple[int, ...])")
        isTrue("isinstance((1, 2), tuple[int, ...])")
        isFalse("isinstance(('x', 2), tuple[int, ...])")
    }
}
