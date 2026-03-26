// port-lint: source src/values/layout/const_frozen_string.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StarlarkStr

/** Create a [FrozenStringValue]. */
fun constFrozenString(s: String): FrozenStringValue {
    return constantString(s) ?: run {
        // `s.len() <= 1`, `StarlarkStrNRepr::new` should not be called
        // because it fails and it should be handled by `constant_string`.
        // But we still have to put something in `static`.
        // so for `s.len() <= 1` we put dummy string of length 2 there,
        // and `N == 1` in that case.
        val unreachable: Boolean = s.length <= 1
        val n: Int = if (unreachable) {
            1
        } else {
            StarlarkStr.payloadLenForLen(s.length)
        }
        val x: StarlarkStrNRepr =
            StarlarkStrNRepr.new(if (unreachable) "xx" else s)
        if (unreachable) {
            error("unreachable")
        } else {
            x.erase()
        }
    }
}

// #[cfg(test)]
internal object ConstFrozenStringTests {

    private fun assertStr(expected: String, s: String) {
        check(expected == constFrozenString(s).asStr())
    }

    fun testConstFrozenStringForShortStrings() {
        check(constFrozenString("a") === constFrozenString("a"))
        check(constFrozenString("a") === constFrozenString("a"))
        check(constFrozenString("a") === constFrozenString("a"))
    }

    fun testConstFrozenString() {
        assertStr("", "")
        assertStr("a", "a")
        assertStr("ab", "ab")
        assertStr("abc", "abc")
        assertStr("abcd", "abcd")
        assertStr("abcde", "abcde")
        assertStr("abcdef", "abcdef")
        assertStr("abcdefg", "abcdefg")
        assertStr("abcdefgh", "abcdefgh")
        assertStr("abcdefghi", "abcdefghi")
        assertStr("abcdefghij", "abcdefghij")
        assertStr("abcdefghijk", "abcdefghijk")
    }
}
