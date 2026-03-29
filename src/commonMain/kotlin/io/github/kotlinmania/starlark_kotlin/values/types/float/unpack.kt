// port-lint: source src/values/types/float/unpack.rs
package io.github.kotlinmania.starlark_kotlin.values.types.float

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

import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.num.NumRef

/** Unpack `int` or `float` into `Double`. */
class UnpackFloat(val value: Double) {
    companion object {
        fun unpackValue(value: Value): UnpackFloat? {
            val num = NumRef.unpackValueImpl(value) ?: return null
            return UnpackFloat(num.asFloat())
        }
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
