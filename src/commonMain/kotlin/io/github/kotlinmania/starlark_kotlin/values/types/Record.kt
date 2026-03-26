// port-lint: source src/values/types/record.rs
package io.github.kotlinmania.starlark_kotlin.values.types.record

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

/**
 * A `record` type, comprising of a fixed set of fields.
 *
 * Calling `record()` produces a `RecordType`. Calling `RecordType` produces a `Record`.
 * The field names of the record are only stored once, potentially reducing memory usage.
 */

// Rust mod declarations — in Kotlin, these are separate files in the record/ package.
// mod field
// mod globals
// mod instance
// mod matcher
// mod record_type
// mod ty_record_type
// pub use instance::Record
