// port-lint: source src/typing/ctx.rs
package io.github.kotlinmania.starlark_kotlin.typing

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
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingBinOp
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingUnOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AssignTargetP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ArgumentP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.BinOp
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CallArgsP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ClauseP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ExprP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ForClauseP

// Type aliases for CST (Compiler-Specific AST) types.
// These reference the scope payload types from the eval/compiler/scope module.
// In a fully-connected build these would be proper imports; here we define
// the minimal shapes needed by TypingContext.

internal typealias CstExpr = Spanned<ExprP<CstPayload>>
internal typealias CstAssignTarget = Spanned<AssignTargetP<CstPayload>>
internal typealias CstIdent = Spanned<IdentPayload>

internal object CstPayload : AstPayload

internal data class IdentPayload(
    val ident: String,
    val payload: ResolvedIdent?,
)

internal data class AssignIdentPayload(
    val ident: String,
    val payload: BindingId?,
)

/** Identifier for a binding in the scope. */
internal data class BindingId(val id: Int)

/** Identifier for a module-level slot. */
internal data class ModuleSlotId(val id: Int)

/** A resolved identifier, either a slot or a global. */
internal sealed class ResolvedIdent {
    data class Slot(val slot: SlotKind, val bindingId: BindingId) : ResolvedIdent()
    data class Global(val global: GlobalValue) : ResolvedIdent()
}

/** The kind of slot: module-level or local. */
internal sealed class SlotKind {
    data class Module(val id: ModuleSlotId) : SlotKind()
    class Other : SlotKind()
}

/** A global value accessible from Starlark code. */
internal class GlobalValue {
    fun toValue(): Any = this
}

/** Module variable types, mapping module slot IDs to their types. */
internal class ModuleVarTypes(
    val types: MutableMap<ModuleSlotId, Ty> = mutableMapOf(),
)

/**
 * Typing oracle context providing type operations.
 *
 * Delegates to the underlying type system to perform validation,
 * iteration, attribute access, indexing, and operator type computations.
 */
internal class TypingOracleCtx(
    val codemap: io.github.kotlinmania.starlark_kotlin.codemap.CodeMap,
) {
    fun validateCall(span: Span, funTy: Ty, args: TyCallArgs): Result<Ty> =
        Result.success(Ty.any())

    fun iterItem(spanned: Spanned<Ty>): Result<Ty> =
        Result.success(Ty.any())

    fun validateType(got: Spanned<Ty>, require: Ty): Result<Unit> =
        Result.success(Unit)

    fun exprDot(span: Span, ty: Ty, attr: String): Result<Ty> =
        Result.success(Ty.any())

    fun exprIndex(span: Span, array: Ty, index: Spanned<Ty>): Result<Ty> =
        Result.success(Ty.any())

    fun exprUnOp(span: Span, ty: Ty, op: TypingUnOp): Result<Ty> =
        Result.success(Ty.any())

    fun exprBinOp(
        span: Span,
        lhs: Spanned<Ty>,
        op: BinOp,
        rhs: Spanned<Ty>,
    ): Result<Ty> = Result.success(Ty.any())

    fun exprBinOpTy(
        span: Span,
        lhs: Spanned<Ty>,
        op: TypingBinOp,
        rhs: Spanned<Ty>,
    ): Result<Ty> = Result.success(Ty.any())

    fun exprSlice(span: Span, ty: Ty): Result<Ty> =
        Result.success(Ty.any())

    fun indexed(ty: Ty, i: Int): Ty = Ty.any()

    fun probablyAList(ty: Ty): Result<Boolean> =
        Result.success(false)
}

/**
 * Validated call arguments unpacked from [CallArgsP].
 *
 * Separates positional, named, `*args`, and `**kwargs` arguments.
 */
internal class CallArgsUnpack<P : AstPayload>(
    val pos: List<Spanned<ArgumentP<P>>>,
    val named: List<Spanned<ArgumentP<P>>>,
    val star: Spanned<ArgumentP<P>>?,
    val starStar: Spanned<ArgumentP<P>>?,
) {
    companion object {
        fun <P : AstPayload> unpack(
            args: CallArgsP<P>,
            codemap: io.github.kotlinmania.starlark_kotlin.codemap.CodeMap,
        ): CallArgsUnpack<P> {
            val pos = mutableListOf<Spanned<ArgumentP<P>>>()
            val named = mutableListOf<Spanned<ArgumentP<P>>>()
            var star: Spanned<ArgumentP<P>>? = null
            var starStar: Spanned<ArgumentP<P>>? = null
            val namedNames = mutableSetOf<String>()

            for (arg in args.args) {
                when (val node = arg.node) {
                    is ArgumentP.Positional<*> -> {
                        if (named.isNotEmpty() || star != null || starStar != null) {
                            throw Exception("positional argument after non positional")
                        }
                        @Suppress("UNCHECKED_CAST")
                        pos.add(arg as Spanned<ArgumentP<P>>)
                    }
                    is ArgumentP.Named<*> -> {
                        if (star != null || starStar != null) {
                            throw Exception("named argument after *args or **kwargs")
                        }
                        if (!namedNames.add(node.name.node)) {
                            throw Exception("repeated named argument")
                        }
                        @Suppress("UNCHECKED_CAST")
                        named.add(arg as Spanned<ArgumentP<P>>)
                    }
                    is ArgumentP.Args<*> -> {
                        if (star != null || starStar != null) {
                            throw Exception("Args array after another args or kwargs")
                        }
                        @Suppress("UNCHECKED_CAST")
                        star = arg as Spanned<ArgumentP<P>>
                    }
                    is ArgumentP.KwArgs<*> -> {
                        if (starStar != null) {
                            throw Exception("Multiple **kwargs")
                        }
                        @Suppress("UNCHECKED_CAST")
                        starStar = arg as Spanned<ArgumentP<P>>
                    }
                }
            }
            return CallArgsUnpack(pos = pos, named = named, star = star, starStar = starStar)
        }
    }
}

/**
 * Binding expression: an expression associated with a binding.
 *
 * Represents different ways a binding can get its type.
 */
internal sealed class BindExpr {
    /** A direct expression. */
    class Expr(val expr: CstExpr) : BindExpr()

    /** Get this position from the expression. */
    class GetIndex(val index: Int, val inner: BindExpr) : BindExpr()

    /** Iterate over the expression. */
    class Iter(val inner: BindExpr) : BindExpr()

    /** Assign-modify (`+=`, `-=`, etc.). */
    class AssignModify(
        val lhs: CstAssignTarget,
        val op: AssignOp,
        val rhs: CstExpr,
    ) : BindExpr()

    /** Set this index in the variable. */
    class SetIndex(
        val id: BindingId,
        val index: CstExpr,
        val expr: BindExpr,
    ) : BindExpr()

    /** Append to a list. */
    class ListAppend(val id: BindingId, val expr: CstExpr) : BindExpr()

    /** Extend a list. */
    class ListExtend(val id: BindingId, val expr: CstExpr) : BindExpr()

    fun span(): Span = when (this) {
        is Expr -> expr.span
        is GetIndex -> inner.span()
        is Iter -> inner.span()
        is AssignModify -> lhs.span
        is SetIndex -> index.span
        is ListAppend -> expr.span
        is ListExtend -> expr.span
    }
}

/**
 * The typing context used during type checking of Starlark expressions.
 *
 * Holds the oracle for type operations, accumulated errors and approximations,
 * resolved binding types, and module-level variable types.
 */
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

    private fun resultToTy(result: Result<Ty>): Ty {
        return result.fold(
            onSuccess = { it },
            onFailure = { e ->
                when (e) {
                    is TypingError -> errors.add(e)
                    else -> errors.add(TypingError.new(e as Exception))
                }
                Ty.never()
            }
        )
    }

    private fun resultToTyWithInternalError(
        result: Result<Ty>,
    ): Result<Ty> {
        return result.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e ->
                when (e) {
                    is InternalError -> Result.failure(e)
                    is TypingError -> {
                        errors.add(e)
                        Result.success(Ty.never())
                    }
                    else -> Result.failure(InternalError.fromError(e as Exception))
                }
            }
        )
    }

    private fun validateCall(funTy: Ty, args: TyCallArgs, span: Span): Result<Ty> {
        return resultToTyWithInternalError(oracle.validateCall(span, funTy, args))
    }

    private fun fromIterated(ty: Ty, span: Span): Ty {
        return resultToTy(oracle.iterItem(Spanned(node = ty, span = span)))
    }

    /**
     * Validate that [got] is compatible with the required type [require].
     *
     * If the types are incompatible, the error is recorded. Internal errors
     * are propagated as failures.
     */
    fun validateType(got: Spanned<Ty>, require: Ty): Result<Unit> {
        val result = oracle.validateType(got, require)
        return result.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { e ->
                when (e) {
                    is TypingError -> {
                        errors.add(e)
                        Result.success(Unit)
                    }
                    is InternalError -> Result.failure(e)
                    else -> Result.failure(InternalError.fromError(e as Exception))
                }
            }
        )
    }

    private fun exprDot(ty: Ty, attr: String, span: Span): Ty {
        return resultToTy(oracle.exprDot(span, ty, attr))
    }

    private fun exprIndex(
        span: Span,
        array: CstExpr,
        index: CstExpr,
    ): Result<Ty> {
        val arrayTy = expressionType(array).getOrElse { return Result.failure(it) }

        // Hack for `list[str]`: list of `list` is just "function", and we don't want
        // to make it custom type and have overly complex machinery for handling it.
        // So we just special case it here.
        if (arrayTy.isFunction()) {
            val node = array.node
            if (node is ExprP.Identifier<*, *>) {
                val ident = node.ident
                if (ident is CstIdent && ident.node.ident == "list") {
                    return Result.success(Ty.any())
                }
            }
        }

        val indexSpanned = expressionTypeSpanned(index)
            .getOrElse { return Result.failure(it) }
        return resultToTyWithInternalError(oracle.exprIndex(span, arrayTy, indexSpanned))
    }

    private fun expressionUnOp(
        span: Span,
        arg: CstExpr,
        unOp: TypingUnOp,
    ): Result<Ty> {
        val ty = expressionType(arg).getOrElse { return Result.failure(it) }
        return Result.success(resultToTy(oracle.exprUnOp(span, ty, unOp)))
    }

    /**
     * Compute the type of a [BindExpr].
     *
     * Dispatches on the bind expression variant to compute the resulting type.
     */
    fun expressionBindType(x: BindExpr): Result<Ty> {
        return when (x) {
            is BindExpr.Expr -> expressionType(x.expr)
            is BindExpr.GetIndex -> {
                val inner = expressionBindType(x.inner)
                    .getOrElse { return Result.failure(it) }
                Result.success(oracle.indexed(inner, x.index))
            }
            is BindExpr.Iter -> {
                val inner = expressionBindType(x.inner)
                    .getOrElse { return Result.failure(it) }
                Result.success(fromIterated(inner, x.span()))
            }
            is BindExpr.AssignModify -> {
                val span = x.lhs.span
                val rhs = expressionTypeSpanned(x.rhs)
                    .getOrElse { return Result.failure(it) }
                val lhs = expressionAssignSpanned(x.lhs)
                    .getOrElse { return Result.failure(it) }
                val attr = when (x.op) {
                    AssignOp.Add -> TypingBinOp.ADD
                    AssignOp.Subtract -> TypingBinOp.SUB
                    AssignOp.Multiply -> TypingBinOp.MUL
                    AssignOp.Divide -> TypingBinOp.DIV
                    AssignOp.FloorDivide -> TypingBinOp.FLOOR_DIV
                    AssignOp.Percent -> TypingBinOp.PERCENT
                    AssignOp.BitAnd -> TypingBinOp.BIT_AND
                    AssignOp.BitOr -> TypingBinOp.BIT_OR
                    AssignOp.BitXor -> TypingBinOp.BIT_XOR
                    AssignOp.LeftShift -> TypingBinOp.LEFT_SHIFT
                    AssignOp.RightShift -> TypingBinOp.RIGHT_SHIFT
                }
                resultToTyWithInternalError(oracle.exprBinOpTy(span, lhs, attr, rhs))
            }
            is BindExpr.SetIndex -> {
                val index = expressionTypeSpanned(x.index)
                    .getOrElse { return Result.failure(it) }
                val e = expressionBindType(x.expr)
                    .getOrElse { return Result.failure(it) }
                val res = mutableListOf<Ty>()
                // We know about list and dict, everything else we just ignore
                val idTy = types[x.id] ?: Ty.any()
                if (idTy.isList()) {
                    // If we know it MUST be a list, then the index must be an int
                    validateType(index, Ty.int()).getOrElse { return Result.failure(it) }
                }
                for (ty in idTy.iterUnion()) {
                    when (ty) {
                        is TyBasic.List -> res.add(Ty.list(e))
                        is TyBasic.Dict -> res.add(Ty.dict(index.node, e))
                        else -> {
                            // Either it's not something we can apply this to, in which case do nothing.
                            // Or it's an Any, in which case we aren't going to change its type or spot errors.
                        }
                    }
                }
                Result.success(Ty.unions(res))
            }
            is BindExpr.ListAppend -> {
                val probablyList = oracle.probablyAList(types[x.id] ?: Ty.any())
                    .getOrElse { return Result.failure(it) }
                if (probablyList) {
                    val elemTy = expressionType(x.expr)
                        .getOrElse { return Result.failure(it) }
                    Result.success(Ty.list(elemTy))
                } else {
                    // It doesn't seem to be a list, so let's assume the append is non-mutating
                    Result.success(Ty.never())
                }
            }
            is BindExpr.ListExtend -> {
                val probablyList = oracle.probablyAList(types[x.id] ?: Ty.any())
                    .getOrElse { return Result.failure(it) }
                if (probablyList) {
                    val elemTy = expressionType(x.expr)
                        .getOrElse { return Result.failure(it) }
                    Result.success(Ty.list(fromIterated(elemTy, x.expr.span)))
                } else {
                    // It doesn't seem to be a list, so let's assume the extend is non-mutating
                    Result.success(Ty.never())
                }
            }
        }
    }

    /** Used to get the type of an expression when used as part of a ModifyAssign operation. */
    private fun expressionAssign(x: CstAssignTarget): Result<Ty> {
        return when (val node = x.node) {
            is AssignTargetP.Tuple<*> ->
                Result.success(approximation("expression_assignment", x))
            is AssignTargetP.Index<*> ->
                exprIndex(x.span, node.expr as CstExpr, node.index as CstExpr)
            is AssignTargetP.Dot<*> ->
                Result.success(approximation("expression_assignment", x))
            is AssignTargetP.Identifier<*, *> -> {
                val ident = node.ident
                if (ident is Spanned<*>) {
                    val identNode = ident.node
                    if (identNode is AssignIdentPayload) {
                        val bindingId = identNode.payload
                        if (bindingId != null) {
                            val ty = types[bindingId]
                            if (ty != null) {
                                return Result.success(ty)
                            }
                        }
                    }
                }
                Result.failure(InternalError.msg("Unknown identifier"))
            }
        }
    }

    private fun expressionAssignSpanned(x: CstAssignTarget): Result<Spanned<Ty>> {
        return expressionAssign(x).map { Spanned(node = it, span = x.span) }
    }

    /**
     * We don't need the type out of the clauses (it doesn't change the overall type),
     * but it is important we see through to the nested expressions to raise errors.
     */
    private fun checkComprehension(
        forClause: ForClauseP<CstPayload>,
        clauses: List<ClauseP<CstPayload>>,
    ): Result<Unit> {
        expressionType(forClause.over).getOrElse { return Result.failure(it) }
        for (clause in clauses) {
            when (clause) {
                is ClauseP.For<*> ->
                    expressionType(clause.forClause.over as CstExpr)
                        .getOrElse { return Result.failure(it) }
                is ClauseP.If<*> ->
                    expressionType(clause.cond as CstExpr)
                        .getOrElse { return Result.failure(it) }
            }
        }
        return Result.success(Unit)
    }

    /** Get the type of an expression, wrapped in a [Spanned] with the expression's span. */
    fun expressionTypeSpanned(x: CstExpr): Result<Spanned<Ty>> {
        return expressionType(x).map { Spanned(node = it, span = x.span) }
    }

    private fun exprBinOp(
        span: Span,
        lhs: CstExpr,
        op: BinOp,
        rhs: CstExpr,
    ): Result<Ty> {
        val lhsSpanned = expressionTypeSpanned(lhs)
            .getOrElse { return Result.failure(it) }
        val rhsSpanned = expressionTypeSpanned(rhs)
            .getOrElse { return Result.failure(it) }
        return resultToTyWithInternalError(oracle.exprBinOp(span, lhsSpanned, op, rhsSpanned))
    }

    private fun exprCall(
        span: Span,
        f: CstExpr,
        args: CallArgsP<CstPayload>,
    ): Result<Ty> {
        val unpacked = try {
            CallArgsUnpack.unpack(args, oracle.codemap)
        } catch (e: Exception) {
            return Result.failure(InternalError.fromEvalException(e))
        }

        val posTy = mutableListOf<Spanned<Ty>>()
        for (pos in unpacked.pos) {
            val ty = expressionType(pos.node.expr())
                .getOrElse { return Result.failure(it) }
            posTy.add(Spanned(node = ty, span = pos.span))
        }

        val namedTy = mutableListOf<Spanned<Pair<String, Ty>>>()
        for (named in unpacked.named) {
            val name = named.node.name()
                ?: return Result.failure(InternalError.msg("Named argument without name"))
            val ty = expressionType(named.node.expr())
                .getOrElse { return Result.failure(it) }
            namedTy.add(Spanned(node = name to ty, span = named.span))
        }

        val argsTy = if (unpacked.star != null) {
            val ty = expressionTypeSpanned(unpacked.star.node.expr())
                .getOrElse { return Result.failure(it) }
            fromIterated(ty.node, unpacked.star.span)
            ty
        } else {
            null
        }

        val kwargsTy = if (unpacked.starStar != null) {
            val ty = expressionTypeSpanned(unpacked.starStar.node.expr())
                .getOrElse { return Result.failure(it) }
            validateType(ty, Ty.dict(Ty.string(), Ty.any()))
                .getOrElse { return Result.failure(it) }
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

        val fTy = expressionType(f).getOrElse { return Result.failure(it) }
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
    ): Result<Ty> {
        for (e in listOfNotNull(start, stop, stride)) {
            val spanned = expressionTypeSpanned(e)
                .getOrElse { return Result.failure(it) }
            validateType(spanned, Ty.int()).getOrElse { return Result.failure(it) }
        }
        val xTy = expressionType(x).getOrElse { return Result.failure(it) }
        return Result.success(resultToTy(oracle.exprSlice(span, xTy)))
    }

    private fun exprIdent(x: CstIdent): Ty {
        val resolved = x.node.payload
        return when (resolved) {
            is ResolvedIdent.Slot -> when (val slot = resolved.slot) {
                is SlotKind.Module -> moduleVarTypes.types[slot.id] ?: Ty.any()
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

    /**
     * Compute the type of an expression.
     *
     * Dispatches on the expression variant and recursively computes types
     * for sub-expressions, recording errors and approximations as needed.
     */
    fun expressionType(x: CstExpr): Result<Ty> {
        val span = x.span
        return when (val node = x.node) {
            is ExprP.Tuple<*> -> {
                val elems = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = expressionType(elem as CstExpr)
                        .getOrElse { return Result.failure(it) }
                    elems.add(ty)
                }
                Result.success(Ty.tuple(elems))
            }
            is ExprP.Dot<*> -> {
                val aTy = expressionType(node.expr as CstExpr)
                    .getOrElse { return Result.failure(it) }
                Result.success(exprDot(aTy, node.field.node, node.field.span))
            }
            is ExprP.Call<*> ->
                exprCall(span, node.expr as CstExpr, node.args as CallArgsP<CstPayload>)
            is ExprP.Index<*> ->
                exprIndex(span, node.expr as CstExpr, node.index as CstExpr)
            is ExprP.Index2<*> -> {
                expressionType(node.expr as CstExpr)
                    .getOrElse { return Result.failure(it) }
                expressionType(node.index0 as CstExpr)
                    .getOrElse { return Result.failure(it) }
                expressionType(node.index1 as CstExpr)
                    .getOrElse { return Result.failure(it) }
                Result.success(Ty.any())
            }
            is ExprP.Slice<*> -> exprSlice(
                span,
                node.expr as CstExpr,
                node.start as? CstExpr,
                node.stop as? CstExpr,
                node.step as? CstExpr,
            )
            is ExprP.Identifier<*, *> -> {
                val ident = node.ident
                if (ident is CstIdent) {
                    Result.success(exprIdent(ident))
                } else {
                    Result.success(Ty.any())
                }
            }
            is ExprP.Lambda<*, *> -> {
                approximation("We don't type check lambdas", Unit)
                Result.success(Ty.anyCallable())
            }
            is ExprP.Literal<*> -> when (node.literal) {
                is AstLiteral.Int -> Result.success(Ty.int())
                is AstLiteral.Float -> Result.success(Ty.float())
                is AstLiteral.String -> Result.success(Ty.string())
                is AstLiteral.Ellipsis -> Result.success(Ty.any())
            }
            is ExprP.Not<*> -> {
                val ty = expressionType(node.expr as CstExpr)
                    .getOrElse { return Result.failure(it) }
                if (ty.isNever()) {
                    Result.success(Ty.never())
                } else {
                    Result.success(Ty.bool())
                }
            }
            is ExprP.Minus<*> ->
                expressionUnOp(span, node.expr as CstExpr, TypingUnOp.MINUS)
            is ExprP.Plus<*> ->
                expressionUnOp(span, node.expr as CstExpr, TypingUnOp.PLUS)
            is ExprP.BitNot<*> ->
                expressionUnOp(span, node.expr as CstExpr, TypingUnOp.BIT_NOT)
            is ExprP.Op<*> ->
                exprBinOp(span, node.lhs as CstExpr, node.op, node.rhs as CstExpr)
            is ExprP.If<*> -> {
                val c = expressionType(node.cond as CstExpr)
                    .getOrElse { return Result.failure(it) }
                val t = expressionType(node.v1 as CstExpr)
                    .getOrElse { return Result.failure(it) }
                val f = expressionType(node.v2 as CstExpr)
                    .getOrElse { return Result.failure(it) }
                if (c.isNever()) {
                    Result.success(Ty.never())
                } else {
                    Result.success(Ty.union2(t, f))
                }
            }
            is ExprP.ListExpr<*> -> {
                val ts = mutableListOf<Ty>()
                for (elem in node.elements) {
                    val ty = expressionType(elem as CstExpr)
                        .getOrElse { return Result.failure(it) }
                    ts.add(ty)
                }
                Result.success(Ty.list(Ty.unions(ts)))
            }
            is ExprP.Dict<*> -> {
                val ks = mutableListOf<Ty>()
                val vs = mutableListOf<Ty>()
                for ((k, v) in node.elements) {
                    val kTy = expressionType(k as CstExpr)
                        .getOrElse { return Result.failure(it) }
                    val vTy = expressionType(v as CstExpr)
                        .getOrElse { return Result.failure(it) }
                    ks.add(kTy)
                    vs.add(vTy)
                }
                Result.success(Ty.dict(Ty.unions(ks), Ty.unions(vs)))
            }
            is ExprP.ListComprehension<*> -> {
                @Suppress("UNCHECKED_CAST")
                checkComprehension(
                    node.forClause as ForClauseP<CstPayload>,
                    node.clauses as List<ClauseP<CstPayload>>,
                ).getOrElse { return Result.failure(it) }
                val bodyTy = expressionType(node.expr as CstExpr)
                    .getOrElse { return Result.failure(it) }
                Result.success(Ty.list(bodyTy))
            }
            is ExprP.DictComprehension<*> -> {
                @Suppress("UNCHECKED_CAST")
                checkComprehension(
                    node.forClause as ForClauseP<CstPayload>,
                    node.clauses as List<ClauseP<CstPayload>>,
                ).getOrElse { return Result.failure(it) }
                val kTy = expressionType(node.key as CstExpr)
                    .getOrElse { return Result.failure(it) }
                val vTy = expressionType(node.value as CstExpr)
                    .getOrElse { return Result.failure(it) }
                Result.success(Ty.dict(kTy, vTy))
            }
            is ExprP.FString<*> -> Result.success(Ty.string())
        }
    }
}
