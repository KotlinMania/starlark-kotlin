// port-lint: source src/values/layout.rs
@file:Suppress("unused", "ObjectPropertyName")
package io.github.kotlinmania.starlark.values

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

// pub(crate) mod aligned_size;
internal val aligned_size = "aligned_size"
// pub(crate) mod avalue;
internal val avalue = "avalue"
// pub(crate) mod avalues;
internal val avalues = "avalues"
// pub(crate) mod complex;
internal val complex = "complex"
// mod const_frozen_string;
internal val const_frozen_string = "const_frozen_string"
// pub(crate) mod const_type_id;
internal val const_type_id = "const_type_id"
// pub(crate) mod freezer;
internal val freezer = "freezer"
// pub(crate) mod heap;
internal val heap = "heap"
// pub(crate) mod identity;
internal val identity = "identity"
// pub(crate) mod pointer;
internal val pointer = "pointer"
// pub(crate) mod static_string;
internal val static_string = "static_string"
// pub(crate) mod typed;
internal val typed = "typed"
// pub(crate) mod value;
internal val value = "value"
// pub(crate) mod value_alloc_size;
internal val value_alloc_size = "value_alloc_size"
// pub(crate) mod value_captured;
internal val value_captured = "value_captured"
// pub(crate) mod value_lifetimeless;
internal val value_lifetimeless = "value_lifetimeless"
// pub(crate) mod value_not_special;
internal val value_not_special = "value_not_special"
// pub(crate) mod vtable;
internal val vtable = "vtable"
