// port-lint: source src/eval/bc.rs
package io.github.kotlinmania.starlark_kotlin.eval.bc

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

/** Bytecode interpreter. */

// Rust module declarations (pub(crate) mod ...) are structural and have no
// Kotlin equivalent -- sub-packages are established by the files that live in
// them.  The original port translated each `mod foo;` as
// `typealias foo = Unit`, but those typealiases introduce classifiers that
// clash with identically-named sub-packages (e.g. typealias compiler vs
// package eval.bc.compiler), causing "Package conflicts with classifier"
// errors.  Removed.
