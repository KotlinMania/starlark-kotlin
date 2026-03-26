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
object TraceVec {
    fun <T : Trace> trace(self: MutableList<T>, tracer: Tracer) {
        self.forEach { x -> x.trace(tracer) }
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]
object TraceSlice {
    fun <T : Trace> trace(self: Array<T>, tracer: Tracer) {
        self.forEach { x -> x.trace(tracer) }
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T>
object TraceHashTable {
    fun <T : Trace> trace(self: MutableCollection<T>, tracer: Tracer) {
        self.forEach { e -> e.trace(tracer) }
    }
}

// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>
object TraceSmallMap {
    fun <K : Trace, V : Trace> trace(self: SmallMap<K, V>, tracer: Tracer) {
        for ((k, v) in self) {
            k.trace(tracer)
            v.trace(tracer)
        }
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
object TraceSmallSet {
    fun <T : Trace> trace(self: SmallSet<T>, tracer: Tracer) {
        for (v in self) {
            v.trace(tracer)
        }
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
object TraceHashed {
    fun <T : Trace> trace(self: Hashed<T>, tracer: Tracer) {
        self.key().trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
object TraceOption {
    fun <T : Trace> trace(self: T?, tracer: Tracer) {
        if (self != null) {
            self.trace(tracer)
        }
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
object TraceRefCell {
    fun <T : Trace> trace(self: MutableHolder<T>, tracer: Tracer) {
        self.getMut().trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>
object TraceCell {
    fun <T : Trace> trace(self: MutableHolder<T>, tracer: Tracer) {
        self.getMut().trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>
object TraceOnceCell {
    fun <T : Trace> trace(self: T?, tracer: Tracer) {
        if (self != null) {
            self.trace(tracer)
        }
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>
object TraceUnsafeCell {
    fun <T : Trace> trace(self: MutableHolder<T>, tracer: Tracer) {
        self.getMut().trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
object TraceBox {
    fun <T : Trace> trace(self: T, tracer: Tracer) {
        self.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for ()
object TraceUnit {
    fun trace(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {}
}

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
object TraceTuple1 {
    fun <T1 : Trace> trace(self: T1, tracer: Tracer) {
        self.trace(tracer)
    }
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)
object TraceTuple2 {
    fun <T1 : Trace, T2 : Trace> trace(first: T1, second: T2, tracer: Tracer) {
        first.trace(tracer)
        second.trace(tracer)
    }
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)
object TraceTuple3 {
    fun <T1 : Trace, T2 : Trace, T3 : Trace> trace(
        first: T1,
        second: T2,
        third: T3,
        tracer: Tracer,
    ) {
        first.trace(tracer)
        second.trace(tracer)
        third.trace(tracer)
    }
}

// unsafe impl<'v, T1..T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4)
object TraceTuple4 {
    fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> trace(
        first: T1,
        second: T2,
        third: T3,
        fourth: T4,
        tracer: Tracer,
    ) {
        first.trace(tracer)
        second.trace(tracer)
        third.trace(tracer)
        fourth.trace(tracer)
    }
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2>
object TraceEither {
    fun <T1 : Trace, T2 : Trace> trace(self: Either<T1, T2>, tracer: Tracer) {
        when (self) {
            is Either.Left -> self.value.trace(tracer)
            is Either.Right -> self.value.trace(tracer)
        }
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v>
object TraceValue {
    fun trace(self: Value, tracer: Tracer) {
        tracer.trace(self)
    }
}

// unsafe impl<'v> Trace<'v> for FrozenValue
object TraceFrozenValue {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: FrozenValue,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for String
object TraceString {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: String,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for usize
object TraceUsize {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: ULong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for i32
object TraceI32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: Int,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for u32
object TraceU32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: UInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for u64
object TraceU64 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: ULong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for bool
object TraceBool {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: Boolean,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicBool
object TraceAtomicBool {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicBoolean,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicI8
object TraceAtomicI8 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicU8
object TraceAtomicU8 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicI16
object TraceAtomicI16 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicU16
object TraceAtomicU16 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicI32
object TraceAtomicI32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicU32
object TraceAtomicU32 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicInt,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicI64
object TraceAtomicI64 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicLong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicU64
object TraceAtomicU64 {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicLong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicUsize
object TraceAtomicUsize {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicLong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for AtomicIsize
object TraceAtomicIsize {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: AtomicLong,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v> Trace<'v> for std::time::Instant
object TraceInstant {
    fun trace(
        @Suppress("UNUSED_PARAMETER") self: Instant,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
object TracePhantomData {
    fun <T> trace(
        @Suppress("UNUSED_PARAMETER") self: T?,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
object TraceArcMutex {
    fun <T : Trace> trace(self: MutableHolder<T>, tracer: Tracer) {
        self.getMut().trace(tracer)
    }
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
object TraceFn1 {
    fun <A, R> trace(
        @Suppress("UNUSED_PARAMETER") self: (A) -> R,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
object TraceFn2 {
    fun <A, B, R> trace(
        @Suppress("UNUSED_PARAMETER") self: (A, B) -> R,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
object TraceFn3 {
    fun <A, B, C, R> trace(
        @Suppress("UNUSED_PARAMETER") self: (A, B, C) -> R,
        @Suppress("UNUSED_PARAMETER") tracer: Tracer,
    ) {}
}

// Convenience aliases for backward compatibility with callers

/** Trace each element in a [MutableList]. */
fun <T : Trace> traceList(list: MutableList<T>, tracer: Tracer) = TraceVec.trace(list, tracer)

/** Trace each element in an [Array]. */
fun <T : Trace> traceArray(array: Array<T>, tracer: Tracer) = TraceSlice.trace(array, tracer)

/** Trace each key and value in a [MutableMap]. */
fun <K : Trace, V : Trace> traceMap(map: MutableMap<K, V>, tracer: Tracer) {
    for ((k, v) in map) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

/** Trace each element in a [MutableSet]. */
fun <T : Trace> traceSet(set: MutableSet<T>, tracer: Tracer) {
    for (v in set) {
        v.trace(tracer)
    }
}

/** Trace the key within a [Hashed] wrapper. */
fun <T : Trace> traceHashed(hashed: Hashed<T>, tracer: Tracer) = TraceHashed.trace(hashed, tracer)

/** Trace a nullable [Trace] value. */
fun <T : Trace> traceNullable(value: T?, tracer: Tracer) = TraceOption.trace(value, tracer)

/** Trace a boxed [Trace] value. */
fun <T : Trace> traceBoxed(value: T, tracer: Tracer) = TraceBox.trace(value, tracer)

/** No-op trace for [Unit]. */
fun traceUnit(@Suppress("UNUSED_PARAMETER") tracer: Tracer) {}

/** Trace a single-element tuple. */
fun <T1 : Trace> traceTuple1(t1: T1, tracer: Tracer) {
    t1.trace(tracer)
}

/** Trace a two-element tuple. */
fun <T1 : Trace, T2 : Trace> traceTuple2(t1: T1, t2: T2, tracer: Tracer) {
    t1.trace(tracer)
    t2.trace(tracer)
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

/** Trace an [Either] value. */
fun <T1 : Trace, T2 : Trace> traceEither(either: Either<T1, T2>, tracer: Tracer) =
    TraceEither.trace(either, tracer)

/** Trace a [Value]. */
fun traceValue(value: Value, tracer: Tracer) = TraceValue.trace(value, tracer)

/** No-op trace for [FrozenValue]. */
fun traceFrozenValue(
    @Suppress("UNUSED_PARAMETER") value: FrozenValue,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for [String]. */
fun traceString(
    @Suppress("UNUSED_PARAMETER") value: String,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for usize. */
fun traceUsize(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for i32. */
fun traceI32(
    @Suppress("UNUSED_PARAMETER") value: Int,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for u32. */
fun traceU32(
    @Suppress("UNUSED_PARAMETER") value: UInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for u64. */
fun traceU64(
    @Suppress("UNUSED_PARAMETER") value: ULong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for bool. */
fun traceBool(
    @Suppress("UNUSED_PARAMETER") value: Boolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for [AtomicBoolean]. */
fun traceAtomicBoolean(
    @Suppress("UNUSED_PARAMETER") value: AtomicBoolean,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for [AtomicInt]. */
fun traceAtomicInt(
    @Suppress("UNUSED_PARAMETER") value: AtomicInt,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for [AtomicLong]. */
fun traceAtomicLong(
    @Suppress("UNUSED_PARAMETER") value: AtomicLong,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for [Instant]. */
fun traceInstant(
    @Suppress("UNUSED_PARAMETER") value: Instant,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for phantom data. */
fun <T> tracePhantomData(
    @Suppress("UNUSED_PARAMETER") value: T?,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for function pointers. */
fun <A, R> traceFn1(
    @Suppress("UNUSED_PARAMETER") fn: (A) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for function pointers. */
fun <A, B, R> traceFn2(
    @Suppress("UNUSED_PARAMETER") fn: (A, B) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}

/** No-op trace for function pointers. */
fun <A, B, C, R> traceFn3(
    @Suppress("UNUSED_PARAMETER") fn: (A, B, C) -> R,
    @Suppress("UNUSED_PARAMETER") tracer: Tracer,
) {}
