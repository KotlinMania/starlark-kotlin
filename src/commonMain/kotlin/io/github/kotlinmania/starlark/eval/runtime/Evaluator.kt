// port-lint: source src/eval/runtime/evaluator.rs
package io.github.kotlinmania.starlark.eval.runtime

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

import io.github.kotlinmania.starlark.CallStack
import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.Frame
import io.github.kotlinmania.starlark.any.AnyLifetime
import io.github.kotlinmania.starlark.codemap.FileSpan
import io.github.kotlinmania.starlark.codemap.FileSpanRef
import io.github.kotlinmania.starlark.codemap.ResolvedFileSpan
import io.github.kotlinmania.starlark.collections.Alloca
import io.github.kotlinmania.starlark.collections.StringPool
import io.github.kotlinmania.starlark.environment.FrozenModuleData
import io.github.kotlinmania.starlark.environment.Module
import io.github.kotlinmania.starlark.environment.ModuleSlotId
import io.github.kotlinmania.starlark.eval.HardErrorSoftErrorHandler
import io.github.kotlinmania.starlark.eval.SoftErrorHandler
import io.github.kotlinmania.starlark.eval.bc.Bc
import io.github.kotlinmania.starlark.eval.bc.BcFramePtr
import io.github.kotlinmania.starlark.eval.bc.BcInstrs
import io.github.kotlinmania.starlark.eval.bc.BcOpcode
import io.github.kotlinmania.starlark.eval.bc.BcPtrAddr
import io.github.kotlinmania.starlark.eval.bc.BcStatementLocations
import io.github.kotlinmania.starlark.eval.bc.trace
import io.github.kotlinmania.starlark.eval.compiler.CopySlotFromParent
import io.github.kotlinmania.starlark.eval.compiler.Def
import io.github.kotlinmania.starlark.eval.compiler.DefInfo
import io.github.kotlinmania.starlark.eval.compiler.FrozenDef
import io.github.kotlinmania.starlark.eval.runtime.beforestmt.BeforeStmt
import io.github.kotlinmania.starlark.eval.runtime.beforestmt.BeforeStmtFunc
import io.github.kotlinmania.starlark.eval.runtime.fileloader.FileLoader
import io.github.kotlinmania.starlark.eval.runtime.profile.BcProfile
import io.github.kotlinmania.starlark.eval.runtime.profile.ProfileOrInstrumentationMode
import io.github.kotlinmania.starlark.eval.runtime.profile.StmtProfile
import io.github.kotlinmania.starlark.eval.runtime.profile.TimeFlameProfile
import io.github.kotlinmania.starlark.eval.runtime.profile.TypecheckProfile
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileData
import io.github.kotlinmania.starlark.eval.runtime.profile.data.ProfileDataImpl
import io.github.kotlinmania.starlark.eval.runtime.profile.heap.HeapProfile
import io.github.kotlinmania.starlark.eval.runtime.profile.heap.HeapProfileFormat
import io.github.kotlinmania.starlark.eval.runtime.profile.heap.RetainedHeapProfileMode
import io.github.kotlinmania.starlark.eval.runtime.profile.mode.ProfileMode
import io.github.kotlinmania.starlark.eval.runtime.rustloc.rustLoc
import io.github.kotlinmania.starlark.stdlib.BreakpointConsole
import io.github.kotlinmania.starlark.stdlib.PrintHandler
import io.github.kotlinmania.starlark.stdlib.RealBreakpointConsole
import io.github.kotlinmania.starlark.stdlib.StderrPrintHandler
import io.github.kotlinmania.starlark.typing.EvalException
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.FrozenValueCaptured
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueCaptured
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.valueCapturedGet
import io.github.kotlinmania.starlark.values.types.NativeFunction

private sealed class EvaluatorError(
    override val message: String,
) : Exception(message) {
    data object ProfilingNotEnabled :
        EvaluatorError("Profiling was not enabled")

    data object ProfileDataAlreadyCollected :
        EvaluatorError("Profile data already collected")

    data object RetainedMemoryProfilingCannotBeObtainedFromEvaluator :
        EvaluatorError("Retained memory profiling can be only obtained from `FrozenModule`")

    data object ProfileOrInstrumentationAlreadyEnabled :
        EvaluatorError("Profile or instrumentation already enabled")

    data object TopFrameNotDef :
        EvaluatorError("Top frame is not def (internal error)")

    data object CoverageNotEnabled :
        EvaluatorError("Coverage not enabled")

    data class LocalVariableReferencedBeforeAssignment(
        val name: String,
    ) : EvaluatorError("Local variable `$name` referenced before assignment")

    data object CallstackSizeAlreadySet :
        EvaluatorError("Max callstack size is already set")

    data object ZeroCallstackSize :
        EvaluatorError("Max callstack size cannot be zero")

    data object Cancelled :
        EvaluatorError("Evaluation cancelled")
}

/** Number of bytes to allocate between GC's. */
const val GC_THRESHOLD: Int = 100000

/** Number of instructions to execute before running "infrequent" checks */
private const val INFREQUENT_INSTRUCTION_CHECK_PERIOD: UInt = 1000u

/** Default value for max starlark stack size */
const val DEFAULT_STACK_SIZE: Int = 50

// Kotlin/Multiplatform equivalent of Rust's `eprintln!` / `System.err.println`.
// In commonMain there is no direct stderr access; we use println as a fallback.
private fun eprintln(msg: String) {
    println(msg)
}

// Rust uses compile-time variance checks for `Evaluator` lifetimes.
// Kotlin has no equivalent lifetime system, but we keep no-op parity markers (camelCase per Kotlin conventions).
private fun checkVariance() {
    fun checkCovariantA(a: Evaluator) {
        val loader: FileLoader? = a.loader
        val evalInstrumentation: EvaluationInstrumentation = a.evalInstrumentation
        val extra: AnyLifetime? = a.extra
        val extraMut: AnyLifetime? = a.extraMut
        val printHandler: PrintHandler = a.printHandler
        val softErrorHandler: SoftErrorHandler = a.softErrorHandler
        val isCancelled: () -> Boolean = a.isCancelled
        val self: Evaluator = a

        if (self === self) {
            // Avoid unused warnings while keeping references typed.
            if (loader != loader || evalInstrumentation != evalInstrumentation || extra != extra || extraMut != extraMut) {
                error("unreachable")
            }
            if (printHandler != printHandler || softErrorHandler != softErrorHandler || isCancelled != isCancelled) {
                error("unreachable")
            }
        }
    }
}

/** Just holds things that require using EvaluationCallbacksEnabled so that we can cache whether that needs to be enabled or not. */
internal class EvaluationInstrumentation {
    // Bytecode profile.
    var bcProfile: BcProfile = BcProfile()

    // Extra functions to run on each statement, usually empty
    var beforeStmt: BeforeStmt = BeforeStmt()
    var heapOrFlameProfile: Boolean = false

    // Whether we need to instrument evaluation or not, should be set if before_stmt or bc_profile are enabled.
    var enabled: Boolean = false

    companion object {
        fun new(): EvaluationInstrumentation = EvaluationInstrumentation()
    }

    fun enableHeapOrFlameProfile() {
        heapOrFlameProfile = true
    }

    fun <R> change(f: (EvaluationInstrumentation) -> R): R {
        val r = f(this)
        enabled = bcProfile.enabled() || beforeStmt.enabled() || heapOrFlameProfile
        return r
    }
}

/**
 * Holds everything about an ongoing evaluation (local variables, globals, module resolution etc).
 */
class Evaluator(
    // The module that is being used for this evaluation
    internal val moduleEnv: Module,
) {
    /** Current function (`def` or `lambda`) frame: locals and bytecode stack. */
    internal var currentFrame: BcFramePtr = BcFramePtr.nullPtr()

    /** Current bytecode instructions being executed. Set by runBlock. */
    internal var currentBcInstrs: BcInstrs = BcInstrs.default()

    // How we deal with a `load` function.
    internal var loader: FileLoader? = null

    // `DefInfo` of currently executed module.
    // `DefInfo` of currently execution function can be obtained from call stack.
    internal var moduleDefInfo: DefInfo = DefInfo.empty()

    // Should we enable heap profiling or not
    internal var heapProfile: HeapProfile = HeapProfile()

    // Should we enable flame profiling or not
    internal var timeFlameProfile: TimeFlameProfile = TimeFlameProfile()

    // Is GC disabled for some reason
    internal var disableGc: Boolean = false

    // If true, the interpreter prints to stderr on GC.
    // This is used for debugging.
    internal var verboseGc: Boolean = false

    // Size of the heap when we should next perform a GC.
    internal var nextGcLevel: Int = GC_THRESHOLD

    /** Run static typechecking of the module being evaluated. */
    internal var staticTypechecking: Boolean = false

    // Profiling or instrumentation enabled.
    internal var profileOrInstrumentationMode: ProfileOrInstrumentationMode =
        ProfileOrInstrumentationMode.None

    // Used for line profiling
    private var stmtProfile: StmtProfile = StmtProfile.new()

    // Holds things that require hooking into evaluation.
    internal var evalInstrumentation: EvaluationInstrumentation = EvaluationInstrumentation()

    // Total time spent in runtime typechecking.
    // Filled only if runtime typechecking profiling is enabled.
    internal var typecheckProfile: TypecheckProfile = TypecheckProfile()

    // Used for stack-like allocation
    private val alloca: Alloca = Alloca()

    // Another stack-like allocation
    internal val stringPool: StringPool = StringPool()

    /** Field that can be used for any purpose you want (can store types you define).
     * Typically accessed via native functions you also define. */
    var extra: AnyLifetime? = null

    /** Like `extra`, but mutable */
    var extraMut: AnyLifetime? = null

    /** Called to perform console IO each time `breakpoint` function is called. */
    internal var breakpointHandler: (() -> BreakpointConsole)? = null

    /** Use in implementation of `print` function. */
    internal var printHandler: PrintHandler = StderrPrintHandler()

    /** Deprecation handler. */
    internal var softErrorHandler: SoftErrorHandler = HardErrorSoftErrorHandler

    /** Max size of starlark stack */
    internal var maxCallstackSize: Int? = null

    // The Starlark-level call-stack of functions.
    // Must go last because it's quite a big structure
    internal var callStack: CheapCallStack = CheapCallStack()

    /** Function to check if evaluation should be cancelled early */
    internal var isCancelled: () -> Boolean = { false }

    /** A counter to track when to perform "infrequent" checks like cancellation, timeouts, etc */
    internal var infrequentInstrCheckCounter: UInt = 0u

    /** Disables garbage collection from now onwards. Cannot be re-enabled.
     * Usually called because you have captured [Value]'s unsafely, either in
     * global variables or the [extra] field. */
    fun disableGc() {
        disableGc = true
    }

    /** Enable GC logging. */
    fun verboseGc() {
        verboseGc = true
    }

    /**
     * Enable static typechecking. For example:
     *
     * ```python
     * def foo() -> int: return "hello"
     * ```
     *
     * would fail when static typechecking is enabled even if `foo` is never called.
     */
    fun enableStaticTypechecking(enable: Boolean) {
        staticTypechecking = enable
    }

    /**
     * Set the [FileLoader] used to resolve `load()` statements.
     * A list of all load statements can be obtained through
     * [AstModule.loads][io.github.kotlinmania.starlark.syntax.AstModule.loads].
     */
    fun setLoader(loader: FileLoader) {
        this.loader = loader
    }

    /**
     * Enable profiling, allowing [Evaluator.genProfile] to be used.
     * Profilers add overhead, and while some profilers can be used together,
     * it's better to run at most one profiler at a time.
     */
    fun enableProfile(mode: ProfileMode) {
        if (profileOrInstrumentationMode != ProfileOrInstrumentationMode.None) {
            throw EvaluatorError.ProfileOrInstrumentationAlreadyEnabled
        }

        profileOrInstrumentationMode = ProfileOrInstrumentationMode.Profile(mode)

        when (mode) {
            ProfileMode.HeapAllocated,
            ProfileMode.HeapRetained,
            ProfileMode.HeapSummaryAllocated,
            ProfileMode.HeapFlameAllocated,
            ProfileMode.HeapSummaryRetained,
            ProfileMode.HeapFlameRetained,
            -> {
                heapProfile.enable()

                when (mode) {
                    ProfileMode.HeapFlameRetained ->
                        moduleEnv
                            .enableRetainedHeapProfile(RetainedHeapProfileMode.Flame)
                    ProfileMode.HeapSummaryRetained ->
                        moduleEnv
                            .enableRetainedHeapProfile(RetainedHeapProfileMode.Summary)
                    ProfileMode.HeapRetained -> {
                        moduleEnv
                            .enableRetainedHeapProfile(RetainedHeapProfileMode.FlameAndSummary)
                    }
                    else -> {}
                }

                evalInstrumentation
                    .change { it.enableHeapOrFlameProfile() }

                // Disable GC because otherwise why lose the profile records, as we use the heap
                // to store a complete list of what happened in linear order.
                disableGc = true
            }
            ProfileMode.Statement, ProfileMode.Coverage -> {
                stmtProfile.enable()
                beforeStmtFn { span, continued, eval -> eval.stmtProfile.beforeStmt(span) }
            }
            ProfileMode.TimeFlame -> {
                timeFlameProfile.enable()
                evalInstrumentation
                    .change { it.enableHeapOrFlameProfile() }
            }
            ProfileMode.Bytecode -> {
                evalInstrumentation
                    .change { it.bcProfile.enable1() }
            }
            ProfileMode.BytecodePairs -> {
                evalInstrumentation
                    .change { it.bcProfile.enable2() }
            }
            ProfileMode.Typecheck -> {
                typecheckProfile.enabled = true
            }
            ProfileMode.None -> {}
        }
    }

    /**
     * Generate profile for a given mode.
     * Only valid if corresponding profiler was enabled.
     */
    fun genProfile(): ProfileData {
        val mode =
            when (val pMode = profileOrInstrumentationMode) {
                ProfileOrInstrumentationMode.None -> {
                    throw Error.newOther(
                        EvaluatorError.ProfilingNotEnabled,
                    )
                }
                ProfileOrInstrumentationMode.Collected -> {
                    throw Error.newOther(
                        EvaluatorError.ProfileDataAlreadyCollected,
                    )
                }
                is ProfileOrInstrumentationMode.Profile -> pMode.mode
            }
        profileOrInstrumentationMode = ProfileOrInstrumentationMode.Collected
        return when (mode) {
            ProfileMode.HeapAllocated ->
                heapProfile
                    .gen(heap(), HeapProfileFormat.FlameGraphAndSummary)
            ProfileMode.HeapSummaryAllocated ->
                heapProfile
                    .gen(heap(), HeapProfileFormat.Summary)
            ProfileMode.HeapFlameAllocated ->
                heapProfile
                    .gen(heap(), HeapProfileFormat.FlameGraph)
            ProfileMode.HeapSummaryRetained,
            ProfileMode.HeapFlameRetained,
            ProfileMode.HeapRetained,
            -> throw Error.newOther(
                EvaluatorError.RetainedMemoryProfilingCannotBeObtainedFromEvaluator,
            )
            ProfileMode.Statement -> stmtProfile.gen()
            ProfileMode.Coverage -> stmtProfile.genCoverage()
            ProfileMode.Bytecode -> genBcProfile()
            ProfileMode.BytecodePairs -> genBcPairsProfile()
            ProfileMode.TimeFlame -> timeFlameProfile.gen()
            ProfileMode.Typecheck -> typecheckProfile.gen()
            ProfileMode.None ->
                ProfileData(
                    profile = ProfileDataImpl.None,
                )
        }
    }

    /**
     * Get code coverage.
     *
     * Works if statement profile is enabled.
     *
     * Note coverage is not precise, because
     * * some optimizer transformations may create incorrect spans
     * * some optimizer transformations may remove statements
     */
    fun coverage(): Set<ResolvedFileSpan> {
        val pMode = profileOrInstrumentationMode
        if (pMode is ProfileOrInstrumentationMode.Profile && pMode.mode == ProfileMode.Coverage) {
            return stmtProfile.coverage()
        }
        throw Error.newOther(EvaluatorError.CoverageNotEnabled)
    }

    /**
     * Enable interactive `breakpoint()`. When enabled, `breakpoint()`
     * reads commands from stdin and write to stdout.
     * When disabled (default), `breakpoint()` function results in error.
     */
    fun enableTerminalBreakpointConsole() {
        breakpointHandler = RealBreakpointConsole.factory()
    }

    /** Obtain the current call-stack, suitable for use in diagnostics. */
    fun callStack(): CallStack = callStack.toDiagnosticFrames(InlinedFrames())

    /** Obtain the top frame on the call-stack. May be `null` if the
     * call happened via native functions. */
    fun callStackTopFrame(): Frame? = callStack.topFrame()

    /** Current size (in frames) of the stack. */
    fun callStackCount(): Int = callStack.count()

    /** Obtain the top location on the call-stack. May be `null` if the
     * call happened via native functions. */
    fun callStackTopLocation(): FileSpan? = callStack.topLocation()

    /** Obtain the nth location on the call-stack. May be `null` if the
     * stack is not that deep. n=0 is the top of the stack. */
    fun callStackNthLocation(n: Int): FileSpan? = callStack.nthLocation(n)

    internal fun beforeStmtFn(
        f: (FileSpanRef, Boolean, Evaluator) -> Unit,
    ) {
        beforeStmt(BeforeStmtFunc.fromFn(f))
    }

    internal fun beforeStmt(f: BeforeStmtFunc) {
        evalInstrumentation
            .change { it.beforeStmt.beforeStmt.add(f) }
    }

    /** This function is used by DAP, and it is not public API. */
    fun beforeStmtForDap(f: BeforeStmtFunc) {
        beforeStmt(f)
    }

    /** Set the handler invoked when `print` function is used. */
    fun setPrintHandler(handler: PrintHandler) {
        printHandler = handler
    }

    /** Set deprecation handler. If not set, deprecations are treated as hard errors. */
    fun setSoftErrorHandler(handler: SoftErrorHandler) {
        softErrorHandler = handler
    }

    /** Set canceled-checking function. This function is called periodically to check if the evaluator should return early (with an error condition). */
    fun setCheckCancelled(isCanceled: () -> Boolean) {
        isCancelled = isCanceled
    }

    internal fun addCallStackDiagnostics(e: Error): Error {
        e.setCallStack { callStack.toDiagnosticFrames(InlinedFrames()).frames }
        return e
    }

    /**
     * Called to add an entry to the call stack, by the function being invoked.
     * Called for all types of function, including those written in Kotlin.
     */
    internal fun <R> withCallStack(
        function: Value,
        span: FrozenRef<FrameSpan>?,
        within: (Evaluator) -> Result<R>,
    ): Result<R> {
        callStack.push(function, span)
        // Must always call .pop regardless
        try {
            val result = within(this)
            val failure = result.exceptionOrNull()
            return if (failure is Error) {
                Result.failure(addCallStackDiagnostics(failure))
            } else {
                result
            }
        } catch (e: Error) {
            throw addCallStackDiagnostics(e)
        } finally {
            callStack.pop()
        }
    }

    /** The active heap where [Value]s are allocated. */
    fun heap(): Heap = moduleEnv.heap()

    /** Module which was passed to the evaluator. */
    fun module(): Module = moduleEnv

    /**
     * The frozen heap. It's possible to allocate [FrozenValue][io.github.kotlinmania.starlark.values.FrozenValue]s here,
     * but often not a great idea, as they will remain allocated as long
     * as the results of this execution are required.
     * Suitable for use with [addReference][FrozenHeap.addReference]
     * and [OwnedFrozenValue.ownedFrozenValue][io.github.kotlinmania.starlark.values.OwnedFrozenValue.ownedFrozenValue].
     */
    fun frozenHeap(): FrozenHeap = moduleEnv.frozenHeap()

    internal fun getSlotModule(slot: ModuleSlotId): Result<Value> {
        val name =
            try {
                when (val frozenModule = topFrameDefFrozenModule(false)) {
                    null ->
                        moduleEnv
                            .mutableNames()
                            .getSlot(slot)
                            ?.asStr()
                    else -> frozenModule.asRef().getSlotName(slot)?.asStr()
                }
            } catch (e: Exception) {
                null
            } ?: "<unknown>"

        fun error(eval: Evaluator, slot: ModuleSlotId): Error {
            return Error.newOther(
                EvaluatorError.LocalVariableReferencedBeforeAssignment(name),
            )
        }

        val value =
            when (val frozenModule = topFrameDefFrozenModule(false)) {
                null -> {
                    val v = moduleEnv.slots().getSlot(slot)
                    println("[DEBUG_SLOT] getSlotModule slot=$slot name=$name val_repr=${v?.toRepr()} val_identity=${v?.let{System.identityHashCode(it)}} val_ptr_idx=${v?.ptr?.unpackPtrOpt()}")
                    v
                }
                else -> {
                    val fv = frozenModule.asRef().getSlot(slot)
                    println("[DEBUG_SLOT] getSlotModule FROZEN slot=$slot name=$name val_repr=${fv?.toValue()?.toRepr()} val_ptr_idx=${fv?.ptr?.toPointer()?.unpackPtrOpt()}")
                    fv?.let { Value.newFrozen(it) }
                }
            }
        return if (value != null) {
            Result.success(value)
        } else {
            Result.failure(error(this, slot))
        }
    }

    internal fun localVarReferencedBeforeAssignment(slot: LocalSlotId): Error {
        val defInfo =
            try {
                topFrameDefInfo()
            } catch (e: Error) {
                return e
            }
        val names = defInfo.used
        val name = names[slot.index.toInt()].asStr()
        return Error.newOther(
            EvaluatorError.LocalVariableReferencedBeforeAssignment(name),
        )
    }

    internal fun getSlotLocal(
        frame: BcFramePtr,
        slot: LocalSlotId,
    ): Result<Value> {
        // We access locals from explicitly passed frame because it is faster.
        check(currentFrame == frame)

        val value = frame.getSlot(slot.toCapturedOrNot())
        return if (value != null) {
            Result.success(value)
        } else {
            Result.failure(localVarReferencedBeforeAssignment(slot))
        }
    }

    internal fun getSlotLocalCaptured(
        slot: LocalCapturedSlotId,
    ): Result<Value> {
        val valueCaptured =
            getSlotLocal(currentFrame, LocalSlotId(slot.index))
                .getOrElse { return Result.failure(it) }
        val result = valueCapturedGet(valueCaptured)
        return if (result != null) {
            Result.success(result)
        } else {
            Result.failure(localVarReferencedBeforeAssignment(LocalSlotId(slot.index)))
        }
    }

    internal fun cloneSlotCapture(
        copy: CopySlotFromParent,
        targetDefInfo: DefInfo,
    ): Value =
        when (val valueCaptured = currentFrame.getSlot(copy.parent)) {
            null -> {
                val newValueCaptured = heap().allocComplex(ValueCaptured.new(null))
                currentFrame.setSlot(copy.parent, newValueCaptured)
                newValueCaptured
            }
            else -> {
                check(
                    valueCaptured.downcastRef<ValueCaptured>() != null ||
                        valueCaptured.downcastRef<FrozenValueCaptured>() != null,
                ) {
                    "slot ${copy.parent.index} (${
                        targetDefInfo.used.getOrNull(copy.child.index.toInt())?.asStr() ?: ""
                    }) is expected to be ValueCaptured, it is $valueCaptured (${
                        valueCaptured.getType()
                    }); def location: ${targetDefInfo.signatureSpan}"
                }
                valueCaptured
            }
        }

    /**
     * Set a variable in the top-level module currently being processed.
     * This may not be the module the function is being called in.
     *
     * Any variables which are set will be available in the [Module] after evaluation returns.
     * If those variables are _also_ existing top-level variables, then the program from that point on
     * will incorporate those values. If they aren't existing top-level variables, they will be ignored.
     * These details are subject to change.
     * As such, use this API with a healthy dose of caution and in limited settings.
     */
    fun setModuleVariableAtSomePoint(
        name: String,
        value: Value,
    ) {
        value.exportAs(name, this).getOrThrow()
        moduleEnv.set(name, value)
    }

    internal fun setSlotModule(slot: ModuleSlotId, value: Value) {
        moduleEnv.slots().setSlot(slot, value)
    }

    internal fun setSlotLocalCaptured(slot: LocalCapturedSlotId, value: Value) {
        val localSlot = LocalSlotId(slot.index)
        when (val valueCaptured = currentFrame.getSlot(localSlot.toCapturedOrNot())) {
            null -> {
                val newValueCaptured = heap().allocComplex(ValueCaptured.new(value))
                currentFrame.setSlot(localSlot.toCapturedOrNot(), newValueCaptured)
            }
            else -> {
                val vc =
                    valueCaptured.downcastRef<ValueCaptured>()
                        ?: error("not a ValueCaptured")
                vc.set(value)
            }
        }
    }

    /** Take a value from the local slot and store it back wrapped in [ValueCaptured]. */
    internal fun wrapLocalSlotCaptured(slot: LocalSlotId) {
        val value =
            currentFrame.getSlot(slot.toCapturedOrNot())
                ?: error("slot unset")
        check(value.downcastRef<ValueCaptured>() == null)
        val valueCaptured = heap().allocComplex(ValueCaptured.new(value))
        currentFrame.setSlot(slot.toCapturedOrNot(), valueCaptured)
    }

    internal fun checkReturnType(ret: Value): Result<Unit> {
        val func = callStack.topNthFunction(0)
        val defRef = func.downcastRef<Def>()
        if (defRef != null) {
            return defRef.checkReturnType(ret, this)
        }
        val frozenDefRef = func.downcastRef<FrozenDef>()
        if (frozenDefRef != null) {
            return frozenDefRef.checkReturnType(ret, this)
        }
        return Result.failure(
            Error.newOther(EvaluatorError.TopFrameNotDef),
        )
    }

    private fun funcToDefInfo(func: Value): DefInfo {
        func.downcastRef<Def>()?.let { return it.defInfo }
        func.downcastRef<FrozenDef>()?.let { return it.defInfo }
        if (func.isNone()) {
            // For module, it is `None`.
            return moduleDefInfo
        }
        throw Error.newOther(EvaluatorError.TopFrameNotDef)
    }

    internal fun topFrameDefInfo(): DefInfo {
        val func = callStack.topNthFunction(0)
        return funcToDefInfo(func)
    }

    internal fun topFrameDefFrozenModule(
        forDebugger: Boolean,
    ): FrozenRef<FrozenModuleData>? {
        val func = topFrameMaybeForDebugger(forDebugger)
        func.downcastRef<FrozenDef>()?.let { return it.module.loadRelaxed() }
        func.downcastRef<Def>()?.let { return it.module.loadRelaxed() }
        return null
    }

    private fun topFrameMaybeForDebugger(forDebugger: Boolean): Value {
        val func = callStack.topNthFunction(0)
        if (forDebugger && func.downcastRef<NativeFunction>() != null) {
            // If top frame is `breakpoint` or `debug_evaluate`, it will be skipped.
            return callStack.topNthFunction(1)
        }
        return func
    }

    /** Gets the "top frame" for debugging. If the real top frame is `breakpoint` or `debug_evaluate`
     * it will be skipped. This should only be used for the starlark debugger. */
    internal fun topFrameDefInfoForDebugger(): DefInfo {
        val func = topFrameMaybeForDebugger(true)
        return funcToDefInfo(func)
    }

    /** Cause a GC to be triggered next time it's possible. */
    internal fun triggerGc() {
        // We will GC next time we can, since the threshold is if 0 or more bytes are allocated
        nextGcLevel = 0
    }

    private fun trace(tracer: Tracer) {
        timeFlameProfile.recordCallEnter(constFrozenString("trace/walk").toValue())
        moduleEnv.trace(tracer)
        currentFrame.trace(tracer)
        callStack.trace(tracer)
        timeFlameProfile.recordCallExit()
        timeFlameProfile.recordCallEnter(constFrozenString("trace/walk (profiling)").toValue())
        timeFlameProfile.trace(tracer)
        timeFlameProfile.recordCallExit()
    }

    /**
     * Perform a garbage collection.
     * After this operation all [Value]s not reachable from the evaluator will be invalid,
     * and using them will lead to undefined behavior.
     * Do not call during Starlark evaluation.
     */
    fun garbageCollect() {
        if (verboseGc) {
            eprintln(
                "Starlark: allocated bytes: ${heap().allocatedBytes()}, starting GC...",
            )
        }

        stmtProfile.beforeStmt(
            rustLoc("evaluator.kt", 704).asRef().span.fileSpanRef(),
        )

        timeFlameProfile.recordCallEnter(constFrozenString("GC").toValue())

        // Garbage collection does two time-consuming tasks:
        // 1. It calls the closure we provide here to trace the existing
        //    heap, moving objects to the new heap.
        // 2. It returns, implicitly dropping the old arena and any objects
        //    it may still contain.
        //
        // The best way to measure the former and the latter are to record
        // enter/exits around our call to self.trace, and then an enter at
        // the end of the closure. Once we regain control we record the
        // matching exit, which covers the time it took to drop the old
        // heap.
        heap().garbageCollect { tracer ->
            trace(tracer)

            // See above, this enter begins right as our closure ends, and
            // will catch the implicit drop of the old arena as the
            // self.heap() lets it auto-drop on return from the
            // .garbage_collect()
            timeFlameProfile.recordCallEnter(constFrozenString("cleanup").toValue())
        }
        // This exits the "cleanup" in the closure above
        timeFlameProfile.recordCallExit()

        // For the "GC" above
        timeFlameProfile.recordCallExit()

        if (verboseGc) {
            eprintln(
                "Starlark: GC complete. Allocated bytes: ${heap().allocatedBytes()}.",
            )
        }
    }

    /**
     * Note that the finalizer for the `T` will not be called. That's safe if there is no finalizer,
     * or you call it yourself.
     */
    internal fun <T, R> allocaUninit(len: Int, k: (Array<Any?>, Evaluator) -> R): R = alloca.allocaUninit<T, R>(len) { xs -> k(xs, this) }

    /**
     * Allocate `len` elements, initialize them with `init` function, and invoke
     * a callback `k` with the pointer to the allocated memory and `self`.
     */
    internal fun <T, R> allocaInit(len: Int, init: () -> T, k: (MutableList<T>, Evaluator) -> R): R = alloca.allocaInit(len, init) { xs -> k(xs, this) }

    /** Concat two slices and invoke the callback with the result. */
    internal fun <T, R> allocaConcat(x: List<T>, y: List<T>, k: (List<T>, Evaluator) -> R): R = alloca.allocaConcat(x, y) { xs -> k(xs, this) }

    internal fun genBcProfile(): ProfileData = evalInstrumentation.bcProfile.genBcProfile()

    internal fun genBcPairsProfile(): ProfileData = evalInstrumentation.bcProfile.genBcPairsProfile()

    private fun evalBcWithCallbacks(
        def: Value,
        bc: Bc,
    ): Result<Value> {
        check(evalInstrumentation.enabled)
        if (evalInstrumentation.heapOrFlameProfile) {
            heapProfile.recordCallEnter(def, heap())
            timeFlameProfile.recordCallEnter(def)
            val res = bc.run(this, EvalCallbacksDisabled)
            heapProfile.recordCallExit(heap())
            timeFlameProfile.recordCallExit()
            return res
        } else {
            val mode =
                when {
                    evalInstrumentation.beforeStmt.enabled() && !evalInstrumentation.bcProfile.enabled() ->
                        EvalCallbacksMode.BeforeStmt
                    !evalInstrumentation.beforeStmt.enabled() && evalInstrumentation.bcProfile.enabled() ->
                        EvalCallbacksMode.BcProfile
                    evalInstrumentation.beforeStmt.enabled() && evalInstrumentation.bcProfile.enabled() ->
                        return Result.failure(
                            EvalException("both before_stmt and bc_profile are enabled"),
                        )
                    else ->
                        return Result.failure(
                            EvalException("neither before_stmt nor bc_profile are enabled"),
                        )
                }
            return bc.run(
                this,
                EvalCallbacksEnabled(
                    mode = mode,
                    stmtLocs = bc.instrs.stmtLocs,
                    bcStartPtr = bc.instrs.startPtr(),
                ),
            )
        }
    }

    internal fun evalBc(def: Value, bc: Bc): Result<Value> =
        if (evalInstrumentation.enabled) {
            evalBcWithCallbacks(def, bc)
        } else {
            bc.run(this, EvalCallbacksDisabled)
        }

    /**
     * Sets max call stack size.
     * Stack allocation will happen on entry point of evaluation if not allocated yet.
     */
    fun setMaxCallstackSize(stackSize: Int) {
        if (stackSize == 0) {
            throw EvaluatorError.ZeroCallstackSize
        }
        if (maxCallstackSize != null) {
            throw EvaluatorError.CallstackSizeAlreadySet
        }
        maxCallstackSize = stackSize
    }

    internal fun reportForwardProgress(): Result<Unit> {
        infrequentInstrCheckCounter++
        if (infrequentInstrCheckCounter >= INFREQUENT_INSTRUCTION_CHECK_PERIOD) {
            val result = runInfrequentInstrChecks()
            infrequentInstrCheckCounter = 0u
            return result
        }
        return Result.success(Unit)
    }

    internal fun runInfrequentInstrChecks(): Result<Unit> {
        if (isCancelled()) {
            return Result.failure(
                Error.newOther(EvaluatorError.Cancelled),
            )
        }
        return Result.success(Unit)
    }
}

interface EvaluationCallbacks {
    fun beforeInstr(
        eval: Evaluator,
        ip: BcPtrAddr,
        opcode: BcOpcode,
    )
}

object EvalCallbacksDisabled : EvaluationCallbacks {
    override fun beforeInstr(
        eval: Evaluator,
        ip: BcPtrAddr,
        opcode: BcOpcode,
    ) {
        // No-op
    }
}

enum class EvalCallbacksMode {
    BcProfile,
    BeforeStmt,
}

class EvalCallbacksEnabled(
    val mode: EvalCallbacksMode,
    val stmtLocs: BcStatementLocations,
    val bcStartPtr: BcPtrAddr,
) : EvaluationCallbacks {
    private fun beforeStmt(eval: Evaluator, ip: BcPtrAddr) {
        val offset = ip.offsetFrom(bcStartPtr)
        val stmtAt = stmtLocs.stmtAt(offset) ?: return
        val (loc, continued) = stmtAt
        beforeStmtFn(loc.span, continued, eval)
    }

    override fun beforeInstr(
        eval: Evaluator,
        ip: BcPtrAddr,
        opcode: BcOpcode,
    ) {
        when (mode) {
            EvalCallbacksMode.BcProfile -> {
                eval.evalInstrumentation.bcProfile.beforeInstr(opcode)
            }
            EvalCallbacksMode.BeforeStmt -> beforeStmt(eval, ip)
        }
    }
}

// This function should be called before every meaningful statement (continued==false), and after a call that returns into a previously entered statement (continued==true).
// The purposes are GC, profiling and debugging.
//
// This function is called only if `before_stmt` is set before compilation start.
fun beforeStmtFn(
    span: FrameSpan,
    continued: Boolean,
    eval: Evaluator,
) {
    check(eval.evalInstrumentation.beforeStmt.enabled()) {
        "this code should only be called if `before_stmt` is set"
    }
    val fs =
        eval.evalInstrumentation.change { evalInstrumentation ->
            val taken = evalInstrumentation.beforeStmt.beforeStmt.toMutableList()
            evalInstrumentation.beforeStmt.beforeStmt.clear()
            taken
        }
    var result: Exception? = null
    for (f in fs) {
        if (result == null) {
            try {
                f.call(span.span.fileSpanRef(), continued, eval)
            } catch (e: Exception) {
                result = e
            }
        }
    }
    val added =
        eval.evalInstrumentation.change { evalInstrumentation ->
            val added = evalInstrumentation.beforeStmt.beforeStmt.toMutableList()
            evalInstrumentation.beforeStmt.beforeStmt.clear()
            evalInstrumentation.beforeStmt.beforeStmt.addAll(fs)
            added
        }
    check(added.isEmpty()) {
        "`before_stmt` cannot be modified during evaluation"
    }
    if (result != null) {
        throw result
    }
}
