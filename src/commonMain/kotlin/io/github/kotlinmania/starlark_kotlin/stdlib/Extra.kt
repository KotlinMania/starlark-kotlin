// port-lint: source src/stdlib/extra.rs
package io.github.kotlinmania.starlark_kotlin.stdlib

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

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpack.UnpackTuple
import io.github.kotlinmania.starlark_kotlin.values.types.string.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.list.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.types.list.NoneType
import io.github.kotlinmania.starlark_kotlin.values.types.list.NoneOr
import io.github.kotlinmania.starlark_kotlin.values.types.StarlarkFunction
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.string.allocStr
import io.github.kotlinmania.starlark_kotlin.values.types.list.None
import io.github.kotlinmania.starlark_kotlin.values.iterate
import io.github.kotlinmania.starlark_kotlin.analysis.Other
import io.github.kotlinmania.starlark_kotlin.values.types.none.isNone
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.toStr
import io.github.kotlinmania.starlark_kotlin.assert.printHandler

/// Apply a predicate to each element of the iterable, returning those that match.
/// As a special case if the function is `None` then removes all the `None` values.
///
/// ```
/// filter(bool, [0, 1, False, True]) == [1, True]
/// filter(lambda x: x > 2, [1, 2, 3, 4]) == [3, 4]
/// filter(None, [True, None, False]) == [True, False]
/// ```
fun filter(
    func: NoneOr<ValueOfUnchecked<StarlarkFunction>>,
    seq: ValueOfUnchecked<StarlarkIter<Value>>,
    eval: Evaluator,
): Result<List<Value>> {
    val res = mutableListOf<Value>()

    for (v in seq.get().iterate(eval.heap())) {
        when (func) {
            is NoneOr.None -> {
                if (!v.isNone()) {
                    res.add(v)
                }
            }
            is NoneOr.Other -> {
                if (func.get().invokePos(listOf(v), eval).toBool()) {
                    res.add(v)
                }
            }
        }
    }
    return Result.success(res)
}

/// Apply a function to each element of the iterable, returning the results.
///
/// ```
/// map(abs, [7, -5, -6]) == [7, 5, 6]
/// map(lambda x: x * 2, [1, 2, 3, 4]) == [2, 4, 6, 8]
/// ```
fun map(
    func: ValueOfUnchecked<StarlarkFunction>,
    seq: ValueOfUnchecked<StarlarkIter<Value>>,
    eval: Evaluator,
): Result<List<Value>> {
    val it = seq.get().iterate(eval.heap())
    val res = mutableListOf<Value>()
    for (v in it) {
        res.add(func.get().invokePos(listOf(v), eval))
    }
    return Result.success(res)
}

/// Print the value with full debug formatting. The result may not be stable over time.
/// Intended for debugging purposes and guaranteed to produce verbose output not suitable for user display.
fun debug(
    v: Value,
): Result<String> {
    return Result.success(v.debugRepr())
}

private class PrintWrapper(private val values: List<Value>) {
    override fun toString(): String {
        return values.joinToString(" ") { it.toString() }
    }

    fun prettyPrint(): String {
        return values.joinToString(" ") { it.prettyRepr() }
    }
}

/// Invoked from `print` or `pprint` to print a value.
interface PrintHandler {
    /// If this function returns error, evaluation fails with this error.
    fun println(text: String): Result<Unit>
}

internal class StderrPrintHandler : PrintHandler {
    override fun println(text: String): Result<Unit> {
        System.err.println(text)
        return Result.success(Unit)
    }
}

/// Print some values to the output.
fun print(
    args: UnpackTuple<Value>,
    eval: Evaluator,
): Result<NoneType> {
    // In practice most users should want to put the print somewhere else, but this does for now.
    // Unfortunately, we can't use PrintWrapper because strings to_str() and Display are different.
    eval.printHandler.println(args.items.joinToString(" ") { it.toStr() }).getOrThrow()
    return Result.success(NoneType)
}

fun pprint(
    args: UnpackTuple<Value>,
    eval: Evaluator,
): Result<NoneType> {
    // In practice most users may want to put the print somewhere else, but this does for now.
    eval.printHandler.println(PrintWrapper(args.items).prettyPrint()).getOrThrow()
    return Result.success(NoneType)
}

private fun prettyRepr(
    a: Value,
    eval: Evaluator,
): Result<StringValue> {
    val s = a.prettyRepr()
    val r = eval.heap().allocStr(s)
    return Result.success(r)
}

/// Like `str`, but produces more verbose pretty-printed output.
fun pstr(
    a: Value,
    eval: Evaluator,
): Result<StringValue> {
    val sv = StringValue.new(a)
    if (sv != null) {
        return Result.success(sv)
    }
    return prettyRepr(a, eval)
}

/// Like `repr`, but produces more verbose pretty-printed output.
fun prepr(
    a: Value,
    eval: Evaluator,
): Result<StringValue> {
    return prettyRepr(a, eval)
}

fun registerFilter(globals: GlobalsBuilder) {
    globals.set("filter", ::filter)
}

fun registerMap(globals: GlobalsBuilder) {
    globals.set("map", ::map)
}

fun registerDebug(globals: GlobalsBuilder) {
    globals.set("debug", ::debug)
}

fun registerPrint(globals: GlobalsBuilder) {
    globals.set("print", ::print)
}

fun registerPprint(globals: GlobalsBuilder) {
    globals.set("pprint", ::pprint)
}

fun registerPstr(globals: GlobalsBuilder) {
    globals.set("pstr", ::pstr)
}

fun registerPrepr(globals: GlobalsBuilder) {
    globals.set("prepr", ::prepr)
}
