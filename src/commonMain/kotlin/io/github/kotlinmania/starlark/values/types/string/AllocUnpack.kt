// port-lint: source src/values/types/string/alloc_unpack.rs
package io.github.kotlinmania.starlark.values.types.string

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

/** Implementations of alloc and unpack traits for string. */

// use std::convert::Infallible;

// use crate::typing::Ty;
// use crate::values::AllocFrozenValue;
// use crate::values::AllocValue;
// use crate::values::FrozenHeap;
// use crate::values::FrozenStringValue;
// use crate::values::FrozenValue;
// use crate::values::Heap;
// use crate::values::StringValue;
// use crate::values::UnpackValue;
// use crate::values::Value;
// use crate::values::alloc_value::AllocFrozenStringValue;
// use crate::values::alloc_value::AllocStringValue;
// use crate::values::type_repr::StarlarkTypeRepr;

import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StringTypeRepr
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue

// impl AllocFrozenValue for String {
//     fn alloc_frozen_value(self, heap: &FrozenHeap) -> FrozenValue {
//         self.alloc_frozen_string_value(heap).to_frozen_value()
//     }
// }
fun String.allocFrozenValue(heap: FrozenHeap): FrozenValue {
    return this.allocFrozenStringValue(heap).toFrozenValue()
}

// impl AllocFrozenStringValue for String {
//     fn alloc_frozen_string_value(self, heap: &FrozenHeap) -> FrozenStringValue {
//         heap.alloc_str(self.as_str())
//     }
// }
fun String.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue {
    return heap.allocStrIntern(this)
}

// impl<'a> AllocFrozenValue for &'a str {
//     fn alloc_frozen_value(self, heap: &FrozenHeap) -> FrozenValue {
//         self.alloc_frozen_string_value(heap).to_frozen_value()
//     }
// }
// impl<'a> AllocFrozenStringValue for &'a str {
//     fn alloc_frozen_string_value(self, heap: &FrozenHeap) -> FrozenStringValue {
//         heap.alloc_str(self)
//     }
// }
// Kotlin note: String is already a reference type, so &str and String share the same
// extension functions above.

// impl<'v> AllocValue<'v> for String {
//     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
//         self.alloc_string_value(heap).to_value()
//     }
// }
fun String.allocValue(heap: Heap): Value {
    return this.allocStringValue(heap).toValue()
}

// impl<'v> AllocStringValue<'v> for String {
//     fn alloc_string_value(self, heap: Heap<'v>) -> StringValue<'v> {
//         heap.alloc_str(self.as_str())
//     }
// }
fun String.allocStringValue(heap: Heap): StringValue {
    return StringValue.newUnchecked(heap.allocStr(this))
}

// impl StarlarkTypeRepr for char {
//     type Canonical = <String as StarlarkTypeRepr>::Canonical;
//
//     fn starlark_type_repr() -> Ty {
//         String::starlark_type_repr()
//     }
// }
// Kotlin note: Char uses the same type representation as String in Starlark because
// individual characters are represented as single-character strings.
// See StringTypeRepr in TypeRepr.kt.
object CharTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = StringTypeRepr.starlarkTypeRepr()
}

// impl<'v> AllocValue<'v> for char {
//     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
//         self.alloc_string_value(heap).to_value()
//     }
// }
fun Char.allocValue(heap: Heap): Value {
    return this.allocStringValue(heap).toValue()
}

// impl<'v> AllocStringValue<'v> for char {
//     fn alloc_string_value(self, heap: Heap<'v>) -> StringValue<'v> {
//         heap.alloc_char(self)
//     }
// }
fun Char.allocStringValue(heap: Heap): StringValue {
    // Rust has heap.alloc_char(self). Kotlin Heap does not have allocChar,
    // so we convert to a single-character string and allocate that.
    return this.toString().allocStringValue(heap)
}

// impl StarlarkTypeRepr for &'_ String {
//     type Canonical = <String as StarlarkTypeRepr>::Canonical;
//
//     fn starlark_type_repr() -> Ty {
//         String::starlark_type_repr()
//     }
// }
// impl<'v> AllocValue<'v> for &'_ String {
//     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
//         self.alloc_string_value(heap).to_value()
//     }
// }
// impl<'v> AllocStringValue<'v> for &'_ String {
//     fn alloc_string_value(self, heap: Heap<'v>) -> StringValue<'v> {
//         heap.alloc_str(self.as_str())
//     }
// }
// impl<'v> AllocValue<'v> for &'_ str {
//     fn alloc_value(self, heap: Heap<'v>) -> Value<'v> {
//         self.alloc_string_value(heap).to_value()
//     }
// }
// impl<'v> AllocStringValue<'v> for &'_ str {
//     fn alloc_string_value(self, heap: Heap<'v>) -> StringValue<'v> {
//         heap.alloc_str(self)
//     }
// }
// Kotlin note: String is already a reference type in Kotlin, so all string reference
// implementations (&str, &String) are covered by the String extension functions above.

// impl<'v> UnpackValue<'v> for &'v str {
//     type Error = Infallible;
//
//     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
//         Ok(value.unpack_str())
//     }
// }
fun unpackValueImplBorrowedString(value: Value): Result<String?> {
    return Result.success(value.unpackStr())
}

// impl<'v> UnpackValue<'v> for String {
//     type Error = Infallible;
//
//     fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error> {
//         Ok(value.unpack_str().map(ToOwned::to_owned))
//     }
// }
fun unpackValueImplOwnedString(value: Value): Result<String?> {
    // In Kotlin, strings are immutable reference types, so there's no
    // borrow-vs-owned distinction. The .map(ToOwned::to_owned) is a no-op.
    return Result.success(value.unpackStr())
}
