package io.github.kotlinmania.starlark_kotlin.tests.derive

import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.values.EitherTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StringTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.types.int.i32StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever

internal object I32TypeRepr : StarlarkTypeRepr {
    override fun starlarkTypeRepr(): Ty = i32StarlarkTypeRepr()
}

internal fun EmptyEnum.Companion.starlark_type_repr(): Ty = StarlarkNever.starlarkTypeRepr()

internal fun JustInt.Companion.starlark_type_repr(): Ty = i32StarlarkTypeRepr()

internal fun IntOrStr.Companion.starlark_type_repr(): Ty =
    EitherTypeRepr(I32TypeRepr, StringTypeRepr).starlarkTypeRepr()

internal fun WithLifetime.Companion.starlark_type_repr(): Ty =
    EitherTypeRepr(I32TypeRepr, StringTypeRepr).starlarkTypeRepr()

internal fun TransparentIntOrStr.Companion.starlark_type_repr(): Ty = IntOrStr.starlark_type_repr()

internal fun JustInt.Companion.unpack_value(value: Value): Result<JustInt?> {
    val i = value.unpackI32()
    return Result.success(i?.let { JustInt.Int(it) })
}

internal fun IntOrStr.Companion.unpack_value(value: Value): Result<IntOrStr?> {
    val i = value.unpackI32()
    if (i != null) return Result.success(IntOrStr.Int(i))
    val s = value.unpackStr()
    return Result.success(s?.let { IntOrStr.Str(it) })
}

internal fun WithLifetime.Companion.unpack_value(value: Value): Result<WithLifetime?> {
    val i = value.unpackI32()
    if (i != null) return Result.success(WithLifetime.Int(i))
    val s = value.unpackStr()
    return Result.success(s?.let { WithLifetime.Str(it) })
}

internal fun TransparentIntOrStr.Companion.unpack_value(value: Value): Result<TransparentIntOrStr?> {
    val v = IntOrStr.unpack_value(value).getOrThrow() ?: return Result.success(null)
    return Result.success(TransparentIntOrStr(v))
}

