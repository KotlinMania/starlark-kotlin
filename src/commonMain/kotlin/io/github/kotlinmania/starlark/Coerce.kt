package io.github.kotlinmania.starlark

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

/** Marker for values that can be viewed as [To] without changing representation. */
interface Coerce<To : Any?>

/** Marker for values that keep equivalent key ordering and equality when viewed as [To]. */
interface CoerceKey<To : Any?> : Coerce<To>

/** Same-type identity conversion. Cross-type conversions must be expressed with typed APIs. */
fun <T> coerce(x: T): T = x
