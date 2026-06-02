// port-lint: tests src/values/layout/const_frozen_string.rs
package io.github.kotlinmania.starlark.values.layout

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

import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import kotlin.test.Test

/** Test object for const_frozen_string. */
internal class ConstFrozenStringTest {
    @Test
    fun testConstFrozenStringForShortStrings() {
        check(constFrozenString("a").toValue().ptrEq(constFrozenString("a").toValue()))

        Heap.temp { heap ->
            check(constFrozenString("a").toValue().ptrEq(heap.allocStr("a").toValue()))
        }

        val frozenHeap = FrozenHeap.new()
        val ref = frozenHeap.intoRef()
        try {
            check(constFrozenString("a").toValue().ptrEq(frozenHeap.allocStrIntern("a").toValue()))
        } finally {
            (ref as kotlin.AutoCloseable).close()
        }
    }

    @Test
    fun testConstFrozenString() {
        // assert_eq!("", const_frozen_string!("").as_str());
        check("" == constFrozenString("").asStr())
        // assert_eq!("a", const_frozen_string!("a").as_str());
        check("a" == constFrozenString("a").asStr())
        // assert_eq!("ab", const_frozen_string!("ab").as_str());
        check("ab" == constFrozenString("ab").asStr())
        // assert_eq!("abc", const_frozen_string!("abc").as_str());
        check("abc" == constFrozenString("abc").asStr())
        // assert_eq!("abcd", const_frozen_string!("abcd").as_str());
        check("abcd" == constFrozenString("abcd").asStr())
        // assert_eq!("abcde", const_frozen_string!("abcde").as_str());
        check("abcde" == constFrozenString("abcde").asStr())
        // assert_eq!("abcdef", const_frozen_string!("abcdef").as_str());
        check("abcdef" == constFrozenString("abcdef").asStr())
        // assert_eq!("abcdefg", const_frozen_string!("abcdefg").as_str());
        check("abcdefg" == constFrozenString("abcdefg").asStr())
        // assert_eq!("abcdefgh", const_frozen_string!("abcdefgh").as_str());
        check("abcdefgh" == constFrozenString("abcdefgh").asStr())
        // assert_eq!("abcdefghi", const_frozen_string!("abcdefghi").as_str());
        check("abcdefghi" == constFrozenString("abcdefghi").asStr())
        // assert_eq!("abcdefghij", const_frozen_string!("abcdefghij").as_str());
        check("abcdefghij" == constFrozenString("abcdefghij").asStr())
        // assert_eq!("abcdefghijk", const_frozen_string!("abcdefghijk").as_str());
        check("abcdefghijk" == constFrozenString("abcdefghijk").asStr())
    }
}
