// port-lint: source src/values/types/known_methods.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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

import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.eval.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.dict.value.dictMethods
import io.github.kotlinmania.starlark_kotlin.values.list.value.listMethods
import io.github.kotlinmania.starlark_kotlin.values.set.value.setMethods
import io.github.kotlinmania.starlark_kotlin.values.string.str_type.strMethods
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMeth
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMethod

/// Method and a `Methods` container which declares it.
// #[derive(Clone, Copy, Dupe)]
// pub(crate) struct KnownMethod
internal class KnownMethod(
    /// An object where the method is defined.
    val typeMethods: Methods,
    /// The method.
    val method: FrozenValueTyped<NativeMethod>,
    /// Copied here from `method` to faster invocation (one fewer deref).
    val imp: NativeMeth,
) {
    // pub(crate) fn to_value<'v>(&self) -> Value<'v>
    fun toValue(): Value {
        return method.toValue()
    }

    // pub(crate) fn invoke_method<'v>(&self, this: Value<'v>, args: &Arguments<'v, '_>, eval: &mut Evaluator<'v, '_, '_>) -> crate::Result<Value<'v>>
    fun invokeMethod(thisValue: Value, args: Arguments, eval: Evaluator): Result<Value> {
        return imp.invoke(eval, thisValue, args)
    }
}

/// Some of stdlib methods.
// struct KnownMethods
private class KnownMethods(
    val methods: Map<String, KnownMethod>,
) {
    companion object {
        // fn build() -> KnownMethods
        fun build(): KnownMethods {
            val methods = mutableMapOf<String, KnownMethod>()

            // fn add_methods(...)
            fun addMethods(
                methods: MutableMap<String, KnownMethod>,
                typeMethods: Methods?,
            ) {
                val tm = typeMethods!!
                var hasAtLeastOneMethod = false
                for ((name, member) in tm.members()) {
                    // Take methods, ignore attributes.
                    val method = FrozenValueTyped.new<NativeMethod>(member)
                    if (method != null) {
                        // First wins, e. g. `list.clear` is hit, and `dict.clear` is miss.
                        methods.putIfAbsent(name, KnownMethod(
                            typeMethods = tm,
                            method = method,
                            imp = method.asRef().function,
                        ))
                        hasAtLeastOneMethod = true
                    }
                }
                // Sanity check.
                check(hasAtLeastOneMethod)
            }

            // We don't need to add all the methods, only the most common ones. This is fine.
            addMethods(methods, listMethods())
            addMethods(methods, dictMethods())
            addMethods(methods, setMethods())
            addMethods(methods, strMethods())

            return KnownMethods(methods)
        }
    }
}

/// Get stdlib method by name, or `None` if method is not found
/// or method is not very common. Return arbitrary method if more than one
/// method is found (e. g. `list.clear` and `dict.clear`).
// pub(crate) fn get_known_method(name: &str) -> Option<KnownMethod>
// static ANY_METHODS: Lazy<KnownMethods> = Lazy::new(KnownMethods::build)
private val anyMethods: KnownMethods by lazy { KnownMethods.build() }

internal fun getKnownMethod(name: String): KnownMethod? {
    return anyMethods.methods[name]
}
