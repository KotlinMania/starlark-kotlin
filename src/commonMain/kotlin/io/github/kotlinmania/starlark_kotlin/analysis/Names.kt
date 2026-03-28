// port-lint: source src/analysis/names.rs
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

// There are two improvements that we could make:
//
// 1. Use the existing name resolution code, so we don't end up duplicating that logic.
// 2. Extract a flow graph rather than doing a fixed point.
//
// But it does as things stand.

import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstAssignTarget
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstAssignIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstTypeExpr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstStmt
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstExpr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

sealed class NameWarning : LintWarning {
    data class UnusedLoad(val name: String) : NameWarning()
    data class UnusedAssign(val name: String) : NameWarning()
    data class UnusedArgument(val name: String) : NameWarning()
    data class UsingUnassigned(val name: String) : NameWarning()
    data class UsingUndefined(val name: String) : NameWarning()
    data class UsingMaybeUndefined(val name: String) : NameWarning()

    override fun toString(): String = when (this) {
        is UnusedLoad -> "Unused `load` of `$name`"
        is UnusedAssign -> "Unused assignment of `$name`"
        is UnusedArgument -> "Unused argument `$name`"
        is UsingUnassigned -> "Use of unassigned variable `$name`"
        is UsingUndefined -> "Use of undefined variable `$name`"
        is UsingMaybeUndefined -> "Use of potentially undefined variable `$name`"
    }

    override fun severity(): EvalSeverity = when (this) {
        is UsingUnassigned, is UsingMaybeUndefined -> EvalSeverity.Warning
        else -> EvalSeverity.Disabled
    }

    override fun shortName(): String = when (this) {
        is UnusedLoad -> "unused-load"
        is UnusedAssign -> "unused-assign"
        is UnusedArgument -> "unused-argument"
        is UsingUnassigned -> "using-unassigned"
        is UsingUndefined -> "using-undefined"
        is UsingMaybeUndefined -> "using-maybe-undefined"
    }

    fun about(): String = when (this) {
        is UnusedLoad -> name
        is UnusedAssign -> name
        is UnusedArgument -> name
        is UsingUnassigned -> name
        is UsingUndefined -> name
        is UsingMaybeUndefined -> name
    }
}

private enum class Kind {
    Load,
    Argument,
    Assign;

    fun unused(name: String): NameWarning = when (this) {
        Load -> NameWarning.UnusedLoad(name)
        Argument -> NameWarning.UnusedArgument(name)
        Assign -> NameWarning.UnusedAssign(name)
    }
}

private enum class Assigned {
    Definitely,
    Maybe,
}

private typealias AstStr = Spanned<String>

private fun astStrFromIdent(x: AstIdent): AstStr {
    // AstIdent = Spanned<IdentP<AstNoPayload, Unit>>, .node.ident is the string
    return Spanned(x.node.ident, x.span)
}

private fun astStrFromAssignIdent(x: AstAssignIdent): AstStr {
    // AstAssignIdent = Spanned<AssignIdentP<AstNoPayload, Unit>>, .node.ident is the string
    return Spanned(x.node.ident, x.span)
}

/// When we see a control flow operator, how far does it apply?
private enum class Abort {
    /// Abort the loop (e.g. `continue` or `break`)
    Loop,
    /// Abort the function (e.g. `return`)
    Function;
}

/// Is this function the global `fail` function.
/// Can technically be shadowed, but if you shadow it with something that doesn't fail
/// you're going to confuse users, so not too problematic.
private fun isFail(x: AstExpr): Boolean {
    val expr = x.node
    if (expr is ExprP.Call) {
        val func = expr.expr.node
        if (func is ExprP.Identifier<*, *>) {
            return func.ident.node.ident == "fail"
        }
    }
    return false
}

/// Visit all expression children of an assign target (e.g. index/dot exprs, not the lvalue itself).
private fun AstAssignTarget.visitExpr(visitor: (AstExpr) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    when (val t = this.node) {
        is AssignTargetP.Tuple -> t.elements.forEach { (it as AstAssignTarget).visitExpr(visitor) }
        is AssignTargetP.Index<*> -> {
            visitor(t.expr as AstExpr)
            visitor(t.index as AstExpr)
        }
        is AssignTargetP.Dot<*> -> visitor(t.expr as AstExpr)
        is AssignTargetP.Identifier<*, *> -> {} // no sub-expressions
    }
}

/// Visit all lvalue identifier leaves of an assign target.
private fun AstAssignTarget.visitLvalue(visitor: (AstAssignIdent) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    when (val t = this.node) {
        is AssignTargetP.Tuple -> t.elements.forEach { (it as AstAssignTarget).visitLvalue(visitor) }
        is AssignTargetP.Identifier<*, *> -> visitor(t.ident as AstAssignIdent)
        is AssignTargetP.Index<*>, is AssignTargetP.Dot<*> -> {} // not a simple lvalue
    }
}

/// Visit all expression children of a parameter (default value, type annotation).
private fun ParameterP<AstNoPayload>.visitExpr(visitor: (AstExpr) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    when (this) {
        is ParameterP.Normal -> {
            typ?.let { visitor(it.node.expr as AstExpr) }
            defaultVal?.let { visitor(it as AstExpr) }
        }
        is ParameterP.Args -> typ?.let { visitor(it.node.expr as AstExpr) }
        is ParameterP.KwArgs -> typ?.let { visitor(it.node.expr as AstExpr) }
        is ParameterP.NoArgs, is ParameterP.Slash -> {}
    }
}

/// Visit all expression children of an AstExpr (recursing one level via the visitor).
private fun AstExpr.visitExpr(visitor: (AstExpr) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    when (val e = this.node) {
        is ExprP.Tuple -> e.elements.forEach { visitor(it as AstExpr) }
        is ExprP.Dot<*> -> visitor(e.expr as AstExpr)
        is ExprP.Call<*> -> {
            visitor(e.expr as AstExpr)
            e.args.args.forEach { visitor(it.node.expr() as AstExpr) }
        }
        is ExprP.Index<*> -> {
            visitor(e.expr as AstExpr)
            visitor(e.index as AstExpr)
        }
        is ExprP.Index2<*> -> {
            visitor(e.expr as AstExpr)
            visitor(e.index0 as AstExpr)
            visitor(e.index1 as AstExpr)
        }
        is ExprP.Slice<*> -> {
            visitor(e.expr as AstExpr)
            e.start?.let { visitor(it as AstExpr) }
            e.stop?.let { visitor(it as AstExpr) }
            e.step?.let { visitor(it as AstExpr) }
        }
        is ExprP.Not<*> -> visitor(e.expr as AstExpr)
        is ExprP.Minus<*> -> visitor(e.expr as AstExpr)
        is ExprP.Plus<*> -> visitor(e.expr as AstExpr)
        is ExprP.BitNot<*> -> visitor(e.expr as AstExpr)
        is ExprP.Op<*> -> {
            visitor(e.lhs as AstExpr)
            visitor(e.rhs as AstExpr)
        }
        is ExprP.If<*> -> {
            visitor(e.cond as AstExpr)
            visitor(e.v1 as AstExpr)
            visitor(e.v2 as AstExpr)
        }
        is ExprP.ListExpr<*> -> e.elements.forEach { visitor(it as AstExpr) }
        is ExprP.Dict<*> -> e.elements.forEach { (k, v) ->
            visitor(k as AstExpr)
            visitor(v as AstExpr)
        }
        is ExprP.ListComprehension<*> -> {
            visitor(e.expr as AstExpr)
            visitor(e.forClause.over as AstExpr)
            e.clauses.forEach { clause ->
                when (clause) {
                    is ClauseP.For<*> -> visitor(clause.forClause.over as AstExpr)
                    is ClauseP.If<*> -> visitor(clause.cond as AstExpr)
                }
            }
        }
        is ExprP.DictComprehension<*> -> {
            visitor(e.key as AstExpr)
            visitor(e.value as AstExpr)
            visitor(e.forClause.over as AstExpr)
            e.clauses.forEach { clause ->
                when (clause) {
                    is ClauseP.For<*> -> visitor(clause.forClause.over as AstExpr)
                    is ClauseP.If<*> -> visitor(clause.cond as AstExpr)
                }
            }
        }
        is ExprP.FString<*> -> e.fstring.node.expressions.forEach { visitor(it as AstExpr) }
        is ExprP.Identifier<*, *>, is ExprP.Lambda<*, *>, is ExprP.Literal -> {}
    }
}

/// A combination of the scope information (what is set) and state information as we
/// step through the function.
private class ScopeState {
    /// Those identifiers that weren't set at that point, so we went to the parent.
    /// If these are set later on, then it is a unassigned usage error.
    val cantSet: MutableList<AstStr> = mutableListOf()
    /// Those identifiers that I couldn't find at the point I saw them.
    /// Since the child runs in a different order with respect to the parent, they might resolve later.
    val unbound: MutableList<AstStr> = mutableListOf()
    /// Those definition sites that we have ended up using.
    val used: MutableSet<AstStr> = mutableSetOf()
    /// Those definitions that we have set.
    val set: MutableList<Pair<AstStr, Kind>> = mutableListOf()
    /// The last location/locations where I was set.
    /// The assigned is whether I am always set or not.
    var lastSet: MutableMap<String, Pair<Assigned, MutableSet<Span>>> = mutableMapOf()
    /// Whether I can be reached.
    var abort: Abort? = null
}

/// The state we use when scanning the variables.
private class State(
    val codemap: CodeMap,
    /// Those that are set in the global scope.
    /// If null then assume anything might be set at the global scope.
    val globals: Set<String>?,
    /// These are the various scopes - one for the module, one for each def.
    val scopes: MutableList<ScopeState> = mutableListOf(),
    /// The current list of warnings.
    val warnings: MutableList<LintT<NameWarning>> = mutableListOf(),
    /// The things we have already warned about (no duplicates due to running loops twice).
    val warned: MutableSet<AstStr> = mutableSetOf(),
    /// How many nested loops we are in.
    var loopDepth: Int = 0,
) {
    fun addWarning(ident: AstStr, ctor: (String) -> NameWarning) {
        if (warned.add(ident)) {
            warnings.add(LintT.new(codemap, ident.span, ctor(ident.node)))
        }
    }

    // Scope stuff

    fun exitScope() {
        // The scope only collects things that could have been assigned, so should always be
        val scope = scopes.removeLast()
        // Variables defined in the local scope
        val local: Set<String> = scope.set.map { it.first.node }.toSet()

        // unset & set => we thought it was in the parent scope, but was actually undefined
        for (x in scope.cantSet) {
            if (x.node in local) {
                addWarning(x, NameWarning::UsingUnassigned)
            }
        }

        // some of our unbound variables were from our children
        // these might use any random variable at any point - impossible to know
        // so for those, just assume they are used
        val unboundDefined = scope.unbound.filter { it.node in local }
        val unboundUndefined = scope.unbound.filter { it.node !in local }
        val unboundDefinedNames: Set<String> = unboundDefined.map { it.node }.toSet()

        // set & !used => we set it but never used it in any branch
        // if we are in unbound,
        val top = scopes.isEmpty()
        for ((ident, kind) in scope.set) {
            if (ident !in scope.used && ident.node !in unboundDefinedNames) {
                val underscore = ident.node.startsWith('_')
                val exported = top
                    && !underscore
                    && kind == Kind.Assign // Assume loads don't automatically export
                    && scope.lastSet[ident.node]?.second?.contains(ident.span) == true
                val ignored = !top && underscore

                if (!exported && !ignored) {
                    addWarning(ident) { s -> kind.unused(s) }
                }
            }
        }

        // unbound & not defined => move to the parents unbound
        // unbound & no parent => undefined
        val parent = scopes.lastOrNull()
        if (parent == null) {
            if (globals != null) {
                for (x in unboundUndefined) {
                    if (x.node !in globals) {
                        addWarning(x, NameWarning::UsingUndefined)
                    }
                }
            }
        } else {
            // these things were unbound, but perhaps in a child, and perhaps we defined it later
            // as we can use a variable in a child before we define it, since the child is a def,
            // so runs later.
            for (x in unboundUndefined) {
                if (x.node !in local) {
                    parent.unbound.add(x)
                }
            }
        }
    }

    fun enterScope() {
        scopes.add(ScopeState())
    }

    // Nest stuff

    fun setAbort(abort: Abort) {
        scopes.last().abort = abort
    }

    fun loops(inner: (State) -> Unit) {
        loopDepth += 1
        // We run the loop twice since it might set a variable that is only used in subsequent iterations of the loop.
        // That means n nested loops are now O(2^n), but if n > 5, just give up and only iterate once
        // (a few too many warnings, bounded runtime).
        val iterations = if (loopDepth > 5) 1 else 2
        for (i in 0 until iterations) {
            branch(inner) { }
            val scope = scopes.last()
            if (scope.abort == Abort.Loop) {
                scope.abort = null
            }
        }
        loopDepth -= 1
    }

    fun branch(opt1: (State) -> Unit, opt2: (State) -> Unit) {
        val originalAbort = scopes.last().abort
        var other = HashMap(scopes.last().lastSet)
        opt1(this)
        val otherAbort = scopes.last().abort
        val temp = scopes.last().lastSet
        scopes.last().lastSet = other.toMutableMap()
        other = HashMap(temp)
        scopes.last().abort = originalAbort
        opt2(this)
        val currentAbort = scopes.last().abort

        // now need to merge `current` with `other`
        scopes.last().abort = minOfNullable(otherAbort, currentAbort)
        val current = scopes.last().lastSet

        val joinAssigned = { c: Assigned, o: Assigned ->
            when {
                currentAbort != null && otherAbort != null -> Assigned.Definitely // Probably irrelevant
                currentAbort != null && otherAbort == null -> o
                currentAbort == null && otherAbort != null -> c
                else -> {
                    if (c == Assigned.Maybe) Assigned.Maybe else o
                }
            }
        }

        val keys = mutableSetOf<String>()
        keys.addAll(current.keys)
        keys.addAll(other.keys)
        for (k in keys) {
            val currentEntry = current[k]
            val otherEntry = other[k]
            if (currentEntry == null) {
                if (otherEntry != null) {
                    current[k] = Pair(joinAssigned(Assigned.Maybe, otherEntry.first), otherEntry.second)
                }
            } else {
                if (otherEntry == null) {
                    current[k] = Pair(joinAssigned(currentEntry.first, Assigned.Maybe), currentEntry.second)
                } else {
                    val newAssigned = joinAssigned(currentEntry.first, otherEntry.first)
                    val newSpans = currentEntry.second.toMutableSet()
                    newSpans.addAll(otherEntry.second)
                    current[k] = Pair(newAssigned, newSpans)
                }
            }
        }
    }

    // Actual operations

    fun useIdent(ident: AstStr) {
        for ((depth, scope) in scopes.asReversed().withIndex()) {
            val entry = scope.lastSet[ident.node]
            if (entry == null) {
                if (depth == 0) {
                    scope.cantSet.add(ident)
                }
            } else {
                val (assigned, spans) = entry
                for (span in spans) {
                    scope.used.add(Spanned(ident.node, span))
                }
                if (assigned == Assigned.Maybe) {
                    addWarning(ident, NameWarning::UsingMaybeUndefined)
                }
                return
            }
        }
        scopes.last().unbound.add(ident)
    }

    fun setIdent(ident: AstAssignIdent, kind: Kind) {
        val astStr = astStrFromAssignIdent(ident)
        val scope = scopes.last()
        scope.set.add(Pair(astStr, kind))
        scope.lastSet[astStr.node] = Pair(Assigned.Definitely, mutableSetOf(astStr.span))
    }

    // Traverse the syntax tree

    fun assign(assign: AstAssignTarget) {
        assign.visitExpr { x -> expr(x) }
        assign.visitLvalue { x -> setIdent(x, Kind.Assign) }
    }

    fun comprehension(
        res1: AstExpr,
        res2: AstExpr?,
        forClause: io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP<AstNoPayload>,
        clauses: List<io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP<AstNoPayload>>,
    ) {
        expr(forClause.over)
        enterScope()
        // This isn't quite right, as we assume the comprehensions are always evaluated (e.g. no zero arrays)
        // and the assign is always hit, which isn't true.
        // But it is a close enough approximation, and comprehensions tend not to have variable assignment issues.
        assign(forClause.varTarget)
        for (clause in clauses) {
            when (clause) {
                is ClauseP.For -> {
                    expr(clause.forClause.over)
                    assign(clause.forClause.varTarget)
                }
                is ClauseP.If -> expr(clause.cond)
            }
        }
        expr(res1)
        if (res2 != null) expr(res2)
        exitScope()
    }

    @Suppress("UNCHECKED_CAST")
    fun expr(expr: AstExpr) {
        when (val e = expr.node) {
            is ExprP.Identifier<*, *> -> useIdent(astStrFromIdent(e.ident as AstIdent))
            is ExprP.Lambda<*, *> -> {
                for (p in e.lambda.params) {
                    (p.node as ParameterP<AstNoPayload>).visitExpr { x -> expr(x) }
                }
                enterScope()
                for (p in e.lambda.params) {
                    val pname = (p.node as ParameterP<AstNoPayload>).ident()
                    if (pname != null) {
                        setIdent(pname as AstAssignIdent, Kind.Argument)
                    }
                }
                expr(e.lambda.body as AstExpr)
                exitScope()
            }
            is ExprP.ListComprehension<*> -> comprehension(e.expr as AstExpr, null, e.forClause as io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP<AstNoPayload>, e.clauses as List<io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP<AstNoPayload>>)
            is ExprP.DictComprehension<*> -> comprehension(e.key as AstExpr, e.value as AstExpr, e.forClause as io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP<AstNoPayload>, e.clauses as List<io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP<AstNoPayload>>)
            else -> expr.visitExpr { x -> expr(x) }
        }
    }

    fun exprOpt(expr: AstExpr?) {
        if (expr != null) expr(expr)
    }

    fun typOpt(ty: AstTypeExpr?) {
        if (ty != null) typ(ty)
    }

    fun typ(ty: AstTypeExpr) {
        expr(ty.node.expr)
    }

    fun assignAsExpr(assign: AstAssignTarget) {
        assign.visitExpr { x -> expr(x) }
        assign.visitLvalue { x -> useIdent(astStrFromAssignIdent(x)) }
    }

    @Suppress("UNCHECKED_CAST")
    fun stmt(stmt: AstStmt) {
        when (val s = stmt.node) {
            is StmtP.Expression -> {
                expr(s.expr)
                if (isFail(s.expr)) {
                    setAbort(Abort.Function)
                }
            }
            is StmtP.Return -> {
                exprOpt(s.expr as AstExpr?)
                setAbort(Abort.Function)
            }
            is StmtP.Assign -> {
                typOpt(s.assign.ty as AstTypeExpr?)
                expr(s.assign.rhs as AstExpr)
                assign(s.assign.lhs as AstAssignTarget)
            }
            is StmtP.AssignModify -> {
                expr(s.rhs as AstExpr)
                assignAsExpr(s.lhs as AstAssignTarget)
                assign(s.lhs as AstAssignTarget)
            }
            is StmtP.Statements -> {
                for (x in s.stmts) {
                    stmt(x as AstStmt)
                }
            }
            is StmtP.If -> {
                expr(s.cond as AstExpr)
                branch({ me -> me.stmt(s.suite as AstStmt) }, { })
            }
            is StmtP.IfElse -> {
                expr(s.cond as AstExpr)
                branch({ me -> me.stmt(s.suite1 as AstStmt) }, { me -> me.stmt(s.suite2 as AstStmt) })
            }
            is StmtP.For -> {
                expr(s.forStmt.over as AstExpr)
                // Note this isn't 100% correct, as a for loop may set something the next iteration consumes
                loops { me ->
                    me.assign(s.forStmt.varTarget as AstAssignTarget)
                    me.stmt(s.forStmt.body as AstStmt)
                }
            }
            is StmtP.Def<*, *> -> {
                for (p in s.def.params) {
                    (p.node as ParameterP<AstNoPayload>).visitExpr { e -> expr(e) }
                }
                typOpt(s.def.returnType as AstTypeExpr?)
                setIdent(s.def.name as AstAssignIdent, Kind.Assign)
                enterScope()
                for (p in s.def.params) {
                    val pname = (p.node as ParameterP<AstNoPayload>).ident()
                    if (pname != null) {
                        setIdent(pname as AstAssignIdent, Kind.Argument)
                    }
                }
                stmt(s.def.body as AstStmt)
                exitScope()
            }
            // These were handled by collecting the scopes
            is StmtP.Load<*, *> -> {
                for (arg in s.loadStmt.args) {
                    setIdent(arg.local as AstAssignIdent, Kind.Load)
                }
            }
            // These control flow operators can be ignored - either the code after is fine (no problem)
            // or in error (in which case you have useless code after flow control)
            is StmtP.Break, is StmtP.Continue -> {
                setAbort(Abort.Loop)
            }
            is StmtP.Pass -> {}
        }
    }

    fun module(module: AstModule) {
        enterScope()
        stmt(module.statement)
        exitScope()
    }
}

private fun minOfNullable(a: Abort?, b: Abort?): Abort? {
    if (a == null) return b
    if (b == null) return a
    return if (a.ordinal <= b.ordinal) a else b
}

internal fun namesLint(
    module: AstModule,
    globals: Set<String>?,
): List<LintT<NameWarning>> {
    val state = State(
        codemap = module.codemap,
        globals = globals,
    )
    state.module(module)
    return state.warnings
}
