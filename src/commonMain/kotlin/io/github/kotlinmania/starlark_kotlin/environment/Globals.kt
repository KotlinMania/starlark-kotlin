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

import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableComponents
import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.collections.symbol.map.SymbolMap
import io.github.kotlinmania.starlark_kotlin.docs.DocFunction
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocModule
import io.github.kotlinmania.starlark_kotlin.docs.DocString
import io.github.kotlinmania.starlark_kotlin.docs.DocStringKind
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpecBuilder
import io.github.kotlinmania.starlark_kotlin.stdlib.LibraryExtension
import io.github.kotlinmania.starlark_kotlin.stdlib.standardEnvironment
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.documentation
import io.github.kotlinmania.starlark_kotlin.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.NativeFunc
import io.github.kotlinmania.starlark_kotlin.values.types.NativeFuncFn
import io.github.kotlinmania.starlark_kotlin.values.types.NativeFunction
import io.github.kotlinmania.starlark_kotlin.values.types.SpecialBuiltinFunction
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.FrozenNamespace
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.MaybeDocHiddenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/** Type alias matching Rust: `type GlobalValue = MaybeDocHiddenValue<'static, FrozenValue>` */
internal typealias GlobalValue = MaybeDocHiddenValue<FrozenValue>

/**
 * The global values available during execution.
 *
 * Corresponds to Rust's `Globals(Arc<GlobalsData>)`. In Kotlin, the `Arc` is
 * unnecessary since objects are reference types with garbage collection.
 */
class Globals internal constructor(
    internal val data: GlobalsData,
) {

    companion object {
        /**
         * Create an empty [Globals], with no functions in scope.
         */
        fun new(): Globals {
            return GlobalsBuilder.new().build()
        }

        /**
         * Create a [Globals] following the
         * [Starlark standard](https://github.com/bazelbuild/starlark/blob/master/spec.md#built-in-constants-and-functions).
         */
        fun standard(): Globals {
            return GlobalsBuilder.standard().build()
        }

        /**
         * Create a [Globals] combining those functions in the Starlark standard plus
         * all those defined in [LibraryExtension].
         *
         * This function is public to use in the `starlark` binary,
         * but users of starlark should list the extensions they want explicitly.
         */
        fun extendedInternal(): Globals {
            return GlobalsBuilder.extended().build()
        }

        /** Empty globals. */
        internal val empty: Globals by lazy {
            GlobalsBuilder.new().build()
        }

        /**
         * Create a [Globals] combining those functions in the Starlark standard plus
         * all those given in the [LibraryExtension] arguments.
         */
        fun extendedBy(extensions: List<LibraryExtension>): Globals {
            return GlobalsBuilder.extendedBy(extensions).build()
        }
    }

    /**
     * This function is only safe if you first call [heap] and keep a reference to it.
     * Therefore, don't expose it on the public API.
     */
    internal fun getFrozen(name: String): FrozenValue? {
        return data.variables.getStr(name)?.value
    }

    internal fun getOwned(name: String): OwnedFrozenValue? {
        val v = getFrozen(name) ?: return null
        // Safety: We know the heap this is allocated in.
        // In Kotlin, FrozenHeapRef is already a reference type (no dupe needed).
        return OwnedFrozenValue(heap(), v)
    }

    /** Get all the names defined in this environment. */
    fun names(): Iterator<FrozenStringValue> {
        return data.variableNames.iterator()
    }

    /**
     * Iterate over all the items in this environment.
     * Note returned values are owned by this globals.
     */
    fun iter(): Iterator<Pair<String, FrozenValue>> {
        return data.variables.iter().map { (n, v) -> Pair(n.asStr(), v.value) }.iterator()
    }

    internal fun heap(): FrozenHeapRef {
        return data.heap
    }

    /** Print information about the values in this object. */
    fun describe(): String {
        return data.variables.iter().joinToString("\n") { (name, value) ->
            value.value.toValue().describe(name.asStr())
        }
    }

    /** Get the documentation for the object itself. */
    fun docstring(): String? {
        return data.docstring
    }

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

class GlobalsData(
    val heap: FrozenHeapRef,
    val variables: SymbolMap<GlobalValue>,
    val variableNames: List<FrozenStringValue>,
    val docstring: String?,
)

/**
 * Used to build a [Globals] value.
 *
 * @property docstring The raw docstring for this module.
 *   FIXME(JakobDegen): This should probably be removed. Having a docstring on a `GlobalsBuilder`
 *   doesn't really make sense, because there's no good way to combine multiple docstrings.
 */
class GlobalsBuilder private constructor(
    /** The heap everything is allocated in. */
    private val heap: FrozenHeap,
    /** Normal top-level variables, e.g. True/hash. */
    private val variables: SymbolMap<GlobalValue>,
    /** The list of struct fields, pushed to the end. */
    private val namespaceFields: MutableList<SmallMap<FrozenStringValue, GlobalValue>>,
    /** The raw docstring for this module. */
    internal var docstring: String?,
) {
    companion object {
        /** Create an empty [GlobalsBuilder], with no functions in scope. */
        fun new(): GlobalsBuilder {
            return GlobalsBuilder(
                heap = FrozenHeap.new(),
                variables = SymbolMap(),
                namespaceFields = mutableListOf(),
                docstring = null,
            )
        }

        /**
         * Create a [GlobalsBuilder] following the
         * [Starlark standard](https://github.com/bazelbuild/starlark/blob/master/spec.md#built-in-constants-and-functions).
         */
        fun standard(): GlobalsBuilder {
            return standardEnvironment()
        }

        /**
         * Create a [GlobalsBuilder] combining those functions in the Starlark standard plus
         * all those defined in [LibraryExtension].
         */
        internal fun extended(): GlobalsBuilder {
            return extendedBy(LibraryExtension.all())
        }

        /**
         * Create a [GlobalsBuilder] combining those functions in the Starlark standard plus
         * all those defined in [LibraryExtension].
         */
        fun extendedBy(extensions: List<LibraryExtension>): GlobalsBuilder {
            val res = standard()
            for (x in extensions) {
                x.add(res)
            }
            return res
        }
    }

    /**
     * Add a nested namespace to the builder. If [f] adds the definition `foo`,
     * it will end up on a namespace [name], accessible as `name.foo`.
     */
    fun namespace(name: String, f: (GlobalsBuilder) -> Unit) {
        namespaceInner(name, false, f)
    }

    /** Same as [namespace], but this value will not show up in generated documentation. */
    fun namespaceNoDocs(name: String, f: (GlobalsBuilder) -> Unit) {
        namespaceInner(name, true, f)
    }

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

    /** A fluent API for modifying [GlobalsBuilder] and returning the result. */
    fun with(f: (GlobalsBuilder) -> Unit): GlobalsBuilder {
        f(this)
        return this
    }

    /** A fluent API for modifying [GlobalsBuilder] using [namespace]. */
    fun withNamespace(name: String, f: (GlobalsBuilder) -> Unit): GlobalsBuilder {
        namespace(name, f)
        return this
    }

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

    /** Set a value in the [GlobalsBuilder]. */
    fun set(name: String, value: AllocFrozenValue) {
        val frozenValue = value.allocFrozenValue(heap)
        setInner(name, frozenValue, false)
    }

    internal fun setInner(name: String, value: FrozenValue, docHidden: Boolean) {
        val globalValue = MaybeDocHiddenValue(
            value = value,
            docHidden = docHidden,
        )
        val lastNamespace = namespaceFields.lastOrNull()
        when (lastNamespace) {
            null -> {
                variables.insert(name, globalValue)
            }
            else -> {
                val frozenName = heap.allocStr(name)
                lastNamespace.insert(frozenName, globalValue)
            }
        }
    }

    /**
     * Set a method. This function is usually called from code
     * generated by `starlark_derive` and rarely needs to be called manually.
     */
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
                ).getOrThrow(),
                docs = components.intoDocs(asType),
                specialBuiltinFunction = specialBuiltinFunction,
            ),
        )
    }

    /**
     * Convenience overload: register a simple function by name with a lambda.
     * The lambda receives (Arguments, Evaluator) and the result is auto-wrapped.
     */
    fun setFunction(
        name: String,
        speculativeExecSafe: Boolean = false,
        f: (io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments,
            io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator) -> Any?,
    ) {
        val sig = io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
            .withCapacity<FrozenValue>(name).finish()
        val nativeFn: NativeFuncFn = { eval, _, args ->
            @Suppress("UNCHECKED_CAST")
            val result = f(args, eval)
            when (result) {
                is io.github.kotlinmania.starlark_kotlin.values.layout.Value ->
                    kotlin.Result.success(result)
                is kotlin.Result<*> ->
                    result as kotlin.Result<io.github.kotlinmania.starlark_kotlin.values.layout.Value>
                else ->
                    kotlin.Result.success(io.github.kotlinmania.starlark_kotlin.values.layout.Value.newNone())
            }
        }
        set(
            name,
            NativeFunction(
                function = NativeFunc(nativeFn, sig),
                name = name,
                speculativeExecSafe = speculativeExecSafe,
                asType = null,
                ty = Ty.any(),
                docs = DocItem.Member(DocMember.Function(DocFunction())),
                specialBuiltinFunction = null,
            ),
        )
    }

    /** Heap where globals are allocated. Can be used to allocate additional values. */
    fun frozenHeap(): FrozenHeap {
        return heap
    }

    /**
     * Allocate a value using the same underlying heap as the [GlobalsBuilder],
     * only intended for values that are referred to by those which are passed
     * to [set].
     */
    fun alloc(value: AllocFrozenValue): FrozenValue {
        return value.allocFrozenValue(heap)
    }

    /**
     * Set per module docstring.
     *
     * This function is called by the `starlark_derive` generated code
     * and rarely needs to be called manually.
     */
    fun setDocstring(docstring: String) {
        this.docstring = docstring
    }
}

/**
 * Used to create globals.
 *
 * Corresponds to Rust's `GlobalsStatic(OnceCell<Globals>)`.
 * Uses a synchronized lazy pattern for thread-safe initialization.
 */
class GlobalsStatic {

    /** Create a new [GlobalsStatic]. */
    constructor()

    @Volatile
    private var cachedGlobals: Globals? = null

    private fun globals(x: (GlobalsBuilder) -> Unit): Globals {
        cachedGlobals?.let { return it }
        val built = GlobalsBuilder.new().with(x).build()
        cachedGlobals = built
        return built
    }

    /**
     * Get a function out of the object. Requires that the function passed only set a single
     * value. If populated via a `#[starlark_module]`, that means a single function in it.
     */
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

    /**
     * Move all the globals in this [GlobalsStatic] into a new one. All variables will
     * only be allocated once (ensuring things like function comparison works properly).
     */
    fun populate(x: (GlobalsBuilder) -> Unit, out: GlobalsBuilder) {
        val g = globals(x)
        for ((name, value) in g.data.variables.iter()) {
            out.setInner(name.asStr(), value.value, value.docHidden)
        }
        out.docstring = g.data.docstring
    }
}

fun commonDocumentation(
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
