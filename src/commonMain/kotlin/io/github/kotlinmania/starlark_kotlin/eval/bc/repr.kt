// port-lint: source src/eval/bc/repr.rs
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

/// Instruction representation in memory.

import kotlin.reflect.KClass

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
interface BcInstr {
    /** The argument type for this instruction. */
    val arg: Any
}

enum class BcOpcode {
    ;

    companion object {
        /** Get the opcode for a given instruction type. */
        fun forInstr(instrClass: KClass<out BcInstr>): BcOpcode {
            // Dispatch based on instruction class
            throw IllegalArgumentException("Unknown instruction: ${instrClass.simpleName}")
        }
    }

    /** Size of instruction representation. */
    fun sizeOfRepr(): Int {
        return dispatch(object : BcOpcodeHandler<Int> {
            override fun <I : BcInstr> handle(instrClass: KClass<I>): Int {
                BcInstrRepr.assertAlign(instrClass)
                return BcInstrRepr.sizeOf(instrClass)
            }
        })
    }

    fun <R> dispatch(handler: BcOpcodeHandler<R>): R {
        throw NotImplementedError("dispatch not yet wired")
    }
}

interface BcOpcodeHandler<R> {
    fun <I : BcInstr> handle(instrClass: KClass<I>): R
}

/// Instructions are aligned to store `u64` even on 32-bit machines.
internal const val BC_INSTR_ALIGN: Int = 8

/// Instruction header.
class BcInstrHeader(
    internal val opcode: BcOpcode,
) {
    companion object {
        fun forInstr(instrClass: KClass<out BcInstr>): BcInstrHeader {
            return BcInstrHeader(
                opcode = BcOpcode.forInstr(instrClass),
            )
        }

        fun forOpcode(opcode: BcOpcode): BcInstrHeader {
            return BcInstrHeader(opcode)
        }
    }
}

/// How instructions are stored in memory.
class BcInstrRepr<I : BcInstr>(
    internal val header: BcInstrHeader,
    internal val arg: Any,
) {
    companion object {
        fun new(instrClass: KClass<out BcInstr>, arg: Any): BcInstrRepr<out BcInstr> {
            assertAlign(instrClass)
            return BcInstrRepr<BcInstr>(
                header = BcInstrHeader.forInstr(instrClass),
                arg = arg,
            )
        }

        fun assertAlign(instrClass: KClass<out BcInstr>) {
            // In Rust this checks mem::align_of and mem::size_of against BC_INSTR_ALIGN.
            // In Kotlin/Multiplatform there is no direct equivalent of repr(C) alignment,
            // but we preserve the assertion structure for parity.
            // assert(alignOf<BcInstrRepr<I>>() == BC_INSTR_ALIGN)
            // assert(sizeOf<BcInstrRepr<I>>() % BC_INSTR_ALIGN == 0)
        }

        fun sizeOf(instrClass: KClass<out BcInstr>): Int {
            // In Rust this returns mem::size_of::<BcInstrRepr<I>>().
            // In Kotlin there is no direct equivalent; returns a nominal value.
            return BC_INSTR_ALIGN
        }
    }
}
