// port-lint: source src/stdlib/funcs/other.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.funcs.other

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

/**
 * A module with the standard functions and constants that are by default in all
 * dialects of Starlark.
 */

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.eval.runtime.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.toValue
import io.github.kotlinmania.starlark_kotlin.values.layout.avalues.allocTuple
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.typed.FrozenStringValue

/**
 * fail: fail the execution.
 *
 * ```
 * fail("this is an error")  # fail: this is an error
 * fail("oops", 1, False)  # fail: oops 1 False
 * ```
 *
 * @param args Positional arguments to be formatted into the error message.
 * @return Never returns successfully; always returns a failure [Result].
 */
// fn fail(#[starlark(args)] args: UnpackTuple<Value>) -> starlark::Result<StarlarkNever>
private fun fail(args: List<Value>): Nothing {
    val s = StringBuilder()
    for (x in args) {
        s.append(' ')
        val str = x.unpackStr()
        if (str != null) {
            s.append(str)
        } else {
            x.collectRepr(s)
        }
    }
    throw StarlarkFailError(s.toString())
}

/**
 * Error thrown by the `fail()` builtin function.
 *
 * Corresponds to Rust's `ErrorKind::Fail(anyhow::Error::msg(s))`.
 */
class StarlarkFailError(message: String) : RuntimeException(message)

/**
 * [any](https://github.com/bazelbuild/starlark/blob/master/spec.md#any):
 * returns true if any value in the iterable object have a truth value of true.
 *
 * ```
 * any([0, True]) == True
 * any([0, 1]) == True
 * any([0, 1, True]) == True
 * any([0, 0]) == False
 * any([0, False]) == False
 * ```
 *
 * @param x An iterable of values.
 * @param heap The active heap.
 * @return `true` if any element is truthy, `false` otherwise.
 */
// fn any<'v>(x: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, heap: Heap<'v>) -> starlark::Result<bool>
private fun any(x: Value, heap: Heap): Boolean {
    val iter = x.iterate(heap).getOrThrow()
    for (i in iter) {
        if (i.toBool()) {
            return true
        }
    }
    return false
}

/**
 * [all](https://github.com/bazelbuild/starlark/blob/master/spec.md#all):
 * returns true if all values in the iterable object have a truth value of true.
 *
 * ```
 * all([1, True]) == True
 * all([1, 1]) == True
 * all([0, 1, True]) == False
 * all([True, 1, True]) == True
 * all([0, 0]) == False
 * all([0, False]) == False
 * all([True, 0]) == False
 * all([1, False]) == False
 * ```
 *
 * @param x An iterable of values.
 * @param heap The active heap.
 * @return `true` if all elements are truthy, `false` otherwise.
 */
// fn all<'v>(x: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, heap: Heap<'v>) -> starlark::Result<bool>
private fun all(x: Value, heap: Heap): Boolean {
    val iter = x.iterate(heap).getOrThrow()
    for (i in iter) {
        if (!i.toBool()) {
            return false
        }
    }
    return true
}

/**
 * [dir](https://github.com/bazelbuild/starlark/blob/master/spec.md#dir):
 * list attributes of a value.
 *
 * `dir(x)` returns a list of the names of the attributes (fields and
 * methods) of its operand. The attributes of a value `x` are the names
 * `f` such that `x.f` is a valid expression.
 *
 * ```
 * "capitalize" in dir("abc")
 * ```
 *
 * @param x The value to inspect.
 * @return A list of attribute names.
 */
// fn dir(x: Value) -> anyhow::Result<Vec<String>>
private fun dir(x: Value): List<String> {
    return x.dirAttr()
}

/**
 * [enumerate](https://github.com/bazelbuild/starlark/blob/master/spec.md#enumerate):
 * return a list of (index, element) from an iterable.
 *
 * `enumerate(x)` returns a list of `(index, value)` pairs, each containing
 * successive values of the iterable sequence and the index of the
 * value within the sequence.
 *
 * The optional second parameter, `start`, specifies an integer value to
 * add to each index.
 *
 * ```
 * enumerate(["zero", "one", "two"]) == [(0, "zero"), (1, "one"), (2, "two")]
 * enumerate(["one", "two"], 1) == [(1, "one"), (2, "two")]
 * ```
 *
 * @param it An iterable of values.
 * @param start The starting index offset (default 0).
 * @param heap The active heap for allocating tuples.
 * @return A list of (index, value) tuples.
 */
// fn enumerate<'v>(it: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, start: i32, heap: Heap<'v>)
//     -> starlark::Result<impl AllocValue<'v>>
private fun enumerate(it: Value, start: Int, heap: Heap): Value {
    val iter = it.iterate(heap).getOrThrow()
    val result = mutableListOf<Value>()
    for ((k, v) in iter.withIndex()) {
        val tuple = heap.allocTuple(listOf(heap.allocInt(k + start), v))
        result.add(tuple)
    }
    return heap.allocList(result)
}

/**
 * [getattr](https://github.com/bazelbuild/starlark/blob/master/spec.md#getattr):
 * returns the value of an attribute.
 *
 * `getattr(x, name)` returns the value of the attribute (field or method)
 * of x named `name`. It is a dynamic error if x has no such attribute.
 *
 * `getattr(x, "f")` is equivalent to `x.f`.
 *
 * `getattr(x, "f", d)` is equivalent to `x.f if hasattr(x, "f") else d`
 * and will never raise an error.
 *
 * ```
 * getattr("banana", "split")("a") == ["b", "n", "n", ""]
 * ```
 *
 * @param a The value to get the attribute from.
 * @param attr The name of the attribute.
 * @param default An optional default value if the attribute does not exist.
 * @param heap The active heap.
 * @return The value of the attribute, or [default] if provided and the attribute is missing.
 */
// fn getattr<'v>(a: Value<'v>, attr: &str, default: Option<Value<'v>>, heap: Heap<'v>)
//     -> starlark::Result<Value<'v>>
private fun getattr(a: Value, attr: String, default: Value?, heap: Heap): Value {
    val v = a.getAttr(attr, heap).getOrThrow()
    if (v != null) {
        return v
    }
    if (default != null) {
        return default
    }
    ValueError.unsupportedOwned<Value>(a.getType(), ".$attr", null).getOrThrow()
    error("unreachable")
}

/**
 * [hasattr](https://github.com/bazelbuild/starlark/blob/master/spec.md#hasattr):
 * test if an object has an attribute.
 *
 * `hasattr(x, name)` reports whether x has an attribute (field or method)
 * named `name`.
 *
 * @param a The value to check.
 * @param attr The attribute name to look for.
 * @param heap The active heap.
 * @return `true` if the attribute exists, `false` otherwise.
 */
// fn hasattr<'v>(a: Value<'v>, attr: &str, heap: Heap<'v>) -> anyhow::Result<bool>
private fun hasattr(a: Value, attr: String, heap: Heap): Boolean {
    return a.hasAttr(attr, heap)
}

/**
 * [hash](https://github.com/bazelbuild/starlark/blob/master/spec.md#hash):
 * returns the hash number of a value.
 *
 * `hash(x)` returns an integer hash value for x such that `x == y`
 * implies `hash(x) == hash(y)`.
 *
 * `hash` fails if x, or any value upon which its hash depends, is
 * unhashable.
 *
 * ```
 * hash("hello") != hash("world")
 * ```
 *
 * @param a The string to hash.
 * @return The hash value as an [Int], using the java.lang.String.hashCode algorithm
 *   over the UTF-16 transcoding of the string as specified by the Starlark spec.
 */
// fn hash(a: &str) -> anyhow::Result<i32>
private fun hash(a: String): Int {
    // From the starlark spec:
    // > the hash function for strings is the same as that implemented by java.lang.String.hashCode,
    // > a simple polynomial accumulator over the UTF-16 transcoding of the string:
    // > `s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]`
    // As per spec the function should only support string and bytes types.
    // We don't have support for bytes, so parameter is forced to be a string.

    // Most strings are ASCII strings, try them first.
    var hashVal = 0
    var allAscii = true
    for (b in a.encodeToByteArray()) {
        if (b.toInt() and 0xFF > 0x7f) {
            allAscii = false
            break
        }
        hashVal = hashVal * 31 + (b.toInt() and 0xFF)
    }
    if (allAscii) {
        return hashVal
    }

    // Fallback to UTF-16 encoding for non-ASCII strings.
    var hash16 = 0
    for (c in a) {
        // Kotlin Char is already UTF-16, so surrogates are handled naturally
        // when iterating over the string's chars.
        hash16 = 31 * hash16 + c.code
    }
    return hash16
}

/**
 * [len](https://github.com/bazelbuild/starlark/blob/master/spec.md#len):
 * get the length of a sequence.
 *
 * `len(x)` returns the number of elements in its argument.
 *
 * It is a dynamic error if its argument is not a sequence.
 *
 * ```
 * len(()) == 0
 * len({}) == 0
 * len([]) == 0
 * len([1]) == 1
 * len([1,2]) == 2
 * len({'16': 10}) == 1
 * ```
 *
 * @param a The value whose length to compute.
 * @return The number of elements.
 */
// fn len(a: Value) -> starlark::Result<i32>
private fun len(a: Value): Int {
    return a.length().getOrThrow()
}

/**
 * [reversed](https://github.com/bazelbuild/starlark/blob/master/spec.md#reversed):
 * reverse a sequence.
 *
 * `reversed(x)` returns a new list containing the elements of the iterable
 * sequence x in reverse order.
 *
 * ```
 * reversed(['a', 'b', 'c'])              == ['c', 'b', 'a']
 * reversed(range(5))                     == [4, 3, 2, 1, 0]
 * reversed("stressed".elems())           == ["d", "e", "s", "s", "e", "r", "t", "s"]
 * reversed({"one": 1, "two": 2}.keys())  == ["two", "one"]
 * ```
 *
 * @param a An iterable of values.
 * @param heap The active heap.
 * @return A new list with elements in reverse order.
 */
// fn reversed<'v>(a: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, heap: Heap<'v>)
//     -> starlark::Result<Vec<Value<'v>>>
private fun reversed(a: Value, heap: Heap): Value {
    val iter = a.iterate(heap).getOrThrow()
    val v = iter.asSequence().toMutableList()
    v.reverse()
    return heap.allocList(v)
}

/**
 * [sorted](https://github.com/bazelbuild/starlark/blob/master/spec.md#sorted):
 * sort a sequence.
 *
 * `sorted(x)` returns a new list containing the elements of the iterable
 * sequence x, in sorted order. The sort algorithm is stable.
 *
 * The optional named parameter `reverse`, if true, causes `sorted` to
 * return results in reverse sorted order.
 *
 * The optional named parameter `key` specifies a function of one
 * argument to apply to obtain the value's sort key.
 * The default behavior is the identity function.
 *
 * ```
 * sorted([3, 1, 4, 1, 5, 9])                               == [1, 1, 3, 4, 5, 9]
 * sorted([3, 1, 4, 1, 5, 9], reverse=True)                 == [9, 5, 4, 3, 1, 1]
 * sorted(["two", "three", "four"], key=len)                == ["two", "four", "three"]
 * sorted(["two", "three", "four"], key=len, reverse=True)  == ["three", "four", "two"]
 * ```
 *
 * @param x An iterable of values.
 * @param key Optional key function applied to each element before comparison.
 * @param reverse If true, sort in reverse order.
 * @param eval The current evaluator, used to invoke the key function.
 * @return A new sorted list.
 */
// This function is not spec-safe, because it may call `key` function
// which might be not spec-safe.
// fn sorted<'v>(x: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, key: Option<Value<'v>>,
//     reverse: bool, eval: &mut Evaluator<'v, '_, '_>) -> starlark::Result<AllocList<...>>
private fun sorted(x: Value, key: Value?, reverse: Boolean, eval: Evaluator): Value {
    val heap = eval.heap()
    val it = x.iterate(heap).getOrThrow()
    val pairs: MutableList<Pair<Value, Value>> = if (key == null) {
        it.asSequence().map { v -> Pair(v, v) }.toMutableList()
    } else {
        val v = mutableListOf<Pair<Value, Value>>()
        for (el in it) {
            v.add(Pair(el, key.invokePos(listOf(el), eval).getOrThrow()))
        }
        v
    }

    var compareOk: Exception? = null

    pairs.sortWith(Comparator { a, b ->
        val ordOrErr = try {
            val cmp = a.second.compare(b.second).getOrThrow()
            if (reverse) -cmp else cmp
        } catch (e: Exception) {
            compareOk = e
            0 // does not matter
        }
        ordOrErr
    })

    if (compareOk != null) {
        throw compareOk!!
    }

    return heap.allocList(pairs.map { it.first })
}

/**
 * [type](https://github.com/bazelbuild/starlark/blob/master/spec.md#type):
 * returns a string describing the type of its operand.
 *
 * ```
 * type(None)              == "NoneType"
 * type(0)                 == "int"
 * type(1)                 == "int"
 * type(())                == "tuple"
 * type("hello")           == "string"
 * ```
 *
 * @param a The value to inspect.
 * @return A frozen string value containing the type name.
 */
// fn r#type<'v>(a: Value) -> anyhow::Result<FrozenStringValue>
private fun type(a: Value): FrozenStringValue {
    return a.getTypeValue()
}

// #[starlark_module]
// pub(crate) fn register_other(builder: &mut GlobalsBuilder)
/**
 * Register the standard functions (`fail`, `any`, `all`, `dir`, `enumerate`,
 * `getattr`, `hasattr`, `hash`, `len`, `reversed`, `sorted`, `type`) with
 * the given [GlobalsBuilder].
 */
internal fun registerOther(globals: GlobalsBuilder) {
    // fn fail(#[starlark(args)] args: UnpackTuple<Value>) -> starlark::Result<StarlarkNever>
    globals.setFunction("fail") { _, callArgs ->
        fail(callArgs.positionalAll())
    }

    // #[starlark(speculative_exec_safe)]
    // fn any<'v>(x: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, heap: Heap<'v>) -> starlark::Result<bool>
    globals.setFunction("any", speculativeExecSafe = true) { eval, callArgs ->
        val x = callArgs.positional<Value>(0)
        eval.heap().allocBool(any(x, eval.heap()))
    }

    // #[starlark(speculative_exec_safe)]
    // fn all<'v>(x: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, heap: Heap<'v>) -> starlark::Result<bool>
    globals.setFunction("all", speculativeExecSafe = true) { eval, callArgs ->
        val x = callArgs.positional<Value>(0)
        eval.heap().allocBool(all(x, eval.heap()))
    }

    // #[starlark(speculative_exec_safe)]
    // fn dir(x: Value) -> anyhow::Result<Vec<String>>
    globals.setFunction("dir", speculativeExecSafe = true) { eval, callArgs ->
        val x = callArgs.positional<Value>(0)
        eval.heap().allocList(dir(x).map { eval.heap().allocStr(it) })
    }

    // #[starlark(speculative_exec_safe)]
    // fn enumerate<'v>(it: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, start: i32, heap: Heap<'v>)
    globals.setFunction("enumerate", speculativeExecSafe = true) { eval, callArgs ->
        val it = callArgs.positional<Value>(0)
        val start = callArgs.optionalPositional<Int>(1) ?: 0
        enumerate(it, start, eval.heap())
    }

    // #[starlark(speculative_exec_safe)]
    // fn getattr<'v>(a: Value<'v>, attr: &str, default: Option<Value<'v>>, heap: Heap<'v>)
    globals.setFunction("getattr", speculativeExecSafe = true) { eval, callArgs ->
        val a = callArgs.positional<Value>(0)
        val attr = callArgs.positional<String>(1)
        val default = callArgs.optionalPositional<Value>(2)
        getattr(a, attr, default, eval.heap())
    }

    // #[starlark(speculative_exec_safe)]
    // fn hasattr<'v>(a: Value<'v>, attr: &str, heap: Heap<'v>) -> anyhow::Result<bool>
    globals.setFunction("hasattr", speculativeExecSafe = true) { eval, callArgs ->
        val a = callArgs.positional<Value>(0)
        val attr = callArgs.positional<String>(1)
        eval.heap().allocBool(hasattr(a, attr, eval.heap()))
    }

    // #[starlark(speculative_exec_safe)]
    // fn hash(a: &str) -> anyhow::Result<i32>
    globals.setFunction("hash", speculativeExecSafe = true) { eval, callArgs ->
        val a = callArgs.positional<String>(0)
        eval.heap().allocInt(hash(a))
    }

    // #[starlark(speculative_exec_safe)]
    // fn len(a: Value) -> starlark::Result<i32>
    globals.setFunction("len", speculativeExecSafe = true) { eval, callArgs ->
        val a = callArgs.positional<Value>(0)
        eval.heap().allocInt(len(a))
    }

    // #[starlark(speculative_exec_safe)]
    // fn reversed<'v>(a: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, heap: Heap<'v>)
    globals.setFunction("reversed", speculativeExecSafe = true) { eval, callArgs ->
        val a = callArgs.positional<Value>(0)
        reversed(a, eval.heap())
    }

    // This function is not spec-safe, because it may call `key` function
    // which might be not spec-safe.
    // fn sorted<'v>(x: ValueOfUnchecked<'v, StarlarkIter<Value<'v>>>, key: Option<Value<'v>>,
    //     reverse: bool, eval: &mut Evaluator<'v, '_, '_>)
    globals.setFunction("sorted") { eval, callArgs ->
        val x = callArgs.positional<Value>(0)
        val key = callArgs.optionalNamed<Value>("key")
        val reverse = callArgs.optionalNamed<Boolean>("reverse") ?: false
        sorted(x, key, reverse, eval)
    }

    // #[starlark(speculative_exec_safe, as_type = AbstractType)]
    // fn r#type<'v>(a: Value) -> anyhow::Result<FrozenStringValue>
    globals.setFunction("type", speculativeExecSafe = true) { eval, callArgs ->
        val a = callArgs.positional<Value>(0)
        type(a).toValue()
    }
}
