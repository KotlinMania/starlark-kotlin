// port-lint: source tests:src/eval/runtime/profile/csv.rs
package io.github.kotlinmania.starlark.eval.runtime.profile

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.eval.runtime.SmallDuration
import io.github.kotlinmania.starlark.eval.runtime.profile.csv.CsvWriter
import io.github.kotlinmania.starlark.eval.runtime.profile.csv.quoteStrForCsv
import kotlin.test.Test
import kotlin.test.assertEquals

class CsvTest {
    @Test
    fun testCsvWriter() {
        val csv = CsvWriter(listOf("File", "Count", "Duration"))
        csv.writeValue("a.bzl")
        csv.writeValue(10)
        csv.writeValue(SmallDuration(nanos = 17_000_000UL))
        csv.finishRow()
        csv.writeValue("b.bzl")
        csv.writeValue(20)
        csv.writeValue(SmallDuration(nanos = 19_000_000UL))
        csv.finishRow()
        assertEquals(
            "File,Count,Duration\n" +
                "\"a.bzl\",10,0.017\n" +
                "\"b.bzl\",20,0.019\n",
            csv.finish(),
        )
    }

    @Test
    fun testQuoteStrForCsv() {
        assertEquals("\"a\"", quoteStrForCsv("a"))
        assertEquals("\"a\"\"\"", quoteStrForCsv("a\""))
    }
}
