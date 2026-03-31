# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Current Progress:** 100.0% (496/470 files)
- **Matched Files:** 470
- **Average Similarity:** 0.53
- **Critical Issues:** 229 files with <0.60 similarity

## Priority 1: Fix Incomplete High-Dependency Files

### 1. layout.heap
- **Similarity:** 0.18 (needs 67% improvement)
- **Dependencies:** 109
- **Priority Score:** 89.3
- **Action:** Deep review - likely missing major functionality

### 2. layout.value
- **Similarity:** 0.77 (needs 8% improvement)
- **Dependencies:** 178
- **Priority Score:** 41.5
- **Action:** Minor refinements needed

### 3. values.freeze
- **Similarity:** 0.06 (needs 79% improvement)
- **Dependencies:** 42
- **Priority Score:** 39.4
- **Action:** Deep review - likely missing major functionality

### 4. assert
- **Similarity:** 0.63 (needs 22% improvement)
- **Dependencies:** 84
- **Priority Score:** 31.3
- **Action:** Review and complete missing sections

### 5. values.trace
- **Similarity:** 0.43 (needs 42% improvement)
- **Dependencies:** 52
- **Priority Score:** 29.8
- **Action:** Deep review - likely missing major functionality

### 6. derive.unpack_value
- **Similarity:** 0.50 (needs 35% improvement)
- **Dependencies:** 51
- **Priority Score:** 25.3
- **Action:** Deep review - likely missing major functionality

### 7. params.display
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.2
- **Action:** Review and complete missing sections

### 8. typing.starlark_value
- **Similarity:** 0.68 (needs 17% improvement)
- **Dependencies:** 76
- **Priority Score:** 24.0
- **Action:** Review and complete missing sections

### 9. typing.ty
- **Similarity:** 0.79 (needs 6% improvement)
- **Dependencies:** 109
- **Priority Score:** 22.7
- **Action:** Minor refinements needed

### 10. coerce
- **Similarity:** 0.60 (needs 25% improvement)
- **Dependencies:** 34
- **Priority Score:** 13.5
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
./ast_distance --init-tasks ../../tmp/starlark rust ../../src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
