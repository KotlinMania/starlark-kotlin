// port-lint: source src/eval/compiler/constants.rs
package io.github.kotlinmania.starlark.eval.compiler.constants

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.types.namespace.NamespaceGen

internal data class BuiltinFn(val value: FrozenValue) {

    fun eq(other: FrozenValue): Boolean {
        // generates a singleton which allocates the function only once
        // even if builder function is called multiple times.
        return value.toValue().ptrEq(other.toValue())
    }

    override fun equals(other: Any?): Boolean {
        if (other is FrozenValue) return eq(other)
        if (other is BuiltinFn) return other.eq(value)
        return false
    }

    override fun hashCode(): Int = value.hashCode()
}

internal class Constants(
    val fnLen: BuiltinFn,
    val fnType: BuiltinFn,
    val fnList: BuiltinFn,
    val fnDict: BuiltinFn,
    val fnTuple: BuiltinFn,
    val fnIsinstance: BuiltinFn,
    val fnSet: BuiltinFn,
    // Technically, this is not a function.
    val typingCallable: BuiltinFn,
) {
    companion object {
        private val RES: Constants by lazy {
            val g = Globals.extendedInternal()
            Constants(
                fnLen = BuiltinFn(g.getFrozen("len")!!),
                fnType = BuiltinFn(g.getFrozen("type")!!),
                fnList = BuiltinFn(g.getFrozen("list")!!),
                fnDict = BuiltinFn(g.getFrozen("dict")!!),
                fnTuple = BuiltinFn(g.getFrozen("tuple")!!),
                fnIsinstance = BuiltinFn(g.getFrozen("isinstance")!!),
                fnSet = BuiltinFn(g.getFrozen("set")!!),
                typingCallable = run {
                    val typing = g.getFrozen("typing")!!
                        .downcastFrozenRef<NamespaceGen<FrozenValue>>()!!
                    BuiltinFn(typing.value.get("Callable")!!)
                },
            )
        }

        fun get(): Constants = RES
    }
}
