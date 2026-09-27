"""Regression tests: manifest dependency-cycle rejection and runtime-tuning
placement (added 2026-08-15).

Defect classes covered:
- ``Manifest.ordered()`` marks a phase visited before recursing, so a
  cyclic ``depends_on`` graph used to yield a silently-wrong order
  instead of a load-time error.
- ``session_dir`` for isabelle_verify sat under ``args:``, which the
  loader routes to ``Phase.args`` — invisible to python runners that
  read ``ctx.config`` — so the declared value was silently ignored.
"""
from __future__ import annotations

import textwrap
from pathlib import Path

import pytest

from web.manifest import Manifest

_REPO_ROOT = Path(__file__).resolve().parents[2]


def _write(tmp_path: Path, body: str) -> Path:
    f = tmp_path / "pipeline.yaml"
    f.write_text(textwrap.dedent(body), encoding="utf-8")
    return f


def test_rejects_two_phase_cycle(tmp_path):
    f = _write(tmp_path, """
        phases:
          a:
            label: "A"
            runner: {kind: python, function: "m:f"}
            depends_on: [b]
          b:
            label: "B"
            runner: {kind: python, function: "m:g"}
            depends_on: [a]
    """)
    with pytest.raises(ValueError, match="cycle"):
        Manifest.load(f)


def test_rejects_self_cycle(tmp_path):
    f = _write(tmp_path, """
        phases:
          a:
            label: "A"
            runner: {kind: python, function: "m:f"}
            depends_on: [a]
    """)
    with pytest.raises(ValueError, match="cycle"):
        Manifest.load(f)


def test_accepts_acyclic_chain(tmp_path):
    f = _write(tmp_path, """
        phases:
          a:
            label: "A"
            runner: {kind: python, function: "m:f"}
          b:
            label: "B"
            runner: {kind: python, function: "m:g"}
            depends_on: [a]
    """)
    m = Manifest.load(f)
    assert [p.id for p in m.ordered()] == ["a", "b"]


def test_real_manifest_loads_and_orders_12_phases():
    m = Manifest.load(_REPO_ROOT / "pipeline.yaml")
    ordered = [p.id for p in m.ordered()]
    assert len(ordered) == 12
    # Verifiers must come after their generators.
    assert ordered.index("fdr4") > ordered.index("m2t")
    assert ordered.index("dafny_verify") > ordered.index("dafny_gen")
    assert ordered.index("isabelle_verify") > ordered.index("isabelle_gen")


def test_isabelle_session_dir_is_runner_visible_config():
    m = Manifest.load(_REPO_ROOT / "pipeline.yaml")
    p = m.phase("isabelle_verify")
    # run_isabelle_verify reads ctx.config['session_dir']; it must be in
    # config (phase-level key), not in args (invisible to python runners).
    assert p.config.get("session_dir", "").endswith("isabelle")
    assert "session_dir" not in p.args