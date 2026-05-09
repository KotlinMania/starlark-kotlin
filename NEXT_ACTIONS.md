# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 422/468 (90.2%)
- **Function parity:** 3906/4568 matched (target 6034) — 85.5%
- **Class/type parity:** 844/1209 matched (target 1637) — 69.8%
- **Combined symbol parity:** 4750/5777 matched (target 7671) — 82.2%
- **Average inline-code cosine:** 0.62 (function body across 409 matched files)
- **Average documentation cosine:** 0.68 (doc text across 409 matched files)
- **Cheat-zeroed Files:** 31
- **Critical Issues:** 167 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.81 (needs 4% improvement)
- **Dependencies:** 178
- **Priority Score:** 178052704.0
- **Functions:** 116/118 matched (target 174)
- **Missing functions:** `testing_new_int`, `_test_send_sync`
- **Types:** 7/9 matched (target 11)
- **Missing types:** `Canonical`, `String`
- **Symbol Deficit:** 4 (functions: 2, types: 2)
- **Missing Tests:** 1 of 9 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 2. typing.ty
- **Similarity:** 0.80 (needs 5% improvement)
- **Dependencies:** 109
- **Priority Score:** 109005400.0
- **Functions:** 50/50 matched (target 61)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 3. typing.starlark_value
- **Similarity:** 0.75 (needs 10% improvement)
- **Dependencies:** 76
- **Priority Score:** 76003800.0
- **Functions:** 34/34 matched (target 47)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 4. params.display
- **Similarity:** 0.74 (needs 11% improvement)
- **Dependencies:** 76
- **Priority Score:** 76000704.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Action:** Review and complete missing sections

### 5. runtime.evaluator
- **Similarity:** 0.83 (needs 2% improvement)
- **Dependencies:** 55
- **Priority Score:** 55006700.0
- **Functions:** 60/60 matched (target 64)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 6. values.trace
- **Similarity:** 0.30 (needs 55% improvement)
- **Dependencies:** 52
- **Priority Score:** 52000208.0
- **Functions:** 1/1 matched (target 14)
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
- **Similarity:** 0.52 (needs 33% improvement)
- **Dependencies:** 42
- **Priority Score:** 42010304.0
- **Functions:** 1/1 matched (target 19)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Frozen`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Deep review - likely missing major functionality

### 9. coerce
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

### 10. values.frozen_ref
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 27
- **Priority Score:** 27052104.0
- **Functions:** 14/17 matched (target 19)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Target`, `Frozen`
- **Symbol Deficit:** 5 (functions: 3, types: 2)
- **Action:** Review and complete missing sections

### 11. none.none_type
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 27
- **Priority Score:** 27011302.0
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Minor refinements needed

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
- **Similarity:** 0.64 (needs 21% improvement)
- **Dependencies:** 25
- **Priority Score:** 25023804.0
- **Functions:** 28/30 matched (target 51)
- **Missing functions:** `from`, `f`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 1 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 14. util.arc_str
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 21
- **Priority Score:** 21010704.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Review and complete missing sections

### 15. environment.globals
- **Similarity:** 0.70 (needs 15% improvement)
- **Dependencies:** 20
- **Priority Score:** 20064004.0
- **Functions:** 30/35 matched (target 38)
- **Missing functions:** `empty`, `get`, `test_send_sync`, `register_foo`, `foo`
- **Types:** 4/5 matched
- **Missing types:** `GlobalValue`
- **Symbol Deficit:** 6 (functions: 5, types: 1)
- **Missing Tests:** 4 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 16. values.value_of_unchecked
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20062510.0
- **Functions:** 15/18 matched (target 28)
- **Missing functions:** `fmt`, `assert_send_sync`, `_assert_covariant`
- **Types:** 4/7 matched (target 8)
- **Missing types:** `Canonical`, `Frozen`, `Error`
- **Symbol Deficit:** 6 (functions: 3, types: 3)
- **Missing Tests:** 2 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 17. __derive_refs.param_spec
- **Similarity:** 0.83 (needs 2% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 18. util.refcell
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000202.0
- **Functions:** 2/2 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 19. derive.module
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000010.0
- **Functions:** 0/0 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 20. values.iter
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 17
- **Priority Score:** 17020704.0
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Item`
- **Symbol Deficit:** 2 (functions: 1, types: 1)
- **Action:** Review and complete missing sections

### 21. environment.methods
- **Similarity:** 0.77 (needs 8% improvement)
- **Dependencies:** 17
- **Priority Score:** 17012302.0
- **Functions:** 18/19 matched (target 23)
- **Missing functions:** `get_methods`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 22. values.error
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 23. layout.avalue
- **Similarity:** 0.75 (needs 10% improvement)
- **Dependencies:** 14
- **Priority Score:** 14021103.0
- **Functions:** 6/8 matched (target 12)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 24. typing.tuple
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 12
- **Priority Score:** 12010704.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 25. layout.const_frozen_string
- **Similarity:** 0.79 (needs 6% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000202.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 26. int.inline_int
- **Similarity:** 0.44 (needs 41% improvement)
- **Dependencies:** 11
- **Priority Score:** 11113906.0
- **Functions:** 26/34 matched (target 40)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Symbol Deficit:** 11 (functions: 8, types: 3)
- **Missing Tests:** 1 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **layout.heap** (109 deps)
   - Path: `values/layout/heap.rs`
   - Essential for 109 other files

2. **typing.type_compiled** (22 deps)
   - Path: `values/typing/type_compiled.rs`
   - Essential for 22 other files

3. **collections.symbol** (15 deps)
   - Path: `collections/symbol.rs`
   - Essential for 15 other files

4. **types.dict** (12 deps)
   - Path: `values/types/dict.rs`
   - Essential for 12 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. layout.value

- **Target:** `values.ToValue`
- **Similarity:** 0.81
- **Dependents:** 178
- **Priority Score:** 178052704.0
- **Functions:** 116/118 matched (target 174)
- **Missing functions:** `testing_new_int`, `_test_send_sync`
- **Types:** 7/9 matched (target 11)
- **Missing types:** `Canonical`, `String`
- **Tests:** 8/9 matched

### 2. typing.ty

- **Target:** `typing.Ty`
- **Similarity:** 0.80
- **Dependents:** 109
- **Priority Score:** 109005400.0
- **Functions:** 50/50 matched (target 61)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_

### 3. assert

- **Target:** `assert.AssertModule [STUB]`
- **Similarity:** 1.00
- **Dependents:** 84
- **Priority Score:** 84000000.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 4. typing.starlark_value

- **Target:** `typing.StarlarkValue`
- **Similarity:** 0.75
- **Dependents:** 76
- **Priority Score:** 76003800.0
- **Functions:** 34/34 matched (target 47)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Lint issues:** 2

### 5. params.display

- **Target:** `params.Display`
- **Similarity:** 0.74
- **Dependents:** 76
- **Priority Score:** 76000704.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_

### 6. runtime.evaluator

- **Target:** `runtime.Evaluator`
- **Similarity:** 0.83
- **Dependents:** 55
- **Priority Score:** 55006700.0
- **Functions:** 60/60 matched (target 64)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_

### 7. debug

- **Target:** `debug.Debug [STUB]`
- **Similarity:** 1.00
- **Dependents:** 53
- **Priority Score:** 53000000.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 8. values.trace

- **Target:** `values.Trace`
- **Similarity:** 0.30
- **Dependents:** 52
- **Priority Score:** 52000208.0
- **Functions:** 1/1 matched (target 14)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 9. derive.unpack_value

- **Target:** `derive.UnpackValue`
- **Similarity:** 0.71
- **Dependents:** 51
- **Priority Score:** 51000704.0
- **Functions:** 2/2 matched (target 12)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 10. values.freeze

- **Target:** `values.Freeze`
- **Similarity:** 0.52
- **Dependents:** 42
- **Priority Score:** 42010304.0
- **Functions:** 1/1 matched (target 19)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Frozen`
- **Lint issues:** 1

### 11. values.alloc_value

- **Target:** `values.AllocValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 42
- **Priority Score:** 42000600.0
- **Functions:** 2/2 matched (target 5)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 10)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `tests:src/tests/derive/allocValue.rs` vs expected `values/alloc_value.rs`
- **Proposed provenance header:** `// port-lint: tests values/alloc_value.rs` (current: `// port-lint: tests src/tests/derive/allocValue.rs`)
- **Lint issues:** 1

### 12. layout.freezer

- **Target:** `layout.Freezer`
- **Similarity:** 0.85
- **Dependents:** 36
- **Priority Score:** 36000600.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 13. coerce

- **Target:** `starlark.Coerce`
- **Similarity:** 0.63
- **Dependents:** 34
- **Priority Score:** 34031404.0
- **Functions:** 4/5 matched
- **Missing functions:** `f`
- **Types:** 7/9 matched (target 10)
- **Missing types:** `Trait`, `Assoc`
- **Tests:** 3/4 matched

### 14. compiler.span

- **Target:** `compiler.Span`
- **Similarity:** 0.92
- **Dependents:** 29
- **Priority Score:** 29010400.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`

### 15. values.frozen_ref

- **Target:** `values.FrozenRef`
- **Similarity:** 0.61
- **Dependents:** 27
- **Priority Score:** 27052104.0
- **Functions:** 14/17 matched (target 19)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Target`, `Frozen`
- **Lint issues:** 2

### 16. none.none_type

- **Target:** `none.NoneType`
- **Similarity:** 0.82
- **Dependents:** 27
- **Priority Score:** 27011302.0
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Lint issues:** 1

### 17. runtime.frame_span

- **Target:** `runtime.FrameSpan`
- **Similarity:** 0.65
- **Dependents:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 18. runtime.arguments

- **Target:** `runtime.Arguments [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 25
- **Priority Score:** 25023804.0
- **Functions:** 28/30 matched (target 51)
- **Missing functions:** `from`, `f`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/arguments.rs` vs expected `eval/runtime/arguments.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/arguments.rs` (current: `// port-lint: tests src/eval/runtime/arguments.rs`)
- **Lint issues:** 1

### 19. util.arc_str

- **Target:** `util.ArcStr`
- **Similarity:** 0.60
- **Dependents:** 21
- **Priority Score:** 21010704.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`

### 20. environment.globals

- **Target:** `environment.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 20
- **Priority Score:** 20064004.0
- **Functions:** 30/35 matched (target 38)
- **Missing functions:** `empty`, `get`, `test_send_sync`, `register_foo`, `foo`
- **Types:** 4/5 matched
- **Missing types:** `GlobalValue`
- **Tests:** 1/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/environment/globals.rs` vs expected `environment/globals.rs`
- **Proposed provenance header:** `// port-lint: tests environment/globals.rs` (current: `// port-lint: tests src/environment/globals.rs`)
- **Lint issues:** 1

### 21. values.value_of_unchecked

- **Target:** `values.ValueOfUnchecked [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20062510.0
- **Functions:** 15/18 matched (target 28)
- **Missing functions:** `fmt`, `assert_send_sync`, `_assert_covariant`
- **Types:** 4/7 matched (target 8)
- **Missing types:** `Canonical`, `Frozen`, `Error`
- **Tests:** 3/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/valueOfUnchecked.rs` vs expected `values/value_of_unchecked.rs`
- **Proposed provenance header:** `// port-lint: tests values/value_of_unchecked.rs` (current: `// port-lint: tests src/values/valueOfUnchecked.rs`)
- **Lint issues:** 3

### 22. __derive_refs.param_spec

- **Target:** `deriverefs.ParamSpec`
- **Similarity:** 0.83
- **Dependents:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 23. util.refcell

- **Target:** `util.RefCellCompat [PROVENANCE-FALLBACK]`
- **Similarity:** 0.82
- **Dependents:** 20
- **Priority Score:** 20000202.0
- **Functions:** 2/2 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/util/refcell.rs` vs expected `util/refcell.rs`
- **Proposed provenance header:** `// port-lint: tests util/refcell.rs` (current: `// port-lint: tests src/util/refcell.rs`)
- **Lint issues:** 1

### 24. derive.module

- **Target:** `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20000010.0
- **Functions:** 0/0 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlark_syntax/src/syntax/module.rs` vs expected `tests/derive/module.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module.rs` (current: `// port-lint: source ../starlark_syntax/src/syntax/module.rs`)
- **Lint issues:** 1

### 25. values.iter

- **Target:** `values.Iter`
- **Similarity:** 0.61
- **Dependents:** 17
- **Priority Score:** 17020704.0
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Item`

### 26. environment.methods

- **Target:** `environment.Methods [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 17
- **Priority Score:** 17012302.0
- **Functions:** 18/19 matched (target 23)
- **Missing functions:** `get_methods`
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Tests:** 1/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/environment/methods.rs` vs expected `environment/methods.rs`
- **Proposed provenance header:** `// port-lint: tests environment/methods.rs` (current: `// port-lint: tests src/environment/methods.rs`)
- **Lint issues:** 2

### 27. values.error

- **Target:** `values.Error`
- **Similarity:** 0.62
- **Dependents:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_

### 28. private

- **Target:** `starlark.Private`
- **Similarity:** 1.00
- **Dependents:** 15
- **Priority Score:** 15000100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 29. layout.avalue

- **Target:** `layout.AValue`
- **Similarity:** 0.75
- **Dependents:** 14
- **Priority Score:** 14021103.0
- **Functions:** 6/8 matched (target 12)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 30. typing.tuple

- **Target:** `typing.Tuple`
- **Similarity:** 0.60
- **Dependents:** 12
- **Priority Score:** 12010704.0
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 31. layout.const_frozen_string

- **Target:** `layout.ConstFrozenString`
- **Similarity:** 0.79
- **Dependents:** 12
- **Priority Score:** 12000202.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 32. layout.value_lifetimeless

- **Target:** `layout.ValueLifetimeless`
- **Similarity:** 1.00
- **Dependents:** 12
- **Priority Score:** 12000100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 33. int.inline_int

- **Target:** `int.InlineInt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 11
- **Priority Score:** 11113906.0
- **Functions:** 26/34 matched (target 40)
- **Missing functions:** `fmt`, `testing_new`, `try_from_impl`, `bitand`, `bitor`, `bitxor`, `eq`, `partial_cmp`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Error`, `Output`, `Canonical`
- **Tests:** 1/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/int/inlineInt.rs` vs expected `values/types/int/inline_int.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/int/inline_int.rs` (current: `// port-lint: tests src/values/types/int/inlineInt.rs`)
- **Lint issues:** 1

### 34. int.pointer_i32

- **Target:** `int.PointerI32`
- **Similarity:** 0.66
- **Dependents:** 9
- **Priority Score:** 9043303.0
- **Functions:** 28/31 matched (target 33)
- **Missing functions:** `eq`, `fmt`, `serialize`
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Lint issues:** 2

### 35. types.type_instance_id

- **Target:** `types.TypeInstanceId`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9010210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `r#gen`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 36. any

- **Target:** `starlark.Any [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8062710.0
- **Functions:** 8/12 matched (target 15)
- **Missing functions:** `is`, `convert_value`, `convert_any`, `test`
- **Types:** 13/15 matched
- **Missing types:** `StaticType`, `My`
- **Tests:** 4/7 matched

### 37. layout.aligned_size

- **Target:** `layout.AlignedSize`
- **Similarity:** 0.46
- **Dependents:** 8
- **Priority Score:** 8061505.5
- **Functions:** 8/13 matched (target 17)
- **Missing functions:** `layout`, `ptr_diff`, `add`, `sub`, `mul`
- **Types:** 1/2 matched
- **Missing types:** `Output`
- **Tests:** 2/2 matched

### 38. eval.compiler

- **Target:** `eval.Compiler`
- **Similarity:** 0.81
- **Dependents:** 8
- **Priority Score:** 8000702.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 39. types.bigint

- **Target:** `types.Bigint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 7
- **Priority Score:** 7027401.0
- **Functions:** 71/73 matched (target 76)
- **Missing functions:** `unpack_integer`, `eq`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 42/42 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/bigint.rs` vs expected `values/types/bigint.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/bigint.rs` (current: `// port-lint: tests src/values/types/bigint.rs`)
- **Lint issues:** 1

### 40. values.starlark_type_id

- **Target:** `values.StarlarkTypeId`
- **Similarity:** 0.61
- **Dependents:** 7
- **Priority Score:** 7010804.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `eq`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 41. runtime.frozen_file_span

- **Target:** `runtime.FrozenFileSpan`
- **Similarity:** 0.86
- **Dependents:** 7
- **Priority Score:** 7001101.5
- **Functions:** 10/10 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 42. compiler.opt_ctx

- **Target:** `compiler.OptCtx`
- **Similarity:** 0.71
- **Dependents:** 7
- **Priority Score:** 7000703.0
- **Functions:** 5/5 matched (target 13)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 4)
- **Missing types:** _none_

### 43. bc.expr

- **Target:** `bc.Expr`
- **Similarity:** 0.98
- **Dependents:** 7
- **Priority Score:** 7000700.0
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 7/7 matched

### 44. type_compiled.type_matcher_factory

- **Target:** `typecompiled.TypeMatcherFactory`
- **Similarity:** 0.69
- **Dependents:** 7
- **Priority Score:** 7000603.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 45. runtime.small_duration

- **Target:** `runtime.SmallDuration`
- **Similarity:** 0.38
- **Dependents:** 6
- **Priority Score:** 6040906.0
- **Functions:** 4/7 matched (target 11)
- **Missing functions:** `from_millis`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched

### 46. typing.typecheck

- **Target:** `typing.Typecheck`
- **Similarity:** 0.52
- **Dependents:** 6
- **Priority Score:** 6030705.0
- **Functions:** 2/5 matched
- **Missing functions:** `fmt`, `find_bindings_by_name`, `find_first_binding`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 47. none.none_or

- **Target:** `none.NoneOr`
- **Similarity:** 0.73
- **Dependents:** 6
- **Priority Score:** 6021002.5
- **Functions:** 7/7 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 4)
- **Missing types:** `Canonical`, `Error`

### 48. values.freeze_error

- **Target:** `values.FreezeError`
- **Similarity:** 0.42
- **Dependents:** 6
- **Priority Score:** 6020806.0
- **Functions:** 3/4 matched (target 6)
- **Missing functions:** `from`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `FreezeResult`

### 49. dict.dict_type

- **Target:** `dict.DictType`
- **Similarity:** 0.66
- **Dependents:** 6
- **Priority Score:** 6020503.5
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 50. layout.value_alloc_size

- **Target:** `layout.ValueAllocSize`
- **Similarity:** 0.43
- **Dependents:** 6
- **Priority Score:** 6010605.5
- **Functions:** 4/5 matched
- **Missing functions:** `layout`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 51. compiler.stmt

- **Target:** `compiler.Stmt`
- **Similarity:** 0.79
- **Dependents:** 6
- **Priority Score:** 6003202.0
- **Functions:** 25/25 matched (target 28)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 24)
- **Missing types:** _none_

### 52. profile.profiler_type

- **Target:** `profile.ProfilerType`
- **Similarity:** 0.69
- **Dependents:** 6
- **Priority Score:** 6000303.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 53. types.array

- **Target:** `types.Array [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 5
- **Priority Score:** 5073404.5
- **Functions:** 25/32 matched (target 27)
- **Missing functions:** `fmt`, `offset_of_content`, `ptr_at`, `mut_ptr_at`, `get_unchecked`, `is_special`, `serialize`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/array.rs` vs expected `values/types/array.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/array.rs` (current: `// port-lint: tests src/values/types/array.rs`)
- **Lint issues:** 1

### 54. typing.arc_ty

- **Target:** `typing.ArcTy`
- **Similarity:** 0.57
- **Dependents:** 5
- **Priority Score:** 5021104.5
- **Functions:** 6/7 matched (target 15)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 10)
- **Missing types:** `Target`

### 55. tests.def

- **Target:** `tests.Def`
- **Similarity:** 0.95
- **Dependents:** 5
- **Priority Score:** 5001400.5
- **Functions:** 14/14 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 14/14 matched

### 56. typing.interface

- **Target:** `typing.Interface`
- **Similarity:** 0.60
- **Dependents:** 5
- **Priority Score:** 5000404.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 57. scope.scope_resolver_globals

- **Target:** `scope.ScopeResolverGlobals`
- **Similarity:** 0.72
- **Dependents:** 5
- **Priority Score:** 5000403.0
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 58. enumeration.enum_type

- **Target:** `enumeration.EnumType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 4
- **Priority Score:** 4094403.5
- **Functions:** 33/36 matched (target 39)
- **Missing functions:** `fmt`, `r#type`, `values`
- **Types:** 2/8 matched (target 5)
- **Missing types:** `EnumCell`, `TyEnumDataOpt`, `Frozen`, `EnumType`, `FrozenEnumType`, `Canonical`
- **Tests:** 12/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/enumeration/enumType.rs` vs expected `values/types/enumeration/enum_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/enumeration/enum_type.rs` (current: `// port-lint: tests src/values/types/enumeration/enumType.rs`)
- **Lint issues:** 1

### 59. bc.frame

- **Target:** `bc.Frame`
- **Similarity:** 0.58
- **Dependents:** 4
- **Priority Score:** 4082604.2
- **Functions:** 16/24 matched (target 31)
- **Missing functions:** `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 60. types.starlark_value_as_type

- **Target:** `types.StarlarkValueAsType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 4
- **Priority Score:** 4051704.8
- **Functions:** 9/13 matched (target 15)
- **Missing functions:** `fmt`, `new`, `compiler_args_globals`, `compiler_args`
- **Types:** 3/4 matched
- **Missing types:** `Canonical`
- **Tests:** 3/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/starlark_value_as_type.rs` vs expected `values/types/starlark_value_as_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/starlark_value_as_type.rs` (current: `// port-lint: tests src/values/types/starlark_value_as_type.rs`)
- **Lint issues:** 1

### 61. values.value_of

- **Target:** `values.ValueOf`
- **Similarity:** 0.54
- **Dependents:** 4
- **Priority Score:** 4051004.8
- **Functions:** 4/6 matched (target 7)
- **Missing functions:** `deref`, `fmt`
- **Types:** 1/4 matched (target 2)
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

### 63. values.demand

- **Target:** `values.Demand [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 4
- **Priority Score:** 4031102.0
- **Functions:** 5/7 matched (target 8)
- **Missing functions:** `payload`, `provide`
- **Types:** 3/4 matched
- **Missing types:** `StaticType`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/demand.rs` vs expected `values/demand.rs`
- **Proposed provenance header:** `// port-lint: tests values/demand.rs` (current: `// port-lint: tests src/values/demand.rs`)
- **Lint issues:** 1

### 64. bc.native_function

- **Target:** `bc.NativeFunction`
- **Similarity:** 0.51
- **Dependents:** 4
- **Priority Score:** 4010505.0
- **Functions:** 3/4 matched
- **Missing functions:** `fun`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 65. types.ellipsis

- **Target:** `types.Ellipsis [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 4
- **Priority Score:** 4000402.0
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/ellipsis.rs` vs expected `values/types/ellipsis.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/ellipsis.rs` (current: `// port-lint: tests src/values/types/ellipsis.rs`)
- **Lint issues:** 1

### 66. record.record_type

- **Target:** `record.RecordType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 3
- **Priority Score:** 3083004.2
- **Functions:** 20/22 matched
- **Missing functions:** `fmt`, `r#type`
- **Types:** 2/8 matched (target 3)
- **Missing types:** `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/record_type.rs` vs expected `values/types/record/record_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/record/record_type.rs` (current: `// port-lint: tests src/values/types/record/record_type.rs`)
- **Lint issues:** 1

### 67. alloc.chunk

- **Target:** `alloc.Chunk [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 3
- **Priority Score:** 3082205.8
- **Functions:** 12/19 matched (target 20)
- **Missing functions:** `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`
- **Types:** 2/3 matched
- **Missing types:** `ChunkDataEmpty`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chunk.rs` vs expected `values/layout/heap/allocator/alloc/chunk.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chunk.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chunk.rs`)
- **Lint issues:** 1

### 68. list.alloc

- **Target:** `list.Alloc [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 3
- **Priority Score:** 3040510.0
- **Functions:** 0/3 matched (target 0)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 69. list.list_type

- **Target:** `list.ListType`
- **Similarity:** 0.37
- **Dependents:** 3
- **Priority Score:** 3030506.2
- **Functions:** 1/2 matched (target 5)
- **Missing functions:** `unpack_value_impl`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 70. stdlib.call_stack

- **Target:** `stdlib.CallStack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 3
- **Priority Score:** 3021404.0
- **Functions:** 11/13 matched (target 14)
- **Missing functions:** `fmt`, `global`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/callStack.rs` vs expected `stdlib/call_stack.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/call_stack.rs` (current: `// port-lint: tests src/stdlib/callStack.rs`)
- **Lint issues:** 1

### 71. profile.instant

- **Target:** `profile.Instant`
- **Similarity:** 0.34
- **Dependents:** 3
- **Priority Score:** 3020606.5
- **Functions:** 3/4 matched (target 8)
- **Missing functions:** `sub`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`

### 72. values.unpack_and_discard

- **Target:** `values.UnpackAndDiscard`
- **Similarity:** 0.34
- **Dependents:** 3
- **Priority Score:** 3020506.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 73. errors.did_you_mean

- **Target:** `errors.DidYouMean [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 3
- **Priority Score:** 3000604.5
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/errors/didYouMean.rs` vs expected `errors/did_you_mean.rs`
- **Proposed provenance header:** `// port-lint: tests errors/did_you_mean.rs` (current: `// port-lint: tests src/errors/didYouMean.rs`)
- **Lint issues:** 1

### 74. compiler.constants

- **Target:** `compiler.Constants [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 3
- **Priority Score:** 3000505.5
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/compiler/constants.rs` vs expected `eval/compiler/constants.rs`
- **Proposed provenance header:** `// port-lint: tests eval/compiler/constants.rs` (current: `// port-lint: tests src/eval/compiler/constants.rs`)
- **Lint issues:** 1

### 75. sealed

- **Target:** `starlark.Sealed`
- **Similarity:** 1.00
- **Dependents:** 3
- **Priority Score:** 3000100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 76. util.arc_or_static

- **Target:** `util.ArcOrStatic`
- **Similarity:** 0.42
- **Dependents:** 2
- **Priority Score:** 2061305.9
- **Functions:** 5/10 matched
- **Missing functions:** `fmt`, `eq`, `partial_cmp`, `cmp`, `hash`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Target`

### 77. typing.type_type

- **Target:** `typing.TypeType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 2
- **Priority Score:** 2040803.4
- **Functions:** 3/5 matched (target 6)
- **Missing functions:** `module`, `takes_type`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/type_type.rs` vs expected `values/typing/type_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/typing/type_type.rs` (current: `// port-lint: tests src/values/typing/type_type.rs`)
- **Lint issues:** 1

### 78. alloc.chunk_part

- **Target:** `alloc.ChunkPart [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 2
- **Priority Score:** 2031602.0
- **Functions:** 12/15 matched (target 17)
- **Missing functions:** `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chunk_part.rs` vs expected `values/layout/heap/allocator/alloc/chunk_part.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chunk_part.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chunk_part.rs`)
- **Lint issues:** 1

### 79. compiler.small_vec_1

- **Target:** `compiler.SmallVec1`
- **Similarity:** 0.57
- **Dependents:** 2
- **Priority Score:** 2031504.2
- **Functions:** 11/11 matched (target 16)
- **Missing functions:** _none_
- **Types:** 1/4 matched (target 3)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 80. layout.const_type_id

- **Target:** `layout.ConstTypeId`
- **Similarity:** 0.14
- **Dependents:** 2
- **Priority Score:** 2030608.5
- **Functions:** 2/5 matched (target 6)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 81. avalues.str_

- **Target:** `avalues.Str`
- **Similarity:** 0.48
- **Dependents:** 2
- **Priority Score:** 2021405.2
- **Functions:** 11/11 matched (target 12)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Lint issues:** 1

### 82. values.stack_guard

- **Target:** `values.StackGuard`
- **Similarity:** 0.35
- **Dependents:** 2
- **Priority Score:** 2020506.5
- **Functions:** 2/4 matched
- **Missing functions:** `drop`, `inc`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 83. runtime.rust_loc

- **Target:** `runtime.RustLoc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 2
- **Priority Score:** 2020302.0
- **Functions:** 1/3 matched (target 4)
- **Missing functions:** `rust_loc_globals`, `invoke`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/rust_loc.rs` vs expected `eval/runtime/rust_loc.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/rust_loc.rs` (current: `// port-lint: tests src/eval/runtime/rust_loc.rs`)
- **Lint issues:** 1

### 84. values.owned_frozen_ref

- **Target:** `values.OwnedFrozenRef`
- **Similarity:** 0.72
- **Dependents:** 2
- **Priority Score:** 2011502.9
- **Functions:** 12/12 matched (target 19)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Target`

### 85. collections.aligned_padded_str

- **Target:** `collections.AlignedPaddedStr`
- **Similarity:** 0.35
- **Dependents:** 2
- **Priority Score:** 2010406.5
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 86. profile.string_index

- **Target:** `profile.StringIndex`
- **Similarity:** 0.68
- **Dependents:** 2
- **Priority Score:** 2000403.1
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

- **Target:** `collections.StringPool`
- **Similarity:** 0.41
- **Dependents:** 2
- **Priority Score:** 2000305.9
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 89. def_inline.local_as_value

- **Target:** `definline.LocalAsValue`
- **Similarity:** 0.60
- **Dependents:** 2
- **Priority Score:** 2000204.0
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 90. hint

- **Target:** `starlark.Hint`
- **Similarity:** 0.92
- **Dependents:** 2
- **Priority Score:** 2000200.8
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 91. heap.arena

- **Target:** `heap.Arena`
- **Similarity:** 0.46
- **Dependents:** 1
- **Priority Score:** 1124405.4
- **Functions:** 26/37 matched (target 33)
- **Missing functions:** `max`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty`
- **Types:** 6/7 matched (target 8)
- **Missing types:** `Item`
- **Tests:** 0/7 matched

### 92. stdlib

- **Target:** `starlark.Stdlib [PROVENANCE-FALLBACK]`
- **Similarity:** 0.39
- **Dependents:** 1
- **Priority Score:** 1111706.1
- **Functions:** 5/14 matched (target 11)
- **Missing functions:** `global`, `nop`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Bool2`, `Error`
- **Tests:** 2/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib.rs` vs expected `stdlib.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib.rs` (current: `// port-lint: tests src/stdlib.rs`)
- **Lint issues:** 1

### 93. types.list_or_tuple

- **Target:** `types.ListOrTuple`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1061008.0
- **Functions:** 3/5 matched (target 6)
- **Missing functions:** `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 2)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched

### 94. layout.pointer

- **Target:** `layout.Pointer [PROVENANCE-FALLBACK]`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1043705.0
- **Functions:** 28/32 matched (target 49)
- **Missing functions:** `fmt`, `from_usize_unchecked`, `to_usize`, `unpack`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/pointer.rs` vs expected `values/layout/pointer.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/pointer.rs` (current: `// port-lint: tests src/values/layout/pointer.rs`)
- **Lint issues:** 1

### 95. stdlib.breakpoint

- **Target:** `stdlib.Breakpoint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 1
- **Priority Score:** 1042303.8
- **Functions:** 14/17 matched (target 19)
- **Missing functions:** `global`, `breakpoint`, `reset_global_state`
- **Types:** 5/6 matched
- **Missing types:** `Handler`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/breakpoint.rs` vs expected `stdlib/breakpoint.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/breakpoint.rs` (current: `// port-lint: tests src/stdlib/breakpoint.rs`)
- **Lint issues:** 1

### 96. types.any_complex

- **Target:** `types.AnyComplex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1041210.0
- **Functions:** 5/7 matched (target 8)
- **Missing functions:** `fmt`, `freeze`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Canonical`, `Frozen`
- **Tests:** 1/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/any_complex.rs` vs expected `values/types/any_complex.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/any_complex.rs` (current: `// port-lint: tests src/values/types/any_complex.rs`)
- **Lint issues:** 1

### 97. types.any_array

- **Target:** `types.AnyArray [PROVENANCE-FALLBACK]`
- **Similarity:** 0.39
- **Dependents:** 1
- **Priority Score:** 1031006.1
- **Functions:** 5/7 matched (target 9)
- **Missing functions:** `fmt`, `drop`
- **Types:** 2/3 matched
- **Missing types:** `Canonical`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/any_array.rs` vs expected `values/types/any_array.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/any_array.rs` (current: `// port-lint: tests src/values/types/any_array.rs`)
- **Lint issues:** 1

### 98. bc.if_debug

- **Target:** `bc.IfDebug`
- **Similarity:** 0.40
- **Dependents:** 1
- **Priority Score:** 1030906.0
- **Functions:** 5/8 matched (target 9)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 99. util.rtabort

- **Target:** `util.Rtabort [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 1
- **Priority Score:** 1030704.9
- **Functions:** 3/6 matched
- **Missing functions:** `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/util/rtabort.rs` vs expected `util/rtabort.rs`
- **Proposed provenance header:** `// port-lint: tests util/rtabort.rs` (current: `// port-lint: tests src/util/rtabort.rs`)
- **Lint issues:** 1

### 100. runtime.cheap_call_stack

- **Target:** `runtime.CheapCallStack`
- **Similarity:** 0.73
- **Dependents:** 1
- **Priority Score:** 1022002.7
- **Functions:** 15/17 matched
- **Missing functions:** `fmt`, `default`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_

### 101. string.dot_format

- **Target:** `string.DotFormat [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 1
- **Priority Score:** 1021202.9
- **Functions:** 9/11 matched (target 10)
- **Missing functions:** `new`, `format_capture_for_test`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/dot_format.rs` vs expected `values/types/string/dot_format.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/string/dot_format.rs` (current: `// port-lint: tests src/values/types/string/dot_format.rs`)
- **Lint issues:** 1

### 102. avalues.simple

- **Target:** `avalues.Simple`
- **Similarity:** 0.70
- **Dependents:** 1
- **Priority Score:** 1021102.9
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 103. layout.value_captured

- **Target:** `layout.ValueCaptured`
- **Similarity:** 0.80
- **Dependents:** 1
- **Priority Score:** 1020802.0
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Frozen`

### 104. record.field

- **Target:** `record.Field`
- **Similarity:** 0.61
- **Dependents:** 1
- **Priority Score:** 1020603.9
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 0/1 matched
- **Missing types:** `FieldGen`

### 105. util.non_static_type_id

- **Target:** `util.NonStaticTypeId [PROVENANCE-FALLBACK]`
- **Similarity:** 0.22
- **Dependents:** 1
- **Priority Score:** 1020407.8
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `get_type_id`
- **Types:** 0/1 matched
- **Missing types:** `NonStaticAny`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/util/nonStaticTypeId.rs` vs expected `util/non_static_type_id.rs`
- **Proposed provenance header:** `// port-lint: tests util/non_static_type_id.rs` (current: `// port-lint: tests src/util/nonStaticTypeId.rs`)
- **Lint issues:** 1

### 106. collections.alloca

- **Target:** `collections.Alloca [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 1
- **Priority Score:** 1012604.5
- **Functions:** 21/22 matched (target 32)
- **Missing functions:** `len_in_to_to_len_in_words`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/collections/alloca.rs` vs expected `collections/alloca.rs`
- **Proposed provenance header:** `// port-lint: tests collections/alloca.rs` (current: `// port-lint: tests src/collections/alloca.rs`)
- **Lint issues:** 4

### 107. typing.bindings

- **Target:** `typing.Bindings`
- **Similarity:** 0.69
- **Dependents:** 1
- **Priority Score:** 1011103.1
- **Functions:** 7/8 matched (target 18)
- **Missing functions:** `get_for_clause`
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_
- **Lint issues:** 1

### 108. typing.structs

- **Target:** `typing.Structs`
- **Similarity:** 0.63
- **Dependents:** 1
- **Priority Score:** 1011003.7
- **Functions:** 7/8 matched (target 9)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 109. structs.unordered_hasher

- **Target:** `structs.UnorderedHasher`
- **Similarity:** 0.64
- **Dependents:** 1
- **Priority Score:** 1010603.6
- **Functions:** 4/5 matched (target 4)
- **Missing functions:** `_write`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 110. read_line

- **Target:** `starlark.ReadLine`
- **Similarity:** 0.28
- **Dependents:** 1
- **Priority Score:** 1010407.2
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `NoRustyline`

### 111. tests.before_stmt

- **Target:** `tests.BeforeStmt`
- **Similarity:** 0.91
- **Dependents:** 1
- **Priority Score:** 1010100.9
- **Functions:** 0/1 matched
- **Missing functions:** `before_stmt`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 112. string.interpolation

- **Target:** `string.Interpolation [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 1
- **Priority Score:** 1001601.2
- **Functions:** 12/12 matched (target 14)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 21)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/interpolation.rs` vs expected `values/types/string/interpolation.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/string/interpolation.rs` (current: `// port-lint: tests src/values/types/string/interpolation.rs`)
- **Lint issues:** 1

### 113. typing.function

- **Target:** `typing.Function`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001502.6
- **Functions:** 12/12 matched (target 24)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 114. heap.fast_cell

- **Target:** `heap.FastCell`
- **Similarity:** 0.59
- **Dependents:** 1
- **Priority Score:** 1000804.1
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 115. analysis.lint_message

- **Target:** `analysis.LintMessage`
- **Similarity:** 0.81
- **Dependents:** 1
- **Priority Score:** 1000201.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 116. tests

- **Target:** `typing.Tests [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched (target 27)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

### 117. bc.instr_impl

- **Target:** `bc.InstrImpl`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 777001.7
- **Functions:** 7/7 matched (target 95)
- **Missing functions:** _none_
- **Types:** 87/163 matched (target 103)
- **Missing types:** `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc`
- **Lint issues:** 1

### 118. adapter.tests

- **Target:** `tests.Tests`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 222503.4
- **Functions:** 0/22 matched (target 21)
- **Missing functions:** `new`, `event_stopped`, `get_client`, `eval_stopped`, `wait_for_eval_stopped`, `drop`, `breakpoint`, `breakpoints_args`, `eval_with_hook`, `join_timeout`, `dap_test_template`, `test_breakpoint`, `test_breakpoint_with_failing_condition`, `test_breakpoint_with_passing_condition`, `test_step_over`, `test_step_into`, `test_step_out`, `test_local_variables`, `test_inspect_variables`, `test_evaluate_expression`, `assert_variable`, `test_truncate_string`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/22 matched

### 119. values.typing.callable

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 204004.1
- **Functions:** 15/32 matched (target 50)
- **Missing functions:** `clone`, `fmt`, `trace`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `module`, `good`, `bad`
- **Types:** 5/8 matched (target 6)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 2/15 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/callable.rs` vs expected `values/typing/callable.rs`
- **Proposed provenance header:** `// port-lint: tests values/typing/callable.rs` (current: `// port-lint: tests src/values/typing/callable.rs`)
- **Lint issues:** 1

### 120. int.int_or_big

- **Target:** `int.IntOrBig [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 175305.6
- **Functions:** 33/46 matched (target 62)
- **Missing functions:** `from_str`, `unpack_value_impl`, `bitand`, `bitor`, `bitxor`, `neg`, `add`, `sub`, `mul`, `partial_cmp`, `cmp`, `eq`, `int`
- **Types:** 3/7 matched (target 12)
- **Missing types:** `Canonical`, `Err`, `Error`, `Output`
- **Tests:** 8/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/int/intOrBig.rs` vs expected `values/types/int/int_or_big.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/int/int_or_big.rs` (current: `// port-lint: tests src/values/types/int/intOrBig.rs`)
- **Lint issues:** 1

### 121. typing.user

- **Target:** `typing.User [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 153510.0
- **Functions:** 15/27 matched (target 29)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `hash`, `get_type_starlark_repr`, `alloc_value`, `typechecker_ty`, `eval_type`, `invoke`, `globals`, `fruit`, `mk_fruit`
- **Types:** 5/8 matched (target 9)
- **Missing types:** `AbstractPlant`, `FruitCallable`, `Fruit`
- **Tests:** 2/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/typing/user.rs` vs expected `typing/user.rs`
- **Proposed provenance header:** `// port-lint: tests typing/user.rs` (current: `// port-lint: tests src/typing/user.rs`)
- **Lint issues:** 1

### 122. scope.payload

- **Target:** `scope.Payload`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 152405.0
- **Functions:** 6/7 matched (target 21)
- **Missing functions:** `from_ast`
- **Types:** 3/17 matched (target 3)
- **Missing types:** `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CstStmtFromAst`, `CstAssignIdentExt`, `CstExpr`, `CstTypeExpr`, `CstAssignTarget`, `CstAssignIdent`, `CstIdent`, `CstParameter`, `CstStmt`
- **Lint issues:** 3

### 123. list.value

- **Target:** `list.Value`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 136404.2
- **Functions:** 46/56 matched (target 99)
- **Missing functions:** `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare`
- **Types:** 5/8 matched
- **Missing types:** `FrozenList`, `List`, `Canonical`
- **Tests:** 0/7 matched

### 124. layout.typed

- **Target:** `layout.ValueTyped [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 133804.3
- **Functions:** 23/31 matched (target 49)
- **Missing functions:** `fmt`, `serialize`, `eq`, `deref`, `unpack_value_impl`, `module`, `mutable`, `takes_frozen_value_typed`
- **Types:** 2/7 matched (target 3)
- **Missing types:** `Frozen`, `Target`, `Canonical`, `Error`, `NotFrozenError`
- **Tests:** 2/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/typed.rs` vs expected `values/layout/typed.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/typed.rs` (current: `// port-lint: tests src/values/layout/typed.rs`)
- **Lint issues:** 1

### 125. string.str_type

- **Target:** `string.StrType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 125105.4
- **Functions:** 38/47 matched (target 70)
- **Missing functions:** `freeze`, `deref`, `eq`, `partial_cmp`, `cmp`, `fmt`, `new`, `serialize`, `test_str`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `StarlarkStrN`, `Frozen`, `Target`
- **Tests:** 10/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/str_type.rs` vs expected `values/types/string/str_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/string/str_type.rs` (current: `// port-lint: tests src/values/types/string/str_type.rs`)
- **Lint issues:** 1

### 126. float.float

- **Target:** `float.Float [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 124210.0
- **Functions:** 29/39 matched (target 44)
- **Missing functions:** `fmt`, `non_finite`, `test_write_non_finite`, `decimal`, `test_write_decimal`, `scientific`, `test_write_scientific`, `compact`, `test_write_compact`, `test_arithmetic_operators`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 3/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/float/float.rs` vs expected `values/types/float/float.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/float/float.rs` (current: `// port-lint: tests src/values/types/float/float.rs`)
- **Lint issues:** 1

### 127. dict.value

- **Target:** `dict.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 106203.6
- **Functions:** 47/52 matched (target 68)
- **Missing functions:** `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`
- **Types:** 5/10 matched (target 8)
- **Missing types:** `Canonical`, `FrozenDict`, `MutableDict`, `Frozen`, `ContentRef`
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/dict/value.rs` vs expected `values/types/dict/value.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/dict/value.rs` (current: `// port-lint: tests src/values/types/dict/value.rs`)
- **Lint issues:** 1

### 128. tests.markdown

- **Target:** `tests.Markdown [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102910.0
- **Functions:** 17/27 matched (target 32)
- **Missing functions:** `module`, `object`, `golden_docs_starlark`, `native_docs_module`, `linked_ty_mapper`, `globals_render_default`, `globals_render_default_with_linked_type`, `globals_render_signature_at_bottom`, `globals_render_signature_at_bottom_with_linked_type`, `golden_docs_object`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched

### 129. typing.small_arc_vec_or_static

- **Target:** `typing.SmallArcVecOrStatic`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 101507.5
- **Functions:** 3/10 matched
- **Missing functions:** `default`, `deref`, `eq`, `hash`, `partial_cmp`, `cmp`, `into_iter`
- **Types:** 2/5 matched (target 4)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 130. pagable.vtable_registry

- **Target:** `pagable.VtableRegistry [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 91703.7
- **Functions:** 5/13 matched (target 22)
- **Missing functions:** `registered_type_ids`, `test_simple_type_is_registered`, `test_complex_type_frozen_is_registered`, `test_starlark_str_is_registered`, `test_frozen_tuple_is_registered`, `test_frozen_list_is_registered`, `test_type_compiled_non_generic_matcher_is_registered`, `test_type_compiled_generic_matcher_is_registered`
- **Types:** 3/4 matched (target 14)
- **Missing types:** `TestComplexGen`
- **Tests:** 1/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/pagable/vtableRegistry.rs` vs expected `pagable/vtable_registry.rs`
- **Proposed provenance header:** `// port-lint: tests pagable/vtable_registry.rs` (current: `// port-lint: tests src/pagable/vtableRegistry.rs`)
- **Lint issues:** 1

### 131. heap.send

- **Target:** `heap.Send [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 91110.0
- **Functions:** 0/5 matched (target 0)
- **Missing functions:** `new`, `into_inner`, `deref`, `deref_mut`, `fmt`
- **Types:** 2/6 matched (target 2)
- **Missing types:** `Sealed`, `DynStarlark`, `Target`, `StaticType`

### 132. typing.callable

- **Target:** `tests.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 90910.0
- **Functions:** 0/7 matched (target 2)
- **Missing functions:** `new`, `validate_call`, `params`, `result`, `any`, `fmt_with_config`, `fmt`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `TyCallableInner`, `TyCallable`
- **Provenance warning:** port-lint provenance header matched only by basename: `callable.rs` vs expected `typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable.rs` (current: `// port-lint: source callable.rs`)
- **Lint issues:** 1

### 133. thin_box_slice_frozen_value.thin_box

- **Target:** `thinboxslicefrozenvalue.ThinBox [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 83205.5
- **Functions:** 22/29 matched (target 28)
- **Missing functions:** `deref`, `deref_mut`, `fmt`, `eq`, `partial_cmp`, `hash`, `visit`
- **Types:** 2/3 matched
- **Missing types:** `Target`
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `values/thinBoxSliceFrozenValue/thin_box.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: source values/thinBoxSliceFrozenValue/thin_box.rs`)
- **Lint issues:** 1

### 134. bc.instrs

- **Target:** `bc.Instrs`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 82805.9
- **Functions:** 17/24 matched (target 27)
- **Missing functions:** `drop_in_place`, `handle`, `drop_instrs`, `drop`, `opcodes`, `fmt`, `display`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `HandlerImpl`
- **Tests:** 0/2 matched

### 135. alloc.allocator

- **Target:** `alloc.Allocator`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 82106.0
- **Functions:** 11/18 matched (target 15)
- **Missing functions:** `fmt`, `default`, `drop`, `test_small`, `test_big`, `random_iteration`, `test_many`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Item`
- **Tests:** 0/4 matched

### 136. typing.small_arc_vec

- **Target:** `typing.SmallArcVec`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 81406.9
- **Functions:** 4/11 matched (target 16)
- **Missing functions:** `deref`, `default`, `partial_cmp`, `cmp`, `hash`, `fmt`, `from_iter`
- **Types:** 2/3 matched (target 5)
- **Missing types:** `Target`

### 137. tests.uncategorized

- **Target:** `tests.Uncategorized`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 75802.4
- **Functions:** 49/52 matched (target 61)
- **Missing functions:** `unpack_value_impl`, `module`, `freeze`
- **Types:** 2/6 matched (target 3)
- **Missing types:** `Error`, `FrozenWrapper`, `Canonical`, `Frozen`
- **Tests:** 36/36 matched
- **Lint issues:** 1

### 138. type_compiled.compiled

- **Target:** `typecompiled.Compiled`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 74603.0
- **Functions:** 34/39 matched (target 49)
- **Missing functions:** `fmt`, `starlark_type_repr`, `alloc_value`, `hash`, `eq`
- **Types:** 5/7 matched (target 12)
- **Missing types:** `StaticType`, `Canonical`

### 139. num.value

- **Target:** `num.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 72605.1
- **Functions:** 16/22 matched (target 30)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`, `add`, `sub`, `mul`
- **Types:** 3/4 matched (target 7)
- **Missing types:** `Output`
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/num/value.rs` vs expected `values/types/num/value.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/num/value.rs` (current: `// port-lint: tests src/values/types/num/value.rs`)
- **Lint issues:** 1

### 140. string.simd

- **Target:** `string.Simd`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 71009.6
- **Functions:** 1/8 matched (target 1)
- **Missing functions:** `splat`, `load_unaligned`, `store_unaligned`, `cmplt`, `cmpeq`, `or`, `movemask`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 141. compiler.scope

- **Target:** `compiler.Scope`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 67102.3
- **Functions:** 48/51 matched (target 65)
- **Missing functions:** `from`, `assign_ident_impl`, `new`
- **Types:** 17/20 matched (target 28)
- **Missing types:** `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue`

### 142. set.value

- **Target:** `set.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 65902.0
- **Functions:** 49/50 matched (target 62)
- **Missing functions:** `fmt`
- **Types:** 4/9 matched (target 7)
- **Missing types:** `MutableSet`, `FrozenSet`, `Canonical`, `Frozen`, `ContentRef`
- **Tests:** 19/19 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/value.rs` vs expected `values/types/set/value.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/set/value.rs` (current: `// port-lint: tests src/values/types/set/value.rs`)
- **Lint issues:** 1

### 143. heap.heap_type

- **Target:** `heap.HeapType [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 65510.0
- **Functions:** 42/47 matched (target 67)
- **Missing functions:** `fmt`, `hash`, `eq`, `validate_str_interning`, `append_x`
- **Types:** 7/8 matched (target 9)
- **Missing types:** `FrozenHeapName`
- **Tests:** 4/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/heap_type.rs` vs expected `values/layout/heap/heap_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/heap_type.rs` (current: `// port-lint: tests src/values/layout/heap/heap_type.rs`)
- **Lint issues:** 2

### 144. assert.assert

- **Target:** `assert.Assert [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 65210.0
- **Functions:** 44/50 matched (target 72)
- **Missing functions:** `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **TODOs:** 1

### 145. analysis.names

- **Target:** `analysis.Names [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 64303.1
- **Functions:** 31/35 matched (target 42)
- **Missing functions:** `new`, `ident`, `assign_ident`, `about`
- **Types:** 6/8 matched (target 13)
- **Missing types:** `AstStr`, `AstStrExt`
- **Tests:** 9/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/names.rs` vs expected `analysis/names.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/names.rs` (current: `// port-lint: tests src/analysis/names.rs`)
- **Lint issues:** 1

### 146. tuple.value

- **Target:** `tuple.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 63403.7
- **Functions:** 27/31 matched (target 28)
- **Missing functions:** `fmt`, `new`, `offset_of_content`, `typechecker_ty`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Tuple`, `FrozenTuple`
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/value.rs` vs expected `values/types/tuple/value.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/value.rs` (current: `// port-lint: tests src/values/types/tuple/value.rs`)
- **Lint issues:** 1

### 147. profile.bc

- **Target:** `profile.Bc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 62903.6
- **Functions:** 14/19 matched (target 30)
- **Missing functions:** `sum`, `add_assign`, `default`, `test_smoke`, `test_smoke_2`
- **Types:** 9/10 matched (target 14)
- **Missing types:** `Data`
- **Tests:** 2/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/bc.rs` vs expected `eval/runtime/profile/bc.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/bc.rs` (current: `// port-lint: tests src/eval/runtime/profile/bc.rs`)
- **Lint issues:** 1

### 148. thin_box_slice_frozen_value.packed_impl

- **Target:** `thinboxslicefrozenvalue.PackedImpl [PROVENANCE-FALLBACK]`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 62105.3
- **Functions:** 13/18 matched
- **Missing functions:** `visit`, `deref`, `fmt`, `eq`, `across_lengths`
- **Types:** 2/3 matched
- **Missing types:** `Target`
- **Tests:** 5/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `values/thinBoxSliceFrozenValue/packed_impl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: source values/thinBoxSliceFrozenValue/packed_impl.rs`)
- **Lint issues:** 1

### 149. typed.string

- **Target:** `typed.String [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 61805.6
- **Functions:** 9/15 matched (target 39)
- **Missing functions:** `borrow`, `equivalent`, `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/typed/string.rs` vs expected `values/layout/typed/string.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/typed/string.rs` (current: `// port-lint: tests src/values/layout/typed/string.rs`)
- **Lint issues:** 2

### 150. layout.complex

- **Target:** `layout.Complex [PROVENANCE-FALLBACK]`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 61705.1
- **Functions:** 10/13 matched (target 18)
- **Missing functions:** `unpack_value_impl`, `fmt`, `test_module`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 1/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/complex.rs` vs expected `values/layout/complex.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/complex.rs` (current: `// port-lint: tests src/values/layout/complex.rs`)
- **Lint issues:** 1

### 151. analysis.types

- **Target:** `analysis.Types`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 61206.9
- **Functions:** 4/7 matched
- **Missing functions:** `fmt`, `new`, `from`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LintWarning`, `LintT`, `EvalSeverity`

### 152. bigint.convert

- **Target:** `bigint.Convert [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61010.0
- **Functions:** 4/8 matched (target 27)
- **Missing functions:** `unpack_value_impl`, `module`, `takes_i32`, `takes_i64`
- **Types:** 0/2 matched (target 7)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 1/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/bigint/convert.rs` vs expected `values/types/bigint/convert.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/bigint/convert.rs` (current: `// port-lint: tests src/values/types/bigint/convert.rs`)
- **Lint issues:** 1

### 153. tuple.rust_tuple

- **Target:** `tuple.RustTuple`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60610.0
- **Functions:** 0/4 matched (target 11)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`, `starlark_type_repr`, `unpack_value_impl`
- **Types:** 0/2 matched (target 0)
- **Missing types:** `Canonical`, `Error`

### 154. environment.modules

- **Target:** `environment.Modules`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 54702.0
- **Functions:** 38/43 matched (target 52)
- **Missing functions:** `test_send_sync`, `test_gen_heap_summary_profile`, `test_frozen_module_from_globals`, `some_globals`, `foo`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Tests:** 0/5 matched

### 155. values.owned

- **Target:** `values.Owned`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 53402.3
- **Functions:** 26/29 matched (target 32)
- **Missing functions:** `fmt`, `downcast_starlark`, `deref`
- **Types:** 3/5 matched
- **Missing types:** `Canonical`, `Target`

### 156. profile.stmt

- **Target:** `profile.Stmt`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 52603.2
- **Functions:** 13/17 matched (target 23)
- **Missing functions:** `r#gen`, `test_coverage`, `test_empty`, `test_merge`
- **Types:** 8/9 matched
- **Missing types:** `Data`
- **Tests:** 0/3 matched

### 157. stdlib.partial

- **Target:** `stdlib.Partial [PROVENANCE-FALLBACK]`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 51703.5
- **Functions:** 9/12 matched (target 14)
- **Missing functions:** `partial`, `fmt`, `eq`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Frozen`, `Canonical`
- **Tests:** 5/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/partial.rs` vs expected `stdlib/partial.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/partial.rs` (current: `// port-lint: tests src/stdlib/partial.rs`)
- **Lint issues:** 2

### 158. namespace.value

- **Target:** `namespace.Value`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 51702.4
- **Functions:** 10/15 matched (target 17)
- **Missing functions:** `fmt`, `test_repr`, `test_repr_cycle`, `test_to_json_cycle`, `test_kwargs`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 159. dict.refs

- **Target:** `dict.Refs`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 51604.4
- **Functions:** 7/9 matched (target 13)
- **Missing functions:** `from_value`, `deref`
- **Types:** 4/7 matched (target 6)
- **Missing types:** `Target`, `Canonical`, `Error`

### 160. list.unpack

- **Target:** `list.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 51006.5
- **Functions:** 4/5 matched (target 10)
- **Missing functions:** `into_iter`
- **Types:** 1/5 matched
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/unpack.rs` vs expected `values/types/list/unpack.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/list/unpack.rs` (current: `// port-lint: tests src/values/types/list/unpack.rs`)
- **Lint issues:** 1

### 161. tuple.unpack

- **Target:** `tuple.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 51005.6
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `into_iter`
- **Types:** 1/5 matched (target 4)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/unpack.rs` vs expected `values/types/tuple/unpack.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/unpack.rs` (current: `// port-lint: tests src/values/types/tuple/unpack.rs`)
- **Lint issues:** 1

### 162. typing.iter

- **Target:** `typing.Iter`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 51001.2
- **Functions:** 3/6 matched (target 8)
- **Missing functions:** `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `NonInstantiable`, `Canonical`
- **Tests:** 0/3 matched
- **Lint issues:** 1

### 163. compiler.def

- **Target:** `compiler.Def`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 45203.1
- **Functions:** 38/39 matched (target 46)
- **Missing functions:** `fmt`
- **Types:** 10/13 matched (target 15)
- **Missing types:** `Def`, `FrozenDef`, `Frozen`
- **Lint issues:** 3

### 164. params.spec

- **Target:** `params.Spec`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 44403.6
- **Functions:** 34/38 matched (target 34)
- **Missing functions:** `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl`
- **Types:** 6/6 matched (target 11)
- **Missing types:** _none_

### 165. typing.custom

- **Target:** `typing.Custom`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 43804.2
- **Functions:** 31/35 matched (target 50)
- **Missing functions:** `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 166. profile.aggregated

- **Target:** `profile.Aggregated [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 43202.8
- **Functions:** 20/24 matched (target 39)
- **Missing functions:** `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `make`
- **Types:** 8/8 matched (target 11)
- **Missing types:** _none_
- **Tests:** 3/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/profile/aggregated.rs` vs expected `values/layout/heap/profile/aggregated.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/profile/aggregated.rs` (current: `// port-lint: tests src/values/layout/heap/profile/aggregated.rs`)
- **Lint issues:** 1

### 167. profile.time_flame

- **Target:** `profile.TimeFlame [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 43003.1
- **Functions:** 16/19 matched (target 21)
- **Missing functions:** `r#gen`, `register_sleep`, `sleep`
- **Types:** 10/11 matched (target 16)
- **Missing types:** `Data`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/time_flame.rs` vs expected `eval/runtime/profile/time_flame.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/time_flame.rs` (current: `// port-lint: tests src/eval/runtime/profile/time_flame.rs`)
- **Lint issues:** 1

### 168. bc.addr

- **Target:** `bc.Addr`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 42904.1
- **Functions:** 20/23 matched (target 35)
- **Missing functions:** `add_assign`, `get_instr_mut`, `sub_usize`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Output`

### 169. typing.callable_param

- **Target:** `typing.CallableParam [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 42604.0
- **Functions:** 17/20 matched (target 28)
- **Missing functions:** `fmt`, `pf`, `new_named_only`
- **Types:** 5/6 matched (target 11)
- **Missing types:** `ParamSpecDisplay`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/typing/callable_param.rs` vs expected `typing/callable_param.rs`
- **Proposed provenance header:** `// port-lint: tests typing/callable_param.rs` (current: `// port-lint: tests src/typing/callable_param.rs`)
- **Lint issues:** 1

### 170. range.range_type

- **Target:** `range.RangeType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 42502.7
- **Functions:** 20/24 matched (target 41)
- **Missing functions:** `fmt`, `range`, `range_start_stop`, `range_stop`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 5/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/range/range_type.rs` vs expected `values/types/range/range_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/range/range_type.rs` (current: `// port-lint: tests src/values/types/range/range_type.rs`)
- **Lint issues:** 1

### 171. types.function

- **Target:** `types.Function`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 42502.4
- **Functions:** 12/13 matched (target 27)
- **Missing functions:** `new`
- **Types:** 9/12 matched (target 10)
- **Missing types:** `Canonical`, `NativeFuncFn`, `NativeMethFn`

### 172. string.repr

- **Target:** `string.Repr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 42310.0
- **Functions:** 18/22 matched (target 24)
- **Missing functions:** `push_vec_tail`, `test`, `string_repr_for_test`, `load`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 8/11 matched

### 173. values.unpack

- **Target:** `values.Unpack`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 41603.7
- **Functions:** 9/9 matched (target 19)
- **Missing functions:** _none_
- **Types:** 3/7 matched (target 8)
- **Missing types:** `IncorrectType`, `IncorrectParameterTypeWithExpected`, `IncorrectParameterTypeNamedWithExpected`, `Error`

### 174. analysis.dubious

- **Target:** `analysis.Dubious [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 41403.2
- **Functions:** 9/12 matched (target 22)
- **Missing functions:** `lint`, `module`, `about`
- **Types:** 1/2 matched (target 9)
- **Missing types:** `Key`
- **Tests:** 2/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/dubious.rs` vs expected `analysis/dubious.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/dubious.rs` (current: `// port-lint: tests src/analysis/dubious.rs`)
- **Lint issues:** 1

### 175. profile.csv

- **Target:** `profile.Csv [PROVENANCE-FALLBACK]`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 41306.7
- **Functions:** 8/10 matched (target 11)
- **Missing functions:** `new`, `format_for_csv`
- **Types:** 1/3 matched
- **Missing types:** `Impl`, `CsvValue`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/csv.rs` vs expected `eval/runtime/profile/csv.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/csv.rs` (current: `// port-lint: tests src/eval/runtime/profile/csv.rs`)
- **Lint issues:** 1

### 176. runtime.inlined_frame

- **Target:** `runtime.InlinedFrame`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 41202.5
- **Functions:** 5/9 matched
- **Missing functions:** `eq`, `test_inline_into`, `make_span`, `assert_stack`
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 177. params.parser

- **Target:** `params.Parser`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 41002.5
- **Functions:** 5/9 matched (target 10)
- **Missing functions:** `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched

### 178. structs.alloc

- **Target:** `structs.Alloc [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/3 matched (target 0)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 179. dict.alloc

- **Target:** `dict.Alloc`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 40510.0
- **Functions:** 0/3 matched (target 1)
- **Missing functions:** `starlark_type_repr`, `alloc_value`, `alloc_frozen_value`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 180. enumeration.globals

- **Target:** `enumeration.Globals`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 40508.8
- **Functions:** 1/5 matched (target 1)
- **Missing functions:** `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 181. heap.repr

- **Target:** `heap.Repr`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 33204.2
- **Functions:** 24/27 matched (target 36)
- **Missing functions:** `hash`, `eq`, `as_avalue_or_header`
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_

### 182. opt.if_rand

- **Target:** `opt.IfRand`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 32902.2
- **Functions:** 23/26 matched (target 30)
- **Missing functions:** `r#true`, `r#false`, `fmt`
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Tests:** 7/7 matched

### 183. alloc.chain

- **Target:** `alloc.Chain [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 32703.6
- **Functions:** 21/22 matched (target 26)
- **Missing functions:** `drop`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Item`, `ResetSplitAtZeroTest`
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chain.rs` vs expected `values/layout/heap/allocator/alloc/chain.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chain.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chain.rs`)
- **Lint issues:** 1

### 184. analysis.flow

- **Target:** `analysis.Flow [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 32503.2
- **Functions:** 21/24 matched (target 38)
- **Missing functions:** `lint`, `module`, `about`
- **Types:** 1/1 matched (target 12)
- **Missing types:** _none_
- **Tests:** 5/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/flow.rs` vs expected `analysis/flow.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/flow.rs` (current: `// port-lint: tests src/analysis/flow.rs`)
- **Lint issues:** 1

### 185. profile.heap

- **Target:** `profile.Heap`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 32402.0
- **Functions:** 11/13 matched (target 28)
- **Missing functions:** `r#gen`, `test_profiling`
- **Types:** 10/11 matched
- **Missing types:** `Data`
- **Tests:** 0/1 matched

### 186. tests.rustdocs

- **Target:** `tests.Rustdocs`
- **Similarity:** 0.26
- **Dependents:** 0
- **Priority Score:** 31707.4
- **Functions:** 11/14 matched (target 18)
- **Missing functions:** `object`, `func1`, `module`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 187. type_compiled.matcher

- **Target:** `typecompiled.Matcher`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 31702.0
- **Functions:** 10/10 matched (target 13)
- **Missing functions:** _none_
- **Types:** 4/7 matched
- **Missing types:** `TypeMatcher`, `TypeMatcherBoxAlloc`, `Result`

### 188. avalues.list

- **Target:** `avalues.List`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 31405.1
- **Functions:** 9/10 matched (target 18)
- **Missing functions:** `alloc_list_concat`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 189. list.refs

- **Target:** `list.Refs`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 31404.8
- **Functions:** 9/9 matched (target 29)
- **Missing functions:** _none_
- **Types:** 2/5 matched (target 10)
- **Missing types:** `Target`, `Canonical`, `Error`

### 190. analysis.underscore

- **Target:** `analysis.Underscore [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 31403.8
- **Functions:** 10/13 matched (target 19)
- **Missing functions:** `lint`, `about`, `module`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 2/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/underscore.rs` vs expected `analysis/underscore.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/underscore.rs` (current: `// port-lint: tests src/analysis/underscore.rs`)
- **Lint issues:** 1

### 191. avalues.static_

- **Target:** `avalues.Static`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 31403.4
- **Functions:** 8/9 matched (target 11)
- **Missing functions:** `test_alloc_static_simple`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Tests:** 0/1 matched

### 192. symbol.map

- **Target:** `symbol.Map`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 31306.0
- **Functions:** 9/12 matched (target 11)
- **Missing functions:** `fmt`, `new`, `with_capacity`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 193. stdlib.json

- **Target:** `stdlib.Json [PROVENANCE-FALLBACK]`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 31207.3
- **Functions:** 9/11 matched (target 28)
- **Missing functions:** `alloc_value`, `alloc_frozen_value`
- **Types:** 0/1 matched (target 12)
- **Missing types:** `Canonical`
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/json.rs` vs expected `stdlib/json.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/json.rs` (current: `// port-lint: tests src/stdlib/json.rs`)
- **Lint issues:** 1

### 194. module.named_positional

- **Target:** `module.NamedPositional`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 31103.9
- **Functions:** 8/11 matched (target 8)
- **Missing functions:** `positional`, `named`, `named_only`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 195. tuple.refs

- **Target:** `tuple.Refs`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 31103.6
- **Functions:** 6/7 matched (target 15)
- **Missing functions:** `unpack_value_impl`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Error`

### 196. bc.repr

- **Target:** `bc.Repr`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 30906.0
- **Functions:** 4/6 matched (target 5)
- **Missing functions:** `size_of_repr`, `handle`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `HandlerImpl`
- **Lint issues:** 2

### 197. debug.inspect

- **Target:** `debug.Inspect [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 30901.7
- **Functions:** 6/9 matched (target 11)
- **Missing functions:** `debugger`, `debug_inspect_stack`, `debug_inspect_variables`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/debug/inspect.rs` vs expected `debug/inspect.rs`
- **Proposed provenance header:** `// port-lint: tests debug/inspect.rs` (current: `// port-lint: tests src/debug/inspect.rs`)
- **Lint issues:** 1

### 198. string.alloc_unpack

- **Target:** `string.AllocUnpack`
- **Similarity:** 0.39
- **Dependents:** 0
- **Priority Score:** 30806.1
- **Functions:** 5/6 matched (target 9)
- **Missing functions:** `unpack_value_impl`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 199. profile.mode

- **Target:** `profile.Mode`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 30607.3
- **Functions:** 2/4 matched
- **Missing functions:** `fmt`, `from_str`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Err`

### 200. freeze.validator_order

- **Target:** `freeze.ValidatorOrder`
- **Similarity:** 0.53
- **Dependents:** 0
- **Priority Score:** 30604.7
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `test`
- **Types:** 1/3 matched
- **Missing types:** `Frozen`, `Test`
- **Tests:** 0/1 matched
- **Lint issues:** 1

### 201. values.typing.ty

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Ty`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 30602.2
- **Functions:** 2/5 matched (target 7)
- **Missing functions:** `test_isinstance`, `test_pass`, `test_fail_compile_time`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched

### 202. tests.freeze_access_value

- **Target:** `tests.FreezeAccessValue`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 30406.0
- **Functions:** 1/2 matched
- **Missing functions:** `test`
- **Types:** 0/2 matched (target 3)
- **Missing types:** `Test`, `Frozen`
- **Tests:** 0/1 matched

### 203. layout.vtable

- **Target:** `layout.Vtable`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 27302.0
- **Functions:** 67/67 matched (target 75)
- **Missing functions:** _none_
- **Types:** 4/6 matched (target 4)
- **Missing types:** `GetTypeId`, `GetAllocativeKey`

### 204. compiler.expr

- **Target:** `compiler.Expr`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 27002.6
- **Functions:** 59/59 matched (target 63)
- **Missing functions:** _none_
- **Types:** 9/11 matched (target 56)
- **Missing types:** `AstLiteralCompile`, `CompilerExprUtil`

### 205. adapter.implementation

- **Target:** `adapter.Implementation`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 22902.8
- **Functions:** 22/23 matched (target 28)
- **Missing functions:** `fmt`
- **Types:** 5/6 matched (target 10)
- **Missing types:** `ToEvalMessage`
- **Lint issues:** 1

### 206. stdlib.extra

- **Target:** `stdlib.Extra [PROVENANCE-FALLBACK]`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 22005.5
- **Functions:** 15/16 matched (target 26)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched
- **Missing types:** `PrintHandlerImpl`
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/extra.rs` vs expected `stdlib/extra.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/extra.rs` (current: `// port-lint: tests src/stdlib/extra.rs`)
- **Lint issues:** 1

### 207. bc.stack_ptr

- **Target:** `bc.StackPtr`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 21903.5
- **Functions:** 10/11 matched (target 25)
- **Missing functions:** `add`
- **Types:** 7/8 matched (target 7)
- **Missing types:** `Output`

### 208. tests.call

- **Target:** `tests.Call [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21810.0
- **Functions:** 16/18 matched (target 20)
- **Missing functions:** `funcall_test`, `funcall_extra_args_def`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 13/15 matched

### 209. docs.parse

- **Target:** `docs.Parse [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 21603.0
- **Functions:** 14/15 matched (target 18)
- **Missing functions:** `arg`
- **Types:** 0/1 matched
- **Missing types:** `DocStringKind`
- **Tests:** 6/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/docs/parse.rs` vs expected `docs/parse.rs`
- **Proposed provenance header:** `// port-lint: tests docs/parse.rs` (current: `// port-lint: tests src/docs/parse.rs`)
- **Lint issues:** 1

### 210. analysis.incompatible

- **Target:** `analysis.Incompatible [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 21502.7
- **Functions:** 12/14 matched (target 20)
- **Missing functions:** `lint`, `module`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 2/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/incompatible.rs` vs expected `analysis/incompatible.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/incompatible.rs` (current: `// port-lint: tests src/analysis/incompatible.rs`)
- **Lint issues:** 1

### 211. compiler.args

- **Target:** `compiler.Args`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 21304.0
- **Functions:** 10/11 matched
- **Missing functions:** `args`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Never`

### 212. profile.typecheck

- **Target:** `profile.Typecheck [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 21303.2
- **Functions:** 7/8 matched
- **Missing functions:** `r#gen`
- **Types:** 4/5 matched (target 6)
- **Missing types:** `Data`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/typecheck.rs` vs expected `eval/runtime/profile/typecheck.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/typecheck.rs` (current: `// port-lint: tests src/eval/runtime/profile/typecheck.rs`)
- **Lint issues:** 1

### 213. avalues.array

- **Target:** `avalues.Array`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 21303.2
- **Functions:** 9/9 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 214. profile.summary_by_function

- **Target:** `profile.SummaryByFunction`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 21303.1
- **Functions:** 9/10 matched
- **Missing functions:** `drop_non_drop`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `RowKind`
- **Tests:** 0/1 matched

### 215. analysis

- **Target:** `starlark.Analysis [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 21302.7
- **Functions:** 10/12 matched (target 11)
- **Missing functions:** `lint`, `module`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 10/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis.rs` vs expected `analysis.rs`
- **Proposed provenance header:** `// port-lint: tests analysis.rs` (current: `// port-lint: tests src/analysis.rs`)
- **Lint issues:** 1

### 216. bc.opcode

- **Target:** `bc.Opcode [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 21204.5
- **Functions:** 7/7 matched (target 11)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `ByNumber`, `FindOpcode`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/bc/opcode.rs` vs expected `eval/bc/opcode.rs`
- **Proposed provenance header:** `// port-lint: tests eval/bc/opcode.rs` (current: `// port-lint: tests src/eval/bc/opcode.rs`)
- **Lint issues:** 1

### 217. avalues.tuple

- **Target:** `avalues.Tuple`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 21204.2
- **Functions:** 8/8 matched (target 16)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 218. record.globals

- **Target:** `record.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 21202.1
- **Functions:** 10/12 matched (target 10)
- **Missing functions:** `record`, `field`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/globals.rs` vs expected `values/types/record/globals.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/record/globals.rs` (current: `// port-lint: tests src/values/types/record/globals.rs`)
- **Lint issues:** 1

### 219. tests.basic

- **Target:** `tests.Basic`
- **Similarity:** 0.94
- **Dependents:** 0
- **Priority Score:** 21200.6
- **Functions:** 10/12 matched
- **Missing functions:** `arithmetic_test`, `bitwise_test`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/11 matched

### 220. avalues.complex

- **Target:** `avalues.Complex`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 21103.9
- **Functions:** 6/6 matched (target 14)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `StarlarkValue`, `ExtraElem`

### 221. analysis.performance

- **Target:** `analysis.Performance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 21103.2
- **Functions:** 8/10 matched (target 17)
- **Missing functions:** `lint`, `module`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 2/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/performance.rs` vs expected `analysis/performance.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/performance.rs` (current: `// port-lint: tests src/analysis/performance.rs`)
- **Lint issues:** 1

### 222. eval.bc.compiler.stmt

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 21004.1
- **Functions:** 8/10 matched (target 11)
- **Missing functions:** `write_if_then`, `write_if_else`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 223. symbol.symbol

- **Target:** `symbol.Symbol`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 21003.7
- **Functions:** 7/9 matched (target 11)
- **Missing functions:** `fmt`, `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 224. set.refs

- **Target:** `set.Refs`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 21002.9
- **Functions:** 5/5 matched (target 10)
- **Missing functions:** _none_
- **Types:** 3/5 matched
- **Missing types:** `Canonical`, `Error`

### 225. module.basic

- **Target:** `module.Basic`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 20905.7
- **Functions:** 7/9 matched (target 8)
- **Missing functions:** `r#enum`, `test`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 226. bc.bytecode

- **Target:** `bc.Bytecode`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 20904.0
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `handle`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `HandlerImpl`

### 227. bc.call

- **Target:** `bc.Call`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 20903.3
- **Functions:** 3/4 matched (target 15)
- **Missing functions:** `fmt`
- **Types:** 4/5 matched (target 8)
- **Missing types:** `Args`
- **Lint issues:** 8

### 228. structs.refs

- **Target:** `structs.Refs`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 20903.0
- **Functions:** 5/5 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/4 matched
- **Missing types:** `Canonical`, `Error`

### 229. bc.instr_arg

- **Target:** `bc.InstrArg`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 20902.6
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 42)
- **Missing types:** `HandlerImpl`
- **Lint issues:** 25

### 230. derive.docs

- **Target:** `derive.Docs`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 20805.6
- **Functions:** 4/6 matched (target 7)
- **Missing functions:** `foo`, `serialize`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 231. heap.call_enter_exit

- **Target:** `heap.CallEnterExit`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20710.0
- **Functions:** 0/1 matched (target 4)
- **Missing functions:** `drop`
- **Types:** 5/6 matched (target 5)
- **Missing types:** `Canonical`

### 232. types.any

- **Target:** `types.Any`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 20702.9
- **Functions:** 4/5 matched
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Canonical`

### 233. dict.traits

- **Target:** `dict.Traits`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 20606.7
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 6)
- **Missing types:** `Canonical`, `Error`

### 234. list.globals

- **Target:** `list.Globals`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 20604.9
- **Functions:** 4/5 matched
- **Missing functions:** `list`
- **Types:** 0/1 matched
- **Missing types:** `ListType`
- **Lint issues:** 1

### 235. float.unpack

- **Target:** `float.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 20604.4
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/float/unpack.rs` vs expected `values/types/float/unpack.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/float/unpack.rs` (current: `// port-lint: tests src/values/types/float/unpack.rs`)
- **Lint issues:** 1

### 236. freeze.bounds

- **Target:** `freeze.Bounds`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 20604.0
- **Functions:** 2/3 matched (target 5)
- **Missing functions:** `assert_impl`
- **Types:** 2/3 matched
- **Missing types:** `Test`
- **Tests:** 0/1 matched

### 237. int.i32

- **Target:** `int.I32`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 20602.7
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 4)
- **Missing types:** `Canonical`, `Error`

### 238. dict.unpack

- **Target:** `dict.Unpack`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 20602.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`

### 239. bc.definitely_assigned

- **Target:** `bc.DefinitelyAssigned`
- **Similarity:** 0.42
- **Dependents:** 0
- **Priority Score:** 20505.8
- **Functions:** 2/4 matched (target 7)
- **Missing functions:** `new`, `assert_smaller_then`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 240. funcs.min_max

- **Target:** `funcs.MinMax`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 20505.7
- **Functions:** 3/5 matched (target 3)
- **Missing functions:** `max`, `min`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 241. typing.any

- **Target:** `typing.Any`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 20501.8
- **Functions:** 2/4 matched (target 6)
- **Missing functions:** `test_any_runtime`, `test_any_compile_time`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 242. module.return_impl

- **Target:** `module.ReturnImpl`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 20407.9
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `func`, `attr`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 243. collections.maybe_uninit_backport

- **Target:** `collections.MaybeUninitBackport`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 20406.9
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `Guard`

### 244. debug.evaluate

- **Target:** `debug.Evaluate [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 20401.9
- **Functions:** 2/4 matched (target 5)
- **Missing functions:** `debugger`, `debug_evaluate`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/debug/evaluate.rs` vs expected `debug/evaluate.rs`
- **Proposed provenance header:** `// port-lint: tests debug/evaluate.rs` (current: `// port-lint: tests src/debug/evaluate.rs`)
- **Lint issues:** 1

### 245. enumeration.ty_enum_type

- **Target:** `enumeration.TyEnumType`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20310.0
- **Functions:** 0/2 matched (target 3)
- **Missing functions:** `eq`, `hash`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 246. heap.maybe_uninit_slice_util

- **Target:** `heap.MaybeUninitSliceUtil`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 20306.6
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `drop`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `WriteRemOnDrop`

### 247. tests.callable

- **Target:** `typing.Callable`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/2 matched (target 10)
- **Missing functions:** `test_callable_with_args`, `test_callable_named`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 248. values.traits

- **Target:** `values.Traits`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 15902.5
- **Functions:** 56/56 matched
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Canonical`
- **Lint issues:** 9

### 249. debug.adapter

- **Target:** `debug.Adapter`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 13603.2
- **Functions:** 21/22 matched (target 23)
- **Missing functions:** `fmt`
- **Types:** 14/14 matched (target 29)
- **Missing types:** _none_

### 250. docs

- **Target:** `docs.Docs`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 12304.5
- **Functions:** 12/13 matched (target 16)
- **Missing functions:** `default`
- **Types:** 10/10 matched (target 15)
- **Missing types:** _none_

### 251. structs.value

- **Target:** `structs.Value [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12210.0
- **Functions:** 20/21 matched (target 23)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/structs/value.rs` vs expected `values/types/structs/value.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/structs/value.rs` (current: `// port-lint: tests src/values/types/structs/value.rs`)
- **Lint issues:** 1

### 252. typing.basic

- **Target:** `typing.Basic`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 12002.8
- **Functions:** 18/19 matched (target 20)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_

### 253. funcs.other

- **Target:** `funcs.Other [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11910.0
- **Functions:** 18/19 matched
- **Missing functions:** `r#type`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/funcs/other.rs` vs expected `stdlib/funcs/other.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/funcs/other.rs` (current: `// port-lint: tests src/stdlib/funcs/other.rs`)
- **Lint issues:** 1

### 254. tests.runtime

- **Target:** `tests.Runtime`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 11504.1
- **Functions:** 13/14 matched (target 15)
- **Missing functions:** `drop`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/6 matched

### 255. record.instance

- **Target:** `record.Instance`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 11403.4
- **Functions:** 12/13 matched (target 17)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 256. compiler.def_inline

- **Target:** `compiler.DefInline`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 11403.0
- **Functions:** 9/10 matched (target 9)
- **Missing functions:** `new`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_

### 257. profile.flamegraph

- **Target:** `profile.Flamegraph [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 11302.4
- **Functions:** 9/10 matched (target 19)
- **Missing functions:** `new`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/flamegraph.rs` vs expected `eval/runtime/profile/flamegraph.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/flamegraph.rs` (current: `// port-lint: tests src/eval/runtime/profile/flamegraph.rs`)
- **Lint issues:** 1

### 258. list.methods

- **Target:** `list.Methods [PROVENANCE-FALLBACK]`
- **Similarity:** 0.67
- **Dependents:** 0
- **Priority Score:** 11103.3
- **Functions:** 10/11 matched (target 17)
- **Missing functions:** `list_methods`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/methods.rs` vs expected `values/types/list/methods.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/list/methods.rs` (current: `// port-lint: tests src/values/types/list/methods.rs`)
- **Lint issues:** 3

### 259. type_compiled.factory

- **Target:** `typecompiled.Factory`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 11100.7
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Result`

### 260. module.generic

- **Target:** `module.Generic [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11010.0
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Tests:** 1/1 matched

### 261. bool.value

- **Target:** `bool.Value`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 11005.1
- **Functions:** 8/9 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 262. enumeration.value

- **Target:** `enumeration.Value`
- **Similarity:** 0.57
- **Dependents:** 0
- **Priority Score:** 11004.3
- **Functions:** 8/9 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Lint issues:** 2

### 263. namespace.typing

- **Target:** `namespace.Typing`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 11003.5
- **Functions:** 6/7 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 3/3 matched
- **Missing types:** _none_

### 264. typing.never

- **Target:** `typing.Never [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 10901.2
- **Functions:** 6/6 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/3 matched
- **Missing types:** `Canonical`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/never.rs` vs expected `values/typing/never.rs`
- **Proposed provenance header:** `// port-lint: tests values/typing/never.rs` (current: `// port-lint: tests src/values/typing/never.rs`)
- **Lint issues:** 3

### 265. values.recursive_repr_or_json_guard

- **Target:** `values.RecursiveReprOrJsonGuard`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 10705.9
- **Functions:** 2/3 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 4/4 matched
- **Missing types:** _none_

### 266. tuple.alloc

- **Target:** `tuple.Alloc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 10702.7
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/alloc.rs` vs expected `values/types/tuple/alloc.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/alloc.rs` (current: `// port-lint: tests src/values/types/tuple/alloc.rs`)
- **Lint issues:** 1

### 267. profile.by_type

- **Target:** `profile.ByType`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 10702.4
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `normalize_for_golden_tests`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched

### 268. values.type_repr

- **Target:** `values.TypeRepr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 10604.9
- **Functions:** 3/3 matched (target 8)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 8)
- **Missing types:** `Canonical`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typeRepr.rs` vs expected `values/type_repr.rs`
- **Proposed provenance header:** `// port-lint: tests values/type_repr.rs` (current: `// port-lint: tests src/values/typeRepr.rs`)
- **Lint issues:** 1

### 269. eval.bc.compiler.call

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 10505.9
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `mark_definitely_assigned_after`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 270. types.unbound

- **Target:** `types.Unbound`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 10504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 271. structs.structs

- **Target:** `structs.Structs`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 10503.8
- **Functions:** 3/4 matched
- **Missing functions:** `r#struct`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 272. set.set

- **Target:** `set.Set [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 10502.6
- **Functions:** 4/5 matched (target 4)
- **Missing functions:** `set`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/set.rs` vs expected `values/types/set/set.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/set/set.rs` (current: `// port-lint: tests src/values/types/set/set.rs`)
- **Lint issues:** 1

### 273. module.type_annotation

- **Target:** `module.TypeAnnotation`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 10405.7
- **Functions:** 2/3 matched
- **Missing functions:** `foo`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 274. type_compiled.globals

- **Target:** `typecompiled.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 10404.1
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `eval_type`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/type_compiled/globals.rs` vs expected `values/typing/type_compiled/globals.rs`
- **Proposed provenance header:** `// port-lint: tests values/typing/type_compiled/globals.rs` (current: `// port-lint: tests src/values/typing/type_compiled/globals.rs`)
- **Lint issues:** 1

### 275. freeze.validator

- **Target:** `freeze.Validator`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 10401.6
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 2)
- **Missing types:** `Test`
- **Tests:** 2/2 matched
- **Lint issues:** 1

### 276. dict.globals

- **Target:** `dict.Globals [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10310.0
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `dict`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 277. module.default_value

- **Target:** `module.DefaultValue`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 10305.5
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `foo`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 278. compiler.assign_modify

- **Target:** `compiler.AssignModify`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 10301.2
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/1 matched (target 0)
- **Missing types:** `AssignOnWriteBc`

### 279. pagable.error

- **Target:** `pagable.Error`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched
- **Missing functions:** `from`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 280. runtime.visit_span

- **Target:** `runtime.VisitSpan`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 18)
- **Missing functions:** `visit_spans`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 281. float.globals

- **Target:** `float.Globals`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 10208.2
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `float`
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_

### 282. bool.globals

- **Target:** `bool.Globals`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 10207.9
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `bool`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 283. int.globals

- **Target:** `int.Globals`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 10207.7
- **Functions:** 1/2 matched
- **Missing functions:** `int`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 284. bool.type_repr

- **Target:** `bool.TypeRepr`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 10207.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Canonical`

### 285. tuple.globals

- **Target:** `tuple.Globals`
- **Similarity:** 0.29
- **Dependents:** 0
- **Priority Score:** 10207.1
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `tuple`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 286. range.globals

- **Target:** `range.Globals`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 10207.0
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `range`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 287. namespace.globals

- **Target:** `namespace.Globals`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 10206.9
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `namespace`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 288. num.globals

- **Target:** `num.Globals`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 10206.8
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `abs`
- **Types:** 0/0 matched
- **Missing types:** _none_

### 289. bool.unpack

- **Target:** `bool.Unpack`
- **Similarity:** 0.94
- **Dependents:** 0
- **Priority Score:** 10200.6
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `Error`

### 290. freeze.basic

- **Target:** `freeze.Basic`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 10100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `TestUnitStruct`

### 291. set.methods

- **Target:** `set.Methods [PROVENANCE-FALLBACK]`
- **Similarity:** 0.90
- **Dependents:** 0
- **Priority Score:** 6901.0
- **Functions:** 68/68 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 50/50 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/methods.rs` vs expected `values/types/set/methods.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/set/methods.rs` (current: `// port-lint: tests src/values/types/set/methods.rs`)
- **Lint issues:** 2

### 292. bc.writer

- **Target:** `bc.Writer`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 4601.9
- **Functions:** 42/42 matched (target 44)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_

### 293. string.methods

- **Target:** `string.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 4210.0
- **Functions:** 41/41 matched (target 54)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/methods.rs` vs expected `values/types/string/methods.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/string/methods.rs` (current: `// port-lint: tests src/values/types/string/methods.rs`)
- **Lint issues:** 2

### 294. typing.fill_types_for_lint

- **Target:** `typing.FillTypesForLint`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 4202.4
- **Functions:** 39/39 matched (target 40)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 295. oracle.ctx

- **Target:** `oracle.Ctx`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 3401.9
- **Functions:** 32/32 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 14)
- **Missing types:** _none_

### 296. type_compiled.alloc

- **Target:** `typecompiled.Alloc`
- **Similarity:** 0.90
- **Dependents:** 0
- **Priority Score:** 2901.0
- **Functions:** 28/28 matched (target 37)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 297. type_compiled.matchers

- **Target:** `typecompiled.Matchers`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 2601.4
- **Functions:** 3/3 matched (target 25)
- **Missing functions:** _none_
- **Types:** 23/23 matched
- **Missing types:** _none_
- **Lint issues:** 2

### 298. typing.ctx

- **Target:** `typing.Ctx`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 2002.8
- **Functions:** 19/19 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 299. docs.markdown

- **Target:** `docs.Markdown`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 2001.6
- **Functions:** 18/18 matched (target 19)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 300. dict.methods

- **Target:** `dict.Methods [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1710.0
- **Functions:** 17/17 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/dict/methods.rs` vs expected `values/types/dict/methods.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/dict/methods.rs` (current: `// port-lint: tests src/values/types/dict/methods.rs`)
- **Lint issues:** 2

### 301. scope.tests

- **Target:** `scope.Tests`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 1701.2
- **Functions:** 16/16 matched (target 23)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 302. tests.fstring

- **Target:** `tests.Fstring`
- **Similarity:** 0.97
- **Dependents:** 0
- **Priority Score:** 1700.3
- **Functions:** 17/17 matched (target 18)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 14/14 matched

### 303. environment.names

- **Target:** `environment.Names`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 1503.7
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 304. eval.bc.compiler.expr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 1501.9
- **Functions:** 15/15 matched (target 16)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 305. typing.error

- **Target:** `typing.Error [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 9/9 matched (target 17)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_

### 306. compiler.call

- **Target:** `compiler.Call`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 1402.5
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 307. module.unpack_value

- **Target:** `module.UnpackValue`
- **Similarity:** 0.54
- **Dependents:** 0
- **Priority Score:** 1304.6
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/4 matched

### 308. type_compiled.tests

- **Target:** `typecompiled.Tests [PROVENANCE-FALLBACK]`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 1300.5
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 12/12 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `values/typing/typeCompiled/tests.rs` vs expected `values/typing/type_compiled/tests.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/tests.rs` (current: `// port-lint: source values/typing/typeCompiled/tests.rs`)
- **Lint issues:** 1

### 309. profile.tests

- **Target:** `profile.Tests`
- **Similarity:** 0.96
- **Dependents:** 0
- **Priority Score:** 1300.4
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 12/12 matched

### 310. compiler.compr

- **Target:** `compiler.Compr`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 1202.4
- **Functions:** 9/9 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 311. bc.if_stmt

- **Target:** `bc.IfStmt`
- **Similarity:** 0.98
- **Dependents:** 0
- **Priority Score:** 1200.2
- **Functions:** 12/12 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 12/12 matched

### 312. environment.slots

- **Target:** `environment.Slots`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 1103.4
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 313. docs.multipage

- **Target:** `docs.Multipage`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 1101.4
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 7)
- **Missing types:** _none_

### 314. tests.comprehension

- **Target:** `tests.Comprehension`
- **Similarity:** 0.96
- **Dependents:** 0
- **Priority Score:** 1000.4
- **Functions:** 10/10 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched

### 315. profile.data

- **Target:** `profile.Data`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 903.9
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_

### 316. typing.tests.call

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.typing.tests.Call [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 902.9
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/call.rs` vs expected `typing/tests/call.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/call.rs` (current: `// port-lint: source tests/call.rs`)
- **Lint issues:** 1

### 317. compiler.types

- **Target:** `compiler.Types`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 901.6
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_

### 318. tests.type_annot

- **Target:** `tests.TypeAnnot`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 900.5
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched

### 319. __derive_refs.parse_args

- **Target:** `deriverefs.ParseArgs`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 802.8
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 320. record.ty_record_type

- **Target:** `record.TyRecordType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 801.6
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/tyRecordType.rs` vs expected `values/types/record/ty_record_type.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/record/ty_record_type.rs` (current: `// port-lint: tests src/values/types/record/tyRecordType.rs`)
- **Lint issues:** 1

### 321. opt.def_inline

- **Target:** `opt.DefInline`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 800.7
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 322. tests.opt

- **Target:** `tests.Opt`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 800.1
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 323. bc.and_or

- **Target:** `bc.AndOr`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 800.1
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 324. docs.code

- **Target:** `docs.Code`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 703.1
- **Functions:** 7/7 matched (target 14)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 325. alloc.per_thread

- **Target:** `alloc.PerThread`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 702.9
- **Functions:** 6/6 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 326. layout.value_not_special

- **Target:** `layout.ValueNotSpecial`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 702.8
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 327. unused_loads.find

- **Target:** `unusedloads.Find`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 702.1
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 328. types.known_methods

- **Target:** `types.KnownMethods`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 701.6
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 329. compiler.module

- **Target:** `compiler.Module`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 701.6
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_

### 330. runtime.before_stmt

- **Target:** `runtime.BeforeStmt`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 701.4
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 331. unused_loads.find_tests

- **Target:** `unusedloads.FindTestsTest [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 700.7
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `analysis/unusedLoads/find_tests.rs` vs expected `analysis/unused_loads/find_tests.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/find_tests.rs` (current: `// port-lint: source analysis/unusedLoads/find_tests.rs`)
- **Lint issues:** 1

### 332. opt.eq

- **Target:** `opt.Eq`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 700.1
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 7/7 matched

### 333. module.other_attributes

- **Target:** `module.OtherAttributes [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 334. layout.static_string

- **Target:** `layout.StaticString`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 603.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 335. values.index

- **Target:** `values.Index [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 602.9
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/index.rs` vs expected `values/index.rs`
- **Proposed provenance header:** `// port-lint: tests values/index.rs` (current: `// port-lint: tests src/values/index.rs`)
- **Lint issues:** 1

### 336. assert.conformance

- **Target:** `assert.Conformance`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 602.7
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 337. module.methods

- **Target:** `module.Methods`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 602.4
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 338. tests.list

- **Target:** `tests.List [PROVENANCE-FALLBACK]`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 602.2
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/list.rs` vs expected `typing/tests/list.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/list.rs` (current: `// port-lint: source tests/list.rs`)
- **Lint issues:** 1

### 339. int.tests

- **Target:** `int.Tests`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 602.0
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 340. freeze.identity

- **Target:** `freeze.Identity`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 601.1
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 7)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Lint issues:** 4

### 341. compiler.if_compiler

- **Target:** `compiler.IfCompiler`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 600.7
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 342. string.globals

- **Target:** `string.Globals`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 502.9
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 343. runtime.slots

- **Target:** `runtime.Slots`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 502.4
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 344. values.comparison

- **Target:** `values.Comparison`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 502.1
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 345. funcs.zip

- **Target:** `funcs.Zip`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 502.0
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 346. intern.interner

- **Target:** `intern.Interner [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 501.7
- **Functions:** 3/3 matched (target 7)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/intern/interner.rs` vs expected `values/types/string/intern/interner.rs`
- **Proposed provenance header:** `// port-lint: tests values/types/string/intern/interner.rs` (current: `// port-lint: tests src/values/types/string/intern/interner.rs`)
- **Lint issues:** 1

### 347. compiler.expr_bool

- **Target:** `compiler.ExprBool`
- **Similarity:** 0.87
- **Dependents:** 0
- **Priority Score:** 501.3
- **Functions:** 4/4 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 348. num.typecheck

- **Target:** `num.Typecheck`
- **Similarity:** 0.90
- **Dependents:** 0
- **Priority Score:** 501.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 349. unused_loads.remove

- **Target:** `unusedloads.Remove`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 500.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 350. tests.bc.definitely_assigned

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.tests.bc.DefinitelyAssigned`
- **Similarity:** 0.97
- **Dependents:** 0
- **Priority Score:** 500.3
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 351. module.kwargs

- **Target:** `module.Kwargs`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 405.9
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 352. __derive_refs.components

- **Target:** `deriverefs.Components`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 402.3
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 353. string.iter

- **Target:** `string.Iter`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 401.8
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 354. analysis.find_call_name

- **Target:** `analysis.FindCallName [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 401.7
- **Functions:** 3/3 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/find_call_name.rs` vs expected `analysis/find_call_name.rs`
- **Proposed provenance header:** `// port-lint: tests analysis/find_call_name.rs` (current: `// port-lint: tests src/analysis/find_call_name.rs`)
- **Lint issues:** 1

### 355. tests.util

- **Target:** `tests.Util`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 401.7
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 356. unused_loads.remove_tests

- **Target:** `unusedloads.RemoveTestsTest [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 401.4
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `analysis/unusedLoads/remove_tests.rs` vs expected `analysis/unused_loads/remove_tests.rs`
- **Proposed provenance header:** `// port-lint: source analysis/unused_loads/remove_tests.rs` (current: `// port-lint: source analysis/unusedLoads/remove_tests.rs`)
- **Lint issues:** 1

### 357. __derive_refs.sig

- **Target:** `deriverefs.Sig`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 401.4
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_

### 358. stdlib.internal

- **Target:** `stdlib.Internal [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 401.1
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/internal.rs` vs expected `stdlib/internal.rs`
- **Proposed provenance header:** `// port-lint: tests stdlib/internal.rs` (current: `// port-lint: tests src/stdlib/internal.rs`)
- **Lint issues:** 1

### 359. trace.bounds

- **Target:** `trace.Bounds`
- **Similarity:** 0.98
- **Dependents:** 0
- **Priority Score:** 400.2
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 360. bc.compr

- **Target:** `bc.Compr`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 400.1
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/4 matched

### 361. tests.go

- **Target:** `tests.Go [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 362. derive.attrs

- **Target:** `derive.Attrs`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 304.2
- **Functions:** 1/1 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 363. eval.soft_error

- **Target:** `eval.SoftError`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 304.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 364. oracle.traits

- **Target:** `oracle.Traits`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 304.1
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 365. compiler.error

- **Target:** `compiler.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 303.8
- **Functions:** 2/2 matched (target 21)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 13)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlark_syntax/src/error.rs` vs expected `eval/compiler/error.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source ../starlark_syntax/src/error.rs`)
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
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/types.rs` vs expected `typing/tests/types.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/types.rs` (current: `// port-lint: source tests/types.rs`)
- **Lint issues:** 1

### 367. callable.param

- **Target:** `callable.Param`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 302.4
- **Functions:** 1/1 matched (target 6)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 7)
- **Missing types:** _none_

### 368. opt.type_is

- **Target:** `opt.TypeIs`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 302.4
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 369. module.special_params

- **Target:** `module.SpecialParams`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 302.1
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 370. compiler.type_expr

- **Target:** `compiler.TypeExpr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 301.1
- **Functions:** 2/2 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 17)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlark_syntax/src/syntax/type_expr.rs` vs expected `eval/compiler/type_expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source ../starlark_syntax/src/syntax/type_expr.rs`)
- **Lint issues:** 1

### 371. bc.for_stmt

- **Target:** `bc.ForStmt`
- **Similarity:** 0.97
- **Dependents:** 0
- **Priority Score:** 300.3
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 372. opt.speculative_exec

- **Target:** `opt.SpeculativeExec`
- **Similarity:** 0.98
- **Dependents:** 0
- **Priority Score:** 300.2
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched

### 373. derive.unpack_value_attr

- **Target:** `derive.UnpackValueAttr [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 374. __derive_refs.invoke_macro_error

- **Target:** `deriverefs.InvokeMacroError`
- **Similarity:** 0.33
- **Dependents:** 0
- **Priority Score:** 206.7
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 375. bc.golden

- **Target:** `bc.Golden`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 202.9
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 376. tests.special_function

- **Target:** `tests.SpecialFunction`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 202.3
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 377. typing.macro_refs

- **Target:** `typing.MacroRefs`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 202.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 378. enumeration.matcher

- **Target:** `enumeration.Matcher`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 201.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 379. eval.bc.compiler.def

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 201.6
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlark_syntax/src/syntax/def.rs` vs expected `eval/bc/compiler/def.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/def.rs` (current: `// port-lint: source ../starlark_syntax/src/syntax/def.rs`)
- **Lint issues:** 1

### 380. eval

- **Target:** `eval.Eval`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 201.6
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 381. typing.macro_support

- **Target:** `typing.MacroSupport`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 201.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 382. compiler.assign

- **Target:** `compiler.Assign`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 201.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 383. layout.identity

- **Target:** `layout.Identity`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 201.4
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 384. opt.constant_folding

- **Target:** `opt.ConstantFolding`
- **Similarity:** 0.87
- **Dependents:** 0
- **Priority Score:** 201.3
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 385. record.matcher

- **Target:** `record.Matcher`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 201.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 386. eval.bc.compiler.compr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 200.7
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 387. bool.alloc

- **Target:** `bool.Alloc`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 200.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 388. opt.list_add

- **Target:** `opt.ListAdd`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 200.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 389. opt.types

- **Target:** `opt.Types`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 200.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 390. trace.statics

- **Target:** `trace.Statics`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 391. bc.slow_arg

- **Target:** `bc.SlowArg`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 392. allocator.api

- **Target:** `allocator.Api`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 393. bc.instr

- **Target:** `bc.Instr`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_

### 394. bc.for_loop

- **Target:** `bc.ForLoop [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 110.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 395. wasm

- **Target:** `starlark.Wasm`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 107.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 396. tests.replace_binary

- **Target:** `tests.ReplaceBinary`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 103.0
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 397. none.globals

- **Target:** `none.Globals`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 102.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 398. typing.globals

- **Target:** `typing.Globals`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 102.6
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 399. environment.module_dump

- **Target:** `environment.ModuleDump`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 102.4
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 400. compiler.known

- **Target:** `compiler.Known`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 102.3
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 401. eval.params

- **Target:** `eval.Params`
- **Similarity:** 0.91
- **Dependents:** 0
- **Priority Score:** 100.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 402. tests.for_loop

- **Target:** `tests.ForLoop`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 403. bc.isinstance

- **Target:** `bc.Isinstance`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 404. tests.bc.call

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.tests.bc.Call`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 405. funcs.globals

- **Target:** `funcs.Globals`
- **Similarity:** 0.99
- **Dependents:** 0
- **Priority Score:** 100.1
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 406. typing.mode

- **Target:** `typing.Mode`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 407. profile.or_instrumentation

- **Target:** `profile.OrInstrumentation`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_

### 408. environment

- **Target:** `starlark.Environment`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_

### 409. trace.enums

- **Target:** `trace.Enums`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_

### 410. freeze.enums

- **Target:** `freeze.Enums`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 8)
- **Missing types:** _none_

### 411. typing.call_args

- **Target:** `typing.CallArgs`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 412. pagable.vtable_register

- **Target:** `pagable.VtableRegister [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 413. macros

- **Target:** `starlark.Macros [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 9)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 9)
- **Missing types:** _none_

### 414. types.tuple

- **Target:** `tests.Tuple [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/tuple.rs` vs expected `values/types/tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple.rs` (current: `// port-lint: source tests/tuple.rs`)
- **Lint issues:** 1

### 415. heap.allocator

- **Target:** `alloc.AllocatorTest [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `values/layout/heap/allocator/alloc/allocator.rs` vs expected `values/layout/heap/allocator.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator.rs` (current: `// port-lint: source values/layout/heap/allocator/alloc/allocator.rs`)
- **Lint issues:** 1

### 416. util

- **Target:** `util.Util [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 417. derive.freeze

- **Target:** `derive.Freeze [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 418. collections

- **Target:** `collections.Collections [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 419. derive.trace

- **Target:** `derive.Trace [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 420. docs.tests

- **Target:** `tests.TestsModule [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `tests.rs` vs expected `docs/tests.rs`
- **Proposed provenance header:** `// port-lint: source docs/tests.rs` (current: `// port-lint: source tests.rs`)
- **Lint issues:** 1

### 421. heap.branding

- **Target:** `heap.Branding [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 422. errors

- **Target:** `errors.Errors [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

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
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

