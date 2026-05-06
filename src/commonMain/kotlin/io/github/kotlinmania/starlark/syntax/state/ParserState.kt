package io.github.kotlinmania.starlark.syntax.state

import io.github.kotlinmania.starlarksyntax.codemap.CodeMap as CodeMap
import io.github.kotlinmania.starlarksyntax.codemap.Span as Span
import io.github.kotlinmania.starlarksyntax.evalexception.EvalException
import io.github.kotlinmania.starlark.typing.StarlarkError
import io.github.kotlinmania.starlark.syntax.dialect.Dialect

class ParserState(
    val dialect: Dialect,
    val codemap: CodeMap,
    val errors: MutableList<EvalException>
) {
    fun error(span: Span, error: String) {
        errors.add(EvalException.new(StarlarkError(error), span, codemap))
    }
}
