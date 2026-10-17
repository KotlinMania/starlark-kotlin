# High Priority Ports - Action Plan

## Files by Impact

Priority = deps * 1,000,000 + SymDeficit * 10,000 + SrcSymbols * 100 + (1 - function similarity) * 10

Dependency fanout is ranked first so the ladder favors ports that clear downstream compilation failures fastest.

This list is complete and includes function/type detail for every matched file. Function similarity is the required body/parameter comparison; file-level shape does not rescue a port.

| Rank | Source | Target | Function similarity | Deps | Functions | Missing functions | Types | Missing types | SymDeficit | SrcSymbols | Priority |
|------|--------|--------|------------|------|-----------|-------------------|-------|---------------|-----------|------------|----------|
| 1 | `layout.value` | `layout.Value` | 0.33 | 178 | 142/155 matched (target 166) | `fmt`, `fmt`, `fmt`, `fmt`, `eq`, `eq`, `testing_new_int`, `display_for_type_error`, `fmt`, `testing_new_int`, `to_value`, `compare`, `_test_send_sync` | 7/9 matched (target 11) | `Canonical`, `String` | 15 | 164 | 178166400.0 |
| 2 | `typing.ty` | `typing.Ty` | 0.39 | 109 | 49/52 matched (target 57) | `fmt`, `fmt`, `fmt` | 4/4 matched (target 6) | _none_ | 3 | 56 | 109035608.0 |
| 3 | `typing.starlark_value` | `typing.StarlarkValue [ZERO]` | 0.00 | 76 | 29/35 matched (target 45) | `fmt`, `fmt`, `eq`, `hash`, `partial_cmp`, `cmp` | 4/4 matched (target 5) | _none_ | 6 | 39 | 76063912.0 |
| 4 | `params.display` | `params.Display` | 0.26 | 76 | 4/4 matched | _none_ | 3/3 matched (target 8) | _none_ | 0 | 7 | 76000712.0 |
| 5 | `runtime.evaluator` | `runtime.Evaluator [ZERO]` | 0.00 | 55 | 60/64 matched (target 63) | `drop`, `new`, `add_diagnostics`, `before_stmt` | 7/7 matched (target 17) | _none_ | 4 | 71 | 55047112.0 |
| 6 | `values.trace` | `values.Trace [ZERO]` | 0.00 | 52 | 43/43 matched | _none_ | 1/1 matched | _none_ | 0 | 44 | 52004408.0 |
| 7 | `values.freeze` | `values.Freeze [ZERO]` | 0.00 | 42 | 23/26 matched (target 31) | `freeze`, `freeze`, `freeze` | 1/2 matched (target 6) | `Frozen` | 4 | 28 | 42042808.0 |
| 8 | `values.alloc_value` | `values.AllocValue [ZERO]` | 0.00 | 42 | 5/5 matched | _none_ | 4/4 matched | _none_ | 0 | 9 | 42000912.0 |
| 9 | `layout.freezer` | `layout.Freezer [ZERO]` | 0.00 | 36 | 5/5 matched | _none_ | 1/1 matched | _none_ | 0 | 6 | 36000608.0 |
| 10 | `compiler.span` | `compiler.Span` | 0.23 | 29 | 2/2 matched | _none_ | 1/2 matched (target 1) | `Target` | 1 | 4 | 29010408.0 |
| 11 | `values.frozen_ref` | `values.FrozenRef [ZERO]` | 0.00 | 27 | 20/20 matched (target 23) | _none_ | 2/4 matched (target 2) | `Target`, `Frozen` | 2 | 24 | 27022410.0 |
| 12 | `none.none_type` | `none.NoneType [ZERO]` | 0.00 | 27 | 11/11 matched (target 16) | _none_ | 1/2 matched | `Error` | 1 | 13 | 27011310.0 |
| 13 | `runtime.frame_span` | `runtime.FrameSpan` | 0.24 | 26 | 3/4 matched | `fmt` | 1/1 matched | _none_ | 1 | 5 | 26010508.0 |
| 14 | `runtime.arguments` | `runtime.Arguments [ZERO]` | 0.00 | 25 | 37/40 matched (target 55) | `get_index_from_param_spec`, `small_hash`, `f` | 8/8 matched (target 17) | _none_ | 3 | 48 | 25034810.0 |
| 15 | `typing.type_compiled` | `type_compiled.TypeCompiled [ZERO]` | 0.00 | 22 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 22000010.0 |
| 16 | `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 0.00 | 20 | 15/20 matched (target 24) | `fmt`, `fmt`, `assert_send_sync`, `starlark_type_repr`, `_assert_covariant` | 4/7 matched (target 8) | `Canonical`, `Frozen`, `Error` | 8 | 27 | 20082710.0 |
| 17 | `environment.globals` | `environment.Globals [ZERO]` | 0.00 | 20 | 34/39 matched (target 42) | `get`, `new`, `test_send_sync`, `register_foo`, `foo` | 5/5 matched (target 7) | _none_ | 5 | 44 | 20054410.0 |
| 18 | `util.refcell` | `refcell.RefCell` | 0.28 | 20 | 2/2 matched (target 12) | _none_ | 0/0 matched (target 4) | _none_ | 0 | 2 | 20000208.0 |
| 19 | `environment.methods` | `environment.Methods [ZERO]` | 0.00 | 17 | 19/21 matched (target 26) | `new`, `get_methods` | 4/4 matched (target 5) | _none_ | 2 | 25 | 17022510.0 |
| 20 | `values.iter` | `values.Iter [ZERO]` | 0.00 | 17 | 4/5 matched (target 6) | `drop` | 1/2 matched (target 1) | `Item` | 2 | 7 | 17020710.0 |
| 21 | `values.error` | `values.Error` | 0.28 | 17 | 4/5 matched | `from` | 2/2 matched (target 20) | _none_ | 1 | 7 | 17010708.0 |
| 22 | `private` | `starlark.Private [ZERO]` | 0.00 | 15 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 15000110.0 |
| 23 | `collections.symbol` | `collections.Symbol [ZERO]` | 0.00 | 15 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 15000010.0 |
| 24 | `layout.avalue` | `layout.AValue [ZERO]` | 0.00 | 14 | 8/8 matched (target 13) | _none_ | 3/3 matched (target 4) | _none_ | 0 | 11 | 14001110.0 |
| 25 | `typing.tuple` | `typing.Tuple [ZERO]` | 0.00 | 12 | 5/6 matched (target 9) | `fmt` | 1/1 matched (target 3) | _none_ | 1 | 7 | 12010710.0 |
| 26 | `layout.const_frozen_string` | `layout.ConstFrozenString [ZERO]` | 0.00 | 12 | 2/2 matched (target 3) | _none_ | 0/0 matched (target 1) | _none_ | 0 | 2 | 12000210.0 |
| 27 | `layout.value_lifetimeless` | `layout.ValueLifetimeless` | 1.00 | 12 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 12000100.0 |
| 28 | `int.inline_int` | `int.InlineInt [ZERO]` | 0.00 | 11 | 29/42 matched (target 43) | `fmt`, `testing_new`, `try_from_impl`, `try_from`, `try_from`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `eq`, `partial_cmp`, `test_min_max_for_bits` | 2/5 matched (target 6) | `Error`, `Output`, `Canonical` | 16 | 47 | 11164710.0 |
| 29 | `int.pointer_i32` | `int.PointerI32 [ZERO]` | 0.00 | 9 | 28/32 matched (target 34) | `eq`, `fmt`, `fmt`, `serialize` | 1/2 matched | `Canonical` | 5 | 34 | 9053410.0 |
| 30 | `types.type_instance_id` | `types.TypeInstanceId` | 0.00 | 9 | 0/1 matched (target 2) | `r#gen` | 1/1 matched | _none_ | 1 | 2 | 9010210.0 |
| 31 | `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 0.00 | 8 | 6/13 matched (target 15) | `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub` | 1/2 matched (target 1) | `Output` | 8 | 15 | 8081510.0 |
| 32 | `any` | `starlark.Any` | 0.12 | 8 | 8/13 matched (target 17) | `is`, `convert_value`, `convert_any`, `test`, `test` | 14/15 matched (target 50) | `StaticType` | 6 | 28 | 8062809.0 |
| 33 | `eval.compiler` | `eval.Compiler [ZERO]` | 0.00 | 8 | 6/6 matched | _none_ | 1/1 matched | _none_ | 0 | 7 | 8000710.0 |
| 34 | `cast` | `starlark.Cast [ZERO]` | 0.00 | 8 | 3/3 matched (target 4) | _none_ | 0/0 matched | _none_ | 0 | 3 | 8000310.0 |
| 35 | `types.bigint` | `types.Bigint [ZERO]` | 0.00 | 7 | 71/73 matched (target 77) | `unpack_integer`, `eq` | 1/1 matched (target 2) | _none_ | 2 | 74 | 7027410.0 |
| 36 | `runtime.frozen_file_span` | `runtime.FrozenFileSpan [ZERO]` | 0.00 | 7 | 9/10 matched | `fmt` | 1/1 matched | _none_ | 1 | 11 | 7011110.0 |
| 37 | `values.starlark_type_id` | `values.StarlarkTypeId` | 0.27 | 7 | 5/6 matched (target 7) | `eq` | 2/2 matched | _none_ | 1 | 8 | 7010807.5 |
| 38 | `compiler.opt_ctx` | `compiler.OptCtx` | 0.18 | 7 | 13/13 matched | _none_ | 2/2 matched (target 4) | _none_ | 0 | 15 | 7001508.0 |
| 39 | `type_compiled.type_matcher_factory` | `type_compiled.TypeMatcherFactory` | 0.21 | 7 | 3/3 matched (target 6) | _none_ | 3/3 matched | _none_ | 0 | 6 | 7000608.0 |
| 40 | `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 0.00 | 6 | 4/10 matched (target 9) | `from_millis`, `add_assign`, `add_assign`, `add`, `add`, `sum` | 1/2 matched (target 1) | `Output` | 7 | 12 | 6071210.0 |
| 41 | `values.freeze_error` | `values.FreezeError [ZERO]` | 0.00 | 6 | 3/5 matched (target 6) | `from`, `from` | 3/4 matched (target 3) | `FreezeResult` | 3 | 9 | 6030910.0 |
| 42 | `typing.typecheck` | `typing.Typecheck [STUB]` | 0.00 | 6 | 2/5 matched | `fmt`, `find_bindings_by_name`, `find_first_binding` | 2/2 matched (target 3) | _none_ | 3 | 7 | 6030710.0 |
| 43 | `dict.dict_type` | `dict.DictType [ZERO]` | 0.00 | 6 | 1/2 matched (target 4) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 5 | 6030510.0 |
| 44 | `none.none_or` | `none.NoneOr` | 0.19 | 6 | 7/7 matched (target 9) | _none_ | 1/3 matched (target 4) | `Canonical`, `Error` | 2 | 10 | 6021008.0 |
| 45 | `layout.value_alloc_size` | `layout.ValueAllocSize [ZERO]` | 0.00 | 6 | 4/5 matched | `layout` | 1/1 matched | _none_ | 1 | 6 | 6010610.0 |
| 46 | `compiler.stmt` | `compiler.Stmt [ZERO]` | 0.00 | 6 | 28/28 matched | _none_ | 7/7 matched (target 24) | _none_ | 0 | 35 | 6003510.0 |
| 47 | `profile.profiler_type` | `profile.ProfilerType` | 0.25 | 6 | 1/1 matched | _none_ | 2/2 matched | _none_ | 0 | 3 | 6000307.5 |
| 48 | `values.layout` | `values.Layout [ZERO]` | 0.00 | 6 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 6000010.0 |
| 49 | `types.array` | `types.Array [ZERO]` | 0.00 | 5 | 23/34 matched (target 24) | `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `fmt`, `is_special`, `serialize`, `debug`, `display`, `push` | 2/2 matched | _none_ | 11 | 36 | 5113610.0 |
| 50 | `typing.arc_ty` | `typing.ArcTy` | 0.34 | 5 | 6/8 matched (target 16) | `fmt`, `fmt` | 3/4 matched (target 10) | `Target` | 3 | 12 | 5031206.5 |
| 51 | `typing.interface` | `typing.Interface` | 0.19 | 5 | 3/3 matched | _none_ | 1/1 matched | _none_ | 0 | 4 | 5000408.0 |
| 52 | `scope.scope_resolver_globals` | `scope.ScopeResolverGlobals` | 0.19 | 5 | 3/3 matched | _none_ | 1/1 matched | _none_ | 0 | 4 | 5000408.0 |
| 53 | `eval.bc` | `bc.Bc [ZERO]` | 0.00 | 5 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 5000010.0 |
| 54 | `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 0.00 | 4 | 33/38 matched (target 37) | `get_or_init_ty`, `get_ty`, `fmt`, `r#type`, `values` | 4/8 matched (target 7) | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` | 9 | 46 | 4094610.0 |
| 55 | `bc.frame` | `bc.Frame [ZERO]` | 0.00 | 4 | 25/33 matched (target 31) | `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit` | 2/2 matched | _none_ | 8 | 35 | 4083510.0 |
| 56 | `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 0.00 | 4 | 9/16 matched (target 23) | `fmt`, `fmt`, `fmt`, `new`, `alloc_value`, `compiler_args_globals`, `compiler_args` | 3/4 matched | `Canonical` | 8 | 20 | 4082010.0 |
| 57 | `values.value_of` | `values.ValueOf [ZERO]` | 0.00 | 4 | 4/6 matched (target 5) | `deref`, `fmt` | 1/4 matched (target 1) | `Target`, `Canonical`, `Error` | 5 | 10 | 4051010.0 |
| 58 | `profile.alloc_counts` | `profile.AllocCounts` | 0.18 | 4 | 1/4 matched (target 5) | `normalize_for_golden_tests`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 4 | 6 | 4040608.2 |
| 59 | `values.demand` | `values.Demand` | 0.17 | 4 | 5/7 matched (target 8) | `payload`, `provide` | 3/4 matched | `StaticType` | 3 | 11 | 4031108.2 |
| 60 | `bc.native_function` | `bc.NativeFunction` | 0.18 | 4 | 3/4 matched | `fun` | 1/1 matched | _none_ | 1 | 5 | 4010508.2 |
| 61 | `types.ellipsis` | `types.Ellipsis` | 0.38 | 4 | 3/3 matched (target 5) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 4 | 4000406.2 |
| 62 | `record.record_type` | `record.RecordType [ZERO]` | 0.00 | 3 | 20/24 matched (target 23) | `get_or_init_ty`, `get_ty`, `fmt`, `r#type` | 2/8 matched (target 3) | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` | 10 | 32 | 3103210.0 |
| 63 | `alloc.chunk` | `alloc.Chunk [ZERO]` | 0.00 | 3 | 12/21 matched (target 20) | `fmt`, `begin`, `fmt`, `ptr_eq`, `begin`, `drop`, `clone`, `counter_overflow`, `test_empty` | 2/3 matched | `ChunkDataEmpty` | 10 | 24 | 3102410.0 |
| 64 | `list.alloc` | `list.Alloc [ZERO]` | 0.00 | 3 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | 4 | 5 | 3040510.0 |
| 65 | `list.list_type` | `list.ListType` | 0.09 | 3 | 1/2 matched (target 5) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 5 | 3030509.0 |
| 66 | `stdlib.call_stack` | `stdlib.CallStack [ZERO]` | 0.00 | 3 | 11/13 matched (target 15) | `fmt`, `global` | 1/1 matched (target 2) | _none_ | 2 | 14 | 3021410.0 |
| 67 | `compiler.constants` | `compiler.Constants [ZERO]` | 0.00 | 3 | 2/4 matched (target 6) | `eq`, `eq` | 2/2 matched (target 3) | _none_ | 2 | 6 | 3020610.0 |
| 68 | `profile.instant` | `profile.Instant [ZERO]` | 0.00 | 3 | 3/4 matched (target 9) | `sub` | 1/2 matched (target 1) | `Output` | 2 | 6 | 3020610.0 |
| 69 | `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 0.00 | 3 | 2/2 matched | _none_ | 1/3 matched (target 1) | `Canonical`, `Error` | 2 | 5 | 3020510.0 |
| 70 | `errors.did_you_mean` | `errors.DidYouMean [ZERO]` | 0.00 | 3 | 6/6 matched (target 7) | _none_ | 0/0 matched (target 1) | _none_ | 0 | 6 | 3000610.0 |
| 71 | `sealed` | `starlark.Sealed [ZERO]` | 0.00 | 3 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 3000110.0 |
| 72 | `types.record` | `types.Record [ZERO]` | 0.00 | 3 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 3000010.0 |
| 73 | `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 0.00 | 2 | 4/11 matched (target 9) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter` | 1/4 matched (target 3) | `Target`, `Item`, `IntoIter` | 10 | 15 | 2101510.0 |
| 74 | `util.arc_or_static` | `util.ArcOrStatic` | 0.20 | 2 | 5/10 matched (target 9) | `fmt`, `eq`, `partial_cmp`, `cmp`, `hash` | 2/3 matched (target 4) | `Target` | 6 | 13 | 2061308.0 |
| 75 | `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 0.00 | 2 | 16/19 matched | `fmt`, `fmt`, `deref` | 2/3 matched (target 2) | `Target` | 4 | 22 | 2042210.0 |
| 76 | `typing.type_type` | `typing.TypeType` | 0.12 | 2 | 3/5 matched (target 8) | `module`, `takes_type` | 1/3 matched (target 2) | `Canonical`, `Error` | 4 | 8 | 2040808.8 |
| 77 | `alloc.chunk_part` | `alloc.ChunkPart` | 0.43 | 2 | 12/15 matched (target 17) | `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero` | 1/1 matched (target 2) | _none_ | 3 | 16 | 2031605.8 |
| 78 | `layout.const_type_id` | `layout.ConstTypeId [ZERO]` | 0.00 | 2 | 2/5 matched (target 4) | `fmt`, `eq`, `hash` | 1/1 matched | _none_ | 3 | 6 | 2030610.0 |
| 79 | `avalues.str_` | `avalues.Str [ZERO]` | 0.00 | 2 | 13/13 matched (target 15) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | 2 | 16 | 2021610.0 |
| 80 | `runtime.rust_loc` | `runtime.RustLoc [ZERO]` | 0.00 | 2 | 1/3 matched (target 4) | `rust_loc_globals`, `invoke` | 0/0 matched (target 1) | _none_ | 2 | 3 | 2020310.0 |
| 81 | `values.stack_guard` | `values.StackGuard [ZERO]` | 0.00 | 2 | 3/4 matched | `drop` | 1/1 matched | _none_ | 1 | 5 | 2010510.0 |
| 82 | `collections.aligned_padded_str` | `alignedpaddedstr.AlignedPaddedStr` | 0.06 | 2 | 2/3 matched (target 4) | `eq` | 1/1 matched | _none_ | 1 | 4 | 2010409.4 |
| 83 | `runtime.file_loader` | `runtime.FileLoader` | 0.09 | 2 | 2/2 matched | _none_ | 3/3 matched | _none_ | 0 | 5 | 2000509.1 |
| 84 | `profile.string_index` | `profile.StringIndex [ZERO]` | 0.00 | 2 | 2/2 matched | _none_ | 2/2 matched | _none_ | 0 | 4 | 2000410.0 |
| 85 | `collections.string_pool` | `collections.StringPool [ZERO]` | 0.00 | 2 | 2/2 matched | _none_ | 1/1 matched | _none_ | 0 | 3 | 2000310.0 |
| 86 | `def_inline.local_as_value` | `def_inline.LocalAsValue [ZERO]` | 0.00 | 2 | 1/1 matched (target 2) | _none_ | 1/1 matched | _none_ | 0 | 2 | 2000210.0 |
| 87 | `hint` | `starlark.Hint` | 0.72 | 2 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 2000202.8 |
| 88 | `values.thin_box_slice_frozen_value` | `values.ThinBoxSliceFrozenValue [ZERO]` | 0.00 | 2 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 2000010.0 |
| 89 | `heap.arena` | `heap.Arena [ZERO]` | 0.00 | 1 | 18/37 matched (target 23) | `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty` | 4/7 matched (target 6) | `ChunkIter`, `Item`, `ArenaUninit` | 22 | 44 | 1224410.0 |
| 90 | `collections.alloca` | `collections.Alloca [ZERO]` | 0.00 | 1 | 5/23 matched (target 5) | `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `drop`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat` | 1/4 matched (target 1) | `Buffer`, `Align`, `DropSliceGuard` | 21 | 27 | 1212710.0 |
| 91 | `stdlib` | `starlark.Stdlib [ZERO]` | 0.00 | 1 | 5/14 matched (target 16) | `global`, `nop`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2` | 2/3 matched | `Error` | 10 | 17 | 1101710.0 |
| 92 | `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 0.00 | 1 | 1/7 matched (target 5) | `default`, `starlark_type_repr`, `into_iter`, `into_iter`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 10 | 12 | 1101210.0 |
| 93 | `layout.pointer` | `layout.Pointer [ZERO]` | 0.00 | 1 | 39/47 matched (target 46) | `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `unpack_pointer_i32_unchecked`, `test_int_tag`, `check` | 5/5 matched | _none_ | 8 | 52 | 1085210.0 |
| 94 | `stdlib.breakpoint` | `stdlib.Breakpoint` | 0.30 | 1 | 14/19 matched (target 18) | `global`, `breakpoint`, `reset_global_state`, `read_line`, `println` | 5/6 matched (target 7) | `Handler` | 6 | 25 | 1062507.0 |
| 95 | `types.any_complex` | `types.AnyComplex [ZERO]` | 0.00 | 1 | 5/8 matched (target 9) | `fmt`, `fmt`, `freeze` | 3/5 matched (target 4) | `Canonical`, `Frozen` | 5 | 13 | 1051310.0 |
| 96 | `types.any_array` | `types.AnyArray [ZERO]` | 0.00 | 1 | 5/8 matched (target 9) | `fmt`, `drop`, `drop` | 2/3 matched | `Canonical` | 4 | 11 | 1041110.0 |
| 97 | `util.rtabort` | `util.Rtabort [ZERO]` | 0.00 | 1 | 3/6 matched | `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args` | 0/1 matched | `AbortOnDrop` | 4 | 7 | 1040710.0 |
| 98 | `bc.if_debug` | `bc.IfDebug [ZERO]` | 0.00 | 1 | 5/8 matched (target 9) | `eq`, `partial_cmp`, `cmp` | 1/1 matched | _none_ | 3 | 9 | 1030910.0 |
| 99 | `read_line` | `starlark.ReadLine [ZERO]` | 0.00 | 1 | 2/4 matched (target 2) | `new`, `read_line` | 1/2 matched (target 1) | `NoRustyline` | 3 | 6 | 1030610.0 |
| 100 | `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 0.00 | 1 | 1/3 matched (target 1) | `get_type_id`, `test_non_static_type_id` | 0/1 matched (target 0) | `NonStaticAny` | 3 | 4 | 1030410.0 |
| 101 | `runtime.cheap_call_stack` | `runtime.CheapCallStack [ZERO]` | 0.00 | 1 | 15/17 matched | `fmt`, `default` | 3/3 matched (target 6) | _none_ | 2 | 20 | 1022010.0 |
| 102 | `avalues.simple` | `avalues.Simple [ZERO]` | 0.00 | 1 | 9/9 matched (target 11) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | 2 | 12 | 1021210.0 |
| 103 | `layout.value_captured` | `layout.ValueCaptured` | 0.16 | 1 | 4/4 matched (target 9) | _none_ | 2/4 matched (target 2) | `Canonical`, `Frozen` | 2 | 8 | 1020808.4 |
| 104 | `record.field` | `record.Field [ZERO]` | 0.00 | 1 | 4/5 matched (target 10) | `fmt` | 0/1 matched | `FieldGen` | 2 | 6 | 1020610.0 |
| 105 | `string.dot_format` | `string.DotFormat` | 0.32 | 1 | 10/11 matched | `format_capture_for_test` | 1/1 matched (target 2) | _none_ | 1 | 12 | 1011206.8 |
| 106 | `typing.bindings` | `typing.Bindings [STUB]` | 0.00 | 1 | 7/8 matched (target 18) | `get_for_clause` | 3/3 matched (target 18) | _none_ | 1 | 11 | 1011110.0 |
| 107 | `typing.structs` | `typing.Structs` | 0.21 | 1 | 7/8 matched (target 9) | `fmt` | 2/2 matched | _none_ | 1 | 10 | 1011007.9 |
| 108 | `heap.fast_cell` | `heap.FastCell [ZERO]` | 0.00 | 1 | 6/7 matched | `drop` | 1/1 matched | _none_ | 1 | 8 | 1010810.0 |
| 109 | `structs.unordered_hasher` | `structs.UnorderedHasher [ZERO]` | 0.00 | 1 | 4/5 matched (target 4) | `_write` | 1/1 matched (target 2) | _none_ | 1 | 6 | 1010610.0 |
| 110 | `typing.function` | `typing.Function [STUB]` | 0.00 | 1 | 17/17 matched (target 24) | _none_ | 3/3 matched | _none_ | 0 | 20 | 1002010.0 |
| 111 | `string.interpolation` | `string.Interpolation [ZERO]` | 0.00 | 1 | 12/12 matched (target 14) | _none_ | 4/4 matched (target 21) | _none_ | 0 | 16 | 1001610.0 |
| 112 | `analysis.lint_message` | `analysis.LintMessage` | 0.34 | 1 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 1000206.6 |
| 113 | `typing` | `starlark.Typing [ZERO]` | 0.00 | 1 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000010.0 |
| 114 | `types.int` | `types.Int [ZERO]` | 0.00 | 1 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000010.0 |
| 115 | `types.bool` | `types.Bool` | 1.00 | 1 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000000.0 |
| 116 | `bc.instr_impl` | `bc.InstrImpl` | 0.38 | 0 | 87/87 matched (target 95) | _none_ | 87/163 matched (target 104) | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` | 76 | 250 | 785006.2 |
| 117 | `set.methods` | `set.Methods [STUB]` | 0.00 | 0 | 18/68 matched (target 19) | `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self` | 1/1 matched (target 3) | _none_ | 50 | 69 | 506910.0 |
| 118 | `string.str_type` | `string.StrType [ZERO]` | 0.00 | 0 | 12/50 matched (target 36) | `freeze`, `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `fmt`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `get_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_str` | 0/4 matched (target 1) | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` | 42 | 54 | 425410.0 |
| 119 | `int.int_or_big` | `int.IntOrBig [STUB]` | 0.00 | 0 | 37/62 matched (target 68) | `starlark_type_repr`, `from_str`, `starlark_type_repr`, `unpack_value_impl`, `from`, `from`, `bitand`, `bitor`, `bitxor`, `neg`, `neg`, `add`, `sub`, `mul`, `mul`, `mul`, `partial_cmp`, `cmp`, `eq`, `partial_cmp`, `eq`, `partial_cmp`, `int`, `floor_div`, `percent` | 3/7 matched (target 12) | `Canonical`, `Err`, `Error`, `Output` | 29 | 69 | 296910.0 |
| 120 | `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 0.00 | 0 | 10/29 matched (target 15) | `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit` | 1/3 matched (target 2) | `ThinBoxSliceLayout`, `Target` | 21 | 32 | 213210.0 |
| 121 | `layout.typed` | `layout.ValueTyped [ZERO]` | 0.00 | 0 | 36/51 matched (target 49) | `fmt`, `fmt`, `fmt`, `fmt`, `serialize`, `serialize`, `eq`, `eq`, `deref`, `deref`, `unpack_value_impl`, `unpack_value_impl`, `module`, `mutable`, `takes_frozen_value_typed` | 2/7 matched (target 3) | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` | 20 | 58 | 205810.0 |
| 122 | `values.typing.callable` | `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 0.00 | 0 | 30/47 matched (target 43) | `clone`, `fmt`, `trace`, `fmt`, `_assert_sync_send`, `_assert`, `clone`, `trace`, `clone`, `fmt`, `trace`, `my_module`, `accept_f`, `module`, `accept_f`, `good`, `bad` | 5/8 matched (target 7) | `Canonical`, `Error`, `Frozen` | 20 | 55 | 205510.0 |
| 123 | `stdlib.json` | `stdlib.Json` | 0.14 | 0 | 8/26 matched (target 28) | `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `starlark_type_repr`, `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `starlark_type_repr`, `starlark_type_repr`, `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `json`, `encode`, `decode` | 0/1 matched (target 12) | `Canonical` | 19 | 27 | 192708.6 |
| 124 | `runtime.visit_span` | `runtime.VisitSpan` | 0.00 | 0 | 0/19 matched | `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans` | 1/1 matched | _none_ | 19 | 20 | 192010.0 |
| 125 | `typing.user` | `typing.User [ZERO]` | 0.00 | 0 | 15/30 matched (target 37) | `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `get_type_starlark_repr`, `typechecker_ty`, `eval_type`, `invoke`, `alloc_value`, `get_type_starlark_repr`, `globals`, `fruit`, `mk_fruit` | 8/8 matched (target 12) | _none_ | 15 | 38 | 153810.0 |
| 126 | `scope.payload` | `scope.Payload [ZERO]` | 0.00 | 0 | 0/7 matched (target 0) | `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id` | 9/17 matched (target 14) | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` | 15 | 24 | 152410.0 |
| 127 | `stdlib.extra` | `stdlib.Extra` | 0.13 | 0 | 11/24 matched (target 28) | `filter`, `map`, `debug`, `fmt`, `print`, `print`, `pprint`, `pprint`, `pstr`, `pstr`, `prepr`, `prepr`, `println` | 3/4 matched | `PrintHandlerImpl` | 14 | 28 | 142808.7 |
| 128 | `tuple.rust_tuple` | `tuple.RustTuple` | 0.00 | 0 | 0/11 matched | `alloc_value`, `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `alloc_frozen_value`, `starlark_type_repr`, `starlark_type_repr`, `starlark_type_repr`, `unpack_value_impl`, `unpack_value_impl` | 0/2 matched (target 0) | `Canonical`, `Error` | 13 | 13 | 131310.0 |
| 129 | `typed.string` | `typed.String [ZERO]` | 0.00 | 0 | 14/26 matched (target 60) | `borrow`, `borrow`, `equivalent`, `equivalent`, `eq`, `eq`, `hash`, `hash`, `partial_cmp`, `cmp`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | 12 | 29 | 122910.0 |
| 130 | `profile.csv` | `profile.Csv` | 0.11 | 0 | 8/18 matched (target 11) | `new`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv` | 1/3 matched | `Impl`, `CsvValue` | 12 | 21 | 122108.9 |
| 131 | `layout.vtable` | `layout.Vtable` | 0.23 | 0 | 60/69 matched (target 65) | `value_ptr`, `value_ref`, `drop_in_place`, `fmt`, `new`, `as_allocative`, `total_memory_for_profile`, `as_serialize`, `new` | 4/6 matched (target 4) | `GetTypeId`, `GetAllocativeKey` | 11 | 75 | 117507.7 |
| 132 | `dict.value` | `dict.Value [ZERO]` | 0.00 | 0 | 56/64 matched (target 82) | `fmt`, `fmt`, `hash`, `equivalent`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `serialize` | 7/10 matched (target 11) | `Canonical`, `Frozen`, `ContentRef` | 11 | 74 | 117410.0 |
| 133 | `bigint.convert` | `bigint.Convert [ZERO]` | 0.00 | 0 | 19/28 matched (target 31) | `alloc_frozen_value`, `unpack_value_impl`, `unpack_value_impl`, `unpack_value_impl`, `unpack_value_impl`, `unpack_value_impl`, `module`, `takes_i32`, `takes_i64` | 0/2 matched (target 8) | `Canonical`, `Error` | 11 | 30 | 113010.0 |
| 134 | `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 0.00 | 0 | 9/19 matched (target 14) | `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `visit`, `default`, `fmt`, `eq`, `across_lengths` | 2/3 matched | `Target` | 11 | 22 | 112210.0 |
| 135 | `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 0.00 | 0 | 7/16 matched (target 9) | `alloc_frozen_value`, `alloc_frozen_string_value`, `starlark_type_repr`, `alloc_value`, `alloc_string_value`, `alloc_value`, `alloc_string_value`, `unpack_value_impl`, `unpack_value_impl` | 0/2 matched (target 1) | `Canonical`, `Error` | 11 | 18 | 111810.0 |
| 136 | `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 0.00 | 0 | 4/13 matched (target 12) | `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered` | 2/4 matched (target 8) | `TestSimpleType`, `TestComplexGen` | 11 | 17 | 111710.0 |
| 137 | `record.globals` | `record.Globals [ZERO]` | 0.00 | 0 | 1/12 matched (target 1) | `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid` | 0/0 matched | _none_ | 11 | 12 | 111210.0 |
| 138 | `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 0.00 | 0 | 36/44 matched (target 48) | `fmt`, `check_matches`, `fmt`, `starlark_type_repr`, `alloc_value`, `matches`, `hash`, `eq` | 5/7 matched (target 12) | `StaticType`, `Canonical` | 10 | 51 | 105110.0 |
| 139 | `dict.traits` | `dict.Traits` | 0.10 | 0 | 6/14 matched (target 7) | `alloc_value`, `alloc_frozen_value`, `alloc_value`, `alloc_frozen_value`, `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `starlark_type_repr` | 0/2 matched (target 6) | `Canonical`, `Error` | 10 | 16 | 101609.0 |
| 140 | `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 0.13 | 0 | 3/10 matched | `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter` | 2/5 matched (target 4) | `Target`, `Item`, `IntoIter` | 10 | 15 | 101508.7 |
| 141 | `bc.instrs` | `bc.Instrs [ZERO]` | 0.00 | 0 | 19/27 matched (target 29) | `handle`, `drop`, `drop`, `opcodes`, `fmt`, `write`, `default`, `display` | 3/4 matched (target 3) | `HandlerImpl` | 9 | 31 | 93110.0 |
| 142 | `range.range_type` | `range.RangeType [ZERO]` | 0.00 | 0 | 16/25 matched (target 33) | `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `eq` | 1/1 matched (target 3) | _none_ | 9 | 26 | 92610.0 |
| 143 | `values.unpack` | `values.Unpack [ZERO]` | 0.00 | 0 | 11/16 matched (target 14) | `into_error`, `into_infallible`, `error`, `error`, `error` | 3/7 matched | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` | 9 | 23 | 92310.0 |
| 144 | `analysis.types` | `analysis.Types` | 0.10 | 0 | 4/10 matched (target 7) | `fmt`, `fmt`, `new`, `fmt`, `fmt`, `from` | 2/5 matched (target 2) | `LintWarning`, `LintT`, `EvalSeverity` | 9 | 15 | 91509.0 |
| 145 | `tuple.unpack` | `tuple.Unpack [ZERO]` | 0.00 | 0 | 2/7 matched | `default`, `starlark_type_repr`, `into_iter`, `into_iter`, `into_iter` | 1/5 matched (target 2) | `Canonical`, `Error`, `Item`, `IntoIter` | 9 | 12 | 91210.0 |
| 146 | `list.value` | `list.Value [ZERO]` | 0.00 | 0 | 69/75 matched (target 103) | `fmt`, `error`, `starlark_type_repr`, `fmt`, `fmt`, `fmt` | 6/8 matched (target 10) | `List`, `Canonical` | 8 | 83 | 88310.0 |
| 147 | `heap.heap_type` | `heap.HeapType [ZERO]` | 0.00 | 0 | 56/64 matched (target 74) | `fmt`, `fmt`, `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `validate_str_interning`, `append_x` | 8/8 matched (target 10) | _none_ | 8 | 72 | 87210.0 |
| 148 | `set.value` | `set.Value [ZERO]` | 0.00 | 0 | 50/55 matched (target 66) | `content`, `iter_start`, `content_unchecked`, `iter_stop`, `fmt` | 6/9 matched (target 7) | `Canonical`, `Frozen`, `ContentRef` | 8 | 64 | 86410.0 |
| 149 | `layout.complex` | `layout.Complex [ZERO]` | 0.00 | 0 | 10/15 matched (target 18) | `unpack_value_impl`, `fmt`, `fmt`, `test_module`, `test_unpack` | 1/4 matched (target 2) | `Canonical`, `Error`, `Frozen` | 8 | 19 | 81910.0 |
| 150 | `typing.small_arc_vec` | `typing.SmallArcVec` | 0.17 | 0 | 4/11 matched (target 16) | `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter` | 2/3 matched (target 5) | `Target` | 8 | 14 | 81408.3 |
| 151 | `compiler.scope` | `compiler.Scope [ZERO]` | 0.00 | 0 | 49/53 matched (target 70) | `from`, `assign_ident_impl`, `new`, `new` | 17/20 matched (target 28) | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` | 7 | 73 | 77310.0 |
| 152 | `float.float` | `float.Float [STUB]` | 0.00 | 0 | 36/41 matched (target 45) | `fmt`, `non_finite`, `decimal`, `scientific`, `compact` | 1/3 matched (target 2) | `Canonical`, `Error` | 7 | 44 | 74410.0 |
| 153 | `profile.bc` | `profile.Bc` | 0.25 | 0 | 23/29 matched (target 28) | `sum`, `add_assign`, `add_assign`, `add_assign`, `add_assign`, `default` | 9/10 matched (target 14) | `Data` | 7 | 39 | 73907.5 |
| 154 | `adapter.implementation` | `adapter.Implementation [ZERO]` | 0.00 | 0 | 18/25 matched (target 27) | `prepare_dap_adapter`, `fmt`, `new`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints` | 6/6 matched (target 10) | _none_ | 7 | 31 | 73110.0 |
| 155 | `num.value` | `num.Value` | 0.24 | 0 | 16/22 matched (target 32) | `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul` | 3/4 matched (target 7) | `Output` | 7 | 26 | 72607.6 |
| 156 | `string.repr` | `string.Repr [ZERO]` | 0.00 | 0 | 15/22 matched (target 20) | `or4`, `push_vec_tail`, `test_to_repr`, `test`, `string_repr_for_test`, `test_chunk_non_ascii_or_need_escape`, `load` | 1/1 matched (target 2) | _none_ | 7 | 23 | 72310.0 |
| 157 | `dict.refs` | `dict.Refs` | 0.13 | 0 | 6/10 matched (target 13) | `from_value`, `from_value`, `error`, `deref` | 4/7 matched (target 11) | `Target`, `Canonical`, `Error` | 7 | 17 | 71708.7 |
| 158 | `heap.send` | `heap.Send [ZERO]` | 0.00 | 0 | 2/6 matched | `deref`, `deref_mut`, `fmt`, `fmt` | 3/6 matched (target 3) | `Sealed`, `Target`, `StaticType` | 7 | 12 | 71210.0 |
| 159 | `list.unpack` | `list.Unpack [ZERO]` | 0.00 | 0 | 4/7 matched (target 18) | `into_iter`, `into_iter`, `into_iter` | 1/5 matched (target 7) | `Canonical`, `Error`, `Item`, `IntoIter` | 7 | 12 | 71210.0 |
| 160 | `string.simd` | `string.Simd [ZERO]` | 0.00 | 0 | 1/8 matched (target 2) | `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask` | 2/2 matched | _none_ | 7 | 10 | 71010.0 |
| 161 | `assert.assert` | `assert.Assert [STUB]` | 0.00 | 0 | 54/60 matched (target 66) | `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck` | 2/2 matched | _none_ | 6 | 62 | 66210.0 |
| 162 | `analysis.names` | `analysis.Names` | 0.25 | 0 | 31/36 matched (target 42) | `new`, `ident`, `assign_ident`, `about`, `module` | 7/8 matched (target 14) | `AstStrExt` | 6 | 44 | 64407.5 |
| 163 | `values.owned` | `values.Owned [ZERO]` | 0.00 | 0 | 31/35 matched (target 34) | `fmt`, `downcast_starlark`, `deref`, `new` | 3/5 matched | `Canonical`, `Target` | 6 | 40 | 64010.0 |
| 164 | `profile.aggregated` | `profile.Aggregated [ZERO]` | 0.00 | 0 | 22/28 matched (target 40) | `new`, `normalize_for_golden_tests`, `fmt`, `normalize_for_golden_tests`, `total_alloc_count`, `make` | 8/8 matched (target 11) | _none_ | 6 | 36 | 63610.0 |
| 165 | `bc.addr` | `bc.Addr [ZERO]` | 0.00 | 0 | 22/27 matched (target 35) | `add_assign`, `get_instr_mut`, `sub_usize`, `sub`, `add` | 5/6 matched (target 5) | `Output` | 6 | 33 | 63310.0 |
| 166 | `stdlib.partial` | `stdlib.Partial [STUB]` | 0.00 | 0 | 9/13 matched | `partial`, `partial`, `fmt`, `eq` | 3/5 matched (target 4) | `Frozen`, `Canonical` | 6 | 18 | 61810.0 |
| 167 | `environment.modules` | `environment.Modules [ZERO]` | 0.00 | 0 | 47/52 matched (target 48) | `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo` | 4/4 matched (target 6) | _none_ | 5 | 56 | 55610.0 |
| 168 | `tuple.value` | `tuple.Value [ZERO]` | 0.00 | 0 | 27/32 matched (target 40) | `fmt`, `fmt`, `new`, `offset_of_content`, `typechecker_ty` | 3/3 matched (target 4) | _none_ | 5 | 35 | 53510.0 |
| 169 | `typing.callable_param` | `typing.CallableParam` | 0.16 | 0 | 19/23 matched (target 27) | `fmt`, `pf`, `new_named_only`, `fmt` | 5/6 matched (target 11) | `ParamSpecDisplay` | 5 | 29 | 52908.4 |
| 170 | `alloc.allocator` | `alloc.Allocator [ZERO]` | 0.00 | 0 | 14/18 matched (target 19) | `fmt`, `default`, `drop`, `random_iteration` | 2/3 matched | `Item` | 5 | 21 | 52110.0 |
| 171 | `analysis.underscore` | `analysis.Underscore` | 0.13 | 0 | 8/13 matched (target 14) | `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored` | 1/1 matched (target 3) | _none_ | 5 | 14 | 51408.7 |
| 172 | `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 0.00 | 0 | 6/8 matched (target 6) | `next`, `size_hint` | 0/3 matched (target 1) | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` | 5 | 11 | 51110.0 |
| 173 | `params.spec` | `params.Spec [ZERO]` | 0.00 | 0 | 34/38 matched (target 35) | `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl` | 6/6 matched (target 11) | _none_ | 4 | 44 | 44410.0 |
| 174 | `typing.custom` | `typing.Custom [ZERO]` | 0.00 | 0 | 34/38 matched (target 49) | `eq`, `hash`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | 4 | 41 | 44110.0 |
| 175 | `heap.repr` | `heap.Repr [ZERO]` | 0.00 | 0 | 28/32 matched (target 35) | `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut` | 5/5 matched (target 8) | _none_ | 4 | 37 | 43710.0 |
| 176 | `profile.time_flame` | `profile.TimeFlame` | 0.25 | 0 | 17/20 matched (target 21) | `r#gen`, `register_sleep`, `sleep` | 10/11 matched (target 16) | `Data` | 4 | 31 | 43107.5 |
| 177 | `alloc.chain` | `alloc.Chain [ZERO]` | 0.00 | 0 | 21/23 matched (target 26) | `drop`, `drop` | 3/5 matched (target 4) | `Item`, `ResetSplitAtZeroTest` | 4 | 28 | 42810.0 |
| 178 | `avalues.list` | `avalues.List` | 0.11 | 0 | 14/16 matched (target 19) | `alloc_list`, `alloc_list_concat` | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | 4 | 20 | 42008.9 |
| 179 | `tuple.refs` | `tuple.Refs` | 0.21 | 0 | 11/13 matched (target 15) | `unpack_value_impl`, `unpack_value_impl` | 2/4 matched (target 2) | `Canonical`, `Error` | 4 | 17 | 41707.9 |
| 180 | `analysis.dubious` | `analysis.Dubious` | 0.20 | 0 | 9/12 matched (target 22) | `lint`, `module`, `about` | 1/2 matched (target 9) | `Key` | 4 | 14 | 41408.0 |
| 181 | `analysis.performance` | `analysis.Performance` | 0.20 | 0 | 6/10 matched (target 14) | `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function` | 1/1 matched (target 4) | _none_ | 4 | 11 | 41108.0 |
| 182 | `int.i32` | `int.I32` | 0.03 | 0 | 2/4 matched (target 5) | `alloc_value`, `alloc_frozen_value` | 0/2 matched (target 1) | `Canonical`, `Error` | 4 | 6 | 40609.7 |
| 183 | `profile.mode` | `profile.Mode` | 0.05 | 0 | 1/4 matched | `fmt`, `name`, `from_str` | 1/2 matched (target 1) | `Err` | 4 | 6 | 40609.5 |
| 184 | `structs.alloc` | `structs.Alloc [ZERO]` | 0.00 | 0 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | 4 | 5 | 40510.0 |
| 185 | `dict.alloc` | `dict.Alloc [ZERO]` | 0.00 | 0 | 0/3 matched (target 1) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | 4 | 5 | 40510.0 |
| 186 | `enumeration.globals` | `enumeration.Globals` | 0.02 | 0 | 1/5 matched (target 1) | `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr` | 0/0 matched | _none_ | 4 | 5 | 40509.8 |
| 187 | `bc.instr_arg` | `bc.InstrArg [ZERO]` | 0.00 | 0 | 82/84 matched | `fmt`, `fmt` | 3/4 matched (target 42) | `HandlerImpl` | 3 | 88 | 38810.0 |
| 188 | `compiler.def` | `compiler.Def [ZERO]` | 0.00 | 0 | 38/40 matched (target 46) | `fmt`, `new` | 12/13 matched (target 18) | `Frozen` | 3 | 53 | 35310.0 |
| 189 | `analysis.flow` | `analysis.Flow` | 0.29 | 0 | 22/25 matched (target 37) | `lint`, `module`, `about` | 1/1 matched (target 12) | _none_ | 3 | 26 | 32607.1 |
| 190 | `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 0.00 | 0 | 15/17 matched (target 25) | `add`, `add` | 7/8 matched (target 7) | `Output` | 3 | 25 | 32510.0 |
| 191 | `list.refs` | `list.Refs [ZERO]` | 0.00 | 0 | 15/15 matched (target 29) | _none_ | 2/5 matched (target 10) | `Target`, `Canonical`, `Error` | 3 | 20 | 32010.0 |
| 192 | `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 0.00 | 0 | 11/11 matched (target 13) | _none_ | 4/7 matched | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` | 3 | 18 | 31810.0 |
| 193 | `bc.opcode` | `bc.Opcode [ZERO]` | 0.00 | 0 | 7/8 matched (target 11) | `handle` | 3/5 matched (target 4) | `ByNumber`, `FindOpcode` | 3 | 13 | 31310.0 |
| 194 | `symbol.map` | `symbol.Map [ZERO]` | 0.00 | 0 | 9/12 matched (target 11) | `fmt`, `new`, `with_capacity` | 1/1 matched | _none_ | 3 | 13 | 31310.0 |
| 195 | `runtime.inlined_frame` | `runtime.InlinedFrame` | 0.31 | 0 | 7/10 matched (target 9) | `eq`, `make_span`, `assert_stack` | 3/3 matched (target 4) | _none_ | 3 | 13 | 31306.9 |
| 196 | `eval.bc.compiler.stmt` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` | 0.00 | 0 | 8/11 matched | `write_bc`, `write_if_then`, `write_if_else` | 0/0 matched | _none_ | 3 | 11 | 31110.0 |
| 197 | `enumeration.value` | `enumeration.Value` | 0.12 | 0 | 6/9 matched (target 11) | `fmt`, `index`, `value` | 1/1 matched (target 8) | _none_ | 3 | 10 | 31008.8 |
| 198 | `bc.repr` | `bc.Repr [ZERO]` | 0.00 | 0 | 4/6 matched (target 5) | `size_of_repr`, `handle` | 2/3 matched (target 2) | `HandlerImpl` | 3 | 9 | 30910.0 |
| 199 | `debug.inspect` | `debug.Inspect [ZERO]` | 0.00 | 0 | 6/9 matched (target 7) | `debugger`, `debug_inspect_stack`, `debug_inspect_variables` | 0/0 matched (target 1) | _none_ | 3 | 9 | 30910.0 |
| 200 | `eval.bc.compiler.call` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` | 0.00 | 0 | 4/7 matched (target 8) | `mark_definitely_assigned_after`, `mark_definitely_assigned_after`, `write_bc` | 0/0 matched (target 3) | _none_ | 3 | 7 | 30710.0 |
| 201 | `dict.unpack` | `dict.Unpack` | 0.10 | 0 | 2/3 matched | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 6 | 30609.0 |
| 202 | `compiler.expr` | `compiler.Expr [ZERO]` | 0.00 | 0 | 60/60 matched (target 63) | _none_ | 9/11 matched (target 56) | `AstLiteralCompile`, `CompilerExprUtil` | 2 | 71 | 27110.0 |
| 203 | `values.traits` | `values.Traits [ZERO]` | 0.00 | 0 | 55/56 matched (target 55) | `please_use_starlark_type_macro` | 2/3 matched (target 2) | `Canonical` | 2 | 59 | 25910.0 |
| 204 | `profile.heap` | `profile.Heap` | 0.38 | 0 | 27/28 matched | `r#gen` | 10/11 matched (target 12) | `Data` | 2 | 39 | 23906.2 |
| 205 | `types.function` | `types.Function [ZERO]` | 0.00 | 0 | 20/21 matched (target 28) | `new` | 11/12 matched (target 14) | `Canonical` | 2 | 33 | 23310.0 |
| 206 | `profile.stmt` | `profile.Stmt [ZERO]` | 0.00 | 0 | 22/23 matched (target 25) | `r#gen` | 8/9 matched (target 10) | `Data` | 2 | 32 | 23210.0 |
| 207 | `structs.value` | `structs.Value [STUB]` | 0.00 | 0 | 19/21 matched (target 35) | `iter_frozen`, `fmt` | 1/1 matched (target 6) | _none_ | 2 | 22 | 22210.0 |
| 208 | `avalues.tuple` | `avalues.Tuple [ZERO]` | 0.00 | 0 | 14/14 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | 2 | 18 | 21810.0 |
| 209 | `avalues.array` | `avalues.Array [ZERO]` | 0.00 | 0 | 13/13 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | 2 | 17 | 21710.0 |
| 210 | `namespace.value` | `namespace.Value` | 0.41 | 0 | 13/15 matched (target 27) | `new`, `fmt` | 2/2 matched (target 7) | _none_ | 2 | 17 | 21705.9 |
| 211 | `avalues.complex` | `avalues.Complex [ZERO]` | 0.00 | 0 | 10/10 matched (target 17) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | 2 | 15 | 21510.0 |
| 212 | `analysis.incompatible` | `analysis.Incompatible` | 0.29 | 0 | 12/14 matched (target 20) | `lint`, `module` | 1/1 matched (target 4) | _none_ | 2 | 15 | 21507.1 |
| 213 | `avalues.static_` | `avalues.Static [ZERO]` | 0.00 | 0 | 9/9 matched (target 11) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | 2 | 14 | 21410.0 |
| 214 | `record.instance` | `record.Instance [ZERO]` | 0.00 | 0 | 11/13 matched (target 17) | `fmt`, `get_attr_hashed` | 1/1 matched (target 3) | _none_ | 2 | 14 | 21410.0 |
| 215 | `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 0.00 | 0 | 9/10 matched | `drop_non_drop` | 2/3 matched (target 2) | `RowKind` | 2 | 13 | 21310.0 |
| 216 | `profile.typecheck` | `profile.Typecheck [ZERO]` | 0.00 | 0 | 7/8 matched | `r#gen` | 4/5 matched (target 6) | `Data` | 2 | 13 | 21310.0 |
| 217 | `compiler.args` | `compiler.Args` | 0.16 | 0 | 10/11 matched | `args` | 1/2 matched (target 1) | `Never` | 2 | 13 | 21308.4 |
| 218 | `values.type_repr` | `values.TypeRepr [ZERO]` | 0.00 | 0 | 8/9 matched (target 8) | `starlark_type_repr` | 2/3 matched (target 8) | `Canonical` | 2 | 12 | 21210.0 |
| 219 | `bc.call` | `bc.Call [ZERO]` | 0.00 | 0 | 6/7 matched (target 15) | `fmt` | 4/5 matched (target 8) | `Args` | 2 | 12 | 21210.0 |
| 220 | `structs.refs` | `structs.Refs [ZERO]` | 0.00 | 0 | 7/7 matched (target 8) | _none_ | 2/4 matched | `Canonical`, `Error` | 2 | 11 | 21110.0 |
| 221 | `params.parser` | `params.Parser [ZERO]` | 0.00 | 0 | 8/10 matched | `test`, `test` | 1/1 matched (target 2) | _none_ | 2 | 11 | 21110.0 |
| 222 | `typing.iter` | `typing.Iter [ZERO]` | 0.00 | 0 | 6/6 matched (target 8) | _none_ | 2/4 matched (target 3) | `NonInstantiable`, `Canonical` | 2 | 10 | 21010.0 |
| 223 | `set.refs` | `set.Refs [ZERO]` | 0.00 | 0 | 5/5 matched (target 19) | _none_ | 3/5 matched (target 13) | `Canonical`, `Error` | 2 | 10 | 21010.0 |
| 224 | `profile.data` | `profile.Data` | 0.15 | 0 | 5/7 matched (target 5) | `_assert_profile_data_send_sync`, `_assert_send_sync` | 3/3 matched (target 18) | _none_ | 2 | 10 | 21008.5 |
| 225 | `symbol.symbol` | `symbol.Symbol` | 0.18 | 0 | 7/9 matched (target 11) | `fmt`, `eq` | 1/1 matched | _none_ | 2 | 10 | 21008.2 |
| 226 | `typing.callable` | `typing.Callable` | 0.19 | 0 | 6/7 matched (target 10) | `fmt` | 1/2 matched (target 1) | `TyCallableInner` | 2 | 9 | 20908.1 |
| 227 | `bc.bytecode` | `bc.Bytecode` | 0.24 | 0 | 6/7 matched (target 11) | `handle` | 1/2 matched (target 1) | `HandlerImpl` | 2 | 9 | 20907.6 |
| 228 | `values.recursive_repr_or_json_guard` | `values.RecursiveReprOrJsonGuard [ZERO]` | 0.00 | 0 | 2/4 matched (target 5) | `drop`, `drop` | 4/4 matched | _none_ | 2 | 8 | 20810.0 |
| 229 | `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 0.00 | 0 | 0/1 matched (target 4) | `drop` | 5/6 matched (target 5) | `Canonical` | 2 | 7 | 20710.0 |
| 230 | `types.any` | `types.Any` | 0.40 | 0 | 4/5 matched | `fmt` | 1/2 matched (target 1) | `Canonical` | 2 | 7 | 20706.0 |
| 231 | `list.globals` | `list.Globals [ZERO]` | 0.00 | 0 | 4/5 matched | `list` | 0/1 matched | `ListType` | 2 | 6 | 20610.0 |
| 232 | `float.unpack` | `float.Unpack` | 0.22 | 0 | 3/3 matched (target 4) | _none_ | 1/3 matched (target 2) | `Canonical`, `Error` | 2 | 6 | 20607.8 |
| 233 | `funcs.min_max` | `funcs.MinMax [ZERO]` | 0.00 | 0 | 3/5 matched (target 3) | `max`, `min` | 0/0 matched | _none_ | 2 | 5 | 20510.0 |
| 234 | `bc.definitely_assigned` | `bc.DefinitelyAssigned` | 0.14 | 0 | 2/4 matched (target 7) | `new`, `assert_smaller_then` | 1/1 matched | _none_ | 2 | 5 | 20508.6 |
| 235 | `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 0.00 | 0 | 2/3 matched (target 2) | `drop` | 0/1 matched (target 0) | `Guard` | 2 | 4 | 20410.0 |
| 236 | `debug.evaluate` | `debug.Evaluate [ZERO]` | 0.00 | 0 | 2/4 matched (target 3) | `debugger`, `debug_evaluate` | 0/0 matched (target 1) | _none_ | 2 | 4 | 20410.0 |
| 237 | `type_compiled.globals` | `type_compiled.Globals [ZERO]` | 0.00 | 0 | 2/4 matched (target 2) | `eval_type`, `isinstance` | 0/0 matched (target 1) | _none_ | 2 | 4 | 20410.0 |
| 238 | `enumeration.ty_enum_type` | `enumeration.TyEnumType` | 0.00 | 0 | 0/2 matched (target 3) | `eq`, `hash` | 1/1 matched | _none_ | 2 | 3 | 20310.0 |
| 239 | `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 0.04 | 0 | 1/2 matched (target 1) | `drop` | 0/1 matched (target 0) | `WriteRemOnDrop` | 2 | 3 | 20309.6 |
| 240 | `bool.type_repr` | `bool.TypeRepr [ZERO]` | 0.00 | 0 | 0/1 matched | `starlark_type_repr` | 0/1 matched (target 0) | `Canonical` | 2 | 2 | 20210.0 |
| 241 | `bool.unpack` | `bool.Unpack` | 0.00 | 0 | 0/1 matched | `unpack_value_impl` | 0/1 matched (target 0) | `Error` | 2 | 2 | 20210.0 |
| 242 | `debug.adapter` | `debug.Adapter [ZERO]` | 0.00 | 0 | 22/23 matched | `fmt` | 14/14 matched (target 29) | _none_ | 1 | 37 | 13710.0 |
| 243 | `docs` | `docs.Docs [ZERO]` | 0.00 | 0 | 16/17 matched (target 16) | `default` | 10/10 matched (target 15) | _none_ | 1 | 27 | 12710.0 |
| 244 | `typing.basic` | `typing.Basic` | 0.40 | 0 | 18/19 matched (target 20) | `fmt` | 1/1 matched (target 11) | _none_ | 1 | 20 | 12006.0 |
| 245 | `funcs.other` | `funcs.Other [STUB]` | 0.00 | 0 | 18/19 matched | `r#type` | 0/0 matched (target 2) | _none_ | 1 | 19 | 11910.0 |
| 246 | `docs.parse` | `docs.Parse [ZERO]` | 0.00 | 0 | 15/16 matched (target 18) | `arg` | 1/1 matched (target 2) | _none_ | 1 | 17 | 11710.0 |
| 247 | `profile.flamegraph` | `profile.Flamegraph` | 0.31 | 0 | 12/13 matched (target 16) | `new` | 3/3 matched (target 4) | _none_ | 1 | 16 | 11606.9 |
| 248 | `compiler.def_inline` | `compiler.DefInline` | 0.23 | 0 | 9/10 matched (target 9) | `new` | 4/4 matched (target 6) | _none_ | 1 | 14 | 11407.7 |
| 249 | `analysis` | `starlark.Analysis [STUB]` | 0.00 | 0 | 11/12 matched | `module` | 1/1 matched (target 2) | _none_ | 1 | 13 | 11310.0 |
| 250 | `list.methods` | `list.Methods [ZERO]` | 0.00 | 0 | 10/11 matched (target 19) | `list_methods` | 0/0 matched (target 2) | _none_ | 1 | 11 | 11110.0 |
| 251 | `type_compiled.factory` | `type_compiled.Factory` | 0.58 | 0 | 9/9 matched | _none_ | 1/2 matched (target 1) | `Result` | 1 | 11 | 11104.2 |
| 252 | `bool.value` | `bool.Value` | 0.20 | 0 | 8/9 matched | `fmt` | 1/1 matched | _none_ | 1 | 10 | 11008.0 |
| 253 | `namespace.typing` | `namespace.Typing` | 0.25 | 0 | 6/7 matched (target 8) | `fmt` | 3/3 matched | _none_ | 1 | 10 | 11007.5 |
| 254 | `typing.never` | `typing.Never [ZERO]` | 0.00 | 0 | 6/6 matched (target 9) | _none_ | 2/3 matched | `Canonical` | 1 | 9 | 10910.0 |
| 255 | `profile.by_type` | `profile.ByType [ZERO]` | 0.00 | 0 | 5/6 matched (target 7) | `normalize_for_golden_tests` | 1/1 matched | _none_ | 1 | 7 | 10710.0 |
| 256 | `tuple.alloc` | `tuple.Alloc` | 0.30 | 0 | 5/5 matched | _none_ | 1/2 matched | `Canonical` | 1 | 7 | 10707.0 |
| 257 | `compiler.if_compiler` | `compiler.IfCompiler [ZERO]` | 0.00 | 0 | 5/6 matched (target 5) | `wr` | 0/0 matched | _none_ | 1 | 6 | 10610.0 |
| 258 | `set.set` | `set.Set [ZERO]` | 0.00 | 0 | 4/5 matched (target 4) | `set` | 0/0 matched (target 1) | _none_ | 1 | 5 | 10510.0 |
| 259 | `structs.structs` | `structs.Structs [ZERO]` | 0.00 | 0 | 3/4 matched (target 3) | `r#struct` | 1/1 matched | _none_ | 1 | 5 | 10510.0 |
| 260 | `types.unbound` | `types.Unbound` | 0.17 | 0 | 3/4 matched | `fmt` | 1/1 matched (target 3) | _none_ | 1 | 5 | 10508.3 |
| 261 | `stdlib.internal` | `stdlib.Internal` | 0.37 | 0 | 3/4 matched (target 3) | `ty_of_value_debug` | 0/0 matched (target 1) | _none_ | 1 | 4 | 10406.3 |
| 262 | `compiler.assign_modify` | `compiler.AssignModify` | 0.53 | 0 | 3/3 matched | _none_ | 0/1 matched (target 0) | `AssignOnWriteBc` | 1 | 4 | 10404.7 |
| 263 | `dict.globals` | `dict.Globals [ZERO]` | 0.00 | 0 | 2/3 matched (target 4) | `dict` | 0/0 matched | _none_ | 1 | 3 | 10310.0 |
| 264 | `float.globals` | `float.Globals [ZERO]` | 0.00 | 0 | 1/2 matched (target 1) | `float` | 0/0 matched (target 4) | _none_ | 1 | 2 | 10210.0 |
| 265 | `pagable.error` | `pagable.Error` | 0.00 | 0 | 0/1 matched | `from` | 1/1 matched (target 2) | _none_ | 1 | 2 | 10210.0 |
| 266 | `int.globals` | `int.Globals [ZERO]` | 0.00 | 0 | 1/2 matched | `int` | 0/0 matched | _none_ | 1 | 2 | 10210.0 |
| 267 | `bool.globals` | `bool.Globals` | 0.03 | 0 | 1/2 matched (target 1) | `bool` | 0/0 matched | _none_ | 1 | 2 | 10209.7 |
| 268 | `namespace.globals` | `namespace.Globals` | 0.05 | 0 | 1/2 matched (target 1) | `namespace` | 0/0 matched | _none_ | 1 | 2 | 10209.5 |
| 269 | `num.globals` | `num.Globals` | 0.05 | 0 | 1/2 matched (target 1) | `abs` | 0/0 matched | _none_ | 1 | 2 | 10209.5 |
| 270 | `range.globals` | `range.Globals` | 0.05 | 0 | 1/2 matched (target 1) | `range` | 0/0 matched | _none_ | 1 | 2 | 10209.5 |
| 271 | `tuple.globals` | `tuple.Globals` | 0.06 | 0 | 1/2 matched (target 1) | `tuple` | 0/0 matched | _none_ | 1 | 2 | 10209.4 |
| 272 | `type_compiled.matchers` | `type_compiled.Matchers [ZERO]` | 0.00 | 0 | 25/25 matched | _none_ | 23/23 matched | _none_ | 0 | 48 | 4810.0 |
| 273 | `bc.writer` | `bc.Writer [ZERO]` | 0.00 | 0 | 43/43 matched (target 44) | _none_ | 4/4 matched | _none_ | 0 | 47 | 4710.0 |
| 274 | `typing.fill_types_for_lint` | `typing.FillTypesForLint [ZERO]` | 0.00 | 0 | 39/39 matched (target 40) | _none_ | 3/3 matched | _none_ | 0 | 42 | 4210.0 |
| 275 | `string.methods` | `string.Methods [ZERO]` | 0.00 | 0 | 41/41 matched (target 68) | _none_ | 1/1 matched (target 5) | _none_ | 0 | 42 | 4210.0 |
| 276 | `oracle.ctx` | `oracle.Ctx` | 0.30 | 0 | 32/32 matched | _none_ | 2/2 matched (target 14) | _none_ | 0 | 34 | 3407.0 |
| 277 | `type_compiled.alloc` | `type_compiled.Alloc` | 0.38 | 0 | 28/28 matched (target 37) | _none_ | 1/1 matched (target 3) | _none_ | 0 | 29 | 2906.2 |
| 278 | `typing.error` | `typing.Error [ZERO]` | 0.00 | 0 | 16/16 matched (target 25) | _none_ | 5/5 matched (target 10) | _none_ | 0 | 21 | 2110.0 |
| 279 | `docs.markdown` | `docs.Markdown [ZERO]` | 0.00 | 0 | 19/19 matched | _none_ | 2/2 matched | _none_ | 0 | 21 | 2110.0 |
| 280 | `typing.ctx` | `typing.Ctx` | 0.25 | 0 | 19/19 matched (target 20) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 20 | 2007.5 |
| 281 | `dict.methods` | `dict.Methods [ZERO]` | 0.00 | 0 | 17/17 matched (target 19) | _none_ | 0/0 matched (target 1) | _none_ | 0 | 17 | 1710.0 |
| 282 | `environment.names` | `environment.Names [ZERO]` | 0.00 | 0 | 14/14 matched | _none_ | 2/2 matched | _none_ | 0 | 16 | 1610.0 |
| 283 | `eval.bc.compiler.expr` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` | 0.00 | 0 | 15/15 matched (target 16) | _none_ | 0/0 matched | _none_ | 0 | 15 | 1510.0 |
| 284 | `docs.code` | `docs.Code [ZERO]` | 0.00 | 0 | 14/14 matched | _none_ | 0/0 matched | _none_ | 0 | 14 | 1410.0 |
| 285 | `compiler.compr` | `compiler.Compr` | 0.23 | 0 | 11/11 matched (target 12) | _none_ | 3/3 matched (target 5) | _none_ | 0 | 14 | 1407.7 |
| 286 | `compiler.call` | `compiler.Call` | 0.26 | 0 | 13/13 matched (target 14) | _none_ | 1/1 matched | _none_ | 0 | 14 | 1407.4 |
| 287 | `environment.slots` | `environment.Slots` | 0.21 | 0 | 10/10 matched | _none_ | 3/3 matched | _none_ | 0 | 13 | 1307.9 |
| 288 | `docs.multipage` | `docs.Multipage` | 0.34 | 0 | 6/6 matched | _none_ | 5/5 matched (target 7) | _none_ | 0 | 11 | 1106.6 |
| 289 | `compiler.types` | `compiler.Types [ZERO]` | 0.00 | 0 | 8/8 matched | _none_ | 1/1 matched (target 7) | _none_ | 0 | 9 | 910.0 |
| 290 | `record.ty_record_type` | `record.TyRecordType [ZERO]` | 0.00 | 0 | 7/7 matched | _none_ | 1/1 matched (target 2) | _none_ | 0 | 8 | 810.0 |
| 291 | `callable.param` | `callable.Param` | 0.33 | 0 | 6/6 matched | _none_ | 2/2 matched (target 7) | _none_ | 0 | 8 | 806.7 |
| 292 | `alloc.per_thread` | `alloc.PerThread [ZERO]` | 0.00 | 0 | 6/6 matched (target 9) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 7 | 710.0 |
| 293 | `compiler.module` | `compiler.Module` | 0.26 | 0 | 6/6 matched | _none_ | 1/1 matched (target 4) | _none_ | 0 | 7 | 707.4 |
| 294 | `layout.value_not_special` | `layout.ValueNotSpecial` | 0.28 | 0 | 6/6 matched (target 7) | _none_ | 1/1 matched | _none_ | 0 | 7 | 707.2 |
| 295 | `types.known_methods` | `types.KnownMethods` | 0.28 | 0 | 5/5 matched | _none_ | 2/2 matched | _none_ | 0 | 7 | 707.2 |
| 296 | `unused_loads.find` | `unusedloads.Find` | 0.32 | 0 | 4/4 matched (target 8) | _none_ | 3/3 matched | _none_ | 0 | 7 | 706.8 |
| 297 | `runtime.before_stmt` | `runtime.BeforeStmt` | 0.38 | 0 | 4/4 matched | _none_ | 3/3 matched (target 5) | _none_ | 0 | 7 | 706.2 |
| 298 | `intern.interner` | `intern.Interner [ZERO]` | 0.00 | 0 | 4/4 matched (target 7) | _none_ | 2/2 matched (target 3) | _none_ | 0 | 6 | 610.0 |
| 299 | `values.typing.ty` | `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` | 0.00 | 0 | 5/5 matched (target 7) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 6 | 610.0 |
| 300 | `values.index` | `values.Index [ZERO]` | 0.00 | 0 | 6/6 matched (target 7) | _none_ | 0/0 matched (target 1) | _none_ | 0 | 6 | 610.0 |
| 301 | `layout.static_string` | `layout.StaticString [ZERO]` | 0.00 | 0 | 5/5 matched | _none_ | 1/1 matched (target 2) | _none_ | 0 | 6 | 610.0 |
| 302 | `runtime.slots` | `runtime.Slots` | 0.20 | 0 | 3/3 matched | _none_ | 3/3 matched | _none_ | 0 | 6 | 608.0 |
| 303 | `assert.conformance` | `assert.Conformance` | 0.21 | 0 | 5/5 matched | _none_ | 1/1 matched | _none_ | 0 | 6 | 607.9 |
| 304 | `compiler.expr_bool` | `compiler.ExprBool` | 0.29 | 0 | 5/5 matched | _none_ | 1/1 matched (target 3) | _none_ | 0 | 6 | 607.1 |
| 305 | `string.globals` | `string.Globals [ZERO]` | 0.00 | 0 | 5/5 matched | _none_ | 0/0 matched | _none_ | 0 | 5 | 510.0 |
| 306 | `unused_loads.remove` | `unusedloads.Remove [ZERO]` | 0.00 | 0 | 4/4 matched | _none_ | 1/1 matched | _none_ | 0 | 5 | 510.0 |
| 307 | `typing.any` | `typing.Any [ZERO]` | 0.00 | 0 | 4/4 matched (target 6) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 5 | 510.0 |
| 308 | `values.comparison` | `values.Comparison` | 0.29 | 0 | 5/5 matched | _none_ | 0/0 matched | _none_ | 0 | 5 | 507.1 |
| 309 | `funcs.zip` | `funcs.Zip` | 0.43 | 0 | 4/4 matched (target 7) | _none_ | 1/1 matched | _none_ | 0 | 5 | 505.7 |
| 310 | `num.typecheck` | `num.Typecheck` | 0.54 | 0 | 2/2 matched | _none_ | 3/3 matched (target 5) | _none_ | 0 | 5 | 504.6 |
| 311 | `string.iter` | `string.Iter [ZERO]` | 0.00 | 0 | 3/3 matched (target 6) | _none_ | 1/1 matched | _none_ | 0 | 4 | 410.0 |
| 312 | `analysis.find_call_name` | `analysis.FindCallName` | 0.20 | 0 | 3/3 matched (target 9) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 4 | 408.0 |
| 313 | `oracle.traits` | `oracle.Traits` | 0.08 | 0 | 1/1 matched (target 3) | _none_ | 2/2 matched | _none_ | 0 | 3 | 309.2 |
| 314 | `eval.soft_error` | `eval.SoftError` | 0.37 | 0 | 1/1 matched | _none_ | 2/2 matched | _none_ | 0 | 3 | 306.3 |
| 315 | `compiler.type_expr` | `compiler.TypeExpr` | 0.41 | 0 | 2/2 matched | _none_ | 1/1 matched | _none_ | 0 | 3 | 305.9 |
| 316 | `compiler.error` | `compiler.Error` | 0.42 | 0 | 2/2 matched | _none_ | 1/1 matched | _none_ | 0 | 3 | 305.8 |
| 317 | `eval.bc.compiler.compr` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr` | 0.51 | 0 | 3/3 matched | _none_ | 0/0 matched | _none_ | 0 | 3 | 304.9 |
| 318 | `eval.bc.compiler.def` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` | 0.00 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 210.0 |
| 319 | `compiler.assign` | `compiler.Assign [ZERO]` | 0.00 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 210.0 |
| 320 | `environment.module_dump` | `environment.ModuleDump` | 0.09 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 209.1 |
| 321 | `eval` | `eval.Eval` | 0.24 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 207.6 |
| 322 | `enumeration.matcher` | `enumeration.Matcher` | 0.26 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 207.4 |
| 323 | `typing.macro_refs` | `typing.MacroRefs` | 0.30 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 207.0 |
| 324 | `record.matcher` | `record.Matcher` | 0.35 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 206.5 |
| 325 | `layout.identity` | `layout.Identity` | 0.36 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 206.4 |
| 326 | `typing.macro_support` | `typing.MacroSupport` | 0.41 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 205.9 |
| 327 | `bool.alloc` | `bool.Alloc` | 0.41 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 205.9 |
| 328 | `allocator.api` | `allocator.Api` | 1.00 | 0 | 0/0 matched | _none_ | 2/2 matched | _none_ | 0 | 2 | 200.0 |
| 329 | `bc.instr` | `bc.Instr` | 1.00 | 0 | 0/0 matched | _none_ | 2/2 matched (target 5) | _none_ | 0 | 2 | 200.0 |
| 330 | `bc.slow_arg` | `bc.SlowArg` | 1.00 | 0 | 0/0 matched | _none_ | 2/2 matched | _none_ | 0 | 2 | 200.0 |
| 331 | `environment` | `starlark.Environment [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 1/1 matched (target 5) | _none_ | 0 | 1 | 110.0 |
| 332 | `bc.for_loop` | `bc.ForLoop [ZERO]` | 0.00 | 0 | 0/0 matched (target 2) | _none_ | 1/1 matched | _none_ | 0 | 1 | 110.0 |
| 333 | `wasm` | `starlark.Wasm` | 0.00 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 110.0 |
| 334 | `compiler.known` | `compiler.Known` | 0.24 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 107.6 |
| 335 | `none.globals` | `none.Globals` | 0.32 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 106.8 |
| 336 | `eval.params` | `eval.Params` | 0.36 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 106.4 |
| 337 | `typing.globals` | `typing.Globals` | 0.50 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 105.0 |
| 338 | `funcs.globals` | `funcs.Globals` | 1.00 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 100.0 |
| 339 | `typing.call_args` | `typing.CallArgs` | 1.00 | 0 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 100.0 |
| 340 | `profile.or_instrumentation` | `profile.OrInstrumentation` | 1.00 | 0 | 0/0 matched | _none_ | 1/1 matched (target 4) | _none_ | 0 | 1 | 100.0 |
| 341 | `typing.mode` | `typing.Mode` | 1.00 | 0 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 100.0 |
| 342 | `values.typing` | `values.Typing [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 343 | `typing.oracle` | `typing.Oracle [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 344 | `macros` | `starlark.Macros [ZERO]` | 0.00 | 0 | 0/0 matched (target 9) | _none_ | 0/0 matched (target 9) | _none_ | 0 | 0 | 10.0 |
| 345 | `errors` | `starlark.Errors [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 346 | `stdlib.funcs` | `stdlib.Funcs [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 347 | `util` | `starlark.Util [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 348 | `pagable.vtable_register` | `pagable.VtableRegister [ZERO]` | 0.00 | 0 | 0/0 matched (target 3) | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 349 | `eval.runtime` | `eval.Runtime [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 350 | `syntax` | `starlark.Syntax [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 351 | `pagable` | `starlark.Pagable [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 352 | `values.types` | `values.Types [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 353 | `layout.avalues` | `layout.AValues [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 354 | `values` | `values.Values` | 1.00 | 0 | 0/0 matched | _none_ | 0/0 matched (target 10) | _none_ | 0 | 0 | 0.0 |

## Cheat Detection / Scoring Failures

- `typing.starlark_value` -> `typing.StarlarkValue [ZERO]`: function-by-function score forced to 0. StarlarkValue.kt: snake_case identifier `starlark_type_id` in Kotlin comments
- `runtime.evaluator` -> `runtime.Evaluator [ZERO]`: function-by-function score forced to 0. Evaluator.kt: snake_case identifier `before_stmt` in Kotlin comments; Evaluator.kt: Rust lifetime explanation in Kotlin comments
- `values.trace` -> `values.Trace [ZERO]`: function-by-function score forced to 0. Trace.kt: Rust lifetime explanation in Kotlin comments; Trace.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.freeze` -> `values.Freeze [ZERO]`: function-by-function score forced to 0. Freeze.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.alloc_value` -> `values.AllocValue [ZERO]`: function-by-function score forced to 0. AllocValue.kt: snake_case identifier `alloc_simple` in Kotlin comments; AllocValue.kt: Rust `fn` declaration in Kotlin comments; AllocValue.kt: Rust attribute syntax in Kotlin comments; AllocValue.kt: Rust `use` path in Kotlin comments; AllocValue.kt: Rust lifetime explanation in Kotlin comments
- `layout.freezer` -> `layout.Freezer [ZERO]`: function-by-function score forced to 0. Freezer.kt: snake_case identifier `debug_assert` in Kotlin comments; Freezer.kt: Rust macro invocation in Kotlin comments
- `values.frozen_ref` -> `values.FrozenRef [ZERO]`: function-by-function score forced to 0. FrozenRef.kt: Rust `fn` declaration in Kotlin comments; FrozenRef.kt: score-padding suppression annotation `@Suppress` in Kotlin code; FrozenRef.kt: Rust lifetime explanation in Kotlin comments
- `none.none_type` -> `none.NoneType [ZERO]`: function-by-function score forced to 0. NoneType.kt: snake_case identifier `HAS_eval_type` in Kotlin code; NoneType.kt: snake_case identifier `serialize_none` in Kotlin comments
- `runtime.arguments` -> `runtime.Arguments [ZERO]`: function-by-function score forced to 0. Arguments.kt: snake_case identifier `to_string` in Kotlin comments; Arguments.kt: Rust macro invocation in Kotlin comments; Arguments.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arguments.kt: Rust lifetime explanation in Kotlin comments
- `typing.type_compiled` -> `type_compiled.TypeCompiled [ZERO]`: function-by-function score forced to 0. TypeCompiled.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.value_of_unchecked` -> `values.ValueOfUnchecked [ZERO]`: function-by-function score forced to 0. ValueOfUnchecked.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; ValueOfUnchecked.kt: Rust `fn` declaration in Kotlin comments; ValueOfUnchecked.kt: score-padding suppression annotation `@Suppress` in Kotlin code; ValueOfUnchecked.kt: Rust lifetime explanation in Kotlin comments; ValueOfUncheckedTest.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `environment.globals` -> `environment.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; GlobalsTest.kt: snake_case identifier `test_send_sync` in Kotlin comments; GlobalsTest.kt: Rust `fn` declaration in Kotlin comments; GlobalsTest.kt: Rust auto-trait terminology in Kotlin comments
- `environment.methods` -> `environment.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.iter` -> `values.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `iter_stop` in Kotlin comments; Iter.kt: Rust-only type/unsafe terminology in Kotlin comments
- `private` -> `starlark.Private [ZERO]`: function-by-function score forced to 0. Private.kt: snake_case identifier `missing_docs` in Kotlin comments
- `collections.symbol` -> `collections.Symbol [ZERO]`: function-by-function score forced to 0. Symbol.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.avalue` -> `layout.AValue [ZERO]`: function-by-function score forced to 0. AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments; AValueTest.kt: snake_case identifier `try_freeze_directly` in Kotlin comments
- `typing.tuple` -> `typing.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `type_compiled_factory` in Kotlin comments
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]`: function-by-function score forced to 0. ConstFrozenString.kt: snake_case identifier `constant_string` in Kotlin comments; ConstFrozenStringTest.kt: snake_case identifier `const_frozen_string` in Kotlin comments; ConstFrozenStringTest.kt: Rust macro invocation in Kotlin comments
- `int.inline_int` -> `int.InlineInt [ZERO]`: function-by-function score forced to 0. InlineInt.kt: snake_case identifier `derive_more` in Kotlin comments; InlineInt.kt: Rust `fn` declaration in Kotlin comments; InlineInt.kt: Rust `pub` item in Kotlin comments; InlineInt.kt: Rust attribute syntax in Kotlin comments; InlineInt.kt: score-padding suppression annotation `@Suppress` in Kotlin code; InlineInt.kt: Rust lifetime explanation in Kotlin comments
- `int.pointer_i32` -> `int.PointerI32 [ZERO]`: function-by-function score forced to 0. PointerI32.kt: snake_case identifier `HAS_equals` in Kotlin code; PointerI32.kt: snake_case identifier `from_raw_pointer_unchecked` in Kotlin comments; PointerI32.kt: Rust `fn` declaration in Kotlin comments; PointerI32.kt: Rust `pub` item in Kotlin comments; PointerI32.kt: score-padding suppression annotation `@Suppress` in Kotlin code; PointerI32.kt: Rust lifetime explanation in Kotlin comments
- `layout.aligned_size` -> `layout.AlignedSize [ZERO]`: function-by-function score forced to 0. AlignedSize.kt: snake_case identifier `size_of` in Kotlin comments; AlignedSize.kt: Rust macro invocation in Kotlin comments; AlignedSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `eval.compiler` -> `eval.Compiler [ZERO]`: function-by-function score forced to 0. Compiler.kt: snake_case identifier `def_inline` in Kotlin comments
- `cast` -> `starlark.Cast [ZERO]`: function-by-function score forced to 0. Cast.kt: Rust lifetime explanation in Kotlin comments; Cast.kt: Rust-only type/unsafe terminology in Kotlin comments
- `types.bigint` -> `types.Bigint [ZERO]`: function-by-function score forced to 0. Bigint.kt: snake_case identifier `HAS_equals` in Kotlin code; Bigint.kt: snake_case identifier `non_zero_int` in Kotlin comments; Bigint.kt: Rust attribute syntax in Kotlin comments; Bigint.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Bigint.kt: Rust lifetime explanation in Kotlin comments
- `runtime.frozen_file_span` -> `runtime.FrozenFileSpan [ZERO]`: function-by-function score forced to 0. FrozenFileSpan.kt: snake_case identifier `empty_static` in Kotlin comments
- `runtime.small_duration` -> `runtime.SmallDuration [ZERO]`: function-by-function score forced to 0. SmallDuration.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.freeze_error` -> `values.FreezeError [ZERO]`: function-by-function score forced to 0. FreezeError.kt: snake_case identifier `starlark_syntax` in Kotlin comments
- `typing.typecheck` -> `typing.Typecheck [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies
- `dict.dict_type` -> `dict.DictType [ZERO]`: function-by-function score forced to 0. DictType.kt: snake_case identifier `starlark_type_repr` in Kotlin comments
- `layout.value_alloc_size` -> `layout.ValueAllocSize [ZERO]`: function-by-function score forced to 0. ValueAllocSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `compiler.stmt` -> `compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `build_file` in Kotlin comments
- `values.layout` -> `values.Layout [ZERO]`: function-by-function score forced to 0. Layout.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `types.array` -> `types.Array [ZERO]`: function-by-function score forced to 0. Array.kt: snake_case identifier `iter_count` in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `eval.bc` -> `bc.Bc [ZERO]`: function-by-function score forced to 0. Bc.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]`: function-by-function score forced to 0. EnumType.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `bc.frame` -> `bc.Frame [ZERO]`: function-by-function score forced to 0. Frame.kt: snake_case identifier `loop_indices` in Kotlin comments
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]`: function-by-function score forced to 0. StarlarkValueAsType.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `values.value_of` -> `values.ValueOf [ZERO]`: function-by-function score forced to 0. ValueOf.kt: snake_case identifier `starlark_module` in Kotlin comments
- `record.record_type` -> `record.RecordType [ZERO]`: function-by-function score forced to 0. RecordType.kt: snake_case identifier `HAS_invoke` in Kotlin code; RecordType.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `alloc.chunk` -> `alloc.Chunk [ZERO]`: function-by-function score forced to 0. Chunk.kt: snake_case identifier `ref_count` in Kotlin comments; Chunk.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Chunk.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.alloc` -> `list.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; no target functions found; report scoring is function-by-function only
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO]`: function-by-function score forced to 0. CallStack.kt: snake_case identifier `call_stack` in Kotlin comments; CallStackTest.kt: snake_case identifier `call_stack` in Kotlin comments
- `compiler.constants` -> `compiler.Constants [ZERO]`: function-by-function score forced to 0. Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments
- `profile.instant` -> `profile.Instant [ZERO]`: function-by-function score forced to 0. Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust attribute syntax in Kotlin comments
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]`: function-by-function score forced to 0. UnpackAndDiscard.kt: snake_case identifier `unpack_value_impl` in Kotlin comments; UnpackAndDiscard.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `errors.did_you_mean` -> `errors.DidYouMean [ZERO]`: function-by-function score forced to 0. DidYouMean.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `sealed` -> `starlark.Sealed [ZERO]`: function-by-function score forced to 0. Sealed.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `types.record` -> `types.Record [ZERO]`: function-by-function score forced to 0. Record.kt: snake_case identifier `record_type` in Kotlin comments; Record.kt: Rust `pub` item in Kotlin comments; Record.kt: Rust `use` path in Kotlin comments
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]`: function-by-function score forced to 0. SmallVec1.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]`: function-by-function score forced to 0. OwnedFrozenRef.kt: translator-note comment (`Kotlin:`) in Kotlin comments; OwnedFrozenRef.kt: Rust lifetime explanation in Kotlin comments
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]`: function-by-function score forced to 0. ConstTypeId.kt: snake_case identifier `const_type_id` in Kotlin comments; ConstTypeId.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.str_` -> `avalues.Str [ZERO]`: function-by-function score forced to 0. Str.kt: snake_case identifier `alloc_str_init` in Kotlin comments; Str.kt: Rust-only type/unsafe terminology in Kotlin comments
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]`: function-by-function score forced to 0. RustLoc.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.stack_guard` -> `values.StackGuard [ZERO]`: function-by-function score forced to 0. StackGuard.kt: snake_case identifier `to_str` in Kotlin comments
- `profile.string_index` -> `profile.StringIndex [ZERO]`: function-by-function score forced to 0. StringIndex.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `collections.string_pool` -> `collections.StringPool [ZERO]`: function-by-function score forced to 0. StringPool.kt: snake_case identifier `debug_assert` in Kotlin comments; StringPool.kt: Rust macro invocation in Kotlin comments; StringPool.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]`: function-by-function score forced to 0. LocalAsValue.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; LocalAsValue.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.thin_box_slice_frozen_value` -> `values.ThinBoxSliceFrozenValue [ZERO]`: function-by-function score forced to 0. ThinBoxSliceFrozenValue.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.arena` -> `heap.Arena [ZERO]`: function-by-function score forced to 0. Arena.kt: snake_case identifier `HAS_invoke` in Kotlin code; Arena.kt: snake_case identifier `non_drop` in Kotlin comments; Arena.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Arena.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arena.kt: Rust lifetime explanation in Kotlin comments
- `collections.alloca` -> `collections.Alloca [ZERO]`: function-by-function score forced to 0. Alloca.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `stdlib` -> `starlark.Stdlib [ZERO]`: function-by-function score forced to 0. Stdlib.kt: snake_case identifier `call_stack` in Kotlin comments; StdlibTest.kt: snake_case identifier `HAS_equals` in Kotlin code
- `types.list_or_tuple` -> `types.ListOrTuple [ZERO]`: function-by-function score forced to 0. ListOrTuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `layout.pointer` -> `layout.Pointer [ZERO]`: function-by-function score forced to 0. Pointer.kt: snake_case identifier `get_user_tag` in Kotlin comments; Pointer.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Pointer.kt: Rust lifetime explanation in Kotlin comments
- `types.any_complex` -> `types.AnyComplex [ZERO]`: function-by-function score forced to 0. AnyComplexTest.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `types.any_array` -> `types.AnyArray [ZERO]`: function-by-function score forced to 0. AnyArray.kt: score-padding suppression annotation `@Suppress` in Kotlin code; AnyArray.kt: translator-note comment (`Kotlin:`) in Kotlin comments; AnyArray.kt: Rust-only type/unsafe terminology in Kotlin comments
- `util.rtabort` -> `util.Rtabort [ZERO]`: function-by-function score forced to 0. Rtabort.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `bc.if_debug` -> `bc.IfDebug [ZERO]`: function-by-function score forced to 0. IfDebug.kt: snake_case identifier `debug_assertions` in Kotlin comments; IfDebug.kt: Rust attribute syntax in Kotlin comments; IfDebug.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `read_line` -> `starlark.ReadLine [ZERO]`: function-by-function score forced to 0. ReadLine.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `util.non_static_type_id` -> `util.NonStaticTypeId [ZERO]`: function-by-function score forced to 0. NonStaticTypeId.kt: Rust lifetime explanation in Kotlin comments; NonStaticTypeId.kt: Rust-only type/unsafe terminology in Kotlin comments
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]`: function-by-function score forced to 0. CheapCallStack.kt: translator-note comment (`Kotlin:`) in Kotlin comments; CheapCallStack.kt: Rust lifetime explanation in Kotlin comments
- `avalues.simple` -> `avalues.Simple [ZERO]`: function-by-function score forced to 0. Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments
- `record.field` -> `record.Field [ZERO]`: function-by-function score forced to 0. Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `typing.bindings` -> `typing.Bindings [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments
- `heap.fast_cell` -> `heap.FastCell [ZERO]`: function-by-function score forced to 0. FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust macro invocation in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]`: function-by-function score forced to 0. UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments
- `typing.function` -> `typing.Function [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Function.kt: Rust attribute syntax in Kotlin comments
- `string.interpolation` -> `string.Interpolation [ZERO]`: function-by-function score forced to 0. Interpolation.kt: snake_case identifier `string_pool` in Kotlin comments
- `typing` -> `starlark.Typing [ZERO]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `types.int` -> `types.Int [ZERO]`: function-by-function score forced to 0. Int.kt: snake_case identifier `num_bigint` in Kotlin comments
- `set.methods` -> `set.Methods [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `string.str_type` -> `string.StrType [ZERO]`: function-by-function score forced to 0. StrType.kt: snake_case identifier `starlark_value` in Kotlin comments; StrType.kt: Rust attribute syntax in Kotlin comments
- `int.int_or_big` -> `int.IntOrBig [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; IntOrBig.kt: snake_case identifier `from_str_radix` in Kotlin comments
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]`: function-by-function score forced to 0. ThinBox.kt: snake_case identifier `buck2_util` in Kotlin comments; ThinBox.kt: Rust-only type/unsafe terminology in Kotlin comments
- `layout.typed` -> `layout.ValueTyped [ZERO]`: function-by-function score forced to 0. ValueTyped.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing.callable` -> `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]`: function-by-function score forced to 0. Callable.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `typing.user` -> `typing.User [ZERO]`: function-by-function score forced to 0. UserTest.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `scope.payload` -> `scope.Payload [ZERO]`: function-by-function score forced to 0. no target functions found; report scoring is function-by-function only
- `typed.string` -> `typed.String [ZERO]`: function-by-function score forced to 0. String.kt: snake_case identifier `HAS_equals` in Kotlin code
- `dict.value` -> `dict.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bigint.convert` -> `bigint.Convert [ZERO]`: function-by-function score forced to 0. Convert.kt: Rust-only type/unsafe terminology in Kotlin comments
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]`: function-by-function score forced to 0. PackedImpl.kt: Rust-only type/unsafe terminology in Kotlin comments
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]`: function-by-function score forced to 0. AllocUnpack.kt: snake_case identifier `alloc_frozen_string_value` in Kotlin comments
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]`: function-by-function score forced to 0. VtableRegistry.kt: snake_case identifier `type_name` in Kotlin comments; VtableRegistry.kt: Rust lifetime explanation in Kotlin comments; VtableRegistryTest.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; VtableRegistryTest.kt: Rust attribute syntax in Kotlin comments
- `record.globals` -> `record.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `rec_type` in Kotlin comments
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]`: function-by-function score forced to 0. Compiled.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Compiled.kt: snake_case identifier `check_matches` in Kotlin comments
- `bc.instrs` -> `bc.Instrs [ZERO]`: function-by-function score forced to 0. Instrs.kt: snake_case identifier `drop_in_place` in Kotlin comments; Instrs.kt: Rust attribute syntax in Kotlin comments; Instrs.kt: Rust `match` expression in Kotlin comments; Instrs.kt: Rust lifetime explanation in Kotlin comments; Instrs.kt: Rust-only type/unsafe terminology in Kotlin comments
- `range.range_type` -> `range.RangeType [ZERO]`: function-by-function score forced to 0. RangeType.kt: snake_case identifier `HAS_iterate` in Kotlin code; RangeType.kt: snake_case identifier `saturating_mul` in Kotlin comments
- `values.unpack` -> `values.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `starlark_module` in Kotlin comments
- `tuple.unpack` -> `tuple.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `list.value` -> `list.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.heap_type` -> `heap.HeapType [ZERO]`: function-by-function score forced to 0. HeapType.kt: snake_case identifier `into_ref` in Kotlin comments; HeapType.kt: score-padding suppression annotation `@Suppress` in Kotlin code; HeapType.kt: Rust lifetime explanation in Kotlin comments; HeapType.kt: Rust-only type/unsafe terminology in Kotlin comments; HeapTypeTest.kt: snake_case identifier `test_send_sync` in Kotlin comments; HeapTypeTest.kt: Rust `fn` declaration in Kotlin comments
- `set.value` -> `set.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `layout.complex` -> `layout.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `new_err` in Kotlin comments; Complex.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Complex.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Complex.kt: Rust lifetime explanation in Kotlin comments
- `compiler.scope` -> `compiler.Scope [ZERO]`: function-by-function score forced to 0. Scope.kt: snake_case identifier `scope_resolver_globals` in Kotlin comments
- `float.float` -> `float.Float [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Float.kt: snake_case identifier `HAS_equals` in Kotlin code; Float.kt: snake_case identifier `starlark_value` in Kotlin comments; Float.kt: Rust attribute syntax in Kotlin comments; Float.kt: Rust lifetime explanation in Kotlin comments
- `adapter.implementation` -> `adapter.Implementation [ZERO]`: function-by-function score forced to 0. Implementation.kt: snake_case identifier `before_stmt` in Kotlin comments
- `string.repr` -> `string.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `is_printable` in Kotlin comments; Repr.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.send` -> `heap.Send [ZERO]`: function-by-function score forced to 0. Send.kt: Rust `fn` declaration in Kotlin comments; Send.kt: Rust lifetime explanation in Kotlin comments; Send.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.unpack` -> `list.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `unpack_value_opt` in Kotlin comments; Unpack.kt: Rust `let` binding in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments
- `string.simd` -> `string.Simd [ZERO]`: function-by-function score forced to 0. Simd.kt: snake_case identifier `x86_64` in Kotlin comments
- `assert.assert` -> `assert.Assert [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Assert.kt: snake_case identifier `assert_eq` in Kotlin comments; Assert.kt: Rust `pub` item in Kotlin comments; Assert.kt: Rust `use` path in Kotlin comments; Assert.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Assert.kt: Rust lifetime explanation in Kotlin comments
- `values.owned` -> `values.Owned [ZERO]`: function-by-function score forced to 0. Owned.kt: Rust lifetime explanation in Kotlin comments
- `profile.aggregated` -> `profile.Aggregated [ZERO]`: function-by-function score forced to 0. Aggregated.kt: snake_case identifier `string_index` in Kotlin comments
- `bc.addr` -> `bc.Addr [ZERO]`: function-by-function score forced to 0. Addr.kt: snake_case identifier `debug_assert` in Kotlin comments; Addr.kt: Rust macro invocation in Kotlin comments
- `stdlib.partial` -> `stdlib.Partial [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Partial.kt: snake_case identifier `HAS_invoke` in Kotlin code; Partial.kt: snake_case identifier `alloca_concat` in Kotlin comments
- `environment.modules` -> `environment.Modules [ZERO]`: function-by-function score forced to 0. Modules.kt: snake_case identifier `starlark_module` in Kotlin comments; Modules.kt: Rust `fn` declaration in Kotlin comments; Modules.kt: Rust `pub` item in Kotlin comments; Modules.kt: Rust attribute syntax in Kotlin comments; Modules.kt: Rust lifetime explanation in Kotlin comments
- `tuple.value` -> `tuple.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code
- `alloc.allocator` -> `alloc.Allocator [ZERO]`: function-by-function score forced to 0. Allocator.kt: Rust `pub` item in Kotlin comments; Allocator.kt: Rust lifetime explanation in Kotlin comments; Allocator.kt: Rust-only type/unsafe terminology in Kotlin comments
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]`: function-by-function score forced to 0. Bumpalo.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `params.spec` -> `params.Spec [ZERO]`: function-by-function score forced to 0. Spec.kt: snake_case identifier `no_args` in Kotlin comments
- `typing.custom` -> `typing.Custom [ZERO]`: function-by-function score forced to 0. Custom.kt: snake_case identifier `validate_call` in Kotlin comments; Custom.kt: Rust `fn` declaration in Kotlin comments; Custom.kt: Rust `pub` item in Kotlin comments; Custom.kt: Rust-only type/unsafe terminology in Kotlin comments
- `heap.repr` -> `heap.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `forward_ptr` in Kotlin comments; Repr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `alloc.chain` -> `alloc.Chain [ZERO]`: function-by-function score forced to 0. Chain.kt: Rust attribute syntax in Kotlin comments
- `structs.alloc` -> `structs.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `fn` declaration in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments; Alloc.kt: Rust lifetime explanation in Kotlin comments; no target functions found; report scoring is function-by-function only
- `dict.alloc` -> `dict.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments
- `bc.instr_arg` -> `bc.InstrArg [ZERO]`: function-by-function score forced to 0. InstrArg.kt: snake_case identifier `fmt_append` in Kotlin comments; InstrArg.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.def` -> `compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: snake_case identifier `HAS_invoke` in Kotlin code; Def.kt: unchecked cast suppression hiding transliteration work in Kotlin code; Def.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]`: function-by-function score forced to 0. StackPtr.kt: snake_case identifier `assert_eq` in Kotlin comments; StackPtr.kt: Rust macro invocation in Kotlin comments; StackPtr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.refs` -> `list.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `from_value` in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]`: function-by-function score forced to 0. Matcher.kt: snake_case identifier `type_matcher` in Kotlin comments; Matcher.kt: Rust attribute syntax in Kotlin comments; Matcher.kt: Rust lifetime explanation in Kotlin comments; Matcher.kt: Rust-only type/unsafe terminology in Kotlin comments; Matcher.kt: Rust auto-trait terminology in Kotlin comments
- `bc.opcode` -> `bc.Opcode [ZERO]`: function-by-function score forced to 0. Opcode.kt: snake_case identifier `dispatch_all` in Kotlin comments
- `symbol.map` -> `symbol.Map [ZERO]`: function-by-function score forced to 0. Map.kt: Rust-only type/unsafe terminology in Kotlin comments
- `eval.bc.compiler.stmt` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `bc.repr` -> `bc.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `align_of` in Kotlin comments
- `debug.inspect` -> `debug.Inspect [ZERO]`: function-by-function score forced to 0. Inspect.kt: snake_case identifier `call_stack` in Kotlin comments
- `eval.bc.compiler.call` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.expr` -> `compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `get_methods` in Kotlin comments; Expr.kt: Rust `let` binding in Kotlin comments
- `values.traits` -> `values.Traits [ZERO]`: function-by-function score forced to 0. Traits.kt: snake_case identifier `HAS_invoke` in Kotlin code; Traits.kt: snake_case identifier `starlark_value` in Kotlin comments; Traits.kt: Rust `let` binding in Kotlin comments; Traits.kt: Rust attribute syntax in Kotlin comments
- `types.function` -> `types.Function [ZERO]`: function-by-function score forced to 0. Function.kt: snake_case identifier `HAS_invoke` in Kotlin code; Function.kt: snake_case identifier `starlark_module` in Kotlin comments; Function.kt: Rust attribute syntax in Kotlin comments
- `profile.stmt` -> `profile.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `last_span` in Kotlin comments; Stmt.kt: Rust-only type/unsafe terminology in Kotlin comments
- `structs.value` -> `structs.Value [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Value.kt: snake_case identifier `HAS_equals` in Kotlin code; Value.kt: snake_case identifier `of_value` in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments
- `avalues.tuple` -> `avalues.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `register_special_avalue_frozen` in Kotlin comments; Tuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.array` -> `avalues.Array [ZERO]`: function-by-function score forced to 0. Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.complex` -> `avalues.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `alloc_complex` in Kotlin comments
- `avalues.static_` -> `avalues.Static [ZERO]`: function-by-function score forced to 0. Static.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `record.instance` -> `record.Instance [ZERO]`: function-by-function score forced to 0. Instance.kt: snake_case identifier `HAS_equals` in Kotlin code; Instance.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]`: function-by-function score forced to 0. SummaryByFunction.kt: snake_case identifier `top_stack` in Kotlin comments
- `profile.typecheck` -> `profile.Typecheck [ZERO]`: function-by-function score forced to 0. Typecheck.kt: snake_case identifier `by_function` in Kotlin comments
- `values.type_repr` -> `values.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: snake_case identifier `type_repr` in Kotlin comments
- `bc.call` -> `bc.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `structs.refs` -> `structs.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `debug_assert` in Kotlin comments
- `params.parser` -> `params.Parser [ZERO]`: function-by-function score forced to 0. Parser.kt: snake_case identifier `get_next` in Kotlin comments
- `typing.iter` -> `typing.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Iter.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `set.refs` -> `set.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `unpack_value_opt` in Kotlin comments
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]`: function-by-function score forced to 0. RecursiveReprOrJsonGuard.kt: snake_case identifier `to_json` in Kotlin comments
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]`: function-by-function score forced to 0. CallEnterExit.kt: snake_case identifier `needs_drop` in Kotlin comments
- `list.globals` -> `list.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `register_list` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `funcs.min_max` -> `funcs.MinMax [ZERO]`: function-by-function score forced to 0. MinMax.kt: snake_case identifier `update_max_ordering` in Kotlin comments; MinMax.kt: Rust `match` expression in Kotlin comments
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]`: function-by-function score forced to 0. MaybeUninitBackport.kt: snake_case identifier `write_slice_cloned` in Kotlin comments
- `debug.evaluate` -> `debug.Evaluate [ZERO]`: function-by-function score forced to 0. Evaluate.kt: snake_case identifier `module_env` in Kotlin comments
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `eval_type` in Kotlin comments
- `bool.type_repr` -> `bool.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `debug.adapter` -> `debug.Adapter [ZERO]`: function-by-function score forced to 0. Adapter.kt: snake_case identifier `Requests_SetBreakpoints` in Kotlin comments
- `docs` -> `docs.Docs [ZERO]`: function-by-function score forced to 0. Docs.kt: Rust lifetime explanation in Kotlin comments
- `funcs.other` -> `funcs.Other [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Other.kt: Rust lifetime explanation in Kotlin comments
- `docs.parse` -> `docs.Parse [ZERO]`: function-by-function score forced to 0. Parse.kt: snake_case identifier `some_function` in Kotlin comments; Parse.kt: Rust attribute syntax in Kotlin comments; ParseTest.kt: snake_case identifier `from_docstring` in Kotlin comments
- `analysis` -> `starlark.Analysis [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies
- `list.methods` -> `list.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Methods.kt: Rust `let` binding in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `typing.never` -> `typing.Never [ZERO]`: function-by-function score forced to 0. Never.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Never.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `profile.by_type` -> `profile.ByType [ZERO]`: function-by-function score forced to 0. ByType.kt: snake_case identifier `allocated_summary` in Kotlin comments
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]`: function-by-function score forced to 0. IfCompiler.kt: snake_case identifier `maybe_not` in Kotlin comments
- `set.set` -> `set.Set [ZERO]`: function-by-function score forced to 0. Set.kt: snake_case identifier `starlark_module` in Kotlin comments; Set.kt: Rust attribute syntax in Kotlin comments
- `structs.structs` -> `structs.Structs [ZERO]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `starlark_module` in Kotlin comments; Structs.kt: Rust attribute syntax in Kotlin comments
- `dict.globals` -> `dict.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `as_type` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `float.globals` -> `float.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `int.globals` -> `int.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]`: function-by-function score forced to 0. Matchers.kt: snake_case identifier `starlark_type_id` in Kotlin comments; Matchers.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `bc.writer` -> `bc.Writer [ZERO]`: function-by-function score forced to 0. Writer.kt: snake_case identifier `alloc_any` in Kotlin comments
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]`: function-by-function score forced to 0. FillTypesForLint.kt: snake_case identifier `of_value` in Kotlin comments; FillTypesForLint.kt: Rust `match` expression in Kotlin comments
- `string.methods` -> `string.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `is_true` in Kotlin comments; Methods.kt: Rust lifetime explanation in Kotlin comments
- `typing.error` -> `typing.Error [ZERO]`: function-by-function score forced to 0. Error.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `docs.markdown` -> `docs.Markdown [ZERO]`: function-by-function score forced to 0. Markdown.kt: Rust `let` binding in Kotlin comments
- `dict.methods` -> `dict.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: Rust lifetime explanation in Kotlin comments
- `environment.names` -> `environment.Names [ZERO]`: function-by-function score forced to 0. Names.kt: snake_case identifier `collect_defines_lvalue` in Kotlin comments
- `eval.bc.compiler.expr` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `mark_definitely_assigned_after` in Kotlin comments
- `docs.code` -> `docs.Code [ZERO]`: function-by-function score forced to 0. Code.kt: snake_case identifier `render_as_code` in Kotlin comments
- `compiler.types` -> `compiler.Types [ZERO]`: function-by-function score forced to 0. Types.kt: snake_case identifier `expr_ident` in Kotlin comments
- `record.ty_record_type` -> `record.TyRecordType [ZERO]`: function-by-function score forced to 0. TyRecordType.kt: Rust `pub` item in Kotlin comments; TyRecordType.kt: Rust attribute syntax in Kotlin comments
- `alloc.per_thread` -> `alloc.PerThread [ZERO]`: function-by-function score forced to 0. PerThread.kt: snake_case identifier `thread_local` in Kotlin comments
- `intern.interner` -> `intern.Interner [ZERO]`: function-by-function score forced to 0. Interner.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing.ty` -> `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]`: function-by-function score forced to 0. Ty.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `values.index` -> `values.Index [ZERO]`: function-by-function score forced to 0. Index.kt: snake_case identifier `set_at` in Kotlin comments; IndexTest.kt: snake_case identifier `convert_index` in Kotlin comments
- `layout.static_string` -> `layout.StaticString [ZERO]`: function-by-function score forced to 0. StaticString.kt: Rust attribute syntax in Kotlin comments; StaticString.kt: Rust-only type/unsafe terminology in Kotlin comments
- `string.globals` -> `string.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]`: function-by-function score forced to 0. Remove.kt: Rust lifetime explanation in Kotlin comments
- `typing.any` -> `typing.Any [ZERO]`: function-by-function score forced to 0. Any.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `string.iter` -> `string.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_iterate` in Kotlin code; Iter.kt: snake_case identifier `produce_char` in Kotlin comments
- `eval.bc.compiler.def` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.assign` -> `compiler.Assign [ZERO]`: function-by-function score forced to 0. Assign.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `environment` -> `starlark.Environment [ZERO]`: function-by-function score forced to 0. Environment.kt: snake_case identifier `module_dump` in Kotlin comments; Environment.kt: Rust `pub` item in Kotlin comments; Environment.kt: Rust `use` path in Kotlin comments
- `bc.for_loop` -> `bc.ForLoop [ZERO]`: function-by-function score forced to 0. ForLoop.kt: snake_case identifier `derive_more` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `values.typing` -> `values.Typing [ZERO]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.oracle` -> `typing.Oracle [ZERO]`: function-by-function score forced to 0. Oracle.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `macros` -> `starlark.Macros [ZERO]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `errors` -> `starlark.Errors [ZERO]`: function-by-function score forced to 0. Errors.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `stdlib.funcs` -> `stdlib.Funcs [ZERO]`: function-by-function score forced to 0. Funcs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `util` -> `starlark.Util [ZERO]`: function-by-function score forced to 0. Util.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]`: function-by-function score forced to 0. VtableRegister.kt: snake_case identifier `register_avalue_simple_frozen` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `eval.runtime` -> `eval.Runtime [ZERO]`: function-by-function score forced to 0. Runtime.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `syntax` -> `starlark.Syntax [ZERO]`: function-by-function score forced to 0. Syntax.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable` -> `starlark.Pagable [ZERO]`: function-by-function score forced to 0. Pagable.kt: snake_case identifier `type_name` in Kotlin comments; Pagable.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.types` -> `values.Types [ZERO]`: function-by-function score forced to 0. Types.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.avalues` -> `layout.AValues [ZERO]`: function-by-function score forced to 0. AValues.kt: score-padding suppression annotation `@Suppress` in Kotlin code

## Critical Issues (Function Similarity < 0.60 with Dependencies)

These files need immediate attention:

- **layout.value** → `layout.Value`
  - Function similarity: 0.33
  - Dependencies: 178
  - Functions: 142/155 matched (target 166)
  - Missing functions: `fmt`, `fmt`, `fmt`, `fmt`, `eq`, `eq`, `testing_new_int`, `display_for_type_error`, `fmt`, `testing_new_int`, `to_value`, `compare`, `_test_send_sync`
  - Types: 7/9 matched (target 11)
  - Missing types: `Canonical`, `String`

- **typing.ty** → `typing.Ty`
  - Function similarity: 0.39
  - Dependencies: 109
  - Functions: 49/52 matched (target 57)
  - Missing functions: `fmt`, `fmt`, `fmt`
  - Types: 4/4 matched (target 6)
  - Missing types: _none_

- **typing.starlark_value** → `typing.StarlarkValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 76
  - Functions: 29/35 matched (target 45)
  - Missing functions: `fmt`, `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`
  - Types: 4/4 matched (target 5)
  - Missing types: _none_
  - Scoring failure: StarlarkValue.kt: snake_case identifier `starlark_type_id` in Kotlin comments

- **params.display** → `params.Display`
  - Function similarity: 0.26
  - Dependencies: 76
  - Functions: 4/4 matched
  - Missing functions: _none_
  - Types: 3/3 matched (target 8)
  - Missing types: _none_

- **runtime.evaluator** → `runtime.Evaluator [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 55
  - Functions: 60/64 matched (target 63)
  - Missing functions: `drop`, `new`, `add_diagnostics`, `before_stmt`
  - Types: 7/7 matched (target 17)
  - Missing types: _none_
  - Scoring failure: Evaluator.kt: snake_case identifier `before_stmt` in Kotlin comments; Evaluator.kt: Rust lifetime explanation in Kotlin comments
  - Lint issues: 3

- **values.trace** → `values.Trace [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 52
  - Functions: 43/43 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Trace.kt: Rust lifetime explanation in Kotlin comments; Trace.kt: Rust-only type/unsafe terminology in Kotlin comments

- **values.freeze** → `values.Freeze [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 42
  - Functions: 23/26 matched (target 31)
  - Missing functions: `freeze`, `freeze`, `freeze`
  - Types: 1/2 matched (target 6)
  - Missing types: `Frozen`
  - Scoring failure: Freeze.kt: Rust-only type/unsafe terminology in Kotlin comments

- **values.alloc_value** → `values.AllocValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 42
  - Functions: 5/5 matched
  - Missing functions: _none_
  - Types: 4/4 matched
  - Missing types: _none_
  - Scoring failure: AllocValue.kt: snake_case identifier `alloc_simple` in Kotlin comments; AllocValue.kt: Rust `fn` declaration in Kotlin comments; AllocValue.kt: Rust attribute syntax in Kotlin comments; AllocValue.kt: Rust `use` path in Kotlin comments; AllocValue.kt: Rust lifetime explanation in Kotlin comments

- **layout.freezer** → `layout.Freezer [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 36
  - Functions: 5/5 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Freezer.kt: snake_case identifier `debug_assert` in Kotlin comments; Freezer.kt: Rust macro invocation in Kotlin comments

- **compiler.span** → `compiler.Span`
  - Function similarity: 0.23
  - Dependencies: 29
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 1/2 matched (target 1)
  - Missing types: `Target`

- **values.frozen_ref** → `values.FrozenRef [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 27
  - Functions: 20/20 matched (target 23)
  - Missing functions: _none_
  - Types: 2/4 matched (target 2)
  - Missing types: `Target`, `Frozen`
  - Scoring failure: FrozenRef.kt: Rust `fn` declaration in Kotlin comments; FrozenRef.kt: score-padding suppression annotation `@Suppress` in Kotlin code; FrozenRef.kt: Rust lifetime explanation in Kotlin comments

- **none.none_type** → `none.NoneType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 27
  - Functions: 11/11 matched (target 16)
  - Missing functions: _none_
  - Types: 1/2 matched
  - Missing types: `Error`
  - Scoring failure: NoneType.kt: snake_case identifier `HAS_eval_type` in Kotlin code; NoneType.kt: snake_case identifier `serialize_none` in Kotlin comments

- **runtime.frame_span** → `runtime.FrameSpan`
  - Function similarity: 0.24
  - Dependencies: 26
  - Functions: 3/4 matched
  - Missing functions: `fmt`
  - Types: 1/1 matched
  - Missing types: _none_

- **runtime.arguments** → `runtime.Arguments [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 25
  - Functions: 37/40 matched (target 55)
  - Missing functions: `get_index_from_param_spec`, `small_hash`, `f`
  - Types: 8/8 matched (target 17)
  - Missing types: _none_
  - Scoring failure: Arguments.kt: snake_case identifier `to_string` in Kotlin comments; Arguments.kt: Rust macro invocation in Kotlin comments; Arguments.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arguments.kt: Rust lifetime explanation in Kotlin comments

- **typing.type_compiled** → `type_compiled.TypeCompiled [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 22
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: TypeCompiled.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **values.value_of_unchecked** → `values.ValueOfUnchecked [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 20
  - Functions: 15/20 matched (target 24)
  - Missing functions: `fmt`, `fmt`, `assert_send_sync`, `starlark_type_repr`, `_assert_covariant`
  - Types: 4/7 matched (target 8)
  - Missing types: `Canonical`, `Frozen`, `Error`
  - Scoring failure: ValueOfUnchecked.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; ValueOfUnchecked.kt: Rust `fn` declaration in Kotlin comments; ValueOfUnchecked.kt: score-padding suppression annotation `@Suppress` in Kotlin code; ValueOfUnchecked.kt: Rust lifetime explanation in Kotlin comments; ValueOfUncheckedTest.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **environment.globals** → `environment.Globals [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 20
  - Functions: 34/39 matched (target 42)
  - Missing functions: `get`, `new`, `test_send_sync`, `register_foo`, `foo`
  - Types: 5/5 matched (target 7)
  - Missing types: _none_
  - Scoring failure: Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; GlobalsTest.kt: snake_case identifier `test_send_sync` in Kotlin comments; GlobalsTest.kt: Rust `fn` declaration in Kotlin comments; GlobalsTest.kt: Rust auto-trait terminology in Kotlin comments

- **util.refcell** → `refcell.RefCell`
  - Function similarity: 0.28
  - Dependencies: 20
  - Functions: 2/2 matched (target 12)
  - Missing functions: _none_
  - Types: 0/0 matched (target 4)
  - Missing types: _none_

- **environment.methods** → `environment.Methods [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 17
  - Functions: 19/21 matched (target 26)
  - Missing functions: `new`, `get_methods`
  - Types: 4/4 matched (target 5)
  - Missing types: _none_
  - Scoring failure: Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **values.iter** → `values.Iter [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 17
  - Functions: 4/5 matched (target 6)
  - Missing functions: `drop`
  - Types: 1/2 matched (target 1)
  - Missing types: `Item`
  - Scoring failure: Iter.kt: snake_case identifier `iter_stop` in Kotlin comments; Iter.kt: Rust-only type/unsafe terminology in Kotlin comments

- **values.error** → `values.Error`
  - Function similarity: 0.28
  - Dependencies: 17
  - Functions: 4/5 matched
  - Missing functions: `from`
  - Types: 2/2 matched (target 20)
  - Missing types: _none_

- **private** → `starlark.Private [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 15
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Private.kt: snake_case identifier `missing_docs` in Kotlin comments

- **collections.symbol** → `collections.Symbol [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 15
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Symbol.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **layout.avalue** → `layout.AValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 14
  - Functions: 8/8 matched (target 13)
  - Missing functions: _none_
  - Types: 3/3 matched (target 4)
  - Missing types: _none_
  - Scoring failure: AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments; AValueTest.kt: snake_case identifier `try_freeze_directly` in Kotlin comments

- **typing.tuple** → `typing.Tuple [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 12
  - Functions: 5/6 matched (target 9)
  - Missing functions: `fmt`
  - Types: 1/1 matched (target 3)
  - Missing types: _none_
  - Scoring failure: Tuple.kt: snake_case identifier `type_compiled_factory` in Kotlin comments

- **layout.const_frozen_string** → `layout.ConstFrozenString [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 12
  - Functions: 2/2 matched (target 3)
  - Missing functions: _none_
  - Types: 0/0 matched (target 1)
  - Missing types: _none_
  - Scoring failure: ConstFrozenString.kt: snake_case identifier `constant_string` in Kotlin comments; ConstFrozenStringTest.kt: snake_case identifier `const_frozen_string` in Kotlin comments; ConstFrozenStringTest.kt: Rust macro invocation in Kotlin comments

- **int.inline_int** → `int.InlineInt [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 11
  - Functions: 29/42 matched (target 43)
  - Missing functions: `fmt`, `testing_new`, `try_from_impl`, `try_from`, `try_from`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `eq`, `partial_cmp`, `test_min_max_for_bits`
  - Types: 2/5 matched (target 6)
  - Missing types: `Error`, `Output`, `Canonical`
  - Scoring failure: InlineInt.kt: snake_case identifier `derive_more` in Kotlin comments; InlineInt.kt: Rust `fn` declaration in Kotlin comments; InlineInt.kt: Rust `pub` item in Kotlin comments; InlineInt.kt: Rust attribute syntax in Kotlin comments; InlineInt.kt: score-padding suppression annotation `@Suppress` in Kotlin code; InlineInt.kt: Rust lifetime explanation in Kotlin comments

- **int.pointer_i32** → `int.PointerI32 [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 9
  - Functions: 28/32 matched (target 34)
  - Missing functions: `eq`, `fmt`, `fmt`, `serialize`
  - Types: 1/2 matched
  - Missing types: `Canonical`
  - Scoring failure: PointerI32.kt: snake_case identifier `HAS_equals` in Kotlin code; PointerI32.kt: snake_case identifier `from_raw_pointer_unchecked` in Kotlin comments; PointerI32.kt: Rust `fn` declaration in Kotlin comments; PointerI32.kt: Rust `pub` item in Kotlin comments; PointerI32.kt: score-padding suppression annotation `@Suppress` in Kotlin code; PointerI32.kt: Rust lifetime explanation in Kotlin comments

- **types.type_instance_id** → `types.TypeInstanceId`
  - Function similarity: 0.00
  - Dependencies: 9
  - Functions: 0/1 matched (target 2)
  - Missing functions: `r#gen`
  - Types: 1/1 matched
  - Missing types: _none_

- **layout.aligned_size** → `layout.AlignedSize [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 8
  - Functions: 6/13 matched (target 15)
  - Missing functions: `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`
  - Scoring failure: AlignedSize.kt: snake_case identifier `size_of` in Kotlin comments; AlignedSize.kt: Rust macro invocation in Kotlin comments; AlignedSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **any** → `starlark.Any`
  - Function similarity: 0.12
  - Dependencies: 8
  - Functions: 8/13 matched (target 17)
  - Missing functions: `is`, `convert_value`, `convert_any`, `test`, `test`
  - Types: 14/15 matched (target 50)
  - Missing types: `StaticType`

- **eval.compiler** → `eval.Compiler [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 8
  - Functions: 6/6 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Compiler.kt: snake_case identifier `def_inline` in Kotlin comments

- **cast** → `starlark.Cast [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 8
  - Functions: 3/3 matched (target 4)
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Cast.kt: Rust lifetime explanation in Kotlin comments; Cast.kt: Rust-only type/unsafe terminology in Kotlin comments

- **types.bigint** → `types.Bigint [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 7
  - Functions: 71/73 matched (target 77)
  - Missing functions: `unpack_integer`, `eq`
  - Types: 1/1 matched (target 2)
  - Missing types: _none_
  - Scoring failure: Bigint.kt: snake_case identifier `HAS_equals` in Kotlin code; Bigint.kt: snake_case identifier `non_zero_int` in Kotlin comments; Bigint.kt: Rust attribute syntax in Kotlin comments; Bigint.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Bigint.kt: Rust lifetime explanation in Kotlin comments

- **runtime.frozen_file_span** → `runtime.FrozenFileSpan [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 7
  - Functions: 9/10 matched
  - Missing functions: `fmt`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: FrozenFileSpan.kt: snake_case identifier `empty_static` in Kotlin comments

- **values.starlark_type_id** → `values.StarlarkTypeId`
  - Function similarity: 0.27
  - Dependencies: 7
  - Functions: 5/6 matched (target 7)
  - Missing functions: `eq`
  - Types: 2/2 matched
  - Missing types: _none_

- **compiler.opt_ctx** → `compiler.OptCtx`
  - Function similarity: 0.18
  - Dependencies: 7
  - Functions: 13/13 matched
  - Missing functions: _none_
  - Types: 2/2 matched (target 4)
  - Missing types: _none_

- **type_compiled.type_matcher_factory** → `type_compiled.TypeMatcherFactory`
  - Function similarity: 0.21
  - Dependencies: 7
  - Functions: 3/3 matched (target 6)
  - Missing functions: _none_
  - Types: 3/3 matched
  - Missing types: _none_

- **runtime.small_duration** → `runtime.SmallDuration [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 4/10 matched (target 9)
  - Missing functions: `from_millis`, `add_assign`, `add_assign`, `add`, `add`, `sum`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`
  - Scoring failure: SmallDuration.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **values.freeze_error** → `values.FreezeError [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 3/5 matched (target 6)
  - Missing functions: `from`, `from`
  - Types: 3/4 matched (target 3)
  - Missing types: `FreezeResult`
  - Scoring failure: FreezeError.kt: snake_case identifier `starlark_syntax` in Kotlin comments

- **typing.typecheck** → `typing.Typecheck [STUB]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 2/5 matched
  - Missing functions: `fmt`, `find_bindings_by_name`, `find_first_binding`
  - Types: 2/2 matched (target 3)
  - Missing types: _none_
  - Scoring failure: target contains TODO/stub/placeholder markers in function bodies

- **dict.dict_type** → `dict.DictType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 1/2 matched (target 4)
  - Missing functions: `unpack_value_impl`
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `Error`
  - Scoring failure: DictType.kt: snake_case identifier `starlark_type_repr` in Kotlin comments

- **none.none_or** → `none.NoneOr`
  - Function similarity: 0.19
  - Dependencies: 6
  - Functions: 7/7 matched (target 9)
  - Missing functions: _none_
  - Types: 1/3 matched (target 4)
  - Missing types: `Canonical`, `Error`

- **layout.value_alloc_size** → `layout.ValueAllocSize [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 4/5 matched
  - Missing functions: `layout`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: ValueAllocSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **compiler.stmt** → `compiler.Stmt [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 28/28 matched
  - Missing functions: _none_
  - Types: 7/7 matched (target 24)
  - Missing types: _none_
  - Scoring failure: Stmt.kt: snake_case identifier `build_file` in Kotlin comments

- **profile.profiler_type** → `profile.ProfilerType`
  - Function similarity: 0.25
  - Dependencies: 6
  - Functions: 1/1 matched
  - Missing functions: _none_
  - Types: 2/2 matched
  - Missing types: _none_

- **values.layout** → `values.Layout [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Layout.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **types.array** → `types.Array [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 5
  - Functions: 23/34 matched (target 24)
  - Missing functions: `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `fmt`, `is_special`, `serialize`, `debug`, `display`, `push`
  - Types: 2/2 matched
  - Missing types: _none_
  - Scoring failure: Array.kt: snake_case identifier `iter_count` in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **typing.arc_ty** → `typing.ArcTy`
  - Function similarity: 0.34
  - Dependencies: 5
  - Functions: 6/8 matched (target 16)
  - Missing functions: `fmt`, `fmt`
  - Types: 3/4 matched (target 10)
  - Missing types: `Target`

- **typing.interface** → `typing.Interface`
  - Function similarity: 0.19
  - Dependencies: 5
  - Functions: 3/3 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_

- **scope.scope_resolver_globals** → `scope.ScopeResolverGlobals`
  - Function similarity: 0.19
  - Dependencies: 5
  - Functions: 3/3 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_

- **eval.bc** → `bc.Bc [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 5
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Bc.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **enumeration.enum_type** → `enumeration.EnumType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 33/38 matched (target 37)
  - Missing functions: `get_or_init_ty`, `get_ty`, `fmt`, `r#type`, `values`
  - Types: 4/8 matched (target 7)
  - Missing types: `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical`
  - Scoring failure: EnumType.kt: snake_case identifier `HAS_invoke` in Kotlin code
  - Lint issues: 3

- **bc.frame** → `bc.Frame [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 25/33 matched (target 31)
  - Missing functions: `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit`
  - Types: 2/2 matched
  - Missing types: _none_
  - Scoring failure: Frame.kt: snake_case identifier `loop_indices` in Kotlin comments

- **types.starlark_value_as_type** → `types.StarlarkValueAsType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 9/16 matched (target 23)
  - Missing functions: `fmt`, `fmt`, `fmt`, `new`, `alloc_value`, `compiler_args_globals`, `compiler_args`
  - Types: 3/4 matched
  - Missing types: `Canonical`
  - Scoring failure: StarlarkValueAsType.kt: snake_case identifier `HAS_eval_type` in Kotlin code

- **values.value_of** → `values.ValueOf [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 4/6 matched (target 5)
  - Missing functions: `deref`, `fmt`
  - Types: 1/4 matched (target 1)
  - Missing types: `Target`, `Canonical`, `Error`
  - Scoring failure: ValueOf.kt: snake_case identifier `starlark_module` in Kotlin comments

- **profile.alloc_counts** → `profile.AllocCounts`
  - Function similarity: 0.18
  - Dependencies: 4
  - Functions: 1/4 matched (target 5)
  - Missing functions: `normalize_for_golden_tests`, `add_assign`, `add`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`

- **values.demand** → `values.Demand`
  - Function similarity: 0.17
  - Dependencies: 4
  - Functions: 5/7 matched (target 8)
  - Missing functions: `payload`, `provide`
  - Types: 3/4 matched
  - Missing types: `StaticType`

- **bc.native_function** → `bc.NativeFunction`
  - Function similarity: 0.18
  - Dependencies: 4
  - Functions: 3/4 matched
  - Missing functions: `fun`
  - Types: 1/1 matched
  - Missing types: _none_

- **types.ellipsis** → `types.Ellipsis`
  - Function similarity: 0.38
  - Dependencies: 4
  - Functions: 3/3 matched (target 5)
  - Missing functions: _none_
  - Types: 1/1 matched (target 2)
  - Missing types: _none_

- **record.record_type** → `record.RecordType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 20/24 matched (target 23)
  - Missing functions: `get_or_init_ty`, `get_ty`, `fmt`, `r#type`
  - Types: 2/8 matched (target 3)
  - Missing types: `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
  - Scoring failure: RecordType.kt: snake_case identifier `HAS_invoke` in Kotlin code; RecordType.kt: translator-note comment (`Kotlin:`) in Kotlin comments
  - Lint issues: 1

- **alloc.chunk** → `alloc.Chunk [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 12/21 matched (target 20)
  - Missing functions: `fmt`, `begin`, `fmt`, `ptr_eq`, `begin`, `drop`, `clone`, `counter_overflow`, `test_empty`
  - Types: 2/3 matched
  - Missing types: `ChunkDataEmpty`
  - Scoring failure: Chunk.kt: snake_case identifier `ref_count` in Kotlin comments; Chunk.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Chunk.kt: Rust-only type/unsafe terminology in Kotlin comments

- **list.alloc** → `list.Alloc [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 0/3 matched (target 0)
  - Missing functions: `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
  - Types: 1/2 matched (target 1)
  - Missing types: `Canonical`
  - Scoring failure: Alloc.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; no target functions found; report scoring is function-by-function only

- **list.list_type** → `list.ListType`
  - Function similarity: 0.09
  - Dependencies: 3
  - Functions: 1/2 matched (target 5)
  - Missing functions: `unpack_value_impl`
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `Error`

- **stdlib.call_stack** → `stdlib.CallStack [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 11/13 matched (target 15)
  - Missing functions: `fmt`, `global`
  - Types: 1/1 matched (target 2)
  - Missing types: _none_
  - Scoring failure: CallStack.kt: snake_case identifier `call_stack` in Kotlin comments; CallStackTest.kt: snake_case identifier `call_stack` in Kotlin comments

- **compiler.constants** → `compiler.Constants [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 2/4 matched (target 6)
  - Missing functions: `eq`, `eq`
  - Types: 2/2 matched (target 3)
  - Missing types: _none_
  - Scoring failure: Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments

- **profile.instant** → `profile.Instant [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 3/4 matched (target 9)
  - Missing functions: `sub`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`
  - Scoring failure: Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust attribute syntax in Kotlin comments

- **values.unpack_and_discard** → `values.UnpackAndDiscard [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `Error`
  - Scoring failure: UnpackAndDiscard.kt: snake_case identifier `unpack_value_impl` in Kotlin comments; UnpackAndDiscard.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **errors.did_you_mean** → `errors.DidYouMean [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 6/6 matched (target 7)
  - Missing functions: _none_
  - Types: 0/0 matched (target 1)
  - Missing types: _none_
  - Scoring failure: DidYouMean.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **sealed** → `starlark.Sealed [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Sealed.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **types.record** → `types.Record [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Record.kt: snake_case identifier `record_type` in Kotlin comments; Record.kt: Rust `pub` item in Kotlin comments; Record.kt: Rust `use` path in Kotlin comments

- **compiler.small_vec_1** → `compiler.SmallVec1 [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 4/11 matched (target 9)
  - Missing functions: `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter`
  - Types: 1/4 matched (target 3)
  - Missing types: `Target`, `Item`, `IntoIter`
  - Scoring failure: SmallVec1.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **util.arc_or_static** → `util.ArcOrStatic`
  - Function similarity: 0.20
  - Dependencies: 2
  - Functions: 5/10 matched (target 9)
  - Missing functions: `fmt`, `eq`, `partial_cmp`, `cmp`, `hash`
  - Types: 2/3 matched (target 4)
  - Missing types: `Target`

- **values.owned_frozen_ref** → `values.OwnedFrozenRef [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 16/19 matched
  - Missing functions: `fmt`, `fmt`, `deref`
  - Types: 2/3 matched (target 2)
  - Missing types: `Target`
  - Scoring failure: OwnedFrozenRef.kt: translator-note comment (`Kotlin:`) in Kotlin comments; OwnedFrozenRef.kt: Rust lifetime explanation in Kotlin comments

- **typing.type_type** → `typing.TypeType`
  - Function similarity: 0.12
  - Dependencies: 2
  - Functions: 3/5 matched (target 8)
  - Missing functions: `module`, `takes_type`
  - Types: 1/3 matched (target 2)
  - Missing types: `Canonical`, `Error`

- **alloc.chunk_part** → `alloc.ChunkPart`
  - Function similarity: 0.43
  - Dependencies: 2
  - Functions: 12/15 matched (target 17)
  - Missing functions: `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`
  - Types: 1/1 matched (target 2)
  - Missing types: _none_

- **layout.const_type_id** → `layout.ConstTypeId [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 2/5 matched (target 4)
  - Missing functions: `fmt`, `eq`, `hash`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: ConstTypeId.kt: snake_case identifier `const_type_id` in Kotlin comments; ConstTypeId.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **avalues.str_** → `avalues.Str [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 13/13 matched (target 15)
  - Missing functions: _none_
  - Types: 1/3 matched (target 1)
  - Missing types: `StarlarkValue`, `ExtraElem`
  - Scoring failure: Str.kt: snake_case identifier `alloc_str_init` in Kotlin comments; Str.kt: Rust-only type/unsafe terminology in Kotlin comments

- **runtime.rust_loc** → `runtime.RustLoc [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 1/3 matched (target 4)
  - Missing functions: `rust_loc_globals`, `invoke`
  - Types: 0/0 matched (target 1)
  - Missing types: _none_
  - Scoring failure: RustLoc.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **values.stack_guard** → `values.StackGuard [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 3/4 matched
  - Missing functions: `drop`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: StackGuard.kt: snake_case identifier `to_str` in Kotlin comments

- **collections.aligned_padded_str** → `alignedpaddedstr.AlignedPaddedStr`
  - Function similarity: 0.06
  - Dependencies: 2
  - Functions: 2/3 matched (target 4)
  - Missing functions: `eq`
  - Types: 1/1 matched
  - Missing types: _none_

- **runtime.file_loader** → `runtime.FileLoader`
  - Function similarity: 0.09
  - Dependencies: 2
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 3/3 matched
  - Missing types: _none_

- **profile.string_index** → `profile.StringIndex [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 2/2 matched
  - Missing types: _none_
  - Scoring failure: StringIndex.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **collections.string_pool** → `collections.StringPool [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: StringPool.kt: snake_case identifier `debug_assert` in Kotlin comments; StringPool.kt: Rust macro invocation in Kotlin comments; StringPool.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **def_inline.local_as_value** → `def_inline.LocalAsValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 1/1 matched (target 2)
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: LocalAsValue.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; LocalAsValue.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **values.thin_box_slice_frozen_value** → `values.ThinBoxSliceFrozenValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: ThinBoxSliceFrozenValue.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **heap.arena** → `heap.Arena [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 18/37 matched (target 23)
  - Missing functions: `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty`
  - Types: 4/7 matched (target 6)
  - Missing types: `ChunkIter`, `Item`, `ArenaUninit`
  - Scoring failure: Arena.kt: snake_case identifier `HAS_invoke` in Kotlin code; Arena.kt: snake_case identifier `non_drop` in Kotlin comments; Arena.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Arena.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arena.kt: Rust lifetime explanation in Kotlin comments

- **collections.alloca** → `collections.Alloca [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 5/23 matched (target 5)
  - Missing functions: `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `drop`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat`
  - Types: 1/4 matched (target 1)
  - Missing types: `Buffer`, `Align`, `DropSliceGuard`
  - Scoring failure: Alloca.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **stdlib** → `starlark.Stdlib [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 5/14 matched (target 16)
  - Missing functions: `global`, `nop`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
  - Types: 2/3 matched
  - Missing types: `Error`
  - Scoring failure: Stdlib.kt: snake_case identifier `call_stack` in Kotlin comments; StdlibTest.kt: snake_case identifier `HAS_equals` in Kotlin code

- **types.list_or_tuple** → `types.ListOrTuple [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 1/7 matched (target 5)
  - Missing functions: `default`, `starlark_type_repr`, `into_iter`, `into_iter`, `into_iter`, `test_unpack`
  - Types: 1/5 matched (target 1)
  - Missing types: `Canonical`, `Error`, `Item`, `IntoIter`
  - Scoring failure: ListOrTuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **layout.pointer** → `layout.Pointer [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 39/47 matched (target 46)
  - Missing functions: `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `unpack_pointer_i32_unchecked`, `test_int_tag`, `check`
  - Types: 5/5 matched
  - Missing types: _none_
  - Scoring failure: Pointer.kt: snake_case identifier `get_user_tag` in Kotlin comments; Pointer.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Pointer.kt: Rust lifetime explanation in Kotlin comments

- **stdlib.breakpoint** → `stdlib.Breakpoint`
  - Function similarity: 0.30
  - Dependencies: 1
  - Functions: 14/19 matched (target 18)
  - Missing functions: `global`, `breakpoint`, `reset_global_state`, `read_line`, `println`
  - Types: 5/6 matched (target 7)
  - Missing types: `Handler`
  - Lint issues: 1

- **types.any_complex** → `types.AnyComplex [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 5/8 matched (target 9)
  - Missing functions: `fmt`, `fmt`, `freeze`
  - Types: 3/5 matched (target 4)
  - Missing types: `Canonical`, `Frozen`
  - Scoring failure: AnyComplexTest.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **types.any_array** → `types.AnyArray [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 5/8 matched (target 9)
  - Missing functions: `fmt`, `drop`, `drop`
  - Types: 2/3 matched
  - Missing types: `Canonical`
  - Scoring failure: AnyArray.kt: score-padding suppression annotation `@Suppress` in Kotlin code; AnyArray.kt: translator-note comment (`Kotlin:`) in Kotlin comments; AnyArray.kt: Rust-only type/unsafe terminology in Kotlin comments

- **util.rtabort** → `util.Rtabort [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 3/6 matched
  - Missing functions: `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`
  - Types: 0/1 matched
  - Missing types: `AbortOnDrop`
  - Scoring failure: Rtabort.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **bc.if_debug** → `bc.IfDebug [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 5/8 matched (target 9)
  - Missing functions: `eq`, `partial_cmp`, `cmp`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: IfDebug.kt: snake_case identifier `debug_assertions` in Kotlin comments; IfDebug.kt: Rust attribute syntax in Kotlin comments; IfDebug.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **read_line** → `starlark.ReadLine [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 2/4 matched (target 2)
  - Missing functions: `new`, `read_line`
  - Types: 1/2 matched (target 1)
  - Missing types: `NoRustyline`
  - Scoring failure: ReadLine.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **util.non_static_type_id** → `util.NonStaticTypeId [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 1/3 matched (target 1)
  - Missing functions: `get_type_id`, `test_non_static_type_id`
  - Types: 0/1 matched (target 0)
  - Missing types: `NonStaticAny`
  - Scoring failure: NonStaticTypeId.kt: Rust lifetime explanation in Kotlin comments; NonStaticTypeId.kt: Rust-only type/unsafe terminology in Kotlin comments

- **runtime.cheap_call_stack** → `runtime.CheapCallStack [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 15/17 matched
  - Missing functions: `fmt`, `default`
  - Types: 3/3 matched (target 6)
  - Missing types: _none_
  - Scoring failure: CheapCallStack.kt: translator-note comment (`Kotlin:`) in Kotlin comments; CheapCallStack.kt: Rust lifetime explanation in Kotlin comments

- **avalues.simple** → `avalues.Simple [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 9/9 matched (target 11)
  - Missing functions: _none_
  - Types: 1/3 matched (target 1)
  - Missing types: `StarlarkValue`, `ExtraElem`
  - Scoring failure: Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments

- **layout.value_captured** → `layout.ValueCaptured`
  - Function similarity: 0.16
  - Dependencies: 1
  - Functions: 4/4 matched (target 9)
  - Missing functions: _none_
  - Types: 2/4 matched (target 2)
  - Missing types: `Canonical`, `Frozen`

- **record.field** → `record.Field [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 4/5 matched (target 10)
  - Missing functions: `fmt`
  - Types: 0/1 matched
  - Missing types: `FieldGen`
  - Scoring failure: Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments

- **string.dot_format** → `string.DotFormat`
  - Function similarity: 0.32
  - Dependencies: 1
  - Functions: 10/11 matched
  - Missing functions: `format_capture_for_test`
  - Types: 1/1 matched (target 2)
  - Missing types: _none_

- **typing.bindings** → `typing.Bindings [STUB]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 7/8 matched (target 18)
  - Missing functions: `get_for_clause`
  - Types: 3/3 matched (target 18)
  - Missing types: _none_
  - Scoring failure: target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments

- **typing.structs** → `typing.Structs`
  - Function similarity: 0.21
  - Dependencies: 1
  - Functions: 7/8 matched (target 9)
  - Missing functions: `fmt`
  - Types: 2/2 matched
  - Missing types: _none_

- **heap.fast_cell** → `heap.FastCell [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 6/7 matched
  - Missing functions: `drop`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust macro invocation in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments

- **structs.unordered_hasher** → `structs.UnorderedHasher [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 4/5 matched (target 4)
  - Missing functions: `_write`
  - Types: 1/1 matched (target 2)
  - Missing types: _none_
  - Scoring failure: UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments

- **typing.function** → `typing.Function [STUB]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 17/17 matched (target 24)
  - Missing functions: _none_
  - Types: 3/3 matched
  - Missing types: _none_
  - Scoring failure: target contains TODO/stub/placeholder markers in function bodies; Function.kt: Rust attribute syntax in Kotlin comments
  - TODOs: 1
  - Lint issues: 2

- **string.interpolation** → `string.Interpolation [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 12/12 matched (target 14)
  - Missing functions: _none_
  - Types: 4/4 matched (target 21)
  - Missing types: _none_
  - Scoring failure: Interpolation.kt: snake_case identifier `string_pool` in Kotlin comments

- **analysis.lint_message** → `analysis.LintMessage`
  - Function similarity: 0.34
  - Dependencies: 1
  - Functions: 1/1 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_

- **typing** → `starlark.Typing [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **types.int** → `types.Int [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Int.kt: snake_case identifier `num_bigint` in Kotlin comments

## Missing Files (by Dependents)

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `layout.heap` | `values.layout.heap.Heap` | 109 | 0 | 0 | 0 | `values/layout/heap.rs` | `values/layout/heap/Heap.kt` |
| 2 | `assert` | `assert.Assert` | 84 | 0 | 0 | 0 | `assert.rs` | `assert/Assert.kt` |
| 3 | `debug` | `debug.Debug` | 53 | 0 | 0 | 0 | `debug.rs` | `debug/Debug.kt` |
| 4 | `derive.unpack_value` | `tests.derive.UnpackValue` | 51 | 2 | 5 | 7 | `tests/derive/unpack_value.rs` | `tests/derive/UnpackValue.kt` |
| 5 | `coerce` | `Coerce` | 34 | 5 | 9 | 14 | `coerce.rs` | `Coerce.kt` |
| 6 | `util.arc_str` | `util.ArcStr` | 21 | 5 | 2 | 7 | `util/arc_str.rs` | `util/ArcStr.kt` |
| 7 | `__derive_refs.param_spec` | `deriverefs.ParamSpec` | 20 | 5 | 3 | 8 | `__derive_refs/param_spec.rs` | `deriverefs/ParamSpec.kt` |
| 8 | `derive.module` | `tests.derive.module.Module` | 20 | 0 | 0 | 0 | `tests/derive/module.rs` | `tests/derive/module/Module.kt` |
| 9 | `types.dict` | `values.types.dict.Dict` | 12 | 0 | 0 | 0 | `values/types/dict.rs` | `values/types/dict/Dict.kt` |
| 10 | `bc.expr` | `tests.bc.Expr` | 7 | 7 | 0 | 7 | `tests/bc/expr.rs` | `tests/bc/Expr.kt` |
| 11 | `types.range` | `values.types.range.Range` | 5 | 0 | 0 | 0 | `values/types/range.rs` | `values/types/range/Range.kt` |
| 12 | `tests.def` | `tests.Def` | 5 | 14 | 0 | 14 | `tests/def.rs` | `tests/Def.kt` |
| 13 | `types.namespace` | `values.types.namespace.Namespace` | 3 | 0 | 0 | 0 | `values/types/namespace.rs` | `values/types/namespace/Namespace.kt` |
| 14 | `types.num` | `values.types.num.Num` | 2 | 0 | 0 | 0 | `values/types/num.rs` | `values/types/num/Num.kt` |
| 15 | `types.list` | `values.types.list.List` | 2 | 0 | 0 | 0 | `values/types/list.rs` | `values/types/list/List.kt` |
| 16 | `types.float` | `values.types.float.Float` | 2 | 0 | 0 | 0 | `values/types/float.rs` | `values/types/float/Float.kt` |
| 17 | `tests` | `tests.Tests` | 1 | 0 | 0 | 0 | `tests.rs` | `tests/Tests.kt` |
| 18 | `types.string` | `values.types.string.String` | 1 | 0 | 0 | 0 | `values/types/string.rs` | `values/types/string/String.kt` |
| 19 | `types.set` | `values.types.set.Set` | 1 | 0 | 0 | 0 | `values/types/set.rs` | `values/types/set/Set.kt` |
| 20 | `types.none` | `values.types.none.None` | 1 | 0 | 0 | 0 | `values/types/none.rs` | `values/types/none/None.kt` |
| 21 | `types.enumeration` | `values.types.enumeration.Enumeration` | 1 | 0 | 0 | 0 | `values/types/enumeration.rs` | `values/types/enumeration/Enumeration.kt` |
| 22 | `tests.before_stmt` | `tests.BeforeStmt` | 1 | 1 | 0 | 1 | `tests/before_stmt.rs` | `tests/BeforeStmt.kt` |
| 23 | `__derive_refs` | `deriverefs.DeriveRefs` | 0 | 0 | 0 | 0 | `__derive_refs.rs` | `deriverefs/DeriveRefs.kt` |
| 24 | `type_compiled.tests` | `values.typing.typecompiled.Tests` | 0 | 13 | 0 | 13 | `values/typing/type_compiled/tests.rs` | `values/typing/typecompiled/Tests.kt` |
| 25 | `tests.basic` | `tests.Basic` | 0 | 12 | 0 | 12 | `tests/basic.rs` | `tests/Basic.kt` |
| 26 | `tests.bc` | `tests.bc.Bc` | 0 | 0 | 0 | 0 | `tests/bc.rs` | `tests/bc/Bc.kt` |
| 27 | `bc.and_or` | `tests.bc.AndOr` | 0 | 8 | 0 | 8 | `tests/bc/and_or.rs` | `tests/bc/AndOr.kt` |
| 28 | `tests.bc.call` | `tests.bc.Call` | 0 | 1 | 0 | 1 | `tests/bc/call.rs` | `tests/bc/Call.kt` |
| 29 | `bc.compr` | `tests.bc.Compr` | 0 | 4 | 0 | 4 | `tests/bc/compr.rs` | `tests/bc/Compr.kt` |
| 30 | `tests.bc.definitely_assigned` | `tests.bc.DefinitelyAssigned` | 0 | 5 | 0 | 5 | `tests/bc/definitely_assigned.rs` | `tests/bc/DefinitelyAssigned.kt` |
| 31 | `profile.tests` | `eval.runtime.profile.Tests` | 0 | 13 | 0 | 13 | `eval/runtime/profile/tests.rs` | `eval/runtime/profile/Tests.kt` |
| 32 | `bc.for_stmt` | `tests.bc.ForStmt` | 0 | 3 | 0 | 3 | `tests/bc/for_stmt.rs` | `tests/bc/ForStmt.kt` |
| 33 | `bc.golden` | `tests.bc.Golden` | 0 | 2 | 0 | 2 | `tests/bc/golden.rs` | `tests/bc/Golden.kt` |
| 34 | `bc.if_stmt` | `tests.bc.IfStmt` | 0 | 12 | 0 | 12 | `tests/bc/if_stmt.rs` | `tests/bc/IfStmt.kt` |
| 35 | `bc.isinstance` | `tests.bc.Isinstance` | 0 | 1 | 0 | 1 | `tests/bc/isinstance.rs` | `tests/bc/Isinstance.kt` |
| 36 | `runtime.profile` | `eval.runtime.profile.Profile` | 0 | 0 | 0 | 0 | `eval/runtime/profile.rs` | `eval/runtime/profile/Profile.kt` |
| 37 | `tests.call` | `tests.Call` | 0 | 20 | 0 | 20 | `tests/call.rs` | `tests/Call.kt` |
| 38 | `tests.comprehension` | `tests.Comprehension` | 0 | 10 | 0 | 10 | `tests/comprehension.rs` | `tests/Comprehension.kt` |
| 39 | `runtime.params` | `eval.runtime.params.Params` | 0 | 0 | 0 | 0 | `eval/runtime/params.rs` | `eval/runtime/params/Params.kt` |
| 40 | `tests.derive` | `tests.derive.Derive` | 0 | 0 | 0 | 0 | `tests/derive.rs` | `tests/derive/Derive.kt` |
| 41 | `derive.alloc_value` | `tests.derive.AllocValue` | 0 | 0 | 4 | 4 | `tests/derive/alloc_value.rs` | `tests/derive/AllocValue.kt` |
| 42 | `derive.attrs` | `tests.derive.Attrs` | 0 | 1 | 2 | 3 | `tests/derive/attrs.rs` | `tests/derive/Attrs.kt` |
| 43 | `derive.docs` | `tests.derive.Docs` | 0 | 7 | 2 | 9 | `tests/derive/docs.rs` | `tests/derive/Docs.kt` |
| 44 | `derive.freeze` | `tests.derive.freeze.Freeze` | 0 | 0 | 0 | 0 | `tests/derive/freeze.rs` | `tests/derive/freeze/Freeze.kt` |
| 45 | `freeze.basic` | `tests.derive.freeze.Basic` | 0 | 0 | 1 | 1 | `tests/derive/freeze/basic.rs` | `tests/derive/freeze/Basic.kt` |
| 46 | `freeze.bounds` | `tests.derive.freeze.Bounds` | 0 | 3 | 3 | 6 | `tests/derive/freeze/bounds.rs` | `tests/derive/freeze/Bounds.kt` |
| 47 | `freeze.enums` | `tests.derive.freeze.Enums` | 0 | 0 | 1 | 1 | `tests/derive/freeze/enums.rs` | `tests/derive/freeze/Enums.kt` |
| 48 | `freeze.identity` | `tests.derive.freeze.Identity` | 0 | 2 | 4 | 6 | `tests/derive/freeze/identity.rs` | `tests/derive/freeze/Identity.kt` |
| 49 | `freeze.validator` | `tests.derive.freeze.Validator` | 0 | 3 | 1 | 4 | `tests/derive/freeze/validator.rs` | `tests/derive/freeze/Validator.kt` |
| 50 | `freeze.validator_order` | `tests.derive.freeze.ValidatorOrder` | 0 | 3 | 3 | 6 | `tests/derive/freeze/validator_order.rs` | `tests/derive/freeze/ValidatorOrder.kt` |
| 51 | `scope.tests` | `eval.compiler.scope.Tests` | 0 | 16 | 1 | 17 | `eval/compiler/scope/tests.rs` | `eval/compiler/scope/Tests.kt` |
| 52 | `module.basic` | `tests.derive.module.Basic` | 0 | 9 | 0 | 9 | `tests/derive/module/basic.rs` | `tests/derive/module/Basic.kt` |
| 53 | `module.default_value` | `tests.derive.module.DefaultValue` | 0 | 3 | 0 | 3 | `tests/derive/module/default_value.rs` | `tests/derive/module/DefaultValue.kt` |
| 54 | `module.generic` | `tests.derive.module.Generic` | 0 | 8 | 2 | 10 | `tests/derive/module/generic.rs` | `tests/derive/module/Generic.kt` |
| 55 | `module.kwargs` | `tests.derive.module.Kwargs` | 0 | 4 | 0 | 4 | `tests/derive/module/kwargs.rs` | `tests/derive/module/Kwargs.kt` |
| 56 | `module.methods` | `tests.derive.module.Methods` | 0 | 5 | 1 | 6 | `tests/derive/module/methods.rs` | `tests/derive/module/Methods.kt` |
| 57 | `module.named_positional` | `tests.derive.module.NamedPositional` | 0 | 11 | 0 | 11 | `tests/derive/module/named_positional.rs` | `tests/derive/module/NamedPositional.kt` |
| 58 | `module.other_attributes` | `tests.derive.module.OtherAttributes` | 0 | 6 | 0 | 6 | `tests/derive/module/other_attributes.rs` | `tests/derive/module/OtherAttributes.kt` |
| 59 | `module.return_impl` | `tests.derive.module.ReturnImpl` | 0 | 4 | 0 | 4 | `tests/derive/module/return_impl.rs` | `tests/derive/module/ReturnImpl.kt` |
| 60 | `module.special_params` | `tests.derive.module.SpecialParams` | 0 | 3 | 0 | 3 | `tests/derive/module/special_params.rs` | `tests/derive/module/SpecialParams.kt` |
| 61 | `module.type_annotation` | `tests.derive.module.TypeAnnotation` | 0 | 3 | 1 | 4 | `tests/derive/module/type_annotation.rs` | `tests/derive/module/TypeAnnotation.kt` |
| 62 | `module.unpack_value` | `tests.derive.module.UnpackValue` | 0 | 13 | 0 | 13 | `tests/derive/module/unpack_value.rs` | `tests/derive/module/UnpackValue.kt` |
| 63 | `derive.trace` | `tests.derive.trace.Trace` | 0 | 0 | 0 | 0 | `tests/derive/trace.rs` | `tests/derive/trace/Trace.kt` |
| 64 | `trace.bounds` | `tests.derive.trace.Bounds` | 0 | 2 | 2 | 4 | `tests/derive/trace/bounds.rs` | `tests/derive/trace/Bounds.kt` |
| 65 | `trace.enums` | `tests.derive.trace.Enums` | 0 | 0 | 1 | 1 | `tests/derive/trace/enums.rs` | `tests/derive/trace/Enums.kt` |
| 66 | `trace.statics` | `tests.derive.trace.Statics` | 0 | 0 | 2 | 2 | `tests/derive/trace/statics.rs` | `tests/derive/trace/Statics.kt` |
| 67 | `bc.compiler` | `eval.bc.compiler.Compiler` | 0 | 0 | 0 | 0 | `eval/bc/compiler.rs` | `eval/bc/compiler/Compiler.kt` |
| 68 | `derive.unpack_value_attr` | `tests.derive.UnpackValueAttr` | 0 | 0 | 2 | 2 | `tests/derive/unpack_value_attr.rs` | `tests/derive/UnpackValueAttr.kt` |
| 69 | `tests.for_loop` | `tests.ForLoop` | 0 | 1 | 0 | 1 | `tests/for_loop.rs` | `tests/ForLoop.kt` |
| 70 | `tests.freeze_access_value` | `tests.FreezeAccessValue` | 0 | 2 | 2 | 4 | `tests/freeze_access_value.rs` | `tests/FreezeAccessValue.kt` |
| 71 | `tests.fstring` | `tests.Fstring` | 0 | 18 | 0 | 18 | `tests/fstring.rs` | `tests/Fstring.kt` |
| 72 | `tests.go` | `tests.Go` | 0 | 3 | 0 | 3 | `tests/go.rs` | `tests/Go.kt` |
| 73 | `tests.opt` | `tests.opt.Opt` | 0 | 8 | 0 | 8 | `tests/opt.rs` | `tests/opt/Opt.kt` |
| 74 | `opt.constant_folding` | `tests.opt.ConstantFolding` | 0 | 2 | 0 | 2 | `tests/opt/constant_folding.rs` | `tests/opt/ConstantFolding.kt` |
| 75 | `opt.def_inline` | `tests.opt.DefInline` | 0 | 8 | 0 | 8 | `tests/opt/def_inline.rs` | `tests/opt/DefInline.kt` |
| 76 | `opt.eq` | `tests.opt.Eq` | 0 | 7 | 0 | 7 | `tests/opt/eq.rs` | `tests/opt/Eq.kt` |
| 77 | `opt.if_rand` | `tests.opt.IfRand` | 0 | 27 | 3 | 30 | `tests/opt/if_rand.rs` | `tests/opt/IfRand.kt` |
| 78 | `opt.list_add` | `tests.opt.ListAdd` | 0 | 2 | 0 | 2 | `tests/opt/list_add.rs` | `tests/opt/ListAdd.kt` |
| 79 | `opt.speculative_exec` | `tests.opt.SpeculativeExec` | 0 | 3 | 0 | 3 | `tests/opt/speculative_exec.rs` | `tests/opt/SpeculativeExec.kt` |
| 80 | `opt.type_is` | `tests.opt.TypeIs` | 0 | 4 | 0 | 4 | `tests/opt/type_is.rs` | `tests/opt/TypeIs.kt` |
| 81 | `opt.types` | `tests.opt.Types` | 0 | 2 | 0 | 2 | `tests/opt/types.rs` | `tests/opt/Types.kt` |
| 82 | `tests.replace_binary` | `tests.ReplaceBinary` | 0 | 1 | 0 | 1 | `tests/replace_binary.rs` | `tests/ReplaceBinary.kt` |
| 83 | `tests.runtime` | `tests.Runtime` | 0 | 14 | 1 | 15 | `tests/runtime.rs` | `tests/Runtime.kt` |
| 84 | `tests.type_annot` | `tests.TypeAnnot` | 0 | 9 | 0 | 9 | `tests/type_annot.rs` | `tests/TypeAnnot.kt` |
| 85 | `tests.uncategorized` | `tests.Uncategorized` | 0 | 55 | 6 | 61 | `tests/uncategorized.rs` | `tests/Uncategorized.kt` |
| 86 | `tests.util` | `tests.Util` | 0 | 3 | 1 | 4 | `tests/util.rs` | `tests/Util.kt` |
| 87 | `typing.tests` | `typing.tests.Tests` | 0 | 28 | 2 | 30 | `typing/tests.rs` | `typing/tests/Tests.kt` |
| 88 | `typing.tests.call` | `typing.tests.Call` | 0 | 9 | 0 | 9 | `typing/tests/call.rs` | `typing/tests/Call.kt` |
| 89 | `tests.callable` | `typing.tests.Callable` | 0 | 2 | 0 | 2 | `typing/tests/callable.rs` | `typing/tests/Callable.kt` |
| 90 | `tests.list` | `typing.tests.List` | 0 | 6 | 0 | 6 | `typing/tests/list.rs` | `typing/tests/List.kt` |
| 91 | `tests.special_function` | `typing.tests.SpecialFunction` | 0 | 2 | 0 | 2 | `typing/tests/special_function.rs` | `typing/tests/SpecialFunction.kt` |
| 92 | `tests.tuple` | `typing.tests.Tuple` | 0 | 2 | 0 | 2 | `typing/tests/tuple.rs` | `typing/tests/Tuple.kt` |
| 93 | `tests.types` | `typing.tests.Types` | 0 | 3 | 0 | 3 | `typing/tests/types.rs` | `typing/tests/Types.kt` |
| 94 | `tests.rustdocs` | `docs.tests.Rustdocs` | 0 | 15 | 3 | 18 | `docs/tests/rustdocs.rs` | `docs/tests/Rustdocs.kt` |
| 95 | `tests.markdown` | `docs.tests.Markdown` | 0 | 29 | 2 | 31 | `docs/tests/markdown.rs` | `docs/tests/Markdown.kt` |
| 96 | `heap.allocator` | `values.layout.heap.allocator.Allocator` | 0 | 0 | 0 | 0 | `values/layout/heap/allocator.rs` | `values/layout/heap/allocator/Allocator.kt` |
| 97 | `allocator.alloc` | `values.layout.heap.allocator.alloc.Alloc` | 0 | 0 | 0 | 0 | `values/layout/heap/allocator/alloc.rs` | `values/layout/heap/allocator/alloc/Alloc.kt` |
| 98 | `heap.branding` | `values.layout.heap.Branding` | 0 | 0 | 0 | 0 | `values/layout/heap/branding.rs` | `values/layout/heap/Branding.kt` |
| 99 | `heap.profile` | `values.layout.heap.profile.Profile` | 0 | 0 | 0 | 0 | `values/layout/heap/profile.rs` | `values/layout/heap/profile/Profile.kt` |
| 100 | `docs.tests` | `docs.tests.Tests` | 0 | 0 | 0 | 0 | `docs/tests.rs` | `docs/tests/Tests.kt` |
| 101 | `adapter.tests` | `debug.adapter.Tests` | 0 | 23 | 3 | 26 | `debug/adapter/tests.rs` | `debug/adapter/Tests.kt` |
| 102 | `collections` | `collections.Collections` | 0 | 0 | 0 | 0 | `collections.rs` | `collections/Collections.kt` |
| 103 | `int.tests` | `values.types.int.Tests` | 0 | 6 | 0 | 6 | `values/types/int/tests.rs` | `values/types/int/Tests.kt` |
| 104 | `unused_loads.remove_tests` | `analysis.unusedloads.RemoveTests` | 0 | 4 | 0 | 4 | `analysis/unused_loads/remove_tests.rs` | `analysis/unusedloads/RemoveTests.kt` |
| 105 | `unused_loads.find_tests` | `analysis.unusedloads.FindTests` | 0 | 7 | 0 | 7 | `analysis/unused_loads/find_tests.rs` | `analysis/unusedloads/FindTests.kt` |
| 106 | `analysis.unused_loads` | `analysis.unusedloads.UnusedLoads` | 0 | 0 | 0 | 0 | `analysis/unused_loads.rs` | `analysis/unusedloads/UnusedLoads.kt` |
| 107 | `__derive_refs.sig` | `deriverefs.Sig` | 0 | 3 | 1 | 4 | `__derive_refs/sig.rs` | `deriverefs/Sig.kt` |
| 108 | `__derive_refs.parse_args` | `deriverefs.ParseArgs` | 0 | 8 | 0 | 8 | `__derive_refs/parse_args.rs` | `deriverefs/ParseArgs.kt` |
| 109 | `__derive_refs.invoke_macro_error` | `deriverefs.InvokeMacroError` | 0 | 2 | 1 | 3 | `__derive_refs/invoke_macro_error.rs` | `deriverefs/InvokeMacroError.kt` |
| 110 | `__derive_refs.components` | `deriverefs.Components` | 0 | 3 | 1 | 4 | `__derive_refs/components.rs` | `deriverefs/Components.kt` |
| 111 | `string.intern` | `values.types.string.intern.Intern` | 0 | 0 | 0 | 0 | `values/types/string/intern.rs` | `values/types/string/intern/Intern.kt` |
| 112 | `types.structs` | `values.types.structs.Structs` | 0 | 0 | 0 | 0 | `values/types/structs.rs` | `values/types/structs/Structs.kt` |
| 113 | `types.tuple` | `values.types.tuple.Tuple` | 0 | 0 | 0 | 0 | `values/types/tuple.rs` | `values/types/tuple/Tuple.kt` |
| 114 | `lib` | `Lib` | 0 | 0 | 0 | 0 | `lib.rs` | `Lib.kt` |

