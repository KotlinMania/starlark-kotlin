package io.github.kotlinmania.starlark_kotlin.syntax.parser

import io.github.kotlinmania.starlark_kotlin.codemap.CodeMap
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
import io.github.kotlinmania.starlark_kotlin.syntax.ast.*
import io.github.kotlinmania.starlark_kotlin.syntax.lexer.TokenFString
import io.github.kotlinmania.starlark_kotlin.syntax.state.ParserState

object GrammarUtil {
    fun statements(xs: List<AstStmt>, begin: Int, end: Int): AstStmt {
        TODO("Not yet implemented")
    }

    fun check_assign(codemap: CodeMap, x: AstExpr): AstAssignTarget {
        TODO("Not yet implemented")
    }

    fun check_assignment(
        codemap: CodeMap,
        lhs: AstExpr,
        ty: AstTypeExpr?,
        op: AssignOp?,
        rhs: AstExpr
    ): AstStmt {
        TODO("Not yet implemented")
    }

    fun check_load_0(module: AstString, parser_state: ParserState): Stmt {
        TODO("Not yet implemented")
    }

    fun check_load(
        module: AstString,
        args: List<Pair<Pair<AstAssignIdent, AstString>, Spanned<Comma>>>,
        last: Pair<AstAssignIdent, AstString>?,
        parser_state: ParserState
    ): Stmt {
        TODO("Not yet implemented")
    }

    fun fstring(
        fstring: TokenFString,
        begin: Int,
        end: Int,
        parser_state: ParserState
    ): AstFString {
        TODO("Not yet implemented")
    }

    fun dialect_check_type(
        state: ParserState,
        x: AstExpr
    ): AstTypeExpr {
        TODO("Not yet implemented")
    }

    fun check_call(
        e: AstExpr,
        a: List<AstArgument>,
        state: ParserState
    ): AstExpr {
        TODO("Not yet implemented")
    }
}
