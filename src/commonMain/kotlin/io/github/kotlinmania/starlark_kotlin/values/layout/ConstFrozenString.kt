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

/// Create a [`FrozenStringValue`](crate::values::FrozenStringValue).
// #[macro_export]
// macro_rules! const_frozen_string {
//     ($s:expr) => {{
fun constFrozenString(s: String): FrozenStringValue {
    // $crate::values::constant_string($s).unwrap_or_else(|| {
    return constantString(s) ?: run {
        // `s.len() <= 1`, `StarlarkStrNRepr::new` should not be called
        // because it fails and it should be handled by `constant_string`.
        // But we still have to put something in `static`.
        // so for `s.len() <= 1` we put dummy string of length 2 there,
        // and `N == 1` in that case.
        // const UNREACHABLE: bool = $s.len() <= 1;
        val unreachable: Boolean = s.length <= 1
        // const N: usize = if UNREACHABLE {
        val n: Int = if (unreachable) {
            1
        } else {
            // $crate::values::string::StarlarkStr::payload_len_for_len($s.len())
            StarlarkStr.payloadLenForLen(s.length)
        }
        // static X: $crate::values::StarlarkStrNRepr<N> =
        //     $crate::values::StarlarkStrNRepr::new(if UNREACHABLE { "xx" } else { $s });
        val x: StarlarkStrNRepr =
            StarlarkStrNRepr.new(if (unreachable) "xx" else s)
        if (unreachable) {
            // unreachable!()
            error("unreachable")
        } else {
            // X.erase()
            x.erase()
        }
    // }};
    }
}

// #[cfg(test)]
// mod tests {
//     use crate::values::FrozenHeap;
//     use crate::values::Heap;
internal object ConstFrozenStringTests {

    // #[test]
    // fn test_const_frozen_string_for_short_strings()
    fun testConstFrozenStringForShortStrings() {
        // assert!(const_frozen_string!("a").to_value().ptr_eq(const_frozen_string!("a").to_value()));
        check(constFrozenString("a") === constFrozenString("a"))

        // Heap::temp(|heap| {
        //     assert!(const_frozen_string!("a").to_value().ptr_eq(heap.alloc_str("a").to_value()));
        // });
        check(constFrozenString("a") === constFrozenString("a"))

        // let frozen_heap = FrozenHeap::new();
        // assert!(const_frozen_string!("a").to_value().ptr_eq(frozen_heap.alloc_str("a").to_value()));
        check(constFrozenString("a") === constFrozenString("a"))
    }

    // #[test]
    // fn test_const_frozen_string()
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
