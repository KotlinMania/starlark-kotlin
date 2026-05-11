<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/float/FloatTest.kt
// port-lint: source tests:src/values/types/float/float.rs
package io.github.kotlinmania.starlark.values.types.float
=======
// port-lint: tests src/values/types/float/float.rs
package io.github.kotlinmania.starlark_kotlin.values.types.float
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/float/FloatTest.kt

/*
 * Copyright 2018 The Starlark in Rust Authors.
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Copyright (c) 2025 Sydney Renee, The Solace Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/float/FloatTest.kt
 * you may not import this file except in compliance with the License.
=======
 * you may not use this file except in compliance with the License.
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/float/FloatTest.kt
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

<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/float/FloatTest.kt
import io.github.kotlinmania.starlark.assert.Assert
import kotlin.test.Test

class FloatTest {
=======
import io.github.kotlinmania.starlark_kotlin.assert.Assert
import kotlin.math.E
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

private fun nonFinite(f: Double): String {
    val buffer = StringBuilder()
    writeNonFinite(buffer, f)
    return buffer.toString()
}

@Suppress("SameParameterValue")
private fun decimal(f: Double): String {
    val buffer = StringBuilder()
    writeDecimal(buffer, f)
    return buffer.toString()
}

private fun scientific(f: Double): String {
    val buffer = StringBuilder()
    writeScientific(buffer, f, 'e', false)
    return buffer.toString()
}

private fun compact(f: Double): String {
    val buffer = StringBuilder()
    writeCompact(buffer, f, 'e')
    return buffer.toString()
}

internal class FloatTest {
    @Test
    fun testWriteNonFinite() {
        assertEquals("nan", nonFinite(Double.NaN))
        assertEquals("+inf", nonFinite(Double.POSITIVE_INFINITY))
        assertEquals("-inf", nonFinite(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun testWriteDecimal() {
        assertEquals("nan", decimal(Double.NaN))
        assertEquals("+inf", decimal(Double.POSITIVE_INFINITY))
        assertEquals("-inf", decimal(Double.NEGATIVE_INFINITY))
        assertEquals("0.000000", decimal(0.0))
        assertEquals("3.141593", decimal(PI))
        assertEquals("-2.718282", decimal(-E))
        assertEquals("10000000000.000000", decimal(1e10))
    }

    @Test
    fun testWriteScientific() {
        assertEquals("nan", scientific(Double.NaN))
        assertEquals("+inf", scientific(Double.POSITIVE_INFINITY))
        assertEquals("-inf", scientific(Double.NEGATIVE_INFINITY))
        assertEquals("0.000000e+00", scientific(0.0))
        assertEquals("-0.000000e+00", scientific(-0.0))
        assertEquals("1.230000e+45", scientific(1.23e45))
        assertEquals("-3.140000e-145", scientific(-3.14e-145))
        assertEquals("1.000000e+300", scientific(1e300))
    }

    @Test
    fun testWriteCompact() {
        assertEquals("nan", compact(Double.NaN))
        assertEquals("+inf", compact(Double.POSITIVE_INFINITY))
        assertEquals("-inf", compact(Double.NEGATIVE_INFINITY))
        assertEquals("0.0", compact(0.0))
        assertEquals("3.141592653589793", compact(PI))
        assertEquals("-2.718281828459045", compact(-E))
        assertEquals("1e+10", compact(1e10))
        assertEquals("1.23e+45", compact(1.23e45))
        assertEquals("-3.14e-145", compact(-3.14e-145))
        assertEquals("1e+300", compact(1e300))
    }

    @Test
    fun testArithmeticOperators() {
        Assert.allTrue(
            """
+1.0 == 1.0
-1.0 == 0. - 1.
1.0 + 2.0 == 3.0
1.0 - 2.0 == -1.0
2.0 * 3.0 == 6.0
5.0 / 2.0 == 2.5
5.0 % 3.0 == 2.0
5.0 // 2.0 == 2.0
""",
        )
    }
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/float/FloatTest.kt

    @Test
    fun testDictionaryKey() {
        Assert.pass(
            """
x = {0: 123}
assert_eq(x[0], 123)
# TODO(nga): fix typechecker, and remove `noop`.
assert_eq(x[noop(0.0)], 123)
assert_eq(x[noop(-0.0)], 123)
assert_eq(1 in x, False)
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/float/FloatTest.kt
        """,
=======
""",
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/float/FloatTest.kt
        )
    }

    @Test
    fun testComparisons() {
        val a = Assert()
<<<<<<< HEAD:src/commonTest/kotlin/io/github/kotlinmania/starlark/values/types/float/FloatTest.kt
        // TODO(nga): fix and enable.
=======
>>>>>>> origin/main:src/commonTest/kotlin/io/github/kotlinmania/starlark_kotlin/values/types/float/FloatTest.kt
        a.disableStaticTypechecking()
        a.allTrue(
            """
+0.0 == -0.0
0.0 == 0
0 == 0.0
0 < 1.0
0.0 < 1
1 > 0.0
1.0 > 0
0.0 < float("nan")
float("+inf") < float("nan")
""",
        )
    }

    @Test
    fun testComparisonsBySorting() {
        Assert.eq(
            "sorted([float('inf'), float('-inf'), float('nan'), 1e300, -1e300, 1.0, -1.0, 1, -1, 1e-300, -1e-300, 0, 0.0, float('-0.0'), 1e-300, -1e-300])",
            "[float('-inf'), -1e+300, -1.0, -1, -1e-300, -1e-300, 0, 0.0, -0.0, 1e-300, 1e-300, 1.0, 1, 1e+300, float('+inf'), float('nan')]",
        )
    }
}
