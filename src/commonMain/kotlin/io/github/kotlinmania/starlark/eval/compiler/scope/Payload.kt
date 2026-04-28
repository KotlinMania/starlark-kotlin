// port-lint: source src/eval/compiler/scope/payload.rs
package io.github.kotlinmania.starlark.eval.compiler.scope

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

import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopeData
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.ScopeId
import io.github.kotlinmania.starlark.eval.compiler.visitExprMut
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.syntax.ast.LambdaP
import io.github.kotlinmania.starlark.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.InternalError
import io.github.kotlinmania.starlark.typing.Interface
import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Spanned

/** Compiler-specific AST payload. */
object CstPayload : AstPayload

internal data class CstTypeExprPayload(
    /** Populated before evaluation of top level statements in normal evaluation. */
    var compilerTy: Ty? = null,
    /** Populated during lightweight evaluation for the lint type checker. */
    var typecheckerTy: Ty? = null,
)

private class CompilerAstMap(
    private val scopeData: ModuleScopeData,
    private val loads: Map<String, Interface>,
) {
    fun mapLoad(importPath: String, unit: Unit): Interface {
        return loads[importPath] ?: Interface.empty()
    }

    fun mapIdent(unit: Unit): ResolvedIdent? {
        return null
    }

    fun mapIdentAssign(unit: Unit): BindingId? {
        return null
    }

    fun mapDef(unit: Unit): ScopeId {
        return scopeData.newScope().first
    }

    fun mapTypeExpr(unit: Unit): CstTypeExprPayload {
        return CstTypeExprPayload()
    }
}

internal fun cstStmtFromAst(
    stmt: Spanned<StmtP<AstNoPayload>>,
    scopeData: ModuleScopeData,
    loads: Map<String, Interface>,
): Spanned<StmtP<CstPayload>> {
    // Reinterpret the AST under a different payload tag — the AST nodes are
    // structurally identical and CompilerAstMap fills in the new payload values below.
    val cst = stmt as Spanned<StmtP<CstPayload>>
    mapPayloadsStmt(cst, CompilerAstMap(scopeData, loads))
    return cst
}

internal fun Spanned<AssignIdentP<CstPayload, *>>.resolvedBindingId(codemap: CodeMap): BindingId {
    val bindingId = this.node.payload as? BindingId
    return bindingId
        ?: throw InternalError.msg(
            "Binding id is not filled",
            this.span,
            codemap,
        )
}

// Payload writes go through star-projected nodes; centralize the cast so
// each concrete write below stays a single typed assignment.
private fun setIdentPayload(ident: IdentP<CstPayload, *>, value: ResolvedIdent?) {
    (ident as IdentP<CstPayload, ResolvedIdent?>).payload = value
}

private fun setAssignIdentPayload(ident: AssignIdentP<CstPayload, *>, value: BindingId?) {
    (ident as AssignIdentP<CstPayload, BindingId?>).payload = value
}

private fun setLambdaPayload(lambda: LambdaP<CstPayload, *>, value: ScopeId) {
    (lambda as LambdaP<CstPayload, ScopeId>).payload = value
}

private fun setDefPayload(def: io.github.kotlinmania.starlark.syntax.ast.DefP<CstPayload, *>, value: ScopeId) {
    (def as io.github.kotlinmania.starlark.syntax.ast.DefP<CstPayload, ScopeId>).payload = value
}

private fun setLoadPayload(load: LoadP<CstPayload, *>, value: Interface) {
    (load as LoadP<CstPayload, Interface>).payload = value
}

private fun setTypeExprPayload(typeExpr: TypeExprP<CstPayload, *>, value: CstTypeExprPayload) {
    (typeExpr as TypeExprP<CstPayload, CstTypeExprPayload>).payload = value
}

private fun mapPayloadsStmt(
    stmt: Spanned<StmtP<CstPayload>>,
    mapper: CompilerAstMap,
) {
    when (val node = stmt.node) {
        is StmtP.Statements -> node.stmts.forEach { mapPayloadsStmt(it, mapper) }

        is StmtP.Def<CstPayload, *> -> {
            val defP = node.def
            mapPayloadsAssignIdent(defP.name, mapper)
            for (p in defP.params) {
                mapPayloadsParam(p.node, mapper)
            }
            defP.returnType?.let { mapPayloadsTypeExpr(it, mapper) }
            setDefPayload(defP, mapper.mapDef(Unit))
            mapPayloadsStmt(defP.body, mapper)
        }

        is StmtP.For -> {
            val forP = node.forStmt
            mapPayloadsAssignTarget(forP.varTarget, mapper)
            mapPayloadsExpr(forP.over, mapper)
            mapPayloadsStmt(forP.body, mapper)
        }

        is StmtP.If -> {
            mapPayloadsExpr(node.cond, mapper)
            mapPayloadsStmt(node.suite, mapper)
        }

        is StmtP.IfElse -> {
            mapPayloadsExpr(node.cond, mapper)
            mapPayloadsStmt(node.suite1, mapper)
            mapPayloadsStmt(node.suite2, mapper)
        }

        is StmtP.Return ->
            node.expr?.let { mapPayloadsExpr(it, mapper) }

        is StmtP.Expression -> mapPayloadsExpr(node.expr, mapper)

        is StmtP.Assign -> {
            mapPayloadsAssignTarget(node.assign.lhs, mapper)
            node.assign.ty?.let { mapPayloadsTypeExpr(it, mapper) }
            mapPayloadsExpr(node.assign.rhs, mapper)
        }

        is StmtP.AssignModify -> {
            mapPayloadsAssignTarget(node.lhs, mapper)
            mapPayloadsExpr(node.rhs, mapper)
        }

        is StmtP.Load<CstPayload, *> -> {
            val loadP = node.loadStmt
            val importPath = loadP.module.node
            setLoadPayload(loadP, mapper.mapLoad(importPath, Unit))
            for (arg in loadP.args) {
                mapPayloadsAssignIdent(arg.local, mapper)
            }
        }

        is StmtP.Break, is StmtP.Continue, is StmtP.Pass -> {}
    }
}

private fun mapPayloadsExpr(
    expr: Spanned<ExprP<CstPayload>>,
    mapper: CompilerAstMap,
) {
    when (val node = expr.node) {
        is ExprP.Identifier<CstPayload, *> -> {
            setIdentPayload(node.ident.node, mapper.mapIdent(Unit))
        }
        is ExprP.Lambda<CstPayload, *> -> {
            val lambdaP = node.lambda
            for (p in lambdaP.params) {
                mapPayloadsParam(p.node, mapper)
            }
            setLambdaPayload(lambdaP, mapper.mapDef(Unit))
            mapPayloadsExpr(lambdaP.body, mapper)
        }
        is ExprP.ListComprehension -> {
            mapPayloadsExpr(node.expr, mapper)
            mapPayloadsForClause(node.forClause, mapper)
            for (clause in node.clauses) {
                mapPayloadsClause(clause, mapper)
            }
        }
        is ExprP.DictComprehension -> {
            mapPayloadsExpr(node.key, mapper)
            mapPayloadsExpr(node.value, mapper)
            mapPayloadsForClause(node.forClause, mapper)
            for (clause in node.clauses) {
                mapPayloadsClause(clause, mapper)
            }
        }
        else -> {
            expr.visitExprMut { child -> mapPayloadsExpr(child, mapper) }
        }
    }
}

private fun mapPayloadsParam(
    param: ParameterP<CstPayload>,
    mapper: CompilerAstMap,
) {
    val ident = param.ident()
    if (ident != null) {
        mapPayloadsAssignIdent(ident, mapper)
    }
    param.visitExprMut { e -> mapPayloadsExpr(e, mapper) }
}

private fun mapPayloadsAssignTarget(
    target: Spanned<AssignTargetP<CstPayload>>,
    mapper: CompilerAstMap,
) {
    when (val node = target.node) {
        is AssignTargetP.Identifier<CstPayload, *> ->
            mapPayloadsAssignIdent(node.ident, mapper)
        is AssignTargetP.Tuple ->
            for (elem in node.elements) {
                mapPayloadsAssignTarget(elem, mapper)
            }
        is AssignTargetP.Index -> {
            mapPayloadsExpr(node.expr, mapper)
            mapPayloadsExpr(node.index, mapper)
        }
        is AssignTargetP.Dot ->
            mapPayloadsExpr(node.expr, mapper)
    }
}

private fun mapPayloadsAssignIdent(
    ident: Spanned<AssignIdentP<CstPayload, *>>,
    mapper: CompilerAstMap,
) {
    setAssignIdentPayload(ident.node, mapper.mapIdentAssign(Unit))
}

private fun mapPayloadsTypeExpr(
    typeExpr: Spanned<TypeExprP<CstPayload, *>>,
    mapper: CompilerAstMap,
) {
    setTypeExprPayload(typeExpr.node, mapper.mapTypeExpr(Unit))
    mapPayloadsExpr(typeExpr.node.expr, mapper)
}

private fun mapPayloadsForClause(
    forClause: ForClauseP<CstPayload>,
    mapper: CompilerAstMap,
) {
    mapPayloadsAssignTarget(
        forClause.varTarget,
        mapper,
    )
    mapPayloadsExpr(forClause.over, mapper)
}

private fun mapPayloadsClause(
    clause: ClauseP<CstPayload>,
    mapper: CompilerAstMap,
) {
    when (clause) {
        is ClauseP.For -> mapPayloadsForClause(clause.forClause, mapper)
        is ClauseP.If -> mapPayloadsExpr(clause.cond, mapper)
    }
}

internal typealias CstExpr = Spanned<ExprP<CstPayload>>
internal typealias CstTypeExpr = Spanned<TypeExprP<CstPayload, *>>
internal typealias CstAssignTarget = Spanned<AssignTargetP<CstPayload>>
internal typealias CstAssignIdent = Spanned<AssignIdentP<CstPayload, *>>
internal typealias CstIdent = Spanned<IdentP<CstPayload, *>>
internal typealias CstParameter = ParameterP<CstPayload>
internal typealias CstStmt = Spanned<StmtP<CstPayload>>
