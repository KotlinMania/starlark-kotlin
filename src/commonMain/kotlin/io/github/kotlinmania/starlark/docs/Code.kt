// port-lint: source src/docs/code.rs
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

/** Render documentation items as Starlark code. */

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TypeRenderConfig

/**
 * There have been bugs around line endings in the textwrap crate. Just join
 * into a single string, and trim the line endings.
 */
// fn wrap_trimmed(s: &str, width: usize) -> String
private fun wrapTrimmed(s: String, width: Int): String {
    // Simple word-wrap implementation (textwrap equivalent).
    val words = s.split(' ')
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()
    for (word in words) {
        if (currentLine.isNotEmpty() && currentLine.length + 1 + word.length > width) {
            lines.add(currentLine.toString())
            currentLine = StringBuilder(word)
        } else {
            if (currentLine.isNotEmpty()) currentLine.append(' ')
            currentLine.append(word)
        }
    }
    if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
    return lines.joinToString("\n").trimEnd()
}

/** There have been bugs around line endings in the textwrap crate. Just trim the line endings. */
// fn indent_trimmed(s: &str, prefix: &str) -> String
private fun indentTrimmed(s: String, prefix: String): String {
    return s.lines().joinToString("\n") { line ->
        if (line.isBlank()) line else "$prefix$line"
    }.trimEnd()
}

// impl DocString { fn render_as_code }

/** Render this docstring as a "starlark" docstring. */
// fn render_as_code(&self) -> String
fun DocString.renderAsCode(): String {
    val s = when (val d = this.details) {
        null -> this.summary
        else -> "${this.summary}\n\n$d"
    }
    return wrapTrimmed(s, 80)
}

/**
 * Render the docstring as in `render_as_code`, but surround it in triple quotes,
 * a common convention in starlark docstrings.
 */
// fn render_as_quoted_code(&self) -> String
fun DocString.renderAsQuotedCode(): String {
    return "\"\"\"\n${renderAsCode()}\n\"\"\""
}

// impl DocModule { pub fn render_as_code }

// pub fn render_as_code(&self) -> String
fun DocModule.renderAsCode(): String {
    var res = docs?.renderAsQuotedCode() ?: ""
    for ((k, v) in members) {
        val member = v.tryAsMemberWithCollapsedObject().getOrNull() ?: continue
        res += "\n"
        res += when (member) {
            is DocMember.Property -> member.property.renderAsCode(k)
            is DocMember.Function -> member.function.renderAsCode(k)
        }
        res += "\n"
    }
    return res
}

// impl DocFunction

// fn starlark_docstring(&self) -> Option<String>
private fun DocFunction.starlarkDocstring(): String? {
    var docs = ""
    val mainDocs = this.docs?.renderAsCode()
    if (mainDocs != null) {
        docs += mainDocs
    }

    val argsIndentationCount = this.params.docParams()
        .map { it.name.length + 2 }
        .maxOrNull() ?: 0
    val argsIndentation = " ".repeat(argsIndentationCount)

    val argsDocs = this.params.docParams()
        .mapNotNull { it.starlarkDocstring(argsIndentation) }
        .joinToString("\n")
    if (argsDocs.isNotEmpty()) {
        val indented = indentTrimmed(argsDocs, "    ")
        docs += "\n\nArgs:\n$indented"
    }

    val retDocs = this.ret.starlarkDocstring()
    if (retDocs != null) {
        val indented = indentTrimmed(retDocs, "    ")
        docs += "\n\nRet:\n$indented"
    }

    return if (docs.isEmpty()) {
        null
    } else {
        indentTrimmed(
            "\"\"\"\n${docs.trimStart()}\n\"\"\"",
            "    ",
        )
    }
}

// pub fn render_as_code(&self, name: &str) -> String
fun DocFunction.renderAsCode(name: String): String {
    val paramsOneLine = this.params.renderCode(null, TypeRenderConfig.Default)

    val params = if (paramsOneLine.length > 60) {
        "(\n${this.params.renderCode("    ", TypeRenderConfig.Default)})"
    } else {
        "($paramsOneLine)"
    }
    val docstring = starlarkDocstring()?.let { "$it\n" } ?: ""
    val ret = if (this.ret.typ != Ty.any()) {
        " -> ${this.ret.typ}"
    } else {
        ""
    }

    return "def $name$params$ret:\n${docstring}    pass"
}

// impl DocParam

// fn starlark_docstring(&self, max_indentation: &str) -> Option<String>
private fun DocParam.starlarkDocstring(maxIndentation: String): String? {
    val renderedDocs = this.docs?.renderAsCode() ?: return null
    val indented = indentTrimmed(renderedDocs, maxIndentation)
    // Replace the leading indentation with "name: " prefix.
    val prefix = "${this.name}: "
    return if (indented.length >= this.name.length + 2) {
        prefix + indented.substring(this.name.length + 2)
    } else {
        indented
    }
}

// fn fmt_param (DocParam)
// Rendered via DocParams.renderCode which uses ParamFmt.

// impl DocParams

/** Render multiline if `indent` is `Some`. */
// pub(crate) fn render_code(&self, indent: Option<&str>, render_config: &TypeRenderConfig) -> String
fun DocParams.renderCode(indent: String?, renderConfig: TypeRenderConfig): String {
    val parts = mutableListOf<String>()

    // Positional-only params
    for (p in posOnly) {
        parts.add(fmtParam(p, renderConfig))
    }
    if (posOnly.isNotEmpty()) {
        parts.add("/")
    }

    // Positional or named params
    for (p in posOrNamed) {
        parts.add(fmtParam(p, renderConfig))
    }

    // *args
    if (args != null) {
        parts.add("*${fmtParam(args!!, renderConfig)}")
    } else if (namedOnly.isNotEmpty()) {
        parts.add("*")
    }

    // Named-only params
    for (p in namedOnly) {
        parts.add(fmtParam(p, renderConfig))
    }

    // **kwargs
    if (kwargs != null) {
        parts.add("**${fmtParam(kwargs!!, renderConfig)}")
    }

    return if (indent != null) {
        parts.joinToString(",\n") { "$indent$it" }
    } else {
        parts.joinToString(", ")
    }
}

// Format a single param.
private fun fmtParam(p: DocParam, renderConfig: TypeRenderConfig): String {
    val ty = if (p.typ.isAny()) "" else ": ${p.typ.displayWith(renderConfig)}"
    val default = p.defaultValue?.let { " = $it" } ?: ""
    return "${p.name}$ty$default"
}

// impl DocReturn

// fn starlark_docstring(&self) -> Option<String>
private fun DocReturn.starlarkDocstring(): String? {
    return this.docs?.renderAsCode()
}

// impl DocProperty

// pub fn render_as_code(&self, name: &str) -> String
fun DocProperty.renderAsCode(name: String): String {
    val ds = this.docs?.renderAsQuotedCode()
    val t = this.typ
    return when {
        t.isAny() && ds != null -> "$ds\n_$name = None"
        t.isAny() -> "_$name = None"
        ds != null -> "$ds\n# type: $t\n_$name = None"
        else -> "# type: $t\n_$name = None"
    }
}

// impl DocType

// fn render_as_code(&self, name: &str) -> String
fun DocType.renderAsCode(name: String): String {
    val summary = this.docs?.let {
        var s = it.renderAsQuotedCode()
        s += "\n"
        s
    } ?: ""

    val memberDocs = this.members.iter()
        .map { (memberName, member) ->
            when (member) {
                is DocMember.Property -> member.property.renderAsCode(memberName)
                is DocMember.Function -> member.function.renderAsCode("_$memberName")
            }
        }
        .joinToString("\n\n")

    val exportedStructMembers = this.members.iter()
        .map { (memberName, _) -> "    $memberName = _$memberName," }
        .joinToString("\n")
    val exportedStruct = if (exportedStructMembers.isNotEmpty()) {
        "$summary$name = struct(\n$exportedStructMembers\n)"
    } else {
        ""
    }

    return "$memberDocs\n\n$exportedStruct".trim()
}

// impl DocItem

// pub fn render_as_code(&self, name: &str) -> String
fun DocItem.renderAsCode(name: String): String {
    return when (this) {
        is DocItem.Module -> this.module.renderAsCode()
        is DocItem.Type -> this.type.renderAsCode(name)
        is DocItem.Member -> when (val m = this.member) {
            is DocMember.Function -> m.function.renderAsCode(name)
            is DocMember.Property -> m.property.renderAsCode(name)
        }
    }
}
