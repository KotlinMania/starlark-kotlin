// port-lint: source src/analysis/flow.rs
package io.github.kotlinmania.starlark_kotlin.analysis

import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.Disabled
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.values.types.string.elems
import io.github.kotlinmania.starlark_kotlin.eval.compiler.thenExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.forP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.elseExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.cond
import io.github.kotlinmania.starlark_kotlin.eval.bc.call.resolve
import io.github.kotlinmania.starlark_kotlin.entries
import io.github.kotlinmania.starlark_kotlin.docs.lit
import io.github.kotlinmania.starlark_kotlin.docs.args
import io.github.kotlinmania.starlark_kotlin.codemap
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstTypeExpr
import io.github.kotlinmania.starlark_kotlin.codemap.ResolvedFileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmt
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExpr
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
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

// Placeholder types removed. Rely on imports.

enum class EvalSeverity {
    Disabled,
    Warning,
    Error,
}

class LintT<T>(
    val location: FileSpan,
    val problem: T,
) {
    companion object {
        fun <T> new(codemap: CodeMap, span: Span, problem: T): LintT<T> {
            return LintT(codemap.fileSpan(span), problem)
        }
    }
}

interface LintWarning {
    fun severity(): EvalSeverity
    fun shortName(): String
}

// visit_stmt helper: visit immediate child statements
private fun AstStmt.visitStmt(visitor: (AstStmt) -> Unit) {
    when (val s = this.node) {
        is StmtP.Statements -> s.stmts.forEach(visitor)
        is StmtP.Def -> visitor(s.def.body)
        is StmtP.If -> visitor(s.body)
        is StmtP.IfElse -> {
            visitor(s.bodies.first)
            visitor(s.bodies.second)
        }
        is StmtP.For -> visitor(s.forP.body)
        else -> {}
    }
}

// visit_expr helper: visit immediate child expressions
private fun AstExpr.visitExpr(visitor: (AstExpr) -> Unit) {
    when (val e = this.node) {
        is ExprP.Call -> {
            visitor(e.func)
            e.args.forEach(visitor)
        }
        is ExprP.IfExpr -> {
            visitor(e.cond)
            visitor(e.thenExpr)
            visitor(e.elseExpr)
        }
        is ExprP.Tuple -> e.elems.forEach(visitor)
        is ExprP.ListExpr -> e.elems.forEach(visitor)
        is ExprP.Dict -> e.entries.forEach { (k, v) ->
            visitor(k)
            visitor(v)
        }
        is ExprP.Lambda -> visitor(e.lambda.body)
        else -> {}
    }
}

sealed class FlowIssue : LintWarning {
    /// `return` lacks expression, but function at location seems to want one
    data class MissingReturnExpression(
        val name: String,
        val defSpan: ResolvedFileSpan,
        val reason: ResolvedFileSpan,
    ) : FlowIssue() {
        override fun toString(): String =
            "`return` lacks expression, but function `$name` at $defSpan seems to want one due to $reason"
    }

    /// No `return` at the end, but function seems to want one
    data class MissingReturn(
        val name: String,
        val reason: ResolvedFileSpan,
    ) : FlowIssue() {
        override fun toString(): String =
            "No `return` at the end, but function `$name` seems to want one due to $reason"
    }

    /// Unreachable statement
    data class Unreachable(val stmt: String) : FlowIssue() {
        override fun toString(): String = "Unreachable statement `$stmt`"
    }

    /// Redundant `return` at the end of a function
    data object RedundantReturn : FlowIssue() {
        override fun toString(): String = "Redundant `return` at the end of a function"
    }

    /// Redundant `continue` at the end of a loop
    data object RedundantContinue : FlowIssue() {
        override fun toString(): String = "Redundant `continue` at the end of a loop"
    }

    /// A `load` statement not at the top of the file
    data object MisplacedLoad : FlowIssue() {
        override fun toString(): String = "A `load` statement not at the top of the file"
    }

    /// Statement has no effect
    data object NoEffect : FlowIssue() {
        override fun toString(): String = "Statement has no effect"
    }

    override fun severity(): EvalSeverity {
        return when (this) {
            // Sometimes people add these to make flow clearer
            is RedundantContinue, is RedundantReturn -> EvalSeverity.Disabled
            else -> EvalSeverity.Warning
        }
    }

    override fun shortName(): String {
        return when (this) {
            is MissingReturnExpression -> "missing-return-expression"
            is MissingReturn -> "missing-return"
            is Unreachable -> "unreachable"
            is RedundantReturn -> "redundant-return"
            is RedundantContinue -> "redundant-continue"
            is MisplacedLoad -> "misplaced-load"
            is NoEffect -> "no-effect"
        }
    }

    fun about(): String {
        return when (this) {
            is MissingReturnExpression -> name
            is MissingReturn -> name
            is Unreachable -> stmt
            else -> error("Should not be used on such issues")
        }
    }
}

private fun returns(x: AstStmt): List<Pair<Span, AstExpr?>> {
    fun f(x: AstStmt, res: MutableList<Pair<Span, AstExpr?>>) {
        when (val s = x.node) {
            is StmtP.Return -> res.add(Pair(x.span, s.expr))
            is StmtP.Def -> {} // Do not descend
            else -> x.visitStmt { f(it, res) }
        }
    }

    val res = mutableListOf<Pair<Span, AstExpr?>>()
    f(x, res)
    return res
}

// fail is kind of like a return with error
private fun isFail(x: AstExpr): Boolean {
    return when (val e = x.node) {
        is ExprP.Call -> when (val func = e.func.node) {
            is ExprP.Identifier -> func.name.node.ident == "fail"
            else -> false
        }
        else -> false
    }
}

private fun hasEffect(x: AstExpr): Boolean {
    return when (val e = x.node) {
        is ExprP.Literal -> {
            // String literals have the "effect" of providing documentation
            e.lit is AstLiteral.StringLit
        }
        is ExprP.Lambda -> false
        is ExprP.IfExpr, is ExprP.Tuple, is ExprP.ListExpr, is ExprP.Dict -> {
            var res = false
            x.visitExpr { res = res || hasEffect(it) }
            res
        }
        else -> true
    }
}

private fun finalReturn(x: AstStmt): Boolean {
    return when (val s = x.node) {
        is StmtP.Return -> true
        is StmtP.Expression -> isFail(s.expr)
        is StmtP.Statements -> {
            val last = s.stmts.lastOrNull() ?: return false
            finalReturn(last)
        }
        is StmtP.IfElse -> {
            val (thenBranch, elseBranch) = s.bodies
            finalReturn(thenBranch) && finalReturn(elseBranch)
        }
        else -> false
    }
}

private fun requireReturnExpression(retType: AstTypeExpr?): Span? {
    if (retType == null) return null
    val expr = retType.node.expr
    return when (val e = expr.node) {
        is ExprP.Identifier -> if (e.name.node.ident == "None") null else retType.span
        else -> retType.span
    }
}

private fun checkStmt(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    when (val s = x.node) {
        is StmtP.Def -> {
            val def = s.def
            val rets = returns(def.body)

            // Do I require my return statements to have an expression
            val requireExpression = requireReturnExpression(def.returnType)
                ?: rets.firstOrNull { it.second != null }?.first
            if (requireExpression != null) {
                if (!finalReturn(def.body)) {
                    res.add(
                        LintT.new(
                            codemap,
                            x.span,
                            FlowIssue.MissingReturn(
                                // Statements often end with \n, so remove that to fit nicely
                                def.name.node.ident.trimEnd(),
                                codemap.fileSpan(requireExpression).resolve(),
                            ),
                        )
                    )
                }
                for ((span, ret) in rets) {
                    if (ret == null) {
                        res.add(
                            LintT.new(
                                codemap,
                                span,
                                FlowIssue.MissingReturnExpression(
                                    def.name.node.ident,
                                    codemap.fileSpan(x.span).resolve(),
                                    codemap.fileSpan(requireExpression).resolve(),
                                ),
                            )
                        )
                    }
                }
            }
        }
        else -> {}
    }
}

private fun stmt(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    checkStmt(codemap, x, res)
    x.visitStmt { stmt(codemap, it, res) }
}

/// Returns true if the code aborts this sequence early, due to return, fail, break or continue.
private fun reachable(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>): Boolean {
    return when (val s = x.node) {
        is StmtP.Break, is StmtP.Continue, is StmtP.Return -> true
        is StmtP.Expression -> isFail(s.expr)
        is StmtP.Statements -> {
            val iter = s.stmts.iterator()
            while (iter.hasNext()) {
                val current = iter.next()
                val aborts = reachable(codemap, current, res)
                if (aborts) {
                    if (iter.hasNext()) {
                        val nxt = iter.next()
                        res.add(
                            LintT.new(
                                codemap,
                                nxt.span,
                                FlowIssue.Unreachable(nxt.node.toString().trim()),
                            )
                        )
                    }
                    // All the remaining statements are totally unreachable, but we declared that once
                    // so don't even bother looking at them
                    return aborts
                }
            }
            false
        }
        is StmtP.IfElse -> {
            val (thenBranch, elseBranch) = s.bodies
            val abort1 = reachable(codemap, thenBranch, res)
            val abort2 = reachable(codemap, elseBranch, res)
            abort1 && abort2
        }
        // For all remaining constructs, visit their children to accumulate errors,
        // but even if they are present with returns, you don't guarantee the code with inner returns
        // gets executed.
        else -> {
            x.visitStmt { reachable(codemap, it, res) }
            false
        }
    }
}

// If you have a definition which ends with return, or a loop which ends with continue
// that is a useless statement
private fun redundant(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    fun check(isLoop: Boolean, codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
        when (val s = x.node) {
            is StmtP.Continue -> if (isLoop) {
                res.add(LintT.new(codemap, x.span, FlowIssue.RedundantContinue))
            }
            is StmtP.Return -> if (s.expr == null && !isLoop) {
                res.add(LintT.new(codemap, x.span, FlowIssue.RedundantReturn))
            }
            is StmtP.Statements -> if (s.stmts.isNotEmpty()) {
                check(isLoop, codemap, s.stmts.last(), res)
            }
            is StmtP.If -> check(isLoop, codemap, s.body, res)
            is StmtP.IfElse -> {
                val (thenBranch, elseBranch) = s.bodies
                check(isLoop, codemap, thenBranch, res)
                check(isLoop, codemap, elseBranch, res)
            }
            else -> {}
        }
    }

    fun f(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
        when (val s = x.node) {
            is StmtP.For -> check(true, codemap, s.forP.body, res)
            is StmtP.Def -> check(false, codemap, s.def.body, res)
            else -> {}
        }
        // We always want to look inside everything for other types of violation
        x.visitStmt { f(codemap, it, res) }
    }

    x.visitStmt { f(codemap, it, res) }
}

private fun misplacedLoad(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    // accumulate all statements at the top-level
    fun topStatements(x: AstStmt, stmts: MutableList<AstStmt>) {
        when (val s = x.node) {
            is StmtP.Statements -> {
                for (child in s.stmts) {
                    topStatements(child, stmts)
                }
            }
            else -> stmts.add(x)
        }
    }

    val stmts = mutableListOf<AstStmt>()
    topStatements(x, stmts)

    // We allow loads or documentation strings, but after that, no loads
    var allowLoads = true
    for (s in stmts) {
        when (val node = s.node) {
            is StmtP.Load -> {
                if (!allowLoads) {
                    res.add(LintT.new(codemap, s.span, FlowIssue.MisplacedLoad))
                }
            }
            is StmtP.Expression -> {
                // Still allow loads after a literal string (probably documentation)
                val expr = node.expr
                if (expr.node !is ExprP.Literal || (expr.node as ExprP.Literal).lit !is AstLiteral.StringLit) {
                    allowLoads = false
                }
            }
            else -> allowLoads = false
        }
    }
}

private fun noEffect(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    when (val s = x.node) {
        is StmtP.Expression -> if (!hasEffect(s.expr)) {
            res.add(LintT.new(codemap, s.expr.span, FlowIssue.NoEffect))
        }
        else -> x.visitStmt { noEffect(codemap, it, res) }
    }
}

/// Lint an AST module for flow issues.
fun flowLint(module: AstModule): List<LintT<FlowIssue>> {
    val res = mutableListOf<LintT<FlowIssue>>()
    stmt(module.codemap, module.statement, res)
    reachable(module.codemap, module.statement, res)
    redundant(module.codemap, module.statement, res)
    misplacedLoad(module.codemap, module.statement, res)
    noEffect(module.codemap, module.statement, res)
    return res
}
