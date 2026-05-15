# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 390/468 (83.3%)
- **Function parity:** 3188/4581 matched (target 5310) — 69.6%
- **Class/type parity:** 812/1209 matched (target 1595) — 67.2%
- **Combined symbol parity:** 4000/5790 matched (target 6905) — 69.1%
- **Average inline-code cosine:** 0.00 (function body across 339 matched files)
- **Average documentation cosine:** 0.76 (doc text across 339 matched files)
- **Cheat-zeroed Files:** 390
- **Critical Issues:** 390 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 178
- **Priority Score:** 178082720.0
- **Functions:** 114/118 matched (target 162)
- **Missing functions:** `fmt`, `eq`, `testing_new_int`, `_test_send_sync`
- **Types:** 6/9 matched (target 10)
- **Missing types:** `DisplayWithTypeImpl`, `Canonical`, `String`
- **Symbol Deficit:** 7 (functions: 4, types: 3)
- **Missing Tests:** 1 of 9 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 2. typing.ty
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 109
- **Priority Score:** 109015408.0
- **Functions:** 49/50 matched (target 57)
- **Missing functions:** `fmt`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 3. layout.heap
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 109
- **Priority Score:** 109000008.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 4. assert
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 84
- **Priority Score:** 84000008.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 5. typing.starlark_value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 76
- **Priority Score:** 76053808.0
- **Functions:** 29/34 matched (target 41)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Symbol Deficit:** 5 (functions: 5, types: 0)
- **Action:** Deep review - likely missing major functionality

### 6. params.display
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 76
- **Priority Score:** 76000712.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 7. runtime.evaluator
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 55
- **Priority Score:** 55016712.0
- **Functions:** 59/60 matched (target 63)
- **Missing functions:** `drop`
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 8. debug
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 53
- **Priority Score:** 53000008.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 9. values.trace
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 52
- **Priority Score:** 52000208.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 10. values.freeze
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42010312.0
- **Functions:** 1/1 matched (target 33)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Frozen`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 11. values.alloc_value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 42
- **Priority Score:** 42000608.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 10)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 12. layout.freezer
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 36
- **Priority Score:** 36000608.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 13. coerce
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 34
- **Priority Score:** 34031408.0
- **Functions:** 4/5 matched
- **Missing functions:** `f`
- **Types:** 7/9 matched (target 36)
- **Missing types:** `Trait`, `Assoc`
- **Symbol Deficit:** 3 (functions: 1, types: 2)
- **Missing Tests:** 1 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 14. compiler.span
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 29
- **Priority Score:** 29010410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 15. values.frozen_ref
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 27
- **Priority Score:** 27022110.0
- **Functions:** 17/17 matched (target 23)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Target`, `Frozen`
- **Symbol Deficit:** 2 (functions: 0, types: 2)
- **Action:** Deep review - likely missing major functionality

### 16. none.none_type
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 27
- **Priority Score:** 27011310.0
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 17. runtime.frame_span
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 26
- **Priority Score:** 26010510.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 18. runtime.arguments
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 25
- **Priority Score:** 25013810.0
- **Functions:** 29/30 matched (target 54)
- **Missing functions:** `f`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 19. typing.type_compiled
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 22
- **Priority Score:** 22000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 20. util.arc_str
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 21
- **Priority Score:** 21010710.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 21. values.value_of_unchecked
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20062510.0
- **Functions:** 15/18 matched (target 24)
- **Missing functions:** `fmt`, `assert_send_sync`, `_assert_covariant`
- **Types:** 4/7 matched (target 8)
- **Missing types:** `Canonical`, `Frozen`, `Error`
- **Symbol Deficit:** 6 (functions: 3, types: 3)
- **Missing Tests:** 2 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 22. environment.globals
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20044010.0
- **Functions:** 31/35 matched (target 37)
- **Missing functions:** `get`, `test_send_sync`, `register_foo`, `foo`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Symbol Deficit:** 4 (functions: 4, types: 0)
- **Missing Tests:** 4 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 23. __derive_refs.param_spec
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000810.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 24. util.refcell
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000210.0
- **Functions:** 2/2 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 25. derive.module
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000010.0
- **Functions:** 0/0 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 26. environment.methods
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 17
- **Priority Score:** 17032310.0
- **Functions:** 17/19 matched (target 26)
- **Missing functions:** `test_set_attribute`, `get_methods`
- **Types:** 3/4 matched
- **Missing types:** `Magic`
- **Symbol Deficit:** 3 (functions: 2, types: 1)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 27. values.iter
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 17
- **Priority Score:** 17020710.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 14)
- **Missing types:** `Item`
- **Symbol Deficit:** 2 (functions: 1, types: 1)
- **Action:** Deep review - likely missing major functionality

### 28. values.error
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 17
- **Priority Score:** 17010710.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 29. private
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 15
- **Priority Score:** 15000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 30. collections.symbol
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 15
- **Priority Score:** 15000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 31. layout.avalue
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 14
- **Priority Score:** 14001110.0
- **Functions:** 8/8 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 32. typing.tuple
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12010710.0
- **Functions:** 5/6 matched (target 11)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 33. layout.const_frozen_string
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000210.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 34. layout.value_lifetimeless
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 35. types.dict
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 36. int.inline_int
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

1. **derive.unpack_value** (51 deps)
   - Path: `tests/derive/unpack_value.rs`
   - Essential for 51 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. layout.value

- **Target:** `layout.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 178
- **Priority Score:** 178082720.0
- **Functions:** 114/118 matched (target 162)
- **Missing functions:** `fmt`, `eq`, `testing_new_int`, `_test_send_sync`
- **Types:** 6/9 matched (target 10)
- **Missing types:** `DisplayWithTypeImpl`, `Canonical`, `String`
- **Tests:** 8/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/value.rs` vs expected `values/layout/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/value.rs` vs expected `values/layout/value.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value.rs` (current: `// port-lint: source src/values/layout/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/value.rs` (current: `// port-lint: tests src/values/layout/value.rs`)
- **Lint issues:** 2

### 2. typing.ty

- **Target:** `typing.Ty [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 109
- **Priority Score:** 109015408.0
- **Functions:** 49/50 matched (target 57)
- **Missing functions:** `fmt`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/ty.rs` vs expected `typing/ty.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/ty.rs` vs expected `typing/ty.rs`
- **Proposed provenance header:** `// port-lint: source typing/ty.rs` (current: `// port-lint: source src/typing/ty.rs`)
- **Proposed provenance header:** `// port-lint: source typing/ty.rs` (current: `// port-lint: source src/typing/ty.rs`)
- **Lint issues:** 2

### 3. layout.heap

- **Target:** `heap.Heap [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 109
- **Priority Score:** 109000008.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap.rs` vs expected `values/layout/heap.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap.rs` (current: `// port-lint: source src/values/layout/heap.rs`)
- **Lint issues:** 1

### 4. assert

- **Target:** `assert.AssertModule [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 84
- **Priority Score:** 84000008.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/assert.rs` vs expected `assert.rs`
- **Proposed provenance header:** `// port-lint: source assert.rs` (current: `// port-lint: source src/assert.rs`)
- **Lint issues:** 1

### 5. typing.starlark_value

- **Target:** `typing.StarlarkValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 76
- **Priority Score:** 76053808.0
- **Functions:** 29/34 matched (target 41)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/starlark_value.rs` vs expected `typing/starlark_value.rs`
- **Proposed provenance header:** `// port-lint: source typing/starlark_value.rs` (current: `// port-lint: source src/typing/starlark_value.rs`)
- **Lint issues:** 1

### 6. params.display

- **Target:** `params.Display [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 76
- **Priority Score:** 76000712.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/display.rs` vs expected `eval/runtime/params/display.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/display.rs` (current: `// port-lint: source src/eval/runtime/params/display.rs`)
- **Lint issues:** 1

### 7. runtime.evaluator

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

### 8. debug

- **Target:** `debug.Debug [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 53
- **Priority Score:** 53000008.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

### 9. values.trace

- **Target:** `values.Trace [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 52
- **Priority Score:** 52000208.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/trace.rs` vs expected `values/trace.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/trace.rs` vs expected `values/trace.rs`
- **Proposed provenance header:** `// port-lint: source values/trace.rs` (current: `// port-lint: source src/values/trace.rs`)
- **Proposed provenance header:** `// port-lint: tests values/trace.rs` (current: `// port-lint: tests tests/derive/trace.rs`)
- **Lint issues:** 2

### 10. values.freeze

- **Target:** `values.Freeze [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 42
- **Priority Score:** 42010312.0
- **Functions:** 1/1 matched (target 33)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 6)
- **Missing types:** `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/freeze.rs` vs expected `values/freeze.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/freeze.rs` vs expected `values/freeze.rs`
- **Proposed provenance header:** `// port-lint: source values/freeze.rs` (current: `// port-lint: source src/values/freeze.rs`)
- **Proposed provenance header:** `// port-lint: tests values/freeze.rs` (current: `// port-lint: tests tests/derive/freeze.rs`)
- **Lint issues:** 2

### 11. values.alloc_value

- **Target:** `values.AllocValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 42
- **Priority Score:** 42000608.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/alloc_value.rs` vs expected `values/alloc_value.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/alloc_value.rs` vs expected `values/alloc_value.rs`
- **Proposed provenance header:** `// port-lint: source values/alloc_value.rs` (current: `// port-lint: source src/values/alloc_value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/alloc_value.rs` (current: `// port-lint: tests tests/derive/alloc_value.rs`)
- **Lint issues:** 2

### 12. layout.freezer

- **Target:** `layout.Freezer [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 36
- **Priority Score:** 36000608.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/freezer.rs` vs expected `values/layout/freezer.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/freezer.rs` (current: `// port-lint: source src/values/layout/freezer.rs`)
- **Lint issues:** 1

### 13. coerce

- **Target:** `starlark_kotlin.Coerce [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 34
- **Priority Score:** 34031408.0
- **Functions:** 4/5 matched
- **Missing functions:** `f`
- **Types:** 7/9 matched (target 36)
- **Missing types:** `Trait`, `Assoc`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/coerce.rs` vs expected `coerce.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/coerce.rs` vs expected `coerce.rs`
- **Proposed provenance header:** `// port-lint: source coerce.rs` (current: `// port-lint: source src/coerce.rs`)
- **Proposed provenance header:** `// port-lint: tests coerce.rs` (current: `// port-lint: tests src/coerce.rs`)
- **Lint issues:** 2

### 14. compiler.span

- **Target:** `compiler.Span [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 29
- **Priority Score:** 29010410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/span.rs` vs expected `eval/compiler/span.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/span.rs` (current: `// port-lint: source src/eval/compiler/span.rs`)
- **Lint issues:** 1

### 15. values.frozen_ref

- **Target:** `values.FrozenRef [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 27
- **Priority Score:** 27022110.0
- **Functions:** 17/17 matched (target 23)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Target`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/frozen_ref.rs` vs expected `values/frozen_ref.rs`
- **Proposed provenance header:** `// port-lint: source values/frozen_ref.rs` (current: `// port-lint: source src/values/frozen_ref.rs`)
- **Lint issues:** 1

### 16. none.none_type

- **Target:** `none.NoneType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 27
- **Priority Score:** 27011310.0
- **Functions:** 11/11 matched (target 15)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none/none_type.rs` vs expected `values/types/none/none_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none/none_type.rs` (current: `// port-lint: source src/values/types/none/none_type.rs`)
- **Lint issues:** 1

### 17. runtime.frame_span

- **Target:** `runtime.FrameSpan [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 26
- **Priority Score:** 26010510.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/frame_span.rs` vs expected `eval/runtime/frame_span.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/frame_span.rs` (current: `// port-lint: source src/eval/runtime/frame_span.rs`)
- **Lint issues:** 1

### 18. runtime.arguments

- **Target:** `runtime.Arguments [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 25
- **Priority Score:** 25013810.0
- **Functions:** 29/30 matched (target 54)
- **Missing functions:** `f`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/arguments.rs` vs expected `eval/runtime/arguments.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/arguments.rs` vs expected `eval/runtime/arguments.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/arguments.rs` (current: `// port-lint: source src/eval/runtime/arguments.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/arguments.rs` (current: `// port-lint: tests src/eval/runtime/arguments.rs`)
- **Lint issues:** 2

### 19. typing.type_compiled

- **Target:** `type_compiled.TypeCompiled [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 22
- **Priority Score:** 22000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled.rs` vs expected `values/typing/type_compiled.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled.rs` (current: `// port-lint: source src/values/typing/type_compiled.rs`)
- **Lint issues:** 1

### 20. util.arc_str

- **Target:** `util.ArcStr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 21
- **Priority Score:** 21010710.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/arc_str.rs` vs expected `util/arc_str.rs`
- **Proposed provenance header:** `// port-lint: source util/arc_str.rs` (current: `// port-lint: source src/util/arc_str.rs`)
- **Lint issues:** 1

### 21. values.value_of_unchecked

- **Target:** `values.ValueOfUnchecked [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20062510.0
- **Functions:** 15/18 matched (target 24)
- **Missing functions:** `fmt`, `assert_send_sync`, `_assert_covariant`
- **Types:** 4/7 matched (target 8)
- **Missing types:** `Canonical`, `Frozen`, `Error`
- **Tests:** 3/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/value_of_unchecked.rs` vs expected `values/value_of_unchecked.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/value_of_unchecked.rs` vs expected `values/value_of_unchecked.rs`
- **Proposed provenance header:** `// port-lint: source values/value_of_unchecked.rs` (current: `// port-lint: source src/values/value_of_unchecked.rs`)
- **Proposed provenance header:** `// port-lint: tests values/value_of_unchecked.rs` (current: `// port-lint: tests src/values/value_of_unchecked.rs`)
- **Lint issues:** 2

### 22. environment.globals

- **Target:** `environment.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20044010.0
- **Functions:** 31/35 matched (target 37)
- **Missing functions:** `get`, `test_send_sync`, `register_foo`, `foo`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Tests:** 1/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/globals.rs` vs expected `environment/globals.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/environment/globals.rs` vs expected `environment/globals.rs`
- **Proposed provenance header:** `// port-lint: source environment/globals.rs` (current: `// port-lint: source src/environment/globals.rs`)
- **Proposed provenance header:** `// port-lint: tests environment/globals.rs` (current: `// port-lint: tests src/environment/globals.rs`)
- **Lint issues:** 2

### 23. __derive_refs.param_spec

- **Target:** `__derive_refs.ParamSpec [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20000810.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/param_spec.rs` vs expected `__derive_refs/param_spec.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/param_spec.rs` (current: `// port-lint: source src/__derive_refs/param_spec.rs`)
- **Lint issues:** 1

### 24. util.refcell

- **Target:** `refcell.RefCell [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20000210.0
- **Functions:** 2/2 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/refcell.rs` vs expected `util/refcell.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/util/refcell.rs` vs expected `util/refcell.rs`
- **Proposed provenance header:** `// port-lint: source util/refcell.rs` (current: `// port-lint: source src/util/refcell.rs`)
- **Proposed provenance header:** `// port-lint: tests util/refcell.rs` (current: `// port-lint: tests src/util/refcell.rs`)
- **Lint issues:** 2

### 25. derive.module

- **Target:** `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20000010.0
- **Functions:** 0/0 matched (target 12)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `src/syntax/module.rs` vs expected `tests/derive/module.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module.rs` (current: `// port-lint: source src/syntax/module.rs`)
- **Lint issues:** 1

### 26. environment.methods

- **Target:** `environment.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 17
- **Priority Score:** 17032310.0
- **Functions:** 17/19 matched (target 26)
- **Missing functions:** `test_set_attribute`, `get_methods`
- **Types:** 3/4 matched
- **Missing types:** `Magic`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/methods.rs` vs expected `environment/methods.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/module/methods.rs` vs expected `environment/methods.rs`
- **Proposed provenance header:** `// port-lint: source environment/methods.rs` (current: `// port-lint: source src/environment/methods.rs`)
- **Proposed provenance header:** `// port-lint: tests environment/methods.rs` (current: `// port-lint: tests tests/derive/module/methods.rs`)
- **Lint issues:** 2

### 27. values.iter

- **Target:** `values.Iter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 17
- **Priority Score:** 17020710.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 14)
- **Missing types:** `Item`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/small_map/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/small_set/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec2/iter.rs` vs expected `values/iter.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec_map/iter.rs` vs expected `values/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/values/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/small_map/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/small_set/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/vec2/iter.rs`)
- **Proposed provenance header:** `// port-lint: source values/iter.rs` (current: `// port-lint: source src/vec_map/iter.rs`)
- **Lint issues:** 6

### 28. values.error

- **Target:** `values.Error [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 17
- **Priority Score:** 17010710.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/error.rs` vs expected `values/error.rs`
- **Proposed provenance header:** `// port-lint: source values/error.rs` (current: `// port-lint: source src/values/error.rs`)
- **Lint issues:** 1

### 29. private

- **Target:** `starlark_kotlin.Private [ZERO] [PROVENANCE-FALLBACK]`
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

### 30. collections.symbol

- **Target:** `collections.Symbol [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 15
- **Priority Score:** 15000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/symbol.rs` vs expected `collections/symbol.rs`
- **Proposed provenance header:** `// port-lint: source collections/symbol.rs` (current: `// port-lint: source src/collections/symbol.rs`)
- **Lint issues:** 1

### 31. layout.avalue

- **Target:** `layout.AValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 14
- **Priority Score:** 14001110.0
- **Functions:** 8/8 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalue.rs` vs expected `values/layout/avalue.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/avalue.rs` vs expected `values/layout/avalue.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalue.rs` (current: `// port-lint: source src/values/layout/avalue.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/avalue.rs` (current: `// port-lint: tests src/values/layout/avalue.rs`)
- **Lint issues:** 2

### 32. typing.tuple

- **Target:** `typing.Tuple [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12010710.0
- **Functions:** 5/6 matched (target 11)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/tuple.rs` vs expected `typing/tuple.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/typing/tests/tuple.rs` vs expected `typing/tuple.rs`
- **Proposed provenance header:** `// port-lint: source typing/tuple.rs` (current: `// port-lint: source src/typing/tuple.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/tuple.rs` (current: `// port-lint: tests src/typing/tests/tuple.rs`)
- **Lint issues:** 2

### 33. layout.const_frozen_string

- **Target:** `layout.ConstFrozenString [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12000210.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/const_frozen_string.rs` vs expected `values/layout/const_frozen_string.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/const_frozen_string.rs` vs expected `values/layout/const_frozen_string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/const_frozen_string.rs` (current: `// port-lint: source src/values/layout/const_frozen_string.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/const_frozen_string.rs` (current: `// port-lint: tests src/values/layout/const_frozen_string.rs`)
- **Lint issues:** 2

### 34. layout.value_lifetimeless

- **Target:** `layout.ValueLifetimeless [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12000110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/value_lifetimeless.rs` vs expected `values/layout/value_lifetimeless.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_lifetimeless.rs` (current: `// port-lint: source src/values/layout/value_lifetimeless.rs`)
- **Lint issues:** 1

### 35. types.dict

- **Target:** `types.Dict [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict.rs` vs expected `values/types/dict.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict.rs` (current: `// port-lint: source src/values/types/dict.rs`)
- **Lint issues:** 1

### 36. int.inline_int

- **Target:** `int.InlineInt [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 11
- **Priority Score:** 11123910.0
- **Functions:** 25/34 matched (target 43)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`, `test_min_max_for_bits`
- **Types:** 2/5 matched (target 6)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/inline_int.rs` vs expected `values/types/int/inline_int.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/inline_int.rs` (current: `// port-lint: source src/values/types/int/inline_int.rs`)
- **Lint issues:** 1

### 37. int.pointer_i32

- **Target:** `int.PointerI32 [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9043310.0
- **Functions:** 28/31 matched (target 34)
- **Missing functions:** `eq`, `fmt`, `serialize`
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/pointer_i32.rs` vs expected `values/types/int/pointer_i32.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/pointer_i32.rs` (current: `// port-lint: source src/values/types/int/pointer_i32.rs`)
- **Lint issues:** 1

### 38. types.type_instance_id

- **Target:** `types.TypeInstanceId [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9010210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `r#gen`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/type_instance_id.rs` vs expected `values/types/type_instance_id.rs`
- **Proposed provenance header:** `// port-lint: source values/types/type_instance_id.rs` (current: `// port-lint: source src/values/types/type_instance_id.rs`)
- **Lint issues:** 1

### 39. layout.aligned_size

- **Target:** `layout.AlignedSize [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8081510.0
- **Functions:** 6/13 matched (target 15)
- **Missing functions:** `layout`, `ptr_diff`, `add`, `sub`, `mul`, `test_checked_next_power_of_two`, `test_sub`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/aligned_size.rs` vs expected `values/layout/aligned_size.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/aligned_size.rs` (current: `// port-lint: source src/values/layout/aligned_size.rs`)
- **Lint issues:** 1

### 40. any

- **Target:** `starlark_kotlin.Any [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8052710.0
- **Functions:** 8/12 matched (target 17)
- **Missing functions:** `is`, `convert_value`, `convert_any`, `test`
- **Types:** 14/15 matched (target 50)
- **Missing types:** `StaticType`
- **Tests:** 4/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/any.rs` vs expected `any.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/any.rs` vs expected `any.rs`
- **Proposed provenance header:** `// port-lint: source any.rs` (current: `// port-lint: source src/any.rs`)
- **Proposed provenance header:** `// port-lint: tests any.rs` (current: `// port-lint: tests src/any.rs`)
- **Lint issues:** 2

### 41. eval.compiler

- **Target:** `eval.Compiler [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8000710.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler.rs` vs expected `eval/compiler.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler.rs` (current: `// port-lint: source src/eval/compiler.rs`)
- **Lint issues:** 1

### 42. cast

- **Target:** `starlark_kotlin.Cast [ZERO] [PROVENANCE-FALLBACK]`
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

### 43. types.bigint

- **Target:** `types.Bigint [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7027410.0
- **Functions:** 71/73 matched (target 77)
- **Missing functions:** `unpack_integer`, `eq`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 42/42 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bigint.rs` vs expected `values/types/bigint.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/bigint.rs` vs expected `values/types/bigint.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bigint.rs` (current: `// port-lint: source src/values/types/bigint.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/bigint.rs` (current: `// port-lint: tests src/values/types/bigint.rs`)
- **Lint issues:** 2

### 44. runtime.frozen_file_span

- **Target:** `runtime.FrozenFileSpan [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7011110.0
- **Functions:** 9/10 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/frozen_file_span.rs` vs expected `eval/runtime/frozen_file_span.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/frozen_file_span.rs` (current: `// port-lint: source src/eval/runtime/frozen_file_span.rs`)
- **Lint issues:** 1

### 45. values.starlark_type_id

- **Target:** `values.StarlarkTypeId [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7010810.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `eq`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/starlark_type_id.rs` vs expected `values/starlark_type_id.rs`
- **Proposed provenance header:** `// port-lint: source values/starlark_type_id.rs` (current: `// port-lint: source src/values/starlark_type_id.rs`)
- **Lint issues:** 1

### 46. compiler.opt_ctx

- **Target:** `compiler.OptCtx [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7000710.0
- **Functions:** 5/5 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/opt_ctx.rs` vs expected `eval/compiler/opt_ctx.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/opt_ctx.rs` (current: `// port-lint: source src/eval/compiler/opt_ctx.rs`)
- **Lint issues:** 1

### 47. type_compiled.type_matcher_factory

- **Target:** `type_compiled.TypeMatcherFactory [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 7
- **Priority Score:** 7000610.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/type_matcher_factory.rs` vs expected `values/typing/type_compiled/type_matcher_factory.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/type_matcher_factory.rs` (current: `// port-lint: source src/values/typing/type_compiled/type_matcher_factory.rs`)
- **Lint issues:** 1

### 48. runtime.small_duration

- **Target:** `runtime.SmallDuration [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6040910.0
- **Functions:** 4/7 matched (target 9)
- **Missing functions:** `from_millis`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/small_duration.rs` vs expected `eval/runtime/small_duration.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/small_duration.rs` (current: `// port-lint: source src/eval/runtime/small_duration.rs`)
- **Lint issues:** 1

### 49. typing.typecheck

- **Target:** `typing.Typecheck [STUB] [PROVENANCE-FALLBACK]`
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

### 50. dict.dict_type

- **Target:** `dict.DictType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6030510.0
- **Functions:** 1/2 matched (target 4)
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/dict_type.rs` vs expected `values/types/dict/dict_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/dict_type.rs` (current: `// port-lint: source src/values/types/dict/dict_type.rs`)
- **Lint issues:** 1

### 51. none.none_or

- **Target:** `none.NoneOr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6021010.0
- **Functions:** 7/7 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 4)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none/none_or.rs` vs expected `values/types/none/none_or.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none/none_or.rs` (current: `// port-lint: source src/values/types/none/none_or.rs`)
- **Lint issues:** 1

### 52. values.freeze_error

- **Target:** `values.FreezeError [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6020810.0
- **Functions:** 3/4 matched (target 6)
- **Missing functions:** `from`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `FreezeResult`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/freeze_error.rs` vs expected `values/freeze_error.rs`
- **Proposed provenance header:** `// port-lint: source values/freeze_error.rs` (current: `// port-lint: source src/values/freeze_error.rs`)
- **Lint issues:** 1

### 53. layout.value_alloc_size

- **Target:** `layout.ValueAllocSize [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6010610.0
- **Functions:** 4/5 matched
- **Missing functions:** `layout`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/value_alloc_size.rs` vs expected `values/layout/value_alloc_size.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_alloc_size.rs` (current: `// port-lint: source src/values/layout/value_alloc_size.rs`)
- **Lint issues:** 1

### 54. compiler.stmt

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

### 55. profile.profiler_type

- **Target:** `profile.ProfilerType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6000310.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/profiler_type.rs` vs expected `eval/runtime/profile/profiler_type.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/profiler_type.rs` (current: `// port-lint: source src/eval/runtime/profile/profiler_type.rs`)
- **Lint issues:** 1

### 56. values.layout

- **Target:** `values.Layout [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 6
- **Priority Score:** 6000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout.rs` vs expected `values/layout.rs`
- **Proposed provenance header:** `// port-lint: source values/layout.rs` (current: `// port-lint: source src/values/layout.rs`)
- **Lint issues:** 1

### 57. tests.def

- **Target:** `typing.DefParamIndices [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5141410.0
- **Functions:** 0/14 matched (target 18)
- **Missing functions:** `test_lambda`, `test_frozen_lambda`, `test_nested_def_1`, `test_nested_def_2`, `test_nested_def_3`, `test_lambda_capture_from_module`, `test_lambda_capture_from_def`, `test_lambda_capture_reassigned_from_def`, `test_def_freeze`, `test_frozen_lambda_nest`, `test_context_captured`, `test_lambda_errors`, `test_lambda_errors_nested`, `test_double_capture_and_freeze`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/14 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_syntax/src/syntax/def.rs` vs expected `tests/def.rs`
- **Proposed provenance header:** `// port-lint: source tests/def.rs` (current: `// port-lint: source starlark_syntax/src/syntax/def.rs`)
- **Lint issues:** 1

### 58. types.array

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

### 59. typing.arc_ty

- **Target:** `typing.ArcTy [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5021110.0
- **Functions:** 6/7 matched (target 16)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 10)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/arc_ty.rs` vs expected `typing/arc_ty.rs`
- **Proposed provenance header:** `// port-lint: source typing/arc_ty.rs` (current: `// port-lint: source src/typing/arc_ty.rs`)
- **Lint issues:** 1

### 60. typing.interface

- **Target:** `typing.Interface [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5000410.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/interface.rs` vs expected `typing/interface.rs`
- **Proposed provenance header:** `// port-lint: source typing/interface.rs` (current: `// port-lint: source src/typing/interface.rs`)
- **Lint issues:** 1

### 61. scope.scope_resolver_globals

- **Target:** `scope.ScopeResolverGlobals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5000410.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope/scope_resolver_globals.rs` vs expected `eval/compiler/scope/scope_resolver_globals.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope/scope_resolver_globals.rs` (current: `// port-lint: source src/eval/compiler/scope/scope_resolver_globals.rs`)
- **Lint issues:** 1

### 62. eval.bc

- **Target:** `bc.Bc [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc.rs` vs expected `eval/bc.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/bc.rs` vs expected `eval/bc.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc.rs` (current: `// port-lint: source src/eval/bc.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc.rs` (current: `// port-lint: tests tests/bc.rs`)
- **Lint issues:** 2

### 63. types.range

- **Target:** `types.Range [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 5
- **Priority Score:** 5000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/range.rs` vs expected `values/types/range.rs`
- **Proposed provenance header:** `// port-lint: source values/types/range.rs` (current: `// port-lint: source src/values/types/range.rs`)
- **Lint issues:** 1

### 64. enumeration.enum_type

- **Target:** `enumeration.EnumType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4194410.0
- **Functions:** 21/36 matched (target 24)
- **Missing functions:** `fmt`, `r#type`, `values`, `test_enum_type_as_type_pass`, `test_enum_type_fail_runtime`, `test_enum_type_fail_compile_time`, `test_enum_is_callable`, `test_enum_value_index`, `test_enum_value_index_correct_type`, `test_enum_index`, `test_enum_index_fail`, `test_enum_call`, `test_enum_attribute_access`, `test_enum_attribute_access_invalid`, `test_enum_attribute_access_type`
- **Types:** 4/8 matched (target 6)
- **Missing types:** `EnumCell`, `TyEnumDataOpt`, `Frozen`, `Canonical`
- **Tests:** 0/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/enum_type.rs` vs expected `values/types/enumeration/enum_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/enum_type.rs` (current: `// port-lint: source src/values/types/enumeration/enum_type.rs`)
- **Lint issues:** 1

### 65. bc.frame

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

### 66. values.demand

- **Target:** `values.Demand [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4061110.0
- **Functions:** 4/7 matched (target 5)
- **Missing functions:** `payload`, `provide`, `test_trait_downcast`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `SomeTrait`, `StaticType`, `MyValue`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/demand.rs` vs expected `values/demand.rs`
- **Proposed provenance header:** `// port-lint: source values/demand.rs` (current: `// port-lint: source src/values/demand.rs`)
- **Lint issues:** 1

### 67. types.starlark_value_as_type

- **Target:** `types.StarlarkValueAsType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4051710.0
- **Functions:** 9/13 matched (target 15)
- **Missing functions:** `fmt`, `new`, `compiler_args_globals`, `compiler_args`
- **Types:** 3/4 matched
- **Missing types:** `Canonical`
- **Tests:** 3/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/starlark_value_as_type.rs` vs expected `values/types/starlark_value_as_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/starlark_value_as_type.rs` vs expected `values/types/starlark_value_as_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/starlark_value_as_type.rs` (current: `// port-lint: source src/values/types/starlark_value_as_type.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/starlark_value_as_type.rs` (current: `// port-lint: tests src/values/types/starlark_value_as_type.rs`)
- **Lint issues:** 2

### 68. values.value_of

- **Target:** `values.ValueOf [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4051010.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `deref`, `fmt`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Target`, `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/value_of.rs` vs expected `values/value_of.rs`
- **Proposed provenance header:** `// port-lint: source values/value_of.rs` (current: `// port-lint: source src/values/value_of.rs`)
- **Lint issues:** 1

### 69. profile.alloc_counts

- **Target:** `profile.AllocCounts [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4040610.0
- **Functions:** 1/4 matched (target 5)
- **Missing functions:** `normalize_for_golden_tests`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/alloc_counts.rs` vs expected `values/layout/heap/profile/alloc_counts.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/alloc_counts.rs` (current: `// port-lint: source src/values/layout/heap/profile/alloc_counts.rs`)
- **Lint issues:** 1

### 70. bc.native_function

- **Target:** `bc.NativeFunction [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4010510.0
- **Functions:** 3/4 matched
- **Missing functions:** `fun`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/native_function.rs` vs expected `eval/bc/native_function.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/native_function.rs` (current: `// port-lint: source src/eval/bc/native_function.rs`)
- **Lint issues:** 1

### 71. types.ellipsis

- **Target:** `types.Ellipsis [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 4
- **Priority Score:** 4010410.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `test_ellipsis`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/ellipsis.rs` vs expected `values/types/ellipsis.rs`
- **Proposed provenance header:** `// port-lint: source values/types/ellipsis.rs` (current: `// port-lint: source src/values/types/ellipsis.rs`)
- **Lint issues:** 1

### 72. record.record_type

- **Target:** `record.RecordType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3133010.0
- **Functions:** 15/22 matched (target 17)
- **Missing functions:** `fmt`, `r#type`, `test_record_type_as_type_pass`, `test_record_type_as_type_compile_time`, `test_record_type_as_type_runtime`, `test_anon_record`, `test_missing_field_error`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/record_type.rs` vs expected `values/types/record/record_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/record_type.rs` (current: `// port-lint: source src/values/types/record/record_type.rs`)
- **Lint issues:** 1

### 73. alloc.chunk

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

### 74. list.alloc

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

### 75. list.list_type

- **Target:** `list.ListType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3030510.0
- **Functions:** 1/2 matched (target 5)
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/list_type.rs` vs expected `values/types/list/list_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/list_type.rs` (current: `// port-lint: source src/values/types/list/list_type.rs`)
- **Lint issues:** 1

### 76. stdlib.call_stack

- **Target:** `stdlib.CallStack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3021410.0
- **Functions:** 11/13 matched (target 18)
- **Missing functions:** `fmt`, `global`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/call_stack.rs` vs expected `stdlib/call_stack.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlark_syntax/src/call_stack.rs` vs expected `stdlib/call_stack.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/call_stack.rs` vs expected `stdlib/call_stack.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/call_stack.rs` (current: `// port-lint: source src/stdlib/call_stack.rs`)
- **Proposed provenance header:** `// port-lint: source stdlib/call_stack.rs` (current: `// port-lint: source ../starlark_syntax/src/call_stack.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/call_stack.rs` (current: `// port-lint: tests src/stdlib/call_stack.rs`)
- **Lint issues:** 3

### 77. profile.instant

- **Target:** `profile.Instant [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3020610.0
- **Functions:** 3/4 matched (target 9)
- **Missing functions:** `sub`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/instant.rs` vs expected `eval/runtime/profile/instant.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/instant.rs` (current: `// port-lint: source src/eval/runtime/profile/instant.rs`)
- **Lint issues:** 1

### 78. compiler.constants

- **Target:** `compiler.Constants [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3020510.0
- **Functions:** 1/3 matched (target 4)
- **Missing functions:** `eq`, `test_constants`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/constants.rs` vs expected `eval/compiler/constants.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/constants.rs` (current: `// port-lint: source src/eval/compiler/constants.rs`)
- **Lint issues:** 1

### 79. values.unpack_and_discard

- **Target:** `values.UnpackAndDiscard [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3020510.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/unpack_and_discard.rs` vs expected `values/unpack_and_discard.rs`
- **Proposed provenance header:** `// port-lint: source values/unpack_and_discard.rs` (current: `// port-lint: source src/values/unpack_and_discard.rs`)
- **Lint issues:** 1

### 80. errors.did_you_mean

- **Target:** `errors.DidYouMean [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3000610.0
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/errors/did_you_mean.rs` vs expected `errors/did_you_mean.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/errors/did_you_mean.rs` vs expected `errors/did_you_mean.rs`
- **Proposed provenance header:** `// port-lint: source errors/did_you_mean.rs` (current: `// port-lint: source src/errors/did_you_mean.rs`)
- **Proposed provenance header:** `// port-lint: tests errors/did_you_mean.rs` (current: `// port-lint: tests src/errors/did_you_mean.rs`)
- **Lint issues:** 2

### 81. sealed

- **Target:** `starlark_kotlin.Sealed [ZERO] [PROVENANCE-FALLBACK]`
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

### 82. types.record

- **Target:** `types.Record [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record.rs` vs expected `values/types/record.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record.rs` (current: `// port-lint: source src/values/types/record.rs`)
- **Lint issues:** 1

### 83. types.namespace

- **Target:** `types.Namespace [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace.rs` vs expected `values/types/namespace.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace.rs` (current: `// port-lint: source src/values/types/namespace.rs`)
- **Lint issues:** 1

### 84. compiler.small_vec_1

- **Target:** `compiler.SmallVec1 [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2101510.0
- **Functions:** 4/11 matched (target 9)
- **Missing functions:** `fmt`, `eq`, `hash`, `partial_cmp`, `cmp`, `deref`, `into_iter`
- **Types:** 1/4 matched (target 3)
- **Missing types:** `Target`, `Item`, `IntoIter`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/small_vec_1.rs` vs expected `eval/compiler/small_vec_1.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/small_vec_1.rs` (current: `// port-lint: source src/eval/compiler/small_vec_1.rs`)
- **Lint issues:** 1

### 85. util.arc_or_static

- **Target:** `util.ArcOrStatic [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2061310.0
- **Functions:** 5/10 matched (target 9)
- **Missing functions:** `fmt`, `eq`, `partial_cmp`, `cmp`, `hash`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/arc_or_static.rs` vs expected `util/arc_or_static.rs`
- **Proposed provenance header:** `// port-lint: source util/arc_or_static.rs` (current: `// port-lint: source src/util/arc_or_static.rs`)
- **Lint issues:** 1

### 86. typing.type_type

- **Target:** `typing.TypeType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2050810.0
- **Functions:** 2/5 matched (target 3)
- **Missing functions:** `test`, `module`, `takes_type`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_type.rs` vs expected `values/typing/type_type.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_type.rs` (current: `// port-lint: source src/values/typing/type_type.rs`)
- **Lint issues:** 1

### 87. alloc.chunk_part

- **Target:** `alloc.ChunkPart [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2041610.0
- **Functions:** 11/15 matched (target 16)
- **Missing functions:** `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`, `test_is_full`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chunk_part.rs` vs expected `values/layout/heap/allocator/alloc/chunk_part.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chunk_part.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chunk_part.rs`)
- **Lint issues:** 1

### 88. values.owned_frozen_ref

- **Target:** `values.OwnedFrozenRef [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2031510.0
- **Functions:** 10/12 matched (target 17)
- **Missing functions:** `fmt`, `deref`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/owned_frozen_ref.rs` vs expected `values/owned_frozen_ref.rs`
- **Proposed provenance header:** `// port-lint: source values/owned_frozen_ref.rs` (current: `// port-lint: source src/values/owned_frozen_ref.rs`)
- **Lint issues:** 1

### 89. layout.const_type_id

- **Target:** `layout.ConstTypeId [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2030610.0
- **Functions:** 2/5 matched (target 4)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/const_type_id.rs` vs expected `values/layout/const_type_id.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/const_type_id.rs` (current: `// port-lint: source src/values/layout/const_type_id.rs`)
- **Lint issues:** 1

### 90. runtime.rust_loc

- **Target:** `runtime.RustLoc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2030310.0
- **Functions:** 0/3 matched (target 1)
- **Missing functions:** `rust_loc_globals`, `invoke`, `test_rust_loc`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/rust_loc.rs` vs expected `eval/runtime/rust_loc.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/rust_loc.rs` (current: `// port-lint: source src/eval/runtime/rust_loc.rs`)
- **Lint issues:** 1

### 91. avalues.str_

- **Target:** `avalues.Str [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2021410.0
- **Functions:** 11/11 matched (target 14)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/str_.rs` vs expected `values/layout/avalues/str_.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/str_.rs` (current: `// port-lint: source src/values/layout/avalues/str_.rs`)
- **Lint issues:** 1

### 92. values.stack_guard

- **Target:** `values.StackGuard [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2010510.0
- **Functions:** 3/4 matched
- **Missing functions:** `drop`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/stack_guard.rs` vs expected `values/stack_guard.rs`
- **Proposed provenance header:** `// port-lint: source values/stack_guard.rs` (current: `// port-lint: source src/values/stack_guard.rs`)
- **Lint issues:** 1

### 93. collections.aligned_padded_str

- **Target:** `aligned_padded_str.AlignedPaddedStr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2010410.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/aligned_padded_str.rs` vs expected `collections/aligned_padded_str.rs`
- **Proposed provenance header:** `// port-lint: source collections/aligned_padded_str.rs` (current: `// port-lint: source src/collections/aligned_padded_str.rs`)
- **Lint issues:** 1

### 94. runtime.file_loader

- **Target:** `runtime.FileLoader [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000410.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/file_loader.rs` vs expected `eval/runtime/file_loader.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/file_loader.rs` (current: `// port-lint: source src/eval/runtime/file_loader.rs`)
- **Lint issues:** 1

### 95. profile.string_index

- **Target:** `profile.StringIndex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/string_index.rs` vs expected `values/layout/heap/profile/string_index.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/string_index.rs` (current: `// port-lint: source src/values/layout/heap/profile/string_index.rs`)
- **Lint issues:** 1

### 96. collections.string_pool

- **Target:** `collections.StringPool [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000310.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/string_pool.rs` vs expected `collections/string_pool.rs`
- **Proposed provenance header:** `// port-lint: source collections/string_pool.rs` (current: `// port-lint: source src/collections/string_pool.rs`)
- **Lint issues:** 1

### 97. hint

- **Target:** `starlark_kotlin.Hint [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000210.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/hint.rs` vs expected `hint.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec_map/hint.rs` vs expected `hint.rs`
- **Proposed provenance header:** `// port-lint: source hint.rs` (current: `// port-lint: source src/hint.rs`)
- **Proposed provenance header:** `// port-lint: source hint.rs` (current: `// port-lint: source src/vec_map/hint.rs`)
- **Lint issues:** 2

### 98. def_inline.local_as_value

- **Target:** `def_inline.LocalAsValue [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000210.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/def_inline/local_as_value.rs` vs expected `eval/compiler/def_inline/local_as_value.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def_inline/local_as_value.rs` (current: `// port-lint: source src/eval/compiler/def_inline/local_as_value.rs`)
- **Lint issues:** 1

### 99. types.num

- **Target:** `types.Num [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num.rs` vs expected `values/types/num.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num.rs` (current: `// port-lint: source src/values/types/num.rs`)
- **Lint issues:** 1

### 100. types.float

- **Target:** `types.Float [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float.rs` vs expected `values/types/float.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float.rs` (current: `// port-lint: source src/values/types/float.rs`)
- **Lint issues:** 1

### 101. types.list

- **Target:** `types.List [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list.rs` vs expected `values/types/list.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list.rs` (current: `// port-lint: source src/values/types/list.rs`)
- **Lint issues:** 1

### 102. values.thin_box_slice_frozen_value

- **Target:** `values.ThinBoxSliceFrozenValue [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thin_box_slice_frozen_value.rs` vs expected `values/thin_box_slice_frozen_value.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value.rs` (current: `// port-lint: source src/values/thin_box_slice_frozen_value.rs`)
- **Lint issues:** 1

### 103. heap.arena

- **Target:** `heap.Arena [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1224410.0
- **Functions:** 18/37 matched (target 20)
- **Missing functions:** `max`, `next`, `write_black_hole`, `debug_assert_extra_is_empty`, `write`, `write_no_extra`, `alloc_uninit`, `bump_for_type`, `iter_chunk`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty`
- **Types:** 4/7 matched (target 6)
- **Missing types:** `ChunkIter`, `Item`, `ArenaUninit`
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/arena.rs` vs expected `values/layout/heap/arena.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/arena.rs` (current: `// port-lint: source src/values/layout/heap/arena.rs`)
- **Lint issues:** 1

### 104. collections.alloca

- **Target:** `collections.Alloca [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1202610.0
- **Functions:** 5/22 matched (target 5)
- **Missing functions:** `alloc`, `ptr`, `end`, `size_words`, `drop`, `new`, `with_capacity`, `assert_state`, `allocate_more`, `rem_in_words_to_rem_in_t`, `len_in_to_to_len_in_words`, `test_rem_in_words_to_rem_in_t`, `test_len_in_t_to_len_in_words`, `test_alloca`, `trigger_bug`, `test_alloca_bug_not_aligned`, `test_alloca_concat`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `Buffer`, `Align`, `DropSliceGuard`
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/alloca.rs` vs expected `collections/alloca.rs`
- **Proposed provenance header:** `// port-lint: source collections/alloca.rs` (current: `// port-lint: source src/collections/alloca.rs`)
- **Lint issues:** 1

### 105. stdlib

- **Target:** `starlark_kotlin.Stdlib [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1131710.0
- **Functions:** 3/14 matched (target 3)
- **Missing functions:** `test_no_arg`, `global`, `nop`, `test_value_attributes`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Bool2`, `Error`
- **Tests:** 0/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib.rs` vs expected `stdlib.rs`
- **Proposed provenance header:** `// port-lint: source stdlib.rs` (current: `// port-lint: source src/stdlib.rs`)
- **Lint issues:** 1

### 106. string.interpolation

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

### 107. types.list_or_tuple

- **Target:** `types.ListOrTuple [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1081010.0
- **Functions:** 1/5 matched
- **Missing functions:** `default`, `starlark_type_repr`, `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list_or_tuple.rs` vs expected `values/types/list_or_tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list_or_tuple.rs` (current: `// port-lint: source src/values/types/list_or_tuple.rs`)
- **Lint issues:** 1

### 108. layout.pointer

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

### 109. types.any_complex

- **Target:** `types.AnyComplex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1071210.0
- **Functions:** 4/7 matched (target 6)
- **Missing functions:** `fmt`, `test_any_complex`, `freeze`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `UnfrozenData`, `Frozen`, `FrozenData`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/any_complex.rs` vs expected `values/types/any_complex.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any_complex.rs` (current: `// port-lint: source src/values/types/any_complex.rs`)
- **Lint issues:** 1

### 110. types.any_array

- **Target:** `types.AnyArray [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1061010.0
- **Functions:** 3/7 matched
- **Missing functions:** `fmt`, `drop`, `test_drop`, `test_allocation_size`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `IncrementOnDrop`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/any_array.rs` vs expected `values/types/any_array.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any_array.rs` (current: `// port-lint: source src/values/types/any_array.rs`)
- **Lint issues:** 1

### 111. string.dot_format

- **Target:** `string.DotFormat [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1051210.0
- **Functions:** 6/11 matched (target 6)
- **Missing functions:** `new`, `format_capture_for_test`, `test_format_capture`, `test_format`, `test_parse_format_one`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/dot_format.rs` vs expected `values/types/string/dot_format.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/dot_format.rs` (current: `// port-lint: source src/values/types/string/dot_format.rs`)
- **Lint issues:** 1

### 112. util.rtabort

- **Target:** `util.Rtabort [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1050710.0
- **Functions:** 2/6 matched (target 3)
- **Missing functions:** `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`, `test_rtabort`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `AbortOnDrop`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/rtabort.rs` vs expected `util/rtabort.rs`
- **Proposed provenance header:** `// port-lint: source util/rtabort.rs` (current: `// port-lint: source src/util/rtabort.rs`)
- **Lint issues:** 1

### 113. stdlib.breakpoint

- **Target:** `stdlib.Breakpoint [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1042310.0
- **Functions:** 14/17 matched (target 18)
- **Missing functions:** `global`, `breakpoint`, `reset_global_state`
- **Types:** 5/6 matched (target 7)
- **Missing types:** `Handler`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/breakpoint.rs` vs expected `stdlib/breakpoint.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/breakpoint.rs` vs expected `stdlib/breakpoint.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/breakpoint.rs` (current: `// port-lint: source src/stdlib/breakpoint.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/breakpoint.rs` (current: `// port-lint: tests src/stdlib/breakpoint.rs`)
- **Lint issues:** 2

### 114. bc.if_debug

- **Target:** `bc.IfDebug [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1030910.0
- **Functions:** 5/8 matched (target 9)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/if_debug.rs` vs expected `eval/bc/if_debug.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/if_debug.rs` (current: `// port-lint: source src/eval/bc/if_debug.rs`)
- **Lint issues:** 1

### 115. util.non_static_type_id

- **Target:** `util.NonStaticTypeId [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1030410.0
- **Functions:** 1/3 matched (target 1)
- **Missing functions:** `get_type_id`, `test_non_static_type_id`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `NonStaticAny`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/non_static_type_id.rs` vs expected `util/non_static_type_id.rs`
- **Proposed provenance header:** `// port-lint: source util/non_static_type_id.rs` (current: `// port-lint: source src/util/non_static_type_id.rs`)
- **Lint issues:** 1

### 116. runtime.cheap_call_stack

- **Target:** `runtime.CheapCallStack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1022010.0
- **Functions:** 15/17 matched
- **Missing functions:** `fmt`, `default`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/cheap_call_stack.rs` vs expected `eval/runtime/cheap_call_stack.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/cheap_call_stack.rs` (current: `// port-lint: source src/eval/runtime/cheap_call_stack.rs`)
- **Lint issues:** 1

### 117. avalues.simple

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

### 118. layout.value_captured

- **Target:** `layout.ValueCaptured [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1020810.0
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/value_captured.rs` vs expected `values/layout/value_captured.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_captured.rs` (current: `// port-lint: source src/values/layout/value_captured.rs`)
- **Lint issues:** 1

### 119. record.field

- **Target:** `record.Field [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1020610.0
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 0/1 matched
- **Missing types:** `FieldGen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/field.rs` vs expected `values/types/record/field.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/field.rs` (current: `// port-lint: source src/values/types/record/field.rs`)
- **Lint issues:** 1

### 120. typing.bindings

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
- **Lint issues:** 1

### 121. typing.structs

- **Target:** `typing.Structs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1011010.0
- **Functions:** 7/8 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/structs.rs` vs expected `typing/structs.rs`
- **Proposed provenance header:** `// port-lint: source typing/structs.rs` (current: `// port-lint: source src/typing/structs.rs`)
- **Lint issues:** 1

### 122. heap.fast_cell

- **Target:** `heap.FastCell [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010810.0
- **Functions:** 6/7 matched
- **Missing functions:** `drop`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/fast_cell.rs` vs expected `values/layout/heap/fast_cell.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/fast_cell.rs` (current: `// port-lint: source src/values/layout/heap/fast_cell.rs`)
- **Lint issues:** 1

### 123. structs.unordered_hasher

- **Target:** `structs.UnorderedHasher [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010610.0
- **Functions:** 4/5 matched (target 4)
- **Missing functions:** `_write`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/unordered_hasher.rs` vs expected `values/types/structs/unordered_hasher.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/structs/unordered_hasher.rs` vs expected `values/types/structs/unordered_hasher.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/unordered_hasher.rs` (current: `// port-lint: source src/values/types/structs/unordered_hasher.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/structs/unordered_hasher.rs` (current: `// port-lint: tests src/values/types/structs/unordered_hasher.rs`)
- **Lint issues:** 2

### 124. read_line

- **Target:** `starlark_kotlin.ReadLine [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `NoRustyline`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/read_line.rs` vs expected `read_line.rs`
- **Proposed provenance header:** `// port-lint: source read_line.rs` (current: `// port-lint: source src/read_line.rs`)
- **Lint issues:** 1

### 125. typing.function

- **Target:** `typing.Function [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1001510.0
- **Functions:** 12/12 matched (target 24)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/function.rs` vs expected `typing/function.rs`
- **Proposed provenance header:** `// port-lint: source typing/function.rs` (current: `// port-lint: source src/typing/function.rs`)
- **TODOs:** 1
- **Lint issues:** 1

### 126. analysis.lint_message

- **Target:** `analysis.LintMessage [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000210.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/lint_message.rs` vs expected `analysis/lint_message.rs`
- **Proposed provenance header:** `// port-lint: source analysis/lint_message.rs` (current: `// port-lint: source src/analysis/lint_message.rs`)
- **Lint issues:** 1

### 127. types.none

- **Target:** `types.None [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none.rs` vs expected `values/types/none.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none.rs` (current: `// port-lint: source src/values/types/none.rs`)
- **Lint issues:** 1

### 128. types.bool

- **Target:** `types.Bool [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool.rs` vs expected `values/types/bool.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool.rs` (current: `// port-lint: source src/values/types/bool.rs`)
- **Lint issues:** 1

### 129. types.string

- **Target:** `types.String [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string.rs` vs expected `values/types/string.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string.rs` (current: `// port-lint: source src/values/types/string.rs`)
- **Lint issues:** 1

### 130. typing

- **Target:** `starlark_kotlin.Typing [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing.rs` vs expected `typing.rs`
- **Proposed provenance header:** `// port-lint: source typing.rs` (current: `// port-lint: source src/typing.rs`)
- **Lint issues:** 1

### 131. types.set

- **Target:** `types.Set [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set.rs` vs expected `values/types/set.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set.rs` vs expected `values/types/set.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set.rs` (current: `// port-lint: source src/values/types/set.rs`)
- **Proposed provenance header:** `// port-lint: source values/types/set.rs` (current: `// port-lint: source src/values/types/set.rs`)
- **Lint issues:** 2

### 132. types.enumeration

- **Target:** `types.Enumeration [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration.rs` vs expected `values/types/enumeration.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration.rs` (current: `// port-lint: source src/values/types/enumeration.rs`)
- **Lint issues:** 1

### 133. types.int

- **Target:** `types.Int [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int.rs` vs expected `values/types/int.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int.rs` (current: `// port-lint: source src/values/types/int.rs`)
- **Lint issues:** 1

### 134. bc.instr_impl

- **Target:** `bc.InstrImpl [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 777010.0
- **Functions:** 7/7 matched (target 96)
- **Missing functions:** _none_
- **Types:** 87/163 matched (target 103)
- **Missing types:** `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instr_impl.rs` vs expected `eval/bc/instr_impl.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr_impl.rs` (current: `// port-lint: source src/eval/bc/instr_impl.rs`)
- **Lint issues:** 1

### 135. set.methods

- **Target:** `set.Methods [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 506910.0
- **Functions:** 18/68 matched (target 19)
- **Missing functions:** `test_empty`, `test_single`, `test_eq`, `test_clear`, `test_type`, `test_iter`, `test_bool_true`, `test_bool_false`, `test_union`, `test_union_empty`, `test_union_iter`, `test_union_ordering_mixed`, `test_intersection`, `test_intersection_empty`, `test_intersection_iter`, `test_intersection_order`, `test_symmetric_difference`, `test_symmetric_difference_empty`, `test_symmetric_difference_iter`, `test_symmetric_difference_ord`, `test_add`, `test_add_empty`, `test_add_existing`, `test_add_order`, `test_remove`, `test_remove_empty`, `test_remove_not_existing`, `test_discard`, `test_discard_multiple_times`, `test_pop`, `test_pop_empty`, `test_difference`, `test_difference_iter`, `test_difference_order`, `test_difference_empty_lhs`, `test_difference_empty_rhs`, `test_is_superset`, `test_is_not_superset`, `test_is_not_superset_empty_lhs`, `test_is_superset_empty_rhs`, `test_is_superset_iter`, `test_is_subset`, `test_is_not_subset`, `test_is_subset_empty_lhs`, `test_is_not_subset_empty_rhs`, `test_is_subset_iter`, `test_update`, `test_update_empty`, `test_update_self`, `test_update_frozen_set_cannot_be_updated_with_self`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/50 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/methods.rs` vs expected `values/types/set/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/methods.rs` (current: `// port-lint: source src/values/types/set/methods.rs`)
- **Lint issues:** 1

### 136. string.str_type

- **Target:** `string.StrType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 495110.0
- **Functions:** 2/47 matched (target 24)
- **Missing functions:** `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `payload_len_for_len`, `new`, `as_str`, `as_aligned_padded_str`, `get_hash`, `as_str_hashed`, `len`, `is_empty`, `offset_of_content`, `repr`, `is_special`, `get_methods`, `collect_repr`, `to_bool`, `write_hash`, `equals`, `compare`, `at`, `length`, `is_in`, `slice`, `start_stop_to_none_or`, `add`, `mul`, `rmul`, `percent`, `typechecker_ty`, `serialize`, `test_string_corruption`, `test_escape_characters`, `test_string_hash`, `test_zero_length_string_hash_is_not_zero`, `test_string_len`, `test_arithmetic_on_string`, `test_slice_string`, `test_string_is_in`, `test_successive_add`, `test_string_index`, `test_str`
- **Types:** 0/4 matched (target 0)
- **Missing types:** `StarlarkStrN`, `StarlarkStr`, `Frozen`, `Target`
- **Tests:** 0/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/str_type.rs` vs expected `values/types/string/str_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/str_type.rs` (current: `// port-lint: source src/values/types/string/str_type.rs`)
- **Lint issues:** 1

### 137. set.value

- **Target:** `set.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 235910.0
- **Functions:** 30/50 matched (target 44)
- **Missing functions:** `fmt`, `test_bit_or`, `test_bit_or_lhs_empty`, `test_bit_or_rhs_empty`, `test_bit_or_fail_iter`, `test_bit_or_ord`, `test_bit_and`, `test_bit_and_lhs_empty`, `test_bit_and_rhs_empty`, `test_bit_and_ord`, `test_bit_and_fail_iter`, `test_bit_xor`, `test_bit_xor_ord`, `test_bit_xor_lhs_empty`, `test_bit_xor_rhs_empty`, `test_bit_xor_fail_iter`, `test_sub`, `test_sub_empty_lhs`, `test_sub_empty_rhs`, `test_sub_fail_iter`
- **Types:** 6/9 matched (target 8)
- **Missing types:** `Canonical`, `Frozen`, `ContentRef`
- **Tests:** 0/19 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/value.rs` vs expected `values/types/set/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/value.rs` (current: `// port-lint: source src/values/types/set/value.rs`)
- **Lint issues:** 1

### 138. thin_box_slice_frozen_value.thin_box

- **Target:** `thin_box_slice_frozen_value.ThinBox [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 213210.0
- **Functions:** 10/29 matched (target 15)
- **Missing functions:** `offset_of_data`, `get_reserved_tag_bit_count`, `get_unshifted_tag_bit_mask`, `get_tag_bit_mask`, `get_max_short_len`, `layout_for_len`, `get_tag_bits`, `as_ptr`, `as_nonnull_ptr`, `from_inner`, `deref`, `deref_mut`, `assume_init`, `default`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `ThinBoxSliceLayout`, `Target`
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thin_box_slice_frozen_value/thin_box.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/thin_box_slice_frozen_value/thin_box.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: source src/values/thin_box_slice_frozen_value/thin_box.rs`)
- **Proposed provenance header:** `// port-lint: tests values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: tests src/values/thin_box_slice_frozen_value/thin_box.rs`)
- **Lint issues:** 2

### 139. int.int_or_big

- **Target:** `int.IntOrBig [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 185310.0
- **Functions:** 32/46 matched (target 61)
- **Missing functions:** `starlark_type_repr`, `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`
- **Types:** 3/7 matched (target 12)
- **Missing types:** `Canonical`, `Err`, `Error`, `Output`
- **Tests:** 8/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/int_or_big.rs` vs expected `values/types/int/int_or_big.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/int/int_or_big.rs` vs expected `values/types/int/int_or_big.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/int_or_big.rs` (current: `// port-lint: source src/values/types/int/int_or_big.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/int/int_or_big.rs` (current: `// port-lint: tests src/values/types/int/int_or_big.rs`)
- **TODOs:** 3
- **Lint issues:** 2

### 140. layout.typed

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

### 141. typing.user

- **Target:** `typing.User [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 153510.0
- **Functions:** 15/27 matched
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`
- **Types:** 5/8 matched (target 9)
- **Missing types:** `AbstractPlant`, `FruitCallable`, `Fruit`
- **Tests:** 2/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/user.rs` vs expected `typing/user.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/typing/user.rs` vs expected `typing/user.rs`
- **Proposed provenance header:** `// port-lint: source typing/user.rs` (current: `// port-lint: source src/typing/user.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/user.rs` (current: `// port-lint: tests src/typing/user.rs`)
- **Lint issues:** 2

### 142. scope.payload

- **Target:** `scope.Payload [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 152410.0
- **Functions:** 0/7 matched (target 0)
- **Missing functions:** `map_load`, `map_ident`, `map_ident_assign`, `map_def`, `map_type_expr`, `from_ast`, `resolved_binding_id`
- **Types:** 9/17 matched (target 14)
- **Missing types:** `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CompilerAstMap`, `CstStmtFromAst`, `CstAssignIdentExt`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope/payload.rs` vs expected `eval/compiler/scope/payload.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope/payload.rs` (current: `// port-lint: source src/eval/compiler/scope/payload.rs`)
- **Lint issues:** 1

### 143. values.typing.callable

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.values.typing.Callable [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 134010.0
- **Functions:** 22/32 matched (target 41)
- **Missing functions:** `clone`, `fmt`, `trace`, `_assert_sync_send`, `_assert`, `my_module`, `accept_f`, `module`, `good`, `bad`
- **Types:** 5/8 matched (target 6)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 10/15 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/callable.rs` vs expected `values/typing/callable.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/callable.rs` vs expected `values/typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/callable.rs` (current: `// port-lint: source src/values/typing/callable.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/callable.rs` (current: `// port-lint: tests src/values/typing/callable.rs`)
- **Lint issues:** 2

### 144. num.value

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

### 145. analysis

- **Target:** `starlark_kotlin.Analysis [ZERO] [PROVENANCE-FALLBACK]`
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

### 146. dict.value

- **Target:** `dict.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 116210.0
- **Functions:** 44/52 matched (target 68)
- **Missing functions:** `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`, `test_mutate_dict`, `test_get_str`, `test_repr_cycle`
- **Types:** 7/10 matched
- **Missing types:** `Canonical`, `Frozen`, `ContentRef`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/value.rs` vs expected `values/types/dict/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/value.rs` (current: `// port-lint: source src/values/types/dict/value.rs`)
- **Lint issues:** 1

### 147. pagable.vtable_registry

- **Target:** `pagable.VtableRegistry [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 111710.0
- **Functions:** 4/13 matched (target 12)
- **Missing functions:** `fmt`, `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered`
- **Types:** 2/4 matched (target 8)
- **Missing types:** `TestSimpleType`, `TestComplexGen`
- **Tests:** 1/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pagable/vtable_registry.rs` vs expected `pagable/vtable_registry.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/pagable/vtable_registry.rs` vs expected `pagable/vtable_registry.rs`
- **Proposed provenance header:** `// port-lint: source pagable/vtable_registry.rs` (current: `// port-lint: source src/pagable/vtable_registry.rs`)
- **Proposed provenance header:** `// port-lint: tests pagable/vtable_registry.rs` (current: `// port-lint: tests src/pagable/vtable_registry.rs`)
- **Lint issues:** 2

### 148. record.globals

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

### 149. heap.heap_type

- **Target:** `heap.HeapType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 105510.0
- **Functions:** 37/47 matched (target 59)
- **Missing functions:** `fmt`, `_test_frozen_heap_ref_send_sync`, `hash`, `eq`, `test_send_sync`, `test_string_reallocated_on_heap`, `test_interned_string_equal`, `validate_str_interning`, `append_x`, `test_interned_str_starlark`
- **Types:** 8/8 matched (target 9)
- **Missing types:** _none_
- **Tests:** 0/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/heap_type.rs` vs expected `values/layout/heap/heap_type.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/heap_type.rs` (current: `// port-lint: source src/values/layout/heap/heap_type.rs`)
- **Lint issues:** 1

### 150. thin_box_slice_frozen_value.packed_impl

- **Target:** `thin_box_slice_frozen_value.PackedImpl [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102110.0
- **Functions:** 9/18 matched (target 14)
- **Missing functions:** `new_allocated`, `unpack`, `drop`, `visit`, `deref`, `default`, `fmt`, `eq`, `across_lengths`
- **Types:** 2/3 matched
- **Missing types:** `Target`
- **Tests:** 5/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thin_box_slice_frozen_value/packed_impl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/thin_box_slice_frozen_value/packed_impl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: source src/values/thin_box_slice_frozen_value/packed_impl.rs`)
- **Proposed provenance header:** `// port-lint: tests values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: tests src/values/thin_box_slice_frozen_value/packed_impl.rs`)
- **Lint issues:** 2

### 151. typing.small_arc_vec_or_static

- **Target:** `typing.SmallArcVecOrStatic [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 101510.0
- **Functions:** 3/10 matched
- **Missing functions:** `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Target`, `Item`, `IntoIter`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/small_arc_vec_or_static.rs` vs expected `typing/small_arc_vec_or_static.rs`
- **Proposed provenance header:** `// port-lint: source typing/small_arc_vec_or_static.rs` (current: `// port-lint: source src/typing/small_arc_vec_or_static.rs`)
- **Lint issues:** 1

### 152. layout.vtable

- **Target:** `layout.Vtable [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 87310.0
- **Functions:** 61/67 matched (target 65)
- **Missing functions:** `value_ptr`, `drop_in_place`, `fmt`, `as_allocative`, `total_memory_for_profile`, `as_serialize`
- **Types:** 4/6 matched (target 4)
- **Missing types:** `GetTypeId`, `GetAllocativeKey`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/vtable.rs` vs expected `values/layout/vtable.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/vtable.rs` (current: `// port-lint: source src/values/layout/vtable.rs`)
- **Lint issues:** 1

### 153. type_compiled.compiled

- **Target:** `type_compiled.Compiled [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 84610.0
- **Functions:** 33/39 matched (target 47)
- **Missing functions:** `fmt`, `check_matches`, `starlark_type_repr`, `alloc_value`, `hash`, `eq`
- **Types:** 5/7 matched (target 12)
- **Missing types:** `StaticType`, `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/compiled.rs` vs expected `values/typing/type_compiled/compiled.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/compiled.rs` (current: `// port-lint: source src/values/typing/type_compiled/compiled.rs`)
- **Lint issues:** 1

### 154. analysis.flow

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

### 155. range.range_type

- **Target:** `range.RangeType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 82510.0
- **Functions:** 16/24 matched (target 30)
- **Missing functions:** `fmt`, `eq`, `range`, `range_start_stop`, `range_stop`, `length_stop`, `length_start_stop`, `length_start_stop_step`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/range/range_type.rs` vs expected `values/types/range/range_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/range/range_type.rs` vs expected `values/types/range/range_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/range/range_type.rs` (current: `// port-lint: source src/values/types/range/range_type.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/range/range_type.rs` (current: `// port-lint: tests src/values/types/range/range_type.rs`)
- **Lint issues:** 2

### 156. typing.small_arc_vec

- **Target:** `typing.SmallArcVec [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 81410.0
- **Functions:** 4/11 matched (target 16)
- **Missing functions:** `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter`
- **Types:** 2/3 matched (target 5)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/small_arc_vec.rs` vs expected `typing/small_arc_vec.rs`
- **Proposed provenance header:** `// port-lint: source typing/small_arc_vec.rs` (current: `// port-lint: source src/typing/small_arc_vec.rs`)
- **Lint issues:** 1

### 157. tuple.unpack

- **Target:** `tuple.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 81010.0
- **Functions:** 1/5 matched
- **Missing functions:** `default`, `starlark_type_repr`, `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 1)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/unpack.rs` vs expected `values/types/tuple/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/unpack.rs` (current: `// port-lint: source src/values/types/tuple/unpack.rs`)
- **Lint issues:** 1

### 158. float.float

- **Target:** `float.Float [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 74210.0
- **Functions:** 34/39 matched (target 45)
- **Missing functions:** `fmt`, `non_finite`, `decimal`, `scientific`, `compact`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 8/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/float.rs` vs expected `values/types/float/float.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/float/float.rs` vs expected `values/types/float/float.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/float.rs` (current: `// port-lint: source src/values/types/float/float.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/float/float.rs` (current: `// port-lint: tests src/values/types/float/float.rs`)
- **Lint issues:** 2

### 159. profile.aggregated

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

### 160. string.repr

- **Target:** `string.Repr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 72310.0
- **Functions:** 15/22 matched (target 20)
- **Missing functions:** `or4`, `push_vec_tail`, `test_to_repr`, `test`, `string_repr_for_test`, `test_chunk_non_ascii_or_need_escape`, `load`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/repr.rs` vs expected `values/types/string/repr.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/repr.rs` vs expected `values/types/string/repr.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/repr.rs` (current: `// port-lint: source src/values/types/string/repr.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/string/repr.rs` (current: `// port-lint: tests src/values/types/string/repr.rs`)
- **Lint issues:** 2

### 161. typed.string

- **Target:** `typed.String [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71810.0
- **Functions:** 8/15 matched (target 59)
- **Missing functions:** `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`, `test_string_hashes`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/typed/string.rs` vs expected `values/layout/typed/string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/typed/string.rs` (current: `// port-lint: source src/values/layout/typed/string.rs`)
- **Lint issues:** 1

### 162. dict.methods

- **Target:** `dict.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71710.0
- **Functions:** 10/17 matched (target 12)
- **Missing functions:** `test_error_codes`, `test_dict_add`, `test_dict_with_duplicates`, `test_dict_update_with_self_pos`, `test_dict_update_with_self_as_kwargs`, `test_frozen_dict_cannot_be_updated_with_self_pos`, `test_frozen_dict_cannot_be_updated_with_self_as_kwargs`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/methods.rs` vs expected `values/types/dict/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/methods.rs` (current: `// port-lint: source src/values/types/dict/methods.rs`)
- **Lint issues:** 1

### 163. layout.complex

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

### 164. bigint.convert

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

### 165. string.simd

- **Target:** `string.Simd [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 71010.0
- **Functions:** 1/8 matched (target 4)
- **Missing functions:** `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/simd.rs` vs expected `values/types/string/simd.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `src/vec_map/simd.rs` vs expected `values/types/string/simd.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/simd.rs` (current: `// port-lint: source src/values/types/string/simd.rs`)
- **Proposed provenance header:** `// port-lint: source values/types/string/simd.rs` (current: `// port-lint: source src/vec_map/simd.rs`)
- **Lint issues:** 2

### 166. compiler.scope

- **Target:** `compiler.Scope [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 67110.0
- **Functions:** 48/51 matched (target 70)
- **Missing functions:** `from`, `assign_ident_impl`, `new`
- **Types:** 17/20 matched (target 28)
- **Missing types:** `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope.rs` vs expected `eval/compiler/scope.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope.rs` (current: `// port-lint: source src/eval/compiler/scope.rs`)
- **Lint issues:** 1

### 167. assert.assert

- **Target:** `assert.Assert [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 65210.0
- **Functions:** 44/50 matched (target 64)
- **Missing functions:** `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/assert/assert.rs` vs expected `assert/assert.rs`
- **Proposed provenance header:** `// port-lint: source assert/assert.rs` (current: `// port-lint: source src/assert/assert.rs`)
- **TODOs:** 1
- **Lint issues:** 1

### 168. adapter.implementation

- **Target:** `adapter.Implementation [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62910.0
- **Functions:** 17/23 matched (target 27)
- **Missing functions:** `prepare_dap_adapter`, `fmt`, `new`, `continue_`, `breakpoint`, `resolve_breakpoints`
- **Types:** 6/6 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter/implementation.rs` vs expected `debug/adapter/implementation.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter/implementation.rs` (current: `// port-lint: source src/debug/adapter/implementation.rs`)
- **Lint issues:** 1

### 169. bc.instrs

- **Target:** `bc.Instrs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62810.0
- **Functions:** 19/24 matched (target 29)
- **Missing functions:** `handle`, `drop`, `opcodes`, `fmt`, `display`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `HandlerImpl`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrs.rs` vs expected `eval/bc/instrs.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instrs.rs` (current: `// port-lint: source src/eval/bc/instrs.rs`)
- **Lint issues:** 1

### 170. structs.value

- **Target:** `structs.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62210.0
- **Functions:** 15/21 matched (target 18)
- **Missing functions:** `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_to_json`, `test_comparison_bug`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/value.rs` vs expected `values/types/structs/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/value.rs` (current: `// port-lint: source src/values/types/structs/value.rs`)
- **Lint issues:** 1

### 171. stdlib.extra

- **Target:** `stdlib.Extra [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 62010.0
- **Functions:** 11/16 matched (target 24)
- **Missing functions:** `fmt`, `print`, `pprint`, `pstr`, `prepr`
- **Types:** 3/4 matched
- **Missing types:** `PrintHandlerImpl`
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/extra.rs` vs expected `stdlib/extra.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/extra.rs` vs expected `stdlib/extra.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/extra.rs` (current: `// port-lint: source src/stdlib/extra.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/extra.rs` (current: `// port-lint: tests src/stdlib/extra.rs`)
- **Lint issues:** 2

### 172. analysis.dubious

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

### 173. analysis.types

- **Target:** `analysis.Types [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61210.0
- **Functions:** 4/7 matched (target 12)
- **Missing functions:** `fmt`, `new`, `from`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LintWarning`, `LintT`, `EvalSeverity`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/types.rs` vs expected `analysis/types.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/opt/types.rs` vs expected `analysis/types.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/typing/tests/types.rs` vs expected `analysis/types.rs`
- **Proposed provenance header:** `// port-lint: source analysis/types.rs` (current: `// port-lint: source src/analysis/types.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/types.rs` (current: `// port-lint: tests tests/opt/types.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/types.rs` (current: `// port-lint: tests src/typing/tests/types.rs`)
- **Lint issues:** 3

### 174. stdlib.json

- **Target:** `stdlib.Json [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61210.0
- **Functions:** 6/11 matched (target 28)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `json`, `encode`, `decode`
- **Types:** 0/1 matched (target 12)
- **Missing types:** `Canonical`
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/json.rs` vs expected `stdlib/json.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/json.rs` vs expected `stdlib/json.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/json.rs` (current: `// port-lint: source src/stdlib/json.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/json.rs` (current: `// port-lint: tests src/stdlib/json.rs`)
- **Lint issues:** 2

### 175. heap.send

- **Target:** `heap.Send [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61110.0
- **Functions:** 2/5 matched (target 6)
- **Missing functions:** `deref`, `deref_mut`, `fmt`
- **Types:** 3/6 matched (target 3)
- **Missing types:** `Sealed`, `Target`, `StaticType`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/send.rs` vs expected `values/layout/heap/send.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/send.rs` (current: `// port-lint: source src/values/layout/heap/send.rs`)
- **Lint issues:** 1

### 176. int.i32

- **Target:** `int.I32 [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60610.0
- **Functions:** 0/4 matched (target 3)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/i32.rs` vs expected `values/types/int/i32.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/i32.rs` (current: `// port-lint: source src/values/types/int/i32.rs`)
- **Lint issues:** 1

### 177. tuple.rust_tuple

- **Target:** `tuple.RustTuple [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60610.0
- **Functions:** 0/4 matched (target 11)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/rust_tuple.rs` vs expected `values/types/tuple/rust_tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/rust_tuple.rs` (current: `// port-lint: source src/values/types/tuple/rust_tuple.rs`)
- **Lint issues:** 1

### 178. list.value

- **Target:** `list.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 56410.0
- **Functions:** 53/56 matched (target 113)
- **Missing functions:** `fmt`, `error`, `starlark_type_repr`
- **Types:** 6/8 matched (target 10)
- **Missing types:** `List`, `Canonical`
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/value.rs` vs expected `values/types/list/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/value.rs` vs expected `values/types/list/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/value.rs` (current: `// port-lint: source src/values/types/list/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/list/value.rs` (current: `// port-lint: tests src/values/types/list/value.rs`)
- **Lint issues:** 2

### 179. environment.modules

- **Target:** `environment.Modules [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 54710.0
- **Functions:** 38/43 matched (target 47)
- **Missing functions:** `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/modules.rs` vs expected `environment/modules.rs`
- **Proposed provenance header:** `// port-lint: source environment/modules.rs` (current: `// port-lint: source src/environment/modules.rs`)
- **Lint issues:** 1

### 180. params.spec

- **Target:** `params.Spec [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 54410.0
- **Functions:** 33/38 matched (target 33)
- **Missing functions:** `as_value`, `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl`
- **Types:** 6/6 matched (target 11)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/spec.rs` vs expected `eval/runtime/params/spec.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/spec.rs` (current: `// port-lint: source src/eval/runtime/params/spec.rs`)
- **Lint issues:** 1

### 181. analysis.names

- **Target:** `analysis.Names [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 54310.0
- **Functions:** 31/35 matched (target 42)
- **Missing functions:** `new`, `ident`, `assign_ident`, `about`
- **Types:** 7/8 matched (target 14)
- **Missing types:** `AstStrExt`
- **Tests:** 9/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/names.rs` vs expected `analysis/names.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/names.rs` vs expected `analysis/names.rs`
- **Proposed provenance header:** `// port-lint: source analysis/names.rs` (current: `// port-lint: source src/analysis/names.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/names.rs` (current: `// port-lint: tests src/analysis/names.rs`)
- **Lint issues:** 2

### 182. values.owned

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

### 183. profile.time_flame

- **Target:** `profile.TimeFlame [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 53010.0
- **Functions:** 15/19 matched (target 18)
- **Missing functions:** `r#gen`, `test_time_flame_works_inside_frozen_module`, `register_sleep`, `sleep`
- **Types:** 10/11 matched (target 15)
- **Missing types:** `Data`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/time_flame.rs` vs expected `eval/runtime/profile/time_flame.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/time_flame.rs` (current: `// port-lint: source src/eval/runtime/profile/time_flame.rs`)
- **Lint issues:** 1

### 184. typing.callable_param

- **Target:** `typing.CallableParam [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 52610.0
- **Functions:** 16/20 matched (target 27)
- **Missing functions:** `fmt`, `pf`, `new_named_only`, `test_param_spec_display`
- **Types:** 5/6 matched (target 10)
- **Missing types:** `ParamSpecDisplay`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callable_param.rs` vs expected `typing/callable_param.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable_param.rs` (current: `// port-lint: source src/typing/callable_param.rs`)
- **Lint issues:** 1

### 185. alloc.allocator

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/allocator.rs` vs expected `values/layout/heap/allocator/alloc/allocator.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/allocator.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/allocator.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/allocator.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/allocator.rs`)
- **Lint issues:** 2

### 186. stdlib.partial

- **Target:** `stdlib.Partial [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51710.0
- **Functions:** 9/12 matched (target 13)
- **Missing functions:** `partial`, `fmt`, `eq`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Frozen`, `Canonical`
- **Tests:** 5/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/partial.rs` vs expected `stdlib/partial.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/partial.rs` vs expected `stdlib/partial.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/partial.rs` (current: `// port-lint: source src/stdlib/partial.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/partial.rs` (current: `// port-lint: tests src/stdlib/partial.rs`)
- **Lint issues:** 2

### 187. dict.refs

- **Target:** `dict.Refs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51610.0
- **Functions:** 7/9 matched (target 13)
- **Missing functions:** `from_value`, `deref`
- **Types:** 4/7 matched (target 11)
- **Missing types:** `Target`, `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/refs.rs` vs expected `values/types/dict/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/refs.rs` (current: `// port-lint: source src/values/types/dict/refs.rs`)
- **Lint issues:** 1

### 188. values.unpack

- **Target:** `values.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51610.0
- **Functions:** 8/9 matched (target 14)
- **Missing functions:** `error`
- **Types:** 3/7 matched
- **Missing types:** `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/unpack.rs` vs expected `values/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/unpack.rs` (current: `// port-lint: source src/values/unpack.rs`)
- **Lint issues:** 1

### 189. analysis.underscore

- **Target:** `analysis.Underscore [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51410.0
- **Functions:** 8/13 matched (target 17)
- **Missing functions:** `lint`, `about`, `module`, `test_lint_inappropriate_underscore`, `test_lint_use_ignored`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/underscore.rs` vs expected `analysis/underscore.rs`
- **Proposed provenance header:** `// port-lint: source analysis/underscore.rs` (current: `// port-lint: source src/analysis/underscore.rs`)
- **Lint issues:** 1

### 190. allocator.bumpalo

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

### 191. list.unpack

- **Target:** `list.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 51010.0
- **Functions:** 4/5 matched (target 11)
- **Missing functions:** `into_iter`
- **Types:** 1/5 matched
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/unpack.rs` vs expected `values/types/list/unpack.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/unpack.rs` vs expected `values/types/list/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/unpack.rs` (current: `// port-lint: source src/values/types/list/unpack.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/list/unpack.rs` (current: `// port-lint: tests src/values/types/list/unpack.rs`)
- **Lint issues:** 2

### 192. string.methods

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
- **Lint issues:** 1

### 193. typing.custom

- **Target:** `typing.Custom [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 43810.0
- **Functions:** 31/35 matched (target 49)
- **Missing functions:** `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/custom.rs` vs expected `typing/custom.rs`
- **Proposed provenance header:** `// port-lint: source typing/custom.rs` (current: `// port-lint: source src/typing/custom.rs`)
- **Lint issues:** 1

### 194. tuple.value

- **Target:** `tuple.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 43410.0
- **Functions:** 27/31 matched (target 28)
- **Missing functions:** `fmt`, `new`, `offset_of_content`, `typechecker_ty`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/value.rs` vs expected `values/types/tuple/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/value.rs` vs expected `values/types/tuple/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/value.rs` (current: `// port-lint: source src/values/types/tuple/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/value.rs` (current: `// port-lint: tests src/values/types/tuple/value.rs`)
- **Lint issues:** 2

### 195. heap.repr

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

### 196. bc.addr

- **Target:** `bc.Addr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 42910.0
- **Functions:** 20/23 matched (target 35)
- **Missing functions:** `add_assign`, `get_instr_mut`, `sub_usize`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/addr.rs` vs expected `eval/bc/addr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/addr.rs` (current: `// port-lint: source src/eval/bc/addr.rs`)
- **Lint issues:** 1

### 197. profile.bc

- **Target:** `profile.Bc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 42910.0
- **Functions:** 16/19 matched (target 28)
- **Missing functions:** `sum`, `add_assign`, `default`
- **Types:** 9/10 matched (target 14)
- **Missing types:** `Data`
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/bc.rs` vs expected `eval/runtime/profile/bc.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/bc.rs` vs expected `eval/runtime/profile/bc.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/bc.rs` (current: `// port-lint: source src/eval/runtime/profile/bc.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/bc.rs` (current: `// port-lint: tests src/eval/runtime/profile/bc.rs`)
- **Lint issues:** 2

### 198. analysis.incompatible

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

### 199. profile.csv

- **Target:** `profile.Csv [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41310.0
- **Functions:** 8/10 matched (target 9)
- **Missing functions:** `new`, `format_for_csv`
- **Types:** 1/3 matched
- **Missing types:** `Impl`, `CsvValue`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/csv.rs` vs expected `eval/runtime/profile/csv.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/csv.rs` vs expected `eval/runtime/profile/csv.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/csv.rs` (current: `// port-lint: source src/eval/runtime/profile/csv.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/csv.rs` (current: `// port-lint: tests src/eval/runtime/profile/csv.rs`)
- **Lint issues:** 2

### 200. profile.typecheck

- **Target:** `profile.Typecheck [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41310.0
- **Functions:** 5/8 matched (target 6)
- **Missing functions:** `r#gen`, `test_typecheck_profile`, `test_typecheck_profile_merge`
- **Types:** 4/5 matched
- **Missing types:** `Data`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/typecheck.rs` vs expected `eval/runtime/profile/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/typecheck.rs` (current: `// port-lint: source src/eval/runtime/profile/typecheck.rs`)
- **Lint issues:** 1

### 201. analysis.performance

- **Target:** `analysis.Performance [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 41110.0
- **Functions:** 6/10 matched (target 14)
- **Missing functions:** `lint`, `module`, `test_lint_matches_dict_issue`, `test_lint_matches_any_function`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/performance.rs` vs expected `analysis/performance.rs`
- **Proposed provenance header:** `// port-lint: source analysis/performance.rs` (current: `// port-lint: source src/analysis/performance.rs`)
- **Lint issues:** 1

### 202. profile.mode

- **Target:** `profile.Mode [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40610.0
- **Functions:** 1/4 matched
- **Missing functions:** `fmt`, `name`, `from_str`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Err`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/mode.rs` vs expected `eval/runtime/profile/mode.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/mode.rs` (current: `// port-lint: source src/eval/runtime/profile/mode.rs`)
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

### 205. dict.alloc

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

### 206. alloc.chain

- **Target:** `alloc.Chain [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 32710.0
- **Functions:** 21/22 matched (target 26)
- **Missing functions:** `drop`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Item`, `ResetSplitAtZeroTest`
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chain.rs` vs expected `values/layout/heap/allocator/alloc/chain.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chain.rs` vs expected `values/layout/heap/allocator/alloc/chain.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chain.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chain.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chain.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chain.rs`)
- **Lint issues:** 2

### 207. type_compiled.matcher

- **Target:** `type_compiled.Matcher [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31710.0
- **Functions:** 10/10 matched (target 13)
- **Missing functions:** _none_
- **Types:** 4/7 matched
- **Missing types:** `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/matcher.rs` vs expected `values/typing/type_compiled/matcher.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/matcher.rs` (current: `// port-lint: source src/values/typing/type_compiled/matcher.rs`)
- **Lint issues:** 1

### 208. list.refs

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

### 209. avalues.list

- **Target:** `avalues.List [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31410.0
- **Functions:** 9/10 matched (target 24)
- **Missing functions:** `alloc_list_concat`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/list.rs` vs expected `values/layout/avalues/list.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/typing/tests/list.rs` vs expected `values/layout/avalues/list.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/list.rs` (current: `// port-lint: source src/values/layout/avalues/list.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/avalues/list.rs` (current: `// port-lint: tests src/typing/tests/list.rs`)
- **Lint issues:** 2

### 210. symbol.map

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

### 211. runtime.inlined_frame

- **Target:** `runtime.InlinedFrame [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31210.0
- **Functions:** 6/9 matched
- **Missing functions:** `eq`, `make_span`, `assert_stack`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/inlined_frame.rs` vs expected `eval/runtime/inlined_frame.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/inlined_frame.rs` vs expected `eval/runtime/inlined_frame.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/inlined_frame.rs` (current: `// port-lint: source src/eval/runtime/inlined_frame.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/inlined_frame.rs` (current: `// port-lint: tests src/eval/runtime/inlined_frame.rs`)
- **Lint issues:** 2

### 212. bc.opcode

- **Target:** `bc.Opcode [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31210.0
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `opcode_count`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `ByNumber`, `FindOpcode`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/opcode.rs` vs expected `eval/bc/opcode.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/opcode.rs` (current: `// port-lint: source src/eval/bc/opcode.rs`)
- **Lint issues:** 1

### 213. tuple.refs

- **Target:** `tuple.Refs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31110.0
- **Functions:** 6/7 matched (target 15)
- **Missing functions:** `unpack_value_impl`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/refs.rs` vs expected `values/types/tuple/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/refs.rs` (current: `// port-lint: source src/values/types/tuple/refs.rs`)
- **Lint issues:** 1

### 214. enumeration.value

- **Target:** `enumeration.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31010.0
- **Functions:** 6/9 matched (target 10)
- **Missing functions:** `fmt`, `index`, `value`
- **Types:** 1/1 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/value.rs` vs expected `values/types/enumeration/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/value.rs` (current: `// port-lint: source src/values/types/enumeration/value.rs`)
- **Lint issues:** 1

### 215. debug.inspect

- **Target:** `debug.Inspect [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30910.0
- **Functions:** 6/9 matched (target 7)
- **Missing functions:** `debugger`, `debug_inspect_stack`, `debug_inspect_variables`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/inspect.rs` vs expected `debug/inspect.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/debug/inspect.rs` vs expected `debug/inspect.rs`
- **Proposed provenance header:** `// port-lint: source debug/inspect.rs` (current: `// port-lint: source src/debug/inspect.rs`)
- **Proposed provenance header:** `// port-lint: tests debug/inspect.rs` (current: `// port-lint: tests src/debug/inspect.rs`)
- **Lint issues:** 2

### 216. bc.repr

- **Target:** `bc.Repr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30910.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `size_of_repr`, `handle`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/repr.rs` vs expected `eval/bc/repr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/repr.rs` (current: `// port-lint: source src/eval/bc/repr.rs`)
- **Lint issues:** 1

### 217. string.alloc_unpack

- **Target:** `string.AllocUnpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30810.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `unpack_value_impl`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/alloc_unpack.rs` vs expected `values/types/string/alloc_unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/alloc_unpack.rs` (current: `// port-lint: source src/values/types/string/alloc_unpack.rs`)
- **Lint issues:** 1

### 218. float.unpack

- **Target:** `float.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30610.0
- **Functions:** 2/3 matched
- **Missing functions:** `test_unpack_float`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/unpack.rs` vs expected `values/types/float/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/unpack.rs` (current: `// port-lint: source src/values/types/float/unpack.rs`)
- **Lint issues:** 1

### 219. dict.unpack

- **Target:** `dict.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30610.0
- **Functions:** 2/3 matched
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/unpack.rs` vs expected `values/types/dict/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/unpack.rs` (current: `// port-lint: source src/values/types/dict/unpack.rs`)
- **Lint issues:** 1

### 220. type_compiled.globals

- **Target:** `type_compiled.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 30410.0
- **Functions:** 1/4 matched (target 1)
- **Missing functions:** `eval_type`, `isinstance`, `test_typechecking`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/globals.rs` vs expected `values/typing/type_compiled/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/globals.rs` (current: `// port-lint: source src/values/typing/type_compiled/globals.rs`)
- **Lint issues:** 1

### 221. compiler.expr

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

### 222. values.traits

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
- **Lint issues:** 1

### 223. compiler.def

- **Target:** `compiler.Def [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 25210.0
- **Functions:** 38/39 matched (target 46)
- **Missing functions:** `fmt`
- **Types:** 12/13 matched (target 17)
- **Missing types:** `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/def.rs` vs expected `eval/compiler/def.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def.rs` (current: `// port-lint: source src/eval/compiler/def.rs`)
- **Lint issues:** 1

### 224. profile.stmt

- **Target:** `profile.Stmt [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 22610.0
- **Functions:** 16/17 matched (target 25)
- **Missing functions:** `r#gen`
- **Types:** 8/9 matched (target 10)
- **Missing types:** `Data`
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/stmt.rs` vs expected `eval/runtime/profile/stmt.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/stmt.rs` vs expected `eval/runtime/profile/stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/stmt.rs` (current: `// port-lint: source src/eval/runtime/profile/stmt.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/stmt.rs` (current: `// port-lint: tests src/eval/runtime/profile/stmt.rs`)
- **Lint issues:** 2

### 225. types.function

- **Target:** `types.Function [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 22510.0
- **Functions:** 12/13 matched (target 27)
- **Missing functions:** `new`
- **Types:** 11/12 matched (target 14)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/function.rs` vs expected `values/types/function.rs`
- **Proposed provenance header:** `// port-lint: source values/types/function.rs` (current: `// port-lint: source src/values/types/function.rs`)
- **Lint issues:** 1

### 226. profile.heap

- **Target:** `profile.Heap [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 22410.0
- **Functions:** 12/13 matched (target 28)
- **Missing functions:** `r#gen`
- **Types:** 10/11 matched (target 12)
- **Missing types:** `Data`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/heap.rs` vs expected `eval/runtime/profile/heap.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/heap.rs` vs expected `eval/runtime/profile/heap.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/heap.rs` (current: `// port-lint: source src/eval/runtime/profile/heap.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/heap.rs` (current: `// port-lint: tests src/eval/runtime/profile/heap.rs`)
- **Lint issues:** 2

### 227. bc.stack_ptr

- **Target:** `bc.StackPtr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21910.0
- **Functions:** 10/11 matched (target 25)
- **Missing functions:** `add`
- **Types:** 7/8 matched (target 7)
- **Missing types:** `Output`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/stack_ptr.rs` vs expected `eval/bc/stack_ptr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/stack_ptr.rs` (current: `// port-lint: source src/eval/bc/stack_ptr.rs`)
- **Lint issues:** 1

### 228. avalues.static_

- **Target:** `avalues.Static [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21410.0
- **Functions:** 9/9 matched (target 11)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/static_.rs` vs expected `values/layout/avalues/static_.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/avalues/static_.rs` vs expected `values/layout/avalues/static_.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/static_.rs` (current: `// port-lint: source src/values/layout/avalues/static_.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/avalues/static_.rs` (current: `// port-lint: tests src/values/layout/avalues/static_.rs`)
- **Lint issues:** 2

### 229. avalues.array

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

### 230. compiler.args

- **Target:** `compiler.Args [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21310.0
- **Functions:** 10/11 matched
- **Missing functions:** `args`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Never`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/args.rs` vs expected `eval/compiler/args.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/args.rs` (current: `// port-lint: source src/eval/compiler/args.rs`)
- **Lint issues:** 1

### 231. profile.summary_by_function

- **Target:** `profile.SummaryByFunction [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21310.0
- **Functions:** 9/10 matched
- **Missing functions:** `drop_non_drop`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `RowKind`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/summary_by_function.rs` vs expected `values/layout/heap/profile/summary_by_function.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/summary_by_function.rs` (current: `// port-lint: source src/values/layout/heap/profile/summary_by_function.rs`)
- **Lint issues:** 1

### 232. avalues.tuple

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

### 233. avalues.complex

- **Target:** `avalues.Complex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21110.0
- **Functions:** 6/6 matched (target 14)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/complex.rs` vs expected `values/layout/avalues/complex.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/complex.rs` (current: `// port-lint: source src/values/layout/avalues/complex.rs`)
- **Lint issues:** 1

### 234. list.methods

- **Target:** `list.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21110.0
- **Functions:** 9/11 matched (target 16)
- **Missing functions:** `list_methods`, `recursive_list`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/methods.rs` vs expected `values/types/list/methods.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/methods.rs` vs expected `values/types/list/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/methods.rs` (current: `// port-lint: source src/values/types/list/methods.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/list/methods.rs` (current: `// port-lint: tests src/values/types/list/methods.rs`)
- **Lint issues:** 2

### 235. symbol.symbol

- **Target:** `symbol.Symbol [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21010.0
- **Functions:** 7/9 matched (target 11)
- **Missing functions:** `fmt`, `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/symbol/symbol.rs` vs expected `collections/symbol/symbol.rs`
- **Proposed provenance header:** `// port-lint: source collections/symbol/symbol.rs` (current: `// port-lint: source src/collections/symbol/symbol.rs`)
- **Lint issues:** 1

### 236. set.refs

- **Target:** `set.Refs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21010.0
- **Functions:** 5/5 matched (target 14)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 11)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/refs.rs` vs expected `values/types/set/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/refs.rs` (current: `// port-lint: source src/values/types/set/refs.rs`)
- **Lint issues:** 1

### 237. typing.iter

- **Target:** `typing.Iter [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21010.0
- **Functions:** 6/6 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 3)
- **Missing types:** `NonInstantiable`, `Canonical`
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/iter.rs` vs expected `values/typing/iter.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/iter.rs` vs expected `values/typing/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/iter.rs` (current: `// port-lint: source src/values/typing/iter.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/iter.rs` (current: `// port-lint: tests src/values/typing/iter.rs`)
- **Lint issues:** 2

### 238. eval.bc.compiler.stmt

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.Stmt [ZERO] [PROVENANCE-FALLBACK]`
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

### 239. bc.bytecode

- **Target:** `bc.Bytecode [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `handle`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/bytecode.rs` vs expected `eval/bc/bytecode.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/bytecode.rs` (current: `// port-lint: source src/eval/bc/bytecode.rs`)
- **Lint issues:** 1

### 240. structs.refs

- **Target:** `structs.Refs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 5/5 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/4 matched
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/refs.rs` vs expected `values/types/structs/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/refs.rs` (current: `// port-lint: source src/values/types/structs/refs.rs`)
- **Lint issues:** 1

### 241. bc.call

- **Target:** `bc.Call [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 3/4 matched (target 42)
- **Missing functions:** `fmt`
- **Types:** 4/5 matched (target 8)
- **Missing types:** `Args`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/call.rs` vs expected `eval/bc/call.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/call.rs` vs expected `eval/bc/call.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/bc/call.rs` vs expected `eval/bc/call.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/typing/tests/call.rs` vs expected `eval/bc/call.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/call.rs` (current: `// port-lint: source src/eval/bc/call.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/call.rs` (current: `// port-lint: tests tests/call.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/call.rs` (current: `// port-lint: tests tests/bc/call.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/call.rs` (current: `// port-lint: tests src/typing/tests/call.rs`)
- **Lint issues:** 4

### 242. profile.data

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

### 243. bc.instr_arg

- **Target:** `bc.InstrArg [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 42)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instr_arg.rs` vs expected `eval/bc/instr_arg.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr_arg.rs` (current: `// port-lint: source src/eval/bc/instr_arg.rs`)
- **Lint issues:** 1

### 244. typing.callable

- **Target:** `typing.Callable [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20910.0
- **Functions:** 6/7 matched (target 12)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `TyCallableInner`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callable.rs` vs expected `typing/callable.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/typing/tests/callable.rs` vs expected `typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable.rs` (current: `// port-lint: source src/typing/callable.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/callable.rs` (current: `// port-lint: tests src/typing/tests/callable.rs`)
- **Lint issues:** 2

### 245. heap.call_enter_exit

- **Target:** `heap.CallEnterExit [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20710.0
- **Functions:** 0/1 matched (target 4)
- **Missing functions:** `drop`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/call_enter_exit.rs` vs expected `values/layout/heap/call_enter_exit.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/call_enter_exit.rs` (current: `// port-lint: source src/values/layout/heap/call_enter_exit.rs`)
- **Lint issues:** 1

### 246. types.any

- **Target:** `types.Any [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20710.0
- **Functions:** 4/5 matched
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/any.rs` vs expected `values/types/any.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any.rs` (current: `// port-lint: source src/values/types/any.rs`)
- **Lint issues:** 1

### 247. list.globals

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
- **Lint issues:** 1

### 248. dict.traits

- **Target:** `dict.Traits [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20610.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 6)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/traits.rs` vs expected `values/types/dict/traits.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/traits.rs` (current: `// port-lint: source src/values/types/dict/traits.rs`)
- **Lint issues:** 1

### 249. bc.definitely_assigned

- **Target:** `bc.DefinitelyAssigned [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 2/4 matched (target 12)
- **Missing functions:** `new`, `assert_smaller_then`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/definitely_assigned.rs` vs expected `eval/bc/definitely_assigned.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/bc/definitely_assigned.rs` vs expected `eval/bc/definitely_assigned.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/definitely_assigned.rs` (current: `// port-lint: source src/eval/bc/definitely_assigned.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/definitely_assigned.rs` (current: `// port-lint: tests tests/bc/definitely_assigned.rs`)
- **Lint issues:** 2

### 250. funcs.min_max

- **Target:** `funcs.MinMax [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20510.0
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `max`, `min`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/min_max.rs` vs expected `stdlib/funcs/min_max.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/min_max.rs` (current: `// port-lint: source src/stdlib/funcs/min_max.rs`)
- **Lint issues:** 1

### 251. intern.interner

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

### 252. debug.evaluate

- **Target:** `debug.Evaluate [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20410.0
- **Functions:** 2/4 matched (target 3)
- **Missing functions:** `debugger`, `debug_evaluate`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/evaluate.rs` vs expected `debug/evaluate.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/debug/evaluate.rs` vs expected `debug/evaluate.rs`
- **Proposed provenance header:** `// port-lint: source debug/evaluate.rs` (current: `// port-lint: source src/debug/evaluate.rs`)
- **Proposed provenance header:** `// port-lint: tests debug/evaluate.rs` (current: `// port-lint: tests src/debug/evaluate.rs`)
- **Lint issues:** 2

### 253. stdlib.internal

- **Target:** `stdlib.Internal [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20410.0
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `ty_of_value_debug`, `test_ty_of_value_debug`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/internal.rs` vs expected `stdlib/internal.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/internal.rs` (current: `// port-lint: source src/stdlib/internal.rs`)
- **Lint issues:** 1

### 254. collections.maybe_uninit_backport

- **Target:** `collections.MaybeUninitBackport [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20410.0
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Guard`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/maybe_uninit_backport.rs` vs expected `collections/maybe_uninit_backport.rs`
- **Proposed provenance header:** `// port-lint: source collections/maybe_uninit_backport.rs` (current: `// port-lint: source src/collections/maybe_uninit_backport.rs`)
- **Lint issues:** 1

### 255. heap.maybe_uninit_slice_util

- **Target:** `heap.MaybeUninitSliceUtil [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20310.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `WriteRemOnDrop`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/maybe_uninit_slice_util.rs` vs expected `values/layout/heap/maybe_uninit_slice_util.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/maybe_uninit_slice_util.rs` (current: `// port-lint: source src/values/layout/heap/maybe_uninit_slice_util.rs`)
- **Lint issues:** 1

### 256. enumeration.ty_enum_type

- **Target:** `enumeration.TyEnumType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20310.0
- **Functions:** 0/2 matched (target 3)
- **Missing functions:** `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/ty_enum_type.rs` vs expected `values/types/enumeration/ty_enum_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/ty_enum_type.rs` (current: `// port-lint: source src/values/types/enumeration/ty_enum_type.rs`)
- **Lint issues:** 1

### 257. bool.type_repr

- **Target:** `bool.TypeRepr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/1 matched
- **Missing functions:** `starlark_type_repr`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/type_repr.rs` vs expected `values/types/bool/type_repr.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/type_repr.rs` (current: `// port-lint: source src/values/types/bool/type_repr.rs`)
- **Lint issues:** 1

### 258. bool.unpack

- **Target:** `bool.Unpack [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/1 matched
- **Missing functions:** `unpack_value_impl`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/unpack.rs` vs expected `values/types/bool/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/unpack.rs` (current: `// port-lint: source src/values/types/bool/unpack.rs`)
- **Lint issues:** 1

### 259. debug.adapter

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

### 260. docs

- **Target:** `docs.Docs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12310.0
- **Functions:** 12/13 matched (target 23)
- **Missing functions:** `default`
- **Types:** 10/10 matched (target 18)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs.rs` vs expected `docs.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/docs.rs` vs expected `docs.rs`
- **Proposed provenance header:** `// port-lint: source docs.rs` (current: `// port-lint: source src/docs.rs`)
- **Proposed provenance header:** `// port-lint: tests docs.rs` (current: `// port-lint: tests tests/derive/docs.rs`)
- **Lint issues:** 2

### 261. typing.basic

- **Target:** `typing.Basic [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12010.0
- **Functions:** 18/19 matched (target 38)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 12)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/basic.rs` vs expected `typing/basic.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/basic.rs` vs expected `typing/basic.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/freeze/basic.rs` vs expected `typing/basic.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/module/basic.rs` vs expected `typing/basic.rs`
- **Proposed provenance header:** `// port-lint: source typing/basic.rs` (current: `// port-lint: source src/typing/basic.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/basic.rs` (current: `// port-lint: tests tests/basic.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/basic.rs` (current: `// port-lint: tests tests/derive/freeze/basic.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/basic.rs` (current: `// port-lint: tests tests/derive/module/basic.rs`)
- **Lint issues:** 4

### 262. funcs.other

- **Target:** `funcs.Other [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11910.0
- **Functions:** 18/19 matched
- **Missing functions:** `r#type`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/other.rs` vs expected `stdlib/funcs/other.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/funcs/other.rs` vs expected `stdlib/funcs/other.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/other.rs` (current: `// port-lint: source src/stdlib/funcs/other.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/funcs/other.rs` (current: `// port-lint: tests src/stdlib/funcs/other.rs`)
- **Lint issues:** 2

### 263. namespace.value

- **Target:** `namespace.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11710.0
- **Functions:** 14/15 matched (target 17)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace/value.rs` vs expected `values/types/namespace/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/namespace/value.rs` vs expected `values/types/namespace/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace/value.rs` (current: `// port-lint: source src/values/types/namespace/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/namespace/value.rs` (current: `// port-lint: tests src/values/types/namespace/value.rs`)
- **Lint issues:** 2

### 264. docs.parse

- **Target:** `docs.Parse [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11610.0
- **Functions:** 14/15 matched (target 18)
- **Missing functions:** `arg`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/parse.rs` vs expected `docs/parse.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/docs/parse.rs` vs expected `docs/parse.rs`
- **Proposed provenance header:** `// port-lint: source docs/parse.rs` (current: `// port-lint: source src/docs/parse.rs`)
- **Proposed provenance header:** `// port-lint: tests docs/parse.rs` (current: `// port-lint: tests src/docs/parse.rs`)
- **Lint issues:** 2

### 265. record.instance

- **Target:** `record.Instance [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11410.0
- **Functions:** 12/13 matched (target 17)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/instance.rs` vs expected `values/types/record/instance.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/instance.rs` (current: `// port-lint: source src/values/types/record/instance.rs`)
- **Lint issues:** 1

### 266. compiler.def_inline

- **Target:** `compiler.DefInline [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11410.0
- **Functions:** 9/10 matched (target 17)
- **Missing functions:** `new`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/def_inline.rs` vs expected `eval/compiler/def_inline.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/opt/def_inline.rs` vs expected `eval/compiler/def_inline.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def_inline.rs` (current: `// port-lint: source src/eval/compiler/def_inline.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/compiler/def_inline.rs` (current: `// port-lint: tests tests/opt/def_inline.rs`)
- **Lint issues:** 2

### 267. profile.flamegraph

- **Target:** `profile.Flamegraph [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11310.0
- **Functions:** 9/10 matched (target 16)
- **Missing functions:** `new`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/flamegraph.rs` vs expected `eval/runtime/profile/flamegraph.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/flamegraph.rs` vs expected `eval/runtime/profile/flamegraph.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/flamegraph.rs` (current: `// port-lint: source src/eval/runtime/profile/flamegraph.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/flamegraph.rs` (current: `// port-lint: tests src/eval/runtime/profile/flamegraph.rs`)
- **Lint issues:** 2

### 268. type_compiled.factory

- **Target:** `type_compiled.Factory [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11110.0
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Result`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/factory.rs` vs expected `values/typing/type_compiled/factory.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/factory.rs` (current: `// port-lint: source src/values/typing/type_compiled/factory.rs`)
- **Lint issues:** 1

### 269. namespace.typing

- **Target:** `namespace.Typing [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11010.0
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace/typing.rs` vs expected `values/types/namespace/typing.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace/typing.rs` (current: `// port-lint: source src/values/types/namespace/typing.rs`)
- **Lint issues:** 1

### 270. params.parser

- **Target:** `params.Parser [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11010.0
- **Functions:** 8/9 matched (target 10)
- **Missing functions:** `test`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/parser.rs` vs expected `eval/runtime/params/parser.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/params/parser.rs` vs expected `eval/runtime/params/parser.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/parser.rs` (current: `// port-lint: source src/eval/runtime/params/parser.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/params/parser.rs` (current: `// port-lint: tests src/eval/runtime/params/parser.rs`)
- **Lint issues:** 2

### 271. bool.value

- **Target:** `bool.Value [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11010.0
- **Functions:** 8/9 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/value.rs` vs expected `values/types/bool/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/value.rs` (current: `// port-lint: source src/values/types/bool/value.rs`)
- **Lint issues:** 1

### 272. typing.never

- **Target:** `typing.Never [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10910.0
- **Functions:** 6/6 matched (target 9)
- **Missing functions:** _none_
- **Types:** 2/3 matched
- **Missing types:** `Canonical`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/never.rs` vs expected `values/typing/never.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/never.rs` vs expected `values/typing/never.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/never.rs` (current: `// port-lint: source src/values/typing/never.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/never.rs` (current: `// port-lint: tests src/values/typing/never.rs`)
- **Lint issues:** 2

### 273. values.recursive_repr_or_json_guard

- **Target:** `values.RecursiveReprOrJsonGuard [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10710.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `drop`
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/recursive_repr_or_json_guard.rs` vs expected `values/recursive_repr_or_json_guard.rs`
- **Proposed provenance header:** `// port-lint: source values/recursive_repr_or_json_guard.rs` (current: `// port-lint: source src/values/recursive_repr_or_json_guard.rs`)
- **Lint issues:** 1

### 274. tuple.alloc

- **Target:** `tuple.Alloc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10710.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/alloc.rs` vs expected `values/types/tuple/alloc.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/alloc.rs` vs expected `values/types/tuple/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/alloc.rs` (current: `// port-lint: source src/values/types/tuple/alloc.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/alloc.rs` (current: `// port-lint: tests src/values/types/tuple/alloc.rs`)
- **Lint issues:** 2

### 275. profile.by_type

- **Target:** `profile.ByType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10710.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `normalize_for_golden_tests`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/by_type.rs` vs expected `values/layout/heap/profile/by_type.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/by_type.rs` (current: `// port-lint: source src/values/layout/heap/profile/by_type.rs`)
- **Lint issues:** 1

### 276. values.type_repr

- **Target:** `values.TypeRepr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10610.0
- **Functions:** 3/3 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 8)
- **Missing types:** `Canonical`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/type_repr.rs` vs expected `values/type_repr.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/type_repr.rs` vs expected `values/type_repr.rs`
- **Proposed provenance header:** `// port-lint: source values/type_repr.rs` (current: `// port-lint: source src/values/type_repr.rs`)
- **Proposed provenance header:** `// port-lint: tests values/type_repr.rs` (current: `// port-lint: tests src/values/type_repr.rs`)
- **Lint issues:** 2

### 277. compiler.if_compiler

- **Target:** `compiler.IfCompiler [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10610.0
- **Functions:** 5/6 matched (target 5)
- **Missing functions:** `wr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/if_compiler.rs` vs expected `eval/bc/compiler/if_compiler.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/if_compiler.rs` (current: `// port-lint: source src/eval/bc/compiler/if_compiler.rs`)
- **Lint issues:** 1

### 278. set.set

- **Target:** `set.Set [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10510.0
- **Functions:** 4/5 matched (target 4)
- **Missing functions:** `set`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/set.rs` vs expected `values/types/set/set.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/set.rs` vs expected `values/types/set/set.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/set.rs` (current: `// port-lint: source src/values/types/set/set.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/set/set.rs` (current: `// port-lint: tests src/values/types/set/set.rs`)
- **Lint issues:** 2

### 279. structs.structs

- **Target:** `structs.Structs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10510.0
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `r#struct`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/structs.rs` vs expected `values/types/structs/structs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/structs.rs` (current: `// port-lint: source src/values/types/structs/structs.rs`)
- **Lint issues:** 1

### 280. eval.bc.compiler.call

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.Call [ZERO] [PROVENANCE-FALLBACK]`
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

### 281. types.unbound

- **Target:** `types.Unbound [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10510.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/unbound.rs` vs expected `values/types/unbound.rs`
- **Proposed provenance header:** `// port-lint: source values/types/unbound.rs` (current: `// port-lint: source src/values/types/unbound.rs`)
- **Lint issues:** 1

### 282. analysis.find_call_name

- **Target:** `analysis.FindCallName [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10410.0
- **Functions:** 2/3 matched (target 8)
- **Missing functions:** `finds_function_calls_with_name_kwarg`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/find_call_name.rs` vs expected `analysis/find_call_name.rs`
- **Proposed provenance header:** `// port-lint: source analysis/find_call_name.rs` (current: `// port-lint: source src/analysis/find_call_name.rs`)
- **Lint issues:** 1

### 283. dict.globals

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

### 284. compiler.assign_modify

- **Target:** `compiler.AssignModify [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10310.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `AssignOnWriteBc`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/assign_modify.rs` vs expected `eval/bc/compiler/assign_modify.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/assign_modify.rs` (current: `// port-lint: source src/eval/bc/compiler/assign_modify.rs`)
- **Lint issues:** 1

### 285. num.globals

- **Target:** `num.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `abs`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num/globals.rs` vs expected `values/types/num/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num/globals.rs` (current: `// port-lint: source src/values/types/num/globals.rs`)
- **Lint issues:** 1

### 286. float.globals

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

### 287. pagable.error

- **Target:** `pagable.Error [ZERO] [PROVENANCE-FALLBACK]`
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

### 288. tuple.globals

- **Target:** `tuple.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `tuple`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/globals.rs` vs expected `values/types/tuple/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/globals.rs` (current: `// port-lint: source src/values/types/tuple/globals.rs`)
- **Lint issues:** 1

### 289. int.globals

- **Target:** `int.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched
- **Missing functions:** `int`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/globals.rs` vs expected `values/types/int/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/globals.rs` (current: `// port-lint: source src/values/types/int/globals.rs`)
- **Lint issues:** 1

### 290. namespace.globals

- **Target:** `namespace.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `namespace`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/namespace/globals.rs` vs expected `values/types/namespace/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/namespace/globals.rs` (current: `// port-lint: source src/values/types/namespace/globals.rs`)
- **Lint issues:** 1

### 291. runtime.visit_span

- **Target:** `runtime.VisitSpan [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 19)
- **Missing functions:** `visit_spans`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/visit_span.rs` vs expected `eval/runtime/visit_span.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/visit_span.rs` (current: `// port-lint: source src/eval/runtime/visit_span.rs`)
- **Lint issues:** 1

### 292. bool.globals

- **Target:** `bool.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `bool`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/globals.rs` vs expected `values/types/bool/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/globals.rs` (current: `// port-lint: source src/values/types/bool/globals.rs`)
- **Lint issues:** 1

### 293. range.globals

- **Target:** `range.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `range`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/range/globals.rs` vs expected `values/types/range/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/range/globals.rs` (current: `// port-lint: source src/values/types/range/globals.rs`)
- **Lint issues:** 1

### 294. bc.writer

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

### 295. typing.fill_types_for_lint

- **Target:** `typing.FillTypesForLint [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 4210.0
- **Functions:** 39/39 matched (target 40)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/fill_types_for_lint.rs` vs expected `typing/fill_types_for_lint.rs`
- **Proposed provenance header:** `// port-lint: source typing/fill_types_for_lint.rs` (current: `// port-lint: source src/typing/fill_types_for_lint.rs`)
- **Lint issues:** 1

### 296. oracle.ctx

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

### 297. type_compiled.alloc

- **Target:** `type_compiled.Alloc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2910.0
- **Functions:** 28/28 matched (target 37)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/alloc.rs` vs expected `values/typing/type_compiled/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/alloc.rs` (current: `// port-lint: source src/values/typing/type_compiled/alloc.rs`)
- **Lint issues:** 1

### 298. type_compiled.matchers

- **Target:** `type_compiled.Matchers [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2610.0
- **Functions:** 3/3 matched (target 25)
- **Missing functions:** _none_
- **Types:** 23/23 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/type_compiled/matchers.rs` vs expected `values/typing/type_compiled/matchers.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/matchers.rs` (current: `// port-lint: source src/values/typing/type_compiled/matchers.rs`)
- **Lint issues:** 1

### 299. typing.ctx

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
- **Lint issues:** 1

### 300. docs.markdown

- **Target:** `docs.Markdown [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 2010.0
- **Functions:** 18/18 matched (target 37)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/markdown.rs` vs expected `docs/markdown.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/docs/tests/markdown.rs` vs expected `docs/markdown.rs`
- **Proposed provenance header:** `// port-lint: source docs/markdown.rs` (current: `// port-lint: source src/docs/markdown.rs`)
- **Proposed provenance header:** `// port-lint: tests docs/markdown.rs` (current: `// port-lint: tests src/docs/tests/markdown.rs`)
- **Lint issues:** 2

### 301. eval.bc.compiler.expr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.Expr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1510.0
- **Functions:** 15/15 matched (target 23)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/expr.rs` vs expected `eval/bc/compiler/expr.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/bc/expr.rs` vs expected `eval/bc/compiler/expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/expr.rs` (current: `// port-lint: source src/eval/bc/compiler/expr.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/compiler/expr.rs` (current: `// port-lint: tests tests/bc/expr.rs`)
- **Lint issues:** 2

### 302. environment.names

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

### 303. compiler.call

- **Target:** `compiler.Call [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/call.rs` vs expected `eval/compiler/call.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/call.rs` (current: `// port-lint: source src/eval/compiler/call.rs`)
- **Lint issues:** 1

### 304. typing.error

- **Target:** `typing.Error [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 9/9 matched (target 25)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/error.rs` vs expected `typing/error.rs`
- **Proposed provenance header:** `// port-lint: source typing/error.rs` (current: `// port-lint: source src/typing/error.rs`)
- **Lint issues:** 1

### 305. compiler.compr

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

### 306. docs.multipage

- **Target:** `docs.Multipage [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1110.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/multipage.rs` vs expected `docs/multipage.rs`
- **Proposed provenance header:** `// port-lint: source docs/multipage.rs` (current: `// port-lint: source src/docs/multipage.rs`)
- **Lint issues:** 1

### 307. environment.slots

- **Target:** `environment.Slots [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1110.0
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/slots.rs` vs expected `environment/slots.rs`
- **Proposed provenance header:** `// port-lint: source environment/slots.rs` (current: `// port-lint: source src/environment/slots.rs`)
- **Lint issues:** 1

### 308. compiler.types

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

### 309. __derive_refs.parse_args

- **Target:** `__derive_refs.ParseArgs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 810.0
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/parse_args.rs` vs expected `__derive_refs/parse_args.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/parse_args.rs` (current: `// port-lint: source src/__derive_refs/parse_args.rs`)
- **Lint issues:** 1

### 310. record.ty_record_type

- **Target:** `record.TyRecordType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 810.0
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/ty_record_type.rs` vs expected `values/types/record/ty_record_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/ty_record_type.rs` vs expected `values/types/record/ty_record_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/ty_record_type.rs` (current: `// port-lint: source src/values/types/record/ty_record_type.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/record/ty_record_type.rs` (current: `// port-lint: tests src/values/types/record/ty_record_type.rs`)
- **Lint issues:** 2

### 311. unused_loads.find

- **Target:** `unused_loads.Find [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unused_loads/find.rs` vs expected `analysis/unused_loads/find.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/find.rs` (current: `// port-lint: source src/analysis/unused_loads/find.rs`)
- **Lint issues:** 1

### 312. alloc.per_thread

- **Target:** `alloc.PerThread [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 6/6 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/per_thread.rs` vs expected `values/layout/heap/allocator/alloc/per_thread.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/per_thread.rs` vs expected `values/layout/heap/allocator/alloc/per_thread.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/per_thread.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/per_thread.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/per_thread.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/per_thread.rs`)
- **Lint issues:** 2

### 313. runtime.before_stmt

- **Target:** `runtime.BeforeStmt [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/before_stmt.rs` vs expected `eval/runtime/before_stmt.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/before_stmt.rs` vs expected `eval/runtime/before_stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/before_stmt.rs` (current: `// port-lint: source src/eval/runtime/before_stmt.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/before_stmt.rs` (current: `// port-lint: tests tests/before_stmt.rs`)
- **Lint issues:** 2

### 314. compiler.module

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

### 315. docs.code

- **Target:** `docs.Code [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 7/7 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/code.rs` vs expected `docs/code.rs`
- **Proposed provenance header:** `// port-lint: source docs/code.rs` (current: `// port-lint: source src/docs/code.rs`)
- **Lint issues:** 1

### 316. layout.value_not_special

- **Target:** `layout.ValueNotSpecial [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/value_not_special.rs` vs expected `values/layout/value_not_special.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_not_special.rs` (current: `// port-lint: source src/values/layout/value_not_special.rs`)
- **Lint issues:** 1

### 317. types.known_methods

- **Target:** `types.KnownMethods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 710.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/known_methods.rs` vs expected `values/types/known_methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/known_methods.rs` (current: `// port-lint: source src/values/types/known_methods.rs`)
- **Lint issues:** 1

### 318. assert.conformance

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

### 319. values.typing.ty

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.values.typing.Ty [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/ty.rs` vs expected `values/typing/ty.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/ty.rs` vs expected `values/typing/ty.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/ty.rs` (current: `// port-lint: source src/values/typing/ty.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/ty.rs` (current: `// port-lint: tests src/values/typing/ty.rs`)
- **Lint issues:** 2

### 320. layout.static_string

- **Target:** `layout.StaticString [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/static_string.rs` vs expected `values/layout/static_string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/static_string.rs` (current: `// port-lint: source src/values/layout/static_string.rs`)
- **Lint issues:** 1

### 321. values.index

- **Target:** `values.Index [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/index.rs` vs expected `values/index.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/index.rs` vs expected `values/index.rs`
- **Proposed provenance header:** `// port-lint: source values/index.rs` (current: `// port-lint: source src/values/index.rs`)
- **Proposed provenance header:** `// port-lint: tests values/index.rs` (current: `// port-lint: tests src/values/index.rs`)
- **Lint issues:** 2

### 322. string.globals

- **Target:** `string.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/globals.rs` vs expected `values/types/string/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/globals.rs` (current: `// port-lint: source src/values/types/string/globals.rs`)
- **Lint issues:** 1

### 323. compiler.expr_bool

- **Target:** `compiler.ExprBool [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/expr_bool.rs` vs expected `eval/compiler/expr_bool.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/expr_bool.rs` (current: `// port-lint: source src/eval/compiler/expr_bool.rs`)
- **Lint issues:** 1

### 324. unused_loads.remove

- **Target:** `unused_loads.Remove [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unused_loads/remove.rs` vs expected `analysis/unused_loads/remove.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/remove.rs` (current: `// port-lint: source src/analysis/unused_loads/remove.rs`)
- **Lint issues:** 1

### 325. values.comparison

- **Target:** `values.Comparison [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/comparison.rs` vs expected `values/comparison.rs`
- **Proposed provenance header:** `// port-lint: source values/comparison.rs` (current: `// port-lint: source src/values/comparison.rs`)
- **Lint issues:** 1

### 326. funcs.zip

- **Target:** `funcs.Zip [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/zip.rs` vs expected `stdlib/funcs/zip.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/zip.rs` (current: `// port-lint: source src/stdlib/funcs/zip.rs`)
- **Lint issues:** 1

### 327. typing.any

- **Target:** `typing.Any [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 4/4 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/any.rs` vs expected `values/typing/any.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/any.rs` vs expected `values/typing/any.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/any.rs` (current: `// port-lint: source src/values/typing/any.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/any.rs` (current: `// port-lint: tests src/values/typing/any.rs`)
- **Lint issues:** 2

### 328. num.typecheck

- **Target:** `num.Typecheck [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 510.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num/typecheck.rs` vs expected `values/types/num/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num/typecheck.rs` (current: `// port-lint: source src/values/types/num/typecheck.rs`)
- **Lint issues:** 1

### 329. runtime.slots

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

### 330. string.iter

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

### 331. __derive_refs.sig

- **Target:** `__derive_refs.Sig [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/sig.rs` vs expected `__derive_refs/sig.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/sig.rs` (current: `// port-lint: source src/__derive_refs/sig.rs`)
- **Lint issues:** 1

### 332. __derive_refs.components

- **Target:** `__derive_refs.Components [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/components.rs` vs expected `__derive_refs/components.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/components.rs` (current: `// port-lint: source src/__derive_refs/components.rs`)
- **Lint issues:** 1

### 333. compiler.type_expr

- **Target:** `compiler.TypeExpr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 2/2 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 17)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/type_expr.rs` vs expected `eval/compiler/type_expr.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_syntax/src/syntax/type_expr.rs` vs expected `eval/compiler/type_expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source src/eval/compiler/type_expr.rs`)
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source starlark_syntax/src/syntax/type_expr.rs`)
- **Lint issues:** 2

### 334. eval.soft_error

- **Target:** `eval.SoftError [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/soft_error.rs` vs expected `eval/soft_error.rs`
- **Proposed provenance header:** `// port-lint: source eval/soft_error.rs` (current: `// port-lint: source src/eval/soft_error.rs`)
- **Lint issues:** 1

### 335. callable.param

- **Target:** `callable.Param [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 1/1 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/callable/param.rs` vs expected `values/typing/callable/param.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/callable/param.rs` (current: `// port-lint: source src/values/typing/callable/param.rs`)
- **Lint issues:** 1

### 336. compiler.error

- **Target:** `compiler.Error [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 2/2 matched (target 24)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 14)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/error.rs` vs expected `eval/compiler/error.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_syntax/src/error.rs` vs expected `eval/compiler/error.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source src/eval/compiler/error.rs`)
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source starlark_syntax/src/error.rs`)
- **Lint issues:** 2

### 337. oracle.traits

- **Target:** `oracle.Traits [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/oracle/traits.rs` vs expected `typing/oracle/traits.rs`
- **Proposed provenance header:** `// port-lint: source typing/oracle/traits.rs` (current: `// port-lint: source src/typing/oracle/traits.rs`)
- **Lint issues:** 1

### 338. eval.bc.compiler.def

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.Def [ZERO] [PROVENANCE-FALLBACK]`
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

### 339. allocator.api

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

### 340. compiler.assign

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

### 341. layout.identity

- **Target:** `layout.Identity [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 1/1 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/identity.rs` vs expected `values/layout/identity.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/derive/freeze/identity.rs` vs expected `values/layout/identity.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/identity.rs` (current: `// port-lint: source src/values/layout/identity.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/identity.rs` (current: `// port-lint: tests tests/derive/freeze/identity.rs`)
- **Lint issues:** 2

### 342. bc.instr

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

### 343. eval.bc.compiler.compr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark_kotlin.eval.bc.compiler.Compr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/compr.rs` vs expected `eval/bc/compiler/compr.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/bc/compr.rs` vs expected `eval/bc/compiler/compr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/compr.rs` (current: `// port-lint: source src/eval/bc/compiler/compr.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/compiler/compr.rs` (current: `// port-lint: tests tests/bc/compr.rs`)
- **Lint issues:** 2

### 344. record.matcher

- **Target:** `record.Matcher [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/matcher.rs` vs expected `values/types/record/matcher.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/matcher.rs` (current: `// port-lint: source src/values/types/record/matcher.rs`)
- **Lint issues:** 1

### 345. typing.macro_refs

- **Target:** `typing.MacroRefs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/macro_refs.rs` vs expected `values/typing/macro_refs.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/macro_refs.rs` (current: `// port-lint: source src/values/typing/macro_refs.rs`)
- **Lint issues:** 1

### 346. bc.slow_arg

- **Target:** `bc.SlowArg [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/slow_arg.rs` vs expected `eval/bc/slow_arg.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/slow_arg.rs` (current: `// port-lint: source src/eval/bc/slow_arg.rs`)
- **Lint issues:** 1

### 347. __derive_refs.invoke_macro_error

- **Target:** `__derive_refs.InvokeMacroError [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs/invoke_macro_error.rs` vs expected `__derive_refs/invoke_macro_error.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs/invoke_macro_error.rs` (current: `// port-lint: source src/__derive_refs/invoke_macro_error.rs`)
- **Lint issues:** 1

### 348. enumeration.matcher

- **Target:** `enumeration.Matcher [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/matcher.rs` vs expected `values/types/enumeration/matcher.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/matcher.rs` (current: `// port-lint: source src/values/types/enumeration/matcher.rs`)
- **Lint issues:** 1

### 349. typing.macro_support

- **Target:** `typing.MacroSupport [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/macro_support.rs` vs expected `typing/macro_support.rs`
- **Proposed provenance header:** `// port-lint: source typing/macro_support.rs` (current: `// port-lint: source src/typing/macro_support.rs`)
- **Lint issues:** 1

### 350. bool.alloc

- **Target:** `bool.Alloc [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bool/alloc.rs` vs expected `values/types/bool/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bool/alloc.rs` (current: `// port-lint: source src/values/types/bool/alloc.rs`)
- **Lint issues:** 1

### 351. eval

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

### 352. bc.for_loop

- **Target:** `bc.ForLoop [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/for_loop.rs` vs expected `eval/bc/for_loop.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/for_loop.rs` vs expected `eval/bc/for_loop.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/for_loop.rs` (current: `// port-lint: source src/eval/bc/for_loop.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/for_loop.rs` (current: `// port-lint: tests tests/for_loop.rs`)
- **Lint issues:** 2

### 353. environment

- **Target:** `starlark_kotlin.Environment [ZERO] [PROVENANCE-FALLBACK]`
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

### 354. typing.call_args

- **Target:** `typing.CallArgs [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/call_args.rs` vs expected `typing/call_args.rs`
- **Proposed provenance header:** `// port-lint: source typing/call_args.rs` (current: `// port-lint: source src/typing/call_args.rs`)
- **Lint issues:** 1

### 355. typing.globals

- **Target:** `typing.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/globals.rs` vs expected `values/typing/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/globals.rs` (current: `// port-lint: source src/values/typing/globals.rs`)
- **Lint issues:** 1

### 356. none.globals

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

### 357. wasm

- **Target:** `starlark_kotlin.Wasm [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/wasm.rs` vs expected `wasm.rs`
- **Proposed provenance header:** `// port-lint: source wasm.rs` (current: `// port-lint: source src/wasm.rs`)
- **Lint issues:** 1

### 358. compiler.known

- **Target:** `compiler.Known [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/known.rs` vs expected `eval/compiler/known.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/known.rs` (current: `// port-lint: source src/eval/compiler/known.rs`)
- **Lint issues:** 1

### 359. eval.params

- **Target:** `eval.Params [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/params.rs` vs expected `eval/params.rs`
- **Proposed provenance header:** `// port-lint: source eval/params.rs` (current: `// port-lint: source src/eval/params.rs`)
- **Lint issues:** 1

### 360. profile.or_instrumentation

- **Target:** `profile.OrInstrumentation [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/or_instrumentation.rs` vs expected `eval/runtime/profile/or_instrumentation.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/or_instrumentation.rs` (current: `// port-lint: source src/eval/runtime/profile/or_instrumentation.rs`)
- **Lint issues:** 1

### 361. environment.module_dump

- **Target:** `environment.ModuleDump [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/module_dump.rs` vs expected `environment/module_dump.rs`
- **Proposed provenance header:** `// port-lint: source environment/module_dump.rs` (current: `// port-lint: source src/environment/module_dump.rs`)
- **Lint issues:** 1

### 362. funcs.globals

- **Target:** `funcs.Globals [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs/globals.rs` vs expected `stdlib/funcs/globals.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs/globals.rs` (current: `// port-lint: source src/stdlib/funcs/globals.rs`)
- **Lint issues:** 1

### 363. typing.mode

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

### 364. util

- **Target:** `starlark_kotlin.Util [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util.rs` vs expected `util.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/util.rs` vs expected `util.rs`
- **Proposed provenance header:** `// port-lint: source util.rs` (current: `// port-lint: source src/util.rs`)
- **Proposed provenance header:** `// port-lint: tests util.rs` (current: `// port-lint: tests tests/util.rs`)
- **Lint issues:** 2

### 365. macros

- **Target:** `starlark_kotlin.Macros [ZERO] [PROVENANCE-FALLBACK]`
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

### 366. allocator.alloc

- **Target:** `allocator.Alloc [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc.rs` vs expected `values/layout/heap/allocator/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc.rs`)
- **Lint issues:** 1

### 367. heap.profile

- **Target:** `profile.Profile [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile.rs` vs expected `values/layout/heap/profile.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile.rs` (current: `// port-lint: source src/values/layout/heap/profile.rs`)
- **Lint issues:** 1

### 368. bc.compiler

- **Target:** `bc.Compiler [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler.rs` vs expected `eval/bc/compiler.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler.rs` (current: `// port-lint: source src/eval/bc/compiler.rs`)
- **Lint issues:** 1

### 369. stdlib.funcs

- **Target:** `stdlib.Funcs [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/funcs.rs` vs expected `stdlib/funcs.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/funcs.rs` (current: `// port-lint: source src/stdlib/funcs.rs`)
- **Lint issues:** 1

### 370. layout.avalues

- **Target:** `layout.AValues [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues.rs` vs expected `values/layout/avalues.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues.rs` (current: `// port-lint: source src/values/layout/avalues.rs`)
- **Lint issues:** 1

### 371. string.intern

- **Target:** `string.Intern [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/intern.rs` vs expected `values/types/string/intern.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/intern.rs` (current: `// port-lint: source src/values/types/string/intern.rs`)
- **Lint issues:** 1

### 372. eval.runtime

- **Target:** `eval.Runtime [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime.rs` vs expected `eval/runtime.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/runtime.rs` vs expected `eval/runtime.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:tests/runtime.rs` vs expected `eval/runtime.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime.rs` (current: `// port-lint: source src/eval/runtime.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime.rs` (current: `// port-lint: tests tests/runtime.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime.rs` (current: `// port-lint: tests tests/runtime.rs`)
- **Lint issues:** 3

### 373. syntax

- **Target:** `starlark_kotlin.Syntax [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/syntax.rs` vs expected `syntax.rs`
- **Proposed provenance header:** `// port-lint: source syntax.rs` (current: `// port-lint: source src/syntax.rs`)
- **Lint issues:** 1

### 374. __derive_refs

- **Target:** `__derive_refs.DeriveRefs [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/__derive_refs.rs` vs expected `__derive_refs.rs`
- **Proposed provenance header:** `// port-lint: source __derive_refs.rs` (current: `// port-lint: source src/__derive_refs.rs`)
- **Lint issues:** 1

### 375. pagable.vtable_register

- **Target:** `pagable.VtableRegister [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pagable/vtable_register.rs` vs expected `pagable/vtable_register.rs`
- **Proposed provenance header:** `// port-lint: source pagable/vtable_register.rs` (current: `// port-lint: source src/pagable/vtable_register.rs`)
- **Lint issues:** 1

### 376. types.structs

- **Target:** `types.Structs [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs.rs` vs expected `values/types/structs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs.rs` (current: `// port-lint: source src/values/types/structs.rs`)
- **Lint issues:** 1

### 377. analysis.unused_loads

- **Target:** `analysis.UnusedLoads [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/unused_loads.rs` vs expected `analysis/unused_loads.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads.rs` (current: `// port-lint: source src/analysis/unused_loads.rs`)
- **Lint issues:** 1

### 378. runtime.profile

- **Target:** `runtime.Profile [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile.rs` vs expected `eval/runtime/profile.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile.rs` (current: `// port-lint: source src/eval/runtime/profile.rs`)
- **Lint issues:** 1

### 379. values.types

- **Target:** `values.Types [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types.rs` vs expected `values/types.rs`
- **Proposed provenance header:** `// port-lint: source values/types.rs` (current: `// port-lint: source src/values/types.rs`)
- **Lint issues:** 1

### 380. runtime.params

- **Target:** `runtime.Params [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params.rs` vs expected `eval/runtime/params.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params.rs` (current: `// port-lint: source src/eval/runtime/params.rs`)
- **Lint issues:** 1

### 381. heap.allocator

- **Target:** `heap.Allocator [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator.rs` vs expected `values/layout/heap/allocator.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator.rs` (current: `// port-lint: source src/values/layout/heap/allocator.rs`)
- **Lint issues:** 1

### 382. types.tuple

- **Target:** `types.Tuple [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple.rs` vs expected `values/types/tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple.rs` (current: `// port-lint: source src/values/types/tuple.rs`)
- **Lint issues:** 1

### 383. pagable

- **Target:** `starlark_kotlin.Pagable [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/pagable.rs` vs expected `pagable.rs`
- **Proposed provenance header:** `// port-lint: source pagable.rs` (current: `// port-lint: source src/pagable.rs`)
- **Lint issues:** 1

### 384. errors

- **Target:** `starlark_kotlin.Errors [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/errors.rs` vs expected `errors.rs`
- **Proposed provenance header:** `// port-lint: source errors.rs` (current: `// port-lint: source src/errors.rs`)
- **Lint issues:** 1

### 385. values

- **Target:** `values.Values [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values.rs` vs expected `values.rs`
- **Proposed provenance header:** `// port-lint: source values.rs` (current: `// port-lint: source src/values.rs`)
- **Lint issues:** 1

### 386. typing.oracle

- **Target:** `typing.Oracle [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/oracle.rs` vs expected `typing/oracle.rs`
- **Proposed provenance header:** `// port-lint: source typing/oracle.rs` (current: `// port-lint: source src/typing/oracle.rs`)
- **Lint issues:** 1

### 387. values.typing

- **Target:** `values.Typing [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing.rs` vs expected `values/typing.rs`
- **Proposed provenance header:** `// port-lint: source values/typing.rs` (current: `// port-lint: source src/values/typing.rs`)
- **Lint issues:** 1

### 388. lib

- **Target:** `starlark_kotlin.Lib [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Lint issues:** 1

### 389. collections

- **Target:** `collections.Collections [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections.rs` vs expected `collections.rs`
- **Proposed provenance header:** `// port-lint: source collections.rs` (current: `// port-lint: source src/collections.rs`)
- **Lint issues:** 1

### 390. heap.branding

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
./ast_distance --init-tasks ../../tmp/starlark/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
