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
import io.github.kotlinmania.starlark_kotlin.collections.StarlarkHasher
import io.github.kotlinmania.starlark_kotlin.environment.Methods
import io.github.kotlinmania.starlark_kotlin.environment.MethodsBuilder
import io.github.kotlinmania.starlark_kotlin.environment.MethodsStatic
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.ParametersParser
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark_kotlin.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark_kotlin.typing.ParamIsRequired
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyCallable
import io.github.kotlinmania.starlark_kotlin.typing.TyStarlarkValue
import io.github.kotlinmania.starlark_kotlin.typing.TyUser
import io.github.kotlinmania.starlark_kotlin.typing.TyUserFields
import io.github.kotlinmania.starlark_kotlin.typing.TyUserParams
import io.github.kotlinmania.starlark_kotlin.values.ComplexValue
import io.github.kotlinmania.starlark_kotlin.values.Freeze
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.ValueUnpackValue
import io.github.kotlinmania.starlark_kotlin.values.freezeSmallMap
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.types.record.Field
import io.github.kotlinmania.starlark_kotlin.values.types.record.RecordGen
import io.github.kotlinmania.starlark_kotlin.values.types.record.RecordTypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.types.record.TyRecordData
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherFactory

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
    internal val fields: SmallMap<String, Field>,
    private val frozen: Boolean,
) : ComplexValue, Freeze<RecordTypeGen> {

    // Track whether tyRecordData has been initialized (for unfrozen).
    private var tyRecordDataInitialized: Boolean = tyRecordData != null

    // impl Display for RecordTypeGen
    override fun toString(): String {
        return "record(${fields.iter().joinToString(", ") { (k, v) -> "$k=$v" }})"
    }

    // impl Freeze for RecordType
    // fn freeze(self, freezer: &Freezer) -> FreezeResult<Self::Frozen>
    override fun freeze(freezer: Freezer): FreezeResult<RecordTypeGen> {
        val frozenFields = freezeSmallMap(
            fields,
            freezer,
            freezeKey = { k, _ -> FreezeResult.success(k) },
            freezeValue = { field, _ -> FreezeResult.success(field) },
        ).getOrElse { return FreezeResult.failure(it) }
        return FreezeResult.success(
            RecordTypeGen(
                id = id,
                tyRecordData = tyRecordData,
                fields = frozenFields,
                frozen = true,
            )
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
        fields: SmallMap<String, Field>,
    ): ParametersSpec<FrozenValue> {
        return ParametersSpec.newNamedOnly(
            name,
            fields.iter().map { (fieldName, field) ->
                Pair(
                    fieldName,
                    if (field.default != null) ParametersSpecParam.Optional
                    else ParametersSpecParam.Required,
                )
            }.toList(),
        )
    }

    // #[starlark_value(type = FUNCTION_TYPE)]
    // impl StarlarkValue for RecordTypeGen

    // fn write_hash(&self, hasher: &mut StarlarkHasher) -> crate::Result<()>
    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        for ((name, typ) in fields) {
            name.hashCode()
            typ.writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    // fn invoke(...)
    override fun invoke(me: Value, args: Arguments, eval: Evaluator): Result<Value> {
        val tyRecordDataVal = tyRecordData()
            ?: return Result.failure(RecordTypeError.recordTypeNotAssigned())

        val thisValue = me

        return Result.success(tyRecordDataVal.parameterSpec.parser(args, eval) { paramParser, ev ->
            val recordFields = this.fields
            val values = mutableListOf<Value>()
            for ((name, field) in recordFields) {
                val value = if (field.default == null) {
                    val v: Value = paramParser.next(ValueUnpackValue)
                    field.typ.checkType(v, name).getOrThrow()
                    v
                } else {
                    val v: Value? = paramParser.nextOpt(ValueUnpackValue)
                    when (v) {
                        null -> field.default
                        else -> {
                            field.typ.checkType(v, name).getOrThrow()
                            v
                        }
                    }
                }
                values.add(value)
            }
            ev.heap().allocComplex(
                RecordGen(
                    typ = thisValue,
                    values = values,
                )
            )
        })
    }

    // fn get_methods() -> Option<&'static Methods>
    override fun getMethods(): Methods? {
        return recordTypeMethodsStatic.methods(::recordTypeMethods)
    }

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return tyRecordData()?.tyRecord
    }

    // fn typechecker_ty(&self) -> Option<Ty>
    override fun typecheckerTy(): Ty? {
        return tyRecordData()?.tyRecordType
    }

    // fn export_as(...)
    override fun exportAs(variableName: String, eval: Evaluator): Result<Unit> {
        getOrInitTy {
            val fieldsTy = linkedMapOf<String, Ty>().apply {
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
                ).getOrThrow()
            )

            val tyRecordType = Ty.custom(
                TyUser.new(
                    "record[$variableName]",
                    TyStarlarkValue.new("function"),
                    TypeInstanceId.gen(),
                    TyUserParams(
                        callable = TyCallable.new(
                            ParamSpec.newParts(
                                namedOnly = fields.iter().map { (name, field) ->
                                    Triple(
                                        name,
                                        if (field.default != null) ParamIsRequired.No else ParamIsRequired.Yes,
                                        field.ty(),
                                    )
                                }.toList(),
                            ),
                            tyRecord,
                        ),
                    ),
                ).getOrThrow()
            )

            TyRecordData(
                name = variableName,
                tyRecord = tyRecord,
                tyRecordType = tyRecordType,
                parameterSpec = makeParameterSpec(variableName, fields),
            )
        }
        return Result.success(Unit)
    }

    companion object {
        // Type aliases:
        // pub type RecordType<'v> = RecordTypeGen<Value<'v>>;
        // pub type FrozenRecordType = RecordTypeGen<FrozenValue>;
        // Kotlin: Use RecordTypeGen directly; frozen flag distinguishes.

        // impl RecordType::new(...)
        // pub(crate) fn new(fields: SmallMap<String, FieldGen<Value<'v>>>) -> Self
        fun new(fields: SmallMap<String, Field>): RecordTypeGen {
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
internal fun recordFields(x: RecordTypeGen): SmallMap<String, Field> = x.fields

// static RES: MethodsStatic = MethodsStatic::new();
private val recordTypeMethodsStatic = MethodsStatic()

// #[starlark_module]
// fn record_type_methods(methods: &mut MethodsBuilder)
private fun recordTypeMethods(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    // fn r#type<'v>(this: ValueTypedComplex<'v, RecordType<'v>>) -> starlark::Result<&'v str>
    methods.setAttributeFn(
        name = "type",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.string(),
    ) { _, thisValue, heap ->
        val recordType = thisValue.downcastRef<RecordTypeGen>()
            ?: return@setAttributeFn Result.failure(IllegalStateException("Expected RecordTypeGen"))
        val tyRecordType = recordType.tyRecordData()
        val name = when {
            tyRecordType != null -> tyRecordType.name
            else -> RecordGen.TYPE
        }
        Result.success(heap.allocStr(name))
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
