# Code Port - Progress Report

**Generated:** 2026-10-17
**Source:** tmp/starlark/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/starlark

## Executive Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| Function parity | 3911/5428 matched (target 5344) | 72.1% |
| Class/type parity | 809/1209 matched (target 1553) | 66.9% |
| Combined symbol parity | 4720/6637 matched (target 6897) | 71.1% |
| Average function body similarity | 0.12 | inline-code cosine |
| Average documentation similarity | 0.64 | doc text cosine |
| Missing source functions | 570 | 0% parity until ported |
| Missing source classes/types | 78 | 0% parity until ported |
| Missing source symbol files | 80 | 648 symbols |
| Cheat/scoring failures | 214 | forced to 0% |
| Total source files | 468 | 100% |
| Target units (paired) | 615 | - |
| Target files (total) | 615 | - |
| Porting progress | 354 | 75.6% (matched) |
| Missing files | 114 | 24.4% |

## Port Quality Analysis

**Average Function Similarity:** 0.12

Similarity in this report is the required function-by-function body/parameter score. Class/type parity and symbol deficits are reported beside it; whole-file shape is diagnostic only.

**Work Distribution:**
- Critical (<0.60): 343 files (96.9% of matched)
- Needs review (0.60-0.84): 1 files (0.3% of matched)
- Excellent (>=0.85): 10 files (2.8% of matched)

## Worst Function Scores First

Every matched file is listed from lowest function body/parameter similarity upward. Missing symbol names are not capped.

| Rank | Source | Target | Function similarity | Functions | Missing functions | Types | Missing types | Tests | Symbol deficit | Priority |
|------|--------|--------|---------------------|-----------|-------------------|-------|---------------|-------|----------------|----------|
| 1 | `typing.starlark_value` | `typing.StarlarkValue [ZERO]` | 0.00 | 29/35 matched (target 45) | `fmt`, `fmt`, `eq`, `hash`, `partial_cmp`, `cmp` | 4/4 matched (target 5) | _none_ | - | 6 | 76063912.0 |
| 2 | `runtime.evaluator` | `runtime.Evaluator [ZERO]` | 0.00 | 60/64 matched (target 63) | `drop`, `new`, `add_diagnostics`, `before_stmt` | 7/7 matched (target 17) | _none_ | - | 4 | 55047112.0 |
| 3 | `values.trace` | `values.Trace [ZERO]` | 0.00 | 43/43 matched | _none_ | 1/1 matched | _none_ | - | 0 | 52004408.0 |
| 4 | `values.freeze` | `values.Freeze [ZERO]` | 0.00 | 23/26 matched (target 31) | `freeze`, `freeze`, `freeze` | 1/2 matched (target 6) | `Frozen` | - | 4 | 42042808.0 |
| 5 | `values.alloc_value` | `values.AllocValue [ZERO]` | 0.00 | 5/5 matched | _none_ | 4/4 matched | _none_ | - | 0 | 42000912.0 |
| 6 | `layout.freezer` | `layout.Freezer [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched | _none_ | - | 0 | 36000608.0 |
| 7 | `values.frozen_ref` | `values.FrozenRef [ZERO]` | 0.00 | 20/20 matched (target 23) | _none_ | 2/4 matched (target 2) | `Target`, `Frozen` | - | 2 | 27022410.0 |
| 8 | `none.none_type` | `none.NoneType [ZERO]` | 0.00 | 11/11 matched (target 16) | _none_ | 1/2 matched | `Error` | - | 1 | 27011310.0 |
| 9 | `runtime.arguments` | `runtime.Arguments [ZERO]` | 0.00 | 37/40 matched (target 55) | `get_index_from_param_spec`, `small_hash`, `f` | 8/8 matched (target 17) | _none_ | 3/4 | 3 | 25034810.0 |
| 10 | `typing.type_compiled` | `type_compiled.TypeCompiled [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 22000010.0 |
| 11 | `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 0.00 | 15/20 matched (target 24) | `fmt`, `fmt`, `assert_send_sync`, `starlark_type_repr`, `_assert_covariant` | 4/7 matched (target 8) | `Canonical`, `Frozen`, `Error` | 3/6 | 8 | 20082710.0 |
| 12 | `environment.globals` | `environment.Globals [ZERO]` | 0.00 | 34/39 matched (target 42) | `get`, `new`, `test_send_sync`, `register_foo`, `foo` | 5/5 matched (target 7) | _none_ | 1/5 | 5 | 20054410.0 |
| 13 | `environment.methods` | `environment.Methods [ZERO]` | 0.00 | 19/21 matched (target 26) | `new`, `get_methods` | 4/4 matched (target 5) | _none_ | 1/2 | 2 | 17022510.0 |
| 14 | `values.iter` | `values.Iter [ZERO]` | 0.00 | 4/5 matched (target 6) | `drop` | 1/2 matched (target 1) | `Item` | - | 2 | 17020710.0 |
| 15 | `collections.symbol` | `collections.Symbol [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 15000010.0 |
| 16 | `private` | `starlark.Private [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 15000110.0 |
| 17 | `layout.avalue` | `layout.AValue [ZERO]` | 0.00 | 8/8 matched (target 13) | _none_ | 3/3 matched (target 4) | _none_ | 2/2 | 0 | 14001110.0 |
| 18 | `typing.tuple` | `typing.Tuple [ZERO]` | 0.00 | 5/6 matched (target 9) | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 12010710.0 |
| 19 | `layout.const_frozen_string` | `layout.ConstFrozenString [ZERO]` | 0.00 | 2/2 matched (target 3) | _none_ | 0/0 matched (target 1) | _none_ | 2/2 | 0 | 12000210.0 |
| 20 | `int.inline_int` | `int.InlineInt [ZERO]` | 0.00 | 29/42 matched (target 43) | `fmt`, `testing_new`, `try_from_impl`, `try_from`, `try_from`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `eq`, `partial_cmp`, `test_min_max_for_bits` | 2/5 matched (target 6) | `Error`, `Output`, `Canonical` | 0/2 | 16 | 11164710.0 |
| 21 | `int.pointer_i32` | `int.PointerI32 [ZERO]` | 0.00 | 28/32 matched (target 34) | `eq`, `fmt`, `fmt`, `serialize` | 1/2 matched | `Canonical` | - | 5 | 9053410.0 |
| 22 | `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 0.00 | 6/13 matched (target 15) | `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub` | 1/2 matched (target 1) | `Output` | 0/2 | 8 | 8081510.0 |
| 23 | `cast` | `starlark.Cast [ZERO]` | 0.00 | 3/3 matched (target 4) | _none_ | 0/0 matched | _none_ | - | 0 | 8000310.0 |
| 24 | `eval.compiler` | `eval.Compiler [ZERO]` | 0.00 | 6/6 matched | _none_ | 1/1 matched | _none_ | - | 0 | 8000710.0 |
| 25 | `types.bigint` | `types.Bigint [ZERO]` | 0.00 | 71/73 matched (target 77) | `unpack_integer`, `eq` | 1/1 matched (target 2) | _none_ | 42/42 | 2 | 7027410.0 |
| 26 | `runtime.frozen_file_span` | `runtime.FrozenFileSpan [ZERO]` | 0.00 | 9/10 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 7011110.0 |
| 27 | `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 0.00 | 4/10 matched (target 9) | `from_millis`, `add_assign`, `add_assign`, `add`, `add`, `sum` | 1/2 matched (target 1) | `Output` | 0/1 | 7 | 6071210.0 |
| 28 | `dict.dict_type` | `dict.DictType [ZERO]` | 0.00 | 1/2 matched (target 4) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 6030510.0 |
| 29 | `typing.typecheck` | `typing.Typecheck [STUB]` | 0.00 | 2/5 matched | `fmt`, `find_bindings_by_name`, `find_first_binding` | 2/2 matched (target 3) | _none_ | 0/2 | 3 | 6030710.0 |
| 30 | `values.freeze_error` | `values.FreezeError [ZERO]` | 0.00 | 3/5 matched (target 6) | `from`, `from` | 3/4 matched (target 3) | `FreezeResult` | - | 3 | 6030910.0 |
| 31 | `layout.value_alloc_size` | `layout.ValueAllocSize [ZERO]` | 0.00 | 4/5 matched | `layout` | 1/1 matched | _none_ | - | 1 | 6010610.0 |
| 32 | `compiler.stmt` | `compiler.Stmt [ZERO]` | 0.00 | 28/28 matched | _none_ | 7/7 matched (target 24) | _none_ | - | 0 | 6003510.0 |
| 33 | `values.layout` | `values.Layout [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 6000010.0 |
| 34 | `types.array` | `types.Array [ZERO]` | 0.00 | 23/34 matched (target 24) | `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `fmt`, `is_special`, `serialize`, `debug`, `display`, `push` | 2/2 matched | _none_ | 0/3 | 11 | 5113610.0 |
| 35 | `eval.bc` | `bc.Bc [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 5000010.0 |
| 36 | `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 0.00 | 33/38 matched (target 37) | `get_or_init_ty`, `get_ty`, `fmt`, `r#type`, `values` | 4/8 matched (target 7) | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` | 12/12 | 9 | 4094610.0 |
| 37 | `bc.frame` | `bc.Frame [ZERO]` | 0.00 | 25/33 matched (target 31) | `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit` | 2/2 matched | _none_ | - | 8 | 4083510.0 |
| 38 | `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 0.00 | 9/16 matched (target 23) | `fmt`, `fmt`, `fmt`, `new`, `alloc_value`, `compiler_args_globals`, `compiler_args` | 3/4 matched | `Canonical` | 3/6 | 8 | 4082010.0 |
| 39 | `values.value_of` | `values.ValueOf [ZERO]` | 0.00 | 4/6 matched (target 5) | `deref`, `fmt` | 1/4 matched (target 1) | `Target`, `Canonical`, `Error` | - | 5 | 4051010.0 |
| 40 | `alloc.chunk` | `alloc.Chunk [ZERO]` | 0.00 | 12/21 matched (target 20) | `fmt`, `begin`, `fmt`, `ptr_eq`, `begin`, `drop`, `clone`, `counter_overflow`, `test_empty` | 2/3 matched | `ChunkDataEmpty` | 1/3 | 10 | 3102410.0 |
| 41 | `record.record_type` | `record.RecordType [ZERO]` | 0.00 | 20/24 matched (target 23) | `get_or_init_ty`, `get_ty`, `fmt`, `r#type` | 2/8 matched (target 3) | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` | 5/5 | 10 | 3103210.0 |
| 42 | `list.alloc` | `list.Alloc [ZERO]` | 0.00 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 3040510.0 |
| 43 | `compiler.constants` | `compiler.Constants [ZERO]` | 0.00 | 2/4 matched (target 6) | `eq`, `eq` | 2/2 matched (target 3) | _none_ | 1/1 | 2 | 3020610.0 |
| 44 | `profile.instant` | `profile.Instant [ZERO]` | 0.00 | 3/4 matched (target 9) | `sub` | 1/2 matched (target 1) | `Output` | - | 2 | 3020610.0 |
| 45 | `stdlib.call_stack` | `stdlib.CallStack [ZERO]` | 0.00 | 11/13 matched (target 15) | `fmt`, `global` | 1/1 matched (target 2) | _none_ | 4/4 | 2 | 3021410.0 |
| 46 | `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/3 matched (target 1) | `Canonical`, `Error` | - | 2 | 3020510.0 |
| 47 | `errors.did_you_mean` | `errors.DidYouMean [ZERO]` | 0.00 | 6/6 matched (target 7) | _none_ | 0/0 matched (target 1) | _none_ | 5/5 | 0 | 3000610.0 |
| 48 | `sealed` | `starlark.Sealed [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 3000110.0 |
| 49 | `types.record` | `types.Record [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 3000010.0 |
| 50 | `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 0.00 | 4/11 matched (target 9) | `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter` | 1/4 matched (target 3) | `Target`, `Item`, `IntoIter` | - | 10 | 2101510.0 |
| 51 | `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 0.00 | 16/19 matched | `fmt`, `fmt`, `deref` | 2/3 matched (target 2) | `Target` | - | 4 | 2042210.0 |
| 52 | `layout.const_type_id` | `layout.ConstTypeId [ZERO]` | 0.00 | 2/5 matched (target 4) | `fmt`, `eq`, `hash` | 1/1 matched | _none_ | - | 3 | 2030610.0 |
| 53 | `avalues.str_` | `avalues.Str [ZERO]` | 0.00 | 13/13 matched (target 15) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | - | 2 | 2021610.0 |
| 54 | `runtime.rust_loc` | `runtime.RustLoc [ZERO]` | 0.00 | 1/3 matched (target 4) | `rust_loc_globals`, `invoke` | 0/0 matched (target 1) | _none_ | 1/3 | 2 | 2020310.0 |
| 55 | `values.stack_guard` | `values.StackGuard [ZERO]` | 0.00 | 3/4 matched | `drop` | 1/1 matched | _none_ | - | 1 | 2010510.0 |
| 56 | `collections.string_pool` | `collections.StringPool [ZERO]` | 0.00 | 2/2 matched | _none_ | 1/1 matched | _none_ | - | 0 | 2000310.0 |
| 57 | `def_inline.local_as_value` | `def_inline.LocalAsValue [ZERO]` | 0.00 | 1/1 matched (target 2) | _none_ | 1/1 matched | _none_ | - | 0 | 2000210.0 |
| 58 | `profile.string_index` | `profile.StringIndex [ZERO]` | 0.00 | 2/2 matched | _none_ | 2/2 matched | _none_ | - | 0 | 2000410.0 |
| 59 | `values.thin_box_slice_frozen_value` | `values.ThinBoxSliceFrozenValue [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000010.0 |
| 60 | `heap.arena` | `heap.Arena [ZERO]` | 0.00 | 18/37 matched (target 23) | `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty` | 4/7 matched (target 6) | `ChunkIter`, `Item`, `ArenaUninit` | 0/7 | 22 | 1224410.0 |
| 61 | `collections.alloca` | `collections.Alloca [ZERO]` | 0.00 | 5/23 matched (target 5) | `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `drop`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat` | 1/4 matched (target 1) | `Buffer`, `Align`, `DropSliceGuard` | 0/6 | 21 | 1212710.0 |
| 62 | `stdlib` | `starlark.Stdlib [ZERO]` | 0.00 | 5/14 matched (target 16) | `global`, `nop`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2` | 2/3 matched | `Error` | 2/11 | 10 | 1101710.0 |
| 63 | `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 0.00 | 1/7 matched (target 5) | `default`, `starlark_type_repr`, `into_iter`, `into_iter`, `into_iter`, `test_unpack` | 1/5 matched (target 1) | `Canonical`, `Error`, `Item`, `IntoIter` | 0/1 | 10 | 1101210.0 |
| 64 | `layout.pointer` | `layout.Pointer [ZERO]` | 0.00 | 39/47 matched (target 46) | `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `unpack_pointer_i32_unchecked`, `test_int_tag`, `check` | 5/5 matched | _none_ | 0/1 | 8 | 1085210.0 |
| 65 | `types.any_complex` | `types.AnyComplex [ZERO]` | 0.00 | 5/8 matched (target 9) | `fmt`, `fmt`, `freeze` | 3/5 matched (target 4) | `Canonical`, `Frozen` | 1/2 | 5 | 1051310.0 |
| 66 | `types.any_array` | `types.AnyArray [ZERO]` | 0.00 | 5/8 matched (target 9) | `fmt`, `drop`, `drop` | 2/3 matched | `Canonical` | 2/3 | 4 | 1041110.0 |
| 67 | `util.rtabort` | `util.Rtabort [ZERO]` | 0.00 | 3/6 matched | `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args` | 0/1 matched | `AbortOnDrop` | 1/3 | 4 | 1040710.0 |
| 68 | `bc.if_debug` | `bc.IfDebug [ZERO]` | 0.00 | 5/8 matched (target 9) | `eq`, `partial_cmp`, `cmp` | 1/1 matched | _none_ | - | 3 | 1030910.0 |
| 69 | `read_line` | `starlark.ReadLine [ZERO]` | 0.00 | 2/4 matched (target 2) | `new`, `read_line` | 1/2 matched (target 1) | `NoRustyline` | - | 3 | 1030610.0 |
| 70 | `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 0.00 | 1/3 matched (target 1) | `get_type_id`, `test_non_static_type_id` | 0/1 matched (target 0) | `NonStaticAny` | 0/1 | 3 | 1030410.0 |
| 71 | `avalues.simple` | `avalues.Simple [ZERO]` | 0.00 | 9/9 matched (target 11) | _none_ | 1/3 matched (target 1) | `StarlarkValue`, `ExtraElem` | - | 2 | 1021210.0 |
| 72 | `record.field` | `record.Field [ZERO]` | 0.00 | 4/5 matched (target 10) | `fmt` | 0/1 matched | `FieldGen` | - | 2 | 1020610.0 |
| 73 | `runtime.cheap_call_stack` | `runtime.CheapCallStack [ZERO]` | 0.00 | 15/17 matched | `fmt`, `default` | 3/3 matched (target 6) | _none_ | - | 2 | 1022010.0 |
| 74 | `heap.fast_cell` | `heap.FastCell [ZERO]` | 0.00 | 6/7 matched | `drop` | 1/1 matched | _none_ | - | 1 | 1010810.0 |
| 75 | `structs.unordered_hasher` | `structs.UnorderedHasher [ZERO]` | 0.00 | 4/5 matched (target 4) | `_write` | 1/1 matched (target 2) | _none_ | 1/1 | 1 | 1010610.0 |
| 76 | `typing.bindings` | `typing.Bindings [STUB]` | 0.00 | 7/8 matched (target 18) | `get_for_clause` | 3/3 matched (target 18) | _none_ | - | 1 | 1011110.0 |
| 77 | `string.interpolation` | `string.Interpolation [ZERO]` | 0.00 | 12/12 matched (target 14) | _none_ | 4/4 matched (target 21) | _none_ | 8/8 | 0 | 1001610.0 |
| 78 | `types.int` | `types.Int [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 79 | `typing` | `starlark.Typing [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000010.0 |
| 80 | `typing.function` | `typing.Function [STUB]` | 0.00 | 17/17 matched (target 24) | _none_ | 3/3 matched | _none_ | - | 0 | 1002010.0 |
| 81 | `set.methods` | `set.Methods [STUB]` | 0.00 | 18/68 matched (target 19) | `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self` | 1/1 matched (target 3) | _none_ | 0/50 | 50 | 506910.0 |
| 82 | `string.str_type` | `string.StrType [ZERO]` | 0.00 | 12/50 matched (target 36) | `freeze`, `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `fmt`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `get_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_str` | 0/4 matched (target 1) | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` | 10/11 | 42 | 425410.0 |
| 83 | `int.int_or_big` | `int.IntOrBig [STUB]` | 0.00 | 37/62 matched (target 68) | `starlark_type_repr`, `from_str`, `starlark_type_repr`, `unpack_value_impl`, `from`, `from`, `bitand`, `bitor`, `bitxor`, `neg`, `neg`, `add`, `sub`, `mul`, `mul`, `mul`, `partial_cmp`, `cmp`, `eq`, `partial_cmp`, `eq`, `partial_cmp`, `int`, `floor_div`, `percent` | 3/7 matched (target 12) | `Canonical`, `Err`, `Error`, `Output` | 8/11 | 29 | 296910.0 |
| 84 | `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 0.00 | 10/29 matched (target 15) | `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit` | 1/3 matched (target 2) | `ThinBoxSliceLayout`, `Target` | 4/4 | 21 | 213210.0 |
| 85 | `layout.typed` | `layout.ValueTyped [ZERO]` | 0.00 | 36/51 matched (target 49) | `fmt`, `fmt`, `fmt`, `fmt`, `serialize`, `serialize`, `eq`, `eq`, `deref`, `deref`, `unpack_value_impl`, `unpack_value_impl`, `module`, `mutable`, `takes_frozen_value_typed` | 2/7 matched (target 3) | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` | 2/5 | 20 | 205810.0 |
| 86 | `values.typing.callable` | `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 0.00 | 30/47 matched (target 43) | `clone`, `fmt`, `trace`, `fmt`, `_assert_sync_send`, `_assert`, `clone`, `trace`, `clone`, `fmt`, `trace`, `my_module`, `accept_f`, `module`, `accept_f`, `good`, `bad` | 5/8 matched (target 7) | `Canonical`, `Error`, `Frozen` | 10/16 | 20 | 205510.0 |
| 87 | `scope.payload` | `scope.Payload [ZERO]` | 0.00 | 0/7 matched (target 0) | `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id` | 9/17 matched (target 14) | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` | - | 15 | 152410.0 |
| 88 | `typing.user` | `typing.User [ZERO]` | 0.00 | 15/30 matched (target 37) | `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `get_type_starlark_repr`, `typechecker_ty`, `eval_type`, `invoke`, `alloc_value`, `get_type_starlark_repr`, `globals`, `fruit`, `mk_fruit` | 8/8 matched (target 12) | _none_ | 2/13 | 15 | 153810.0 |
| 89 | `typed.string` | `typed.String [ZERO]` | 0.00 | 14/26 matched (target 60) | `borrow`, `borrow`, `equivalent`, `equivalent`, `eq`, `eq`, `hash`, `hash`, `partial_cmp`, `cmp`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | 1/1 | 12 | 122910.0 |
| 90 | `bigint.convert` | `bigint.Convert [ZERO]` | 0.00 | 19/28 matched (target 31) | `alloc_frozen_value`, `unpack_value_impl`, `unpack_value_impl`, `unpack_value_impl`, `unpack_value_impl`, `unpack_value_impl`, `module`, `takes_i32`, `takes_i64` | 0/2 matched (target 8) | `Canonical`, `Error` | 1/4 | 11 | 113010.0 |
| 91 | `dict.value` | `dict.Value [ZERO]` | 0.00 | 56/64 matched (target 82) | `fmt`, `fmt`, `hash`, `equivalent`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `serialize` | 7/10 matched (target 11) | `Canonical`, `Frozen`, `ContentRef` | 3/3 | 11 | 117410.0 |
| 92 | `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 0.00 | 4/13 matched (target 12) | `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered` | 2/4 matched (target 8) | `TestSimpleType`, `TestComplexGen` | 1/9 | 11 | 111710.0 |
| 93 | `record.globals` | `record.Globals [ZERO]` | 0.00 | 1/12 matched (target 1) | `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid` | 0/0 matched | _none_ | 0/9 | 11 | 111210.0 |
| 94 | `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 0.00 | 7/16 matched (target 9) | `alloc_frozen_value`, `alloc_frozen_string_value`, `starlark_type_repr`, `alloc_value`, `alloc_string_value`, `alloc_value`, `alloc_string_value`, `unpack_value_impl`, `unpack_value_impl` | 0/2 matched (target 1) | `Canonical`, `Error` | - | 11 | 111810.0 |
| 95 | `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 0.00 | 9/19 matched (target 14) | `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `visit`, `default`, `fmt`, `eq`, `across_lengths` | 2/3 matched | `Target` | 5/6 | 11 | 112210.0 |
| 96 | `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 0.00 | 36/44 matched (target 48) | `fmt`, `check_matches`, `fmt`, `starlark_type_repr`, `alloc_value`, `matches`, `hash`, `eq` | 5/7 matched (target 12) | `StaticType`, `Canonical` | - | 10 | 105110.0 |
| 97 | `bc.instrs` | `bc.Instrs [ZERO]` | 0.00 | 19/27 matched (target 29) | `handle`, `drop`, `drop`, `opcodes`, `fmt`, `write`, `default`, `display` | 3/4 matched (target 3) | `HandlerImpl` | 0/4 | 9 | 93110.0 |
| 98 | `range.range_type` | `range.RangeType [ZERO]` | 0.00 | 16/25 matched (target 33) | `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `eq` | 1/1 matched (target 3) | _none_ | 2/9 | 9 | 92610.0 |
| 99 | `tuple.unpack` | `tuple.Unpack [ZERO]` | 0.00 | 2/7 matched | `default`, `starlark_type_repr`, `into_iter`, `into_iter`, `into_iter` | 1/5 matched (target 2) | `Canonical`, `Error`, `Item`, `IntoIter` | 1/1 | 9 | 91210.0 |
| 100 | `values.unpack` | `values.Unpack [ZERO]` | 0.00 | 11/16 matched (target 14) | `into_error`, `into_infallible`, `error`, `error`, `error` | 3/7 matched | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` | - | 9 | 92310.0 |
| 101 | `heap.heap_type` | `heap.HeapType [ZERO]` | 0.00 | 56/64 matched (target 74) | `fmt`, `fmt`, `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `validate_str_interning`, `append_x` | 8/8 matched (target 10) | _none_ | 4/6 | 8 | 87210.0 |
| 102 | `layout.complex` | `layout.Complex [ZERO]` | 0.00 | 10/15 matched (target 18) | `unpack_value_impl`, `fmt`, `fmt`, `test_module`, `test_unpack` | 1/4 matched (target 2) | `Canonical`, `Error`, `Frozen` | 1/3 | 8 | 81910.0 |
| 103 | `list.value` | `list.Value [ZERO]` | 0.00 | 69/75 matched (target 103) | `fmt`, `error`, `starlark_type_repr`, `fmt`, `fmt`, `fmt` | 6/8 matched (target 10) | `List`, `Canonical` | 7/7 | 8 | 88310.0 |
| 104 | `set.value` | `set.Value [ZERO]` | 0.00 | 50/55 matched (target 66) | `content`, `iter_start`, `content_unchecked`, `iter_stop`, `fmt` | 6/9 matched (target 7) | `Canonical`, `Frozen`, `ContentRef` | 19/19 | 8 | 86410.0 |
| 105 | `adapter.implementation` | `adapter.Implementation [ZERO]` | 0.00 | 18/25 matched (target 27) | `prepare_dap_adapter`, `fmt`, `new`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints` | 6/6 matched (target 10) | _none_ | - | 7 | 73110.0 |
| 106 | `compiler.scope` | `compiler.Scope [ZERO]` | 0.00 | 49/53 matched (target 70) | `from`, `assign_ident_impl`, `new`, `new` | 17/20 matched (target 28) | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` | - | 7 | 77310.0 |
| 107 | `float.float` | `float.Float [STUB]` | 0.00 | 36/41 matched (target 45) | `fmt`, `non_finite`, `decimal`, `scientific`, `compact` | 1/3 matched (target 2) | `Canonical`, `Error` | 8/12 | 7 | 74410.0 |
| 108 | `heap.send` | `heap.Send [ZERO]` | 0.00 | 2/6 matched | `deref`, `deref_mut`, `fmt`, `fmt` | 3/6 matched (target 3) | `Sealed`, `Target`, `StaticType` | - | 7 | 71210.0 |
| 109 | `list.unpack` | `list.Unpack [ZERO]` | 0.00 | 4/7 matched (target 18) | `into_iter`, `into_iter`, `into_iter` | 1/5 matched (target 7) | `Canonical`, `Error`, `Item`, `IntoIter` | 1/1 | 7 | 71210.0 |
| 110 | `string.repr` | `string.Repr [ZERO]` | 0.00 | 15/22 matched (target 20) | `or4`, `push_vec_tail`, `test_to_repr`, `test`, `string_repr_for_test`, `test_chunk_non_ascii_or_need_escape`, `load` | 1/1 matched (target 2) | _none_ | 6/11 | 7 | 72310.0 |
| 111 | `string.simd` | `string.Simd [ZERO]` | 0.00 | 1/8 matched (target 2) | `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask` | 2/2 matched | _none_ | - | 7 | 71010.0 |
| 112 | `assert.assert` | `assert.Assert [STUB]` | 0.00 | 54/60 matched (target 66) | `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck` | 2/2 matched | _none_ | 0/4 | 6 | 66210.0 |
| 113 | `bc.addr` | `bc.Addr [ZERO]` | 0.00 | 22/27 matched (target 35) | `add_assign`, `get_instr_mut`, `sub_usize`, `sub`, `add` | 5/6 matched (target 5) | `Output` | - | 6 | 63310.0 |
| 114 | `profile.aggregated` | `profile.Aggregated [ZERO]` | 0.00 | 22/28 matched (target 40) | `new`, `normalize_for_golden_tests`, `fmt`, `normalize_for_golden_tests`, `total_alloc_count`, `make` | 8/8 matched (target 11) | _none_ | 3/7 | 6 | 63610.0 |
| 115 | `stdlib.partial` | `stdlib.Partial [STUB]` | 0.00 | 9/13 matched | `partial`, `partial`, `fmt`, `eq` | 3/5 matched (target 4) | `Frozen`, `Canonical` | 5/6 | 6 | 61810.0 |
| 116 | `values.owned` | `values.Owned [ZERO]` | 0.00 | 31/35 matched (target 34) | `fmt`, `downcast_starlark`, `deref`, `new` | 3/5 matched | `Canonical`, `Target` | - | 6 | 64010.0 |
| 117 | `alloc.allocator` | `alloc.Allocator [ZERO]` | 0.00 | 14/18 matched (target 19) | `fmt`, `default`, `drop`, `random_iteration` | 2/3 matched | `Item` | 3/4 | 5 | 52110.0 |
| 118 | `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 0.00 | 6/8 matched (target 6) | `next`, `size_hint` | 0/3 matched (target 1) | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` | - | 5 | 51110.0 |
| 119 | `environment.modules` | `environment.Modules [ZERO]` | 0.00 | 47/52 matched (target 48) | `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo` | 4/4 matched (target 6) | _none_ | 0/5 | 5 | 55610.0 |
| 120 | `tuple.value` | `tuple.Value [ZERO]` | 0.00 | 27/32 matched (target 40) | `fmt`, `fmt`, `new`, `offset_of_content`, `typechecker_ty` | 3/3 matched (target 4) | _none_ | 3/3 | 5 | 53510.0 |
| 121 | `alloc.chain` | `alloc.Chain [ZERO]` | 0.00 | 21/23 matched (target 26) | `drop`, `drop` | 3/5 matched (target 4) | `Item`, `ResetSplitAtZeroTest` | 7/8 | 4 | 42810.0 |
| 122 | `dict.alloc` | `dict.Alloc [ZERO]` | 0.00 | 0/3 matched (target 1) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 40510.0 |
| 123 | `heap.repr` | `heap.Repr [ZERO]` | 0.00 | 28/32 matched (target 35) | `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut` | 5/5 matched (target 8) | _none_ | - | 4 | 43710.0 |
| 124 | `params.spec` | `params.Spec [ZERO]` | 0.00 | 34/38 matched (target 35) | `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl` | 6/6 matched (target 11) | _none_ | - | 4 | 44410.0 |
| 125 | `structs.alloc` | `structs.Alloc [ZERO]` | 0.00 | 0/3 matched (target 0) | `starlark_type_repr`, `alloc_value`, `alloc_frozen_value` | 1/2 matched (target 1) | `Canonical` | - | 4 | 40510.0 |
| 126 | `typing.custom` | `typing.Custom [ZERO]` | 0.00 | 34/38 matched (target 49) | `eq`, `hash`, `partial_cmp`, `cmp` | 3/3 matched (target 5) | _none_ | - | 4 | 44110.0 |
| 127 | `bc.instr_arg` | `bc.InstrArg [ZERO]` | 0.00 | 82/84 matched | `fmt`, `fmt` | 3/4 matched (target 42) | `HandlerImpl` | - | 3 | 38810.0 |
| 128 | `bc.opcode` | `bc.Opcode [ZERO]` | 0.00 | 7/8 matched (target 11) | `handle` | 3/5 matched (target 4) | `ByNumber`, `FindOpcode` | 1/1 | 3 | 31310.0 |
| 129 | `bc.repr` | `bc.Repr [ZERO]` | 0.00 | 4/6 matched (target 5) | `size_of_repr`, `handle` | 2/3 matched (target 2) | `HandlerImpl` | - | 3 | 30910.0 |
| 130 | `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 0.00 | 15/17 matched (target 25) | `add`, `add` | 7/8 matched (target 7) | `Output` | - | 3 | 32510.0 |
| 131 | `compiler.def` | `compiler.Def [ZERO]` | 0.00 | 38/40 matched (target 46) | `fmt`, `new` | 12/13 matched (target 18) | `Frozen` | - | 3 | 35310.0 |
| 132 | `debug.inspect` | `debug.Inspect [ZERO]` | 0.00 | 6/9 matched (target 7) | `debugger`, `debug_inspect_stack`, `debug_inspect_variables` | 0/0 matched (target 1) | _none_ | 2/5 | 3 | 30910.0 |
| 133 | `eval.bc.compiler.call` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` | 0.00 | 4/7 matched (target 8) | `mark_definitely_assigned_after`, `mark_definitely_assigned_after`, `write_bc` | 0/0 matched (target 3) | _none_ | - | 3 | 30710.0 |
| 134 | `eval.bc.compiler.stmt` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` | 0.00 | 8/11 matched | `write_bc`, `write_if_then`, `write_if_else` | 0/0 matched | _none_ | - | 3 | 31110.0 |
| 135 | `list.refs` | `list.Refs [ZERO]` | 0.00 | 15/15 matched (target 29) | _none_ | 2/5 matched (target 10) | `Target`, `Canonical`, `Error` | - | 3 | 32010.0 |
| 136 | `symbol.map` | `symbol.Map [ZERO]` | 0.00 | 9/12 matched (target 11) | `fmt`, `new`, `with_capacity` | 1/1 matched | _none_ | - | 3 | 31310.0 |
| 137 | `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 0.00 | 11/11 matched (target 13) | _none_ | 4/7 matched | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` | - | 3 | 31810.0 |
| 138 | `avalues.array` | `avalues.Array [ZERO]` | 0.00 | 13/13 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 2 | 21710.0 |
| 139 | `avalues.complex` | `avalues.Complex [ZERO]` | 0.00 | 10/10 matched (target 17) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | - | 2 | 21510.0 |
| 140 | `avalues.static_` | `avalues.Static [ZERO]` | 0.00 | 9/9 matched (target 11) | _none_ | 3/5 matched (target 4) | `StarlarkValue`, `ExtraElem` | 1/1 | 2 | 21410.0 |
| 141 | `avalues.tuple` | `avalues.Tuple [ZERO]` | 0.00 | 14/14 matched (target 17) | _none_ | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 2 | 21810.0 |
| 142 | `bc.call` | `bc.Call [ZERO]` | 0.00 | 6/7 matched (target 15) | `fmt` | 4/5 matched (target 8) | `Args` | - | 2 | 21210.0 |
| 143 | `bool.type_repr` | `bool.TypeRepr [ZERO]` | 0.00 | 0/1 matched | `starlark_type_repr` | 0/1 matched (target 0) | `Canonical` | - | 2 | 20210.0 |
| 144 | `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 0.00 | 2/3 matched (target 2) | `drop` | 0/1 matched (target 0) | `Guard` | - | 2 | 20410.0 |
| 145 | `compiler.expr` | `compiler.Expr [ZERO]` | 0.00 | 60/60 matched (target 63) | _none_ | 9/11 matched (target 56) | `AstLiteralCompile`, `CompilerExprUtil` | - | 2 | 27110.0 |
| 146 | `debug.evaluate` | `debug.Evaluate [ZERO]` | 0.00 | 2/4 matched (target 3) | `debugger`, `debug_evaluate` | 0/0 matched (target 1) | _none_ | 1/3 | 2 | 20410.0 |
| 147 | `funcs.min_max` | `funcs.MinMax [ZERO]` | 0.00 | 3/5 matched (target 3) | `max`, `min` | 0/0 matched | _none_ | - | 2 | 20510.0 |
| 148 | `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 0.00 | 0/1 matched (target 4) | `drop` | 5/6 matched (target 5) | `Canonical` | - | 2 | 20710.0 |
| 149 | `list.globals` | `list.Globals [ZERO]` | 0.00 | 4/5 matched | `list` | 0/1 matched | `ListType` | - | 2 | 20610.0 |
| 150 | `params.parser` | `params.Parser [ZERO]` | 0.00 | 8/10 matched | `test`, `test` | 1/1 matched (target 2) | _none_ | 3/5 | 2 | 21110.0 |
| 151 | `profile.stmt` | `profile.Stmt [ZERO]` | 0.00 | 22/23 matched (target 25) | `r#gen` | 8/9 matched (target 10) | `Data` | 3/3 | 2 | 23210.0 |
| 152 | `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 0.00 | 9/10 matched | `drop_non_drop` | 2/3 matched (target 2) | `RowKind` | 0/1 | 2 | 21310.0 |
| 153 | `profile.typecheck` | `profile.Typecheck [ZERO]` | 0.00 | 7/8 matched | `r#gen` | 4/5 matched (target 6) | `Data` | 2/2 | 2 | 21310.0 |
| 154 | `record.instance` | `record.Instance [ZERO]` | 0.00 | 11/13 matched (target 17) | `fmt`, `get_attr_hashed` | 1/1 matched (target 3) | _none_ | - | 2 | 21410.0 |
| 155 | `set.refs` | `set.Refs [ZERO]` | 0.00 | 5/5 matched (target 19) | _none_ | 3/5 matched (target 13) | `Canonical`, `Error` | - | 2 | 21010.0 |
| 156 | `structs.refs` | `structs.Refs [ZERO]` | 0.00 | 7/7 matched (target 8) | _none_ | 2/4 matched | `Canonical`, `Error` | - | 2 | 21110.0 |
| 157 | `structs.value` | `structs.Value [STUB]` | 0.00 | 19/21 matched (target 35) | `iter_frozen`, `fmt` | 1/1 matched (target 6) | _none_ | 5/5 | 2 | 22210.0 |
| 158 | `type_compiled.globals` | `type_compiled.Globals [ZERO]` | 0.00 | 2/4 matched (target 2) | `eval_type`, `isinstance` | 0/0 matched (target 1) | _none_ | 1/1 | 2 | 20410.0 |
| 159 | `types.function` | `types.Function [ZERO]` | 0.00 | 20/21 matched (target 28) | `new` | 11/12 matched (target 14) | `Canonical` | - | 2 | 23310.0 |
| 160 | `typing.iter` | `typing.Iter [ZERO]` | 0.00 | 6/6 matched (target 8) | _none_ | 2/4 matched (target 3) | `NonInstantiable`, `Canonical` | 3/3 | 2 | 21010.0 |
| 161 | `values.recursive_repr_or_json_guard` | `values.RecursiveReprOrJsonGuard [ZERO]` | 0.00 | 2/4 matched (target 5) | `drop`, `drop` | 4/4 matched | _none_ | - | 2 | 20810.0 |
| 162 | `values.traits` | `values.Traits [ZERO]` | 0.00 | 55/56 matched (target 55) | `please_use_starlark_type_macro` | 2/3 matched (target 2) | `Canonical` | - | 2 | 25910.0 |
| 163 | `values.type_repr` | `values.TypeRepr [ZERO]` | 0.00 | 8/9 matched (target 8) | `starlark_type_repr` | 2/3 matched (target 8) | `Canonical` | 1/1 | 2 | 21210.0 |
| 164 | `analysis` | `starlark.Analysis [STUB]` | 0.00 | 11/12 matched | `module` | 1/1 matched (target 2) | _none_ | 10/11 | 1 | 11310.0 |
| 165 | `compiler.if_compiler` | `compiler.IfCompiler [ZERO]` | 0.00 | 5/6 matched (target 5) | `wr` | 0/0 matched | _none_ | - | 1 | 10610.0 |
| 166 | `debug.adapter` | `debug.Adapter [ZERO]` | 0.00 | 22/23 matched | `fmt` | 14/14 matched (target 29) | _none_ | - | 1 | 13710.0 |
| 167 | `dict.globals` | `dict.Globals [ZERO]` | 0.00 | 2/3 matched (target 4) | `dict` | 0/0 matched | _none_ | - | 1 | 10310.0 |
| 168 | `docs` | `docs.Docs [ZERO]` | 0.00 | 16/17 matched (target 16) | `default` | 10/10 matched (target 15) | _none_ | - | 1 | 12710.0 |
| 169 | `docs.parse` | `docs.Parse [ZERO]` | 0.00 | 15/16 matched (target 18) | `arg` | 1/1 matched (target 2) | _none_ | 6/7 | 1 | 11710.0 |
| 170 | `float.globals` | `float.Globals [ZERO]` | 0.00 | 1/2 matched (target 1) | `float` | 0/0 matched (target 4) | _none_ | - | 1 | 10210.0 |
| 171 | `funcs.other` | `funcs.Other [STUB]` | 0.00 | 18/19 matched | `r#type` | 0/0 matched (target 2) | _none_ | 6/6 | 1 | 11910.0 |
| 172 | `int.globals` | `int.Globals [ZERO]` | 0.00 | 1/2 matched | `int` | 0/0 matched | _none_ | - | 1 | 10210.0 |
| 173 | `list.methods` | `list.Methods [ZERO]` | 0.00 | 10/11 matched (target 19) | `list_methods` | 0/0 matched (target 2) | _none_ | 3/3 | 1 | 11110.0 |
| 174 | `profile.by_type` | `profile.ByType [ZERO]` | 0.00 | 5/6 matched (target 7) | `normalize_for_golden_tests` | 1/1 matched | _none_ | 0/1 | 1 | 10710.0 |
| 175 | `set.set` | `set.Set [ZERO]` | 0.00 | 4/5 matched (target 4) | `set` | 0/0 matched (target 1) | _none_ | 3/3 | 1 | 10510.0 |
| 176 | `structs.structs` | `structs.Structs [ZERO]` | 0.00 | 3/4 matched (target 3) | `r#struct` | 1/1 matched | _none_ | - | 1 | 10510.0 |
| 177 | `typing.never` | `typing.Never [ZERO]` | 0.00 | 6/6 matched (target 9) | _none_ | 2/3 matched | `Canonical` | 2/2 | 1 | 10910.0 |
| 178 | `alloc.per_thread` | `alloc.PerThread [ZERO]` | 0.00 | 6/6 matched (target 9) | _none_ | 1/1 matched (target 2) | _none_ | 1/1 | 0 | 710.0 |
| 179 | `bc.for_loop` | `bc.ForLoop [ZERO]` | 0.00 | 0/0 matched (target 2) | _none_ | 1/1 matched | _none_ | - | 0 | 110.0 |
| 180 | `bc.writer` | `bc.Writer [ZERO]` | 0.00 | 43/43 matched (target 44) | _none_ | 4/4 matched | _none_ | - | 0 | 4710.0 |
| 181 | `compiler.assign` | `compiler.Assign [ZERO]` | 0.00 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 182 | `compiler.types` | `compiler.Types [ZERO]` | 0.00 | 8/8 matched | _none_ | 1/1 matched (target 7) | _none_ | - | 0 | 910.0 |
| 183 | `dict.methods` | `dict.Methods [ZERO]` | 0.00 | 17/17 matched (target 19) | _none_ | 0/0 matched (target 1) | _none_ | 7/7 | 0 | 1710.0 |
| 184 | `docs.code` | `docs.Code [ZERO]` | 0.00 | 14/14 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1410.0 |
| 185 | `docs.markdown` | `docs.Markdown [ZERO]` | 0.00 | 19/19 matched | _none_ | 2/2 matched | _none_ | - | 0 | 2110.0 |
| 186 | `environment` | `starlark.Environment [ZERO]` | 0.00 | 0/0 matched | _none_ | 1/1 matched (target 5) | _none_ | - | 0 | 110.0 |
| 187 | `environment.names` | `environment.Names [ZERO]` | 0.00 | 14/14 matched | _none_ | 2/2 matched | _none_ | - | 0 | 1610.0 |
| 188 | `errors` | `starlark.Errors [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 189 | `eval.bc.compiler.def` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` | 0.00 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 210.0 |
| 190 | `eval.bc.compiler.expr` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` | 0.00 | 15/15 matched (target 16) | _none_ | 0/0 matched | _none_ | - | 0 | 1510.0 |
| 191 | `eval.runtime` | `eval.Runtime [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 192 | `intern.interner` | `intern.Interner [ZERO]` | 0.00 | 4/4 matched (target 7) | _none_ | 2/2 matched (target 3) | _none_ | 2/2 | 0 | 610.0 |
| 193 | `layout.avalues` | `layout.AValues [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 194 | `layout.static_string` | `layout.StaticString [ZERO]` | 0.00 | 5/5 matched | _none_ | 1/1 matched (target 2) | _none_ | - | 0 | 610.0 |
| 195 | `macros` | `starlark.Macros [ZERO]` | 0.00 | 0/0 matched (target 9) | _none_ | 0/0 matched (target 9) | _none_ | - | 0 | 10.0 |
| 196 | `pagable` | `starlark.Pagable [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 197 | `pagable.vtable_register` | `pagable.VtableRegister [ZERO]` | 0.00 | 0/0 matched (target 3) | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 198 | `record.ty_record_type` | `record.TyRecordType [ZERO]` | 0.00 | 7/7 matched | _none_ | 1/1 matched (target 2) | _none_ | 7/7 | 0 | 810.0 |
| 199 | `stdlib.funcs` | `stdlib.Funcs [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 200 | `string.globals` | `string.Globals [ZERO]` | 0.00 | 5/5 matched | _none_ | 0/0 matched | _none_ | - | 0 | 510.0 |
| 201 | `string.iter` | `string.Iter [ZERO]` | 0.00 | 3/3 matched (target 6) | _none_ | 1/1 matched | _none_ | - | 0 | 410.0 |
| 202 | `string.methods` | `string.Methods [ZERO]` | 0.00 | 41/41 matched (target 68) | _none_ | 1/1 matched (target 5) | _none_ | 4/4 | 0 | 4210.0 |
| 203 | `syntax` | `starlark.Syntax [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 204 | `type_compiled.matchers` | `type_compiled.Matchers [ZERO]` | 0.00 | 25/25 matched | _none_ | 23/23 matched | _none_ | - | 0 | 4810.0 |
| 205 | `typing.any` | `typing.Any [ZERO]` | 0.00 | 4/4 matched (target 6) | _none_ | 1/1 matched (target 2) | _none_ | 2/2 | 0 | 510.0 |
| 206 | `typing.error` | `typing.Error [ZERO]` | 0.00 | 16/16 matched (target 25) | _none_ | 5/5 matched (target 10) | _none_ | - | 0 | 2110.0 |
| 207 | `typing.fill_types_for_lint` | `typing.FillTypesForLint [ZERO]` | 0.00 | 39/39 matched (target 40) | _none_ | 3/3 matched | _none_ | - | 0 | 4210.0 |
| 208 | `typing.oracle` | `typing.Oracle [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 209 | `unused_loads.remove` | `unusedloads.Remove [ZERO]` | 0.00 | 4/4 matched | _none_ | 1/1 matched | _none_ | - | 0 | 510.0 |
| 210 | `util` | `starlark.Util [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 211 | `values.index` | `values.Index [ZERO]` | 0.00 | 6/6 matched (target 7) | _none_ | 0/0 matched (target 1) | _none_ | 2/2 | 0 | 610.0 |
| 212 | `values.types` | `values.Types [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 213 | `values.typing` | `values.Typing [ZERO]` | 0.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 10.0 |
| 214 | `values.typing.ty` | `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` | 0.00 | 5/5 matched (target 7) | _none_ | 1/1 matched (target 2) | _none_ | 3/3 | 0 | 610.0 |
| 215 | `types.type_instance_id` | `types.TypeInstanceId` | 0.00 | 0/1 matched (target 2) | `r#gen` | 1/1 matched | _none_ | - | 1 | 9010210.0 |
| 216 | `runtime.visit_span` | `runtime.VisitSpan` | 0.00 | 0/19 matched | `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans`, `visit_spans` | 1/1 matched | _none_ | - | 19 | 192010.0 |
| 217 | `tuple.rust_tuple` | `tuple.RustTuple` | 0.00 | 0/11 matched | `alloc_value`, `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `alloc_frozen_value`, `starlark_type_repr`, `starlark_type_repr`, `starlark_type_repr`, `unpack_value_impl`, `unpack_value_impl` | 0/2 matched (target 0) | `Canonical`, `Error` | - | 13 | 131310.0 |
| 218 | `bool.unpack` | `bool.Unpack` | 0.00 | 0/1 matched | `unpack_value_impl` | 0/1 matched (target 0) | `Error` | - | 2 | 20210.0 |
| 219 | `enumeration.ty_enum_type` | `enumeration.TyEnumType` | 0.00 | 0/2 matched (target 3) | `eq`, `hash` | 1/1 matched | _none_ | - | 2 | 20310.0 |
| 220 | `pagable.error` | `pagable.Error` | 0.00 | 0/1 matched | `from` | 1/1 matched (target 2) | _none_ | - | 1 | 10210.0 |
| 221 | `wasm` | `starlark.Wasm` | 0.00 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 110.0 |
| 222 | `enumeration.globals` | `enumeration.Globals` | 0.02 | 1/5 matched (target 1) | `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr` | 0/0 matched | _none_ | 0/3 | 4 | 40509.8 |
| 223 | `bool.globals` | `bool.Globals` | 0.03 | 1/2 matched (target 1) | `bool` | 0/0 matched | _none_ | - | 1 | 10209.7 |
| 224 | `int.i32` | `int.I32` | 0.03 | 2/4 matched (target 5) | `alloc_value`, `alloc_frozen_value` | 0/2 matched (target 1) | `Canonical`, `Error` | - | 4 | 40609.7 |
| 225 | `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 0.04 | 1/2 matched (target 1) | `drop` | 0/1 matched (target 0) | `WriteRemOnDrop` | - | 2 | 20309.6 |
| 226 | `namespace.globals` | `namespace.Globals` | 0.05 | 1/2 matched (target 1) | `namespace` | 0/0 matched | _none_ | - | 1 | 10209.5 |
| 227 | `num.globals` | `num.Globals` | 0.05 | 1/2 matched (target 1) | `abs` | 0/0 matched | _none_ | - | 1 | 10209.5 |
| 228 | `range.globals` | `range.Globals` | 0.05 | 1/2 matched (target 1) | `range` | 0/0 matched | _none_ | - | 1 | 10209.5 |
| 229 | `profile.mode` | `profile.Mode` | 0.05 | 1/4 matched | `fmt`, `name`, `from_str` | 1/2 matched (target 1) | `Err` | - | 4 | 40609.5 |
| 230 | `tuple.globals` | `tuple.Globals` | 0.06 | 1/2 matched (target 1) | `tuple` | 0/0 matched | _none_ | - | 1 | 10209.4 |
| 231 | `collections.aligned_padded_str` | `alignedpaddedstr.AlignedPaddedStr` | 0.06 | 2/3 matched (target 4) | `eq` | 1/1 matched | _none_ | - | 1 | 2010409.4 |
| 232 | `oracle.traits` | `oracle.Traits` | 0.08 | 1/1 matched (target 3) | _none_ | 2/2 matched | _none_ | - | 0 | 309.2 |
| 233 | `environment.module_dump` | `environment.ModuleDump` | 0.09 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 209.1 |
| 234 | `runtime.file_loader` | `runtime.FileLoader` | 0.09 | 2/2 matched | _none_ | 3/3 matched | _none_ | - | 0 | 2000509.1 |
| 235 | `list.list_type` | `list.ListType` | 0.09 | 1/2 matched (target 5) | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 3030509.0 |
| 236 | `analysis.types` | `analysis.Types` | 0.10 | 4/10 matched (target 7) | `fmt`, `fmt`, `new`, `fmt`, `fmt`, `from` | 2/5 matched (target 2) | `LintWarning`, `LintT`, `EvalSeverity` | - | 9 | 91509.0 |
| 237 | `dict.unpack` | `dict.Unpack` | 0.10 | 2/3 matched | `unpack_value_impl` | 1/3 matched (target 1) | `Canonical`, `Error` | - | 3 | 30609.0 |
| 238 | `dict.traits` | `dict.Traits` | 0.10 | 6/14 matched (target 7) | `alloc_value`, `alloc_frozen_value`, `alloc_value`, `alloc_frozen_value`, `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `starlark_type_repr` | 0/2 matched (target 6) | `Canonical`, `Error` | - | 10 | 101609.0 |
| 239 | `profile.csv` | `profile.Csv` | 0.11 | 8/18 matched (target 11) | `new`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv`, `format_for_csv` | 1/3 matched | `Impl`, `CsvValue` | 2/2 | 12 | 122108.9 |
| 240 | `avalues.list` | `avalues.List` | 0.11 | 14/16 matched (target 19) | `alloc_list`, `alloc_list_concat` | 2/4 matched (target 2) | `StarlarkValue`, `ExtraElem` | - | 4 | 42008.9 |
| 241 | `enumeration.value` | `enumeration.Value` | 0.12 | 6/9 matched (target 11) | `fmt`, `index`, `value` | 1/1 matched (target 8) | _none_ | - | 3 | 31008.8 |
| 242 | `typing.type_type` | `typing.TypeType` | 0.12 | 3/5 matched (target 8) | `module`, `takes_type` | 1/3 matched (target 2) | `Canonical`, `Error` | 1/3 | 4 | 2040808.8 |
| 243 | `any` | `starlark.Any` | 0.12 | 8/13 matched (target 17) | `is`, `convert_value`, `convert_any`, `test`, `test` | 14/15 matched (target 50) | `StaticType` | 4/8 | 6 | 8062809.0 |
| 244 | `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 0.13 | 3/10 matched | `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter` | 2/5 matched (target 4) | `Target`, `Item`, `IntoIter` | - | 10 | 101508.7 |
| 245 | `dict.refs` | `dict.Refs` | 0.13 | 6/10 matched (target 13) | `from_value`, `from_value`, `error`, `deref` | 4/7 matched (target 11) | `Target`, `Canonical`, `Error` | - | 7 | 71708.7 |
| 246 | `stdlib.extra` | `stdlib.Extra` | 0.13 | 11/24 matched (target 28) | `filter`, `map`, `debug`, `fmt`, `print`, `print`, `pprint`, `pprint`, `pstr`, `pstr`, `prepr`, `prepr`, `println` | 3/4 matched | `PrintHandlerImpl` | 6/7 | 14 | 142808.7 |
| 247 | `analysis.underscore` | `analysis.Underscore` | 0.13 | 8/13 matched (target 14) | `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored` | 1/1 matched (target 3) | _none_ | 0/4 | 5 | 51408.7 |
| 248 | `bc.definitely_assigned` | `bc.DefinitelyAssigned` | 0.14 | 2/4 matched (target 7) | `new`, `assert_smaller_then` | 1/1 matched | _none_ | - | 2 | 20508.6 |
| 249 | `stdlib.json` | `stdlib.Json` | 0.14 | 8/26 matched (target 28) | `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `starlark_type_repr`, `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `starlark_type_repr`, `starlark_type_repr`, `alloc_value`, `alloc_value`, `alloc_frozen_value`, `alloc_frozen_value`, `json`, `encode`, `decode` | 0/1 matched (target 12) | `Canonical` | 4/4 | 19 | 192708.6 |
| 250 | `profile.data` | `profile.Data` | 0.15 | 5/7 matched (target 5) | `_assert_profile_data_send_sync`, `_assert_send_sync` | 3/3 matched (target 18) | _none_ | - | 2 | 21008.5 |
| 251 | `compiler.args` | `compiler.Args` | 0.16 | 10/11 matched | `args` | 1/2 matched (target 1) | `Never` | - | 2 | 21308.4 |
| 252 | `layout.value_captured` | `layout.ValueCaptured` | 0.16 | 4/4 matched (target 9) | _none_ | 2/4 matched (target 2) | `Canonical`, `Frozen` | - | 2 | 1020808.4 |
| 253 | `typing.callable_param` | `typing.CallableParam` | 0.16 | 19/23 matched (target 27) | `fmt`, `pf`, `new_named_only`, `fmt` | 5/6 matched (target 11) | `ParamSpecDisplay` | 1/1 | 5 | 52908.4 |
| 254 | `typing.small_arc_vec` | `typing.SmallArcVec` | 0.17 | 4/11 matched (target 16) | `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter` | 2/3 matched (target 5) | `Target` | - | 8 | 81408.3 |
| 255 | `types.unbound` | `types.Unbound` | 0.17 | 3/4 matched | `fmt` | 1/1 matched (target 3) | _none_ | - | 1 | 10508.3 |
| 256 | `values.demand` | `values.Demand` | 0.17 | 5/7 matched (target 8) | `payload`, `provide` | 3/4 matched | `StaticType` | 1/3 | 3 | 4031108.2 |
| 257 | `symbol.symbol` | `symbol.Symbol` | 0.18 | 7/9 matched (target 11) | `fmt`, `eq` | 1/1 matched | _none_ | - | 2 | 21008.2 |
| 258 | `bc.native_function` | `bc.NativeFunction` | 0.18 | 3/4 matched | `fun` | 1/1 matched | _none_ | - | 1 | 4010508.2 |
| 259 | `profile.alloc_counts` | `profile.AllocCounts` | 0.18 | 1/4 matched (target 5) | `normalize_for_golden_tests`, `add_assign`, `add` | 1/2 matched (target 1) | `Output` | 0/1 | 4 | 4040608.2 |
| 260 | `compiler.opt_ctx` | `compiler.OptCtx` | 0.18 | 13/13 matched | _none_ | 2/2 matched (target 4) | _none_ | - | 0 | 7001508.0 |
| 261 | `scope.scope_resolver_globals` | `scope.ScopeResolverGlobals` | 0.19 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 5000408.0 |
| 262 | `typing.interface` | `typing.Interface` | 0.19 | 3/3 matched | _none_ | 1/1 matched | _none_ | - | 0 | 5000408.0 |
| 263 | `none.none_or` | `none.NoneOr` | 0.19 | 7/7 matched (target 9) | _none_ | 1/3 matched (target 4) | `Canonical`, `Error` | - | 2 | 6021008.0 |
| 264 | `typing.callable` | `typing.Callable` | 0.19 | 6/7 matched (target 10) | `fmt` | 1/2 matched (target 1) | `TyCallableInner` | - | 2 | 20908.1 |
| 265 | `util.arc_or_static` | `util.ArcOrStatic` | 0.20 | 5/10 matched (target 9) | `fmt`, `eq`, `partial_cmp`, `cmp`, `hash` | 2/3 matched (target 4) | `Target` | - | 6 | 2061308.0 |
| 266 | `runtime.slots` | `runtime.Slots` | 0.20 | 3/3 matched | _none_ | 3/3 matched | _none_ | - | 0 | 608.0 |
| 267 | `analysis.find_call_name` | `analysis.FindCallName` | 0.20 | 3/3 matched (target 9) | _none_ | 1/1 matched (target 2) | _none_ | 1/1 | 0 | 408.0 |
| 268 | `bool.value` | `bool.Value` | 0.20 | 8/9 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 11008.0 |
| 269 | `analysis.performance` | `analysis.Performance` | 0.20 | 6/10 matched (target 14) | `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function` | 1/1 matched (target 4) | _none_ | 0/3 | 4 | 41108.0 |
| 270 | `analysis.dubious` | `analysis.Dubious` | 0.20 | 9/12 matched (target 22) | `lint`, `module`, `about` | 1/2 matched (target 9) | `Key` | 2/4 | 4 | 41408.0 |
| 271 | `assert.conformance` | `assert.Conformance` | 0.21 | 5/5 matched | _none_ | 1/1 matched | _none_ | - | 0 | 607.9 |
| 272 | `typing.structs` | `typing.Structs` | 0.21 | 7/8 matched (target 9) | `fmt` | 2/2 matched | _none_ | - | 1 | 1011007.9 |
| 273 | `environment.slots` | `environment.Slots` | 0.21 | 10/10 matched | _none_ | 3/3 matched | _none_ | - | 0 | 1307.9 |
| 274 | `tuple.refs` | `tuple.Refs` | 0.21 | 11/13 matched (target 15) | `unpack_value_impl`, `unpack_value_impl` | 2/4 matched (target 2) | `Canonical`, `Error` | - | 4 | 41707.9 |
| 275 | `type_compiled.type_matcher_factory` | `type_compiled.TypeMatcherFactory` | 0.21 | 3/3 matched (target 6) | _none_ | 3/3 matched | _none_ | - | 0 | 7000608.0 |
| 276 | `float.unpack` | `float.Unpack` | 0.22 | 3/3 matched (target 4) | _none_ | 1/3 matched (target 2) | `Canonical`, `Error` | 1/1 | 2 | 20607.8 |
| 277 | `compiler.span` | `compiler.Span` | 0.23 | 2/2 matched | _none_ | 1/2 matched (target 1) | `Target` | - | 1 | 29010408.0 |
| 278 | `compiler.def_inline` | `compiler.DefInline` | 0.23 | 9/10 matched (target 9) | `new` | 4/4 matched (target 6) | _none_ | - | 1 | 11407.7 |
| 279 | `layout.vtable` | `layout.Vtable` | 0.23 | 60/69 matched (target 65) | `value_ptr`, `value_ref`, `drop_in_place`, `fmt`, `new`, `as_allocative`, `total_memory_for_profile`, `as_serialize`, `new` | 4/6 matched (target 4) | `GetTypeId`, `GetAllocativeKey` | - | 11 | 117507.7 |
| 280 | `compiler.compr` | `compiler.Compr` | 0.23 | 11/11 matched (target 12) | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 1407.7 |
| 281 | `bc.bytecode` | `bc.Bytecode` | 0.24 | 6/7 matched (target 11) | `handle` | 1/2 matched (target 1) | `HandlerImpl` | - | 2 | 20907.6 |
| 282 | `eval` | `eval.Eval` | 0.24 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 207.6 |
| 283 | `runtime.frame_span` | `runtime.FrameSpan` | 0.24 | 3/4 matched | `fmt` | 1/1 matched | _none_ | - | 1 | 26010508.0 |
| 284 | `compiler.known` | `compiler.Known` | 0.24 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 107.6 |
| 285 | `num.value` | `num.Value` | 0.24 | 16/22 matched (target 32) | `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul` | 3/4 matched (target 7) | `Output` | 5/5 | 7 | 72607.6 |
| 286 | `profile.profiler_type` | `profile.ProfilerType` | 0.25 | 1/1 matched | _none_ | 2/2 matched | _none_ | - | 0 | 6000307.5 |
| 287 | `typing.ctx` | `typing.Ctx` | 0.25 | 19/19 matched (target 20) | _none_ | 1/1 matched (target 2) | _none_ | - | 0 | 2007.5 |
| 288 | `profile.time_flame` | `profile.TimeFlame` | 0.25 | 17/20 matched (target 21) | `r#gen`, `register_sleep`, `sleep` | 10/11 matched (target 16) | `Data` | 1/3 | 4 | 43107.5 |
| 289 | `analysis.names` | `analysis.Names` | 0.25 | 31/36 matched (target 42) | `new`, `ident`, `assign_ident`, `about`, `module` | 7/8 matched (target 14) | `AstStrExt` | 9/11 | 6 | 64407.5 |
| 290 | `profile.bc` | `profile.Bc` | 0.25 | 23/29 matched (target 28) | `sum`, `add_assign`, `add_assign`, `add_assign`, `add_assign`, `default` | 9/10 matched (target 14) | `Data` | 4/4 | 7 | 73907.5 |
| 291 | `namespace.typing` | `namespace.Typing` | 0.25 | 6/7 matched (target 8) | `fmt` | 3/3 matched | _none_ | - | 1 | 11007.5 |
| 292 | `params.display` | `params.Display` | 0.26 | 4/4 matched | _none_ | 3/3 matched (target 8) | _none_ | - | 0 | 76000712.0 |
| 293 | `compiler.call` | `compiler.Call` | 0.26 | 13/13 matched (target 14) | _none_ | 1/1 matched | _none_ | - | 0 | 1407.4 |
| 294 | `enumeration.matcher` | `enumeration.Matcher` | 0.26 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 207.4 |
| 295 | `compiler.module` | `compiler.Module` | 0.26 | 6/6 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 707.4 |
| 296 | `values.starlark_type_id` | `values.StarlarkTypeId` | 0.27 | 5/6 matched (target 7) | `eq` | 2/2 matched | _none_ | - | 1 | 7010807.5 |
| 297 | `layout.value_not_special` | `layout.ValueNotSpecial` | 0.28 | 6/6 matched (target 7) | _none_ | 1/1 matched | _none_ | - | 0 | 707.2 |
| 298 | `values.error` | `values.Error` | 0.28 | 4/5 matched | `from` | 2/2 matched (target 20) | _none_ | - | 1 | 17010708.0 |
| 299 | `util.refcell` | `refcell.RefCell` | 0.28 | 2/2 matched (target 12) | _none_ | 0/0 matched (target 4) | _none_ | 1/1 | 0 | 20000208.0 |
| 300 | `types.known_methods` | `types.KnownMethods` | 0.28 | 5/5 matched | _none_ | 2/2 matched | _none_ | - | 0 | 707.2 |
| 301 | `values.comparison` | `values.Comparison` | 0.29 | 5/5 matched | _none_ | 0/0 matched | _none_ | - | 0 | 507.1 |
| 302 | `analysis.incompatible` | `analysis.Incompatible` | 0.29 | 12/14 matched (target 20) | `lint`, `module` | 1/1 matched (target 4) | _none_ | 2/3 | 2 | 21507.1 |
| 303 | `analysis.flow` | `analysis.Flow` | 0.29 | 22/25 matched (target 37) | `lint`, `module`, `about` | 1/1 matched (target 12) | _none_ | 5/7 | 3 | 32607.1 |
| 304 | `compiler.expr_bool` | `compiler.ExprBool` | 0.29 | 5/5 matched | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 607.1 |
| 305 | `oracle.ctx` | `oracle.Ctx` | 0.30 | 32/32 matched | _none_ | 2/2 matched (target 14) | _none_ | - | 0 | 3407.0 |
| 306 | `tuple.alloc` | `tuple.Alloc` | 0.30 | 5/5 matched | _none_ | 1/2 matched | `Canonical` | 2/2 | 1 | 10707.0 |
| 307 | `typing.macro_refs` | `typing.MacroRefs` | 0.30 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 207.0 |
| 308 | `stdlib.breakpoint` | `stdlib.Breakpoint` | 0.30 | 14/19 matched (target 18) | `global`, `breakpoint`, `reset_global_state`, `read_line`, `println` | 5/6 matched (target 7) | `Handler` | 3/6 | 6 | 1062507.0 |
| 309 | `runtime.inlined_frame` | `runtime.InlinedFrame` | 0.31 | 7/10 matched (target 9) | `eq`, `make_span`, `assert_stack` | 3/3 matched (target 4) | _none_ | 1/3 | 3 | 31306.9 |
| 310 | `profile.flamegraph` | `profile.Flamegraph` | 0.31 | 12/13 matched (target 16) | `new` | 3/3 matched (target 4) | _none_ | 3/3 | 1 | 11606.9 |
| 311 | `none.globals` | `none.Globals` | 0.32 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 106.8 |
| 312 | `unused_loads.find` | `unusedloads.Find` | 0.32 | 4/4 matched (target 8) | _none_ | 3/3 matched | _none_ | - | 0 | 706.8 |
| 313 | `string.dot_format` | `string.DotFormat` | 0.32 | 10/11 matched | `format_capture_for_test` | 1/1 matched (target 2) | _none_ | 3/4 | 1 | 1011206.8 |
| 314 | `callable.param` | `callable.Param` | 0.33 | 6/6 matched | _none_ | 2/2 matched (target 7) | _none_ | - | 0 | 806.7 |
| 315 | `layout.value` | `layout.Value` | 0.33 | 142/155 matched (target 166) | `fmt`, `fmt`, `fmt`, `fmt`, `eq`, `eq`, `testing_new_int`, `display_for_type_error`, `fmt`, `testing_new_int`, `to_value`, `compare`, `_test_send_sync` | 7/9 matched (target 11) | `Canonical`, `String` | 8/10 | 15 | 178166400.0 |
| 316 | `analysis.lint_message` | `analysis.LintMessage` | 0.34 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 1000206.6 |
| 317 | `docs.multipage` | `docs.Multipage` | 0.34 | 6/6 matched | _none_ | 5/5 matched (target 7) | _none_ | - | 0 | 1106.6 |
| 318 | `typing.arc_ty` | `typing.ArcTy` | 0.34 | 6/8 matched (target 16) | `fmt`, `fmt` | 3/4 matched (target 10) | `Target` | - | 3 | 5031206.5 |
| 319 | `record.matcher` | `record.Matcher` | 0.35 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 206.5 |
| 320 | `layout.identity` | `layout.Identity` | 0.36 | 1/1 matched | _none_ | 1/1 matched | _none_ | - | 0 | 206.4 |
| 321 | `eval.params` | `eval.Params` | 0.36 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 106.4 |
| 322 | `eval.soft_error` | `eval.SoftError` | 0.37 | 1/1 matched | _none_ | 2/2 matched | _none_ | - | 0 | 306.3 |
| 323 | `stdlib.internal` | `stdlib.Internal` | 0.37 | 3/4 matched (target 3) | `ty_of_value_debug` | 0/0 matched (target 1) | _none_ | 1/1 | 1 | 10406.3 |
| 324 | `profile.heap` | `profile.Heap` | 0.38 | 27/28 matched | `r#gen` | 10/11 matched (target 12) | `Data` | 1/1 | 2 | 23906.2 |
| 325 | `type_compiled.alloc` | `type_compiled.Alloc` | 0.38 | 28/28 matched (target 37) | _none_ | 1/1 matched (target 3) | _none_ | - | 0 | 2906.2 |
| 326 | `types.ellipsis` | `types.Ellipsis` | 0.38 | 3/3 matched (target 5) | _none_ | 1/1 matched (target 2) | _none_ | 1/1 | 0 | 4000406.2 |
| 327 | `runtime.before_stmt` | `runtime.BeforeStmt` | 0.38 | 4/4 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 706.2 |
| 328 | `bc.instr_impl` | `bc.InstrImpl` | 0.38 | 87/87 matched (target 95) | _none_ | 87/163 matched (target 104) | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` | - | 76 | 785006.2 |
| 329 | `typing.ty` | `typing.Ty` | 0.39 | 49/52 matched (target 57) | `fmt`, `fmt`, `fmt` | 4/4 matched (target 6) | _none_ | - | 3 | 109035608.0 |
| 330 | `typing.basic` | `typing.Basic` | 0.40 | 18/19 matched (target 20) | `fmt` | 1/1 matched (target 11) | _none_ | - | 1 | 12006.0 |
| 331 | `types.any` | `types.Any` | 0.40 | 4/5 matched | `fmt` | 1/2 matched (target 1) | `Canonical` | - | 2 | 20706.0 |
| 332 | `typing.macro_support` | `typing.MacroSupport` | 0.41 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 205.9 |
| 333 | `bool.alloc` | `bool.Alloc` | 0.41 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 205.9 |
| 334 | `namespace.value` | `namespace.Value` | 0.41 | 13/15 matched (target 27) | `new`, `fmt` | 2/2 matched (target 7) | _none_ | 4/4 | 2 | 21705.9 |
| 335 | `compiler.type_expr` | `compiler.TypeExpr` | 0.41 | 2/2 matched | _none_ | 1/1 matched | _none_ | - | 0 | 305.9 |
| 336 | `compiler.error` | `compiler.Error` | 0.42 | 2/2 matched | _none_ | 1/1 matched | _none_ | - | 0 | 305.8 |
| 337 | `alloc.chunk_part` | `alloc.ChunkPart` | 0.43 | 12/15 matched (target 17) | `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero` | 1/1 matched (target 2) | _none_ | 1/4 | 3 | 2031605.8 |
| 338 | `funcs.zip` | `funcs.Zip` | 0.43 | 4/4 matched (target 7) | _none_ | 1/1 matched | _none_ | - | 0 | 505.7 |
| 339 | `typing.globals` | `typing.Globals` | 0.50 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 105.0 |
| 340 | `eval.bc.compiler.compr` | `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr` | 0.51 | 3/3 matched | _none_ | 0/0 matched | _none_ | - | 0 | 304.9 |
| 341 | `compiler.assign_modify` | `compiler.AssignModify` | 0.53 | 3/3 matched | _none_ | 0/1 matched (target 0) | `AssignOnWriteBc` | - | 1 | 10404.7 |
| 342 | `num.typecheck` | `num.Typecheck` | 0.54 | 2/2 matched | _none_ | 3/3 matched (target 5) | _none_ | - | 0 | 504.6 |
| 343 | `type_compiled.factory` | `type_compiled.Factory` | 0.58 | 9/9 matched | _none_ | 1/2 matched (target 1) | `Result` | - | 1 | 11104.2 |
| 344 | `hint` | `starlark.Hint` | 0.72 | 2/2 matched | _none_ | 0/0 matched | _none_ | - | 0 | 2000202.8 |
| 345 | `funcs.globals` | `funcs.Globals` | 1.00 | 1/1 matched | _none_ | 0/0 matched | _none_ | - | 0 | 100.0 |
| 346 | `layout.value_lifetimeless` | `layout.ValueLifetimeless` | 1.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 12000100.0 |
| 347 | `types.bool` | `types.Bool` | 1.00 | 0/0 matched | _none_ | 0/0 matched | _none_ | - | 0 | 1000000.0 |
| 348 | `allocator.api` | `allocator.Api` | 1.00 | 0/0 matched | _none_ | 2/2 matched | _none_ | - | 0 | 200.0 |
| 349 | `bc.instr` | `bc.Instr` | 1.00 | 0/0 matched | _none_ | 2/2 matched (target 5) | _none_ | - | 0 | 200.0 |
| 350 | `bc.slow_arg` | `bc.SlowArg` | 1.00 | 0/0 matched | _none_ | 2/2 matched | _none_ | - | 0 | 200.0 |
| 351 | `profile.or_instrumentation` | `profile.OrInstrumentation` | 1.00 | 0/0 matched | _none_ | 1/1 matched (target 4) | _none_ | - | 0 | 100.0 |
| 352 | `typing.call_args` | `typing.CallArgs` | 1.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 100.0 |
| 353 | `typing.mode` | `typing.Mode` | 1.00 | 0/0 matched | _none_ | 1/1 matched | _none_ | - | 0 | 100.0 |
| 354 | `values` | `values.Values` | 1.00 | 0/0 matched | _none_ | 0/0 matched (target 10) | _none_ | - | 0 | 0.0 |

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
- `collections.symbol` -> `collections.Symbol [ZERO]`: function-by-function score forced to 0. Symbol.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `private` -> `starlark.Private [ZERO]`: function-by-function score forced to 0. Private.kt: snake_case identifier `missing_docs` in Kotlin comments
- `layout.avalue` -> `layout.AValue [ZERO]`: function-by-function score forced to 0. AValue.kt: snake_case identifier `heap_freeze` in Kotlin comments; AValue.kt: Rust `let` binding in Kotlin comments; AValueTest.kt: snake_case identifier `try_freeze_directly` in Kotlin comments
- `typing.tuple` -> `typing.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `type_compiled_factory` in Kotlin comments
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]`: function-by-function score forced to 0. ConstFrozenString.kt: snake_case identifier `constant_string` in Kotlin comments; ConstFrozenStringTest.kt: snake_case identifier `const_frozen_string` in Kotlin comments; ConstFrozenStringTest.kt: Rust macro invocation in Kotlin comments
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
- `values.layout` -> `values.Layout [ZERO]`: function-by-function score forced to 0. Layout.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `types.array` -> `types.Array [ZERO]`: function-by-function score forced to 0. Array.kt: snake_case identifier `iter_count` in Kotlin comments; Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `eval.bc` -> `bc.Bc [ZERO]`: function-by-function score forced to 0. Bc.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]`: function-by-function score forced to 0. EnumType.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `bc.frame` -> `bc.Frame [ZERO]`: function-by-function score forced to 0. Frame.kt: snake_case identifier `loop_indices` in Kotlin comments
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]`: function-by-function score forced to 0. StarlarkValueAsType.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `values.value_of` -> `values.ValueOf [ZERO]`: function-by-function score forced to 0. ValueOf.kt: snake_case identifier `starlark_module` in Kotlin comments
- `alloc.chunk` -> `alloc.Chunk [ZERO]`: function-by-function score forced to 0. Chunk.kt: snake_case identifier `ref_count` in Kotlin comments; Chunk.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Chunk.kt: Rust-only type/unsafe terminology in Kotlin comments
- `record.record_type` -> `record.RecordType [ZERO]`: function-by-function score forced to 0. RecordType.kt: snake_case identifier `HAS_invoke` in Kotlin code; RecordType.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `list.alloc` -> `list.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `starlark_type_repr` in Kotlin comments; Alloc.kt: Rust `pub` item in Kotlin comments; no target functions found; report scoring is function-by-function only
- `compiler.constants` -> `compiler.Constants [ZERO]`: function-by-function score forced to 0. Constants.kt: snake_case identifier `starlark_module` in Kotlin comments; Constants.kt: Rust attribute syntax in Kotlin comments; Constants.kt: Rust lifetime explanation in Kotlin comments
- `profile.instant` -> `profile.Instant [ZERO]`: function-by-function score forced to 0. Instant.kt: snake_case identifier `thread_local` in Kotlin comments; Instant.kt: Rust `let` binding in Kotlin comments; Instant.kt: Rust attribute syntax in Kotlin comments
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO]`: function-by-function score forced to 0. CallStack.kt: snake_case identifier `call_stack` in Kotlin comments; CallStackTest.kt: snake_case identifier `call_stack` in Kotlin comments
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
- `collections.string_pool` -> `collections.StringPool [ZERO]`: function-by-function score forced to 0. StringPool.kt: snake_case identifier `debug_assert` in Kotlin comments; StringPool.kt: Rust macro invocation in Kotlin comments; StringPool.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]`: function-by-function score forced to 0. LocalAsValue.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; LocalAsValue.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `profile.string_index` -> `profile.StringIndex [ZERO]`: function-by-function score forced to 0. StringIndex.kt: translator-note comment (`Kotlin:`) in Kotlin comments
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
- `avalues.simple` -> `avalues.Simple [ZERO]`: function-by-function score forced to 0. Simple.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Simple.kt: Rust lifetime explanation in Kotlin comments
- `record.field` -> `record.Field [ZERO]`: function-by-function score forced to 0. Field.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]`: function-by-function score forced to 0. CheapCallStack.kt: translator-note comment (`Kotlin:`) in Kotlin comments; CheapCallStack.kt: Rust lifetime explanation in Kotlin comments
- `heap.fast_cell` -> `heap.FastCell [ZERO]`: function-by-function score forced to 0. FastCell.kt: snake_case identifier `debug_assert` in Kotlin comments; FastCell.kt: Rust macro invocation in Kotlin comments; FastCell.kt: translator-note comment (`Kotlin:`) in Kotlin comments; FastCell.kt: Rust-only type/unsafe terminology in Kotlin comments
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]`: function-by-function score forced to 0. UnorderedHasher.kt: snake_case identifier `wrapping_add` in Kotlin comments
- `typing.bindings` -> `typing.Bindings [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Bindings.kt: snake_case identifier `visit_children` in Kotlin comments
- `string.interpolation` -> `string.Interpolation [ZERO]`: function-by-function score forced to 0. Interpolation.kt: snake_case identifier `string_pool` in Kotlin comments
- `types.int` -> `types.Int [ZERO]`: function-by-function score forced to 0. Int.kt: snake_case identifier `num_bigint` in Kotlin comments
- `typing` -> `starlark.Typing [ZERO]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.function` -> `typing.Function [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Function.kt: Rust attribute syntax in Kotlin comments
- `set.methods` -> `set.Methods [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Methods.kt: snake_case identifier `starlark_module` in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `string.str_type` -> `string.StrType [ZERO]`: function-by-function score forced to 0. StrType.kt: snake_case identifier `starlark_value` in Kotlin comments; StrType.kt: Rust attribute syntax in Kotlin comments
- `int.int_or_big` -> `int.IntOrBig [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; IntOrBig.kt: snake_case identifier `from_str_radix` in Kotlin comments
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]`: function-by-function score forced to 0. ThinBox.kt: snake_case identifier `buck2_util` in Kotlin comments; ThinBox.kt: Rust-only type/unsafe terminology in Kotlin comments
- `layout.typed` -> `layout.ValueTyped [ZERO]`: function-by-function score forced to 0. ValueTyped.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing.callable` -> `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]`: function-by-function score forced to 0. Callable.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `scope.payload` -> `scope.Payload [ZERO]`: function-by-function score forced to 0. no target functions found; report scoring is function-by-function only
- `typing.user` -> `typing.User [ZERO]`: function-by-function score forced to 0. UserTest.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `typed.string` -> `typed.String [ZERO]`: function-by-function score forced to 0. String.kt: snake_case identifier `HAS_equals` in Kotlin code
- `bigint.convert` -> `bigint.Convert [ZERO]`: function-by-function score forced to 0. Convert.kt: Rust-only type/unsafe terminology in Kotlin comments
- `dict.value` -> `dict.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]`: function-by-function score forced to 0. VtableRegistry.kt: snake_case identifier `type_name` in Kotlin comments; VtableRegistry.kt: Rust lifetime explanation in Kotlin comments; VtableRegistryTest.kt: snake_case identifier `starlark_simple_value` in Kotlin comments; VtableRegistryTest.kt: Rust attribute syntax in Kotlin comments
- `record.globals` -> `record.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `rec_type` in Kotlin comments
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]`: function-by-function score forced to 0. AllocUnpack.kt: snake_case identifier `alloc_frozen_string_value` in Kotlin comments
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]`: function-by-function score forced to 0. PackedImpl.kt: Rust-only type/unsafe terminology in Kotlin comments
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]`: function-by-function score forced to 0. Compiled.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Compiled.kt: snake_case identifier `check_matches` in Kotlin comments
- `bc.instrs` -> `bc.Instrs [ZERO]`: function-by-function score forced to 0. Instrs.kt: snake_case identifier `drop_in_place` in Kotlin comments; Instrs.kt: Rust attribute syntax in Kotlin comments; Instrs.kt: Rust `match` expression in Kotlin comments; Instrs.kt: Rust lifetime explanation in Kotlin comments; Instrs.kt: Rust-only type/unsafe terminology in Kotlin comments
- `range.range_type` -> `range.RangeType [ZERO]`: function-by-function score forced to 0. RangeType.kt: snake_case identifier `HAS_iterate` in Kotlin code; RangeType.kt: snake_case identifier `saturating_mul` in Kotlin comments
- `tuple.unpack` -> `tuple.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `values.unpack` -> `values.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `starlark_module` in Kotlin comments
- `heap.heap_type` -> `heap.HeapType [ZERO]`: function-by-function score forced to 0. HeapType.kt: snake_case identifier `into_ref` in Kotlin comments; HeapType.kt: score-padding suppression annotation `@Suppress` in Kotlin code; HeapType.kt: Rust lifetime explanation in Kotlin comments; HeapType.kt: Rust-only type/unsafe terminology in Kotlin comments; HeapTypeTest.kt: snake_case identifier `test_send_sync` in Kotlin comments; HeapTypeTest.kt: Rust `fn` declaration in Kotlin comments
- `layout.complex` -> `layout.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `new_err` in Kotlin comments; Complex.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Complex.kt: translator-note comment (`Kotlin:`) in Kotlin comments; Complex.kt: Rust lifetime explanation in Kotlin comments
- `list.value` -> `list.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `set.value` -> `set.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code; Value.kt: snake_case identifier `starlark_value` in Kotlin comments; Value.kt: Rust attribute syntax in Kotlin comments; Value.kt: Rust-only type/unsafe terminology in Kotlin comments
- `adapter.implementation` -> `adapter.Implementation [ZERO]`: function-by-function score forced to 0. Implementation.kt: snake_case identifier `before_stmt` in Kotlin comments
- `compiler.scope` -> `compiler.Scope [ZERO]`: function-by-function score forced to 0. Scope.kt: snake_case identifier `scope_resolver_globals` in Kotlin comments
- `float.float` -> `float.Float [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Float.kt: snake_case identifier `HAS_equals` in Kotlin code; Float.kt: snake_case identifier `starlark_value` in Kotlin comments; Float.kt: Rust attribute syntax in Kotlin comments; Float.kt: Rust lifetime explanation in Kotlin comments
- `heap.send` -> `heap.Send [ZERO]`: function-by-function score forced to 0. Send.kt: Rust `fn` declaration in Kotlin comments; Send.kt: Rust lifetime explanation in Kotlin comments; Send.kt: Rust-only type/unsafe terminology in Kotlin comments
- `list.unpack` -> `list.Unpack [ZERO]`: function-by-function score forced to 0. Unpack.kt: snake_case identifier `unpack_value_opt` in Kotlin comments; Unpack.kt: Rust `let` binding in Kotlin comments; Unpack.kt: Rust attribute syntax in Kotlin comments; Unpack.kt: Rust lifetime explanation in Kotlin comments
- `string.repr` -> `string.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `is_printable` in Kotlin comments; Repr.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `string.simd` -> `string.Simd [ZERO]`: function-by-function score forced to 0. Simd.kt: snake_case identifier `x86_64` in Kotlin comments
- `assert.assert` -> `assert.Assert [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Assert.kt: snake_case identifier `assert_eq` in Kotlin comments; Assert.kt: Rust `pub` item in Kotlin comments; Assert.kt: Rust `use` path in Kotlin comments; Assert.kt: score-padding suppression annotation `@Suppress` in Kotlin code; Assert.kt: Rust lifetime explanation in Kotlin comments
- `bc.addr` -> `bc.Addr [ZERO]`: function-by-function score forced to 0. Addr.kt: snake_case identifier `debug_assert` in Kotlin comments; Addr.kt: Rust macro invocation in Kotlin comments
- `profile.aggregated` -> `profile.Aggregated [ZERO]`: function-by-function score forced to 0. Aggregated.kt: snake_case identifier `string_index` in Kotlin comments
- `stdlib.partial` -> `stdlib.Partial [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Partial.kt: snake_case identifier `HAS_invoke` in Kotlin code; Partial.kt: snake_case identifier `alloca_concat` in Kotlin comments
- `values.owned` -> `values.Owned [ZERO]`: function-by-function score forced to 0. Owned.kt: Rust lifetime explanation in Kotlin comments
- `alloc.allocator` -> `alloc.Allocator [ZERO]`: function-by-function score forced to 0. Allocator.kt: Rust `pub` item in Kotlin comments; Allocator.kt: Rust lifetime explanation in Kotlin comments; Allocator.kt: Rust-only type/unsafe terminology in Kotlin comments
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]`: function-by-function score forced to 0. Bumpalo.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `environment.modules` -> `environment.Modules [ZERO]`: function-by-function score forced to 0. Modules.kt: snake_case identifier `starlark_module` in Kotlin comments; Modules.kt: Rust `fn` declaration in Kotlin comments; Modules.kt: Rust `pub` item in Kotlin comments; Modules.kt: Rust attribute syntax in Kotlin comments; Modules.kt: Rust lifetime explanation in Kotlin comments
- `tuple.value` -> `tuple.Value [ZERO]`: function-by-function score forced to 0. Value.kt: snake_case identifier `HAS_iterate` in Kotlin code
- `alloc.chain` -> `alloc.Chain [ZERO]`: function-by-function score forced to 0. Chain.kt: Rust attribute syntax in Kotlin comments
- `dict.alloc` -> `dict.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments
- `heap.repr` -> `heap.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `forward_ptr` in Kotlin comments; Repr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `params.spec` -> `params.Spec [ZERO]`: function-by-function score forced to 0. Spec.kt: snake_case identifier `no_args` in Kotlin comments
- `structs.alloc` -> `structs.Alloc [ZERO]`: function-by-function score forced to 0. Alloc.kt: snake_case identifier `frozen_heap` in Kotlin comments; Alloc.kt: Rust `fn` declaration in Kotlin comments; Alloc.kt: Rust `let` binding in Kotlin comments; Alloc.kt: Rust `use` path in Kotlin comments; Alloc.kt: Rust lifetime explanation in Kotlin comments; no target functions found; report scoring is function-by-function only
- `typing.custom` -> `typing.Custom [ZERO]`: function-by-function score forced to 0. Custom.kt: snake_case identifier `validate_call` in Kotlin comments; Custom.kt: Rust `fn` declaration in Kotlin comments; Custom.kt: Rust `pub` item in Kotlin comments; Custom.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.instr_arg` -> `bc.InstrArg [ZERO]`: function-by-function score forced to 0. InstrArg.kt: snake_case identifier `fmt_append` in Kotlin comments; InstrArg.kt: Rust-only type/unsafe terminology in Kotlin comments
- `bc.opcode` -> `bc.Opcode [ZERO]`: function-by-function score forced to 0. Opcode.kt: snake_case identifier `dispatch_all` in Kotlin comments
- `bc.repr` -> `bc.Repr [ZERO]`: function-by-function score forced to 0. Repr.kt: snake_case identifier `align_of` in Kotlin comments
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]`: function-by-function score forced to 0. StackPtr.kt: snake_case identifier `assert_eq` in Kotlin comments; StackPtr.kt: Rust macro invocation in Kotlin comments; StackPtr.kt: Rust-only type/unsafe terminology in Kotlin comments
- `compiler.def` -> `compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: snake_case identifier `HAS_invoke` in Kotlin code; Def.kt: unchecked cast suppression hiding transliteration work in Kotlin code; Def.kt: Rust-only type/unsafe terminology in Kotlin comments
- `debug.inspect` -> `debug.Inspect [ZERO]`: function-by-function score forced to 0. Inspect.kt: snake_case identifier `call_stack` in Kotlin comments
- `eval.bc.compiler.call` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.stmt` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `list.refs` -> `list.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `from_value` in Kotlin comments; Refs.kt: Rust lifetime explanation in Kotlin comments
- `symbol.map` -> `symbol.Map [ZERO]`: function-by-function score forced to 0. Map.kt: Rust-only type/unsafe terminology in Kotlin comments
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]`: function-by-function score forced to 0. Matcher.kt: snake_case identifier `type_matcher` in Kotlin comments; Matcher.kt: Rust attribute syntax in Kotlin comments; Matcher.kt: Rust lifetime explanation in Kotlin comments; Matcher.kt: Rust-only type/unsafe terminology in Kotlin comments; Matcher.kt: Rust auto-trait terminology in Kotlin comments
- `avalues.array` -> `avalues.Array [ZERO]`: function-by-function score forced to 0. Array.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `avalues.complex` -> `avalues.Complex [ZERO]`: function-by-function score forced to 0. Complex.kt: snake_case identifier `alloc_complex` in Kotlin comments
- `avalues.static_` -> `avalues.Static [ZERO]`: function-by-function score forced to 0. Static.kt: snake_case identifier `HAS_invoke` in Kotlin code
- `avalues.tuple` -> `avalues.Tuple [ZERO]`: function-by-function score forced to 0. Tuple.kt: snake_case identifier `register_special_avalue_frozen` in Kotlin comments; Tuple.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `bc.call` -> `bc.Call [ZERO]`: function-by-function score forced to 0. Call.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `bool.type_repr` -> `bool.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: translator-note comment (`Kotlin:`) in Kotlin comments
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]`: function-by-function score forced to 0. MaybeUninitBackport.kt: snake_case identifier `write_slice_cloned` in Kotlin comments
- `compiler.expr` -> `compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `get_methods` in Kotlin comments; Expr.kt: Rust `let` binding in Kotlin comments
- `debug.evaluate` -> `debug.Evaluate [ZERO]`: function-by-function score forced to 0. Evaluate.kt: snake_case identifier `module_env` in Kotlin comments
- `funcs.min_max` -> `funcs.MinMax [ZERO]`: function-by-function score forced to 0. MinMax.kt: snake_case identifier `update_max_ordering` in Kotlin comments; MinMax.kt: Rust `match` expression in Kotlin comments
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]`: function-by-function score forced to 0. CallEnterExit.kt: snake_case identifier `needs_drop` in Kotlin comments
- `list.globals` -> `list.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `register_list` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `params.parser` -> `params.Parser [ZERO]`: function-by-function score forced to 0. Parser.kt: snake_case identifier `get_next` in Kotlin comments
- `profile.stmt` -> `profile.Stmt [ZERO]`: function-by-function score forced to 0. Stmt.kt: snake_case identifier `last_span` in Kotlin comments; Stmt.kt: Rust-only type/unsafe terminology in Kotlin comments
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]`: function-by-function score forced to 0. SummaryByFunction.kt: snake_case identifier `top_stack` in Kotlin comments
- `profile.typecheck` -> `profile.Typecheck [ZERO]`: function-by-function score forced to 0. Typecheck.kt: snake_case identifier `by_function` in Kotlin comments
- `record.instance` -> `record.Instance [ZERO]`: function-by-function score forced to 0. Instance.kt: snake_case identifier `HAS_equals` in Kotlin code; Instance.kt: snake_case identifier `starlark_complex_value` in Kotlin comments
- `set.refs` -> `set.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `unpack_value_opt` in Kotlin comments
- `structs.refs` -> `structs.Refs [ZERO]`: function-by-function score forced to 0. Refs.kt: snake_case identifier `debug_assert` in Kotlin comments
- `structs.value` -> `structs.Value [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Value.kt: snake_case identifier `HAS_equals` in Kotlin code; Value.kt: snake_case identifier `of_value` in Kotlin comments; Value.kt: Rust lifetime explanation in Kotlin comments
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `eval_type` in Kotlin comments
- `types.function` -> `types.Function [ZERO]`: function-by-function score forced to 0. Function.kt: snake_case identifier `HAS_invoke` in Kotlin code; Function.kt: snake_case identifier `starlark_module` in Kotlin comments; Function.kt: Rust attribute syntax in Kotlin comments
- `typing.iter` -> `typing.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Iter.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]`: function-by-function score forced to 0. RecursiveReprOrJsonGuard.kt: snake_case identifier `to_json` in Kotlin comments
- `values.traits` -> `values.Traits [ZERO]`: function-by-function score forced to 0. Traits.kt: snake_case identifier `HAS_invoke` in Kotlin code; Traits.kt: snake_case identifier `starlark_value` in Kotlin comments; Traits.kt: Rust `let` binding in Kotlin comments; Traits.kt: Rust attribute syntax in Kotlin comments
- `values.type_repr` -> `values.TypeRepr [ZERO]`: function-by-function score forced to 0. TypeRepr.kt: snake_case identifier `type_repr` in Kotlin comments
- `analysis` -> `starlark.Analysis [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]`: function-by-function score forced to 0. IfCompiler.kt: snake_case identifier `maybe_not` in Kotlin comments
- `debug.adapter` -> `debug.Adapter [ZERO]`: function-by-function score forced to 0. Adapter.kt: snake_case identifier `Requests_SetBreakpoints` in Kotlin comments
- `dict.globals` -> `dict.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `as_type` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `docs` -> `docs.Docs [ZERO]`: function-by-function score forced to 0. Docs.kt: Rust lifetime explanation in Kotlin comments
- `docs.parse` -> `docs.Parse [ZERO]`: function-by-function score forced to 0. Parse.kt: snake_case identifier `some_function` in Kotlin comments; Parse.kt: Rust attribute syntax in Kotlin comments; ParseTest.kt: snake_case identifier `from_docstring` in Kotlin comments
- `float.globals` -> `float.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `funcs.other` -> `funcs.Other [STUB]`: function-by-function score forced to 0. target contains TODO/stub/placeholder markers in function bodies; Other.kt: Rust lifetime explanation in Kotlin comments
- `int.globals` -> `int.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments
- `list.methods` -> `list.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `starlark_syntax` in Kotlin comments; Methods.kt: Rust `let` binding in Kotlin comments; Methods.kt: Rust attribute syntax in Kotlin comments
- `profile.by_type` -> `profile.ByType [ZERO]`: function-by-function score forced to 0. ByType.kt: snake_case identifier `allocated_summary` in Kotlin comments
- `set.set` -> `set.Set [ZERO]`: function-by-function score forced to 0. Set.kt: snake_case identifier `starlark_module` in Kotlin comments; Set.kt: Rust attribute syntax in Kotlin comments
- `structs.structs` -> `structs.Structs [ZERO]`: function-by-function score forced to 0. Structs.kt: snake_case identifier `starlark_module` in Kotlin comments; Structs.kt: Rust attribute syntax in Kotlin comments
- `typing.never` -> `typing.Never [ZERO]`: function-by-function score forced to 0. Never.kt: snake_case identifier `HAS_eval_type` in Kotlin code; Never.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `alloc.per_thread` -> `alloc.PerThread [ZERO]`: function-by-function score forced to 0. PerThread.kt: snake_case identifier `thread_local` in Kotlin comments
- `bc.for_loop` -> `bc.ForLoop [ZERO]`: function-by-function score forced to 0. ForLoop.kt: snake_case identifier `derive_more` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `bc.writer` -> `bc.Writer [ZERO]`: function-by-function score forced to 0. Writer.kt: snake_case identifier `alloc_any` in Kotlin comments
- `compiler.assign` -> `compiler.Assign [ZERO]`: function-by-function score forced to 0. Assign.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `compiler.types` -> `compiler.Types [ZERO]`: function-by-function score forced to 0. Types.kt: snake_case identifier `expr_ident` in Kotlin comments
- `dict.methods` -> `dict.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: Rust lifetime explanation in Kotlin comments
- `docs.code` -> `docs.Code [ZERO]`: function-by-function score forced to 0. Code.kt: snake_case identifier `render_as_code` in Kotlin comments
- `docs.markdown` -> `docs.Markdown [ZERO]`: function-by-function score forced to 0. Markdown.kt: Rust `let` binding in Kotlin comments
- `environment` -> `starlark.Environment [ZERO]`: function-by-function score forced to 0. Environment.kt: snake_case identifier `module_dump` in Kotlin comments; Environment.kt: Rust `pub` item in Kotlin comments; Environment.kt: Rust `use` path in Kotlin comments
- `environment.names` -> `environment.Names [ZERO]`: function-by-function score forced to 0. Names.kt: snake_case identifier `collect_defines_lvalue` in Kotlin comments
- `errors` -> `starlark.Errors [ZERO]`: function-by-function score forced to 0. Errors.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.def` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]`: function-by-function score forced to 0. Def.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `eval.bc.compiler.expr` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]`: function-by-function score forced to 0. Expr.kt: snake_case identifier `mark_definitely_assigned_after` in Kotlin comments
- `eval.runtime` -> `eval.Runtime [ZERO]`: function-by-function score forced to 0. Runtime.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `intern.interner` -> `intern.Interner [ZERO]`: function-by-function score forced to 0. Interner.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.avalues` -> `layout.AValues [ZERO]`: function-by-function score forced to 0. AValues.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `layout.static_string` -> `layout.StaticString [ZERO]`: function-by-function score forced to 0. StaticString.kt: Rust attribute syntax in Kotlin comments; StaticString.kt: Rust-only type/unsafe terminology in Kotlin comments
- `macros` -> `starlark.Macros [ZERO]`: function-by-function score forced to 0. no source functions found; target defines functions; report scoring is function-by-function only
- `pagable` -> `starlark.Pagable [ZERO]`: function-by-function score forced to 0. Pagable.kt: snake_case identifier `type_name` in Kotlin comments; Pagable.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]`: function-by-function score forced to 0. VtableRegister.kt: snake_case identifier `register_avalue_simple_frozen` in Kotlin comments; no source functions found; target defines functions; report scoring is function-by-function only
- `record.ty_record_type` -> `record.TyRecordType [ZERO]`: function-by-function score forced to 0. TyRecordType.kt: Rust `pub` item in Kotlin comments; TyRecordType.kt: Rust attribute syntax in Kotlin comments
- `stdlib.funcs` -> `stdlib.Funcs [ZERO]`: function-by-function score forced to 0. Funcs.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `string.globals` -> `string.Globals [ZERO]`: function-by-function score forced to 0. Globals.kt: snake_case identifier `starlark_module` in Kotlin comments; Globals.kt: Rust attribute syntax in Kotlin comments; Globals.kt: Rust lifetime explanation in Kotlin comments
- `string.iter` -> `string.Iter [ZERO]`: function-by-function score forced to 0. Iter.kt: snake_case identifier `HAS_iterate` in Kotlin code; Iter.kt: snake_case identifier `produce_char` in Kotlin comments
- `string.methods` -> `string.Methods [ZERO]`: function-by-function score forced to 0. Methods.kt: snake_case identifier `is_true` in Kotlin comments; Methods.kt: Rust lifetime explanation in Kotlin comments
- `syntax` -> `starlark.Syntax [ZERO]`: function-by-function score forced to 0. Syntax.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]`: function-by-function score forced to 0. Matchers.kt: snake_case identifier `starlark_type_id` in Kotlin comments; Matchers.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.any` -> `typing.Any [ZERO]`: function-by-function score forced to 0. Any.kt: snake_case identifier `HAS_eval_type` in Kotlin code
- `typing.error` -> `typing.Error [ZERO]`: function-by-function score forced to 0. Error.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]`: function-by-function score forced to 0. FillTypesForLint.kt: snake_case identifier `of_value` in Kotlin comments; FillTypesForLint.kt: Rust `match` expression in Kotlin comments
- `typing.oracle` -> `typing.Oracle [ZERO]`: function-by-function score forced to 0. Oracle.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]`: function-by-function score forced to 0. Remove.kt: Rust lifetime explanation in Kotlin comments
- `util` -> `starlark.Util [ZERO]`: function-by-function score forced to 0. Util.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.index` -> `values.Index [ZERO]`: function-by-function score forced to 0. Index.kt: snake_case identifier `set_at` in Kotlin comments; IndexTest.kt: snake_case identifier `convert_index` in Kotlin comments
- `values.types` -> `values.Types [ZERO]`: function-by-function score forced to 0. Types.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing` -> `values.Typing [ZERO]`: function-by-function score forced to 0. Typing.kt: score-padding suppression annotation `@Suppress` in Kotlin code
- `values.typing.ty` -> `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]`: function-by-function score forced to 0. Ty.kt: snake_case identifier `HAS_eval_type` in Kotlin code

### Critical Ports (Similarity < 0.60, Worst First)

These files need significant work:

- `typing.starlark_value` -> `typing.StarlarkValue [ZERO]` (0.00, 76 deps)
- `runtime.evaluator` -> `runtime.Evaluator [ZERO]` (0.00, 55 deps)
- `values.trace` -> `values.Trace [ZERO]` (0.00, 52 deps)
- `values.freeze` -> `values.Freeze [ZERO]` (0.00, 42 deps)
- `values.alloc_value` -> `values.AllocValue [ZERO]` (0.00, 42 deps)
- `layout.freezer` -> `layout.Freezer [ZERO]` (0.00, 36 deps)
- `values.frozen_ref` -> `values.FrozenRef [ZERO]` (0.00, 27 deps)
- `none.none_type` -> `none.NoneType [ZERO]` (0.00, 27 deps)
- `runtime.arguments` -> `runtime.Arguments [ZERO]` (0.00, 25 deps)
- `typing.type_compiled` -> `type_compiled.TypeCompiled [ZERO]` (0.00, 22 deps)
- `values.value_of_unchecked` -> `values.ValueOfUnchecked [ZERO]` (0.00, 20 deps)
- `environment.globals` -> `environment.Globals [ZERO]` (0.00, 20 deps)
- `environment.methods` -> `environment.Methods [ZERO]` (0.00, 17 deps)
- `values.iter` -> `values.Iter [ZERO]` (0.00, 17 deps)
- `collections.symbol` -> `collections.Symbol [ZERO]` (0.00, 15 deps)
- `private` -> `starlark.Private [ZERO]` (0.00, 15 deps)
- `layout.avalue` -> `layout.AValue [ZERO]` (0.00, 14 deps)
- `typing.tuple` -> `typing.Tuple [ZERO]` (0.00, 12 deps)
- `layout.const_frozen_string` -> `layout.ConstFrozenString [ZERO]` (0.00, 12 deps)
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
- `values.layout` -> `values.Layout [ZERO]` (0.00, 6 deps)
- `types.array` -> `types.Array [ZERO]` (0.00, 5 deps)
- `eval.bc` -> `bc.Bc [ZERO]` (0.00, 5 deps)
- `enumeration.enum_type` -> `enumeration.EnumType [ZERO]` (0.00, 4 deps)
- `bc.frame` -> `bc.Frame [ZERO]` (0.00, 4 deps)
- `types.starlark_value_as_type` -> `types.StarlarkValueAsType [ZERO]` (0.00, 4 deps)
- `values.value_of` -> `values.ValueOf [ZERO]` (0.00, 4 deps)
- `alloc.chunk` -> `alloc.Chunk [ZERO]` (0.00, 3 deps)
- `record.record_type` -> `record.RecordType [ZERO]` (0.00, 3 deps)
- `list.alloc` -> `list.Alloc [ZERO]` (0.00, 3 deps)
- `compiler.constants` -> `compiler.Constants [ZERO]` (0.00, 3 deps)
- `profile.instant` -> `profile.Instant [ZERO]` (0.00, 3 deps)
- `stdlib.call_stack` -> `stdlib.CallStack [ZERO]` (0.00, 3 deps)
- `values.unpack_and_discard` -> `values.UnpackAndDiscard [ZERO]` (0.00, 3 deps)
- `errors.did_you_mean` -> `errors.DidYouMean [ZERO]` (0.00, 3 deps)
- `sealed` -> `starlark.Sealed [ZERO]` (0.00, 3 deps)
- `types.record` -> `types.Record [ZERO]` (0.00, 3 deps)
- `compiler.small_vec_1` -> `compiler.SmallVec1 [ZERO]` (0.00, 2 deps)
- `values.owned_frozen_ref` -> `values.OwnedFrozenRef [ZERO]` (0.00, 2 deps)
- `layout.const_type_id` -> `layout.ConstTypeId [ZERO]` (0.00, 2 deps)
- `avalues.str_` -> `avalues.Str [ZERO]` (0.00, 2 deps)
- `runtime.rust_loc` -> `runtime.RustLoc [ZERO]` (0.00, 2 deps)
- `values.stack_guard` -> `values.StackGuard [ZERO]` (0.00, 2 deps)
- `collections.string_pool` -> `collections.StringPool [ZERO]` (0.00, 2 deps)
- `def_inline.local_as_value` -> `def_inline.LocalAsValue [ZERO]` (0.00, 2 deps)
- `profile.string_index` -> `profile.StringIndex [ZERO]` (0.00, 2 deps)
- `values.thin_box_slice_frozen_value` -> `values.ThinBoxSliceFrozenValue [ZERO]` (0.00, 2 deps)
- `heap.arena` -> `heap.Arena [ZERO]` (0.00, 1 deps)
- `collections.alloca` -> `collections.Alloca [ZERO]` (0.00, 1 deps)
- `stdlib` -> `starlark.Stdlib [ZERO]` (0.00, 1 deps)
- `types.list_or_tuple` -> `types.ListOrTuple [ZERO]` (0.00, 1 deps)
- `layout.pointer` -> `layout.Pointer [ZERO]` (0.00, 1 deps)
- `types.any_complex` -> `types.AnyComplex [ZERO]` (0.00, 1 deps)
- `types.any_array` -> `types.AnyArray [ZERO]` (0.00, 1 deps)
- `util.rtabort` -> `util.Rtabort [ZERO]` (0.00, 1 deps)
- `bc.if_debug` -> `bc.IfDebug [ZERO]` (0.00, 1 deps)
- `read_line` -> `starlark.ReadLine [ZERO]` (0.00, 1 deps)
- `util.non_static_type_id` -> `util.NonStaticTypeId [ZERO]` (0.00, 1 deps)
- `avalues.simple` -> `avalues.Simple [ZERO]` (0.00, 1 deps)
- `record.field` -> `record.Field [ZERO]` (0.00, 1 deps)
- `runtime.cheap_call_stack` -> `runtime.CheapCallStack [ZERO]` (0.00, 1 deps)
- `heap.fast_cell` -> `heap.FastCell [ZERO]` (0.00, 1 deps)
- `structs.unordered_hasher` -> `structs.UnorderedHasher [ZERO]` (0.00, 1 deps)
- `typing.bindings` -> `typing.Bindings [STUB]` (0.00, 1 deps)
- `string.interpolation` -> `string.Interpolation [ZERO]` (0.00, 1 deps)
- `types.int` -> `types.Int [ZERO]` (0.00, 1 deps)
- `typing` -> `starlark.Typing [ZERO]` (0.00, 1 deps)
- `typing.function` -> `typing.Function [STUB]` (0.00, 1 deps)
- `set.methods` -> `set.Methods [STUB]` (0.00)
- `string.str_type` -> `string.StrType [ZERO]` (0.00)
- `int.int_or_big` -> `int.IntOrBig [STUB]` (0.00)
- `thin_box_slice_frozen_value.thin_box` -> `thinboxslicefrozenvalue.ThinBox [ZERO]` (0.00)
- `layout.typed` -> `layout.ValueTyped [ZERO]` (0.00)
- `values.typing.callable` -> `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` (0.00)
- `scope.payload` -> `scope.Payload [ZERO]` (0.00)
- `typing.user` -> `typing.User [ZERO]` (0.00)
- `typed.string` -> `typed.String [ZERO]` (0.00)
- `bigint.convert` -> `bigint.Convert [ZERO]` (0.00)
- `dict.value` -> `dict.Value [ZERO]` (0.00)
- `pagable.vtable_registry` -> `pagable.VtableRegistry [ZERO]` (0.00)
- `record.globals` -> `record.Globals [ZERO]` (0.00)
- `string.alloc_unpack` -> `string.AllocUnpack [ZERO]` (0.00)
- `thin_box_slice_frozen_value.packed_impl` -> `thinboxslicefrozenvalue.PackedImpl [ZERO]` (0.00)
- `type_compiled.compiled` -> `type_compiled.Compiled [ZERO]` (0.00)
- `bc.instrs` -> `bc.Instrs [ZERO]` (0.00)
- `range.range_type` -> `range.RangeType [ZERO]` (0.00)
- `tuple.unpack` -> `tuple.Unpack [ZERO]` (0.00)
- `values.unpack` -> `values.Unpack [ZERO]` (0.00)
- `heap.heap_type` -> `heap.HeapType [ZERO]` (0.00)
- `layout.complex` -> `layout.Complex [ZERO]` (0.00)
- `list.value` -> `list.Value [ZERO]` (0.00)
- `set.value` -> `set.Value [ZERO]` (0.00)
- `adapter.implementation` -> `adapter.Implementation [ZERO]` (0.00)
- `compiler.scope` -> `compiler.Scope [ZERO]` (0.00)
- `float.float` -> `float.Float [STUB]` (0.00)
- `heap.send` -> `heap.Send [ZERO]` (0.00)
- `list.unpack` -> `list.Unpack [ZERO]` (0.00)
- `string.repr` -> `string.Repr [ZERO]` (0.00)
- `string.simd` -> `string.Simd [ZERO]` (0.00)
- `assert.assert` -> `assert.Assert [STUB]` (0.00)
- `bc.addr` -> `bc.Addr [ZERO]` (0.00)
- `profile.aggregated` -> `profile.Aggregated [ZERO]` (0.00)
- `stdlib.partial` -> `stdlib.Partial [STUB]` (0.00)
- `values.owned` -> `values.Owned [ZERO]` (0.00)
- `alloc.allocator` -> `alloc.Allocator [ZERO]` (0.00)
- `allocator.bumpalo` -> `allocator.Bumpalo [ZERO]` (0.00)
- `environment.modules` -> `environment.Modules [ZERO]` (0.00)
- `tuple.value` -> `tuple.Value [ZERO]` (0.00)
- `alloc.chain` -> `alloc.Chain [ZERO]` (0.00)
- `dict.alloc` -> `dict.Alloc [ZERO]` (0.00)
- `heap.repr` -> `heap.Repr [ZERO]` (0.00)
- `params.spec` -> `params.Spec [ZERO]` (0.00)
- `structs.alloc` -> `structs.Alloc [ZERO]` (0.00)
- `typing.custom` -> `typing.Custom [ZERO]` (0.00)
- `bc.instr_arg` -> `bc.InstrArg [ZERO]` (0.00)
- `bc.opcode` -> `bc.Opcode [ZERO]` (0.00)
- `bc.repr` -> `bc.Repr [ZERO]` (0.00)
- `bc.stack_ptr` -> `bc.StackPtr [ZERO]` (0.00)
- `compiler.def` -> `compiler.Def [ZERO]` (0.00)
- `debug.inspect` -> `debug.Inspect [ZERO]` (0.00)
- `eval.bc.compiler.call` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]` (0.00)
- `eval.bc.compiler.stmt` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]` (0.00)
- `list.refs` -> `list.Refs [ZERO]` (0.00)
- `symbol.map` -> `symbol.Map [ZERO]` (0.00)
- `type_compiled.matcher` -> `type_compiled.Matcher [ZERO]` (0.00)
- `avalues.array` -> `avalues.Array [ZERO]` (0.00)
- `avalues.complex` -> `avalues.Complex [ZERO]` (0.00)
- `avalues.static_` -> `avalues.Static [ZERO]` (0.00)
- `avalues.tuple` -> `avalues.Tuple [ZERO]` (0.00)
- `bc.call` -> `bc.Call [ZERO]` (0.00)
- `bool.type_repr` -> `bool.TypeRepr [ZERO]` (0.00)
- `collections.maybe_uninit_backport` -> `collections.MaybeUninitBackport [ZERO]` (0.00)
- `compiler.expr` -> `compiler.Expr [ZERO]` (0.00)
- `debug.evaluate` -> `debug.Evaluate [ZERO]` (0.00)
- `funcs.min_max` -> `funcs.MinMax [ZERO]` (0.00)
- `heap.call_enter_exit` -> `heap.CallEnterExit [ZERO]` (0.00)
- `list.globals` -> `list.Globals [ZERO]` (0.00)
- `params.parser` -> `params.Parser [ZERO]` (0.00)
- `profile.stmt` -> `profile.Stmt [ZERO]` (0.00)
- `profile.summary_by_function` -> `profile.SummaryByFunction [ZERO]` (0.00)
- `profile.typecheck` -> `profile.Typecheck [ZERO]` (0.00)
- `record.instance` -> `record.Instance [ZERO]` (0.00)
- `set.refs` -> `set.Refs [ZERO]` (0.00)
- `structs.refs` -> `structs.Refs [ZERO]` (0.00)
- `structs.value` -> `structs.Value [STUB]` (0.00)
- `type_compiled.globals` -> `type_compiled.Globals [ZERO]` (0.00)
- `types.function` -> `types.Function [ZERO]` (0.00)
- `typing.iter` -> `typing.Iter [ZERO]` (0.00)
- `values.recursive_repr_or_json_guard` -> `values.RecursiveReprOrJsonGuard [ZERO]` (0.00)
- `values.traits` -> `values.Traits [ZERO]` (0.00)
- `values.type_repr` -> `values.TypeRepr [ZERO]` (0.00)
- `analysis` -> `starlark.Analysis [STUB]` (0.00)
- `compiler.if_compiler` -> `compiler.IfCompiler [ZERO]` (0.00)
- `debug.adapter` -> `debug.Adapter [ZERO]` (0.00)
- `dict.globals` -> `dict.Globals [ZERO]` (0.00)
- `docs` -> `docs.Docs [ZERO]` (0.00)
- `docs.parse` -> `docs.Parse [ZERO]` (0.00)
- `float.globals` -> `float.Globals [ZERO]` (0.00)
- `funcs.other` -> `funcs.Other [STUB]` (0.00)
- `int.globals` -> `int.Globals [ZERO]` (0.00)
- `list.methods` -> `list.Methods [ZERO]` (0.00)
- `profile.by_type` -> `profile.ByType [ZERO]` (0.00)
- `set.set` -> `set.Set [ZERO]` (0.00)
- `structs.structs` -> `structs.Structs [ZERO]` (0.00)
- `typing.never` -> `typing.Never [ZERO]` (0.00)
- `alloc.per_thread` -> `alloc.PerThread [ZERO]` (0.00)
- `bc.for_loop` -> `bc.ForLoop [ZERO]` (0.00)
- `bc.writer` -> `bc.Writer [ZERO]` (0.00)
- `compiler.assign` -> `compiler.Assign [ZERO]` (0.00)
- `compiler.types` -> `compiler.Types [ZERO]` (0.00)
- `dict.methods` -> `dict.Methods [ZERO]` (0.00)
- `docs.code` -> `docs.Code [ZERO]` (0.00)
- `docs.markdown` -> `docs.Markdown [ZERO]` (0.00)
- `environment` -> `starlark.Environment [ZERO]` (0.00)
- `environment.names` -> `environment.Names [ZERO]` (0.00)
- `errors` -> `starlark.Errors [ZERO]` (0.00)
- `eval.bc.compiler.def` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]` (0.00)
- `eval.bc.compiler.expr` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]` (0.00)
- `eval.runtime` -> `eval.Runtime [ZERO]` (0.00)
- `intern.interner` -> `intern.Interner [ZERO]` (0.00)
- `layout.avalues` -> `layout.AValues [ZERO]` (0.00)
- `layout.static_string` -> `layout.StaticString [ZERO]` (0.00)
- `macros` -> `starlark.Macros [ZERO]` (0.00)
- `pagable` -> `starlark.Pagable [ZERO]` (0.00)
- `pagable.vtable_register` -> `pagable.VtableRegister [ZERO]` (0.00)
- `record.ty_record_type` -> `record.TyRecordType [ZERO]` (0.00)
- `stdlib.funcs` -> `stdlib.Funcs [ZERO]` (0.00)
- `string.globals` -> `string.Globals [ZERO]` (0.00)
- `string.iter` -> `string.Iter [ZERO]` (0.00)
- `string.methods` -> `string.Methods [ZERO]` (0.00)
- `syntax` -> `starlark.Syntax [ZERO]` (0.00)
- `type_compiled.matchers` -> `type_compiled.Matchers [ZERO]` (0.00)
- `typing.any` -> `typing.Any [ZERO]` (0.00)
- `typing.error` -> `typing.Error [ZERO]` (0.00)
- `typing.fill_types_for_lint` -> `typing.FillTypesForLint [ZERO]` (0.00)
- `typing.oracle` -> `typing.Oracle [ZERO]` (0.00)
- `unused_loads.remove` -> `unusedloads.Remove [ZERO]` (0.00)
- `util` -> `starlark.Util [ZERO]` (0.00)
- `values.index` -> `values.Index [ZERO]` (0.00)
- `values.types` -> `values.Types [ZERO]` (0.00)
- `values.typing` -> `values.Typing [ZERO]` (0.00)
- `values.typing.ty` -> `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]` (0.00)
- `types.type_instance_id` -> `types.TypeInstanceId` (0.00, 9 deps)
- `runtime.visit_span` -> `runtime.VisitSpan` (0.00)
- `tuple.rust_tuple` -> `tuple.RustTuple` (0.00)
- `bool.unpack` -> `bool.Unpack` (0.00)
- `enumeration.ty_enum_type` -> `enumeration.TyEnumType` (0.00)
- `pagable.error` -> `pagable.Error` (0.00)
- `wasm` -> `starlark.Wasm` (0.00)
- `enumeration.globals` -> `enumeration.Globals` (0.02)
- `bool.globals` -> `bool.Globals` (0.03)
- `int.i32` -> `int.I32` (0.03)
- `heap.maybe_uninit_slice_util` -> `heap.MaybeUninitSliceUtil` (0.04)
- `namespace.globals` -> `namespace.Globals` (0.05)
- `num.globals` -> `num.Globals` (0.05)
- `range.globals` -> `range.Globals` (0.05)
- `profile.mode` -> `profile.Mode` (0.05)
- `tuple.globals` -> `tuple.Globals` (0.06)
- `collections.aligned_padded_str` -> `alignedpaddedstr.AlignedPaddedStr` (0.06, 2 deps)
- `oracle.traits` -> `oracle.Traits` (0.08)
- `environment.module_dump` -> `environment.ModuleDump` (0.09)
- `runtime.file_loader` -> `runtime.FileLoader` (0.09, 2 deps)
- `list.list_type` -> `list.ListType` (0.09, 3 deps)
- `analysis.types` -> `analysis.Types` (0.10)
- `dict.unpack` -> `dict.Unpack` (0.10)
- `dict.traits` -> `dict.Traits` (0.10)
- `profile.csv` -> `profile.Csv` (0.11)
- `avalues.list` -> `avalues.List` (0.11)
- `enumeration.value` -> `enumeration.Value` (0.12)
- `typing.type_type` -> `typing.TypeType` (0.12, 2 deps)
- `any` -> `starlark.Any` (0.12, 8 deps)
- `typing.small_arc_vec_or_static` -> `typing.SmallArcVecOrStatic` (0.13)
- `dict.refs` -> `dict.Refs` (0.13)
- `stdlib.extra` -> `stdlib.Extra` (0.13)
- `analysis.underscore` -> `analysis.Underscore` (0.13)
- `bc.definitely_assigned` -> `bc.DefinitelyAssigned` (0.14)
- `stdlib.json` -> `stdlib.Json` (0.14)
- `profile.data` -> `profile.Data` (0.15)
- `compiler.args` -> `compiler.Args` (0.16)
- `layout.value_captured` -> `layout.ValueCaptured` (0.16, 1 deps)
- `typing.callable_param` -> `typing.CallableParam` (0.16)
- `typing.small_arc_vec` -> `typing.SmallArcVec` (0.17)
- `types.unbound` -> `types.Unbound` (0.17)
- `values.demand` -> `values.Demand` (0.17, 4 deps)
- `symbol.symbol` -> `symbol.Symbol` (0.18)
- `bc.native_function` -> `bc.NativeFunction` (0.18, 4 deps)
- `profile.alloc_counts` -> `profile.AllocCounts` (0.18, 4 deps)
- `compiler.opt_ctx` -> `compiler.OptCtx` (0.18, 7 deps)
- `scope.scope_resolver_globals` -> `scope.ScopeResolverGlobals` (0.19, 5 deps)
- `typing.interface` -> `typing.Interface` (0.19, 5 deps)
- `none.none_or` -> `none.NoneOr` (0.19, 6 deps)
- `typing.callable` -> `typing.Callable` (0.19)
- `util.arc_or_static` -> `util.ArcOrStatic` (0.20, 2 deps)
- `runtime.slots` -> `runtime.Slots` (0.20)
- `analysis.find_call_name` -> `analysis.FindCallName` (0.20)
- `bool.value` -> `bool.Value` (0.20)
- `analysis.performance` -> `analysis.Performance` (0.20)
- `analysis.dubious` -> `analysis.Dubious` (0.20)
- `assert.conformance` -> `assert.Conformance` (0.21)
- `typing.structs` -> `typing.Structs` (0.21, 1 deps)
- `environment.slots` -> `environment.Slots` (0.21)
- `tuple.refs` -> `tuple.Refs` (0.21)
- `type_compiled.type_matcher_factory` -> `type_compiled.TypeMatcherFactory` (0.21, 7 deps)
- `float.unpack` -> `float.Unpack` (0.22)
- `compiler.span` -> `compiler.Span` (0.23, 29 deps)
- `compiler.def_inline` -> `compiler.DefInline` (0.23)
- `layout.vtable` -> `layout.Vtable` (0.23)
- `compiler.compr` -> `compiler.Compr` (0.23)
- `bc.bytecode` -> `bc.Bytecode` (0.24)
- `eval` -> `eval.Eval` (0.24)
- `runtime.frame_span` -> `runtime.FrameSpan` (0.24, 26 deps)
- `compiler.known` -> `compiler.Known` (0.24)
- `num.value` -> `num.Value` (0.24)
- `profile.profiler_type` -> `profile.ProfilerType` (0.25, 6 deps)
- `typing.ctx` -> `typing.Ctx` (0.25)
- `profile.time_flame` -> `profile.TimeFlame` (0.25)
- `analysis.names` -> `analysis.Names` (0.25)
- `profile.bc` -> `profile.Bc` (0.25)
- `namespace.typing` -> `namespace.Typing` (0.25)
- `params.display` -> `params.Display` (0.26, 76 deps)
- `compiler.call` -> `compiler.Call` (0.26)
- `enumeration.matcher` -> `enumeration.Matcher` (0.26)
- `compiler.module` -> `compiler.Module` (0.26)
- `values.starlark_type_id` -> `values.StarlarkTypeId` (0.27, 7 deps)
- `layout.value_not_special` -> `layout.ValueNotSpecial` (0.28)
- `values.error` -> `values.Error` (0.28, 17 deps)
- `util.refcell` -> `refcell.RefCell` (0.28, 20 deps)
- `types.known_methods` -> `types.KnownMethods` (0.28)
- `values.comparison` -> `values.Comparison` (0.29)
- `analysis.incompatible` -> `analysis.Incompatible` (0.29)
- `analysis.flow` -> `analysis.Flow` (0.29)
- `compiler.expr_bool` -> `compiler.ExprBool` (0.29)
- `oracle.ctx` -> `oracle.Ctx` (0.30)
- `tuple.alloc` -> `tuple.Alloc` (0.30)
- `typing.macro_refs` -> `typing.MacroRefs` (0.30)
- `stdlib.breakpoint` -> `stdlib.Breakpoint` (0.30, 1 deps)
- `runtime.inlined_frame` -> `runtime.InlinedFrame` (0.31)
- `profile.flamegraph` -> `profile.Flamegraph` (0.31)
- `none.globals` -> `none.Globals` (0.32)
- `unused_loads.find` -> `unusedloads.Find` (0.32)
- `string.dot_format` -> `string.DotFormat` (0.32, 1 deps)
- `callable.param` -> `callable.Param` (0.33)
- `layout.value` -> `layout.Value` (0.33, 178 deps)
- `analysis.lint_message` -> `analysis.LintMessage` (0.34, 1 deps)
- `docs.multipage` -> `docs.Multipage` (0.34)
- `typing.arc_ty` -> `typing.ArcTy` (0.34, 5 deps)
- `record.matcher` -> `record.Matcher` (0.35)
- `layout.identity` -> `layout.Identity` (0.36)
- `eval.params` -> `eval.Params` (0.36)
- `eval.soft_error` -> `eval.SoftError` (0.37)
- `stdlib.internal` -> `stdlib.Internal` (0.37)
- `profile.heap` -> `profile.Heap` (0.38)
- `type_compiled.alloc` -> `type_compiled.Alloc` (0.38)
- `types.ellipsis` -> `types.Ellipsis` (0.38, 4 deps)
- `runtime.before_stmt` -> `runtime.BeforeStmt` (0.38)
- `bc.instr_impl` -> `bc.InstrImpl` (0.38)
- `typing.ty` -> `typing.Ty` (0.39, 109 deps)
- `typing.basic` -> `typing.Basic` (0.40)
- `types.any` -> `types.Any` (0.40)
- `typing.macro_support` -> `typing.MacroSupport` (0.41)
- `bool.alloc` -> `bool.Alloc` (0.41)
- `namespace.value` -> `namespace.Value` (0.41)
- `compiler.type_expr` -> `compiler.TypeExpr` (0.41)
- `compiler.error` -> `compiler.Error` (0.42)
- `alloc.chunk_part` -> `alloc.ChunkPart` (0.43, 2 deps)
- `funcs.zip` -> `funcs.Zip` (0.43)
- `typing.globals` -> `typing.Globals` (0.50)
- `eval.bc.compiler.compr` -> `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr` (0.51)
- `compiler.assign_modify` -> `compiler.AssignModify` (0.53)
- `num.typecheck` -> `num.Typecheck` (0.54)
- `type_compiled.factory` -> `type_compiled.Factory` (0.58)

## Incorrect Ports (Missing Types)

These files are matched (often via `// port-lint`) but appear to be missing one or more type declarations
present in the Rust source file.

| Source | Target | Missing types | Examples |
|--------|--------|---------------|----------|
| `layout.value` | `layout.Value` | 2/9 | `Canonical`, `String` |
| `values.freeze` | `values.Freeze [ZERO]` | 1/2 | `Frozen` |
| `compiler.span` | `compiler.Span` | 1/2 | `Target` |
| `values.frozen_ref` | `values.FrozenRef [ZERO]` | 2/4 | `Target`, `Frozen` |
| `none.none_type` | `none.NoneType [ZERO]` | 1/2 | `Error` |
| `values.value_of_unchecked` | `values.ValueOfUnchecked [ZERO]` | 3/7 | `Canonical`, `Frozen`, `Error` |
| `values.iter` | `values.Iter [ZERO]` | 1/2 | `Item` |
| `int.inline_int` | `int.InlineInt [ZERO]` | 3/5 | `Error`, `Output`, `Canonical` |
| `int.pointer_i32` | `int.PointerI32 [ZERO]` | 1/2 | `Canonical` |
| `layout.aligned_size` | `layout.AlignedSize [ZERO]` | 1/2 | `Output` |
| `any` | `starlark.Any` | 1/15 | `StaticType` |
| `runtime.small_duration` | `runtime.SmallDuration [ZERO]` | 1/2 | `Output` |
| `values.freeze_error` | `values.FreezeError [ZERO]` | 1/4 | `FreezeResult` |
| `dict.dict_type` | `dict.DictType [ZERO]` | 2/3 | `Canonical`, `Error` |
| `none.none_or` | `none.NoneOr` | 2/3 | `Canonical`, `Error` |
| `typing.arc_ty` | `typing.ArcTy` | 1/4 | `Target` |
| `enumeration.enum_type` | `enumeration.EnumType [ZERO]` | 4/8 | `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical` |
| `types.starlark_value_as_type` | `types.StarlarkValueAsType [ZERO]` | 1/4 | `Canonical` |
| `values.value_of` | `values.ValueOf [ZERO]` | 3/4 | `Target`, `Canonical`, `Error` |
| `profile.alloc_counts` | `profile.AllocCounts` | 1/2 | `Output` |
| `values.demand` | `values.Demand` | 1/4 | `StaticType` |
| `record.record_type` | `record.RecordType [ZERO]` | 6/8 | `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical` |
| `alloc.chunk` | `alloc.Chunk [ZERO]` | 1/3 | `ChunkDataEmpty` |
| `list.alloc` | `list.Alloc [ZERO]` | 1/2 | `Canonical` |
| `list.list_type` | `list.ListType` | 2/3 | `Canonical`, `Error` |
| `profile.instant` | `profile.Instant [ZERO]` | 1/2 | `Output` |
| `values.unpack_and_discard` | `values.UnpackAndDiscard [ZERO]` | 2/3 | `Canonical`, `Error` |
| `compiler.small_vec_1` | `compiler.SmallVec1 [ZERO]` | 3/4 | `Target`, `Item`, `IntoIter` |
| `util.arc_or_static` | `util.ArcOrStatic` | 1/3 | `Target` |
| `values.owned_frozen_ref` | `values.OwnedFrozenRef [ZERO]` | 1/3 | `Target` |
| `typing.type_type` | `typing.TypeType` | 2/3 | `Canonical`, `Error` |
| `avalues.str_` | `avalues.Str [ZERO]` | 2/3 | `StarlarkValue`, `ExtraElem` |
| `heap.arena` | `heap.Arena [ZERO]` | 3/7 | `ChunkIter`, `Item`, `ArenaUninit` |
| `collections.alloca` | `collections.Alloca [ZERO]` | 3/4 | `Buffer`, `Align`, `DropSliceGuard` |
| `stdlib` | `starlark.Stdlib [ZERO]` | 1/3 | `Error` |
| `types.list_or_tuple` | `types.ListOrTuple [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `stdlib.breakpoint` | `stdlib.Breakpoint` | 1/6 | `Handler` |
| `types.any_complex` | `types.AnyComplex [ZERO]` | 2/5 | `Canonical`, `Frozen` |
| `types.any_array` | `types.AnyArray [ZERO]` | 1/3 | `Canonical` |
| `util.rtabort` | `util.Rtabort [ZERO]` | 1/1 | `AbortOnDrop` |
| `read_line` | `starlark.ReadLine [ZERO]` | 1/2 | `NoRustyline` |
| `util.non_static_type_id` | `util.NonStaticTypeId [ZERO]` | 1/1 | `NonStaticAny` |
| `avalues.simple` | `avalues.Simple [ZERO]` | 2/3 | `StarlarkValue`, `ExtraElem` |
| `layout.value_captured` | `layout.ValueCaptured` | 2/4 | `Canonical`, `Frozen` |
| `record.field` | `record.Field [ZERO]` | 1/1 | `FieldGen` |
| `bc.instr_impl` | `bc.InstrImpl` | 76/163 | `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc` |
| `string.str_type` | `string.StrType [ZERO]` | 4/4 | `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target` |
| `int.int_or_big` | `int.IntOrBig [STUB]` | 4/7 | `Canonical`, `Err`, `Error`, `Output` |
| `thin_box_slice_frozen_value.thin_box` | `thinboxslicefrozenvalue.ThinBox [ZERO]` | 2/3 | `ThinBoxSliceLayout`, `Target` |
| `layout.typed` | `layout.ValueTyped [ZERO]` | 5/7 | `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError` |
| `values.typing.callable` | `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]` | 3/8 | `Canonical`, `Error`, `Frozen` |
| `stdlib.json` | `stdlib.Json` | 1/1 | `Canonical` |
| `scope.payload` | `scope.Payload [ZERO]` | 8/17 | `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt` |
| `stdlib.extra` | `stdlib.Extra` | 1/4 | `PrintHandlerImpl` |
| `tuple.rust_tuple` | `tuple.RustTuple` | 2/2 | `Canonical`, `Error` |
| `profile.csv` | `profile.Csv` | 2/3 | `Impl`, `CsvValue` |
| `layout.vtable` | `layout.Vtable` | 2/6 | `GetTypeId`, `GetAllocativeKey` |
| `dict.value` | `dict.Value [ZERO]` | 3/10 | `Canonical`, `Frozen`, `ContentRef` |
| `bigint.convert` | `bigint.Convert [ZERO]` | 2/2 | `Canonical`, `Error` |
| `thin_box_slice_frozen_value.packed_impl` | `thinboxslicefrozenvalue.PackedImpl [ZERO]` | 1/3 | `Target` |
| `string.alloc_unpack` | `string.AllocUnpack [ZERO]` | 2/2 | `Canonical`, `Error` |
| `pagable.vtable_registry` | `pagable.VtableRegistry [ZERO]` | 2/4 | `TestSimpleType`, `TestComplexGen` |
| `type_compiled.compiled` | `type_compiled.Compiled [ZERO]` | 2/7 | `StaticType`, `Canonical` |
| `dict.traits` | `dict.Traits` | 2/2 | `Canonical`, `Error` |
| `typing.small_arc_vec_or_static` | `typing.SmallArcVecOrStatic` | 3/5 | `Target`, `Item`, `IntoIter` |
| `bc.instrs` | `bc.Instrs [ZERO]` | 1/4 | `HandlerImpl` |
| `values.unpack` | `values.Unpack [ZERO]` | 4/7 | `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error` |
| `analysis.types` | `analysis.Types` | 3/5 | `LintWarning`, `LintT`, `EvalSeverity` |
| `tuple.unpack` | `tuple.Unpack [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `list.value` | `list.Value [ZERO]` | 2/8 | `List`, `Canonical` |
| `set.value` | `set.Value [ZERO]` | 3/9 | `Canonical`, `Frozen`, `ContentRef` |
| `layout.complex` | `layout.Complex [ZERO]` | 3/4 | `Canonical`, `Error`, `Frozen` |
| `typing.small_arc_vec` | `typing.SmallArcVec` | 1/3 | `Target` |
| `compiler.scope` | `compiler.Scope [ZERO]` | 3/20 | `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue` |
| `float.float` | `float.Float [STUB]` | 2/3 | `Canonical`, `Error` |
| `profile.bc` | `profile.Bc` | 1/10 | `Data` |
| `num.value` | `num.Value` | 1/4 | `Output` |
| `dict.refs` | `dict.Refs` | 3/7 | `Target`, `Canonical`, `Error` |
| `heap.send` | `heap.Send [ZERO]` | 3/6 | `Sealed`, `Target`, `StaticType` |
| `list.unpack` | `list.Unpack [ZERO]` | 4/5 | `Canonical`, `Error`, `Item`, `IntoIter` |
| `analysis.names` | `analysis.Names` | 1/8 | `AstStrExt` |
| `values.owned` | `values.Owned [ZERO]` | 2/5 | `Canonical`, `Target` |
| `bc.addr` | `bc.Addr [ZERO]` | 1/6 | `Output` |
| `stdlib.partial` | `stdlib.Partial [STUB]` | 2/5 | `Frozen`, `Canonical` |
| `typing.callable_param` | `typing.CallableParam` | 1/6 | `ParamSpecDisplay` |
| `alloc.allocator` | `alloc.Allocator [ZERO]` | 1/3 | `Item` |
| `allocator.bumpalo` | `allocator.Bumpalo [ZERO]` | 3/3 | `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator` |
| `profile.time_flame` | `profile.TimeFlame` | 1/11 | `Data` |
| `alloc.chain` | `alloc.Chain [ZERO]` | 2/5 | `Item`, `ResetSplitAtZeroTest` |
| `avalues.list` | `avalues.List` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `tuple.refs` | `tuple.Refs` | 2/4 | `Canonical`, `Error` |
| `analysis.dubious` | `analysis.Dubious` | 1/2 | `Key` |
| `int.i32` | `int.I32` | 2/2 | `Canonical`, `Error` |
| `profile.mode` | `profile.Mode` | 1/2 | `Err` |
| `structs.alloc` | `structs.Alloc [ZERO]` | 1/2 | `Canonical` |
| `dict.alloc` | `dict.Alloc [ZERO]` | 1/2 | `Canonical` |
| `bc.instr_arg` | `bc.InstrArg [ZERO]` | 1/4 | `HandlerImpl` |
| `compiler.def` | `compiler.Def [ZERO]` | 1/13 | `Frozen` |
| `bc.stack_ptr` | `bc.StackPtr [ZERO]` | 1/8 | `Output` |
| `list.refs` | `list.Refs [ZERO]` | 3/5 | `Target`, `Canonical`, `Error` |
| `type_compiled.matcher` | `type_compiled.Matcher [ZERO]` | 3/7 | `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result` |
| `bc.opcode` | `bc.Opcode [ZERO]` | 2/5 | `ByNumber`, `FindOpcode` |
| `bc.repr` | `bc.Repr [ZERO]` | 1/3 | `HandlerImpl` |
| `dict.unpack` | `dict.Unpack` | 2/3 | `Canonical`, `Error` |
| `compiler.expr` | `compiler.Expr [ZERO]` | 2/11 | `AstLiteralCompile`, `CompilerExprUtil` |
| `values.traits` | `values.Traits [ZERO]` | 1/3 | `Canonical` |
| `profile.heap` | `profile.Heap` | 1/11 | `Data` |
| `types.function` | `types.Function [ZERO]` | 1/12 | `Canonical` |
| `profile.stmt` | `profile.Stmt [ZERO]` | 1/9 | `Data` |
| `avalues.tuple` | `avalues.Tuple [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `avalues.array` | `avalues.Array [ZERO]` | 2/4 | `StarlarkValue`, `ExtraElem` |
| `avalues.complex` | `avalues.Complex [ZERO]` | 2/5 | `StarlarkValue`, `ExtraElem` |
| `avalues.static_` | `avalues.Static [ZERO]` | 2/5 | `StarlarkValue`, `ExtraElem` |
| `profile.summary_by_function` | `profile.SummaryByFunction [ZERO]` | 1/3 | `RowKind` |
| `profile.typecheck` | `profile.Typecheck [ZERO]` | 1/5 | `Data` |
| `compiler.args` | `compiler.Args` | 1/2 | `Never` |
| `values.type_repr` | `values.TypeRepr [ZERO]` | 1/3 | `Canonical` |
| `bc.call` | `bc.Call [ZERO]` | 1/5 | `Args` |
| `structs.refs` | `structs.Refs [ZERO]` | 2/4 | `Canonical`, `Error` |
| `typing.iter` | `typing.Iter [ZERO]` | 2/4 | `NonInstantiable`, `Canonical` |
| `set.refs` | `set.Refs [ZERO]` | 2/5 | `Canonical`, `Error` |
| `typing.callable` | `typing.Callable` | 1/2 | `TyCallableInner` |
| `bc.bytecode` | `bc.Bytecode` | 1/2 | `HandlerImpl` |
| `heap.call_enter_exit` | `heap.CallEnterExit [ZERO]` | 1/6 | `Canonical` |
| `types.any` | `types.Any` | 1/2 | `Canonical` |
| `list.globals` | `list.Globals [ZERO]` | 1/1 | `ListType` |
| `float.unpack` | `float.Unpack` | 2/3 | `Canonical`, `Error` |
| `collections.maybe_uninit_backport` | `collections.MaybeUninitBackport [ZERO]` | 1/1 | `Guard` |
| `heap.maybe_uninit_slice_util` | `heap.MaybeUninitSliceUtil` | 1/1 | `WriteRemOnDrop` |
| `bool.type_repr` | `bool.TypeRepr [ZERO]` | 1/1 | `Canonical` |
| `bool.unpack` | `bool.Unpack` | 1/1 | `Error` |
| `type_compiled.factory` | `type_compiled.Factory` | 1/2 | `Result` |
| `typing.never` | `typing.Never [ZERO]` | 1/3 | `Canonical` |
| `tuple.alloc` | `tuple.Alloc` | 1/2 | `Canonical` |
| `compiler.assign_modify` | `compiler.AssignModify` | 1/1 | `AssignOnWriteBc` |

## High Priority Missing Files

| Rank | Source file | Expected target | Deps | Functions | Classes/types | Symbols | Source path | Expected path |
|------|-------------|-----------------|------|-----------|---------------|---------|-------------|---------------|
| 1 | `tests.uncategorized` | `tests.Uncategorized` | 0 | 55 | 6 | 61 | `tests/uncategorized.rs` | `tests/Uncategorized.kt` |
| 2 | `tests.markdown` | `docs.tests.Markdown` | 0 | 29 | 2 | 31 | `docs/tests/markdown.rs` | `docs/tests/Markdown.kt` |
| 3 | `opt.if_rand` | `tests.opt.IfRand` | 0 | 27 | 3 | 30 | `tests/opt/if_rand.rs` | `tests/opt/IfRand.kt` |
| 4 | `typing.tests` | `typing.tests.Tests` | 0 | 28 | 2 | 30 | `typing/tests.rs` | `typing/tests/Tests.kt` |
| 5 | `adapter.tests` | `debug.adapter.Tests` | 0 | 23 | 3 | 26 | `debug/adapter/tests.rs` | `debug/adapter/Tests.kt` |
| 6 | `tests.call` | `tests.Call` | 0 | 20 | 0 | 20 | `tests/call.rs` | `tests/Call.kt` |
| 7 | `tests.fstring` | `tests.Fstring` | 0 | 18 | 0 | 18 | `tests/fstring.rs` | `tests/Fstring.kt` |
| 8 | `tests.rustdocs` | `docs.tests.Rustdocs` | 0 | 15 | 3 | 18 | `docs/tests/rustdocs.rs` | `docs/tests/Rustdocs.kt` |
| 9 | `scope.tests` | `eval.compiler.scope.Tests` | 0 | 16 | 1 | 17 | `eval/compiler/scope/tests.rs` | `eval/compiler/scope/Tests.kt` |
| 10 | `tests.runtime` | `tests.Runtime` | 0 | 14 | 1 | 15 | `tests/runtime.rs` | `tests/Runtime.kt` |
| 11 | `coerce` | `Coerce` | 34 | 5 | 9 | 14 | `coerce.rs` | `Coerce.kt` |
| 12 | `tests.def` | `tests.Def` | 5 | 14 | 0 | 14 | `tests/def.rs` | `tests/Def.kt` |
| 13 | `module.unpack_value` | `tests.derive.module.UnpackValue` | 0 | 13 | 0 | 13 | `tests/derive/module/unpack_value.rs` | `tests/derive/module/UnpackValue.kt` |
| 14 | `profile.tests` | `eval.runtime.profile.Tests` | 0 | 13 | 0 | 13 | `eval/runtime/profile/tests.rs` | `eval/runtime/profile/Tests.kt` |
| 15 | `type_compiled.tests` | `values.typing.typecompiled.Tests` | 0 | 13 | 0 | 13 | `values/typing/type_compiled/tests.rs` | `values/typing/typecompiled/Tests.kt` |
| 16 | `bc.if_stmt` | `tests.bc.IfStmt` | 0 | 12 | 0 | 12 | `tests/bc/if_stmt.rs` | `tests/bc/IfStmt.kt` |
| 17 | `tests.basic` | `tests.Basic` | 0 | 12 | 0 | 12 | `tests/basic.rs` | `tests/Basic.kt` |
| 18 | `module.named_positional` | `tests.derive.module.NamedPositional` | 0 | 11 | 0 | 11 | `tests/derive/module/named_positional.rs` | `tests/derive/module/NamedPositional.kt` |
| 19 | `module.generic` | `tests.derive.module.Generic` | 0 | 8 | 2 | 10 | `tests/derive/module/generic.rs` | `tests/derive/module/Generic.kt` |
| 20 | `tests.comprehension` | `tests.Comprehension` | 0 | 10 | 0 | 10 | `tests/comprehension.rs` | `tests/Comprehension.kt` |
| 21 | `derive.docs` | `tests.derive.Docs` | 0 | 7 | 2 | 9 | `tests/derive/docs.rs` | `tests/derive/Docs.kt` |
| 22 | `module.basic` | `tests.derive.module.Basic` | 0 | 9 | 0 | 9 | `tests/derive/module/basic.rs` | `tests/derive/module/Basic.kt` |
| 23 | `tests.type_annot` | `tests.TypeAnnot` | 0 | 9 | 0 | 9 | `tests/type_annot.rs` | `tests/TypeAnnot.kt` |
| 24 | `typing.tests.call` | `typing.tests.Call` | 0 | 9 | 0 | 9 | `typing/tests/call.rs` | `typing/tests/Call.kt` |
| 25 | `__derive_refs.param_spec` | `deriverefs.ParamSpec` | 20 | 5 | 3 | 8 | `__derive_refs/param_spec.rs` | `deriverefs/ParamSpec.kt` |
| 26 | `__derive_refs.parse_args` | `deriverefs.ParseArgs` | 0 | 8 | 0 | 8 | `__derive_refs/parse_args.rs` | `deriverefs/ParseArgs.kt` |
| 27 | `bc.and_or` | `tests.bc.AndOr` | 0 | 8 | 0 | 8 | `tests/bc/and_or.rs` | `tests/bc/AndOr.kt` |
| 28 | `opt.def_inline` | `tests.opt.DefInline` | 0 | 8 | 0 | 8 | `tests/opt/def_inline.rs` | `tests/opt/DefInline.kt` |
| 29 | `tests.opt` | `tests.opt.Opt` | 0 | 8 | 0 | 8 | `tests/opt.rs` | `tests/opt/Opt.kt` |
| 30 | `derive.unpack_value` | `tests.derive.UnpackValue` | 51 | 2 | 5 | 7 | `tests/derive/unpack_value.rs` | `tests/derive/UnpackValue.kt` |
| 31 | `util.arc_str` | `util.ArcStr` | 21 | 5 | 2 | 7 | `util/arc_str.rs` | `util/ArcStr.kt` |
| 32 | `bc.expr` | `tests.bc.Expr` | 7 | 7 | 0 | 7 | `tests/bc/expr.rs` | `tests/bc/Expr.kt` |
| 33 | `opt.eq` | `tests.opt.Eq` | 0 | 7 | 0 | 7 | `tests/opt/eq.rs` | `tests/opt/Eq.kt` |
| 34 | `unused_loads.find_tests` | `analysis.unusedloads.FindTests` | 0 | 7 | 0 | 7 | `analysis/unused_loads/find_tests.rs` | `analysis/unusedloads/FindTests.kt` |
| 35 | `freeze.bounds` | `tests.derive.freeze.Bounds` | 0 | 3 | 3 | 6 | `tests/derive/freeze/bounds.rs` | `tests/derive/freeze/Bounds.kt` |
| 36 | `freeze.identity` | `tests.derive.freeze.Identity` | 0 | 2 | 4 | 6 | `tests/derive/freeze/identity.rs` | `tests/derive/freeze/Identity.kt` |
| 37 | `freeze.validator_order` | `tests.derive.freeze.ValidatorOrder` | 0 | 3 | 3 | 6 | `tests/derive/freeze/validator_order.rs` | `tests/derive/freeze/ValidatorOrder.kt` |
| 38 | `int.tests` | `values.types.int.Tests` | 0 | 6 | 0 | 6 | `values/types/int/tests.rs` | `values/types/int/Tests.kt` |
| 39 | `module.methods` | `tests.derive.module.Methods` | 0 | 5 | 1 | 6 | `tests/derive/module/methods.rs` | `tests/derive/module/Methods.kt` |
| 40 | `module.other_attributes` | `tests.derive.module.OtherAttributes` | 0 | 6 | 0 | 6 | `tests/derive/module/other_attributes.rs` | `tests/derive/module/OtherAttributes.kt` |
| 41 | `tests.list` | `typing.tests.List` | 0 | 6 | 0 | 6 | `typing/tests/list.rs` | `typing/tests/List.kt` |
| 42 | `tests.bc.definitely_assigned` | `tests.bc.DefinitelyAssigned` | 0 | 5 | 0 | 5 | `tests/bc/definitely_assigned.rs` | `tests/bc/DefinitelyAssigned.kt` |
| 43 | `__derive_refs.components` | `deriverefs.Components` | 0 | 3 | 1 | 4 | `__derive_refs/components.rs` | `deriverefs/Components.kt` |
| 44 | `__derive_refs.sig` | `deriverefs.Sig` | 0 | 3 | 1 | 4 | `__derive_refs/sig.rs` | `deriverefs/Sig.kt` |
| 45 | `bc.compr` | `tests.bc.Compr` | 0 | 4 | 0 | 4 | `tests/bc/compr.rs` | `tests/bc/Compr.kt` |
| 46 | `derive.alloc_value` | `tests.derive.AllocValue` | 0 | 0 | 4 | 4 | `tests/derive/alloc_value.rs` | `tests/derive/AllocValue.kt` |
| 47 | `freeze.validator` | `tests.derive.freeze.Validator` | 0 | 3 | 1 | 4 | `tests/derive/freeze/validator.rs` | `tests/derive/freeze/Validator.kt` |
| 48 | `module.kwargs` | `tests.derive.module.Kwargs` | 0 | 4 | 0 | 4 | `tests/derive/module/kwargs.rs` | `tests/derive/module/Kwargs.kt` |
| 49 | `module.return_impl` | `tests.derive.module.ReturnImpl` | 0 | 4 | 0 | 4 | `tests/derive/module/return_impl.rs` | `tests/derive/module/ReturnImpl.kt` |
| 50 | `module.type_annotation` | `tests.derive.module.TypeAnnotation` | 0 | 3 | 1 | 4 | `tests/derive/module/type_annotation.rs` | `tests/derive/module/TypeAnnotation.kt` |
| 51 | `opt.type_is` | `tests.opt.TypeIs` | 0 | 4 | 0 | 4 | `tests/opt/type_is.rs` | `tests/opt/TypeIs.kt` |
| 52 | `tests.freeze_access_value` | `tests.FreezeAccessValue` | 0 | 2 | 2 | 4 | `tests/freeze_access_value.rs` | `tests/FreezeAccessValue.kt` |
| 53 | `tests.util` | `tests.Util` | 0 | 3 | 1 | 4 | `tests/util.rs` | `tests/Util.kt` |
| 54 | `trace.bounds` | `tests.derive.trace.Bounds` | 0 | 2 | 2 | 4 | `tests/derive/trace/bounds.rs` | `tests/derive/trace/Bounds.kt` |
| 55 | `unused_loads.remove_tests` | `analysis.unusedloads.RemoveTests` | 0 | 4 | 0 | 4 | `analysis/unused_loads/remove_tests.rs` | `analysis/unusedloads/RemoveTests.kt` |
| 56 | `__derive_refs.invoke_macro_error` | `deriverefs.InvokeMacroError` | 0 | 2 | 1 | 3 | `__derive_refs/invoke_macro_error.rs` | `deriverefs/InvokeMacroError.kt` |
| 57 | `bc.for_stmt` | `tests.bc.ForStmt` | 0 | 3 | 0 | 3 | `tests/bc/for_stmt.rs` | `tests/bc/ForStmt.kt` |
| 58 | `derive.attrs` | `tests.derive.Attrs` | 0 | 1 | 2 | 3 | `tests/derive/attrs.rs` | `tests/derive/Attrs.kt` |
| 59 | `module.default_value` | `tests.derive.module.DefaultValue` | 0 | 3 | 0 | 3 | `tests/derive/module/default_value.rs` | `tests/derive/module/DefaultValue.kt` |
| 60 | `module.special_params` | `tests.derive.module.SpecialParams` | 0 | 3 | 0 | 3 | `tests/derive/module/special_params.rs` | `tests/derive/module/SpecialParams.kt` |
| 61 | `opt.speculative_exec` | `tests.opt.SpeculativeExec` | 0 | 3 | 0 | 3 | `tests/opt/speculative_exec.rs` | `tests/opt/SpeculativeExec.kt` |
| 62 | `tests.go` | `tests.Go` | 0 | 3 | 0 | 3 | `tests/go.rs` | `tests/Go.kt` |
| 63 | `tests.types` | `typing.tests.Types` | 0 | 3 | 0 | 3 | `typing/tests/types.rs` | `typing/tests/Types.kt` |
| 64 | `bc.golden` | `tests.bc.Golden` | 0 | 2 | 0 | 2 | `tests/bc/golden.rs` | `tests/bc/Golden.kt` |
| 65 | `derive.unpack_value_attr` | `tests.derive.UnpackValueAttr` | 0 | 0 | 2 | 2 | `tests/derive/unpack_value_attr.rs` | `tests/derive/UnpackValueAttr.kt` |
| 66 | `opt.constant_folding` | `tests.opt.ConstantFolding` | 0 | 2 | 0 | 2 | `tests/opt/constant_folding.rs` | `tests/opt/ConstantFolding.kt` |
| 67 | `opt.list_add` | `tests.opt.ListAdd` | 0 | 2 | 0 | 2 | `tests/opt/list_add.rs` | `tests/opt/ListAdd.kt` |
| 68 | `opt.types` | `tests.opt.Types` | 0 | 2 | 0 | 2 | `tests/opt/types.rs` | `tests/opt/Types.kt` |
| 69 | `tests.callable` | `typing.tests.Callable` | 0 | 2 | 0 | 2 | `typing/tests/callable.rs` | `typing/tests/Callable.kt` |
| 70 | `tests.special_function` | `typing.tests.SpecialFunction` | 0 | 2 | 0 | 2 | `typing/tests/special_function.rs` | `typing/tests/SpecialFunction.kt` |
| 71 | `tests.tuple` | `typing.tests.Tuple` | 0 | 2 | 0 | 2 | `typing/tests/tuple.rs` | `typing/tests/Tuple.kt` |
| 72 | `trace.statics` | `tests.derive.trace.Statics` | 0 | 0 | 2 | 2 | `tests/derive/trace/statics.rs` | `tests/derive/trace/Statics.kt` |
| 73 | `tests.before_stmt` | `tests.BeforeStmt` | 1 | 1 | 0 | 1 | `tests/before_stmt.rs` | `tests/BeforeStmt.kt` |
| 74 | `bc.isinstance` | `tests.bc.Isinstance` | 0 | 1 | 0 | 1 | `tests/bc/isinstance.rs` | `tests/bc/Isinstance.kt` |
| 75 | `freeze.basic` | `tests.derive.freeze.Basic` | 0 | 0 | 1 | 1 | `tests/derive/freeze/basic.rs` | `tests/derive/freeze/Basic.kt` |
| 76 | `freeze.enums` | `tests.derive.freeze.Enums` | 0 | 0 | 1 | 1 | `tests/derive/freeze/enums.rs` | `tests/derive/freeze/Enums.kt` |
| 77 | `tests.bc.call` | `tests.bc.Call` | 0 | 1 | 0 | 1 | `tests/bc/call.rs` | `tests/bc/Call.kt` |
| 78 | `tests.for_loop` | `tests.ForLoop` | 0 | 1 | 0 | 1 | `tests/for_loop.rs` | `tests/ForLoop.kt` |
| 79 | `tests.replace_binary` | `tests.ReplaceBinary` | 0 | 1 | 0 | 1 | `tests/replace_binary.rs` | `tests/ReplaceBinary.kt` |
| 80 | `trace.enums` | `tests.derive.trace.Enums` | 0 | 0 | 1 | 1 | `tests/derive/trace/enums.rs` | `tests/derive/trace/Enums.kt` |
| 81 | `layout.heap` | `values.layout.heap.Heap` | 109 | 0 | 0 | 0 | `values/layout/heap.rs` | `values/layout/heap/Heap.kt` |
| 82 | `assert` | `assert.Assert` | 84 | 0 | 0 | 0 | `assert.rs` | `assert/Assert.kt` |
| 83 | `debug` | `debug.Debug` | 53 | 0 | 0 | 0 | `debug.rs` | `debug/Debug.kt` |
| 84 | `derive.module` | `tests.derive.module.Module` | 20 | 0 | 0 | 0 | `tests/derive/module.rs` | `tests/derive/module/Module.kt` |
| 85 | `types.dict` | `values.types.dict.Dict` | 12 | 0 | 0 | 0 | `values/types/dict.rs` | `values/types/dict/Dict.kt` |
| 86 | `types.range` | `values.types.range.Range` | 5 | 0 | 0 | 0 | `values/types/range.rs` | `values/types/range/Range.kt` |
| 87 | `types.namespace` | `values.types.namespace.Namespace` | 3 | 0 | 0 | 0 | `values/types/namespace.rs` | `values/types/namespace/Namespace.kt` |
| 88 | `types.float` | `values.types.float.Float` | 2 | 0 | 0 | 0 | `values/types/float.rs` | `values/types/float/Float.kt` |
| 89 | `types.list` | `values.types.list.List` | 2 | 0 | 0 | 0 | `values/types/list.rs` | `values/types/list/List.kt` |
| 90 | `types.num` | `values.types.num.Num` | 2 | 0 | 0 | 0 | `values/types/num.rs` | `values/types/num/Num.kt` |
| 91 | `tests` | `tests.Tests` | 1 | 0 | 0 | 0 | `tests.rs` | `tests/Tests.kt` |
| 92 | `types.enumeration` | `values.types.enumeration.Enumeration` | 1 | 0 | 0 | 0 | `values/types/enumeration.rs` | `values/types/enumeration/Enumeration.kt` |
| 93 | `types.none` | `values.types.none.None` | 1 | 0 | 0 | 0 | `values/types/none.rs` | `values/types/none/None.kt` |
| 94 | `types.set` | `values.types.set.Set` | 1 | 0 | 0 | 0 | `values/types/set.rs` | `values/types/set/Set.kt` |
| 95 | `types.string` | `values.types.string.String` | 1 | 0 | 0 | 0 | `values/types/string.rs` | `values/types/string/String.kt` |
| 96 | `__derive_refs` | `deriverefs.DeriveRefs` | 0 | 0 | 0 | 0 | `__derive_refs.rs` | `deriverefs/DeriveRefs.kt` |
| 97 | `allocator.alloc` | `values.layout.heap.allocator.alloc.Alloc` | 0 | 0 | 0 | 0 | `values/layout/heap/allocator/alloc.rs` | `values/layout/heap/allocator/alloc/Alloc.kt` |
| 98 | `analysis.unused_loads` | `analysis.unusedloads.UnusedLoads` | 0 | 0 | 0 | 0 | `analysis/unused_loads.rs` | `analysis/unusedloads/UnusedLoads.kt` |
| 99 | `bc.compiler` | `eval.bc.compiler.Compiler` | 0 | 0 | 0 | 0 | `eval/bc/compiler.rs` | `eval/bc/compiler/Compiler.kt` |
| 100 | `collections` | `collections.Collections` | 0 | 0 | 0 | 0 | `collections.rs` | `collections/Collections.kt` |
| 101 | `derive.freeze` | `tests.derive.freeze.Freeze` | 0 | 0 | 0 | 0 | `tests/derive/freeze.rs` | `tests/derive/freeze/Freeze.kt` |
| 102 | `derive.trace` | `tests.derive.trace.Trace` | 0 | 0 | 0 | 0 | `tests/derive/trace.rs` | `tests/derive/trace/Trace.kt` |
| 103 | `docs.tests` | `docs.tests.Tests` | 0 | 0 | 0 | 0 | `docs/tests.rs` | `docs/tests/Tests.kt` |
| 104 | `heap.allocator` | `values.layout.heap.allocator.Allocator` | 0 | 0 | 0 | 0 | `values/layout/heap/allocator.rs` | `values/layout/heap/allocator/Allocator.kt` |
| 105 | `heap.branding` | `values.layout.heap.Branding` | 0 | 0 | 0 | 0 | `values/layout/heap/branding.rs` | `values/layout/heap/Branding.kt` |
| 106 | `heap.profile` | `values.layout.heap.profile.Profile` | 0 | 0 | 0 | 0 | `values/layout/heap/profile.rs` | `values/layout/heap/profile/Profile.kt` |
| 107 | `lib` | `Lib` | 0 | 0 | 0 | 0 | `lib.rs` | `Lib.kt` |
| 108 | `runtime.params` | `eval.runtime.params.Params` | 0 | 0 | 0 | 0 | `eval/runtime/params.rs` | `eval/runtime/params/Params.kt` |
| 109 | `runtime.profile` | `eval.runtime.profile.Profile` | 0 | 0 | 0 | 0 | `eval/runtime/profile.rs` | `eval/runtime/profile/Profile.kt` |
| 110 | `string.intern` | `values.types.string.intern.Intern` | 0 | 0 | 0 | 0 | `values/types/string/intern.rs` | `values/types/string/intern/Intern.kt` |
| 111 | `tests.bc` | `tests.bc.Bc` | 0 | 0 | 0 | 0 | `tests/bc.rs` | `tests/bc/Bc.kt` |
| 112 | `tests.derive` | `tests.derive.Derive` | 0 | 0 | 0 | 0 | `tests/derive.rs` | `tests/derive/Derive.kt` |
| 113 | `types.structs` | `values.types.structs.Structs` | 0 | 0 | 0 | 0 | `values/types/structs.rs` | `values/types/structs/Structs.kt` |
| 114 | `types.tuple` | `values.types.tuple.Tuple` | 0 | 0 | 0 | 0 | `values/types/tuple.rs` | `values/types/tuple/Tuple.kt` |

## Documentation Gaps

Documentation correspondence is incomplete; it is reported separately from implementation scoring.

**Documentation line amount:** 9573 / 12638 lines (76%)

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
- `bc.writer` - 40% gap (94 → 56 lines)
- `heap.fast_cell` - 100% gap (38 → 0 lines)
- `debug.adapter` - 21% gap (182 → 144 lines)
- `dict.globals` - 58% gap (60 → 25 lines)
- `types.array` - 55% gap (62 → 28 lines)
- `int.globals` - 34% gap (96 → 63 lines)
- `typed.string` - 55% gap (60 → 27 lines)
- `allocator.api` - 100% gap (32 → 0 lines)
- `heap.arena` - 64% gap (50 → 18 lines)
- `scope.payload` - 89% gap (36 → 4 lines)
- `dict.value` - 53% gap (60 → 28 lines)
- `values.owned_frozen_ref` - 62% gap (52 → 20 lines)
- `values.type_repr` - 42% gap (74 → 43 lines)
- `pagable.vtable_register` - 52% gap (60 → 29 lines)
- `range.globals` - 45% gap (64 → 35 lines)
- `analysis.types` - 54% gap (52 → 24 lines)
- `values` - 100% gap (28 → 0 lines)
- `tuple.refs` - 100% gap (26 → 0 lines)
- `pagable` - 100% gap (26 → 0 lines)
- `environment.methods` - 25% gap (102 → 76 lines)
- `bool.globals` - 50% gap (50 → 25 lines)
- `record.globals` - 33% gap (76 → 51 lines)
- `types.function` - 48% gap (50 → 26 lines)
- `bc.definitely_assigned` - 43% gap (54 → 31 lines)
- `eval.bc.compiler.expr` - 77% gap (30 → 7 lines)
- `stdlib.extra` - 46% gap (50 → 27 lines)
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
- `values.freeze` - 30% gap (56 → 39 lines)
- `bc.for_loop` - 100% gap (16 → 0 lines)
- `types.record` - 32% gap (50 → 34 lines)
- `environment.names` - 36% gap (44 → 28 lines)
- `compiler.def_inline` - 62% gap (26 → 10 lines)
- `type_compiled.globals` - 44% gap (34 → 19 lines)
- `avalues.str_` - 68% gap (22 → 7 lines)
- `float.globals` - 26% gap (54 → 40 lines)
- `typing.user` - 29% gap (48 → 34 lines)
- `runtime.slots` - 35% gap (40 → 26 lines)
- `type_compiled.compiled` - 39% gap (36 → 22 lines)
- `typing.fill_types_for_lint` - 34% gap (38 → 25 lines)
- `profile.summary_by_function` - 43% gap (30 → 17 lines)
- `oracle.traits` - 34% gap (38 → 25 lines)
- `docs.multipage` - 26% gap (46 → 34 lines)
- `types.bool` - 100% gap (12 → 0 lines)
- `values.iter` - 100% gap (12 → 0 lines)
- `profile.time_flame` - 43% gap (28 → 16 lines)
- `docs.markdown` - 33% gap (36 → 24 lines)
- `typing.bindings` - 40% gap (30 → 18 lines)
- `params.display` - 50% gap (22 → 11 lines)
- `runtime.cheap_call_stack` - 42% gap (26 → 15 lines)
- `bc.instr_impl` - 34% gap (32 → 21 lines)
- `enumeration.globals` - 27% gap (40 → 29 lines)
- `values.frozen_ref` - 50% gap (20 → 10 lines)
- `callable.param` - 45% gap (22 → 12 lines)
- `bc.opcode` - 50% gap (20 → 10 lines)
- `num.globals` - 45% gap (22 → 12 lines)
- `types.int` - 100% gap (10 → 0 lines)
- `tuple.globals` - 42% gap (24 → 14 lines)
- `values.freeze_error` - 33% gap (30 → 20 lines)
- `tuple.value` - 50% gap (18 → 9 lines)
- `thin_box_slice_frozen_value.packed_impl` - 38% gap (24 → 15 lines)
- `stdlib.call_stack` - 25% gap (36 → 27 lines)
- `compiler.stmt` - 26% gap (34 → 25 lines)
- `eval.soft_error` - 100% gap (8 → 0 lines)
- `dict.dict_type` - 100% gap (8 → 0 lines)
- `heap.call_enter_exit` - 100% gap (8 → 0 lines)
- `string.interpolation` - 25% gap (32 → 24 lines)
- `string.str_type` - 31% gap (26 → 18 lines)
- `values.stack_guard` - 33% gap (24 → 16 lines)
- `typing.error` - 58% gap (12 → 5 lines)
- `runtime.inlined_frame` - 27% gap (26 → 19 lines)
- `alloc.chunk` - 58% gap (12 → 5 lines)
- `values.value_of` - 44% gap (16 → 9 lines)
- `compiler.compr` - 38% gap (16 → 10 lines)
- `none.none_type` - 100% gap (6 → 0 lines)
- `compiler.call` - 38% gap (16 → 10 lines)
- `num.typecheck` - 50% gap (12 → 6 lines)
- `pagable.error` - 75% gap (8 → 2 lines)
- `types.known_methods` - 38% gap (16 → 10 lines)
- `typing.function` - 50% gap (12 → 6 lines)
- `oracle.ctx` - 30% gap (20 → 14 lines)
- `alloc.chunk_part` - 50% gap (12 → 6 lines)
- `profile.data` - 50% gap (12 → 6 lines)
- `assert.conformance` - 30% gap (20 → 14 lines)
- `layout.freezer` - 38% gap (16 → 10 lines)
- `record.record_type` - 75% gap (8 → 2 lines)
- `values.index` - 27% gap (22 → 16 lines)
- `heap.maybe_uninit_slice_util` - 43% gap (14 → 8 lines)
- `values.recursive_repr_or_json_guard` - 43% gap (14 → 8 lines)
- `bc.slow_arg` - 36% gap (14 → 9 lines)
- `types.unbound` - 50% gap (10 → 5 lines)
- `profile.flamegraph` - 36% gap (14 → 9 lines)
- `compiler.known` - 42% gap (12 → 7 lines)
- `profile.csv` - 50% gap (10 → 5 lines)
- `params.parser` - 36% gap (14 → 9 lines)
- `range.range_type` - 50% gap (8 → 4 lines)
- `cast` - 50% gap (8 → 4 lines)
- `profile.by_type` - 25% gap (16 → 12 lines)
- `values.error` - 50% gap (8 → 4 lines)
- `typing.interface` - 50% gap (8 → 4 lines)
- `enumeration.ty_enum_type` - 50% gap (8 → 4 lines)
- `bool.value` - 67% gap (6 → 2 lines)
- `debug.evaluate` - 33% gap (12 → 8 lines)
- `collections.aligned_padded_str` - 38% gap (8 → 5 lines)
- `avalues.list` - 30% gap (10 → 7 lines)
- `unused_loads.find` - 25% gap (12 → 9 lines)
- `typing.structs` - 30% gap (10 → 7 lines)
- `types.list_or_tuple` - 50% gap (6 → 3 lines)
- `compiler.expr_bool` - 38% gap (8 → 5 lines)
- `layout.value_captured` - 25% gap (8 → 6 lines)
- `types.any_array` - 33% gap (6 → 4 lines)
- `typing.typecheck` - 33% gap (6 → 4 lines)

