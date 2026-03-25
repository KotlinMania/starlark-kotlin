// port-lint: source src/analysis/lint_message.rs
package io.github.kotlinmania.starlark_kotlin.analysis

import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.allocator.alloc.begin


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

/// A JSON-deriving type that gives a stable interface to downstream types.
/// Do NOT change this type, change Message instead.
// #[derive(Debug, Clone, Serialize)]
// pub struct LintMessage
class LintMessage(
    val path: String,
    val line: Int?,
    val char_: Int?,
    val code: String,
    val severity: EvalSeverity,
    val name: String,
    val description: String?,
    val original: String?,
) {
    companion object {
        /// Construct from an [`EvalMessage`].
        // pub fn new(x: EvalMessage) -> Self
        fun new(x: EvalMessage): LintMessage {
            return LintMessage(
                path = x.path,
                line = x.span?.begin?.line?.plus(1),
                char_ = x.span?.begin?.column?.plus(1),
                code = "STARLARK",
                severity = x.severity,
                name = x.name,
                description = x.description,
                original = x.original,
            )
        }
    }

    override fun toString(): String {
        return "LintMessage(path=$path, line=$line, char=$char_, code=$code, severity=$severity, name=$name)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LintMessage) return false
        return path == other.path &&
            line == other.line &&
            char_ == other.char_ &&
            code == other.code &&
            severity == other.severity &&
            name == other.name &&
            description == other.description &&
            original == other.original
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + (line ?: 0)
        result = 31 * result + (char_ ?: 0)
        result = 31 * result + code.hashCode()
        result = 31 * result + severity.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (original?.hashCode() ?: 0)
        return result
    }
}
