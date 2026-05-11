// port-lint: source eval/compiler/def.rs
package io.github.kotlinmania.starlark.eval.compiler

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

// Implementation of `def`.

import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlarkmap.Hashed
import io.github.kotlinmania.starlarkmap.StarlarkHasher
import io.github.kotlinmania.starlark.docs.DocFunction
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocStringKind
import io.github.kotlinmania.starlark.docs.fromDocstring
import io.github.kotlinmania.starlark.environment.FrozenModuleData
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark.eval.runtime.frozenfilespan.FrozenFileSpan
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsImpl
import io.github.kotlinmania.starlark.eval.runtime.ResolvedArgName
import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.eval.bc.Bc
import io.github.kotlinmania.starlark.eval.bc.allocaFrame
import io.github.kotlinmania.starlark.eval.bc.compiler.asBc
import io.github.kotlinmania.starlark.eval.compiler.optctx.OptCtx
import io.github.kotlinmania.starlark.eval.compiler.optctx.OptCtxEvalForOptimizeOnFreeze
import io.github.kotlinmania.starlark.docs.extractRawStarlarkDocstring
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.typing.DefParam
import io.github.kotlinmania.starlark.typing.DefParamKind
import io.github.kotlinmania.starlark.typing.DefParamIndices
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.ParamIsRequired
import io.github.kotlinmania.starlark.values.AtomicFrozenRefOption
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.values.toValue
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.TypeExprP

// ---- DefError ----

/**
 * Errors specific to `def` compilation and invocation.
 */
private sealed class DefError(message: String) : Exception(message) {
    class CheckReturnTypeNoType :
        DefError("Function has no type, while function was compiled with return type (internal error)")
}

// ---- StmtCompiledCell ----

/**
 * Store frozen [Bc].
 * This is initialized in [DefGen<FrozenValue>.postFreeze].
 */
internal class StmtCompiledCell {
    private var bc: Bc = Bc()

    companion object {
        fun new(): StmtCompiledCell = StmtCompiledCell()
    }

    /**
     * Set the bytecode. This function is dangerous if another thread is executing the stmt.
     */
    fun set(value: Bc) {
        bc = value
    }

    fun get(): Bc = bc
}

// ---- ParameterName ----

/**
 * A compiled parameter name together with its captured status.
 */
internal data class ParameterName(
    val name: String,
    val captured: Captured,
)

// ---- ParameterCompiled ----

/**
 * A compiled parameter: normal (positional/keyword), `*args`, or `**kwargs`.
 */
internal sealed class ParameterCompiled<out T> {
    /**
     * A normal parameter, optionally with a type annotation and default value.
     */
    data class Normal<T>(
        /** Name. */
        val paramName: ParameterName,
        /** Type. */
        val type: TypeCompiled?,
        /** Default value. */
        val defaultValue: T?,
    ) : ParameterCompiled<T>()

    /**
     * A `*args` parameter.
     */
    data class Args<T>(
        val paramName: ParameterName,
        val type: TypeCompiled?,
    ) : ParameterCompiled<T>()

    /**
     * A `**kwargs` parameter.
     */
    data class KwArgs<T>(
        val paramName: ParameterName,
        val type: TypeCompiled?,
    ) : ParameterCompiled<T>()
}

/**
 * Map the expression type of this parameter using [f].
 */
internal fun <T, U> ParameterCompiled<T>.mapExpr(f: (T) -> U): ParameterCompiled<U> {
    return when (this) {
        is ParameterCompiled.Normal -> ParameterCompiled.Normal(paramName, type, defaultValue?.let(f))
        is ParameterCompiled.Args -> ParameterCompiled.Args(paramName, type)
        is ParameterCompiled.KwArgs -> ParameterCompiled.KwArgs(paramName, type)
    }
}

/**
 * Returns `true` if this parameter accepts a positional argument.
 */
internal fun <T> ParameterCompiled<T>.acceptsPositional(): Boolean {
    return this is ParameterCompiled.Normal
}

/**
 * Returns the [Captured] status of this parameter.
 */
internal fun <T> ParameterCompiled<T>.captured(): Captured {
    return nameTy().first.captured
}

/**
 * Returns a pair of the parameter name and its optional type.
 */
internal fun <T> ParameterCompiled<T>.nameTy(): Pair<ParameterName, TypeCompiled?> {
    return when (this) {
        is ParameterCompiled.Normal -> Pair(paramName, type)
        is ParameterCompiled.Args -> Pair(paramName, type)
        is ParameterCompiled.KwArgs -> Pair(paramName, type)
    }
}

/**
 * Returns `true` if this parameter has a type annotation.
 */
internal fun <T> ParameterCompiled<T>.hasType(): Boolean {
    return nameTy().second != null
}

/**
 * Returns the [Ty] for this parameter, or [Ty.any] if no type annotation is present.
 */
internal fun <T> ParameterCompiled<T>.ty(): Ty {
    val (_, t) = nameTy()
    return t?.asTy() ?: Ty.any()
}

/**
 * Returns whether this parameter is required (i.e., has no default value).
 */
internal fun <T> ParameterCompiled<T>.required(): ParamIsRequired {
    return when (this) {
        is ParameterCompiled.Normal -> {
            if (defaultValue == null) ParamIsRequired.Yes else ParamIsRequired.No
        }
        is ParameterCompiled.Args -> ParamIsRequired.No
        is ParameterCompiled.KwArgs -> ParamIsRequired.No
    }
}

/**
 * Returns `true` if this parameter is `*args` or `**kwargs`.
 */
internal fun <T> ParameterCompiled<T>.isStarOrStarStar(): Boolean {
    return this is ParameterCompiled.Args || this is ParameterCompiled.KwArgs
}

// ---- ParametersCompiled ----

/**
 * All compiled parameters for a function definition.
 */
internal data class ParametersCompiled<T>(
    val params: List<IrSpanned<ParameterCompiled<T>>>,
    val indices: DefParamIndices,
) {
    /**
     * How many expressions this parameters references (default values and types).
     */
    fun countExprs(): Int {
        var count = 0
        for (p in params) {
            p.node.mapExpr<T, Unit> { count += 1 }
        }
        return count
    }

    /**
     * How many parameter variables?
     */
    fun countParamVariables(): Int = params.size

    /**
     * Any parameter has type annotation?
     */
    fun hasTypes(): Boolean = params.any { it.node.hasType() }

    /**
     * Has `*args` or `**kwargs` parameter? `*` (bare star) is fine.
     */
    fun hasArgsOrKwargs(): Boolean {
        return params.any { it.node.isStarOrStarStar() }
    }

    /**
     * Returns indices of parameters which are captured in nested defs.
     */
    fun parameterCaptures(): List<LocalSlotId> {
        return params.mapIndexedNotNull { i, p ->
            if (p.node.captured() == Captured.Yes) {
                LocalSlotId(i.toUInt())
            } else {
                null
            }
        }
    }

    /**
     * Converts the compiled parameters to a [ParamSpec] for type checking.
     */
    fun toTyParams(): ParamSpec {
        return ParamSpec.newParts(
            posOnly = indices.posOnly().map { i ->
                val p = params[i].node
                Pair(p.required(), p.ty())
            },
            posOrName = indices.posOrNamed().map { i ->
                val p = params[i].node
                Triple(p.nameTy().first.name, p.required(), p.ty())
            },
            args = indices.args?.let { i ->
                params[i.toInt()].node.ty()
            },
            namedOnly = indices.namedOnly(params.size).map { i ->
                val p = params[i].node
                Triple(p.nameTy().first.name, p.required(), p.ty())
            },
            kwargs = indices.kwargs?.let { i ->
                params[i.toInt()].node.ty()
            },
        )
    }
}

// ---- CopySlotFromParent ----

/**
 * Copy local variable slot to nested function.
 */
internal data class CopySlotFromParent(
    /** Slot in the outer function. */
    val parent: LocalSlotIdCapturedOrNot,
    /** Slot in the nested function. */
    val child: LocalSlotIdCapturedOrNot,
)

// ---- DefInfo ----

/**
 * Static info for `def`, `lambda` or module.
 *
 * This data is created during compilation and shared by all instances of a given
 * `def`. For example, a `lambda` inside a loop creates multiple function values
 * that all reference the same [DefInfo].
 */
internal class DefInfo(
    val name: FrozenStringValue,
    /** Span of function signature. */
    val signatureSpan: FrozenFileSpan,
    /** Indices of parameters, which are captured in nested defs. */
    val parameterCaptures: List<LocalSlotId>,
    /** Type of this function, for the typechecker. */
    val ty: Ty,
    /** Codemap of the file where the function is declared. */
    val codemap: FrozenRef<CodeMap>,
    /** The raw docstring pulled out of the AST. */
    val docstring: String?,
    /**
     * Slots this scope uses, including for parameters and [parent].
     * Indexed by [LocalSlotId], values are variable names.
     */
    val used: List<FrozenStringValue>,
    /**
     * Slots to copy from the parent.
     * Module-level identifiers are not copied over, to avoid excess copying.
     */
    val parent: List<CopySlotFromParent>,
    /** Statement compiled for non-frozen def. */
    val stmtCompiled: Bc,
    /**
     * The compiled expression for the body of this definition, to be run
     * after the parameters are evaluated.
     */
    val bodyStmts: StmtsCompiled,
    /** How to compile the statement on freeze. */
    val stmtCompileContext: StmtCompileContext,
    /** Function can be inlined. */
    val inlineDefBody: InlineDefBody?,
    /**
     * Globals captured during function or module creation.
     * Only needed for debugger evaluation.
     */
    val globals: FrozenRef<Globals>,
) {
    override fun toString(): String = "DefInfo"

    companion object {
        private val EMPTY: DefInfo by lazy {
            DefInfo(
                name = FrozenStringValue.default(),
                signatureSpan = FrozenFileSpan.default(),
                parameterCaptures = emptyList(),
                ty = Ty.any(),
                codemap = FrozenRef(CodeMap.new("", "")),
                docstring = null,
                used = emptyList(),
                parent = emptyList(),
                stmtCompiled = Bc(),
                bodyStmts = StmtsCompiled.empty(),
                stmtCompileContext = StmtCompileContext(),
                inlineDefBody = null,
                globals = FrozenRef(Globals.empty),
            )
        }

        fun empty(): DefInfo = EMPTY

        /**
         * Create a [DefInfo] for a module-level scope.
         */
        fun forModule(
            codemap: FrozenRef<CodeMap>,
            localNames: List<FrozenStringValue>,
            parent: List<CopySlotFromParent>,
            globals: FrozenRef<Globals>,
        ): DefInfo {
            return DefInfo(
                name = FrozenStringValue.default(),
                signatureSpan = FrozenFileSpan.default(),
                parameterCaptures = emptyList(),
                ty = Ty.any(),
                codemap = codemap,
                docstring = null,
                used = localNames,
                parent = parent,
                stmtCompiled = Bc(),
                bodyStmts = StmtsCompiled.empty(),
                stmtCompileContext = StmtCompileContext(),
                inlineDefBody = null,
                globals = globals,
            )
        }
    }
}

// ---- DefCompiled ----

/**
 * The compiled representation of a `def` or `lambda` expression,
 * ready to be emitted as an [ExprCompiled.Def].
 */
internal data class DefCompiled(
    val functionName: String,
    val params: ParametersCompiled<IrSpanned<ExprCompiled>>,
    val returnType: TypeCompiled?,
    val info: FrozenRef<DefInfo>,
)

// ---- Compiler extensions for parameter compilation ----

/**
 * Compile a parameter name from a CST assignment identifier.
 */
internal fun Compiler.parameterName(ident: Spanned<AssignIdentP<CstPayload, *>>): ParameterName {
    val bindingId = ident.node.payload as? BindingId ?: error("no binding for parameter")
    val binding = this.scopeData.getBinding(bindingId)
    return ParameterName(
        name = ident.node.ident,
        captured = binding.captured,
    )
}

/**
 * Compile a single function parameter.
 */
internal fun Compiler.parameter(
    x: Spanned<DefParam>,
): IrSpanned<ParameterCompiled<IrSpanned<ExprCompiled>>> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, x.span))
    val pName = parameterName(x.node.ident)
    val node: ParameterCompiled<IrSpanned<ExprCompiled>> = when (val kind = x.node.kind) {
        is DefParamKind.Regular -> ParameterCompiled.Normal(
            pName,
            this.exprForType(x.node.ty)?.node,
            kind.defaultValue?.let { d -> this.expr(d).getOrThrow() },
        )
        is DefParamKind.Args -> ParameterCompiled.Args(
            pName,
            this.exprForType(x.node.ty)?.node,
        )
        is DefParamKind.Kwargs -> ParameterCompiled.KwArgs(
            pName,
            this.exprForType(x.node.ty)?.node,
        )
    }
    return IrSpanned(span, node)
}

/**
 * Compile a function definition (`def` or `lambda`).
 *
 * This is the main entry point for function compilation. It:
 * 1. Compiles parameters in the parent scope
 * 2. Enters the function scope and compiles the body
 * 3. Optionally attempts to create an inline body
 * 4. Returns an [ExprCompiled.Def] wrapping all compiled information
 */
internal fun Compiler.function(
    name: String,
    signatureSpan: FrozenFileSpan,
    scopeId: ScopeId,
    params: List<Spanned<ParameterP<CstPayload>>>,
    returnType: Spanned<TypeExprP<CstPayload, *>>?,
    suite: Spanned<StmtP<CstPayload>>,
): ExprCompiled {
    val file = this.codemap.asRef().fileSpan(suite.span)
    val functionName = "${file.file.filename}.$name"
    val frozenName = this.eval.frozenHeap().allocStrIntern(name)

    val defParams = unpackDefParamsForCompiler(params, this.codemap.asRef())

    // The parameters run in the scope of the parent, so compile them with the outer scope
    val compiler = this
    val compiledParams = defParams.first.map { x -> compiler.parameter(x) }
    val parametersCompiled = ParametersCompiled(compiledParams, defParams.second)
    val compiledReturnType = this.exprForType(returnType)?.node

    val ty = Ty.function(
        parametersCompiled.toTyParams(),
        compiledReturnType?.asTy() ?: Ty.any(),
    )

    this.enterScope(scopeId)

    val docstring = DocString.extractRawStarlarkDocstring(suite)
    val body = this.stmt(suite, false).getOrThrow()
    val exitedScopeId = this.exitScope()
    val scopeNames = this.scopeData.getScope(exitedScopeId)

    val hasTypes = compiledReturnType != null || parametersCompiled.hasTypes()

    val inlineDef = if (hasTypes) {
        // It is harder to inline if a function declares parameter types or return type.
        null
    } else {
        inlineDefBody(parametersCompiled, body)
    }

    val paramCount = parametersCompiled.countParamVariables()

    val used = scopeNames.used
    val info = DefInfo(
        name = frozenName,
        signatureSpan = signatureSpan,
        parameterCaptures = parametersCompiled.parameterCaptures(),
        ty = ty,
        codemap = this.codemap,
        docstring = docstring,
        used = used,
        parent = scopeNames.parent,
        stmtCompiled = body.asBc(
            this.compileContext(compiledReturnType != null),
            FrozenRef(used),
            paramCount,
            this.eval.moduleEnv.frozenHeap(),
        ),
        bodyStmts = body,
        inlineDefBody = inlineDef,
        stmtCompileContext = this.compileContext(compiledReturnType != null),
        globals = this.globals,
    )
    val frozenInfo = FrozenRef(info)

    return ExprCompiled.Def(DefCompiled(
        functionName = functionName,
        params = parametersCompiled,
        returnType = compiledReturnType,
        info = frozenInfo,
    ))
}

/**
 * Unpack CST parameters into DefParam list + DefParamIndices for the compiler.
 */
private fun unpackDefParamsForCompiler(
    params: List<Spanned<ParameterP<CstPayload>>>,
    codemap: CodeMap,
): Pair<List<Spanned<DefParam>>, DefParamIndices> {
    val defParams = mutableListOf<Spanned<DefParam>>()
    var numPositional: UInt = 0u
    var numPositionalOnly: UInt = 0u
    var args: UInt? = null
    var kwargs: UInt? = null
    var seenStar = false
    var seenSlash = false

    for (p in params) {
        when (val param = p.node) {
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Slash -> {
                seenSlash = true
                numPositionalOnly = numPositional
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.NoArgs -> {
                seenStar = true
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Normal -> {
                val mode = if (seenStar) {
                    io.github.kotlinmania.starlark.typing.DefRegularParamMode.NameOnly
                } else {
                    numPositional++
                    io.github.kotlinmania.starlark.typing.DefRegularParamMode.PosOrName
                }
                val kind = DefParamKind.Regular(mode, param.defaultVal)
                defParams.add(Spanned(DefParam(param.name, kind, param.typ), p.span))
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.Args -> {
                seenStar = true
                args = defParams.size.toUInt()
                numPositional = defParams.size.toUInt()
                defParams.add(Spanned(DefParam(param.name, DefParamKind.Args, param.typ), p.span))
            }
            is io.github.kotlinmania.starlark.syntax.ast.ParameterP.KwArgs -> {
                kwargs = defParams.size.toUInt()
                defParams.add(Spanned(DefParam(param.name, DefParamKind.Kwargs, param.typ), p.span))
            }
        }
    }

    if (!seenSlash && !seenStar) {
        numPositionalOnly = 0u
    }
    if (!seenStar && args == null) {
        numPositional = defParams.size.toUInt()
    }

    val indices = DefParamIndices(
        numPositional = numPositional,
        numPositionalOnly = numPositionalOnly,
        args = args,
        kwargs = kwargs,
    )
    return Pair(defParams, indices)
}

// ---- DefGen ----

/**
 * Starlark function internal representation and implementation of
 * [StarlarkValue].
 *
 * This is the runtime representation of a `def` or `lambda` after it has
 * been compiled. It holds the [ParametersSpec] (including default values),
 * type annotations, captured variables, and a reference to the shared
 * [DefInfo].
 *
 * The type parameter [V] is either [Value] (unfrozen) or [FrozenValue] (frozen).
 */
internal class DefGen<V>(
    /** The parameters, `**kwargs` etc including defaults (which are evaluated afresh each time). */
    val parameters: ParametersSpec<V>,
    /**
     * Indices of parameters, which are captured in nested defs.
     * This is a copy of [DefInfo.parameterCaptures].
     */
    private val parameterCaptures: List<LocalSlotId>,
    /**
     * The types of the parameters.
     * Sparse indexed array: `(slotId, argName, typeCompiled)` implies the parameter at
     * `slotId` named `argName` must have the given type.
     */
    private val parameterTypes: List<Triple<LocalSlotId, String, TypeCompiled>>,
    /** The return type annotation for the function. */
    val returnType: TypeCompiled?,
    /**
     * Data created during function compilation but before function instantiation.
     * [DefInfo] can be shared by multiple `def` instances; for example,
     * `lambda` functions can be instantiated multiple times.
     */
    val defInfo: DefInfo,
    /**
     * Any variables captured from the outer scope (nested def/lambda).
     * Values are either [Value] or [FrozenValue] pointing respectively to
     * `ValueCaptured` or `FrozenValueCaptured`.
     */
    private val captured: List<V>,
    /**
     * A reference to the module where the function is defined after the module has been frozen.
     * When the module is not frozen yet, this field contains `null`, and function's module
     * can be accessed from evaluator's module.
     */
    val module: AtomicFrozenRefOption<FrozenModuleData>,
    /**
     * This field is only used in `DefGen<FrozenValue>`. It is populated in [postFreeze].
     */
    internal val optimizedOnFreezeStmt: StmtCompiledCell,
    /** Whether this DefGen holds frozen values. */
    private val frozen: Boolean,
) : ComplexValue, Trace, Freeze<DefGen<FrozenValue>> {

    override fun toString(): String = parameters.signature()

    // Trace implementation: trace all captured Value references.
    override fun trace(tracer: Tracer) {
        // In the unfrozen case, we need to trace captured values.
        // The parameters also contain Values that need tracing.
        // For the frozen case, there's nothing to trace.
        if (!frozen) {
            for (cap in captured) {
                if (cap is Value) {
                    tracer.trace(ValueHolder(cap))
                }
            }
        }
    }

    // Freeze implementation: freeze into DefGen<FrozenValue>.
    override fun freeze(freezer: Freezer): Result<DefGen<FrozenValue>> {
        val frozenParameters = parameters as ParametersSpec<FrozenValue>
        val frozenParameterTypes = parameterTypes.map { (slot, name, ty) ->
            Triple(slot, name, ty.toFrozen(freezer.heap))
        }
        val frozenReturnType = returnType?.toFrozen(freezer.heap)
        val frozenCaptured = (captured as List<Value>).map { v ->
            freezer.freeze(v).getOrElse { return Result.failure(it) }
        }
        return Result.success(DefGen(
            parameters = frozenParameters,
            parameterCaptures = parameterCaptures,
            parameterTypes = frozenParameterTypes,
            returnType = frozenReturnType,
            defInfo = defInfo,
            captured = frozenCaptured,
            module = AtomicFrozenRefOption(module.loadRelaxed()),
            optimizedOnFreezeStmt = optimizedOnFreezeStmt,
            frozen = true,
        ))
    }

    /**
     * Returns the bytecode for this function. For frozen defs, returns
     * the post-freeze optimized bytecode; otherwise, returns the original.
     */
    fun bc(): Bc {
        return if (frozen) {
            optimizedOnFreezeStmt.get()
        } else {
            defInfo.stmtCompiled
        }
    }

    /**
     * Check that the parameter types match the values provided.
     */
    private fun checkParameterTypes(eval: Evaluator): Result<Unit> {
        val start = if (eval.typecheckProfile.enabled) {
            ProfilerInstant.now()
        } else {
            null
        }
        for ((i, argName, ty) in parameterTypes) {
            val v = eval.currentFrame.getSlot(i.toCapturedOrNot())
                ?: error("Not allowed optional unassigned with type annotations on them")
            ty.checkType(v, argName).getOrElse { return Result.failure(it) }
        }
        if (start != null) {
            eval.typecheckProfile.add(defInfo.name, start.elapsed())
        }
        return Result.success(Unit)
    }

    /**
     * Check the return value matches the declared return type.
     */
    fun checkReturnType(ret: Value, eval: Evaluator): Result<Unit> {
        val returnTypeTy = returnType
            ?: return Result.failure(DefError.CheckReturnTypeNoType())
        val start = if (eval.typecheckProfile.enabled) {
            ProfilerInstant.now()
        } else {
            null
        }
        returnTypeTy.checkType(ret, null).getOrElse { return Result.failure(it) }
        if (start != null) {
            eval.typecheckProfile.add(defInfo.name, start.elapsed())
        }
        return Result.success(Unit)
    }

    /**
     * Core invocation implementation. Allocates a frame, collects arguments into
     * local slots, and delegates to [invokeRaw].
     */
    private fun invokeImpl(
        me: Value,
        args: ArgumentsImpl<*>,
        eval: Evaluator,
    ): Result<Value> {
        val bc = bc()
        return allocaFrame(
            eval,
            bc.localCount.toInt(),
            bc.maxStackSize.toInt(),
            bc.maxLoopDepth,
        ) { ev ->
            val slots = ArrayBackedMutableList(ev.currentFrame.localsMut())
            runCatching { parameters.collectInline(args, slots, ev.heap()) }
                .getOrElse { return@allocaFrame Result.failure(it) }
            invokeRaw(me, ev)
        }
    }

    /**
     * Invoke the function with the given arguments.
     *
     * This is a trivial function which delegates to [invokeImpl].
     * [invokeImpl] is called from two places, giving this function a different
     * name makes it easier to see in a profiler.
     */
    fun invokeWithArgs(
        me: Value,
        args: ArgumentsImpl<*>,
        eval: Evaluator,
    ): Result<Value> {
        return invokeImpl(me, args, eval)
    }

    /**
     * Invoke the function, assuming that:
     * - the frame has been allocated and stored in `eval.currentFrame`
     * - the arguments have been collected into the frame
     */
    private fun invokeRaw(
        me: Value,
        eval: Evaluator,
    ): Result<Value> {
        if (parameterTypes.isNotEmpty()) {
            checkParameterTypes(eval).getOrElse { return Result.failure(it) }
        }

        // Parameters are collected into local slots without captures
        // (to avoid even more branches in parameter capture),
        // and this loop wraps captured parameters.
        for (capturedSlot in parameterCaptures) {
            eval.wrapLocalSlotCaptured(capturedSlot)
        }

        // Copy over the parent slots.
        // Explicitly check `captured` is not empty to avoid accessing
        // defInfo.parent which is two indirections.
        if (captured.isNotEmpty()) {
            for ((copy, cap) in defInfo.parent.zip(captured)) {
                val capValue = when (cap) {
                    is Value -> cap
                    is FrozenValue -> cap.toValue()
                    else -> (cap as ValueLike<*>).toValue()
                }
                eval.currentFrame.setSlot(copy.child, capValue)
            }
        }

        if (frozen) {
            check(module.loadRelaxed() != null) { "frozen def must have module set" }
        }

        return eval.evalBc(me, bc())
            .mapCatching { it }
    }

    /**
     * Resolve a named argument to its index in the parameters.
     */
    fun resolveArgName(name: Hashed<String>): ResolvedArgName {
        return parameters.resolveName(name)
    }

    /**
     * Dump debug information about the bytecode.
     */
    fun dumpDebug(): String {
        val sb = StringBuilder()
        sb.appendLine("Bytecode:")
        sb.appendLine("  (debug dump not available)")
        return sb.toString()
    }

    // StarlarkValue implementation

    override val TYPE: String get() = FUNCTION_TYPE

    /**
     * Returns the name used in call stack frames.
     */
    override fun nameForCallStack(_me: Value): String {
        return defInfo.name.asStr()
    }

    /**
     * Invoke this function with the given arguments.
     */
    override fun invoke(me: Value, args: Arguments, eval: Evaluator): Result<Value> {
        return invokeImpl(me, args.inner, eval)
    }

    /**
     * Generate documentation for this function.
     */
    override fun documentation(): DocItem {
        val paramTys = MutableList(parameters.len()) { Ty.any() }
        for ((idx, _, ty) in parameterTypes) {
            // Local slot number for parameter is the same as parameter index.
            paramTys[idx.index.toInt()] = ty.asTy()
        }

        val retType = returnType?.asTy() ?: Ty.any()

        val functionDocs = DocFunction.fromDocstring(
            DocStringKind.Starlark,
            parameters.documentation(paramTys, mutableMapOf()),
            retType,
            defInfo.docstring,
        )

        return DocItem.Member(DocMember.Function(functionDocs))
    }

    /**
     * Returns the type of this function for the typechecker.
     */
    override fun typecheckerTy(): Ty? = defInfo.ty

    /**
     * Write a hash for this function.
     * It's hard to come up with a good hash here, but let's at least make an effort.
     */
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        // Hash the name of the function
        hasher.write(defInfo.name.asStr().encodeToByteArray())
        return Result.success(Unit)
    }
}

// ---- DefLike ----

/**
 * Trait indicating whether a [DefGen] variant is frozen or not.
 */
internal interface DefLike {
    val isFrozen: Boolean
}

// ---- Def.new ----

/**
 * Create a new unfrozen [DefGen], allocating it on the evaluator's heap.
 */
internal fun newDef(
    parameters: ParametersSpec<Value>,
    parameterTypes: List<Triple<LocalSlotId, String, TypeCompiled>>,
    returnType: TypeCompiled?,
    stmt: DefInfo,
    eval: Evaluator,
): Result<Value> {
    val captured = stmt.parent.map { copy ->
        eval.cloneSlotCapture(copy, stmt)
    }
    val def = DefGen(
        parameters = parameters,
        parameterCaptures = stmt.parameterCaptures,
        parameterTypes = parameterTypes,
        returnType = returnType,
        defInfo = stmt,
        captured = captured,
        module = AtomicFrozenRefOption(runCatching { eval.topFrameDefFrozenModule(false) }.getOrElse { return Result.failure(it) }),
        optimizedOnFreezeStmt = StmtCompiledCell.new(),
        frozen = false,
    )
    return Result.success(eval.heap().allocComplex(def))
}

// ---- DefGen<FrozenValue>.postFreeze ----

/**
 * Post-freeze optimization for a frozen def.
 *
 * Module passed to this function is not always the module where the function
 * is declared: a function can be created in a frozen module and frozen later
 * in another module. The [module] parameter is the module being frozen now.
 */
internal fun DefGen<FrozenValue>.postFreeze(
    module: FrozenRef<FrozenModuleData>,
    heap: Heap,
    frozenHeap: FrozenHeap,
) {
    // `defModule` contains the module where this `def` is declared.
    val defModule = this.module.loadRelaxed() ?: run {
        this.module.storeRelaxed(module)
        module
    }

    // Now perform the optimization of function body with fully frozen module:
    // all module variables are frozen, so we can inline more aggressively.
    val bodyOptimized = this.defInfo.bodyStmts
        .optimize(OptCtx.new(
            OptCtxEvalForOptimizeOnFreeze(
                OptimizeOnFreezeContext(
                    module = defModule.asRef(),
                    heap = heap,
                    frozenHeap = frozenHeap,
                ),
            ),
            this.parameters.len().toUInt(),
        ))
        .asBc(
            this.defInfo.stmtCompileContext,
            FrozenRef(this.defInfo.used),
            this.parameters.len(),
            frozenHeap,
        )

    // Store the optimized body.
    // This is (relatively) safe because we know that during freeze
    // nobody has a reference to stmt: nobody is executing this `def`.
    this.optimizedOnFreezeStmt.set(bodyOptimized)
}

/**
 * A [MutableList] view backed by an array, so that [ParametersSpec.collectInline]
 * can write directly into frame slots without an intermediate copy.
 */
private class ArrayBackedMutableList<T>(private val array: Array<T>) : AbstractMutableList<T>() {
    override val size: Int get() = array.size

    override fun get(index: Int): T = array[index]

    override fun set(index: Int, element: T): T {
        val old = array[index]
        array[index] = element
        return old
    }

    override fun add(index: Int, element: T) {
        throw UnsupportedOperationException("ArrayBackedMutableList does not support add")
    }

    override fun removeAt(index: Int): T {
        throw UnsupportedOperationException("ArrayBackedMutableList does not support removeAt")
    }
}
