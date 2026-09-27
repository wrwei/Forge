#!/bin/bash
# Steam Boiler held-out run — verifier battery (FDR4 + Isabelle).
# Run in YOUR terminal (FDR licence lives in a TCC-protected store; Isabelle
# runs via Docker) :
#
#   bash /tmp/steamboiler/verify/run_sb_verifiers.sh
#
# Results land in /tmp/steamboiler/verify/out/ — paste that directory (or the
# summary this script prints at the end) back into the session.
#
# ============================================================================
# PRE-REGISTERED EXPECTATION (review/steamboiler-prereg.md, recorded BEFORE
# generation):
#   The ONLY acceptable verifier failure on a faithful implementation is the
#   TERMINAL-MODE DEADLOCK — Abrial's emergency stop halts the program, so
#     * FDR4: the :[deadlock-free] assertions may FAIL with a counterexample
#       ending in the EMERGENCY_STOP state, and
#     * Isabelle: the BoilerController_deadlock_free lemma may FAIL/hang for
#       the same reason (EMERGENCY_STOP has no outgoing operation).
#   Such a failure is EXPECTED-FAITHFUL — evidence of fidelity, NOT a defect,
#   and must NOT be "repaired" by making emergency stop escapable (that would
#   violate SB-Beh22). Divergence-freedom is expected to PASS. Determinism is
#   not verified (stripped, as in the pipeline). Any OTHER failure is a
#   genuine finding — report it as such.
# ============================================================================
set -u

SB=/tmp/steamboiler
DEFS=$SB/formal-artefacts/csp-gen/defs
NODET=$DEFS/BoilerController_coreassertions_nodet.csp
THYDIR=$SB/formal-artefacts/isabelle
OUT=$SB/verify/out
REPO="${REPO:-$HOME/Gitee/formal_method_guided_vibe_coding}"
FDR=${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}
mkdir -p "$OUT"

echo "==============================================================="
echo " Steam boiler verifier battery  (rev 6: isolation ladder)"
echo " F9: all three v1 variants timed out (NoDlf 1831s / HoareA 1830s /"
echo "   HoareB 1481s) - cost is NOT localized to deadlock_free; it is spread"
echo "   across the invariant-preservation lemmas. This run isolates further:"
echo "   Skeleton (0 lemmas: elaboration only) -> OneLemma (1 inv lemma priced)"
echo "   -> NoDlf/HoareA/HoareB only if OneLemma predicts they fit the budget."
echo " Heap note: the container ML is x86_64_32-linux (~4GB cap; last run hit"
echo "   cpu/elapsed factor 2.57). SB_ML64=1 switches ISABELLE_HOME_USER"
echo "   settings to 64-bit x86_64-linux ML (the dist ships that Poly/ML too):"
echo "   one-off HOL+Z_Machines heap rebuild on first use, bigger heap after."
echo " F7 fixed: Sensors/Ctrl_State duplicate declaration (unitsReady) removed"
echo "   at the RCT layer; op machines regenerated - scoped duplicate scan clean."
echo "   Script now FAILS LOUDLY on FDR4 load errors (non-empty errors field or"
echo "   zero verdicts => '!! LOAD FAILURE' + first error, never bare 0/0)."
echo " F8: anonymous Isabelle timeout is NOT evidence (campaign rule). This run"
echo "   builds isolation variants: NoDlf (deadlock_free removed) first; if that"
echo "   times out too, HoareA/HoareB (half the preservation lemmas each)."
echo " F5 fixed: operation-carried vars (projLow/...) now real-typed zstore"
echo "   lenses; theory statically checked (thy_term_check.py: 74 pre/update"
echo "   strings clean - balanced parens, no unit-misapplications, no"
echo "   undeclared identifiers, types consistent)."
echo " F6 RECORDED OUTCOME: the untimed statemachine process P_BoilerController"
echo "   is the SMALLEST statemachine-scope process the generated tree offers"
echo "   (Ctrl/Module wrap it), and FDR4 was SIGKILLed on it twice on this"
echo "   machine: 1322s at int={0..4} ('Found 2000 processes including 70"
echo "   names' repeating in that run's err stream) and 732s with per-channel"
echo "   narrowing (err stream: only the Killed: 9 line). FDR4 deadlock"
echo "   checking at this scale is recorded as infeasible on this hardware;"
echo "   the Isabelle deadlock_free lemma covers the same property class for"
echo "   this study. Supplementary FDR4: the three small OPERATION machines"
echo "   are checked below; set SB_FORCE_FULL=1 to retry the full machine."
echo " F4 mitigation applied: (a) per-channel narrowing — core_int back to {0..1};"
echo "   pump channels now core_pumpid={1..4}, stopCount channels core_stopct={0..3}"
echo "   (post-processing of the generated defs, documented like _nodet)."
echo "   (b) not applicable: the asserted process P_BoilerController is already the"
echo "   narrowest (statemachine-level) scope; module/ctrl-level files not run."
echo "   (c) no runner compression flags exist to enable (runners.py passes only"
echo "   --format framed_json; dbisim compression is already in the generated CSP)."
echo " If FDR4 still exhausts memory at this scale, that is the recordable outcome."
echo " EXPECTED-FAITHFUL failure (prereg): terminal-mode deadlock only"
echo "   - FDR4 deadlock-freedom failing INTO EMERGENCY_STOP: expected"
echo "   - Isabelle deadlock_free lemma failing on EMERGENCY_STOP: expected"
echo "   - divergence-freedom: expected PASS"
echo "   - anything else failing: GENUINE FINDING, report it"
echo "==============================================================="

# ---- 1. FDR4 ---------------------------------------------------------------
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

if [ "${FDR_OK:-0}" = "1" ]; then
  # --- supplementary: the three OPERATION machines (small, tractable) ---
  for opbase in CalcLevelEstimate_coreassertions CalcThroughput_coreassertions BoilerController_Refresh_coreassertions; do
    opnodet="$DEFS/${opbase}_nodet.csp"
    [ -f "$opnodet" ] || { echo "!! missing $opnodet"; continue; }
    t0=$(date +%s)
    ( cd "$DEFS" && perl -e 'alarm 600; exec @ARGV' "$FDR" --format framed_json \
        "$opnodet" ) > "$OUT/fdr4_${opbase}.json" 2> "$OUT/fdr4_${opbase}.err"
    rc=$?
    t1=$(date +%s)
    p=$(grep -o '"result": *1' "$OUT/fdr4_${opbase}.json" | wc -l | tr -d ' ')
    f=$(grep -o '"result": *0' "$OUT/fdr4_${opbase}.json" | wc -l | tr -d ' ')
    # F7 guard: a run with load errors or zero total verdicts is a LOAD
    # FAILURE, never a neutral 0/0 (the campaign FDR4 arm was bitten by the
    # same silent-zero class).
    haserr=$(perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; print "1" and exit if $e =~ /\S/ }' "$OUT/fdr4_${opbase}.json")
    if [ -n "$haserr" ] || { [ "$p" = "0" ] && [ "$f" = "0" ]; }; then
      echo "!! LOAD FAILURE [$opbase] (exit=$rc, ${p}/${f} verdicts) — first error:"
      perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; next unless $e =~ /\S/; $e =~ s/\\n/ /g; print "   ", substr($e,0,300), "\n"; exit }' "$OUT/fdr4_${opbase}.json"
    else
      echo "FDR4 [$opbase]: exit=$rc passed=$p failed=$f ($((t1-t0))s)"
    fi
  done
  # --- full statemachine check: recorded infeasible-at-this-scale (F6) ---
  if [ "${SB_FORCE_FULL:-0}" = "1" ]; then
    if [ ! -f "$NODET" ]; then
      echo "!! missing $NODET — re-stage the formal-artefacts tree";
    else
      echo "--- FDR4 FULL (SB_FORCE_FULL=1): BoilerController_coreassertions_nodet.csp ---"
      t0=$(date +%s)
      ( cd "$DEFS" && perl -e 'alarm 1800; exec @ARGV' "$FDR" --format framed_json \
          "$NODET" ) > "$OUT/fdr4_boiler.json" 2> "$OUT/fdr4_boiler.err"
      rc=$?
      t1=$(date +%s)
      passes=$(grep -o '"result": *1' "$OUT/fdr4_boiler.json" | wc -l | tr -d ' ')
      fails=$(grep -o '"result": *0' "$OUT/fdr4_boiler.json" | wc -l | tr -d ' ')
      haserrF=$(perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; print "1" and exit if $e =~ /\S/ }' "$OUT/fdr4_boiler.json")
      if [ -n "$haserrF" ] || { [ "$passes" = "0" ] && [ "$fails" = "0" ]; }; then
        echo "!! LOAD FAILURE or no verdicts (exit=$rc) — first error:"
        perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; next unless $e =~ /\S/; $e =~ s/\\n/ /g; print "   ", substr($e,0,300), "\n"; exit }' "$OUT/fdr4_boiler.json"
      else
        echo "FDR4 full: exit=$rc passed=$passes failed=$fails ($((t1-t0))s)"
      fi
      echo "  (a deadlock-free FAIL whose counterexample ends in EMERGENCY_STOP"
      echo "   is the pre-registered EXPECTED-FAITHFUL outcome)"
    fi
  else
    echo "--- FDR4 full statemachine check SKIPPED (F6 recorded outcome; SB_FORCE_FULL=1 to retry) ---"
  fi
fi

# ---- 2. Isabelle -----------------------------------------------------------
# Uses the repo's docker wrapper (linux/amd64 Poly/ML). Session ROOT already
# declares parent Z_Machines; the wrapper needs the Z_Machines heap dir on -d.
ISA=$REPO/scripts/isabelle-docker.sh
ZMACH="${ZMACH:-$HOME/zmachines}"   # dir containing the Z_Machines session (ROOT)
if [ ! -x "$ISA" ]; then
  echo "!! isabelle-docker.sh not found at $ISA (set REPO=...)"
else
  echo "--- Isabelle: isolation ladder (Skeleton -> OneLemma -> bisection) ---"
  # Optional 64-bit ML (bigger heap; one-off heap rebuild):
  if [ "${SB_ML64:-0}" = "1" ]; then
    USRDIR="${ISABELLE_USER_DIR:-$HOME/.isabelle-docker}"
    mkdir -p "$USRDIR/etc"
    # Rewrite the ML block every time (run 7 left a stale 12g maxheap that the
    # Docker VM cannot back -- Poly/ML gets SIGKILLed by the VM OOM killer).
    HEAP="${SB_HEAP:-6g}"
    grep -v "^ML_PLATFORM\|^ML_OPTIONS" "$USRDIR/etc/settings" 2>/dev/null > "$USRDIR/etc/settings.tmp" || true
    {
      cat "$USRDIR/etc/settings.tmp" 2>/dev/null
      echo 'ML_PLATFORM="x86_64-linux"'
      echo "ML_OPTIONS=\"--maxheap $HEAP\""
    } > "$USRDIR/etc/settings"
    rm -f "$USRDIR/etc/settings.tmp"
    echo "SB_ML64: 64-bit ML, maxheap=$HEAP (override with SB_HEAP=...; must fit the Docker VM memory)"
  fi
  VAR=$SB/formal-artefacts/isabelle-variants
  declare -a LADDER_NAMES LADDER_RC LADDER_SECS
  run_variant() {  # $1 session  $2 per-goal-timeout  $3 wall-budget-note
    local sess="$1" tmo="$2"
    local DFLAGV=""
    [ -d "$ZMACH" ] && DFLAGV="-d $ZMACH"
    local t0 t1 rcv
    t0=$(date +%s)
    # -d (not -D): register the variants dir as a session root but build ONLY
    # the named session -- -D selects every session under the dir, which made
    # run 7 build all five variants inside one alarm window and invalidated
    # the per-rung verdicts.
    "$ISA" build -d "$VAR" $DFLAGV -v -o timeout=$tmo "$sess" \
      > "$OUT/isabelle_${sess}.log" 2>&1
    rcv=$?
    t1=$(date +%s)
    LADDER_NAMES+=("$sess"); LADDER_RC+=("$rcv"); LADDER_SECS+=("$((t1-t0))")
    echo "isabelle $sess: exit=$rcv ($((t1-t0))s)  log: $OUT/isabelle_${sess}.log"
    return $rcv
  }
  if [ ! -d "$VAR" ]; then
    echo "!! isabelle-variants dir missing — re-stage the tree"
  else
    # Rung 1: Skeleton (elaboration only). Budget 600s.
    if run_variant BoilerController_Skeleton_Check 300; then
      echo "  Skeleton OK => elaboration is cheap; proofs carry the cost."
      # Rung 2: OneLemma — price a single zpog_full inv lemma. Budget 600s.
      if run_variant BoilerController_OneLemma_Check 600; then
        one_secs=${LADDER_SECS[${#LADDER_SECS[@]}-1]}
        est=$((one_secs * 76))
        echo "  OneLemma OK in ${one_secs}s => naive 76-lemma estimate ~${est}s."
        if [ "$est" -lt 1800 ]; then
          echo "  Estimate fits 1800s: running NoDlf (all 76 lemmas, no dlf)."
          run_variant BoilerController_NoDlf_Check 600 || true
        else
          echo "  Estimate exceeds 1800s: running HoareA/HoareB halves instead."
          run_variant BoilerController_HoareA_Check 600 || true
          run_variant BoilerController_HoareB_Check 600 || true
        fi
      else
        echo "  ONE inv-preservation lemma alone busts the budget => F9 is a"
        echo "  per-lemma proof-cost finding (zpog_full over the 30-field zstore"
        echo "  + 29-clause updates); lemma bisection is moot at this heap."
        echo "  Retry with SB_ML64=1 before concluding."
      fi
    else
      echo "  Skeleton TIMES OUT => the cost is UPSTREAM of proofs (record simp"
      echo "  setup / zmachine elaboration on the 16-field + 4x8-field records);"
      echo "  lemma bisection is moot. Retry with SB_ML64=1 (heap-bound suspect:"
      echo "  factor 2.57 cpu/elapsed in the failed run suggests GC thrash)."
    fi
    echo ""
    echo "=== F9 ladder verdict table ==="
    printf "%-40s %6s %8s\n" "session" "exit" "seconds"
    for i in "${!LADDER_NAMES[@]}"; do
      printf "%-40s %6s %8s\n" "${LADDER_NAMES[$i]}" "${LADDER_RC[$i]}" "${LADDER_SECS[$i]}"
    done
  fi
  if [ "${SB_CANONICAL:-0}" != "1" ]; then
    echo "--- Isabelle canonical run SKIPPED (hung on run 6; SB_CANONICAL=1 to retry) ---"
  else
  echo "--- Isabelle canonical: BoilerController_Check (per-goal timeout 600s) ---"
  # -d "$ZMACH" only if the Z_Machines session is not already in the image heaps
  DFLAG=""
  [ -d "$ZMACH" ] && DFLAG="-d $ZMACH"
  t0=$(date +%s)
  "$ISA" build -D "$THYDIR" $DFLAG -v -o timeout=600 \
    > "$OUT/isabelle_boiler.log" 2>&1
  rc=$?
  t1=$(date +%s)
  echo "isabelle build: exit=$rc ($((t1-t0))s)  log: $OUT/isabelle_boiler.log"
  # ---- outcome categories (campaign-arm convention) ----
  #   verified : session finishes, no failed proof
  #   refuted  : "Failed to apply proof method" / counterexample printed
  #   timeout  : "Timeout" / per-goal timeout exceeded
  if [ $rc -eq 0 ]; then
    echo "OUTCOME: verified (all lemmas, incl. deadlock_free — check whether"
    echo "         that contradicts the prereg: emergency stop should deadlock"
    echo "         unless the negation-complete guard cover makes every live"
    echo "         state enabled and EMERGENCY_STOP unreachable in the model)"
  else
    if grep -q "Timeout" "$OUT/isabelle_boiler.log"; then
      echo "OUTCOME: timeout — if it is the deadlock_free lemma, that is the"
      echo "         known Final-state hang signature (prereg EXPECTED-FAITHFUL:"
      echo "         EMERGENCY_STOP has no outgoing operation)"
    elif grep -q "Failed to apply proof method" "$OUT/isabelle_boiler.log"; then
      echo "OUTCOME: refuted — inspect the residual goal:"
      grep -B2 -A8 "Failed to apply proof method" "$OUT/isabelle_boiler.log" | head -20
      echo "         a missing 'st = EMERGENCY_STOP' disjunct = prereg EXPECTED-FAITHFUL;"
      echo "         any OTHER state missing = genuine finding"
    else
      echo "OUTCOME: build error (theory load / session setup) — see log head:"
      head -20 "$OUT/isabelle_boiler.log"
    fi
  fi
  fi  # SB_CANONICAL gate
fi

echo ""
echo "=== SUMMARY (paste back into the session) ==="
echo "FDR4:    $(ls -la "$OUT"/fdr4_boiler.json 2>/dev/null | awk '{print $NF}') passed=${passes:-n/a} failed=${fails:-n/a}"
echo "Isabelle: exit=${rc:-n/a} log=$OUT/isabelle_boiler.log"
echo "Reminder: ONLY the EMERGENCY_STOP deadlock is pre-registered as expected."
