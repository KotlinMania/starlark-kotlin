// port-lint: source src/debug/adapter.rs
package io.github.kotlinmania.starlark.debug

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

/**
 * Provides utilities useful for implementation of the debug adapter protocol (DAP, see
 * [DAP specification](https://microsoft.github.io/debug-adapter-protocol/)), primarily the
 * [DapAdapter]/[DapAdapterEvalHook] that provide for debugging a starlark Evaluation.
 */

import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.debug.adapterimpl.prepareDapAdapterImpl
import io.github.kotlinmania.starlark.debug.adapterimpl.resolveBreakpointsImpl
import io.github.kotlinmania.starlark.debug.adapterimpl.resolvedBreakpointsToDap
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.dict.DictRef
import io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark.values.types.dict.iter
import kotlin.compareTo

/**
 * The DapAdapterClient is implemented by the user and provides functionality
 * required by the [DapAdapter].
 */
interface DapAdapterClient {
    /** Indicates that the evaluation stopped at a breakpoint. */
    fun eventStopped(): Result<Unit>
}

/** Information about the variables scopes. */
data class ScopesInfo(
    /** Number of local variables. */
    val numLocals: Int,
)

/**
 * Information about a "structural variable" inspected by a debugger.
 * This currently has DAP-like semantic meaning that every complex object returned
 * by debugger from the stack or from the heap can be broken down into "variables".
 * This is how structured data is managed by the debugger.
 * Something similar to LLDB's SBValue.
 */
data class Variable(
    /** Name of the variable. */
    val name: PathSegment,
    /** The value as a String. */
    val value: String,
    /** The variables type. */
    val type: String,
    /** Indicates whether there are children available for a given variable. */
    val hasChildren: Boolean,
) {
    companion object {
        private fun tupleValueAsStr(v: Value): String {
            val size = v.length().getOrNull()
            return if (size != null && size > 0) "<tuple, size=$size>" else "()"
        }

        private fun listValueAsStr(v: Value): String {
            val size = v.length().getOrNull()
            return if (size != null && size > 0) "<list, size=$size>" else "[]"
        }

        private fun dictValueAsStr(v: Value): String {
            val size = v.length().getOrNull()
            return if (size != null && size > 0) "<dict, size=$size>" else "{}"
        }

        private fun structLikeValueAsStr(v: Value): String {
            val attrs = v.dirAttr()
            return "<type:${v.getType()}, size=${attrs.size}>"
        }

        internal fun truncateString(strValue: String, maxLen: Int): String {
            if (strValue.length > maxLen) {
                // Find a valid character boundary cut-off point within max length.
                var cutoff = maxLen
                // Walk back to a valid char boundary (for surrogate pairs)
                while (cutoff > 0 && strValue[cutoff - 1].isHighSurrogate()) {
                    cutoff -= 1
                }
                if (cutoff > 0) {
                    return strValue.substring(0, cutoff) + "...(truncated)"
                }
            }
            return strValue
        }

        internal fun valueAsStr(v: Value): String =
            if (hasChildren(v)) {
                when (v.getType()) {
                    "list" -> listValueAsStr(v)
                    "tuple" -> tupleValueAsStr(v)
                    "dict" -> dictValueAsStr(v)
                    else -> structLikeValueAsStr(v)
                }
            } else {
                when (v.getType()) {
                    "function" -> "<function>"
                    else -> {
                        val MAX_STR_LEN = 10000
                        truncateString(v.toStr(), MAX_STR_LEN)
                    }
                }
            }

        /** Creates a new instance of [Variable] from a given starlark value. */
        fun fromValue(name: PathSegment, v: Value): Variable =
            Variable(
                name = name,
                value = valueAsStr(v),
                type = v.getType(),
                hasChildren = hasChildren(v),
            )

        internal fun hasChildren(v: Value): Boolean {
            return when (v.getType()) {
                "function", "never", "NoneType", "bool", "int", "float", "string" -> false
                "list", "tuple", "dict" -> {
                    val length = v.length().getOrNull() ?: return false
                    length > 0
                }
                else -> true
            }
        }
    }

    /** Helper to convert to the DAP Variable type. */
    fun toDap(): DapVariable =
        DapVariable(
            name = this.name.toString(),
            value = this.value,
            type = this.type,
            evaluateName = null,
            indexedVariables = null,
            namedVariables = null,
            presentationHint = null,
            variablesReference = 0,
        )
}

/** Represents the scope of a variable. */
sealed class Scope {
    /** A local variable's scope, identified by its name. */
    data class Local(
        val name: String,
    ) : Scope()

    /** A scope determined by a particular expression. */
    data class Expr(
        val expression: String,
    ) : Scope()
}

/**
 * Represents a variable's "access path" for a local variable or watch expression.
 *
 * Examples:
 *
 * - For path `var1.field1[0]`, the scope is `Local("var1")` and the access path is `["field1", 0]`.
 * - For path `someObject.method().something`, the scope is `Expr("someObject.method().something")`.
 *   The access path includes segments inside the evaluated result of `someObject.method().something`
 *   if it returns a complex object.
 */
data class VariablePath(
    val scope: Scope,
    val accessPath: List<PathSegment>,
) {
    companion object {
        /** Creates new instance of [VariablePath] from a given expression. */
        fun newExpression(expr: String): VariablePath =
            VariablePath(
                scope = Scope.Expr(expr),
                accessPath = emptyList(),
            )

        /** Creates new instance of [VariablePath] from a given local variable. */
        fun newLocal(scope: String): VariablePath =
            VariablePath(
                scope = Scope.Local(scope),
                accessPath = emptyList(),
            )
    }

    /** Creates a child segment of given access path. */
    fun makeChild(path: PathSegment): VariablePath {
        val newAccessPath = accessPath.toMutableList()
        newAccessPath.add(path)
        return VariablePath(
            scope = scope,
            accessPath = newAccessPath,
        )
    }
}

/**
 * Represents a segment in an access expression.
 *
 * For the given expression `name.field1.array[0]`, the segments are "field1", "array", and "0".
 */
sealed class PathSegment {
    /** Represents a path segment that accesses array-like types (i.e., types indexable by numbers). */
    data class Index(
        val index: Int,
    ) : PathSegment()

    /** Represents a path segment that accesses object-like types (i.e., types keyed by strings). */
    data class Attr(
        val name: String,
    ) : PathSegment()

    /** Represents a path segment that accesses dict items by key. */
    data class Key(
        val key: String,
    ) : PathSegment()

    override fun toString(): String =
        when (this) {
            is Index -> "$index"
            is Attr -> name
            is Key -> "\"$key\""
        }

    fun get(v: Value, heap: Heap): Result<Value> =
        when (this) {
            is Index -> v.at(index.allocValue(heap), heap)
            is Attr -> v.getAttrError(name, heap)
            is Key -> v.at(heap.allocStr(key), heap)
        }
}

/** The kind of debugger step, used for next/stepin/stepout requests. */
enum class StepKind {
    /**
     * Step "into" the statement. This is generally used on a function call to stop in the
     * function call. In practice, this will stop on the next statement.
     */
    Into,

    /**
     * Step "over" the statement. This will stop on the next statement in the current function
     * after the current one (so will step "over" a function call).
     */
    Over,

    /**
     * Step "out" of the current function. This will stop on the next statement after this
     * function returns.
     */
    Out,
}

/** Information about variables in scope. */
data class VariablesInfo(
    /** Local variables. */
    val locals: List<Variable>,
)

/** Information about variable child "sub-values". */
data class InspectVariableInfo(
    /** Child variables. */
    val subValues: List<Variable> = emptyList(),
) {
    companion object {
        private fun tryFromDict(valueDict: DictRef): Result<InspectVariableInfo> {
            val keySegments =
                valueDict
                    .iter()
                    .map { (key, value) -> Pair(PathSegment.Key(key.toStr()), value) }

            return Result.success(
                InspectVariableInfo(
                    subValues =
                        keySegments
                            .map { (pathSegment, value) -> Variable.fromValue(pathSegment, value) }
                            .toList(),
                ),
            )
        }

        private fun tryFromStructLike(v: Value, heap: Heap): Result<InspectVariableInfo> =
            try {
                val subValues =
                    v.dirAttr().map { childName ->
                        val childValue = v.getAttrError(childName, heap).getOrThrow()
                        val segment = PathSegment.Attr(childName)
                        Variable.fromValue(segment, childValue)
                    }
                Result.success(InspectVariableInfo(subValues = subValues))
            } catch (e: Exception) {
                Result.failure(e)
            }

        private fun tryFromArrayLike(v: Value, heap: Heap): Result<InspectVariableInfo> =
            try {
                val len = v.length().getOrThrow()
                val subValues =
                    (0 until len).map { i ->
                        val index = i.allocValue(heap)
                        val elem = v.at(index, heap).getOrThrow()
                        Variable.fromValue(PathSegment.Index(i), elem)
                    }
                Result.success(InspectVariableInfo(subValues = subValues))
            } catch (e: Exception) {
                Result.failure(e)
            }

        /** Tries to create [InspectVariableInfo] from a given starlark value. */
        fun tryFromValue(v: Value, heap: Heap): Result<InspectVariableInfo> {
            return when (v.getType()) {
                "dict" -> {
                    val dictRef =
                        dictRefFromValue(v)
                            ?: return Result.failure(IllegalArgumentException("not a dictionary"))
                    tryFromDict(dictRef)
                }
                "struct" -> tryFromStructLike(v, heap)
                "list", "tuple" -> tryFromArrayLike(v, heap)
                "bool", "int", "float", "string" -> Result.success(InspectVariableInfo())
                "function", "never", "NoneType" -> Result.success(InspectVariableInfo())
                // This branch will catch Ty::basic(name)
                else -> tryFromStructLike(v, heap)
            }
        }
    }
}

/** Information about expression evaluation result. */
data class EvaluateExprInfo(
    /** The value as a String. */
    val result: String,
    /** The variables type. */
    val type: String,
    /** Indicates whether there are children available for a given variable. */
    val hasChildren: Boolean,
) {
    companion object {
        /** Creates [EvaluateExprInfo] from a given starlark value. */
        fun fromValue(v: Value): EvaluateExprInfo =
            EvaluateExprInfo(
                result = Variable.valueAsStr(v),
                type = v.getType(),
                hasChildren = Variable.hasChildren(v),
            )
    }
}

/** DAP Variable type for protocol compatibility. */
data class DapVariable(
    val name: String,
    val value: String,
    val type: String?,
    val evaluateName: String?,
    val indexedVariables: Int?,
    val namedVariables: Int?,
    val presentationHint: Any?,
    val variablesReference: Int,
)

/** DAP StackFrame type. */
data class StackFrame(
    val id: Int,
    val name: String,
    val source: String?,
    val line: Int,
    val column: Int,
    val endColumn: Int? = null,
    val endLine: Int? = null,
    val moduleId: String? = null,
    val presentationHint: String? = null,
)

/** DAP StackTraceArguments. */
data class StackTraceArguments(
    val threadId: Int,
    val startFrame: Int? = null,
    val levels: Int? = null,
)

/** DAP StackTraceResponseBody. */
data class StackTraceResponseBody(
    val stackFrames: List<StackFrame>,
    val totalFrames: Int? = null,
)

/** DAP SetBreakpointsArguments. */
data class SetBreakpointsArguments(
    val source: Source,
    val breakpoints: List<SourceBreakpoint>? = null,
    val lines: List<Int>? = null,
    val sourceModified: Boolean? = null,
)

/** DAP Source. */
data class Source(
    val adapterData: Any? = null,
    val checksums: List<Any>? = null,
    val name: String? = null,
    val origin: String? = null,
    val path: String? = null,
    val presentationHint: String? = null,
    val sourceReference: Int? = null,
    val sources: List<Source>? = null,
)

/** DAP SourceBreakpoint. */
data class SourceBreakpoint(
    val line: Long,
    val column: Int? = null,
    val condition: String? = null,
    val hitCondition: String? = null,
    val logMessage: String? = null,
)

/** DAP SetBreakpointsResponseBody. */
data class SetBreakpointsResponseBody(
    val breakpoints: List<DapBreakpoint>,
)

/** DAP Breakpoint response type. */
data class DapBreakpoint(
    val verified: Boolean,
    val column: Int? = null,
    val endColumn: Int? = null,
    val endLine: Int? = null,
    val id: Int? = null,
    val line: Int? = null,
    val message: String? = null,
    val source: Source? = null,
)

/** DAP Capabilities. */
data class Capabilities(
    val supportsConfigurationDoneRequest: Boolean? = null,
    val supportsEvaluateForHovers: Boolean? = null,
    val supportsSetVariable: Boolean? = null,
    val supportsStepInTargetsRequest: Boolean? = null,
    val supportsConditionalBreakpoints: Boolean? = null,
)

internal data class Breakpoint(
    val span: FileSpan,
    val condition: String?,
)

/**
 * Breakpoints resolved to their spans.
 */
class ResolvedBreakpoints internal constructor(
    internal val breakpoints: List<Breakpoint?>,
) {
    /**
     * Converts resolved breakpoints to a [SetBreakpointsResponseBody].
     * The breakpoints should've been resolved from the corresponding SetBreakpointsRequest.
     */
    fun toResponse(): SetBreakpointsResponseBody = resolvedBreakpointsToDap(this)
}

/** Resolves the breakpoints to their [FileSpan] if possible. */
fun resolveBreakpoints(
    args: SetBreakpointsArguments,
    ast: AstModule,
): Result<ResolvedBreakpoints> = resolveBreakpointsImpl(args, ast)

/**
 * The [DapAdapter] accepts DAP requests and updates the hooks in the running evaluator.
 */
interface DapAdapter {
    /**
     * Sets multiple breakpoints for a file (and clears existing ones).
     *
     * See [SetBreakpoints](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_SetBreakpoints)
     */
    fun setBreakpoints(
        source: String,
        breakpoints: ResolvedBreakpoints,
    ): Result<Unit>

    /** Gets the top stack frame, may be null if entered from native. */
    fun topFrame(): Result<StackFrame?>

    /**
     * Gets a stacktrace from the current execution state.
     *
     * See [StackTrace](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_StackTrace)
     */
    fun stackTrace(args: StackTraceArguments): Result<StackTraceResponseBody>

    /**
     * Gets the variables scope for a frame.
     *
     * See [Scopes](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_Scopes)
     */
    fun scopes(): Result<ScopesInfo>

    /**
     * Gets variables for the current scope.
     *
     * See [Variables](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_Variables)
     */
    fun variables(): Result<VariablesInfo>

    /**
     * Gets all child variables for the given access path.
     *
     * See [Variables](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_Variables)
     */
    fun inspectVariable(path: VariablePath): Result<InspectVariableInfo>

    /**
     * Resumes execution.
     *
     * See [Continue](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_Continue)
     */
    fun continueExecution(): Result<Unit>

    /**
     * Continues execution until some condition.
     *
     * See [Next](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_Next),
     * [StepIn](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_StepIn),
     * [StepOut](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_StepOut)
     */
    fun step(kind: StepKind): Result<Unit>

    /**
     * Evaluates an expression in the context of the top-most frame.
     *
     * See [Evaluate](https://microsoft.github.io/debug-adapter-protocol/specification#Requests_Evaluate)
     */
    fun evaluate(expr: String): Result<EvaluateExprInfo>
}

/**
 * This is sort of the evaluation side of the [DapAdapter]. It's expected that these are on
 * different threads (the starlark evaluation is single-threaded, so certainly the [DapAdapter]
 * itself doesn't do interesting things there).
 */
interface DapAdapterEvalHook {
    /** Hooks the evaluator for this DapAdapter. */
    fun addDapHooks(eval: Evaluator)
}

/** The DAP capabilities that the adapter supports. */
fun dapCapabilities(): Capabilities =
    Capabilities(
        supportsConfigurationDoneRequest = true,
        supportsEvaluateForHovers = true,
        supportsSetVariable = true,
        supportsStepInTargetsRequest = true,
        supportsConditionalBreakpoints = true,
    )

/** Creates a [DapAdapter] and corresponding [DapAdapterEvalHook]. */
fun prepareDapAdapter(
    client: DapAdapterClient,
): Pair<DapAdapter, DapAdapterEvalHook> = prepareDapAdapterImpl(client)
