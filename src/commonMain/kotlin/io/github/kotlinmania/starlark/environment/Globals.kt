// port-lint: source environment/globals.rs
package io.github.kotlinmania.starlark.environment

/*
 * Copyright 2018 The Starlark in Rust Authors.
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

import io.github.kotlinmania.starlark.LibraryExtension
import io.github.kotlinmania.starlark.deriverefs.NativeCallableComponents
import io.github.kotlinmania.starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.collections.symbol.map.SymbolMap
import io.github.kotlinmania.starlark.docs.DocFunction
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocModule
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocStringKind
import io.github.kotlinmania.starlark.docs.fromDocstring
import io.github.kotlinmania.starlark.docs.DocType
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpecBuilder
import io.github.kotlinmania.starlark.standardEnvironment
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark.values.types.NativeFunc
import io.github.kotlinmania.starlark.values.types.NativeFuncFn
import io.github.kotlinmania.starlark.values.types.NativeFunction
import io.github.kotlinmania.starlark.values.types.SpecialBuiltinFunction
import io.github.kotlinmania.starlark.values.types.namespace.MaybeDocHiddenValue
import io.github.kotlinmania.starlark.values.types.namespace.NamespaceGen
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStr
import io.github.kotlinmania.starlark.values.types.bigint.allocFrozenValue
import kotlin.concurrent.Volatile


/**
 * The global values available during execution.
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
         * This function is public to import in the `starlark` binary,
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
            members = members,
        )
    }
}

class GlobalsData(
    val heap: FrozenHeapRef,
    val variables: SymbolMap<MaybeDocHiddenValue<FrozenValue>>,
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
    private val variables: SymbolMap<MaybeDocHiddenValue<FrozenValue>>,
    /** The list of struct fields, pushed to the end. */
    private val namespaceFields: MutableList<SmallMap<FrozenStringValue, MaybeDocHiddenValue<FrozenValue>>>,
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
        // Convert SmallMap<FrozenStringValue, MaybeDocHiddenValue<FrozenValue>> to
        // SmallMap<String, MaybeDocHiddenValue<FrozenValue>>
        // because NamespaceGen<V> uses String keys in the Kotlin port.
        val stringKeyFields = SmallMap.new<String, MaybeDocHiddenValue<FrozenValue>>()
        for ((k, v) in fields) {
            stringKeyFields.insert(k.asStr(), v)
        }
        setInner(
            name,
            heap.allocSimple(NamespaceGen.new(stringKeyFields)),
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

    /**
     * Set a constant value in the [GlobalsBuilder].
     *
     * Convenience method that allocates the value on the builder's frozen heap.
     * Supports primitives (Int, Long, Boolean, String) and [AllocFrozenValue] types.
     *
     */
    fun setConst(name: String, value: Any) {
        val frozenValue = when (value) {
            is AllocFrozenValue -> value.allocFrozenValue(heap)
            is Int -> value.allocFrozenValue(heap)
            is Long -> value.allocFrozenValue(heap)
            is Boolean -> FrozenValue.newBool(value)
            is String -> heap.allocStr(value).toFrozenValue()
            else -> error("setConst: unsupported value type ${value::class.simpleName}")
        }
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
     * generated by `starlarkDerive` and rarely needs to be called manually.
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
     * The optional [asType] parameter associates the function with a type
     * for type annotation purposes (the `asType = T` annotation in source-level docs).
     */
    fun setFunction(
        name: String,
        speculativeExecSafe: Boolean = false,
        asType: Ty? = null,
        f: (io.github.kotlinmania.starlark.eval.runtime.Arguments,
            io.github.kotlinmania.starlark.eval.runtime.Evaluator) -> Any?,
    ) {
        val sig = io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
            .withCapacity<FrozenValue>(name).finish()
        val nativeFn: NativeFuncFn = { eval, _, args ->
            try {
                val raw = f(args, eval)
                val heap = eval.heap()
                fun toValue(x: Any?): io.github.kotlinmania.starlark.values.layout.Value {
                    return when (x) {
                        null -> io.github.kotlinmania.starlark.values.layout.Value.newNone()
                        is Unit -> io.github.kotlinmania.starlark.values.layout.Value.newNone()
                        is io.github.kotlinmania.starlark.values.layout.Value -> x
                        is io.github.kotlinmania.starlark.values.layout.FrozenValue -> x.toValue()
                        is io.github.kotlinmania.starlark.values.AllocValue -> x.allocValue(heap)
                        else -> io.github.kotlinmania.starlark.values.layout.Value.newNone()
                    }
                }
                when (raw) {
                    is io.github.kotlinmania.starlark.values.layout.Value ->
                        kotlin.Result.success(raw)
                    is io.github.kotlinmania.starlark.values.layout.FrozenValue ->
                        kotlin.Result.success(raw.toValue())
                    is io.github.kotlinmania.starlark.values.AllocValue ->
                        kotlin.Result.success(raw.allocValue(heap))
                    is kotlin.Result<*> -> {
                        if (raw.isFailure) {
                            kotlin.Result.failure(raw.exceptionOrNull()!!)
                        } else {
                            kotlin.Result.success(toValue(raw.getOrNull()))
                        }
                    }
                    else ->
                        kotlin.Result.success(toValue(raw))
                }
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }
        set(
            name,
            NativeFunction(
                function = NativeFunc(nativeFn, sig),
                name = name,
                speculativeExecSafe = speculativeExecSafe,
                asType = asType,
                ty = asType ?: Ty.any(),
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
     * This function is called by the `starlarkDerive` generated code
     * and rarely needs to be called manually.
     */
    fun setDocstring(docstring: String) {
        this.docstring = docstring
    }
}

/**
 * Used to create globals.
 *
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
): Pair<DocString?, SmallMap<String, DocItem>> {
    val mainDocs = docstring?.let { ds ->
        DocString.fromDocstring(DocStringKind.Rust, ds)
    }
    val sorted = members
        .map { (name, value) -> Pair(name, value.toValue().documentation()) }
        .sortedBy { (name, _) -> name }
    val memberDocs = SmallMap.new<String, DocItem>()
    for ((name, doc) in sorted) {
        memberDocs.insert(name, doc)
    }

    return Pair(mainDocs, memberDocs)
}
