// port-lint: source src/eval/compiler/args.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.args

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

// #[derive(Default, Clone, Debug, VisitSpanMut)]
// pub(crate) struct ArgsCompiledValue {
//     pub(crate) pos_named: Vec<IrSpanned<ExprCompiled>>,
//     pub(crate) names: Vec<(Symbol, FrozenStringValue)>,
//     pub(crate) args: Option<IrSpanned<ExprCompiled>>,
//     pub(crate) kwargs: Option<IrSpanned<ExprCompiled>>,
// }
internal class ArgsCompiledValue(
    val posNamed: MutableList<IrSpanned<ExprCompiled>> = mutableListOf(),
    /// Named arguments compiled.
    ///
    /// Note names are guaranteed to be unique here because names are validated in AST:
    /// named arguments in `Expr.Call` are unique.
    val names: MutableList<Pair<Symbol, FrozenStringValue>> = mutableListOf(),
    var args: IrSpanned<ExprCompiled>? = null,
    var kwargs: IrSpanned<ExprCompiled>? = null,
) {
    /// Check if arguments is one positional argument.
    // pub(crate) fn one_pos(&self) -> Option<&IrSpanned<ExprCompiled>>
    fun onePos(): IrSpanned<ExprCompiled>? {
        return if (posNamed.size == 1 && names.isEmpty() && args == null && kwargs == null) {
            posNamed[0]
        } else {
            null
        }
    }

    /// Check if arguments is two positional arguments.
    // pub(crate) fn two_pos(&self) -> Option<(&IrSpanned<ExprCompiled>, &IrSpanned<ExprCompiled>)>
    fun twoPos(): Pair<IrSpanned<ExprCompiled>, IrSpanned<ExprCompiled>>? {
        return if (posNamed.size == 2 && names.isEmpty() && args == null && kwargs == null) {
            Pair(posNamed[0], posNamed[1])
        } else {
            null
        }
    }

    // pub(crate) fn pos_only(&self) -> Option<&[IrSpanned<ExprCompiled>]>
    fun posOnly(): List<IrSpanned<ExprCompiled>>? {
        return if (names.isEmpty() && args == null && kwargs == null) {
            posNamed
        } else {
            null
        }
    }

    // fn split_pos_names(&self) -> (&[IrSpanned<ExprCompiled>], &[IrSpanned<ExprCompiled>])
    private fun splitPosNames(): Pair<List<IrSpanned<ExprCompiled>>, List<IrSpanned<ExprCompiled>>> {
        val splitAt = posNamed.size - names.size
        return Pair(
            posNamed.subList(0, splitAt),
            posNamed.subList(splitAt, posNamed.size),
        )
    }

    /// Invoke a callback if all arguments are frozen values.
    // pub(crate) fn all_values<'v, R>(&self, handler: impl FnOnce(&Arguments<'v, '_>) -> R) -> Option<R>
    fun <R> allValues(handler: (Arguments) -> R): R? {
        return allValuesGeneric({ e -> e.asValue()?.toValue() }, handler)
    }

    /// Invoke a callback if all arguments are frozen values.
    // pub(crate) fn all_values_generic<'v, R>(&self, expr_to_value, handler) -> Option<R>
    fun <R> allValuesGeneric(
        exprToValue: (ExprCompiled) -> Any?,
        handler: (Arguments) -> R,
    ): R? {
        val (pos, named) = splitPosNames()
        val posValues = pos.mapNotNull { e -> exprToValue(e.node) } .takeIf { it.size == pos.size } ?: return null
        val namedValues = named.mapNotNull { e -> exprToValue(e.node) } .takeIf { it.size == named.size } ?: return null
        val argsValue = args?.let { exprToValue(it.node) ?: return null }
        val kwargsValue = kwargs?.let { exprToValue(it.node) ?: return null }
        return handler(Arguments(ArgumentsFull(
            pos = posValues,
            named = namedValues,
            names = ArgNames.newUnique(names),
            args = argsValue,
            kwargs = kwargsValue,
        )))
    }

    /// Expressions of all arguments: positional, named, star-args, star-star-args.
    // pub(crate) fn arg_exprs(&self) -> impl Iterator<Item = &IrSpanned<ExprCompiled>>
    fun argExprs(): Sequence<IrSpanned<ExprCompiled>> {
        return sequence {
            yieldAll(posNamed)
            args?.let { yield(it) }
            kwargs?.let { yield(it) }
        }
    }

    // pub(crate) fn map_exprs<E>(&self, f: impl FnMut(&IrSpanned<ExprCompiled>) -> Result<IrSpanned<ExprCompiled>, E>) -> Result<ArgsCompiledValue, E>
    fun <E : Exception> mapExprs(
        f: (IrSpanned<ExprCompiled>) -> IrSpanned<ExprCompiled>,
    ): ArgsCompiledValue {
        return ArgsCompiledValue(
            posNamed = posNamed.map { f(it) }.toMutableList(),
            names = names.toMutableList(),
            args = args?.let { f(it) },
            kwargs = kwargs?.let { f(it) },
        )
    }

    // pub(crate) fn optimize(&self, ctx: &mut OptCtx) -> ArgsCompiledValue
    fun optimize(ctx: OptCtx): ArgsCompiledValue {
        return mapExprs<Nothing> { e -> e.optimize(ctx) }
    }

    // pub(crate) fn push_pos(&mut self, expr: IrSpanned<ExprCompiled>)
    fun pushPos(expr: IrSpanned<ExprCompiled>) {
        posNamed.add(expr)
    }
}

// impl Compiler { pub(crate) fn args(...) }
// Kotlin: free function or extension; will be placed on Compiler when that class is ported.
internal fun compilerArgs(
    compiler: Compiler,
    callArgs: CallArgsP,
): ArgsCompiledValue {
    val res = ArgsCompiledValue()
    for (x in callArgs.args) {
        when (val node = x.node) {
            is ArgumentP.Positional -> res.posNamed.add(compiler.expr(node.expr))
            is ArgumentP.Named -> {
                val fv = compiler.eval.moduleEnv.frozenHeap().allocStrIntern(node.name.node)
                res.names.add(Pair(Symbol.new(node.name.node), fv))
                res.posNamed.add(compiler.expr(node.value))
            }
            is ArgumentP.Args -> res.args = compiler.expr(node.expr)
            is ArgumentP.KwArgs -> res.kwargs = compiler.expr(node.expr)
        }
    }
    return res
}

// Placeholder types for dependencies not yet ported
internal class IrSpanned<T>(val span: Any = Any(), val node: T) {
    fun optimize(ctx: OptCtx): IrSpanned<T> = this
}
internal class ExprCompiled {
    fun asValue(): FrozenValue? = null
}
internal class Symbol {
    companion object {
        fun new(name: String): Symbol = Symbol()
    }
}
internal class FrozenStringValue
internal class FrozenValue {
    fun toValue(): Any = Any()
}
internal class Arguments(val full: ArgumentsFull)
internal class ArgumentsFull(
    val pos: List<Any>,
    val named: List<Any>,
    val names: ArgNames,
    val args: Any?,
    val kwargs: Any?,
)
internal class ArgNames {
    companion object {
        fun newUnique(names: List<Pair<Symbol, FrozenStringValue>>): ArgNames = ArgNames()
    }
}
internal class OptCtx
internal class Compiler(val eval: EvalContext = EvalContext()) {
    fun expr(x: Any): IrSpanned<ExprCompiled> = IrSpanned(node = ExprCompiled())
}
internal class EvalContext(val moduleEnv: ModuleEnv = ModuleEnv())
internal class ModuleEnv {
    fun frozenHeap(): FrozenHeap = FrozenHeap()
}
internal class FrozenHeap {
    fun allocStrIntern(s: String): FrozenStringValue = FrozenStringValue()
}
internal class CallArgsP(val args: List<Spanned<ArgumentP>> = emptyList())
internal class Spanned<T>(val node: T)
internal sealed class ArgumentP {
    class Positional(val expr: Any) : ArgumentP()
    class Named(val name: Spanned<String>, val value: Any) : ArgumentP()
    class Args(val expr: Any) : ArgumentP()
    class KwArgs(val expr: Any) : ArgumentP()
}
