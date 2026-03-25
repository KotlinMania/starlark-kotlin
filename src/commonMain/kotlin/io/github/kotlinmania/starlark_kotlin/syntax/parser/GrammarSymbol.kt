package io.github.kotlinmania.starlark_kotlin.syntax.parser

import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.syntax.ast.*
import io.github.kotlinmania.starlark_kotlin.syntax.lexer.Token
import io.github.kotlinmania.starlark_kotlin.syntax.lexer.TokenFString
import io.github.kotlinmania.starlark_kotlin.syntax.lexer.TokenInt

/** Marker for nested Option<> handling where nullability flattening in Kotlin loses state. */

class Comma

sealed class GrammarSymbol {
    data class Variant0(val value: Token) : GrammarSymbol()
    data class Variant1(val value: Double) : GrammarSymbol()
    data class Variant2(val value: TokenFString) : GrammarSymbol()
    data class Variant3(val value: String) : GrammarSymbol()
    data class Variant4(val value: TokenInt) : GrammarSymbol()
    data class Variant5(val value: Token?) : GrammarSymbol()
    data class Variant6(val value: List<Token>) : GrammarSymbol()
    data class Variant7(val value: AstExpr?) : GrammarSymbol()
    data class Variant8(val value: AstExpr?) : GrammarSymbol()
    data class Variant9(val value: AstStmt) : GrammarSymbol()
    data class Variant10(val value: List<AstStmt>) : GrammarSymbol()
    data class Variant11(val value: AstArgument) : GrammarSymbol()
    data class Variant12(val value: List<AstArgument>) : GrammarSymbol()
    data class Variant13(val value: AstParameter) : GrammarSymbol()
    data class Variant14(val value: List<AstParameter>) : GrammarSymbol()
    data class Variant15(val value: Pair<AstExpr, AstExpr>) : GrammarSymbol()
    data class Variant16(val value: List<Pair<AstExpr, AstExpr>>) : GrammarSymbol()
    data class Variant17(val value: AstExpr) : GrammarSymbol()
    data class Variant18(val value: List<AstExpr>) : GrammarSymbol()
    data class Variant19(val value: Pair<Pair<AstAssignIdent, AstString>, Spanned<Comma>>) : GrammarSymbol()
    data class Variant20(val value: List<Pair<Pair<AstAssignIdent, AstString>, Spanned<Comma>>>) : GrammarSymbol()
    data class Variant21(val value: Pair<AstAssignIdent, AstString>) : GrammarSymbol()
    data class Variant22(val value: Pair<AstAssignIdent, AstString>?) : GrammarSymbol()
    data class Variant23(val value: Int) : GrammarSymbol()
    data class Variant24(val value: AstArgument?) : GrammarSymbol()
    data class Variant25(val value: Argument) : GrammarSymbol()
    data class Variant26(val value: AstAssignIdent) : GrammarSymbol()
    data class Variant27(val value: AssignOp?) : GrammarSymbol()
    data class Variant28(val value: Stmt) : GrammarSymbol()
    data class Variant29(val value: List<AstArgument>) : GrammarSymbol()
    data class Variant30(val value: List<AstParameter>) : GrammarSymbol()
    data class Variant31(val value: List<Pair<AstExpr, AstExpr>>) : GrammarSymbol()
    data class Variant32(val value: List<AstExpr>) : GrammarSymbol()
    data class Variant33(val value: Clause) : GrammarSymbol()
    data class Variant34(val value: List<Clause>) : GrammarSymbol()
    data class Variant35(val value: Spanned<Comma>) : GrammarSymbol()
    data class Variant36(val value: Pair<ForClause, List<Clause>>) : GrammarSymbol()
    data class Variant37(val value: AstParameter?) : GrammarSymbol()
    data class Variant38(val value: Parameter) : GrammarSymbol()
    data class Variant39(val value: Expr) : GrammarSymbol()
    data class Variant40(val value: Pair<AstExpr, AstExpr>?) : GrammarSymbol()
    data class Variant41(val value: AstStmt?) : GrammarSymbol()
    data class Variant42(val value: ForClause) : GrammarSymbol()
    data class Variant43(val value: AstIdent) : GrammarSymbol()
    data class Variant44(val value: AstString) : GrammarSymbol()
    data class Variant45(val value: AstString?) : GrammarSymbol()
    data class Variant46(val value: AstTypeExpr?) : GrammarSymbol()
    data class Variant47(val value: AstTypeExpr) : GrammarSymbol()
    data class Variant48(val value: AstFloat) : GrammarSymbol()
    data class Variant49(val value: AstFString) : GrammarSymbol()
    data class Variant50(val value: AstInt) : GrammarSymbol()
}
