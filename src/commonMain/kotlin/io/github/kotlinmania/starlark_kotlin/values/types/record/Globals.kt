// port-lint: source src/values/types/record/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.record

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

/// Implementation of `record` function.

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.RecordTypeGen
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.function
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.fromValue
import io.github.kotlinmania.starlark_kotlin.tests.derive.freeze.checkType

// #[starlark_module]
// pub(crate) fn register_record(builder: &mut GlobalsBuilder)
internal fun registerRecord(builder: GlobalsBuilder) {
    /// A `record` type represents a set of named values, each with their own type.
    ///
    /// For example:
    ///
    /// ```python
    /// MyRecord = record(host=str, port=int)
    /// ```
    ///
    /// This above statement defines a record `MyRecord` with 2 fields, the first
    /// named `host` that must be of type `str`, and the second named `port` that
    /// must be of type `int`.
    ///
    /// Now `MyRecord` is defined, it's possible to do the following:
    ///
    /// * Create values of this type with `MyRecord(host="localhost", port=80)`.
    ///   It is a runtime error if any arguments are missed, of the wrong type,
    ///   or if any unexpected arguments are given.
    /// * Get the type of the record suitable for a type annotation with
    ///   `MyRecord.type`.
    /// * Get the fields of the record. For example,
    ///   `v = MyRecord(host="localhost", port=80)` will provide
    ///   `v.host == "localhost"` and `v.port == 80`. Similarly,
    ///   `dir(v) == ["host", "port"]`.
    ///
    /// It is also possible to specify default values for parameters using the
    /// `field` function.
    ///
    /// For example:
    ///
    /// ```python
    /// MyRecord = record(host=str, port=field(int, 80))
    /// ```
    ///
    /// Now the `port` field can be omitted, defaulting to `80` if not present
    /// (for example, `MyRecord(host="localhost").port == 80`).
    ///
    /// Records are stored deduplicating their field names, making them more
    /// memory efficient than dictionaries.
    // fn record<'v>(#[starlark(kwargs)] kwargs: SmallMap<String, Value<'v>>, eval: &mut Evaluator) -> anyhow::Result<RecordType<'v>>
    builder.function("record") { kwargs: SmallMap<String, Value>, eval: Evaluator ->
        // Every Value must either be a field or a value (the type)
        val mp = SmallMap<String, Field>()
        for ((k, v) in kwargs) {
            val field = Field.fromValue(v)
                ?: Field.new(
                    TypeCompiled.new(v, eval.heap()).getOrElse { return@function Result.failure(it) },
                    null,
                )
            mp.put(k, field)
        }
        Result.success(RecordTypeGen.new(mp))
    }

    /// Creates a field record. Used as an argument to the `record` function.
    ///
    /// ```
    /// rec_type = record(host=field(str), port=field(int), mask=field(int, default=255))
    /// rec = rec_type(host="localhost", port=80)
    /// rec.port == 80
    /// rec.mask == 255
    /// ```
    // fn field<'v>(#[starlark(require = pos)] typ: Value<'v>, default: Option<Value<'v>>, eval: &mut Evaluator) -> starlark::Result<Field<'v>>
    builder.function("field") { args: List<Value>, eval: Evaluator ->
        val typ = args[0]
        val default: Value? = args.getOrNull(1)
        // We compile the type even if we don't have a default to raise the error sooner
        val compiled = TypeCompiled.new(typ, eval.heap())
            .getOrElse { return@function Result.failure(it) }
        if (default != null) {
            compiled.checkType(default, "default")
                .getOrElse { return@function Result.failure(it) }
        }
        Result.success(Field.new(compiled, default))
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
