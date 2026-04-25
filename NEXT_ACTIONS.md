# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 81.4% (396/468 files)
- **Matched Files:** 381
- **Average Similarity:** 0.64
- **Critical Issues:** 125 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. coerce
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 34
- **Priority Score:** 195.1
- **Symbol Deficit:** 11 (functions: 4, types: 7)
- **Missing Tests:** 4 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 2. int.inline_int
- **Similarity:** 0.67 (needs 18% improvement)
- **Dependencies:** 11
- **Priority Score:** 168.8
- **Symbol Deficit:** 11 (functions: 8, types: 3)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 3. values.value_of_unchecked
- **Similarity:** 0.84 (needs 1% improvement)
- **Dependencies:** 20
- **Priority Score:** 147.3
- **Symbol Deficit:** 9 (functions: 5, types: 4)
- **Missing Tests:** 6 of 6 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 4. values.frozen_ref
- **Similarity:** 0.85 (needs 0% improvement)
- **Dependencies:** 27
- **Priority Score:** 35.9
- **Symbol Deficit:** 2 (functions: 0, types: 2)
- **Action:** Minor refinements needed

### 5. layout.const_frozen_string
- **Similarity:** 0.70 (needs 15% improvement)
- **Dependencies:** 12
- **Priority Score:** 34.1
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 6. util.refcell
- **Similarity:** 0.54 (needs 31% improvement)
- **Dependencies:** 20
- **Priority Score:** 23.1
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 1 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 7. values.error
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 17
- **Priority Score:** 22.9
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 8. values.freeze
- **Similarity:** 0.78 (needs 7% improvement)
- **Dependencies:** 42
- **Priority Score:** 21.6
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Minor refinements needed

### 9. util.arc_str
- **Similarity:** 0.67 (needs 18% improvement)
- **Dependencies:** 21
- **Priority Score:** 21.3
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Review and complete missing sections

### 10. debug
- **Similarity:** 0.05 (needs 80% improvement)
- **Dependencies:** 53
- **Priority Score:** 19.0
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **layout.heap** (109 deps)
   - Path: `values/layout/heap.rs`
   - Essential for 109 other files

2. **assert** (84 deps)
   - Path: `assert.rs`
   - Essential for 84 other files

3. **derive.unpack_value** (51 deps)
   - Path: `tests/derive/unpack_value.rs`
   - Essential for 51 other files

4. **types.dict** (12 deps)
   - Path: `values/types/dict.rs`
   - Essential for 12 other files

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
