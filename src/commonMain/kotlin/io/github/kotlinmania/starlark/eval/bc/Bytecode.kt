// port-lint: source src/eval/bc/bytecode.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

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

/** Unsorted/core interpreter stuff. */

import io.github.kotlinmania.starlark_kotlin.eval.bc.BcFramePtr
import io.github.kotlinmania.starlark_kotlin.eval.bc.InstrControl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.EvaluationCallbacks
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.typing.StarlarkError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/** Ready to execute bytecode. */
// #[derive(Default)]
// pub(crate) struct Bc
class Bc(
    val instrs: BcInstrs = BcInstrs.default(),
    /** Number of local variable slots. */
    val localCount: UInt = 0u,
    /** Max stack size in values (`Value`). */
    val maxStackSize: UInt = 0u,
    /** Max depth of loops. */
    val maxLoopDepth: LoopDepth = LoopDepth(),
) {
    companion object {
        /** Find span for instruction. */
        // pub(crate) fn slow_arg_at_ptr(addr_ptr: BcPtrAddr<'_>) -> &BcInstrSlowArg
        fun slowArgAtPtr(addrPtr: BcPtrAddr, bcInstrs: BcInstrs): BcInstrSlowArg {
            var ptr = addrPtr
            while (true) {
                val opcode = bcInstrs.getOpcodeAt(ptr)
                if (opcode == BcOpcode.End) {
                    val endArg = bcInstrs.getArgAt(ptr) as BcInstrEndArg
                    val slowArgs = endArg.slowArgs
                    val endAddr = endArg.endAddr
                    val codeStartPtr = ptr.sub(endAddr)
                    val addr = addrPtr.offsetFrom(codeStartPtr)
                    for ((nextAddr, nextSpan) in slowArgs) {
                        if (nextAddr == addr) {
                            return nextSpan
                        }
                    }
                    error("span not found for addr: $addr")
                }
                ptr = ptr.add(opcode.sizeOfRepr())
            }
        }

        // pub(crate) fn wrap_error_for_instr_ptr(ptr: BcPtrAddr, e: crate::Error, eval: &Evaluator) -> EvalException
        fun wrapErrorForInstrPtr(
            ptr: BcPtrAddr,
            e: StarlarkError,
            eval: Evaluator,
            bcInstrs: BcInstrs,
        ): EvalException {
            val span = slowArgAtPtr(ptr, bcInstrs).span
            return addSpanToExprError(e, span, eval)
        }
    }

    /**
     * Run the bytecode in the current frame allocated in the evaluator.
     *
     * Frame must be allocated properly, otherwise it will likely result in memory corruption.
     */
    // pub(crate) fn run<'v, EC: EvaluationCallbacks>(&self, eval: &mut Evaluator, ec: &mut EC) -> Result<Value, EvalException>
    fun run(eval: Evaluator, ec: EvaluationCallbacks): Result<Value> {
        return runBlock(eval, ec, instrs.startPtr(), instrs)
    }

    // pub(crate) fn dump_debug(&self) -> String
    fun dumpDebug(): String {
        return buildString {
            appendLine("Max stack size: $maxStackSize")
            appendLine("Instructions:")
            instrs.dumpDebug().lines().forEach { line ->
                appendLine("  $line")
            }
        }
    }
}

/**
 * Execute one instruction by dispatching on the opcode.
 *
 * In Rust, this uses generic dispatch via BcOpcodeHandler trait and proc-macro
 * generated code. In Kotlin, we dispatch using the opcode to look up the
 * instruction argument from the buffer and call the appropriate handler.
 */
// fn step<'v, 'b, EC: EvaluationCallbacks>(eval: &mut Evaluator, ec: &mut EC, frame: BcFramePtr, ip: BcPtrAddr) -> InstrControl
private fun step(
    eval: Evaluator,
    ec: EvaluationCallbacks,
    frame: BcFramePtr,
    ip: BcPtrAddr,
    bcInstrs: BcInstrs,
): InstrControl {
    val opcode = bcInstrs.getOpcodeAt(ip)
    val arg = bcInstrs.getArgAt(ip)

    runCatching { ec.beforeInstr(eval, ip, opcode) }.getOrElse {
        val starlarkError = if (it is StarlarkError) it else StarlarkError(it.message ?: "unknown error", it)
        return InstrControl.Err(starlarkError)
    }

    return dispatchInstruction(opcode, eval, frame, ip, arg)
}

/**
 * Dispatch an instruction by opcode.
 *
 * This is the Kotlin equivalent of the Rust proc-macro generated dispatch.
 * Each opcode maps to a specific instruction implementation.
 */
private fun dispatchInstruction(
    opcode: BcOpcode,
    eval: Evaluator,
    frame: BcFramePtr,
    ip: BcPtrAddr,
    arg: Any?,
): InstrControl {
    // Helper for InstrNoFlowImpl-based instructions: execute and wrap result.
    fun noFlow(impl: InstrNoFlowImpl): InstrControl {
        val result = impl.runWithArgs(eval, frame, ip, arg ?: Unit)
        return if (result.isSuccess) {
            InstrControl.Next(ip.add(opcode.sizeOfRepr()))
        } else {
            InstrControl.Err(
                if (result.exceptionOrNull() is StarlarkError)
                    result.exceptionOrNull() as StarlarkError
                else
                    StarlarkError(result.exceptionOrNull()?.message ?: "", result.exceptionOrNull())
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    return when (opcode) {
        // --- No-flow instructions ---
        BcOpcode.Const -> noFlow(InstrConstImpl)
        BcOpcode.LoadLocal -> noFlow(InstrLoadLocalImpl)
        BcOpcode.LoadLocalCaptured -> noFlow(InstrLoadLocalCapturedImpl)
        BcOpcode.LoadModule -> noFlow(InstrLoadModuleImpl)
        BcOpcode.Mov -> noFlow(InstrMovImpl)
        BcOpcode.StoreLocalCaptured -> noFlow(InstrStoreLocalCapturedImpl)
        BcOpcode.StoreModule -> noFlow(InstrStoreModuleImpl)
        BcOpcode.StoreModuleAndExport -> noFlow(InstrStoreModuleAndExportImpl)
        BcOpcode.Unpack -> noFlow(InstrUnpackImpl)
        BcOpcode.ArrayIndex -> noFlow(InstrArrayIndexImpl)
        BcOpcode.SetArrayIndex -> noFlow(InstrSetArrayIndexImpl)
        BcOpcode.ArrayIndexSet -> noFlow(InstrArrayIndexSetImpl)
        BcOpcode.Slice -> noFlow(InstrSliceImpl)
        BcOpcode.ObjectField -> noFlow(InstrObjectFieldImpl)
        BcOpcode.SetObjectField -> noFlow(InstrSetObjectFieldImpl)
        BcOpcode.Eq -> noFlow(InstrEqConstImpl)
        BcOpcode.EqConst -> noFlow(InstrEqConstImpl)
        BcOpcode.EqPtr -> noFlow(InstrEqPtrImpl)
        BcOpcode.EqStr -> noFlow(InstrEqStrImpl)
        BcOpcode.EqInt -> noFlow(InstrEqIntImpl)
        BcOpcode.Not -> noFlow(InstrUnOpWrapper(InstrNotImpl))
        BcOpcode.Minus -> noFlow(InstrUnOpWrapper(InstrMinusImpl))
        BcOpcode.Plus -> noFlow(InstrUnOpWrapper(InstrPlusImpl))
        BcOpcode.BitNot -> noFlow(InstrUnOpWrapper(InstrBitNotImpl))
        BcOpcode.Less -> noFlow(InstrBinOpWrapper(InstrCompareWrapper(InstrLessImpl)))
        BcOpcode.Greater -> noFlow(InstrBinOpWrapper(InstrCompareWrapper(InstrGreaterImpl)))
        BcOpcode.LessOrEqual -> noFlow(InstrBinOpWrapper(InstrCompareWrapper(InstrLessOrEqualImpl)))
        BcOpcode.GreaterOrEqual -> noFlow(InstrBinOpWrapper(InstrCompareWrapper(InstrGreaterOrEqualImpl)))
        BcOpcode.In -> noFlow(InstrBinOpWrapper(InstrInImpl))
        BcOpcode.Add -> noFlow(InstrBinOpWrapper(InstrAddImpl))
        BcOpcode.AddAssign -> noFlow(InstrBinOpWrapper(InstrAddAssignImpl))
        BcOpcode.Sub -> noFlow(InstrBinOpWrapper(InstrSubImpl))
        BcOpcode.Multiply -> noFlow(InstrBinOpWrapper(InstrMultiplyImpl))
        BcOpcode.Percent -> noFlow(InstrBinOpWrapper(InstrPercentImpl))
        BcOpcode.PercentSOne -> noFlow(InstrPercentSOneImpl)
        BcOpcode.FormatOne -> noFlow(InstrFormatOneImpl)
        BcOpcode.Divide -> noFlow(InstrBinOpWrapper(InstrDivideImpl))
        BcOpcode.FloorDivide -> noFlow(InstrBinOpWrapper(InstrFloorDivideImpl))
        BcOpcode.BitAnd -> noFlow(InstrBinOpWrapper(InstrBitAndImpl))
        BcOpcode.BitOr -> noFlow(InstrBinOpWrapper(InstrBitOrImpl))
        BcOpcode.BitOrAssign -> noFlow(InstrBinOpWrapper(InstrBitOrAssignImpl))
        BcOpcode.BitXor -> noFlow(InstrBinOpWrapper(InstrBitXorImpl))
        BcOpcode.LeftShift -> noFlow(InstrBinOpWrapper(InstrLeftShiftImpl))
        BcOpcode.RightShift -> noFlow(InstrBinOpWrapper(InstrRightShiftImpl))
        BcOpcode.Len -> noFlow(InstrUnOpWrapper(InstrLenImpl))
        BcOpcode.Type -> noFlow(InstrUnOpWrapper(InstrTypeImpl))
        BcOpcode.TypeIs -> noFlow(InstrTypeIsImpl)
        BcOpcode.IsInstance -> noFlow(InstrIsInstanceImpl)
        BcOpcode.TupleNPop -> noFlow(InstrTupleNPopImpl)
        BcOpcode.ListNew -> noFlow(InstrListNewImpl)
        BcOpcode.ListNPop -> noFlow(InstrListNPopImpl)
        BcOpcode.ListOfConsts -> noFlow(InstrListOfConstsImpl)
        BcOpcode.DictNew -> noFlow(InstrDictNewImpl)
        BcOpcode.DictNPop -> noFlow(InstrDictNPopImpl)
        BcOpcode.DictOfConsts -> noFlow(InstrDictOfConstsImpl)
        BcOpcode.DictConstKeys -> noFlow(InstrDictConstKeysImpl)
        BcOpcode.CheckType -> noFlow(InstrCheckTypeImpl)
        BcOpcode.ArrayIndex2 -> noFlow(InstrArrayIndex2Impl)
        BcOpcode.Def -> noFlow(InstrDefImpl)
        BcOpcode.Call -> noFlow(InstrCallImpl)
        BcOpcode.CallPos -> noFlow(InstrCallImpl)
        BcOpcode.CallFrozenDef -> noFlow(InstrCallFrozenDefImpl)
        BcOpcode.CallFrozenDefPos -> noFlow(InstrCallFrozenDefImpl)
        BcOpcode.CallFrozenNative -> noFlow(InstrCallFrozenGenericImpl)
        BcOpcode.CallFrozenNativePos -> noFlow(InstrCallFrozenGenericImpl)
        BcOpcode.CallFrozen -> noFlow(InstrCallFrozenGenericImpl)
        BcOpcode.CallFrozenPos -> noFlow(InstrCallFrozenGenericImpl)
        BcOpcode.CallMethod -> noFlow(InstrCallMethodImpl)
        BcOpcode.CallMethodPos -> noFlow(InstrCallMethodImpl)
        BcOpcode.CallMaybeKnownMethod -> noFlow(InstrCallMaybeKnownMethodImpl)
        BcOpcode.CallMaybeKnownMethodPos -> noFlow(InstrCallMaybeKnownMethodImpl)
        BcOpcode.PossibleGc -> noFlow(InstrPossibleGcImpl)

        // --- Flow control instructions ---
        BcOpcode.ComprListAppend -> InstrComprListAppend.run(eval, frame, ip, arg as Pair<BcSlotIn, BcSlotIn>)
        BcOpcode.ComprDictInsert -> InstrComprDictInsert.run(eval, frame, ip, arg as Triple<BcSlotIn, BcSlotIn, BcSlotIn>)
        BcOpcode.Br -> InstrBr.run(eval, frame, ip, arg as BcAddrOffset)
        BcOpcode.IfBr -> InstrIfBr.run(eval, frame, ip, arg as Pair<BcSlotIn, BcAddrOffset>)
        BcOpcode.IfNotBr -> InstrIfNotBr.run(eval, frame, ip, arg as Pair<BcSlotIn, BcAddrOffset>)
        BcOpcode.Iter -> InstrIter.run(eval, frame, ip, arg as InstrIterArg)
        BcOpcode.Continue -> InstrContinue.run(eval, frame, ip, arg as InstrContinueArg)
        BcOpcode.Break -> InstrBreak.run(eval, frame, ip, arg as Pair<BcSlotIn, BcAddrOffset>)
        BcOpcode.IterStop -> InstrIterStop.run(eval, frame, ip, arg as BcSlotIn)
        BcOpcode.Return -> InstrReturn.run(eval, frame, ip, arg as BcSlotIn)
        BcOpcode.ReturnConst -> InstrReturnConst.run(eval, frame, ip, arg as io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue)
        BcOpcode.ReturnCheckType -> InstrReturnCheckType.run(eval, frame, ip, arg as BcSlotIn)

        // --- End pseudo-instruction ---
        BcOpcode.End -> InstrEnd.run(eval, frame, ip, arg as BcInstrEndArg)
    }
}

/** Execute the code block, either a module or a function body. */
// pub(crate) fn run_block<'v, EC: EvaluationCallbacks>(eval: &mut Evaluator, ec: &mut EC, mut ip: BcPtrAddr) -> Result<Value, EvalException>
fun runBlock(
    eval: Evaluator,
    ec: EvaluationCallbacks,
    startIp: BcPtrAddr,
    bcInstrs: BcInstrs,
): Result<Value> {
    // Copy frame pointer to local variable to generate more efficient code.
    val frame = eval.currentFrame
    eval.currentBcInstrs = bcInstrs

    var ip = startIp
    while (true) {
        when (val control = step(eval, ec, frame, ip, bcInstrs)) {
            is InstrControl.Next -> ip = control.ip
            is InstrControl.Return -> return Result.success(control.value)
            is InstrControl.Err -> {
                val evalException = Bc.wrapErrorForInstrPtr(ip, control.error, eval, bcInstrs)
                return Result.failure(evalException)
            }
        }
    }
}
