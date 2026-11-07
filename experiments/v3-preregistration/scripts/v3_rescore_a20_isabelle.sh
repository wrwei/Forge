#!/bin/bash
# v3_rescore_a20_isabelle.sh -- Pre-registration v3, Amendment 20.
# Rebuilds, on the evaluation machine, the Isabelle theory of every recorded session
# iteration whose theory changed at the Amendment 20 pin (80 iterations; the theories were
# regenerated from the recorded Java and staged under experiments/v3-rescore-a20/<iteration>/isabelle
# with the plan in experiments/v3-rescore-a20/plan.json). The CSP and Dafny artefacts of
# these iterations did not change (regression record), so only the Isabelle verdict is re-scored.
# Settings as the pipeline's isabelle_verify phase: isabelle build -v -o timeout=600, 1800 s cap.
# Resumable: an iteration with a row in results.csv is skipped. Run in YOUR terminal (Docker running).
#   RS_PAR (default 2): parallel builds for the three RoboStar studies; the Steam Boiler builds run one at a time.
set -u
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
S="$C/experiments/v3-rescore-a20"; ISA="$C/scripts/isabelle-docker.sh"; RES="$S/results.csv"
[ -x "$ISA" ] || { echo "FATAL: $ISA not executable"; exit 1; }
docker info >/dev/null 2>&1 || { echo "FATAL: Docker is not running"; exit 1; }
[ -f "$RES" ] || echo "iteration,study,status,exit,seconds" > "$RES"
log(){ echo "$(date '+%H:%M:%S') $*" | tee -a "$S/rescore.log"; }
one(){  # one <iteration path> <study>
  local it="$1" study="$2"
  grep -q "^$it,$study," "$RES" && return
  local d="$S/$it/build"; rm -rf "$d"; mkdir -p "$d"; cp "$S/$it"/isabelle/*_Beh.thy "$d/"
  # one session name per iteration: the Isabelle home is shared by every container
  local sess; sess="$(printf '%s' "${it}_Check" | tr -c 'A-Za-z0-9_\n' '_')"
  { printf 'session %s = "Z_Machines" +\n  theories\n' "$sess"; for t in "$d"/*_Beh.thy; do printf '    %s\n' "$(basename "$t" .thy)"; done; } > "$d/ROOT"
  local t0 t1 rc status; t0=$(date +%s)
  perl -e 'alarm 1800; exec @ARGV' bash "$ISA" build -v -o timeout=600 -D "$d" > "$d/build.log" 2>&1; rc=$?
  t1=$(date +%s)
  if [ "$rc" -eq 142 ] || [ "$rc" -eq 124 ]; then status=TIMEOUT
  elif [ "$rc" -eq 137 ]; then status=MEMKILL
  elif grep -q "^Finished ${sess}" "$d/build.log"; then status=verified
  elif grep -q "^\*\*\* Timeout" "$d/build.log"; then status=goal_timeout
  elif grep -qE "Failed to finish proof|\*\*\* " "$d/build.log"; then status=proof_failed
  elif grep -qiE "Cannot connect to the Docker daemon|docker: error" "$d/build.log"; then status=DOCKERDOWN
  else status=UNKNOWN; fi
  echo "$it,$study,$status,$rc,$((t1-t0))" >> "$RES"
  log "  $it: $status ($((t1-t0))s)"
  [ "$status" = DOCKERDOWN ] && { log "Docker went away; stopping (rerun to resume)"; exit 3; }
  rm -f "$d"/*_Beh.thy
}
export -f one log; export S ISA RES
PY=$(command -v python3 || echo /usr/bin/python3)
"$PY" - "$S" > "$S/plan.txt" <<'PYEOF'
import json,sys
for p in json.load(open(sys.argv[1]+"/plan.json")): print(p["iteration"], p["study"])
PYEOF
log "--- RoboStar studies (RS_PAR=${RS_PAR:-2}) ---"
grep -v " steam_boiler$" "$S/plan.txt" | xargs -P "${RS_PAR:-2}" -L 1 bash -c 'one "$0" "$1"'
log "--- Steam Boiler (serial) ---"
grep " steam_boiler$" "$S/plan.txt" | xargs -P 1 -L 1 bash -c 'one "$0" "$1"'
log "done: $(grep -c ',verified,' "$RES") verified, $(grep -cE ',(proof_failed|goal_timeout),' "$RES") failed, $(grep -cE ',(TIMEOUT|MEMKILL|UNKNOWN),' "$RES") environment rows (rerun the script for those)"
echo "=== DONE $(date) ===  Paste $RES back."
