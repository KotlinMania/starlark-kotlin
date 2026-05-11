# STARLARK-KOTLIN SWARM PORTING - READY TO BEGIN

## Infrastructure Status: ✅ COMPLETE

### Repository Setup
- ✅ 468 Rust source files in `tmp/starlark/`
- ✅ 468 Kotlin stub files with CamelCase names
- ✅ Port-lint headers in every file (390/390 matched)
- ✅ Proper package structure (lowercase dirs, CamelCase files)
- ✅ Task queue initialized with 468 tasks

### Tools Ready
- ✅ ast_distance binary with port-lint support
- ✅ Task management with file locking
- ✅ Quality verification (AST similarity scoring)
- ✅ Stub generator (`tools/generate_stubs.py`)

### Documentation
- ✅ CLAUDE.md - Agent instructions
- ✅ AGENTS.md - Porting patterns + workflow
- ✅ PORTING.md - Quick reference
- ✅ TODO policy - No TODOs without approval

## CRITICAL RULES FOR AGENTS

### 1. ALWAYS Use Task Assignment System
```bash
# Get your task
./tools/ast_distance/ast_distance --assign tasks.json <your-agent-id>

# NEVER port files randomly!
# NEVER skip the task system!
```

### 2. Work ONLY on Assigned Task
- One task at a time
- Complete it or release it
- Don't pick files manually

### 3. Port-Lint Header Required
Every file MUST start with:
```kotlin
// port-lint: source <path-from-tmp/starlark>
package io.github.kotlinmania.starlark_kotlin.<module>
```

### 4. Verify Quality Before Completing
```bash
./tools/ast_distance/ast_distance \
  tmp/starlark/<source>.rs rust \
  src/commonMain/kotlin/io/github/kotlinmania/starlark_kotlin/<target>.kt kotlin
```
**Target: ≥ 0.85 similarity**

### 5. NO TODOs Without Approval
Ask Sydney before adding ANY TODO comment.

### 6. Complete Task When Done
```bash
./tools/ast_distance/ast_distance --complete tasks.json <qualified.name>
```

## Top Priority Tasks (by dependency count)

1. typing.ty (107 deps)
2. typing.starlark_value (67 deps)
3. assert (53 deps)
4. debug (53 deps)
5. values.trace (46 deps)
6. values.alloc_value (38 deps)
7. coerce (33 deps)
8. values.freeze (32 deps)
9. values.frozen_ref (26 deps)
10. util.arc_str (20 deps)

## Naming Conventions

- **Directories:** lowercase (`values/`, `typing/`, `environment/`)
- **Filenames:** CamelCase (`Value.kt`, `Ty.kt`, `Globals.kt`)
- **Packages:** lowercase with underscores preserved
- **Types:** PascalCase
- **Functions/Variables:** camelCase

## Commit Messages

NO AI attribution! Follow Sydney's style:
- Clear, descriptive messages
- Focus on what changed and why
- No "Co-Authored-By" lines
- No emoji or robot references

Example:
```
Port typing.ty module from Rust

Implement type inference and checking logic with semantic parity.
Includes type representations, constraints, and unification.
AST similarity: 0.89
```

## Getting Started

Each agent should:

1. **Get assignment:**
   ```bash
   ./tools/ast_distance/ast_distance --assign tasks.json agent-<id>
   ```

2. **Read source file** at the path shown

3. **Port to Kotlin** following AGENTS.md patterns

4. **Verify similarity** (aim for ≥ 0.85)

5. **Complete task:**
   ```bash
   ./tools/ast_distance/ast_distance --complete tasks.json <qualified.name>
   ```

6. **Repeat** from step 1

## Ready to Storm the Castle! 🏰⚔️

468 files to port. Let's do this systematically and with high quality!
