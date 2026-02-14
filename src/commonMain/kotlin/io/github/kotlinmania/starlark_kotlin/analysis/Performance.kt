// port-lint: source src/analysis/performance.rs
package io.github.kotlinmania.starlark_kotlin.analysis

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

// Forward reference AST argument types (from starlark_syntax::syntax::ast).
// These will unify with a single definition when the syntax AST is fully ported.
internal sealed class ArgumentAst {
    class Positional(val expr: AstExpr) : ArgumentAst()
    class Named(val name: String, val value: AstExpr) : ArgumentAst()
    class Args(val expr: AstExpr) : ArgumentAst()
    class KwArgs(val expr: AstExpr) : ArgumentAst()
}

// Forward reference: CallArgs wrapping argument list (from Expr::Call).
internal class CallArgs(val args: List<ArgumentAst>)

// Forward reference: comprehension Expr variants not yet in flow.kt.
// Expr.ListComprehension and Expr.DictComprehension extend Expr.
// These are declared locally until Expr is fully ported.

// #[derive(Error, Debug)]
// pub(crate) enum Performance
internal sealed class Performance : LintWarning {
    // #[error("Dict copy `{0}` is more efficient as `{1}`")]
    class DictWithoutStarStar(val original: String, val suggested: String) : Performance() {
        override fun toString(): String = "Dict copy `$original` is more efficient as `$suggested`"
    }

    // #[error("`{0}` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop.")]
    class EagerAndInefficientBoolCheck(val funcName: String) : Performance() {
        override fun toString(): String =
            "`$funcName` eagerly evaluates all items in the iterable, and allocates an array for the results. Prefer using a for-loop."
    }

    // #[error("`{0}` allocates a new {1} for the results. Prefer using a for-loop.")]
    class InefficientBoolCheck(val callExpr: String, val allocType: String) : Performance() {
        override fun toString(): String =
            "`$callExpr` allocates a new $allocType for the results. Prefer using a for-loop."
    }

    // impl LintWarning for Performance
    override fun severity(): EvalSeverity = EvalSeverity.Warning

    override fun shortName(): String = when (this) {
        is DictWithoutStarStar -> "dict-without-star-star"
        is EagerAndInefficientBoolCheck -> "eager-and-inefficient-bool-check"
        is InefficientBoolCheck -> "inefficient-bool-check"
    }
}

// fn match_dict_copy(codemap: &CodeMap, x: &AstExpr, res: &mut Vec<LintT<Performance>>)
private fun matchDictCopy(codemap: CodeMap, x: AstExpr, res: MutableList<LintT<Performance>>) {
    // If we see `dict(**x)` suggest `dict(x)`
    val expr = x.node
    if (expr is Expr.Call && expr.args.size == 1) {
        val func = expr.func.node
        val arg = expr.args[0]
        if (func is Expr.Identifier
            && func.name.node.ident == "dict"
            && arg.node is ArgumentAst.KwArgs
        ) {
            val kwArg = (arg.node as ArgumentAst.KwArgs).expr
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
    if (expr !is Expr.Call || expr.args.size != 1) return

    val func = expr.func.node
    val argAst = expr.args[0]

    if (func !is Expr.Identifier) return
    val funcIdent = func.name.node.ident
    if (funcIdent != "any" && funcIdent != "all") return

    // Check for positional argument patterns
    if (argAst.node !is ArgumentAst.Positional) return
    val arg = (argAst.node as ArgumentAst.Positional).expr.node

    when (arg) {
        // any([blah for blah in blahs]) or any({k: v for ...})
        is Expr.ListExpr, is Expr.Dict -> {
            // Comprehension variants — in the full AST these would be
            // Expr.ListComprehension / Expr.DictComprehension.
            // Placeholder: trigger on list/dict comprehensions once available.
        }
        is Expr.Call -> {
            // any(list(_get_some_dict())) or any(dict([]))
            val innerFunc = arg.func.node
            if (innerFunc is Expr.Identifier) {
                val innerIdent = innerFunc.name.node.ident
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
        x.visitExpr { child -> check(codemap, child, res) }
    }
    module.statement().visitExpr { x -> check(module.codemap(), x, res) }
}

// pub(crate) fn lint(module: &AstModule) -> Vec<LintT<Performance>>
internal fun lint(module: AstModule): List<LintT<Performance>> {
    val res = mutableListOf<LintT<Performance>>()
    checkCallExpr(module, res)
    return res
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
