// port-lint: source src/values/types/set/set.rs
package io.github.kotlinmania.starlark_kotlin.values.types.set

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

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.Heap
import io.github.kotlinmania.starlark_kotlin.values.Value
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.function.SpecialBuiltinFunction
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter

/**
 * Register the `set` builtin function.
 *
 * This function is generated from the `#[starlark_module]` macro in Rust.
 * It registers the `set` constructor as a global builtin function.
 */
internal fun registerSet(globals: GlobalsBuilder) {
    /**
     * [set](https://github.com/bazelbuild/starlark/blob/master/spec.md#set):
     * construct a set.
     *
     * `set(x)` returns a new set containing the unique elements of the
     * iterable sequence x.
     *
     * With no argument, `set()` returns a new empty set.
     *
     * ```
     * # starlark::assert::all_true(r#"
     * set()           == set([])
     * set([1, 2, 3])  == set([3, 2, 1])
     * set([1, 2, 1])  == set([1, 2])
     * # "#);
     * ```
     */
    globals.registerFunction(
        name = "set",
        asType = FrozenSet::class,
        speculativeExecSafe = true,
        specialBuiltinFunction = SpecialBuiltinFunction.Set
    ) { arg: ValueOfUnchecked<StarlarkIter<Value<*>>>?, heap: Heap<*> ->
        val set = when (arg) {
            null -> SetData.default()
            else -> {
                val pos = arg
                when (val setRef = SetRef.unpackValueOpt(pos.get())) {
                    null -> {
                        val it = pos.get().iterate(heap).getOrElse { return@registerFunction Result.failure(it) }
                        val data = SetData.default<Any?>()
                        for (el in it) {
                            val hashedEl = el.getHashed().getOrElse { return@registerFunction Result.failure(it) }
                            data.content.insertHashed(hashedEl)
                        }
                        data
                    }
                    else -> setRef.aref.clone()
                }
            }
        }
        Result.success(set)
    }
}

// Tests would be here when assert module is ported
/*
#[cfg(test)]
mod tests {
    use crate::assert;

    #[test]
    fn test_set_type_as_type_compile_time() {
        assert::fail(
            r"
def f_fail_ct(x: set[int]):
    return x

s = set(['not_int'])

f_fail_ct(s)
",
            //Is it actually runtime or compile time error?
            r#"Value `set(["not_int"])` of type `set` does not match the type annotation `set[int]` for argument `x`"#,
        );
    }

    #[test]
    fn test_return_set_type_as_type_compile_time() {
        assert::fail(
            r"
def f_fail_ct(x: str) -> set[int]:
    return set([x])

f_fail_ct('not_int')
",
            //Is it actually runtime or compile time error?
            r#"Value `set(["not_int"])` of type `set` does not match the type annotation `set[int]` for return type"#,
        );
    }

    #[test]
    fn test_set_type_as_type_run_time() {
        assert::fail(
            r"
def f_fail_rt(x: set[int]):
    return x

s = set(['not_int'])

noop(f_fail_rt)(s)
",
            r#"Value `set(["not_int"])` of type `set` does not match the type annotation `set[int]` for argument `x`"#,
        );
    }
}
*/
