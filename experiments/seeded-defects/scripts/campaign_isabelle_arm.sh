#!/bin/bash
# Seeded-defect campaign — Isabelle arm (R1-M1).
#
# Run in YOUR terminal (Docker's socket is not reachable from the assistant's
# sandbox):
#
#   bash scripts/campaign_isabelle_arm.sh
#
# Inputs (staged by the sandbox campaign harness):
#   /tmp/campaign/outputs/<mutant_id>/isabelle/{<Stm>_Beh.thy, ROOT}
#   /tmp/campaign/thy_changed.txt   — the mutants whose theory differs from
#                                     the unmutated baseline (only these need
#                                     a build; identical theories inherit the
#                                     baseline verdict)
#   /tmp/campaign/baseline/{lre,sranger,chemical_detector}/isabelle/
#
# If /tmp/campaign is missing (e.g. after a reboot), re-materialize it first:
#   mkdir -p /tmp/campaign && tar -xzf <campaign_staged_trees.tar.gz> -C /tmp/campaign
#
# Output: /tmp/campaign/isabelle-arm/results.csv
#   mutant_id,exit,finished_sessions,error_lines,seconds,outcome
# plus one log per build. Re-running SKIPS mutants that already have a log
# (delete the log to force a re-build), so the batch is resumable.
#
# Expected runtime: ~1-3 min per session build; 86 changed mutants ≈ 2-4 h.
# The three baseline builds run first as a sanity gate.

set -u
REPO="$(cd "$(dirname "$0")/.." && pwd)"
ISA="$REPO/scripts/isabelle-docker.sh"
CAMP=/tmp/campaign
OUT=$CAMP/isabelle-arm
LIST=$CAMP/thy_changed.txt
mkdir -p "$OUT"
RES="$OUT/results.csv"
[ -f "$RES" ] || echo "mutant_id,exit,finished_sessions,error_lines,seconds,outcome" > "$RES"
log () { echo "$@"; }

[ -x "$ISA" ] || { echo "missing $ISA"; exit 2; }
[ -f "$LIST" ] || { echo "missing $LIST — stage the campaign first"; exit 2; }

log "=== toolchain ==="
# Docker preflight. isabelle-docker.sh needs a running daemon; without it every
# build exits 2 in under a second and the run dies three baselines later with a
# message about verification. Fail here instead, with the actual instruction.
if ! docker info >/dev/null 2>&1; then
  log "!! Docker is not running — start Docker Desktop, wait for the whale icon"
  log "   to stop animating, then re-run this script. (The CyPhyAssure Isabelle"
  log "   distribution is Linux-x86_64-only and runs in a container here.)"
  exit 2
fi
"$ISA" version 2>&1 | tail -1

# Budget. Reference (pre-fix theories, measured 2026-08-15): LRE 1:14,
# SRanger 0:22, chemical_detector 0:43 of session proof time. A MUTANT that
# breaks a proof does not fail fast — Isabelle searches until the per-goal
# timeout — so an uncapped sweep of 86 mutants can run many hours on the
# failures alone. GOAL_TIMEOUT bounds each proof obligation; WALL_CAP bounds
# the whole build so one pathological mutant cannot eat the sitting. Both are
# generous against the reference times (4x and ~7x) and overridable.
GOAL_TIMEOUT=${GOAL_TIMEOUT:-300}
WALL_CAP=${WALL_CAP:-600}

build_one () {   # build_one <label> <dir-containing-ROOT>
  local label="$1" dir="$2"
  if [ ! -f "$dir/ROOT" ]; then log "$label: no ROOT at $dir — skipped"; return; fi
  if [ -f "$OUT/$label.log" ]; then log "$label: log exists — skipped (resume)"; return; fi
  # Scratch copy: isabelle build drops logs beside the session it is given,
  # and the staged trees must stay pristine for re-runs.
  local work="$OUT/work-$label"
  rm -rf "$work"; mkdir -p "$work"
  cp "$dir"/*.thy "$dir"/ROOT "$work"/ 2>/dev/null
  local t0 t1 rc fin err outcome
  t0=$(date +%s)
  ( cd "$work" && perl -e 'alarm shift; exec @ARGV' "$WALL_CAP" \
      "$ISA" build -D . -v -o "timeout=$GOAL_TIMEOUT" ) > "$OUT/$label.log" 2>&1
  rc=$?
  t1=$(date +%s)
  fin=$(grep -c "^Finished " "$OUT/$label.log" 2>/dev/null || true)
  err=$(grep -c "^\*\*\*" "$OUT/$label.log" 2>/dev/null || true)
  # Classify. A timeout is NOT the same as a refuted proof: it means the
  # obligation did not close within budget. Kept as its own category so the
  # kill table never reports "killed" for what is really "inconclusive".
  if   [ "$rc" -eq 0 ] && [ "${fin:-0}" -gt 0 ];        then outcome=verified
  elif grep -q "^\*\*\* Timeout" "$OUT/$label.log" 2>/dev/null; then outcome=timeout
  elif [ "$rc" -eq 142 ] || [ "$rc" -eq 124 ];          then outcome=wall_capped
  elif [ "${err:-0}" -gt 0 ];                           then outcome=proof_failed
  else                                                       outcome=unknown
  fi
  echo "$label,$rc,${fin:-0},${err:-0},$((t1-t0)),$outcome" >> "$RES"
  log "$label: $outcome (exit=$rc finished=$fin errors=$err, $((t1-t0))s)"
  rm -rf "$work"
}

log ""
log "=== 1. baselines (sanity gate — all three must finish with 0 errors) ==="
for s in lre sranger chemical_detector; do
  build_one "baseline-$s" "$CAMP/baseline/$s/isabelle"
done
# HARD GATE. Without this the "sanity gate" was only a label: a broken harness
# would run all 86 mutants and produce a column of failures indistinguishable
# from real kills. The FDR4 arm needed exactly this guard twice.
for s in lre sranger chemical_detector; do
  row=$(grep "^baseline-$s," "$RES" | tail -1)
  oc=$(echo "$row" | cut -d, -f6)
  if [ "$oc" != "verified" ]; then
    log ""
    log "!! ABORT: baseline-$s is '$oc', not 'verified' — harness problem, not a"
    log "   verification result. The unmutated theory must build before mutant"
    log "   outcomes mean anything. Log: $OUT/baseline-$s.log"
    tail -5 "$OUT/baseline-$s.log" 2>/dev/null | sed 's/^/     /'
    exit 4
  fi
done
log "baselines OK — all three verified; proceeding to mutants."

log ""
log "=== 2. mutants with changed theories ($(wc -l < "$LIST" | tr -d ' ') of them) ==="
while IFS= read -r mid; do
  [ -n "$mid" ] || continue
  build_one "$mid" "$CAMP/outputs/$mid/isabelle"
done < "$LIST"

log ""
log "==== results: $RES ===="
log "Done. Tell the assistant results are in $OUT/"
