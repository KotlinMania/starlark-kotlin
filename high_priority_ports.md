# High Priority Ports - Action Plan

## Files by Impact

Priority = deps * 1,000,000 + SymDeficit * 10,000 + SrcSymbols * 100 + (1 - function similarity) * 10

Dependency fanout is ranked first so the ladder favors ports that clear downstream compilation failures fastest.

This list is complete and includes function/type detail for every matched file. Function similarity is the required body/parameter comparison; file-level shape does not rescue a port.

| Rank | Source | Target | Function similarity | Deps | Functions | Missing functions | Types | Missing types | SymDeficit | SrcSymbols | Priority |
|------|--------|--------|------------|------|-----------|-------------------|-------|---------------|-----------|------------|----------|
| 1 | `layout.value` | `layout.Value` | 0.72 | 178 | 105/118 matched (target 158) | `fmt`, `eq`, `testing_new_int`, `display_for_type_error`, `_test_send_sync`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function` | 7/9 matched (target 10) | `Canonical`, `String` | 15 | 127 | 178162704.0 |
| 2 | `typing.ty` | `typing.Ty` | 0.75 | 109 | 49/50 matched (target 57) | `fmt` | 4/4 matched (target 6) | _none_ | 1 | 54 | 109015400.0 |
| 3 | `typing.starlark_value` | `typing.StarlarkValue [ZERO]` | 0.00 | 76 | 29/34 matched (target 43) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp` | 4/4 matched (target 5) | _none_ | 5 | 38 | 76053808.0 |
| 4 | `params.display` | `params.Display` | 0.75 | 76 | 4/4 matched | _none_ | 3/3 matched (target 8) | _none_ | 0 | 7 | 76000704.0 |
| 5 | `runtime.evaluator` | `runtime.Evaluator [ZERO]` | 0.00 | 56 | 58/60 matched (target 63) | `drop`, `add_diagnostics` | 7/7 matched (target 17) | _none_ | 2 | 67 | 56026712.0 |
| 6 | `values.trace` | `values.Trace [ZERO]` | 0.00 | 52 | 1/1 matched (target 43) | _none_ | 1/1 matched | _none_ | 0 | 2 | 52000208.0 |
| 7 | `values.freeze` | `values.Freeze [ZERO]` | 0.00 | 42 | 1/1 matched (target 31) | _none_ | 1/2 matched (target 6) | `Frozen` | 1 | 3 | 42010312.0 |
| 8 | `values.alloc_value` | `values.AllocValue [ZERO]` | 0.00 | 42 | 2/2 matched (target 5) | _none_ | 4/4 matched | _none_ | 0 | 6 | 42000608.0 |
| 9 | `layout.freezer` | `layout.Freezer [ZERO]` | 0.00 | 36 | 5/5 matched | _none_ | 1/1 matched | _none_ | 0 | 6 | 36000608.0 |
| 10 | `compiler.span` | `compiler.Span` | 0.92 | 29 | 2/2 matched | _none_ | 1/2 matched (target 1) | `Target` | 1 | 4 | 29010400.0 |
| 11 | `values.frozen_ref` | `values.FrozenRef [ZERO]` | 0.00 | 27 | 17/17 matched (target 23) | _none_ | 2/4 matched (target 2) | `Target`, `Frozen` | 2 | 21 | 27022110.0 |
| 12 | `none.none_type` | `none.NoneType [ZERO]` | 0.00 | 27 | 11/11 matched (target 16) | _none_ | 1/2 matched | `Error` | 1 | 13 | 27011310.0 |
| 13 | `runtime.frame_span` | `runtime.FrameSpan` | 0.65 | 26 | 3/4 matched | `fmt` | 1/1 matched | _none_ | 1 | 5 | 26010504.0 |
| 14 | `runtime.arguments` | `runtime.Arguments [ZERO]` | 0.00 | 25 | 26/30 matched (target 49) | `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names` | 8/8 matched (target 16) | _none_ | 4 | 38 | 25043810.0 |
| 15 | `typing.type_compiled` | `type_compiled.TypeCompiled [STUB]` | 0.00 | 22 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 22000010.0 |
| 16 | `environment.globals` | `environment.Globals [ZERO]` | 0.00 | 21 | 30/35 matched (target 38) | `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden` | 5/5 matched | _none_ | 5 | 40 | 21054010.0 |
| 17 | `derive.module` | `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 21 | 0/0 matched (target 21) | _none_ | 0/0 matched (target 3) | _none_ | 0 | 0 | 21000010.0 |
| 18 | `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 0.00 | 20 | 12/18 matched (target 17) | `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant` | 3/7 matched (target 4) | `Canonical`, `Frozen`, `Error`, `ReprNotSendSync` | 10 | 25 | 20102510.0 |
| 19 | `util.refcell` | `refcell.RefCell` | 0.32 | 20 | 1/2 matched (target 11) | `test_unleak_borrow` | 0/0 matched (target 3) | _none_ | 1 | 2 | 20010206.0 |
| 20 | `__derive_refs.param_spec` | `deriverefs.ParamSpec [PROVENANCE-FALLBACK]` | 0.83 | 20 | 5/5 matched | _none_ | 3/3 matched (target 5) | _none_ | 0 | 8 | 20000802.0 |
| 21 | `environment.methods` | `environment.Methods [ZERO]` | 0.00 | 17 | 17/19 matched (target 21) | `test_set_attribute`, `get_methods` | 3/4 matched (target 3) | `Magic` | 3 | 23 | 17032310.0 |
| 22 | `values.iter` | `values.Iter [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 17 | 4/5 matched (target 84) | `drop` | 1/2 matched (target 14) | `Item` | 2 | 7 | 17020710.0 |
| 23 | `values.error` | `values.Error` | 0.62 | 17 | 4/5 matched | `from` | 2/2 matched (target 20) | _none_ | 1 | 7 | 17010704.0 |
| 24 | `private` | `starlark.Private [ZERO]` | 0.00 | 15 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 15000110.0 |
| 25 | `collections.symbol` | `collections.Symbol [STUB]` | 0.00 | 15 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 15000010.0 |
| 26 | `layout.avalue` | `layout.AValue [ZERO]` | 0.00 | 14 | 6/8 matched (target 11) | `tuple_cycle_freeze`, `test_try_freeze_directly` | 3/3 matched | _none_ | 2 | 11 | 14021110.0 |
| 27 | `layout.const_frozen_string` | `layout.ConstFrozenString [ZERO]` | 0.00 | 12 | 0/2 matched (target 1) | `test_const_frozen_string_for_short_strings`, `test_const_frozen_string` | 0/0 matched | _none_ | 2 | 2 | 12020210.0 |
| 28 | `typing.tuple` | `typing.Tuple [ZERO]` | 0.00 | 12 | 5/6 matched (target 9) | `fmt` | 1/1 matched (target 3) | _none_ | 1 | 7 | 12010710.0 |
| 29 | `layout.value_lifetimeless` | `layout.ValueLifetimeless` | 1.00 | 12 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 12000100.0 |
| 30 | `int.inline_int` | `int.InlineInt [ZERO]` | 0.00 | 11 | 25/34 matched (target 43) | `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits` | 2/5 matched (target 6) | `Error`, `Output`, `Canonical` | 12 | 39 | 11123910.0 |
| 31 | `int.pointer_i32` | `int.PointerI32 [ZERO]` | 0.00 | 9 | 28/31 matched (target 34) | `eq`, `fmt`, `serialize` | 1/2 matched | `Canonical` | 4 | 33 | 9043310.0 |
| 32 | `types.type_instance_id` | `types.TypeInstanceId` | 0.00 | 9 | 0/1 matched (target 2) | `r#gen` | 1/1 matched | _none_ | 1 | 2 | 9010210.0 |
| 33 | `any` | `starlark.Any` | 0.04 | 8 | 2/12 matched (target 3) | `static_type_id`, `static_type_of`, `is`, `test_can_convert`, `convert_value`, `convert_any`, `test_any_lifetime`, `test`, `test_provides_static_type_id`, `test_provides_static_type_when_type_parameter_has_bound_with_lifetime` | 3/15 matched (target 37) | `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar` | 22 | 27 | 8222709.5 |
| 34 | `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 0.00 | 8 | 6/13 matched (target 15) | `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub` | 1/2 matched (target 1) | `Output` | 8 | 15 | 8081510.0 |
| 35 | `eval.compiler` | `eval.Compiler [ZERO]` | 0.00 | 8 | 6/6 matched | _none_ | 1/1 matched | _none_ | 0 | 7 | 8000710.0 |
| 36 | `cast` | `starlark.Cast [ZERO]` | 0.00 | 8 | 3/3 matched (target 4) | _none_ | 0/0 matched | _none_ | 0 | 3 | 8000310.0 |
| 37 | `types.bigint` | `types.Bigint [ZERO]` | 0.00 | 7 | 29/73 matched (target 35) | `unpack_integer`, `eq`, `test_parse`, `test_str`, `test_repr`, `test_equals`, `test_plus`, `test_compare_big_big`, `test_compare_big_small`, `test_compare_big_float`, `test_add_big`, `test_add_big_small`, `test_add_big_float`, `test_mul_big`, `test_mul_big_small`, `test_mul_big_float`, `test_div_big`, `test_div_big_small`, `test_div_big_float`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_big_float`, `test_percent_big`, `test_percent_big_small`, `test_percent_big_float`, `test_bit_and_big`, `test_bit_and_big_small`, `test_bit_and_float`, `test_bit_or_big`, `test_bit_or_big_small`, `test_bit_or_float`, `test_bit_xor_big`, `test_bit_xor_big_small`, `test_bit_xor_float`, `test_bit_not`, `test_left_shift`, `test_left_shift_small`, `test_left_shift_float`, `test_right_shift`, `test_right_shift_small`, `test_right_shift_float`, `test_int_function`, `test_hash`, `test_int_type_matches_bigint` | 1/1 matched | _none_ | 44 | 74 | 7447410.0 |
| 38 | `runtime.frozen_file_span` | `runtime.FrozenFileSpan [ZERO]` | 0.00 | 7 | 9/10 matched | `fmt` | 1/1 matched | _none_ | 1 | 11 | 7011110.0 |
| 39 | `values.starlark_type_id` | `values.StarlarkTypeId` | 0.61 | 7 | 5/6 matched (target 7) | `eq` | 2/2 matched | _none_ | 1 | 8 | 7010804.0 |
| 40 | `compiler.opt_ctx` | `compiler.OptCtx` | 0.71 | 7 | 5/5 matched (target 13) | _none_ | 2/2 matched (target 4) | _none_ | 0 | 7 | 7000703.0 |
| 41 | `type_compiled.type_matcher_factory` | `type_compiled.TypeMatcherFactory` | 0.69 | 7 | 3/3 matched (target 6) | _none_ | 3/3 matched | _none_ | 0 | 6 | 7000603.0 |
| 42 | `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 0.00 | 6 | 4/7 matched (target 9) | `from_millis`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 4 | 9 | 6040910.0 |
| 43 | `typing.typecheck` | `typing.Typecheck [STUB]` | 0.00 | 6 | 2/5 matched | `fmt`, `find_bindings_by_name`, `find_first_binding` | 2/2 matched (target 3) | _none_ | 3 | 7 | 6030710.0 |
| 44 | `dict.dict_type` | `dict.DictType [ZERO]` | 0.00 | 6 | 1/2 matched (target 4) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 5 | 6030510.0 |
| 45 | `none.none_or` | `none.NoneOr` | 0.73 | 6 | 7/7 matched (target 9) | _none_ | 1/3 matched (target 4) | `Canonical`, `Error` | 2 | 10 | 6021002.5 |
| 46 | `values.freeze_error` | `values.FreezeError [ZERO]` | 0.00 | 6 | 3/4 matched (target 6) | `from` | 3/4 matched (target 3) | `FreezeResult` | 2 | 8 | 6020810.0 |
| 47 | `layout.value_alloc_size` | `layout.ValueAllocSize [ZERO]` | 0.00 | 6 | 4/5 matched | `layout` | 1/1 matched | _none_ | 1 | 6 | 6010610.0 |
| 48 | `compiler.stmt` | `compiler.Stmt [ZERO]` | 0.00 | 6 | 25/25 matched (target 28) | _none_ | 7/7 matched (target 24) | _none_ | 0 | 32 | 6003210.0 |
| 49 | `profile.profiler_type` | `profile.ProfilerType` | 0.69 | 6 | 1/1 matched | _none_ | 2/2 matched | _none_ | 0 | 3 | 6000303.0 |
| 50 | `values.layout` | `values.Layout [STUB]` | 0.00 | 6 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 6000010.0 |
| 51 | `tests.def` | `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 5 | 0/14 matched (target 4) | `test_lambda`, `test_frozen_lambda`, `test_nested_def_1`, `test_nested_def_2`, `test_nested_def_3`, `test_lambda_capture_from_module`, `test_lambda_capture_from_def`, `test_lambda_capture_reassigned_from_def`, `test_def_freeze`, `test_frozen_lambda_nest`, `test_context_captured`, `test_lambda_errors`, `test_lambda_errors_nested`, `test_double_capture_and_freeze` | 0/0 matched (target 1) | _none_ | 14 | 14 | 5141410.0 |
| 52 | `types.array` | `types.Array [ZERO]` | 0.00 | 5 | 23/32 matched (target 24) | `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`, `debug`, `display` | 2/2 matched | _none_ | 9 | 34 | 5093410.0 |
| 53 | `typing.arc_ty` | `typing.ArcTy` | 0.60 | 5 | 6/7 matched (target 16) | `fmt` | 3/4 matched (target 10) | `Target` | 2 | 11 | 5021104.0 |
| 54 | `typing.interface` | `typing.Interface` | 0.60 | 5 | 3/3 matched | _none_ | 1/1 matched | _none_ | 0 | 4 | 5000404.0 |
| 55 | `scope.scope_resolver_globals` | `scope.ScopeResolverGlobals` | 0.72 | 5 | 3/3 matched | _none_ | 1/1 matched | _none_ | 0 | 4 | 5000403.0 |
| 56 | `eval.bc` | `bc.Bc [STUB]` | 0.00 | 5 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 5000010.0 |
| 57 | `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 0.00 | 4 | 21/36 matched (target 24) | `fmt`, `r#type`, `values`, `test_enum_type_as_type_pass`, `test_enum_type_fail_runtime`, `test_enum_type_fail_compile_time`, `test_enum_is_callable`, `test_enum_value_index`, `test_enum_value_index_correct_type`, `test_enum_index`, `test_enum_index_fail`, `test_enum_call`, `test_enum_attribute_access`, `test_enum_attribute_access_invalid`, `test_enum_attribute_access_type` | 4/8 matched (target 6) | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` | 19 | 44 | 4194410.0 |
| 58 | `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 0.00 | 4 | 6/13 matched (target 8) | `fmt`, `new`, `compiler_args_globals`, `compiler_args`, `test_pass`, `test_fail_compile_time`, `test_fail_runtime` | 2/4 matched (target 2) | `Canonical`, `CompilerArgs` | 9 | 17 | 4091710.0 |
| 59 | `bc.frame` | `bc.Frame [ZERO]` | 0.00 | 4 | 16/24 matched (target 31) | `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit` | 2/2 matched | _none_ | 8 | 26 | 4082610.0 |
| 60 | `values.demand` | `values.Demand` | 0.37 | 4 | 4/7 matched (target 5) | `payload`, `provide`, `test_trait_downcast` | 1/4 matched (target 1) | `SomeTrait`, `StaticType`, `MyValue` | 6 | 11 | 4061106.2 |
| 61 | `values.value_of` | `values.ValueOf [ZERO]` | 0.00 | 4 | 4/6 matched (target 5) | `deref`, `fmt` | 1/4 matched (target 1) | `Target`, `Canonical`, `Error` | 5 | 10 | 4051010.0 |
| 62 | `profile.alloc_counts` | `profile.AllocCounts` | 0.40 | 4 | 1/4 matched (target 5) | `normalize_for_golden_tests`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 4 | 6 | 4040606.0 |
| 63 | `bc.native_function` | `bc.NativeFunction` | 0.51 | 4 | 3/4 matched | `fun` | 1/1 matched | _none_ | 1 | 5 | 4010505.0 |
| 64 | `types.ellipsis` | `types.Ellipsis` | 0.55 | 4 | 2/3 matched (target 4) | `test_ellipsis` | 1/1 matched | _none_ | 1 | 4 | 4010404.5 |
| 65 | `record.record_type` | `record.RecordType [ZERO]` | 0.00 | 3 | 15/22 matched (target 18) | `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error` | 2/8 matched (target 2) | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` | 13 | 30 | 3133010.0 |
| 66 | `alloc.chunk` | `alloc.Chunk [ZERO]` | 0.00 | 3 | 11/19 matched (target 18) | `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`, `test_alloc_release` | 2/3 matched (target 2) | `ChunkDataEmpty` | 9 | 22 | 3092210.0 |
| 67 | `stdlib.call_stack` | `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 3 | 7/13 matched (target 14) | `fmt`, `global`, `test_simple`, `test_strip_one`, `test_strip_all`, `test_call_stack_frame` | 1/1 matched (target 2) | _none_ | 6 | 14 | 3061410.0 |
| 68 | `errors.did_you_mean` | `errors.DidYouMean [ZERO]` | 0.00 | 3 | 1/6 matched (target 2) | `prefixes`, `typos`, `best`, `very_short`, `earlier_variants_are_more_important` | 0/0 matched | _none_ | 5 | 6 | 3050610.0 |
| 69 | `list.alloc` | `list.Alloc [ZERO]` | 0.00 | 3 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | 4 | 5 | 3040510.0 |
| 70 | `list.list_type` | `list.ListType` | 0.37 | 3 | 1/2 matched (target 5) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 5 | 3030506.2 |
| 71 | `profile.instant` | `profile.Instant [ZERO]` | 0.00 | 3 | 3/4 matched (target 9) | `sub` | 1/2 matched (target 1) | `Output` | 2 | 6 | 3020610.0 |
| 72 | `compiler.constants` | `compiler.Constants [ZERO]` | 0.00 | 3 | 1/3 matched (target 5) | `eq`, `test_constants` | 2/2 matched | _none_ | 2 | 5 | 3020510.0 |
| 73 | `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 0.00 | 3 | 2/2 matched | _none_ | 1/3 matched (target 1) | `Canonical`, `Error` | 2 | 5 | 3020510.0 |
| 74 | `sealed` | `starlark.Sealed [ZERO]` | 0.00 | 3 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 3000110.0 |
| 75 | `types.record` | `types.Record [ZERO]` | 0.00 | 3 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 3000010.0 |
| 76 | `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 0.00 | 2 | 4/11 matched (target 9) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter` | 1/4 matched (target 3) | `Target`, `Item`, `IntoIter` | 10 | 15 | 2101510.0 |
| 77 | `util.arc_or_static` | `util.ArcOrStatic` | 0.42 | 2 | 5/10 matched (target 9) | `fmt`, `eq`, `partial_cmp`, `cmp`, `hash` | 2/3 matched (target 4) | `Target` | 6 | 13 | 2061305.9 |
| 78 | `typing.type_type` | `typing.TypeType` | 0.27 | 2 | 2/5 matched (target 3) | `test`, `module`, `takes_type` | 1/3 matched (target 1) | `Canonical`, `Error` | 5 | 8 | 2050807.2 |
| 79 | `alloc.chunk_part` | `alloc.ChunkPart` | 0.75 | 2 | 11/15 matched (target 16) | `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`, `test_is_full` | 1/1 matched | _none_ | 4 | 16 | 2041602.5 |
| 80 | `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 0.00 | 2 | 10/12 matched (target 19) | `fmt`, `deref` | 2/3 matched (target 2) | `Target` | 3 | 15 | 2031510.0 |
| 81 | `layout.const_type_id` | `layout.ConstTypeId [ZERO]` | 0.00 | 2 | 2/5 matched (target 4) | `fmt`, `eq`, `hash` | 1/1 matched | _none_ | 3 | 6 | 2030610.0 |
| 82 | `runtime.rust_loc` | `runtime.RustLoc [ZERO]` | 0.00 | 2 | 0/3 matched (target 1) | `rust_loc_globals`, `invoke`, `test_rust_loc` | 0/0 matched | _none_ | 3 | 3 | 2030310.0 |
| 83 | `avalues.str_` | `avalues.Str [ZERO]` | 0.00 | 2 | 11/11 matched (target 15) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | 2 | 14 | 2021410.0 |
| 84 | `values.stack_guard` | `values.StackGuard [ZERO]` | 0.00 | 2 | 3/4 matched | `drop` | 1/1 matched | _none_ | 1 | 5 | 2010510.0 |
| 85 | `collections.aligned_padded_str` | `alignedpaddedstr.AlignedPaddedStr` | 0.34 | 2 | 2/3 matched (target 4) | `eq` | 1/1 matched | _none_ | 1 | 4 | 2010406.6 |
| 86 | `profile.string_index` | `profile.StringIndex [ZERO]` | 0.00 | 2 | 2/2 matched | _none_ | 2/2 matched | _none_ | 0 | 4 | 2000410.0 |
| 87 | `runtime.file_loader` | `runtime.FileLoader` | 0.70 | 2 | 1/1 matched (target 2) | _none_ | 3/3 matched | _none_ | 0 | 4 | 2000403.0 |
| 88 | `collections.string_pool` | `collections.StringPool [ZERO]` | 0.00 | 2 | 2/2 matched | _none_ | 1/1 matched | _none_ | 0 | 3 | 2000310.0 |
| 89 | `def_inline.local_as_value` | `def_inline.LocalAsValue [ZERO]` | 0.00 | 2 | 1/1 matched (target 2) | _none_ | 1/1 matched | _none_ | 0 | 2 | 2000210.0 |
| 90 | `hint` | `starlark.Hint [PROVENANCE-FALLBACK]` | 0.95 | 2 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | 0 | 2 | 2000200.5 |
| 91 | `values.thin_box_slice_frozen_value` | `values.ThinBoxSliceFrozenValue [STUB]` | 0.00 | 2 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 2000010.0 |
| 92 | `heap.arena` | `heap.Arena [ZERO]` | 0.00 | 1 | 18/37 matched (target 23) | `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty` | 4/7 matched (target 6) | `ChunkIter`, `Item`, `ArenaUninit` | 22 | 44 | 1224410.0 |
| 93 | `collections.alloca` | `collections.Alloca [ZERO]` | 0.00 | 1 | 5/22 matched (target 5) | `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat` | 1/4 matched (target 1) | `Buffer`, `Align`, `DropSliceGuard` | 20 | 26 | 1202610.0 |
| 94 | `stdlib` | `starlark.Stdlib [ZERO]` | 0.00 | 1 | 3/14 matched (target 3) | `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2` | 1/3 matched (target 1) | `Bool2`, `Error` | 13 | 17 | 1131710.0 |
| 95 | `string.interpolation` | `string.Interpolation [ZERO]` | 0.00 | 1 | 4/12 matched (target 6) | `test_incomplete_format`, `test_unsupported_format_character`, `test_parse_percent_s_one`, `test_type_support_d`, `test_type_support_o`, `test_type_support_x`, `test_type_support_e`, `test_int_min` | 4/4 matched (target 20) | _none_ | 8 | 16 | 1081610.0 |
| 96 | `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 0.00 | 1 | 1/5 matched | `default`, `starlark_type_repr`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 8 | 10 | 1081010.0 |
| 97 | `layout.pointer` | `layout.Pointer [ZERO]` | 0.00 | 1 | 25/32 matched (target 46) | `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `test_int_tag`, `check` | 5/5 matched | _none_ | 7 | 37 | 1073710.0 |
| 98 | `stdlib.breakpoint` | `stdlib.Breakpoint` | 0.45 | 1 | 11/17 matched (target 13) | `global`, `breakpoint`, `reset_global_state`, `test_breakpoint_real`, `test_breakpoint_mock`, `test_breakpoint_disabled` | 5/6 matched | `Handler` | 7 | 23 | 1072305.5 |
| 99 | `types.any_complex` | `types.AnyComplex` | 0.49 | 1 | 4/7 matched | `fmt`, `test_any_complex`, `freeze` | 1/5 matched (target 1) | `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData` | 7 | 12 | 1071205.1 |
| 100 | `types.any_array` | `types.AnyArray [ZERO]` | 0.00 | 1 | 3/7 matched | `fmt`, `drop`, `test_drop`, `test_allocation_size` | 1/3 matched (target 1) | `Canonical`, `IncrementOnDrop` | 6 | 10 | 1061010.0 |
| 101 | `util.rtabort` | `util.Rtabort [ZERO]` | 0.00 | 1 | 2/6 matched (target 3) | `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort` | 0/1 matched (target 0) | `AbortOnDrop` | 5 | 7 | 1050710.0 |
| 102 | `string.dot_format` | `string.DotFormat` | 0.43 | 1 | 7/11 matched (target 7) | `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one` | 1/1 matched | _none_ | 4 | 12 | 1041205.7 |
| 103 | `bc.if_debug` | `bc.IfDebug [ZERO]` | 0.00 | 1 | 5/8 matched (target 9) | `eq`, `partial_cmp`, `cmp` | 1/1 matched | _none_ | 3 | 9 | 1030910.0 |
| 104 | `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 0.00 | 1 | 1/3 matched (target 1) | `get_type_id`, `test_non_static_type_id` | 0/1 matched (target 0) | `NonStaticAny` | 3 | 4 | 1030410.0 |
| 105 | `runtime.cheap_call_stack` | `runtime.CheapCallStack [ZERO]` | 0.00 | 1 | 15/17 matched | `fmt`, `default` | 3/3 matched (target 6) | _none_ | 2 | 20 | 1022010.0 |
| 106 | `avalues.simple` | `avalues.Simple [ZERO]` | 0.00 | 1 | 8/8 matched (target 11) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | 2 | 11 | 1021110.0 |
| 107 | `layout.value_captured` | `layout.ValueCaptured` | 0.78 | 1 | 4/4 matched (target 9) | _none_ | 2/4 matched (target 2) | `Canonical`, `Frozen` | 2 | 8 | 1020802.2 |
| 108 | `record.field` | `record.Field [ZERO]` | 0.00 | 1 | 4/5 matched (target 10) | `fmt` | 0/1 matched | `FieldGen` | 2 | 6 | 1020610.0 |
| 109 | `structs.unordered_hasher` | `structs.UnorderedHasher [ZERO]` | 0.00 | 1 | 3/5 matched (target 3) | `_write`, `test_unordered_hasher` | 1/1 matched | _none_ | 2 | 6 | 1020610.0 |
| 110 | `typing.bindings` | `typing.Bindings [STUB]` | 0.00 | 1 | 7/8 matched (target 18) | `get_for_clause` | 3/3 matched (target 18) | _none_ | 1 | 11 | 1011110.0 |
| 111 | `typing.structs` | `typing.Structs` | 0.63 | 1 | 7/8 matched (target 9) | `fmt` | 2/2 matched | _none_ | 1 | 10 | 1011003.7 |
| 112 | `heap.fast_cell` | `heap.FastCell [ZERO]` | 0.00 | 1 | 6/7 matched | `drop` | 1/1 matched | _none_ | 1 | 8 | 1010810.0 |
| 113 | `read_line` | `starlark.ReadLine [ZERO]` | 0.00 | 1 | 2/2 matched | _none_ | 1/2 matched (target 1) | `NoRustyline` | 1 | 4 | 1010410.0 |
| 114 | `typing.function` | `typing.Function [STUB]` | 0.00 | 1 | 12/12 matched (target 24) | _none_ | 3/3 matched | _none_ | 0 | 15 | 1001510.0 |
| 115 | `analysis.lint_message` | `analysis.LintMessage` | 0.75 | 1 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 1000202.5 |
| 116 | `typing` | `starlark.Typing [STUB]` | 0.00 | 1 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000010.0 |
| 117 | `types.int` | `types.Int [ZERO]` | 0.00 | 1 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000010.0 |
| 118 | `types.bool` | `types.Bool` | 1.00 | 1 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 1000000.0 |
| 119 | `bc.instr_impl` | `bc.InstrImpl` | 0.83 | 0 | 7/7 matched (target 95) | _none_ | 87/163 matched (target 104) | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` | 76 | 170 | 777001.7 |
| 120 | `set.methods` | `set.Methods [STUB]` | 0.00 | 0 | 18/68 matched (target 19) | `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self` | 1/1 matched (target 3) | _none_ | 50 | 69 | 506910.0 |
| 121 | `string.str_type` | `string.StrType [ZERO]` | 0.00 | 0 | 2/47 matched (target 25) | `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_string_corruption`, `test_escape_characters`, `test_string_hash`, `test_zero_length_string_hash_is_not_zero`, `test_string_len`, `test_arithmetic_on_string`, `test_slice_string`, `test_string_is_in`, `test_successive_add`, `test_string_index`, `test_str` | 0/4 matched (target 0) | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` | 49 | 51 | 495110.0 |
| 122 | `int.int_or_big` | `int.IntOrBig [STUB]` | 0.00 | 0 | 24/46 matched (target 57) | `starlark_type_repr`, `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_small_big`, `test_floor_div_small`, `test_percent_big`, `test_percent_big_small`, `test_percent_small_big`, `test_percent_small` | 3/7 matched (target 11) | `Canonical`, `Err`, `Error`, `Output` | 26 | 53 | 265310.0 |
| 123 | `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 0.00 | 0 | 6/29 matched (target 11) | `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`, `test_empty`, `test_from_iter_sized`, `test_from_iter_unknown_size`, `test_stress` | 1/3 matched (target 1) | `ThinBoxSliceLayout`, `Target` | 25 | 32 | 253210.0 |
| 124 | `set.value` | `set.Value [ZERO]` | 0.00 | 0 | 30/50 matched (target 47) | `fmt`, `test_bit_or`, `test_bit_or_lhs_empty`, `test_bit_or_rhs_empty`, `test_bit_or_fail_iter`, `test_bit_or_ord`, `test_bit_and`, `test_bit_and_lhs_empty`, `test_bit_and_rhs_empty`, `test_bit_and_ord`, `test_bit_and_fail_iter`, `test_bit_xor`, `test_bit_xor_ord`, `test_bit_xor_lhs_empty`, `test_bit_xor_rhs_empty`, `test_bit_xor_fail_iter`, `test_sub`, `test_sub_empty_lhs`, `test_sub_empty_rhs`, `test_sub_fail_iter` | 6/9 matched (target 6) | `Canonical`, `Frozen`, `ContentRef` | 23 | 59 | 235910.0 |
| 125 | `values.typing.callable` | `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 0.00 | 0 | 12/32 matched (target 31) | `clone`, `fmt`, `trace`, `_assert_sync_send`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_pass`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `test_callable_checked_runtime`, `module`, `good`, `bad` | 5/8 matched (target 6) | `Canonical`, `Error`, `Frozen` | 23 | 40 | 234010.0 |
| 126 | `typing.user` | `typing.User` | 0.32 | 0 | 13/27 matched (target 26) | `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`, `test_intersect_with_abstract_type`, `test_ty_user_intersects_with_base_starlark_value` | 5/8 matched | `AbstractPlant`, `FruitCallable`, `Fruit` | 17 | 35 | 173506.8 |
| 127 | `analysis.names` | `analysis.Names` | 0.48 | 0 | 21/35 matched (target 31) | `new`, `ident`, `assign_ident`, `lint`, `about`, `test_lint_unused`, `test_lint_duplicate_assign`, `test_lint_unassigned`, `test_lint_undefined`, `test_early_fail`, `test_assign_for_next`, `test_flow_control`, `test_lambda_capture`, `test_global_defined_later` | 7/8 matched (target 13) | `AstStrExt` | 15 | 43 | 154305.2 |
| 128 | `float.float` | `float.Float [ZERO]` | 0.00 | 0 | 26/39 matched (target 33) | `fmt`, `non_finite`, `test_write_non_finite`, `decimal`, `test_write_decimal`, `scientific`, `test_write_scientific`, `compact`, `test_write_compact`, `test_arithmetic_operators`, `test_dictionary_key`, `test_comparisons`, `test_comparisons_by_sorting` | 1/3 matched (target 1) | `Canonical`, `Error` | 15 | 42 | 154210.0 |
| 129 | `layout.typed` | `layout.ValueTyped [ZERO]` | 0.00 | 0 | 21/31 matched (target 44) | `fmt`, `serialize`, `eq`, `deref`, `unpack_value_impl`, `int`, `test_unpack_value_for_frozen_value_typed`, `module`, `mutable`, `takes_frozen_value_typed` | 2/7 matched (target 2) | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` | 15 | 38 | 153810.0 |
| 130 | `scope.payload` | `scope.Payload [ZERO]` | 0.00 | 0 | 0/7 matched (target 0) | `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id` | 9/17 matched (target 14) | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` | 15 | 24 | 152410.0 |
| 131 | `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 0.00 | 0 | 4/18 matched (target 8) | `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `default`, `fmt`, `eq`, `across_lengths`, `test_strings`, `test_ints`, `test_mixed_types`, `test_default`, `test_empty` | 2/3 matched (target 2) | `Target` | 15 | 21 | 152110.0 |
| 132 | `string.repr` | `string.Repr [ZERO]` | 0.00 | 0 | 9/22 matched (target 11) | `or4`, `push_vec_tail`, `test_to_repr`, `test_string_repr`, `test`, `test_to_repr_long_smoke`, `string_repr_for_test`, `to_repr_sse`, `to_repr_no_escape_all_lengths`, `to_repr_tail_escape_all_lengths`, `to_repr_middle_escape_all_lengths`, `test_chunk_non_ascii_or_need_escape`, `load` | 1/1 matched | _none_ | 13 | 23 | 132310.0 |
| 133 | `list.value` | `list.Value [ZERO]` | 0.00 | 0 | 46/56 matched (target 96) | `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare` | 6/8 matched (target 9) | `List`, `Canonical` | 12 | 64 | 126410.0 |
| 134 | `dict.value` | `dict.Value [ZERO]` | 0.00 | 0 | 43/52 matched (target 79) | `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `serialize`, `test_mutate_dict`, `test_get_str`, `test_repr_cycle` | 7/10 matched | `Canonical`, `Frozen`, `ContentRef` | 12 | 62 | 126210.0 |
| 135 | `num.value` | `num.Value` | 0.32 | 0 | 11/22 matched (target 27) | `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul`, `test_from_value`, `test_conversion_to_float`, `test_conversion_to_int`, `test_hashing`, `test_eq` | 3/4 matched (target 6) | `Output` | 12 | 26 | 122606.8 |
| 136 | `stdlib.extra` | `stdlib.Extra` | 0.14 | 0 | 5/16 matched (target 21) | `fmt`, `print`, `pprint`, `pstr`, `prepr`, `test_filter`, `test_map`, `test_debug`, `test_print`, `test_pstr`, `test_prepr` | 3/4 matched (target 3) | `PrintHandlerImpl` | 12 | 20 | 122008.6 |
| 137 | `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 0.00 | 0 | 3/13 matched (target 6) | `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_lookup_nonexistent_type`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered` | 2/4 matched (target 3) | `TestSimpleType`, `TestComplexGen` | 12 | 17 | 121710.0 |
| 138 | `analysis` | `starlark.Analysis` | 0.05 | 0 | 1/12 matched (target 1) | `module`, `test_lint_suppressions_keyword_matching`, `test_lint_suppressions_fn_with_many_issues`, `test_lint_suppressions_preceding_whitespace`, `test_lint_suppressions_with_space_separator`, `test_lint_suppressions_multiline_span`, `test_lint_suppressions_small_span`, `test_lint_suppressions_data`, `test_lint_suppressions_line_before`, `test_lint_suppressions_line_before_windows_newlines`, `test_lint_suppressions_inside_fn` | 1/1 matched | _none_ | 11 | 13 | 111309.5 |
| 139 | `record.globals` | `record.Globals [ZERO]` | 0.00 | 0 | 1/12 matched (target 1) | `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid` | 0/0 matched | _none_ | 11 | 12 | 111210.0 |
| 140 | `heap.heap_type` | `heap.HeapType [ZERO]` | 0.00 | 0 | 37/47 matched (target 68) | `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `test_send_sync`, `test_string_reallocated_on_heap`, `test_interned_string_equal`, `validate_str_interning`, `append_x`, `test_interned_str_starlark` | 8/8 matched (target 9) | _none_ | 10 | 55 | 105510.0 |
| 141 | `alloc.chain` | `alloc.Chain [ZERO]` | 0.00 | 0 | 14/22 matched (target 19) | `drop`, `test_default`, `test_new_drop`, `test_new_drop_many`, `test_split_at`, `test_split_at_len`, `test_split_at_zero`, `test_depth` | 3/5 matched (target 3) | `Item`, `ResetSplitAtZeroTest` | 10 | 27 | 102710.0 |
| 142 | `range.range_type` | `range.RangeType [ZERO]` | 0.00 | 0 | 14/24 matched (target 21) | `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `test_range_exhaustive`, `test_max_len` | 1/1 matched (target 2) | _none_ | 10 | 25 | 102510.0 |
| 143 | `stdlib.partial` | `stdlib.Partial [ZERO]` | 0.00 | 0 | 4/12 matched (target 7) | `partial`, `fmt`, `eq`, `test_simple`, `test_star_to_partial`, `test_start_to_returned_func`, `test_no_args_to_partial`, `test_typecheck_bug` | 3/5 matched (target 3) | `Frozen`, `Canonical` | 10 | 17 | 101710.0 |
| 144 | `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 0.25 | 0 | 3/10 matched | `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter` | 2/5 matched (target 4) | `Target`, `Item`, `IntoIter` | 10 | 15 | 101507.5 |
| 145 | `stdlib.json` | `stdlib.Json` | 0.04 | 0 | 2/11 matched (target 24) | `alloc_value`, `alloc_frozen_value`, `json`, `encode`, `decode`, `test_json_encode`, `test_json_decode`, `test_json_very_large_int`, `test_json_128bit_and_beyond` | 0/1 matched (target 11) | `Canonical` | 10 | 12 | 101209.6 |
| 146 | `layout.vtable` | `layout.Vtable` | 0.67 | 0 | 60/67 matched (target 65) | `value_ptr`, `value_ref`, `drop_in_place`, `fmt`, `as_allocative`, `total_memory_for_profile`, `as_serialize` | 4/6 matched (target 4) | `GetTypeId`, `GetAllocativeKey` | 9 | 73 | 97303.3 |
| 147 | `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 0.00 | 0 | 33/39 matched (target 48) | `fmt`, `check_matches`, `starlark_type_repr`, `alloc_value`, `hash`, `eq` | 5/7 matched (target 12) | `StaticType`, `Canonical` | 8 | 46 | 84610.0 |
| 148 | `profile.bc` | `profile.Bc` | 0.52 | 0 | 12/19 matched (target 24) | `sum`, `add_assign`, `default`, `test_smoke`, `test_smoke_2`, `test_bc_profile_data_merge`, `test_bc_pairs_profile_data_merge` | 9/10 matched (target 13) | `Data` | 8 | 29 | 82904.8 |
| 149 | `analysis.flow` | `analysis.Flow` | 0.52 | 0 | 16/24 matched (target 31) | `lint`, `module`, `about`, `test_lint_returns`, `test_lint_unreachable`, `test_lint_redundant`, `test_lint_misplaced_load`, `test_lint_no_effect` | 1/1 matched (target 11) | _none_ | 8 | 25 | 82504.8 |
| 150 | `alloc.allocator` | `alloc.Allocator [ZERO]` | 0.00 | 0 | 11/18 matched (target 15) | `fmt`, `default`, `drop`, `test_small`, `test_big`, `random_iteration`, `test_many` | 2/3 matched (target 2) | `Item` | 8 | 21 | 82110.0 |
| 151 | `typing.small_arc_vec` | `typing.SmallArcVec` | 0.31 | 0 | 4/11 matched (target 16) | `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter` | 2/3 matched (target 5) | `Target` | 8 | 14 | 81406.9 |
| 152 | `tuple.unpack` | `tuple.Unpack [ZERO]` | 0.00 | 0 | 1/5 matched | `default`, `starlark_type_repr`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 8 | 10 | 81010.0 |
| 153 | `tuple.value` | `tuple.Value [ZERO]` | 0.00 | 0 | 24/31 matched (target 37) | `fmt`, `new`, `offset_of_content`, `typechecker_ty`, `test_to_str`, `test_repr_cycle`, `test_tuple_ellipsis_runtime` | 3/3 matched | _none_ | 7 | 34 | 73410.0 |
| 154 | `profile.aggregated` | `profile.Aggregated [ZERO]` | 0.00 | 0 | 17/24 matched (target 35) | `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `test_stacks_collect`, `test_stacks_collect_retained`, `test_merge`, `make` | 8/8 matched (target 10) | _none_ | 7 | 32 | 73210.0 |
| 155 | `structs.value` | `structs.Value [ZERO]` | 0.00 | 0 | 14/21 matched (target 30) | `iter_frozen`, `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_to_json`, `test_comparison_bug` | 1/1 matched (target 3) | _none_ | 7 | 22 | 72210.0 |
| 156 | `funcs.other` | `funcs.Other [ZERO]` | 0.00 | 0 | 12/19 matched (target 13) | `r#type`, `test_abs`, `test_constants`, `test_chr`, `test_hash`, `test_int`, `test_tuple` | 0/0 matched (target 1) | _none_ | 7 | 19 | 71910.0 |
| 157 | `typed.string` | `typed.String [ZERO]` | 0.00 | 0 | 8/15 matched (target 59) | `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`, `test_string_hashes` | 3/3 matched (target 4) | _none_ | 7 | 18 | 71810.0 |
| 158 | `layout.complex` | `layout.Complex [ZERO]` | 0.00 | 0 | 9/13 matched (target 15) | `unpack_value_impl`, `fmt`, `test_module`, `test_unpack` | 1/4 matched (target 1) | `Canonical`, `Error`, `Frozen` | 7 | 17 | 71710.0 |
| 159 | `dict.methods` | `dict.Methods [ZERO]` | 0.00 | 0 | 10/17 matched (target 12) | `test_error_codes`, `test_dict_add`, `test_dict_with_duplicates`, `test_dict_update_with_self_pos`, `test_dict_update_with_self_as_kwargs`, `test_frozen_dict_cannot_be_updated_with_self_pos`, `test_frozen_dict_cannot_be_updated_with_self_as_kwargs` | 0/0 matched | _none_ | 7 | 17 | 71710.0 |
| 160 | `docs.parse` | `docs.Parse [ZERO]` | 0.00 | 0 | 8/15 matched (target 11) | `parses_starlark_docstring`, `parses_rust_docstring`, `parses_and_removes_sections_from_starlark_docstring`, `parses_and_removes_sections_from_rust_docstring`, `arg`, `parses_starlark_function_docstring`, `parses_rust_function_docstring` | 1/1 matched | _none_ | 7 | 16 | 71610.0 |
| 161 | `string.simd` | `string.Simd [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0 | 1/8 matched (target 4) | `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask` | 2/2 matched | _none_ | 7 | 10 | 71010.0 |
| 162 | `record.ty_record_type` | `record.TyRecordType [ZERO]` | 0.00 | 0 | 0/7 matched (target 0) | `test_good`, `test_fail_compile_time`, `test_fail_runtime_time`, `test_record_instance_typechecker_ty`, `test_typecheck_field_pass`, `test_typecheck_field_fail`, `test_typecheck_record_type_call` | 1/1 matched | _none_ | 7 | 8 | 70810.0 |
| 163 | `compiler.scope` | `compiler.Scope [ZERO]` | 0.00 | 0 | 48/51 matched (target 70) | `from`, `assign_ident_impl`, `new` | 17/20 matched (target 28) | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` | 6 | 71 | 67110.0 |
| 164 | `assert.assert` | `assert.Assert [STUB]` | 0.00 | 0 | 44/50 matched (target 66) | `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck` | 2/2 matched | _none_ | 6 | 52 | 65210.0 |
| 165 | `adapter.implementation` | `adapter.Implementation [ZERO]` | 0.00 | 0 | 17/23 matched (target 27) | `prepare_dap_adapter`, `fmt`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints` | 6/6 matched (target 10) | _none_ | 6 | 29 | 62910.0 |
| 166 | `bc.instrs` | `bc.Instrs [ZERO]` | 0.00 | 0 | 19/24 matched (target 29) | `handle`, `drop`, `opcodes`, `fmt`, `display` | 3/4 matched (target 3) | `HandlerImpl` | 6 | 28 | 62810.0 |
| 167 | `namespace.value` | `namespace.Value` | 0.44 | 0 | 9/15 matched (target 23) | `new`, `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_kwargs` | 2/2 matched (target 4) | _none_ | 6 | 17 | 61705.6 |
| 168 | `analysis.dubious` | `analysis.Dubious` | 0.48 | 0 | 7/12 matched (target 19) | `lint`, `module`, `about`, `test_lint_duplicate_keys`, `test_lint_identifier_as_statement` | 1/2 matched (target 8) | `Key` | 6 | 14 | 61405.2 |
| 169 | `profile.csv` | `profile.Csv` | 0.24 | 0 | 6/10 matched (target 7) | `new`, `format_for_csv`, `test_csv_writer`, `test_quote_str_for_csv` | 1/3 matched (target 2) | `Impl`, `CsvValue` | 6 | 13 | 61307.6 |
| 170 | `analysis.types` | `analysis.Types` | 0.30 | 0 | 4/7 matched | `fmt`, `new`, `from` | 2/5 matched (target 2) | `LintWarning`, `LintT`, `EvalSeverity` | 6 | 12 | 61207.0 |
| 171 | `heap.send` | `heap.Send [ZERO]` | 0.00 | 0 | 2/5 matched (target 6) | `deref`, `deref_mut`, `fmt` | 3/6 matched (target 3) | `Sealed`, `Target`, `StaticType` | 6 | 11 | 61110.0 |
| 172 | `list.unpack` | `list.Unpack [ZERO]` | 0.00 | 0 | 3/5 matched (target 8) | `into_iter`, `test_unpack` | 1/5 matched (target 3) | `Canonical`, `Error`, `Item`, `IntoIter` | 6 | 10 | 61010.0 |
| 173 | `bigint.convert` | `bigint.Convert [ZERO]` | 0.00 | 0 | 4/8 matched (target 27) | `test_unpack_int_error`, `module`, `takes_i32`, `takes_i64` | 0/2 matched (target 7) | `Canonical`, `Error` | 6 | 10 | 61010.0 |
| 174 | `tuple.rust_tuple` | `tuple.RustTuple` | 0.00 | 0 | 0/4 matched (target 11) | `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl` | 0/2 matched (target 0) | `Canonical`, `Error` | 6 | 6 | 60610.0 |
| 175 | `environment.modules` | `environment.Modules [ZERO]` | 0.00 | 0 | 38/43 matched (target 48) | `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo` | 4/4 matched (target 6) | _none_ | 5 | 47 | 54710.0 |
| 176 | `values.owned` | `values.Owned [ZERO]` | 0.00 | 0 | 26/29 matched (target 34) | `fmt`, `downcast_starlark`, `deref` | 3/5 matched | `Canonical`, `Target` | 5 | 34 | 53410.0 |
| 177 | `profile.time_flame` | `profile.TimeFlame` | 0.60 | 0 | 15/19 matched (target 18) | `r#gen`, `test_time_flame_works_inside_frozen_module`, `register_sleep`, `sleep` | 10/11 matched (target 15) | `Data` | 5 | 30 | 53004.0 |
| 178 | `profile.stmt` | `profile.Stmt [ZERO]` | 0.00 | 0 | 13/17 matched (target 20) | `r#gen`, `test_coverage`, `test_empty`, `test_merge` | 8/9 matched | `Data` | 5 | 26 | 52610.0 |
| 179 | `typing.callable_param` | `typing.CallableParam` | 0.56 | 0 | 16/20 matched (target 27) | `fmt`, `pf`, `new_named_only`, `test_param_spec_display` | 5/6 matched (target 10) | `ParamSpecDisplay` | 5 | 26 | 52604.4 |
| 180 | `values.unpack` | `values.Unpack [ZERO]` | 0.00 | 0 | 8/9 matched (target 14) | `error` | 3/7 matched | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` | 5 | 16 | 51610.0 |
| 181 | `dict.refs` | `dict.Refs` | 0.51 | 0 | 7/9 matched (target 13) | `from_value`, `deref` | 4/7 matched (target 11) | `Target`, `Canonical`, `Error` | 5 | 16 | 51604.9 |
| 182 | `analysis.underscore` | `analysis.Underscore` | 0.44 | 0 | 8/13 matched (target 14) | `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored` | 1/1 matched (target 3) | _none_ | 5 | 14 | 51405.6 |
| 183 | `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 0.00 | 0 | 6/8 matched (target 6) | `next`, `size_hint` | 0/3 matched (target 1) | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` | 5 | 11 | 51110.0 |
| 184 | `typing.iter` | `typing.Iter [ZERO]` | 0.00 | 0 | 3/6 matched (target 5) | `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail` | 2/4 matched (target 2) | `NonInstantiable`, `Canonical` | 5 | 10 | 51010.0 |
| 185 | `debug.inspect` | `debug.Inspect [ZERO]` | 0.00 | 0 | 4/9 matched (target 4) | `debugger`, `debug_inspect_stack`, `debug_inspect_variables`, `test_debug_stack`, `test_debug_variables` | 0/0 matched | _none_ | 5 | 9 | 50910.0 |
| 186 | `params.spec` | `params.Spec [ZERO]` | 0.00 | 0 | 34/38 matched (target 35) | `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl` | 6/6 matched (target 11) | _none_ | 4 | 44 | 44410.0 |
| 187 | `string.methods` | `string.Methods [ZERO]` | 0.00 | 0 | 37/41 matched (target 64) | `test_error_codes`, `test_count`, `test_find`, `test_opaque_iterator` | 1/1 matched (target 4) | _none_ | 4 | 42 | 44210.0 |
| 188 | `typing.custom` | `typing.Custom [ZERO]` | 0.00 | 0 | 31/35 matched (target 49) | `eq`, `hash`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | 4 | 38 | 43810.0 |
| 189 | `heap.repr` | `heap.Repr [ZERO]` | 0.00 | 0 | 23/27 matched (target 35) | `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut` | 5/5 matched (target 8) | _none_ | 4 | 32 | 43210.0 |
| 190 | `bc.addr` | `bc.Addr [ZERO]` | 0.00 | 0 | 20/23 matched (target 35) | `add_assign`, `get_instr_mut`, `sub_usize` | 5/6 matched (target 5) | `Output` | 4 | 29 | 42910.0 |
| 191 | `analysis.incompatible` | `analysis.Incompatible` | 0.58 | 0 | 10/14 matched (target 17) | `lint`, `module`, `test_lint_incompatible`, `test_lint_duplicate_top_level_assign` | 1/1 matched (target 3) | _none_ | 4 | 15 | 41504.2 |
| 192 | `avalues.static_` | `avalues.Static [ZERO]` | 0.00 | 0 | 8/9 matched | `test_alloc_static_simple` | 2/5 matched (target 2) | `StarlarkValue`, `ExtraElem`, `MySimpleValue` | 4 | 14 | 41410.0 |
| 193 | `profile.typecheck` | `profile.Typecheck [ZERO]` | 0.00 | 0 | 5/8 matched (target 6) | `r#gen`, `test_typecheck_profile`, `test_typecheck_profile_merge` | 4/5 matched | `Data` | 4 | 13 | 41310.0 |
| 194 | `profile.flamegraph` | `profile.Flamegraph` | 0.59 | 0 | 6/10 matched (target 13) | `new`, `test_flamegraph_writer`, `test_flamegraph_data`, `test_merge` | 3/3 matched | _none_ | 4 | 13 | 41304.1 |
| 195 | `runtime.inlined_frame` | `runtime.InlinedFrame` | 0.50 | 0 | 5/9 matched (target 6) | `eq`, `test_inline_into`, `make_span`, `assert_stack` | 3/3 matched | _none_ | 4 | 12 | 41205.0 |
| 196 | `list.methods` | `list.Methods [ZERO]` | 0.00 | 0 | 7/11 matched (target 13) | `list_methods`, `test_error_codes`, `test_index`, `recursive_list` | 0/0 matched | _none_ | 4 | 11 | 41110.0 |
| 197 | `analysis.performance` | `analysis.Performance` | 0.45 | 0 | 6/10 matched (target 14) | `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function` | 1/1 matched (target 4) | _none_ | 4 | 11 | 41105.5 |
| 198 | `params.parser` | `params.Parser [ZERO]` | 0.00 | 0 | 5/9 matched (target 5) | `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args` | 1/1 matched | _none_ | 4 | 10 | 41010.0 |
| 199 | `profile.mode` | `profile.Mode` | 0.15 | 0 | 1/4 matched | `fmt`, `name`, `from_str` | 1/2 matched (target 1) | `Err` | 4 | 6 | 40608.5 |
| 200 | `int.i32` | `int.I32` | 0.23 | 0 | 2/4 matched (target 5) | `alloc_value`, `alloc_frozen_value` | 0/2 matched (target 1) | `Canonical`, `Error` | 4 | 6 | 40607.7 |
| 201 | `structs.alloc` | `structs.Alloc [ZERO]` | 0.00 | 0 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | 4 | 5 | 40510.0 |
| 202 | `dict.alloc` | `dict.Alloc [ZERO]` | 0.00 | 0 | 0/3 matched (target 1) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | 4 | 5 | 40510.0 |
| 203 | `set.set` | `set.Set [ZERO]` | 0.00 | 0 | 1/5 matched (target 1) | `set`, `test_set_type_as_type_compile_time`, `test_return_set_type_as_type_compile_time`, `test_set_type_as_type_run_time` | 0/0 matched | _none_ | 4 | 5 | 40510.0 |
| 204 | `enumeration.globals` | `enumeration.Globals` | 0.12 | 0 | 1/5 matched (target 1) | `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr` | 0/0 matched | _none_ | 4 | 5 | 40508.8 |
| 205 | `profile.heap` | `profile.Heap` | 0.77 | 0 | 11/13 matched (target 27) | `r#gen`, `test_profiling` | 10/11 matched | `Data` | 3 | 24 | 32402.3 |
| 206 | `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 0.00 | 0 | 10/10 matched (target 13) | _none_ | 4/7 matched | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` | 3 | 17 | 31710.0 |
| 207 | `list.refs` | `list.Refs [ZERO]` | 0.00 | 0 | 9/9 matched (target 29) | _none_ | 2/5 matched (target 10) | `Target`, `Canonical`, `Error` | 3 | 14 | 31410.0 |
| 208 | `avalues.list` | `avalues.List` | 0.48 | 0 | 9/10 matched (target 19) | `alloc_list_concat` | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | 3 | 14 | 31405.2 |
| 209 | `symbol.map` | `symbol.Map [ZERO]` | 0.00 | 0 | 9/12 matched (target 11) | `fmt`, `new`, `with_capacity` | 1/1 matched | _none_ | 3 | 13 | 31310.0 |
| 210 | `bc.opcode` | `bc.Opcode [ZERO]` | 0.00 | 0 | 6/7 matched (target 10) | `opcode_count` | 3/5 matched (target 3) | `ByNumber`, `FindOpcode` | 3 | 12 | 31210.0 |
| 211 | `tuple.refs` | `tuple.Refs` | 0.64 | 0 | 6/7 matched (target 15) | `unpack_value_impl` | 2/4 matched (target 2) | `Canonical`, `Error` | 3 | 11 | 31103.6 |
| 212 | `enumeration.value` | `enumeration.Value` | 0.46 | 0 | 6/9 matched (target 10) | `fmt`, `index`, `value` | 1/1 matched (target 8) | _none_ | 3 | 10 | 31005.4 |
| 213 | `bc.repr` | `bc.Repr [ZERO]` | 0.00 | 0 | 4/6 matched (target 5) | `size_of_repr`, `handle` | 2/3 matched (target 2) | `HandlerImpl` | 3 | 9 | 30910.0 |
| 214 | `typing.never` | `typing.Never [ZERO]` | 0.00 | 0 | 4/6 matched (target 7) | `test_never_runtime`, `test_never_compile_time` | 2/3 matched (target 2) | `Canonical` | 3 | 9 | 30910.0 |
| 215 | `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 0.00 | 0 | 5/6 matched (target 9) | `unpack_value_impl` | 0/2 matched (target 1) | `Canonical`, `Error` | 3 | 8 | 30810.0 |
| 216 | `tuple.alloc` | `tuple.Alloc` | 0.49 | 0 | 3/5 matched (target 3) | `test_alloc_tuple`, `test_alloc_frozen_tuple` | 1/2 matched (target 1) | `Canonical` | 3 | 7 | 30705.1 |
| 217 | `values.typing.ty` | `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` | 0.00 | 0 | 2/5 matched (target 4) | `test_isinstance`, `test_pass`, `test_fail_compile_time` | 1/1 matched | _none_ | 3 | 6 | 30610.0 |
| 218 | `float.unpack` | `float.Unpack` | 0.33 | 0 | 2/3 matched | `test_unpack_float` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 6 | 30606.7 |
| 219 | `dict.unpack` | `dict.Unpack` | 0.55 | 0 | 2/3 matched | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | 3 | 6 | 30604.5 |
| 220 | `debug.evaluate` | `debug.Evaluate [ZERO]` | 0.00 | 0 | 1/4 matched (target 1) | `debugger`, `debug_evaluate`, `test_debug_evaluate` | 0/0 matched | _none_ | 3 | 4 | 30410.0 |
| 221 | `type_compiled.globals` | `type_compiled.Globals [ZERO]` | 0.00 | 0 | 1/4 matched (target 1) | `eval_type`, `isinstance`, `test_typechecking` | 0/0 matched | _none_ | 3 | 4 | 30410.0 |
| 222 | `compiler.expr` | `compiler.Expr [ZERO]` | 0.00 | 0 | 59/59 matched (target 63) | _none_ | 9/11 matched (target 56) | `AstLiteralCompile`, `CompilerExprUtil` | 2 | 70 | 27010.0 |
| 223 | `values.traits` | `values.Traits [ZERO]` | 0.00 | 0 | 55/56 matched (target 55) | `please_use_starlark_type_macro` | 2/3 matched (target 2) | `Canonical` | 2 | 59 | 25910.0 |
| 224 | `compiler.def` | `compiler.Def [ZERO]` | 0.00 | 0 | 38/39 matched (target 46) | `fmt` | 12/13 matched (target 18) | `Frozen` | 2 | 52 | 25210.0 |
| 225 | `types.function` | `types.Function [ZERO]` | 0.00 | 0 | 12/13 matched (target 28) | `new` | 11/12 matched (target 14) | `Canonical` | 2 | 25 | 22510.0 |
| 226 | `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 0.00 | 0 | 10/11 matched (target 25) | `add` | 7/8 matched (target 7) | `Output` | 2 | 19 | 21910.0 |
| 227 | `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 0.00 | 0 | 9/10 matched | `drop_non_drop` | 2/3 matched (target 2) | `RowKind` | 2 | 13 | 21310.0 |
| 228 | `avalues.array` | `avalues.Array [ZERO]` | 0.00 | 0 | 9/9 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | 2 | 13 | 21310.0 |
| 229 | `compiler.args` | `compiler.Args` | 0.60 | 0 | 10/11 matched | `args` | 1/2 matched (target 1) | `Never` | 2 | 13 | 21304.0 |
| 230 | `avalues.tuple` | `avalues.Tuple [ZERO]` | 0.00 | 0 | 8/8 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | 2 | 12 | 21210.0 |
| 231 | `avalues.complex` | `avalues.Complex [ZERO]` | 0.00 | 0 | 6/6 matched (target 17) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | 2 | 11 | 21110.0 |
| 232 | `eval.bc.compiler.stmt` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` | 0.00 | 0 | 8/10 matched (target 11) | `write_if_then`, `write_if_else` | 0/0 matched | _none_ | 2 | 10 | 21010.0 |
| 233 | `set.refs` | `set.Refs [ZERO]` | 0.00 | 0 | 5/5 matched (target 19) | _none_ | 3/5 matched (target 13) | `Canonical`, `Error` | 2 | 10 | 21010.0 |
| 234 | `symbol.symbol` | `symbol.Symbol` | 0.63 | 0 | 7/9 matched (target 11) | `fmt`, `eq` | 1/1 matched | _none_ | 2 | 10 | 21003.7 |
| 235 | `bc.instr_arg` | `bc.InstrArg [ZERO]` | 0.00 | 0 | 4/5 matched (target 84) | `fmt` | 3/4 matched (target 42) | `HandlerImpl` | 2 | 9 | 20910.0 |
| 236 | `structs.refs` | `structs.Refs [ZERO]` | 0.00 | 0 | 5/5 matched (target 8) | _none_ | 2/4 matched | `Canonical`, `Error` | 2 | 9 | 20910.0 |
| 237 | `bc.call` | `bc.Call [ZERO]` | 0.00 | 0 | 3/4 matched (target 15) | `fmt` | 4/5 matched (target 8) | `Args` | 2 | 9 | 20910.0 |
| 238 | `profile.data` | `profile.Data` | 0.55 | 0 | 4/6 matched (target 5) | `_assert_profile_data_send_sync`, `_assert_send_sync` | 3/3 matched (target 18) | _none_ | 2 | 9 | 20904.5 |
| 239 | `typing.callable` | `typing.Callable` | 0.58 | 0 | 6/7 matched (target 10) | `fmt` | 1/2 matched (target 1) | `TyCallableInner` | 2 | 9 | 20904.2 |
| 240 | `bc.bytecode` | `bc.Bytecode` | 0.60 | 0 | 6/7 matched (target 11) | `handle` | 1/2 matched (target 1) | `HandlerImpl` | 2 | 9 | 20904.0 |
| 241 | `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 0.00 | 0 | 0/1 matched (target 4) | `drop` | 5/6 matched (target 5) | `Canonical` | 2 | 7 | 20710.0 |
| 242 | `types.any` | `types.Any` | 0.71 | 0 | 4/5 matched | `fmt` | 1/2 matched (target 1) | `Canonical` | 2 | 7 | 20702.9 |
| 243 | `values.type_repr` | `values.TypeRepr [ZERO]` | 0.00 | 0 | 2/3 matched (target 6) | `test_canonical_for_complex_value` | 2/3 matched (target 6) | `Canonical` | 2 | 6 | 20610.0 |
| 244 | `list.globals` | `list.Globals [ZERO]` | 0.00 | 0 | 4/5 matched | `list` | 0/1 matched | `ListType` | 2 | 6 | 20610.0 |
| 245 | `values.index` | `values.Index [ZERO]` | 0.00 | 0 | 4/6 matched (target 5) | `test_convert_index`, `test_apply_slice` | 0/0 matched | _none_ | 2 | 6 | 20610.0 |
| 246 | `dict.traits` | `dict.Traits` | 0.33 | 0 | 4/4 matched (target 7) | _none_ | 0/2 matched (target 6) | `Canonical`, `Error` | 2 | 6 | 20606.7 |
| 247 | `intern.interner` | `intern.Interner [ZERO]` | 0.00 | 0 | 1/3 matched (target 5) | `test_intern`, `test_string_value_intern` | 2/2 matched | _none_ | 2 | 5 | 20510.0 |
| 248 | `funcs.min_max` | `funcs.MinMax [ZERO]` | 0.00 | 0 | 3/5 matched (target 3) | `max`, `min` | 0/0 matched | _none_ | 2 | 5 | 20510.0 |
| 249 | `typing.any` | `typing.Any [ZERO]` | 0.00 | 0 | 2/4 matched | `test_any_runtime`, `test_any_compile_time` | 1/1 matched | _none_ | 2 | 5 | 20510.0 |
| 250 | `bc.definitely_assigned` | `bc.DefinitelyAssigned` | 0.42 | 0 | 2/4 matched (target 7) | `new`, `assert_smaller_then` | 1/1 matched | _none_ | 2 | 5 | 20505.8 |
| 251 | `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 0.00 | 0 | 2/3 matched (target 2) | `drop` | 0/1 matched (target 0) | `Guard` | 2 | 4 | 20410.0 |
| 252 | `stdlib.internal` | `stdlib.Internal` | 0.31 | 0 | 2/4 matched (target 2) | `ty_of_value_debug`, `test_ty_of_value_debug` | 0/0 matched | _none_ | 2 | 4 | 20406.9 |
| 253 | `enumeration.ty_enum_type` | `enumeration.TyEnumType` | 0.00 | 0 | 0/2 matched (target 3) | `eq`, `hash` | 1/1 matched | _none_ | 2 | 3 | 20310.0 |
| 254 | `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 0.34 | 0 | 1/2 matched (target 1) | `drop` | 0/1 matched (target 0) | `WriteRemOnDrop` | 2 | 3 | 20306.6 |
| 255 | `bool.unpack` | `bool.Unpack` | 0.00 | 0 | 0/1 matched | `unpack_value_impl` | 0/1 matched (target 0) | `Error` | 2 | 2 | 20210.0 |
| 256 | `build` | `starlark.Build [ZERO]` | 0.00 | 0 | 0/2 matched (target 0) | `main`, `rust_nightly` | 0/0 matched (target 1) | _none_ | 2 | 2 | 20210.0 |
| 257 | `bool.type_repr` | `bool.TypeRepr [ZERO]` | 0.00 | 0 | 0/1 matched | `starlark_type_repr` | 0/1 matched (target 0) | `Canonical` | 2 | 2 | 20210.0 |
| 258 | `debug.adapter` | `debug.Adapter [ZERO]` | 0.00 | 0 | 21/22 matched (target 23) | `fmt` | 14/14 matched (target 29) | _none_ | 1 | 36 | 13610.0 |
| 259 | `docs` | `docs.Docs [ZERO]` | 0.00 | 0 | 12/13 matched (target 16) | `default` | 10/10 matched (target 15) | _none_ | 1 | 23 | 12310.0 |
| 260 | `typing.basic` | `typing.Basic` | 0.72 | 0 | 18/19 matched (target 20) | `fmt` | 1/1 matched (target 11) | _none_ | 1 | 20 | 12002.8 |
| 261 | `record.instance` | `record.Instance [ZERO]` | 0.00 | 0 | 12/13 matched (target 18) | `fmt` | 1/1 matched (target 3) | _none_ | 1 | 14 | 11410.0 |
| 262 | `compiler.def_inline` | `compiler.DefInline` | 0.70 | 0 | 9/10 matched (target 9) | `new` | 4/4 matched (target 6) | _none_ | 1 | 14 | 11403.0 |
| 263 | `type_compiled.factory` | `type_compiled.Factory` | 0.93 | 0 | 9/9 matched | _none_ | 1/2 matched (target 1) | `Result` | 1 | 11 | 11100.7 |
| 264 | `bool.value` | `bool.Value` | 0.49 | 0 | 8/9 matched | `fmt` | 1/1 matched | _none_ | 1 | 10 | 11005.1 |
| 265 | `namespace.typing` | `namespace.Typing` | 0.65 | 0 | 6/7 matched (target 8) | `fmt` | 3/3 matched | _none_ | 1 | 10 | 11003.5 |
| 266 | `alloc.per_thread` | `alloc.PerThread [ZERO]` | 0.00 | 0 | 5/6 matched (target 5) | `test_release_partial` | 1/1 matched | _none_ | 1 | 7 | 10710.0 |
| 267 | `profile.by_type` | `profile.ByType [ZERO]` | 0.00 | 0 | 5/6 matched (target 7) | `normalize_for_golden_tests` | 1/1 matched | _none_ | 1 | 7 | 10710.0 |
| 268 | `values.recursive_repr_or_json_guard` | `values.RecursiveReprOrJsonGuard [ZERO]` | 0.00 | 0 | 2/3 matched (target 5) | `drop` | 4/4 matched | _none_ | 1 | 7 | 10710.0 |
| 269 | `compiler.if_compiler` | `compiler.IfCompiler [ZERO]` | 0.00 | 0 | 5/6 matched (target 5) | `wr` | 0/0 matched | _none_ | 1 | 6 | 10610.0 |
| 270 | `eval.bc.compiler.call` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` | 0.00 | 0 | 4/5 matched (target 8) | `mark_definitely_assigned_after` | 0/0 matched (target 3) | _none_ | 1 | 5 | 10510.0 |
| 271 | `structs.structs` | `structs.Structs [ZERO]` | 0.00 | 0 | 3/4 matched (target 3) | `r#struct` | 1/1 matched | _none_ | 1 | 5 | 10510.0 |
| 272 | `types.unbound` | `types.Unbound` | 0.60 | 0 | 3/4 matched | `fmt` | 1/1 matched (target 3) | _none_ | 1 | 5 | 10504.0 |
| 273 | `analysis.find_call_name` | `analysis.FindCallName` | 0.55 | 0 | 2/3 matched (target 8) | `finds_function_calls_with_name_kwarg` | 1/1 matched | _none_ | 1 | 4 | 10404.5 |
| 274 | `dict.globals` | `dict.Globals [ZERO]` | 0.00 | 0 | 2/3 matched (target 4) | `dict` | 0/0 matched | _none_ | 1 | 3 | 10310.0 |
| 275 | `compiler.assign_modify` | `compiler.AssignModify` | 0.88 | 0 | 2/2 matched (target 3) | _none_ | 0/1 matched (target 0) | `AssignOnWriteBc` | 1 | 3 | 10301.2 |
| 276 | `runtime.visit_span` | `runtime.VisitSpan` | 0.00 | 0 | 0/1 matched (target 19) | `visit_spans` | 1/1 matched | _none_ | 1 | 2 | 10210.0 |
| 277 | `pagable.error` | `pagable.Error` | 0.00 | 0 | 0/1 matched | `from` | 1/1 matched (target 2) | _none_ | 1 | 2 | 10210.0 |
| 278 | `int.globals` | `int.Globals [ZERO]` | 0.00 | 0 | 1/2 matched | `int` | 0/0 matched | _none_ | 1 | 2 | 10210.0 |
| 279 | `float.globals` | `float.Globals [ZERO]` | 0.00 | 0 | 1/2 matched (target 1) | `float` | 0/0 matched (target 4) | _none_ | 1 | 2 | 10210.0 |
| 280 | `bool.globals` | `bool.Globals` | 0.19 | 0 | 1/2 matched (target 1) | `bool` | 0/0 matched | _none_ | 1 | 2 | 10208.1 |
| 281 | `range.globals` | `range.Globals` | 0.30 | 0 | 1/2 matched (target 1) | `range` | 0/0 matched | _none_ | 1 | 2 | 10207.0 |
| 282 | `namespace.globals` | `namespace.Globals` | 0.30 | 0 | 1/2 matched (target 1) | `namespace` | 0/0 matched | _none_ | 1 | 2 | 10207.0 |
| 283 | `tuple.globals` | `tuple.Globals` | 0.31 | 0 | 1/2 matched (target 1) | `tuple` | 0/0 matched | _none_ | 1 | 2 | 10206.9 |
| 284 | `num.globals` | `num.Globals` | 0.32 | 0 | 1/2 matched (target 1) | `abs` | 0/0 matched | _none_ | 1 | 2 | 10206.8 |
| 285 | `bc.writer` | `bc.Writer [ZERO]` | 0.00 | 0 | 42/42 matched (target 44) | _none_ | 4/4 matched | _none_ | 0 | 46 | 4610.0 |
| 286 | `typing.fill_types_for_lint` | `typing.FillTypesForLint [ZERO]` | 0.00 | 0 | 39/39 matched (target 40) | _none_ | 3/3 matched | _none_ | 0 | 42 | 4210.0 |
| 287 | `oracle.ctx` | `oracle.Ctx` | 0.79 | 0 | 32/32 matched | _none_ | 2/2 matched (target 14) | _none_ | 0 | 34 | 3402.1 |
| 288 | `type_compiled.alloc` | `type_compiled.Alloc` | 0.87 | 0 | 28/28 matched (target 37) | _none_ | 1/1 matched (target 3) | _none_ | 0 | 29 | 2901.3 |
| 289 | `type_compiled.matchers` | `type_compiled.Matchers [ZERO]` | 0.00 | 0 | 3/3 matched (target 25) | _none_ | 23/23 matched | _none_ | 0 | 26 | 2610.0 |
| 290 | `docs.markdown` | `docs.Markdown [ZERO]` | 0.00 | 0 | 18/18 matched (target 19) | _none_ | 2/2 matched | _none_ | 0 | 20 | 2010.0 |
| 291 | `typing.ctx` | `typing.Ctx` | 0.76 | 0 | 19/19 matched (target 20) | _none_ | 1/1 matched (target 2) | _none_ | 0 | 20 | 2002.4 |
| 292 | `eval.bc.compiler.expr` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` | 0.00 | 0 | 15/15 matched (target 16) | _none_ | 0/0 matched | _none_ | 0 | 15 | 1510.0 |
| 293 | `environment.names` | `environment.Names [ZERO]` | 0.00 | 0 | 13/13 matched (target 14) | _none_ | 2/2 matched | _none_ | 0 | 15 | 1510.0 |
| 294 | `typing.error` | `typing.Error [ZERO]` | 0.00 | 0 | 9/9 matched (target 25) | _none_ | 5/5 matched (target 10) | _none_ | 0 | 14 | 1410.0 |
| 295 | `compiler.call` | `compiler.Call` | 0.67 | 0 | 13/13 matched (target 14) | _none_ | 1/1 matched | _none_ | 0 | 14 | 1403.3 |
| 296 | `compiler.compr` | `compiler.Compr` | 0.75 | 0 | 9/9 matched (target 12) | _none_ | 3/3 matched (target 5) | _none_ | 0 | 12 | 1202.5 |
| 297 | `environment.slots` | `environment.Slots` | 0.67 | 0 | 8/8 matched (target 10) | _none_ | 3/3 matched | _none_ | 0 | 11 | 1103.3 |
| 298 | `docs.multipage` | `docs.Multipage` | 0.86 | 0 | 6/6 matched | _none_ | 5/5 matched (target 7) | _none_ | 0 | 11 | 1101.4 |
| 299 | `compiler.types` | `compiler.Types [ZERO]` | 0.00 | 0 | 8/8 matched | _none_ | 1/1 matched (target 7) | _none_ | 0 | 9 | 910.0 |
| 300 | `__derive_refs.parse_args` | `deriverefs.ParseArgs [PROVENANCE-FALLBACK]` | 0.72 | 0 | 8/8 matched | _none_ | 0/0 matched | _none_ | 0 | 8 | 802.8 |
| 301 | `docs.code` | `docs.Code [ZERO]` | 0.00 | 0 | 7/7 matched (target 14) | _none_ | 0/0 matched | _none_ | 0 | 7 | 710.0 |
| 302 | `layout.value_not_special` | `layout.ValueNotSpecial` | 0.72 | 0 | 6/6 matched (target 7) | _none_ | 1/1 matched | _none_ | 0 | 7 | 702.8 |
| 303 | `unused_loads.find` | `unusedloads.Find` | 0.79 | 0 | 4/4 matched (target 8) | _none_ | 3/3 matched | _none_ | 0 | 7 | 702.1 |
| 304 | `types.known_methods` | `types.KnownMethods` | 0.83 | 0 | 5/5 matched | _none_ | 2/2 matched | _none_ | 0 | 7 | 701.7 |
| 305 | `runtime.before_stmt` | `runtime.BeforeStmt` | 0.86 | 0 | 4/4 matched | _none_ | 3/3 matched (target 5) | _none_ | 0 | 7 | 701.4 |
| 306 | `compiler.module` | `compiler.Module` | 0.86 | 0 | 6/6 matched | _none_ | 1/1 matched (target 4) | _none_ | 0 | 7 | 701.4 |
| 307 | `layout.static_string` | `layout.StaticString [ZERO]` | 0.00 | 0 | 5/5 matched | _none_ | 1/1 matched (target 2) | _none_ | 0 | 6 | 610.0 |
| 308 | `assert.conformance` | `assert.Conformance` | 0.74 | 0 | 5/5 matched | _none_ | 1/1 matched | _none_ | 0 | 6 | 602.6 |
| 309 | `string.globals` | `string.Globals [ZERO]` | 0.00 | 0 | 5/5 matched | _none_ | 0/0 matched | _none_ | 0 | 5 | 510.0 |
| 310 | `unused_loads.remove` | `unusedloads.Remove [ZERO]` | 0.00 | 0 | 4/4 matched | _none_ | 1/1 matched | _none_ | 0 | 5 | 510.0 |
| 311 | `runtime.slots` | `runtime.Slots` | 0.76 | 0 | 2/2 matched (target 3) | _none_ | 3/3 matched | _none_ | 0 | 5 | 502.4 |
| 312 | `values.comparison` | `values.Comparison` | 0.79 | 0 | 5/5 matched | _none_ | 0/0 matched | _none_ | 0 | 5 | 502.1 |
| 313 | `compiler.expr_bool` | `compiler.ExprBool` | 0.79 | 0 | 4/4 matched (target 5) | _none_ | 1/1 matched (target 3) | _none_ | 0 | 5 | 502.1 |
| 314 | `funcs.zip` | `funcs.Zip` | 0.80 | 0 | 4/4 matched (target 7) | _none_ | 1/1 matched | _none_ | 0 | 5 | 502.0 |
| 315 | `num.typecheck` | `num.Typecheck` | 0.90 | 0 | 2/2 matched | _none_ | 3/3 matched (target 5) | _none_ | 0 | 5 | 501.0 |
| 316 | `string.iter` | `string.Iter [ZERO]` | 0.00 | 0 | 3/3 matched (target 6) | _none_ | 1/1 matched | _none_ | 0 | 4 | 410.0 |
| 317 | `__derive_refs.components` | `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0 | 3/3 matched | _none_ | 1/1 matched | _none_ | 0 | 4 | 410.0 |
| 318 | `__derive_refs.sig` | `deriverefs.Sig [PROVENANCE-FALLBACK]` | 0.86 | 0 | 3/3 matched | _none_ | 1/1 matched (target 4) | _none_ | 0 | 4 | 401.4 |
| 319 | `compiler.error` | `compiler.Error [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0 | 2/2 matched (target 24) | _none_ | 1/1 matched (target 14) | _none_ | 0 | 3 | 310.0 |
| 320 | `oracle.traits` | `oracle.Traits` | 0.60 | 0 | 1/1 matched (target 3) | _none_ | 2/2 matched | _none_ | 0 | 3 | 304.0 |
| 321 | `callable.param` | `callable.Param` | 0.76 | 0 | 1/1 matched (target 6) | _none_ | 2/2 matched (target 7) | _none_ | 0 | 3 | 302.4 |
| 322 | `eval.soft_error` | `eval.SoftError` | 0.84 | 0 | 1/1 matched | _none_ | 2/2 matched | _none_ | 0 | 3 | 301.6 |
| 323 | `compiler.type_expr` | `compiler.TypeExpr [PROVENANCE-FALLBACK]` | 0.89 | 0 | 2/2 matched (target 7) | _none_ | 1/1 matched (target 17) | _none_ | 0 | 3 | 301.1 |
| 324 | `fuzz_targets.starlark` | `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | 0 | 2 | 210.0 |
| 325 | `compiler.assign` | `compiler.Assign [ZERO]` | 0.00 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 210.0 |
| 326 | `eval.bc.compiler.def` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` | 0.00 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 210.0 |
| 327 | `__derive_refs.invoke_macro_error` | `deriverefs.InvokeMacroError [PROVENANCE-FALLBACK]` | 0.31 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 206.9 |
| 328 | `typing.macro_refs` | `typing.MacroRefs` | 0.80 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 202.0 |
| 329 | `enumeration.matcher` | `enumeration.Matcher` | 0.82 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 201.8 |
| 330 | `eval` | `eval.Eval` | 0.84 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 201.6 |
| 331 | `typing.macro_support` | `typing.MacroSupport` | 0.85 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 201.5 |
| 332 | `layout.identity` | `layout.Identity` | 0.87 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 201.3 |
| 333 | `record.matcher` | `record.Matcher` | 0.92 | 0 | 1/1 matched | _none_ | 1/1 matched | _none_ | 0 | 2 | 200.8 |
| 334 | `eval.bc.compiler.compr` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr` | 0.93 | 0 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | 0 | 2 | 200.7 |
| 335 | `bool.alloc` | `bool.Alloc` | 0.95 | 0 | 2/2 matched | _none_ | 0/0 matched | _none_ | 0 | 2 | 200.5 |
| 336 | `bc.slow_arg` | `bc.SlowArg` | 1.00 | 0 | 0/0 matched | _none_ | 2/2 matched | _none_ | 0 | 2 | 200.0 |
| 337 | `allocator.api` | `allocator.Api` | 1.00 | 0 | 0/0 matched | _none_ | 2/2 matched | _none_ | 0 | 2 | 200.0 |
| 338 | `bc.instr` | `bc.Instr` | 1.00 | 0 | 0/0 matched | _none_ | 2/2 matched (target 5) | _none_ | 0 | 2 | 200.0 |
| 339 | `environment` | `starlark.Environment [ZERO]` | 0.00 | 0 | 0/0 matched | _none_ | 1/1 matched (target 5) | _none_ | 0 | 1 | 110.0 |
| 340 | `bc.for_loop` | `bc.ForLoop [ZERO]` | 0.00 | 0 | 0/0 matched (target 2) | _none_ | 1/1 matched | _none_ | 0 | 1 | 110.0 |
| 341 | `wasm` | `starlark.Wasm` | 0.25 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 107.5 |
| 342 | `environment.module_dump` | `environment.ModuleDump` | 0.48 | 0 | 1/1 matched (target 2) | _none_ | 0/0 matched | _none_ | 0 | 1 | 105.2 |
| 343 | `none.globals` | `none.Globals` | 0.71 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 102.9 |
| 344 | `typing.globals` | `typing.Globals` | 0.74 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 102.6 |
| 345 | `compiler.known` | `compiler.Known` | 0.78 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 102.2 |
| 346 | `eval.params` | `eval.Params` | 0.91 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 100.9 |
| 347 | `funcs.globals` | `funcs.Globals` | 0.99 | 0 | 1/1 matched | _none_ | 0/0 matched | _none_ | 0 | 1 | 100.1 |
| 348 | `profile.or_instrumentation` | `profile.OrInstrumentation` | 1.00 | 0 | 0/0 matched | _none_ | 1/1 matched (target 4) | _none_ | 0 | 1 | 100.0 |
| 349 | `typing.call_args` | `typing.CallArgs` | 1.00 | 0 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 100.0 |
| 350 | `typing.mode` | `typing.Mode` | 1.00 | 0 | 0/0 matched | _none_ | 1/1 matched | _none_ | 0 | 1 | 100.0 |
| 351 | `syntax` | `starlark.Syntax [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 352 | `values.types` | `values.Types [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 353 | `pagable` | `starlark.Pagable [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 354 | `values.typing` | `values.Typing [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 355 | `typing.oracle` | `typing.Oracle [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 356 | `util` | `starlark.Util [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 357 | `layout.avalues` | `layout.AValues [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 358 | `errors` | `starlark.Errors [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 359 | `macros` | `starlark.Macros [ZERO]` | 0.00 | 0 | 0/0 matched (target 9) | _none_ | 0/0 matched (target 9) | _none_ | 0 | 0 | 10.0 |
| 360 | `stdlib.funcs` | `stdlib.Funcs [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 361 | `__derive_refs` | `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched (target 1) | _none_ | 0 | 0 | 10.0 |
| 362 | `pagable.vtable_register` | `pagable.VtableRegister [ZERO]` | 0.00 | 0 | 0/0 matched (target 3) | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 363 | `eval.runtime` | `eval.Runtime [STUB]` | 0.00 | 0 | 0/0 matched | _none_ | 0/0 matched | _none_ | 0 | 0 | 10.0 |
| 364 | `values` | `values.Values` | 1.00 | 0 | 0/0 matched | _none_ | 0/0 matched (target 10) | _none_ | 0 | 0 | 0.0 |

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
- `typing.type_compiled` -> `type_compiled.TypeCompiled [STUB]`: function-by-function score forced to 0. TypeCompiled.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `environment.globals` -> `environment.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `derive.module` -> `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `values.value_of_unchecked` -> `values.ValueOfUnchecked [ZERO]`: function-by-function score forced to 0. ValueOfUnchecked.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; ValueOfUnchecked.kt: Rust `fn` declaration in Kotlin comments; ValueOfUnchecked.kt: score-padding suppression annotation `@Suppress` in Kotlin code; ValueOfUnchecked.kt: Rust lifetime explanation in Kotlin comments
- `environment.methods` -> `environment.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.iter` -> `values.Iter [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `iter_stop` in Kotlin comments; Iter.kt: Rust-only type/unsafe terminology in Kotlin comments; Iter.kt: snake_case identifier `def_iter` in Kotlin comments; Iter.kt: snake_case identifier `vec_map` in Kotlin comments; Iter.kt: Rust lifetime explanation in Kotlin comments; Iter.kt: snake_case identifier `small_map` in Kotlin comments
- `private` -> `starlark.Private [ZERO]`: function-by-function score forced to 0. Private.kt: snake_case identifier `missing_docs` in Kotlin comments
- `collections.symbol` -> `collections.Symbol [STUB]`: function-by-function score forced to 0. Symbol.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.avalue` -> `layout.AValue [ZERO]`: function-by-function score forced to 0. AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]`: function-by-function score forced to 0. ConstFrozenString.kt: snake_case identifier `constant_string` in Kotlin comments
- `typing.tuple` -> `typing.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `type_compiled_factory` in Kotlin comments
- `int.inline_int` -> `int.InlineInt [ZERO]`: function-by-function score forced to 0. InlineInt.kt: snake_case identifier `derive_more` in Kotlin comments; InlineInt.kt: Rust `fn` declaration in Kotlin comments; InlineInt.kt: Rust `pub` item in Kotlin comments; InlineInt.kt: Rust attribute syntax in Kotlin comments; InlineInt.kt: score-padding suppression annotation `@Suppress` in Kotlin code; InlineInt.kt: Rust lifetime explanation in Kotlin comments
- `int.pointer_i32` -> `int.PointerI32 [ZERO]`: function-by-function score forced to 0. PointerI32.kt: snake_case identifier `HAS_equals` in Kotlin code; PointerI32.kt: snake_case identifier `from_raw_pointer_unchecked` in Kotlin comments; PointerI32.kt: Rust `fn` declaration in Kotlin comments; PointerI32.kt: Rust `pub` item in Kotlin comments; PointerI32.kt: score-padding suppression annotation `@Suppress` in Kotlin code; PointerI32.kt: Rust lifetime explanation in Kotlin comments
- `layout.aligned_size` -> `layout.AlignedSize [ZERO]`: function-by-function score forced to 0. AlignedSize.kt: snake_case identifier `size_of` in Kotlin comments; AlignedSize.kt: Rust macro invocation in Kotlin comments; AlignedSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `eval.compiler` -> `eval.Compiler [ZERO]`: function-by-function score forced to 0. Compiler.kt: snake_case identifier `def_inline` in Kotlin comments
- `cast` -> `starlark.Cast [ZERO]`: function-by-function score forced to 0. Cast.kt: Rust lifetime explanation in Kotlin comments; Cast.kt: Rust-only type/unsafe terminology in Kotlin comments
- `types.bigint` -> `types.Bigint [ZERO]`: function-by-function score forced to 0. Bigint.kt: snake_case identifier `HAS_equals` in Kotlin code; Bigint.kt: snake_case identifier `non_zero_int` in Kotlin comments; Bigint.kt: Rust attribute syntax in Kotlin comments; Bigint.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Bigint.kt: Rust lifetime explanation in Kotlin comments
- `runtime.frozen_file_span` -> `runtime.FrozenFileSpan [ZERO]`: function-by-function score forced to 0. FrozenFileSpan.kt: snake_case identifier `empty_static` in Kotlin comments
- `runtime.small_duration` -> `runtime.SmallDuration [ZERO]`: function-by-function score forced to 0. SmallDuration.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `typing.typecheck` -> `typing.Typecheck [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies
- `dict.dict_type` -> `dict.DictType [ZERO]`: function-by-function score forced to 0. DictType.kt: snake_case identifier `starlark_type_repr` in Kotlin comments
- `values.freeze_error` -> `values.FreezeError [ZERO]`: function-by-function score forced to 0. FreezeError.kt: snake_case identifier `starlark_syntax` in Kotlin comments
- `layout.value_alloc_size` -> `layout.ValueAllocSize [ZERO]`: function-by-function score forced to 0. ValueAllocSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `compiler.stmt` -> `compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `build_file` in Kotlin comments
- `values.layout` -> `values.Layout [STUB]`: function-by-function score forced to 0. Layout.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `tests.def` -> `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. DefParamIndices.kt: snake_case identifier `num_positional` in Kotlin comments
- `types.array` -> `types.Array [ZERO]`: function-by-function score forced to 0. Array.kt: snake_case identifier `iter_count` in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `eval.bc` -> `bc.Bc [STUB]`: function-by-function score forced to 0. Bc.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]`: function-by-function score forced to 0. EnumType.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]`: function-by-function score forced to 0. StarlarkValueAsType.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `bc.frame` -> `bc.Frame [ZERO]`: function-by-function score forced to 0. Frame.kt: snake_case identifier `loop_indices` in Kotlin comments
- `values.value_of` -> `values.ValueOf [ZERO]`: function-by-function score forced to 0. ValueOf.kt: snake_case identifier `starlark_module` in Kotlin comments
- `record.record_type` -> `record.RecordType [ZERO]`: function-by-function score forced to 0. RecordType.kt: snake_case identifier `HAS_invoke` in Kotlin code; RecordType.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `alloc.chunk` -> `alloc.Chunk [ZERO]`: function-by-function score forced to 0. Chunk.kt: snake_case identifier `ref_count` in Kotlin comments; Chunk.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Chunk.kt: Rust-only type/unsafe terminology in Kotlin comments
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. CallStack.kt: snake_case identifier `call_stack` in Kotlin comments
- `errors.did_you_mean` -> `errors.DidYouMean [ZERO]`: function-by-function score forced to 0. DidYouMean.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `list.alloc` -> `list.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; no target functions found; report scoring is function-by-function only
- `profile.instant` -> `profile.Instant [ZERO]`: function-by-function score forced to 0. Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust attribute syntax in Kotlin comments
- `compiler.constants` -> `compiler.Constants [ZERO]`: function-by-function score forced to 0. Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]`: function-by-function score forced to 0. UnpackAndDiscard.kt: snake_case identifier `unpack_value_impl` in Kotlin comments; UnpackAndDiscard.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `sealed` -> `starlark.Sealed [ZERO]`: function-by-function score forced to 0. Sealed.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `types.record` -> `types.Record [ZERO]`: function-by-function score forced to 0. Record.kt: snake_case identifier `record_type` in Kotlin comments; Record.kt: Rust `pub` item in Kotlin comments; Record.kt: Rust `use` path in Kotlin comments
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]`: function-by-function score forced to 0. SmallVec1.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]`: function-by-function score forced to 0. OwnedFrozenRef.kt: translator-note comment (`Kotlin:`) in Kotlin comments; OwnedFrozenRef.kt: Rust lifetime explanation in Kotlin comments
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]`: function-by-function score forced to 0. ConstTypeId.kt: snake_case identifier `const_type_id` in Kotlin comments; ConstTypeId.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]`: function-by-function score forced to 0. RustLoc.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.str_` -> `avalues.Str [ZERO]`: function-by-function score forced to 0. Str.kt: snake_case identifier `alloc_str_init` in Kotlin comments; Str.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.stack_guard` -> `values.StackGuard [ZERO]`: function-by-function score forced to 0. StackGuard.kt: snake_case identifier `to_str` in Kotlin comments
- `profile.string_index` -> `profile.StringIndex [ZERO]`: function-by-function score forced to 0. StringIndex.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `collections.string_pool` -> `collections.StringPool [ZERO]`: function-by-function score forced to 0. StringPool.kt: snake_case identifier `debug_assert` in Kotlin comments; StringPool.kt: Rust macro invocation in Kotlin comments; StringPool.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]`: function-by-function score forced to 0. LocalAsValue.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; LocalAsValue.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.thin_box_slice_frozen_value` -> `values.ThinBoxSliceFrozenValue [STUB]`: function-by-function score forced to 0. ThinBoxSliceFrozenValue.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.arena` -> `heap.Arena [ZERO]`: function-by-function score forced to 0. Arena.kt: snake_case identifier `HAS_invoke` in Kotlin code; Arena.kt: snake_case identifier `non_drop` in Kotlin comments; Arena.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Arena.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arena.kt: Rust lifetime explanation in Kotlin comments
- `collections.alloca` -> `collections.Alloca [ZERO]`: function-by-function score forced to 0. Alloca.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `stdlib` -> `starlark.Stdlib [ZERO]`: function-by-function score forced to 0. Stdlib.kt: snake_case identifier `call_stack` in Kotlin comments
- `string.interpolation` -> `string.Interpolation [ZERO]`: function-by-function score forced to 0. Interpolation.kt: snake_case identifier `string_pool` in Kotlin comments
- `types.list_or_tuple` -> `types.ListOrTuple [ZERO]`: function-by-function score forced to 0. ListOrTuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `layout.pointer` -> `layout.Pointer [ZERO]`: function-by-function score forced to 0. Pointer.kt: snake_case identifier `get_user_tag` in Kotlin comments; Pointer.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Pointer.kt: Rust lifetime explanation in Kotlin comments
- `types.any_array` -> `types.AnyArray [ZERO]`: function-by-function score forced to 0. AnyArray.kt: score-padding suppression annotation `@Suppress` in Kotlin code; AnyArray.kt: translator-note comment (`Kotlin:`) in Kotlin comments; AnyArray.kt: Rust-only type/unsafe terminology in Kotlin comments
- `util.rtabort` -> `util.Rtabort [ZERO]`: function-by-function score forced to 0. Rtabort.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `bc.if_debug` -> `bc.IfDebug [ZERO]`: function-by-function score forced to 0. IfDebug.kt: snake_case identifier `debug_assertions` in Kotlin comments; IfDebug.kt: Rust attribute syntax in Kotlin comments; IfDebug.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `util.non_static_type_id` -> `util.NonStaticTypeId [ZERO]`: function-by-function score forced to 0. NonStaticTypeId.kt: Rust lifetime explanation in Kotlin comments; NonStaticTypeId.kt: Rust-only type/unsafe terminology in Kotlin comments
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]`: function-by-function score forced to 0. CheapCallStack.kt: translator-note comment (`Kotlin:`) in Kotlin comments; CheapCallStack.kt: Rust lifetime explanation in Kotlin comments
- `avalues.simple` -> `avalues.Simple [ZERO]`: function-by-function score forced to 0. Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments
- `record.field` -> `record.Field [ZERO]`: function-by-function score forced to 0. Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]`: function-by-function score forced to 0. UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments
- `typing.bindings` -> `typing.Bindings [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments
- `heap.fast_cell` -> `heap.FastCell [ZERO]`: function-by-function score forced to 0. FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust macro invocation in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments
- `read_line` -> `starlark.ReadLine [ZERO]`: function-by-function score forced to 0. ReadLine.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `typing.function` -> `typing.Function [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Function.kt: Rust attribute syntax in Kotlin comments
- `typing` -> `starlark.Typing [STUB]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `types.int` -> `types.Int [ZERO]`: function-by-function score forced to 0. Int.kt: snake_case identifier `num_bigint` in Kotlin comments
- `set.methods` -> `set.Methods [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `string.str_type` -> `string.StrType [ZERO]`: function-by-function score forced to 0. StrType.kt: snake_case identifier `starlark_value` in Kotlin comments; StrType.kt: Rust attribute syntax in Kotlin comments
- `int.int_or_big` -> `int.IntOrBig [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; IntOrBig.kt: snake_case identifier `from_str_radix` in Kotlin comments
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]`: function-by-function score forced to 0. ThinBox.kt: snake_case identifier `buck2_util` in Kotlin comments; ThinBox.kt: Rust-only type/unsafe terminology in Kotlin comments
- `set.value` -> `set.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.typing.callable` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]`: function-by-function score forced to 0. Callable.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `float.float` -> `float.Float [ZERO]`: function-by-function score forced to 0. Float.kt: snake_case identifier `HAS_equals` in Kotlin code; Float.kt: snake_case identifier `starlark_value` in Kotlin comments; Float.kt: Rust attribute syntax in Kotlin comments; Float.kt: Rust lifetime explanation in Kotlin comments
- `layout.typed` -> `layout.ValueTyped [ZERO]`: function-by-function score forced to 0. ValueTyped.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `scope.payload` -> `scope.Payload [ZERO]`: function-by-function score forced to 0. no target functions found; report scoring is function-by-function only
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]`: function-by-function score forced to 0. PackedImpl.kt: Rust-only type/unsafe terminology in Kotlin comments
- `string.repr` -> `string.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `is_printable` in Kotlin comments; Repr.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `list.value` -> `list.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `dict.value` -> `dict.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]`: function-by-function score forced to 0. VtableRegistry.kt: snake_case identifier `type_name` in Kotlin comments; VtableRegistry.kt: Rust lifetime explanation in Kotlin comments
- `record.globals` -> `record.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `rec_type` in Kotlin comments
- `heap.heap_type` -> `heap.HeapType [ZERO]`: function-by-function score forced to 0. HeapType.kt: snake_case identifier `into_ref` in Kotlin comments; HeapType.kt: score-padding suppression annotation `@Suppress` in Kotlin code; HeapType.kt: Rust lifetime explanation in Kotlin comments; HeapType.kt: Rust-only type/unsafe terminology in Kotlin comments
- `alloc.chain` -> `alloc.Chain [ZERO]`: function-by-function score forced to 0. Chain.kt: Rust attribute syntax in Kotlin comments
- `range.range_type` -> `range.RangeType [ZERO]`: function-by-function score forced to 0. RangeType.kt: snake_case identifier `HAS_iterate` in Kotlin code; RangeType.kt: snake_case identifier `saturating_mul` in Kotlin comments
- `stdlib.partial` -> `stdlib.Partial [ZERO]`: function-by-function score forced to 0. Partial.kt: snake_case identifier `HAS_invoke` in Kotlin code; Partial.kt: snake_case identifier `alloca_concat` in Kotlin comments
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]`: function-by-function score forced to 0. Compiled.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Compiled.kt: snake_case identifier `check_matches` in Kotlin comments
- `alloc.allocator` -> `alloc.Allocator [ZERO]`: function-by-function score forced to 0. Allocator.kt: Rust `pub` item in Kotlin comments; Allocator.kt: Rust lifetime explanation in Kotlin comments; Allocator.kt: Rust-only type/unsafe terminology in Kotlin comments
- `tuple.unpack` -> `tuple.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `tuple.value` -> `tuple.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code
- `profile.aggregated` -> `profile.Aggregated [ZERO]`: function-by-function score forced to 0. Aggregated.kt: snake_case identifier `string_index` in Kotlin comments
- `structs.value` -> `structs.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_equals` in Kotlin code; Value.kt: snake_case identifier `of_value` in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments
- `funcs.other` -> `funcs.Other [ZERO]`: function-by-function score forced to 0. Other.kt: Rust lifetime explanation in Kotlin comments
- `typed.string` -> `typed.String [ZERO]`: function-by-function score forced to 0. String.kt: snake_case identifier `HAS_equals` in Kotlin code
- `layout.complex` -> `layout.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `new_err` in Kotlin comments; Complex.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Complex.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Complex.kt: Rust lifetime explanation in Kotlin comments
- `dict.methods` -> `dict.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: Rust lifetime explanation in Kotlin comments
- `docs.parse` -> `docs.Parse [ZERO]`: function-by-function score forced to 0. Parse.kt: snake_case identifier `some_function` in Kotlin comments; Parse.kt: Rust attribute syntax in Kotlin comments
- `string.simd` -> `string.Simd [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Simd.kt: snake_case identifier `x86_64` in Kotlin comments; Simd.kt: snake_case identifier `find_hash_in_array_without_simd` in Kotlin comments
- `record.ty_record_type` -> `record.TyRecordType [ZERO]`: function-by-function score forced to 0. TyRecordType.kt: Rust `pub` item in Kotlin comments; TyRecordType.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `compiler.scope` -> `compiler.Scope [ZERO]`: function-by-function score forced to 0. Scope.kt: snake_case identifier `scope_resolver_globals` in Kotlin comments
- `assert.assert` -> `assert.Assert [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Assert.kt: snake_case identifier `assert_eq` in Kotlin comments; Assert.kt: Rust `pub` item in Kotlin comments; Assert.kt: Rust `use` path in Kotlin comments; Assert.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Assert.kt: Rust lifetime explanation in Kotlin comments
- `adapter.implementation` -> `adapter.Implementation [ZERO]`: function-by-function score forced to 0. Implementation.kt: snake_case identifier `before_stmt` in Kotlin comments
- `bc.instrs` -> `bc.Instrs [ZERO]`: function-by-function score forced to 0. Instrs.kt: snake_case identifier `drop_in_place` in Kotlin comments; Instrs.kt: Rust attribute syntax in Kotlin comments; Instrs.kt: Rust `match` expression in Kotlin comments; Instrs.kt: Rust lifetime explanation in Kotlin comments; Instrs.kt: Rust-only type/unsafe terminology in Kotlin comments
- `heap.send` -> `heap.Send [ZERO]`: function-by-function score forced to 0. Send.kt: Rust `fn` declaration in Kotlin comments; Send.kt: Rust lifetime explanation in Kotlin comments; Send.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.unpack` -> `list.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `unpack_value_opt` in Kotlin comments; Unpack.kt: Rust `let` binding in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments
- `bigint.convert` -> `bigint.Convert [ZERO]`: function-by-function score forced to 0. Convert.kt: Rust-only type/unsafe terminology in Kotlin comments
- `environment.modules` -> `environment.Modules [ZERO]`: function-by-function score forced to 0. Modules.kt: snake_case identifier `starlark_module` in Kotlin comments; Modules.kt: Rust `fn` declaration in Kotlin comments; Modules.kt: Rust `pub` item in Kotlin comments; Modules.kt: Rust attribute syntax in Kotlin comments; Modules.kt: Rust lifetime explanation in Kotlin comments
- `values.owned` -> `values.Owned [ZERO]`: function-by-function score forced to 0. Owned.kt: Rust lifetime explanation in Kotlin comments
- `profile.stmt` -> `profile.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `last_span` in Kotlin comments; Stmt.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.unpack` -> `values.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `starlark_module` in Kotlin comments
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]`: function-by-function score forced to 0. Bumpalo.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `typing.iter` -> `typing.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Iter.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `debug.inspect` -> `debug.Inspect [ZERO]`: function-by-function score forced to 0. Inspect.kt: snake_case identifier `call_stack` in Kotlin comments
- `params.spec` -> `params.Spec [ZERO]`: function-by-function score forced to 0. Spec.kt: snake_case identifier `no_args` in Kotlin comments
- `string.methods` -> `string.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `is_true` in Kotlin comments; Methods.kt: Rust lifetime explanation in Kotlin comments
- `typing.custom` -> `typing.Custom [ZERO]`: function-by-function score forced to 0. Custom.kt: snake_case identifier `validate_call` in Kotlin comments; Custom.kt: Rust `fn` declaration in Kotlin comments; Custom.kt: Rust `pub` item in Kotlin comments; Custom.kt: Rust-only type/unsafe terminology in Kotlin comments
- `heap.repr` -> `heap.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `forward_ptr` in Kotlin comments; Repr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.addr` -> `bc.Addr [ZERO]`: function-by-function score forced to 0. Addr.kt: snake_case identifier `debug_assert` in Kotlin comments; Addr.kt: Rust macro invocation in Kotlin comments
- `avalues.static_` -> `avalues.Static [ZERO]`: function-by-function score forced to 0. Static.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `profile.typecheck` -> `profile.Typecheck [ZERO]`: function-by-function score forced to 0. Typecheck.kt: snake_case identifier `by_function` in Kotlin comments
- `list.methods` -> `list.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Methods.kt: Rust `let` binding in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `params.parser` -> `params.Parser [ZERO]`: function-by-function score forced to 0. Parser.kt: snake_case identifier `get_next` in Kotlin comments
- `structs.alloc` -> `structs.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `fn` declaration in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments; Alloc.kt: Rust lifetime explanation in Kotlin comments; no target functions found; report scoring is function-by-function only
- `dict.alloc` -> `dict.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments
- `set.set` -> `set.Set [ZERO]`: function-by-function score forced to 0. Set.kt: snake_case identifier `starlark_module` in Kotlin comments; Set.kt: Rust attribute syntax in Kotlin comments
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]`: function-by-function score forced to 0. Matcher.kt: snake_case identifier `type_matcher` in Kotlin comments; Matcher.kt: Rust attribute syntax in Kotlin comments; Matcher.kt: Rust lifetime explanation in Kotlin comments; Matcher.kt: Rust-only type/unsafe terminology in Kotlin comments; Matcher.kt: Rust auto-trait terminology in Kotlin comments
- `list.refs` -> `list.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `from_value` in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments
- `symbol.map` -> `symbol.Map [ZERO]`: function-by-function score forced to 0. Map.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.opcode` -> `bc.Opcode [ZERO]`: function-by-function score forced to 0. Opcode.kt: snake_case identifier `dispatch_all` in Kotlin comments
- `bc.repr` -> `bc.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `align_of` in Kotlin comments
- `typing.never` -> `typing.Never [ZERO]`: function-by-function score forced to 0. Never.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Never.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]`: function-by-function score forced to 0. AllocUnpack.kt: snake_case identifier `alloc_frozen_string_value` in Kotlin comments
- `values.typing.ty` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]`: function-by-function score forced to 0. Ty.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `debug.evaluate` -> `debug.Evaluate [ZERO]`: function-by-function score forced to 0. Evaluate.kt: snake_case identifier `module_env` in Kotlin comments
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `eval_type` in Kotlin comments
- `compiler.expr` -> `compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `get_methods` in Kotlin comments; Expr.kt: Rust `let` binding in Kotlin comments
- `values.traits` -> `values.Traits [ZERO]`: function-by-function score forced to 0. Traits.kt: snake_case identifier `HAS_invoke` in Kotlin code; Traits.kt: snake_case identifier `starlark_value` in Kotlin comments; Traits.kt: Rust `let` binding in Kotlin comments; Traits.kt: Rust attribute syntax in Kotlin comments
- `compiler.def` -> `compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: snake_case identifier `HAS_invoke` in Kotlin code; Def.kt: unchecked cast suppression hiding transliteration work in Kotlin code; Def.kt: Rust-only type/unsafe terminology in Kotlin comments
- `types.function` -> `types.Function [ZERO]`: function-by-function score forced to 0. Function.kt: snake_case identifier `HAS_invoke` in Kotlin code; Function.kt: snake_case identifier `starlark_module` in Kotlin comments; Function.kt: Rust attribute syntax in Kotlin comments
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]`: function-by-function score forced to 0. StackPtr.kt: snake_case identifier `assert_eq` in Kotlin comments; StackPtr.kt: Rust macro invocation in Kotlin comments; StackPtr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]`: function-by-function score forced to 0. SummaryByFunction.kt: snake_case identifier `top_stack` in Kotlin comments
- `avalues.array` -> `avalues.Array [ZERO]`: function-by-function score forced to 0. Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.tuple` -> `avalues.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `register_special_avalue_frozen` in Kotlin comments; Tuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.complex` -> `avalues.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `alloc_complex` in Kotlin comments
- `eval.bc.compiler.stmt` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `set.refs` -> `set.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `unpack_value_opt` in Kotlin comments
- `bc.instr_arg` -> `bc.InstrArg [ZERO]`: function-by-function score forced to 0. InstrArg.kt: snake_case identifier `fmt_append` in Kotlin comments; InstrArg.kt: Rust-only type/unsafe terminology in Kotlin comments
- `structs.refs` -> `structs.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `debug_assert` in Kotlin comments
- `bc.call` -> `bc.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]`: function-by-function score forced to 0. CallEnterExit.kt: snake_case identifier `needs_drop` in Kotlin comments
- `values.type_repr` -> `values.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: snake_case identifier `type_repr` in Kotlin comments
- `list.globals` -> `list.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `register_list` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `values.index` -> `values.Index [ZERO]`: function-by-function score forced to 0. Index.kt: snake_case identifier `set_at` in Kotlin comments
- `intern.interner` -> `intern.Interner [ZERO]`: function-by-function score forced to 0. Interner.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `funcs.min_max` -> `funcs.MinMax [ZERO]`: function-by-function score forced to 0. MinMax.kt: snake_case identifier `update_max_ordering` in Kotlin comments; MinMax.kt: Rust `match` expression in Kotlin comments
- `typing.any` -> `typing.Any [ZERO]`: function-by-function score forced to 0. Any.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]`: function-by-function score forced to 0. MaybeUninitBackport.kt: snake_case identifier `write_slice_cloned` in Kotlin comments
- `build` -> `starlark.Build [ZERO]`: function-by-function score forced to 0. Build.kt: snake_case identifier `rust_nightly` in Kotlin comments; Build.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `bool.type_repr` -> `bool.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `debug.adapter` -> `debug.Adapter [ZERO]`: function-by-function score forced to 0. Adapter.kt: snake_case identifier `Requests_SetBreakpoints` in Kotlin comments
- `docs` -> `docs.Docs [ZERO]`: function-by-function score forced to 0. Docs.kt: Rust lifetime explanation in Kotlin comments
- `record.instance` -> `record.Instance [ZERO]`: function-by-function score forced to 0. Instance.kt: snake_case identifier `HAS_equals` in Kotlin code; Instance.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `alloc.per_thread` -> `alloc.PerThread [ZERO]`: function-by-function score forced to 0. PerThread.kt: snake_case identifier `thread_local` in Kotlin comments
- `profile.by_type` -> `profile.ByType [ZERO]`: function-by-function score forced to 0. ByType.kt: snake_case identifier `allocated_summary` in Kotlin comments
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]`: function-by-function score forced to 0. RecursiveReprOrJsonGuard.kt: snake_case identifier `to_json` in Kotlin comments
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]`: function-by-function score forced to 0. IfCompiler.kt: snake_case identifier `maybe_not` in Kotlin comments
- `eval.bc.compiler.call` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `structs.structs` -> `structs.Structs [ZERO]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `starlark_module` in Kotlin comments; Structs.kt: Rust attribute syntax in Kotlin comments
- `dict.globals` -> `dict.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `as_type` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `int.globals` -> `int.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `float.globals` -> `float.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `bc.writer` -> `bc.Writer [ZERO]`: function-by-function score forced to 0. Writer.kt: snake_case identifier `alloc_any` in Kotlin comments
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]`: function-by-function score forced to 0. FillTypesForLint.kt: snake_case identifier `of_value` in Kotlin comments; FillTypesForLint.kt: Rust `match` expression in Kotlin comments
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]`: function-by-function score forced to 0. Matchers.kt: snake_case identifier `starlark_type_id` in Kotlin comments; Matchers.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `docs.markdown` -> `docs.Markdown [ZERO]`: function-by-function score forced to 0. Markdown.kt: Rust `let` binding in Kotlin comments
- `eval.bc.compiler.expr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `mark_definitely_assigned_after` in Kotlin comments
- `environment.names` -> `environment.Names [ZERO]`: function-by-function score forced to 0. Names.kt: snake_case identifier `collect_defines_lvalue` in Kotlin comments
- `typing.error` -> `typing.Error [ZERO]`: function-by-function score forced to 0. Error.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.types` -> `compiler.Types [ZERO]`: function-by-function score forced to 0. Types.kt: snake_case identifier `expr_ident` in Kotlin comments
- `docs.code` -> `docs.Code [ZERO]`: function-by-function score forced to 0. Code.kt: snake_case identifier `render_as_code` in Kotlin comments
- `layout.static_string` -> `layout.StaticString [ZERO]`: function-by-function score forced to 0. StaticString.kt: Rust attribute syntax in Kotlin comments; StaticString.kt: Rust-only type/unsafe terminology in Kotlin comments
- `string.globals` -> `string.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]`: function-by-function score forced to 0. Remove.kt: Rust lifetime explanation in Kotlin comments
- `string.iter` -> `string.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_iterate` in Kotlin code; Iter.kt: snake_case identifier `produce_char` in Kotlin comments
- `__derive_refs.components` -> `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Components.kt: snake_case identifier `set_function` in Kotlin comments
- `compiler.error` -> `compiler.Error [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Error.kt: snake_case identifier `starlark_syntax` in Kotlin comments
- `fuzz_targets.starlark` -> `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Starlark.kt: snake_case identifier `fuzz_target` in Kotlin comments; Starlark.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.assign` -> `compiler.Assign [ZERO]`: function-by-function score forced to 0. Assign.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.def` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `environment` -> `starlark.Environment [ZERO]`: function-by-function score forced to 0. Environment.kt: snake_case identifier `module_dump` in Kotlin comments; Environment.kt: Rust `pub` item in Kotlin comments; Environment.kt: Rust `use` path in Kotlin comments
- `bc.for_loop` -> `bc.ForLoop [ZERO]`: function-by-function score forced to 0. ForLoop.kt: snake_case identifier `derive_more` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `syntax` -> `starlark.Syntax [STUB]`: function-by-function score forced to 0. Syntax.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.types` -> `values.Types [STUB]`: function-by-function score forced to 0. Types.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable` -> `starlark.Pagable [STUB]`: function-by-function score forced to 0. Pagable.kt: snake_case identifier `type_name` in Kotlin comments; Pagable.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing` -> `values.Typing [STUB]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.oracle` -> `typing.Oracle [STUB]`: function-by-function score forced to 0. Oracle.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `util` -> `starlark.Util [STUB]`: function-by-function score forced to 0. Util.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.avalues` -> `layout.AValues [STUB]`: function-by-function score forced to 0. AValues.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `errors` -> `starlark.Errors [STUB]`: function-by-function score forced to 0. Errors.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `macros` -> `starlark.Macros [ZERO]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `stdlib.funcs` -> `stdlib.Funcs [STUB]`: function-by-function score forced to 0. Funcs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `__derive_refs` -> `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. DeriveRefs.kt: snake_case identifier `derive_refs` in Kotlin comments; DeriveRefs.kt: Rust `pub` item in Kotlin comments; DeriveRefs.kt: Rust `use` path in Kotlin comments; DeriveRefs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]`: function-by-function score forced to 0. VtableRegister.kt: snake_case identifier `register_avalue_simple_frozen` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `eval.runtime` -> `eval.Runtime [STUB]`: function-by-function score forced to 0. Runtime.kt: score-padding suppression annotation `@Suppress` in Kotlin code

## Critical Issues (Function Similarity < 0.60 with Dependencies)

These files need immediate attention:

- **typing.starlark_value** → `typing.StarlarkValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 76
  - Functions: 29/34 matched (target 43)
  - Missing functions: `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`
  - Types: 4/4 matched (target 5)
  - Missing types: _none_
  - Scoring failure: StarlarkValue.kt: snake_case identifier `starlark_type_id` in Kotlin comments

- **runtime.evaluator** → `runtime.Evaluator [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 56
  - Functions: 58/60 matched (target 63)
  - Missing functions: `drop`, `add_diagnostics`
  - Types: 7/7 matched (target 17)
  - Missing types: _none_
  - Scoring failure: Evaluator.kt: snake_case identifier `before_stmt` in Kotlin comments; Evaluator.kt: Rust lifetime explanation in Kotlin comments
  - Lint issues: 3

- **values.trace** → `values.Trace [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 52
  - Functions: 1/1 matched (target 43)
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Trace.kt: Rust lifetime explanation in Kotlin comments; Trace.kt: Rust-only type/unsafe terminology in Kotlin comments

- **values.freeze** → `values.Freeze [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 42
  - Functions: 1/1 matched (target 31)
  - Missing functions: _none_
  - Types: 1/2 matched (target 6)
  - Missing types: `Frozen`
  - Scoring failure: Freeze.kt: Rust-only type/unsafe terminology in Kotlin comments

- **values.alloc_value** → `values.AllocValue [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 42
  - Functions: 2/2 matched (target 5)
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

- **values.frozen_ref** → `values.FrozenRef [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 27
  - Functions: 17/17 matched (target 23)
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

- **runtime.arguments** → `runtime.Arguments [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 25
  - Functions: 26/30 matched (target 49)
  - Missing functions: `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names`
  - Types: 8/8 matched (target 16)
  - Missing types: _none_
  - Scoring failure: Arguments.kt: snake_case identifier `to_string` in Kotlin comments; Arguments.kt: Rust macro invocation in Kotlin comments; Arguments.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arguments.kt: Rust lifetime explanation in Kotlin comments

- **typing.type_compiled** → `type_compiled.TypeCompiled [STUB]`
  - Function similarity: 0.00
  - Dependencies: 22
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: TypeCompiled.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **environment.globals** → `environment.Globals [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 21
  - Functions: 30/35 matched (target 38)
  - Missing functions: `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden`
  - Types: 5/5 matched
  - Missing types: _none_
  - Scoring failure: Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments

- **derive.module** → `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`
  - Function similarity: 0.00
  - Dependencies: 21
  - Functions: 0/0 matched (target 21)
  - Missing functions: _none_
  - Types: 0/0 matched (target 3)
  - Missing types: _none_
  - Scoring failure: no source functions found; target defines functions; report scoring is function-by-function only
  - Lint issues: 1

- **values.value_of_unchecked** → `values.ValueOfUnchecked [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 20
  - Functions: 12/18 matched (target 17)
  - Missing functions: `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant`
  - Types: 3/7 matched (target 4)
  - Missing types: `Canonical`, `Frozen`, `Error`, `ReprNotSendSync`
  - Scoring failure: ValueOfUnchecked.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; ValueOfUnchecked.kt: Rust `fn` declaration in Kotlin comments; ValueOfUnchecked.kt: score-padding suppression annotation `@Suppress` in Kotlin code; ValueOfUnchecked.kt: Rust lifetime explanation in Kotlin comments

- **util.refcell** → `refcell.RefCell`
  - Function similarity: 0.32
  - Dependencies: 20
  - Functions: 1/2 matched (target 11)
  - Missing functions: `test_unleak_borrow`
  - Types: 0/0 matched (target 3)
  - Missing types: _none_

- **environment.methods** → `environment.Methods [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 17
  - Functions: 17/19 matched (target 21)
  - Missing functions: `test_set_attribute`, `get_methods`
  - Types: 3/4 matched (target 3)
  - Missing types: `Magic`
  - Scoring failure: Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **values.iter** → `values.Iter [ZERO] [PROVENANCE-FALLBACK]`
  - Function similarity: 0.00
  - Dependencies: 17
  - Functions: 4/5 matched (target 84)
  - Missing functions: `drop`
  - Types: 1/2 matched (target 14)
  - Missing types: `Item`
  - Scoring failure: Iter.kt: snake_case identifier `iter_stop` in Kotlin comments; Iter.kt: Rust-only type/unsafe terminology in Kotlin comments; Iter.kt: snake_case identifier `def_iter` in Kotlin comments; Iter.kt: snake_case identifier `vec_map` in Kotlin comments; Iter.kt: Rust lifetime explanation in Kotlin comments; Iter.kt: snake_case identifier `small_map` in Kotlin comments
  - Lint issues: 5

- **private** → `starlark.Private [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 15
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: Private.kt: snake_case identifier `missing_docs` in Kotlin comments

- **collections.symbol** → `collections.Symbol [STUB]`
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
  - Functions: 6/8 matched (target 11)
  - Missing functions: `tuple_cycle_freeze`, `test_try_freeze_directly`
  - Types: 3/3 matched
  - Missing types: _none_
  - Scoring failure: AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments

- **layout.const_frozen_string** → `layout.ConstFrozenString [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 12
  - Functions: 0/2 matched (target 1)
  - Missing functions: `test_const_frozen_string_for_short_strings`, `test_const_frozen_string`
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: ConstFrozenString.kt: snake_case identifier `constant_string` in Kotlin comments

- **typing.tuple** → `typing.Tuple [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 12
  - Functions: 5/6 matched (target 9)
  - Missing functions: `fmt`
  - Types: 1/1 matched (target 3)
  - Missing types: _none_
  - Scoring failure: Tuple.kt: snake_case identifier `type_compiled_factory` in Kotlin comments

- **int.inline_int** → `int.InlineInt [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 11
  - Functions: 25/34 matched (target 43)
  - Missing functions: `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits`
  - Types: 2/5 matched (target 6)
  - Missing types: `Error`, `Output`, `Canonical`
  - Scoring failure: InlineInt.kt: snake_case identifier `derive_more` in Kotlin comments; InlineInt.kt: Rust `fn` declaration in Kotlin comments; InlineInt.kt: Rust `pub` item in Kotlin comments; InlineInt.kt: Rust attribute syntax in Kotlin comments; InlineInt.kt: score-padding suppression annotation `@Suppress` in Kotlin code; InlineInt.kt: Rust lifetime explanation in Kotlin comments

- **int.pointer_i32** → `int.PointerI32 [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 9
  - Functions: 28/31 matched (target 34)
  - Missing functions: `eq`, `fmt`, `serialize`
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

- **any** → `starlark.Any`
  - Function similarity: 0.04
  - Dependencies: 8
  - Functions: 2/12 matched (target 3)
  - Missing functions: `static_type_id`, `static_type_of`, `is`, `test_can_convert`, `convert_value`, `convert_any`, `test_any_lifetime`, `test`, `test_provides_static_type_id`, `test_provides_static_type_when_type_parameter_has_bound_with_lifetime`
  - Types: 3/15 matched (target 37)
  - Missing types: `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar`

- **layout.aligned_size** → `layout.AlignedSize [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 8
  - Functions: 6/13 matched (target 15)
  - Missing functions: `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`
  - Scoring failure: AlignedSize.kt: snake_case identifier `size_of` in Kotlin comments; AlignedSize.kt: Rust macro invocation in Kotlin comments; AlignedSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments

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
  - Functions: 29/73 matched (target 35)
  - Missing functions: `unpack_integer`, `eq`, `test_parse`, `test_str`, `test_repr`, `test_equals`, `test_plus`, `test_compare_big_big`, `test_compare_big_small`, `test_compare_big_float`, `test_add_big`, `test_add_big_small`, `test_add_big_float`, `test_mul_big`, `test_mul_big_small`, `test_mul_big_float`, `test_div_big`, `test_div_big_small`, `test_div_big_float`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_big_float`, `test_percent_big`, `test_percent_big_small`, `test_percent_big_float`, `test_bit_and_big`, `test_bit_and_big_small`, `test_bit_and_float`, `test_bit_or_big`, `test_bit_or_big_small`, `test_bit_or_float`, `test_bit_xor_big`, `test_bit_xor_big_small`, `test_bit_xor_float`, `test_bit_not`, `test_left_shift`, `test_left_shift_small`, `test_left_shift_float`, `test_right_shift`, `test_right_shift_small`, `test_right_shift_float`, `test_int_function`, `test_hash`, `test_int_type_matches_bigint`
  - Types: 1/1 matched
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

- **runtime.small_duration** → `runtime.SmallDuration [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 4/7 matched (target 9)
  - Missing functions: `from_millis`, `add_assign`, `add`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`
  - Scoring failure: SmallDuration.kt: translator-note comment (`Kotlin:`) in Kotlin comments

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

- **values.freeze_error** → `values.FreezeError [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 3/4 matched (target 6)
  - Missing functions: `from`
  - Types: 3/4 matched (target 3)
  - Missing types: `FreezeResult`
  - Scoring failure: FreezeError.kt: snake_case identifier `starlark_syntax` in Kotlin comments

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
  - Functions: 25/25 matched (target 28)
  - Missing functions: _none_
  - Types: 7/7 matched (target 24)
  - Missing types: _none_
  - Scoring failure: Stmt.kt: snake_case identifier `build_file` in Kotlin comments

- **values.layout** → `values.Layout [STUB]`
  - Function similarity: 0.00
  - Dependencies: 6
  - Functions: 0/0 matched
  - Missing functions: _none_
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: Layout.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **tests.def** → `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]`
  - Function similarity: 0.00
  - Dependencies: 5
  - Functions: 0/14 matched (target 4)
  - Missing functions: `test_lambda`, `test_frozen_lambda`, `test_nested_def_1`, `test_nested_def_2`, `test_nested_def_3`, `test_lambda_capture_from_module`, `test_lambda_capture_from_def`, `test_lambda_capture_reassigned_from_def`, `test_def_freeze`, `test_frozen_lambda_nest`, `test_context_captured`, `test_lambda_errors`, `test_lambda_errors_nested`, `test_double_capture_and_freeze`
  - Types: 0/0 matched (target 1)
  - Missing types: _none_
  - Scoring failure: DefParamIndices.kt: snake_case identifier `num_positional` in Kotlin comments
  - Lint issues: 1

- **types.array** → `types.Array [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 5
  - Functions: 23/32 matched (target 24)
  - Missing functions: `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`, `debug`, `display`
  - Types: 2/2 matched
  - Missing types: _none_
  - Scoring failure: Array.kt: snake_case identifier `iter_count` in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **typing.interface** → `typing.Interface`
  - Function similarity: 0.60
  - Dependencies: 5
  - Functions: 3/3 matched
  - Missing functions: _none_
  - Types: 1/1 matched
  - Missing types: _none_

- **eval.bc** → `bc.Bc [STUB]`
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
  - Functions: 21/36 matched (target 24)
  - Missing functions: `fmt`, `r#type`, `values`, `test_enum_type_as_type_pass`, `test_enum_type_fail_runtime`, `test_enum_type_fail_compile_time`, `test_enum_is_callable`, `test_enum_value_index`, `test_enum_value_index_correct_type`, `test_enum_index`, `test_enum_index_fail`, `test_enum_call`, `test_enum_attribute_access`, `test_enum_attribute_access_invalid`, `test_enum_attribute_access_type`
  - Types: 4/8 matched (target 6)
  - Missing types: `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical`
  - Scoring failure: EnumType.kt: snake_case identifier `HAS_invoke` in Kotlin code
  - Lint issues: 3

- **types.starlark_value_as_type** → `types.StarlarkValueAsType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 6/13 matched (target 8)
  - Missing functions: `fmt`, `new`, `compiler_args_globals`, `compiler_args`, `test_pass`, `test_fail_compile_time`, `test_fail_runtime`
  - Types: 2/4 matched (target 2)
  - Missing types: `Canonical`, `CompilerArgs`
  - Scoring failure: StarlarkValueAsType.kt: snake_case identifier `HAS_eval_type` in Kotlin code

- **bc.frame** → `bc.Frame [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 16/24 matched (target 31)
  - Missing functions: `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit`
  - Types: 2/2 matched
  - Missing types: _none_
  - Scoring failure: Frame.kt: snake_case identifier `loop_indices` in Kotlin comments

- **values.demand** → `values.Demand`
  - Function similarity: 0.37
  - Dependencies: 4
  - Functions: 4/7 matched (target 5)
  - Missing functions: `payload`, `provide`, `test_trait_downcast`
  - Types: 1/4 matched (target 1)
  - Missing types: `SomeTrait`, `StaticType`, `MyValue`

- **values.value_of** → `values.ValueOf [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 4
  - Functions: 4/6 matched (target 5)
  - Missing functions: `deref`, `fmt`
  - Types: 1/4 matched (target 1)
  - Missing types: `Target`, `Canonical`, `Error`
  - Scoring failure: ValueOf.kt: snake_case identifier `starlark_module` in Kotlin comments

- **profile.alloc_counts** → `profile.AllocCounts`
  - Function similarity: 0.40
  - Dependencies: 4
  - Functions: 1/4 matched (target 5)
  - Missing functions: `normalize_for_golden_tests`, `add_assign`, `add`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`

- **bc.native_function** → `bc.NativeFunction`
  - Function similarity: 0.51
  - Dependencies: 4
  - Functions: 3/4 matched
  - Missing functions: `fun`
  - Types: 1/1 matched
  - Missing types: _none_

- **types.ellipsis** → `types.Ellipsis`
  - Function similarity: 0.55
  - Dependencies: 4
  - Functions: 2/3 matched (target 4)
  - Missing functions: `test_ellipsis`
  - Types: 1/1 matched
  - Missing types: _none_

- **record.record_type** → `record.RecordType [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 15/22 matched (target 18)
  - Missing functions: `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error`
  - Types: 2/8 matched (target 2)
  - Missing types: `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
  - Scoring failure: RecordType.kt: snake_case identifier `HAS_invoke` in Kotlin code; RecordType.kt: translator-note comment (`Kotlin:`) in Kotlin comments
  - Lint issues: 1

- **alloc.chunk** → `alloc.Chunk [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 11/19 matched (target 18)
  - Missing functions: `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`, `test_alloc_release`
  - Types: 2/3 matched (target 2)
  - Missing types: `ChunkDataEmpty`
  - Scoring failure: Chunk.kt: snake_case identifier `ref_count` in Kotlin comments; Chunk.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Chunk.kt: Rust-only type/unsafe terminology in Kotlin comments

- **stdlib.call_stack** → `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 7/13 matched (target 14)
  - Missing functions: `fmt`, `global`, `test_simple`, `test_strip_one`, `test_strip_all`, `test_call_stack_frame`
  - Types: 1/1 matched (target 2)
  - Missing types: _none_
  - Scoring failure: CallStack.kt: snake_case identifier `call_stack` in Kotlin comments
  - Lint issues: 1

- **errors.did_you_mean** → `errors.DidYouMean [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 1/6 matched (target 2)
  - Missing functions: `prefixes`, `typos`, `best`, `very_short`, `earlier_variants_are_more_important`
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: DidYouMean.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **list.alloc** → `list.Alloc [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 0/3 matched (target 0)
  - Missing functions: `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
  - Types: 1/2 matched (target 1)
  - Missing types: `Canonical`
  - Scoring failure: Alloc.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; no target functions found; report scoring is function-by-function only

- **list.list_type** → `list.ListType`
  - Function similarity: 0.37
  - Dependencies: 3
  - Functions: 1/2 matched (target 5)
  - Missing functions: `unpack_value_impl`
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `Error`

- **profile.instant** → `profile.Instant [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 3/4 matched (target 9)
  - Missing functions: `sub`
  - Types: 1/2 matched (target 1)
  - Missing types: `Output`
  - Scoring failure: Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust attribute syntax in Kotlin comments

- **compiler.constants** → `compiler.Constants [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 1/3 matched (target 5)
  - Missing functions: `eq`, `test_constants`
  - Types: 2/2 matched
  - Missing types: _none_
  - Scoring failure: Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments

- **values.unpack_and_discard** → `values.UnpackAndDiscard [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 3
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `Error`
  - Scoring failure: UnpackAndDiscard.kt: snake_case identifier `unpack_value_impl` in Kotlin comments; UnpackAndDiscard.kt: translator-note comment (`Kotlin:`) in Kotlin comments

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
  - Function similarity: 0.42
  - Dependencies: 2
  - Functions: 5/10 matched (target 9)
  - Missing functions: `fmt`, `eq`, `partial_cmp`, `cmp`, `hash`
  - Types: 2/3 matched (target 4)
  - Missing types: `Target`

- **typing.type_type** → `typing.TypeType`
  - Function similarity: 0.27
  - Dependencies: 2
  - Functions: 2/5 matched (target 3)
  - Missing functions: `test`, `module`, `takes_type`
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `Error`

- **values.owned_frozen_ref** → `values.OwnedFrozenRef [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 10/12 matched (target 19)
  - Missing functions: `fmt`, `deref`
  - Types: 2/3 matched (target 2)
  - Missing types: `Target`
  - Scoring failure: OwnedFrozenRef.kt: translator-note comment (`Kotlin:`) in Kotlin comments; OwnedFrozenRef.kt: Rust lifetime explanation in Kotlin comments

- **layout.const_type_id** → `layout.ConstTypeId [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 2/5 matched (target 4)
  - Missing functions: `fmt`, `eq`, `hash`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: ConstTypeId.kt: snake_case identifier `const_type_id` in Kotlin comments; ConstTypeId.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **runtime.rust_loc** → `runtime.RustLoc [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 0/3 matched (target 1)
  - Missing functions: `rust_loc_globals`, `invoke`, `test_rust_loc`
  - Types: 0/0 matched
  - Missing types: _none_
  - Scoring failure: RustLoc.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **avalues.str_** → `avalues.Str [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 11/11 matched (target 15)
  - Missing functions: _none_
  - Types: 1/3 matched (target 1)
  - Missing types: `StarlarkValue`, `ExtraElem`
  - Scoring failure: Str.kt: snake_case identifier `alloc_str_init` in Kotlin comments; Str.kt: Rust-only type/unsafe terminology in Kotlin comments

- **values.stack_guard** → `values.StackGuard [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 2
  - Functions: 3/4 matched
  - Missing functions: `drop`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: StackGuard.kt: snake_case identifier `to_str` in Kotlin comments

- **collections.aligned_padded_str** → `alignedpaddedstr.AlignedPaddedStr`
  - Function similarity: 0.34
  - Dependencies: 2
  - Functions: 2/3 matched (target 4)
  - Missing functions: `eq`
  - Types: 1/1 matched
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

- **values.thin_box_slice_frozen_value** → `values.ThinBoxSliceFrozenValue [STUB]`
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
  - Functions: 5/22 matched (target 5)
  - Missing functions: `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat`
  - Types: 1/4 matched (target 1)
  - Missing types: `Buffer`, `Align`, `DropSliceGuard`
  - Scoring failure: Alloca.kt: score-padding suppression annotation `@Suppress` in Kotlin code

- **stdlib** → `starlark.Stdlib [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 3/14 matched (target 3)
  - Missing functions: `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
  - Types: 1/3 matched (target 1)
  - Missing types: `Bool2`, `Error`
  - Scoring failure: Stdlib.kt: snake_case identifier `call_stack` in Kotlin comments

- **string.interpolation** → `string.Interpolation [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 4/12 matched (target 6)
  - Missing functions: `test_incomplete_format`, `test_unsupported_format_character`, `test_parse_percent_s_one`, `test_type_support_d`, `test_type_support_o`, `test_type_support_x`, `test_type_support_e`, `test_int_min`
  - Types: 4/4 matched (target 20)
  - Missing types: _none_
  - Scoring failure: Interpolation.kt: snake_case identifier `string_pool` in Kotlin comments

- **types.list_or_tuple** → `types.ListOrTuple [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 1/5 matched
  - Missing functions: `default`, `starlark_type_repr`, `into_iter`, `test_unpack`
  - Types: 1/5 matched (target 1)
  - Missing types: `Canonical`, `Error`, `Item`, `IntoIter`
  - Scoring failure: ListOrTuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **layout.pointer** → `layout.Pointer [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 25/32 matched (target 46)
  - Missing functions: `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `test_int_tag`, `check`
  - Types: 5/5 matched
  - Missing types: _none_
  - Scoring failure: Pointer.kt: snake_case identifier `get_user_tag` in Kotlin comments; Pointer.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Pointer.kt: Rust lifetime explanation in Kotlin comments

- **stdlib.breakpoint** → `stdlib.Breakpoint`
  - Function similarity: 0.45
  - Dependencies: 1
  - Functions: 11/17 matched (target 13)
  - Missing functions: `global`, `breakpoint`, `reset_global_state`, `test_breakpoint_real`, `test_breakpoint_mock`, `test_breakpoint_disabled`
  - Types: 5/6 matched
  - Missing types: `Handler`
  - Lint issues: 1

- **types.any_complex** → `types.AnyComplex`
  - Function similarity: 0.49
  - Dependencies: 1
  - Functions: 4/7 matched
  - Missing functions: `fmt`, `test_any_complex`, `freeze`
  - Types: 1/5 matched (target 1)
  - Missing types: `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData`

- **types.any_array** → `types.AnyArray [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 3/7 matched
  - Missing functions: `fmt`, `drop`, `test_drop`, `test_allocation_size`
  - Types: 1/3 matched (target 1)
  - Missing types: `Canonical`, `IncrementOnDrop`
  - Scoring failure: AnyArray.kt: score-padding suppression annotation `@Suppress` in Kotlin code; AnyArray.kt: translator-note comment (`Kotlin:`) in Kotlin comments; AnyArray.kt: Rust-only type/unsafe terminology in Kotlin comments

- **util.rtabort** → `util.Rtabort [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 2/6 matched (target 3)
  - Missing functions: `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort`
  - Types: 0/1 matched (target 0)
  - Missing types: `AbortOnDrop`
  - Scoring failure: Rtabort.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **string.dot_format** → `string.DotFormat`
  - Function similarity: 0.43
  - Dependencies: 1
  - Functions: 7/11 matched (target 7)
  - Missing functions: `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one`
  - Types: 1/1 matched
  - Missing types: _none_

- **bc.if_debug** → `bc.IfDebug [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 5/8 matched (target 9)
  - Missing functions: `eq`, `partial_cmp`, `cmp`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: IfDebug.kt: snake_case identifier `debug_assertions` in Kotlin comments; IfDebug.kt: Rust attribute syntax in Kotlin comments; IfDebug.kt: translator-note comment (`Kotlin:`) in Kotlin comments

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
  - Functions: 8/8 matched (target 11)
  - Missing functions: _none_
  - Types: 1/3 matched (target 1)
  - Missing types: `StarlarkValue`, `ExtraElem`
  - Scoring failure: Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments

- **record.field** → `record.Field [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 4/5 matched (target 10)
  - Missing functions: `fmt`
  - Types: 0/1 matched
  - Missing types: `FieldGen`
  - Scoring failure: Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments

- **structs.unordered_hasher** → `structs.UnorderedHasher [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 3/5 matched (target 3)
  - Missing functions: `_write`, `test_unordered_hasher`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments

- **typing.bindings** → `typing.Bindings [STUB]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 7/8 matched (target 18)
  - Missing functions: `get_for_clause`
  - Types: 3/3 matched (target 18)
  - Missing types: _none_
  - Scoring failure: target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments

- **heap.fast_cell** → `heap.FastCell [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 6/7 matched
  - Missing functions: `drop`
  - Types: 1/1 matched
  - Missing types: _none_
  - Scoring failure: FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust macro invocation in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments

- **read_line** → `starlark.ReadLine [ZERO]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 2/2 matched
  - Missing functions: _none_
  - Types: 1/2 matched (target 1)
  - Missing types: `NoRustyline`
  - Scoring failure: ReadLine.kt: translator-note comment (`Kotlin:`) in Kotlin comments

- **typing.function** → `typing.Function [STUB]`
  - Function similarity: 0.00
  - Dependencies: 1
  - Functions: 12/12 matched (target 24)
  - Missing functions: _none_
  - Types: 3/3 matched
  - Missing types: _none_
  - Scoring failure: target contains TODO/stub/placeholder markers in function bodies; Function.kt: Rust attribute syntax in Kotlin comments
  - TODOs: 1
  - Lint issues: 2

- **typing** → `starlark.Typing [STUB]`
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
| 1 | `layout.heap` | `values.layout.heap.Heap` | 109 | 0 | 0 | 0 | `src/values/layout/heap.rs` | `values/layout/heap/Heap.kt` |
| 2 | `assert` | `assert.Assert` | 84 | 0 | 0 | 0 | `src/assert.rs` | `assert/Assert.kt` |
| 3 | `debug` | `debug.Debug` | 53 | 0 | 0 | 0 | `src/debug.rs` | `debug/Debug.kt` |
| 4 | `derive.unpack_value` | `tests.derive.UnpackValue` | 51 | 2 | 5 | 7 | `src/tests/derive/unpack_value.rs` | `tests/derive/UnpackValue.kt` |
| 5 | `coerce` | `Coerce` | 34 | 5 | 9 | 14 | `src/coerce.rs` | `Coerce.kt` |
| 6 | `util.arc_str` | `util.ArcStr` | 21 | 5 | 2 | 7 | `src/util/arc_str.rs` | `util/ArcStr.kt` |
| 7 | `types.dict` | `values.types.dict.Dict` | 12 | 0 | 0 | 0 | `src/values/types/dict.rs` | `values/types/dict/Dict.kt` |
| 8 | `bc.expr` | `tests.bc.Expr` | 7 | 7 | 0 | 7 | `src/tests/bc/expr.rs` | `tests/bc/Expr.kt` |
| 9 | `types.range` | `values.types.range.Range` | 5 | 0 | 0 | 0 | `src/values/types/range.rs` | `values/types/range/Range.kt` |
| 10 | `types.namespace` | `values.types.namespace.Namespace` | 3 | 0 | 0 | 0 | `src/values/types/namespace.rs` | `values/types/namespace/Namespace.kt` |
| 11 | `types.num` | `values.types.num.Num` | 2 | 0 | 0 | 0 | `src/values/types/num.rs` | `values/types/num/Num.kt` |
| 12 | `types.list` | `values.types.list.List` | 2 | 0 | 0 | 0 | `src/values/types/list.rs` | `values/types/list/List.kt` |
| 13 | `types.float` | `values.types.float.Float` | 2 | 0 | 0 | 0 | `src/values/types/float.rs` | `values/types/float/Float.kt` |
| 14 | `types.string` | `values.types.string.String` | 1 | 0 | 0 | 0 | `src/values/types/string.rs` | `values/types/string/String.kt` |
| 15 | `types.set` | `values.types.set.Set` | 1 | 0 | 0 | 0 | `src/values/types/set.rs` | `values/types/set/Set.kt` |
| 16 | `types.none` | `values.types.none.None` | 1 | 0 | 0 | 0 | `src/values/types/none.rs` | `values/types/none/None.kt` |
| 17 | `types.enumeration` | `values.types.enumeration.Enumeration` | 1 | 0 | 0 | 0 | `src/values/types/enumeration.rs` | `values/types/enumeration/Enumeration.kt` |
| 18 | `tests` | `tests.Tests` | 1 | 0 | 0 | 0 | `src/tests.rs` | `tests/Tests.kt` |
| 19 | `tests.before_stmt` | `tests.BeforeStmt` | 1 | 1 | 0 | 1 | `src/tests/before_stmt.rs` | `tests/BeforeStmt.kt` |
| 20 | `analysis.unused_loads` | `analysis.unusedloads.UnusedLoads` | 0 | 0 | 0 | 0 | `src/analysis/unused_loads.rs` | `analysis/unusedloads/UnusedLoads.kt` |
| 21 | `type_compiled.tests` | `values.typing.typecompiled.Tests` | 0 | 13 | 0 | 13 | `src/values/typing/type_compiled/tests.rs` | `values/typing/typecompiled/Tests.kt` |
| 22 | `tests.bc.call` | `tests.bc.Call` | 0 | 1 | 0 | 1 | `src/tests/bc/call.rs` | `tests/bc/Call.kt` |
| 23 | `bc.compr` | `tests.bc.Compr` | 0 | 4 | 0 | 4 | `src/tests/bc/compr.rs` | `tests/bc/Compr.kt` |
| 24 | `tests.bc.definitely_assigned` | `tests.bc.DefinitelyAssigned` | 0 | 5 | 0 | 5 | `src/tests/bc/definitely_assigned.rs` | `tests/bc/DefinitelyAssigned.kt` |
| 25 | `tests.bc` | `tests.bc.Bc` | 0 | 0 | 0 | 0 | `src/tests/bc.rs` | `tests/bc/Bc.kt` |
| 26 | `bc.for_stmt` | `tests.bc.ForStmt` | 0 | 3 | 0 | 3 | `src/tests/bc/for_stmt.rs` | `tests/bc/ForStmt.kt` |
| 27 | `bc.golden` | `tests.bc.Golden` | 0 | 2 | 0 | 2 | `src/tests/bc/golden.rs` | `tests/bc/Golden.kt` |
| 28 | `bc.if_stmt` | `tests.bc.IfStmt` | 0 | 12 | 0 | 12 | `src/tests/bc/if_stmt.rs` | `tests/bc/IfStmt.kt` |
| 29 | `bc.isinstance` | `tests.bc.Isinstance` | 0 | 1 | 0 | 1 | `src/tests/bc/isinstance.rs` | `tests/bc/Isinstance.kt` |
| 30 | `tests.basic` | `tests.Basic` | 0 | 12 | 0 | 12 | `src/tests/basic.rs` | `tests/Basic.kt` |
| 31 | `tests.call` | `tests.Call` | 0 | 20 | 0 | 20 | `src/tests/call.rs` | `tests/Call.kt` |
| 32 | `tests.comprehension` | `tests.Comprehension` | 0 | 10 | 0 | 10 | `src/tests/comprehension.rs` | `tests/Comprehension.kt` |
| 33 | `tests.derive` | `tests.derive.Derive` | 0 | 0 | 0 | 0 | `src/tests/derive.rs` | `tests/derive/Derive.kt` |
| 34 | `derive.alloc_value` | `tests.derive.AllocValue` | 0 | 0 | 4 | 4 | `src/tests/derive/alloc_value.rs` | `tests/derive/AllocValue.kt` |
| 35 | `derive.attrs` | `tests.derive.Attrs` | 0 | 1 | 2 | 3 | `src/tests/derive/attrs.rs` | `tests/derive/Attrs.kt` |
| 36 | `derive.docs` | `tests.derive.Docs` | 0 | 7 | 2 | 9 | `src/tests/derive/docs.rs` | `tests/derive/Docs.kt` |
| 37 | `derive.freeze` | `tests.derive.freeze.Freeze` | 0 | 0 | 0 | 0 | `src/tests/derive/freeze.rs` | `tests/derive/freeze/Freeze.kt` |
| 38 | `freeze.basic` | `tests.derive.freeze.Basic` | 0 | 0 | 1 | 1 | `src/tests/derive/freeze/basic.rs` | `tests/derive/freeze/Basic.kt` |
| 39 | `freeze.bounds` | `tests.derive.freeze.Bounds` | 0 | 3 | 3 | 6 | `src/tests/derive/freeze/bounds.rs` | `tests/derive/freeze/Bounds.kt` |
| 40 | `freeze.enums` | `tests.derive.freeze.Enums` | 0 | 0 | 1 | 1 | `src/tests/derive/freeze/enums.rs` | `tests/derive/freeze/Enums.kt` |
| 41 | `freeze.identity` | `tests.derive.freeze.Identity` | 0 | 2 | 4 | 6 | `src/tests/derive/freeze/identity.rs` | `tests/derive/freeze/Identity.kt` |
| 42 | `freeze.validator` | `tests.derive.freeze.Validator` | 0 | 3 | 1 | 4 | `src/tests/derive/freeze/validator.rs` | `tests/derive/freeze/Validator.kt` |
| 43 | `freeze.validator_order` | `tests.derive.freeze.ValidatorOrder` | 0 | 3 | 3 | 6 | `src/tests/derive/freeze/validator_order.rs` | `tests/derive/freeze/ValidatorOrder.kt` |
| 44 | `module.basic` | `tests.derive.module.Basic` | 0 | 9 | 0 | 9 | `src/tests/derive/module/basic.rs` | `tests/derive/module/Basic.kt` |
| 45 | `module.default_value` | `tests.derive.module.DefaultValue` | 0 | 3 | 0 | 3 | `src/tests/derive/module/default_value.rs` | `tests/derive/module/DefaultValue.kt` |
| 46 | `module.generic` | `tests.derive.module.Generic` | 0 | 8 | 2 | 10 | `src/tests/derive/module/generic.rs` | `tests/derive/module/Generic.kt` |
| 47 | `module.kwargs` | `tests.derive.module.Kwargs` | 0 | 4 | 0 | 4 | `src/tests/derive/module/kwargs.rs` | `tests/derive/module/Kwargs.kt` |
| 48 | `module.methods` | `tests.derive.module.Methods` | 0 | 5 | 1 | 6 | `src/tests/derive/module/methods.rs` | `tests/derive/module/Methods.kt` |
| 49 | `module.named_positional` | `tests.derive.module.NamedPositional` | 0 | 11 | 0 | 11 | `src/tests/derive/module/named_positional.rs` | `tests/derive/module/NamedPositional.kt` |
| 50 | `module.other_attributes` | `tests.derive.module.OtherAttributes` | 0 | 6 | 0 | 6 | `src/tests/derive/module/other_attributes.rs` | `tests/derive/module/OtherAttributes.kt` |
| 51 | `module.return_impl` | `tests.derive.module.ReturnImpl` | 0 | 4 | 0 | 4 | `src/tests/derive/module/return_impl.rs` | `tests/derive/module/ReturnImpl.kt` |
| 52 | `module.special_params` | `tests.derive.module.SpecialParams` | 0 | 3 | 0 | 3 | `src/tests/derive/module/special_params.rs` | `tests/derive/module/SpecialParams.kt` |
| 53 | `module.type_annotation` | `tests.derive.module.TypeAnnotation` | 0 | 3 | 1 | 4 | `src/tests/derive/module/type_annotation.rs` | `tests/derive/module/TypeAnnotation.kt` |
| 54 | `module.unpack_value` | `tests.derive.module.UnpackValue` | 0 | 13 | 0 | 13 | `src/tests/derive/module/unpack_value.rs` | `tests/derive/module/UnpackValue.kt` |
| 55 | `derive.trace` | `tests.derive.trace.Trace` | 0 | 0 | 0 | 0 | `src/tests/derive/trace.rs` | `tests/derive/trace/Trace.kt` |
| 56 | `trace.bounds` | `tests.derive.trace.Bounds` | 0 | 2 | 2 | 4 | `src/tests/derive/trace/bounds.rs` | `tests/derive/trace/Bounds.kt` |
| 57 | `trace.enums` | `tests.derive.trace.Enums` | 0 | 0 | 1 | 1 | `src/tests/derive/trace/enums.rs` | `tests/derive/trace/Enums.kt` |
| 58 | `trace.statics` | `tests.derive.trace.Statics` | 0 | 0 | 2 | 2 | `src/tests/derive/trace/statics.rs` | `tests/derive/trace/Statics.kt` |
| 59 | `lib` | `Lib` | 0 | 0 | 0 | 0 | `src/lib.rs` | `Lib.kt` |
| 60 | `derive.unpack_value_attr` | `tests.derive.UnpackValueAttr` | 0 | 0 | 2 | 2 | `src/tests/derive/unpack_value_attr.rs` | `tests/derive/UnpackValueAttr.kt` |
| 61 | `tests.for_loop` | `tests.ForLoop` | 0 | 1 | 0 | 1 | `src/tests/for_loop.rs` | `tests/ForLoop.kt` |
| 62 | `tests.freeze_access_value` | `tests.FreezeAccessValue` | 0 | 2 | 2 | 4 | `src/tests/freeze_access_value.rs` | `tests/FreezeAccessValue.kt` |
| 63 | `tests.fstring` | `tests.Fstring` | 0 | 18 | 0 | 18 | `src/tests/fstring.rs` | `tests/Fstring.kt` |
| 64 | `tests.go` | `tests.Go` | 0 | 3 | 0 | 3 | `src/tests/go.rs` | `tests/Go.kt` |
| 65 | `tests.opt` | `tests.opt.Opt` | 0 | 8 | 0 | 8 | `src/tests/opt.rs` | `tests/opt/Opt.kt` |
| 66 | `opt.constant_folding` | `tests.opt.ConstantFolding` | 0 | 2 | 0 | 2 | `src/tests/opt/constant_folding.rs` | `tests/opt/ConstantFolding.kt` |
| 67 | `opt.def_inline` | `tests.opt.DefInline` | 0 | 8 | 0 | 8 | `src/tests/opt/def_inline.rs` | `tests/opt/DefInline.kt` |
| 68 | `opt.eq` | `tests.opt.Eq` | 0 | 7 | 0 | 7 | `src/tests/opt/eq.rs` | `tests/opt/Eq.kt` |
| 69 | `opt.if_rand` | `tests.opt.IfRand` | 0 | 27 | 3 | 30 | `src/tests/opt/if_rand.rs` | `tests/opt/IfRand.kt` |
| 70 | `opt.list_add` | `tests.opt.ListAdd` | 0 | 2 | 0 | 2 | `src/tests/opt/list_add.rs` | `tests/opt/ListAdd.kt` |
| 71 | `opt.speculative_exec` | `tests.opt.SpeculativeExec` | 0 | 3 | 0 | 3 | `src/tests/opt/speculative_exec.rs` | `tests/opt/SpeculativeExec.kt` |
| 72 | `opt.type_is` | `tests.opt.TypeIs` | 0 | 4 | 0 | 4 | `src/tests/opt/type_is.rs` | `tests/opt/TypeIs.kt` |
| 73 | `opt.types` | `tests.opt.Types` | 0 | 2 | 0 | 2 | `src/tests/opt/types.rs` | `tests/opt/Types.kt` |
| 74 | `tests.replace_binary` | `tests.ReplaceBinary` | 0 | 1 | 0 | 1 | `src/tests/replace_binary.rs` | `tests/ReplaceBinary.kt` |
| 75 | `tests.runtime` | `tests.Runtime` | 0 | 14 | 1 | 15 | `src/tests/runtime.rs` | `tests/Runtime.kt` |
| 76 | `tests.type_annot` | `tests.TypeAnnot` | 0 | 9 | 0 | 9 | `src/tests/type_annot.rs` | `tests/TypeAnnot.kt` |
| 77 | `tests.uncategorized` | `tests.Uncategorized` | 0 | 55 | 6 | 61 | `src/tests/uncategorized.rs` | `tests/Uncategorized.kt` |
| 78 | `tests.util` | `tests.Util` | 0 | 3 | 1 | 4 | `src/tests/util.rs` | `tests/Util.kt` |
| 79 | `typing.tests` | `typing.tests.Tests` | 0 | 28 | 2 | 30 | `src/typing/tests.rs` | `typing/tests/Tests.kt` |
| 80 | `typing.tests.call` | `typing.tests.Call` | 0 | 9 | 0 | 9 | `src/typing/tests/call.rs` | `typing/tests/Call.kt` |
| 81 | `tests.callable` | `typing.tests.Callable` | 0 | 2 | 0 | 2 | `src/typing/tests/callable.rs` | `typing/tests/Callable.kt` |
| 82 | `tests.list` | `typing.tests.List` | 0 | 6 | 0 | 6 | `src/typing/tests/list.rs` | `typing/tests/List.kt` |
| 83 | `tests.special_function` | `typing.tests.SpecialFunction` | 0 | 2 | 0 | 2 | `src/typing/tests/special_function.rs` | `typing/tests/SpecialFunction.kt` |
| 84 | `tests.tuple` | `typing.tests.Tuple` | 0 | 2 | 0 | 2 | `src/typing/tests/tuple.rs` | `typing/tests/Tuple.kt` |
| 85 | `tests.types` | `typing.tests.Types` | 0 | 3 | 0 | 3 | `src/typing/tests/types.rs` | `typing/tests/Types.kt` |
| 86 | `profile.tests` | `eval.runtime.profile.Tests` | 0 | 13 | 0 | 13 | `src/eval/runtime/profile/tests.rs` | `eval/runtime/profile/Tests.kt` |
| 87 | `runtime.profile` | `eval.runtime.profile.Profile` | 0 | 0 | 0 | 0 | `src/eval/runtime/profile.rs` | `eval/runtime/profile/Profile.kt` |
| 88 | `heap.allocator` | `values.layout.heap.allocator.Allocator` | 0 | 0 | 0 | 0 | `src/values/layout/heap/allocator.rs` | `values/layout/heap/allocator/Allocator.kt` |
| 89 | `allocator.alloc` | `values.layout.heap.allocator.alloc.Alloc` | 0 | 0 | 0 | 0 | `src/values/layout/heap/allocator/alloc.rs` | `values/layout/heap/allocator/alloc/Alloc.kt` |
| 90 | `heap.branding` | `values.layout.heap.Branding` | 0 | 0 | 0 | 0 | `src/values/layout/heap/branding.rs` | `values/layout/heap/Branding.kt` |
| 91 | `heap.profile` | `values.layout.heap.profile.Profile` | 0 | 0 | 0 | 0 | `src/values/layout/heap/profile.rs` | `values/layout/heap/profile/Profile.kt` |
| 92 | `runtime.params` | `eval.runtime.params.Params` | 0 | 0 | 0 | 0 | `src/eval/runtime/params.rs` | `eval/runtime/params/Params.kt` |
| 93 | `scope.tests` | `eval.compiler.scope.Tests` | 0 | 16 | 1 | 17 | `src/eval/compiler/scope/tests.rs` | `eval/compiler/scope/Tests.kt` |
| 94 | `bc.compiler` | `eval.bc.compiler.Compiler` | 0 | 0 | 0 | 0 | `src/eval/bc/compiler.rs` | `eval/bc/compiler/Compiler.kt` |
| 95 | `int.tests` | `values.types.int.Tests` | 0 | 6 | 0 | 6 | `src/values/types/int/tests.rs` | `values/types/int/Tests.kt` |
| 96 | `tests.rustdocs` | `docs.tests.Rustdocs` | 0 | 15 | 3 | 18 | `src/docs/tests/rustdocs.rs` | `docs/tests/Rustdocs.kt` |
| 97 | `tests.markdown` | `docs.tests.Markdown` | 0 | 29 | 2 | 31 | `src/docs/tests/markdown.rs` | `docs/tests/Markdown.kt` |
| 98 | `docs.tests` | `docs.tests.Tests` | 0 | 0 | 0 | 0 | `src/docs/tests.rs` | `docs/tests/Tests.kt` |
| 99 | `adapter.tests` | `debug.adapter.Tests` | 0 | 23 | 3 | 26 | `src/debug/adapter/tests.rs` | `debug/adapter/Tests.kt` |
| 100 | `collections` | `collections.Collections` | 0 | 0 | 0 | 0 | `src/collections.rs` | `collections/Collections.kt` |
| 101 | `unused_loads.remove_tests` | `analysis.unusedloads.RemoveTests` | 0 | 4 | 0 | 4 | `src/analysis/unused_loads/remove_tests.rs` | `analysis/unusedloads/RemoveTests.kt` |
| 102 | `unused_loads.find_tests` | `analysis.unusedloads.FindTests` | 0 | 7 | 0 | 7 | `src/analysis/unused_loads/find_tests.rs` | `analysis/unusedloads/FindTests.kt` |
| 103 | `string.intern` | `values.types.string.intern.Intern` | 0 | 0 | 0 | 0 | `src/values/types/string/intern.rs` | `values/types/string/intern/Intern.kt` |
| 104 | `types.structs` | `values.types.structs.Structs` | 0 | 0 | 0 | 0 | `src/values/types/structs.rs` | `values/types/structs/Structs.kt` |
| 105 | `types.tuple` | `values.types.tuple.Tuple` | 0 | 0 | 0 | 0 | `src/values/types/tuple.rs` | `values/types/tuple/Tuple.kt` |
| 106 | `bc.and_or` | `tests.bc.AndOr` | 0 | 8 | 0 | 8 | `src/tests/bc/and_or.rs` | `tests/bc/AndOr.kt` |

