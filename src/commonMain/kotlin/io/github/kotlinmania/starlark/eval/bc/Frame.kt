
// port-lint: source eval/bc/frame.rs
package io.github.kotlinmania.starlark.eval.bc

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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
 * [ loopIndices | BcFrame | locals | stack ]
 *   BcFramePtr points here ^
 * ```
 */
internal class BcFrame(
    /** Number of local slots. */
    val localCount: Int,
    /** Number of stack slots. */
    val maxStackSize: Int,
    /** Max number of nested for loops. */
    val maxLoopDepth: LoopDepth,
) {
    /** `localCount` local slots followed by `maxStackSize` stack slots. */
    val slots: Array<Value?> = arrayOfNulls(localCount + maxStackSize)

    /** Loop iteration indices, stored separately. */
    val loopIndices: IntArray = IntArray(maxLoopDepth.depth)

    fun framePtr(): BcFramePtr = BcFramePtr(this)

    fun localsMut(): Array<Value?> {
        // Returns the backing slots array. Callers must only access indices 0 until localCount.
        return slots
    }

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
        val start = range.start.get().index.toInt()
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

/** Pointer to a [BcFrame]. */
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

    fun getSlotSlow(slot: LocalSlotIdCapturedOrNot): Value? {
        val f = frame!!
        check(slot.index < f.localCount.toUInt())
        return f.getSlot(slot)
    }

    fun getSlot(slot: LocalSlotIdCapturedOrNot): Value? {
        return frame!!.getSlot(slot)
    }

    fun setSlotSlow(slot: LocalSlotIdCapturedOrNot, value: Value) {
        val f = frame!!
        check(slot.index < f.localCount.toUInt())
        f.setSlot(slot, value)
    }

    fun setSlot(slot: LocalSlotIdCapturedOrNot, value: Value) {
        frame!!.setSlot(slot, value)
    }

    fun getBcSlot(slot: BcSlotIn): Value {
        return frame!!.getBcSlot(slot)
    }

    fun setBcSlot(slot: BcSlotOut, value: Value) {
        frame!!.setBcSlot(slot, value)
    }

    fun getBcSlotRange(slots: BcSlotInRange): List<Value> {
        return frame!!.getBcSlotRange(slots)
    }

    fun getIterIndex(loopDepth: LoopDepth): Int {
        return frame!!.getIterIndex(loopDepth)
    }

    fun setIterIndex(loopDepth: LoopDepth, index: Int) {
        frame!!.setIterIndex(loopDepth, index)
    }

    fun maxStackSize(): Int {
        return frame!!.maxStackSize
    }

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

/**
 * Trace implementation for [BcFramePtr].
 * Delegates to the underlying [BcFrame]'s trace.
 */
internal fun BcFramePtr.trace(tracer: Tracer) {
    getFrame()?.trace(tracer)
}

/** Allocate raw frame memory. */
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
