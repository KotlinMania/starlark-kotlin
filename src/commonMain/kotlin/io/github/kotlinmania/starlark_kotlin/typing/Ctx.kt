// port-lint: source src/typing/ctx.rs
package io.github.kotlinmania.starlark_kotlin.typing

import io.github.kotlinmania.starlark_kotlin.values.types.string.start
import io.github.kotlinmania.starlark_kotlin.values.types.string.literal
import io.github.kotlinmania.starlark_kotlin.values.types.range.stop
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.enum_type.elements
import io.github.kotlinmania.starlark_kotlin.values.owned.asRef
import io.github.kotlinmania.starlark_kotlin.values.op
import io.github.kotlinmania.starlark_kotlin.values.attr
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.ExprP
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstExpr
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.star
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignTarget
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.kv
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.CallArgsP
import io.github.kotlinmania.starlark_kotlin.eval.bc.over
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.clauses
import io.github.kotlinmania.starlark_kotlin.entries
import io.github.kotlinmania.starlark_kotlin.docs.name
import io.github.kotlinmania.starlark_kotlin.docs.args
import io.github.kotlinmania.starlark_kotlin.debug.condition
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.pos
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstIdent
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.analysis.rhs
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.analysis.lhs
import io.github.kotlinmania.starlark_kotlin.analysis.ident
import io.github.kotlinmania.starlark_kotlin.analysis.func
import io.github.kotlinmania.starlark_kotlin.analysis.expr
import io.github.kotlinmania.starlark_kotlin.analysis.body
import io.github.kotlinmania.starlark_kotlin.values.types.list.List
import io.github.kotlinmania.starlark_kotlin.values.types.ellipsis.Ellipsis
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.Int
import io.github.kotlinmania.starlark_kotlin.analysis.dubious.Float
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.asRef

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
 {
}

class BindingId
// TODO: stub - ModuleSlotId needs real import
class ModuleSlotId

// TODO: stub - ResolvedIdent needs real import
sealed class ResolvedIdent {
    // TODO: stub - Slot needs real import
    class Slot(val slot: SlotKind, val bindingId: BindingId) : ResolvedIdent()
    class Global(val global: GlobalValue) : ResolvedIdent()
}

sealed class SlotKind {
    class Module(val id: ModuleSlotId) : SlotKind()
    // TODO: stub - Other needs real import
    class Other : SlotKind()
}

class GlobalValue {
    fun toValue(): Any = this
}

class Ty {
    companion object {
        fun any(): Ty = Ty()
        fun never(): Ty = Ty()
        fun int(): Ty = Ty()
        fun float(): Ty = Ty()
        fun string(): Ty = Ty()
        fun bool(): Ty = Ty()
        fun list(elem: Ty): Ty = Ty()
        fun dict(key: Ty, value: Ty): Ty = Ty()
        fun tuple(elems: List<Ty>): Ty = Ty()
        fun union2(a: Ty, b: Ty): Ty = Ty()
        fun unions(tys: List<Ty>): Ty = Ty()
        fun anyCallable(): Ty = Ty()
        fun ofValue(value: Any): Ty = Ty()
    }

    fun isNever(): Boolean = false
    fun isFunction(): Boolean = false
    fun isList(): Boolean = false
    fun iterUnion(): List<TyBasic> = emptyList()
    fun clone(): Ty = Ty()
}

// TODO: stub - TyBasic needs real import
sealed class TyBasic {
    // TODO: stub - List needs real import
    class List(val elem: Ty) : TyBasic()
    // TODO: stub - Dict needs real import
    class Dict(val key: Ty, val value: Ty) : TyBasic()
    // TODO: stub - Other needs real import
    class Other : TyBasic()
}

class Approximation(val category: String, val message: String) {
    companion object {
        fun new(category: String, message: Any): Approximation = Approximation(category, message.toString())
    }
}

// TODO: stub - TypingError needs real import
class TypingError(val message: String = "")
// TODO: stub - InternalError needs real import
class InternalError(val message: String = "") {
    companion object {
        fun msg(message: String, span: Span, codemap: CodeMap): InternalError = InternalError(message)
        fun fromEvalException(e: Exception): InternalError = InternalError(e.message ?: "")
    }
}

// TODO: stub - TypingOrInternalError needs real import
sealed class TypingOrInternalError {
    class Typing(val error: TypingError) : TypingOrInternalError()
    class Internal(val error: InternalError) : TypingOrInternalError()
}

class CodeMap

class TyCallArgs(
    val pos: List<Spanned<Ty>> = emptyList(),
    val named: List<Spanned<Pair<String, Ty>>> = emptyList(),
    val args: Spanned<Ty>? = null,
    val kwargs: Spanned<Ty>? = null,
)

class TypingOracleCtx(val codemap: CodeMap) {
    fun validateCall(span: Span, fun_: Ty, args: TyCallArgs): Result<Ty, TypingOrInternalError> = Result.Ok(Ty())
    fun iterItem(spanned: Spanned<Ty>): Result<Ty, TypingError> = Result.Ok(Ty())
    fun validateType(got: Spanned<Ty>, require: Ty): Result<Unit, TypingOrInternalError> = Result.Ok(Unit)
    fun exprDot(span: Span, ty: Ty, attr: String): Result<Ty, TypingError> = Result.Ok(Ty())
    fun exprIndex(span: Span, array: Ty, index: Spanned<Ty>): Result<Ty, TypingOrInternalError> = Result.Ok(Ty())
    fun exprUnOp(span: Span, ty: Ty, op: TypingUnOp): Result<Ty, TypingError> = Result.Ok(Ty())
    fun exprBinOp(span: Span, lhs: Spanned<Ty>, op: BinOp, rhs: Spanned<Ty>): Result<Ty, TypingOrInternalError> = Result.Ok(Ty())
    fun exprBinOpTy(span: Span, lhs: Spanned<Ty>, op: TypingBinOp, rhs: Spanned<Ty>): Result<Ty, TypingOrInternalError> = Result.Ok(Ty())
    fun exprSlice(span: Span, ty: Ty): Result<Ty, TypingError> = Result.Ok(Ty())
    fun indexed(ty: Ty, i: Int): Ty = Ty()
    fun probablyAList(ty: Ty): Result<Boolean, InternalError> = Result.Ok(false)
}

// TODO: stub - TypingUnOp needs real import
enum class TypingUnOp { Minus, Plus, BitNot }

// TODO: stub - TypingBinOp needs real import
enum class TypingBinOp {
    Add, Sub, Mul, Div, FloorDiv, Percent,
    BitAnd, BitOr, BitXor, LeftShift, RightShift,
}

enum class AssignOp {
    Add, Subtract, Multiply, Divide, FloorDivide, Percent,
    BitAnd, BitOr, BitXor, LeftShift, RightShift,
}

// TODO: stub - BinOp needs real import
enum class BinOp

sealed class Result<out T, out E> {
    class Ok<T>(val value: T) : Result<T, Nothing>()
    class Err<E>(val error: E) : Result<Nothing, E>()
}

class ModuleVarTypes(
    val types: MutableMap<ModuleSlotId, Ty> = mutableMapOf()
)



sealed class BindExpr {
    class Expr(val expr: CstExpr) : BindExpr()
    class GetIndex(val index: Int, val inner: BindExpr) : BindExpr()
    // TODO: stub - Iter needs real import
    class Iter(val inner: BindExpr) : BindExpr() {
        fun span(): Span = Span()
    }
    class AssignModify(val lhs: CstAssignTarget, val op: AssignOp, val rhs: CstExpr) : BindExpr()
    class SetIndex(val id: BindingId, val index: CstExpr, val expr: BindExpr) : BindExpr()
    class ListAppend(val id: BindingId, val expr: CstExpr) : BindExpr()
    class ListExtend(val id: BindingId, val expr: CstExpr) : BindExpr()
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

    private fun resultToTy(result: Result<Ty, TypingError>): Ty {
        return when (result) {
            is Result.Ok -> result.value
            is Result.Err -> {
                errors.add(result.error)
                Ty.never()
            }
        }
    }

    private fun resultToTyWithInternalError(
        result: Result<Ty, TypingOrInternalError>,
    ): Result<Ty, InternalError> {
        return when (result) {
            is Result.Ok -> Result.Ok(result.value)
            is Result.Err -> when (val e = result.error) {
                is TypingOrInternalError.Internal -> Result.Err(e.error)
                is TypingOrInternalError.Typing -> {
                    errors.add(e.error)
                    Result.Ok(Ty.never())
                }
            }
        }
    }

    private fun validateCall(fun_: Ty, args: TyCallArgs, span: Span): Result<Ty, InternalError> {
        return resultToTyWithInternalError(oracle.validateCall(span, fun_, args))
    }

    private fun fromIterated(ty: Ty, span: Span): Ty {
        return resultToTy(oracle.iterItem(Spanned(span, ty)))
    }

    fun validateType(
        got: Spanned<Ty>,
        require: Ty,
    ): Result<Unit, InternalError> {
        val result = oracle.validateType(got, require)
        if (result is Result.Err) {
            when (val e = result.error) {
                is TypingOrInternalError.Typing -> {
                    errors.add(e.error)
                }
                is TypingOrInternalError.Internal -> {
                    return Result.Err(e.error)
                }
            }
        }
        return Result.Ok(Unit)
    }

    private fun exprDot(ty: Ty, attr: String, span: Span): Ty {
        return resultToTy(oracle.exprDot(span, ty, attr))
    }

    private fun exprIndex(
        span: Span,
        array: CstExpr,
        index: CstExpr,
    ): Result<Ty, InternalError> {
        val arrayTy = when (val r = expressionType(array)) {
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
        }

        // Hack for `list[str]`: list of `list` is just "function", and we don't want
        // to make it custom type and have overly complex machinery for handling it.
        // So we just special case it here.
        if (arrayTy.isFunction()) {
            if (array.node is ExprP.Identifier) {
                val v0 = (array.node as ExprP.Identifier).ident
                if (v0.node.ident == "list") {
                    return Result.Ok(Ty.any())
                }
            }
        }

        val indexSpanned = when (val r = expressionTypeSpanned(index)) {
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
        }
        return resultToTyWithInternalError(oracle.exprIndex(span, arrayTy, indexSpanned))
    }

    private fun expressionUnOp(
        span: Span,
        arg: CstExpr,
        unOp: TypingUnOp,
    ): Result<Ty, InternalError> {
        val ty = when (val r = expressionType(arg)) {
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
        }
        return Result.Ok(resultToTy(oracle.exprUnOp(span, ty, unOp)))
    }

    fun expressionBindType(x: BindExpr): Result<Ty, InternalError> {
        return when (x) {
            is BindExpr.Expr -> expressionType(x.expr)
            is BindExpr.GetIndex -> {
                val inner = when (val r = expressionBindType(x.inner)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                Result.Ok(oracle.indexed(inner, x.index))
            }
            is BindExpr.Iter -> {
                val inner = when (val r = expressionBindType(x.inner)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                Result.Ok(fromIterated(inner, x.span()))
            }
            is BindExpr.AssignModify -> {
                val span = x.lhs.span
                val rhs = when (val r = expressionTypeSpanned(x.rhs)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                val lhs = when (val r = expressionAssignSpanned(x.lhs)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
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
                }
                resultToTyWithInternalError(
                    oracle.exprBinOpTy(span, lhs, attr, rhs)
                )
            }
            is BindExpr.SetIndex -> {
                val index = when (val r = expressionTypeSpanned(x.index)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                val e = when (val r = expressionBindType(x.expr)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                val res = mutableListOf<Ty>()
                // We know about list and dict, everything else we just ignore
                val idTy = types[x.id] ?: Ty.any()
                if (idTy.isList()) {
                    // If we know it MUST be a list, then the index must be an int
                    when (val vr = validateType(index.asRef(), Ty.int())) {
                        is Result.Err -> return Result.Err(vr.error)
                        is Result.Ok -> {}
                    }
                }
                for (ty in idTy.iterUnion()) {
                    when (ty) {
                        is TyBasic.List -> {
                            res.add(Ty.list(e.clone()))
                        }
                        is TyBasic.Dict -> {
                            res.add(Ty.dict(index.node.clone(), e.clone()))
                        }
                        else -> {
                            // Either it's not something we can apply this to, in which case do nothing.
                            // Or it's an Any, in which case we aren't going to change its type or spot errors.
                        }
                    }
                }
                Result.Ok(Ty.unions(res))
            }
            is BindExpr.ListAppend -> {
                val probablyList = when (val r = oracle.probablyAList(types[x.id] ?: Ty.any())) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                if (probablyList) {
                    val elemTy = when (val r = expressionType(x.expr)) {
                        is Result.Ok -> r.value
                        is Result.Err -> return Result.Err(r.error)
                    }
                    Result.Ok(Ty.list(elemTy))
                } else {
                    // It doesn't seem to be a list, so let's assume the append is non-mutating
                    Result.Ok(Ty.never())
                }
            }
            is BindExpr.ListExtend -> {
                val probablyList = when (val r = oracle.probablyAList(types[x.id] ?: Ty.any())) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                if (probablyList) {
                    val elemTy = when (val r = expressionType(x.expr)) {
                        is Result.Ok -> r.value
                        is Result.Err -> return Result.Err(r.error)
                    }
                    Result.Ok(Ty.list(fromIterated(elemTy, x.expr.span)))
                } else {
                    // It doesn't seem to be a list, so let's assume the extend is non-mutating
                    Result.Ok(Ty.never())
                }
            }
        }
    }

    /// Used to get the type of an expression when used as part of a ModifyAssign operation
    private fun expressionAssign(x: CstAssignTarget): Result<Ty, InternalError> {
        return when (val node = x.node) {
            is AssignTargetP.Tuple -> Result.Ok(approximation("expression_assignment", x))
            is AssignTargetP.Index -> exprIndex(x.span, node.pair.first, node.pair.second)
            is AssignTargetP.Dot -> Result.Ok(approximation("expression_assignment", x))
            is AssignTargetP.Identifier -> {
                val payload = node.ident.payload
                if (payload != null) {
                    val ty = types[payload]
                    if (ty != null) {
                        return Result.Ok(ty.clone())
                    }
                }
                Result.Err(InternalError.msg(
                    "Unknown identifier",
                    node.ident.span,
                    oracle.codemap,
                ))
            }
        }
    }

    private fun expressionAssignSpanned(x: CstAssignTarget): Result<Spanned<Ty>, InternalError> {
        return when (val r = expressionAssign(x)) {
            is Result.Ok -> Result.Ok(Spanned(x.span, r.value))
            is Result.Err -> Result.Err(r.error)
        }
    }

    /// We don't need the type out of the clauses (it doesn't change the overall type),
    /// but it is important we see through to the nested expressions to raise errors
    private fun checkComprehension(
        for_: ForClauseP,
        clauses: List<ClauseP>,
    ): Result<Unit, InternalError> {
        when (val r = expressionType(for_.over)) {
            is Result.Err -> return Result.Err(r.error)
            is Result.Ok -> {}
        }
        for (clause in clauses) {
            when (clause) {
                is ClauseP.For -> when (val r = expressionType(clause.forClause.over)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
                is ClauseP.If -> when (val r = expressionType(clause.expr)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
            }
        }
        return Result.Ok(Unit)
    }

    fun expressionTypeSpanned(
        x: CstExpr,
    ): Result<Spanned<Ty>, InternalError> {
        return when (val r = expressionType(x)) {
            is Result.Ok -> Result.Ok(Spanned(x.span, r.value))
            is Result.Err -> Result.Err(r.error)
        }
    }

    private fun exprBinOp(
        span: Span,
        lhs: CstExpr,
        op: BinOp,
        rhs: CstExpr,
    ): Result<Ty, InternalError> {
        val lhsSpanned = when (val r = expressionTypeSpanned(lhs)) {
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
        }
        val rhsSpanned = when (val r = expressionTypeSpanned(rhs)) {
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
        }
        return resultToTyWithInternalError(oracle.exprBinOp(span, lhsSpanned, op, rhsSpanned))
    }

    private fun exprCall(
        span: Span,
        f: CstExpr,
        args: CallArgsP,
    ): Result<Ty, InternalError> {
        val unpackedArgs = try {
            CallArgsUnpack.unpack(args, oracle.codemap)
        } catch (e: Exception) {
            return Result.Err(InternalError.fromEvalException(e))
        }

        val posTy = mutableListOf<Spanned<Ty>>()
        for (pos in unpackedArgs.pos) {
            val ty = when (val r = expressionType(pos.node.expr())) {
                is Result.Ok -> r.value
                is Result.Err -> return Result.Err(r.error)
            }
            posTy.add(Spanned(pos.span, ty))
        }

        val namedTy = mutableListOf<Spanned<Pair<String, Ty>>>()
        for (named in unpackedArgs.named) {
            val name = named.name()
                ?: return Result.Err(InternalError.msg(
                    "Named argument without name",
                    named.span,
                    oracle.codemap,
                ))
            val ty = when (val r = expressionType(named.node.expr())) {
                is Result.Ok -> r.value
                is Result.Err -> return Result.Err(r.error)
            }
            namedTy.add(Spanned(named.span, name to ty))
        }

        val argsTy = if (unpackedArgs.star != null) {
            val ty = when (val r = expressionTypeSpanned(unpackedArgs.star.node.expr())) {
                is Result.Ok -> r.value
                is Result.Err -> return Result.Err(r.error)
            }
            fromIterated(ty, unpackedArgs.star.span)
            ty
        } else {
            null
        }

        val kwargsTy = if (unpackedArgs.starStar != null) {
            val ty = when (val r = expressionTypeSpanned(unpackedArgs.starStar.node.expr())) {
                is Result.Ok -> r.value
                is Result.Err -> return Result.Err(r.error)
            }
            when (val vr = validateType(ty.asRef(), Ty.dict(Ty.string(), Ty.any()))) {
                is Result.Err -> return Result.Err(vr.error)
                is Result.Ok -> {}
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
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
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
    ): Result<Ty, InternalError> {
        for (e in listOfNotNull(start, stop, stride)) {
            val spanned = when (val r = expressionTypeSpanned(e)) {
                is Result.Ok -> r.value
                is Result.Err -> return Result.Err(r.error)
            }
            when (val vr = validateType(spanned.asRef(), Ty.int())) {
                is Result.Err -> return Result.Err(vr.error)
                is Result.Ok -> {}
            }
        }
        val xTy = when (val r = expressionType(x)) {
            is Result.Ok -> r.value
            is Result.Err -> return Result.Err(r.error)
        }
        return Result.Ok(resultToTy(oracle.exprSlice(span, xTy)))
    }

    private fun exprIdent(x: CstIdent): Ty {
        return when (val resolved = x.node.payload) {
            is ResolvedIdent.Slot -> when (val slot = resolved.slot) {
                is SlotKind.Module -> moduleVarTypes
                    .types[slot.id]
                    ?.clone()
                    ?: Ty.any()
                is SlotKind.Other -> {
                    val ty = types[resolved.bindingId]
                    if (ty != null) {
                        ty.clone()
                    } else {
                        // All types must be resolved to this point,
                        // this code is unreachable.
                        Ty.any()
                    }
                }
            }
            is ResolvedIdent.Global -> Ty.ofValue(resolved.global.toValue())
            null -> {
                // All identifiers must be resolved at this point,
                // but we don't stop after scope resolution error,
                // so this code is reachable.
                Ty.any()
            }
        }
    }

    fun expressionType(x: CstExpr): Result<Ty, InternalError> {
        val span = x.span
        return when (val node = x.node) {
            is ExprP.Tuple -> {
                val elems = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = when (val r = expressionType(elem)) {
                        is Result.Ok -> r.value
                        is Result.Err -> return Result.Err(r.error)
                    }
                    elems.add(ty)
                }
                Result.Ok(Ty.tuple(elems))
            }
            is ExprP.Dot -> {
                val aTy = when (val r = expressionType(node.expr)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                Result.Ok(exprDot(aTy, node.attr.value, node.attr.span))
            }
            is ExprP.Call -> exprCall(span, node.func, node.args)
            is ExprP.Index -> exprIndex(span, node.pair.first, node.pair.second)
            is ExprP.Index2 -> {
                val (a, i0, i1) = node.triple
                when (val r = expressionType(a)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
                when (val r = expressionType(i0)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
                when (val r = expressionType(i1)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
                Result.Ok(Ty.any())
            }
            is ExprP.Slice -> exprSlice(span, node.expr, node.start, node.stop, node.stride)
            is ExprP.Identifier -> Result.Ok(exprIdent(node.ident))
            is ExprP.Lambda -> {
                approximation("We don't type check lambdas", Unit)
                Result.Ok(Ty.anyCallable())
            }
            is ExprP.Literal -> when (node.literal) {
                is AstLiteral.Int -> Result.Ok(Ty.int())
                is AstLiteral.Float -> Result.Ok(Ty.float())
                is AstLiteral.StringLit -> Result.Ok(Ty.string())
                is AstLiteral.Ellipsis -> Result.Ok(Ty.any())
            }
            is ExprP.Not -> {
                val ty = when (val r = expressionType(node.expr)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                if (ty.isNever()) {
                    Result.Ok(Ty.never())
                } else {
                    Result.Ok(Ty.bool())
                }
            }
            is ExprP.Minus -> expressionUnOp(span, node.expr, TypingUnOp.Minus)
            is ExprP.Plus -> expressionUnOp(span, node.expr, TypingUnOp.Plus)
            is ExprP.BitNot -> expressionUnOp(span, node.expr, TypingUnOp.BitNot)
            is ExprP.Op -> exprBinOp(span, node.lhs, node.op, node.rhs)
            is ExprP.If -> {
                val c = when (val r = expressionType(node.condition)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                val t = when (val r = expressionType(node.trueBranch)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                val f = when (val r = expressionType(node.falseBranch)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                if (c.isNever()) {
                    Result.Ok(Ty.never())
                } else {
                    Result.Ok(Ty.union2(t, f))
                }
            }
            is ExprP.List -> {
                val ts = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = when (val r = expressionType(elem)) {
                        is Result.Ok -> r.value
                        is Result.Err -> return Result.Err(r.error)
                    }
                    ts.add(ty)
                }
                Result.Ok(Ty.list(Ty.unions(ts)))
            }
            is ExprP.Dict -> {
                val ks = mutableListOf<Ty>()
                val vs = mutableListOf<Ty>()
                for ((k, v) in node.entries) {
                    val kTy = when (val r = expressionType(k)) {
                        is Result.Ok -> r.value
                        is Result.Err -> return Result.Err(r.error)
                    }
                    val vTy = when (val r = expressionType(v)) {
                        is Result.Ok -> r.value
                        is Result.Err -> return Result.Err(r.error)
                    }
                    ks.add(kTy)
                    vs.add(vTy)
                }
                Result.Ok(Ty.dict(Ty.unions(ks), Ty.unions(vs)))
            }
            is ExprP.ListComprehension -> {
                when (val r = checkComprehension(node.forClause, node.clauses)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
                val bodyTy = when (val r = expressionType(node.body)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                Result.Ok(Ty.list(bodyTy))
            }
            is ExprP.DictComprehension -> {
                when (val r = checkComprehension(node.forClause, node.clauses)) {
                    is Result.Err -> return Result.Err(r.error)
                    is Result.Ok -> {}
                }
                val kTy = when (val r = expressionType(node.kv.first)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                val vTy = when (val r = expressionType(node.kv.second)) {
                    is Result.Ok -> r.value
                    is Result.Err -> return Result.Err(r.error)
                }
                Result.Ok(Ty.dict(kTy, vTy))
            }
            is ExprP.FString -> Result.Ok(Ty.string())
        }
    }
}
