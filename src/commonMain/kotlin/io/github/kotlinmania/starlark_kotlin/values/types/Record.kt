// port-lint: source src/values/types/record.rs
@file:OptIn(ExperimentalStdlibApi::class)

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
 * Calling `record()` produces a [RecordType]. Calling [RecordType] produces a [Record].
 * The field names of the record are only stored once, potentially reducing memory usage.
 * Created in Starlark using the `record()` function, which accepts keyword arguments.
 * The keys become field names, and values are the types. Calling the resulting
 * function produces an actual record.
 *
 * ```
 * IpAddress = record(host=str, port=int)
 * rec = IpAddress(host="localhost", port=80)
 * rec.port == 80
 * ```
 *
 * It is also possible to use `field(type, default)` type to give defaults:
 *
 * ```
 * IpAddress = record(host=str, port=field(int, 80))
 * rec = IpAddress(host="localhost")
 * rec.port == 80
 * ```
 *
 * Submodules:
 * - field: [Field] / [FieldGen], the result of calling `field()`
 * - globals: [registerRecord], registers the `record` and `field` global functions
 * - instance: [RecordGen] / [Record] / [FrozenRecord], an actual record instance
 * - matcher: [RecordTypeMatcher], type matcher for record instances
 * - record_type: [RecordTypeGen] / [RecordType] / [FrozenRecordType], the type of records
 * - ty_record_type: [TyRecordData], typechecking data for a record type
 *
 * Re-export: `pub use crate::values::record::instance::Record`
 * In Kotlin, [Record] is a typealias for [RecordGen] defined in `Instance.kt`.
 */

// Submodule declarations: In Rust these are `pub(crate) mod` items.
// In Kotlin, each submodule is a separate file in the record/ package.

// pub(crate) mod field
typealias FieldSubmodule = Field

// pub(crate) mod globals
typealias GlobalsSubmodule = Unit

// pub(crate) mod instance
typealias InstanceSubmodule = RecordGen

// pub(crate) mod matcher
typealias MatcherSubmodule = RecordTypeMatcher

// pub(crate) mod record_type
typealias RecordTypeSubmodule = io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.RecordTypeGen

// pub(crate) mod ty_record_type
typealias TyRecordTypeSubmodule = TyRecordData

// Re-exports from submodules.
// pub use crate::values::record::instance::Record
typealias RecordReExport = RecordGen
// RecordTypeGen (Rust has RecordType = RecordTypeGen<FrozenValue>, but we just alias directly)
typealias RecordTypeReExport = io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.RecordTypeGen
// RecordTypeGen
typealias RecordTypeGenReExport = io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.RecordTypeGen
// TyRecordData
typealias TyRecordDataReExport = TyRecordData
// RecordTypeMatcher
typealias MatcherReExport = RecordTypeMatcher
// recordFields
typealias RecordFieldsReExport = Field
// FrozenRecord
typealias FrozenRecordReExport = RecordGen
// FieldDefault
typealias FieldDefaultReExport = Field
// RecordInstance
typealias RecordInstanceReExport = RecordGen
