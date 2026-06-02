@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark.syntax.state

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.syntax.dialect.Dialect
import io.github.kotlinmania.starlark.typing.EvalException
import io.github.kotlinmania.starlark.typing.StarlarkError
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
class ParserState(
    val dialect: Dialect,
    val codemap: CodeMap,
    val errors: MutableList<EvalException>,
) {
    fun error(span: Span, error: String) {
        errors.add(EvalException.new(StarlarkError(error), span, codemap))
    }
}
