// port-lint: source src/values/trace.rs
package io.github.kotlinmania.starlark_kotlin.values

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlinx.datetime.Instant

/**
 * Called by the garbage collection, and must walk over every contained [Value] in the type.
 *
 * For the most cases [Trace] is enough to implement this interface:
 *
 * ```
 * class MySet(
 *     val keys: MutableList<Value>,
 * ) : Trace {
 *     override fun trace(tracer: Tracer) {
 *         traceList(keys, tracer)
 *     }
 * }
 * ```
 */
interface Trace {
    /**
     * Recursively "trace" the value.
     *
     * Note during trace, [Value] objects in `this` might be already special forward-objects,
     * trying to unpack these values may crash the process.
     *
     * Generally this function should not do anything except calling [trace] on the fields.
     */
    fun trace(tracer: Tracer)
}

/** Trace impl for Vec (MutableList). */
object TraceList {
    fun <T : Trace> trace(list: MutableList<T>, tracer: Tracer) {
        list.forEach { x -> x.trace(tracer) }
    }
}

/** Trace each element in the list. */
fun <T : Trace> traceList(list: MutableList<T>, tracer: Tracer) {
    list.forEach { x -> x.trace(tracer) }
}

/** Trace impl for slices (Array). */
object TraceSlice {
    fun <T : Trace> trace(array: Array<T>, tracer: Tracer) {
        array.forEach { x -> x.trace(tracer) }
    }
}

/** Trace each element in the array. */
fun <T : Trace> traceArray(array: Array<T>, tracer: Tracer) {
    array.forEach { x -> x.trace(tracer) }
}

/** Trace impl for HashTable. */
object TraceHashTable {
    fun <T : Trace> trace(table: MutableCollection<T>, tracer: Tracer) {
        table.forEach { e -> e.trace(tracer) }
    }
}

/** Trace impl for SmallMap. */
object TraceSmallMap {
    fun <K : Trace, V : Trace> trace(map: SmallMap<K, V>, tracer: Tracer) {
        for ((k, v) in map) {
            k.trace(tracer)
            v.trace(tracer)
        }
    }
}

/** Trace each key and value in a map. */
fun <K : Trace, V : Trace> traceMap(map: MutableMap<K, V>, tracer: Tracer) {
    for ((k, v) in map) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

/** Trace impl for SmallSet. */
object TraceSmallSet {
    fun <T : Trace> trace(set: SmallSet<T>, tracer: Tracer) {
        for (v in set) {
            v.trace(tracer)
        }
    }
}

/** Trace each element in a set. */
fun <T : Trace> traceSet(set: MutableSet<T>, tracer: Tracer) {
    for (v in set) {
        v.trace(tracer)
    }
}

/** Trace impl for Hashed. */
object TraceHashed {
    fun <T : Trace> trace(hashed: Hashed<T>, tracer: Tracer) {
        hashed.key().trace(tracer)
    }
}

/** Trace the key within a [Hashed] wrapper. */
fun <T : Trace> traceHashed(hashed: Hashed<T>, tracer: Tracer) {
    hashed.key().trace(tracer)
}

/** Trace impl for Option (nullable). */
object TraceOption {
    fun <T : Trace> trace(value: T?, tracer: Tracer) {
        if (value != null) {
            value.trace(tracer)
        }
    }
}

/** Trace a nullable value. */
fun <T : Trace> traceNullable(value: T?, tracer: Tracer) {
    if (value != null) {
        value.trace(tracer)
    }
}

/**
 * Mutable holder interface, corresponding to Rust's RefCell/Cell/UnsafeCell.
 */
interface MutableHolder<T> {
    fun getMut(): T
}

/** Trace impl for RefCell. */
object TraceRefCell {
    fun <T : Trace> trace(holder: MutableHolder<T>, tracer: Tracer) {
        holder.getMut().trace(tracer)
    }
}

/** Trace impl for Cell. */
object TraceCell {
    fun <T : Trace> trace(holder: MutableHolder<T>, tracer: Tracer) {
        holder.getMut().trace(tracer)
    }
}

/** Trace impl for OnceCell. */
object TraceOnceCell {
    fun <T : Trace> trace(value: T?, tracer: Tracer) {
        if (value != null) {
            value.trace(tracer)
        }
    }
}

/** Trace impl for UnsafeCell. */
object TraceUnsafeCell {
    fun <T : Trace> trace(holder: MutableHolder<T>, tracer: Tracer) {
        holder.getMut().trace(tracer)
    }
}

/** Trace impl for Box. */
object TraceBox {
    fun <T : Trace> trace(value: T, tracer: Tracer) {
        value.trace(tracer)
    }
}

/** Trace a boxed value. */
fun <T : Trace> traceBoxed(value: T, tracer: Tracer) {
    value.trace(tracer)
}

/** Trace impl for Unit (no-op). */
object TraceUnit {
    fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {}
}

/** No-op trace for Unit. */
fun traceUnit(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {}

/** Trace impl for single-element tuple. */
object TraceTuple1 {
    fun <T1 : Trace> trace(t1: T1, tracer: Tracer) {
        t1.trace(tracer)
    }
}

/** Trace a single-element tuple. */
fun <T1 : Trace> traceTuple1(t1: T1, tracer: Tracer) {
    t1.trace(tracer)
}

/** Trace impl for two-element tuple. */
object TraceTuple2 {
    fun <T1 : Trace, T2 : Trace> trace(t1: T1, t2: T2, tracer: Tracer) {
        t1.trace(tracer)
        t2.trace(tracer)
    }
}

/** Trace a two-element tuple. */
fun <T1 : Trace, T2 : Trace> traceTuple2(t1: T1, t2: T2, tracer: Tracer) {
    t1.trace(tracer)
    t2.trace(tracer)
}

/** Trace impl for three-element tuple. */
object TraceTuple3 {
    fun <T1 : Trace, T2 : Trace, T3 : Trace> trace(
        t1: T1,
        t2: T2,
        t3: T3,
        tracer: Tracer,
    ) {
        t1.trace(tracer)
        t2.trace(tracer)
        t3.trace(tracer)
    }
}

/** Trace a three-element tuple. */
fun <T1 : Trace, T2 : Trace, T3 : Trace> traceTuple3(
    t1: T1,
    t2: T2,
    t3: T3,
    tracer: Tracer,
) {
    t1.trace(tracer)
    t2.trace(tracer)
    t3.trace(tracer)
}

/** Trace impl for four-element tuple. */
object TraceTuple4 {
    fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> trace(
        t1: T1,
        t2: T2,
        t3: T3,
        t4: T4,
        tracer: Tracer,
    ) {
        t1.trace(tracer)
        t2.trace(tracer)
        t3.trace(tracer)
        t4.trace(tracer)
    }
}

/** Trace a four-element tuple. */
fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> traceTuple4(
    t1: T1,
    t2: T2,
    t3: T3,
    t4: T4,
    tracer: Tracer,
) {
    t1.trace(tracer)
    t2.trace(tracer)
    t3.trace(tracer)
    t4.trace(tracer)
}

/** Trace impl for Either. */
object TraceEither {
    fun <T1 : Trace, T2 : Trace> trace(either: Either<T1, T2>, tracer: Tracer) {
        when (either) {
            is Either.Left -> either.value.trace(tracer)
            is Either.Right -> either.value.trace(tracer)
        }
    }
}

/** Trace an [Either] value. */
fun <T1 : Trace, T2 : Trace> traceEither(either: Either<T1, T2>, tracer: Tracer) {
    when (either) {
        is Either.Left -> either.value.trace(tracer)
        is Either.Right -> either.value.trace(tracer)
    }
}

/** Trace impl for Value. */
object TraceValue {
    fun trace(value: Value, tracer: Tracer) {
        tracer.trace(value)
    }
}

/** Trace a [Value]. */
fun traceValue(value: Value, tracer: Tracer) {
    tracer.trace(value)
}

/** Trace impl for FrozenValue (no-op). */
object TraceFrozenValue {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: FrozenValue,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for [FrozenValue]. */
fun traceFrozenValue(
    @Suppress("UNUSED_PARAMETER") value: FrozenValue,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for String (no-op). */
object TraceString {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: String,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for [String]. */
fun traceString(
    @Suppress("UNUSED_PARAMETER") value: String,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for usize (no-op). */
object TraceUsize {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: ULong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for usize. */
fun traceUsize(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for i32 (no-op). */
object TraceI32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: Int,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for i32. */
fun traceI32(
    @Suppress("UNUSED_PARAMETER") value: Int,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for u32 (no-op). */
object TraceU32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: UInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for u32. */
fun traceU32(
    @Suppress("UNUSED_PARAMETER") value: UInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for u64 (no-op). */
object TraceU64 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: ULong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for u64. */
fun traceU64(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for bool (no-op). */
object TraceBool {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: Boolean,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for bool. */
fun traceBool(
    @Suppress("UNUSED_PARAMETER") value: Boolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for AtomicBool (no-op). */
object TraceAtomicBool {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: AtomicBoolean,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for AtomicBool. */
fun traceAtomicBoolean(
    @Suppress("UNUSED_PARAMETER") value: AtomicBoolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for AtomicI8/AtomicU8/AtomicI16/AtomicU16/AtomicI32/AtomicU32 (no-op). */
object TraceAtomicI32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for atomic integers. */
fun traceAtomicInt(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for AtomicI64/AtomicU64/AtomicIsize/AtomicUsize (no-op). */
object TraceAtomicI64 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: AtomicLong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for atomic longs. */
fun traceAtomicLong(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for Instant (no-op). */
object TraceInstant {
    fun trace(
        @Suppress("UNUSED_PARAMETER") value: Instant,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for Instant. */
fun traceInstant(
    @Suppress("UNUSED_PARAMETER") value: Instant,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for PhantomData (no-op). */
object TracePhantomData {
    fun <T> trace(
        @Suppress("UNUSED_PARAMETER") value: T?,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for phantom data (type markers). */
fun <T> tracePhantomData(
    @Suppress("UNUSED_PARAMETER") value: T?,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for Arc Mutex (synchronized mutable reference). */
object TraceMutex {
    fun <T : Trace> trace(holder: MutableHolder<T>, tracer: Tracer) {
        holder.getMut().trace(tracer)
    }
}

/** Trace impl for fn(A) -> R (no-op). */
object TraceFn1 {
    fun <A, R> trace(
        @Suppress("UNUSED_PARAMETER") fn: (A) -> R,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for single-argument function pointers. */
fun <A, R> traceFn1(
    @Suppress("UNUSED_PARAMETER") fn: (A) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for fn(A, B) -> R (no-op). */
object TraceFn2 {
    fun <A, B, R> trace(
        @Suppress("UNUSED_PARAMETER") fn: (A, B) -> R,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for two-argument function pointers. */
fun <A, B, R> traceFn2(
    @Suppress("UNUSED_PARAMETER") fn: (A, B) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** Trace impl for fn(A, B, C) -> R (no-op). */
object TraceFn3 {
    fun <A, B, C, R> trace(
        @Suppress("UNUSED_PARAMETER") fn: (A, B, C) -> R,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

/** No-op trace for three-argument function pointers. */
fun <A, B, C, R> traceFn3(
    @Suppress("UNUSED_PARAMETER") fn: (A, B, C) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}
