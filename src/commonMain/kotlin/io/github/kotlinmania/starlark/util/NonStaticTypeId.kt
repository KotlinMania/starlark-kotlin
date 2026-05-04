// port-lint: source util/nonStaticTypeId.rs
package io.github.kotlinmania.starlark.util.nonstatictypeid

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import kotlin.reflect.KClass

// This module is test-only. Kotlin uses [KClass] as the runtime type identifier;
// type erasure removes generic parameter information at runtime.

/**
 * Get the runtime type identifier for a type.
 */
internal inline fun <reified T : Any> nonStaticTypeId(): KClass<T> {
    return T::class
}

// Tests are in commonTest, not here.
