// port-lint: source src/hint.rs
package io.github.kotlinmania.starlark_kotlin.hint

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

// #[cfg(rust_nightly)]
// pub(crate) use std::intrinsics::likely;
// #[cfg(rust_nightly)]
// pub(crate) use std::intrinsics::unlikely;

// #[cfg(not(rust_nightly))]
// #[inline]
// pub(crate) fn likely(b: bool) -> bool { b }
internal inline fun likely(b: Boolean): Boolean = b

// #[cfg(not(rust_nightly))]
// #[inline]
// pub(crate) fn unlikely(b: bool) -> bool { b }
internal inline fun unlikely(b: Boolean): Boolean = b
