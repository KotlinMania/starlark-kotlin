// port-lint: source tests/derive/unpackValueAttr.rs
package io.github.kotlinmania.starlark.tests.derive

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

// Only check it compiles (test-only).

import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.Value

private class ValueWithLifetimeParam(val value: Value) : StarlarkValue {
    override val TYPE: String get() = "ValueWithLifetimeParam"
    override fun toString(): String = "ValueWithLifetimeParam"
}

private class ValueWithoutParam(val value: String) : StarlarkValue {
    override val TYPE: String get() = "ValueWithoutParam"
    override fun toString(): String = "ValueWithoutParam"
}
