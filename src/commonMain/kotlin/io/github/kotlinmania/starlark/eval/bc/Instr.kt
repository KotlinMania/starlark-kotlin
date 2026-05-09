// port-lint: source eval/bc/instr.rs
package io.github.kotlinmania.starlark.eval.bc

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

/** Define the bytecode instruction. */

import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.typing.StarlarkError
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Result of instruction evaluation.
 *
 * This is more efficient than wrapping every result in an exception-carrying [Result].
 */
internal sealed class InstrControl {
    /** Go to address. */
    data class Next(val ip: BcPtrAddr) : InstrControl()

    /** Return from the function. */
    data class Return(val value: Value) : InstrControl()

    /**
     * Error. This can be either any error or diagnostics.
     * If it is the former, error span will be added from instruction metadata.
     */
    data class Err(val error: StarlarkError) : InstrControl()
}

/**
 * Bytecode instruction interface.
 *
 * Each instruction type implements this interface with its concrete fixed argument type [A]
 * (which may encode additional arguments pushed or popped from the stack by the
 * instruction implementation).
 */
internal interface BcInstr<A> {
    /** Execute the instruction. */
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: A,
    ): InstrControl
}
