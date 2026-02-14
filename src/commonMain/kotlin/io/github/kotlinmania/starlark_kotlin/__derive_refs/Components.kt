// port-lint: source src/__derive_refs/components.rs
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

import io.github.kotlinmania.starlark_kotlin.__derive_refs.param_spec.NativeCallableParam
import io.github.kotlinmania.starlark_kotlin.__derive_refs.param_spec.NativeCallableParamDefaultValue
import io.github.kotlinmania.starlark_kotlin.__derive_refs.param_spec.NativeCallableParamSpec
import io.github.kotlinmania.starlark_kotlin.docs.DocFunction
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocParam
import io.github.kotlinmania.starlark_kotlin.docs.DocParams
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.display.PARAM_FMT_OPTIONAL
import io.github.kotlinmania.starlark_kotlin.typing.Ty

/// A wrapper for the parameters to `GlobalsBuilder::set_function` and `MethodBuilder::set_method`
// pub struct NativeCallableComponents
class NativeCallableComponents(
    val speculativeExecSafe: Boolean,
    val rustDocstring: String?,
    val paramSpec: NativeCallableParamSpec,
    val returnType: Ty,
) {
    // fn doc_params(&self) -> DocParams
    private fun docParams(): DocParams {
        // fn doc_param(p: &NativeCallableParam) -> DocParam
        fun docParam(p: NativeCallableParam): DocParam {
            val (name, ty, required) = p
            return DocParam(
                name = name,
                docs = null,
                typ = ty,
                defaultValue = when (required) {
                    null -> null
                    is NativeCallableParamDefaultValue.Optional -> PARAM_FMT_OPTIONAL
                    is NativeCallableParamDefaultValue.Value -> required.v.toValue().toRepr()
                },
            )
        }

        return DocParams(
            posOnly = paramSpec.posOnly.map(::docParam),
            posOrNamed = paramSpec.posOrNamed.map(::docParam),
            args = paramSpec.args?.let(::docParam),
            namedOnly = paramSpec.namedOnly.map(::docParam),
            kwargs = paramSpec.kwargs?.let(::docParam),
        )
    }

    // pub(crate) fn into_docs(self, as_type: Option<(Ty, DocType)>) -> DocItem
    internal fun intoDocs(asType: Pair<Ty, DocType>?): DocItem {
        val funcDocs = DocFunction.fromDocstring(
            DocStringKind.Rust,
            docParams(),
            returnType,
            rustDocstring,
        )
        return when (asType) {
            null -> DocItem.Member(DocMember.Function(funcDocs))
            else -> {
                val (_, tyDocs) = asType
                DocItem.Type(tyDocs.copy(constructor = funcDocs))
            }
        }
    }
}
