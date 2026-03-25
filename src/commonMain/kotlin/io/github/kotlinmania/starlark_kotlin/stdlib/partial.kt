// port-lint: source src/stdlib/partial.rs
package io.github.kotlinmania.starlark_kotlin.stdlib

import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.tests.freeze
import io.github.kotlinmania.starlark_kotlin.values.types.list.fmt

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

// Placeholder types referenced from other modules
// These will be replaced with real imports as the port progresses
open class Value {
    fun toStr(): String = ""
    fun fmt(): String = ""
    open fun toValue(): Value = this
    open fun freeze(freezer: Freezer): Value = this
    fun invokeWithLoc(loc: Any?, params: Arguments, eval: Evaluator): kotlin.Result<Value> =
        kotlin.Result.success(Value())
    companion object
}
class FrozenValue : Value() {
    override fun toValue(): Value = this
    companion object
}
class StringValue(val value: String = "") {
    fun asStrHashed(): HashedStr = HashedStr(value)
    override fun toString(): String = value
    fun freeze(freezer: Freezer): FrozenStringValue = FrozenStringValue(value)
    companion object {
        fun new(v: Value): StringValue? = null
    }
}
class FrozenStringValue(val value: String = "") {
    override fun toString(): String = value
}
class HashedStr(val str: String)
class Symbol(val str: String, val hash: Int = str.hashCode()) {
    fun asStr(): String = str
    fun hash(): Int = hash
    companion object {
        fun newHashed(s: HashedStr): Symbol = Symbol(s.str)
    }
}
class Tuple {
    fun content(): List<Value> = emptyList()
    companion object {
        fun fromValue(value: Value): Tuple? = null
    }
}
class DictRef {
    fun keys(): List<Value> = emptyList()
    fun values(): List<Value> = emptyList()
}
class Evaluator {
    fun <T> allocaConcat(a: List<T>, b: List<T>, block: (List<T>, Evaluator) -> kotlin.Result<Value>): kotlin.Result<Value> {
        return block(a + b, this)
    }
}
class Arguments(val full: ArgumentsFull = ArgumentsFull()) {
    companion object
}
class ArgumentsFull(
    val pos: List<Value> = emptyList(),
    val named: List<Value> = emptyList(),
    val names: ArgNames = ArgNames(emptyList()),
    val args: Value? = null,
    val kwargs: Value? = null,
)
class ArgNames(val entries: List<Pair<Symbol, StringValue>>) {
    fun names(): List<Pair<Symbol, StringValue>> = entries
    companion object {
        fun newUnique(names: List<Pair<Symbol, StringValue>>): ArgNames = ArgNames(names)
    }
}
class Freezer
class GlobalsBuilder {
    fun set(name: String, value: Any) {}
}

const val FUNCTION_TYPE: String = "function"

/// Construct a partial application. In almost all cases it is simpler to use a `lambda`.
fun partialStdlib(builder: GlobalsBuilder) {
    builder.set("partial", PartialBuiltin())
}

class PartialBuiltin

/// Generic partial application value.
open class PartialGen<V : Value, S>(
    val func: V,
    // Always references a tuple.
    val pos: V,
    val named: List<V>,
    val names: List<Pair<Symbol, S>>,
    val namesIndex: HashMap<Int, Int> = HashMap(),
) {
    fun posContent(): List<Value> {
        return Tuple.fromValue(pos.toValue())?.content() ?: emptyList()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("partial(")
        sb.append(func)
        sb.append(", *[")
        for ((i, v) in posContent().withIndex()) {
            if (i != 0) sb.append(",")
            sb.append(v.fmt())
        }
        sb.append("], **{")
        for ((i, entry) in names.zip(named).withIndex()) {
            val (kPair, v) = entry
            if (i != 0) sb.append(",")
            sb.append(kPair.first.asStr())
            sb.append(":")
            sb.append(v.toValue().fmt())
        }
        sb.append("})")
        return sb.toString()
    }

    fun nameForCallStack(me: Value): String = "partial"
}

/// Partial application with live values.
class Partial(
    func: Value,
    pos: Value,
    named: List<Value>,
    names: List<Pair<Symbol, StringValue>>,
    namesIndex: HashMap<Int, Int> = HashMap(),
) : PartialGen<Value, StringValue>(func, pos, named, names, namesIndex)

/// Partial application with frozen values.
class FrozenPartial(
    func: FrozenValue,
    pos: FrozenValue,
    named: List<FrozenValue>,
    names: List<Pair<Symbol, FrozenStringValue>>,
    namesIndex: HashMap<Int, Int> = HashMap(),
) : PartialGen<FrozenValue, FrozenStringValue>(func, pos, named, names, namesIndex)

/// Freeze a Partial into a FrozenPartial.
fun freezePartial(partial: Partial, freezer: Freezer): FrozenPartial {
    return FrozenPartial(
        func = partial.func.freeze(freezer) as FrozenValue,
        pos = partial.pos.freeze(freezer) as FrozenValue,
        named = partial.named.map { it.freeze(freezer) as FrozenValue },
        names = partial.names.map { (s, x) -> Pair(s, x.freeze(freezer)) },
        namesIndex = partial.namesIndex,
    )
}

/// Construct a partial application.
fun createPartial(
    func: Value,
    args: Value,
    kwargs: DictRef,
): Partial {
    check(Tuple.fromValue(args) != null)
    val names = kwargs.keys().map { x ->
        val sv = StringValue.new(x)!!
        Pair(
            // We duplicate string here.
            // If this becomes hot, we should do better.
            Symbol.newHashed(sv.asStrHashed()),
            sv,
        )
    }
    val namesIndex = HashMap<Int, Int>()
    for ((i, entry) in names.withIndex()) {
        val (k, _) = entry
        namesIndex[k.hash()] = i
    }
    return Partial(
        func = func,
        pos = args,
        named = kwargs.values(),
        names = names,
        namesIndex = namesIndex,
    )
}

/// Invoke a partial application.
fun invokePartial(
    partial: PartialGen<*, *>,
    args: Arguments,
    eval: Evaluator,
): kotlin.Result<Value> {
    // apply the partial arguments first, then the remaining arguments I was given

    val selfPos = partial.posContent()
    @Suppress("UNCHECKED_CAST")
    val selfNamed = partial.named as List<Value>
    @Suppress("UNCHECKED_CAST")
    val selfNames = partial.names as List<Pair<Symbol, Any>>
    val selfNamesPairs = selfNames.map { Pair(it.first, StringValue(it.second.toString())) }

    for ((symbol, _) in args.full.names.names()) {
        val found = partial.namesIndex.entries.any { (hash, idx) ->
            hash == symbol.hash() && partial.names[idx].first.asStr() == symbol.asStr()
        }
        if (found) {
            return kotlin.Result.failure(
                IllegalArgumentException(
                    "partial() got multiple values for argument `${symbol.asStr()}`"
                )
            )
        }
    }

    return eval.allocaConcat(selfPos, args.full.pos) { pos, eval1 ->
        eval1.allocaConcat(selfNamed, args.full.named) { named, eval2 ->
            eval2.allocaConcat(selfNamesPairs, args.full.names.names()) { names, eval3 ->
                val params = Arguments(
                    ArgumentsFull(
                        pos = pos,
                        named = named,
                        names = ArgNames.newUnique(names),
                        args = args.full.args,
                        kwargs = args.full.kwargs,
                    )
                )
                partial.func.toValue().invokeWithLoc(null, params, eval3)
            }
        }
    }
}
