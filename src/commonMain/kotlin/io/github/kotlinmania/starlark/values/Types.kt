// port-lint: source src/values/types.rs
@file:Suppress("unused", "ObjectPropertyName")
package io.github.kotlinmania.starlark_kotlin.values

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

// pub mod any;
// internal val any = "any" // conflicts with Typing.kt declaration
// pub mod any_array;
internal val any_array = "any_array"
// pub mod any_complex;
internal val any_complex = "any_complex"
// pub mod array;
internal val array = "array"
// pub mod bigint;
internal val bigint = "bigint"
// pub mod bool;
@Suppress("ObjectPropertyName")
internal val `bool` = "bool"
// pub mod dict;
internal val dict = "dict"
// pub(crate) mod ellipsis;
internal val ellipsis = "ellipsis"
// pub mod enumeration;
internal val enumeration = "enumeration"
// pub mod float;
internal val float = "float"
// pub mod function;
internal val function = "function"
// pub mod int;
internal val int = "int"
// pub(crate) mod known_methods;
internal val known_methods = "known_methods"
// pub mod list;
internal val list = "list"
// pub mod list_or_tuple;
internal val list_or_tuple = "list_or_tuple"
// pub mod namespace;
internal val namespace = "namespace"
// pub mod none;
internal val none = "none"
// pub(crate) mod num;
internal val num = "num"
// pub mod range;
internal val range = "range"
// pub mod record;
internal val record = "record"
// pub mod set;
internal val set = "set"
// pub mod starlark_value_as_type;
internal val starlark_value_as_type = "starlark_value_as_type"
// pub mod string;
internal val string = "string"
// pub mod structs;
internal val structs = "structs"
// pub mod tuple;
internal val tuple = "tuple"
// pub(crate) mod type_instance_id;
internal val type_instance_id = "type_instance_id"
// pub(crate) mod unbound;
internal val unbound = "unbound"
