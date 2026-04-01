// port-lint: tests src/values/typing/type_compiled/tests.rs
package io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled

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
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

// #[test]
// fn test_types()
internal fun testTypes() {
    val a = Assert()
    a.isTrue(
        """
def f(i: int) -> bool:
    return i == 3
f(8) == False""",
    )

    // If the types are either malformed or runtime errors, it should fail
    a.fail("def f(i: made_up):\n pass", "Variable")
    a.fail(
        "def f(i: fail('bad')):\n pass",
        "call expression is not allowed in type expression",
    )

    // Type errors should be caught in arguments
    a.fail(
        "def f_runtime(i: bool):\n pass\nf_runtime(noop(1))",
        "Value `1` of type `int` does not match the type annotation `bool` for argument `i`",
    )
    a.fail(
        """
def f_compile_time(i: bool):
    pass
def g():
    f_compile_time(1)
""",
        "Expected type `bool` but got `int`",
    )
    a.pass(
        """Foo = record(value=int)
def f(v: bool) -> Foo:
    return Foo(value=1)""",
    )
    // Type errors should be caught in return positions
    a.fail(
        "def f_return_runtime() -> bool:\n return noop(1)\nf_return_runtime()",
        "Value `1` of type `int` does not match the type annotation `bool` for return type",
    )
    a.fail(
        """
def f_return_compile_time() -> bool:
    return 1
def g():
    f_return_compile_time()
""",
        "Expected type `bool` but got `int`",
    )
    // And for functions without return
    a.fail(
        "def f_bool_none() -> bool:\n pass\nf_bool_none()",
        "Value `None` of type `NoneType` does not match the type annotation `bool` for return type",
    )
    // And for functions that return None implicitly or explicitly
    a.fails(
        "def f_none_bool_runtime() -> None:\n return noop(True)\nf_none_bool_runtime()",
        listOf("type annotation", "`None`", "`bool`", "return"),
    )
    a.fail(
        """
def f_none_bool_compile_time() -> None:
    return True
def g():
    f_none_bool_compile_time()
""",
        "Expected type `None` but got `bool`",
    )
    a.pass("def f() -> None:\n pass\nf()")

    // The following are all valid types
    a.allTrue(
        """
isinstance(1, int)
isinstance(True, bool)
isinstance(True, typing.Any)
isinstance(None, None)
isinstance(assert_type, typing.Callable)
isinstance([], list[int])
isinstance([], list[typing.Any])
isinstance([1, 2, 3], list[int])
isinstance(None, [None, int])
isinstance('test', int | str)
isinstance(('test', None), (str, None))
isinstance({"test": 1, "more": 2}, dict[str, int])
isinstance({1: 1, 2: 2}, dict[int, int])

not isinstance(1, None)
not isinstance((1, 1), str)
not isinstance('test', int | bool)
not isinstance([1,2,None], list[int])
not isinstance({"test": 1, 8: 2}, dict[str, int])
not isinstance({"test": 1, "more": None}, dict[str, int])

isinstance(1, typing.Any)
isinstance([1,2,"test"], list)
""",
    )

    // Checking types fails for invalid types
    a.fail("isinstance(None, isinstance)", "not a valid type")
    a.fail("isinstance(None, [])", "cannot be used as type")
    a.fail(
        "isinstance(None, {'1': '', '2': ''})",
        "cannot be used as type",
    )

    // Should check the type of default parameters that aren't used
    a.fail(
        """
def foo(f: int = None):
    pass
""",
        "`None` of type `NoneType` does not match the type annotation `int`",
    )
}

// #[test]
// fn test_new_syntax_without_dot_type_compile_time()
internal fun testNewSyntaxWithoutDotTypeCompileTime() {
    Assert.pass("def f() -> int: return 17")
    Assert.fail(
        """
def f() -> int: return 'tea'
""",
        "Expected type `int` but got `str`",
    )
}

// #[test]
// fn test_new_syntax_without_dot_type_runtime()
internal fun testNewSyntaxWithoutDotTypeRuntime() {
    Assert.pass(
        """
def f() -> str: return noop('coke')
f()
""",
    )
    Assert.fail(
        """
def f() -> str: return noop(19)
f()
""",
        "Value `19` of type `int`",
    )
}

// #[test]
// fn test_type_compiled_display()
internal fun testTypeCompiledDisplay() {
    fun t(expected: String, ty0: String) {
        Heap.temp { heap ->
            val ty = Assert.pass(ty0)
            val tyValue = ty.uncheckedFrozenValue().toValue()
            val compiled = TypeCompiled.new(tyValue, heap)!!
            check(expected == compiled.toString()) { "for `$ty0`" }
        }
    }

    t("typing.Any", "typing.Any")
    t("list", "list")
    t("list", "list[typing.Any]")
    t("None", "None")
}

// #[test]
// fn test_type_compiled_starlark_api()
internal fun testTypeCompiledStarlarkApi() {
    Assert.eq("\"int\"", "repr(eval_type(int))")
    Assert.eq("\"int | str\"", "repr(eval_type(int | str))")
    Assert.isTrue("eval_type(int).matches(1)")
    Assert.isTrue("not eval_type(int).matches([])")
    Assert.pass("eval_type(int).check_matches(1)")
    Assert.fail(
        "eval_type(int).check_matches([])",
        "Value of type `list` does not match type `int`: []",
    )
}

// #[test]
// fn test_eval_type_eval_type()
internal fun testEvalTypeEvalType() {
    Assert.isTrue("isinstance(1, eval_type(eval_type(int)))")
}

// #[test]
// fn test_type_compiled_can_be_used_in_function_signature()
internal fun testTypeCompiledCanBeUsedInFunctionSignature() {
    Assert.pass(
        """
ty = eval_type(int)
def f(x: ty):
    pass

f(1)
""",
    )
    Assert.fail(
        """
ty = eval_type(int)
def f(x: ty):
    pass

# Runtime error.
f(noop("x"))
""",
        "Value `x` of type `string` does not match the type annotation `int` for argument `x`",
    )
    Assert.fail(
        """
ty = eval_type(int)
def f(x: ty):
    pass

def g():
    # Compile-time error.
    f("x")
""",
        "Expected type `int` but got `str`",
    )
}

// #[test]
// fn test_isinstance()
internal fun testIsinstance() {
    Assert.eq("True", "isinstance(1, int)")
    Assert.eq("False", "isinstance(1, str)")
}

// #[test]
// fn test_new_list_dict_syntax_pass()
internal fun testNewListDictSyntaxPass() {
    Assert.pass(
        """
def uuu(x: list[int]):
    pass

uuu([1, 2, 3])
""",
    )
}

// #[test]
// fn test_new_list_dict_syntax_fail_compile_time()
internal fun testNewListDictSyntaxFailCompileTime() {
    Assert.fail(
        """
def uuu(x: list[int]):
    pass

def www():
    uuu(["mm"])
""",
        "Expected type `list[int]` but got `list[str]`",
    )
}

// #[test]
// fn test_new_list_dict_syntax_fail_runtime()
internal fun testNewListDictSyntaxFailRuntime() {
    Assert.fail(
        """
def uuu(x: list[int]):
    pass

noop(uuu)(["mm"])
""",
        """Value `["mm"]` of type `list` does not match""",
    )
}

// #[test]
// fn test_bit_or()
internal fun testBitOr() {
    val types = listOf(
        Pair("int", "17"),
        Pair("str", "'x'"),
        Pair("None", "None"),
        Pair("list", "[]"),
        Pair("(str | int)", "19"),
    )
    for ((at, av) in types) {
        for ((bt, bv) in types) {
            Assert.isTrue("isinstance($av, $at | $bt)")
            Assert.isTrue("isinstance($bv, $at | $bt)")
            Assert.isTrue("not isinstance(range(10), $at | $bt)")
        }
    }
}
