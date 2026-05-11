<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/eval/compiler/Module.kt
// port-lint: source eval/compiler/module.rs
package io.github.kotlinmania.starlark.eval.compiler
=======
// port-lint: source src/eval/compiler/module.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/eval/compiler/Module.kt

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

/** Compile and evaluate module top-level statements. */

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/eval/compiler/Module.kt
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlark.eval.bc.compiler.asBc
import io.github.kotlinmania.starlark.eval.bc.allocaFrame
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark.eval.runtime.frozenfilespan.FrozenFileSpan
import io.github.kotlinmania.starlark.syntax.ast.AssignIdentP
import io.github.kotlinmania.starlark.syntax.ast.LoadP
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.typing.BindingsCollect
import io.github.kotlinmania.starlarksyntax.evalexception.EvalException
import io.github.kotlinmania.starlark.typing.InternalError
import io.github.kotlinmania.starlark.typing.ModuleVarTypes
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TypecheckMode
import io.github.kotlinmania.starlark.typing.TypingError
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.typing.solveBindings
import io.github.kotlinmania.starlark.values.FrozenRef
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.toValue
=======
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.asBc
import io.github.kotlinmania.starlark_kotlin.eval.bc.allocaFrame
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstAssignIdent
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.frozen_file_span.FrozenFileSpan
import io.github.kotlinmania.starlark_kotlin.syntax.ast.LoadP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.typing.BindingsCollect
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.typing.InternalError
import io.github.kotlinmania.starlark_kotlin.typing.ModuleVarTypes
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TypecheckMode
import io.github.kotlinmania.starlark_kotlin.typing.TypingError
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.typing.solveBindings
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.constFrozenString
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.toValue
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/eval/compiler/Module.kt

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
internal fun Compiler.evalLoad(load: Spanned<LoadP<CstPayload, *>>): Result<Unit> {
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
        try {
            exprThrow(runCatching { loader.load(name) }, span, eval)
        } catch (e: EvalException) {
            return Result.failure(e)
        }
    }

    for (loadArg in load.node.args) {
        @Suppress("UNCHECKED_CAST")
        val local = loadArg.local as CstAssignIdent
        val (slot, _captured) = scopeData.getAssignIdentSlot(local, codemap.deref())
        val moduleSlot = when (slot) {
            is Slot.Local -> error("symbol need to be resolved to module")
            is Slot.Module -> slot.id
        }
        val value = try {
            exprThrow(
                eval.moduleEnv.loadSymbol(loadenv, loadArg.their.node),
                FrameSpan.new(FrozenFileSpan.new(codemap, loadArg.span())),
                eval,
            )
        } catch (e: EvalException) {
            return Result.failure(e)
        }
        eval.setSlotModule(moduleSlot, value)
    }

    return Result.success(Unit)
}

/**
 * Compile and evaluate regular statement.
 * Regular statement is a statement which is not `load` or a sequence of statements.
 */
// fn eval_regular_top_level_stmt(&mut self, stmt: &mut CstStmt, local_names: FrozenRef<'static, [FrozenStringValue]>) -> Result<Value<'v>, EvalException>
internal fun Compiler.evalRegularTopLevelStmt(
    stmt: CstStmt,
    localNames: FrozenRef<List<FrozenStringValue>>,
): Result<Value> {
    if (stmt.node is StmtP.Statements<*> || stmt.node is StmtP.Load<*, *>) {
        return Result.failure(
            EvalException.newAnyhow(
                ModuleError.UnexpectedStatement,
                stmt.span,
                codemap.deref(),
            )
        )
    }

    val compiledStmt = moduleTopLevelStmt(stmt)
        .getOrElse { e -> return Result.failure(e) }
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
                codemap.deref(),
            )
        )
    }

    var last: Value = Value.newNone()
    for (s in stmts) {
        runCatching { populateTypesInStmt(s) }.getOrElse { return Result.failure(it) }

        when (s.node) {
            is StmtP.Load<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val loadNode = s.node as StmtP.Load<CstPayload, *>
                evalLoad(Spanned(
                    node = loadNode.loadStmt,
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
        codemap = codemap.deref(),
    )
    val moduleVarTypes = mkModuleVarTypes()
    for (top in stmts) {
        if (top.node is StmtP.Def<*, *>) {
            val bindingsCollect = runCatching {
                BindingsCollect.collectOne(
                    top,
                    TypecheckMode.Compiler,
                    codemap.deref(),
                    mutableListOf(),
                )
            }.getOrElse { e ->
                return Result.failure(
                    if (e is InternalError) e.intoEvalException() else e
                )
            }

            val (errors) = runCatching {
                solveBindings(bindingsCollect.bindings, oracle, moduleVarTypes)
            }.getOrElse { e ->
                return Result.failure(
                    if (e is InternalError) e.intoEvalException() else e
                )
            }

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
        .associate { (moduleSlotId, value) ->
            moduleSlotId to Ty.ofValue(value)
        }
        .toMutableMap()
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
