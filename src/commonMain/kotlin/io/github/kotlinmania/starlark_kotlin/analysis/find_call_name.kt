// port-lint: source src/analysis/find_call_name.rs
package io.github.kotlinmania.starlark_kotlin.analysis.find_call_name

import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgumentP
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.values.types.string.literal
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExpr
import io.github.kotlinmania.starlark_kotlin.analysis.visitExpr
import io.github.kotlinmania.starlark_kotlin.analysis.statement
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

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

/// Linter.

// Placeholder types removed. Rely on imports.

/// Find the location of a top level function call that has a kwarg "name", and a string value
/// matching `name`.
interface AstModuleFindCallName {
    /// Find the location of a top level function call that has a kwarg "name", and a string value
    /// matching `name`.
    ///
    /// NOTE: If the AST is exposed in the future, this function may be removed and implemented
    ///       by specific programs instead.
    fun findFunctionCallWithName(name: String): Span?
}

fun AstModule.findFunctionCallWithName(name: String): Span? {
    var ret: Span? = null

    fun visitExpr(node: AstExpr) {
        if (ret != null) {
            return
        }

        when (val expr = node.node) {
            is ExprP.Call<*> -> {
                val identifier = expr.expr
                if (identifier.node is ExprP.Identifier<*, *> || identifier.node is ExprP.Dot<*>) {
                    val found = expr.args.args.firstNotNullOfOrNull { argument ->
                        when (val arg = argument.node) {
                            is ArgumentP.Named<*> -> {
                                val value = arg.expr
                                if (arg.name.node == "name" &&
                                    value.node is ExprP.Literal<*> &&
                                    (value.node as ExprP.Literal<*>).literal is AstLiteral.StringLit &&
                                    ((value.node as ExprP.Literal<*>).literal as AstLiteral.StringLit).node == name
                                ) {
                                    identifier.span
                                } else {
                                    null
                                }
                            }
                            else -> null
                        }
                    }
                    if (found != null) {
                        ret = found
                    }
                }
            }
            else -> node.visitExpr(::visitExpr)
        }
    }

    statement.visitExpr(::visitExpr)
    return ret
}
