// port-lint: source src/typing/ty.rs
package io.github.kotlinmania.starlark_kotlin.typing

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

/**
 * A Starlark type.
 */
class Ty private constructor() {
    companion object {
        fun int(): Ty {
            TODO("Not yet implemented")
        }

        fun float(): Ty {
            TODO("Not yet implemented")
        }

        fun bool(): Ty {
            TODO("Not yet implemented")
        }

        fun union2(a: Ty, b: Ty): Ty {
            TODO("Not yet implemented")
        }
    }
}
