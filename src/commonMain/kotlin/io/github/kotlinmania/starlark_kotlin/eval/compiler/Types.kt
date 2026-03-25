// port-lint: source src/eval/compiler/types.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

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

import io.github.kotlinmania.starlark_kotlin.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypePathP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.TypeExprUnpackP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Slot
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.typing.error.EvalException
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.CstPayload
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstStmt
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstIdent
import io.github.kotlinmania.starlark_kotlin.values.types.list.List
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.string.moduleEnv
import io.github.kotlinmania.starlark_kotlin.values.types.list.ptrEq
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Union
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Path
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Index2
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.analysis.Tuple
import io.github.kotlinmania.starlark_kotlin.analysis.Index
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.typeAnyOf
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.toInner
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.fromTy
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.asTy
import io.github.kotlinmania.starlark_kotlin.values.types.list_or_tuple.items
import io.github.kotlinmania.starlark_kotlin.values.types.array.rem
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocList
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.internalError
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.i0
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.getAttrError
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Union
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.TypePathP
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.TypeExprUnpackP
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Slot
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Path
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.Index2
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.tests.xs
import io.github.kotlinmania.starlark_kotlin.tests.opt.i1
import io.github.kotlinmania.starlark_kotlin.tests.frozenHeap
import io.github.kotlinmania.starlark_kotlin.tests.derive.i
import io.github.kotlinmania.starlark_kotlin.tests.a
import io.github.kotlinmania.starlark_kotlin.analysis.path
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.values.types.record.record_type.id
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span

// #[derive(Debug, thiserror::Error)]
// enum TypesError
private sealed class TypesError(message: String) : Exception(message) {
    // #[error("Identifier is not resolved (internal error)")]
    class UnresolvedIdentifier : TypesError("Identifier is not resolved (internal error)")

    // #[error("Identifier is resolve as local variable (internal error)")]
    class LocalIdentifier : TypesError("Identifier is resolve as local variable (internal error)")

    // #[error("Module variable is not set: `{0}`")]
    class ModuleVariableNotSet(val name: String) : TypesError("Module variable is not set: `$name`")

    // #[error("Type payload not set (internal error)")]
    class TypePayloadNotSet : TypesError("Type payload not set (internal error)")

    // #[error("[] can only be applied to list function in type expression")]
    class TypeIndexOnNonList : TypesError("[] can only be applied to list function in type expression")

    // #[error("[,] can only be applied to dict or tuple functions in type expression")]
    class TypeIndexOnNonDictOrTuple : TypesError("[,] can only be applied to dict or tuple functions in type expression")
}

// impl<'v> Compiler<'v, '_, '_, '_>
// Extension functions on Compiler for type expression evaluation.

/// Compile expression when it is expected to be interpreted as type.
// pub(crate) fn expr_for_type(
//     &mut self,
//     expr: Option<&CstTypeExpr>,
// ) -> Option<IrSpanned<TypeCompiled<FrozenValue>>>
internal fun Compiler.exprForType(
    expr: CstTypeExpr?,
): IrSpanned<TypeCompiled<FrozenValue>>? {
    if (!checkTypes) {
        return null
    }
    if (expr == null) return null
    val span = FrameSpan.new(FrozenFileSpan.new(codemap, expr.span))
    val ty = expr.Payload.compilerTy
    if (ty == null) {
        // This is unreachable. But unfortunately we do not return error here.
        // Still make an error in panic to produce nice panic message.
        error(
            EvalException.newAnyhow(
                TypesError.TypePayloadNotSet(),
                expr.span,
                codemap,
            ).toString()
        )
    }
    val typeValue = TypeCompiled.fromTy(ty, eval.heap())
    if (typeValue.isRuntimeWildcard()) {
        return null
    }
    val frozenTypeValue = typeValue.toFrozen(eval.frozenHeap())
    return IrSpanned(
        span = span,
        node = frozenTypeValue,
    )
}

/// We evaluated type expression to `Value`, now convert it to `FrozenValue`.
// fn alloc_value_for_type(
//     &mut self,
//     value: Value<'v>,
//     span: Span,
// ) -> Result<TypeCompiled<Value<'v>>, EvalException>
private fun Compiler.allocValueForType(
    value: Value,
    span: Span,
): TypeCompiled<Value> {
    return try {
        TypeCompiled.new(value, eval.heap())
    } catch (e: Exception) {
        throw EvalException.newAnyhow(e, span, codemap)
    }
}

// fn eval_ident_in_type_expr(&mut self, ident: &CstIdent) -> Result<Value<'v>, EvalException>
private fun Compiler.evalIdentInTypeExpr(ident: CstIdent): Value {
    val identPayload = ident.node.payload
        ?: throw EvalException.newAnyhow(
            TypesError.UnresolvedIdentifier(),
            ident.span,
            codemap,
        )
    return when (identPayload) {
        is ResolvedIdent.Slot -> {
            when (val slot = identPayload.slot) {
                is Slot.Local -> throw EvalException.newAnyhow(
                    TypesError.LocalIdentifier(),
                    ident.span,
                    codemap,
                )
                is Slot.Module -> {
                    eval.moduleEnv.slots().getSlot(slot.id)
                        ?: throw EvalException.newAnyhow(
                            TypesError.ModuleVariableNotSet(ident.node.ident),
                            ident.span,
                            codemap,
                        )
                }
            }
        }
        is ResolvedIdent.Global -> identPayload.value.toValue()
    }
}

/// We may use non-frozen values as types, so we don't reuse `expr_ident` function
/// which is used in normal compilation.
// fn eval_path(&mut self, path: TypePathP<CstPayload>) -> Result<Value<'v>, EvalException>
private fun Compiler.evalPath(path: TypePathP<CstPayload>): Value {
    var value = evalIdentInTypeExpr(path.first)
    for (step in path.rem) {
        value = try {
            value.getAttrError(step.node, eval.heap())
        } catch (e: Exception) {
            throw EvalException.new(e, step.span, codemap)
        }
    }
    return value
}

// fn eval_expr_as_type(
//     &mut self,
//     expr: Spanned<TypeExprUnpackP<CstPayload>>,
// ) -> Result<TypeCompiled<Value<'v>>, EvalException>
private fun Compiler.evalExprAsType(
    expr: Spanned<TypeExprUnpackP<CstPayload>>,
): TypeCompiled<Value> {
    val span = expr.span
    val value = evalExpr(expr)
    return allocValueForType(value, span)
}

/// Evaluate expression in context of typechecker.
/// It is very restricted in what it can do.
// fn eval_expr(
//     &mut self,
//     expr: Spanned<TypeExprUnpackP<CstPayload>>,
// ) -> Result<Value<'v>, EvalException>
private fun Compiler.evalExpr(
    expr: Spanned<TypeExprUnpackP<CstPayload>>,
): Value {
    return when (val node = expr.node) {
        is TypeExprUnpackP.Ellipsis -> Ellipsis.newValue().toValue()
        is TypeExprUnpackP.List -> {
            val values = node.items.map { item -> evalExpr(item) }
            eval.heap().allocList(values)
        }
        is TypeExprUnpackP.Path -> evalPath(node.path)
        is TypeExprUnpackP.Index -> {
            val a = evalIdentInTypeExpr(node.a)
            if (!a.ptrEq(Constants.get().fnList.first.toValue())
                && !a.ptrEq(Constants.get().fnSet.first.toValue())
            ) {
                throw EvalException.newAnyhow(
                    TypesError.TypeIndexOnNonList(),
                    expr.span,
                    codemap,
                )
            }
            val i = evalExprAsType(node.i)
            try {
                a.getRef().at(i.toInner(), eval.heap())
            } catch (e: Exception) {
                throw EvalException.new(e, expr.span, codemap)
            }
        }
        is TypeExprUnpackP.Index2 -> {
            val a = evalPath(node.a.node)
            if (a.ptrEq(Constants.get().fnDict.first.toValue())
                || a.ptrEq(Constants.get().fnTuple.first.toValue())
                || a.ptrEq(Constants.get().typingCallable.first.toValue())
            ) {
                val i0 = evalExpr(node.i0)
                val i1 = evalExpr(node.i1)
                try {
                    a.getRef().at2(i0, i1, eval.heap())
                } catch (e: Exception) {
                    throw EvalException.new(e, expr.span, codemap)
                }
            } else {
                throw EvalException.newAnyhow(
                    TypesError.TypeIndexOnNonDictOrTuple(),
                    expr.span,
                    codemap,
                )
            }
        }
        is TypeExprUnpackP.Union -> {
            val xs = node.xs.map { x -> evalExprAsType(x) }
            TypeCompiled.typeAnyOf(xs, eval.heap()).toInner()
        }
        is TypeExprUnpackP.Tuple -> {
            val xs = node.xs.map { x -> evalExprAsType(x).asTy().clone() }
            TypeCompiled.fromTy(Ty.tuple(xs), eval.heap()).toInner()
        }
    }
}

// fn populate_types_in_type_expr(
//     &mut self,
//     type_expr: &mut CstTypeExpr,
// ) -> Result<(), EvalException>
private fun Compiler.populateTypesInTypeExpr(
    typeExpr: CstTypeExpr,
) {
    if (typeExpr.Payload.compilerTy != null) {
        throw EvalException.new(
            internalError("Type already initialized"),
            typeExpr.span,
            codemap,
        )
    }
    // This should not fail because we validated it at parse time.
    val unpack = TypeExprUnpackP.unpack(typeExpr.Expr, codemap)
    val typeValue = evalExprAsType(unpack)
    typeExpr.Payload.compilerTy = typeValue.asTy().clone()
}

// pub(crate) fn populate_types_in_stmt(
//     &mut self,
//     stmt: &mut CstStmt,
// ) -> Result<(), EvalException>
internal fun Compiler.populateTypesInStmt(
    stmt: CstStmt,
) {
    stmt.visitTypeExprErrMut { typeExpr -> populateTypesInTypeExpr(typeExpr) }
}
