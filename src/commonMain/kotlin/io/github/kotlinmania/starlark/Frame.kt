// port-lint: source ../starlark_syntax/src/frame.rs
package io.github.kotlinmania.starlark

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

import io.github.kotlinmania.starlark.codemap.FileSpan

/** A frame of the call-stack. */
data class Frame(
    /** The name of the entry on the call-stack. */
    val name: String,
    /** The location of the definition, or `null` for native Kotlin functions. */
    val location: FileSpan?,
) {
    override fun toString(): String {
        val sb = StringBuilder(name)
        if (location != null) {
            sb.append(" (called from $location)")
        }
        return sb.toString()
    }

    fun writeTwoLines(
        indent: String,
        caller: String,
        write: StringBuilder,
    ) {
        if (location != null) {
            val rawLine = location.file.sourceLine(location.file.findLine(location.span.begin)).trim()
            val (line, ddd) = truncateSnippet(rawLine, 80)
            write.append(indent)
            write.append("* ")
            write.append(location.resolve().beginFileLine())
            write.append(", in ")
            // Note we print caller function here as in Python, not callee,
            // so in the stack trace, top frame is printed without executed function name.
            write.append(caller)
            write.append('\n')
            write.append(indent)
            write.append("    ")
            write.append(line)
            write.append(ddd)
            write.append('\n')
        } else {
            // Python just omits builtin functions in the traceback.
            write.append(indent)
            write.append("File <builtin>, in ")
            write.append(caller)
            write.append('\n')
        }
    }
}

internal fun truncateSnippet(snippet: String, maxLen: Int): Pair<String, String> {
    val ddd = "..."
    require(maxLen >= ddd.length)
    val cutIdx = maxLen - ddd.length
    if (snippet.length <= cutIdx) {
        return Pair(snippet, "")
    }
    val tail = snippet.substring(cutIdx)
    if (tail.length < 4) {
        return Pair(snippet, "")
    }
    return Pair(snippet.substring(0, cutIdx), "...")
}
