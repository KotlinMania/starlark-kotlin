// port-lint: source values/typing/typeCompiled/matchers.rs
package io.github.kotlinmania.starlark.values.typing.typecompiled

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

/** TypeMatcher implementations for runtime type checking. */

import io.github.kotlinmania.starlark.values.starlarktypeid.StarlarkTypeId
import io.github.kotlinmania.starlark.values.types.dict.DictRef
import io.github.kotlinmania.starlark.values.types.dict.DictGen
import io.github.kotlinmania.starlark.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark.values.types.dict.iter
import io.github.kotlinmania.starlark.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark.values.types.list.ListRef
import io.github.kotlinmania.starlark.values.types.list.ListGen
import io.github.kotlinmania.starlark.values.types.set.SetGen
import io.github.kotlinmania.starlark.values.types.set.SetRef
import io.github.kotlinmania.starlark.values.types.set.content
import io.github.kotlinmania.starlark.values.types.tuple.TupleGen
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.typing.TyStarlarkValue

internal object IsAny : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return true
    }

    override fun isWildcard(): Boolean {
        return true
    }
}

internal object IsNever : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return false
    }
}

internal object IsStr : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.unpackStr() != null
    }
}

internal object IsList : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of(ListGen::class)
    }
}

internal class IsListOf(
    val item: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val list = ListRef.fromValue(value) ?: return false
        return list.content().all { v -> item.matches(v) }
    }
}

internal class IsTupleOf(
    val elem: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val tuple = TupleGen.fromValue(value) ?: return false
        return tuple.content().all { v -> elem.matches(v) }
    }
}

internal class IsTupleElems(
    val elems: List<TypeMatcherBox>,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val tuple = TupleGen.fromValue(value) ?: return false
        val content = tuple.content()
        if (content.size != elems.size) return false
        return content.zip(elems).all { (v, t) -> t.matches(v) }
    }
}

internal object IsTupleElems0 : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val tuple = TupleGen.fromValue(value) ?: return false
        return tuple.content().isEmpty()
    }
}

internal class IsTupleElems1(
    val a: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val tuple = TupleGen.fromValue(value) ?: return false
        val content = tuple.content()
        if (content.size != 1) return false
        return a.matches(content[0])
    }
}

internal class IsTupleElems2(
    val a: TypeMatcher,
    val b: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val tuple = TupleGen.fromValue(value) ?: return false
        val content = tuple.content()
        if (content.size != 2) return false
        return a.matches(content[0]) && b.matches(content[1])
    }
}

internal object IsDict : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of(DictGen::class)
    }
}

internal class IsDictOf(
    val key: TypeMatcher,
    val valueMatcher: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val dict = dictRefFromValue(value) ?: return false
        return dict.iter().all { pair -> key.matches(pair.first) && valueMatcher.matches(pair.second) }
    }
}

internal object IsSet : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of(SetGen::class)
    }
}

internal class IsSetOf(
    val item: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        val set = SetRef.unpackValueOpt(value) ?: return false
        return set.content.iter().all { v -> item.matches(v) }
    }
}

internal class IsAnyOfTwo(
    val a: TypeMatcher,
    val b: TypeMatcher,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return a.matches(value) || b.matches(value)
    }
}

internal class IsAnyOf(
    val matchers: List<TypeMatcher>,
) : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return matchers.any { t -> t.matches(value) }
    }
}

internal object IsCallable : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.vtable().hasInvoke
    }
}

internal object IsType : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.vtable().hasEvalType
    }
}

internal object IsIterable : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.vtable().hasIterate
    }
}

internal object IsInt : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return StarlarkIntRef.unpack(value) != null
    }
}

internal object IsBool : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.unpackBool() != null
    }
}

internal object IsNone : TypeMatcher {

    override fun matches(value: Value): Boolean {
        return value.isNone()
    }
}

/**
 * Matches a value by its Starlark type name.
 *
 * uses string-based type names while the runtime uses [KClass]-based IDs. We bridge
 * this by comparing the type name from [AValueVTable.typeName] against the type name
 * from [TyStarlarkValue.starlarkTypeId].
 */
internal class StarlarkTypeIdMatcher(
    private val expectedTypeName: String,
) : TypeMatcher {
    companion object {
        fun new(ty: TyStarlarkValue): StarlarkTypeIdMatcher {
            return StarlarkTypeIdMatcher(
                expectedTypeName = ty.starlarkTypeId(),
            )
        }
    }

    override fun matches(value: Value): Boolean {
        return value.vtable().typeName == expectedTypeName
    }
}
