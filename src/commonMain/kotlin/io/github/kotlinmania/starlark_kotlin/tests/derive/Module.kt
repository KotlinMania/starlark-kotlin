// port-lint: source src/tests/derive/module.rs
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

/**
 * Tests for the `#[starlark_module]` derive macro.
 *
 * This module mirrors `src/tests/derive/module.rs` which declares test submodules.
 *
 * ## Submodules
 *
 * | Rust submodule       | Kotlin package                              |
 * |----------------------|---------------------------------------------|
 * | `basic`              | `tests.derive.module.basic`                 |
 * | `default_value`      | `tests.derive.module.defaultValue`          |
 * | `generic`            | `tests.derive.module.generic`               |
 * | `kwargs`             | `tests.derive.module.kwargs`                |
 * | `methods`            | `tests.derive.module.methods`               |
 * | `named_positional`   | `tests.derive.module.namedPositional`       |
 * | `other_attributes`   | `tests.derive.module.otherAttributes`       |
 * | `return_impl`        | `tests.derive.module.returnImpl`            |
 * | `special_params`     | `tests.derive.module.specialParams`         |
 * | `type_annotation`    | `tests.derive.module.typeAnnotation`        |
 * | `unpack_value`       | `tests.derive.module.unpackValue`           |
 */
