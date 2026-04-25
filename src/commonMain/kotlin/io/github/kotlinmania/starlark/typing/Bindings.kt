// port-lint: source src/typing/bindings.rs
package io.github.kotlinmania.starlark.typing

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
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstTypeExprPayload
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark.syntax.ast.AssignOp
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.AstNoPayload
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP

/** A visitable AST node: either a statement or an expression. */
internal sealed class Visit {
    data class Stmt(val stmt: Spanned<StmtP<CstPayload>>) : Visit()
    data class Expr(val expr: Spanned<ExprP<CstPayload>>) : Visit()

    val span: Span get() = when (this) {
        is Stmt -> stmt.span
        is Expr -> expr.span
    }
}

// Param-unpacking helpers (mirrors Rust's syntax::def module)

internal enum class DefRegularParamMode { PosOnly, PosOrName, NameOnly }

internal sealed class DefParamKind {
    data class Regular(val mode: DefRegularParamMode, val defaultValue: Spanned<ExprP<CstPayload>>?) : DefParamKind()
    data object Args : DefParamKind()
    data object Kwargs : DefParamKind()
}

internal class DefParam(
    val ident: Spanned<AssignIdentP<CstPayload, *>>,
    val kind: DefParamKind,
    val ty: Spanned<TypeExprP<CstPayload, *>>?,
)

private fun resolvedBindingId(ident: Spanned<AssignIdentP<CstPayload, *>>, codemap: CodeMap): BindingId =
    ident.node.payload as? BindingId
        ?: throw InternalError.msg("Binding not resolved for '${ident.node.ident}'", ident.span, codemap)

private fun unpackDefParams(params: List<Spanned<ParameterP<CstPayload>>>, codemap: CodeMap): List<DefParam> {
    val result = mutableListOf<DefParam>()
    var seenStar = false
    var seenSlash = false
    for (p in params) {
        when (val param = p.node) {
            is ParameterP.Slash -> seenSlash = true
            is ParameterP.NoArgs -> seenStar = true
            is ParameterP.Normal -> {
                val mode = if (seenStar) DefRegularParamMode.NameOnly else DefRegularParamMode.PosOrName
                result.add(DefParam(
                    param.name,
                    DefParamKind.Regular(mode, param.defaultVal),
                    param.typ,
                ))
            }
            is ParameterP.Args -> {
                seenStar = true
                result.add(DefParam(
                    param.name,
                    DefParamKind.Args,
                    param.typ,
                ))
            }
            is ParameterP.KwArgs ->
                result.add(DefParam(
                    param.name,
                    DefParamKind.Kwargs,
                    param.typ,
                ))
        }
    }
    return result
}

// BindExpr

sealed class BindExpr {
    data class Expr(val expr: Spanned<ExprP<CstPayload>>) : BindExpr()
    /** Get this position from the expression. */
    data class GetIndex(val index: Int, val inner: BindExpr) : BindExpr()
    data class Iter(val inner: BindExpr) : BindExpr()
    data class AssignModify(val target: Spanned<AssignTargetP<CstPayload>>, val op: AssignOp, val expr: Spanned<ExprP<CstPayload>>) : BindExpr()
    /** Set this index in the variable. */
    data class SetIndex(val id: BindingId, val indexExpr: Spanned<ExprP<CstPayload>>, val inner: BindExpr) : BindExpr()
    data class ListAppend(val id: BindingId, val expr: Spanned<ExprP<CstPayload>>) : BindExpr()
    data class ListExtend(val id: BindingId, val expr: Spanned<ExprP<CstPayload>>) : BindExpr()

    fun span(): Span = when (this) {
        is Expr -> expr.span
        is GetIndex -> inner.span()
        is Iter -> inner.span()
        is AssignModify -> target.span
        is SetIndex -> indexExpr.span
        is ListAppend -> expr.span
        is ListExtend -> expr.span
    }
}

// Bindings

internal class Bindings(
    val expressions: SmallMap<BindingId, MutableList<BindExpr>> = SmallMap.new(),
    /** Non-inferred types of bindings: from `load`, or from variable or function parameter type annotations. */
    val types: MutableMap<BindingId, Ty> = mutableMapOf(),
    /**
     * Expressions which need to be typechecked, but which are not used
     * in assignments or in other expressions.
     * For example `expr` in:
     * ```python
     * if expr: ...
     * ```
     */
    val check: MutableList<Spanned<ExprP<CstPayload>>> = mutableListOf(),
    val checkType: MutableList<Triple<Span, Spanned<ExprP<CstPayload>>?, Ty>> = mutableListOf(),
)

// BindingsCollect

internal class BindingsCollect(
    val bindings: Bindings,
    val approximations: MutableList<Approximation>,
) {
    companion object {
        /**
         * Collect all the assignments to variables.
         *
         * This function only fails on internal errors.
         */
        fun collectOne(
            x: Spanned<StmtP<CstPayload>>,
            typecheckMode: TypecheckMode,
            codemap: CodeMap,
            approximations: MutableList<Approximation>,
        ): BindingsCollect {
            val res = BindingsCollect(Bindings(), approximations)
            res.visit(Visit.Stmt(x), Ty.any(), typecheckMode, codemap)
            return res
        }
    }

    private fun assign(lhs: Spanned<AssignTargetP<CstPayload>>, rhs: BindExpr, codemap: CodeMap) {
        when (val node = lhs.node) {
            is AssignTargetP.Identifier<CstPayload, *> -> {
                val id = resolvedBindingId(node.ident, codemap)
                expressionsEntry(id).add(rhs)
            }
            is AssignTargetP.Tuple -> {
                for ((i, x) in node.elements.withIndex()) {
                    assign(x, BindExpr.GetIndex(i, rhs), codemap)
                }
            }
            is AssignTargetP.Index -> {
                val lhsExpr = node.expr
                val indexExpr = node.index
                val lhsNode = lhsExpr.node
                if (lhsNode is ExprP.Identifier<CstPayload, *>) {
                    val payload = lhsNode.ident.node.payload
                    if (payload is ResolvedIdent.Slot) {
                        val id = payload.bindingId
                        expressionsEntry(id).add(BindExpr.SetIndex(id, indexExpr, rhs))
                        return
                    }
                }
                approximations.add(Approximation.new("Underapproximation", "a.b[x] = .. not handled"))
            }
            is AssignTargetP.Dot -> {
                approximations.add(Approximation.new("Underapproximation", "a.b = .. not handled"))
            }
        }
    }

    /** Type must be populated earlier. */
    private fun resolvedTy(expr: Spanned<TypeExprP<CstPayload, *>>, typecheckMode: TypecheckMode, codemap: CodeMap): Ty {
        val payload = expr.node.payload as? CstTypeExprPayload
        val ty = when (typecheckMode) {
            TypecheckMode.Lint -> payload?.typecheckerTy
            TypecheckMode.Compiler -> payload?.compilerTy
        }
        return ty ?: throw InternalError.msg("Type must be populated earlier", expr.span, codemap)
    }

    private fun resolveTyOpt(expr: Spanned<TypeExprP<CstPayload, *>>?, typecheckMode: TypecheckMode, codemap: CodeMap): Ty =
        if (expr != null) resolvedTy(expr, typecheckMode, codemap) else Ty.any()

    private fun visitDef(def: DefP<CstPayload, *>, typecheckMode: TypecheckMode, codemap: CodeMap) {
        val name = def.name
        val params = def.params
        val returnType = def.returnType

        val defParams = unpackDefParams(params, codemap)

        val posOnly = mutableListOf<Pair<ParamIsRequired, Ty>>()
        val posOrNamed = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var args: Ty? = null
        val namedOnly = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var kwargs: Ty? = null

        for (p in defParams) {
            val paramName = p.ident
            val ty = resolveTyOpt(p.ty, typecheckMode, codemap)
            val nameTy: Pair<Spanned<AssignIdentP<CstPayload, *>>, Ty> = when (val kind = p.kind) {
                is DefParamKind.Regular -> {
                    val required = if (kind.defaultValue != null) ParamIsRequired.No else ParamIsRequired.Yes
                    when (kind.mode) {
                        DefRegularParamMode.PosOnly -> posOnly.add(Pair(required, ty))
                        DefRegularParamMode.PosOrName -> posOrNamed.add(Triple(paramName.node.ident, required, ty))
                        DefRegularParamMode.NameOnly -> namedOnly.add(Triple(paramName.node.ident, required, ty))
                    }
                    Pair(paramName, ty)
                }
                is DefParamKind.Args -> {
                    // There is the type we require people calling us use (usually any)
                    // and then separately the type we are when we are running (always tuple)
                    args = ty
                    Pair(paramName, Ty.basic(TyBasic.Tuple(TyTuple.Of(ArcTy.new(ty)))))
                }
                is DefParamKind.Kwargs -> {
                    val varTy = Ty.dict(Ty.string(), ty)
                    kwargs = ty
                    Pair(paramName, varTy)
                }
            }
            bindings.types[resolvedBindingId(nameTy.first, codemap)] = nameTy.second
        }
        val params2 = ParamSpec.newParts(posOnly, posOrNamed, args, namedOnly, kwargs)
        val retTy = resolveTyOpt(returnType, typecheckMode, codemap)
        bindings.types[resolvedBindingId(name, codemap)] = Ty.function(params2, retTy)
        visitDefChildren(def) { x -> visit(x, retTy, typecheckMode, codemap) }
    }

    private fun visit(x: Visit, returnType: Ty, typecheckMode: TypecheckMode, codemap: CodeMap) {
        when (x) {
            is Visit.Stmt -> when (val node = x.stmt.node) {
                is StmtP.Assign -> {
                    val assignP = node.assign
                    val assignTy = assignP.ty
                    if (assignTy != null) {
                        val ty2 = resolvedTy(assignTy, typecheckMode, codemap)
                        bindings.checkType.add(Triple(assignTy.span, assignP.rhs, ty2))
                        val lhsNode = assignP.lhs.node
                        if (lhsNode is AssignTargetP.Identifier<CstPayload, *>) {
                            // FIXME: This could be duplicated if you declare the type of a variable twice,
                            // we would only see the second one.
                            bindings.types[resolvedBindingId(lhsNode.ident, codemap)] = ty2
                        }
                    }
                    assign(assignP.lhs, BindExpr.Expr(assignP.rhs), codemap)
                }
                is StmtP.AssignModify -> {
                    val lhs = node.lhs
                    val rhs = node.rhs
                    assign(lhs, BindExpr.AssignModify(lhs, node.op, rhs), codemap)
                }
                is StmtP.For -> {
                    val forStmt = node.forStmt
                    assign(forStmt.varTarget, BindExpr.Iter(BindExpr.Expr(forStmt.over)), codemap)
                }
                is StmtP.Def<CstPayload, *> -> {
                    visitDef(node.def, typecheckMode, codemap)
                    // We do our own visit_children, with a different return type
                    return
                }
                is StmtP.Load<CstPayload, *> -> {}
                is StmtP.Return -> {
                    bindings.checkType.add(Triple(x.stmt.span, node.expr, returnType))
                }
                is StmtP.Expression -> {
                    val expr = node.expr
                    // We want to find ident.append(), ident.extend(), ident.insert()
                    // to fake up a BindExpr::ListAppend/ListExtend
                    // so that mutating list operations aren't invisible to us
                    val exprNode = expr.node
                    if (exprNode is ExprP.Call) {
                        val funExpr = exprNode.expr
                        val funNode = funExpr.node
                        if (funNode is ExprP.Dot) {
                            val idExpr = funNode.expr
                            val idNode = idExpr.node
                            if (idNode is ExprP.Identifier<CstPayload, *>) {
                                val attr = funNode.field.node
                                val res = when {
                                    attr == "append" && exprNode.args.args.size == 1 -> Pair(false, 0)
                                    attr == "insert" && exprNode.args.args.size == 2 -> Pair(false, 1)
                                    attr == "extend" && exprNode.args.args.size == 1 -> Pair(true, 0)
                                    else -> null
                                }
                                if (res != null) {
                                    val (extend, arg) = res
                                    val payload = idNode.ident.node.payload
                                    if (payload is ResolvedIdent.Slot) {
                                        val bindId = payload.bindingId
                                        val argExpr = exprNode.args.args[arg].node.expr()
                                        val bind = if (extend) BindExpr.ListExtend(bindId, argExpr)
                                            else BindExpr.ListAppend(bindId, argExpr)
                                        expressionsEntry(bindId).add(bind)
                                    }
                                }
                            }
                        }
                    }
                    bindings.check.add(expr)
                }
                is StmtP.If -> bindings.check.add(node.cond)
                is StmtP.IfElse -> bindings.check.add(node.cond)
                else -> {}
            }
            is Visit.Expr -> when (val node = x.expr.node) {
                is ExprP.ListComprehension -> {
                    val forClauses = mutableListOf(node.forClause)
                    for (clause in node.clauses) {
                        if (clause is ClauseP.For)
                            forClauses.add(clause.forClause)
                    }
                    for (fc in forClauses) {
                        assign(fc.varTarget, BindExpr.Iter(BindExpr.Expr(fc.over)), codemap)
                    }
                }
                is ExprP.DictComprehension -> {
                    val forClauses = mutableListOf(node.forClause)
                    for (clause in node.clauses) {
                        if (clause is ClauseP.For)
                            forClauses.add(clause.forClause)
                    }
                    for (fc in forClauses) {
                        assign(fc.varTarget, BindExpr.Iter(BindExpr.Expr(fc.over)), codemap)
                    }
                }
                else -> {}
            }
        }
        visitChildren(x) { child -> visit(child, returnType, typecheckMode, codemap) }
    }

    private fun expressionsEntry(id: BindingId): MutableList<BindExpr> {
        val existing = bindings.expressions.get(id)
        if (existing != null) return existing
        val list = mutableListOf<BindExpr>()
        bindings.expressions.insert(id, list)
        return list
    }
}

// AST traversal (mirrors Rust's uniplate::Visit::visit_children_err)

private fun visitChildren(x: Visit, f: (Visit) -> Unit) {
    when (x) {
        is Visit.Stmt -> visitStmtChildren(x.stmt, f)
        is Visit.Expr -> visitExprChildren(x.expr, f)
    }
}

private fun visitStmtChildren(stmt: Spanned<StmtP<CstPayload>>, f: (Visit) -> Unit) {
    when (val node = stmt.node) {
        is StmtP.Statements -> node.stmts.forEach { f(Visit.Stmt(it)) }
        is StmtP.If -> { f(Visit.Expr(node.cond)); f(Visit.Stmt(node.suite)) }
        is StmtP.IfElse -> {
            f(Visit.Expr(node.cond))
            f(Visit.Stmt(node.suite1)); f(Visit.Stmt(node.suite2))
        }
        is StmtP.Def<CstPayload, *> -> visitDefChildren(node.def, f)
        is StmtP.For -> {
            val fp = node.forStmt
            visitAssignTargetExprs(fp.varTarget) { f(Visit.Expr(it)) }
            f(Visit.Expr(fp.over)); f(Visit.Stmt(fp.body))
        }
        is StmtP.Return -> node.expr?.let { f(Visit.Expr(it)) }
        is StmtP.Expression -> f(Visit.Expr(node.expr))
        is StmtP.Assign -> {
            val a = node.assign
            visitAssignTargetExprs(a.lhs) { f(Visit.Expr(it)) }
            a.ty?.let { f(Visit.Expr(it.node.expr)) }
            f(Visit.Expr(a.rhs))
        }
        is StmtP.AssignModify -> {
            visitAssignTargetExprs(node.lhs) { f(Visit.Expr(it)) }
            f(Visit.Expr(node.rhs))
        }
        is StmtP.Break, is StmtP.Continue, is StmtP.Pass, is StmtP.Load<CstPayload, *> -> {}
    }
}

private fun visitDefChildren(def: DefP<CstPayload, *>, f: (Visit) -> Unit) {
    for (p in def.params) visitParamExprs(p) { f(Visit.Expr(it)) }
    def.returnType?.let { f(Visit.Expr(it.node.expr)) }
    f(Visit.Stmt(def.body))
}

private fun visitParamExprs(param: Spanned<ParameterP<CstPayload>>, f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (val p = param.node) {
        is ParameterP.Normal -> {
            p.typ?.let { f(it.node.expr) }
            p.defaultVal?.let { f(it) }
        }
        is ParameterP.Args -> p.typ?.let { f(it.node.expr) }
        is ParameterP.KwArgs -> p.typ?.let { f(it.node.expr) }
        is ParameterP.NoArgs, is ParameterP.Slash -> {}
    }
}

private fun visitAssignTargetExprs(target: Spanned<AssignTargetP<CstPayload>>, f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (val node = target.node) {
        is AssignTargetP.Tuple -> node.elements.forEach { visitAssignTargetExprs(it, f) }
        is AssignTargetP.Dot -> f(node.expr)
        is AssignTargetP.Index -> { f(node.expr); f(node.index) }
        is AssignTargetP.Identifier<CstPayload, *> -> {}
    }
}

private fun visitExprChildren(expr: Spanned<ExprP<CstPayload>>, f: (Visit) -> Unit) {
    when (val node = expr.node) {
        is ExprP.Tuple -> node.elements.forEach { f(Visit.Expr(it)) }
        is ExprP.Dot -> f(Visit.Expr(node.expr))
        is ExprP.Call -> {
            f(Visit.Expr(node.expr))
            node.args.args.forEach { f(Visit.Expr(it.node.expr())) }
        }
        is ExprP.Index -> { f(Visit.Expr(node.expr)); f(Visit.Expr(node.index)) }
        is ExprP.Index2 -> {
            f(Visit.Expr(node.expr))
            f(Visit.Expr(node.index0))
            f(Visit.Expr(node.index1))
        }
        is ExprP.Slice -> {
            f(Visit.Expr(node.expr))
            node.start?.let { f(Visit.Expr(it)) }
            node.stop?.let { f(Visit.Expr(it)) }
            node.step?.let { f(Visit.Expr(it)) }
        }
        is ExprP.Identifier<CstPayload, *> -> {}
        is ExprP.Lambda<CstPayload, *> -> {
            node.lambda.params.forEach { visitParamExprs(it) { e -> f(Visit.Expr(e)) } }
            f(Visit.Expr(node.lambda.body))
        }
        is ExprP.Literal -> {}
        is ExprP.Not -> f(Visit.Expr(node.expr))
        is ExprP.Minus -> f(Visit.Expr(node.expr))
        is ExprP.Plus -> f(Visit.Expr(node.expr))
        is ExprP.BitNot -> f(Visit.Expr(node.expr))
        is ExprP.Op -> { f(Visit.Expr(node.lhs)); f(Visit.Expr(node.rhs)) }
        is ExprP.If -> {
            f(Visit.Expr(node.cond))
            f(Visit.Expr(node.v1))
            f(Visit.Expr(node.v2))
        }
        is ExprP.ListExpr -> node.elements.forEach { f(Visit.Expr(it)) }
        is ExprP.Dict -> node.elements.forEach { (k, v) -> f(Visit.Expr(k)); f(Visit.Expr(v)) }
        is ExprP.ListComprehension -> {
            visitForClauseExprs(node.forClause) { f(Visit.Expr(it)) }
            node.clauses.forEach { visitClauseExprs(it) { e -> f(Visit.Expr(e)) } }
            f(Visit.Expr(node.expr))
        }
        is ExprP.DictComprehension -> {
            visitForClauseExprs(node.forClause) { f(Visit.Expr(it)) }
            node.clauses.forEach { visitClauseExprs(it) { e -> f(Visit.Expr(e)) } }
            f(Visit.Expr(node.key)); f(Visit.Expr(node.value))
        }
        is ExprP.FString -> node.fstring.node.expressions.forEach { f(Visit.Expr(it)) }
    }
}

private fun visitForClauseExprs(fc: ForClauseP<CstPayload>, f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    visitAssignTargetExprs(fc.varTarget, f); f(fc.over)
}

private fun visitClauseExprs(clause: ClauseP<CstPayload>, f: (Spanned<ExprP<CstPayload>>) -> Unit) {
    when (clause) {
        is ClauseP.For -> visitForClauseExprs(clause.forClause, f)
        is ClauseP.If -> f(clause.cond)
    }
}
