// port-lint: source src/typing/fill_types_for_lint.rs
package io.github.kotlinmania.starlark_kotlin.typing

import io.github.kotlinmania.starlark_kotlin.values.typing.Approximation
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.merge
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.environment.GlobalValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP

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
    fun err(span: Span, e: Exception): GlobalValue {
        errors.add(TypingError.new(e, span, ctx.codemap))
        return GlobalValue.any()
    }

    // fn call(&mut self, _f: &CstExpr, _args: &CallArgsP<CstPayload>) -> Result<GlobalValue, InternalError>
    fun call(f: CstExpr, args: CallArgs): GlobalValue {
        return GlobalValue.any()
    }

    // fn expr_ident(&self, ident: &CstIdent) -> Result<GlobalValue, InternalError>
    fun exprIdent(ident: CstIdent): GlobalValue {
        val resolved = ident.payload ?: throw internalError(ident.span, "unresolved ident")
        return when (resolved) {
            is ResolvedIdent.SlotModule -> {
                values[resolved.slotId] ?: GlobalValue.any()
            }
            is ResolvedIdent.SlotLocal -> {
                throw internalError(ident.span, "local slot in global scope")
            }
            is ResolvedIdent.Global -> {
                GlobalValue.value(resolved.value)
            }
            else -> throw IllegalStateException("Unexpected resolved ident: $resolved")
        }
    }

    // fn expr_literal(&mut self, literal: &AstLiteral) -> Result<GlobalValue, InternalError>
    fun exprLiteral(literal: AstLiteral): GlobalValue {
        return when (literal) {
            is AstLiteral.StringLit -> GlobalValue.value(heap.alloc(literal.value))
            else -> GlobalValue.any()
        }
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
        return try {
            GlobalValue.value(getAttrError(v, field.value, heap))
        } catch (e: Exception) {
            err(span, e)
        }
    }

    // fn index(&mut self, span: Span, array: &CstExpr, index: &CstExpr) -> Result<GlobalValue, InternalError>
    fun index(span: Span, array: CstExpr, indexExpr: CstExpr): GlobalValue {
        val arrayVal = expr(array)
        val indexVal = exprSpanned(indexExpr)
        val a = arrayVal.value ?: return GlobalValue.any()
        val i = indexVal.node.value ?: return GlobalValue.any()
        return try {
            GlobalValue.value(at(a, i, heap))
        } catch (e: Exception) {
            err(span, e)
        }
    }

    // fn index2(&mut self, span: Span, array: &CstExpr, index0: &CstExpr, index1: &CstExpr) -> Result<GlobalValue, InternalError>
    fun index2(span: Span, array: CstExpr, index0: CstExpr, index1: CstExpr): GlobalValue {
        val arrayVal = expr(array)
        val idx0Val = expr(index0)
        val idx1Val = expr(index1)
        val a = arrayVal.value ?: return GlobalValue.any()
        val i0 = idx0Val.value ?: return GlobalValue.any()
        val i1 = idx1Val.value ?: return GlobalValue.any()
        return try {
            GlobalValue.value(at2(a, i0, i1, heap))
        } catch (e: Exception) {
            err(span, e)
        }
    }

    // fn bin_op(&mut self, span: Span, lhs: &CstExpr, op: BinOp, rhs: &CstExpr) -> Result<GlobalValue, InternalError>
    fun binOp(span: Span, lhs: CstExpr, op: BinOp, rhs: CstExpr): GlobalValue {
        val lhsVal = expr(lhs)
        val rhsVal = expr(rhs)
        val l = lhsVal.value
        val r = rhsVal.value
        return if (l != null && op == BinOp.BitOr && r != null) {
            try {
                GlobalValue.value(bitOr(l, r, heap))
            } catch (e: Exception) {
                err(span, e)
            }
        } else {
            GlobalValue.any()
        }
    }

    // fn expr(&mut self, expr: &CstExpr) -> Result<GlobalValue, InternalError>
    fun expr(e: CstExpr): GlobalValue {
        val span = e.span
        return when (val node = e.node) {
            is ExprP.Tuple -> tuple(node.elements)
            is ExprP.Dot -> dot(span, node.obj, node.field)
            is ExprP.Call -> call(node.function, node.args)
            is ExprP.Index -> index(span, node.array, node.index)
            is ExprP.Index2 -> index2(span, node.array, node.index0, node.index1)
            is ExprP.Identifier<*, *> -> exprIdent(node.ident as CstIdent)
            is ExprP.Literal -> exprLiteral(node.literal)
            is ExprP.Op -> binOp(span, node.lhs, node.op, node.rhs)
            // These are not used in type expressions.
            is ExprP.Slice,
            is ExprP.Lambda<*, *>,
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
    fun exprSpanned(e: CstExpr): Spanned<GlobalValue> {
        val value = expr(e)
        return Spanned(span = e.span, node = value)
    }

    // fn load(&mut self, load: &LoadP<CstPayload>) -> Result<(), InternalError>
    fun load(loadStmt: LoadP) {
        for (arg in loadStmt.args) {
            val ty = loadStmt.payload[arg.their] ?: Ty.any()
            assignIdentValue(arg.local, GlobalValue.ty(ty))
        }
    }

    // fn resolve_assign_ident_to_module_slot_id(&self, ident: &CstAssignIdent) -> Result<ModuleSlotId, InternalError>
    fun resolveAssignIdentToModuleSlotId(ident: CstAssignIdent): ModuleSlotId {
        val bindingId = ident.resolvedBindingId(ctx.codemap)
        val binding = moduleScopeData.getBinding(bindingId)
        val resolvedSlot = binding.resolvedSlot(ctx.codemap)
        return when (resolvedSlot) {
            is Slot.Module -> resolvedSlot.slotId
            is Slot.Local -> throw internalError(ident.span, "local slot")
            else -> throw IllegalStateException("Unexpected slot: $resolvedSlot")
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
    fun assignValue(lhs: AssignTarget, rhs: GlobalValue) {
        when (lhs) {
            is AssignTargetP.Tuple -> {
                for (x in lhs.elements) {
                    assignUnset(x)
                }
            }
            is AssignTargetP.Index -> { /* noop */ }
            is AssignTargetP.Dot -> { /* noop */ }
            is AssignTargetP.Identifier -> assignIdentValue(lhs.ident, rhs)
        }
    }

    // fn assign(&mut self, lhs: &AssignTargetP<CstPayload>, rhs: &CstExpr) -> Result<(), InternalError>
    fun assign(lhs: AssignTarget, rhs: CstExpr) {
        val rhsValue = expr(rhs)
        assignValue(lhs, rhsValue)
    }

    /// Unset the variables.
    ///
    /// When evaluating code like:
    ///
    /// ```python
    /// if x:
    ///   a = list
    /// else:
    ///   b = int
    /// ```
    ///
    /// We don't know what branch is taken. So we just unset both `a` and `b`.
    // fn assign_unset(&mut self, lhs: &AssignTargetP<CstPayload>) -> Result<(), InternalError>
    fun assignUnset(lhs: AssignTarget) {
        when (lhs) {
            is AssignTargetP.Tuple -> {
                for (x in lhs.elements) {
                    assignUnset(x)
                }
            }
            is AssignTargetP.Index -> { /* noop */ }
            is AssignTargetP.Dot -> { /* noop */ }
            is AssignTargetP.Identifier -> assignUnsetIdent(lhs.ident)
        }
    }

    // fn assign_stmt(&mut self, assign: &AssignP<CstPayload>) -> Result<(), InternalError>
    fun assignStmt(assignStmt: AssignP) {
        // match ty { None => assign, Some(_ty) => assign }
        assign(assignStmt.lhs, assignStmt.rhs)
    }

    // fn for_stmt_unset(&mut self, for_stmt: &ForP<CstPayload>) -> Result<(), InternalError>
    fun forStmtUnset(forStmt: ForP) {
        assignUnset(forStmt.variable)
        evalStmtUnset(forStmt.body)
    }

    /// When we are not sure if code is executed exactly once (like in a for loop body),
    /// we just reset all the variables.
    // fn eval_stmt_unset(&mut self, stmt: &CstStmt) -> Result<(), InternalError>
    fun evalStmtUnset(stmt: CstStmt) {
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
            is StmtP.Def -> assignUnsetIdent(node.def.name)
            is StmtP.Load -> throw internalError(stmt.span, "load")
        }
    }

    // fn top_level_def(&mut self, def: &DefP<CstPayload>) -> Result<(), InternalError>
    fun topLevelDef(def: DefP) {
        val defParams = DefParams.unpack(def.params, ctx.codemap)

        val posOnly = mutableListOf<Pair<ParamIsRequired, Ty>>()
        val posOrName = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var args: Ty? = null
        val nameOnly = mutableListOf<Triple<String, ParamIsRequired, Ty>>()
        var kwargs: Ty? = null

        for (param in defParams.params) {
            val ty = getTyExprOpt(param.ty)
            when (val kind = param.kind) {
                is DefParamKind.Regular -> {
                    val name = param.ident.ident
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
    fun evalStmt(stmt: CstStmt) {
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
            is StmtP.Def -> topLevelDef(node.def)
            is StmtP.Load -> load(node.loadStmt)
        }
    }

    // fn unknown_ty(&mut self, span: Span) -> Ty
    fun unknownTy(span: Span): Ty {
        approximations.add(Approximation.new("Unknown type", span))
        return Ty.any()
    }

    // fn eval_path(&mut self, path: &TypePathP<CstPayload>) -> Result<Option<Value>, InternalError>
    fun evalPath(path: TypePathP): Any? {
        var value = exprIdent(path.first).value ?: return null
        for (x in path.rem) {
            try {
                value = getAttrError(value, x.value, heap)
            } catch (e: Exception) {
                val span = path.first.span.merge(x.span)
                errors.add(TypingError.new(e, span, ctx.codemap))
                return null
            }
        }
        return value
    }

    // fn try_proper_ty(&mut self, path: &TypePathP<CstPayload>) -> Result<Option<Ty>, InternalError>
    fun tryProperTy(path: TypePathP): Ty? {
        val value = evalPath(path) ?: return null
        return try {
            val ty = TypeCompiled.new(value, heap)
            ty.asTy()
        } catch (e: Exception) {
            val span = Span.mergeAll(
                listOf(path.first.span) + path.rem.map { it.span }
            )
            errors.add(TypingError.newAnyhow(e, span, ctx.codemap))
            null
        }
    }

    // fn path_ty(&mut self, path: &TypePathP<CstPayload>) -> Result<Ty, InternalError>
    fun pathTy(path: TypePathP): Ty {
        tryProperTy(path)?.let { return it }
        val span = Span.mergeAll(
            listOf(path.first.span) + path.rem.map { it.span }
        )
        return unknownTy(span)
    }

    // fn from_type_expr_impl(&mut self, x: &Spanned<TypeExprUnpackP<CstPayload>>) -> Result<Ty, InternalError>
    fun fromTypeExprImpl(x: Spanned<TypeExprUnpack>): Ty {
        return when (val node = x.node) {
            is TypeExprUnpack.Ellipsis -> {
                approximations.add(Approximation.new("Ellipsis cannot be used as type", x))
                Ty.any()
            }
            is TypeExprUnpack.ListLiteral -> {
                approximations.add(Approximation.new("List literal [...] cannot be used as type", x))
                Ty.any()
            }
            is TypeExprUnpack.Tuple -> {
                Ty.tuple(node.elements.map { fromTypeExprImpl(it) })
            }
            is TypeExprUnpack.Union -> {
                Ty.unions(node.elements.map { fromTypeExprImpl(it) })
            }
            is TypeExprUnpack.Path -> pathTy(node.path)
            is TypeExprUnpack.Index -> {
                val aValue = exprIdent(node.base).value
                if (aValue == null) {
                    approximations.add(Approximation.new("Not global", x))
                    Ty.any()
                } else if (!ptrEq(aValue, Constants.get().fnList)) {
                    approximations.add(Approximation.new("Not list", x))
                    Ty.any()
                } else {
                    val iTy = fromTypeExprImpl(node.index)
                    val iCompiled = TypeCompiled.fromTy(iTy, heap)
                    try {
                        val t = at(aValue, iCompiled.toInner(), heap)
                        val ty = TypeCompiled.new(t, heap)
                        ty.asTy()
                    } catch (_: Exception) {
                        approximations.add(Approximation.new("Getitem failed or TypeCompiled::new failed", x))
                        Ty.any()
                    }
                }
            }
            is TypeExprUnpack.Index2 -> {
                val aValue = evalPath(node.base)
                if (aValue == null) {
                    approximations.add(Approximation.new("Not global", x))
                    Ty.any()
                } else if (ptrEq(aValue, Constants.get().fnDict)) {
                    val i0Ty = fromTypeExprImpl(node.index0)
                    val i1Ty = fromTypeExprImpl(node.index1)
                    val i0Compiled = TypeCompiled.fromTy(i0Ty, heap)
                    val i1Compiled = TypeCompiled.fromTy(i1Ty, heap)
                    try {
                        val t = at2(aValue, i0Compiled.toInner(), i1Compiled.toInner(), heap)
                        val ty = TypeCompiled.new(t, heap)
                        ty.asTy()
                    } catch (_: Exception) {
                        approximations.add(Approximation.new("Getitem2 failed or TypeCompiled::new failed", x))
                        Ty.any()
                    }
                } else if (ptrEq(aValue, Constants.get().fnTuple)) {
                    val i0Ty = fromTypeExprImpl(node.index0)
                    if (node.index1.node !is TypeExprUnpack.Ellipsis) {
                        approximations.add(Approximation.new("Expecting ellipsis in tuple[x, ...]", x))
                        return Ty.any()
                    }
                    val r0 = TypeCompiled.fromTy(i0Ty, heap)
                    try {
                        val t = at2(aValue, r0.toInner(), Ellipsis.newValue(), heap)
                        val ty = TypeCompiled.new(t, heap)
                        ty.asTy()
                    } catch (_: Exception) {
                        approximations.add(Approximation.new("Getitem2 failed or TypeCompiled::new failed", x))
                        Ty.any()
                    }
                } else if (ptrEq(aValue, Constants.get().typingCallable)) {
                    val i0Node = node.index0.node
                    if (i0Node !is TypeExprUnpack.ListLiteral) {
                        approximations.add(Approximation.new("Expecting list in Callable[[...], ...]", x))
                        return Ty.any()
                    }
                    val argValues = i0Node.elements.map {
                        TypeCompiled.fromTy(fromTypeExprImpl(it), heap).toInner()
                    }
                    val argsList = heap.allocList(argValues)
                    val retTy = fromTypeExprImpl(node.index1)
                    val ret = TypeCompiled.fromTy(retTy, heap).toInner()
                    try {
                        val t = at2(aValue, argsList, ret, heap)
                        val ty = TypeCompiled.new(t, heap)
                        ty.asTy()
                    } catch (_: Exception) {
                        approximations.add(Approximation.new("Getitem2 failed or TypeCompiled::new failed", x))
                        Ty.any()
                    }
                } else {
                    approximations.add(Approximation.new("Not dict or tuple", x))
                    Ty.any()
                }
            }
            else -> {
                approximations.add(Approximation.new("Unexpected type expression", x))
                Ty.any()
            }
        }
    }

    // fn ty_expr(&mut self, expr: &CstTypeExpr) -> Result<Ty, InternalError>
    fun tyExpr(expr: CstTypeExpr): Ty {
        val x = TypeExprUnpackP.unpack(expr.expr, ctx.codemap)
        return fromTypeExprImpl(x)
    }

    // fn get_ty_expr(&self, expr: &CstTypeExpr) -> Result<Ty, InternalError>
    fun getTyExpr(expr: CstTypeExpr): Ty {
        return expr.payload.typecheckerTy
            ?: throw internalError(expr.span, "type not set")
    }

    // fn get_ty_expr_opt(&mut self, expr: Option<&CstTypeExpr>) -> Result<Ty, InternalError>
    fun getTyExprOpt(expr: CstTypeExpr?): Ty {
        return if (expr == null) Ty.any() else getTyExpr(expr)
    }

    // fn fill_types(&mut self, stmt: &mut CstStmt) -> Result<(), InternalError>
    fun fillTypes(stmt: CstStmt) {
        stmt.visitTypeExprMut { typeExpr ->
            if (typeExpr.payload.typecheckerTy != null) {
                throw internalError(typeExpr.span, "type already set")
            }
            typeExpr.payload.typecheckerTy = tyExpr(typeExpr)
        }
    }

    // fn top_level_stmt(&mut self, stmt: &mut CstStmt) -> Result<(), InternalError>
    fun topLevelStmt(stmt: CstStmt) {
        // Fill all type payloads.
        fillTypes(stmt)
        // Partially evaluate expressions which can be used in the following type expressions.
        evalStmt(stmt)
    }
}

/// Types of module-level variables.
// #[derive(Default)]
// pub(crate) struct ModuleVarTypes { pub(crate) types: UnorderedMap<ModuleSlotId, Ty> }
internal class ModuleVarTypes(
    val types: MutableMap<ModuleSlotId, Ty> = mutableMapOf(),
)

/// Populate `TypeExprP` type payload when running lint typechecker.
/// (Compiler typechecked populates the payload after proper full evaluation.)
// pub(crate) fn fill_types_for_lint_typechecker(...)
internal fun fillTypesForLintTypechecker(
    module: List<CstStmt>,
    ctx: TypingOracleCtx,
    moduleScopeData: ModuleScopeData,
    approximations: MutableList<Approximation>,
): Pair<List<TypingError>, ModuleVarTypes> {
    val heap = Heap()
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
    return Pair(builder.errors, ModuleVarTypes(types))
}

// Stub types removed - real types should be imported from their respective packages
