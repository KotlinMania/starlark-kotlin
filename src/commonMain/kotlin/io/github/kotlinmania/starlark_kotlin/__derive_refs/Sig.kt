// port-lint: source src/__derive_refs/sig.rs
package io.github.kotlinmania.starlark_kotlin.__derive_refs

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

import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue

// pub enum NativeSigArg
sealed class NativeSigArg {
    data class Required(val name: String) : NativeSigArg()
    data class Optional(val name: String) : NativeSigArg()
    data class Defaulted(val name: String, val value: FrozenValue) : NativeSigArg()

    // impl NativeSigArg

    // fn param(&self) -> (&str, ParametersSpecParam<FrozenValue>)
    internal fun param(): Pair<String, ParametersSpecParam<FrozenValue>> {
        return when (this) {
            is Required -> Pair(name, ParametersSpecParam.Required)
            is Optional -> Pair(name, ParametersSpecParam.Optional)
            is Defaulted -> Pair(name, ParametersSpecParam.Defaulted(value))
        }
    }
}

// pub fn parameter_spec(...)
fun parameterSpec(
    name: String,
    posOnly: List<NativeSigArg>,
    posOrNamed: List<NativeSigArg>,
    args: Boolean,
    namedOnly: List<NativeSigArg>,
    kwargs: Boolean,
): ParametersSpec<FrozenValue> {
    return ParametersSpec.newParts(
        name,
        posOnly.map { it.param() },
        posOrNamed.map { it.param() },
        args,
        namedOnly.map { it.param() },
        kwargs,
    )
}

/** [ParametersSpec] for a function which accepts `&Arguments`. */
// pub fn parameter_spec_for_arguments(name: &'static str) -> ParametersSpec<FrozenValue>
fun parameterSpecForArguments(name: String): ParametersSpec<FrozenValue> {
    return parameterSpec(name, emptyList(), emptyList(), true, emptyList(), true)
}
