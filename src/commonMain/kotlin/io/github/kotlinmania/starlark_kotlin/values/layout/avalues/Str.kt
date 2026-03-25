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

import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHashValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalue.AValueImpl
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.StarlarkStr
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.AValueHeader
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.UNINIT_HASH
import io.github.kotlinmania.starlark_kotlin.values.Tracer
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.values.types.string.payloadLenForLen
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.intern
import io.github.kotlinmania.starlark_kotlin.values.types.array.len
import io.github.kotlinmania.starlark_kotlin.values.types.any_array.offsetOfContent
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.toStringValue
import io.github.kotlinmania.starlark_kotlin.values.layout.constantString

// pub(crate) const VALUE_STR_A_VALUE_PTR: AValueHeader = ...
internal val VALUE_STR_A_VALUE_PTR: AValueHeader = AValueHeader.newConst<StarlarkStrAValue>()

// #[inline]
// pub(crate) fn starlark_str<'v>(len: usize, hash: StarlarkHashValue) -> AValueImpl<...>
internal fun starlarkStr(len: Int, hash: StarlarkHashValue): AValueImpl<StarlarkStr> {
    return AValueImpl(StarlarkStr.new(len, hash))
}

// pub(crate) struct StarlarkStrAValue;
internal object StarlarkStrAValue : AValue<StarlarkStr> {
    // impl AValue for StarlarkStrAValue

    // const IS_STR: bool = true;
    override val isStr: Boolean = true

    // fn extra_len(value: &StarlarkStr) -> usize
    override fun extraLen(value: StarlarkStr): Int {
        return StarlarkStr.payloadLenForLen(value.len())
    }

    // fn offset_of_extra() -> usize
    override fun offsetOfExtra(): Int {
        return StarlarkStr.offsetOfContent()
    }

    // unsafe fn heap_freeze(me: ..., freezer: &Freezer) -> FreezeResult<FrozenValue>
    override fun heapFreeze(value: StarlarkStr, freezer: Freezer): FrozenValue {
        val s = value.asStr()
        return freezer.alloc(s)
    }

    // unsafe fn heap_copy(me: ..., tracer: &Tracer<'v>) -> Value<'v>
    override fun heapCopy(value: StarlarkStr, tracer: Tracer): Value {
        val s = value.asStr()
        return tracer.allocStr(s)
    }
}

// impl FrozenHeap

/// Allocate a string on this heap.
// pub fn alloc_str(&self, x: &str) -> FrozenStringValue
fun FrozenHeap.allocStr(x: String): FrozenStringValue {
    return allocStrIntern(x)
}

/// Intern string.
// pub(crate) fn alloc_str_intern(&self, s: &str) -> FrozenStringValue
internal fun FrozenHeap.allocStrIntern(s: String): FrozenStringValue {
    return allocStrHashed(Hashed.new(s))
}

/// Allocate prehashed string.
// pub fn alloc_str_hashed(&self, s: Hashed<&str>) -> FrozenStringValue
fun FrozenHeap.allocStrHashed(s: Hashed<String>): FrozenStringValue {
    val constant = constantString(s.key)
    if (constant != null) {
        return constant
    }
    return stringInterner().intern(s) {
        allocStrInit(s.key.length, s.hash) { s.key }
    }
}

// impl Heap

/// Allocate a string on the heap.
// pub fn alloc_str(self, x: &str) -> StringValue<'v>
fun Heap.allocStr(x: String): StringValue {
    val constant = constantString(x)
    if (constant != null) {
        return constant.toStringValue()
    }
    return allocStrInit(x.length, StarlarkStr.UNINIT_HASH) { x }
}

/// Intern string.
// pub fn alloc_str_intern(self, x: &str) -> StringValue<'v>
fun Heap.allocStrIntern(x: String): StringValue {
    val constant = constantString(x)
    if (constant != null) {
        return constant.toStringValue()
    }
    val hashed = Hashed.new(x)
    return stringInterner().intern(hashed) {
        allocStrInit(x.length, hashed.hash) { x }
    }
}

/// Allocate a string on the heap, based on two concatenated strings.
// pub fn alloc_str_concat(self, x: &str, y: &str) -> StringValue<'v>
fun Heap.allocStrConcat(x: String, y: String): StringValue {
    return when {
        x.isEmpty() -> allocStr(y)
        y.isEmpty() -> allocStr(x)
        else -> allocStrInit(x.length + y.length, StarlarkStr.UNINIT_HASH) { x + y }
    }
}

/// Allocate a string on the heap, based on three concatenated strings.
// pub fn alloc_str_concat3(x: &str, y: &str, z: &str) -> StringValue<'v>
fun Heap.allocStrConcat3(x: String, y: String, z: String): StringValue {
    return when {
        x.isEmpty() -> allocStrConcat(y, z)
        y.isEmpty() -> allocStrConcat(x, z)
        z.isEmpty() -> allocStrConcat(x, y)
        else -> allocStrInit(x.length + y.length + z.length, StarlarkStr.UNINIT_HASH) { x + y + z }
    }
}

// pub(crate) fn alloc_char(self, x: char) -> StringValue<'v>
internal fun Heap.allocChar(x: Char): StringValue {
    return allocStr(x.toString())
}
