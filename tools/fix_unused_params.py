#!/usr/bin/env python
"""
Batch-rename unused Kotlin parameters to a `_` prefix to mirror Rust convention.

The ast_distance lint reports `unused_param` errors when a Kotlin port keeps a
non-underscore parameter name that the Rust source already underscored (e.g.
`_heap`). The lint suppresses Kotlin params iff:
  - the param starts with `_`, AND
  - the same `_param` appears in the corresponding Rust source.

So for each lint error, if the Rust source has `_paramname`, we rename
`paramname:` → `_paramname:` at the reported function line. We do not rename
params that aren't underscored in Rust (those mark real translation gaps).

Usage:
    python tools/fix_unused_params.py
"""
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LINT_BIN = ROOT / "tools" / "ast_distance" / "ast_distance"

LINT_RE = re.compile(
    r"^(?P<file>[^:\n]+):(?P<line>\d+): unused_param: Unused parameter '(?P<param>[^']+)' in function '(?P<fn>[^']+)'",
    re.MULTILINE,
)


def header_to_rust_path(kt_text: str) -> Path | None:
    m = re.match(r"//\s*port-lint:\s*source\s+(\S+)", kt_text)
    if not m:
        return None
    return ROOT / "tmp" / "starlark" / m.group(1)


def rust_has_underscored(rust_text: str, param: str) -> bool:
    return re.search(rf"\b_{re.escape(param)}\b", rust_text) is not None


def fix_one(kt_path: Path, line_num: int, param: str) -> bool:
    """Returns True if a change was made.

    The lint reports the line of the `fun` declaration, but the parameter
    declaration may be on a subsequent line for multi-line signatures.
    Scan from `line_num` until the function's opening `{`, looking for
    `<param>:` to rename.
    """
    text = kt_path.read_text()
    rs_path = header_to_rust_path(text)
    if not rs_path or not rs_path.exists():
        return False
    rs_text = rs_path.read_text()
    if not rust_has_underscored(rs_text, param):
        # Rust source uses the param actively; keep the gap visible.
        return False

    lines = text.splitlines(keepends=True)
    if line_num - 1 >= len(lines):
        return False

    pat = re.compile(rf"\b{re.escape(param)}\s*:")
    # Walk forward from the lint-reported line until the function body opens
    # (a `{` outside parentheses). Cap at 60 lines just in case.
    end = min(len(lines), line_num - 1 + 60)
    paren_depth = 0
    saw_paren = False
    for i in range(line_num - 1, end):
        line = lines[i]
        for ch in line:
            if ch == "(":
                paren_depth += 1
                saw_paren = True
            elif ch == ")":
                paren_depth -= 1
        m = pat.search(line)
        if m:
            new = pat.sub(f"_{param}:", line, count=1)
            if new != line:
                lines[i] = new
                kt_path.write_text("".join(lines))
                return True
        # Stop once we've left the parameter list and entered the body.
        if saw_paren and paren_depth == 0 and "{" in line:
            break
    return False


def main():
    out = subprocess.run(
        [str(LINT_BIN), "--lint", "src/commonMain/kotlin"],
        cwd=ROOT,
        capture_output=True,
        text=True,
    )
    fixed = 0
    skipped = 0
    seen = set()
    for m in LINT_RE.finditer(out.stdout):
        file = ROOT / m.group("file")
        line = int(m.group("line"))
        param = m.group("param")
        key = (file, line, param)
        if key in seen:
            continue
        seen.add(key)
        if fix_one(file, line, param):
            fixed += 1
        else:
            skipped += 1
    print(f"fixed={fixed}  skipped={skipped}")


if __name__ == "__main__":
    main()
