// port-lint: source src/values/types/list/refs.rs
package io.github.kotlinmania.starlark_kotlin.values.types.list

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

import io.github.kotlinmania.starlark_kotlin.coerce.coerce
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.typeRepr.StarlarkTypeRepr

/**
 * Reference to list content (mutable or frozen).
 */
@JvmInline
value class ListRef<'v> internal constructor(
    internal val content: List<Value<'v>>
)

/**
 * Reference to frozen list content.
 */
@JvmInline
value class FrozenListRef internal constructor(
    internal val content: List<FrozenValue>
)

// impl<'v> ListRef<'v>
object ListRefImpl {
    /**
     * `type([])`, which is `"list"`.
     */
    const val TYPE: String = ListData.TYPE

    internal fun <'v> new(slice: List<Value<'v>>): ListRef<'v> = ListRef(slice)

    /**
     * Empty list reference.
     */
    fun <'v> empty(): ListRef<'v> {
        return new(emptyList())
    }

    /**
     * Downcast the value to the list or frozen list (both are represented by `ListRef`).
     */
    fun <'v> fromValue(x: Value<'v>): ListRef<'v>? {
        if (x.unpackFrozen() != null) {
            return x.downcastRef<ListGen<FrozenListData>>()
                ?.let { new(coerce(it.`0`.content())) }
        } else {
            val ptr = x.downcastRef<ListGen<ListData<'v>>>() ?: return null
            return new(ptr.`0`.content())
        }
    }

    /**
     * Downcast the list.
     */
    fun <'f> fromFrozenValue(x: FrozenValue): ListRef<'f>? {
        return x.downcastRef<ListGen<FrozenListData>>()
            ?.let { new(coerce(it.`0`.content())) }
    }
}

/**
 * List elements.
 */
fun <'v> ListRef<'v>.content(): List<Value<'v>> {
    return content
}

/**
 * Iterate over the elements in the list.
 */
fun <'v, 'a> ListRef<'v>.iter(): Iterator<Value<'v>> where 'v : 'a {
    return content.iterator()
}

// impl FrozenListRef
object FrozenListRefImpl {
    /**
     * `type([])`, which is `"list"`.
     */
    val TYPE: String = ListRefImpl.TYPE

    internal fun new(slice: List<FrozenValue>): FrozenListRef = FrozenListRef(slice)

    /**
     * Downcast to the frozen list.
     *
     * This function returns `null` if the value is not a list or the list is not frozen.
     */
    fun fromValue(x: Value<*>): FrozenListRef? {
        return fromFrozenValue(x.unpackFrozen() ?: return null)
    }

    /**
     * Downcast to the frozen list.
     *
     * This function returns `null` if the value is not a frozen list.
     * (Value cannot be a mutable list because value is frozen.)
     */
    fun fromFrozenValue(x: FrozenValue): FrozenListRef? {
        return x.downcastRef<ListGen<FrozenListData>>()
            ?.let { new(it.`0`.content()) }
    }
}

// impl<'v> Deref for ListRef<'v>
operator fun <'v> ListRef<'v>.getValue(thisRef: Any?, property: Any?): List<Value<'v>> {
    return content
}

// impl Deref for FrozenListRef
operator fun FrozenListRef.getValue(thisRef: Any?, property: Any?): List<FrozenValue> {
    return content
}

// impl<'v> Display for ListRef<'v>
fun <'v> ListRef<'v>.fmt(): String {
    return displayList(content)
}

// impl Display for FrozenListRef
fun FrozenListRef.fmt(): String {
    return displayList(coerce(content))
}

// impl<'v> StarlarkTypeRepr for &'v ListRef<'v>
object ListRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return VecStarlarkTypeRepr<Value<Any>>().starlarkTypeRepr()
    }
}

// impl<'v> StarlarkTypeRepr for &'v FrozenListRef
object FrozenListRefStarlarkTypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        return VecStarlarkTypeRepr<FrozenValue>().starlarkTypeRepr()
    }
}

// impl<'v> UnpackValue<'v> for &'v ListRef<'v>
object ListRefUnpackValue : UnpackValue<Nothing> {
    override fun <'v> unpackValueImpl(value: Value<'v>): Result<ListRef<'v>?> {
        return Result.success(ListRefImpl.fromValue(value))
    }
}

// impl<'v> UnpackValue<'v> for &'v FrozenListRef
object FrozenListRefUnpackValue : UnpackValue<io.github.kotlinmania.starlark_kotlin.Error> {
    override fun <'v> unpackValueImpl(value: Value<'v>): io.github.kotlinmania.starlark_kotlin.Result<FrozenListRef?> {
        return io.github.kotlinmania.starlark_kotlin.Result.success(FrozenListRefImpl.fromValue(value))
    }
}

internal fun <'v> displayList(content: List<Value<'v>>): String {
    return "[${content.joinToString(", ")}]"
}
