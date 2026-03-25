// port-lint: source src/stdlib/funcs/min_max.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.funcs

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpack.UnpackTuple
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.iterate

private fun minMaxIter(
    it: Iterator<Value>,
    key: Value?,
    eval: Evaluator,
    // Select min on true, max on false.
    min: Boolean,
): Result<Value> {
    var best = if (it.hasNext()) {
        it.next()
    } else {
        return Result.failure(
            IllegalArgumentException(
                "Argument is an empty iterable, max() expect a non empty iterable"
            )
        )
    }
    // updateMaxOrdering: if min, we update when current > candidate (Greater);
    // if max, we update when current < candidate (Less).
    val updateMaxOrdering = if (min) 1 else -1
    when (key) {
        null -> {
            for (i in it) {
                if (best.compare(i) == updateMaxOrdering) {
                    best = i
                }
            }
        }
        else -> {
            var cached = key.invokePos(listOf(best), eval)
            for (i in it) {
                val keyi = key.invokePos(listOf(i), eval)
                if (cached.compare(keyi) == updateMaxOrdering) {
                    best = i
                    cached = keyi
                }
            }
        }
    }
    return Result.success(best)
}

/// Common implementation of `min` and `max`.
private fun minMax(
    args: UnpackTuple<Value>,
    key: Value?,
    eval: Evaluator,
    // Select min on true, max on false.
    min: Boolean,
): Result<Value> {
    return if (args.items.size == 1) {
        val it = args.items[0].iterate(eval.heap())
        minMaxIter(it, key, eval, min)
    } else {
        minMaxIter(args.items.iterator(), key, eval, min)
    }
}

/// [max](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#max
/// ): returns the maximum of a sequence.
///
/// `max(x)` returns the greatest element in the iterable sequence x.
///
/// It is an error if any element does not support ordered comparison,
/// or if the sequence is empty.
///
/// The optional named parameter `key` specifies a function to be applied
/// to each element prior to comparison.
///
/// ```
/// max([3, 1, 4, 1, 5, 9])               == 9
/// max("two", "three", "four")           == "two"    # the lexicographically greatest
/// max("two", "three", "four", key=len)  == "three"  # the longest
/// ```
fun max(
    args: UnpackTuple<Value>,
    key: Value? = null,
    eval: Evaluator,
): Result<Value> {
    return minMax(args, key, eval, false)
}

/// [min](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#min
/// ): returns the minimum of a sequence.
///
/// `min(x)` returns the least element in the iterable sequence x.
///
/// It is an error if any element does not support ordered comparison,
/// or if the sequence is empty.
///
/// ```
/// min([3, 1, 4, 1, 5, 9])                 == 1
/// min("two", "three", "four")             == "four"  # the lexicographically least
/// min("two", "three", "four", key=len)    == "two"   # the shortest
/// ```
fun min(
    args: UnpackTuple<Value>,
    key: Value? = null,
    eval: Evaluator,
): Result<Value> {
    return minMax(args, key, eval, true)
}

fun registerMinMax(globals: GlobalsBuilder) {
    globals.set("max", ::max)
    globals.set("min", ::min)
}
