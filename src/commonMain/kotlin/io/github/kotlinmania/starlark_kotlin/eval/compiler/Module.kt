// port-lint: source src/eval/compiler/module.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler

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

//! Compile and evaluate module top-level statements.

import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.bindings.BindingsCollect
import io.github.kotlinmania.starlark_kotlin.typing.InternalError
import io.github.kotlinmania.starlark_kotlin.syntax.ast.ModuleVarTypes
import io.github.kotlinmania.starlark_kotlin.typing.mode.TypecheckMode
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.CstStmt
import io.github.kotlinmania.starlark_kotlin.analysis.LoadP
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.starlark_error.Error
import io.github.kotlinmania.starlark_kotlin.analysis.Statements
import io.github.kotlinmania.starlark_kotlin.analysis.Def
import io.github.kotlinmania.starlark_kotlin.values.layout.constFrozenString
import io.github.kotlinmania.starlark_kotlin.typing.solveBindings
import io.github.kotlinmania.starlark_kotlin.typing.ofValue
import io.github.kotlinmania.starlark_kotlin.typing.ModuleVarTypes
import io.github.kotlinmania.starlark_kotlin.typing.intoEvalException
import io.github.kotlinmania.starlark_kotlin.typing.bindings.bindings
import io.github.kotlinmania.starlark_kotlin.eval.runtime.evalBc
import io.github.kotlinmania.starlark_kotlin.eval.compiler.moduleTopLevelStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.compileContext
import io.github.kotlinmania.starlark_kotlin.eval.bc.setSlotModule
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.allocaFrame
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.asBc
import io.github.kotlinmania.starlark_kotlin.assert.staticTypechecking
import io.github.kotlinmania.starlark_kotlin.assert.loader
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.their
import io.github.kotlinmania.starlark_kotlin.analysis.unused_loads.load
import io.github.kotlinmania.starlark_kotlin.analysis.module
import io.github.kotlinmania.starlark_kotlin.analysis.local
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned

// #[derive(Debug, thiserror::Error)]
// enum ModuleError
internal sealed class ModuleError(override val message: String) : Exception(message) {
    // #[error("No imports are available, you tried `{0}` (no call to `Evaluator.set_loader`)")]
    data class NoImportsAvailable(val name: String) :
        ModuleError("No imports are available, you tried `$name` (no call to `Evaluator.set_loader`)")

    // #[error("Unexpected statement (internal error)")]
    data object UnexpectedStatement :
        ModuleError("Unexpected statement (internal error)")

    // #[error("Top level stmt count mismatch (internal error)")]
    data object TopLevelStmtCountMismatch :
        ModuleError("Top level stmt count mismatch (internal error)")
}

// impl<'v> Compiler<'v, '_, '_, '_>
// Extension functions on Compiler for module evaluation.

// fn eval_load(&mut self, load: Spanned<&LoadP<CstPayload>>) -> Result<(), EvalException>
internal fun Compiler.evalLoad(load: Spanned<LoadP<CstPayload>>): Result<Unit> {
    val name = load.node.module.node

    val span = FrameSpan.new(FrozenFileSpan.new(codemap, load.span))

    val loader = eval.loader
    val loadenv = if (loader == null) {
        return Result.failure(
            addSpanToExprError(
                io.github.kotlinmania.starlark_kotlin.Error.newOther(
                    ModuleError.NoImportsAvailable(name)
                ),
                span,
                eval,
            )
        )
    } else {
        exprThrow(loader.load(name), span, eval).getOrElse { return Result.failure(it) }
    }

    for (loadArg in load.node.args) {
        val (slot, _captured) = scopeData.getAssignIdentSlot(loadArg.local, codemap)
        val moduleSlot = when (slot) {
            is Slot.Local -> error("symbol need to be resolved to module")
            is Slot.Module -> slot
        }
        val value = runCatching {
            exprThrow(
                eval.moduleEnv.loadSymbol(loadenv, loadArg.their.node),
                FrameSpan.new(FrozenFileSpan.new(codemap, loadArg.span())),
                eval,
            )
        }.getOrElse { return Result.failure(it) }
        eval.setSlotModule(moduleSlot, value)
    }

    return Result.success(Unit)
}

/// Compile and evaluate regular statement.
/// Regular statement is a statement which is not `load` or a sequence of statements.
// fn eval_regular_top_level_stmt(&mut self, stmt: &mut CstStmt, local_names: FrozenRef<'static, [FrozenStringValue]>) -> Result<Value<'v>, EvalException>
internal fun Compiler.evalRegularTopLevelStmt(
    stmt: CstStmt,
    localNames: FrozenRef<List<FrozenStringValue>>,
): Result<Value> {
    if (stmt.node is StmtP.Statements || stmt.node is StmtP.Load) {
        return Result.failure(
            EvalException.newAnyhow(
                ModuleError.UnexpectedStatement,
                stmt.span,
                codemap,
            )
        )
    }

    val compiledStmt = moduleTopLevelStmt(stmt)
        .mapCatching { it }
        .getOrElse { e -> return Result.failure((e as EvalException)) }
    val bc = compiledStmt.asBc(
        compileContext(false),
        localNames,
        0,
        eval.moduleEnv.frozenHeap(),
    )
    // We don't preserve locals between top level statements.
    // That is OK for now: the only locals used in module evaluation
    // are comprehension bindings.
    val localCount = localNames.asRef().size
    return allocaFrame(
        eval,
        localCount,
        bc.maxStackSize.toInt(),
        bc.maxLoopDepth,
    ) { evaluator -> evaluator.evalBc(constFrozenString("module").toValue(), bc) }
}

// fn eval_top_level_stmt(&mut self, stmt: &mut CstStmt, local_names: FrozenRef<'static, [FrozenStringValue]>) -> Result<Value<'v>, EvalException>
internal fun Compiler.evalTopLevelStmt(
    stmt: CstStmt,
    localNames: FrozenRef<List<FrozenStringValue>>,
): Result<Value> {
    val stmts = topLevelStmtsMut(stmt)

    if (stmts.size != topLevelStmtCount) {
        return Result.failure(
            EvalException.newAnyhow(
                ModuleError.TopLevelStmtCountMismatch,
                stmt.span,
                codemap,
            )
        )
    }

    var last: Value = Value.newNone()
    for (s in stmts) {
        runCatching { populateTypesInStmt(s) }.getOrElse { return Result.failure(it) }

        when (s.node) {
            is StmtP.Load -> {
                evalLoad(Spanned(
                    node = (s.node as StmtP.Load).loadStmt,
                    span = s.span,
                )).getOrElse { return Result.failure(it) }
                last = Value.newNone()
            }
            else -> {
                last = evalRegularTopLevelStmt(s, localNames)
                    .getOrElse { return Result.failure(it) }
            }
        }
    }

    typecheck(stmts).getOrElse { return Result.failure(it) }

    return Result.success(last)
}

// fn typecheck(&mut self, stmts: &mut [&mut CstStmt]) -> Result<(), EvalException>
internal fun Compiler.typecheck(stmts: List<CstStmt>): Result<Unit> {
    val doTypecheck = eval.staticTypechecking || this.typecheck
    if (!doTypecheck) {
        return Result.success(Unit)
    }

    val oracle = TypingOracleCtx(
        codemap = codemap,
    )
    val moduleVarTypes = mkModuleVarTypes()
    for (top in stmts) {
        if (top.node is StmtP.Def) {
            val bindingsCollect = runCatching {
                BindingsCollect.collectOne(
                    top,
                    TypecheckMode.Compiler,
                    codemap,
                    mutableListOf(),
                )
            }.getOrElse { e -> return Result.failure((e as InternalError).intoEvalException()) }

            val (errors) = runCatching {
                solveBindings(bindingsCollect.bindings, oracle, moduleVarTypes)
            }.getOrElse { e -> return Result.failure((e as InternalError).intoEvalException()) }

            val firstError = errors.firstOrNull()
            if (firstError != null) {
                return Result.failure(firstError.intoEvalException())
            }
        }
    }

    return Result.success(Unit)
}

// fn mk_module_var_types(&self) -> ModuleVarTypes
internal fun Compiler.mkModuleVarTypes(): ModuleVarTypes {
    val types = eval.moduleEnv.valuesBySlotId()
        .map { (moduleSlotId, value) -> Pair(moduleSlotId, Ty.ofValue(value)) }
        .toMap()
    return ModuleVarTypes(types = types)
}

// pub(crate) fn eval_module(&mut self, mut stmt: CstStmt, local_names: FrozenRef<'static, [FrozenStringValue]>) -> Result<Value<'v>, EvalException>
internal fun Compiler.evalModule(
    stmt: CstStmt,
    localNames: FrozenRef<List<FrozenStringValue>>,
): Result<Value> {
    enterScope(ScopeId.module())
    val value = evalTopLevelStmt(stmt, localNames)
        .getOrElse { e ->
            exitScope()
            return Result.failure(e)
        }
    exitScope()
    check(locals.isEmpty())
    return Result.success(value)
}
