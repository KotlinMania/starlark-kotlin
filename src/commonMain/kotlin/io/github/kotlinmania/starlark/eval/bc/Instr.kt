// port-lint: source src/eval/bc/instr.rs
package io.github.kotlinmania.starlark.eval.bc

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

import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.typing.StarlarkError
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Result of instruction evaluation.
 *
 * This is more efficient than `Result<R, Exception>`,
 * see the Rust version for details on compiler optimisation.
 */
sealed class InstrControl {
    /** Go to address. */
    // Next(BcPtrAddr<'b>)
    data class Next(
        val ip: BcPtrAddr,
    ) : InstrControl()

    /** Return from the function. */
    // Return(Value<'v>)
    data class Return(
        val value: Value,
    ) : InstrControl()

    /**
     * Error. This can be either any error or diagnostics.
     * If it is the former, error span will be added from instruction metadata.
     */
    // Err(crate::Error)
    data class Err(
        val error: StarlarkError,
    ) : InstrControl()
}

/**
 * Bytecode instruction interface.
 *
 * Each instruction type implements this interface, defining its argument type
 * and its execution behavior.
 */
// pub(crate) trait BcInstr: Sized + 'static
interface BcInstr {
    /**
     * Execute the instruction.
     *
     * Rust's associated type `type Arg: BcInstrArg` has no runtime representation;
     * in Kotlin the concrete arg is passed as [Any] and cast by each implementation.
     */
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): InstrControl
}
