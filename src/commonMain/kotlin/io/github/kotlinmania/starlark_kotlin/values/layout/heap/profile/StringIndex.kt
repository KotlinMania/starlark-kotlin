// port-lint: source src/values/layout/heap/profile/string_index.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.string_index

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

/// Map strings to integers 0, 1, 2, ...
// #[derive(Default, Clone, Allocative)]
// pub(crate) struct StringIndex { strings: SmallSet<ArcStr> }
internal class StringIndex {
    private val strings: MutableList<String> = mutableListOf()
    private val indexMap: MutableMap<String, Int> = mutableMapOf()

    // pub(crate) fn index(&mut self, s: &str) -> StringId
    fun index(s: String): StringId {
        val existing = indexMap[s]
        if (existing != null) {
            return StringId(existing)
        }
        val id = strings.size
        strings.add(s)
        indexMap[s] = id
        return StringId(id)
    }

    // pub(crate) fn get(&self, id: StringId) -> &ArcStr
    fun get(id: StringId): String {
        return strings[id.index]
    }
}

// #[derive(Copy, Clone, Dupe, Debug, Eq, PartialEq, Hash, Allocative)]
// pub(crate) struct StringId(pub(crate) usize);
internal data class StringId(
    /// Index in strings index.
    val index: Int,
)
