// port-lint: source src/eval/bc/compiler/call.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.compiler

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

/// Compile function calls.

import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlotIn
import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.call.BcCallArgsFull
import io.github.kotlinmania.starlark_kotlin.eval.bc.call.BcCallArgsPos
import io.github.kotlinmania.starlark_kotlin.eval.bc.native_function.BcNativeFunction
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcWriter
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.CallCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.NativeFunction
import io.github.kotlinmania.starlark_kotlin.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.types.getKnownMethod
import io.github.kotlinmania.starlark_kotlin.values.layout.newFrozen
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.writeExprs
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.writeExprOpt
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.writeBcCb
import io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.assign.markDefinitelyAssignedAfter
import io.github.kotlinmania.starlark_kotlin.eval.bc.call.resolve
import io.github.kotlinmania.starlark_kotlin.syntax.ast.Expr
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped

// impl ArgsCompiledValue

/// After evaluation of function arguments like `foo(a, b=c[d], **e)`,
/// variables `a`, `b`, `c`, `d`, and `e` are definitely assigned.
// fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun ArgsCompiledValue.markDefinitelyAssignedAfterCall(bc: BcWriter) {
    for (n in posNamed) {
        n.markDefinitelyAssignedAfter(bc)
    }
    @Suppress("UNUSED_VARIABLE")
    val unused = names
    args?.markDefinitelyAssignedAfter(bc)
    kwargs?.markDefinitelyAssignedAfter(bc)
}

// fn write_bc(&self, bc: &mut BcWriter, k: impl FnOnce(BcCallArgsFull<Symbol>, &mut BcWriter))
private fun ArgsCompiledValue.writeBc(bc: BcWriter, k: (BcCallArgsFull<Symbol>, BcWriter) -> Unit) {
    writeExprs(posNamed, bc) { posNamed, bc2 ->
        writeExprOpt(args, bc2) { argsSlot, bc3 ->
            writeExprOpt(kwargs, bc3) { kwargsSlot, bc4 ->
                val argsFull = BcCallArgsFull(
                    posNamed = posNamed,
                    names = names.toList(),
                    args = argsSlot,
                    kwargs = kwargsSlot,
                )
                k(argsFull, bc4)
            }
        }
    }
}

// impl CallCompiled

/// After evaluation of call like `a[b](c.d)`,
/// variables `a`, `b`, and `c` are definitely assigned.
// pub(crate) fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun CallCompiled.markDefinitelyAssignedAfterCall(bc: BcWriter) {
    fun_.markDefinitelyAssignedAfter(bc)
    args.markDefinitelyAssignedAfterCall(bc)
}

// impl IrSpanned<CallCompiled>

// fn write_args(args, bc, k)
private fun writeArgs(
    args: ArgsCompiledValue,
    bc: BcWriter,
    k: (Either<BcCallArgsPos, BcCallArgsFull<Symbol>>, BcWriter) -> Unit,
) {
    val pos = args.posOnly()
    if (pos != null) {
        writeExprs(pos, bc) { posSlots, bc2 ->
            val argsPos = Either.Left<BcCallArgsPos, BcCallArgsFull<Symbol>>(BcCallArgsPos(posSlots))
            k(argsPos, bc2)
        }
    } else {
        args.writeBc(bc) { argsFull, bc2 ->
            val argsFull2 = Either.Right<BcCallArgsPos, BcCallArgsFull<Symbol>>(argsFull)
            k(argsFull2, bc2)
        }
    }
}

// fn write_call_frozen(span, fun, args, target, bc)
private fun writeCallFrozen(
    span: FrameSpan,
    fun_: FrozenValue,
    args: ArgsCompiledValue,
    target: BcSlotOut,
    bc: BcWriter,
) {
    val fileSpan = bc.allocFileSpan(span)
    val frozenDef = FrozenValueTyped.new<FrozenDef>(fun_)
    if (frozenDef != null) {
        writeArgs(args, bc) { callArgs, bc2 ->
            when (callArgs) {
                is Either.Left -> {
                    bc2.writeInstr("CallFrozenDefPos", span, listOf(frozenDef, callArgs.value, fileSpan, target))
                }
                is Either.Right -> {
                    bc2.writeInstr("CallFrozenDef", span, listOf(frozenDef, callArgs.value.resolve(frozenDef), fileSpan, target))
                }
            }
        }
        return
    }
    val nativeFunc = FrozenValueTyped.new<NativeFunction>(fun_)
    if (nativeFunc != null) {
        val bcNative = BcNativeFunction.new(nativeFunc)
        writeArgs(args, bc) { callArgs, bc2 ->
            when (callArgs) {
                is Either.Left -> {
                    bc2.writeInstr("CallFrozenNativePos", span, listOf(bcNative, callArgs.value, fileSpan, target))
                }
                is Either.Right -> {
                    bc2.writeInstr("CallFrozenNative", span, listOf(bcNative, callArgs.value, fileSpan, target))
                }
            }
        }
        return
    }
    // Generic frozen call.
    writeArgs(args, bc) { callArgs, bc2 ->
        when (callArgs) {
            is Either.Left -> {
                bc2.writeInstr("CallFrozenPos", span, listOf(fun_, callArgs.value, fileSpan, target))
            }
            is Either.Right -> {
                bc2.writeInstr("CallFrozen", span, listOf(fun_, callArgs.value, fileSpan, target))
            }
        }
    }
}

// fn write_call_method(target, span, this, symbol, args, bc)
private fun writeCallMethod(
    target: BcSlotOut,
    span: FrameSpan,
    this_: IrSpanned<ExprCompiled>,
    symbol: Symbol,
    args: ArgsCompiledValue,
    bc: BcWriter,
) {
    this_.writeBcCb(bc) { thisSlot, bc2 ->
        val fileSpan = bc2.allocFileSpan(span)
        val knownMethod = getKnownMethod(symbol.asStr())
        val pos = args.posOnly()
        if (pos != null) {
            writeExprs(pos, bc2) { posSlots, bc3 ->
                if (knownMethod != null) {
                    bc3.writeInstr(
                        "CallMaybeKnownMethodPos",
                        span,
                        listOf(thisSlot, symbol, knownMethod, BcCallArgsPos(posSlots), fileSpan, target),
                    )
                } else {
                    bc3.writeInstr(
                        "CallMethodPos",
                        span,
                        listOf(thisSlot, symbol, BcCallArgsPos(posSlots), fileSpan, target),
                    )
                }
            }
        } else {
            args.writeBc(bc2) { argsFull, bc3 ->
                if (knownMethod != null) {
                    bc3.writeInstr(
                        "CallMaybeKnownMethod",
                        span,
                        listOf(thisSlot, symbol, knownMethod, argsFull, fileSpan, target),
                    )
                } else {
                    bc3.writeInstr(
                        "CallMethod",
                        span,
                        listOf(thisSlot, symbol, argsFull, fileSpan, target),
                    )
                }
            }
        }
    }
}

/// Compile a call expression to bytecode.
// pub(crate) fn write_bc(&self, target: BcSlotOut, bc: &mut BcWriter)
internal fun IrSpanned<CallCompiled>.writeBcCall(target: BcSlotOut, bc: BcWriter) {
    val call = this.node

    // Special-case: len(x)
    val lenArg = CallCompiled.asLen(call)
    if (lenArg != null) {
        lenArg.writeBcCb(bc) { argSlot, bc2 ->
            bc2.writeInstr("Len", this.span, listOf(argSlot, target))
        }
        return
    }

    // Special-case: type(x)
    val typeArg = CallCompiled.asType(call)
    if (typeArg != null) {
        typeArg.writeBcCb(bc) { argSlot, bc2 ->
            bc2.writeInstr("Type", this.span, listOf(argSlot, target))
        }
        return
    }

    // Special-case: isinstance(x, t)
    val isinstanceArgs = CallCompiled.asIsinstance(call)
    if (isinstanceArgs != null) {
        val (x, t) = isinstanceArgs
        val compiled = try {
            TypeCompiled.newFrozen(t, bc.heap)
        } catch (_: Exception) {
            null
        }
        if (compiled != null) {
            x.writeBcCb(bc) { xSlot, bc2 ->
                bc2.writeInstr("IsInstance", this.span, listOf(xSlot, compiled, target))
            }
            return
        }
    }

    val span = this.span
    val fileSpan = bc.allocFileSpan(span)
    val method = CallCompiled.method(call)
    if (method == null) {
        val frozenFun = call.fun_.asValue()
        if (frozenFun != null) {
            writeCallFrozen(span, frozenFun, call.args, target, bc)
        } else {
            call.fun_.writeBcCb(bc) { funSlot, bc2 ->
                writeArgs(call.args, bc2) { callArgs, bc3 ->
                    when (callArgs) {
                        is Either.Left -> {
                            bc3.writeInstr("CallPos", span, listOf(funSlot, callArgs.value, fileSpan, target))
                        }
                        is Either.Right -> {
                            bc3.writeInstr("Call", span, listOf(funSlot, callArgs.value, fileSpan, target))
                        }
                    }
                }
            }
        }
    } else {
        val (this_, symbol, methodArgs) = method
        writeCallMethod(target, span, this_, symbol, methodArgs, bc)
    }
}

/// Simple Either type to match Rust's `either::Either`.
// use either::Either;
internal sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
}
