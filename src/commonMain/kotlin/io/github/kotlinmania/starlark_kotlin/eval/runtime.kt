// port-lint: source src/eval/runtime.rs
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
 * Runtime evaluation support.
 *
 * Submodules:
 * - [arguments][io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments] - argument handling
 * - [beforeStmt][io.github.kotlinmania.starlark_kotlin.eval.runtime.BeforeStmt] - pre-statement hooks
 * - [cheapCallStack][io.github.kotlinmania.starlark_kotlin.eval.runtime.CheapCallStack] - lightweight call stack
 * - [evaluator][io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator] - main evaluator
 * - [fileLoader][io.github.kotlinmania.starlark_kotlin.eval.runtime.FileLoader] - file loading
 * - [frameSpan][io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan] - frame span tracking
 * - [frozenFileSpan][io.github.kotlinmania.starlark_kotlin.eval.runtime.FrozenFileSpan] - frozen file spans
 * - [inlinedFrame][io.github.kotlinmania.starlark_kotlin.eval.runtime.InlinedFrame] - inlined frame support
 * - [params][io.github.kotlinmania.starlark_kotlin.eval.runtime.Params] - parameter handling
 * - [profile][io.github.kotlinmania.starlark_kotlin.eval.runtime.Profile] - profiling support
 * - [rustLoc][io.github.kotlinmania.starlark_kotlin.eval.runtime.RustLoc] - source location
 * - [slots][io.github.kotlinmania.starlark_kotlin.eval.runtime.Slots] - variable slots
 * - [smallDuration][io.github.kotlinmania.starlark_kotlin.eval.runtime.SmallDuration] - small duration type
 * - [visitSpan][io.github.kotlinmania.starlark_kotlin.eval.runtime.VisitSpan] - span visitor
 */
