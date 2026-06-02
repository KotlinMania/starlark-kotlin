// port-lint: source src/read_line.rs
package io.github.kotlinmania.starlark.readline

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

// This is not public API, but it is used by Starlark command line utility.

// Kotlin: simple stdin-based readline. No rustyline equivalent in KMP.

/** Wrapper for the readline library, whichever we are using at the moment. */
//     editor: Editor<(), DefaultHistory>,
class ReadLine private constructor(
    private val histfileEnv: String?,
) {
    companion object {
        fun new(histfileEnv: String): ReadLine = ReadLine(histfileEnv = histfileEnv)
    }

    /** Read line. Return `null` on EOF or interrupt. */
    fun readLine(prompt: String): String? {
        print(prompt)
        return kotlin.io.readlnOrNull()
    }
}
