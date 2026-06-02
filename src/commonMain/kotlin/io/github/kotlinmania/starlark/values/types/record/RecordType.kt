// port-lint: source src/values/types/record/record_type.rs
package io.github.kotlinmania.starlark.values.types.record.recordtype

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

import io.github.kotlinmania.starlark.collections.SmallMap
import io.github.kotlinmania.starlark.collections.StarlarkHasher
import io.github.kotlinmania.starlark.environment.Methods
import io.github.kotlinmania.starlark.environment.MethodsBuilder
import io.github.kotlinmania.starlark.environment.MethodsStatic
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpec
import io.github.kotlinmania.starlark.eval.runtime.params.spec.ParametersSpecParam
import io.github.kotlinmania.starlark.typing.ParamIsRequired
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.typing.TyStarlarkValue
import io.github.kotlinmania.starlark.typing.TyUser
import io.github.kotlinmania.starlark.typing.TyUserFields
import io.github.kotlinmania.starlark.typing.TyUserParams
import io.github.kotlinmania.starlark.values.ComplexValue
import io.github.kotlinmania.starlark.values.Freeze
import io.github.kotlinmania.starlark.values.ValueUnpackValue
import io.github.kotlinmania.starlark.values.freezeSmallMap
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.allocComplex
import io.github.kotlinmania.starlark.values.types.FUNCTION_TYPE
import io.github.kotlinmania.starlark.values.types.TypeInstanceId
import io.github.kotlinmania.starlark.values.types.record.Field
import io.github.kotlinmania.starlark.values.types.record.RecordGen
import io.github.kotlinmania.starlark.values.types.record.RecordTypeMatcher
import io.github.kotlinmania.starlark.values.types.record.TyRecordData
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeMatcherFactory

// #[doc(hidden)]
// }
// Kotlin: Abstracted via the frozen flag in RecordTypeGen. See below.

//     #[error("Record instance cannot be created if record type is not assigned to a global variable")]
//     RecordTypeNotAssigned,
// }
private class RecordTypeError private constructor(
    message: String,
) : Exception(message) {
    companion object {
        fun recordTypeNotAssigned(): RecordTypeError =
            RecordTypeError("Record instance cannot be created if record type is not assigned to a global variable")
    }
}

/** The result of `record()`, being the type of records. */
//     pub(crate) id: TypeInstanceId,
//     pub(crate) ty_record_data: V::TyRecordDataOpt,
//     fields: SmallMap<String, FieldGen<V>>,
// }
class RecordTypeGen internal constructor(
    internal val id: TypeInstanceId,
    // Kotlin: combined OnceCell (unfrozen) and Option (frozen) into single nullable field.
    internal var tyRecordData: TyRecordData?,
    /** The V is the type the field must satisfy (e.g. `"string"`) */
    internal val fields: SmallMap<String, Field>,
    private val frozen: Boolean,
) : ComplexValue,
    Freeze<RecordTypeGen> {
    // Track whether tyRecordData has been initialized (for unfrozen).
    private var tyRecordDataInitialized: Boolean = tyRecordData != null

    // impl Display for RecordTypeGen
    override fun toString(): String = "record(${fields.iter().joinToString(", ") { (k, v) -> "$k=$v" }})"

    // impl Freeze for RecordType
    override fun freeze(freezer: Freezer): Result<RecordTypeGen> {
        val frozenFields =
            freezeSmallMap(
                fields,
                freezer,
                freezeKey = { k, _ -> Result.success(k) },
                freezeValue = { field, _ -> Result.success(field) },
            ).getOrElse { return Result.failure(it) }
        return Result.success(
            RecordTypeGen(
                id = id,
                tyRecordData = tyRecordData,
                fields = frozenFields,
                frozen = true,
            ),
        )
    }

    // -- RecordCell helpers --

    internal fun getOrInitTy(f: () -> TyRecordData) {
        if (frozen) return
        if (!tyRecordDataInitialized) {
            tyRecordData = f()
            tyRecordDataInitialized = true
        }
    }

    internal fun getTy(): TyRecordData? = tyRecordData

    fun tyRecordData(): TyRecordData? = getTy()

    fun instanceTy(): Ty =
        tyRecordData()?.tyRecord
            ?: error("Instances can only be created if named are assigned")

    private fun makeParameterSpec(
        name: String,
        fields: SmallMap<String, Field>,
    ): ParametersSpec<FrozenValue> =
        ParametersSpec.newNamedOnly(
            name,
            fields
                .iter()
                .map { (fieldName, field) ->
                    Pair(
                        fieldName,
                        if (field.default != null) {
                            ParametersSpecParam.Optional
                        } else {
                            ParametersSpecParam.Required
                        },
                    )
                }.toList(),
        )

    // impl StarlarkValue for RecordTypeGen
    override val TYPE: String get() = FUNCTION_TYPE
    override val HAS_invoke: Boolean get() = true
    override val HAS_eval_type: Boolean get() = true

    override fun writeHash(hasher: StarlarkHasher): Result<Unit> {
        for ((name, typ) in fields) {
            name.hashCode()
            typ.writeHash(hasher).getOrElse { return Result.failure(it) }
        }
        return Result.success(Unit)
    }

    override fun invoke(me: Value, args: Arguments, eval: Evaluator): Result<Value> {
        val tyRecordDataVal =
            tyRecordData()
                ?: return Result.failure(RecordTypeError.recordTypeNotAssigned())

        val thisValue = me

        return Result.success(
            tyRecordDataVal.parameterSpec.parser(args, eval) { paramParser, ev ->
                val recordFields = this.fields
                val values = mutableListOf<Value>()
                for ((name, field) in recordFields) {
                    val value =
                        if (field.default == null) {
                            val v: Value = paramParser.next(ValueUnpackValue)
                            field.typ.checkType(v, name).getOrThrow()
                            v
                        } else {
                            when (val v: Value? = paramParser.nextOpt(ValueUnpackValue)) {
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
                    ),
                )
            },
        )
    }

    override fun getMethods(): Methods = recordTypeMethodsStatic.methods(::recordTypeMethods)

    override fun evalType(): Ty? = tyRecordData()?.tyRecord

    override fun typecheckerTy(): Ty? = tyRecordData()?.tyRecordType

    override fun exportAs(variableName: String, eval: Evaluator): Result<Unit> {
        getOrInitTy {
            val fieldsTy =
                linkedMapOf<String, Ty>().apply {
                    for ((name, field) in fields) {
                        put(name, field.ty())
                    }
                }

            val tyRecord =
                Ty.custom(
                    TyUser
                        .new(
                            variableName,
                            TyStarlarkValue.new("record"),
                            id,
                            TyUserParams(
                                matcher = TypeMatcherFactory.new(RecordTypeMatcher(id = id)),
                                fields =
                                    TyUserFields(
                                        known = fieldsTy,
                                        unknown = false,
                                    ),
                            ),
                        ).getOrThrow(),
                )

            val tyRecordType =
                Ty.custom(
                    TyUser
                        .new(
                            "record[$variableName]",
                            TyStarlarkValue.new("function"),
                            TypeInstanceId.gen(),
                            TyUserParams(
                                callable =
                                    TyCallable.new(
                                        ParamSpec.newParts(
                                            namedOnly =
                                                fields
                                                    .iter()
                                                    .map { (name, field) ->
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
                        ).getOrThrow(),
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
        // Kotlin: Use RecordTypeGen directly; frozen flag distinguishes.

        // impl RecordType::new(...)
        fun new(fields: SmallMap<String, Field>): RecordTypeGen =
            RecordTypeGen(
                id = TypeInstanceId.gen(),
                tyRecordData = null,
                fields = fields,
                frozen = false,
            )
    }
}

// Rust defines `RecordType<'v>` and `FrozenRecordType` type aliases.
// Kotlin call sites should use [RecordTypeGen] directly.

internal fun recordFields(x: RecordTypeGen): SmallMap<String, Field> = x.fields

// static RES: MethodsStatic = MethodsStatic::new();
private val recordTypeMethodsStatic = MethodsStatic()

// #[starlark_module]
private fun recordTypeMethods(methods: MethodsBuilder) {
    // #[starlark(attribute)]
    methods.setAttributeFn(
        name = "type",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.string(),
    ) { _, thisValue, heap ->
        val recordType =
            thisValue.downcastRef<RecordTypeGen>()
                ?: return@setAttributeFn Result.failure(IllegalStateException("Expected RecordTypeGen"))
        val tyRecordType = recordType.tyRecordData()
        val name =
            when {
                tyRecordType != null -> tyRecordType.name
                else -> RecordGen.TYPE
            }
        Result.success(heap.allocStr(name))
    }
}

// Tests are in commonTest, not here.
