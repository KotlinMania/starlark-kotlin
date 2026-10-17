// port-lint: source src/analysis/lint_message.rs
package io.github.kotlinmania.starlark.analysis

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

import io.github.kotlinmania.either.wrapSerialize
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer

/**
 * A JSON-deriving type that gives a stable interface to downstream types.
 * Do NOT change this type, change Message instead.
 *
 * [Linter JSON format](https://www.internalfb.com/intern/wiki/Linting/adding-linters/).
 */
data class LintMessage(
    val path: String,
    val line: Int?,
    val char: Int?,
    val code: String,
    val severity: EvalSeverity,
    val name: String,
    val descriptionText: String?,
    val original: String?,
) : Serialize {
    companion object {
        /**
         * Construct from an [EvalMessage].
         */
        fun new(x: EvalMessage): LintMessage =
            LintMessage(
                path = x.path,
                line =
                    x.span
                        ?.span
                        ?.begin
                        ?.line
                        ?.plus(1),
                char =
                    x.span
                        ?.span
                        ?.begin
                        ?.column
                        ?.plus(1),
                code = "STARLARK",
                severity = x.severity,
                name = x.name,
                descriptionText = x.descriptionText,
                original = x.original,
            )
    }

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serdeCatching {
            var count = 6
            if (descriptionText != null) count++
            if (original != null) count++
            val s = serializer.serializeStruct("LintMessage", count).getOrThrow()
            s.serializeField("path", wrapSerialize(path)).getOrThrow()
            s.serializeField("line", wrapSerialize(line)).getOrThrow()
            s.serializeField("char", wrapSerialize(char)).getOrThrow()
            s.serializeField("code", wrapSerialize(code)).getOrThrow()
            s.serializeField("severity", severity).getOrThrow()
            s.serializeField("name", wrapSerialize(name)).getOrThrow()
            if (descriptionText != null) {
                s.serializeField("description", wrapSerialize(descriptionText)).getOrThrow()
            }
            if (original != null) {
                s.serializeField("original", wrapSerialize(original)).getOrThrow()
            }
            s.end().getOrThrow()
        }
}
