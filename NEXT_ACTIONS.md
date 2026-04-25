# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (575/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.66
- **Critical Issues:** 166 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. values.value_of_unchecked
- **Similarity:** 0.77 (needs 8% improvement)
- **Dependencies:** 20
- **Priority Score:** 164.4
- **Symbol Deficit:** 10 (functions: 6, types: 4)
- **Missing Tests:** 6 of 6 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 2. int.inline_int
- **Similarity:** 0.70 (needs 15% improvement)
- **Dependencies:** 11
- **Priority Score:** 153.5
- **Symbol Deficit:** 10 (functions: 7, types: 3)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 3. coerce
- **Similarity:** 0.61 (needs 24% improvement)
- **Dependencies:** 34
- **Priority Score:** 58.2
- **Symbol Deficit:** 3 (functions: 1, types: 2)
- **Missing Tests:** 1 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 4. derive.unpack_value
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 51
- **Priority Score:** 43.3
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 5. values.frozen_ref
- **Similarity:** 0.85 (needs 0% improvement)
- **Dependencies:** 27
- **Priority Score:** 35.9
- **Symbol Deficit:** 2 (functions: 0, types: 2)
- **Action:** Minor refinements needed

### 6. layout.const_frozen_string
- **Similarity:** 0.70 (needs 15% improvement)
- **Dependencies:** 12
- **Priority Score:** 34.1
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 7. util.refcell
- **Similarity:** 0.54 (needs 31% improvement)
- **Dependencies:** 20
- **Priority Score:** 23.1
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Missing Tests:** 1 of 1 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 8. values.error
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 17
- **Priority Score:** 22.9
- **Symbol Deficit:** 1 (functions: 1, types: 0)
- **Action:** Deep review - likely missing major functionality

### 9. values.freeze
- **Similarity:** 0.78 (needs 7% improvement)
- **Dependencies:** 42
- **Priority Score:** 21.6
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Minor refinements needed

### 10. util.arc_str
- **Similarity:** 0.67 (needs 18% improvement)
- **Dependencies:** 21
- **Priority Score:** 21.3
- **Symbol Deficit:** 1 (functions: 0, types: 1)
- **Action:** Review and complete missing sections

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

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
./ast_distance --init-tasks ../../tmp/starlark rust ../../src kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
