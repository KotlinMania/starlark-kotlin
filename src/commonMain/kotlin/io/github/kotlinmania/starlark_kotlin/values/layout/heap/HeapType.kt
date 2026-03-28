// port-lint: source src/values/layout/heap/heap_type.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap

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

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.instant.ProfilerInstant
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.Arena
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.ArenaVisitor
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.arena.Reservation
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.fast_cell.FastCell
import kotlin.math.max
import io.github.kotlinmania.starlark_kotlin.values.value_of.ValueOf
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.StringValueInterner
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValueInterner
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.profile.HeapSummary
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueTyped
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.trace
import io.github.kotlinmania.starlark_kotlin.values.types.list.ptr
import io.github.kotlinmania.starlark_kotlin.values.types.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.owned_frozen_ref.newUnchecked
import io.github.kotlinmania.starlark_kotlin.values.layout.toValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.unpackPtr
import io.github.kotlinmania.starlark_kotlin.values.layout.isUnfrozen
import io.github.kotlinmania.starlark_kotlin.values.layout.newRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.heapCopy
import io.github.kotlinmania.starlark_kotlin.tests.derive.unpackValue
import io.github.kotlinmania.starlark_kotlin.values.layout.heapCopy
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.ProfilerInstant

enum class HeapKind {
    Unfrozen,
    Frozen,
}

/// An owned heap on which Values can be allocated.
///
/// Private for now, but there's no reason it couldn't be public as long as access is restricted to
/// branded functions with signatures like those of Heap.temp
private class OwnedHeap(
    /// Peak memory seen when a garbage collection takes place (may be lower than currently allocated)
    var peakAllocated: Long = 0,
    val arena: FastCell<Arena> = FastCell(),
    val strInterner: StringValueInterner = StringValueInterner(),
    /// Memory I depend on.
    val refs: MutableSet<FrozenHeapRef> = mutableSetOf(),
    var banGc: Boolean = true,
) {
    companion object {
        /// Create a new OwnedHeap.
        fun new(): OwnedHeap = OwnedHeap()
    }
}

/// A heap on which Values can be allocated. The values will be annotated with the heap lifetime.
class Heap internal constructor(
    private val owned: OwnedHeap,
) {
    override fun toString(): String {
        val bytes = owned.arena.tryBorrow()?.allocatedBytes()
        return "Heap(bytes=$bytes)"
    }

    companion object {
        /// Create a heap and use it within the closure.
        ///
        /// Heap is discarded at the end of the closure.
        fun <R> temp(f: (Heap) -> R): R {
            val heap = OwnedHeap.new()
            return f(Heap(heap))
        }

        /// Like temp, but suspend.
        suspend fun <R> tempAsync(f: suspend (Heap) -> R): R {
            val heap = OwnedHeap.new()
            return f(Heap(heap))
        }
    }

    internal fun stringInterner(): StringValueInterner {
        // The lifetime of the interner is the lifetime of the heap.
        return owned.strInterner
    }

    internal fun traceInterner(tracer: Tracer) {
        stringInterner().trace(tracer)
    }

    fun referencedHeaps(): List<FrozenHeapRef> {
        return owned.refs.toList()
    }

    /// Get access to the underlying value within the context of this heap.
    ///
    /// Adds the frozen value's heap as a dependency of this heap.
    fun accessOwnedFrozenValue(v: OwnedFrozenValue): Value {
        addReference(v.owner())
        // Safe: We just added a reference to this heap.
        return v.uncheckedFrozenValue().toValue()
    }

    /// Similar to accessOwnedFrozenValue, but typed.
    fun <T : StarlarkValue> accessOwnedFrozenValueTyped(v: OwnedFrozenValueTyped<T>): ValueTyped<T> {
        addReference(v.owner())
        // Safe: We just added a reference to this heap.
        return v.valueTyped().toValueTyped()
    }

    /// Add a dependency onto the provided frozen heap.
    fun addReference(h: FrozenHeapRef) {
        if (!owned.refs.contains(h)) {
            owned.refs.add(h)
        }
    }

    /// Number of bytes allocated on this heap, not including any memory
    /// allocated outside of the starlark heap.
    fun allocatedBytes(): Long {
        return owned.arena.borrow().allocatedBytes()
    }

    /// Peak memory allocated to this heap, even if the value is now lower
    /// as a result of a subsequent garbage collection.
    fun peakAllocatedBytes(): Long {
        return max(allocatedBytes(), owned.peakAllocated)
    }

    /// Number of bytes allocated by the heap but not yet filled.
    fun availableBytes(): Long {
        return owned.arena.borrow().availableBytes()
    }

    internal fun <A : AValue> allocRaw(x: AValueImpl<A>): ValueTyped<A> {
        val arena = owned.arena.borrow()
        val v: AValueRepr<AValueImpl<A>> = arena.alloc(x)
        return ValueTyped.newRepr(v)
    }

    internal fun <A : AValue> allocRawExtra(x: AValueImpl<A>): Pair<ValueTyped<A>, Any> {
        val arena = owned.arena.borrow()
        val (v, extra) = arena.allocExtra(x)
        val vt = ValueTyped.newRepr<A>(v)
        return Pair(vt, extra)
    }

    internal fun allocStrInit(
        len: Int,
        hash: StarlarkHashValue,
        init: (ByteArray) -> Unit,
    ): StringValue {
        val arena = owned.arena.borrow()
        val v = arena.allocStrInit(len, hash, init)
        // We have an arena inside which stores ValueMem
        // However, we promise not to clear it other than for GC
        // so we can make the arena available longer
        val value = Value.newPtr(v, true)
        return StringValue.newUnchecked(value)
    }

    fun allocStr(x: String): Value {
        val v = owned.arena.borrow().allocStr(x)
        return Value.newPtr(v, true)
    }

    /// Allocate a new value on a Heap.
    fun <T : AllocValue> alloc(x: T): Value {
        return x.allocValue(this)
    }

    /// Allocate a value and return ValueTyped of it.
    /// Can fail if the AllocValue trait generates a different type on the heap.
    fun <T> allocTyped(x: T): ValueTyped<T> where T : AllocValue, T : StarlarkValue {
        return ValueTyped.new<T>(alloc(x))
            ?: error("just allocated value must have the right type")
    }

    /// Allocate a value and return ValueOfUnchecked of it.
    fun <T : AllocValue> allocTypedUnchecked(x: T): ValueOfUnchecked<T> {
        return ValueOfUnchecked.new(alloc(x))
    }

    /// Allocate a value and return ValueOf of it.
    fun <T> allocValueOf(x: T): ValueOf<T> where T : AllocValue, T : UnpackValue {
        val value = alloc(x)
        return ValueOf.unpackValue<T>(value)
            ?: error("just allocated value must be unpackable to the type of value")
    }

    // Safety: The caller must ensure this is safe.
    internal fun visitArena(forwardHeapKind: HeapKind, v: ArenaVisitor) {
        owned.arena.getMut().visitArena(HeapKind.Unfrozen, forwardHeapKind, v)
    }

    /// Allow gcing in this heap.
    ///
    /// This is basically impossible to reason about, hence its existence in the first place.
    internal fun allowGc() {
        owned.banGc = false
    }

    /// Garbage collect any values that are unused. This function is unsafe in
    /// the sense that any Value not returned by Tracer will become
    /// invalid. Furthermore, any references to values, e.g. str will
    /// also become invalid.
    internal fun garbageCollect(f: (Tracer) -> Unit) {
        if (owned.banGc) {
            return
        }

        // Record the highest peak, so it never decreases
        owned.peakAllocated = peakAllocatedBytes()
        garbageCollectInternal(f)
    }

    private fun garbageCollectInternal(f: (Tracer) -> Unit) {
        // Must rewrite all Value's so they point at the new heap.
        // Take the arena out of the heap to make sure nobody allocates in it,
        // but hold the reference until the GC is done.
        val _arena = owned.arena.take()

        val tracer = Tracer(
            arena = Arena(),
        )
        f(tracer)
        owned.arena.set(tracer.arena)
    }

    /// Obtain a summary of how much memory is currently allocated by this heap.
    fun allocatedSummary(): HeapSummary {
        return owned.arena.borrow().allocatedSummary()
    }

    internal fun recordCallEnter(function: Value) {
        val time = ProfilerInstant.now()
        allocComplexNoFreeze(CallEnter(
            function = function,
            time = time,
            maybeDrop = NeedsDrop,
        ))
        allocComplexNoFreeze(CallEnter(
            function = function,
            time = time,
            maybeDrop = NoDrop,
        ))
    }

    internal fun recordCallExit() {
        val time = ProfilerInstant.now()
        allocSimple(CallExit(
            time = time,
            maybeDrop = NeedsDrop,
        ))
        allocSimple(CallExit(
            time = time,
            maybeDrop = NoDrop,
        ))
    }

    // Internal allocation helpers — stubs that delegate to arena
    internal fun allocComplexNoFreeze(value: Any) {
        owned.arena.borrow().allocComplex(value)
    }

    internal fun allocSimple(value: Any) {
        owned.arena.borrow().allocSimple(value)
    }
}

/// A heap on which FrozenValues can be allocated.
/// Can be kept alive by a FrozenHeapRef.
class FrozenHeap(
    /// My memory.
    private val arena: Arena = Arena(),
    /// Memory I depend on.
    private val refs: MutableSet<FrozenHeapRef> = mutableSetOf(),
    /// String interner.
    private val strInterner: FrozenStringValueInterner = FrozenStringValueInterner(),
) {
    override fun toString(): String {
        val bytes = arena.allocatedBytes()
        val refCount = refs.size
        return "FrozenHeap(bytes=$bytes, refs=$refCount)"
    }

    companion object {
        /// Create a new FrozenHeap.
        fun new(): FrozenHeap = FrozenHeap()
    }

    /// into_ref but also assign a name.
    ///
    /// See FrozenHeapRef.name for more details.
    fun nameAndIntoRef(name: Any): FrozenHeapRef {
        return intoRefImpl(name)
    }

    /// After all values have been allocated, convert the FrozenHeap into a
    /// FrozenHeapRef which can be cloned, shared between threads,
    /// and ensures the underlying values allocated on the FrozenHeap remain valid.
    fun intoRef(): FrozenHeapRef {
        return intoRefImpl(null)
    }

    internal fun intoRefImpl(name: Any?): FrozenHeapRef {
        arena.finish()
        if (arena.isEmpty() && refs.isEmpty()) {
            return FrozenHeapRef()
        } else {
            return FrozenHeapRef(FrozenFrozenHeap(
                arena = arena,
                refs = refs.toList(),
                name = name,
            ))
        }
    }

    /// Keep the argument FrozenHeapRef alive as long as this FrozenHeap
    /// is kept alive. Used if a FrozenValue in this heap points at values in another
    /// FrozenHeap.
    fun addReference(heap: FrozenHeapRef) {
        if (heap.inner == null) {
            return
        }

        if (!refs.contains(heap)) {
            refs.add(heap)
        }
    }

    internal fun stringInterner(): FrozenStringValueInterner {
        return strInterner
    }

    internal fun <T : AValue> allocRaw(x: AValueImpl<T>): FrozenValueTyped<T> {
        val v: AValueRepr<AValueImpl<T>> = arena.alloc(x)
        return FrozenValueTyped.newRepr(v)
    }

    internal fun <T : AValue> allocRawExtra(x: AValueImpl<T>): Pair<FrozenValueTyped<T>, Any> {
        val (v, extra) = arena.allocExtra(x)
        val fv = FrozenValueTyped.newRepr<T>(v)
        return Pair(fv, extra)
    }

    internal fun allocStrInit(
        len: Int,
        hash: StarlarkHashValue,
        init: (ByteArray) -> Unit,
    ): FrozenStringValue {
        val v = arena.allocStrInit(len, hash, init)
        val value = FrozenValue.newPtr(v, true)
        return FrozenStringValue.newUnchecked(value)
    }

    /// Allocate a new value on a FrozenHeap.
    fun <T : AllocFrozenValue> alloc(v: T): FrozenValue {
        return v.allocFrozenValue(this)
    }

    /// Allocate a value and return FrozenValueOfUnchecked of it.
    fun <T : AllocFrozenValue> allocTypedUnchecked(v: T): FrozenValueOfUnchecked<T> {
        return FrozenValueOfUnchecked.new(v.allocFrozenValue(this))
    }

    /// Number of bytes allocated on this heap, not including any memory
    /// allocated outside of the starlark heap.
    fun allocatedBytes(): Long {
        return arena.allocatedBytes()
    }

    /// Number of bytes allocated by the heap but not yet filled.
    fun availableBytes(): Long {
        return arena.availableBytes()
    }

    /// Obtain a summary of how much memory is currently allocated by this heap.
    fun allocatedSummary(): HeapSummary {
        return arena.allocatedSummary()
    }

    internal fun <T : AValue> reserveWithExtra(
        extraLen: Int,
    ): Triple<FrozenValue, Reservation<T>, Any> {
        val (r, extra) = arena.reserveWithExtra<T>(extraLen)
        val fv = FrozenValue.newPtr(r.ptr(), false)
        return Triple(fv, r, extra)
    }
}

typealias FrozenHeapName = Any

/// FrozenHeap when it is no longer modified and can be shared between threads.
/// Although, arena is not safe to share between threads, but at least refs is.
class FrozenFrozenHeap(
    val arena: Arena,
    val refs: List<FrozenHeapRef>,
    val name: Any? = null,
) {
    override fun toString(): String {
        val bytes = arena.allocatedBytes()
        val refCount = refs.size
        return "FrozenHeap(bytes=$bytes, refs=$refCount)"
    }
}

/// A reference to a FrozenHeap that keeps alive all values on the underlying heap.
/// Note that the Hash is consistent for a single FrozenHeapRef, but non-deterministic
/// across executions and distinct but observably identical FrozenHeapRef values.
class FrozenHeapRef(
    // The Eq/Hash are by identity rather than value, since we produce unique values
    // given an underlying FrozenHeap.
    internal val inner: FrozenFrozenHeap? = null,
) {
    /// Number of bytes allocated on this heap, not including any memory
    /// allocated outside of the starlark heap.
    fun allocatedBytes(): Long {
        return inner?.arena?.allocatedBytes() ?: 0
    }

    /// Number of bytes allocated by the heap but not filled.
    /// Note that these bytes will never be filled as no further allocations can
    /// be made on this heap (it has been sealed).
    fun availableBytes(): Long {
        return inner?.arena?.availableBytes() ?: 0
    }

    /// Obtain a summary of how much memory is currently allocated by this heap.
    /// Doesn't include the heaps it keeps alive by reference.
    fun allocatedSummary(): HeapSummary {
        return inner?.arena?.allocatedSummary() ?: HeapSummary()
    }

    /// Get the name of this heap.
    ///
    /// Names can be assigned when finalizing frozen heaps; in practice, this is done when freezing
    /// modules, see Module.freezeAndName.
    ///
    /// The name is intentionally made available here and not at a higher point like the module
    /// level so that it can be inspected even when traversing the dependency graph of frozen heaps.
    fun name(): Any? {
        return inner?.name
    }

    /// Get the frozen heaps that this frozen heap depends on.
    fun refs(): List<FrozenHeapRef> {
        return inner?.refs ?: emptyList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenHeapRef) return false
        return inner === other.inner
    }

    override fun hashCode(): Int {
        return System.identityHashCode(inner)
    }
}

/// Used to perform garbage collection by Trace.trace.
class Tracer(
    internal val arena: Arena = Arena(),
) {
    /// Walk over a value during garbage collection.
    fun trace(value: ValueHolder) {
        value.value = adjust(value.value)
    }

    /// Helper function to annotate that this field has been considered for tracing,
    /// but is not relevant because it has a static lifetime containing no relevant values.
    /// Does nothing.
    fun <T> traceStatic(@Suppress("UNUSED_PARAMETER") value: T) {
        // Nothing to do because T can't contain the relevant lifetime
    }

    internal fun <T : AValue> reserve(): Pair<Value, Reservation<T>> {
        val (v, r, extra) = reserveWithExtra<T>(0)
        return Pair(v, r)
    }

    internal fun <T : AValue> reserveWithExtra(
        extraLen: Int,
    ): Triple<Value, Reservation<T>, Any> {
        val (r, extra) = arena.reserveWithExtra<T>(extraLen)
        val v = Value.newPtr(r.ptr(), false)
        return Triple(v, r, extra)
    }

    fun allocStr(x: String): Value {
        val v = arena.allocStr(x)
        return Value.newPtr(v, true)
    }

    private fun adjust(value: Value): Value {
        // Case 1, doesn't point at the old arena
        if (!value.isUnfrozen()) {
            return value
        }
        val oldVal = value.unpackPtr() ?: return value

        // Case 2: We have already been replaced with a forwarding, or need to freeze
        return when (val unpacked = oldVal.unpack()) {
            is AValueOrForwardUnpack.Forward -> unpacked.forwardPtr().unpackUnfrozenValue()
            is AValueOrForwardUnpack.Header -> unpacked.header.unpack().heapCopy(this)
            else -> throw IllegalStateException("Unexpected unpack result: $unpacked")
        }
    }
}

/// Mutable holder for Value, used during tracing.
class ValueHolder(var value: Value)
