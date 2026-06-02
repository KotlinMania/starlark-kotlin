// port-lint: source src/eval/runtime/profile/csv.rs
package io.github.kotlinmania.starlark.eval.runtime.profile.csv

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

/** Write CSV files. */

import io.github.kotlinmania.starlark.eval.runtime.SmallDuration

internal fun quoteStrForCsv(s: String): String =
    "\"${s.replace("\"", "\"\"")}\""

/** Writer for CSV files. */
internal class CsvWriter(
    columns: List<String>,
) {
    /** Column count in CSV file. */
    private val columnCount: Int

    /** While writing a row, this is the current column index. */
    private var currentColumnIndex: Int = 0

    /** Write CSV there. */
    private val buf: StringBuilder = StringBuilder()

    init {
        columnCount = columns.size
        for ((i, column) in columns.withIndex()) {
            if (i != 0) {
                buf.append(',')
            }
            buf.append(column)
        }
        buf.append('\n')
    }

    fun writeValue(value: Any) {
        check(currentColumnIndex < columnCount)
        if (currentColumnIndex != 0) {
            buf.append(',')
        }
        buf.append(formatCsvValue(value))
        currentColumnIndex++
    }

    fun writeDisplay(value: Any) {
        writeValue(QuotedCsvValue(value.toString()))
    }

    fun writeDebug(value: Any) {
        writeValue(QuotedCsvValue(value.toString()))
    }

    fun finishRow() {
        check(currentColumnIndex == columnCount)
        currentColumnIndex = 0
        buf.append('\n')
    }

    fun finish(): String {
        check(currentColumnIndex == 0)
        return buf.toString()
    }
}

// Helper to wrap pre-quoted values
private class QuotedCsvValue(
    val quoted: String,
)

private fun formatCsvValue(value: Any): String =
    when (value) {
        is SmallDuration ->
            run {
                val s = value.toDuration().inWholeMilliseconds / 1000.0
                "${((s * 1000).toLong() / 1000.0)}"
            }
        is String -> quoteStrForCsv(value)
        is Int -> value.toString()
        is Long -> value.toString()
        is ULong -> value.toString()
        is QuotedCsvValue -> quoteStrForCsv(value.quoted)
        else -> value.toString()
    }
