#!/bin/bash
# sb_fdr4_full.sh (rev 4) -- the FULL 37-operation P_BoilerController machine,
# retried on the 48 GB machine. Every prior attempt was on 16 GB hardware:
# September x2 (SIGKILL) and the 2026-09-27 attempt (~40 GB, machine froze).
# 48 GB may fit it; this run decides the scope result at the larger scale.
#
# Run in YOUR terminal:   bash /tmp/sb-fdr4/sb_fdr4_full.sh
# One assertion file, one arm at a time. Paste /tmp/sb-fdr4/out/SUMMARY.txt back.
#
# PRE-REGISTERED READINGS (same as the paired runs):
#   * bare deadlock-free FAIL with trace ending in tick = termination semantics
#     (benign, matches the op machines); the ;RUN variant and divergence-free
#     are the substantive assertions.
#   * MEMORY GUARD / EXTERNAL KILL / TIMEOUT rows are environment outcomes,
#     not verdicts. If the guard fires here too, the ceiling is re-confirmed
#     at 48 GB and that is the (negative) scope result.
# GUARD: free+inactive below SB_KILLFREE GB (default 6 -- this machine has
# headroom; the guard exists so the OS never strangles) kills FDR, labels the
# row, machine stays usable.
set -u
T=/tmp/sb-fdr4
OUT=$T/out; mkdir -p "$OUT"
FDR=${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}
BUDGET=${SB_BUDGET:-5400}
KILLFREE=${SB_KILLFREE:-6}
PY=$(command -v python3 || echo /usr/bin/python3)
ARM=${SB_ARM:-arm1}        # arm1 = regenerated tree (default), arm0 = September tree
[ -x "$FDR" ] || { echo "!! no refines at $FDR"; exit 1; }

freegb() { vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}'; }

# stage
case "$ARM" in
  arm1) SRC=$T/new-tree; INST=instantiations_tuned.csp;;
  arm0) SRC=$T/old-tree; INST=instantiations.csp;;
  *) echo "!! SB_ARM must be arm0|arm1"; exit 1;;
esac
S=$T/stage-full-$ARM; rm -rf "$S"; mkdir -p "$S"
cp -R "$SRC/defs" "$S/defs"; cp "$SRC"/*.csp "$S/" 2>/dev/null || true
cp "$SRC/$INST" "$S/instantiations.csp"
FILE=BoilerController_coreassertions_nodet.csp

FREE=$(freegb)
awk -v f="$FREE" -v k="$KILLFREE" 'BEGIN{exit !(f < k+4)}' && {
  echo "REFUSED to start: only ${FREE} GB free (need > $((${KILLFREE%%.*}+4))). Close apps."; exit 1; }

echo "=== sb_fdr4_full rev4 $(date '+%F %T')  arm=$ARM budget=${BUDGET}s guard=${KILLFREE}GB-free  free_now=${FREE}GB ===" | tee -a "$OUT/SUMMARY.txt"
J=$OUT/full_${ARM}.json; t0=$SECONDS
rm -f "$J.guard"
( cd "$S/defs" && perl -e "alarm $BUDGET; exec @ARGV" "$FDR" --format framed_json "$FILE" > "$J" 2>&1 ) &
runner=$!
( while kill -0 $runner 2>/dev/null; do
    FREENOW=$(vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}')
    awk -v f="$FREENOW" -v k="$KILLFREE" 'BEGIN{exit !(f<k)}' && {
      touch "$J.guard"; pkill -9 -f "refines --format framed_json" 2>/dev/null; kill -9 $runner 2>/dev/null; break; }
    sleep 5
  done ) & guard=$!
wait $runner; rc=$?
kill $guard 2>/dev/null; wait $guard 2>/dev/null
el=$((SECONDS-t0))
if [ -f "$J.guard" ]; then
  printf "full  %-6s rc=%-4s %5ss  MEMORY GUARD (free fell below ${KILLFREE} GB) -- ceiling re-confirmed at this budget, not a verdict\n" "$ARM" "$rc" "$el" | tee -a "$OUT/SUMMARY.txt"
else
  verdict=$("$PY" - "$J" "$rc" <<'PYEOF'
import sys, re
raw = open(sys.argv[1], errors="ignore").read(); rc = int(sys.argv[2])
if "license is invalid" in raw.lower() or "license" in raw.lower() and "expired" in raw.lower(): print("LICENCE: FDR licence invalid/expired -- run the GUI once (or refines interactively) to re-validate; not a verdict"); sys.exit()
if rc == 137 or "Killed: 9" in raw: print(f"EXTERNAL KILL (rc={rc}) -- not a verdict"); sys.exit()
if "std::bad_alloc" in raw or "out of memory" in raw.lower() or rc == 134: print(f"MEMORY (rc={rc}) -- not a verdict"); sys.exit()
if rc == 142 or (rc != 0 and not raw.strip()): print(f"TIMEOUT/NO OUTPUT (rc={rc})"); sys.exit()
res = re.findall(r'"result"\s*:\s*(\w+)', raw)
errs = [e for e in re.findall(r'"errors"\s*:\s*\[([^\]]*)\]', raw) if e.strip()]
if errs or not res:
    first = re.search(r'"errors"\s*:\s*\[\s*"([^"]{0,140})', raw)
    print(f"!! LOAD FAILURE: {len(errs)} error(s), {len(res)} verdict(s)" + (f" | {first.group(1)}" if first else "")); sys.exit()
p = sum(1 for r in res if r in ("true","1","Passed"))
print(f"{p}/{len(res)} pass" + (f", {len(res)-p} FAIL (trace ending in tick = termination, benign)" if len(res)-p else ""))
PYEOF
)
  printf "full  %-6s rc=%-4s %5ss  %s\n" "$ARM" "$rc" "$el" "$verdict" | tee -a "$OUT/SUMMARY.txt"
fi
echo "peak guidance: watch Activity Monitor; refines may legitimately sit at 20-40+ GB." | tee -a "$OUT/SUMMARY.txt"
echo "Paste this file back." | tee -a "$OUT/SUMMARY.txt"
