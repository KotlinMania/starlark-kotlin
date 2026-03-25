// port-lint: source src/typing/bindings.rs
package io.github.kotlinmania.starlark_kotlin.typing.bindings

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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.arc_ty.ArcTy
import io.github.kotlinmania.starlark_kotlin.typing.callable_param.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.typing.error.InternalError
import io.github.kotlinmania.starlark_kotlin.typing.mode.TypecheckMode
import io.github.kotlinmania.starlark_kotlin.values.typing.TyTuple
import io.github.kotlinmania.starlark_kotlin.values.typing.Approximation
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.Ty
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefRegularParamMode
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefParams
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefParamKind
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstExpr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.BindingId
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignOp
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.CstPayload
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.StmtP
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstStmt
import io.github.kotlinmania.starlark_kotlin.analysis.DefP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Slot
import io.github.kotlinmania.starlark_kotlin.values.types.function
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.size
import io.github.kotlinmania.starlark_kotlin.values.index
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.analysis.def
import io.github.kotlinmania.starlark_kotlin.analysis.Return
import io.github.kotlinmania.starlark_kotlin.analysis.IfElse
import io.github.kotlinmania.starlark_kotlin.analysis.If
import io.github.kotlinmania.starlark_kotlin.analysis.For
import io.github.kotlinmania.starlark_kotlin.analysis.Expression
import io.github.kotlinmania.starlark_kotlin.analysis.Def
import io.github.kotlinmania.starlark_kotlin.analysis.AssignModify
import io.github.kotlinmania.starlark_kotlin.analysis.Assign
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.values.typing.basic
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.string.string
import io.github.kotlinmania.starlark_kotlin.values.types.dict.dict
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.mode
import io.github.kotlinmania.starlark_kotlin.values.attr
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.resolvedBindingId
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Slot
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.IfElse
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.If
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.For
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ExprP
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefRegularParamMode
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefParams
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefParamKind
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstExpr
import io.github.kotlinmania.starlark_kotlin.typing.ctx.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.typing.ctx.BindingId
import io.github.kotlinmania.starlark_kotlin.tests.derive.obj
import io.github.kotlinmania.starlark_kotlin.stdlib.add
import io.github.kotlinmania.starlark_kotlin.eval.bc.over
import io.github.kotlinmania.starlark_kotlin.docs.params
import io.github.kotlinmania.starlark_kotlin.docs.defaultValue
import io.github.kotlinmania.starlark_kotlin.docs.args
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.bindingId
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.analysis.ident
import io.github.kotlinmania.starlark_kotlin.analysis.func
import io.github.kotlinmania.starlark_kotlin.__derive_refs.returnType
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.enum_type.elements
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefP
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.clauses
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Stmt
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.values.layout.size

// pub(crate) enum BindExpr<'a>
internal sealed class BindExpr {
    // Expr(&'a CstExpr)
    class Expr(val expr: CstExpr) : BindExpr()
    // GetIndex(usize, Box<BindExpr<'a>>)
    /// Get this position from the expression
    class GetIndex(val index: Int, val inner: BindExpr) : BindExpr()
    // Iter(Box<BindExpr<'a>>)
    class Iter(val inner: BindExpr) : BindExpr()
    // AssignModify(&'a CstAssignTarget, AssignOp, &'a CstExpr)
    class AssignModify(val target: CstAssignTarget, val op: AssignOp, val expr: CstExpr) : BindExpr()
    // SetIndex(BindingId, &'a CstExpr, Box<BindExpr<'a>>)
    /// Set this index in the variable
    class SetIndex(val id: BindingId, val indexExpr: CstExpr, val inner: BindExpr) : BindExpr()
    // ListAppend(BindingId, &'a CstExpr)
    class ListAppend(val id: BindingId, val expr: CstExpr) : BindExpr()
    // ListExtend(BindingId, &'a CstExpr)
    class ListExtend(val id: BindingId, val expr: CstExpr) : BindExpr()

    // impl BindExpr
    // pub(crate) fn span(&self) -> Span
    fun span(): Span {
        return when (this) {
            is Expr -> expr.span
            is GetIndex -> inner.span()
            is Iter -> inner.span()
            is AssignModify -> target.span
            is SetIndex -> indexExpr.span
            is ListAppend -> expr.span
            is ListExtend -> expr.span
        }
    }
}

// #[derive(Default)]
// pub(crate) struct Bindings<'a>
internal class Bindings(
    // pub(crate) expressions: SmallMap<BindingId, Vec<BindExpr<'a>>>
    val expressions: SmallMap<BindingId, MutableList<BindExpr>> = SmallMap(),
    /// Non-inferred types of bindings: from `load`,
    /// or from variable or function parameter type annotations.
    // pub(crate) types: HashMap<BindingId, Ty>
    val types: MutableMap<BindingId, Ty> = mutableMapOf(),
    /// Expressions which need to be typechecked, but which are not used
    /// in assignments or in other expressions.
    // pub(crate) check: Vec<&'a CstExpr>
    val check: MutableList<CstExpr> = mutableListOf(),
    // pub(crate) check_type: Vec<(Span, Option<&'a CstExpr>, Ty)>
    val checkType: MutableList<Triple<Span, CstExpr?, Ty>> = mutableListOf(),
)

// pub(crate) struct BindingsCollect<'a, 'b>
internal class BindingsCollect(
    // pub(crate) bindings: Bindings<'a>
    val bindings: Bindings,
    // pub(crate) approximations: &'b mut Vec<Approximation>
    val approximations: MutableList<Approximation>,
) {
    companion object {
        /// Collect all the assignments to variables.
        ///
        /// This function only fails on internal errors.
        // pub(crate) fn collect_one(...) -> Result<Self, InternalError>
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

    // fn assign(&mut self, lhs, rhs, codemap) -> Result<(), InternalError>
    private fun assign(
        lhs: CstAssignTarget,
        rhs: BindExpr,
        codemap: CodeMap,
    ) {
        when (val node = lhs.node) {
            is AssignTargetP.Identifier -> {
                bindings.expressions
                    .getOrPut(node.ident.resolvedBindingId(codemap)) { mutableListOf() }
                    .add(rhs)
            }
            is AssignTargetP.Tuple -> {
                for ((i, x) in node.elements.withIndex()) {
                    assign(x, BindExpr.GetIndex(i, rhs), codemap)
                }
            }
            is AssignTargetP.Index -> {
                val arrayIndex = node
                val lhsExpr = arrayIndex.obj
                if (lhsExpr.node is ExprP.Identifier) {
                    val ident = (lhsExpr.node as ExprP.Identifier).ident
                    val payload = ident.node.payload
                    if (payload is ResolvedIdent.Slot) {
                        val id = payload.bindingId
                        bindings.expressions
                            .getOrPut(id) { mutableListOf() }
                            .add(BindExpr.SetIndex(id, arrayIndex.index, rhs))
                        return
                    }
                }
                approximations.add(
                    Approximation.new("Underapproximation", "a.b[x] = .. not handled")
                )
            }
            is AssignTargetP.Dot -> {
                approximations.add(
                    Approximation.new("Underapproximation", "a.b = .. not handled")
                )
            }
        }
    }

    /// Type must be populated earlier.
    // fn resolved_ty(expr, typecheck_mode, codemap) -> Result<Ty, InternalError>
    private fun resolvedTy(
        expr: CstTypeExpr,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ): Ty {
        val ty = when (typecheckMode) {
            TypecheckMode.Lint -> expr.Payload.typecheckerTy
            TypecheckMode.Compiler -> expr.Payload.compilerTy
        }
        return ty ?: throw InternalError.msg(
            "Type must be populated earlier",
            expr.span,
            codemap,
        )
    }

    // fn resolve_ty_opt(expr, typecheck_mode, codemap) -> Result<Ty, InternalError>
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

    // fn visit_def(&mut self, def, typecheck_mode, codemap) -> Result<(), InternalError>
    private fun visitDef(
        def: DefP<CstPayload>,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ) {
        val name = def.name
        val params = def.params
        val returnType = def.returnType

        val defParams = DefParams.unpack(params, codemap)

        val posOnly = mutableListOf<Pair<ParamIsRequired, Ty>>()
        val posOrNamed = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var args: Ty? = null
        val namedOnly = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var kwargs: Ty? = null

        for (p in defParams.params) {
            val paramName = p.node.ident
            val ty = resolveTyOpt(p.node.ty, typecheckMode, codemap)
            var nameTy: Pair<Any, Ty>? = null

            when (val kind = p.node.kind) {
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
                            posOrNamed.add(Triple(paramName.ident, required, ty))
                        }
                        DefRegularParamMode.NameOnly -> {
                            namedOnly.add(Triple(paramName.ident, required, ty))
                        }
                    }
                    nameTy = Pair(paramName, ty)
                }
                is DefParamKind.Args -> {
                    args = ty
                    nameTy = Pair(paramName, Ty.basic(TyBasic.Tuple(TyTuple.Of(ArcTy.new(ty)))))
                }
                is DefParamKind.Kwargs -> {
                    val varTy = Ty.dict(Ty.string(), ty)
                    kwargs = ty
                    nameTy = Pair(paramName, varTy)
                }
            }
            if (nameTy != null) {
                @Suppress("UNCHECKED_CAST")
                val ident = nameTy.first as CstAssignIdentExt
                bindings.types[ident.resolvedBindingId(codemap)] = nameTy.second
            }
        }
        val params2 = ParamSpec.newParts(posOnly, posOrNamed, args, namedOnly, kwargs)
        val retTy = resolveTyOpt(returnType, typecheckMode, codemap)
        bindings.types[name.resolvedBindingId(codemap)] = Ty.function(params2, retTy)
        def.visitChildrenErr { x -> visit(x, retTy, typecheckMode, codemap) }
    }

    // fn visit(&mut self, x, return_type, typecheck_mode, codemap) -> Result<(), InternalError>
    private fun visit(
        x: Visit<CstPayload>,
        returnType: Ty,
        typecheckMode: TypecheckMode,
        codemap: CodeMap,
    ) {
        when (x) {
            is Visit.Stmt -> when (val node = x.StmtP.node) {
                is StmtP.Assign -> {
                    val assignP = node.assign
                    if (assignP.ty != null) {
                        val ty2 = resolvedTy(assignP.ty, typecheckMode, codemap)
                        bindings.checkType.add(Triple(assignP.ty.span, assignP.rhs, ty2))
                        if (assignP.lhs.node is AssignTargetP.Identifier) {
                            val id = (assignP.lhs.node as AssignTargetP.Identifier).ident
                            bindings.types[id.resolvedBindingId(codemap)] = ty2
                        }
                    }
                    assign(assignP.lhs, BindExpr.Expr(assignP.rhs), codemap)
                }
                is StmtP.AssignModify -> {
                    assign(
                        node.lhs,
                        BindExpr.AssignModify(node.lhs, node.op, node.rhs),
                        codemap,
                    )
                }
                is StmtP.For -> {
                    assign(
                        node.forP.`var`,
                        BindExpr.Iter(BindExpr.Expr(node.forP.over)),
                        codemap,
                    )
                }
                is StmtP.Def -> {
                    visitDef(node.def, typecheckMode, codemap)
                    // We do our own visit_children, with a different return type
                    return
                }
                is StmtP.Load -> {}
                is StmtP.Return -> {
                    bindings.checkType.add(Triple(x.StmtP.span, node.Expr, returnType))
                }
                is StmtP.Expression -> {
                    val expr = node.Expr
                    // We want to find ident.append(), ident.extend()
                    // to fake up a BindExpr::ListAppend/ListExtend
                    if (expr.node is ExprP.Call) {
                        val call = expr.node as ExprP.Call
                        val fun_ = call.func
                        if (fun_.node is ExprP.Dot) {
                            val dot = fun_.node as ExprP.Dot
                            val id = dot.obj
                            if (id.node is ExprP.Identifier) {
                                val ident = (id.node as ExprP.Identifier).ident
                                val attr = dot.attr
                                val res = when {
                                    attr == "append" && call.args.size == 1 -> Pair(false, 0)
                                    attr == "insert" && call.args.size == 2 -> Pair(false, 1)
                                    attr == "extend" && call.args.size == 1 -> Pair(true, 0)
                                    else -> null
                                }
                                if (res != null) {
                                    val (extend, arg) = res
                                    val payload = ident.node.payload
                                    if (payload is ResolvedIdent.Slot) {
                                        val bindId = payload.bindingId
                                        val bind = if (extend) {
                                            BindExpr.ListExtend(bindId, call.args[arg].Expr())
                                        } else {
                                            BindExpr.ListAppend(bindId, call.args[arg].Expr())
                                        }
                                        bindings.expressions
                                            .getOrPut(bindId) { mutableListOf() }
                                            .add(bind)
                                    }
                                }
                            }
                        }
                    }
                    bindings.check.add(expr)
                }
                is StmtP.If -> bindings.check.add(node.condition)
                is StmtP.IfElse -> bindings.check.add(node.condition)
                else -> {}
            }
            is Visit.Expr -> when (val node = x.ExprP.node) {
                is ExprP.ListComprehension, is ExprP.DictComprehension -> {
                    val (for1, clauses) = when (node) {
                        is ExprP.ListComprehension -> Pair(node.for1, node.clauses)
                        is ExprP.DictComprehension -> Pair(node.for1, node.clauses)
                        else -> error("unreachable")
                    }
                    val forClauses = listOf(for1) + clauses.filterIsInstance<ClauseP.For<CstPayload>>()
                        .map { it.forClause }
                    for (fc in forClauses) {
                        assign(
                            fc.`var`,
                            BindExpr.Iter(BindExpr.Expr(fc.over)),
                            codemap,
                        )
                    }
                }
                else -> {}
            }
        }
        x.visitChildrenErr { child -> visit(child, returnType, typecheckMode, codemap) }
    }
}
