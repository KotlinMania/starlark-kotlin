// port-lint: source src/assert/conformance.rs
package io.github.kotlinmania.starlark.assert

import io.github.kotlinmania.starlark.Error

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
 * Run conformance tests, which are used by the Go starlark.
 * e.g. <https://github.com/google/skylark/tree/master/testdata>
 */

/** Run a conformance test, e.g. the Go Starlark tests */
// pub fn conformance(&self, code: &str)
fun Assert.conformance(code: String) {
    conformanceExcept(code, emptyList())
}

/**
 * Run a conformance test, but where some test cases are allowed to fail.
 * The `except` argument represents a list of these permissible failures,
 * in the order they occur in the conformance test set,
 * identified by a substring that occurs in the test.
 */
// pub fn conformance_except(&self, code: &str, except: &[&str])
fun Assert.conformanceExcept(code: String, except: List<String>) {
    val exceptIter = except.iterator()
    var nextExcept: String? = if (exceptIter.hasNext()) exceptIter.next() else null

    for (x in ConformanceTest.parse(code)) {
        if (nextExcept != null) {
            if (x.code.contains(nextExcept)) {
                nextExcept = if (exceptIter.hasNext()) exceptIter.next() else null
                continue
            }
        }

        x.test(this)
    }

    if (nextExcept != null) {
        error("Exception given but not used, `$nextExcept`")
    }
}

/** Describe a conformance test */
// struct ConformanceTest
private class ConformanceTest(
    /** The code of the test */
    val code: String,
    /** If this might throw an error, what is it */
    val errorInfo: Pair<Int, String>?,
) {
    companion object {
        // fn parse(code: &str) -> Vec<Self>
        fun parse(code: String): List<ConformanceTest> {
            // First split on "---"
            val lines = code.lines()
            val sections = mutableListOf<List<String>>()
            var current = mutableListOf<String>()
            for (line in lines) {
                if (line == "---") {
                    sections.add(current)
                    current = mutableListOf()
                } else {
                    current.add(line)
                }
            }
            sections.add(current)

            return sections.map { xs ->
                val codeStr = xs.joinToString("\n")
                var errorPair: Pair<Int, String>? = null
                for ((i, x) in xs.withIndex()) {
                    if (x.contains("###")) {
                        val after = x.substringAfter("###").trimStart()
                        errorPair = Pair(i + 1, after)
                        break
                    }
                }
                ConformanceTest(
                    code = codeStr,
                    errorInfo = errorPair,
                )
            }
        }
    }

    // fn test(&self, assert: &Assert)
    fun test(assert: Assert) {
        fun getLine(err: io.github.kotlinmania.starlark.Error): Int? {
            return err.span()?.resolveSpan()?.begin?.line?.let { it + 1 }
        }

        when (errorInfo) {
            null -> {
                assert.pass(code)
            }
            else -> {
                val (line, _msg) = errorInfo
                // We don't actually check error messages, since these tests were taken from upstream
                // and our error messages are different
                val err = assert.fail(code, "")
                val got = getLine(err)
                if (got != line) {
                    error(
                        "starlark::assert::conformance, failed at wrong line!\n" +
                            "Code:\n$code\n" +
                            "Error:\n$err\n" +
                            "Expected: $line\n" +
                            "Got: $got\n"
                    )
                }
            }
        }
    }
}
