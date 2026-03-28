// port-lint: source src/lexer.rs
package io.github.kotlinmania.starlark_kotlin.syntax.lexer

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

import com.ionspin.kotlin.bignum.integer.BigInteger

sealed class TokenInt {
    data class I32(val value: Int) : TokenInt()
    /** Only if larger than `i32`. */
    data class BigInt(val value: BigInteger) : TokenInt()

    override fun toString(): String = when (this) {
        is I32 -> value.toString()
        is BigInt -> value.toString()
    }
}
class TokenString
class Token
class TokenFString
