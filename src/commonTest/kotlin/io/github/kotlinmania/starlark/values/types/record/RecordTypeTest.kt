// port-lint: source tests:src/values/types/record/record_type.rs
package io.github.kotlinmania.starlark.values.types.record

/*
 * Copyright 2018 The Starlark in Rust Authors.
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
import io.github.kotlinmania.starlark.assert.failGolden
import kotlin.test.Test

class RecordTypeTest {
    @Test
    fun testRecordTypeAsTypePass() {
        Assert.pass(
            """
RecPass = record(a = field(int), b = field(int))

def f_pass(x: RecPass):
    return x.a

f_pass(RecPass(a = 1, b = 2))
""",
        )
    }

    @Test
    fun testRecordTypeAsTypeCompileTime() {
        Assert.failGolden(
            "src/values/types/record/record_type/record_type_as_type_compile_time.golden",
            """
RecFailCt1 = record(a = field(int), b = field(int))
RecFailCt2 = record(a = field(int), b = field(int))

def f_fail_ct(x: RecFailCt1):
    return x.a

def test():
    f_fail_ct(RecFailCt2(a = 1, b = 2))
""",
        )
    }

    @Test
    fun testRecordTypeAsTypeRuntime() {
        Assert.failGolden(
            "src/values/types/record/record_type/record_type_as_type_runtime.golden",
            """
RecFailRt1 = record(a = field(int), b = field(int))
RecFailRt2 = record(a = field(int), b = field(int))

def f_fail_rt(x: RecFailRt1):
    return x.a

noop(f_fail_rt)(RecFailRt2(a = 1, b = 2))
""",
        )
    }

    @Test
    fun testAnonRecord() {
        Assert.failGolden(
            "src/values/types/record/record_type/anon_record.golden",
            "record(a = field(int))(a = 1)",
        )
    }

    @Test
    fun testMissingFieldError() {
        Assert.failGolden(
            "src/values/types/record/record_type/missing_field_error.golden",
            """
RecFail = record(a = field(int), b = field(int))

_x = RecFail(a = 1)
""",
        )
    }
}
