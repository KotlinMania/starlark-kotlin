// port-lint: source src/analysis/performance.rs
package io.github.kotlinmania.starlark.analysis

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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.toSourceString

internal sealed class Performance : LintWarning {
    data class DictWithoutStarStar(
        val original: String,
        val replacement: String,
    ) : Performance() {
        override fun toString(): String = "Dict copy `$original` is more efficient as `$replacement`"

        override fun severity(): EvalSeverity = EvalSeverity.Warning

        override fun shortName(): String = "dict-without-star-star"
    }

    data class EagerAndInefficientBoolCheck(
        val expr: String,
    ) : Performance() {
        override fun toString(): String =
            "`$expr` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop."

        override fun severity(): EvalSeverity = EvalSeverity.Warning

        override fun shortName(): String = "eager-and-inefficient-bool-check"
    }

    data class InefficientBoolCheck(
        val expr: String,
        val kind: String,
    ) : Performance() {
        override fun toString(): String =
            "`$expr` allocates a new $kind for the results. Prefer using a for-loop."

        override fun severity(): EvalSeverity = EvalSeverity.Warning

        override fun shortName(): String = "inefficient-bool-check"
    }
}

private fun matchDictCopy(codemap: CodeMap, x: AstExpr, res: MutableList<LintT<Performance>>) {
    val expr = x.node
    if (expr is ExprP.Call<*> && expr.args.args.size == 1) {
        val func = expr.expr.node
        val arg = expr.args.args[0]
        if (func is ExprP.Identifier<*, *> &&
            func.ident.node.ident == "dict" &&
            arg.node is ArgumentP.KwArgs<*>
        ) {
            val kwArg = (arg.node as ArgumentP.KwArgs<*>).expr
            res.add(
                LintT.new(
                    codemap,
                    x.span,
                    Performance.DictWithoutStarStar(
                        x.node.toSourceString(),
                        "dict(${kwArg.node})",
                    ),
                ),
            )
        }
    }
}

private fun matchInefficientBoolCheck(
    codemap: CodeMap,
    x: AstExpr,
    res: MutableList<LintT<Performance>>,
) {
    val expr = x.node
    if (expr !is ExprP.Call<*> || expr.args.args.size != 1) return

    val func = expr.expr.node
    val argAst = expr.args.args[0]

    if (func !is ExprP.Identifier<*, *>) return
    val funcIdent = func.ident.node.ident
    if (funcIdent != "any" && funcIdent != "all") return

    // Check for positional argument patterns
    if (argAst.node !is ArgumentP.Positional<*>) return
    val arg = (argAst.node as ArgumentP.Positional<*>).expr.node

    when (arg) {
        is ExprP.ListComprehension<*>, is ExprP.DictComprehension<*> ->
            res.add(
                LintT.new(
                    codemap,
                    x.span,
                    Performance.EagerAndInefficientBoolCheck(funcIdent),
                ),
            )
        is ExprP.Call<*> -> {
            val innerFunc = arg.expr.node
            if (innerFunc is ExprP.Identifier<*, *>) {
                val innerIdent = innerFunc.ident.node.ident
                if (innerIdent == "dict" || innerIdent == "list") {
                    res.add(
                        LintT.new(
                            codemap,
                            x.span,
                            Performance.InefficientBoolCheck(
                                x.node.toSourceString(),
                                innerIdent,
                            ),
                        ),
                    )
                }
            }
        }
        else -> {}
    }
}

private fun checkCallExpr(module: AstModule, res: MutableList<LintT<Performance>>) {
    fun check(codemap: CodeMap, x: AstExpr, res: MutableList<LintT<Performance>>) {
        matchDictCopy(codemap, x, res)
        matchInefficientBoolCheck(codemap, x, res)
        x.node.visitChildExprs { child -> check(codemap, child, res) }
    }
    module.statement.visitExprs { x -> check(module.codemap, x, res) }
}

internal fun lintPerformance(module: AstModule): List<LintT<Performance>> {
    val res = mutableListOf<LintT<Performance>>()
    checkCallExpr(module, res)
    return res
}
