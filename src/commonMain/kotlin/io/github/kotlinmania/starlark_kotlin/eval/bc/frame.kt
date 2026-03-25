// port-lint: source src/eval/bc/frame.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc.frame

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

//! Local variables and stack, in single allocation.

import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotIn
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotInRange
import io.github.kotlinmania.starlark_kotlin.eval.bc.stack_ptr.BcSlotOut
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.eval.bc.writer.LoopDepth
import io.github.kotlinmania.starlark_kotlin.values.layout.value
import io.github.kotlinmania.starlark_kotlin.values.trace
import io.github.kotlinmania.starlark_kotlin.values.index
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.it
import io.github.kotlinmania.starlark_kotlin.eval.runtime.currentFrame

/// Current `def` frame (but not native function frame).
///
/// Frame memory layout:
///
/// ```text
/// [ loop_indices | BcFrame | locals | stack ]
///   BcFramePtr points here ^
/// ```
// #[repr(C)]
// struct BcFrame<'v> {
//     local_count: u32,
//     max_stack_size: u32,
//     max_loop_depth: LoopDepth,
//     slots: [Option<Value<'v>>; 0],
// }
internal class BcFrame(
    /// Number of local slots.
    val localCount: Int,
    /// Number of stack slots.
    val maxStackSize: Int,
    /// Max number of nested for loops.
    val maxLoopDepth: LoopDepth,
) {
    /// `local_count` local slots followed by `max_stack_size` stack slots.
    // Kotlin: using Array instead of raw pointer arithmetic.
    val slots: Array<Value?> = arrayOfNulls(localCount + maxStackSize)

    /// Loop iteration indices, stored separately.
    // Rust stores these before the BcFrame in memory.
    // Kotlin: using an IntArray.
    val loopIndices: IntArray = IntArray(maxLoopDepth.value)

    // impl BcFrame

    /// Initialize frame after it was allocated.
    // fn init(&mut self)
    fun init() {
        // Locals start as null (None / unassigned).
        for (i in 0 until localCount) {
            slots[i] = null
        }
        // Stack slots can remain null; they will be written before read.
    }

    /// Gets a local variable. Returns None to indicate the variable is not yet assigned.
    // fn get_slot(&self, slot: LocalSlotIdCapturedOrNot) -> Option<Value<'v>>
    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? {
        check(slot.index < localCount)
        return slots[slot.index]
    }

    /// Get a stack slot.
    // fn get_bc_slot(&self, slot: BcSlotIn) -> Value<'v>
    fun getBcSlot(slot: BcSlotIn): Value {
        check(slot.get().index < localCount + maxStackSize)
        return slots[slot.get().index]!!
    }

    // fn set_slot(&mut self, slot: LocalSlotIdCapturedOrNot, value: Value<'v>)
    fun setSlot(slot: LocalSlotIdCapturedOrNot, value: Value) {
        check(slot.index < localCount)
        slots[slot.index] = value
    }

    // fn get_bc_slot_range(&self, slots: BcSlotInRange) -> &[Value<'v>]
    fun getBcSlotRange(range: BcSlotInRange): List<Value> {
        check(range.end.get().index <= localCount + maxStackSize)
        val start = range.start.get().index
        val end = range.end.get().index
        return (start until end).map { slots[it]!! }
    }

    // fn set_bc_slot(&mut self, slot: BcSlotOut, value: Value<'v>)
    fun setBcSlot(slot: BcSlotOut, value: Value) {
        check(slot.get().index < localCount + maxStackSize)
        slots[slot.get().index] = value
    }

    // fn set_iter_index(&mut self, iter_index: LoopDepth, index: usize)
    fun setIterIndex(iterIndex: LoopDepth, index: Int) {
        check(iterIndex < maxLoopDepth)
        loopIndices[iterIndex.value] = index
    }

    // fn get_iter_index(&self, iter_index: LoopDepth) -> usize
    fun getIterIndex(iterIndex: LoopDepth): Int {
        check(iterIndex < maxLoopDepth)
        return loopIndices[iterIndex.value]
    }

    // fn locals_mut(&mut self) -> &mut [Option<Value<'v>>]
    fun localsMut(): Array<Value?> {
        // Returns a view of the locals portion of the slots array.
        // Caller should only access indices 0 until localCount.
        return slots
    }

    // unsafe impl Trace for BcFrame
    // fn trace(&mut self, tracer: &Tracer<'v>)
    fun trace(tracer: Tracer) {
        // Trace locals only. Stack is empty during GC.
        for (i in 0 until localCount) {
            val v = slots[i]
            if (v != null) {
                slots[i] = tracer.trace(v)
            }
        }
    }
}

/// Pointer to a `BcFrame`.
// #[derive(Copy, Clone, Dupe)]
// pub(crate) struct BcFramePtr<'v> {
//     slots_ptr: *mut Option<Value<'v>>,
// }
internal class BcFramePtr(
    private var frame: BcFrame?,
) {

    companion object {
        // pub(crate) fn null() -> BcFramePtr<'v>
        fun nil(): BcFramePtr {
            return BcFramePtr(null)
        }
    }

    // impl PartialEq for BcFramePtr
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BcFramePtr) return false
        return frame === other.frame
    }

    override fun hashCode(): Int = frame?.hashCode() ?: 0

    /// Is this frame allocated or constructed empty?
    // pub(crate) fn is_inititalized(self) -> bool
    fun isInitialized(): Boolean = frame != null

    // pub(crate) fn get_slot_slow(self, slot: LocalSlotIdCapturedOrNot) -> Option<Value<'v>>
    fun getSlotSlow(slot: LocalSlotIdCapturedOrNot): Value? {
        val f = frame!!
        check(slot.index < f.localCount)
        return f.getSlot(slot)
    }

    // pub(crate) fn get_slot(self, slot: LocalSlotIdCapturedOrNot) -> Option<Value<'v>>
    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? {
        return frame!!.getSlot(slot)
    }

    // pub(crate) fn set_slot_slow(self, slot: LocalSlotIdCapturedOrNot, value: Value<'v>)
    fun setSlotSlow(slot: LocalSlotIdCapturedOrNot, value: Value) {
        val f = frame!!
        check(slot.index < f.localCount)
        f.setSlot(slot, value)
    }

    // pub(crate) fn set_slot(self, slot: LocalSlotIdCapturedOrNot, value: Value<'v>)
    fun setSlot(slot: LocalSlotIdCapturedOrNot, value: Value) {
        frame!!.setSlot(slot, value)
    }

    // pub(crate) fn get_bc_slot(self, slot: BcSlotIn) -> Value<'v>
    fun getBcSlot(slot: BcSlotIn): Value {
        return frame!!.getBcSlot(slot)
    }

    // pub(crate) fn set_bc_slot(self, slot: BcSlotOut, value: Value<'v>)
    fun setBcSlot(slot: BcSlotOut, value: Value) {
        frame!!.setBcSlot(slot, value)
    }

    // pub(crate) fn get_bc_slot_range<'a>(self, slots: BcSlotInRange) -> &'a [Value<'v>]
    fun getBcSlotRange(slots: BcSlotInRange): List<Value> {
        return frame!!.getBcSlotRange(slots)
    }

    // pub(crate) fn get_iter_index(self, loop_depth: LoopDepth) -> usize
    fun getIterIndex(loopDepth: LoopDepth): Int {
        return frame!!.getIterIndex(loopDepth)
    }

    // pub(crate) fn set_iter_index(self, loop_depth: LoopDepth, index: usize)
    fun setIterIndex(loopDepth: LoopDepth, index: Int) {
        frame!!.setIterIndex(loopDepth, index)
    }

    // pub(crate) fn max_stack_size(self) -> u32
    fun maxStackSize(): Int {
        return frame!!.maxStackSize
    }

    // pub(crate) unsafe fn locals_mut<'a>(self) -> &'a mut [Option<Value<'v>>]
    fun localsMut(): Array<Value?> {
        return frame!!.localsMut()
    }

    // unsafe impl Trace for BcFramePtr
    // fn trace(&mut self, tracer: &Tracer<'v>)
    fun trace(tracer: Tracer) {
        frame?.trace(tracer)
    }

    /// Set the underlying frame reference.
    internal fun setFrame(newFrame: BcFrame?) {
        frame = newFrame
    }

    /// Get the underlying frame reference.
    internal fun getFrame(): BcFrame? = frame
}

/// Allocate a frame and store it in the evaluator.
///
/// After callback finishes, previous frame is restored.
// pub(crate) fn alloca_frame<'v, 'a, 'e, R>(
//     eval: &mut Evaluator<'v, 'a, 'e>,
//     local_count: u32,
//     max_stack_size: u32,
//     loop_depth: LoopDepth,
//     k: impl FnOnce(&mut Evaluator<'v, 'a, 'e>) -> R,
// ) -> R
internal fun <R> allocaFrame(
    eval: Evaluator,
    localCount: Int,
    maxStackSize: Int,
    loopDepth: LoopDepth,
    k: (Evaluator) -> R,
): R {
    val frame = BcFrame(localCount, maxStackSize, loopDepth)
    frame.init()
    val oldFrame = eval.currentFrame
    eval.currentFrame = BcFramePtr(frame)
    return try {
        k(eval)
    } finally {
        eval.currentFrame = oldFrame
    }
}
