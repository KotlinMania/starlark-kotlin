// port-lint: source src/stdlib/partial.rs
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
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.types.dict.DictRef
import io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark.values.types.dict.iter
import io.github.kotlinmania.starlark.values.types.tuple.TupleGen
import io.github.kotlinmania.starlark.values.types.tuple.fromValue
import kotlin.collections.plus
import kotlin.text.iterator

// #[starlark_module]
// pub fn partial(builder: &mut GlobalsBuilder)

/** Construct a partial application. In almost all cases it is simpler to use a `lambda`. */
fun partialStdlib(builder: io.github.kotlinmania.starlark.environment.GlobalsBuilder) {
    // fn partial(func: Value, #[starlark(args)] args: Value, #[starlark(kwargs)] kwargs: DictRef)
    builder.setFunction("partial") { callArgs, eval ->
        val func = callArgs.positional<io.github.kotlinmania.starlark.values.layout.Value>(0)
        // Remaining positional args are collected as *args into a tuple
        val args = eval.heap().allocTuple(callArgs.positionalAll().drop(1))
        // kwargs dict
        val kwargsValue = callArgs.full.kwargs
        val kwargs: io.github.kotlinmania.starlark.values.types.dict.DictRef? =
            if (kwargsValue != null) {
                io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue(
                    kwargsValue,
                )
            } else {
                null
            }

        check(
            io.github.kotlinmania.starlark.values.types.tuple.TupleGen.Companion
                .fromValue(args) != null,
        )
        val names = mutableListOf<Pair<io.github.kotlinmania.starlark.collections.symbol.Symbol, io.github.kotlinmania.starlark.values.layout.typed.StringValue>>()
        val named = mutableListOf<io.github.kotlinmania.starlark.values.layout.Value>()
        if (kwargs != null) {
            for ((k, v) in kwargs.iter()) {
                val sv =
                    io.github.kotlinmania.starlark.values.layout.typed.StringValue.Companion
                        .new(k)!!
                // We duplicate string here.
                // If this becomes hot, we should do better.
                names.add(
                    Pair(
                        io.github.kotlinmania.starlark.collections.symbol.Symbol.Companion
                            .newHashed(sv.getHashedStr()),
                        sv,
                    ),
                )
                named.add(v)
            }
        }
        val namesIndex = HashMap<ULong, Int>()
        for ((i, entry) in names.withIndex()) {
            val (k, _) = entry
            namesIndex[k.hash()] = i
        }
        val partial =
            Partial(
                func = func,
                pos = args,
                named = named,
                names = names,
                namesIndex = namesIndex,
            )
        eval.heap().allocComplex(partial)
    }
}

// #[derive(Debug, Coerce, Trace, NoSerialize, ProvidesStaticType, Allocative)]
// #[repr(C)]
// struct PartialGen<V, S>

/** Generic partial application value. */
open class PartialGen<V : Any, S : Any>(
    val func: V,
    // Always references a tuple.
    val pos: V,
    val named: List<V>,
    val names: List<Pair<io.github.kotlinmania.starlark.collections.symbol.Symbol, S>>,
    val namesIndex: HashMap<ULong, Int> = HashMap(),
) : io.github.kotlinmania.starlark.values.StarlarkValue,
    io.github.kotlinmania.starlark.values.Trace {
    override val TYPE: String get() = FUNCTION_TYPE
    override val HAS_invoke: Boolean get() = true

    // impl<'v, V: ValueLike<'v>, S> PartialGen<V, S>
    // fn pos_content(&self) -> &'v [Value<'v>]
    fun posContent(): List<io.github.kotlinmania.starlark.values.layout.Value> {
        @Suppress("UNCHECKED_CAST")
        val posValue =
            when (pos) {
                is io.github.kotlinmania.starlark.values.layout.Value -> pos
                is io.github.kotlinmania.starlark.values.layout.FrozenValue ->
                    (pos as io.github.kotlinmania.starlark.values.layout.FrozenValue).toValue()
                else -> return emptyList()
            }
        return io.github.kotlinmania.starlark.values.types.tuple.TupleGen.Companion
            .fromValue(posValue)
            ?.content() ?: emptyList()
    }

    // impl Display for PartialGen<V, S>
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
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
            @Suppress("UNCHECKED_CAST")
            val value =
                when (v) {
                    is io.github.kotlinmania.starlark.values.layout.Value -> v
                    is io.github.kotlinmania.starlark.values.layout.FrozenValue ->
                        (v as io.github.kotlinmania.starlark.values.layout.FrozenValue).toValue()
                    else -> v
                }
            sb.append(value)
        }
        sb.append("})")
        return sb.toString()
    }

    // #[starlark_value(type = FUNCTION_TYPE)]
    // impl StarlarkValue for PartialGen<V, S>

    // fn name_for_call_stack(&self, _me: Value<'v>) -> String
    override fun nameForCallStack(me: io.github.kotlinmania.starlark.values.layout.Value): String = "partial"

    // fn invoke(&self, _me: Value, args: &Arguments, eval: &mut Evaluator) -> crate::Result<Value>
    override fun invoke(
        me: io.github.kotlinmania.starlark.values.layout.Value,
        args: io.github.kotlinmania.starlark.eval.runtime.Arguments,
        eval: Evaluator,
    ): Result<io.github.kotlinmania.starlark.values.layout.Value> {
        // apply the partial arguments first, then the remaining arguments I was given

        val selfPos = posContent()

        @Suppress("UNCHECKED_CAST")
        val selfNamed =
            named.map { v ->
                when (v) {
                    is io.github.kotlinmania.starlark.values.layout.Value -> v
                    is io.github.kotlinmania.starlark.values.layout.FrozenValue ->
                        (v as io.github.kotlinmania.starlark.values.layout.FrozenValue).toValue()
                    else -> v as io.github.kotlinmania.starlark.values.layout.Value
                }
            }

        @Suppress("UNCHECKED_CAST")
        val selfNames =
            names.map { (sym, sv) ->
                val strVal =
                    when (sv) {
                        is io.github.kotlinmania.starlark.values.layout.typed.StringValue -> sv
                        is io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue ->
                            io.github.kotlinmania.starlark.values.layout.typed.StringValue.Companion
                                .new((sv as io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue).toValue())!!
                        else -> sv as io.github.kotlinmania.starlark.values.layout.typed.StringValue
                    }
                Pair(sym, strVal)
            }

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
            io.github.kotlinmania.starlark.eval.runtime.Arguments(
                io.github.kotlinmania.starlark.eval.runtime.ArgumentsFull(
                    pos = pos,
                    named = namedConcat,
                    names =
                        io.github.kotlinmania.starlark.eval.runtime
                            .ArgNames(namesConcat),
                    args = args.full.args,
                    kwargs = args.full.kwargs,
                ),
            )

        @Suppress("UNCHECKED_CAST")
        val funcValue =
            when (func) {
                is io.github.kotlinmania.starlark.values.layout.Value -> func
                is io.github.kotlinmania.starlark.values.layout.FrozenValue ->
                    (func as io.github.kotlinmania.starlark.values.layout.FrozenValue).toValue()
                else -> func as io.github.kotlinmania.starlark.values.layout.Value
            }
        return funcValue.invokeWithLoc(PARTIAL_RUST_LOC, params, eval)
    }

    // Trace implementation — Rust #[derive(Trace)] walks all fields.
    override fun trace(tracer: io.github.kotlinmania.starlark.values.layout.heap.Tracer) {
        // func, pos, and named may contain Value objects that need GC tracing.
        // names contain Symbols and StringValues which are identity-traced.
        if (func is io.github.kotlinmania.starlark.values.layout.Value) {
            val holder =
                io.github.kotlinmania.starlark.values.layout.heap
                    .ValueHolder(func as io.github.kotlinmania.starlark.values.layout.Value)
            tracer.trace(holder)
        }
        if (pos is io.github.kotlinmania.starlark.values.layout.Value) {
            val holder =
                io.github.kotlinmania.starlark.values.layout.heap
                    .ValueHolder(pos as io.github.kotlinmania.starlark.values.layout.Value)
            tracer.trace(holder)
        }
        for (v in named) {
            if (v is io.github.kotlinmania.starlark.values.layout.Value) {
                val holder =
                    io.github.kotlinmania.starlark.values.layout.heap
                        .ValueHolder(v as io.github.kotlinmania.starlark.values.layout.Value)
                tracer.trace(holder)
            }
        }
    }
}

private val PARTIAL_RUST_LOC = rustLoc("partial.kt", 1)

// type Partial<'v> = PartialGen<Value<'v>, StringValue<'v>>;

/** Partial application with live values. */
class Partial(
    func: io.github.kotlinmania.starlark.values.layout.Value,
    pos: io.github.kotlinmania.starlark.values.layout.Value,
    named: List<io.github.kotlinmania.starlark.values.layout.Value>,
    names: List<Pair<io.github.kotlinmania.starlark.collections.symbol.Symbol, io.github.kotlinmania.starlark.values.layout.typed.StringValue>>,
    namesIndex: HashMap<ULong, Int> = HashMap(),
) : PartialGen<io.github.kotlinmania.starlark.values.layout.Value, io.github.kotlinmania.starlark.values.layout.typed.StringValue>(func, pos, named, names, namesIndex),
    io.github.kotlinmania.starlark.values.ComplexValue,
    io.github.kotlinmania.starlark.values.Freeze<FrozenPartial> {
    // impl Freeze for Partial
    // fn freeze(self, freezer: &Freezer) -> Result<Self::Frozen>
    override fun freeze(freezer: io.github.kotlinmania.starlark.values.layout.Freezer): Result<FrozenPartial> {
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
        val frozenNames = mutableListOf<Pair<io.github.kotlinmania.starlark.collections.symbol.Symbol, io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue>>()
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
                namesIndex = namesIndex,
            ),
        )
    }
}

// type FrozenPartial = PartialGen<FrozenValue, FrozenStringValue>;

/** Partial application with frozen values. */
class FrozenPartial(
    func: io.github.kotlinmania.starlark.values.layout.FrozenValue,
    pos: io.github.kotlinmania.starlark.values.layout.FrozenValue,
    named: List<io.github.kotlinmania.starlark.values.layout.FrozenValue>,
    names: List<Pair<io.github.kotlinmania.starlark.collections.symbol.Symbol, io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue>>,
    namesIndex: HashMap<ULong, Int> = HashMap(),
) : PartialGen<io.github.kotlinmania.starlark.values.layout.FrozenValue, io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue>(
        func,
        pos,
        named,
        names,
        namesIndex,
    )
