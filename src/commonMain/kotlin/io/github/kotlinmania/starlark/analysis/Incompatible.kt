// port-lint: source src/analysis/incompatible.rs
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
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

sealed class Incompatibility : LintWarning {
    /** Type check should be written differently. */
    data class IncompatibleTypeCheck(
        val original: String,
        val replacement: String,
    ) : Incompatibility() {
        override fun toString(): String =
            "Type check `$original` should be written `$replacement`"
    }

    /** Duplicate top-level assignment. */
    data class DuplicateTopLevelAssign(
        val name: String,
        val firstDefined: FileSpan,
    ) : Incompatibility() {
        override fun toString(): String =
            "Duplicate top-level assignment of `$name`, first defined at $firstDefined"
    }

    override fun severity(): EvalSeverity {
        return EvalSeverity.Warning
    }

    override fun shortName(): String {
        return when (this) {
            is IncompatibleTypeCheck -> "incompatible-type-check"
            is DuplicateTopLevelAssign -> "duplicate-top-level-assign"
        }
    }
}

private val TYPES: Map<String, String> = mapOf(
    "bool" to "True",
    "tuple" to "()",
    "str" to "\"\"",
    "list" to "[]",
    "int" to "0",
)

// --- Visitor helpers ---
// visitStmt helper: visit immediate child statements
private fun Spanned<StmtP<AstNoPayload>>.visitStmt(visitor: (Spanned<StmtP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Statements -> s.stmts.forEach(visitor)
        is StmtP.Def<AstNoPayload, *> -> visitor(s.def.body)
        else -> {}
    }
}

// visitExpr helper: visit immediate child expressions in a statement
private fun Spanned<StmtP<AstNoPayload>>.visitExpr(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val s = this.node) {
        is StmtP.Expression -> visitor(s.expr)
        is StmtP.Statements -> s.stmts.forEach { it.visitExpr(visitor) }
        is StmtP.Def<AstNoPayload, *> -> s.def.body.visitExpr(visitor)
        is StmtP.Assign -> visitor(s.assign.rhs)
        is StmtP.AssignModify -> visitor(s.rhs)
        else -> {}
    }
}

// visitExpr helper for expressions: visit immediate child expressions
private fun Spanned<ExprP<AstNoPayload>>.visitExpr(visitor: (Spanned<ExprP<AstNoPayload>>) -> Unit) {
    when (val e = this.node) {
        is ExprP.Call -> {
            visitor(e.expr)
            for (arg in e.args.args) {
                visitor(arg.node.expr())
            }
        }
        is ExprP.Op -> {
            visitor(e.lhs)
            visitor(e.rhs)
        }
        else -> {}
    }
}

// visitLvalue helper: visit all identifier lvalues in an assignment target
private fun Spanned<AssignTargetP<AstNoPayload>>.visitLvalue(visitor: (Spanned<AssignIdentP<AstNoPayload, *>>) -> Unit) {
    when (val t = this.node) {
        is AssignTargetP.Identifier<AstNoPayload, *> -> visitor(t.ident)
        is AssignTargetP.Tuple -> t.elements.forEach { it.visitLvalue(visitor) }
        else -> {} // Index, Dot don't contain identifiers
    }
}

private fun lookupType(x: Spanned<ExprP<AstNoPayload>>, types: Map<String, String>): String? {
    return when (val e = x.node) {
        is ExprP.Identifier<AstNoPayload, *> -> types[e.ident.node.ident]
        else -> null
    }
}

// Return true if this expression matches `type($x)`
private fun isTypeCall(x: Spanned<ExprP<AstNoPayload>>): Boolean {
    return when (val e = x.node) {
        is ExprP.Call<AstNoPayload> -> {
            if (e.args.args.size == 1) {
                val func = e.expr.node
                func is ExprP.Identifier<AstNoPayload, *> && func.ident.node.ident == "type"
            } else {
                false
            }
        }
        else -> false
    }
}

private fun matchBadTypeEquality(
    codemap: CodeMap,
    x: Spanned<ExprP<AstNoPayload>>,
    types: Map<String, String>,
    res: MutableList<LintT<Incompatibility>>,
) {
    // If we see type(x) == y (or negated), where y is in our types table, suggest a replacement
    when (val e = x.node) {
        is ExprP.Op -> {
            if ((e.op == BinOp.Equal || e.op == BinOp.NotEqual) && isTypeCall(e.lhs)) {
                val replacement = lookupType(e.rhs, types)
                if (replacement != null) {
                    res.add(
                        LintT.new(
                            codemap,
                            x.span,
                            Incompatibility.IncompatibleTypeCheck(
                                x.toString(),
                                "${e.lhs.node}${e.op}type($replacement)",
                            ),
                        )
                    )
                }
            }
        }
        else -> {}
    }
}

private fun badTypeEquality(module: AstModule, res: MutableList<LintT<Incompatibility>>) {
    val types = TYPES
    fun check(
        codemap: CodeMap,
        x: Spanned<ExprP<AstNoPayload>>,
        types: Map<String, String>,
        res: MutableList<LintT<Incompatibility>>,
    ) {
        matchBadTypeEquality(codemap, x, types, res)
        x.visitExpr { check(codemap, it, types, res) }
    }
    module.statement.visitExpr { check(module.codemap, it, types, res) }
}

// Go implementation of Starlark disallows duplicate top-level assignments,
// it's likely that will become Starlark standard sooner or later, so check now.
// The one place we allow it is to export something you grabbed with load.
private fun duplicateTopLevelAssignment(module: AstModule, res: MutableList<LintT<Incompatibility>>) {
    val defined = HashMap<String, Pair<Span, Boolean>>() // (name, (location, isLoad))
    val exported = HashSet<String>() // name's already exported by isLoad

    fun ident(
        x: Spanned<AssignIdentP<AstNoPayload, *>>,
        isLoad: Boolean,
        codemap: CodeMap,
        defined: HashMap<String, Pair<Span, Boolean>>,
        res: MutableList<LintT<Incompatibility>>,
    ) {
        val old = defined[x.node.ident]
        if (old != null) {
            res.add(
                LintT.new(
                    codemap,
                    x.span,
                    Incompatibility.DuplicateTopLevelAssign(x.node.ident, codemap.fileSpan(old.first)),
                )
            )
        } else {
            defined[x.node.ident] = Pair(x.span, isLoad)
        }
    }

    fun stmt(
        x: Spanned<StmtP<AstNoPayload>>,
        codemap: CodeMap,
        defined: HashMap<String, Pair<Span, Boolean>>,
        exported: HashSet<String>,
        res: MutableList<LintT<Incompatibility>>,
    ) {
        when (val s = x.node) {
            is StmtP.Assign -> {
                val lhsNode = s.assign.lhs.node
                val rhsNode = s.assign.rhs.node
                when {
                    lhsNode is AssignTargetP.Identifier<AstNoPayload, *>
                        && rhsNode is ExprP.Identifier<AstNoPayload, *>
                        && lhsNode.ident.node.ident == rhsNode.ident.node.ident
                        && defined[lhsNode.ident.node.ident]?.second == true
                        && !exported.contains(lhsNode.ident.node.ident) -> {
                        // Normally this would be an error, but if we load()'d it,
                        // this is how we'd reexport through Starlark.
                        // But only allow one export
                        exported.add(lhsNode.ident.node.ident)
                    }
                    else -> s.assign.lhs.visitLvalue { ident(it, false, codemap, defined, res) }
                }
            }
            is StmtP.AssignModify -> {
                s.lhs.visitLvalue { ident(it, false, codemap, defined, res) }
            }
            is StmtP.Def<AstNoPayload, *> -> {
                ident(s.def.name, false, codemap, defined, res)
            }
            is StmtP.Load<AstNoPayload, *> -> {
                for (arg in s.loadStmt.args) {
                    ident(arg.local, true, codemap, defined, res)
                }
            }
            // Visit statements, but don't descend under def - only top-level statements are interesting
            else -> x.visitStmt { stmt(it, codemap, defined, exported, res) }
        }
    }

    stmt(
        module.statement,
        module.codemap,
        defined,
        exported,
        res,
    )
}

/** Lint an AST module for incompatibilities. */
internal fun incompatibleLint(module: AstModule): List<LintT<Incompatibility>> {
    val res = mutableListOf<LintT<Incompatibility>>()
    badTypeEquality(module, res)
    duplicateTopLevelAssignment(module, res)
    return res
}
