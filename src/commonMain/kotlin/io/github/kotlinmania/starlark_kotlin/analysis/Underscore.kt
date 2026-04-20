// port-lint: source src/analysis/underscore.rs
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

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP

// #[derive(Error, Debug)]
// pub(crate) enum UnderscoreWarning
internal sealed class UnderscoreWarning : LintWarning {
    /** Underscore definitions should be simple. */
    // #[error("Underscore definitions should be simple `{0}`")]
    data class UnderscoreDefinition(val name: String) : UnderscoreWarning() {
        override fun toString(): String = "Underscore definitions should be simple `$name`"
    }

    /** Used ignored variable. */
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

    /** Get the subject of the warning. */
    fun about(): String = when (this) {
        is UnderscoreDefinition -> name
        is UsingIgnored -> name
    }
}

// pub(crate) fn lint(module: &AstModule) -> Vec<LintT<UnderscoreWarning>>
internal fun underscoreLint(module: AstModule): List<LintT<UnderscoreWarning>> {
    val res = mutableListOf<LintT<UnderscoreWarning>>()
    inappropriateUnderscore(module.codemap, module.statement, true, res)
    useIgnored(module.codemap, module.statement, res)
    return res
}

// Visit immediate child statements of this Spanned<StmtP<AstNoPayload>> (local helper).
private fun Spanned<StmtP<AstNoPayload>>.visitStmtU(visitor: (Spanned<StmtP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Statements -> s.stmts.forEach { visitor(it as Spanned<StmtP<AstNoPayload>>) }
        is StmtP.Def<*, *> -> visitor(s.def.body as Spanned<StmtP<AstNoPayload>>)
        is StmtP.If -> visitor(s.suite as Spanned<StmtP<AstNoPayload>>)
        is StmtP.IfElse -> {
            visitor(s.suite1 as Spanned<StmtP<AstNoPayload>>)
            visitor(s.suite2 as Spanned<StmtP<AstNoPayload>>)
        }
        is StmtP.For -> visitor(s.forStmt.body as Spanned<StmtP<AstNoPayload>>)
        else -> {}
    }
}

// Visit immediate child expressions of this Spanned<ExprP<AstNoPayload>> (local helper).
private fun Spanned<ExprP<AstNoPayload>>.visitExprU(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val e = this.node) {
        is ExprP.Call -> {
            visitor(e.expr as Spanned<ExprP<AstNoPayload>>)
            for (arg in e.args.args) {
                visitor(arg.node.expr() as Spanned<ExprP<AstNoPayload>>)
            }
        }
        is ExprP.If -> {
            visitor(e.cond as Spanned<ExprP<AstNoPayload>>)
            visitor(e.v1 as Spanned<ExprP<AstNoPayload>>)
            visitor(e.v2 as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.Tuple -> e.elements.forEach { visitor(it as Spanned<ExprP<AstNoPayload>>) }
        is ExprP.ListExpr -> e.elements.forEach { visitor(it as Spanned<ExprP<AstNoPayload>>) }
        is ExprP.Dict -> e.elements.forEach { (k, v) ->
            visitor(k as Spanned<ExprP<AstNoPayload>>)
            visitor(v as Spanned<ExprP<AstNoPayload>>)
        }
        is ExprP.Lambda<*, *> -> visitor(e.lambda.body as Spanned<ExprP<AstNoPayload>>)
        else -> {}
    }
}

// Visit immediate child expressions of this Spanned<StmtP<AstNoPayload>> (local helper).
private fun Spanned<StmtP<AstNoPayload>>.visitStmtExprU(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Expression -> visitor(s.expr as Spanned<ExprP<AstNoPayload>>)
        is StmtP.Return -> (s.expr as Spanned<ExprP<AstNoPayload>>?)?.let(visitor)
        is StmtP.Statements -> s.stmts.forEach { (it as Spanned<StmtP<AstNoPayload>>).visitStmtExprU(visitor) }
        is StmtP.Def<*, *> -> (s.def.body as Spanned<StmtP<AstNoPayload>>).visitStmtExprU(visitor)
        is StmtP.If -> {
            visitor(s.cond as Spanned<ExprP<AstNoPayload>>)
            (s.suite as Spanned<StmtP<AstNoPayload>>).visitStmtExprU(visitor)
        }
        is StmtP.IfElse -> {
            visitor(s.cond as Spanned<ExprP<AstNoPayload>>)
            (s.suite1 as Spanned<StmtP<AstNoPayload>>).visitStmtExprU(visitor)
            (s.suite2 as Spanned<StmtP<AstNoPayload>>).visitStmtExprU(visitor)
        }
        is StmtP.For -> {
            visitor(s.forStmt.over as Spanned<ExprP<AstNoPayload>>)
            (s.forStmt.body as Spanned<StmtP<AstNoPayload>>).visitStmtExprU(visitor)
        }
        else -> {}
    }
}

/** There's no reason to make a def or lambda and give it an underscore name not at the top level. */
// fn inappropriate_underscore(codemap: &CodeMap, x: &Spanned<StmtP<AstNoPayload>>, top: bool, res: &mut Vec<LintT<UnderscoreWarning>>)
private fun inappropriateUnderscore(
    codemap: CodeMap,
    x: Spanned<StmtP<AstNoPayload>>,
    top: Boolean,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // Is this value allowed as an assignment to a boring identifier - just tuple of vars and var.
    // fn is_allowed(x: &Spanned<ExprP<AstNoPayload>>) -> bool
    fun isAllowed(x: Spanned<ExprP<AstNoPayload>>): Boolean {
        return when (val e = x.node) {
            is ExprP.Tuple<*> -> e.elements.isNotEmpty() && e.elements.all { it.node is ExprP.Identifier<*, *> }
            is ExprP.Identifier<*, *> -> true
            else -> false
        }
    }

    when (val s = x.node) {
        is StmtP.Def<*, *> -> {
            val name = s.def.name
            val nameIdent = (name as Spanned<*>).node as AssignIdentP<*, *>
            if (!top && nameIdent.ident.startsWith('_')) {
                res.add(
                    LintT.new(
                        codemap,
                        (name as Spanned<*>).span,
                        UnderscoreWarning.UnderscoreDefinition(nameIdent.ident),
                    )
                )
            }
            inappropriateUnderscore(codemap, s.def.body as Spanned<StmtP<AstNoPayload>>, false, res)
        }
        // StmtP<AstNoPayload>::Assign(assign) if !top =>
        is StmtP.Assign<*> -> if (!top) {
            val assign = s.assign
            val lhsNode = (assign.lhs as Spanned<*>).node
            if (lhsNode is AssignTargetP.Identifier<*, *>) {
                val identSpanned = lhsNode.ident as Spanned<*>
                val assignIdent = identSpanned.node as AssignIdentP<*, *>
                if (assignIdent.ident.startsWith('_') && !isAllowed(assign.rhs as Spanned<ExprP<AstNoPayload>>)) {
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

/** Don't want to use a variable that has been defined to be ignored. */
// fn use_ignored(codemap: &CodeMap, x: &Spanned<StmtP<AstNoPayload>>, res: &mut Vec<LintT<UnderscoreWarning>>)
private fun useIgnored(
    codemap: CodeMap,
    x: Spanned<StmtP<AstNoPayload>>,
    res: MutableList<LintT<UnderscoreWarning>>,
) {
    // We are ok with using things that were defined at the top level, but not nested.
    // fn root_definitions<'a>(x: &'a Spanned<StmtP<AstNoPayload>>, res: &mut HashSet<&'a str>)
    fun rootDefinitions(x: Spanned<StmtP<AstNoPayload>>, defs: MutableSet<String>) {
        when (val s = x.node) {
            is StmtP.Assign<*> -> {
                val lhsNode = (s.assign.lhs as Spanned<*>).node
                fun visitLvalue(target: Any?) {
                    when (target) {
                        is AssignTargetP.Tuple<*> -> target.elements.forEach { visitLvalue((it as Spanned<*>).node) }
                        is AssignTargetP.Identifier<*, *> -> {
                            val assignIdent = (target.ident as Spanned<*>).node as AssignIdentP<*, *>
                            defs.add(assignIdent.ident)
                        }
                        else -> {}
                    }
                }
                visitLvalue(lhsNode)
            }
            is StmtP.AssignModify<*> -> {
                val lhsNode = (s.lhs as Spanned<*>).node
                fun visitLvalue(target: Any?) {
                    when (target) {
                        is AssignTargetP.Tuple<*> -> target.elements.forEach { visitLvalue((it as Spanned<*>).node) }
                        is AssignTargetP.Identifier<*, *> -> {
                            val assignIdent = (target.ident as Spanned<*>).node as AssignIdentP<*, *>
                            defs.add(assignIdent.ident)
                        }
                        else -> {}
                    }
                }
                visitLvalue(lhsNode)
            }
            is StmtP.Def<*, *> -> {
                val nameIdent = (s.def.name as Spanned<*>).node as AssignIdentP<*, *>
                defs.add(nameIdent.ident)
            }
            is StmtP.Load<*, *> -> {
                for (arg in s.loadStmt.args) {
                    val localIdent = (arg.local as Spanned<*>).node as AssignIdentP<*, *>
                    defs.add(localIdent.ident)
                }
            }
            else -> x.visitStmtU { child -> rootDefinitions(child, defs) }
        }
    }

    // fn is_ignored(x: &str) -> bool
    fun isIgnored(name: String): Boolean {
        // we want things like __internal__ for builtin things to expose themselves quietly
        return name.startsWith('_') && !(name.startsWith("__") && name.endsWith("__"))
    }

    // fn check_expr(codemap: &CodeMap, x: &Spanned<ExprP<AstNoPayload>>, roots: &HashSet<&str>, res: &mut Vec<LintT<UnderscoreWarning>>)
    fun checkExpr(
        codemap: CodeMap,
        x: Spanned<ExprP<AstNoPayload>>,
        roots: Set<String>,
        res: MutableList<LintT<UnderscoreWarning>>,
    ) {
        when (val e = x.node) {
            is ExprP.Identifier<*, *> -> {
                val identSpanned = e.ident as Spanned<*>
                @Suppress("UNCHECKED_CAST")
                val ident = (identSpanned.node as io.github.kotlinmania.starlark_kotlin.syntax.ast.IdentP<*, *>).ident
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

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
