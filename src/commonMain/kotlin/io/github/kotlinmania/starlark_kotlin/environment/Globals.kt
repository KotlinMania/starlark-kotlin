// port-lint: source src/environment/globals.rs
package io.github.kotlinmania.starlark_kotlin.environment

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

import io.github.kotlinmania.starlark_kotlin.__derive_refs.components.NativeCallableComponents
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.symbol.map.SymbolMap
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocModule
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.eval.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.stdlib.LibraryExtension
import io.github.kotlinmania.starlark_kotlin.stdlib.standardEnvironment
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.function.NativeFunc
import io.github.kotlinmania.starlark_kotlin.values.function.NativeFuncFn
import io.github.kotlinmania.starlark_kotlin.values.function.SpecialBuiltinFunction
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.FrozenNamespace
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.MaybeDocHiddenValue
import io.github.kotlinmania.starlark_kotlin.values.types.function.NativeFunction

// type GlobalValue = MaybeDocHiddenValue<'static, FrozenValue>;
internal typealias GlobalValue = MaybeDocHiddenValue<FrozenValue>

/// The global values available during execution.
// #[derive(Clone, Dupe, Debug, Allocative)]
// pub struct Globals(Arc<GlobalsData>);
class Globals internal constructor(
    internal val data: GlobalsData,
) {
    // impl Globals

    companion object {
        // pub fn new() -> Self
        /** Create an empty [Globals], with no functions in scope. */
        fun new(): Globals {
            return GlobalsBuilder.new().build()
        }

        // pub fn standard() -> Self
        /** Create a [Globals] following the Starlark standard. */
        fun standard(): Globals {
            return GlobalsBuilder.standard().build()
        }

        // #[doc(hidden)]
        // pub fn extended_internal() -> Self
        /** Create a [Globals] combining those functions in the Starlark standard plus all those defined in [LibraryExtension]. */
        fun extendedInternal(): Globals {
            return GlobalsBuilder.extended().build()
        }

        // pub(crate) fn empty() -> &'static Globals
        /** Empty globals. */
        internal val empty: Globals by lazy {
            GlobalsBuilder.new().build()
        }

        // pub fn extended_by(extensions: &[LibraryExtension]) -> Self
        /** Create a [Globals] combining standard functions plus all given in [LibraryExtension] arguments. */
        fun extendedBy(extensions: List<LibraryExtension>): Globals {
            return GlobalsBuilder.extendedBy(extensions).build()
        }
    }

    // #[cfg(test)]
    // pub(crate) fn get<'v>(&'v self, name: &str) -> Option<crate::values::Value<'v>>
    // Test-only method, not included in main source per rules.

    // pub(crate) fn get_frozen(&self, name: &str) -> Option<FrozenValue>
    /** Get a frozen value by name. */
    internal fun getFrozen(name: String): FrozenValue? {
        return data.variables.getStr(name)?.value
    }

    // pub(crate) fn get_owned(&self, name: &str) -> Option<OwnedFrozenValue>
    internal fun getOwned(name: String): OwnedFrozenValue? {
        val v = getFrozen(name) ?: return null
        // SAFETY: We know the heap this is allocated in
        return OwnedFrozenValue.new(heap().dupe(), v)
    }

    // pub fn names(&self) -> impl Iterator<Item = FrozenStringValue> + '_
    /** Get all the names defined in this environment. */
    fun names(): Iterator<FrozenStringValue> {
        return data.variableNames.iterator()
    }

    // pub fn iter(&self) -> impl Iterator<Item = (&str, FrozenValue)>
    /** Iterate over all the items in this environment. Note returned values are owned by this globals. */
    fun iter(): Iterator<Pair<String, FrozenValue>> {
        return data.variables.iter().map { (n, v) -> Pair(n.asStr(), v.value) }.iterator()
    }

    // pub(crate) fn heap(&self) -> &FrozenHeapRef
    internal fun heap(): FrozenHeapRef {
        return data.heap
    }

    // pub fn describe(&self) -> String
    /** Print information about the values in this object. */
    fun describe(): String {
        return data.variables.iter().joinToString("\n") { (name, value) ->
            value.value.toValue().describe(name.asStr())
        }
    }

    // pub fn docstring(&self) -> Option<&str>
    /** Get the documentation for the object itself. */
    fun docstring(): String? {
        return data.docstring
    }

    // pub fn documentation(&self) -> DocModule
    /** Get the documentation for both the object itself, and its members. */
    fun documentation(): DocModule {
        val (docs, members) = commonDocumentation(
            data.docstring,
            data.variables.iter()
                .filter { (_, v) -> !v.docHidden }
                .map { (n, v) -> Pair(n.asStr(), v.value) },
        )
        return DocModule(
            docs = docs,
            members = members.toMap(),
        )
    }
}

// #[derive(Debug, Allocative)]
// struct GlobalsData
internal class GlobalsData(
    val heap: FrozenHeapRef,
    val variables: SymbolMap<GlobalValue>,
    val variableNames: List<FrozenStringValue>,
    val docstring: String?,
)

/// Used to build a [`Globals`] value.
// #[derive(Debug)]
// pub struct GlobalsBuilder
class GlobalsBuilder private constructor(
    // The heap everything is allocated in
    private val heap: FrozenHeap,
    // Normal top-level variables, e.g. True/hash
    private val variables: SymbolMap<GlobalValue>,
    // The list of struct fields, pushed to the end
    private val namespaceFields: MutableList<SmallMap<FrozenStringValue, GlobalValue>>,
    // The raw docstring for this module
    internal var docstring: String?,
) {
    companion object {
        // pub fn new() -> Self
        /** Create an empty [GlobalsBuilder], with no functions in scope. */
        fun new(): GlobalsBuilder {
            return GlobalsBuilder(
                heap = FrozenHeap.new(),
                variables = SymbolMap.new(),
                namespaceFields = mutableListOf(),
                docstring = null,
            )
        }

        // pub fn standard() -> Self
        /** Create a [GlobalsBuilder] following the Starlark standard. */
        fun standard(): GlobalsBuilder {
            return standardEnvironment()
        }

        // pub(crate) fn extended() -> Self
        internal fun extended(): GlobalsBuilder {
            return extendedBy(LibraryExtension.all())
        }

        // pub fn extended_by(extensions: &[LibraryExtension]) -> Self
        /** Create a [GlobalsBuilder] combining standard functions plus all given in [LibraryExtension] arguments. */
        fun extendedBy(extensions: List<LibraryExtension>): GlobalsBuilder {
            val res = standard()
            for (x in extensions) {
                x.add(res)
            }
            return res
        }
    }

    // pub fn namespace(&mut self, name: &str, f: impl FnOnce(&mut GlobalsBuilder))
    /** Add a nested namespace to the builder. */
    fun namespace(name: String, f: (GlobalsBuilder) -> Unit) {
        namespaceInner(name, false, f)
    }

    // pub fn namespace_no_docs(&mut self, name: &str, f: impl FnOnce(&mut GlobalsBuilder))
    /** Same as [namespace], but this value will not show up in generated documentation. */
    fun namespaceNoDocs(name: String, f: (GlobalsBuilder) -> Unit) {
        namespaceInner(name, true, f)
    }

    // fn namespace_inner(&mut self, name: &str, doc_hidden: bool, f: impl FnOnce(&mut GlobalsBuilder))
    private fun namespaceInner(name: String, docHidden: Boolean, f: (GlobalsBuilder) -> Unit) {
        namespaceFields.add(SmallMap.new())
        f(this)
        val fields = namespaceFields.removeLast()
        setInner(
            name,
            heap.alloc(FrozenNamespace.new(fields)),
            docHidden,
        )
    }

    // pub fn with(mut self, f: impl FnOnce(&mut Self)) -> Self
    /** A fluent API for modifying [GlobalsBuilder] and returning the result. */
    fun with(f: (GlobalsBuilder) -> Unit): GlobalsBuilder {
        f(this)
        return this
    }

    // pub fn with_namespace(mut self, name: &str, f: impl Fn(&mut GlobalsBuilder)) -> Self
    /** A fluent API for modifying [GlobalsBuilder] using [namespace]. */
    fun withNamespace(name: String, f: (GlobalsBuilder) -> Unit): GlobalsBuilder {
        namespace(name, f)
        return this
    }

    // pub fn build(self) -> Globals
    /** Called at the end to build a [Globals]. */
    fun build(): Globals {
        val variableNames: MutableList<FrozenStringValue> = variables.keys()
            .map { x -> heap.allocStrIntern(x.asStr()) }
            .toMutableList()
        variableNames.sort()
        return Globals(
            GlobalsData(
                heap = heap.intoRef(),
                variables = variables,
                variableNames = variableNames,
                docstring = docstring,
            )
        )
    }

    // pub fn set<'v, V: AllocFrozenValue>(&'v mut self, name: &str, value: V)
    /** Set a value in the [GlobalsBuilder]. */
    fun set(name: String, value: AllocFrozenValue) {
        val frozenValue = value.allocFrozenValue(heap)
        setInner(name, frozenValue, false)
    }

    // fn set_inner<'v>(&'v mut self, name: &str, value: FrozenValue, doc_hidden: bool)
    internal fun setInner(name: String, value: FrozenValue, docHidden: Boolean) {
        val globalValue = MaybeDocHiddenValue(
            value = value,
            docHidden = docHidden,
        )
        val lastNamespace = namespaceFields.lastOrNull()
        when (lastNamespace) {
            null -> {
                // TODO(nga): do not quietly ignore redefinitions.
                variables.insert(name, globalValue)
            }
            else -> {
                val frozenName = heap.allocStr(name)
                lastNamespace.insert(frozenName, globalValue)
            }
        }
    }

    // pub fn set_function(&mut self, name: &str, ...)
    /** Set a method. This function is usually called from code generated by starlark_derive. */
    fun setFunction(
        name: String,
        components: NativeCallableComponents,
        sig: ParametersSpec<FrozenValue>,
        asType: Pair<Ty, DocType>?,
        ty: Ty?,
        specialBuiltinFunction: SpecialBuiltinFunction?,
        f: NativeFuncFn,
    ) {
        set(
            name,
            NativeFunction(
                function = NativeFunc(f, sig),
                name = name,
                speculativeExecSafe = components.speculativeExecSafe,
                asType = asType?.first,
                ty = ty ?: Ty.fromNativeCallableComponents(
                    components,
                    asType?.first,
                ).getOrThrow(), // TODO(nga): do not unwrap.
                docs = components.intoDocs(asType),
                specialBuiltinFunction = specialBuiltinFunction,
            ),
        )
    }

    // pub fn frozen_heap(&self) -> &FrozenHeap
    /** Heap where globals are allocated. Can be used to allocate additional values. */
    fun frozenHeap(): FrozenHeap {
        return heap
    }

    // pub fn alloc<'v, V: AllocFrozenValue>(&'v self, value: V) -> FrozenValue
    /** Allocate a value using the same underlying heap as the [GlobalsBuilder]. */
    fun alloc(value: AllocFrozenValue): FrozenValue {
        return value.allocFrozenValue(heap)
    }

    // pub fn set_docstring(&mut self, docstring: &str)
    /** Set per module docstring. */
    fun setDocstring(docstring: String) {
        this.docstring = docstring
    }
}

/// Used to create globals.
// pub struct GlobalsStatic(OnceCell<Globals>);
class GlobalsStatic {
    // Kotlin: OnceCell<Globals> equivalent via lazy + volatile
    @Volatile
    private var globals: Globals? = null

    // impl GlobalsStatic

    // pub const fn new() -> Self
    /** Create a new [GlobalsStatic]. */
    constructor()

    // fn globals(&'static self, x: impl FnOnce(&mut GlobalsBuilder)) -> &'static Globals
    private fun globals(x: (GlobalsBuilder) -> Unit): Globals {
        globals?.let { return it }
        val built = GlobalsBuilder.new().with(x).build()
        globals = built
        return built
    }

    // pub fn function(&'static self, x: impl FnOnce(&mut GlobalsBuilder)) -> FrozenValue
    /** Get a function out of the object. Requires that the function passed only set a single value. */
    fun function(x: (GlobalsBuilder) -> Unit): FrozenValue {
        val g = globals(x)
        val namesList = g.names().asSequence().toList()
        check(namesList.size == 1) {
            "GlobalsBuilder.function must have exactly 1 member, you had ${
                namesList.joinToString(", ") { "`${it.asStr()}`" }
            }"
        }
        return g.iter().next().second
    }

    // pub fn populate(&'static self, x: impl FnOnce(&mut GlobalsBuilder), out: &mut GlobalsBuilder)
    /** Move all the globals in this [GlobalsBuilder] into a new one. */
    fun populate(x: (GlobalsBuilder) -> Unit, out: GlobalsBuilder) {
        val g = globals(x)
        for ((name, value) in g.data.variables.iter()) {
            out.setInner(name.asStr(), value.value, value.docHidden)
        }
        out.docstring = g.data.docstring
    }
}

// pub(crate) fn common_documentation<'a, T: IntoIterator<Item = (&'a str, FrozenValue)>>(...)
internal fun commonDocumentation(
    docstring: String?,
    members: Iterable<Pair<String, FrozenValue>>,
): Pair<DocString?, Map<String, DocItem>> {
    val mainDocs = docstring?.let { ds ->
        DocString.fromDocstring(DocStringKind.Rust, ds)
    }
    val memberDocs = members
        .map { (name, value) -> Pair(name, value.toValue().documentation()) }
        .sortedBy { (name, _) -> name }
        .toMap()

    return Pair(mainDocs, memberDocs)
}
