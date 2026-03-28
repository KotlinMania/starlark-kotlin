# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 99.1% (516/470 files)
- **Matched Files:** 466
- **Average Similarity:** 0.44
- **Critical Issues:** 326 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.heap
- **Similarity:** 0.28 (needs 57% improvement)
- **Dependencies:** 109
- **Priority Score:** 78.5
- **Action:** Deep review - likely missing major functionality

### 2. typing.starlark_value
- **Similarity:** 0.01 (needs 84% improvement)
- **Dependencies:** 76
- **Priority Score:** 75.4
- **TODOs:** 1
- **Action:** Deep review - likely missing major functionality

### 3. layout.value
- **Similarity:** 0.59 (needs 26% improvement)
- **Dependencies:** 178
- **Priority Score:** 73.3
- **Action:** Deep review - likely missing major functionality

### 4. values.trace
- **Similarity:** 0.16 (needs 69% improvement)
- **Dependencies:** 52
- **Priority Score:** 43.7
- **Action:** Deep review - likely missing major functionality

### 5. values.freeze
- **Similarity:** 0.07 (needs 78% improvement)
- **Dependencies:** 42
- **Priority Score:** 39.1
- **Action:** Deep review - likely missing major functionality

### 6. typing.ty
- **Similarity:** 0.65 (needs 20% improvement)
- **Dependencies:** 109
- **Priority Score:** 37.8
- **Action:** Review and complete missing sections

### 7. coerce
- **Similarity:** 0.14 (needs 71% improvement)
- **Dependencies:** 34
- **Priority Score:** 29.1
- **Action:** Deep review - likely missing major functionality

### 8. derive.unpack_value
- **Similarity:** 0.51 (needs 34% improvement)
- **Dependencies:** 51
- **Priority Score:** 25.2
- **Action:** Deep review - likely missing major functionality

### 9. params.display
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.4
- **Action:** Review and complete missing sections

### 10. runtime.evaluator
- **Similarity:** 0.59 (needs 26% improvement)
- **Dependencies:** 56
- **Priority Score:** 23.0
- **Action:** Deep review - likely missing major functionality

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

1. **assert** (84 deps)
   - Path: `src/assert.rs`
   - Essential for 84 other files

2. **debug** (53 deps)
   - Path: `src/debug.rs`
   - Essential for 53 other files

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
