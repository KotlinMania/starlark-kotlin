package io.github.kotlinmania.starlark_kotlin.syntax.state

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.syntax.dialect.Dialect

class ParserState(
    val dialect: Dialect,
    val codemap: CodeMap,
    val errors: MutableList<EvalException>
) {
    fun error(span: Span, error: String) {
        errors.add(EvalException.parserError(error, span, codemap))
    }
}
