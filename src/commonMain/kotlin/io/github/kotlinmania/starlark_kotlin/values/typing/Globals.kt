// port-lint: source src/values/typing/globals.rs
package io.github.kotlinmania.starlark_kotlin.values.typing

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

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.registerEvalType

// pub(crate) fn register_typing(globals: &mut GlobalsBuilder)
internal fun registerTyping(globals: GlobalsBuilder) {
    registerEvalType(globals)
    globals.namespace("typing") { globals ->
        globals.set("Any", TypingAny())
        globals.set("Never", TypingNever())
        globals.set("Callable", TypingCallable())
        globals.set("Iterable", TypingIterable())
    }
}
