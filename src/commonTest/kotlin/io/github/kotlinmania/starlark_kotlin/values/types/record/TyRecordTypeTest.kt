// port-lint: tests src/values/types/record/ty_record_type.rs (tests)
package io.github.kotlinmania.starlark_kotlin.values.types.record

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
import kotlin.test.Test

class TyRecordTypeTest {

    // #[test]
    // fn test_good()
    @Test
    fun testGood() {
        Assert.pass(
            """
MyRec = record(x = int)

def foo(x: MyRec): pass

foo(MyRec(x = 1))
            """.trimIndent(),
        )
    }

    // #[test]
    // fn test_fail_compile_time()
    @Test
    fun testFailCompileTime() {
        Assert.failGolden(
            "src/values/types/record/ty_record_type/fail_compile_time.golden",
            """
MyRec = record(x = int)
WrongRec = record(x = int)

def foo(x: MyRec): pass

def bar():
    foo(WrongRec(x = 1))
            """.trimIndent(),
        )
    }

    // #[test]
    // fn test_fail_runtime_time()
    @Test
    fun testFailRuntimeTime() {
        Assert.failGolden(
            "src/values/types/record/ty_record_type/fail_runtime_time.golden",
            """
MyRec = record(x = int)
WrongRec = record(x = int)

def foo(x: MyRec): pass

noop(foo)(WrongRec(x = 1))
            """.trimIndent(),
        )
    }

    // #[test]
    // fn test_record_instance_typechecker_ty()
    @Test
    fun testRecordInstanceTypecheckerTy() {
        Assert.pass(
            """
MyRec = record(x = int)
X = MyRec(x = 1)

def foo() -> MyRec:
    # This fails if record instance does not override typechecker_ty.
    return X
            """.trimIndent(),
        )
    }

    // #[test]
    // fn test_typecheck_field_pass()
    @Test
    fun testTypecheckFieldPass() {
        Assert.pass(
            """
MyRec = record(x = int, y = int)

def f(rec: MyRec) -> int:
    return rec.x + rec.y

assert_eq(f(MyRec(x = 1, y = 2)), 3)
            """.trimIndent(),
        )
    }

    // #[test]
    // fn test_typecheck_field_fail()
    @Test
    fun testTypecheckFieldFail() {
        Assert.failGolden(
            "src/values/types/record/ty_record_type/typecheck_field_fail.golden",
            """
MyRec = record(x = int, y = int)

def f(rec: MyRec) -> int:
    return rec.z
            """.trimIndent(),
        )
    }

    // #[test]
    // fn test_typecheck_record_type_call()
    @Test
    fun testTypecheckRecordTypeCall() {
        Assert.failGolden(
            "src/values/types/record/ty_record_type/typecheck_record_type_call.golden",
            """
MyRec = record(x = int)

def test():
    MyRec(x = "")
            """.trimIndent(),
        )
    }
}
