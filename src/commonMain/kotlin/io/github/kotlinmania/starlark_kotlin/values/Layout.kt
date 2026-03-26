// port-lint: source src/values/layout.rs
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

// Possible optimisations:
// Encoding none, bool etc in the pointer of frozen value

// Rust mod declarations — in Kotlin, these are separate files in the layout/ package.
// mod aligned_size
// mod avalue
// mod avalues
// mod complex
// mod const_frozen_string
// mod const_type_id
// mod freezer
// mod heap
// mod identity
// mod pointer
// mod static_string
// mod typed
// mod value
// mod value_alloc_size
// mod value_captured
// mod value_lifetimeless
// mod value_not_special
// mod vtable
