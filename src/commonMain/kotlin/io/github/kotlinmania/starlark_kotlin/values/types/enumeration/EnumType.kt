// port-lint: source src/values/types/enumeration/enum_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.enumeration.enum_type

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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.typing.TyUser
import io.github.kotlinmania.starlark_kotlin.typing.TyUserFields
import io.github.kotlinmania.starlark_kotlin.typing.TyUserIndex
import io.github.kotlinmania.starlark_kotlin.typing.TyUserParams
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.freezeSmallMap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.types.dict.ValueStr
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.EnumTypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.TyEnumData
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.EnumValueGen
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.EnumValue
import io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherFactory

// #[derive(thiserror::Error, Debug)]
// enum EnumError {
//     #[error("enum values must all be distinct, but repeated `{0}`")]
//     DuplicateEnumValue(String),
//     #[error("Unknown enum element `{0}`, given to `{1}`")]
//     InvalidElement(String, String),
// }
private sealed class EnumError(message: String) : Exception(message) {
    class DuplicateEnumValue(value: String) :
        EnumError("enum values must all be distinct, but repeated `$value`")

    class InvalidElement(element: String, enumType: String) :
        EnumError("Unknown enum element `$element`, given to `$enumType`")
}

// #[doc(hidden)]
// pub trait EnumCell: Freeze {
//     type TyEnumDataOpt: Debug;
//     fn get_or_init_ty(...);
//     fn get_ty(...);
// }
// Kotlin: Abstracted via the frozen flag in EnumTypeGen. See below.

/// The type of an enumeration, created by `enum()`.
// #[derive(Debug, Trace, Coerce, NoSerialize, ProvidesStaticType, Allocative)]
// #[repr(C)]
// pub struct EnumTypeGen<V: EnumCell> {
//     pub(crate) id: TypeInstanceId,
//     pub(crate) ty_enum_data: V::TyEnumDataOpt,
//     elements: UnsafeCell<SmallMap<V, V>>,
// }
class EnumTypeGen internal constructor(
    internal val id: TypeInstanceId,
    // Kotlin: combined OnceCell<Arc<TyEnumData>> (unfrozen) and Option<Arc<TyEnumData>> (frozen)
    // into a single nullable field. Initialized via getOrInitTy / getTy helpers.
    internal var tyEnumData: TyEnumData?,
    // The key is the value of the enumeration
    // The value is a value of type EnumValue
    private val elements: SmallMap<Value, Value>,
    private val frozen: Boolean,
) : StarlarkValue, AllocValue, Freeze<EnumTypeGen> {

    override val TYPE: String get() = FUNCTION_TYPE

    // Track whether tyEnumData has been initialized (for unfrozen).
    private var tyEnumDataInitialized: Boolean = tyEnumData != null

    // impl AllocValue for EnumTypeGen
    override fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }

    // impl StarlarkTypeRepr for EnumTypeGen
    override fun starlarkTypeRepr(): Ty = Ty.any()

    // impl Display for EnumTypeGen
    override fun toString(): String {
        return "enum(${elements().keys().joinToString(", ")})"
    }

    // impl Freeze for EnumTypeGen<Value>
    // fn freeze(self, freezer: &Freezer) -> FreezeResult<Self::Frozen>
    override fun freeze(freezer: Freezer): FreezeResult<EnumTypeGen> {
        val frozenElements = freezeSmallMap(
            elements,
            freezer,
            { v, f -> v.freeze(f) },
            { v, f -> v.freeze(f) },
        )
        if (frozenElements.isFailure) return FreezeResult.failure(frozenElements.exceptionOrNull()!!)
        return FreezeResult.success(EnumTypeGen(
            id = id,
            tyEnumData = tyEnumData,
            elements = frozenElements.getOrThrow(),
            frozen = true,
        ))
    }

    // -- EnumCell helpers --

    // fn get_or_init_ty(...)
    internal fun getOrInitTy(f: () -> TyEnumData) {
        if (frozen) {
            // Frozen: already set, ignore.
            return
        }
        if (!tyEnumDataInitialized) {
            tyEnumData = f()
            tyEnumDataInitialized = true
        }
    }

    // fn get_ty(...)
    internal fun getTy(): TyEnumData? = tyEnumData

    // impl EnumTypeGen (elements accessor)
    // pub(crate) fn elements(&self) -> &SmallMap<V, V>
    fun elements(): SmallMap<Value, Value> = elements

    // impl EnumTypeGen (ty_enum_data accessor)
    // pub(crate) fn ty_enum_data(&self) -> Option<&Arc<TyEnumData>>
    fun tyEnumData(): TyEnumData? = getTy()

    // impl EnumTypeGen (construct)
    // pub(crate) fn construct(&self, val: Value<'v>) -> crate::Result<V>
    fun construct(value: Value): Value {
        val hashed = value.getHashed().getOrThrow()
        return elements().getHashedByValue(hashed)
            ?: throw EnumError.InvalidElement(value.toStr(), toString())
    }

    // #[starlark_value(type = FUNCTION_TYPE)]
    // impl StarlarkValue for EnumTypeGen

    // fn invoke(...)
    fun invoke(me: Value, args: Arguments, eval: Evaluator): Value {
        args.noNamedArgs().getOrThrow()
        val v = args.positional1(eval.heap()).getOrThrow()
        return construct(v)
    }

    // fn get_attr(&self, attribute: &str, _heap: Heap<'v>) -> Option<Value<'v>>
    fun getAttr(attribute: String, heap: Heap): Value? {
        return elements().get(ValueStr(attribute))
    }

    // fn dir_attr(&self) -> Vec<String>
    fun dirAttr(): List<String> {
        return elements().keys().map { it.unpackStr()!! }.toList()
    }

    // fn length(&self) -> crate::Result<i32>
    fun length(): Int = elements().len()

    // fn at(&self, index: Value, _heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun at(index: Value, heap: Heap): Value {
        val i = convertIndex(index, elements().len()).getOrThrow()
        return elements().getIndex(i)!!.second
    }

    // unsafe fn iterate(&self, me: Value<'v>, _heap: Heap<'v>) -> crate::Result<Value<'v>>
    fun iterate(me: Value, heap: Heap): Value = me

    // unsafe fn iter_size_hint(&self, index: usize) -> (usize, Option<usize>)
    fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = elements().len() - index
        return Pair(rem, rem)
    }

    // unsafe fn iter_next(&self, index: usize, _heap: Heap<'v>) -> Option<Value<'v>>
    fun iterNext(index: Int, heap: Heap): Value? {
        val values = elements().values().toList()
        return if (index < values.size) values[index] else null
    }

    // unsafe fn iter_stop(&self)
    fun iterStop() {}

    // fn get_methods() -> Option<&'static Methods>
    fun getMethods(): Methods? {
        return enumTypeMethodsStatic.methods(::enumTypeMethods)
    }

    // fn eval_type(&self) -> Option<Ty>
    fun evalType(): Ty? {
        return tyEnumData()?.tyEnumValue
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    fun typecheckerTy(): Ty? {
        return tyEnumData()?.tyEnumType
    }

    // fn export_as(&self, variable_name: &str, _eval: &mut Evaluator<'v, '_, '_>) -> crate::Result<()>
    fun exportAs(variableName: String, eval: Evaluator) {
        getOrInitTy {
            val tyEnumValue = Ty.custom(
                TyUser.new(
                    variableName,
                    TyStarlarkValue.new("enum"),
                    id,
                    TyUserParams(
                        matcher = TypeMatcherFactory.new(EnumTypeMatcher(id = id)),
                    ),
                ).getOrThrow()
            )

            // The unwrap here is safe because the new() method requires the elements be
            // of type StringValue<'v>
            val fieldsMap = mutableMapOf<String, Ty>().apply {
                for (key in elements().keys()) {
                    put(key.unpackStr()!!, tyEnumValue)
                }
            }.toSortedMap()

            val tyEnumType = Ty.custom(
                TyUser.new(
                    "enum[$variableName]",
                    TyStarlarkValue.new("function"),
                    TypeInstanceId.gen(),
                    TyUserParams(
                        fields = TyUserFields(
                            known = fieldsMap,
                            unknown = false,
                        ),
                        index = TyUserIndex(
                            index = Ty.int(),
                            result = tyEnumValue,
                        ),
                        iterItem = tyEnumValue,
                        callable = TyCallable.new(
                            ParamSpec.posOnly(
                                listOf(Ty.any()),
                                emptyList(),
                            ),
                            tyEnumValue,
                        ),
                    ),
                ).getOrThrow()
            )
            TyEnumData(
                name = variableName,
                id = id,
                tyEnumValue = tyEnumValue,
                tyEnumType = tyEnumType,
            )
        }
    }

    companion object {
        // Type aliases:
        // pub type EnumType<'v> = EnumTypeGen<Value<'v>>;
        // pub type FrozenEnumType = EnumTypeGen<FrozenValue>;
        // Kotlin: Use EnumTypeGen directly; frozen flag distinguishes.

        private const val FUNCTION_TYPE = "function"

        // impl EnumType::new(...)
        // pub(crate) fn new(elements: Vec<StringValue<'v>>, heap: Heap<'v>) -> crate::Result<ValueTyped<'v, EnumType<'v>>>
        fun new(elements: List<StringValue>, heap: Heap): ValueTyped<EnumTypeGen> {
            val id = TypeInstanceId.gen()
            val elemMap = SmallMap.new<Value, Value>()

            val typ = EnumTypeGen(
                id = id,
                tyEnumData = null,
                elements = elemMap,
                frozen = false,
            )
            val typValue = heap.allocTyped(typ)

            for ((i, x) in elements.withIndex()) {
                val v = heap.allocSimple(
                    EnumValueGen(
                        id = id,
                        typ = typValue.toValue(),
                        index = i,
                        value = x.toValue(),
                    )
                )
                val hashed = x.toValue().getHashed().getOrThrow()
                if (elemMap.insertHashed(hashed, v) != null) {
                    throw EnumError.DuplicateEnumValue(x.toString())
                }
            }

            return typValue
        }
    }
}

/// Type alias for unfrozen enum type.
// pub type EnumType<'v> = EnumTypeGen<Value<'v>>;
typealias EnumType = EnumTypeGen
/// Type alias for frozen enum type.
// pub type FrozenEnumType = EnumTypeGen<FrozenValue>;
typealias FrozenEnumType = EnumTypeGen

// static RES: MethodsStatic = MethodsStatic::new();
private val enumTypeMethodsStatic = MethodsStatic()

// #[starlark_module]
// fn enum_type_methods(builder: &mut MethodsBuilder)
private fun enumTypeMethods(builder: MethodsBuilder) {
    // #[starlark(attribute)]
    // fn r#type<'v>(this: Value, heap: Heap<'_>) -> starlark::Result<Value<'v>>
    builder.setAttributeFn(
        name = "type",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.string(),
    ) { _, thisValue, heap ->
        val enumTypeGen = thisValue.downcastRef<EnumTypeGen>()!!
        val tyEnumType = enumTypeGen.tyEnumData()
        when {
            tyEnumType != null -> Result.success(heap.allocStr(tyEnumType.name))
            else -> Result.success(heap.allocStr(EnumValue.TYPE))
        }
    }

    // fn values<'v>(this: Value<'v>) -> anyhow::Result<AllocList<impl Iterator<Item = Value<'v>>>>
    // Note: In Rust this is a method, but the MethodsBuilder.setMethod API requires
    // NativeCallableComponents and ParametersSpec. Using setAttributeFn as a simpler
    // approach that exposes the values list.
    builder.setAttributeFn(
        name = "values",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.anyList(),
    ) { _, thisValue, heap ->
        val enumTypeGen = thisValue.downcastRef<EnumTypeGen>()!!
        val valuesList = enumTypeGen.elements().keys().toList()
        Result.success(heap.alloc(AllocList(valuesList)))
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
