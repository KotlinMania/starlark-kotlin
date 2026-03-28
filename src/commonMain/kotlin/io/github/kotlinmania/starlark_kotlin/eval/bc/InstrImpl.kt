// port-lint: source src/eval/bc/instr_impl.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

import io.github.kotlinmania.starlark_kotlin.eval.bc.instr.InstrControl
import io.github.kotlinmania.starlark_kotlin.values.types.list.ptrEq
import io.github.kotlinmania.starlark_kotlin.values.types.dict.getHashed
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark_kotlin.values.length
import io.github.kotlinmania.starlark_kotlin.values.iterate
import io.github.kotlinmania.starlark_kotlin.values.types.typeMethods
import io.github.kotlinmania.starlark_kotlin.values.types.invokeMethod
import io.github.kotlinmania.starlark_kotlin.values.toBool
import io.github.kotlinmania.starlark_kotlin.values.sub
import io.github.kotlinmania.starlark_kotlin.values.setAt
import io.github.kotlinmania.starlark_kotlin.values.rightShift
import io.github.kotlinmania.starlark_kotlin.values.percent
import io.github.kotlinmania.starlark_kotlin.values.mul
import io.github.kotlinmania.starlark_kotlin.values.leftShift
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.toStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.unpackIntValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.info
import io.github.kotlinmania.starlark_kotlin.values.layout.getRef
import io.github.kotlinmania.starlark_kotlin.values.key
import io.github.kotlinmania.starlark_kotlin.values.isIn
import io.github.kotlinmania.starlark_kotlin.values.exportAs
import io.github.kotlinmania.starlark_kotlin.values.div
import io.github.kotlinmania.starlark_kotlin.values.bitXor
import io.github.kotlinmania.starlark_kotlin.values.bitOr
import io.github.kotlinmania.starlark_kotlin.values.bitNot
import io.github.kotlinmania.starlark_kotlin.values.bitAnd
import io.github.kotlinmania.starlark_kotlin.values.at
import io.github.kotlinmania.starlark_kotlin.typing.numPositionalOnly
import io.github.kotlinmania.starlark_kotlin.typing.numPositional
import io.github.kotlinmania.starlark_kotlin.typing.bindings.nameTy
import io.github.kotlinmania.starlark_kotlin.tests.setAttr
import io.github.kotlinmania.starlark_kotlin.tests.derive.freeze.checkType
import io.github.kotlinmania.starlark_kotlin.stdlib.invokeWithLoc
import io.github.kotlinmania.starlark_kotlin.stdlib.add
import io.github.kotlinmania.starlark_kotlin.pagable.vtable
import io.github.kotlinmania.starlark_kotlin.eval.bc.toRange
import io.github.kotlinmania.starlark_kotlin.docs.params
import io.github.kotlinmania.starlark_kotlin.__derive_refs.returnType
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.typing.EvalException
// Types from values.layout
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
// Types from values.layout.typed
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import starlark_map.Hashed
// Types from values.layout.heap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
// Types from values.layout.heap.profile
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.SmallMap
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.ProfilerInstant
// Types from eval.runtime
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.FrameSpan
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotId
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalCapturedSlotId
// Types from eval.bc.frame (sub-package)
import io.github.kotlinmania.starlark_kotlin.eval.bc.frame.BcFramePtr
// Types from values.types.dict
import io.github.kotlinmania.starlark_kotlin.values.types.dict.Dict
// Types from eval.runtime.params.spec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
// Types from values.types.list
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListData
// Types from values
import io.github.kotlinmania.starlark_kotlin.values.FrozenRef
// Types from values.typing.type_compiled
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
// Types from collections.symbol
import io.github.kotlinmania.starlark_kotlin.collections.symbol.Symbol

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

/// Instruction implementations.

fun addSpanToExprError(e: Exception, span: FrameSpan, eval: Evaluator): EvalException =
    EvalException(e.message ?: "")
fun exprThrowStarlarkResult(result: kotlin.Result<Unit>, span: FrameSpan, eval: Evaluator): kotlin.Result<Unit> =
    result
fun addAssign(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
    kotlin.Result.success(Value())
fun bitOrAssign(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
    kotlin.Result.success(Value())
fun possibleGc(eval: Evaluator) {}
fun getAttrHashedBind(obj: Value, field: Symbol, heap: Heap): kotlin.Result<Value> =
    kotlin.Result.success(Value())
fun getAttrHashedRaw(obj: Value, field: Symbol, heap: Heap): kotlin.Result<MethodValue> =
    kotlin.Result.success(MethodValue())
fun percentSOne(before: String, arg: Value, after: String, heap: Heap): kotlin.Result<StringValue> =
    kotlin.Result.success(StringValue())
fun formatOne(before: String, arg: Value, after: String, heap: Heap): StringValue = StringValue()

/// Instructions which either fail or proceed to the following instruction,
/// and it returns error with span.
/// Instructions which either fail or proceed to the following instruction.
interface InstrNoFlowImpl {
    fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit>
}

class InstrNoFlow(val impl: InstrNoFlowImpl) {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): InstrControl {
        return when (val result = impl.runWithArgs(eval, frame, ip, arg)) {
            else -> if (result.isSuccess) {
                InstrControl.Next(ip.addInstr<InstrNoFlow>())
            } else {
                InstrControl.Err(result.exceptionOrNull() ?: Exception("unknown error"))
            }
        }
    }
}

// --- Constant Loading ---

object InstrConstImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (constant, target) = arg as Pair<FrozenValue, BcSlotOut>
        frame.setBcSlot(target, constant.toValue())
        return kotlin.Result.success(Unit)
    }
}

// --- Local/Module Load/Store ---

object InstrLoadLocalImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<LocalSlotId, BcSlotOut>
        val value = eval.getSlotLocal(frame, source)
        return if (value.isSuccess) {
            frame.setBcSlot(target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

object InstrLoadLocalCapturedImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<LocalCapturedSlotId, BcSlotOut>
        val value = eval.getSlotLocalCaptured(source)
        return if (value.isSuccess) {
            frame.setBcSlot(target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

object InstrLoadModuleImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<ModuleSlotId, BcSlotOut>
        val value = eval.getSlotModule(source)
        return if (value.isSuccess) {
            frame.setBcSlot(target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

object InstrMovImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<BcSlotIn, BcSlotOut>
        val v = frame.getBcSlot(source)
        frame.setBcSlot(target, v)
        return kotlin.Result.success(Unit)
    }
}

object InstrStoreLocalCapturedImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<BcSlotIn, LocalCapturedSlotId>
        val v = frame.getBcSlot(source)
        eval.setSlotLocalCaptured(target, v)
        return kotlin.Result.success(Unit)
    }
}

object InstrStoreModuleAndExportImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, slot, name) = arg as Triple<BcSlotIn, ModuleSlotId, String>
        val v = frame.getBcSlot(source)
        val result = v.exportAs(name, eval)
        if (result.isFailure) return kotlin.Result.failure(result.exceptionOrNull()!!)
        eval.setSlotModule(slot, v)
        return kotlin.Result.success(Unit)
    }
}

object InstrStoreModuleImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<BcSlotIn, ModuleSlotId>
        val v = frame.getBcSlot(source)
        eval.setSlotModule(target, v)
        return kotlin.Result.success(Unit)
    }
}

// --- Unpack ---

data class UnpackArg(val source: BcSlotIn, val targets: List<BcSlotOut>)

object InstrUnpackImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, targets) = arg as UnpackArg
        val v = frame.getBcSlot(source)
        val nvl = v.length()
        if (nvl.isFailure) return kotlin.Result.failure(nvl.exceptionOrNull()!!)
        if (nvl.getOrThrow() != targets.size) {
            return kotlin.Result.failure(
                AssignError.IncorrectNumberOfValueToUnpack(targets.size, nvl.getOrThrow())
            )
        }
        val iterResult = v.iterate(eval.heap())
        if (iterResult.isFailure) return kotlin.Result.failure(iterResult.exceptionOrNull()!!)
        var i = 0
        for (item in iterResult.getOrThrow()) {
            check(i < targets.size)
            frame.setBcSlot(targets[i], item)
            i += 1
        }
        check(i == targets.size)
        return kotlin.Result.success(Unit)
    }
}

// --- Array/Object Access ---

object InstrArrayIndexImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (array, index, target) = arg as Triple<BcSlotIn, BcSlotIn, BcSlotOut>
        val arrayVal = frame.getBcSlot(array)
        val indexVal = frame.getBcSlot(index)
        val value = arrayVal.at(indexVal, eval.heap())
        return if (value.isSuccess) {
            frame.setBcSlot(target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

object InstrSetArrayIndexImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, array, index) = arg as Triple<BcSlotIn, BcSlotIn, BcSlotIn>
        val value = frame.getBcSlot(source)
        val arrayVal = frame.getBcSlot(array)
        val indexVal = frame.getBcSlot(index)
        return arrayVal.setAt(indexVal, value)
    }
}

object InstrArrayIndexSetImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (array, index, source) = arg as Triple<BcSlotIn, BcSlotIn, BcSlotIn>
        val value = frame.getBcSlot(source)
        val arrayVal = frame.getBcSlot(array)
        val indexVal = frame.getBcSlot(index)
        return arrayVal.setAt(indexVal, value)
    }
}

object InstrObjectFieldImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (obj, field, target) = arg as Triple<BcSlotIn, Symbol, BcSlotOut>
        val objVal = frame.getBcSlot(obj)
        val value = getAttrHashedBind(objVal, field, eval.heap())
        return if (value.isSuccess) {
            frame.setBcSlot(target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

object InstrSetObjectFieldImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, obj, field) = arg as Triple<BcSlotIn, BcSlotIn, Symbol>
        val v = frame.getBcSlot(source)
        val objVal = frame.getBcSlot(obj)
        return objVal.setAttr(field.asStr(), v)
    }
}

// --- Slice ---

data class SliceArg(
    val list: BcSlotIn,
    val start: BcSlotIn?,
    val stop: BcSlotIn?,
    val step: BcSlotIn?,
    val target: BcSlotOut,
)

object InstrSliceImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val sa = arg as SliceArg
        val list = frame.getBcSlot(sa.list)
        val start = sa.start?.let { frame.getBcSlot(it) }
        val stop = sa.stop?.let { frame.getBcSlot(it) }
        val step = sa.step?.let { frame.getBcSlot(it) }
        val value = list.slice(start, stop, step, eval.heap())
        return if (value.isSuccess) {
            frame.setBcSlot(sa.target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

// --- Array Index 2 ---

data class ArrayIndex2Arg(
    val array: BcSlotIn,
    val index0: BcSlotIn,
    val index1: BcSlotIn,
    val target: BcSlotOut,
)

object InstrArrayIndex2Impl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as ArrayIndex2Arg
        val array = frame.getBcSlot(a.array)
        val index0 = frame.getBcSlot(a.index0)
        val index1 = frame.getBcSlot(a.index1)
        val value = array.getRef().at2(index0, index1, eval.heap())
        return if (value.isSuccess) {
            frame.setBcSlot(a.target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

// --- Equality ---

object InstrEqImpl {
    fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> {
        return v0.equals(v1).map { Value.newBool(it) }
    }
}

object InstrEqConstImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (a, b, target) = arg as Triple<BcSlotIn, FrozenValueNotSpecial, BcSlotOut>
        val aVal = frame.getBcSlot(a)
        val r = b.equals(aVal)
        return if (r.isSuccess) {
            frame.setBcSlot(target, Value.newBool(r.getOrThrow()))
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(r.exceptionOrNull()!!)
        }
    }
}

object InstrEqPtrImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (a, b, target) = arg as Triple<BcSlotIn, FrozenValue, BcSlotOut>
        val aVal = frame.getBcSlot(a)
        val r = aVal.ptrEq(b.toValue())
        frame.setBcSlot(target, Value.newBool(r))
        return kotlin.Result.success(Unit)
    }
}

object InstrEqIntImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (a, b, target) = arg as Triple<BcSlotIn, FrozenValueTyped<PointerI32>, BcSlotOut>
        val aVal = frame.getBcSlot(a)
        val aInt = aVal.unpackIntValue()
        val r = if (aInt != null) {
            aInt.asRef() == b.asRef()
        } else {
            val eq = b.toValue().equals(aVal)
            if (eq.isFailure) return kotlin.Result.failure(eq.exceptionOrNull()!!)
            eq.getOrThrow()
        }
        frame.setBcSlot(target, Value.newBool(r))
        return kotlin.Result.success(Unit)
    }
}

object InstrEqStrImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (a, b, target) = arg as Triple<BcSlotIn, FrozenStringValue, BcSlotOut>
        val aVal = frame.getBcSlot(a)
        val aStr = StringValue.new(aVal)
        val r = if (aStr != null) {
            aStr == b.toStringValue()
        } else {
            false
        }
        frame.setBcSlot(target, Value.newBool(r))
        return kotlin.Result.success(Unit)
    }
}

// --- Unary Operators ---

interface InstrUnOpImpl {
    fun eval(v: Value, heap: Heap): kotlin.Result<Value>
}

interface InstrBinOpImpl {
    fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value>
}

object InstrNotImpl : InstrUnOpImpl {
    override fun eval(v: Value, heap: Heap): kotlin.Result<Value> =
        kotlin.Result.success(Value.newBool(!v.toBool()))
}

object InstrPlusImpl : InstrUnOpImpl {
    override fun eval(v: Value, heap: Heap): kotlin.Result<Value> = v.plus(heap)
}

object InstrMinusImpl : InstrUnOpImpl {
    override fun eval(v: Value, heap: Heap): kotlin.Result<Value> = v.minus(heap)
}

object InstrBitNotImpl : InstrUnOpImpl {
    override fun eval(v: Value, heap: Heap): kotlin.Result<Value> = v.bitNot(heap)
}

// --- Binary Operators ---

object InstrAddImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> = v0.add(v1, heap)
}

object InstrAddAssignImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        addAssign(v0, v1, heap)
}

object InstrSubImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> = v0.sub(v1, heap)
}

object InstrMultiplyImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> = v0.mul(v1, heap)
}

object InstrPercentImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.percent(v1, heap)
}

object InstrFloorDivideImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.floorDiv(v1, heap)
}

object InstrDivideImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> = v0.div(v1, heap)
}

object InstrBitAndImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.bitAnd(v1, heap)
}

object InstrBitOrImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.bitOr(v1, heap)
}

object InstrBitOrAssignImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        bitOrAssign(v0, v1, heap)
}

object InstrBitXorImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.bitXor(v1, heap)
}

object InstrLeftShiftImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.leftShift(v1, heap)
}

object InstrRightShiftImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v0.rightShift(v1, heap)
}

object InstrInImpl : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> =
        v1.isIn(v0).map { Value.newBool(it) }
}

// --- Binary/Unary Op Wrappers ---

class InstrBinOpWrapper(private val impl: InstrBinOpImpl) : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (v0, v1, target) = arg as Triple<BcSlotIn, BcSlotIn, BcSlotOut>
        val v0Val = frame.getBcSlot(v0)
        val v1Val = frame.getBcSlot(v1)
        val v = impl.eval(v0Val, v1Val, eval.heap())
        return if (v.isSuccess) {
            frame.setBcSlot(target, v.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(v.exceptionOrNull()!!)
        }
    }
}

class InstrUnOpWrapper(private val impl: InstrUnOpImpl) : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (source, target) = arg as Pair<BcSlotIn, BcSlotOut>
        val sourceVal = frame.getBcSlot(source)
        val value = impl.eval(sourceVal, eval.heap())
        return if (value.isSuccess) {
            frame.setBcSlot(target, value.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(value.exceptionOrNull()!!)
        }
    }
}

// --- String Interpolation ---

object InstrPercentSOneImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (before, argSlot, after, target) = arg as PercentSOneArg
        val argVal = frame.getBcSlot(argSlot)
        val r = percentSOne(before.asStr(), argVal, after.asStr(), eval.heap())
        return if (r.isSuccess) {
            frame.setBcSlot(target, r.getOrThrow().toValue())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(r.exceptionOrNull()!!)
        }
    }
}

data class PercentSOneArg(
    val before: FrozenStringValue,
    val argSlot: BcSlotIn,
    val after: FrozenStringValue,
    val target: BcSlotOut,
)

object InstrFormatOneImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (before, argSlot, after, target) = arg as PercentSOneArg
        val argVal = frame.getBcSlot(argSlot)
        val r = formatOne(before.asStr(), argVal, after.asStr(), eval.heap())
        frame.setBcSlot(target, r.toValue())
        return kotlin.Result.success(Unit)
    }
}

// --- Comparison ---

interface InstrCompareImpl {
    fun evalCompare(ordering: Int): Boolean
}

object InstrLessImpl : InstrCompareImpl {
    override fun evalCompare(ordering: Int): Boolean = ordering < 0
}

object InstrGreaterImpl : InstrCompareImpl {
    override fun evalCompare(ordering: Int): Boolean = ordering > 0
}

object InstrLessOrEqualImpl : InstrCompareImpl {
    override fun evalCompare(ordering: Int): Boolean = ordering <= 0
}

object InstrGreaterOrEqualImpl : InstrCompareImpl {
    override fun evalCompare(ordering: Int): Boolean = ordering >= 0
}

class InstrCompareWrapper(private val impl: InstrCompareImpl) : InstrBinOpImpl {
    override fun eval(v0: Value, v1: Value, heap: Heap): kotlin.Result<Value> {
        val cmp = v0.compare(v1)
        return if (cmp.isSuccess) {
            kotlin.Result.success(Value.newBool(impl.evalCompare(cmp.getOrThrow())))
        } else {
            kotlin.Result.failure(cmp.exceptionOrNull()!!)
        }
    }
}

// --- Type/Len ---

object InstrTypeImpl : InstrUnOpImpl {
    override fun eval(v: Value, heap: Heap): kotlin.Result<Value> =
        kotlin.Result.success(v.getTypeValue().toFrozenValue().toValue())
}

object InstrTypeIsImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (argSlot, t, target) = arg as Triple<BcSlotIn, FrozenStringValue, BcSlotOut>
        val argVal = frame.getBcSlot(argSlot)
        val r = argVal.getTypeValue() == t
        frame.setBcSlot(target, Value.newBool(r))
        return kotlin.Result.success(Unit)
    }
}

object InstrIsInstanceImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (argSlot, t, target) = arg as Triple<BcSlotIn, TypeCompiled, BcSlotOut>
        val argVal = frame.getBcSlot(argSlot)
        val r = t.matches(argVal)
        frame.setBcSlot(target, Value.newBool(r))
        return kotlin.Result.success(Unit)
    }
}

object InstrLenImpl : InstrUnOpImpl {
    override fun eval(v: Value, heap: Heap): kotlin.Result<Value> {
        val len = v.length()
        return if (len.isSuccess) {
            kotlin.Result.success(heap.alloc(len.getOrThrow()))
        } else {
            kotlin.Result.failure(len.exceptionOrNull()!!)
        }
    }
}

// --- Tuple/List/Dict Construction ---

object InstrTupleNPopImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (values, target) = arg as Pair<BcSlotInRange, BcSlotOut>
        val items = frame.getBcSlotRange(values)
        val value = eval.heap().allocTuple(items)
        frame.setBcSlot(target, value)
        return kotlin.Result.success(Unit)
    }
}

object InstrListNPopImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (values, target) = arg as Pair<BcSlotInRange, BcSlotOut>
        val items = frame.getBcSlotRange(values)
        val value = eval.heap().allocList(items)
        frame.setBcSlot(target, value)
        return kotlin.Result.success(Unit)
    }
}

object InstrListOfConstsImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (values, target) = arg as Pair<List<FrozenValue>, BcSlotOut>
        val list = eval.heap().allocList(values.map { it.toValue() })
        frame.setBcSlot(target, list)
        return kotlin.Result.success(Unit)
    }
}

object InstrDictOfConstsImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (values, target) = arg as Pair<SmallMap<FrozenValue, FrozenValue>, BcSlotOut>
        val dict = eval.heap().alloc(Dict.new(values))
        frame.setBcSlot(target, dict)
        return kotlin.Result.success(Unit)
    }
}

object InstrDictNPopImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (npops, target) = arg as Pair<BcSlotInRange, BcSlotOut>
        val items = frame.getBcSlotRange(npops)
        check(items.size % 2 == 0)
        val dict = SmallMap.withCapacity<Value, Value>(items.size / 2)
        for (i in 0 until items.size / 2) {
            val k = items[i * 2]
            val v = items[i * 2 + 1]
            val hashed = k.getHashed()
            if (hashed.isFailure) {
                val spans = Bc.slowArgAtPtr(ip).spans
                return kotlin.Result.failure(
                    addSpanToExprError(hashed.exceptionOrNull()!!, spans[i], eval).intoError()
                )
            }
            val prev = dict.insertHashed(hashed.getOrThrow(), v)
            if (prev != null) {
                val e = EvalError.DuplicateDictionaryKey(hashed.getOrThrow().key.toString())
                val spans = Bc.slowArgAtPtr(ip).spans
                return kotlin.Result.failure(
                    addSpanToExprError(e, spans[i], eval).intoError()
                )
            }
        }
        val dictVal = eval.heap().alloc(Dict.new(dict))
        frame.setBcSlot(target, dictVal)
        return kotlin.Result.success(Unit)
    }
}

data class DictConstKeysArg(
    val keys: List<Hashed<FrozenValue>>,
    val values: BcSlotInRangeFrom,
    val target: BcSlotOut,
)

object InstrDictConstKeysImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as DictConstKeysArg
        val values = frame.getBcSlotRange(a.values.toRange(a.keys.size.toUInt()))
        val dict = SmallMap.withCapacity<Value, Value>(a.keys.size)
        for ((k, v) in a.keys.zip(values)) {
            dict.insertHashed(HashedValue(k.key.toValue()), v)
        }
        val dictVal = eval.heap().alloc(Dict.new(dict))
        frame.setBcSlot(a.target, dictVal)
        return kotlin.Result.success(Unit)
    }
}

object InstrListNewImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val target = arg as BcSlotOut
        val list = eval.heap().allocList(emptyList())
        frame.setBcSlot(target, list)
        return kotlin.Result.success(Unit)
    }
}

object InstrDictNewImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val target = arg as BcSlotOut
        val dict = eval.heap().alloc(Dict.default())
        frame.setBcSlot(target, dict)
        return kotlin.Result.success(Unit)
    }
}

// --- Comprehension ---

object InstrComprListAppend {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Pair<BcSlotIn, BcSlotIn>,
    ): InstrControl {
        val (list, item) = arg
        val listVal = frame.getBcSlot(list)
        val itemVal = frame.getBcSlot(item)
        val listData = ListData.fromValueUncheckedMut(listVal)
        listData.push(itemVal, eval.heap())
        return InstrControl.Next(ip.addInstr<InstrComprListAppend>())
    }
}

object InstrComprDictInsert {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Triple<BcSlotIn, BcSlotIn, BcSlotIn>,
    ): InstrControl {
        val (dict, key, value) = arg
        val dictVal = frame.getBcSlot(dict)
        val keyVal = frame.getBcSlot(key)
        val valueVal = frame.getBcSlot(value)
        val hashed = keyVal.getHashed()
        if (hashed.isFailure) return InstrControl.Err(hashed.exceptionOrNull()!!)
        val dictData = Dict.new(SmallMap<Value, Value>())
        dictData.insertHashed(hashed.getOrThrow(), valueVal)
        return InstrControl.Next(ip.addInstr<InstrComprDictInsert>())
    }
}

// --- Type Checking ---

object InstrCheckTypeImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (expr, ty) = arg as Pair<BcSlotIn, TypeCompiled>
        val exprVal = frame.getBcSlot(expr)
        val start = if (eval.typecheckProfile.enabled) {
            ProfilerInstant.now()
        } else {
            null
        }
        val res = ty.checkType(exprVal, null)
        if (start != null) {
            eval.typecheckProfile.add(FrozenStringValue(), start.elapsed())
        }
        return res
    }
}

// --- Branching ---

object InstrBr {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        target: BcAddrOffset,
    ): InstrControl {
        return InstrControl.Next(ip.addRel(target))
    }
}

object InstrIfBr {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Pair<BcSlotIn, BcAddrOffset>,
    ): InstrControl {
        val (cond, target) = arg
        val condVal = frame.getBcSlot(cond)
        return if (condVal.toBool()) {
            InstrControl.Next(ip.addRel(target))
        } else {
            InstrControl.Next(ip.addInstr<InstrIfBr>())
        }
    }
}

object InstrIfNotBr {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Pair<BcSlotIn, BcAddrOffset>,
    ): InstrControl {
        val (cond, target) = arg
        val condVal = frame.getBcSlot(cond)
        return if (!condVal.toBool()) {
            InstrControl.Next(ip.addRel(target))
        } else {
            InstrControl.Next(ip.addInstr<InstrIfNotBr>())
        }
    }
}

// --- For Loop ---

/// Setup `for` loop.
data class InstrIterArg(
    val over: BcSlotIn,
    val loopDepth: LoopDepth,
    val iterSlot: BcSlotOut,
    val varSlot: BcSlotOut,
    val end: BcAddrOffset,
)

object InstrIter {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: InstrIterArg,
    ): InstrControl {
        val over = frame.getBcSlot(arg.over)
        val iter = over.getRef().iterate(over, eval.heap())
        if (iter.isFailure) return InstrControl.Err(iter.exceptionOrNull()!!)
        val iterVal = iter.getOrThrow()
        val next = iterVal.getRef().iterNext(0, eval.heap())
        return if (next != null) {
            frame.setBcSlot(arg.iterSlot, iterVal)
            frame.setBcSlot(arg.varSlot, next)
            frame.setIterIndex(arg.loopDepth, 1)
            InstrControl.Next(ip.addInstr<InstrIter>())
        } else {
            iterVal.getRef().iterStop()
            InstrControl.Next(ip.addRel(arg.end))
        }
    }
}

/// `continue` statement.
data class InstrContinueArg(
    val iter: BcSlotIn,
    val loopDepth: LoopDepth,
    val varSlot: BcSlotOut,
    val begin: BcAddrOffsetNeg,
    val end: BcAddrOffset,
)

object InstrContinue {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: InstrContinueArg,
    ): InstrControl {
        val progress = eval.reportForwardProgress()
        if (progress.isFailure) return InstrControl.Err(progress.exceptionOrNull()!!)
        val iter = frame.getBcSlot(arg.iter)
        val loopDepth = arg.loopDepth
        val i = frame.getIterIndex(loopDepth)
        val next = iter.getRef().iterNext(i, eval.heap())
        return if (next != null) {
            frame.setIterIndex(loopDepth, i + 1)
            frame.setBcSlot(arg.varSlot, next)
            InstrControl.Next(ip.addRelNeg(arg.begin))
        } else {
            iter.getRef().iterStop()
            InstrControl.Next(ip.addRel(arg.end))
        }
    }
}

/// `break` statement.
object InstrBreak {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Pair<BcSlotIn, BcAddrOffset>,
    ): InstrControl {
        val (iter, end) = arg
        val iterVal = frame.getBcSlot(iter)
        iterVal.getRef().iterStop()
        return InstrControl.Next(ip.addRel(end))
    }
}

/// Stop all the iterations to release mutation locks before `return`.
object InstrIterStop {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        iter: BcSlotIn,
    ): InstrControl {
        val iterVal = frame.getBcSlot(iter)
        iterVal.getRef().iterStop()
        return InstrControl.Next(ip.addInstr<InstrIterStop>())
    }
}

// --- Return ---

object InstrReturnConst {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        value: FrozenValue,
    ): InstrControl {
        return InstrControl.Return(value.toValue())
    }
}

object InstrReturn {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        slot: BcSlotIn,
    ): InstrControl {
        val v = frame.getBcSlot(slot)
        return InstrControl.Return(v)
    }
}

object InstrReturnCheckType {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        slot: BcSlotIn,
    ): InstrControl {
        val v = frame.getBcSlot(slot)
        val check = eval.checkReturnType(v)
        if (check.isFailure) return InstrControl.Err(check.exceptionOrNull()!!)
        return InstrControl.Return(v)
    }
}

// --- Def ---

data class InstrDefData(
    val functionName: String,
    val params: ParametersCompiled,
    val returnType: TypeCompiled?,
    val info: FrozenRef<DefInfo>,
)

object InstrDefImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val (pops, defData, target) = arg as Triple<BcSlotInRange, InstrDefData, BcSlotOut>
        val pop = frame.getBcSlotRange(pops)

        val parameters = ParametersSpec.withCapacity(
            defData.functionName,
            defData.params.params.size,
        )
        val parameterTypes = mutableListOf<Triple<LocalSlotId, String, TypeCompiled>>()

        var popIndex = 0

        for ((i, x) in defData.params.params.withIndex()) {
            if (i == defData.params.indices.numPositionalOnly && !x.isStarOrStarStar()) {
                parameters.noMorePositionalOnlyArgs()
            }
            if (i == defData.params.indices.numPositional && !x.isStarOrStarStar()) {
                parameters.noMorePositionalArgs()
            }

            val (name, ty) = x.nameTy()
            if (ty != null) {
                parameterTypes.add(Triple(LocalSlotId(i), name.name, ty))
            }

            when (val node = x.node) {
                is ParameterCompiled.Normal -> {
                    if (node.defaultSlot == null) {
                        parameters.required(node.name.name)
                    } else {
                        check(node.defaultSlot == popIndex)
                        val value = pop[popIndex]
                        popIndex += 1

                        if (node.ty != null) {
                            val (_, _, tyCompiled) = parameterTypes.last()
                            val checkResult = exprThrowStarlarkResult(
                                tyCompiled.checkType(value, node.name.name),
                                x.span,
                                eval,
                            )
                            if (checkResult.isFailure) {
                                return kotlin.Result.failure(checkResult.exceptionOrNull()!!)
                            }
                        }
                        parameters.defaulted(node.name.name, value)
                    }
                }
                is ParameterCompiled.Args -> parameters.args()
                is ParameterCompiled.KwArgs -> parameters.kwargs()
                else -> {}
            }
        }
        val returnType = defData.returnType
        check(popIndex == pop.size)
        val defResult = Def.new(
            parameters.finish(),
            parameterTypes,
            returnType,
            defData.info,
            eval,
        )
        if (defResult.isFailure) return kotlin.Result.failure(defResult.exceptionOrNull()!!)
        val def = eval.heap().alloc(defResult.getOrThrow())
        frame.setBcSlot(target, def)
        return kotlin.Result.success(Unit)
    }
}

// --- Callable ---

/// A frozen function argument to a call instruction.
interface BcFrozenCallable {
    fun bcInvoke(
        location: FrameSpan,
        args: Arguments,
        eval: Evaluator,
    ): kotlin.Result<Value>
}

class FrozenValueCallable(private val value: FrozenValue) : BcFrozenCallable {
    override fun bcInvoke(
        location: FrameSpan,
        args: Arguments,
        eval: Evaluator,
    ): kotlin.Result<Value> {
        return value.toValue().invokeWithLoc(location, args, eval)
    }
}

class FrozenDefCallable(private val def: FrozenValueTyped<FrozenDef>) : BcFrozenCallable {
    override fun bcInvoke(
        location: FrameSpan,
        args: Arguments,
        eval: Evaluator,
    ): kotlin.Result<Value> {
        return eval.withCallStack(def.toValue(), location) { innerEval ->
            def.asRef().invoke(def.toValue(), args, innerEval)
        }
    }
}

class BcNativeFunctionCallable(private val func: BcNativeFunction) : BcFrozenCallable {
    override fun bcInvoke(
        location: FrameSpan,
        args: Arguments,
        eval: Evaluator,
    ): kotlin.Result<Value> {
        return eval.withCallStack(func.toValue(), location) { innerEval ->
            func.invoke(args, innerEval)
        }
    }
}

// --- Call Instructions ---

data class CallArg(
    val funSlot: BcSlotIn,
    val args: Any,
    val span: FrameSpan,
    val target: BcSlotOut,
)

object InstrCallImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as CallArg
        val progress = eval.reportForwardProgress()
        if (progress.isFailure) return kotlin.Result.failure(progress.exceptionOrNull()!!)
        val f = frame.getBcSlot(a.funSlot)
        val r = f.invokeWithLoc(a.span, Arguments(), eval)
        return if (r.isSuccess) {
            frame.setBcSlot(a.target, r.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(r.exceptionOrNull()!!)
        }
    }
}

data class CallFrozenArg(
    val callable: BcFrozenCallable,
    val args: Any,
    val span: FrameSpan,
    val target: BcSlotOut,
)

object InstrCallFrozenGenericImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as CallFrozenArg
        val progress = eval.reportForwardProgress()
        if (progress.isFailure) return kotlin.Result.failure(progress.exceptionOrNull()!!)
        val r = a.callable.bcInvoke(a.span, Arguments(), eval)
        return if (r.isSuccess) {
            frame.setBcSlot(a.target, r.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(r.exceptionOrNull()!!)
        }
    }
}

data class CallFrozenDefArg(
    val fun_: FrozenValueTyped<FrozenDef>,
    val args: Any,
    val span: FrameSpan,
    val target: BcSlotOut,
)

object InstrCallFrozenDefImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as CallFrozenDefArg
        val progress = eval.reportForwardProgress()
        if (progress.isFailure) return kotlin.Result.failure(progress.exceptionOrNull()!!)
        val r = eval.withCallStack(a.fun_.toValue(), a.span) { innerEval ->
            a.fun_.asRef().invokeWithArgs(a.fun_.toValue(), a.args, innerEval)
        }
        return if (r.isSuccess) {
            frame.setBcSlot(a.target, r.getOrThrow())
            kotlin.Result.success(Unit)
        } else {
            kotlin.Result.failure(r.exceptionOrNull()!!)
        }
    }
}

// --- Method Calls ---

data class CallMethodArg(
    val thisSlot: BcSlotIn,
    val symbol: Symbol,
    val args: Any,
    val span: FrameSpan,
    val target: BcSlotOut,
)

/// Common of method invocation instructions.
private fun callMethodCommon(
    eval: Evaluator,
    frame: BcFramePtr,
    thisValue: Value,
    symbol: Symbol,
    arguments: Arguments,
    span: FrameSpan,
    target: BcSlotOut,
): kotlin.Result<Unit> {
    val progress = eval.reportForwardProgress()
    if (progress.isFailure) return kotlin.Result.failure(progress.exceptionOrNull()!!)
    val method = getAttrHashedRaw(thisValue, symbol, eval.heap())
    if (method.isFailure) return kotlin.Result.failure(method.exceptionOrNull()!!)
    val r = method.getOrThrow().invoke(thisValue, span, arguments, eval)
    return if (r.isSuccess) {
        frame.setBcSlot(target, r.getOrThrow())
        kotlin.Result.success(Unit)
    } else {
        kotlin.Result.failure(r.exceptionOrNull()!!)
    }
}

data class CallMaybeKnownMethodArg(
    val thisSlot: BcSlotIn,
    val symbol: Symbol,
    val knownMethod: KnownMethod,
    val args: Any,
    val span: FrameSpan,
    val target: BcSlotOut,
)

/// Common of method invocation instructions where a method is likely stdlib method.
private fun callMaybeKnownMethodCommon(
    eval: Evaluator,
    frame: BcFramePtr,
    thisValue: Value,
    symbol: Symbol,
    knownMethod: KnownMethod,
    arguments: Arguments,
    span: FrameSpan,
    target: BcSlotOut,
): kotlin.Result<Unit> {
    val methods = thisValue.vtable().methods()
    if (methods != null) {
        // Instead of method lookup by name, we compare Methods pointers.
        // If pointers are equal, getattr would return the same method we already have.
        if (methods === knownMethod.typeMethods) {
            val r = eval.withCallStack(knownMethod.toValue(), span) { innerEval ->
                knownMethod.invokeMethod(thisValue, arguments, innerEval)
            }
            return if (r.isSuccess) {
                frame.setBcSlot(target, r.getOrThrow())
                kotlin.Result.success(Unit)
            } else {
                kotlin.Result.failure(r.exceptionOrNull()!!)
            }
        }
    }
    return callMethodCommon(eval, frame, thisValue, symbol, arguments, span, target)
}

object InstrCallMethodImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as CallMethodArg
        val thisValue = frame.getBcSlot(a.thisSlot)
        return callMethodCommon(eval, frame, thisValue, a.symbol, Arguments(), a.span, a.target)
    }
}

object InstrCallMaybeKnownMethodImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        val a = arg as CallMaybeKnownMethodArg
        val thisValue = frame.getBcSlot(a.thisSlot)
        return callMaybeKnownMethodCommon(
            eval, frame, thisValue, a.symbol, a.knownMethod, Arguments(), a.span, a.target,
        )
    }
}

// --- GC ---

object InstrPossibleGcImpl : InstrNoFlowImpl {
    override fun runWithArgs(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: Any,
    ): kotlin.Result<Unit> {
        possibleGc(eval)
        return kotlin.Result.success(Unit)
    }
}

/// Pseudo-instruction:
/// * to store bytecode metadata (i.e. spans): when bytecode is evaluated, we only have IP,
///   we don't have a pointer to bytecode object. To obtain spans by IP, we scroll
///   through the instruction until we encounter this pseudo-instruction.
/// * as a safety against memory overruns. Function block must terminate with return instruction,
///   but if return was missed, this instruction is executed and it panics.
object InstrEnd {
    fun run(
        eval: Evaluator,
        frame: BcFramePtr,
        ip: BcPtrAddr,
        arg: BcInstrEndArg,
    ): InstrControl {
        throw IllegalStateException("this instruction is not meant to be executed")
    }
}
