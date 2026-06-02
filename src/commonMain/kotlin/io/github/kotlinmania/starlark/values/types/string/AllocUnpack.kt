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
//         self.alloc_frozen_string_value(heap).to_frozen_value()
//     }
// }
fun String.allocFrozenValue(heap: FrozenHeap): FrozenValue = this.allocFrozenStringValue(heap).toFrozenValue()

// impl AllocFrozenStringValue for String {
//         heap.alloc_str(self.as_str())
//     }
// }
fun String.allocFrozenStringValue(heap: FrozenHeap): FrozenStringValue = heap.allocStrIntern(this)

// impl<'a> AllocFrozenValue for &'a str {
//         self.alloc_frozen_string_value(heap).to_frozen_value()
//     }
// }
// impl<'a> AllocFrozenStringValue for &'a str {
//         heap.alloc_str(self)
//     }
// }
// Kotlin note: String is already a reference type, so &str and String share the same
// extension functions above.

// impl<'v> AllocValue<'v> for String {
//         self.alloc_string_value(heap).to_value()
//     }
// }
fun String.allocValue(heap: Heap): Value = this.allocStringValue(heap).toValue()

// impl<'v> AllocStringValue<'v> for String {
//         heap.alloc_str(self.as_str())
//     }
// }
fun String.allocStringValue(heap: Heap): StringValue = StringValue.newUnchecked(heap.allocStr(this))

// impl StarlarkTypeRepr for char {
//
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
//         self.alloc_string_value(heap).to_value()
//     }
// }
fun Char.allocValue(heap: Heap): Value = this.allocStringValue(heap).toValue()

// impl<'v> AllocStringValue<'v> for char {
//         heap.alloc_char(self)
//     }
// }
fun Char.allocStringValue(heap: Heap): StringValue {
    // Rust has heap.alloc_char(self). Kotlin Heap does not have allocChar,
    // so we convert to a single-character string and allocate that.
    return this.toString().allocStringValue(heap)
}

// impl StarlarkTypeRepr for &'_ String {
//
//         String::starlark_type_repr()
//     }
// }
// impl<'v> AllocValue<'v> for &'_ String {
//         self.alloc_string_value(heap).to_value()
//     }
// }
// impl<'v> AllocStringValue<'v> for &'_ String {
//         heap.alloc_str(self.as_str())
//     }
// }
// impl<'v> AllocValue<'v> for &'_ str {
//         self.alloc_string_value(heap).to_value()
//     }
// }
// impl<'v> AllocStringValue<'v> for &'_ str {
//         heap.alloc_str(self)
//     }
// }
// Kotlin note: String is already a reference type in Kotlin, so all string reference
// implementations (&str, &String) are covered by the String extension functions above.

// impl<'v> UnpackValue<'v> for &'v str {
//
//         Ok(value.unpack_str())
//     }
// }
fun unpackValueImplBorrowedString(value: Value): Result<String?> = Result.success(value.unpackStr())

// impl<'v> UnpackValue<'v> for String {
//
//         Ok(value.unpack_str().map(ToOwned::to_owned))
//     }
// }
fun unpackValueImplOwnedString(value: Value): Result<String?> {
    // In Kotlin, strings are immutable reference types, so there's no
    // borrow-vs-owned distinction. The .map(ToOwned::to_owned) is a no-op.
    return Result.success(value.unpackStr())
}
