// port-lint: source src/eval/runtime/profile/flamegraph.rs
package io.github.kotlinmania.starlark_kotlin.eval.runtime.profile.flamegraph

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

//! Utility to write files in formats understood by `flamegraph.pl`.

import io.github.kotlinmania.starlark_kotlin.util.ArcStr

/// Node in flamegraph tree.
// #[derive(Debug, Clone, Default, PartialEq, Eq)]
// pub(crate) struct FlameGraphNode
// TODO: stub - FlameGraphNode needs real import
internal class FlameGraphNode(
    val children: MutableMap<ArcStr, FlameGraphNode> = mutableMapOf(),
    var value: ULong? = null,
) {
    // fn write<'a>(&'a self, writer: &mut FlameGraphWriter, stack: &mut Vec<&'a str>)
    fun write(writer: FlameGraphWriter, stack: MutableList<String>) {
        value?.let { v ->
            writer.write(stack, v)
        }
        for ((k, v) in children) {
            stack.add(k.toString())
            v.write(writer, stack)
            stack.removeLast()
        }
    }

    /// Add value to the node.
    // pub(crate) fn add(&mut self, value: u64)
    fun add(value: ULong) {
        val existing = this.value
        if (existing == null) {
            this.value = value
        } else {
            this.value = existing + value
        }
    }

    // pub(crate) fn merge(&mut self, other: &FlameGraphNode)
    fun merge(other: FlameGraphNode) {
        other.value?.let { value -> add(value) }
        for ((k, v) in other.children) {
            child(k).merge(v)
        }
    }

    /// Get or create a child node.
    // pub(crate) fn child(&mut self, name: ArcStr) -> &mut FlameGraphNode
    fun child(name: ArcStr): FlameGraphNode =
        children.getOrPut(name) { FlameGraphNode() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlameGraphNode) return false
        return children == other.children && value == other.value
    }

    override fun hashCode(): Int = children.hashCode() * 31 + value.hashCode()
}

/// Profiling data as flame tree.
///
/// Can be written to `flamegraph.pl` format.
// #[derive(Debug, Clone, Default, PartialEq, Eq)]
// pub(crate) struct FlameGraphData
// TODO: stub - FlameGraphData needs real import
internal class FlameGraphData(
    val root: FlameGraphNode = FlameGraphNode(),
) {
    // pub(crate) fn write(&self) -> String
    fun write(): String {
        val writer = FlameGraphWriter()
        val stack = mutableListOf<String>()
        root.write(writer, stack)
        check(stack.isEmpty())
        return writer.finish()
    }

    // pub(crate) fn root(&mut self) -> &mut FlameGraphNode
    fun root(): FlameGraphNode = root

    companion object {
        // pub(crate) fn merge(graphs: impl IntoIterator<Item = &'a FlameGraphData>) -> FlameGraphData
        fun merge(graphs: Iterable<FlameGraphData>): FlameGraphData {
            val result = FlameGraphData()
            for (graph in graphs) {
                result.root.merge(graph.root)
            }
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlameGraphData) return false
        return root == other.root
    }

    override fun hashCode(): Int = root.hashCode()
}

// pub(crate) struct FlameGraphWriter
internal class FlameGraphWriter {
    private val buf: StringBuilder = StringBuilder()

    // pub(crate) fn write<'s>(&mut self, key: impl IntoIterator<Item = &'s str>, value: u64)
    fun write(key: List<String>, value: ULong) {
        if (key.isEmpty()) {
            buf.appendLine("(unknown) $value")
        } else {
            buf.appendLine("${key.joinToString(";")} $value")
        }
    }

    // pub(crate) fn finish(self) -> String
    fun finish(): String = buf.toString()
}

// --- Tests ---

// #[test] fn test_flamegraph_writer()
internal fun testFlamegraphWriter() {
    val writer = FlameGraphWriter()
    writer.write(listOf("aa", "bb"), 20u)
    check("aa;bb 20\n" == writer.finish())
}

// #[test] fn test_flamegraph_data()
internal fun testFlamegraphData() {
    val data = FlameGraphData()
    data.root().child(ArcStr("a")).add(10u)
    data.root().child(ArcStr("a")).child(ArcStr("b")).add(20u)
    data.root().child(ArcStr("a")).add(30u)
    val result = data.write()
    check("a 40\na;b 20\n" == result)
}

// #[test] fn test_merge()
internal fun testMerge() {
    val a = FlameGraphData()
    a.root().add(10u)
    a.root().child(ArcStr("a")).add(100u)
    a.root().child(ArcStr("b")).child(ArcStr("c")).add(1000u)
    val b = FlameGraphData()
    b.root().add(20u)
    b.root().child(ArcStr("a")).add(200u)

    val c = FlameGraphData.merge(listOf(a, b))

    val expected = FlameGraphData()
    expected.root().add(30u)
    expected.root().child(ArcStr("a")).add(300u)
    expected.root().child(ArcStr("b")).child(ArcStr("c")).add(1000u)

    check(expected == c)
}
