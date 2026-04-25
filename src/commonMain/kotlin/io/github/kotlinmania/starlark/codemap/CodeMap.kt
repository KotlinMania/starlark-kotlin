// port-lint: source src/codemap.rs
package io.github.kotlinmania.starlark.codemap

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

import kotlin.concurrent.Volatile

/** A cheap unique identifier per CodeMap, used for profiling optimisations. */
// In Rust this is a pointer-based identity (CodeMapId). In Kotlin we use an
// incrementing counter so that each CodeMap instance gets a unique id.
data class CodeMapId(val value: Long) {
    companion object {
        val EMPTY: CodeMapId = CodeMapId(0L)
        @Volatile
        private var nextId: Long = 1L

        internal fun next(): CodeMapId {
            val id = nextId
            nextId = id + 1
            return CodeMapId(id)
        }
    }
}

/** Multiple [CodeMap]s, keyed by [CodeMapId]. */
// pub struct CodeMaps { codemaps: HashMap<CodeMapId, CodeMap> }
class CodeMaps {
    private val codemaps: MutableMap<CodeMapId, CodeMap> = mutableMapOf()

    /** Lookup by id. */
    fun get(id: CodeMapId): CodeMap? = codemaps[id]

    /** Add codemap if not already present. */
    fun add(codemap: CodeMap) {
        val id = codemap.id()
        if (id !in codemaps) {
            codemaps[id] = codemap
        }
    }

    /** Add all codemaps. */
    fun addAll(codemaps: CodeMaps) {
        for (codemap in codemaps.codemaps.values) {
            add(codemap)
        }
    }
}

class CodeMap(
    val filename: String,
    val source: String
) {
    val lines: List<Pos>
    private val _id: CodeMapId = CodeMapId.next()

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

    /** Only used internally for profiling optimisations. */
    fun id(): CodeMapId = _id

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

    private fun findLineCol(pos: Pos): ResolvedPos {
        val line = findLine(pos)
        val lineSpan = lineSpan(line)
        val byteCol = pos.value - lineSpan.begin.value
        val column = sourceSpan(lineSpan).substring(0, byteCol).length
        return ResolvedPos(line = line, column = column)
    }

    /** Gets the file and its line and column ranges represented by a Span. */
    fun resolveSpan(span: Span): ResolvedSpan {
        val begin = findLineCol(span.begin)
        val end = findLineCol(span.end)
        return ResolvedSpan(begin = begin, end = end)
    }

    /** Filename method (mirrors Rust's CodeMap::filename()). */
    fun filename(): String = filename

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CodeMap) return false
        return _id == other._id
    }

    override fun hashCode(): Int = _id.hashCode()

    override fun toString(): String = "CodeMap(\"$filename\")"
}

data class FileSpan(
    val file: CodeMap,
    val span: Span
) : Comparable<FileSpan> {
    fun sourceSpan(): String = file.sourceSpan(span)

    /** Resolve the span to lines and columns. */
    fun resolveSpan(): ResolvedSpan = file.resolveSpan(span)

    /** Resolve the span to a [ResolvedFileSpan]. */
    fun resolve(): ResolvedFileSpan = ResolvedFileSpan(
        file = file.filename,
        span = file.resolveSpan(span),
    )

    override fun compareTo(other: FileSpan): Int {
        val fc = file.filename.compareTo(other.file.filename)
        if (fc != 0) return fc
        val sc = span.compareTo(other.span)
        if (sc != 0) return sc
        return file.id().value.compareTo(other.file.id().value)
    }

    override fun toString(): String = "${file.filename}:${resolveSpan()}"
}

/**
 * A file, and a line and column range within it.
 * In Rust this is a borrowing reference (`&CodeMap`), but in Kotlin
 * it is structurally identical to [FileSpan].
 */
class FileSpanRef(
    val file: CodeMap,
    val span: Span,
)
