// port-lint: source src/tests/bc.rs
@file:Suppress("unused", "ObjectPropertyName")
package io.github.kotlinmania.starlark.tests

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

/// Bytecode generation tests.

// mod and_or;
internal val and_or = "and_or"
// mod call;
// internal val call = "call" // conflicts with tests.call package
// mod compr;
internal val compr = "compr"
// mod definitely_assigned;
internal val definitely_assigned = "definitely_assigned"
// mod expr;
internal val expr = "expr"
// mod for_stmt;
internal val for_stmt = "for_stmt"
// pub(crate) mod golden;
internal val golden = "golden"
// mod if_stmt;
internal val if_stmt = "if_stmt"
// mod isinstance;
internal val isinstance = "isinstance"
