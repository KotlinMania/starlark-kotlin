// port-lint: source src/eval/runtime/before_stmt.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.before_stmt

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

import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.FileSpanRef

/// This is used by DAP, and it is not public API.
// pub trait BeforeStmtFuncDyn<'e>
interface BeforeStmtFuncDyn {
    /// This is used by DAP, and it is not public API.
    // fn call(&mut self, span, continued, eval) -> crate::Result<()>
    fun call(
        span: FileSpanRef,
        continued: Boolean,
        eval: Evaluator,
    )
}

/// This is used by DAP, and it is not public API.
// pub enum BeforeStmtFunc<'a, 'e: 'a>
// Kotlin: sealed class without lifetime parameters.
sealed class BeforeStmtFunc {
    // Fn(&'a dyn Fn(FileSpanRef, bool, &mut Evaluator<'_, '_, 'e>))
    class Fn(val f: (FileSpanRef, Boolean, Evaluator) -> Unit) : BeforeStmtFunc()
    // Dyn(Box<dyn BeforeStmtFuncDyn<'e>>)
    class Dyn(val d: BeforeStmtFuncDyn) : BeforeStmtFunc()

    // pub(crate) fn call(&mut self, span, continued, eval) -> crate::Result<()>
    fun call(
        span: FileSpanRef,
        continued: Boolean,
        eval: Evaluator,
    ) {
        when (this) {
            is Fn -> f(span, continued, eval)
            is Dyn -> d.call(span, continued, eval)
        }
    }

    companion object {
        // pub(crate) fn from_fn(value) -> Self
        fun fromFn(value: (FileSpanRef, Boolean, Evaluator) -> Unit): BeforeStmtFunc {
            return Fn(value)
        }

        // pub fn from_dyn(value) -> Self
        fun fromDyn(value: BeforeStmtFuncDyn): BeforeStmtFunc {
            return Dyn(value)
        }
    }
}

/// Configuration of `BeforeStmt` instrumentation of bytecode.
// #[derive(Default)]
// pub(crate) struct BeforeStmt<'a, 'e: 'a>
// Kotlin: no lifetime parameters.
internal class BeforeStmt {
    /// Functions to run before each statement.
    // pub(crate) before_stmt: Vec<BeforeStmtFunc<'a, 'e>>
    val beforeStmt: MutableList<BeforeStmtFunc> = mutableListOf()

    /// Explicitly request generation of `BeforeStmt` instructions
    /// even if no `before_stmt` functions are registered.
    /// This is needed when compiling dependencies of a file to be profiled.
    // pub(crate) instrument: bool
    var instrument: Boolean = false

    // pub(crate) fn enabled(&self) -> bool
    fun enabled(): Boolean {
        return instrument || beforeStmt.isNotEmpty()
    }
}
