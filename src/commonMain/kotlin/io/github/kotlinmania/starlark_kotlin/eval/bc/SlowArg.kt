// port-lint: source src/eval/bc/slow_arg.rs
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

import io.github.kotlinmania.starlark_kotlin.eval.bc.addr.BcAddr
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue

/**
 * Slow instruction arg: stored in the end of bytecode,
 * expensive to access. Used to implement errors.
 */
data class BcInstrSlowArg(
    /** Instruction code span. */
    val span: FrameSpan = FrameSpan.DEFAULT,
    /** Spans when an instruction needs multiple spans. */
    val spans: MutableList<FrameSpan> = mutableListOf(),
)

data class BcInstrEndArg(
    /** Offset of end instruction. */
    val endAddr: BcAddr = BcAddr(0u),
    /** Spans of all instructions. */
    val slowArgs: MutableList<Pair<BcAddr, BcInstrSlowArg>> = mutableListOf(),
    /** Frame local names. */
    val localNames: FrozenRef<List<FrozenStringValue>> = FrozenRef.empty(),
)
