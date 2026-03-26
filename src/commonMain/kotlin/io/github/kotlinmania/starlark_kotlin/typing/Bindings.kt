// port-lint: source src/typing/bindings.rs
package io.github.kotlinmania.starlark_kotlin.typing

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
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstTypeExprPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ParameterP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypeExprP

/**
 * An expression bound to a variable during type-checking.
 *
 * Represents the different ways a variable can be assigned a value,
 * including direct expressions, tuple unpacking, iteration, modification,
 * index assignment, and list mutation operations.
 */
internal sealed class BindExpr {
    /** A direct expression. */
    data class Expr(val expr: CstExpr) : BindExpr()

    /** Get this position from the expression (tuple unpacking). */
    data class GetIndex(val index: Int, val inner: BindExpr) : BindExpr()

    /** Iterate over the expression. */
    data class Iter(val inner: BindExpr) : BindExpr()

    /** An augmented assignment (e.g. `x += e`). */
    data class AssignModify(
        val target: CstAssignTarget,
        val op: AssignOp,
        val expr: CstExpr,
    ) : BindExpr()

    /** Set this index in the variable. */
    data class SetIndex(
        val id: BindingId,
        val indexExpr: CstExpr,
        val inner: BindExpr,
    ) : BindExpr()

    /** Append a single element to a list variable. */
    data class ListAppend(val id: BindingId, val expr: CstExpr) : BindExpr()

    /** Extend a list variable with elements from another iterable. */
    data class ListExtend(val id: BindingId, val expr: CstExpr) : BindExpr()

    /** The source span associated with this binding expression. */
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

/**
 * Collected bindings from a statement for type-checking.
 *
 * Contains mappings from binding IDs to their assigned expressions,
 * non-inferred types from annotations, and expressions that need
 * type-checking but are not used in assignments.
 */
internal class Bindings(
    /** Mapping from binding IDs to their assigned expressions. */
    val expressions: SmallMap<BindingId, MutableList<BindExpr>> = SmallMap.new(),
    /**
     * Non-inferred types of bindings: from `load`,
     * or from variable or function parameter type annotations.
     */
    val types: MutableMap<BindingId, Ty> = mutableMapOf(),
    /**
     * Expressions which need to be typechecked, but which are not used
     * in assignments or in other expressions.
     * For example `expr` in:
     *
     * ```python
     * if expr: ...
     * ```
     */
    val check: MutableList<CstExpr> = mutableListOf(),
    /** Expressions that must conform to a given type (e.g. return statements). */
    val checkType: MutableList<Triple<Span, CstExpr?, Ty>> = mutableListOf(),
)

/**
 * Collects all variable bindings from a statement tree for type-checking.
 *
 * Walks the AST and records how each variable is assigned, what type annotations
 * exist, and which expressions need to be independently checked.
 */
internal class BindingsCollect(
    /** The accumulated bindings. */
    val bindings: Bindings,
    /** Approximations encountered during collection. */
    val approximations: MutableList<Approximation>,
) {
    companion object {
        /**
         * Collect all the assignments to variables.
         *
         * This function only fails on internal errors.
         */
        fun collectOne(
            x: CstStmt,
            typecheckMode: TypecheckMode,
            codemap: CodeMap,
            approximations: MutableList<Approximation>,
        ): BindingsCollect {
            val res = BindingsCollect(
                bindings = Bindings(),
                approximations = approximations,
            )
            res.visit(Visit.Stmt(x), Ty.any(), typecheckMode, codemap)
            return res
        }
    }

    private fun assign(
        lhs: CstAssignTarget,
        rhs: BindExpr,
        codemap: CodeMap,
    ) {
        when (val node = lhs.node) {
            is AssignTargetP.Identifier<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val ident = node.ident as CstAssignIdent
                val bindingId = resolvedBindingId(ident, codemap)
                val list = bindings.expressions.get(bindingId)
                if (list != null) {
                    list.add(rhs)
                } else {
                    bindings.expressions.insert(bindingId, mutableListOf(rhs))
                }
            }
            is AssignTargetP.Tuple<*> -> {
                @Suppress("UNCHECKED_CAST")
                val elements = node.elements as List<CstAssignTarget>
                for ((i, x) in elements.withIndex()) {
                    assign(x, BindExpr.GetIndex(i, rhs), codemap)
                }
            }
            is AssignTargetP.Index<*> -> {
                @Suppress("UNCHECKED_CAST")
                val lhsExpr = node.expr as CstExpr
                @Suppress("UNCHECKED_CAST")
                val indexExpr = node.index as CstExpr
                if (lhsExpr.node is ExprP.Identifier<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val identNode = lhsExpr.node as ExprP.Identifier<CstPayload, *>
                    val payload = identNode.ident.node.payload
                    if (payload is ResolvedIdent.Slot) {
                        val id = payload.bindingId
                        val list = bindings.expressions.get(id)
                        if (list != null) {
                            list.add(BindExpr.SetIndex(id, indexExpr, rhs))
                        } else {
                            bindings.expressions.insert(
                                id,
                                mutableListOf(BindExpr.SetIndex(id, indexExpr, rhs)),
                            )
                        }
                        return
                    }
                }
                approximations.add(
                    Approximation.new("Underapproximation", "a.b[x] = .. not handled")
                )
            }
            is AssignTargetP.Dot<*> -> {
                approximations.add(
                    Approximation.new("Underapproximation", "a.b = .. not handled")
                )
            }
        }
    }

    /**
     * Resolve the type from a type expression.
     * Type must be populated earlier.
     */
    private fun resolvedTy(
        expr: CstTypeExpr,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ): Ty {
        @Suppress("UNCHECKED_CAST")
        val payload = expr.node.payload as? CstTypeExprPayload
        val ty = when (typecheckMode) {
            TypecheckMode.Lint -> payload?.typecheckerTy
            TypecheckMode.Compiler -> payload?.compilerTy
        }
        return ty ?: throw InternalError.msg(
            "Type must be populated earlier",
        )
    }

    private fun resolveTyOpt(
        expr: CstTypeExpr?,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ): Ty {
        return if (expr != null) {
            resolvedTy(expr, typecheckMode, codemap)
        } else {
            Ty.any()
        }
    }

    private fun visitDef(
        def: DefP<CstPayload, *>,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ) {
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
            val paramIdent = p.ident
            val ty = resolveTyOpt(p.ty, typecheckMode, codemap)
            var nameTy: Pair<CstAssignIdent, Ty>? = null

            when (val kind = p.kind) {
                is DefParamKind.Regular -> {
                    val required = if (kind.defaultValue != null) {
                        ParamIsRequired.No
                    } else {
                        ParamIsRequired.Yes
                    }
                    when (kind.mode) {
                        DefRegularParamMode.PosOnly -> {
                            posOnly.add(Pair(required, ty))
                        }
                        DefRegularParamMode.PosOrName -> {
                            posOrNamed.add(Triple(paramIdent.node.ident, required, ty))
                        }
                        DefRegularParamMode.NameOnly -> {
                            namedOnly.add(Triple(paramIdent.node.ident, required, ty))
                        }
                    }
                    nameTy = Pair(paramIdent, ty)
                }
                is DefParamKind.Args -> {
                    // There is the type we require people calling us use (usually any)
                    // and then separately the type we are when we are running (always tuple)
                    args = ty
                    nameTy = Pair(
                        paramIdent,
                        Ty.basic(TyBasic.Tuple(TyTuple.Of(ArcTy.new(ty)))),
                    )
                }
                is DefParamKind.Kwargs -> {
                    val varTy = Ty.dict(Ty.string(), ty)
                    kwargs = ty
                    nameTy = Pair(paramIdent, varTy)
                }
            }
            if (nameTy != null) {
                bindings.types[resolvedBindingId(nameTy.first, codemap)] = nameTy.second
            }
        }
        val params2 = ParamSpec.newParts(posOnly, posOrNamed, args, namedOnly, kwargs)
        @Suppress("UNCHECKED_CAST")
        val retTy = resolveTyOpt(returnType, typecheckMode, codemap)
        @Suppress("UNCHECKED_CAST")
        val nameIdent = name as CstAssignIdent
        bindings.types[resolvedBindingId(nameIdent, codemap)] = Ty.function(params2, retTy)
        visitDefChildren(def) { x -> visit(x, retTy, typecheckMode, codemap) }
    }

    private fun visit(
        x: Visit,
        returnType: Ty,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ) {
        when (x) {
            is Visit.Stmt -> when (val node = x.stmt.node) {
                is StmtP.Assign<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val assignP = node.assign as AssignP<CstPayload>
                    if (assignP.ty != null) {
                        val ty2 = resolvedTy(assignP.ty, typecheckMode, codemap)
                        bindings.checkType.add(Triple(assignP.ty.span, assignP.rhs, ty2))
                        if (assignP.lhs.node is AssignTargetP.Identifier<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            val id = (assignP.lhs.node as AssignTargetP.Identifier<CstPayload, *>)
                                .ident as CstAssignIdent
                            // FIXME: This could be duplicated if you declare the type of a variable
                            // twice, we would only see the second one.
                            bindings.types[resolvedBindingId(id, codemap)] = ty2
                        }
                    }
                    assign(assignP.lhs, BindExpr.Expr(assignP.rhs), codemap)
                }
                is StmtP.AssignModify<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val lhs = node.lhs as CstAssignTarget
                    @Suppress("UNCHECKED_CAST")
                    val rhs = node.rhs as CstExpr
                    assign(
                        lhs,
                        BindExpr.AssignModify(lhs, node.op, rhs),
                        codemap,
                    )
                }
                is StmtP.For<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val forStmt = node.forStmt as ForP<CstPayload>
                    assign(
                        forStmt.varTarget,
                        BindExpr.Iter(BindExpr.Expr(forStmt.over)),
                        codemap,
                    )
                }
                is StmtP.Def<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val def = node.defStmt as DefP<CstPayload, *>
                    visitDef(def, typecheckMode, codemap)
                    // We do our own visit_children, with a different return type
                    return
                }
                is StmtP.Load<*, *> -> { /* nothing to do */ }
                is StmtP.Return<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val retExpr = node.expr as CstExpr?
                    bindings.checkType.add(Triple(x.stmt.span, retExpr, returnType))
                }
                is StmtP.Expression<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val expr = node.expr as CstExpr
                    // We want to find ident.append(), ident.extend(), ident.insert()
                    // to fake up a BindExpr.ListAppend/ListExtend
                    // so that mutating list operations aren't invisible to us
                    if (expr.node is ExprP.Call<*>) {
                        @Suppress("UNCHECKED_CAST")
                        val call = expr.node as ExprP.Call<CstPayload>
                        val funExpr = call.expr
                        if (funExpr.node is ExprP.Dot<*>) {
                            @Suppress("UNCHECKED_CAST")
                            val dot = funExpr.node as ExprP.Dot<CstPayload>
                            val idExpr = dot.expr
                            if (idExpr.node is ExprP.Identifier<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val ident = idExpr.node as ExprP.Identifier<CstPayload, *>
                                val attr = dot.field.node
                                val res = when {
                                    attr == "append" && call.args.args.size == 1 ->
                                        Pair(false, 0)
                                    attr == "insert" && call.args.args.size == 2 ->
                                        Pair(false, 1)
                                    attr == "extend" && call.args.args.size == 1 ->
                                        Pair(true, 0)
                                    else -> null
                                }
                                if (res != null) {
                                    val (extend, arg) = res
                                    val payload = ident.ident.node.payload
                                    if (payload is ResolvedIdent.Slot) {
                                        val bindId = payload.bindingId
                                        val argExpr = call.args.args[arg].expr()
                                        val bind = if (extend) {
                                            BindExpr.ListExtend(bindId, argExpr)
                                        } else {
                                            BindExpr.ListAppend(bindId, argExpr)
                                        }
                                        val list = bindings.expressions.get(bindId)
                                        if (list != null) {
                                            list.add(bind)
                                        } else {
                                            bindings.expressions.insert(
                                                bindId,
                                                mutableListOf(bind),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    bindings.check.add(expr)
                }
                is StmtP.If<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    bindings.check.add(node.cond as CstExpr)
                }
                is StmtP.IfElse<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    bindings.check.add(node.cond as CstExpr)
                }
                else -> { /* break, continue, pass, statements */ }
            }
            is Visit.Expr -> when (val node = x.expr.node) {
                is ExprP.ListComprehension<*>, is ExprP.DictComprehension<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val (for1, clauses) = when (node) {
                        is ExprP.ListComprehension<*> ->
                            Pair(
                                node.forClause as ForClauseP<CstPayload>,
                                node.clauses as List<ClauseP<CstPayload>>,
                            )
                        is ExprP.DictComprehension<*> ->
                            Pair(
                                node.forClause as ForClauseP<CstPayload>,
                                node.clauses as List<ClauseP<CstPayload>>,
                            )
                        else -> error("unreachable")
                    }
                    val forClauses = mutableListOf(for1)
                    for (clause in clauses) {
                        if (clause is ClauseP.For<*>) {
                            @Suppress("UNCHECKED_CAST")
                            forClauses.add(clause.forClause as ForClauseP<CstPayload>)
                        }
                    }
                    for (fc in forClauses) {
                        assign(
                            fc.varTarget,
                            BindExpr.Iter(BindExpr.Expr(fc.over)),
                            codemap,
                        )
                    }
                }
                else -> { /* nothing to do */ }
            }
        }
        visitChildren(x) { child -> visit(child, returnType, typecheckMode, codemap) }
    }
}

// ---------------------------------------------------------------------------
// Visit type -- corresponds to Rust's uniplate::Visit
// ---------------------------------------------------------------------------

/**
 * A visitable node in the AST: either a statement or an expression.
 *
 * Corresponds to Rust's `Visit<'a, CstPayload>`.
 */
internal sealed class Visit {
    data class Stmt(val stmt: CstStmt) : Visit()
    data class Expr(val expr: CstExpr) : Visit()

    /** The span of the visited node. */
    val span: Span get() = when (this) {
        is Stmt -> stmt.span
        is Expr -> expr.span
    }
}

// ---------------------------------------------------------------------------
// DefParam helpers -- corresponds to parts of Rust's syntax::def module
// ---------------------------------------------------------------------------

internal enum class DefRegularParamMode {
    PosOnly,
    PosOrName,
    NameOnly,
}

internal sealed class DefParamKind {
    data class Regular(
        val mode: DefRegularParamMode,
        val defaultValue: CstExpr?,
    ) : DefParamKind()

    data object Args : DefParamKind()
    data object Kwargs : DefParamKind()
}

internal class DefParam(
    val ident: CstAssignIdent,
    val kind: DefParamKind,
    val ty: CstTypeExpr?,
)

/**
 * Unpack raw AST parameters into structured [DefParam] entries.
 *
 * This mirrors Rust's `DefParams::unpack`.
 */
@Suppress("UNCHECKED_CAST")
private fun unpackDefParams(
    params: List<Spanned<ParameterP<CstPayload>>>,
    codemap: CodeMap,
): List<DefParam> {
    val result = mutableListOf<DefParam>()
    var seenStar = false
    var seenSlash = false

    for (p in params) {
        when (val param = p.node) {
            is ParameterP.Slash<*> -> {
                seenSlash = true
            }
            is ParameterP.NoArgs<*> -> {
                seenStar = true
            }
            is ParameterP.Normal<*> -> {
                val mode = when {
                    seenStar -> DefRegularParamMode.NameOnly
                    seenSlash -> DefRegularParamMode.PosOrName
                    else -> DefRegularParamMode.PosOrName
                }
                result.add(
                    DefParam(
                        ident = param.name as CstAssignIdent,
                        kind = DefParamKind.Regular(mode, param.defaultVal as CstExpr?),
                        ty = param.typ as CstTypeExpr?,
                    )
                )
            }
            is ParameterP.Args<*> -> {
                seenStar = true
                result.add(
                    DefParam(
                        ident = param.name as CstAssignIdent,
                        kind = DefParamKind.Args,
                        ty = param.typ as CstTypeExpr?,
                    )
                )
            }
            is ParameterP.KwArgs<*> -> {
                result.add(
                    DefParam(
                        ident = param.name as CstAssignIdent,
                        kind = DefParamKind.Kwargs,
                        ty = param.typ as CstTypeExpr?,
                    )
                )
            }
        }
    }
    return result
}

// ---------------------------------------------------------------------------
// resolvedBindingId helper
// ---------------------------------------------------------------------------

/**
 * Get the resolved [BindingId] from a CST assign-identifier.
 *
 * Throws [InternalError] if the binding has not been resolved.
 */
private fun resolvedBindingId(ident: CstAssignIdent, codemap: CodeMap): BindingId {
    return ident.node.payload as? BindingId
        ?: throw InternalError.msg("Binding not resolved for '${ident.node.ident}'")
}

// ---------------------------------------------------------------------------
// AST traversal helpers (Visit children)
// ---------------------------------------------------------------------------

/**
 * Visit immediate children of a [Visit] node.
 *
 * Corresponds to Rust's `Visit::visit_children_err`.
 */
@Suppress("UNCHECKED_CAST")
private fun visitChildren(x: Visit, f: (Visit) -> Unit) {
    when (x) {
        is Visit.Stmt -> visitStmtChildren(x.stmt, f)
        is Visit.Expr -> visitExprChildren(x.expr, f)
    }
}

@Suppress("UNCHECKED_CAST")
private fun visitStmtChildren(stmt: CstStmt, f: (Visit) -> Unit) {
    when (val node = stmt.node) {
        is StmtP.Statements<*> -> {
            for (s in node.stmts as List<CstStmt>) {
                f(Visit.Stmt(s))
            }
        }
        is StmtP.If<*> -> {
            f(Visit.Expr(node.cond as CstExpr))
            f(Visit.Stmt(node.suite as CstStmt))
        }
        is StmtP.IfElse<*> -> {
            f(Visit.Expr(node.cond as CstExpr))
            f(Visit.Stmt(node.suite1 as CstStmt))
            f(Visit.Stmt(node.suite2 as CstStmt))
        }
        is StmtP.Def<*, *> -> {
            val def = node.defStmt as DefP<CstPayload, *>
            visitDefChildren(def, f)
        }
        is StmtP.For<*> -> {
            val forStmt = node.forStmt as ForP<CstPayload>
            visitAssignTargetExprs(forStmt.varTarget) { f(Visit.Expr(it)) }
            f(Visit.Expr(forStmt.over))
            f(Visit.Stmt(forStmt.body))
        }
        is StmtP.Break<*> -> {}
        is StmtP.Continue<*> -> {}
        is StmtP.Pass<*> -> {}
        is StmtP.Return<*> -> {
            val ret = node.expr as CstExpr?
            if (ret != null) f(Visit.Expr(ret))
        }
        is StmtP.Expression<*> -> {
            f(Visit.Expr(node.expr as CstExpr))
        }
        is StmtP.Assign<*> -> {
            val assign = node.assign as AssignP<CstPayload>
            visitAssignTargetExprs(assign.lhs) { f(Visit.Expr(it)) }
            if (assign.ty != null) {
                f(Visit.Expr(assign.ty.node.expr))
            }
            f(Visit.Expr(assign.rhs))
        }
        is StmtP.AssignModify<*> -> {
            visitAssignTargetExprs(node.lhs as CstAssignTarget) { f(Visit.Expr(it)) }
            f(Visit.Expr(node.rhs as CstExpr))
        }
        is StmtP.Load<*, *> -> {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun visitDefChildren(def: DefP<CstPayload, *>, f: (Visit) -> Unit) {
    for (param in def.params) {
        visitParameterExprs(param) { f(Visit.Expr(it)) }
    }
    if (def.returnType != null) {
        f(Visit.Expr(def.returnType.node.expr))
    }
    f(Visit.Stmt(def.body))
}

@Suppress("UNCHECKED_CAST")
private fun visitParameterExprs(param: Spanned<ParameterP<CstPayload>>, f: (CstExpr) -> Unit) {
    when (val p = param.node) {
        is ParameterP.Normal<*> -> {
            if (p.typ != null) f((p.typ as CstTypeExpr).node.expr)
            if (p.defaultVal != null) f(p.defaultVal as CstExpr)
        }
        is ParameterP.Args<*> -> {
            if (p.typ != null) f((p.typ as CstTypeExpr).node.expr)
        }
        is ParameterP.KwArgs<*> -> {
            if (p.typ != null) f((p.typ as CstTypeExpr).node.expr)
        }
        is ParameterP.NoArgs<*> -> {}
        is ParameterP.Slash<*> -> {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun visitAssignTargetExprs(target: CstAssignTarget, f: (CstExpr) -> Unit) {
    when (val node = target.node) {
        is AssignTargetP.Tuple<*> -> {
            for (elem in node.elements as List<CstAssignTarget>) {
                visitAssignTargetExprs(elem, f)
            }
        }
        is AssignTargetP.Dot<*> -> {
            f(node.expr as CstExpr)
        }
        is AssignTargetP.Index<*> -> {
            f(node.expr as CstExpr)
            f(node.index as CstExpr)
        }
        is AssignTargetP.Identifier<*, *> -> {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun visitExprChildren(expr: CstExpr, f: (Visit) -> Unit) {
    when (val node = expr.node) {
        is ExprP.Tuple<*> -> {
            for (e in node.elements as List<CstExpr>) f(Visit.Expr(e))
        }
        is ExprP.Dot<*> -> {
            f(Visit.Expr(node.expr as CstExpr))
        }
        is ExprP.Call<*> -> {
            f(Visit.Expr(node.expr as CstExpr))
            val call = node as ExprP.Call<CstPayload>
            for (arg in call.args.args) f(Visit.Expr(arg.expr()))
        }
        is ExprP.Index<*> -> {
            f(Visit.Expr(node.expr as CstExpr))
            f(Visit.Expr(node.index as CstExpr))
        }
        is ExprP.Index2<*> -> {
            f(Visit.Expr(node.expr as CstExpr))
            f(Visit.Expr(node.index0 as CstExpr))
            f(Visit.Expr(node.index1 as CstExpr))
        }
        is ExprP.Slice<*> -> {
            f(Visit.Expr(node.expr as CstExpr))
            if (node.start != null) f(Visit.Expr(node.start as CstExpr))
            if (node.stop != null) f(Visit.Expr(node.stop as CstExpr))
            if (node.step != null) f(Visit.Expr(node.step as CstExpr))
        }
        is ExprP.Identifier<*, *> -> {}
        is ExprP.Lambda<*, *> -> {
            val lambda = node as ExprP.Lambda<CstPayload, *>
            for (param in lambda.lambda.params) {
                visitParameterExprs(param as Spanned<ParameterP<CstPayload>>) {
                    f(Visit.Expr(it))
                }
            }
            f(Visit.Expr(lambda.lambda.body))
        }
        is ExprP.Literal<*> -> {}
        is ExprP.Not<*> -> f(Visit.Expr(node.expr as CstExpr))
        is ExprP.Minus<*> -> f(Visit.Expr(node.expr as CstExpr))
        is ExprP.Plus<*> -> f(Visit.Expr(node.expr as CstExpr))
        is ExprP.BitNot<*> -> f(Visit.Expr(node.expr as CstExpr))
        is ExprP.Op<*> -> {
            f(Visit.Expr(node.lhs as CstExpr))
            f(Visit.Expr(node.rhs as CstExpr))
        }
        is ExprP.If<*> -> {
            f(Visit.Expr(node.cond as CstExpr))
            f(Visit.Expr(node.v1 as CstExpr))
            f(Visit.Expr(node.v2 as CstExpr))
        }
        is ExprP.ListExpr<*> -> {
            for (e in node.elements as List<CstExpr>) f(Visit.Expr(e))
        }
        is ExprP.Dict<*> -> {
            for ((k, v) in node.elements as List<Pair<CstExpr, CstExpr>>) {
                f(Visit.Expr(k))
                f(Visit.Expr(v))
            }
        }
        is ExprP.ListComprehension<*> -> {
            val lc = node as ExprP.ListComprehension<CstPayload>
            visitForClauseExprs(lc.forClause) { f(Visit.Expr(it)) }
            for (clause in lc.clauses) visitClauseExprs(clause) { f(Visit.Expr(it)) }
            f(Visit.Expr(lc.expr))
        }
        is ExprP.DictComprehension<*> -> {
            val dc = node as ExprP.DictComprehension<CstPayload>
            visitForClauseExprs(dc.forClause) { f(Visit.Expr(it)) }
            for (clause in dc.clauses) visitClauseExprs(clause) { f(Visit.Expr(it)) }
            f(Visit.Expr(dc.key))
            f(Visit.Expr(dc.value))
        }
        is ExprP.FString<*> -> {
            val fs = node as ExprP.FString<CstPayload>
            for (e in fs.fstring.node.expressions) f(Visit.Expr(e))
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun visitForClauseExprs(fc: ForClauseP<CstPayload>, f: (CstExpr) -> Unit) {
    visitAssignTargetExprs(fc.varTarget) { f(it) }
    f(fc.over)
}

@Suppress("UNCHECKED_CAST")
private fun visitClauseExprs(clause: ClauseP<CstPayload>, f: (CstExpr) -> Unit) {
    when (clause) {
        is ClauseP.For<*> -> visitForClauseExprs(clause.forClause as ForClauseP<CstPayload>, f)
        is ClauseP.If<*> -> f(clause.cond as CstExpr)
    }
}
