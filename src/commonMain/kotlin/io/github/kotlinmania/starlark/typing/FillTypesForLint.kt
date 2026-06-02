// port-lint: source src/typing/fill_types_for_lint.rs
package io.github.kotlinmania.starlark.typing

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopeData
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.Slot
import io.github.kotlinmania.starlark.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark.eval.compiler.scope.CstAssignIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdentPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark.eval.compiler.scope.CstTypeExpr
import io.github.kotlinmania.starlark.eval.compiler.scope.cstPayload
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.syntax.ast.AstString
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.typeexpr.TypeExprUnpackP
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark.values.types.list.allocList
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

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

/** Value computed during partial evaluation of globals. */
private data class GlobalValue(
    /** `null` means we don't know (or know it may have different value depending on condition). */
    val value: Value?,
    val ty: Ty,
) {
    companion object {
        fun union2(a: GlobalValue, b: GlobalValue): GlobalValue =
            GlobalValue(
                value = null,
                ty = Ty.union2(a.ty, b.ty),
            )

        fun value(v: Value): GlobalValue =
            GlobalValue(
                value = v,
                ty = Ty.any(), // Ty::of_value not yet ported
            )

        fun any(): GlobalValue =
            GlobalValue(
                value = null,
                ty = Ty.any(),
            )

        fun ty(ty: Ty): GlobalValue = GlobalValue(value = null, ty = ty)
    }
}

private class GlobalTypesBuilder(
    val approximations: MutableList<Approximation>,
    val heap: Heap,
    val values: MutableMap<ModuleSlotId, GlobalValue>,
    val errors: MutableList<TypingError>,
    val moduleScopeData: ModuleScopeData,
    val ctx: TypingOracleCtx,
) {
    fun internalError(span: Span, message: String): InternalError = InternalError.msg(message, span, ctx.codemap)

    fun err(span: Span, e: StarlarkError): GlobalValue {
        errors.add(TypingError.new(e, span, ctx.codemap))
        return GlobalValue.any()
    }

    fun call(_f: CstExpr, _args: CallArgsP<CstPayload>): GlobalValue = GlobalValue.any()

    fun exprIdent(ident: CstIdent): GlobalValue {
        val resolved = ident.node.payload ?: throw internalError(ident.span, "unresolved ident")
        return when (resolved) {
            is ResolvedIdent.Slot -> {
                when (val slot = resolved.slot) {
                    is Slot.Module -> {
                        values[slot.id] ?: GlobalValue.any()
                    }
                    is Slot.Local -> {
                        throw internalError(ident.span, "local slot in global scope")
                    }
                }
            }
            is ResolvedIdent.Global -> {
                GlobalValue.value(resolved.value.toValue())
            }
        }
    }

    fun exprLiteral(literal: AstLiteral): GlobalValue =
        when (literal) {
            is AstLiteral.String -> GlobalValue.value(heap.allocStr(literal.value.node))
            else -> GlobalValue.any()
        }

    fun tuple(xs: List<CstExpr>): GlobalValue {
        val results = xs.map { exprSpanned(it) }
        val allValues = results.mapNotNull { it.node.value }
        return if (allValues.size == results.size) {
            GlobalValue.value(heap.allocTuple(allValues))
        } else {
            GlobalValue.any()
        }
    }

    fun dot(span: Span, obj: CstExpr, field: AstString): GlobalValue {
        val objValue = expr(obj)
        val v = objValue.value ?: return GlobalValue.any()
        return v.getAttrError(field.node, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "getattr error", it)) },
        )
    }

    fun index(span: Span, array: CstExpr, indexExpr: CstExpr): GlobalValue {
        val arrayVal = expr(array)
        val indexVal = exprSpanned(indexExpr)
        val a = arrayVal.value ?: return GlobalValue.any()
        val i = indexVal.node.value ?: return GlobalValue.any()
        return a.at(i, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "at error", it)) },
        )
    }

    fun index2(span: Span, array: CstExpr, index0: CstExpr, index1: CstExpr): GlobalValue {
        val arrayVal = expr(array)
        val idx0Val = expr(index0)
        val idx1Val = expr(index1)
        val a = arrayVal.value ?: return GlobalValue.any()
        val i0 = idx0Val.value ?: return GlobalValue.any()
        val i1 = idx1Val.value ?: return GlobalValue.any()
        return a.getRef().at2(i0, i1, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "at2 error", it)) },
        )
    }

    fun binOp(span: Span, lhs: CstExpr, op: BinOp, rhs: CstExpr): GlobalValue {
        val lhsVal = expr(lhs)
        val rhsVal = expr(rhs)
        val l = lhsVal.value
        val r = rhsVal.value
        return if (l != null && op == BinOp.BitOr && r != null) {
            l.bitOr(r, heap).fold(
                onSuccess = { GlobalValue.value(it) },
                onFailure = { err(span, StarlarkError(it.message ?: "bitor error", it)) },
            )
        } else {
            GlobalValue.any()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun expr(e: CstExpr): GlobalValue {
        val span = e.span
        return when (val node = e.node) {
            is ExprP.Tuple<*> -> tuple(node.elements as List<CstExpr>)
            is ExprP.Dot<*> -> dot(span, node.expr as CstExpr, node.field)
            is ExprP.Call<*> -> call(node.expr as CstExpr, node.args as CallArgsP<CstPayload>)
            is ExprP.Index<*> -> index(span, node.expr as CstExpr, node.index as CstExpr)
            is ExprP.Index2<*> -> index2(span, node.expr as CstExpr, node.index0 as CstExpr, node.index1 as CstExpr)
            is ExprP.Identifier<*, *> -> exprIdent(node.ident as CstIdent)
            is ExprP.Literal<*> -> exprLiteral(node.literal)
            is ExprP.Op<*> -> binOp(span, node.lhs as CstExpr, node.op, node.rhs as CstExpr)
            // These are not used in type expressions.
            is ExprP.Slice<*>,
            is ExprP.Lambda<*, *>,
            is ExprP.Not<*>,
            is ExprP.Minus<*>,
            is ExprP.Plus<*>,
            is ExprP.BitNot<*>,
            is ExprP.If<*>,
            is ExprP.ListExpr<*>,
            is ExprP.Dict<*>,
            is ExprP.ListComprehension<*>,
            is ExprP.DictComprehension<*>,
            is ExprP.FString<*>,
            -> GlobalValue.any()
        }
    }

    fun exprSpanned(e: CstExpr): Spanned<GlobalValue> {
        val value = expr(e)
        return Spanned(node = value, span = e.span)
    }

    @Suppress("UNCHECKED_CAST")
    fun load(loadStmt: LoadP<CstPayload, *>) {
        for (arg in loadStmt.args) {
            val ty = (loadStmt.payload as? Interface)?.get(arg.their.node) ?: Ty.any()
            assignIdentValue(arg.local as CstAssignIdent, GlobalValue.ty(ty))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun resolveAssignIdentToModuleSlotId(ident: CstAssignIdent): ModuleSlotId {
        val bindingId =
            ident.node.payload
                ?: throw internalError(ident.span, "binding not resolved")
        val binding = moduleScopeData.getBinding(bindingId)
        val resolvedSlot = binding.resolvedSlot(ctx.codemap)
        return when (resolvedSlot) {
            is Slot.Module -> resolvedSlot.id
            is Slot.Local -> throw internalError(ident.span, "local slot")
        }
    }

    fun assignIdentValue(ident: CstAssignIdent, value: GlobalValue) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(ident)
        val existing = values[moduleSlotId]
        if (existing != null) {
            values[moduleSlotId] = GlobalValue.union2(value, existing)
        } else {
            values[moduleSlotId] = value
        }
    }

    fun assignUnsetIdent(target: CstAssignIdent) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(target)
        values[moduleSlotId] = GlobalValue.any()
    }

    @Suppress("UNCHECKED_CAST")
    fun assignValue(lhs: Spanned<AssignTargetP<CstPayload>>, rhs: GlobalValue) {
        when (val node = lhs.node) {
            is AssignTargetP.Tuple<*> -> {
                for (x in node.elements) {
                    assignUnset(x as Spanned<AssignTargetP<CstPayload>>)
                }
            }
            is AssignTargetP.Index<*> -> { /* noop */ }
            is AssignTargetP.Dot<*> -> { /* noop */ }
            is AssignTargetP.Identifier<*, *> -> assignIdentValue(node.ident as CstAssignIdent, rhs)
        }
    }

    fun assign(lhs: Spanned<AssignTargetP<CstPayload>>, rhs: CstExpr) {
        val rhsValue = expr(rhs)
        assignValue(lhs, rhsValue)
    }

    /**
     * Unset the variables.
     *
     * When evaluating code like:
     *
     * ```python
     * if x:
     *   a = list
     * else:
     *   b = int
     * ```
     *
     * We don't know what branch is taken. So we just unset both `a` and `b`.
     */
    @Suppress("UNCHECKED_CAST")
    fun assignUnset(lhs: Spanned<AssignTargetP<CstPayload>>) {
        when (val node = lhs.node) {
            is AssignTargetP.Tuple<*> -> {
                for (x in node.elements) {
                    assignUnset(x as Spanned<AssignTargetP<CstPayload>>)
                }
            }
            is AssignTargetP.Index<*> -> { /* noop */ }
            is AssignTargetP.Dot<*> -> { /* noop */ }
            is AssignTargetP.Identifier<*, *> -> assignUnsetIdent(node.ident as CstAssignIdent)
        }
    }

    fun assignStmt(assignStmt: AssignP<CstPayload>) {
        // match ty { None => assign, Some(_ty) => assign }
        assign(assignStmt.lhs, assignStmt.rhs)
    }

    fun forStmtUnset(forStmt: ForP<CstPayload>) {
        assignUnset(forStmt.varTarget)
        evalStmtUnset(forStmt.body)
    }

    /**
     * When we are not sure if code is executed exactly once (like in a for loop body),
     * we just reset all the variables.
     */
    @Suppress("UNCHECKED_CAST")
    fun evalStmtUnset(stmt: CstStmt) {
        when (val node = stmt.node) {
            is StmtP.Break<*> -> { /* noop */ }
            is StmtP.Continue<*> -> { /* noop */ }
            is StmtP.Pass<*> -> { /* noop */ }
            is StmtP.Return<*> -> throw internalError(stmt.span, "return")
            is StmtP.Expression<*> -> { /* noop */ }
            is StmtP.Assign<*> -> assignUnset((node.assign as AssignP<CstPayload>).lhs)
            is StmtP.AssignModify<*> -> assignUnset(node.lhs as Spanned<AssignTargetP<CstPayload>>)
            is StmtP.Statements<*> -> {
                for (x in node.stmts) {
                    evalStmtUnset(x as CstStmt)
                }
            }
            is StmtP.If<*> -> evalStmtUnset(node.suite as CstStmt)
            is StmtP.IfElse<*> -> {
                evalStmtUnset(node.suite1 as CstStmt)
                evalStmtUnset(node.suite2 as CstStmt)
            }
            is StmtP.For<*> -> forStmtUnset(node.forStmt as ForP<CstPayload>)
            is StmtP.Def<*, *> -> assignUnsetIdent(node.def.name as CstAssignIdent)
            is StmtP.Load<*, *> -> throw internalError(stmt.span, "load")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun topLevelDef(def: DefP<CstPayload, *>) {
        val defParams = unpackDefParams(def.params, ctx.codemap)

        val posOnly = mutableListOf<Pair<ParamIsRequired, Ty>>()
        val posOrName = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var args: Ty? = null
        val nameOnly = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var kwargs: Ty? = null

        for (param in defParams) {
            val ty = getTyExprOpt(param.ty)
            when (val kind = param.kind) {
                is DefParamKind.Regular -> {
                    val name = param.ident.node.ident
                    val required = if (kind.defaultValue != null) ParamIsRequired.No else ParamIsRequired.Yes
                    when (kind.mode) {
                        DefRegularParamMode.PosOnly -> posOnly.add(Pair(required, ty))
                        DefRegularParamMode.PosOrName -> posOrName.add(Triple(name, required, ty))
                        DefRegularParamMode.NameOnly -> nameOnly.add(Triple(name, required, ty))
                    }
                }
                is DefParamKind.Args -> args = ty
                is DefParamKind.Kwargs -> kwargs = ty
            }
        }

        val result = getTyExprOpt(def.returnType)
        val paramSpec = ParamSpec.newParts(posOnly, posOrName, args, nameOnly, kwargs)
        assignIdentValue(def.name as CstAssignIdent, GlobalValue.ty(Ty.function(paramSpec, result)))
    }

    @Suppress("UNCHECKED_CAST")
    fun evalStmt(stmt: CstStmt) {
        val span = stmt.span
        when (val node = stmt.node) {
            is StmtP.Break<*> -> throw internalError(span, "top-level break")
            is StmtP.Continue<*> -> throw internalError(span, "top-level continue")
            is StmtP.Pass<*> -> { /* noop */ }
            is StmtP.Return<*> -> throw internalError(span, "top-level return")
            is StmtP.Expression<*> -> { /* noop */ }
            is StmtP.Assign<*> -> assignStmt(node.assign as AssignP<CstPayload>)
            is StmtP.AssignModify<*> -> { /* noop */ }
            is StmtP.Statements<*> -> throw internalError(span, "statements in top-level statement")
            is StmtP.If<*> -> evalStmtUnset(node.suite as CstStmt)
            is StmtP.IfElse<*> -> {
                evalStmtUnset(node.suite1 as CstStmt)
                evalStmtUnset(node.suite2 as CstStmt)
            }
            is StmtP.For<*> -> forStmtUnset(node.forStmt as ForP<CstPayload>)
            is StmtP.Def<*, *> -> topLevelDef(node.def as DefP<CstPayload, *>)
            is StmtP.Load<*, *> -> load(node.loadStmt as LoadP<CstPayload, *>)
        }
    }

    fun unknownTy(span: Span): Ty {
        approximations.add(Approximation.new("Unknown type", span))
        return Ty.any()
    }

    // Not yet ported as a separate type, inlined as Pair
    fun evalPath(first: CstIdent, rem: List<AstString>): Value? {
        var value = exprIdent(first).value ?: return null
        for (x in rem) {
            val result = value.getAttrError(x.node, heap)
            result.fold(
                onSuccess = { value = it },
                onFailure = { e ->
                    val span = first.span.merge(x.span)
                    errors.add(TypingError.new(StarlarkError(e.message ?: "getattr error", e), span, ctx.codemap))
                    return null
                },
            )
        }
        return value
    }

    fun tryProperTy(first: CstIdent, rem: List<AstString>): Ty? {
        val value = evalPath(first, rem) ?: return null
        return try {
            val ty = TypeCompiled.new(value, heap)
            ty.asTy()
        } catch (e: Exception) {
            val span =
                Span.mergeAll(
                    (listOf(first.span) + rem.map { it.span }).iterator(),
                )
            errors.add(TypingError.newAnyhow(e, span, ctx.codemap))
            null
        }
    }

    fun pathTy(first: CstIdent, rem: List<AstString>): Ty {
        tryProperTy(first, rem)?.let { return it }
        val span =
            Span.mergeAll(
                (listOf(first.span) + rem.map { it.span }).iterator(),
            )
        return unknownTy(span)
    }

    fun fromTypeExprImpl(x: Spanned<TypeExprUnpackP<CstPayload, CstIdentPayload>>): Ty =
        when (val node = x.node) {
            is TypeExprUnpackP.Ellipsis -> {
                approximations.add(Approximation.new("Ellipsis cannot be used as type", x))
                Ty.any()
            }
            is TypeExprUnpackP.List -> {
                approximations.add(Approximation.new("List literal [...] cannot be used as type", x))
                Ty.any()
            }
            is TypeExprUnpackP.Tuple -> {
                Ty.tuple(node.xs.map { fromTypeExprImpl(it) })
            }
            is TypeExprUnpackP.Union -> {
                Ty.unions(node.xs.map { fromTypeExprImpl(it) })
            }
            is TypeExprUnpackP.Path -> {
                pathTy(node.path.first, node.path.rem)
            }
            is TypeExprUnpackP.Index -> {
                val a = exprIdent(node.ident).value
                if (a != null) {
                    if (Constants.get().fnList?.let { a.ptrEq(it.value.toValue()) } != true &&
                        Constants.get().fnSet?.let { a.ptrEq(it.value.toValue()) } != true
                    ) {
                        approximations.add(Approximation.new("Not list", x))
                        Ty.any()
                    } else {
                        val i = fromTypeExprImpl(node.index)
                        val iCompiled = TypeCompiled.fromTy(i, heap)
                        a.getRef().at(iCompiled.toInner(), heap).fold(
                            onSuccess = { t ->
                                try {
                                    TypeCompiled.new(t, heap).asTy()
                                } catch (e: Exception) {
                                    approximations.add(Approximation.new("TypeCompiled.new failed", x))
                                    Ty.any()
                                }
                            },
                            onFailure = { e ->
                                approximations.add(Approximation.new("Getitem failed", x))
                                Ty.any()
                            },
                        )
                    }
                } else {
                    approximations.add(Approximation.new("Not global", x))
                    Ty.any()
                }
            }
            is TypeExprUnpackP.Index2 -> {
                val a = evalPath(node.path.node.first, node.path.node.rem)
                if (a != null) {
                    if (Constants.get().fnDict?.let { a.ptrEq(it.value.toValue()) } == true) {
                        val i0 = fromTypeExprImpl(node.i0)
                        val i1 = fromTypeExprImpl(node.i1)
                        val r0 = TypeCompiled.fromTy(i0, heap).toInner()
                        val r1 = TypeCompiled.fromTy(i1, heap).toInner()
                        a.getRef().at2(r0, r1, heap).fold(
                            onSuccess = { t ->
                                try {
                                    TypeCompiled.new(t, heap).asTy()
                                } catch (e: Exception) {
                                    approximations.add(Approximation.new("TypeCompiled.new failed", x))
                                    Ty.any()
                                }
                            },
                            onFailure = { e ->
                                approximations.add(Approximation.new("Getitem2 failed", x))
                                Ty.any()
                            },
                        )
                    } else if (Constants.get().fnTuple?.let { a.ptrEq(it.value.toValue()) } == true) {
                        val i0 = fromTypeExprImpl(node.i0)
                        val nodeI1 = node.i1.node
                        if (nodeI1 !is TypeExprUnpackP.Ellipsis) {
                            approximations.add(Approximation.new("Expecting ellipsis in tuple[x, ...]", x))
                            Ty.any()
                        } else {
                            val r0 = TypeCompiled.fromTy(i0, heap).toInner()
                            a.getRef().at2(r0, Ellipsis.newValue().toValue(), heap).fold(
                                onSuccess = { t ->
                                    try {
                                        TypeCompiled.new(t, heap).asTy()
                                    } catch (e: Exception) {
                                        approximations.add(Approximation.new("TypeCompiled.new failed", x))
                                        Ty.any()
                                    }
                                },
                                onFailure = { e ->
                                    approximations.add(Approximation.new("Getitem2 failed", x))
                                    Ty.any()
                                },
                            )
                        }
                    } else if (Constants.get().typingCallable?.let { a.ptrEq(it.value.toValue()) } == true) {
                        val nodeI0 = node.i0.node
                        if (nodeI0 !is TypeExprUnpackP.List) {
                            approximations.add(Approximation.new("Expecting list in Callable[[...], ...]", x))
                            Ty.any()
                        } else {
                            val args = nodeI0.items.map { TypeCompiled.fromTy(fromTypeExprImpl(it), heap).toInner() }
                            val argsList = heap.allocList(args)
                            val ret = fromTypeExprImpl(node.i1)
                            val retCompiled = TypeCompiled.fromTy(ret, heap).toInner()
                            a.getRef().at2(argsList, retCompiled, heap).fold(
                                onSuccess = { t ->
                                    try {
                                        TypeCompiled.new(t, heap).asTy()
                                    } catch (e: Exception) {
                                        approximations.add(Approximation.new("TypeCompiled.new failed", x))
                                        Ty.any()
                                    }
                                },
                                onFailure = { e ->
                                    approximations.add(Approximation.new("Getitem2 failed", x))
                                    Ty.any()
                                },
                            )
                        }
                    } else {
                        approximations.add(Approximation.new("Not dict or tuple", x))
                        Ty.any()
                    }
                } else {
                    approximations.add(Approximation.new("Not global", x))
                    Ty.any()
                }
            }
        }

    fun tyExpr(expr: CstTypeExpr): Ty {
        val x = TypeExprUnpackP.unpack<CstPayload, CstIdentPayload>(expr.node.expr, ctx.codemap)
        return fromTypeExprImpl(x)
    }

    fun getTyExpr(expr: CstTypeExpr): Ty =
        expr.cstPayload.typecheckerTy
            ?: throw internalError(expr.span, "type not set")

    fun getTyExprOpt(expr: CstTypeExpr?): Ty = if (expr == null) Ty.any() else getTyExpr(expr)

    fun fillTypes(stmt: CstStmt) {
        stmt.node.visitTypeExprErrMut { typeExpr ->
            val payload = typeExpr.cstPayload
            if (payload.typecheckerTy == null) {
                payload.typecheckerTy = tyExpr(typeExpr)
            }
        }
    }

    fun topLevelStmt(stmt: CstStmt) {
        // Fill all type payloads.
        fillTypes(stmt)
        // Partially evaluate expressions which can be used in the following type expressions.
        evalStmt(stmt)
    }
}

// Helper to unpack def params (mirrors Rust's DefParams::unpack)
@Suppress("UNCHECKED_CAST")
private fun unpackDefParams(params: List<io.github.kotlinmania.starlark.syntax.ast.AstParameterP<CstPayload>>, codemap: CodeMap): List<DefParam> {
    // States mirror Rust's DefParams::unpack
    // 0=Normal, 1=SeenSlash, 2=SeenStar, 3=SeenStarStar
    val argset = mutableSetOf<String>()
    var seenOptional = false

    val result = mutableListOf<DefParam>()
    var indexOfStar: Int? = null

    val numPositionalOnly: Int =
        run {
            val slashIdx =
                params.indexOfFirst {
                    it.node is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash<CstPayload>
                }
            when {
                slashIdx < 0 -> 0
                slashIdx == 0 -> throw EvalException.parserError(
                    "`/` cannot be first parameter",
                    params[0].span,
                    codemap,
                )
                else -> slashIdx
            }
        }

    var state = if (numPositionalOnly == 0) 1 else 0

    for ((i, p) in params.withIndex()) {
        val span = p.span

        // Check for duplicate parameter names
        val paramNode = p.node
        val ident = paramNode.ident()
        if (ident != null) {
            val name = (ident as CstAssignIdent).node.ident
            if (!argset.add(name)) {
                throw EvalException.parserError("duplicated parameter name", span, codemap)
            }
        }

        when (val param = p.node) {
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal<CstPayload> -> {
                if (state >= 3) {
                    throw EvalException.parserError("Parameter after kwargs", span, codemap)
                }
                if (param.defaultVal == null) {
                    if (seenOptional && state < 2) {
                        throw EvalException.parserError("positional parameter after non positional", span, codemap)
                    }
                } else {
                    seenOptional = true
                }
                val mode =
                    when {
                        state < 1 -> DefRegularParamMode.PosOnly
                        state < 2 -> DefRegularParamMode.PosOrName
                        else -> DefRegularParamMode.NameOnly
                    }
                result.add(
                    DefParam(
                        param.name as CstAssignIdent,
                        DefParamKind.Regular(mode, param.defaultVal),
                        param.typ,
                    ),
                )
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs<CstPayload> -> {
                if (state >= 2) {
                    throw EvalException.parserError(
                        "Args parameter after another args or kwargs parameter",
                        span,
                        codemap,
                    )
                }
                state = 2
                if (indexOfStar != null) {
                    throw EvalException.internalError(
                        "Multiple `*` in parameters, must have been caught earlier",
                        span,
                        codemap,
                    )
                }
                indexOfStar = i
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash<CstPayload> -> {
                if (state >= 1) {
                    throw EvalException.parserError("Multiple `/` in parameters", span, codemap)
                }
                state = 1
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args<CstPayload> -> {
                if (state >= 2) {
                    throw EvalException.parserError(
                        "Args parameter after another args or kwargs parameter",
                        span,
                        codemap,
                    )
                }
                state = 2
                result.add(DefParam(param.name as CstAssignIdent, DefParamKind.Args, param.typ))
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs<CstPayload> -> {
                if (state >= 3) {
                    throw EvalException.parserError("Multiple kwargs dictionary in parameters", span, codemap)
                }
                state = 3
                result.add(DefParam(param.name as CstAssignIdent, DefParamKind.Kwargs, param.typ))
            }
        }
    }

    if (indexOfStar != null) {
        val next = params.getOrNull(indexOfStar + 1)
        if (next == null) {
            throw EvalException.parserError("`*` parameter must not be last", params[indexOfStar].span, codemap)
        }
        if (next.node !is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal<*>) {
            throw EvalException.parserError("`*` must be followed by named parameter", next.span, codemap)
        }
    }

    return result
}

/** Types of module-level variables. */
internal class ModuleVarTypes(
    val types: MutableMap<ModuleSlotId, Ty> = mutableMapOf(),
)

/**
 * Populate `TypeExprP` type payload when running lint typechecker.
 * (Compiler typechecked populates the payload after proper full evaluation.)
 */
internal fun fillTypesForLintTypechecker(
    module: List<CstStmt>,
    ctx: TypingOracleCtx,
    moduleScopeData: ModuleScopeData,
    approximations: MutableList<Approximation>,
): Pair<List<TypingError>, ModuleVarTypes> =
    Heap.temp { heap ->
        val builder =
            GlobalTypesBuilder(
                heap = heap,
                ctx = ctx,
                values = mutableMapOf(),
                errors = mutableListOf(),
                moduleScopeData = moduleScopeData,
                approximations = approximations,
            )
        for (stmt in module) {
            builder.topLevelStmt(stmt)
        }
        val types = mutableMapOf<ModuleSlotId, Ty>()
        for ((k, v) in builder.values) {
            types[k] = v.ty
        }
        Pair(builder.errors, ModuleVarTypes(types))
    }
