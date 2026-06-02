// port-lint: source src/stdlib/extra.rs
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

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.list.allocList
import io.github.kotlinmania.starlark.values.types.none.NoneType

/**
 * Apply a predicate to each element of the iterable, returning those that match.
 * As a special case if the function is `None` then removes all the `None` values.
 *
 * ```
 * filter(bool, [0, 1, False, True]) == [1, True]
 * filter(lambda x: x > 2, [1, 2, 3, 4]) == [3, 4]
 * filter(None, [True, None, False]) == [True, False]
 * ```
 */
private fun filter(
    func: Value,
    seq: Value,
    eval: Evaluator,
): Result<List<Value>> {
    val res = mutableListOf<Value>()
    val heap = eval.heap()

    val iter = seq.iterate(heap).getOrElse { return Result.failure(it) }
    for (v in iter) {
        if (func.isNone()) {
            if (!v.isNone()) {
                res.add(v)
            }
        } else {
            val callResult =
                func.invokePos(listOf(v), eval).getOrElse {
                    return Result.failure(it)
                }
            if (callResult.toBool()) {
                res.add(v)
            }
        }
    }
    return Result.success(res)
}

/**
 * Apply a function to each element of the iterable, returning the results.
 *
 * ```
 * map(abs, [7, -5, -6]) == [7, 5, 6]
 * map(lambda x: x * 2, [1, 2, 3, 4]) == [2, 4, 6, 8]
 * ```
 */

private fun map(
    func: Value,
    seq: Value,
    eval: Evaluator,
): Result<List<Value>> {
    val iter = seq.iterate(eval.heap()).getOrElse { return Result.failure(it) }
    val res = mutableListOf<Value>()
    for (v in iter) {
        res.add(func.invokePos(listOf(v), eval).getOrElse { return Result.failure(it) })
    }
    return Result.success(res)
}

/**
 * Print the value with full debug formatting. The result may not be stable over time.
 * Intended for debugging purposes and guaranteed to produce verbose output not suitable for user display.
 */
// fn debug(val: Value) -> anyhow::Result<String>
private fun debug(
    v: Value,
): Result<String> = Result.success(v.toString())

private class PrintWrapper(
    private val values: List<Value>,
) {
    override fun toString(): String = values.joinToString(" ") { it.toString() }
}

/** Invoked from `print` or `pprint` to print a value. */
interface PrintHandler {
    /** If this function returns error, evaluation fails with this error. */
    fun println(text: String): Result<Unit>
}

// Users are expected to replace this handler via Evaluator.setPrintHandler().
internal class StderrPrintHandler : PrintHandler {
    override fun println(text: String): Result<Unit> {
        kotlin.io.println(text)
        return Result.success(Unit)
    }
}

/** Print some values to the output. */
private fun printImpl(
    args: List<Value>,
    eval: Evaluator,
): Result<NoneType> {
    // In practice most users should want to put the print somewhere else, but this does for now.
    // Unfortunately, we can't use PrintWrapper because strings toStr() and Display are different.
    eval.printHandler.println(args.joinToString(" ") { it.toStr() }).getOrThrow()
    return Result.success(NoneType)
}

private fun pprintImpl(
    args: List<Value>,
    eval: Evaluator,
): Result<NoneType> {
    // In practice most users may want to put the print somewhere else, but this does for now.
    eval.printHandler.println(PrintWrapper(args).toString()).getOrThrow()
    return Result.success(NoneType)
}

private fun prettyRepr(
    a: Value,
    eval: Evaluator,
): Result<StringValue> {
    val s = a.toRepr()
    val r = eval.heap().allocStr(s)
    return Result.success(StringValue.newUnchecked(r))
}

/** Like `str`, but produces more verbose pretty-printed output. */
private fun pstrImpl(
    a: Value,
    eval: Evaluator,
): Result<StringValue> {
    val sv = StringValue.new(a)
    if (sv != null) {
        return Result.success(sv)
    }
    return prettyRepr(a, eval)
}

/** Like `repr`, but produces more verbose pretty-printed output. */
private fun preprImpl(
    a: Value,
    eval: Evaluator,
): Result<StringValue> = prettyRepr(a, eval)

fun registerFilter(globals: GlobalsBuilder) {
    globals.setFunction("filter") { callArgs, eval ->
        val func = callArgs.positional<Value>(0)
        val seq = callArgs.positional<Value>(1)
        val result = filter(func, seq, eval).getOrThrow()
        eval.heap().allocList(result)
    }
}

fun registerMap(globals: GlobalsBuilder) {
    globals.setFunction("map") { callArgs, eval ->
        val func = callArgs.positional<Value>(0)
        val seq = callArgs.positional<Value>(1)
        val result = map(func, seq, eval).getOrThrow()
        eval.heap().allocList(result)
    }
}

fun registerDebug(globals: GlobalsBuilder) {
    globals.setFunction("debug") { callArgs, eval ->
        val v = callArgs.positional<Value>(0)
        val result = debug(v).getOrThrow()
        eval.heap().allocStr(result)
    }
}

fun registerPrint(globals: GlobalsBuilder) {
    globals.setFunction("print") { callArgs, eval ->
        printImpl(callArgs.positionalAll(), eval).getOrThrow()
        Value.newNone()
    }
}

fun registerPprint(globals: GlobalsBuilder) {
    globals.setFunction("pprint") { callArgs, eval ->
        pprintImpl(callArgs.positionalAll(), eval).getOrThrow()
        Value.newNone()
    }
}

fun registerPstr(globals: GlobalsBuilder) {
    globals.setFunction("pstr") { callArgs, eval ->
        val a = callArgs.positional<Value>(0)
        pstrImpl(a, eval).getOrThrow().toValue()
    }
}

fun registerPrepr(globals: GlobalsBuilder) {
    globals.setFunction("prepr") { callArgs, eval ->
        val a = callArgs.positional<Value>(0)
        preprImpl(a, eval).getOrThrow().toValue()
    }
}
