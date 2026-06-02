// port-lint: tests src/values/value_of_unchecked.rs (tests)
package io.github.kotlinmania.starlark.values

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
import io.github.kotlinmania.starlark.values.layout.constFrozenString
import kotlin.test.Test
import kotlin.test.assertEquals

private class OwnedStringRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.string()
}

private class BorrowedStringRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = Ty.string()
}

private class ReprNotSendSync(
    @Suppress("unused")
    private val value: MutableList<String>,
) : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty {
        error("not needed in test")
    }
}

class ValueOfUncheckedTest {
    @Test
    fun testCastExample() {
        val a: ValueOfUnchecked<OwnedStringRepr> = ValueOfUnchecked.new(constFrozenString("a").toValue())
        val b: ValueOfUnchecked<BorrowedStringRepr> = a.cast()
        assertEquals(a.get(), b.get())
    }

    @Test
    fun testFrozenValueOfUncheckedSendSync() {
        val value: FrozenValueOfUnchecked<ReprNotSendSync> =
            FrozenValueOfUnchecked.new(constFrozenString("a").toFrozenValue())
        assertEquals(constFrozenString("a").toFrozenValue(), value.get())
    }

    @Test
    fun testFrozenValueOfUncheckedCovariant() {
        fun assertCovariant(
            value: FrozenValueOfUnchecked<OwnedStringRepr>,
        ): FrozenValueOfUnchecked<OwnedStringRepr> = value

        val value: FrozenValueOfUnchecked<OwnedStringRepr> =
            FrozenValueOfUnchecked.new(constFrozenString("a").toFrozenValue())
        assertEquals(value.get(), assertCovariant(value).get())
    }
}
