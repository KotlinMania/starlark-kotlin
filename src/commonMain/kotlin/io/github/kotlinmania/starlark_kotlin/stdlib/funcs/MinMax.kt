// port-lint: source src/stdlib/funcs/min_max.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.funcs.min_max

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
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.layout.Value

// fn min_max_iter<'v>(...) -> crate::Result<Value<'v>>
private fun minMaxIter(
    it: Iterator<Value>,
    key: Value?,
    eval: Evaluator,
    min: Boolean,
): Value {
    var max = if (it.hasNext()) {
        it.next()
    } else {
        error("Argument is an empty iterable, max() expect a non empty iterable")
    }
    // Select min on true, max on false.
    val updateMaxOrdering = if (min) 1 else -1 // Ordering::Greater = 1, Ordering::Less = -1
    when (key) {
        null -> {
            for (i in it) {
                if (max.compare(i) == updateMaxOrdering) {
                    max = i
                }
            }
        }
        else -> {
            var cached = key.invokePos(listOf(max), eval)
            for (i in it) {
                val keyi = key.invokePos(listOf(i), eval)
                if (cached.compare(keyi) == updateMaxOrdering) {
                    max = i
                    cached = keyi
                }
            }
        }
    }
    return max
}

/** Common implementation of `min` and `max`. */
// fn min_max<'v>(...) -> crate::Result<Value<'v>>
private fun minMax(
    args: List<Value>,
    key: Value?,
    eval: Evaluator,
    min: Boolean,
): Value {
    return if (args.size == 1) {
        val it = args[0].iterate(eval.heap())
        minMaxIter(it, key, eval, min)
    } else {
        minMaxIter(args.iterator(), key, eval, min)
    }
}

// #[starlark_module]
// pub(crate) fn register_min_max(globals: &mut GlobalsBuilder)
internal fun registerMinMax(globals: GlobalsBuilder) {
    /**
     * [max](https://github.com/bazelbuild/starlark/blob/master/spec.md#max): returns the maximum of a sequence.
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
     * max("two", "three", "four")           == "two"    # the lexicographically greatest
     * max("two", "three", "four", key=len)  == "three"  # the longest
     * ```
     */
    // #[starlark(speculative_exec_safe)]
    // fn max<'v>(args: UnpackTuple<Value<'v>>, key: Option<Value<'v>>, eval: ...) -> ...
    globals.setFunction("max", speculativeExecSafe = true) { eval, callArgs ->
        val args = callArgs.positionalAll()
        val key = callArgs.optionalNamed<Value>("key")
        minMax(args, key, eval, min = false)
    }

    /**
     * [min](https://github.com/bazelbuild/starlark/blob/master/spec.md#min): returns the minimum of a sequence.
     *
     * `min(x)` returns the least element in the iterable sequence x.
     *
     * It is an error if any element does not support ordered comparison,
     * or if the sequence is empty.
     *
     * ```
     * min([3, 1, 4, 1, 5, 9])                 == 1
     * min("two", "three", "four")             == "four"  # the lexicographically least
     * min("two", "three", "four", key=len)    == "two"   # the shortest
     * ```
     */
    // #[starlark(speculative_exec_safe)]
    // fn min<'v>(args: UnpackTuple<Value<'v>>, key: Option<Value<'v>>, eval: ...) -> ...
    globals.setFunction("min", speculativeExecSafe = true) { eval, callArgs ->
        val args = callArgs.positionalAll()
        val key = callArgs.optionalNamed<Value>("key")
        minMax(args, key, eval, min = true)
    }
}
