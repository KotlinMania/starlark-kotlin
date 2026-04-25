// port-lint: source starlark_syntax/src/syntax/type_expr.rs
package io.github.kotlinmania.starlark.syntax.typeexpr

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

import io.github.kotlinmania.starlark.codemap.CodeMap
import io.github.kotlinmania.starlark.codemap.Span
import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral
import io.github.kotlinmania.starlark.syntax.ast.AstPayload
import io.github.kotlinmania.starlark.syntax.ast.BinOp
import io.github.kotlinmania.starlark.syntax.ast.ExprP
import io.github.kotlinmania.starlark.syntax.ast.IdentP
import io.github.kotlinmania.starlark.typing.WithDiagnostic

// #[derive(Debug, thiserror::Error)]
// pub enum TypeExprUnpackError
sealed class TypeExprUnpackError(message: String) : Exception(message) {
    // #[error("{0} expression is not allowed in type expression")]
    class InvalidType(val type_: String) : TypeExprUnpackError("$type_ expression is not allowed in type expression")
    // #[error("Empty list is not allowed in type expression")]
    class EmptyListInType : TypeExprUnpackError("Empty list is not allowed in type expression")
    // #[error("Only dot expression of form `ident.ident` is allowed in type expression")]
    class DotInType : TypeExprUnpackError("Only dot expression of form `ident.ident` is allowed in type expression")
    // #[error("Expecting path like `a.b.c`")]
    class ExpectingPath : TypeExprUnpackError("Expecting path like `a.b.c`")
    // #[error(r#"`{0}.type` is not allowed in type expression, use `{0}` instead"#)]
    class DotTypeBan(val name: String) : TypeExprUnpackError("`$name.type` is not allowed in type expression, use `$name` instead")
}

/**
 * Types that are `""` or start with `"_"` are wildcard - they match everything
 * (also deprecated).
 */
// pub fn type_str_literal_is_wildcard(s: &str) -> bool
fun typeStrLiteralIsWildcard(s: String): Boolean {
    return s == "" || s.startsWith('_')
}

/** Path component of type. */
// #[derive(Debug)]
// pub struct TypePathP<'a, P: AstPayload>
data class TypePathP<P : AstPayload, IP>(
    val first: Spanned<IdentP<P, IP>>,
    val rem: List<Spanned<String>>,
)

/** This type should be used instead of `TypeExprP`, but a lot of code needs to be updated. */
// #[derive(Debug)]
// pub enum TypeExprUnpackP<'a, P: AstPayload>
sealed class TypeExprUnpackP<P : AstPayload, IP> {
    // Ellipsis
    class Ellipsis<P : AstPayload, IP> : TypeExprUnpackP<P, IP>()
    // Path(TypePathP<'a, P>)
    data class Path<P : AstPayload, IP>(val path: TypePathP<P, IP>) : TypeExprUnpackP<P, IP>()
    /** `list[str]`. */
    // Index(&'a Spanned<IdentP<P>>, Box<Spanned<TypeExprUnpackP<'a, P>>>)
    data class Index<P : AstPayload, IP>(val ident: Spanned<IdentP<P, IP>>, val index: Spanned<TypeExprUnpackP<P, IP>>) : TypeExprUnpackP<P, IP>()
    /** `dict[str, int]` or `typing.Callable[[int], str]`. */
    // Index2(Spanned<TypePathP<'a, P>>, Box<Spanned<TypeExprUnpackP<'a, P>>>, Box<Spanned<TypeExprUnpackP<'a, P>>>)
    data class Index2<P : AstPayload, IP>(val path: Spanned<TypePathP<P, IP>>, val i0: Spanned<TypeExprUnpackP<P, IP>>, val i1: Spanned<TypeExprUnpackP<P, IP>>) : TypeExprUnpackP<P, IP>()
    /** List argument in `typing.Callable[[int], str]`. */
    // List(Vec<Spanned<TypeExprUnpackP<'a, P>>>)
    data class List<P : AstPayload, IP>(val items: kotlin.collections.List<Spanned<TypeExprUnpackP<P, IP>>>) : TypeExprUnpackP<P, IP>()
    // Union(Vec<Spanned<TypeExprUnpackP<'a, P>>>)
    data class Union<P : AstPayload, IP>(val xs: kotlin.collections.List<Spanned<TypeExprUnpackP<P, IP>>>) : TypeExprUnpackP<P, IP>()
    // Tuple(Vec<Spanned<TypeExprUnpackP<'a, P>>>)
    data class Tuple<P : AstPayload, IP>(val xs: kotlin.collections.List<Spanned<TypeExprUnpackP<P, IP>>>) : TypeExprUnpackP<P, IP>()

    companion object {
        // fn unpack_path(expr: &'a Spanned<ExprP<P>>, codemap: &CodeMap) -> Result<Spanned<TypePathP<'a, P>>, WithDiagnostic<TypeExprUnpackError>>
        private fun <P : AstPayload, IP> unpackPath(
            expr: Spanned<ExprP<P>>,
            codemap: CodeMap,
        ): Spanned<TypePathP<P, IP>> {
            val span = expr.span
            return when (val node = expr.node) {
                is ExprP.Identifier<P, *> -> Spanned(
                    node = TypePathP(
                        first = identAsIp<P, IP>(node.ident),
                        rem = emptyList(),
                    ),
                    span = span,
                )
                is ExprP.Dot -> {
                    var current: Spanned<ExprP<P>> = node.expr
                    val rem = mutableListOf(Spanned(node = node.field.node, span = node.field.span))
                    while (true) {
                        when (val cur = current.node) {
                            is ExprP.Dot -> {
                                current = cur.expr
                                rem.add(Spanned(node = cur.field.node, span = cur.field.span))
                            }
                            is ExprP.Identifier<P, *> -> {
                                rem.reverse()
                                val last = rem.lastOrNull()
                                if (last != null && last.node == "type") {
                                    val butLast = rem.dropLast(1)
                                    var fullPath = cur.ident.node.ident
                                    for (elem in butLast) {
                                        fullPath += ".${elem.node}"
                                    }
                                    throw WithDiagnosticException(
                                        WithDiagnostic(
                                            TypeExprUnpackError.DotTypeBan(fullPath),
                                            current.span,
                                            codemap,
                                        )
                                    )
                                }
                                return Spanned(
                                    node = TypePathP(first = identAsIp<P, IP>(cur.ident), rem = rem),
                                    span = span,
                                )
                            }
                            else -> throw WithDiagnosticException(
                                WithDiagnostic(
                                    TypeExprUnpackError.DotInType(),
                                    current.span,
                                    codemap,
                                )
                            )
                        }
                    }
                    @Suppress("UNREACHABLE_CODE")
                    throw IllegalStateException("unreachable")
                }
                else -> throw WithDiagnosticException(
                    WithDiagnostic(
                        TypeExprUnpackError.ExpectingPath(),
                        expr.span,
                        codemap,
                    )
                )
            }
        }

        // fn unpack_argument(expr: &'a Spanned<ExprP<P>>, codemap: &CodeMap) -> Result<Spanned<TypeExprUnpackP<'a, P>>, WithDiagnostic<TypeExprUnpackError>>
        private fun <P : AstPayload, IP> unpackArgument(
            expr: Spanned<ExprP<P>>,
            codemap: CodeMap,
        ): Spanned<TypeExprUnpackP<P, IP>> {
            val span = expr.span
            return when (val node = expr.node) {
                is ExprP.ListExpr -> {
                    val items = node.elements.map { x ->
                        unpackArgument<P, IP>(x, codemap)
                    }
                    Spanned(
                        node = List(items),
                        span = span,
                    )
                }
                else -> unpack(expr, codemap)
            }
        }

        // pub fn unpack(expr: &'a Spanned<ExprP<P>>, codemap: &CodeMap) -> Result<Spanned<TypeExprUnpackP<'a, P>>, WithDiagnostic<TypeExprUnpackError>>
        fun <P : AstPayload, IP> unpack(
            expr: Spanned<ExprP<P>>,
            codemap: CodeMap,
        ): Spanned<TypeExprUnpackP<P, IP>> {
            val span = expr.span
            fun err(t: String): Nothing {
                throw WithDiagnosticException(
                    WithDiagnostic(
                        TypeExprUnpackError.InvalidType(t),
                        expr.span,
                        codemap,
                    )
                )
            }

            return when (val node = expr.node) {
                is ExprP.Tuple -> {
                    val xs = node.elements.map { x ->
                        unpack<P, IP>(x, codemap)
                    }
                    Spanned(node = Tuple(xs), span = span)
                }
                is ExprP.Dot -> {
                    val path = unpackPath<P, IP>(expr, codemap)
                    Spanned(node = Path(path.node), span = span)
                }
                is ExprP.Call -> err("call")
                is ExprP.Index -> {
                    val a = node.expr
                    val i = node.index
                    when (val aNode = a.node) {
                        is ExprP.Identifier<P, *> -> {
                            val unpacked = unpack<P, IP>(i, codemap)
                            Spanned(
                                node = Index(identAsIp<P, IP>(aNode.ident), unpacked),
                                span = span,
                            )
                        }
                        else -> err("array indirection where array is not an identifier")
                    }
                }
                is ExprP.Index2 -> {
                    val a = node.expr
                    val i0 = node.index0
                    val i1 = node.index1
                    val path = unpackPath<P, IP>(a, codemap)
                    val unpackedI0 = unpackArgument<P, IP>(i0, codemap)
                    val unpackedI1 = unpackArgument<P, IP>(i1, codemap)
                    Spanned(
                        node = Index2(path, unpackedI0, unpackedI1),
                        span = span,
                    )
                }
                is ExprP.Slice -> err("slice")
                is ExprP.Identifier<P, *> -> {
                    val path = unpackPath<P, IP>(expr, codemap)
                    Spanned(node = Path(path.node), span = span)
                }
                is ExprP.Lambda<P, *> -> err("lambda")
                is ExprP.Literal -> when (node.literal) {
                    is AstLiteral.String -> err("string literal")
                    is AstLiteral.Int -> err("int")
                    is AstLiteral.Float -> err("float")
                    is AstLiteral.Ellipsis -> Spanned(node = Ellipsis(), span = span)
                }
                is ExprP.Not -> err("not")
                is ExprP.Minus -> err("minus")
                is ExprP.Plus -> err("plus")
                is ExprP.BitNot -> err("bit not")
                is ExprP.Op -> {
                    if (node.op == BinOp.BitOr) {
                        val a = unpack<P, IP>(node.lhs, codemap)
                        val b = unpack<P, IP>(node.rhs, codemap)
                        Spanned(node = Union(listOf(a, b)), span = span)
                    } else {
                        err("bin op except `|`")
                    }
                }
                is ExprP.If -> err("if")
                is ExprP.ListExpr -> {
                    val xs = node.elements
                    if (xs.isEmpty()) {
                        throw WithDiagnosticException(
                            WithDiagnostic(
                                TypeExprUnpackError.EmptyListInType(),
                                expr.span,
                                codemap,
                            )
                        )
                    } else if (xs.size == 1) {
                        err("list of 1 element")
                    } else {
                        val unpacked = xs.map { x -> unpack<P, IP>(x, codemap) }
                        Spanned(node = Union(unpacked), span = span)
                    }
                }
                is ExprP.Dict -> err("dict")
                is ExprP.ListComprehension -> err("list comprehension")
                is ExprP.DictComprehension -> err("dict comprehension")
                is ExprP.FString -> err("f-string")
            }
        }

        // Centralized IP-cast helper. The IP type parameter is unconstrained at the
        // ExprP level (Identifier carries IdentP<P, *>); callers assert IP via this
        // narrow one-line helper rather than file/function-level suppressions.
        @Suppress("UNCHECKED_CAST")
        private fun <P : AstPayload, IP> identAsIp(ident: Spanned<IdentP<P, *>>): Spanned<IdentP<P, IP>> =
            ident as Spanned<IdentP<P, IP>>
    }
}

/**
 * Exception wrapper for WithDiagnostic results.
 * Used to convert Rust's Result<_, WithDiagnostic<E>> pattern to Kotlin exceptions.
 */
class WithDiagnosticException(
    val diagnostic: WithDiagnostic<TypeExprUnpackError>,
) : Exception(diagnostic.value.message)
