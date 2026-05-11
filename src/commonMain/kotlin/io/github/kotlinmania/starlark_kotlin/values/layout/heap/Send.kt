// port-lint: source src/values/layout/heap/send.rs
package io.github.kotlinmania.starlark_kotlin.values.layout.heap

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

// unsafe impl Send for FrozenValue {}
// unsafe impl Sync for FrozenValue {}

// unsafe impl Send for Value<'static> {}
// unsafe impl Sync for Value<'static> {}

/**
 * A trait for handling the unusual sendness requirements of starlark values.
 *
 * Semantically, the send and sync impls in starlark exist in support of the following goals, from
 * more obvious to less obvious:
 *
 *  1. Frozen heaps and values should be fully thread safe - parallel starlark evaluations must be
 *     able to depend on the same frozen heap.
 *  2. Unfrozen values should support non-thread safe interior mutability such as `RefCell`; in
 *     other words, they must support being non-sync.
 *  3. Unfrozen heaps should be sendable. Concretely, it should be possible to hold them over an
 *     await point so as to interleave starlark evaluation with other async work.
 *
 * Since a `Value<'v>` is semantically a reference to an unfrozen value, an immediate consequence
 * of 2 is that `Value<'v>` cannot `Send`. Given that starlark values need to contain `Value<'v>`s,
 * this makes the third condition weird. However, we achieve soundness by requiring a combination
 * of two properties:
 *
 *  a. Unfrozen starlark values must be send up to any `Value<'v>` inside them. In other words,
 *     they can contain a `Vec<Value<'v>>` or whatever, but not a `Rc<u8>`.
 *
 *  b. When we send a heap to another thread, we enforce that the heap is sent "in its entirety",
 *     ie together with any references into that heap.
 *
 * To be able to check these requirements, our heap allocation functions have a signature that
 * looks (slightly simplified) like this:
 *
 * ```rust,ignore
 * impl<'v> Heap<'v> {
 *     fn alloc(self, x: T) -> Value<'v>
 *     where
 *         T: StarlarkValue<'v>,
 *         T: ProvidesStaticType<'v>,
 *         T::StaticType: Send;
 * }
 * ```
 *
 * The first trait bound is obvious.
 *
 * The `T: ProvidesStaticType<'v>` trait bound says that all lifetimes appearing on `T` must be
 * `'v`. Together with branding, this guarantees that any `Value`s that `T` holds are `Value<'v>`s
 * for the same `'v` that our heap has, ie that the contained values point back into the same heap.
 * This, together with branding on heap access, is how we achieve the second requirement above. It
 * prevents `Value<'v>`s from leaking either out of the heap access closure or into any heap other
 * than the one they're associated with.
 *
 * The third impl is how we achieve the first requirement above. As a reminder, we would like to
 * write `T: Send`, but that would prevent users from holding any `Value<'v>` in `T`. So instead,
 * we replace the `T: Send` bound with a combination of the following two things:
 *
 * ```rust,ignore
 * // Bound. `T::StaticType` is effectively "`T` with all lifetimes replaced with `'static`
 * T::StaticType: Send;
 * // impl
 * impl Send for Value<'static>;
 * ```
 *
 *  `T::StaticType`, while not quite equivalent, is *almost* as good as `T: Send`. On the other
 *  hand, the `'static` thing together with the impl on `Value` means that it is actually satisfied
 *  for a `T` that contains a `Value<'v>`, which is what we want.
 */
// pub trait HeapSendable<'v>: sealed_send::Sealed {}
interface HeapSendable

// impl<'v, T: ProvidesStaticType<'v>> HeapSendable<'v> for T
// where <T as ProvidesStaticType<'v>>::StaticType: Send {}

// mod sealed_send {
//     pub trait Sealed {}
//     impl<'v, T: ProvidesStaticType<'v>> Sealed for T
//     where <T as ProvidesStaticType<'v>>::StaticType: Send {}
// }

/**
 * The sync analogue of [HeapSendable].
 *
 * Mostly see the docs on [HeapSendable], which is slightly more interesting - this one is just
 * needed on frozen heaps.
 */
// pub trait HeapSyncable<'v>: sealed_sync::Sealed {}
interface HeapSyncable

// impl<'v, T: ProvidesStaticType<'v>> HeapSyncable<'v> for T
// where <T as ProvidesStaticType<'v>>::StaticType: Sync {}

// mod sealed_sync {
//     pub trait Sealed {}
//     impl<'v, T: ProvidesStaticType<'v>> Sealed for T
//     where <T as ProvidesStaticType<'v>>::StaticType: Sync {}
// }

/**
 * A helper to pass the send-if-static property through `dyn Trait`.
 *
 * Unfortunately, the property of starlark values that they are send for `'v = 'static` does not
 * cleanly pass through `dyn MyTrait<'v>`; concretely, it's not possible to make `dyn
 * MyTrait<'static>: Send` even if that is true of the underlying concrete type.
 *
 * This type acts as a wrapper to recover the send impl; where previously you might've written
 *
 * ```rust,ignore
 * trait MyTrait<'v>: Trace {}
 *
 * struct MyValue<'v> {
 *     field: Box<dyn MyTrait<'v>>,
 * }
 * ```
 *
 * Now write instead:
 *
 * ```rust,ignore
 * trait MyTrait<'v>: Trace + HeapSendable<'v> {}
 *
 * struct MyValue<'v> {
 *     field: Box<DynStarlark<dyn MyTrait<'v>>>,
 * }
 * ```
 */
// #[derive(Trace, Allocative)]
// pub struct DynStarlark<'v, T>(PhantomData<dyn HeapSendable<'v>>, T)
// where T: ?Sized;
class DynStarlark<T>(
    private var inner: T,
) {
    companion object {
        // pub fn new(v: T) -> Self
        fun <T> new(v: T): DynStarlark<T> = DynStarlark(v)
    }

    // pub fn into_inner(self) -> T
    fun intoInner(): T = inner

    // impl Deref for DynStarlark
    // fn deref(&self) -> &Self::Target
    fun get(): T = inner

    // impl DerefMut for DynStarlark
    // fn deref_mut(&mut self) -> &mut Self::Target
    fun set(value: T) {
        inner = value
    }

    // impl Debug for DynStarlark
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
    fun debug(): String = inner.toString()

    // impl Display for DynStarlark
    // fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result
    override fun toString(): String = inner.toString()
}
