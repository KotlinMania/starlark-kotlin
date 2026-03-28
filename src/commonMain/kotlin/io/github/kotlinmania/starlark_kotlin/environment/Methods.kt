// port-lint: source src/environment/methods.rs
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
import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.symbol.map.SymbolMap
import io.github.kotlinmania.starlark_kotlin.collections.symbol.symbol.Symbol
import io.github.kotlinmania.starlark_kotlin.docs.DocFunction
import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.docs.DocMember
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.types.NativeAttribute
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMeth
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMethFn
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMethod
import io.github.kotlinmania.starlark_kotlin.values.types.UnboundValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimpleTypedStatic

/**
 * Methods of an object.
 *
 * @property heap This field holds the objects referenced in [members].
 * @property members The member map keyed by symbol name.
 * @property docstring The raw docstring for this object.
 */
class Methods internal constructor(
    /** This field holds the objects referenced in [members]. */
    @Suppress("unused")
    private val heap: FrozenHeapRef,
    internal val members: SymbolMap<UnboundValue>,
    internal val docstring: String?,
) {

    internal fun get(name: String): Value? {
        return members.getStr(name)?.toFrozenValue()?.toValue()
    }

    /**
     * Gets the type of the member.
     *
     * In the case of an attribute, this is the type the attribute evaluates to, while in the case
     * of a method, this is the `TyCallable`.
     */
    internal fun getTy(name: String): Ty? {
        return when (val member = members.getStr(name)) {
            null -> null
            is UnboundValue.Attr -> member.attr.typ
            is UnboundValue.Method -> member.method.ty
        }
    }

    internal fun getHashed(name: Hashed<String>): UnboundValue? {
        return members.getHashedStr(name)
    }

    internal fun getFrozenSymbol(name: Symbol): UnboundValue? {
        return members.get(name)
    }

    internal fun names(): List<String> {
        return members.keys().map { it.asStr() }
    }

    internal fun members(): Iterator<Pair<String, FrozenValue>> {
        return members.iter().map { (k, v) -> Pair(k.asStr(), v.toFrozenValue()) }.iterator()
    }

    /** Fetch the documentation. */
    fun documentation(ty: Ty): DocType {
        val (docs, memberDocs) = commonDocumentation(
            docstring,
            members.iter().map { (n, v) -> Pair(n.asStr(), v.toFrozenValue()) },
        )

        return DocType(
            docs = docs,
            members = memberDocs
                .mapNotNull { (n, item) ->
                    // This is only `None` if the item is a module, but types shouldn't really have
                    // modules in them anyway, so that seems ok
                    val member = item.tryAsMemberWithCollapsedObject() ?: return@mapNotNull null
                    Pair(n, member)
                }
                .toMap(),
            ty = ty,
            constructor = null,
        )
    }

    companion object {
        /** Create an empty [Methods], with no functions in scope. */
        fun new(): Methods {
            return MethodsBuilder.new().build()
        }
    }
}

/**
 * Used to build a [Methods] value.
 *
 * @property heap The heap everything is allocated in.
 * @property members Members, either `NativeMethod` or `NativeAttribute`.
 * @property docstring The raw docstring for the main object.
 *   FIXME(JakobDegen): This should probably be removed. Not only can these docstrings not be
 *   combined with each other, but having the main documentation for the object on the methods
 *   instead of on the object type directly is extraordinarily confusing.
 */
class MethodsBuilder private constructor(
    /** The heap everything is allocated in. */
    private val heap: FrozenHeap,
    /** Members, either `NativeMethod` or `NativeAttribute`. */
    internal val members: SymbolMap<UnboundValue>,
    /** The raw docstring for the main object. */
    internal var docstring: String?,
) {

    companion object {
        /** Create an empty [MethodsBuilder], with no functions in scope. */
        fun new(): MethodsBuilder {
            return MethodsBuilder(
                heap = FrozenHeap.new(),
                members = SymbolMap(),
                docstring = null,
            )
        }
    }

    /** Called at the end to build a [Methods]. */
    fun build(): Methods {
        return Methods(
            heap = heap.intoRef(),
            members = members,
            docstring = docstring,
        )
    }

    /** A fluent API for modifying [MethodsBuilder] and returning the result. */
    fun with(f: (MethodsBuilder) -> Unit): MethodsBuilder {
        f(this)
        return this
    }

    /** Set the raw docstring for this object. */
    fun setDocstring(docstring: String) {
        this.docstring = docstring
    }

    /**
     * Set a constant value in the [MethodsBuilder] that will be suitable for use with
     * `StarlarkValue.getMethods`.
     */
    fun setAttribute(name: String, value: AllocFrozenValue, docstring: String?) {
        // We want to build an attribute, that ignores its self argument, and does no subsequent allocation.
        val frozenValue = heap.alloc(value)
        members.insert(
            name,
            UnboundValue.Attr(heap.allocSimpleTypedStatic(NativeAttribute(
                speculativeExecSafe = true,
                docstring = docstring,
                typ = value.starlarkTypeRepr(),
                data = frozenValue,
                // Safety: Set to `Some` immediately above
                callable = { v, _, _ -> Result.success(v!!.toValue()) },
            ))),
        )
    }

    /** Set an attribute. Only used by `starlark_module` macro. */
    fun setAttributeFn(
        name: String,
        speculativeExecSafe: Boolean,
        docstring: String?,
        typ: Ty,
        /** The first argument is always `null`. */
        f: (FrozenValue?, Value, Heap) -> Result<Value>,
    ) {
        members.insert(
            name,
            UnboundValue.Attr(heap.allocSimpleTypedStatic(NativeAttribute(
                speculativeExecSafe = speculativeExecSafe,
                docstring = docstring,
                typ = typ,
                data = null,
                callable = f,
            ))),
        )
    }

    /** Set a method. Only used by `starlark_module` macro. */
    fun setMethod(
        name: String,
        components: NativeCallableComponents,
        sig: ParametersSpec<FrozenValue>,
        f: NativeMethFn,
    ) {
        val ty = Ty.fromNativeCallableComponents(components, null).getOrThrow()

        members.insert(
            name,
            UnboundValue.Method(heap.allocSimpleTypedStatic(NativeMethod(
                function = NativeMeth(f, sig),
                name = name,
                speculativeExecSafe = components.speculativeExecSafe,
                docs = components.intoDocs(null),
                ty = ty,
            ))),
        )
    }

    /**
     * Convenience overload: register a method by name with a lambda.
     * The lambda receives (Evaluator, Value, ParametersSpec, Arguments) -> Result<Value>
     * and is wrapped into the full method registration.
     */
    fun setMethod(
        name: String,
        f: (Evaluator, Value, ParametersSpec<FrozenValue>, Arguments) -> Result<Value>,
    ) {
        val sig = ParametersSpec.withCapacity<FrozenValue>(name).finish()
        val nativeMethFn: NativeMethFn = f
        val ty = Ty.any()
        members.insert(
            name,
            UnboundValue.Method(heap.allocSimpleTypedStatic(NativeMethod(
                function = NativeMeth(nativeMethFn, sig),
                name = name,
                speculativeExecSafe = false,
                docs = DocItem.Member(DocMember.Function(DocFunction())),
                ty = ty,
            ))),
        )
    }

    /**
     * Convenience overload: register an attribute by name with a lambda.
     * The lambda receives (Value, Heap) -> Result<Value>.
     */
    fun setAttribute(
        name: String,
        docstring: String? = null,
        f: (Value, Heap) -> Result<Value>,
    ) {
        setAttributeFn(
            name = name,
            speculativeExecSafe = false,
            docstring = docstring,
            typ = Ty.any(),
            f = { _, thisValue, heap -> f(thisValue, heap) },
        )
    }

    /** Allocate a value using the same underlying heap as the [MethodsBuilder]. */
    fun alloc(value: AllocFrozenValue): FrozenValue {
        return value.allocFrozenValue(heap)
    }
}

/**
 * Used to create methods for a `StarlarkValue`.
 *
 * To define a method `foo()` on your type, define
 * usually written as:
 *
 * ```
 * fun myMethods(builder: MethodsBuilder) {
 *     // define foo here
 * }
 *
 * // In your StarlarkValue implementation:
 * override fun getMethods(): Methods? {
 *     return RES.methods(::myMethods)
 * }
 * ```
 */
class MethodsStatic {

    /** Create a new [MethodsStatic]. */
    constructor()

    @Volatile
    private var cachedMethods: Methods? = null

    /**
     * Populate the methods with a builder function. Always returns non-null,
     * but using this API to be a better fit for `StarlarkValue.getMethods`.
     */
    fun methods(x: (MethodsBuilder) -> Unit): Methods {
        cachedMethods?.let { return it }
        val built = MethodsBuilder.new().with(x).build()
        cachedMethods = built
        return built
    }

    /**
     * Copy all the methods in this [MethodsStatic] into a new one. All variables will
     * only be allocated once (ensuring things like function comparison works properly).
     */
    fun populate(x: (MethodsBuilder) -> Unit, out: MethodsBuilder) {
        val m = methods(x)
        for ((name, value) in m.members.iter()) {
            out.members.insert(name.asStr(), value)
        }
        out.docstring = m.docstring
    }
}
