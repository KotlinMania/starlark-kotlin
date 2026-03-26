// port-lint: source src/__derive_refs/components.rs
package io.github.kotlinmania.starlark_kotlin.__derive_refs.components

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

import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableParam
import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableParamDefaultValue
import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableParamSpec
import io.github.kotlinmania.starlark_kotlin.docs.DocFunction
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocParam
import io.github.kotlinmania.starlark_kotlin.docs.DocParams
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.display.PARAM_FMT_OPTIONAL
import io.github.kotlinmania.starlark_kotlin.typing.Ty

/** A wrapper for the parameters to `GlobalsBuilder.setFunction` and `MethodBuilder.setMethod`. */
// pub struct NativeCallableComponents
class NativeCallableComponents(
    val speculativeExecSafe: Boolean,
    val rustDocstring: String?,
    val paramSpec: NativeCallableParamSpec,
    val returnType: Ty,
) {
    // impl NativeCallableComponents

    // fn doc_params(&self) -> DocParams
    private fun docParams(): DocParams {
        fun docParam(p: NativeCallableParam): DocParam {
            return DocParam(
                name = p.name,
                docs = null,
                typ = p.ty,
                defaultValue = when (val req = p.required) {
                    null -> null
                    is NativeCallableParamDefaultValue.Optional -> PARAM_FMT_OPTIONAL
                    is NativeCallableParamDefaultValue.Value -> req.value.toValue().toRepr()
                },
            )
        }

        return DocParams(
            posOnly = paramSpec.posOnly.map { docParam(it) },
            posOrNamed = paramSpec.posOrNamed.map { docParam(it) },
            args = paramSpec.args?.let { docParam(it) },
            namedOnly = paramSpec.namedOnly.map { docParam(it) },
            kwargs = paramSpec.kwargs?.let { docParam(it) },
        )
    }

    // pub(crate) fn into_docs(self, as_type: Option<(Ty, DocType)>) -> DocItem
    internal fun intoDocs(asType: Pair<Ty, DocType>?): DocItem {
        val funcDocs = DocFunction.fromDocstring(
            DocStringKind.RUST,
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
