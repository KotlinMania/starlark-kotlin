// port-lint: source src/__derive_refs/param_spec.rs
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

import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.callable_param.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.typing.macro_support.unpackArgsItemTy
import io.github.kotlinmania.starlark_kotlin.typing.macro_support.unpackKwargsValueTy
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue

// pub enum NativeCallableParamDefaultValue
sealed class NativeCallableParamDefaultValue {
    /** Value is used for documentation only, not when the function is called. */
    data class Value(val value: FrozenValue) : NativeCallableParamDefaultValue()
    data object Optional : NativeCallableParamDefaultValue()
}

// pub struct NativeCallableParam
class NativeCallableParam(
    val name: String,
    /**
     * Type of the parameter.
     * For `*args` is the type of the element, and for `**kwargs` is the type of the value.
     */
    val ty: Ty,
    /** `null` means the parameter is required. */
    val required: NativeCallableParamDefaultValue?,
) {
    // impl NativeCallableParam

    // pub fn args(name: &'static str, param_ty: Ty) -> NativeCallableParam
    // pub fn kwargs(name: &'static str, param_ty: Ty) -> NativeCallableParam
    // fn is_required(&self) -> ParamIsRequired

    private fun isRequired(): ParamIsRequired {
        return when (required) {
            null -> ParamIsRequired.Yes
            else -> ParamIsRequired.No
        }
    }

    companion object {
        fun args(name: String, paramTy: Ty): NativeCallableParam {
            return NativeCallableParam(
                name = name,
                ty = unpackArgsItemTy(paramTy),
                required = null,
            )
        }

        fun kwargs(name: String, paramTy: Ty): NativeCallableParam {
            return NativeCallableParam(
                name = name,
                ty = unpackKwargsValueTy(paramTy),
                required = null,
            )
        }
    }
}

// pub struct NativeCallableParamSpec
class NativeCallableParamSpec(
    val posOnly: List<NativeCallableParam>,
    val posOrNamed: List<NativeCallableParam>,
    val args: NativeCallableParam?,
    val namedOnly: List<NativeCallableParam>,
    val kwargs: NativeCallableParam?,
) {
    // impl NativeCallableParamSpec

    /** For a function accepting raw `&Arguments`. */
    // pub fn for_arguments() -> NativeCallableParamSpec
    companion object {
        fun forArguments(): NativeCallableParamSpec {
            return NativeCallableParamSpec(
                posOnly = emptyList(),
                posOrNamed = emptyList(),
                args = NativeCallableParam.args("args", Ty.any()),
                namedOnly = emptyList(),
                kwargs = NativeCallableParam.kwargs("kwargs", Ty.any()),
            )
        }
    }

    // pub(crate) fn param_spec(&self) -> ParamSpec
    internal fun paramSpec(): ParamSpec {
        return ParamSpec.newParts(
            posOnly = posOnly.map { p -> Pair(p.isRequired(), p.ty) },
            posOrNamed = posOrNamed.map { p -> Triple(ArcStr.newStatic(p.name), p.isRequired(), p.ty) },
            args = args?.ty,
            namedOnly = namedOnly.map { p -> Triple(ArcStr.newStatic(p.name), p.isRequired(), p.ty) },
            kwargs = kwargs?.ty,
        )
    }
}
