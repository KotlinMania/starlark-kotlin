
// port-lint: source src/eval/bc/frame.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

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

/**
 * Local variables and stack, in single allocation.
 */

import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder

/**
 * Current `def` frame (but not native function frame).
 *
 * Frame memory layout (conceptual, from Rust):
 *
 * ```text
 * [ loop_indices | BcFrame | locals | stack ]
 *   BcFramePtr points here ^
 * ```
 *
 * In Kotlin, we use safe arrays instead of raw pointer arithmetic.
 * Loop indices are stored in a separate [IntArray], and locals/stack
 * share a single [Array] of nullable [Value].
 */
// #[repr(C)]
// struct BcFrame<'v> {
//     local_count: u32,
//     max_stack_size: u32,
//     max_loop_depth: LoopDepth,
//     slots: [Option<Value<'v>>; 0],
// }
internal class BcFrame(
    /** Number of local slots. */
    val localCount: Int,
    /** Number of stack slots. */
    val maxStackSize: Int,
    /** Max number of nested for loops. */
    val maxLoopDepth: LoopDepth,
) {
    /**
     * `localCount` local slots followed by `maxStackSize` stack slots.
     *
     * In Rust this is a zero-length trailing array accessed via raw pointers.
     * In Kotlin we use a safe array.
     */
    val slots: Array<Value?> = arrayOfNulls(localCount + maxStackSize)

    /**
     * Loop iteration indices, stored separately.
     *
     * In Rust these are stored in memory before the BcFrame header
     * and accessed via pointer arithmetic. In Kotlin we use a safe [IntArray].
     */
    val loopIndices: IntArray = IntArray(maxLoopDepth.depth)

    // impl BcFrame

    // fn offset_of_slots() -> usize
    // Not needed in Kotlin -- no raw pointer arithmetic.

    // fn frame_ptr(&mut self) -> BcFramePtr<'v>
    fun framePtr(): BcFramePtr = BcFramePtr(this)

    // fn locals_mut(&mut self) -> &mut [Option<Value<'v>>]
    fun localsMut(): Array<Value?> {
        // Returns the backing slots array. Callers must only access indices 0 until localCount.
        return slots
    }

    // fn locals_uninit(&mut self) -> &mut [MaybeUninit<Option<Value<'v>>>]
    // fn stack_uninit(&mut self) -> &mut [MaybeUninit<Value<'v>>]
    // Not needed in Kotlin -- arrays are initialized by the runtime.

    /**
     * Initialize frame after it was allocated.
     *
     * Sets all local slots to `null` (unassigned). In debug mode, fills stack
     * slots with a sentinel to help catch use-before-write bugs.
     */
    // fn init(&mut self)
    fun init() {
        // Locals start as null (None / unassigned).
        for (i in 0 until localCount) {
            slots[i] = null
        }

        // In Kotlin, stack slots are already null from arrayOfNulls.
        // The Rust code writes junk bytes in debug mode to trigger memory errors
        // if the stack is used incorrectly. Kotlin's null default serves a similar
        // purpose: reading an uninitialized stack slot will produce null, which
        // will be caught by the non-null assertion in getBcSlot.
    }

    /**
     * Gets a local variable. Returns null to indicate the variable is not yet assigned.
     */
    // pub(crate) fn get_slot(&self, slot: LocalSlotIdCapturedOrNot) -> Option<Value<'v>>
    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? {
        check(slot.index < localCount.toUInt())
        return slots[slot.index.toInt()]
    }

    /**
     * Get a stack slot.
     */
    // pub(crate) fn get_bc_slot(&self, slot: BcSlotIn) -> Value<'v>
    fun getBcSlot(slot: BcSlotIn): Value {
        check(slot.get().index < (localCount + maxStackSize).toUInt())
        // Slot must always be initialized.
        return slots[slot.get().index.toInt()]!!
    }

    // pub(crate) fn set_slot(&mut self, slot: LocalSlotIdCapturedOrNot, value: Value<'v>)
    fun setSlot(slot: LocalSlotIdCapturedOrNot, value: Value) {
        check(slot.index < localCount.toUInt())
        slots[slot.index.toInt()] = value
    }

    // pub(crate) fn get_bc_slot_range(&self, slots: BcSlotInRange) -> &[Value<'v>]
    fun getBcSlotRange(range: BcSlotInRange): List<Value> {
        check(range.end.get().index <= (localCount + maxStackSize).toUInt())
        val start = range.start.get().index.toInt()
        val end = start + range.len().toInt()
        return (start until end).map { slots[it]!! }
    }

    // pub(crate) fn set_bc_slot(&mut self, slot: BcSlotOut, value: Value<'v>)
    fun setBcSlot(slot: BcSlotOut, value: Value) {
        check(slot.get().index < (localCount + maxStackSize).toUInt())
        slots[slot.get().index.toInt()] = value
    }

    // pub(crate) fn set_iter_index(&mut self, iter_index: LoopDepth, index: usize)
    fun setIterIndex(iterIndex: LoopDepth, index: Int) {
        check(iterIndex < maxLoopDepth)
        loopIndices[iterIndex.depth] = index
    }

    // pub(crate) fn get_iter_index(&self, iter_index: LoopDepth) -> usize
    fun getIterIndex(iterIndex: LoopDepth): Int {
        check(iterIndex < maxLoopDepth)
        return loopIndices[iterIndex.depth]
    }
}

// unsafe impl<'v> Trace<'v> for BcFrame<'v> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.locals_mut().trace(tracer);
//         // Note this does not trace the stack.
//         // GC can be performed only when the stack is empty.
//     }
// }
/**
 * Trace implementation for [BcFrame].
 *
 * Only traces local variable slots, not the stack.
 * GC can only be performed when the stack is empty.
 */
internal fun BcFrame.trace(tracer: Tracer) {
    for (i in 0 until localCount) {
        val v = slots[i]
        if (v != null) {
            val holder = ValueHolder(v)
            tracer.trace(holder)
            slots[i] = holder.value
        }
    }
}

/**
 * Pointer to a [BcFrame].
 *
 * In Rust, this stores a raw pointer to the `slots` field for efficiency.
 * In Kotlin, we simply hold a nullable reference to the [BcFrame].
 */
// #[derive(Copy, Clone, Dupe)]
// pub(crate) struct BcFramePtr<'v> {
//     slots_ptr: *mut Option<Value<'v>>,
// }
class BcFramePtr internal constructor(
    private var frame: BcFrame?,
) {
    companion object {
        // pub(crate) fn null() -> BcFramePtr<'v>
        fun nullPtr(): BcFramePtr = BcFramePtr(null)
    }

    // impl PartialEq for BcFramePtr
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BcFramePtr) return false
        return frame === other.frame
    }

    override fun hashCode(): Int = frame?.hashCode() ?: 0

    /**
     * Is this frame allocated or constructed empty?
     */
    // pub(crate) fn is_inititalized(self) -> bool
    fun isInitialized(): Boolean = frame != null

    // #[inline(always)]
    // fn frame<'a>(self) -> &'a BcFrame<'v>
    // fn frame_mut<'a>(self) -> &'a mut BcFrame<'v>
    // In Kotlin we simply access the non-null frame reference.

    // pub(crate) fn get_slot_slow(self, slot: LocalSlotIdCapturedOrNot) -> Option<Value<'v>>
    fun getSlotSlow(slot: LocalSlotIdCapturedOrNot): Value? {
        val f = frame!!
        check(slot.index < f.localCount.toUInt())
        return f.getSlot(slot)
    }

    // pub(crate) fn get_slot(self, slot: LocalSlotIdCapturedOrNot) -> Option<Value<'v>>
    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? {
        return frame!!.getSlot(slot)
    }

    // pub(crate) fn set_slot_slow(self, slot: LocalSlotIdCapturedOrNot, value: Value<'v>)
    fun setSlotSlow(slot: LocalSlotIdCapturedOrNot, value: Value) {
        val f = frame!!
        check(slot.index < f.localCount.toUInt())
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

    /** Set the underlying frame reference. */
    internal fun setFrame(newFrame: BcFrame?) {
        frame = newFrame
    }

    /** Get the underlying frame reference. */
    internal fun getFrame(): BcFrame? = frame
}

// unsafe impl<'v> Trace<'v> for BcFramePtr<'v> {
//     fn trace(&mut self, tracer: &Tracer<'v>) {
//         self.frame_mut().trace(tracer);
//     }
// }
/**
 * Trace implementation for [BcFramePtr].
 * Delegates to the underlying [BcFrame]'s trace.
 */
internal fun BcFramePtr.trace(tracer: Tracer) {
    getFrame()?.trace(tracer)
}

// #[inline(always)]
// fn alloca_raw<'v, 'a, 'e, R>(
//     eval: &mut Evaluator<'v, 'a, 'e>,
//     local_count: u32,
//     max_stack_size: u32,
//     max_loop_depth: LoopDepth,
//     k: impl FnOnce(&mut Evaluator<'v, 'a, 'e>, BcFramePtr<'v>) -> R,
// ) -> R
/**
 * Allocate raw frame memory.
 *
 * In Rust, this uses stack-like allocation via the evaluator's alloca.
 * In Kotlin, we simply construct a [BcFrame] on the heap.
 */
private fun <R> allocaRaw(
    eval: Evaluator,
    localCount: Int,
    maxStackSize: Int,
    maxLoopDepth: LoopDepth,
    k: (Evaluator, BcFramePtr) -> R,
): R {
    val frame = BcFrame(localCount, maxStackSize, maxLoopDepth)
    return k(eval, frame.framePtr())
}

/**
 * Allocate a frame and store it in the evaluator.
 *
 * After the callback finishes, the previous frame is restored.
 */
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
    return allocaRaw(eval, localCount, maxStackSize, loopDepth) { ev, framePtr ->
        framePtr.getFrame()!!.init()
        val oldFrame = ev.currentFrame
        ev.currentFrame = framePtr
        try {
            k(ev)
        } finally {
            ev.currentFrame = oldFrame
        }
    }
}
