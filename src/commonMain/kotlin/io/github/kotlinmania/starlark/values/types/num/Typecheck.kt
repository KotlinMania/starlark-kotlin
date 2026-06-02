// port-lint: source src/values/types/num/typecheck.rs
package io.github.kotlinmania.starlark.values.types.num

/*
 * Copyright 2019 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.oracle.TypingBinOp
// TyBasic.Any is a data object in the typing module

internal enum class NumTy {
    Int,
    Float,
}

private sealed class NumRhsTy {
    data class Num(
        val value: NumTy,
    ) : NumRhsTy()

    data object Any : NumRhsTy()
}

private fun intOrFloat(): Ty = Ty.union2(Ty.int(), Ty.float())

/** Group of operators sharing the typing behavior. */
private enum class BinOpClass {
    /** If any operand is a float, the result is a float. */
    Add,

    /** Result is always a float. */
    Div,

    /** Only supported for integers. */
    BitAnd,

    /** Not supported. */
    In,

    /** Supported. */
    Less,
}

internal fun typecheckNumBinOp(lhs: NumTy, op: TypingBinOp, rhs: TyBasic): Ty? {
    val rhsTy =
        when {
            rhs == TyBasic.Any -> NumRhsTy.Any
            rhs == TyBasic.int() -> NumRhsTy.Num(NumTy.Int)
            rhs == TyBasic.float() -> NumRhsTy.Num(NumTy.Float)
            else -> return null
        }

    val opClass =
        when (op) {
            TypingBinOp.ADD,
            TypingBinOp.SUB,
            TypingBinOp.MUL,
            TypingBinOp.FLOOR_DIV,
            TypingBinOp.PERCENT,
            -> BinOpClass.Add
            TypingBinOp.DIV -> BinOpClass.Div
            TypingBinOp.BIT_OR,
            TypingBinOp.BIT_XOR,
            TypingBinOp.BIT_AND,
            TypingBinOp.LEFT_SHIFT,
            TypingBinOp.RIGHT_SHIFT,
            -> BinOpClass.BitAnd
            TypingBinOp.IN -> BinOpClass.In
            TypingBinOp.LESS -> BinOpClass.Less
        }

    return when {
        opClass == BinOpClass.In -> null
        opClass == BinOpClass.Less -> Ty.bool()
        lhs == NumTy.Float && opClass == BinOpClass.Add -> Ty.float()
        lhs == NumTy.Int && opClass == BinOpClass.Add && rhsTy is NumRhsTy.Num && rhsTy.value == NumTy.Int -> Ty.int()
        opClass == BinOpClass.Add && rhsTy is NumRhsTy.Num && rhsTy.value == NumTy.Float -> Ty.float()
        opClass == BinOpClass.Add && rhsTy is NumRhsTy.Any -> intOrFloat()
        opClass == BinOpClass.Div -> Ty.float()
        lhs == NumTy.Int && opClass == BinOpClass.BitAnd && (rhsTy is NumRhsTy.Num && rhsTy.value == NumTy.Int || rhsTy is NumRhsTy.Any) -> Ty.int()
        opClass == BinOpClass.BitAnd -> null
        else -> null
    }
}
