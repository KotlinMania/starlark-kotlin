// port-lint: source src/eval/runtime/profile/csv.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.csv

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

//! Write CSV files.

import io.github.kotlinmania.starlark_kotlin.eval.runtime.small_duration.SmallDuration
import io.github.kotlinmania.starlark_kotlin.values.types.string.format

// fn quote_str_for_csv(s: &str) -> String
internal fun quoteStrForCsv(s: String): String =
    "\"${s.replace("\"", "\"\"")}\""

/// Writer for CSV files.
// pub(crate) struct CsvWriter
internal class CsvWriter(
    columns: List<String>,
) {
    /// Column count in CSV file.
    private val columnCount: Int
    /// While writing a row, this is the current column index.
    private var currentColumnIndex: Int = 0
    /// Write CSV there.
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

    // pub(crate) fn write_value(&mut self, value: impl CsvValue)
    fun writeValue(value: Any) {
        check(currentColumnIndex < columnCount)
        if (currentColumnIndex != 0) {
            buf.append(',')
        }
        buf.append(formatCsvValue(value))
        currentColumnIndex++
    }

    // pub(crate) fn write_display(&mut self, value: impl Display)
    fun writeDisplay(value: Any) {
        writeValue(QuotedCsvValue(value.toString()))
    }

    // pub(crate) fn write_debug(&mut self, value: impl Debug)
    fun writeDebug(value: Any) {
        writeValue(QuotedCsvValue(value.toString()))
    }

    // pub(crate) fn finish_row(&mut self)
    fun finishRow() {
        check(currentColumnIndex == columnCount)
        currentColumnIndex = 0
        buf.append('\n')
    }

    // pub(crate) fn finish(self) -> String
    fun finish(): String {
        check(currentColumnIndex == 0)
        return buf.toString()
    }
}

// Helper to wrap pre-quoted values
private class QuotedCsvValue(val quoted: String)

// pub(crate) trait CsvValue + impl for various types
private fun formatCsvValue(value: Any): String = when (value) {
    is SmallDuration -> "%.3f".format(value.toDuration().inWholeMilliseconds / 1000.0)
    is String -> quoteStrForCsv(value)
    is Int -> value.toString()
    is Long -> value.toString()
    is ULong -> value.toString()
    is QuotedCsvValue -> quoteStrForCsv(value.quoted)
    else -> value.toString()
}

// --- Tests ---

// #[test] fn test_csv_writer()
internal fun testCsvWriter() {
    val csv = CsvWriter(listOf("File", "Count", "Duration"))
    csv.writeValue("a.bzl")
    csv.writeValue(10)
    csv.writeValue(SmallDuration(nanos = 17_000_000u))
    csv.finishRow()
    csv.writeValue("b.bzl")
    csv.writeValue(20)
    csv.writeValue(SmallDuration(nanos = 19_000_000u))
    csv.finishRow()
    check(
        "File,Count,Duration\n\"a.bzl\",10,0.017\n\"b.bzl\",20,0.019\n" == csv.finish()
    )
}

// #[test] fn test_quote_str_for_csv()
internal fun testQuoteStrForCsv() {
    check("\"a\"" == quoteStrForCsv("a"))
    check("\"a\"\"\"" == quoteStrForCsv("a\""))
}
