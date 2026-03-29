// port-lint: source src/eval/bc.rs
package io.github.kotlinmania.starlark_kotlin.eval

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

/**
 * Bytecode interpreter.
 *
 * Submodules (Kotlin packages under eval.bc):
 * - pub(crate) mod addr              -> eval.bc.addr
 * - pub(crate) mod bytecode          -> eval.bc.bytecode
 * - pub(crate) mod call              -> eval.bc.call
 * - pub(crate) mod compiler          -> eval.bc.compiler
 * - pub(crate) mod definitely_assigned -> eval.bc.definitely_assigned
 * - pub(crate) mod for_loop          -> eval.bc.for_loop
 * - pub(crate) mod frame             -> eval.bc.frame
 * - pub(crate) mod if_debug          -> eval.bc.if_debug
 * - pub(crate) mod instr             -> eval.bc.instr
 * - pub(crate) mod instr_arg         -> eval.bc.instr_arg
 * - pub(crate) mod instr_impl        -> eval.bc.instr_impl
 * - pub(crate) mod instrs            -> eval.bc.instrs
 * - pub(crate) mod native_function   -> eval.bc.native_function
 * - pub(crate) mod opcode            -> eval.bc.opcode
 * - pub(crate) mod repr              -> eval.bc.repr
 * - pub(crate) mod slow_arg          -> eval.bc.slow_arg
 * - pub(crate) mod stack_ptr         -> eval.bc.stack_ptr
 * - pub(crate) mod writer            -> eval.bc.writer
 */
