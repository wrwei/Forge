"""Unified verification feedback writer.

Produces one Markdown + one JSON file per pipeline phase run in
``forge.assets/corrections/`` so the interactive refinement loop
(Claude Code, ``/fix-from-feedback``) has a single place to read.

Layout:

- :mod:`.base` — schema (``TraceRef``, ``Issue``, ``Feedback``), trace
  loading, diff/serialisation/rendering, and the common ``write_feedback``
- :mod:`.fdr4` — FDR4 (CSP) classifier + builder
- :mod:`.dafny` — Dafny (DBC / Z3) classifier + builders
- :mod:`.isabelle` — Isabelle/UTP Z-Machine classifier + builder
- :mod:`.gradle` — gradle-driven phases (T2M, M2M, M2T, dafny_gen,
  isabelle_gen, compile) plus the structural-linter preflight
- :mod:`.coverage` — bidirectional requirement ↔ Java trace check

Schema (JSON):

    {
      "phase": "fdr4",
      "label": "Formal Verification (FDR4)",
      "status": "passed" | "failed" | "stopped",
      "summary": "one-sentence outcome",
      "issues": [
        {
          "kind": "deadlock" | "divergence" | "nondeterminism"
                   | "parse_error" | "dafny_postcondition" | ...,
          "title": "short heading",
          "raw": "tool-level message, trimmed",
          "fix_directive": "prescriptive instruction",
          "java_trace": [
            {"file": "...", "line_start": n, "line_end": n,
             "element": "...", "robochart_type": "...",
             "robochart_element": "...", "requirement_ids": [...]}
          ]
        }
      ],
      "files_to_review": [{"file": "<Controller>.java",
                           "requirement_ids": ["<study>-FR1", ...]}],
      "next_step": "..."
    }

The Markdown mirrors the JSON with human-readable headings.
"""
from __future__ import annotations

# Schema and common writer
from .base import (
    THRASHING_THRESHOLD,
    Feedback,
    Issue,
    TraceRef,
    load_dafny_trace,
    load_trace,
    write_feedback,
    # Fix 1: requirement-trace amplification helpers
    load_codegen_trace,
    load_requirements,
    lookup_requirements_for_java,
    amplify_directive_with_requirements,
    populate_trace_refs_with_requirements,
)

# Per-phase builders (called by web.bridge)
from .coverage import build_coverage_feedback, build_coverage_issues
from .dafny import build_dafny_feedback, build_dafny_issues
from .fdr4 import build_fdr4_feedback
from .gradle import (
    build_gradle_issues,
    build_gradle_phase_feedback,
    build_preflight_feedback,
)
from .isabelle import build_isabelle_feedback, build_isabelle_issues

__all__ = [
    # Schema
    "THRASHING_THRESHOLD",
    "Feedback",
    "Issue",
    "TraceRef",
    # Trace loaders + writer
    "load_dafny_trace",
    "load_trace",
    "write_feedback",
    # Fix 1: requirement amplification
    "load_codegen_trace",
    "load_requirements",
    "lookup_requirements_for_java",
    "amplify_directive_with_requirements",
    "populate_trace_refs_with_requirements",
    # Per-phase builders
    "build_coverage_feedback",
    "build_coverage_issues",
    "build_dafny_feedback",
    "build_dafny_issues",
    "build_fdr4_feedback",
    "build_gradle_issues",
    "build_gradle_phase_feedback",
    "build_isabelle_feedback",
    "build_isabelle_issues",
    "build_preflight_feedback",
]
