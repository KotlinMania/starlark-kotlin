// port-lint: source src/values/layout/avalues/str_.rs
package io.github.kotlinmania.starlark.values.layout.avalues.str

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

import io.github.kotlinmania.starlark.collections.Hashed
import io.github.kotlinmania.starlark.collections.StarlarkHashValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.AValue
import io.github.kotlinmania.starlark.values.layout.AValueImpl
import io.github.kotlinmania.starlark.values.layout.AValueVTable
import io.github.kotlinmania.starlark.values.layout.AlignedSize
import io.github.kotlinmania.starlark.values.layout.ConstTypeId
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark.values.layout.constantString
import io.github.kotlinmania.starlark.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.heap.Tracer
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId

internal val VALUE_STR_A_VALUE_PTR: AValueHeader by lazy {
    AValueHeader(
        AValueVTable(
            staticTypeOfValue = ConstTypeId.of<StarlarkStr>(),
            starlarkTypeId = StarlarkTypeId.fromTypeId(ConstTypeId.of<StarlarkStr>()),
            typeName = "string",
            isStr = true,
            memorySizeFn = { ptr ->
                val str = ptr.valueRef<StarlarkStr>()
                val byteLen = str.len()
                ValueAllocSize.new(
                    AlignedSize.alignUp(StarlarkStr.offsetOfContent() + byteLen),
                )
            },
            heapFreezeFn = { _, ptr, freezer ->
                val str = ptr.valueRef<StarlarkStr>()
                val fv = freezer.frozenHeap().allocStrIntern(str.asStr())
                Result.success(fv.toFrozenValue())
            },
            heapCopyFn = { ptr, tracer ->
                val str = ptr.valueRef<StarlarkStr>()
                tracer.allocStr(str.asStr())
            },
            starlarkValue =
                object : StarlarkValue {
                    override val TYPE: String get() = "string"
                },
        ),
    )
}

internal fun starlarkStr(len: Int, hash: StarlarkHashValue): AValueImpl<StarlarkStrAValue> {
    // AValueImpl::<StarlarkStrAValue>::new(unsafe { StarlarkStr::new(len, hash) })
    //   assert!(len as u32 as usize == len, "len overflow");
    //   StarlarkStr { str: StarlarkStrN { hash: AtomicU32::new(hash.get()), len, body: [] } }
    require(len.toLong() == (len.toLong() and 0xFFFFFFFFL)) { "len overflow" }
    // In Rust, StarlarkStr stores hash in an AtomicU32 field and the body is
    // filled later via raw pointer writes in alloc_str_init.
    // In Kotlin, StarlarkStr wraps an immutable String and computes hash from it.
    // We create a placeholder of `len` zero-bytes; the content and hash are
    // finalized when the caller fills the allocation via alloc_str_init.
    val str = StarlarkStr(ByteArray(len).decodeToString())
    str.precomputedHash = hash
    return AValueImpl.new(str, StarlarkStrAValue(str))
}

internal class StarlarkStrAValue(
    private val str: StarlarkStr,
) : AValue {
    override val isStr: Boolean get() = true

    override fun extraLen(value: StarlarkValue): Int = StarlarkStr.payloadLenForLen((value as? StarlarkStr)?.len() ?: str.len())

    override fun offsetOfExtra(): Int = StarlarkStr.offsetOfContent()

    override fun heapFreeze(freezer: Freezer): Result<FrozenValue> {
        val s = str.asStr()
        val fv = freezer.frozenHeap().allocStrIntern(s)
        return Result.success(fv.toFrozenValue())
    }

    override fun heapCopy(tracer: Tracer): Value {
        val s = str.asStr()
        return tracer.allocStr(s)
    }

    override fun unpack(): StarlarkValue = str
}


/** Allocate a string on this heap. */
fun FrozenHeap.allocStr(x: String): FrozenStringValue = allocStrIntern(x)

/** Intern string. */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal fun FrozenHeap.allocStrIntern(s: String): FrozenStringValue = allocStrHashed(Hashed.new(s))

/** Allocate prehashed string. */
fun FrozenHeap.allocStrHashed(s: Hashed<String>): FrozenStringValue {
    val constant = constantString(s.key)
    if (constant != null) {
        return constant
    }
    val bytes = s.key.encodeToByteArray()
    return allocStrInit(bytes.size, s.hash) { dst ->
        bytes.copyInto(dst)
    }
}


/** Allocate a string on the heap. */
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun Heap.allocStr(x: String): StringValue {
    val constant = constantString(x)
    if (constant != null) {
        return constant.toStringValue()
    }
    val bytes = x.encodeToByteArray()
    return allocStrInit(bytes.size, StarlarkStr.UNINIT_HASH) { dst ->
        bytes.copyInto(dst)
    }
}

/** Intern string. */
fun Heap.allocStrIntern(x: String): StringValue {
    val constant = constantString(x)
    if (constant != null) {
        return constant.toStringValue()
    }
    val hash = StarlarkHashValue.new(x)
    val bytes = x.encodeToByteArray()
    return allocStrInit(bytes.size, hash) { dst ->
        bytes.copyInto(dst)
    }
}

/** Allocate a string on the heap, based on two concatenated strings. */
fun Heap.allocStrConcat(x: String, y: String): StringValue {
    val s =
        when {
            x.isEmpty() -> y
            y.isEmpty() -> x
            else -> x + y
        }
    val constant = constantString(s)
    if (constant != null) {
        return constant.toStringValue()
    }
    val bytes = s.encodeToByteArray()
    return allocStrInit(bytes.size, StarlarkStr.UNINIT_HASH) { dst ->
        bytes.copyInto(dst)
    }
}

/** Allocate a string on the heap, based on three concatenated strings. */
fun Heap.allocStrConcat3(x: String, y: String, z: String): StringValue =
    when {
        x.isEmpty() -> allocStrConcat(y, z)
        y.isEmpty() -> allocStrConcat(x, z)
        z.isEmpty() -> allocStrConcat(x, y)
        else -> {
            val combined = x + y + z
            val bytes = combined.encodeToByteArray()
            allocStrInit(bytes.size, StarlarkStr.UNINIT_HASH) { dst ->
                bytes.copyInto(dst)
            }
        }
    }

internal fun Heap.allocChar(x: Char): StringValue {
    val s = x.toString()
    val bytes = s.encodeToByteArray()
    return allocStrInit(bytes.size, StarlarkStr.UNINIT_HASH) { dst ->
        bytes.copyInto(dst)
    }
}
