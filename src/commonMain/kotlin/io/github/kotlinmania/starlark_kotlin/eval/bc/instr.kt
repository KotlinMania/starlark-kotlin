// port-lint: source src/eval/bc/instr.rs
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

/// Define the bytecode instruction.

import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.bc.addr.BcPtrAddr
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.BcFramePtr
import io.github.kotlinmania.starlark_kotlin.eval.bc.instr_arg.BcInstrArg
import io.github.kotlinmania.starlark_kotlin.values.Value

/// Result of instruction evaluation.
///
/// This is more efficient than `Result<R, Exception>`.
internal sealed class InstrControl {
    /// Go to address.
    class Next(val ip: BcPtrAddr) : InstrControl()
    /// Return from the function.
    class Return(val value: Value) : InstrControl()
    /// Error. This can be either any error or diagnostics.
    /// If it is the former, error span will be added from instruction metadata.
    class Err(val error: Exception) : InstrControl()
}

/// Bytecode instruction interface.
///
/// Each instruction type implements this interface, defining its argument type
/// and its execution behavior.
internal interface BcInstr {
    /// The type token for the fixed instruction argument (which may encode additional
    /// arguments pushed or popped from the stack by the instruction implementation).
    val argType: Class<out BcInstrArg>
        get() = BcInstrArg::class.java

    /// Execute the instruction.
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: BcInstrArg,
    ): InstrControl
}
