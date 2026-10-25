#!/bin/bash
# replay_published_extractor.sh -- re-run the cold baseline (30 passes) and the
# compile-only ablation stop points (15 passes) through the extractor this
# repository ships, recording each pass under experiments/<collection>-replay/.
#
# Run in YOUR terminal (Docker Desktop running; dafny and refines on PATH):
#     bash experiments/scripts/replay_published_extractor.sh
# One collection:   REPLAY_ONLY=cold  (or ablation)
# One study:        REPLAY_STUDY=lre
# Per-pass cap:     REPLAY_TIMEOUT=3600 (seconds, default)
#
# Resumable: a pass whose phase_status.json exists (and did not time out) is
# skipped. Outputs are written into THIS checkout; commit them with
# forge_commit.sh when the collection is complete.
set -u
cd "$(dirname "$0")/../.."
ROOT=$(pwd)
# Interpreter: the first candidate that imports the dashboard's dependencies
# (the same probe the iteration runner uses); PYTHON=... overrides.
PY=""
for cand in ${PYTHON:-} python3 /opt/homebrew/bin/python3 /usr/local/bin/python3 /usr/bin/python3 "$HOME/.claude-science/conda/envs/python/bin/python"; do
  [ -n "$cand" ] || continue
  if "$cand" -c 'import yaml, jsonschema' >/dev/null 2>&1; then PY=$("$cand" -c 'import os, sys; print(os.path.realpath(sys.executable))'); break; fi
done
[ -n "$PY" ] || { echo "FATAL: no python3 with pyyaml and jsonschema found; run: <python3> -m pip install pyyaml jsonschema"; exit 1; }
# Tool lookup: pipeline.yaml names bare binaries (refines, dafny) and resolves them
# via PATH, so put the FDR4 app's bin dir, Homebrew and the JDK on PATH here, as
# the iteration runner does.
for d in /Applications/FDR4.app/Contents/MacOS /Applications/FDR4.app/Contents/Resources/bin; do [ -x "$d/refines" ] && { export PATH="$d:$PATH"; break; }; done
export PATH="/opt/homebrew/bin:$PATH"
[ -z "${JAVA_HOME:-}" ] && [ -d /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ] && export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
[ -n "${JAVA_HOME:-}" ] && export PATH="$JAVA_HOME/bin:$PATH"
ONLY=${REPLAY_ONLY:-all}
STUDY_FILTER=${REPLAY_STUDY:-}
STUDIES="sranger lre chemical_detector"
LOG="$ROOT/experiments/replay.log"

log(){ echo "$*" | tee -a "$LOG"; }
[ -f pipeline.yaml ] || { echo "FATAL: run from the Forge checkout (pipeline.yaml not found)"; exit 1; }
HEAD=$(git rev-parse --short HEAD 2>/dev/null) || { echo "FATAL: not a git checkout; the replay records the extractor's commit"; exit 1; }
command -v dafny >/dev/null 2>&1 || { echo "FATAL: dafny not on PATH"; exit 1; }
command -v refines >/dev/null 2>&1 || { echo "FATAL: refines (FDR4) not on PATH"; exit 1; }
docker info >/dev/null 2>&1 || { echo "FATAL: Docker is not running (Isabelle runs in the container)"; exit 1; }
[ -n "$(git status --porcelain --untracked-files=no -- forge.transformations forge.dashboard forge.assets/java_codegen_rules.txt 2>/dev/null)" ] && { echo "FATAL: tracked pipeline files are modified; replay only a committed extractor"; exit 1; }

log "=== replay with extractor at $HEAD, $(date '+%F %H:%M:%S'), python $PY ==="

done_pass(){ # <out-dir> -> 0 if recorded and not timed out
  [ -f "$1/phase_status.json" ] && ! grep -q '"driver_timeout": true' "$1/phase_status.json"
}

run_pass(){ # <study> <src> <out> <label>
  if done_pass "$3"; then log "  skip  $4 (recorded)"; return 0; fi
  log "  run   $4"
  "$PY" experiments/scripts/replay_pass.py "$1" "$2" "$3" 2>&1 | tail -3 | sed 's/^/        /' | tee -a "$LOG"
  if done_pass "$3"; then
    log "        -> $(grep -oE '"all_passed": (true|false)' "$3/phase_status.json")"
  elif [ -f "$3/phase_status.json" ]; then
    log "        -> TIMEOUT (environment outcome: rerun, the script resumes here)"
  else
    log "        -> SETUP ERROR: the pass runner did not record anything; stopping. Fix the cause above and rerun."
    exit 1
  fi
}

if [ "$ONLY" = all ] || [ "$ONLY" = cold ]; then
  log "--- cold baseline: 10 cold sources per study, one pass each ---"
  for s in $STUDIES; do
    [ -n "$STUDY_FILTER" ] && [ "$s" != "$STUDY_FILTER" ] && continue
    for k in 1 2 3 4 5 6 7 8 9 10; do
      src=$(ls -d experiments/cold-baseline/$s/run-$k/src/main/java/*/ 2>/dev/null | head -1)
      [ -n "$src" ] || { log "  MISSING cold source: $s run-$k"; continue; }
      run_pass "$s" "$src" "experiments/cold-baseline-replay/$s/run-$k" "cold $s run-$k"
    done
  done
fi

if [ "$ONLY" = all ] || [ "$ONLY" = ablation ]; then
  log "--- compile-only ablation: the recorded stop-point Java of each run, one pass each ---"
  for s in $STUDIES; do
    [ -n "$STUDY_FILTER" ] && [ "$s" != "$STUDY_FILTER" ] && continue
    for r in experiments/convergence/ablation/$s/run-*; do
      [ -d "$r" ] || continue
      k=${r##*-}
      stop=$(ls -d $r/iter-* 2>/dev/null | sort -t- -k2 -n | tail -1)
      [ -n "$stop" ] && [ -d "$stop/java" ] || { log "  MISSING stop-point java: $s run-$k"; continue; }
      out="experiments/convergence/ablation-replay/$s/run-$k"
      run_pass "$s" "$stop/java" "$out" "ablation $s run-$k ($(basename $stop))"
      [ -f "$out/phase_status.json" ] && echo "{\"stop_iteration\": \"$(basename $stop)\", \"recorded_run\": \"$r\"}" > "$out/source.json"
    done
  done
fi

log ""
log "--- aggregate ---"
"$PY" experiments/scripts/replay_aggregate.py 2>&1 | tee -a "$LOG"
# the passes run through the pipeline's live scratch (java.generated.project/src/main/java,
# forge.assets/corrections, forge.transformations/output), which the repository tracks as a
# sample; put it back as committed so the replay leaves no diff behind.
git checkout -q -- forge.assets/corrections java.generated.project/src/main/java 2>/dev/null || true
git clean -fdq -- java.generated.project/src/main/java java.generated.project/.gradle java.generated.project/build 2>/dev/null || true
rm -rf forge.transformations/output
log "=== DONE $(date '+%F %H:%M:%S') ==="
log "  TIMEOUT rows are environment outcomes: rerun the script, it resumes. Paste experiments/replay.log back."
