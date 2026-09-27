#!/bin/bash
# Seeded-defect campaign — FDR4 arm (R1-M1).
#
# Run in YOUR terminal (the FDR licence lives in a TCC-protected store the
# assistant's sandbox cannot read):
#
#   bash scripts/campaign_fdr4_arm.sh
#
# Inputs (staged by the sandbox campaign harness; csp-gen already run by the
# vendored RoboChart generator, so this script only needs refines):
#   /tmp/campaign/outputs/<mutant_id>/csp-gen/defs/*_coreassertions.csp
#   /tmp/campaign/rct_changed.txt  — mutants whose .rct differs from baseline
#                                    (identical .rct ⇒ identical CSP ⇒ inherit
#                                    the baseline verdict; not re-checked)
#   /tmp/campaign/baseline/{lre,sranger,chemical_detector}/csp-gen/defs/
#
# If /tmp/campaign is missing (e.g. after a reboot), re-materialize it first:
#   mkdir -p /tmp/campaign && tar -xzf <campaign_staged_trees.tar.gz> -C /tmp/campaign
#
# Protocol notes (both inherited from scripts/rerun_fdr4_2026-08-15.sh):
#   * FDR4 resolves `include "..."` relative to the SCRIPT FILE's directory,
#     so the WHOLE defs tree is copied per check and the _nodet copy written
#     beside the includes.
#   * `assert ... :[deterministic]` lines are commented out into a _nodet
#     sibling exactly as forge.dashboard/web/runners.py:845-860 does — the
#     pipeline never verifies determinism (see the comment there).
#   * framed_json verdicts are INTEGERS: "result": 1 passed, 0 failed.
#
# Output: /tmp/campaign/fdr4-arm/results.csv
#   mutant_id,exit,passed,failed,det_stripped,seconds
# Resumable: a mutant with an existing .json is skipped.
#
# Expected runtime: seconds-to-minutes per check; 134 changed mutants ≈ 1-2 h.

set -u
FDR=${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}
REPO="$(cd "$(dirname "$0")/.." && pwd)"
CAMP=/tmp/campaign
OUT=$CAMP/fdr4-arm
LIST=$CAMP/rct_changed.txt
mkdir -p "$OUT"
RES="$OUT/results.csv"
[ -f "$RES" ] || echo "mutant_id,exit,passed,failed,det_stripped,seconds" > "$RES"
log () { echo "$@"; }

[ -x "$FDR" ] || { echo "no refines binary at $FDR"; exit 2; }
[ -f "$LIST" ] || { echo "missing $LIST — stage the campaign first"; exit 2; }

# ---- licence pre-flight on a real assertion (--version does NOT test it) ---
PRE="$OUT/preflight.csp"
printf 'channel a\nP = a -> STOP\nassert P [T= P\n' > "$PRE"
perl -e 'alarm 120; exec @ARGV' "$FDR" --format framed_json "$PRE" \
  > "$OUT/preflight.json" 2> "$OUT/preflight.err"
if grep -qE '"result": *[0-9]' "$OUT/preflight.json" 2>/dev/null; then
  log "pre-flight OK: licence exercised and accepted."
else
  log "!! pre-flight produced NO verdict — licence wall or broken install."
  head -5 "$OUT/preflight.err" 2>/dev/null
  exit 3
fi

# ---- per-mutant check --------------------------------------------------------
check_one () {   # check_one <label> <defs-dir>
  local label="$1" defs="$2"
  if [ ! -d "$defs" ]; then log "$label: MISSING defs dir"; echo "$label,,,,missing,0" >> "$RES"; return; fi
  if [ -f "$OUT/$label.json" ]; then log "$label: json exists — skipped (resume)"; return; fi
  # Auto-discover in runners.py:_discover_fdr4_csp preference order
  # (runners.py:753-773): 1. *_System_Module aggregate; 2. controller-only
  # *Controller_coreassertions.csp with no _Module_/_Ctrl_/_InputEnv_/
  # _OutputEnv_ infix; 3. *_Module_coreassertions.csp; 4. unique fallback.
  local csp=""
  csp=$(ls "$defs"/*_System_Module_coreassertions.csp 2>/dev/null | head -1)
  if [ -z "$csp" ]; then
    csp=$(ls "$defs"/*Controller_coreassertions.csp 2>/dev/null \
          | grep -v -e '_Module_' -e '_Ctrl_' -e '_InputEnv_' -e '_OutputEnv_' \
          | head -1)
  fi
  [ -n "$csp" ] || csp=$(ls "$defs"/*_Module_coreassertions.csp 2>/dev/null | head -1)
  [ -n "$csp" ] || csp=$(ls "$defs"/*_coreassertions.csp 2>/dev/null | head -1)
  if [ -z "$csp" ]; then log "$label: no coreassertions file"; echo "$label,,,,nocsp,0" >> "$RES"; return; fi
  # Stage the WHOLE csp-gen tree, not just defs/: several defs/*_coreassertions
  # files include "../instantiations.csp", so a defs-only stage breaks the
  # parent-relative include and refines exits 0 with zero verdicts and the
  # error only in the JSON "errors" array (the entire first run of this arm did
  # exactly that — 137 rows, all 0/0). The _nodet copy must sit INSIDE the
  # staged defs/ so its own sibling includes and the ../ include both resolve.
  local base stage cspgen nodet det t0 t1 rc passes fails
  base=$(basename "$csp" .csp)
  cspgen=$(dirname "$defs")             # .../csp-gen
  stage="$OUT/stage-$label"
  rm -rf "$stage"; mkdir -p "$stage"
  cp -R "$cspgen"/. "$stage"/ 2>/dev/null
  # Apply the pipeline's type-range corrections to the STAGED copy. The raw
  # generator emits unbounded union(...calc_type_min...) nametypes; the pipeline
  # always narrows them (apply_csp_corrections) before refines — the archived
  # instantiations.csp files are the corrected form ({0..1} ranges). Without
  # this, LRE's state space explodes (first run: killed by alarm at 20 min,
  # 40,000+ processes) and chem trips FDR4's polymorphic-channel errors.
  # The helper reuses csp_corrections.py's own merge/naming logic verbatim.
  python3 "$REPO/scripts/apply_corrections_standalone.py" "$stage" >/dev/null \
    || { log "$label: corrections helper FAILED"; echo "$label,,,,corrfail,0" >> "$RES"; return; }
  nodet="$stage/defs/${base}_nodet.csp"
  det=$(grep -cE '^[[:space:]]*assert.*:\[deterministic\]' "$csp" || true)
  if [ "${det:-0}" -gt 0 ]; then
    perl -pe 's{^(\s*assert.*:\[deterministic\].*)$}{-- determinism not verified (see runners.run_fdr4): $1}' \
      "$csp" > "$nodet"
  else
    cp "$csp" "$nodet"
  fi
  t0=$(date +%s)
  ( cd "$stage/defs" && perl -e 'alarm 1200; exec @ARGV' "$FDR" --format framed_json "$nodet" ) \
    > "$OUT/$label.json" 2> "$OUT/$label.err"
  rc=$?
  t1=$(date +%s)
  passes=$(grep -o '"result": *1' "$OUT/$label.json" 2>/dev/null | wc -l | tr -d ' ')
  fails=$(grep -o '"result": *0' "$OUT/$label.json" 2>/dev/null | wc -l | tr -d ' ')
  echo "$label,$rc,${passes:-0},${fails:-0},${det:-0},$((t1-t0))" >> "$RES"
  log "$label: exit=$rc passed=$passes failed=$fails ($((t1-t0))s)"
  if [ "${passes:-0}" -eq 0 ] && [ "${fails:-0}" -eq 0 ]; then
    log "   !! no verdicts — load failure? errors array excerpt:"
    perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; next unless $e =~ /\S/; $e =~ s/\\n/ /g; print "     $e\n" }' \
      "$OUT/$label.json" 2>/dev/null | head -3
  fi
  rm -rf "$stage"
}

log ""
log "=== 1. baselines (sanity gate) ==="
for s in lre sranger chemical_detector; do
  check_one "baseline-$s" "$CAMP/baseline/$s/csp-gen/defs"
done
# HARD GATE: if any baseline produced zero verdicts, the harness is broken
# (staging/include problem), and running 134 mutants would burn ~2h producing
# 0/0 rows. Rev 1 of this arm did exactly that. Abort loudly instead.
for s in lre sranger chemical_detector; do
  row=$(grep "^baseline-$s," "$RES" | tail -1)
  p=$(echo "$row" | cut -d, -f3); f=$(echo "$row" | cut -d, -f4)
  if [ "${p:-0}" -eq 0 ] && [ "${f:-0}" -eq 0 ]; then
    log ""
    log "!! ABORT: baseline-$s produced zero verdicts — harness problem, not a"
    log "   verification result. Fix before the mutant sweep (see errors above)."
    exit 4
  fi
done
log "baselines OK — all three produced verdicts; proceeding to mutants."

log ""
log "=== 2. mutants with changed models ($(wc -l < "$LIST" | tr -d ' ') of them) ==="
while IFS= read -r mid; do
  [ -n "$mid" ] || continue
  check_one "$mid" "$CAMP/outputs/$mid/csp-gen/defs"
done < "$LIST"

log ""
log "==== results: $RES ===="
log "Done. Tell the assistant results are in $OUT/"
