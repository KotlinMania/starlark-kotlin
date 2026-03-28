// port-lint: source src/typing/ctx.rs
package io.github.kotlinmania.starlark_kotlin.typing

import io.github.kotlinmania.starlark_kotlin.values.typing.Approximation
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstIdent
import io.github.kotlinmania.starlark_kotlin.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignOp
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingUnOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.BinOp
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CallArgsP

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

// Rust-style Result type for typing operations (avoids conflict with kotlin.Result)
sealed class TypingResult<out T, out E> {
    class Ok<T>(val value: T) : TypingResult<T, Nothing>()
    class Err<E>(val error: E) : TypingResult<Nothing, E>()
}

// --- TypingContext ---

internal class TypingContext(
    val oracle: TypingOracleCtx,
    // We'd prefer this to be mutable directly,
    // but that makes writing the code more fiddly, so just use a mutable list
    val errors: MutableList<TypingError> = mutableListOf(),
    val approximations: MutableList<Approximation> = mutableListOf(),
    val types: MutableMap<BindingId, Ty> = mutableMapOf(),
    val moduleVarTypes: ModuleVarTypes,
) {
    fun approximation(category: String, message: Any): Ty {
        approximations.add(Approximation.new(category, message))
        return Ty.any()
    }

    private fun resultToTy(result: TypingResult<Ty, TypingError>): Ty {
        return when (result) {
            is TypingResult.Ok -> result.value
            is TypingResult.Err -> {
                errors.add(result.error)
                Ty.never()
            }
        }
    }

    private fun resultToTyWithInternalError(
        result: TypingResult<Ty, TypingOrInternalError>,
    ): TypingResult<Ty, InternalError> {
        return when (result) {
            is TypingResult.Ok -> TypingResult.Ok(result.value)
            is TypingResult.Err -> when (val e = result.error) {
                is TypingOrInternalError.Internal -> TypingResult.Err(e.error)
                is TypingOrInternalError.Typing -> {
                    errors.add(e.error)
                    TypingResult.Ok(Ty.never())
                }
            }
        }
    }

    private fun validateCall(fun_: Ty, args: TyCallArgs, span: Span): TypingResult<Ty, InternalError> {
        return resultToTyWithInternalError(oracle.validateCall(span, fun_, args))
    }

    private fun fromIterated(ty: Ty, span: Span): Ty {
        return resultToTy(oracle.iterItem(Spanned(span, ty)))
    }

    fun validateType(
        got: Spanned<Ty>,
        require: Ty,
    ): TypingResult<Unit, InternalError> {
        val result = oracle.validateType(got, require)
        if (result is TypingResult.Err) {
            when (val e = result.error) {
                is TypingOrInternalError.Typing -> {
                    errors.add(e.error)
                }
                is TypingOrInternalError.Internal -> {
                    return TypingResult.Err(e.error)
                }
            }
        }
        return TypingResult.Ok(Unit)
    }

    private fun exprDot(ty: Ty, attr: String, span: Span): Ty {
        return resultToTy(oracle.exprDot(span, ty, attr))
    }

    private fun exprIndex(
        span: Span,
        array: CstExpr,
        index: CstExpr,
    ): TypingResult<Ty, InternalError> {
        val arrayTy = when (val r = expressionType(array)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }

        // Hack for `list[str]`: list of `list` is just "function", and we don't want
        // to make it custom type and have overly complex machinery for handling it.
        // So we just special case it here.
        if (arrayTy.isFunction()) {
            if (array.node is ExprP.Identifier) {
                val v0 = (array.node as ExprP.Identifier).ident
                if (v0.node.ident == "list") {
                    return TypingResult.Ok(Ty.any())
                }
            }
        }

        val indexSpanned = when (val r = expressionTypeSpanned(index)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }
        return resultToTyWithInternalError(oracle.exprIndex(span, arrayTy, indexSpanned))
    }

    private fun expressionUnOp(
        span: Span,
        arg: CstExpr,
        unOp: TypingUnOp,
    ): TypingResult<Ty, InternalError> {
        val ty = when (val r = expressionType(arg)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }
        return TypingResult.Ok(resultToTy(oracle.exprUnOp(span, ty, unOp)))
    }

    fun expressionBindType(x: BindExpr): TypingResult<Ty, InternalError> {
        return when (x) {
            is BindExpr.Expr -> expressionType(x.expr)
            is BindExpr.GetIndex -> {
                val inner = when (val r = expressionBindType(x.inner)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                TypingResult.Ok(oracle.indexed(inner, x.index))
            }
            is BindExpr.Iter -> {
                val inner = when (val r = expressionBindType(x.inner)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                TypingResult.Ok(fromIterated(inner, x.span()))
            }
            is BindExpr.AssignModify -> {
                val span = x.lhs.span
                val rhs = when (val r = expressionTypeSpanned(x.rhs)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val lhs = when (val r = expressionAssignSpanned(x.lhs)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val attr = when (x.op) {
                    AssignOp.Add -> TypingBinOp.Add
                    AssignOp.Subtract -> TypingBinOp.Sub
                    AssignOp.Multiply -> TypingBinOp.Mul
                    AssignOp.Divide -> TypingBinOp.Div
                    AssignOp.FloorDivide -> TypingBinOp.FloorDiv
                    AssignOp.Percent -> TypingBinOp.Percent
                    AssignOp.BitAnd -> TypingBinOp.BitAnd
                    AssignOp.BitOr -> TypingBinOp.BitOr
                    AssignOp.BitXor -> TypingBinOp.BitXor
                    AssignOp.LeftShift -> TypingBinOp.LeftShift
                    AssignOp.RightShift -> TypingBinOp.RightShift
                    else -> throw IllegalStateException("Unexpected assign op: ${x.op}")
                }
                resultToTyWithInternalError(
                    oracle.exprBinOpTy(span, lhs, attr, rhs)
                )
            }
            is BindExpr.SetIndex -> {
                val index = when (val r = expressionTypeSpanned(x.index)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val e = when (val r = expressionBindType(x.expr)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val res = mutableListOf<Ty>()
                // We know about list and dict, everything else we just ignore
                val idTy = types[x.id] ?: Ty.any()
                if (idTy.isList()) {
                    // If we know it MUST be a list, then the index must be an int
                    when (val vr = validateType(index.asRef(), Ty.int())) {
                        is TypingResult.Err -> return TypingResult.Err(vr.error)
                        is TypingResult.Ok -> {}
                    }
                }
                for (ty in idTy.iterUnion()) {
                    when (ty) {
                        is TyBasic.List -> {
                            res.add(Ty.list(e))
                        }
                        is TyBasic.Dict -> {
                            res.add(Ty.dict(index.node, e))
                        }
                        else -> {
                            // Either it's not something we can apply this to, in which case do nothing.
                            // Or it's an Any, in which case we aren't going to change its type or spot errors.
                        }
                    }
                }
                TypingResult.Ok(Ty.unions(res))
            }
            is BindExpr.ListAppend -> {
                val probablyList = when (val r = oracle.probablyAList(types[x.id] ?: Ty.any())) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    else -> throw IllegalStateException("Unexpected result: $r")
                }
                if (probablyList) {
                    val elemTy = when (val r = expressionType(x.expr)) {
                        is TypingResult.Ok -> r.value
                        is TypingResult.Err -> return TypingResult.Err(r.error)
                        else -> throw IllegalStateException("Unexpected result: $r")
                    }
                    TypingResult.Ok(Ty.list(elemTy))
                } else {
                    // It doesn't seem to be a list, so let's assume the append is non-mutating
                    TypingResult.Ok(Ty.never())
                }
            }
            is BindExpr.ListExtend -> {
                val probablyList = when (val r = oracle.probablyAList(types[x.id] ?: Ty.any())) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    else -> throw IllegalStateException("Unexpected result: $r")
                }
                if (probablyList) {
                    val elemTy = when (val r = expressionType(x.expr)) {
                        is TypingResult.Ok -> r.value
                        is TypingResult.Err -> return TypingResult.Err(r.error)
                        else -> throw IllegalStateException("Unexpected result: $r")
                    }
                    TypingResult.Ok(Ty.list(fromIterated(elemTy, x.expr.span)))
                } else {
                    // It doesn't seem to be a list, so let's assume the extend is non-mutating
                    TypingResult.Ok(Ty.never())
                }
            }
        }
    }

    /// Used to get the type of an expression when used as part of a ModifyAssign operation
    private fun expressionAssign(x: CstAssignTarget): TypingResult<Ty, InternalError> {
        return when (val node = x.node) {
            is AssignTargetP.Tuple -> TypingResult.Ok(approximation("expression_assignment", x))
            is AssignTargetP.Index -> exprIndex(x.span, node.pair.first, node.pair.second)
            is AssignTargetP.Dot -> TypingResult.Ok(approximation("expression_assignment", x))
            is AssignTargetP.Identifier<*, *> -> {
                val payload = node.ident.payload
                if (payload != null) {
                    val ty = types[payload]
                    if (ty != null) {
                        return TypingResult.Ok(ty)
                    }
                }
                TypingResult.Err(InternalError.msg(
                    "Unknown identifier",
                    node.ident.span,
                    oracle.codemap,
                ))
            }
        }
    }

    private fun expressionAssignSpanned(x: CstAssignTarget): TypingResult<Spanned<Ty>, InternalError> {
        return when (val r = expressionAssign(x)) {
            is TypingResult.Ok -> TypingResult.Ok(Spanned(x.span, r.value))
            is TypingResult.Err -> TypingResult.Err(r.error)
        }
    }

    /// We don't need the type out of the clauses (it doesn't change the overall type),
    /// but it is important we see through to the nested expressions to raise errors
    private fun checkComprehension(
        for_: ForClauseP,
        clauses: List<ClauseP>,
    ): TypingResult<Unit, InternalError> {
        when (val r = expressionType(for_.over)) {
            is TypingResult.Err -> return TypingResult.Err(r.error)
            is TypingResult.Ok -> {}
        }
        for (clause in clauses) {
            when (clause) {
                is ClauseP.For -> when (val r = expressionType(clause.forClause.over)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
                is ClauseP.If -> when (val r = expressionType(clause.expr)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
            }
        }
        return TypingResult.Ok(Unit)
    }

    fun expressionTypeSpanned(
        x: CstExpr,
    ): TypingResult<Spanned<Ty>, InternalError> {
        return when (val r = expressionType(x)) {
            is TypingResult.Ok -> TypingResult.Ok(Spanned(x.span, r.value))
            is TypingResult.Err -> TypingResult.Err(r.error)
        }
    }

    private fun exprBinOp(
        span: Span,
        lhs: CstExpr,
        op: BinOp,
        rhs: CstExpr,
    ): TypingResult<Ty, InternalError> {
        val lhsSpanned = when (val r = expressionTypeSpanned(lhs)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }
        val rhsSpanned = when (val r = expressionTypeSpanned(rhs)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }
        return resultToTyWithInternalError(oracle.exprBinOp(span, lhsSpanned, op, rhsSpanned))
    }

    private fun exprCall(
        span: Span,
        f: CstExpr,
        args: CallArgsP,
    ): TypingResult<Ty, InternalError> {
        val unpackedArgs = try {
            CallArgsUnpack.unpack(args, oracle.codemap)
        } catch (e: Exception) {
            return TypingResult.Err(InternalError.fromEvalException(e))
        }

        val posTy = mutableListOf<Spanned<Ty>>()
        for (pos in unpackedArgs.pos) {
            val ty = when (val r = expressionType(pos.node.expr())) {
                is TypingResult.Ok -> r.value
                is TypingResult.Err -> return TypingResult.Err(r.error)
            }
            posTy.add(Spanned(pos.span, ty))
        }

        val namedTy = mutableListOf<Spanned<Pair<String, Ty>>>()
        for (named in unpackedArgs.named) {
            val name = named.name()
                ?: return TypingResult.Err(InternalError.msg(
                    "Named argument without name",
                    named.span,
                    oracle.codemap,
                ))
            val ty = when (val r = expressionType(named.node.expr())) {
                is TypingResult.Ok -> r.value
                is TypingResult.Err -> return TypingResult.Err(r.error)
            }
            namedTy.add(Spanned(named.span, name to ty))
        }

        val argsTy = if (unpackedArgs.star != null) {
            val ty = when (val r = expressionTypeSpanned(unpackedArgs.star.node.expr())) {
                is TypingResult.Ok -> r.value
                is TypingResult.Err -> return TypingResult.Err(r.error)
            }
            fromIterated(ty, unpackedArgs.star.span)
            ty
        } else {
            null
        }

        val kwargsTy = if (unpackedArgs.starStar != null) {
            val ty = when (val r = expressionTypeSpanned(unpackedArgs.starStar.node.expr())) {
                is TypingResult.Ok -> r.value
                is TypingResult.Err -> return TypingResult.Err(r.error)
            }
            when (val vr = validateType(ty.asRef(), Ty.dict(Ty.string(), Ty.any()))) {
                is TypingResult.Err -> return TypingResult.Err(vr.error)
                is TypingResult.Ok -> {}
            }
            ty
        } else {
            null
        }

        val callArgs = TyCallArgs(
            pos = posTy,
            named = namedTy,
            args = argsTy,
            kwargs = kwargsTy,
        )

        val fTy = when (val r = expressionType(f)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }
        // If we can't resolve the types of the arguments, we can't validate the call,
        // but we still know the type of the result since the args don't impact that
        return validateCall(fTy, callArgs, span)
    }

    private fun exprSlice(
        span: Span,
        x: CstExpr,
        start: CstExpr?,
        stop: CstExpr?,
        stride: CstExpr?,
    ): TypingResult<Ty, InternalError> {
        for (e in listOfNotNull(start, stop, stride)) {
            val spanned = when (val r = expressionTypeSpanned(e)) {
                is TypingResult.Ok -> r.value
                is TypingResult.Err -> return TypingResult.Err(r.error)
            }
            when (val vr = validateType(spanned.asRef(), Ty.int())) {
                is TypingResult.Err -> return TypingResult.Err(vr.error)
                is TypingResult.Ok -> {}
            }
        }
        val xTy = when (val r = expressionType(x)) {
            is TypingResult.Ok -> r.value
            is TypingResult.Err -> return TypingResult.Err(r.error)
        }
        return TypingResult.Ok(resultToTy(oracle.exprSlice(span, xTy)))
    }

    private fun exprIdent(x: CstIdent): Ty {
        return when (val resolved = x.node.payload) {
            is ResolvedIdent.Slot -> when (val slot = resolved.slot) {
                is SlotKind.Module -> moduleVarTypes
                    .types[slot.id]
                    ?: Ty.any()
                is SlotKind.Other -> {
                    val ty = types[resolved.bindingId]
                    if (ty != null) {
                        ty
                    } else {
                        // All types must be resolved to this point,
                        // this code is unreachable.
                        Ty.any()
                    }
                }
                else -> Ty.any()
            }
            is ResolvedIdent.Global -> Ty.ofValue(resolved.global.toValue())
            null -> {
                // All identifiers must be resolved at this point,
                // but we don't stop after scope resolution error,
                // so this code is reachable.
                Ty.any()
            }
            else -> Ty.any()
        }
    }

    fun expressionType(x: CstExpr): TypingResult<Ty, InternalError> {
        val span = x.span
        return when (val node = x.node) {
            is ExprP.Tuple -> {
                val elems = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = when (val r = expressionType(elem)) {
                        is TypingResult.Ok -> r.value
                        is TypingResult.Err -> return TypingResult.Err(r.error)
                    }
                    elems.add(ty)
                }
                TypingResult.Ok(Ty.tuple(elems))
            }
            is ExprP.Dot -> {
                val aTy = when (val r = expressionType(node.expr)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                TypingResult.Ok(exprDot(aTy, node.attr.value, node.attr.span))
            }
            is ExprP.Call -> exprCall(span, node.func, node.args)
            is ExprP.Index -> exprIndex(span, node.pair.first, node.pair.second)
            is ExprP.Index2 -> {
                val (a, i0, i1) = node.triple
                when (val r = expressionType(a)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
                when (val r = expressionType(i0)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
                when (val r = expressionType(i1)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
                TypingResult.Ok(Ty.any())
            }
            is ExprP.Slice -> exprSlice(span, node.expr, node.start, node.stop, node.stride)
            is ExprP.Identifier<*, *> -> TypingResult.Ok(exprIdent(node.ident as CstIdent))
            is ExprP.Lambda<*, *> -> {
                approximation("We don't type check lambdas", Unit)
                TypingResult.Ok(Ty.anyCallable())
            }
            is ExprP.Literal -> when (node.literal) {
                is AstLiteral.Int -> TypingResult.Ok(Ty.int())
                is AstLiteral.Float -> TypingResult.Ok(Ty.float())
                is AstLiteral.String -> TypingResult.Ok(Ty.string())
                is AstLiteral.Ellipsis -> TypingResult.Ok(Ty.any())
            }
            is ExprP.Not -> {
                val ty = when (val r = expressionType(node.expr)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                if (ty.isNever()) {
                    TypingResult.Ok(Ty.never())
                } else {
                    TypingResult.Ok(Ty.bool())
                }
            }
            is ExprP.Minus -> expressionUnOp(span, node.expr, TypingUnOp.Minus)
            is ExprP.Plus -> expressionUnOp(span, node.expr, TypingUnOp.Plus)
            is ExprP.BitNot -> expressionUnOp(span, node.expr, TypingUnOp.BitNot)
            is ExprP.Op -> exprBinOp(span, node.lhs, node.op, node.rhs)
            is ExprP.If -> {
                val c = when (val r = expressionType(node.condition)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val t = when (val r = expressionType(node.trueBranch)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val f = when (val r = expressionType(node.falseBranch)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                if (c.isNever()) {
                    TypingResult.Ok(Ty.never())
                } else {
                    TypingResult.Ok(Ty.union2(t, f))
                }
            }
            is ExprP.ListExpr -> {
                val ts = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = when (val r = expressionType(elem)) {
                        is TypingResult.Ok -> r.value
                        is TypingResult.Err -> return TypingResult.Err(r.error)
                    }
                    ts.add(ty)
                }
                TypingResult.Ok(Ty.list(Ty.unions(ts)))
            }
            is ExprP.Dict -> {
                val ks = mutableListOf<Ty>()
                val vs = mutableListOf<Ty>()
                for ((k, v) in node.entries) {
                    val kTy = when (val r = expressionType(k)) {
                        is TypingResult.Ok -> r.value
                        is TypingResult.Err -> return TypingResult.Err(r.error)
                    }
                    val vTy = when (val r = expressionType(v)) {
                        is TypingResult.Ok -> r.value
                        is TypingResult.Err -> return TypingResult.Err(r.error)
                    }
                    ks.add(kTy)
                    vs.add(vTy)
                }
                TypingResult.Ok(Ty.dict(Ty.unions(ks), Ty.unions(vs)))
            }
            is ExprP.ListComprehension -> {
                when (val r = checkComprehension(node.forClause, node.clauses)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
                val bodyTy = when (val r = expressionType(node.body)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                TypingResult.Ok(Ty.list(bodyTy))
            }
            is ExprP.DictComprehension -> {
                when (val r = checkComprehension(node.forClause, node.clauses)) {
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                    is TypingResult.Ok -> {}
                }
                val kTy = when (val r = expressionType(node.kv.first)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                val vTy = when (val r = expressionType(node.kv.second)) {
                    is TypingResult.Ok -> r.value
                    is TypingResult.Err -> return TypingResult.Err(r.error)
                }
                TypingResult.Ok(Ty.dict(kTy, vTy))
            }
            is ExprP.FString -> TypingResult.Ok(Ty.string())
        }
    }
}
