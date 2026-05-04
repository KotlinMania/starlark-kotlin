// port-lint: source analysis/underscore.rs
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
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

internal sealed class UnderscoreWarning : LintWarning {
    /** Underscore definitions should be simple. */
    data class UnderscoreDefinition(val name: String) : UnderscoreWarning() {
        override fun toString(): String = "Underscore definitions should be simple `$name`"
    }

    /** Used ignored variable. */
    data class UsingIgnored(val name: String) : UnderscoreWarning() {
        override fun toString(): String = "Used ignored variable `$name`"
    }

    override fun severity(): EvalSeverity = EvalSeverity.Disabled

    override fun shortName(): String = when (this) {
        is UnderscoreDefinition -> "underscore-definition"
        is UsingIgnored -> "using-ignored"
    }

    /** Get the subject of the warning. */
    fun about(): String = when (this) {
        is UnderscoreDefinition -> name
        is UsingIgnored -> name
    }
}

internal fun underscoreLint(module: AstModule): List<LintT<UnderscoreWarning>> {
    val res = mutableListOf<LintT<UnderscoreWarning>>()
    inappropriateUnderscore(module.codemap, module.statement, true, res)
    useIgnored(module.codemap, module.statement, res)
    return res
}

// Visit immediate child statements of this Spanned<StmtP<AstNoPayload>> (local helper).
private fun Spanned<StmtP<AstNoPayload>>.visitStmtU(visitor: (Spanned<StmtP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Statements -> s.stmts.forEach { visitor(it) }
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

// Visit immediate child expressions of this Spanned<ExprP<AstNoPayload>> (local helper).
private fun Spanned<ExprP<AstNoPayload>>.visitExprU(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
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

// Visit immediate child expressions of this Spanned<StmtP<AstNoPayload>> (local helper).
private fun Spanned<StmtP<AstNoPayload>>.visitStmtExprU(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Expression -> visitor(s.expr)
        is StmtP.Return -> (s.expr)?.let(visitor)
        is StmtP.Statements -> s.stmts.forEach { (it).visitStmtExprU(visitor) }
        is StmtP.Def<AstNoPayload, *> -> s.def.body.visitStmtExprU(visitor)
        is StmtP.If -> {
            visitor(s.cond)
            (s.suite).visitStmtExprU(visitor)
        }
        is StmtP.IfElse -> {
            visitor(s.cond)
            (s.suite1).visitStmtExprU(visitor)
            (s.suite2).visitStmtExprU(visitor)
        }
        is StmtP.For -> {
            visitor(s.forStmt.over)
            (s.forStmt.body).visitStmtExprU(visitor)
        }
        else -> {}
    }
}

/** There's no reason to make a def or lambda and give it an underscore name not at the top level. */
internal fun inappropriateUnderscore(
    codemap: CodeMap,
    x: Spanned<StmtP<AstNoPayload>>,
    top: Boolean,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // Is this value allowed as an assignment to a boring identifier - just tuple of vars and var.
    fun isAllowed(x: Spanned<ExprP<AstNoPayload>>): Boolean {
        return when (val e = x.node) {
            is ExprP.Tuple -> e.elements.isNotEmpty() && e.elements.all { it.node is ExprP.Identifier<AstNoPayload, *> }
            is ExprP.Identifier<AstNoPayload, *> -> true
            else -> false
        }
    }

    when (val s = x.node) {
        is StmtP.Def<AstNoPayload, *> -> {
            val name = s.def.name
            val nameIdent = name.node
            if (!top && nameIdent.ident.startsWith('_')) {
                res.add(
                    LintT.new(
                        codemap,
                        name.span,
                        UnderscoreWarning.UnderscoreDefinition(nameIdent.ident),
                    )
                )
            }
            inappropriateUnderscore(codemap, s.def.body, false, res)
        }
        // StmtP<AstNoPayload>::Assign(assign) if !top =>
        is StmtP.Assign -> if (!top) {
            val assign = s.assign
            val lhs = assign.lhs
            val lhsNode = lhs.node
            if (lhsNode is AssignTargetP.Identifier<AstNoPayload, *>) {
                val identSpanned = lhsNode.ident
                val assignIdent = identSpanned.node
                if (assignIdent.ident.startsWith('_') && !isAllowed(assign.rhs)) {
                    res.add(
                        LintT.new(
                            codemap,
                            identSpanned.span,
                            UnderscoreWarning.UnderscoreDefinition(assignIdent.ident),
                        )
                    )
                }
            }
        }
        else -> x.visitStmtU { child ->
            inappropriateUnderscore(codemap, child, top, res)
        }
    }
}

/** Don't want to import a variable that has been defined to be ignored. */
internal fun useIgnored(
    codemap: CodeMap,
    x: Spanned<StmtP<AstNoPayload>>,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // We are ok with using things that were defined at the top level, but not nested.
    fun visitLvalue(target: AssignTargetP<AstNoPayload>, defs: MutableSet<String>) {
        when (target) {
            is AssignTargetP.Tuple -> target.elements.forEach { visitLvalue(it.node, defs) }
            is AssignTargetP.Identifier<AstNoPayload, *> -> defs.add(target.ident.node.ident)
            else -> {}
        }
    }

    fun rootDefinitions(x: Spanned<StmtP<AstNoPayload>>, defs: MutableSet<String>) {
        when (val s = x.node) {
            is StmtP.Assign -> visitLvalue(s.assign.lhs.node, defs)
            is StmtP.AssignModify -> visitLvalue(s.lhs.node, defs)
            is StmtP.Def<AstNoPayload, *> -> defs.add(s.def.name.node.ident)
            is StmtP.Load<AstNoPayload, *> -> {
                for (arg in s.loadStmt.args) {
                    defs.add(arg.local.node.ident)
                }
            }
            else -> x.visitStmtU { child -> rootDefinitions(child, defs) }
        }
    }

    fun isIgnored(name: String): Boolean {
        // we want things like __internal__ for builtin things to expose themselves quietly
        return name.startsWith('_') && !(name.startsWith("__") && name.endsWith("__"))
    }

    fun checkExpr(
        codemap: CodeMap,
        x: Spanned<ExprP<AstNoPayload>>,
        roots: Set<String>,
        res: MutableList<LintT<UnderscoreWarning>>,
    ) {
        when (val e = x.node) {
            is ExprP.Identifier<AstNoPayload, *> -> {
                val identSpanned = e.ident
                val ident = identSpanned.node.ident
                if (isIgnored(ident) && ident !in roots) {
                    res.add(
                        LintT.new(
                            codemap,
                            identSpanned.span,
                            UnderscoreWarning.UsingIgnored(ident),
                        )
                    )
                }
            }
            else -> x.visitExprU { child -> checkExpr(codemap, child, roots, res) }
        }
    }

    val roots = mutableSetOf<String>()
    rootDefinitions(x, roots)
    x.visitStmtExprU { child -> checkExpr(codemap, child, roots, res) }
}

// Tests are in commonTest, not here.
