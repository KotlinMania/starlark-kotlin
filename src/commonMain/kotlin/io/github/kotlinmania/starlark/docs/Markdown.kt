// port-lint: source src/docs/markdown.rs
package io.github.kotlinmania.starlark.docs.markdown

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.docs.DocFunction
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocModule
import io.github.kotlinmania.starlark.docs.DocParam
import io.github.kotlinmania.starlark.docs.DocProperty
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocType
import io.github.kotlinmania.starlark.docs.RenderConfig
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TypeRenderConfig
import io.github.kotlinmania.starlark.docs.renderCode

/** Configuration for layout rendering. */
enum class LayoutRenderConfig {
    Default,
    /** Renders the summary + detail above function signature. */
    SignatureAtBottom,
}

/** What to render from a [DocString]. */
private enum class DSOpts {
    /** Just the summary. */
    Summary,
    /** Just the details (if present). */
    Details,
    /** Just the examples section (if present). */
    Examples,
    /** Both the summary, details, and examples separated in an appropriate fashion. */
    Combined,
}

private fun renderDocString(opts: DSOpts, string: DocString?): String? {
    val d = string ?: return null
    return when (opts) {
        DSOpts.Summary -> d.summary
        DSOpts.Details -> d.details
        DSOpts.Examples -> d.examples
        DSOpts.Combined -> {
            val details = d.details
            val examples = d.examples
            when {
                details != null && examples != null ->
                    "${d.summary}\n\n$details\n\nExamples:\n$examples"
                details != null ->
                    "${d.summary}\n\n$details"
                examples != null ->
                    "${d.summary}\n\nExamples:\n$examples"
                else -> d.summary
            }
        }
    }
}

/**
 * Function names can have underscores in them, which are markdown,
 * so escape them if we render them outside a codeblock.
 */
private fun escapeName(name: String): String {
    return name.replace("_", "\\_")
}

private fun renderProperty(name: String, property: DocProperty, renderConfig: RenderConfig): String {
    val prototype = renderCodeBlock(
        "$name: ${property.typ.displayWith(renderConfig.typeConfig)}",
        renderConfig.typeConfig,
    )
    val header = "## ${escapeName(name)}\n\n$prototype"
    val summary = renderDocString(DSOpts.Summary, property.docs)
    val details = renderDocString(DSOpts.Details, property.docs)

    return buildString {
        append(header)
        if (summary != null) {
            append("\n\n")
            append(summary)
        }
        if (details != null) {
            append("\n\n")
            append(details)
        }
    }
}

/** If there are any parameter docs to render, render them as a list. */
private fun renderFunctionParameters(params: Iterable<Pair<String, DocParam>>): String? {
    var paramList: StringBuilder? = null

    for ((name, p) in params) {
        if (p.docs == null) {
            continue
        }

        if (paramList == null) {
            paramList = StringBuilder()
        }

        val docs = renderDocString(DSOpts.Combined, p.docs) ?: ""

        val linesIter = docs.lines().iterator()
        if (linesIter.hasNext()) {
            val firstLine = linesIter.next()
            val default = when (val v = p.defaultValue) {
                null -> " (required)"
                else -> " (defaults to: `$v`)"
            }

            paramList.appendLine("* `$name`:$default\n")
            paramList.appendLine("  $firstLine\n")
            while (linesIter.hasNext()) {
                paramList.appendLine("  ${linesIter.next()}")
            }
        } else {
            paramList.appendLine("* `$name`")
        }
    }

    return paramList?.toString()
}

private fun renderFunction(
    name: String,
    function: DocFunction,
    includeHeader: Boolean,
    renderConfig: RenderConfig,
): String {
    // Render the layouts differently based on the configs provided.
    return when (renderConfig.layoutConfig) {
        LayoutRenderConfig.SignatureAtBottom ->
            renderSignatureAtBottomLayout(name, function, renderConfig)
        LayoutRenderConfig.Default ->
            renderDefaultLayout(name, function, includeHeader, renderConfig)
    }
}

private fun renderDefaultLayout(
    name: String,
    function: DocFunction,
    includeHeader: Boolean,
    renderConfig: RenderConfig,
): String {
    val prototype = renderCodeBlock(
        renderFunctionPrototype(name, function, renderConfig.typeConfig),
        renderConfig.typeConfig,
    )
    val header = if (includeHeader) {
        "## ${escapeName(name)}\n\n$prototype"
    } else {
        prototype
    }

    val summary = renderDocString(DSOpts.Summary, function.docs)
    val details = renderDocString(DSOpts.Details, function.docs)
    val examples = renderDocString(DSOpts.Examples, function.docs)

    val parameterDocs = renderFunctionParameters(function.params.docParamsWithStarredNames().asIterable())
    val returnDocs = renderDocString(DSOpts.Combined, function.ret.docs)

    return buildString {
        append(header)
        if (summary != null) {
            append("\n\n")
            append(summary)
        }
        if (parameterDocs != null) {
            append("\n\n#### Parameters\n\n")
            append(parameterDocs)
        }
        if (returnDocs != null) {
            append("\n\n#### Returns\n\n")
            append(returnDocs)
        }
        if (details != null) {
            if (parameterDocs != null || returnDocs != null) {
                append("\n\n#### Details\n\n")
            } else {
                // No need to aggressively separate the defaults from the summary if there
                // was nothing in between them. Just allow them to flow together.
                append("\n\n")
            }
            append(details)
        }
        if (examples != null) {
            append("\n\n#### Examples\n\n")
            append(examples)
        }
    }
}

private fun renderSignatureAtBottomLayout(
    name: String,
    function: DocFunction,
    renderConfig: RenderConfig,
): String {
    val prototype = renderCodeBlock(
        renderFunctionPrototype(name, function, renderConfig.typeConfig),
        renderConfig.typeConfig,
    )

    val summary = renderDocString(DSOpts.Summary, function.docs)
    val details = renderDocString(DSOpts.Details, function.docs)
    val examples = renderDocString(DSOpts.Examples, function.docs)

    val parameterDocs = renderFunctionParameters(function.params.docParamsWithStarredNames().asIterable())

    return buildString {
        if (summary != null) {
            append(summary)
        }
        if (details != null) {
            append("\n\n### Details\n\n")
            append(details)
        }
        append("\n\n")
        append("### Function Signature\n\n$prototype")
        if (parameterDocs != null) {
            append("\n\n### Parameters\n\n")
            append(parameterDocs)
        }
        if (examples != null) {
            append("\n\n### Examples\n\n")
            append(renderStringsWithCodeBlocks(examples, renderConfig.typeConfig))
        }
    }
}

internal fun renderMembers(
    name: String,
    docs: DocString?,
    prefix: String,
    members: Iterable<Pair<String, DocMember>>,
    afterSummary: String?,
    renderConfig: RenderConfig,
): String {
    val summary = renderDocString(DSOpts.Combined, docs)
        ?.let { "\n\n$it" } ?: ""

    val sortedMembers = members.sortedBy { it.first }
    val memberDetails = sortedMembers.map { (child, member) ->
        renderDocMember("$prefix$child", member, renderConfig)
    }
    val allParts = buildList {
        if (afterSummary != null) add(afterSummary)
        addAll(memberDetails)
    }
    val membersDetails = allParts.joinToString("\n\n---\n\n")

    val header = if (name.isEmpty()) "" else "# $name"

    return "$header$summary\n\n$membersDetails"
}

internal fun renderDocType(
    name: String,
    prefix: String,
    t: DocType,
    renderConfig: RenderConfig,
): String {
    val constructor = t.constructor?.let { renderFunction(name, it, false, renderConfig) }
    return renderMembers(
        name,
        t.docs,
        prefix,
        t.members.iter().map { (n, m) -> Pair(n, m) }.asIterable(),
        constructor,
        renderConfig,
    )
}

/**
 * Used by LSP.
 * It will not render the type signatures with link to types.
 */
fun renderDocItemNoLink(name: String, item: DocItem): String {
    return renderDocItem(
        name,
        item,
        RenderConfig(
            typeConfig = TypeRenderConfig.Default,
            layoutConfig = LayoutRenderConfig.Default,
        ),
    )
}

fun renderDocItem(name: String, item: DocItem, renderConfig: RenderConfig): String {
    return when (item) {
        is DocItem.Module -> renderMembers(
            name,
            item.module.docs,
            "",
            item.module.members.iter().mapNotNull { (n, m) ->
                m.tryAsMemberWithCollapsedObject().getOrNull()?.let { Pair(n, it) }
            }.asIterable(),
            null,
            renderConfig,
        )
        is DocItem.Type -> renderDocType(
            "`$name` type",
            "$name.",
            item.type,
            renderConfig,
        )
        is DocItem.Member -> when (val member = item.member) {
            is DocMember.Function -> renderFunction(name, member.function, true, renderConfig)
            is DocMember.Property -> renderProperty(name, member.property, renderConfig)
        }
    }
}

/** Used by LSP. */
fun renderDocMember(name: String, item: DocMember, renderConfig: RenderConfig): String {
    return when (item) {
        is DocMember.Function -> renderFunction(name, item.function, true, renderConfig)
        is DocMember.Property -> renderProperty(name, item.property, renderConfig)
    }
}

/** Used by LSP. */
fun renderDocParam(starredName: String, item: DocParam): String {
    return renderFunctionParameters(listOf(Pair(starredName, item))) ?: ""
}

/**
 * Any functions with more parameters than this will have
 * their prototype split over multiple lines. Otherwise, it is returned as
 * a single line.
 */
private const val MAX_ARGS_BEFORE_MULTILINE = 3

/** If the prototype ends up longer than this length, we'll split it anyway. */
private const val MAX_LENGTH_BEFORE_MULTILINE = 80

private fun rawTypePrefix(prefix: String, t: Ty, renderConfig: TypeRenderConfig): String {
    return if (t.isAny()) {
        ""
    } else {
        "$prefix${t.displayWith(renderConfig)}"
    }
}

private fun renderFunctionPrototype(
    functionName: String,
    f: DocFunction,
    renderConfig: TypeRenderConfig,
): String {
    val retType = rawTypePrefix(" -> ", f.ret.typ, renderConfig)
    val prefix = "def $functionName"
    val oneLineParams = f.params.renderCode(null, renderConfig)
    val singleLineResult = "$prefix($oneLineParams)$retType"

    return if (f.params.docParams().count() > MAX_ARGS_BEFORE_MULTILINE
        || singleLineResult.length > MAX_LENGTH_BEFORE_MULTILINE
    ) {
        val chunkedParams = f.params.renderCode("    ", renderConfig)
        "$prefix(\n$chunkedParams)$retType"
    } else {
        singleLineResult
    }
}

private val CODE_BLOCK_RE = Regex("""```([\s\S]*?)```""")

private fun renderStringsWithCodeBlocks(contents: String, renderConfig: TypeRenderConfig): String {
    return CODE_BLOCK_RE.replace(contents) { matchResult ->
        renderCodeBlock(matchResult.groupValues[1], renderConfig)
    }
}

// For LinkedType render in markdown, for code block ``` ``` we cannot contain the link in it.
// We need to import the html block here.
private fun renderCodeBlock(contents: String, renderConfig: TypeRenderConfig): String {
    return when (renderConfig) {
        is TypeRenderConfig.Default -> "```python\n$contents\n```"
        is TypeRenderConfig.LinkedType ->
            """<pre class="language-python"><code>$contents</code></pre>"""
    }
}

fun DocModule.renderMarkdownPageForMultipageRender(
    name: String,
    renderConfig: RenderConfig,
): String {
    return renderMembers(
        name,
        this.docs,
        "",
        this.members.iter().mapNotNull { (n, m) -> m.tryAsMember()?.let { Pair(n, it) } }.asIterable(),
        null,
        renderConfig,
    )
}

fun DocType.renderMarkdownPageForMultipageRender(
    name: String,
    renderConfig: RenderConfig,
): String {
    return renderDocType(name, "$name.", this, renderConfig)
}
