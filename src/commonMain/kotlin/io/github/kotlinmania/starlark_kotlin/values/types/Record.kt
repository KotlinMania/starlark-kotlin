// port-lint: source src/values/types/record.rs
@file:Suppress("unused")

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
 * Calling `record()` produces a `RecordType`. Calling `RecordType` produces a [Record].
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
 * - field: [FieldGen], the result of calling `field()`
 * - globals: [registerRecord] for global functions
 * - instance: [RecordGen] / [Record], an actual record instance
 * - matcher: [RecordTypeMatcher] for type matching
 * - record_type: [RecordTypeGen] / [RecordType], the type of records
 * - ty_record_type: [TyRecordData] for typechecking
 *
 * Re-export: `pub use crate::values::record::instance::Record`
 * In Kotlin, [Record] is a typealias for [RecordGen] defined in `Instance.kt`.
 */

// mod field
typealias FieldModule = Field
// mod globals
typealias GlobalsModule = Unit
// mod instance
typealias InstanceModule = RecordGen
// mod matcher
typealias MatcherModule = RecordTypeMatcher
// mod record_type
typealias RecordTypeModule = record_type.RecordTypeGen
// mod ty_record_type
typealias TyRecordTypeModule = TyRecordData

// pub use record::instance::Record
typealias RecordExport = RecordGen
typealias FieldGenExport = Field
typealias RecordTypeExport = record_type.RecordType
typealias FrozenRecordTypeExport = record_type.FrozenRecordType
typealias RecordTypeGenExport = record_type.RecordTypeGen
typealias TyRecordDataExport = TyRecordData
typealias RecordMatcherExport = RecordTypeMatcher
typealias RecordFieldsExport = Field
typealias FrozenRecordExport = RecordGen
typealias FieldDefaultExport = Field
typealias RecordTypeIdExport = RecordTypeMatcher
typealias ParameterSpecExport = TyRecordData
typealias RecordValuesExport = RecordGen
typealias RecordInstanceExport = RecordGen
