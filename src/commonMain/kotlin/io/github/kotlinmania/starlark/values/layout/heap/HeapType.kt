// port-lint: source src/values/layout/heap/heap_type.rs
package io.github.kotlinmania.starlark.values.layout.heap

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

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.eval.runtime.profile.ProfilerInstant
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.FrozenValueOfUnchecked
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.ValueOfUnchecked
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueTyped
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueTyped
import io.github.kotlinmania.starlark.values.layout.heapCopy
import io.github.kotlinmania.starlark.values.layout.constantString
import io.github.kotlinmania.starlark.values.layout.avalues.AValueComplexNoFreeze
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplexNoFreeze
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.avalues.simple.simple
import io.github.kotlinmania.starlark.values.layout.heap.arena.Arena
import io.github.kotlinmania.starlark.values.layout.heap.arena.ArenaVisitor
import io.github.kotlinmania.starlark.values.layout.heap.arena.Reservation
import io.github.kotlinmania.starlark.values.layout.heap.profile.HeapSummary
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark.values.owned.OwnedFrozenValueTyped
import io.github.kotlinmania.starlark.values.traceRecursiveGuardStacks
import io.github.kotlinmania.starlark.values.types.string.intern.FrozenStringValueInterner
import io.github.kotlinmania.starlark.values.types.string.intern.StringValueInterner
import io.github.kotlinmania.starlark.values.valueof.ValueOf
import kotlin.math.max

enum class HeapKind {
    Unfrozen,
    Frozen,
}

/**
 * An owned heap on which Values can be allocated.
 *
 * Private for now, but there's no reason it couldn't be public as long as access is restricted to
 * branded functions with signatures like those of Heap.temp
 */
internal class OwnedHeap(
    /** Peak memory seen when a garbage collection takes place (may be lower than currently allocated) */
    var peakAllocated: Int = 0,
    val arena: FastCell<Arena> = FastCell.default(Arena()),
    val strInterner: StringValueInterner = StringValueInterner(),
    /** Memory I depend on. */
    val refs: MutableSet<FrozenHeapRef> = mutableSetOf(),
    var banGc: Boolean = true,
) {
    companion object {
        /** Create a new OwnedHeap. */
        fun new(): OwnedHeap = OwnedHeap()
    }
}

/** A heap on which Values can be allocated. The values will be annotated with the heap lifetime. */
class Heap internal constructor(
    private val owned: OwnedHeap,
) {
    override fun toString(): String {
        val bytes = owned.arena.tryBorrow()?.allocatedBytes()
        return "Heap(bytes=$bytes)"
    }

    companion object {
        /**
         * Create a heap and use it within the closure.
         *
         * Heap is discarded at the end of the closure.
         */
        fun <R> temp(f: (Heap) -> R): R {
            val heap = OwnedHeap.new()
            try {
                return f(Heap(heap))
            } finally {
                heap.arena.borrow().finish()
                for (ref in heap.refs) {
                    ref.close()
                }
            }
        }

        /** Like temp, but suspend. */
        suspend fun <R> tempAsync(f: suspend (Heap) -> R): R {
            val heap = OwnedHeap.new()
            try {
                return f(Heap(heap))
            } finally {
                heap.arena.borrow().finish()
                for (ref in heap.refs) {
                    ref.close()
                }
            }
        }
    }

    internal fun stringInterner(): StringValueInterner {
        // The lifetime of the interner is the lifetime of the heap.
        return owned.strInterner
    }

    internal fun traceInterner(tracer: Tracer) {
        // In Rust, this traces the string interner's HashTable entries.
        // Kotlin's GC handles memory management, so explicit tracing is a no-op,
        // but we call through for structural parity.
        owned.strInterner.trace(tracer)
    }

    fun referencedHeaps(): List<FrozenHeapRef> = owned.refs.toList()

    /**
     * Get access to the underlying value within the context of this heap.
     *
     * Adds the frozen value's heap as a dependency of this heap.
     */
    fun accessOwnedFrozenValue(v: OwnedFrozenValue): Value {
        addReference(v.owner())
        // Safe: We just added a reference to this heap.
        return v.uncheckedFrozenValue().toValue()
    }

    /** Similar to accessOwnedFrozenValue, but typed. */
    fun <T : StarlarkValue> accessOwnedFrozenValueTyped(v: OwnedFrozenValueTyped<T>): ValueTyped<T> {
        addReference(v.owner())
        // Safe: We just added a reference to this heap.
        return v.valueTyped().toValueTyped()
    }

    /** Add a dependency onto the provided frozen heap. */
    fun addReference(h: FrozenHeapRef) {
        if (!owned.refs.contains(h)) {
            owned.refs.add(h.clone())
        }
    }

    /**
     * Number of bytes allocated on this heap, not including any memory
     * allocated outside of the starlark heap.
     */
    fun allocatedBytes(): Int = owned.arena.borrow().allocatedBytes()

    /**
     * Peak memory allocated to this heap, even if the value is now lower
     * as a result of a subsequent garbage collection.
     */
    fun peakAllocatedBytes(): Int = max(allocatedBytes(), owned.peakAllocated)

    /** Number of bytes allocated by the heap but not yet filled. */
    fun availableBytes(): Int = owned.arena.borrow().availableBytes()

    internal fun <A : AValue> allocRaw(x: AValueImpl<A>): ValueTyped<StarlarkValue> {
        val arena = owned.arena.borrow()
        val v = arena.alloc(x)
        val value = Value.newPtr(v.header, v.header.vtable.isStr)
        return ValueTyped.newUnchecked(value)
    }

    internal fun <A : AValue> allocRawExtra(x: AValueImpl<A>): Pair<ValueTyped<StarlarkValue>, Any> {
        val arena = owned.arena.borrow()
        val v = arena.allocExtra(x)
        val value = Value.newPtr(v.header, v.header.vtable.isStr)
        val vt = ValueTyped.newUnchecked<StarlarkValue>(value)
        return Pair(vt, Unit)
    }

    private fun <A : AValue> allocRawNoDrop(x: AValueImpl<A>): ValueTyped<StarlarkValue> {
        val arena = owned.arena.borrow()
        val v = arena.allocNoDrop(x)
        val value = Value.newPtr(v.header, v.header.vtable.isStr)
        return ValueTyped.newUnchecked(value)
    }

    internal fun allocStrInit(
        len: Int,
        hash: StarlarkHashValue,
        init: (ByteArray) -> Unit,
    ): StringValue {
        val arena = owned.arena.borrow()
        val bytes = ByteArray(len)
        init(bytes)
        val header = arena.allocStr(bytes.decodeToString())
        // We have an arena inside which stores ValueMem
        // However, we promise not to clear it other than for GC
        // so we can make the arena available longer
        val value = Value.newPtr(header, true)
        return StringValue.newUnchecked(value)
    }
    fun allocStr(x: String): Value {
        val constant = constantString(x)
        if (constant != null) {
            return constant.toValue()
        }
        val v = owned.arena.borrow().allocStr(x)
        return Value.newPtr(v, true)
    }    /** Allocate a new value on a Heap. */
    fun <T : AllocValue> alloc(x: T): Value = x.allocValue(this)

    /**
     * Allocate a value and return ValueTyped of it.
     * Can fail if the AllocValue trait generates a different type on the heap.
     */
    internal inline fun <reified T> allocTyped(x: T): ValueTyped<T> where T : AllocValue, T : StarlarkValue =
        ValueTyped.new<T>(alloc(x))
            ?: error("just allocated value must have the right type")

    /** Allocate a value and return ValueOfUnchecked of it. */
    fun <T : AllocValue> allocTypedUnchecked(x: T): ValueOfUnchecked<T> = ValueOfUnchecked.new(alloc(x))

    /** Allocate a value and return ValueOf of it. */
    internal inline fun <reified T> allocValueOf(x: T): ValueOf<T> where T : AllocValue, T : Any {
        val value = alloc(x)
        return ValueOf.unpackValueImpl<T>(value)
            ?: error("just allocated value must be unpackable to the type of value")
    }

    // Safety: The caller must ensure this is safe.
    internal fun visitArena(forwardHeapKind: HeapKind, v: ArenaVisitor) {
        owned.arena.getMut().visitArena(HeapKind.Unfrozen, forwardHeapKind, v)
    }

    /**
     * Allow gcing in this heap.
     *
     * This is basically impossible to reason about, hence its existence in the first place.
     */
    internal fun allowGc() {
        owned.banGc = false
    }

    /**
     * Garbage collect any values that are unused. This function is unsafe in
     * the sense that any Value not returned by Tracer will become
     * invalid. Furthermore, any references to values, e.g. str will
     * also become invalid.
     */
    internal fun garbageCollect(f: (Tracer) -> Unit) {
        if (owned.banGc) {
            return
        }

        // Record the highest peak, so it never decreases
        owned.peakAllocated = peakAllocatedBytes()
        garbageCollectInternal(f)
    }

    private fun garbageCollectInternal(f: (Tracer) -> Unit) {
        val retainedArena = owned.arena.take()
        var success = false
        try {
            val maxOldIndex = AValueHeader.currentCounter()
            val tracer =
                Tracer(
                    arena = Arena(),
                    maxOldIndex = maxOldIndex,
                )
            f(tracer)
            traceRecursiveGuardStacks(tracer)
            owned.arena.set(tracer.arena)
            success = true
        } finally {
            if (!success) {
                owned.arena.set(retainedArena)
            } else {
                retainedArena.finish()
            }
        }
    }

    /** Obtain a summary of how much memory is currently allocated by this heap. */
    fun allocatedSummary(): HeapSummary = owned.arena.borrow().allocatedSummary()

    internal fun recordCallEnter(function: Value) {
        val time = ProfilerInstant.now()
        this.allocComplexNoFreeze(
            CallEnter<NeedsDrop>(
                function = function,
                time = time,
                maybeDrop = NeedsDrop(),
            ),
        )
        val noDropEnter =
            CallEnter<NoDrop>(
                function = function,
                time = time,
                maybeDrop = NoDrop(),
            )
        this.allocRawNoDrop(
            AValueImpl.new(noDropEnter, AValueComplexNoFreeze(noDropEnter)),
        )
    }

    internal fun recordCallExit() {
        val time = ProfilerInstant.now()
        this.allocSimple(
            CallExit<NeedsDrop>(
                time = time,
                maybeDrop = NeedsDrop(),
            ),
        )
        val noDropExit =
            CallExit<NoDrop>(
                time = time,
                maybeDrop = NoDrop(),
            )
        this.allocRawNoDrop(
            simple(noDropExit),
        )
    }
}

/**
 * A heap on which FrozenValues can be allocated.
 * Can be kept alive by a FrozenHeapRef.
 */
class FrozenHeap internal constructor(
    /** My memory. */
    private val arena: Arena = Arena(),
    /** Memory I depend on. */
    private val refs: MutableSet<FrozenHeapRef> = mutableSetOf(),
    /** String interner. */
    private val strInterner: FrozenStringValueInterner = FrozenStringValueInterner(),
) {
    override fun toString(): String {
        val bytes = arena.allocatedBytes()
        val refCount = refs.size
        return "FrozenHeap(bytes=$bytes, refs=$refCount)"
    }

    companion object {
        /** Create a new FrozenHeap. */
        fun new(): FrozenHeap = FrozenHeap()
    }

    /**
     * into_ref but also assign a name.
     *
     * See FrozenHeapRef.name for more details.
     */
    fun nameAndIntoRef(name: Any): FrozenHeapRef = intoRefImpl(name)

    /**
     * After all values have been allocated, convert the FrozenHeap into a
     * FrozenHeapRef which can be cloned, shared between threads,
     * and ensures the underlying values allocated on the FrozenHeap remain valid.
     */
    fun intoRef(): FrozenHeapRef = intoRefImpl(null)

    internal fun intoRefImpl(name: Any?): FrozenHeapRef {
        if (arena.isEmpty() && refs.isEmpty()) {
            return FrozenHeapRef()
        } else {
            return FrozenHeapRef(
                FrozenFrozenHeap(
                    arena = arena,
                    refs = refs.toList(),
                    name = name,
                ),
            )
        }
    }

    /**
     * Keep the argument FrozenHeapRef alive as long as this FrozenHeap
     * is kept alive. Used if a FrozenValue in this heap points at values in another
     * FrozenHeap.
     */
    fun addReference(heap: FrozenHeapRef) {
        if (heap.inner == null) {
            return
        }

        if (!refs.contains(heap)) {
            refs.add(heap.clone())
        }
    }

    internal fun stringInterner(): FrozenStringValueInterner = strInterner

    internal fun <T : AValue> allocRaw(x: AValueImpl<T>): FrozenValueTyped<StarlarkValue> {
        val v = arena.alloc(x)
        val frozenValue = FrozenValue.newPtr(v.header, v.header.vtable.isStr)
        return FrozenValueTyped.newUnchecked(frozenValue)
    }

    internal fun <T : AValue> allocRawExtra(x: AValueImpl<T>): Pair<FrozenValueTyped<StarlarkValue>, Any> {
        val v = arena.allocExtra(x)
        val frozenValue = FrozenValue.newPtr(v.header, v.header.vtable.isStr)
        val fv = FrozenValueTyped.newUnchecked<StarlarkValue>(frozenValue)
        return Pair(fv, Unit)
    }

    internal fun allocStrInit(
        len: Int,
        hash: StarlarkHashValue,
        init: (ByteArray) -> Unit,
    ): FrozenStringValue {
        val bytes = ByteArray(len)
        init(bytes)
        val header = arena.allocStr(bytes.decodeToString())
        val value = FrozenValue.newPtr(header, true)
        return FrozenStringValue.newUnchecked(value)
    }

    /** Allocate a new value on a FrozenHeap. */
    fun <T : AllocFrozenValue> alloc(v: T): FrozenValue = v.allocFrozenValue(this)

    /** Allocate a value and return FrozenValueOfUnchecked of it. */
    fun <T : AllocFrozenValue> allocTypedUnchecked(v: T): FrozenValueOfUnchecked<T> = FrozenValueOfUnchecked.new(v.allocFrozenValue(this))

    /** Allocate an interned string. Returns a FrozenStringValue. */
    fun allocStrIntern(s: String): FrozenStringValue {
        val constant = constantString(s)
        if (constant != null) {
            return constant
        }
        val hashed =
            io.github.kotlinmania.starlark.collections.Hashed
                .new(s)
        return stringInterner()
            .intern(hashed) {
                val bytes = s.encodeToByteArray()
                allocStrInit(bytes.size, hashed.hash) { dst ->
                    bytes.copyInto(dst)
                }
            }
    }

    /**
     * Number of bytes allocated on this heap, not including any memory
     * allocated outside of the starlark heap.
     */
    fun allocatedBytes(): Int = arena.allocatedBytes()

    /** Number of bytes allocated by the heap but not yet filled. */
    fun availableBytes(): Int = arena.availableBytes()

    /** Obtain a summary of how much memory is currently allocated by this heap. */
    fun allocatedSummary(): HeapSummary = arena.allocatedSummary()

    internal fun <T : AValue> reserveWithExtra(
        extraLen: Int,
    ): Triple<FrozenValue, Reservation<T>, Any> {
        val r = arena.reserveWithExtra<T>(extraLen)
        val fv = FrozenValue.newPtr(r.ptr(), false)
        return Triple(fv, r, Unit)
    }
}

typealias FrozenHeapName = Any

/**
 * FrozenHeap when it is no longer modified and can be shared between threads.
 * Although, arena is not safe to share between threads, but at least refs is.
 */
class FrozenFrozenHeap internal constructor(
    internal val arena: Arena,
    val refs: List<FrozenHeapRef>,
    val name: Any? = null,
) : AutoCloseable {
    private val refCount = AtomicInt(0)


    fun incRef() {
        refCount.fetchAndAdd(1)
    }

    fun decRef() {
        val prev = refCount.fetchAndAdd(-1)
        if (prev == 1) {
            arena.finish()
            for (ref in refs) {
                ref.close()
            }
        } else if (prev < 1) {
            refCount.store(0)
        }
    }

    override fun close() {
        decRef()
    }

    override fun toString(): String {
        val bytes = arena.allocatedBytes()
        val refsCount = refs.size
        val activeCount = refCount.load()
        return "FrozenHeap(bytes=$bytes, refs=$refsCount, activeRefs=$activeCount)"
    }
}

/**
 * A reference to a FrozenHeap that keeps alive all values on the underlying heap.
 * Note that the Hash is consistent for a single FrozenHeapRef, but non-deterministic
 * across executions and distinct but observably identical FrozenHeapRef values.
 */
class FrozenHeapRef(
    // The Eq/Hash are by identity rather than value, since we produce unique values
    // given an underlying FrozenHeap.
    internal val inner: FrozenFrozenHeap? = null,
) : AutoCloseable {
    private val closed = AtomicBoolean(false)

    init {
        inner?.incRef()
    }

    fun clone(): FrozenHeapRef {
        return FrozenHeapRef(inner)
    }

    fun incRef() {
        inner?.incRef()
    }

    fun decRef() {
        inner?.decRef()
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            inner?.close()
        }
    }
    /**
     * Number of bytes allocated on this heap, not including any memory
     * allocated outside of the starlark heap.
     */
    fun allocatedBytes(): Int = inner?.arena?.allocatedBytes() ?: 0

    /**
     * Number of bytes allocated by the heap but not filled.
     * Note that these bytes will never be filled as no further allocations can
     * be made on this heap (it has been sealed).
     */
    fun availableBytes(): Int = inner?.arena?.availableBytes() ?: 0

    /**
     * Obtain a summary of how much memory is currently allocated by this heap.
     * Doesn't include the heaps it keeps alive by reference.
     */
    fun allocatedSummary(): HeapSummary = inner?.arena?.allocatedSummary() ?: HeapSummary()

    /**
     * Get the name of this heap.
     *
     * Names can be assigned when finalizing frozen heaps; in practice, this is done when freezing
     * modules, see Module.freezeAndName.
     *
     * The name is intentionally made available here and not at a higher point like the module
     * level so that it can be inspected even when traversing the dependency graph of frozen heaps.
     */
    fun name(): Any? = inner?.name

    /** Get the frozen heaps that this frozen heap depends on. */
    fun refs(): List<FrozenHeapRef> = inner?.refs ?: emptyList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FrozenHeapRef) return false
        return inner === other.inner
    }

    override fun hashCode(): Int = inner?.hashCode() ?: 0
}

/** Used to perform garbage collection by Trace.trace. */
class Tracer internal constructor(
    internal val arena: Arena = Arena(),
    internal val maxOldIndex: Long,
) {
    /** Walk over a value during garbage collection. */
    fun trace(value: ValueHolder) {
        value.value = adjust(value.value)
    }

    /**
     * Helper function to annotate that this field has been considered for tracing,
     * but is not relevant because it has a static lifetime containing no relevant values.
     * Does nothing.
     */
    fun <T> traceStatic(
        @Suppress("UNUSED_PARAMETER") value: T,
    ) {
        // Nothing to do because T can't contain the relevant lifetime
    }

    internal fun <T : AValue> reserve(): Pair<Value, Reservation<T>> {
        val r = reserveWithExtra<T>(0)
        return Pair(r.first, r.second)
    }

    internal fun <T : AValue> reserveWithExtra(
        extraLen: Int,
    ): Triple<Value, Reservation<T>, Any> {
        val r = arena.reserveWithExtra<T>(extraLen)
        val v = Value.newPtr(r.ptr(), false)
        return Triple(v, r, Unit)
    }

    fun allocStr(x: String): Value {
        val v = arena.allocStr(x)
        return Value.newPtr(v, true)
    }

    private fun adjust(value: Value): Value {
        // Case 1, doesn't point at the old arena
        if (!value.ptr.isUnfrozen()) {
            return value
        }
        val ptrIndex = value.ptr.unpackPtrOpt() ?: return value
        if (ptrIndex >= maxOldIndex) {
            return value
        }

        // Case 2: We have already been replaced with a forwarding, or need to freeze
        val header = AValueHeader.fromIndex(ptrIndex)
        val aValueOrForward = AValueOrForward.Header(header)
        println("ADJUST: value=$value type=${value.getType()} ptrVal=${value.ptrValue()} index=${header.index} maxOldIndex=$maxOldIndex")
        return when (val unpacked = aValueOrForward.unpack()) {
            is AValueOrForwardUnpack.Forward -> {
                val dest = unpacked.forward.forwardPtr().unpackUnfrozenValue()
                println("ADJUST FORWARDED: old=$value to new=$dest")
                dest
            }
            is AValueOrForwardUnpack.Header -> {
                val dest = unpacked.header.heapCopy(this)
                println("ADJUST COPIED: old=$value to new=$dest")
                dest
            }
        }
    }
}

/** Mutable holder for Value, used during tracing. */
class ValueHolder(
    var value: Value,
)
