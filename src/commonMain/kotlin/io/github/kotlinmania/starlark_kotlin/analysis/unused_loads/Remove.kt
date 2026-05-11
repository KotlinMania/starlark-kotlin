<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/analysis/unusedloads/Remove.kt
// port-lint: source analysis/unused_loads/remove.rs
package io.github.kotlinmania.starlark.analysis.unusedloads
=======
// port-lint: source src/analysis/unused_loads/remove.rs
package io.github.kotlinmania.starlark_kotlin.analysis.unused_loads
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/analysis/unused_loads/Remove.kt

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

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/analysis/unusedloads/Remove.kt
import io.github.kotlinmania.starlarksyntax.codemap.Pos as Pos
import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
=======
import io.github.kotlinmania.starlark_kotlin.codemap.Pos
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Span
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/analysis/unused_loads/Remove.kt

/**
 * Helper for building the output string with span-based skipping.
 *
 * struct Out<'a> { codemap: &'a CodeMap, out: String, pos: Pos }
 */
private class Out(
    val codemap: CodeMap,
    val out: StringBuilder = StringBuilder(),
    var pos: Pos = Pos(0),
) {
    /**
     * fn append_to(&mut self, pos: Pos)
     *
     * Append source text from current position up to [pos], then advance.
     */
    fun appendTo(pos: Pos) {
        check(this.pos <= pos)
        check(pos <= codemap.fullSpan().end())
        out.append(codemap.sourceSpan(Span(this.pos, pos)))
        this.pos = pos
    }

    /**
     * fn skip_to(&mut self, pos: Pos)
     *
     * Advance the current position to [pos] without appending.
     */
    fun skipTo(pos: Pos) {
        check(this.pos <= pos)
        check(pos <= codemap.fullSpan().end())
        this.pos = pos
    }

    /**
     * Append to the beginning of the span, and set the position to the end of the span.
     *
     * fn skip_span(&mut self, span: Span)
     */
    fun skipSpan(span: Span) {
        appendTo(span.begin())
        skipTo(span.end())
    }
}

/**
 * Return `null` if there are no unused loads.
 *
 * pub fn remove_unused_loads(name: &str, program: &str) -> crate::Result<Option<String>>
 */
fun removeUnusedLoads(name: String, program: String): Result<String?> {
    val (codemap, unusedLoads) = findUnusedLoads(name, program).getOrElse {
        return Result.failure(it)
    }
    if (unusedLoads.isEmpty()) {
        return Result.success(null)
    }

    val out = Out(
        codemap = codemap.value,
    )

    for (load in unusedLoads) {
        if (load.allUnused()) {
            out.skipSpan(load.load.span)
        } else {
            for (arg in load.unusedArgs) {
                out.skipSpan(arg.spanWithTrailingComma())
            }
        }
    }

    out.appendTo(codemap.value.fullSpan().end)

    return Result.success(out.out.toString())
}
