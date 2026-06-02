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

/** Compile function calls. */

import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcCallArgsFull
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcCallArgsPos
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcNativeFunction
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcWriter
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.eval.bc.resolve
import io.github.kotlinmania.starlark_kotlin.eval.bc.CallArg
import io.github.kotlinmania.starlark_kotlin.eval.bc.CallFrozenArg
import io.github.kotlinmania.starlark_kotlin.eval.bc.CallFrozenDefArg
import io.github.kotlinmania.starlark_kotlin.eval.bc.CallMethodArg
import io.github.kotlinmania.starlark_kotlin.eval.bc.CallMaybeKnownMethodArg
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcNativeFunctionCallable
import io.github.kotlinmania.starlark_kotlin.eval.bc.FrozenValueCallable
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcCallArgsPosCallArgs
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcCallArgsFullCallArgs
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcCallArgsPosForDef
import io.github.kotlinmania.starlark_kotlin.eval.bc.BcCallArgsFullForDef
import io.github.kotlinmania.starlark_kotlin.eval.compiler.args.ArgsCompiledValue
import io.github.kotlinmania.starlark_kotlin.eval.compiler.CallCompiled
import io.github.kotlinmania.starlark_kotlin.eval.compiler.ExprCompiled
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.NativeFunction
import io.github.kotlinmania.starlark_kotlin.eval.compiler.IrSpanned
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.values.types.getKnownMethod
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.eval.compiler.FrozenDef

// impl ArgsCompiledValue

/**
 * After evaluation of function arguments like `foo(a, b=c[d], **e)`,
 * variables `a`, `b`, `c`, `d`, and `e` are definitely assigned.
 */
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

/**
 * After evaluation of call like `a[b](c.d)`,
 * variables `a`, `b`, and `c` are definitely assigned.
 */
// pub(crate) fn mark_definitely_assigned_after(&self, bc: &mut BcWriter)
internal fun CallCompiled.markDefinitelyAssignedAfterCall(bc: BcWriter) {
    function.markDefinitelyAssignedAfter(bc)
    args.markDefinitelyAssignedAfterCall(bc)
}

/**
 * Wrap raw call args into the [BcCallArgs] interface expected by instruction impls.
 * In Rust, `BcCallArgsPos` directly implements `BcCallArgs<S>`.  In Kotlin, separate
 * wrapper classes are used.
 */
private fun Either<BcCallArgsPos, BcCallArgsFull<Symbol>>.toBcCallArgs(): Any {
    return when (this) {
        is Either.Left -> BcCallArgsPosCallArgs<Symbol>(value)
        is Either.Right -> BcCallArgsFullCallArgs<Symbol>(value)
    }
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
            val argsPos = Either.Left(BcCallArgsPos(posSlots))
            k(argsPos, bc2)
        }
    } else {
        args.writeBc(bc) { argsFull, bc2 ->
            val argsFull2 = Either.Right(argsFull)
            k(argsFull2, bc2)
        }
    }
}

// fn write_call_frozen(span, fun, args, target, bc)
private fun writeCallFrozen(
    span: FrameSpan,
    function: FrozenValue,
    args: ArgsCompiledValue,
    target: BcSlotOut,
    bc: BcWriter,
) {
    val fileSpan = bc.allocFileSpan(span)
    val frozenDef = FrozenValueTyped.new<FrozenDef>(function)
    if (frozenDef != null) {
        writeArgs(args, bc) { callArgs, bc2 ->
            when (callArgs) {
                is Either.Left -> {
                    bc2.writeInstr("CallFrozenDefPos", span, CallFrozenDefArg(frozenDef, BcCallArgsPosForDef(callArgs.value), fileSpan, target))
                }
                is Either.Right -> {
                    bc2.writeInstr("CallFrozenDef", span, CallFrozenDefArg(frozenDef, BcCallArgsFullForDef(callArgs.value.resolve(frozenDef.asRef())), fileSpan, target))
                }
            }
        }
        return
    }
    val nativeFunc = FrozenValueTyped.new<NativeFunction>(function)
    if (nativeFunc != null) {
        val bcNative = BcNativeFunction.new(nativeFunc)
        writeArgs(args, bc) { callArgs, bc2 ->
            when (callArgs) {
                is Either.Left -> {
                    bc2.writeInstr("CallFrozenNativePos", span, CallFrozenArg(BcNativeFunctionCallable(bcNative), callArgs.toBcCallArgs(), fileSpan, target))
                }
                is Either.Right -> {
                    bc2.writeInstr("CallFrozenNative", span, CallFrozenArg(BcNativeFunctionCallable(bcNative), callArgs.toBcCallArgs(), fileSpan, target))
                }
            }
        }
        return
    }
    // Generic frozen call.
    writeArgs(args, bc) { callArgs, bc2 ->
        when (callArgs) {
            is Either.Left -> {
                bc2.writeInstr("CallFrozenPos", span, CallFrozenArg(FrozenValueCallable(function), callArgs.toBcCallArgs(), fileSpan, target))
            }
            is Either.Right -> {
                bc2.writeInstr("CallFrozen", span, CallFrozenArg(FrozenValueCallable(function), callArgs.toBcCallArgs(), fileSpan, target))
            }
        }
    }
}

// fn write_call_method(target, span, this, symbol, args, bc)
private fun writeCallMethod(
    target: BcSlotOut,
    span: FrameSpan,
    receiver: IrSpanned<ExprCompiled>,
    symbol: Symbol,
    args: ArgsCompiledValue,
    bc: BcWriter,
) {
    receiver.writeBcCb(bc) { thisSlot, bc2 ->
        val fileSpan = bc2.allocFileSpan(span)
        val knownMethod = getKnownMethod(symbol.asStr())
        val pos = args.posOnly()
        if (pos != null) {
            writeExprs(pos, bc2) { posSlots, bc3 ->
                val wrappedArgs = BcCallArgsPosCallArgs<Symbol>(BcCallArgsPos(posSlots))
                if (knownMethod != null) {
                    bc3.writeInstr(
                        "CallMaybeKnownMethodPos",
                        span,
                        CallMaybeKnownMethodArg(thisSlot, symbol, knownMethod, wrappedArgs, fileSpan, target),
                    )
                } else {
                    bc3.writeInstr(
                        "CallMethodPos",
                        span,
                        CallMethodArg(thisSlot, symbol, wrappedArgs, fileSpan, target),
                    )
                }
            }
        } else {
            args.writeBc(bc2) { argsFull, bc3 ->
                val wrappedArgs = BcCallArgsFullCallArgs<Symbol>(argsFull)
                if (knownMethod != null) {
                    bc3.writeInstr(
                        "CallMaybeKnownMethod",
                        span,
                        CallMaybeKnownMethodArg(thisSlot, symbol, knownMethod, wrappedArgs, fileSpan, target),
                    )
                } else {
                    bc3.writeInstr(
                        "CallMethod",
                        span,
                        CallMethodArg(thisSlot, symbol, wrappedArgs, fileSpan, target),
                    )
                }
            }
        }
    }
}

/** Compile a call expression to bytecode. */
// pub(crate) fn write_bc(&self, target: BcSlotOut, bc: &mut BcWriter)
internal fun IrSpanned<CallCompiled>.writeBcCall(target: BcSlotOut, bc: BcWriter) {
    val call = this.node

    // Special-case: len(x)
    val lenArg = call.asLen()
    if (lenArg != null) {
        lenArg.writeBcCb(bc) { argSlot, bc2 ->
            bc2.writeInstr("Len", this.span, Pair(argSlot, target))
        }
        return
    }

    // Special-case: type(x)
    val typeArg = call.asType()
    if (typeArg != null) {
        typeArg.writeBcCb(bc) { argSlot, bc2 ->
            bc2.writeInstr("Type", this.span, Pair(argSlot, target))
        }
        return
    }

    // Special-case: isinstance(x, t)
    val isinstanceArgs = call.asIsinstance()
    if (isinstanceArgs != null) {
        val (x, t) = isinstanceArgs
        val compiled = try {
            TypeCompiled.newFrozen(t, bc.heap)
        } catch (_: Exception) {
            null
        }
        if (compiled != null) {
            x.writeBcCb(bc) { xSlot, bc2 ->
                bc2.writeInstr("IsInstance", this.span, Triple(xSlot, compiled, target))
            }
            return
        }
    }

    val span = this.span
    val fileSpan = bc.allocFileSpan(span)
    val method = call.method()
    if (method == null) {
        val frozenFun = call.function.node.asValue()
        if (frozenFun != null) {
            writeCallFrozen(span, frozenFun, call.args, target, bc)
        } else {
            call.function.writeBcCb(bc) { funSlot, bc2 ->
                writeArgs(call.args, bc2) { callArgs, bc3 ->
                    when (callArgs) {
                        is Either.Left -> {
                            bc3.writeInstr("CallPos", span, CallArg(funSlot, callArgs.toBcCallArgs(), fileSpan, target))
                        }
                        is Either.Right -> {
                            bc3.writeInstr("Call", span, CallArg(funSlot, callArgs.toBcCallArgs(), fileSpan, target))
                        }
                    }
                }
            }
        }
    } else {
        val (receiver, symbol, methodArgs) = method
        writeCallMethod(target, span, receiver, symbol, methodArgs, bc)
    }
}

/** Simple Either type to match Rust's `either::Either`. */
// use either::Either;
internal sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
}
