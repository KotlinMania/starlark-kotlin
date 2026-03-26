// port-lint: source src/values/demand.rs
package io.github.kotlinmania.starlark_kotlin.values.demand

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

import kotlin.reflect.KClass
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/**
 * Taken by [StarlarkValue.provide]
 * to provide different data depending on the type.
 */
// pub struct Demand<'a, 'v>
class Demand @PublishedApi internal constructor(
    // type_id_of_t: TypeId
    @PublishedApi internal val typeIdOfT: KClass<*>,
    // option: *mut ()
    @PublishedApi internal var option: Any? = null,
    // _marker: PhantomData<&'a mut &'v ()>
    @PublishedApi internal var filled: Boolean = false,
) {
    // impl<'a, 'v> Demand<'a, 'v>

    /**
     * Provide a value of given type.
     *
     * If type matches the type requested from [Value.requestValue], the value is stored
     * inside the [Demand] and later returned, otherwise the value is discarded.
     */
    // pub fn provide_value<T: AnyLifetime<'v>>(&mut self, value: T)
    fun provideValue(value: Any) {
        if (typeIdOfT.isInstance(value)) {
            // SAFETY: check checked type.
            option = value
            filled = true
        }
    }

    /**
     * Similar to [provideValue], but does not require implementing `ProvidesStaticType`.
     */
    // pub(crate) fn provide_ref_static<T: 'static + ?Sized>(&mut self, value: &'v T)
    internal fun provideRefStatic(value: Any) {
        if (typeIdOfT.isInstance(value)) {
            // SAFETY: check checked type.
            option = value
            filled = true
        }
    }
}

// fn new<T: AnyLifetime<'v>>(option: &mut Option<T>) -> Demand<'a, 'v>
@PublishedApi
internal inline fun <reified T : Any> newDemand(): Demand = Demand(typeIdOfT = T::class)

// pub(crate) fn request_value_impl<'v, T: AnyLifetime<'v>>(value: Value<'v>) -> Option<T>
internal inline fun <reified T : Any> requestValueImpl(value: Value): T? {
    val demand = newDemand<T>()
    value.getRef().provide(demand)
    @Suppress("UNCHECKED_CAST")
    return if (demand.filled) demand.option as? T else null
}

// #[cfg(test)]
// mod tests

/**
 * A trait for testing the demand/provide mechanism.
 */
// trait SomeTrait
internal interface SomeTrait {
    /** Return the payload value. */
    // fn payload(&self) -> u32;
    fun payload(): UInt
}

/**
 * A test value type implementing [SomeTrait] and [StarlarkValue].
 */
// #[derive(ProvidesStaticType, derive_more::Display, Debug, NoSerialize, Allocative)]
// #[display("SomeType")]
// struct MyValue { payload: u32 }
internal class MyValue(val payload: UInt) : StarlarkValue, SomeTrait {
    // starlark_simple_value!(MyValue);
    // #[starlark_value(type = "MyValue")]
    override val TYPE: String get() = "MyValue"

    // impl SomeTrait for MyValue
    // fn payload(&self) -> u32 { self.payload }
    override fun payload(): UInt = payload

    override fun toString(): String = "SomeType"

    // fn provide(&'v self, demand: &mut Demand<'_, 'v>)
    override fun provide(demand: Demand) {
        // demand.provide_value::<&dyn SomeTrait>(self);
        demand.provideValue(this)
    }
}

// #[test]
// fn test_trait_downcast()
internal fun testTraitDowncast() {
    Heap.temp { heap ->
        // let value = heap.alloc_simple(MyValue { payload: 17 });
        heap.alloc(MyValue(payload = 17u)).let { value ->
            // assert!(value.request_value::<String>().is_none());
            check(value.requestValue<String>() == null)
            // let some_trait = value.request_value::<&dyn SomeTrait>().unwrap();
            // assert_eq!(17, some_trait.payload());
            check(17u == value.requestValue<MyValue>()!!.payload())
        }
    }
}
