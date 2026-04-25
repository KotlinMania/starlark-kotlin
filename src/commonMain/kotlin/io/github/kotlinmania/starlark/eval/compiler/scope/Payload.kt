// port-lint: source src/eval/compiler/scope/payload.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.scope

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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopeData
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ScopeId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.visitExprMut
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.IdentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LambdaP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadArgP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.InternalError
import io.github.kotlinmania.starlark_kotlin.typing.Interface
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned

/** Compiler-specific AST payload. */
// #[derive(Debug, Clone)]
// pub(crate) struct CstPayload
// impl AstPayload for CstPayload
object CstPayload : AstPayload

// #[derive(Default, Debug, Clone)]
// pub(crate) struct CstTypeExprPayload
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
    // fn map_load(&mut self, import_path: &str, (): ()) -> Interface
    fun mapLoad(importPath: String, @Suppress("UNUSED_PARAMETER") unit: Unit): Interface {
        return loads[importPath] ?: Interface.empty()
    }

    // fn map_ident(&mut self, (): ()) -> Option<ResolvedIdent>
    fun mapIdent(@Suppress("UNUSED_PARAMETER") unit: Unit): ResolvedIdent? {
        return null
    }

    // fn map_ident_assign(&mut self, (): ()) -> Option<BindingId>
    fun mapIdentAssign(@Suppress("UNUSED_PARAMETER") unit: Unit): BindingId? {
        return null
    }

    // fn map_def(&mut self, (): ()) -> ScopeId
    fun mapDef(@Suppress("UNUSED_PARAMETER") unit: Unit): ScopeId {
        return scopeData.newScope().first
    }

    // fn map_type_expr(&mut self, (): ()) -> CstTypeExprPayload
    fun mapTypeExpr(@Suppress("UNUSED_PARAMETER") unit: Unit): CstTypeExprPayload {
        return CstTypeExprPayload()
    }
}

// pub(crate) trait CstStmtFromAst
internal fun cstStmtFromAst(
    stmt: Spanned<StmtP<AstNoPayload>>,
    scopeData: ModuleScopeData,
    loads: Map<String, Interface>,
): Spanned<StmtP<CstPayload>> {
    @Suppress("UNCHECKED_CAST")
    val cst = stmt as Spanned<StmtP<CstPayload>>
    mapPayloadsStmt(cst, CompilerAstMap(scopeData, loads))
    return cst
}

// pub(crate) trait CstAssignIdentExt
internal fun Spanned<AssignIdentP<CstPayload, *>>.resolvedBindingId(codemap: CodeMap): BindingId {
    val bindingId = this.node.payload as? BindingId
    return bindingId
        ?: throw InternalError.msg(
            "Binding id is not filled",
            this.span,
            codemap,
        )
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsStmt(
    stmt: Spanned<StmtP<CstPayload>>,
    mapper: CompilerAstMap,
) {
    when (val node = stmt.node) {
        is StmtP.Statements<*> ->
            (node.stmts as List<Spanned<StmtP<CstPayload>>>).forEach { mapPayloadsStmt(it, mapper) }

        is StmtP.Def<*, *> -> {
            val defP = node.def as io.github.kotlinmania.starlark_kotlin.syntax.ast.DefP<CstPayload, Any?>
            mapPayloadsAssignIdent(defP.name as Spanned<AssignIdentP<CstPayload, Any?>>, mapper)
            for (p in defP.params as List<Spanned<ParameterP<CstPayload>>>) {
                mapPayloadsParam(p.node, mapper)
            }
            defP.returnType?.let { mapPayloadsTypeExpr(it as Spanned<TypeExprP<CstPayload, Any?>>, mapper) }
            defP.payload = mapper.mapDef(Unit)
            mapPayloadsStmt(defP.body as Spanned<StmtP<CstPayload>>, mapper)
        }

        is StmtP.For<*> -> {
            val forP = node.forStmt as ForP<CstPayload>
            mapPayloadsAssignTarget(forP.varTarget as Spanned<AssignTargetP<CstPayload>>, mapper)
            mapPayloadsExpr(forP.over as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsStmt(forP.body as Spanned<StmtP<CstPayload>>, mapper)
        }

        is StmtP.If<*> -> {
            mapPayloadsExpr(node.cond as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsStmt(node.suite as Spanned<StmtP<CstPayload>>, mapper)
        }

        is StmtP.IfElse<*> -> {
            mapPayloadsExpr(node.cond as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsStmt(node.suite1 as Spanned<StmtP<CstPayload>>, mapper)
            mapPayloadsStmt(node.suite2 as Spanned<StmtP<CstPayload>>, mapper)
        }

        is StmtP.Return<*> ->
            node.expr?.let { mapPayloadsExpr(it as Spanned<ExprP<CstPayload>>, mapper) }

        is StmtP.Expression<*> ->
            mapPayloadsExpr(node.expr as Spanned<ExprP<CstPayload>>, mapper)

        is StmtP.Assign<*> -> {
            mapPayloadsAssignTarget(node.assign.lhs as Spanned<AssignTargetP<CstPayload>>, mapper)
            node.assign.ty?.let { mapPayloadsTypeExpr(it as Spanned<TypeExprP<CstPayload, Any?>>, mapper) }
            mapPayloadsExpr(node.assign.rhs as Spanned<ExprP<CstPayload>>, mapper)
        }

        is StmtP.AssignModify<*> -> {
            mapPayloadsAssignTarget(node.lhs as Spanned<AssignTargetP<CstPayload>>, mapper)
            mapPayloadsExpr(node.rhs as Spanned<ExprP<CstPayload>>, mapper)
        }

        is StmtP.Load<*, *> -> {
            val loadP = node.loadStmt as LoadP<CstPayload, Any?>
            val importPath = loadP.module.node
            loadP.payload = mapper.mapLoad(importPath, Unit)
            for (arg in loadP.args) {
                mapPayloadsAssignIdent(
                    (arg as LoadArgP<CstPayload, Any?>).local as Spanned<AssignIdentP<CstPayload, Any?>>,
                    mapper,
                )
            }
        }

        is StmtP.Break<*>, is StmtP.Continue<*>, is StmtP.Pass<*> -> {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsExpr(
    expr: Spanned<ExprP<CstPayload>>,
    mapper: CompilerAstMap,
) {
    when (val node = expr.node) {
        is ExprP.Identifier<*, *> -> {
            val identP = node.ident as Spanned<IdentP<CstPayload, Any?>>
            identP.node.payload = mapper.mapIdent(Unit)
        }
        is ExprP.Lambda<*, *> -> {
            val lambdaP = node.lambda as LambdaP<CstPayload, Any?>
            for (p in lambdaP.params as List<Spanned<ParameterP<CstPayload>>>) {
                mapPayloadsParam(p.node, mapper)
            }
            lambdaP.payload = mapper.mapDef(Unit)
            mapPayloadsExpr(lambdaP.body as Spanned<ExprP<CstPayload>>, mapper)
        }
        is ExprP.ListComprehension<*> -> {
            mapPayloadsExpr(node.expr as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsForClause(node.forClause as ForClauseP<CstPayload>, mapper)
            for (clause in node.clauses as List<ClauseP<CstPayload>>) {
                mapPayloadsClause(clause, mapper)
            }
        }
        is ExprP.DictComprehension<*> -> {
            mapPayloadsExpr(node.key as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsExpr(node.value as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsForClause(node.forClause as ForClauseP<CstPayload>, mapper)
            for (clause in node.clauses as List<ClauseP<CstPayload>>) {
                mapPayloadsClause(clause, mapper)
            }
        }
        else -> {
            expr.visitExprMut { child -> mapPayloadsExpr(child, mapper) }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsParam(
    param: ParameterP<CstPayload>,
    mapper: CompilerAstMap,
) {
    val ident = param.ident()
    if (ident != null) {
        mapPayloadsAssignIdent(ident as Spanned<AssignIdentP<CstPayload, Any?>>, mapper)
    }
    param.visitExprMut { e -> mapPayloadsExpr(e, mapper) }
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsAssignTarget(
    target: Spanned<AssignTargetP<CstPayload>>,
    mapper: CompilerAstMap,
) {
    when (val node = target.node) {
        is AssignTargetP.Identifier<*, *> ->
            mapPayloadsAssignIdent(node.ident as Spanned<AssignIdentP<CstPayload, Any?>>, mapper)
        is AssignTargetP.Tuple<*> ->
            for (elem in node.elements as List<Spanned<AssignTargetP<CstPayload>>>) {
                mapPayloadsAssignTarget(elem, mapper)
            }
        is AssignTargetP.Index<*> -> {
            mapPayloadsExpr(node.expr as Spanned<ExprP<CstPayload>>, mapper)
            mapPayloadsExpr(node.index as Spanned<ExprP<CstPayload>>, mapper)
        }
        is AssignTargetP.Dot<*> ->
            mapPayloadsExpr(node.expr as Spanned<ExprP<CstPayload>>, mapper)
    }
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsAssignIdent(
    ident: Spanned<AssignIdentP<CstPayload, Any?>>,
    mapper: CompilerAstMap,
) {
    ident.node.payload = mapper.mapIdentAssign(Unit)
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsTypeExpr(
    typeExpr: Spanned<TypeExprP<CstPayload, Any?>>,
    mapper: CompilerAstMap,
) {
    typeExpr.node.payload = mapper.mapTypeExpr(Unit)
    mapPayloadsExpr(typeExpr.node.expr as Spanned<ExprP<CstPayload>>, mapper)
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsForClause(
    forClause: ForClauseP<CstPayload>,
    mapper: CompilerAstMap,
) {
    mapPayloadsAssignTarget(
        forClause.varTarget as Spanned<AssignTargetP<CstPayload>>,
        mapper,
    )
    mapPayloadsExpr(forClause.over as Spanned<ExprP<CstPayload>>, mapper)
}

@Suppress("UNCHECKED_CAST")
private fun mapPayloadsClause(
    clause: ClauseP<CstPayload>,
    mapper: CompilerAstMap,
) {
    when (clause) {
        is ClauseP.For<*> -> mapPayloadsForClause(
            clause.forClause as ForClauseP<CstPayload>,
            mapper,
        )
        is ClauseP.If<*> -> mapPayloadsExpr(clause.cond as Spanned<ExprP<CstPayload>>, mapper)
    }
}
