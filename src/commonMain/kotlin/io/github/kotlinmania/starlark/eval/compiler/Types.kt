// port-lint: source src/eval/compiler/types.rs
package io.github.kotlinmania.starlark.eval.compiler

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

import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.eval.compiler.constants.Constants
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdentPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark.eval.compiler.scope.CstTypeExpr
import io.github.kotlinmania.starlark.eval.compiler.scope.cstPayload
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.frozenfilespan.FrozenFileSpan
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.AstTypeExprP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.typeexpr.TypeExprUnpackP
import io.github.kotlinmania.starlark.syntax.typeexpr.TypePathP
import io.github.kotlinmania.starlark.typing.EvalException
import io.github.kotlinmania.starlark.typing.StarlarkError
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark.values.types.list.allocList
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled

private sealed class TypesError(
    message: String,
) : Exception(message) {
    class UnresolvedIdentifier : TypesError("Identifier is not resolved (internal error)")

    class LocalIdentifier : TypesError("Identifier is resolve as local variable (internal error)")

    class ModuleVariableNotSet(
        val name: String,
    ) : TypesError("Module variable is not set: `$name`")

    class TypePayloadNotSet : TypesError("Type payload not set (internal error)")

    class TypeIndexOnNonList : TypesError("[] can only be applied to list function in type expression")

    class TypeIndexOnNonDictOrTuple : TypesError("[,] can only be applied to dict or tuple functions in type expression")
}

// Extension functions on Compiler for type expression evaluation.

/** Compile expression when it is expected to be interpreted as type. */
internal fun Compiler.exprForType(
    expr: CstTypeExpr?,
): IrSpanned<TypeCompiled>? {
    if (!checkTypes) {
        return null
    }
    if (expr == null) return null
    val span = FrameSpan.new(FrozenFileSpan.new(codemap, expr.span))

    val ty = expr.cstPayload.compilerTy
    if (ty == null) {
        // This is unreachable. But unfortunately we do not return error here.
        // Still make an error in panic to produce nice panic message.
        error(
            EvalException
                .newAnyhow(
                    TypesError.TypePayloadNotSet(),
                    expr.span,
                    codemap.value,
                ).toString(),
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

/** We evaluated type expression to `Value`, now convert it to `FrozenValue`. */
private fun Compiler.allocValueForType(
    value: Value,
    span: Span,
): TypeCompiled =
    try {
        TypeCompiled.new(value, eval.heap())
    } catch (e: Exception) {
        throw EvalException.newAnyhow(e, span, codemap.value)
    }

private fun Compiler.evalIdentInTypeExpr(ident: CstIdent): Value {
    val identPayload =
        ident.node.payload
            ?: throw EvalException.newAnyhow(
                TypesError.UnresolvedIdentifier(),
                ident.span,
                codemap.value,
            )
    return when (identPayload) {
        is ResolvedIdent.Slot -> {
            when (val slot = identPayload.slot) {
                is Slot.Local -> throw EvalException.newAnyhow(
                    TypesError.LocalIdentifier(),
                    ident.span,
                    codemap.value,
                )
                is Slot.Module -> {
                    eval.moduleEnv.slots().getSlot(slot.id)
                        ?: throw EvalException.newAnyhow(
                            TypesError.ModuleVariableNotSet(ident.node.ident),
                            ident.span,
                            codemap.value,
                        )
                }
            }
        }
        is ResolvedIdent.Global -> identPayload.value.toValue()
    }
}

/**
 * We may use non-frozen values as types, so we don't reuse `expr_ident` function
 * which is used in normal compilation.
 */
private fun Compiler.evalPath(path: TypePathP<CstPayload, CstIdentPayload>): Value {
    var value = evalIdentInTypeExpr(path.first)
    for (step in path.rem) {
        value =
            value.getAttrError(step.node, eval.heap()).getOrElse { e ->
                throw EvalException.newAnyhow(e, step.span, codemap.value)
            }
    }
    return value
}

private fun Compiler.evalExprAsType(
    expr: Spanned<TypeExprUnpackP<CstPayload, CstIdentPayload>>,
): TypeCompiled {
    val span = expr.span
    val value = evalExpr(expr)
    return allocValueForType(value, span)
}

/**
 * Evaluate expression in context of typechecker.
 * It is very restricted in what it can do.
 */

private fun Compiler.evalExpr(
    expr: Spanned<TypeExprUnpackP<CstPayload, CstIdentPayload>>,
): Value =
    when (val node = expr.node) {
        is TypeExprUnpackP.Ellipsis -> Ellipsis.newValue().toValue()
        is TypeExprUnpackP.List -> {
            val values = node.items.map { item -> evalExpr(item) }
            eval.heap().allocList(values)
        }
        is TypeExprUnpackP.Path -> evalPath(node.path)
        is TypeExprUnpackP.Index -> {
            val a = evalIdentInTypeExpr(node.ident)
            if (Constants.get().fnList?.let { a.ptrEq(it.value.toValue()) } != true &&
                Constants.get().fnSet?.let { a.ptrEq(it.value.toValue()) } != true
            ) {
                throw EvalException.newAnyhow(
                    TypesError.TypeIndexOnNonList(),
                    expr.span,
                    codemap.value,
                )
            }
            val i = evalExprAsType(node.index)
            a.getRef().at(i.toInner(), eval.heap()).getOrElse { e ->
                throw EvalException.newAnyhow(e, expr.span, codemap.value)
            }
        }
        is TypeExprUnpackP.Index2 -> {
            val a = evalPath(node.path.node)
            if (Constants.get().fnDict?.let { a.ptrEq(it.value.toValue()) } == true ||
                Constants.get().fnTuple?.let { a.ptrEq(it.value.toValue()) } == true ||
                Constants.get().typingCallable?.let { a.ptrEq(it.value.toValue()) } == true
            ) {
                val i0 = evalExpr(node.i0)
                val i1 = evalExpr(node.i1)
                a.getRef().at2(i0, i1, eval.heap()).getOrElse { e ->
                    throw EvalException.newAnyhow(e, expr.span, codemap.value)
                }
            } else {
                throw EvalException.newAnyhow(
                    TypesError.TypeIndexOnNonDictOrTuple(),
                    expr.span,
                    codemap.value,
                )
            }
        }
        is TypeExprUnpackP.Union -> {
            val xs = node.xs.map { x -> evalExprAsType(x) }
            TypeCompiled.typeAnyOf(xs, eval.heap()).toInner()
        }
        is TypeExprUnpackP.Tuple -> {
            val xs = node.xs.map { x -> evalExprAsType(x).asTy() }
            TypeCompiled.fromTy(Ty.tuple(xs), eval.heap()).toInner()
        }
    }

private fun Compiler.populateTypesInTypeExpr(
    typeExpr: CstTypeExpr,
) {
    val payload = typeExpr.cstPayload
    if (payload.compilerTy != null) {
        throw EvalException.newAnyhow(
            StarlarkError("Type already initialized"),
            typeExpr.span,
            codemap.value,
        )
    }
    // This should not fail because we validated it at parse time.
    val unpack = TypeExprUnpackP.unpack<CstPayload, CstIdentPayload>(typeExpr.node.expr, codemap.value)
    val typeValue = evalExprAsType(unpack)
    payload.compilerTy = typeValue.asTy()
}

internal fun Compiler.populateTypesInStmt(
    stmt: CstStmt,
) {
    stmt.node.visitTypeExprErrMut { typeExpr ->
        populateTypesInTypeExpr(typeExpr)
    }
}
