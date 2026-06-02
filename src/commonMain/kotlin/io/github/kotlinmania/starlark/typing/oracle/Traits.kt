// port-lint: source src/typing/oracle/traits.rs
package io.github.kotlinmania.starlark.typing.oracle

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
 * Unary operator for typechecker.
 */
enum class TypingUnOp(private val symbol: String) {
    /** `+`. */
    PLUS("+"),
    /** `-`. */
    MINUS("-"),
    /** `~`. */
    BIT_NOT("~");

    override fun toString(): String = symbol
}

/**
 * Binary operator for typechecker.
 */
enum class TypingBinOp(private val symbol: String) {
    /** `+`. */
    ADD("+"),
    /** `-`. */
    SUB("-"),
    /** `/`. */
    DIV("/"),
    /** `//`. */
    FLOOR_DIV("/"),
    /** `*`. */
    MUL("*"),
    /** `%`. */
    PERCENT("%"),
    /** `y in x`. */
    IN("in"),
    /** `|`. */
    BIT_OR("|"),
    /** `^`. */
    BIT_XOR("^"),
    /** `&`. */
    BIT_AND("&"),
    /** `<`. */
    LESS("<"),
    /** `<<`. */
    LEFT_SHIFT("<<"),
    /** `>>`. */
    RIGHT_SHIFT(">>");

    override fun toString(): String = symbol

    /**
     * Result type is always `bool`.
     */
    fun alwaysBool(): Boolean {
        return this == IN || this == LESS
    }
}
