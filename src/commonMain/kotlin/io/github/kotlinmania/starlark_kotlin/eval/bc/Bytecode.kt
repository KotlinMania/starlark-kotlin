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

/// Unsorted/core interpreter stuff.

import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.BcFramePtr
import io.github.kotlinmania.starlark_kotlin.eval.bc.instr.BcInstr
import io.github.kotlinmania.starlark_kotlin.eval.bc.instr.InstrControl
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.EvaluationCallbacks
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/// Ready to execute bytecode.
// #[derive(Default)]
// pub(crate) struct Bc
class Bc(
    val instrs: BcInstrs = BcInstrs.default(),
    /// Number of local variable slots.
    val localCount: UInt = 0u,
    /// Max stack size in values (`Value`).
    val maxStackSize: UInt = 0u,
    /// Max depth of loops.
    val maxLoopDepth: LoopDepth = LoopDepth(),
) {
    companion object {
        /// Find span for instruction.
        // pub(crate) fn slow_arg_at_ptr(addr_ptr: BcPtrAddr<'_>) -> &BcInstrSlowArg
        fun slowArgAtPtr(addrPtr: BcPtrAddr): BcInstrSlowArg {
            var ptr = addrPtr
            while (true) {
                val opcode = ptr.getOpcode()
                if (opcode == BcOpcode.End) {
                    val endOfBc = ptr.getInstr<InstrEnd>()
                    val endArg = endOfBc.arg as BcInstrEndArg
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
            e: Exception,
            eval: Evaluator,
        ): EvalException {
            val span = slowArgAtPtr(ptr).span
            return addSpanToExprError(e, span, eval)
        }
    }

    /// Run the bytecode in the current frame allocated in the evaluator.
    ///
    /// Frame must be allocated properly, otherwise it will likely result in memory corruption.
    // pub(crate) fn run<'v, EC: EvaluationCallbacks>(&self, eval: &mut Evaluator, ec: &mut EC) -> Result<Value, EvalException>
    fun run(eval: Evaluator, ec: EvaluationCallbacks): Result<Value> {
        return runBlock(eval, ec, instrs.startPtr())
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

/// Execute one instruction.
// fn step<'v, 'b, EC: EvaluationCallbacks>(eval: &mut Evaluator, ec: &mut EC, frame: BcFramePtr, ip: BcPtrAddr) -> InstrControl
private fun step(
    eval: Evaluator,
    ec: EvaluationCallbacks,
    frame: BcFramePtr,
    ip: BcPtrAddr,
): InstrControl {
    val opcode = ip.getOpcode()

    val handler = object : BcOpcodeHandler<InstrControl> {
        override fun <I : BcInstr> handle(): InstrControl {
            val repr = ip.getInstr<I>()
            return repr.arg.run(eval, frame, ip)
        }
    }

    runCatching { ec.beforeInstr(eval, ip, opcode) }.getOrElse {
        return InstrControl.Err(it)
    }
    return opcode.dispatch(handler)
}

/// Execute the code block, either a module or a function body.
// pub(crate) fn run_block<'v, EC: EvaluationCallbacks>(eval: &mut Evaluator, ec: &mut EC, mut ip: BcPtrAddr) -> Result<Value, EvalException>
fun runBlock(
    eval: Evaluator,
    ec: EvaluationCallbacks,
    startIp: BcPtrAddr,
): Result<Value> {
    // Copy frame pointer to local variable to generate more efficient code.
    val frame = eval.currentFrame

    var ip = startIp
    while (true) {
        when (val control = step(eval, ec, frame, ip)) {
            is InstrControl.Next -> ip = control.ip
            is InstrControl.Return -> return Result.success(control.value)
            is InstrControl.Err -> {
                val evalException = Bc.wrapErrorForInstrPtr(ip, control.error, eval)
                return Result.failure(evalException)
            }
        }
    }
}
