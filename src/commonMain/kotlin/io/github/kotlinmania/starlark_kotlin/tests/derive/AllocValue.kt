// port-lint: source src/tests/derive/alloc_value.rs (tests)
package io.github.kotlinmania.starlark_kotlin.tests.derive

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

// Tests for `#[derive(AllocValue)]`.

// Only check it compiles.

// #[derive(StarlarkTypeRepr, AllocValue, AllocFrozenValue)]
// enum AllocNoVariant {}
@Suppress("unused")
private sealed class AllocNoVariant

// #[derive(StarlarkTypeRepr, AllocValue, AllocFrozenValue)]
// enum AllocOneVariant { Int(u32) }
@Suppress("unused")
private sealed class AllocOneVariant {
    class Int(val value: UInt) : AllocOneVariant()
}

// #[derive(StarlarkTypeRepr, AllocValue, AllocFrozenValue)]
// enum AllocTwoVariants { Int(u32), String(String) }
@Suppress("unused")
private sealed class AllocTwoVariants {
    class Int(val value: UInt) : AllocTwoVariants()
    class `String`(val value: String) : AllocTwoVariants()
}

// #[derive(StarlarkTypeRepr, AllocValue, AllocFrozenValue)]
// enum AllocWithLifetime<'v> { String(&'v str) }
@Suppress("unused")
private sealed class AllocWithLifetime {
    class `String`(val value: String) : AllocWithLifetime()
}
