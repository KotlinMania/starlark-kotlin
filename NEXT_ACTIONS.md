# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (664/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.76
- **Critical Issues:** 54 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.value
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 178
- **Priority Score:** 32.1
- **Action:** Minor refinements needed

### 2. derive.unpack_value
- **Similarity:** 0.67 (needs 18% improvement)
- **Dependencies:** 51
- **Priority Score:** 16.6
- **Action:** Review and complete missing sections

### 3. typing.starlark_value
- **Similarity:** 0.84 (needs 1% improvement)
- **Dependencies:** 76
- **Priority Score:** 11.9
- **TODOs:** 1
- **Action:** Minor refinements needed

### 4. values.trace
- **Similarity:** 0.78 (needs 7% improvement)
- **Dependencies:** 52
- **Priority Score:** 11.2
- **Action:** Minor refinements needed

### 5. values.alloc_value
- **Similarity:** 0.74 (needs 11% improvement)
- **Dependencies:** 42
- **Priority Score:** 10.8
- **Action:** Review and complete missing sections

### 6. debug
- **Similarity:** 0.85 (needs 0% improvement)
- **Dependencies:** 53
- **Priority Score:** 8.0
- **Action:** Minor refinements needed

### 7. compiler.span
- **Similarity:** 0.77 (needs 8% improvement)
- **Dependencies:** 29
- **Priority Score:** 6.6
- **Action:** Minor refinements needed

### 8. private
- **Similarity:** 0.56 (needs 29% improvement)
- **Dependencies:** 15
- **Priority Score:** 6.5
- **Action:** Deep review - likely missing major functionality

### 9. coerce
- **Similarity:** 0.82 (needs 3% improvement)
- **Dependencies:** 34
- **Priority Score:** 6.1
- **Action:** Minor refinements needed

### 10. collections.symbol
- **Similarity:** 0.62 (needs 23% improvement)
- **Dependencies:** 15
- **Priority Score:** 5.7
- **Action:** Review and complete missing sections

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

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
