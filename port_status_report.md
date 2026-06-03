# Code Port - Progress Report

**Generated:** 2026-06-02
**Source:** tmp/starlark
**Target:** src/commonMain

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 2940/4585 matched (target 4971) | 64.1% |
| Class/type parity | 790/1209 matched (target 1472) | 65.3% |
| Combined symbol parity | 3730/5794 matched (target 6443) | 64.4% |
| Average function body similarity | 0.25 | inline-code cosine |
| Average documentation similarity | 0.67 | doc text cosine |
| Missing source functions | 535 | 0% parity until ported |
| Missing source classes/types | 72 | 0% parity until ported |
| Missing source symbol files | 74 | 607 symbols |
| Cheat/scoring failures | 219 | forced to 0% |
| Total source files | 470 | 100% |
| Target units (paired) | 424 | - |
| Target files (total) | 424 | - |
| Porting progress | 364 | 77.4% (matched) |
| Missing files | 106 | 22.6% |

## Port Quality Analysis

**Average Function Similarity:** 0.25

Similarity in this report is the required function-by-function body/parameter score. Class/type parity and symbol deficits are reported beside it; whole-file shape is diagnostic only.

**Work Distribution:**
- Critical (<0.60): 287 files (78.8% of matched)
- Needs review (0.60-0.84): 50 files (13.7% of matched)

## Worst Function Scores First

Every matched file is listed from lowest function body/parameter similarity upward. Missing symbol names are not capped.

| Rank | Source | Target | Function similarity | Functions | Missing functions | Types | Missing types | Tests | Symbol deficit | Priority |
|------|--------|--------|---------------------|-----------|-------------------|-------|---------------|-------|----------------|----------|
| 1 | `layout.value` | `layout.Value [ZERO]` | 0.00 | 106/118 matched (target 158) | `fmt`, `eq`, `testing_new_int`, `_test_send_sync`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function` | 6/9 matched | `DisplayWithTypeImpl`, `Canonical`, `String` | 0/9 | 15 | 178162720.0 |
| 2 | `typing.starlark_value` | `typing.StarlarkValue [ZERO]` | 0.00 | 29/34 matched (target 43) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp` | 4/4 matched (target 5) | _none_ | - | 5 | 76053808.0 |
| 3 | `runtime.evaluator` | `runtime.Evaluator [ZERO]` | 0.00 | 58/60 matched (target 63) | `drop`, `add_diagnostics` | 7/7 matched (target 17) | _none_ | - | 2 | 56026712.0 |
| 4 | `values.trace` | `values.Trace [ZERO]` | 0.00 | 1/1 matched (target 43) | _none_ | 1/1 matched | _none_ | - | 0 | 52000208.0 |
| 5 | `values.freeze` | `values.Freeze [ZERO]` | 0.00 | 1/1 matched (target 31) | _none_ | 1/2 matched (target 6) | `Frozen` | - | 1 | 42010312.0 |
| 6 | `values.alloc_value` | `values.AllocValue [ZERO]` | 0.00 | 2/2 matched (target 5) | _none_ | 4/4 matched | _none_ | - | 0 | 42000608.0 |
| 7 | `layout.freezer` | `layout.Freezer [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched | _none_ | - | 0 | 36000608.0 |
| 8 | `values.frozen_ref` | `values.FrozenRef [ZERO]` | 0.00 | 17/17 matched (target 23) | _none_ | 2/4 matched (target 2) | `Target`, `Frozen` | - | 2 | 27022110.0 |
| 9 | `none.none_type` | `none.NoneType [ZERO]` | 0.00 | 11/11 matched (target 16) | _none_ | 1/2 matched | `Error` | - | 1 | 27011310.0 |
| 10 | `runtime.arguments` | `runtime.Arguments [ZERO]` | 0.00 | 26/30 matched (target 49) | `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names` | 8/8 matched (target 16) | _none_ | 0/4 | 4 | 25043810.0 |
| 11 | `typing.type_compiled` | `type_compiled.TypeCompiled [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 22000010.0 |
| 12 | `environment.globals` | `environment.Globals [ZERO]` | 0.00 | 30/35 matched (target 38) | `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden` | 5/5 matched | _none_ | 0/5 | 5 | 21054010.0 |
| 13 | `derive.module` | `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched (target 21) | _none_ | 0/0 matched (target 3) | _none_ | - | 0 | 21000010.0 |
| 14 | `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 0.00 | 12/18 matched (target 17) | `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant` | 3/7 matched (target 4) | `Canonical`, `Frozen`, `Error`, `ReprNotSendSync` | 0/5 | 10 | 20102510.0 |
| 15 | `environment.methods` | `environment.Methods [ZERO]` | 0.00 | 17/19 matched (target 21) | `test_set_attribute`, `get_methods` | 3/4 matched (target 3) | `Magic` | 0/2 | 3 | 17032310.0 |
| 16 | `values.iter` | `values.Iter [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 4/5 matched (target 84) | `drop` | 1/2 matched (target 14) | `Item` | - | 2 | 17020710.0 |
| 17 | `collections.symbol` | `collections.Symbol [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 15000010.0 |
| 18 | `private` | `starlark.Private [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 15000110.0 |
| 19 | `layout.avalue` | `layout.AValue [ZERO]` | 0.00 | 6/8 matched (target 11) | `tuple_cycle_freeze`, `test_try_freeze_directly` | 3/3 matched | _none_ | 0/2 | 2 | 14021110.0 |
| 20 | `layout.const_frozen_string` | `layout.ConstFrozenString [ZERO]` | 0.00 | 0/2 matched (target 1) | `test_const_frozen_string_for_short_strings`, `test_const_frozen_string` | 0/0 matched | _none_ | 0/2 | 2 | 12020210.0 |
| 21 | `typing.tuple` | `typing.Tuple [ZERO]` | 0.00 | 5/6 matched (target 9) | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 12010710.0 |
| 22 | `int.inline_int` | `int.InlineInt [ZERO]` | 0.00 | 25/34 matched (target 43) | `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits` | 2/5 matched (target 6) | `Error`, `Output`, `Canonical` | 0/2 | 12 | 11123910.0 |
| 23 | `int.pointer_i32` | `int.PointerI32 [ZERO]` | 0.00 | 28/31 matched (target 34) | `eq`, `fmt`, `serialize` | 1/2 matched | `Canonical` | - | 4 | 9043310.0 |
| 24 | `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 0.00 | 6/13 matched (target 15) | `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub` | 1/2 matched (target 1) | `Output` | 0/2 | 8 | 8081510.0 |
| 25 | `cast` | `starlark.Cast [ZERO]` | 0.00 | 3/3 matched (target 4) | _none_ | 0/0 matched | _none_ | - | 0 | 8000310.0 |
| 26 | `eval.compiler` | `eval.Compiler [ZERO]` | 0.00 | 6/6 matched | _none_ | 1/1 matched | _none_ | - | 0 | 8000710.0 |
| 27 | `types.bigint` | `types.Bigint [ZERO]` | 0.00 | 29/73 matched (target 35) | `unpack_integer`, `eq`, `test_parse`, `test_str`, `test_repr`, `test_equals`, `test_plus`, `test_compare_big_big`, `test_compare_big_small`, `test_compare_big_float`, `test_add_big`, `test_add_big_small`, `test_add_big_float`, `test_mul_big`, `test_mul_big_small`, `test_mul_big_float`, `test_div_big`, `test_div_big_small`, `test_div_big_float`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_big_float`, `test_percent_big`, `test_percent_big_small`, `test_percent_big_float`, `test_bit_and_big`, `test_bit_and_big_small`, `test_bit_and_float`, `test_bit_or_big`, `test_bit_or_big_small`, `test_bit_or_float`, `test_bit_xor_big`, `test_bit_xor_big_small`, `test_bit_xor_float`, `test_bit_not`, `test_left_shift`, `test_left_shift_small`, `test_left_shift_float`, `test_right_shift`, `test_right_shift_small`, `test_right_shift_float`, `test_int_function`, `test_hash`, `test_int_type_matches_bigint` | 1/1 matched | _none_ | 0/42 | 44 | 7447410.0 |
| 28 | `runtime.frozen_file_span` | `runtime.FrozenFileSpan [ZERO]` | 0.00 | 9/10 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 7011110.0 |
| 29 | `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 0.00 | 4/7 matched (target 9) | `from_millis`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 0/1 | 4 | 6040910.0 |
| 30 | `dict.dict_type` | `dict.DictType [ZERO]` | 0.00 | 1/2 matched (target 4) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 6030510.0 |
| 31 | `typing.typecheck` | `typing.Typecheck [STUB]` | 0.00 | 2/5 matched | `fmt`, `find_bindings_by_name`, `find_first_binding` | 2/2 matched (target 3) | _none_ | 0/2 | 3 | 6030710.0 |
| 32 | `values.freeze_error` | `values.FreezeError [ZERO]` | 0.00 | 3/4 matched (target 6) | `from` | 3/4 matched (target 3) | `FreezeResult` | - | 2 | 6020810.0 |
| 33 | `layout.value_alloc_size` | `layout.ValueAllocSize [ZERO]` | 0.00 | 4/5 matched | `layout` | 1/1 matched | _none_ | - | 1 | 6010610.0 |
| 34 | `compiler.stmt` | `compiler.Stmt [ZERO]` | 0.00 | 25/25 matched (target 28) | _none_ | 7/7 matched (target 24) | _none_ | - | 0 | 6003210.0 |
| 35 | `values.layout` | `values.Layout [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 6000010.0 |
| 36 | `tests.def` | `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0/14 matched (target 4) | `test_lambda`, `test_frozen_lambda`, `test_nested_def_1`, `test_nested_def_2`, `test_nested_def_3`, `test_lambda_capture_from_module`, `test_lambda_capture_from_def`, `test_lambda_capture_reassigned_from_def`, `test_def_freeze`, `test_frozen_lambda_nest`, `test_context_captured`, `test_lambda_errors`, `test_lambda_errors_nested`, `test_double_capture_and_freeze` | 0/0 matched (target 1) | _none_ | 0/14 | 14 | 5141410.0 |
| 37 | `types.array` | `types.Array [ZERO]` | 0.00 | 23/32 matched (target 24) | `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`, `debug`, `display` | 2/2 matched | _none_ | 0/2 | 9 | 5093410.0 |
| 38 | `eval.bc` | `bc.Bc [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 5000010.0 |
| 39 | `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 0.00 | 21/36 matched (target 24) | `fmt`, `r#type`, `values`, `test_enum_type_as_type_pass`, `test_enum_type_fail_runtime`, `test_enum_type_fail_compile_time`, `test_enum_is_callable`, `test_enum_value_index`, `test_enum_value_index_correct_type`, `test_enum_index`, `test_enum_index_fail`, `test_enum_call`, `test_enum_attribute_access`, `test_enum_attribute_access_invalid`, `test_enum_attribute_access_type` | 4/8 matched (target 6) | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` | 0/12 | 19 | 4194410.0 |
| 40 | `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 0.00 | 6/13 matched (target 8) | `fmt`, `new`, `compiler_args_globals`, `compiler_args`, `test_pass`, `test_fail_compile_time`, `test_fail_runtime` | 2/4 matched (target 2) | `Canonical`, `CompilerArgs` | 0/5 | 9 | 4091710.0 |
| 41 | `bc.frame` | `bc.Frame [ZERO]` | 0.00 | 16/24 matched (target 31) | `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit` | 2/2 matched | _none_ | - | 8 | 4082610.0 |
| 42 | `values.value_of` | `values.ValueOf [ZERO]` | 0.00 | 4/6 matched (target 5) | `deref`, `fmt` | 1/4 matched (target 1) | `Target`, `Canonical`, `Error` | - | 5 | 4051010.0 |
| 43 | `record.record_type` | `record.RecordType [ZERO]` | 0.00 | 15/22 matched (target 18) | `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error` | 2/8 matched (target 2) | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` | 0/5 | 13 | 3133010.0 |
| 44 | `alloc.chunk` | `alloc.Chunk [ZERO]` | 0.00 | 11/19 matched (target 18) | `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`, `test_alloc_release` | 2/3 matched (target 2) | `ChunkDataEmpty` | 0/3 | 9 | 3092210.0 |
| 45 | `stdlib.call_stack` | `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 7/13 matched (target 14) | `fmt`, `global`, `test_simple`, `test_strip_one`, `test_strip_all`, `test_call_stack_frame` | 1/1 matched (target 2) | _none_ | 0/4 | 6 | 3061410.0 |
| 46 | `errors.did_you_mean` | `errors.DidYouMean [ZERO]` | 0.00 | 1/6 matched (target 2) | `prefixes`, `typos`, `best`, `very_short`, `earlier_variants_are_more_important` | 0/0 matched | _none_ | 0/5 | 5 | 3050610.0 |
| 47 | `list.alloc` | `list.Alloc [ZERO]` | 0.00 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 3040510.0 |
| 48 | `compiler.constants` | `compiler.Constants [ZERO]` | 0.00 | 1/3 matched (target 5) | `eq`, `test_constants` | 2/2 matched | _none_ | 0/1 | 2 | 3020510.0 |
| 49 | `profile.instant` | `profile.Instant [ZERO]` | 0.00 | 3/4 matched (target 9) | `sub` | 1/2 matched (target 1) | `Output` | - | 2 | 3020610.0 |
| 50 | `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/3 matched (target 1) | `Canonical`, `Error` | - | 2 | 3020510.0 |
| 51 | `sealed` | `starlark.Sealed [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 3000110.0 |
| 52 | `types.record` | `types.Record [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 3000010.0 |
| 53 | `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 0.00 | 4/11 matched (target 9) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter` | 1/4 matched (target 3) | `Target`, `Item`, `IntoIter` | - | 10 | 2101510.0 |
| 54 | `layout.const_type_id` | `layout.ConstTypeId [ZERO]` | 0.00 | 2/5 matched (target 4) | `fmt`, `eq`, `hash` | 1/1 matched | _none_ | - | 3 | 2030610.0 |
| 55 | `runtime.rust_loc` | `runtime.RustLoc [ZERO]` | 0.00 | 0/3 matched (target 1) | `rust_loc_globals`, `invoke`, `test_rust_loc` | 0/0 matched | _none_ | 0/3 | 3 | 2030310.0 |
| 56 | `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 0.00 | 10/12 matched (target 19) | `fmt`, `deref` | 2/3 matched (target 2) | `Target` | - | 3 | 2031510.0 |
| 57 | `avalues.str_` | `avalues.Str [ZERO]` | 0.00 | 11/11 matched (target 15) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | - | 2 | 2021410.0 |
| 58 | `values.stack_guard` | `values.StackGuard [ZERO]` | 0.00 | 3/4 matched | `drop` | 1/1 matched | _none_ | - | 1 | 2010510.0 |
| 59 | `collections.string_pool` | `collections.StringPool [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/1 matched | _none_ | - | 0 | 2000310.0 |
| 60 | `def_inline.local_as_value` | `def_inline.LocalAsValue [ZERO]` | 0.00 | 1/1 matched (target 2) | _none_ | 1/1 matched | _none_ | - | 0 | 2000210.0 |
| 61 | `profile.string_index` | `profile.StringIndex [ZERO]` | 0.00 | 2/2 matched | _none_ | 2/2 matched | _none_ | - | 0 | 2000410.0 |
| 62 | `values.thin_box_slice_frozen_value` | `values.ThinBoxSliceFrozenValue [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000010.0 |
| 63 | `heap.arena` | `heap.Arena [ZERO]` | 0.00 | 18/37 matched (target 23) | `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty` | 4/7 matched (target 6) | `ChunkIter`, `Item`, `ArenaUninit` | 0/7 | 22 | 1224410.0 |
| 64 | `collections.alloca` | `collections.Alloca [ZERO]` | 0.00 | 5/22 matched (target 5) | `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat` | 1/4 matched (target 1) | `Buffer`, `Align`, `DropSliceGuard` | 0/6 | 20 | 1202610.0 |
| 65 | `stdlib` | `starlark.Stdlib [ZERO]` | 0.00 | 3/14 matched (target 3) | `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2` | 1/3 matched (target 1) | `Bool2`, `Error` | 0/11 | 13 | 1131710.0 |
| 66 | `string.interpolation` | `string.Interpolation [ZERO]` | 0.00 | 4/12 matched (target 6) | `test_incomplete_format`, `test_unsupported_format_character`, `test_parse_percent_s_one`, `test_type_support_d`, `test_type_support_o`, `test_type_support_x`, `test_type_support_e`, `test_int_min` | 4/4 matched (target 20) | _none_ | 0/8 | 8 | 1081610.0 |
| 67 | `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 0.00 | 1/5 matched | `default`, `starlark_type_repr`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 8 | 1081010.0 |
| 68 | `layout.pointer` | `layout.Pointer [ZERO]` | 0.00 | 25/32 matched (target 46) | `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `test_int_tag`, `check` | 5/5 matched | _none_ | 0/1 | 7 | 1073710.0 |
| 69 | `types.any_array` | `types.AnyArray [ZERO]` | 0.00 | 3/7 matched | `fmt`, `drop`, `test_drop`, `test_allocation_size` | 1/3 matched (target 1) | `Canonical`, `IncrementOnDrop` | 0/2 | 6 | 1061010.0 |
| 70 | `util.rtabort` | `util.Rtabort [ZERO]` | 0.00 | 2/6 matched (target 3) | `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort` | 0/1 matched (target 0) | `AbortOnDrop` | 0/3 | 5 | 1050710.0 |
| 71 | `bc.if_debug` | `bc.IfDebug [ZERO]` | 0.00 | 5/8 matched (target 9) | `eq`, `partial_cmp`, `cmp` | 1/1 matched | _none_ | - | 3 | 1030910.0 |
| 72 | `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 0.00 | 1/3 matched (target 1) | `get_type_id`, `test_non_static_type_id` | 0/1 matched (target 0) | `NonStaticAny` | 0/1 | 3 | 1030410.0 |
| 73 | `avalues.simple` | `avalues.Simple [ZERO]` | 0.00 | 8/8 matched (target 11) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | - | 2 | 1021110.0 |
| 74 | `record.field` | `record.Field [ZERO]` | 0.00 | 4/5 matched (target 10) | `fmt` | 0/1 matched | `FieldGen` | - | 2 | 1020610.0 |
| 75 | `runtime.cheap_call_stack` | `runtime.CheapCallStack [ZERO]` | 0.00 | 15/17 matched | `fmt`, `default` | 3/3 matched (target 6) | _none_ | - | 2 | 1022010.0 |
| 76 | `structs.unordered_hasher` | `structs.UnorderedHasher [ZERO]` | 0.00 | 3/5 matched (target 3) | `_write`, `test_unordered_hasher` | 1/1 matched | _none_ | 0/1 | 2 | 1020610.0 |
| 77 | `heap.fast_cell` | `heap.FastCell [ZERO]` | 0.00 | 6/7 matched | `drop` | 1/1 matched | _none_ | - | 1 | 1010810.0 |
| 78 | `read_line` | `starlark.ReadLine [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/2 matched (target 1) | `NoRustyline` | - | 1 | 1010410.0 |
| 79 | `typing.bindings` | `typing.Bindings [STUB]` | 0.00 | 7/8 matched (target 18) | `get_for_clause` | 3/3 matched (target 18) | _none_ | - | 1 | 1011110.0 |
| 80 | `types.int` | `types.Int [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 81 | `typing` | `starlark.Typing [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 82 | `typing.function` | `typing.Function [STUB]` | 0.00 | 12/12 matched (target 24) | _none_ | 3/3 matched | _none_ | - | 0 | 1001510.0 |
| 83 | `set.methods` | `set.Methods [STUB]` | 0.00 | 18/68 matched (target 19) | `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self` | 1/1 matched (target 3) | _none_ | 0/50 | 50 | 506910.0 |
| 84 | `string.str_type` | `string.StrType [ZERO]` | 0.00 | 2/47 matched (target 25) | `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_string_corruption`, `test_escape_characters`, `test_string_hash`, `test_zero_length_string_hash_is_not_zero`, `test_string_len`, `test_arithmetic_on_string`, `test_slice_string`, `test_string_is_in`, `test_successive_add`, `test_string_index`, `test_str` | 0/4 matched (target 0) | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` | 0/11 | 49 | 495110.0 |
| 85 | `int.int_or_big` | `int.IntOrBig [STUB]` | 0.00 | 24/46 matched (target 57) | `starlark_type_repr`, `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_small_big`, `test_floor_div_small`, `test_percent_big`, `test_percent_big_small`, `test_percent_small_big`, `test_percent_small` | 3/7 matched (target 11) | `Canonical`, `Err`, `Error`, `Output` | 0/9 | 26 | 265310.0 |
| 86 | `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 0.00 | 6/29 matched (target 11) | `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`, `test_empty`, `test_from_iter_sized`, `test_from_iter_unknown_size`, `test_stress` | 1/3 matched (target 1) | `ThinBoxSliceLayout`, `Target` | 0/4 | 25 | 253210.0 |
| 87 | `set.value` | `set.Value [ZERO]` | 0.00 | 30/50 matched (target 47) | `fmt`, `test_bit_or`, `test_bit_or_lhs_empty`, `test_bit_or_rhs_empty`, `test_bit_or_fail_iter`, `test_bit_or_ord`, `test_bit_and`, `test_bit_and_lhs_empty`, `test_bit_and_rhs_empty`, `test_bit_and_ord`, `test_bit_and_fail_iter`, `test_bit_xor`, `test_bit_xor_ord`, `test_bit_xor_lhs_empty`, `test_bit_xor_rhs_empty`, `test_bit_xor_fail_iter`, `test_sub`, `test_sub_empty_lhs`, `test_sub_empty_rhs`, `test_sub_fail_iter` | 6/9 matched (target 6) | `Canonical`, `Frozen`, `ContentRef` | 0/19 | 23 | 235910.0 |
| 88 | `values.typing.callable` | `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 0.00 | 12/32 matched (target 31) | `clone`, `fmt`, `trace`, `_assert_sync_send`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_pass`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `test_callable_checked_runtime`, `module`, `good`, `bad` | 5/8 matched (target 6) | `Canonical`, `Error`, `Frozen` | 0/15 | 23 | 234010.0 |
| 89 | `float.float` | `float.Float [ZERO]` | 0.00 | 26/39 matched (target 33) | `fmt`, `non_finite`, `test_write_non_finite`, `decimal`, `test_write_decimal`, `scientific`, `test_write_scientific`, `compact`, `test_write_compact`, `test_arithmetic_operators`, `test_dictionary_key`, `test_comparisons`, `test_comparisons_by_sorting` | 1/3 matched (target 1) | `Canonical`, `Error` | 0/12 | 15 | 154210.0 |
| 90 | `layout.typed` | `layout.ValueTyped [ZERO]` | 0.00 | 21/31 matched (target 44) | `fmt`, `serialize`, `eq`, `deref`, `unpack_value_impl`, `int`, `test_unpack_value_for_frozen_value_typed`, `module`, `mutable`, `takes_frozen_value_typed` | 2/7 matched (target 2) | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` | 0/5 | 15 | 153810.0 |
| 91 | `scope.payload` | `scope.Payload [ZERO]` | 0.00 | 0/7 matched (target 0) | `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id` | 9/17 matched (target 14) | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` | - | 15 | 152410.0 |
| 92 | `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 0.00 | 4/18 matched (target 8) | `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `default`, `fmt`, `eq`, `across_lengths`, `test_strings`, `test_ints`, `test_mixed_types`, `test_default`, `test_empty` | 2/3 matched (target 2) | `Target` | 0/6 | 15 | 152110.0 |
| 93 | `string.repr` | `string.Repr [ZERO]` | 0.00 | 9/22 matched (target 11) | `or4`, `push_vec_tail`, `test_to_repr`, `test_string_repr`, `test`, `test_to_repr_long_smoke`, `string_repr_for_test`, `to_repr_sse`, `to_repr_no_escape_all_lengths`, `to_repr_tail_escape_all_lengths`, `to_repr_middle_escape_all_lengths`, `test_chunk_non_ascii_or_need_escape`, `load` | 1/1 matched | _none_ | 0/11 | 13 | 132310.0 |
| 94 | `dict.value` | `dict.Value [ZERO]` | 0.00 | 43/52 matched (target 79) | `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `serialize`, `test_mutate_dict`, `test_get_str`, `test_repr_cycle` | 7/10 matched | `Canonical`, `Frozen`, `ContentRef` | 0/3 | 12 | 126210.0 |
| 95 | `list.value` | `list.Value [ZERO]` | 0.00 | 46/56 matched (target 96) | `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare` | 6/8 matched (target 9) | `List`, `Canonical` | 0/7 | 12 | 126410.0 |
| 96 | `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 0.00 | 3/13 matched (target 6) | `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_lookup_nonexistent_type`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered` | 2/4 matched (target 3) | `TestSimpleType`, `TestComplexGen` | 0/9 | 12 | 121710.0 |
| 97 | `record.globals` | `record.Globals [ZERO]` | 0.00 | 1/12 matched (target 1) | `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid` | 0/0 matched | _none_ | 0/9 | 11 | 111210.0 |
| 98 | `alloc.chain` | `alloc.Chain [ZERO]` | 0.00 | 14/22 matched (target 19) | `drop`, `test_default`, `test_new_drop`, `test_new_drop_many`, `test_split_at`, `test_split_at_len`, `test_split_at_zero`, `test_depth` | 3/5 matched (target 3) | `Item`, `ResetSplitAtZeroTest` | 0/7 | 10 | 102710.0 |
| 99 | `heap.heap_type` | `heap.HeapType [ZERO]` | 0.00 | 37/47 matched (target 68) | `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `test_send_sync`, `test_string_reallocated_on_heap`, `test_interned_string_equal`, `validate_str_interning`, `append_x`, `test_interned_str_starlark` | 8/8 matched (target 9) | _none_ | 0/6 | 10 | 105510.0 |
| 100 | `range.range_type` | `range.RangeType [ZERO]` | 0.00 | 14/24 matched (target 21) | `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `test_range_exhaustive`, `test_max_len` | 1/1 matched (target 2) | _none_ | 0/8 | 10 | 102510.0 |
| 101 | `stdlib.partial` | `stdlib.Partial [ZERO]` | 0.00 | 4/12 matched (target 7) | `partial`, `fmt`, `eq`, `test_simple`, `test_star_to_partial`, `test_start_to_returned_func`, `test_no_args_to_partial`, `test_typecheck_bug` | 3/5 matched (target 3) | `Frozen`, `Canonical` | 0/6 | 10 | 101710.0 |
| 102 | `alloc.allocator` | `alloc.Allocator [ZERO]` | 0.00 | 11/18 matched (target 15) | `fmt`, `default`, `drop`, `test_small`, `test_big`, `random_iteration`, `test_many` | 2/3 matched (target 2) | `Item` | 0/4 | 8 | 82110.0 |
| 103 | `tuple.unpack` | `tuple.Unpack [ZERO]` | 0.00 | 1/5 matched | `default`, `starlark_type_repr`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 8 | 81010.0 |
| 104 | `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 0.00 | 33/39 matched (target 48) | `fmt`, `check_matches`, `starlark_type_repr`, `alloc_value`, `hash`, `eq` | 5/7 matched (target 12) | `StaticType`, `Canonical` | - | 8 | 84610.0 |
| 105 | `dict.methods` | `dict.Methods [ZERO]` | 0.00 | 10/17 matched (target 12) | `test_error_codes`, `test_dict_add`, `test_dict_with_duplicates`, `test_dict_update_with_self_pos`, `test_dict_update_with_self_as_kwargs`, `test_frozen_dict_cannot_be_updated_with_self_pos`, `test_frozen_dict_cannot_be_updated_with_self_as_kwargs` | 0/0 matched | _none_ | 0/7 | 7 | 71710.0 |
| 106 | `docs.parse` | `docs.Parse [ZERO]` | 0.00 | 8/15 matched (target 11) | `parses_starlark_docstring`, `parses_rust_docstring`, `parses_and_removes_sections_from_starlark_docstring`, `parses_and_removes_sections_from_rust_docstring`, `arg`, `parses_starlark_function_docstring`, `parses_rust_function_docstring` | 1/1 matched | _none_ | 0/7 | 7 | 71610.0 |
| 107 | `funcs.other` | `funcs.Other [ZERO]` | 0.00 | 12/19 matched (target 13) | `r#type`, `test_abs`, `test_constants`, `test_chr`, `test_hash`, `test_int`, `test_tuple` | 0/0 matched (target 1) | _none_ | 0/6 | 7 | 71910.0 |
| 108 | `layout.complex` | `layout.Complex [ZERO]` | 0.00 | 9/13 matched (target 15) | `unpack_value_impl`, `fmt`, `test_module`, `test_unpack` | 1/4 matched (target 1) | `Canonical`, `Error`, `Frozen` | 0/2 | 7 | 71710.0 |
| 109 | `profile.aggregated` | `profile.Aggregated [ZERO]` | 0.00 | 17/24 matched (target 35) | `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `test_stacks_collect`, `test_stacks_collect_retained`, `test_merge`, `make` | 8/8 matched (target 10) | _none_ | 0/6 | 7 | 73210.0 |
| 110 | `record.ty_record_type` | `record.TyRecordType [ZERO]` | 0.00 | 0/7 matched (target 0) | `test_good`, `test_fail_compile_time`, `test_fail_runtime_time`, `test_record_instance_typechecker_ty`, `test_typecheck_field_pass`, `test_typecheck_field_fail`, `test_typecheck_record_type_call` | 1/1 matched | _none_ | 0/7 | 7 | 70810.0 |
| 111 | `string.simd` | `string.Simd [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 1/8 matched (target 4) | `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask` | 2/2 matched | _none_ | - | 7 | 71010.0 |
| 112 | `structs.value` | `structs.Value [ZERO]` | 0.00 | 14/21 matched (target 30) | `iter_frozen`, `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_to_json`, `test_comparison_bug` | 1/1 matched (target 3) | _none_ | 0/5 | 7 | 72210.0 |
| 113 | `tuple.value` | `tuple.Value [ZERO]` | 0.00 | 24/31 matched (target 37) | `fmt`, `new`, `offset_of_content`, `typechecker_ty`, `test_to_str`, `test_repr_cycle`, `test_tuple_ellipsis_runtime` | 3/3 matched | _none_ | 0/3 | 7 | 73410.0 |
| 114 | `typed.string` | `typed.String [ZERO]` | 0.00 | 8/15 matched (target 59) | `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`, `test_string_hashes` | 3/3 matched (target 4) | _none_ | 0/1 | 7 | 71810.0 |
| 115 | `adapter.implementation` | `adapter.Implementation [ZERO]` | 0.00 | 17/23 matched (target 27) | `prepare_dap_adapter`, `fmt`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints` | 6/6 matched (target 10) | _none_ | - | 6 | 62910.0 |
| 116 | `assert.assert` | `assert.Assert [STUB]` | 0.00 | 44/50 matched (target 66) | `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck` | 2/2 matched | _none_ | 0/4 | 6 | 65210.0 |
| 117 | `bc.instrs` | `bc.Instrs [ZERO]` | 0.00 | 19/24 matched (target 29) | `handle`, `drop`, `opcodes`, `fmt`, `display` | 3/4 matched (target 3) | `HandlerImpl` | 0/2 | 6 | 62810.0 |
| 118 | `bigint.convert` | `bigint.Convert [ZERO]` | 0.00 | 4/8 matched (target 27) | `test_unpack_int_error`, `module`, `takes_i32`, `takes_i64` | 0/2 matched (target 7) | `Canonical`, `Error` | 0/4 | 6 | 61010.0 |
| 119 | `compiler.scope` | `compiler.Scope [ZERO]` | 0.00 | 48/51 matched (target 70) | `from`, `assign_ident_impl`, `new` | 17/20 matched (target 28) | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` | - | 6 | 67110.0 |
| 120 | `heap.send` | `heap.Send [ZERO]` | 0.00 | 2/5 matched (target 6) | `deref`, `deref_mut`, `fmt` | 3/6 matched (target 3) | `Sealed`, `Target`, `StaticType` | - | 6 | 61110.0 |
| 121 | `list.unpack` | `list.Unpack [ZERO]` | 0.00 | 3/5 matched (target 8) | `into_iter`, `test_unpack` | 1/5 matched (target 3) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 6 | 61010.0 |
| 122 | `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 0.00 | 6/8 matched (target 6) | `next`, `size_hint` | 0/3 matched (target 1) | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` | - | 5 | 51110.0 |
| 123 | `debug.inspect` | `debug.Inspect [ZERO]` | 0.00 | 4/9 matched (target 4) | `debugger`, `debug_inspect_stack`, `debug_inspect_variables`, `test_debug_stack`, `test_debug_variables` | 0/0 matched | _none_ | 0/5 | 5 | 50910.0 |
| 124 | `environment.modules` | `environment.Modules [ZERO]` | 0.00 | 38/43 matched (target 48) | `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo` | 4/4 matched (target 6) | _none_ | 0/5 | 5 | 54710.0 |
| 125 | `params.spec` | `params.Spec [ZERO]` | 0.00 | 33/38 matched (target 34) | `as_value`, `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl` | 6/6 matched (target 11) | _none_ | - | 5 | 54410.0 |
| 126 | `profile.stmt` | `profile.Stmt [ZERO]` | 0.00 | 13/17 matched (target 20) | `r#gen`, `test_coverage`, `test_empty`, `test_merge` | 8/9 matched | `Data` | 0/3 | 5 | 52610.0 |
| 127 | `typing.iter` | `typing.Iter [ZERO]` | 0.00 | 3/6 matched (target 5) | `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail` | 2/4 matched (target 2) | `NonInstantiable`, `Canonical` | 0/3 | 5 | 51010.0 |
| 128 | `values.owned` | `values.Owned [ZERO]` | 0.00 | 26/29 matched (target 34) | `fmt`, `downcast_starlark`, `deref` | 3/5 matched | `Canonical`, `Target` | - | 5 | 53410.0 |
| 129 | `values.unpack` | `values.Unpack [ZERO]` | 0.00 | 8/9 matched (target 14) | `error` | 3/7 matched | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` | - | 5 | 51610.0 |
| 130 | `avalues.static_` | `avalues.Static [ZERO]` | 0.00 | 8/9 matched | `test_alloc_static_simple` | 2/5 matched (target 2) | `StarlarkValue`, `ExtraElem`, `MySimpleValue` | 0/1 | 4 | 41410.0 |
| 131 | `bc.addr` | `bc.Addr [ZERO]` | 0.00 | 20/23 matched (target 35) | `add_assign`, `get_instr_mut`, `sub_usize` | 5/6 matched (target 5) | `Output` | - | 4 | 42910.0 |
| 132 | `dict.alloc` | `dict.Alloc [ZERO]` | 0.00 | 0/3 matched (target 1) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 40510.0 |
| 133 | `heap.repr` | `heap.Repr [ZERO]` | 0.00 | 23/27 matched (target 35) | `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut` | 5/5 matched (target 8) | _none_ | - | 4 | 43210.0 |
| 134 | `list.methods` | `list.Methods [ZERO]` | 0.00 | 7/11 matched (target 13) | `list_methods`, `test_error_codes`, `test_index`, `recursive_list` | 0/0 matched | _none_ | 0/3 | 4 | 41110.0 |
| 135 | `params.parser` | `params.Parser [ZERO]` | 0.00 | 5/9 matched (target 5) | `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args` | 1/1 matched | _none_ | 0/4 | 4 | 41010.0 |
| 136 | `profile.typecheck` | `profile.Typecheck [ZERO]` | 0.00 | 5/8 matched (target 6) | `r#gen`, `test_typecheck_profile`, `test_typecheck_profile_merge` | 4/5 matched | `Data` | 0/2 | 4 | 41310.0 |
| 137 | `set.set` | `set.Set [ZERO]` | 0.00 | 1/5 matched (target 1) | `set`, `test_set_type_as_type_compile_time`, `test_return_set_type_as_type_compile_time`, `test_set_type_as_type_run_time` | 0/0 matched | _none_ | 0/3 | 4 | 40510.0 |
| 138 | `string.methods` | `string.Methods [ZERO]` | 0.00 | 37/41 matched (target 64) | `test_error_codes`, `test_count`, `test_find`, `test_opaque_iterator` | 1/1 matched (target 4) | _none_ | 0/4 | 4 | 44210.0 |
| 139 | `structs.alloc` | `structs.Alloc [ZERO]` | 0.00 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 40510.0 |
| 140 | `typing.custom` | `typing.Custom [ZERO]` | 0.00 | 31/35 matched (target 49) | `eq`, `hash`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | - | 4 | 43810.0 |
| 141 | `bc.opcode` | `bc.Opcode [ZERO]` | 0.00 | 6/7 matched (target 10) | `opcode_count` | 3/5 matched (target 3) | `ByNumber`, `FindOpcode` | 0/1 | 3 | 31210.0 |
| 142 | `bc.repr` | `bc.Repr [ZERO]` | 0.00 | 4/6 matched (target 5) | `size_of_repr`, `handle` | 2/3 matched (target 2) | `HandlerImpl` | - | 3 | 30910.0 |
| 143 | `debug.evaluate` | `debug.Evaluate [ZERO]` | 0.00 | 1/4 matched (target 1) | `debugger`, `debug_evaluate`, `test_debug_evaluate` | 0/0 matched | _none_ | 0/3 | 3 | 30410.0 |
| 144 | `list.refs` | `list.Refs [ZERO]` | 0.00 | 9/9 matched (target 29) | _none_ | 2/5 matched (target 10) | `Target`, `Canonical`, `Error` | - | 3 | 31410.0 |
| 145 | `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 0.00 | 5/6 matched (target 9) | `unpack_value_impl` | 0/2 matched (target 1) | `Canonical`, `Error` | - | 3 | 30810.0 |
| 146 | `symbol.map` | `symbol.Map [ZERO]` | 0.00 | 9/12 matched (target 11) | `fmt`, `new`, `with_capacity` | 1/1 matched | _none_ | - | 3 | 31310.0 |
| 147 | `type_compiled.globals` | `type_compiled.Globals [ZERO]` | 0.00 | 1/4 matched (target 1) | `eval_type`, `isinstance`, `test_typechecking` | 0/0 matched | _none_ | 0/1 | 3 | 30410.0 |
| 148 | `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 0.00 | 10/10 matched (target 13) | _none_ | 4/7 matched | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` | - | 3 | 31710.0 |
| 149 | `typing.never` | `typing.Never [ZERO]` | 0.00 | 4/6 matched (target 7) | `test_never_runtime`, `test_never_compile_time` | 2/3 matched (target 2) | `Canonical` | 0/2 | 3 | 30910.0 |
| 150 | `values.typing.ty` | `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` | 0.00 | 2/5 matched (target 4) | `test_isinstance`, `test_pass`, `test_fail_compile_time` | 1/1 matched | _none_ | 0/3 | 3 | 30610.0 |
| 151 | `avalues.array` | `avalues.Array [ZERO]` | 0.00 | 9/9 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 2 | 21310.0 |
| 152 | `avalues.complex` | `avalues.Complex [ZERO]` | 0.00 | 6/6 matched (target 17) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | - | 2 | 21110.0 |
| 153 | `avalues.tuple` | `avalues.Tuple [ZERO]` | 0.00 | 8/8 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 2 | 21210.0 |
| 154 | `bc.call` | `bc.Call [ZERO]` | 0.00 | 3/4 matched (target 15) | `fmt` | 4/5 matched (target 8) | `Args` | - | 2 | 20910.0 |
| 155 | `bc.instr_arg` | `bc.InstrArg [ZERO]` | 0.00 | 4/5 matched (target 84) | `fmt` | 3/4 matched (target 42) | `HandlerImpl` | - | 2 | 20910.0 |
| 156 | `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 0.00 | 10/11 matched (target 25) | `add` | 7/8 matched (target 7) | `Output` | - | 2 | 21910.0 |
| 157 | `bool.type_repr` | `bool.TypeRepr [ZERO]` | 0.00 | 0/1 matched | `starlark_type_repr` | 0/1 matched (target 0) | `Canonical` | - | 2 | 20210.0 |
| 158 | `build` | `starlark.Build [ZERO]` | 0.00 | 0/2 matched (target 0) | `main`, `rust_nightly` | 0/0 matched (target 1) | _none_ | - | 2 | 20210.0 |
| 159 | `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 0.00 | 2/3 matched (target 2) | `drop` | 0/1 matched (target 0) | `Guard` | - | 2 | 20410.0 |
| 160 | `compiler.def` | `compiler.Def [ZERO]` | 0.00 | 38/39 matched (target 46) | `fmt` | 12/13 matched (target 18) | `Frozen` | - | 2 | 25210.0 |
| 161 | `compiler.expr` | `compiler.Expr [ZERO]` | 0.00 | 59/59 matched (target 63) | _none_ | 9/11 matched (target 56) | `AstLiteralCompile`, `CompilerExprUtil` | - | 2 | 27010.0 |
| 162 | `eval.bc.compiler.stmt` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` | 0.00 | 8/10 matched (target 11) | `write_if_then`, `write_if_else` | 0/0 matched | _none_ | - | 2 | 21010.0 |
| 163 | `funcs.min_max` | `funcs.MinMax [ZERO]` | 0.00 | 3/5 matched (target 3) | `max`, `min` | 0/0 matched | _none_ | - | 2 | 20510.0 |
| 164 | `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 0.00 | 0/1 matched (target 4) | `drop` | 5/6 matched (target 5) | `Canonical` | - | 2 | 20710.0 |
| 165 | `intern.interner` | `intern.Interner [ZERO]` | 0.00 | 1/3 matched (target 5) | `test_intern`, `test_string_value_intern` | 2/2 matched | _none_ | 0/2 | 2 | 20510.0 |
| 166 | `list.globals` | `list.Globals [ZERO]` | 0.00 | 4/5 matched | `list` | 0/1 matched | `ListType` | - | 2 | 20610.0 |
| 167 | `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 0.00 | 9/10 matched | `drop_non_drop` | 2/3 matched (target 2) | `RowKind` | 0/1 | 2 | 21310.0 |
| 168 | `set.refs` | `set.Refs [ZERO]` | 0.00 | 5/5 matched (target 19) | _none_ | 3/5 matched (target 13) | `Canonical`, `Error` | - | 2 | 21010.0 |
| 169 | `structs.refs` | `structs.Refs [ZERO]` | 0.00 | 5/5 matched (target 8) | _none_ | 2/4 matched | `Canonical`, `Error` | - | 2 | 20910.0 |
| 170 | `types.function` | `types.Function [ZERO]` | 0.00 | 12/13 matched (target 28) | `new` | 11/12 matched (target 14) | `Canonical` | - | 2 | 22510.0 |
| 171 | `typing.any` | `typing.Any [ZERO]` | 0.00 | 2/4 matched | `test_any_runtime`, `test_any_compile_time` | 1/1 matched | _none_ | 0/2 | 2 | 20510.0 |
| 172 | `values.index` | `values.Index [ZERO]` | 0.00 | 4/6 matched (target 5) | `test_convert_index`, `test_apply_slice` | 0/0 matched | _none_ | 0/2 | 2 | 20610.0 |
| 173 | `values.traits` | `values.Traits [ZERO]` | 0.00 | 55/56 matched (target 55) | `please_use_starlark_type_macro` | 2/3 matched (target 2) | `Canonical` | - | 2 | 25910.0 |
| 174 | `values.type_repr` | `values.TypeRepr [ZERO]` | 0.00 | 2/3 matched (target 6) | `test_canonical_for_complex_value` | 2/3 matched (target 6) | `Canonical` | 0/1 | 2 | 20610.0 |
| 175 | `alloc.per_thread` | `alloc.PerThread [ZERO]` | 0.00 | 5/6 matched (target 5) | `test_release_partial` | 1/1 matched | _none_ | 0/1 | 1 | 10710.0 |
| 176 | `compiler.if_compiler` | `compiler.IfCompiler [ZERO]` | 0.00 | 5/6 matched (target 5) | `wr` | 0/0 matched | _none_ | - | 1 | 10610.0 |
| 177 | `debug.adapter` | `debug.Adapter [ZERO]` | 0.00 | 21/22 matched (target 23) | `fmt` | 14/14 matched (target 29) | _none_ | - | 1 | 13610.0 |
| 178 | `dict.globals` | `dict.Globals [ZERO]` | 0.00 | 2/3 matched (target 4) | `dict` | 0/0 matched | _none_ | - | 1 | 10310.0 |
| 179 | `docs` | `docs.Docs [ZERO]` | 0.00 | 12/13 matched (target 16) | `default` | 10/10 matched (target 15) | _none_ | - | 1 | 12310.0 |
| 180 | `eval.bc.compiler.call` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` | 0.00 | 4/5 matched (target 8) | `mark_definitely_assigned_after` | 0/0 matched (target 3) | _none_ | - | 1 | 10510.0 |
| 181 | `float.globals` | `float.Globals [ZERO]` | 0.00 | 1/2 matched (target 1) | `float` | 0/0 matched (target 4) | _none_ | - | 1 | 10210.0 |
| 182 | `int.globals` | `int.Globals [ZERO]` | 0.00 | 1/2 matched | `int` | 0/0 matched | _none_ | - | 1 | 10210.0 |
| 183 | `profile.by_type` | `profile.ByType [ZERO]` | 0.00 | 5/6 matched (target 7) | `normalize_for_golden_tests` | 1/1 matched | _none_ | 0/1 | 1 | 10710.0 |
| 184 | `record.instance` | `record.Instance [ZERO]` | 0.00 | 12/13 matched (target 18) | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 11410.0 |
| 185 | `structs.structs` | `structs.Structs [ZERO]` | 0.00 | 3/4 matched (target 3) | `r#struct` | 1/1 matched | _none_ | - | 1 | 10510.0 |
| 186 | `values.recursive_repr_or_json_guard` | `values.RecursiveReprOrJsonGuard [ZERO]` | 0.00 | 2/3 matched (target 5) | `drop` | 4/4 matched | _none_ | - | 1 | 10710.0 |
| 187 | `__derive_refs` | `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched | _none_ | 0/0 matched (target 1) | _none_ | - | 0 | 10.0 |
| 188 | `__derive_refs.components` | `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 410.0 |
| 189 | `bc.for_loop` | `bc.ForLoop [ZERO]` | 0.00 | 0/0 matched (target 2) | _none_ | 1/1 matched | _none_ | - | 0 | 110.0 |
| 190 | `bc.writer` | `bc.Writer [ZERO]` | 0.00 | 42/42 matched (target 44) | _none_ | 4/4 matched | _none_ | - | 0 | 4610.0 |
| 191 | `compiler.assign` | `compiler.Assign [ZERO]` | 0.00 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 192 | `compiler.error` | `compiler.Error [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 2/2 matched (target 24) | _none_ | 1/1 matched (target 14) | _none_ | - | 0 | 310.0 |
| 193 | `compiler.types` | `compiler.Types [ZERO]` | 0.00 | 8/8 matched | _none_ | 1/1 matched (target 7) | _none_ | - | 0 | 910.0 |
| 194 | `docs.code` | `docs.Code [ZERO]` | 0.00 | 7/7 matched (target 14) | _none_ | 0/0 matched | _none_ | - | 0 | 710.0 |
| 195 | `docs.markdown` | `docs.Markdown [ZERO]` | 0.00 | 18/18 matched (target 19) | _none_ | 2/2 matched | _none_ | - | 0 | 2010.0 |
| 196 | `environment` | `starlark.Environment [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched (target 5) | _none_ | - | 0 | 110.0 |
| 197 | `environment.names` | `environment.Names [ZERO]` | 0.00 | 13/13 matched (target 14) | _none_ | 2/2 matched | _none_ | - | 0 | 1510.0 |
| 198 | `errors` | `starlark.Errors [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 199 | `eval.bc.compiler.def` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` | 0.00 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 200 | `eval.bc.compiler.expr` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` | 0.00 | 15/15 matched (target 16) | _none_ | 0/0 matched | _none_ | - | 0 | 1510.0 |
| 201 | `eval.runtime` | `eval.Runtime [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 202 | `fuzz_targets.starlark` | `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 203 | `layout.avalues` | `layout.AValues [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 204 | `layout.static_string` | `layout.StaticString [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched (target 2) | _none_ | - | 0 | 610.0 |
| 205 | `macros` | `starlark.Macros [ZERO]` | 0.00 | 0/0 matched (target 9) | _none_ | 0/0 matched (target 9) | _none_ | - | 0 | 10.0 |
| 206 | `pagable` | `starlark.Pagable [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 207 | `pagable.vtable_register` | `pagable.VtableRegister [ZERO]` | 0.00 | 0/0 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 208 | `stdlib.funcs` | `stdlib.Funcs [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 209 | `string.globals` | `string.Globals [ZERO]` | 0.00 | 5/5 matched | _none_ | 0/0 matched | _none_ | - | 0 | 510.0 |
| 210 | `string.iter` | `string.Iter [ZERO]` | 0.00 | 3/3 matched (target 6) | _none_ | 1/1 matched | _none_ | - | 0 | 410.0 |
| 211 | `syntax` | `starlark.Syntax [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 212 | `type_compiled.matchers` | `type_compiled.Matchers [ZERO]` | 0.00 | 3/3 matched (target 25) | _none_ | 23/23 matched | _none_ | - | 0 | 2610.0 |
| 213 | `typing.error` | `typing.Error [ZERO]` | 0.00 | 9/9 matched (target 25) | _none_ | 5/5 matched (target 10) | _none_ | - | 0 | 1410.0 |
| 214 | `typing.fill_types_for_lint` | `typing.FillTypesForLint [ZERO]` | 0.00 | 39/39 matched (target 40) | _none_ | 3/3 matched | _none_ | - | 0 | 4210.0 |
| 215 | `typing.oracle` | `typing.Oracle [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 216 | `unused_loads.remove` | `unusedloads.Remove [ZERO]` | 0.00 | 4/4 matched | _none_ | 1/1 matched | _none_ | - | 0 | 510.0 |
| 217 | `util` | `starlark.Util [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 218 | `values.types` | `values.Types [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 219 | `values.typing` | `values.Typing [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 220 | `types.type_instance_id` | `types.TypeInstanceId` | 0.00 | 0/1 matched (target 2) | `r#gen` | 1/1 matched | _none_ | - | 1 | 9010210.0 |
| 221 | `tuple.rust_tuple` | `tuple.RustTuple` | 0.00 | 0/4 matched (target 11) | `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl` | 0/2 matched (target 0) | `Canonical`, `Error` | - | 6 | 60610.0 |
| 222 | `bool.unpack` | `bool.Unpack` | 0.00 | 0/1 matched | `unpack_value_impl` | 0/1 matched (target 0) | `Error` | - | 2 | 20210.0 |
| 223 | `enumeration.ty_enum_type` | `enumeration.TyEnumType` | 0.00 | 0/2 matched (target 3) | `eq`, `hash` | 1/1 matched | _none_ | - | 2 | 20310.0 |
| 224 | `pagable.error` | `pagable.Error` | 0.00 | 0/1 matched | `from` | 1/1 matched (target 2) | _none_ | - | 1 | 10210.0 |
| 225 | `runtime.visit_span` | `runtime.VisitSpan` | 0.00 | 0/1 matched (target 19) | `visit_spans` | 1/1 matched | _none_ | - | 1 | 10210.0 |
| 226 | `any` | `starlark.Any` | 0.04 | 2/12 matched (target 3) | `static_type_id`, `static_type_of`, `is`, `test_can_convert`, `convert_value`, `convert_any`, `test_any_lifetime`, `test`, `test_provides_static_type_id`, `test_provides_static_type_when_type_parameter_has_bound_with_lifetime` | 3/15 matched (target 37) | `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar` | 0/7 | 22 | 8222709.5 |
| 227 | `stdlib.json` | `stdlib.Json` | 0.04 | 2/11 matched (target 24) | `alloc_value`, `alloc_frozen_value`, `json`, `encode`, `decode`, `test_json_encode`, `test_json_decode`, `test_json_very_large_int`, `test_json_128bit_and_beyond` | 0/1 matched (target 11) | `Canonical` | 0/4 | 10 | 101209.6 |
| 228 | `analysis` | `starlark.Analysis` | 0.05 | 1/12 matched (target 1) | `module`, `test_lint_suppressions_keyword_matching`, `test_lint_suppressions_fn_with_many_issues`, `test_lint_suppressions_preceding_whitespace`, `test_lint_suppressions_with_space_separator`, `test_lint_suppressions_multiline_span`, `test_lint_suppressions_small_span`, `test_lint_suppressions_data`, `test_lint_suppressions_line_before`, `test_lint_suppressions_line_before_windows_newlines`, `test_lint_suppressions_inside_fn` | 1/1 matched | _none_ | 0/11 | 11 | 111309.5 |
| 229 | `enumeration.globals` | `enumeration.Globals` | 0.12 | 1/5 matched (target 1) | `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr` | 0/0 matched | _none_ | 0/3 | 4 | 40508.8 |
| 230 | `stdlib.extra` | `stdlib.Extra` | 0.14 | 5/16 matched (target 21) | `fmt`, `print`, `pprint`, `pstr`, `prepr`, `test_filter`, `test_map`, `test_debug`, `test_print`, `test_pstr`, `test_prepr` | 3/4 matched (target 3) | `PrintHandlerImpl` | 0/6 | 12 | 122008.6 |
| 231 | `profile.mode` | `profile.Mode` | 0.15 | 1/4 matched | `fmt`, `name`, `from_str` | 1/2 matched (target 1) | `Err` | - | 4 | 40608.5 |
| 232 | `bool.globals` | `bool.Globals` | 0.19 | 1/2 matched (target 1) | `bool` | 0/0 matched | _none_ | - | 1 | 10208.1 |
| 233 | `int.i32` | `int.I32` | 0.23 | 2/4 matched (target 5) | `alloc_value`, `alloc_frozen_value` | 0/2 matched (target 1) | `Canonical`, `Error` | - | 4 | 40607.7 |
| 234 | `profile.csv` | `profile.Csv` | 0.24 | 6/10 matched (target 7) | `new`, `format_for_csv`, `test_csv_writer`, `test_quote_str_for_csv` | 1/3 matched (target 2) | `Impl`, `CsvValue` | 0/2 | 6 | 61307.6 |
| 235 | `wasm` | `starlark.Wasm` | 0.25 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 107.5 |
| 236 | `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 0.25 | 3/10 matched | `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter` | 2/5 matched (target 4) | `Target`, `Item`, `IntoIter` | - | 10 | 101507.5 |
| 237 | `typing.type_type` | `typing.TypeType` | 0.27 | 2/5 matched (target 3) | `test`, `module`, `takes_type` | 1/3 matched (target 1) | `Canonical`, `Error` | 0/3 | 5 | 2050807.2 |
| 238 | `analysis.types` | `analysis.Types` | 0.30 | 4/7 matched | `fmt`, `new`, `from` | 2/5 matched (target 2) | `LintWarning`, `LintT`, `EvalSeverity` | - | 6 | 61207.0 |
| 239 | `range.globals` | `range.Globals` | 0.30 | 1/2 matched (target 1) | `range` | 0/0 matched | _none_ | - | 1 | 10207.0 |
| 240 | `namespace.globals` | `namespace.Globals` | 0.30 | 1/2 matched (target 1) | `namespace` | 0/0 matched | _none_ | - | 1 | 10207.0 |
| 241 | `stdlib.internal` | `stdlib.Internal` | 0.31 | 2/4 matched (target 2) | `ty_of_value_debug`, `test_ty_of_value_debug` | 0/0 matched | _none_ | 0/1 | 2 | 20406.9 |
| 242 | `typing.small_arc_vec` | `typing.SmallArcVec` | 0.31 | 4/11 matched (target 16) | `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter` | 2/3 matched (target 5) | `Target` | - | 8 | 81406.9 |
| 243 | `tuple.globals` | `tuple.Globals` | 0.31 | 1/2 matched (target 1) | `tuple` | 0/0 matched | _none_ | - | 1 | 10206.9 |
| 244 | `__derive_refs.invoke_macro_error` | `deriverefs.InvokeMacroError [PROVENANCE-FALLBACK]` | 0.31 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 206.9 |
| 245 | `num.globals` | `num.Globals` | 0.32 | 1/2 matched (target 1) | `abs` | 0/0 matched | _none_ | - | 1 | 10206.8 |
| 246 | `num.value` | `num.Value` | 0.32 | 11/22 matched (target 27) | `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul`, `test_from_value`, `test_conversion_to_float`, `test_conversion_to_int`, `test_hashing`, `test_eq` | 3/4 matched (target 6) | `Output` | 0/5 | 12 | 122606.8 |
| 247 | `typing.user` | `typing.User` | 0.32 | 13/27 matched (target 26) | `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`, `test_intersect_with_abstract_type`, `test_ty_user_intersects_with_base_starlark_value` | 5/8 matched | `AbstractPlant`, `FruitCallable`, `Fruit` | 0/10 | 17 | 173506.8 |
| 248 | `util.refcell` | `refcell.RefCell` | 0.32 | 1/2 matched (target 11) | `test_unleak_borrow` | 0/0 matched (target 3) | _none_ | 0/1 | 1 | 20010206.0 |
| 249 | `float.unpack` | `float.Unpack` | 0.33 | 2/3 matched | `test_unpack_float` | 1/3 matched (target 1) | `Canonical`, `Error` | 0/1 | 3 | 30606.7 |
| 250 | `dict.traits` | `dict.Traits` | 0.33 | 4/4 matched (target 7) | _none_ | 0/2 matched (target 6) | `Canonical`, `Error` | - | 2 | 20606.7 |
| 251 | `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 0.34 | 1/2 matched (target 1) | `drop` | 0/1 matched (target 0) | `WriteRemOnDrop` | - | 2 | 20306.6 |
| 252 | `collections.aligned_padded_str` | `alignedpaddedstr.AlignedPaddedStr` | 0.34 | 2/3 matched (target 4) | `eq` | 1/1 matched | _none_ | - | 1 | 2010406.6 |
| 253 | `values.demand` | `values.Demand` | 0.37 | 4/7 matched (target 5) | `payload`, `provide`, `test_trait_downcast` | 1/4 matched (target 1) | `SomeTrait`, `StaticType`, `MyValue` | 0/3 | 6 | 4061106.2 |
| 254 | `list.list_type` | `list.ListType` | 0.37 | 1/2 matched (target 5) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 3030506.2 |
| 255 | `profile.alloc_counts` | `profile.AllocCounts` | 0.40 | 1/4 matched (target 5) | `normalize_for_golden_tests`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 0/1 | 4 | 4040606.0 |
| 256 | `util.arc_or_static` | `util.ArcOrStatic` | 0.42 | 5/10 matched (target 9) | `fmt`, `eq`, `partial_cmp`, `cmp`, `hash` | 2/3 matched (target 4) | `Target` | - | 6 | 2061305.9 |
| 257 | `bc.definitely_assigned` | `bc.DefinitelyAssigned` | 0.42 | 2/4 matched (target 7) | `new`, `assert_smaller_then` | 1/1 matched | _none_ | - | 2 | 20505.8 |
| 258 | `string.dot_format` | `string.DotFormat` | 0.43 | 7/11 matched (target 7) | `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one` | 1/1 matched | _none_ | 0/4 | 4 | 1041205.7 |
| 259 | `analysis.underscore` | `analysis.Underscore` | 0.44 | 8/13 matched (target 14) | `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored` | 1/1 matched (target 3) | _none_ | 0/4 | 5 | 51405.6 |
| 260 | `namespace.value` | `namespace.Value` | 0.44 | 9/15 matched (target 23) | `new`, `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_kwargs` | 2/2 matched (target 4) | _none_ | 0/4 | 6 | 61705.6 |
| 261 | `analysis.performance` | `analysis.Performance` | 0.45 | 6/10 matched (target 14) | `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function` | 1/1 matched (target 4) | _none_ | 0/3 | 4 | 41105.5 |
| 262 | `stdlib.breakpoint` | `stdlib.Breakpoint` | 0.45 | 11/17 matched (target 13) | `global`, `breakpoint`, `reset_global_state`, `test_breakpoint_real`, `test_breakpoint_mock`, `test_breakpoint_disabled` | 5/6 matched | `Handler` | 0/4 | 7 | 1072305.5 |
| 263 | `enumeration.value` | `enumeration.Value` | 0.46 | 6/9 matched (target 10) | `fmt`, `index`, `value` | 1/1 matched (target 8) | _none_ | - | 3 | 31005.4 |
| 264 | `analysis.names` | `analysis.Names` | 0.48 | 21/35 matched (target 31) | `new`, `ident`, `assign_ident`, `lint`, `about`, `test_lint_unused`, `test_lint_duplicate_assign`, `test_lint_unassigned`, `test_lint_undefined`, `test_early_fail`, `test_assign_for_next`, `test_flow_control`, `test_lambda_capture`, `test_global_defined_later` | 7/8 matched (target 13) | `AstStrExt` | 0/10 | 15 | 154305.2 |
| 265 | `analysis.dubious` | `analysis.Dubious` | 0.48 | 7/12 matched (target 19) | `lint`, `module`, `about`, `test_lint_duplicate_keys`, `test_lint_identifier_as_statement` | 1/2 matched (target 8) | `Key` | 0/4 | 6 | 61405.2 |
| 266 | `avalues.list` | `avalues.List` | 0.48 | 9/10 matched (target 19) | `alloc_list_concat` | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 3 | 31405.2 |
| 267 | `environment.module_dump` | `environment.ModuleDump` | 0.48 | 1/1 matched (target 2) | _none_ | 0/0 matched | _none_ | - | 0 | 105.2 |
| 268 | `bool.value` | `bool.Value` | 0.49 | 8/9 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 11005.1 |
| 269 | `types.any_complex` | `types.AnyComplex` | 0.49 | 4/7 matched | `fmt`, `test_any_complex`, `freeze` | 1/5 matched (target 1) | `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData` | 0/2 | 7 | 1071205.1 |
| 270 | `tuple.alloc` | `tuple.Alloc` | 0.49 | 3/5 matched (target 3) | `test_alloc_tuple`, `test_alloc_frozen_tuple` | 1/2 matched (target 1) | `Canonical` | 0/2 | 3 | 30705.1 |
| 271 | `runtime.inlined_frame` | `runtime.InlinedFrame` | 0.50 | 5/9 matched (target 6) | `eq`, `test_inline_into`, `make_span`, `assert_stack` | 3/3 matched | _none_ | 0/3 | 4 | 41205.0 |
| 272 | `bc.native_function` | `bc.NativeFunction` | 0.51 | 3/4 matched | `fun` | 1/1 matched | _none_ | - | 1 | 4010505.0 |
| 273 | `dict.refs` | `dict.Refs` | 0.51 | 7/9 matched (target 13) | `from_value`, `deref` | 4/7 matched (target 11) | `Target`, `Canonical`, `Error` | - | 5 | 51604.9 |
| 274 | `profile.bc` | `profile.Bc` | 0.52 | 12/19 matched (target 24) | `sum`, `add_assign`, `default`, `test_smoke`, `test_smoke_2`, `test_bc_profile_data_merge`, `test_bc_pairs_profile_data_merge` | 9/10 matched (target 13) | `Data` | 0/4 | 8 | 82904.8 |
| 275 | `analysis.flow` | `analysis.Flow` | 0.52 | 16/24 matched (target 31) | `lint`, `module`, `about`, `test_lint_returns`, `test_lint_unreachable`, `test_lint_redundant`, `test_lint_misplaced_load`, `test_lint_no_effect` | 1/1 matched (target 11) | _none_ | 0/7 | 8 | 82504.8 |
| 276 | `types.ellipsis` | `types.Ellipsis` | 0.55 | 2/3 matched (target 4) | `test_ellipsis` | 1/1 matched | _none_ | 0/1 | 1 | 4010404.5 |
| 277 | `analysis.find_call_name` | `analysis.FindCallName` | 0.55 | 2/3 matched (target 8) | `finds_function_calls_with_name_kwarg` | 1/1 matched | _none_ | 0/1 | 1 | 10404.5 |
| 278 | `dict.unpack` | `dict.Unpack` | 0.55 | 2/3 matched | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 30604.5 |
| 279 | `profile.data` | `profile.Data` | 0.55 | 4/6 matched (target 5) | `_assert_profile_data_send_sync`, `_assert_send_sync` | 3/3 matched (target 18) | _none_ | - | 2 | 20904.5 |
| 280 | `typing.callable_param` | `typing.CallableParam` | 0.56 | 16/20 matched (target 27) | `fmt`, `pf`, `new_named_only`, `test_param_spec_display` | 5/6 matched (target 10) | `ParamSpecDisplay` | 0/1 | 5 | 52604.4 |
| 281 | `analysis.incompatible` | `analysis.Incompatible` | 0.58 | 10/14 matched (target 17) | `lint`, `module`, `test_lint_incompatible`, `test_lint_duplicate_top_level_assign` | 1/1 matched (target 3) | _none_ | 0/3 | 4 | 41504.2 |
| 282 | `typing.callable` | `typing.Callable` | 0.58 | 6/7 matched (target 10) | `fmt` | 1/2 matched (target 1) | `TyCallableInner` | - | 2 | 20904.2 |
| 283 | `profile.flamegraph` | `profile.Flamegraph` | 0.59 | 6/10 matched (target 13) | `new`, `test_flamegraph_writer`, `test_flamegraph_data`, `test_merge` | 3/3 matched | _none_ | 0/3 | 4 | 41304.1 |
| 284 | `types.unbound` | `types.Unbound` | 0.60 | 3/4 matched | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 10504.0 |
| 285 | `bc.bytecode` | `bc.Bytecode` | 0.60 | 6/7 matched (target 11) | `handle` | 1/2 matched (target 1) | `HandlerImpl` | - | 2 | 20904.0 |
| 286 | `typing.interface` | `typing.Interface` | 0.60 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 5000404.0 |
| 287 | `oracle.traits` | `oracle.Traits` | 0.60 | 1/1 matched (target 3) | _none_ | 2/2 matched | _none_ | - | 0 | 304.0 |
| 288 | `profile.time_flame` | `profile.TimeFlame` | 0.60 | 15/19 matched (target 18) | `r#gen`, `test_time_flame_works_inside_frozen_module`, `register_sleep`, `sleep` | 10/11 matched (target 15) | `Data` | 0/3 | 5 | 53004.0 |
| 289 | `typing.arc_ty` | `typing.ArcTy` | 0.60 | 6/7 matched (target 16) | `fmt` | 3/4 matched (target 10) | `Target` | - | 2 | 5021104.0 |
| 290 | `compiler.args` | `compiler.Args` | 0.60 | 10/11 matched | `args` | 1/2 matched (target 1) | `Never` | - | 2 | 21304.0 |
| 291 | `values.starlark_type_id` | `values.StarlarkTypeId` | 0.61 | 5/6 matched (target 7) | `eq` | 2/2 matched | _none_ | - | 1 | 7010804.0 |
| 292 | `values.error` | `values.Error` | 0.62 | 4/5 matched | `from` | 2/2 matched (target 20) | _none_ | - | 1 | 17010704.0 |
| 293 | `symbol.symbol` | `symbol.Symbol` | 0.63 | 7/9 matched (target 11) | `fmt`, `eq` | 1/1 matched | _none_ | - | 2 | 21003.7 |
| 294 | `typing.structs` | `typing.Structs` | 0.63 | 7/8 matched (target 9) | `fmt` | 2/2 matched | _none_ | - | 1 | 1011003.7 |
| 295 | `tuple.refs` | `tuple.Refs` | 0.64 | 6/7 matched (target 15) | `unpack_value_impl` | 2/4 matched (target 2) | `Canonical`, `Error` | - | 3 | 31103.6 |
| 296 | `runtime.frame_span` | `runtime.FrameSpan` | 0.65 | 3/4 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 26010504.0 |
| 297 | `namespace.typing` | `namespace.Typing` | 0.65 | 6/7 matched (target 8) | `fmt` | 3/3 matched | _none_ | - | 1 | 11003.5 |
| 298 | `layout.vtable` | `layout.Vtable` | 0.67 | 60/67 matched (target 65) | `value_ptr`, `value_ref`, `drop_in_place`, `fmt`, `as_allocative`, `total_memory_for_profile`, `as_serialize` | 4/6 matched (target 4) | `GetTypeId`, `GetAllocativeKey` | - | 9 | 97303.3 |
| 299 | `environment.slots` | `environment.Slots` | 0.67 | 8/8 matched (target 10) | _none_ | 3/3 matched | _none_ | - | 0 | 1103.3 |
| 300 | `compiler.call` | `compiler.Call` | 0.67 | 13/13 matched (target 14) | _none_ | 1/1 matched | _none_ | - | 0 | 1403.3 |
| 301 | `profile.profiler_type` | `profile.ProfilerType` | 0.69 | 1/1 matched | _none_ | 2/2 matched | _none_ | - | 0 | 6000303.0 |
| 302 | `type_compiled.type_matcher_factory` | `type_compiled.TypeMatcherFactory` | 0.69 | 3/3 matched (target 6) | _none_ | 3/3 matched | _none_ | - | 0 | 7000603.0 |
| 303 | `compiler.def_inline` | `compiler.DefInline` | 0.70 | 9/10 matched (target 9) | `new` | 4/4 matched (target 6) | _none_ | - | 1 | 11403.0 |
| 304 | `runtime.file_loader` | `runtime.FileLoader` | 0.70 | 1/1 matched (target 2) | _none_ | 3/3 matched | _none_ | - | 0 | 2000403.0 |
| 305 | `types.any` | `types.Any` | 0.71 | 4/5 matched | `fmt` | 1/2 matched (target 1) | `Canonical` | - | 2 | 20702.9 |
| 306 | `none.globals` | `none.Globals` | 0.71 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 102.9 |
| 307 | `compiler.opt_ctx` | `compiler.OptCtx` | 0.71 | 5/5 matched (target 13) | _none_ | 2/2 matched (target 4) | _none_ | - | 0 | 7000703.0 |
| 308 | `layout.value_not_special` | `layout.ValueNotSpecial` | 0.72 | 6/6 matched (target 7) | _none_ | 1/1 matched | _none_ | - | 0 | 702.8 |
| 309 | `scope.scope_resolver_globals` | `scope.ScopeResolverGlobals` | 0.72 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 5000403.0 |
| 310 | `typing.basic` | `typing.Basic` | 0.72 | 18/19 matched (target 20) | `fmt` | 1/1 matched (target 11) | _none_ | - | 1 | 12002.8 |
| 311 | `__derive_refs.parse_args` | `deriverefs.ParseArgs [PROVENANCE-FALLBACK]` | 0.72 | 8/8 matched | _none_ | 0/0 matched | _none_ | - | 0 | 802.8 |
| 312 | `none.none_or` | `none.NoneOr` | 0.73 | 7/7 matched (target 9) | _none_ | 1/3 matched (target 4) | `Canonical`, `Error` | - | 2 | 6021002.5 |
| 313 | `typing.globals` | `typing.Globals` | 0.74 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 102.6 |
| 314 | `assert.conformance` | `assert.Conformance` | 0.74 | 5/5 matched | _none_ | 1/1 matched | _none_ | - | 0 | 602.6 |
| 315 | `alloc.chunk_part` | `alloc.ChunkPart` | 0.75 | 11/15 matched (target 16) | `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`, `test_is_full` | 1/1 matched | _none_ | 0/4 | 4 | 2041602.5 |
| 316 | `compiler.compr` | `compiler.Compr` | 0.75 | 9/9 matched (target 12) | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 1202.5 |
| 317 | `params.display` | `params.Display` | 0.75 | 4/4 matched | _none_ | 3/3 matched (target 8) | _none_ | - | 0 | 76000704.0 |
| 318 | `analysis.lint_message` | `analysis.LintMessage` | 0.75 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 1000202.5 |
| 319 | `typing.ty` | `typing.Ty` | 0.75 | 49/50 matched (target 57) | `fmt` | 4/4 matched (target 6) | _none_ | - | 1 | 109015400.0 |
| 320 | `callable.param` | `callable.Param` | 0.76 | 1/1 matched (target 6) | _none_ | 2/2 matched (target 7) | _none_ | - | 0 | 302.4 |
| 321 | `typing.ctx` | `typing.Ctx` | 0.76 | 19/19 matched (target 20) | _none_ | 1/1 matched (target 2) | _none_ | - | 0 | 2002.4 |
| 322 | `runtime.slots` | `runtime.Slots` | 0.76 | 2/2 matched (target 3) | _none_ | 3/3 matched | _none_ | - | 0 | 502.4 |
| 323 | `profile.heap` | `profile.Heap` | 0.77 | 11/13 matched (target 27) | `r#gen`, `test_profiling` | 10/11 matched | `Data` | 0/1 | 3 | 32402.3 |
| 324 | `layout.value_captured` | `layout.ValueCaptured` | 0.78 | 4/4 matched (target 9) | _none_ | 2/4 matched (target 2) | `Canonical`, `Frozen` | - | 2 | 1020802.2 |
| 325 | `compiler.known` | `compiler.Known` | 0.78 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 102.2 |
| 326 | `values.comparison` | `values.Comparison` | 0.79 | 5/5 matched | _none_ | 0/0 matched | _none_ | - | 0 | 502.1 |
| 327 | `compiler.expr_bool` | `compiler.ExprBool` | 0.79 | 4/4 matched (target 5) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 502.1 |
| 328 | `unused_loads.find` | `unusedloads.Find` | 0.79 | 4/4 matched (target 8) | _none_ | 3/3 matched | _none_ | - | 0 | 702.1 |
| 329 | `oracle.ctx` | `oracle.Ctx` | 0.79 | 32/32 matched | _none_ | 2/2 matched (target 14) | _none_ | - | 0 | 3402.1 |
| 330 | `typing.macro_refs` | `typing.MacroRefs` | 0.80 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 202.0 |
| 331 | `funcs.zip` | `funcs.Zip` | 0.80 | 4/4 matched (target 7) | _none_ | 1/1 matched | _none_ | - | 0 | 502.0 |
| 332 | `enumeration.matcher` | `enumeration.Matcher` | 0.82 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 201.8 |
| 333 | `__derive_refs.param_spec` | `deriverefs.ParamSpec [PROVENANCE-FALLBACK]` | 0.83 | 5/5 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 20000802.0 |
| 334 | `types.known_methods` | `types.KnownMethods` | 0.83 | 5/5 matched | _none_ | 2/2 matched | _none_ | - | 0 | 701.7 |
| 335 | `bc.instr_impl` | `bc.InstrImpl` | 0.83 | 7/7 matched (target 95) | _none_ | 87/163 matched (target 104) | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` | - | 76 | 777001.7 |
| 336 | `eval` | `eval.Eval` | 0.84 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 201.6 |
| 337 | `eval.soft_error` | `eval.SoftError` | 0.84 | 1/1 matched | _none_ | 2/2 matched | _none_ | - | 0 | 301.6 |
| 338 | `typing.macro_support` | `typing.MacroSupport` | 0.85 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 201.5 |
| 339 | `docs.multipage` | `docs.Multipage` | 0.86 | 6/6 matched | _none_ | 5/5 matched (target 7) | _none_ | - | 0 | 1101.4 |
| 340 | `runtime.before_stmt` | `runtime.BeforeStmt` | 0.86 | 4/4 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 701.4 |
| 341 | `compiler.module` | `compiler.Module` | 0.86 | 6/6 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 701.4 |
| 342 | `__derive_refs.sig` | `deriverefs.Sig [PROVENANCE-FALLBACK]` | 0.86 | 3/3 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 401.4 |
| 343 | `layout.identity` | `layout.Identity` | 0.87 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 201.3 |
| 344 | `type_compiled.alloc` | `type_compiled.Alloc` | 0.87 | 28/28 matched (target 37) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 2901.3 |
| 345 | `compiler.assign_modify` | `compiler.AssignModify` | 0.88 | 2/2 matched (target 3) | _none_ | 0/1 matched (target 0) | `AssignOnWriteBc` | - | 1 | 10301.2 |
| 346 | `compiler.type_expr` | `compiler.TypeExpr [PROVENANCE-FALLBACK]` | 0.89 | 2/2 matched (target 7) | _none_ | 1/1 matched (target 17) | _none_ | - | 0 | 301.1 |
| 347 | `num.typecheck` | `num.Typecheck` | 0.90 | 2/2 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 501.0 |
| 348 | `eval.params` | `eval.Params` | 0.91 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 100.9 |
| 349 | `compiler.span` | `compiler.Span` | 0.92 | 2/2 matched | _none_ | 1/2 matched (target 1) | `Target` | - | 1 | 29010400.0 |
| 350 | `record.matcher` | `record.Matcher` | 0.92 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 200.8 |
| 351 | `eval.bc.compiler.compr` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr` | 0.93 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 200.7 |
| 352 | `type_compiled.factory` | `type_compiled.Factory` | 0.93 | 9/9 matched | _none_ | 1/2 matched (target 1) | `Result` | - | 1 | 11100.7 |
| 353 | `bool.alloc` | `bool.Alloc` | 0.95 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 200.5 |
| 354 | `hint` | `starlark.Hint [PROVENANCE-FALLBACK]` | 0.95 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 2000200.5 |
| 355 | `funcs.globals` | `funcs.Globals` | 0.99 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 100.1 |
| 356 | `layout.value_lifetimeless` | `layout.ValueLifetimeless` | 1.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 12000100.0 |
| 357 | `types.bool` | `types.Bool` | 1.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000000.0 |
| 358 | `allocator.api` | `allocator.Api` | 1.00 | 0/0 matched | _none_ | 2/2 matched | _none_ | - | 0 | 200.0 |
| 359 | `bc.instr` | `bc.Instr` | 1.00 | 0/0 matched | _none_ | 2/2 matched (target 5) | _none_ | - | 0 | 200.0 |
| 360 | `bc.slow_arg` | `bc.SlowArg` | 1.00 | 0/0 matched | _none_ | 2/2 matched | _none_ | - | 0 | 200.0 |
| 361 | `profile.or_instrumentation` | `profile.OrInstrumentation` | 1.00 | 0/0 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 100.0 |
| 362 | `typing.call_args` | `typing.CallArgs` | 1.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 100.0 |
| 363 | `typing.mode` | `typing.Mode` | 1.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 100.0 |
| 364 | `values` | `values.Values` | 1.00 | 0/0 matched | _none_ | 0/0 matched (target 10) | _none_ | - | 0 | 0.0 |

## Cheat Detection / Scoring Failures

- `layout.value` -> `layout.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `val_ref` in Kotlin comments; Value.kt: unchecked cast suppression hiding transliteration work in Kotlin code; Value.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
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
- `collections.symbol` -> `collections.Symbol [STUB]`: function-by-function score forced to 0. Symbol.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `private` -> `starlark.Private [ZERO]`: function-by-function score forced to 0. Private.kt: snake_case identifier `missing_docs` in Kotlin comments
- `layout.avalue` -> `layout.AValue [ZERO]`: function-by-function score forced to 0. AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]`: function-by-function score forced to 0. ConstFrozenString.kt: snake_case identifier `constant_string` in Kotlin comments
- `typing.tuple` -> `typing.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `type_compiled_factory` in Kotlin comments
- `int.inline_int` -> `int.InlineInt [ZERO]`: function-by-function score forced to 0. InlineInt.kt: snake_case identifier `derive_more` in Kotlin comments; InlineInt.kt: Rust `fn` declaration in Kotlin comments; InlineInt.kt: Rust `pub` item in Kotlin comments; InlineInt.kt: Rust attribute syntax in Kotlin comments; InlineInt.kt: score-padding suppression annotation `@Suppress` in Kotlin code; InlineInt.kt: Rust lifetime explanation in Kotlin comments
- `int.pointer_i32` -> `int.PointerI32 [ZERO]`: function-by-function score forced to 0. PointerI32.kt: snake_case identifier `HAS_equals` in Kotlin code; PointerI32.kt: snake_case identifier `from_raw_pointer_unchecked` in Kotlin comments; PointerI32.kt: Rust `fn` declaration in Kotlin comments; PointerI32.kt: Rust `pub` item in Kotlin comments; PointerI32.kt: score-padding suppression annotation `@Suppress` in Kotlin code; PointerI32.kt: Rust lifetime explanation in Kotlin comments
- `layout.aligned_size` -> `layout.AlignedSize [ZERO]`: function-by-function score forced to 0. AlignedSize.kt: snake_case identifier `size_of` in Kotlin comments; AlignedSize.kt: Rust macro invocation in Kotlin comments; AlignedSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `cast` -> `starlark.Cast [ZERO]`: function-by-function score forced to 0. Cast.kt: Rust lifetime explanation in Kotlin comments; Cast.kt: Rust-only type/unsafe terminology in Kotlin comments
- `eval.compiler` -> `eval.Compiler [ZERO]`: function-by-function score forced to 0. Compiler.kt: snake_case identifier `def_inline` in Kotlin comments
- `types.bigint` -> `types.Bigint [ZERO]`: function-by-function score forced to 0. Bigint.kt: snake_case identifier `HAS_equals` in Kotlin code; Bigint.kt: snake_case identifier `non_zero_int` in Kotlin comments; Bigint.kt: Rust attribute syntax in Kotlin comments; Bigint.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Bigint.kt: Rust lifetime explanation in Kotlin comments
- `runtime.frozen_file_span` -> `runtime.FrozenFileSpan [ZERO]`: function-by-function score forced to 0. FrozenFileSpan.kt: snake_case identifier `empty_static` in Kotlin comments
- `runtime.small_duration` -> `runtime.SmallDuration [ZERO]`: function-by-function score forced to 0. SmallDuration.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `dict.dict_type` -> `dict.DictType [ZERO]`: function-by-function score forced to 0. DictType.kt: snake_case identifier `starlark_type_repr` in Kotlin comments
- `typing.typecheck` -> `typing.Typecheck [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies
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
- `compiler.constants` -> `compiler.Constants [ZERO]`: function-by-function score forced to 0. Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments
- `profile.instant` -> `profile.Instant [ZERO]`: function-by-function score forced to 0. Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust attribute syntax in Kotlin comments
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]`: function-by-function score forced to 0. UnpackAndDiscard.kt: snake_case identifier `unpack_value_impl` in Kotlin comments; UnpackAndDiscard.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `sealed` -> `starlark.Sealed [ZERO]`: function-by-function score forced to 0. Sealed.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `types.record` -> `types.Record [ZERO]`: function-by-function score forced to 0. Record.kt: snake_case identifier `record_type` in Kotlin comments; Record.kt: Rust `pub` item in Kotlin comments; Record.kt: Rust `use` path in Kotlin comments
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]`: function-by-function score forced to 0. SmallVec1.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]`: function-by-function score forced to 0. ConstTypeId.kt: snake_case identifier `const_type_id` in Kotlin comments; ConstTypeId.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]`: function-by-function score forced to 0. RustLoc.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]`: function-by-function score forced to 0. OwnedFrozenRef.kt: translator-note comment (`Kotlin:`) in Kotlin comments; OwnedFrozenRef.kt: Rust lifetime explanation in Kotlin comments
- `avalues.str_` -> `avalues.Str [ZERO]`: function-by-function score forced to 0. Str.kt: snake_case identifier `alloc_str_init` in Kotlin comments; Str.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.stack_guard` -> `values.StackGuard [ZERO]`: function-by-function score forced to 0. StackGuard.kt: snake_case identifier `to_str` in Kotlin comments
- `collections.string_pool` -> `collections.StringPool [ZERO]`: function-by-function score forced to 0. StringPool.kt: snake_case identifier `debug_assert` in Kotlin comments; StringPool.kt: Rust macro invocation in Kotlin comments; StringPool.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]`: function-by-function score forced to 0. LocalAsValue.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; LocalAsValue.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `profile.string_index` -> `profile.StringIndex [ZERO]`: function-by-function score forced to 0. StringIndex.kt: translator-note comment (`Kotlin:`) in Kotlin comments
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
- `avalues.simple` -> `avalues.Simple [ZERO]`: function-by-function score forced to 0. Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments
- `record.field` -> `record.Field [ZERO]`: function-by-function score forced to 0. Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]`: function-by-function score forced to 0. CheapCallStack.kt: translator-note comment (`Kotlin:`) in Kotlin comments; CheapCallStack.kt: Rust lifetime explanation in Kotlin comments
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]`: function-by-function score forced to 0. UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments
- `heap.fast_cell` -> `heap.FastCell [ZERO]`: function-by-function score forced to 0. FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust macro invocation in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments
- `read_line` -> `starlark.ReadLine [ZERO]`: function-by-function score forced to 0. ReadLine.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `typing.bindings` -> `typing.Bindings [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments
- `types.int` -> `types.Int [ZERO]`: function-by-function score forced to 0. Int.kt: snake_case identifier `num_bigint` in Kotlin comments
- `typing` -> `starlark.Typing [STUB]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.function` -> `typing.Function [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Function.kt: Rust attribute syntax in Kotlin comments
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
- `dict.value` -> `dict.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.value` -> `list.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]`: function-by-function score forced to 0. VtableRegistry.kt: snake_case identifier `type_name` in Kotlin comments; VtableRegistry.kt: Rust lifetime explanation in Kotlin comments
- `record.globals` -> `record.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `rec_type` in Kotlin comments
- `alloc.chain` -> `alloc.Chain [ZERO]`: function-by-function score forced to 0. Chain.kt: Rust attribute syntax in Kotlin comments
- `heap.heap_type` -> `heap.HeapType [ZERO]`: function-by-function score forced to 0. HeapType.kt: snake_case identifier `into_ref` in Kotlin comments; HeapType.kt: score-padding suppression annotation `@Suppress` in Kotlin code; HeapType.kt: Rust lifetime explanation in Kotlin comments; HeapType.kt: Rust-only type/unsafe terminology in Kotlin comments
- `range.range_type` -> `range.RangeType [ZERO]`: function-by-function score forced to 0. RangeType.kt: snake_case identifier `HAS_iterate` in Kotlin code; RangeType.kt: snake_case identifier `saturating_mul` in Kotlin comments
- `stdlib.partial` -> `stdlib.Partial [ZERO]`: function-by-function score forced to 0. Partial.kt: snake_case identifier `HAS_invoke` in Kotlin code; Partial.kt: snake_case identifier `alloca_concat` in Kotlin comments
- `alloc.allocator` -> `alloc.Allocator [ZERO]`: function-by-function score forced to 0. Allocator.kt: Rust `pub` item in Kotlin comments; Allocator.kt: Rust lifetime explanation in Kotlin comments; Allocator.kt: Rust-only type/unsafe terminology in Kotlin comments
- `tuple.unpack` -> `tuple.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]`: function-by-function score forced to 0. Compiled.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Compiled.kt: snake_case identifier `check_matches` in Kotlin comments
- `dict.methods` -> `dict.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: Rust lifetime explanation in Kotlin comments
- `docs.parse` -> `docs.Parse [ZERO]`: function-by-function score forced to 0. Parse.kt: snake_case identifier `some_function` in Kotlin comments; Parse.kt: Rust attribute syntax in Kotlin comments
- `funcs.other` -> `funcs.Other [ZERO]`: function-by-function score forced to 0. Other.kt: Rust lifetime explanation in Kotlin comments
- `layout.complex` -> `layout.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `new_err` in Kotlin comments; Complex.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Complex.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Complex.kt: Rust lifetime explanation in Kotlin comments
- `profile.aggregated` -> `profile.Aggregated [ZERO]`: function-by-function score forced to 0. Aggregated.kt: snake_case identifier `string_index` in Kotlin comments
- `record.ty_record_type` -> `record.TyRecordType [ZERO]`: function-by-function score forced to 0. TyRecordType.kt: Rust `pub` item in Kotlin comments; TyRecordType.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `string.simd` -> `string.Simd [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Simd.kt: snake_case identifier `x86_64` in Kotlin comments; Simd.kt: snake_case identifier `find_hash_in_array_without_simd` in Kotlin comments
- `structs.value` -> `structs.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_equals` in Kotlin code; Value.kt: snake_case identifier `of_value` in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments
- `tuple.value` -> `tuple.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code
- `typed.string` -> `typed.String [ZERO]`: function-by-function score forced to 0. String.kt: snake_case identifier `HAS_equals` in Kotlin code
- `adapter.implementation` -> `adapter.Implementation [ZERO]`: function-by-function score forced to 0. Implementation.kt: snake_case identifier `before_stmt` in Kotlin comments
- `assert.assert` -> `assert.Assert [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Assert.kt: snake_case identifier `assert_eq` in Kotlin comments; Assert.kt: Rust `pub` item in Kotlin comments; Assert.kt: Rust `use` path in Kotlin comments; Assert.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Assert.kt: Rust lifetime explanation in Kotlin comments
- `bc.instrs` -> `bc.Instrs [ZERO]`: function-by-function score forced to 0. Instrs.kt: snake_case identifier `drop_in_place` in Kotlin comments; Instrs.kt: Rust attribute syntax in Kotlin comments; Instrs.kt: Rust `match` expression in Kotlin comments; Instrs.kt: Rust lifetime explanation in Kotlin comments; Instrs.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bigint.convert` -> `bigint.Convert [ZERO]`: function-by-function score forced to 0. Convert.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.scope` -> `compiler.Scope [ZERO]`: function-by-function score forced to 0. Scope.kt: snake_case identifier `scope_resolver_globals` in Kotlin comments
- `heap.send` -> `heap.Send [ZERO]`: function-by-function score forced to 0. Send.kt: Rust `fn` declaration in Kotlin comments; Send.kt: Rust lifetime explanation in Kotlin comments; Send.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.unpack` -> `list.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `unpack_value_opt` in Kotlin comments; Unpack.kt: Rust `let` binding in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]`: function-by-function score forced to 0. Bumpalo.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `debug.inspect` -> `debug.Inspect [ZERO]`: function-by-function score forced to 0. Inspect.kt: snake_case identifier `call_stack` in Kotlin comments
- `environment.modules` -> `environment.Modules [ZERO]`: function-by-function score forced to 0. Modules.kt: snake_case identifier `starlark_module` in Kotlin comments; Modules.kt: Rust `fn` declaration in Kotlin comments; Modules.kt: Rust `pub` item in Kotlin comments; Modules.kt: Rust attribute syntax in Kotlin comments; Modules.kt: Rust lifetime explanation in Kotlin comments
- `params.spec` -> `params.Spec [ZERO]`: function-by-function score forced to 0. Spec.kt: snake_case identifier `no_args` in Kotlin comments
- `profile.stmt` -> `profile.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `last_span` in Kotlin comments; Stmt.kt: Rust-only type/unsafe terminology in Kotlin comments
- `typing.iter` -> `typing.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Iter.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.owned` -> `values.Owned [ZERO]`: function-by-function score forced to 0. Owned.kt: Rust lifetime explanation in Kotlin comments
- `values.unpack` -> `values.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `starlark_module` in Kotlin comments
- `avalues.static_` -> `avalues.Static [ZERO]`: function-by-function score forced to 0. Static.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `bc.addr` -> `bc.Addr [ZERO]`: function-by-function score forced to 0. Addr.kt: snake_case identifier `debug_assert` in Kotlin comments; Addr.kt: Rust macro invocation in Kotlin comments
- `dict.alloc` -> `dict.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments
- `heap.repr` -> `heap.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `forward_ptr` in Kotlin comments; Repr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.methods` -> `list.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Methods.kt: Rust `let` binding in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `params.parser` -> `params.Parser [ZERO]`: function-by-function score forced to 0. Parser.kt: snake_case identifier `get_next` in Kotlin comments
- `profile.typecheck` -> `profile.Typecheck [ZERO]`: function-by-function score forced to 0. Typecheck.kt: snake_case identifier `by_function` in Kotlin comments
- `set.set` -> `set.Set [ZERO]`: function-by-function score forced to 0. Set.kt: snake_case identifier `starlark_module` in Kotlin comments; Set.kt: Rust attribute syntax in Kotlin comments
- `string.methods` -> `string.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `is_true` in Kotlin comments; Methods.kt: Rust lifetime explanation in Kotlin comments
- `structs.alloc` -> `structs.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `fn` declaration in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments; Alloc.kt: Rust lifetime explanation in Kotlin comments; no target functions found; report scoring is function-by-function only
- `typing.custom` -> `typing.Custom [ZERO]`: function-by-function score forced to 0. Custom.kt: snake_case identifier `validate_call` in Kotlin comments; Custom.kt: Rust `fn` declaration in Kotlin comments; Custom.kt: Rust `pub` item in Kotlin comments; Custom.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.opcode` -> `bc.Opcode [ZERO]`: function-by-function score forced to 0. Opcode.kt: snake_case identifier `dispatch_all` in Kotlin comments
- `bc.repr` -> `bc.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `align_of` in Kotlin comments
- `debug.evaluate` -> `debug.Evaluate [ZERO]`: function-by-function score forced to 0. Evaluate.kt: snake_case identifier `module_env` in Kotlin comments
- `list.refs` -> `list.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `from_value` in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]`: function-by-function score forced to 0. AllocUnpack.kt: snake_case identifier `alloc_frozen_string_value` in Kotlin comments
- `symbol.map` -> `symbol.Map [ZERO]`: function-by-function score forced to 0. Map.kt: Rust-only type/unsafe terminology in Kotlin comments
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `eval_type` in Kotlin comments
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]`: function-by-function score forced to 0. Matcher.kt: snake_case identifier `type_matcher` in Kotlin comments; Matcher.kt: Rust attribute syntax in Kotlin comments; Matcher.kt: Rust lifetime explanation in Kotlin comments; Matcher.kt: Rust-only type/unsafe terminology in Kotlin comments; Matcher.kt: Rust auto-trait terminology in Kotlin comments
- `typing.never` -> `typing.Never [ZERO]`: function-by-function score forced to 0. Never.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Never.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing.ty` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]`: function-by-function score forced to 0. Ty.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `avalues.array` -> `avalues.Array [ZERO]`: function-by-function score forced to 0. Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.complex` -> `avalues.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `alloc_complex` in Kotlin comments
- `avalues.tuple` -> `avalues.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `register_special_avalue_frozen` in Kotlin comments; Tuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `bc.call` -> `bc.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `bc.instr_arg` -> `bc.InstrArg [ZERO]`: function-by-function score forced to 0. InstrArg.kt: snake_case identifier `fmt_append` in Kotlin comments; InstrArg.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]`: function-by-function score forced to 0. StackPtr.kt: snake_case identifier `assert_eq` in Kotlin comments; StackPtr.kt: Rust macro invocation in Kotlin comments; StackPtr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bool.type_repr` -> `bool.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `build` -> `starlark.Build [ZERO]`: function-by-function score forced to 0. Build.kt: snake_case identifier `rust_nightly` in Kotlin comments; Build.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]`: function-by-function score forced to 0. MaybeUninitBackport.kt: snake_case identifier `write_slice_cloned` in Kotlin comments
- `compiler.def` -> `compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: snake_case identifier `HAS_invoke` in Kotlin code; Def.kt: unchecked cast suppression hiding transliteration work in Kotlin code; Def.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.expr` -> `compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `get_methods` in Kotlin comments; Expr.kt: Rust `let` binding in Kotlin comments
- `eval.bc.compiler.stmt` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `funcs.min_max` -> `funcs.MinMax [ZERO]`: function-by-function score forced to 0. MinMax.kt: snake_case identifier `update_max_ordering` in Kotlin comments; MinMax.kt: Rust `match` expression in Kotlin comments
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]`: function-by-function score forced to 0. CallEnterExit.kt: snake_case identifier `needs_drop` in Kotlin comments
- `intern.interner` -> `intern.Interner [ZERO]`: function-by-function score forced to 0. Interner.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `list.globals` -> `list.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `register_list` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]`: function-by-function score forced to 0. SummaryByFunction.kt: snake_case identifier `top_stack` in Kotlin comments
- `set.refs` -> `set.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `unpack_value_opt` in Kotlin comments
- `structs.refs` -> `structs.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `debug_assert` in Kotlin comments
- `types.function` -> `types.Function [ZERO]`: function-by-function score forced to 0. Function.kt: snake_case identifier `HAS_invoke` in Kotlin code; Function.kt: snake_case identifier `starlark_module` in Kotlin comments; Function.kt: Rust attribute syntax in Kotlin comments
- `typing.any` -> `typing.Any [ZERO]`: function-by-function score forced to 0. Any.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `values.index` -> `values.Index [ZERO]`: function-by-function score forced to 0. Index.kt: snake_case identifier `set_at` in Kotlin comments
- `values.traits` -> `values.Traits [ZERO]`: function-by-function score forced to 0. Traits.kt: snake_case identifier `HAS_invoke` in Kotlin code; Traits.kt: snake_case identifier `starlark_value` in Kotlin comments; Traits.kt: Rust `let` binding in Kotlin comments; Traits.kt: Rust attribute syntax in Kotlin comments
- `values.type_repr` -> `values.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: snake_case identifier `type_repr` in Kotlin comments
- `alloc.per_thread` -> `alloc.PerThread [ZERO]`: function-by-function score forced to 0. PerThread.kt: snake_case identifier `thread_local` in Kotlin comments
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]`: function-by-function score forced to 0. IfCompiler.kt: snake_case identifier `maybe_not` in Kotlin comments
- `debug.adapter` -> `debug.Adapter [ZERO]`: function-by-function score forced to 0. Adapter.kt: snake_case identifier `Requests_SetBreakpoints` in Kotlin comments
- `dict.globals` -> `dict.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `as_type` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `docs` -> `docs.Docs [ZERO]`: function-by-function score forced to 0. Docs.kt: Rust lifetime explanation in Kotlin comments
- `eval.bc.compiler.call` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `float.globals` -> `float.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `int.globals` -> `int.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `profile.by_type` -> `profile.ByType [ZERO]`: function-by-function score forced to 0. ByType.kt: snake_case identifier `allocated_summary` in Kotlin comments
- `record.instance` -> `record.Instance [ZERO]`: function-by-function score forced to 0. Instance.kt: snake_case identifier `HAS_equals` in Kotlin code; Instance.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `structs.structs` -> `structs.Structs [ZERO]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `starlark_module` in Kotlin comments; Structs.kt: Rust attribute syntax in Kotlin comments
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]`: function-by-function score forced to 0. RecursiveReprOrJsonGuard.kt: snake_case identifier `to_json` in Kotlin comments
- `__derive_refs` -> `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. DeriveRefs.kt: snake_case identifier `derive_refs` in Kotlin comments; DeriveRefs.kt: Rust `pub` item in Kotlin comments; DeriveRefs.kt: Rust `use` path in Kotlin comments; DeriveRefs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `__derive_refs.components` -> `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Components.kt: snake_case identifier `set_function` in Kotlin comments
- `bc.for_loop` -> `bc.ForLoop [ZERO]`: function-by-function score forced to 0. ForLoop.kt: snake_case identifier `derive_more` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `bc.writer` -> `bc.Writer [ZERO]`: function-by-function score forced to 0. Writer.kt: snake_case identifier `alloc_any` in Kotlin comments
- `compiler.assign` -> `compiler.Assign [ZERO]`: function-by-function score forced to 0. Assign.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.error` -> `compiler.Error [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Error.kt: snake_case identifier `starlark_syntax` in Kotlin comments
- `compiler.types` -> `compiler.Types [ZERO]`: function-by-function score forced to 0. Types.kt: snake_case identifier `expr_ident` in Kotlin comments
- `docs.code` -> `docs.Code [ZERO]`: function-by-function score forced to 0. Code.kt: snake_case identifier `render_as_code` in Kotlin comments
- `docs.markdown` -> `docs.Markdown [ZERO]`: function-by-function score forced to 0. Markdown.kt: Rust `let` binding in Kotlin comments
- `environment` -> `starlark.Environment [ZERO]`: function-by-function score forced to 0. Environment.kt: snake_case identifier `module_dump` in Kotlin comments; Environment.kt: Rust `pub` item in Kotlin comments; Environment.kt: Rust `use` path in Kotlin comments
- `environment.names` -> `environment.Names [ZERO]`: function-by-function score forced to 0. Names.kt: snake_case identifier `collect_defines_lvalue` in Kotlin comments
- `errors` -> `starlark.Errors [STUB]`: function-by-function score forced to 0. Errors.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.def` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.expr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `mark_definitely_assigned_after` in Kotlin comments
- `eval.runtime` -> `eval.Runtime [STUB]`: function-by-function score forced to 0. Runtime.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `fuzz_targets.starlark` -> `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Starlark.kt: snake_case identifier `fuzz_target` in Kotlin comments; Starlark.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.avalues` -> `layout.AValues [STUB]`: function-by-function score forced to 0. AValues.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.static_string` -> `layout.StaticString [ZERO]`: function-by-function score forced to 0. StaticString.kt: Rust attribute syntax in Kotlin comments; StaticString.kt: Rust-only type/unsafe terminology in Kotlin comments
- `macros` -> `starlark.Macros [ZERO]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `pagable` -> `starlark.Pagable [STUB]`: function-by-function score forced to 0. Pagable.kt: snake_case identifier `type_name` in Kotlin comments; Pagable.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]`: function-by-function score forced to 0. VtableRegister.kt: snake_case identifier `register_avalue_simple_frozen` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `stdlib.funcs` -> `stdlib.Funcs [STUB]`: function-by-function score forced to 0. Funcs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `string.globals` -> `string.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `string.iter` -> `string.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_iterate` in Kotlin code; Iter.kt: snake_case identifier `produce_char` in Kotlin comments
- `syntax` -> `starlark.Syntax [STUB]`: function-by-function score forced to 0. Syntax.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]`: function-by-function score forced to 0. Matchers.kt: snake_case identifier `starlark_type_id` in Kotlin comments; Matchers.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.error` -> `typing.Error [ZERO]`: function-by-function score forced to 0. Error.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]`: function-by-function score forced to 0. FillTypesForLint.kt: snake_case identifier `of_value` in Kotlin comments; FillTypesForLint.kt: Rust `match` expression in Kotlin comments
- `typing.oracle` -> `typing.Oracle [STUB]`: function-by-function score forced to 0. Oracle.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]`: function-by-function score forced to 0. Remove.kt: Rust lifetime explanation in Kotlin comments
- `util` -> `starlark.Util [STUB]`: function-by-function score forced to 0. Util.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.types` -> `values.Types [STUB]`: function-by-function score forced to 0. Types.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing` -> `values.Typing [STUB]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `layout.value` -> `layout.Value [ZERO]` (0.00, 178 deps)
- `typing.starlark_value` -> `typing.StarlarkValue [ZERO]` (0.00, 76 deps)
- `runtime.evaluator` -> `runtime.Evaluator [ZERO]` (0.00, 56 deps)
- `values.trace` -> `values.Trace [ZERO]` (0.00, 52 deps)
- `values.freeze` -> `values.Freeze [ZERO]` (0.00, 42 deps)
- `values.alloc_value` -> `values.AllocValue [ZERO]` (0.00, 42 deps)
- `layout.freezer` -> `layout.Freezer [ZERO]` (0.00, 36 deps)
- `values.frozen_ref` -> `values.FrozenRef [ZERO]` (0.00, 27 deps)
- `none.none_type` -> `none.NoneType [ZERO]` (0.00, 27 deps)
- `runtime.arguments` -> `runtime.Arguments [ZERO]` (0.00, 25 deps)
- `typing.type_compiled` -> `type_compiled.TypeCompiled [STUB]` (0.00, 22 deps)
- `environment.globals` -> `environment.Globals [ZERO]` (0.00, 21 deps)
- `derive.module` -> `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]` (0.00, 21 deps)
- `values.value_of_unchecked` -> `values.ValueOfUnchecked [ZERO]` (0.00, 20 deps)
- `environment.methods` -> `environment.Methods [ZERO]` (0.00, 17 deps)
- `values.iter` -> `values.Iter [ZERO] [PROVENANCE-FALLBACK]` (0.00, 17 deps)
- `collections.symbol` -> `collections.Symbol [STUB]` (0.00, 15 deps)
- `private` -> `starlark.Private [ZERO]` (0.00, 15 deps)
- `layout.avalue` -> `layout.AValue [ZERO]` (0.00, 14 deps)
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]` (0.00, 12 deps)
- `typing.tuple` -> `typing.Tuple [ZERO]` (0.00, 12 deps)
- `int.inline_int` -> `int.InlineInt [ZERO]` (0.00, 11 deps)
- `int.pointer_i32` -> `int.PointerI32 [ZERO]` (0.00, 9 deps)
- `layout.aligned_size` -> `layout.AlignedSize [ZERO]` (0.00, 8 deps)
- `cast` -> `starlark.Cast [ZERO]` (0.00, 8 deps)
- `eval.compiler` -> `eval.Compiler [ZERO]` (0.00, 8 deps)
- `types.bigint` -> `types.Bigint [ZERO]` (0.00, 7 deps)
- `runtime.frozen_file_span` -> `runtime.FrozenFileSpan [ZERO]` (0.00, 7 deps)
- `runtime.small_duration` -> `runtime.SmallDuration [ZERO]` (0.00, 6 deps)
- `dict.dict_type` -> `dict.DictType [ZERO]` (0.00, 6 deps)
- `typing.typecheck` -> `typing.Typecheck [STUB]` (0.00, 6 deps)
- `values.freeze_error` -> `values.FreezeError [ZERO]` (0.00, 6 deps)
- `layout.value_alloc_size` -> `layout.ValueAllocSize [ZERO]` (0.00, 6 deps)
- `compiler.stmt` -> `compiler.Stmt [ZERO]` (0.00, 6 deps)
- `values.layout` -> `values.Layout [STUB]` (0.00, 6 deps)
- `tests.def` -> `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]` (0.00, 5 deps)
- `types.array` -> `types.Array [ZERO]` (0.00, 5 deps)
- `eval.bc` -> `bc.Bc [STUB]` (0.00, 5 deps)
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]` (0.00, 4 deps)
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]` (0.00, 4 deps)
- `bc.frame` -> `bc.Frame [ZERO]` (0.00, 4 deps)
- `values.value_of` -> `values.ValueOf [ZERO]` (0.00, 4 deps)
- `record.record_type` -> `record.RecordType [ZERO]` (0.00, 3 deps)
- `alloc.chunk` -> `alloc.Chunk [ZERO]` (0.00, 3 deps)
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]` (0.00, 3 deps)
- `errors.did_you_mean` -> `errors.DidYouMean [ZERO]` (0.00, 3 deps)
- `list.alloc` -> `list.Alloc [ZERO]` (0.00, 3 deps)
- `compiler.constants` -> `compiler.Constants [ZERO]` (0.00, 3 deps)
- `profile.instant` -> `profile.Instant [ZERO]` (0.00, 3 deps)
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]` (0.00, 3 deps)
- `sealed` -> `starlark.Sealed [ZERO]` (0.00, 3 deps)
- `types.record` -> `types.Record [ZERO]` (0.00, 3 deps)
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]` (0.00, 2 deps)
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]` (0.00, 2 deps)
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]` (0.00, 2 deps)
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]` (0.00, 2 deps)
- `avalues.str_` -> `avalues.Str [ZERO]` (0.00, 2 deps)
- `values.stack_guard` -> `values.StackGuard [ZERO]` (0.00, 2 deps)
- `collections.string_pool` -> `collections.StringPool [ZERO]` (0.00, 2 deps)
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]` (0.00, 2 deps)
- `profile.string_index` -> `profile.StringIndex [ZERO]` (0.00, 2 deps)
- `values.thin_box_slice_frozen_value` -> `values.ThinBoxSliceFrozenValue [STUB]` (0.00, 2 deps)
- `heap.arena` -> `heap.Arena [ZERO]` (0.00, 1 deps)
- `collections.alloca` -> `collections.Alloca [ZERO]` (0.00, 1 deps)
- `stdlib` -> `starlark.Stdlib [ZERO]` (0.00, 1 deps)
- `string.interpolation` -> `string.Interpolation [ZERO]` (0.00, 1 deps)
- `types.list_or_tuple` -> `types.ListOrTuple [ZERO]` (0.00, 1 deps)
- `layout.pointer` -> `layout.Pointer [ZERO]` (0.00, 1 deps)
- `types.any_array` -> `types.AnyArray [ZERO]` (0.00, 1 deps)
- `util.rtabort` -> `util.Rtabort [ZERO]` (0.00, 1 deps)
- `bc.if_debug` -> `bc.IfDebug [ZERO]` (0.00, 1 deps)
- `util.non_static_type_id` -> `util.NonStaticTypeId [ZERO]` (0.00, 1 deps)
- `avalues.simple` -> `avalues.Simple [ZERO]` (0.00, 1 deps)
- `record.field` -> `record.Field [ZERO]` (0.00, 1 deps)
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]` (0.00, 1 deps)
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]` (0.00, 1 deps)
- `heap.fast_cell` -> `heap.FastCell [ZERO]` (0.00, 1 deps)
- `read_line` -> `starlark.ReadLine [ZERO]` (0.00, 1 deps)
- `typing.bindings` -> `typing.Bindings [STUB]` (0.00, 1 deps)
- `types.int` -> `types.Int [ZERO]` (0.00, 1 deps)
- `typing` -> `starlark.Typing [STUB]` (0.00, 1 deps)
- `typing.function` -> `typing.Function [STUB]` (0.00, 1 deps)
- `set.methods` -> `set.Methods [STUB]` (0.00)
- `string.str_type` -> `string.StrType [ZERO]` (0.00)
- `int.int_or_big` -> `int.IntOrBig [STUB]` (0.00)
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]` (0.00)
- `set.value` -> `set.Value [ZERO]` (0.00)
- `values.typing.callable` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` (0.00)
- `float.float` -> `float.Float [ZERO]` (0.00)
- `layout.typed` -> `layout.ValueTyped [ZERO]` (0.00)
- `scope.payload` -> `scope.Payload [ZERO]` (0.00)
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]` (0.00)
- `string.repr` -> `string.Repr [ZERO]` (0.00)
- `dict.value` -> `dict.Value [ZERO]` (0.00)
- `list.value` -> `list.Value [ZERO]` (0.00)
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]` (0.00)
- `record.globals` -> `record.Globals [ZERO]` (0.00)
- `alloc.chain` -> `alloc.Chain [ZERO]` (0.00)
- `heap.heap_type` -> `heap.HeapType [ZERO]` (0.00)
- `range.range_type` -> `range.RangeType [ZERO]` (0.00)
- `stdlib.partial` -> `stdlib.Partial [ZERO]` (0.00)
- `alloc.allocator` -> `alloc.Allocator [ZERO]` (0.00)
- `tuple.unpack` -> `tuple.Unpack [ZERO]` (0.00)
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]` (0.00)
- `dict.methods` -> `dict.Methods [ZERO]` (0.00)
- `docs.parse` -> `docs.Parse [ZERO]` (0.00)
- `funcs.other` -> `funcs.Other [ZERO]` (0.00)
- `layout.complex` -> `layout.Complex [ZERO]` (0.00)
- `profile.aggregated` -> `profile.Aggregated [ZERO]` (0.00)
- `record.ty_record_type` -> `record.TyRecordType [ZERO]` (0.00)
- `string.simd` -> `string.Simd [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `structs.value` -> `structs.Value [ZERO]` (0.00)
- `tuple.value` -> `tuple.Value [ZERO]` (0.00)
- `typed.string` -> `typed.String [ZERO]` (0.00)
- `adapter.implementation` -> `adapter.Implementation [ZERO]` (0.00)
- `assert.assert` -> `assert.Assert [STUB]` (0.00)
- `bc.instrs` -> `bc.Instrs [ZERO]` (0.00)
- `bigint.convert` -> `bigint.Convert [ZERO]` (0.00)
- `compiler.scope` -> `compiler.Scope [ZERO]` (0.00)
- `heap.send` -> `heap.Send [ZERO]` (0.00)
- `list.unpack` -> `list.Unpack [ZERO]` (0.00)
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]` (0.00)
- `debug.inspect` -> `debug.Inspect [ZERO]` (0.00)
- `environment.modules` -> `environment.Modules [ZERO]` (0.00)
- `params.spec` -> `params.Spec [ZERO]` (0.00)
- `profile.stmt` -> `profile.Stmt [ZERO]` (0.00)
- `typing.iter` -> `typing.Iter [ZERO]` (0.00)
- `values.owned` -> `values.Owned [ZERO]` (0.00)
- `values.unpack` -> `values.Unpack [ZERO]` (0.00)
- `avalues.static_` -> `avalues.Static [ZERO]` (0.00)
- `bc.addr` -> `bc.Addr [ZERO]` (0.00)
- `dict.alloc` -> `dict.Alloc [ZERO]` (0.00)
- `heap.repr` -> `heap.Repr [ZERO]` (0.00)
- `list.methods` -> `list.Methods [ZERO]` (0.00)
- `params.parser` -> `params.Parser [ZERO]` (0.00)
- `profile.typecheck` -> `profile.Typecheck [ZERO]` (0.00)
- `set.set` -> `set.Set [ZERO]` (0.00)
- `string.methods` -> `string.Methods [ZERO]` (0.00)
- `structs.alloc` -> `structs.Alloc [ZERO]` (0.00)
- `typing.custom` -> `typing.Custom [ZERO]` (0.00)
- `bc.opcode` -> `bc.Opcode [ZERO]` (0.00)
- `bc.repr` -> `bc.Repr [ZERO]` (0.00)
- `debug.evaluate` -> `debug.Evaluate [ZERO]` (0.00)
- `list.refs` -> `list.Refs [ZERO]` (0.00)
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]` (0.00)
- `symbol.map` -> `symbol.Map [ZERO]` (0.00)
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]` (0.00)
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]` (0.00)
- `typing.never` -> `typing.Never [ZERO]` (0.00)
- `values.typing.ty` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` (0.00)
- `avalues.array` -> `avalues.Array [ZERO]` (0.00)
- `avalues.complex` -> `avalues.Complex [ZERO]` (0.00)
- `avalues.tuple` -> `avalues.Tuple [ZERO]` (0.00)
- `bc.call` -> `bc.Call [ZERO]` (0.00)
- `bc.instr_arg` -> `bc.InstrArg [ZERO]` (0.00)
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]` (0.00)
- `bool.type_repr` -> `bool.TypeRepr [ZERO]` (0.00)
- `build` -> `starlark.Build [ZERO]` (0.00)
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]` (0.00)
- `compiler.def` -> `compiler.Def [ZERO]` (0.00)
- `compiler.expr` -> `compiler.Expr [ZERO]` (0.00)
- `eval.bc.compiler.stmt` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` (0.00)
- `funcs.min_max` -> `funcs.MinMax [ZERO]` (0.00)
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]` (0.00)
- `intern.interner` -> `intern.Interner [ZERO]` (0.00)
- `list.globals` -> `list.Globals [ZERO]` (0.00)
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]` (0.00)
- `set.refs` -> `set.Refs [ZERO]` (0.00)
- `structs.refs` -> `structs.Refs [ZERO]` (0.00)
- `types.function` -> `types.Function [ZERO]` (0.00)
- `typing.any` -> `typing.Any [ZERO]` (0.00)
- `values.index` -> `values.Index [ZERO]` (0.00)
- `values.traits` -> `values.Traits [ZERO]` (0.00)
- `values.type_repr` -> `values.TypeRepr [ZERO]` (0.00)
- `alloc.per_thread` -> `alloc.PerThread [ZERO]` (0.00)
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]` (0.00)
- `debug.adapter` -> `debug.Adapter [ZERO]` (0.00)
- `dict.globals` -> `dict.Globals [ZERO]` (0.00)
- `docs` -> `docs.Docs [ZERO]` (0.00)
- `eval.bc.compiler.call` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` (0.00)
- `float.globals` -> `float.Globals [ZERO]` (0.00)
- `int.globals` -> `int.Globals [ZERO]` (0.00)
- `profile.by_type` -> `profile.ByType [ZERO]` (0.00)
- `record.instance` -> `record.Instance [ZERO]` (0.00)
- `structs.structs` -> `structs.Structs [ZERO]` (0.00)
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]` (0.00)
- `__derive_refs` -> `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]` (0.00)
- `__derive_refs.components` -> `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `bc.for_loop` -> `bc.ForLoop [ZERO]` (0.00)
- `bc.writer` -> `bc.Writer [ZERO]` (0.00)
- `compiler.assign` -> `compiler.Assign [ZERO]` (0.00)
- `compiler.error` -> `compiler.Error [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `compiler.types` -> `compiler.Types [ZERO]` (0.00)
- `docs.code` -> `docs.Code [ZERO]` (0.00)
- `docs.markdown` -> `docs.Markdown [ZERO]` (0.00)
- `environment` -> `starlark.Environment [ZERO]` (0.00)
- `environment.names` -> `environment.Names [ZERO]` (0.00)
- `errors` -> `starlark.Errors [STUB]` (0.00)
- `eval.bc.compiler.def` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` (0.00)
- `eval.bc.compiler.expr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` (0.00)
- `eval.runtime` -> `eval.Runtime [STUB]` (0.00)
- `fuzz_targets.starlark` -> `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `layout.avalues` -> `layout.AValues [STUB]` (0.00)
- `layout.static_string` -> `layout.StaticString [ZERO]` (0.00)
- `macros` -> `starlark.Macros [ZERO]` (0.00)
- `pagable` -> `starlark.Pagable [STUB]` (0.00)
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]` (0.00)
- `stdlib.funcs` -> `stdlib.Funcs [STUB]` (0.00)
- `string.globals` -> `string.Globals [ZERO]` (0.00)
- `string.iter` -> `string.Iter [ZERO]` (0.00)
- `syntax` -> `starlark.Syntax [STUB]` (0.00)
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]` (0.00)
- `typing.error` -> `typing.Error [ZERO]` (0.00)
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]` (0.00)
- `typing.oracle` -> `typing.Oracle [STUB]` (0.00)
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]` (0.00)
- `util` -> `starlark.Util [STUB]` (0.00)
- `values.types` -> `values.Types [STUB]` (0.00)
- `values.typing` -> `values.Typing [STUB]` (0.00)
- `types.type_instance_id` -> `types.TypeInstanceId` (0.00, 9 deps)
- `tuple.rust_tuple` -> `tuple.RustTuple` (0.00)
- `bool.unpack` -> `bool.Unpack` (0.00)
- `enumeration.ty_enum_type` -> `enumeration.TyEnumType` (0.00)
- `pagable.error` -> `pagable.Error` (0.00)
- `runtime.visit_span` -> `runtime.VisitSpan` (0.00)
- `any` -> `starlark.Any` (0.04, 8 deps)
- `stdlib.json` -> `stdlib.Json` (0.04)
- `analysis` -> `starlark.Analysis` (0.05)
- `enumeration.globals` -> `enumeration.Globals` (0.12)
- `stdlib.extra` -> `stdlib.Extra` (0.14)
- `profile.mode` -> `profile.Mode` (0.15)
- `bool.globals` -> `bool.Globals` (0.19)
- `int.i32` -> `int.I32` (0.23)
- `profile.csv` -> `profile.Csv` (0.24)
- `wasm` -> `starlark.Wasm` (0.25)
- `typing.small_arc_vec_or_static` -> `typing.SmallArcVecOrStatic` (0.25)
- `typing.type_type` -> `typing.TypeType` (0.27, 2 deps)
- `analysis.types` -> `analysis.Types` (0.30)
- `range.globals` -> `range.Globals` (0.30)
- `namespace.globals` -> `namespace.Globals` (0.30)
- `stdlib.internal` -> `stdlib.Internal` (0.31)
- `typing.small_arc_vec` -> `typing.SmallArcVec` (0.31)
- `tuple.globals` -> `tuple.Globals` (0.31)
- `__derive_refs.invoke_macro_error` -> `deriverefs.InvokeMacroError [PROVENANCE-FALLBACK]` (0.31)
- `num.globals` -> `num.Globals` (0.32)
- `num.value` -> `num.Value` (0.32)
- `typing.user` -> `typing.User` (0.32)
- `util.refcell` -> `refcell.RefCell` (0.32, 20 deps)
- `float.unpack` -> `float.Unpack` (0.33)
- `dict.traits` -> `dict.Traits` (0.33)
- `heap.maybe_uninit_slice_util` -> `heap.MaybeUninitSliceUtil` (0.34)
- `collections.aligned_padded_str` -> `alignedpaddedstr.AlignedPaddedStr` (0.34, 2 deps)
- `values.demand` -> `values.Demand` (0.37, 4 deps)
- `list.list_type` -> `list.ListType` (0.37, 3 deps)
- `profile.alloc_counts` -> `profile.AllocCounts` (0.40, 4 deps)
- `util.arc_or_static` -> `util.ArcOrStatic` (0.42, 2 deps)
- `bc.definitely_assigned` -> `bc.DefinitelyAssigned` (0.42)
- `string.dot_format` -> `string.DotFormat` (0.43, 1 deps)
- `analysis.underscore` -> `analysis.Underscore` (0.44)
- `namespace.value` -> `namespace.Value` (0.44)
- `analysis.performance` -> `analysis.Performance` (0.45)
- `stdlib.breakpoint` -> `stdlib.Breakpoint` (0.45, 1 deps)
- `enumeration.value` -> `enumeration.Value` (0.46)
- `analysis.names` -> `analysis.Names` (0.48)
- `analysis.dubious` -> `analysis.Dubious` (0.48)
- `avalues.list` -> `avalues.List` (0.48)
- `environment.module_dump` -> `environment.ModuleDump` (0.48)
- `bool.value` -> `bool.Value` (0.49)
- `types.any_complex` -> `types.AnyComplex` (0.49, 1 deps)
- `tuple.alloc` -> `tuple.Alloc` (0.49)
- `runtime.inlined_frame` -> `runtime.InlinedFrame` (0.50)
- `bc.native_function` -> `bc.NativeFunction` (0.51, 4 deps)
- `dict.refs` -> `dict.Refs` (0.51)
- `profile.bc` -> `profile.Bc` (0.52)
- `analysis.flow` -> `analysis.Flow` (0.52)
- `types.ellipsis` -> `types.Ellipsis` (0.55, 4 deps)
- `analysis.find_call_name` -> `analysis.FindCallName` (0.55)
- `dict.unpack` -> `dict.Unpack` (0.55)
- `profile.data` -> `profile.Data` (0.55)
- `typing.callable_param` -> `typing.CallableParam` (0.56)
- `analysis.incompatible` -> `analysis.Incompatible` (0.58)
- `typing.callable` -> `typing.Callable` (0.58)
- `profile.flamegraph` -> `profile.Flamegraph` (0.59)
- `types.unbound` -> `types.Unbound` (0.60)
- `bc.bytecode` -> `bc.Bytecode` (0.60)
- `typing.interface` -> `typing.Interface` (0.60, 5 deps)
- `oracle.traits` -> `oracle.Traits` (0.60)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| `layout.value` | `layout.Value [ZERO]` | 3/9 | `DisplayWithTypeImpl`, `Canonical`, `String` |
| `values.freeze` | `values.Freeze [ZERO]` | 1/2 | `Frozen` |
| `compiler.span` | `compiler.Span` | 1/2 | `Target` |
| `values.frozen_ref` | `values.FrozenRef [ZERO]` | 2/4 | `Target`, `Frozen` |
| `none.none_type` | `none.NoneType [ZERO]` | 1/2 | `Error` |
| `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 4/7 | `Canonical`, `Frozen`, `Error`, `ReprNotSendSync` |
| `environment.methods` | `environment.Methods [ZERO]` | 1/4 | `Magic` |
| `values.iter` | `values.Iter [ZERO] [PROVENANCE-FALLBACK]` | 1/2 | `Item` |
| `int.inline_int` | `int.InlineInt [ZERO]` | 3/5 | `Error`, `Output`, `Canonical` |
| `int.pointer_i32` | `int.PointerI32 [ZERO]` | 1/2 | `Canonical` |
| `any` | `starlark.Any` | 12/15 | `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar` |
| `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 1/2 | `Output` |
| `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 1/2 | `Output` |
| `dict.dict_type` | `dict.DictType [ZERO]` | 2/3 | `Canonical`, `Error` |
| `none.none_or` | `none.NoneOr` | 2/3 | `Canonical`, `Error` |
| `values.freeze_error` | `values.FreezeError [ZERO]` | 1/4 | `FreezeResult` |
| `typing.arc_ty` | `typing.ArcTy` | 1/4 | `Target` |
| `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 4/8 | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` |
| `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 2/4 | `Canonical`, `CompilerArgs` |
| `values.demand` | `values.Demand` | 3/4 | `SomeTrait`, `StaticType`, `MyValue` |
| `values.value_of` | `values.ValueOf [ZERO]` | 3/4 | `Target`, `Canonical`, `Error` |
| `profile.alloc_counts` | `profile.AllocCounts` | 1/2 | `Output` |
| `record.record_type` | `record.RecordType [ZERO]` | 6/8 | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` |
| `alloc.chunk` | `alloc.Chunk [ZERO]` | 1/3 | `ChunkDataEmpty` |
| `list.alloc` | `list.Alloc [ZERO]` | 1/2 | `Canonical` |
| `list.list_type` | `list.ListType` | 2/3 | `Canonical`, `Error` |
| `profile.instant` | `profile.Instant [ZERO]` | 1/2 | `Output` |
| `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 2/3 | `Canonical`, `Error` |
| `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 3/4 | `Target`, `Item`, `IntoIter` |
| `util.arc_or_static` | `util.ArcOrStatic` | 1/3 | `Target` |
| `typing.type_type` | `typing.TypeType` | 2/3 | `Canonical`, `Error` |
| `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 1/3 | `Target` |
| `avalues.str_` | `avalues.Str [ZERO]` | 2/3 | `StarlarkValue`, `ExtraElem` |
| `heap.arena` | `heap.Arena [ZERO]` | 3/7 | `ChunkIter`, `Item`, `ArenaUninit` |
| `collections.alloca` | `collections.Alloca [ZERO]` | 3/4 | `Buffer`, `Align`, `DropSliceGuard` |
| `stdlib` | `starlark.Stdlib [ZERO]` | 2/3 | `Bool2`, `Error` |
| `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `stdlib.breakpoint` | `stdlib.Breakpoint` | 1/6 | `Handler` |
| `types.any_complex` | `types.AnyComplex` | 4/5 | `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData` |
| `types.any_array` | `types.AnyArray [ZERO]` | 2/3 | `Canonical`, `IncrementOnDrop` |
| `util.rtabort` | `util.Rtabort [ZERO]` | 1/1 | `AbortOnDrop` |
| `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 1/1 | `NonStaticAny` |
| `avalues.simple` | `avalues.Simple [ZERO]` | 2/3 | `StarlarkValue`, `ExtraElem` |
| `layout.value_captured` | `layout.ValueCaptured` | 2/4 | `Canonical`, `Frozen` |
| `record.field` | `record.Field [ZERO]` | 1/1 | `FieldGen` |
| `read_line` | `starlark.ReadLine [ZERO]` | 1/2 | `NoRustyline` |
| `bc.instr_impl` | `bc.InstrImpl` | 76/163 | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` |
| `string.str_type` | `string.StrType [ZERO]` | 4/4 | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` |
| `int.int_or_big` | `int.IntOrBig [STUB]` | 4/7 | `Canonical`, `Err`, `Error`, `Output` |
| `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 2/3 | `ThinBoxSliceLayout`, `Target` |
| `set.value` | `set.Value [ZERO]` | 3/9 | `Canonical`, `Frozen`, `ContentRef` |
| `values.typing.callable` | `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 3/8 | `Canonical`, `Error`, `Frozen` |
| `typing.user` | `typing.User` | 3/8 | `AbstractPlant`, `FruitCallable`, `Fruit` |
| `analysis.names` | `analysis.Names` | 1/8 | `AstStrExt` |
| `float.float` | `float.Float [ZERO]` | 2/3 | `Canonical`, `Error` |
| `layout.typed` | `layout.ValueTyped [ZERO]` | 5/7 | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` |
| `scope.payload` | `scope.Payload [ZERO]` | 8/17 | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` |
| `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 1/3 | `Target` |
| `list.value` | `list.Value [ZERO]` | 2/8 | `List`, `Canonical` |
| `dict.value` | `dict.Value [ZERO]` | 3/10 | `Canonical`, `Frozen`, `ContentRef` |
| `num.value` | `num.Value` | 1/4 | `Output` |
| `stdlib.extra` | `stdlib.Extra` | 1/4 | `PrintHandlerImpl` |
| `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 2/4 | `TestSimpleType`, `TestComplexGen` |
| `alloc.chain` | `alloc.Chain [ZERO]` | 2/5 | `Item`, `ResetSplitAtZeroTest` |
| `stdlib.partial` | `stdlib.Partial [ZERO]` | 2/5 | `Frozen`, `Canonical` |
| `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 3/5 | `Target`, `Item`, `IntoIter` |
| `stdlib.json` | `stdlib.Json` | 1/1 | `Canonical` |
| `layout.vtable` | `layout.Vtable` | 2/6 | `GetTypeId`, `GetAllocativeKey` |
| `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 2/7 | `StaticType`, `Canonical` |
| `profile.bc` | `profile.Bc` | 1/10 | `Data` |
| `alloc.allocator` | `alloc.Allocator [ZERO]` | 1/3 | `Item` |
| `typing.small_arc_vec` | `typing.SmallArcVec` | 1/3 | `Target` |
| `tuple.unpack` | `tuple.Unpack [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `layout.complex` | `layout.Complex [ZERO]` | 3/4 | `Canonical`, `Error`, `Frozen` |
| `compiler.scope` | `compiler.Scope [ZERO]` | 3/20 | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` |
| `bc.instrs` | `bc.Instrs [ZERO]` | 1/4 | `HandlerImpl` |
| `analysis.dubious` | `analysis.Dubious` | 1/2 | `Key` |
| `profile.csv` | `profile.Csv` | 2/3 | `Impl`, `CsvValue` |
| `analysis.types` | `analysis.Types` | 3/5 | `LintWarning`, `LintT`, `EvalSeverity` |
| `heap.send` | `heap.Send [ZERO]` | 3/6 | `Sealed`, `Target`, `StaticType` |
| `list.unpack` | `list.Unpack [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `bigint.convert` | `bigint.Convert [ZERO]` | 2/2 | `Canonical`, `Error` |
| `tuple.rust_tuple` | `tuple.RustTuple` | 2/2 | `Canonical`, `Error` |
| `values.owned` | `values.Owned [ZERO]` | 2/5 | `Canonical`, `Target` |
| `profile.time_flame` | `profile.TimeFlame` | 1/11 | `Data` |
| `profile.stmt` | `profile.Stmt [ZERO]` | 1/9 | `Data` |
| `typing.callable_param` | `typing.CallableParam` | 1/6 | `ParamSpecDisplay` |
| `values.unpack` | `values.Unpack [ZERO]` | 4/7 | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` |
| `dict.refs` | `dict.Refs` | 3/7 | `Target`, `Canonical`, `Error` |
| `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 3/3 | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` |
| `typing.iter` | `typing.Iter [ZERO]` | 2/4 | `NonInstantiable`, `Canonical` |
| `bc.addr` | `bc.Addr [ZERO]` | 1/6 | `Output` |
| `avalues.static_` | `avalues.Static [ZERO]` | 3/5 | `StarlarkValue`, `ExtraElem`, `MySimpleValue` |
| `profile.typecheck` | `profile.Typecheck [ZERO]` | 1/5 | `Data` |
| `profile.mode` | `profile.Mode` | 1/2 | `Err` |
| `int.i32` | `int.I32` | 2/2 | `Canonical`, `Error` |
| `structs.alloc` | `structs.Alloc [ZERO]` | 1/2 | `Canonical` |
| `dict.alloc` | `dict.Alloc [ZERO]` | 1/2 | `Canonical` |
| `profile.heap` | `profile.Heap` | 1/11 | `Data` |
| `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 3/7 | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` |
| `list.refs` | `list.Refs [ZERO]` | 3/5 | `Target`, `Canonical`, `Error` |
| `avalues.list` | `avalues.List` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `bc.opcode` | `bc.Opcode [ZERO]` | 2/5 | `ByNumber`, `FindOpcode` |
| `tuple.refs` | `tuple.Refs` | 2/4 | `Canonical`, `Error` |
| `typing.never` | `typing.Never [ZERO]` | 1/3 | `Canonical` |
| `bc.repr` | `bc.Repr [ZERO]` | 1/3 | `HandlerImpl` |
| `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 2/2 | `Canonical`, `Error` |
| `tuple.alloc` | `tuple.Alloc` | 1/2 | `Canonical` |
| `float.unpack` | `float.Unpack` | 2/3 | `Canonical`, `Error` |
| `dict.unpack` | `dict.Unpack` | 2/3 | `Canonical`, `Error` |
| `compiler.expr` | `compiler.Expr [ZERO]` | 2/11 | `AstLiteralCompile`, `CompilerExprUtil` |
| `values.traits` | `values.Traits [ZERO]` | 1/3 | `Canonical` |
| `compiler.def` | `compiler.Def [ZERO]` | 1/13 | `Frozen` |
| `types.function` | `types.Function [ZERO]` | 1/12 | `Canonical` |
| `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 1/8 | `Output` |
| `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 1/3 | `RowKind` |
| `avalues.array` | `avalues.Array [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `compiler.args` | `compiler.Args` | 1/2 | `Never` |
| `avalues.tuple` | `avalues.Tuple [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `avalues.complex` | `avalues.Complex [ZERO]` | 2/5 | `StarlarkValue`, `ExtraElem` |
| `set.refs` | `set.Refs [ZERO]` | 2/5 | `Canonical`, `Error` |
| `bc.instr_arg` | `bc.InstrArg [ZERO]` | 1/4 | `HandlerImpl` |
| `structs.refs` | `structs.Refs [ZERO]` | 2/4 | `Canonical`, `Error` |
| `bc.call` | `bc.Call [ZERO]` | 1/5 | `Args` |
| `typing.callable` | `typing.Callable` | 1/2 | `TyCallableInner` |
| `bc.bytecode` | `bc.Bytecode` | 1/2 | `HandlerImpl` |
| `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 1/6 | `Canonical` |
| `types.any` | `types.Any` | 1/2 | `Canonical` |
| `values.type_repr` | `values.TypeRepr [ZERO]` | 1/3 | `Canonical` |
| `list.globals` | `list.Globals [ZERO]` | 1/1 | `ListType` |
| `dict.traits` | `dict.Traits` | 2/2 | `Canonical`, `Error` |
| `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 1/1 | `Guard` |
| `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 1/1 | `WriteRemOnDrop` |
| `bool.unpack` | `bool.Unpack` | 1/1 | `Error` |
| `bool.type_repr` | `bool.TypeRepr [ZERO]` | 1/1 | `Canonical` |
| `type_compiled.factory` | `type_compiled.Factory` | 1/2 | `Result` |
| `compiler.assign_modify` | `compiler.AssignModify` | 1/1 | `AssignOnWriteBc` |

## High Priority Missing Files

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `tests.uncategorized` | `tests.Uncategorized` | 0 | 55 | 6 | 61 | `src/tests/uncategorized.rs` | `tests/Uncategorized.kt` |
| 2 | `tests.markdown` | `docs.tests.Markdown` | 0 | 29 | 2 | 31 | `src/docs/tests/markdown.rs` | `docs/tests/Markdown.kt` |
| 3 | `opt.if_rand` | `tests.opt.IfRand` | 0 | 27 | 3 | 30 | `src/tests/opt/if_rand.rs` | `tests/opt/IfRand.kt` |
| 4 | `typing.tests` | `typing.tests.Tests` | 0 | 28 | 2 | 30 | `src/typing/tests.rs` | `typing/tests/Tests.kt` |
| 5 | `adapter.tests` | `debug.adapter.Tests` | 0 | 23 | 3 | 26 | `src/debug/adapter/tests.rs` | `debug/adapter/Tests.kt` |
| 6 | `tests.call` | `tests.Call` | 0 | 20 | 0 | 20 | `src/tests/call.rs` | `tests/Call.kt` |
| 7 | `tests.fstring` | `tests.Fstring` | 0 | 18 | 0 | 18 | `src/tests/fstring.rs` | `tests/Fstring.kt` |
| 8 | `tests.rustdocs` | `docs.tests.Rustdocs` | 0 | 15 | 3 | 18 | `src/docs/tests/rustdocs.rs` | `docs/tests/Rustdocs.kt` |
| 9 | `scope.tests` | `eval.compiler.scope.Tests` | 0 | 16 | 1 | 17 | `src/eval/compiler/scope/tests.rs` | `eval/compiler/scope/Tests.kt` |
| 10 | `tests.runtime` | `tests.Runtime` | 0 | 14 | 1 | 15 | `src/tests/runtime.rs` | `tests/Runtime.kt` |
| 11 | `coerce` | `Coerce` | 34 | 5 | 9 | 14 | `src/coerce.rs` | `Coerce.kt` |
| 12 | `module.unpack_value` | `tests.derive.module.UnpackValue` | 0 | 13 | 0 | 13 | `src/tests/derive/module/unpack_value.rs` | `tests/derive/module/UnpackValue.kt` |
| 13 | `profile.tests` | `eval.runtime.profile.Tests` | 0 | 13 | 0 | 13 | `src/eval/runtime/profile/tests.rs` | `eval/runtime/profile/Tests.kt` |
| 14 | `type_compiled.tests` | `values.typing.typecompiled.Tests` | 0 | 13 | 0 | 13 | `src/values/typing/type_compiled/tests.rs` | `values/typing/typecompiled/Tests.kt` |
| 15 | `bc.if_stmt` | `tests.bc.IfStmt` | 0 | 12 | 0 | 12 | `src/tests/bc/if_stmt.rs` | `tests/bc/IfStmt.kt` |
| 16 | `tests.basic` | `tests.Basic` | 0 | 12 | 0 | 12 | `src/tests/basic.rs` | `tests/Basic.kt` |
| 17 | `module.named_positional` | `tests.derive.module.NamedPositional` | 0 | 11 | 0 | 11 | `src/tests/derive/module/named_positional.rs` | `tests/derive/module/NamedPositional.kt` |
| 18 | `module.generic` | `tests.derive.module.Generic` | 0 | 8 | 2 | 10 | `src/tests/derive/module/generic.rs` | `tests/derive/module/Generic.kt` |
| 19 | `tests.comprehension` | `tests.Comprehension` | 0 | 10 | 0 | 10 | `src/tests/comprehension.rs` | `tests/Comprehension.kt` |
| 20 | `derive.docs` | `tests.derive.Docs` | 0 | 7 | 2 | 9 | `src/tests/derive/docs.rs` | `tests/derive/Docs.kt` |
| 21 | `module.basic` | `tests.derive.module.Basic` | 0 | 9 | 0 | 9 | `src/tests/derive/module/basic.rs` | `tests/derive/module/Basic.kt` |
| 22 | `tests.type_annot` | `tests.TypeAnnot` | 0 | 9 | 0 | 9 | `src/tests/type_annot.rs` | `tests/TypeAnnot.kt` |
| 23 | `typing.tests.call` | `typing.tests.Call` | 0 | 9 | 0 | 9 | `src/typing/tests/call.rs` | `typing/tests/Call.kt` |
| 24 | `bc.and_or` | `tests.bc.AndOr` | 0 | 8 | 0 | 8 | `src/tests/bc/and_or.rs` | `tests/bc/AndOr.kt` |
| 25 | `opt.def_inline` | `tests.opt.DefInline` | 0 | 8 | 0 | 8 | `src/tests/opt/def_inline.rs` | `tests/opt/DefInline.kt` |
| 26 | `tests.opt` | `tests.opt.Opt` | 0 | 8 | 0 | 8 | `src/tests/opt.rs` | `tests/opt/Opt.kt` |
| 27 | `derive.unpack_value` | `tests.derive.UnpackValue` | 51 | 2 | 5 | 7 | `src/tests/derive/unpack_value.rs` | `tests/derive/UnpackValue.kt` |
| 28 | `util.arc_str` | `util.ArcStr` | 21 | 5 | 2 | 7 | `src/util/arc_str.rs` | `util/ArcStr.kt` |
| 29 | `bc.expr` | `tests.bc.Expr` | 7 | 7 | 0 | 7 | `src/tests/bc/expr.rs` | `tests/bc/Expr.kt` |
| 30 | `opt.eq` | `tests.opt.Eq` | 0 | 7 | 0 | 7 | `src/tests/opt/eq.rs` | `tests/opt/Eq.kt` |
| 31 | `unused_loads.find_tests` | `analysis.unusedloads.FindTests` | 0 | 7 | 0 | 7 | `src/analysis/unused_loads/find_tests.rs` | `analysis/unusedloads/FindTests.kt` |
| 32 | `freeze.bounds` | `tests.derive.freeze.Bounds` | 0 | 3 | 3 | 6 | `src/tests/derive/freeze/bounds.rs` | `tests/derive/freeze/Bounds.kt` |
| 33 | `freeze.identity` | `tests.derive.freeze.Identity` | 0 | 2 | 4 | 6 | `src/tests/derive/freeze/identity.rs` | `tests/derive/freeze/Identity.kt` |
| 34 | `freeze.validator_order` | `tests.derive.freeze.ValidatorOrder` | 0 | 3 | 3 | 6 | `src/tests/derive/freeze/validator_order.rs` | `tests/derive/freeze/ValidatorOrder.kt` |
| 35 | `int.tests` | `values.types.int.Tests` | 0 | 6 | 0 | 6 | `src/values/types/int/tests.rs` | `values/types/int/Tests.kt` |
| 36 | `module.methods` | `tests.derive.module.Methods` | 0 | 5 | 1 | 6 | `src/tests/derive/module/methods.rs` | `tests/derive/module/Methods.kt` |
| 37 | `module.other_attributes` | `tests.derive.module.OtherAttributes` | 0 | 6 | 0 | 6 | `src/tests/derive/module/other_attributes.rs` | `tests/derive/module/OtherAttributes.kt` |
| 38 | `tests.list` | `typing.tests.List` | 0 | 6 | 0 | 6 | `src/typing/tests/list.rs` | `typing/tests/List.kt` |
| 39 | `tests.bc.definitely_assigned` | `tests.bc.DefinitelyAssigned` | 0 | 5 | 0 | 5 | `src/tests/bc/definitely_assigned.rs` | `tests/bc/DefinitelyAssigned.kt` |
| 40 | `bc.compr` | `tests.bc.Compr` | 0 | 4 | 0 | 4 | `src/tests/bc/compr.rs` | `tests/bc/Compr.kt` |
| 41 | `derive.alloc_value` | `tests.derive.AllocValue` | 0 | 0 | 4 | 4 | `src/tests/derive/alloc_value.rs` | `tests/derive/AllocValue.kt` |
| 42 | `freeze.validator` | `tests.derive.freeze.Validator` | 0 | 3 | 1 | 4 | `src/tests/derive/freeze/validator.rs` | `tests/derive/freeze/Validator.kt` |
| 43 | `module.kwargs` | `tests.derive.module.Kwargs` | 0 | 4 | 0 | 4 | `src/tests/derive/module/kwargs.rs` | `tests/derive/module/Kwargs.kt` |
| 44 | `module.return_impl` | `tests.derive.module.ReturnImpl` | 0 | 4 | 0 | 4 | `src/tests/derive/module/return_impl.rs` | `tests/derive/module/ReturnImpl.kt` |
| 45 | `module.type_annotation` | `tests.derive.module.TypeAnnotation` | 0 | 3 | 1 | 4 | `src/tests/derive/module/type_annotation.rs` | `tests/derive/module/TypeAnnotation.kt` |
| 46 | `opt.type_is` | `tests.opt.TypeIs` | 0 | 4 | 0 | 4 | `src/tests/opt/type_is.rs` | `tests/opt/TypeIs.kt` |
| 47 | `tests.freeze_access_value` | `tests.FreezeAccessValue` | 0 | 2 | 2 | 4 | `src/tests/freeze_access_value.rs` | `tests/FreezeAccessValue.kt` |
| 48 | `tests.util` | `tests.Util` | 0 | 3 | 1 | 4 | `src/tests/util.rs` | `tests/Util.kt` |
| 49 | `trace.bounds` | `tests.derive.trace.Bounds` | 0 | 2 | 2 | 4 | `src/tests/derive/trace/bounds.rs` | `tests/derive/trace/Bounds.kt` |
| 50 | `unused_loads.remove_tests` | `analysis.unusedloads.RemoveTests` | 0 | 4 | 0 | 4 | `src/analysis/unused_loads/remove_tests.rs` | `analysis/unusedloads/RemoveTests.kt` |
| 51 | `bc.for_stmt` | `tests.bc.ForStmt` | 0 | 3 | 0 | 3 | `src/tests/bc/for_stmt.rs` | `tests/bc/ForStmt.kt` |
| 52 | `derive.attrs` | `tests.derive.Attrs` | 0 | 1 | 2 | 3 | `src/tests/derive/attrs.rs` | `tests/derive/Attrs.kt` |
| 53 | `module.default_value` | `tests.derive.module.DefaultValue` | 0 | 3 | 0 | 3 | `src/tests/derive/module/default_value.rs` | `tests/derive/module/DefaultValue.kt` |
| 54 | `module.special_params` | `tests.derive.module.SpecialParams` | 0 | 3 | 0 | 3 | `src/tests/derive/module/special_params.rs` | `tests/derive/module/SpecialParams.kt` |
| 55 | `opt.speculative_exec` | `tests.opt.SpeculativeExec` | 0 | 3 | 0 | 3 | `src/tests/opt/speculative_exec.rs` | `tests/opt/SpeculativeExec.kt` |
| 56 | `tests.go` | `tests.Go` | 0 | 3 | 0 | 3 | `src/tests/go.rs` | `tests/Go.kt` |
| 57 | `tests.types` | `typing.tests.Types` | 0 | 3 | 0 | 3 | `src/typing/tests/types.rs` | `typing/tests/Types.kt` |
| 58 | `bc.golden` | `tests.bc.Golden` | 0 | 2 | 0 | 2 | `src/tests/bc/golden.rs` | `tests/bc/Golden.kt` |
| 59 | `derive.unpack_value_attr` | `tests.derive.UnpackValueAttr` | 0 | 0 | 2 | 2 | `src/tests/derive/unpack_value_attr.rs` | `tests/derive/UnpackValueAttr.kt` |
| 60 | `opt.constant_folding` | `tests.opt.ConstantFolding` | 0 | 2 | 0 | 2 | `src/tests/opt/constant_folding.rs` | `tests/opt/ConstantFolding.kt` |
| 61 | `opt.list_add` | `tests.opt.ListAdd` | 0 | 2 | 0 | 2 | `src/tests/opt/list_add.rs` | `tests/opt/ListAdd.kt` |
| 62 | `opt.types` | `tests.opt.Types` | 0 | 2 | 0 | 2 | `src/tests/opt/types.rs` | `tests/opt/Types.kt` |
| 63 | `tests.callable` | `typing.tests.Callable` | 0 | 2 | 0 | 2 | `src/typing/tests/callable.rs` | `typing/tests/Callable.kt` |
| 64 | `tests.special_function` | `typing.tests.SpecialFunction` | 0 | 2 | 0 | 2 | `src/typing/tests/special_function.rs` | `typing/tests/SpecialFunction.kt` |
| 65 | `tests.tuple` | `typing.tests.Tuple` | 0 | 2 | 0 | 2 | `src/typing/tests/tuple.rs` | `typing/tests/Tuple.kt` |
| 66 | `trace.statics` | `tests.derive.trace.Statics` | 0 | 0 | 2 | 2 | `src/tests/derive/trace/statics.rs` | `tests/derive/trace/Statics.kt` |
| 67 | `tests.before_stmt` | `tests.BeforeStmt` | 1 | 1 | 0 | 1 | `src/tests/before_stmt.rs` | `tests/BeforeStmt.kt` |
| 68 | `bc.isinstance` | `tests.bc.Isinstance` | 0 | 1 | 0 | 1 | `src/tests/bc/isinstance.rs` | `tests/bc/Isinstance.kt` |
| 69 | `freeze.basic` | `tests.derive.freeze.Basic` | 0 | 0 | 1 | 1 | `src/tests/derive/freeze/basic.rs` | `tests/derive/freeze/Basic.kt` |
| 70 | `freeze.enums` | `tests.derive.freeze.Enums` | 0 | 0 | 1 | 1 | `src/tests/derive/freeze/enums.rs` | `tests/derive/freeze/Enums.kt` |
| 71 | `tests.bc.call` | `tests.bc.Call` | 0 | 1 | 0 | 1 | `src/tests/bc/call.rs` | `tests/bc/Call.kt` |
| 72 | `tests.for_loop` | `tests.ForLoop` | 0 | 1 | 0 | 1 | `src/tests/for_loop.rs` | `tests/ForLoop.kt` |
| 73 | `tests.replace_binary` | `tests.ReplaceBinary` | 0 | 1 | 0 | 1 | `src/tests/replace_binary.rs` | `tests/ReplaceBinary.kt` |
| 74 | `trace.enums` | `tests.derive.trace.Enums` | 0 | 0 | 1 | 1 | `src/tests/derive/trace/enums.rs` | `tests/derive/trace/Enums.kt` |
| 75 | `layout.heap` | `values.layout.heap.Heap` | 109 | 0 | 0 | 0 | `src/values/layout/heap.rs` | `values/layout/heap/Heap.kt` |
| 76 | `assert` | `assert.Assert` | 84 | 0 | 0 | 0 | `src/assert.rs` | `assert/Assert.kt` |
| 77 | `debug` | `debug.Debug` | 53 | 0 | 0 | 0 | `src/debug.rs` | `debug/Debug.kt` |
| 78 | `types.dict` | `values.types.dict.Dict` | 12 | 0 | 0 | 0 | `src/values/types/dict.rs` | `values/types/dict/Dict.kt` |
| 79 | `types.range` | `values.types.range.Range` | 5 | 0 | 0 | 0 | `src/values/types/range.rs` | `values/types/range/Range.kt` |
| 80 | `types.namespace` | `values.types.namespace.Namespace` | 3 | 0 | 0 | 0 | `src/values/types/namespace.rs` | `values/types/namespace/Namespace.kt` |
| 81 | `types.float` | `values.types.float.Float` | 2 | 0 | 0 | 0 | `src/values/types/float.rs` | `values/types/float/Float.kt` |
| 82 | `types.list` | `values.types.list.List` | 2 | 0 | 0 | 0 | `src/values/types/list.rs` | `values/types/list/List.kt` |
| 83 | `types.num` | `values.types.num.Num` | 2 | 0 | 0 | 0 | `src/values/types/num.rs` | `values/types/num/Num.kt` |
| 84 | `tests` | `tests.Tests` | 1 | 0 | 0 | 0 | `src/tests.rs` | `tests/Tests.kt` |
| 85 | `types.enumeration` | `values.types.enumeration.Enumeration` | 1 | 0 | 0 | 0 | `src/values/types/enumeration.rs` | `values/types/enumeration/Enumeration.kt` |
| 86 | `types.none` | `values.types.none.None` | 1 | 0 | 0 | 0 | `src/values/types/none.rs` | `values/types/none/None.kt` |
| 87 | `types.set` | `values.types.set.Set` | 1 | 0 | 0 | 0 | `src/values/types/set.rs` | `values/types/set/Set.kt` |
| 88 | `types.string` | `values.types.string.String` | 1 | 0 | 0 | 0 | `src/values/types/string.rs` | `values/types/string/String.kt` |
| 89 | `allocator.alloc` | `values.layout.heap.allocator.alloc.Alloc` | 0 | 0 | 0 | 0 | `src/values/layout/heap/allocator/alloc.rs` | `values/layout/heap/allocator/alloc/Alloc.kt` |
| 90 | `analysis.unused_loads` | `analysis.unusedloads.UnusedLoads` | 0 | 0 | 0 | 0 | `src/analysis/unused_loads.rs` | `analysis/unusedloads/UnusedLoads.kt` |
| 91 | `bc.compiler` | `eval.bc.compiler.Compiler` | 0 | 0 | 0 | 0 | `src/eval/bc/compiler.rs` | `eval/bc/compiler/Compiler.kt` |
| 92 | `collections` | `collections.Collections` | 0 | 0 | 0 | 0 | `src/collections.rs` | `collections/Collections.kt` |
| 93 | `derive.freeze` | `tests.derive.freeze.Freeze` | 0 | 0 | 0 | 0 | `src/tests/derive/freeze.rs` | `tests/derive/freeze/Freeze.kt` |
| 94 | `derive.trace` | `tests.derive.trace.Trace` | 0 | 0 | 0 | 0 | `src/tests/derive/trace.rs` | `tests/derive/trace/Trace.kt` |
| 95 | `docs.tests` | `docs.tests.Tests` | 0 | 0 | 0 | 0 | `src/docs/tests.rs` | `docs/tests/Tests.kt` |
| 96 | `heap.allocator` | `values.layout.heap.allocator.Allocator` | 0 | 0 | 0 | 0 | `src/values/layout/heap/allocator.rs` | `values/layout/heap/allocator/Allocator.kt` |
| 97 | `heap.branding` | `values.layout.heap.Branding` | 0 | 0 | 0 | 0 | `src/values/layout/heap/branding.rs` | `values/layout/heap/Branding.kt` |
| 98 | `heap.profile` | `values.layout.heap.profile.Profile` | 0 | 0 | 0 | 0 | `src/values/layout/heap/profile.rs` | `values/layout/heap/profile/Profile.kt` |
| 99 | `lib` | `Lib` | 0 | 0 | 0 | 0 | `src/lib.rs` | `Lib.kt` |
| 100 | `runtime.params` | `eval.runtime.params.Params` | 0 | 0 | 0 | 0 | `src/eval/runtime/params.rs` | `eval/runtime/params/Params.kt` |
| 101 | `runtime.profile` | `eval.runtime.profile.Profile` | 0 | 0 | 0 | 0 | `src/eval/runtime/profile.rs` | `eval/runtime/profile/Profile.kt` |
| 102 | `string.intern` | `values.types.string.intern.Intern` | 0 | 0 | 0 | 0 | `src/values/types/string/intern.rs` | `values/types/string/intern/Intern.kt` |
| 103 | `tests.bc` | `tests.bc.Bc` | 0 | 0 | 0 | 0 | `src/tests/bc.rs` | `tests/bc/Bc.kt` |
| 104 | `tests.derive` | `tests.derive.Derive` | 0 | 0 | 0 | 0 | `src/tests/derive.rs` | `tests/derive/Derive.kt` |
| 105 | `types.structs` | `values.types.structs.Structs` | 0 | 0 | 0 | 0 | `src/values/types/structs.rs` | `values/types/structs/Structs.kt` |
| 106 | `types.tuple` | `values.types.tuple.Tuple` | 0 | 0 | 0 | 0 | `src/values/types/tuple.rs` | `values/types/tuple/Tuple.kt` |

## Documentation Gaps

There is missing documentation that is hurting overall scoring.

**Documentation coverage:** 9668 / 12684 lines (76%)

Documentation gaps (>20%), complete list:

- `values.traits` - 79% gap (1072 → 222 lines)
- `string.methods` - 43% gap (1112 → 637 lines)
- `dict.methods` - 59% gap (396 → 162 lines)
- `values.unpack` - 80% gap (176 → 35 lines)
- `funcs.other` - 37% gap (364 → 228 lines)
- `values.owned` - 73% gap (180 → 48 lines)
- `docs.parse` - 63% gap (174 → 64 lines)
- `macros` - 87% gap (116 → 15 lines)
- `set.methods` - 39% gap (258 → 158 lines)
- `types.any` - 78% gap (124 → 27 lines)
- `heap.send` - 49% gap (184 → 94 lines)
- `any` - 71% gap (120 → 35 lines)
- `params.spec` - 49% gap (172 → 87 lines)
- `list.methods` - 27% gap (306 → 224 lines)
- `types.starlark_value_as_type` - 95% gap (74 → 4 lines)
- `runtime.evaluator` - 37% gap (186 → 118 lines)
- `assert.assert` - 32% gap (204 → 138 lines)
- `values.alloc_value` - 44% gap (138 → 77 lines)
- `string.globals` - 42% gap (130 → 76 lines)
- `heap.repr` - 71% gap (68 → 20 lines)
- `heap.heap_type` - 28% gap (170 → 123 lines)
- `dict.alloc` - 100% gap (42 → 0 lines)
- `docs` - 40% gap (98 → 59 lines)
- `thin_box_slice_frozen_value.thin_box` - 70% gap (54 → 16 lines)
- `heap.fast_cell` - 100% gap (38 → 0 lines)
- `bc.writer` - 40% gap (94 → 56 lines)
- `debug.adapter` - 21% gap (182 → 144 lines)
- `dict.globals` - 58% gap (60 → 25 lines)
- `types.array` - 55% gap (62 → 28 lines)
- `int.globals` - 34% gap (96 → 63 lines)
- `typed.string` - 55% gap (60 → 27 lines)
- `allocator.api` - 100% gap (32 → 0 lines)
- `heap.arena` - 64% gap (50 → 18 lines)
- `scope.payload` - 89% gap (36 → 4 lines)
- `values.owned_frozen_ref` - 62% gap (52 → 20 lines)
- `dict.value` - 53% gap (60 → 28 lines)
- `values.type_repr` - 42% gap (74 → 43 lines)
- `pagable.vtable_register` - 52% gap (60 → 29 lines)
- `range.globals` - 45% gap (64 → 35 lines)
- `analysis.types` - 54% gap (52 → 24 lines)
- `values` - 100% gap (28 → 0 lines)
- `tuple.refs` - 100% gap (26 → 0 lines)
- `pagable` - 100% gap (26 → 0 lines)
- `environment.methods` - 25% gap (102 → 76 lines)
- `record.globals` - 33% gap (76 → 51 lines)
- `bool.globals` - 50% gap (50 → 25 lines)
- `types.function` - 48% gap (50 → 26 lines)
- `bc.definitely_assigned` - 43% gap (54 → 31 lines)
- `eval.bc.compiler.expr` - 77% gap (30 → 7 lines)
- `stdlib.extra` - 46% gap (50 → 27 lines)
- `runtime.file_loader` - 100% gap (22 → 0 lines)
- `profile.mode` - 52% gap (40 → 19 lines)
- `values.typing.callable` - 43% gap (46 → 26 lines)
- `collections.string_pool` - 100% gap (20 → 0 lines)
- `typing.basic` - 42% gap (48 → 28 lines)
- `stdlib` - 26% gap (72 → 53 lines)
- `compiler.if_compiler` - 41% gap (44 → 26 lines)
- `funcs.zip` - 50% gap (36 → 18 lines)
- `layout.pointer` - 53% gap (34 → 16 lines)
- `type_compiled.matcher` - 47% gap (38 → 20 lines)
- `type_compiled.alloc` - 38% gap (48 → 30 lines)
- `types.any_complex` - 82% gap (22 → 4 lines)
- `bc.if_debug` - 45% gap (38 → 21 lines)
- `tuple.alloc` - 57% gap (30 → 13 lines)
- `avalues.simple` - 61% gap (28 → 11 lines)
- `dict.refs` - 94% gap (18 → 1 lines)
- `values.freeze` - 30% gap (56 → 39 lines)
- `profile.aggregated` - 30% gap (56 → 39 lines)
- `types.record` - 32% gap (50 → 34 lines)
- `environment.names` - 36% gap (44 → 28 lines)
- `bc.for_loop` - 100% gap (16 → 0 lines)
- `compiler.def_inline` - 62% gap (26 → 10 lines)
- `avalues.str_` - 68% gap (22 → 7 lines)
- `type_compiled.globals` - 44% gap (34 → 19 lines)
- `runtime.slots` - 35% gap (40 → 26 lines)
- `float.globals` - 26% gap (54 → 40 lines)
- `typing.user` - 29% gap (48 → 34 lines)
- `type_compiled.compiled` - 39% gap (36 → 22 lines)
- `profile.summary_by_function` - 43% gap (30 → 17 lines)
- `oracle.traits` - 34% gap (38 → 25 lines)
- `typing.fill_types_for_lint` - 34% gap (38 → 25 lines)
- `docs.multipage` - 26% gap (46 → 34 lines)
- `values.iter` - 100% gap (12 → 0 lines)
- `types.bool` - 100% gap (12 → 0 lines)
- `profile.time_flame` - 43% gap (28 → 16 lines)
- `typing.bindings` - 40% gap (30 → 18 lines)
- `docs.markdown` - 33% gap (36 → 24 lines)
- `params.display` - 50% gap (22 → 11 lines)
- `runtime.cheap_call_stack` - 42% gap (26 → 15 lines)
- `bc.instr_impl` - 34% gap (32 → 21 lines)
- `enumeration.globals` - 27% gap (40 → 29 lines)
- `callable.param` - 45% gap (22 → 12 lines)
- `tuple.globals` - 42% gap (24 → 14 lines)
- `num.globals` - 45% gap (22 → 12 lines)
- `types.int` - 100% gap (10 → 0 lines)
- `bc.opcode` - 50% gap (20 → 10 lines)
- `values.frozen_ref` - 50% gap (20 → 10 lines)
- `values.freeze_error` - 33% gap (30 → 20 lines)
- `compiler.stmt` - 26% gap (34 → 25 lines)
- `stdlib.call_stack` - 25% gap (36 → 27 lines)
- `tuple.value` - 50% gap (18 → 9 lines)
- `thin_box_slice_frozen_value.packed_impl` - 38% gap (24 → 15 lines)
- `__derive_refs.param_spec` - 80% gap (10 → 2 lines)
- `heap.call_enter_exit` - 100% gap (8 → 0 lines)
- `string.interpolation` - 25% gap (32 → 24 lines)
- `dict.dict_type` - 100% gap (8 → 0 lines)
- `eval.soft_error` - 100% gap (8 → 0 lines)
- `string.str_type` - 31% gap (26 → 18 lines)
- `values.stack_guard` - 33% gap (24 → 16 lines)
- `runtime.inlined_frame` - 27% gap (26 → 19 lines)
- `typing.error` - 58% gap (12 → 5 lines)
- `alloc.chunk` - 58% gap (12 → 5 lines)
- `values.value_of` - 44% gap (16 → 9 lines)
- `__derive_refs.parse_args` - 38% gap (16 → 10 lines)
- `layout.freezer` - 38% gap (16 → 10 lines)
- `alloc.chunk_part` - 50% gap (12 → 6 lines)
- `compiler.compr` - 38% gap (16 → 10 lines)
- `none.none_type` - 100% gap (6 → 0 lines)
- `num.typecheck` - 50% gap (12 → 6 lines)
- `assert.conformance` - 30% gap (20 → 14 lines)
- `typing.function` - 50% gap (12 → 6 lines)
- `compiler.call` - 38% gap (16 → 10 lines)
- `oracle.ctx` - 30% gap (20 → 14 lines)
- `types.known_methods` - 38% gap (16 → 10 lines)
- `pagable.error` - 75% gap (8 → 2 lines)
- `profile.data` - 50% gap (12 → 6 lines)
- `record.record_type` - 75% gap (8 → 2 lines)
- `values.recursive_repr_or_json_guard` - 43% gap (14 → 8 lines)
- `heap.maybe_uninit_slice_util` - 43% gap (14 → 8 lines)
- `values.index` - 27% gap (22 → 16 lines)
- `bc.slow_arg` - 36% gap (14 → 9 lines)
- `profile.flamegraph` - 36% gap (14 → 9 lines)
- `profile.csv` - 50% gap (10 → 5 lines)
- `compiler.known` - 42% gap (12 → 7 lines)
- `types.unbound` - 50% gap (10 → 5 lines)
- `params.parser` - 36% gap (14 → 9 lines)
- `range.range_type` - 50% gap (8 → 4 lines)
- `typing.interface` - 50% gap (8 → 4 lines)
- `profile.by_type` - 25% gap (16 → 12 lines)
- `values.error` - 50% gap (8 → 4 lines)
- `enumeration.ty_enum_type` - 50% gap (8 → 4 lines)
- `cast` - 50% gap (8 → 4 lines)
- `bool.value` - 67% gap (6 → 2 lines)
- `debug.evaluate` - 33% gap (12 → 8 lines)
- `types.list_or_tuple` - 50% gap (6 → 3 lines)
- `unused_loads.find` - 25% gap (12 → 9 lines)
- `typing.structs` - 30% gap (10 → 7 lines)
- `compiler.expr_bool` - 38% gap (8 → 5 lines)
- `collections.aligned_padded_str` - 38% gap (8 → 5 lines)
- `avalues.list` - 30% gap (10 → 7 lines)
- `layout.value_captured` - 25% gap (8 → 6 lines)
- `typing.typecheck` - 33% gap (6 → 4 lines)
- `types.any_array` - 33% gap (6 → 4 lines)

