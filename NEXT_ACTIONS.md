# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (569/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.53
- **Critical Issues:** 251 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. coerce
- **Similarity:** 0.12 (needs 73% improvement)
- **Dependencies:** 34
- **Priority Score:** 203.9
- **Symbol Deficit:** 11 (functions: 4, types: 7)
- **Missing Tests:** 4 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 2. values.value_of_unchecked
- **Similarity:** 0.55 (needs 30% improvement)
- **Dependencies:** 20
- **Priority Score:** 167.7
- **Symbol Deficit:** 10 (functions: 6, types: 4)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 3. int.inline_int
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 11
- **Priority Score:** 154.0
- **Symbol Deficit:** 10 (functions: 7, types: 3)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 4. environment.globals
- **Similarity:** 0.76 (needs 9% improvement)
- **Dependencies:** 21
- **Priority Score:** 117.1
- **Symbol Deficit:** 7 (functions: 6, types: 1)
- **Missing Tests:** 5 of 5 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 5. runtime.arguments
- **Similarity:** 0.71 (needs 14% improvement)
- **Dependencies:** 25
- **Priority Score:** 103.9
- **Symbol Deficit:** 6 (functions: 6, types: 0)
- **Missing Tests:** 4 of 4 `#[test]` functions have no Kotlin counterpart
- **Action:** Review and complete missing sections

### 6. derive.unpack_value
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 51
- **Priority Score:** 55.6
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 7. environment.methods
- **Similarity:** 0.80 (needs 5% improvement)
- **Dependencies:** 17
- **Priority Score:** 50.2
- **Symbol Deficit:** 3 (functions: 2, types: 1)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Minor refinements needed

### 8. layout.const_frozen_string
- **Similarity:** 0.00 (needs 85% improvement)
- **Dependencies:** 12
- **Priority Score:** 43.1
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
- **Action:** Deep review - likely missing major functionality

### 9. values.frozen_ref
- **Similarity:** 0.85 (needs 0% improvement)
- **Dependencies:** 27
- **Priority Score:** 35.9
- **Symbol Deficit:** 2 (functions: 0, types: 2)
- **Action:** Minor refinements needed

### 10. layout.avalue
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 14
- **Priority Score:** 35.5
- **Symbol Deficit:** 2 (functions: 2, types: 0)
- **Missing Tests:** 2 of 2 `#[test]` functions have no Kotlin counterpart
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
