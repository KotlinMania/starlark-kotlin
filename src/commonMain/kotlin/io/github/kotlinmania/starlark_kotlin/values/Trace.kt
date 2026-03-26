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

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T>
/** Trace impl for [MutableList] (Vec). */
fun <T : Trace> traceList(list: MutableList<T>, tracer: Tracer) {
    list.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]
/** Trace impl for [Array] (slice). */
fun <T : Trace> traceArray(array: Array<T>, tracer: Tracer) {
    array.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T>
/** Trace impl for HashTable. */
fun <T : Trace> traceHashTable(table: MutableCollection<T>, tracer: Tracer) {
    table.forEach { e -> e.trace(tracer) }
}

// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>
/** Trace impl for [SmallMap]. */
fun <K : Trace, V : Trace> traceSmallMap(map: SmallMap<K, V>, tracer: Tracer) {
    for ((k, v) in map) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

/** Trace impl for [MutableMap]. */
fun <K : Trace, V : Trace> traceMap(map: MutableMap<K, V>, tracer: Tracer) {
    for ((k, v) in map) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
/** Trace impl for [SmallSet]. */
fun <T : Trace> traceSmallSet(set: SmallSet<T>, tracer: Tracer) {
    for (v in set) {
        v.trace(tracer)
    }
}

/** Trace impl for [MutableSet]. */
fun <T : Trace> traceSet(set: MutableSet<T>, tracer: Tracer) {
    for (v in set) {
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
/** Trace impl for [Hashed]. */
fun <T : Trace> traceHashed(hashed: Hashed<T>, tracer: Tracer) {
    hashed.key().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
/** Trace impl for nullable (Option). */
fun <T : Trace> traceNullable(value: T?, tracer: Tracer) {
    if (value != null) {
        value.trace(tracer)
    }
}

/**
 * Mutable holder interface, corresponding to Rust's RefCell/Cell/UnsafeCell.
 */
interface MutableHolder<T> {
    /** Get a mutable reference to the inner value. */
    fun getMut(): T
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T>
/** Trace impl for RefCell (MutableHolder). */
fun <T : Trace> traceRefCell(holder: MutableHolder<T>, tracer: Tracer) {
    holder.getMut().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>
/** Trace impl for Cell (MutableHolder). */
fun <T : Trace> traceCell(holder: MutableHolder<T>, tracer: Tracer) {
    holder.getMut().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>
/** Trace impl for OnceCell (nullable). */
fun <T : Trace> traceOnceCell(value: T?, tracer: Tracer) {
    if (value != null) {
        value.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>
/** Trace impl for UnsafeCell (MutableHolder). */
fun <T : Trace> traceUnsafeCell(holder: MutableHolder<T>, tracer: Tracer) {
    holder.getMut().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
/** Trace impl for Box (heap-allocated). */
fun <T : Trace> traceBoxed(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v> Trace<'v> for ()
/** Trace impl for Unit (no-op). */
fun traceUnit(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {}

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
/** Trace impl for single-element tuple. */
fun <T1 : Trace> traceTuple1(t1: T1, tracer: Tracer) {
    t1.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)
/** Trace impl for two-element tuple. */
fun <T1 : Trace, T2 : Trace> traceTuple2(t1: T1, t2: T2, tracer: Tracer) {
    t1.trace(tracer)
    t2.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)
/** Trace impl for three-element tuple. */
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

// unsafe impl<'v, T1..T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4)
/** Trace impl for four-element tuple. */
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

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2>
/** Trace impl for [Either]. */
fun <T1 : Trace, T2 : Trace> traceEither(either: Either<T1, T2>, tracer: Tracer) {
    when (either) {
        is Either.Left -> either.value.trace(tracer)
        is Either.Right -> either.value.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v>
/** Trace impl for [Value]. */
fun traceValue(value: Value, tracer: Tracer) {
    tracer.trace(value)
}

// unsafe impl<'v> Trace<'v> for FrozenValue
/** Trace impl for [FrozenValue] (no-op). */
fun traceFrozenValue(
    @Suppress("UNUSED_PARAMETER") value: FrozenValue,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for String
/** Trace impl for [String] (no-op). */
fun traceString(
    @Suppress("UNUSED_PARAMETER") value: String,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for usize
/** Trace impl for usize (no-op). */
fun traceUsize(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for i32
/** Trace impl for i32 (no-op). */
fun traceI32(
    @Suppress("UNUSED_PARAMETER") value: Int,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for u32
/** Trace impl for u32 (no-op). */
fun traceU32(
    @Suppress("UNUSED_PARAMETER") value: UInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for u64
/** Trace impl for u64 (no-op). */
fun traceU64(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for bool
/** Trace impl for bool (no-op). */
fun traceBool(
    @Suppress("UNUSED_PARAMETER") value: Boolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicBool
/** Trace impl for [AtomicBoolean] (no-op). */
fun traceAtomicBoolean(
    @Suppress("UNUSED_PARAMETER") value: AtomicBoolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicI8
/** Trace impl for AtomicI8 (no-op). */
fun traceAtomicI8(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicU8
/** Trace impl for AtomicU8 (no-op). */
fun traceAtomicU8(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicI16
/** Trace impl for AtomicI16 (no-op). */
fun traceAtomicI16(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicU16
/** Trace impl for AtomicU16 (no-op). */
fun traceAtomicU16(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicI32
/** Trace impl for [AtomicInt] (AtomicI32, no-op). */
fun traceAtomicInt(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicU32
/** Trace impl for AtomicU32 (no-op). */
fun traceAtomicU32(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicI64
/** Trace impl for [AtomicLong] (AtomicI64, no-op). */
fun traceAtomicLong(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicU64
/** Trace impl for AtomicU64 (no-op). */
fun traceAtomicU64(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicUsize
/** Trace impl for AtomicUsize (no-op). */
fun traceAtomicUsize(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for AtomicIsize
/** Trace impl for AtomicIsize (no-op). */
fun traceAtomicIsize(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v> Trace<'v> for std::time::Instant
/** Trace impl for [Instant] (no-op). */
fun traceInstant(
    @Suppress("UNUSED_PARAMETER") value: Instant,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
/** Trace impl for PhantomData (no-op). */
fun <T> tracePhantomData(
    @Suppress("UNUSED_PARAMETER") value: T?,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
/** Trace impl for Arc Mutex (synchronized mutable reference). */
fun <T : Trace> traceMutex(holder: MutableHolder<T>, tracer: Tracer) {
    holder.getMut().trace(tracer)
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
/** Trace impl for fn(A) -> R (no-op). */
fun <A, R> traceFn1(
    @Suppress("UNUSED_PARAMETER") fn: (A) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
/** Trace impl for fn(A, B) -> R (no-op). */
fun <A, B, R> traceFn2(
    @Suppress("UNUSED_PARAMETER") fn: (A, B) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
/** Trace impl for fn(A, B, C) -> R (no-op). */
fun <A, B, C, R> traceFn3(
    @Suppress("UNUSED_PARAMETER") fn: (A, B, C) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}
