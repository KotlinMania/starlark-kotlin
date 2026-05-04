# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 416/468 (88.9%)
- **Function parity:** 3876/4568 matched (target 6069) — 84.9%
- **Class/type parity:** 845/1209 matched (target 1697) — 69.9%
- **Combined symbol parity:** 4721/5777 matched (target 7766) — 81.7%
- **Average inline-code cosine:** 0.61 (function body across 410 matched files)
- **Average documentation cosine:** 0.68 (doc text across 410 matched files)
- **Cheat-zeroed Files:** 31
- **Critical Issues:** 162 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.81 (needs 4% improvement)
- **Dependencies:** 178
- **Priority Score:** 178042704.0
- **Functions:** 117/118 matched (target 173)
- **Missing functions:** `testing_new_int`
- **Types:** 7/9 matched (target 11)
- **Missing types:** `Canonical`, `String`
- **Symbol Deficit:** 3 (functions: 1, types: 2)
- **Missing Tests:** 1 of 9 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 2. typing.ty
- **Similarity:** 0.80 (needs 5% improvement)
- **Dependencies:** 109
- **Priority Score:** 109005400.0
- **Functions:** 50/50 matched (target 60)
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

### 6. derive.unpack_value
- **Similarity:** 0.71 (needs 14% improvement)
- **Dependencies:** 51
- **Priority Score:** 51000704.0
- **Functions:** 2/2 matched (target 12)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Action:** Review and complete missing sections

### 7. values.freeze
- **Similarity:** 0.69 (needs 16% improvement)
- **Dependencies:** 42
- **Priority Score:** 42010304.0
- **Functions:** 1/1 matched (target 26)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Frozen`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Review and complete missing sections

### 8. coerce
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

### 9. values.frozen_ref
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 27
- **Priority Score:** 27052104.0
- **Functions:** 14/17 matched (target 20)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `Target`, `Frozen`
- **Symbol Deficit:** 5 (functions: 3, types: 2)
- **Action:** Review and complete missing sections

### 10. none.none_type
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 27
- **Priority Score:** 27011302.0
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Minor refinements needed

### 11. runtime.frame_span
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 12. runtime.arguments
- **Similarity:** 0.64 (needs 21% improvement)
- **Dependencies:** 25
- **Priority Score:** 25023804.0
- **Functions:** 28/30 matched (target 45)
- **Missing functions:** `from`, `f`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 1 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 13. util.arc_str
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 21
- **Priority Score:** 21010704.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Review and complete missing sections

### 14. environment.globals
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

### 15. values.value_of_unchecked
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

### 16. __derive_refs.param_spec
- **Similarity:** 0.83 (needs 2% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 17. util.refcell
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000202.0
- **Functions:** 2/2 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 18. derive.module
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 20
- **Priority Score:** 20000010.0
- **Functions:** 0/0 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Action:** Deep review - likely missing major functionality

### 19. values.iter
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 17
- **Priority Score:** 17020704.0
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Item`
- **Symbol Deficit:** 2 (functions: 1, types: 1)
- **Action:** Review and complete missing sections

### 20. environment.methods
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

### 21. values.error
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Review and complete missing sections

### 22. layout.avalue
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

### 23. typing.tuple
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 12070710.0
- **Functions:** 0/6 matched (target 2)
- **Missing functions:** `get`, `item_ty`, `intersects`, `matcher`, `fmt_with_config`, `fmt`
- **Types:** 0/1 matched
- **Missing types:** `TyTuple`
- **Symbol Deficit:** 7 (functions: 6, types: 1)
- **Action:** Deep review - likely missing major functionality

### 24. layout.const_frozen_string
- **Similarity:** 0.79 (needs 6% improvement)
- **Dependencies:** 12
- **Priority Score:** 12000202.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Action:** Minor refinements needed

### 25. int.inline_int
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

2. **assert** (84 deps)
   - Path: `assert.rs`
   - Essential for 84 other files

3. **debug** (53 deps)
   - Path: `debug.rs`
   - Essential for 53 other files

4. **typing.type_compiled** (22 deps)
   - Path: `values/typing/type_compiled.rs`
   - Essential for 22 other files

5. **collections.symbol** (15 deps)
   - Path: `collections/symbol.rs`
   - Essential for 15 other files

6. **types.dict** (12 deps)
   - Path: `values/types/dict.rs`
   - Essential for 12 other files

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. layout.value

- **Target:** `layout.Value`
- **Similarity:** 0.81
- **Dependents:** 178
- **Priority Score:** 178042704.0
- **Functions:** 117/118 matched (target 173)
- **Missing functions:** `testing_new_int`
- **Types:** 7/9 matched (target 11)
- **Missing types:** `Canonical`, `String`
- **Tests:** 8/9 matched
- **Lint issues:** 2

### 2. typing.ty

- **Target:** `typing.Ty`
- **Similarity:** 0.80
- **Dependents:** 109
- **Priority Score:** 109005400.0
- **Functions:** 50/50 matched (target 60)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_

### 3. typing.starlark_value

- **Target:** `typing.StarlarkValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 76
- **Priority Score:** 76003800.0
- **Functions:** 34/34 matched (target 47)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `starlark_value.rs` vs expected `typing/starlark_value.rs`
- **Proposed provenance header:** `// port-lint: source typing/starlark_value.rs` (current: `// port-lint: source starlark_value.rs`)
- **Lint issues:** 4

### 4. params.display

- **Target:** `params.Display`
- **Similarity:** 0.74
- **Dependents:** 76
- **Priority Score:** 76000704.0
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_

### 5. runtime.evaluator

- **Target:** `runtime.Evaluator`
- **Similarity:** 0.83
- **Dependents:** 55
- **Priority Score:** 55006700.0
- **Functions:** 60/60 matched (target 64)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 17)
- **Missing types:** _none_

### 6. values.trace

- **Target:** `values.Trace`
- **Similarity:** 0.90
- **Dependents:** 52
- **Priority Score:** 52000200.0
- **Functions:** 1/1 matched (target 43)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 12)
- **Missing types:** _none_

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

- **Target:** `values.Freeze`
- **Similarity:** 0.69
- **Dependents:** 42
- **Priority Score:** 42010304.0
- **Functions:** 1/1 matched (target 26)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 5)
- **Missing types:** `Frozen`
- **Lint issues:** 1

### 9. values.alloc_value

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

### 10. layout.freezer

- **Target:** `layout.Freezer`
- **Similarity:** 0.85
- **Dependents:** 36
- **Priority Score:** 36000600.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/coerce.rs` vs expected `coerce.rs`
- **Proposed provenance header:** `// port-lint: tests coerce.rs` (current: `// port-lint: tests src/coerce.rs`)
- **Lint issues:** 1

### 12. compiler.span

- **Target:** `compiler.Span`
- **Similarity:** 0.92
- **Dependents:** 29
- **Priority Score:** 29010400.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`

### 13. values.frozen_ref

- **Target:** `values.FrozenRef`
- **Similarity:** 0.62
- **Dependents:** 27
- **Priority Score:** 27052104.0
- **Functions:** 14/17 matched (target 20)
- **Missing functions:** `fmt`, `eq`, `hash`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `Target`, `Frozen`
- **Lint issues:** 2

### 14. none.none_type

- **Target:** `none.NoneType`
- **Similarity:** 0.82
- **Dependents:** 27
- **Priority Score:** 27011302.0
- **Functions:** 11/11 matched (target 13)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Error`
- **Lint issues:** 1

### 15. runtime.frame_span

- **Target:** `runtime.FrameSpan`
- **Similarity:** 0.65
- **Dependents:** 26
- **Priority Score:** 26010504.0
- **Functions:** 3/4 matched
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 16. runtime.arguments

- **Target:** `runtime.Arguments [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 25
- **Priority Score:** 25023804.0
- **Functions:** 28/30 matched (target 45)
- **Missing functions:** `from`, `f`
- **Types:** 8/8 matched (target 16)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/arguments.rs` vs expected `eval/runtime/arguments.rs`
- **Proposed provenance header:** `// port-lint: tests eval/runtime/arguments.rs` (current: `// port-lint: tests src/eval/runtime/arguments.rs`)
- **Lint issues:** 1

### 17. util.arc_str

- **Target:** `util.ArcStr`
- **Similarity:** 0.60
- **Dependents:** 21
- **Priority Score:** 21010704.0
- **Functions:** 5/5 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`

### 18. environment.globals

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

### 19. values.value_of_unchecked

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

### 20. __derive_refs.param_spec

- **Target:** `deriverefs.ParamSpec`
- **Similarity:** 0.83
- **Dependents:** 20
- **Priority Score:** 20000802.0
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_

### 21. util.refcell

- **Target:** `refcell.RefCell [PROVENANCE-FALLBACK]`
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

### 22. derive.module

- **Target:** `syntax.AstModule [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 20
- **Priority Score:** 20000010.0
- **Functions:** 0/0 matched (target 13)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `src/syntax/module.rs` vs expected `tests/derive/module.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module.rs` (current: `// port-lint: source src/syntax/module.rs`)
- **Lint issues:** 1

### 23. values.iter

- **Target:** `values.Iter`
- **Similarity:** 0.61
- **Dependents:** 17
- **Priority Score:** 17020704.0
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `drop`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Item`

### 24. environment.methods

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

### 25. values.error

- **Target:** `values.Error`
- **Similarity:** 0.62
- **Dependents:** 17
- **Priority Score:** 17010704.0
- **Functions:** 4/5 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 19)
- **Missing types:** _none_

### 26. private

- **Target:** `starlark.Private`
- **Similarity:** 1.00
- **Dependents:** 15
- **Priority Score:** 15000100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 27. layout.avalue

- **Target:** `layout.AValue`
- **Similarity:** 0.75
- **Dependents:** 14
- **Priority Score:** 14021103.0
- **Functions:** 6/8 matched (target 12)
- **Missing functions:** `tuple_cycle_freeze`, `test_try_freeze_directly`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 28. typing.tuple

- **Target:** `tests.Tuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 12
- **Priority Score:** 12070710.0
- **Functions:** 0/6 matched (target 2)
- **Missing functions:** `get`, `item_ty`, `intersects`, `matcher`, `fmt_with_config`, `fmt`
- **Types:** 0/1 matched
- **Missing types:** `TyTuple`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/tuple.rs` vs expected `typing/tuple.rs`
- **Proposed provenance header:** `// port-lint: source typing/tuple.rs` (current: `// port-lint: source tests/tuple.rs`)
- **Lint issues:** 1

### 29. layout.const_frozen_string

- **Target:** `layout.ConstFrozenString [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 12
- **Priority Score:** 12000202.0
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/constFrozenString.rs` vs expected `values/layout/const_frozen_string.rs`
- **Proposed provenance header:** `// port-lint: tests values/layout/const_frozen_string.rs` (current: `// port-lint: tests src/values/layout/constFrozenString.rs`)
- **Lint issues:** 1

### 30. layout.value_lifetimeless

- **Target:** `layout.ValueLifetimeless`
- **Similarity:** 1.00
- **Dependents:** 12
- **Priority Score:** 12000100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 31. int.inline_int

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

### 32. int.pointer_i32

- **Target:** `int.PointerI32`
- **Similarity:** 0.66
- **Dependents:** 9
- **Priority Score:** 9043303.0
- **Functions:** 28/31 matched (target 33)
- **Missing functions:** `eq`, `fmt`, `serialize`
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Lint issues:** 2

### 33. types.type_instance_id

- **Target:** `types.TypeInstanceId`
- **Similarity:** 0.00
- **Dependents:** 9
- **Priority Score:** 9010210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `r#gen`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 34. any

- **Target:** `starlark.Any [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 8
- **Priority Score:** 8062710.0
- **Functions:** 8/12 matched (target 15)
- **Missing functions:** `is`, `convert_value`, `convert_any`, `test`
- **Types:** 13/15 matched (target 43)
- **Missing types:** `StaticType`, `My`
- **Tests:** 4/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/any.rs` vs expected `any.rs`
- **Proposed provenance header:** `// port-lint: source any.rs` (current: `// port-lint: source src/any.rs`)
- **Lint issues:** 1

### 35. layout.aligned_size

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
- **Proposed provenance header:** `// port-lint: source values/layout/aligned_size.rs` (current: `// port-lint: source src/values/layout/alignedSize.rs`)
- **Lint issues:** 1

### 36. eval.compiler

- **Target:** `eval.Compiler`
- **Similarity:** 0.81
- **Dependents:** 8
- **Priority Score:** 8000702.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 37. types.bigint

- **Target:** `types.Bigint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 7
- **Priority Score:** 7027401.0
- **Functions:** 71/73 matched (target 77)
- **Missing functions:** `unpack_integer`, `eq`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 42/42 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bigint.rs` vs expected `values/types/bigint.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/bigint.rs` vs expected `values/types/bigint.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bigint.rs` (current: `// port-lint: source src/values/types/bigint.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/bigint.rs` (current: `// port-lint: tests src/values/types/bigint.rs`)
- **Lint issues:** 3

### 38. values.starlark_type_id

- **Target:** `values.StarlarkTypeId`
- **Similarity:** 0.61
- **Dependents:** 7
- **Priority Score:** 7010804.0
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `eq`
- **Types:** 2/2 matched
- **Missing types:** _none_

### 39. runtime.frozen_file_span

- **Target:** `runtime.FrozenFileSpan`
- **Similarity:** 0.86
- **Dependents:** 7
- **Priority Score:** 7001101.5
- **Functions:** 10/10 matched (target 11)
- **Missing functions:** _none_
- **Types:** 1/1 matched
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

- **Target:** `typecompiled.TypeMatcherFactory`
- **Similarity:** 0.69
- **Dependents:** 7
- **Priority Score:** 7000603.0
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_

### 43. runtime.small_duration

- **Target:** `runtime.SmallDuration`
- **Similarity:** 0.38
- **Dependents:** 6
- **Priority Score:** 6040906.0
- **Functions:** 4/7 matched (target 11)
- **Missing functions:** `from_millis`, `add_assign`, `add`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Output`
- **Tests:** 0/1 matched

### 44. typing.typecheck

- **Target:** `typing.Typecheck [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 6
- **Priority Score:** 6030705.0
- **Functions:** 2/5 matched
- **Missing functions:** `fmt`, `find_bindings_by_name`, `find_first_binding`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `typecheck.rs` vs expected `typing/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source typing/typecheck.rs` (current: `// port-lint: source typecheck.rs`)
- **Lint issues:** 1

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

- **Target:** `values.FreezeError`
- **Similarity:** 0.42
- **Dependents:** 6
- **Priority Score:** 6020806.0
- **Functions:** 3/4 matched (target 6)
- **Missing functions:** `from`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `FreezeResult`

### 47. dict.dict_type

- **Target:** `dict.DictType`
- **Similarity:** 0.66
- **Dependents:** 6
- **Priority Score:** 6020503.5
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `Canonical`, `Error`

### 48. layout.value_alloc_size

- **Target:** `layout.ValueAllocSize`
- **Similarity:** 0.43
- **Dependents:** 6
- **Priority Score:** 6010605.5
- **Functions:** 4/5 matched
- **Missing functions:** `layout`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 49. compiler.stmt

- **Target:** `compiler.Stmt`
- **Similarity:** 0.79
- **Dependents:** 6
- **Priority Score:** 6003202.0
- **Functions:** 25/25 matched (target 28)
- **Missing functions:** _none_
- **Types:** 7/7 matched (target 24)
- **Missing types:** _none_

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

### 52. typing.arc_ty

- **Target:** `typing.ArcTy [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 5
- **Priority Score:** 5021104.5
- **Functions:** 6/7 matched (target 13)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 10)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only by basename: `arc_ty.rs` vs expected `typing/arc_ty.rs`
- **Proposed provenance header:** `// port-lint: source typing/arc_ty.rs` (current: `// port-lint: source arc_ty.rs`)
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
- **Provenance warning:** port-lint provenance header matched only by basename: `interface.rs` vs expected `typing/interface.rs`
- **Proposed provenance header:** `// port-lint: source typing/interface.rs` (current: `// port-lint: source interface.rs`)
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

### 57. bc.frame

- **Target:** `bc.Frame [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 4
- **Priority Score:** 4082604.2
- **Functions:** 16/24 matched (target 34)
- **Missing functions:** `eq`, `null`, `is_inititalized`, `frame`, `frame_mut`, `offset_of_slots`, `locals_uninit`, `stack_uninit`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `starlarkSyntax/src/frame.rs` vs expected `eval/bc/frame.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/frame.rs` (current: `// port-lint: source starlarkSyntax/src/frame.rs`)
- **Lint issues:** 1

### 58. types.starlark_value_as_type

- **Target:** `types.StarlarkValueAsType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 4
- **Priority Score:** 4051704.8
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

### 59. values.value_of

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

### 60. profile.alloc_counts

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

### 61. values.demand

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
- **Similarity:** 0.81
- **Dependents:** 4
- **Priority Score:** 4000402.0
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/ellipsis.rs` vs expected `values/types/ellipsis.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/ellipsis.rs` vs expected `values/types/ellipsis.rs`
- **Proposed provenance header:** `// port-lint: source values/types/ellipsis.rs` (current: `// port-lint: source src/values/types/ellipsis.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/ellipsis.rs` (current: `// port-lint: tests src/values/types/ellipsis.rs`)
- **Lint issues:** 2

### 64. record.record_type

- **Target:** `record.RecordType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.57
- **Dependents:** 3
- **Priority Score:** 3083004.2
- **Functions:** 20/22 matched
- **Missing functions:** `fmt`, `r#type`
- **Types:** 2/8 matched (target 3)
- **Missing types:** `RecordCell`, `TyRecordDataOpt`, `RecordType`, `FrozenRecordType`, `Frozen`, `Canonical`
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/recordType.rs` vs expected `values/types/record/record_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/record_type.rs` vs expected `values/types/record/record_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/record_type.rs` (current: `// port-lint: source src/values/types/record/recordType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/record/record_type.rs` (current: `// port-lint: tests src/values/types/record/record_type.rs`)
- **Lint issues:** 3

### 65. alloc.chunk

- **Target:** `alloc.Chunk [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 3
- **Priority Score:** 3082205.8
- **Functions:** 12/19 matched (target 20)
- **Missing functions:** `fmt`, `begin`, `ptr_eq`, `drop`, `clone`, `counter_overflow`, `test_empty`
- **Types:** 2/3 matched
- **Missing types:** `ChunkDataEmpty`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chunk.rs` vs expected `values/layout/heap/allocator/alloc/chunk.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chunk.rs` vs expected `values/layout/heap/allocator/alloc/chunk.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chunk.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chunk.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chunk.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chunk.rs`)
- **Lint issues:** 2

### 66. list.alloc

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

### 67. list.list_type

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

### 68. stdlib.call_stack

- **Target:** `stdlib.CallStack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 3
- **Priority Score:** 3021404.0
- **Functions:** 11/13 matched (target 17)
- **Missing functions:** `fmt`, `global`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/callStack.rs` vs expected `stdlib/call_stack.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `../starlarkSyntax/src/callStack.rs` vs expected `stdlib/call_stack.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/callStack.rs` vs expected `stdlib/call_stack.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/call_stack.rs` (current: `// port-lint: source src/stdlib/callStack.rs`)
- **Proposed provenance header:** `// port-lint: source stdlib/call_stack.rs` (current: `// port-lint: source ../starlarkSyntax/src/callStack.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/call_stack.rs` (current: `// port-lint: tests src/stdlib/callStack.rs`)
- **Lint issues:** 3

### 69. profile.instant

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

### 70. values.unpack_and_discard

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

### 71. errors.did_you_mean

- **Target:** `errors.DidYouMean [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 3
- **Priority Score:** 3000604.5
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/errors/didYouMean.rs` vs expected `errors/did_you_mean.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/errors/didYouMean.rs` vs expected `errors/did_you_mean.rs`
- **Proposed provenance header:** `// port-lint: source errors/did_you_mean.rs` (current: `// port-lint: source src/errors/didYouMean.rs`)
- **Proposed provenance header:** `// port-lint: tests errors/did_you_mean.rs` (current: `// port-lint: tests src/errors/didYouMean.rs`)
- **Lint issues:** 2

### 72. compiler.constants

- **Target:** `compiler.Constants [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 3
- **Priority Score:** 3000505.5
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/constants.rs` vs expected `eval/compiler/constants.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/compiler/constants.rs` vs expected `eval/compiler/constants.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/constants.rs` (current: `// port-lint: source src/eval/compiler/constants.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/compiler/constants.rs` (current: `// port-lint: tests src/eval/compiler/constants.rs`)
- **Lint issues:** 2

### 73. sealed

- **Target:** `starlark.Sealed [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 3
- **Priority Score:** 3000100.0
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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/arc_or_static.rs` vs expected `util/arc_or_static.rs`
- **Proposed provenance header:** `// port-lint: source util/arc_or_static.rs` (current: `// port-lint: source src/util/arc_or_static.rs`)
- **Lint issues:** 1

### 75. typing.type_type

- **Target:** `typing.TypeType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 2
- **Priority Score:** 2040803.4
- **Functions:** 3/5 matched (target 6)
- **Missing functions:** `module`, `takes_type`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeType.rs` vs expected `values/typing/type_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/type_type.rs` vs expected `values/typing/type_type.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_type.rs` (current: `// port-lint: source src/values/typing/typeType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/type_type.rs` (current: `// port-lint: tests src/values/typing/type_type.rs`)
- **Lint issues:** 2

### 76. alloc.chunk_part

- **Target:** `alloc.ChunkPart [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 2
- **Priority Score:** 2031602.0
- **Functions:** 12/15 matched (target 17)
- **Missing functions:** `chunk_ptr_eq`, `test_split_at`, `test_split_at_zero`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chunkPart.rs` vs expected `values/layout/heap/allocator/alloc/chunk_part.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chunk_part.rs` vs expected `values/layout/heap/allocator/alloc/chunk_part.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chunk_part.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chunkPart.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chunk_part.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chunk_part.rs`)
- **Lint issues:** 2

### 77. compiler.small_vec_1

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

### 78. layout.const_type_id

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

### 79. avalues.str_

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

### 80. runtime.rust_loc

- **Target:** `runtime.RustLoc [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 2
- **Priority Score:** 2020302.0
- **Functions:** 1/3 matched (target 4)
- **Missing functions:** `rust_loc_globals`, `invoke`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/rustLoc.rs` vs expected `eval/runtime/rust_loc.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/rust_loc.rs` vs expected `eval/runtime/rust_loc.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/rust_loc.rs` (current: `// port-lint: source src/eval/runtime/rustLoc.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/rust_loc.rs` (current: `// port-lint: tests src/eval/runtime/rust_loc.rs`)
- **Lint issues:** 2

### 81. values.owned_frozen_ref

- **Target:** `values.OwnedFrozenRef [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 2
- **Priority Score:** 2011502.9
- **Functions:** 12/12 matched (target 19)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/ownedFrozenRef.rs` vs expected `values/owned_frozen_ref.rs`
- **Proposed provenance header:** `// port-lint: source values/owned_frozen_ref.rs` (current: `// port-lint: source src/values/ownedFrozenRef.rs`)
- **Lint issues:** 1

### 82. values.stack_guard

- **Target:** `values.StackGuard [PROVENANCE-FALLBACK]`
- **Similarity:** 0.56
- **Dependents:** 2
- **Priority Score:** 2010504.4
- **Functions:** 3/4 matched
- **Missing functions:** `drop`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/stackGuard.rs` vs expected `values/stack_guard.rs`
- **Proposed provenance header:** `// port-lint: source values/stack_guard.rs` (current: `// port-lint: source src/values/stackGuard.rs`)
- **Lint issues:** 1

### 83. collections.aligned_padded_str

- **Target:** `collections.AlignedPaddedStr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.35
- **Dependents:** 2
- **Priority Score:** 2010406.5
- **Functions:** 2/3 matched (target 4)
- **Missing functions:** `eq`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/alignedPaddedStr.rs` vs expected `collections/aligned_padded_str.rs`
- **Proposed provenance header:** `// port-lint: source collections/aligned_padded_str.rs` (current: `// port-lint: source src/collections/alignedPaddedStr.rs`)
- **Lint issues:** 1

### 84. profile.string_index

- **Target:** `profile.StringIndex [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 2
- **Priority Score:** 2000403.1
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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/file_loader.rs` vs expected `eval/runtime/file_loader.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/file_loader.rs` (current: `// port-lint: source src/eval/runtime/file_loader.rs`)
- **Lint issues:** 1

### 86. collections.string_pool

- **Target:** `collections.StringPool [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 2
- **Priority Score:** 2000305.9
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/string_pool.rs` vs expected `collections/string_pool.rs`
- **Proposed provenance header:** `// port-lint: source collections/string_pool.rs` (current: `// port-lint: source src/collections/string_pool.rs`)
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

### 89. types.list

- **Target:** `tests.List [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2000010.0
- **Functions:** 0/0 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/list.rs` vs expected `values/types/list.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list.rs` (current: `// port-lint: source tests/list.rs`)
- **Lint issues:** 1

### 90. heap.arena

- **Target:** `heap.Arena [PROVENANCE-FALLBACK]`
- **Similarity:** 0.46
- **Dependents:** 1
- **Priority Score:** 1124405.4
- **Functions:** 26/37 matched (target 33)
- **Missing functions:** `max`, `drop`, `visit`, `visit_bump`, `to_repr`, `mk_str`, `reserve_str`, `test_trait_arena_iteration`, `drop_with_blackhole`, `test_allocated_summary`, `test_is_empty`
- **Types:** 6/7 matched (target 8)
- **Missing types:** `Item`
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/arena.rs` vs expected `values/layout/heap/arena.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/arena.rs` (current: `// port-lint: source src/values/layout/heap/arena.rs`)
- **Lint issues:** 1

### 91. stdlib

- **Target:** `starlark.Stdlib [PROVENANCE-FALLBACK]`
- **Similarity:** 0.39
- **Dependents:** 1
- **Priority Score:** 1111706.1
- **Functions:** 5/14 matched (target 11)
- **Missing functions:** `global`, `nop`, `get_methods`, `equals`, `unpack_value_impl`, `globals`, `methods`, `invert1`, `invert2`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Bool2`, `Error`
- **Tests:** 2/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib.rs` vs expected `stdlib.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib.rs` vs expected `stdlib.rs`
- **Proposed provenance header:** `// port-lint: source stdlib.rs` (current: `// port-lint: source src/stdlib.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib.rs` (current: `// port-lint: tests src/stdlib.rs`)
- **Lint issues:** 2

### 92. types.list_or_tuple

- **Target:** `types.ListOrTuple`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1061008.0
- **Functions:** 3/5 matched (target 6)
- **Missing functions:** `into_iter`, `test_unpack`
- **Types:** 1/5 matched (target 2)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 0/1 matched

### 93. layout.pointer

- **Target:** `layout.Pointer [PROVENANCE-FALLBACK]`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1043705.0
- **Functions:** 28/32 matched (target 49)
- **Missing functions:** `fmt`, `from_usize_unchecked`, `to_usize`, `unpack`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/pointer.rs` vs expected `values/layout/pointer.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/pointer.rs` vs expected `values/layout/pointer.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/pointer.rs` (current: `// port-lint: source src/values/layout/pointer.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/pointer.rs` (current: `// port-lint: tests src/values/layout/pointer.rs`)
- **Lint issues:** 2

### 94. stdlib.breakpoint

- **Target:** `stdlib.Breakpoint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 1
- **Priority Score:** 1042303.8
- **Functions:** 14/17 matched (target 19)
- **Missing functions:** `global`, `breakpoint`, `reset_global_state`
- **Types:** 5/6 matched
- **Missing types:** `Handler`
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/breakpoint.rs` vs expected `stdlib/breakpoint.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/breakpoint.rs` vs expected `stdlib/breakpoint.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/breakpoint.rs` (current: `// port-lint: source src/stdlib/breakpoint.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/breakpoint.rs` (current: `// port-lint: tests src/stdlib/breakpoint.rs`)
- **Lint issues:** 2

### 95. types.any_complex

- **Target:** `types.AnyComplex [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1041210.0
- **Functions:** 5/7 matched (target 8)
- **Missing functions:** `fmt`, `freeze`
- **Types:** 3/5 matched (target 4)
- **Missing types:** `Canonical`, `Frozen`
- **Tests:** 1/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/any_complex.rs` vs expected `values/types/any_complex.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/any_complex.rs` vs expected `values/types/any_complex.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any_complex.rs` (current: `// port-lint: source src/values/types/any_complex.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/any_complex.rs` (current: `// port-lint: tests src/values/types/any_complex.rs`)
- **Lint issues:** 2

### 96. types.any_array

- **Target:** `types.AnyArray [PROVENANCE-FALLBACK]`
- **Similarity:** 0.39
- **Dependents:** 1
- **Priority Score:** 1031006.1
- **Functions:** 5/7 matched (target 9)
- **Missing functions:** `fmt`, `drop`
- **Types:** 2/3 matched
- **Missing types:** `Canonical`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/any_array.rs` vs expected `values/types/any_array.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/any_array.rs` vs expected `values/types/any_array.rs`
- **Proposed provenance header:** `// port-lint: source values/types/any_array.rs` (current: `// port-lint: source src/values/types/any_array.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/any_array.rs` (current: `// port-lint: tests src/values/types/any_array.rs`)
- **Lint issues:** 2

### 97. bc.if_debug

- **Target:** `bc.IfDebug [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 1
- **Priority Score:** 1030906.0
- **Functions:** 5/8 matched (target 9)
- **Missing functions:** `eq`, `partial_cmp`, `cmp`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/ifDebug.rs` vs expected `eval/bc/if_debug.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/if_debug.rs` (current: `// port-lint: source src/eval/bc/ifDebug.rs`)
- **Lint issues:** 1

### 98. util.rtabort

- **Target:** `util.Rtabort [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 1
- **Priority Score:** 1030704.9
- **Functions:** 3/6 matched
- **Missing functions:** `drop`, `_test_compiles_fixed_string`, `_test_compiles_with_format_args`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/rtabort.rs` vs expected `util/rtabort.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/util/rtabort.rs` vs expected `util/rtabort.rs`
- **Proposed provenance header:** `// port-lint: source util/rtabort.rs` (current: `// port-lint: source src/util/rtabort.rs`)
- **Proposed provenance header:** `// port-lint: tests util/rtabort.rs` (current: `// port-lint: tests src/util/rtabort.rs`)
- **Lint issues:** 2

### 99. runtime.cheap_call_stack

- **Target:** `runtime.CheapCallStack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 1
- **Priority Score:** 1022002.7
- **Functions:** 15/17 matched
- **Missing functions:** `fmt`, `default`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/cheapCallStack.rs` vs expected `eval/runtime/cheap_call_stack.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/cheap_call_stack.rs` (current: `// port-lint: source src/eval/runtime/cheapCallStack.rs`)
- **Lint issues:** 1

### 100. string.dot_format

- **Target:** `string.DotFormat [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 1
- **Priority Score:** 1021202.9
- **Functions:** 9/11 matched (target 10)
- **Missing functions:** `new`, `format_capture_for_test`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 3/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/dotFormat.rs` vs expected `values/types/string/dot_format.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/dot_format.rs` vs expected `values/types/string/dot_format.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/dot_format.rs` (current: `// port-lint: source src/values/types/string/dotFormat.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/string/dot_format.rs` (current: `// port-lint: tests src/values/types/string/dot_format.rs`)
- **Lint issues:** 2

### 101. avalues.simple

- **Target:** `avalues.Simple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 1
- **Priority Score:** 1021102.9
- **Functions:** 8/8 matched (target 10)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 1)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/simple.rs` vs expected `values/layout/avalues/simple.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/simple.rs` (current: `// port-lint: source src/values/layout/avalues/simple.rs`)
- **Lint issues:** 1

### 102. layout.value_captured

- **Target:** `layout.ValueCaptured [PROVENANCE-FALLBACK]`
- **Similarity:** 0.80
- **Dependents:** 1
- **Priority Score:** 1020802.0
- **Functions:** 4/4 matched (target 9)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `Canonical`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/valueCaptured.rs` vs expected `values/layout/value_captured.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/value_captured.rs` (current: `// port-lint: source src/values/layout/valueCaptured.rs`)
- **Lint issues:** 1

### 103. record.field

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

### 104. util.non_static_type_id

- **Target:** `util.NonStaticTypeId [PROVENANCE-FALLBACK]`
- **Similarity:** 0.22
- **Dependents:** 1
- **Priority Score:** 1020407.8
- **Functions:** 2/3 matched (target 2)
- **Missing functions:** `get_type_id`
- **Types:** 0/1 matched
- **Missing types:** `NonStaticAny`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util/nonStaticTypeId.rs` vs expected `util/non_static_type_id.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/util/nonStaticTypeId.rs` vs expected `util/non_static_type_id.rs`
- **Proposed provenance header:** `// port-lint: source util/non_static_type_id.rs` (current: `// port-lint: source src/util/nonStaticTypeId.rs`)
- **Proposed provenance header:** `// port-lint: tests util/non_static_type_id.rs` (current: `// port-lint: tests src/util/nonStaticTypeId.rs`)
- **Lint issues:** 2

### 105. collections.alloca

- **Target:** `collections.Alloca [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 1
- **Priority Score:** 1012604.5
- **Functions:** 21/22 matched (target 32)
- **Missing functions:** `len_in_to_to_len_in_words`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/alloca.rs` vs expected `collections/alloca.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/collections/alloca.rs` vs expected `collections/alloca.rs`
- **Proposed provenance header:** `// port-lint: source collections/alloca.rs` (current: `// port-lint: source src/collections/alloca.rs`)
- **Proposed provenance header:** `// port-lint: tests collections/alloca.rs` (current: `// port-lint: tests src/collections/alloca.rs`)
- **Lint issues:** 5

### 106. typing.bindings

- **Target:** `typing.Bindings [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 1
- **Priority Score:** 1011103.1
- **Functions:** 7/8 matched (target 18)
- **Missing functions:** `get_for_clause`
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `bindings.rs` vs expected `typing/bindings.rs`
- **Proposed provenance header:** `// port-lint: source typing/bindings.rs` (current: `// port-lint: source bindings.rs`)
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
- **Provenance warning:** port-lint provenance header matched only by basename: `structs.rs` vs expected `typing/structs.rs`
- **Proposed provenance header:** `// port-lint: source typing/structs.rs` (current: `// port-lint: source structs.rs`)
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

- **Target:** `starlark.ReadLine [PROVENANCE-FALLBACK]`
- **Similarity:** 0.28
- **Dependents:** 1
- **Priority Score:** 1010407.2
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

### 111. string.interpolation

- **Target:** `string.Interpolation [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 1
- **Priority Score:** 1001601.2
- **Functions:** 12/12 matched (target 14)
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 21)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/interpolation.rs` vs expected `values/types/string/interpolation.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/interpolation.rs` vs expected `values/types/string/interpolation.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/interpolation.rs` (current: `// port-lint: source src/values/types/string/interpolation.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/string/interpolation.rs` (current: `// port-lint: tests src/values/types/string/interpolation.rs`)
- **Lint issues:** 2

### 112. typing.function

- **Target:** `typing.Function [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 1
- **Priority Score:** 1001502.6
- **Functions:** 12/12 matched (target 24)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `function.rs` vs expected `typing/function.rs`
- **Proposed provenance header:** `// port-lint: source typing/function.rs` (current: `// port-lint: source function.rs`)
- **Lint issues:** 6

### 113. heap.fast_cell

- **Target:** `heap.FastCell [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 1
- **Priority Score:** 1000804.1
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/fastCell.rs` vs expected `values/layout/heap/fast_cell.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/fast_cell.rs` (current: `// port-lint: source src/values/layout/heap/fastCell.rs`)
- **Lint issues:** 1

### 114. analysis.lint_message

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

### 115. tests

- **Target:** `typing.Tests [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000010.0
- **Functions:** 0/0 matched (target 27)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_

### 116. bc.instr_impl

- **Target:** `bc.InstrImpl [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 777001.7
- **Functions:** 7/7 matched (target 95)
- **Missing functions:** _none_
- **Types:** 87/163 matched (target 103)
- **Missing types:** `Arg`, `InstrConst`, `InstrLoadLocal`, `InstrLoadLocalCaptured`, `InstrLoadModule`, `InstrMov`, `InstrStoreLocalCaptured`, `InstrStoreModule`, `InstrStoreModuleAndExport`, `InstrUnpack`, `InstrArrayIndex`, `InstrSetArrayIndex`, `InstrArrayIndexSet`, `InstrObjectField`, `InstrSetObjectField`, `InstrSlice`, `InstrArrayIndex2`, `InstrEq`, `InstrEqConst`, `InstrEqPtr`, `InstrEqStr`, `InstrEqInt`, `InstrNot`, `InstrMinus`, `InstrPlus`, `InstrBitNot`, `InstrBinOp`, `InstrUnOp`, `InstrAdd`, `InstrAddAssign`, `InstrSub`, `InstrMultiply`, `InstrPercent`, `InstrDivide`, `InstrFloorDivide`, `InstrBitAnd`, `InstrBitOr`, `InstrBitOrAssign`, `InstrBitXor`, `InstrLeftShift`, `InstrRightShift`, `InstrIn`, `InstrPercentSOne`, `InstrFormatOne`, `InstrCompare`, `InstrLess`, `InstrGreater`, `InstrLessOrEqual`, `InstrGreaterOrEqual`, `InstrType`, `InstrTypeIs`, `InstrIsInstance`, `InstrLen`, `InstrTupleNPop`, `InstrListNew`, `InstrListNPop`, `InstrListOfConsts`, `InstrDictNew`, `InstrDictOfConsts`, `InstrDictConstKeys`, `InstrDictNPop`, `InstrCheckType`, `InstrDef`, `InstrCall`, `InstrCallPos`, `InstrCallFrozenDef`, `InstrCallFrozenDefPos`, `InstrCallFrozenNative`, `InstrCallFrozenNativePos`, `InstrCallFrozen`, `InstrCallFrozenPos`, `InstrCallMethod`, `InstrCallMethodPos`, `InstrCallMaybeKnownMethod`, `InstrCallMaybeKnownMethodPos`, `InstrPossibleGc`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrImpl.rs` vs expected `eval/bc/instr_impl.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr_impl.rs` (current: `// port-lint: source src/eval/bc/instrImpl.rs`)
- **Lint issues:** 20

### 117. adapter.tests

- **Target:** `tests.Tests [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 222503.4
- **Functions:** 0/22 matched (target 21)
- **Missing functions:** `new`, `event_stopped`, `get_client`, `eval_stopped`, `wait_for_eval_stopped`, `drop`, `breakpoint`, `breakpoints_args`, `eval_with_hook`, `join_timeout`, `dap_test_template`, `test_breakpoint`, `test_breakpoint_with_failing_condition`, `test_breakpoint_with_passing_condition`, `test_step_over`, `test_step_into`, `test_step_out`, `test_local_variables`, `test_inspect_variables`, `test_evaluate_expression`, `assert_variable`, `test_truncate_string`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 0/22 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter/tests.rs` vs expected `debug/adapter/tests.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter/tests.rs` (current: `// port-lint: source src/debug/adapter/tests.rs`)
- **Lint issues:** 1

### 118. values.typing.callable

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.values.typing.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 204004.1
- **Functions:** 15/32 matched (target 50)
- **Missing functions:** `clone`, `fmt`, `trace`, `_assert`, `test_callable_runtime`, `test_callable_pass_compile_time`, `test_callable_fail_compile_time`, `my_module`, `accept_f`, `test_native_callable_fail_compile_time_wrong_param_type`, `test_native_callable_fail_compile_time_wrong_param_count`, `test_typing_callable_pass`, `test_typing_callable_fail_compile_time_wrong_param_type`, `test_typing_callable_fail_compile_time_wrong_param_count`, `module`, `good`, `bad`
- **Types:** 5/8 matched (target 6)
- **Missing types:** `Canonical`, `Error`, `Frozen`
- **Tests:** 2/15 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/callable.rs` vs expected `values/typing/callable.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/callable.rs` vs expected `values/typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/callable.rs` (current: `// port-lint: source src/values/typing/callable.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/callable.rs` (current: `// port-lint: tests src/values/typing/callable.rs`)
- **Lint issues:** 2

### 119. tests.call

- **Target:** `commonTest.kotlin.io.github.kotlinmania.starlark.typing.tests.Call`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 181810.0
- **Functions:** 0/18 matched (target 9)
- **Missing functions:** `funcall_test`, `f`, `funcall_extra_args_def`, `test_repeated_parameters`, `test_bad_application`, `test_extra_args_native`, `test_insufficient_args_native`, `test_parameter_defaults`, `test_parameter_defaults_frozen`, `test_arguments`, `test_argument_evaluation_order`, `test_empty_args_kwargs`, `test_non_optional_after_optional`, `test_pos_only_pass`, `test_pos_only_fail`, `test_frame_size`, `natives`, `stack_ptr`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 0/15 matched

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/intOrBig.rs` vs expected `values/types/int/int_or_big.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/int/intOrBig.rs` vs expected `values/types/int/int_or_big.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/int_or_big.rs` (current: `// port-lint: source src/values/types/int/intOrBig.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/int/int_or_big.rs` (current: `// port-lint: tests src/values/types/int/intOrBig.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only by basename: `user.rs` vs expected `typing/user.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/typing/user.rs` vs expected `typing/user.rs`
- **Proposed provenance header:** `// port-lint: source typing/user.rs` (current: `// port-lint: source user.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/user.rs` (current: `// port-lint: tests src/typing/user.rs`)
- **Lint issues:** 2

### 122. scope.payload

- **Target:** `scope.Payload [PROVENANCE-FALLBACK]`
- **Similarity:** 0.50
- **Dependents:** 0
- **Priority Score:** 152405.0
- **Functions:** 6/7 matched (target 21)
- **Missing functions:** `from_ast`
- **Types:** 3/17 matched (target 3)
- **Missing types:** `LoadPayload`, `IdentPayload`, `IdentAssignPayload`, `DefPayload`, `TypeExprPayload`, `CstStmtFromAst`, `CstAssignIdentExt`, `CstExpr`, `CstTypeExpr`, `CstAssignTarget`, `CstAssignIdent`, `CstIdent`, `CstParameter`, `CstStmt`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope/payload.rs` vs expected `eval/compiler/scope/payload.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope/payload.rs` (current: `// port-lint: source src/eval/compiler/scope/payload.rs`)
- **Lint issues:** 4

### 123. list.value

- **Target:** `list.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 136404.2
- **Functions:** 46/56 matched (target 99)
- **Missing functions:** `fmt`, `error`, `starlark_type_repr`, `test_to_str`, `test_repr_cycle`, `test_mutate_list`, `test_arithmetic_on_list`, `test_value_alias`, `test_mutating_imports`, `test_compare`
- **Types:** 5/8 matched
- **Missing types:** `FrozenList`, `List`, `Canonical`
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/value.rs` vs expected `values/types/list/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/value.rs` (current: `// port-lint: source src/values/types/list/value.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/typed.rs` vs expected `values/layout/typed.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/typed.rs` vs expected `values/layout/typed.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/typed.rs` (current: `// port-lint: source src/values/layout/typed.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/typed.rs` (current: `// port-lint: tests src/values/layout/typed.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/strType.rs` vs expected `values/types/string/str_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/str_type.rs` vs expected `values/types/string/str_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/str_type.rs` (current: `// port-lint: source src/values/types/string/strType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/string/str_type.rs` (current: `// port-lint: tests src/values/types/string/str_type.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/float.rs` vs expected `values/types/float/float.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/float/float.rs` vs expected `values/types/float/float.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/float.rs` (current: `// port-lint: source src/values/types/float/float.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/float/float.rs` (current: `// port-lint: tests src/values/types/float/float.rs`)
- **Lint issues:** 2

### 127. dict.value

- **Target:** `dict.Value [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 106203.7
- **Functions:** 47/52 matched (target 70)
- **Missing functions:** `fmt`, `hash`, `get_type_value_static`, `_assert_coerce`, `dict_methods`
- **Types:** 5/10 matched (target 9)
- **Missing types:** `Canonical`, `FrozenDict`, `MutableDict`, `Frozen`, `ContentRef`
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/value.rs` vs expected `values/types/dict/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/dict/value.rs` vs expected `values/types/dict/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/value.rs` (current: `// port-lint: source src/values/types/dict/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/dict/value.rs` (current: `// port-lint: tests src/values/types/dict/value.rs`)
- **Lint issues:** 2

### 128. tests.markdown

- **Target:** `tests.Markdown [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 102910.0
- **Functions:** 17/27 matched (target 32)
- **Missing functions:** `module`, `object`, `golden_docs_starlark`, `native_docs_module`, `linked_ty_mapper`, `globals_render_default`, `globals_render_default_with_linked_type`, `globals_render_signature_at_bottom`, `globals_render_signature_at_bottom_with_linked_type`, `golden_docs_object`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/tests/markdown.rs` vs expected `docs/tests/markdown.rs`
- **Proposed provenance header:** `// port-lint: source docs/tests/markdown.rs` (current: `// port-lint: source src/docs/tests/markdown.rs`)
- **Lint issues:** 1

### 129. typing.small_arc_vec_or_static

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `pagable/vtableRegistry.rs` vs expected `pagable/vtable_registry.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/pagable/vtableRegistry.rs` vs expected `pagable/vtable_registry.rs`
- **Proposed provenance header:** `// port-lint: source pagable/vtable_registry.rs` (current: `// port-lint: source pagable/vtableRegistry.rs`)
- **Proposed provenance header:** `// port-lint: tests pagable/vtable_registry.rs` (current: `// port-lint: tests src/pagable/vtableRegistry.rs`)
- **Lint issues:** 2

### 131. heap.send

- **Target:** `heap.Send [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 91110.0
- **Functions:** 0/5 matched (target 0)
- **Missing functions:** `new`, `into_inner`, `deref`, `deref_mut`, `fmt`
- **Types:** 2/6 matched (target 2)
- **Missing types:** `Sealed`, `DynStarlark`, `Target`, `StaticType`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/send.rs` vs expected `values/layout/heap/send.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/send.rs` (current: `// port-lint: source src/values/layout/heap/send.rs`)
- **Lint issues:** 1

### 132. typing.tests.call

- **Target:** `tests.Call [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 90910.0
- **Functions:** 0/9 matched (target 20)
- **Missing functions:** `test_type_kwargs`, `test_types_of_args_kwargs`, `test_kwargs_in_native_code`, `test_call_callable`, `test_call_not_callable`, `test_call_callable_or_not_callable`, `test_calls`, `test_never_call_bug`, `test_call_pos_only`
- **Types:** 0/0 matched (target 2)
- **Missing types:** _none_
- **Tests:** 0/9 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `src/tests/call.rs` vs expected `typing/tests/call.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/call.rs` (current: `// port-lint: source src/tests/call.rs`)
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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/thinBox.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/thinBox.rs` vs expected `values/thin_box_slice_frozen_value/thin_box.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/thinBox.rs`)
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/thin_box.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/thinBox.rs`)
- **Lint issues:** 2

### 134. bc.instrs

- **Target:** `bc.Instrs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 82805.9
- **Functions:** 17/24 matched (target 27)
- **Missing functions:** `drop_in_place`, `handle`, `drop_instrs`, `drop`, `opcodes`, `fmt`, `display`
- **Types:** 3/4 matched (target 3)
- **Missing types:** `HandlerImpl`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrs.rs` vs expected `eval/bc/instrs.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instrs.rs` (current: `// port-lint: source src/eval/bc/instrs.rs`)
- **Lint issues:** 1

### 135. alloc.allocator

- **Target:** `alloc.Allocator [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 82106.0
- **Functions:** 11/18 matched (target 15)
- **Missing functions:** `fmt`, `default`, `drop`, `test_small`, `test_big`, `random_iteration`, `test_many`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Item`
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/allocator.rs` vs expected `values/layout/heap/allocator/alloc/allocator.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/allocator.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/allocator.rs`)
- **Lint issues:** 1

### 136. typing.small_arc_vec

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

### 137. tests.uncategorized

- **Target:** `tests.Uncategorized [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 75802.4
- **Functions:** 49/52 matched (target 61)
- **Missing functions:** `unpack_value_impl`, `module`, `freeze`
- **Types:** 2/6 matched (target 3)
- **Missing types:** `Error`, `FrozenWrapper`, `Canonical`, `Frozen`
- **Tests:** 36/36 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/uncategorized.rs` vs expected `tests/uncategorized.rs`
- **Proposed provenance header:** `// port-lint: source tests/uncategorized.rs` (current: `// port-lint: source src/tests/uncategorized.rs`)
- **Lint issues:** 2

### 138. type_compiled.compiled

- **Target:** `typecompiled.Compiled [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 74603.0
- **Functions:** 34/39 matched (target 49)
- **Missing functions:** `fmt`, `starlark_type_repr`, `alloc_value`, `hash`, `eq`
- **Types:** 5/7 matched (target 12)
- **Missing types:** `StaticType`, `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/compiled.rs` vs expected `values/typing/type_compiled/compiled.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/compiled.rs` (current: `// port-lint: source src/values/typing/typeCompiled/compiled.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/num/value.rs` vs expected `values/types/num/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/num/value.rs` vs expected `values/types/num/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/num/value.rs` (current: `// port-lint: source src/values/types/num/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/num/value.rs` (current: `// port-lint: tests src/values/types/num/value.rs`)
- **Lint issues:** 2

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

- **Target:** `compiler.Scope [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 67102.3
- **Functions:** 48/51 matched (target 65)
- **Missing functions:** `from`, `assign_ident_impl`, `new`
- **Types:** 17/20 matched (target 28)
- **Missing types:** `StmtCollectDefines`, `AssignIdentCollect`, `AssignTargetCollectDefinesLvalue`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/scope.rs` vs expected `eval/compiler/scope.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/scope.rs` (current: `// port-lint: source src/eval/compiler/scope.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/value.rs` vs expected `values/types/set/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/value.rs` vs expected `values/types/set/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/value.rs` (current: `// port-lint: source src/values/types/set/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/set/value.rs` (current: `// port-lint: tests src/values/types/set/value.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/heapType.rs` vs expected `values/layout/heap/heap_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/heap_type.rs` vs expected `values/layout/heap/heap_type.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/heap_type.rs` (current: `// port-lint: source src/values/layout/heap/heapType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/heap_type.rs` (current: `// port-lint: tests src/values/layout/heap/heap_type.rs`)
- **Lint issues:** 3

### 144. assert.assert

- **Target:** `assert.Assert [PROVENANCE-FALLBACK]`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 65201.8
- **Functions:** 44/50 matched (target 72)
- **Missing functions:** `r#true`, `new`, `fail_golden`, `fail_skip_typecheck`, `fails_skip_typecheck`, `is_true_skip_typecheck`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/assert/assert.rs` vs expected `assert/assert.rs`
- **Proposed provenance header:** `// port-lint: source assert/assert.rs` (current: `// port-lint: source src/assert/assert.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/names.rs` vs expected `analysis/names.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/names.rs` vs expected `analysis/names.rs`
- **Proposed provenance header:** `// port-lint: source analysis/names.rs` (current: `// port-lint: source src/analysis/names.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/names.rs` (current: `// port-lint: tests src/analysis/names.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/value.rs` vs expected `values/types/tuple/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/value.rs` vs expected `values/types/tuple/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/value.rs` (current: `// port-lint: source src/values/types/tuple/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/value.rs` (current: `// port-lint: tests src/values/types/tuple/value.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/bc.rs` vs expected `eval/runtime/profile/bc.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/bc.rs` vs expected `eval/runtime/profile/bc.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/bc.rs` (current: `// port-lint: source src/eval/runtime/profile/bc.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/bc.rs` (current: `// port-lint: tests src/eval/runtime/profile/bc.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/packedImpl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/thinBoxSliceFrozenValue/packedImpl.rs` vs expected `values/thin_box_slice_frozen_value/packed_impl.rs`
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/packedImpl.rs`)
- **Proposed provenance header:** `// port-lint: source values/thin_box_slice_frozen_value/packed_impl.rs` (current: `// port-lint: source src/values/thinBoxSliceFrozenValue/packedImpl.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/typed/string.rs` vs expected `values/layout/typed/string.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/typed/string.rs` vs expected `values/layout/typed/string.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/typed/string.rs` (current: `// port-lint: source src/values/layout/typed/string.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/typed/string.rs` (current: `// port-lint: tests src/values/layout/typed/string.rs`)
- **Lint issues:** 3

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/complex.rs` vs expected `values/layout/complex.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/complex.rs` vs expected `values/layout/complex.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/complex.rs` (current: `// port-lint: source src/values/layout/complex.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/complex.rs` (current: `// port-lint: tests src/values/layout/complex.rs`)
- **Lint issues:** 2

### 151. analysis.types

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/bigint/convert.rs` vs expected `values/types/bigint/convert.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/bigint/convert.rs` vs expected `values/types/bigint/convert.rs`
- **Proposed provenance header:** `// port-lint: source values/types/bigint/convert.rs` (current: `// port-lint: source src/values/types/bigint/convert.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/bigint/convert.rs` (current: `// port-lint: tests src/values/types/bigint/convert.rs`)
- **Lint issues:** 2

### 153. tuple.rust_tuple

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

- **Target:** `values.Owned [PROVENANCE-FALLBACK]`
- **Similarity:** 0.77
- **Dependents:** 0
- **Priority Score:** 53402.3
- **Functions:** 26/29 matched (target 32)
- **Missing functions:** `fmt`, `downcast_starlark`, `deref`
- **Types:** 3/5 matched
- **Missing types:** `Canonical`, `Target`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/owned.rs` vs expected `values/owned.rs`
- **Proposed provenance header:** `// port-lint: source values/owned.rs` (current: `// port-lint: source src/values/owned.rs`)
- **Lint issues:** 1

### 156. profile.stmt

- **Target:** `profile.Stmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 52603.2
- **Functions:** 13/17 matched (target 23)
- **Missing functions:** `r#gen`, `test_coverage`, `test_empty`, `test_merge`
- **Types:** 8/9 matched
- **Missing types:** `Data`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/stmt.rs` vs expected `eval/runtime/profile/stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/stmt.rs` (current: `// port-lint: source src/eval/runtime/profile/stmt.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/partial.rs` vs expected `stdlib/partial.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/partial.rs` vs expected `stdlib/partial.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/partial.rs` (current: `// port-lint: source src/stdlib/partial.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/partial.rs` (current: `// port-lint: tests src/stdlib/partial.rs`)
- **Lint issues:** 3

### 158. namespace.value

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

### 159. values.unpack

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

### 160. dict.refs

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

### 161. list.unpack

- **Target:** `list.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.35
- **Dependents:** 0
- **Priority Score:** 51006.5
- **Functions:** 4/5 matched (target 10)
- **Missing functions:** `into_iter`
- **Types:** 1/5 matched
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/unpack.rs` vs expected `values/types/list/unpack.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/unpack.rs` vs expected `values/types/list/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/unpack.rs` (current: `// port-lint: source src/values/types/list/unpack.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/list/unpack.rs` (current: `// port-lint: tests src/values/types/list/unpack.rs`)
- **Lint issues:** 2

### 162. tuple.unpack

- **Target:** `tuple.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 51005.6
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `into_iter`
- **Types:** 1/5 matched (target 4)
- **Missing types:** `Canonical`, `Error`, `Item`, `IntoIter`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/unpack.rs` vs expected `values/types/tuple/unpack.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/unpack.rs` vs expected `values/types/tuple/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/unpack.rs` (current: `// port-lint: source src/values/types/tuple/unpack.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/unpack.rs` (current: `// port-lint: tests src/values/types/tuple/unpack.rs`)
- **Lint issues:** 2

### 163. typing.iter

- **Target:** `typing.Iter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.88
- **Dependents:** 0
- **Priority Score:** 51001.2
- **Functions:** 3/6 matched (target 8)
- **Missing functions:** `test_iterable_runtime`, `test_iterable_compile_time_pass`, `test_iterable_compile_time_fail`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `NonInstantiable`, `Canonical`
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/iter.rs` vs expected `values/typing/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/iter.rs` (current: `// port-lint: source src/values/typing/iter.rs`)
- **Lint issues:** 2

### 164. compiler.def

- **Target:** `compiler.Def [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 45203.1
- **Functions:** 38/39 matched (target 46)
- **Missing functions:** `fmt`
- **Types:** 10/13 matched (target 15)
- **Missing types:** `Def`, `FrozenDef`, `Frozen`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/def.rs` vs expected `eval/compiler/def.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def.rs` (current: `// port-lint: source src/eval/compiler/def.rs`)
- **Lint issues:** 4

### 165. params.spec

- **Target:** `params.Spec [PROVENANCE-FALLBACK]`
- **Similarity:** 0.64
- **Dependents:** 0
- **Priority Score:** 44403.6
- **Functions:** 34/38 matched (target 34)
- **Missing functions:** `collect_impl`, `collect_into_impl`, `can_fill_with_args_impl`, `parser_impl`
- **Types:** 6/6 matched (target 11)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/spec.rs` vs expected `eval/runtime/params/spec.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/spec.rs` (current: `// port-lint: source src/eval/runtime/params/spec.rs`)
- **Lint issues:** 1

### 166. typing.custom

- **Target:** `typing.Custom [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 43804.2
- **Functions:** 31/35 matched (target 50)
- **Missing functions:** `eq`, `hash`, `partial_cmp`, `cmp`
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `custom.rs` vs expected `typing/custom.rs`
- **Proposed provenance header:** `// port-lint: source typing/custom.rs` (current: `// port-lint: source custom.rs`)
- **Lint issues:** 1

### 167. profile.aggregated

- **Target:** `profile.Aggregated [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 43202.8
- **Functions:** 20/24 matched (target 39)
- **Missing functions:** `normalize_for_golden_tests`, `fmt`, `total_alloc_count`, `make`
- **Types:** 8/8 matched (target 11)
- **Missing types:** _none_
- **Tests:** 3/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/aggregated.rs` vs expected `values/layout/heap/profile/aggregated.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/profile/aggregated.rs` vs expected `values/layout/heap/profile/aggregated.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/aggregated.rs` (current: `// port-lint: source src/values/layout/heap/profile/aggregated.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/profile/aggregated.rs` (current: `// port-lint: tests src/values/layout/heap/profile/aggregated.rs`)
- **Lint issues:** 2

### 168. profile.time_flame

- **Target:** `profile.TimeFlame [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 43003.1
- **Functions:** 16/19 matched (target 21)
- **Missing functions:** `r#gen`, `register_sleep`, `sleep`
- **Types:** 10/11 matched (target 16)
- **Missing types:** `Data`
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/timeFlame.rs` vs expected `eval/runtime/profile/time_flame.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/time_flame.rs` vs expected `eval/runtime/profile/time_flame.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/time_flame.rs` (current: `// port-lint: source src/eval/runtime/profile/timeFlame.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/time_flame.rs` (current: `// port-lint: tests src/eval/runtime/profile/time_flame.rs`)
- **Lint issues:** 2

### 169. bc.addr

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

### 170. typing.callable_param

- **Target:** `typing.CallableParam [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 42604.0
- **Functions:** 17/20 matched (target 28)
- **Missing functions:** `fmt`, `pf`, `new_named_only`
- **Types:** 5/6 matched (target 11)
- **Missing types:** `ParamSpecDisplay`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callableParam.rs` vs expected `typing/callable_param.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/typing/callable_param.rs` vs expected `typing/callable_param.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable_param.rs` (current: `// port-lint: source src/typing/callableParam.rs`)
- **Proposed provenance header:** `// port-lint: tests typing/callable_param.rs` (current: `// port-lint: tests src/typing/callable_param.rs`)
- **Lint issues:** 2

### 171. range.range_type

- **Target:** `range.RangeType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 42502.7
- **Functions:** 20/24 matched (target 41)
- **Missing functions:** `fmt`, `range`, `range_start_stop`, `range_stop`
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Tests:** 5/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/range/rangeType.rs` vs expected `values/types/range/range_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/range/range_type.rs` vs expected `values/types/range/range_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/range/range_type.rs` (current: `// port-lint: source src/values/types/range/rangeType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/range/range_type.rs` (current: `// port-lint: tests src/values/types/range/range_type.rs`)
- **Lint issues:** 3

### 172. types.function

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

### 173. string.repr

- **Target:** `string.Repr [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 42310.0
- **Functions:** 18/22 matched (target 24)
- **Missing functions:** `push_vec_tail`, `test`, `string_repr_for_test`, `load`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 8/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/repr.rs` vs expected `values/types/string/repr.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/repr.rs` vs expected `values/types/string/repr.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/repr.rs` (current: `// port-lint: source src/values/types/string/repr.rs`)
- **Proposed provenance header:** `// port-lint: source values/types/string/repr.rs` (current: `// port-lint: source src/values/types/string/repr.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/dubious.rs` vs expected `analysis/dubious.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/dubious.rs` vs expected `analysis/dubious.rs`
- **Proposed provenance header:** `// port-lint: source analysis/dubious.rs` (current: `// port-lint: source src/analysis/dubious.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/dubious.rs` (current: `// port-lint: tests src/analysis/dubious.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/csv.rs` vs expected `eval/runtime/profile/csv.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/csv.rs` vs expected `eval/runtime/profile/csv.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/csv.rs` (current: `// port-lint: source src/eval/runtime/profile/csv.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/csv.rs` (current: `// port-lint: tests src/eval/runtime/profile/csv.rs`)
- **Lint issues:** 2

### 176. runtime.inlined_frame

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

### 177. params.parser

- **Target:** `params.Parser [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 41002.5
- **Functions:** 5/9 matched (target 10)
- **Missing functions:** `test_documentation`, `test_parameters_str`, `test`, `test_can_fill_with_args`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/params/parser.rs` vs expected `eval/runtime/params/parser.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/params/parser.rs` (current: `// port-lint: source src/eval/runtime/params/parser.rs`)
- **Lint issues:** 1

### 178. dict.alloc

- **Target:** `dict.Alloc [PROVENANCE-FALLBACK]`
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

### 179. structs.alloc

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

### 180. enumeration.globals

- **Target:** `enumeration.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 40508.8
- **Functions:** 1/5 matched (target 1)
- **Missing functions:** `r#enum`, `test_enum`, `test_enum_equality`, `test_enum_repr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Tests:** 0/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/enumeration/globals.rs` vs expected `values/types/enumeration/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/enumeration/globals.rs` (current: `// port-lint: source src/values/types/enumeration/globals.rs`)
- **Lint issues:** 1

### 181. heap.repr

- **Target:** `heap.Repr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 33204.2
- **Functions:** 24/27 matched (target 36)
- **Missing functions:** `hash`, `eq`, `as_avalue_or_header`
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/repr.rs` vs expected `values/layout/heap/repr.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/repr.rs` (current: `// port-lint: source src/values/layout/heap/repr.rs`)
- **Lint issues:** 1

### 182. opt.if_rand

- **Target:** `opt.IfRand [PROVENANCE-FALLBACK]`
- **Similarity:** 0.78
- **Dependents:** 0
- **Priority Score:** 32902.2
- **Functions:** 23/26 matched (target 30)
- **Missing functions:** `r#true`, `r#false`, `fmt`
- **Types:** 3/3 matched (target 8)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/ifRand.rs` vs expected `tests/opt/if_rand.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/if_rand.rs` (current: `// port-lint: source src/tests/opt/ifRand.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/alloc/chain.rs` vs expected `values/layout/heap/allocator/alloc/chain.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/layout/heap/allocator/alloc/chain.rs` vs expected `values/layout/heap/allocator/alloc/chain.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/alloc/chain.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/chain.rs`)
- **Proposed provenance header:** `// port-lint: tests values/layout/heap/allocator/alloc/chain.rs` (current: `// port-lint: tests src/values/layout/heap/allocator/alloc/chain.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/flow.rs` vs expected `analysis/flow.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/flow.rs` vs expected `analysis/flow.rs`
- **Proposed provenance header:** `// port-lint: source analysis/flow.rs` (current: `// port-lint: source src/analysis/flow.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/flow.rs` (current: `// port-lint: tests src/analysis/flow.rs`)
- **Lint issues:** 2

### 185. profile.heap

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

### 186. tests.rustdocs

- **Target:** `tests.Rustdocs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.26
- **Dependents:** 0
- **Priority Score:** 31707.4
- **Functions:** 11/14 matched (target 18)
- **Missing functions:** `object`, `func1`, `module`
- **Types:** 3/3 matched (target 4)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/tests/rustdocs.rs` vs expected `docs/tests/rustdocs.rs`
- **Proposed provenance header:** `// port-lint: source docs/tests/rustdocs.rs` (current: `// port-lint: source src/docs/tests/rustdocs.rs`)
- **Lint issues:** 1

### 187. type_compiled.matcher

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

### 188. avalues.list

- **Target:** `avalues.List [PROVENANCE-FALLBACK]`
- **Similarity:** 0.49
- **Dependents:** 0
- **Priority Score:** 31405.1
- **Functions:** 9/10 matched (target 18)
- **Missing functions:** `alloc_list_concat`
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/list.rs` vs expected `values/layout/avalues/list.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/list.rs` (current: `// port-lint: source src/values/layout/avalues/list.rs`)
- **Lint issues:** 1

### 189. list.refs

- **Target:** `list.Refs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.52
- **Dependents:** 0
- **Priority Score:** 31404.8
- **Functions:** 9/9 matched (target 29)
- **Missing functions:** _none_
- **Types:** 2/5 matched (target 10)
- **Missing types:** `Target`, `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/refs.rs` vs expected `values/types/list/refs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/refs.rs` (current: `// port-lint: source src/values/types/list/refs.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/underscore.rs` vs expected `analysis/underscore.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/underscore.rs` vs expected `analysis/underscore.rs`
- **Proposed provenance header:** `// port-lint: source analysis/underscore.rs` (current: `// port-lint: source src/analysis/underscore.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/underscore.rs` (current: `// port-lint: tests src/analysis/underscore.rs`)
- **Lint issues:** 2

### 191. avalues.static_

- **Target:** `avalues.Static [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 31403.4
- **Functions:** 8/9 matched (target 11)
- **Missing functions:** `test_alloc_static_simple`
- **Types:** 3/5 matched (target 3)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/static_.rs` vs expected `values/layout/avalues/static_.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/static_.rs` (current: `// port-lint: source src/values/layout/avalues/static_.rs`)
- **Lint issues:** 1

### 192. symbol.map

- **Target:** `symbol.Map [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 31306.0
- **Functions:** 9/12 matched (target 11)
- **Missing functions:** `fmt`, `new`, `with_capacity`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/collections/symbol/map.rs` vs expected `collections/symbol/map.rs`
- **Proposed provenance header:** `// port-lint: source collections/symbol/map.rs` (current: `// port-lint: source src/collections/symbol/map.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/json.rs` vs expected `stdlib/json.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/json.rs` vs expected `stdlib/json.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/json.rs` (current: `// port-lint: source src/stdlib/json.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/json.rs` (current: `// port-lint: tests src/stdlib/json.rs`)
- **Lint issues:** 2

### 194. module.named_positional

- **Target:** `module.NamedPositional [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 31103.9
- **Functions:** 8/11 matched (target 8)
- **Missing functions:** `positional`, `named`, `named_only`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/namedPositional.rs` vs expected `tests/derive/module/named_positional.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/named_positional.rs` (current: `// port-lint: source src/tests/derive/module/namedPositional.rs`)
- **Lint issues:** 1

### 195. tuple.refs

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

### 196. bc.repr

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/inspect.rs` vs expected `debug/inspect.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/debug/inspect.rs` vs expected `debug/inspect.rs`
- **Proposed provenance header:** `// port-lint: source debug/inspect.rs` (current: `// port-lint: source src/debug/inspect.rs`)
- **Proposed provenance header:** `// port-lint: tests debug/inspect.rs` (current: `// port-lint: tests src/debug/inspect.rs`)
- **Lint issues:** 2

### 198. string.alloc_unpack

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

### 199. profile.mode

- **Target:** `profile.Mode [PROVENANCE-FALLBACK]`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 30607.3
- **Functions:** 2/4 matched
- **Missing functions:** `fmt`, `from_str`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Err`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/mode.rs` vs expected `eval/runtime/profile/mode.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/mode.rs` (current: `// port-lint: source src/eval/runtime/profile/mode.rs`)
- **Lint issues:** 1

### 200. freeze.validator_order

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

### 201. values.typing.ty

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

### 202. tests.freeze_access_value

- **Target:** `tests.FreezeAccessValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.40
- **Dependents:** 0
- **Priority Score:** 30406.0
- **Functions:** 1/2 matched
- **Missing functions:** `test`
- **Types:** 0/2 matched (target 3)
- **Missing types:** `Test`, `Frozen`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/freezeAccessValue.rs` vs expected `tests/freeze_access_value.rs`
- **Proposed provenance header:** `// port-lint: source tests/freeze_access_value.rs` (current: `// port-lint: source src/tests/freezeAccessValue.rs`)
- **Lint issues:** 1

### 203. layout.vtable

- **Target:** `layout.Vtable`
- **Similarity:** 0.80
- **Dependents:** 0
- **Priority Score:** 27302.0
- **Functions:** 67/67 matched (target 76)
- **Missing functions:** _none_
- **Types:** 4/6 matched (target 4)
- **Missing types:** `GetTypeId`, `GetAllocativeKey`

### 204. compiler.expr

- **Target:** `compiler.Expr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 27002.6
- **Functions:** 59/59 matched (target 63)
- **Missing functions:** _none_
- **Types:** 9/11 matched (target 56)
- **Missing types:** `AstLiteralCompile`, `CompilerExprUtil`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/expr.rs` vs expected `eval/compiler/expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/expr.rs` (current: `// port-lint: source src/eval/compiler/expr.rs`)
- **Lint issues:** 1

### 205. adapter.implementation

- **Target:** `adapter.Implementation [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 22902.7
- **Functions:** 22/23 matched (target 28)
- **Missing functions:** `fmt`
- **Types:** 5/6 matched (target 10)
- **Missing types:** `ToEvalMessage`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter/implementation.rs` vs expected `debug/adapter/implementation.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter/implementation.rs` (current: `// port-lint: source src/debug/adapter/implementation.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/extra.rs` vs expected `stdlib/extra.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/extra.rs` vs expected `stdlib/extra.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/extra.rs` (current: `// port-lint: source src/stdlib/extra.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/extra.rs` (current: `// port-lint: tests src/stdlib/extra.rs`)
- **Lint issues:** 2

### 207. bc.stack_ptr

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

### 208. analysis.incompatible

- **Target:** `analysis.Incompatible [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 21502.7
- **Functions:** 12/14 matched (target 20)
- **Missing functions:** `lint`, `module`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Tests:** 2/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/incompatible.rs` vs expected `analysis/incompatible.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/incompatible.rs` vs expected `analysis/incompatible.rs`
- **Proposed provenance header:** `// port-lint: source analysis/incompatible.rs` (current: `// port-lint: source src/analysis/incompatible.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/incompatible.rs` (current: `// port-lint: tests src/analysis/incompatible.rs`)
- **Lint issues:** 2

### 209. compiler.args

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

### 210. profile.typecheck

- **Target:** `profile.Typecheck [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 21303.2
- **Functions:** 7/8 matched
- **Missing functions:** `r#gen`
- **Types:** 4/5 matched (target 6)
- **Missing types:** `Data`
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/typecheck.rs` vs expected `eval/runtime/profile/typecheck.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/typecheck.rs` vs expected `eval/runtime/profile/typecheck.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/typecheck.rs` (current: `// port-lint: source src/eval/runtime/profile/typecheck.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/typecheck.rs` (current: `// port-lint: tests src/eval/runtime/profile/typecheck.rs`)
- **Lint issues:** 2

### 211. avalues.array

- **Target:** `avalues.Array [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 21303.2
- **Functions:** 9/9 matched (target 17)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/array.rs` vs expected `values/layout/avalues/array.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/array.rs` (current: `// port-lint: source src/values/layout/avalues/array.rs`)
- **Lint issues:** 1

### 212. profile.summary_by_function

- **Target:** `profile.SummaryByFunction [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 21303.1
- **Functions:** 9/10 matched
- **Missing functions:** `drop_non_drop`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `RowKind`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/profile/summaryByFunction.rs` vs expected `values/layout/heap/profile/summary_by_function.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/profile/summary_by_function.rs` (current: `// port-lint: source src/values/layout/heap/profile/summaryByFunction.rs`)
- **Lint issues:** 1

### 213. analysis

- **Target:** `starlark.Analysis [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 21302.7
- **Functions:** 10/12 matched (target 11)
- **Missing functions:** `lint`, `module`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 10/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis.rs` vs expected `analysis.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis.rs` vs expected `analysis.rs`
- **Proposed provenance header:** `// port-lint: source analysis.rs` (current: `// port-lint: source src/analysis.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis.rs` (current: `// port-lint: tests src/analysis.rs`)
- **Lint issues:** 2

### 214. bc.opcode

- **Target:** `bc.Opcode [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 21204.5
- **Functions:** 7/7 matched (target 11)
- **Missing functions:** _none_
- **Types:** 3/5 matched (target 4)
- **Missing types:** `ByNumber`, `FindOpcode`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/opcode.rs` vs expected `eval/bc/opcode.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/bc/opcode.rs` vs expected `eval/bc/opcode.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/opcode.rs` (current: `// port-lint: source src/eval/bc/opcode.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/bc/opcode.rs` (current: `// port-lint: tests src/eval/bc/opcode.rs`)
- **Lint issues:** 2

### 215. avalues.tuple

- **Target:** `avalues.Tuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 21204.2
- **Functions:** 8/8 matched (target 16)
- **Missing functions:** _none_
- **Types:** 2/4 matched (target 2)
- **Missing types:** `StarlarkValue`, `ExtraElem`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/avalues/tuple.rs` vs expected `values/layout/avalues/tuple.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/avalues/tuple.rs` (current: `// port-lint: source src/values/layout/avalues/tuple.rs`)
- **Lint issues:** 1

### 216. record.globals

- **Target:** `record.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 21202.1
- **Functions:** 10/12 matched (target 10)
- **Missing functions:** `record`, `field`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/globals.rs` vs expected `values/types/record/globals.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/globals.rs` vs expected `values/types/record/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/globals.rs` (current: `// port-lint: source src/values/types/record/globals.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/record/globals.rs` (current: `// port-lint: tests src/values/types/record/globals.rs`)
- **Lint issues:** 2

### 217. tests.basic

- **Target:** `tests.Basic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.94
- **Dependents:** 0
- **Priority Score:** 21200.6
- **Functions:** 10/12 matched
- **Missing functions:** `arithmetic_test`, `bitwise_test`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/11 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/basic.rs` vs expected `tests/basic.rs`
- **Proposed provenance header:** `// port-lint: source tests/basic.rs` (current: `// port-lint: source src/tests/basic.rs`)
- **Lint issues:** 1

### 218. avalues.complex

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

### 219. analysis.performance

- **Target:** `analysis.Performance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 21103.2
- **Functions:** 8/10 matched (target 17)
- **Missing functions:** `lint`, `module`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 2/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/performance.rs` vs expected `analysis/performance.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/performance.rs` vs expected `analysis/performance.rs`
- **Proposed provenance header:** `// port-lint: source analysis/performance.rs` (current: `// port-lint: source src/analysis/performance.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/performance.rs` (current: `// port-lint: tests src/analysis/performance.rs`)
- **Lint issues:** 2

### 220. eval.bc.compiler.stmt

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Stmt [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 21004.1
- **Functions:** 8/10 matched (target 11)
- **Missing functions:** `write_if_then`, `write_if_else`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/stmt.rs` vs expected `eval/bc/compiler/stmt.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/stmt.rs` (current: `// port-lint: source src/eval/bc/compiler/stmt.rs`)
- **Lint issues:** 1

### 221. symbol.symbol

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

### 222. set.refs

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

### 223. module.basic

- **Target:** `module.Basic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.43
- **Dependents:** 0
- **Priority Score:** 20905.7
- **Functions:** 7/9 matched (target 8)
- **Missing functions:** `r#enum`, `test`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 3/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/basic.rs` vs expected `tests/derive/module/basic.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/basic.rs` (current: `// port-lint: source src/tests/derive/module/basic.rs`)
- **Lint issues:** 1

### 224. typing.callable

- **Target:** `typing.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 20904.2
- **Functions:** 6/7 matched (target 10)
- **Missing functions:** `fmt`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `TyCallableInner`
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/callable.rs` vs expected `typing/callable.rs`
- **Proposed provenance header:** `// port-lint: source typing/callable.rs` (current: `// port-lint: source tests/callable.rs`)
- **Lint issues:** 1

### 225. bc.bytecode

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

### 226. bc.call

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

### 227. structs.refs

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

### 228. bc.instr_arg

- **Target:** `bc.InstrArg [PROVENANCE-FALLBACK]`
- **Similarity:** 0.74
- **Dependents:** 0
- **Priority Score:** 20902.6
- **Functions:** 4/5 matched (target 84)
- **Missing functions:** `fmt`
- **Types:** 3/4 matched (target 42)
- **Missing types:** `HandlerImpl`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instrArg.rs` vs expected `eval/bc/instr_arg.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr_arg.rs` (current: `// port-lint: source src/eval/bc/instrArg.rs`)
- **Lint issues:** 55

### 229. derive.docs

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

### 230. heap.call_enter_exit

- **Target:** `heap.CallEnterExit [PROVENANCE-FALLBACK]`
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

### 231. types.any

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

### 232. dict.traits

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

### 233. list.globals

- **Target:** `list.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.51
- **Dependents:** 0
- **Priority Score:** 20604.9
- **Functions:** 4/5 matched
- **Missing functions:** `list`
- **Types:** 0/1 matched
- **Missing types:** `ListType`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/globals.rs` vs expected `values/types/list/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/globals.rs` (current: `// port-lint: source src/values/types/list/globals.rs`)
- **Lint issues:** 2

### 234. float.unpack

- **Target:** `float.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.56
- **Dependents:** 0
- **Priority Score:** 20604.4
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/unpack.rs` vs expected `values/types/float/unpack.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/float/unpack.rs` vs expected `values/types/float/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/unpack.rs` (current: `// port-lint: source src/values/types/float/unpack.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/float/unpack.rs` (current: `// port-lint: tests src/values/types/float/unpack.rs`)
- **Lint issues:** 2

### 235. freeze.bounds

- **Target:** `freeze.Bounds [PROVENANCE-FALLBACK]`
- **Similarity:** 0.60
- **Dependents:** 0
- **Priority Score:** 20604.0
- **Functions:** 2/3 matched (target 5)
- **Missing functions:** `assert_impl`
- **Types:** 2/3 matched
- **Missing types:** `Test`
- **Tests:** 0/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/bounds.rs` vs expected `tests/derive/freeze/bounds.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/bounds.rs` (current: `// port-lint: source src/tests/derive/freeze/bounds.rs`)
- **Lint issues:** 1

### 236. int.i32

- **Target:** `int.I32 [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 20602.7
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 0/2 matched (target 4)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/int/i32.rs` vs expected `values/types/int/i32.rs`
- **Proposed provenance header:** `// port-lint: source values/types/int/i32.rs` (current: `// port-lint: source src/values/types/int/i32.rs`)
- **Lint issues:** 1

### 237. dict.unpack

- **Target:** `dict.Unpack [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 20602.4
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Canonical`, `Error`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/unpack.rs` vs expected `values/types/dict/unpack.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/unpack.rs` (current: `// port-lint: source src/values/types/dict/unpack.rs`)
- **Lint issues:** 1

### 238. bc.definitely_assigned

- **Target:** `bc.DefinitelyAssigned [PROVENANCE-FALLBACK]`
- **Similarity:** 0.42
- **Dependents:** 0
- **Priority Score:** 20505.8
- **Functions:** 2/4 matched (target 7)
- **Missing functions:** `new`, `assert_smaller_then`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/definitelyAssigned.rs` vs expected `eval/bc/definitely_assigned.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/definitely_assigned.rs` (current: `// port-lint: source src/eval/bc/definitelyAssigned.rs`)
- **Lint issues:** 1

### 239. funcs.min_max

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

### 240. typing.any

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

### 241. module.return_impl

- **Target:** `module.ReturnImpl [PROVENANCE-FALLBACK]`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 20407.9
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `func`, `attr`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/returnImpl.rs` vs expected `tests/derive/module/return_impl.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/return_impl.rs` (current: `// port-lint: source src/tests/derive/module/returnImpl.rs`)
- **Lint issues:** 1

### 242. collections.maybe_uninit_backport

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

### 243. debug.evaluate

- **Target:** `debug.Evaluate [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 20401.9
- **Functions:** 2/4 matched (target 5)
- **Missing functions:** `debugger`, `debug_evaluate`
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/3 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/evaluate.rs` vs expected `debug/evaluate.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/debug/evaluate.rs` vs expected `debug/evaluate.rs`
- **Proposed provenance header:** `// port-lint: source debug/evaluate.rs` (current: `// port-lint: source src/debug/evaluate.rs`)
- **Proposed provenance header:** `// port-lint: tests debug/evaluate.rs` (current: `// port-lint: tests src/debug/evaluate.rs`)
- **Lint issues:** 2

### 244. enumeration.ty_enum_type

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

### 245. heap.maybe_uninit_slice_util

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

### 246. tests.tuple

- **Target:** `typing.Tuple [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/2 matched (target 9)
- **Missing functions:** `test_tuple`, `test_tuple_ellipsis`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `tuple.rs` vs expected `typing/tests/tuple.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/tuple.rs` (current: `// port-lint: source tuple.rs`)
- **Lint issues:** 1

### 247. values.traits

- **Target:** `values.Traits [PROVENANCE-FALLBACK]`
- **Similarity:** 0.75
- **Dependents:** 0
- **Priority Score:** 15902.5
- **Functions:** 56/56 matched
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 2)
- **Missing types:** `Canonical`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/traits.rs` vs expected `values/traits.rs`
- **Proposed provenance header:** `// port-lint: source values/traits.rs` (current: `// port-lint: source src/values/traits.rs`)
- **Lint issues:** 10

### 248. debug.adapter

- **Target:** `debug.Adapter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.68
- **Dependents:** 0
- **Priority Score:** 13603.2
- **Functions:** 21/22 matched (target 23)
- **Missing functions:** `fmt`
- **Types:** 14/14 matched (target 29)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/debug/adapter.rs` vs expected `debug/adapter.rs`
- **Proposed provenance header:** `// port-lint: source debug/adapter.rs` (current: `// port-lint: source src/debug/adapter.rs`)
- **Lint issues:** 1

### 249. docs

- **Target:** `docs.Docs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 12304.5
- **Functions:** 12/13 matched (target 16)
- **Missing functions:** `default`
- **Types:** 10/10 matched (target 15)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs.rs` vs expected `docs.rs`
- **Proposed provenance header:** `// port-lint: source docs.rs` (current: `// port-lint: source src/docs.rs`)
- **Lint issues:** 1

### 250. structs.value

- **Target:** `structs.Value [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 12210.0
- **Functions:** 20/21 matched (target 23)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 5/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/value.rs` vs expected `values/types/structs/value.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/structs/value.rs` vs expected `values/types/structs/value.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/value.rs` (current: `// port-lint: source src/values/types/structs/value.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/structs/value.rs` (current: `// port-lint: tests src/values/types/structs/value.rs`)
- **Lint issues:** 2

### 251. typing.basic

- **Target:** `typing.Basic [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 12002.8
- **Functions:** 18/19 matched (target 20)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched (target 11)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `basic.rs` vs expected `typing/basic.rs`
- **Proposed provenance header:** `// port-lint: source typing/basic.rs` (current: `// port-lint: source basic.rs`)
- **Lint issues:** 1

### 252. funcs.other

- **Target:** `funcs.Other [ZERO] [PROVENANCE-FALLBACK]`
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

### 253. docs.parse

- **Target:** `docs.Parse [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 11603.0
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

### 254. tests.runtime

- **Target:** `tests.Runtime [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 11504.1
- **Functions:** 13/14 matched (target 15)
- **Missing functions:** `drop`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 6/6 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/runtime.rs` vs expected `tests/runtime.rs`
- **Proposed provenance header:** `// port-lint: source tests/runtime.rs` (current: `// port-lint: source src/tests/runtime.rs`)
- **Lint issues:** 1

### 255. record.instance

- **Target:** `record.Instance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 11403.4
- **Functions:** 12/13 matched (target 17)
- **Missing functions:** `fmt`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/instance.rs` vs expected `values/types/record/instance.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/instance.rs` (current: `// port-lint: source src/values/types/record/instance.rs`)
- **Lint issues:** 1

### 256. compiler.def_inline

- **Target:** `compiler.DefInline [PROVENANCE-FALLBACK]`
- **Similarity:** 0.70
- **Dependents:** 0
- **Priority Score:** 11403.0
- **Functions:** 9/10 matched (target 9)
- **Missing functions:** `new`
- **Types:** 4/4 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/defInline.rs` vs expected `eval/compiler/def_inline.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/def_inline.rs` (current: `// port-lint: source src/eval/compiler/defInline.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/flamegraph.rs` vs expected `eval/runtime/profile/flamegraph.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/eval/runtime/profile/flamegraph.rs` vs expected `eval/runtime/profile/flamegraph.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/flamegraph.rs` (current: `// port-lint: source src/eval/runtime/profile/flamegraph.rs`)
- **Proposed provenance header:** `// port-lint: tests eval/runtime/profile/flamegraph.rs` (current: `// port-lint: tests src/eval/runtime/profile/flamegraph.rs`)
- **Lint issues:** 2

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/list/methods.rs` vs expected `values/types/list/methods.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/list/methods.rs` vs expected `values/types/list/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/list/methods.rs` (current: `// port-lint: source src/values/types/list/methods.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/list/methods.rs` (current: `// port-lint: tests src/values/types/list/methods.rs`)
- **Lint issues:** 4

### 259. type_compiled.factory

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

### 260. module.generic

- **Target:** `module.Generic [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 11010.0
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/2 matched
- **Missing types:** `Canonical`
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/generic.rs` vs expected `tests/derive/module/generic.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/generic.rs` (current: `// port-lint: source src/tests/derive/module/generic.rs`)
- **Lint issues:** 1

### 261. bool.value

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

### 262. enumeration.value

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

### 263. namespace.typing

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/never.rs` vs expected `values/typing/never.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/never.rs` vs expected `values/typing/never.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/never.rs` (current: `// port-lint: source src/values/typing/never.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/never.rs` (current: `// port-lint: tests src/values/typing/never.rs`)
- **Lint issues:** 4

### 265. values.recursive_repr_or_json_guard

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/tuple/alloc.rs` vs expected `values/types/tuple/alloc.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/tuple/alloc.rs` vs expected `values/types/tuple/alloc.rs`
- **Proposed provenance header:** `// port-lint: source values/types/tuple/alloc.rs` (current: `// port-lint: source src/values/types/tuple/alloc.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/tuple/alloc.rs` (current: `// port-lint: tests src/values/types/tuple/alloc.rs`)
- **Lint issues:** 2

### 267. profile.by_type

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typeRepr.rs` vs expected `values/type_repr.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typeRepr.rs` vs expected `values/type_repr.rs`
- **Proposed provenance header:** `// port-lint: source values/type_repr.rs` (current: `// port-lint: source src/values/typeRepr.rs`)
- **Proposed provenance header:** `// port-lint: tests values/type_repr.rs` (current: `// port-lint: tests src/values/typeRepr.rs`)
- **Lint issues:** 2

### 269. eval.bc.compiler.call

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Call [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 10505.9
- **Functions:** 4/5 matched (target 8)
- **Missing functions:** `mark_definitely_assigned_after`
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/call.rs` vs expected `eval/bc/compiler/call.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/call.rs` (current: `// port-lint: source src/eval/bc/compiler/call.rs`)
- **Lint issues:** 1

### 270. types.unbound

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

### 271. structs.structs

- **Target:** `structs.Structs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 10503.8
- **Functions:** 3/4 matched
- **Missing functions:** `r#struct`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/structs/structs.rs` vs expected `values/types/structs/structs.rs`
- **Proposed provenance header:** `// port-lint: source values/types/structs/structs.rs` (current: `// port-lint: source src/values/types/structs/structs.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/set.rs` vs expected `values/types/set/set.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/set.rs` vs expected `values/types/set/set.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/set.rs` (current: `// port-lint: source src/values/types/set/set.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/set/set.rs` (current: `// port-lint: tests src/values/types/set/set.rs`)
- **Lint issues:** 2

### 273. module.type_annotation

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/globals.rs` vs expected `values/typing/type_compiled/globals.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/typing/type_compiled/globals.rs` vs expected `values/typing/type_compiled/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/globals.rs` (current: `// port-lint: source src/values/typing/typeCompiled/globals.rs`)
- **Proposed provenance header:** `// port-lint: tests values/typing/type_compiled/globals.rs` (current: `// port-lint: tests src/values/typing/type_compiled/globals.rs`)
- **Lint issues:** 2

### 275. freeze.validator

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

### 276. dict.globals

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

### 277. module.default_value

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

### 278. compiler.assign_modify

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

### 279. pagable.error

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

### 280. runtime.visit_span

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

### 281. float.globals

- **Target:** `float.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 10208.2
- **Functions:** 1/2 matched (target 1)
- **Missing functions:** `float`
- **Types:** 0/0 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/float/globals.rs` vs expected `values/types/float/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/float/globals.rs` (current: `// port-lint: source src/values/types/float/globals.rs`)
- **Lint issues:** 1

### 282. bool.globals

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

### 283. int.globals

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

### 284. bool.type_repr

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

### 285. tuple.globals

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

### 286. range.globals

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

### 287. namespace.globals

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

### 288. num.globals

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

### 289. bool.unpack

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

### 290. freeze.basic

- **Target:** `freeze.Basic [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 10100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/1 matched
- **Missing types:** `TestUnitStruct`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/basic.rs` vs expected `tests/derive/freeze/basic.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/basic.rs` (current: `// port-lint: source src/tests/derive/freeze/basic.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/set/methods.rs` vs expected `values/types/set/methods.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/set/methods.rs` vs expected `values/types/set/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/set/methods.rs` (current: `// port-lint: source src/values/types/set/methods.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/set/methods.rs` (current: `// port-lint: tests src/values/types/set/methods.rs`)
- **Lint issues:** 3

### 292. bc.writer

- **Target:** `bc.Writer [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 4601.9
- **Functions:** 42/42 matched (target 44)
- **Missing functions:** _none_
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/writer.rs` vs expected `eval/bc/writer.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/writer.rs` (current: `// port-lint: source src/eval/bc/writer.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/methods.rs` vs expected `values/types/string/methods.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/methods.rs` vs expected `values/types/string/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/methods.rs` (current: `// port-lint: source src/values/types/string/methods.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/string/methods.rs` (current: `// port-lint: tests src/values/types/string/methods.rs`)
- **Lint issues:** 3

### 294. typing.fill_types_for_lint

- **Target:** `typing.FillTypesForLint [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 4202.4
- **Functions:** 39/39 matched (target 40)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/fillTypesForLint.rs` vs expected `typing/fill_types_for_lint.rs`
- **Proposed provenance header:** `// port-lint: source typing/fill_types_for_lint.rs` (current: `// port-lint: source src/typing/fillTypesForLint.rs`)
- **Lint issues:** 4

### 295. oracle.ctx

- **Target:** `oracle.Ctx [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 3401.9
- **Functions:** 32/32 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 14)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `oracle/ctx.rs` vs expected `typing/oracle/ctx.rs`
- **Proposed provenance header:** `// port-lint: source typing/oracle/ctx.rs` (current: `// port-lint: source oracle/ctx.rs`)
- **Lint issues:** 1

### 296. type_compiled.alloc

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

### 297. type_compiled.matchers

- **Target:** `typecompiled.Matchers [PROVENANCE-FALLBACK]`
- **Similarity:** 0.86
- **Dependents:** 0
- **Priority Score:** 2601.4
- **Functions:** 3/3 matched (target 25)
- **Missing functions:** _none_
- **Types:** 23/23 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/matchers.rs` vs expected `values/typing/type_compiled/matchers.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/matchers.rs` (current: `// port-lint: source src/values/typing/typeCompiled/matchers.rs`)
- **Lint issues:** 3

### 298. typing.ctx

- **Target:** `typing.Ctx [PROVENANCE-FALLBACK]`
- **Similarity:** 0.72
- **Dependents:** 0
- **Priority Score:** 2002.8
- **Functions:** 19/19 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/ctx.rs` vs expected `typing/ctx.rs`
- **Proposed provenance header:** `// port-lint: source typing/ctx.rs` (current: `// port-lint: source src/typing/ctx.rs`)
- **Lint issues:** 1

### 299. docs.markdown

- **Target:** `docs.Markdown [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 2001.6
- **Functions:** 18/18 matched (target 19)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/docs/markdown.rs` vs expected `docs/markdown.rs`
- **Proposed provenance header:** `// port-lint: source docs/markdown.rs` (current: `// port-lint: source src/docs/markdown.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/dict/methods.rs` vs expected `values/types/dict/methods.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/dict/methods.rs` vs expected `values/types/dict/methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/dict/methods.rs` (current: `// port-lint: source src/values/types/dict/methods.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/dict/methods.rs` (current: `// port-lint: tests src/values/types/dict/methods.rs`)
- **Lint issues:** 3

### 301. scope.tests

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

### 302. tests.fstring

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

### 303. environment.names

- **Target:** `environment.Names [PROVENANCE-FALLBACK]`
- **Similarity:** 0.63
- **Dependents:** 0
- **Priority Score:** 1503.7
- **Functions:** 13/13 matched (target 14)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment/names.rs` vs expected `environment/names.rs`
- **Proposed provenance header:** `// port-lint: source environment/names.rs` (current: `// port-lint: source src/environment/names.rs`)
- **Lint issues:** 1

### 304. eval.bc.compiler.expr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Expr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.81
- **Dependents:** 0
- **Priority Score:** 1501.9
- **Functions:** 15/15 matched (target 16)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/expr.rs` vs expected `eval/bc/compiler/expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/expr.rs` (current: `// port-lint: source src/eval/bc/compiler/expr.rs`)
- **Lint issues:** 1

### 305. typing.error

- **Target:** `typing.Error [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 1410.0
- **Functions:** 9/9 matched (target 17)
- **Missing functions:** _none_
- **Types:** 5/5 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/error.rs` vs expected `typing/error.rs`
- **Proposed provenance header:** `// port-lint: source typing/error.rs` (current: `// port-lint: source src/typing/error.rs`)
- **Lint issues:** 1

### 306. compiler.call

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

### 307. module.unpack_value

- **Target:** `module.UnpackValue [PROVENANCE-FALLBACK]`
- **Similarity:** 0.54
- **Dependents:** 0
- **Priority Score:** 1304.6
- **Functions:** 13/13 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 4/4 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/unpackValue.rs` vs expected `tests/derive/module/unpack_value.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/unpack_value.rs` (current: `// port-lint: source src/tests/derive/module/unpackValue.rs`)
- **Lint issues:** 1

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
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/typing/typeCompiled/tests.rs` vs expected `values/typing/type_compiled/tests.rs`
- **Proposed provenance header:** `// port-lint: source values/typing/type_compiled/tests.rs` (current: `// port-lint: source src/values/typing/typeCompiled/tests.rs`)
- **Lint issues:** 1

### 309. profile.tests

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

### 310. compiler.compr

- **Target:** `compiler.Compr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 1202.4
- **Functions:** 9/9 matched (target 12)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/compr.rs` vs expected `eval/compiler/compr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/compr.rs` (current: `// port-lint: source src/eval/compiler/compr.rs`)
- **Lint issues:** 1

### 311. bc.if_stmt

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

### 312. environment.slots

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

### 313. docs.multipage

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

### 314. tests.comprehension

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

### 315. profile.data

- **Target:** `profile.Data [PROVENANCE-FALLBACK]`
- **Similarity:** 0.61
- **Dependents:** 0
- **Priority Score:** 903.9
- **Functions:** 6/6 matched (target 7)
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 18)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/data.rs` vs expected `eval/runtime/profile/data.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/data.rs` (current: `// port-lint: source src/eval/runtime/profile/data.rs`)
- **Lint issues:** 1

### 316. compiler.types

- **Target:** `compiler.Types [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 901.6
- **Functions:** 8/8 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 7)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/types.rs` vs expected `eval/compiler/types.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/types.rs` (current: `// port-lint: source src/eval/compiler/types.rs`)
- **Lint issues:** 1

### 317. tests.type_annot

- **Target:** `tests.TypeAnnot [PROVENANCE-FALLBACK]`
- **Similarity:** 0.95
- **Dependents:** 0
- **Priority Score:** 900.5
- **Functions:** 9/9 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 9/9 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/typeAnnot.rs` vs expected `tests/type_annot.rs`
- **Proposed provenance header:** `// port-lint: source tests/type_annot.rs` (current: `// port-lint: source src/tests/typeAnnot.rs`)
- **Lint issues:** 1

### 318. __derive_refs.parse_args

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

### 319. record.ty_record_type

- **Target:** `record.TyRecordType [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 801.6
- **Functions:** 7/7 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 7/7 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/record/tyRecordType.rs` vs expected `values/types/record/ty_record_type.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/record/tyRecordType.rs` vs expected `values/types/record/ty_record_type.rs`
- **Proposed provenance header:** `// port-lint: source values/types/record/ty_record_type.rs` (current: `// port-lint: source src/values/types/record/tyRecordType.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/record/ty_record_type.rs` (current: `// port-lint: tests src/values/types/record/tyRecordType.rs`)
- **Lint issues:** 2

### 320. opt.def_inline

- **Target:** `opt.DefInline [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 800.7
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 8/8 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/defInline.rs` vs expected `tests/opt/def_inline.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/def_inline.rs` (current: `// port-lint: source src/tests/opt/defInline.rs`)
- **Lint issues:** 1

### 321. tests.opt

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

### 322. bc.and_or

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

### 323. docs.code

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

### 324. alloc.per_thread

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

### 325. layout.value_not_special

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

### 326. unused_loads.find

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

### 327. types.known_methods

- **Target:** `types.KnownMethods [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 701.6
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/knownMethods.rs` vs expected `values/types/known_methods.rs`
- **Proposed provenance header:** `// port-lint: source values/types/known_methods.rs` (current: `// port-lint: source src/values/types/knownMethods.rs`)
- **Lint issues:** 1

### 328. compiler.module

- **Target:** `compiler.Module [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 701.6
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/module.rs` vs expected `eval/compiler/module.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/module.rs` (current: `// port-lint: source src/eval/compiler/module.rs`)
- **Lint issues:** 1

### 329. runtime.before_stmt

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

### 330. unused_loads.find_tests

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

### 331. opt.eq

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

### 332. module.other_attributes

- **Target:** `module.OtherAttributes [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 610.0
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/otherAttributes.rs` vs expected `tests/derive/module/other_attributes.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/other_attributes.rs` (current: `// port-lint: source src/tests/derive/module/otherAttributes.rs`)
- **Lint issues:** 1

### 333. layout.static_string

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

### 334. values.index

- **Target:** `values.Index [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 602.9
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

### 335. assert.conformance

- **Target:** `assert.Conformance [PROVENANCE-FALLBACK]`
- **Similarity:** 0.73
- **Dependents:** 0
- **Priority Score:** 602.7
- **Functions:** 5/5 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/assert/conformance.rs` vs expected `assert/conformance.rs`
- **Proposed provenance header:** `// port-lint: source assert/conformance.rs` (current: `// port-lint: source src/assert/conformance.rs`)
- **Lint issues:** 1

### 336. module.methods

- **Target:** `module.Methods [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 602.4
- **Functions:** 5/5 matched (target 7)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/methods.rs` vs expected `tests/derive/module/methods.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/methods.rs` (current: `// port-lint: source src/tests/derive/module/methods.rs`)
- **Lint issues:** 1

### 337. int.tests

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

### 338. freeze.identity

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

### 339. compiler.if_compiler

- **Target:** `compiler.IfCompiler [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 600.7
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/ifCompiler.rs` vs expected `eval/bc/compiler/if_compiler.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/if_compiler.rs` (current: `// port-lint: source src/eval/bc/compiler/ifCompiler.rs`)
- **Lint issues:** 1

### 340. string.globals

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

### 341. runtime.slots

- **Target:** `runtime.Slots [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 502.4
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 3/3 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/slots.rs` vs expected `eval/runtime/slots.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/slots.rs` (current: `// port-lint: source src/eval/runtime/slots.rs`)
- **Lint issues:** 1

### 342. values.comparison

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

### 343. funcs.zip

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

### 344. intern.interner

- **Target:** `intern.Interner [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 501.7
- **Functions:** 3/3 matched (target 7)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/intern/interner.rs` vs expected `values/types/string/intern/interner.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/values/types/string/intern/interner.rs` vs expected `values/types/string/intern/interner.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/intern/interner.rs` (current: `// port-lint: source src/values/types/string/intern/interner.rs`)
- **Proposed provenance header:** `// port-lint: tests values/types/string/intern/interner.rs` (current: `// port-lint: tests src/values/types/string/intern/interner.rs`)
- **Lint issues:** 2

### 345. compiler.expr_bool

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

### 346. num.typecheck

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

### 347. unused_loads.remove

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

### 348. tests.bc.definitely_assigned

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

### 349. module.kwargs

- **Target:** `module.Kwargs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 405.9
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/kwargs.rs` vs expected `tests/derive/module/kwargs.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/kwargs.rs` (current: `// port-lint: source src/tests/derive/module/kwargs.rs`)
- **Lint issues:** 1

### 350. __derive_refs.components

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

### 351. string.iter

- **Target:** `string.Iter [PROVENANCE-FALLBACK]`
- **Similarity:** 0.82
- **Dependents:** 0
- **Priority Score:** 401.8
- **Functions:** 3/3 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/string/iter.rs` vs expected `values/types/string/iter.rs`
- **Proposed provenance header:** `// port-lint: source values/types/string/iter.rs` (current: `// port-lint: source src/values/types/string/iter.rs`)
- **Lint issues:** 1

### 352. analysis.find_call_name

- **Target:** `analysis.FindCallName [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 401.7
- **Functions:** 3/3 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/analysis/findCallName.rs` vs expected `analysis/find_call_name.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/analysis/find_call_name.rs` vs expected `analysis/find_call_name.rs`
- **Proposed provenance header:** `// port-lint: source analysis/find_call_name.rs` (current: `// port-lint: source src/analysis/findCallName.rs`)
- **Proposed provenance header:** `// port-lint: tests analysis/find_call_name.rs` (current: `// port-lint: tests src/analysis/find_call_name.rs`)
- **Lint issues:** 2

### 353. tests.util

- **Target:** `tests.Util [PROVENANCE-FALLBACK]`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 401.7
- **Functions:** 3/3 matched (target 6)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/util.rs` vs expected `tests/util.rs`
- **Proposed provenance header:** `// port-lint: source tests/util.rs` (current: `// port-lint: source src/tests/util.rs`)
- **Lint issues:** 2

### 354. unused_loads.remove_tests

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

### 355. __derive_refs.sig

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

### 356. stdlib.internal

- **Target:** `stdlib.Internal [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 401.1
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/stdlib/internal.rs` vs expected `stdlib/internal.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/stdlib/internal.rs` vs expected `stdlib/internal.rs`
- **Proposed provenance header:** `// port-lint: source stdlib/internal.rs` (current: `// port-lint: source src/stdlib/internal.rs`)
- **Proposed provenance header:** `// port-lint: tests stdlib/internal.rs` (current: `// port-lint: tests src/stdlib/internal.rs`)
- **Lint issues:** 2

### 357. trace.bounds

- **Target:** `trace.Bounds [PROVENANCE-FALLBACK]`
- **Similarity:** 0.98
- **Dependents:** 0
- **Priority Score:** 400.2
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace/bounds.rs` vs expected `tests/derive/trace/bounds.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace/bounds.rs` (current: `// port-lint: source src/tests/derive/trace/bounds.rs`)
- **Lint issues:** 1

### 358. bc.compr

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

### 359. tests.go

- **Target:** `tests.Go [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 310.0
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/go.rs` vs expected `tests/go.rs`
- **Proposed provenance header:** `// port-lint: source tests/go.rs` (current: `// port-lint: source src/tests/go.rs`)
- **Lint issues:** 1

### 360. derive.attrs

- **Target:** `derive.Attrs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 304.2
- **Functions:** 1/1 matched (target 11)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/attrs.rs` vs expected `tests/derive/attrs.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/attrs.rs` (current: `// port-lint: source src/tests/derive/attrs.rs`)
- **Lint issues:** 1

### 361. eval.soft_error

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

### 362. oracle.traits

- **Target:** `oracle.Traits [PROVENANCE-FALLBACK]`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 304.1
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `oracle/traits.rs` vs expected `typing/oracle/traits.rs`
- **Proposed provenance header:** `// port-lint: source typing/oracle/traits.rs` (current: `// port-lint: source oracle/traits.rs`)
- **Lint issues:** 1

### 363. compiler.error

- **Target:** `compiler.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 0.62
- **Dependents:** 0
- **Priority Score:** 303.8
- **Functions:** 2/2 matched (target 21)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 13)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/error.rs` vs expected `eval/compiler/error.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `starlarkSyntax/src/error.rs` vs expected `eval/compiler/error.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source src/eval/compiler/error.rs`)
- **Proposed provenance header:** `// port-lint: source eval/compiler/error.rs` (current: `// port-lint: source starlarkSyntax/src/error.rs`)
- **Lint issues:** 2

### 364. callable.param

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

### 365. opt.type_is

- **Target:** `opt.TypeIs [PROVENANCE-FALLBACK]`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 302.4
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/opt/typeIs.rs` vs expected `tests/opt/type_is.rs`
- **Proposed provenance header:** `// port-lint: source tests/opt/type_is.rs` (current: `// port-lint: source src/tests/opt/typeIs.rs`)
- **Lint issues:** 1

### 366. module.special_params

- **Target:** `module.SpecialParams [PROVENANCE-FALLBACK]`
- **Similarity:** 0.79
- **Dependents:** 0
- **Priority Score:** 302.1
- **Functions:** 3/3 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/module/specialParams.rs` vs expected `tests/derive/module/special_params.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/module/special_params.rs` (current: `// port-lint: source src/tests/derive/module/specialParams.rs`)
- **Lint issues:** 1

### 367. compiler.type_expr

- **Target:** `compiler.TypeExpr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.89
- **Dependents:** 0
- **Priority Score:** 301.1
- **Functions:** 2/2 matched (target 8)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 17)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/compiler/typeExpr.rs` vs expected `eval/compiler/type_expr.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `starlarkSyntax/src/syntax/typeExpr.rs` vs expected `eval/compiler/type_expr.rs`
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source src/eval/compiler/typeExpr.rs`)
- **Proposed provenance header:** `// port-lint: source eval/compiler/type_expr.rs` (current: `// port-lint: source starlarkSyntax/src/syntax/typeExpr.rs`)
- **Lint issues:** 2

### 368. bc.for_stmt

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

### 369. opt.speculative_exec

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

### 370. derive.unpack_value_attr

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

### 371. __derive_refs.invoke_macro_error

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

### 372. tests.callable

- **Target:** `tests.Callable [PROVENANCE-FALLBACK]`
- **Similarity:** 0.69
- **Dependents:** 0
- **Priority Score:** 203.1
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Tests:** 2/2 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `callable.rs` vs expected `typing/tests/callable.rs`
- **Proposed provenance header:** `// port-lint: source typing/tests/callable.rs` (current: `// port-lint: source callable.rs`)
- **Lint issues:** 1

### 373. bc.golden

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

### 374. tests.special_function

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

### 375. typing.macro_refs

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

### 376. enumeration.matcher

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

### 377. eval.bc.compiler.def

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Def [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 201.6
- **Functions:** 2/2 matched (target 6)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/def.rs` vs expected `eval/bc/compiler/def.rs`
- **Provenance warning:** port-lint provenance header matched only by basename: `starlarkSyntax/src/syntax/def.rs` vs expected `eval/bc/compiler/def.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/def.rs` (current: `// port-lint: source src/eval/bc/compiler/def.rs`)
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/def.rs` (current: `// port-lint: source starlarkSyntax/src/syntax/def.rs`)
- **Lint issues:** 2

### 378. eval

- **Target:** `eval.Eval [PROVENANCE-FALLBACK]`
- **Similarity:** 0.84
- **Dependents:** 0
- **Priority Score:** 201.6
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval.rs` vs expected `eval.rs`
- **Proposed provenance header:** `// port-lint: source eval.rs` (current: `// port-lint: source src/eval.rs`)
- **Lint issues:** 1

### 379. typing.macro_support

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

### 380. compiler.assign

- **Target:** `compiler.Assign [PROVENANCE-FALLBACK]`
- **Similarity:** 0.85
- **Dependents:** 0
- **Priority Score:** 201.5
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/assign.rs` vs expected `eval/bc/compiler/assign.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/assign.rs` (current: `// port-lint: source src/eval/bc/compiler/assign.rs`)
- **Lint issues:** 1

### 381. layout.identity

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

### 382. opt.constant_folding

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

### 383. record.matcher

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

### 384. eval.bc.compiler.compr

- **Target:** `commonMain.kotlin.io.github.kotlinmania.starlark.eval.bc.compiler.Compr [PROVENANCE-FALLBACK]`
- **Similarity:** 0.93
- **Dependents:** 0
- **Priority Score:** 200.7
- **Functions:** 2/2 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/compiler/compr.rs` vs expected `eval/bc/compiler/compr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/compiler/compr.rs` (current: `// port-lint: source src/eval/bc/compiler/compr.rs`)
- **Lint issues:** 1

### 385. bool.alloc

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

### 386. opt.list_add

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

### 387. opt.types

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

### 388. trace.statics

- **Target:** `trace.Statics [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace/statics.rs` vs expected `tests/derive/trace/statics.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace/statics.rs` (current: `// port-lint: source src/tests/derive/trace/statics.rs`)
- **Lint issues:** 1

### 389. bc.instr

- **Target:** `bc.Instr [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/instr.rs` vs expected `eval/bc/instr.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/instr.rs` (current: `// port-lint: source src/eval/bc/instr.rs`)
- **Lint issues:** 1

### 390. allocator.api

- **Target:** `allocator.Api [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/allocator/api.rs` vs expected `values/layout/heap/allocator/api.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator/api.rs` (current: `// port-lint: source src/values/layout/heap/allocator/api.rs`)
- **Lint issues:** 1

### 391. bc.slow_arg

- **Target:** `bc.SlowArg [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 200.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/bc/slowArg.rs` vs expected `eval/bc/slow_arg.rs`
- **Proposed provenance header:** `// port-lint: source eval/bc/slow_arg.rs` (current: `// port-lint: source src/eval/bc/slowArg.rs`)
- **Lint issues:** 1

### 392. bc.for_loop

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

### 393. wasm

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

### 394. tests.replace_binary

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

### 395. none.globals

- **Target:** `none.Globals [PROVENANCE-FALLBACK]`
- **Similarity:** 0.71
- **Dependents:** 0
- **Priority Score:** 102.9
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/types/none/globals.rs` vs expected `values/types/none/globals.rs`
- **Proposed provenance header:** `// port-lint: source values/types/none/globals.rs` (current: `// port-lint: source src/values/types/none/globals.rs`)
- **Lint issues:** 1

### 396. typing.globals

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

### 397. environment.module_dump

- **Target:** `environment.ModuleDump`
- **Similarity:** 0.76
- **Dependents:** 0
- **Priority Score:** 102.4
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

### 398. compiler.known

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

### 399. eval.params

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

### 400. tests.for_loop

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

### 401. bc.isinstance

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

### 402. tests.bc.call

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

### 403. funcs.globals

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

### 404. freeze.enums

- **Target:** `freeze.Enums [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze/enums.rs` vs expected `tests/derive/freeze/enums.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze/enums.rs` (current: `// port-lint: source src/tests/derive/freeze/enums.rs`)
- **Lint issues:** 1

### 405. typing.mode

- **Target:** `typing.Mode [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `mode.rs` vs expected `typing/mode.rs`
- **Proposed provenance header:** `// port-lint: source typing/mode.rs` (current: `// port-lint: source mode.rs`)
- **Lint issues:** 1

### 406. environment

- **Target:** `starlark.Environment [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/environment.rs` vs expected `environment.rs`
- **Proposed provenance header:** `// port-lint: source environment.rs` (current: `// port-lint: source src/environment.rs`)
- **Lint issues:** 1

### 407. trace.enums

- **Target:** `trace.Enums [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace/enums.rs` vs expected `tests/derive/trace/enums.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace/enums.rs` (current: `// port-lint: source src/tests/derive/trace/enums.rs`)
- **Lint issues:** 1

### 408. typing.call_args

- **Target:** `typing.CallArgs [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/typing/callArgs.rs` vs expected `typing/call_args.rs`
- **Proposed provenance header:** `// port-lint: source typing/call_args.rs` (current: `// port-lint: source src/typing/callArgs.rs`)
- **Lint issues:** 1

### 409. profile.or_instrumentation

- **Target:** `profile.OrInstrumentation [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 100.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/eval/runtime/profile/orInstrumentation.rs` vs expected `eval/runtime/profile/or_instrumentation.rs`
- **Proposed provenance header:** `// port-lint: source eval/runtime/profile/or_instrumentation.rs` (current: `// port-lint: source src/eval/runtime/profile/orInstrumentation.rs`)
- **Lint issues:** 1

### 410. pagable.vtable_register

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

### 411. macros

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

### 412. values.types

- **Target:** `tests.Types [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 3)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `tests/types.rs` vs expected `values/types.rs`
- **Proposed provenance header:** `// port-lint: source values/types.rs` (current: `// port-lint: source tests/types.rs`)
- **Lint issues:** 1

### 413. heap.allocator

- **Target:** `alloc.AllocatorTest [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 4)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `src/values/layout/heap/allocator/alloc/allocator.rs` vs expected `values/layout/heap/allocator.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/allocator.rs` (current: `// port-lint: source src/values/layout/heap/allocator/alloc/allocator.rs`)
- **Lint issues:** 1

### 414. heap.branding

- **Target:** `heap.Branding [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/values/layout/heap/branding.rs` vs expected `values/layout/heap/branding.rs`
- **Proposed provenance header:** `// port-lint: source values/layout/heap/branding.rs` (current: `// port-lint: source src/values/layout/heap/branding.rs`)
- **Lint issues:** 1

### 415. derive.freeze

- **Target:** `derive.Freeze [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/freeze.rs` vs expected `tests/derive/freeze.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/freeze.rs` (current: `// port-lint: source src/tests/derive/freeze.rs`)
- **Lint issues:** 1

### 416. derive.trace

- **Target:** `derive.Trace [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/tests/derive/trace.rs` vs expected `tests/derive/trace.rs`
- **Proposed provenance header:** `// port-lint: source tests/derive/trace.rs` (current: `// port-lint: source src/tests/derive/trace.rs`)
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
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

