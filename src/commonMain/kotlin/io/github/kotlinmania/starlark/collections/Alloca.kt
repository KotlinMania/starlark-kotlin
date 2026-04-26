// port-lint: source src/collections/alloca.rs
package io.github.kotlinmania.starlark.collections

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

private const val INITIAL_SIZE: Int = 1000000 // ~ 1Mb
private const val ALIGN: Int = 8

internal class Align(
    val value: ULong,
)

/**
 * A reusable arena-style allocator for temporary slices.
 *
 * Transliterated from the Rust implementation which uses a continuous buffer and bump-pointer
 * allocation, doubling the buffer when capacity is exceeded. The Kotlin port models "words"
 * of size [ALIGN] and keeps old buffers around after growth, matching the Rust behavior.
 *
 * Note that Kotlin does not expose raw uninitialized memory in `commonMain`, so the returned
 * "uninitialized" slices are represented as `MutableList<T?>` backed by an `Array<Any?>`.
 */
internal class Alloca {

    companion object {
        fun new(): Alloca = Alloca()

        fun withCapacity(sizeBytes: Int): Alloca = Alloca(sizeBytes)
    }

    private class Layout(
        val size: Int,
        val align: Int,
    ) {
        companion object {
            fun arrayAlign(sizeWords: Int): Layout {
                return Layout(sizeWords * ALIGN, ALIGN)
            }

            fun fromSizeAlign(size: Int, align: Int): Layout {
                return Layout(size, align)
            }
        }
    }

    private class Buffer(
        private val data: ULongArray,
        private val layout: Layout,
    ) {
        companion object {
            fun alloc(layout: Layout): Buffer {
                val sizeWords = layout.size / ALIGN
                return Buffer(ULongArray(sizeWords), layout)
            }
        }

        fun ptr(): Int {
            return 0
        }

        fun end(): Int {
            return sizeWords()
        }

        fun sizeWords(): Int {
            return layout.size / ALIGN
        }

        fun drop() {
            // In Rust this deallocates the backing buffer. In Kotlin, GC owns memory.
        }
    }

    private var alloc: Int
    private var end: Int
    private val buffers: MutableList<Buffer>

    constructor() : this(INITIAL_SIZE)

    constructor(sizeBytes: Int) {
        val sizeWords = divCeil(sizeBytes, ALIGN)
        val layout = Layout.arrayAlign(sizeWords)
        val buffer = Buffer.alloc(layout)
        alloc = buffer.ptr()
        end = buffer.end()
        buffers = ArrayList(1)
        buffers.add(buffer)
    }

    private fun assertState() {
        check(end - alloc >= 0)
        check(end - alloc <= buffers.last().sizeWords())
    }

    private fun allocateMore(len: Int, one: Layout) {
        val want = Layout.fromSizeAlign(one.size * len, one.align)
        check(want.align <= ALIGN)
        val last = buffers.last()
        val sizeWords = last.sizeWords() * 2 + want.size / ALIGN
        val layout = Layout.arrayAlign(sizeWords)
        val buffer = Buffer.alloc(layout)
        val pointer = buffer.ptr()
        val end = buffer.end()
        buffers.add(buffer)
        this.alloc = pointer
        this.end = end
    }

    internal fun <T> remInWordsToRemInT(remInWords: Int): Int {
        return remInWords
    }

    internal fun <T> lenInTToLenInWords(len: Int): Int {
        return len
    }

    /**
     * Note that the finalizer for the `T` will not be called. That's safe if there is no finalizer,
     * or you call it yourself.
     */
    fun <T, R> allocaUninit(len: Int, k: (MutableList<T?>) -> R): R {
        assertState()

        var start = alloc

        val remWords = end - start
        val remInT = remInWordsToRemInT<T>(remWords)
        if (len > remInT) {
            allocateMore(len, Layout(ALIGN, ALIGN))
            start = alloc
        }
        // Capture the active buffer AFTER any growth so the rollback check below
        // mirrors Rust's `ptr::eq(self.alloc.get(), stop)` — we only need to know
        // whether the callback added another buffer beyond the one we just sat in.
        val startBufferIndex = buffers.lastIndex

        val sizeWords = lenInTToLenInWords<T>(len)
        val stop = start + sizeWords
        val old = start
        alloc = stop

        val data = MutableList<T?>(len) { null }
        val res = k(data)

        // If the pointer changed, it means a callback called alloca again, which allocated a new buffer.
        // So we are abandoning the current allocation here.
        if (buffers.lastIndex == startBufferIndex && alloc == stop) {
            alloc = old
        }

        assertState()
        return res
    }

    /**
     * Allocate and initialize an array of [len] elements, then pass it to [k].
     *
     * Each element is initialized by calling [init]. The resulting mutable list
     * is passed to the callback.
     *
     * In Rust, this calls `alloca_uninit` internally and writes each element
     * using `MaybeUninit::write`, then transmutes to an initialized slice.
     *
     * @param len Number of elements to allocate.
     * @param init Factory function called once per element to produce its initial value.
     * @param k Callback that receives the initialized mutable list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaInit(len: Int, init: () -> T, k: (MutableList<T>) -> R): R {
        return allocaUninit<T, R>(len) { data ->
            for (i in 0 until len) {
                data[i] = init()
            }
            val initView = object : AbstractMutableList<T>() {
                override val size: Int = data.size

                override fun get(index: Int): T {
                    return requireNotNull(data[index])
                }

                override fun set(index: Int, element: T): T {
                    val old = requireNotNull(data[index])
                    data[index] = element
                    return old
                }

                override fun add(index: Int, element: T) {
                    throw UnsupportedOperationException("fixed size")
                }

                override fun removeAt(index: Int): T {
                    throw UnsupportedOperationException("fixed size")
                }
            }
            k(initView)
        }
    }

    /**
     * Allocate an array of [len] elements, all set to [fill], then pass it to [k].
     *
     * This is a convenience wrapper around [allocaInit] for copyable values.
     *
     * @param len Number of elements to allocate.
     * @param fill The value to fill every element with.
     * @param k Callback that receives the filled mutable list and produces a result.
     * @return The result produced by [k].
     */
    fun <T, R> allocaFill(len: Int, fill: T, k: (MutableList<T>) -> R): R {
        return allocaInit(len, { fill }, k)
    }

    private class DropSliceGuard<A>(
        private val data: MutableList<A?>,
    ) {
        fun drop() {
            for (i in 0 until data.size) {
                data[i] = null
            }
        }
    }

    private fun <T, R> allocaConcatSlow(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        return allocaUninit<T, R>(x.size + y.size) { xy ->
            val xUninit = xy.subList(0, x.size)
            val yUninit = xy.subList(x.size, x.size + y.size)

            for (i in 0 until xUninit.size) {
                xUninit[i] = x[i]
            }
            val xDropGuard = DropSliceGuard(xUninit)

            for (i in 0 until yUninit.size) {
                yUninit[i] = y[i]
            }
            val yDropGuard = DropSliceGuard(yUninit)

            val initView = object : AbstractList<T>() {
                override val size: Int = xy.size

                override fun get(index: Int): T {
                    return requireNotNull(xy[index])
                }
            }

            try {
                k(initView)
            } finally {
                yDropGuard.drop()
                xDropGuard.drop()
            }
        }
    }

    fun <T, R> allocaConcat(x: List<T>, y: List<T>, k: (List<T>) -> R): R {
        return if (x.isEmpty()) {
            k(y)
        } else if (y.isEmpty()) {
            k(x)
        } else {
            allocaConcatSlow(x, y, k)
        }
    }

    internal fun buffersLen(): Int {
        return buffers.size
    }
}

private fun divCeil(n: Int, d: Int): Int {
    check(d > 0)
    return ((n.toLong() + d.toLong() - 1L) / d.toLong()).toInt()
}
