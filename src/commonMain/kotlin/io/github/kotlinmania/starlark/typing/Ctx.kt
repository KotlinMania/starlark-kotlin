// port-lint: source src/typing/ctx.rs
package io.github.kotlinmania.starlark.typing

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.ResolvedIdent
import io.github.kotlinmania.starlark.eval.compiler.Slot
import io.github.kotlinmania.starlark.eval.compiler.scope.CstAssignTarget
import io.github.kotlinmania.starlark.eval.compiler.scope.CstExpr
import io.github.kotlinmania.starlark.eval.compiler.scope.CstIdent
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AssignOp
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstArgumentP
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark.syntax.ast.ClauseP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.typing.oracle.TypingUnOp

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

// --- CallArgsUnpack ---

/** Validated call arguments, unpacked from a CallArgsP. */
internal class CallArgsUnpack<P : AstPayload>(
    val pos: List<AstArgumentP<P>>,
    val named: List<AstArgumentP<P>>,
    val star: AstArgumentP<P>?,
    val starStar: AstArgumentP<P>?,
) {
    companion object {
        fun <P : AstPayload> unpack(
            callArgs: CallArgsP<P>,
            codemap: CodeMap,
        ): CallArgsUnpack<P> {
            val args = callArgs.args
            var numPos = 0
            var numNamed = 0
            var star: AstArgumentP<P>? = null
            var starStar: AstArgumentP<P>? = null
            val namedNames = mutableSetOf<String>()
            var stage = 0 // 0=positional, 1=named, 2=args, 3=kwargs

            for (arg in args) {
                when (arg.node) {
                    is ArgumentP.Positional -> {
                        if (stage != 0) {
                            throw EvalException.parserError("positional argument after non positional", arg.span, codemap)
                        }
                        numPos++
                    }
                    is ArgumentP.Named -> {
                        if (stage > 1) {
                            throw EvalException.parserError("named argument after *args or **kwargs", arg.span, codemap)
                        }
                        val namedArg = arg.node as ArgumentP.Named
                        if (!namedNames.add(namedArg.name.node)) {
                            // Check the names are distinct
                            throw EvalException.parserError("repeated named argument", namedArg.name.span, codemap)
                        }
                        stage = 1
                        numNamed++
                    }
                    is ArgumentP.Args -> {
                        if (stage > 1) {
                            throw EvalException.parserError("Args array after another args or kwargs", arg.span, codemap)
                        }
                        if (star != null) {
                            throw EvalException.internalError("Multiple *args in arguments", arg.span, codemap)
                        }
                        stage = 2
                        star = arg
                    }
                    is ArgumentP.KwArgs -> {
                        if (stage == 3) {
                            throw EvalException.parserError("Multiple kwargs dictionary in arguments", arg.span, codemap)
                        }
                        if (starStar != null) {
                            throw EvalException.internalError("Multiple **kwargs in arguments", arg.span, codemap)
                        }
                        stage = 3
                        starStar = arg
                    }
                }
            }

            val totalAccounted = numPos + numNamed +
                (if (star != null) 1 else 0) +
                (if (starStar != null) 1 else 0)
            if (totalAccounted != args.size) {
                throw EvalException.internalError(
                    "Argument count mismatch",
                    Span.mergeAll(args.map { it.span }.iterator()),
                    codemap,
                )
            }

            return CallArgsUnpack(
                pos = args.subList(0, numPos),
                named = args.subList(numPos, numPos + numNamed),
                star = star,
                starStar = starStar,
            )
        }
    }
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

    private fun resultToTy(result: kotlin.Result<Ty>): Ty {
        return result.getOrElse { e ->
            // The oracle returns typing errors as failures;
            // we record them and return Ty.never().
            errors.add(TypingError.msg(e.message ?: "typing error", Span.DEFAULT, oracle.codemap))
            Ty.never()
        }
    }

    private fun resultToTyWithInternalError(
        result: kotlin.Result<Ty>,
    ): kotlin.Result<Ty> {
        // The oracle already returns kotlin.Result<Ty>.
        // On failure, if it's a TypingOrInternalError we need to split it;
        // but the oracle returns plain kotlin.Result, so failures are exceptions.
        // We treat all failures as potential typing errors and push them.
        return result.fold(
            onSuccess = { kotlin.Result.success(it) },
            onFailure = { e ->
                when (e) {
                    is InternalError -> kotlin.Result.failure(e)
                    else -> {
                        errors.add(TypingError.msg(e.message ?: "typing error", Span.DEFAULT, oracle.codemap))
                        kotlin.Result.success(Ty.never())
                    }
                }
            }
        )
    }

    private fun validateCall(function: Ty, args: TyCallArgs, span: Span): kotlin.Result<Ty> {
        return resultToTyWithInternalError(oracle.validateCall(span, function, args))
    }

    private fun fromIterated(ty: Ty, span: Span): Ty {
        return resultToTy(oracle.iterItem(Spanned(ty, span)))
    }

    fun validateType(
        got: Spanned<Ty>,
        require: Ty,
    ): kotlin.Result<Unit> {
        val result = oracle.validateType(got, require)
        if (result.isFailure) {
            val e = result.exceptionOrNull()!!
            when (e) {
                is InternalError -> return kotlin.Result.failure(e)
                else -> {
                    errors.add(TypingError.msg(e.message ?: "typing error", got.span, oracle.codemap))
                }
            }
        }
        return kotlin.Result.success(Unit)
    }

    private fun exprDot(ty: Ty, attr: String, span: Span): Ty {
        return resultToTy(oracle.exprDot(span, ty, attr))
    }

    private fun exprIndex(
        span: Span,
        array: CstExpr,
        index: CstExpr,
    ): kotlin.Result<Ty> {
        val arrayTy = expressionType(array).getOrElse { return kotlin.Result.failure(it) }

        // Hack for `list[str]`: list of `list` is just "function", and we don't want
        // to make it custom type and have overly complex machinery for handling it.
        // So we just special case it here.
        if (arrayTy.isFunction()) {
            if (array.node is ExprP.Identifier<*, *>) {
                val v0 = (array.node as ExprP.Identifier<*, *>).ident
                if (v0.node.ident == "list") {
                    return kotlin.Result.success(Ty.any())
                }
            }
        }

        val indexSpanned = expressionTypeSpanned(index).getOrElse { return kotlin.Result.failure(it) }
        return resultToTyWithInternalError(oracle.exprIndex(span, arrayTy, indexSpanned))
    }

    private fun expressionUnOp(
        span: Span,
        arg: CstExpr,
        unOp: TypingUnOp,
    ): kotlin.Result<Ty> {
        val ty = expressionType(arg).getOrElse { return kotlin.Result.failure(it) }
        return kotlin.Result.success(resultToTy(oracle.exprUnOp(span, ty, unOp)))
    }

    fun expressionBindType(x: BindExpr): kotlin.Result<Ty> {
        return when (x) {
            is BindExpr.Expr -> expressionType(x.expr)
            is BindExpr.GetIndex -> {
                val inner = expressionBindType(x.inner).getOrElse { return kotlin.Result.failure(it) }
                kotlin.Result.success(oracle.indexed(inner, x.index))
            }
            is BindExpr.Iter -> {
                val inner = expressionBindType(x.inner).getOrElse { return kotlin.Result.failure(it) }
                kotlin.Result.success(fromIterated(inner, x.span()))
            }
            is BindExpr.AssignModify -> {
                val span = x.target.span
                val rhs = expressionTypeSpanned(x.expr).getOrElse { return kotlin.Result.failure(it) }
                val lhs = expressionAssignSpanned(x.target).getOrElse { return kotlin.Result.failure(it) }
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
                val index = expressionTypeSpanned(x.indexExpr).getOrElse { return kotlin.Result.failure(it) }
                val e = expressionBindType(x.inner).getOrElse { return kotlin.Result.failure(it) }
                val res = mutableListOf<Ty>()
                // We know about list and dict, everything else we just ignore
                val idTy = types[x.id] ?: Ty.any()
                if (idTy.isList()) {
                    // If we know it MUST be a list, then the index must be an int
                    validateType(index, Ty.int()).getOrElse { return kotlin.Result.failure(it) }
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
                kotlin.Result.success(Ty.unions(res))
            }
            is BindExpr.ListAppend -> {
                val probablyList = oracle.probablyAList(types[x.id] ?: Ty.any())
                    .getOrElse { return kotlin.Result.failure(it) }
                if (probablyList) {
                    val elemTy = expressionType(x.expr).getOrElse { return kotlin.Result.failure(it) }
                    kotlin.Result.success(Ty.list(elemTy))
                } else {
                    // It doesn't seem to be a list, so let's assume the append is non-mutating
                    kotlin.Result.success(Ty.never())
                }
            }
            is BindExpr.ListExtend -> {
                val probablyList = oracle.probablyAList(types[x.id] ?: Ty.any())
                    .getOrElse { return kotlin.Result.failure(it) }
                if (probablyList) {
                    val elemTy = expressionType(x.expr).getOrElse { return kotlin.Result.failure(it) }
                    kotlin.Result.success(Ty.list(fromIterated(elemTy, x.expr.span)))
                } else {
                    // It doesn't seem to be a list, so let's assume the extend is non-mutating
                    kotlin.Result.success(Ty.never())
                }
            }
        }
    }

    /** Used to get the type of an expression when used as part of a ModifyAssign operation */
    private fun expressionAssign(x: CstAssignTarget): kotlin.Result<Ty> {
        return when (val node = x.node) {
            is AssignTargetP.Tuple -> kotlin.Result.success(approximation("expression_assignment", x))
            is AssignTargetP.Index -> exprIndex(x.span, node.expr, node.index)
            is AssignTargetP.Dot -> kotlin.Result.success(approximation("expression_assignment", x))
            is AssignTargetP.Identifier<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val payload = node.ident.node.payload as? BindingId
                if (payload != null) {
                    val ty = types[payload]
                    if (ty != null) {
                        return kotlin.Result.success(ty)
                    }
                }
                kotlin.Result.failure(InternalError.msg(
                    "Unknown identifier",
                    node.ident.span,
                    oracle.codemap,
                ))
            }
        }
    }

    private fun expressionAssignSpanned(x: CstAssignTarget): kotlin.Result<Spanned<Ty>> {
        return expressionAssign(x).map { Spanned(it, x.span) }
    }

    /**
     * We don't need the type out of the clauses (it doesn't change the overall type),
     * but it is important we see through to the nested expressions to raise errors
     */
    private fun checkComprehension(
        forClause: ForClauseP<CstPayload>,
        clauses: List<ClauseP<CstPayload>>,
    ): kotlin.Result<Unit> {
        expressionType(forClause.over).getOrElse { return kotlin.Result.failure(it) }
        for (clause in clauses) {
            when (clause) {
                is ClauseP.For -> expressionType(clause.forClause.over).getOrElse { return kotlin.Result.failure(it) }
                is ClauseP.If -> expressionType(clause.cond).getOrElse { return kotlin.Result.failure(it) }
            }
        }
        return kotlin.Result.success(Unit)
    }

    fun expressionTypeSpanned(
        x: CstExpr,
    ): kotlin.Result<Spanned<Ty>> {
        return expressionType(x).map { Spanned(it, x.span) }
    }

    private fun exprBinOp(
        span: Span,
        lhs: CstExpr,
        op: BinOp,
        rhs: CstExpr,
    ): kotlin.Result<Ty> {
        val lhsSpanned = expressionTypeSpanned(lhs).getOrElse { return kotlin.Result.failure(it) }
        val rhsSpanned = expressionTypeSpanned(rhs).getOrElse { return kotlin.Result.failure(it) }
        return resultToTyWithInternalError(oracle.exprBinOp(span, lhsSpanned, op, rhsSpanned))
    }

    private fun exprCall(
        span: Span,
        f: CstExpr,
        args: CallArgsP<CstPayload>,
    ): kotlin.Result<Ty> {
        val unpackedArgs = try {
            CallArgsUnpack.unpack(args, oracle.codemap)
        } catch (e: EvalException) {
            return kotlin.Result.failure(InternalError.fromEvalException(e))
        }

        val posTy = mutableListOf<Spanned<Ty>>()
        for (pos in unpackedArgs.pos) {
            val ty = expressionType(pos.node.expr()).getOrElse { return kotlin.Result.failure(it) }
            posTy.add(Spanned(ty, pos.span))
        }

        val namedTy = mutableListOf<Spanned<Pair<String, Ty>>>()
        for (named in unpackedArgs.named) {
            val name = named.node.name()
                ?: return kotlin.Result.failure(InternalError.msg(
                    "Named argument without name",
                    named.span,
                    oracle.codemap,
                ))
            val ty = expressionType(named.node.expr()).getOrElse { return kotlin.Result.failure(it) }
            namedTy.add(Spanned(name to ty, named.span))
        }

        val argsTy = if (unpackedArgs.star != null) {
            val star = unpackedArgs.star
            val ty = expressionTypeSpanned(star.node.expr()).getOrElse { return kotlin.Result.failure(it) }
            fromIterated(ty.node, star.span)
            ty
        } else {
            null
        }

        val kwargsTy = if (unpackedArgs.starStar != null) {
            val starStar = unpackedArgs.starStar
            val ty = expressionTypeSpanned(starStar.node.expr()).getOrElse { return kotlin.Result.failure(it) }
            validateType(ty, Ty.dict(Ty.string(), Ty.any())).getOrElse { return kotlin.Result.failure(it) }
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

        val fTy = expressionType(f).getOrElse { return kotlin.Result.failure(it) }
        // If we can't resolve the types of the arguments, we can't validate the call,
        // but we still know the type of the result since the args don't impact that
        return validateCall(fTy, callArgs, span)
    }

    private fun exprSlice(
        span: Span,
        x: CstExpr,
        start: CstExpr?,
        stop: CstExpr?,
        step: CstExpr?,
    ): kotlin.Result<Ty> {
        for (e in listOfNotNull(start, stop, step)) {
            val spanned = expressionTypeSpanned(e).getOrElse { return kotlin.Result.failure(it) }
            validateType(spanned, Ty.int()).getOrElse { return kotlin.Result.failure(it) }
        }
        val xTy = expressionType(x).getOrElse { return kotlin.Result.failure(it) }
        return kotlin.Result.success(resultToTy(oracle.exprSlice(span, xTy)))
    }

    private fun exprIdent(x: CstIdent): Ty {
        return when (val resolved = x.node.payload) {
            is ResolvedIdent.Slot -> when (val slot = resolved.slot) {
                is Slot.Module -> moduleVarTypes
                    .types[slot.id]
                    ?: Ty.any()
                is Slot.Local -> {
                    val ty = types[resolved.bindingId]
                    if (ty != null) {
                        ty
                    } else {
                        // All types must be resolved to this point,
                        // this code is unreachable.
                        Ty.any()
                    }
                }
            }
            is ResolvedIdent.Global -> {
                // Ty.ofValue not yet ported; use Ty.any() as fallback
                Ty.any()
            }
            null -> {
                // All identifiers must be resolved at this point,
                // but we don't stop after scope resolution error,
                // so this code is reachable.
                Ty.any()
            }
        }
    }

    fun expressionType(x: CstExpr): kotlin.Result<Ty> {
        val span = x.span
        return when (val node = x.node) {
            is ExprP.Tuple -> {
                val elems = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = expressionType(elem).getOrElse { return kotlin.Result.failure(it) }
                    elems.add(ty)
                }
                kotlin.Result.success(Ty.tuple(elems))
            }
            is ExprP.Dot -> {
                val aTy = expressionType(node.expr).getOrElse { return kotlin.Result.failure(it) }
                kotlin.Result.success(exprDot(aTy, node.field.node, node.field.span))
            }
            is ExprP.Call -> exprCall(span, node.expr, node.args)
            is ExprP.Index -> exprIndex(span, node.expr, node.index)
            is ExprP.Index2 -> {
                expressionType(node.expr).getOrElse { return kotlin.Result.failure(it) }
                expressionType(node.index0).getOrElse { return kotlin.Result.failure(it) }
                expressionType(node.index1).getOrElse { return kotlin.Result.failure(it) }
                kotlin.Result.success(Ty.any())
            }
            is ExprP.Slice -> exprSlice(span, node.expr, node.start, node.stop, node.step)
            is ExprP.Identifier<*, *> -> kotlin.Result.success(exprIdent(node.ident as CstIdent))
            is ExprP.Lambda<*, *> -> {
                approximation("We don't type check lambdas", Unit)
                kotlin.Result.success(Ty.anyCallable())
            }
            is ExprP.Literal -> when (node.literal) {
                is AstLiteral.Int -> kotlin.Result.success(Ty.int())
                is AstLiteral.Float -> kotlin.Result.success(Ty.float())
                is AstLiteral.String -> kotlin.Result.success(Ty.string())
                is AstLiteral.Ellipsis -> kotlin.Result.success(Ty.any())
            }
            is ExprP.Not -> {
                val ty = expressionType(node.expr).getOrElse { return kotlin.Result.failure(it) }
                if (ty.isNever()) {
                    kotlin.Result.success(Ty.never())
                } else {
                    kotlin.Result.success(Ty.bool())
                }
            }
            is ExprP.Minus -> expressionUnOp(span, node.expr, TypingUnOp.MINUS)
            is ExprP.Plus -> expressionUnOp(span, node.expr, TypingUnOp.PLUS)
            is ExprP.BitNot -> expressionUnOp(span, node.expr, TypingUnOp.BIT_NOT)
            is ExprP.Op -> exprBinOp(span, node.lhs, node.op, node.rhs)
            is ExprP.If -> {
                val c = expressionType(node.cond).getOrElse { return kotlin.Result.failure(it) }
                val t = expressionType(node.v1).getOrElse { return kotlin.Result.failure(it) }
                val f = expressionType(node.v2).getOrElse { return kotlin.Result.failure(it) }
                if (c.isNever()) {
                    kotlin.Result.success(Ty.never())
                } else {
                    kotlin.Result.success(Ty.union2(t, f))
                }
            }
            is ExprP.ListExpr -> {
                val ts = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = expressionType(elem).getOrElse { return kotlin.Result.failure(it) }
                    ts.add(ty)
                }
                kotlin.Result.success(Ty.list(Ty.unions(ts)))
            }
            is ExprP.Dict -> {
                val ks = mutableListOf<Ty>()
                val vs = mutableListOf<Ty>()
                for ((k, v) in node.elements) {
                    val kTy = expressionType(k).getOrElse { return kotlin.Result.failure(it) }
                    val vTy = expressionType(v).getOrElse { return kotlin.Result.failure(it) }
                    ks.add(kTy)
                    vs.add(vTy)
                }
                kotlin.Result.success(Ty.dict(Ty.unions(ks), Ty.unions(vs)))
            }
            is ExprP.ListComprehension -> {
                checkComprehension(node.forClause, node.clauses).getOrElse { return kotlin.Result.failure(it) }
                val bodyTy = expressionType(node.expr).getOrElse { return kotlin.Result.failure(it) }
                kotlin.Result.success(Ty.list(bodyTy))
            }
            is ExprP.DictComprehension -> {
                checkComprehension(node.forClause, node.clauses).getOrElse { return kotlin.Result.failure(it) }
                val kTy = expressionType(node.key).getOrElse { return kotlin.Result.failure(it) }
                val vTy = expressionType(node.value).getOrElse { return kotlin.Result.failure(it) }
                kotlin.Result.success(Ty.dict(kTy, vTy))
            }
            is ExprP.FString -> kotlin.Result.success(Ty.string())
        }
    }
}
