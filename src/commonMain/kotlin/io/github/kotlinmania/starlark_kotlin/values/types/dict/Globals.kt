// port-lint: source src/values/types/dict/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.types.dict

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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.Arguments
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.function.SpecialBuiltinFunction

/**
 * Helper function to unpack a pair (two-element sequence) from a value.
 */
private fun <V> unpackPair(pair: Value<V>, heap: Heap<V>): Result<Pair<Value<V>, Value<V>>> {
    val it = pair.iterate(heap).getOrElse { return Result.failure(it) }
    val first = it.next()
    if (first != null) {
        val second = it.next()
        if (second != null) {
            if (it.next() == null) {
                return Result.success(Pair(first, second))
            }
        }
    }
    return Result.failure(
        IllegalArgumentException(
            "Found a non-pair element in the positional argument of dict(): ${pair.toRepr()}"
        )
    )
}

/**
 * Register the dict constructor function.
 *
 * This is the Kotlin port of the Rust `#[starlark_module]` annotated function.
 */
internal fun registerDict(globals: GlobalsBuilder) {
    /**
     * [dict](
     * https://github.com/bazelbuild/starlark/blob/master/spec.md#dict
     * ): creates a dictionary.
     *
     * `dict` creates a dictionary. It accepts up to one positional argument,
     * which is interpreted as an iterable of two-element sequences
     * (pairs), each specifying a key/value pair in the
     * resulting dictionary.
     *
     * `dict` also accepts any number of keyword arguments, each of which
     * specifies a key/value pair in the resulting dictionary; each keyword
     * is treated as a string.
     *
     * ```
     * # starlark::assert::all_true(r#"
     * dict() == {}
     * dict(**{'a': 1}) == {'a': 1}
     * dict({'a': 1}) == {'a': 1}
     * dict([(1, 2), (3, 4)]) == {1: 2, 3: 4}
     * dict([(1, 2), ['a', 'b']]) == {1: 2, 'a': 'b'}
     * dict(one=1, two=2) == {'one': 1, 'two': 2}
     * dict([(1, 2)], x=3) == {1: 2, 'x': 3}
     * dict([('x', 2)], x=3) == {'x': 3}
     * # "#);
     * # starlark::assert::is_true(r#"
     * x = {'a': 1}
     * y = dict([('x', 2)], **x)
     * x == {'a': 1} and y == {'x': 2, 'a': 1}
     * # "#);
     * ```
     */
    globals.registerFunction(
        name = "dict",
        asType = FrozenDict::class,
        speculativeExecSafe = true,
        specialBuiltinFunction = SpecialBuiltinFunction.Dict
    ) { args: Arguments<V, *>, heap: Heap<V> ->
        // Dict is super hot, and has a slightly odd signature, so we can do a bunch of special cases on it.
        // In particular, we don't generate the kwargs if there are no positional arguments.
        // Therefore we make it take the raw Arguments.
        // It might have one positional argument, which could be a dict or an array of pairs.
        // It might have named/kwargs arguments, which we copy over (afterwards).

        val pos = args.optional1(heap).getOrElse { return@registerFunction Result.failure(it) }
        val kwargs = args.names().getOrElse { return@registerFunction Result.failure(it) }

        when (pos) {
            null -> Result.success(kwargs)
            else -> {
                var result: Dict<V> = when (val dictPos = DictRef.fromValue(pos)) {
                    null -> {
                        val it = pos.iterate(heap).getOrElse { return@registerFunction Result.failure(it) }
                        val sizeHint = it.sizeHint()
                        val map = SmallMap.withCapacity<Value<V>, Value<V>>(sizeHint.first + kwargs.len())
                        for (el in it) {
                            val (k, v) = unpackPair(el, heap).getOrElse { return@registerFunction Result.failure(it) }
                            val kHashed = k.getHashed().getOrElse { return@registerFunction Result.failure(it) }
                            map.insertHashed(kHashed, v)
                        }
                        Dict.new(map)
                    }
                    else -> {
                        val cloned = dictPos.clone()
                        cloned.reserve(kwargs.len())
                        cloned
                    }
                }
                for ((k, v) in kwargs.iterHashed()) {
                    result.insertHashed(k, v)
                }
                Result.success(result)
            }
        }
    }
}
