// port-lint: source src/eval.rs
package io.github.kotlinmania.starlark_kotlin.eval

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

//! Evaluate some code, typically done by creating an [Evaluator], then calling
//! [evalModule].

// pub(crate) mod bc;
// pub(crate) mod compiler;
// mod params;
// pub(crate) mod runtime;
// pub(crate) mod soft_error;

import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.eval.compiler.Compiler
import io.github.kotlinmania.starlark_kotlin.eval.compiler.def.DefInfo
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.scope_resolver_globals.ScopeResolverGlobals
import io.github.kotlinmania.starlark_kotlin.eval.runtime.DEFAULT_STACK_SIZE
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import kotlin.time.TimeSource
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.Arguments
import io.github.kotlinmania.starlark_kotlin.stdlib.Symbol
import io.github.kotlinmania.starlark_kotlin.stdlib.ArgumentsFull
import io.github.kotlinmania.starlark_kotlin.stdlib.ArgNames
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ScopeId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.string.allocStr
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.DialectTypes
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.values.types.allocAny
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocAnySlice
import io.github.kotlinmania.starlark_kotlin.typing.scopeData
import io.github.kotlinmania.starlark_kotlin.typing.cst
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.enableTypes
import io.github.kotlinmania.starlark_kotlin.eval.runtime.newCheckUnique
import io.github.kotlinmania.starlark_kotlin.eval.compiler.topLevelStmtCount
import io.github.kotlinmania.starlark_kotlin.eval.compiler.moduleSlotCount
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule

// --- Re-exports (Rust `pub use`) ---
// pub use runtime::arguments::Arguments;
// pub use runtime::before_stmt::BeforeStmtFuncDyn;
// pub use runtime::evaluator::Evaluator;
// pub use runtime::file_loader::FileLoader;
// pub use runtime::file_loader::ReturnFileLoader;
// pub use runtime::params::parser::ParametersParser;
// pub use runtime::params::spec::ParametersSpec;
// pub use runtime::params::spec::ParametersSpecParam;
// pub use runtime::profile::data::ProfileData;
// pub use runtime::profile::mode::ProfileMode;
// pub use soft_error::SoftErrorHandler;
// pub use starlark_syntax::call_stack::CallStack;
// In Kotlin, these are accessible via their own packages. No re-export needed.

// --- impl Evaluator ---

/// Evaluate an [AstModule] with this [Evaluator], modifying the in-scope
/// [Module] as appropriate.
// pub fn eval_module(&mut self, ast: AstModule, globals: &Globals) -> crate::Result<Value<'v>>
fun Evaluator.evalModule(ast: AstModule, globals: Globals): Result<Value> {
    val start = TimeSource.Monotonic.markNow()

    val (codemap, statement, dialect, typecheck) = ast.intoParts()

    val codemapRef = moduleEnv.frozenHeap().allocAny(codemap)

    val globalsRef = moduleEnv.frozenHeap().allocAny(globals)

    val docstring = DocString.extractRawStarlarkDocstring(statement)
    if (docstring != null) {
        moduleEnv.setDocstring(docstring)
    }

    val moduleScopes = ModuleScopes.checkModuleErr(
        moduleEnv.mutableNames(),
        moduleEnv.frozenHeap(),
        emptyMap(),
        statement,
        ScopeResolverGlobals(globals = globalsRef),
        codemapRef,
        dialect,
    ).getOrElse { return Result.failure(it) }

    val scopeNames = moduleScopes.scopeData.getScope(ScopeId.module())
    val localNames = frozenHeap().allocAnySlice(scopeNames.used)

    moduleEnv.slots().ensureSlots(moduleScopes.moduleSlotCount)
    val oldDefInfo = moduleDefInfo
    moduleDefInfo = moduleEnv.frozenHeap().allocAny(
        DefInfo.forModule(
            codemapRef,
            localNames,
            moduleEnv.frozenHeap().allocAnySlice(scopeNames.parent),
            globalsRef,
        )
    )

    callStack.allocIfNeeded(
        maxCallstackSize ?: DEFAULT_STACK_SIZE,
    ).getOrElse { return Result.failure(it) }

    // Set up the world to allow evaluation (do NOT use getOrElse from now on)

    callStack.push(Value.newNone(), null)

    // Evaluation
    val compiler = Compiler(
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

/// Evaluate a function stored in a [Value], passing in `positional` and `named` arguments.
// pub fn eval_function(&mut self, function: Value, positional: &[Value], named: &[(&str, Value)]) -> crate::Result<Value>
fun Evaluator.evalFunction(
    function: Value,
    positional: List<Value>,
    named: List<Pair<String, Value>>,
): Result<Value> {
    val names = named.map { (s, _) -> Pair(Symbol.new(s), heap().allocStr(s)) }
    val namedValues = named.map { it.second }
    val params = Arguments(
        ArgumentsFull(
            pos = positional,
            named = namedValues,
            names = ArgNames.newCheckUnique(names).getOrElse { return Result.failure(it) },
            args = null,
            kwargs = null,
        )
    )
    callStack.allocIfNeeded(
        maxCallstackSize ?: DEFAULT_STACK_SIZE,
    ).getOrElse { return Result.failure(it) }

    // eval_module pushes an "empty" call stack frame. other places expect that first frame
    // to be ignorable, and so we push an empty frame too (otherwise things would ignore
    // this function's own frame).
    val res = withCallStack(Value.newNone(), null) { eval ->
        function.invoke(params, eval)
    }

    runInfrequentInstrChecks().getOrElse { return Result.failure(it) }

    return res
}
