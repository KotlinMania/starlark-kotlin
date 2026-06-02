// port-lint: source src/typing/fill_types_for_lint.rs
package io.github.kotlinmania.starlark.typing

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopeData
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.Slot
import io.github.kotlinmania.starlark.eval.compiler.scope.CstAssignIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark.eval.compiler.scope.CstTypeExpr
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
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Heap
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
// struct GlobalValue<'v>
private data class GlobalValue(
    /** `null` means we don't know (or know it may have different value depending on condition). */
    val value: Value?,
    val ty: Ty,
) {
    companion object {
        // fn union2(a: GlobalValue, b: GlobalValue) -> GlobalValue
        fun union2(a: GlobalValue, b: GlobalValue): GlobalValue =
            GlobalValue(
                value = null,
                ty = Ty.union2(a.ty, b.ty),
            )

        // fn value(value: Value) -> GlobalValue
        fun value(v: Value): GlobalValue =
            GlobalValue(
                value = v,
                ty = Ty.any(), // Ty::of_value not yet ported
            )

        // fn any() -> GlobalValue
        fun any(): GlobalValue =
            GlobalValue(
                value = null,
                ty = Ty.any(),
            )

        // fn ty(ty: Ty) -> GlobalValue
        fun ty(ty: Ty): GlobalValue = GlobalValue(value = null, ty = ty)
    }
}

// struct GlobalTypesBuilder<'a, 'v>
private class GlobalTypesBuilder(
    val approximations: MutableList<Approximation>,
    val heap: Heap,
    val values: MutableMap<ModuleSlotId, GlobalValue>,
    val errors: MutableList<TypingError>,
    val moduleScopeData: ModuleScopeData,
    val ctx: TypingOracleCtx,
) {
    // fn internal_error(&self, span: Span, message: impl Display) -> InternalError
    fun internalError(span: Span, message: String): InternalError = InternalError.msg(message, span, ctx.codemap)

    // fn err(&mut self, span: Span, e: crate::Error) -> GlobalValue
    fun err(span: Span, e: StarlarkError): GlobalValue {
        errors.add(TypingError.new(e, span, ctx.codemap))
        return GlobalValue.any()
    }

    // fn call(&mut self, _f: &CstExpr, _args: &CallArgsP<CstPayload>) -> Result<GlobalValue, InternalError>
    fun call(_f: CstExpr, _args: CallArgsP<CstPayload>): GlobalValue = GlobalValue.any()

    // fn expr_ident(&self, ident: &CstIdent) -> Result<GlobalValue, InternalError>
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

    // fn expr_literal(&mut self, literal: &AstLiteral) -> Result<GlobalValue, InternalError>
    fun exprLiteral(literal: AstLiteral): GlobalValue =
        when (literal) {
            is AstLiteral.String -> GlobalValue.value(heap.allocStr(literal.value.node))
            else -> GlobalValue.any()
        }

    // fn tuple(&mut self, xs: &[CstExpr]) -> Result<GlobalValue, InternalError>
    fun tuple(xs: List<CstExpr>): GlobalValue {
        val results = xs.map { exprSpanned(it) }
        val allValues = results.mapNotNull { it.node.value }
        return if (allValues.size == results.size) {
            GlobalValue.value(heap.allocTuple(allValues))
        } else {
            GlobalValue.any()
        }
    }

    // fn dot(&mut self, span: Span, object: &CstExpr, field: &AstString) -> Result<GlobalValue, InternalError>
    fun dot(span: Span, obj: CstExpr, field: AstString): GlobalValue {
        val objValue = expr(obj)
        val v = objValue.value ?: return GlobalValue.any()
        return v.getAttrError(field.node, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "getattr error", it)) },
        )
    }

    // fn index(&mut self, span: Span, array: &CstExpr, index: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn index2(&mut self, span: Span, array: &CstExpr, index0: &CstExpr, index1: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn bin_op(&mut self, span: Span, lhs: &CstExpr, op: BinOp, rhs: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn expr(&mut self, expr: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn expr_spanned(&mut self, expr: &CstExpr) -> Result<Spanned<GlobalValue>, InternalError>
    fun exprSpanned(e: CstExpr): Spanned<GlobalValue> {
        val value = expr(e)
        return Spanned(node = value, span = e.span)
    }

    // fn load(&mut self, load: &LoadP<CstPayload>) -> Result<(), InternalError>
    @Suppress("UNCHECKED_CAST")
    fun load(loadStmt: LoadP<CstPayload, *>) {
        for (arg in loadStmt.args) {
            val ty = (loadStmt.payload as? Interface)?.get(arg.their.node) ?: Ty.any()
            assignIdentValue(arg.local as CstAssignIdent, GlobalValue.ty(ty))
        }
    }

    // fn resolve_assign_ident_to_module_slot_id(&self, ident: &CstAssignIdent) -> Result<ModuleSlotId, InternalError>
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

    // fn assign_ident_value(&mut self, ident: &CstAssignIdent, value: GlobalValue) -> Result<(), InternalError>
    fun assignIdentValue(ident: CstAssignIdent, value: GlobalValue) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(ident)
        val existing = values[moduleSlotId]
        if (existing != null) {
            values[moduleSlotId] = GlobalValue.union2(value, existing)
        } else {
            values[moduleSlotId] = value
        }
    }

    // fn assign_unset_ident(&mut self, target: &CstAssignIdent) -> Result<(), InternalError>
    fun assignUnsetIdent(target: CstAssignIdent) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(target)
        values[moduleSlotId] = GlobalValue.any()
    }

    // fn assign_value(&mut self, lhs: &AssignTargetP<CstPayload>, rhs: GlobalValue) -> Result<(), InternalError>
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

    // fn assign(&mut self, lhs: &AssignTargetP<CstPayload>, rhs: &CstExpr) -> Result<(), InternalError>
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
    // fn assign_unset(&mut self, lhs: &AssignTargetP<CstPayload>) -> Result<(), InternalError>
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

    // fn assign_stmt(&mut self, assign: &AssignP<CstPayload>) -> Result<(), InternalError>
    fun assignStmt(assignStmt: AssignP<CstPayload>) {
        // match ty { None => assign, Some(_ty) => assign }
        assign(assignStmt.lhs, assignStmt.rhs)
    }

    // fn for_stmt_unset(&mut self, for_stmt: &ForP<CstPayload>) -> Result<(), InternalError>
    fun forStmtUnset(forStmt: ForP<CstPayload>) {
        assignUnset(forStmt.varTarget)
        evalStmtUnset(forStmt.body)
    }

    /**
     * When we are not sure if code is executed exactly once (like in a for loop body),
     * we just reset all the variables.
     */
    // fn eval_stmt_unset(&mut self, stmt: &CstStmt) -> Result<(), InternalError>
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

    // fn top_level_def(&mut self, def: &DefP<CstPayload>) -> Result<(), InternalError>
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

        val result = getTyExprOpt(def.returnType as CstTypeExpr?)
        val paramSpec = ParamSpec.newParts(posOnly, posOrName, args, nameOnly, kwargs)
        assignIdentValue(def.name as CstAssignIdent, GlobalValue.ty(Ty.function(paramSpec, result)))
    }

    // fn eval_stmt(&mut self, stmt: &CstStmt) -> Result<(), InternalError>
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

    // fn unknown_ty(&mut self, span: Span) -> Ty
    fun unknownTy(span: Span): Ty {
        approximations.add(Approximation.new("Unknown type", span))
        return Ty.any()
    }

    // fn eval_path(&mut self, path: TypePathP<CstPayload>) -> Result<Option<Value>, InternalError>
    // TypePathP: { first: CstIdent, rem: Vec<Spanned<&str>> }
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

    // fn try_proper_ty(&mut self, path: TypePathP<CstPayload>) -> Result<Option<Ty>, InternalError>
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

    // fn path_ty(&mut self, path: TypePathP<CstPayload>) -> Result<Ty, InternalError>
    fun pathTy(first: CstIdent, rem: List<AstString>): Ty {
        tryProperTy(first, rem)?.let { return it }
        val span =
            Span.mergeAll(
                (listOf(first.span) + rem.map { it.span }).iterator(),
            )
        return unknownTy(span)
    }

    // fn from_type_expr_impl(&mut self, x: &Spanned<TypeExprUnpackP<CstPayload>>) -> Result<Ty, InternalError>
    // TypeExprUnpackP is not yet ported as a Kotlin type.
    // This function currently cannot be fully implemented until TypeExprUnpackP is ported.
    // For now we provide a stub that returns Ty.any() with an approximation.
    fun fromTypeExprImpl(x: Spanned<*>): Ty {
        approximations.add(Approximation.new("TypeExprUnpackP not yet ported", x))
        return Ty.any()
    }

    fun tyExpr(expr: CstTypeExpr): Ty {
        // TypeExprUnpackP.unpack not yet available
        // When TypeExprUnpackP is ported, this should be:
        //   val x = TypeExprUnpackP.unpack(expr.node.expr, ctx.codemap)
        //   return fromTypeExprImpl(x)
        return Ty.any()
    }

    fun getTyExpr(expr: CstTypeExpr): Ty {
        return expr.node.payload.typecheckerTy
            ?: throw internalError(expr.span, "type not set")
    }

    fun getTyExprOpt(expr: CstTypeExpr?): Ty = if (expr == null) Ty.any() else getTyExpr(expr)

    // fn fill_types(&mut self, stmt: &mut CstStmt) -> Result<(), InternalError>
    fun fillTypes(stmt: CstStmt) {
        // stmt.visit_type_expr_err_mut is not available yet.
        // When it is ported, this should iterate over all type expressions
        // in the statement and set their typecheckerTy payload.
        // For now, this is a no-op since we can't traverse type expressions.
    }

    // fn top_level_stmt(&mut self, stmt: &mut CstStmt) -> Result<(), InternalError>
    fun topLevelStmt(stmt: CstStmt) {
        // Fill all type payloads.
        fillTypes(stmt)
        // Partially evaluate expressions which can be used in the following type expressions.
        evalStmt(stmt)
    }
}

// Helper to unpack def params (mirrors Rust's DefParams::unpack)
@Suppress("UNCHECKED_CAST")
private fun unpackDefParams(params: List<Spanned<*>>, codemap: CodeMap): List<DefParam> {
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
                    it.node is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash<*>
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
        val paramNode = p.node as? io.github.kotlinmania.starlark.syntax.ast.ParameterP<*>
        val ident = paramNode?.ident()
        if (ident != null) {
            val name = (ident as CstAssignIdent).node.ident
            if (!argset.add(name)) {
                throw EvalException.parserError("duplicated parameter name", span, codemap)
            }
        }

        when (val param = p.node) {
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal<*> -> {
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
                        DefParamKind.Regular(mode, param.defaultVal as CstExpr?),
                        param.typ as CstTypeExpr?,
                    ),
                )
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs<*> -> {
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
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash<*> -> {
                if (state >= 1) {
                    throw EvalException.parserError("Multiple `/` in parameters", span, codemap)
                }
                state = 1
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args<*> -> {
                if (state >= 2) {
                    throw EvalException.parserError(
                        "Args parameter after another args or kwargs parameter",
                        span,
                        codemap,
                    )
                }
                state = 2
                result.add(DefParam(param.name as CstAssignIdent, DefParamKind.Args, param.typ as CstTypeExpr?))
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs<*> -> {
                if (state >= 3) {
                    throw EvalException.parserError("Multiple kwargs dictionary in parameters", span, codemap)
                }
                state = 3
                result.add(DefParam(param.name as CstAssignIdent, DefParamKind.Kwargs, param.typ as CstTypeExpr?))
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
// #[derive(Default)]
// pub(crate) struct ModuleVarTypes { pub(crate) types: UnorderedMap<ModuleSlotId, Ty> }
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
