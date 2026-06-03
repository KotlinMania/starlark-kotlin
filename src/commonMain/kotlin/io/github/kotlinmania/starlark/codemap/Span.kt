// port-lint: source src/codemap.rs
package io.github.kotlinmania.starlark.codemap

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

import kotlin.math.max
import kotlin.math.min

// / A small, `Copy`, value representing a position in a `CodeMap`'s file.
data class Pos(
    val value: Int,
) : Comparable<Pos> {
    operator fun plus(other: Int): Pos = Pos(value + other)

    operator fun minus(other: Int): Pos = Pos(value - other)

    operator fun minus(other: Pos): Int = value - other.value

    override fun compareTo(other: Pos): Int = value.compareTo(other.value)
}

// / A range of text within a CodeMap.
data class Span(
    val begin: Pos,
    val end: Pos,
) : Comparable<Span> {
    init {
        require(begin <= end) { "Span end must be >= begin" }
    }

    fun merge(other: Span): Span =
        Span(
            begin = Pos(min(begin.value, other.begin.value)),
            end = Pos(max(end.value, other.end.value)),
        )

    fun endSpan(): Span = Span(end, end)

    fun contains(pos: Pos): Boolean = pos in begin..end

    fun intersects(span: Span): Boolean =
        contains(span.begin) || contains(span.end) || span.contains(begin)

    override fun compareTo(other: Span): Int {
        val beginCmp = begin.compareTo(other.begin)
        if (beginCmp != 0) return beginCmp
        return end.compareTo(other.end)
    }

    companion object {
        val DEFAULT = Span(Pos(0), Pos(0))

        fun mergeAll(spans: Iterator<Span>): Span {
            if (!spans.hasNext()) return DEFAULT
            var result = spans.next()
            while (spans.hasNext()) {
                result = result.merge(spans.next())
            }
            return result
        }
    }
}

data class Spanned<out T>(
    // / Data in the node.
    val node: T,
    val span: Span,
) {
    fun <U> map(f: (T) -> U): Spanned<U> = Spanned(f(node), span)
}

data class ResolvedPos(
    /** The line number within the file (0-indexed). */
    val line: Int,
    /** The column within the line (0-indexed in characters). */
    val column: Int,
) : Comparable<ResolvedPos> {
    override fun toString(): String = "${line + 1}:${column + 1}"

    override fun compareTo(other: ResolvedPos): Int {
        val lc = line.compareTo(other.line)
        if (lc != 0) return lc
        return column.compareTo(other.column)
    }
}

data class ResolvedSpan(
    val begin: ResolvedPos,
    val end: ResolvedPos,
) {
    override fun toString(): String {
        val singleLine = begin.line == end.line
        val isEmpty = singleLine && begin.column == end.column

        return when {
            isEmpty -> "${begin.line + 1}:${begin.column + 1}"
            singleLine -> "$begin-${end.column + 1}"
            else -> "$begin-$end"
        }
    }

    fun contains(pos: ResolvedPos): Boolean =
        (begin.line < pos.line || (begin.line == pos.line && begin.column <= pos.column)) &&
            (end.line > pos.line || (end.line == pos.line && end.column >= pos.column))
}

data class ResolvedFileLine(
    val file: String,
    val line: Int,
) {
    override fun toString(): String = "$file:${line + 1}"
}

data class ResolvedFileSpan(
    val file: String,
    val span: ResolvedSpan,
) : Comparable<ResolvedFileSpan> {
    fun beginFileLine() = ResolvedFileLine(file, span.begin.line)

    override fun toString(): String = "$file:$span"

    override fun compareTo(other: ResolvedFileSpan): Int {
        val fc = file.compareTo(other.file)
        if (fc != 0) return fc
        val blc = span.begin.line.compareTo(other.span.begin.line)
        if (blc != 0) return blc
        val bcc = span.begin.column.compareTo(other.span.begin.column)
        if (bcc != 0) return bcc
        val elc = span.end.line.compareTo(other.span.end.line)
        if (elc != 0) return elc
        return span.end.column.compareTo(other.span.end.column)
    }
}
