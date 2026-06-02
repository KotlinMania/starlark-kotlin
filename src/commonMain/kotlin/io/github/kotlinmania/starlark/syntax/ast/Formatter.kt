package io.github.kotlinmania.starlark.syntax.ast

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

import io.github.kotlinmania.starlark.codemap.Spanned
import io.github.kotlinmania.starlark.syntax.lexer.TokenInt


/** Formats BinOp to Starlark code representation. */
fun BinOp.toSourceString(): String = when (this) {
    BinOp.Or -> " or "
    BinOp.And -> " and "
    BinOp.Equal -> " == "
    BinOp.NotEqual -> " != "
    BinOp.Less -> " < "
    BinOp.Greater -> " > "
    BinOp.LessOrEqual -> " <= "
    BinOp.GreaterOrEqual -> " >= "
    BinOp.In -> " in "
    BinOp.NotIn -> " not in "
    BinOp.Subtract -> " - "
    BinOp.Add -> " + "
    BinOp.Multiply -> " * "
    BinOp.Percent -> " % "
    BinOp.Divide -> " / "
    BinOp.FloorDivide -> " // "
    BinOp.BitAnd -> " & "
    BinOp.BitOr -> " | "
    BinOp.BitXor -> " ^ "
    BinOp.LeftShift -> " << "
    BinOp.RightShift -> " >> "
}

/** Formats AssignOp to Starlark code representation. */
fun AssignOp.toSourceString(): String = when (this) {
    AssignOp.Add -> " += "
    AssignOp.Subtract -> " -= "
    AssignOp.Multiply -> " *= "
    AssignOp.Divide -> " /= "
    AssignOp.FloorDivide -> " //= "
    AssignOp.Percent -> " %= "
    AssignOp.BitAnd -> " &= "
    AssignOp.BitOr -> " |= "
    AssignOp.BitXor -> " ^= "
    AssignOp.LeftShift -> " <<= "
    AssignOp.RightShift -> " >>= "
}

/** Formats TokenInt to Starlark code representation. */
fun TokenInt.toSourceString(): String = when (this) {
    is TokenInt.I32 -> value.toString()
    is TokenInt.BigInt -> value.toString()
}

/** Formats AstLiteral to Starlark code representation. */
fun AstLiteral.toSourceString(): String = when (this) {
    is AstLiteral.Int -> value.node.toSourceString()
    is AstLiteral.Float -> value.node.toString()
    is AstLiteral.String -> {
        val s = value.node
        val sb = StringBuilder()
        sb.append('"')
        for (c in s) {
            when (c) {
                '\n' -> sb.append("\\n")
                '\t' -> sb.append("\\t")
                '\r' -> sb.append("\\r")
                '\u0000' -> sb.append("\\0")
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                else -> sb.append(c)
            }
        }
        sb.append('"')
        sb.toString()
    }
    is AstLiteral.Ellipsis -> "..."
}

/** Formats ArgumentP to Starlark code representation. */
fun <P : AstPayload> ArgumentP<P>.toSourceString(): String = when (this) {
    is ArgumentP.Positional -> expr.node.toSourceString()
    is ArgumentP.Named -> "${name.node} = ${expr.node.toSourceString()}"
    is ArgumentP.Args -> "*${expr.node.toSourceString()}"
    is ArgumentP.KwArgs -> "**${expr.node.toSourceString()}"
}

/** Formats ParameterP to Starlark code representation. */
fun <P : AstPayload> ParameterP<P>.toSourceString(): String = when (this) {
    is ParameterP.Slash -> "/"
    is ParameterP.Normal -> {
        val sb = StringBuilder()
        sb.append(name.node.ident)
        if (typ != null) {
            sb.append(": ").append(typ.node.expr.node.toSourceString())
        }
        if (defaultVal != null) {
            sb.append(" = ").append(defaultVal.node.toSourceString())
        }
        sb.toString()
    }
    is ParameterP.NoArgs -> "*"
    is ParameterP.Args -> {
        val sb = StringBuilder()
        sb.append("*").append(name.node.ident)
        if (typ != null) {
            sb.append(": ").append(typ.node.expr.node.toSourceString())
        }
        sb.toString()
    }
    is ParameterP.KwArgs -> {
        val sb = StringBuilder()
        sb.append("**").append(name.node.ident)
        if (typ != null) {
            sb.append(": ").append(typ.node.expr.node.toSourceString())
        }
        sb.toString()
    }
}

private fun <T> commaSeparatedFmt(list: List<T>, forTuple: Boolean, transform: (T) -> String): String {
    val sb = StringBuilder()
    for (i in list.indices) {
        if (i > 0) sb.append(", ")
        sb.append(transform(list[i]))
    }
    if (list.size == 1 && forTuple) {
        sb.append(",")
    }
    return sb.toString()
}

/** Formats ExprP to Starlark code representation. */
fun <P : AstPayload> ExprP<P>.toSourceString(): String = when (this) {
    is ExprP.Tuple -> "(" + commaSeparatedFmt(elements, true) { it.node.toSourceString() } + ")"
    is ExprP.Dot -> "${expr.node.toSourceString()}.${field.node}"
    is ExprP.Call -> {
        val sb = StringBuilder()
        sb.append(expr.node.toSourceString()).append("(")
        for (i in args.args.indices) {
            if (i > 0) sb.append(", ")
            sb.append(args.args[i].node.toSourceString())
        }
        sb.append(")")
        sb.toString()
    }
    is ExprP.Index -> "${expr.node.toSourceString()}[${index.node.toSourceString()}]"
    is ExprP.Index2 -> "${expr.node.toSourceString()}[${index0.node.toSourceString()}, ${index1.node.toSourceString()}]"
    is ExprP.Slice -> {
        val sb = StringBuilder()
        sb.append(expr.node.toSourceString()).append("[")
        if (start != null) sb.append(start.node.toSourceString())
        sb.append(":")
        if (stop != null) sb.append(stop.node.toSourceString())
        if (step != null) {
            sb.append(":").append(step.node.toSourceString())
        }
        sb.append("]")
        sb.toString()
    }
    is ExprP.Identifier<*, *> -> ident.node.ident
    is ExprP.Lambda<*, *> -> {
        val sb = StringBuilder()
        sb.append("(lambda ")
        sb.append(commaSeparatedFmt(lambda.params, false) { it.node.toSourceString() })
        sb.append(": ")
        sb.append(lambda.body.node.toSourceString())
        sb.append(")")
        sb.toString()
    }
    is ExprP.Literal -> literal.toSourceString()
    is ExprP.Not -> "(not ${expr.node.toSourceString()})"
    is ExprP.Minus -> "-${expr.node.toSourceString()}"
    is ExprP.Plus -> "+${expr.node.toSourceString()}"
    is ExprP.BitNot -> "~${expr.node.toSourceString()}"
    is ExprP.Op -> "(${lhs.node.toSourceString()}${op.toSourceString()}${rhs.node.toSourceString()})"
    is ExprP.If -> "(${v1.node.toSourceString()} if ${cond.node.toSourceString()} else ${v2.node.toSourceString()})"
    is ExprP.ListExpr -> "[" + commaSeparatedFmt(elements, false) { it.node.toSourceString() } + "]"
    is ExprP.Dict -> "{" + commaSeparatedFmt(elements, false) { "${it.first.node.toSourceString()}: ${it.second.node.toSourceString()}" } + "}"
    is ExprP.ListComprehension -> {
        val sb = StringBuilder()
        sb.append("[").append(expr.node.toSourceString())
        sb.append(" for ").append(forClause.varTarget.node.toSourceString()).append(" in ").append(forClause.over.node.toSourceString())
        for (c in clauses) {
            when (c) {
                is ClauseP.For -> sb.append(" for ").append(c.forClause.varTarget.node.toSourceString()).append(" in ").append(c.forClause.over.node.toSourceString())
                is ClauseP.If -> sb.append(" if ").append(c.cond.node.toSourceString())
            }
        }
        sb.append("]")
        sb.toString()
    }
    is ExprP.DictComprehension -> {
        val sb = StringBuilder()
        sb.append("{").append(key.node.toSourceString()).append(": ").append(value.node.toSourceString())
        sb.append(" for ").append(forClause.varTarget.node.toSourceString()).append(" in ").append(forClause.over.node.toSourceString())
        for (c in clauses) {
            when (c) {
                is ClauseP.For -> sb.append(" for ").append(c.forClause.varTarget.node.toSourceString()).append(" in ").append(c.forClause.over.node.toSourceString())
                is ClauseP.If -> sb.append(" if ").append(c.cond.node.toSourceString())
            }
        }
        sb.append("}")
        sb.toString()
    }
    is ExprP.FString -> {
        val sb = StringBuilder()
        sb.append(fstring.node.format.node).append(".format(")
        sb.append(commaSeparatedFmt(fstring.node.expressions, false) { it.node.toSourceString() })
        sb.append(")")
        sb.toString()
    }
}

/** Formats AssignTargetP to Starlark code representation. */
fun <P : AstPayload> AssignTargetP<P>.toSourceString(): String = when (this) {
    is AssignTargetP.Tuple -> "(" + commaSeparatedFmt(elements, true) { it.node.toSourceString() } + ")"
    is AssignTargetP.Dot -> "${expr.node.toSourceString()}.${field.node}"
    is AssignTargetP.Index -> "${expr.node.toSourceString()}[${index.node.toSourceString()}]"
    is AssignTargetP.Identifier<*, *> -> ident.node.ident
}

/** Formats StmtP to Starlark code representation. */
fun <P : AstPayload> StmtP<P>.toSourceString(tab: String = ""): String = when (this) {
    is StmtP.Break -> "${tab}break\n"
    is StmtP.Continue -> "${tab}continue\n"
    is StmtP.Pass -> "${tab}pass\n"
    is StmtP.Return -> {
        if (expr != null) {
            "${tab}return ${expr.node.toSourceString()}\n"
        } else {
            "${tab}return\n"
        }
    }
    is StmtP.Expression -> "${tab}${expr.node.toSourceString()}\n"
    is StmtP.Assign -> {
        val sb = StringBuilder()
        sb.append(tab).append(assign.lhs.node.toSourceString())
        if (assign.ty != null) {
            sb.append(": ").append(assign.ty.node.expr.node.toSourceString())
        }
        sb.append(" = ").append(assign.rhs.node.toSourceString()).append("\n")
        sb.toString()
    }
    is StmtP.AssignModify -> "${tab}${lhs.node.toSourceString()}${op.toSourceString()}${rhs.node.toSourceString()}\n"
    is StmtP.Statements -> {
        val sb = StringBuilder()
        for (s in stmts) {
            sb.append(s.node.toSourceString(tab))
        }
        sb.toString()
    }
    is StmtP.If -> {
        val sb = StringBuilder()
        sb.append(tab).append("if ").append(cond.node.toSourceString()).append(":\n")
        sb.append(suite.node.toSourceString(tab + "  "))
        sb.toString()
    }
    is StmtP.IfElse -> {
        val sb = StringBuilder()
        sb.append(tab).append("if ").append(cond.node.toSourceString()).append(":\n")
        sb.append(suite1.node.toSourceString(tab + "  "))
        sb.append(tab).append("else:\n")
        sb.append(suite2.node.toSourceString(tab + "  "))
        sb.toString()
    }
    is StmtP.For -> {
        val sb = StringBuilder()
        sb.append(tab).append("for ").append(forStmt.varTarget.node.toSourceString()).append(" in ").append(forStmt.over.node.toSourceString()).append(":\n")
        sb.append(forStmt.body.node.toSourceString(tab + "  "))
        sb.toString()
    }
    is StmtP.Def<*, *> -> {
        val sb = StringBuilder()
        sb.append(tab).append("def ").append(def.name.node.ident).append("(")
        sb.append(commaSeparatedFmt(def.params, false) { it.node.toSourceString() })
        sb.append(")")
        if (def.returnType != null) {
            sb.append(" -> ").append(def.returnType.node.expr.node.toSourceString())
        }
        sb.append(":\n")
        sb.append(def.body.node.toSourceString(tab + "  "))
        sb.toString()
    }
    is StmtP.Load<*, *> -> {
        val sb = StringBuilder()
        sb.append(tab).append("load(")
        val m = loadStmt.module.node
        sb.append('"').append(m).append('"')
        for (arg in loadStmt.args) {
            sb.append(", ").append(arg.local.node.ident).append(" = ")
            sb.append('"').append(arg.their.node).append('"')
        }
        sb.append(")\n")
        sb.toString()
    }
}

/** Formats Spanned<ExprP> to Starlark code representation. */
fun <P : AstPayload> Spanned<ExprP<P>>.toSourceString(): String = node.toSourceString()

/** Formats Spanned<StmtP> to Starlark code representation. */
fun <P : AstPayload> Spanned<StmtP<P>>.toSourceString(tab: String = ""): String = node.toSourceString(tab)
