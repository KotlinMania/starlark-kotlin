// port-lint: source stdlib/funcs/minMax.rs
package io.github.kotlinmania.starlark.stdlib.funcs.minmax

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
import io.github.kotlinmania.starlark.eval.runtime.optionalNamed
import io.github.kotlinmania.starlark.eval.runtime.positionalAll
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Shared iterator-based implementation for both `min` and `max`.
 *
 * Iterates over [it], tracking the best (minimum or maximum) element seen so far.
 * When [key] is provided, it is invoked on each element to obtain a comparison proxy.
 *
 * @param it Iterator of values to compare.
 * @param key Optional key function applied to each element before comparison.
 * @param eval Current evaluator, used to invoke the key function.
 * @param min If `true`, select the minimum; if `false`, select the maximum.
 * @return The minimum or maximum value, or a failure if the iterator is empty.
 */
private fun minMaxIter(
    it: Iterator<Value>,
    key: Value?,
    eval: Evaluator,
    // Select min on true, max on false.
    min: Boolean,
): Value {
    var best = if (it.hasNext()) {
        it.next()
    } else {
        error("ArgumentP<AstNoPayload> is an empty iterable, max() expect a non empty iterable")
    }
    // When finding min, we update when best > candidate (compare returns positive).
    // When finding max, we update when best < candidate (compare returns negative).
    val updateMaxOrdering = if (min) 1 else -1
    when (key) {
        null -> {
            for (i in it) {
                if (best.compare(i).getOrThrow() == updateMaxOrdering) {
                    best = i
                }
            }
        }
        else -> {
            var cached = key.invokePos(listOf(best), eval).getOrThrow()
            for (i in it) {
                val keyi = key.invokePos(listOf(i), eval).getOrThrow()
                if (cached.compare(keyi).getOrThrow() == updateMaxOrdering) {
                    best = i
                    cached = keyi
                }
            }
        }
    }
    return best
}

/**
 * Common implementation of `min` and `max`.
 *
 * When called with a single argument, iterates over that argument as an iterable.
 * When called with multiple arguments, compares the arguments directly.
 *
 * @param args Positional arguments, either a single iterable or multiple values.
 * @param key Optional key function applied to each element before comparison.
 * @param eval Current evaluator, used to invoke the key function and access the heap.
 * @param min If `true`, select the minimum; if `false`, select the maximum.
 * @return The minimum or maximum value.
 */
private fun minMax(
    args: List<Value>,
    key: Value?,
    eval: Evaluator,
    // Select min on true, max on false.
    min: Boolean,
): Value {
    // if args.items.len() == 1 {
    //     minMaxIter(it, key, eval, min)
    // } else {
    return if (args.size == 1) {
        val it = args[0].iterate(eval.heap()).getOrThrow()
        minMaxIter(it, key, eval, min)
    } else {
        minMaxIter(args.iterator(), key, eval, min)
    }
}

/**
 * Register the `min` and `max` builtin functions with the given [GlobalsBuilder].
 *
 * Both functions accept either a single iterable argument or multiple positional
 * arguments, along with an optional `key` function for comparison.
 */
internal fun registerMinMax(globals: GlobalsBuilder) {
    /**
     * [max](https://github.com/bazelbuild/starlark/blob/master/spec.md#max):
     * returns the maximum of a sequence.
     *
     * `max(x)` returns the greatest element in the iterable sequence x.
     *
     * It is an error if any element does not support ordered comparison,
     * or if the sequence is empty.
     *
     * The optional named parameter `key` specifies a function to be applied
     * to each element prior to comparison.
     *
     * ```
     * max([3, 1, 4, 1, 5, 9])               == 9
     * max("two", "three", "four")           == "two"    // the lexicographically greatest
     * max("two", "three", "four", key=len)  == "three"  // the longest
     * ```
     */
    globals.setFunction("max", speculativeExecSafe = true) { callArgs, eval ->
        val args = callArgs.positionalAll()
        val key = callArgs.optionalNamed<Value>("key")
        minMax(args, key, eval, min = false)
    }

    /**
     * [min](https://github.com/bazelbuild/starlark/blob/master/spec.md#min):
     * returns the minimum of a sequence.
     *
     * `min(x)` returns the least element in the iterable sequence x.
     *
     * It is an error if any element does not support ordered comparison,
     * or if the sequence is empty.
     *
     * The optional named parameter `key` specifies a function to be applied
     * to each element prior to comparison.
     *
     * ```
     * min([3, 1, 4, 1, 5, 9])                 == 1
     * min("two", "three", "four")             == "four"  // the lexicographically least
     * min("two", "three", "four", key=len)    == "two"   // the shortest
     * ```
     */
    globals.setFunction("min", speculativeExecSafe = true) { callArgs, eval ->
        val args = callArgs.positionalAll()
        val key = callArgs.optionalNamed<Value>("key")
        minMax(args, key, eval, min = true)
    }
}
