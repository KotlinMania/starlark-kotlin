// port-lint: source src/docs.rs
package io.github.kotlinmania.starlark_kotlin.docs

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

/// Types supporting documentation for code written in or for Starlark.

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.display.FmtParam
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.display.iterFmtParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue

/// The documentation provided by a user for a specific module, object, function, etc.
// #[derive(Debug, Clone, PartialEq, Trace, Default, Allocative)]
// pub struct DocString
class DocString(
    /// The first line of a doc string. This has whitespace trimmed from it.
    val summary: String = "",
    /// The contents of a doc string that follow the summary, and a single blank line.
    /// This also has whitespace trimmed from it, and it is dedented.
    val details: String? = null,
    /// Examples provided as a part of the doc string. It's separated by a 'Examples:' string
    val examples: String? = null,
)

/// The documentation for a module/namespace.
///
/// See the docs on [DocType] for the distinction between that type and this one.
// #[derive(Debug, Clone, PartialEq, Default, Allocative)]
// pub struct DocModule
class DocModule(
    val docs: DocString? = null,
    val members: SmallMap<String, DocItem> = SmallMap(),
) {
    // pub fn filter<P>(self, mut predicate: P) -> Self
    fun filter(predicate: (Pair<String, DocItem>) -> Boolean): DocModule {
        val filtered = SmallMap<String, DocItem>()
        for ((k, v) in members) {
            if (predicate(Pair(k, v))) {
                filtered[k] = v
            }
        }
        return DocModule(docs = docs, members = filtered)
    }
}

/// Documents a single function.
// #[derive(Debug, Clone, PartialEq, Default, Allocative)]
// pub struct DocFunction
class DocFunction(
    /// Documentation for the function. If parsed, this should generally be the first statement
    /// of a function's body if that statement is a string literal. Any sections like "Args:",
    /// "Returns", etc are kept intact. It is up to the consumer to remove these sections if
    /// they are present.
    val docs: DocString? = null,
    /// The parameters that this function takes. Docs for these parameters should generally be
    /// extracted from the main docstring's details, but may be extracted from the definition if the
    /// docstring is not present.
    val params: DocParams = DocParams(),
    /// Details about what this function returns.
    val ret: DocReturn = DocReturn(),
) {
    /// Used by LSP. Return starred name and the doc.
    // pub fn find_param_with_name(&self, param_name: &str) -> Option<(String, &DocParam)>
    fun findParamWithName(paramName: String): Pair<String, DocParam>? {
        return params.docParamsWithStarredNames()
            .firstOrNull { (_, p) -> p.name == paramName }
    }
}

/// Function parameters.
// #[derive(Debug, Clone, PartialEq, Default, Allocative)]
// pub struct DocParams
class DocParams(
    val posOnly: List<DocParam> = emptyList(),
    val posOrNamed: List<DocParam> = emptyList(),
    val args: DocParam? = null,
    val namedOnly: List<DocParam> = emptyList(),
    val kwargs: DocParam? = null,
) {
    /// Iterate parameters ignoring information about positional-only, named-only.
    // pub(crate) fn doc_params(&self) -> impl Iterator<Item = &DocParam>
    internal fun docParams(): Sequence<DocParam> {
        return sequence {
            yieldAll(posOnly)
            yieldAll(posOrNamed)
            if (args != null) yield(args)
            yieldAll(namedOnly)
            if (kwargs != null) yield(kwargs)
        }
    }

    // pub(crate) fn doc_params_with_starred_names(&self) -> impl Iterator<Item = (String, &DocParam)>
    internal fun docParamsWithStarredNames(): Sequence<Pair<String, DocParam>> {
        return sequence {
            for (p in posOnly) yield(Pair(p.name, p))
            for (p in posOrNamed) yield(Pair(p.name, p))
            if (args != null) yield(Pair("*${args.name}", args))
            for (p in namedOnly) yield(Pair(p.name, p))
            if (kwargs != null) yield(Pair("**${kwargs.name}", kwargs))
        }
    }

    /// Non-star parameters.
    // pub fn regular_params(&self) -> impl Iterator<Item = &DocParam>
    fun regularParams(): Sequence<DocParam> {
        return sequence {
            yieldAll(posOnly)
            yieldAll(posOrNamed)
            yieldAll(namedOnly)
        }
    }

    /// Iterate params with `/` and `*` markers to output function signature.
    // pub fn fmt_params(&self) -> impl Iterator<Item = FmtParam<&'_ DocParam>>
    fun fmtParams(): Sequence<FmtParam<DocParam>> {
        return iterFmtParamSpec(
            posOnly,
            posOrNamed,
            args,
            namedOnly,
            kwargs,
        )
    }
}

/// A single parameter of a function.
// #[derive(Debug, Clone, PartialEq, Allocative)]
// pub struct DocParam
class DocParam(
    /// Does not include `*` or `**`.
    val name: String,
    val docs: DocString? = null,
    /// Element type for `*args` and value type for `**kwargs`.
    val typ: Ty,
    val defaultValue: String? = null,
) {
    /// Get the underlying [DocString] for this item, if it exists.
    // pub fn get_doc_string(&self) -> Option<&DocString>
    fun getDocString(): DocString? {
        return docs
    }

    /// Get the summary of the underlying [DocString] for this item, if it exists.
    // pub fn get_doc_summary(&self) -> Option<&str>
    fun getDocSummary(): String? {
        return getDocString()?.summary
    }
}

/// Details about the return value of a function.
// #[derive(Debug, Clone, PartialEq, Allocative)]
// pub struct DocReturn
class DocReturn(
    /// Extra semantic details around the returned value's meaning.
    val docs: DocString? = null,
    val typ: Ty = Ty.any(),
)

/// A single property of an object. These are explicitly not functions (see [DocMember]).
// #[derive(Debug, Clone, PartialEq, Allocative)]
// pub struct DocProperty
class DocProperty(
    val docs: DocString? = null,
    val typ: Ty,
)

/// A named member of an object.
// pub enum DocMember
sealed class DocMember {
    // Property(DocProperty)
    class Property(val property: DocProperty) : DocMember()
    // Function(DocFunction)
    class Function(val function: DocFunction) : DocMember()

    /// Get the underlying [DocString] for this item, if it exists.
    // pub fn get_doc_string(&self) -> Option<&DocString>
    fun getDocString(): DocString? {
        return when (this) {
            is Function -> function.docs
            is Property -> property.docs
        }
    }

    /// Get the summary of the underlying [DocString] for this item, if it exists.
    // pub fn get_doc_summary(&self) -> Option<&str>
    fun getDocSummary(): String? {
        return getDocString()?.summary
    }
}

/// The documentation for a type.
///
/// This is distinct from a module since, well, types and modules are different things, but more
/// importantly because the members here are expected to be attributes on *values* of the type, not
/// on the type itself.
// #[derive(Debug, Clone, PartialEq, Allocative)]
// pub struct DocType
class DocType(
    val docs: DocString? = null,
    /// Name and details of each attr/function that can be accessed on this type.
    val members: SmallMap<String, DocMember> = SmallMap(),
    val ty: Ty,
    val constructor: DocFunction? = null,
) {
    companion object {
        // pub fn from_starlark_value<T: StarlarkValue<'static>>() -> DocType
        fun <T : StarlarkValue> fromStarlarkValue(value: T): DocType {
            val ty = value.starlarkTypeRepr()
            val methods = value.getMethods()
            return if (methods != null) {
                methods.documentation(ty)
            } else {
                DocType(
                    docs = null,
                    members = SmallMap(),
                    ty = ty,
                    constructor = null,
                )
            }
        }
    }
}

// pub enum DocItem
sealed class DocItem {
    // Module(DocModule)
    class Module(val module: DocModule) : DocItem()
    // Type(DocType)
    class Type(val type: DocType) : DocItem()
    // Member(DocMember)
    class Member(val member: DocMember) : DocItem()

    /// Get the underlying [DocString] for this item, if it exists.
    // pub fn get_doc_string(&self) -> Option<&DocString>
    fun getDocString(): DocString? {
        return when (this) {
            is Module -> module.docs
            is Type -> type.docs
            is Member -> member.getDocString()
        }
    }

    /// Get the summary of the underlying [DocString] for this item, if it exists.
    // pub fn get_doc_summary(&self) -> Option<&str>
    fun getDocSummary(): String? {
        return getDocString()?.summary
    }

    /// Converts to a doc member, if possible.
    ///
    /// This conversion is trivial, except in the case of objects - those are flattened into a
    /// single property that just indicates their type.
    // pub fn try_as_member_with_collapsed_object(&self) -> Result<DocMember, &DocModule>
    fun tryAsMemberWithCollapsedObject(): Result<DocMember> {
        return when (this) {
            is Module -> Result.failure(IllegalStateException("Cannot collapse module to member"))
            is Member -> Result.success(member)
            is Type -> Result.success(
                DocMember.Property(
                    DocProperty(
                        docs = type.docs,
                        typ = type.ty,
                    )
                )
            )
        }
    }

    // pub fn try_as_member(&self) -> Option<DocMember>
    fun tryAsMember(): DocMember? {
        return when (this) {
            is Member -> member
            else -> null
        }
    }
}
