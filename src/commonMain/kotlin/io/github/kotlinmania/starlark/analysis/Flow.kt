// port-lint: source analysis/flow.rs
package io.github.kotlinmania.starlark.analysis

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.codemap.ResolvedFileSpan
import io.github.kotlinmania.starlark.codemap.ResolvedPos
import io.github.kotlinmania.starlark.codemap.ResolvedSpan
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP

// ---------------------------------------------------------------------------
// Codemap resolution helpers (until FileSpan.resolve / description are added
// to the codemap module proper).
// ---------------------------------------------------------------------------

/** Resolve a [Pos] to a [ResolvedPos] using this [CodeMap]. */
private fun CodeMap.resolvePos(pos: Pos): ResolvedPos {
    val line = findLine(pos)
    val column = pos.value - lines[line].value
    return ResolvedPos(line, column)
}

/** The filename associated with this [FileSpan]. */
internal val FileSpan.description: String
    get() = file.filename

// ---------------------------------------------------------------------------
// Shared lint infrastructure (used across the analysis package).
// ---------------------------------------------------------------------------

/** A standardised set of severities. */
enum class EvalSeverity {
    Disabled,
    Warning,
    Error,
}

/** A typed lint result pairing a location with a problem of type [T]. */
class LintT<T>(
    val location: FileSpan,
    val problem: T,
) {
    override fun toString(): String = "$location: $problem"

    companion object {
        fun <T> new(codemap: CodeMap, span: Span, problem: T): LintT<T> {
            return LintT(codemap.fileSpan(span), problem)
        }
    }
}

/** Marker interface for lint warning types. */
interface LintWarning {
    fun severity(): EvalSeverity
    fun shortName(): String
}

// ---------------------------------------------------------------------------
// visitStmt / visitExpr helpers
// ---------------------------------------------------------------------------

/** Visit immediate child statements of this [Spanned<StmtP<AstNoPayload>>]. */
private fun Spanned<StmtP<AstNoPayload>>.visitStmt(visitor: (Spanned<StmtP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Statements -> s.stmts.forEach(visitor)
        is StmtP.Def<AstNoPayload, *> -> visitor(s.def.body)
        is StmtP.If -> visitor(s.suite)
        is StmtP.IfElse -> {
            visitor(s.suite1)
            visitor(s.suite2)
        }
        is StmtP.For -> visitor(s.forStmt.body)
        else -> {}
    }
}

/** Visit immediate child expressions of this [Spanned<ExprP<AstNoPayload>>]. */
private fun Spanned<ExprP<AstNoPayload>>.visitExpr(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val e = this.node) {
        is ExprP.Call -> {
            visitor(e.expr)
            for (arg in e.args.args) {
                visitor(arg.node.expr())
            }
        }
        is ExprP.If -> {
            visitor(e.cond)
            visitor(e.v1)
            visitor(e.v2)
        }
        is ExprP.Tuple -> e.elements.forEach { visitor(it) }
        is ExprP.ListExpr -> e.elements.forEach { visitor(it) }
        is ExprP.Dict -> e.elements.forEach { (k, v) ->
            visitor(k)
            visitor(v)
        }
        is ExprP.Lambda<AstNoPayload, *> -> visitor(e.lambda.body)
        else -> {}
    }
}

// ---------------------------------------------------------------------------
// FlowIssue
// ---------------------------------------------------------------------------

/** Flow-analysis lint issues. */
sealed class FlowIssue : LintWarning {
    /** `return` lacks expression, but function at location seems to want one. */
    data class MissingReturnExpression(
        val name: String,
        val defSpan: ResolvedFileSpan,
        val reason: ResolvedFileSpan,
    ) : FlowIssue() {
        override fun toString(): String =
            "`return` lacks expression, but function `$name` at $defSpan seems to want one due to $reason"
    }

    /** No `return` at the end, but function seems to want one. */
    data class MissingReturn(
        val name: String,
        val reason: ResolvedFileSpan,
    ) : FlowIssue() {
        override fun toString(): String =
            "No `return` at the end, but function `$name` seems to want one due to $reason"
    }

    /** Unreachable statement. */
    data class Unreachable(val stmt: String) : FlowIssue() {
        override fun toString(): String = "Unreachable statement `$stmt`"
    }

    /** Redundant `return` at the end of a function. */
    data object RedundantReturn : FlowIssue() {
        override fun toString(): String = "Redundant `return` at the end of a function"
    }

    /** Redundant `continue` at the end of a loop. */
    data object RedundantContinue : FlowIssue() {
        override fun toString(): String = "Redundant `continue` at the end of a loop"
    }

    /** A `load` statement not at the top of the file. */
    data object MisplacedLoad : FlowIssue() {
        override fun toString(): String = "A `load` statement not at the top of the file"
    }

    /** Statement has no effect. */
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

// ---------------------------------------------------------------------------
// returns
// ---------------------------------------------------------------------------

private fun returns(x: Spanned<StmtP<AstNoPayload>>): List<Pair<Span, Spanned<ExprP<AstNoPayload>>?>> {
    fun f(x: Spanned<StmtP<AstNoPayload>>, res: MutableList<Pair<Span, Spanned<ExprP<AstNoPayload>>?>>) {
        when (val s = x.node) {
            is StmtP.Return -> res.add(Pair(x.span, s.expr))
            is StmtP.Def<AstNoPayload, *> -> {} // Do not descend
            else -> x.visitStmt { f(it, res) }
        }
    }

    val res = mutableListOf<Pair<Span, Spanned<ExprP<AstNoPayload>>?>>()
    f(x, res)
    return res
}

// ---------------------------------------------------------------------------
// isFail – fail is kind of like a return with error
// ---------------------------------------------------------------------------

private fun isFail(x: Spanned<ExprP<AstNoPayload>>): Boolean {
    val e = x.node
    if (e !is ExprP.Call) return false
    val func = e.expr.node
    if (func !is ExprP.Identifier<AstNoPayload, *>) return false
    return func.ident.node.ident == "fail"
}

// ---------------------------------------------------------------------------
// hasEffect
// ---------------------------------------------------------------------------

private fun hasEffect(x: Spanned<ExprP<AstNoPayload>>): Boolean {
    return when (val e = x.node) {
        is ExprP.Literal -> {
            // String literals have the "effect" of providing documentation
            e.literal is AstLiteral.String
        }
        is ExprP.Lambda<AstNoPayload, *> -> false
        is ExprP.If, is ExprP.Tuple, is ExprP.ListExpr, is ExprP.Dict -> {
            var res = false
            x.visitExpr { res = res || hasEffect(it) }
            res
        }
        else -> true
    }
}

// ---------------------------------------------------------------------------
// finalReturn
// ---------------------------------------------------------------------------

private fun finalReturn(x: Spanned<StmtP<AstNoPayload>>): Boolean {
    return when (val s = x.node) {
        is StmtP.Return -> true
        is StmtP.Expression -> isFail(s.expr)
        is StmtP.Statements -> {
            val last = s.stmts.lastOrNull() ?: return false
            finalReturn(last)
        }
        is StmtP.IfElse -> {
            finalReturn(s.suite1) && finalReturn(s.suite2)
        }
        else -> false
    }
}

// ---------------------------------------------------------------------------
// requireReturnExpression
// ---------------------------------------------------------------------------

private fun requireReturnExpression(retType: Spanned<TypeExprP<AstNoPayload, Unit>>?): Span? {
    if (retType == null) return null
    val e = retType.node.expr.node
    if (e is ExprP.Identifier<AstNoPayload, *>) {
        if (e.ident.node.ident == "None") return null
    }
    return retType.span
}

// ---------------------------------------------------------------------------
// helpers
// ---------------------------------------------------------------------------

/** Extract the identifier string from a [Spanned] wrapping an [AssignIdentP]. */
private fun defName(spanned: Spanned<AssignIdentP<*, *>>): String {
    return spanned.node.ident
}

// ---------------------------------------------------------------------------
// checkStmt
// ---------------------------------------------------------------------------

private fun checkStmt(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
    when (val s = x.node) {
        is StmtP.Def<AstNoPayload, *> -> {
            val def = s.def
            val body = def.body
            val rets = returns(body)

            // Do I require my return statements to have an expression
            val requireExpression = requireReturnExpression(def.returnType)
                ?: rets.firstOrNull { it.second != null }?.first
            if (requireExpression != null) {
                if (!finalReturn(body)) {
                    res.add(
                        LintT.new(
                            codemap,
                            x.span,
                            FlowIssue.MissingReturn(
                                // Statements often end with \n, so remove that to fit nicely
                                defName(def.name).trimEnd(),
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
                                    defName(def.name),
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

// ---------------------------------------------------------------------------
// stmt
// ---------------------------------------------------------------------------

internal fun stmt(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
    checkStmt(codemap, x, res)
    x.visitStmt { stmt(codemap, it, res) }
}

// ---------------------------------------------------------------------------
// reachable
// ---------------------------------------------------------------------------

/**
 * Returns `true` if the code aborts this sequence early,
 * due to return, fail, break or continue.
 */
internal fun reachable(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>): Boolean {
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
                    // All the remaining statements are totally unreachable, but we declared
                    // that once so don't even bother looking at them
                    return aborts
                }
            }
            false
        }
        is StmtP.IfElse -> {
            val abort1 = reachable(codemap, s.suite1, res)
            val abort2 = reachable(codemap, s.suite2, res)
            abort1 && abort2
        }
        // For all remaining constructs, visit their children to accumulate errors,
        // but even if they are present with returns, you don't guarantee the code with inner
        // returns gets executed.
        else -> {
            x.visitStmt { reachable(codemap, it, res) }
            false
        }
    }
}

// ---------------------------------------------------------------------------
// redundant
// ---------------------------------------------------------------------------

/**
 * If you have a definition which ends with return, or a loop which ends with continue
 * that is a useless statement.
 */
internal fun redundant(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
    fun check(isLoop: Boolean, codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
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
            is StmtP.If -> check(isLoop, codemap, s.suite, res)
            is StmtP.IfElse -> {
                check(isLoop, codemap, s.suite1, res)
                check(isLoop, codemap, s.suite2, res)
            }
            else -> {}
        }
    }

    fun f(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
        when (val s = x.node) {
            is StmtP.For -> check(true, codemap, s.forStmt.body, res)
            is StmtP.Def<AstNoPayload, *> -> check(false, codemap, s.def.body, res)
            else -> {}
        }
        // We always want to look inside everything for other types of violation
        x.visitStmt { f(codemap, it, res) }
    }

    x.visitStmt { f(codemap, it, res) }
}

// ---------------------------------------------------------------------------
// misplacedLoad
// ---------------------------------------------------------------------------

internal fun misplacedLoad(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
    // accumulate all statements at the top-level
    fun topStatements(x: Spanned<StmtP<AstNoPayload>>, stmts: MutableList<Spanned<StmtP<AstNoPayload>>>) {
        when (val s = x.node) {
            is StmtP.Statements -> {
                for (child in s.stmts) {
                    topStatements(child, stmts)
                }
            }
            else -> stmts.add(x)
        }
    }

    val stmts = mutableListOf<Spanned<StmtP<AstNoPayload>>>()
    topStatements(x, stmts)

    // We allow loads or documentation strings, but after that, no loads
    var allowLoads = true
    for (s in stmts) {
        when (val node = s.node) {
            is StmtP.Load<AstNoPayload, *> -> {
                if (!allowLoads) {
                    res.add(LintT.new(codemap, s.span, FlowIssue.MisplacedLoad))
                }
            }
            is StmtP.Expression -> {
                val expr = (node.expr)
                val exprNode = expr.node
                // Still allow loads after a literal string (probably documentation)
                if (exprNode is ExprP.Literal && exprNode.literal is AstLiteral.String) {
                    // keep allowLoads as-is
                } else {
                    allowLoads = false
                }
            }
            else -> allowLoads = false
        }
    }
}

// ---------------------------------------------------------------------------
// noEffect
// ---------------------------------------------------------------------------

internal fun noEffect(codemap: CodeMap, x: Spanned<StmtP<AstNoPayload>>, res: MutableList<LintT<FlowIssue>>) {
    when (val s = x.node) {
        is StmtP.Expression -> if (!hasEffect(s.expr)) {
            res.add(LintT.new(codemap, (s.expr).span, FlowIssue.NoEffect))
        }
        else -> x.visitStmt { noEffect(codemap, it, res) }
    }
}

// ---------------------------------------------------------------------------
// lint (public entry point)
// ---------------------------------------------------------------------------

/** Lint an [AstModule] for flow issues. */
fun flowLint(module: AstModule): List<LintT<FlowIssue>> {
    val res = mutableListOf<LintT<FlowIssue>>()
    stmt(module.codemap, module.statement, res)
    reachable(module.codemap, module.statement, res)
    redundant(module.codemap, module.statement, res)
    misplacedLoad(module.codemap, module.statement, res)
    noEffect(module.codemap, module.statement, res)
    return res
}
