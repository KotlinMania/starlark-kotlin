// port-lint: source src/collections.rs
package io.github.kotlinmania.starlark_kotlin.collections

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
 * Defines [SmallMap] and [SmallSet] - collections with deterministic iteration and small memory footprint.
 *
 * These structures use vector-backed storage if there are only a few elements, and an index
 * for larger collections. The API mirrors standard Rust collections.
 *
 * Rust uses `pub use ...` re-exports from `starlark_map` in this module. Kotlin has no direct
 * equivalent of `pub use`, so call sites should import the concrete types directly from
 * `io.github.kotlinmania.starlark_kotlin.collections.*`.
 */

