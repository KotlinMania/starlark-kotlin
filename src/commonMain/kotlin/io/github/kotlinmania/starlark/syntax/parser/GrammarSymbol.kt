@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)
package io.github.kotlinmania.starlark.syntax.parser

import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.ast.Argument
import io.github.kotlinmania.starlark.syntax.ast.AssignOp
import io.github.kotlinmania.starlark.syntax.ast.AstArgument
import io.github.kotlinmania.starlark.syntax.ast.AstAssignIdent
import io.github.kotlinmania.starlark.syntax.ast.AstExpr
import io.github.kotlinmania.starlark.syntax.ast.AstFString
import io.github.kotlinmania.starlark.syntax.ast.AstFloat
import io.github.kotlinmania.starlark.syntax.ast.AstIdent
import io.github.kotlinmania.starlark.syntax.ast.AstInt
import io.github.kotlinmania.starlark.syntax.ast.AstParameter
import io.github.kotlinmania.starlark.syntax.ast.AstStmt
import io.github.kotlinmania.starlark.syntax.ast.AstString
import io.github.kotlinmania.starlark.syntax.ast.AstTypeExpr
import io.github.kotlinmania.starlark.syntax.ast.Clause
import io.github.kotlinmania.starlark.syntax.ast.Comma
import io.github.kotlinmania.starlark.syntax.ast.Expr
import io.github.kotlinmania.starlark.syntax.ast.ForClause
import io.github.kotlinmania.starlark.syntax.ast.Parameter
import io.github.kotlinmania.starlark.syntax.ast.Stmt
import io.github.kotlinmania.starlark.syntax.lexer.Token
import io.github.kotlinmania.starlark.syntax.lexer.TokenFString
import io.github.kotlinmania.starlark.syntax.lexer.TokenInt
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
sealed class GrammarSymbol {
    data class Variant0(
        val value: Token,
    ) : GrammarSymbol()

    data class Variant1(
        val value: Double,
    ) : GrammarSymbol()

    data class Variant2(
        val value: TokenFString,
    ) : GrammarSymbol()

    data class Variant3(
        val value: String,
    ) : GrammarSymbol()

    data class Variant4(
        val value: TokenInt,
    ) : GrammarSymbol()

    data class Variant5(
        val value: Token?,
    ) : GrammarSymbol()

    data class Variant6(
        val value: List<Token>,
    ) : GrammarSymbol()

    data class Variant7(
        val value: AstExpr?,
    ) : GrammarSymbol()

    data class Variant8(
        val value: AstExpr?,
    ) : GrammarSymbol()

    data class Variant9(
        val value: AstStmt,
    ) : GrammarSymbol()

    data class Variant10(
        val value: List<AstStmt>,
    ) : GrammarSymbol()

    data class Variant11(
        val value: AstArgument,
    ) : GrammarSymbol()

    data class Variant12(
        val value: List<AstArgument>,
    ) : GrammarSymbol()

    data class Variant13(
        val value: AstParameter,
    ) : GrammarSymbol()

    data class Variant14(
        val value: List<AstParameter>,
    ) : GrammarSymbol()

    data class Variant15(
        val value: Pair<AstExpr, AstExpr>,
    ) : GrammarSymbol()

    data class Variant16(
        val value: List<Pair<AstExpr, AstExpr>>,
    ) : GrammarSymbol()

    data class Variant17(
        val value: AstExpr,
    ) : GrammarSymbol()

    data class Variant18(
        val value: List<AstExpr>,
    ) : GrammarSymbol()

    data class Variant19(
        val value: Pair<Pair<AstAssignIdent, AstString>, Spanned<Comma>>,
    ) : GrammarSymbol()

    data class Variant20(
        val value: List<Pair<Pair<AstAssignIdent, AstString>, Spanned<Comma>>>,
    ) : GrammarSymbol()

    data class Variant21(
        val value: Pair<AstAssignIdent, AstString>,
    ) : GrammarSymbol()

    data class Variant22(
        val value: Pair<AstAssignIdent, AstString>?,
    ) : GrammarSymbol()

    data class Variant23(
        val value: Int,
    ) : GrammarSymbol()

    data class Variant24(
        val value: AstArgument?,
    ) : GrammarSymbol()

    data class Variant25(
        val value: Argument,
    ) : GrammarSymbol()

    data class Variant26(
        val value: AstAssignIdent,
    ) : GrammarSymbol()

    data class Variant27(
        val value: AssignOp?,
    ) : GrammarSymbol()

    data class Variant28(
        val value: Stmt,
    ) : GrammarSymbol()

    data class Variant29(
        val value: List<AstArgument>,
    ) : GrammarSymbol()

    data class Variant30(
        val value: List<AstParameter>,
    ) : GrammarSymbol()

    data class Variant31(
        val value: List<Pair<AstExpr, AstExpr>>,
    ) : GrammarSymbol()

    data class Variant32(
        val value: List<AstExpr>,
    ) : GrammarSymbol()

    data class Variant33(
        val value: Clause,
    ) : GrammarSymbol()

    data class Variant34(
        val value: List<Clause>,
    ) : GrammarSymbol()

    data class Variant35(
        val value: Spanned<Comma>,
    ) : GrammarSymbol()

    data class Variant36(
        val value: Pair<ForClause, List<Clause>>,
    ) : GrammarSymbol()

    data class Variant37(
        val value: AstParameter?,
    ) : GrammarSymbol()

    data class Variant38(
        val value: Parameter,
    ) : GrammarSymbol()

    data class Variant39(
        val value: Expr,
    ) : GrammarSymbol()

    data class Variant40(
        val value: Pair<AstExpr, AstExpr>?,
    ) : GrammarSymbol()

    data class Variant41(
        val value: AstStmt?,
    ) : GrammarSymbol()

    data class Variant42(
        val value: ForClause,
    ) : GrammarSymbol()

    data class Variant43(
        val value: AstIdent,
    ) : GrammarSymbol()

    data class Variant44(
        val value: AstString,
    ) : GrammarSymbol()

    data class Variant45(
        val value: AstString?,
    ) : GrammarSymbol()

    data class Variant46(
        val value: AstTypeExpr?,
    ) : GrammarSymbol()

    data class Variant47(
        val value: AstTypeExpr,
    ) : GrammarSymbol()

    data class Variant48(
        val value: AstFloat,
    ) : GrammarSymbol()

    data class Variant49(
        val value: AstFString,
    ) : GrammarSymbol()

    data class Variant50(
        val value: AstInt,
    ) : GrammarSymbol()

    /** Extract the inner value from this wrapper for use in reducer action functions. */
    fun unwrap(): Any? =
        when (this) {
            is Variant0 -> value
            is Variant1 -> value
            is Variant2 -> value
            is Variant3 -> value
            is Variant4 -> value
            is Variant5 -> value
            is Variant6 -> value
            is Variant7 -> value
            is Variant8 -> value
            is Variant9 -> value
            is Variant10 -> value
            is Variant11 -> value
            is Variant12 -> value
            is Variant13 -> value
            is Variant14 -> value
            is Variant15 -> value
            is Variant16 -> value
            is Variant17 -> value
            is Variant18 -> value
            is Variant19 -> value
            is Variant20 -> value
            is Variant21 -> value
            is Variant22 -> value
            is Variant23 -> value
            is Variant24 -> value
            is Variant25 -> value
            is Variant26 -> value
            is Variant27 -> value
            is Variant28 -> value
            is Variant29 -> value
            is Variant30 -> value
            is Variant31 -> value
            is Variant32 -> value
            is Variant33 -> value
            is Variant34 -> value
            is Variant35 -> value
            is Variant36 -> value
            is Variant37 -> value
            is Variant38 -> value
            is Variant39 -> value
            is Variant40 -> value
            is Variant41 -> value
            is Variant42 -> value
            is Variant43 -> value
            is Variant44 -> value
            is Variant45 -> value
            is Variant46 -> value
            is Variant47 -> value
            is Variant48 -> value
            is Variant49 -> value
            is Variant50 -> value
        }
}
