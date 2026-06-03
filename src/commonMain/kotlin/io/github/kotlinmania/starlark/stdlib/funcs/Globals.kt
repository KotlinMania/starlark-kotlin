// port-lint: source src/stdlib/funcs/globals.rs
package io.github.kotlinmania.starlark.stdlib.funcs

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

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.stdlib.funcs.minmax.registerMinMax
import io.github.kotlinmania.starlark.stdlib.funcs.other.registerOther
import io.github.kotlinmania.starlark.stdlib.funcs.zip.registerZip
import io.github.kotlinmania.starlark.values.types.bool.registerBool
import io.github.kotlinmania.starlark.values.types.dict.registerDict
import io.github.kotlinmania.starlark.values.types.float.registerFloat
import io.github.kotlinmania.starlark.values.types.int.registerInt
import io.github.kotlinmania.starlark.values.types.list.registerList
import io.github.kotlinmania.starlark.values.types.none.registerNone
import io.github.kotlinmania.starlark.values.types.num.registerNum
import io.github.kotlinmania.starlark.values.types.range.registerRange
import io.github.kotlinmania.starlark.values.types.string.registerStr
import io.github.kotlinmania.starlark.values.types.tuple.registerTuple

internal fun registerGlobals(globals: GlobalsBuilder) {
    registerList(globals)
    registerTuple(globals)
    registerDict(globals)
    registerBool(globals)
    registerNone(globals)
    registerStr(globals)
    registerRange(globals)
    registerInt(globals)
    registerNum(globals)
    registerFloat(globals)
    registerMinMax(globals)
    registerZip(globals)
    registerOther(globals)
}
