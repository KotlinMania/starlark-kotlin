// port-lint: source src/values/trace.rs
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

package io.github.kotlinmania.starlark_kotlin.values

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Tracer
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlinx.datetime.Instant

/**
 * Called by the garbage collection, and must walk over every contained [Value] in the type.
 * Marked as requiring careful implementation because if you miss a nested [Value], it will
 * likely cause memory corruption.
 *
 * For the most cases [Trace] is enough to implement this interface:
 *
 * ```kotlin
 * class MySet(val keys: MutableList<Value>) : Trace {
 *     override fun trace(tracer: Tracer) {
 *         keys.forEach { it.trace(tracer) }
 *     }
 * }
 * ```
 */
// pub unsafe trait Trace<'v>
interface Trace {
    /**
     * Recursively "trace" the value.
     *
     * Note during trace, [Value] objects in `this` might be already special forward-objects,
     * trying to unpack these values may crash the process.
     *
     * Generally this function should not do anything except calling [trace] on the fields.
     */
    // fn trace(&mut self, tracer: &Tracer<'v>)
    fun trace(tracer: Tracer)
}

/**
 * Mutable holder interface, corresponding to Rust's `RefCell<T>`, `Cell<T>`, and `UnsafeCell<T>`.
 *
 * Enables safe mutable access during tracing.
 */
// RefCell<T>, Cell<T>, UnsafeCell<T> all expose get_mut() for tracing
interface MutableHolder<T> {
    /** Get a mutable reference to the inner value. */
    fun getMut(): T
}

/**
 * Either a left or right value, corresponding to Rust's `either::Either<L, R>`.
 */
// either::Either<L, R>
sealed class Either<out L, out R>

/** Left variant of [Either]. */
data class EitherLeft<out L>(val value: L) : Either<L, Nothing>()

/** Right variant of [Either]. */
data class EitherRight<out R>(val value: R) : Either<Nothing, R>()

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T>`.
 * The trace implementation is provided by [traceList].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T>
private class ImplTraceForVec

/** Trace a [MutableList] by tracing each element. */
fun <T : Trace> traceList(self: MutableList<T>, tracer: Tracer) =
    self.forEach { x -> x.trace(tracer) }

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]`.
 * The trace implementation is provided by [traceSlice].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]
private class ImplTraceForSlice

/** Trace an [Array] by tracing each element. */
fun <T : Trace> traceSlice(self: Array<T>, tracer: Tracer) =
    self.forEach { x -> x.trace(tracer) }

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T>`.
 * The trace implementation is provided by [traceHashTable].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T>
private class ImplTraceForHashTable

/** Trace a [MutableCollection] by tracing each element. */
fun <T : Trace> traceHashTable(self: MutableCollection<T>, tracer: Tracer) =
    self.forEach { e -> e.trace(tracer) }

/**
 * Marker class corresponding to `unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>`.
 * The trace implementation is provided by [traceSmallMap].
 */
// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>
private class ImplTraceForSmallMap

/** Trace a [SmallMap] by tracing each key-value pair. */
fun <K : Trace, V : Trace> traceSmallMap(self: SmallMap<K, V>, tracer: Tracer) {
    for ((k, v) in self.iterMutUnchecked()) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>`.
 * The trace implementation is provided by [traceSmallSet].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
private class ImplTraceForSmallSet

/** Trace a [SmallSet] by tracing each element. */
fun <T : Trace> traceSmallSet(self: SmallSet<T>, tracer: Tracer) {
    for (v in self.iterMutUnchecked()) {
        v.trace(tracer)
    }
}

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>`.
 * The trace implementation is provided by [traceHashed].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
private class ImplTraceForHashed

/** Trace a [Hashed] value by tracing its key. */
fun <T : Trace> traceHashed(self: Hashed<T>, tracer: Tracer) =
    self.key().trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>`.
 * The trace implementation is provided by [traceNullable].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
private class ImplTraceForOption

/** Trace a nullable value by tracing it if present. */
fun <T : Trace> traceNullable(self: T?, tracer: Tracer) {
    if (self != null) self.trace(tracer)
}

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T>`.
 * The trace implementation is provided by [traceRefCell].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T>
private class ImplTraceForRefCell

/** Trace a [MutableHolder] by tracing the inner value via [MutableHolder.getMut]. */
fun <T : Trace> traceRefCell(self: MutableHolder<T>, tracer: Tracer) =
    self.getMut().trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>`.
 * The trace implementation is provided by [traceCell].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>
private class ImplTraceForCell

/** Trace a [MutableHolder] by tracing the inner value via [MutableHolder.getMut]. */
fun <T : Trace> traceCell(self: MutableHolder<T>, tracer: Tracer) =
    self.getMut().trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>`.
 * The trace implementation is provided by [traceOnceCell].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>
private class ImplTraceForOnceCell

/** Trace a lazy-initialized nullable value by tracing it if initialized. */
fun <T : Trace> traceOnceCell(self: T?, tracer: Tracer) {
    if (self != null) self.trace(tracer)
}

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>`.
 * The trace implementation is provided by [traceUnsafeCell].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>
private class ImplTraceForUnsafeCell

/** Trace a [MutableHolder] by tracing the inner value via [MutableHolder.getMut]. */
fun <T : Trace> traceUnsafeCell(self: MutableHolder<T>, tracer: Tracer) =
    self.getMut().trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>`.
 * The trace implementation is provided by [traceBox].
 */
// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
private class ImplTraceForBox

/** Trace a boxed value by delegating to the inner [Trace] implementation. */
fun <T : Trace> traceBox(self: T, tracer: Tracer) =
    self.trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for ()`.
 * The trace implementation is provided by [traceUnit].
 */
// unsafe impl<'v> Trace<'v> for ()
private class ImplTraceForUnit

/** No-op trace for the unit type. */
fun traceUnit(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)`.
 * The trace implementation is provided by [traceTuple1].
 */
// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
private class ImplTraceForTuple1

/** Trace a single-element tuple by tracing the element. */
fun <T1 : Trace> traceTuple1(self: T1, tracer: Tracer) =
    self.trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)`.
 * The trace implementation is provided by [traceTuple2].
 */
// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)
private class ImplTraceForTuple2

/** Trace a pair by tracing each component. */
fun <T1 : Trace, T2 : Trace> traceTuple2(self: Pair<T1, T2>, tracer: Tracer) {
    self.first.trace(tracer)
    self.second.trace(tracer)
}

/**
 * Marker class corresponding to `unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)`.
 * The trace implementation is provided by [traceTuple3].
 */
// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)
private class ImplTraceForTuple3

/** Trace a triple by tracing each component. */
fun <T1 : Trace, T2 : Trace, T3 : Trace> traceTuple3(self: Triple<T1, T2, T3>, tracer: Tracer) {
    self.first.trace(tracer)
    self.second.trace(tracer)
    self.third.trace(tracer)
}

/**
 * Marker class corresponding to
 * `unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>, T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4)`.
 * The trace implementation is provided by [traceTuple4].
 */
// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>, T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4)
private class ImplTraceForTuple4

/** Trace a four-element tuple by tracing each component. */
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

/**
 * Marker class corresponding to `unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2>`.
 * The trace implementation is provided by [traceEither].
 */
// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2>
private class ImplTraceForEither

/** Trace an [Either] value by delegating to the active variant. */
fun <T1 : Trace, T2 : Trace> traceEither(self: Either<T1, T2>, tracer: Tracer) {
    when (self) {
        is EitherLeft -> self.value.trace(tracer)
        is EitherRight -> self.value.trace(tracer)
    }
}

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for Value<'v>`.
 * The trace implementation is provided by the [Value.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for Value<'v>
private class ImplTraceForValue

/** Trace a [Value] by delegating to the [Tracer]. */
fun Value.trace(tracer: Tracer) = tracer.trace(this)

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for FrozenValue`.
 * The trace implementation is provided by the [FrozenValue.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for FrozenValue
private class ImplTraceForFrozenValue

/** No-op trace for [FrozenValue] — frozen values contain no live references. */
fun FrozenValue.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for String`.
 * The trace implementation is provided by the [String.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for String
private class ImplTraceForString

/** No-op trace for [String] — strings contain no live references. */
fun String.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for usize`.
 * The trace implementation is provided by [traceUsize].
 */
// unsafe impl<'v> Trace<'v> for usize
private class ImplTraceForUsize

/** No-op trace for [ULong] (Rust `usize`) — contains no live references. */
fun traceUsize(self: ULong, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for i32`.
 * The trace implementation is provided by the [Int.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for i32
private class ImplTraceForI32

/** No-op trace for [Int] (Rust `i32`) — contains no live references. */
fun Int.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for u32`.
 * The trace implementation is provided by the [UInt.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for u32
private class ImplTraceForU32

/** No-op trace for [UInt] (Rust `u32`) — contains no live references. */
fun UInt.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for u64`.
 * The trace implementation is provided by the [ULong.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for u64
private class ImplTraceForU64

/** No-op trace for [ULong] (Rust `u64`) — contains no live references. */
fun ULong.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for bool`.
 * The trace implementation is provided by the [Boolean.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for bool
private class ImplTraceForBool

/** No-op trace for [Boolean] — contains no live references. */
fun Boolean.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicBool`.
 * The trace implementation is provided by the [AtomicBoolean.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for AtomicBool
private class ImplTraceForAtomicBool

/** No-op trace for [AtomicBoolean] — contains no live references. */
fun AtomicBoolean.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicI8`.
 * The trace implementation is provided by [traceAtomicI8].
 */
// unsafe impl<'v> Trace<'v> for AtomicI8
private class ImplTraceForAtomicI8

/** No-op trace for [AtomicInt] as `AtomicI8`. */
fun traceAtomicI8(self: AtomicInt, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicU8`.
 * The trace implementation is provided by [traceAtomicU8].
 */
// unsafe impl<'v> Trace<'v> for AtomicU8
private class ImplTraceForAtomicU8

/** No-op trace for [AtomicInt] as `AtomicU8`. */
fun traceAtomicU8(self: AtomicInt, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicI16`.
 * The trace implementation is provided by [traceAtomicI16].
 */
// unsafe impl<'v> Trace<'v> for AtomicI16
private class ImplTraceForAtomicI16

/** No-op trace for [AtomicInt] as `AtomicI16`. */
fun traceAtomicI16(self: AtomicInt, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicU16`.
 * The trace implementation is provided by [traceAtomicU16].
 */
// unsafe impl<'v> Trace<'v> for AtomicU16
private class ImplTraceForAtomicU16

/** No-op trace for [AtomicInt] as `AtomicU16`. */
fun traceAtomicU16(self: AtomicInt, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicI32`.
 * The trace implementation is provided by the [AtomicInt.traceI32] extension function.
 */
// unsafe impl<'v> Trace<'v> for AtomicI32
private class ImplTraceForAtomicI32

/** No-op trace for [AtomicInt] as `AtomicI32`. */
fun AtomicInt.traceI32(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicU32`.
 * The trace implementation is provided by [traceAtomicU32].
 */
// unsafe impl<'v> Trace<'v> for AtomicU32
private class ImplTraceForAtomicU32

/** No-op trace for [AtomicInt] as `AtomicU32`. */
fun traceAtomicU32(self: AtomicInt, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicI64`.
 * The trace implementation is provided by the [AtomicLong.traceI64] extension function.
 */
// unsafe impl<'v> Trace<'v> for AtomicI64
private class ImplTraceForAtomicI64

/** No-op trace for [AtomicLong] as `AtomicI64`. */
fun AtomicLong.traceI64(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicU64`.
 * The trace implementation is provided by [traceAtomicU64].
 */
// unsafe impl<'v> Trace<'v> for AtomicU64
private class ImplTraceForAtomicU64

/** No-op trace for [AtomicLong] as `AtomicU64`. */
fun traceAtomicU64(self: AtomicLong, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicUsize`.
 * The trace implementation is provided by [traceAtomicUsize].
 */
// unsafe impl<'v> Trace<'v> for AtomicUsize
private class ImplTraceForAtomicUsize

/** No-op trace for [AtomicLong] as `AtomicUsize`. */
fun traceAtomicUsize(self: AtomicLong, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for AtomicIsize`.
 * The trace implementation is provided by [traceAtomicIsize].
 */
// unsafe impl<'v> Trace<'v> for AtomicIsize
private class ImplTraceForAtomicIsize

/** No-op trace for [AtomicLong] as `AtomicIsize`. */
fun traceAtomicIsize(self: AtomicLong, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v> Trace<'v> for std::time::Instant`.
 * The trace implementation is provided by the [Instant.trace] extension function.
 */
// unsafe impl<'v> Trace<'v> for std::time::Instant
private class ImplTraceForInstant

/** No-op trace for [Instant] — contains no live references. */
fun Instant.trace(tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>`.
 * The trace implementation is provided by [tracePhantomData].
 */
// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
private class ImplTraceForPhantomData

/** No-op trace for phantom data — contains no live references. */
fun <T> tracePhantomData(self: T?, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>`.
 * The trace implementation is provided by [traceArcMutex].
 */
// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
private class ImplTraceForArcMutex

/** Trace an [Arc]<[Mutex]<[T]>> by locking and tracing the inner value. */
fun <T : Trace> traceArcMutex(self: MutableHolder<T>, tracer: Tracer) =
    self.getMut().trace(tracer)

/**
 * Marker class corresponding to `unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R`.
 * The trace implementation is provided by [traceFn1].
 */
// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
private class ImplTraceForFn1

/** No-op trace for single-argument function pointers. */
fun <A, R> traceFn1(self: (A) -> R, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R`.
 * The trace implementation is provided by [traceFn2].
 */
// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
private class ImplTraceForFn2

/** No-op trace for two-argument function pointers. */
fun <A, B, R> traceFn2(self: (A, B) -> R, tracer: Tracer) = Unit

/**
 * Marker class corresponding to `unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R`.
 * The trace implementation is provided by [traceFn3].
 */
// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
private class ImplTraceForFn3

/** No-op trace for three-argument function pointers. */
fun <A, B, C, R> traceFn3(self: (A, B, C) -> R, tracer: Tracer) = Unit
