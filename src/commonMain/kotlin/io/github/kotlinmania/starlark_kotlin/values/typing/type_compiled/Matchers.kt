// port-lint: source src/values/typing/type_compiled/matchers.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled

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

/** TypeMatcher implementations for runtime type checking. */

import io.github.kotlinmania.starlark_kotlin.values.starlark_type_id.StarlarkTypeId
import io.github.kotlinmania.starlark_kotlin.values.types.dict.dictRefFromValue
import io.github.kotlinmania.starlark_kotlin.values.types.dict.iter
import io.github.kotlinmania.starlark_kotlin.values.types.int.StarlarkIntRef
import io.github.kotlinmania.starlark_kotlin.values.types.list.ListRef
import io.github.kotlinmania.starlark_kotlin.values.types.list.FrozenList
import io.github.kotlinmania.starlark_kotlin.values.types.dict.FrozenDict
import io.github.kotlinmania.starlark_kotlin.values.types.set.FrozenSet
import io.github.kotlinmania.starlark_kotlin.values.types.set.SetRef
import io.github.kotlinmania.starlark_kotlin.values.types.set.content
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.Tuple
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.fromValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsAny;
internal object IsAny : TypeMatcher {
    // impl TypeMatcher for IsAny

    // fn matches(&self, _value: Value) -> bool
    override fun matches(@Suppress("unused") value: Value): Boolean {
        return true
    }

    // fn is_wildcard(&self) -> bool
    override fun isWildcard(): Boolean {
        return true
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsNever;
internal object IsNever : TypeMatcher {
    // impl TypeMatcher for IsNever

    // fn matches(&self, _value: Value) -> bool
    override fun matches(@Suppress("unused") value: Value): Boolean {
        return false
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsStr;
internal object IsStr : TypeMatcher {
    // impl TypeMatcher for IsStr

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.unpackStr() != null
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsList;
internal object IsList : TypeMatcher {
    // impl TypeMatcher for IsList

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of(FrozenList::class)
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsListOf<I: TypeMatcher>(pub(crate) I);
internal class IsListOf(
    val item: TypeMatcher,
) : TypeMatcher {
    // impl<I: TypeMatcher> TypeMatcher for IsListOf<I>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val list = ListRef.fromValue(value) ?: return false
        return list.content().all { v -> item.matches(v) }
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsTupleOf<A: TypeMatcher>(pub(crate) A);
internal class IsTupleOf(
    val elem: TypeMatcher,
) : TypeMatcher {
    // impl<A: TypeMatcher> TypeMatcher for IsTupleOf<A>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val tuple = Tuple.fromValue(value) ?: return false
        return tuple.content().all { v -> elem.matches(v) }
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsTupleElems(pub(crate) Vec<TypeMatcherBox>);
internal class IsTupleElems(
    val elems: List<TypeMatcherBox>,
) : TypeMatcher {
    // impl TypeMatcher for IsTupleElems

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val tuple = Tuple.fromValue(value) ?: return false
        val content = tuple.content()
        if (content.size != elems.size) return false
        return content.zip(elems).all { (v, t) -> t.matches(v) }
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsTupleElems0;
internal object IsTupleElems0 : TypeMatcher {
    // impl TypeMatcher for IsTupleElems0

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val tuple = Tuple.fromValue(value) ?: return false
        return tuple.content().isEmpty()
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsTupleElems1<A: TypeMatcher>(pub(crate) A);
internal class IsTupleElems1(
    val a: TypeMatcher,
) : TypeMatcher {
    // impl<A: TypeMatcher> TypeMatcher for IsTupleElems1<A>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val tuple = Tuple.fromValue(value) ?: return false
        val content = tuple.content()
        if (content.size != 1) return false
        return a.matches(content[0])
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsTupleElems2<A: TypeMatcher, B: TypeMatcher>(pub(crate) A, pub(crate) B);
internal class IsTupleElems2(
    val a: TypeMatcher,
    val b: TypeMatcher,
) : TypeMatcher {
    // impl<A: TypeMatcher, B: TypeMatcher> TypeMatcher for IsTupleElems2<A, B>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val tuple = Tuple.fromValue(value) ?: return false
        val content = tuple.content()
        if (content.size != 2) return false
        return a.matches(content[0]) && b.matches(content[1])
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsDict;
internal object IsDict : TypeMatcher {
    // impl TypeMatcher for IsDict

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of(FrozenDict::class)
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsDictOf<K: TypeMatcher, V: TypeMatcher>(pub(crate) K, pub(crate) V);
internal class IsDictOf(
    val key: TypeMatcher,
    val valueMatcher: TypeMatcher,
) : TypeMatcher {
    // impl<K: TypeMatcher, V: TypeMatcher> TypeMatcher for IsDictOf<K, V>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val dict = dictRefFromValue(value) ?: return false
        return dict.iter().all { pair -> key.matches(pair.first) && valueMatcher.matches(pair.second) }
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsSet;
internal object IsSet : TypeMatcher {
    // impl TypeMatcher for IsSet

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.starlarkTypeId() == StarlarkTypeId.of(FrozenSet::class)
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsSetOf<I: TypeMatcher>(pub(crate) I);
internal class IsSetOf(
    val item: TypeMatcher,
) : TypeMatcher {
    // impl<I: TypeMatcher> TypeMatcher for IsSetOf<I>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val set = SetRef.unpackValueOpt(value) ?: return false
        return set.content.iter().all { v -> item.matches(v) }
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsAnyOfTwo<A: TypeMatcher, B: TypeMatcher>(pub(crate) A, pub(crate) B);
internal class IsAnyOfTwo(
    val a: TypeMatcher,
    val b: TypeMatcher,
) : TypeMatcher {
    // impl<A: TypeMatcher, B: TypeMatcher> TypeMatcher for IsAnyOfTwo<A, B>

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return a.matches(value) || b.matches(value)
    }
}

// #[derive(Clone, Allocative, Debug)]
// pub(crate) struct IsAnyOf(pub(crate) Vec<TypeMatcherBox>);
internal class IsAnyOf(
    val matchers: List<TypeMatcher>,
) : TypeMatcher {
    // impl TypeMatcher for IsAnyOf

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return matchers.any { t -> t.matches(value) }
    }
}

// #[derive(Allocative, Clone, Copy, Dupe, Debug)]
// pub(crate) struct IsCallable;
internal object IsCallable : TypeMatcher {
    // impl TypeMatcher for IsCallable

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.vtable().hasInvoke
    }
}

// #[derive(Allocative, Clone, Copy, Dupe, Debug)]
// pub(crate) struct IsType;
internal object IsType : TypeMatcher {
    // impl TypeMatcher for IsType

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.vtable().hasEvalType
    }
}

// #[derive(Copy, Clone, Dupe, Debug, Allocative)]
// pub(crate) struct IsIterable;
internal object IsIterable : TypeMatcher {
    // impl TypeMatcher for IsIterable

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.vtable().hasIterate
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsInt;
internal object IsInt : TypeMatcher {
    // impl TypeMatcher for IsInt

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return StarlarkIntRef.unpack(value) != null
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsBool;
internal object IsBool : TypeMatcher {
    // impl TypeMatcher for IsBool

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.unpackBool() != null
    }
}

// #[derive(Clone, Copy, Dupe, Allocative, Debug)]
// pub(crate) struct IsNone;
internal object IsNone : TypeMatcher {
    // impl TypeMatcher for IsNone

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        return value.isNone()
    }
}

// #[derive(Allocative, Debug, Clone)]
// pub(crate) struct StarlarkTypeIdMatcher {
//     starlark_type_id: StarlarkTypeIdAligned,
// }
/**
 * Matches a value by its Starlark type id (when available), falling back to name.
 *
 * In Rust, this compares [StarlarkTypeId] values directly. In Kotlin, the typing system can
 * construct [TyStarlarkValue] from just a type name, so the type id may be missing and we
 * fall back to matching by [AValueVTable.typeName].
 */
internal class StarlarkTypeIdMatcher(
    private val expectedTypeId: StarlarkTypeId?,
    private val expectedTypeName: String,
) : TypeMatcher {
    companion object {
        // pub(crate) fn new(ty: TyStarlarkValue) -> StarlarkTypeIdMatcher
        fun new(ty: TyStarlarkValue): StarlarkTypeIdMatcher {
            return StarlarkTypeIdMatcher(
                expectedTypeId = ty.starlarkTypeId(),
                expectedTypeName = ty.asName(),
            )
        }
    }

    // impl TypeMatcher for StarlarkTypeIdMatcher

    // fn matches(&self, value: Value) -> bool
    override fun matches(value: Value): Boolean {
        val expectedId = expectedTypeId
        return if (expectedId != null) {
            value.starlarkTypeId() == expectedId
        } else {
            value.vtable().typeName == expectedTypeName
        }
    }
}
