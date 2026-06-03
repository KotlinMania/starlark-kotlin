# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 364/470 (77.4%)
- **Function parity:** 2940/4585 matched (target 4971) — 64.1%
- **Class/type parity:** 790/1209 matched (target 1472) — 65.3%
- **Combined symbol parity:** 3730/5794 matched (target 6443) — 64.4%
- **Average inline-code cosine:** 0.25 (function body across 341 matched files)
- **Average documentation cosine:** 0.67 (doc text across 341 matched files)
- **Cheat-zeroed Files:** 219
- **Critical Issues:** 287 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 178
- **Priority Score:** 178162720.0
- **Functions:** 106/118 matched (target 158)
- **Missing functions:** `fmt`, `eq`, `testing_new_int`, `_test_send_sync`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function`
- **Types:** 6/9 matched
- **Missing types:** `DisplayWithTypeImpl`, `Canonical`, `String`
- **Symbol Deficit:** 15 (functions: 12, types: 3)
- **Missing Tests:** 9 of 9 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 2. typing.ty
- **Similarity:** 0.75 (needs 10% improvement)
- **Dependencies:** 109
- **Priority Score:** 109015400.0
- **Functions:** 49/50 matched (target 57)
- **Missing functions:** `fmt`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Minor refinements needed

### 3. typing.starlark_value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 76
- **Priority Score:** 76053808.0
- **Functions:** 29/34 matched (target 43)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Symbol Deficit:** 5 (functions: 5, types: 0)
- **Action:** Deep review - likely missing major functionality

### 4. params.display
- **Similarity:** 0.75 (needs 10% improvement)
- **Dependencies:** 76
- **Priority Score:** 76000704.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Action:** Review and complete missing sections

### 5. runtime.evaluator
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 56
- **Priority Score:** 56026712.0
- **Functions:** 58/60 matched (target 63)
- **Missing functions:** `drop`, `add_diagnostics`
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Action:** Deep review - likely missing major functionality

### 6. values.trace
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 52
- **Priority Score:** 52000208.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 7. values.freeze
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42010312.0
- **Functions:** 1/1 matched (target 31)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Frozen`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 8. values.alloc_value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42000608.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 9. layout.freezer
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 36
- **Priority Score:** 36000608.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 10. values.frozen_ref
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 27
- **Priority Score:** 27022110.0
- **Functions:** 17/17 matched (target 23)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Target`, `Frozen`
- **Symbol Deficit:** 2 (functions: 0, types: 2)
- **Action:** Deep review - likely missing major functionality

### 11. none.none_type
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 27
- **Priority Score:** 27011310.0
- **Functions:** 11/11 matched (target 16)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Error`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 12. runtime.frame_span
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 13. runtime.arguments
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 25
- **Priority Score:** 25043810.0
- **Functions:** 26/30 matched (target 49)
- **Missing functions:** `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Symbol Deficit:** 4 (functions: 4, types: 0)
- **Missing Tests:** 4 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 14. typing.type_compiled
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 22
- **Priority Score:** 22000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 15. environment.globals
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 21
- **Priority Score:** 21054010.0
- **Functions:** 30/35 matched (target 38)
- **Missing functions:** `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden`
- **Types:** 5/5 matched
- **Missing types:** _none_
- **Symbol Deficit:** 5 (functions: 5, types: 0)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 16. derive.module
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 21
- **Priority Score:** 21000010.0
- **Functions:** 0/0 matched (target 21)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 17. values.value_of_unchecked
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20102510.0
- **Functions:** 12/18 matched (target 17)
- **Missing functions:** `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant`
- **Types:** 3/7 matched (target 4)
- **Missing types:** `Canonical`, `Frozen`, `Error`, `ReprNotSendSync`
- **Symbol Deficit:** 10 (functions: 6, types: 4)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 18. util.refcell
- **Similarity:** 0.32 (needs 53% improvement)
- **Dependencies:** 20
- **Priority Score:** 20010206.0
- **Functions:** 1/2 matched (target 11)
- **Missing functions:** `test_unleak_borrow`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 1 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 19. __derive_refs.param_spec
- **Similarity:** 0.83 (needs 2% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 20. environment.methods
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 17
- **Priority Score:** 17032310.0
- **Functions:** 17/19 matched (target 21)
- **Missing functions:** `test_set_attribute`, `get_methods`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `Magic`
- **Symbol Deficit:** 3 (functions: 2, types: 1)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 21. values.iter
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 17
- **Priority Score:** 17020710.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 14)
- **Missing types:** `Item`
- **Symbol Deficit:** 2 (functions: 1, types: 1)
- **Action:** Deep review - likely missing major functionality

### 22. values.error
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 20)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 23. private
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 15
- **Priority Score:** 15000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 24. collections.symbol
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 15
- **Priority Score:** 15000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 25. layout.avalue
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 14
- **Priority Score:** 14021110.0
- **Functions:** 6/8 matched (target 11)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 26. layout.const_frozen_string
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12020210.0
- **Functions:** 0/2 matched (target 1)
- **Missing functions:** `test_const_frozen_string_for_short_strings`, `test_const_frozen_string`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 27. typing.tuple
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12010710.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 28. int.inline_int
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 11
- **Priority Score:** 11123910.0
- **Functions:** 25/34 matched (target 43)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits`
- **Types:** 2/5 matched (target 6)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Symbol Deficit:** 12 (functions: 9, types: 3)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **layout.heap** (109 deps)
   - Path: `src/values/layout/heap.rs`
   - Essential for 109 other files

2. **assert** (84 deps)
   - Path: `src/assert.rs`
   - Essential for 84 other files

3. **debug** (53 deps)
   - Path: `src/debug.rs`
   - Essential for 53 other files

4. **derive.unpack_value** (51 deps)
   - Path: `src/tests/derive/unpack_value.rs`
   - Essential for 51 other files

5. **coerce** (34 deps)
   - Path: `src/coerce.rs`
   - Essential for 34 other files

6. **util.arc_str** (21 deps)
   - Path: `src/util/arc_str.rs`
   - Essential for 21 other files

7. **types.dict** (12 deps)
   - Path: `src/values/types/dict.rs`
   - Essential for 12 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. layout.value

- **Target:** `layout.Value [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 178
- **Priority Score:** 178162720.0
- **Functions:** 106/118 matched (target 158)
- **Missing functions:** `fmt`, `eq`, `testing_new_int`, `_test_send_sync`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function`
- **Types:** 6/9 matched
- **Missing types:** `DisplayWithTypeImpl`, `Canonical`, `String`
- **Tests:** 0/9 matched

### 2. typing.ty

- **Target:** `typing.Ty`
- **Similarity:** 0.75
- **Dependents:** 109
- **Priority Score:** 109015400.0
- **Functions:** 49/50 matched (target 57)
- **Missing functions:** `fmt`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_

### 3. typing.starlark_value

- **Target:** `typing.StarlarkValue [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 76
- **Priority Score:** 76053808.0
- **Functions:** 29/34 matched (target 43)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_

### 4. params.display

- **Target:** `params.Display`
- **Similarity:** 0.75
- **Dependents:** 76
- **Priority Score:** 76000704.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_

### 5. runtime.evaluator

- **Target:** `runtime.Evaluator [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 56
- **Priority Score:** 56026712.0
- **Functions:** 58/60 matched (target 63)
- **Missing functions:** `drop`, `add_diagnostics`
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_
- **Lint issues:** 3

### 6. values.trace

- **Target:** `values.Trace [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 52
- **Priority Score:** 52000208.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 7. values.freeze

- **Target:** `values.Freeze [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 42
- **Priority Score:** 42010312.0
- **Functions:** 1/1 matched (target 31)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Frozen`

### 8. values.alloc_value

- **Target:** `values.AllocValue [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 42
- **Priority Score:** 42000608.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_

### 9. layout.freezer

- **Target:** `layout.Freezer [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 36
- **Priority Score:** 36000608.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 10. compiler.span

- **Target:** `compiler.Span`
- **Similarity:** 0.92
- **Dependents:** 29
- **Priority Score:** 29010400.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`

### 11. values.frozen_ref

- **Target:** `values.FrozenRef [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 27
- **Priority Score:** 27022110.0
- **Functions:** 17/17 matched (target 23)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Target`, `Frozen`

### 12. none.none_type

- **Target:** `none.NoneType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 27
- **Priority Score:** 27011310.0
- **Functions:** 11/11 matched (target 16)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Error`

### 13. runtime.frame_span

- **Target:** `runtime.FrameSpan`
- **Similarity:** 0.65
- **Dependents:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 14. runtime.arguments

- **Target:** `runtime.Arguments [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 25
- **Priority Score:** 25043810.0
- **Functions:** 26/30 matched (target 49)
- **Missing functions:** `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 15. typing.type_compiled

- **Target:** `type_compiled.TypeCompiled [STUB]`
- **Similarity:** 0.00
- **Dependents:** 22
- **Priority Score:** 22000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 16. environment.globals

- **Target:** `environment.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 21
- **Priority Score:** 21054010.0
- **Functions:** 30/35 matched (target 38)
- **Missing functions:** `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden`
- **Types:** 5/5 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 17. derive.module

- **Target:** `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 21
- **Priority Score:** 21000010.0
- **Functions:** 0/0 matched (target 21)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `src/syntax/module.rs` vs expected `tests/derive/module.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module.rs` (current: `// port-lint: source src/syntax/module.rs`)
- **Lint issues:** 1

### 18. values.value_of_unchecked

- **Target:** `values.ValueOfUnchecked [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20102510.0
- **Functions:** 12/18 matched (target 17)
- **Missing functions:** `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant`
- **Types:** 3/7 matched (target 4)
- **Missing types:** `Canonical`, `Frozen`, `Error`, `ReprNotSendSync`
- **Tests:** 0/5 matched

### 19. util.refcell

- **Target:** `refcell.RefCell`
- **Similarity:** 0.32
- **Dependents:** 20
- **Priority Score:** 20010206.0
- **Functions:** 1/2 matched (target 11)
- **Missing functions:** `test_unleak_borrow`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 20. __derive_refs.param_spec

- **Target:** `deriverefs.ParamSpec [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/derive_refs/param_spec.rs` vs expected `__derive_refs/param_spec.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/param_spec.rs` (current: `// port-lint: source src/derive_refs/param_spec.rs`)
- **Lint issues:** 1

### 21. environment.methods

- **Target:** `environment.Methods [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 17
- **Priority Score:** 17032310.0
- **Functions:** 17/19 matched (target 21)
- **Missing functions:** `test_set_attribute`, `get_methods`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `Magic`
- **Tests:** 0/2 matched

### 22. values.iter

- **Target:** `values.Iter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 17
- **Priority Score:** 17020710.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 14)
- **Missing types:** `Item`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/small_map/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/small_set/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec2/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec_map/iter.rs` vs expected `values/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/small_map/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/small_set/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/vec2/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/vec_map/iter.rs`)
- **Lint issues:** 5

### 23. values.error

- **Target:** `values.Error`
- **Similarity:** 0.62
- **Dependents:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 20)
- **Missing types:** _none_

### 24. private

- **Target:** `starlark.Private [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 15
- **Priority Score:** 15000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 25. collections.symbol

- **Target:** `collections.Symbol [STUB]`
- **Similarity:** 0.00
- **Dependents:** 15
- **Priority Score:** 15000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 26. layout.avalue

- **Target:** `layout.AValue [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 14
- **Priority Score:** 14021110.0
- **Functions:** 6/8 matched (target 11)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 27. layout.const_frozen_string

- **Target:** `layout.ConstFrozenString [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12020210.0
- **Functions:** 0/2 matched (target 1)
- **Missing functions:** `test_const_frozen_string_for_short_strings`, `test_const_frozen_string`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 28. typing.tuple

- **Target:** `typing.Tuple [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12010710.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 29. layout.value_lifetimeless

- **Target:** `layout.ValueLifetimeless`
- **Similarity:** 1.00
- **Dependents:** 12
- **Priority Score:** 12000100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 30. int.inline_int

- **Target:** `int.InlineInt [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 11
- **Priority Score:** 11123910.0
- **Functions:** 25/34 matched (target 43)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits`
- **Types:** 2/5 matched (target 6)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Tests:** 0/2 matched

### 31. int.pointer_i32

- **Target:** `int.PointerI32 [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9043310.0
- **Functions:** 28/31 matched (target 34)
- **Missing functions:** `eq`, `fmt`, `serialize`
- **Types:** 1/2 matched
- **Missing types:** `Canonical`

### 32. types.type_instance_id

- **Target:** `types.TypeInstanceId`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9010210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `r#gen`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 33. any

- **Target:** `starlark.Any`
- **Similarity:** 0.04
- **Dependents:** 8
- **Priority Score:** 8222709.5
- **Functions:** 2/12 matched (target 3)
- **Missing functions:** `static_type_id`, `static_type_of`, `is`, `test_can_convert`, `convert_value`, `convert_any`, `test_any_lifetime`, `test`, `test_provides_static_type_id`, `test_provides_static_type_when_type_parameter_has_bound_with_lifetime`
- **Types:** 3/15 matched (target 37)
- **Missing types:** `StaticType`, `Value`, `Value2`, `Aaa`, `Bbb`, `Ccc`, `LifetimeTypeConst`, `TypeWithConstraint`, `TypeWhichDoesNotImplementAnyLifetime`, `TypeWithStaticLifetime`, `My`, `FooBar`
- **Tests:** 0/7 matched

### 34. layout.aligned_size

- **Target:** `layout.AlignedSize [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8081510.0
- **Functions:** 6/13 matched (target 15)
- **Missing functions:** `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/2 matched

### 35. eval.compiler

- **Target:** `eval.Compiler [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8000710.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 36. cast

- **Target:** `starlark.Cast [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8000310.0
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 37. types.bigint

- **Target:** `types.Bigint [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7447410.0
- **Functions:** 29/73 matched (target 35)
- **Missing functions:** `unpack_integer`, `eq`, `test_parse`, `test_str`, `test_repr`, `test_equals`, `test_plus`, `test_compare_big_big`, `test_compare_big_small`, `test_compare_big_float`, `test_add_big`, `test_add_big_small`, `test_add_big_float`, `test_mul_big`, `test_mul_big_small`, `test_mul_big_float`, `test_div_big`, `test_div_big_small`, `test_div_big_float`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_big_float`, `test_percent_big`, `test_percent_big_small`, `test_percent_big_float`, `test_bit_and_big`, `test_bit_and_big_small`, `test_bit_and_float`, `test_bit_or_big`, `test_bit_or_big_small`, `test_bit_or_float`, `test_bit_xor_big`, `test_bit_xor_big_small`, `test_bit_xor_float`, `test_bit_not`, `test_left_shift`, `test_left_shift_small`, `test_left_shift_float`, `test_right_shift`, `test_right_shift_small`, `test_right_shift_float`, `test_int_function`, `test_hash`, `test_int_type_matches_bigint`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/42 matched

### 38. runtime.frozen_file_span

- **Target:** `runtime.FrozenFileSpan [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7011110.0
- **Functions:** 9/10 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 39. values.starlark_type_id

- **Target:** `values.StarlarkTypeId`
- **Similarity:** 0.61
- **Dependents:** 7
- **Priority Score:** 7010804.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `eq`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 40. compiler.opt_ctx

- **Target:** `compiler.OptCtx`
- **Similarity:** 0.71
- **Dependents:** 7
- **Priority Score:** 7000703.0
- **Functions:** 5/5 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 41. type_compiled.type_matcher_factory

- **Target:** `type_compiled.TypeMatcherFactory`
- **Similarity:** 0.69
- **Dependents:** 7
- **Priority Score:** 7000603.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 42. runtime.small_duration

- **Target:** `runtime.SmallDuration [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6040910.0
- **Functions:** 4/7 matched (target 9)
- **Missing functions:** `from_millis`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched

### 43. typing.typecheck

- **Target:** `typing.Typecheck [STUB]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6030710.0
- **Functions:** 2/5 matched
- **Missing functions:** `fmt`, `find_bindings_by_name`, `find_first_binding`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 44. dict.dict_type

- **Target:** `dict.DictType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6030510.0
- **Functions:** 1/2 matched (target 4)
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 45. none.none_or

- **Target:** `none.NoneOr`
- **Similarity:** 0.73
- **Dependents:** 6
- **Priority Score:** 6021002.5
- **Functions:** 7/7 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 4)
- **Missing types:** `Canonical`, `Error`

### 46. values.freeze_error

- **Target:** `values.FreezeError [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6020810.0
- **Functions:** 3/4 matched (target 6)
- **Missing functions:** `from`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `FreezeResult`

### 47. layout.value_alloc_size

- **Target:** `layout.ValueAllocSize [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6010610.0
- **Functions:** 4/5 matched
- **Missing functions:** `layout`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 48. compiler.stmt

- **Target:** `compiler.Stmt [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6003210.0
- **Functions:** 25/25 matched (target 28)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 24)
- **Missing types:** _none_

### 49. profile.profiler_type

- **Target:** `profile.ProfilerType`
- **Similarity:** 0.69
- **Dependents:** 6
- **Priority Score:** 6000303.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 50. values.layout

- **Target:** `values.Layout [STUB]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 51. tests.def

- **Target:** `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5141410.0
- **Functions:** 0/14 matched (target 4)
- **Missing functions:** `test_lambda`, `test_frozen_lambda`, `test_nested_def_1`, `test_nested_def_2`, `test_nested_def_3`, `test_lambda_capture_from_module`, `test_lambda_capture_from_def`, `test_lambda_capture_reassigned_from_def`, `test_def_freeze`, `test_frozen_lambda_nest`, `test_context_captured`, `test_lambda_errors`, `test_lambda_errors_nested`, `test_double_capture_and_freeze`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/14 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_syntax/src/syntax/def.rs` vs expected `tests/def.rs`
- **Proposed provenance header:** `// port-lint: source tests/def.rs` (current: `// port-lint: source starlark_syntax/src/syntax/def.rs`)
- **Lint issues:** 1

### 52. types.array

- **Target:** `types.Array [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5093410.0
- **Functions:** 23/32 matched (target 24)
- **Missing functions:** `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`, `debug`, `display`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 53. typing.arc_ty

- **Target:** `typing.ArcTy`
- **Similarity:** 0.60
- **Dependents:** 5
- **Priority Score:** 5021104.0
- **Functions:** 6/7 matched (target 16)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 10)
- **Missing types:** `Target`

### 54. typing.interface

- **Target:** `typing.Interface`
- **Similarity:** 0.60
- **Dependents:** 5
- **Priority Score:** 5000404.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 55. scope.scope_resolver_globals

- **Target:** `scope.ScopeResolverGlobals`
- **Similarity:** 0.72
- **Dependents:** 5
- **Priority Score:** 5000403.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 56. eval.bc

- **Target:** `bc.Bc [STUB]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 57. enumeration.enum_type

- **Target:** `enumeration.EnumType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4194410.0
- **Functions:** 21/36 matched (target 24)
- **Missing functions:** `fmt`, `r#type`, `values`, `test_enum_type_as_type_pass`, `test_enum_type_fail_runtime`, `test_enum_type_fail_compile_time`, `test_enum_is_callable`, `test_enum_value_index`, `test_enum_value_index_correct_type`, `test_enum_index`, `test_enum_index_fail`, `test_enum_call`, `test_enum_attribute_access`, `test_enum_attribute_access_invalid`, `test_enum_attribute_access_type`
- **Types:** 4/8 matched (target 6)
- **Missing types:** `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical`
- **Tests:** 0/12 matched
- **Lint issues:** 3

### 58. types.starlark_value_as_type

- **Target:** `types.StarlarkValueAsType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4091710.0
- **Functions:** 6/13 matched (target 8)
- **Missing functions:** `fmt`, `new`, `compiler_args_globals`, `compiler_args`, `test_pass`, `test_fail_compile_time`, `test_fail_runtime`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `CompilerArgs`
- **Tests:** 0/5 matched

### 59. bc.frame

- **Target:** `bc.Frame [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4082610.0
- **Functions:** 16/24 matched (target 31)
- **Missing functions:** `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 60. values.demand

- **Target:** `values.Demand`
- **Similarity:** 0.37
- **Dependents:** 4
- **Priority Score:** 4061106.2
- **Functions:** 4/7 matched (target 5)
- **Missing functions:** `payload`, `provide`, `test_trait_downcast`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `SomeTrait`, `StaticType`, `MyValue`
- **Tests:** 0/3 matched

### 61. values.value_of

- **Target:** `values.ValueOf [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4051010.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `deref`, `fmt`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Target`, `Canonical`, `Error`

### 62. profile.alloc_counts

- **Target:** `profile.AllocCounts`
- **Similarity:** 0.40
- **Dependents:** 4
- **Priority Score:** 4040606.0
- **Functions:** 1/4 matched (target 5)
- **Missing functions:** `normalize_for_golden_tests`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched

### 63. bc.native_function

- **Target:** `bc.NativeFunction`
- **Similarity:** 0.51
- **Dependents:** 4
- **Priority Score:** 4010505.0
- **Functions:** 3/4 matched
- **Missing functions:** `fun`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 64. types.ellipsis

- **Target:** `types.Ellipsis`
- **Similarity:** 0.55
- **Dependents:** 4
- **Priority Score:** 4010404.5
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `test_ellipsis`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 65. record.record_type

- **Target:** `record.RecordType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3133010.0
- **Functions:** 15/22 matched (target 18)
- **Missing functions:** `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
- **Tests:** 0/5 matched
- **Lint issues:** 1

### 66. alloc.chunk

- **Target:** `alloc.Chunk [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3092210.0
- **Functions:** 11/19 matched (target 18)
- **Missing functions:** `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`, `test_alloc_release`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `ChunkDataEmpty`
- **Tests:** 0/3 matched

### 67. stdlib.call_stack

- **Target:** `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3061410.0
- **Functions:** 7/13 matched (target 14)
- **Missing functions:** `fmt`, `global`, `test_simple`, `test_strip_one`, `test_strip_all`, `test_call_stack_frame`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlark_syntax/src/call_stack.rs` vs expected `stdlib/call_stack.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/call_stack.rs` (current: `// port-lint: source ../starlark_syntax/src/call_stack.rs`)
- **Lint issues:** 1

### 68. errors.did_you_mean

- **Target:** `errors.DidYouMean [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3050610.0
- **Functions:** 1/6 matched (target 2)
- **Missing functions:** `prefixes`, `typos`, `best`, `very_short`, `earlier_variants_are_more_important`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 69. list.alloc

- **Target:** `list.Alloc [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3040510.0
- **Functions:** 0/3 matched (target 0)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 70. list.list_type

- **Target:** `list.ListType`
- **Similarity:** 0.37
- **Dependents:** 3
- **Priority Score:** 3030506.2
- **Functions:** 1/2 matched (target 5)
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 71. profile.instant

- **Target:** `profile.Instant [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3020610.0
- **Functions:** 3/4 matched (target 9)
- **Missing functions:** `sub`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`

### 72. compiler.constants

- **Target:** `compiler.Constants [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3020510.0
- **Functions:** 1/3 matched (target 5)
- **Missing functions:** `eq`, `test_constants`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 73. values.unpack_and_discard

- **Target:** `values.UnpackAndDiscard [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3020510.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 74. sealed

- **Target:** `starlark.Sealed [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 75. types.record

- **Target:** `types.Record [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 76. compiler.small_vec_1

- **Target:** `compiler.SmallVec1 [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2101510.0
- **Functions:** 4/11 matched (target 9)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter`
- **Types:** 1/4 matched (target 3)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 77. util.arc_or_static

- **Target:** `util.ArcOrStatic`
- **Similarity:** 0.42
- **Dependents:** 2
- **Priority Score:** 2061305.9
- **Functions:** 5/10 matched (target 9)
- **Missing functions:** `fmt`, `eq`, `partial_cmp`, `cmp`, `hash`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Target`

### 78. typing.type_type

- **Target:** `typing.TypeType`
- **Similarity:** 0.27
- **Dependents:** 2
- **Priority Score:** 2050807.2
- **Functions:** 2/5 matched (target 3)
- **Missing functions:** `test`, `module`, `takes_type`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/3 matched

### 79. alloc.chunk_part

- **Target:** `alloc.ChunkPart`
- **Similarity:** 0.75
- **Dependents:** 2
- **Priority Score:** 2041602.5
- **Functions:** 11/15 matched (target 16)
- **Missing functions:** `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`, `test_is_full`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 80. values.owned_frozen_ref

- **Target:** `values.OwnedFrozenRef [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2031510.0
- **Functions:** 10/12 matched (target 19)
- **Missing functions:** `fmt`, `deref`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Target`

### 81. layout.const_type_id

- **Target:** `layout.ConstTypeId [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2030610.0
- **Functions:** 2/5 matched (target 4)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 82. runtime.rust_loc

- **Target:** `runtime.RustLoc [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2030310.0
- **Functions:** 0/3 matched (target 1)
- **Missing functions:** `rust_loc_globals`, `invoke`, `test_rust_loc`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 83. avalues.str_

- **Target:** `avalues.Str [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2021410.0
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 84. values.stack_guard

- **Target:** `values.StackGuard [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2010510.0
- **Functions:** 3/4 matched
- **Missing functions:** `drop`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 85. collections.aligned_padded_str

- **Target:** `alignedpaddedstr.AlignedPaddedStr`
- **Similarity:** 0.34
- **Dependents:** 2
- **Priority Score:** 2010406.6
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 86. profile.string_index

- **Target:** `profile.StringIndex [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 87. runtime.file_loader

- **Target:** `runtime.FileLoader`
- **Similarity:** 0.70
- **Dependents:** 2
- **Priority Score:** 2000403.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 88. collections.string_pool

- **Target:** `collections.StringPool [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000310.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 89. def_inline.local_as_value

- **Target:** `def_inline.LocalAsValue [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000210.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 90. hint

- **Target:** `starlark.Hint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.95
- **Dependents:** 2
- **Priority Score:** 2000200.5
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec_map/hint.rs` vs expected `hint.rs`
- **Proposed provenance header:** `// port-lint: source hint.rs` (current: `// port-lint: source src/vec_map/hint.rs`)
- **Lint issues:** 1

### 91. values.thin_box_slice_frozen_value

- **Target:** `values.ThinBoxSliceFrozenValue [STUB]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 92. heap.arena

- **Target:** `heap.Arena [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1224410.0
- **Functions:** 18/37 matched (target 23)
- **Missing functions:** `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty`
- **Types:** 4/7 matched (target 6)
- **Missing types:** `ChunkIter`, `Item`, `ArenaUninit`
- **Tests:** 0/7 matched

### 93. collections.alloca

- **Target:** `collections.Alloca [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1202610.0
- **Functions:** 5/22 matched (target 5)
- **Missing functions:** `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Buffer`, `Align`, `DropSliceGuard`
- **Tests:** 0/6 matched

### 94. stdlib

- **Target:** `starlark.Stdlib [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1131710.0
- **Functions:** 3/14 matched (target 3)
- **Missing functions:** `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Bool2`, `Error`
- **Tests:** 0/11 matched

### 95. string.interpolation

- **Target:** `string.Interpolation [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1081610.0
- **Functions:** 4/12 matched (target 6)
- **Missing functions:** `test_incomplete_format`, `test_unsupported_format_character`, `test_parse_percent_s_one`, `test_type_support_d`, `test_type_support_o`, `test_type_support_x`, `test_type_support_e`, `test_int_min`
- **Types:** 4/4 matched (target 20)
- **Missing types:** _none_
- **Tests:** 0/8 matched

### 96. types.list_or_tuple

- **Target:** `types.ListOrTuple [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1081010.0
- **Functions:** 1/5 matched
- **Missing functions:** `default`, `starlark_type_repr`, `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched

### 97. layout.pointer

- **Target:** `layout.Pointer [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1073710.0
- **Functions:** 25/32 matched (target 46)
- **Missing functions:** `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `test_int_tag`, `check`
- **Types:** 5/5 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 98. stdlib.breakpoint

- **Target:** `stdlib.Breakpoint`
- **Similarity:** 0.45
- **Dependents:** 1
- **Priority Score:** 1072305.5
- **Functions:** 11/17 matched (target 13)
- **Missing functions:** `global`, `breakpoint`, `reset_global_state`, `test_breakpoint_real`, `test_breakpoint_mock`, `test_breakpoint_disabled`
- **Types:** 5/6 matched
- **Missing types:** `Handler`
- **Tests:** 0/4 matched
- **Lint issues:** 1

### 99. types.any_complex

- **Target:** `types.AnyComplex`
- **Similarity:** 0.49
- **Dependents:** 1
- **Priority Score:** 1071205.1
- **Functions:** 4/7 matched
- **Missing functions:** `fmt`, `test_any_complex`, `freeze`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData`
- **Tests:** 0/2 matched

### 100. types.any_array

- **Target:** `types.AnyArray [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1061010.0
- **Functions:** 3/7 matched
- **Missing functions:** `fmt`, `drop`, `test_drop`, `test_allocation_size`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `IncrementOnDrop`
- **Tests:** 0/2 matched

### 101. util.rtabort

- **Target:** `util.Rtabort [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1050710.0
- **Functions:** 2/6 matched (target 3)
- **Missing functions:** `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `AbortOnDrop`
- **Tests:** 0/3 matched

### 102. string.dot_format

- **Target:** `string.DotFormat`
- **Similarity:** 0.43
- **Dependents:** 1
- **Priority Score:** 1041205.7
- **Functions:** 7/11 matched (target 7)
- **Missing functions:** `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 103. bc.if_debug

- **Target:** `bc.IfDebug [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1030910.0
- **Functions:** 5/8 matched (target 9)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 104. util.non_static_type_id

- **Target:** `util.NonStaticTypeId [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1030410.0
- **Functions:** 1/3 matched (target 1)
- **Missing functions:** `get_type_id`, `test_non_static_type_id`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `NonStaticAny`
- **Tests:** 0/1 matched

### 105. runtime.cheap_call_stack

- **Target:** `runtime.CheapCallStack [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1022010.0
- **Functions:** 15/17 matched
- **Missing functions:** `fmt`, `default`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_

### 106. avalues.simple

- **Target:** `avalues.Simple [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1021110.0
- **Functions:** 8/8 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 107. layout.value_captured

- **Target:** `layout.ValueCaptured`
- **Similarity:** 0.78
- **Dependents:** 1
- **Priority Score:** 1020802.2
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Frozen`

### 108. record.field

- **Target:** `record.Field [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1020610.0
- **Functions:** 4/5 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 0/1 matched
- **Missing types:** `FieldGen`

### 109. structs.unordered_hasher

- **Target:** `structs.UnorderedHasher [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1020610.0
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `_write`, `test_unordered_hasher`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 110. typing.bindings

- **Target:** `typing.Bindings [STUB]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1011110.0
- **Functions:** 7/8 matched (target 18)
- **Missing functions:** `get_for_clause`
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_

### 111. typing.structs

- **Target:** `typing.Structs`
- **Similarity:** 0.63
- **Dependents:** 1
- **Priority Score:** 1011003.7
- **Functions:** 7/8 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 112. heap.fast_cell

- **Target:** `heap.FastCell [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010810.0
- **Functions:** 6/7 matched
- **Missing functions:** `drop`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 113. read_line

- **Target:** `starlark.ReadLine [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `NoRustyline`

### 114. typing.function

- **Target:** `typing.Function [STUB]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1001510.0
- **Functions:** 12/12 matched (target 24)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **TODOs:** 1
- **Lint issues:** 2

### 115. analysis.lint_message

- **Target:** `analysis.LintMessage`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1000202.5
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 116. typing

- **Target:** `starlark.Typing [STUB]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 117. types.int

- **Target:** `types.Int [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 118. types.bool

- **Target:** `types.Bool`
- **Similarity:** 1.00
- **Dependents:** 1
- **Priority Score:** 1000000.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 119. bc.instr_impl

- **Target:** `bc.InstrImpl`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 777001.7
- **Functions:** 7/7 matched (target 95)
- **Missing functions:** _none_
- **Types:** 87/163 matched (target 104)
- **Missing types:** `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc`
- **Lint issues:** 10

### 120. set.methods

- **Target:** `set.Methods [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 506910.0
- **Functions:** 18/68 matched (target 19)
- **Missing functions:** `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/50 matched

### 121. string.str_type

- **Target:** `string.StrType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 495110.0
- **Functions:** 2/47 matched (target 25)
- **Missing functions:** `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_string_corruption`, `test_escape_characters`, `test_string_hash`, `test_zero_length_string_hash_is_not_zero`, `test_string_len`, `test_arithmetic_on_string`, `test_slice_string`, `test_string_is_in`, `test_successive_add`, `test_string_index`, `test_str`
- **Types:** 0/4 matched (target 0)
- **Missing types:** `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target`
- **Tests:** 0/11 matched

### 122. int.int_or_big

- **Target:** `int.IntOrBig [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 265310.0
- **Functions:** 24/46 matched (target 57)
- **Missing functions:** `starlark_type_repr`, `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_small_big`, `test_floor_div_small`, `test_percent_big`, `test_percent_big_small`, `test_percent_small_big`, `test_percent_small`
- **Types:** 3/7 matched (target 11)
- **Missing types:** `Canonical`, `Err`, `Error`, `Output`
- **Tests:** 0/9 matched
- **TODOs:** 3

### 123. thin_box_slice_frozen_value.thin_box

- **Target:** `thinboxslicefrozenvalue.ThinBox [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 253210.0
- **Functions:** 6/29 matched (target 11)
- **Missing functions:** `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`, `test_empty`, `test_from_iter_sized`, `test_from_iter_unknown_size`, `test_stress`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `ThinBoxSliceLayout`, `Target`
- **Tests:** 0/4 matched

### 124. set.value

- **Target:** `set.Value [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 235910.0
- **Functions:** 30/50 matched (target 47)
- **Missing functions:** `fmt`, `test_bit_or`, `test_bit_or_lhs_empty`, `test_bit_or_rhs_empty`, `test_bit_or_fail_iter`, `test_bit_or_ord`, `test_bit_and`, `test_bit_and_lhs_empty`, `test_bit_and_rhs_empty`, `test_bit_and_ord`, `test_bit_and_fail_iter`, `test_bit_xor`, `test_bit_xor_ord`, `test_bit_xor_lhs_empty`, `test_bit_xor_rhs_empty`, `test_bit_xor_fail_iter`, `test_sub`, `test_sub_empty_lhs`, `test_sub_empty_rhs`, `test_sub_fail_iter`
- **Types:** 6/9 matched (target 6)
- **Missing types:** `Canonical`, `Frozen`, `ContentRef`
- **Tests:** 0/19 matched
- **Lint issues:** 1

### 125. values.typing.callable

- **Target:** `kotlin.io.github.kotlinmania.starlark.values.typing.Callable [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 234010.0
- **Functions:** 12/32 matched (target 31)
- **Missing functions:** `clone`, `fmt`, `trace`, `_assert_sync_send`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_pass`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `test_callable_checked_runtime`, `module`, `good`, `bad`
- **Types:** 5/8 matched (target 6)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 0/15 matched

### 126. typing.user

- **Target:** `typing.User`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 173506.8
- **Functions:** 13/27 matched (target 26)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`, `test_intersect_with_abstract_type`, `test_ty_user_intersects_with_base_starlark_value`
- **Types:** 5/8 matched
- **Missing types:** `AbstractPlant`, `FruitCallable`, `Fruit`
- **Tests:** 0/10 matched

### 127. analysis.names

- **Target:** `analysis.Names`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 154305.2
- **Functions:** 21/35 matched (target 31)
- **Missing functions:** `new`, `ident`, `assign_ident`, `lint`, `about`, `test_lint_unused`, `test_lint_duplicate_assign`, `test_lint_unassigned`, `test_lint_undefined`, `test_early_fail`, `test_assign_for_next`, `test_flow_control`, `test_lambda_capture`, `test_global_defined_later`
- **Types:** 7/8 matched (target 13)
- **Missing types:** `AstStrExt`
- **Tests:** 0/10 matched

### 128. float.float

- **Target:** `float.Float [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 154210.0
- **Functions:** 26/39 matched (target 33)
- **Missing functions:** `fmt`, `non_finite`, `test_write_non_finite`, `decimal`, `test_write_decimal`, `scientific`, `test_write_scientific`, `compact`, `test_write_compact`, `test_arithmetic_operators`, `test_dictionary_key`, `test_comparisons`, `test_comparisons_by_sorting`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/12 matched

### 129. layout.typed

- **Target:** `layout.ValueTyped [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 153810.0
- **Functions:** 21/31 matched (target 44)
- **Missing functions:** `fmt`, `serialize`, `eq`, `deref`, `unpack_value_impl`, `int`, `test_unpack_value_for_frozen_value_typed`, `module`, `mutable`, `takes_frozen_value_typed`
- **Types:** 2/7 matched (target 2)
- **Missing types:** `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError`
- **Tests:** 0/5 matched

### 130. scope.payload

- **Target:** `scope.Payload [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 152410.0
- **Functions:** 0/7 matched (target 0)
- **Missing functions:** `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id`
- **Types:** 9/17 matched (target 14)
- **Missing types:** `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt`

### 131. thin_box_slice_frozen_value.packed_impl

- **Target:** `thinboxslicefrozenvalue.PackedImpl [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 152110.0
- **Functions:** 4/18 matched (target 8)
- **Missing functions:** `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `default`, `fmt`, `eq`, `across_lengths`, `test_strings`, `test_ints`, `test_mixed_types`, `test_default`, `test_empty`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Target`
- **Tests:** 0/6 matched

### 132. string.repr

- **Target:** `string.Repr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 132310.0
- **Functions:** 9/22 matched (target 11)
- **Missing functions:** `or4`, `push_vec_tail`, `test_to_repr`, `test_string_repr`, `test`, `test_to_repr_long_smoke`, `string_repr_for_test`, `to_repr_sse`, `to_repr_no_escape_all_lengths`, `to_repr_tail_escape_all_lengths`, `to_repr_middle_escape_all_lengths`, `test_chunk_non_ascii_or_need_escape`, `load`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/11 matched

### 133. list.value

- **Target:** `list.Value [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 126410.0
- **Functions:** 46/56 matched (target 96)
- **Missing functions:** `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare`
- **Types:** 6/8 matched (target 9)
- **Missing types:** `List`, `Canonical`
- **Tests:** 0/7 matched
- **Lint issues:** 2

### 134. dict.value

- **Target:** `dict.Value [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 126210.0
- **Functions:** 43/52 matched (target 79)
- **Missing functions:** `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `serialize`, `test_mutate_dict`, `test_get_str`, `test_repr_cycle`
- **Types:** 7/10 matched
- **Missing types:** `Canonical`, `Frozen`, `ContentRef`
- **Tests:** 0/3 matched
- **Lint issues:** 2

### 135. num.value

- **Target:** `num.Value`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 122606.8
- **Functions:** 11/22 matched (target 27)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul`, `test_from_value`, `test_conversion_to_float`, `test_conversion_to_int`, `test_hashing`, `test_eq`
- **Types:** 3/4 matched (target 6)
- **Missing types:** `Output`
- **Tests:** 0/5 matched

### 136. stdlib.extra

- **Target:** `stdlib.Extra`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 122008.6
- **Functions:** 5/16 matched (target 21)
- **Missing functions:** `fmt`, `print`, `pprint`, `pstr`, `prepr`, `test_filter`, `test_map`, `test_debug`, `test_print`, `test_pstr`, `test_prepr`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `PrintHandlerImpl`
- **Tests:** 0/6 matched

### 137. pagable.vtable_registry

- **Target:** `pagable.VtableRegistry [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 121710.0
- **Functions:** 3/13 matched (target 6)
- **Missing functions:** `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_lookup_nonexistent_type`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `TestSimpleType`, `TestComplexGen`
- **Tests:** 0/9 matched

### 138. analysis

- **Target:** `starlark.Analysis`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 111309.5
- **Functions:** 1/12 matched (target 1)
- **Missing functions:** `module`, `test_lint_suppressions_keyword_matching`, `test_lint_suppressions_fn_with_many_issues`, `test_lint_suppressions_preceding_whitespace`, `test_lint_suppressions_with_space_separator`, `test_lint_suppressions_multiline_span`, `test_lint_suppressions_small_span`, `test_lint_suppressions_data`, `test_lint_suppressions_line_before`, `test_lint_suppressions_line_before_windows_newlines`, `test_lint_suppressions_inside_fn`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/11 matched

### 139. record.globals

- **Target:** `record.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 111210.0
- **Functions:** 1/12 matched (target 1)
- **Missing functions:** `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/9 matched

### 140. heap.heap_type

- **Target:** `heap.HeapType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 105510.0
- **Functions:** 37/47 matched (target 68)
- **Missing functions:** `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `test_send_sync`, `test_string_reallocated_on_heap`, `test_interned_string_equal`, `validate_str_interning`, `append_x`, `test_interned_str_starlark`
- **Types:** 8/8 matched (target 9)
- **Missing types:** _none_
- **Tests:** 0/6 matched

### 141. alloc.chain

- **Target:** `alloc.Chain [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102710.0
- **Functions:** 14/22 matched (target 19)
- **Missing functions:** `drop`, `test_default`, `test_new_drop`, `test_new_drop_many`, `test_split_at`, `test_split_at_len`, `test_split_at_zero`, `test_depth`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Item`, `ResetSplitAtZeroTest`
- **Tests:** 0/7 matched

### 142. range.range_type

- **Target:** `range.RangeType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102510.0
- **Functions:** 14/24 matched (target 21)
- **Missing functions:** `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `test_range_exhaustive`, `test_max_len`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/8 matched

### 143. stdlib.partial

- **Target:** `stdlib.Partial [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 101710.0
- **Functions:** 4/12 matched (target 7)
- **Missing functions:** `partial`, `fmt`, `eq`, `test_simple`, `test_star_to_partial`, `test_start_to_returned_func`, `test_no_args_to_partial`, `test_typecheck_bug`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Frozen`, `Canonical`
- **Tests:** 0/6 matched
- **Lint issues:** 1

### 144. typing.small_arc_vec_or_static

- **Target:** `typing.SmallArcVecOrStatic`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 101507.5
- **Functions:** 3/10 matched
- **Missing functions:** `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 145. stdlib.json

- **Target:** `stdlib.Json`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 101209.6
- **Functions:** 2/11 matched (target 24)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `json`, `encode`, `decode`, `test_json_encode`, `test_json_decode`, `test_json_very_large_int`, `test_json_128bit_and_beyond`
- **Types:** 0/1 matched (target 11)
- **Missing types:** `Canonical`
- **Tests:** 0/4 matched

### 146. layout.vtable

- **Target:** `layout.Vtable`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 97303.3
- **Functions:** 60/67 matched (target 65)
- **Missing functions:** `value_ptr`, `value_ref`, `drop_in_place`, `fmt`, `as_allocative`, `total_memory_for_profile`, `as_serialize`
- **Types:** 4/6 matched (target 4)
- **Missing types:** `GetTypeId`, `GetAllocativeKey`

### 147. type_compiled.compiled

- **Target:** `type_compiled.Compiled [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 84610.0
- **Functions:** 33/39 matched (target 48)
- **Missing functions:** `fmt`, `check_matches`, `starlark_type_repr`, `alloc_value`, `hash`, `eq`
- **Types:** 5/7 matched (target 12)
- **Missing types:** `StaticType`, `Canonical`

### 148. profile.bc

- **Target:** `profile.Bc`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 82904.8
- **Functions:** 12/19 matched (target 24)
- **Missing functions:** `sum`, `add_assign`, `default`, `test_smoke`, `test_smoke_2`, `test_bc_profile_data_merge`, `test_bc_pairs_profile_data_merge`
- **Types:** 9/10 matched (target 13)
- **Missing types:** `Data`
- **Tests:** 0/4 matched

### 149. analysis.flow

- **Target:** `analysis.Flow`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 82504.8
- **Functions:** 16/24 matched (target 31)
- **Missing functions:** `lint`, `module`, `about`, `test_lint_returns`, `test_lint_unreachable`, `test_lint_redundant`, `test_lint_misplaced_load`, `test_lint_no_effect`
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_
- **Tests:** 0/7 matched

### 150. alloc.allocator

- **Target:** `alloc.Allocator [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 82110.0
- **Functions:** 11/18 matched (target 15)
- **Missing functions:** `fmt`, `default`, `drop`, `test_small`, `test_big`, `random_iteration`, `test_many`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Item`
- **Tests:** 0/4 matched

### 151. typing.small_arc_vec

- **Target:** `typing.SmallArcVec`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 81406.9
- **Functions:** 4/11 matched (target 16)
- **Missing functions:** `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter`
- **Types:** 2/3 matched (target 5)
- **Missing types:** `Target`

### 152. tuple.unpack

- **Target:** `tuple.Unpack [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 81010.0
- **Functions:** 1/5 matched
- **Missing functions:** `default`, `starlark_type_repr`, `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched

### 153. tuple.value

- **Target:** `tuple.Value [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 73410.0
- **Functions:** 24/31 matched (target 37)
- **Missing functions:** `fmt`, `new`, `offset_of_content`, `typechecker_ty`, `test_to_str`, `test_repr_cycle`, `test_tuple_ellipsis_runtime`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Lint issues:** 1

### 154. profile.aggregated

- **Target:** `profile.Aggregated [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 73210.0
- **Functions:** 17/24 matched (target 35)
- **Missing functions:** `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `test_stacks_collect`, `test_stacks_collect_retained`, `test_merge`, `make`
- **Types:** 8/8 matched (target 10)
- **Missing types:** _none_
- **Tests:** 0/6 matched

### 155. structs.value

- **Target:** `structs.Value [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 72210.0
- **Functions:** 14/21 matched (target 30)
- **Missing functions:** `iter_frozen`, `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_to_json`, `test_comparison_bug`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 156. funcs.other

- **Target:** `funcs.Other [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71910.0
- **Functions:** 12/19 matched (target 13)
- **Missing functions:** `r#type`, `test_abs`, `test_constants`, `test_chr`, `test_hash`, `test_int`, `test_tuple`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/6 matched

### 157. typed.string

- **Target:** `typed.String [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71810.0
- **Functions:** 8/15 matched (target 59)
- **Missing functions:** `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`, `test_string_hashes`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 158. dict.methods

- **Target:** `dict.Methods [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71710.0
- **Functions:** 10/17 matched (target 12)
- **Missing functions:** `test_error_codes`, `test_dict_add`, `test_dict_with_duplicates`, `test_dict_update_with_self_pos`, `test_dict_update_with_self_as_kwargs`, `test_frozen_dict_cannot_be_updated_with_self_pos`, `test_frozen_dict_cannot_be_updated_with_self_as_kwargs`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched

### 159. layout.complex

- **Target:** `layout.Complex [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71710.0
- **Functions:** 9/13 matched (target 15)
- **Missing functions:** `unpack_value_impl`, `fmt`, `test_module`, `test_unpack`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 0/2 matched

### 160. docs.parse

- **Target:** `docs.Parse [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71610.0
- **Functions:** 8/15 matched (target 11)
- **Missing functions:** `parses_starlark_docstring`, `parses_rust_docstring`, `parses_and_removes_sections_from_starlark_docstring`, `parses_and_removes_sections_from_rust_docstring`, `arg`, `parses_starlark_function_docstring`, `parses_rust_function_docstring`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched

### 161. string.simd

- **Target:** `string.Simd [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71010.0
- **Functions:** 1/8 matched (target 4)
- **Missing functions:** `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec_map/simd.rs` vs expected `values/types/string/simd.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/simd.rs` (current: `// port-lint: source src/vec_map/simd.rs`)
- **Lint issues:** 1

### 162. record.ty_record_type

- **Target:** `record.TyRecordType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 70810.0
- **Functions:** 0/7 matched (target 0)
- **Missing functions:** `test_good`, `test_fail_compile_time`, `test_fail_runtime_time`, `test_record_instance_typechecker_ty`, `test_typecheck_field_pass`, `test_typecheck_field_fail`, `test_typecheck_record_type_call`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched

### 163. compiler.scope

- **Target:** `compiler.Scope [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 67110.0
- **Functions:** 48/51 matched (target 70)
- **Missing functions:** `from`, `assign_ident_impl`, `new`
- **Types:** 17/20 matched (target 28)
- **Missing types:** `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue`

### 164. assert.assert

- **Target:** `assert.Assert [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 65210.0
- **Functions:** 44/50 matched (target 66)
- **Missing functions:** `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **TODOs:** 1

### 165. adapter.implementation

- **Target:** `adapter.Implementation [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62910.0
- **Functions:** 17/23 matched (target 27)
- **Missing functions:** `prepare_dap_adapter`, `fmt`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints`
- **Types:** 6/6 matched (target 10)
- **Missing types:** _none_
- **Lint issues:** 1

### 166. bc.instrs

- **Target:** `bc.Instrs [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62810.0
- **Functions:** 19/24 matched (target 29)
- **Missing functions:** `handle`, `drop`, `opcodes`, `fmt`, `display`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `HandlerImpl`
- **Tests:** 0/2 matched
- **Lint issues:** 1

### 167. namespace.value

- **Target:** `namespace.Value`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 61705.6
- **Functions:** 9/15 matched (target 23)
- **Missing functions:** `new`, `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_kwargs`
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 168. analysis.dubious

- **Target:** `analysis.Dubious`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 61405.2
- **Functions:** 7/12 matched (target 19)
- **Missing functions:** `lint`, `module`, `about`, `test_lint_duplicate_keys`, `test_lint_identifier_as_statement`
- **Types:** 1/2 matched (target 8)
- **Missing types:** `Key`
- **Tests:** 0/4 matched

### 169. profile.csv

- **Target:** `profile.Csv`
- **Similarity:** 0.24
- **Dependents:** 0
- **Priority Score:** 61307.6
- **Functions:** 6/10 matched (target 7)
- **Missing functions:** `new`, `format_for_csv`, `test_csv_writer`, `test_quote_str_for_csv`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Impl`, `CsvValue`
- **Tests:** 0/2 matched

### 170. analysis.types

- **Target:** `analysis.Types`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 61207.0
- **Functions:** 4/7 matched
- **Missing functions:** `fmt`, `new`, `from`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LintWarning`, `LintT`, `EvalSeverity`

### 171. heap.send

- **Target:** `heap.Send [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61110.0
- **Functions:** 2/5 matched (target 6)
- **Missing functions:** `deref`, `deref_mut`, `fmt`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `Sealed`, `Target`, `StaticType`

### 172. list.unpack

- **Target:** `list.Unpack [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61010.0
- **Functions:** 3/5 matched (target 8)
- **Missing functions:** `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 3)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched

### 173. bigint.convert

- **Target:** `bigint.Convert [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61010.0
- **Functions:** 4/8 matched (target 27)
- **Missing functions:** `test_unpack_int_error`, `module`, `takes_i32`, `takes_i64`
- **Types:** 0/2 matched (target 7)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/4 matched

### 174. tuple.rust_tuple

- **Target:** `tuple.RustTuple`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60610.0
- **Functions:** 0/4 matched (target 11)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Canonical`, `Error`

### 175. environment.modules

- **Target:** `environment.Modules [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 54710.0
- **Functions:** 38/43 matched (target 48)
- **Missing functions:** `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 176. params.spec

- **Target:** `params.Spec [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 54410.0
- **Functions:** 33/38 matched (target 34)
- **Missing functions:** `as_value`, `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl`
- **Types:** 6/6 matched (target 11)
- **Missing types:** _none_

### 177. values.owned

- **Target:** `values.Owned [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 53410.0
- **Functions:** 26/29 matched (target 34)
- **Missing functions:** `fmt`, `downcast_starlark`, `deref`
- **Types:** 3/5 matched
- **Missing types:** `Canonical`, `Target`

### 178. profile.time_flame

- **Target:** `profile.TimeFlame`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 53004.0
- **Functions:** 15/19 matched (target 18)
- **Missing functions:** `r#gen`, `test_time_flame_works_inside_frozen_module`, `register_sleep`, `sleep`
- **Types:** 10/11 matched (target 15)
- **Missing types:** `Data`
- **Tests:** 0/3 matched

### 179. profile.stmt

- **Target:** `profile.Stmt [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 52610.0
- **Functions:** 13/17 matched (target 20)
- **Missing functions:** `r#gen`, `test_coverage`, `test_empty`, `test_merge`
- **Types:** 8/9 matched
- **Missing types:** `Data`
- **Tests:** 0/3 matched

### 180. typing.callable_param

- **Target:** `typing.CallableParam`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 52604.4
- **Functions:** 16/20 matched (target 27)
- **Missing functions:** `fmt`, `pf`, `new_named_only`, `test_param_spec_display`
- **Types:** 5/6 matched (target 10)
- **Missing types:** `ParamSpecDisplay`
- **Tests:** 0/1 matched

### 181. values.unpack

- **Target:** `values.Unpack [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51610.0
- **Functions:** 8/9 matched (target 14)
- **Missing functions:** `error`
- **Types:** 3/7 matched
- **Missing types:** `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error`

### 182. dict.refs

- **Target:** `dict.Refs`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 51604.9
- **Functions:** 7/9 matched (target 13)
- **Missing functions:** `from_value`, `deref`
- **Types:** 4/7 matched (target 11)
- **Missing types:** `Target`, `Canonical`, `Error`

### 183. analysis.underscore

- **Target:** `analysis.Underscore`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 51405.6
- **Functions:** 8/13 matched (target 14)
- **Missing functions:** `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 184. allocator.bumpalo

- **Target:** `allocator.Bumpalo [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51110.0
- **Functions:** 6/8 matched (target 6)
- **Missing functions:** `next`, `size_hint`
- **Types:** 0/3 matched (target 1)
- **Missing types:** `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator`

### 185. typing.iter

- **Target:** `typing.Iter [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51010.0
- **Functions:** 3/6 matched (target 5)
- **Missing functions:** `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `NonInstantiable`, `Canonical`
- **Tests:** 0/3 matched

### 186. debug.inspect

- **Target:** `debug.Inspect [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 50910.0
- **Functions:** 4/9 matched (target 4)
- **Missing functions:** `debugger`, `debug_inspect_stack`, `debug_inspect_variables`, `test_debug_stack`, `test_debug_variables`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 187. string.methods

- **Target:** `string.Methods [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 44210.0
- **Functions:** 37/41 matched (target 64)
- **Missing functions:** `test_error_codes`, `test_count`, `test_find`, `test_opaque_iterator`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 188. typing.custom

- **Target:** `typing.Custom [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 43810.0
- **Functions:** 31/35 matched (target 49)
- **Missing functions:** `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 189. heap.repr

- **Target:** `heap.Repr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 43210.0
- **Functions:** 23/27 matched (target 35)
- **Missing functions:** `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut`
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_

### 190. bc.addr

- **Target:** `bc.Addr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 42910.0
- **Functions:** 20/23 matched (target 35)
- **Missing functions:** `add_assign`, `get_instr_mut`, `sub_usize`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Output`

### 191. analysis.incompatible

- **Target:** `analysis.Incompatible`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 41504.2
- **Functions:** 10/14 matched (target 17)
- **Missing functions:** `lint`, `module`, `test_lint_incompatible`, `test_lint_duplicate_top_level_assign`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 192. avalues.static_

- **Target:** `avalues.Static [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41410.0
- **Functions:** 8/9 matched
- **Missing functions:** `test_alloc_static_simple`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`, `MySimpleValue`
- **Tests:** 0/1 matched
- **Lint issues:** 4

### 193. profile.typecheck

- **Target:** `profile.Typecheck [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41310.0
- **Functions:** 5/8 matched (target 6)
- **Missing functions:** `r#gen`, `test_typecheck_profile`, `test_typecheck_profile_merge`
- **Types:** 4/5 matched
- **Missing types:** `Data`
- **Tests:** 0/2 matched

### 194. profile.flamegraph

- **Target:** `profile.Flamegraph`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 41304.1
- **Functions:** 6/10 matched (target 13)
- **Missing functions:** `new`, `test_flamegraph_writer`, `test_flamegraph_data`, `test_merge`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 195. runtime.inlined_frame

- **Target:** `runtime.InlinedFrame`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 41205.0
- **Functions:** 5/9 matched (target 6)
- **Missing functions:** `eq`, `test_inline_into`, `make_span`, `assert_stack`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 196. list.methods

- **Target:** `list.Methods [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41110.0
- **Functions:** 7/11 matched (target 13)
- **Missing functions:** `list_methods`, `test_error_codes`, `test_index`, `recursive_list`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 197. analysis.performance

- **Target:** `analysis.Performance`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 41105.5
- **Functions:** 6/10 matched (target 14)
- **Missing functions:** `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 198. params.parser

- **Target:** `params.Parser [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41010.0
- **Functions:** 5/9 matched (target 5)
- **Missing functions:** `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 199. profile.mode

- **Target:** `profile.Mode`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 40608.5
- **Functions:** 1/4 matched
- **Missing functions:** `fmt`, `name`, `from_str`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Err`

### 200. int.i32

- **Target:** `int.I32`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 40607.7
- **Functions:** 2/4 matched (target 5)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 201. structs.alloc

- **Target:** `structs.Alloc [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/3 matched (target 0)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 202. dict.alloc

- **Target:** `dict.Alloc [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/3 matched (target 1)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 203. set.set

- **Target:** `set.Set [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 1/5 matched (target 1)
- **Missing functions:** `set`, `test_set_type_as_type_compile_time`, `test_return_set_type_as_type_compile_time`, `test_set_type_as_type_run_time`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 204. enumeration.globals

- **Target:** `enumeration.Globals`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 40508.8
- **Functions:** 1/5 matched (target 1)
- **Missing functions:** `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 205. profile.heap

- **Target:** `profile.Heap`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 32402.3
- **Functions:** 11/13 matched (target 27)
- **Missing functions:** `r#gen`, `test_profiling`
- **Types:** 10/11 matched
- **Missing types:** `Data`
- **Tests:** 0/1 matched

### 206. type_compiled.matcher

- **Target:** `type_compiled.Matcher [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31710.0
- **Functions:** 10/10 matched (target 13)
- **Missing functions:** _none_
- **Types:** 4/7 matched
- **Missing types:** `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result`

### 207. list.refs

- **Target:** `list.Refs [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31410.0
- **Functions:** 9/9 matched (target 29)
- **Missing functions:** _none_
- **Types:** 2/5 matched (target 10)
- **Missing types:** `Target`, `Canonical`, `Error`

### 208. avalues.list

- **Target:** `avalues.List`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 31405.2
- **Functions:** 9/10 matched (target 19)
- **Missing functions:** `alloc_list_concat`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Lint issues:** 3

### 209. symbol.map

- **Target:** `symbol.Map [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31310.0
- **Functions:** 9/12 matched (target 11)
- **Missing functions:** `fmt`, `new`, `with_capacity`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 210. bc.opcode

- **Target:** `bc.Opcode [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31210.0
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `opcode_count`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `ByNumber`, `FindOpcode`
- **Tests:** 0/1 matched

### 211. tuple.refs

- **Target:** `tuple.Refs`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 31103.6
- **Functions:** 6/7 matched (target 15)
- **Missing functions:** `unpack_value_impl`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Error`

### 212. enumeration.value

- **Target:** `enumeration.Value`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 31005.4
- **Functions:** 6/9 matched (target 10)
- **Missing functions:** `fmt`, `index`, `value`
- **Types:** 1/1 matched (target 8)
- **Missing types:** _none_

### 213. typing.never

- **Target:** `typing.Never [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30910.0
- **Functions:** 4/6 matched (target 7)
- **Missing functions:** `test_never_runtime`, `test_never_compile_time`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Canonical`
- **Tests:** 0/2 matched

### 214. bc.repr

- **Target:** `bc.Repr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30910.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `size_of_repr`, `handle`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `HandlerImpl`

### 215. string.alloc_unpack

- **Target:** `string.AllocUnpack [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30810.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `unpack_value_impl`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 216. tuple.alloc

- **Target:** `tuple.Alloc`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 30705.1
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `test_alloc_tuple`, `test_alloc_frozen_tuple`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Tests:** 0/2 matched

### 217. values.typing.ty

- **Target:** `kotlin.io.github.kotlinmania.starlark.values.typing.Ty [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30610.0
- **Functions:** 2/5 matched (target 4)
- **Missing functions:** `test_isinstance`, `test_pass`, `test_fail_compile_time`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 218. float.unpack

- **Target:** `float.Unpack`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 30606.7
- **Functions:** 2/3 matched
- **Missing functions:** `test_unpack_float`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/1 matched

### 219. dict.unpack

- **Target:** `dict.Unpack`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 30604.5
- **Functions:** 2/3 matched
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 220. debug.evaluate

- **Target:** `debug.Evaluate [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30410.0
- **Functions:** 1/4 matched (target 1)
- **Missing functions:** `debugger`, `debug_evaluate`, `test_debug_evaluate`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 221. type_compiled.globals

- **Target:** `type_compiled.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30410.0
- **Functions:** 1/4 matched (target 1)
- **Missing functions:** `eval_type`, `isinstance`, `test_typechecking`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 222. compiler.expr

- **Target:** `compiler.Expr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 27010.0
- **Functions:** 59/59 matched (target 63)
- **Missing functions:** _none_
- **Types:** 9/11 matched (target 56)
- **Missing types:** `AstLiteralCompile`, `CompilerExprUtil`

### 223. values.traits

- **Target:** `values.Traits [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 25910.0
- **Functions:** 55/56 matched (target 55)
- **Missing functions:** `please_use_starlark_type_macro`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Canonical`
- **Lint issues:** 5

### 224. compiler.def

- **Target:** `compiler.Def [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 25210.0
- **Functions:** 38/39 matched (target 46)
- **Missing functions:** `fmt`
- **Types:** 12/13 matched (target 18)
- **Missing types:** `Frozen`

### 225. types.function

- **Target:** `types.Function [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 22510.0
- **Functions:** 12/13 matched (target 28)
- **Missing functions:** `new`
- **Types:** 11/12 matched (target 14)
- **Missing types:** `Canonical`

### 226. bc.stack_ptr

- **Target:** `bc.StackPtr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21910.0
- **Functions:** 10/11 matched (target 25)
- **Missing functions:** `add`
- **Types:** 7/8 matched (target 7)
- **Missing types:** `Output`

### 227. profile.summary_by_function

- **Target:** `profile.SummaryByFunction [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21310.0
- **Functions:** 9/10 matched
- **Missing functions:** `drop_non_drop`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `RowKind`
- **Tests:** 0/1 matched

### 228. avalues.array

- **Target:** `avalues.Array [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21310.0
- **Functions:** 9/9 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Lint issues:** 3

### 229. compiler.args

- **Target:** `compiler.Args`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 21304.0
- **Functions:** 10/11 matched
- **Missing functions:** `args`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Never`

### 230. avalues.tuple

- **Target:** `avalues.Tuple [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21210.0
- **Functions:** 8/8 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Lint issues:** 3

### 231. avalues.complex

- **Target:** `avalues.Complex [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21110.0
- **Functions:** 6/6 matched (target 17)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 232. eval.bc.compiler.stmt

- **Target:** `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21010.0
- **Functions:** 8/10 matched (target 11)
- **Missing functions:** `write_if_then`, `write_if_else`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 233. set.refs

- **Target:** `set.Refs [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21010.0
- **Functions:** 5/5 matched (target 19)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 13)
- **Missing types:** `Canonical`, `Error`

### 234. symbol.symbol

- **Target:** `symbol.Symbol`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 21003.7
- **Functions:** 7/9 matched (target 11)
- **Missing functions:** `fmt`, `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 235. bc.instr_arg

- **Target:** `bc.InstrArg [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 42)
- **Missing types:** `HandlerImpl`
- **Lint issues:** 54

### 236. structs.refs

- **Target:** `structs.Refs [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 5/5 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/4 matched
- **Missing types:** `Canonical`, `Error`

### 237. bc.call

- **Target:** `bc.Call [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 3/4 matched (target 15)
- **Missing functions:** `fmt`
- **Types:** 4/5 matched (target 8)
- **Missing types:** `Args`
- **Lint issues:** 2

### 238. profile.data

- **Target:** `profile.Data`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 20904.5
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `_assert_profile_data_send_sync`, `_assert_send_sync`
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_

### 239. typing.callable

- **Target:** `typing.Callable`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 20904.2
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `TyCallableInner`

### 240. bc.bytecode

- **Target:** `bc.Bytecode`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 20904.0
- **Functions:** 6/7 matched (target 11)
- **Missing functions:** `handle`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `HandlerImpl`

### 241. heap.call_enter_exit

- **Target:** `heap.CallEnterExit [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20710.0
- **Functions:** 0/1 matched (target 4)
- **Missing functions:** `drop`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Canonical`

### 242. types.any

- **Target:** `types.Any`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 20702.9
- **Functions:** 4/5 matched
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 243. values.type_repr

- **Target:** `values.TypeRepr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 2/3 matched (target 6)
- **Missing functions:** `test_canonical_for_complex_value`
- **Types:** 2/3 matched (target 6)
- **Missing types:** `Canonical`
- **Tests:** 0/1 matched

### 244. list.globals

- **Target:** `list.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 4/5 matched
- **Missing functions:** `list`
- **Types:** 0/1 matched
- **Missing types:** `ListType`

### 245. values.index

- **Target:** `values.Index [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `test_convert_index`, `test_apply_slice`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 246. dict.traits

- **Target:** `dict.Traits`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 20606.7
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 6)
- **Missing types:** `Canonical`, `Error`

### 247. intern.interner

- **Target:** `intern.Interner [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 1/3 matched (target 5)
- **Missing functions:** `test_intern`, `test_string_value_intern`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 248. funcs.min_max

- **Target:** `funcs.MinMax [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `max`, `min`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 249. typing.any

- **Target:** `typing.Any [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 2/4 matched
- **Missing functions:** `test_any_runtime`, `test_any_compile_time`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 250. bc.definitely_assigned

- **Target:** `bc.DefinitelyAssigned`
- **Similarity:** 0.42
- **Dependents:** 0
- **Priority Score:** 20505.8
- **Functions:** 2/4 matched (target 7)
- **Missing functions:** `new`, `assert_smaller_then`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 251. collections.maybe_uninit_backport

- **Target:** `collections.MaybeUninitBackport [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20410.0
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Guard`

### 252. stdlib.internal

- **Target:** `stdlib.Internal`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 20406.9
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `ty_of_value_debug`, `test_ty_of_value_debug`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 253. enumeration.ty_enum_type

- **Target:** `enumeration.TyEnumType`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20310.0
- **Functions:** 0/2 matched (target 3)
- **Missing functions:** `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 254. heap.maybe_uninit_slice_util

- **Target:** `heap.MaybeUninitSliceUtil`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 20306.6
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `WriteRemOnDrop`

### 255. bool.unpack

- **Target:** `bool.Unpack`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/1 matched
- **Missing functions:** `unpack_value_impl`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Error`

### 256. build

- **Target:** `starlark.Build [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/2 matched (target 0)
- **Missing functions:** `main`, `rust_nightly`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 257. bool.type_repr

- **Target:** `bool.TypeRepr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/1 matched
- **Missing functions:** `starlark_type_repr`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Canonical`

### 258. debug.adapter

- **Target:** `debug.Adapter [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 13610.0
- **Functions:** 21/22 matched (target 23)
- **Missing functions:** `fmt`
- **Types:** 14/14 matched (target 29)
- **Missing types:** _none_

### 259. docs

- **Target:** `docs.Docs [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12310.0
- **Functions:** 12/13 matched (target 16)
- **Missing functions:** `default`
- **Types:** 10/10 matched (target 15)
- **Missing types:** _none_

### 260. typing.basic

- **Target:** `typing.Basic`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 12002.8
- **Functions:** 18/19 matched (target 20)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_

### 261. record.instance

- **Target:** `record.Instance [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11410.0
- **Functions:** 12/13 matched (target 18)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 262. compiler.def_inline

- **Target:** `compiler.DefInline`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 11403.0
- **Functions:** 9/10 matched (target 9)
- **Missing functions:** `new`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_

### 263. type_compiled.factory

- **Target:** `type_compiled.Factory`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 11100.7
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Result`

### 264. bool.value

- **Target:** `bool.Value`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 11005.1
- **Functions:** 8/9 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 265. namespace.typing

- **Target:** `namespace.Typing`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 11003.5
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 3/3 matched
- **Missing types:** _none_

### 266. alloc.per_thread

- **Target:** `alloc.PerThread [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10710.0
- **Functions:** 5/6 matched (target 5)
- **Missing functions:** `test_release_partial`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 267. profile.by_type

- **Target:** `profile.ByType [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10710.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `normalize_for_golden_tests`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 268. values.recursive_repr_or_json_guard

- **Target:** `values.RecursiveReprOrJsonGuard [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10710.0
- **Functions:** 2/3 matched (target 5)
- **Missing functions:** `drop`
- **Types:** 4/4 matched
- **Missing types:** _none_

### 269. compiler.if_compiler

- **Target:** `compiler.IfCompiler [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10610.0
- **Functions:** 5/6 matched (target 5)
- **Missing functions:** `wr`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 270. eval.bc.compiler.call

- **Target:** `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10510.0
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `mark_definitely_assigned_after`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

### 271. structs.structs

- **Target:** `structs.Structs [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10510.0
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `r#struct`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 272. types.unbound

- **Target:** `types.Unbound`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 10504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 273. analysis.find_call_name

- **Target:** `analysis.FindCallName`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 10404.5
- **Functions:** 2/3 matched (target 8)
- **Missing functions:** `finds_function_calls_with_name_kwarg`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 274. dict.globals

- **Target:** `dict.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10310.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `dict`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 275. compiler.assign_modify

- **Target:** `compiler.AssignModify`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 10301.2
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `AssignOnWriteBc`

### 276. runtime.visit_span

- **Target:** `runtime.VisitSpan`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 19)
- **Missing functions:** `visit_spans`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 277. pagable.error

- **Target:** `pagable.Error`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched
- **Missing functions:** `from`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 278. int.globals

- **Target:** `int.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched
- **Missing functions:** `int`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 279. float.globals

- **Target:** `float.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `float`
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_

### 280. bool.globals

- **Target:** `bool.Globals`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 10208.1
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `bool`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 281. range.globals

- **Target:** `range.Globals`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 10207.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `range`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 282. namespace.globals

- **Target:** `namespace.Globals`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 10207.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `namespace`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 283. tuple.globals

- **Target:** `tuple.Globals`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 10206.9
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `tuple`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 284. num.globals

- **Target:** `num.Globals`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 10206.8
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `abs`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 285. bc.writer

- **Target:** `bc.Writer [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 4610.0
- **Functions:** 42/42 matched (target 44)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_

### 286. typing.fill_types_for_lint

- **Target:** `typing.FillTypesForLint [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 4210.0
- **Functions:** 39/39 matched (target 40)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 287. oracle.ctx

- **Target:** `oracle.Ctx`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 3402.1
- **Functions:** 32/32 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 14)
- **Missing types:** _none_

### 288. type_compiled.alloc

- **Target:** `type_compiled.Alloc`
- **Similarity:** 0.87
- **Dependents:** 0
- **Priority Score:** 2901.3
- **Functions:** 28/28 matched (target 37)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 289. type_compiled.matchers

- **Target:** `type_compiled.Matchers [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2610.0
- **Functions:** 3/3 matched (target 25)
- **Missing functions:** _none_
- **Types:** 23/23 matched
- **Missing types:** _none_

### 290. docs.markdown

- **Target:** `docs.Markdown [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2010.0
- **Functions:** 18/18 matched (target 19)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 291. typing.ctx

- **Target:** `typing.Ctx`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 2002.4
- **Functions:** 19/19 matched (target 20)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 292. eval.bc.compiler.expr

- **Target:** `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1510.0
- **Functions:** 15/15 matched (target 16)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 293. environment.names

- **Target:** `environment.Names [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1510.0
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 294. typing.error

- **Target:** `typing.Error [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 9/9 matched (target 25)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_

### 295. compiler.call

- **Target:** `compiler.Call`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 1403.3
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 296. compiler.compr

- **Target:** `compiler.Compr`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 1202.5
- **Functions:** 9/9 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 297. environment.slots

- **Target:** `environment.Slots`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 1103.3
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 298. docs.multipage

- **Target:** `docs.Multipage`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 1101.4
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_

### 299. compiler.types

- **Target:** `compiler.Types [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 910.0
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_

### 300. __derive_refs.parse_args

- **Target:** `deriverefs.ParseArgs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 802.8
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/derive_refs/parse_args.rs` vs expected `__derive_refs/parse_args.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/parse_args.rs` (current: `// port-lint: source src/derive_refs/parse_args.rs`)
- **Lint issues:** 1

### 301. docs.code

- **Target:** `docs.Code [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 7/7 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 302. layout.value_not_special

- **Target:** `layout.ValueNotSpecial`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 702.8
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 303. unused_loads.find

- **Target:** `unusedloads.Find`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 702.1
- **Functions:** 4/4 matched (target 8)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 304. types.known_methods

- **Target:** `types.KnownMethods`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 701.7
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 305. runtime.before_stmt

- **Target:** `runtime.BeforeStmt`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 701.4
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 306. compiler.module

- **Target:** `compiler.Module`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 701.4
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_

### 307. layout.static_string

- **Target:** `layout.StaticString [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 308. assert.conformance

- **Target:** `assert.Conformance`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 602.6
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 309. string.globals

- **Target:** `string.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 310. unused_loads.remove

- **Target:** `unusedloads.Remove [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 311. runtime.slots

- **Target:** `runtime.Slots`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 502.4
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 312. values.comparison

- **Target:** `values.Comparison`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 502.1
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 313. compiler.expr_bool

- **Target:** `compiler.ExprBool`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 502.1
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 314. funcs.zip

- **Target:** `funcs.Zip`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 502.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 315. num.typecheck

- **Target:** `num.Typecheck`
- **Similarity:** 0.90
- **Dependents:** 0
- **Priority Score:** 501.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 316. string.iter

- **Target:** `string.Iter [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 317. __derive_refs.components

- **Target:** `deriverefs.Components [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/derive_refs/components.rs` vs expected `__derive_refs/components.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/components.rs` (current: `// port-lint: source src/derive_refs/components.rs`)
- **Lint issues:** 1

### 318. __derive_refs.sig

- **Target:** `deriverefs.Sig [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 401.4
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/derive_refs/sig.rs` vs expected `__derive_refs/sig.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/sig.rs` (current: `// port-lint: source src/derive_refs/sig.rs`)
- **Lint issues:** 1

### 319. compiler.error

- **Target:** `compiler.Error [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 2/2 matched (target 24)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 14)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_syntax/src/error.rs` vs expected `eval/compiler/error.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source starlark_syntax/src/error.rs`)
- **Lint issues:** 1

### 320. oracle.traits

- **Target:** `oracle.Traits`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 304.0
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 321. callable.param

- **Target:** `callable.Param`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 302.4
- **Functions:** 1/1 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 7)
- **Missing types:** _none_

### 322. eval.soft_error

- **Target:** `eval.SoftError`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 301.6
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 323. compiler.type_expr

- **Target:** `compiler.TypeExpr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 301.1
- **Functions:** 2/2 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 17)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_syntax/src/syntax/type_expr.rs` vs expected `eval/compiler/type_expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source starlark_syntax/src/syntax/type_expr.rs`)
- **Lint issues:** 1

### 324. fuzz_targets.starlark

- **Target:** `fuzztargets.Starlark [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `fuzz/fuzztargets/starlark.rs` vs expected `fuzz/fuzz_targets/starlark.rs`
- **Proposed provenance header:** `// port-lint: source fuzz/fuzz_targets/starlark.rs` (current: `// port-lint: source fuzz/fuzztargets/starlark.rs`)
- **Lint issues:** 1

### 325. compiler.assign

- **Target:** `compiler.Assign [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 326. eval.bc.compiler.def

- **Target:** `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 327. __derive_refs.invoke_macro_error

- **Target:** `deriverefs.InvokeMacroError [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 206.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/derive_refs/invoke_macro_error.rs` vs expected `__derive_refs/invoke_macro_error.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/invoke_macro_error.rs` (current: `// port-lint: source src/derive_refs/invoke_macro_error.rs`)
- **Lint issues:** 1

### 328. typing.macro_refs

- **Target:** `typing.MacroRefs`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 202.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 329. enumeration.matcher

- **Target:** `enumeration.Matcher`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 201.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 330. eval

- **Target:** `eval.Eval`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 201.6
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 331. typing.macro_support

- **Target:** `typing.MacroSupport`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 201.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 332. layout.identity

- **Target:** `layout.Identity`
- **Similarity:** 0.87
- **Dependents:** 0
- **Priority Score:** 201.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 333. record.matcher

- **Target:** `record.Matcher`
- **Similarity:** 0.92
- **Dependents:** 0
- **Priority Score:** 200.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 334. eval.bc.compiler.compr

- **Target:** `kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 200.7
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 335. bool.alloc

- **Target:** `bool.Alloc`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 200.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 336. bc.slow_arg

- **Target:** `bc.SlowArg`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 337. allocator.api

- **Target:** `allocator.Api`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 338. bc.instr

- **Target:** `bc.Instr`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_

### 339. environment

- **Target:** `starlark.Environment [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_

### 340. bc.for_loop

- **Target:** `bc.ForLoop [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 341. wasm

- **Target:** `starlark.Wasm`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 107.5
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 342. environment.module_dump

- **Target:** `environment.ModuleDump`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 105.2
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 343. none.globals

- **Target:** `none.Globals`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 102.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 344. typing.globals

- **Target:** `typing.Globals`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 102.6
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 345. compiler.known

- **Target:** `compiler.Known`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 102.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 346. eval.params

- **Target:** `eval.Params`
- **Similarity:** 0.91
- **Dependents:** 0
- **Priority Score:** 100.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 347. funcs.globals

- **Target:** `funcs.Globals`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 348. profile.or_instrumentation

- **Target:** `profile.OrInstrumentation`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_

### 349. typing.call_args

- **Target:** `typing.CallArgs`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 350. typing.mode

- **Target:** `typing.Mode`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 351. syntax

- **Target:** `starlark.Syntax [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 352. values.types

- **Target:** `values.Types [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 353. pagable

- **Target:** `starlark.Pagable [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 354. values.typing

- **Target:** `values.Typing [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 355. typing.oracle

- **Target:** `typing.Oracle [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 356. util

- **Target:** `starlark.Util [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 357. layout.avalues

- **Target:** `layout.AValues [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 358. errors

- **Target:** `starlark.Errors [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 359. macros

- **Target:** `starlark.Macros [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 9)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 9)
- **Missing types:** _none_

### 360. stdlib.funcs

- **Target:** `stdlib.Funcs [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 361. __derive_refs

- **Target:** `deriverefs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/derive_refs.rs` vs expected `__derive_refs.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs.rs` (current: `// port-lint: source src/derive_refs.rs`)
- **Lint issues:** 1

### 362. pagable.vtable_register

- **Target:** `pagable.VtableRegister [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 363. eval.runtime

- **Target:** `eval.Runtime [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 364. values

- **Target:** `values.Values`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 10)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

