#!/bin/bash
# T-arm verification measurement — verifier battery (FDR4 + Isabelle).
# Style follows run_sb_verifiers.sh (licence pre-flight; loud empty-verdict
# guard; orphan-container sweep before Isabelle; per-goal timeout 600s).
#
#   bash /tmp/tarm-verify/run_tarm_verifiers.sh
#
# ============================================================================
# MEASUREMENT RECORD (R2-3b protocol, one-shot, no repair):
#   All THREE T-arm terminal trees (tests-green + static-clean) were REJECTED
#   by the pipeline's phase-1 structural linter (pre-fix extractor snapshot,
#   rule4_double_missing_real_annotation, severity=error):
#     run-1: Sensor.DEFAULT_DISTANCE        (Sensor.java:12)
#     run-2: IrDistanceSensor.DEFAULT_DISTANCE (IrDistanceSensor.java:10)
#     run-3: Sensor.DEFAULT_DISTANCE        (Sensor.java:10)
#   Per protocol the pipeline STOPPED at preflight for every run: no model was
#   extracted, so NO CSP, Dafny, or Isabelle artefacts exist for any run.
#   This script is retained as the pre-committed verifier harness; every rung
#   below guards LOUDLY on the (expected) absence of artefacts rather than
#   reporting a silent 0/0. If a future re-measurement (after an upstream
#   generation-prompt change, never a hand-edit) produces artefacts under
#   /tmp/tarm-verify/run-{1,2,3}/out/, this script verifies them unmodified.
# ============================================================================
set -u

TV=/tmp/tarm-verify
OUT=$TV/verify-out
REPO="${REPO:-$HOME/Gitee/formal_method_guided_vibe_coding}"
FDR=${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}
DAFNY=${DAFNY:-dafny}
mkdir -p "$OUT"

echo "==============================================================="
echo " T-arm verifier battery (post-preflight artefacts, 3 runs)"
echo " Phase-1 verdict on record: all three runs REJECTED by the"
echo " structural linter (rule4_double_missing_real_annotation)."
echo " Expect every artefact guard below to fire unless re-measured."
echo "==============================================================="

MISSING=0

# ---- 0. artefact presence audit (loud, per run) ----------------------------
for r in 1 2 3; do
  D=$TV/run-$r/out
  for want in "$D/csp-gen/defs" "$D"/*.dfy "$D/isabelle"; do
    if ! ls $want >/dev/null 2>&1; then
      echo "!! run-$r: MISSING $want (phase-1 rejection is the recorded cause)"
      MISSING=$((MISSING+1))
    fi
  done
done
if [ "$MISSING" -gt 0 ]; then
  echo "!! $MISSING artefact groups absent — consistent with the recorded"
  echo "   preflight rejections (one-shot trees were never extracted)."
  if [ "${TARM_VARIANT:-0}" = "1" ]; then
    echo "   TARM_VARIANT=1: skipping the one-shot rungs, proceeding to the"
    echo "   annotated-variant artefacts in run-*/out-variant/."
    SKIP_ONESHOT=1
  else
    echo "   Nothing to verify; exiting 0 (measurement already closed at"
    echo "   phase 1). TARM_VARIANT=1 runs the annotated-variant artefacts."
    exit 0
  fi
fi
SKIP_ONESHOT=${SKIP_ONESHOT:-0}

# ---- 1. Dafny ---------------------------------------------------------------
if [ "$SKIP_ONESHOT" = "0" ]; then
for r in 1 2 3; do
  D=$TV/run-$r/out
  for dfy in "$D"/*.dfy; do
    t0=$(date +%s)
    perl -e 'alarm 600; exec @ARGV' "$DAFNY" verify "$dfy" \
      > "$OUT/dafny_run${r}_$(basename "$dfy" .dfy).log" 2>&1
    rc=$?
    t1=$(date +%s)
    L="$OUT/dafny_run${r}_$(basename "$dfy" .dfy).log"
    if ! grep -qE "verified, [0-9]+ error" "$L"; then
      echo "!! run-$r $(basename "$dfy"): NO Dafny verdict line (exit=$rc) — first lines:"
      head -5 "$L"
    else
      echo "run-$r $(basename "$dfy"): $(grep -E "verified, [0-9]+ error" "$L" | tail -1) (exit=$rc, $((t1-t0))s)"
    fi
  done
done

fi  # SKIP_ONESHOT guard (Dafny one-shot rung)

# ---- 2. FDR4 (pre-flight always runs; one-shot checks respect the guard) ----
if [ ! -x "$FDR" ]; then
  echo "!! no refines binary at $FDR (set FDR=/path/to/refines)"; FDR_OK=0
else
  # licence pre-flight on a REAL assertion (--version does not exercise it)
  PRE="$OUT/preflight.csp"
  printf 'channel a\nP = a -> STOP\nassert P [T= P\n' > "$PRE"
  perl -e 'alarm 120; exec @ARGV' "$FDR" --format framed_json "$PRE" \
    > "$OUT/preflight.json" 2> "$OUT/preflight.err"
  if grep -qE '"result": *[0-9]' "$OUT/preflight.json" 2>/dev/null; then
    echo "FDR4 pre-flight OK: licence exercised and accepted."
    FDR_OK=1
  else
    echo "!! FDR4 pre-flight produced NO verdict — licence wall or broken install."
    head -5 "$OUT/preflight.err" 2>/dev/null
    FDR_OK=0
  fi
fi
if [ "${FDR_OK:-0}" = "1" ] && [ "$SKIP_ONESHOT" = "0" ]; then
  for r in 1 2 3; do
    DEFS=$TV/run-$r/out/csp-gen/defs
    # standard per-study narrowing via the pipeline's own corrections mechanism
    python3 "$REPO/scripts/apply_corrections_standalone.py" "$TV/run-$r/out/csp-gen" \
      || { echo "!! run-$r: corrections helper FAILED"; continue; }
    # _nodet sibling: determinism assertions are never checked (runners.run_fdr4)
    for csp in "$DEFS"/*Controller_coreassertions.csp; do
      [ -f "$csp" ] || { echo "!! run-$r: no controller coreassertions file"; continue; }
      base=$(basename "$csp" .csp)
      nodet="$DEFS/${base}_nodet.csp"
      det=$(grep -cE '^[[:space:]]*assert.*:\[deterministic\]' "$csp" || true)
      if [ "${det:-0}" -gt 0 ]; then
        perl -pe 's{^(\s*assert.*:\[deterministic\].*)$}{-- determinism not verified: $1}' \
          "$csp" > "$nodet"
      else
        cp "$csp" "$nodet"
      fi
      t0=$(date +%s)
      ( cd "$DEFS" && perl -e 'alarm 600; exec @ARGV' "$FDR" --format framed_json "$nodet" ) \
        > "$OUT/fdr4_run${r}_${base}.json" 2> "$OUT/fdr4_run${r}_${base}.err"
      rc=$?
      t1=$(date +%s)
      p=$(grep -o '"result": *1' "$OUT/fdr4_run${r}_${base}.json" | wc -l | tr -d ' ')
      f=$(grep -o '"result": *0' "$OUT/fdr4_run${r}_${base}.json" | wc -l | tr -d ' ')
      haserr=$(perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; print "1" and exit if $e =~ /\S/ }' "$OUT/fdr4_run${r}_${base}.json")
      # loud empty-verdict guard: load errors or 0/0 is NEVER a neutral result
      if [ -n "$haserr" ] || { [ "$p" = "0" ] && [ "$f" = "0" ]; }; then
        echo "!! LOAD FAILURE [run-$r $base] (exit=$rc, ${p}/${f} verdicts) — first error:"
        perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; next unless $e =~ /\S/; $e =~ s/\\n/ /g; print "   ", substr($e,0,300), "\n"; exit }' "$OUT/fdr4_run${r}_${base}.json"
      else
        echo "FDR4 [run-$r $base]: exit=$rc passed=$p failed=$f ($((t1-t0))s)"
      fi
    done
  done
fi

# ---- 3. Isabelle ------------------------------------------------------------
ISA=$REPO/scripts/isabelle-docker.sh
ZMACH="${ZMACH:-$HOME/zmachines}"
if [ ! -x "$ISA" ]; then
  echo "!! isabelle-docker.sh not found at $ISA (set REPO=...)"
else
  # orphan-container sweep BEFORE any measurement (leaked builds skew memory)
  ORPH=$(docker ps -q --filter "ancestor=${ISABELLE_IMAGE:-forge/isabelle:2023-cyphyassure}" 2>/dev/null)
  if [ -n "$ORPH" ]; then
    echo "!! killing orphaned Isabelle container(s) from previous runs:"
    docker ps --filter "ancestor=${ISABELLE_IMAGE:-forge/isabelle:2023-cyphyassure}" --format "   {{.ID}}  up {{.RunningFor}}"
    docker kill $ORPH >/dev/null
    sleep 3
  else
    echo "orphan sweep: no leftover Isabelle containers."
  fi
  [ "$SKIP_ONESHOT" = "1" ] && echo "(one-shot Isabelle rung skipped — no artefacts)"
  for r in $( [ "$SKIP_ONESHOT" = "0" ] && echo "1 2 3" ); do
    THYDIR=$TV/run-$r/out/isabelle
    [ -d "$THYDIR" ] || { echo "!! run-$r: no isabelle dir"; continue; }
    DFLAG=""
    [ -d "$ZMACH" ] && DFLAG="-d $ZMACH"
    t0=$(date +%s)
    "$ISA" build -D "$THYDIR" $DFLAG -v -o timeout=600 \
      > "$OUT/isabelle_run${r}.log" 2>&1
    rc=$?
    t1=$(date +%s)
    echo "isabelle run-$r: exit=$rc ($((t1-t0))s)  log: $OUT/isabelle_run${r}.log"
    if [ $rc -ne 0 ]; then
      if grep -q "Timeout" "$OUT/isabelle_run${r}.log"; then echo "  OUTCOME: timeout"
      elif grep -q "Failed to apply proof method" "$OUT/isabelle_run${r}.log"; then
        echo "  OUTCOME: refuted — residual goal:"
        grep -B2 -A8 "Failed to apply proof method" "$OUT/isabelle_run${r}.log" | head -12
      else
        echo "  OUTCOME: build error — log head:"; head -10 "$OUT/isabelle_run${r}.log"
      fi
    fi
  done
fi

# ============================================================================
# VARIANT SECTION — annotated-variant (EXPERIMENTER INTERVENTION, outside the
# T-arm loop). Sensitivity probe only: one @RoboChartType("real") line was
# added per tree (the exact fix the lint directive prescribes; diffs in
# variant_diffs.txt). The one-shot measurement above remains the headline.
#
# Recorded variant results (2026-09-08, local dafny 4.11.0 + homebrew z3):
#   lint:    3/3 clean (single rule-4 violation was the only phase-1 blocker)
#   extract: 3/3 completed (T2M -> M2M -> Dafny/RCT/Isabelle emitted)
#   Dafny:   run-1  4 verified / 1 error  (transitionFromTURNING ensures
#                   now()-clockResetTime>=2.0 ==> mode==MOVING unprovable:
#                   generated code checks EndTask BEFORE the timeout branch)
#            run-2  DID NOT VERIFY — ill-formed program: 2 resolution errors
#                   ('member currentTimeSeconds does not exist', dfy:64,71) —
#                   extractor emitted a spec referencing a Java private helper
#                   it never generated as a Dafny member (unseen construct:
#                   inline time read, no Clock class)
#            run-3  4 verified / 1 error  (same obligation as run-1)
#   CSP-gen: run-1, run-3 REJECTED by the official RoboChart generator —
#                   'feature fields ... with 0 values must have at least 1
#                   values': field-less per-event datatypes (EndTaskEvent{},
#                   TickEvent{}...) map to empty RecordTypes (unseen
#                   construct: per-event class hierarchies). No CSP exists.
#            run-2  csp-gen OK; corrections applied; _nodet sibling written.
#   Isabelle theories emitted for all three variant runs (not built here).
# ============================================================================
if [ "${TARM_VARIANT:-0}" = "1" ]; then
  echo ""
  echo "=== VARIANT: annotated-variant artefacts (experimenter intervention) ==="

  # ---- V1. Dafny (all three variant programs) ----
  for r in 1 2 3; do
    D=$TV/run-$r/out-variant
    for dfy in "$D"/*.dfy; do
      [ -f "$dfy" ] || { echo "!! variant run-$r: no .dfy"; continue; }
      t0=$(date +%s)
      perl -e 'alarm 600; exec @ARGV' "$DAFNY" verify --solver-path "${Z3:-/opt/homebrew/bin/z3}" "$dfy" \
        > "$OUT/variant_dafny_run${r}.log" 2>&1
      rc=$?
      t1=$(date +%s)
      L="$OUT/variant_dafny_run${r}.log"
      if grep -qE "verified, [0-9]+ error" "$L"; then
        echo "variant run-$r Dafny: $(grep -E 'verified, [0-9]+ error' "$L" | tail -1) (exit=$rc, $((t1-t0))s)"
      elif grep -q "resolution/type errors" "$L"; then
        echo "!! variant run-$r Dafny: ILL-FORMED program (resolution errors) — extraction defect, not a proof failure:"
        grep -E "Error:" "$L" | head -4
      else
        echo "!! variant run-$r Dafny: NO verdict line (exit=$rc) — head:"; head -5 "$L"
      fi
    done
  done

  # ---- V2. FDR4 (only run-2 has CSP; runs 1/3 rejected at csp-gen) ----
  if [ "${FDR_OK:-0}" = "1" ]; then
    for r in 1 2 3; do
      DEFS=$TV/run-$r/out-variant/csp-gen/defs
      nodet="$DEFS/SRangerController_coreassertions_nodet.csp"
      if [ ! -f "$nodet" ]; then
        echo "!! variant run-$r: no CSP (csp-gen rejected: empty RecordTypes from field-less event datatypes) — recorded generator verdict, nothing to check"
        continue
      fi
      t0=$(date +%s)
      ( cd "$DEFS" && perl -e 'alarm 600; exec @ARGV' "$FDR" --format framed_json "$nodet" ) \
        > "$OUT/variant_fdr4_run${r}.json" 2> "$OUT/variant_fdr4_run${r}.err"
      rc=$?
      t1=$(date +%s)
      p=$(grep -o '"result": *1' "$OUT/variant_fdr4_run${r}.json" | wc -l | tr -d ' ')
      f=$(grep -o '"result": *0' "$OUT/variant_fdr4_run${r}.json" | wc -l | tr -d ' ')
      haserr=$(perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; print "1" and exit if $e =~ /\S/ }' "$OUT/variant_fdr4_run${r}.json")
      if [ -n "$haserr" ] || { [ "$p" = "0" ] && [ "$f" = "0" ]; }; then
        echo "!! VARIANT LOAD FAILURE [run-$r] (exit=$rc, ${p}/${f} verdicts) — first error:"
        perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; next unless $e =~ /\S/; $e =~ s/\\n/ /g; print "   ", substr($e,0,300), "\n"; exit }' "$OUT/variant_fdr4_run${r}.json"
      else
        echo "variant FDR4 [run-$r]: exit=$rc passed=$p failed=$f ($((t1-t0))s)"
      fi
    done
  fi

  # ---- V3. Isabelle (theories emitted for all three; orphan sweep done above) ----
  if [ -x "$ISA" ]; then
    for r in 1 2 3; do
      THYDIR=$TV/run-$r/out-variant/isabelle
      [ -d "$THYDIR" ] || { echo "!! variant run-$r: no isabelle dir"; continue; }
      DFLAG=""
      [ -d "$ZMACH" ] && DFLAG="-d $ZMACH"
      t0=$(date +%s)
      # -c: all three variant sessions share the name SRangerController_Check;
      # without a clean build, runs 2/3 would reuse run-1's heap image and
      # report 'up to date' having proved nothing.
      "$ISA" build -c -D "$THYDIR" $DFLAG -v -o timeout=600 \
        > "$OUT/variant_isabelle_run${r}.log" 2>&1
      rc=$?
      t1=$(date +%s)
      echo "variant isabelle run-$r: exit=$rc ($((t1-t0))s)  log: $OUT/variant_isabelle_run${r}.log"
    done
  fi
else
  echo "--- VARIANT section skipped (TARM_VARIANT=1 to run FDR4/Isabelle on the annotated-variant artefacts) ---"
fi

echo ""
echo "=== SUMMARY ==="
echo "Recorded measurement: 3/3 T-arm trees rejected at phase-1 preflight."
