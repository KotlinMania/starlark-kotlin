// port-lint: source src/values/layout/heap.rs
package io.github.kotlinmania.starlark_kotlin.values.layout

import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue

/**
 * Heap kind - distinguishes between frozen and unfrozen heaps.
 */
enum class HeapKind {
    /** Unfrozen heap - values can be modified. */
    Unfrozen,
    /** Frozen heap - values are immutable. */
    Frozen
}

/**
 * A heap on which [Value]s can be allocated.
 *
 * In Kotlin, we don't need explicit lifetime management like Rust, as the garbage
 * collector handles memory. However, we still maintain the concept of heap allocation
 * to match Starlark semantics and support freezing.
 *
 * # Examples
 *
 * ```kotlin
 * val result = Heap.temp { heap ->
 *     val value = heap.allocStr("Hello, World!")
 *     value.toString()
 * }
 * ```
 */
class Heap internal constructor() {
    /** Peak memory allocated (for profiling). */
    private var peakAllocated: Long = 0

    /** Current allocated memory estimate. */
    private var currentAllocated: Long = 0

    /** String interning cache for efficient string allocation. */
    private val stringInterner: MutableMap<String, Value> = mutableMapOf()

    /** References to frozen heaps that this heap depends on. */
    private val refs: MutableSet<FrozenHeapRef> = mutableSetOf()

    /** Whether garbage collection is banned (for safety during certain operations). */
    private var banGc: Boolean = true

    /** Arena for allocating values - in Kotlin this is just a list. */
    private val arena: MutableList<Any> = mutableListOf()

    /**
     * Get the total number of bytes allocated (estimated).
     */
    fun allocatedBytes(): Long = currentAllocated

    /**
     * Get the peak bytes ever allocated on this heap.
     */
    fun peakAllocatedBytes(): Long = peakAllocated

    /**
     * Allocate a value on the heap.
     */
    fun <T : StarlarkValue> alloc(value: T): Value where T : AllocValue {
        val heapValue = HeapValue(value, value.getVTable())
        arena.add(value)
        currentAllocated += estimateSize(value)
        updatePeak()
        return heapValue
    }

    /**
     * Allocate a string on the heap, with interning.
     */
    fun allocStr(s: String): Value {
        // Check if we already have this string interned
        return stringInterner.getOrPut(s) {
            val value = StringValue(s)
            arena.add(s)
            currentAllocated += s.length.toLong() * 2 // rough estimate for UTF-16
            updatePeak()
            value
        }
    }

    /**
     * Allocate an integer on the heap.
     */
    fun allocInt(i: Int): Value {
        val value = IntValue(i)
        arena.add(i)
        currentAllocated += 4 // sizeof(int)
        updatePeak()
        return value
    }

    /**
     * Allocate a list on the heap.
     */
    fun allocList(elements: List<Value>): Value {
        val tuple = TupleValue(elements)
        arena.add(tuple)
        currentAllocated += elements.size.toLong() * 8 // rough pointer size estimate
        updatePeak()
        return tuple
    }

    /**
     * Allocate a tuple on the heap.
     */
    fun allocTuple(elements: List<Value>): Value {
        return allocList(elements) // Tuples and lists have similar allocation in Kotlin
    }

    /**
     * Add a reference to a frozen heap.
     */
    fun addReference(heapRef: FrozenHeapRef) {
        refs.add(heapRef)
    }

    /**
     * Get all referenced frozen heaps.
     */
    fun referencedHeaps(): List<FrozenHeapRef> = refs.toList()

    /**
     * Record a call enter event (for profiling/debugging).
     */
    fun recordCallEnter(function: Value) {
        // Implementation for call profiling
    }

    /**
     * Record a call exit event (for profiling/debugging).
     */
    fun recordCallExit() {
        // Implementation for call profiling
    }

    /**
     * Check if garbage collection is currently banned.
     */
    fun isGcBanned(): Boolean = banGc

    /**
     * Set whether garbage collection is banned.
     */
    fun setBanGc(ban: Boolean) {
        banGc = ban
    }

    /**
     * Perform garbage collection (in Kotlin, this just clears references and lets GC handle it).
     */
    fun collectGarbage() {
        if (banGc) return
        // In Kotlin, we don't need explicit GC - the JVM/Native runtime handles it
        // This method exists for API compatibility
    }

    /**
     * Get heap statistics for profiling.
     */
    fun getStatistics(): HeapStatistics {
        return HeapStatistics(
            allocated = currentAllocated,
            peak = peakAllocated,
            objectCount = arena.size
        )
    }

    private fun estimateSize(value: Any): Long {
        return when (value) {
            is String -> value.length.toLong() * 2
            is List<*> -> value.size.toLong() * 8
            is Map<*, *> -> value.size.toLong() * 16
            else -> 24 // default object overhead estimate
        }
    }

    private fun updatePeak() {
        if (currentAllocated > peakAllocated) {
            peakAllocated = currentAllocated
        }
    }

    companion object {
        /**
         * Create a heap and use it within the closure.
         *
         * The heap is discarded at the end of the closure.
         */
        fun <R> temp(block: (Heap) -> R): R {
            val heap = Heap()
            heap.setBanGc(false)
            return block(heap)
        }

        /**
         * Create a heap and use it within a suspending function.
         */
        suspend fun <R> tempAsync(block: suspend (Heap) -> R): R {
            val heap = Heap()
            heap.setBanGc(false)
            return block(heap)
        }
    }
}

/**
 * A frozen heap containing immutable values.
 *
 * Frozen heaps are cheaper to use than regular heaps as they don't require
 * garbage collection tracking. Values on a frozen heap cannot be modified.
 */
class FrozenHeap internal constructor() {
    /** Immutable arena of frozen values. */
    private val arena: MutableList<Any> = mutableListOf()

    /** String interning for frozen strings. */
    private val stringInterner: MutableMap<String, FrozenValue> = mutableMapOf()

    /** Total allocated bytes. */
    private var allocated: Long = 0

    /**
     * Get the total number of bytes allocated.
     */
    fun allocatedBytes(): Long = allocated

    /**
     * Allocate a frozen value on this heap.
     */
    fun <T : StarlarkValue> allocFrozen(value: T): FrozenValue where T : AllocFrozenValue {
        arena.add(value)
        allocated += estimateSize(value)
        return when (value) {
            is NoneValue -> FrozenNoneValue
            is BoolValue -> FrozenBoolValue(value.value)
            is IntValue -> FrozenIntValue(value.value)
            is StringValue -> FrozenStringValue(value.value)
            else -> error("Unsupported frozen value type: ${value::class}")
        }
    }

    /**
     * Allocate a frozen string.
     */
    fun allocFrozenStr(s: String): FrozenValue {
        return stringInterner.getOrPut(s) {
            arena.add(s)
            allocated += s.length.toLong() * 2
            FrozenStringValue(s)
        }
    }

    /**
     * Create a reference to this frozen heap.
     */
    fun createRef(): FrozenHeapRef {
        return FrozenHeapRef(this)
    }

    private fun estimateSize(value: Any): Long {
        return when (value) {
            is String -> value.length.toLong() * 2
            is List<*> -> value.size.toLong() * 8
            is Map<*, *> -> value.size.toLong() * 16
            else -> 24
        }
    }

    companion object {
        /**
         * Create a new frozen heap.
         */
        fun new(): FrozenHeap = FrozenHeap()
    }
}

/**
 * Statistics about heap memory usage.
 */
data class HeapStatistics(
    /** Currently allocated bytes. */
    val allocated: Long,
    /** Peak allocated bytes. */
    val peak: Long,
    /** Number of objects allocated. */
    val objectCount: Int
)

/**
 * A tracer for garbage collection marking.
 */
class Tracer(private val heap: Heap) {
    private val marked: MutableSet<Any> = mutableSetOf()

    /**
     * Mark a value as reachable.
     */
    fun trace(value: Value) {
        when (value) {
            is TupleValue -> {
                if (marked.add(value)) {
                    value.elements.forEach { trace(it) }
                }
            }
            is HeapValue -> {
                marked.add(value.pointer)
            }
            else -> {
                // Primitive values don't need marking
            }
        }
    }

    /**
     * Get all marked objects.
     */
    fun getMarked(): Set<Any> = marked
}

