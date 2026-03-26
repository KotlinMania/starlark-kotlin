// port-lint: source src/eval/bc/repr.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.repr

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

import io.github.kotlinmania.starlark_kotlin.eval.bc.instr.BcInstr
import io.github.kotlinmania.starlark_kotlin.eval.bc.opcode.BcOpcode

/**
 * Instructions are aligned to store `u64` even on 32-bit machines.
 *
 * In Kotlin, we don't need manual memory alignment since the JVM handles it.
 * This constant is kept for documentation parity.
 */
// pub(crate) const BC_INSTR_ALIGN: usize = 8;
internal const val BC_INSTR_ALIGN: Int = 8

/**
 * Instruction header.
 *
 * In Rust, this is `#[repr(C)]` for C-compatible layout.
 * In Kotlin, the opcode is stored directly.
 */
// #[derive(Clone, Copy)]
// #[repr(C)]
// pub(crate) struct BcInstrHeader
internal data class BcInstrHeader(
    val opcode: BcOpcode,
) {
    // impl BcInstrHeader

    companion object {
        // fn for_instr<I: BcInstr>() -> BcInstrHeader
        fun <I : BcInstr> forInstr(): BcInstrHeader {
            return BcInstrHeader(opcode = BcOpcode.forInstr<I>())
        }

        // pub(crate) const fn for_opcode(opcode: BcOpcode) -> Self
        fun forOpcode(opcode: BcOpcode): BcInstrHeader {
            return BcInstrHeader(opcode = opcode)
        }
    }
}

/**
 * How instructions are stored in memory.
 *
 * In Rust, this is `#[repr(C, align(8))]` with a header, arg, and alignment padding.
 * In Kotlin, we store the header and arg as a simple container since the JVM
 * manages memory layout.
 */
// #[repr(C, align(8))]
// pub(crate) struct BcInstrRepr<I: BcInstr>
internal class BcInstrRepr<I : BcInstr>(
    val header: BcInstrHeader,
    val arg: Any,
) {
    // impl BcInstrRepr

    companion object {
        // pub(crate) fn new(arg: I::Arg) -> BcInstrRepr<I>
        inline fun <reified I : BcInstr> new(arg: Any): BcInstrRepr<I> {
            return BcInstrRepr<I>(
                header = BcInstrHeader.forInstr<I>(),
                arg = arg,
            )
        }
    }
}
