// port-lint: source src/typing/oracle/ctx.rs
package io.github.kotlinmania.starlark_kotlin.typing.oracle

import io.github.kotlinmania.starlark_kotlin.values.typing.isNever
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.values.owned.default
import io.github.kotlinmania.starlark_kotlin.values.default

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

class CodeMap

class Ty(private val repr: String = "") {
    companion object {
        fun any(): Ty = Ty("any")
        fun never(): Ty = Ty("never")
        fun none(): Ty = Ty("none")
        fun bool(): Ty = Ty("bool")
        fun int(): Ty = Ty("int")
        fun unions(tys: List<Ty>): Ty = if (tys.size == 1) tys[0] else Ty("union")
        fun union2(a: Ty, b: Ty): Ty = Ty("union")
        fun basic(basic: TyBasic): Ty = Ty(basic.toString())
        fun list(item: Ty): Ty = Ty("list")
        fun tuple(items: List<Ty>): Ty = Ty("tuple")
        fun dict(key: Ty, value: Ty): Ty = Ty("dict")
        fun set(item: Ty): Ty = Ty("set")
        fun function(params: ParamSpec, result: Ty): Ty = Ty("function")
        fun anyTuple(): Ty = Ty("tuple")
    }
    fun isAny(): Boolean = repr == "any"
    fun isNever(): Boolean = repr == "never"
    fun iterUnion(): List<TyBasic> = listOf()
    fun dupe(): Ty = this
    fun clone(): Ty = Ty(repr)
    fun typecheckUnionSimple(checker: (TyBasic) -> kotlin.Result<Ty>): kotlin.Result<Ty> =
        kotlin.Result.success(any())
    override fun toString(): String = repr
}

class ParamSpec(private val params: List<Param> = emptyList()) {
    companion object {
        fun any(): ParamSpec = ParamSpec()
        fun empty(): ParamSpec = ParamSpec()
        fun posOnly(required: List<Ty>, optional: List<Ty>): ParamSpec = ParamSpec()
    }
    fun params(): List<Param> = params
    fun isAny(): Boolean = false
    fun allRequiredPosOnlyNamedOnly(): Pair<List<Ty>, List<Pair<String, Ty>>>? = null
    override fun equals(other: Any?): Boolean = other is ParamSpec && params == other.params
    override fun hashCode(): Int = params.hashCode()
}

class Param(
    val mode: ParamMode,
    val ty: Ty,
) {
    fun allowsPos(): Boolean = mode is ParamMode.PosOnly || mode is ParamMode.PosOrName || mode is ParamMode.Args
    fun name(): String? = when (mode) {
        is ParamMode.PosOrName -> mode.name
        is ParamMode.NameOnly -> mode.name
        else -> null
    }
    fun nameDisplay(): String = name() ?: "<unnamed>"
}

sealed class ParamMode {
    class PosOnly(val required: ParamIsRequired) : ParamMode()
    class PosOrName(val name: String, val required: ParamIsRequired) : ParamMode()
    class NameOnly(val name: String, val required: ParamIsRequired) : ParamMode()
    data object Args : ParamMode()
    data object Kwargs : ParamMode()
}

enum class ParamIsRequired { Yes, No }

class TyCallArgs(
    val pos: MutableList<Spanned<Ty>> = mutableListOf(),
    val named: MutableList<Spanned<Pair<String, Ty>>> = mutableListOf(),
    val args: Spanned<Ty>? = null,
    val kwargs: Spanned<Ty>? = null,
)

class TyCallable(private val params: ParamSpec = ParamSpec(), private val result: Ty = Ty()) {
    fun params(): ParamSpec = params
    fun result(): Ty = result
    fun validateCall(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): kotlin.Result<Ty> =
        kotlin.Result.success(result)
}

sealed class TyBasic {
    data object Any : TyBasic()
    class StarlarkValue(val value: TyStarlarkValue) : TyBasic()
    class Iter(val item: ArcTy) : TyBasic()
    class Callable(val callable: TyCallable) : TyBasic()
    data object Type : TyBasic()
    class List(val item: ArcTy) : TyBasic()
    class Tuple(val tuple: TyTuple) : TyBasic()
    class Dict(val key: ArcTy, val value: ArcTy) : TyBasic()
    class Custom(val custom: TyCustom) : TyBasic()
    class Set(val item: ArcTy) : TyBasic()

    companion object {
        fun int(): TyBasic = StarlarkValue(TyStarlarkValue.int())
        fun anyList(): TyBasic = List(ArcTy.any())
        fun anyDict(): TyBasic = Dict(ArcTy.any(), ArcTy.any())
        fun anySet(): TyBasic = Set(ArcTy.any())
    }

    fun dupe(): TyBasic = this
    fun isTuple(): Boolean = this is Tuple
    fun isList(): Boolean = this is List
}

class ArcTy(private val ty: Ty = Ty()) {
    companion object {
        fun any(): ArcTy = ArcTy(Ty.any())
    }
    fun toTy(): Ty = ty
    fun dupe(): ArcTy = this
    override fun toString(): String = ty.toString()
}

class TyStarlarkValue(private val name: String = "") {
    companion object {
        fun <T> new(): TyStarlarkValue = TyStarlarkValue()
        fun int(): TyStarlarkValue = TyStarlarkValue("int")
        fun tuple(): TyStarlarkValue = TyStarlarkValue("tuple")
    }
    fun validateCall(span: Span, oracle: TypingOracleCtx): Ty = Ty.any()
    fun iterItem(): kotlin.Result<Ty> = kotlin.Result.success(Ty.any())
    fun index(index: TyBasic): Ty = Ty.any()
    fun slice(): kotlin.Result<Ty> = kotlin.Result.success(Ty.any())
    fun attr(attr: String): kotlin.Result<Ty> = kotlin.Result.failure(TypingNoContextError)
    fun unOp(unOp: TypingUnOp): kotlin.Result<TyStarlarkValue> = kotlin.Result.failure(TypingNoContextError)
    fun binOp(binOp: TypingBinOp, rhs: TyBasic): Ty = Ty.any()
    fun rbinOp(binOp: TypingBinOp, lhs: TyBasic): Ty = Ty.any()
    fun isList(): Boolean = name == "list"
    fun isSet(): Boolean = name == "set"
    fun isDict(): Boolean = name == "dict"
    fun isTuple(): Boolean = name == "tuple"
    fun isCallable(): Boolean = false
    fun isType(): Boolean = name == "type"
}

class TyTuple {
    fun get(i: Int): Ty? = null
    fun itemTy(): Ty = Ty.any()
    companion object {
        fun intersects(x: TyTuple, y: TyTuple, oracle: TypingOracleCtx): kotlin.Result<Boolean> =
            kotlin.Result.success(true)
    }
}

class TyCustom(internal val inner: Any? = null) {
    fun intersectsWith(y: TyBasic, oracle: TypingOracleCtx): kotlin.Result<Boolean> =
        kotlin.Result.success(false)
    fun validateCallDyn(span: Span, args: TyCallArgs, oracle: TypingOracleCtx): kotlin.Result<Ty> =
        kotlin.Result.success(Ty.any())
    fun attributeDyn(attr: String): kotlin.Result<Ty> = kotlin.Result.failure(TypingNoContextError)
    fun indexDyn(index: TyBasic, oracle: TypingOracleCtx): Ty = Ty.any()
    fun binOpDyn(binOp: TypingBinOp, rhs: TyBasic, oracle: TypingOracleCtx): Ty = Ty.any()
    fun iterItemDyn(): kotlin.Result<Ty> = kotlin.Result.failure(TypingNoContextError)
}

class ListType
class MutableDictType
class TupleType
class MutableSetType

class TypingError(val message: String = "") {
    companion object {
        fun newAnyhow(err: Any, span: Span, codemap: CodeMap): TypingError = TypingError(err.toString())
        fun msg(msg: Any, span: Span, codemap: CodeMap): TypingError = TypingError(msg.toString())
    }
}

class InternalError(val message: String = "") {
    companion object {
        fun msg(msg: Any, span: Span, codemap: CodeMap): InternalError = InternalError(msg.toString())
    }
}

sealed class TypingOrInternalError {
    class Typing(val error: TypingError) : TypingOrInternalError()
    class Internal(val error: InternalError) : TypingOrInternalError()
}

object TypingNoContextError : Exception()

sealed class TypingNoContextOrInternalError {
    data object Typing : TypingNoContextOrInternalError()
    class Internal(val error: InternalError) : TypingNoContextOrInternalError()
}

enum class TypingBinOp {
    Less, In, Add, Mul, Sub, Percent, Div, FloorDiv,
    BitAnd, BitOr, BitXor, LeftShift, RightShift;

    fun alwaysBool(): Boolean = this == Less || this == In
}

enum class TypingUnOp { Plus, Minus, BitNot }

enum class BinOp {
    And, Or, Equal, NotEqual, In, NotIn,
    Less, LessOrEqual, Greater, GreaterOrEqual,
    Subtract, Add, Multiply, Percent, Divide, FloorDivide,
    BitAnd, BitOr, BitXor, LeftShift, RightShift,
}

sealed class TypingOracleCtxError : Exception() {
    class IncompatibleType(val got: String, val require: String) : TypingOracleCtxError() {
        override val message: String get() = "Expected type `$require` but got `$got`"
    }
    class CallToNonCallable(val ty: String) : TypingOracleCtxError() {
        override val message: String get() = "Call to a non-callable type `$ty`"
    }
    class MissingRequiredParameter(val name: String) : TypingOracleCtxError() {
        override val message: String get() = "Missing required parameter `$name`"
    }
    class UnexpectedNamedArgument(val name: String) : TypingOracleCtxError() {
        override val message: String get() = "Unexpected parameter named `$name`"
    }
    data object TooManyPositionalArguments : TypingOracleCtxError() {
        override val message: String get() = "Too many positional arguments"
    }
    class CallArgumentsIncompatible(val fn: Ty) : TypingOracleCtxError() {
        override val message: String get() = "Call arguments incompatible, fn type is `$fn`"
    }
    class MissingIndexOperator(val ty: Ty, val index: Ty) : TypingOracleCtxError() {
        override val message: String get() = "Type `$ty` does not have [] operator or [] cannot accept `$index`"
    }
    class MissingSliceOperator(val ty: Ty) : TypingOracleCtxError() {
        override val message: String get() = "Type `$ty` does not have [::] operator"
    }
    class AttributeNotAvailable(val ty: Ty, val attr: String) : TypingOracleCtxError() {
        override val message: String get() = "The attribute `$attr` is not available on the type `$ty`"
    }
    class NotIterable(val ty: Ty) : TypingOracleCtxError() {
        override val message: String get() = "Type `$ty` is not iterable"
    }
    class UnaryOperatorNotAvailable(val ty: Ty, val unOp: TypingUnOp) : TypingOracleCtxError() {
        override val message: String get() = "Unary operator `$unOp` is not available on the type `$ty`"
    }
    class BinaryOperatorNotAvailable(val binOp: TypingBinOp, val left: Ty, val right: Ty) : TypingOracleCtxError() {
        override val message: String get() = "Binary operator `$binOp` is not available on the types `$left` and `$right`"
    }
}

/// Oracle reference with utility methods.
///
/// This type is stateless.
class TypingOracleCtx(
    internal val codemap: CodeMap,
) {
    internal fun mkError(span: Span, err: Exception): TypingError {
        return TypingError.newAnyhow(err, span, codemap)
    }

    internal fun mkErrorAsMaybeInternal(span: Span, err: Exception): TypingOrInternalError {
        return TypingOrInternalError.Typing(TypingError.newAnyhow(err, span, codemap))
    }

    internal fun msgError(span: Span, msg: Any): TypingOrInternalError {
        return TypingOrInternalError.Typing(TypingError.msg(msg, span, codemap))
    }

    /// If I do `self[i]` what will the resulting type be.
    internal fun indexedBasic(ty: TyBasic, i: Int): Ty {
        return when (ty) {
            is TyBasic.Any -> Ty.any()
            is TyBasic.List -> ty.item.toTy()
            is TyBasic.Tuple -> ty.tuple.get(i) ?: Ty.never()
            // Not exactly sure what we should do here
            else -> Ty.any()
        }
    }

    /// If I do `self[i]` what will the resulting type be.
    internal fun indexed(ty: Ty, i: Int): Ty {
        return Ty.unions(
            ty.iterUnion().map { x -> indexedBasic(x, i) }
        )
    }

    internal fun validateType(
        got: Spanned<Ty>,
        require: Ty,
    ): kotlin.Result<Unit> {
        val intersects = intersects(got.node, require)
        if (intersects.isFailure) return kotlin.Result.failure(intersects.exceptionOrNull()!!)
        if (!intersects.getOrThrow()) {
            return kotlin.Result.failure(
                Exception(mkErrorAsMaybeInternal(
                    got.span,
                    TypingOracleCtxError.IncompatibleType(
                        got = got.toString(),
                        require = require.toString(),
                    ),
                ).toString())
            )
        }
        return kotlin.Result.success(Unit)
    }

    private fun validateArgs(
        params: ParamSpec,
        args: TyCallArgs,
        span: Span,
    ): kotlin.Result<Unit> {
        // Want to figure out which arguments go in which positions
        val paramArgs: MutableList<MutableList<Spanned<Ty>>> =
            MutableList(params.params().size) { mutableListOf() }
        // The next index a positional parameter might fill
        var paramPos = 0
        var seenVargs = false

        val argsPos = args.pos
        val argsNamed = args.named
        val argsArgs = args.args
        val argsKwargs = args.kwargs

        for (ty in argsPos) {
            while (true) {
                val param = params.params().getOrNull(paramPos)
                if (param == null) {
                    return kotlin.Result.failure(
                        Exception(mkErrorAsMaybeInternal(
                            ty.span,
                            TypingOracleCtxError.TooManyPositionalArguments,
                        ).toString())
                    )
                }
                val foundIndex = paramPos
                if (param.mode != ParamMode.Args) {
                    paramPos += 1
                }
                if (param.allowsPos()) {
                    paramArgs[foundIndex].add(Spanned(ty.node, ty.span))
                    break
                }
            }
        }

        for (arg in argsNamed) {
            val (name, ty) = arg.node
            var success = false
            for ((i, param) in params.params().withIndex()) {
                if (param.name() == name || param.mode == ParamMode.Kwargs) {
                    paramArgs[i].add(Spanned(ty, arg.span))
                    success = true
                    break
                }
            }
            if (!success) {
                return kotlin.Result.failure(
                    Exception(mkErrorAsMaybeInternal(
                        arg.span,
                        TypingOracleCtxError.UnexpectedNamedArgument(name = name),
                    ).toString())
                )
            }
        }

        if (argsArgs != null) {
            seenVargs = true
        }
        if (argsKwargs != null) {
            seenVargs = true
        }

        for ((param, argsList) in params.params().zip(paramArgs)) {
            when (val mode = param.mode) {
                is ParamMode.PosOnly, is ParamMode.PosOrName, is ParamMode.NameOnly -> {
                    val req = when (mode) {
                        is ParamMode.PosOnly -> mode.required
                        is ParamMode.PosOrName -> mode.required
                        is ParamMode.NameOnly -> mode.required
                        else -> ParamIsRequired.No
                    }
                    when {
                        argsList.isEmpty() -> {
                            if (req == ParamIsRequired.Yes && !seenVargs) {
                                return kotlin.Result.failure(
                                    Exception(mkErrorAsMaybeInternal(
                                        span,
                                        TypingOracleCtxError.MissingRequiredParameter(
                                            name = param.nameDisplay(),
                                        ),
                                    ).toString())
                                )
                            }
                        }
                        argsList.size == 1 -> {
                            val vr = validateType(argsList[0], param.ty)
                            if (vr.isFailure) return vr
                        }
                        else -> {
                            return kotlin.Result.failure(
                                Exception(TypingOrInternalError.Internal(
                                    InternalError.msg(
                                        "Multiple arguments bound to parameter",
                                        span,
                                        codemap,
                                    )
                                ).toString())
                            )
                        }
                    }
                }
                is ParamMode.Args -> {
                    for (ty in argsList) {
                        // For an arg, we require the type annotation to be inner value,
                        // rather than the outer (which is always a tuple)
                        val vr = validateType(ty, param.ty)
                        if (vr.isFailure) return vr
                    }
                }
                is ParamMode.Kwargs -> {
                    for (ty in argsList) {
                        val vr = validateType(ty, param.ty)
                        if (vr.isFailure) return vr
                    }
                }
            }
        }
        return kotlin.Result.success(Unit)
    }

    internal fun validateFnCall(
        span: Span,
        fun_: TyCallable,
        args: TyCallArgs,
    ): kotlin.Result<Ty> {
        val vr = validateArgs(fun_.params(), args, span)
        if (vr.isFailure) return kotlin.Result.failure(vr.exceptionOrNull()!!)
        return kotlin.Result.success(fun_.result().dupe())
    }

    private fun validateCallBasic(
        span: Span,
        fun_: TyBasic,
        args: TyCallArgs,
    ): kotlin.Result<Ty> {
        return when (fun_) {
            is TyBasic.Any -> kotlin.Result.success(Ty.any())
            is TyBasic.StarlarkValue -> kotlin.Result.success(fun_.value.validateCall(span, this))
            is TyBasic.List, is TyBasic.Dict, is TyBasic.Tuple, is TyBasic.Set -> {
                kotlin.Result.failure(Exception(mkErrorAsMaybeInternal(
                    span,
                    TypingOracleCtxError.CallToNonCallable(ty = fun_.toString()),
                ).toString()))
            }
            is TyBasic.Iter, is TyBasic.Type -> {
                // Unknown type, may be callable.
                kotlin.Result.success(Ty.any())
            }
            is TyBasic.Callable -> fun_.callable.validateCall(span, args, this)
            is TyBasic.Custom -> fun_.custom.validateCallDyn(span, args, this)
        }
    }

    internal fun validateCall(
        span: Span,
        fun_: Ty,
        args: TyCallArgs,
    ): kotlin.Result<Ty> {
        if (fun_.isAny() || fun_.isNever()) {
            return kotlin.Result.success(fun_.dupe())
        }

        val successful = mutableListOf<Ty>()
        val errors = mutableListOf<TypingError>()
        for (variant in fun_.iterUnion()) {
            val result = validateCallBasic(span, variant, args)
            if (result.isSuccess) {
                successful.add(result.getOrThrow())
            } else {
                errors.add(TypingError(result.exceptionOrNull()?.message ?: ""))
            }
        }

        return if (successful.isNotEmpty()) {
            kotlin.Result.success(Ty.unions(successful))
        } else {
            if (errors.size == 1) {
                kotlin.Result.failure(Exception(errors.removeAt(0).message))
            } else {
                kotlin.Result.failure(Exception(mkErrorAsMaybeInternal(
                    span,
                    TypingOracleCtxError.CallArgumentsIncompatible(fn = fun_.dupe()),
                ).toString()))
            }
        }
    }

    private fun iterItemBasic(ty: TyBasic): kotlin.Result<Ty> {
        return when (ty) {
            is TyBasic.Any -> kotlin.Result.success(Ty.any())
            is TyBasic.StarlarkValue -> ty.value.iterItem()
            is TyBasic.List -> kotlin.Result.success(ty.item.toTy())
            is TyBasic.Dict -> kotlin.Result.success(ty.key.toTy())
            is TyBasic.Tuple -> kotlin.Result.success(ty.tuple.itemTy())
            is TyBasic.Callable -> kotlin.Result.success(Ty.any())
            is TyBasic.Type -> kotlin.Result.success(Ty.any())
            is TyBasic.Iter -> kotlin.Result.success(ty.item.toTy())
            is TyBasic.Custom -> ty.custom.iterItemDyn()
            is TyBasic.Set -> kotlin.Result.success(ty.item.toTy())
        }
    }

    /// Item type of an iterable.
    internal fun iterItem(iter: Spanned<Ty>): kotlin.Result<Ty> {
        val result = iter.node.typecheckUnionSimple { basic -> iterItemBasic(basic) }
        return if (result.isSuccess) {
            result
        } else {
            kotlin.Result.failure(Exception(mkError(
                iter.span,
                TypingOracleCtxError.NotIterable(ty = iter.node.clone()),
            ).message))
        }
    }

    private fun exprIndexTy(
        array: TyBasic,
        index: Spanned<TyBasic>,
    ): kotlin.Result<Ty> {
        return when (array) {
            is TyBasic.Any, is TyBasic.Callable, is TyBasic.Iter, is TyBasic.Type ->
                kotlin.Result.success(Ty.any())
            is TyBasic.Tuple -> {
                val ir = intersectsBasic(index.node, TyBasic.int())
                if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                if (!ir.getOrThrow()) {
                    return kotlin.Result.failure(TypingNoContextError)
                }
                kotlin.Result.success(array.tuple.itemTy())
            }
            is TyBasic.List -> {
                val ir = intersectsBasic(index.node, TyBasic.int())
                if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                if (!ir.getOrThrow()) {
                    return kotlin.Result.failure(TypingNoContextError)
                }
                kotlin.Result.success(array.item.toTy())
            }
            is TyBasic.Dict -> {
                val ir = intersects(Ty.basic(index.node.dupe()), array.key.toTy())
                if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                if (!ir.getOrThrow()) {
                    return kotlin.Result.failure(TypingNoContextError)
                }
                kotlin.Result.success(array.value.toTy())
            }
            is TyBasic.Set -> {
                val ir = intersects(Ty.basic(index.node.dupe()), array.item.toTy())
                if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                if (!ir.getOrThrow()) {
                    return kotlin.Result.failure(TypingNoContextError)
                }
                kotlin.Result.success(array.item.toTy())
            }
            is TyBasic.StarlarkValue ->
                kotlin.Result.success(array.value.index(index.node))
            is TyBasic.Custom ->
                kotlin.Result.success(array.custom.indexDyn(index.node, this))
        }
    }

    internal fun exprIndex(
        span: Span,
        array: Ty,
        index: Spanned<Ty>,
    ): kotlin.Result<Ty> {
        if (array.isAny() || array.isNever()) {
            return kotlin.Result.success(array)
        }
        if (index.isNever()) {
            return kotlin.Result.success(Ty.never())
        }

        val good = mutableListOf<Ty>()
        for (arrayBasic in array.iterUnion()) {
            for (indexBasic in index.node.iterUnion()) {
                val result = exprIndexTy(
                    arrayBasic,
                    Spanned(indexBasic, index.span),
                )
                if (result.isSuccess) {
                    good.add(result.getOrThrow())
                }
            }
        }

        return if (good.isEmpty()) {
            kotlin.Result.failure(Exception(mkErrorAsMaybeInternal(
                span,
                TypingOracleCtxError.MissingIndexOperator(
                    ty = array,
                    index = index.node,
                ),
            ).toString()))
        } else {
            kotlin.Result.success(Ty.unions(good))
        }
    }

    private fun exprSliceBasic(array: TyBasic): kotlin.Result<Ty> {
        return if (array is TyBasic.StarlarkValue) {
            array.value.slice()
        } else if (array.isTuple() || array.isList()) {
            kotlin.Result.success(Ty.basic(array.dupe()))
        } else {
            kotlin.Result.failure(TypingNoContextError)
        }
    }

    internal fun exprSlice(span: Span, array: Ty): kotlin.Result<Ty> {
        val result = array.typecheckUnionSimple { basic -> exprSliceBasic(basic) }
        return if (result.isSuccess) {
            result
        } else {
            kotlin.Result.failure(Exception(mkError(
                span,
                TypingOracleCtxError.MissingSliceOperator(ty = array),
            ).message))
        }
    }

    private fun exprDotBasic(array: TyBasic, attr: String): kotlin.Result<Ty> {
        return when (array) {
            is TyBasic.Any, is TyBasic.Callable, is TyBasic.Iter, is TyBasic.Type ->
                kotlin.Result.success(Ty.any())
            is TyBasic.StarlarkValue -> array.value.attr(attr)
            is TyBasic.Tuple -> kotlin.Result.failure(TypingNoContextError)
            is TyBasic.List -> when (attr) {
                "pop" -> kotlin.Result.success(Ty.function(
                    ParamSpec.posOnly(emptyList(), listOf(Ty.int())),
                    array.item.toTy(),
                ))
                "index" -> kotlin.Result.success(Ty.function(
                    ParamSpec.posOnly(listOf(array.item.toTy()), listOf(Ty.int())),
                    Ty.int(),
                ))
                "remove" -> kotlin.Result.success(Ty.function(
                    ParamSpec.posOnly(listOf(array.item.toTy()), emptyList()),
                    Ty.none(),
                ))
                else -> TyStarlarkValue.new<ListType>().attr(attr)
            }
            is TyBasic.Dict -> when (attr) {
                "get" -> kotlin.Result.success(Ty.union2(
                    Ty.function(
                        ParamSpec.posOnly(listOf(array.key.toTy()), emptyList()),
                        Ty.union2(array.value.toTy(), Ty.none()),
                    ),
                    // This second signature is a bit too lax, but get with a default is much rarer
                    Ty.function(ParamSpec.posOnly(listOf(array.key.toTy(), Ty.any()), emptyList()), Ty.any()),
                ))
                "keys" -> kotlin.Result.success(Ty.function(
                    ParamSpec.empty(),
                    Ty.basic(TyBasic.List(array.key.dupe())),
                ))
                "values" -> kotlin.Result.success(Ty.function(
                    ParamSpec.empty(),
                    Ty.basic(TyBasic.List(array.value.dupe())),
                ))
                "items" -> kotlin.Result.success(Ty.function(
                    ParamSpec.empty(),
                    Ty.list(Ty.tuple(listOf(array.key.toTy(), array.value.toTy()))),
                ))
                "popitem" -> kotlin.Result.success(Ty.function(
                    ParamSpec.empty(),
                    Ty.tuple(listOf(array.key.toTy(), array.value.toTy())),
                ))
                else -> TyStarlarkValue.new<MutableDictType>().attr(attr)
            }
            is TyBasic.Custom -> array.custom.attributeDyn(attr)
            is TyBasic.Set -> TyStarlarkValue.new<MutableSetType>().attr(attr)
        }
    }

    internal fun exprDot(span: Span, array: Ty, attr: String): kotlin.Result<Ty> {
        val result = array.typecheckUnionSimple { basic -> exprDotBasic(basic, attr) }
        return if (result.isSuccess) {
            result
        } else {
            kotlin.Result.failure(Exception(mkError(
                span,
                TypingOracleCtxError.AttributeNotAvailable(ty = array.clone(), attr = attr),
            ).message))
        }
    }

    private fun exprUnOpBasic(ty: TyBasic, unOp: TypingUnOp): kotlin.Result<Ty> {
        return when (ty) {
            is TyBasic.StarlarkValue -> {
                val result = ty.value.unOp(unOp)
                if (result.isSuccess) {
                    kotlin.Result.success(Ty.basic(TyBasic.StarlarkValue(result.getOrThrow())))
                } else {
                    kotlin.Result.failure(TypingNoContextError)
                }
            }
            else -> kotlin.Result.failure(TypingNoContextError)
        }
    }

    internal fun exprUnOp(span: Span, ty: Ty, unOp: TypingUnOp): kotlin.Result<Ty> {
        val result = ty.typecheckUnionSimple { basic -> exprUnOpBasic(basic, unOp) }
        return if (result.isSuccess) {
            result
        } else {
            kotlin.Result.failure(Exception(mkError(
                span,
                TypingOracleCtxError.UnaryOperatorNotAvailable(ty = ty, unOp = unOp),
            ).message))
        }
    }

    private fun exprBinOpTyBasicLhs(
        lhs: TyBasic,
        binOp: TypingBinOp,
        rhs: Spanned<TyBasic>,
    ): kotlin.Result<Ty> {
        return when (lhs) {
            is TyBasic.Any, is TyBasic.Iter, is TyBasic.Callable, is TyBasic.Type ->
                kotlin.Result.success(Ty.any())
            is TyBasic.StarlarkValue ->
                kotlin.Result.success(lhs.value.binOp(binOp, rhs.node))
            is TyBasic.List -> when (binOp) {
                TypingBinOp.Less -> {
                    val ir = intersectsBasic(lhs, rhs.node)
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.bool())
                    else kotlin.Result.failure(TypingNoContextError)
                }
                TypingBinOp.In -> {
                    val ir = intersects(lhs.item.toTy(), Ty.basic(rhs.node.dupe()))
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.bool())
                    else kotlin.Result.failure(TypingNoContextError)
                }
                TypingBinOp.Add -> {
                    val ir = intersectsBasic(rhs.node, TyBasic.anyList())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) {
                        val iterResult = iterItemBasic(rhs.node)
                        if (iterResult.isFailure) return kotlin.Result.failure(iterResult.exceptionOrNull()!!)
                        kotlin.Result.success(Ty.list(Ty.union2(lhs.item.toTy(), iterResult.getOrThrow())))
                    } else {
                        kotlin.Result.failure(TypingNoContextError)
                    }
                }
                TypingBinOp.Mul -> {
                    val ir = intersectsBasic(rhs.node, TyBasic.int())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.basic(lhs.dupe()))
                    else kotlin.Result.failure(TypingNoContextError)
                }
                else -> kotlin.Result.success(TyStarlarkValue.new<ListType>().binOp(binOp, rhs.node))
            }
            is TyBasic.Tuple ->
                kotlin.Result.success(TyStarlarkValue.new<TupleType>().binOp(binOp, rhs.node))
            is TyBasic.Dict -> when (binOp) {
                TypingBinOp.BitOr -> {
                    val ir = intersectsBasic(rhs.node, TyBasic.anyDict())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) {
                        kotlin.Result.success(Ty.union2(
                            Ty.dict(lhs.key.toTy(), lhs.value.toTy()),
                            Ty.basic(rhs.node.dupe()),
                        ))
                    } else {
                        kotlin.Result.failure(TypingNoContextError)
                    }
                }
                TypingBinOp.In -> {
                    val ir = intersects(Ty.basic(rhs.node.dupe()), lhs.key.toTy())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.bool())
                    else kotlin.Result.failure(TypingNoContextError)
                }
                else -> kotlin.Result.success(TyStarlarkValue.new<MutableDictType>().binOp(binOp, rhs.node))
            }
            is TyBasic.Custom ->
                kotlin.Result.success(lhs.custom.binOpDyn(binOp, rhs.node, this))
            is TyBasic.Set -> when (binOp) {
                TypingBinOp.In -> {
                    val ir = intersects(Ty.basic(rhs.node.dupe()), lhs.item.toTy())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.bool())
                    else kotlin.Result.failure(TypingNoContextError)
                }
                TypingBinOp.BitXor, TypingBinOp.BitAnd, TypingBinOp.Sub, TypingBinOp.BitOr -> {
                    val ir = intersectsBasic(rhs.node, TyBasic.anySet())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) {
                        kotlin.Result.success(Ty.union2(
                            Ty.set(lhs.item.toTy()),
                            Ty.basic(rhs.node.dupe()),
                        ))
                    } else {
                        kotlin.Result.failure(TypingNoContextError)
                    }
                }
                else -> kotlin.Result.success(TyStarlarkValue.new<MutableSetType>().binOp(binOp, rhs.node))
            }
        }
    }

    private fun exprBinOpTyBasicRhs(
        lhs: TyBasic,
        binOp: TypingBinOp,
        rhs: TyBasic,
    ): kotlin.Result<Ty> {
        return when (rhs) {
            is TyBasic.StarlarkValue ->
                kotlin.Result.success(rhs.value.rbinOp(binOp, lhs))
            is TyBasic.List -> when (binOp) {
                TypingBinOp.Mul -> {
                    val ir = intersectsBasic(lhs, TyBasic.int())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.basic(rhs.dupe()))
                    else kotlin.Result.failure(TypingNoContextError)
                }
                else -> kotlin.Result.success(TyStarlarkValue.new<ListType>().rbinOp(binOp, lhs))
            }
            is TyBasic.Tuple -> when (binOp) {
                TypingBinOp.Mul -> {
                    val ir = intersectsBasic(lhs, TyBasic.int())
                    if (ir.isFailure) return kotlin.Result.failure(ir.exceptionOrNull()!!)
                    if (ir.getOrThrow()) kotlin.Result.success(Ty.anyTuple())
                    else kotlin.Result.failure(TypingNoContextError)
                }
                else -> kotlin.Result.success(TyStarlarkValue.tuple().rbinOp(binOp, lhs))
            }
            else -> kotlin.Result.failure(TypingNoContextError)
        }
    }

    private fun exprBinOpTyBasic(
        span: Span,
        lhs: Spanned<TyBasic>,
        binOp: TypingBinOp,
        rhs: Spanned<TyBasic>,
    ): kotlin.Result<Ty> {
        if (lhs.node is TyBasic.Any) {
            return kotlin.Result.success(Ty.any())
        }

        val lhsResult = exprBinOpTyBasicLhs(lhs.node, binOp, rhs)
        if (lhsResult.isSuccess) return lhsResult

        val rhsResult = exprBinOpTyBasicRhs(lhs.node, binOp, rhs.node)
        if (rhsResult.isSuccess) return rhsResult

        return kotlin.Result.failure(Exception(mkErrorAsMaybeInternal(
            span,
            TypingOracleCtxError.BinaryOperatorNotAvailable(
                binOp = binOp,
                left = Ty.basic(lhs.node.dupe()),
                right = Ty.basic(rhs.node.dupe()),
            ),
        ).toString()))
    }

    internal fun exprBinOpTy(
        span: Span,
        lhs: Spanned<Ty>,
        binOp: TypingBinOp,
        rhs: Spanned<Ty>,
    ): kotlin.Result<Ty> {
        if (lhs.isNever() || rhs.isNever()) {
            return when {
                binOp.alwaysBool() -> kotlin.Result.success(Ty.bool())
                else -> kotlin.Result.success(Ty.never())
            }
        }

        val good = mutableListOf<Ty>()
        for (lhsI in lhs.node.iterUnion()) {
            for (rhsI in rhs.node.iterUnion()) {
                val lhsSpanned = Spanned(lhsI, lhs.span)
                val rhsSpanned = Spanned(rhsI, rhs.span)
                val result = exprBinOpTyBasic(span, lhsSpanned, binOp, rhsSpanned)
                if (result.isSuccess) {
                    good.add(result.getOrThrow())
                }
            }
        }

        return if (good.isEmpty()) {
            kotlin.Result.failure(Exception(mkErrorAsMaybeInternal(
                span,
                TypingOracleCtxError.BinaryOperatorNotAvailable(
                    left = lhs.node,
                    right = rhs.node,
                    binOp = binOp,
                ),
            ).toString()))
        } else {
            when {
                binOp.alwaysBool() -> kotlin.Result.success(Ty.bool())
                else -> kotlin.Result.success(Ty.unions(good))
            }
        }
    }

    internal fun exprBinOp(
        span: Span,
        lhs: Spanned<Ty>,
        binOp: BinOp,
        rhs: Spanned<Ty>,
    ): kotlin.Result<Ty> {
        val boolRet = if (lhs.isNever() || rhs.isNever()) Ty.never() else Ty.bool()
        return when (binOp) {
            BinOp.And, BinOp.Or -> {
                if (lhs.isNever()) kotlin.Result.success(Ty.never())
                else kotlin.Result.success(Ty.union2(lhs.node, rhs.node))
            }
            BinOp.Equal, BinOp.NotEqual -> {
                // It's not an error to compare two different types, but it is pointless
                @Suppress("UNCHECKED_CAST")
                val vr = validateType(rhs as Spanned<Ty>, lhs.node)
                if (vr.isFailure) return kotlin.Result.failure(vr.exceptionOrNull()!!)
                kotlin.Result.success(boolRet)
            }
            BinOp.In, BinOp.NotIn -> {
                // We dispatch `x in y` as y.__in__(x) as that's how we validate
                exprBinOpTy(span, rhs, TypingBinOp.In, lhs)
            }
            BinOp.Less, BinOp.LessOrEqual, BinOp.Greater, BinOp.GreaterOrEqual -> {
                exprBinOpTy(span, lhs, TypingBinOp.Less, rhs)
            }
            BinOp.Subtract -> exprBinOpTy(span, lhs, TypingBinOp.Sub, rhs)
            BinOp.Add -> exprBinOpTy(span, lhs, TypingBinOp.Add, rhs)
            BinOp.Multiply -> exprBinOpTy(span, lhs, TypingBinOp.Mul, rhs)
            BinOp.Percent -> exprBinOpTy(span, lhs, TypingBinOp.Percent, rhs)
            BinOp.Divide -> exprBinOpTy(span, lhs, TypingBinOp.Div, rhs)
            BinOp.FloorDivide -> exprBinOpTy(span, lhs, TypingBinOp.FloorDiv, rhs)
            BinOp.BitAnd -> exprBinOpTy(span, lhs, TypingBinOp.BitAnd, rhs)
            BinOp.BitOr -> exprBinOpTy(span, lhs, TypingBinOp.BitOr, rhs)
            BinOp.BitXor -> exprBinOpTy(span, lhs, TypingBinOp.BitXor, rhs)
            BinOp.LeftShift -> exprBinOpTy(span, lhs, TypingBinOp.LeftShift, rhs)
            BinOp.RightShift -> exprBinOpTy(span, lhs, TypingBinOp.RightShift, rhs)
        }
    }

    /// Returns false on Void, since that is definitely not a list
    internal fun probablyAList(ty: Ty): kotlin.Result<Boolean> {
        if (ty.isNever()) {
            return kotlin.Result.success(false)
        }
        return intersects(ty, Ty.list(Ty.any()))
    }

    /// If you get to a point where these types are being checked, might they succeed
    internal fun intersects(xs: Ty, ys: Ty): kotlin.Result<Boolean> {
        if (xs.isAny() || xs.isNever() || ys.isAny() || ys.isNever()) {
            return kotlin.Result.success(true)
        }

        for (x in xs.iterUnion()) {
            for (y in ys.iterUnion()) {
                val result = intersectsBasic(x, y)
                if (result.isFailure) return result
                if (result.getOrThrow()) return kotlin.Result.success(true)
            }
        }
        return kotlin.Result.success(false)
    }

    internal fun intersectsBasic(x: TyBasic, y: TyBasic): kotlin.Result<Boolean> {
        if (x == y) return kotlin.Result.success(true)
        val left = intersectsOneSide(x, y)
        if (left.isFailure) return left
        if (left.getOrThrow()) return kotlin.Result.success(true)
        return intersectsOneSide(y, x)
    }

    private fun paramsIntersect(x: ParamSpec, y: ParamSpec): kotlin.Result<Boolean> {
        // Fast path.
        if (x == y) return kotlin.Result.success(true)
        // Another fast path.
        if (x.isAny() || y.isAny()) return kotlin.Result.success(true)

        val xParts = x.allRequiredPosOnlyNamedOnly()
        val yParts = y.allRequiredPosOnlyNamedOnly()

        return when {
            xParts != null && yParts != null -> {
                val (xP, xN) = xParts
                val (yP, yN) = yParts
                if (xP.size != yP.size || xN.size != yN.size) {
                    return kotlin.Result.success(false)
                }
                for ((xTy, yTy) in xP.zip(yP)) {
                    val ir = intersects(xTy, yTy)
                    if (ir.isFailure) return ir
                    if (!ir.getOrThrow()) return kotlin.Result.success(false)
                }
                val yNMap = yN.toMap()
                for ((name, xTy) in xN) {
                    val yTy = yNMap[name]
                    if (yTy != null) {
                        val ir = intersects(xTy, yTy)
                        if (ir.isFailure) return ir
                        if (!ir.getOrThrow()) return kotlin.Result.success(false)
                    } else {
                        return kotlin.Result.success(false)
                    }
                }
                kotlin.Result.success(true)
            }
            xParts != null -> {
                val (xP, xN) = xParts
                paramsAllPosOnlyNamedOnlyIntersect(xP, xN, y)
            }
            yParts != null -> {
                val (yP, yN) = yParts
                paramsAllPosOnlyNamedOnlyIntersect(yP, yN, x)
            }
            else -> {
                // The rest is hard to check, but required pos-only in signatures
                // is what we need the most.
                kotlin.Result.success(true)
            }
        }
    }

    private fun paramsAllPosOnlyNamedOnlyIntersect(
        xP: List<Ty>,
        xN: List<Pair<String, Ty>>,
        y: ParamSpec,
    ): kotlin.Result<Boolean> {
        val callArgs = TyCallArgs(
            pos = xP.map { ty ->
                Spanned(ty.dupe(), Span.default())
            }.toMutableList(),
            named = xN.map { (name, ty) ->
                Spanned(Pair(name, ty.dupe()), Span.default())
            }.toMutableList(),
            args = null,
            kwargs = null,
        )
        val result = validateArgs(y, callArgs, Span.default())
        return if (result.isSuccess) {
            kotlin.Result.success(true)
        } else {
            kotlin.Result.success(false)
        }
    }

    internal fun callablesIntersect(x: TyCallable, y: TyCallable): kotlin.Result<Boolean> {
        val pi = paramsIntersect(x.params(), y.params())
        if (pi.isFailure) return pi
        if (!pi.getOrThrow()) return kotlin.Result.success(false)
        return intersects(x.result(), y.result())
    }

    /// We consider two type intersecting if either side knows if they intersect.
    /// This function checks the left side.
    private fun intersectsOneSide(x: TyBasic, y: TyBasic): kotlin.Result<Boolean> {
        return when {
            x is TyBasic.Any -> kotlin.Result.success(true)
            x is TyBasic.List && y is TyBasic.List ->
                intersects(x.item.toTy(), y.item.toTy())
            x is TyBasic.List && y is TyBasic.StarlarkValue ->
                kotlin.Result.success(y.value.isList())
            x is TyBasic.List ->
                kotlin.Result.success(false)
            x is TyBasic.Set && y is TyBasic.Set ->
                intersects(x.item.toTy(), y.item.toTy())
            x is TyBasic.Set && y is TyBasic.StarlarkValue ->
                kotlin.Result.success(y.value.isSet())
            x is TyBasic.Set ->
                kotlin.Result.success(false)
            x is TyBasic.Dict && y is TyBasic.Dict -> {
                val ki = intersects(x.key.toTy(), y.key.toTy())
                if (ki.isFailure) return ki
                if (!ki.getOrThrow()) return kotlin.Result.success(false)
                intersects(x.value.toTy(), y.value.toTy())
            }
            x is TyBasic.Dict && y is TyBasic.StarlarkValue ->
                kotlin.Result.success(y.value.isDict())
            x is TyBasic.Dict ->
                kotlin.Result.success(false)
            x is TyBasic.Tuple && y is TyBasic.Tuple ->
                TyTuple.intersects(x.tuple, y.tuple, this)
            x is TyBasic.Tuple && y is TyBasic.StarlarkValue ->
                kotlin.Result.success(y.value.isTuple())
            x is TyBasic.Tuple ->
                kotlin.Result.success(false)
            x is TyBasic.Iter && y is TyBasic.Iter ->
                intersects(x.item.toTy(), y.item.toTy())
            x is TyBasic.Iter -> {
                val yIterItem = iterItemBasic(y)
                if (yIterItem.isSuccess) intersects(x.item.toTy(), yIterItem.getOrThrow())
                else kotlin.Result.success(false)
            }
            y is TyBasic.Iter -> {
                val xIterItem = iterItemBasic(x)
                if (xIterItem.isSuccess) intersects(y.item.toTy(), xIterItem.getOrThrow())
                else kotlin.Result.success(false)
            }
            x is TyBasic.Callable && y is TyBasic.Callable ->
                callablesIntersect(x.callable, y.callable)
            x is TyBasic.Callable && y is TyBasic.Custom -> {
                // Handled when custom is lhs
                kotlin.Result.success(false)
            }
            x is TyBasic.Callable ->
                kotlin.Result.success(false)
            x is TyBasic.Custom ->
                x.custom.intersectsWith(y, this)
            x is TyBasic.StarlarkValue && y is TyBasic.Callable ->
                kotlin.Result.success(x.value.isCallable())
            x is TyBasic.StarlarkValue ->
                kotlin.Result.success(false)
            x is TyBasic.Type && y is TyBasic.StarlarkValue ->
                kotlin.Result.success(y.value.isType())
            x is TyBasic.Type ->
                kotlin.Result.success(true)
            else -> kotlin.Result.success(false)
        }
    }
}
