// port-lint: source ../starlark_syntax/src/syntax/validate.rs
package io.github.kotlinmania.starlark.syntax

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

import io.github.kotlinmania.starlark.syntax.dialect.DialectTypes
import io.github.kotlinmania.starlark.syntax.state.ParserState
import io.github.kotlinmania.starlark.typing.EvalException
import io.github.kotlinmania.starlark.syntax.ast.AssignTargetP as ValidateAssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstAssignTargetP as ValidateAstAssignTargetP
import io.github.kotlinmania.starlark.syntax.ast.AstExprP as ValidateAstExprP
import io.github.kotlinmania.starlark.syntax.ast.AstLiteral as ValidateAstLiteral
import io.github.kotlinmania.starlark.syntax.ast.AstParameterP as ValidateAstParameterP
import io.github.kotlinmania.starlark.syntax.ast.AstStmtP as ValidateAstStmtP
import io.github.kotlinmania.starlark.syntax.ast.ClauseP as ValidateClauseP
import io.github.kotlinmania.starlark.syntax.ast.ExprP as ValidateExprP
import io.github.kotlinmania.starlark.syntax.ast.ForClauseP as ValidateForClauseP
import io.github.kotlinmania.starlark.syntax.ast.ParameterP as ValidateParameterP
import io.github.kotlinmania.starlark.syntax.ast.StmtP as ValidateStmtP

private enum class ParameterState {
    Normal,
    SeenSlash,
    SeenStar,
    SeenStarStar,
}

internal fun validateParams(params: List<ValidateAstParameterP<*>>, parserState: ParserState) {
    val codemap = parserState.codemap
    if (!parserState.dialect.enableKeywordOnlyArguments) {
        for (param in params) {
            if (param.node is ValidateParameterP.NoArgs<*>) {
                parserState.error(
                    param.span,
                    "* keyword-only-arguments is not allowed in this dialect",
                )
            }
        }
    }
    if (!parserState.dialect.enablePositionalOnlyArguments) {
        for (param in params) {
            if (param.node is ValidateParameterP.Slash<*>) {
                parserState.error(
                    param.span,
                    "/ positional-only-arguments is not allowed in this dialect",
                )
            }
        }
    }

    // Now implement the DefParams::unpack validation:
    // you can't repeat argument names
    val argset = mutableSetOf<String>()
    // You can't have more than one *args/*, **kwargs
    // **kwargs must be last
    // You can't have a required `x` after an optional `y=1`
    var seenOptional = false

    var argsSeen = false
    var kwargsSeen = false
    var indexStar: Int? = null

    val slashIndex = params.indexOfFirst { it.node is ValidateParameterP.Slash<*> }
    if (slashIndex == 0) {
        parserState.errors.add(
            EvalException.parserError(
                "`/` cannot be first parameter",
                params[0].span,
                codemap,
            ),
        )
        return
    }

    var state =
        if (slashIndex == -1) {
            ParameterState.SeenSlash
        } else {
            ParameterState.Normal
        }

    for ((i, param) in params.withIndex()) {
        val name = param.node.ident()
        if (name != null) {
            if (!argset.add(name.node.ident)) {
                parserState.errors.add(
                    EvalException.parserError(
                        "duplicated parameter name",
                        param.span,
                        codemap,
                    ),
                )
                return
            }
        }

        when (val node = param.node) {
            is ValidateParameterP.Normal<*> -> {
                if (state >= ParameterState.SeenStarStar) {
                    parserState.errors.add(
                        EvalException.parserError(
                            "Parameter after kwargs",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                if (node.defaultVal == null) {
                    if (seenOptional && state < ParameterState.SeenStar) {
                        parserState.errors.add(
                            EvalException.parserError(
                                "positional parameter after non positional",
                                param.span,
                                codemap,
                            ),
                        )
                        return
                    }
                } else {
                    seenOptional = true
                }
            }
            is ValidateParameterP.NoArgs<*> -> {
                if (state >= ParameterState.SeenStar) {
                    parserState.errors.add(
                        EvalException.parserError(
                            "Args parameter after another args or kwargs parameter",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                state = ParameterState.SeenStar
                indexStar = i
            }
            is ValidateParameterP.Slash<*> -> {
                if (state >= ParameterState.SeenSlash) {
                    parserState.errors.add(
                        EvalException.parserError(
                            "Multiple `/` in parameters",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                state = ParameterState.SeenSlash
            }
            is ValidateParameterP.Args<*> -> {
                if (state >= ParameterState.SeenStar) {
                    parserState.errors.add(
                        EvalException.parserError(
                            "Args parameter after another args or kwargs parameter",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                state = ParameterState.SeenStar
                if (argsSeen) {
                    parserState.errors.add(
                        EvalException.internalError(
                            "Multiple *args",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                argsSeen = true
            }
            is ValidateParameterP.KwArgs<*> -> {
                if (state >= ParameterState.SeenStarStar) {
                    parserState.errors.add(
                        EvalException.parserError(
                            "Multiple kwargs dictionary in parameters",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                if (kwargsSeen) {
                    parserState.errors.add(
                        EvalException.internalError(
                            "Multiple **kwargs",
                            param.span,
                            codemap,
                        ),
                    )
                    return
                }
                kwargsSeen = true
                state = ParameterState.SeenStarStar
            }
        }
    }

    if (indexStar != null) {
        val next = params.getOrNull(indexStar + 1)
        if (next == null) {
            parserState.errors.add(
                EvalException.parserError(
                    "`*` parameter must not be last",
                    params[indexStar].span,
                    codemap,
                ),
            )
            return
        }
        when (next.node) {
            is ValidateParameterP.Normal<*> -> {}
            else -> {
                parserState.errors.add(
                    EvalException.parserError(
                        "`*` must be followed by named parameter",
                        next.span,
                        codemap,
                    ),
                )
                return
            }
        }
    }
}

private fun walkExpr(expr: ValidateAstExprP<*>, f: (ValidateAstExprP<*>) -> Unit) {
    f(expr)
    when (val node = expr.node) {
        is ValidateExprP.Tuple<*> -> {
            for (elem in node.elements) {
                walkExpr(elem, f)
            }
        }
        is ValidateExprP.Dot<*> -> {
            walkExpr(node.expr, f)
        }
        is ValidateExprP.Call<*> -> {
            walkExpr(node.expr, f)
            for (arg in node.args.args) {
                walkExpr(arg.node.expr(), f)
            }
        }
        is ValidateExprP.Index<*> -> {
            walkExpr(node.expr, f)
            walkExpr(node.index, f)
        }
        is ValidateExprP.Index2<*> -> {
            walkExpr(node.expr, f)
            walkExpr(node.index0, f)
            walkExpr(node.index1, f)
        }
        is ValidateExprP.Slice<*> -> {
            walkExpr(node.expr, f)
            node.start?.let { walkExpr(it, f) }
            node.stop?.let { walkExpr(it, f) }
            node.step?.let { walkExpr(it, f) }
        }
        is ValidateExprP.Identifier<*, *> -> {}
        is ValidateExprP.Lambda<*, *> -> {
            for (param in node.lambda.params) {
                when (val p = param.node) {
                    is ValidateParameterP.Normal<*> -> {
                        p.typ?.let { walkExpr(it.node.expr, f) }
                        p.defaultVal?.let { walkExpr(it, f) }
                    }
                    is ValidateParameterP.Args<*> -> {
                        p.typ?.let { walkExpr(it.node.expr, f) }
                    }
                    is ValidateParameterP.KwArgs<*> -> {
                        p.typ?.let { walkExpr(it.node.expr, f) }
                    }
                    else -> {}
                }
            }
            walkExpr(node.lambda.body, f)
        }
        is ValidateExprP.Literal<*> -> {}
        is ValidateExprP.Not<*> -> walkExpr(node.expr, f)
        is ValidateExprP.Minus<*> -> walkExpr(node.expr, f)
        is ValidateExprP.Plus<*> -> walkExpr(node.expr, f)
        is ValidateExprP.BitNot<*> -> walkExpr(node.expr, f)
        is ValidateExprP.Op<*> -> {
            walkExpr(node.lhs, f)
            walkExpr(node.rhs, f)
        }
        is ValidateExprP.If<*> -> {
            walkExpr(node.cond, f)
            walkExpr(node.v1, f)
            walkExpr(node.v2, f)
        }
        is ValidateExprP.ListExpr<*> -> {
            for (elem in node.elements) {
                walkExpr(elem, f)
            }
        }
        is ValidateExprP.Dict<*> -> {
            for ((k, v) in node.elements) {
                walkExpr(k, f)
                walkExpr(v, f)
            }
        }
        is ValidateExprP.ListComprehension<*> -> {
            walkExpr(node.expr, f)
            walkForClause(node.forClause, f)
            for (clause in node.clauses) {
                walkClause(clause, f)
            }
        }
        is ValidateExprP.DictComprehension<*> -> {
            walkExpr(node.key, f)
            walkExpr(node.value, f)
            walkForClause(node.forClause, f)
            for (clause in node.clauses) {
                walkClause(clause, f)
            }
        }
        is ValidateExprP.FString<*> -> {
            for (exprItem in node.fstring.node.expressions) {
                walkExpr(exprItem, f)
            }
        }
    }
}

private fun walkAssignTarget(target: ValidateAstAssignTargetP<*>, f: (ValidateAstExprP<*>) -> Unit) {
    when (val node = target.node) {
        is ValidateAssignTargetP.Tuple<*> -> {
            for (elem in node.elements) {
                walkAssignTarget(elem, f)
            }
        }
        is ValidateAssignTargetP.Index<*> -> {
            walkExpr(node.expr, f)
            walkExpr(node.index, f)
        }
        is ValidateAssignTargetP.Dot<*> -> {
            walkExpr(node.expr, f)
        }
        is ValidateAssignTargetP.Identifier<*, *> -> {}
    }
}

private fun walkForClause(fc: ValidateForClauseP<*>, f: (ValidateAstExprP<*>) -> Unit) {
    walkAssignTarget(fc.varTarget, f)
    walkExpr(fc.over, f)
}

private fun walkClause(c: ValidateClauseP<*>, f: (ValidateAstExprP<*>) -> Unit) {
    when (c) {
        is ValidateClauseP.For<*> -> walkForClause(c.forClause, f)
        is ValidateClauseP.If<*> -> walkExpr(c.cond, f)
    }
}

private fun walkExprsInStmt(stmt: ValidateAstStmtP<*>, f: (ValidateAstExprP<*>) -> Unit) {
    when (val node = stmt.node) {
        is ValidateStmtP.Expression<*> -> walkExpr(node.expr, f)
        is ValidateStmtP.Return<*> -> node.expr?.let { walkExpr(it, f) }
        is ValidateStmtP.Assign<*> -> {
            walkAssignTarget(node.assign.lhs, f)
            node.assign.ty?.let { walkExpr(it.node.expr, f) }
            walkExpr(node.assign.rhs, f)
        }
        is ValidateStmtP.AssignModify<*> -> {
            walkAssignTarget(node.lhs, f)
            walkExpr(node.rhs, f)
        }
        is ValidateStmtP.If<*> -> walkExpr(node.cond, f)
        is ValidateStmtP.IfElse<*> -> walkExpr(node.cond, f)
        is ValidateStmtP.For<*> -> {
            walkAssignTarget(node.forStmt.varTarget, f)
            walkExpr(node.forStmt.over, f)
        }
        is ValidateStmtP.Def<*, *> -> {
            for (param in node.def.params) {
                when (val p = param.node) {
                    is ValidateParameterP.Normal<*> -> {
                        p.typ?.let { walkExpr(it.node.expr, f) }
                        p.defaultVal?.let { walkExpr(it, f) }
                    }
                    is ValidateParameterP.Args<*> -> {
                        p.typ?.let { walkExpr(it.node.expr, f) }
                    }
                    is ValidateParameterP.KwArgs<*> -> {
                        p.typ?.let { walkExpr(it.node.expr, f) }
                    }
                    else -> {}
                }
            }
            node.def.returnType?.let { walkExpr(it.node.expr, f) }
        }
        else -> {}
    }
}

internal fun validateModule(stmt: ValidateAstStmtP<*>, parserState: ParserState) {
    fun f(
        stmt: ValidateAstStmtP<*>,
        topLevel: Boolean,
        insideFor: Boolean,
        insideDef: Boolean,
    ) {
        val span = stmt.span
        when (val node = stmt.node) {
            is ValidateStmtP.Def<*, *> -> {
                if (!parserState.dialect.enableDef) {
                    parserState.error(span, "`def` is not allowed in this dialect")
                }
                validateParams(node.def.params, parserState)
                f(node.def.body, topLevel = false, insideFor = false, insideDef = true)
            }
            is ValidateStmtP.For<*> -> {
                if (topLevel && !parserState.dialect.enableTopLevelStmt) {
                    parserState.error(span, "`for` cannot be used outside `def` in this dialect")
                } else {
                    f(node.forStmt.body, topLevel = false, insideFor = true, insideDef = insideDef)
                }
            }
            is ValidateStmtP.If<*> -> {
                if (topLevel && !parserState.dialect.enableTopLevelStmt) {
                    parserState.error(span, "`if` cannot be used outside `def` in this dialect")
                } else {
                    f(node.suite, topLevel = false, insideFor = insideFor, insideDef = insideDef)
                }
            }
            is ValidateStmtP.IfElse<*> -> {
                if (topLevel && !parserState.dialect.enableTopLevelStmt) {
                    parserState.error(span, "`if` cannot be used outside `def` in this dialect")
                } else {
                    f(node.suite1, topLevel = false, insideFor = insideFor, insideDef = insideDef)
                    f(node.suite2, topLevel = false, insideFor = insideFor, insideDef = insideDef)
                }
            }
            is ValidateStmtP.Break<*> -> {
                if (!insideFor) {
                    parserState.error(span, "`break` cannot be used outside of a `for` loop")
                }
            }
            is ValidateStmtP.Continue<*> -> {
                if (!insideFor) {
                    parserState.error(span, "`continue` cannot be used outside of a `for` loop")
                }
            }
            is ValidateStmtP.Return<*> -> {
                if (!insideDef) {
                    parserState.error(span, "`return` cannot be used outside of a `def` function")
                }
            }
            is ValidateStmtP.Load<*, *> -> {
                if (!topLevel) {
                    parserState.error(span, "`load` must only occur at the top of a module")
                }
                if (!parserState.dialect.enableLoad) {
                    parserState.error(span, "`load` is not allowed in this dialect")
                }
            }
            is ValidateStmtP.Statements<*> -> {
                for (s in node.stmts) {
                    f(s, topLevel, insideFor, insideDef)
                }
            }
            else -> {}
        }
    }

    fun expr(x: ValidateAstExprP<*>) {
        when (val node = x.node) {
            is ValidateExprP.Literal<*> -> {
                if (node.literal is ValidateAstLiteral.Ellipsis) {
                    if (parserState.dialect.enableTypes == DialectTypes.Disable) {
                        parserState.error(x.span, "`...` is not allowed in this dialect")
                    }
                }
            }
            is ValidateExprP.Lambda<*, *> -> {
                if (!parserState.dialect.enableLambda) {
                    parserState.error(x.span, "`lambda` is not allowed in this dialect")
                }
                validateParams(node.lambda.params, parserState)
            }
            else -> {}
        }
    }

    // Walk all statements
    f(stmt, topLevel = true, insideFor = false, insideDef = false)

    // Walk all expressions inside the statements
    fun walkStmtExprs(s: ValidateAstStmtP<*>) {
        walkExprsInStmt(s) { e ->
            walkExpr(e) { expr(it) }
        }
        when (val node = s.node) {
            is ValidateStmtP.Def<*, *> -> walkStmtExprs(node.def.body)
            is ValidateStmtP.For<*> -> walkStmtExprs(node.forStmt.body)
            is ValidateStmtP.If<*> -> walkStmtExprs(node.suite)
            is ValidateStmtP.IfElse<*> -> {
                walkStmtExprs(node.suite1)
                walkStmtExprs(node.suite2)
            }
            is ValidateStmtP.Statements<*> -> {
                for (child in node.stmts) {
                    walkStmtExprs(child)
                }
            }
            else -> {}
        }
    }
    walkStmtExprs(stmt)
}
