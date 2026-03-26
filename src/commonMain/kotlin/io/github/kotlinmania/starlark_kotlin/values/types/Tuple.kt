// port-lint: source src/values/types/tuple.rs
package io.github.kotlinmania.starlark_kotlin.values.types.tuple

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

/** The tuple type, an immutable sequence of values. */

// Rust mod declarations — in Kotlin, these are separate files in the tuple/ package.
// pub(crate) mod alloc
// pub(crate) mod globals
// pub(crate) mod refs
// pub(crate) mod rust_tuple
// pub(crate) mod unpack
// pub(crate) mod value
// pub use AllocTuple
// pub use FrozenTupleRef
// pub use TupleRef
// pub use UnpackTuple
