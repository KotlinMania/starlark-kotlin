// port-lint: source src/values/layout/avalues/str_.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.avalues.str_

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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.values.layout.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueImpl
import io.github.kotlinmania.starlark_kotlin.values.layout.ConstTypeId
import io.github.kotlinmania.starlark_kotlin.values.layout.AValueVTable
import io.github.kotlinmania.starlark_kotlin.values.layout.StarlarkValueRawPtr
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueAllocSize
import io.github.kotlinmania.starlark_kotlin.values.layout.AlignedSize
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.constantString
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId

// pub(crate) const VALUE_STR_A_VALUE_PTR: AValueHeader = AValueHeader::new_const::<StarlarkStrAValue>()
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
                    AlignedSize.alignUp(StarlarkStr.offsetOfContent() + byteLen)
                )
            },
            heapFreezeFn = { ptr, freezer ->
                val str = ptr.valueRef<StarlarkStr>()
                val fv = freezer.frozenHeap().allocStrIntern(str.asStr())
                Result.success(fv.toFrozenValue())
            },
            heapCopyFn = { ptr, tracer ->
                val str = ptr.valueRef<StarlarkStr>()
                tracer.allocStr(str.asStr())
            },
            starlarkValue = object : StarlarkValue {
                override val TYPE: String get() = "string"
            },
        )
    )
}

// #[inline]
// pub(crate) fn starlark_str<'v>(len: usize, hash: StarlarkHashValue) -> AValueImpl<...>
internal fun starlarkStr(len: Int, hash: StarlarkHashValue): AValueImpl<StarlarkStrAValue> {
    // AValueImpl::<StarlarkStrAValue>::new(unsafe { StarlarkStr::new(len, hash) })
    // StarlarkStr::new creates a struct header with byte length and pre-computed hash:
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
    return AValueImpl.new(str)
}

// pub(crate) struct StarlarkStrAValue;
internal class StarlarkStrAValue(private val str: StarlarkStr) : AValue {
    // impl AValue for StarlarkStrAValue

    // const IS_STR: bool = true;
    override val isStr: Boolean get() = true

    // fn extra_len(value: &StarlarkStr) -> usize
    override fun extraLen(value: StarlarkValue): Int {
        return StarlarkStr.payloadLenForLen((value as? StarlarkStr)?.len() ?: str.len())
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int {
        return StarlarkStr.offsetOfContent()
    }

    // unsafe fn heap_freeze(me: ..., freezer: &Freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(freezer: Freezer): FreezeResult<FrozenValue> {
        val s = str.asStr()
        val fv = freezer.frozenHeap().allocStrIntern(s)
        return Result.success(fv.toFrozenValue())
    }

    // unsafe fn heap_copy(me: ..., tracer: &Tracer<'v>) -> Value<'v>
    override fun heapCopy(tracer: Tracer): Value {
        val s = str.asStr()
        return tracer.allocStr(s)
    }

    // fn unpack(&self) -> &StarlarkValue
    override fun unpack(): StarlarkValue = str
}

// impl FrozenHeap

/** Allocate a string on this heap. */
// pub fn alloc_str(&self, x: &str) -> FrozenStringValue
fun FrozenHeap.allocStr(x: String): FrozenStringValue {
    return allocStrIntern(x)
}

/** Intern string. */
// pub(crate) fn alloc_str_intern(&self, s: &str) -> FrozenStringValue
internal fun FrozenHeap.allocStrIntern(s: String): FrozenStringValue {
    return allocStrHashed(Hashed.new(s))
}

/** Allocate prehashed string. */
// pub fn alloc_str_hashed(&self, s: Hashed<&str>) -> FrozenStringValue
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

// impl Heap

/** Allocate a string on the heap. */
// pub fn alloc_str(self, x: &str) -> StringValue<'v>
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
// pub fn alloc_str_intern(self, x: &str) -> StringValue<'v>
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
// pub fn alloc_str_concat(self, x: &str, y: &str) -> StringValue<'v>
fun Heap.allocStrConcat(x: String, y: String): StringValue {
    val s = when {
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
// pub fn alloc_str_concat3(x: &str, y: &str, z: &str) -> StringValue<'v>
fun Heap.allocStrConcat3(x: String, y: String, z: String): StringValue {
    return when {
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
}

// pub(crate) fn alloc_char(self, x: char) -> StringValue<'v>
internal fun Heap.allocChar(x: Char): StringValue {
    val s = x.toString()
    val bytes = s.encodeToByteArray()
    return allocStrInit(bytes.size, StarlarkStr.UNINIT_HASH) { dst ->
        bytes.copyInto(dst)
    }
}
