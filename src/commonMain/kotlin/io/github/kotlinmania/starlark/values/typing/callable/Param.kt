// port-lint: source src/values/typing/callable/param.rs
package io.github.kotlinmania.starlark.values.typing.callable

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

import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty

/**
 * Type parameter for [`StarlarkCallable`] or [`FrozenStarlarkCallable`]
 * describing the expected parameters of the callable.
 */
interface StarlarkCallableParamSpec {
    /** Get the parameter specification for the callable. */
    fun params(): ParamSpec
}

/** Indicates that a callable accepts any number of positional and keyword arguments. */

/** `*args` and `**kwargs` parameters. */
// impl StarlarkCallableParamSpec for StarlarkCallableParamAny
object StarlarkCallableParamAny : StarlarkCallableParamSpec {
    override fun params(): ParamSpec = ParamSpec.any()
}

/** No parameters. */
// impl StarlarkCallableParamSpec for ()
object StarlarkCallableParamSpecNone : StarlarkCallableParamSpec {
    override fun params(): ParamSpec = ParamSpec.posOnly(emptyList(), emptyList())
}

/** Single positional-only parameter. */
// impl<A: StarlarkTypeRepr> StarlarkCallableParamSpec for (A,)
class StarlarkCallableParamSpec1(
    private val a: Ty,
) : StarlarkCallableParamSpec {
    override fun params(): ParamSpec = ParamSpec.posOnly(listOf(a), emptyList())
}

/** Two positional-only parameters. */
// impl<A: StarlarkTypeRepr, B: StarlarkTypeRepr> StarlarkCallableParamSpec for (A, B)
class StarlarkCallableParamSpec2(
    private val a: Ty,
    private val b: Ty,
) : StarlarkCallableParamSpec {
    override fun params(): ParamSpec = ParamSpec.posOnly(listOf(a, b), emptyList())
}

/** Three positional-only parameters. */
// impl<A, B, C: StarlarkTypeRepr> StarlarkCallableParamSpec for (A, B, C)
class StarlarkCallableParamSpec3(
    private val a: Ty,
    private val b: Ty,
    private val c: Ty,
) : StarlarkCallableParamSpec {
    override fun params(): ParamSpec = ParamSpec.posOnly(listOf(a, b, c), emptyList())
}

/** Four positional-only parameters. */
// impl<A, B, C, D: StarlarkTypeRepr> StarlarkCallableParamSpec for (A, B, C, D)
class StarlarkCallableParamSpec4(
    private val a: Ty,
    private val b: Ty,
    private val c: Ty,
    private val d: Ty,
) : StarlarkCallableParamSpec {
    override fun params(): ParamSpec = ParamSpec.posOnly(listOf(a, b, c, d), emptyList())
}
