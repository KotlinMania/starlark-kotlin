// port-lint: source typing/fill_types_for_lint.rs
package io.github.kotlinmania.starlark.typing

import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.AssignP
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.syntax.ast.ForP
import io.github.kotlinmania.starlark.syntax.ast.DefP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstTypeExprPayload
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.Slot
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopeData
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark.eval.compiler.visitTypeExprErrMut
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP
import io.github.kotlinmania.starlark.syntax.typeexpr.TypeExprUnpackP
import io.github.kotlinmania.starlark.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark.values.types.list.allocList

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

/** Value computed during partial evaluation of globals. */
private data class GlobalValue(
    /** `null` means we don't know (or know it may have different value depending on condition). */
    val value: Value?,
    val ty: Ty,
) {
    companion object {
        fun union2(a: GlobalValue, b: GlobalValue): GlobalValue {
            return GlobalValue(
                value = null,
                ty = Ty.union2(a.ty, b.ty),
            )
        }

        fun value(v: Value): GlobalValue {
            return GlobalValue(
                value = v,
                ty = Ty.ofValue(v),
            )
        }

        fun any(): GlobalValue {
            return GlobalValue(
                value = null,
                ty = Ty.any(),
            )
        }

        fun ty(ty: Ty): GlobalValue {
            return GlobalValue(value = null, ty = ty)
        }
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
    fun internalError(span: Span, message: String): InternalError {
        return InternalError.msg(message, span, ctx.codemap)
    }

    fun err(span: Span, e: StarlarkError): GlobalValue {
        errors.add(TypingError.new(e, span, ctx.codemap))
        return GlobalValue.any()
    }

    fun call(_f: Spanned<ExprP<CstPayload>>, _args: CallArgsP<CstPayload>): GlobalValue {
        return GlobalValue.any()
    }

    fun exprIdent(ident: Spanned<IdentP<CstPayload, *>>): GlobalValue {
        val resolved = ident.node.payload as? ResolvedIdent ?: throw internalError(ident.span, "unresolved ident")
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

    fun exprLiteral(literal: AstLiteral): GlobalValue {
        return when (literal) {
            is AstLiteral.String -> GlobalValue.value(heap.allocStr(literal.value.node).toValue())
            else -> GlobalValue.any()
        }
    }

    fun tuple(xs: List<Spanned<ExprP<CstPayload>>>): GlobalValue {
        val results = xs.map { exprSpanned(it) }
        val allValues = results.mapNotNull { it.node.value }
        return if (allValues.size == results.size) {
            GlobalValue.value(heap.allocTuple(allValues))
        } else {
            GlobalValue.any()
        }
    }

    fun dot(span: Span, obj: Spanned<ExprP<CstPayload>>, field: Spanned<String>): GlobalValue {
        val objValue = expr(obj)
        val v = objValue.value ?: return GlobalValue.any()
        return v.getAttrError(field.node, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "getattr error", it)) },
        )
    }

    fun index(span: Span, array: Spanned<ExprP<CstPayload>>, indexExpr: Spanned<ExprP<CstPayload>>): GlobalValue {
        val arrayVal = expr(array)
        val indexVal = exprSpanned(indexExpr)
        val a = arrayVal.value ?: return GlobalValue.any()
        val i = indexVal.node.value ?: return GlobalValue.any()
        return a.at(i, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "at error", it)) },
        )
    }

    fun index2(
        span: Span,
        array: Spanned<ExprP<CstPayload>>,
        index0: Spanned<ExprP<CstPayload>>,
        index1: Spanned<ExprP<CstPayload>>,
    ): GlobalValue {
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

    fun binOp(span: Span, lhs: Spanned<ExprP<CstPayload>>, op: BinOp, rhs: Spanned<ExprP<CstPayload>>): GlobalValue {
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

    fun expr(e: Spanned<ExprP<CstPayload>>): GlobalValue {
        val span = e.span
        return when (val node = e.node) {
            is ExprP.Tuple -> tuple(node.elements)
            is ExprP.Dot -> dot(span, node.expr, node.field)
            is ExprP.Call -> call(node.expr, node.args)
            is ExprP.Index -> index(span, node.expr, node.index)
            is ExprP.Index2 -> index2(span, node.expr, node.index0, node.index1)
            is ExprP.Identifier<CstPayload, *> -> exprIdent(node.ident)
            is ExprP.Literal -> exprLiteral(node.literal)
            is ExprP.Op -> binOp(span, node.lhs, node.op, node.rhs)
            // These are not used in type expressions.
            is ExprP.Slice,
            is ExprP.Lambda<CstPayload, *>,
            is ExprP.Not,
            is ExprP.Minus,
            is ExprP.Plus,
            is ExprP.BitNot,
            is ExprP.If,
            is ExprP.ListExpr,
            is ExprP.Dict,
            is ExprP.ListComprehension,
            is ExprP.DictComprehension,
            is ExprP.FString -> GlobalValue.any()
        }
    }

    fun exprSpanned(e: Spanned<ExprP<CstPayload>>): Spanned<GlobalValue> {
        val value = expr(e)
        return Spanned(node = value, span = e.span)
    }

    fun load(loadStmt: LoadP<CstPayload, *>) {
        for (arg in loadStmt.args) {
            val ty = (loadStmt.payload as? Interface)?.get(arg.their.node) ?: Ty.any()
            assignIdentValue(arg.local, GlobalValue.ty(ty))
        }
    }

    fun resolveAssignIdentToModuleSlotId(ident: Spanned<AssignIdentP<CstPayload, *>>): ModuleSlotId {
        val bindingId = ident.node.payload as? BindingId
            ?: throw internalError(ident.span, "binding not resolved")
        val binding = moduleScopeData.getBinding(bindingId)
        val resolvedSlot = binding.resolvedSlot(ctx.codemap)
        return when (resolvedSlot) {
            is Slot.Module -> resolvedSlot.id
            is Slot.Local -> throw internalError(ident.span, "local slot")
        }
    }

    fun assignIdentValue(ident: Spanned<AssignIdentP<CstPayload, *>>, value: GlobalValue) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(ident)
        val existing = values[moduleSlotId]
        if (existing != null) {
            values[moduleSlotId] = GlobalValue.union2(value, existing)
        } else {
            values[moduleSlotId] = value
        }
    }

    fun assignUnsetIdent(target: Spanned<AssignIdentP<CstPayload, *>>) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(target)
        values[moduleSlotId] = GlobalValue.any()
    }

    fun assignValue(lhs: Spanned<AssignTargetP<CstPayload>>, rhs: GlobalValue) {
        when (val node = lhs.node) {
            is AssignTargetP.Tuple -> {
                for (x in node.elements) {
                    assignUnset(x)
                }
            }
            is AssignTargetP.Index -> { /* noop */ }
            is AssignTargetP.Dot -> { /* noop */ }
            is AssignTargetP.Identifier<CstPayload, *> -> assignIdentValue(node.ident, rhs)
        }
    }

    fun assign(lhs: Spanned<AssignTargetP<CstPayload>>, rhs: Spanned<ExprP<CstPayload>>) {
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
    fun assignUnset(lhs: Spanned<AssignTargetP<CstPayload>>) {
        when (val node = lhs.node) {
            is AssignTargetP.Tuple -> {
                for (x in node.elements) {
                    assignUnset(x)
                }
            }
            is AssignTargetP.Index -> { /* noop */ }
            is AssignTargetP.Dot -> { /* noop */ }
            is AssignTargetP.Identifier<CstPayload, *> -> assignUnsetIdent(node.ident)
        }
    }

    fun assignStmt(assignStmt: AssignP<CstPayload>) {
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
    fun evalStmtUnset(stmt: Spanned<StmtP<CstPayload>>) {
        when (val node = stmt.node) {
            is StmtP.Break -> { /* noop */ }
            is StmtP.Continue -> { /* noop */ }
            is StmtP.Pass -> { /* noop */ }
            is StmtP.Return -> throw internalError(stmt.span, "return")
            is StmtP.Expression -> { /* noop */ }
            is StmtP.Assign -> assignUnset(node.assign.lhs)
            is StmtP.AssignModify -> assignUnset(node.lhs)
            is StmtP.Statements -> {
                for (x in node.stmts) {
                    evalStmtUnset(x)
                }
            }
            is StmtP.If -> evalStmtUnset(node.suite)
            is StmtP.IfElse -> {
                evalStmtUnset(node.suite1)
                evalStmtUnset(node.suite2)
            }
            is StmtP.For -> forStmtUnset(node.forStmt)
            is StmtP.Def<CstPayload, *> -> assignUnsetIdent(node.def.name)
            is StmtP.Load<CstPayload, *> -> throw internalError(stmt.span, "load")
        }
    }

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
        assignIdentValue(def.name, GlobalValue.ty(Ty.function(paramSpec, result)))
    }

    fun evalStmt(stmt: Spanned<StmtP<CstPayload>>) {
        val span = stmt.span
        when (val node = stmt.node) {
            is StmtP.Break -> throw internalError(span, "top-level break")
            is StmtP.Continue -> throw internalError(span, "top-level continue")
            is StmtP.Pass -> { /* noop */ }
            is StmtP.Return -> throw internalError(span, "top-level return")
            is StmtP.Expression -> { /* noop */ }
            is StmtP.Assign -> assignStmt(node.assign)
            is StmtP.AssignModify -> { /* noop */ }
            is StmtP.Statements -> throw internalError(span, "statements in top-level statement")
            is StmtP.If -> evalStmtUnset(node.suite)
            is StmtP.IfElse -> {
                evalStmtUnset(node.suite1)
                evalStmtUnset(node.suite2)
            }
            is StmtP.For -> forStmtUnset(node.forStmt)
            is StmtP.Def<CstPayload, *> -> topLevelDef(node.def)
            is StmtP.Load<CstPayload, *> -> load(node.loadStmt)
        }
    }

    fun unknownTy(span: Span): Ty {
        approximations.add(Approximation.new("Unknown type", span))
        return Ty.any()
    }

    // TypePathP { first: CstIdent, rem: List<Spanned<String>> }
    // Not yet ported as a separate type, parameters passed individually.
    fun evalPath(first: Spanned<IdentP<CstPayload, *>>, rem: List<Spanned<String>>): Value? {
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

    fun tryProperTy(first: Spanned<IdentP<CstPayload, *>>, rem: List<Spanned<String>>): Ty? {
        val value = evalPath(first, rem) ?: return null
        return try {
            val ty = TypeCompiled.new(value, heap)
            ty.asTy()
        } catch (e: Exception) {
            val span = Span.mergeAll(
                (listOf(first.span) + rem.map { it.span }).iterator()
            )
            errors.add(TypingError.newAnyhow(e, span, ctx.codemap))
            null
        }
    }

    fun pathTy(first: Spanned<IdentP<CstPayload, *>>, rem: List<Spanned<String>>): Ty {
        tryProperTy(first, rem)?.let { return it }
        val span = Span.mergeAll(
            (listOf(first.span) + rem.map { it.span }).iterator()
        )
        return unknownTy(span)
    }

    fun fromTypeExprImpl(x: Spanned<TypeExprUnpackP<CstPayload, ResolvedIdent?>>): Ty {
        return when (val node = x.node) {
            is TypeExprUnpackP.Ellipsis<CstPayload, ResolvedIdent?> -> {
                approximations.add(Approximation.new("Ellipsis cannot be used as type", x))
                Ty.any()
            }
            is TypeExprUnpackP.List<CstPayload, ResolvedIdent?> -> {
                approximations.add(Approximation.new("List literal [...] cannot be used as type", x))
                Ty.any()
            }
            is TypeExprUnpackP.Tuple<CstPayload, ResolvedIdent?> -> {
                Ty.tuple(node.xs.map { fromTypeExprImpl(it) })
            }
            is TypeExprUnpackP.Union<CstPayload, ResolvedIdent?> -> {
                Ty.unions(node.xs.map { fromTypeExprImpl(it) })
            }
            is TypeExprUnpackP.Path<CstPayload, ResolvedIdent?> -> {
                pathTy(node.path.first, node.path.rem)
            }
            is TypeExprUnpackP.Index<CstPayload, ResolvedIdent?> -> {
                val pathFirst = node.ident
                val pathSpanned = Spanned(
                    node = io.github.kotlinmania.starlark.syntax.typeexpr.TypePathP<CstPayload, ResolvedIdent?>(
                        first = pathFirst,
                        rem = emptyList(),
                    ),
                    span = pathFirst.span,
                )
                val a = exprIdent(pathFirst).value
                if (a == null) {
                    approximations.add(Approximation.new("Not global", x))
                    return Ty.any()
                }
                val fnList = Constants.get().fnList?.value?.toValue()
                if (fnList == null || !a.ptrEq(fnList)) {
                    approximations.add(Approximation.new("Not list", x))
                    return Ty.any()
                }
                val i = fromTypeExprImpl(node.index)
                val iCompiled = TypeCompiled.fromTy(i, heap)
                val atResult = a.getRef().at(iCompiled.toInner(), heap)
                atResult.fold(
                    onSuccess = { t ->
                        try {
                            TypeCompiled.new(t, heap).asTy()
                        } catch (e: Exception) {
                            approximations.add(Approximation.new("TypeCompiled::new failed", x))
                            Ty.any()
                        }
                    },
                    onFailure = { e ->
                        approximations.add(Approximation.new("Getitem failed", e))
                        Ty.any()
                    },
                )
            }
            is TypeExprUnpackP.Index2<CstPayload, ResolvedIdent?> -> {
                val a = evalPath(node.path.node.first, node.path.node.rem)
                if (a == null) {
                    approximations.add(Approximation.new("Not global", x))
                    return Ty.any()
                }
                val constants = Constants.get()
                val fnDict = constants.fnDict?.value?.toValue()
                val fnTuple = constants.fnTuple?.value?.toValue()
                val typingCallable = constants.typingCallable?.value?.toValue()
                when {
                    fnDict != null && a.ptrEq(fnDict) -> {
                        val i0 = fromTypeExprImpl(node.i0)
                        val i1 = fromTypeExprImpl(node.i1)
                        val r0 = TypeCompiled.fromTy(i0, heap)
                        val r1 = TypeCompiled.fromTy(i1, heap)
                        a.getRef().at2(r0.toInner(), r1.toInner(), heap).fold(
                            onSuccess = { t ->
                                try { TypeCompiled.new(t, heap).asTy() } catch (e: Exception) {
                                    approximations.add(Approximation.new("TypeCompiled::new failed", x))
                                    Ty.any()
                                }
                            },
                            onFailure = { e ->
                                approximations.add(Approximation.new("Getitem2 failed", e))
                                Ty.any()
                            },
                        )
                    }
                    fnTuple != null && a.ptrEq(fnTuple) -> {
                        val i0 = fromTypeExprImpl(node.i0)
                        if (node.i1.node !is TypeExprUnpackP.Ellipsis<*, *>) {
                            approximations.add(Approximation.new("Expecting ellipsis in tuple[x, ...]", x))
                            return Ty.any()
                        }
                        val r0 = TypeCompiled.fromTy(i0, heap)
                        a.getRef().at2(r0.toInner(), Ellipsis.newValue().toValue(), heap).fold(
                            onSuccess = { t ->
                                try { TypeCompiled.new(t, heap).asTy() } catch (e: Exception) {
                                    approximations.add(Approximation.new("TypeCompiled::new failed", x))
                                    Ty.any()
                                }
                            },
                            onFailure = { e ->
                                approximations.add(Approximation.new("Getitem2 failed", e))
                                Ty.any()
                            },
                        )
                    }
                    typingCallable != null && a.ptrEq(typingCallable) -> {
                        val items = node.i0.node
                        if (items !is TypeExprUnpackP.List<CstPayload, ResolvedIdent?>) {
                            approximations.add(Approximation.new("Expecting list in Callable[[...], ...]", x))
                            return Ty.any()
                        }
                        val argList = items.items.map { TypeCompiled.fromTy(fromTypeExprImpl(it), heap).toInner() }
                        val argsValue = heap.allocList(argList)
                        val ret = fromTypeExprImpl(node.i1)
                        val retValue = TypeCompiled.fromTy(ret, heap).toInner()
                        a.getRef().at2(argsValue, retValue, heap).fold(
                            onSuccess = { t ->
                                try { TypeCompiled.new(t, heap).asTy() } catch (e: Exception) {
                                    approximations.add(Approximation.new("TypeCompiled::new failed", x))
                                    Ty.any()
                                }
                            },
                            onFailure = { e ->
                                approximations.add(Approximation.new("Getitem2 failed", e))
                                Ty.any()
                            },
                        )
                    }
                    else -> {
                        approximations.add(Approximation.new("Not dict or tuple", x))
                        Ty.any()
                    }
                }
            }
        }
    }

    fun tyExpr(expr: Spanned<TypeExprP<CstPayload, *>>): Ty {
        val unpacked = TypeExprUnpackP.unpack<CstPayload, ResolvedIdent?>(expr.node.expr, ctx.codemap)
        return fromTypeExprImpl(unpacked)
    }

    fun getTyExpr(expr: Spanned<TypeExprP<CstPayload, *>>): Ty {
        val payload = expr.node.payload as? CstTypeExprPayload
            ?: throw internalError(expr.span, "type not set")
        return payload.typecheckerTy
            ?: throw internalError(expr.span, "type not set")
    }

    fun getTyExprOpt(expr: Spanned<TypeExprP<CstPayload, *>>?): Ty {
        return if (expr == null) Ty.any() else getTyExpr(expr)
    }

    fun fillTypes(stmt: Spanned<StmtP<CstPayload>>) {
        stmt.node.visitTypeExprErrMut { typeExpr ->
            val payload = typeExpr.node.payload as? CstTypeExprPayload
                ?: throw internalError(typeExpr.span, "type expr without CstTypeExprPayload")
            if (payload.typecheckerTy != null) {
                throw internalError(typeExpr.span, "type already set")
            }
            payload.typecheckerTy = tyExpr(typeExpr)
        }
    }

    fun topLevelStmt(stmt: Spanned<StmtP<CstPayload>>) {
        // Fill all type payloads.
        fillTypes(stmt)
        // Partially evaluate expressions which can be used in the following type expressions.
        evalStmt(stmt)
    }
}

// Helper to unpack def params (mirrors Bindings.kt's unpackDefParams)
private fun unpackDefParams(
    params: List<Spanned<io.github.kotlinmania.starlark.syntax.ast.ParameterP<CstPayload>>>,
    codemap: CodeMap,
): List<DefParam> {
    val result = mutableListOf<DefParam>()
    var seenStar = false
    for (p in params) {
        when (val param = p.node) {
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash -> { /* skip */ }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs -> seenStar = true
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal -> {
                val mode = if (seenStar) DefRegularParamMode.NameOnly else DefRegularParamMode.PosOrName
                result.add(DefParam(
                    param.name,
                    DefParamKind.Regular(mode, param.defaultVal),
                    param.typ,
                ))
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args -> {
                seenStar = true
                result.add(DefParam(param.name, DefParamKind.Args, param.typ))
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs ->
                result.add(DefParam(param.name, DefParamKind.Kwargs, param.typ))
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
    module: List<Spanned<StmtP<CstPayload>>>,
    ctx: TypingOracleCtx,
    moduleScopeData: ModuleScopeData,
    approximations: MutableList<Approximation>,
): Pair<List<TypingError>, ModuleVarTypes> {
    return Heap.temp { heap ->
        val builder = GlobalTypesBuilder(
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
}
