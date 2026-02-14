// port-lint: source src/analysis/flow.rs
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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
class Span(val begin: Int = 0, val end: Int = 0)
class ResolvedFileSpan(val begin: ResolvedPos = ResolvedPos(), val end: ResolvedPos = ResolvedPos()) {
    override fun toString(): String = "$begin-$end"
}
class ResolvedPos(val line: Int = 0, val column: Int = 0) {
    override fun toString(): String = "$line:$column"
}
class CodeMap {
    fun fileSpan(span: Span): FileSpan = FileSpan()
}
class FileSpan {
    fun resolve(): ResolvedFileSpan = ResolvedFileSpan()
}
class Spanned<T>(val node: T, val span: Span = Span())

// AST node types
sealed class Stmt {
    data class Return(val expr: AstExpr?) : Stmt()
    data class Expression(val expr: AstExpr) : Stmt()
    data class Statements(val stmts: List<AstStmt>) : Stmt()
    data class Def(val def: DefP) : Stmt()
    data class If(val cond: AstExpr, val body: AstStmt) : Stmt()
    data class IfElse(val cond: AstExpr, val bodies: Pair<AstStmt, AstStmt>) : Stmt()
    data class For(val forP: ForP) : Stmt()
    data class Load(val module: String, val names: List<String>) : Stmt()
    data object Break : Stmt()
    data object Continue : Stmt()
    data object Pass : Stmt()
}

class DefP(
    val name: Spanned<Ident>,
    val params: List<Any>,
    val returnType: AstTypeExpr?,
    val body: AstStmt,
    val payload: Any? = null,
)

class ForP(
    val var_: AstExpr,
    val over: AstExpr,
    val body: AstStmt,
)

class Ident(val ident: String) {
    override fun toString(): String = ident
}

sealed class Expr {
    data class Call(val func: AstExpr, val args: List<AstExpr>) : Expr()
    data class Identifier(val name: Spanned<Ident>) : Expr()
    data class Literal(val lit: AstLiteral) : Expr()
    data class Lambda(val params: List<Any>, val body: AstExpr) : Expr()
    data class IfExpr(val cond: AstExpr, val thenExpr: AstExpr, val elseExpr: AstExpr) : Expr()
    data class Tuple(val elems: List<AstExpr>) : Expr()
    data class ListExpr(val elems: List<AstExpr>) : Expr()
    data class Dict(val entries: List<Pair<AstExpr, AstExpr>>) : Expr()
    class Other : Expr()
}

sealed class AstLiteral {
    data class StringLit(val value: String) : AstLiteral()
    data class IntLit(val value: Int) : AstLiteral()
    class Other : AstLiteral()
}

typealias AstStmt = Spanned<Stmt>
typealias AstExpr = Spanned<Expr>
typealias AstTypeExpr = Spanned<TypeExpr>

class TypeExpr(val expr: AstExpr)

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

class AstModule(
    private val codemap: CodeMap,
    private val statement: AstStmt,
) {
    fun codemap(): CodeMap = codemap
    fun statement(): AstStmt = statement
}

// visit_stmt helper: visit immediate child statements
private fun AstStmt.visitStmt(visitor: (AstStmt) -> Unit) {
    when (val s = this.node) {
        is Stmt.Statements -> s.stmts.forEach(visitor)
        is Stmt.Def -> visitor(s.def.body)
        is Stmt.If -> visitor(s.body)
        is Stmt.IfElse -> {
            visitor(s.bodies.first)
            visitor(s.bodies.second)
        }
        is Stmt.For -> visitor(s.forP.body)
        else -> {}
    }
}

// visit_expr helper: visit immediate child expressions
private fun AstExpr.visitExpr(visitor: (AstExpr) -> Unit) {
    when (val e = this.node) {
        is Expr.Call -> {
            visitor(e.func)
            e.args.forEach(visitor)
        }
        is Expr.IfExpr -> {
            visitor(e.cond)
            visitor(e.thenExpr)
            visitor(e.elseExpr)
        }
        is Expr.Tuple -> e.elems.forEach(visitor)
        is Expr.ListExpr -> e.elems.forEach(visitor)
        is Expr.Dict -> e.entries.forEach { (k, v) ->
            visitor(k)
            visitor(v)
        }
        is Expr.Lambda -> visitor(e.body)
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
            is Stmt.Return -> res.add(Pair(x.span, s.expr))
            is Stmt.Def -> {} // Do not descend
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
        is Expr.Call -> when (val func = e.func.node) {
            is Expr.Identifier -> func.name.node.ident == "fail"
            else -> false
        }
        else -> false
    }
}

private fun hasEffect(x: AstExpr): Boolean {
    return when (val e = x.node) {
        is Expr.Literal -> {
            // String literals have the "effect" of providing documentation
            e.lit is AstLiteral.StringLit
        }
        is Expr.Lambda -> false
        is Expr.IfExpr, is Expr.Tuple, is Expr.ListExpr, is Expr.Dict -> {
            var res = false
            x.visitExpr { res = res || hasEffect(it) }
            res
        }
        else -> true
    }
}

private fun finalReturn(x: AstStmt): Boolean {
    return when (val s = x.node) {
        is Stmt.Return -> true
        is Stmt.Expression -> isFail(s.expr)
        is Stmt.Statements -> {
            val last = s.stmts.lastOrNull() ?: return false
            finalReturn(last)
        }
        is Stmt.IfElse -> {
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
        is Expr.Identifier -> if (e.name.node.ident == "None") null else retType.span
        else -> retType.span
    }
}

private fun checkStmt(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    when (val s = x.node) {
        is Stmt.Def -> {
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
        is Stmt.Break, is Stmt.Continue, is Stmt.Return -> true
        is Stmt.Expression -> isFail(s.expr)
        is Stmt.Statements -> {
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
        is Stmt.IfElse -> {
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
            is Stmt.Continue -> if (isLoop) {
                res.add(LintT.new(codemap, x.span, FlowIssue.RedundantContinue))
            }
            is Stmt.Return -> if (s.expr == null && !isLoop) {
                res.add(LintT.new(codemap, x.span, FlowIssue.RedundantReturn))
            }
            is Stmt.Statements -> if (s.stmts.isNotEmpty()) {
                check(isLoop, codemap, s.stmts.last(), res)
            }
            is Stmt.If -> check(isLoop, codemap, s.body, res)
            is Stmt.IfElse -> {
                val (thenBranch, elseBranch) = s.bodies
                check(isLoop, codemap, thenBranch, res)
                check(isLoop, codemap, elseBranch, res)
            }
            else -> {}
        }
    }

    fun f(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
        when (val s = x.node) {
            is Stmt.For -> check(true, codemap, s.forP.body, res)
            is Stmt.Def -> check(false, codemap, s.def.body, res)
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
            is Stmt.Statements -> {
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
            is Stmt.Load -> {
                if (!allowLoads) {
                    res.add(LintT.new(codemap, s.span, FlowIssue.MisplacedLoad))
                }
            }
            is Stmt.Expression -> {
                // Still allow loads after a literal string (probably documentation)
                val expr = node.expr
                if (expr.node !is Expr.Literal || (expr.node as Expr.Literal).lit !is AstLiteral.StringLit) {
                    allowLoads = false
                }
            }
            else -> allowLoads = false
        }
    }
}

private fun noEffect(codemap: CodeMap, x: AstStmt, res: MutableList<LintT<FlowIssue>>) {
    when (val s = x.node) {
        is Stmt.Expression -> if (!hasEffect(s.expr)) {
            res.add(LintT.new(codemap, s.expr.span, FlowIssue.NoEffect))
        }
        else -> x.visitStmt { noEffect(codemap, it, res) }
    }
}

/// Lint an AST module for flow issues.
fun flowLint(module: AstModule): List<LintT<FlowIssue>> {
    val res = mutableListOf<LintT<FlowIssue>>()
    stmt(module.codemap(), module.statement(), res)
    reachable(module.codemap(), module.statement(), res)
    redundant(module.codemap(), module.statement(), res)
    misplacedLoad(module.codemap(), module.statement(), res)
    noEffect(module.codemap(), module.statement(), res)
    return res
}
