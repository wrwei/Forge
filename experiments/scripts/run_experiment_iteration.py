#!/usr/bin/env python3
"""Drive one pipeline iteration end-to-end (all phases in order).

Kept at this path because the run scripts call it; the implementation is
``forge_pipeline.cli`` (``python -m forge_pipeline run-iteration``).

Usage: python experiments/scripts/run_experiment_iteration.py [--phases ...]
"""
from __future__ import annotations

import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO_ROOT / "forge.pipeline"))

from forge_pipeline.cli import main  # noqa: E402

if __name__ == "__main__":
    sys.exit(main(["run-iteration", *sys.argv[1:]]))
