// port-lint: source src/eval/compiler/constants.rs
package io.github.kotlinmania.starlark.eval.compiler.constants

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

import io.github.kotlinmania.starlark.environment.Globals
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.types.namespace.FrozenNamespace

/**
 * A wrapper around [FrozenValue] representing a built-in function.
 *
 * Equality is based on pointer identity, which works because the `starlark_module` macro
 * generates a singleton that allocates the function only once even if the builder function
 * is called multiple times.
 */
// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) struct BuiltinFn(pub(crate) FrozenValue);
internal class BuiltinFn(
    val value: FrozenValue,
) {
    // impl PartialEq<FrozenValue> for BuiltinFn
    // Pointer equality works because #[starlark_module] proc macro
    // generates a singleton which allocates the function only once
    // even if builder function is called multiple times.
    fun ptrEq(other: FrozenValue): Boolean = value.toValue().ptrEq(other.toValue())

    // impl PartialEq<FrozenValue> for BuiltinFn
    // impl PartialEq<BuiltinFn> for FrozenValue (symmetric)
    override fun equals(other: Any?): Boolean {
        if (other is FrozenValue) return ptrEq(other)
        if (other is BuiltinFn) return ptrEq(other.value)
        return false
    }

    override fun hashCode(): Int = value.hashCode()
}

/**
 * Lazily-initialized collection of well-known built-in function references.
 *
 * These are looked up once from [Globals.extendedInternal] and cached for the
 * lifetime of the process. The compiler uses them to recognise calls to
 * built-ins such as `len`, `type`, `list`, etc. and emit optimised bytecode.
 */
// pub(crate) struct Constants { ... }
internal class Constants(
    val fnLen: BuiltinFn?,
    val fnType: BuiltinFn?,
    val fnList: BuiltinFn?,
    val fnDict: BuiltinFn?,
    val fnTuple: BuiltinFn?,
    val fnIsinstance: BuiltinFn?,
    val fnSet: BuiltinFn?,
    /** Technically, this is not a function. */
    val typingCallable: BuiltinFn?,
) {
    companion object {
        // pub fn get() -> &'static Constants
        // static RES: Lazy<Constants> = Lazy::new(|| { ... });
        private val instance: Constants by lazy {
            val g = Globals.extendedInternal()
            Constants(
                fnLen = g.getFrozen("len")?.let { BuiltinFn(it) },
                fnType = g.getFrozen("type")?.let { BuiltinFn(it) },
                fnList = g.getFrozen("list")?.let { BuiltinFn(it) },
                fnDict = g.getFrozen("dict")?.let { BuiltinFn(it) },
                fnTuple = g.getFrozen("tuple")?.let { BuiltinFn(it) },
                fnIsinstance = g.getFrozen("isinstance")?.let { BuiltinFn(it) },
                fnSet = g.getFrozen("set")?.let { BuiltinFn(it) },
                typingCallable =
                    run {
                        val typing =
                            g
                                .getFrozen("typing")
                                ?.downcastFrozenRef<FrozenNamespace>()
                        typing?.value?.get("Callable")?.let { BuiltinFn(it) }
                    },
            )
        }

        /**
         * Returns the singleton [Constants] instance, initializing it on first access.
         */
        fun get(): Constants = instance
    }
}
