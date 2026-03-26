// port-lint: source src/values/types/type_instance_id.rs
package io.github.kotlinmania.starlark_kotlin.values.types

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


/** Globally unique identifier for a type, like record type or enum type. */
data class TypeInstanceId(private val id: Long) : Comparable<TypeInstanceId> {

    override fun compareTo(other: TypeInstanceId): Int {
        return id.compareTo(other.id)
    }

    companion object {
        private val lastId = atomic(0L)

        /** Generate a new unique identifier. */
        fun gen(): TypeInstanceId {
            return TypeInstanceId(lastId.incrementAndGet())
        }
    }
}
