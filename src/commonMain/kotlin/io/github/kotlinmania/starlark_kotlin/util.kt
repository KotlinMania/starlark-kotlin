// port-lint: source src/util.rs
package io.github.kotlinmania.starlark_kotlin

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

import io.github.kotlinmania.starlark_kotlin.util.ArcStr

/**
 * Utilities.
 *
 * Submodules (Kotlin packages under util):
 * - pub(crate) mod arc_or_static      -> util.arc_or_static
 * - pub(crate) mod arc_str            -> util.arc_str
 * - pub(crate) mod non_static_type_id -> util.non_static_type_id
 * - pub(crate) mod refcell            -> util.refcell
 * - pub(crate) mod rtabort            -> util.rtabort
 *
 * Re-exports:
 * - pub use crate::util::arc_str::ArcStr
 */
