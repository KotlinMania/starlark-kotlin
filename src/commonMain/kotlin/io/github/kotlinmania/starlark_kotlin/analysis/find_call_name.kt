// port-lint: source src/analysis/find_call_name.rs
package io.github.kotlinmania.starlark_kotlin.analysis.find_call_name

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Span

class AstExpr(val node: Expr, val span: Span) {
    fun visitExpr(visitor: (AstExpr) -> Unit) {
        node.visitChildren(visitor)
    }
}

sealed class Expr {
    class Call(val identifier: AstExpr, val arguments: CallArgs) : Expr()
    class Identifier(val name: String) : Expr()
    class Dot(val expr: AstExpr, val attr: String) : Expr()
    class Literal(val literal: AstLiteral) : Expr()
    class Other : Expr()

    fun visitChildren(visitor: (AstExpr) -> Unit) {}
}

sealed class AstLiteral {
    class StringLit(val node: String) : AstLiteral()
    class Other : AstLiteral()
}

class CallArgs(val args: List<SpannedArgument>)

class SpannedArgument(val node: Argument)

sealed class Argument {
    class Named(val name: SpannedString, val value: AstExpr) : Argument()
    class Other : Argument()
}

class SpannedString(val node: String)

class AstStmt {
    fun visitExpr(visitor: (AstExpr) -> Unit) {}
}

class AstModule(
    private val statement: AstStmt,
) {
    fun statement(): AstStmt = statement
}

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
            is Expr.Call -> {
                val identifier = expr.identifier
                if (identifier.node is Expr.Identifier || identifier.node is Expr.Dot) {
                    val found = expr.arguments.args.firstNotNullOfOrNull { argument ->
                        when (val arg = argument.node) {
                            is Argument.Named -> {
                                val value = arg.value
                                if (arg.name.node == "name" &&
                                    value.node is Expr.Literal &&
                                    (value.node as Expr.Literal).literal is AstLiteral.StringLit &&
                                    ((value.node as Expr.Literal).literal as AstLiteral.StringLit).node == name
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

    statement().visitExpr(::visitExpr)
    return ret
}
