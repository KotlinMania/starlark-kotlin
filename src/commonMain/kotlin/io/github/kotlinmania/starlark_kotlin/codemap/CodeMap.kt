// port-lint: source src/codemap.rs
package io.github.kotlinmania.starlark_kotlin.codemap

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

class CodeMap(
    val filename: String,
    val source: String
) {
    val lines: List<Pos>

    init {
        val linePositions = mutableListOf(Pos(0))
        var index = 0
        while (index < source.length) {
            val nextNewline = source.indexOf('\n', index)
            if (nextNewline == -1) break
            linePositions.add(Pos(nextNewline + 1))
            index = nextNewline + 1
        }
        lines = linePositions
    }

    fun fullSpan(): Span = Span(Pos(0), Pos(source.length))

    fun fileSpan(span: Span): FileSpan = FileSpan(this, span)

    fun findLine(pos: Pos): Int {
        val searchIndex = lines.binarySearch(pos)
        return if (searchIndex >= 0) searchIndex else -searchIndex - 2
    }

    fun sourceSpan(span: Span): String = source.substring(span.begin.value, span.end.value)

    fun lineSpanOpt(line: Int): Span? {
        if (line < 0 || line >= lines.size) return null
        val begin = lines[line]
        val end = if (line + 1 < lines.size) lines[line + 1] else fullSpan().end
        return Span(begin, end)
    }

    fun lineSpan(line: Int): Span = lineSpanOpt(line) ?: throw IllegalArgumentException("Line $line out of bounds")

    fun sourceLine(line: Int): String {
        return sourceSpan(lineSpan(line)).trimEnd('\r', '\n')
    }
}

data class FileSpan(
    val file: CodeMap,
    val span: Span
) {
    fun sourceSpan(): String = file.sourceSpan(span)
}
