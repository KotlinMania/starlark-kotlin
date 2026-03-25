// port-lint: source src/stdlib/funcs/other.rs
package io.github.kotlinmania.starlark_kotlin.stdlib.funcs

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

/// A module with the standard function and constants that are by default in all
/// dialect of Starlark

import io.github.kotlinmania.starlark_kotlin.environment.GlobalsBuilder
import io.github.kotlinmania.starlark_kotlin.values.AllocValue
import io.github.kotlinmania.starlark_kotlin.values.ValueError
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkIter
import io.github.kotlinmania.starlark_kotlin.values.typing.StarlarkNever
import io.github.kotlinmania.starlark_kotlin.values.types.tuple.unpack.UnpackTuple
import io.github.kotlinmania.starlark_kotlin.values.types.string.intern.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.types.string.ValueLike
import io.github.kotlinmania.starlark_kotlin.values.types.string.Evaluator
import io.github.kotlinmania.starlark_kotlin.values.types.list.AllocList
import io.github.kotlinmania.starlark_kotlin.values.ValueOfUnchecked
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.FrozenStringValue
import io.github.kotlinmania.starlark_kotlin.values.typing.type_compiled.getType
import io.github.kotlinmania.starlark_kotlin.values.owned.unpackStr
import io.github.kotlinmania.starlark_kotlin.values.length
import io.github.kotlinmania.starlark_kotlin.values.iterate
import io.github.kotlinmania.starlark_kotlin.values.toBool
import io.github.kotlinmania.starlark_kotlin.values.hasAttr
import io.github.kotlinmania.starlark_kotlin.values.dirAttr
import io.github.kotlinmania.starlark_kotlin.tests.getAttr
import io.github.kotlinmania.starlark_kotlin.tests.collectRepr
import io.github.kotlinmania.starlark_kotlin.eval.bc.getTypeValue

/// fail: fail the execution
///
/// ```
/// fail("this is an error")  # fail: this is an error
/// fail("oops", 1, False)  # fail: oops 1 False
/// ```
fun fail(args: UnpackTuple<Value>): Result<StarlarkNever> {
    val s = StringBuilder()
    for (x in args.items) {
        s.append(' ')
        val str = x.unpackStr()
        if (str != null) {
            s.append(str)
        } else {
            x.collectRepr(s)
        }
    }
    return Result.failure(
        IllegalStateException(s.toString())
    )
}

/// [any](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#any
/// ): returns true if any value in the iterable object have a truth value
/// of true.
///
/// ```
/// any([0, True]) == True
/// any([0, 1]) == True
/// any([0, 1, True]) == True
/// any([0, 0]) == False
/// any([0, False]) == False
/// ```
fun any(x: ValueOfUnchecked<StarlarkIter<Value>>, heap: Heap): Result<Boolean> {
    for (i in x.get().iterate(heap)) {
        if (i.toBool()) {
            return Result.success(true)
        }
    }
    return Result.success(false)
}

/// [all](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#all
/// ): returns true if all values in the iterable object have a truth value
/// of true.
///
/// ```
/// all([1, True]) == True
/// all([1, 1]) == True
/// all([0, 1, True]) == False
/// all([True, 1, True]) == True
/// all([0, 0]) == False
/// all([0, False]) == False
/// all([True, 0]) == False
/// all([1, False]) == False
/// ```
fun all(x: ValueOfUnchecked<StarlarkIter<Value>>, heap: Heap): Result<Boolean> {
    for (i in x.get().iterate(heap)) {
        if (!i.toBool()) {
            return Result.success(false)
        }
    }
    return Result.success(true)
}

/// [dir](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#dir
/// ): list attributes of a value.
///
/// `dir(x)` returns a list of the names of the attributes (fields and
/// methods) of its operand. The attributes of a value `x` are the names
/// `f` such that `x.f` is a valid expression.
///
/// ```
/// "capitalize" in dir("abc")
/// ```
fun dir(x: Value): Result<List<String>> {
    return Result.success(x.dirAttr())
}

/// [enumerate](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#enumerate
/// ): return a list of (index, element) from an iterable.
///
/// `enumerate(x)` returns a list of `(index, value)` pairs, each containing
/// successive values of the iterable sequence and the index of the
/// value within the sequence.
///
/// The optional second parameter, `start`, specifies an integer value to
/// add to each index.
///
/// ```
/// enumerate(["zero", "one", "two"]) == [(0, "zero"), (1, "one"), (2, "two")]
/// enumerate(["one", "two"], 1) == [(1, "one"), (2, "two")]
/// ```
fun enumerate(
    it: ValueOfUnchecked<StarlarkIter<Value>>,
    start: Int = 0,
    heap: Heap,
): Result<AllocList<List<Pair<Int, Value>>>> {
    val v = it.get().iterate(heap)
        .withIndex()
        .map { (k, v) -> Pair(k + start, v) }
        .toList()
    return Result.success(AllocList(v))
}

/// [getattr](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#getattr
/// ): returns the value of an attribute
///
/// `getattr(x, name)` returns the value of the attribute (field or method)
/// of x named `name`. It is a dynamic error if x has no such attribute.
///
/// `getattr(x, "f")` is equivalent to `x.f`.
///
/// `getattr(x, "f", d)` is equivalent to `x.f if hasattr(x, "f") else d`
/// and will never raise an error.
///
/// ```
/// getattr("banana", "split")("a") == ["b", "n", "n", ""]
/// ```
fun getattr(
    a: Value,
    attr: String,
    default: Value? = null,
    heap: Heap,
): Result<Value> {
    val v = a.getAttr(attr, heap)
    if (v != null) {
        return Result.success(v)
    }
    if (default != null) {
        return Result.success(default)
    }
    return ValueError.unsupportedOwned(a.getType(), ".$attr", null)
}

/// [hasattr](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#hasattr
/// ): test if an object has an attribute
///
/// `hasattr(x, name)` reports whether x has an attribute (field or method)
/// named `name`.
fun hasattr(a: Value, attr: String, heap: Heap): Result<Boolean> {
    return Result.success(a.hasAttr(attr, heap))
}

/// [hash](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#hash
/// ): returns the hash number of a value.
///
/// `hash(x)` returns an integer hash value for x such that `x == y`
/// implies `hash(x) == hash(y)`.
///
/// `hash` fails if x, or any value upon which its hash depends, is
/// unhashable.
///
/// ```
/// hash("hello") != hash("world")
/// ```
fun hash(a: String): Result<Int> {
    // From the starlark spec:
    // > the hash function for strings is the same as that implemented by java.lang.String.hashCode,
    // > a simple polynomial accumulator over the UTF-16 transcoding of the string:
    // > `s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]`
    // As per spec the function should only support string and bytes types.
    // We don't have support for bytes, so parameter is forced to be a string.

    // Most strings are ASCII strings, try them first.
    var hash = 0
    var allAscii = true
    for (b in a.encodeToByteArray()) {
        if (b.toInt() and 0xFF > 0x7f) {
            allAscii = false
            break
        }
        hash = hash * 31 + (b.toInt() and 0xFF)
    }
    if (allAscii) {
        return Result.success(hash)
    }

    // Fallback to UTF-16 encoding
    var hash16 = 0
    for (c in a) {
        // Handle surrogate pairs for characters outside BMP
        if (c.code > 0xFFFF) {
            val high = ((c.code - 0x10000) shr 10) + 0xD800
            val low = ((c.code - 0x10000) and 0x3FF) + 0xDC00
            hash16 = 31 * hash16 + high
            hash16 = 31 * hash16 + low
        } else {
            hash16 = 31 * hash16 + c.code
        }
    }
    return Result.success(hash16)
}

/// [len](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#len
/// ): get the length of a sequence
///
/// `len(x)` returns the number of elements in its argument.
///
/// It is a dynamic error if its argument is not a sequence.
///
/// ```
/// len(()) == 0
/// len({}) == 0
/// len([]) == 0
/// len([1]) == 1
/// len([1,2]) == 2
/// len({'16': 10}) == 1
/// ```
fun len(a: Value): Result<Int> {
    return a.length()
}

/// [reversed](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#reversed
/// ): reverse a sequence
///
/// `reversed(x)` returns a new list containing the elements of the iterable
/// sequence x in reverse order.
///
/// ```
/// reversed(['a', 'b', 'c'])              == ['c', 'b', 'a']
/// reversed(range(5))                     == [4, 3, 2, 1, 0]
/// reversed("stressed".elems())           == ["d", "e", "s", "s", "e", "r", "t", "s"]
/// reversed({"one": 1, "two": 2}.keys())  == ["two", "one"]
/// ```
fun reversed(
    a: ValueOfUnchecked<StarlarkIter<Value>>,
    heap: Heap,
): Result<List<Value>> {
    val v = a.get().iterate(heap).toMutableList()
    v.reverse()
    return Result.success(v)
}

/// [sorted](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#sorted
/// ): sort a sequence
///
/// `sorted(x)` returns a new list containing the elements of the iterable
/// sequence x, in sorted order. The sort algorithm is stable.
///
/// The optional named parameter `reverse`, if true, causes `sorted` to
/// return results in reverse sorted order.
///
/// The optional named parameter `key` specifies a function of one
/// argument to apply to obtain the value's sort key.
/// The default behavior is the identity function.
///
/// ```
/// sorted([3, 1, 4, 1, 5, 9])                               == [1, 1, 3, 4, 5, 9]
/// sorted([3, 1, 4, 1, 5, 9], reverse=True)                 == [9, 5, 4, 3, 1, 1]
/// sorted(["two", "three", "four"], key=len)                == ["two", "four", "three"]
/// sorted(["two", "three", "four"], key=len, reverse=True)  == ["three", "four", "two"]
/// ```
// This function is not spec-safe, because it may call `key` function
// which might be not spec-safe.
fun sorted(
    x: ValueOfUnchecked<StarlarkIter<Value>>,
    key: Value? = null,
    reverse: Boolean = false,
    eval: Evaluator,
): Result<AllocList<List<Value>>> {
    val it = x.get().iterate(eval.heap())
    val pairs: MutableList<Pair<Value, Value>> = if (key == null) {
        it.map { v -> Pair(v, v) }.toMutableList()
    } else {
        val v = mutableListOf<Pair<Value, Value>>()
        for (el in it) {
            v.add(Pair(el, key.invokePos(listOf(el), eval)))
        }
        v
    }

    var compareOk: Result<Unit> = Result.success(Unit)

    pairs.sortWith(Comparator { a, b ->
        val ordOrErr = try {
            val cmp = a.second.compare(b.second)
            if (reverse) -cmp else cmp
        } catch (e: Exception) {
            compareOk = Result.failure(e)
            0 // does not matter
        }
        ordOrErr
    })

    compareOk.getOrThrow()

    return Result.success(AllocList(pairs.map { it.first }))
}

/// [type](
/// https://github.com/bazelbuild/starlark/blob/master/spec.md#type
/// ): returns a string describing the type of its operand.
///
/// ```
/// type(None)              == "NoneType"
/// type(0)                 == "int"
/// type(1)                 == "int"
/// type(())                == "tuple"
/// type("hello")           == "string"
/// ```
fun type(a: Value): Result<FrozenStringValue> {
    return Result.success(a.getTypeValue())
}

fun registerOther(builder: GlobalsBuilder) {
    builder.set("fail", ::fail)
    builder.set("any", ::any)
    builder.set("all", ::all)
    builder.set("dir", ::dir)
    builder.set("enumerate", ::enumerate)
    builder.set("getattr", ::getattr)
    builder.set("hasattr", ::hasattr)
    builder.set("hash", ::hash)
    builder.set("len", ::len)
    builder.set("reversed", ::reversed)
    builder.set("sorted", ::sorted)
    builder.set("type", ::type)
}
