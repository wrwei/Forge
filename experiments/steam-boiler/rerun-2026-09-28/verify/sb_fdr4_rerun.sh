#!/bin/bash
# sb_fdr4_rerun.sh (rev 3) -- FDR4 on the regenerated Steam Boiler CSP,
# SEPTEMBER'S SCOPE: the three operation machines only. The full
# P_BoilerController machine is recorded as FDR4-infeasible at 16 GB (a
# pre-registered scope result) and is NOT rerun here; SB_FORCE_FULL=1 overrides.
#
# Run in YOUR terminal:   bash /tmp/sb-fdr4/sb_fdr4_rerun.sh
# Paste back:             /tmp/sb-fdr4/out/SUMMARY.txt
#
# REV 3 after the 40 GB incident:
#   * scope reduced to the three op machines (tractable in September);
#   * the rev-2 RSS watchdog measured the wrong number (macOS compresses pages,
#     so RSS stays low while the footprint climbs), and ulimit -v is NOT
#     enforced on macOS (probed). The guard is now a FREE-MEMORY watchdog:
#     while FDR runs, if free+inactive drops below SB_KILLFREE GB (default 2),
#     FDR is killed and the row is labelled MEMORY GUARD -- the machine stays
#     usable instead of being strangled;
#   * memory gate before each file (SB_MINFREE GB free+inactive, default 4);
#   * per-file budget SB_BUDGET (default 900 s).
# Verdict discipline unchanged: kills/timeouts/caps are labelled outcomes, never
# verdicts; load failures fail loudly; a deadlock-free FAIL ending in
# EMERGENCY_STOP would be the pre-registered expected-faithful outcome.
set -u
T=/tmp/sb-fdr4
OUT=$T/out; mkdir -p "$OUT"
FDR=${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}
BUDGET=${SB_BUDGET:-900}
KILLFREE=${SB_KILLFREE:-2}
MINFREE=${SB_MINFREE:-4}
PY=$(command -v python3 || echo /usr/bin/python3)
[ -x "$FDR" ] || { echo "!! no refines binary at $FDR"; exit 1; }

freegb() { vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}'; }

stage() { local S=$T/stage-$3; rm -rf "$S"; mkdir -p "$S"
  cp -R "$1/defs" "$S/defs"; cp "$1"/*.csp "$S/" 2>/dev/null || true
  cp "$1/$2" "$S/instantiations.csp"; echo "$S"; }

check_file() { # $1 arm, $2 stagedir, $3 csp file (in defs/)
  local NAME=$1 S=$2 FILE=$3
  local FREE; FREE=$(freegb)
  awk -v f="$FREE" -v m="$MINFREE" 'BEGIN{exit !(f<m)}' && {
    echo "$NAME/$FILE  SKIPPED: ${FREE} GB free < ${MINFREE}" | tee -a "$OUT/SUMMARY.txt"; return; }
  local J=$OUT/${NAME}_${FILE%.csp}.json t0=$SECONDS
  rm -f "$J.guard"
  ( cd "$S/defs" && \
    perl -e "alarm $BUDGET; exec @ARGV" "$FDR" --format framed_json "$FILE" > "$J" 2>&1 ) &
  local runner=$!
  ( while kill -0 $runner 2>/dev/null; do
      FREENOW=$(vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}')
      awk -v f="$FREENOW" -v k="$KILLFREE" 'BEGIN{exit !(f<k)}' && {
        touch "$J.guard"; pkill -9 -f "refines --format framed_json" 2>/dev/null; kill -9 $runner 2>/dev/null; break; }
      sleep 3
    done ) & local guard=$!
  wait $runner; local rc=$?
  kill $guard 2>/dev/null; wait $guard 2>/dev/null
  local el=$((SECONDS-t0))
  if [ -f "$J.guard" ]; then
    printf "%-5s %-46s rc=%-4s %4ss  MEMORY GUARD (free fell below ${KILLFREE} GB) -- not a verdict\n" \
      "$NAME" "$FILE" "$rc" "$el" | tee -a "$OUT/SUMMARY.txt"
    return
  fi
  local verdict
  verdict=$("$PY" - "$J" "$rc" <<'PYEOF'
import sys, re
raw = open(sys.argv[1], errors="ignore").read(); rc = int(sys.argv[2])
if rc == 137 or "Killed: 9" in raw: print(f"EXTERNAL KILL (rc={rc}) -- not a verdict"); sys.exit()
if "std::bad_alloc" in raw or "out of memory" in raw.lower() or rc in (134,): print(f"MEMORY CAP (rc={rc}) -- not a verdict"); sys.exit()
if rc == 142 or (rc != 0 and not raw.strip()): print(f"TIMEOUT/NO OUTPUT (rc={rc})"); sys.exit()
res = re.findall(r'"result"\s*:\s*(\w+)', raw)
errs = [e for e in re.findall(r'"errors"\s*:\s*\[([^\]]*)\]', raw) if e.strip()]
if errs or not res:
    first = re.search(r'"errors"\s*:\s*\[\s*"([^"]{0,140})', raw)
    print(f"!! LOAD FAILURE: {len(errs)} error(s), {len(res)} verdict(s)" + (f" | {first.group(1)}" if first else "")); sys.exit()
p = sum(1 for r in res if r in ("true","1","Passed"))
print(f"{p}/{len(res)} pass" + (f", {len(res)-p} FAIL (EMERGENCY_STOP trace = expected-faithful)" if len(res)-p else ""))
PYEOF
)
  printf "%-5s %-46s rc=%-4s %4ss  %s\n" "$NAME" "$FILE" "$rc" "$el" "$verdict" | tee -a "$OUT/SUMMARY.txt"
}

OPS="CalcLevelEstimate_coreassertions_nodet.csp CalcThroughput_coreassertions_nodet.csp BoilerController_Refresh_coreassertions_nodet.csp"
: > "$OUT/SUMMARY.txt"
echo "=== sb_fdr4_rerun rev3 $(date '+%F %T')  scope: three op machines  budget=${BUDGET}s guard=${KILLFREE}GB-free ===" | tee -a "$OUT/SUMMARY.txt"
echo "  free+inactive: $(freegb) GB" | tee -a "$OUT/SUMMARY.txt"
S0=$(stage "$T/old-tree" "instantiations.csp" arm0)
S1=$(stage "$T/new-tree" "instantiations_tuned.csp" arm1)
for f in $OPS; do check_file arm0 "$S0" "$f"; done
for f in $OPS; do check_file arm1 "$S1" "$f"; done
if [ "${SB_FORCE_FULL:-0}" = "1" ]; then
  echo "--- SB_FORCE_FULL=1: full statemachine (recorded FDR4-infeasible at 16 GB)" | tee -a "$OUT/SUMMARY.txt"
  check_file arm1 "$S1" "BoilerController_coreassertions_nodet.csp"
else
  echo "full statemachine: NOT RUN (pre-registered scope result: infeasible at 16 GB; SB_FORCE_FULL=1 to override)" | tee -a "$OUT/SUMMARY.txt"
fi
echo | tee -a "$OUT/SUMMARY.txt"
echo "arm0 = September tree (baseline), arm1 = regenerated tree. Paste this file back." | tee -a "$OUT/SUMMARY.txt"
