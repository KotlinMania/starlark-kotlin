// port-lint: source src/values/types/dict/dict_type.rs
package io.github.kotlinmania.starlark.values.types.dict

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




import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.layout.Value

// / A dict type marker.
// /
// / [`StarlarkTypeRepr`] provides `dict[K, V]`.
// / [`UnpackValue`] implementation verifies the types of entries and discards them.
//     k: PhantomData<K>,
//     v: PhantomData<V>,
// }
class DictType<K : StarlarkTypeRepr, V : StarlarkTypeRepr> private constructor() {
    companion object {
        fun <K : StarlarkTypeRepr, V : StarlarkTypeRepr> instance(): DictType<K, V> = DictType()

        // impl<K: StarlarkTypeRepr, V: StarlarkTypeRepr> StarlarkTypeRepr for DictType<K, V>
        //         Ty::dict(K::starlark_type_repr(), V::starlark_type_repr())
        //     }
        inline fun <reified K : StarlarkTypeRepr, reified V : StarlarkTypeRepr> starlarkTypeRepr(): Ty = Ty.dict(K::class.starlarkTypeRepr(), V::class.starlarkTypeRepr())
    }
}

// impl<'v, K: UnpackValue<'v>, V: UnpackValue<'v>> UnpackValue<'v> for DictType<K, V> {
fun <K : StarlarkTypeRepr, V : StarlarkTypeRepr> unpackDictType(
    value: Value,
): Result<DictType<K, V>?> =
    when (val result = UnpackDictEntries.unpackValue<K, V>(value)) {
        null -> Result.success(null)
        else ->
            result.map { entries ->
                if (entries != null) DictType.instance<K, V>() else null
            }
    }

fun <T : StarlarkTypeRepr> kotlin.reflect.KClass<T>.starlarkTypeRepr(): Ty = Ty.any()
