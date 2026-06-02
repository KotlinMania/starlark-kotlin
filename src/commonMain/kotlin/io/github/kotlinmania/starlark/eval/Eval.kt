// port-lint: source src/eval.rs
package io.github.kotlinmania.starlark.eval

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
 * Evaluate some code, typically done by creating an [Evaluator], then calling
 * [evalModule].
 */

import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.extractRawStarlarkDocstring
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.eval.compiler.Compiler
import io.github.kotlinmania.starlark.eval.compiler.DefInfo
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark.eval.compiler.ScopeId
import io.github.kotlinmania.starlark.eval.compiler.evalModule
import io.github.kotlinmania.starlark.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark.eval.runtime.ArgNames
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.eval.runtime.DEFAULT_STACK_SIZE
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.DialectTypes
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocAnySlice
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.allocAny
import kotlin.time.TimeSource

/**
 * Evaluate an [AstModule] with this [Evaluator], modifying the in-scope
 * [Module] as appropriate.
 */
internal fun Evaluator.evalModule(ast: AstModule, globals: Globals): Result<Value> {
    val start = TimeSource.Monotonic.markNow()

    val (codemap, statement, dialect, typecheck) = ast.intoParts()

    val codemapRef = moduleEnv.frozenHeap().allocAny(codemap)

    val globalsRef = moduleEnv.frozenHeap().allocAny(globals)

    val docstring = DocString.extractRawStarlarkDocstring(statement)
    if (docstring != null) {
        moduleEnv.setDocstring(docstring)
    }

    val moduleScopes =
        runCatching {
            ModuleScopes.checkModuleErr(
                moduleEnv.mutableNames(),
                moduleEnv.frozenHeap(),
                emptyMap(),
                statement,
                ScopeResolverGlobals(globals = globalsRef),
                codemapRef,
                dialect,
            )
        }.getOrElse { return Result.failure(it) }

    val scopeNames = moduleScopes.scopeData.getScope(ScopeId.module())
    val localNames = frozenHeap().allocAnySlice(scopeNames.used)

    moduleEnv.slots().ensureSlots(moduleScopes.moduleSlotCount)
    val oldDefInfo = moduleDefInfo
    moduleDefInfo =
        moduleEnv
            .frozenHeap()
            .allocAny(
                DefInfo.forModule(
                    codemapRef,
                    localNames.deref(),
                    moduleEnv.frozenHeap().allocAnySlice(scopeNames.parent).deref(),
                    globalsRef,
                ),
            ).deref()

    runCatching {
        callStack.allocIfNeeded(
            maxCallstackSize ?: DEFAULT_STACK_SIZE,
        )
    }.getOrElse { return Result.failure(it) }

    // Set up the world to allow evaluation (do NOT use getOrElse from now on)

    callStack.push(Value.newNone(), null)

    // Evaluation
    val compiler =
        Compiler(
            scopeData = moduleScopes.scopeData,
            locals = mutableListOf(),
            globals = globalsRef,
            codemap = codemapRef,
            eval = this,
            checkTypes = dialect.enableTypes == DialectTypes.Enable,
            topLevelStmtCount = moduleScopes.topLevelStmtCount,
            typecheck = typecheck,
        )

    val res = compiler.evalModule(moduleScopes.cst, localNames)

    // Clean up the world, putting everything back
    callStack.pop()

    moduleDefInfo = oldDefInfo

    moduleEnv.addEvalDuration(start.elapsedNow())

    runInfrequentInstrChecks().getOrElse { return Result.failure(it) }

    // Return the result of evaluation
    return res.mapCatching { it }
}

/** Evaluate a function stored in a [Value], passing in `positional` and `named` arguments. */
internal fun Evaluator.evalFunction(
    function: Value,
    positional: List<Value>,
    named: List<Pair<String, Value>>,
): Result<Value> {
    val names = named.map { (s, _) -> Pair(Symbol.new(s), StringValue.newUnchecked(heap().allocStr(s))) }
    val namedValues = named.map { it.second }
    val params =
        Arguments(
            ArgumentsFull(
                pos = positional,
                named = namedValues,
                names = ArgNames.newCheckUnique(names).getOrElse { return Result.failure(it) },
                args = null,
                kwargs = null,
            ),
        )
    runCatching {
        callStack.allocIfNeeded(
            maxCallstackSize ?: DEFAULT_STACK_SIZE,
        )
    }.getOrElse { return Result.failure(it) }

    // evalmodule pushes an "empty" call stack frame. other places expect that first frame
    // to be ignorable, and so we push an empty frame too (otherwise things would ignore
    // this function's own frame).
    val res =
        withCallStack(Value.newNone(), null) { eval ->
            function.invoke(params, eval)
        }

    runInfrequentInstrChecks().getOrElse { return Result.failure(it) }

    return res
}
