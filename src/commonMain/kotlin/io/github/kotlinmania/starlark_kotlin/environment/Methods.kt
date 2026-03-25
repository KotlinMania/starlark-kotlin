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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
import io.github.kotlinmania.starlark_kotlin.collections.symbol.map.SymbolMap
import io.github.kotlinmania.starlark_kotlin.docs.DocType
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeapRef
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.types.UnboundValue
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMethod
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMethFn
import io.github.kotlinmania.starlark_kotlin.values.types.NativeMeth
import io.github.kotlinmania.starlark_kotlin.values.types.NativeAttribute
import io.github.kotlinmania.starlark_kotlin.stdlib.Symbol
import io.github.kotlinmania.starlark_kotlin.eval.bc.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.__derive_refs.NativeCallableComponents
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.util.asStr
import io.github.kotlinmania.starlark_kotlin.stdlib.new
import io.github.kotlinmania.starlark_kotlin.values.types.allocSimpleTypedStatic
import io.github.kotlinmania.starlark_kotlin.values.owned.toFrozenValue
import io.github.kotlinmania.starlark_kotlin.docs.typ
import io.github.kotlinmania.starlark_kotlin.docs.ty
import io.github.kotlinmania.starlark_kotlin.analysis.iter

/// Methods of an object.
// #[derive(Clone, Debug)]
// pub struct Methods
class Methods internal constructor(
    // This field holds the objects referenced in `members`.
    @Suppress("unused")
    private val heap: FrozenHeapRef,
    internal val members: SymbolMap<UnboundValue>,
    private val docstring: String?,
) {
    // impl Methods (first block)

    // pub(crate) fn get<'v>(&'v self, name: &str) -> Option<Value<'v>>
    internal fun get(name: String): Value? {
        return members.getStr(name)?.toFrozenValue()?.toValue()
    }

    /// Gets the type of the member.
    /// In the case of an attribute, this is the type the attribute evaluates to, while in the case
    /// of a method, this is the `TyCallable`.
    // pub(crate) fn get_ty(&self, name: &str) -> Option<Ty>
    internal fun getTy(name: String): Ty? {
        return when (val member = members.getStr(name)) {
            null -> null
            is UnboundValue.Attr -> member.attr.typ
            is UnboundValue.Method -> member.method.ty
        }
    }

    // pub(crate) fn get_hashed(&self, name: Hashed<&str>) -> Option<&UnboundValue>
    internal fun getHashed(name: Hashed<String>): UnboundValue? {
        return members.getHashedStr(name)
    }

    // pub(crate) fn get_frozen_symbol(&self, name: &Symbol) -> Option<&UnboundValue>
    internal fun getFrozenSymbol(name: Symbol): UnboundValue? {
        return members.get(name)
    }

    // pub(crate) fn names(&self) -> Vec<String>
    internal fun names(): List<String> {
        return members.keys().map { it.asStr() }
    }

    // pub(crate) fn members(&self) -> impl Iterator<Item = (&str, FrozenValue)>
    internal fun members(): Iterator<Pair<String, FrozenValue>> {
        return members.iter().map { (k, v) -> Pair(k.asStr(), v.toFrozenValue()) }.iterator()
    }

    /// Fetch the documentation.
    // pub fn documentation(&self, ty: Ty) -> DocType
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

    // impl Methods (second block)

    companion object {
        // pub fn new() -> Self
        /** Create an empty [Methods], with no functions in scope. */
        fun new(): Methods {
            return MethodsBuilder.new().build()
        }
    }
}

/// Used to build a [`Methods`] value.
// #[derive(Debug)]
// pub struct MethodsBuilder
class MethodsBuilder private constructor(
    // The heap everything is allocated in.
    private val heap: FrozenHeap,
    // Members, either `NativeMethod` or `NativeAttribute`.
    internal val members: SymbolMap<UnboundValue>,
    // The raw docstring for the main object.
    internal var docstring: String?,
) {
    // impl MethodsBuilder

    companion object {
        // pub fn new() -> Self
        /** Create an empty [MethodsBuilder], with no functions in scope. */
        fun new(): MethodsBuilder {
            return MethodsBuilder(
                heap = FrozenHeap.new(),
                members = SymbolMap.new(),
                docstring = null,
            )
        }
    }

    // pub fn build(self) -> Methods
    /** Called at the end to build a [Methods]. */
    fun build(): Methods {
        return Methods(
            heap = heap.intoRef(),
            members = members,
            docstring = docstring,
        )
    }

    // pub fn with(mut self, f: impl FnOnce(&mut Self)) -> Self
    /** A fluent API for modifying [MethodsBuilder] and returning the result. */
    fun with(f: (MethodsBuilder) -> Unit): MethodsBuilder {
        f(this)
        return this
    }

    // pub fn set_docstring(&mut self, docstring: &str)
    /** Set the raw docstring for this object. */
    fun setDocstring(docstring: String) {
        this.docstring = docstring
    }

    // pub fn set_attribute<'v, V: AllocFrozenValue>(&'v mut self, name: &str, value: V, docstring: Option<String>)
    /** Set a constant value in the [MethodsBuilder]. */
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
                callable = { v, _, _ -> Result.success(v!!.toValue()) },
            ))),
        )
    }

    // #[doc(hidden)]
    // pub fn set_attribute_fn(&mut self, name: &str, ...)
    /** Set an attribute. Only used by starlark_module macro. */
    fun setAttributeFn(
        name: String,
        speculativeExecSafe: Boolean,
        docstring: String?,
        typ: Ty,
        // The first argument is always `None`
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

    // #[doc(hidden)]
    // pub fn set_method(&mut self, name: &str, ...)
    /** Set a method. Only used by starlark_module macro. */
    fun setMethod(
        name: String,
        components: NativeCallableComponents,
        sig: ParametersSpec<FrozenValue>,
        f: NativeMethFn,
    ) {
        // TODO(nga): do not unwrap.
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

    // pub fn alloc<'v, V: AllocFrozenValue>(&'v self, value: V) -> FrozenValue
    /** Allocate a value using the same underlying heap as the [MethodsBuilder]. */
    fun alloc(value: AllocFrozenValue): FrozenValue {
        return value.allocFrozenValue(heap)
    }
}

/// Used to create methods for a StarlarkValue.
// pub struct MethodsStatic(OnceCell<Methods>);
class MethodsStatic {
    @Volatile
    private var methods: Methods? = null

    // impl MethodsStatic

    // pub const fn new() -> Self
    /** Create a new [MethodsStatic]. */
    constructor()

    // pub fn methods(&'static self, x: impl FnOnce(&mut MethodsBuilder)) -> Option<&'static Methods>
    /** Populate the methods with a builder function. Always returns non-null. */
    fun methods(x: (MethodsBuilder) -> Unit): Methods {
        methods?.let { return it }
        val built = MethodsBuilder.new().with(x).build()
        methods = built
        return built
    }

    // pub fn populate(&'static self, x: impl FnOnce(&mut MethodsBuilder), out: &mut MethodsBuilder)
    /** Copy all the methods in this [MethodsBuilder] into a new one. */
    fun populate(x: (MethodsBuilder) -> Unit, out: MethodsBuilder) {
        val m = methods(x)
        for ((name, value) in m.members.iter()) {
            out.members.insert(name.asStr(), value)
        }
        out.docstring = m.docstring()
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
