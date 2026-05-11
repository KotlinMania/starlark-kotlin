<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/typing/NeverTest.kt
// port-lint: source tests:src/values/typing/never.rs
package io.github.kotlinmania.starlark.values.typing
=======
<<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/int/InlineIntTest.kt
// port-lint: source tests:src/values/types/int/inlineInt.rs
package io.github.kotlinmania.starlark.values.types.int
========
// port-lint: tests src/values/typing/never.rs
package io.github.kotlinmania.starlark_kotlin.values.typing
>>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/typing/NeverTest.kt
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/typing/NeverTest.kt

/*
 * Copyright 2019 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/typing/NeverTest.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/typing/NeverTest.kt
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

<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/typing/NeverTest.kt
import io.github.kotlinmania.starlark.assert.isTrue
import io.github.kotlinmania.starlark.assert.pass
import kotlin.test.Test

class NeverTest {

    @Test
    fun testNeverRuntime() {
        isTrue("not isinstance(1, typing.Never)")
=======
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import kotlin.test.Test

internal class NeverTest {
    @Test
    fun testNeverRuntime() {
        Assert.isTrue("not isinstance(1, typing.Never)")
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/typing/NeverTest.kt
    }

    @Test
    fun testNeverCompileTime() {
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/typing/NeverTest.kt
        pass(
            """
def f() -> typing.Never:
    return fail()
"""
=======
        Assert.pass(
            """
def f() -> typing.Never:
    return fail()
""",
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/typing/NeverTest.kt
        )
    }
}
