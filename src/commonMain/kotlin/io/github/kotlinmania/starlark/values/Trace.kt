// port-lint: source src/values/trace.rs
package io.github.kotlinmania.starlark.values

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

import io.github.kotlinmania.starlark.Either
import starlarkmap.Hashed
import starlarkmap.smallmap.SmallMap
import starlarkmap.smallset.SmallSet
import io.github.kotlinmania.starlark.values.layout.heap.ValueHolder
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlinx.datetime.Instant

/**
 * Called by the garbage collection, and must walk over every contained [Value] in the type.
 *
 * For the most cases a simple implementation is enough:
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

/** Trace for `Vec<T>` — traces each element. */
fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

/** Trace for `[T]` (slice) — traces each element. */
fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

/** Trace for `HashTable<T>` — traces each element. */
fun <T : Trace> MutableCollection<T>.trace(tracer: Tracer) {
    this.forEach { e -> e.trace(tracer) }
}

/** Trace for `SmallMap<K, V>` — traces each key and value. */
fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    for ((k, v) in this) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

/** Trace for `SmallSet<T>` — traces each element. */
fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this) {
        v.trace(tracer)
    }
}

/** Trace for `Hashed<T>` — traces the key. */
fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.key().trace(tracer)
}

/** Trace for `Option<T>` — traces the value if present. */
fun <T : Trace> T?.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

/** Trace for `RefCell<T>` — traces the inner value. */
fun <T : Trace> traceRefCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

/** Trace for `Cell<T>` — traces the inner value. */
fun <T : Trace> traceCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

/** Trace for `OnceCell<T>` — traces the inner value if set. */
fun <T : Trace> traceOnceCell(self: T?, tracer: Tracer) {
    self?.trace(tracer)
}

/** Trace for `UnsafeCell<T>` — traces the inner value. */
fun <T : Trace> traceUnsafeCell(self: T, tracer: Tracer) {
    self.trace(tracer)
}

/** Trace for `Box<T>` — traces the inner value. */
fun <T : Trace> traceBox(self: T, tracer: Tracer) {
    self.trace(tracer)
}

/** Trace for `()` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun traceUnit(_tracer: Tracer) {
}

/** Trace for 1-tuple `(T1,)`. */
fun <T1 : Trace> Tuple1<T1>.trace(tracer: Tracer) {
    this.value0.trace(tracer)
}

/** Trace for 2-tuple `(T1, T2)`. */
fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

/** Trace for 3-tuple `(T1, T2, T3)`. */
fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

/** Trace for 4-tuple `(T1, T2, T3, T4)`. */
fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> Tuple4<T1, T2, T3, T4>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
    this.fourth.trace(tracer)
}

/** Trace for `Either<T1, T2>` — traces whichever side is present. */
fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

/** Trace for [Value] — delegates to [Tracer.trace]. */
fun ValueHolder.trace(tracer: Tracer) {
    tracer.trace(this)
}

/** Trace for [FrozenValue] — already frozen, nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun FrozenValue.trace(tracer: Tracer) {
}

/** Trace for [String] — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun String.trace(tracer: Tracer) {
}

/** Trace for `usize` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun Int.traceUsize(tracer: Tracer) {
}

/** Trace for `i32` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun Int.trace(tracer: Tracer) {
}

/** Trace for `u32` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun UInt.trace(tracer: Tracer) {
}

/** Trace for `u64` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun ULong.trace(tracer: Tracer) {
}

/** Trace for `bool` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun Boolean.trace(tracer: Tracer) {
}

/** Trace for `AtomicBool` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicBoolean.trace(tracer: Tracer) {
}

/** Trace for `AtomicI8` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicI8(tracer: Tracer) {
}

/** Trace for `AtomicU8` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU8(tracer: Tracer) {
}

/** Trace for `AtomicI16` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicI16(tracer: Tracer) {
}

/** Trace for `AtomicU16` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU16(tracer: Tracer) {
}

/** Trace for `AtomicI32` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.trace(tracer: Tracer) {
}

/** Trace for `AtomicU32` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicInt.traceAtomicU32(tracer: Tracer) {
}

/** Trace for `AtomicI64` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.trace(tracer: Tracer) {
}

/** Trace for `AtomicU64` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicU64(tracer: Tracer) {
}

/** Trace for `AtomicUsize` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicUsize(tracer: Tracer) {
}

/** Trace for `AtomicIsize` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun AtomicLong.traceAtomicIsize(tracer: Tracer) {
}

/** Trace for `Instant` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun Instant.trace(tracer: Tracer) {
}

/** Trace for `PhantomData<T>` — nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun <T> PhantomData<T>.trace(tracer: Tracer) {
}

/** Trace for `Arc<Mutex<T>>` — locks and traces the inner value. */
fun <T : Trace> traceArcMutex(value: T, tracer: Tracer) {
    value.trace(tracer)
}

/** Trace for `fn(A) -> R` — function pointers have nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun <A, R> traceFn1(value: (A) -> R, tracer: Tracer) {
}

/** Trace for `fn(A, B) -> R` — function pointers have nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun <A, B, R> traceFn2(value: (A, B) -> R, tracer: Tracer) {
}

/** Trace for `fn(A, B, C) -> R` — function pointers have nothing to trace. */
@Suppress("UNUSED_PARAMETER")
fun <A, B, C, R> traceFn3(value: (A, B, C) -> R, tracer: Tracer) {
}
