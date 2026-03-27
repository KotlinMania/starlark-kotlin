// port-lint: source src/analysis/underscore.rs
package io.github.kotlinmania.starlark_kotlin.analysis

import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.Disabled
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.string.elems
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.enum_type.elements
import io.github.kotlinmania.starlark_kotlin.eval.compiler.thenExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.forP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.elseExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.cond
import io.github.kotlinmania.starlark_kotlin.entries
import io.github.kotlinmania.starlark_kotlin.docs.args
import io.github.kotlinmania.starlark_kotlin.codemap.*
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.names
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Ident
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
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

// Forward reference placeholder types for AST nodes not yet in flow.kt.
// These mirror the Rust `AssignP`, `AssignTarget`, `Stmt::Assign`, etc.
// Once the full AST is ported, these should be replaced with canonical imports.

/// An assign LHS target.
// use starlark_syntax::syntax::ast::AssignTarget;
private sealed class AssignTarget {
    class Tuple(val elements: List<AssignTarget>) : AssignTarget()
    class Index(val array: Any, val index: Any) : AssignTarget()
    class Dot(val obj: Any, val field: Any) : AssignTarget()
    class Identifier(val ident: CstAssignIdent) : AssignTarget()
}

/// A variable identifier in an assign LHS.
private class CstAssignIdent(
    val ident: String,
    val span: Span = Span(),
) {
    val node: CstAssignIdent get() = this
}

/// Assignment statement payload.
// use starlark_syntax::syntax::ast::AssignP;
private class AssignP(
    val lhs: Spanned<AssignTarget>,
    val rhs: AstExpr,
)

/// Load argument with local binding.
private class LoadArg(
    val local: Spanned<Ident>,
)

/// Load statement payload.
private class LoadP(
    val module: Spanned<String>,
    val args: List<LoadArg>,
)

// Extension: visit lvalues in an AssignTargetP.
private fun Spanned<AssignTarget>.visitLvalue(visitor: (CstAssignIdent) -> Unit) {
    when (val t = this.node) {
        is AssignTargetP.Tuple -> t.elements.forEach { Spanned(it).visitLvalue(visitor) }
        is AssignTargetP.Identifier -> visitor(t.ident)
        is AssignTargetP.Index -> {}
        is AssignTargetP.Dot -> {}
    }
}

// Extension: visit immediate child statements in an AstStmt.
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

// Extension: visit immediate child expressions in an AstStmt.
private fun AstStmt.visitExpr(visitor: (AstExpr) -> Unit) {
    when (val s = this.node) {
        is StmtP.Expression -> visitor(s.expr)
        is StmtP.Return -> s.expr?.let(visitor)
        is StmtP.Statements -> s.stmts.forEach { it.visitExpr(visitor) }
        is StmtP.Def -> s.def.body.visitExpr(visitor)
        is StmtP.If -> {
            visitor(s.cond)
            s.body.visitExpr(visitor)
        }
        is StmtP.IfElse -> {
            visitor(s.cond)
            s.bodies.first.visitExpr(visitor)
            s.bodies.second.visitExpr(visitor)
        }
        is StmtP.For -> {
            visitor(s.forP.var_)
            visitor(s.forP.over)
            s.forP.body.visitExpr(visitor)
        }
        else -> {}
    }
}

// Extension: visit child expressions in an AstExpr.
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

// #[derive(Error, Debug)]
// pub(crate) enum UnderscoreWarning
internal sealed class UnderscoreWarning : LintWarning {
    /// Underscore definitions should be simple.
    // #[error("Underscore definitions should be simple `{0}`")]
    data class UnderscoreDefinition(val name: String) : UnderscoreWarning() {
        override fun toString(): String = "Underscore definitions should be simple `$name`"
    }

    /// Used ignored variable.
    // #[error("Used ignored variable `{0}`")]
    data class UsingIgnored(val name: String) : UnderscoreWarning() {
        override fun toString(): String = "Used ignored variable `$name`"
    }

    // impl LintWarning for UnderscoreWarning

    override fun severity(): EvalSeverity = EvalSeverity.Disabled

    override fun shortName(): String = when (this) {
        is UnderscoreDefinition -> "underscore-definition"
        is UsingIgnored -> "using-ignored"
    }

    /// Get the subject of the warning.
    fun about(): String = when (this) {
        is UnderscoreDefinition -> name
        is UsingIgnored -> name
    }
}

// pub(crate) fn lint(module: &AstModule) -> Vec<LintT<UnderscoreWarning>>
internal fun lint(module: AstModule): List<LintT<UnderscoreWarning>> {
    val res = mutableListOf<LintT<UnderscoreWarning>>()
    inappropriateUnderscore(module.codemap, module.statement, true, res)
    useIgnored(module.codemap, module.statement, res)
    return res
}

/// There's no reason to make a def or lambda and give it an underscore name not at the top level.
// fn inappropriate_underscore(codemap: &CodeMap, x: &AstStmt, top: bool, res: &mut Vec<LintT<UnderscoreWarning>>)
private fun inappropriateUnderscore(
    codemap: CodeMap,
    x: AstStmt,
    top: Boolean,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // Is this value allowed as an assignment to a boring identifier - just tuple of vars and var.
    // fn is_allowed(x: &AstExpr) -> bool
    fun isAllowed(x: AstExpr): Boolean {
        return when (val e = x.node) {
            is ExprP.Tuple -> e.elems.isNotEmpty() && e.elems.all { it.node is ExprP.Identifier }
            is ExprP.Identifier -> true
            else -> false
        }
    }

    when (val s = x.node) {
        is StmtP.Def -> {
            val name = s.def.name
            if (!top && name.node.ident.startsWith('_')) {
                res.add(
                    LintT.new(
                        codemap,
                        name.span,
                        UnderscoreWarning.UnderscoreDefinition(name.node.ident),
                    )
                )
            }
            inappropriateUnderscore(codemap, s.def.body, false, res)
        }
        // Stmt::Assign(assign) if !top =>
        // Note: flow.kt Stmt doesn't have Assign variant yet.
        // When it does, this branch should match on StmtP.Assign.
        // For now, this is a forward reference placeholder.
        else -> x.visitStmt { child ->
            inappropriateUnderscore(codemap, child, top, res)
        }
    }
}

/// Don't want to use a variable that has been defined to be ignored.
// fn use_ignored(codemap: &CodeMap, x: &AstStmt, res: &mut Vec<LintT<UnderscoreWarning>>)
private fun useIgnored(
    codemap: CodeMap,
    x: AstStmt,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // We are ok with using things that were defined at the top level, but not nested.
    // fn root_definitions<'a>(x: &'a AstStmt, res: &mut HashSet<&'a str>)
    fun rootDefinitions(x: AstStmt, defs: MutableSet<String>) {
        when (val s = x.node) {
            is StmtP.Def -> {
                defs.add(s.def.name.node.ident)
            }
            is StmtP.Load -> {
                // flow.kt Load has (module, names) - add all names
                for (name in s.names) {
                    defs.add(name)
                }
            }
            // Stmt::Assign and Stmt::AssignModify would visit lvalues here.
            // Forward reference: when Assign variant is added to Stmt, handle it.
            else -> x.visitStmt { child -> rootDefinitions(child, defs) }
        }
    }

    // fn is_ignored(x: &str) -> bool
    fun isIgnored(name: String): Boolean {
        // we want things like __internal__ for builtin things to expose themselves quietly
        return name.startsWith('_') && !(name.startsWith("__") && name.endsWith("__"))
    }

    // fn check_expr(codemap: &CodeMap, x: &AstExpr, roots: &HashSet<&str>, res: &mut Vec<LintT<UnderscoreWarning>>)
    fun checkExpr(
        codemap: CodeMap,
        x: AstExpr,
        roots: Set<String>,
        res: MutableList<LintT<UnderscoreWarning>>,
    ) {
        when (val e = x.node) {
            is ExprP.Identifier -> {
                val ident = e.name.node.ident
                if (isIgnored(ident) && ident !in roots) {
                    res.add(
                        LintT.new(
                            codemap,
                            e.name.span,
                            UnderscoreWarning.UsingIgnored(ident),
                        )
                    )
                }
            }
            else -> x.visitExpr { child -> checkExpr(codemap, child, roots, res) }
        }
    }

    val roots = mutableSetOf<String>()
    rootDefinitions(x, roots)
    x.visitExpr { child -> checkExpr(codemap, child, roots, res) }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
