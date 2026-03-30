// port-lint: source src/values/trace.rs
package io.github.kotlinmania.starlark_kotlin.values

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

import io.github.kotlinmania.starlark_kotlin.Either
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.SmallSet
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.ValueHolder
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlinx.datetime.Instant

/**
 * Called by the garbage collection, and must walk over every contained [Value] in the type.
 *
 * In Rust this trait is marked `unsafe` because if you miss a nested [Value],
 * it will probably segfault.
 *
 * For most cases a derived implementation is enough:
 *
 * ```kotlin
 * class MySet(val keys: MutableList<Value>) : Trace {
 *     override fun trace(tracer: Tracer) {
 *         keys.forEach { it.trace(tracer) }
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
     * Generally this function should not do anything except calling `trace` on the fields.
     */
    fun trace(tracer: Tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Vec<T>
fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for [T]
fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for HashTable<T>
fun <T : Trace> MutableCollection<T>.trace(tracer: Tracer) {
    this.forEach { e -> e.trace(tracer) }
}

// unsafe impl<'v, K: Trace<'v>, V: Trace<'v>> Trace<'v> for SmallMap<K, V>
fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    for ((k, v) in this.iter()) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for SmallSet<T>
fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this.iter()) {
        v.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Hashed<T>
fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.key().trace(tracer)
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Option<T>
fun <T : Trace> T?.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for RefCell<T>
// Kotlin has no RefCell; the value is traced directly.
// Callers should invoke value.trace(tracer) on the inner value.

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Cell<T>
// Kotlin has no Cell; the value is traced directly.
// Callers should invoke value.trace(tracer) on the inner value.

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for OnceCell<T>
// Kotlin has no OnceCell; modeled as T? and covered by the Option/nullable trace above.

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for UnsafeCell<T>
// Kotlin has no UnsafeCell; the value is traced directly.
// Callers should invoke value.trace(tracer) on the inner value.

// unsafe impl<'v, T: Trace<'v> + ?Sized> Trace<'v> for Box<T>
// Kotlin has no Box; values are references. Trace is called directly on the value.

// unsafe impl<'v> Trace<'v> for ()
// Unit has nothing to trace; no extension needed.

// unsafe impl<'v, T1: Trace<'v>> Trace<'v> for (T1,)
// Kotlin has no single-element tuple; the value is traced directly.

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for (T1, T2)
fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>, T3: Trace<'v>> Trace<'v> for (T1, T2, T3)
fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

// unsafe impl<'v, T1, T2, T3, T4: Trace<'v>> Trace<'v> for (T1, T2, T3, T4)
// Kotlin has no 4-tuple type. Callers should trace each element individually.

// unsafe impl<'v, T1: Trace<'v>, T2: Trace<'v>> Trace<'v> for Either<T1, T2>
fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

// unsafe impl<'v> Trace<'v> for Value<'v>
fun ValueHolder.traceValue(tracer: Tracer) {
    tracer.trace(this)
}

// unsafe impl<'v> Trace<'v> for FrozenValue
@Suppress("UNUSED_PARAMETER")
fun FrozenValue.trace(tracer: Tracer) {
    // FrozenValue cannot contain unfrozen references, so tracing is a no-op.
}

// unsafe impl<'v> Trace<'v> for String
@Suppress("UNUSED_PARAMETER")
fun String.trace(tracer: Tracer) {
    // Strings contain no Values, no-op.
}

// unsafe impl<'v> Trace<'v> for usize
@Suppress("UNUSED_PARAMETER")
fun ULong.trace(tracer: Tracer) {
    // Primitive type, no-op.
}

// unsafe impl<'v> Trace<'v> for i32
@Suppress("UNUSED_PARAMETER")
fun Int.trace(tracer: Tracer) {
    // Primitive type, no-op.
}

// unsafe impl<'v> Trace<'v> for u32
@Suppress("UNUSED_PARAMETER")
fun UInt.trace(tracer: Tracer) {
    // Primitive type, no-op.
}

// unsafe impl<'v> Trace<'v> for u64
@Suppress("UNUSED_PARAMETER")
fun ULong.traceU64(tracer: Tracer) {
    // Primitive type, no-op.
}

// unsafe impl<'v> Trace<'v> for bool
@Suppress("UNUSED_PARAMETER")
fun Boolean.trace(tracer: Tracer) {
    // Primitive type, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicBool
@Suppress("UNUSED_PARAMETER")
fun AtomicBoolean.trace(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicI8
// Kotlin maps AtomicI8 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicI8(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicU8
// Kotlin maps AtomicU8 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU8(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicI16
// Kotlin maps AtomicI16 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicI16(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicU16
// Kotlin maps AtomicU16 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU16(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicI32
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.trace(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicU32
// Kotlin maps AtomicU32 to AtomicInt.
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU32(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicI64
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.trace(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicU64
// Kotlin maps AtomicU64 to AtomicLong.
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicU64(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicUsize
// Kotlin maps AtomicUsize to AtomicLong.
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicUsize(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for AtomicIsize
// Kotlin maps AtomicIsize to AtomicLong.
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicIsize(tracer: Tracer) {
    // Atomic primitive, no-op.
}

// unsafe impl<'v> Trace<'v> for std::time::Instant
@Suppress("UNUSED_PARAMETER")
fun Instant.trace(tracer: Tracer) {
    // Instant contains no Values, no-op.
}

// unsafe impl<'v, T: ?Sized> Trace<'v> for marker::PhantomData<T>
// PhantomData is a zero-size marker in Rust with no Kotlin equivalent; nothing to trace.

// unsafe impl<'v, T: Trace<'v>> Trace<'v> for Arc<Mutex<T>>
// In Kotlin, there is no Arc/Mutex in commonMain; callers should lock and trace the inner value.
// Provided as a helper that traces the value directly.
fun <T : Trace> traceArcMutex(value: T, tracer: Tracer) {
    value.trace(tracer)
}

// unsafe impl<'v, A, R> Trace<'v> for fn(A) -> R
// Function pointers contain no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun <A, R> traceFn1(value: (A) -> R, tracer: Tracer) {
}

// unsafe impl<'v, A, B, R> Trace<'v> for fn(A, B) -> R
// Function pointers contain no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun <A, B, R> traceFn2(value: (A, B) -> R, tracer: Tracer) {
}

// unsafe impl<'v, A, B, C, R> Trace<'v> for fn(A, B, C) -> R
// Function pointers contain no Values, no-op.
@Suppress("UNUSED_PARAMETER")
fun <A, B, C, R> traceFn3(value: (A, B, C) -> R, tracer: Tracer) {
}
