#!/bin/bash
# Re-run the three requirements-level checks on the campaign's survivors, each with --check, then
# recompute the combined totals.  Run from the repository root; needs Python 3 with numpy and pandas.
set -eu
PY=${PY:-python3}
$PY experiments/requirements-conformance/run_reqconf.py --check
$PY experiments/requirements-conformance/multi-study/score_survivors.py --out /tmp/multi_check.csv --check
$PY experiments/blind-spec/blindspec3_score.py --check
$PY experiments/mutation-testing/requirements-scoring/summarize.py
