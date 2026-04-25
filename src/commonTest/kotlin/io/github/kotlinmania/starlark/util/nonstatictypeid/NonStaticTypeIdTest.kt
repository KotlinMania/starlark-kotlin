// port-lint: source src/util/non_static_type_id.rs (tests)
package io.github.kotlinmania.starlark.util.nonstatictypeid

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

import kotlin.test.Test
import kotlin.test.assertEquals

class NonStaticTypeIdTest {

    // #[test]
    // fn test_non_static_type_id()
    @Test
    fun testNonStaticTypeId() {
        // Rust: assert_eq!(non_static_type_id::<&str>(), TypeId::of::<&'static str>());
        // Kotlin has no lifetimes, so &str and &'static str both correspond to String::class.
        assertEquals(String::class, nonStaticTypeId<String>())
    }
}
