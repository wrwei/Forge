#!/usr/bin/env python3
"""Apply the pipeline's CSP type-range corrections to an explicit csp-gen tree.

Usage: python3 scripts/apply_corrections_standalone.py <csp-gen-dir>

Reuses forge.dashboard/web/csp_corrections.py's own parsing/merge/naming logic
(_parse_generate_blocks, _merge, _build_nametype, the block->core_* name map,
DEFAULT_TYPE_RANGES, corrections/type_ranges.json, corrections/csp_overrides.csp)
so the transform is the pipeline's, not a reimplementation. The only difference
from apply_csp_corrections() is that the target file is given explicitly instead
of resolved through pipeline.yaml — that function is config-bound, which is why
this wrapper exists (campaign mutant trees live outside ${output_dir}).

Rewrites <dir>/instantiations.csp and, if present, <dir>/timed/instantiations.csp
(the generator emits the timed sibling for clock-using controllers, and the
pipeline corrects both).
"""
import json
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[3]   # experiments/mutation-testing/scripts -> repository root

# Do NOT `import csp_corrections`: on the host's system python3 the module
# fails at import twice over — it does `import yaml` at module level (no PyYAML
# on the host), and it uses PEP 604 annotations (`Path | None`) evaluated at
# def time, which need python >= 3.10 (host run showed both, in that order).
# We need only four small, dependency-free pieces of it. Extract exactly those
# from the SOURCE at runtime and exec them with `from __future__ import
# annotations` (defers all annotation evaluation), so the logic stays the
# pipeline's own — not a copy that can drift — while being runnable on the
# host's older interpreter.
import ast
import logging
import re  # noqa: F401  (needed by the extracted functions' namespace)

_WANTED_FUNCS = {"_parse_generate_blocks", "_merge", "_build_nametype"}
_WANTED_ASSIGNS = {"DEFAULT_TYPE_RANGES"}


def _load_pipeline_pieces():
    src_path = REPO / "forge.dashboard" / "web" / "csp_corrections.py"
    src = src_path.read_text()
    tree = ast.parse(src)
    segments = []
    found = set()
    for node in tree.body:
        if isinstance(node, ast.FunctionDef) and node.name in _WANTED_FUNCS:
            segments.append(ast.get_source_segment(src, node))
            found.add(node.name)
        elif isinstance(node, ast.Assign):
            for t in node.targets:
                if isinstance(t, ast.Name) and t.id in _WANTED_ASSIGNS:
                    segments.append(ast.get_source_segment(src, node))
                    found.add(t.id)
        elif isinstance(node, ast.AnnAssign):
            # DEFAULT_TYPE_RANGES is declared with an annotation
            # (`DEFAULT_TYPE_RANGES: dict[str, str] = {...}`), which parses as
            # AnnAssign, not Assign.
            t = node.target
            if isinstance(t, ast.Name) and t.id in _WANTED_ASSIGNS and node.value is not None:
                segments.append(ast.get_source_segment(src, node))
                found.add(t.id)
    missing = (_WANTED_FUNCS | _WANTED_ASSIGNS) - found
    if missing:
        raise RuntimeError(
            f"csp_corrections.py no longer defines {sorted(missing)} at module "
            f"level — update this wrapper's extraction list.")
    ns = {"re": re, "logger": logging.getLogger("csp_corrections_standalone")}
    code = "from __future__ import annotations\n" + "\n\n".join(segments)
    exec(compile(code, str(src_path), "exec"), ns)
    return ns


_ns = _load_pipeline_pieces()


class cc:  # same call-sites as the previous `import csp_corrections as cc`
    _parse_generate_blocks = staticmethod(_ns["_parse_generate_blocks"])
    _merge = staticmethod(_ns["_merge"])
    _build_nametype = staticmethod(_ns["_build_nametype"])
    DEFAULT_TYPE_RANGES = _ns["DEFAULT_TYPE_RANGES"]

CSP_NAMES = {
    "nat": "core_nat", "int": "core_int", "real": "core_real",
    "core_clock_type": "core_clock_type", "string": "core_string",
}


def build_overrides() -> dict[str, list[str]]:
    overrides = {n: [d] for n, d in cc.DEFAULT_TYPE_RANGES.items()}
    ranges_file = REPO / "forge.dashboard" / "corrections" / "type_ranges.json"
    if ranges_file.exists():
        for name, cfg in json.loads(ranges_file.read_text()).items():
            if name in CSP_NAMES:
                overrides[name] = [cc._build_nametype(
                    CSP_NAMES[name], cfg["lower"], cfg["upper"])]
    ov_file = REPO / "forge.dashboard" / "corrections" / "csp_overrides.csp"
    if ov_file.exists():
        for name, lines in cc._parse_generate_blocks(ov_file.read_text()).items():
            overrides[name] = lines
    return overrides


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    cspgen = Path(sys.argv[1])
    overrides = build_overrides()
    changed = 0
    for inst in (cspgen / "instantiations.csp", cspgen / "timed" / "instantiations.csp"):
        if not inst.exists():
            continue
        before = inst.read_text()
        after = cc._merge(before, overrides)
        if after != before:
            inst.write_text(after)
            changed += 1
        print(f"{inst}: {'corrected' if after != before else 'already canonical'}")
    return 0 if changed >= 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
