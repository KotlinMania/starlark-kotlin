// port-lint: source src/values/typing/callable.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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

import io.github.kotlinmania.starlark_kotlin.assert.Assert
import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Arguments
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.typing.ParamSpec
import io.github.kotlinmania.starlark_kotlin.typing.Ty
import io.github.kotlinmania.starlark_kotlin.typing.TyBasic
import io.github.kotlinmania.starlark_kotlin.typing.TyCallable
import io.github.kotlinmania.starlark_kotlin.values.AllocFrozenValue
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.Freezer
import io.github.kotlinmania.starlark_kotlin.values.FrozenHeap
import io.github.kotlinmania.starlark_kotlin.values.FrozenValue
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenValueStarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.UnpackValue
import io.github.kotlinmania.starlark_kotlin.values.freeze_error.FreezeResult
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.simple.allocSimple
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.typing.callable.StarlarkCallableParamAny
import io.github.kotlinmania.starlark_kotlin.values.typing.callable.StarlarkCallableParamSpec
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.TypeCompiled
import io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackList
import io.github.kotlinmania.starlark_kotlin.values.types.list.UnpackListUnpackValue
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType

// Submodules:
// pub(crate) mod param -> callable.param (Param.kt)

// #[derive(Debug, Display, Allocative, ProvidesStaticType, NoSerialize)]
// pub(crate) struct TypingCallable
internal class TypingCallable : StarlarkValue, AllocFrozenValue {

    // #[starlark_value(type = "typing.Callable")]
    override val TYPE: String get() = "typing.Callable"

    override fun toString(): String = TYPE

    // impl StarlarkTypeRepr (required by AllocFrozenValue)
    override fun starlarkTypeRepr(): Ty {
        return StarlarkCallable.starlarkTypeRepr()
    }

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return StarlarkCallable.starlarkTypeRepr()
    }

    // fn at2(&self, param_types: Value<'v>, ret: Value<'v>, heap: Heap<'v>, _private: Private) -> crate::Result<Value<'v>>
    override fun at2(paramTypes: Value, ret: Value, heap: Heap): Result<Value> {
        return runCatching {
            val unpacker = UnpackListUnpackValue<Value>(
                object : UnpackValue<Value> {
                    override fun starlarkTypeRepr(): Ty = Ty.any()
                    override fun unpackValueImpl(value: Value): Result<Value?> = Result.success(value)
                }
            )
            val paramTypesList = unpacker.unpackValueErr(paramTypes)
            val retTy = TypeCompiled.new(ret, heap).asTy()
            val paramTys = mutableListOf<Ty>()
            for (p in paramTypesList.items) {
                val ty = TypeCompiled.new(p, heap).asTy()
                paramTys.add(ty)
            }

            heap.allocSimple(
                TypingCallableAt2(
                    callable = TyCallable.new(ParamSpec.posOnly(paramTys, emptyList()), retTy),
                )
            )
        }
    }

    // impl AllocFrozenValue for TypingCallable
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }
}

// #[derive(Allocative, Debug, ProvidesStaticType, NoSerialize, Display)]
// pub(crate) struct TypingCallableAt2
internal class TypingCallableAt2(
    val callable: TyCallable,
) : StarlarkValue {

    // #[starlark_value(type = "typing.Callable")]
    override val TYPE: String get() = "typing.Callable"

    override fun toString(): String = callable.toString()

    // fn eval_type(&self) -> Option<Ty>
    override fun evalType(): Ty? {
        return Ty.basic(TyBasic.Callable(callable))
    }
}

/// Marker for a callable value. Can be used in function signatures
/// for better documentation and type checking.
// pub struct StarlarkCallable<'v, P: StarlarkCallableParamSpec, R: StarlarkTypeRepr>
class StarlarkCallable<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: Value,
) : StarlarkTypeRepr, UnpackValue<StarlarkCallable<P, R>>, AllocValue {

    companion object {
        /// Wrap the value.
        // pub fn unchecked_new(value: Value<'v>) -> Self
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> uncheckedNew(value: Value): StarlarkCallable<P, R> {
            return StarlarkCallable(value)
        }

        // fn starlark_type_repr() -> Ty
        fun starlarkTypeRepr(): Ty {
            return Ty.callable(StarlarkCallableParamAny.params(), FrozenValueStarlarkTypeRepr.starlarkTypeRepr())
        }

        // fn starlark_type_repr() -> Ty  (parameterized)
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> starlarkTypeRepr(
            paramSpec: P,
            returnTypeRepr: R,
        ): Ty {
            return Ty.callable(paramSpec.params(), returnTypeRepr.starlarkTypeRepr())
        }
    }

    /// Convert to `FrozenValue` version.
    // pub fn unpack_frozen(self) -> Option<FrozenStarlarkCallable<P, R>>
    fun unpackFrozen(): FrozenStarlarkCallable<P, R>? {
        val frozen = value.unpackFrozen() ?: return null
        return FrozenStarlarkCallable.uncheckedNew(frozen)
    }

    /// Erase parameter and return types.
    // pub fn erase(self) -> StarlarkCallable<'v>
    fun erase(): StarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> {
        return uncheckedNew(value)
    }

    // impl StarlarkTypeRepr for StarlarkCallable
    override fun starlarkTypeRepr(): Ty {
        return Companion.starlarkTypeRepr()
    }

    // impl UnpackValue for StarlarkCallable
    // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
    override fun unpackValueImpl(value: Value): Result<StarlarkCallable<P, R>?> {
        return if (value.vtable().hasInvoke) {
            Result.success(uncheckedNew(value))
        } else {
            Result.success(null)
        }
    }

    // impl AllocValue for StarlarkCallable
    override fun allocValue(heap: Heap): Value {
        return value
    }
}

/// Marker for a callable value.
// pub struct FrozenStarlarkCallable<P: StarlarkCallableParamSpec, R: StarlarkTypeRepr>
class FrozenStarlarkCallable<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: FrozenValue,
) : StarlarkTypeRepr, AllocFrozenValue {

    companion object {
        /// Wrap the value.
        // pub fn unchecked_new(value: FrozenValue) -> Self
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> uncheckedNew(value: FrozenValue): FrozenStarlarkCallable<P, R> {
            return FrozenStarlarkCallable(value)
        }
    }

    override fun toString(): String = "FrozenStarlarkCallable($value)"

    /// Erase parameter and return types.
    // pub fn erase(self) -> FrozenStarlarkCallable
    fun erase(): FrozenStarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> {
        return uncheckedNew(value)
    }

    // impl StarlarkTypeRepr for FrozenStarlarkCallable
    override fun starlarkTypeRepr(): Ty {
        return StarlarkCallable.starlarkTypeRepr()
    }

    // impl AllocFrozenValue for FrozenStarlarkCallable
    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return value
    }

    /// Convert to `Value`-version.
    // pub fn to_callable<'v>(self) -> StarlarkCallable<'v, P, R>
    fun toCallable(): StarlarkCallable<P, R> {
        return StarlarkCallable.uncheckedNew(value.toValue())
    }
}

// impl Freeze for StarlarkCallable
fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> StarlarkCallable<P, R>.freeze(
    freezer: Freezer,
): FreezeResult<FrozenStarlarkCallable<P, R>> {
    val frozenValue = freezer.freeze(value).getOrElse { return Result.failure(it) }
    return Result.success(FrozenStarlarkCallable.uncheckedNew(frozenValue))
}

/// More strict version of [`StarlarkCallable`].
///
/// This checks not only that the value is callable,
/// but also that it is a callable with the correct signature.
///
/// The implementation uses starlark-rust typechecker with all its limitations.
/// For example, if there are optional parameters in both value-def and this signature,
/// signature matching is ignored at the time of writing.
///
/// Unpacking with this type is expensive:
/// usually it is OK to use it for code executed once at top-level scope (like `rule()`),
/// but not for code executed many times (like `partial()`).
// pub struct StarlarkCallableChecked<'v, P: StarlarkCallableParamSpec, R: StarlarkTypeRepr>
class StarlarkCallableChecked<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: Value,
) : StarlarkTypeRepr, UnpackValue<StarlarkCallableChecked<P, R>>, AllocValue {

    override fun toString(): String = "StarlarkCallableChecked($value)"

    /// Convert to [`StarlarkCallable`].
    // pub fn to_unchecked(self) -> StarlarkCallable<'v, P, R>
    fun toUnchecked(): StarlarkCallable<P, R> {
        return StarlarkCallable.uncheckedNew(value)
    }

    // impl StarlarkTypeRepr for StarlarkCallableChecked
    override fun starlarkTypeRepr(): Ty {
        return StarlarkCallable.starlarkTypeRepr()
    }

    // impl UnpackValue for StarlarkCallableChecked
    // fn unpack_value_impl(value: Value<'v>) -> Result<Option<Self>, Self::Error>
    override fun unpackValueImpl(value: Value): Result<StarlarkCallableChecked<P, R>?> {
        // Check it is a callable first.
        if (!value.vtable().hasInvoke) {
            return Result.success(null)
        }

        // We need generic statics to cache this.
        val ty = StarlarkCallable.starlarkTypeRepr()

        return Ty.ofValue(value).checkIntersects(ty).map { intersects ->
            if (intersects) StarlarkCallableChecked(value) else null
        }
    }

    // impl AllocValue for StarlarkCallableChecked
    override fun allocValue(heap: Heap): Value {
        return value
    }
}

// #[cfg(test)]
// mod tests

// #[starlark_module]
// fn my_module(globals: &mut GlobalsBuilder)
private fun myModule(globals: GlobalsBuilder) {
    // fn accept_f(_x: StarlarkCallable<(String,), i32>) -> anyhow::Result<NoneType>
    globals.setFunction("accept_f") { _args: Arguments, _eval: Evaluator ->
        Result.success(NoneType)
    }
}

// #[test]
// fn test_callable_runtime()
internal fun testCallableRuntime() {
    Assert.isTrue("isinstance(lambda: None, typing.Callable)")
    Assert.isTrue("isinstance(len, typing.Callable)")
    Assert.isTrue("Rec = record(); isinstance(Rec, typing.Callable)")
    Assert.isFalse("isinstance(37, typing.Callable)")
}

// #[test]
// fn test_callable_pass_compile_time()
internal fun testCallablePassCompileTime() {
    Assert.pass(
        """
Rec = record()

def foo(x: typing.Callable):
    pass

def bar():
    foo(len)
    foo(lambda x: 1)
    foo(Rec)
""",
    )
}

// #[test]
// fn test_callable_fail_compile_time()
internal fun testCallableFailCompileTime() {
    Assert.fail(
        """
def foo(x: typing.Callable):
    pass

def bar():
    foo(1)
""",
        "Expected type",
    )
}

// #[test]
// fn test_native_callable_pass()
internal fun testNativeCallablePass() {
    val a = Assert()
    a.globalsAdd(::myModule)
    a.pass(
        """
def f(x: str) -> int:
    return len(x)

def test():
    accept_f(f)
""",
    )
}

// #[test]
// fn test_native_callable_fail_compile_time_wrong_param_type()
internal fun testNativeCallableFailCompileTimeWrongParamType() {
    val a = Assert()
    a.globalsAdd(::myModule)
    a.fail(
        """
def f(x: list) -> int:
    return 1

def test():
    accept_f(f)
""",
        "Expected type `typing.Callable[[str], int]` but got",
    )
}

// #[test]
// fn test_native_callable_fail_compile_time_wrong_param_count()
internal fun testNativeCallableFailCompileTimeWrongParamCount() {
    val a = Assert()
    a.globalsAdd(::myModule)
    a.fail(
        """
def f() -> int:
    return 1

def test():
    accept_f(f)
""",
        "Expected type `typing.Callable[[str], int]` but got",
    )
}

// #[test]
// fn test_typing_callable_pass()
internal fun testTypingCallablePass() {
    val a = Assert()
    a.pass(
        """
def accept_f(x: typing.Callable[[str], int]) -> None:
    pass

def f(x: str) -> int:
    return len(x)

def test():
    accept_f(f)
""",
    )
}

// #[test]
// fn test_typing_callable_fail_compile_time_wrong_param_type()
internal fun testTypingCallableFailCompileTimeWrongParamType() {
    val a = Assert()
    a.fail(
        """
def accept_f(x: typing.Callable[[str], int]) -> None:
    pass

def f(x: list) -> int:
    return 1

def test():
    accept_f(f)
""",
        "Expected type `typing.Callable[[str], int]` but got",
    )
}

// #[test]
// fn test_typing_callable_fail_compile_time_wrong_param_count()
internal fun testTypingCallableFailCompileTimeWrongParamCount() {
    val a = Assert()
    a.fail(
        """
def accept_f(x: typing.Callable[[str], int]) -> None:
    pass

def f() -> int:
    return 1

def test():
    accept_f(f)
""",
        "Expected type `typing.Callable[[str], int]` but got",
    )
}

// #[test]
// fn test_callable_checked_runtime()
internal fun testCallableCheckedRuntime() {
    // #[starlark_module]
    // fn module(globals: &mut GlobalsBuilder)
    fun checkedModule(globals: GlobalsBuilder) {
        // fn accept_f(_f: StarlarkCallableChecked<(), NoneType>) -> anyhow::Result<NoneType>
        globals.setFunction("accept_f") { _args: Arguments, _eval: Evaluator ->
            Result.success(NoneType)
        }

        // fn good() -> anyhow::Result<NoneType>
        globals.setFunction("good") { _args: Arguments, _eval: Evaluator ->
            Result.success(NoneType)
        }

        // fn bad() -> anyhow::Result<i32>
        globals.setFunction("bad") { _args: Arguments, _eval: Evaluator ->
            Result.success(10)
        }
    }

    val a = Assert()
    a.globalsAdd(::checkedModule)

    a.pass("accept_f(good)")

    a.fail(
        """
def test():
    x = noop(bad) # Hide the type from static typechecker.
    accept_f(x)

test()
        """,
        "Type of parameter `_f` doesn't match",
    )
}
