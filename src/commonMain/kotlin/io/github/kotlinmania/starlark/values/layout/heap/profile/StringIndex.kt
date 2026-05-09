// port-lint: source values/layout/heap/profile/string_index.rs
package io.github.kotlinmania.starlark.values.layout.heap.profile.stringindex

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

/** Map strings to integers 0, 1, 2, ... */
internal class StringIndex {
    private val strings: LinkedHashSet<String> = linkedSetOf()

    fun index(s: String): StringId {
        val list = strings.toList()
        val existing = list.indexOf(s)
        if (existing >= 0) {
            return StringId(existing)
        }

        strings.add(s)
        return StringId(strings.size - 1)
    }

    fun get(id: StringId): String {
        return strings.toList()[id.index]
    }
}

/** Index in strings index. */
internal data class StringId(
    val index: Int,
)
