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

import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.Slot
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.eval.compiler.span.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frame_span.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.syntax.eval_exception.EvalException
import io.github.kotlinmania.starlark_kotlin.syntax.internalError
import io.github.kotlinmania.starlark_kotlin.syntax.syntax.type_expr.TypeExprUnpackP
import io.github.kotlinmania.starlark_kotlin.syntax.syntax.type_expr.TypePathP
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.compiled.TypeCompiled

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
    val ty = expr.payload.compilerTy
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
    if (typeExpr.payload.compilerTy != null) {
        throw EvalException.new(
            internalError("Type already initialized"),
            typeExpr.span,
            codemap,
        )
    }
    // This should not fail because we validated it at parse time.
    val unpack = TypeExprUnpackP.unpack(typeExpr.expr, codemap)
    val typeValue = evalExprAsType(unpack)
    typeExpr.payload.compilerTy = typeValue.asTy().clone()
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
