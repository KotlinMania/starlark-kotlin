# Code Port - Progress Report

**Generated:** 2026-06-02
**Source:** tmp/starlark
**Target:** src/commonMain

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 2943/4585 matched (target 4822) | 64.2% |
| Class/type parity | 792/1209 matched (target 1470) | 65.5% |
| Combined symbol parity | 3735/5794 matched (target 6292) | 64.5% |
| Average function body similarity | 0.13 | inline-code cosine |
| Average documentation similarity | 0.67 | doc text cosine |
| Missing source functions | 527 | 0% parity until ported |
| Missing source classes/types | 62 | 0% parity until ported |
| Missing source symbol files | 72 | 589 symbols |
| Cheat/scoring failures | 309 | forced to 0% |
| Total source files | 470 | 100% |
| Target units (paired) | 448 | - |
| Target files (total) | 448 | - |
| Porting progress | 390 | 83.0% (matched) |
| Missing files | 80 | 17.0% |

## Port Quality Analysis

**Average Function Similarity:** 0.13

Similarity in this report is the required function-by-function body/parameter score. Class/type parity and symbol deficits are reported beside it; whole-file shape is diagnostic only.

**Work Distribution:**
- Critical (<0.60): 352 files (90.3% of matched)
- Needs review (0.60-0.84): 25 files (6.4% of matched)

## Worst Function Scores First

Every matched file is listed from lowest function body/parameter similarity upward. Missing symbol names are not capped.

| Rank | Source | Target | Function similarity | Functions | Missing functions | Types | Missing types | Tests | Symbol deficit | Priority |
|------|--------|--------|---------------------|-----------|-------------------|-------|---------------|-------|----------------|----------|
| 1 | `layout.value` | `layout.Value [ZERO]` | 0.00 | 106/118 matched (target 154) | `fmt`, `eq`, `testing_new_int`, `_test_send_sync`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function` | 6/9 matched | `DisplayWithTypeImpl`, `Canonical`, `String` | 0/9 | 15 | 178162720.0 |
| 2 | `typing.ty` | `typing.Ty [ZERO]` | 0.00 | 49/50 matched (target 57) | `fmt` | 4/4 matched (target 6) | _none_ | - | 1 | 109015408.0 |
| 3 | `layout.heap` | `heap.Heap [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 109000008.0 |
| 4 | `typing.starlark_value` | `typing.StarlarkValue [ZERO]` | 0.00 | 29/34 matched (target 41) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp` | 4/4 matched (target 5) | _none_ | - | 5 | 76053808.0 |
| 5 | `runtime.evaluator` | `runtime.Evaluator [ZERO]` | 0.00 | 59/60 matched (target 63) | `drop` | 7/7 matched (target 17) | _none_ | - | 1 | 56016712.0 |
| 6 | `values.trace` | `values.Trace [ZERO]` | 0.00 | 1/1 matched (target 43) | _none_ | 1/1 matched | _none_ | - | 0 | 52000208.0 |
| 7 | `values.freeze` | `values.Freeze [ZERO]` | 0.00 | 1/1 matched (target 33) | _none_ | 1/2 matched (target 6) | `Frozen` | - | 1 | 42010312.0 |
| 8 | `values.alloc_value` | `values.AllocValue [ZERO]` | 0.00 | 2/2 matched (target 5) | _none_ | 4/4 matched | _none_ | - | 0 | 42000608.0 |
| 9 | `layout.freezer` | `layout.Freezer [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched | _none_ | - | 0 | 36000608.0 |
| 10 | `coerce` | `starlark.Coerce [ZERO]` | 0.00 | 1/5 matched (target 1) | `test_ptr_coerce`, `f`, `test_coerce_type_and_lifetime_params`, `test_coerce_is_unsound` | 2/9 matched (target 14) | `Aaa`, `Bbb`, `StructWithLifetimeAndTypeParams`, `Newtype`, `Struct`, `Trait`, `Assoc` | 0/4 | 11 | 34111408.0 |
| 11 | `values.frozen_ref` | `values.FrozenRef [ZERO]` | 0.00 | 17/17 matched (target 23) | _none_ | 2/4 matched (target 2) | `Target`, `Frozen` | - | 2 | 27022110.0 |
| 12 | `none.none_type` | `none.NoneType [ZERO]` | 0.00 | 11/11 matched (target 15) | _none_ | 1/2 matched (target 1) | `Error` | - | 1 | 27011310.0 |
| 13 | `runtime.arguments` | `runtime.Arguments [ZERO]` | 0.00 | 26/30 matched (target 48) | `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names` | 8/8 matched (target 15) | _none_ | 0/4 | 4 | 25043810.0 |
| 14 | `typing.type_compiled` | `type_compiled.TypeCompiled [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 22000010.0 |
| 15 | `environment.globals` | `environment.Globals [ZERO]` | 0.00 | 30/35 matched (target 36) | `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden` | 5/5 matched | _none_ | 0/5 | 5 | 21054010.0 |
| 16 | `derive.module` | `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched (target 14) | _none_ | 0/0 matched (target 3) | _none_ | - | 0 | 21000010.0 |
| 17 | `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 0.00 | 12/18 matched (target 17) | `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant` | 3/7 matched (target 4) | `Canonical`, `Frozen`, `Error`, `ReprNotSendSync` | 0/5 | 10 | 20102510.0 |
| 18 | `__derive_refs.param_spec` | `deriverefs.ParamSpec [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 5/5 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 20000810.0 |
| 19 | `environment.methods` | `environment.Methods [ZERO]` | 0.00 | 17/19 matched (target 20) | `test_set_attribute`, `get_methods` | 3/4 matched (target 3) | `Magic` | 0/2 | 3 | 17032310.0 |
| 20 | `values.iter` | `values.Iter [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 4/5 matched (target 84) | `drop` | 1/2 matched (target 14) | `Item` | - | 2 | 17020710.0 |
| 21 | `collections.symbol` | `collections.Symbol [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 15000010.0 |
| 22 | `private` | `starlark.Private [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 15000110.0 |
| 23 | `layout.avalue` | `layout.AValue [ZERO]` | 0.00 | 6/8 matched (target 10) | `tuple_cycle_freeze`, `test_try_freeze_directly` | 3/3 matched | _none_ | 0/2 | 2 | 14021110.0 |
| 24 | `layout.const_frozen_string` | `layout.ConstFrozenString [ZERO]` | 0.00 | 0/2 matched (target 1) | `test_const_frozen_string_for_short_strings`, `test_const_frozen_string` | 0/0 matched | _none_ | 0/2 | 2 | 12020210.0 |
| 25 | `typing.tuple` | `typing.Tuple [ZERO]` | 0.00 | 5/6 matched (target 9) | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 12010710.0 |
| 26 | `layout.value_lifetimeless` | `layout.ValueLifetimeless [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 12000110.0 |
| 27 | `types.dict` | `types.Dict [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 12000010.0 |
| 28 | `int.inline_int` | `int.InlineInt [ZERO]` | 0.00 | 25/34 matched (target 43) | `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits` | 2/5 matched (target 6) | `Error`, `Output`, `Canonical` | 0/2 | 12 | 11123910.0 |
| 29 | `int.pointer_i32` | `int.PointerI32 [ZERO]` | 0.00 | 28/31 matched (target 34) | `eq`, `fmt`, `serialize` | 1/2 matched | `Canonical` | - | 4 | 9043310.0 |
| 30 | `any` | `starlark.Any [ZERO]` | 0.00 | 2/12 matched (target 3) | `static_type_id`, `static_type_of`, `is`, `test_can_convert`, `convert_value`, `convert_any`, `test_any_lifetime`, `test`, `test_provides_static_type_id`, `test_provides_static_type_when_type_parameter_has_bound_with_lifetime` | 3/15 matched (target 37) | `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar` | 0/7 | 22 | 8222710.0 |
| 31 | `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 0.00 | 6/13 matched (target 15) | `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub` | 1/2 matched (target 1) | `Output` | 0/2 | 8 | 8081510.0 |
| 32 | `cast` | `starlark.Cast [ZERO]` | 0.00 | 3/3 matched (target 4) | _none_ | 0/0 matched | _none_ | - | 0 | 8000310.0 |
| 33 | `eval.compiler` | `eval.Compiler [ZERO]` | 0.00 | 6/6 matched | _none_ | 1/1 matched | _none_ | - | 0 | 8000710.0 |
| 34 | `types.bigint` | `types.Bigint [ZERO]` | 0.00 | 29/73 matched (target 35) | `unpack_integer`, `eq`, `test_parse`, `test_str`, `test_repr`, `test_equals`, `test_plus`, `test_compare_big_big`, `test_compare_big_small`, `test_compare_big_float`, `test_add_big`, `test_add_big_small`, `test_add_big_float`, `test_mul_big`, `test_mul_big_small`, `test_mul_big_float`, `test_div_big`, `test_div_big_small`, `test_div_big_float`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_big_float`, `test_percent_big`, `test_percent_big_small`, `test_percent_big_float`, `test_bit_and_big`, `test_bit_and_big_small`, `test_bit_and_float`, `test_bit_or_big`, `test_bit_or_big_small`, `test_bit_or_float`, `test_bit_xor_big`, `test_bit_xor_big_small`, `test_bit_xor_float`, `test_bit_not`, `test_left_shift`, `test_left_shift_small`, `test_left_shift_float`, `test_right_shift`, `test_right_shift_small`, `test_right_shift_float`, `test_int_function`, `test_hash`, `test_int_type_matches_bigint` | 1/1 matched | _none_ | 0/42 | 44 | 7447410.0 |
| 35 | `runtime.frozen_file_span` | `runtime.FrozenFileSpan [ZERO]` | 0.00 | 9/10 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 7011110.0 |
| 36 | `type_compiled.type_matcher_factory` | `type_compiled.TypeMatcherFactory [ZERO]` | 0.00 | 3/3 matched (target 6) | _none_ | 3/3 matched | _none_ | - | 0 | 7000610.0 |
| 37 | `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 0.00 | 4/7 matched (target 9) | `from_millis`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 0/1 | 4 | 6040910.0 |
| 38 | `dict.dict_type` | `dict.DictType [ZERO]` | 0.00 | 1/2 matched (target 4) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 6030510.0 |
| 39 | `typing.typecheck` | `typing.Typecheck [STUB]` | 0.00 | 2/5 matched | `fmt`, `find_bindings_by_name`, `find_first_binding` | 2/2 matched (target 3) | _none_ | 0/2 | 3 | 6030710.0 |
| 40 | `values.freeze_error` | `values.FreezeError [ZERO]` | 0.00 | 3/4 matched (target 6) | `from` | 3/4 matched (target 3) | `FreezeResult` | - | 2 | 6020810.0 |
| 41 | `layout.value_alloc_size` | `layout.ValueAllocSize [ZERO]` | 0.00 | 4/5 matched | `layout` | 1/1 matched | _none_ | - | 1 | 6010610.0 |
| 42 | `compiler.stmt` | `compiler.Stmt [ZERO]` | 0.00 | 25/25 matched (target 28) | _none_ | 7/7 matched (target 24) | _none_ | - | 0 | 6003210.0 |
| 43 | `profile.profiler_type` | `profile.ProfilerType [ZERO]` | 0.00 | 1/1 matched | _none_ | 2/2 matched | _none_ | - | 0 | 6000310.0 |
| 44 | `values.layout` | `values.Layout [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 6000010.0 |
| 45 | `tests.def` | `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 0/14 matched (target 4) | `test_lambda`, `test_frozen_lambda`, `test_nested_def_1`, `test_nested_def_2`, `test_nested_def_3`, `test_lambda_capture_from_module`, `test_lambda_capture_from_def`, `test_lambda_capture_reassigned_from_def`, `test_def_freeze`, `test_frozen_lambda_nest`, `test_context_captured`, `test_lambda_errors`, `test_lambda_errors_nested`, `test_double_capture_and_freeze` | 0/0 matched (target 1) | _none_ | 0/14 | 14 | 5141410.0 |
| 46 | `types.array` | `types.Array [ZERO]` | 0.00 | 23/32 matched (target 24) | `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`, `debug`, `display` | 2/2 matched | _none_ | 0/2 | 9 | 5093410.0 |
| 47 | `typing.arc_ty` | `typing.ArcTy [ZERO]` | 0.00 | 6/7 matched (target 16) | `fmt` | 3/4 matched (target 10) | `Target` | - | 2 | 5021110.0 |
| 48 | `eval.bc` | `bc.Bc [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 5000010.0 |
| 49 | `scope.scope_resolver_globals` | `scope.ScopeResolverGlobals [ZERO]` | 0.00 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 5000410.0 |
| 50 | `types.range` | `types.Range [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 5000010.0 |
| 51 | `typing.interface` | `typing.Interface [ZERO]` | 0.00 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 5000410.0 |
| 52 | `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 0.00 | 21/36 matched (target 24) | `fmt`, `r#type`, `values`, `test_enum_type_as_type_pass`, `test_enum_type_fail_runtime`, `test_enum_type_fail_compile_time`, `test_enum_is_callable`, `test_enum_value_index`, `test_enum_value_index_correct_type`, `test_enum_index`, `test_enum_index_fail`, `test_enum_call`, `test_enum_attribute_access`, `test_enum_attribute_access_invalid`, `test_enum_attribute_access_type` | 4/8 matched (target 6) | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` | 0/12 | 19 | 4194410.0 |
| 53 | `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 0.00 | 6/13 matched (target 8) | `fmt`, `new`, `compiler_args_globals`, `compiler_args`, `test_pass`, `test_fail_compile_time`, `test_fail_runtime` | 2/4 matched (target 2) | `Canonical`, `CompilerArgs` | 0/5 | 9 | 4091710.0 |
| 54 | `bc.frame` | `bc.Frame [ZERO]` | 0.00 | 16/24 matched (target 31) | `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit` | 2/2 matched | _none_ | - | 8 | 4082610.0 |
| 55 | `values.value_of` | `values.ValueOf [ZERO]` | 0.00 | 4/6 matched (target 5) | `deref`, `fmt` | 1/4 matched (target 1) | `Target`, `Canonical`, `Error` | - | 5 | 4051010.0 |
| 56 | `profile.alloc_counts` | `profile.AllocCounts [ZERO]` | 0.00 | 1/4 matched (target 5) | `normalize_for_golden_tests`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 0/1 | 4 | 4040610.0 |
| 57 | `record.record_type` | `record.RecordType [ZERO]` | 0.00 | 15/22 matched (target 17) | `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error` | 2/8 matched (target 2) | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` | 0/5 | 13 | 3133010.0 |
| 58 | `alloc.chunk` | `alloc.Chunk [ZERO]` | 0.00 | 11/19 matched (target 18) | `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`, `test_alloc_release` | 2/3 matched (target 2) | `ChunkDataEmpty` | 0/3 | 9 | 3092210.0 |
| 59 | `stdlib.call_stack` | `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 7/13 matched (target 14) | `fmt`, `global`, `test_simple`, `test_strip_one`, `test_strip_all`, `test_call_stack_frame` | 1/1 matched (target 2) | _none_ | 0/4 | 6 | 3061410.0 |
| 60 | `errors.did_you_mean` | `errors.DidYouMean [ZERO]` | 0.00 | 1/6 matched (target 2) | `prefixes`, `typos`, `best`, `very_short`, `earlier_variants_are_more_important` | 0/0 matched | _none_ | 0/5 | 5 | 3050610.0 |
| 61 | `list.alloc` | `list.Alloc [ZERO]` | 0.00 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 3040510.0 |
| 62 | `compiler.constants` | `compiler.Constants [ZERO]` | 0.00 | 1/3 matched (target 5) | `eq`, `test_constants` | 2/2 matched | _none_ | 0/1 | 2 | 3020510.0 |
| 63 | `profile.instant` | `profile.Instant [ZERO]` | 0.00 | 3/4 matched (target 9) | `sub` | 1/2 matched (target 1) | `Output` | - | 2 | 3020610.0 |
| 64 | `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/3 matched (target 1) | `Canonical`, `Error` | - | 2 | 3020510.0 |
| 65 | `sealed` | `starlark.Sealed [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 3000110.0 |
| 66 | `types.namespace` | `types.Namespace [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 3000010.0 |
| 67 | `types.record` | `types.Record [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 3000010.0 |
| 68 | `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 0.00 | 4/11 matched (target 9) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter` | 1/4 matched (target 3) | `Target`, `Item`, `IntoIter` | - | 10 | 2101510.0 |
| 69 | `util.arc_or_static` | `util.ArcOrStatic [ZERO]` | 0.00 | 5/10 matched (target 9) | `fmt`, `eq`, `partial_cmp`, `cmp`, `hash` | 2/3 matched (target 4) | `Target` | - | 6 | 2061310.0 |
| 70 | `typing.type_type` | `typing.TypeType [ZERO]` | 0.00 | 2/5 matched (target 3) | `test`, `module`, `takes_type` | 1/3 matched (target 1) | `Canonical`, `Error` | 0/3 | 5 | 2050810.0 |
| 71 | `alloc.chunk_part` | `alloc.ChunkPart [ZERO]` | 0.00 | 11/15 matched (target 16) | `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`, `test_is_full` | 1/1 matched | _none_ | 0/4 | 4 | 2041610.0 |
| 72 | `layout.const_type_id` | `layout.ConstTypeId [ZERO]` | 0.00 | 2/5 matched (target 4) | `fmt`, `eq`, `hash` | 1/1 matched | _none_ | - | 3 | 2030610.0 |
| 73 | `runtime.rust_loc` | `runtime.RustLoc [ZERO]` | 0.00 | 0/3 matched (target 1) | `rust_loc_globals`, `invoke`, `test_rust_loc` | 0/0 matched | _none_ | 0/3 | 3 | 2030310.0 |
| 74 | `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 0.00 | 10/12 matched (target 17) | `fmt`, `deref` | 2/3 matched (target 2) | `Target` | - | 3 | 2031510.0 |
| 75 | `avalues.str_` | `avalues.Str [ZERO]` | 0.00 | 11/11 matched (target 14) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | - | 2 | 2021410.0 |
| 76 | `values.stack_guard` | `values.StackGuard [ZERO]` | 0.00 | 3/4 matched | `drop` | 1/1 matched | _none_ | - | 1 | 2010510.0 |
| 77 | `collections.string_pool` | `collections.StringPool [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/1 matched | _none_ | - | 0 | 2000310.0 |
| 78 | `def_inline.local_as_value` | `def_inline.LocalAsValue [ZERO]` | 0.00 | 1/1 matched (target 2) | _none_ | 1/1 matched | _none_ | - | 0 | 2000210.0 |
| 79 | `hint` | `starlark.Hint [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 2000210.0 |
| 80 | `profile.string_index` | `profile.StringIndex [ZERO]` | 0.00 | 2/2 matched | _none_ | 2/2 matched | _none_ | - | 0 | 2000410.0 |
| 81 | `runtime.file_loader` | `runtime.FileLoader [ZERO]` | 0.00 | 1/1 matched (target 2) | _none_ | 3/3 matched | _none_ | - | 0 | 2000410.0 |
| 82 | `types.float` | `types.Float [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000010.0 |
| 83 | `types.list` | `types.List [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000010.0 |
| 84 | `types.num` | `types.Num [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000010.0 |
| 85 | `values.thin_box_slice_frozen_value` | `values.ThinBoxSliceFrozenValue [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000010.0 |
| 86 | `heap.arena` | `heap.Arena [ZERO]` | 0.00 | 18/37 matched (target 20) | `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty` | 4/7 matched (target 6) | `ChunkIter`, `Item`, `ArenaUninit` | 0/7 | 22 | 1224410.0 |
| 87 | `collections.alloca` | `collections.Alloca [ZERO]` | 0.00 | 5/22 matched (target 5) | `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat` | 1/4 matched (target 1) | `Buffer`, `Align`, `DropSliceGuard` | 0/6 | 20 | 1202610.0 |
| 88 | `stdlib` | `starlark.Stdlib [ZERO]` | 0.00 | 3/14 matched (target 3) | `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2` | 1/3 matched (target 1) | `Bool2`, `Error` | 0/11 | 13 | 1131710.0 |
| 89 | `string.interpolation` | `string.Interpolation [ZERO]` | 0.00 | 4/12 matched (target 6) | `test_incomplete_format`, `test_unsupported_format_character`, `test_parse_percent_s_one`, `test_type_support_d`, `test_type_support_o`, `test_type_support_x`, `test_type_support_e`, `test_int_min` | 4/4 matched (target 20) | _none_ | 0/8 | 8 | 1081610.0 |
| 90 | `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 0.00 | 1/5 matched | `default`, `starlark_type_repr`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 8 | 1081010.0 |
| 91 | `layout.pointer` | `layout.Pointer [ZERO]` | 0.00 | 25/32 matched (target 46) | `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `test_int_tag`, `check` | 5/5 matched | _none_ | 0/1 | 7 | 1073710.0 |
| 92 | `types.any_complex` | `types.AnyComplex [ZERO]` | 0.00 | 4/7 matched | `fmt`, `test_any_complex`, `freeze` | 1/5 matched (target 1) | `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData` | 0/2 | 7 | 1071210.0 |
| 93 | `types.any_array` | `types.AnyArray [ZERO]` | 0.00 | 3/7 matched | `fmt`, `drop`, `test_drop`, `test_allocation_size` | 1/3 matched (target 1) | `Canonical`, `IncrementOnDrop` | 0/2 | 6 | 1061010.0 |
| 94 | `util.rtabort` | `util.Rtabort [ZERO]` | 0.00 | 2/6 matched (target 3) | `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort` | 0/1 matched (target 0) | `AbortOnDrop` | 0/3 | 5 | 1050710.0 |
| 95 | `bc.if_debug` | `bc.IfDebug [ZERO]` | 0.00 | 5/8 matched (target 9) | `eq`, `partial_cmp`, `cmp` | 1/1 matched | _none_ | - | 3 | 1030910.0 |
| 96 | `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 0.00 | 1/3 matched (target 1) | `get_type_id`, `test_non_static_type_id` | 0/1 matched (target 0) | `NonStaticAny` | 0/1 | 3 | 1030410.0 |
| 97 | `avalues.simple` | `avalues.Simple [ZERO]` | 0.00 | 8/8 matched (target 10) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | - | 2 | 1021110.0 |
| 98 | `layout.value_captured` | `layout.ValueCaptured [ZERO]` | 0.00 | 4/4 matched (target 9) | _none_ | 2/4 matched (target 2) | `Canonical`, `Frozen` | - | 2 | 1020810.0 |
| 99 | `record.field` | `record.Field [ZERO]` | 0.00 | 4/5 matched (target 8) | `fmt` | 0/1 matched | `FieldGen` | - | 2 | 1020610.0 |
| 100 | `runtime.cheap_call_stack` | `runtime.CheapCallStack [ZERO]` | 0.00 | 15/17 matched | `fmt`, `default` | 3/3 matched (target 6) | _none_ | - | 2 | 1022010.0 |
| 101 | `structs.unordered_hasher` | `structs.UnorderedHasher [ZERO]` | 0.00 | 3/5 matched (target 3) | `_write`, `test_unordered_hasher` | 1/1 matched | _none_ | 0/1 | 2 | 1020610.0 |
| 102 | `heap.fast_cell` | `heap.FastCell [ZERO]` | 0.00 | 6/7 matched | `drop` | 1/1 matched | _none_ | - | 1 | 1010810.0 |
| 103 | `read_line` | `starlark.ReadLine [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/2 matched (target 1) | `NoRustyline` | - | 1 | 1010410.0 |
| 104 | `typing.bindings` | `typing.Bindings [STUB]` | 0.00 | 7/8 matched (target 18) | `get_for_clause` | 3/3 matched (target 18) | _none_ | - | 1 | 1011110.0 |
| 105 | `typing.structs` | `typing.Structs [ZERO]` | 0.00 | 7/8 matched (target 9) | `fmt` | 2/2 matched | _none_ | - | 1 | 1011010.0 |
| 106 | `analysis.lint_message` | `analysis.LintMessage [ZERO]` | 0.00 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 1000210.0 |
| 107 | `types.bool` | `types.Bool [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 108 | `types.enumeration` | `types.Enumeration [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 109 | `types.int` | `types.Int [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 110 | `types.none` | `types.None [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 111 | `types.set` | `types.Set [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 112 | `types.string` | `types.String [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 113 | `typing` | `starlark.Typing [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 114 | `typing.function` | `typing.Function [STUB]` | 0.00 | 12/12 matched (target 24) | _none_ | 3/3 matched | _none_ | - | 0 | 1001510.0 |
| 115 | `set.methods` | `set.Methods [STUB]` | 0.00 | 18/68 matched (target 19) | `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self` | 1/1 matched (target 3) | _none_ | 0/50 | 50 | 506910.0 |
| 116 | `string.str_type` | `string.StrType [ZERO]` | 0.00 | 2/47 matched (target 24) | `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_string_corruption`, `test_escape_characters`, `test_string_hash`, `test_zero_length_string_hash_is_not_zero`, `test_string_len`, `test_arithmetic_on_string`, `test_slice_string`, `test_string_is_in`, `test_successive_add`, `test_string_index`, `test_str` | 0/4 matched (target 0) | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` | 0/11 | 49 | 495110.0 |
| 117 | `int.int_or_big` | `int.IntOrBig [STUB]` | 0.00 | 24/46 matched (target 50) | `starlark_type_repr`, `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_small_big`, `test_floor_div_small`, `test_percent_big`, `test_percent_big_small`, `test_percent_small_big`, `test_percent_small` | 3/7 matched (target 11) | `Canonical`, `Err`, `Error`, `Output` | 0/9 | 26 | 265310.0 |
| 118 | `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 0.00 | 6/29 matched (target 11) | `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`, `test_empty`, `test_from_iter_sized`, `test_from_iter_unknown_size`, `test_stress` | 1/3 matched (target 1) | `ThinBoxSliceLayout`, `Target` | 0/4 | 25 | 253210.0 |
| 119 | `set.value` | `set.Value [ZERO]` | 0.00 | 30/50 matched (target 44) | `fmt`, `test_bit_or`, `test_bit_or_lhs_empty`, `test_bit_or_rhs_empty`, `test_bit_or_fail_iter`, `test_bit_or_ord`, `test_bit_and`, `test_bit_and_lhs_empty`, `test_bit_and_rhs_empty`, `test_bit_and_ord`, `test_bit_and_fail_iter`, `test_bit_xor`, `test_bit_xor_ord`, `test_bit_xor_lhs_empty`, `test_bit_xor_rhs_empty`, `test_bit_xor_fail_iter`, `test_sub`, `test_sub_empty_lhs`, `test_sub_empty_rhs`, `test_sub_fail_iter` | 6/9 matched (target 8) | `Canonical`, `Frozen`, `ContentRef` | 0/19 | 23 | 235910.0 |
| 120 | `values.typing.callable` | `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 0.00 | 12/32 matched (target 29) | `clone`, `fmt`, `trace`, `_assert_sync_send`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_pass`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `test_callable_checked_runtime`, `module`, `good`, `bad` | 5/8 matched (target 5) | `Canonical`, `Error`, `Frozen` | 0/15 | 23 | 234010.0 |
| 121 | `typing.user` | `typing.User [ZERO]` | 0.00 | 13/27 matched (target 25) | `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`, `test_intersect_with_abstract_type`, `test_ty_user_intersects_with_base_starlark_value` | 5/8 matched | `AbstractPlant`, `FruitCallable`, `Fruit` | 0/10 | 17 | 173510.0 |
| 122 | `float.float` | `float.Float [ZERO]` | 0.00 | 26/39 matched (target 33) | `fmt`, `non_finite`, `test_write_non_finite`, `decimal`, `test_write_decimal`, `scientific`, `test_write_scientific`, `compact`, `test_write_compact`, `test_arithmetic_operators`, `test_dictionary_key`, `test_comparisons`, `test_comparisons_by_sorting` | 1/3 matched (target 1) | `Canonical`, `Error` | 0/12 | 15 | 154210.0 |
| 123 | `layout.typed` | `layout.ValueTyped [ZERO]` | 0.00 | 21/31 matched (target 43) | `fmt`, `serialize`, `eq`, `deref`, `unpack_value_impl`, `int`, `test_unpack_value_for_frozen_value_typed`, `module`, `mutable`, `takes_frozen_value_typed` | 2/7 matched (target 2) | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` | 0/5 | 15 | 153810.0 |
| 124 | `scope.payload` | `scope.Payload [ZERO]` | 0.00 | 0/7 matched (target 0) | `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id` | 9/17 matched (target 14) | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` | - | 15 | 152410.0 |
| 125 | `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 0.00 | 4/18 matched (target 8) | `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `default`, `fmt`, `eq`, `across_lengths`, `test_strings`, `test_ints`, `test_mixed_types`, `test_default`, `test_empty` | 2/3 matched (target 2) | `Target` | 0/6 | 15 | 152110.0 |
| 126 | `string.repr` | `string.Repr [ZERO]` | 0.00 | 9/22 matched (target 11) | `or4`, `push_vec_tail`, `test_to_repr`, `test_string_repr`, `test`, `test_to_repr_long_smoke`, `string_repr_for_test`, `to_repr_sse`, `to_repr_no_escape_all_lengths`, `to_repr_tail_escape_all_lengths`, `to_repr_middle_escape_all_lengths`, `test_chunk_non_ascii_or_need_escape`, `load` | 1/1 matched | _none_ | 0/11 | 13 | 132310.0 |
| 127 | `list.value` | `list.Value [ZERO]` | 0.00 | 46/56 matched (target 86) | `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare` | 6/8 matched (target 9) | `List`, `Canonical` | 0/7 | 12 | 126410.0 |
| 128 | `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 0.00 | 3/13 matched (target 6) | `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_lookup_nonexistent_type`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered` | 2/4 matched (target 3) | `TestSimpleType`, `TestComplexGen` | 0/9 | 12 | 121710.0 |
| 129 | `stdlib.extra` | `stdlib.Extra [ZERO]` | 0.00 | 5/16 matched (target 17) | `fmt`, `print`, `pprint`, `pstr`, `prepr`, `test_filter`, `test_map`, `test_debug`, `test_print`, `test_pstr`, `test_prepr` | 3/4 matched (target 3) | `PrintHandlerImpl` | 0/6 | 12 | 122010.0 |
| 130 | `dict.value` | `dict.Value [ZERO]` | 0.00 | 44/52 matched (target 68) | `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `test_mutate_dict`, `test_get_str`, `test_repr_cycle` | 7/10 matched | `Canonical`, `Frozen`, `ContentRef` | 0/3 | 11 | 116210.0 |
| 131 | `record.globals` | `record.Globals [ZERO]` | 0.00 | 1/12 matched (target 1) | `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid` | 0/0 matched | _none_ | 0/9 | 11 | 111210.0 |
| 132 | `alloc.chain` | `alloc.Chain [ZERO]` | 0.00 | 14/22 matched (target 19) | `drop`, `test_default`, `test_new_drop`, `test_new_drop_many`, `test_split_at`, `test_split_at_len`, `test_split_at_zero`, `test_depth` | 3/5 matched (target 3) | `Item`, `ResetSplitAtZeroTest` | 0/7 | 10 | 102710.0 |
| 133 | `heap.heap_type` | `heap.HeapType [ZERO]` | 0.00 | 37/47 matched (target 59) | `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `test_send_sync`, `test_string_reallocated_on_heap`, `test_interned_string_equal`, `validate_str_interning`, `append_x`, `test_interned_str_starlark` | 8/8 matched (target 9) | _none_ | 0/6 | 10 | 105510.0 |
| 134 | `range.range_type` | `range.RangeType [ZERO]` | 0.00 | 14/24 matched (target 21) | `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `test_range_exhaustive`, `test_max_len` | 1/1 matched (target 2) | _none_ | 0/8 | 10 | 102510.0 |
| 135 | `stdlib.partial` | `stdlib.Partial [ZERO]` | 0.00 | 4/12 matched (target 7) | `partial`, `fmt`, `eq`, `test_simple`, `test_star_to_partial`, `test_start_to_returned_func`, `test_no_args_to_partial`, `test_typecheck_bug` | 3/5 matched (target 3) | `Frozen`, `Canonical` | 0/6 | 10 | 101710.0 |
| 136 | `alloc.allocator` | `alloc.Allocator [ZERO]` | 0.00 | 11/18 matched (target 15) | `fmt`, `default`, `drop`, `test_small`, `test_big`, `random_iteration`, `test_many` | 2/3 matched (target 2) | `Item` | 0/4 | 8 | 82110.0 |
| 137 | `profile.bc` | `profile.Bc [ZERO]` | 0.00 | 12/19 matched (target 24) | `sum`, `add_assign`, `default`, `test_smoke`, `test_smoke_2`, `test_bc_profile_data_merge`, `test_bc_pairs_profile_data_merge` | 9/10 matched (target 13) | `Data` | 0/4 | 8 | 82910.0 |
| 138 | `tuple.unpack` | `tuple.Unpack [ZERO]` | 0.00 | 1/5 matched | `default`, `starlark_type_repr`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 8 | 81010.0 |
| 139 | `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 0.00 | 33/39 matched (target 47) | `fmt`, `check_matches`, `starlark_type_repr`, `alloc_value`, `hash`, `eq` | 5/7 matched (target 12) | `StaticType`, `Canonical` | - | 8 | 84610.0 |
| 140 | `bigint.convert` | `bigint.Convert [ZERO]` | 0.00 | 3/8 matched (target 23) | `unpack_value_impl`, `test_unpack_int_error`, `module`, `takes_i32`, `takes_i64` | 0/2 matched (target 6) | `Canonical`, `Error` | 0/4 | 7 | 71010.0 |
| 141 | `dict.methods` | `dict.Methods [ZERO]` | 0.00 | 10/17 matched (target 12) | `test_error_codes`, `test_dict_add`, `test_dict_with_duplicates`, `test_dict_update_with_self_pos`, `test_dict_update_with_self_as_kwargs`, `test_frozen_dict_cannot_be_updated_with_self_pos`, `test_frozen_dict_cannot_be_updated_with_self_as_kwargs` | 0/0 matched | _none_ | 0/7 | 7 | 71710.0 |
| 142 | `docs.parse` | `docs.Parse [ZERO]` | 0.00 | 8/15 matched (target 11) | `parses_starlark_docstring`, `parses_rust_docstring`, `parses_and_removes_sections_from_starlark_docstring`, `parses_and_removes_sections_from_rust_docstring`, `arg`, `parses_starlark_function_docstring`, `parses_rust_function_docstring` | 1/1 matched | _none_ | 0/7 | 7 | 71610.0 |
| 143 | `funcs.other` | `funcs.Other [ZERO]` | 0.00 | 12/19 matched (target 13) | `r#type`, `test_abs`, `test_constants`, `test_chr`, `test_hash`, `test_int`, `test_tuple` | 0/0 matched (target 1) | _none_ | 0/6 | 7 | 71910.0 |
| 144 | `layout.complex` | `layout.Complex [ZERO]` | 0.00 | 9/13 matched (target 15) | `unpack_value_impl`, `fmt`, `test_module`, `test_unpack` | 1/4 matched (target 1) | `Canonical`, `Error`, `Frozen` | 0/2 | 7 | 71710.0 |
| 145 | `profile.aggregated` | `profile.Aggregated [ZERO]` | 0.00 | 17/24 matched (target 35) | `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `test_stacks_collect`, `test_stacks_collect_retained`, `test_merge`, `make` | 8/8 matched (target 10) | _none_ | 0/6 | 7 | 73210.0 |
| 146 | `record.ty_record_type` | `record.TyRecordType [ZERO]` | 0.00 | 0/7 matched (target 0) | `test_good`, `test_fail_compile_time`, `test_fail_runtime_time`, `test_record_instance_typechecker_ty`, `test_typecheck_field_pass`, `test_typecheck_field_fail`, `test_typecheck_record_type_call` | 1/1 matched | _none_ | 0/7 | 7 | 70810.0 |
| 147 | `string.simd` | `string.Simd [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 1/8 matched (target 4) | `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask` | 2/2 matched | _none_ | - | 7 | 71010.0 |
| 148 | `tuple.value` | `tuple.Value [ZERO]` | 0.00 | 24/31 matched (target 25) | `fmt`, `new`, `offset_of_content`, `typechecker_ty`, `test_to_str`, `test_repr_cycle`, `test_tuple_ellipsis_runtime` | 3/3 matched | _none_ | 0/3 | 7 | 73410.0 |
| 149 | `typed.string` | `typed.String [ZERO]` | 0.00 | 8/15 matched (target 59) | `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`, `test_string_hashes` | 3/3 matched (target 4) | _none_ | 0/1 | 7 | 71810.0 |
| 150 | `adapter.implementation` | `adapter.Implementation [ZERO]` | 0.00 | 17/23 matched (target 27) | `prepare_dap_adapter`, `fmt`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints` | 6/6 matched (target 10) | _none_ | - | 6 | 62910.0 |
| 151 | `assert.assert` | `assert.Assert [STUB]` | 0.00 | 44/50 matched (target 64) | `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck` | 2/2 matched | _none_ | 0/4 | 6 | 65210.0 |
| 152 | `bc.instrs` | `bc.Instrs [ZERO]` | 0.00 | 19/24 matched (target 29) | `handle`, `drop`, `opcodes`, `fmt`, `display` | 3/4 matched (target 3) | `HandlerImpl` | 0/2 | 6 | 62810.0 |
| 153 | `compiler.scope` | `compiler.Scope [ZERO]` | 0.00 | 48/51 matched (target 70) | `from`, `assign_ident_impl`, `new` | 17/20 matched (target 28) | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` | - | 6 | 67110.0 |
| 154 | `heap.send` | `heap.Send [ZERO]` | 0.00 | 2/5 matched (target 6) | `deref`, `deref_mut`, `fmt` | 3/6 matched (target 3) | `Sealed`, `Target`, `StaticType` | - | 6 | 61110.0 |
| 155 | `int.i32` | `int.I32 [ZERO]` | 0.00 | 0/4 matched (target 3) | `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl` | 0/2 matched (target 0) | `Canonical`, `Error` | - | 6 | 60610.0 |
| 156 | `list.unpack` | `list.Unpack [ZERO]` | 0.00 | 3/5 matched (target 8) | `into_iter`, `test_unpack` | 1/5 matched (target 3) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 6 | 61010.0 |
| 157 | `profile.csv` | `profile.Csv [ZERO]` | 0.00 | 6/10 matched (target 7) | `new`, `format_for_csv`, `test_csv_writer`, `test_quote_str_for_csv` | 1/3 matched (target 2) | `Impl`, `CsvValue` | 0/2 | 6 | 61310.0 |
| 158 | `structs.value` | `structs.Value [ZERO]` | 0.00 | 15/21 matched (target 18) | `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_to_json`, `test_comparison_bug` | 1/1 matched (target 3) | _none_ | 0/5 | 6 | 62210.0 |
| 159 | `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 0.00 | 6/8 matched (target 6) | `next`, `size_hint` | 0/3 matched (target 1) | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` | - | 5 | 51110.0 |
| 160 | `debug.inspect` | `debug.Inspect [ZERO]` | 0.00 | 4/9 matched (target 4) | `debugger`, `debug_inspect_stack`, `debug_inspect_variables`, `test_debug_stack`, `test_debug_variables` | 0/0 matched | _none_ | 0/5 | 5 | 50910.0 |
| 161 | `dict.refs` | `dict.Refs [ZERO]` | 0.00 | 7/9 matched (target 13) | `from_value`, `deref` | 4/7 matched (target 11) | `Target`, `Canonical`, `Error` | - | 5 | 51610.0 |
| 162 | `environment.modules` | `environment.Modules [ZERO]` | 0.00 | 38/43 matched (target 47) | `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo` | 4/4 matched (target 6) | _none_ | 0/5 | 5 | 54710.0 |
| 163 | `params.spec` | `params.Spec [ZERO]` | 0.00 | 33/38 matched (target 33) | `as_value`, `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl` | 6/6 matched (target 11) | _none_ | - | 5 | 54410.0 |
| 164 | `profile.stmt` | `profile.Stmt [ZERO]` | 0.00 | 13/17 matched (target 20) | `r#gen`, `test_coverage`, `test_empty`, `test_merge` | 8/9 matched | `Data` | 0/3 | 5 | 52610.0 |
| 165 | `profile.time_flame` | `profile.TimeFlame [ZERO]` | 0.00 | 15/19 matched (target 18) | `r#gen`, `test_time_flame_works_inside_frozen_module`, `register_sleep`, `sleep` | 10/11 matched (target 15) | `Data` | 0/3 | 5 | 53010.0 |
| 166 | `typing.iter` | `typing.Iter [ZERO]` | 0.00 | 3/6 matched (target 5) | `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail` | 2/4 matched (target 2) | `NonInstantiable`, `Canonical` | 0/3 | 5 | 51010.0 |
| 167 | `values.owned` | `values.Owned [ZERO]` | 0.00 | 26/29 matched (target 32) | `fmt`, `downcast_starlark`, `deref` | 3/5 matched | `Canonical`, `Target` | - | 5 | 53410.0 |
| 168 | `values.unpack` | `values.Unpack [ZERO]` | 0.00 | 8/9 matched (target 14) | `error` | 3/7 matched | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` | - | 5 | 51610.0 |
| 169 | `avalues.static_` | `avalues.Static [ZERO]` | 0.00 | 8/9 matched | `test_alloc_static_simple` | 2/5 matched (target 2) | `StarlarkValue`, `ExtraElem`, `MySimpleValue` | 0/1 | 4 | 41410.0 |
| 170 | `bc.addr` | `bc.Addr [ZERO]` | 0.00 | 20/23 matched (target 35) | `add_assign`, `get_instr_mut`, `sub_usize` | 5/6 matched (target 5) | `Output` | - | 4 | 42910.0 |
| 171 | `dict.alloc` | `dict.Alloc [ZERO]` | 0.00 | 0/3 matched (target 1) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 40510.0 |
| 172 | `enumeration.globals` | `enumeration.Globals [ZERO]` | 0.00 | 1/5 matched (target 1) | `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr` | 0/0 matched | _none_ | 0/3 | 4 | 40510.0 |
| 173 | `heap.repr` | `heap.Repr [ZERO]` | 0.00 | 23/27 matched (target 34) | `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut` | 5/5 matched (target 8) | _none_ | - | 4 | 43210.0 |
| 174 | `list.methods` | `list.Methods [ZERO]` | 0.00 | 7/11 matched (target 13) | `list_methods`, `test_error_codes`, `test_index`, `recursive_list` | 0/0 matched | _none_ | 0/3 | 4 | 41110.0 |
| 175 | `params.parser` | `params.Parser [ZERO]` | 0.00 | 5/9 matched (target 5) | `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args` | 1/1 matched | _none_ | 0/4 | 4 | 41010.0 |
| 176 | `profile.flamegraph` | `profile.Flamegraph [ZERO]` | 0.00 | 6/10 matched (target 13) | `new`, `test_flamegraph_writer`, `test_flamegraph_data`, `test_merge` | 3/3 matched | _none_ | 0/3 | 4 | 41310.0 |
| 177 | `profile.mode` | `profile.Mode [ZERO]` | 0.00 | 1/4 matched | `fmt`, `name`, `from_str` | 1/2 matched (target 1) | `Err` | - | 4 | 40610.0 |
| 178 | `profile.typecheck` | `profile.Typecheck [ZERO]` | 0.00 | 5/8 matched (target 6) | `r#gen`, `test_typecheck_profile`, `test_typecheck_profile_merge` | 4/5 matched | `Data` | 0/2 | 4 | 41310.0 |
| 179 | `runtime.inlined_frame` | `runtime.InlinedFrame [ZERO]` | 0.00 | 5/9 matched (target 6) | `eq`, `test_inline_into`, `make_span`, `assert_stack` | 3/3 matched | _none_ | 0/3 | 4 | 41210.0 |
| 180 | `set.set` | `set.Set [ZERO]` | 0.00 | 1/5 matched (target 1) | `set`, `test_set_type_as_type_compile_time`, `test_return_set_type_as_type_compile_time`, `test_set_type_as_type_run_time` | 0/0 matched | _none_ | 0/3 | 4 | 40510.0 |
| 181 | `string.methods` | `string.Methods [ZERO]` | 0.00 | 37/41 matched (target 50) | `test_error_codes`, `test_count`, `test_find`, `test_opaque_iterator` | 1/1 matched (target 4) | _none_ | 0/4 | 4 | 44210.0 |
| 182 | `structs.alloc` | `structs.Alloc [ZERO]` | 0.00 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 40510.0 |
| 183 | `tests.util` | `util.Util [STUB] [PROVENANCE-FALLBACK]` | 0.00 | 0/3 matched (target 0) | `alloc_value`, `alloc_frozen_value`, `trim_rust_backtrace` | 0/1 matched (target 0) | `TestComplexValue` | - | 4 | 40410.0 |
| 184 | `typing.custom` | `typing.Custom [ZERO]` | 0.00 | 31/35 matched (target 49) | `eq`, `hash`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | - | 4 | 43810.0 |
| 185 | `avalues.list` | `avalues.List [ZERO]` | 0.00 | 9/10 matched (target 18) | `alloc_list_concat` | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 3 | 31410.0 |
| 186 | `bc.opcode` | `bc.Opcode [ZERO]` | 0.00 | 6/7 matched (target 10) | `opcode_count` | 3/5 matched (target 3) | `ByNumber`, `FindOpcode` | 0/1 | 3 | 31210.0 |
| 187 | `bc.repr` | `bc.Repr [ZERO]` | 0.00 | 4/6 matched (target 5) | `size_of_repr`, `handle` | 2/3 matched (target 2) | `HandlerImpl` | - | 3 | 30910.0 |
| 188 | `debug.evaluate` | `debug.Evaluate [ZERO]` | 0.00 | 1/4 matched (target 1) | `debugger`, `debug_evaluate`, `test_debug_evaluate` | 0/0 matched | _none_ | 0/3 | 3 | 30410.0 |
| 189 | `enumeration.value` | `enumeration.Value [ZERO]` | 0.00 | 6/9 matched (target 10) | `fmt`, `index`, `value` | 1/1 matched (target 8) | _none_ | - | 3 | 31010.0 |
| 190 | `float.unpack` | `float.Unpack [ZERO]` | 0.00 | 2/3 matched | `test_unpack_float` | 1/3 matched (target 1) | `Canonical`, `Error` | 0/1 | 3 | 30610.0 |
| 191 | `list.refs` | `list.Refs [ZERO]` | 0.00 | 9/9 matched (target 29) | _none_ | 2/5 matched (target 10) | `Target`, `Canonical`, `Error` | - | 3 | 31410.0 |
| 192 | `profile.heap` | `profile.Heap [ZERO]` | 0.00 | 11/13 matched (target 27) | `r#gen`, `test_profiling` | 10/11 matched | `Data` | 0/1 | 3 | 32410.0 |
| 193 | `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 0.00 | 5/6 matched (target 9) | `unpack_value_impl` | 0/2 matched (target 1) | `Canonical`, `Error` | - | 3 | 30810.0 |
| 194 | `symbol.map` | `symbol.Map [ZERO]` | 0.00 | 9/12 matched (target 11) | `fmt`, `new`, `with_capacity` | 1/1 matched | _none_ | - | 3 | 31310.0 |
| 195 | `tuple.refs` | `tuple.Refs [ZERO]` | 0.00 | 6/7 matched (target 15) | `unpack_value_impl` | 2/4 matched (target 2) | `Canonical`, `Error` | - | 3 | 31110.0 |
| 196 | `type_compiled.globals` | `type_compiled.Globals [ZERO]` | 0.00 | 1/4 matched (target 1) | `eval_type`, `isinstance`, `test_typechecking` | 0/0 matched | _none_ | 0/1 | 3 | 30410.0 |
| 197 | `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 0.00 | 10/10 matched (target 13) | _none_ | 4/7 matched | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` | - | 3 | 31710.0 |
| 198 | `typing.never` | `typing.Never [ZERO]` | 0.00 | 4/6 matched (target 7) | `test_never_runtime`, `test_never_compile_time` | 2/3 matched (target 2) | `Canonical` | 0/2 | 3 | 30910.0 |
| 199 | `values.typing.ty` | `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` | 0.00 | 2/5 matched (target 4) | `test_isinstance`, `test_pass`, `test_fail_compile_time` | 1/1 matched | _none_ | 0/3 | 3 | 30610.0 |
| 200 | `avalues.array` | `avalues.Array [ZERO]` | 0.00 | 9/9 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 2 | 21310.0 |
| 201 | `avalues.complex` | `avalues.Complex [ZERO]` | 0.00 | 6/6 matched (target 14) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | - | 2 | 21110.0 |
| 202 | `avalues.tuple` | `avalues.Tuple [ZERO]` | 0.00 | 8/8 matched (target 16) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 2 | 21210.0 |
| 203 | `bc.bytecode` | `bc.Bytecode [ZERO]` | 0.00 | 6/7 matched (target 10) | `handle` | 1/2 matched (target 1) | `HandlerImpl` | - | 2 | 20910.0 |
| 204 | `bc.call` | `bc.Call [ZERO]` | 0.00 | 3/4 matched (target 15) | `fmt` | 4/5 matched (target 8) | `Args` | - | 2 | 20910.0 |
| 205 | `bc.definitely_assigned` | `bc.DefinitelyAssigned [ZERO]` | 0.00 | 2/4 matched (target 7) | `new`, `assert_smaller_then` | 1/1 matched | _none_ | - | 2 | 20510.0 |
| 206 | `bc.instr_arg` | `bc.InstrArg [ZERO]` | 0.00 | 4/5 matched (target 84) | `fmt` | 3/4 matched (target 42) | `HandlerImpl` | - | 2 | 20910.0 |
| 207 | `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 0.00 | 10/11 matched (target 25) | `add` | 7/8 matched (target 7) | `Output` | - | 2 | 21910.0 |
| 208 | `bool.type_repr` | `bool.TypeRepr [ZERO]` | 0.00 | 0/1 matched | `starlark_type_repr` | 0/1 matched (target 0) | `Canonical` | - | 2 | 20210.0 |
| 209 | `build` | `starlark.Build [ZERO]` | 0.00 | 0/2 matched (target 0) | `main`, `rust_nightly` | 0/0 matched (target 1) | _none_ | - | 2 | 20210.0 |
| 210 | `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 0.00 | 2/3 matched (target 2) | `drop` | 0/1 matched (target 0) | `Guard` | - | 2 | 20410.0 |
| 211 | `compiler.args` | `compiler.Args [ZERO]` | 0.00 | 10/11 matched | `args` | 1/2 matched (target 1) | `Never` | - | 2 | 21310.0 |
| 212 | `compiler.def` | `compiler.Def [ZERO]` | 0.00 | 38/39 matched (target 46) | `fmt` | 12/13 matched (target 17) | `Frozen` | - | 2 | 25210.0 |
| 213 | `compiler.expr` | `compiler.Expr [ZERO]` | 0.00 | 59/59 matched (target 63) | _none_ | 9/11 matched (target 56) | `AstLiteralCompile`, `CompilerExprUtil` | - | 2 | 27010.0 |
| 214 | `eval.bc.compiler.stmt` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` | 0.00 | 8/10 matched (target 11) | `write_if_then`, `write_if_else` | 0/0 matched | _none_ | - | 2 | 21010.0 |
| 215 | `funcs.min_max` | `funcs.MinMax [ZERO]` | 0.00 | 3/5 matched (target 3) | `max`, `min` | 0/0 matched | _none_ | - | 2 | 20510.0 |
| 216 | `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 0.00 | 0/1 matched (target 4) | `drop` | 5/6 matched (target 5) | `Canonical` | - | 2 | 20710.0 |
| 217 | `intern.interner` | `intern.Interner [ZERO]` | 0.00 | 1/3 matched (target 5) | `test_intern`, `test_string_value_intern` | 2/2 matched | _none_ | 0/2 | 2 | 20510.0 |
| 218 | `list.globals` | `list.Globals [ZERO]` | 0.00 | 4/5 matched | `list` | 0/1 matched | `ListType` | - | 2 | 20610.0 |
| 219 | `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 0.00 | 9/10 matched | `drop_non_drop` | 2/3 matched (target 2) | `RowKind` | 0/1 | 2 | 21310.0 |
| 220 | `set.refs` | `set.Refs [ZERO]` | 0.00 | 5/5 matched (target 14) | _none_ | 3/5 matched (target 11) | `Canonical`, `Error` | - | 2 | 21010.0 |
| 221 | `stdlib.internal` | `stdlib.Internal [ZERO]` | 0.00 | 2/4 matched (target 2) | `ty_of_value_debug`, `test_ty_of_value_debug` | 0/0 matched | _none_ | 0/1 | 2 | 20410.0 |
| 222 | `structs.refs` | `structs.Refs [ZERO]` | 0.00 | 5/5 matched (target 8) | _none_ | 2/4 matched | `Canonical`, `Error` | - | 2 | 20910.0 |
| 223 | `symbol.symbol` | `symbol.Symbol [ZERO]` | 0.00 | 7/9 matched (target 11) | `fmt`, `eq` | 1/1 matched | _none_ | - | 2 | 21010.0 |
| 224 | `types.function` | `types.Function [ZERO]` | 0.00 | 12/13 matched (target 27) | `new` | 11/12 matched (target 14) | `Canonical` | - | 2 | 22510.0 |
| 225 | `typing.any` | `typing.Any [ZERO]` | 0.00 | 2/4 matched | `test_any_runtime`, `test_any_compile_time` | 1/1 matched | _none_ | 0/2 | 2 | 20510.0 |
| 226 | `typing.callable` | `typing.Callable [ZERO]` | 0.00 | 6/7 matched (target 10) | `fmt` | 1/2 matched (target 1) | `TyCallableInner` | - | 2 | 20910.0 |
| 227 | `values.index` | `values.Index [ZERO]` | 0.00 | 4/6 matched (target 5) | `test_convert_index`, `test_apply_slice` | 0/0 matched | _none_ | 0/2 | 2 | 20610.0 |
| 228 | `values.traits` | `values.Traits [ZERO]` | 0.00 | 55/56 matched (target 55) | `please_use_starlark_type_macro` | 2/3 matched (target 2) | `Canonical` | - | 2 | 25910.0 |
| 229 | `values.type_repr` | `values.TypeRepr [ZERO]` | 0.00 | 2/3 matched (target 6) | `test_canonical_for_complex_value` | 2/3 matched (target 6) | `Canonical` | 0/1 | 2 | 20610.0 |
| 230 | `alloc.per_thread` | `alloc.PerThread [ZERO]` | 0.00 | 5/6 matched (target 5) | `test_release_partial` | 1/1 matched | _none_ | 0/1 | 1 | 10710.0 |
| 231 | `compiler.assign_modify` | `compiler.AssignModify [ZERO]` | 0.00 | 2/2 matched (target 3) | _none_ | 0/1 matched (target 0) | `AssignOnWriteBc` | - | 1 | 10310.0 |
| 232 | `compiler.if_compiler` | `compiler.IfCompiler [ZERO]` | 0.00 | 5/6 matched (target 5) | `wr` | 0/0 matched | _none_ | - | 1 | 10610.0 |
| 233 | `debug.adapter` | `debug.Adapter [ZERO]` | 0.00 | 21/22 matched (target 23) | `fmt` | 14/14 matched (target 29) | _none_ | - | 1 | 13610.0 |
| 234 | `dict.globals` | `dict.Globals [ZERO]` | 0.00 | 2/3 matched (target 4) | `dict` | 0/0 matched | _none_ | - | 1 | 10310.0 |
| 235 | `docs` | `docs.Docs [ZERO]` | 0.00 | 12/13 matched (target 16) | `default` | 10/10 matched (target 15) | _none_ | - | 1 | 12310.0 |
| 236 | `eval.bc.compiler.call` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` | 0.00 | 4/5 matched (target 8) | `mark_definitely_assigned_after` | 0/0 matched (target 3) | _none_ | - | 1 | 10510.0 |
| 237 | `float.globals` | `float.Globals [ZERO]` | 0.00 | 1/2 matched (target 1) | `float` | 0/0 matched (target 4) | _none_ | - | 1 | 10210.0 |
| 238 | `int.globals` | `int.Globals [ZERO]` | 0.00 | 1/2 matched | `int` | 0/0 matched | _none_ | - | 1 | 10210.0 |
| 239 | `namespace.globals` | `namespace.Globals [ZERO]` | 0.00 | 1/2 matched (target 1) | `namespace` | 0/0 matched | _none_ | - | 1 | 10210.0 |
| 240 | `namespace.typing` | `namespace.Typing [ZERO]` | 0.00 | 6/7 matched (target 8) | `fmt` | 3/3 matched | _none_ | - | 1 | 11010.0 |
| 241 | `profile.by_type` | `profile.ByType [ZERO]` | 0.00 | 5/6 matched (target 7) | `normalize_for_golden_tests` | 1/1 matched | _none_ | 0/1 | 1 | 10710.0 |
| 242 | `range.globals` | `range.Globals [ZERO]` | 0.00 | 1/2 matched (target 1) | `range` | 0/0 matched | _none_ | - | 1 | 10210.0 |
| 243 | `record.instance` | `record.Instance [ZERO]` | 0.00 | 12/13 matched (target 17) | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 11410.0 |
| 244 | `structs.structs` | `structs.Structs [ZERO]` | 0.00 | 3/4 matched (target 3) | `r#struct` | 1/1 matched | _none_ | - | 1 | 10510.0 |
| 245 | `types.unbound` | `types.Unbound [ZERO]` | 0.00 | 3/4 matched | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 10510.0 |
| 246 | `values.recursive_repr_or_json_guard` | `values.RecursiveReprOrJsonGuard [ZERO]` | 0.00 | 2/3 matched (target 4) | `drop` | 4/4 matched | _none_ | - | 1 | 10710.0 |
| 247 | `__derive_refs` | `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched | _none_ | 0/0 matched (target 1) | _none_ | - | 0 | 10.0 |
| 248 | `__derive_refs.components` | `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 410.0 |
| 249 | `__derive_refs.invoke_macro_error` | `deriverefs.InvokeMacroError [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 210.0 |
| 250 | `__derive_refs.sig` | `deriverefs.Sig [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 3/3 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 410.0 |
| 251 | `allocator.alloc` | `allocator.Alloc [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 252 | `allocator.api` | `allocator.Api [ZERO]` | 0.00 | 0/0 matched | _none_ | 2/2 matched | _none_ | - | 0 | 210.0 |
| 253 | `assert.conformance` | `assert.Conformance [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched | _none_ | - | 0 | 610.0 |
| 254 | `bc.compiler` | `bc.Compiler [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 255 | `bc.for_loop` | `bc.ForLoop [ZERO]` | 0.00 | 0/0 matched (target 2) | _none_ | 1/1 matched | _none_ | - | 0 | 110.0 |
| 256 | `bc.instr` | `bc.Instr [ZERO]` | 0.00 | 0/0 matched | _none_ | 2/2 matched (target 5) | _none_ | - | 0 | 210.0 |
| 257 | `bc.writer` | `bc.Writer [ZERO]` | 0.00 | 42/42 matched (target 44) | _none_ | 4/4 matched | _none_ | - | 0 | 4610.0 |
| 258 | `callable.param` | `callable.Param [ZERO]` | 0.00 | 1/1 matched (target 6) | _none_ | 2/2 matched (target 7) | _none_ | - | 0 | 310.0 |
| 259 | `compiler.assign` | `compiler.Assign [ZERO]` | 0.00 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 260 | `compiler.error` | `compiler.Error [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 2/2 matched (target 24) | _none_ | 1/1 matched (target 14) | _none_ | - | 0 | 310.0 |
| 261 | `compiler.expr_bool` | `compiler.ExprBool [ZERO]` | 0.00 | 4/4 matched (target 5) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 510.0 |
| 262 | `compiler.module` | `compiler.Module [ZERO]` | 0.00 | 6/6 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 710.0 |
| 263 | `compiler.type_expr` | `compiler.TypeExpr [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 2/2 matched (target 7) | _none_ | 1/1 matched (target 17) | _none_ | - | 0 | 310.0 |
| 264 | `compiler.types` | `compiler.Types [ZERO]` | 0.00 | 8/8 matched (target 9) | _none_ | 1/1 matched (target 7) | _none_ | - | 0 | 910.0 |
| 265 | `docs.code` | `docs.Code [ZERO]` | 0.00 | 7/7 matched (target 14) | _none_ | 0/0 matched | _none_ | - | 0 | 710.0 |
| 266 | `docs.markdown` | `docs.Markdown [ZERO]` | 0.00 | 18/18 matched (target 19) | _none_ | 2/2 matched | _none_ | - | 0 | 2010.0 |
| 267 | `docs.multipage` | `docs.Multipage [ZERO]` | 0.00 | 6/6 matched | _none_ | 5/5 matched (target 7) | _none_ | - | 0 | 1110.0 |
| 268 | `environment` | `starlark.Environment [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched (target 5) | _none_ | - | 0 | 110.0 |
| 269 | `environment.names` | `environment.Names [ZERO]` | 0.00 | 13/13 matched (target 14) | _none_ | 2/2 matched | _none_ | - | 0 | 1510.0 |
| 270 | `environment.slots` | `environment.Slots [ZERO]` | 0.00 | 8/8 matched (target 10) | _none_ | 3/3 matched | _none_ | - | 0 | 1110.0 |
| 271 | `errors` | `starlark.Errors [STUB] [PROVENANCE-FALLBACK]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 272 | `eval.bc.compiler.compr` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr [ZERO]` | 0.00 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 273 | `eval.bc.compiler.def` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` | 0.00 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 274 | `eval.bc.compiler.expr` | `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` | 0.00 | 15/15 matched (target 16) | _none_ | 0/0 matched | _none_ | - | 0 | 1510.0 |
| 275 | `eval.runtime` | `eval.Runtime [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 276 | `eval.soft_error` | `eval.SoftError [ZERO]` | 0.00 | 1/1 matched | _none_ | 2/2 matched | _none_ | - | 0 | 310.0 |
| 277 | `fuzz_targets.starlark` | `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]` | 0.00 | 2/2 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 278 | `heap.allocator` | `heap.Allocator [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 279 | `heap.branding` | `heap.Branding [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 280 | `layout.avalues` | `layout.AValues [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 281 | `layout.static_string` | `layout.StaticString [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched (target 2) | _none_ | - | 0 | 610.0 |
| 282 | `lib` | `starlark.Lib [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 283 | `macros` | `starlark.Macros [ZERO]` | 0.00 | 0/0 matched (target 9) | _none_ | 0/0 matched (target 9) | _none_ | - | 0 | 10.0 |
| 284 | `pagable` | `starlark.Pagable [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 285 | `pagable.vtable_register` | `pagable.VtableRegister [ZERO]` | 0.00 | 0/0 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 286 | `runtime.params` | `runtime.Params [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 287 | `runtime.profile` | `runtime.Profile [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 288 | `runtime.slots` | `runtime.Slots [ZERO]` | 0.00 | 2/2 matched (target 3) | _none_ | 3/3 matched | _none_ | - | 0 | 510.0 |
| 289 | `stdlib.funcs` | `stdlib.Funcs [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 290 | `string.globals` | `string.Globals [ZERO]` | 0.00 | 5/5 matched | _none_ | 0/0 matched | _none_ | - | 0 | 510.0 |
| 291 | `string.intern` | `string.Intern [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 292 | `string.iter` | `string.Iter [ZERO]` | 0.00 | 3/3 matched (target 5) | _none_ | 1/1 matched | _none_ | - | 0 | 410.0 |
| 293 | `syntax` | `starlark.Syntax [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 294 | `type_compiled.matchers` | `type_compiled.Matchers [ZERO]` | 0.00 | 3/3 matched (target 25) | _none_ | 23/23 matched | _none_ | - | 0 | 2610.0 |
| 295 | `types.known_methods` | `types.KnownMethods [ZERO]` | 0.00 | 5/5 matched | _none_ | 2/2 matched | _none_ | - | 0 | 710.0 |
| 296 | `types.structs` | `types.Structs [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 297 | `types.tuple` | `types.Tuple [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 298 | `typing.call_args` | `typing.CallArgs [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 110.0 |
| 299 | `typing.error` | `typing.Error [ZERO]` | 0.00 | 9/9 matched (target 25) | _none_ | 5/5 matched (target 10) | _none_ | - | 0 | 1410.0 |
| 300 | `typing.fill_types_for_lint` | `typing.FillTypesForLint [ZERO]` | 0.00 | 39/39 matched (target 40) | _none_ | 3/3 matched | _none_ | - | 0 | 4210.0 |
| 301 | `typing.mode` | `typing.Mode [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 110.0 |
| 302 | `typing.oracle` | `typing.Oracle [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 303 | `unused_loads.remove` | `unusedloads.Remove [ZERO]` | 0.00 | 4/4 matched | _none_ | 1/1 matched | _none_ | - | 0 | 510.0 |
| 304 | `util` | `starlark.Util [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 305 | `values` | `values.Values [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 306 | `values.comparison` | `values.Comparison [ZERO]` | 0.00 | 5/5 matched | _none_ | 0/0 matched | _none_ | - | 0 | 510.0 |
| 307 | `values.types` | `values.Types [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 308 | `values.typing` | `values.Typing [STUB]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 309 | `wasm` | `starlark.Wasm [ZERO]` | 0.00 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 110.0 |
| 310 | `types.type_instance_id` | `types.TypeInstanceId` | 0.00 | 0/1 matched (target 2) | `r#gen` | 1/1 matched | _none_ | - | 1 | 9010210.0 |
| 311 | `tuple.rust_tuple` | `tuple.RustTuple` | 0.00 | 0/4 matched (target 11) | `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl` | 0/2 matched (target 0) | `Canonical`, `Error` | - | 6 | 60610.0 |
| 312 | `bool.unpack` | `bool.Unpack` | 0.00 | 0/1 matched | `unpack_value_impl` | 0/1 matched (target 0) | `Error` | - | 2 | 20210.0 |
| 313 | `enumeration.ty_enum_type` | `enumeration.TyEnumType` | 0.00 | 0/2 matched (target 3) | `eq`, `hash` | 1/1 matched | _none_ | - | 2 | 20310.0 |
| 314 | `pagable.error` | `pagable.Error` | 0.00 | 0/1 matched | `from` | 1/1 matched (target 2) | _none_ | - | 1 | 10210.0 |
| 315 | `runtime.visit_span` | `runtime.VisitSpan` | 0.00 | 0/1 matched (target 19) | `visit_spans` | 1/1 matched | _none_ | - | 1 | 10210.0 |
| 316 | `stdlib.json` | `stdlib.Json` | 0.04 | 2/11 matched (target 24) | `alloc_value`, `alloc_frozen_value`, `json`, `encode`, `decode`, `test_json_encode`, `test_json_decode`, `test_json_very_large_int`, `test_json_128bit_and_beyond` | 0/1 matched (target 11) | `Canonical` | 0/4 | 10 | 101209.6 |
| 317 | `analysis` | `starlark.Analysis` | 0.05 | 1/12 matched (target 1) | `module`, `test_lint_suppressions_keyword_matching`, `test_lint_suppressions_fn_with_many_issues`, `test_lint_suppressions_preceding_whitespace`, `test_lint_suppressions_with_space_separator`, `test_lint_suppressions_multiline_span`, `test_lint_suppressions_small_span`, `test_lint_suppressions_data`, `test_lint_suppressions_line_before`, `test_lint_suppressions_line_before_windows_newlines`, `test_lint_suppressions_inside_fn` | 1/1 matched | _none_ | 0/11 | 11 | 111309.5 |
| 318 | `bool.globals` | `bool.Globals` | 0.23 | 1/2 matched (target 1) | `bool` | 0/0 matched | _none_ | - | 1 | 10207.7 |
| 319 | `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 0.25 | 3/10 matched | `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter` | 2/5 matched (target 4) | `Target`, `Item`, `IntoIter` | - | 10 | 101507.5 |
| 320 | `tuple.globals` | `tuple.Globals` | 0.29 | 1/2 matched (target 1) | `tuple` | 0/0 matched | _none_ | - | 1 | 10207.1 |
| 321 | `analysis.types` | `analysis.Types` | 0.30 | 4/7 matched | `fmt`, `new`, `from` | 2/5 matched (target 2) | `LintWarning`, `LintT`, `EvalSeverity` | - | 6 | 61207.0 |
| 322 | `typing.small_arc_vec` | `typing.SmallArcVec` | 0.31 | 4/11 matched (target 16) | `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter` | 2/3 matched (target 5) | `Target` | - | 8 | 81406.9 |
| 323 | `num.value` | `num.Value` | 0.31 | 11/22 matched (target 25) | `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul`, `test_from_value`, `test_conversion_to_float`, `test_conversion_to_int`, `test_hashing`, `test_eq` | 3/4 matched (target 6) | `Output` | 0/5 | 12 | 122606.9 |
| 324 | `num.globals` | `num.Globals` | 0.32 | 1/2 matched (target 1) | `abs` | 0/0 matched | _none_ | - | 1 | 10206.8 |
| 325 | `util.refcell` | `refcell.RefCell` | 0.32 | 1/2 matched (target 11) | `test_unleak_borrow` | 0/0 matched (target 3) | _none_ | 0/1 | 1 | 20010206.0 |
| 326 | `dict.traits` | `dict.Traits` | 0.33 | 4/4 matched (target 7) | _none_ | 0/2 matched (target 6) | `Canonical`, `Error` | - | 2 | 20606.7 |
| 327 | `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 0.34 | 1/2 matched (target 1) | `drop` | 0/1 matched (target 0) | `WriteRemOnDrop` | - | 2 | 20306.6 |
| 328 | `collections.aligned_padded_str` | `alignedpaddedstr.AlignedPaddedStr` | 0.34 | 2/3 matched (target 4) | `eq` | 1/1 matched | _none_ | - | 1 | 2010406.6 |
| 329 | `values.demand` | `values.Demand` | 0.37 | 4/7 matched (target 5) | `payload`, `provide`, `test_trait_downcast` | 1/4 matched (target 1) | `SomeTrait`, `StaticType`, `MyValue` | 0/3 | 6 | 4061106.2 |
| 330 | `list.list_type` | `list.ListType` | 0.37 | 1/2 matched (target 5) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 3030506.2 |
| 331 | `string.dot_format` | `string.DotFormat` | 0.43 | 7/11 matched (target 7) | `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one` | 1/1 matched | _none_ | 0/4 | 4 | 1041205.7 |
| 332 | `analysis.underscore` | `analysis.Underscore` | 0.44 | 8/13 matched (target 17) | `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored` | 1/1 matched (target 3) | _none_ | 0/4 | 5 | 51405.6 |
| 333 | `analysis.performance` | `analysis.Performance` | 0.45 | 6/10 matched (target 14) | `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function` | 1/1 matched (target 4) | _none_ | 0/3 | 4 | 41105.5 |
| 334 | `stdlib.breakpoint` | `stdlib.Breakpoint` | 0.45 | 11/17 matched (target 13) | `global`, `breakpoint`, `reset_global_state`, `test_breakpoint_real`, `test_breakpoint_mock`, `test_breakpoint_disabled` | 5/6 matched | `Handler` | 0/4 | 7 | 1072305.5 |
| 335 | `analysis.names` | `analysis.Names` | 0.47 | 21/35 matched (target 31) | `new`, `ident`, `assign_ident`, `lint`, `about`, `test_lint_unused`, `test_lint_duplicate_assign`, `test_lint_unassigned`, `test_lint_undefined`, `test_early_fail`, `test_assign_for_next`, `test_flow_control`, `test_lambda_capture`, `test_global_defined_later` | 7/8 matched (target 13) | `AstStrExt` | 0/10 | 15 | 154305.3 |
| 336 | `analysis.dubious` | `analysis.Dubious` | 0.48 | 7/12 matched (target 19) | `lint`, `module`, `about`, `test_lint_duplicate_keys`, `test_lint_identifier_as_statement` | 1/2 matched (target 8) | `Key` | 0/4 | 6 | 61405.2 |
| 337 | `environment.module_dump` | `environment.ModuleDump` | 0.48 | 1/1 matched (target 2) | _none_ | 0/0 matched | _none_ | - | 0 | 105.2 |
| 338 | `bool.value` | `bool.Value` | 0.49 | 8/9 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 11005.1 |
| 339 | `tuple.alloc` | `tuple.Alloc` | 0.49 | 3/5 matched (target 3) | `test_alloc_tuple`, `test_alloc_frozen_tuple` | 1/2 matched (target 1) | `Canonical` | 0/2 | 3 | 30705.1 |
| 340 | `namespace.value` | `namespace.Value` | 0.50 | 10/15 matched (target 13) | `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_kwargs` | 2/2 matched (target 4) | _none_ | 0/4 | 5 | 51705.0 |
| 341 | `bc.native_function` | `bc.NativeFunction` | 0.51 | 3/4 matched | `fun` | 1/1 matched | _none_ | - | 1 | 4010505.0 |
| 342 | `analysis.flow` | `analysis.Flow` | 0.54 | 16/24 matched (target 30) | `lint`, `module`, `about`, `test_lint_returns`, `test_lint_unreachable`, `test_lint_redundant`, `test_lint_misplaced_load`, `test_lint_no_effect` | 1/1 matched (target 11) | _none_ | 0/7 | 8 | 82504.6 |
| 343 | `types.ellipsis` | `types.Ellipsis` | 0.55 | 2/3 matched (target 4) | `test_ellipsis` | 1/1 matched | _none_ | 0/1 | 1 | 4010404.5 |
| 344 | `analysis.find_call_name` | `analysis.FindCallName` | 0.55 | 2/3 matched (target 8) | `finds_function_calls_with_name_kwarg` | 1/1 matched | _none_ | 0/1 | 1 | 10404.5 |
| 345 | `dict.unpack` | `dict.Unpack` | 0.55 | 2/3 matched | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 30604.5 |
| 346 | `profile.data` | `profile.Data` | 0.55 | 4/6 matched (target 5) | `_assert_profile_data_send_sync`, `_assert_send_sync` | 3/3 matched (target 18) | _none_ | - | 2 | 20904.5 |
| 347 | `typing.callable_param` | `typing.CallableParam` | 0.57 | 16/20 matched (target 27) | `fmt`, `pf`, `new_named_only`, `test_param_spec_display` | 5/6 matched (target 10) | `ParamSpecDisplay` | 0/1 | 5 | 52604.3 |
| 348 | `analysis.incompatible` | `analysis.Incompatible` | 0.59 | 10/14 matched (target 17) | `lint`, `module`, `test_lint_incompatible`, `test_lint_duplicate_top_level_assign` | 1/1 matched (target 3) | _none_ | 0/3 | 4 | 41504.1 |
| 349 | `oracle.traits` | `oracle.Traits` | 0.60 | 1/1 matched (target 3) | _none_ | 2/2 matched | _none_ | - | 0 | 304.0 |
| 350 | `values.starlark_type_id` | `values.StarlarkTypeId` | 0.61 | 5/6 matched (target 7) | `eq` | 2/2 matched | _none_ | - | 1 | 7010804.0 |
| 351 | `values.error` | `values.Error` | 0.62 | 4/5 matched | `from` | 2/2 matched (target 20) | _none_ | - | 1 | 17010704.0 |
| 352 | `runtime.frame_span` | `runtime.FrameSpan` | 0.65 | 3/4 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 26010504.0 |
| 353 | `compiler.call` | `compiler.Call` | 0.67 | 13/13 matched (target 14) | _none_ | 1/1 matched | _none_ | - | 0 | 1403.3 |
| 354 | `layout.vtable` | `layout.Vtable` | 0.68 | 61/67 matched (target 65) | `value_ptr`, `drop_in_place`, `fmt`, `as_allocative`, `total_memory_for_profile`, `as_serialize` | 4/6 matched (target 4) | `GetTypeId`, `GetAllocativeKey` | - | 8 | 87303.2 |
| 355 | `types.any` | `types.Any` | 0.70 | 4/5 matched | `fmt` | 1/2 matched (target 1) | `Canonical` | - | 2 | 20703.0 |
| 356 | `compiler.def_inline` | `compiler.DefInline` | 0.70 | 9/10 matched (target 9) | `new` | 4/4 matched (target 6) | _none_ | - | 1 | 11403.0 |
| 357 | `none.globals` | `none.Globals` | 0.71 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 102.9 |
| 358 | `compiler.opt_ctx` | `compiler.OptCtx` | 0.71 | 5/5 matched (target 13) | _none_ | 2/2 matched (target 4) | _none_ | - | 0 | 7000703.0 |
| 359 | `layout.value_not_special` | `layout.ValueNotSpecial` | 0.72 | 6/6 matched (target 7) | _none_ | 1/1 matched | _none_ | - | 0 | 702.8 |
| 360 | `typing.basic` | `typing.Basic` | 0.72 | 18/19 matched (target 20) | `fmt` | 1/1 matched (target 11) | _none_ | - | 1 | 12002.8 |
| 361 | `__derive_refs.parse_args` | `deriverefs.ParseArgs [PROVENANCE-FALLBACK]` | 0.72 | 8/8 matched | _none_ | 0/0 matched | _none_ | - | 0 | 802.8 |
| 362 | `none.none_or` | `none.NoneOr` | 0.73 | 7/7 matched (target 9) | _none_ | 1/3 matched (target 4) | `Canonical`, `Error` | - | 2 | 6021002.5 |
| 363 | `typing.globals` | `typing.Globals` | 0.74 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 102.6 |
| 364 | `compiler.compr` | `compiler.Compr` | 0.75 | 9/9 matched (target 12) | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 1202.5 |
| 365 | `params.display` | `params.Display` | 0.75 | 4/4 matched | _none_ | 3/3 matched (target 8) | _none_ | - | 0 | 76000704.0 |
| 366 | `typing.ctx` | `typing.Ctx` | 0.76 | 19/19 matched (target 20) | _none_ | 1/1 matched (target 2) | _none_ | - | 0 | 2002.4 |
| 367 | `compiler.known` | `compiler.Known` | 0.78 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 102.2 |
| 368 | `unused_loads.find` | `unusedloads.Find` | 0.79 | 4/4 matched (target 8) | _none_ | 3/3 matched | _none_ | - | 0 | 702.1 |
| 369 | `oracle.ctx` | `oracle.Ctx` | 0.79 | 32/32 matched | _none_ | 2/2 matched (target 14) | _none_ | - | 0 | 3402.1 |
| 370 | `typing.macro_refs` | `typing.MacroRefs` | 0.80 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 202.0 |
| 371 | `funcs.zip` | `funcs.Zip` | 0.80 | 4/4 matched (target 7) | _none_ | 1/1 matched | _none_ | - | 0 | 502.0 |
| 372 | `enumeration.matcher` | `enumeration.Matcher` | 0.82 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 201.8 |
| 373 | `bc.instr_impl` | `bc.InstrImpl` | 0.83 | 7/7 matched (target 97) | _none_ | 87/163 matched (target 104) | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` | - | 76 | 777001.7 |
| 374 | `eval` | `eval.Eval` | 0.84 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 201.6 |
| 375 | `typing.macro_support` | `typing.MacroSupport` | 0.85 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 201.5 |
| 376 | `runtime.before_stmt` | `runtime.BeforeStmt` | 0.86 | 4/4 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 701.4 |
| 377 | `layout.identity` | `layout.Identity` | 0.87 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 201.3 |
| 378 | `type_compiled.alloc` | `type_compiled.Alloc` | 0.90 | 28/28 matched (target 37) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 2901.0 |
| 379 | `num.typecheck` | `num.Typecheck` | 0.90 | 2/2 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 501.0 |
| 380 | `eval.params` | `eval.Params` | 0.91 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 100.9 |
| 381 | `compiler.span` | `compiler.Span` | 0.92 | 2/2 matched | _none_ | 1/2 matched (target 1) | `Target` | - | 1 | 29010400.0 |
| 382 | `record.matcher` | `record.Matcher` | 0.92 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 200.8 |
| 383 | `type_compiled.factory` | `type_compiled.Factory` | 0.93 | 9/9 matched | _none_ | 1/2 matched (target 1) | `Result` | - | 1 | 11100.7 |
| 384 | `bool.alloc` | `bool.Alloc` | 0.95 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 200.5 |
| 385 | `funcs.globals` | `funcs.Globals` | 0.99 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 100.1 |
| 386 | `debug` | `debug.Debug [STUB] [PROVENANCE-FALLBACK]` | 1.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 53000000.0 |
| 387 | `bc.slow_arg` | `bc.SlowArg` | 1.00 | 0/0 matched | _none_ | 2/2 matched | _none_ | - | 0 | 200.0 |
| 388 | `collections` | `collections.Collections [STUB] [PROVENANCE-FALLBACK]` | 1.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 0.0 |
| 389 | `heap.profile` | `profile.Profile [STUB]` | 1.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 0.0 |
| 390 | `profile.or_instrumentation` | `profile.OrInstrumentation` | 1.00 | 0/0 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 100.0 |

## Cheat Detection / Scoring Failures

- `layout.value` -> `layout.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `val_ref` in Kotlin comments; Value.kt: Rust `fn` declaration in Kotlin comments; Value.kt: Rust `let` binding in Kotlin comments; Value.kt: Rust `pub` item in Kotlin comments; Value.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Value.kt: unchecked cast suppression hiding transliteration work in Kotlin code
- `typing.ty` -> `typing.Ty [ZERO]`: function-by-function score forced to 0. Ty.kt: snake_case identifier `of_value` in Kotlin comments; Ty.kt: Rust `fn` declaration in Kotlin comments; Ty.kt: Rust `pub` item in Kotlin comments; Ty.kt: Rust attribute syntax in Kotlin comments; Ty.kt: Rust lifetime explanation in Kotlin comments
- `layout.heap` -> `heap.Heap [STUB]`: function-by-function score forced to 0. Heap.kt: snake_case identifier `call_enter_exit` in Kotlin comments; Heap.kt: Rust `pub` item in Kotlin comments; Heap.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.starlark_value` -> `typing.StarlarkValue [ZERO]`: function-by-function score forced to 0. StarlarkValue.kt: snake_case identifier `starlark_type_id` in Kotlin comments; StarlarkValue.kt: Rust `fn` declaration in Kotlin comments; StarlarkValue.kt: Rust `impl` block in Kotlin comments; StarlarkValue.kt: Rust lifetime explanation in Kotlin comments
- `runtime.evaluator` -> `runtime.Evaluator [ZERO]`: function-by-function score forced to 0. Evaluator.kt: snake_case identifier `before_stmt` in Kotlin comments; Evaluator.kt: Rust lifetime explanation in Kotlin comments
- `values.trace` -> `values.Trace [ZERO]`: function-by-function score forced to 0. Trace.kt: Rust lifetime explanation in Kotlin comments; Trace.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.freeze` -> `values.Freeze [ZERO]`: function-by-function score forced to 0. Freeze.kt: Rust lifetime explanation in Kotlin comments; Freeze.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.alloc_value` -> `values.AllocValue [ZERO]`: function-by-function score forced to 0. AllocValue.kt: snake_case identifier `alloc_simple` in Kotlin comments; AllocValue.kt: Rust `fn` declaration in Kotlin comments; AllocValue.kt: Rust attribute syntax in Kotlin comments; AllocValue.kt: Rust `use` path in Kotlin comments; AllocValue.kt: Rust lifetime explanation in Kotlin comments
- `layout.freezer` -> `layout.Freezer [ZERO]`: function-by-function score forced to 0. Freezer.kt: snake_case identifier `frozen_defs` in Kotlin comments; Freezer.kt: Rust `fn` declaration in Kotlin comments; Freezer.kt: Rust `pub` item in Kotlin comments; Freezer.kt: Rust macro invocation in Kotlin comments; Freezer.kt: Rust lifetime explanation in Kotlin comments
- `coerce` -> `starlark.Coerce [ZERO]`: function-by-function score forced to 0. Coerce.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.frozen_ref` -> `values.FrozenRef [ZERO]`: function-by-function score forced to 0. FrozenRef.kt: Rust `fn` declaration in Kotlin comments; FrozenRef.kt: score-padding suppression annotation `@Suppress` in Kotlin code; FrozenRef.kt: Rust lifetime explanation in Kotlin comments; FrozenRef.kt: Rust-only type/unsafe terminology in Kotlin comments
- `none.none_type` -> `none.NoneType [ZERO]`: function-by-function score forced to 0. NoneType.kt: snake_case identifier `HAS_eval_type` in Kotlin code; NoneType.kt: snake_case identifier `derive_more` in Kotlin comments; NoneType.kt: Rust `fn` declaration in Kotlin comments; NoneType.kt: Rust `pub` item in Kotlin comments; NoneType.kt: Rust lifetime explanation in Kotlin comments
- `runtime.arguments` -> `runtime.Arguments [ZERO]`: function-by-function score forced to 0. Arguments.kt: snake_case identifier `to_string` in Kotlin comments; Arguments.kt: Rust `fn` declaration in Kotlin comments; Arguments.kt: Rust `pub` item in Kotlin comments; Arguments.kt: Rust `impl` block in Kotlin comments; Arguments.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Arguments.kt: Rust lifetime explanation in Kotlin comments
- `typing.type_compiled` -> `type_compiled.TypeCompiled [STUB]`: function-by-function score forced to 0. TypeCompiled.kt: snake_case identifier `type_matcher_factory` in Kotlin comments; TypeCompiled.kt: Rust `pub` item in Kotlin comments; TypeCompiled.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `environment.globals` -> `environment.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `derive.module` -> `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. AstModule.kt: snake_case identifier `into_parts` in Kotlin comments; AstModule.kt: Rust `fn` declaration in Kotlin comments; AstModule.kt: Rust `pub` item in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `values.value_of_unchecked` -> `values.ValueOfUnchecked [ZERO]`: function-by-function score forced to 0. ValueOfUnchecked.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; ValueOfUnchecked.kt: Rust `fn` declaration in Kotlin comments; ValueOfUnchecked.kt: score-padding suppression annotation `@Suppress` in Kotlin code; ValueOfUnchecked.kt: Rust lifetime explanation in Kotlin comments
- `__derive_refs.param_spec` -> `deriverefs.ParamSpec [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. ParamSpec.kt: snake_case identifier `param_ty` in Kotlin comments; ParamSpec.kt: Rust `fn` declaration in Kotlin comments; ParamSpec.kt: Rust `pub` item in Kotlin comments; ParamSpec.kt: Rust lifetime explanation in Kotlin comments
- `environment.methods` -> `environment.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.iter` -> `values.Iter [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `get_ref` in Kotlin comments; Iter.kt: Rust `fn` declaration in Kotlin comments; Iter.kt: Rust `let` binding in Kotlin comments; Iter.kt: Rust `pub` item in Kotlin comments; Iter.kt: Rust lifetime explanation in Kotlin comments; Iter.kt: Rust-only type/unsafe terminology in Kotlin comments
- `collections.symbol` -> `collections.Symbol [STUB]`: function-by-function score forced to 0. Symbol.kt: Rust `pub` item in Kotlin comments; Symbol.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `private` -> `starlark.Private [ZERO]`: function-by-function score forced to 0. Private.kt: snake_case identifier `missing_docs` in Kotlin comments
- `layout.avalue` -> `layout.AValue [ZERO]`: function-by-function score forced to 0. AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]`: function-by-function score forced to 0. ConstFrozenString.kt: snake_case identifier `macro_export` in Kotlin comments; ConstFrozenString.kt: Rust `macro_rules!` in Kotlin comments; ConstFrozenString.kt: Rust attribute syntax in Kotlin comments; ConstFrozenString.kt: Rust-only type/unsafe terminology in Kotlin comments
- `typing.tuple` -> `typing.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `item_ty` in Kotlin comments; Tuple.kt: Rust `fn` declaration in Kotlin comments; Tuple.kt: Rust `pub` item in Kotlin comments; Tuple.kt: Rust-only type/unsafe terminology in Kotlin comments
- `layout.value_lifetimeless` -> `layout.ValueLifetimeless [ZERO]`: function-by-function score forced to 0. ValueLifetimeless.kt: Rust `pub` item in Kotlin comments; ValueLifetimeless.kt: Rust `use` path in Kotlin comments
- `types.dict` -> `types.Dict [STUB]`: function-by-function score forced to 0. Dict.kt: snake_case identifier `dict_type` in Kotlin comments; Dict.kt: Rust `pub` item in Kotlin comments; Dict.kt: Rust `use` path in Kotlin comments
- `int.inline_int` -> `int.InlineInt [ZERO]`: function-by-function score forced to 0. InlineInt.kt: snake_case identifier `derive_more` in Kotlin comments; InlineInt.kt: Rust `fn` declaration in Kotlin comments; InlineInt.kt: Rust `pub` item in Kotlin comments; InlineInt.kt: Rust attribute syntax in Kotlin comments; InlineInt.kt: score-padding suppression annotation `@Suppress` in Kotlin code; InlineInt.kt: Rust lifetime explanation in Kotlin comments
- `int.pointer_i32` -> `int.PointerI32 [ZERO]`: function-by-function score forced to 0. PointerI32.kt: snake_case identifier `HAS_equals` in Kotlin code; PointerI32.kt: snake_case identifier `from_raw_pointer_unchecked` in Kotlin comments; PointerI32.kt: Rust `fn` declaration in Kotlin comments; PointerI32.kt: Rust `pub` item in Kotlin comments; PointerI32.kt: score-padding suppression annotation `@Suppress` in Kotlin code; PointerI32.kt: Rust lifetime explanation in Kotlin comments
- `any` -> `starlark.Any [ZERO]`: function-by-function score forced to 0. Any.kt: snake_case identifier `starlark_derive` in Kotlin comments; Any.kt: Rust `fn` declaration in Kotlin comments; Any.kt: Rust `pub` item in Kotlin comments; Any.kt: Rust `macro_rules!` in Kotlin comments; Any.kt: Rust lifetime explanation in Kotlin comments; Any.kt: Rust-only type/unsafe terminology in Kotlin comments
- `layout.aligned_size` -> `layout.AlignedSize [ZERO]`: function-by-function score forced to 0. AlignedSize.kt: snake_case identifier `derive_more` in Kotlin comments; AlignedSize.kt: Rust `fn` declaration in Kotlin comments; AlignedSize.kt: Rust `pub` item in Kotlin comments; AlignedSize.kt: Rust attribute syntax in Kotlin comments; AlignedSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments; AlignedSize.kt: Rust-only type/unsafe terminology in Kotlin comments
- `cast` -> `starlark.Cast [ZERO]`: function-by-function score forced to 0. Cast.kt: Rust lifetime explanation in Kotlin comments; Cast.kt: Rust-only type/unsafe terminology in Kotlin comments
- `eval.compiler` -> `eval.Compiler [ZERO]`: function-by-function score forced to 0. Compiler.kt: snake_case identifier `def_inline` in Kotlin comments; Compiler.kt: Rust `fn` declaration in Kotlin comments; Compiler.kt: Rust `pub` item in Kotlin comments; Compiler.kt: Rust attribute syntax in Kotlin comments; Compiler.kt: Rust lifetime explanation in Kotlin comments
- `types.bigint` -> `types.Bigint [ZERO]`: function-by-function score forced to 0. Bigint.kt: snake_case identifier `HAS_equals` in Kotlin code; Bigint.kt: snake_case identifier `non_zero_int` in Kotlin comments; Bigint.kt: Rust attribute syntax in Kotlin comments; Bigint.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Bigint.kt: Rust lifetime explanation in Kotlin comments
- `runtime.frozen_file_span` -> `runtime.FrozenFileSpan [ZERO]`: function-by-function score forced to 0. FrozenFileSpan.kt: snake_case identifier `empty_static` in Kotlin comments; FrozenFileSpan.kt: Rust `pub` item in Kotlin comments; FrozenFileSpan.kt: Rust attribute syntax in Kotlin comments; FrozenFileSpan.kt: Rust lifetime explanation in Kotlin comments
- `type_compiled.type_matcher_factory` -> `type_compiled.TypeMatcherFactory [ZERO]`: function-by-function score forced to 0. TypeMatcherFactory.kt: snake_case identifier `matcher_box` in Kotlin comments; TypeMatcherFactory.kt: Rust `fn` declaration in Kotlin comments; TypeMatcherFactory.kt: Rust `pub` item in Kotlin comments; TypeMatcherFactory.kt: Rust attribute syntax in Kotlin comments; TypeMatcherFactory.kt: Rust lifetime explanation in Kotlin comments; TypeMatcherFactory.kt: Rust auto-trait terminology in Kotlin comments
- `runtime.small_duration` -> `runtime.SmallDuration [ZERO]`: function-by-function score forced to 0. SmallDuration.kt: snake_case identifier `from_duration` in Kotlin comments; SmallDuration.kt: Rust `fn` declaration in Kotlin comments; SmallDuration.kt: Rust `pub` item in Kotlin comments; SmallDuration.kt: Rust attribute syntax in Kotlin comments; SmallDuration.kt: translator-note comment (`Kotlin:`) in Kotlin comments; SmallDuration.kt: Rust lifetime explanation in Kotlin comments
- `dict.dict_type` -> `dict.DictType [ZERO]`: function-by-function score forced to 0. DictType.kt: snake_case identifier `type_repr` in Kotlin comments; DictType.kt: Rust `fn` declaration in Kotlin comments; DictType.kt: Rust `pub` item in Kotlin comments; DictType.kt: Rust `use` path in Kotlin comments; DictType.kt: Rust lifetime explanation in Kotlin comments
- `typing.typecheck` -> `typing.Typecheck [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies
- `values.freeze_error` -> `values.FreezeError [ZERO]`: function-by-function score forced to 0. FreezeError.kt: snake_case identifier `starlark_syntax` in Kotlin comments
- `layout.value_alloc_size` -> `layout.ValueAllocSize [ZERO]`: function-by-function score forced to 0. ValueAllocSize.kt: snake_case identifier `try_new` in Kotlin comments; ValueAllocSize.kt: Rust `fn` declaration in Kotlin comments; ValueAllocSize.kt: Rust `pub` item in Kotlin comments; ValueAllocSize.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `compiler.stmt` -> `compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `build_file` in Kotlin comments; Stmt.kt: Rust `fn` declaration in Kotlin comments; Stmt.kt: Rust `pub` item in Kotlin comments; Stmt.kt: Rust attribute syntax in Kotlin comments; Stmt.kt: Rust lifetime explanation in Kotlin comments; Stmt.kt: Rust-only type/unsafe terminology in Kotlin comments
- `profile.profiler_type` -> `profile.ProfilerType [ZERO]`: function-by-function score forced to 0. ProfilerType.kt: snake_case identifier `data_from_generic` in Kotlin comments; ProfilerType.kt: Rust `fn` declaration in Kotlin comments; ProfilerType.kt: Rust `pub` item in Kotlin comments; ProfilerType.kt: Rust attribute syntax in Kotlin comments
- `values.layout` -> `values.Layout [STUB]`: function-by-function score forced to 0. Layout.kt: snake_case identifier `aligned_size` in Kotlin comments; Layout.kt: Rust `pub` item in Kotlin comments; Layout.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `tests.def` -> `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. DefParamIndices.kt: snake_case identifier `num_positional` in Kotlin comments; DefParamIndices.kt: Rust `fn` declaration in Kotlin comments; DefParamIndices.kt: Rust `pub` item in Kotlin comments; DefParamIndices.kt: Rust attribute syntax in Kotlin comments; DefParamIndices.kt: Rust-only type/unsafe terminology in Kotlin comments
- `types.array` -> `types.Array [ZERO]`: function-by-function score forced to 0. Array.kt: snake_case identifier `iter_count` in Kotlin comments; Array.kt: Rust `fn` declaration in Kotlin comments; Array.kt: Rust `pub` item in Kotlin comments; Array.kt: Rust attribute syntax in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Array.kt: Rust lifetime explanation in Kotlin comments
- `typing.arc_ty` -> `typing.ArcTy [ZERO]`: function-by-function score forced to 0. ArcTy.kt: snake_case identifier `derive_more` in Kotlin comments; ArcTy.kt: Rust `fn` declaration in Kotlin comments; ArcTy.kt: Rust `pub` item in Kotlin comments; ArcTy.kt: Rust `impl` block in Kotlin comments; ArcTy.kt: Rust lifetime explanation in Kotlin comments
- `eval.bc` -> `bc.Bc [STUB]`: function-by-function score forced to 0. Bc.kt: snake_case identifier `definitely_assigned` in Kotlin comments; Bc.kt: Rust `pub` item in Kotlin comments; Bc.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `scope.scope_resolver_globals` -> `scope.ScopeResolverGlobals [ZERO]`: function-by-function score forced to 0. ScopeResolverGlobals.kt: snake_case identifier `get_global` in Kotlin comments; ScopeResolverGlobals.kt: Rust `fn` declaration in Kotlin comments; ScopeResolverGlobals.kt: Rust `pub` item in Kotlin comments; ScopeResolverGlobals.kt: Rust lifetime explanation in Kotlin comments
- `types.range` -> `types.Range [STUB]`: function-by-function score forced to 0. Range.kt: snake_case identifier `range_type` in Kotlin comments; Range.kt: Rust `pub` item in Kotlin comments; Range.kt: Rust `use` path in Kotlin comments
- `typing.interface` -> `typing.Interface [ZERO]`: function-by-function score forced to 0. Interface.kt: Rust `fn` declaration in Kotlin comments; Interface.kt: Rust `pub` item in Kotlin comments; Interface.kt: Rust attribute syntax in Kotlin comments
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]`: function-by-function score forced to 0. EnumType.kt: snake_case identifier `HAS_invoke` in Kotlin code; EnumType.kt: Rust lifetime explanation in Kotlin comments
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]`: function-by-function score forced to 0. StarlarkValueAsType.kt: snake_case identifier `HAS_eval_type` in Kotlin code; StarlarkValueAsType.kt: snake_case identifier `starlark_derive` in Kotlin comments; StarlarkValueAsType.kt: Rust `fn` declaration in Kotlin comments; StarlarkValueAsType.kt: Rust `pub` item in Kotlin comments; StarlarkValueAsType.kt: Rust lifetime explanation in Kotlin comments
- `bc.frame` -> `bc.Frame [ZERO]`: function-by-function score forced to 0. Frame.kt: snake_case identifier `loop_indices` in Kotlin comments; Frame.kt: Rust `fn` declaration in Kotlin comments; Frame.kt: Rust `pub` item in Kotlin comments; Frame.kt: Rust attribute syntax in Kotlin comments; Frame.kt: Rust lifetime explanation in Kotlin comments; Frame.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.value_of` -> `values.ValueOf [ZERO]`: function-by-function score forced to 0. ValueOf.kt: snake_case identifier `starlark_module` in Kotlin comments
- `profile.alloc_counts` -> `profile.AllocCounts [ZERO]`: function-by-function score forced to 0. AllocCounts.kt: snake_case identifier `normalize_for_golden_tests` in Kotlin comments; AllocCounts.kt: Rust `fn` declaration in Kotlin comments; AllocCounts.kt: Rust `pub` item in Kotlin comments; AllocCounts.kt: Rust attribute syntax in Kotlin comments; AllocCounts.kt: Rust lifetime explanation in Kotlin comments; AllocCounts.kt: Rust-only type/unsafe terminology in Kotlin comments
- `record.record_type` -> `record.RecordType [ZERO]`: function-by-function score forced to 0. RecordType.kt: snake_case identifier `HAS_invoke` in Kotlin code; RecordType.kt: snake_case identifier `get_or_init_ty` in Kotlin comments; RecordType.kt: Rust `fn` declaration in Kotlin comments; RecordType.kt: Rust `pub` item in Kotlin comments; RecordType.kt: translator-note comment (`Kotlin:`) in Kotlin comments; RecordType.kt: Rust lifetime explanation in Kotlin comments
- `alloc.chunk` -> `alloc.Chunk [ZERO]`: function-by-function score forced to 0. Chunk.kt: snake_case identifier `ref_count` in Kotlin comments; Chunk.kt: Rust `fn` declaration in Kotlin comments; Chunk.kt: Rust `pub` item in Kotlin comments; Chunk.kt: Rust attribute syntax in Kotlin comments; Chunk.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Chunk.kt: Rust-only type/unsafe terminology in Kotlin comments
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. CallStack.kt: snake_case identifier `call_stack` in Kotlin comments; CallStack.kt: snake_case identifier `is_empty` in Kotlin comments; CallStack.kt: Rust `fn` declaration in Kotlin comments; CallStack.kt: Rust `pub` item in Kotlin comments; CallStack.kt: Rust attribute syntax in Kotlin comments
- `errors.did_you_mean` -> `errors.DidYouMean [ZERO]`: function-by-function score forced to 0. DidYouMean.kt: Rust `use` path in Kotlin comments; DidYouMean.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `list.alloc` -> `list.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; no target functions found; report scoring is function-by-function only
- `compiler.constants` -> `compiler.Constants [ZERO]`: function-by-function score forced to 0. Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust `fn` declaration in Kotlin comments; Constants.kt: Rust `pub` item in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments
- `profile.instant` -> `profile.Instant [ZERO]`: function-by-function score forced to 0. Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `fn` declaration in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust `pub` item in Kotlin comments
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]`: function-by-function score forced to 0. UnpackAndDiscard.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; UnpackAndDiscard.kt: Rust `fn` declaration in Kotlin comments; UnpackAndDiscard.kt: translator-note comment (`Kotlin:`) in Kotlin comments; UnpackAndDiscard.kt: Rust lifetime explanation in Kotlin comments
- `sealed` -> `starlark.Sealed [ZERO]`: function-by-function score forced to 0. Sealed.kt: Rust `pub` item in Kotlin comments; Sealed.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `types.namespace` -> `types.Namespace [STUB]`: function-by-function score forced to 0. Namespace.kt: Rust `pub` item in Kotlin comments; Namespace.kt: Rust `use` path in Kotlin comments
- `types.record` -> `types.Record [ZERO]`: function-by-function score forced to 0. Record.kt: snake_case identifier `record_type` in Kotlin comments; Record.kt: Rust `pub` item in Kotlin comments; Record.kt: Rust `use` path in Kotlin comments
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]`: function-by-function score forced to 0. SmallVec1.kt: snake_case identifier `as_slice` in Kotlin comments; SmallVec1.kt: Rust `fn` declaration in Kotlin comments; SmallVec1.kt: Rust `pub` item in Kotlin comments; SmallVec1.kt: Rust attribute syntax in Kotlin comments; SmallVec1.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `util.arc_or_static` -> `util.ArcOrStatic [ZERO]`: function-by-function score forced to 0. ArcOrStatic.kt: snake_case identifier `new_static` in Kotlin comments; ArcOrStatic.kt: Rust `fn` declaration in Kotlin comments; ArcOrStatic.kt: Rust `pub` item in Kotlin comments; ArcOrStatic.kt: Rust attribute syntax in Kotlin comments; ArcOrStatic.kt: Rust lifetime explanation in Kotlin comments
- `typing.type_type` -> `typing.TypeType [ZERO]`: function-by-function score forced to 0. TypeType.kt: Rust attribute syntax in Kotlin comments
- `alloc.chunk_part` -> `alloc.ChunkPart [ZERO]`: function-by-function score forced to 0. ChunkPart.kt: snake_case identifier `new_subslice` in Kotlin comments; ChunkPart.kt: Rust `fn` declaration in Kotlin comments; ChunkPart.kt: Rust `pub` item in Kotlin comments; ChunkPart.kt: Rust attribute syntax in Kotlin comments; ChunkPart.kt: Rust-only type/unsafe terminology in Kotlin comments
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]`: function-by-function score forced to 0. ConstTypeId.kt: snake_case identifier `const_type_id` in Kotlin comments; ConstTypeId.kt: Rust `fn` declaration in Kotlin comments; ConstTypeId.kt: Rust `pub` item in Kotlin comments; ConstTypeId.kt: Rust `impl` block in Kotlin comments; ConstTypeId.kt: translator-note comment (`Kotlin:`) in Kotlin comments; ConstTypeId.kt: Rust lifetime explanation in Kotlin comments
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]`: function-by-function score forced to 0. RustLoc.kt: snake_case identifier `macro_rules` in Kotlin comments; RustLoc.kt: Rust `macro_rules!` in Kotlin comments; RustLoc.kt: Rust attribute syntax in Kotlin comments; RustLoc.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]`: function-by-function score forced to 0. OwnedFrozenRef.kt: snake_case identifier `new_unchecked` in Kotlin comments; OwnedFrozenRef.kt: Rust `fn` declaration in Kotlin comments; OwnedFrozenRef.kt: Rust `pub` item in Kotlin comments; OwnedFrozenRef.kt: Rust attribute syntax in Kotlin comments; OwnedFrozenRef.kt: translator-note comment (`Kotlin:`) in Kotlin comments; OwnedFrozenRef.kt: Rust lifetime explanation in Kotlin comments
- `avalues.str_` -> `avalues.Str [ZERO]`: function-by-function score forced to 0. Str.kt: snake_case identifier `new_const` in Kotlin comments; Str.kt: Rust `fn` declaration in Kotlin comments; Str.kt: Rust `pub` item in Kotlin comments; Str.kt: Rust attribute syntax in Kotlin comments; Str.kt: Rust lifetime explanation in Kotlin comments; Str.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.stack_guard` -> `values.StackGuard [ZERO]`: function-by-function score forced to 0. StackGuard.kt: snake_case identifier `to_str` in Kotlin comments
- `collections.string_pool` -> `collections.StringPool [ZERO]`: function-by-function score forced to 0. StringPool.kt: snake_case identifier `unwrap_or_default` in Kotlin comments; StringPool.kt: Rust `fn` declaration in Kotlin comments; StringPool.kt: Rust `let` binding in Kotlin comments; StringPool.kt: Rust `pub` item in Kotlin comments; StringPool.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]`: function-by-function score forced to 0. LocalAsValue.kt: snake_case identifier `derive_more` in Kotlin comments; LocalAsValue.kt: Rust `fn` declaration in Kotlin comments; LocalAsValue.kt: Rust `pub` item in Kotlin comments; LocalAsValue.kt: Rust attribute syntax in Kotlin comments; LocalAsValue.kt: translator-note comment (`Kotlin:`) in Kotlin comments; LocalAsValue.kt: Rust lifetime explanation in Kotlin comments
- `hint` -> `starlark.Hint [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Hint.kt: snake_case identifier `rust_nightly` in Kotlin comments; Hint.kt: Rust `fn` declaration in Kotlin comments; Hint.kt: Rust `pub` item in Kotlin comments; Hint.kt: Rust attribute syntax in Kotlin comments
- `profile.string_index` -> `profile.StringIndex [ZERO]`: function-by-function score forced to 0. StringIndex.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `runtime.file_loader` -> `runtime.FileLoader [ZERO]`: function-by-function score forced to 0. FileLoader.kt: Rust `fn` declaration in Kotlin comments; FileLoader.kt: Rust `pub` item in Kotlin comments; FileLoader.kt: Rust attribute syntax in Kotlin comments; FileLoader.kt: Rust `use` path in Kotlin comments; FileLoader.kt: Rust lifetime explanation in Kotlin comments
- `types.float` -> `types.Float [STUB]`: function-by-function score forced to 0. Float.kt: Rust `pub` item in Kotlin comments; Float.kt: Rust `use` path in Kotlin comments
- `types.list` -> `types.List [STUB]`: function-by-function score forced to 0. List.kt: snake_case identifier `list_type` in Kotlin comments; List.kt: Rust `pub` item in Kotlin comments; List.kt: Rust `use` path in Kotlin comments
- `types.num` -> `types.Num [STUB]`: function-by-function score forced to 0. Num.kt: Rust `pub` item in Kotlin comments
- `values.thin_box_slice_frozen_value` -> `values.ThinBoxSliceFrozenValue [STUB]`: function-by-function score forced to 0. ThinBoxSliceFrozenValue.kt: snake_case identifier `packed_impl` in Kotlin comments; ThinBoxSliceFrozenValue.kt: Rust `pub` item in Kotlin comments; ThinBoxSliceFrozenValue.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.arena` -> `heap.Arena [ZERO]`: function-by-function score forced to 0. Arena.kt: snake_case identifier `HAS_invoke` in Kotlin code; Arena.kt: snake_case identifier `enter_bump` in Kotlin comments; Arena.kt: Rust `fn` declaration in Kotlin comments; Arena.kt: Rust `let` binding in Kotlin comments; Arena.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Arena.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `collections.alloca` -> `collections.Alloca [ZERO]`: function-by-function score forced to 0. Alloca.kt: snake_case identifier `size_of` in Kotlin comments; Alloca.kt: Rust `fn` declaration in Kotlin comments; Alloca.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Alloca.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Alloca.kt: Rust-only type/unsafe terminology in Kotlin comments
- `stdlib` -> `starlark.Stdlib [ZERO]`: function-by-function score forced to 0. Stdlib.kt: snake_case identifier `call_stack` in Kotlin comments; Stdlib.kt: Rust `fn` declaration in Kotlin comments; Stdlib.kt: Rust `pub` item in Kotlin comments; Stdlib.kt: Rust attribute syntax in Kotlin comments; Stdlib.kt: Rust lifetime explanation in Kotlin comments
- `string.interpolation` -> `string.Interpolation [ZERO]`: function-by-function score forced to 0. Interpolation.kt: snake_case identifier `string_pool` in Kotlin comments
- `types.list_or_tuple` -> `types.ListOrTuple [ZERO]`: function-by-function score forced to 0. ListOrTuple.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; ListOrTuple.kt: Rust `fn` declaration in Kotlin comments; ListOrTuple.kt: Rust `pub` item in Kotlin comments; ListOrTuple.kt: Rust attribute syntax in Kotlin comments; ListOrTuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; ListOrTuple.kt: Rust lifetime explanation in Kotlin comments
- `layout.pointer` -> `layout.Pointer [ZERO]`: function-by-function score forced to 0. Pointer.kt: snake_case identifier `get_user_tag` in Kotlin comments; Pointer.kt: Rust `fn` declaration in Kotlin comments; Pointer.kt: Rust `pub` item in Kotlin comments; Pointer.kt: Rust attribute syntax in Kotlin comments; Pointer.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Pointer.kt: Rust lifetime explanation in Kotlin comments
- `types.any_complex` -> `types.AnyComplex [ZERO]`: function-by-function score forced to 0. AnyComplex.kt: snake_case identifier `starlark_derive` in Kotlin comments; AnyComplex.kt: Rust `fn` declaration in Kotlin comments; AnyComplex.kt: Rust `pub` item in Kotlin comments; AnyComplex.kt: Rust attribute syntax in Kotlin comments; AnyComplex.kt: Rust lifetime explanation in Kotlin comments; AnyComplex.kt: Rust-only type/unsafe terminology in Kotlin comments
- `types.any_array` -> `types.AnyArray [ZERO]`: function-by-function score forced to 0. AnyArray.kt: snake_case identifier `starlark_derive` in Kotlin comments; AnyArray.kt: Rust `fn` declaration in Kotlin comments; AnyArray.kt: Rust `pub` item in Kotlin comments; AnyArray.kt: Rust attribute syntax in Kotlin comments; AnyArray.kt: score-padding suppression annotation `@Suppress` in Kotlin code; AnyArray.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `util.rtabort` -> `util.Rtabort [ZERO]`: function-by-function score forced to 0. Rtabort.kt: snake_case identifier `macro_rules` in Kotlin comments; Rtabort.kt: Rust `fn` declaration in Kotlin comments; Rtabort.kt: Rust `pub` item in Kotlin comments; Rtabort.kt: Rust `macro_rules!` in Kotlin comments; Rtabort.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `bc.if_debug` -> `bc.IfDebug [ZERO]`: function-by-function score forced to 0. IfDebug.kt: snake_case identifier `debug_assertions` in Kotlin comments; IfDebug.kt: Rust `fn` declaration in Kotlin comments; IfDebug.kt: Rust `pub` item in Kotlin comments; IfDebug.kt: Rust attribute syntax in Kotlin comments; IfDebug.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `util.non_static_type_id` -> `util.NonStaticTypeId [ZERO]`: function-by-function score forced to 0. NonStaticTypeId.kt: snake_case identifier `non_static_type_id` in Kotlin comments; NonStaticTypeId.kt: Rust `pub` item in Kotlin comments; NonStaticTypeId.kt: Rust attribute syntax in Kotlin comments; NonStaticTypeId.kt: Rust lifetime explanation in Kotlin comments; NonStaticTypeId.kt: Rust-only type/unsafe terminology in Kotlin comments
- `avalues.simple` -> `avalues.Simple [ZERO]`: function-by-function score forced to 0. Simple.kt: snake_case identifier `extra_len` in Kotlin comments; Simple.kt: Rust `fn` declaration in Kotlin comments; Simple.kt: Rust `pub` item in Kotlin comments; Simple.kt: Rust `impl` block in Kotlin comments; Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments
- `layout.value_captured` -> `layout.ValueCaptured [ZERO]`: function-by-function score forced to 0. ValueCaptured.kt: snake_case identifier `starlark_value` in Kotlin comments; ValueCaptured.kt: Rust `fn` declaration in Kotlin comments; ValueCaptured.kt: Rust `pub` item in Kotlin comments; ValueCaptured.kt: Rust attribute syntax in Kotlin comments; ValueCaptured.kt: Rust lifetime explanation in Kotlin comments; ValueCaptured.kt: Rust-only type/unsafe terminology in Kotlin comments
- `record.field` -> `record.Field [ZERO]`: function-by-function score forced to 0. Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments; Field.kt: Rust `fn` declaration in Kotlin comments; Field.kt: Rust `pub` item in Kotlin comments; Field.kt: Rust attribute syntax in Kotlin comments; Field.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Field.kt: Rust lifetime explanation in Kotlin comments
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]`: function-by-function score forced to 0. CheapCallStack.kt: snake_case identifier `extend_frames` in Kotlin comments; CheapCallStack.kt: Rust `fn` declaration in Kotlin comments; CheapCallStack.kt: Rust `pub` item in Kotlin comments; CheapCallStack.kt: Rust attribute syntax in Kotlin comments; CheapCallStack.kt: translator-note comment (`Kotlin:`) in Kotlin comments; CheapCallStack.kt: Rust lifetime explanation in Kotlin comments
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]`: function-by-function score forced to 0. UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments
- `heap.fast_cell` -> `heap.FastCell [ZERO]`: function-by-function score forced to 0. FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust `fn` declaration in Kotlin comments; FastCell.kt: Rust `pub` item in Kotlin comments; FastCell.kt: Rust attribute syntax in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments
- `read_line` -> `starlark.ReadLine [ZERO]`: function-by-function score forced to 0. ReadLine.kt: snake_case identifier `target_arch` in Kotlin comments; ReadLine.kt: Rust `fn` declaration in Kotlin comments; ReadLine.kt: Rust `pub` item in Kotlin comments; ReadLine.kt: Rust attribute syntax in Kotlin comments; ReadLine.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `typing.bindings` -> `typing.Bindings [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments
- `typing.structs` -> `typing.Structs [ZERO]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `type_matcher` in Kotlin comments; Structs.kt: Rust `fn` declaration in Kotlin comments; Structs.kt: Rust `pub` item in Kotlin comments; Structs.kt: Rust attribute syntax in Kotlin comments
- `analysis.lint_message` -> `analysis.LintMessage [ZERO]`: function-by-function score forced to 0. LintMessage.kt: Rust `fn` declaration in Kotlin comments; LintMessage.kt: Rust `pub` item in Kotlin comments
- `types.bool` -> `types.Bool [ZERO]`: function-by-function score forced to 0. Bool.kt: snake_case identifier `new_bool` in Kotlin comments; Bool.kt: Rust `pub` item in Kotlin comments; Bool.kt: Rust `use` path in Kotlin comments
- `types.enumeration` -> `types.Enumeration [STUB]`: function-by-function score forced to 0. Enumeration.kt: snake_case identifier `assert_eq` in Kotlin comments; Enumeration.kt: Rust `pub` item in Kotlin comments
- `types.int` -> `types.Int [ZERO]`: function-by-function score forced to 0. Int.kt: snake_case identifier `num_bigint` in Kotlin comments; Int.kt: Rust `pub` item in Kotlin comments; Int.kt: Rust `use` path in Kotlin comments
- `types.none` -> `types.None [STUB]`: function-by-function score forced to 0. None.kt: snake_case identifier `none_or` in Kotlin comments; None.kt: Rust `pub` item in Kotlin comments; None.kt: Rust `use` path in Kotlin comments
- `types.set` -> `types.Set [STUB]`: function-by-function score forced to 0. Set.kt: Rust `pub` item in Kotlin comments; Set.kt: Rust `use` path in Kotlin comments; SetModule.kt: Rust `pub` item in Kotlin comments
- `types.string` -> `types.String [STUB]`: function-by-function score forced to 0. String.kt: snake_case identifier `alloc_unpack` in Kotlin comments; String.kt: Rust `pub` item in Kotlin comments
- `typing` -> `starlark.Typing [STUB]`: function-by-function score forced to 0. Typing.kt: snake_case identifier `callable_param` in Kotlin comments; Typing.kt: Rust `pub` item in Kotlin comments; Typing.kt: Rust attribute syntax in Kotlin comments; Typing.kt: Rust `use` path in Kotlin comments; Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.function` -> `typing.Function [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Function.kt: snake_case identifier `is_type` in Kotlin comments; Function.kt: Rust `fn` declaration in Kotlin comments; Function.kt: Rust `pub` item in Kotlin comments; Function.kt: Rust attribute syntax in Kotlin comments; Function.kt: Rust lifetime explanation in Kotlin comments; Function.kt: Rust auto-trait terminology in Kotlin comments
- `set.methods` -> `set.Methods [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: Rust `fn` declaration in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `string.str_type` -> `string.StrType [ZERO]`: function-by-function score forced to 0. StrType.kt: snake_case identifier `starlark_value` in Kotlin comments; StrType.kt: Rust attribute syntax in Kotlin comments
- `int.int_or_big` -> `int.IntOrBig [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; IntOrBig.kt: snake_case identifier `from_str_radix` in Kotlin comments
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]`: function-by-function score forced to 0. ThinBox.kt: snake_case identifier `buck2_util` in Kotlin comments; ThinBox.kt: Rust `fn` declaration in Kotlin comments; ThinBox.kt: Rust `pub` item in Kotlin comments; ThinBox.kt: Rust-only type/unsafe terminology in Kotlin comments
- `set.value` -> `set.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust `fn` declaration in Kotlin comments; Value.kt: Rust `pub` item in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `values.typing.callable` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]`: function-by-function score forced to 0. Callable.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Callable.kt: snake_case identifier `starlark_value` in Kotlin comments; Callable.kt: Rust `fn` declaration in Kotlin comments; Callable.kt: Rust `pub` item in Kotlin comments; Callable.kt: Rust lifetime explanation in Kotlin comments
- `typing.user` -> `typing.User [ZERO]`: function-by-function score forced to 0. User.kt: snake_case identifier `derive_more` in Kotlin comments; User.kt: Rust `fn` declaration in Kotlin comments; User.kt: Rust `pub` item in Kotlin comments; User.kt: Rust attribute syntax in Kotlin comments
- `float.float` -> `float.Float [ZERO]`: function-by-function score forced to 0. Float.kt: snake_case identifier `HAS_equals` in Kotlin code; Float.kt: snake_case identifier `starlark_value` in Kotlin comments; Float.kt: Rust attribute syntax in Kotlin comments; Float.kt: Rust lifetime explanation in Kotlin comments
- `layout.typed` -> `layout.ValueTyped [ZERO]`: function-by-function score forced to 0. ValueTyped.kt: snake_case identifier `new_err` in Kotlin comments; ValueTyped.kt: Rust `fn` declaration in Kotlin comments; ValueTyped.kt: Rust `pub` item in Kotlin comments; ValueTyped.kt: score-padding suppression annotation `@Suppress` in Kotlin code; ValueTyped.kt: Rust lifetime explanation in Kotlin comments; ValueTyped.kt: Rust-only type/unsafe terminology in Kotlin comments
- `scope.payload` -> `scope.Payload [ZERO]`: function-by-function score forced to 0. Payload.kt: Rust `pub` item in Kotlin comments; Payload.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]`: function-by-function score forced to 0. PackedImpl.kt: Rust `fn` declaration in Kotlin comments; PackedImpl.kt: Rust `pub` item in Kotlin comments; PackedImpl.kt: Rust lifetime explanation in Kotlin comments; PackedImpl.kt: Rust-only type/unsafe terminology in Kotlin comments
- `string.repr` -> `string.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `is_printable` in Kotlin comments; Repr.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `list.value` -> `list.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust `fn` declaration in Kotlin comments; Value.kt: Rust `pub` item in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]`: function-by-function score forced to 0. VtableRegistry.kt: snake_case identifier `type_name` in Kotlin comments; VtableRegistry.kt: Rust lifetime explanation in Kotlin comments
- `stdlib.extra` -> `stdlib.Extra [ZERO]`: function-by-function score forced to 0. Extra.kt: Rust `fn` declaration in Kotlin comments
- `dict.value` -> `dict.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust `pub` item in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `record.globals` -> `record.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust `fn` declaration in Kotlin comments; Globals.kt: Rust `pub` item in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `alloc.chain` -> `alloc.Chain [ZERO]`: function-by-function score forced to 0. Chain.kt: Rust attribute syntax in Kotlin comments
- `heap.heap_type` -> `heap.HeapType [ZERO]`: function-by-function score forced to 0. HeapType.kt: snake_case identifier `into_ref` in Kotlin comments; HeapType.kt: score-padding suppression annotation `@Suppress` in Kotlin code; HeapType.kt: Rust lifetime explanation in Kotlin comments; HeapType.kt: Rust-only type/unsafe terminology in Kotlin comments
- `range.range_type` -> `range.RangeType [ZERO]`: function-by-function score forced to 0. RangeType.kt: snake_case identifier `HAS_iterate` in Kotlin code; RangeType.kt: snake_case identifier `saturating_mul` in Kotlin comments
- `stdlib.partial` -> `stdlib.Partial [ZERO]`: function-by-function score forced to 0. Partial.kt: snake_case identifier `HAS_invoke` in Kotlin code; Partial.kt: snake_case identifier `alloca_concat` in Kotlin comments; Partial.kt: Rust lifetime explanation in Kotlin comments
- `alloc.allocator` -> `alloc.Allocator [ZERO]`: function-by-function score forced to 0. Allocator.kt: Rust `pub` item in Kotlin comments; Allocator.kt: Rust lifetime explanation in Kotlin comments; Allocator.kt: Rust-only type/unsafe terminology in Kotlin comments
- `profile.bc` -> `profile.Bc [ZERO]`: function-by-function score forced to 0. Bc.kt: snake_case identifier `write_bc_profile` in Kotlin comments; Bc.kt: Rust `fn` declaration in Kotlin comments; Bc.kt: Rust `pub` item in Kotlin comments; Bc.kt: Rust attribute syntax in Kotlin comments; Bc.kt: Rust lifetime explanation in Kotlin comments
- `tuple.unpack` -> `tuple.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Unpack.kt: Rust `fn` declaration in Kotlin comments; Unpack.kt: Rust `pub` item in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]`: function-by-function score forced to 0. Compiled.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Compiled.kt: snake_case identifier `check_matches` in Kotlin comments
- `bigint.convert` -> `bigint.Convert [ZERO]`: function-by-function score forced to 0. Convert.kt: Rust-only type/unsafe terminology in Kotlin comments
- `dict.methods` -> `dict.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: Rust `fn` declaration in Kotlin comments; Methods.kt: Rust lifetime explanation in Kotlin comments
- `docs.parse` -> `docs.Parse [ZERO]`: function-by-function score forced to 0. Parse.kt: snake_case identifier `some_function` in Kotlin comments; Parse.kt: Rust `fn` declaration in Kotlin comments; Parse.kt: Rust `pub` item in Kotlin comments; Parse.kt: Rust attribute syntax in Kotlin comments
- `funcs.other` -> `funcs.Other [ZERO]`: function-by-function score forced to 0. Other.kt: snake_case identifier `starlark_module` in Kotlin comments; Other.kt: Rust `fn` declaration in Kotlin comments; Other.kt: Rust `pub` item in Kotlin comments; Other.kt: Rust attribute syntax in Kotlin comments; Other.kt: Rust lifetime explanation in Kotlin comments
- `layout.complex` -> `layout.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `new_err` in Kotlin comments; Complex.kt: Rust `fn` declaration in Kotlin comments; Complex.kt: Rust `pub` item in Kotlin comments; Complex.kt: Rust attribute syntax in Kotlin comments; Complex.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Complex.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `profile.aggregated` -> `profile.Aggregated [ZERO]`: function-by-function score forced to 0. Aggregated.kt: snake_case identifier `string_index` in Kotlin comments; Aggregated.kt: Rust `fn` declaration in Kotlin comments; Aggregated.kt: Rust `pub` item in Kotlin comments; Aggregated.kt: Rust attribute syntax in Kotlin comments
- `record.ty_record_type` -> `record.TyRecordType [ZERO]`: function-by-function score forced to 0. TyRecordType.kt: Rust `pub` item in Kotlin comments; TyRecordType.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `string.simd` -> `string.Simd [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Simd.kt: snake_case identifier `x86_64` in Kotlin comments; Simd.kt: snake_case identifier `find_hash_in_array_without_simd` in Kotlin comments
- `tuple.value` -> `tuple.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code
- `typed.string` -> `typed.String [ZERO]`: function-by-function score forced to 0. String.kt: snake_case identifier `HAS_equals` in Kotlin code; String.kt: snake_case identifier `as_str` in Kotlin comments; String.kt: Rust `fn` declaration in Kotlin comments; String.kt: Rust `pub` item in Kotlin comments; String.kt: Rust lifetime explanation in Kotlin comments; String.kt: Rust-only type/unsafe terminology in Kotlin comments
- `adapter.implementation` -> `adapter.Implementation [ZERO]`: function-by-function score forced to 0. Implementation.kt: snake_case identifier `before_stmt` in Kotlin comments
- `assert.assert` -> `assert.Assert [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Assert.kt: snake_case identifier `assert_eq` in Kotlin comments; Assert.kt: Rust `pub` item in Kotlin comments; Assert.kt: Rust `use` path in Kotlin comments; Assert.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Assert.kt: Rust lifetime explanation in Kotlin comments
- `bc.instrs` -> `bc.Instrs [ZERO]`: function-by-function score forced to 0. Instrs.kt: snake_case identifier `drop_in_place` in Kotlin comments; Instrs.kt: Rust `fn` declaration in Kotlin comments; Instrs.kt: Rust `let` binding in Kotlin comments; Instrs.kt: Rust `pub` item in Kotlin comments; Instrs.kt: Rust lifetime explanation in Kotlin comments; Instrs.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.scope` -> `compiler.Scope [ZERO]`: function-by-function score forced to 0. Scope.kt: snake_case identifier `scope_resolver_globals` in Kotlin comments; Scope.kt: Rust `fn` declaration in Kotlin comments; Scope.kt: Rust `pub` item in Kotlin comments; Scope.kt: Rust attribute syntax in Kotlin comments; Scope.kt: Rust lifetime explanation in Kotlin comments; Scope.kt: Rust-only type/unsafe terminology in Kotlin comments
- `heap.send` -> `heap.Send [ZERO]`: function-by-function score forced to 0. Send.kt: snake_case identifier `sealed_send` in Kotlin comments; Send.kt: Rust `fn` declaration in Kotlin comments; Send.kt: Rust `pub` item in Kotlin comments; Send.kt: Rust `impl` block in Kotlin comments; Send.kt: Rust lifetime explanation in Kotlin comments; Send.kt: Rust-only type/unsafe terminology in Kotlin comments
- `int.i32` -> `int.I32 [ZERO]`: function-by-function score forced to 0. I32.kt: snake_case identifier `pointer_i32` in Kotlin comments; I32.kt: Rust `fn` declaration in Kotlin comments; I32.kt: Rust `use` path in Kotlin comments; I32.kt: Rust lifetime explanation in Kotlin comments
- `list.unpack` -> `list.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `unpack_value_opt` in Kotlin comments; Unpack.kt: Rust `let` binding in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments
- `profile.csv` -> `profile.Csv [ZERO]`: function-by-function score forced to 0. Csv.kt: snake_case identifier `quote_str_for_csv` in Kotlin comments; Csv.kt: Rust `fn` declaration in Kotlin comments; Csv.kt: Rust `pub` item in Kotlin comments
- `structs.value` -> `structs.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_equals` in Kotlin code; Value.kt: snake_case identifier `of_value` in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]`: function-by-function score forced to 0. Bumpalo.kt: snake_case identifier `allocated_bytes` in Kotlin comments; Bumpalo.kt: Rust `fn` declaration in Kotlin comments; Bumpalo.kt: Rust `pub` item in Kotlin comments; Bumpalo.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Bumpalo.kt: Rust lifetime explanation in Kotlin comments; Bumpalo.kt: Rust-only type/unsafe terminology in Kotlin comments
- `debug.inspect` -> `debug.Inspect [ZERO]`: function-by-function score forced to 0. Inspect.kt: snake_case identifier `call_stack` in Kotlin comments
- `dict.refs` -> `dict.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `type_repr` in Kotlin comments; Refs.kt: Rust `fn` declaration in Kotlin comments; Refs.kt: Rust `pub` item in Kotlin comments; Refs.kt: Rust attribute syntax in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments
- `environment.modules` -> `environment.Modules [ZERO]`: function-by-function score forced to 0. Modules.kt: snake_case identifier `starlark_module` in Kotlin comments; Modules.kt: Rust `fn` declaration in Kotlin comments; Modules.kt: Rust `pub` item in Kotlin comments; Modules.kt: Rust attribute syntax in Kotlin comments; Modules.kt: Rust lifetime explanation in Kotlin comments
- `params.spec` -> `params.Spec [ZERO]`: function-by-function score forced to 0. Spec.kt: snake_case identifier `is_required` in Kotlin comments; Spec.kt: Rust `fn` declaration in Kotlin comments; Spec.kt: Rust `pub` item in Kotlin comments; Spec.kt: Rust attribute syntax in Kotlin comments; Spec.kt: Rust-only type/unsafe terminology in Kotlin comments
- `profile.stmt` -> `profile.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `data_from_generic` in Kotlin comments; Stmt.kt: Rust `fn` declaration in Kotlin comments; Stmt.kt: Rust `pub` item in Kotlin comments; Stmt.kt: Rust attribute syntax in Kotlin comments; Stmt.kt: Rust-only type/unsafe terminology in Kotlin comments
- `profile.time_flame` -> `profile.TimeFlame [ZERO]`: function-by-function score forced to 0. TimeFlame.kt: snake_case identifier `data_from_generic` in Kotlin comments; TimeFlame.kt: Rust `fn` declaration in Kotlin comments; TimeFlame.kt: Rust `pub` item in Kotlin comments; TimeFlame.kt: Rust attribute syntax in Kotlin comments; TimeFlame.kt: Rust lifetime explanation in Kotlin comments; TimeFlame.kt: Rust-only type/unsafe terminology in Kotlin comments
- `typing.iter` -> `typing.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Iter.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Iter.kt: Rust `fn` declaration in Kotlin comments; Iter.kt: Rust `pub` item in Kotlin comments; Iter.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.owned` -> `values.Owned [ZERO]`: function-by-function score forced to 0. Owned.kt: Rust lifetime explanation in Kotlin comments
- `values.unpack` -> `values.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `starlark_module` in Kotlin comments; Unpack.kt: Rust `fn` declaration in Kotlin comments; Unpack.kt: Rust `pub` item in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments; Unpack.kt: Rust auto-trait terminology in Kotlin comments
- `avalues.static_` -> `avalues.Static [ZERO]`: function-by-function score forced to 0. Static.kt: snake_case identifier `HAS_invoke` in Kotlin code; Static.kt: snake_case identifier `extra_len` in Kotlin comments; Static.kt: Rust `fn` declaration in Kotlin comments; Static.kt: Rust `pub` item in Kotlin comments; Static.kt: Rust lifetime explanation in Kotlin comments; Static.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.addr` -> `bc.Addr [ZERO]`: function-by-function score forced to 0. Addr.kt: snake_case identifier `offset_from` in Kotlin comments; Addr.kt: Rust `fn` declaration in Kotlin comments; Addr.kt: Rust `pub` item in Kotlin comments; Addr.kt: Rust attribute syntax in Kotlin comments; Addr.kt: Rust lifetime explanation in Kotlin comments; Addr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `dict.alloc` -> `dict.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `small_map` in Kotlin comments; Alloc.kt: Rust `fn` declaration in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; Alloc.kt: Rust lifetime explanation in Kotlin comments
- `enumeration.globals` -> `enumeration.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust `fn` declaration in Kotlin comments; Globals.kt: Rust `pub` item in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `heap.repr` -> `heap.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `new_const` in Kotlin comments; Repr.kt: Rust `fn` declaration in Kotlin comments; Repr.kt: Rust `pub` item in Kotlin comments; Repr.kt: Rust `impl` block in Kotlin comments; Repr.kt: Rust lifetime explanation in Kotlin comments; Repr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.methods` -> `list.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Methods.kt: Rust `fn` declaration in Kotlin comments; Methods.kt: Rust `let` binding in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `params.parser` -> `params.Parser [ZERO]`: function-by-function score forced to 0. Parser.kt: snake_case identifier `get_next` in Kotlin comments; Parser.kt: Rust `fn` declaration in Kotlin comments; Parser.kt: Rust `pub` item in Kotlin comments; Parser.kt: Rust lifetime explanation in Kotlin comments
- `profile.flamegraph` -> `profile.Flamegraph [ZERO]`: function-by-function score forced to 0. Flamegraph.kt: Rust `fn` declaration in Kotlin comments; Flamegraph.kt: Rust `pub` item in Kotlin comments; Flamegraph.kt: Rust attribute syntax in Kotlin comments; Flamegraph.kt: Rust lifetime explanation in Kotlin comments
- `profile.mode` -> `profile.Mode [ZERO]`: function-by-function score forced to 0. Mode.kt: snake_case identifier `non_exhaustive` in Kotlin comments; Mode.kt: Rust `fn` declaration in Kotlin comments; Mode.kt: Rust `pub` item in Kotlin comments; Mode.kt: Rust attribute syntax in Kotlin comments
- `profile.typecheck` -> `profile.Typecheck [ZERO]`: function-by-function score forced to 0. Typecheck.kt: snake_case identifier `data_from_generic` in Kotlin comments; Typecheck.kt: Rust `fn` declaration in Kotlin comments; Typecheck.kt: Rust `pub` item in Kotlin comments; Typecheck.kt: Rust attribute syntax in Kotlin comments
- `runtime.inlined_frame` -> `runtime.InlinedFrame [ZERO]`: function-by-function score forced to 0. InlinedFrame.kt: snake_case identifier `extend_frames` in Kotlin comments; InlinedFrame.kt: Rust `fn` declaration in Kotlin comments; InlinedFrame.kt: Rust `pub` item in Kotlin comments; InlinedFrame.kt: Rust attribute syntax in Kotlin comments; InlinedFrame.kt: Rust lifetime explanation in Kotlin comments
- `set.set` -> `set.Set [ZERO]`: function-by-function score forced to 0. Set.kt: snake_case identifier `starlark_module` in Kotlin comments; Set.kt: Rust attribute syntax in Kotlin comments; Set.kt: Rust lifetime explanation in Kotlin comments
- `string.methods` -> `string.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments; Methods.kt: Rust lifetime explanation in Kotlin comments
- `structs.alloc` -> `structs.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `fn` declaration in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments; Alloc.kt: Rust lifetime explanation in Kotlin comments; no target functions found; report scoring is function-by-function only
- `tests.util` -> `util.Util [STUB] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. no target functions found; report scoring is function-by-function only
- `typing.custom` -> `typing.Custom [ZERO]`: function-by-function score forced to 0. Custom.kt: snake_case identifier `validate_call` in Kotlin comments; Custom.kt: Rust `fn` declaration in Kotlin comments; Custom.kt: Rust `pub` item in Kotlin comments; Custom.kt: Rust-only type/unsafe terminology in Kotlin comments
- `avalues.list` -> `avalues.List [ZERO]`: function-by-function score forced to 0. List.kt: snake_case identifier `list_avalue` in Kotlin comments; List.kt: Rust `fn` declaration in Kotlin comments; List.kt: Rust `pub` item in Kotlin comments; List.kt: Rust lifetime explanation in Kotlin comments; List.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.opcode` -> `bc.Opcode [ZERO]`: function-by-function score forced to 0. Opcode.kt: snake_case identifier `dispatch_all` in Kotlin comments; Opcode.kt: Rust `fn` declaration in Kotlin comments; Opcode.kt: Rust `pub` item in Kotlin comments; Opcode.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.repr` -> `bc.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `align_of` in Kotlin comments
- `debug.evaluate` -> `debug.Evaluate [ZERO]`: function-by-function score forced to 0. Evaluate.kt: snake_case identifier `module_env` in Kotlin comments
- `enumeration.value` -> `enumeration.Value [ZERO]`: function-by-function score forced to 0. Value.kt: Rust `fn` declaration in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments
- `float.unpack` -> `float.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `type_repr` in Kotlin comments; Unpack.kt: Rust `fn` declaration in Kotlin comments; Unpack.kt: Rust `pub` item in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments
- `list.refs` -> `list.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `from_value` in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments
- `profile.heap` -> `profile.Heap [ZERO]`: function-by-function score forced to 0. Heap.kt: Rust `pub` item in Kotlin comments; Heap.kt: Rust attribute syntax in Kotlin comments
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]`: function-by-function score forced to 0. AllocUnpack.kt: snake_case identifier `alloc_value` in Kotlin comments; AllocUnpack.kt: Rust `fn` declaration in Kotlin comments; AllocUnpack.kt: Rust `impl` block in Kotlin comments; AllocUnpack.kt: Rust `use` path in Kotlin comments; AllocUnpack.kt: Rust lifetime explanation in Kotlin comments
- `symbol.map` -> `symbol.Map [ZERO]`: function-by-function score forced to 0. Map.kt: Rust-only type/unsafe terminology in Kotlin comments
- `tuple.refs` -> `tuple.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `ref_cast` in Kotlin comments; Refs.kt: Rust `fn` declaration in Kotlin comments; Refs.kt: Rust `pub` item in Kotlin comments; Refs.kt: Rust attribute syntax in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments; Refs.kt: Rust-only type/unsafe terminology in Kotlin comments
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust `fn` declaration in Kotlin comments; Globals.kt: Rust `pub` item in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]`: function-by-function score forced to 0. Matcher.kt: snake_case identifier `type_matcher` in Kotlin comments; Matcher.kt: Rust `pub` item in Kotlin comments; Matcher.kt: Rust attribute syntax in Kotlin comments; Matcher.kt: Rust lifetime explanation in Kotlin comments; Matcher.kt: Rust-only type/unsafe terminology in Kotlin comments; Matcher.kt: Rust auto-trait terminology in Kotlin comments
- `typing.never` -> `typing.Never [ZERO]`: function-by-function score forced to 0. Never.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Never.kt: snake_case identifier `starlark_value` in Kotlin comments; Never.kt: Rust `fn` declaration in Kotlin comments; Never.kt: Rust `pub` item in Kotlin comments; Never.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing.ty` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]`: function-by-function score forced to 0. Ty.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Ty.kt: snake_case identifier `starlark_value` in Kotlin comments; Ty.kt: Rust `fn` declaration in Kotlin comments; Ty.kt: Rust `pub` item in Kotlin comments
- `avalues.array` -> `avalues.Array [ZERO]`: function-by-function score forced to 0. Array.kt: snake_case identifier `array_avalue` in Kotlin comments; Array.kt: Rust `fn` declaration in Kotlin comments; Array.kt: Rust `pub` item in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Array.kt: Rust lifetime explanation in Kotlin comments; Array.kt: Rust-only type/unsafe terminology in Kotlin comments
- `avalues.complex` -> `avalues.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `extra_len` in Kotlin comments; Complex.kt: Rust `fn` declaration in Kotlin comments; Complex.kt: Rust `let` binding in Kotlin comments; Complex.kt: Rust `pub` item in Kotlin comments; Complex.kt: Rust lifetime explanation in Kotlin comments; Complex.kt: Rust-only type/unsafe terminology in Kotlin comments
- `avalues.tuple` -> `avalues.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `tuple_avalue` in Kotlin comments; Tuple.kt: Rust `fn` declaration in Kotlin comments; Tuple.kt: Rust `pub` item in Kotlin comments; Tuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Tuple.kt: Rust lifetime explanation in Kotlin comments; Tuple.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.bytecode` -> `bc.Bytecode [ZERO]`: function-by-function score forced to 0. Bytecode.kt: snake_case identifier `wrap_error_for_instr_ptr` in Kotlin comments; Bytecode.kt: Rust `fn` declaration in Kotlin comments; Bytecode.kt: Rust `pub` item in Kotlin comments; Bytecode.kt: Rust attribute syntax in Kotlin comments; Bytecode.kt: Rust lifetime explanation in Kotlin comments
- `bc.call` -> `bc.Call [ZERO]`: function-by-function score forced to 0. Call.kt: Rust `fn` declaration in Kotlin comments; Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `bc.definitely_assigned` -> `bc.DefinitelyAssigned [ZERO]`: function-by-function score forced to 0. DefinitelyAssigned.kt: snake_case identifier `definitely_assigned` in Kotlin comments; DefinitelyAssigned.kt: Rust `fn` declaration in Kotlin comments; DefinitelyAssigned.kt: Rust `pub` item in Kotlin comments; DefinitelyAssigned.kt: Rust attribute syntax in Kotlin comments
- `bc.instr_arg` -> `bc.InstrArg [ZERO]`: function-by-function score forced to 0. InstrArg.kt: snake_case identifier `fmt_append` in Kotlin comments; InstrArg.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]`: function-by-function score forced to 0. StackPtr.kt: snake_case identifier `derive_more` in Kotlin comments; StackPtr.kt: Rust `fn` declaration in Kotlin comments; StackPtr.kt: Rust `pub` item in Kotlin comments; StackPtr.kt: Rust attribute syntax in Kotlin comments; StackPtr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bool.type_repr` -> `bool.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `build` -> `starlark.Build [ZERO]`: function-by-function score forced to 0. Build.kt: snake_case identifier `rust_nightly` in Kotlin comments; Build.kt: Rust attribute syntax in Kotlin comments; no target functions found; report scoring is function-by-function only
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]`: function-by-function score forced to 0. MaybeUninitBackport.kt: snake_case identifier `write_slice_cloned` in Kotlin comments
- `compiler.args` -> `compiler.Args [ZERO]`: function-by-function score forced to 0. Args.kt: snake_case identifier `pos_only` in Kotlin comments; Args.kt: Rust `fn` declaration in Kotlin comments; Args.kt: Rust `pub` item in Kotlin comments; Args.kt: Rust lifetime explanation in Kotlin comments
- `compiler.def` -> `compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: snake_case identifier `HAS_invoke` in Kotlin code; Def.kt: snake_case identifier `starlark_value` in Kotlin comments; Def.kt: Rust attribute syntax in Kotlin comments; Def.kt: unchecked cast suppression hiding transliteration work in Kotlin code; Def.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.expr` -> `compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `get_methods` in Kotlin comments; Expr.kt: Rust `let` binding in Kotlin comments
- `eval.bc.compiler.stmt` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `funcs.min_max` -> `funcs.MinMax [ZERO]`: function-by-function score forced to 0. MinMax.kt: snake_case identifier `min_max_iter` in Kotlin comments; MinMax.kt: Rust `fn` declaration in Kotlin comments; MinMax.kt: Rust `let` binding in Kotlin comments; MinMax.kt: Rust `pub` item in Kotlin comments; MinMax.kt: Rust lifetime explanation in Kotlin comments
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]`: function-by-function score forced to 0. CallEnterExit.kt: snake_case identifier `needs_drop` in Kotlin comments; CallEnterExit.kt: Rust `fn` declaration in Kotlin comments; CallEnterExit.kt: Rust `pub` item in Kotlin comments; CallEnterExit.kt: Rust `impl` block in Kotlin comments; CallEnterExit.kt: Rust lifetime explanation in Kotlin comments
- `intern.interner` -> `intern.Interner [ZERO]`: function-by-function score forced to 0. Interner.kt: Rust `fn` declaration in Kotlin comments; Interner.kt: Rust `pub` item in Kotlin comments; Interner.kt: Rust attribute syntax in Kotlin comments; Interner.kt: Rust `use` path in Kotlin comments; Interner.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Interner.kt: Rust lifetime explanation in Kotlin comments
- `list.globals` -> `list.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `register_list` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]`: function-by-function score forced to 0. SummaryByFunction.kt: snake_case identifier `alloc_count` in Kotlin comments; SummaryByFunction.kt: Rust `fn` declaration in Kotlin comments; SummaryByFunction.kt: Rust `pub` item in Kotlin comments; SummaryByFunction.kt: Rust attribute syntax in Kotlin comments; SummaryByFunction.kt: Rust lifetime explanation in Kotlin comments; SummaryByFunction.kt: Rust-only type/unsafe terminology in Kotlin comments
- `set.refs` -> `set.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `unpack_value_opt` in Kotlin comments
- `stdlib.internal` -> `stdlib.Internal [ZERO]`: function-by-function score forced to 0. Internal.kt: snake_case identifier `starlark_module` in Kotlin comments; Internal.kt: Rust `fn` declaration in Kotlin comments; Internal.kt: Rust `pub` item in Kotlin comments; Internal.kt: Rust attribute syntax in Kotlin comments
- `structs.refs` -> `structs.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `debug_assert` in Kotlin comments
- `symbol.symbol` -> `symbol.Symbol [ZERO]`: function-by-function score forced to 0. Symbol.kt: snake_case identifier `copy_nonoverlapping` in Kotlin comments; Symbol.kt: Rust `fn` declaration in Kotlin comments; Symbol.kt: Rust `let` binding in Kotlin comments; Symbol.kt: Rust `pub` item in Kotlin comments; Symbol.kt: Rust lifetime explanation in Kotlin comments; Symbol.kt: Rust-only type/unsafe terminology in Kotlin comments
- `types.function` -> `types.Function [ZERO]`: function-by-function score forced to 0. Function.kt: snake_case identifier `HAS_invoke` in Kotlin code; Function.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Function.kt: Rust `fn` declaration in Kotlin comments; Function.kt: Rust `pub` item in Kotlin comments; Function.kt: Rust lifetime explanation in Kotlin comments
- `typing.any` -> `typing.Any [ZERO]`: function-by-function score forced to 0. Any.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Any.kt: snake_case identifier `starlark_value` in Kotlin comments; Any.kt: Rust `fn` declaration in Kotlin comments; Any.kt: Rust `pub` item in Kotlin comments
- `typing.callable` -> `typing.Callable [ZERO]`: function-by-function score forced to 0. Callable.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Callable.kt: Rust `fn` declaration in Kotlin comments; Callable.kt: Rust `pub` item in Kotlin comments; Callable.kt: Rust attribute syntax in Kotlin comments
- `values.index` -> `values.Index [ZERO]`: function-by-function score forced to 0. Index.kt: snake_case identifier `set_at` in Kotlin comments; Index.kt: Rust `fn` declaration in Kotlin comments; Index.kt: Rust `pub` item in Kotlin comments; Index.kt: Rust attribute syntax in Kotlin comments
- `values.traits` -> `values.Traits [ZERO]`: function-by-function score forced to 0. Traits.kt: snake_case identifier `HAS_invoke` in Kotlin code; Traits.kt: snake_case identifier `starlark_value` in Kotlin comments; Traits.kt: Rust `let` binding in Kotlin comments; Traits.kt: Rust attribute syntax in Kotlin comments
- `values.type_repr` -> `values.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: snake_case identifier `type_repr` in Kotlin comments
- `alloc.per_thread` -> `alloc.PerThread [ZERO]`: function-by-function score forced to 0. PerThread.kt: snake_case identifier `thread_local` in Kotlin comments
- `compiler.assign_modify` -> `compiler.AssignModify [ZERO]`: function-by-function score forced to 0. AssignModify.kt: snake_case identifier `mark_definitely_assigned_after` in Kotlin comments; AssignModify.kt: Rust `fn` declaration in Kotlin comments; AssignModify.kt: Rust `pub` item in Kotlin comments
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]`: function-by-function score forced to 0. IfCompiler.kt: snake_case identifier `write_if_else` in Kotlin comments; IfCompiler.kt: Rust `fn` declaration in Kotlin comments; IfCompiler.kt: Rust `pub` item in Kotlin comments
- `debug.adapter` -> `debug.Adapter [ZERO]`: function-by-function score forced to 0. Adapter.kt: snake_case identifier `Requests_SetBreakpoints` in Kotlin comments
- `dict.globals` -> `dict.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `as_type` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `docs` -> `docs.Docs [ZERO]`: function-by-function score forced to 0. Docs.kt: Rust `pub` item in Kotlin comments; Docs.kt: Rust lifetime explanation in Kotlin comments
- `eval.bc.compiler.call` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]`: function-by-function score forced to 0. Call.kt: snake_case identifier `write_bc` in Kotlin comments; Call.kt: Rust `fn` declaration in Kotlin comments; Call.kt: Rust `pub` item in Kotlin comments; Call.kt: Rust `use` path in Kotlin comments; Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `float.globals` -> `float.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `int.globals` -> `int.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `namespace.globals` -> `namespace.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `namespace.typing` -> `namespace.Typing [ZERO]`: function-by-function score forced to 0. Typing.kt: snake_case identifier `type_matcher` in Kotlin comments; Typing.kt: Rust attribute syntax in Kotlin comments
- `profile.by_type` -> `profile.ByType [ZERO]`: function-by-function score forced to 0. ByType.kt: snake_case identifier `allocated_summary` in Kotlin comments; ByType.kt: Rust `fn` declaration in Kotlin comments; ByType.kt: Rust `pub` item in Kotlin comments; ByType.kt: Rust attribute syntax in Kotlin comments; ByType.kt: Rust lifetime explanation in Kotlin comments; ByType.kt: Rust-only type/unsafe terminology in Kotlin comments
- `range.globals` -> `range.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `record.instance` -> `record.Instance [ZERO]`: function-by-function score forced to 0. Instance.kt: snake_case identifier `HAS_equals` in Kotlin code; Instance.kt: snake_case identifier `equals_slice` in Kotlin comments; Instance.kt: Rust `fn` declaration in Kotlin comments; Instance.kt: Rust `pub` item in Kotlin comments; Instance.kt: Rust lifetime explanation in Kotlin comments; Instance.kt: Rust-only type/unsafe terminology in Kotlin comments
- `structs.structs` -> `structs.Structs [ZERO]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `starlark_module` in Kotlin comments; Structs.kt: Rust attribute syntax in Kotlin comments
- `types.unbound` -> `types.Unbound [ZERO]`: function-by-function score forced to 0. Unbound.kt: snake_case identifier `to_frozen_value` in Kotlin comments; Unbound.kt: Rust `fn` declaration in Kotlin comments; Unbound.kt: Rust `pub` item in Kotlin comments; Unbound.kt: Rust attribute syntax in Kotlin comments; Unbound.kt: Rust lifetime explanation in Kotlin comments
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]`: function-by-function score forced to 0. RecursiveReprOrJsonGuard.kt: snake_case identifier `to_json` in Kotlin comments; RecursiveReprOrJsonGuard.kt: Rust `fn` declaration in Kotlin comments; RecursiveReprOrJsonGuard.kt: Rust `pub` item in Kotlin comments
- `__derive_refs` -> `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. DeriveRefs.kt: snake_case identifier `derive_refs` in Kotlin comments; DeriveRefs.kt: Rust `pub` item in Kotlin comments; DeriveRefs.kt: Rust `use` path in Kotlin comments; DeriveRefs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `__derive_refs.components` -> `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Components.kt: snake_case identifier `set_function` in Kotlin comments; Components.kt: Rust `fn` declaration in Kotlin comments; Components.kt: Rust `pub` item in Kotlin comments
- `__derive_refs.invoke_macro_error` -> `deriverefs.InvokeMacroError [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. InvokeMacroError.kt: snake_case identifier `into_starlark_error` in Kotlin comments; InvokeMacroError.kt: Rust `fn` declaration in Kotlin comments; InvokeMacroError.kt: Rust `pub` item in Kotlin comments
- `__derive_refs.sig` -> `deriverefs.Sig [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Sig.kt: snake_case identifier `parameter_spec` in Kotlin comments; Sig.kt: Rust `fn` declaration in Kotlin comments; Sig.kt: Rust `pub` item in Kotlin comments; Sig.kt: Rust lifetime explanation in Kotlin comments
- `allocator.alloc` -> `allocator.Alloc [STUB]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `chunk_part` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments
- `allocator.api` -> `allocator.Api [ZERO]`: function-by-function score forced to 0. Api.kt: snake_case identifier `value_alloc_size` in Kotlin comments; Api.kt: Rust `fn` declaration in Kotlin comments; Api.kt: Rust `pub` item in Kotlin comments; Api.kt: Rust `use` path in Kotlin comments; Api.kt: Rust lifetime explanation in Kotlin comments; Api.kt: Rust-only type/unsafe terminology in Kotlin comments
- `assert.conformance` -> `assert.Conformance [ZERO]`: function-by-function score forced to 0. Conformance.kt: snake_case identifier `conformance_except` in Kotlin comments; Conformance.kt: Rust `fn` declaration in Kotlin comments; Conformance.kt: Rust `pub` item in Kotlin comments
- `bc.compiler` -> `bc.Compiler [STUB]`: function-by-function score forced to 0. Compiler.kt: snake_case identifier `assign_modify` in Kotlin comments; Compiler.kt: Rust `pub` item in Kotlin comments
- `bc.for_loop` -> `bc.ForLoop [ZERO]`: function-by-function score forced to 0. ForLoop.kt: snake_case identifier `derive_more` in Kotlin comments; ForLoop.kt: Rust `pub` item in Kotlin comments; ForLoop.kt: Rust attribute syntax in Kotlin comments; ForLoop.kt: Rust `use` path in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `bc.instr` -> `bc.Instr [ZERO]`: function-by-function score forced to 0. Instr.kt: Rust `pub` item in Kotlin comments; Instr.kt: Rust lifetime explanation in Kotlin comments
- `bc.writer` -> `bc.Writer [ZERO]`: function-by-function score forced to 0. Writer.kt: snake_case identifier `alloc_slots_c` in Kotlin comments; Writer.kt: Rust `pub` item in Kotlin comments; Writer.kt: Rust-only type/unsafe terminology in Kotlin comments
- `callable.param` -> `callable.Param [ZERO]`: function-by-function score forced to 0. Param.kt: Rust `fn` declaration in Kotlin comments; Param.kt: Rust `pub` item in Kotlin comments
- `compiler.assign` -> `compiler.Assign [ZERO]`: function-by-function score forced to 0. Assign.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.error` -> `compiler.Error [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Error.kt: snake_case identifier `starlark_syntax` in Kotlin comments
- `compiler.expr_bool` -> `compiler.ExprBool [ZERO]`: function-by-function score forced to 0. ExprBool.kt: snake_case identifier `into_expr` in Kotlin comments; ExprBool.kt: Rust `fn` declaration in Kotlin comments; ExprBool.kt: Rust `pub` item in Kotlin comments
- `compiler.module` -> `compiler.Module [ZERO]`: function-by-function score forced to 0. Module.kt: snake_case identifier `eval_load` in Kotlin comments; Module.kt: Rust `fn` declaration in Kotlin comments; Module.kt: Rust `pub` item in Kotlin comments; Module.kt: Rust attribute syntax in Kotlin comments; Module.kt: Rust lifetime explanation in Kotlin comments
- `compiler.type_expr` -> `compiler.TypeExpr [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. TypeExpr.kt: snake_case identifier `type_str_literal_is_wildcard` in Kotlin comments; TypeExpr.kt: Rust `fn` declaration in Kotlin comments; TypeExpr.kt: Rust `pub` item in Kotlin comments; TypeExpr.kt: Rust attribute syntax in Kotlin comments; TypeExpr.kt: Rust lifetime explanation in Kotlin comments; TypeExpr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.types` -> `compiler.Types [ZERO]`: function-by-function score forced to 0. Types.kt: snake_case identifier `expr_ident` in Kotlin comments; Types.kt: Rust attribute syntax in Kotlin comments
- `docs.code` -> `docs.Code [ZERO]`: function-by-function score forced to 0. Code.kt: snake_case identifier `render_as_code` in Kotlin comments
- `docs.markdown` -> `docs.Markdown [ZERO]`: function-by-function score forced to 0. Markdown.kt: Rust `let` binding in Kotlin comments; Markdown.kt: Rust `pub` item in Kotlin comments; Markdown.kt: Rust-only type/unsafe terminology in Kotlin comments
- `docs.multipage` -> `docs.Multipage [ZERO]`: function-by-function score forced to 0. Multipage.kt: snake_case identifier `into_page_renders` in Kotlin comments; Multipage.kt: Rust `fn` declaration in Kotlin comments; Multipage.kt: Rust `pub` item in Kotlin comments; Multipage.kt: Rust lifetime explanation in Kotlin comments
- `environment` -> `starlark.Environment [ZERO]`: function-by-function score forced to 0. Environment.kt: snake_case identifier `module_dump` in Kotlin comments; Environment.kt: Rust `pub` item in Kotlin comments; Environment.kt: Rust attribute syntax in Kotlin comments; Environment.kt: Rust `use` path in Kotlin comments
- `environment.names` -> `environment.Names [ZERO]`: function-by-function score forced to 0. Names.kt: snake_case identifier `collect_defines_lvalue` in Kotlin comments; Names.kt: Rust `fn` declaration in Kotlin comments; Names.kt: Rust `pub` item in Kotlin comments; Names.kt: Rust attribute syntax in Kotlin comments; Names.kt: Rust lifetime explanation in Kotlin comments
- `environment.slots` -> `environment.Slots [ZERO]`: function-by-function score forced to 0. Slots.kt: snake_case identifier `get_slots_mut` in Kotlin comments; Slots.kt: Rust `fn` declaration in Kotlin comments; Slots.kt: Rust `pub` item in Kotlin comments; Slots.kt: Rust attribute syntax in Kotlin comments; Slots.kt: Rust lifetime explanation in Kotlin comments
- `errors` -> `starlark.Errors [STUB] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Errors.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Errors.kt: Rust `pub` item in Kotlin comments; Errors.kt: Rust `use` path in Kotlin comments; Errors.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.compr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr [ZERO]`: function-by-function score forced to 0. Compr.kt: snake_case identifier `write_bc` in Kotlin comments; Compr.kt: Rust `fn` declaration in Kotlin comments; Compr.kt: Rust `pub` item in Kotlin comments
- `eval.bc.compiler.def` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: snake_case identifier `mark_definitely_assigned_after` in Kotlin comments; Def.kt: Rust `fn` declaration in Kotlin comments; Def.kt: Rust `pub` item in Kotlin comments; Def.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.expr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `write_n_exprs` in Kotlin comments; Expr.kt: Rust `fn` declaration in Kotlin comments; Expr.kt: Rust `pub` item in Kotlin comments; Expr.kt: Rust `impl` block in Kotlin comments; Expr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `eval.runtime` -> `eval.Runtime [STUB]`: function-by-function score forced to 0. Runtime.kt: snake_case identifier `before_stmt` in Kotlin comments; Runtime.kt: Rust `pub` item in Kotlin comments; Runtime.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.soft_error` -> `eval.SoftError [ZERO]`: function-by-function score forced to 0. SoftError.kt: snake_case identifier `soft_error` in Kotlin comments; SoftError.kt: Rust `fn` declaration in Kotlin comments; SoftError.kt: Rust `pub` item in Kotlin comments; SoftError.kt: Rust `impl` block in Kotlin comments
- `fuzz_targets.starlark` -> `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]`: function-by-function score forced to 0. Starlark.kt: snake_case identifier `run_arbitrary_starlark_err` in Kotlin comments; Starlark.kt: Rust `fn` declaration in Kotlin comments; Starlark.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `heap.allocator` -> `heap.Allocator [STUB]`: function-by-function score forced to 0. Allocator.kt: Rust `pub` item in Kotlin comments
- `heap.branding` -> `heap.Branding [STUB]`: function-by-function score forced to 0. Branding.kt: snake_case identifier `alloc_str` in Kotlin comments; Branding.kt: Rust `fn` declaration in Kotlin comments; Branding.kt: Rust `let` binding in Kotlin comments; Branding.kt: Rust `pub` item in Kotlin comments; Branding.kt: Rust lifetime explanation in Kotlin comments
- `layout.avalues` -> `layout.AValues [STUB]`: function-by-function score forced to 0. AValues.kt: Rust `pub` item in Kotlin comments; AValues.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.static_string` -> `layout.StaticString [ZERO]`: function-by-function score forced to 0. StaticString.kt: Rust attribute syntax in Kotlin comments; StaticString.kt: Rust-only type/unsafe terminology in Kotlin comments
- `lib` -> `starlark.Lib [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Lib.kt: snake_case identifier `hello_world` in Kotlin comments; Lib.kt: Rust `pub` item in Kotlin comments; Lib.kt: Rust `use` path in Kotlin comments
- `macros` -> `starlark.Macros [ZERO]`: function-by-function score forced to 0. Macros.kt: snake_case identifier `from_value` in Kotlin comments; Macros.kt: Rust `macro_rules!` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `pagable` -> `starlark.Pagable [STUB]`: function-by-function score forced to 0. Pagable.kt: snake_case identifier `type_name` in Kotlin comments; Pagable.kt: Rust `pub` item in Kotlin comments; Pagable.kt: Rust attribute syntax in Kotlin comments; Pagable.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]`: function-by-function score forced to 0. VtableRegister.kt: snake_case identifier `register_avalue_simple_frozen` in Kotlin comments; VtableRegister.kt: Rust `macro_rules!` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `runtime.params` -> `runtime.Params [STUB]`: function-by-function score forced to 0. Params.kt: Rust `pub` item in Kotlin comments
- `runtime.profile` -> `runtime.Profile [STUB]`: function-by-function score forced to 0. Profile.kt: snake_case identifier `or_instrumentation` in Kotlin comments; Profile.kt: Rust `pub` item in Kotlin comments
- `runtime.slots` -> `runtime.Slots [ZERO]`: function-by-function score forced to 0. Slots.kt: snake_case identifier `to_bc_slot` in Kotlin comments; Slots.kt: Rust `fn` declaration in Kotlin comments; Slots.kt: Rust `pub` item in Kotlin comments; Slots.kt: Rust attribute syntax in Kotlin comments
- `stdlib.funcs` -> `stdlib.Funcs [STUB]`: function-by-function score forced to 0. Funcs.kt: snake_case identifier `min_max` in Kotlin comments; Funcs.kt: Rust `pub` item in Kotlin comments; Funcs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `string.globals` -> `string.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `string.intern` -> `string.Intern [STUB]`: function-by-function score forced to 0. Intern.kt: Rust `pub` item in Kotlin comments
- `string.iter` -> `string.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_iterate` in Kotlin code; Iter.kt: snake_case identifier `produce_char` in Kotlin comments; Iter.kt: Rust `fn` declaration in Kotlin comments; Iter.kt: Rust `pub` item in Kotlin comments; Iter.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Iter.kt: Rust lifetime explanation in Kotlin comments
- `syntax` -> `starlark.Syntax [STUB]`: function-by-function score forced to 0. Syntax.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Syntax.kt: Rust `pub` item in Kotlin comments; Syntax.kt: Rust `use` path in Kotlin comments; Syntax.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]`: function-by-function score forced to 0. Matchers.kt: snake_case identifier `is_wildcard` in Kotlin comments; Matchers.kt: Rust `fn` declaration in Kotlin comments; Matchers.kt: Rust `pub` item in Kotlin comments; Matchers.kt: Rust attribute syntax in Kotlin comments; Matchers.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `types.known_methods` -> `types.KnownMethods [ZERO]`: function-by-function score forced to 0. KnownMethods.kt: snake_case identifier `to_value` in Kotlin comments; KnownMethods.kt: Rust `fn` declaration in Kotlin comments; KnownMethods.kt: Rust `pub` item in Kotlin comments; KnownMethods.kt: Rust attribute syntax in Kotlin comments; KnownMethods.kt: Rust lifetime explanation in Kotlin comments
- `types.structs` -> `types.Structs [STUB]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `ip_address` in Kotlin comments; Structs.kt: Rust `pub` item in Kotlin comments
- `types.tuple` -> `types.Tuple [STUB]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `rust_tuple` in Kotlin comments; Tuple.kt: Rust `pub` item in Kotlin comments
- `typing.call_args` -> `typing.CallArgs [ZERO]`: function-by-function score forced to 0. CallArgs.kt: Rust `pub` item in Kotlin comments; CallArgs.kt: Rust lifetime explanation in Kotlin comments
- `typing.error` -> `typing.Error [ZERO]`: function-by-function score forced to 0. Error.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Error.kt: Rust `fn` declaration in Kotlin comments; Error.kt: Rust `pub` item in Kotlin comments; Error.kt: Rust `impl` block in Kotlin comments; Error.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Error.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]`: function-by-function score forced to 0. FillTypesForLint.kt: snake_case identifier `of_value` in Kotlin comments; FillTypesForLint.kt: Rust `fn` declaration in Kotlin comments; FillTypesForLint.kt: Rust `pub` item in Kotlin comments; FillTypesForLint.kt: Rust attribute syntax in Kotlin comments; FillTypesForLint.kt: Rust lifetime explanation in Kotlin comments
- `typing.mode` -> `typing.Mode [ZERO]`: function-by-function score forced to 0. Mode.kt: Rust `pub` item in Kotlin comments; Mode.kt: Rust attribute syntax in Kotlin comments
- `typing.oracle` -> `typing.Oracle [STUB]`: function-by-function score forced to 0. Oracle.kt: Rust `pub` item in Kotlin comments; Oracle.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]`: function-by-function score forced to 0. Remove.kt: Rust lifetime explanation in Kotlin comments
- `util` -> `starlark.Util [STUB]`: function-by-function score forced to 0. Util.kt: snake_case identifier `arc_or_static` in Kotlin comments; Util.kt: Rust `pub` item in Kotlin comments; Util.kt: Rust `use` path in Kotlin comments; Util.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values` -> `values.Values [ZERO]`: function-by-function score forced to 0. Values.kt: Rust `pub` item in Kotlin comments; Values.kt: Rust `use` path in Kotlin comments
- `values.comparison` -> `values.Comparison [ZERO]`: function-by-function score forced to 0. Comparison.kt: snake_case identifier `equals_slice` in Kotlin comments; Comparison.kt: Rust `pub` item in Kotlin comments
- `values.types` -> `values.Types [STUB]`: function-by-function score forced to 0. Types.kt: snake_case identifier `any_array` in Kotlin comments; Types.kt: Rust `pub` item in Kotlin comments; Types.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing` -> `values.Typing [STUB]`: function-by-function score forced to 0. Typing.kt: snake_case identifier `macro_refs` in Kotlin comments; Typing.kt: Rust `pub` item in Kotlin comments; Typing.kt: Rust `use` path in Kotlin comments; Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `wasm` -> `starlark.Wasm [ZERO]`: function-by-function score forced to 0. Wasm.kt: snake_case identifier `is_wasm` in Kotlin comments; Wasm.kt: Rust `fn` declaration in Kotlin comments; Wasm.kt: Rust `pub` item in Kotlin comments

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `layout.value` -> `layout.Value [ZERO]` (0.00, 178 deps)
- `typing.ty` -> `typing.Ty [ZERO]` (0.00, 109 deps)
- `layout.heap` -> `heap.Heap [STUB]` (0.00, 109 deps)
- `typing.starlark_value` -> `typing.StarlarkValue [ZERO]` (0.00, 76 deps)
- `runtime.evaluator` -> `runtime.Evaluator [ZERO]` (0.00, 56 deps)
- `values.trace` -> `values.Trace [ZERO]` (0.00, 52 deps)
- `values.freeze` -> `values.Freeze [ZERO]` (0.00, 42 deps)
- `values.alloc_value` -> `values.AllocValue [ZERO]` (0.00, 42 deps)
- `layout.freezer` -> `layout.Freezer [ZERO]` (0.00, 36 deps)
- `coerce` -> `starlark.Coerce [ZERO]` (0.00, 34 deps)
- `values.frozen_ref` -> `values.FrozenRef [ZERO]` (0.00, 27 deps)
- `none.none_type` -> `none.NoneType [ZERO]` (0.00, 27 deps)
- `runtime.arguments` -> `runtime.Arguments [ZERO]` (0.00, 25 deps)
- `typing.type_compiled` -> `type_compiled.TypeCompiled [STUB]` (0.00, 22 deps)
- `environment.globals` -> `environment.Globals [ZERO]` (0.00, 21 deps)
- `derive.module` -> `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]` (0.00, 21 deps)
- `values.value_of_unchecked` -> `values.ValueOfUnchecked [ZERO]` (0.00, 20 deps)
- `__derive_refs.param_spec` -> `deriverefs.ParamSpec [ZERO] [PROVENANCE-FALLBACK]` (0.00, 20 deps)
- `environment.methods` -> `environment.Methods [ZERO]` (0.00, 17 deps)
- `values.iter` -> `values.Iter [ZERO] [PROVENANCE-FALLBACK]` (0.00, 17 deps)
- `collections.symbol` -> `collections.Symbol [STUB]` (0.00, 15 deps)
- `private` -> `starlark.Private [ZERO]` (0.00, 15 deps)
- `layout.avalue` -> `layout.AValue [ZERO]` (0.00, 14 deps)
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]` (0.00, 12 deps)
- `typing.tuple` -> `typing.Tuple [ZERO]` (0.00, 12 deps)
- `layout.value_lifetimeless` -> `layout.ValueLifetimeless [ZERO]` (0.00, 12 deps)
- `types.dict` -> `types.Dict [STUB]` (0.00, 12 deps)
- `int.inline_int` -> `int.InlineInt [ZERO]` (0.00, 11 deps)
- `int.pointer_i32` -> `int.PointerI32 [ZERO]` (0.00, 9 deps)
- `any` -> `starlark.Any [ZERO]` (0.00, 8 deps)
- `layout.aligned_size` -> `layout.AlignedSize [ZERO]` (0.00, 8 deps)
- `cast` -> `starlark.Cast [ZERO]` (0.00, 8 deps)
- `eval.compiler` -> `eval.Compiler [ZERO]` (0.00, 8 deps)
- `types.bigint` -> `types.Bigint [ZERO]` (0.00, 7 deps)
- `runtime.frozen_file_span` -> `runtime.FrozenFileSpan [ZERO]` (0.00, 7 deps)
- `type_compiled.type_matcher_factory` -> `type_compiled.TypeMatcherFactory [ZERO]` (0.00, 7 deps)
- `runtime.small_duration` -> `runtime.SmallDuration [ZERO]` (0.00, 6 deps)
- `dict.dict_type` -> `dict.DictType [ZERO]` (0.00, 6 deps)
- `typing.typecheck` -> `typing.Typecheck [STUB]` (0.00, 6 deps)
- `values.freeze_error` -> `values.FreezeError [ZERO]` (0.00, 6 deps)
- `layout.value_alloc_size` -> `layout.ValueAllocSize [ZERO]` (0.00, 6 deps)
- `compiler.stmt` -> `compiler.Stmt [ZERO]` (0.00, 6 deps)
- `profile.profiler_type` -> `profile.ProfilerType [ZERO]` (0.00, 6 deps)
- `values.layout` -> `values.Layout [STUB]` (0.00, 6 deps)
- `tests.def` -> `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]` (0.00, 5 deps)
- `types.array` -> `types.Array [ZERO]` (0.00, 5 deps)
- `typing.arc_ty` -> `typing.ArcTy [ZERO]` (0.00, 5 deps)
- `eval.bc` -> `bc.Bc [STUB]` (0.00, 5 deps)
- `scope.scope_resolver_globals` -> `scope.ScopeResolverGlobals [ZERO]` (0.00, 5 deps)
- `types.range` -> `types.Range [STUB]` (0.00, 5 deps)
- `typing.interface` -> `typing.Interface [ZERO]` (0.00, 5 deps)
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]` (0.00, 4 deps)
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]` (0.00, 4 deps)
- `bc.frame` -> `bc.Frame [ZERO]` (0.00, 4 deps)
- `values.value_of` -> `values.ValueOf [ZERO]` (0.00, 4 deps)
- `profile.alloc_counts` -> `profile.AllocCounts [ZERO]` (0.00, 4 deps)
- `record.record_type` -> `record.RecordType [ZERO]` (0.00, 3 deps)
- `alloc.chunk` -> `alloc.Chunk [ZERO]` (0.00, 3 deps)
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]` (0.00, 3 deps)
- `errors.did_you_mean` -> `errors.DidYouMean [ZERO]` (0.00, 3 deps)
- `list.alloc` -> `list.Alloc [ZERO]` (0.00, 3 deps)
- `compiler.constants` -> `compiler.Constants [ZERO]` (0.00, 3 deps)
- `profile.instant` -> `profile.Instant [ZERO]` (0.00, 3 deps)
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]` (0.00, 3 deps)
- `sealed` -> `starlark.Sealed [ZERO]` (0.00, 3 deps)
- `types.namespace` -> `types.Namespace [STUB]` (0.00, 3 deps)
- `types.record` -> `types.Record [ZERO]` (0.00, 3 deps)
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]` (0.00, 2 deps)
- `util.arc_or_static` -> `util.ArcOrStatic [ZERO]` (0.00, 2 deps)
- `typing.type_type` -> `typing.TypeType [ZERO]` (0.00, 2 deps)
- `alloc.chunk_part` -> `alloc.ChunkPart [ZERO]` (0.00, 2 deps)
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]` (0.00, 2 deps)
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]` (0.00, 2 deps)
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]` (0.00, 2 deps)
- `avalues.str_` -> `avalues.Str [ZERO]` (0.00, 2 deps)
- `values.stack_guard` -> `values.StackGuard [ZERO]` (0.00, 2 deps)
- `collections.string_pool` -> `collections.StringPool [ZERO]` (0.00, 2 deps)
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]` (0.00, 2 deps)
- `hint` -> `starlark.Hint [ZERO] [PROVENANCE-FALLBACK]` (0.00, 2 deps)
- `profile.string_index` -> `profile.StringIndex [ZERO]` (0.00, 2 deps)
- `runtime.file_loader` -> `runtime.FileLoader [ZERO]` (0.00, 2 deps)
- `types.float` -> `types.Float [STUB]` (0.00, 2 deps)
- `types.list` -> `types.List [STUB]` (0.00, 2 deps)
- `types.num` -> `types.Num [STUB]` (0.00, 2 deps)
- `values.thin_box_slice_frozen_value` -> `values.ThinBoxSliceFrozenValue [STUB]` (0.00, 2 deps)
- `heap.arena` -> `heap.Arena [ZERO]` (0.00, 1 deps)
- `collections.alloca` -> `collections.Alloca [ZERO]` (0.00, 1 deps)
- `stdlib` -> `starlark.Stdlib [ZERO]` (0.00, 1 deps)
- `string.interpolation` -> `string.Interpolation [ZERO]` (0.00, 1 deps)
- `types.list_or_tuple` -> `types.ListOrTuple [ZERO]` (0.00, 1 deps)
- `layout.pointer` -> `layout.Pointer [ZERO]` (0.00, 1 deps)
- `types.any_complex` -> `types.AnyComplex [ZERO]` (0.00, 1 deps)
- `types.any_array` -> `types.AnyArray [ZERO]` (0.00, 1 deps)
- `util.rtabort` -> `util.Rtabort [ZERO]` (0.00, 1 deps)
- `bc.if_debug` -> `bc.IfDebug [ZERO]` (0.00, 1 deps)
- `util.non_static_type_id` -> `util.NonStaticTypeId [ZERO]` (0.00, 1 deps)
- `avalues.simple` -> `avalues.Simple [ZERO]` (0.00, 1 deps)
- `layout.value_captured` -> `layout.ValueCaptured [ZERO]` (0.00, 1 deps)
- `record.field` -> `record.Field [ZERO]` (0.00, 1 deps)
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]` (0.00, 1 deps)
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]` (0.00, 1 deps)
- `heap.fast_cell` -> `heap.FastCell [ZERO]` (0.00, 1 deps)
- `read_line` -> `starlark.ReadLine [ZERO]` (0.00, 1 deps)
- `typing.bindings` -> `typing.Bindings [STUB]` (0.00, 1 deps)
- `typing.structs` -> `typing.Structs [ZERO]` (0.00, 1 deps)
- `analysis.lint_message` -> `analysis.LintMessage [ZERO]` (0.00, 1 deps)
- `types.bool` -> `types.Bool [ZERO]` (0.00, 1 deps)
- `types.enumeration` -> `types.Enumeration [STUB]` (0.00, 1 deps)
- `types.int` -> `types.Int [ZERO]` (0.00, 1 deps)
- `types.none` -> `types.None [STUB]` (0.00, 1 deps)
- `types.set` -> `types.Set [STUB]` (0.00, 1 deps)
- `types.string` -> `types.String [STUB]` (0.00, 1 deps)
- `typing` -> `starlark.Typing [STUB]` (0.00, 1 deps)
- `typing.function` -> `typing.Function [STUB]` (0.00, 1 deps)
- `set.methods` -> `set.Methods [STUB]` (0.00)
- `string.str_type` -> `string.StrType [ZERO]` (0.00)
- `int.int_or_big` -> `int.IntOrBig [STUB]` (0.00)
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]` (0.00)
- `set.value` -> `set.Value [ZERO]` (0.00)
- `values.typing.callable` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` (0.00)
- `typing.user` -> `typing.User [ZERO]` (0.00)
- `float.float` -> `float.Float [ZERO]` (0.00)
- `layout.typed` -> `layout.ValueTyped [ZERO]` (0.00)
- `scope.payload` -> `scope.Payload [ZERO]` (0.00)
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]` (0.00)
- `string.repr` -> `string.Repr [ZERO]` (0.00)
- `list.value` -> `list.Value [ZERO]` (0.00)
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]` (0.00)
- `stdlib.extra` -> `stdlib.Extra [ZERO]` (0.00)
- `dict.value` -> `dict.Value [ZERO]` (0.00)
- `record.globals` -> `record.Globals [ZERO]` (0.00)
- `alloc.chain` -> `alloc.Chain [ZERO]` (0.00)
- `heap.heap_type` -> `heap.HeapType [ZERO]` (0.00)
- `range.range_type` -> `range.RangeType [ZERO]` (0.00)
- `stdlib.partial` -> `stdlib.Partial [ZERO]` (0.00)
- `alloc.allocator` -> `alloc.Allocator [ZERO]` (0.00)
- `profile.bc` -> `profile.Bc [ZERO]` (0.00)
- `tuple.unpack` -> `tuple.Unpack [ZERO]` (0.00)
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]` (0.00)
- `bigint.convert` -> `bigint.Convert [ZERO]` (0.00)
- `dict.methods` -> `dict.Methods [ZERO]` (0.00)
- `docs.parse` -> `docs.Parse [ZERO]` (0.00)
- `funcs.other` -> `funcs.Other [ZERO]` (0.00)
- `layout.complex` -> `layout.Complex [ZERO]` (0.00)
- `profile.aggregated` -> `profile.Aggregated [ZERO]` (0.00)
- `record.ty_record_type` -> `record.TyRecordType [ZERO]` (0.00)
- `string.simd` -> `string.Simd [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `tuple.value` -> `tuple.Value [ZERO]` (0.00)
- `typed.string` -> `typed.String [ZERO]` (0.00)
- `adapter.implementation` -> `adapter.Implementation [ZERO]` (0.00)
- `assert.assert` -> `assert.Assert [STUB]` (0.00)
- `bc.instrs` -> `bc.Instrs [ZERO]` (0.00)
- `compiler.scope` -> `compiler.Scope [ZERO]` (0.00)
- `heap.send` -> `heap.Send [ZERO]` (0.00)
- `int.i32` -> `int.I32 [ZERO]` (0.00)
- `list.unpack` -> `list.Unpack [ZERO]` (0.00)
- `profile.csv` -> `profile.Csv [ZERO]` (0.00)
- `structs.value` -> `structs.Value [ZERO]` (0.00)
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]` (0.00)
- `debug.inspect` -> `debug.Inspect [ZERO]` (0.00)
- `dict.refs` -> `dict.Refs [ZERO]` (0.00)
- `environment.modules` -> `environment.Modules [ZERO]` (0.00)
- `params.spec` -> `params.Spec [ZERO]` (0.00)
- `profile.stmt` -> `profile.Stmt [ZERO]` (0.00)
- `profile.time_flame` -> `profile.TimeFlame [ZERO]` (0.00)
- `typing.iter` -> `typing.Iter [ZERO]` (0.00)
- `values.owned` -> `values.Owned [ZERO]` (0.00)
- `values.unpack` -> `values.Unpack [ZERO]` (0.00)
- `avalues.static_` -> `avalues.Static [ZERO]` (0.00)
- `bc.addr` -> `bc.Addr [ZERO]` (0.00)
- `dict.alloc` -> `dict.Alloc [ZERO]` (0.00)
- `enumeration.globals` -> `enumeration.Globals [ZERO]` (0.00)
- `heap.repr` -> `heap.Repr [ZERO]` (0.00)
- `list.methods` -> `list.Methods [ZERO]` (0.00)
- `params.parser` -> `params.Parser [ZERO]` (0.00)
- `profile.flamegraph` -> `profile.Flamegraph [ZERO]` (0.00)
- `profile.mode` -> `profile.Mode [ZERO]` (0.00)
- `profile.typecheck` -> `profile.Typecheck [ZERO]` (0.00)
- `runtime.inlined_frame` -> `runtime.InlinedFrame [ZERO]` (0.00)
- `set.set` -> `set.Set [ZERO]` (0.00)
- `string.methods` -> `string.Methods [ZERO]` (0.00)
- `structs.alloc` -> `structs.Alloc [ZERO]` (0.00)
- `tests.util` -> `util.Util [STUB] [PROVENANCE-FALLBACK]` (0.00)
- `typing.custom` -> `typing.Custom [ZERO]` (0.00)
- `avalues.list` -> `avalues.List [ZERO]` (0.00)
- `bc.opcode` -> `bc.Opcode [ZERO]` (0.00)
- `bc.repr` -> `bc.Repr [ZERO]` (0.00)
- `debug.evaluate` -> `debug.Evaluate [ZERO]` (0.00)
- `enumeration.value` -> `enumeration.Value [ZERO]` (0.00)
- `float.unpack` -> `float.Unpack [ZERO]` (0.00)
- `list.refs` -> `list.Refs [ZERO]` (0.00)
- `profile.heap` -> `profile.Heap [ZERO]` (0.00)
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]` (0.00)
- `symbol.map` -> `symbol.Map [ZERO]` (0.00)
- `tuple.refs` -> `tuple.Refs [ZERO]` (0.00)
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]` (0.00)
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]` (0.00)
- `typing.never` -> `typing.Never [ZERO]` (0.00)
- `values.typing.ty` -> `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` (0.00)
- `avalues.array` -> `avalues.Array [ZERO]` (0.00)
- `avalues.complex` -> `avalues.Complex [ZERO]` (0.00)
- `avalues.tuple` -> `avalues.Tuple [ZERO]` (0.00)
- `bc.bytecode` -> `bc.Bytecode [ZERO]` (0.00)
- `bc.call` -> `bc.Call [ZERO]` (0.00)
- `bc.definitely_assigned` -> `bc.DefinitelyAssigned [ZERO]` (0.00)
- `bc.instr_arg` -> `bc.InstrArg [ZERO]` (0.00)
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]` (0.00)
- `bool.type_repr` -> `bool.TypeRepr [ZERO]` (0.00)
- `build` -> `starlark.Build [ZERO]` (0.00)
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]` (0.00)
- `compiler.args` -> `compiler.Args [ZERO]` (0.00)
- `compiler.def` -> `compiler.Def [ZERO]` (0.00)
- `compiler.expr` -> `compiler.Expr [ZERO]` (0.00)
- `eval.bc.compiler.stmt` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` (0.00)
- `funcs.min_max` -> `funcs.MinMax [ZERO]` (0.00)
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]` (0.00)
- `intern.interner` -> `intern.Interner [ZERO]` (0.00)
- `list.globals` -> `list.Globals [ZERO]` (0.00)
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]` (0.00)
- `set.refs` -> `set.Refs [ZERO]` (0.00)
- `stdlib.internal` -> `stdlib.Internal [ZERO]` (0.00)
- `structs.refs` -> `structs.Refs [ZERO]` (0.00)
- `symbol.symbol` -> `symbol.Symbol [ZERO]` (0.00)
- `types.function` -> `types.Function [ZERO]` (0.00)
- `typing.any` -> `typing.Any [ZERO]` (0.00)
- `typing.callable` -> `typing.Callable [ZERO]` (0.00)
- `values.index` -> `values.Index [ZERO]` (0.00)
- `values.traits` -> `values.Traits [ZERO]` (0.00)
- `values.type_repr` -> `values.TypeRepr [ZERO]` (0.00)
- `alloc.per_thread` -> `alloc.PerThread [ZERO]` (0.00)
- `compiler.assign_modify` -> `compiler.AssignModify [ZERO]` (0.00)
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]` (0.00)
- `debug.adapter` -> `debug.Adapter [ZERO]` (0.00)
- `dict.globals` -> `dict.Globals [ZERO]` (0.00)
- `docs` -> `docs.Docs [ZERO]` (0.00)
- `eval.bc.compiler.call` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` (0.00)
- `float.globals` -> `float.Globals [ZERO]` (0.00)
- `int.globals` -> `int.Globals [ZERO]` (0.00)
- `namespace.globals` -> `namespace.Globals [ZERO]` (0.00)
- `namespace.typing` -> `namespace.Typing [ZERO]` (0.00)
- `profile.by_type` -> `profile.ByType [ZERO]` (0.00)
- `range.globals` -> `range.Globals [ZERO]` (0.00)
- `record.instance` -> `record.Instance [ZERO]` (0.00)
- `structs.structs` -> `structs.Structs [ZERO]` (0.00)
- `types.unbound` -> `types.Unbound [ZERO]` (0.00)
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]` (0.00)
- `__derive_refs` -> `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]` (0.00)
- `__derive_refs.components` -> `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `__derive_refs.invoke_macro_error` -> `deriverefs.InvokeMacroError [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `__derive_refs.sig` -> `deriverefs.Sig [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `allocator.alloc` -> `allocator.Alloc [STUB]` (0.00)
- `allocator.api` -> `allocator.Api [ZERO]` (0.00)
- `assert.conformance` -> `assert.Conformance [ZERO]` (0.00)
- `bc.compiler` -> `bc.Compiler [STUB]` (0.00)
- `bc.for_loop` -> `bc.ForLoop [ZERO]` (0.00)
- `bc.instr` -> `bc.Instr [ZERO]` (0.00)
- `bc.writer` -> `bc.Writer [ZERO]` (0.00)
- `callable.param` -> `callable.Param [ZERO]` (0.00)
- `compiler.assign` -> `compiler.Assign [ZERO]` (0.00)
- `compiler.error` -> `compiler.Error [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `compiler.expr_bool` -> `compiler.ExprBool [ZERO]` (0.00)
- `compiler.module` -> `compiler.Module [ZERO]` (0.00)
- `compiler.type_expr` -> `compiler.TypeExpr [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `compiler.types` -> `compiler.Types [ZERO]` (0.00)
- `docs.code` -> `docs.Code [ZERO]` (0.00)
- `docs.markdown` -> `docs.Markdown [ZERO]` (0.00)
- `docs.multipage` -> `docs.Multipage [ZERO]` (0.00)
- `environment` -> `starlark.Environment [ZERO]` (0.00)
- `environment.names` -> `environment.Names [ZERO]` (0.00)
- `environment.slots` -> `environment.Slots [ZERO]` (0.00)
- `errors` -> `starlark.Errors [STUB] [PROVENANCE-FALLBACK]` (0.00)
- `eval.bc.compiler.compr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr [ZERO]` (0.00)
- `eval.bc.compiler.def` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` (0.00)
- `eval.bc.compiler.expr` -> `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` (0.00)
- `eval.runtime` -> `eval.Runtime [STUB]` (0.00)
- `eval.soft_error` -> `eval.SoftError [ZERO]` (0.00)
- `fuzz_targets.starlark` -> `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]` (0.00)
- `heap.allocator` -> `heap.Allocator [STUB]` (0.00)
- `heap.branding` -> `heap.Branding [STUB]` (0.00)
- `layout.avalues` -> `layout.AValues [STUB]` (0.00)
- `layout.static_string` -> `layout.StaticString [ZERO]` (0.00)
- `lib` -> `starlark.Lib [STUB]` (0.00)
- `macros` -> `starlark.Macros [ZERO]` (0.00)
- `pagable` -> `starlark.Pagable [STUB]` (0.00)
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]` (0.00)
- `runtime.params` -> `runtime.Params [STUB]` (0.00)
- `runtime.profile` -> `runtime.Profile [STUB]` (0.00)
- `runtime.slots` -> `runtime.Slots [ZERO]` (0.00)
- `stdlib.funcs` -> `stdlib.Funcs [STUB]` (0.00)
- `string.globals` -> `string.Globals [ZERO]` (0.00)
- `string.intern` -> `string.Intern [STUB]` (0.00)
- `string.iter` -> `string.Iter [ZERO]` (0.00)
- `syntax` -> `starlark.Syntax [STUB]` (0.00)
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]` (0.00)
- `types.known_methods` -> `types.KnownMethods [ZERO]` (0.00)
- `types.structs` -> `types.Structs [STUB]` (0.00)
- `types.tuple` -> `types.Tuple [STUB]` (0.00)
- `typing.call_args` -> `typing.CallArgs [ZERO]` (0.00)
- `typing.error` -> `typing.Error [ZERO]` (0.00)
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]` (0.00)
- `typing.mode` -> `typing.Mode [ZERO]` (0.00)
- `typing.oracle` -> `typing.Oracle [STUB]` (0.00)
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]` (0.00)
- `util` -> `starlark.Util [STUB]` (0.00)
- `values` -> `values.Values [ZERO]` (0.00)
- `values.comparison` -> `values.Comparison [ZERO]` (0.00)
- `values.types` -> `values.Types [STUB]` (0.00)
- `values.typing` -> `values.Typing [STUB]` (0.00)
- `wasm` -> `starlark.Wasm [ZERO]` (0.00)
- `types.type_instance_id` -> `types.TypeInstanceId` (0.00, 9 deps)
- `tuple.rust_tuple` -> `tuple.RustTuple` (0.00)
- `bool.unpack` -> `bool.Unpack` (0.00)
- `enumeration.ty_enum_type` -> `enumeration.TyEnumType` (0.00)
- `pagable.error` -> `pagable.Error` (0.00)
- `runtime.visit_span` -> `runtime.VisitSpan` (0.00)
- `stdlib.json` -> `stdlib.Json` (0.04)
- `analysis` -> `starlark.Analysis` (0.05)
- `bool.globals` -> `bool.Globals` (0.23)
- `typing.small_arc_vec_or_static` -> `typing.SmallArcVecOrStatic` (0.25)
- `tuple.globals` -> `tuple.Globals` (0.29)
- `analysis.types` -> `analysis.Types` (0.30)
- `typing.small_arc_vec` -> `typing.SmallArcVec` (0.31)
- `num.value` -> `num.Value` (0.31)
- `num.globals` -> `num.Globals` (0.32)
- `util.refcell` -> `refcell.RefCell` (0.32, 20 deps)
- `dict.traits` -> `dict.Traits` (0.33)
- `heap.maybe_uninit_slice_util` -> `heap.MaybeUninitSliceUtil` (0.34)
- `collections.aligned_padded_str` -> `alignedpaddedstr.AlignedPaddedStr` (0.34, 2 deps)
- `values.demand` -> `values.Demand` (0.37, 4 deps)
- `list.list_type` -> `list.ListType` (0.37, 3 deps)
- `string.dot_format` -> `string.DotFormat` (0.43, 1 deps)
- `analysis.underscore` -> `analysis.Underscore` (0.44)
- `analysis.performance` -> `analysis.Performance` (0.45)
- `stdlib.breakpoint` -> `stdlib.Breakpoint` (0.45, 1 deps)
- `analysis.names` -> `analysis.Names` (0.47)
- `analysis.dubious` -> `analysis.Dubious` (0.48)
- `environment.module_dump` -> `environment.ModuleDump` (0.48)
- `bool.value` -> `bool.Value` (0.49)
- `tuple.alloc` -> `tuple.Alloc` (0.49)
- `namespace.value` -> `namespace.Value` (0.50)
- `bc.native_function` -> `bc.NativeFunction` (0.51, 4 deps)
- `analysis.flow` -> `analysis.Flow` (0.54)
- `types.ellipsis` -> `types.Ellipsis` (0.55, 4 deps)
- `analysis.find_call_name` -> `analysis.FindCallName` (0.55)
- `dict.unpack` -> `dict.Unpack` (0.55)
- `profile.data` -> `profile.Data` (0.55)
- `typing.callable_param` -> `typing.CallableParam` (0.57)
- `analysis.incompatible` -> `analysis.Incompatible` (0.59)
- `oracle.traits` -> `oracle.Traits` (0.60)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| `layout.value` | `layout.Value [ZERO]` | 3/9 | `DisplayWithTypeImpl`, `Canonical`, `String` |
| `values.freeze` | `values.Freeze [ZERO]` | 1/2 | `Frozen` |
| `coerce` | `starlark.Coerce [ZERO]` | 7/9 | `Aaa`, `Bbb`, `StructWithLifetimeAndTypeParams`, `Newtype`, `Struct`, `Trait`, `Assoc` |
| `compiler.span` | `compiler.Span` | 1/2 | `Target` |
| `values.frozen_ref` | `values.FrozenRef [ZERO]` | 2/4 | `Target`, `Frozen` |
| `none.none_type` | `none.NoneType [ZERO]` | 1/2 | `Error` |
| `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 4/7 | `Canonical`, `Frozen`, `Error`, `ReprNotSendSync` |
| `environment.methods` | `environment.Methods [ZERO]` | 1/4 | `Magic` |
| `values.iter` | `values.Iter [ZERO] [PROVENANCE-FALLBACK]` | 1/2 | `Item` |
| `int.inline_int` | `int.InlineInt [ZERO]` | 3/5 | `Error`, `Output`, `Canonical` |
| `int.pointer_i32` | `int.PointerI32 [ZERO]` | 1/2 | `Canonical` |
| `any` | `starlark.Any [ZERO]` | 12/15 | `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar` |
| `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 1/2 | `Output` |
| `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 1/2 | `Output` |
| `dict.dict_type` | `dict.DictType [ZERO]` | 2/3 | `Canonical`, `Error` |
| `none.none_or` | `none.NoneOr` | 2/3 | `Canonical`, `Error` |
| `values.freeze_error` | `values.FreezeError [ZERO]` | 1/4 | `FreezeResult` |
| `typing.arc_ty` | `typing.ArcTy [ZERO]` | 1/4 | `Target` |
| `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 4/8 | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` |
| `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 2/4 | `Canonical`, `CompilerArgs` |
| `values.demand` | `values.Demand` | 3/4 | `SomeTrait`, `StaticType`, `MyValue` |
| `values.value_of` | `values.ValueOf [ZERO]` | 3/4 | `Target`, `Canonical`, `Error` |
| `profile.alloc_counts` | `profile.AllocCounts [ZERO]` | 1/2 | `Output` |
| `record.record_type` | `record.RecordType [ZERO]` | 6/8 | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` |
| `alloc.chunk` | `alloc.Chunk [ZERO]` | 1/3 | `ChunkDataEmpty` |
| `list.alloc` | `list.Alloc [ZERO]` | 1/2 | `Canonical` |
| `list.list_type` | `list.ListType` | 2/3 | `Canonical`, `Error` |
| `profile.instant` | `profile.Instant [ZERO]` | 1/2 | `Output` |
| `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 2/3 | `Canonical`, `Error` |
| `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 3/4 | `Target`, `Item`, `IntoIter` |
| `util.arc_or_static` | `util.ArcOrStatic [ZERO]` | 1/3 | `Target` |
| `typing.type_type` | `typing.TypeType [ZERO]` | 2/3 | `Canonical`, `Error` |
| `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 1/3 | `Target` |
| `avalues.str_` | `avalues.Str [ZERO]` | 2/3 | `StarlarkValue`, `ExtraElem` |
| `heap.arena` | `heap.Arena [ZERO]` | 3/7 | `ChunkIter`, `Item`, `ArenaUninit` |
| `collections.alloca` | `collections.Alloca [ZERO]` | 3/4 | `Buffer`, `Align`, `DropSliceGuard` |
| `stdlib` | `starlark.Stdlib [ZERO]` | 2/3 | `Bool2`, `Error` |
| `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `stdlib.breakpoint` | `stdlib.Breakpoint` | 1/6 | `Handler` |
| `types.any_complex` | `types.AnyComplex [ZERO]` | 4/5 | `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData` |
| `types.any_array` | `types.AnyArray [ZERO]` | 2/3 | `Canonical`, `IncrementOnDrop` |
| `util.rtabort` | `util.Rtabort [ZERO]` | 1/1 | `AbortOnDrop` |
| `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 1/1 | `NonStaticAny` |
| `avalues.simple` | `avalues.Simple [ZERO]` | 2/3 | `StarlarkValue`, `ExtraElem` |
| `layout.value_captured` | `layout.ValueCaptured [ZERO]` | 2/4 | `Canonical`, `Frozen` |
| `record.field` | `record.Field [ZERO]` | 1/1 | `FieldGen` |
| `read_line` | `starlark.ReadLine [ZERO]` | 1/2 | `NoRustyline` |
| `bc.instr_impl` | `bc.InstrImpl` | 76/163 | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` |
| `string.str_type` | `string.StrType [ZERO]` | 4/4 | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` |
| `int.int_or_big` | `int.IntOrBig [STUB]` | 4/7 | `Canonical`, `Err`, `Error`, `Output` |
| `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 2/3 | `ThinBoxSliceLayout`, `Target` |
| `set.value` | `set.Value [ZERO]` | 3/9 | `Canonical`, `Frozen`, `ContentRef` |
| `values.typing.callable` | `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 3/8 | `Canonical`, `Error`, `Frozen` |
| `typing.user` | `typing.User [ZERO]` | 3/8 | `AbstractPlant`, `FruitCallable`, `Fruit` |
| `analysis.names` | `analysis.Names` | 1/8 | `AstStrExt` |
| `float.float` | `float.Float [ZERO]` | 2/3 | `Canonical`, `Error` |
| `layout.typed` | `layout.ValueTyped [ZERO]` | 5/7 | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` |
| `scope.payload` | `scope.Payload [ZERO]` | 8/17 | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` |
| `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 1/3 | `Target` |
| `list.value` | `list.Value [ZERO]` | 2/8 | `List`, `Canonical` |
| `num.value` | `num.Value` | 1/4 | `Output` |
| `stdlib.extra` | `stdlib.Extra [ZERO]` | 1/4 | `PrintHandlerImpl` |
| `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 2/4 | `TestSimpleType`, `TestComplexGen` |
| `dict.value` | `dict.Value [ZERO]` | 3/10 | `Canonical`, `Frozen`, `ContentRef` |
| `alloc.chain` | `alloc.Chain [ZERO]` | 2/5 | `Item`, `ResetSplitAtZeroTest` |
| `stdlib.partial` | `stdlib.Partial [ZERO]` | 2/5 | `Frozen`, `Canonical` |
| `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 3/5 | `Target`, `Item`, `IntoIter` |
| `stdlib.json` | `stdlib.Json` | 1/1 | `Canonical` |
| `layout.vtable` | `layout.Vtable` | 2/6 | `GetTypeId`, `GetAllocativeKey` |
| `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 2/7 | `StaticType`, `Canonical` |
| `profile.bc` | `profile.Bc [ZERO]` | 1/10 | `Data` |
| `alloc.allocator` | `alloc.Allocator [ZERO]` | 1/3 | `Item` |
| `typing.small_arc_vec` | `typing.SmallArcVec` | 1/3 | `Target` |
| `tuple.unpack` | `tuple.Unpack [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `layout.complex` | `layout.Complex [ZERO]` | 3/4 | `Canonical`, `Error`, `Frozen` |
| `bigint.convert` | `bigint.Convert [ZERO]` | 2/2 | `Canonical`, `Error` |
| `compiler.scope` | `compiler.Scope [ZERO]` | 3/20 | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` |
| `bc.instrs` | `bc.Instrs [ZERO]` | 1/4 | `HandlerImpl` |
| `analysis.dubious` | `analysis.Dubious` | 1/2 | `Key` |
| `profile.csv` | `profile.Csv [ZERO]` | 2/3 | `Impl`, `CsvValue` |
| `analysis.types` | `analysis.Types` | 3/5 | `LintWarning`, `LintT`, `EvalSeverity` |
| `heap.send` | `heap.Send [ZERO]` | 3/6 | `Sealed`, `Target`, `StaticType` |
| `list.unpack` | `list.Unpack [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `int.i32` | `int.I32 [ZERO]` | 2/2 | `Canonical`, `Error` |
| `tuple.rust_tuple` | `tuple.RustTuple` | 2/2 | `Canonical`, `Error` |
| `values.owned` | `values.Owned [ZERO]` | 2/5 | `Canonical`, `Target` |
| `profile.time_flame` | `profile.TimeFlame [ZERO]` | 1/11 | `Data` |
| `profile.stmt` | `profile.Stmt [ZERO]` | 1/9 | `Data` |
| `typing.callable_param` | `typing.CallableParam` | 1/6 | `ParamSpecDisplay` |
| `dict.refs` | `dict.Refs [ZERO]` | 3/7 | `Target`, `Canonical`, `Error` |
| `values.unpack` | `values.Unpack [ZERO]` | 4/7 | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` |
| `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 3/3 | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` |
| `typing.iter` | `typing.Iter [ZERO]` | 2/4 | `NonInstantiable`, `Canonical` |
| `bc.addr` | `bc.Addr [ZERO]` | 1/6 | `Output` |
| `avalues.static_` | `avalues.Static [ZERO]` | 3/5 | `StarlarkValue`, `ExtraElem`, `MySimpleValue` |
| `profile.typecheck` | `profile.Typecheck [ZERO]` | 1/5 | `Data` |
| `profile.mode` | `profile.Mode [ZERO]` | 1/2 | `Err` |
| `dict.alloc` | `dict.Alloc [ZERO]` | 1/2 | `Canonical` |
| `structs.alloc` | `structs.Alloc [ZERO]` | 1/2 | `Canonical` |
| `tests.util` | `util.Util [STUB] [PROVENANCE-FALLBACK]` | 1/1 | `TestComplexValue` |
| `profile.heap` | `profile.Heap [ZERO]` | 1/11 | `Data` |
| `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 3/7 | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` |
| `list.refs` | `list.Refs [ZERO]` | 3/5 | `Target`, `Canonical`, `Error` |
| `avalues.list` | `avalues.List [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `bc.opcode` | `bc.Opcode [ZERO]` | 2/5 | `ByNumber`, `FindOpcode` |
| `tuple.refs` | `tuple.Refs [ZERO]` | 2/4 | `Canonical`, `Error` |
| `bc.repr` | `bc.Repr [ZERO]` | 1/3 | `HandlerImpl` |
| `typing.never` | `typing.Never [ZERO]` | 1/3 | `Canonical` |
| `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 2/2 | `Canonical`, `Error` |
| `tuple.alloc` | `tuple.Alloc` | 1/2 | `Canonical` |
| `float.unpack` | `float.Unpack [ZERO]` | 2/3 | `Canonical`, `Error` |
| `dict.unpack` | `dict.Unpack` | 2/3 | `Canonical`, `Error` |
| `compiler.expr` | `compiler.Expr [ZERO]` | 2/11 | `AstLiteralCompile`, `CompilerExprUtil` |
| `values.traits` | `values.Traits [ZERO]` | 1/3 | `Canonical` |
| `compiler.def` | `compiler.Def [ZERO]` | 1/13 | `Frozen` |
| `types.function` | `types.Function [ZERO]` | 1/12 | `Canonical` |
| `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 1/8 | `Output` |
| `avalues.array` | `avalues.Array [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `compiler.args` | `compiler.Args [ZERO]` | 1/2 | `Never` |
| `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 1/3 | `RowKind` |
| `avalues.tuple` | `avalues.Tuple [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `avalues.complex` | `avalues.Complex [ZERO]` | 2/5 | `StarlarkValue`, `ExtraElem` |
| `set.refs` | `set.Refs [ZERO]` | 2/5 | `Canonical`, `Error` |
| `bc.call` | `bc.Call [ZERO]` | 1/5 | `Args` |
| `bc.instr_arg` | `bc.InstrArg [ZERO]` | 1/4 | `HandlerImpl` |
| `typing.callable` | `typing.Callable [ZERO]` | 1/2 | `TyCallableInner` |
| `structs.refs` | `structs.Refs [ZERO]` | 2/4 | `Canonical`, `Error` |
| `bc.bytecode` | `bc.Bytecode [ZERO]` | 1/2 | `HandlerImpl` |
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
| `compiler.assign_modify` | `compiler.AssignModify [ZERO]` | 1/1 | `AssignOnWriteBc` |

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
| 11 | `module.unpack_value` | `tests.derive.module.UnpackValue` | 0 | 13 | 0 | 13 | `src/tests/derive/module/unpack_value.rs` | `tests/derive/module/UnpackValue.kt` |
| 12 | `profile.tests` | `eval.runtime.profile.Tests` | 0 | 13 | 0 | 13 | `src/eval/runtime/profile/tests.rs` | `eval/runtime/profile/Tests.kt` |
| 13 | `type_compiled.tests` | `values.typing.typecompiled.Tests` | 0 | 13 | 0 | 13 | `src/values/typing/type_compiled/tests.rs` | `values/typing/typecompiled/Tests.kt` |
| 14 | `bc.if_stmt` | `tests.bc.IfStmt` | 0 | 12 | 0 | 12 | `src/tests/bc/if_stmt.rs` | `tests/bc/IfStmt.kt` |
| 15 | `tests.basic` | `tests.Basic` | 0 | 12 | 0 | 12 | `src/tests/basic.rs` | `tests/Basic.kt` |
| 16 | `module.named_positional` | `tests.derive.module.NamedPositional` | 0 | 11 | 0 | 11 | `src/tests/derive/module/named_positional.rs` | `tests/derive/module/NamedPositional.kt` |
| 17 | `module.generic` | `tests.derive.module.Generic` | 0 | 8 | 2 | 10 | `src/tests/derive/module/generic.rs` | `tests/derive/module/Generic.kt` |
| 18 | `tests.comprehension` | `tests.Comprehension` | 0 | 10 | 0 | 10 | `src/tests/comprehension.rs` | `tests/Comprehension.kt` |
| 19 | `derive.docs` | `tests.derive.Docs` | 0 | 7 | 2 | 9 | `src/tests/derive/docs.rs` | `tests/derive/Docs.kt` |
| 20 | `module.basic` | `tests.derive.module.Basic` | 0 | 9 | 0 | 9 | `src/tests/derive/module/basic.rs` | `tests/derive/module/Basic.kt` |
| 21 | `tests.type_annot` | `tests.TypeAnnot` | 0 | 9 | 0 | 9 | `src/tests/type_annot.rs` | `tests/TypeAnnot.kt` |
| 22 | `typing.tests.call` | `typing.tests.Call` | 0 | 9 | 0 | 9 | `src/typing/tests/call.rs` | `typing/tests/Call.kt` |
| 23 | `bc.and_or` | `tests.bc.AndOr` | 0 | 8 | 0 | 8 | `src/tests/bc/and_or.rs` | `tests/bc/AndOr.kt` |
| 24 | `opt.def_inline` | `tests.opt.DefInline` | 0 | 8 | 0 | 8 | `src/tests/opt/def_inline.rs` | `tests/opt/DefInline.kt` |
| 25 | `tests.opt` | `tests.opt.Opt` | 0 | 8 | 0 | 8 | `src/tests/opt.rs` | `tests/opt/Opt.kt` |
| 26 | `derive.unpack_value` | `tests.derive.UnpackValue` | 51 | 2 | 5 | 7 | `src/tests/derive/unpack_value.rs` | `tests/derive/UnpackValue.kt` |
| 27 | `util.arc_str` | `util.ArcStr` | 21 | 5 | 2 | 7 | `src/util/arc_str.rs` | `util/ArcStr.kt` |
| 28 | `bc.expr` | `tests.bc.Expr` | 7 | 7 | 0 | 7 | `src/tests/bc/expr.rs` | `tests/bc/Expr.kt` |
| 29 | `opt.eq` | `tests.opt.Eq` | 0 | 7 | 0 | 7 | `src/tests/opt/eq.rs` | `tests/opt/Eq.kt` |
| 30 | `unused_loads.find_tests` | `analysis.unusedloads.FindTests` | 0 | 7 | 0 | 7 | `src/analysis/unused_loads/find_tests.rs` | `analysis/unusedloads/FindTests.kt` |
| 31 | `freeze.bounds` | `tests.derive.freeze.Bounds` | 0 | 3 | 3 | 6 | `src/tests/derive/freeze/bounds.rs` | `tests/derive/freeze/Bounds.kt` |
| 32 | `freeze.identity` | `tests.derive.freeze.Identity` | 0 | 2 | 4 | 6 | `src/tests/derive/freeze/identity.rs` | `tests/derive/freeze/Identity.kt` |
| 33 | `freeze.validator_order` | `tests.derive.freeze.ValidatorOrder` | 0 | 3 | 3 | 6 | `src/tests/derive/freeze/validator_order.rs` | `tests/derive/freeze/ValidatorOrder.kt` |
| 34 | `int.tests` | `values.types.int.Tests` | 0 | 6 | 0 | 6 | `src/values/types/int/tests.rs` | `values/types/int/Tests.kt` |
| 35 | `module.methods` | `tests.derive.module.Methods` | 0 | 5 | 1 | 6 | `src/tests/derive/module/methods.rs` | `tests/derive/module/Methods.kt` |
| 36 | `module.other_attributes` | `tests.derive.module.OtherAttributes` | 0 | 6 | 0 | 6 | `src/tests/derive/module/other_attributes.rs` | `tests/derive/module/OtherAttributes.kt` |
| 37 | `tests.list` | `typing.tests.List` | 0 | 6 | 0 | 6 | `src/typing/tests/list.rs` | `typing/tests/List.kt` |
| 38 | `tests.bc.definitely_assigned` | `tests.bc.DefinitelyAssigned` | 0 | 5 | 0 | 5 | `src/tests/bc/definitely_assigned.rs` | `tests/bc/DefinitelyAssigned.kt` |
| 39 | `bc.compr` | `tests.bc.Compr` | 0 | 4 | 0 | 4 | `src/tests/bc/compr.rs` | `tests/bc/Compr.kt` |
| 40 | `derive.alloc_value` | `tests.derive.AllocValue` | 0 | 0 | 4 | 4 | `src/tests/derive/alloc_value.rs` | `tests/derive/AllocValue.kt` |
| 41 | `freeze.validator` | `tests.derive.freeze.Validator` | 0 | 3 | 1 | 4 | `src/tests/derive/freeze/validator.rs` | `tests/derive/freeze/Validator.kt` |
| 42 | `module.kwargs` | `tests.derive.module.Kwargs` | 0 | 4 | 0 | 4 | `src/tests/derive/module/kwargs.rs` | `tests/derive/module/Kwargs.kt` |
| 43 | `module.return_impl` | `tests.derive.module.ReturnImpl` | 0 | 4 | 0 | 4 | `src/tests/derive/module/return_impl.rs` | `tests/derive/module/ReturnImpl.kt` |
| 44 | `module.type_annotation` | `tests.derive.module.TypeAnnotation` | 0 | 3 | 1 | 4 | `src/tests/derive/module/type_annotation.rs` | `tests/derive/module/TypeAnnotation.kt` |
| 45 | `opt.type_is` | `tests.opt.TypeIs` | 0 | 4 | 0 | 4 | `src/tests/opt/type_is.rs` | `tests/opt/TypeIs.kt` |
| 46 | `tests.freeze_access_value` | `tests.FreezeAccessValue` | 0 | 2 | 2 | 4 | `src/tests/freeze_access_value.rs` | `tests/FreezeAccessValue.kt` |
| 47 | `trace.bounds` | `tests.derive.trace.Bounds` | 0 | 2 | 2 | 4 | `src/tests/derive/trace/bounds.rs` | `tests/derive/trace/Bounds.kt` |
| 48 | `unused_loads.remove_tests` | `analysis.unusedloads.RemoveTests` | 0 | 4 | 0 | 4 | `src/analysis/unused_loads/remove_tests.rs` | `analysis/unusedloads/RemoveTests.kt` |
| 49 | `bc.for_stmt` | `tests.bc.ForStmt` | 0 | 3 | 0 | 3 | `src/tests/bc/for_stmt.rs` | `tests/bc/ForStmt.kt` |
| 50 | `derive.attrs` | `tests.derive.Attrs` | 0 | 1 | 2 | 3 | `src/tests/derive/attrs.rs` | `tests/derive/Attrs.kt` |
| 51 | `module.default_value` | `tests.derive.module.DefaultValue` | 0 | 3 | 0 | 3 | `src/tests/derive/module/default_value.rs` | `tests/derive/module/DefaultValue.kt` |
| 52 | `module.special_params` | `tests.derive.module.SpecialParams` | 0 | 3 | 0 | 3 | `src/tests/derive/module/special_params.rs` | `tests/derive/module/SpecialParams.kt` |
| 53 | `opt.speculative_exec` | `tests.opt.SpeculativeExec` | 0 | 3 | 0 | 3 | `src/tests/opt/speculative_exec.rs` | `tests/opt/SpeculativeExec.kt` |
| 54 | `tests.go` | `tests.Go` | 0 | 3 | 0 | 3 | `src/tests/go.rs` | `tests/Go.kt` |
| 55 | `tests.types` | `typing.tests.Types` | 0 | 3 | 0 | 3 | `src/typing/tests/types.rs` | `typing/tests/Types.kt` |
| 56 | `bc.golden` | `tests.bc.Golden` | 0 | 2 | 0 | 2 | `src/tests/bc/golden.rs` | `tests/bc/Golden.kt` |
| 57 | `derive.unpack_value_attr` | `tests.derive.UnpackValueAttr` | 0 | 0 | 2 | 2 | `src/tests/derive/unpack_value_attr.rs` | `tests/derive/UnpackValueAttr.kt` |
| 58 | `opt.constant_folding` | `tests.opt.ConstantFolding` | 0 | 2 | 0 | 2 | `src/tests/opt/constant_folding.rs` | `tests/opt/ConstantFolding.kt` |
| 59 | `opt.list_add` | `tests.opt.ListAdd` | 0 | 2 | 0 | 2 | `src/tests/opt/list_add.rs` | `tests/opt/ListAdd.kt` |
| 60 | `opt.types` | `tests.opt.Types` | 0 | 2 | 0 | 2 | `src/tests/opt/types.rs` | `tests/opt/Types.kt` |
| 61 | `tests.callable` | `typing.tests.Callable` | 0 | 2 | 0 | 2 | `src/typing/tests/callable.rs` | `typing/tests/Callable.kt` |
| 62 | `tests.special_function` | `typing.tests.SpecialFunction` | 0 | 2 | 0 | 2 | `src/typing/tests/special_function.rs` | `typing/tests/SpecialFunction.kt` |
| 63 | `tests.tuple` | `typing.tests.Tuple` | 0 | 2 | 0 | 2 | `src/typing/tests/tuple.rs` | `typing/tests/Tuple.kt` |
| 64 | `trace.statics` | `tests.derive.trace.Statics` | 0 | 0 | 2 | 2 | `src/tests/derive/trace/statics.rs` | `tests/derive/trace/Statics.kt` |
| 65 | `tests.before_stmt` | `tests.BeforeStmt` | 1 | 1 | 0 | 1 | `src/tests/before_stmt.rs` | `tests/BeforeStmt.kt` |
| 66 | `bc.isinstance` | `tests.bc.Isinstance` | 0 | 1 | 0 | 1 | `src/tests/bc/isinstance.rs` | `tests/bc/Isinstance.kt` |
| 67 | `freeze.basic` | `tests.derive.freeze.Basic` | 0 | 0 | 1 | 1 | `src/tests/derive/freeze/basic.rs` | `tests/derive/freeze/Basic.kt` |
| 68 | `freeze.enums` | `tests.derive.freeze.Enums` | 0 | 0 | 1 | 1 | `src/tests/derive/freeze/enums.rs` | `tests/derive/freeze/Enums.kt` |
| 69 | `tests.bc.call` | `tests.bc.Call` | 0 | 1 | 0 | 1 | `src/tests/bc/call.rs` | `tests/bc/Call.kt` |
| 70 | `tests.for_loop` | `tests.ForLoop` | 0 | 1 | 0 | 1 | `src/tests/for_loop.rs` | `tests/ForLoop.kt` |
| 71 | `tests.replace_binary` | `tests.ReplaceBinary` | 0 | 1 | 0 | 1 | `src/tests/replace_binary.rs` | `tests/ReplaceBinary.kt` |
| 72 | `trace.enums` | `tests.derive.trace.Enums` | 0 | 0 | 1 | 1 | `src/tests/derive/trace/enums.rs` | `tests/derive/trace/Enums.kt` |
| 73 | `assert` | `assert.Assert` | 84 | 0 | 0 | 0 | `src/assert.rs` | `assert/Assert.kt` |
| 74 | `tests` | `tests.Tests` | 1 | 0 | 0 | 0 | `src/tests.rs` | `tests/Tests.kt` |
| 75 | `analysis.unused_loads` | `analysis.unusedloads.UnusedLoads` | 0 | 0 | 0 | 0 | `src/analysis/unused_loads.rs` | `analysis/unusedloads/UnusedLoads.kt` |
| 76 | `derive.freeze` | `tests.derive.freeze.Freeze` | 0 | 0 | 0 | 0 | `src/tests/derive/freeze.rs` | `tests/derive/freeze/Freeze.kt` |
| 77 | `derive.trace` | `tests.derive.trace.Trace` | 0 | 0 | 0 | 0 | `src/tests/derive/trace.rs` | `tests/derive/trace/Trace.kt` |
| 78 | `docs.tests` | `docs.tests.Tests` | 0 | 0 | 0 | 0 | `src/docs/tests.rs` | `docs/tests/Tests.kt` |
| 79 | `tests.bc` | `tests.bc.Bc` | 0 | 0 | 0 | 0 | `src/tests/bc.rs` | `tests/bc/Bc.kt` |
| 80 | `tests.derive` | `tests.derive.Derive` | 0 | 0 | 0 | 0 | `src/tests/derive.rs` | `tests/derive/Derive.kt` |

## Documentation Gaps

There is missing documentation that is hurting overall scoring.

**Documentation coverage:** 10101 / 13812 lines (73%)

Documentation gaps (>20%), complete list:

- `values.traits` - 79% gap (1072 → 222 lines)
- `lib` - 91% gap (774 → 67 lines)
- `string.methods` - 42% gap (1112 → 642 lines)
- `dict.methods` - 59% gap (396 → 162 lines)
- `heap.branding` - 100% gap (168 → 0 lines)
- `values.unpack` - 80% gap (176 → 35 lines)
- `funcs.other` - 37% gap (364 → 228 lines)
- `values.owned` - 73% gap (180 → 48 lines)
- `docs.parse` - 63% gap (174 → 64 lines)
- `set.methods` - 39% gap (258 → 158 lines)
- `types.any` - 78% gap (124 → 27 lines)
- `heap.send` - 49% gap (184 → 94 lines)
- `macros` - 74% gap (116 → 30 lines)
- `params.spec` - 49% gap (172 → 87 lines)
- `list.methods` - 27% gap (306 → 224 lines)
- `types.starlark_value_as_type` - 100% gap (74 → 0 lines)
- `runtime.evaluator` - 37% gap (186 → 118 lines)
- `assert.assert` - 32% gap (204 → 138 lines)
- `values.alloc_value` - 44% gap (138 → 77 lines)
- `string.globals` - 42% gap (130 → 76 lines)
- `heap.repr` - 71% gap (68 → 20 lines)
- `heap.heap_type` - 26% gap (170 → 126 lines)
- `any` - 35% gap (120 → 78 lines)
- `dict.alloc` - 100% gap (42 → 0 lines)
- `docs` - 40% gap (98 → 59 lines)
- `heap.fast_cell` - 100% gap (38 → 0 lines)
- `bc.writer` - 40% gap (94 → 56 lines)
- `thin_box_slice_frozen_value.thin_box` - 70% gap (54 → 16 lines)
- `debug.adapter` - 21% gap (182 → 144 lines)
- `dict.globals` - 58% gap (60 → 25 lines)
- `types.array` - 55% gap (62 → 28 lines)
- `typed.string` - 55% gap (60 → 27 lines)
- `int.globals` - 34% gap (96 → 63 lines)
- `scope.payload` - 89% gap (36 → 4 lines)
- `allocator.api` - 100% gap (32 → 0 lines)
- `values.owned_frozen_ref` - 62% gap (52 → 20 lines)
- `heap.arena` - 62% gap (50 → 19 lines)
- `dict.value` - 52% gap (60 → 29 lines)
- `pagable.vtable_register` - 52% gap (60 → 29 lines)
- `values.type_repr` - 42% gap (74 → 43 lines)
- `analysis.types` - 54% gap (52 → 24 lines)
- `values` - 100% gap (28 → 0 lines)
- `tuple.refs` - 100% gap (26 → 0 lines)
- `pagable` - 100% gap (26 → 0 lines)
- `environment.methods` - 25% gap (102 → 76 lines)
- `bool.globals` - 50% gap (50 → 25 lines)
- `record.globals` - 33% gap (76 → 51 lines)
- `types.function` - 48% gap (50 → 26 lines)
- `bc.definitely_assigned` - 43% gap (54 → 31 lines)
- `range.globals` - 36% gap (64 → 41 lines)
- `eval.bc.compiler.expr` - 77% gap (30 → 7 lines)
- `stdlib.extra` - 46% gap (50 → 27 lines)
- `coerce` - 42% gap (52 → 30 lines)
- `runtime.file_loader` - 100% gap (22 → 0 lines)
- `profile.mode` - 52% gap (40 → 19 lines)
- `collections.string_pool` - 100% gap (20 → 0 lines)
- `typing.basic` - 42% gap (48 → 28 lines)
- `values.typing.callable` - 43% gap (46 → 26 lines)
- `stdlib` - 26% gap (72 → 53 lines)
- `compiler.if_compiler` - 41% gap (44 → 26 lines)
- `funcs.zip` - 50% gap (36 → 18 lines)
- `type_compiled.matcher` - 47% gap (38 → 20 lines)
- `types.any_complex` - 82% gap (22 → 4 lines)
- `type_compiled.alloc` - 38% gap (48 → 30 lines)
- `layout.pointer` - 53% gap (34 → 16 lines)
- `dict.refs` - 94% gap (18 → 1 lines)
- `tuple.alloc` - 57% gap (30 → 13 lines)
- `avalues.simple` - 61% gap (28 → 11 lines)
- `bc.if_debug` - 45% gap (38 → 21 lines)
- `profile.aggregated` - 30% gap (56 → 39 lines)
- `types.record` - 32% gap (50 → 34 lines)
- `bc.for_loop` - 100% gap (16 → 0 lines)
- `environment.names` - 36% gap (44 → 28 lines)
- `compiler.def_inline` - 62% gap (26 → 10 lines)
- `values.freeze` - 27% gap (56 → 41 lines)
- `avalues.str_` - 68% gap (22 → 7 lines)
- `type_compiled.globals` - 44% gap (34 → 19 lines)
- `float.globals` - 26% gap (54 → 40 lines)
- `runtime.slots` - 35% gap (40 → 26 lines)
- `typing.user` - 29% gap (48 → 34 lines)
- `type_compiled.compiled` - 39% gap (36 → 22 lines)
- `profile.summary_by_function` - 43% gap (30 → 17 lines)
- `typing.fill_types_for_lint` - 34% gap (38 → 25 lines)
- `oracle.traits` - 34% gap (38 → 25 lines)
- `docs.multipage` - 26% gap (46 → 34 lines)
- `values.iter` - 100% gap (12 → 0 lines)
- `types.bool` - 100% gap (12 → 0 lines)
- `typing.error` - 100% gap (12 → 0 lines)
- `types.enumeration` - 32% gap (38 → 26 lines)
- `docs.markdown` - 33% gap (36 → 24 lines)
- `profile.time_flame` - 43% gap (28 → 16 lines)
- `typing.bindings` - 40% gap (30 → 18 lines)
- `types.structs` - 31% gap (36 → 25 lines)
- `params.display` - 50% gap (22 → 11 lines)
- `runtime.cheap_call_stack` - 42% gap (26 → 15 lines)
- `bc.instr_impl` - 34% gap (32 → 21 lines)
- `enumeration.globals` - 27% gap (40 → 29 lines)
- `num.globals` - 45% gap (22 → 12 lines)
- `bc.opcode` - 50% gap (20 → 10 lines)
- `types.int` - 100% gap (10 → 0 lines)
- `values.frozen_ref` - 50% gap (20 → 10 lines)
- `callable.param` - 45% gap (22 → 12 lines)
- `tuple.globals` - 42% gap (24 → 14 lines)
- `values.freeze_error` - 33% gap (30 → 20 lines)
- `stdlib.call_stack` - 25% gap (36 → 27 lines)
- `thin_box_slice_frozen_value.packed_impl` - 38% gap (24 → 15 lines)
- `compiler.stmt` - 26% gap (34 → 25 lines)
- `__derive_refs.param_spec` - 80% gap (10 → 2 lines)
- `dict.dict_type` - 100% gap (8 → 0 lines)
- `string.interpolation` - 25% gap (32 → 24 lines)
- `heap.call_enter_exit` - 100% gap (8 → 0 lines)
- `eval.soft_error` - 100% gap (8 → 0 lines)
- `string.str_type` - 31% gap (26 → 18 lines)
- `values.stack_guard` - 33% gap (24 → 16 lines)
- `alloc.chunk` - 58% gap (12 → 5 lines)
- `runtime.inlined_frame` - 27% gap (26 → 19 lines)
- `values.recursive_repr_or_json_guard` - 50% gap (14 → 7 lines)
- `values.value_of` - 44% gap (16 → 9 lines)
- `tuple.value` - 39% gap (18 → 11 lines)
- `types.known_methods` - 38% gap (16 → 10 lines)
- `__derive_refs.parse_args` - 38% gap (16 → 10 lines)
- `alloc.chunk_part` - 50% gap (12 → 6 lines)
- `profile.data` - 50% gap (12 → 6 lines)
- `none.none_type` - 100% gap (6 → 0 lines)
- `num.typecheck` - 50% gap (12 → 6 lines)
- `oracle.ctx` - 30% gap (20 → 14 lines)
- `assert.conformance` - 30% gap (20 → 14 lines)
- `compiler.call` - 38% gap (16 → 10 lines)
- `record.record_type` - 75% gap (8 → 2 lines)
- `typing.function` - 50% gap (12 → 6 lines)
- `compiler.compr` - 38% gap (16 → 10 lines)
- `layout.freezer` - 38% gap (16 → 10 lines)
- `pagable.error` - 75% gap (8 → 2 lines)
- `heap.maybe_uninit_slice_util` - 43% gap (14 → 8 lines)
- `values.index` - 27% gap (22 → 16 lines)
- `types.unbound` - 50% gap (10 → 5 lines)
- `profile.csv` - 50% gap (10 → 5 lines)
- `compiler.known` - 42% gap (12 → 7 lines)
- `bc.slow_arg` - 36% gap (14 → 9 lines)
- `profile.flamegraph` - 36% gap (14 → 9 lines)
- `params.parser` - 36% gap (14 → 9 lines)
- `typing.interface` - 50% gap (8 → 4 lines)
- `profile.by_type` - 25% gap (16 → 12 lines)
- `enumeration.ty_enum_type` - 50% gap (8 → 4 lines)
- `range.range_type` - 50% gap (8 → 4 lines)
- `cast` - 50% gap (8 → 4 lines)
- `values.error` - 50% gap (8 → 4 lines)
- `bool.value` - 67% gap (6 → 2 lines)
- `debug.evaluate` - 33% gap (12 → 8 lines)
- `compiler.expr_bool` - 38% gap (8 → 5 lines)
- `unused_loads.find` - 25% gap (12 → 9 lines)
- `types.list_or_tuple` - 50% gap (6 → 3 lines)
- `avalues.list` - 30% gap (10 → 7 lines)
- `typing.structs` - 30% gap (10 → 7 lines)
- `collections.aligned_padded_str` - 38% gap (8 → 5 lines)
- `layout.value_captured` - 25% gap (8 → 6 lines)
- `typing.typecheck` - 33% gap (6 → 4 lines)
- `types.any_array` - 33% gap (6 → 4 lines)

