// port-lint: source src/values/layout/const_frozen_string.rs
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

import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StarlarkStr

// / Create a [`FrozenStringValue`](crate::values::FrozenStringValue).
// #[macro_export]
//     ($s:expr) => {{
fun constFrozenString(s: String): FrozenStringValue {
    // $crate::values::constant_string($s).unwrap_or_else(|| {
    return constantString(s) ?: run {
        // `s.len() <= 1`, `StarlarkStrNRepr::new` should not be called
        // because it fails and it should be handled by `constant_string`.
        // But we still have to put something in `static`.
        // so for `s.len() <= 1` we put dummy string of length 2 there,
        // and `N == 1` in that case.
        val unreachable: Boolean = s.length <= 1
        val n: Int =
            if (unreachable) {
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
