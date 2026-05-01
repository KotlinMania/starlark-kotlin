// port-lint: source values/trace.rs
package io.github.kotlinmania.starlark.values

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
 * If you miss a nested [Value], it will probably segfault.
 *
 * For the most cases a simple implementation is enough:
 *
 * ```
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

fun <T : Trace> MutableList<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

fun <T : Trace> Array<T>.trace(tracer: Tracer) {
    this.forEach { x -> x.trace(tracer) }
}

fun <T : Trace> MutableCollection<T>.trace(tracer: Tracer) {
    this.forEach { e -> e.trace(tracer) }
}

fun <K : Trace, V : Trace> SmallMap<K, V>.trace(tracer: Tracer) {
    for ((k, v) in this) {
        k.trace(tracer)
        v.trace(tracer)
    }
}

fun <T : Trace> SmallSet<T>.trace(tracer: Tracer) {
    for (v in this) {
        v.trace(tracer)
    }
}

fun <T : Trace> Hashed<T>.trace(tracer: Tracer) {
    this.key().trace(tracer)
}

fun <T : Trace> T?.trace(tracer: Tracer) {
    if (this != null) {
        this.trace(tracer)
    }
}

fun <T1 : Trace> Tuple1<T1>.trace(tracer: Tracer) {
    this.value0.trace(tracer)
}

fun <T1 : Trace, T2 : Trace> Pair<T1, T2>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
}

fun <T1 : Trace, T2 : Trace, T3 : Trace> Triple<T1, T2, T3>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
}

fun <T1 : Trace, T2 : Trace, T3 : Trace, T4 : Trace> Tuple4<T1, T2, T3, T4>.trace(tracer: Tracer) {
    this.first.trace(tracer)
    this.second.trace(tracer)
    this.third.trace(tracer)
    this.fourth.trace(tracer)
}

fun <T1 : Trace, T2 : Trace> Either<T1, T2>.trace(tracer: Tracer) {
    when (this) {
        is Either.Left -> this.value.trace(tracer)
        is Either.Right -> this.value.trace(tracer)
    }
}

fun ValueHolder.trace(tracer: Tracer) {
    tracer.trace(this)
}

fun FrozenValue.trace(tracer: Tracer) {
}

fun String.trace(tracer: Tracer) {
}

fun Int.trace(tracer: Tracer) {
}

fun UInt.trace(tracer: Tracer) {
}

fun ULong.trace(tracer: Tracer) {
}

fun Boolean.trace(tracer: Tracer) {
}

fun AtomicBoolean.trace(tracer: Tracer) {
}

fun AtomicInt.trace(tracer: Tracer) {
}

fun AtomicLong.trace(tracer: Tracer) {
}

fun Instant.trace(tracer: Tracer) {
}

fun <T> PhantomData<T>.trace(tracer: Tracer) {
}

fun Long.trace(tracer: Tracer) {
}

fun UByte.trace(tracer: Tracer) {
}

fun Byte.trace(tracer: Tracer) {
}

fun UShort.trace(tracer: Tracer) {
}

fun Short.trace(tracer: Tracer) {
}

fun <A, R> ((A) -> R).trace(tracer: Tracer) {
}

fun <A, B, R> ((A, B) -> R).trace(tracer: Tracer) {
}

fun <A, B, C, R> ((A, B, C) -> R).trace(tracer: Tracer) {
}

class ArcMutex<T : Trace>(val value: T) {
}

fun <T : Trace> ArcMutex<T>.trace(tracer: Tracer) {
    this.value.trace(tracer)
}

class AtomicI8(val value: Byte) {
}

class AtomicU8(val value: UByte) {
}

class AtomicI16(val value: Short) {
}

class AtomicU16(val value: UShort) {
}

class AtomicI32(val value: Int) {
}

class AtomicU32(val value: UInt) {
}

class AtomicI64(val value: Long) {
}

class AtomicU64(val value: ULong) {
}

class AtomicUsize(val value: ULong) {
}

class AtomicIsize(val value: Long) {
}

fun AtomicI8.trace(tracer: Tracer) {
}

fun AtomicU8.trace(tracer: Tracer) {
}

fun AtomicI16.trace(tracer: Tracer) {
}

fun AtomicU16.trace(tracer: Tracer) {
}

fun AtomicI32.trace(tracer: Tracer) {
}

fun AtomicU32.trace(tracer: Tracer) {
}

fun AtomicI64.trace(tracer: Tracer) {
}

fun AtomicU64.trace(tracer: Tracer) {
}

fun AtomicUsize.trace(tracer: Tracer) {
}

fun AtomicIsize.trace(tracer: Tracer) {
}
