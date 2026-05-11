// port-lint: source src/analysis/performance.rs
package io.github.kotlinmania.starlark_kotlin.analysis

import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.codemap.*
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExpr
import io.github.kotlinmania.starlark_kotlin.values.layout.size


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

import io.github.kotlinmania.starlark_kotlin.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.values.layout.size

// pub(crate) enum Performance
internal sealed class Performance : LintWarning {
    // #[error("Dict copy `{0}` is more efficient as `{1}`")]
    data class DictWithoutStarStar(val original: String, val replacement: String) : Performance() {
        override fun toString(): String = "Dict copy `$original` is more efficient as `$replacement`"
        override fun severity(): EvalSeverity = EvalSeverity.Warning
        override fun shortName(): String = "dict-without-star-star"
    }

    // #[error("`{0}` eagerly evaluates all items...")]
    data class EagerAndInefficientBoolCheck(val expr: String) : Performance() {
        override fun toString(): String =
            "`$expr` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop."
        override fun severity(): EvalSeverity = EvalSeverity.Warning
        override fun shortName(): String = "eager-and-inefficient-bool-check"
    }

    // #[error("`{0}` allocates a new {1}...")]
    data class InefficientBoolCheck(val expr: String, val kind: String) : Performance() {
        override fun toString(): String =
            "`$expr` allocates a new $kind for the results. Prefer using a for-loop."
        override fun severity(): EvalSeverity = EvalSeverity.Warning
        override fun shortName(): String = "inefficient-bool-check"
    }
}

// fn match_dict_copy(codemap: &CodeMap, x: &AstExpr, res: &mut Vec<LintT<Performance>>)
private fun matchDictCopy(codemap: CodeMap, x: AstExpr, res: MutableList<LintT<Performance>>) {
    // If we see `dict(**x)` suggest `dict(x)`
    val expr = x.node
    if (expr is ExprP.Call<*> && expr.args.args.size == 1) {
        val func = expr.expr.node
        val arg = expr.args.args[0]
        if (func is ExprP.Identifier<*, *>
            && func.ident.node.ident == "dict"
            && arg.node is ArgumentP.KwArgs<*>
        ) {
            val kwArg = (arg.node as ArgumentP.KwArgs<*>).expr
            res.add(
                LintT.new(
                    codemap,
                    x.span,
                    Performance.DictWithoutStarStar(
                        x.toString(),
                        "dict(${kwArg.node})",
                    ),
                )
            )
        }
    }
}

// fn match_inefficient_bool_check(codemap: &CodeMap, x: &AstExpr, res: &mut Vec<LintT<Performance>>)
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
        // any([blah for blah in blahs]) or any({k: v for ...})
        is ExprP.ListExpr<*>, is ExprP.Dict<*> -> {
            // Comprehension variants — in the full AST these would be
            // ExprP.ListComprehension / ExprP.DictComprehension.
            // Placeholder: trigger on list/dict comprehensions once available.
        }
        is ExprP.Call<*> -> {
            // any(list(_get_some_dict())) or any(dict([]))
            val innerFunc = arg.expr.node
            if (innerFunc is ExprP.Identifier<*, *>) {
                val innerIdent = innerFunc.ident.node.ident
                if (innerIdent == "dict" || innerIdent == "list") {
                    res.add(
                        LintT.new(
                            codemap,
                            x.span,
                            Performance.InefficientBoolCheck(
                                x.toString(),
                                innerIdent,
                            ),
                        )
                    )
                }
            }
        }
        else -> {}
    }
}

// fn check_call_expr(module: &AstModule, res: &mut Vec<LintT<Performance>>)
private fun checkCallExpr(module: AstModule, res: MutableList<LintT<Performance>>) {
    fun check(codemap: CodeMap, x: AstExpr, res: MutableList<LintT<Performance>>) {
        matchDictCopy(codemap, x, res)
        matchInefficientBoolCheck(codemap, x, res)
        x.node.visitChildExprs { child -> check(codemap, child, res) }
    }
    module.statement.visitExprs { x -> check(module.codemap, x, res) }
}

// pub(crate) fn lint(module: &AstModule) -> Vec<LintT<Performance>>
internal fun lintPerformance(module: AstModule): List<LintT<Performance>> {
    val res = mutableListOf<LintT<Performance>>()
    checkCallExpr(module, res)
    return res
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
