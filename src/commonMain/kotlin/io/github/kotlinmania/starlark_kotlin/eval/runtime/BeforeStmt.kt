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

import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.FileSpanRef
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator

/**
 * Used by DAP; this is not public API.
 *
 * A dynamic callback interface invoked before each statement during evaluation.
 */
interface BeforeStmtFuncDyn {
    /**
     * Invoked before a statement is evaluated.
     *
     * Used by DAP; this is not public API.
     *
     * @param span the source span of the statement about to execute
     * @param continued whether this is a continuation of a previous statement
     * @param eval the current evaluator context
     */
    fun call(
        span: FileSpanRef,
        continued: Boolean,
        eval: Evaluator,
    )
}

/**
 * A before-statement callback, either a plain function or a dynamic trait object.
 *
 * Used by DAP; this is not public API.
 */
sealed class BeforeStmtFunc {
    /** A plain function callback. */
    class Fn(val f: (FileSpanRef, Boolean, Evaluator) -> Unit) : BeforeStmtFunc()

    /** A dynamic (trait-object) callback. */
    class Dyn(val d: BeforeStmtFuncDyn) : BeforeStmtFunc()

    /**
     * Invokes the callback with the given span, continuation flag, and evaluator.
     *
     * @param span the source span of the statement about to execute
     * @param continued whether this is a continuation of a previous statement
     * @param eval the current evaluator context
     */
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
        /** Creates a [BeforeStmtFunc] from a plain function reference. */
        fun fromFn(value: (FileSpanRef, Boolean, Evaluator) -> Unit): BeforeStmtFunc {
            return Fn(value)
        }

        /** Creates a [BeforeStmtFunc] from a dynamic callback. */
        fun fromDyn(value: BeforeStmtFuncDyn): BeforeStmtFunc {
            return Dyn(value)
        }
    }
}

/**
 * Configuration of `BeforeStmt` instrumentation of bytecode.
 */
internal class BeforeStmt {
    /** Functions to run before each statement. */
    val beforeStmt: MutableList<BeforeStmtFunc> = mutableListOf()

    /**
     * Explicitly request generation of `BeforeStmt` instructions
     * even if no [beforeStmt] functions are registered.
     * This is needed when compiling dependencies of a file to be profiled.
     */
    var instrument: Boolean = false

    /** Returns `true` if instrumentation is enabled or callbacks are registered. */
    fun enabled(): Boolean {
        return instrument || beforeStmt.isNotEmpty()
    }
}
