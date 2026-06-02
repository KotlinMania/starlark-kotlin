// port-lint: source src/values/types/record/matcher.rs
package io.github.kotlinmania.starlark.values.types.record

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

import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import io.github.kotlinmania.starlark.values.typing.type_compiled.TypeMatcher

data class RecordTypeMatcher(
    val id: TypeInstanceId,
) : TypeMatcher {
    // impl TypeMatcher for RecordTypeMatcher
    override fun matches(value: Value): Boolean {
        val record = Record.fromValue(value) ?: return false
        return record.recordTypeId() == id
    }
}
