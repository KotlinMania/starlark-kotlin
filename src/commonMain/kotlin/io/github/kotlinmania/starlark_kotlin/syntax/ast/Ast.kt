// port-lint: source src/syntax/ast.rs
package io.github.kotlinmania.starlark_kotlin.syntax.ast

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.syntax.lexer.TokenInt
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span

/** Payload types attached to AST nodes. */
interface AstPayload {
    // We don't really need `Clone` for any payload in Kotlin.
    // In Kotlin we use generics directly or expect the implementation to provide the correct types.
    // Since Kotlin doesn't have associated types, we'll parameterize or just use `Any?` for now,
    // or properly type the nodes using generic type parameters.
}

/** 
 * Default implementation of payload, which attaches `()` to nodes.
 * This payload is returned with AST by parser.
 */
object AstNoPayload : AstPayload

class Comma


sealed class ArgumentP<P : AstPayload> {
    data class Positional<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ArgumentP<P>()
    data class Named<P : AstPayload>(val name: Spanned<String>, val expr: Spanned<ExprP<P>>) : ArgumentP<P>()
    data class Args<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ArgumentP<P>()
    data class KwArgs<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ArgumentP<P>()

    fun expr(): Spanned<ExprP<P>> = when (this) {
        is Positional -> expr
        is Named -> expr
        is Args -> expr
        is KwArgs -> expr
    }

    fun name(): String? = when (this) {
        is Named -> name.node
        else -> null
    }
}

sealed class ParameterP<P : AstPayload> {
    /** `/` marker. */
    class Slash<P : AstPayload> : ParameterP<P>()
    data class Normal<P : AstPayload>(
        /** Name. */
        val name: Spanned<AssignIdentP<P, *>>,
        /** Type. */
        val typ: Spanned<TypeExprP<P, Unit>>?,
        /** Default value. */
        val defaultVal: Spanned<ExprP<P>>?
    ) : ParameterP<P>()
    /** `*` marker. */
    class NoArgs<P : AstPayload> : ParameterP<P>()
    data class Args<P : AstPayload>(val name: Spanned<AssignIdentP<P, *>>, val typ: Spanned<TypeExprP<P, Unit>>?) : ParameterP<P>()
    data class KwArgs<P : AstPayload>(val name: Spanned<AssignIdentP<P, *>>, val typ: Spanned<TypeExprP<P, Unit>>?) : ParameterP<P>()

    fun ident(): Spanned<AssignIdentP<P, *>>? = when (this) {
        is Normal -> name
        is Args -> name
        is KwArgs -> name
        is NoArgs, is Slash -> null
    }
}

sealed class AstLiteral {
    data class Int(val value: Spanned<TokenInt>) : AstLiteral()
    data class Float(val value: Spanned<Double>) : AstLiteral()
    data class String(val value: Spanned<kotlin.String>) : AstLiteral()
    object Ellipsis : AstLiteral()
}

data class LambdaP<P : AstPayload, DP>(
    val params: List<Spanned<ParameterP<P>>>,
    val body: Spanned<ExprP<P>>,
    var payload: DP
) {
    fun signatureSpan(): Span {
        if (params.isEmpty()) return body.span
        var span = params[0].span
        for (i in 1 until params.size) {
            span = span.merge(params[i].span)
        }
        return span
    }
}

data class CallArgsP<P : AstPayload>(val args: List<Spanned<ArgumentP<P>>>)

sealed class ExprP<P : AstPayload> {
    data class Tuple<P : AstPayload>(val elements: List<Spanned<ExprP<P>>>) : ExprP<P>()
    data class Dot<P : AstPayload>(val expr: Spanned<ExprP<P>>, val field: Spanned<String>) : ExprP<P>()
    data class Call<P : AstPayload>(val expr: Spanned<ExprP<P>>, val args: CallArgsP<P>) : ExprP<P>()
    data class Index<P : AstPayload>(val expr: Spanned<ExprP<P>>, val index: Spanned<ExprP<P>>) : ExprP<P>()
    data class Index2<P : AstPayload>(val expr: Spanned<ExprP<P>>, val index0: Spanned<ExprP<P>>, val index1: Spanned<ExprP<P>>) : ExprP<P>()
    data class Slice<P : AstPayload>(
        val expr: Spanned<ExprP<P>>,
        val start: Spanned<ExprP<P>>?,
        val stop: Spanned<ExprP<P>>?,
        val step: Spanned<ExprP<P>>?
    ) : ExprP<P>()
    data class Identifier<P : AstPayload, IP>(val ident: Spanned<IdentP<P, IP>>) : ExprP<P>()
    data class Lambda<P : AstPayload, DP>(val lambda: LambdaP<P, DP>) : ExprP<P>()
    data class Literal<P : AstPayload>(val literal: AstLiteral) : ExprP<P>()
    data class Not<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ExprP<P>()
    data class Minus<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ExprP<P>()
    data class Plus<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ExprP<P>()
    data class BitNot<P : AstPayload>(val expr: Spanned<ExprP<P>>) : ExprP<P>()
    data class Op<P : AstPayload>(val lhs: Spanned<ExprP<P>>, val op: BinOp, val rhs: Spanned<ExprP<P>>) : ExprP<P>()
    // Order: condition, v1, v2 <=> v1 if condition else v2
    data class If<P : AstPayload>(val cond: Spanned<ExprP<P>>, val v1: Spanned<ExprP<P>>, val v2: Spanned<ExprP<P>>) : ExprP<P>()
    data class ListExpr<P : AstPayload>(val elements: List<Spanned<ExprP<P>>>) : ExprP<P>()
    data class Dict<P : AstPayload>(val elements: List<Pair<Spanned<ExprP<P>>, Spanned<ExprP<P>>>>) : ExprP<P>()
    data class ListComprehension<P : AstPayload>(val expr: Spanned<ExprP<P>>, val forClause: ForClauseP<P>, val clauses: List<ClauseP<P>>) : ExprP<P>()
    data class DictComprehension<P : AstPayload>(val key: Spanned<ExprP<P>>, val value: Spanned<ExprP<P>>, val forClause: ForClauseP<P>, val clauses: List<ClauseP<P>>) : ExprP<P>()
    data class FString<P : AstPayload>(val fstring: Spanned<FStringP<P>>) : ExprP<P>()
}

data class TypeExprP<P : AstPayload, TEP>(
    val expr: Spanned<ExprP<P>>,
    var payload: TEP
)

sealed class AssignTargetP<P : AstPayload> {
    data class Tuple<P : AstPayload>(val elements: List<Spanned<AssignTargetP<P>>>) : AssignTargetP<P>()
    data class Index<P : AstPayload>(val expr: Spanned<ExprP<P>>, val index: Spanned<ExprP<P>>) : AssignTargetP<P>()
    data class Dot<P : AstPayload>(val expr: Spanned<ExprP<P>>, val field: Spanned<String>) : AssignTargetP<P>()
    data class Identifier<P : AstPayload, IAP>(val ident: Spanned<AssignIdentP<P, IAP>>) : AssignTargetP<P>()
}

data class AssignP<P : AstPayload>(
    val lhs: Spanned<AssignTargetP<P>>,
    val ty: Spanned<TypeExprP<P, Unit>>?,
    val rhs: Spanned<ExprP<P>>
)

data class AssignIdentP<P : AstPayload, IAP>(
    val ident: String,
    var payload: IAP
)

data class IdentP<P : AstPayload, IP>(
    val ident: String,
    var payload: IP
)

data class LoadArgP<P : AstPayload, IAP>(
    val local: Spanned<AssignIdentP<P, IAP>>,
    val their: Spanned<String>,
    val comma: Spanned<Comma>?
) {
    fun span(): Span = local.span.merge(their.span)
    fun spanWithTrailingComma(): Span = if (comma != null) span().merge(comma.span) else span()
}

data class LoadP<P : AstPayload, LP>(
    val module: Spanned<String>,
    val args: List<LoadArgP<P, *>>,
    var payload: LP
)

data class ForClauseP<P : AstPayload>(val varTarget: Spanned<AssignTargetP<P>>, val over: Spanned<ExprP<P>>)

sealed class ClauseP<P : AstPayload> {
    data class For<P : AstPayload>(val forClause: ForClauseP<P>) : ClauseP<P>()
    data class If<P : AstPayload>(val cond: Spanned<ExprP<P>>) : ClauseP<P>()
}

enum class BinOp {
    Or, And, Equal, NotEqual, Less, Greater, LessOrEqual, GreaterOrEqual,
    In, NotIn, Subtract, Add, Multiply, Percent, Divide, FloorDivide,
    BitAnd, BitOr, BitXor, LeftShift, RightShift
}

enum class AssignOp {
    Add, Subtract, Multiply, Divide, FloorDivide, Percent,
    BitAnd, BitOr, BitXor, LeftShift, RightShift
}

enum class Visibility { Private, Public }

data class DefP<P : AstPayload, DP>(
    val name: Spanned<AssignIdentP<P, *>>,
    val params: List<Spanned<ParameterP<P>>>,
    val returnType: Spanned<TypeExprP<P, Unit>>?,
    val body: Spanned<StmtP<P>>,
    var payload: DP
) {
    fun signatureSpan(): Span {
        var span = name.span
        for (param in params) span = span.merge(param.span)
        if (returnType != null) span = span.merge(returnType.span)
        return span
    }
}

data class ForP<P : AstPayload>(
    val varTarget: Spanned<AssignTargetP<P>>,
    val over: Spanned<ExprP<P>>,
    val body: Spanned<StmtP<P>>
)

data class FStringP<P : AstPayload>(
    val format: Spanned<String>,
    val expressions: List<Spanned<ExprP<P>>>
)

sealed class StmtP<P : AstPayload> {
    class Break<P : AstPayload> : StmtP<P>()
    class Continue<P : AstPayload> : StmtP<P>()
    class Pass<P : AstPayload> : StmtP<P>()
    data class Return<P : AstPayload>(val expr: Spanned<ExprP<P>>?) : StmtP<P>()
    data class Expression<P : AstPayload>(val expr: Spanned<ExprP<P>>) : StmtP<P>()
    data class Assign<P : AstPayload>(val assign: AssignP<P>) : StmtP<P>()
    data class AssignModify<P : AstPayload>(val lhs: Spanned<AssignTargetP<P>>, val op: AssignOp, val rhs: Spanned<ExprP<P>>) : StmtP<P>()
    data class Statements<P : AstPayload>(val stmts: List<Spanned<StmtP<P>>>) : StmtP<P>()
    data class If<P : AstPayload>(val cond: Spanned<ExprP<P>>, val suite: Spanned<StmtP<P>>) : StmtP<P>()
    data class IfElse<P : AstPayload>(val cond: Spanned<ExprP<P>>, val suite1: Spanned<StmtP<P>>, val suite2: Spanned<StmtP<P>>) : StmtP<P>()
    data class For<P : AstPayload>(val forStmt: ForP<P>) : StmtP<P>()
    data class Def<P : AstPayload, DP>(val def: DefP<P, DP>) : StmtP<P>()
    data class Load<P : AstPayload, LP>(val loadStmt: LoadP<P, LP>) : StmtP<P>()
}
