// port-lint: source src/typing/fill_types_for_lint.rs
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
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.CodeMap
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
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP

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
        fun union2(a: GlobalValue, b: GlobalValue): GlobalValue {
            return GlobalValue(
                value = null,
                ty = Ty.union2(a.ty, b.ty),
            )
        }

        // fn value(value: Value) -> GlobalValue
        fun value(v: Value): GlobalValue {
            return GlobalValue(
                value = v,
                ty = Ty.any(), // Ty::of_value not yet ported
            )
        }

        // fn any() -> GlobalValue
        fun any(): GlobalValue {
            return GlobalValue(
                value = null,
                ty = Ty.any(),
            )
        }

        // fn ty(ty: Ty) -> GlobalValue
        fun ty(ty: Ty): GlobalValue {
            return GlobalValue(value = null, ty = ty)
        }
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
    fun internalError(span: Span, message: String): InternalError {
        return InternalError.msg(message, span, ctx.codemap)
    }

    // fn err(&mut self, span: Span, e: crate::Error) -> GlobalValue
    fun err(span: Span, e: StarlarkError): GlobalValue {
        errors.add(TypingError.new(e, span, ctx.codemap))
        return GlobalValue.any()
    }

    // fn call(&mut self, _f: &CstExpr, _args: &CallArgsP<CstPayload>) -> Result<GlobalValue, InternalError>
    fun call(_f: Spanned<ExprP<CstPayload>>, _args: CallArgsP<CstPayload>): GlobalValue {
        return GlobalValue.any()
    }

    // fn expr_ident(&self, ident: &CstIdent) -> Result<GlobalValue, InternalError>
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

    // fn expr_literal(&mut self, literal: &AstLiteral) -> Result<GlobalValue, InternalError>
    fun exprLiteral(literal: AstLiteral): GlobalValue {
        return when (literal) {
            is AstLiteral.String -> GlobalValue.value(heap.allocStr(literal.value.node).toValue())
            else -> GlobalValue.any()
        }
    }

    // fn tuple(&mut self, xs: &[CstExpr]) -> Result<GlobalValue, InternalError>
    fun tuple(xs: List<Spanned<ExprP<CstPayload>>>): GlobalValue {
        val results = xs.map { exprSpanned(it) }
        val allValues = results.mapNotNull { it.node.value }
        return if (allValues.size == results.size) {
            GlobalValue.value(heap.allocTuple(allValues))
        } else {
            GlobalValue.any()
        }
    }

    // fn dot(&mut self, span: Span, object: &CstExpr, field: &Spanned<String>) -> Result<GlobalValue, InternalError>
    fun dot(span: Span, obj: Spanned<ExprP<CstPayload>>, field: Spanned<String>): GlobalValue {
        val objValue = expr(obj)
        val v = objValue.value ?: return GlobalValue.any()
        return v.getAttrError(field.node, heap).fold(
            onSuccess = { GlobalValue.value(it) },
            onFailure = { err(span, StarlarkError(it.message ?: "getattr error", it)) },
        )
    }

    // fn index(&mut self, span: Span, array: &CstExpr, index: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn index2(&mut self, span: Span, array: &CstExpr, index0: &CstExpr, index1: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn bin_op(&mut self, span: Span, lhs: &CstExpr, op: BinOp, rhs: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn expr(&mut self, expr: &CstExpr) -> Result<GlobalValue, InternalError>
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

    // fn expr_spanned(&mut self, expr: &CstExpr) -> Result<Spanned<GlobalValue>, InternalError>
    fun exprSpanned(e: Spanned<ExprP<CstPayload>>): Spanned<GlobalValue> {
        val value = expr(e)
        return Spanned(node = value, span = e.span)
    }

    // fn load(&mut self, load: &LoadP<CstPayload>) -> Result<(), InternalError>
    fun load(loadStmt: LoadP<CstPayload, *>) {
        for (arg in loadStmt.args) {
            val ty = (loadStmt.payload as? Interface)?.get(arg.their.node) ?: Ty.any()
            assignIdentValue(arg.local, GlobalValue.ty(ty))
        }
    }

    // fn resolve_assign_ident_to_module_slot_id(&self, ident: &CstAssignIdent) -> Result<ModuleSlotId, InternalError>
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

    // fn assign_ident_value(&mut self, ident: &CstAssignIdent, value: GlobalValue) -> Result<(), InternalError>
    fun assignIdentValue(ident: Spanned<AssignIdentP<CstPayload, *>>, value: GlobalValue) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(ident)
        val existing = values[moduleSlotId]
        if (existing != null) {
            values[moduleSlotId] = GlobalValue.union2(value, existing)
        } else {
            values[moduleSlotId] = value
        }
    }

    // fn assign_unset_ident(&mut self, target: &CstAssignIdent) -> Result<(), InternalError>
    fun assignUnsetIdent(target: Spanned<AssignIdentP<CstPayload, *>>) {
        val moduleSlotId = resolveAssignIdentToModuleSlotId(target)
        values[moduleSlotId] = GlobalValue.any()
    }

    // fn assign_value(&mut self, lhs: &AssignTargetP<CstPayload>, rhs: GlobalValue) -> Result<(), InternalError>
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

    // fn assign(&mut self, lhs: &AssignTargetP<CstPayload>, rhs: &CstExpr) -> Result<(), InternalError>
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
    // fn assign_unset(&mut self, lhs: &AssignTargetP<CstPayload>) -> Result<(), InternalError>
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

    // fn top_level_def(&mut self, def: &DefP<CstPayload>) -> Result<(), InternalError>
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

    // fn eval_stmt(&mut self, stmt: &CstStmt) -> Result<(), InternalError>
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

    // fn unknown_ty(&mut self, span: Span) -> Ty
    fun unknownTy(span: Span): Ty {
        approximations.add(Approximation.new("Unknown type", span))
        return Ty.any()
    }

    // fn eval_path(&mut self, path: TypePathP<CstPayload>) -> Result<Option<Value>, InternalError>
    // TypePathP: { first: CstIdent, rem: Vec<Spanned<&str>> }
    // Not yet ported as a separate type, inlined as Pair
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

    // fn try_proper_ty(&mut self, path: TypePathP<CstPayload>) -> Result<Option<Ty>, InternalError>
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

    // fn path_ty(&mut self, path: TypePathP<CstPayload>) -> Result<Ty, InternalError>
    fun pathTy(first: Spanned<IdentP<CstPayload, *>>, rem: List<Spanned<String>>): Ty {
        tryProperTy(first, rem)?.let { return it }
        val span = Span.mergeAll(
            (listOf(first.span) + rem.map { it.span }).iterator()
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

    // fn ty_expr(&mut self, expr: &CstTypeExpr) -> Result<Ty, InternalError>
    fun tyExpr(expr: Spanned<TypeExprP<CstPayload, *>>): Ty {
        // TypeExprUnpackP.unpack not yet available
        // When TypeExprUnpackP is ported, this should be:
        //   val x = TypeExprUnpackP.unpack(expr.node.expr, ctx.codemap)
        //   return fromTypeExprImpl(x)
        return Ty.any()
    }

    // fn get_ty_expr(&self, expr: &CstTypeExpr) -> Result<Ty, InternalError>
    fun getTyExpr(expr: Spanned<TypeExprP<CstPayload, *>>): Ty {
        val payload = expr.node.payload as? CstTypeExprPayload
            ?: throw internalError(expr.span, "type not set")
        return payload.typecheckerTy
            ?: throw internalError(expr.span, "type not set")
    }

    // fn get_ty_expr_opt(&mut self, expr: Option<&CstTypeExpr>) -> Result<Ty, InternalError>
    fun getTyExprOpt(expr: Spanned<TypeExprP<CstPayload, *>>?): Ty {
        return if (expr == null) Ty.any() else getTyExpr(expr)
    }

    // fn fill_types(&mut self, stmt: &mut CstStmt) -> Result<(), InternalError>
    fun fillTypes(stmt: Spanned<StmtP<CstPayload>>) {
        // stmt.visit_type_expr_err_mut is not available yet.
        // When it is ported, this should iterate over all type expressions
        // in the statement and set their typecheckerTy payload.
        // For now, this is a no-op since we can't traverse type expressions.
    }

    // fn top_level_stmt(&mut self, stmt: &mut CstStmt) -> Result<(), InternalError>
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
// #[derive(Default)]
// pub(crate) struct ModuleVarTypes { pub(crate) types: UnorderedMap<ModuleSlotId, Ty> }
internal class ModuleVarTypes(
    val types: MutableMap<ModuleSlotId, Ty> = mutableMapOf(),
)

/**
 * Populate `TypeExprP` type payload when running lint typechecker.
 * (Compiler typechecked populates the payload after proper full evaluation.)
 */
// pub(crate) fn fill_types_for_lint_typechecker(...)
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
