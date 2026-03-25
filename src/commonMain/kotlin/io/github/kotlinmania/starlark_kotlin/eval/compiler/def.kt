// port-lint: source src/eval/compiler/def.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.def

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

//! Implementation of `def`.

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.docs.DocFunction
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.environment.FrozenModuleData
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Compiler
import io.github.kotlinmania.starlark_kotlin.eval.compiler.expr.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.opt_ctx.OptCtx
import io.github.kotlinmania.starlark_kotlin.eval.compiler.stmt.OptimizeOnFreezeContext
import io.github.kotlinmania.starlark_kotlin.eval.compiler.stmt.StmtCompileContext
import io.github.kotlinmania.starlark_kotlin.eval.compiler.stmt.StmtsCompiled
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.instant.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.callable_param.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.factory.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.Arguments
import io.github.kotlinmania.starlark_kotlin.values.AtomicFrozenRefOption
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefParams
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefParamKind
import io.github.kotlinmania.starlark_kotlin.syntax.ast.DefParam
import io.github.kotlinmania.starlark_kotlin.syntax.ast.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.typing.callable_param.DefParamIndices
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.eval.runtime.ArgumentsImpl
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compr.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.compiler.call.InlineDefBody
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ScopeId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Captured
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.Bc
import io.github.kotlinmania.starlark_kotlin.eval.bc.ResolvedArgName
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstStmt
import io.github.kotlinmania.starlark_kotlin.analysis.CstAssignIdent
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.moduleEnv
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.owned.default
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.syntax.payload_and_span.Payload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstParameter
import io.github.kotlinmania.starlark_kotlin.values.writeHash
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.asTy
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefParams
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefParamKind
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.DefParam
import io.github.kotlinmania.starlark_kotlin.typing.fill_types_for_lint.CstTypeExpr
import io.github.kotlinmania.starlark_kotlin.tests.frozenHeap
import io.github.kotlinmania.starlark_kotlin.tests.derive.freeze.checkType
import io.github.kotlinmania.starlark_kotlin.pagable.of
import io.github.kotlinmania.starlark_kotlin.inner
import io.github.kotlinmania.starlark_kotlin.eval.runtime.wrapLocalSlotCaptured
import io.github.kotlinmania.starlark_kotlin.eval.runtime.topFrameDefFrozenModule
import io.github.kotlinmania.starlark_kotlin.eval.runtime.toCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.eval.runtime.evalBc
import io.github.kotlinmania.starlark_kotlin.eval.runtime.currentFrame
import io.github.kotlinmania.starlark_kotlin.eval.runtime.cloneSlotCapture
import io.github.kotlinmania.starlark_kotlin.eval.compiler.stmt.compileContext
import io.github.kotlinmania.starlark_kotlin.eval.compiler.inlineDefBody
import io.github.kotlinmania.starlark_kotlin.eval.compiler.exprForType
import io.github.kotlinmania.starlark_kotlin.eval.bc.typecheckProfile
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.allocaFrame
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.asBc
import io.github.kotlinmania.starlark_kotlin.environment.dumpDebug
import io.github.kotlinmania.starlark_kotlin.docs.params
import io.github.kotlinmania.starlark_kotlin.docs.defaultValue
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.heap
import io.github.kotlinmania.starlark_kotlin.analysis.ident
import io.github.kotlinmania.starlark_kotlin.analysis.fileSpan
import io.github.kotlinmania.starlark_kotlin.eval.compiler.getScope
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Stmt
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.analysis.node
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.values.default

// #[derive(thiserror::Error, Debug)]
// enum DefError {
//     #[error("Function has no type, while function was compiled with return type (internal error)")]
//     CheckReturnTypeNoType,
// }
private class DefError {
    class CheckReturnTypeNoType :
        Exception("Function has no type, while function was compiled with return type (internal error)")
}

/// Store frozen `StmtCompiled`.
/// This is initialized in `post_freeze`.
// struct StmtCompiledCell { cell: UnsafeCell<Bc> }
internal class StmtCompiledCell {
    @Volatile
    private var bc: Bc = Bc.default()

    // fn new() -> StmtCompiledCell
    companion object {
        fun new(): StmtCompiledCell = StmtCompiledCell()
    }

    // unsafe fn set(&self, value: Bc)
    /// This function is unsafe if other thread is executing the stmt.
    fun set(value: Bc) {
        bc = value
    }

    // fn get(&self) -> &Bc
    fun get(): Bc = bc
}

// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) struct ParameterName {
//     pub(crate) name: String,
//     captured: Captured,
// }
internal data class ParameterName(
    val name: String,
    val captured: Captured,
)

// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) enum ParameterCompiled<T> {
//     Normal(ParameterName, Option<TypeCompiled<FrozenValue>>, Option<T>),
//     Args(ParameterName, Option<TypeCompiled<FrozenValue>>),
//     KwArgs(ParameterName, Option<TypeCompiled<FrozenValue>>),
// }
internal sealed class ParameterCompiled<out T> {
    data class Normal<T>(
        /// Name.
        val paramName: ParameterName,
        /// Type.
        val type: TypeCompiled<FrozenValue>?,
        /// Default value.
        val defaultValue: T?,
    ) : ParameterCompiled<T>()

    data class Args<T>(
        val paramName: ParameterName,
        val type: TypeCompiled<FrozenValue>?,
    ) : ParameterCompiled<T>()

    data class KwArgs<T>(
        val paramName: ParameterName,
        val type: TypeCompiled<FrozenValue>?,
    ) : ParameterCompiled<T>()
}

// impl<T> ParameterCompiled<T>

// pub(crate) fn map_expr<U>(&self, f: impl FnMut(&T) -> U) -> ParameterCompiled<U>
internal fun <T, U> ParameterCompiled<T>.mapExpr(f: (T) -> U): ParameterCompiled<U> {
    return when (this) {
        is ParameterCompiled.Normal -> ParameterCompiled.Normal(paramName, type, defaultValue?.let(f))
        is ParameterCompiled.Args -> ParameterCompiled.Args(paramName, type)
        is ParameterCompiled.KwArgs -> ParameterCompiled.KwArgs(paramName, type)
    }
}

// pub(crate) fn accepts_positional(&self) -> bool
internal fun <T> ParameterCompiled<T>.acceptsPositional(): Boolean {
    return this is ParameterCompiled.Normal
}

// pub(crate) fn captured(&self) -> Captured
internal fun <T> ParameterCompiled<T>.captured(): Captured {
    return nameTy().first.captured
}

// pub(crate) fn name_ty(&self) -> (&ParameterName, Option<TypeCompiled<FrozenValue>>)
internal fun <T> ParameterCompiled<T>.nameTy(): Pair<ParameterName, TypeCompiled<FrozenValue>?> {
    return when (this) {
        is ParameterCompiled.Normal -> Pair(paramName, type)
        is ParameterCompiled.Args -> Pair(paramName, type)
        is ParameterCompiled.KwArgs -> Pair(paramName, type)
    }
}

// pub(crate) fn has_type(&self) -> bool
internal fun <T> ParameterCompiled<T>.hasType(): Boolean {
    return nameTy().second != null
}

// pub(crate) fn ty(&self) -> Ty
internal fun <T> ParameterCompiled<T>.ty(): Ty {
    val (_, t) = nameTy()
    return t?.asTy() ?: Ty.any()
}

// pub(crate) fn required(&self) -> ParamIsRequired
internal fun <T> ParameterCompiled<T>.required(): ParamIsRequired {
    return when (this) {
        is ParameterCompiled.Normal -> {
            if (defaultValue == null) ParamIsRequired.Yes else ParamIsRequired.No
        }
        is ParameterCompiled.Args -> ParamIsRequired.No
        is ParameterCompiled.KwArgs -> ParamIsRequired.No
    }
}

// pub(crate) fn is_star_or_star_star(&self) -> bool
internal fun <T> ParameterCompiled<T>.isStarOrStarStar(): Boolean {
    return this is ParameterCompiled.Args || this is ParameterCompiled.KwArgs
}

// #[derive(Debug, Clone, VisitSpanMut)]
// pub(crate) struct ParametersCompiled<T> {
//     pub(crate) params: Vec<IrSpanned<ParameterCompiled<T>>>,
//     pub(crate) indices: DefParamIndices,
// }
internal data class ParametersCompiled<T>(
    val params: List<IrSpanned<ParameterCompiled<T>>>,
    val indices: DefParamIndices,
) {

    // impl<T> ParametersCompiled<T>

    /// How many expressions this parameters references (default values and types).
    // pub(crate) fn count_exprs(&self) -> u32
    fun countExprs(): Int {
        var count = 0
        for (p in params) {
            p.node.mapExpr<T, Unit> { count += 1 }
        }
        return count
    }

    /// How many parameter variables?
    // pub(crate) fn count_param_variables(&self) -> u32
    fun countParamVariables(): Int = params.size

    /// Any parameter has type annotation?
    // pub(crate) fn has_types(&self) -> bool
    fun hasTypes(): Boolean = params.any { it.node.hasType() }

    /// Has `*args` or `*kwargs` parameter? `*` is fine.
    // pub(crate) fn has_args_or_kwargs(&self) -> bool
    fun hasArgsOrKwargs(): Boolean {
        return params.any { it.node.isStarOrStarStar() }
    }

    // pub(crate) fn parameter_captures(&self) -> Vec<LocalSlotId>
    fun parameterCaptures(): List<LocalSlotId> {
        return params.mapIndexedNotNull { i, p ->
            if (p.node.captured() == Captured.Yes) {
                LocalSlotId(i)
            } else {
                null
            }
        }
    }

    // pub(crate) fn to_ty_params(&self) -> ParamSpec
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
                params[i].node.ty()
            },
            namedOnly = indices.namedOnly(params.size).map { i ->
                val p = params[i].node
                Triple(ArcStr.from(p.nameTy().first.name), p.required(), p.ty())
            },
            kwargs = indices.kwargs?.let { i ->
                params[i].node.ty()
            },
        )!!
    }
}

/// Copy local variable slot to nested function.
// #[derive(Debug, Clone, Dupe)]
// pub(crate) struct CopySlotFromParent {
//     pub(crate) parent: LocalSlotIdCapturedOrNot,
//     pub(crate) child: LocalSlotIdCapturedOrNot,
// }
internal data class CopySlotFromParent(
    /// Slot in the outer function.
    val parent: LocalSlotIdCapturedOrNot,
    /// Slot in the nested function.
    val child: LocalSlotIdCapturedOrNot,
)

/// Static info for `def`, `lambda` or module.
// #[derive(Derivative, Display)]
// #[derivative(Debug)]
// #[display("DefInfo")]
// pub(crate) struct DefInfo { ... }
internal class DefInfo(
    val name: FrozenStringValue,
    /// Span of function signature.
    val signatureSpan: FrozenFileSpan,
    /// Indices of parameters, which are captured in nested defs.
    val parameterCaptures: List<LocalSlotId>,
    /// Type of this function, for the typechecker.
    val ty: Ty,
    /// Codemap of the file where the function is declared.
    val codemap: FrozenRef<CodeMap>,
    /// The raw docstring pulled out of the AST.
    val docstring: String?,
    /// Slots this scope uses, including for parameters and `parent`.
    /// Indexed by [`LocalSlotId`], values are variable names.
    val used: List<FrozenStringValue>,
    /// Slots to copy from the parent.
    /// Module-level identifiers are not copied over, to avoid excess copying.
    val parent: List<CopySlotFromParent>,
    /// Statement compiled for non-frozen def.
    val stmtCompiled: Bc,
    // The compiled expression for the body of this definition, to be run
    // after the parameters are evaluated.
    val bodyStmts: StmtsCompiled,
    /// How to compile the statement on freeze.
    val stmtCompileContext: StmtCompileContext,
    /// Function can be inlined.
    val inlineDefBody: InlineDefBody?,
    /// Globals captured during function or module creation.
    /// Only needed for debugger evaluation.
    val globals: FrozenRef<Globals>,
) {
    override fun toString(): String = "DefInfo"

    companion object {
        // pub(crate) fn empty() -> FrozenRef<'static, DefInfo>
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

        // pub(crate) fn for_module(...)
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

// #[derive(Clone, Debug, VisitSpanMut)]
// pub(crate) struct DefCompiled {
//     pub(crate) function_name: String,
//     pub(crate) params: ParametersCompiled<IrSpanned<ExprCompiled>>,
//     pub(crate) return_type: Option<TypeCompiled<FrozenValue>>,
//     pub(crate) info: FrozenRef<'static, DefInfo>,
// }
internal data class DefCompiled(
    val functionName: String,
    val params: ParametersCompiled<IrSpanned<ExprCompiled>>,
    val returnType: TypeCompiled<FrozenValue>?,
    val info: FrozenRef<DefInfo>,
)

// impl Compiler - parameter_name, parameter, function

// fn parameter_name(&mut self, ident: &CstAssignIdent) -> ParameterName
internal fun Compiler.parameterName(ident: CstAssignIdent): ParameterName {
    val bindingId = ident.Payload ?: error("no binding for parameter")
    val binding = this.scopeData.getBinding(bindingId)
    return ParameterName(
        name = ident.node.ident,
        captured = binding.captured,
    )
}

// fn parameter(&mut self, x: &Spanned<DefParam<'_, CstPayload>>) -> Result<...>
internal fun Compiler.parameter(
    x: Spanned<DefParam<CstPayload>>,
): Result<IrSpanned<ParameterCompiled<IrSpanned<ExprCompiled>>>> {
    val span = FrameSpan.new(FrozenFileSpan.new(this.codemap, x.span))
    val pName = parameterName(x.node.ident)
    val node: ParameterCompiled<IrSpanned<ExprCompiled>> = when (val kind = x.node.kind) {
        is DefParamKind.Regular -> {
            val ty = this.exprForType(x.node.ty)?.node
            val default = kind.defaultValue?.let { d ->
                this.Expr(d).getOrElse { return Result.failure(it) }
            }
            ParameterCompiled.Normal(pName, ty, default)
        }
        is DefParamKind.Args -> {
            val ty = this.exprForType(x.node.ty)?.node
            ParameterCompiled.Args(pName, ty)
        }
        is DefParamKind.Kwargs -> {
            val ty = this.exprForType(x.node.ty)?.node
            ParameterCompiled.KwArgs(pName, ty)
        }
    }
    return Result.success(IrSpanned(span, node))
}

// pub fn function(&mut self, ...) -> Result<ExprCompiled, CompilerInternalError>
internal fun Compiler.function(
    name: String,
    signatureSpan: FrozenFileSpan,
    scopeId: ScopeId,
    params: List<CstParameter>,
    returnType: CstTypeExpr?,
    suite: CstStmt,
): Result<ExprCompiled> {
    val file = this.codemap.fileSpan(suite.span)
    val functionName = "${file.file.filename()}.$name"
    val frozenName = this.eval.frozenHeap().allocStrIntern(name)

    val defParams = DefParams.unpack(params, this.codemap)
        .getOrElse { return Result.failure(it) }

    // The parameters run in the scope of the parent, so compile them with the outer scope
    val compiledParams = defParams.params.map { x ->
        parameter(x).getOrElse { return Result.failure(it) }
    }
    val parametersCompiled = ParametersCompiled(compiledParams, defParams.indices)
    val compiledReturnType = this.exprForType(returnType)?.node

    val ty = Ty.function(
        parametersCompiled.toTyParams(),
        compiledReturnType?.asTy() ?: Ty.any(),
    )

    this.enterScope(scopeId)

    val docstring = DocString.extractRawStarlarkDocstring(suite)
    val body = this.Stmt(suite, false).getOrElse { return Result.failure(it) }
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

    return Result.success(ExprCompiled.Def(DefCompiled(
        functionName = functionName,
        params = parametersCompiled,
        returnType = compiledReturnType,
        info = frozenInfo,
    )))
}

/// Starlark function internal representation and implementation of
/// [`StarlarkValue`].
// #[derive(Derivative, NoSerialize, ProvidesStaticType, Trace, Allocative)]
// pub(crate) struct DefGen<V> { ... }
// pub(crate) type Def<'v> = DefGen<Value<'v>>;
// pub(crate) type FrozenDef = DefGen<FrozenValue>;
internal class DefGen<V : ValueLike>(
    val parameters: ParametersSpec<V>,
    /// Indices of parameters, which are captured in nested defs.
    private val parameterCaptures: List<LocalSlotId>,
    // The types of the parameters.
    private val parameterTypes: List<Triple<LocalSlotId, String, TypeCompiled<FrozenValue>>>,
    val returnType: TypeCompiled<FrozenValue>?,
    /// Data created during function compilation but before function instantiation.
    val defInfo: DefInfo,
    /// Any variables captured from the outer scope (nested def/lambda).
    private val captured: List<V>,
    /// A reference to the module where the function is defined after the module has been frozen.
    val module: AtomicFrozenRefOption<FrozenModuleData>,
    /// This field is only used in `FrozenDef`. It is populated in `post_freeze`.
    private val optimizedOnFreezeStmt: StmtCompiledCell,
    /// Whether this is frozen.
    private val frozen: Boolean,
) : StarlarkValue, AllocValue, AllocFrozenValue {

    override fun allocValue(heap: io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap): io.github.kotlinmania.starlark_kotlin.values.layout.Value {
        return io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplexNoFreeze(heap, this)
    }

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex(heap, this)
    }

    // impl Display for DefGen
    override fun toString(): String = parameters.signature()

    // pub(crate) fn bc(&self) -> &Bc
    fun bc(): Bc {
        return if (frozen) {
            optimizedOnFreezeStmt.get()
        } else {
            defInfo.stmtCompiled
        }
    }

    // fn check_parameter_types(&self, eval: &mut Evaluator<'v, '_, '_>) -> crate::Result<()>
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

    // pub(crate) fn check_return_type(&self, ret: Value<'v>, eval: &mut Evaluator) -> crate::Result<()>
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

    // fn invoke_impl<'a, A: ArgumentsImpl<'v, 'a>>(&self, me: Value, args: &A, eval: &mut Evaluator) -> crate::Result<Value<'v>>
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
            val slots = eval.currentFrame.localsMut()
            parameters.collectInline(args, slots, eval.heap())
                .getOrElse { return@allocaFrame Result.failure(it) }
            invokeRaw(me, eval)
        }
    }

    // pub(crate) fn invoke_with_args<'a, A>(&self, me, args, eval) -> crate::Result<Value>
    fun invokeWithArgs(
        me: Value,
        args: ArgumentsImpl,
        eval: Evaluator,
    ): Result<Value> {
        return invokeImpl(me, args, eval)
    }

    /// Invoke the function, assuming that:
    /// * the frame has been allocated and stored in `eval.current_frame`
    /// * the arguments have been collected into the frame
    // fn invoke_raw(&self, me: Value, eval: &mut Evaluator) -> crate::Result<Value>
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
        if (captured.isNotEmpty()) {
            for ((copy, cap) in defInfo.parent.zip(captured)) {
                eval.currentFrame.setSlot(copy.child, cap.toValue())
            }
        }

        return eval.evalBc(me, bc())
            .mapCatching { it }
    }

    // pub(crate) fn resolve_arg_name(&self, name: Hashed<&str>) -> ResolvedArgName
    fun resolveArgName(name: Hashed<String>): ResolvedArgName {
        return parameters.resolveName(name)
    }

    // pub(crate) fn dump_debug(&self) -> String
    fun dumpDebug(): String {
        val sb = StringBuilder()
        sb.appendLine("Bytecode:")
        bc().dumpDebug().lines().forEach { l ->
            sb.appendLine("  $l")
        }
        return sb.toString()
    }

    // #[starlark_value(type = FUNCTION_TYPE)]
    // impl StarlarkValue for DefGen<V>

    // fn name_for_call_stack(&self, _me: Value<'v>) -> String
    fun nameForCallStack(me: Value): String {
        return defInfo.name.asStr()
    }

    // fn invoke(&self, me: Value, args: &Arguments, eval: &mut Evaluator) -> crate::Result<Value>
    fun invoke(me: Value, args: Arguments, eval: Evaluator): Result<Value> {
        return invokeImpl(me, args.inner, eval)
    }

    // fn documentation(&self) -> DocItem
    fun documentation(): DocItem {
        val paramTys = MutableList(parameters.len()) { Ty.any() }
        for ((idx, _, ty) in parameterTypes) {
            paramTys[idx.index] = ty.asTy()
        }

        val retType = returnType?.asTy() ?: Ty.any()

        val functionDocs = DocFunction.fromDocstring(
            DocStringKind.Starlark,
            parameters.documentation(paramTys, emptyMap()),
            retType,
            defInfo.docstring,
        )

        return DocItem.Member(DocMember.Function(functionDocs))
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    fun typecheckerTy(): Ty? = defInfo.ty

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        return defInfo.name.writeHash(hasher)
    }
}

/// Type alias for non-frozen def.
internal typealias Def = DefGen<Value>

/// Type alias for frozen def.
internal typealias FrozenDef = DefGen<FrozenValue>

// impl Def<'v> - fn new(...)
internal fun Def.Companion.new(
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
    return Result.success(def.allocValue(eval.heap()))
}

// impl FrozenDef
// pub(crate) fn post_freeze(&self, module, heap, frozen_heap)
internal fun FrozenDef.postFreeze(
    module: FrozenRef<FrozenModuleData>,
    heap: Heap,
    frozenHeap: FrozenHeap,
) {
    // Module passed to this function is not always module where the function is declared:
    // A function can be created in a frozen module and frozen later in another module.
    val defModule = this.module.loadRelaxed() ?: run {
        this.module.storeRelaxed(module)
        module
    }

    // Now perform the optimization of function body with fully frozen module:
    // all module variables are frozen, so we can inline more aggressively.
    val bodyOptimized = this.defInfo.bodyStmts
        .optimize(OptCtx.new(
            OptimizeOnFreezeContext(
                module = defModule.value,
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
    this.optimizedOnFreezeStmt.set(bodyOptimized)
}
