// port-lint: source ../starlark_syntax/src/syntax/grammar.lalrpop
package io.github.kotlinmania.starlark.syntax.parser

import io.github.kotlinmania.starlarksyntax.codemap.Spanned as Spanned
import io.github.kotlinmania.starlark.syntax.ast.*
import io.github.kotlinmania.starlark.syntax.lexer.Token
import io.github.kotlinmania.starlark.syntax.lexer.TokenFString
import io.github.kotlinmania.starlark.syntax.lexer.TokenInt

/** Marker for nested optional handling where Kotlin nullability flattening loses state. */
class Comma

/**
 * Three-state value distinguishing the cases of a nested `Option<Option<T>>` from the
 * upstream Rust grammar.
 *
 * Necessary because Kotlin's nullable types collapse: `T??` is the same as `T?`, so the
 * three Rust states `None` / `Some(None)` / `Some(Some(value))` cannot be carried by a
 * plain `T?`. The upstream LALRPOP type analysis assigns those three states to a single
 * variant ([GrammarSymbol.Variant8] in this grammar) and the parse table relies on that
 * variant being distinguishable from a flat `T?` ([GrammarSymbol.Variant7]), so the
 * Kotlin port has to carry them in a wrapper.
 *
 * - [Absent]  — the outer Option matched `None` (Rust: `None`).
 * - [Empty]   — the outer Option matched `Some(None)` (Rust: `Some(None)`).
 * - [Present] — both matched (Rust: `Some(Some(value))`).
 */
sealed class NullableOption<out T> {
    object Absent : NullableOption<Nothing>()
    object Empty : NullableOption<Nothing>()
    data class Present<T>(val value: T) : NullableOption<T>()

    /**
     * Mirror of Rust's `option.unwrap_or(None)`. Collapses [Absent] and [Empty] both to
     * `null`, and [Present] to its inner value. Use this at the point in the grammar
     * action body where Rust would have called `unwrap_or(None)`; do not call it
     * earlier, because the Variant carrying the value relies on the three-state
     * distinction up to that point.
     */
    fun unwrapOrNull(): T? = when (this) {
        Absent -> null
        Empty -> null
        is Present -> value
    }
}

sealed class GrammarSymbol {
    data class Variant0(val value: Token) : GrammarSymbol()
    data class Variant1(val value: Double) : GrammarSymbol()
    data class Variant2(val value: TokenFString) : GrammarSymbol()
    data class Variant3(val value: String) : GrammarSymbol()
    data class Variant4(val value: TokenInt) : GrammarSymbol()
    data class Variant5(val value: Token?) : GrammarSymbol()
    data class Variant6(val value: List<Token>) : GrammarSymbol()
    data class Variant7(val value: Spanned<ExprP<AstNoPayload>>?) : GrammarSymbol()
    data class Variant8(val value: NullableOption<Spanned<ExprP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant9(val value: Spanned<StmtP<AstNoPayload>>) : GrammarSymbol()
    data class Variant10(val value: List<Spanned<StmtP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant11(val value: Spanned<ArgumentP<AstNoPayload>>) : GrammarSymbol()
    data class Variant12(val value: List<Spanned<ArgumentP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant13(val value: Spanned<ParameterP<AstNoPayload>>) : GrammarSymbol()
    data class Variant14(val value: List<Spanned<ParameterP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant15(val value: Pair<Spanned<ExprP<AstNoPayload>>, Spanned<ExprP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant16(val value: List<Pair<Spanned<ExprP<AstNoPayload>>, Spanned<ExprP<AstNoPayload>>>>) : GrammarSymbol()
    data class Variant17(val value: Spanned<ExprP<AstNoPayload>>) : GrammarSymbol()
    data class Variant18(val value: List<Spanned<ExprP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant19(val value: Pair<Pair<Spanned<AssignIdentP<AstNoPayload, Unit>>, Spanned<String>>, Spanned<Comma>>) : GrammarSymbol()
    data class Variant20(val value: List<Pair<Pair<Spanned<AssignIdentP<AstNoPayload, Unit>>, Spanned<String>>, Spanned<Comma>>>) : GrammarSymbol()
    data class Variant21(val value: Pair<Spanned<AssignIdentP<AstNoPayload, Unit>>, Spanned<String>>) : GrammarSymbol()
    data class Variant22(val value: Pair<Spanned<AssignIdentP<AstNoPayload, Unit>>, Spanned<String>>?) : GrammarSymbol()
    data class Variant23(val value: Int) : GrammarSymbol()
    data class Variant24(val value: Spanned<ArgumentP<AstNoPayload>>?) : GrammarSymbol()
    data class Variant25(val value: ArgumentP<AstNoPayload>) : GrammarSymbol()
    data class Variant26(val value: Spanned<AssignIdentP<AstNoPayload, Unit>>) : GrammarSymbol()
    data class Variant27(val value: AssignOp?) : GrammarSymbol()
    data class Variant28(val value: StmtP<AstNoPayload>) : GrammarSymbol()
    data class Variant29(val value: List<Spanned<ArgumentP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant30(val value: List<Spanned<ParameterP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant31(val value: List<Pair<Spanned<ExprP<AstNoPayload>>, Spanned<ExprP<AstNoPayload>>>>) : GrammarSymbol()
    data class Variant32(val value: List<Spanned<ExprP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant33(val value: ClauseP<AstNoPayload>) : GrammarSymbol()
    data class Variant34(val value: List<ClauseP<AstNoPayload>>) : GrammarSymbol()
    data class Variant35(val value: Spanned<Comma>) : GrammarSymbol()
    data class Variant36(val value: Pair<ForClauseP<AstNoPayload>, List<ClauseP<AstNoPayload>>>) : GrammarSymbol()
    data class Variant37(val value: Spanned<ParameterP<AstNoPayload>>?) : GrammarSymbol()
    data class Variant38(val value: ParameterP<AstNoPayload>) : GrammarSymbol()
    data class Variant39(val value: ExprP<AstNoPayload>) : GrammarSymbol()
    data class Variant40(val value: Pair<Spanned<ExprP<AstNoPayload>>, Spanned<ExprP<AstNoPayload>>>?) : GrammarSymbol()
    data class Variant41(val value: Spanned<StmtP<AstNoPayload>>?) : GrammarSymbol()
    data class Variant42(val value: ForClauseP<AstNoPayload>) : GrammarSymbol()
    data class Variant43(val value: Spanned<IdentP<AstNoPayload, Unit>>) : GrammarSymbol()
    data class Variant44(val value: Spanned<String>) : GrammarSymbol()
    data class Variant45(val value: Spanned<String>?) : GrammarSymbol()
    data class Variant46(val value: Spanned<TypeExprP<AstNoPayload, Unit>>?) : GrammarSymbol()
    data class Variant47(val value: Spanned<TypeExprP<AstNoPayload, Unit>>) : GrammarSymbol()
    data class Variant48(val value: Spanned<Double>) : GrammarSymbol()
    data class Variant49(val value: Spanned<FStringP<AstNoPayload>>) : GrammarSymbol()
    data class Variant50(val value: Spanned<TokenInt>) : GrammarSymbol()

    /** Extract the inner value from this wrapper for import in reducer action functions. */
    fun unwrap(): Any? = when (this) {
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
