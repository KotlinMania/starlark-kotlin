// port-lint: source src/eval/compiler/constants.rs
package io.github.kotlinmania.starlark_kotlin.eval.compiler.constants

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

import io.github.kotlinmania.starlark_kotlin.environment.Globals
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.FrozenNamespace
import io.github.kotlinmania.starlark_kotlin.values.types.list.ptrEq
import io.github.kotlinmania.starlark_kotlin.values.owned.asRef
import io.github.kotlinmania.starlark_kotlin.eval.compiler.call.downcastFrozenRef
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.asRef

// #[derive(Copy, Clone, Dupe, Debug)]
// pub(crate) struct BuiltinFn(pub(crate) FrozenValue);
internal class BuiltinFn(val value: FrozenValue) {
    // impl PartialEq<FrozenValue> for BuiltinFn
    // fn eq(&self, other: &FrozenValue) -> bool
    // Pointer equality works because #[starlark_module] proc macro
    // generates a singleton which allocates the function only once.
    fun ptrEq(other: FrozenValue): Boolean {
        return value.toValue().ptrEq(other.toValue())
    }

    override fun equals(other: Any?): Boolean {
        if (other is FrozenValue) return ptrEq(other)
        if (other is BuiltinFn) return ptrEq(other.value)
        return false
    }

    override fun hashCode(): Int = value.hashCode()
}

// pub(crate) struct Constants {
//     pub(crate) fn_len: BuiltinFn,
//     pub(crate) fn_type: BuiltinFn,
//     pub(crate) fn_list: BuiltinFn,
//     pub(crate) fn_dict: BuiltinFn,
//     pub(crate) fn_tuple: BuiltinFn,
//     pub(crate) fn_isinstance: BuiltinFn,
//     pub(crate) fn_set: BuiltinFn,
//     pub(crate) typing_callable: BuiltinFn,
// }
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
    // impl Constants

    companion object {
        // pub fn get() -> &'static Constants
        // static RES: Lazy<Constants> = Lazy::new(|| { ... });
        private val instance: Constants by lazy {
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
                        .downcastFrozenRef<FrozenNamespace>()!!
                    BuiltinFn(typing.asRef().get("Callable")!!)
                },
            )
        }

        fun get(): Constants = instance
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
