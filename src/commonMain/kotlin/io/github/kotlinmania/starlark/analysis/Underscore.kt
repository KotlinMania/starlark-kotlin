// port-lint: source src/analysis/underscore.rs

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
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstAssignTarget
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

internal sealed class UnderscoreWarning : LintWarning {
    /** Underscore definitions should be simple. */
    data class UnderscoreDefinition(
        val name: String,
    ) : UnderscoreWarning() {
        override fun toString(): String = "Underscore definitions should be simple `$name`"
    }

    /** Used ignored variable. */
    data class UsingIgnored(
        val name: String,
    ) : UnderscoreWarning() {
        override fun toString(): String = "Used ignored variable `$name`"
    }

    override fun severity(): EvalSeverity = EvalSeverity.Disabled

    override fun shortName(): String =
        when (this) {
            is UnderscoreDefinition -> "underscore-definition"
            is UsingIgnored -> "using-ignored"
        }

    /** Get the subject of the warning. */
    fun about(): String =
        when (this) {
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

/** There's no reason to make a def or lambda and give it an underscore name not at the top level. */
private fun inappropriateUnderscore(
    codemap: CodeMap,
    x: AstStmt,
    top: Boolean,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // Is this value allowed as an assignment to a boring identifier - just tuple of vars and var.
    fun isAllowed(x: AstExpr): Boolean =
        when (val e = x.node) {
            is ExprP.Tuple<AstNoPayload> -> e.elements.isNotEmpty() && e.elements.all { it.node is ExprP.Identifier<*, *> }
            is ExprP.Identifier<AstNoPayload, *> -> true
            else -> false
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
                    ),
                )
            }
            inappropriateUnderscore(codemap, s.def.body, false, res)
        }
        // Stmt::Assign(assign) if !top =>
        is StmtP.Assign<AstNoPayload> ->
            if (!top) {
                val assign = s.assign
                val lhsNode = assign.lhs.node
                if (lhsNode is AssignTargetP.Identifier<*, *>) {
                    val identSpanned = lhsNode.ident
                    val assignIdent = identSpanned.node
                    if (assignIdent.ident.startsWith('_') && !isAllowed(assign.rhs)) {
                        res.add(
                            LintT.new(
                                codemap,
                                identSpanned.span,
                                UnderscoreWarning.UnderscoreDefinition(assignIdent.ident),
                            ),
                        )
                    }
                }
            }
        else ->
            x.visitStmtChildren { child ->
                inappropriateUnderscore(codemap, child, top, res)
            }
    }
}

/** Don't want to use a variable that has been defined to be ignored. */
private fun useIgnored(
    codemap: CodeMap,
    x: AstStmt,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // We are ok with using things that were defined at the top level, but not nested.
    fun rootDefinitions(x: AstStmt, defs: MutableSet<String>) {
        when (val s = x.node) {
            is StmtP.Assign<AstNoPayload> -> {
                fun visitLvalue(target: AstAssignTarget) {
                    when (val targetNode = target.node) {
                        is AssignTargetP.Tuple<AstNoPayload> -> targetNode.elements.forEach { visitLvalue(it) }
                        is AssignTargetP.Identifier<AstNoPayload, *> -> defs.add(targetNode.ident.node.ident)
                        is AssignTargetP.Dot<AstNoPayload>,
                        is AssignTargetP.Index<AstNoPayload>,
                        -> {}
                    }
                }
                visitLvalue(s.assign.lhs)
            }
            is StmtP.AssignModify<AstNoPayload> -> {
                fun visitLvalue(target: AstAssignTarget) {
                    when (val targetNode = target.node) {
                        is AssignTargetP.Tuple<AstNoPayload> -> targetNode.elements.forEach { visitLvalue(it) }
                        is AssignTargetP.Identifier<AstNoPayload, *> -> defs.add(targetNode.ident.node.ident)
                        is AssignTargetP.Dot<AstNoPayload>,
                        is AssignTargetP.Index<AstNoPayload>,
                        -> {}
                    }
                }
                visitLvalue(s.lhs)
            }
            is StmtP.Def<AstNoPayload, *> -> defs.add(s.def.name.node.ident)
            is StmtP.Load<AstNoPayload, *> -> {
                for (arg in s.loadStmt.args) {
                    defs.add(arg.local.node.ident)
                }
            }
            else -> x.visitStmtChildren { child -> rootDefinitions(child, defs) }
        }
    }

    fun isIgnored(name: String): Boolean = name.startsWith('_') && !(name.startsWith("__") && name.endsWith("__"))

    fun checkExpr(
        codemap: CodeMap,
        x: AstExpr,
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
                        ),
                    )
                }
            }
            else -> x.node.visitChildExprs { child -> checkExpr(codemap, child, roots, res) }
        }
    }

    val roots = mutableSetOf<String>()
    rootDefinitions(x, roots)
    x.visitExprs { child -> checkExpr(codemap, child, roots, res) }
}

// Tests are in commonTest, not here.
