
// port-lint: source src/eval/bc/frame.rs
package io.github.kotlinmania.starlark.eval.bc

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

import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.LocalSlotIdCapturedOrNot
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder

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
//     local_count: u32,
//     max_stack_size: u32,
//     max_loop_depth: LoopDepth,
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

    // Not needed in Kotlin -- no raw pointer arithmetic.

    fun framePtr(): BcFramePtr = BcFramePtr(this)

    fun localsMut(): Array<Value?> {
        // Returns the backing slots array. Callers must only access indices 0 until localCount.
        return slots
    }

    // Not needed in Kotlin -- arrays are initialized by the runtime.

    /**
     * Initialize frame after it was allocated.
     *
     * Sets all local slots to `null` (unassigned). In debug mode, fills stack
     * slots with a sentinel to help catch use-before-write bugs.
     */
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
    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? {
        check(slot.index < localCount.toUInt())
        return slots[slot.index.toInt()]
    }

    /**
     * Get a stack slot.
     */
    fun getBcSlot(slot: BcSlotIn): Value {
        check(slot.get().index < (localCount + maxStackSize).toUInt())
        // Slot must always be initialized.
        return slots[slot.get().index.toInt()]
            ?: error("BcFrame slot ${slot.get().index} is uninitialized (localCount=$localCount, maxStackSize=$maxStackSize)")
    }

    fun setSlot(slot: LocalSlotIdCapturedOrNot, value: Value) {
        check(slot.index < localCount.toUInt())
        slots[slot.index.toInt()] = value
    }

    fun getBcSlotRange(range: BcSlotInRange): List<Value> {
        check(range.end.get().index <= (localCount + maxStackSize).toUInt())
        val start =
            range.start
                .get()
                .index
                .toInt()
        val end = start + range.len().toInt()
        return (start until end).map { slots[it] ?: error("BcFrame slot $it is uninitialized in range $start..$end") }
    }

    fun setBcSlot(slot: BcSlotOut, value: Value) {
        check(slot.get().index < (localCount + maxStackSize).toUInt())
        slots[slot.get().index.toInt()] = value
    }

    fun setIterIndex(iterIndex: LoopDepth, index: Int) {
        check(iterIndex < maxLoopDepth)
        loopIndices[iterIndex.depth] = index
    }

    fun getIterIndex(iterIndex: LoopDepth): Int {
        check(iterIndex < maxLoopDepth)
        return loopIndices[iterIndex.depth]
    }
}

//         self.locals_mut().trace(tracer);
//         // Note this does not trace the stack.
//         // GC can be performed only when the stack is empty.

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
internal class BcFramePtr internal constructor(
    private var frame: BcFrame?,
) {
    companion object {
        fun nullPtr(): BcFramePtr = BcFramePtr(null)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BcFramePtr) return false
        return frame === other.frame
    }

    override fun hashCode(): Int = frame?.hashCode() ?: 0

    /**
     * Is this frame allocated or constructed empty?
     */
    fun isInitialized(): Boolean = frame != null

    // In Kotlin we simply access the non-null frame reference.

    fun getSlotSlow(slot: LocalSlotIdCapturedOrNot): Value? {
        val f = frame!!
        check(slot.index < f.localCount.toUInt())
        return f.getSlot(slot)
    }

    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? = frame!!.getSlot(slot)

    fun setSlotSlow(slot: LocalSlotIdCapturedOrNot, value: Value) {
        val f = frame!!
        check(slot.index < f.localCount.toUInt())
        f.setSlot(slot, value)
    }

    fun setSlot(slot: LocalSlotIdCapturedOrNot, value: Value) {
        frame!!.setSlot(slot, value)
    }

    internal fun getBcSlot(slot: BcSlotIn): Value = frame!!.getBcSlot(slot)

    internal fun setBcSlot(slot: BcSlotOut, value: Value) {
        frame!!.setBcSlot(slot, value)
    }

    internal fun getBcSlotRange(slots: BcSlotInRange): List<Value> = frame!!.getBcSlotRange(slots)

    internal fun getIterIndex(loopDepth: LoopDepth): Int = frame!!.getIterIndex(loopDepth)

    internal fun setIterIndex(loopDepth: LoopDepth, index: Int) {
        frame!!.setIterIndex(loopDepth, index)
    }

    fun maxStackSize(): Int = frame!!.maxStackSize

    fun localsMut(): Array<Value?> = frame!!.localsMut()

    /** Set the underlying frame reference. */
    internal fun setFrame(newFrame: BcFrame?) {
        frame = newFrame
    }

    /** Get the underlying frame reference. */
    internal fun getFrame(): BcFrame? = frame
}

//         self.frame_mut().trace(tracer);

/**
 * Trace implementation for [BcFramePtr].
 * Delegates to the underlying [BcFrame]'s trace.
 */
internal fun BcFramePtr.trace(tracer: Tracer) {
    getFrame()?.trace(tracer)
}

//     local_count: u32,
//     max_stack_size: u32,
//     max_loop_depth: LoopDepth,
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
//     local_count: u32,
//     max_stack_size: u32,
//     loop_depth: LoopDepth,
// ) -> R
internal fun <R> allocaFrame(
    eval: Evaluator,
    localCount: Int,
    maxStackSize: Int,
    loopDepth: LoopDepth,
    k: (Evaluator) -> R,
): R =
    allocaRaw(eval, localCount, maxStackSize, loopDepth) { ev, framePtr ->
        framePtr.getFrame()!!.init()
        val oldFrame = ev.currentFrame
        ev.currentFrame = framePtr
        try {
            k(ev)
        } finally {
            ev.currentFrame = oldFrame
        }
    }
