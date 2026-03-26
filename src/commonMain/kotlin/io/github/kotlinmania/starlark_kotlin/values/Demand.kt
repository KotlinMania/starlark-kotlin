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

// use std::any::TypeId;
// use std::marker::PhantomData;
// use crate::any::AnyLifetime;
// use crate::values::Value;
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import io.github.kotlinmania.starlark_kotlin.values.StarlarkValue
import io.github.kotlinmania.starlark_kotlin.values.layout.Value
import io.github.kotlinmania.starlark_kotlin.values.layout.heap.Heap

/**
 * Taken by [StarlarkValue.provide]
 * to provide different data depending on the type.
 */
// pub struct Demand<'a, 'v> {
//     type_id_of_t: TypeId,
//     /// `&'a mut Option<T>`.
//     option: *mut (),
//     _marker: PhantomData<&'a mut &'v ()>,
// }
class Demand @PublishedApi internal constructor(
    @PublishedApi internal val typeIdOfT: KClass<*>,
    @PublishedApi internal var option: Any? = null,
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
        // if self.type_id_of_t == T::static_type_id()
        if (this.typeIdOfT.isInstance(value)) {
            // unsafe { *(self.option as *mut Option<T>) = Some(value) };
            this.option = value
            this.filled = true
        }
    }

    /**
     * Similar to [provideValue], but does not require implementing `ProvidesStaticType`.
     */
    // pub(crate) fn provide_ref_static<T: 'static + ?Sized>(&mut self, value: &'v T)
    internal fun provideRefStatic(value: Any) {
        // if self.type_id_of_t == TypeId::of::<&'static T>()
        if (this.typeIdOfT.isInstance(value)) {
            // unsafe { *(self.option as *mut Option<&'v T>) = Some(value) };
            this.option = value
            this.filled = true
        }
    }

    companion object {
        // fn new<T: AnyLifetime<'v>>(option: &mut Option<T>) -> Demand<'a, 'v>
        // Demand { type_id_of_t: T::static_type_id(), option: option as *mut _ as *mut (), _marker: PhantomData }
        @PublishedApi
        internal inline fun <reified T : Any> new(): Demand {
            return Demand(typeIdOfT = T::class, option = null, filled = false)
        }
    }
}

// pub(crate) fn request_value_impl<'v, T: AnyLifetime<'v>>(value: Value<'v>) -> Option<T>
@PublishedApi
internal inline fun <reified T : Any> requestValueImpl(value: Value): T? {
    // let mut option = None;
    // value.get_ref().provide(&mut Demand::new(&mut option));
    // option
    val demand: Demand = Demand.new<T>()
    value.getRef().provide(demand)
    @Suppress("UNCHECKED_CAST")
    return if (demand.filled) demand.option as? T else null
}

// #[cfg(test)]
// mod tests {

// use allocative::Allocative;
// use starlark_derive::NoSerialize;
// use starlark_derive::starlark_value;
// use crate as starlark;
// use crate::any::ProvidesStaticType;
// use crate::starlark_simple_value;
// use crate::values::Heap;
// use crate::values::StarlarkValue;
// use crate::values::demand::Demand;

/** A trait for testing the demand/provide mechanism. */
// trait SomeTrait { fn payload(&self) -> u32; }
internal interface SomeTrait {
    fun payload(): UInt
}

// unsafe impl<'v> ProvidesStaticType<'v> for &'v dyn SomeTrait {
//     type StaticType = &'static dyn SomeTrait;
// }

// #[derive(ProvidesStaticType, derive_more::Display, Debug, NoSerialize, Allocative)]
// #[display("SomeType")]
// struct MyValue { payload: u32 }
//
// impl SomeTrait for MyValue { fn payload(&self) -> u32 { self.payload } }
//
// starlark_simple_value!(MyValue);
//
// #[starlark_value(type = "MyValue")]
// impl<'v> StarlarkValue<'v> for MyValue {
//     fn provide(&'v self, demand: &mut Demand<'_, 'v>) {
//         demand.provide_value::<&dyn SomeTrait>(self);
//     }
// }
internal class MyValue(val payload: UInt) : StarlarkValue, SomeTrait {
    override val TYPE: String get() = "MyValue"
    override fun payload(): UInt = this.payload
    override fun toString(): String = "SomeType"
    override fun provide(demand: Demand) {
        demand.provideValue(this as SomeTrait)
    }
}

// #[test]
// fn test_trait_downcast() {
//     Heap::temp(|heap| {
//         let value = heap.alloc_simple(MyValue { payload: 17 });
//         assert!(value.request_value::<String>().is_none());
//         let some_trait = value.request_value::<&dyn SomeTrait>().unwrap();
//         assert_eq!(17, some_trait.payload());
//     });
// }
internal fun testTraitDowncast() {
    Heap.temp { heap: Heap ->
        val value: Value = heap.alloc(MyValue(payload = 17u))
        val stringRequest: String? = value.requestValue<String>()
        check(stringRequest == null)
        val someTrait: SomeTrait = value.requestValue<MyValue>()!!
        val payloadResult: UInt = someTrait.payload()
        check(payloadResult == 17u)
    }
}

// } // mod tests
