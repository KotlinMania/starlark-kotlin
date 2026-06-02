// port-lint: source src/eval/runtime/params/parser.rs
package io.github.kotlinmania.starlark.eval.runtime.params

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

import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value

/**
 * Parse a series of parameters which were specified by
 * [`ParametersSpec`].
 *
 * This is created with [`ParametersSpec.parser`].
 */
class ParametersParser(
    // Invariant: `slots` and `names` are the same length.
    private val slots: List<Value?>,
    private val names: List<String>,
) {
    private var index: Int = 0

    companion object {
        /** Create a parameter parser, which stored parameters into provided slots reference. */
        fun new(slots: List<Value?>, names: List<String>): ParametersParser {
            // This assertion is important because we get unchecked in `get_next`.
            check(slots.size == names.size)
            return ParametersParser(slots, names)
        }
    }

    private fun getNext(): Pair<Value?, String> {
        check(index < slots.size) { "Requesting more parameters than were specified" }
        val v = slots[index]
        val name = names[index]
        index++
        return Pair(v, name)
    }

    /** Obtain the next optional parameter (without a default value). */
    fun <T> nextOpt(unpack: UnpackValue<T>): T? {
        val (v, name) = getNext()
        return if (v == null) null else unpack.unpackNamedParam(v, name)
    }

    /** Obtain the next parameter. Fail if the parameter is optional and not provided. */
    fun <T> next(unpack: UnpackValue<T>): T {
        val (v, name) = getNext()
        checkNotNull(v) { "Requested non-optional param $name which was declared optional in signature" }
        return unpack.unpackNamedParam(v, name)
    }

    internal fun isEof(): Boolean = index >= slots.size
}
