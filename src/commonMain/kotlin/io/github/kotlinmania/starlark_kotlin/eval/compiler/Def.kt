// port-lint: source src/eval/compiler/def.rs
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

// Implementation of `def`.

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.docs.DocFunction
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.environment.FrozenModuleData
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def_inline.InlineDefBody
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def_inline.inlineDefBody
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.Captured
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.ScopeId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstAssignIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstParameter
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.payload.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.instant.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.eval.runtime.arguments.ArgumentsImpl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.arguments.ResolvedArgName
import io.github.kotlinmania.starlark_kotlin.eval.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.bc.bytecode.Bc
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.allocaFrame
import io.github.kotlinmania.starlark_kotlin.syntax.EvalException
import io.github.kotlinmania.starlark_kotlin.syntax.def.DefParam
import io.github.kotlinmania.starlark_kotlin.syntax.def.DefParamIndices
import io.github.kotlinmania.starlark_kotlin.syntax.def.DefParamKind
import io.github.kotlinmania.starlark_kotlin.syntax.def.DefParams
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.callable_param.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.values.AtomicFrozenRefOption
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue

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
 * This is initialized in [FrozenDef.postFreeze].
 */
internal class StmtCompiledCell {
    @Volatile
    private var bc: Bc = Bc.default()

    companion object {
        fun new(): StmtCompiledCell = StmtCompiledCell()
    }

    /**
     * Set the bytecode. This function is unsafe if another thread is executing the stmt.
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
        val type: TypeCompiled<FrozenValue>?,
        /** Default value. */
        val defaultValue: T?,
    ) : ParameterCompiled<T>()

    /**
     * A `*args` parameter.
     */
    data class Args<T>(
        val paramName: ParameterName,
        val type: TypeCompiled<FrozenValue>?,
    ) : ParameterCompiled<T>()

    /**
     * A `**kwargs` parameter.
     */
    data class KwArgs<T>(
        val paramName: ParameterName,
        val type: TypeCompiled<FrozenValue>?,
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
internal fun <T> ParameterCompiled<T>.nameTy(): Pair<ParameterName, TypeCompiled<FrozenValue>?> {
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
    return t?.asTy()?.clone() ?: Ty.any()
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
// TODO: stub - ParametersCompiled needs real import
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
            posOrNamed = indices.posOrNamed().map { i ->
                val p = params[i].node
                Triple(ArcStr.from(p.nameTy().first.name), p.required(), p.ty())
            },
            args = indices.args?.let { i ->
                params[i.toInt()].node.ty()
            },
            namedOnly = indices.namedOnly(params.size).map { i ->
                val p = params[i].node
                Triple(ArcStr.from(p.nameTy().first.name), p.required(), p.ty())
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
                name = FrozenStringValue.of("<empty>"),
                signatureSpan = FrozenFileSpan.default(),
                parameterCaptures = emptyList(),
                ty = Ty.any(),
                codemap = FrozenRef(CodeMap.emptyStatic()),
                docstring = null,
                used = emptyList(),
                parent = emptyList(),
                stmtCompiled = Bc.default(),
                bodyStmts = StmtsCompiled.empty(),
                stmtCompileContext = StmtCompileContext(),
                inlineDefBody = null,
                globals = FrozenRef(Globals.empty()),
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
                name = FrozenStringValue.of("<module>"),
                signatureSpan = FrozenFileSpan.default(),
                parameterCaptures = emptyList(),
                ty = Ty.any(),
                codemap = codemap,
                docstring = null,
                used = localNames,
                parent = parent,
                stmtCompiled = Bc.default(),
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
    val returnType: TypeCompiled<FrozenValue>?,
    val info: FrozenRef<DefInfo>,
)

// ---- Compiler extensions for parameter compilation ----

/**
 * Compile a parameter name from a CST assignment identifier.
 */
internal fun Compiler.parameterName(ident: CstAssignIdent): ParameterName {
    val bindingId = ident.payload ?: error("no binding for parameter")
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
    x: Spanned<DefParam<CstPayload>>,
): IrSpanned<ParameterCompiled<IrSpanned<ExprCompiled>>> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, x.span))
    val pName = parameterName(x.ident)
    val node: ParameterCompiled<IrSpanned<ExprCompiled>> = when (val kind = x.node.kind) {
        is DefParamKind.Regular -> ParameterCompiled.Normal(
            pName,
            this.exprForType(x.ty)?.node,
            kind.defaultValue?.let { d -> this.expr(d) },
        )
        is DefParamKind.Args -> ParameterCompiled.Args(
            pName,
            this.exprForType(x.ty)?.node,
        )
        is DefParamKind.Kwargs -> ParameterCompiled.KwArgs(
            pName,
            this.exprForType(x.ty)?.node,
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
    params: List<CstParameter>,
    returnType: CstTypeExpr?,
    suite: CstStmt,
): ExprCompiled {
    val file = this.codemap.asRef().fileSpan(suite.span)
    val functionName = "${file.file.filename()}.$name"
    val frozenName = this.eval.frozenHeap().allocStrIntern(name)

    val defParams = DefParams.unpack(params, this.codemap)

    // The parameters run in the scope of the parent, so compile them with the outer scope
    val compiledParams = defParams.params.map { x -> parameter(x) }
    val parametersCompiled = ParametersCompiled(compiledParams, defParams.indices)
    val compiledReturnType = this.exprForType(returnType)?.node

    val ty = Ty.function(
        parametersCompiled.toTyParams(),
        compiledReturnType?.asTy()?.clone() ?: Ty.any(),
    )

    this.enterScope(scopeId)

    val docstring = DocString.extractRawStarlarkDocstring(suite)
    val body = this.stmt(suite, false)
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
            used,
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
    private val parameterTypes: List<Triple<LocalSlotId, String, TypeCompiled<FrozenValue>>>,
    /** The return type annotation for the function. */
    val returnType: TypeCompiled<FrozenValue>?,
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
     * This field is only used in `FrozenDef`. It is populated in [postFreeze].
     */
    internal val optimizedOnFreezeStmt: StmtCompiledCell,
    /** Whether this DefGen holds frozen values. */
    private val frozen: Boolean,
) : StarlarkValue {

    override fun toString(): String = parameters.signature()

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
        args: ArgumentsImpl,
        eval: Evaluator,
    ): Result<Value> {
        val bc = bc()
        return allocaFrame(
            eval,
            bc.localCount,
            bc.maxStackSize,
            bc.maxLoopDepth,
        ) { eval ->
            // Safety: `slots` is unique because `allocaFrame` just allocated the frame,
            // so there are no references to the frame except `eval.currentFrame`.
            val slots = eval.currentFrame.localsMut()
            parameters.collectInline(args, slots, eval.heap())
                .getOrElse { return@allocaFrame Result.failure(it) }
            invokeRaw(me, eval)
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
        args: ArgumentsImpl,
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
                eval.currentFrame.setSlot(copy.child, cap.toValue())
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
        bc().dumpDebug().lines().forEach { l ->
            sb.appendLine("  $l")
        }
        return sb.toString()
    }

    // StarlarkValue implementation

    /**
     * Returns the name used in call stack frames.
     */
    fun nameForCallStack(@Suppress("UNUSED_PARAMETER") me: Value): String {
        return defInfo.name.asStr()
    }

    /**
     * Invoke this function with the given arguments.
     */
    fun invoke(me: Value, args: Arguments, eval: Evaluator): Result<Value> {
        return invokeImpl(me, args.inner, eval)
    }

    /**
     * Generate documentation for this function.
     */
    fun documentation(): DocItem {
        val paramTys = MutableList(parameters.len()) { Ty.any() }
        for ((idx, _, ty) in parameterTypes) {
            // Local slot number for parameter is the same as parameter index.
            paramTys[idx.value.toInt()] = ty.asTy().clone()
        }

        val retType = returnType?.asTy()?.clone() ?: Ty.any()

        val functionDocs = DocFunction.fromDocstring(
            DocStringKind.Starlark,
            parameters.documentation(paramTys, emptyMap()),
            retType,
            defInfo.docstring,
        )

        return DocItem.Member(DocMember.Function(functionDocs))
    }

    /**
     * Returns the type of this function for the typechecker.
     */
    fun typecheckerTy(): Ty? = defInfo.ty

    /**
     * Write a hash for this function.
     * It's hard to come up with a good hash here, but let's at least make an effort.
     */
    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return defInfo.name.writeHash(hasher)
    }
}

/** Type alias for non-frozen def. */
internal typealias Def = DefGen<Value>

/** Type alias for frozen def. */
internal typealias FrozenDef = DefGen<FrozenValue>

// ---- DefLike ----

/**
 * Trait indicating whether a [DefGen] variant is frozen or not.
 */
internal interface DefLike {
    val isFrozen: Boolean
}

// ---- Def.new ----

/**
 * Create a new unfrozen [Def], allocating it on the evaluator's heap.
 */
internal fun newDef(
    parameters: ParametersSpec<Value>,
    parameterTypes: List<Triple<LocalSlotId, String, TypeCompiled<FrozenValue>>>,
    returnType: TypeCompiled<FrozenValue>?,
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
        module = AtomicFrozenRefOption(eval.topFrameDefFrozenModule(false).getOrElse { return Result.failure(it) }),
        optimizedOnFreezeStmt = StmtCompiledCell.new(),
        frozen = false,
    )
    return Result.success(eval.heap().alloc(def))
}

// ---- FrozenDef.postFreeze ----

/**
 * Post-freeze optimization for a frozen def.
 *
 * Module passed to this function is not always the module where the function
 * is declared: a function can be created in a frozen module and frozen later
 * in another module. The [module] parameter is the module being frozen now.
 */
internal fun FrozenDef.postFreeze(
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
            OptimizeOnFreezeContext(
                module = defModule.asRef(),
                heap = heap,
                frozenHeap = frozenHeap,
            ),
            this.parameters.len(),
        ))
        .asBc(
            this.defInfo.stmtCompileContext,
            this.defInfo.used,
            this.parameters.len(),
            frozenHeap,
        )

    // Store the optimized body.
    // This is (relatively) safe because we know that during freeze
    // nobody has a reference to stmt: nobody is executing this `def`.
    this.optimizedOnFreezeStmt.set(bodyOptimized)
}
