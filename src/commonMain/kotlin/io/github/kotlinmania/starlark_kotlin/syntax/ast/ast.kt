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
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.merge
import io.github.kotlinmania.starlark_kotlin.codemap.Spanned
import io.github.kotlinmania.starlark_kotlin.codemap.Span
import io.github.kotlinmania.starlark_kotlin.analysis.span
import io.github.kotlinmania.starlark_kotlin.analysis.node

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

typealias Expr = ExprP<AstNoPayload>
typealias TypeExpr = TypeExprP<AstNoPayload>
typealias AssignTarget = AssignTargetP<AstNoPayload>
typealias AssignIdent = AssignIdentP<AstNoPayload, Unit>
typealias Ident = IdentP<AstNoPayload, Unit>
typealias Clause = ClauseP<AstNoPayload>
typealias ForClause = ForClauseP<AstNoPayload>
typealias Argument = ArgumentP<AstNoPayload>
typealias Parameter = ParameterP<AstNoPayload>
typealias Load = LoadP<AstNoPayload, Unit>
typealias Stmt = StmtP<AstNoPayload>

typealias AstExprP<P> = Spanned<ExprP<P>>
typealias AstTypeExprP<P> = Spanned<TypeExprP<P, Unit>>
typealias AstAssignTargetP<P> = Spanned<AssignTargetP<P>>
typealias AstAssignIdentP<P, IAP> = Spanned<AssignIdentP<P, IAP>>
typealias AstIdentP<P, IP> = Spanned<IdentP<P, IP>>
typealias AstArgumentP<P> = Spanned<ArgumentP<P>>
typealias AstParameterP<P> = Spanned<ParameterP<P>>
typealias AstStmtP<P> = Spanned<StmtP<P>>
typealias AstFStringP<P> = Spanned<FStringP<P>>

typealias AstExpr = AstExprP<AstNoPayload>
typealias AstTypeExpr = AstTypeExprP<AstNoPayload>
typealias AstAssignTarget = AstAssignTargetP<AstNoPayload>
typealias AstAssignIdent = AstAssignIdentP<AstNoPayload, Unit>
typealias AstIdent = AstIdentP<AstNoPayload, Unit>
typealias AstArgument = AstArgumentP<AstNoPayload>
typealias AstString = Spanned<String>
typealias AstParameter = AstParameterP<AstNoPayload>
typealias AstInt = Spanned<TokenInt>
typealias AstFloat = Spanned<Double>
typealias AstFString = AstFStringP<AstNoPayload>
typealias AstStmt = AstStmtP<AstNoPayload>


sealed class ArgumentP<P : AstPayload> {
    data class Positional<P : AstPayload>(val expr: AstExprP<P>) : ArgumentP<P>()
    data class Named<P : AstPayload>(val name: AstString, val expr: AstExprP<P>) : ArgumentP<P>()
    data class Args<P : AstPayload>(val expr: AstExprP<P>) : ArgumentP<P>()
    data class KwArgs<P : AstPayload>(val expr: AstExprP<P>) : ArgumentP<P>()

    fun expr(): AstExprP<P> = when (this) {
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
        val name: AstAssignIdentP<P, *>,
        /** Type. */
        val typ: AstTypeExprP<P>?,
        /** Default value. */
        val defaultVal: AstExprP<P>?
    ) : ParameterP<P>()
    /** `*` marker. */
    class NoArgs<P : AstPayload> : ParameterP<P>()
    data class Args<P : AstPayload>(val name: AstAssignIdentP<P, *>, val typ: AstTypeExprP<P>?) : ParameterP<P>()
    data class KwArgs<P : AstPayload>(val name: AstAssignIdentP<P, *>, val typ: AstTypeExprP<P>?) : ParameterP<P>()

    fun ident(): AstAssignIdentP<P, *>? = when (this) {
        is Normal -> name
        is Args -> name
        is KwArgs -> name
        is NoArgs, is Slash -> null
    }
}

sealed class AstLiteral {
    // TODO: stub - Int needs real import
    data class Int(val value: AstInt) : AstLiteral()
    // TODO: stub - Float needs real import
    data class Float(val value: AstFloat) : AstLiteral()
    // TODO: stub - String needs real import
    data class String(val value: AstString) : AstLiteral()
    // TODO: stub - Ellipsis needs real import
    object Ellipsis : AstLiteral()
}

data class LambdaP<P : AstPayload, DP>(
    val params: List<AstParameterP<P>>,
    val body: AstExprP<P>,
    val payload: DP
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

data class CallArgsP<P : AstPayload>(val args: List<AstArgumentP<P>>)

sealed class ExprP<P : AstPayload> {
    // TODO: stub - Tuple needs real import
    data class Tuple<P : AstPayload>(val elements: List<AstExprP<P>>) : ExprP<P>()
    data class Dot<P : AstPayload>(val expr: AstExprP<P>, val field: AstString) : ExprP<P>()
    data class Call<P : AstPayload>(val expr: AstExprP<P>, val args: CallArgsP<P>) : ExprP<P>()
    data class Index<P : AstPayload>(val expr: AstExprP<P>, val index: AstExprP<P>) : ExprP<P>()
    data class Index2<P : AstPayload>(val expr: AstExprP<P>, val index0: AstExprP<P>, val index1: AstExprP<P>) : ExprP<P>()
    data class Slice<P : AstPayload>(
        val expr: AstExprP<P>,
        val start: AstExprP<P>?,
        val stop: AstExprP<P>?,
        val step: AstExprP<P>?
    ) : ExprP<P>()
    // TODO: stub - Identifier needs real import
    data class Identifier<P : AstPayload, IP>(val ident: AstIdentP<P, IP>) : ExprP<P>()
    data class Lambda<P : AstPayload, DP>(val lambda: LambdaP<P, DP>) : ExprP<P>()
    data class Literal<P : AstPayload>(val literal: AstLiteral) : ExprP<P>()
    data class Not<P : AstPayload>(val expr: AstExprP<P>) : ExprP<P>()
    data class Minus<P : AstPayload>(val expr: AstExprP<P>) : ExprP<P>()
    data class Plus<P : AstPayload>(val expr: AstExprP<P>) : ExprP<P>()
    data class BitNot<P : AstPayload>(val expr: AstExprP<P>) : ExprP<P>()
    data class Op<P : AstPayload>(val lhs: AstExprP<P>, val op: BinOp, val rhs: AstExprP<P>) : ExprP<P>()
    // Order: condition, v1, v2 <=> v1 if condition else v2
    data class If<P : AstPayload>(val cond: AstExprP<P>, val v1: AstExprP<P>, val v2: AstExprP<P>) : ExprP<P>()
    data class ListExpr<P : AstPayload>(val elements: List<AstExprP<P>>) : ExprP<P>()
    // TODO: stub - Dict needs real import
    data class Dict<P : AstPayload>(val elements: List<Pair<AstExprP<P>, AstExprP<P>>>) : ExprP<P>()
    data class ListComprehension<P : AstPayload>(val expr: AstExprP<P>, val forClause: ForClauseP<P>, val clauses: List<ClauseP<P>>) : ExprP<P>()
    data class DictComprehension<P : AstPayload>(val key: AstExprP<P>, val value: AstExprP<P>, val forClause: ForClauseP<P>, val clauses: List<ClauseP<P>>) : ExprP<P>()
    data class FString<P : AstPayload>(val fstring: AstFStringP<P>) : ExprP<P>()
}

data class TypeExprP<P : AstPayload, TEP>(
    val expr: AstExprP<P>,
    val payload: TEP
)

sealed class AssignTargetP<P : AstPayload> {
    // TODO: stub - Tuple needs real import
    data class Tuple<P : AstPayload>(val elements: List<AstAssignTargetP<P>>) : AssignTargetP<P>()
    data class Index<P : AstPayload>(val expr: AstExprP<P>, val index: AstExprP<P>) : AssignTargetP<P>()
    data class Dot<P : AstPayload>(val expr: AstExprP<P>, val field: AstString) : AssignTargetP<P>()
    // TODO: stub - Identifier needs real import
    data class Identifier<P : AstPayload, IAP>(val ident: AstAssignIdentP<P, IAP>) : AssignTargetP<P>()
}

data class AssignP<P : AstPayload>(
    val lhs: AstAssignTargetP<P>,
    val ty: AstTypeExprP<P>?,
    val rhs: AstExprP<P>
)

data class AssignIdentP<P : AstPayload, IAP>(
    val ident: String,
    val payload: IAP
)

data class IdentP<P : AstPayload, IP>(
    val ident: String,
    val payload: IP
)

data class LoadArgP<P : AstPayload, IAP>(
    val local: AstAssignIdentP<P, IAP>,
    val their: AstString,
    val comma: Spanned<Comma>?
) {
    fun span(): Span = local.span.merge(their.span)
    fun spanWithTrailingComma(): Span = if (comma != null) span().merge(comma.span) else span()
}

data class LoadP<P : AstPayload, LP>(
    val module: AstString,
    val args: List<LoadArgP<P, *>>,
    val payload: LP
)

data class ForClauseP<P : AstPayload>(val varTarget: AstAssignTargetP<P>, val over: AstExprP<P>)

sealed class ClauseP<P : AstPayload> {
    data class For<P : AstPayload>(val forClause: ForClauseP<P>) : ClauseP<P>()
    data class If<P : AstPayload>(val cond: AstExprP<P>) : ClauseP<P>()
}

// TODO: stub - BinOp needs real import
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
    val name: AstAssignIdentP<P, *>,
    val params: List<AstParameterP<P>>,
    val returnType: AstTypeExprP<P>?,
    val body: AstStmtP<P>,
    val payload: DP
) {
    fun signatureSpan(): Span {
        var span = name.span
        for (param in params) span = span.merge(param.span)
        if (returnType != null) span = span.merge(returnType.span)
        return span
    }
}

data class ForP<P : AstPayload>(
    val varTarget: AstAssignTargetP<P>,
    val over: AstExprP<P>,
    val body: AstStmtP<P>
)

data class FStringP<P : AstPayload>(
    val format: AstString,
    val expressions: List<AstExprP<P>>
)

sealed class StmtP<P : AstPayload> {
    class Break<P : AstPayload> : StmtP<P>()
    class Continue<P : AstPayload> : StmtP<P>()
    class Pass<P : AstPayload> : StmtP<P>()
    data class Return<P : AstPayload>(val expr: AstExprP<P>?) : StmtP<P>()
    data class Expression<P : AstPayload>(val expr: AstExprP<P>) : StmtP<P>()
    data class Assign<P : AstPayload>(val assign: AssignP<P>) : StmtP<P>()
    data class AssignModify<P : AstPayload>(val lhs: AstAssignTargetP<P>, val op: AssignOp, val rhs: AstExprP<P>) : StmtP<P>()
    data class Statements<P : AstPayload>(val stmts: List<AstStmtP<P>>) : StmtP<P>()
    data class If<P : AstPayload>(val cond: AstExprP<P>, val suite: AstStmtP<P>) : StmtP<P>()
    data class IfElse<P : AstPayload>(val cond: AstExprP<P>, val suite1: AstStmtP<P>, val suite2: AstStmtP<P>) : StmtP<P>()
    data class For<P : AstPayload>(val forStmt: ForP<P>) : StmtP<P>()
    // TODO: stub - Def needs real import
    data class Def<P : AstPayload, DP>(val defStmt: DefP<P, DP>) : StmtP<P>()
    data class Load<P : AstPayload, LP>(val loadStmt: LoadP<P, LP>) : StmtP<P>()
}
typealias Node<T> = Spanned<T>
