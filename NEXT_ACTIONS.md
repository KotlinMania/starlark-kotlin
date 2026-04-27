# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 88.9% (495/468 files)
- **Matched Files:** 416
- **Average Similarity:** 0.35
- **Critical Issues:** 284 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.75 (needs 10% improvement)
- **Dependencies:** 178
- **Priority Score:** 178132704.0
- **Functions:** 108/118 matched (target 157)
- **Missing functions:** `eq`, `testing_new_int`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function`
- **Types:** 7/9 matched (target 10)
- **Missing types:** `Canonical`, `String`
- **Symbol Deficit:** 12 (functions: 10, types: 2)
- **Missing Tests:** 9 of 9 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 2. typing.ty
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 109
- **Priority Score:** 109015408.0
- **Functions:** 49/50 matched (target 56)
- **Missing functions:** `fmt`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 3. typing.starlark_value
- **Similarity:** 0.63 (needs 22% improvement)
- **Dependencies:** 76
- **Priority Score:** 76063800.0
- **Functions:** 28/34 matched (target 41)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `is_iterable`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Symbol Deficit:** 6 (functions: 6, types: 0)
- **Action:** Review and complete missing sections

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
- **Dependencies:** 55
- **Priority Score:** 55016712.0
- **Functions:** 59/60 matched (target 63)
- **Missing functions:** `drop`
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 6. values.trace
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 52
- **Priority Score:** 52000204.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 7. derive.unpack_value
- **Similarity:** 0.71 (needs 14% improvement)
- **Dependencies:** 51
- **Priority Score:** 51000704.0
- **Functions:** 2/2 matched (target 12)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Action:** Review and complete missing sections

### 8. values.freeze
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42010312.0
- **Functions:** 1/1 matched (target 26)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Frozen`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 9. values.alloc_value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42000608.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 10. layout.freezer
- **Similarity:** 0.76 (needs 9% improvement)
- **Dependencies:** 36
- **Priority Score:** 36000604.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 11. coerce
- **Similarity:** 0.63 (needs 22% improvement)
- **Dependencies:** 34
- **Priority Score:** 34031404.0
- **Functions:** 4/5 matched
- **Missing functions:** `f`
- **Types:** 7/9 matched (target 10)
- **Missing types:** `Trait`, `Assoc`
- **Symbol Deficit:** 3 (functions: 1, types: 2)
- **Missing Tests:** 1 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 12. values.frozen_ref
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 27
- **Priority Score:** 27052110.0
- **Functions:** 14/17 matched (target 20)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `Target`, `Frozen`
- **Symbol Deficit:** 5 (functions: 3, types: 2)
- **Action:** Deep review - likely missing major functionality

### 13. none.none_type
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 27
- **Priority Score:** 27011302.0
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Minor refinements needed

### 14. runtime.frame_span
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 15. runtime.arguments
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 25
- **Priority Score:** 25053810.0
- **Functions:** 25/30 matched (target 39)
- **Missing functions:** `from`, `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names`
- **Types:** 8/8 matched (target 15)
- **Missing types:** _none_
- **Symbol Deficit:** 5 (functions: 5, types: 0)
- **Missing Tests:** 4 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 16. util.arc_str
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 21
- **Priority Score:** 21010704.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Review and complete missing sections

### 17. values.value_of_unchecked
- **Similarity:** 0.44 (needs 41% improvement)
- **Dependencies:** 20
- **Priority Score:** 20102506.0
- **Functions:** 12/18 matched (target 22)
- **Missing functions:** `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant`
- **Types:** 3/7 matched (target 4)
- **Missing types:** `Canonical`, `Frozen`, `Error`, `ReprNotSendSync`
- **Symbol Deficit:** 10 (functions: 6, types: 4)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 18. environment.globals
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20074010.0
- **Functions:** 29/35 matched
- **Missing functions:** `empty`, `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden`
- **Types:** 4/5 matched (target 4)
- **Missing types:** `GlobalValue`
- **Symbol Deficit:** 7 (functions: 6, types: 1)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 19. util.refcell
- **Similarity:** 0.32 (needs 53% improvement)
- **Dependencies:** 20
- **Priority Score:** 20010206.0
- **Functions:** 1/2 matched (target 10)
- **Missing functions:** `test_unleak_borrow`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 1 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 20. __derive_refs.param_spec
- **Similarity:** 0.83 (needs 2% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 21. environment.methods
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 17
- **Priority Score:** 17032310.0
- **Functions:** 17/19 matched (target 20)
- **Missing functions:** `test_set_attribute`, `get_methods`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `Magic`
- **Symbol Deficit:** 3 (functions: 2, types: 1)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 22. values.iter
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 17
- **Priority Score:** 17020704.0
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Item`
- **Symbol Deficit:** 2 (functions: 1, types: 1)
- **Action:** Review and complete missing sections

### 23. values.error
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 24. private
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 15
- **Priority Score:** 15000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 25. layout.avalue
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 14
- **Priority Score:** 14021110.0
- **Functions:** 6/8 matched (target 12)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 26. typing.tuple
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 12
- **Priority Score:** 12010704.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 27. layout.const_frozen_string
- **Similarity:** 0.79 (needs 6% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000202.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 28. layout.value_lifetimeless
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 29. int.inline_int
- **Similarity:** 0.42 (needs 43% improvement)
- **Dependencies:** 11
- **Priority Score:** 11123906.0
- **Functions:** 25/34 matched (target 39)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Symbol Deficit:** 12 (functions: 9, types: 3)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. layout.value

- **Target:** `layout.Value`
- **Similarity:** 0.75
- **Dependents:** 178
- **Priority Score:** 178132704.0
- **Functions:** 108/118 matched (target 157)
- **Missing functions:** `eq`, `testing_new_int`, `test_downcast_ref`, `test_unpack_i32`, `test_unpack_frozen`, `test_unpack_bigint`, `test_to_json_value`, `test_display_for_type_error`, `test_check_callable_with_none`, `test_check_callable_with_good_function`
- **Types:** 7/9 matched (target 10)
- **Missing types:** `Canonical`, `String`
- **Tests:** 0/9 matched
- **Lint issues:** 1

### 2. typing.ty

- **Target:** `typing.Ty [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 109
- **Priority Score:** 109015408.0
- **Functions:** 49/50 matched (target 56)
- **Missing functions:** `fmt`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/ty.rs` vs expected `typing/ty.rs`
- **Proposed provenance header:** `// port-lint: source typing/ty.rs` (current: `// port-lint: source src/typing/ty.rs`)
- **Lint issues:** 1

### 3. typing.starlark_value

- **Target:** `typing.StarlarkValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 76
- **Priority Score:** 76063800.0
- **Functions:** 28/34 matched (target 41)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `is_iterable`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/starlarkValue.rs` vs expected `typing/starlark_value.rs`
- **Proposed provenance header:** `// port-lint: source typing/starlark_value.rs` (current: `// port-lint: source src/typing/starlarkValue.rs`)
- **Lint issues:** 4

### 4. params.display

- **Target:** `params.Display [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 76
- **Priority Score:** 76000704.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/display.rs` vs expected `eval/runtime/params/display.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/display.rs` (current: `// port-lint: source src/eval/runtime/params/display.rs`)
- **Lint issues:** 1

### 5. runtime.evaluator

- **Target:** `runtime.Evaluator [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 55
- **Priority Score:** 55016712.0
- **Functions:** 59/60 matched (target 63)
- **Missing functions:** `drop`
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/evaluator.rs` vs expected `eval/runtime/evaluator.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/evaluator.rs` (current: `// port-lint: source src/eval/runtime/evaluator.rs`)
- **Lint issues:** 1

### 6. values.trace

- **Target:** `values.Trace [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 52
- **Priority Score:** 52000204.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/trace.rs` vs expected `values/trace.rs`
- **Proposed provenance header:** `// port-lint: source values/trace.rs` (current: `// port-lint: source src/values/trace.rs`)
- **Lint issues:** 2

### 7. derive.unpack_value

- **Target:** `derive.UnpackValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 51
- **Priority Score:** 51000704.0
- **Functions:** 2/2 matched (target 12)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/unpackValue.rs` vs expected `tests/derive/unpack_value.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/unpack_value.rs` (current: `// port-lint: source src/tests/derive/unpackValue.rs`)
- **Lint issues:** 1

### 8. values.freeze

- **Target:** `values.Freeze [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 42
- **Priority Score:** 42010312.0
- **Functions:** 1/1 matched (target 26)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/freeze.rs` vs expected `values/freeze.rs`
- **Proposed provenance header:** `// port-lint: source values/freeze.rs` (current: `// port-lint: source src/values/freeze.rs`)
- **Lint issues:** 1

### 9. values.alloc_value

- **Target:** `values.AllocValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 42
- **Priority Score:** 42000608.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/allocValue.rs` vs expected `values/alloc_value.rs`
- **Proposed provenance header:** `// port-lint: source values/alloc_value.rs` (current: `// port-lint: source src/values/allocValue.rs`)
- **Lint issues:** 1

### 10. layout.freezer

- **Target:** `layout.Freezer [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 36
- **Priority Score:** 36000604.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/freezer.rs` vs expected `values/layout/freezer.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/freezer.rs` (current: `// port-lint: source src/values/layout/freezer.rs`)
- **Lint issues:** 1

### 11. coerce

- **Target:** `starlark.Coerce [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 34
- **Priority Score:** 34031404.0
- **Functions:** 4/5 matched
- **Missing functions:** `f`
- **Types:** 7/9 matched (target 10)
- **Missing types:** `Trait`, `Assoc`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/coerce.rs` vs expected `coerce.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/coerce.rs` vs expected `coerce.rs`
- **Proposed provenance header:** `// port-lint: source coerce.rs` (current: `// port-lint: source src/coerce.rs`)
- **Proposed provenance header:** `// port-lint: tests coerce.rs` (current: `// port-lint: tests src/coerce.rs`)
- **Lint issues:** 2

### 12. compiler.span

- **Target:** `compiler.Span [PROVENANCE-FALLBACK]`
- **Similarity:** 0.92
- **Dependents:** 29
- **Priority Score:** 29010400.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/span.rs` vs expected `eval/compiler/span.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/span.rs` (current: `// port-lint: source src/eval/compiler/span.rs`)
- **Lint issues:** 1

### 13. values.frozen_ref

- **Target:** `values.FrozenRef [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 27
- **Priority Score:** 27052110.0
- **Functions:** 14/17 matched (target 20)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `Target`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/frozenRef.rs` vs expected `values/frozen_ref.rs`
- **Proposed provenance header:** `// port-lint: source values/frozen_ref.rs` (current: `// port-lint: source src/values/frozenRef.rs`)
- **Lint issues:** 1

### 14. none.none_type

- **Target:** `none.NoneType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.82
- **Dependents:** 27
- **Priority Score:** 27011302.0
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none/noneType.rs` vs expected `values/types/none/none_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none/none_type.rs` (current: `// port-lint: source src/values/types/none/noneType.rs`)
- **Lint issues:** 4

### 15. runtime.frame_span

- **Target:** `runtime.FrameSpan [PROVENANCE-FALLBACK]`
- **Similarity:** 0.65
- **Dependents:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/frameSpan.rs` vs expected `eval/runtime/frame_span.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/frame_span.rs` (current: `// port-lint: source src/eval/runtime/frameSpan.rs`)
- **Lint issues:** 1

### 16. runtime.arguments

- **Target:** `runtime.Arguments [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 25
- **Priority Score:** 25053810.0
- **Functions:** 25/30 matched (target 39)
- **Missing functions:** `from`, `test_parameter_unpack`, `f`, `test_parameter_no_named`, `test_names_map_repeated_name_in_arg_names`
- **Types:** 8/8 matched (target 15)
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/arguments.rs` vs expected `eval/runtime/arguments.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/arguments.rs` (current: `// port-lint: source src/eval/runtime/arguments.rs`)
- **Lint issues:** 1

### 17. util.arc_str

- **Target:** `util.ArcStr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 21
- **Priority Score:** 21010704.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/arcStr.rs` vs expected `util/arc_str.rs`
- **Proposed provenance header:** `// port-lint: source util/arc_str.rs` (current: `// port-lint: source src/util/arcStr.rs`)
- **Lint issues:** 1

### 18. values.value_of_unchecked

- **Target:** `values.ValueOfUnchecked [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 20
- **Priority Score:** 20102506.0
- **Functions:** 12/18 matched (target 22)
- **Missing functions:** `fmt`, `test_cast_example`, `test_frozen_value_of_unchecked_send_sync`, `assert_send_sync`, `test_frozen_value_of_unchecked_covariant`, `_assert_covariant`
- **Types:** 3/7 matched (target 4)
- **Missing types:** `Canonical`, `Frozen`, `Error`, `ReprNotSendSync`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/valueOfUnchecked.rs` vs expected `values/value_of_unchecked.rs`
- **Proposed provenance header:** `// port-lint: source values/value_of_unchecked.rs` (current: `// port-lint: source src/values/valueOfUnchecked.rs`)
- **Lint issues:** 3

### 19. environment.globals

- **Target:** `environment.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20074010.0
- **Functions:** 29/35 matched
- **Missing functions:** `empty`, `get`, `test_send_sync`, `register_foo`, `foo`, `test_doc_hidden`
- **Types:** 4/5 matched (target 4)
- **Missing types:** `GlobalValue`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/globals.rs` vs expected `environment/globals.rs`
- **Proposed provenance header:** `// port-lint: source environment/globals.rs` (current: `// port-lint: source src/environment/globals.rs`)
- **Lint issues:** 1

### 20. util.refcell

- **Target:** `refcell.RefCell [PROVENANCE-FALLBACK]`
- **Similarity:** 0.32
- **Dependents:** 20
- **Priority Score:** 20010206.0
- **Functions:** 1/2 matched (target 10)
- **Missing functions:** `test_unleak_borrow`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/refcell.rs` vs expected `util/refcell.rs`
- **Proposed provenance header:** `// port-lint: source util/refcell.rs` (current: `// port-lint: source src/util/refcell.rs`)
- **Lint issues:** 1

### 21. __derive_refs.param_spec

- **Target:** `deriverefs.ParamSpec [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/paramSpec.rs` vs expected `__derive_refs/param_spec.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/param_spec.rs` (current: `// port-lint: source src/__derive_refs/paramSpec.rs`)
- **Lint issues:** 1

### 22. environment.methods

- **Target:** `environment.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 17
- **Priority Score:** 17032310.0
- **Functions:** 17/19 matched (target 20)
- **Missing functions:** `test_set_attribute`, `get_methods`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `Magic`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/methods.rs` vs expected `environment/methods.rs`
- **Proposed provenance header:** `// port-lint: source environment/methods.rs` (current: `// port-lint: source src/environment/methods.rs`)
- **Lint issues:** 2

### 23. values.iter

- **Target:** `values.Iter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 17
- **Priority Score:** 17020704.0
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/iter.rs` vs expected `values/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/values/iter.rs`)
- **Lint issues:** 1

### 24. values.error

- **Target:** `values.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/error.rs` vs expected `values/error.rs`
- **Proposed provenance header:** `// port-lint: source values/error.rs` (current: `// port-lint: source src/values/error.rs`)
- **Lint issues:** 1

### 25. private

- **Target:** `starlark.Private [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 15
- **Priority Score:** 15000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/private.rs` vs expected `private.rs`
- **Proposed provenance header:** `// port-lint: source private.rs` (current: `// port-lint: source src/private.rs`)
- **Lint issues:** 1

### 26. layout.avalue

- **Target:** `layout.AValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 14
- **Priority Score:** 14021110.0
- **Functions:** 6/8 matched (target 12)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalue.rs` vs expected `values/layout/avalue.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalue.rs` (current: `// port-lint: source src/values/layout/avalue.rs`)
- **Lint issues:** 1

### 27. typing.tuple

- **Target:** `typing.Tuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 12
- **Priority Score:** 12010704.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tuple.rs` vs expected `typing/tuple.rs`
- **Proposed provenance header:** `// port-lint: source typing/tuple.rs` (current: `// port-lint: source src/typing/tuple.rs`)
- **Lint issues:** 1

### 28. layout.const_frozen_string

- **Target:** `layout.ConstFrozenString [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 12
- **Priority Score:** 12000202.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/constFrozenString.rs` vs expected `values/layout/const_frozen_string.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/constFrozenString.rs` vs expected `values/layout/const_frozen_string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/const_frozen_string.rs` (current: `// port-lint: source src/values/layout/constFrozenString.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/const_frozen_string.rs` (current: `// port-lint: tests src/values/layout/constFrozenString.rs`)
- **Lint issues:** 2

### 29. layout.value_lifetimeless

- **Target:** `layout.ValueLifetimeless [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/valueLifetimeless.rs` vs expected `values/layout/value_lifetimeless.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_lifetimeless.rs` (current: `// port-lint: source src/values/layout/valueLifetimeless.rs`)
- **Lint issues:** 1

### 30. int.inline_int

- **Target:** `int.InlineInt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 11
- **Priority Score:** 11123906.0
- **Functions:** 25/34 matched (target 39)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/inlineInt.rs` vs expected `values/types/int/inline_int.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/inline_int.rs` (current: `// port-lint: source src/values/types/int/inlineInt.rs`)
- **Lint issues:** 3

### 31. int.pointer_i32

- **Target:** `int.PointerI32 [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9043310.0
- **Functions:** 28/31 matched (target 33)
- **Missing functions:** `eq`, `fmt`, `serialize`
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/pointerI32.rs` vs expected `values/types/int/pointer_i32.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/pointer_i32.rs` (current: `// port-lint: source src/values/types/int/pointerI32.rs`)
- **Lint issues:** 1

### 32. types.type_instance_id

- **Target:** `types.TypeInstanceId [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9010210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `r#gen`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/typeInstanceId.rs` vs expected `values/types/type_instance_id.rs`
- **Proposed provenance header:** `// port-lint: source values/types/type_instance_id.rs` (current: `// port-lint: source src/values/types/typeInstanceId.rs`)
- **Lint issues:** 1

### 33. any

- **Target:** `starlark.Any [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8062710.0
- **Functions:** 8/12 matched (target 15)
- **Missing functions:** `is`, `convert_value`, `convert_any`, `test`
- **Types:** 13/15 matched (target 49)
- **Missing types:** `StaticType`, `My`
- **Tests:** 4/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/any.rs` vs expected `any.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/any.rs` vs expected `any.rs`
- **Proposed provenance header:** `// port-lint: source any.rs` (current: `// port-lint: source src/any.rs`)
- **Proposed provenance header:** `// port-lint: source any.rs` (current: `// port-lint: source src/any.rs`)
- **Lint issues:** 2

### 34. layout.aligned_size

- **Target:** `layout.AlignedSize [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 8
- **Priority Score:** 8061505.5
- **Functions:** 8/13 matched (target 17)
- **Missing functions:** `layout`, `ptr_diff`, `add`, `sub`, `mul`
- **Types:** 1/2 matched
- **Missing types:** `Output`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/alignedSize.rs` vs expected `values/layout/aligned_size.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/alignedSize.rs` vs expected `values/layout/aligned_size.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/aligned_size.rs` (current: `// port-lint: source src/values/layout/alignedSize.rs`)
- **Proposed provenance header:** `// port-lint: source values/layout/aligned_size.rs` (current: `// port-lint: source src/values/layout/alignedSize.rs`)
- **Lint issues:** 2

### 35. eval.compiler

- **Target:** `eval.Compiler [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 8
- **Priority Score:** 8000702.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler.rs` vs expected `eval/compiler.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler.rs` (current: `// port-lint: source src/eval/compiler.rs`)
- **Lint issues:** 1

### 36. cast

- **Target:** `starlark.Cast [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8000310.0
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/cast.rs` vs expected `cast.rs`
- **Proposed provenance header:** `// port-lint: source cast.rs` (current: `// port-lint: source src/cast.rs`)
- **Lint issues:** 1

### 37. types.bigint

- **Target:** `types.Bigint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 7
- **Priority Score:** 7447407.0
- **Functions:** 29/73 matched (target 35)
- **Missing functions:** `unpack_integer`, `eq`, `test_parse`, `test_str`, `test_repr`, `test_equals`, `test_plus`, `test_compare_big_big`, `test_compare_big_small`, `test_compare_big_float`, `test_add_big`, `test_add_big_small`, `test_add_big_float`, `test_mul_big`, `test_mul_big_small`, `test_mul_big_float`, `test_div_big`, `test_div_big_small`, `test_div_big_float`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_big_float`, `test_percent_big`, `test_percent_big_small`, `test_percent_big_float`, `test_bit_and_big`, `test_bit_and_big_small`, `test_bit_and_float`, `test_bit_or_big`, `test_bit_or_big_small`, `test_bit_or_float`, `test_bit_xor_big`, `test_bit_xor_big_small`, `test_bit_xor_float`, `test_bit_not`, `test_left_shift`, `test_left_shift_small`, `test_left_shift_float`, `test_right_shift`, `test_right_shift_small`, `test_right_shift_float`, `test_int_function`, `test_hash`, `test_int_type_matches_bigint`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/42 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bigint.rs` vs expected `values/types/bigint.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bigint.rs` (current: `// port-lint: source src/values/types/bigint.rs`)
- **Lint issues:** 2

### 38. runtime.frozen_file_span

- **Target:** `runtime.FrozenFileSpan [PROVENANCE-FALLBACK]`
- **Similarity:** 0.78
- **Dependents:** 7
- **Priority Score:** 7011102.0
- **Functions:** 9/10 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/frozenFileSpan.rs` vs expected `eval/runtime/frozen_file_span.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/frozen_file_span.rs` (current: `// port-lint: source src/eval/runtime/frozenFileSpan.rs`)
- **Lint issues:** 1

### 39. values.starlark_type_id

- **Target:** `values.StarlarkTypeId [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 7
- **Priority Score:** 7010804.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `eq`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/starlarkTypeId.rs` vs expected `values/starlark_type_id.rs`
- **Proposed provenance header:** `// port-lint: source values/starlark_type_id.rs` (current: `// port-lint: source src/values/starlarkTypeId.rs`)
- **Lint issues:** 1

### 40. compiler.opt_ctx

- **Target:** `compiler.OptCtx [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 7
- **Priority Score:** 7000703.0
- **Functions:** 5/5 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/optCtx.rs` vs expected `eval/compiler/opt_ctx.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/opt_ctx.rs` (current: `// port-lint: source src/eval/compiler/optCtx.rs`)
- **Lint issues:** 1

### 41. bc.expr

- **Target:** `bc.Expr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.98
- **Dependents:** 7
- **Priority Score:** 7000700.0
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/expr.rs` vs expected `tests/bc/expr.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/expr.rs` (current: `// port-lint: source src/tests/bc/expr.rs`)
- **Lint issues:** 1

### 42. type_compiled.type_matcher_factory

- **Target:** `typecompiled.TypeMatcherFactory [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 7
- **Priority Score:** 7000603.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/typeMatcherFactory.rs` vs expected `values/typing/type_compiled/type_matcher_factory.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/type_matcher_factory.rs` (current: `// port-lint: source src/values/typing/typeCompiled/typeMatcherFactory.rs`)
- **Lint issues:** 1

### 43. runtime.small_duration

- **Target:** `runtime.SmallDuration [PROVENANCE-FALLBACK]`
- **Similarity:** 0.38
- **Dependents:** 6
- **Priority Score:** 6040906.0
- **Functions:** 4/7 matched (target 11)
- **Missing functions:** `from_millis`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/smallDuration.rs` vs expected `eval/runtime/small_duration.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/small_duration.rs` (current: `// port-lint: source src/eval/runtime/smallDuration.rs`)
- **Lint issues:** 1

### 44. typing.typecheck

- **Target:** `typing.Typecheck [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6030710.0
- **Functions:** 2/5 matched
- **Missing functions:** `fmt`, `find_bindings_by_name`, `find_first_binding`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/typecheck.rs` vs expected `typing/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source typing/typecheck.rs` (current: `// port-lint: source src/typing/typecheck.rs`)
- **Lint issues:** 1

### 45. none.none_or

- **Target:** `none.NoneOr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 6
- **Priority Score:** 6021002.5
- **Functions:** 7/7 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 4)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none/noneOr.rs` vs expected `values/types/none/none_or.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none/none_or.rs` (current: `// port-lint: source src/values/types/none/noneOr.rs`)
- **Lint issues:** 1

### 46. values.freeze_error

- **Target:** `values.FreezeError [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 6
- **Priority Score:** 6020806.0
- **Functions:** 3/4 matched (target 6)
- **Missing functions:** `from`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `FreezeResult`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/freezeError.rs` vs expected `values/freeze_error.rs`
- **Proposed provenance header:** `// port-lint: source values/freeze_error.rs` (current: `// port-lint: source src/values/freezeError.rs`)
- **Lint issues:** 1

### 47. dict.dict_type

- **Target:** `dict.DictType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 6
- **Priority Score:** 6020503.5
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/dictType.rs` vs expected `values/types/dict/dict_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/dict_type.rs` (current: `// port-lint: source src/values/types/dict/dictType.rs`)
- **Lint issues:** 1

### 48. layout.value_alloc_size

- **Target:** `layout.ValueAllocSize [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6010610.0
- **Functions:** 4/5 matched
- **Missing functions:** `layout`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/valueAllocSize.rs` vs expected `values/layout/value_alloc_size.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_alloc_size.rs` (current: `// port-lint: source src/values/layout/valueAllocSize.rs`)
- **Lint issues:** 1

### 49. compiler.stmt

- **Target:** `compiler.Stmt [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6003210.0
- **Functions:** 25/25 matched (target 28)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 24)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/stmt.rs` vs expected `eval/compiler/stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/stmt.rs` (current: `// port-lint: source src/eval/compiler/stmt.rs`)
- **Lint issues:** 1

### 50. profile.profiler_type

- **Target:** `profile.ProfilerType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 6
- **Priority Score:** 6000303.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/profilerType.rs` vs expected `eval/runtime/profile/profiler_type.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/profiler_type.rs` (current: `// port-lint: source src/eval/runtime/profile/profilerType.rs`)
- **Lint issues:** 1

### 51. types.array

- **Target:** `types.Array [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5093410.0
- **Functions:** 23/32 matched (target 24)
- **Missing functions:** `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`, `debug`, `display`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/array.rs` vs expected `values/types/array.rs`
- **Proposed provenance header:** `// port-lint: source values/types/array.rs` (current: `// port-lint: source src/values/types/array.rs`)
- **Lint issues:** 1

### 52. typing.arc_ty

- **Target:** `typing.ArcTy [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 5
- **Priority Score:** 5021104.5
- **Functions:** 6/7 matched (target 13)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 10)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/arcTy.rs` vs expected `typing/arc_ty.rs`
- **Proposed provenance header:** `// port-lint: source typing/arc_ty.rs` (current: `// port-lint: source src/typing/arcTy.rs`)
- **Lint issues:** 1

### 53. tests.def

- **Target:** `tests.Def [PROVENANCE-FALLBACK]`
- **Similarity:** 0.95
- **Dependents:** 5
- **Priority Score:** 5001400.5
- **Functions:** 14/14 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 14/14 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/def.rs` vs expected `tests/def.rs`
- **Proposed provenance header:** `// port-lint: source tests/def.rs` (current: `// port-lint: source src/tests/def.rs`)
- **Lint issues:** 1

### 54. typing.interface

- **Target:** `typing.Interface [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 5
- **Priority Score:** 5000404.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/interface.rs` vs expected `typing/interface.rs`
- **Proposed provenance header:** `// port-lint: source typing/interface.rs` (current: `// port-lint: source src/typing/interface.rs`)
- **Lint issues:** 1

### 55. scope.scope_resolver_globals

- **Target:** `scope.ScopeResolverGlobals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 5
- **Priority Score:** 5000403.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope/scopeResolverGlobals.rs` vs expected `eval/compiler/scope/scope_resolver_globals.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope/scope_resolver_globals.rs` (current: `// port-lint: source src/eval/compiler/scope/scopeResolverGlobals.rs`)
- **Lint issues:** 1

### 56. enumeration.enum_type

- **Target:** `enumeration.EnumType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 4
- **Priority Score:** 4094403.5
- **Functions:** 33/36 matched (target 39)
- **Missing functions:** `fmt`, `r#type`, `values`
- **Types:** 2/8 matched (target 5)
- **Missing types:** `EnumCell`, `TyEnumDataOpt`, `Frozen`, `EnumType`, `FrozenEnumType`, `Canonical`
- **Tests:** 12/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/enumType.rs` vs expected `values/types/enumeration/enum_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/enumeration/enumType.rs` vs expected `values/types/enumeration/enum_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/enum_type.rs` (current: `// port-lint: source src/values/types/enumeration/enumType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/enumeration/enum_type.rs` (current: `// port-lint: tests src/values/types/enumeration/enumType.rs`)
- **Lint issues:** 5

### 57. types.starlark_value_as_type

- **Target:** `types.StarlarkValueAsType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.21
- **Dependents:** 4
- **Priority Score:** 4091708.0
- **Functions:** 6/13 matched (target 8)
- **Missing functions:** `fmt`, `new`, `compiler_args_globals`, `compiler_args`, `test_pass`, `test_fail_compile_time`, `test_fail_runtime`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `CompilerArgs`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/starlarkValueAsType.rs` vs expected `values/types/starlark_value_as_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/starlark_value_as_type.rs` (current: `// port-lint: source src/values/types/starlarkValueAsType.rs`)
- **Lint issues:** 1

### 58. bc.frame

- **Target:** `bc.Frame [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4082610.0
- **Functions:** 16/24 matched (target 31)
- **Missing functions:** `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/frame.rs` vs expected `eval/bc/frame.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/frame.rs` (current: `// port-lint: source src/eval/bc/frame.rs`)
- **Lint issues:** 1

### 59. values.demand

- **Target:** `values.Demand [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 4
- **Priority Score:** 4061106.0
- **Functions:** 4/7 matched (target 5)
- **Missing functions:** `payload`, `provide`, `test_trait_downcast`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `SomeTrait`, `StaticType`, `MyValue`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/demand.rs` vs expected `values/demand.rs`
- **Proposed provenance header:** `// port-lint: source values/demand.rs` (current: `// port-lint: source src/values/demand.rs`)
- **Lint issues:** 1

### 60. values.value_of

- **Target:** `values.ValueOf [PROVENANCE-FALLBACK]`
- **Similarity:** 0.54
- **Dependents:** 4
- **Priority Score:** 4051004.8
- **Functions:** 4/6 matched (target 7)
- **Missing functions:** `deref`, `fmt`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/valueOf.rs` vs expected `values/value_of.rs`
- **Proposed provenance header:** `// port-lint: source values/value_of.rs` (current: `// port-lint: source src/values/valueOf.rs`)
- **Lint issues:** 1

### 61. profile.alloc_counts

- **Target:** `profile.AllocCounts [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 4
- **Priority Score:** 4040606.0
- **Functions:** 1/4 matched (target 5)
- **Missing functions:** `normalize_for_golden_tests`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/allocCounts.rs` vs expected `values/layout/heap/profile/alloc_counts.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/alloc_counts.rs` (current: `// port-lint: source src/values/layout/heap/profile/allocCounts.rs`)
- **Lint issues:** 1

### 62. bc.native_function

- **Target:** `bc.NativeFunction [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 4
- **Priority Score:** 4010505.0
- **Functions:** 3/4 matched
- **Missing functions:** `fun`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/nativeFunction.rs` vs expected `eval/bc/native_function.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/native_function.rs` (current: `// port-lint: source src/eval/bc/nativeFunction.rs`)
- **Lint issues:** 1

### 63. types.ellipsis

- **Target:** `types.Ellipsis [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 4
- **Priority Score:** 4010404.5
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `test_ellipsis`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/ellipsis.rs` vs expected `values/types/ellipsis.rs`
- **Proposed provenance header:** `// port-lint: source values/types/ellipsis.rs` (current: `// port-lint: source src/values/types/ellipsis.rs`)
- **Lint issues:** 1

### 64. record.record_type

- **Target:** `record.RecordType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3133010.0
- **Functions:** 15/22 matched (target 17)
- **Missing functions:** `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/recordType.rs` vs expected `values/types/record/record_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/record_type.rs` (current: `// port-lint: source src/values/types/record/recordType.rs`)
- **Lint issues:** 2

### 65. alloc.chunk

- **Target:** `alloc.Chunk [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3092210.0
- **Functions:** 11/19 matched (target 18)
- **Missing functions:** `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`, `test_alloc_release`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `ChunkDataEmpty`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chunk.rs` vs expected `values/layout/heap/allocator/alloc/chunk.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chunk.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chunk.rs`)
- **Lint issues:** 1

### 66. stdlib.call_stack

- **Target:** `stdlib.CallStack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 3
- **Priority Score:** 3061406.0
- **Functions:** 7/13 matched (target 10)
- **Missing functions:** `fmt`, `global`, `test_simple`, `test_strip_one`, `test_strip_all`, `test_call_stack_frame`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/callStack.rs` vs expected `stdlib/call_stack.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/call_stack.rs` (current: `// port-lint: source src/stdlib/callStack.rs`)
- **Lint issues:** 1

### 67. errors.did_you_mean

- **Target:** `errors.DidYouMean [PROVENANCE-FALLBACK]`
- **Similarity:** 0.13
- **Dependents:** 3
- **Priority Score:** 3050608.8
- **Functions:** 1/6 matched (target 2)
- **Missing functions:** `prefixes`, `typos`, `best`, `very_short`, `earlier_variants_are_more_important`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/errors/didYouMean.rs` vs expected `errors/did_you_mean.rs`
- **Proposed provenance header:** `// port-lint: source errors/did_you_mean.rs` (current: `// port-lint: source src/errors/didYouMean.rs`)
- **Lint issues:** 1

### 68. list.alloc

- **Target:** `list.Alloc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3040510.0
- **Functions:** 0/3 matched (target 0)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/alloc.rs` vs expected `values/types/list/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/alloc.rs` (current: `// port-lint: source src/values/types/list/alloc.rs`)
- **Lint issues:** 1

### 69. list.list_type

- **Target:** `list.ListType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.37
- **Dependents:** 3
- **Priority Score:** 3030506.2
- **Functions:** 1/2 matched (target 5)
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/listType.rs` vs expected `values/types/list/list_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/list_type.rs` (current: `// port-lint: source src/values/types/list/listType.rs`)
- **Lint issues:** 1

### 70. profile.instant

- **Target:** `profile.Instant [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 3
- **Priority Score:** 3020606.5
- **Functions:** 3/4 matched (target 8)
- **Missing functions:** `sub`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/instant.rs` vs expected `eval/runtime/profile/instant.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/instant.rs` (current: `// port-lint: source src/eval/runtime/profile/instant.rs`)
- **Lint issues:** 1

### 71. values.unpack_and_discard

- **Target:** `values.UnpackAndDiscard [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 3
- **Priority Score:** 3020506.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/unpackAndDiscard.rs` vs expected `values/unpack_and_discard.rs`
- **Proposed provenance header:** `// port-lint: source values/unpack_and_discard.rs` (current: `// port-lint: source src/values/unpackAndDiscard.rs`)
- **Lint issues:** 1

### 72. compiler.constants

- **Target:** `compiler.Constants [PROVENANCE-FALLBACK]`
- **Similarity:** 0.26
- **Dependents:** 3
- **Priority Score:** 3010507.2
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `test_constants`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/constants.rs` vs expected `eval/compiler/constants.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/constants.rs` (current: `// port-lint: source src/eval/compiler/constants.rs`)
- **Lint issues:** 1

### 73. sealed

- **Target:** `starlark.Sealed [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/sealed.rs` vs expected `sealed.rs`
- **Proposed provenance header:** `// port-lint: source sealed.rs` (current: `// port-lint: source src/sealed.rs`)
- **Lint issues:** 1

### 74. util.arc_or_static

- **Target:** `util.ArcOrStatic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 2
- **Priority Score:** 2061305.9
- **Functions:** 5/10 matched
- **Missing functions:** `fmt`, `eq`, `partial_cmp`, `cmp`, `hash`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/arcOrStatic.rs` vs expected `util/arc_or_static.rs`
- **Proposed provenance header:** `// port-lint: source util/arc_or_static.rs` (current: `// port-lint: source src/util/arcOrStatic.rs`)
- **Lint issues:** 1

### 75. typing.type_type

- **Target:** `typing.TypeType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.27
- **Dependents:** 2
- **Priority Score:** 2050807.2
- **Functions:** 2/5 matched (target 3)
- **Missing functions:** `test`, `module`, `takes_type`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeType.rs` vs expected `values/typing/type_type.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_type.rs` (current: `// port-lint: source src/values/typing/typeType.rs`)
- **Lint issues:** 1

### 76. alloc.chunk_part

- **Target:** `alloc.ChunkPart [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 2
- **Priority Score:** 2041602.5
- **Functions:** 11/15 matched (target 16)
- **Missing functions:** `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`, `test_is_full`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chunkPart.rs` vs expected `values/layout/heap/allocator/alloc/chunk_part.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chunk_part.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chunkPart.rs`)
- **Lint issues:** 1

### 77. values.owned_frozen_ref

- **Target:** `values.OwnedFrozenRef [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2031510.0
- **Functions:** 10/12 matched (target 17)
- **Missing functions:** `fmt`, `deref`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/ownedFrozenRef.rs` vs expected `values/owned_frozen_ref.rs`
- **Proposed provenance header:** `// port-lint: source values/owned_frozen_ref.rs` (current: `// port-lint: source src/values/ownedFrozenRef.rs`)
- **Lint issues:** 1

### 78. compiler.small_vec_1

- **Target:** `compiler.SmallVec1 [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 2
- **Priority Score:** 2031504.2
- **Functions:** 11/11 matched (target 16)
- **Missing functions:** _none_
- **Types:** 1/4 matched (target 3)
- **Missing types:** `Target`, `Item`, `IntoIter`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/smallVec1.rs` vs expected `eval/compiler/small_vec_1.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/small_vec_1.rs` (current: `// port-lint: source src/eval/compiler/smallVec1.rs`)
- **Lint issues:** 1

### 79. layout.const_type_id

- **Target:** `layout.ConstTypeId [PROVENANCE-FALLBACK]`
- **Similarity:** 0.14
- **Dependents:** 2
- **Priority Score:** 2030608.5
- **Functions:** 2/5 matched (target 6)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/constTypeId.rs` vs expected `values/layout/const_type_id.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/const_type_id.rs` (current: `// port-lint: source src/values/layout/constTypeId.rs`)
- **Lint issues:** 1

### 80. runtime.rust_loc

- **Target:** `runtime.RustLoc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2030310.0
- **Functions:** 0/3 matched (target 1)
- **Missing functions:** `rust_loc_globals`, `invoke`, `test_rust_loc`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/rustLoc.rs` vs expected `eval/runtime/rust_loc.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/rust_loc.rs` (current: `// port-lint: source src/eval/runtime/rustLoc.rs`)
- **Lint issues:** 1

### 81. avalues.str_

- **Target:** `avalues.Str [PROVENANCE-FALLBACK]`
- **Similarity:** 0.48
- **Dependents:** 2
- **Priority Score:** 2021405.2
- **Functions:** 11/11 matched (target 12)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/str_.rs` vs expected `values/layout/avalues/str_.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/str_.rs` (current: `// port-lint: source src/values/layout/avalues/str_.rs`)
- **Lint issues:** 2

### 82. values.stack_guard

- **Target:** `values.StackGuard [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2010510.0
- **Functions:** 3/4 matched
- **Missing functions:** `drop`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/stackGuard.rs` vs expected `values/stack_guard.rs`
- **Proposed provenance header:** `// port-lint: source values/stack_guard.rs` (current: `// port-lint: source src/values/stackGuard.rs`)
- **Lint issues:** 1

### 83. collections.aligned_padded_str

- **Target:** `collections.AlignedPaddedStr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2010410.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/alignedPaddedStr.rs` vs expected `collections/aligned_padded_str.rs`
- **Proposed provenance header:** `// port-lint: source collections/aligned_padded_str.rs` (current: `// port-lint: source src/collections/alignedPaddedStr.rs`)
- **Lint issues:** 1

### 84. profile.string_index

- **Target:** `profile.StringIndex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/stringIndex.rs` vs expected `values/layout/heap/profile/string_index.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/string_index.rs` (current: `// port-lint: source src/values/layout/heap/profile/stringIndex.rs`)
- **Lint issues:** 1

### 85. runtime.file_loader

- **Target:** `runtime.FileLoader [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 2
- **Priority Score:** 2000403.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/fileLoader.rs` vs expected `eval/runtime/file_loader.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/file_loader.rs` (current: `// port-lint: source src/eval/runtime/fileLoader.rs`)
- **Lint issues:** 1

### 86. collections.string_pool

- **Target:** `collections.StringPool [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000310.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/stringPool.rs` vs expected `collections/string_pool.rs`
- **Proposed provenance header:** `// port-lint: source collections/string_pool.rs` (current: `// port-lint: source src/collections/stringPool.rs`)
- **Lint issues:** 1

### 87. def_inline.local_as_value

- **Target:** `definline.LocalAsValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 2
- **Priority Score:** 2000204.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/defInline/localAsValue.rs` vs expected `eval/compiler/def_inline/local_as_value.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def_inline/local_as_value.rs` (current: `// port-lint: source src/eval/compiler/defInline/localAsValue.rs`)
- **Lint issues:** 1

### 88. hint

- **Target:** `starlark.Hint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.92
- **Dependents:** 2
- **Priority Score:** 2000200.8
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/hint.rs` vs expected `hint.rs`
- **Proposed provenance header:** `// port-lint: source hint.rs` (current: `// port-lint: source src/hint.rs`)
- **Lint issues:** 1

### 89. stdlib

- **Target:** `starlark.Stdlib [PROVENANCE-FALLBACK]`
- **Similarity:** 0.12
- **Dependents:** 1
- **Priority Score:** 1131708.8
- **Functions:** 3/14 matched (target 3)
- **Missing functions:** `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Bool2`, `Error`
- **Tests:** 0/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib.rs` vs expected `stdlib.rs`
- **Proposed provenance header:** `// port-lint: source stdlib.rs` (current: `// port-lint: source src/stdlib.rs`)
- **Lint issues:** 1

### 90. heap.arena

- **Target:** `heap.Arena [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1124410.0
- **Functions:** 26/37 matched (target 33)
- **Missing functions:** `max`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty`
- **Types:** 6/7 matched (target 8)
- **Missing types:** `Item`
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/arena.rs` vs expected `values/layout/heap/arena.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/arena.rs` (current: `// port-lint: source src/values/layout/heap/arena.rs`)
- **Lint issues:** 1

### 91. string.interpolation

- **Target:** `string.Interpolation [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1081610.0
- **Functions:** 4/12 matched (target 6)
- **Missing functions:** `test_incomplete_format`, `test_unsupported_format_character`, `test_parse_percent_s_one`, `test_type_support_d`, `test_type_support_o`, `test_type_support_x`, `test_type_support_e`, `test_int_min`
- **Types:** 4/4 matched (target 20)
- **Missing types:** _none_
- **Tests:** 0/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/interpolation.rs` vs expected `values/types/string/interpolation.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/interpolation.rs` (current: `// port-lint: source src/values/types/string/interpolation.rs`)
- **Lint issues:** 1

### 92. layout.pointer

- **Target:** `layout.Pointer [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1073710.0
- **Functions:** 25/32 matched (target 46)
- **Missing functions:** `fmt`, `_test_lifetime_covariant`, `from_usize_unchecked`, `to_usize`, `unpack`, `test_int_tag`, `check`
- **Types:** 5/5 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/pointer.rs` vs expected `values/layout/pointer.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/pointer.rs` (current: `// port-lint: source src/values/layout/pointer.rs`)
- **Lint issues:** 1

### 93. collections.alloca

- **Target:** `collections.Alloca [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1072610.0
- **Functions:** 15/22 matched (target 26)
- **Missing functions:** `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/alloca.rs` vs expected `collections/alloca.rs`
- **Proposed provenance header:** `// port-lint: source collections/alloca.rs` (current: `// port-lint: source src/collections/alloca.rs`)
- **Lint issues:** 4

### 94. stdlib.breakpoint

- **Target:** `stdlib.Breakpoint [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1072310.0
- **Functions:** 11/17 matched (target 13)
- **Missing functions:** `global`, `breakpoint`, `reset_global_state`, `test_breakpoint_real`, `test_breakpoint_mock`, `test_breakpoint_disabled`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Handler`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/breakpoint.rs` vs expected `stdlib/breakpoint.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/breakpoint.rs` (current: `// port-lint: source src/stdlib/breakpoint.rs`)
- **Lint issues:** 1

### 95. types.any_complex

- **Target:** `types.AnyComplex [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 1
- **Priority Score:** 1071205.9
- **Functions:** 4/7 matched (target 6)
- **Missing functions:** `fmt`, `test_any_complex`, `freeze`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/anyComplex.rs` vs expected `values/types/any_complex.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any_complex.rs` (current: `// port-lint: source src/values/types/anyComplex.rs`)
- **Lint issues:** 1

### 96. types.any_array

- **Target:** `types.AnyArray [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1061010.0
- **Functions:** 3/7 matched
- **Missing functions:** `fmt`, `drop`, `test_drop`, `test_allocation_size`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `IncrementOnDrop`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/anyArray.rs` vs expected `values/types/any_array.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any_array.rs` (current: `// port-lint: source src/values/types/anyArray.rs`)
- **Lint issues:** 1

### 97. types.list_or_tuple

- **Target:** `types.ListOrTuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1061008.0
- **Functions:** 3/5 matched (target 6)
- **Missing functions:** `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 2)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/listOrTuple.rs` vs expected `values/types/list_or_tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list_or_tuple.rs` (current: `// port-lint: source src/values/types/listOrTuple.rs`)
- **Lint issues:** 1

### 98. string.dot_format

- **Target:** `string.DotFormat [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 1
- **Priority Score:** 1051205.9
- **Functions:** 6/11 matched (target 6)
- **Missing functions:** `new`, `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/dotFormat.rs` vs expected `values/types/string/dot_format.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/dot_format.rs` (current: `// port-lint: source src/values/types/string/dotFormat.rs`)
- **Lint issues:** 1

### 99. util.rtabort

- **Target:** `util.Rtabort [PROVENANCE-FALLBACK]`
- **Similarity:** 0.26
- **Dependents:** 1
- **Priority Score:** 1040707.4
- **Functions:** 2/6 matched (target 3)
- **Missing functions:** `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/rtabort.rs` vs expected `util/rtabort.rs`
- **Proposed provenance header:** `// port-lint: source util/rtabort.rs` (current: `// port-lint: source src/util/rtabort.rs`)
- **Lint issues:** 1

### 100. bc.if_debug

- **Target:** `bc.IfDebug [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1030910.0
- **Functions:** 5/8 matched (target 9)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/ifDebug.rs` vs expected `eval/bc/if_debug.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/if_debug.rs` (current: `// port-lint: source src/eval/bc/ifDebug.rs`)
- **Lint issues:** 1

### 101. util.non_static_type_id

- **Target:** `util.NonStaticTypeId [PROVENANCE-FALLBACK]`
- **Similarity:** 0.05
- **Dependents:** 1
- **Priority Score:** 1030409.5
- **Functions:** 1/3 matched (target 1)
- **Missing functions:** `get_type_id`, `test_non_static_type_id`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `NonStaticAny`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/nonStaticTypeId.rs` vs expected `util/non_static_type_id.rs`
- **Proposed provenance header:** `// port-lint: source util/non_static_type_id.rs` (current: `// port-lint: source src/util/nonStaticTypeId.rs`)
- **Lint issues:** 1

### 102. runtime.cheap_call_stack

- **Target:** `runtime.CheapCallStack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1022010.0
- **Functions:** 15/17 matched
- **Missing functions:** `fmt`, `default`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/cheapCallStack.rs` vs expected `eval/runtime/cheap_call_stack.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/cheap_call_stack.rs` (current: `// port-lint: source src/eval/runtime/cheapCallStack.rs`)
- **Lint issues:** 1

### 103. avalues.simple

- **Target:** `avalues.Simple [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1021110.0
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/simple.rs` vs expected `values/layout/avalues/simple.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/simple.rs` (current: `// port-lint: source src/values/layout/avalues/simple.rs`)
- **Lint issues:** 1

### 104. layout.value_captured

- **Target:** `layout.ValueCaptured [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1020810.0
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/valueCaptured.rs` vs expected `values/layout/value_captured.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_captured.rs` (current: `// port-lint: source src/values/layout/valueCaptured.rs`)
- **Lint issues:** 1

### 105. record.field

- **Target:** `record.Field [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 1
- **Priority Score:** 1020603.9
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 0/1 matched
- **Missing types:** `FieldGen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/field.rs` vs expected `values/types/record/field.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/field.rs` (current: `// port-lint: source src/values/types/record/field.rs`)
- **Lint issues:** 1

### 106. typing.bindings

- **Target:** `typing.Bindings [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1011110.0
- **Functions:** 7/8 matched (target 18)
- **Missing functions:** `get_for_clause`
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/bindings.rs` vs expected `typing/bindings.rs`
- **Proposed provenance header:** `// port-lint: source typing/bindings.rs` (current: `// port-lint: source src/typing/bindings.rs`)
- **Lint issues:** 2

### 107. typing.structs

- **Target:** `typing.Structs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 1
- **Priority Score:** 1011003.7
- **Functions:** 7/8 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/structs.rs` vs expected `typing/structs.rs`
- **Proposed provenance header:** `// port-lint: source typing/structs.rs` (current: `// port-lint: source src/typing/structs.rs`)
- **Lint issues:** 1

### 108. structs.unordered_hasher

- **Target:** `structs.UnorderedHasher [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 1
- **Priority Score:** 1010603.6
- **Functions:** 4/5 matched (target 4)
- **Missing functions:** `_write`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/unorderedHasher.rs` vs expected `values/types/structs/unordered_hasher.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/unorderedHasher.rs` vs expected `values/types/structs/unordered_hasher.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/unordered_hasher.rs` (current: `// port-lint: source src/values/types/structs/unorderedHasher.rs`)
- **Proposed provenance header:** `// port-lint: source values/types/structs/unordered_hasher.rs` (current: `// port-lint: source src/values/types/structs/unorderedHasher.rs`)
- **Lint issues:** 2

### 109. read_line

- **Target:** `starlark.ReadLine [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `NoRustyline`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/readLine.rs` vs expected `read_line.rs`
- **Proposed provenance header:** `// port-lint: source read_line.rs` (current: `// port-lint: source src/readLine.rs`)
- **Lint issues:** 1

### 110. tests.before_stmt

- **Target:** `tests.BeforeStmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.91
- **Dependents:** 1
- **Priority Score:** 1010100.9
- **Functions:** 0/1 matched
- **Missing functions:** `before_stmt`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/beforeStmt.rs` vs expected `tests/before_stmt.rs`
- **Proposed provenance header:** `// port-lint: source tests/before_stmt.rs` (current: `// port-lint: source src/tests/beforeStmt.rs`)
- **Lint issues:** 1

### 111. typing.function

- **Target:** `typing.Function [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001502.6
- **Functions:** 12/12 matched (target 24)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/function.rs` vs expected `typing/function.rs`
- **Proposed provenance header:** `// port-lint: source typing/function.rs` (current: `// port-lint: source src/typing/function.rs`)
- **Lint issues:** 1

### 112. heap.fast_cell

- **Target:** `heap.FastCell [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000810.0
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/fastCell.rs` vs expected `values/layout/heap/fast_cell.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/fast_cell.rs` (current: `// port-lint: source src/values/layout/heap/fastCell.rs`)
- **Lint issues:** 1

### 113. analysis.lint_message

- **Target:** `analysis.LintMessage [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 1
- **Priority Score:** 1000201.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/lintMessage.rs` vs expected `analysis/lint_message.rs`
- **Proposed provenance header:** `// port-lint: source analysis/lint_message.rs` (current: `// port-lint: source src/analysis/lintMessage.rs`)
- **Lint issues:** 1

### 114. bc.instr_impl

- **Target:** `bc.InstrImpl [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 777010.0
- **Functions:** 7/7 matched (target 95)
- **Missing functions:** _none_
- **Types:** 87/163 matched (target 103)
- **Missing types:** `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrImpl.rs` vs expected `eval/bc/instr_impl.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr_impl.rs` (current: `// port-lint: source src/eval/bc/instrImpl.rs`)
- **Lint issues:** 20

### 115. set.methods

- **Target:** `set.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 506910.0
- **Functions:** 18/68 matched (target 18)
- **Missing functions:** `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/50 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/methods.rs` vs expected `values/types/set/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/methods.rs` (current: `// port-lint: source src/values/types/set/methods.rs`)
- **Lint issues:** 2

### 116. int.int_or_big

- **Target:** `int.IntOrBig [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 265310.0
- **Functions:** 24/46 matched (target 50)
- **Missing functions:** `starlark_type_repr`, `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`, `test_floor_div_big`, `test_floor_div_big_small`, `test_floor_div_small_big`, `test_floor_div_small`, `test_percent_big`, `test_percent_big_small`, `test_percent_small_big`, `test_percent_small`
- **Types:** 3/7 matched (target 11)
- **Missing types:** `Canonical`, `Err`, `Error`, `Output`
- **Tests:** 0/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/intOrBig.rs` vs expected `values/types/int/int_or_big.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/int_or_big.rs` (current: `// port-lint: source src/values/types/int/intOrBig.rs`)
- **Lint issues:** 1

### 117. set.value

- **Target:** `set.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 255910.0
- **Functions:** 30/50 matched (target 43)
- **Missing functions:** `fmt`, `test_bit_or`, `test_bit_or_lhs_empty`, `test_bit_or_rhs_empty`, `test_bit_or_fail_iter`, `test_bit_or_ord`, `test_bit_and`, `test_bit_and_lhs_empty`, `test_bit_and_rhs_empty`, `test_bit_and_ord`, `test_bit_and_fail_iter`, `test_bit_xor`, `test_bit_xor_ord`, `test_bit_xor_lhs_empty`, `test_bit_xor_rhs_empty`, `test_bit_xor_fail_iter`, `test_sub`, `test_sub_empty_lhs`, `test_sub_empty_rhs`, `test_sub_fail_iter`
- **Types:** 4/9 matched (target 6)
- **Missing types:** `MutableSet`, `FrozenSet`, `Canonical`, `Frozen`, `ContentRef`
- **Tests:** 0/19 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/value.rs` vs expected `values/types/set/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/value.rs` (current: `// port-lint: source src/values/types/set/value.rs`)
- **Lint issues:** 1

### 118. values.typing.callable

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 234004.8
- **Functions:** 12/32 matched (target 41)
- **Missing functions:** `clone`, `fmt`, `trace`, `_assert_sync_send`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_pass`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `test_callable_checked_runtime`, `module`, `good`, `bad`
- **Types:** 5/8 matched (target 5)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 0/15 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/callable.rs` vs expected `values/typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/callable.rs` (current: `// port-lint: source src/values/typing/callable.rs`)
- **Lint issues:** 1

### 119. string.str_type

- **Target:** `string.StrType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 225110.0
- **Functions:** 28/47 matched (target 59)
- **Missing functions:** `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `new`, `serialize`, `test_string_corruption`, `test_escape_characters`, `test_string_hash`, `test_zero_length_string_hash_is_not_zero`, `test_string_len`, `test_arithmetic_on_string`, `test_slice_string`, `test_string_is_in`, `test_successive_add`, `test_string_index`, `test_str`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `StarlarkStrN`, `Frozen`, `Target`
- **Tests:** 0/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/strType.rs` vs expected `values/types/string/str_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/str_type.rs` (current: `// port-lint: source src/values/types/string/strType.rs`)
- **Lint issues:** 1

### 120. adapter.tests

- **Target:** `tests.Tests [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 222510.0
- **Functions:** 0/22 matched (target 21)
- **Missing functions:** `new`, `event_stopped`, `get_client`, `eval_stopped`, `wait_for_eval_stopped`, `drop`, `breakpoint`, `breakpoints_args`, `eval_with_hook`, `join_timeout`, `dap_test_template`, `test_breakpoint`, `test_breakpoint_with_failing_condition`, `test_breakpoint_with_passing_condition`, `test_step_over`, `test_step_into`, `test_step_out`, `test_local_variables`, `test_inspect_variables`, `test_evaluate_expression`, `assert_variable`, `test_truncate_string`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/22 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter/tests.rs` vs expected `debug/adapter/tests.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter/tests.rs` (current: `// port-lint: source src/debug/adapter/tests.rs`)
- **Lint issues:** 1

### 121. typing.user

- **Target:** `typing.User [PROVENANCE-FALLBACK]`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 173506.8
- **Functions:** 13/27 matched (target 25)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`, `test_intersect_with_abstract_type`, `test_ty_user_intersects_with_base_starlark_value`
- **Types:** 5/8 matched
- **Missing types:** `AbstractPlant`, `FruitCallable`, `Fruit`
- **Tests:** 0/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/user.rs` vs expected `typing/user.rs`
- **Proposed provenance header:** `// port-lint: source typing/user.rs` (current: `// port-lint: source src/typing/user.rs`)
- **Lint issues:** 1

### 122. analysis.names

- **Target:** `analysis.Names [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 164310.0
- **Functions:** 21/35 matched (target 31)
- **Missing functions:** `new`, `ident`, `assign_ident`, `lint`, `about`, `test_lint_unused`, `test_lint_duplicate_assign`, `test_lint_unassigned`, `test_lint_undefined`, `test_early_fail`, `test_assign_for_next`, `test_flow_control`, `test_lambda_capture`, `test_global_defined_later`
- **Types:** 6/8 matched (target 12)
- **Missing types:** `AstStr`, `AstStrExt`
- **Tests:** 0/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/names.rs` vs expected `analysis/names.rs`
- **Proposed provenance header:** `// port-lint: source analysis/names.rs` (current: `// port-lint: source src/analysis/names.rs`)
- **Lint issues:** 1

### 123. float.float

- **Target:** `float.Float [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 154210.0
- **Functions:** 26/39 matched (target 41)
- **Missing functions:** `fmt`, `non_finite`, `test_write_non_finite`, `decimal`, `test_write_decimal`, `scientific`, `test_write_scientific`, `compact`, `test_write_compact`, `test_arithmetic_operators`, `test_dictionary_key`, `test_comparisons`, `test_comparisons_by_sorting`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/float.rs` vs expected `values/types/float/float.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/float.rs` (current: `// port-lint: source src/values/types/float/float.rs`)
- **Lint issues:** 1

### 124. layout.typed

- **Target:** `layout.ValueTyped [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 153810.0
- **Functions:** 21/31 matched (target 44)
- **Missing functions:** `fmt`, `serialize`, `eq`, `deref`, `unpack_value_impl`, `int`, `test_unpack_value_for_frozen_value_typed`, `module`, `mutable`, `takes_frozen_value_typed`
- **Types:** 2/7 matched (target 2)
- **Missing types:** `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/typed.rs` vs expected `values/layout/typed.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/typed.rs` (current: `// port-lint: source src/values/layout/typed.rs`)
- **Lint issues:** 1

### 125. list.value

- **Target:** `list.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 136410.0
- **Functions:** 46/56 matched (target 99)
- **Missing functions:** `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare`
- **Types:** 5/8 matched
- **Missing types:** `FrozenList`, `List`, `Canonical`
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/value.rs` vs expected `values/types/list/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/value.rs` (current: `// port-lint: source src/values/types/list/value.rs`)
- **Lint issues:** 1

### 126. dict.value

- **Target:** `dict.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 136210.0
- **Functions:** 44/52 matched (target 67)
- **Missing functions:** `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `test_mutate_dict`, `test_get_str`, `test_repr_cycle`
- **Types:** 5/10 matched (target 8)
- **Missing types:** `Canonical`, `FrozenDict`, `MutableDict`, `Frozen`, `ContentRef`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/value.rs` vs expected `values/types/dict/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/value.rs` (current: `// port-lint: source src/values/types/dict/value.rs`)
- **Lint issues:** 2

### 127. num.value

- **Target:** `num.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 122610.0
- **Functions:** 11/22 matched (target 25)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul`, `test_from_value`, `test_conversion_to_float`, `test_conversion_to_int`, `test_hashing`, `test_eq`
- **Types:** 3/4 matched (target 6)
- **Missing types:** `Output`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num/value.rs` vs expected `values/types/num/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num/value.rs` (current: `// port-lint: source src/values/types/num/value.rs`)
- **Lint issues:** 1

### 128. stdlib.extra

- **Target:** `stdlib.Extra [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 122010.0
- **Functions:** 5/16 matched (target 19)
- **Missing functions:** `fmt`, `print`, `pprint`, `pstr`, `prepr`, `test_filter`, `test_map`, `test_debug`, `test_print`, `test_pstr`, `test_prepr`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `PrintHandlerImpl`
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/extra.rs` vs expected `stdlib/extra.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/extra.rs` (current: `// port-lint: source src/stdlib/extra.rs`)
- **Lint issues:** 1

### 129. analysis

- **Target:** `starlark.Analysis [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 121310.0
- **Functions:** 0/12 matched (target 0)
- **Missing functions:** `lint`, `module`, `test_lint_suppressions_keyword_matching`, `test_lint_suppressions_fn_with_many_issues`, `test_lint_suppressions_preceding_whitespace`, `test_lint_suppressions_with_space_separator`, `test_lint_suppressions_multiline_span`, `test_lint_suppressions_small_span`, `test_lint_suppressions_data`, `test_lint_suppressions_line_before`, `test_lint_suppressions_line_before_windows_newlines`, `test_lint_suppressions_inside_fn`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis.rs` vs expected `analysis.rs`
- **Proposed provenance header:** `// port-lint: source analysis.rs` (current: `// port-lint: source src/analysis.rs`)
- **Lint issues:** 1

### 130. heap.heap_type

- **Target:** `heap.HeapType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 115510.0
- **Functions:** 37/47 matched (target 59)
- **Missing functions:** `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `test_send_sync`, `test_string_reallocated_on_heap`, `test_interned_string_equal`, `validate_str_interning`, `append_x`, `test_interned_str_starlark`
- **Types:** 7/8 matched
- **Missing types:** `FrozenHeapName`
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/heapType.rs` vs expected `values/layout/heap/heap_type.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/heap_type.rs` (current: `// port-lint: source src/values/layout/heap/heapType.rs`)
- **Lint issues:** 1

### 131. tests.markdown

- **Target:** `tests.Markdown [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 112903.7
- **Functions:** 16/27 matched (target 32)
- **Missing functions:** `module`, `submodule`, `object`, `golden_docs_starlark`, `native_docs_module`, `linked_ty_mapper`, `globals_render_default`, `globals_render_default_with_linked_type`, `globals_render_signature_at_bottom`, `globals_render_signature_at_bottom_with_linked_type`, `golden_docs_object`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/tests/markdown.rs` vs expected `docs/tests/markdown.rs`
- **Proposed provenance header:** `// port-lint: source docs/tests/markdown.rs` (current: `// port-lint: source src/docs/tests/markdown.rs`)
- **Lint issues:** 1

### 132. pagable.vtable_registry

- **Target:** `pagable.VtableRegistry [PROVENANCE-FALLBACK]`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 111708.2
- **Functions:** 4/13 matched (target 8)
- **Missing functions:** `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_lookup_nonexistent_type`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `TestSimpleType`, `TestComplexGen`
- **Tests:** 0/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pagable/vtableRegistry.rs` vs expected `pagable/vtable_registry.rs`
- **Proposed provenance header:** `// port-lint: source pagable/vtable_registry.rs` (current: `// port-lint: source src/pagable/vtableRegistry.rs`)
- **Lint issues:** 1

### 133. record.globals

- **Target:** `record.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 111210.0
- **Functions:** 1/12 matched (target 1)
- **Missing functions:** `record`, `field`, `test_record_pass`, `test_record_fail_0`, `test_record_fail_1`, `test_record_fail_2`, `test_record_fail_3`, `test_record_fail_4`, `test_record_fail_5`, `test_record_equality`, `test_field_invalid`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/globals.rs` vs expected `values/types/record/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/globals.rs` (current: `// port-lint: source src/values/types/record/globals.rs`)
- **Lint issues:** 1

### 134. tests.uncategorized

- **Target:** `tests.Uncategorized [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 105810.0
- **Functions:** 46/52 matched (target 58)
- **Missing functions:** `unpack_value_impl`, `module`, `select`, `rust_failure`, `freeze`, `wrapper`
- **Types:** 2/6 matched (target 3)
- **Missing types:** `Error`, `FrozenWrapper`, `Canonical`, `Frozen`
- **Tests:** 36/36 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/uncategorized.rs` vs expected `tests/uncategorized.rs`
- **Proposed provenance header:** `// port-lint: source tests/uncategorized.rs` (current: `// port-lint: source src/tests/uncategorized.rs`)
- **Lint issues:** 2

### 135. alloc.chain

- **Target:** `alloc.Chain [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102710.0
- **Functions:** 14/22 matched (target 19)
- **Missing functions:** `drop`, `test_default`, `test_new_drop`, `test_new_drop_many`, `test_split_at`, `test_split_at_len`, `test_split_at_zero`, `test_depth`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Item`, `ResetSplitAtZeroTest`
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chain.rs` vs expected `values/layout/heap/allocator/alloc/chain.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chain.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chain.rs`)
- **Lint issues:** 1

### 136. range.range_type

- **Target:** `range.RangeType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102510.0
- **Functions:** 14/24 matched (target 32)
- **Missing functions:** `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`, `test_range_exhaustive`, `test_max_len`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/range/rangeType.rs` vs expected `values/types/range/range_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/range/range_type.rs` (current: `// port-lint: source src/values/types/range/rangeType.rs`)
- **Lint issues:** 2

### 137. stdlib.partial

- **Target:** `stdlib.Partial [PROVENANCE-FALLBACK]`
- **Similarity:** 0.20
- **Dependents:** 0
- **Priority Score:** 101708.0
- **Functions:** 4/12 matched (target 7)
- **Missing functions:** `partial`, `fmt`, `eq`, `test_simple`, `test_star_to_partial`, `test_start_to_returned_func`, `test_no_args_to_partial`, `test_typecheck_bug`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `Frozen`, `Canonical`
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/partial.rs` vs expected `stdlib/partial.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/partial.rs` (current: `// port-lint: source src/stdlib/partial.rs`)
- **Lint issues:** 2

### 138. typing.small_arc_vec_or_static

- **Target:** `typing.SmallArcVecOrStatic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 101507.5
- **Functions:** 3/10 matched
- **Missing functions:** `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Target`, `Item`, `IntoIter`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/smallArcVecOrStatic.rs` vs expected `typing/small_arc_vec_or_static.rs`
- **Proposed provenance header:** `// port-lint: source typing/small_arc_vec_or_static.rs` (current: `// port-lint: source src/typing/smallArcVecOrStatic.rs`)
- **Lint issues:** 1

### 139. stdlib.json

- **Target:** `stdlib.Json [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 101210.0
- **Functions:** 2/11 matched (target 24)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `json`, `encode`, `decode`, `test_json_encode`, `test_json_decode`, `test_json_very_large_int`, `test_json_128bit_and_beyond`
- **Types:** 0/1 matched (target 11)
- **Missing types:** `Canonical`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/json.rs` vs expected `stdlib/json.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/json.rs` (current: `// port-lint: source src/stdlib/json.rs`)
- **Lint issues:** 1

### 140. tuple.value

- **Target:** `tuple.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 93404.5
- **Functions:** 24/31 matched (target 25)
- **Missing functions:** `fmt`, `new`, `offset_of_content`, `typechecker_ty`, `test_to_str`, `test_repr_cycle`, `test_tuple_ellipsis_runtime`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Tuple`, `FrozenTuple`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/value.rs` vs expected `values/types/tuple/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/value.rs` (current: `// port-lint: source src/values/types/tuple/value.rs`)
- **Lint issues:** 1

### 141. tests.rustdocs

- **Target:** `tests.Rustdocs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.20
- **Dependents:** 0
- **Priority Score:** 91708.0
- **Functions:** 5/14 matched (target 12)
- **Missing functions:** `simple`, `default_arg`, `args_kwargs`, `custom_types`, `pos_named`, `with_arguments`, `object`, `func1`, `module`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/tests/rustdocs.rs` vs expected `docs/tests/rustdocs.rs`
- **Proposed provenance header:** `// port-lint: source docs/tests/rustdocs.rs` (current: `// port-lint: source src/docs/tests/rustdocs.rs`)
- **Lint issues:** 1

### 142. type_compiled.compiled

- **Target:** `typecompiled.Compiled [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 84603.4
- **Functions:** 33/39 matched (target 47)
- **Missing functions:** `fmt`, `check_matches`, `starlark_type_repr`, `alloc_value`, `hash`, `eq`
- **Types:** 5/7 matched (target 12)
- **Missing types:** `StaticType`, `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/compiled.rs` vs expected `values/typing/type_compiled/compiled.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/compiled.rs` (current: `// port-lint: source src/values/typing/typeCompiled/compiled.rs`)
- **Lint issues:** 2

### 143. thin_box_slice_frozen_value.thin_box

- **Target:** `thinboxslicefrozenvalue.ThinBox [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 83210.0
- **Functions:** 22/29 matched (target 28)
- **Missing functions:** `deref`, `deref_mut`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`
- **Types:** 2/3 matched
- **Missing types:** `Target`
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/thinBox.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/thinBox.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/thinBox.rs`)
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/thinBox.rs`)
- **Lint issues:** 2

### 144. profile.bc

- **Target:** `profile.Bc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 82903.6
- **Functions:** 12/19 matched (target 28)
- **Missing functions:** `sum`, `add_assign`, `default`, `test_smoke`, `test_smoke_2`, `test_bc_profile_data_merge`, `test_bc_pairs_profile_data_merge`
- **Types:** 9/10 matched (target 13)
- **Missing types:** `Data`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/bc.rs` vs expected `eval/runtime/profile/bc.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/bc.rs` (current: `// port-lint: source src/eval/runtime/profile/bc.rs`)
- **Lint issues:** 1

### 145. bc.instrs

- **Target:** `bc.Instrs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 82810.0
- **Functions:** 17/24 matched (target 27)
- **Missing functions:** `drop_in_place`, `handle`, `drop_instrs`, `drop`, `opcodes`, `fmt`, `display`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `HandlerImpl`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrs.rs` vs expected `eval/bc/instrs.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instrs.rs` (current: `// port-lint: source src/eval/bc/instrs.rs`)
- **Lint issues:** 1

### 146. analysis.flow

- **Target:** `analysis.Flow [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 82510.0
- **Functions:** 16/24 matched (target 32)
- **Missing functions:** `lint`, `module`, `about`, `test_lint_returns`, `test_lint_unreachable`, `test_lint_redundant`, `test_lint_misplaced_load`, `test_lint_no_effect`
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/flow.rs` vs expected `analysis/flow.rs`
- **Proposed provenance header:** `// port-lint: source analysis/flow.rs` (current: `// port-lint: source src/analysis/flow.rs`)
- **Lint issues:** 1

### 147. scope.payload

- **Target:** `scope.Payload [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 82410.0
- **Functions:** 6/7 matched (target 21)
- **Missing functions:** `from_ast`
- **Types:** 10/17 matched (target 10)
- **Missing types:** `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CstStmtFromAst`, `CstAssignIdentExt`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope/payload.rs` vs expected `eval/compiler/scope/payload.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope/payload.rs` (current: `// port-lint: source src/eval/compiler/scope/payload.rs`)
- **Lint issues:** 1

### 148. tests.runtime

- **Target:** `tests.Runtime [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 81510.0
- **Functions:** 6/14 matched (target 11)
- **Missing functions:** `drop`, `globals`, `mk`, `measure_stack`, `stack_depth`, `helpers`, `current_usage`, `is_gc_disabled`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/runtime.rs` vs expected `tests/runtime.rs`
- **Proposed provenance header:** `// port-lint: source tests/runtime.rs` (current: `// port-lint: source src/tests/runtime.rs`)
- **Lint issues:** 1

### 149. typing.small_arc_vec

- **Target:** `typing.SmallArcVec [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 81406.9
- **Functions:** 4/11 matched (target 16)
- **Missing functions:** `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter`
- **Types:** 2/3 matched (target 5)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/smallArcVec.rs` vs expected `typing/small_arc_vec.rs`
- **Proposed provenance header:** `// port-lint: source typing/small_arc_vec.rs` (current: `// port-lint: source src/typing/smallArcVec.rs`)
- **Lint issues:** 1

### 150. module.unpack_value

- **Target:** `module.UnpackValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 81306.9
- **Functions:** 5/13 matched (target 5)
- **Missing functions:** `with_int`, `with_int_list`, `with_list_list`, `with_dict_list`, `with_int_dict`, `with_list_dict`, `with_dict_dict`, `with_either`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/unpackValue.rs` vs expected `tests/derive/module/unpack_value.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/unpack_value.rs` (current: `// port-lint: source src/tests/derive/module/unpackValue.rs`)
- **Lint issues:** 1

### 151. profile.aggregated

- **Target:** `profile.Aggregated [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 73210.0
- **Functions:** 17/24 matched (target 34)
- **Missing functions:** `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `test_stacks_collect`, `test_stacks_collect_retained`, `test_merge`, `make`
- **Types:** 8/8 matched (target 10)
- **Missing types:** _none_
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/aggregated.rs` vs expected `values/layout/heap/profile/aggregated.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/aggregated.rs` (current: `// port-lint: source src/values/layout/heap/profile/aggregated.rs`)
- **Lint issues:** 1

### 152. funcs.other

- **Target:** `funcs.Other [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71910.0
- **Functions:** 12/19 matched (target 13)
- **Missing functions:** `r#type`, `test_abs`, `test_constants`, `test_chr`, `test_hash`, `test_int`, `test_tuple`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/other.rs` vs expected `stdlib/funcs/other.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/other.rs` (current: `// port-lint: source src/stdlib/funcs/other.rs`)
- **Lint issues:** 1

### 153. typed.string

- **Target:** `typed.String [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 71805.9
- **Functions:** 8/15 matched (target 35)
- **Missing functions:** `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`, `test_string_hashes`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/typed/string.rs` vs expected `values/layout/typed/string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/typed/string.rs` (current: `// port-lint: source src/values/layout/typed/string.rs`)
- **Lint issues:** 1

### 154. dict.methods

- **Target:** `dict.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71710.0
- **Functions:** 10/17 matched (target 11)
- **Missing functions:** `test_error_codes`, `test_dict_add`, `test_dict_with_duplicates`, `test_dict_update_with_self_pos`, `test_dict_update_with_self_as_kwargs`, `test_frozen_dict_cannot_be_updated_with_self_pos`, `test_frozen_dict_cannot_be_updated_with_self_as_kwargs`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/methods.rs` vs expected `values/types/dict/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/methods.rs` (current: `// port-lint: source src/values/types/dict/methods.rs`)
- **Lint issues:** 2

### 155. layout.complex

- **Target:** `layout.Complex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71710.0
- **Functions:** 9/13 matched (target 15)
- **Missing functions:** `unpack_value_impl`, `fmt`, `test_module`, `test_unpack`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/complex.rs` vs expected `values/layout/complex.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/complex.rs` (current: `// port-lint: source src/values/layout/complex.rs`)
- **Lint issues:** 1

### 156. docs.parse

- **Target:** `docs.Parse [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71610.0
- **Functions:** 8/15 matched (target 11)
- **Missing functions:** `parses_starlark_docstring`, `parses_rust_docstring`, `parses_and_removes_sections_from_starlark_docstring`, `parses_and_removes_sections_from_rust_docstring`, `arg`, `parses_starlark_function_docstring`, `parses_rust_function_docstring`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/parse.rs` vs expected `docs/parse.rs`
- **Proposed provenance header:** `// port-lint: source docs/parse.rs` (current: `// port-lint: source src/docs/parse.rs`)
- **Lint issues:** 1

### 157. string.simd

- **Target:** `string.Simd [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71010.0
- **Functions:** 1/8 matched (target 2)
- **Missing functions:** `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/simd.rs` vs expected `values/types/string/simd.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/simd.rs` (current: `// port-lint: source src/values/types/string/simd.rs`)
- **Lint issues:** 1

### 158. bigint.convert

- **Target:** `bigint.Convert [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71010.0
- **Functions:** 3/8 matched (target 23)
- **Missing functions:** `unpack_value_impl`, `test_unpack_int_error`, `module`, `takes_i32`, `takes_i64`
- **Types:** 0/2 matched (target 6)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bigint/convert.rs` vs expected `values/types/bigint/convert.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bigint/convert.rs` (current: `// port-lint: source src/values/types/bigint/convert.rs`)
- **Lint issues:** 1

### 159. record.ty_record_type

- **Target:** `record.TyRecordType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 70810.0
- **Functions:** 0/7 matched (target 0)
- **Missing functions:** `test_good`, `test_fail_compile_time`, `test_fail_runtime_time`, `test_record_instance_typechecker_ty`, `test_typecheck_field_pass`, `test_typecheck_field_fail`, `test_typecheck_record_type_call`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/tyRecordType.rs` vs expected `values/types/record/ty_record_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/ty_record_type.rs` (current: `// port-lint: source src/values/types/record/tyRecordType.rs`)
- **Lint issues:** 1

### 160. compiler.scope

- **Target:** `compiler.Scope [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 67110.0
- **Functions:** 48/51 matched (target 65)
- **Missing functions:** `from`, `assign_ident_impl`, `new`
- **Types:** 17/20 matched (target 28)
- **Missing types:** `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope.rs` vs expected `eval/compiler/scope.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope.rs` (current: `// port-lint: source src/eval/compiler/scope.rs`)
- **Lint issues:** 1

### 161. assert.assert

- **Target:** `assert.Assert [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 65210.0
- **Functions:** 44/50 matched (target 72)
- **Missing functions:** `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/assert/assert.rs` vs expected `assert/assert.rs`
- **Proposed provenance header:** `// port-lint: source assert/assert.rs` (current: `// port-lint: source src/assert/assert.rs`)
- **Lint issues:** 1

### 162. string.repr

- **Target:** `string.Repr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62310.0
- **Functions:** 16/22 matched (target 21)
- **Missing functions:** `or4`, `push_vec_tail`, `test`, `string_repr_for_test`, `test_chunk_non_ascii_or_need_escape`, `load`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 7/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/repr.rs` vs expected `values/types/string/repr.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/repr.rs` vs expected `values/types/string/repr.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/repr.rs` (current: `// port-lint: source src/values/types/string/repr.rs`)
- **Proposed provenance header:** `// port-lint: source values/types/string/repr.rs` (current: `// port-lint: source src/values/types/string/repr.rs`)
- **Lint issues:** 2

### 163. structs.value

- **Target:** `structs.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62210.0
- **Functions:** 15/21 matched (target 18)
- **Missing functions:** `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_to_json`, `test_comparison_bug`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/value.rs` vs expected `values/types/structs/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/value.rs` (current: `// port-lint: source src/values/types/structs/value.rs`)
- **Lint issues:** 1

### 164. thin_box_slice_frozen_value.packed_impl

- **Target:** `thinboxslicefrozenvalue.PackedImpl [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62110.0
- **Functions:** 13/18 matched
- **Missing functions:** `visit`, `deref`, `fmt`, `eq`, `across_lengths`
- **Types:** 2/3 matched
- **Missing types:** `Target`
- **Tests:** 5/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/packedImpl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/packedImpl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/packedImpl.rs`)
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/packedImpl.rs`)
- **Lint issues:** 2

### 165. tests.call

- **Target:** `tests.Call [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61810.0
- **Functions:** 12/18 matched (target 17)
- **Missing functions:** `funcall_test`, `funcall_extra_args_def`, `test_extra_args_native`, `test_frame_size`, `natives`, `stack_ptr`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 11/15 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/call.rs` vs expected `tests/call.rs`
- **Proposed provenance header:** `// port-lint: source tests/call.rs` (current: `// port-lint: source src/tests/call.rs`)
- **Lint issues:** 1

### 166. analysis.dubious

- **Target:** `analysis.Dubious [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61410.0
- **Functions:** 7/12 matched (target 19)
- **Missing functions:** `lint`, `module`, `about`, `test_lint_duplicate_keys`, `test_lint_identifier_as_statement`
- **Types:** 1/2 matched (target 8)
- **Missing types:** `Key`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/dubious.rs` vs expected `analysis/dubious.rs`
- **Proposed provenance header:** `// port-lint: source analysis/dubious.rs` (current: `// port-lint: source src/analysis/dubious.rs`)
- **Lint issues:** 1

### 167. profile.csv

- **Target:** `profile.Csv [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 61306.7
- **Functions:** 6/10 matched (target 9)
- **Missing functions:** `new`, `format_for_csv`, `test_csv_writer`, `test_quote_str_for_csv`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Impl`, `CsvValue`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/csv.rs` vs expected `eval/runtime/profile/csv.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/csv.rs` (current: `// port-lint: source src/eval/runtime/profile/csv.rs`)
- **Lint issues:** 1

### 168. analysis.types

- **Target:** `analysis.Types [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 61206.9
- **Functions:** 4/7 matched
- **Missing functions:** `fmt`, `new`, `from`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LintWarning`, `LintT`, `EvalSeverity`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/types.rs` vs expected `analysis/types.rs`
- **Proposed provenance header:** `// port-lint: source analysis/types.rs` (current: `// port-lint: source src/analysis/types.rs`)
- **Lint issues:** 1

### 169. list.unpack

- **Target:** `list.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61010.0
- **Functions:** 3/5 matched (target 9)
- **Missing functions:** `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 4)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/unpack.rs` vs expected `values/types/list/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/unpack.rs` (current: `// port-lint: source src/values/types/list/unpack.rs`)
- **Lint issues:** 1

### 170. tuple.unpack

- **Target:** `tuple.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 61006.7
- **Functions:** 3/5 matched (target 7)
- **Missing functions:** `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 3)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/unpack.rs` vs expected `values/types/tuple/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/unpack.rs` (current: `// port-lint: source src/values/types/tuple/unpack.rs`)
- **Lint issues:** 1

### 171. tuple.rust_tuple

- **Target:** `tuple.RustTuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60610.0
- **Functions:** 0/4 matched (target 11)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/rustTuple.rs` vs expected `values/types/tuple/rust_tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/rust_tuple.rs` (current: `// port-lint: source src/values/types/tuple/rustTuple.rs`)
- **Lint issues:** 1

### 172. environment.modules

- **Target:** `environment.Modules [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 54710.0
- **Functions:** 38/43 matched (target 48)
- **Missing functions:** `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/modules.rs` vs expected `environment/modules.rs`
- **Proposed provenance header:** `// port-lint: source environment/modules.rs` (current: `// port-lint: source src/environment/modules.rs`)
- **Lint issues:** 1

### 173. values.owned

- **Target:** `values.Owned [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 53410.0
- **Functions:** 26/29 matched (target 32)
- **Missing functions:** `fmt`, `downcast_starlark`, `deref`
- **Types:** 3/5 matched
- **Missing types:** `Canonical`, `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/owned.rs` vs expected `values/owned.rs`
- **Proposed provenance header:** `// port-lint: source values/owned.rs` (current: `// port-lint: source src/values/owned.rs`)
- **Lint issues:** 1

### 174. profile.time_flame

- **Target:** `profile.TimeFlame [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 53004.0
- **Functions:** 15/19 matched (target 18)
- **Missing functions:** `r#gen`, `test_time_flame_works_inside_frozen_module`, `register_sleep`, `sleep`
- **Types:** 10/11 matched (target 15)
- **Missing types:** `Data`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/timeFlame.rs` vs expected `eval/runtime/profile/time_flame.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/time_flame.rs` (current: `// port-lint: source src/eval/runtime/profile/timeFlame.rs`)
- **Lint issues:** 1

### 175. profile.stmt

- **Target:** `profile.Stmt [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 52610.0
- **Functions:** 13/17 matched (target 23)
- **Missing functions:** `r#gen`, `test_coverage`, `test_empty`, `test_merge`
- **Types:** 8/9 matched
- **Missing types:** `Data`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/stmt.rs` vs expected `eval/runtime/profile/stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/stmt.rs` (current: `// port-lint: source src/eval/runtime/profile/stmt.rs`)
- **Lint issues:** 1

### 176. typing.callable_param

- **Target:** `typing.CallableParam [PROVENANCE-FALLBACK]`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 52604.4
- **Functions:** 16/20 matched (target 27)
- **Missing functions:** `fmt`, `pf`, `new_named_only`, `test_param_spec_display`
- **Types:** 5/6 matched (target 10)
- **Missing types:** `ParamSpecDisplay`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callableParam.rs` vs expected `typing/callable_param.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable_param.rs` (current: `// port-lint: source src/typing/callableParam.rs`)
- **Lint issues:** 1

### 177. alloc.allocator

- **Target:** `alloc.Allocator [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 52110.0
- **Functions:** 14/18 matched (target 19)
- **Missing functions:** `fmt`, `default`, `drop`, `random_iteration`
- **Types:** 2/3 matched
- **Missing types:** `Item`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/allocator.rs` vs expected `values/layout/heap/allocator/alloc/allocator.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/allocator.rs` vs expected `values/layout/heap/allocator/alloc/allocator.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/allocator.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/allocator.rs`)
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/allocator.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/allocator.rs`)
- **Lint issues:** 2

### 178. namespace.value

- **Target:** `namespace.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 51702.4
- **Functions:** 10/15 matched (target 17)
- **Missing functions:** `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_kwargs`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace/value.rs` vs expected `values/types/namespace/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace/value.rs` (current: `// port-lint: source src/values/types/namespace/value.rs`)
- **Lint issues:** 1

### 179. values.unpack

- **Target:** `values.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 51605.5
- **Functions:** 8/9 matched (target 14)
- **Missing functions:** `error`
- **Types:** 3/7 matched
- **Missing types:** `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/unpack.rs` vs expected `values/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/unpack.rs` (current: `// port-lint: source src/values/unpack.rs`)
- **Lint issues:** 1

### 180. dict.refs

- **Target:** `dict.Refs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 51604.4
- **Functions:** 7/9 matched (target 14)
- **Missing functions:** `from_value`, `deref`
- **Types:** 4/7 matched (target 11)
- **Missing types:** `Target`, `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/refs.rs` vs expected `values/types/dict/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/refs.rs` (current: `// port-lint: source src/values/types/dict/refs.rs`)
- **Lint issues:** 1

### 181. analysis.underscore

- **Target:** `analysis.Underscore [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51410.0
- **Functions:** 8/13 matched (target 16)
- **Missing functions:** `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/underscore.rs` vs expected `analysis/underscore.rs`
- **Proposed provenance header:** `// port-lint: source analysis/underscore.rs` (current: `// port-lint: source src/analysis/underscore.rs`)
- **Lint issues:** 1

### 182. allocator.bumpalo

- **Target:** `allocator.Bumpalo [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51110.0
- **Functions:** 6/8 matched (target 6)
- **Missing functions:** `next`, `size_hint`
- **Types:** 0/3 matched (target 1)
- **Missing types:** `ChunkIteratorWrapper`, `Item`, `ChunkRevIterator`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/bumpalo.rs` vs expected `values/layout/heap/allocator/bumpalo.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/bumpalo.rs` (current: `// port-lint: source src/values/layout/heap/allocator/bumpalo.rs`)
- **Lint issues:** 1

### 183. module.named_positional

- **Target:** `module.NamedPositional [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 51105.4
- **Functions:** 6/11 matched (target 6)
- **Missing functions:** `positional`, `named`, `named_only`, `named_after_args`, `named_after_args_explicitly_marked`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/namedPositional.rs` vs expected `tests/derive/module/named_positional.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/named_positional.rs` (current: `// port-lint: source src/tests/derive/module/namedPositional.rs`)
- **Lint issues:** 1

### 184. typing.iter

- **Target:** `typing.Iter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51010.0
- **Functions:** 3/6 matched (target 8)
- **Missing functions:** `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `NonInstantiable`, `Canonical`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/iter.rs` vs expected `values/typing/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/iter.rs` (current: `// port-lint: source src/values/typing/iter.rs`)
- **Lint issues:** 1

### 185. debug.inspect

- **Target:** `debug.Inspect [PROVENANCE-FALLBACK]`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 50904.7
- **Functions:** 4/9 matched (target 7)
- **Missing functions:** `debugger`, `debug_inspect_stack`, `debug_inspect_variables`, `test_debug_stack`, `test_debug_variables`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/inspect.rs` vs expected `debug/inspect.rs`
- **Proposed provenance header:** `// port-lint: source debug/inspect.rs` (current: `// port-lint: source src/debug/inspect.rs`)
- **Lint issues:** 1

### 186. compiler.def

- **Target:** `compiler.Def [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 45210.0
- **Functions:** 38/39 matched (target 46)
- **Missing functions:** `fmt`
- **Types:** 10/13 matched (target 15)
- **Missing types:** `Def`, `FrozenDef`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/def.rs` vs expected `eval/compiler/def.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def.rs` (current: `// port-lint: source src/eval/compiler/def.rs`)
- **Lint issues:** 4

### 187. params.spec

- **Target:** `params.Spec [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 44410.0
- **Functions:** 34/38 matched (target 34)
- **Missing functions:** `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl`
- **Types:** 6/6 matched (target 11)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/spec.rs` vs expected `eval/runtime/params/spec.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/spec.rs` (current: `// port-lint: source src/eval/runtime/params/spec.rs`)
- **Lint issues:** 1

### 188. string.methods

- **Target:** `string.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 44210.0
- **Functions:** 37/41 matched (target 50)
- **Missing functions:** `test_error_codes`, `test_count`, `test_find`, `test_opaque_iterator`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/methods.rs` vs expected `values/types/string/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/methods.rs` (current: `// port-lint: source src/values/types/string/methods.rs`)
- **Lint issues:** 2

### 189. typing.custom

- **Target:** `typing.Custom [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 43804.2
- **Functions:** 31/35 matched (target 49)
- **Missing functions:** `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/custom.rs` vs expected `typing/custom.rs`
- **Proposed provenance header:** `// port-lint: source typing/custom.rs` (current: `// port-lint: source src/typing/custom.rs`)
- **Lint issues:** 1

### 190. heap.repr

- **Target:** `heap.Repr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 43210.0
- **Functions:** 23/27 matched (target 34)
- **Missing functions:** `hash`, `eq`, `as_avalue_or_header`, `from_payload_ptr_mut`
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/repr.rs` vs expected `values/layout/heap/repr.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/repr.rs` (current: `// port-lint: source src/values/layout/heap/repr.rs`)
- **Lint issues:** 1

### 191. typing.tests

- **Target:** `typing.Tests [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 43003.6
- **Functions:** 24/28 matched (target 24)
- **Missing functions:** `accepts_iterable`, `accepts_typed_kwargs`, `accepts_callable_named_xy`, `new`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 18/18 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests.rs` vs expected `typing/tests.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests.rs` (current: `// port-lint: source src/typing/tests.rs`)
- **Lint issues:** 1

### 192. bc.addr

- **Target:** `bc.Addr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 42904.1
- **Functions:** 20/23 matched (target 35)
- **Missing functions:** `add_assign`, `get_instr_mut`, `sub_usize`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/addr.rs` vs expected `eval/bc/addr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/addr.rs` (current: `// port-lint: source src/eval/bc/addr.rs`)
- **Lint issues:** 1

### 193. types.function

- **Target:** `types.Function [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 42502.4
- **Functions:** 12/13 matched (target 27)
- **Missing functions:** `new`
- **Types:** 9/12 matched (target 10)
- **Missing types:** `Canonical`, `NativeFuncFn`, `NativeMethFn`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/function.rs` vs expected `values/types/function.rs`
- **Proposed provenance header:** `// port-lint: source values/types/function.rs` (current: `// port-lint: source src/values/types/function.rs`)
- **Lint issues:** 1

### 194. analysis.incompatible

- **Target:** `analysis.Incompatible [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41510.0
- **Functions:** 10/14 matched (target 17)
- **Missing functions:** `lint`, `module`, `test_lint_incompatible`, `test_lint_duplicate_top_level_assign`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/incompatible.rs` vs expected `analysis/incompatible.rs`
- **Proposed provenance header:** `// port-lint: source analysis/incompatible.rs` (current: `// port-lint: source src/analysis/incompatible.rs`)
- **Lint issues:** 1

### 195. profile.typecheck

- **Target:** `profile.Typecheck [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 41304.8
- **Functions:** 5/8 matched (target 6)
- **Missing functions:** `r#gen`, `test_typecheck_profile`, `test_typecheck_profile_merge`
- **Types:** 4/5 matched
- **Missing types:** `Data`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/typecheck.rs` vs expected `eval/runtime/profile/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/typecheck.rs` (current: `// port-lint: source src/eval/runtime/profile/typecheck.rs`)
- **Lint issues:** 1

### 196. profile.flamegraph

- **Target:** `profile.Flamegraph [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 41302.4
- **Functions:** 6/10 matched (target 16)
- **Missing functions:** `new`, `test_flamegraph_writer`, `test_flamegraph_data`, `test_merge`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/flamegraph.rs` vs expected `eval/runtime/profile/flamegraph.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/flamegraph.rs` (current: `// port-lint: source src/eval/runtime/profile/flamegraph.rs`)
- **Lint issues:** 1

### 197. runtime.inlined_frame

- **Target:** `runtime.InlinedFrame [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 41202.5
- **Functions:** 5/9 matched
- **Missing functions:** `eq`, `test_inline_into`, `make_span`, `assert_stack`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/inlinedFrame.rs` vs expected `eval/runtime/inlined_frame.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/inlined_frame.rs` (current: `// port-lint: source src/eval/runtime/inlinedFrame.rs`)
- **Lint issues:** 1

### 198. list.methods

- **Target:** `list.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41110.0
- **Functions:** 7/11 matched (target 14)
- **Missing functions:** `list_methods`, `test_error_codes`, `test_index`, `recursive_list`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/methods.rs` vs expected `values/types/list/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/methods.rs` (current: `// port-lint: source src/values/types/list/methods.rs`)
- **Lint issues:** 3

### 199. analysis.performance

- **Target:** `analysis.Performance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 41105.6
- **Functions:** 6/10 matched (target 14)
- **Missing functions:** `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/performance.rs` vs expected `analysis/performance.rs`
- **Proposed provenance header:** `// port-lint: source analysis/performance.rs` (current: `// port-lint: source src/analysis/performance.rs`)
- **Lint issues:** 1

### 200. params.parser

- **Target:** `params.Parser [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41010.0
- **Functions:** 5/9 matched (target 10)
- **Missing functions:** `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/parser.rs` vs expected `eval/runtime/params/parser.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/parser.rs` (current: `// port-lint: source src/eval/runtime/params/parser.rs`)
- **Lint issues:** 1

### 201. module.other_attributes

- **Target:** `module.OtherAttributes [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40610.0
- **Functions:** 2/6 matched (target 3)
- **Missing functions:** `test_global`, `test_method`, `test_other_attributes_in_atributes`, `test_attribute`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/otherAttributes.rs` vs expected `tests/derive/module/other_attributes.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/other_attributes.rs` (current: `// port-lint: source src/tests/derive/module/otherAttributes.rs`)
- **Lint issues:** 1

### 202. dict.alloc

- **Target:** `dict.Alloc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/3 matched (target 1)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/alloc.rs` vs expected `values/types/dict/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/alloc.rs` (current: `// port-lint: source src/values/types/dict/alloc.rs`)
- **Lint issues:** 1

### 203. enumeration.globals

- **Target:** `enumeration.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 1/5 matched (target 1)
- **Missing functions:** `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/globals.rs` vs expected `values/types/enumeration/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/globals.rs` (current: `// port-lint: source src/values/types/enumeration/globals.rs`)
- **Lint issues:** 1

### 204. structs.alloc

- **Target:** `structs.Alloc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/3 matched (target 0)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/alloc.rs` vs expected `values/types/structs/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/alloc.rs` (current: `// port-lint: source src/values/types/structs/alloc.rs`)
- **Lint issues:** 1

### 205. set.set

- **Target:** `set.Set [PROVENANCE-FALLBACK]`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 40508.6
- **Functions:** 1/5 matched (target 1)
- **Missing functions:** `set`, `test_set_type_as_type_compile_time`, `test_return_set_type_as_type_compile_time`, `test_set_type_as_type_run_time`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/set.rs` vs expected `values/types/set/set.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/set.rs` (current: `// port-lint: source src/values/types/set/set.rs`)
- **Lint issues:** 1

### 206. opt.if_rand

- **Target:** `opt.IfRand [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 32910.0
- **Functions:** 23/26 matched (target 28)
- **Missing functions:** `r#true`, `r#false`, `fmt`
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/ifRand.rs` vs expected `tests/opt/if_rand.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/if_rand.rs` (current: `// port-lint: source src/tests/opt/ifRand.rs`)
- **Lint issues:** 1

### 207. profile.heap

- **Target:** `profile.Heap [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 32402.0
- **Functions:** 11/13 matched (target 28)
- **Missing functions:** `r#gen`, `test_profiling`
- **Types:** 10/11 matched
- **Missing types:** `Data`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/heap.rs` vs expected `eval/runtime/profile/heap.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/heap.rs` (current: `// port-lint: source src/eval/runtime/profile/heap.rs`)
- **Lint issues:** 1

### 208. type_compiled.matcher

- **Target:** `typecompiled.Matcher [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 31702.0
- **Functions:** 10/10 matched (target 13)
- **Missing functions:** _none_
- **Types:** 4/7 matched
- **Missing types:** `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/matcher.rs` vs expected `values/typing/type_compiled/matcher.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/matcher.rs` (current: `// port-lint: source src/values/typing/typeCompiled/matcher.rs`)
- **Lint issues:** 1

### 209. avalues.static_

- **Target:** `avalues.Static [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31410.0
- **Functions:** 8/9 matched (target 11)
- **Missing functions:** `test_alloc_static_simple`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/static_.rs` vs expected `values/layout/avalues/static_.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/static_.rs` (current: `// port-lint: source src/values/layout/avalues/static_.rs`)
- **Lint issues:** 1

### 210. list.refs

- **Target:** `list.Refs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31410.0
- **Functions:** 9/9 matched (target 29)
- **Missing functions:** _none_
- **Types:** 2/5 matched (target 10)
- **Missing types:** `Target`, `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/refs.rs` vs expected `values/types/list/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/refs.rs` (current: `// port-lint: source src/values/types/list/refs.rs`)
- **Lint issues:** 1

### 211. avalues.list

- **Target:** `avalues.List [PROVENANCE-FALLBACK]`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 31405.2
- **Functions:** 9/10 matched (target 18)
- **Missing functions:** `alloc_list_concat`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/list.rs` vs expected `values/layout/avalues/list.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/list.rs` (current: `// port-lint: source src/values/layout/avalues/list.rs`)
- **Lint issues:** 1

### 212. symbol.map

- **Target:** `symbol.Map [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31310.0
- **Functions:** 9/12 matched (target 11)
- **Missing functions:** `fmt`, `new`, `with_capacity`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/symbol/map.rs` vs expected `collections/symbol/map.rs`
- **Proposed provenance header:** `// port-lint: source collections/symbol/map.rs` (current: `// port-lint: source src/collections/symbol/map.rs`)
- **Lint issues:** 1

### 213. bc.opcode

- **Target:** `bc.Opcode [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 31205.5
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `opcode_count`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `ByNumber`, `FindOpcode`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/opcode.rs` vs expected `eval/bc/opcode.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/opcode.rs` (current: `// port-lint: source src/eval/bc/opcode.rs`)
- **Lint issues:** 1

### 214. heap.send

- **Target:** `heap.Send [PROVENANCE-FALLBACK]`
- **Similarity:** 0.17
- **Dependents:** 0
- **Priority Score:** 31108.3
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `fmt`
- **Types:** 4/6 matched
- **Missing types:** `Target`, `StaticType`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/send.rs` vs expected `values/layout/heap/send.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/send.rs` (current: `// port-lint: source src/values/layout/heap/send.rs`)
- **Lint issues:** 1

### 215. tuple.refs

- **Target:** `tuple.Refs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 31103.6
- **Functions:** 6/7 matched (target 15)
- **Missing functions:** `unpack_value_impl`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/refs.rs` vs expected `values/types/tuple/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/refs.rs` (current: `// port-lint: source src/values/types/tuple/refs.rs`)
- **Lint issues:** 1

### 216. module.generic

- **Target:** `module.Generic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 31004.9
- **Functions:** 6/8 matched (target 7)
- **Missing functions:** `test_attribute`, `make_my_str`
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/generic.rs` vs expected `tests/derive/module/generic.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/generic.rs` (current: `// port-lint: source src/tests/derive/module/generic.rs`)
- **Lint issues:** 1

### 217. typing.never

- **Target:** `typing.Never [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30910.0
- **Functions:** 4/6 matched (target 9)
- **Missing functions:** `test_never_runtime`, `test_never_compile_time`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Canonical`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/never.rs` vs expected `values/typing/never.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/never.rs` (current: `// port-lint: source src/values/typing/never.rs`)
- **Lint issues:** 1

### 218. module.basic

- **Target:** `module.Basic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.36
- **Dependents:** 0
- **Priority Score:** 30906.4
- **Functions:** 6/9 matched (target 6)
- **Missing functions:** `cc_binary`, `r#enum`, `test`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/basic.rs` vs expected `tests/derive/module/basic.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/basic.rs` (current: `// port-lint: source src/tests/derive/module/basic.rs`)
- **Lint issues:** 1

### 219. bc.repr

- **Target:** `bc.Repr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 30906.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `size_of_repr`, `handle`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/repr.rs` vs expected `eval/bc/repr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/repr.rs` (current: `// port-lint: source src/eval/bc/repr.rs`)
- **Lint issues:** 3

### 220. string.alloc_unpack

- **Target:** `string.AllocUnpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.39
- **Dependents:** 0
- **Priority Score:** 30806.1
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `unpack_value_impl`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/allocUnpack.rs` vs expected `values/types/string/alloc_unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/alloc_unpack.rs` (current: `// port-lint: source src/values/types/string/allocUnpack.rs`)
- **Lint issues:** 1

### 221. tuple.alloc

- **Target:** `tuple.Alloc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 30705.1
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `test_alloc_tuple`, `test_alloc_frozen_tuple`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/alloc.rs` vs expected `values/types/tuple/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/alloc.rs` (current: `// port-lint: source src/values/types/tuple/alloc.rs`)
- **Lint issues:** 1

### 222. profile.mode

- **Target:** `profile.Mode [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30610.0
- **Functions:** 2/4 matched
- **Missing functions:** `fmt`, `from_str`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Err`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/mode.rs` vs expected `eval/runtime/profile/mode.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/mode.rs` (current: `// port-lint: source src/eval/runtime/profile/mode.rs`)
- **Lint issues:** 1

### 223. float.unpack

- **Target:** `float.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 30606.7
- **Functions:** 2/3 matched
- **Missing functions:** `test_unpack_float`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/unpack.rs` vs expected `values/types/float/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/unpack.rs` (current: `// port-lint: source src/values/types/float/unpack.rs`)
- **Lint issues:** 1

### 224. freeze.validator_order

- **Target:** `freeze.ValidatorOrder [PROVENANCE-FALLBACK]`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 30604.7
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `test`
- **Types:** 1/3 matched
- **Missing types:** `Frozen`, `Test`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/validatorOrder.rs` vs expected `tests/derive/freeze/validator_order.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/validator_order.rs` (current: `// port-lint: source src/tests/derive/freeze/validatorOrder.rs`)
- **Lint issues:** 2

### 225. values.typing.ty

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty [PROVENANCE-FALLBACK]`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 30602.2
- **Functions:** 2/5 matched (target 7)
- **Missing functions:** `test_isinstance`, `test_pass`, `test_fail_compile_time`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/ty.rs` vs expected `values/typing/ty.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/ty.rs` (current: `// port-lint: source src/values/typing/ty.rs`)
- **Lint issues:** 1

### 226. tests.freeze_access_value

- **Target:** `tests.FreezeAccessValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30410.0
- **Functions:** 1/2 matched
- **Missing functions:** `test`
- **Types:** 0/2 matched (target 3)
- **Missing types:** `Test`, `Frozen`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/freezeAccessValue.rs` vs expected `tests/freeze_access_value.rs`
- **Proposed provenance header:** `// port-lint: source tests/freeze_access_value.rs` (current: `// port-lint: source src/tests/freezeAccessValue.rs`)
- **Lint issues:** 1

### 227. debug.evaluate

- **Target:** `debug.Evaluate [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30410.0
- **Functions:** 1/4 matched (target 3)
- **Missing functions:** `debugger`, `debug_evaluate`, `test_debug_evaluate`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/evaluate.rs` vs expected `debug/evaluate.rs`
- **Proposed provenance header:** `// port-lint: source debug/evaluate.rs` (current: `// port-lint: source src/debug/evaluate.rs`)
- **Lint issues:** 1

### 228. type_compiled.globals

- **Target:** `typecompiled.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 30408.4
- **Functions:** 1/4 matched (target 1)
- **Missing functions:** `eval_type`, `isinstance`, `test_typechecking`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/globals.rs` vs expected `values/typing/type_compiled/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/globals.rs` (current: `// port-lint: source src/values/typing/typeCompiled/globals.rs`)
- **Lint issues:** 1

### 229. opt.type_is

- **Target:** `opt.TypeIs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30310.0
- **Functions:** 0/3 matched
- **Missing functions:** `globals`, `returns_type_is`, `does_not_return_type_is`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/typeIs.rs` vs expected `tests/opt/type_is.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/type_is.rs` (current: `// port-lint: source src/tests/opt/typeIs.rs`)
- **Lint issues:** 1

### 230. layout.vtable

- **Target:** `layout.Vtable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 27302.6
- **Functions:** 67/67 matched (target 73)
- **Missing functions:** _none_
- **Types:** 4/6 matched (target 4)
- **Missing types:** `GetTypeId`, `GetAllocativeKey`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/vtable.rs` vs expected `values/layout/vtable.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/vtable.rs` (current: `// port-lint: source src/values/layout/vtable.rs`)
- **Lint issues:** 2

### 231. compiler.expr

- **Target:** `compiler.Expr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 27010.0
- **Functions:** 59/59 matched (target 63)
- **Missing functions:** _none_
- **Types:** 9/11 matched (target 56)
- **Missing types:** `AstLiteralCompile`, `CompilerExprUtil`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/expr.rs` vs expected `eval/compiler/expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/expr.rs` (current: `// port-lint: source src/eval/compiler/expr.rs`)
- **Lint issues:** 1

### 232. values.traits

- **Target:** `values.Traits [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 25910.0
- **Functions:** 55/56 matched (target 55)
- **Missing functions:** `please_use_starlark_type_macro`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/traits.rs` vs expected `values/traits.rs`
- **Proposed provenance header:** `// port-lint: source values/traits.rs` (current: `// port-lint: source src/values/traits.rs`)
- **Lint issues:** 6

### 233. adapter.implementation

- **Target:** `adapter.Implementation [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 22910.0
- **Functions:** 22/23 matched (target 28)
- **Missing functions:** `fmt`
- **Types:** 5/6 matched (target 10)
- **Missing types:** `ToEvalMessage`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter/implementation.rs` vs expected `debug/adapter/implementation.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter/implementation.rs` (current: `// port-lint: source src/debug/adapter/implementation.rs`)
- **Lint issues:** 2

### 234. bc.stack_ptr

- **Target:** `bc.StackPtr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 21903.5
- **Functions:** 10/11 matched (target 25)
- **Missing functions:** `add`
- **Types:** 7/8 matched (target 7)
- **Missing types:** `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/stackPtr.rs` vs expected `eval/bc/stack_ptr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/stack_ptr.rs` (current: `// port-lint: source src/eval/bc/stackPtr.rs`)
- **Lint issues:** 1

### 235. profile.summary_by_function

- **Target:** `profile.SummaryByFunction [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21310.0
- **Functions:** 9/10 matched
- **Missing functions:** `drop_non_drop`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `RowKind`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/summaryByFunction.rs` vs expected `values/layout/heap/profile/summary_by_function.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/summary_by_function.rs` (current: `// port-lint: source src/values/layout/heap/profile/summaryByFunction.rs`)
- **Lint issues:** 1

### 236. avalues.array

- **Target:** `avalues.Array [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21310.0
- **Functions:** 9/9 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/array.rs` vs expected `values/layout/avalues/array.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/array.rs` (current: `// port-lint: source src/values/layout/avalues/array.rs`)
- **Lint issues:** 1

### 237. compiler.args

- **Target:** `compiler.Args [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 21304.0
- **Functions:** 10/11 matched
- **Missing functions:** `args`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Never`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/args.rs` vs expected `eval/compiler/args.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/args.rs` (current: `// port-lint: source src/eval/compiler/args.rs`)
- **Lint issues:** 1

### 238. avalues.tuple

- **Target:** `avalues.Tuple [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21210.0
- **Functions:** 8/8 matched (target 16)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/tuple.rs` vs expected `values/layout/avalues/tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/tuple.rs` (current: `// port-lint: source src/values/layout/avalues/tuple.rs`)
- **Lint issues:** 1

### 239. tests.basic

- **Target:** `tests.Basic [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21210.0
- **Functions:** 10/12 matched
- **Missing functions:** `arithmetic_test`, `bitwise_test`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/basic.rs` vs expected `tests/basic.rs`
- **Proposed provenance header:** `// port-lint: source tests/basic.rs` (current: `// port-lint: source src/tests/basic.rs`)
- **Lint issues:** 1

### 240. avalues.complex

- **Target:** `avalues.Complex [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 21103.9
- **Functions:** 6/6 matched (target 14)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/complex.rs` vs expected `values/layout/avalues/complex.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/complex.rs` (current: `// port-lint: source src/values/layout/avalues/complex.rs`)
- **Lint issues:** 1

### 241. eval.bc.compiler.stmt

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21010.0
- **Functions:** 8/10 matched (target 11)
- **Missing functions:** `write_if_then`, `write_if_else`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/stmt.rs` vs expected `eval/bc/compiler/stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/stmt.rs` (current: `// port-lint: source src/eval/bc/compiler/stmt.rs`)
- **Lint issues:** 1

### 242. symbol.symbol

- **Target:** `symbol.Symbol [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 21003.7
- **Functions:** 7/9 matched (target 11)
- **Missing functions:** `fmt`, `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/symbol/symbol.rs` vs expected `collections/symbol/symbol.rs`
- **Proposed provenance header:** `// port-lint: source collections/symbol/symbol.rs` (current: `// port-lint: source src/collections/symbol/symbol.rs`)
- **Lint issues:** 1

### 243. set.refs

- **Target:** `set.Refs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 21002.8
- **Functions:** 5/5 matched (target 14)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 11)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/refs.rs` vs expected `values/types/set/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/refs.rs` (current: `// port-lint: source src/values/types/set/refs.rs`)
- **Lint issues:** 1

### 244. profile.data

- **Target:** `profile.Data [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `_assert_profile_data_send_sync`, `_assert_send_sync`
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/data.rs` vs expected `eval/runtime/profile/data.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/data.rs` (current: `// port-lint: source src/eval/runtime/profile/data.rs`)
- **Lint issues:** 1

### 245. bc.instr_arg

- **Target:** `bc.InstrArg [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 42)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrArg.rs` vs expected `eval/bc/instr_arg.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr_arg.rs` (current: `// port-lint: source src/eval/bc/instrArg.rs`)
- **Lint issues:** 55

### 246. typing.callable

- **Target:** `typing.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 20904.2
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `TyCallableInner`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callable.rs` vs expected `typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable.rs` (current: `// port-lint: source src/typing/callable.rs`)
- **Lint issues:** 1

### 247. bc.bytecode

- **Target:** `bc.Bytecode [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 20904.0
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `handle`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/bytecode.rs` vs expected `eval/bc/bytecode.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/bytecode.rs` (current: `// port-lint: source src/eval/bc/bytecode.rs`)
- **Lint issues:** 1

### 248. bc.call

- **Target:** `bc.Call [PROVENANCE-FALLBACK]`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 20903.3
- **Functions:** 3/4 matched (target 15)
- **Missing functions:** `fmt`
- **Types:** 4/5 matched (target 8)
- **Missing types:** `Args`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/call.rs` vs expected `eval/bc/call.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/call.rs` (current: `// port-lint: source src/eval/bc/call.rs`)
- **Lint issues:** 9

### 249. structs.refs

- **Target:** `structs.Refs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 20903.0
- **Functions:** 5/5 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/4 matched
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/refs.rs` vs expected `values/types/structs/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/refs.rs` (current: `// port-lint: source src/values/types/structs/refs.rs`)
- **Lint issues:** 1

### 250. derive.docs

- **Target:** `derive.Docs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 20805.6
- **Functions:** 4/6 matched (target 7)
- **Missing functions:** `foo`, `serialize`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/docs.rs` vs expected `tests/derive/docs.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/docs.rs` (current: `// port-lint: source src/tests/derive/docs.rs`)
- **Lint issues:** 1

### 251. heap.call_enter_exit

- **Target:** `heap.CallEnterExit [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20710.0
- **Functions:** 0/1 matched (target 4)
- **Missing functions:** `drop`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/callEnterExit.rs` vs expected `values/layout/heap/call_enter_exit.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/call_enter_exit.rs` (current: `// port-lint: source src/values/layout/heap/callEnterExit.rs`)
- **Lint issues:** 1

### 252. types.any

- **Target:** `types.Any [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 20702.9
- **Functions:** 4/5 matched
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/any.rs` vs expected `values/types/any.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any.rs` (current: `// port-lint: source src/values/types/any.rs`)
- **Lint issues:** 1

### 253. freeze.bounds

- **Target:** `freeze.Bounds [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 2/3 matched (target 5)
- **Missing functions:** `assert_impl`
- **Types:** 2/3 matched
- **Missing types:** `Test`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/bounds.rs` vs expected `tests/derive/freeze/bounds.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/bounds.rs` (current: `// port-lint: source src/tests/derive/freeze/bounds.rs`)
- **Lint issues:** 1

### 254. dict.unpack

- **Target:** `dict.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/unpack.rs` vs expected `values/types/dict/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/unpack.rs` (current: `// port-lint: source src/values/types/dict/unpack.rs`)
- **Lint issues:** 1

### 255. list.globals

- **Target:** `list.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 4/5 matched
- **Missing functions:** `list`
- **Types:** 0/1 matched
- **Missing types:** `ListType`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/globals.rs` vs expected `values/types/list/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/globals.rs` (current: `// port-lint: source src/values/types/list/globals.rs`)
- **Lint issues:** 2

### 256. values.type_repr

- **Target:** `values.TypeRepr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 2/3 matched (target 6)
- **Missing functions:** `test_canonical_for_complex_value`
- **Types:** 2/3 matched (target 6)
- **Missing types:** `Canonical`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typeRepr.rs` vs expected `values/type_repr.rs`
- **Proposed provenance header:** `// port-lint: source values/type_repr.rs` (current: `// port-lint: source src/values/typeRepr.rs`)
- **Lint issues:** 1

### 257. int.i32

- **Target:** `int.I32 [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 4)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/i32.rs` vs expected `values/types/int/i32.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/i32.rs` (current: `// port-lint: source src/values/types/int/i32.rs`)
- **Lint issues:** 1

### 258. dict.traits

- **Target:** `dict.Traits [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 20606.7
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 6)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/traits.rs` vs expected `values/types/dict/traits.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/traits.rs` (current: `// port-lint: source src/values/types/dict/traits.rs`)
- **Lint issues:** 1

### 259. values.index

- **Target:** `values.Index [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 20605.4
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `test_convert_index`, `test_apply_slice`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/index.rs` vs expected `values/index.rs`
- **Proposed provenance header:** `// port-lint: source values/index.rs` (current: `// port-lint: source src/values/index.rs`)
- **Lint issues:** 1

### 260. intern.interner

- **Target:** `intern.Interner [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 1/3 matched (target 5)
- **Missing functions:** `test_intern`, `test_string_value_intern`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/intern/interner.rs` vs expected `values/types/string/intern/interner.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/intern/interner.rs` (current: `// port-lint: source src/values/types/string/intern/interner.rs`)
- **Lint issues:** 1

### 261. bc.definitely_assigned

- **Target:** `bc.DefinitelyAssigned [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 2/4 matched (target 7)
- **Missing functions:** `new`, `assert_smaller_then`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/definitelyAssigned.rs` vs expected `eval/bc/definitely_assigned.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/definitely_assigned.rs` (current: `// port-lint: source src/eval/bc/definitelyAssigned.rs`)
- **Lint issues:** 1

### 262. funcs.min_max

- **Target:** `funcs.MinMax [PROVENANCE-FALLBACK]`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 20505.7
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `max`, `min`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/minMax.rs` vs expected `stdlib/funcs/min_max.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/min_max.rs` (current: `// port-lint: source src/stdlib/funcs/minMax.rs`)
- **Lint issues:** 1

### 263. typing.any

- **Target:** `typing.Any [PROVENANCE-FALLBACK]`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 20501.8
- **Functions:** 2/4 matched (target 6)
- **Missing functions:** `test_any_runtime`, `test_any_compile_time`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/any.rs` vs expected `values/typing/any.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/any.rs` (current: `// port-lint: source src/values/typing/any.rs`)
- **Lint issues:** 1

### 264. module.return_impl

- **Target:** `module.ReturnImpl [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20410.0
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `func`, `attr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/returnImpl.rs` vs expected `tests/derive/module/return_impl.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/return_impl.rs` (current: `// port-lint: source src/tests/derive/module/returnImpl.rs`)
- **Lint issues:** 1

### 265. module.kwargs

- **Target:** `module.Kwargs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 20407.2
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `pos_kwargs`, `pos_named_kwargs`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/kwargs.rs` vs expected `tests/derive/module/kwargs.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/kwargs.rs` (current: `// port-lint: source src/tests/derive/module/kwargs.rs`)
- **Lint issues:** 1

### 266. collections.maybe_uninit_backport

- **Target:** `collections.MaybeUninitBackport [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 20406.9
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Guard`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/maybeUninitBackport.rs` vs expected `collections/maybe_uninit_backport.rs`
- **Proposed provenance header:** `// port-lint: source collections/maybe_uninit_backport.rs` (current: `// port-lint: source src/collections/maybeUninitBackport.rs`)
- **Lint issues:** 1

### 267. stdlib.internal

- **Target:** `stdlib.Internal [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 20406.0
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `ty_of_value_debug`, `test_ty_of_value_debug`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/internal.rs` vs expected `stdlib/internal.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/internal.rs` (current: `// port-lint: source src/stdlib/internal.rs`)
- **Lint issues:** 1

### 268. enumeration.ty_enum_type

- **Target:** `enumeration.TyEnumType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20310.0
- **Functions:** 0/2 matched (target 3)
- **Missing functions:** `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/tyEnumType.rs` vs expected `values/types/enumeration/ty_enum_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/ty_enum_type.rs` (current: `// port-lint: source src/values/types/enumeration/tyEnumType.rs`)
- **Lint issues:** 1

### 269. heap.maybe_uninit_slice_util

- **Target:** `heap.MaybeUninitSliceUtil [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 20306.6
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `WriteRemOnDrop`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/maybeUninitSliceUtil.rs` vs expected `values/layout/heap/maybe_uninit_slice_util.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/maybe_uninit_slice_util.rs` (current: `// port-lint: source src/values/layout/heap/maybeUninitSliceUtil.rs`)
- **Lint issues:** 1

### 270. debug.adapter

- **Target:** `debug.Adapter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 13610.0
- **Functions:** 21/22 matched (target 23)
- **Missing functions:** `fmt`
- **Types:** 14/14 matched (target 29)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter.rs` vs expected `debug/adapter.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter.rs` (current: `// port-lint: source src/debug/adapter.rs`)
- **Lint issues:** 1

### 271. docs

- **Target:** `docs.Docs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12310.0
- **Functions:** 12/13 matched (target 16)
- **Missing functions:** `default`
- **Types:** 10/10 matched (target 15)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs.rs` vs expected `docs.rs`
- **Proposed provenance header:** `// port-lint: source docs.rs` (current: `// port-lint: source src/docs.rs`)
- **Lint issues:** 1

### 272. typing.basic

- **Target:** `typing.Basic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 12002.8
- **Functions:** 18/19 matched (target 20)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/basic.rs` vs expected `typing/basic.rs`
- **Proposed provenance header:** `// port-lint: source typing/basic.rs` (current: `// port-lint: source src/typing/basic.rs`)
- **Lint issues:** 1

### 273. compiler.def_inline

- **Target:** `compiler.DefInline [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11410.0
- **Functions:** 9/10 matched (target 9)
- **Missing functions:** `new`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/defInline.rs` vs expected `eval/compiler/def_inline.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def_inline.rs` (current: `// port-lint: source src/eval/compiler/defInline.rs`)
- **Lint issues:** 1

### 274. record.instance

- **Target:** `record.Instance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 11403.5
- **Functions:** 12/13 matched (target 17)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/instance.rs` vs expected `values/types/record/instance.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/instance.rs` (current: `// port-lint: source src/values/types/record/instance.rs`)
- **Lint issues:** 1

### 275. type_compiled.factory

- **Target:** `typecompiled.Factory [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 11100.7
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Result`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/factory.rs` vs expected `values/typing/type_compiled/factory.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/factory.rs` (current: `// port-lint: source src/values/typing/typeCompiled/factory.rs`)
- **Lint issues:** 1

### 276. bool.value

- **Target:** `bool.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 11005.1
- **Functions:** 8/9 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/value.rs` vs expected `values/types/bool/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/value.rs` (current: `// port-lint: source src/values/types/bool/value.rs`)
- **Lint issues:** 1

### 277. enumeration.value

- **Target:** `enumeration.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 11004.3
- **Functions:** 8/9 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/value.rs` vs expected `values/types/enumeration/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/value.rs` (current: `// port-lint: source src/values/types/enumeration/value.rs`)
- **Lint issues:** 3

### 278. namespace.typing

- **Target:** `namespace.Typing [PROVENANCE-FALLBACK]`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 11003.5
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace/typing.rs` vs expected `values/types/namespace/typing.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace/typing.rs` (current: `// port-lint: source src/values/types/namespace/typing.rs`)
- **Lint issues:** 1

### 279. tests.type_annot

- **Target:** `tests.TypeAnnot [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 10901.6
- **Functions:** 8/9 matched
- **Missing functions:** `test_only_globals_or_bultins_allowed`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/typeAnnot.rs` vs expected `tests/type_annot.rs`
- **Proposed provenance header:** `// port-lint: source tests/type_annot.rs` (current: `// port-lint: source src/tests/typeAnnot.rs`)
- **Lint issues:** 1

### 280. values.recursive_repr_or_json_guard

- **Target:** `values.RecursiveReprOrJsonGuard [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 10706.6
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `drop`
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/recursiveReprOrJsonGuard.rs` vs expected `values/recursive_repr_or_json_guard.rs`
- **Proposed provenance header:** `// port-lint: source values/recursive_repr_or_json_guard.rs` (current: `// port-lint: source src/values/recursiveReprOrJsonGuard.rs`)
- **Lint issues:** 1

### 281. profile.by_type

- **Target:** `profile.ByType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 10702.4
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `normalize_for_golden_tests`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/byType.rs` vs expected `values/layout/heap/profile/by_type.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/by_type.rs` (current: `// port-lint: source src/values/layout/heap/profile/byType.rs`)
- **Lint issues:** 1

### 282. module.methods

- **Target:** `module.Methods [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 10603.9
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `test_method`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/methods.rs` vs expected `tests/derive/module/methods.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/methods.rs` (current: `// port-lint: source src/tests/derive/module/methods.rs`)
- **Lint issues:** 1

### 283. compiler.if_compiler

- **Target:** `compiler.IfCompiler [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 10602.3
- **Functions:** 5/6 matched (target 5)
- **Missing functions:** `wr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/ifCompiler.rs` vs expected `eval/bc/compiler/if_compiler.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/if_compiler.rs` (current: `// port-lint: source src/eval/bc/compiler/ifCompiler.rs`)
- **Lint issues:** 1

### 284. eval.bc.compiler.call

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10510.0
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `mark_definitely_assigned_after`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/call.rs` vs expected `eval/bc/compiler/call.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/call.rs` (current: `// port-lint: source src/eval/bc/compiler/call.rs`)
- **Lint issues:** 1

### 285. types.unbound

- **Target:** `types.Unbound [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 10504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/unbound.rs` vs expected `values/types/unbound.rs`
- **Proposed provenance header:** `// port-lint: source values/types/unbound.rs` (current: `// port-lint: source src/values/types/unbound.rs`)
- **Lint issues:** 1

### 286. structs.structs

- **Target:** `structs.Structs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 10504.0
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `r#struct`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/structs.rs` vs expected `values/types/structs/structs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/structs.rs` (current: `// port-lint: source src/values/types/structs/structs.rs`)
- **Lint issues:** 1

### 287. module.type_annotation

- **Target:** `module.TypeAnnotation [PROVENANCE-FALLBACK]`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 10405.7
- **Functions:** 2/3 matched
- **Missing functions:** `foo`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/typeAnnotation.rs` vs expected `tests/derive/module/type_annotation.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/type_annotation.rs` (current: `// port-lint: source src/tests/derive/module/typeAnnotation.rs`)
- **Lint issues:** 1

### 288. analysis.find_call_name

- **Target:** `analysis.FindCallName [PROVENANCE-FALLBACK]`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 10404.7
- **Functions:** 2/3 matched (target 8)
- **Missing functions:** `finds_function_calls_with_name_kwarg`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/findCallName.rs` vs expected `analysis/find_call_name.rs`
- **Proposed provenance header:** `// port-lint: source analysis/find_call_name.rs` (current: `// port-lint: source src/analysis/findCallName.rs`)
- **Lint issues:** 1

### 289. freeze.validator

- **Target:** `freeze.Validator [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 10401.6
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 2)
- **Missing types:** `Test`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/validator.rs` vs expected `tests/derive/freeze/validator.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/validator.rs` (current: `// port-lint: source src/tests/derive/freeze/validator.rs`)
- **Lint issues:** 2

### 290. dict.globals

- **Target:** `dict.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10310.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `dict`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/globals.rs` vs expected `values/types/dict/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/globals.rs` (current: `// port-lint: source src/values/types/dict/globals.rs`)
- **Lint issues:** 1

### 291. module.default_value

- **Target:** `module.DefaultValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 10305.5
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `foo`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/defaultValue.rs` vs expected `tests/derive/module/default_value.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/default_value.rs` (current: `// port-lint: source src/tests/derive/module/defaultValue.rs`)
- **Lint issues:** 1

### 292. module.special_params

- **Target:** `module.SpecialParams [PROVENANCE-FALLBACK]`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 10305.3
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `non_standard_heap_name`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/specialParams.rs` vs expected `tests/derive/module/special_params.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/special_params.rs` (current: `// port-lint: source src/tests/derive/module/specialParams.rs`)
- **Lint issues:** 1

### 293. compiler.assign_modify

- **Target:** `compiler.AssignModify [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 10301.2
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `AssignOnWriteBc`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/assignModify.rs` vs expected `eval/bc/compiler/assign_modify.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/assign_modify.rs` (current: `// port-lint: source src/eval/bc/compiler/assignModify.rs`)
- **Lint issues:** 1

### 294. runtime.visit_span

- **Target:** `runtime.VisitSpan [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 19)
- **Missing functions:** `visit_spans`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/visitSpan.rs` vs expected `eval/runtime/visit_span.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/visit_span.rs` (current: `// port-lint: source src/eval/runtime/visitSpan.rs`)
- **Lint issues:** 1

### 295. pagable.error

- **Target:** `pagable.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched
- **Missing functions:** `from`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pagable/error.rs` vs expected `pagable/error.rs`
- **Proposed provenance header:** `// port-lint: source pagable/error.rs` (current: `// port-lint: source src/pagable/error.rs`)
- **Lint issues:** 1

### 296. float.globals

- **Target:** `float.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `float`
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/globals.rs` vs expected `values/types/float/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/globals.rs` (current: `// port-lint: source src/values/types/float/globals.rs`)
- **Lint issues:** 1

### 297. bool.globals

- **Target:** `bool.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 10207.9
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `bool`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/globals.rs` vs expected `values/types/bool/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/globals.rs` (current: `// port-lint: source src/values/types/bool/globals.rs`)
- **Lint issues:** 1

### 298. int.globals

- **Target:** `int.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 10207.7
- **Functions:** 1/2 matched
- **Missing functions:** `int`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/globals.rs` vs expected `values/types/int/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/globals.rs` (current: `// port-lint: source src/values/types/int/globals.rs`)
- **Lint issues:** 1

### 299. bool.type_repr

- **Target:** `bool.TypeRepr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 10207.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/typeRepr.rs` vs expected `values/types/bool/type_repr.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/type_repr.rs` (current: `// port-lint: source src/values/types/bool/typeRepr.rs`)
- **Lint issues:** 1

### 300. tuple.globals

- **Target:** `tuple.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 10207.1
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `tuple`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/globals.rs` vs expected `values/types/tuple/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/globals.rs` (current: `// port-lint: source src/values/types/tuple/globals.rs`)
- **Lint issues:** 1

### 301. range.globals

- **Target:** `range.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 10207.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `range`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/range/globals.rs` vs expected `values/types/range/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/range/globals.rs` (current: `// port-lint: source src/values/types/range/globals.rs`)
- **Lint issues:** 1

### 302. namespace.globals

- **Target:** `namespace.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 10206.9
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `namespace`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace/globals.rs` vs expected `values/types/namespace/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace/globals.rs` (current: `// port-lint: source src/values/types/namespace/globals.rs`)
- **Lint issues:** 1

### 303. num.globals

- **Target:** `num.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 10206.8
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `abs`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num/globals.rs` vs expected `values/types/num/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num/globals.rs` (current: `// port-lint: source src/values/types/num/globals.rs`)
- **Lint issues:** 1

### 304. bool.unpack

- **Target:** `bool.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.94
- **Dependents:** 0
- **Priority Score:** 10200.6
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/unpack.rs` vs expected `values/types/bool/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/unpack.rs` (current: `// port-lint: source src/values/types/bool/unpack.rs`)
- **Lint issues:** 1

### 305. freeze.basic

- **Target:** `freeze.Basic [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `TestUnitStruct`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/basic.rs` vs expected `tests/derive/freeze/basic.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/basic.rs` (current: `// port-lint: source src/tests/derive/freeze/basic.rs`)
- **Lint issues:** 1

### 306. bc.writer

- **Target:** `bc.Writer [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 4610.0
- **Functions:** 42/42 matched (target 44)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/writer.rs` vs expected `eval/bc/writer.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/writer.rs` (current: `// port-lint: source src/eval/bc/writer.rs`)
- **Lint issues:** 1

### 307. typing.fill_types_for_lint

- **Target:** `typing.FillTypesForLint [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 4210.0
- **Functions:** 39/39 matched (target 40)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/fillTypesForLint.rs` vs expected `typing/fill_types_for_lint.rs`
- **Proposed provenance header:** `// port-lint: source typing/fill_types_for_lint.rs` (current: `// port-lint: source src/typing/fillTypesForLint.rs`)
- **Lint issues:** 4

### 308. oracle.ctx

- **Target:** `oracle.Ctx [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 3410.0
- **Functions:** 32/32 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 14)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/oracle/ctx.rs` vs expected `typing/oracle/ctx.rs`
- **Proposed provenance header:** `// port-lint: source typing/oracle/ctx.rs` (current: `// port-lint: source src/typing/oracle/ctx.rs`)
- **Lint issues:** 1

### 309. type_compiled.alloc

- **Target:** `typecompiled.Alloc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.90
- **Dependents:** 0
- **Priority Score:** 2901.0
- **Functions:** 28/28 matched (target 37)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/alloc.rs` vs expected `values/typing/type_compiled/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/alloc.rs` (current: `// port-lint: source src/values/typing/typeCompiled/alloc.rs`)
- **Lint issues:** 1

### 310. type_compiled.matchers

- **Target:** `typecompiled.Matchers [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2610.0
- **Functions:** 3/3 matched (target 25)
- **Missing functions:** _none_
- **Types:** 23/23 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/matchers.rs` vs expected `values/typing/type_compiled/matchers.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/matchers.rs` (current: `// port-lint: source src/values/typing/typeCompiled/matchers.rs`)
- **Lint issues:** 1

### 311. typing.ctx

- **Target:** `typing.Ctx [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2010.0
- **Functions:** 19/19 matched (target 20)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/ctx.rs` vs expected `typing/ctx.rs`
- **Proposed provenance header:** `// port-lint: source typing/ctx.rs` (current: `// port-lint: source src/typing/ctx.rs`)
- **Lint issues:** 2

### 312. docs.markdown

- **Target:** `docs.Markdown [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2010.0
- **Functions:** 18/18 matched (target 19)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/markdown.rs` vs expected `docs/markdown.rs`
- **Proposed provenance header:** `// port-lint: source docs/markdown.rs` (current: `// port-lint: source src/docs/markdown.rs`)
- **Lint issues:** 1

### 313. scope.tests

- **Target:** `scope.Tests [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 1701.2
- **Functions:** 16/16 matched (target 23)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope/tests.rs` vs expected `eval/compiler/scope/tests.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope/tests.rs` (current: `// port-lint: source src/eval/compiler/scope/tests.rs`)
- **Lint issues:** 1

### 314. tests.fstring

- **Target:** `tests.Fstring [PROVENANCE-FALLBACK]`
- **Similarity:** 0.97
- **Dependents:** 0
- **Priority Score:** 1700.3
- **Functions:** 17/17 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 14/14 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/fstring.rs` vs expected `tests/fstring.rs`
- **Proposed provenance header:** `// port-lint: source tests/fstring.rs` (current: `// port-lint: source src/tests/fstring.rs`)
- **Lint issues:** 1

### 315. environment.names

- **Target:** `environment.Names [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1510.0
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/names.rs` vs expected `environment/names.rs`
- **Proposed provenance header:** `// port-lint: source environment/names.rs` (current: `// port-lint: source src/environment/names.rs`)
- **Lint issues:** 1

### 316. eval.bc.compiler.expr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1510.0
- **Functions:** 15/15 matched (target 16)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/expr.rs` vs expected `eval/bc/compiler/expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/expr.rs` (current: `// port-lint: source src/eval/bc/compiler/expr.rs`)
- **Lint issues:** 1

### 317. typing.error

- **Target:** `typing.Error [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 9/9 matched (target 23)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/error.rs` vs expected `typing/error.rs`
- **Proposed provenance header:** `// port-lint: source typing/error.rs` (current: `// port-lint: source src/typing/error.rs`)
- **Lint issues:** 1

### 318. compiler.call

- **Target:** `compiler.Call [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 1402.5
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/call.rs` vs expected `eval/compiler/call.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/call.rs` (current: `// port-lint: source src/eval/compiler/call.rs`)
- **Lint issues:** 1

### 319. type_compiled.tests

- **Target:** `typecompiled.Tests [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1310.0
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 12/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/tests.rs` vs expected `values/typing/type_compiled/tests.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/tests.rs` (current: `// port-lint: source src/values/typing/typeCompiled/tests.rs`)
- **Lint issues:** 1

### 320. profile.tests

- **Target:** `profile.Tests [PROVENANCE-FALLBACK]`
- **Similarity:** 0.96
- **Dependents:** 0
- **Priority Score:** 1300.4
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 12/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/tests.rs` vs expected `eval/runtime/profile/tests.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/tests.rs` (current: `// port-lint: source src/eval/runtime/profile/tests.rs`)
- **Lint issues:** 1

### 321. compiler.compr

- **Target:** `compiler.Compr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1210.0
- **Functions:** 9/9 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/compr.rs` vs expected `eval/compiler/compr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/compr.rs` (current: `// port-lint: source src/eval/compiler/compr.rs`)
- **Lint issues:** 1

### 322. bc.if_stmt

- **Target:** `bc.IfStmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.98
- **Dependents:** 0
- **Priority Score:** 1200.2
- **Functions:** 12/12 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 12/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/ifStmt.rs` vs expected `tests/bc/if_stmt.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/if_stmt.rs` (current: `// port-lint: source src/tests/bc/ifStmt.rs`)
- **Lint issues:** 1

### 323. environment.slots

- **Target:** `environment.Slots [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1103.4
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/slots.rs` vs expected `environment/slots.rs`
- **Proposed provenance header:** `// port-lint: source environment/slots.rs` (current: `// port-lint: source src/environment/slots.rs`)
- **Lint issues:** 1

### 324. docs.multipage

- **Target:** `docs.Multipage [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 1101.4
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/multipage.rs` vs expected `docs/multipage.rs`
- **Proposed provenance header:** `// port-lint: source docs/multipage.rs` (current: `// port-lint: source src/docs/multipage.rs`)
- **Lint issues:** 1

### 325. tests.comprehension

- **Target:** `tests.Comprehension [PROVENANCE-FALLBACK]`
- **Similarity:** 0.96
- **Dependents:** 0
- **Priority Score:** 1000.4
- **Functions:** 10/10 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/comprehension.rs` vs expected `tests/comprehension.rs`
- **Proposed provenance header:** `// port-lint: source tests/comprehension.rs` (current: `// port-lint: source src/tests/comprehension.rs`)
- **Lint issues:** 1

### 326. compiler.types

- **Target:** `compiler.Types [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 910.0
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/types.rs` vs expected `eval/compiler/types.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/types.rs` (current: `// port-lint: source src/eval/compiler/types.rs`)
- **Lint issues:** 1

### 327. typing.tests.call

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.typing.tests.Call [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 902.9
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests/call.rs` vs expected `typing/tests/call.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/call.rs` (current: `// port-lint: source src/typing/tests/call.rs`)
- **Lint issues:** 1

### 328. opt.def_inline

- **Target:** `opt.DefInline [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 810.0
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/defInline.rs` vs expected `tests/opt/def_inline.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/def_inline.rs` (current: `// port-lint: source src/tests/opt/defInline.rs`)
- **Lint issues:** 1

### 329. __derive_refs.parse_args

- **Target:** `deriverefs.ParseArgs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 802.8
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/parseArgs.rs` vs expected `__derive_refs/parse_args.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/parse_args.rs` (current: `// port-lint: source src/__derive_refs/parseArgs.rs`)
- **Lint issues:** 1

### 330. tests.opt

- **Target:** `tests.Opt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 800.1
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt.rs` vs expected `tests/opt.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt.rs` (current: `// port-lint: source src/tests/opt.rs`)
- **Lint issues:** 1

### 331. bc.and_or

- **Target:** `bc.AndOr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 800.1
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/andOr.rs` vs expected `tests/bc/and_or.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/and_or.rs` (current: `// port-lint: source src/tests/bc/andOr.rs`)
- **Lint issues:** 1

### 332. compiler.module

- **Target:** `compiler.Module [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/module.rs` vs expected `eval/compiler/module.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/module.rs` (current: `// port-lint: source src/eval/compiler/module.rs`)
- **Lint issues:** 1

### 333. types.known_methods

- **Target:** `types.KnownMethods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/knownMethods.rs` vs expected `values/types/known_methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/known_methods.rs` (current: `// port-lint: source src/values/types/knownMethods.rs`)
- **Lint issues:** 1

### 334. docs.code

- **Target:** `docs.Code [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 703.1
- **Functions:** 7/7 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/code.rs` vs expected `docs/code.rs`
- **Proposed provenance header:** `// port-lint: source docs/code.rs` (current: `// port-lint: source src/docs/code.rs`)
- **Lint issues:** 1

### 335. alloc.per_thread

- **Target:** `alloc.PerThread [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 702.9
- **Functions:** 6/6 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/perThread.rs` vs expected `values/layout/heap/allocator/alloc/per_thread.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/perThread.rs` vs expected `values/layout/heap/allocator/alloc/per_thread.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/per_thread.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/perThread.rs`)
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/per_thread.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/perThread.rs`)
- **Lint issues:** 2

### 336. layout.value_not_special

- **Target:** `layout.ValueNotSpecial [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 702.8
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/valueNotSpecial.rs` vs expected `values/layout/value_not_special.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_not_special.rs` (current: `// port-lint: source src/values/layout/valueNotSpecial.rs`)
- **Lint issues:** 1

### 337. unused_loads.find

- **Target:** `unusedloads.Find [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 702.1
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unusedLoads/find.rs` vs expected `analysis/unused_loads/find.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/find.rs` (current: `// port-lint: source src/analysis/unusedLoads/find.rs`)
- **Lint issues:** 1

### 338. runtime.before_stmt

- **Target:** `runtime.BeforeStmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 701.4
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/beforeStmt.rs` vs expected `eval/runtime/before_stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/before_stmt.rs` (current: `// port-lint: source src/eval/runtime/beforeStmt.rs`)
- **Lint issues:** 1

### 339. unused_loads.find_tests

- **Target:** `unusedloads.FindTestsTest [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 700.7
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unusedLoads/findTests.rs` vs expected `analysis/unused_loads/find_tests.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/find_tests.rs` (current: `// port-lint: source src/analysis/unusedLoads/findTests.rs`)
- **Lint issues:** 1

### 340. opt.eq

- **Target:** `opt.Eq [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 700.1
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/eq.rs` vs expected `tests/opt/eq.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/eq.rs` (current: `// port-lint: source src/tests/opt/eq.rs`)
- **Lint issues:** 1

### 341. assert.conformance

- **Target:** `assert.Conformance [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/assert/conformance.rs` vs expected `assert/conformance.rs`
- **Proposed provenance header:** `// port-lint: source assert/conformance.rs` (current: `// port-lint: source src/assert/conformance.rs`)
- **Lint issues:** 1

### 342. layout.static_string

- **Target:** `layout.StaticString [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 603.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/staticString.rs` vs expected `values/layout/static_string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/static_string.rs` (current: `// port-lint: source src/values/layout/staticString.rs`)
- **Lint issues:** 1

### 343. tests.list

- **Target:** `tests.List [PROVENANCE-FALLBACK]`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 602.2
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests/list.rs` vs expected `typing/tests/list.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/list.rs` (current: `// port-lint: source src/typing/tests/list.rs`)
- **Lint issues:** 1

### 344. int.tests

- **Target:** `int.Tests [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 602.0
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/tests.rs` vs expected `values/types/int/tests.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/tests.rs` (current: `// port-lint: source src/values/types/int/tests.rs`)
- **Lint issues:** 1

### 345. freeze.identity

- **Target:** `freeze.Identity [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 601.1
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 7)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/identity.rs` vs expected `tests/derive/freeze/identity.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/identity.rs` (current: `// port-lint: source src/tests/derive/freeze/identity.rs`)
- **Lint issues:** 5

### 346. runtime.slots

- **Target:** `runtime.Slots [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/slots.rs` vs expected `eval/runtime/slots.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/slots.rs` (current: `// port-lint: source src/eval/runtime/slots.rs`)
- **Lint issues:** 1

### 347. string.globals

- **Target:** `string.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 502.9
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/globals.rs` vs expected `values/types/string/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/globals.rs` (current: `// port-lint: source src/values/types/string/globals.rs`)
- **Lint issues:** 1

### 348. values.comparison

- **Target:** `values.Comparison [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 502.1
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/comparison.rs` vs expected `values/comparison.rs`
- **Proposed provenance header:** `// port-lint: source values/comparison.rs` (current: `// port-lint: source src/values/comparison.rs`)
- **Lint issues:** 1

### 349. funcs.zip

- **Target:** `funcs.Zip [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 502.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/zip.rs` vs expected `stdlib/funcs/zip.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/zip.rs` (current: `// port-lint: source src/stdlib/funcs/zip.rs`)
- **Lint issues:** 1

### 350. compiler.expr_bool

- **Target:** `compiler.ExprBool [PROVENANCE-FALLBACK]`
- **Similarity:** 0.87
- **Dependents:** 0
- **Priority Score:** 501.3
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/exprBool.rs` vs expected `eval/compiler/expr_bool.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/expr_bool.rs` (current: `// port-lint: source src/eval/compiler/exprBool.rs`)
- **Lint issues:** 1

### 351. num.typecheck

- **Target:** `num.Typecheck [PROVENANCE-FALLBACK]`
- **Similarity:** 0.90
- **Dependents:** 0
- **Priority Score:** 501.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num/typecheck.rs` vs expected `values/types/num/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num/typecheck.rs` (current: `// port-lint: source src/values/types/num/typecheck.rs`)
- **Lint issues:** 1

### 352. unused_loads.remove

- **Target:** `unusedloads.Remove [PROVENANCE-FALLBACK]`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 500.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unusedLoads/remove.rs` vs expected `analysis/unused_loads/remove.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/remove.rs` (current: `// port-lint: source src/analysis/unusedLoads/remove.rs`)
- **Lint issues:** 1

### 353. tests.bc.definitely_assigned

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.tests.bc.DefinitelyAssigned [PROVENANCE-FALLBACK]`
- **Similarity:** 0.97
- **Dependents:** 0
- **Priority Score:** 500.3
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/definitelyAssigned.rs` vs expected `tests/bc/definitely_assigned.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/definitely_assigned.rs` (current: `// port-lint: source src/tests/bc/definitelyAssigned.rs`)
- **Lint issues:** 1

### 354. trace.bounds

- **Target:** `trace.Bounds [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace/bounds.rs` vs expected `tests/derive/trace/bounds.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace/bounds.rs` (current: `// port-lint: source src/tests/derive/trace/bounds.rs`)
- **Lint issues:** 1

### 355. tests.util

- **Target:** `tests.Util [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/util.rs` vs expected `tests/util.rs`
- **Proposed provenance header:** `// port-lint: source tests/util.rs` (current: `// port-lint: source src/tests/util.rs`)
- **Lint issues:** 2

### 356. string.iter

- **Target:** `string.Iter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/iter.rs` vs expected `values/types/string/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/iter.rs` (current: `// port-lint: source src/values/types/string/iter.rs`)
- **Lint issues:** 1

### 357. __derive_refs.components

- **Target:** `deriverefs.Components [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 402.3
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/components.rs` vs expected `__derive_refs/components.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/components.rs` (current: `// port-lint: source src/__derive_refs/components.rs`)
- **Lint issues:** 1

### 358. unused_loads.remove_tests

- **Target:** `unusedloads.RemoveTestsTest [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 401.4
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unusedLoads/removeTests.rs` vs expected `analysis/unused_loads/remove_tests.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/remove_tests.rs` (current: `// port-lint: source src/analysis/unusedLoads/removeTests.rs`)
- **Lint issues:** 1

### 359. __derive_refs.sig

- **Target:** `deriverefs.Sig [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 401.4
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/sig.rs` vs expected `__derive_refs/sig.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/sig.rs` (current: `// port-lint: source src/__derive_refs/sig.rs`)
- **Lint issues:** 1

### 360. bc.compr

- **Target:** `bc.Compr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 400.1
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/compr.rs` vs expected `tests/bc/compr.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/compr.rs` (current: `// port-lint: source src/tests/bc/compr.rs`)
- **Lint issues:** 1

### 361. derive.attrs

- **Target:** `derive.Attrs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 1/1 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/attrs.rs` vs expected `tests/derive/attrs.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/attrs.rs` (current: `// port-lint: source src/tests/derive/attrs.rs`)
- **Lint issues:** 1

### 362. tests.go

- **Target:** `tests.Go [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/go.rs` vs expected `tests/go.rs`
- **Proposed provenance header:** `// port-lint: source tests/go.rs` (current: `// port-lint: source src/tests/go.rs`)
- **Lint issues:** 1

### 363. eval.soft_error

- **Target:** `eval.SoftError [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 304.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/softError.rs` vs expected `eval/soft_error.rs`
- **Proposed provenance header:** `// port-lint: source eval/soft_error.rs` (current: `// port-lint: source src/eval/softError.rs`)
- **Lint issues:** 2

### 364. oracle.traits

- **Target:** `oracle.Traits [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 304.1
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/oracle/traits.rs` vs expected `typing/oracle/traits.rs`
- **Proposed provenance header:** `// port-lint: source typing/oracle/traits.rs` (current: `// port-lint: source src/typing/oracle/traits.rs`)
- **Lint issues:** 1

### 365. compiler.error

- **Target:** `compiler.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 303.8
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/error.rs` vs expected `eval/compiler/error.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source src/eval/compiler/error.rs`)
- **Lint issues:** 1

### 366. tests.types

- **Target:** `tests.Types [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 302.6
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests/types.rs` vs expected `typing/tests/types.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/types.rs` (current: `// port-lint: source src/typing/tests/types.rs`)
- **Lint issues:** 1

### 367. callable.param

- **Target:** `callable.Param [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 302.4
- **Functions:** 1/1 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/callable/param.rs` vs expected `values/typing/callable/param.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/callable/param.rs` (current: `// port-lint: source src/values/typing/callable/param.rs`)
- **Lint issues:** 1

### 368. compiler.type_expr

- **Target:** `compiler.TypeExpr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 301.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/typeExpr.rs` vs expected `eval/compiler/type_expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source src/eval/compiler/typeExpr.rs`)
- **Lint issues:** 1

### 369. bc.for_stmt

- **Target:** `bc.ForStmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.97
- **Dependents:** 0
- **Priority Score:** 300.3
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/forStmt.rs` vs expected `tests/bc/for_stmt.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/for_stmt.rs` (current: `// port-lint: source src/tests/bc/forStmt.rs`)
- **Lint issues:** 1

### 370. opt.speculative_exec

- **Target:** `opt.SpeculativeExec [PROVENANCE-FALLBACK]`
- **Similarity:** 0.98
- **Dependents:** 0
- **Priority Score:** 300.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/speculativeExec.rs` vs expected `tests/opt/speculative_exec.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/speculative_exec.rs` (current: `// port-lint: source src/tests/opt/speculativeExec.rs`)
- **Lint issues:** 1

### 371. eval.bc.compiler.compr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/compr.rs` vs expected `eval/bc/compiler/compr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/compr.rs` (current: `// port-lint: source src/eval/bc/compiler/compr.rs`)
- **Lint issues:** 1

### 372. trace.statics

- **Target:** `trace.Statics [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace/statics.rs` vs expected `tests/derive/trace/statics.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace/statics.rs` (current: `// port-lint: source src/tests/derive/trace/statics.rs`)
- **Lint issues:** 1

### 373. derive.unpack_value_attr

- **Target:** `derive.UnpackValueAttr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/unpackValueAttr.rs` vs expected `tests/derive/unpack_value_attr.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/unpack_value_attr.rs` (current: `// port-lint: source src/tests/derive/unpackValueAttr.rs`)
- **Lint issues:** 1

### 374. compiler.assign

- **Target:** `compiler.Assign [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/assign.rs` vs expected `eval/bc/compiler/assign.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/assign.rs` (current: `// port-lint: source src/eval/bc/compiler/assign.rs`)
- **Lint issues:** 1

### 375. bc.instr

- **Target:** `bc.Instr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instr.rs` vs expected `eval/bc/instr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr.rs` (current: `// port-lint: source src/eval/bc/instr.rs`)
- **Lint issues:** 1

### 376. bc.slow_arg

- **Target:** `bc.SlowArg [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/slowArg.rs` vs expected `eval/bc/slow_arg.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/slow_arg.rs` (current: `// port-lint: source src/eval/bc/slowArg.rs`)
- **Lint issues:** 1

### 377. eval.bc.compiler.def

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/def.rs` vs expected `eval/bc/compiler/def.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/def.rs` (current: `// port-lint: source src/eval/bc/compiler/def.rs`)
- **Lint issues:** 1

### 378. allocator.api

- **Target:** `allocator.Api [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/api.rs` vs expected `values/layout/heap/allocator/api.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/api.rs` (current: `// port-lint: source src/values/layout/heap/allocator/api.rs`)
- **Lint issues:** 1

### 379. eval

- **Target:** `eval.Eval [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval.rs` vs expected `eval.rs`
- **Proposed provenance header:** `// port-lint: source eval.rs` (current: `// port-lint: source src/eval.rs`)
- **Lint issues:** 1

### 380. __derive_refs.invoke_macro_error

- **Target:** `deriverefs.InvokeMacroError [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 206.7
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/invokeMacroError.rs` vs expected `__derive_refs/invoke_macro_error.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/invoke_macro_error.rs` (current: `// port-lint: source src/__derive_refs/invokeMacroError.rs`)
- **Lint issues:** 1

### 381. tests.callable

- **Target:** `tests.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 203.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests/callable.rs` vs expected `typing/tests/callable.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/callable.rs` (current: `// port-lint: source src/typing/tests/callable.rs`)
- **Lint issues:** 1

### 382. tests.tuple

- **Target:** `tests.Tuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 203.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests/tuple.rs` vs expected `typing/tests/tuple.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/tuple.rs` (current: `// port-lint: source src/typing/tests/tuple.rs`)
- **Lint issues:** 1

### 383. bc.golden

- **Target:** `bc.Golden [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 202.9
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/golden.rs` vs expected `tests/bc/golden.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/golden.rs` (current: `// port-lint: source src/tests/bc/golden.rs`)
- **Lint issues:** 1

### 384. tests.special_function

- **Target:** `tests.SpecialFunction [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 202.3
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tests/specialFunction.rs` vs expected `typing/tests/special_function.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/special_function.rs` (current: `// port-lint: source src/typing/tests/specialFunction.rs`)
- **Lint issues:** 1

### 385. typing.macro_refs

- **Target:** `typing.MacroRefs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 202.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/macroRefs.rs` vs expected `values/typing/macro_refs.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/macro_refs.rs` (current: `// port-lint: source src/values/typing/macroRefs.rs`)
- **Lint issues:** 1

### 386. enumeration.matcher

- **Target:** `enumeration.Matcher [PROVENANCE-FALLBACK]`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 201.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/matcher.rs` vs expected `values/types/enumeration/matcher.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/matcher.rs` (current: `// port-lint: source src/values/types/enumeration/matcher.rs`)
- **Lint issues:** 1

### 387. typing.macro_support

- **Target:** `typing.MacroSupport [PROVENANCE-FALLBACK]`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 201.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/macroSupport.rs` vs expected `typing/macro_support.rs`
- **Proposed provenance header:** `// port-lint: source typing/macro_support.rs` (current: `// port-lint: source src/typing/macroSupport.rs`)
- **Lint issues:** 1

### 388. layout.identity

- **Target:** `layout.Identity [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 201.4
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/identity.rs` vs expected `values/layout/identity.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/identity.rs` (current: `// port-lint: source src/values/layout/identity.rs`)
- **Lint issues:** 1

### 389. opt.constant_folding

- **Target:** `opt.ConstantFolding [PROVENANCE-FALLBACK]`
- **Similarity:** 0.87
- **Dependents:** 0
- **Priority Score:** 201.3
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/constantFolding.rs` vs expected `tests/opt/constant_folding.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/constant_folding.rs` (current: `// port-lint: source src/tests/opt/constantFolding.rs`)
- **Lint issues:** 1

### 390. record.matcher

- **Target:** `record.Matcher [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 201.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/matcher.rs` vs expected `values/types/record/matcher.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/matcher.rs` (current: `// port-lint: source src/values/types/record/matcher.rs`)
- **Lint issues:** 1

### 391. bool.alloc

- **Target:** `bool.Alloc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 200.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/alloc.rs` vs expected `values/types/bool/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/alloc.rs` (current: `// port-lint: source src/values/types/bool/alloc.rs`)
- **Lint issues:** 1

### 392. opt.list_add

- **Target:** `opt.ListAdd [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 200.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/listAdd.rs` vs expected `tests/opt/list_add.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/list_add.rs` (current: `// port-lint: source src/tests/opt/listAdd.rs`)
- **Lint issues:** 1

### 393. opt.types

- **Target:** `opt.Types [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 200.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/types.rs` vs expected `tests/opt/types.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/types.rs` (current: `// port-lint: source src/tests/opt/types.rs`)
- **Lint issues:** 1

### 394. bc.for_loop

- **Target:** `bc.ForLoop [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/forLoop.rs` vs expected `eval/bc/for_loop.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/for_loop.rs` (current: `// port-lint: source src/eval/bc/forLoop.rs`)
- **Lint issues:** 1

### 395. typing.call_args

- **Target:** `typing.CallArgs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callArgs.rs` vs expected `typing/call_args.rs`
- **Proposed provenance header:** `// port-lint: source typing/call_args.rs` (current: `// port-lint: source src/typing/callArgs.rs`)
- **Lint issues:** 1

### 396. environment

- **Target:** `starlark.Environment [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment.rs` vs expected `environment.rs`
- **Proposed provenance header:** `// port-lint: source environment.rs` (current: `// port-lint: source src/environment.rs`)
- **Lint issues:** 1

### 397. trace.enums

- **Target:** `trace.Enums [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace/enums.rs` vs expected `tests/derive/trace/enums.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace/enums.rs` (current: `// port-lint: source src/tests/derive/trace/enums.rs`)
- **Lint issues:** 1

### 398. typing.mode

- **Target:** `typing.Mode [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/mode.rs` vs expected `typing/mode.rs`
- **Proposed provenance header:** `// port-lint: source typing/mode.rs` (current: `// port-lint: source src/typing/mode.rs`)
- **Lint issues:** 1

### 399. none.globals

- **Target:** `none.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none/globals.rs` vs expected `values/types/none/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none/globals.rs` (current: `// port-lint: source src/values/types/none/globals.rs`)
- **Lint issues:** 1

### 400. freeze.enums

- **Target:** `freeze.Enums [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/enums.rs` vs expected `tests/derive/freeze/enums.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/enums.rs` (current: `// port-lint: source src/tests/derive/freeze/enums.rs`)
- **Lint issues:** 1

### 401. profile.or_instrumentation

- **Target:** `profile.OrInstrumentation [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/orInstrumentation.rs` vs expected `eval/runtime/profile/or_instrumentation.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/or_instrumentation.rs` (current: `// port-lint: source src/eval/runtime/profile/orInstrumentation.rs`)
- **Lint issues:** 1

### 402. wasm

- **Target:** `starlark.Wasm [PROVENANCE-FALLBACK]`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 107.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/wasm.rs` vs expected `wasm.rs`
- **Proposed provenance header:** `// port-lint: source wasm.rs` (current: `// port-lint: source src/wasm.rs`)
- **Lint issues:** 1

### 403. environment.module_dump

- **Target:** `environment.ModuleDump [PROVENANCE-FALLBACK]`
- **Similarity:** 0.48
- **Dependents:** 0
- **Priority Score:** 105.2
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/moduleDump.rs` vs expected `environment/module_dump.rs`
- **Proposed provenance header:** `// port-lint: source environment/module_dump.rs` (current: `// port-lint: source src/environment/moduleDump.rs`)
- **Lint issues:** 1

### 404. tests.replace_binary

- **Target:** `tests.ReplaceBinary [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 103.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/replaceBinary.rs` vs expected `tests/replace_binary.rs`
- **Proposed provenance header:** `// port-lint: source tests/replace_binary.rs` (current: `// port-lint: source src/tests/replaceBinary.rs`)
- **Lint issues:** 1

### 405. typing.globals

- **Target:** `typing.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 102.6
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/globals.rs` vs expected `values/typing/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/globals.rs` (current: `// port-lint: source src/values/typing/globals.rs`)
- **Lint issues:** 1

### 406. compiler.known

- **Target:** `compiler.Known [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 102.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/known.rs` vs expected `eval/compiler/known.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/known.rs` (current: `// port-lint: source src/eval/compiler/known.rs`)
- **Lint issues:** 1

### 407. eval.params

- **Target:** `eval.Params [PROVENANCE-FALLBACK]`
- **Similarity:** 0.91
- **Dependents:** 0
- **Priority Score:** 100.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/params.rs` vs expected `eval/params.rs`
- **Proposed provenance header:** `// port-lint: source eval/params.rs` (current: `// port-lint: source src/eval/params.rs`)
- **Lint issues:** 1

### 408. tests.for_loop

- **Target:** `tests.ForLoop [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/forLoop.rs` vs expected `tests/for_loop.rs`
- **Proposed provenance header:** `// port-lint: source tests/for_loop.rs` (current: `// port-lint: source src/tests/forLoop.rs`)
- **Lint issues:** 1

### 409. tests.bc.call

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.tests.bc.Call [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/call.rs` vs expected `tests/bc/call.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/call.rs` (current: `// port-lint: source src/tests/bc/call.rs`)
- **Lint issues:** 1

### 410. bc.isinstance

- **Target:** `bc.Isinstance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/bc/isinstance.rs` vs expected `tests/bc/isinstance.rs`
- **Proposed provenance header:** `// port-lint: source tests/bc/isinstance.rs` (current: `// port-lint: source src/tests/bc/isinstance.rs`)
- **Lint issues:** 1

### 411. funcs.globals

- **Target:** `funcs.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/globals.rs` vs expected `stdlib/funcs/globals.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/globals.rs` (current: `// port-lint: source src/stdlib/funcs/globals.rs`)
- **Lint issues:** 1

### 412. pagable.vtable_register

- **Target:** `pagable.VtableRegister [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pagable/vtableRegister.rs` vs expected `pagable/vtable_register.rs`
- **Proposed provenance header:** `// port-lint: source pagable/vtable_register.rs` (current: `// port-lint: source src/pagable/vtableRegister.rs`)
- **Lint issues:** 1

### 413. heap.branding

- **Target:** `heap.Branding [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/branding.rs` vs expected `values/layout/heap/branding.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/branding.rs` (current: `// port-lint: source src/values/layout/heap/branding.rs`)
- **Lint issues:** 1

### 414. macros

- **Target:** `starlark.Macros [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 9)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 9)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/macros.rs` vs expected `macros.rs`
- **Proposed provenance header:** `// port-lint: source macros.rs` (current: `// port-lint: source src/macros.rs`)
- **Lint issues:** 1

### 415. derive.trace

- **Target:** `derive.Trace [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace.rs` vs expected `tests/derive/trace.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace.rs` (current: `// port-lint: source src/tests/derive/trace.rs`)
- **Lint issues:** 1

### 416. derive.freeze

- **Target:** `derive.Freeze [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze.rs` vs expected `tests/derive/freeze.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze.rs` (current: `// port-lint: source src/tests/derive/freeze.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/starlark/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `layout.heap` | `values.layout.heap.Heap` | 109 | `values/layout/heap.rs` | `values/layout/heap/Heap.kt` |
| `assert` | `assert.Assert` | 84 | `assert.rs` | `assert/Assert.kt` |
| `debug` | `debug.Debug` | 53 | `debug.rs` | `debug/Debug.kt` |
| `typing.type_compiled` | `values.typing.typecompiled.TypeCompiled` | 22 | `values/typing/type_compiled.rs` | `values/typing/typecompiled/TypeCompiled.kt` |
| `derive.module` | `tests.derive.module.Module` | 20 | `tests/derive/module.rs` | `tests/derive/module/Module.kt` |
| `collections.symbol` | `collections.symbol.Symbol` | 15 | `collections/symbol.rs` | `collections/symbol/Symbol.kt` |
| `types.dict` | `values.types.dict.Dict` | 12 | `values/types/dict.rs` | `values/types/dict/Dict.kt` |
| `values.layout` | `values.layout.Layout` | 6 | `values/layout.rs` | `values/layout/Layout.kt` |
| `eval.bc` | `eval.bc.Bc` | 5 | `eval/bc.rs` | `eval/bc/Bc.kt` |
| `types.range` | `values.types.range.Range` | 5 | `values/types/range.rs` | `values/types/range/Range.kt` |
| `types.record` | `values.types.record.Record` | 3 | `values/types/record.rs` | `values/types/record/Record.kt` |
| `types.namespace` | `values.types.namespace.Namespace` | 3 | `values/types/namespace.rs` | `values/types/namespace/Namespace.kt` |
| `types.num` | `values.types.num.Num` | 2 | `values/types/num.rs` | `values/types/num/Num.kt` |
| `types.list` | `values.types.list.List` | 2 | `values/types/list.rs` | `values/types/list/List.kt` |
| `types.float` | `values.types.float.Float` | 2 | `values/types/float.rs` | `values/types/float/Float.kt` |
| `types.string` | `values.types.string.String` | 1 | `values/types/string.rs` | `values/types/string/String.kt` |
| `types.set` | `values.types.set.Set` | 1 | `values/types/set.rs` | `values/types/set/Set.kt` |
| `types.none` | `values.types.none.None` | 1 | `values/types/none.rs` | `values/types/none/None.kt` |
| `tests` | `tests.Tests` | 1 | `tests.rs` | `tests/Tests.kt` |
| `types.int` | `values.types.int.Int` | 1 | `values/types/int.rs` | `values/types/int/Int.kt` |
| `types.enumeration` | `values.types.enumeration.Enumeration` | 1 | `values/types/enumeration.rs` | `values/types/enumeration/Enumeration.kt` |
| `typing` | `typing.Typing` | 1 | `typing.rs` | `typing/Typing.kt` |
| `types.bool` | `values.types.bool.Bool` | 1 | `values/types/bool.rs` | `values/types/bool/Bool.kt` |
| `allocator.alloc` | `values.layout.heap.allocator.alloc.Alloc` | 0 | `values/layout/heap/allocator/alloc.rs` | `values/layout/heap/allocator/alloc/Alloc.kt` |
| `__derive_refs` | `deriverefs.DeriveRefs` | 0 | `__derive_refs.rs` | `deriverefs/DeriveRefs.kt` |
| `docs.tests` | `docs.tests.Tests` | 0 | `docs/tests.rs` | `docs/tests/Tests.kt` |
| `layout.avalues` | `values.layout.avalues.Avalues` | 0 | `values/layout/avalues.rs` | `values/layout/avalues/Avalues.kt` |
| `collections` | `collections.Collections` | 0 | `collections.rs` | `collections/Collections.kt` |
| `heap.allocator` | `values.layout.heap.allocator.Allocator` | 0 | `values/layout/heap/allocator.rs` | `values/layout/heap/allocator/Allocator.kt` |
| `util` | `util.Util` | 0 | `util.rs` | `util/Util.kt` |
| `heap.profile` | `values.layout.heap.profile.Profile` | 0 | `values/layout/heap/profile.rs` | `values/layout/heap/profile/Profile.kt` |
| `values.types` | `values.types.Types` | 0 | `values/types.rs` | `values/types/Types.kt` |
| `typing.oracle` | `typing.oracle.Oracle` | 0 | `typing/oracle.rs` | `typing/oracle/Oracle.kt` |
| `analysis.unused_loads` | `analysis.unusedloads.UnusedLoads` | 0 | `analysis/unused_loads.rs` | `analysis/unusedloads/UnusedLoads.kt` |
| `errors` | `errors.Errors` | 0 | `errors.rs` | `errors/Errors.kt` |
| `tests.derive` | `tests.derive.Derive` | 0 | `tests/derive.rs` | `tests/derive/Derive.kt` |
| `tests.bc` | `tests.bc.Bc` | 0 | `tests/bc.rs` | `tests/bc/Bc.kt` |
| `syntax` | `Syntax` | 0 | `syntax.rs` | `Syntax.kt` |
| `stdlib.funcs` | `stdlib.funcs.Funcs` | 0 | `stdlib/funcs.rs` | `stdlib/funcs/Funcs.kt` |
| `pagable` | `pagable.Pagable` | 0 | `pagable.rs` | `pagable/Pagable.kt` |
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |
| `runtime.profile` | `eval.runtime.profile.Profile` | 0 | `eval/runtime/profile.rs` | `eval/runtime/profile/Profile.kt` |
| `runtime.params` | `eval.runtime.params.Params` | 0 | `eval/runtime/params.rs` | `eval/runtime/params/Params.kt` |
| `eval.runtime` | `eval.runtime.Runtime` | 0 | `eval/runtime.rs` | `eval/runtime/Runtime.kt` |
| `bc.compiler` | `eval.bc.compiler.Compiler` | 0 | `eval/bc/compiler.rs` | `eval/bc/compiler/Compiler.kt` |
| `string.intern` | `values.types.string.intern.Intern` | 0 | `values/types/string/intern.rs` | `values/types/string/intern/Intern.kt` |
| `types.structs` | `values.types.structs.Structs` | 0 | `values/types/structs.rs` | `values/types/structs/Structs.kt` |
| `types.tuple` | `values.types.tuple.Tuple` | 0 | `values/types/tuple.rs` | `values/types/tuple/Tuple.kt` |
| `values.typing` | `values.typing.Typing` | 0 | `values/typing.rs` | `values/typing/Typing.kt` |
| `values` | `values.Values` | 0 | `values.rs` | `values/Values.kt` |

