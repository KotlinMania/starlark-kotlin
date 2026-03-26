// port-lint: source src/eval/bc/instr.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.instr

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

/** Define the bytecode instruction. */

import io.github.kotlinmania.starlark_kotlin.eval.bc.BcInstrArg
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcPtrAddr
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.BcFramePtr
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.typing.error.StarlarkError
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

/**
 * Result of instruction evaluation.
 *
 * This is more efficient than `Result<R, Exception>`,
 * see the Rust version for details on compiler optimisation.
 */
// #[must_use]
// pub(crate) enum InstrControl<'v, 'b>
internal sealed class InstrControl {
    /** Go to address. */
    // Next(BcPtrAddr<'b>)
    data class Next(val ip: BcPtrAddr) : InstrControl()

    /** Return from the function. */
    // Return(Value<'v>)
    data class Return(val value: Value) : InstrControl()

    /**
     * Error. This can be either any error or diagnostics.
     * If it is the former, error span will be added from instruction metadata.
     */
    // Err(crate::Error)
    data class Err(val error: StarlarkError) : InstrControl()
}

/**
 * Bytecode instruction interface.
 *
 * Each instruction type implements this interface, defining its argument type
 * and its execution behavior.
 */
// pub(crate) trait BcInstr: Sized + 'static
internal interface BcInstr {
    /**
     * Fixed instruction argument (which may encode additional arguments
     * pushed or popped from the stack by the instruction implementation).
     */
    // type Arg: BcInstrArg;
    val argType: BcInstrArg

    /**
     * Execute the instruction.
     */
    // fn run<'v, 'b>(
    //     eval: &mut Evaluator<'v, '_, '_>,
    //     frame: BcFramePtr<'v>,
    //     ip: BcPtrAddr<'b>,
    //     arg: &Self::Arg,
    // ) -> InstrControl<'v, 'b>;
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: BcInstrArg,
    ): InstrControl
}
