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

import io.github.kotlinmania.starlark_kotlin.collections.Hashed
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
import io.github.kotlinmania.starlark_kotlin.values.layout.Freezer
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.convertIndex
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.ValueTyped
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocListIter
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.StringValue
import io.github.kotlinmania.starlark_kotlin.values.types.TypeInstanceId
import io.github.kotlinmania.starlark_kotlin.values.types.dict.ValueStr
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.EnumTypeMatcher
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.TyEnumData
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.EnumValueGen
import io.github.kotlinmania.starlark_kotlin.values.types.enumeration.value.EnumValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeMatcherFactory

private sealed class EnumError(message: String) : Exception(message) {
    class DuplicateEnumValue(value: String) :
        EnumError("enum values must all be distinct, but repeated `$value`")

    class InvalidElement(element: String, enumType: String) :
        EnumError("Unknown enum element `$element`, given to `$enumType`")
}

/**
 * The type of an enumeration, created by `enum()`.
 *
 * Deliberately stores fully populated values for each entry,
 * so we can produce enum values with zero allocation.
 */
class EnumTypeGen internal constructor(
    internal val id: TypeInstanceId,
    internal var tyEnumData: TyEnumData?,
    // The key is the value of the enumeration
    // The value is a value of type EnumValue
    private val elements: SmallMap<Value, Value>,
    private val frozen: Boolean,
) : StarlarkValue, AllocValue, Freeze<EnumTypeGen> {

    override val TYPE: String get() = FUNCTION_TYPE
    override val HAS_invoke: Boolean get() = true
    override val HAS_eval_type: Boolean get() = true
    override val HAS_iterate: Boolean get() = true

    private var tyEnumDataInitialized: Boolean = tyEnumData != null

    override fun allocValue(heap: Heap): Value {
        return heap.allocSimple(this)
    }

    override fun starlarkTypeRepr(): Ty = Ty.any()

    override fun toString(): String {
        return "enum(${elements().keys().joinToString(", ")})"
    }

    override fun freeze(freezer: Freezer): Result<EnumTypeGen> {
        val frozenElements = SmallMap.new<Value, Value>()
        for ((hashedKey, v) in elements.iterHashed()) {
            val frozenKey = freezer.freeze(hashedKey.key())
            if (frozenKey.isFailure) return Result.failure(frozenKey.exceptionOrNull()!!)
            val frozenVal = freezer.freeze(v)
            if (frozenVal.isFailure) return Result.failure(frozenVal.exceptionOrNull()!!)
            val newHashedKey = Hashed.newUnchecked(hashedKey.hash(), frozenKey.getOrThrow().toValue())
            frozenElements.insertHashedUniqueUnchecked(newHashedKey, frozenVal.getOrThrow().toValue())
        }
        return Result.success(EnumTypeGen(
            id = id,
            tyEnumData = tyEnumData,
            elements = frozenElements,
            frozen = true,
        ))
    }

    internal fun getOrInitTy(f: () -> TyEnumData) {
        if (frozen) {
            return
        }
        if (!tyEnumDataInitialized) {
            tyEnumData = f()
            tyEnumDataInitialized = true
        }
    }

    internal fun getTy(): TyEnumData? = tyEnumData

    fun elements(): SmallMap<Value, Value> = elements

    fun tyEnumData(): TyEnumData? = getTy()

    fun construct(value: Value): Value {
        val hashed = value.getHashed().getOrThrow()
        return elements().getHashedByValue(hashed)
            ?: throw EnumError.InvalidElement(value.toStr(), toString())
    }

    override fun invoke(_me: Value, args: Arguments, eval: Evaluator): Result<Value> {
        args.noNamedArgs().getOrElse { return Result.failure(it) }
        val v = args.positional1(eval.heap()).getOrElse { return Result.failure(it) }
        return Result.success(construct(v))
    }

    override fun getAttr(attribute: String, heap: Heap): Value? {
        return elements().get(ValueStr(attribute))
    }

    override fun dirAttr(): List<String> {
        return elements().keys().map { it.unpackStr()!! }.toList()
    }

    override fun length(): Result<Int> = Result.success(elements().len())

    override fun at(index: Value, heap: Heap): Result<Value> {
        val i = convertIndex(index, elements().len()).getOrElse { return Result.failure(it) }
        return Result.success(elements().getIndex(i)!!.second)
    }

    override fun iterate(me: Value, heap: Heap): Result<Value> = Result.success(me)

    override fun iterSizeHint(index: Int): Pair<Int, Int?> {
        val rem = elements().len() - index
        return Pair(rem, rem)
    }

    override fun iterNext(index: Int, heap: Heap): Value? {
        val values = elements().values().toList()
        return if (index < values.size) values[index] else null
    }

    override fun iterStop() {}

    override fun getMethods(): Methods? {
        return enumTypeMethodsStatic.methods(::enumTypeMethods)
    }

    override fun evalType(): Ty? {
        return tyEnumData()?.tyEnumValue
    }

    override fun typecheckerTy(): Ty? {
        return tyEnumData()?.tyEnumType
    }

    override fun exportAs(variableName: String, eval: Evaluator): Result<Unit> {
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
            val fieldsMap = linkedMapOf<String, Ty>().apply {
                for (key in elements().keys()) {
                    put(key.unpackStr()!!, tyEnumValue)
                }
            }

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
        return Result.success(Unit)
    }

    companion object {
        private const val FUNCTION_TYPE = "function"

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

/** Unfrozen enum type. */
typealias EnumType = EnumTypeGen
/** Frozen enum type. */
typealias FrozenEnumType = EnumTypeGen

private val enumTypeMethodsStatic = MethodsStatic()

private fun enumTypeMethods(builder: MethodsBuilder) {
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

    builder.setAttributeFn(
        name = "values",
        speculativeExecSafe = true,
        docstring = null,
        typ = Ty.anyList(),
    ) { _, thisValue, heap ->
        val enumTypeGen = thisValue.downcastRef<EnumTypeGen>()!!
        val valuesList = enumTypeGen.elements().keys().toList()
        Result.success(heap.allocListIter(valuesList))
    }
}
