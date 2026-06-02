// port-lint: source starlark_syntax/src/syntax/def.rs
package io.github.kotlinmania.starlark.typing

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

/**
 * Parameters internally in starlark-rust are commonly represented as a flat list of parameters,
 * with markers `/` and `*` omitted.
 * This class contains sizes and indices to split the list into parts.
 */
data class DefParamIndices(
    /**
     * Number of parameters which can be filled positionally.
     * That is, number of parameters before first `*`, `*args` or `**kwargs`.
     */
    val numPositional: UInt,
    /**
     * Number of parameters which can only be filled positionally.
     * Always less or equal to `num_positional`.
     */
    val numPositionalOnly: UInt,
    /**
     * Index of `*args` parameter, if any.
     * If present, equal to `num_positional`.
     */
    val args: UInt? = null,
    /**
     * Index of `**kwargs` parameter, if any.
     * If present, equal to the number of parameters minus 1.
     */
    val kwargs: UInt? = null,
) : Comparable<DefParamIndices> {
    fun posOnly(): IntRange = 0 until numPositionalOnly.toInt()

    fun posOrNamed(): IntRange = numPositionalOnly.toInt() until numPositional.toInt()

    fun namedOnly(paramCount: Int): IntRange {
        val start = args?.let { it.toInt() + 1 } ?: numPositional.toInt()
        val end = kwargs?.toInt() ?: paramCount
        return start until end
    }

    override fun compareTo(other: DefParamIndices): Int {
        numPositional.compareTo(other.numPositional).let { if (it != 0) return it }
        numPositionalOnly.compareTo(other.numPositionalOnly).let { if (it != 0) return it }
        (args ?: UInt.MAX_VALUE).compareTo(other.args ?: UInt.MAX_VALUE).let { if (it != 0) return it }
        return (kwargs ?: UInt.MAX_VALUE).compareTo(other.kwargs ?: UInt.MAX_VALUE)
    }
}
