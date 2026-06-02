// port-lint: source src/stdlib/partial.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark.stdlib

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

import io.github.kotlinmania.starlark.collections.symbol.Symbol
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.ArgNames
import kotlin.native.HiddenFromObjC
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.rustloc.rustLoc
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.Trace
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueLike
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValueLike
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.types.tuple.TupleGen
import io.github.kotlinmania.starlark.values.types.tuple.fromValue
import kotlin.collections.plus
import kotlin.text.iterator

/** Construct a partial application. In almost all cases it is simpler to use a `lambda`. */
fun partialStdlib(builder: GlobalsBuilder) {
    builder.setFunction("partial") { callArgs, eval ->
        val posIter = callArgs.positions(eval.heap()).getOrElse { return@setFunction Result.failure<Value>(it) }
        if (!posIter.hasNext()) {
            return@setFunction Result.failure<Value>(IllegalArgumentException("partial() missing required positional argument: func"))
        }
        val func = posIter.next()
        val posList = mutableListOf<Value>()
        while (posIter.hasNext()) {
            posList.add(posIter.next())
        }
        val args = eval.heap().allocTuple(posList)

        check(
            TupleGen
                .fromValue(args) != null,
        )
        val kwargsMap = callArgs.namesMap().getOrElse { return@setFunction Result.failure<Value>(it) }
        val names = mutableListOf<Pair<Symbol, StringValue>>()
        val named = mutableListOf<Value>()
        for ((k, v) in kwargsMap.iterHashed()) {
            val sv = k.key()
            names.add(
                Pair(
                    Symbol
                        .newHashed(sv.getHashedStr()),
                    sv,
                ),
            )
            named.add(v)
        }
        val partial =
            Partial(
                func = func,
                pos = args,
                named = named,
                names = names,
            )
        eval.heap().allocComplex(partial)
    }
}

/** Generic partial application value. */
@HiddenFromObjC
open class PartialGen<V : ValueLike, S : StringValueLike>(
    var func: V,
    // Always references a tuple.
    var pos: V,
    var named: List<V>,
    val names: List<Pair<Symbol, S>>,
) : StarlarkValue,
    Trace {
    override val TYPE: String get() = FUNCTION_TYPE
    override val HAS_invoke: Boolean get() = true

    internal val namesIndex: HashMap<ULong, Int> = HashMap<ULong, Int>().apply {
        for ((i, entry) in names.withIndex()) {
            put(entry.first.hash(), i)
        }
    }

    fun posContent(): List<Value> =
        TupleGen
            .fromValue(pos.toValue())
            ?.content() ?: emptyList()

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("partial(")
        sb.append(func)
        sb.append(", *[")
        for ((i, v) in posContent().withIndex()) {
            if (i != 0) sb.append(",")
            sb.append(v)
        }
        sb.append("], **{")
        for ((i, entry) in names.zip(named).withIndex()) {
            val (kPair, v) = entry
            if (i != 0) sb.append(",")
            sb.append(kPair.first.asStr())
            sb.append(":")
            sb.append(v.toValue())
        }
        sb.append("})")
        return sb.toString()
    }

    override fun nameForCallStack(me: Value): String = "partial"

    override fun invoke(
        me: Value,
        args: Arguments,
        eval: Evaluator,
    ): Result<Value> {
        // apply the partial arguments first, then the remaining arguments I was given

        val selfPos = posContent()

        val selfNamed = named.map { it.toValue() }

        val selfNames = names.map { (sym, sv) -> Pair(sym, sv.toStringValue()) }

        // Check for duplicate named arguments
        for ((symbol, _) in args.full.names.names()) {
            val found =
                namesIndex.entries.any { (hash, idx) ->
                    hash == symbol.hash() && names[idx].first.asStr() == symbol.asStr()
                }
            if (found) {
                return Result.failure(
                    IllegalArgumentException(
                        "partial() got multiple values for argument `${symbol.asStr()}`",
                    ),
                )
            }
        }

        // Concatenate partial args with call args
        // In Rust this uses eval.alloca_concat for stack allocation;
        // in Kotlin we simply concatenate lists.
        val pos = selfPos + args.full.pos
        val namedConcat = selfNamed + args.full.named
        val namesConcat = selfNames + args.full.names.names()

        val params =
            Arguments(
                ArgumentsFull(
                    pos = pos,
                    named = namedConcat,
                    names =
                        ArgNames(namesConcat),
                    args = args.full.args,
                    kwargs = args.full.kwargs,
                ),
            )

        return func.toValue().invokeWithLoc(PARTIAL_RUST_LOC, params, eval)
    }

    override fun trace(tracer: Tracer) {
        // func, pos, and named may contain Value objects that need GC tracing.
        // names contain Symbols and StringValues which are identity-traced.
        if (func is Value) {
            val holder = ValueHolder(func as Value)
            tracer.trace(holder)
            @Suppress("UNCHECKED_CAST")
            func = holder.value as V
        }
        if (pos is Value) {
            val holder = ValueHolder(pos as Value)
            tracer.trace(holder)
            @Suppress("UNCHECKED_CAST")
            pos = holder.value as V
        }
        val newNamed = mutableListOf<V>()
        for (v in named) {
            if (v is Value) {
                val holder = ValueHolder(v)
                tracer.trace(holder)
                @Suppress("UNCHECKED_CAST")
                newNamed.add(holder.value as V)
            } else {
                newNamed.add(v)
            }
        }
        named = newNamed
    }
}

private val PARTIAL_RUST_LOC = rustLoc("partial.kt", 1)

/** Partial application with live values. */
@HiddenFromObjC
class Partial(
    func: Value,
    pos: Value,
    named: List<Value>,
    names: List<Pair<Symbol, StringValue>>,
) : PartialGen<Value, StringValue>(func, pos, named, names),
    ComplexValue,
    Freeze<FrozenPartial> {
    override fun freeze(freezer: Freezer): Result<FrozenPartial> {
        val frozenFunc = freezer.freeze(func)
        if (frozenFunc.isFailure) return Result.failure(frozenFunc.exceptionOrNull()!!)
        val frozenPos = freezer.freeze(pos)
        if (frozenPos.isFailure) return Result.failure(frozenPos.exceptionOrNull()!!)
        val frozenNamed = mutableListOf<io.github.kotlinmania.starlark.values.layout.FrozenValue>()
        for (v in named) {
            val f = freezer.freeze(v)
            if (f.isFailure) return Result.failure(f.exceptionOrNull()!!)
            frozenNamed.add(f.getOrThrow())
        }
        val frozenNames = mutableListOf<Pair<Symbol, FrozenStringValue>>()
        for ((s, sv) in names) {
            val fv = sv.freeze(freezer)
            if (fv.isFailure) return Result.failure(fv.exceptionOrNull()!!)
            frozenNames.add(Pair(s, fv.getOrThrow()))
        }
        return Result.success(
            FrozenPartial(
                func = frozenFunc.getOrThrow(),
                pos = frozenPos.getOrThrow(),
                named = frozenNamed,
                names = frozenNames,
            ),
        )
    }
}

/** Partial application with frozen values. */
@HiddenFromObjC
class FrozenPartial(
    func: io.github.kotlinmania.starlark.values.layout.FrozenValue,
    pos: io.github.kotlinmania.starlark.values.layout.FrozenValue,
    named: List<io.github.kotlinmania.starlark.values.layout.FrozenValue>,
    names: List<Pair<Symbol, FrozenStringValue>>,
) : PartialGen<io.github.kotlinmania.starlark.values.layout.FrozenValue, FrozenStringValue>(
        func,
        pos,
        named,
        names,
    )
