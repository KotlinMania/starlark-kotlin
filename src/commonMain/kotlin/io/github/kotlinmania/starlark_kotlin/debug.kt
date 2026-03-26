// port-lint: source src/debug.rs
package io.github.kotlinmania.starlark_kotlin

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

/** Provides debug-related functionality and utilities. */

import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

// mod adapter;
// mod evaluate;
// mod inspect;

// pub use adapter::*;

typealias DapAdapterClient = io.github.kotlinmania.starlark_kotlin.debug.DapAdapterClient
typealias ScopesInfo = io.github.kotlinmania.starlark_kotlin.debug.ScopesInfo
typealias Variable = io.github.kotlinmania.starlark_kotlin.debug.Variable
typealias Scope = io.github.kotlinmania.starlark_kotlin.debug.Scope
typealias VariablePath = io.github.kotlinmania.starlark_kotlin.debug.VariablePath
typealias PathSegment = io.github.kotlinmania.starlark_kotlin.debug.PathSegment
typealias StepKind = io.github.kotlinmania.starlark_kotlin.debug.StepKind
typealias VariablesInfo = io.github.kotlinmania.starlark_kotlin.debug.VariablesInfo
typealias InspectVariableInfo = io.github.kotlinmania.starlark_kotlin.debug.InspectVariableInfo
typealias EvaluateExprInfo = io.github.kotlinmania.starlark_kotlin.debug.EvaluateExprInfo
typealias DapVariable = io.github.kotlinmania.starlark_kotlin.debug.DapVariable
typealias StackFrame = io.github.kotlinmania.starlark_kotlin.debug.StackFrame
typealias StackTraceArguments = io.github.kotlinmania.starlark_kotlin.debug.StackTraceArguments
typealias StackTraceResponseBody = io.github.kotlinmania.starlark_kotlin.debug.StackTraceResponseBody
typealias SetBreakpointsArguments = io.github.kotlinmania.starlark_kotlin.debug.SetBreakpointsArguments
typealias SourceBreakpoint = io.github.kotlinmania.starlark_kotlin.debug.SourceBreakpoint
typealias SetBreakpointsResponseBody = io.github.kotlinmania.starlark_kotlin.debug.SetBreakpointsResponseBody
typealias DapBreakpoint = io.github.kotlinmania.starlark_kotlin.debug.DapBreakpoint
typealias Capabilities = io.github.kotlinmania.starlark_kotlin.debug.Capabilities
typealias ResolvedBreakpoints = io.github.kotlinmania.starlark_kotlin.debug.ResolvedBreakpoints
typealias DapAdapter = io.github.kotlinmania.starlark_kotlin.debug.DapAdapter
typealias DapAdapterEvalHook = io.github.kotlinmania.starlark_kotlin.debug.DapAdapterEvalHook

fun resolveBreakpoints(
    args: SetBreakpointsArguments,
    ast: AstModule,
): Result<ResolvedBreakpoints> = io.github.kotlinmania.starlark_kotlin.debug.resolveBreakpoints(args, ast)

fun dapCapabilities(): Capabilities = io.github.kotlinmania.starlark_kotlin.debug.dapCapabilities()

fun prepareDapAdapter(
    client: DapAdapterClient,
): Pair<DapAdapter, DapAdapterEvalHook> = io.github.kotlinmania.starlark_kotlin.debug.prepareDapAdapter(client)
