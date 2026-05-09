// port-lint: source eval/params.rs
package io.github.kotlinmania.starlark.eval

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec

/**
 * Build both [ParametersSpec] (for parsing) and [ParamSpec] (for typechecking)
 * from a list of parameters.
 */
fun <V> paramSpecs(
    functionName: String,
    posOnly: List<Triple<String, ParametersSpecParam<V>, Ty>>,
    posOrNamed: List<Triple<String, ParametersSpecParam<V>, Ty>>,
    args: Ty?,
    namedOnly: List<Triple<String, ParametersSpecParam<V>, Ty>>,
    kwargs: Ty?,
): Pair<ParametersSpec<V>, ParamSpec> {
    val parametersSpec = ParametersSpec.newParts(
        functionName,
        posOnly.map { (name, param, _) -> Pair(name, param) },
        posOrNamed.map { (name, param, _) -> Pair(name, param) },
        args != null,
        namedOnly.map { (name, param, _) -> Pair(name, param) },
        kwargs != null,
    )

    val paramSpec = ParamSpec.newParts(
        posOnly.map { (_, param, ty) -> Pair(param.isRequired(), ty) },
        posOrNamed.map { (name, param, ty) -> Triple(name, param.isRequired(), ty) },
        args,
        namedOnly.map { (name, param, ty) -> Triple(name, param.isRequired(), ty) },
        kwargs,
    )

    return Pair(parametersSpec, paramSpec)
}
