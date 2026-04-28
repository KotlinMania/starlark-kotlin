// port-lint: source __derive_refs/param_spec.rs
package io.github.kotlinmania.starlark.deriverefs

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
import io.github.kotlinmania.starlark.typing.ParamIsRequired
import io.github.kotlinmania.starlark.util.ArcStr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.typing.unpackKwargsValueTy
import io.github.kotlinmania.starlark.typing.unpackArgsItemTy

sealed class NativeCallableParamDefaultValue {
    /** Value is used for documentation only, not when the function is called. */
    data class Value(val value: FrozenValue) : NativeCallableParamDefaultValue()
    data object Optional : NativeCallableParamDefaultValue()
}

// Type of `ty`:
// for `*args` is the type of the element, and for `**kwargs` is the type of the value.
// `required == null` means the parameter is required.
class NativeCallableParam(
    name: String,
    ty: Ty,
    required: NativeCallableParamDefaultValue?,
) {
    val name: String = name
    val ty: Ty = ty
    val required: NativeCallableParamDefaultValue? = required

    internal fun isRequired(): ParamIsRequired {
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

class NativeCallableParamSpec(
    val posOnly: List<NativeCallableParam>,
    val posOrNamed: List<NativeCallableParam>,
    val args: NativeCallableParam?,
    val namedOnly: List<NativeCallableParam>,
    val kwargs: NativeCallableParam?,
) {

    /** For a function accepting raw `&Arguments`. */
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

    internal fun paramSpec(): ParamSpec {
        return ParamSpec.newParts(
            posOnly = posOnly.map { p -> Pair(p.isRequired(), p.ty) },
            posOrName = posOrNamed.map { p -> Triple(p.name, p.isRequired(), p.ty) },
            args = args?.ty,
            namedOnly = namedOnly.map { p -> Triple(p.name, p.isRequired(), p.ty) },
            kwargs = kwargs?.ty,
        )
    }
}
