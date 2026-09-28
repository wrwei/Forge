#!/bin/bash
# sb_isa_rerun.sh -- Isabelle on the regenerated Steam Boiler theory, laddered.
#
# Run in YOUR terminal (Isabelle runs via the repo's Docker wrapper):
#   bash /tmp/sb-isa/sb_isa_rerun.sh
# One session at a time is the default; paste /tmp/sb-isa/out/SUMMARY.txt back.
#
# WHAT THIS MEASURES. September's finding (RQ8): the full theory elaborates but
# proof cost is spread across the 76 invariant-preservation lemmas; the
# deadlock_free lemma was EXPECTED to fail/hang (EMERGENCY_STOP has no outgoing
# operation). The REGENERATED theory (current pipeline) adds
#   until "st = EMERGENCY_STOP"   (terminal-state exemption)
#   invariant "tr ~= [] /\ offered = listens"  (environment coupling)
# so the discriminating question is DlfOnly: Skeleton + ONLY deadlock_free.
#   arm0/DlfOnly (September encoding): expected NOT provable (terminal mode).
#   arm1/DlfOnly (regenerated):        if the exemption works, expected PROVED.
# That pair is the smallest measurement that prices the repair. Skeleton first
# (elaboration cost), OneLemma optional (per-lemma cost, September's ceiling).
#
# SEPTEMBER'S SCRIPT LESSONS, BAKED IN:
#   Build with -d <dir> and an EXPLICIT session name (never -D: it builds
#        every session under the directory concurrently and voids the timing).
#   Poly/ML heap must fit the Docker VM. Default here: no ML64 rewrite,
#        stock 32-bit ML. SB_ML64=1 switches to x86_64-linux with
#        --maxheap ${SB_HEAP:-6g}; raise Docker Desktop VM memory first.
#   Anonymous timeout is NOT evidence: every row records elapsed, exit code and
#   the log path; per-goal timeout stays 600s (September's pre-registered
#   budget) via the session option.
set -u
T=/tmp/sb-isa
OUT=$T/out; mkdir -p "$OUT"
REPO="${REPO:-$HOME/Gitee/formal_method_guided_vibe_coding}"
ISA=$REPO/scripts/isabelle-docker.sh
BUDGET=${SB_BUDGET:-2400}          # wall-clock alarm per session build
GOALTO=${SB_GOALTO:-600}           # per-goal timeout (September's budget)
KILLFREE=${SB_KILLFREE:-2}         # free-GB guard, same as the FDR4 script
[ -x "$ISA" ] || { echo "!! isabelle-docker.sh not found at $ISA (set REPO=...)"; exit 1; }

freegb() { vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}'; }

if [ "${SB_ML64:-0}" = "1" ]; then
  USRDIR="${ISABELLE_USER_DIR:-$HOME/.isabelle-docker}"
  HEAP=${SB_HEAP:-6g}
  mkdir -p "$USRDIR/etc"
  grep -v "^ML_PLATFORM\|^ML_OPTIONS" "$USRDIR/etc/settings" 2>/dev/null > "$USRDIR/etc/settings.tmp" || true
  { cat "$USRDIR/etc/settings.tmp" 2>/dev/null
    echo 'ML_PLATFORM="x86_64-linux"'
    echo "ML_OPTIONS=\"--maxheap $HEAP\""
  } > "$USRDIR/etc/settings"
  rm -f "$USRDIR/etc/settings.tmp"
  echo "ML64 on, maxheap $HEAP (ensure the Docker VM has more than this)"
fi

run_session() { # $1 arm, $2 session name
  local ARM=$1 SESS=$2
  local FREE; FREE=$(freegb)
  awk -v f="$FREE" -v k="$KILLFREE" 'BEGIN{exit !(f<k)}' && {
    echo "$ARM/$SESS  SKIPPED: ${FREE} GB free < ${KILLFREE}" | tee -a "$OUT/SUMMARY.txt"; return; }
  local LOG=$OUT/${ARM}_${SESS}.log t0=$SECONDS
  local QD=""
  case "$SESS" in *_V0_Diag_*|*_V3_Norm_*) QD="-o quick_and_dirty";; esac
  ( perl -e "alarm $BUDGET; exec @ARGV" "$ISA" build -d "$T/$ARM" -v \
      -o timeout=$GOALTO -o threads=${SB_THREADS:-8} $QD "$SESS" > "$LOG" 2>&1 )
  local rc=$? el=$((SECONDS-t0))
  local verdict
  if grep -qE "^Finished" "$LOG" && [ $rc -eq 0 ]; then
    case "$SESS" in
      *_V0_Diag_*|*_V3_Norm_*) verdict="STEPS-BEFORE-SORRY COMPLETED (diagnostic under quick_and_dirty -- NOT a proof)";;
      *) verdict="PROVED (session finished)";;
    esac
  elif grep -qE "^\*\*\* Timeout" "$LOG"; then verdict="SESSION TIMEOUT (whole-theory ${GOALTO}s) -- raise SB_GOALTO / fix heap first"
  elif [ $rc -eq 137 ] || grep -q "Killed" "$LOG"; then verdict="EXTERNAL/OOM KILL -- not a verdict (heap vs VM: see the heap note below)"
  elif [ $rc -eq 142 ]; then verdict="WALL BUDGET (${BUDGET}s) -- not a verdict"
  elif grep -qE '\*\*\*' "$LOG"; then verdict="FAILED: $(grep -m1 -E '\*\*\*' "$LOG" | cut -c1-90)"
  else verdict="UNCLASSIFIED rc=$rc -- read the log"
  fi
  local FACT; FACT=$(grep -oE "factor [0-9.]+" "$LOG" | tail -1)
  printf "%-5s %-34s rc=%-4s %5ss  %s %s\n" "$ARM" "$SESS" "$rc" "$el" "$verdict" "${FACT:+[$FACT; >1.5 = ML heap thrash, see the heap note below]}" | tee -a "$OUT/SUMMARY.txt"
}

: > "$OUT/SUMMARY.txt"
echo "=== sb_isa_rerun $(date '+%F %T')  goal-timeout=${GOALTO}s wall=${BUDGET}s ml64=${SB_ML64:-0} ===" | tee -a "$OUT/SUMMARY.txt"
echo "  free+inactive: $(freegb) GB" | tee -a "$OUT/SUMMARY.txt"
ONLY=${SB_ONLY:-}
maybe() { [ -z "$ONLY" ] || [ "$ONLY" = "$1/$2" ] && run_session "$1" "$2"; }
# ladder order: elaboration price, then the discriminating pair
maybe arm0 BoilerController_Skeleton_Check
maybe arm1 BoilerController_Skeleton_Check
maybe arm0 BoilerController_DlfOnly_Check
maybe arm1 BoilerController_DlfOnly_Check
# proof-probe ladder (arm1 only): V0 diagnoses which method step fails (ends in
# sorry -- NOT a proof); V1/V2 decompose the method with per-goal closers
maybe arm1 BoilerController_V0_Diag_Check
maybe arm1 BoilerController_V1_Script_Check
maybe arm1 BoilerController_V2_Battery_Check
maybe arm1 BoilerController_V3_Norm_Check
maybe arm1 BoilerController_V4_SMT_Check
maybe arm1 BoilerController_V5_FF_Check
maybe arm0 BoilerController_Full_Check
maybe arm1 BoilerController_Full_Check
maybe arm1 BoilerController_FullSMT_Check
maybe arm0 BoilerController_NoDlf_Check
# optional: per-lemma cost (September's ceiling) -- run only if the above finish
if [ "${SB_ONELEMMA:-0}" = "1" ] || [[ "$ONLY" == *OneLemma* ]]; then
  maybe arm0 BoilerController_OneLemma_Check
  maybe arm1 BoilerController_OneLemma_Check
else
  echo "OneLemma: not run (SB_ONELEMMA=1 to price the per-lemma ceiling)" | tee -a "$OUT/SUMMARY.txt"
fi
echo | tee -a "$OUT/SUMMARY.txt"
echo "READING: arm0/DlfOnly failing while arm1/DlfOnly PROVES = the terminal-state" | tee -a "$OUT/SUMMARY.txt"
echo "exemption discharges deadlock-freedom on held-out input. Both timing out =" | tee -a "$OUT/SUMMARY.txt"
echo "the scale ceiling binds before the encoding matters. Paste this file back." | tee -a "$OUT/SUMMARY.txt"
