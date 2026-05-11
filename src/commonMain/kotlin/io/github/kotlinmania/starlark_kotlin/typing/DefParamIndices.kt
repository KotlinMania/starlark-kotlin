<<<<<<< HEAD:src/commonMain/kotlin/io/github/kotlinmania/starlark/typing/DefParamIndices.kt
// port-lint: source ../starlark_syntax/src/syntax/def.rs
package io.github.kotlinmania.starlark.typing
=======
// port-lint: source starlark_syntax/src/syntax/def.rs
package io.github.kotlinmania.starlark_kotlin.typing
>>>>>>> origin/main:src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/typing/DefParamIndices.kt

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
// #[derive(Copy, Clone, Dupe, Debug, Eq, PartialEq, Hash, Ord, PartialOrd, Allocative)]
// pub struct DefParamIndices
data class DefParamIndices(
    /**
     * Number of parameters which can be filled positionally.
     * That is, number of parameters before first `*`, `*args` or `**kwargs`.
     */
    // pub num_positional: u32,
    val numPositional: UInt,
    /**
     * Number of parameters which can only be filled positionally.
     * Always less or equal to `num_positional`.
     */
    // pub num_positional_only: u32,
    val numPositionalOnly: UInt,
    /**
     * Index of `*args` parameter, if any.
     * If present, equal to `num_positional`.
     */
    // pub args: Option<u32>,
    val args: UInt? = null,
    /**
     * Index of `**kwargs` parameter, if any.
     * If present, equal to the number of parameters minus 1.
     */
    // pub kwargs: Option<u32>,
    val kwargs: UInt? = null,
) : Comparable<DefParamIndices> {

    // pub fn pos_only(&self) -> Range<usize>
    fun posOnly(): IntRange = 0 until numPositionalOnly.toInt()

    // pub fn pos_or_named(&self) -> Range<usize>
    fun posOrNamed(): IntRange = numPositionalOnly.toInt() until numPositional.toInt()

    // pub fn named_only(&self, param_count: usize) -> Range<usize>
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
