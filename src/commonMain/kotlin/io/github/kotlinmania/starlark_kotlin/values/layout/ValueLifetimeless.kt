// port-lint: source src/values/layout/value_lifetimeless.rs
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

import io.github.kotlinmania.starlark_kotlin.sealed.Sealed
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue

/**
 * Implemented by [Value] and [FrozenValue].
 *
 * Rust supertrait bounds (for reference):
 * `Sealed + Eq + Copy + Dupe + Debug + Default + Display + Serialize + Allocative + Freeze<Frozen = FrozenValue> + Sized`
 *
 * In Kotlin, `Eq` maps to `equals`/`hashCode`, `Debug`/`Display` map to `toString`,
 * `Copy`/`Dupe`/`Sized` are implicit. `Serialize` and `Allocative` are handled separately.
 */
interface ValueLifetimeless : Sealed, Freeze
