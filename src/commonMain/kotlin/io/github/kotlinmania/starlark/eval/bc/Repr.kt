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

/** Instruction representation in memory. */

import kotlin.reflect.KClass

/**
 * In Rust, instructions are 8-byte aligned in a raw byte buffer.
 * In Kotlin, instructions are stored as 2 list elements (header + arg),
 * so the stride is 2.
 */
const val BC_INSTR_ALIGN: Int = 2

/** Instruction header. */
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

/** How instructions are stored in memory. */
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

        fun assertAlign(_instrClass: KClass<out BcInstr>) {
            // In Rust this checks mem::align_of and mem::size_of against BC_INSTR_ALIGN.
            // In Kotlin/Multiplatform there is no direct equivalent of repr(C) alignment,
            // but we preserve the assertion structure for parity.
            // assert(alignOf<BcInstrRepr<I>>() == BC_INSTR_ALIGN)
            // assert(sizeOf<BcInstrRepr<I>>() % BC_INSTR_ALIGN == 0)
        }

        fun sizeOf(_instrClass: KClass<out BcInstr>): Int {
            // In Rust this returns mem::size_of::<BcInstrRepr<I>>().
            // In Kotlin there is no direct equivalent; returns a nominal value.
            return BC_INSTR_ALIGN
        }
    }
}
