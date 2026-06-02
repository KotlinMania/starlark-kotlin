// port-lint: source src/docs.rs
package io.github.kotlinmania.starlark.docs

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

/** Types supporting documentation for code written in or for Starlark. */

import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.eval.runtime.params.FmtParam
import io.github.kotlinmania.starlark.eval.runtime.params.iterFmtParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.StarlarkValue
import kotlin.js.JsName

/** The documentation provided by a user for a specific module, object, function, etc. */
data class DocString(
    /** The first line of a doc string. This has whitespace trimmed from it. */
    val summary: String = "",
    /**
     * The contents of a doc string that follow the summary, and a single blank line.
     * This also has whitespace trimmed from it, and it is dedented.
     */
    val details: String? = null,
    /** Examples provided as a part of the doc string. It's separated by a 'Examples:' string */
    val examples: String? = null,
) {
    companion object
}

/**
 * The documentation for a module/namespace.
 *
 * See the docs on [DocType] for the distinction between that type and this one.
 */
class DocModule(
    val docs: DocString? = null,
    val members: SmallMap<String, DocItem> = SmallMap.new(),
) {
    // pub fn filter<P>(self, mut predicate: P) -> Self
    fun filter(predicate: (Pair<String, DocItem>) -> Boolean): DocModule {
        val filtered = SmallMap.new<String, DocItem>()
        for ((k, v) in members) {
            if (predicate(Pair(k, v))) {
                filtered.insert(k, v)
            }
        }
        return DocModule(docs = docs, members = filtered)
    }
}

/** Documents a single function. */
class DocFunction(
    /**
     * Documentation for the function. If parsed, this should generally be the first statement
     * of a function's body if that statement is a string literal. Any sections like "Args:",
     * "Returns", etc are kept intact. It is up to the consumer to remove these sections if
     * they are present.
     */
    val docs: DocString? = null,
    /**
     * The parameters that this function takes. Docs for these parameters should generally be
     * extracted from the main docstring's details, but may be extracted from the definition if the
     * docstring is not present.
     */
    val params: DocParams = DocParams(),
    /** Details about what this function returns. */
    val ret: DocReturn = DocReturn(),
) {
    companion object

    /** Used by LSP. Return starred name and the doc. */
    fun findParamWithName(paramName: String): Pair<String, DocParam>? =
        params
            .docParamsWithStarredNames()
            .firstOrNull { (_, p) -> p.name == paramName }
}

/** Function parameters. */
data class DocParams(
    val posOnly: List<DocParam> = emptyList(),
    val posOrNamed: List<DocParam> = emptyList(),
    val args: DocParam? = null,
    val namedOnly: List<DocParam> = emptyList(),
    val kwargs: DocParam? = null,
) {
    /** Iterate parameters ignoring information about positional-only, named-only. */
    internal fun docParams(): Sequence<DocParam> =
        sequence {
            yieldAll(posOnly)
            yieldAll(posOrNamed)
            if (args != null) yield(args)
            yieldAll(namedOnly)
            if (kwargs != null) yield(kwargs)
        }

    internal fun docParamsWithStarredNames(): Sequence<Pair<String, DocParam>> =
        sequence {
            for (p in posOnly) yield(Pair(p.name, p))
            for (p in posOrNamed) yield(Pair(p.name, p))
            if (args != null) yield(Pair("*${args.name}", args))
            for (p in namedOnly) yield(Pair(p.name, p))
            if (kwargs != null) yield(Pair("**${kwargs.name}", kwargs))
        }

    /** Mutable iteration over parameters. */
    internal fun docParamsMut(): Iterator<DocParam> = docParams().iterator()

    /** Non-star parameters. */
    fun regularParams(): Sequence<DocParam> =
        sequence {
            yieldAll(posOnly)
            yieldAll(posOrNamed)
            yieldAll(namedOnly)
        }

    /** Iterate params with `/` and `*` markers to output function signature. */
    fun fmtParams(): Sequence<FmtParam<DocParam>> =
        iterFmtParamSpec(
            posOnly,
            posOrNamed,
            args,
            namedOnly,
            kwargs,
        )
}

/** A single parameter of a function. */
data class DocParam(
    /** Does not include `*` or `**`. */
    val name: String,
    var docs: DocString? = null,
    /** Element type for `*args` and value type for `**kwargs`. */
    val typ: Ty,
    var defaultValue: String? = null,
) {
    /** Get the underlying [DocString] for this item, if it exists. */
    fun getDocString(): DocString? = docs

    /** Get the summary of the underlying [DocString] for this item, if it exists. */
    fun getDocSummary(): String? = getDocString()?.summary
}

/** Details about the return value of a function. */
class DocReturn(
    /** Extra semantic details around the returned value's meaning. */
    val docs: DocString? = null,
    val typ: Ty = Ty.any(),
)

/** A single property of an object. These are explicitly not functions (see [DocMember]). */
class DocProperty(
    val docs: DocString? = null,
    val typ: Ty,
)

/** A named member of an object. */
sealed class DocMember {
    class Property(
        val property: DocProperty,
    ) : DocMember()

    class Function(
        val function: DocFunction,
    ) : DocMember()

    /** Get the underlying [DocString] for this item, if it exists. */
    fun getDocString(): DocString? =
        when (this) {
            is Function -> function.docs
            is Property -> property.docs
        }

    /** Get the summary of the underlying [DocString] for this item, if it exists. */
    fun getDocSummary(): String? = getDocString()?.summary
}

/**
 * The documentation for a type.
 *
 * This is distinct from a module since, well, types and modules are different things, but more
 * importantly because the members here are expected to be attributes on *values* of the type, not
 * on the type itself.
 */
class DocType(
    val docs: DocString? = null,
    /** Name and details of each attr/function that can be accessed on this type. */
    val members: SmallMap<String, DocMember> = SmallMap.new(),
    val ty: Ty,
    @JsName("constructor_")
    val constructor: DocFunction? = null,
) {
    companion object {
        fun <T : StarlarkValue> fromStarlarkValue(value: T): DocType {
            val ty = value.getTypeStarlarkRepr()
            val methods = value.getMethods()
            return if (methods != null) {
                methods.documentation(ty)
            } else {
                DocType(
                    docs = null,
                    members = SmallMap.new(),
                    ty = ty,
                    constructor = null,
                )
            }
        }
    }
}

sealed class DocItem {
    class Module(
        val module: DocModule,
    ) : DocItem()

    class Type(
        val type: DocType,
    ) : DocItem()

    class Member(
        val member: DocMember,
    ) : DocItem()

    /** Get the underlying [DocString] for this item, if it exists. */
    fun getDocString(): DocString? =
        when (this) {
            is Module -> module.docs
            is Type -> type.docs
            is Member -> member.getDocString()
        }

    /** Get the summary of the underlying [DocString] for this item, if it exists. */
    fun getDocSummary(): String? = getDocString()?.summary

    /**
     * Converts to a doc member, if possible.
     *
     * This conversion is trivial, except in the case of objects - those are flattened into a
     * single property that just indicates their type.
     */
    fun tryAsMemberWithCollapsedObject(): Result<DocMember> =
        when (this) {
            is Module -> Result.failure(IllegalStateException("Cannot collapse module to member"))
            is Member -> Result.success(member)
            is Type ->
                Result.success(
                    DocMember.Property(
                        DocProperty(
                            docs = type.docs,
                            typ = type.ty,
                        ),
                    ),
                )
        }

    fun tryAsMember(): DocMember? =
        when (this) {
            is Member -> member
            else -> null
        }
}
