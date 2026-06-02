// port-lint: source src/environment/globals.rs
package io.github.kotlinmania.starlark.environment

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

import io.github.kotlinmania.starlark.Error
import io.github.kotlinmania.starlark.ErrorKind
import io.github.kotlinmania.starlark.LibraryExtension
import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.symbol.SymbolMap
import io.github.kotlinmania.starlark.deriverefs.NativeCallableComponents
import io.github.kotlinmania.starlark.docs.DocFunction
import io.github.kotlinmania.starlark.docs.DocItem
import io.github.kotlinmania.starlark.docs.DocMember
import io.github.kotlinmania.starlark.docs.DocModule
import io.github.kotlinmania.starlark.docs.DocString
import io.github.kotlinmania.starlark.docs.DocStringKind
import io.github.kotlinmania.starlark.docs.DocType
import io.github.kotlinmania.starlark.docs.fromDocstring
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.standardEnvironment
import io.github.kotlinmania.starlark.stdlib.funcs.other.StarlarkFailError
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark.values.layout.avalues.str.allocStr
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeapRef
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.layout.typed.FrozenStringValue
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.owned.OwnedFrozenValue
import io.github.kotlinmania.starlark.values.types.NativeFunc
import io.github.kotlinmania.starlark.values.types.NativeFuncFn
import io.github.kotlinmania.starlark.values.types.NativeFunction
import io.github.kotlinmania.starlark.values.types.SpecialBuiltinFunction
import io.github.kotlinmania.starlark.values.types.bigint.allocFrozenValue
import io.github.kotlinmania.starlark.values.types.bigint.allocValue
import io.github.kotlinmania.starlark.values.types.bool.allocValue
import io.github.kotlinmania.starlark.values.types.dict.Dict
import io.github.kotlinmania.starlark.values.types.dict.allocValue
import io.github.kotlinmania.starlark.values.types.namespace.FrozenNamespace
import io.github.kotlinmania.starlark.values.types.namespace.MaybeDocHiddenValue
import io.github.kotlinmania.starlark.values.types.string.allocValue
import kotlin.concurrent.Volatile

/** Stored global value, optionally hidden from generated documentation. */
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
        fun new(): Globals = GlobalsBuilder.new().build()

        /**
         * Create a [Globals] following the
         * [Starlark standard](https://github.com/bazelbuild/starlark/blob/master/spec.md#built-in-constants-and-functions).
         */
        fun standard(): Globals = STANDARD

        /**
         * Create a [Globals] combining those functions in the Starlark standard plus
         * all those defined in [LibraryExtension].
         *
         * This function is public to use in the `starlark` binary,
         * but users of starlark should list the extensions they want explicitly.
         */
        fun extendedInternal(): Globals = EXTENDED_INTERNAL

        /** Empty globals. */
        private val EMPTY: Globals by lazy { GlobalsBuilder.new().build() }

        private val STANDARD: Globals by lazy { GlobalsBuilder.standard().build() }

        private val EXTENDED_INTERNAL: Globals by lazy { GlobalsBuilder.extended().build() }

        /** Empty globals. */
        internal fun empty(): Globals = EMPTY

        /**
         * Create a [Globals] combining those functions in the Starlark standard plus
         * all those given in the [LibraryExtension] arguments.
         */
        fun extendedBy(extensions: List<LibraryExtension>): Globals = GlobalsBuilder.extendedBy(extensions).build()
    }

    /**
     * This function is only safe if you first call [heap] and keep a reference to it.
     * Therefore, don't expose it on the public API.
     */
    internal fun get(name: String): Value? = getFrozen(name)?.toValue()

    /**
     * This function is only safe if you first call [heap] and keep a reference to it.
     * Therefore, don't expose it on the public API.
     */
    internal fun getFrozen(name: String): FrozenValue? = data.variables.getStr(name)?.value

    internal fun getOwned(name: String): OwnedFrozenValue? {
        val v = getFrozen(name) ?: return null
        // Safety: We know the heap this is allocated in.
        return OwnedFrozenValue(heap().clone(), v)
    }

    /** Get all the names defined in this environment. */
    fun names(): Iterator<FrozenStringValue> = data.variableNames.iterator()

    /**
     * Iterate over all the items in this environment.
     * Note returned values are owned by this globals.
     */
    fun iter(): Iterator<Pair<String, FrozenValue>> =
        data.variables
            .iter()
            .map { (n, v) -> Pair(n.asStr(), v.value) }
            .iterator()

    internal fun heap(): FrozenHeapRef = data.heap

    /** Print information about the values in this object. */
    fun describe(): String =
        data.variables.iter().joinToString("\n") { (name, value) ->
            value.value.toValue().describe(name.asStr())
        }

    /** Get the documentation for the object itself. */
    fun docstring(): String? = data.docstring

    /** Get the documentation for both the object itself, and its members. */
    fun documentation(): DocModule {
        val (docs, members) =
            commonDocumentation(
                data.docstring,
                data.variables
                    .iter()
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
        fun new(): GlobalsBuilder =
            GlobalsBuilder(
                heap = FrozenHeap.new(),
                variables = SymbolMap(),
                namespaceFields = mutableListOf(),
                docstring = null,
            )

        /**
         * Create a [GlobalsBuilder] following the
         * [Starlark standard](https://github.com/bazelbuild/starlark/blob/master/spec.md#built-in-constants-and-functions).
         */
        fun standard(): GlobalsBuilder = standardEnvironment()

        /**
         * Create a [GlobalsBuilder] combining those functions in the Starlark standard plus
         * all those defined in [LibraryExtension].
         */
        internal fun extended(): GlobalsBuilder = extendedBy(LibraryExtension.all())

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
        // Convert SmallMap<FrozenStringValue, GlobalValue> to SmallMap<String, GlobalValue>
        // because NamespaceGen<V> uses String keys in the Kotlin port.
        val stringKeyFields = SmallMap.new<String, GlobalValue>()
        for ((k, v) in fields) {
            stringKeyFields.insert(k.asStr(), v)
        }
        setInner(
            name,
            heap.allocSimple(FrozenNamespace.new(stringKeyFields)),
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
        val variableNames: MutableList<FrozenStringValue> =
            variables
                .keys()
                .map { x -> heap.allocStrIntern(x.asStr()) }
                .toMutableList()
        variableNames.sort()
        return Globals(
            GlobalsData(
                heap = heap.intoRef(),
                variables = variables,
                variableNames = variableNames,
                docstring = docstring,
            ),
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
     * Corresponds to the `const` syntax inside Rust's `#[starlark_module]` macro.
     */
    fun setConst(name: String, value: Any) {
        val frozenValue =
            when (value) {
                is AllocFrozenValue -> value.allocFrozenValue(heap)
                is Int -> value.allocFrozenValue(heap)
                is Long -> value.allocFrozenValue(heap)
                is Boolean -> FrozenValue.newBool(value)
                is String -> heap.allocStr(value).toFrozenValue()
                is FrozenValue -> value
                is Value -> {
                    val frozen = value.unpackFrozen()
                    if (frozen != null) {
                        frozen
                    } else {
                        val freezer =
                            io.github.kotlinmania.starlark.values.layout.Freezer
                                .new(heap)
                        freezer.freeze(value).getOrThrow()
                    }
                }
                else -> error("setConst: unsupported value type ${value::class.simpleName}")
            }
        setInner(name, frozenValue, false)
    }

    internal fun setInner(name: String, value: FrozenValue, docHidden: Boolean) {
        val globalValue =
            MaybeDocHiddenValue(
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
     * generated by native binding helpers and rarely needs to be called manually.
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
                ty =
                    ty ?: Ty
                        .fromNativeCallableComponents(
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
     * for type annotation purposes.
     */
    fun setFunction(
        name: String,
        speculativeExecSafe: Boolean = false,
        asType: Ty? = null,
        ty: Ty? = null,
        specialBuiltinFunction: SpecialBuiltinFunction? = null,
        f: (
            io.github.kotlinmania.starlark.eval.runtime.Arguments,
            io.github.kotlinmania.starlark.eval.runtime.Evaluator,
        ) -> Any?,
    ) {
        val sig =
            io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
                .withCapacity<FrozenValue>(name)
                .finish()
        val nativeFn: NativeFuncFn = { eval, _, args ->
            try {
                val result = f(args, eval)
                autoWrapFunctionResult(result, eval.heap())
            } catch (e: Exception) {
                kotlin.Result.failure(nativeFunctionError(e))
            }
        }
        set(
            name,
            NativeFunction(
                function = NativeFunc(nativeFn, sig),
                name = name,
                speculativeExecSafe = speculativeExecSafe,
                asType = asType,
                ty =
                    ty
                        ?: if (asType != null) {
                            Ty.ctorFunction(asType, ParamSpec.any(), asType)
                        } else {
                            Ty.function(ParamSpec.any(), Ty.any())
                        },
                docs = DocItem.Member(DocMember.Function(DocFunction())),
                specialBuiltinFunction = specialBuiltinFunction,
            ),
        )
    }

    private fun autoWrapFunctionResult(result: Any?, heap: Heap): Result<Value> {
        if (result is Result<*>) {
            val failure = result.exceptionOrNull()
            if (failure != null) {
                return Result.failure(failure)
            }
            return autoWrapFunctionResult(result.getOrNull(), heap)
        }
        if (result is Dict) {
            return Result.success(result.allocValue(heap))
        }
        if (result is Map<*, *>) {
            val sm = SmallMap.withCapacity<Value, Value>(result.size)
            for ((k, v) in result) {
                val wrappedKey = autoWrapFunctionResult(k, heap).getOrElse { return Result.failure(it) }
                val hashedKey = wrappedKey.getHashed().getOrElse { return Result.failure(it) }
                val wrappedValue = autoWrapFunctionResult(v, heap).getOrElse { return Result.failure(it) }
                sm.insertHashed(hashedKey, wrappedValue)
            }
            return Result.success(Dict.new(sm).allocValue(heap))
        }
        if (result is Iterable<*>) {
            val wrapped = mutableListOf<Value>()
            for (item in result) {
                val wrappedItem = autoWrapFunctionResult(item, heap).getOrElse { return Result.failure(it) }
                wrapped.add(wrappedItem)
            }
            return Result.success(heap.allocListIter(wrapped))
        }
        if (result is Sequence<*>) {
            val wrapped = mutableListOf<Value>()
            for (item in result) {
                val wrappedItem = autoWrapFunctionResult(item, heap).getOrElse { return Result.failure(it) }
                wrapped.add(wrappedItem)
            }
            return Result.success(heap.allocListIter(wrapped))
        }
        return Result.success(
            when (result) {
                null -> Value.newNone()
                is Value -> result
                is FrozenValue -> result.toValue()
                is StringValue -> result.toValue()
                is FrozenStringValue -> result.toValue()
                is AllocValue -> result.allocValue(heap)
                is StarlarkValue -> heap.allocSimple(result)
                is String -> result.allocValue(heap)
                is Int -> result.allocValue(heap)
                is Long -> result.allocValue(heap)
                is Boolean -> result.allocValue(heap)
                Unit -> Value.newNone()
                else ->
                    return Result.failure(
                        IllegalArgumentException(
                            "Cannot convert native function result of type ${result::class.simpleName} to Starlark value",
                        ),
                    )
            },
        )
    }

    private fun nativeFunctionError(e: Exception): Throwable =
        when (e) {
            is Error -> e
            is StarlarkFailError -> Error.newKind(ErrorKind.Fail(e))
            else -> e
        }

    /** Heap where globals are allocated. Can be used to allocate additional values. */
    fun frozenHeap(): FrozenHeap = heap

    /**
     * Allocate a value using the same underlying heap as the [GlobalsBuilder],
     * only intended for values that are referred to by those which are passed
     * to [set].
     */
    fun alloc(value: AllocFrozenValue): FrozenValue = value.allocFrozenValue(heap)

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
): Pair<DocString?, SmallMap<String, DocItem>> {
    val mainDocs =
        docstring?.let { ds ->
            DocString.fromDocstring(DocStringKind.Rust, ds)
        }
    val sorted =
        members
            .map { (name, value) -> Pair(name, value.toValue().documentation()) }
            .sortedBy { (name, _) -> name }
    val memberDocs = SmallMap.new<String, DocItem>()
    for ((name, doc) in sorted) {
        memberDocs.insert(name, doc)
    }

    return Pair(mainDocs, memberDocs)
}
