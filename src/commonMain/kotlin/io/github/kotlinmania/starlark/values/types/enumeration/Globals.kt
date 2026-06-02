// port-lint: source src/values/types/enumeration/globals.rs
package io.github.kotlinmania.starlark.values.types.enumeration

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

/** Implementation of `enum` function. */

import io.github.kotlinmania.starlark.environment.GlobalsBuilder
import io.github.kotlinmania.starlark.eval.runtime.Arguments
import io.github.kotlinmania.starlark.eval.runtime.Evaluator
import io.github.kotlinmania.starlark.values.layout.typed.StringValue
import io.github.kotlinmania.starlark.values.types.enumeration.enumtype.EnumType

// #[starlark_module]
// pub fn register_enum(builder: &mut GlobalsBuilder)
fun registerEnum(builder: GlobalsBuilder) {
    /**
     * The `enum` type represents one value picked from a set of values.
     *
     * For example:
     *
     * ```python
     * MyEnum = enum("option1", "option2", "option3")
     * ```
     *
     * This statement defines an enumeration `MyEnum` that consists of the three values
     * `"option1"`, `"option2"` and `"option3"`.
     *
     * Now `MyEnum` is defined, it's possible to do the following:
     *
     * * Create values of this type with `MyEnum("option2")`. It is a runtime error if the
     *   argument is not one of the predeclared values of the enumeration.
     * * Get the type of the enum suitable for a type annotation with `MyEnum`.
     * * Given a value of the enum (for example, `v = MyEnum("option2")`), get the underlying
     *   value `v.value == "option2"` or the index in the enumeration `v.index == 1`.
     * * Get a list of the values that make up the array with
     *   `MyEnum.values() == ["option1", "option2", "option3"]`.
     * * Treat `MyEnum` a bit like an array, with `len(MyEnum) == 3`,
     *   `MyEnum[1] == MyEnum("option2")` and iteration over enums
     *   `[x.value for x in MyEnum] == ["option1", "option2", "option3"]`.
     *
     * Enumeration types store each value once, which are then efficiently referenced by
     * enumeration values.
     */
    // fn r#enum<'v>(#[starlark(args)] args: UnpackTuple<StringValue<'v>>, heap: Heap<'v>) -> starlark::Result<Value<'v>>
    builder.setFunction("enum") { args: Arguments, eval: Evaluator ->
        val heap = eval.heap()
        val positionalArgs = args.positionalAll()

        // Every Value must either be a field or a value (the type)
        @Suppress("UNCHECKED_CAST")
        val stringArgs = positionalArgs as List<StringValue>
        val enumType = EnumType.new(stringArgs, heap)
        Result.success(enumType.toValue())
    }
}

// #[cfg(test)] mod tests
// Tests are in commonTest, not here.
