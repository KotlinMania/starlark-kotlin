<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/typing/Typecheck.kt
// port-lint: source typing/typecheck.rs
package io.github.kotlinmania.starlark.typing
=======
// port-lint: source src/typing/typecheck.rs
package io.github.kotlinmania.starlark_kotlin.typing
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/typing/Typecheck.kt

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

<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/typing/Typecheck.kt
import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.FileSpan as FileSpan
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.environment.MutableNames
import io.github.kotlinmania.starlark.eval.compiler.BindingId
import io.github.kotlinmania.starlark.eval.compiler.BindingSource
import io.github.kotlinmania.starlark.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark.eval.compiler.scope.CstPayload
import io.github.kotlinmania.starlark.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark.eval.compiler.topLevelStmtsMut
import io.github.kotlinmania.starlark.syntax.AstModule
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.syntax.ast.StmtP
import io.github.kotlinmania.starlark.syntax.ast.Visibility
import io.github.kotlinmania.starlark.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.FrozenRef
=======
import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.FileSpan
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.environment.MutableNames
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingId
import io.github.kotlinmania.starlark_kotlin.eval.compiler.BindingSource
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ModuleScopes
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.CstStmt
import io.github.kotlinmania.starlark_kotlin.eval.compiler.scope.ScopeResolverGlobals
import io.github.kotlinmania.starlark_kotlin.eval.compiler.topLevelStmtsMut
import io.github.kotlinmania.starlark_kotlin.syntax.AstModule
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect
import io.github.kotlinmania.starlark_kotlin.syntax.ast.StmtP
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Visibility
import io.github.kotlinmania.starlark_kotlin.typing.oracle.TypingOracleCtx
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
import io.github.kotlinmania.starlark_kotlin.values.typing.Approximation
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/typing/Typecheck.kt

// Things which are None in the map have type void - they are never constructed
internal fun solveBindings(
    bindings: Bindings,
    oracle: TypingOracleCtx,
    moduleVarTypes: ModuleVarTypes,
): Triple<List<TypingError>, MutableMap<BindingId, Ty>, List<Approximation>> {
    val types = mutableMapOf<BindingId, Ty>()
    for (key in bindings.expressions.keys()) {
        types[key] = Ty.never()
    }
    for ((k, ty) in bindings.types) {
        types[k] = ty
    }
    // FIXME: Should be a fixed point, just do 10 iterations since that probably converges
    var changed = false
    val errors = mutableListOf<TypingError>()
    val approximations = mutableListOf<Approximation>()
    val ctx = TypingContext(
        oracle = oracle,
        errors = errors,
        approximations = approximations,
        types = types,
        moduleVarTypes = moduleVarTypes,
    )
    val iterations = 100
    for (iteration in 0 until iterations) {
        changed = false
        ctx.errors.clear()
        for ((name, exprs) in bindings.expressions.iter()) {
            for (expr in exprs) {
                val ty = ctx.expressionBindType(expr).getOrThrow()
                val t = ctx.types[name]!!
                val new = Ty.union2(t, ty)
                if (new != t) {
                    changed = true
                    ctx.types[name] = new
                }
            }
        }
        if (!changed) {
            break
        }
    }
    if (changed) {
        ctx.approximations.add(Approximation.new(
            "Fixed point didn't converge",
            iterations,
        ))
    }
    // Make sure we check every expression, looking for failures
    for (x in bindings.check) {
        ctx.expressionType(x).getOrThrow()
    }
    for ((span, e, require) in bindings.checkType) {
        val ty = if (e == null) {
            Ty.none()
        } else {
            ctx.expressionType(e).getOrThrow()
        }
        ctx.validateType(
            Spanned(
                node = ty,
                span = span,
            ),
            require,
        ).getOrThrow()
    }
    return Triple(
        ctx.errors.toList(),
        ctx.types.toMutableMap(),
        ctx.approximations.toList(),
    )
}

/** Structure containing all the inferred types. */
class TypeMap(
    private val codemap: CodeMap,
    private val bindings: LinkedHashMap<BindingId, Triple<String, Span, Ty>>,
) {
    override fun toString(): String {
        val sb = StringBuilder()
        // Iteration in unstable order - but that's fine because this is just for diagnostics
        for ((_, entry) in bindings.entries.sortedBy { it.key.id }) {
            val (name, span, ty) = entry
            sb.appendLine(
                "$name (${FileSpan(file = codemap, span = span)}) = $ty"
            )
        }
        return sb.toString()
    }

    internal fun findBindingsByName(name: String): List<Ty> {
        return bindings.entries
            .sortedBy { it.key.id }
            .filter { (_, entry) -> entry.first == name }
            .map { (_, entry) -> entry.third }
    }

    internal fun findFirstBinding(): Ty? {
        return bindings.entries
            .minByOrNull { it.key.id }
            ?.value?.third
    }
}

/** Typecheck a module. */
interface AstModuleTypecheck {
    /** Typecheck a module. */
    fun typecheck(
        globals: Globals,
        loads: Map<String, Interface>,
    ): TypecheckResult
}

data class TypecheckResult(
    val errors: List<Exception>,
    val typeMap: TypeMap,
    val `interface`: Interface,
    val approximations: List<Approximation>,
)

/** Typecheck implementation for AstModule. */
fun AstModule.typecheck(
    globals: Globals,
    loads: Map<String, Interface>,
): TypecheckResult {
    val (codemap, statement, dialect, _) = this.intoParts()
    val names = MutableNames()
    val frozenHeap = FrozenHeap()
    val (scopeErrors, moduleScopes) = ModuleScopes.checkModule(
        module = names,
        frozenHeap = frozenHeap,
        loads = loads,
        stmt = statement,
        globals = ScopeResolverGlobals(
            globals = FrozenRef.new(globals),
        ),
        codemap = FrozenRef.new(codemap),
        dialect = Dialect.AllOptionsInternal,
    )
    val cst = moduleScopes.cst
    val scopeData = moduleScopes.scopeData
    val scopeErrorsMapped = scopeErrors.map { TypingError.fromEvalException(it) }
    // We don't really need to properly unpack top-level statements,
    // but make it safe against future changes.
    val cstStmts: MutableList<CstStmt> = topLevelStmtsMut(cst)
    val oracleCtx = TypingOracleCtx(codemap = codemap)

    val approximations = mutableListOf<Approximation>()
    val fillTypesResult = try {
        fillTypesForLintTypechecker(
            module = cstStmts,
            ctx = oracleCtx,
            moduleScopeData = scopeData,
            approximations = approximations,
        )
    } catch (e: InternalError) {
        return TypecheckResult(
            errors = listOf(e),
            typeMap = TypeMap(
                codemap = codemap,
                bindings = linkedMapOf(),
            ),
            `interface` = Interface.empty(),
            approximations = emptyList(),
        )
    }
    val (fillTypesErrors, moduleVarTypes) = fillTypesResult

    val typemap = linkedMapOf<BindingId, Triple<String, Span, Ty>>()
    val allSolveErrors = mutableListOf<TypingError>()

    for (top in cstStmts) {
        if (top.node is StmtP.Def<*, *>) {
            val bindingsCollect = try {
                BindingsCollect.collectOne(
                    x = top,
                    typecheckMode = TypecheckMode.Lint,
                    codemap = codemap,
                    approximations = approximations,
                )
            } catch (e: InternalError) {
                return TypecheckResult(
                    errors = listOf(e),
                    typeMap = TypeMap(
                        codemap = codemap,
                        bindings = linkedMapOf(),
                    ),
                    `interface` = Interface.empty(),
                    approximations = emptyList(),
                )
            }
            val (solveErrors, types, solveApproximations) = try {
                solveBindings(bindingsCollect.bindings, oracleCtx, moduleVarTypes)
            } catch (e: InternalError) {
                return TypecheckResult(
                    errors = listOf(e),
                    typeMap = TypeMap(
                        codemap = codemap,
                        bindings = linkedMapOf(),
                    ),
                    `interface` = Interface.empty(),
                    approximations = emptyList(),
                )
            }

            allSolveErrors.addAll(solveErrors)
            approximations.addAll(solveApproximations)

            for ((id, ty) in types) {
                val binding = scopeData.getBinding(id)
                val name = binding.name.asStr()
                val span = when (binding.source) {
                    is BindingSource.Source -> binding.source.span
                    is BindingSource.FromModule -> Span.DEFAULT
                }
                typemap[id] = Triple(name, span, ty)
            }
        }
    }

    val typeMap = TypeMap(
        bindings = typemap,
        codemap = codemap,
    )

    val errors = (scopeErrorsMapped + fillTypesErrors + allSolveErrors)
        .map { it.intoError() }

    val res = mutableMapOf<String, Ty>()
    for ((name, moduleSlotId, vis) in names.allNamesSlotsAndVisibilities()) {
        if (vis == Visibility.Public) {
            val ty = moduleVarTypes.types[moduleSlotId] ?: Ty.any()
            res[name.asStr()] = ty
        }
    }
    val iface = Interface.new(res)

    return TypecheckResult(
        errors = errors,
        typeMap = typeMap,
        `interface` = iface,
        approximations = approximations,
    )
}
