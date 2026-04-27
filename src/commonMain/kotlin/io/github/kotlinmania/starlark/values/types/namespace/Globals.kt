// port-lint: source src/values/types/namespace/globals.rs
package io.github.kotlinmania.starlark.values.types.namespace

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

import starlarkmap.smallmap.SmallMap
import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.values.layout.Value

fun registerNamespace(builder: GlobalsBuilder) {
    builder.setFunction("namespace") { args, eval ->
        val heap = eval.heap()
        args.noPositionalArgs(heap).getOrElse { return@setFunction Result.failure<Value>(it) }

        val namesMap = args.namesMap().getOrElse { return@setFunction Result.failure<Value>(it) }
        val fields = SmallMap.withCapacity<String, MaybeDocHiddenValue<Value>>(namesMap.len())
        for ((k, v) in namesMap.iter()) {
            fields.insert(
                k.asStr(),
                MaybeDocHiddenValue(
                    value = v,
                    docHidden = false,
                ),
            )
        }
        NamespaceGen.new(fields)
    }
}
