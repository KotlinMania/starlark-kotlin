// port-lint: source src/docs/multipage.rs
package io.github.kotlinmania.starlark.docs

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

import io.github.kotlinmania.starlark.docs.markdown.LayoutRenderConfig
import io.github.kotlinmania.starlark.docs.markdown.renderMarkdownPageForMultipageRender
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.typing.TypeRenderConfig

class RenderConfig(
    val typeConfig: TypeRenderConfig,
    val layoutConfig: LayoutRenderConfig,
)

class DocModuleInfo(
    val module: DocModule,
    val name: String,
    /** A prefix to attach to all of the pages rendered from this module. */
    val pagePath: String,
) {
    // impl DocModuleInfo

    internal fun intoPageRenders(): List<PageRender> = traverseInner(module, name, pagePath)

    companion object {
        private fun traverseInner(
            docs: DocModule,
            moduleName: String,
            basePath: String,
        ): List<PageRender> {
            val result = mutableListOf<PageRender>()

            result.add(
                PageRender(
                    page = DocPageRef.Module(docs),
                    path = basePath,
                    name = moduleName,
                    ty = null,
                ),
            )

            for ((memberName, doc) in docs.members.iter()) {
                val path =
                    if (basePath.isEmpty()) {
                        memberName
                    } else {
                        "$basePath/$memberName"
                    }
                when (doc) {
                    is DocItem.Module -> {
                        result.addAll(traverseInner(doc.module, memberName, path))
                    }
                    is DocItem.Type -> {
                        result.add(
                            PageRender(
                                page = DocPageRef.Type(doc.type),
                                path = path,
                                name = memberName,
                                ty = doc.type.ty,
                            ),
                        )
                    }
                    is DocItem.Member -> {
                        // No page generated for plain members.
                    }
                }
            }

            return result
        }
    }
}

/**
 * A reference to a page to render.
 * DocsRender will have all the PageRender it needs to render the docs.
 * Since types and some modules are owned by other modules, we need to use the reference here.
 */
internal sealed class DocPageRef {
    // Module(&'a DocModule)
    class Module(
        val module: DocModule,
    ) : DocPageRef()

    // Type(&'a DocType)
    class Type(
        val type: DocType,
    ) : DocPageRef()
}

/** A single page to render. */
internal class PageRender(
    val page: DocPageRef,
    val path: String,
    val name: String,
    /** The type of the page, if it is a type page. This is used to get the link to the type. */
    val ty: Ty?,
) {
    // impl PageRender

    fun renderMarkdown(renderConfig: RenderConfig): String =
        when (page) {
            is DocPageRef.Module -> {
                page.module.renderMarkdownPageForMultipageRender(name, renderConfig)
            }
            is DocPageRef.Type -> {
                page.type.renderMarkdownPageForMultipageRender(name, renderConfig)
            }
        }
}

/**
 * Renders the contents into a multi-page tree structure.
 *
 * The output will contain page-paths like `""`, `"type1"`, `"mod1"`, and `"mod1/type2"`,
 * each mapped to the contents of that page. That means that some of the paths may be prefixes
 * of each other, which will need consideration if this is to be materialized to a filesystem.
 */
internal class MultipageRender(
    private val pageRenders: List<PageRender>,
    private val renderConfig: RenderConfig,
) {
    // impl MultipageRender

    companion object {
        /**
         * Create a new MultipageRender from a list of DocModuleInfo, and an optional function
         * to map a type path to a linkable path.
         * If the function is not provided, the type will not be linkable.
         * linkedTyMapper is used to map the **type path** and **type name** to a linkable element
         * in the markdown.
         */
        fun new(
            docs: List<DocModuleInfo>,
            linkedTyMapper: ((String, String) -> String)?,
            renderSignatureAtBottom: Boolean,
        ): MultipageRender {
            val res = mutableListOf<PageRender>()

            for (doc in docs) {
                res.addAll(doc.intoPageRenders())
            }

            var typeRenderConfig: TypeRenderConfig = TypeRenderConfig.Default
            if (linkedTyMapper != null) {
                val tyToPathMap = mutableMapOf<Ty, String>()
                for (page in res) {
                    val ty = page.ty
                    if (ty != null) {
                        tyToPathMap[ty] = page.path
                    }
                }

                val renderLinkedTyStarlarkValue = { ty: TyStarlarkValue ->
                    val typeName = ty.toString()
                    val tyKey = Ty.basic(TyBasic.StarlarkValue(ty))
                    val typePath = tyToPathMap[tyKey]
                    if (typePath != null) {
                        linkedTyMapper(typePath, typeName)
                    } else {
                        typeName
                    }
                }

                typeRenderConfig =
                    TypeRenderConfig.LinkedType(
                        renderLinkedTyStarlarkValue = renderLinkedTyStarlarkValue,
                    )
            }

            return MultipageRender(
                pageRenders = res,
                renderConfig =
                    RenderConfig(
                        typeConfig = typeRenderConfig,
                        layoutConfig =
                            if (renderSignatureAtBottom) {
                                LayoutRenderConfig.SignatureAtBottom
                            } else {
                                LayoutRenderConfig.Default
                            },
                    ),
            )
        }
    }

    /** Render the docs into a map of markdown paths to markdown content. */
    fun renderMarkdownPages(): Map<String, String> =
        pageRenders.associate { page ->
            page.path to page.renderMarkdown(renderConfig)
        }
}

/**
 * Renders the contents into a multi-page tree structure.
 *
 * The output will contain page-paths like `""`, `"type1"`, `"mod1"`, and `"mod1/type2"`,
 * each mapped to the contents of that page. That means that some of the paths may be prefixes
 * of each other, which will need consideration if this is to be materialized to a filesystem.
 *
 * It accepts a list of [DocModuleInfo], and an optional function [linkedTyMapper].
 * [linkedTyMapper] is used to map the **type path** and **type name** to a linkable element
 * in the markdown.
 */
fun renderMarkdownMultipage(
    modulesInfos: List<DocModuleInfo>,
    linkedTyMapper: ((String, String) -> String)?,
    renderSignatureAtBottom: Boolean,
): Map<String, String> {
    val multipageRender = MultipageRender.new(modulesInfos, linkedTyMapper, renderSignatureAtBottom)
    return multipageRender.renderMarkdownPages()
}
