// port-lint: source src/values/types/structs/unordered_hasher.rs
package io.github.kotlinmania.starlark.values.types.structs

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

import starlarkmap.StarlarkHasher

/**
 * Utility to compute hash of an unordered collection (e.g. unordered set).
 */
internal class UnorderedHasher {
    private var state: ULong = 0uL
    private var count: ULong = 0uL

    companion object {
        fun new(): UnorderedHasher {
            return UnorderedHasher()
        }
    }

    fun writeHash(value: ULong) {
        state = state + value  // wrapping_add in Rust
        count = count + 1uL    // wrapping_add in Rust
    }

    fun finish(): ULong {
        val hasher = StarlarkHasher()
        hasher.writeU64(state)
        hasher.writeU64(count)
        return hasher.finish()
    }
}
