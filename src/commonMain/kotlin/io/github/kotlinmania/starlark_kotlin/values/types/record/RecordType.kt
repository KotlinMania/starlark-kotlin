// port-lint: source src/values/types/record/record_type.rs
package io.github.kotlinmania.starlark_kotlin.values.types.record.record_type

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

import io.github.kotlinmania.starlark_kotlin.collections.SmallMap
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.typing.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.user.TyUser
import io.github.kotlinmania.starlark_kotlin.typing.user.TyUserFields
import io.github.kotlinmania.starlark_kotlin.typing.user.TyUserParams
import io.github.kotlinmania.starlark_kotlin.util.ArcStr
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherFactory
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueLike
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.record.TyRecordData
import io.github.kotlinmania.starlark_kotlin.values.types.record.RecordTypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.types.record.Record
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueTypedComplex
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.writeHash
import io.github.kotlinmania.starlark_kotlin.values.types.namespace.attribute
import io.github.kotlinmania.starlark_kotlin.values.owned.downcast
import io.github.kotlinmania.starlark_kotlin.values.next
import io.github.kotlinmania.starlark_kotlin.typing.newNamedOnly
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.nextOpt
import io.github.kotlinmania.starlark_kotlin.docs.typ
import io.github.kotlinmania.starlark_kotlin.docs.ty
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark_kotlin.typing.TyUser
import io.github.kotlinmania.starlark_kotlin.typing.TyUserParams
import io.github.kotlinmania.starlark_kotlin.typing.TyUserFields

// #[doc(hidden)]
// pub trait RecordCell: ValueLifetimeless {
//     type TyRecordDataOpt: Debug;
//     fn get_or_init_ty(...);
//     fn get_ty(...);
// }
// Kotlin: Abstracted via the frozen flag in RecordTypeGen. See below.

// #[derive(Debug, thiserror::Error)]
// enum RecordTypeError {
//     #[error("Record instance cannot be created if record type is not assigned to a global variable")]
//     RecordTypeNotAssigned,
// }
private class RecordTypeError private constructor(message: String) : Exception(message) {
    companion object {
        fun recordTypeNotAssigned(): RecordTypeError =
            RecordTypeError("Record instance cannot be created if record type is not assigned to a global variable")
    }
}

/// The result of `record()`, being the type of records.
// #[derive(Debug, Trace, NoSerialize, ProvidesStaticType, Allocative)]
// pub struct RecordTypeGen<V: RecordCell> {
//     pub(crate) id: TypeInstanceId,
//     pub(crate) ty_record_data: V::TyRecordDataOpt,
//     fields: SmallMap<String, FieldGen<V>>,
// }
class RecordTypeGen internal constructor(
    internal val id: TypeInstanceId,
    // Kotlin: combined OnceCell (unfrozen) and Option (frozen) into single nullable field.
    internal var tyRecordData: TyRecordData?,
    /// The V is the type the field must satisfy (e.g. `"string"`)
    internal val fields: SmallMap<String, FieldGen<Value>>,
    private val frozen: Boolean,
) : StarlarkValue, Freeze {

    // Track whether tyRecordData has been initialized (for unfrozen).
    private var tyRecordDataInitialized: Boolean = tyRecordData != null

    // impl Display for RecordTypeGen
    override fun toString(): String {
        return "record(${fields.entries.joinToString(", ") { (k, v) -> "$k=$v" }})"
    }

    // impl Freeze for RecordType
    // fn freeze(self, freezer: &Freezer) -> FreezeResult<Self::Frozen>
    override fun freeze(freezer: Freezer): RecordTypeGen {
        val frozenFields = SmallMap<String, FieldGen<Value>>()
        for ((name, field) in fields) {
            frozenFields.put(name, field.freeze(freezer))
        }
        return RecordTypeGen(
            id = id,
            tyRecordData = tyRecordData,
            fields = frozenFields,
            frozen = true,
        )
    }

    // -- RecordCell helpers --

    // fn get_or_init_ty(...)
    internal fun getOrInitTy(f: () -> TyRecordData) {
        if (frozen) return
        if (!tyRecordDataInitialized) {
            tyRecordData = f()
            tyRecordDataInitialized = true
        }
    }

    // fn get_ty(...)
    internal fun getTy(): TyRecordData? = tyRecordData

    // pub(crate) fn ty_record_data(&self) -> Option<&Arc<TyRecordData>>
    fun tyRecordData(): TyRecordData? = getTy()

    // pub(crate) fn instance_ty(&self) -> Ty
    fun instanceTy(): Ty {
        return tyRecordData()?.tyRecord
            ?: error("Instances can only be created if named are assigned")
    }

    // fn make_parameter_spec(name: &str, fields: &SmallMap<String, FieldGen<V>>) -> ParametersSpec<FrozenValue>
    private fun makeParameterSpec(
        name: String,
        fields: SmallMap<String, FieldGen<Value>>,
    ): ParametersSpec<FrozenValue> {
        return ParametersSpec.newNamedOnly(
            name,
            fields.entries.map { (fieldName, field) ->
                Pair(
                    fieldName,
                    if (field.default != null) ParametersSpecParam.Optional
                    else ParametersSpecParam.Required,
                )
            },
        )
    }

    // #[starlark_value(type = FUNCTION_TYPE)]
    // impl StarlarkValue for RecordTypeGen

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    fun writeHash(hasher: Any) {
        for ((name, typ) in fields) {
            name.hashCode()
            typ.writeHash(hasher)
        }
    }

    // fn invoke(...)
    fun invoke(me: Value, args: Arguments, eval: Evaluator): Value {
        val tyRecordDataVal = tyRecordData()
            ?: throw RecordTypeError.recordTypeNotAssigned()

        val thisValue = me

        return tyRecordDataVal.parameterSpec.parser(args, eval) { paramParser, ev ->
            val recordFields = this.fields
            val values = mutableListOf<Value>()
            for ((name, field) in recordFields) {
                val value = if (field.default == null) {
                    val v: Value = paramParser.next()
                    field.typ.checkType(v, name)
                    v
                } else {
                    val v: Value? = paramParser.nextOpt()
                    when (v) {
                        null -> field.default
                        else -> {
                            field.typ.checkType(v, name)
                            v
                        }
                    }
                }
                values.add(value)
            }
            ev.heap().allocComplex(
                Record(
                    typ = thisValue,
                    values = values,
                )
            )
        }
    }

    // fn get_methods() -> Option<&'static Methods>
    fun getMethods(): Methods? {
        return recordTypeMethodsStatic.methods(::recordTypeMethods)
    }

    // fn eval_type(&self) -> Option<Ty>
    fun evalType(): Ty? {
        return tyRecordData()?.tyRecord
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    fun typecheckerTy(): Ty? {
        return tyRecordData()?.tyRecordType
    }

    // fn export_as(...)
    fun exportAs(variableName: String, eval: Evaluator) {
        getOrInitTy {
            val fieldsTy = sortedMapOf<String, Ty>().apply {
                for ((name, field) in fields) {
                    put(name, field.ty())
                }
            }

            val tyRecord = Ty.custom(
                TyUser.new(
                    variableName,
                    TyStarlarkValue.new("record"),
                    id,
                    TyUserParams(
                        matcher = TypeMatcherFactory.new(RecordTypeMatcher(id = id)),
                        fields = TyUserFields(
                            known = fieldsTy,
                            unknown = false,
                        ),
                    ),
                )
            )

            val tyRecordType = Ty.custom(
                TyUser.new(
                    "record[$variableName]",
                    TyStarlarkValue.new("function"),
                    TypeInstanceId.gen(),
                    TyUserParams(
                        callable = TyCallable.new(
                            ParamSpec.newNamedOnly(fields.entries.map { (name, field) ->
                                Triple(
                                    ArcStr.from(name),
                                    if (field.default != null) ParamIsRequired.No else ParamIsRequired.Yes,
                                    field.ty(),
                                )
                            }),
                            tyRecord,
                        ),
                    ),
                )
            )

            TyRecordData(
                name = variableName,
                tyRecord = tyRecord,
                tyRecordType = tyRecordType,
                parameterSpec = makeParameterSpec(variableName, fields),
            )
        }
    }

    companion object {
        // Type aliases:
        // pub type RecordType<'v> = RecordTypeGen<Value<'v>>;
        // pub type FrozenRecordType = RecordTypeGen<FrozenValue>;
        // Kotlin: Use RecordTypeGen directly; frozen flag distinguishes.

        // impl RecordType::new(...)
        // pub(crate) fn new(fields: SmallMap<String, FieldGen<Value<'v>>>) -> Self
        fun new(fields: SmallMap<String, FieldGen<Value>>): RecordTypeGen {
            return RecordTypeGen(
                id = TypeInstanceId.gen(),
                tyRecordData = null,
                fields = fields,
                frozen = false,
            )
        }
    }
}

/// Type alias for unfrozen record type.
// pub type RecordType<'v> = RecordTypeGen<Value<'v>>;
typealias RecordType = RecordTypeGen
/// Type alias for frozen record type.
// pub type FrozenRecordType = RecordTypeGen<FrozenValue>;
typealias FrozenRecordType = RecordTypeGen

// pub(crate) fn record_fields<'v>(...) -> &'v SmallMap<String, FieldGen<Value<'v>>>
internal fun recordFields(x: RecordTypeGen): SmallMap<String, FieldGen<Value>> = x.fields

// static RES: MethodsStatic = MethodsStatic::new();
private val recordTypeMethodsStatic = MethodsStatic()

// #[starlark_module]
// fn record_type_methods(methods: &mut MethodsBuilder)
private fun recordTypeMethods(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    // fn r#type<'v>(this: ValueTypedComplex<'v, RecordType<'v>>) -> starlark::Result<&'v str>
    methods.attribute("type") { thisValue, _ ->
        val this = thisValue.downcast<RecordTypeGen>()!!
        val tyRecordType = this.tyRecordData()
        when {
            tyRecordType != null -> tyRecordType.name
            else -> Record.TYPE
        }
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
