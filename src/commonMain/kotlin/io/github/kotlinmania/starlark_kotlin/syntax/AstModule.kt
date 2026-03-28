// port-lint: source src/syntax/module.rs
package io.github.kotlinmania.starlark_kotlin.syntax

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

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmt
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.codemap.Span

class AstLoad(
    val span: FileSpan,
    val moduleId: String,
    val symbols: Map<String, String>
)

class AstModule(
    val codemap: CodeMap,
    val statement: AstStmt,
    val dialect: Dialect,
    val typecheck: Boolean
) {
    companion object {
        fun parse(filename: String, content: String, dialect: Dialect): Result<AstModule> {
            // TODO: handwritten Kotlin recursive descent parser
            return Result.failure(NotImplementedError("Porting pending: Handwritten parser needed to replace LALRPOP."))
        }
    }

    fun loads(): List<AstLoad> {
        val loads = mutableListOf<AstLoad>()
        fun walk(ast: AstStmt) {
            when (val node = ast.node) {
                is StmtP.Load<*, *> -> {
                    loads.add(
                        AstLoad(
                            span = FileSpan(codemap, node.loadStmt.module.span),
                            moduleId = node.loadStmt.module.node,
                            symbols = node.loadStmt.args.associate { 
                                it.local.ident to it.their.node 
                            }
                        )
                    )
                }
                is StmtP.Statements<*> -> {
                    for (stmt in node.stmts) {
                        walk(stmt as AstStmt)
                    }
                }
                else -> {}
            }
        }
        walk(statement)
        return loads
    }

    fun fileSpan(span: Span): FileSpan = codemap.fileSpan(span)

    fun stmtLocations(): List<FileSpan> {
        val res = mutableListOf<FileSpan>()
        fun walk(ast: AstStmt) {
            if (ast.node !is StmtP.Statements<*>) {
                res.add(FileSpan(codemap, ast.span))
            }
            // we should descend if possible (like visitStmt), but since we omit AstStmt's walk here,
            // we can just implement the full traversal later.
        }
        walk(statement)
        return res
    }
}
