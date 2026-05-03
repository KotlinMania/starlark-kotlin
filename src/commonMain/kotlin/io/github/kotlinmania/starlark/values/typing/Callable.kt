// port-lint: source src/values/typing/callable.rs
package io.github.kotlinmania.starlark.values.typing

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not import this file except in compliance with the License.
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

import io.github.kotlinmania.starlark.assert.Assert
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.typing.ParamSpec
import io.github.kotlinmania.starlark.typing.Ty
import io.github.kotlinmania.starlark.typing.TyBasic
import io.github.kotlinmania.starlark.typing.TyCallable
import io.github.kotlinmania.starlark.values.AllocFrozenValue
import io.github.kotlinmania.starlark.values.AllocValue
import io.github.kotlinmania.starlark.values.layout.Freezer
import io.github.kotlinmania.starlark.values.layout.heap.FrozenHeap
import io.github.kotlinmania.starlark.values.layout.FrozenValue
import io.github.kotlinmania.starlark.values.layout.FrozenValueStarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkTypeRepr
import io.github.kotlinmania.starlark.values.StarlarkValue
import io.github.kotlinmania.starlark.values.UnpackValue
import io.github.kotlinmania.starlark.values.layout.Value
import io.github.kotlinmania.starlark.values.layout.avalues.simple.allocSimple
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import io.github.kotlinmania.starlark.values.layout.heap.Heap
import io.github.kotlinmania.starlark.values.typing.callable.StarlarkCallableParamAny
import io.github.kotlinmania.starlark.values.typing.callable.StarlarkCallableParamSpec
import io.github.kotlinmania.starlark.values.typing.typecompiled.TypeCompiled
import io.github.kotlinmania.starlark.values.types.list.UnpackList
import io.github.kotlinmania.starlark.values.types.list.UnpackListUnpackValue
import io.github.kotlinmania.starlark.values.types.none.NoneType

// Submodules:

internal class TypingCallable : StarlarkValue, AllocFrozenValue {

    override val TYPE: String get() = "typing.Callable"

    override fun toString(): String = TYPE

    override fun starlarkTypeRepr(): Ty {
        return StarlarkCallable.starlarkTypeRepr()
    }

    override fun evalType(): Ty? {
        return StarlarkCallable.starlarkTypeRepr()
    }

    override fun at2(index0: Value, index1: Value, heap: Heap): Result<Value> {
        val paramTypes = index0
        val ret = index1
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

    override fun allocFrozenValue(heap: FrozenHeap): FrozenValue {
        return heap.allocSimple(this)
    }
}

internal class TypingCallableAt2(
    val callable: TyCallable,
) : StarlarkValue {

    override val TYPE: String get() = "typing.Callable"

    override fun toString(): String = callable.toString()

    override fun evalType(): Ty? {
        return Ty.basic(TyBasic.Callable(callable))
    }
}

/**
 * Marker for a callable value. Can be used in function signatures
 * for better documentation and type checking.
 */
class StarlarkCallable<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: Value,
) : StarlarkTypeRepr, UnpackValue<StarlarkCallable<P, R>>, AllocValue {

    companion object {
        /** Wrap the value. */
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> uncheckedNew(value: Value): StarlarkCallable<P, R> {
            return StarlarkCallable(value)
        }

        fun starlarkTypeRepr(): Ty {
            return Ty.callable(StarlarkCallableParamAny.params(), FrozenValueStarlarkTypeRepr.starlarkTypeRepr())
        }

        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> starlarkTypeRepr(
            paramSpec: P,
            returnTypeRepr: R,
        ): Ty {
            return Ty.callable(paramSpec.params(), returnTypeRepr.starlarkTypeRepr())
        }
    }

    /** Convert to `FrozenValue` version. */
    fun unpackFrozen(): FrozenStarlarkCallable<P, R>? {
        val frozen = value.unpackFrozen() ?: return null
        return FrozenStarlarkCallable.uncheckedNew(frozen)
    }

    /** Erase parameter and return types. */
    fun erase(): StarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> {
        return uncheckedNew(value)
    }

    override fun starlarkTypeRepr(): Ty {
        return Companion.starlarkTypeRepr()
    }

    override fun unpackValueImpl(value: Value): Result<StarlarkCallable<P, R>?> {
        return if (value.vtable().hasInvoke) {
            Result.success(uncheckedNew(value))
        } else {
            Result.success(null)
        }
    }

    override fun allocValue(_heap: Heap): Value {
        return value
    }
}

/** Marker for a callable value. */
class FrozenStarlarkCallable<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: FrozenValue,
) : StarlarkTypeRepr, AllocFrozenValue {

    companion object {
        /** Wrap the value. */
        fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> uncheckedNew(value: FrozenValue): FrozenStarlarkCallable<P, R> {
            return FrozenStarlarkCallable(value)
        }
    }

    override fun toString(): String = "FrozenStarlarkCallable($value)"

    /** Erase parameter and return types. */
    fun erase(): FrozenStarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> {
        return uncheckedNew(value)
    }

    override fun starlarkTypeRepr(): Ty {
        return StarlarkCallable.starlarkTypeRepr()
    }

    override fun allocFrozenValue(_heap: FrozenHeap): FrozenValue {
        return value
    }

    /** Convert to `Value`-version. */
    fun toCallable(): StarlarkCallable<P, R> {
        return StarlarkCallable.uncheckedNew(value.toValue())
    }
}

fun <P : StarlarkCallableParamSpec, R : StarlarkTypeRepr> StarlarkCallable<P, R>.freeze(
    freezer: Freezer,
): Result<FrozenStarlarkCallable<P, R>> {
    val frozenValue = freezer.freeze(value).getOrElse { return Result.failure(it) }
    return Result.success(FrozenStarlarkCallable.uncheckedNew(frozenValue))
}

/**
 * More strict version of [`StarlarkCallable`].
 *
 * This checks not only that the value is callable,
 * but also that it is a callable with the correct signature.
 *
 * The implementation uses starlark-rust typechecker with all its limitations.
 * For example, if there are optional parameters in both value-def and this signature,
 * signature matching is ignored at the time of writing.
 *
 * Unpacking with this type is expensive:
 * usually it is OK to import it for code executed once at top-level scope (like `rule()`),
 * but not for code executed many times (like `partial()`).
 */
class StarlarkCallableChecked<P : StarlarkCallableParamSpec, R : StarlarkTypeRepr>(
    val value: Value,
) : StarlarkTypeRepr, UnpackValue<StarlarkCallableChecked<P, R>>, AllocValue {

    override fun toString(): String = "StarlarkCallableChecked($value)"

    /** Convert to [`StarlarkCallable`]. */
    fun toUnchecked(): StarlarkCallable<P, R> {
        return StarlarkCallable.uncheckedNew(value)
    }

    override fun starlarkTypeRepr(): Ty {
        return StarlarkCallable.starlarkTypeRepr()
    }

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

    override fun allocValue(_heap: Heap): Value {
        return value
    }
}

private fun myModule(globals: GlobalsBuilder) {
    globals.setFunction("accept_f") { args: Arguments, eval: Evaluator ->
        Result.success(NoneType)
    }
}

internal fun testCallableRuntime() {
    Assert.isTrue("isinstance(lambda: None, typing.Callable)")
    Assert.isTrue("isinstance(len, typing.Callable)")
    Assert.isTrue("Rec = record(); isinstance(Rec, typing.Callable)")
    Assert.isFalse("isinstance(37, typing.Callable)")
}

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

internal fun testCallableCheckedRuntime() {
    fun checkedModule(globals: GlobalsBuilder) {
        globals.setFunction("accept_f") { args: Arguments, eval: Evaluator ->
            Result.success(NoneType)
        }

        globals.setFunction("good") { args: Arguments, eval: Evaluator ->
            Result.success(NoneType)
        }

        globals.setFunction("bad") { args: Arguments, eval: Evaluator ->
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

/**
 * Runtime regression guard verifying [FrozenStarlarkCallable] survives
 * transfer across coroutine workers. Ships an instance through a
 * [kotlinx.coroutines.channels.Channel] across coroutine dispatchers and
 * verifies the wrapped [FrozenValue] equals its original after the round
 * trip. Throws if the wrapped value changes identity under transfer.
 */
internal fun assertSyncSend() {
    kotlinx.coroutines.runBlocking {
        val callable: FrozenStarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr> =
            FrozenStarlarkCallable.uncheckedNew(
                io.github.kotlinmania.starlark.values.layout.FrozenValue.newNone()
            )

        val channel = kotlinx.coroutines.channels.Channel<
            FrozenStarlarkCallable<StarlarkCallableParamAny, StarlarkTypeRepr>
        >(1)
        val received = async(kotlinx.coroutines.Dispatchers.Default) {
            val c = channel.receive()
            c.value
        }
        launch(kotlinx.coroutines.Dispatchers.Default) { channel.send(callable) }
        val receivedValue = received.await()
        channel.close()

        check(callable.value == receivedValue) {
            "FrozenStarlarkCallable did not survive Channel round-trip"
        }
    }
}
