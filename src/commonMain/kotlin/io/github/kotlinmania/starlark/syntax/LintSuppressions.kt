// port-lint: source starlark_syntax/src/syntax/lint_suppressions.rs
package io.github.kotlinmania.starlark.syntax

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Pos
import io.github.kotlinmania.starlark.codemap.Span

private const val LINT_SUPPRESSION_PREFIX = "starlark-lint-disable "

data class SuppressionInfo(
    val tokenSpan: Span,
    val effectiveSpan: Span,
    val suppressNextLine: Boolean,
)

class LintSuppressions internal constructor(
    private val suppressions: Map<String, List<SuppressionInfo>>,
) {
    fun isSuppressed(issueShortName: String, issueSpan: Span): Boolean =
        suppressions[issueShortName]?.any { info ->
            if (info.suppressNextLine && issueSpan.end.value > 0 && issueSpan.end - 1 == info.tokenSpan.end) {
                false
            } else {
                issueSpan.intersects(info.effectiveSpan)
            }
        } == true

    companion object {
        val EMPTY: LintSuppressions = LintSuppressions(emptyMap())
    }
}

private data class ParseState(
    val tokenSpans: MutableList<Span> = mutableListOf(),
    val effectiveSpans: MutableList<Span> = mutableListOf(),
    val shortNames: MutableSet<String> = mutableSetOf(),
    var lastLine: Int = 0,
) {
    fun clear() {
        tokenSpans.clear()
        effectiveSpans.clear()
        shortNames.clear()
        lastLine = 0
    }
}

internal class LintSuppressionsBuilder {
    private val state = ParseState()
    private val suppressions: MutableMap<String, MutableList<SuppressionInfo>> = mutableMapOf()

    fun parseComment(codemap: CodeMap, comment: String, start: Int, end: Int) {
        val parsedShortNames = parseLintSuppressions(comment)
        if (parsedShortNames.isNotEmpty() || state.shortNames.isNotEmpty()) {
            val tokenSpan = Span(Pos(start), Pos(end))
            val line = codemap.findLine(Pos(start))
            state.shortNames.addAll(parsedShortNames)
            state.tokenSpans.add(tokenSpan)
            state.effectiveSpans.add(codemap.lineSpanTrimNewline(line))
            state.lastLine = line
        }
    }

    fun endOfCommentBlock(codemap: CodeMap) {
        if (state.shortNames.isNotEmpty()) {
            updateLintSuppressions(codemap)
        }
    }

    fun build(): LintSuppressions {
        require(state.shortNames.isEmpty() && state.tokenSpans.isEmpty() && state.effectiveSpans.isEmpty())
        return LintSuppressions(suppressions)
    }

    private fun updateLintSuppressions(codemap: CodeMap) {
        val numberOfTokens = state.tokenSpans.size
        val tokenSpan = Span.mergeAll(state.tokenSpans.iterator())
        var effectiveSpan = Span.mergeAll(state.effectiveSpans.iterator())
        val sourceBeforeToken = codemap.sourceSpan(Span(effectiveSpan.begin, tokenSpan.begin))
        val suppressNextLine =
            numberOfTokens > 1 ||
                effectiveSpan == tokenSpan ||
                (effectiveSpan.end == tokenSpan.end && sourceBeforeToken.trim().isEmpty())
        if (suppressNextLine) {
            codemap.lineSpanOpt(state.lastLine + 1)?.let { nextLineSpan ->
                effectiveSpan = effectiveSpan.merge(nextLineSpan)
            }
        }

        for (name in state.shortNames) {
            suppressions.getOrPut(name) { mutableListOf() }.add(
                SuppressionInfo(
                    tokenSpan = tokenSpan,
                    effectiveSpan = effectiveSpan,
                    suppressNextLine = suppressNextLine,
                ),
            )
        }
        state.clear()
    }
}

private fun parseLintSuppressions(commentLine: String): List<String> {
    val shortNames = commentLine.trimStart().removePrefix(LINT_SUPPRESSION_PREFIX)
    if (shortNames == commentLine.trimStart()) return emptyList()
    return shortNames
        .split(' ', ',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
