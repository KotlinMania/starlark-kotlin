#!/usr/bin/env python3
"""
Generate Kotlin stub files from Rust source structure.

This script scans Rust source files and creates corresponding Kotlin stub files
with proper naming conventions:
- Directories: lowercase (values/, typing/)
- Filenames: CamelCase (Value.kt, Ty.kt)
- Packages: lowercase with underscores
- Port-lint headers for provenance tracking

Usage:
    python tools/generate_stubs.py <rust_src_dir> <kotlin_target_dir> <base_package>

Example:
    python tools/generate_stubs.py tmp/starlark/src \
        src/commonMain/kotlin/io/github/kotlinmania/starlark \
        io.github.kotlinmania.starlark
"""

import os
import sys
from pathlib import Path

def snake_to_camel(name):
    """Convert snake_case to CamelCase"""
    parts = name.split('_')
    return ''.join(word.capitalize() for word in parts)

def generate_stubs(rust_src, kotlin_target, base_package):
    """Generate Kotlin stubs from Rust source structure"""
    
    rust_root = Path(rust_src)
    kotlin_root = Path(kotlin_target)
    
    created = 0
    skipped = 0
    
    # Walk Rust source directory
    for rust_file in rust_root.rglob('*.rs'):
        # Get path relative to rust_src
        rel_path = rust_file.relative_to(rust_root)
        
        # Build Kotlin path
        # Example: values/layout.rs -> values/Layout.kt
        parts = list(rel_path.with_suffix('').parts)
        
        # Keep dirs lowercase, CamelCase filename
        kotlin_parts = parts[:-1] + [snake_to_camel(parts[-1])]
        kotlin_rel = Path(*kotlin_parts).with_suffix('.kt')
        kotlin_file = kotlin_root / kotlin_rel
        
        # Build package name
        if len(parts) > 1:
            package = base_package + '.' + '.'.join(parts[:-1])
        else:
            package = base_package
        
        # Skip if exists
        if kotlin_file.exists():
            skipped += 1
            continue
        
        # Create directory
        kotlin_file.parent.mkdir(parents=True, exist_ok=True)
        
        # Create stub
        with open(kotlin_file, 'w') as f:
            f.write(f"// port-lint: source {rel_path.as_posix()}\n")
            f.write(f"package {package}\n")
            f.write(f"\n")
        
        created += 1
    
    print(f"\nGenerated {created} stub files")
    print(f"Skipped {skipped} existing files")
    print(f"\nStubs ready for porting!")

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print(__doc__)
        sys.exit(1)
    
    generate_stubs(sys.argv[1], sys.argv[2], sys.argv[3])
