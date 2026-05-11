// port-lint: tests src/environment/globals.rs (tests)
package io.github.kotlinmania.starlark_kotlin.environment

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

import io.github.kotlinmania.starlark_kotlin.docs.DocItem
import io.github.kotlinmania.starlark_kotlin.values.types.none.NoneType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GlobalsTest {

    // Rust: fn test_send_sync() where Globals: Send + Sync {}
    // Not applicable in Kotlin - all objects are thread-shareable by default.

    @Test
    fun testDocHidden() {
        val globals = GlobalsBuilder.new()
        globals.namespaceNoDocs("ns_hidden") { _ -> }
        globals.namespace("ns") { builder ->
            builder.namespaceNoDocs("nested_ns_hidden") { _ -> }
            builder.set("x", NoneType)
        }
        val docs = globals.build().documentation()

        val keys = docs.members.keys().toList()
        assertEquals(1, keys.size)
        val k = keys.single()
        assertEquals("ns", k)
        val v = docs.members.get(k)!!
        val moduleDocs = assertIs<DocItem.Module>(v)
        val memberKeys = moduleDocs.module.members.keys().toList()
        assertEquals(1, memberKeys.size)
        assertEquals("x", memberKeys.single())
    }
}
