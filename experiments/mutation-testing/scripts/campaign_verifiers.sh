#!/bin/bash
# campaign_verifiers.sh -- FDR4 and Isabelle columns for the regenerated mutation campaign.
#
# Run in YOUR terminal (FDR licence + Docker):
#   bash <campaign-dir>/campaign_verifiers.sh            both columns
#   CV_ONLY=fdr4  bash .../campaign_verifiers.sh         one column
#   CV_ONLY=isabelle bash .../campaign_verifiers.sh
# Resumable: a subject with a recorded verdict is skipped.
# Parallel Isabelle builds: CV_PAR (default 2) for the LRE/SRanger/Gas theories; the Movement
# theories run one at a time (CV_PAR_MOVEMENT=1): three at once were killed for memory on 48 GB.
#
# Layout (written by regen_campaign.py): <campaign-dir>/out/<id>/{rct-project/csp-gen, isabelle/}
# Outputs: <campaign-dir>/fdr4/results.csv, <campaign-dir>/isabelle/results.csv, logs beside them.
#
# Rules (pre-registration, paper repo review/prereg-mutation-campaign-published-extractor-2026-10-25.md):
#   FDR4: pipeline range corrections applied to a staged copy of csp-gen; determinism
#   assertions set aside as the pipeline does; refines --format framed_json, 1200 s cap.
#   A subject whose CSP is identical to its baseline's is not re-checked (inherits).
#   Isabelle: only theories that differ from the baseline's; pipeline Isabelle (Docker),
#   per-goal timeout as the pipeline sets, quick_and_dirty=false, 1800 s cap per build.
#   TIMEOUT / MEMKILL / LOADERR rows are environment outcomes: rerun, never a verdict.
set -u
D="$(cd "$(dirname "$0")/.." && pwd)"          # experiments/mutation-testing
REPO="$(cd "$D/../.." && pwd)"
OUT="$D/outputs"
RESEARCH="${RESEARCH:-$REPO}"
FDR="${FDR:-/Applications/FDR4.app/Contents/MacOS/refines}"
ISA="${ISA:-$RESEARCH/scripts/isabelle-docker.sh}"
ONLY="${CV_ONLY:-both}"
PY="${PYTHON:-python3}"
[ -f "$OUT/results.csv" ] || { echo "FATAL: $OUT/results.csv missing (regeneration not finished)"; exit 1; }
log(){ echo "$*" | tee -a "$D/verifiers.log" >&2; }
log "=== campaign verifiers $(date '+%F %H:%M:%S') ONLY=$ONLY ==="

# --------------------------------------------------------------------- FDR4
if [ "$ONLY" = both ] || [ "$ONLY" = fdr4 ]; then
  [ -x "$FDR" ] || { echo "FATAL: refines not found at $FDR"; exit 1; }
  [ -f "$D/scripts/apply_corrections_standalone.py" ] || { echo "FATAL: corrections helper missing under $RESEARCH"; exit 1; }
  FO="$D/fdr4"; mkdir -p "$FO"; RES="$FO/results.csv"
  [ -f "$RES" ] || echo "mutant_id,study,status,exit,passed,failed,det_stripped,seconds" > "$RES"
  fdr_one(){  # fdr_one <id> <study>
    local id="$1" study="$2"
    grep -q "^$id," "$RES" && return
    local gen="$OUT/$id/rct-project/csp-gen"
    [ -d "$gen/defs" ] || { echo "$id,$study,nocsp,,,,," >> "$RES"; log "  $id: no csp-gen"; return; }
    local csp; csp=$(ls "$gen"/defs/*_System_Module_coreassertions.csp 2>/dev/null | head -1)
    [ -n "$csp" ] || csp=$(ls "$gen"/defs/*Controller_coreassertions.csp 2>/dev/null | grep -v -e _Module_ -e _Ctrl_ -e _InputEnv_ -e _OutputEnv_ | head -1)
    [ -n "$csp" ] || csp=$(ls "$gen"/defs/*_Module_coreassertions.csp 2>/dev/null | head -1)
    [ -n "$csp" ] || csp=$(ls "$gen"/defs/*_coreassertions.csp 2>/dev/null | head -1)
    [ -n "$csp" ] || { echo "$id,$study,nocsp,,,,," >> "$RES"; return; }
    local base stage nodet det t0 t1 rc p f
    base=$(basename "$csp" .csp); stage="$FO/stage-$id"; rm -rf "$stage"; mkdir -p "$stage"; cp -R "$gen"/. "$stage"/
    "$PY" "$D/scripts/apply_corrections_standalone.py" "$stage" > "$FO/$id.corr.log" 2>&1 || { echo "$id,$study,corrfail,,,,," >> "$RES"; log "  $id: corrections FAILED"; rm -rf "$stage"; return; }
    nodet="$stage/defs/${base}_nodet.csp"
    det=$(grep -cE '^[[:space:]]*assert.*:\[deterministic\]' "$csp" || true)
    if [ "${det:-0}" -gt 0 ]; then perl -pe 's{^(\s*assert.*:\[deterministic\].*)$}{-- determinism not verified (see runners.run_fdr4): $1}' "$csp" > "$nodet"; else cp "$csp" "$nodet"; fi
    t0=$(date +%s)
    ( cd "$stage/defs" && perl -e 'alarm 1200; exec @ARGV' "$FDR" --format framed_json "$nodet" ) > "$FO/$id.json" 2> "$FO/$id.err"; rc=$?
    t1=$(date +%s)
    p=$(grep -o '"result": *1' "$FO/$id.json" | wc -l | tr -d ' '); f=$(grep -o '"result": *0' "$FO/$id.json" | wc -l | tr -d ' ')
    local status=verdict
    if [ "$rc" -eq 142 ] || [ "$rc" -eq 124 ]; then status=TIMEOUT; elif [ "$rc" -eq 137 ]; then status=MEMKILL; elif [ "$p" -eq 0 ] && [ "$f" -eq 0 ]; then status=LOADERR; fi
    echo "$id,$study,$status,$rc,$p,$f,${det:-0},$((t1-t0))" >> "$RES"
    log "  fdr4 $id: $status exit=$rc passed=$p failed=$f ($((t1-t0))s)"
    [ "$status" = LOADERR ] && perl -ne 'while (/"errors": *\[([^]]*)\]/g) { my $e=$1; next unless $e =~ /\S/; $e =~ s/\\n/ /g; print "     $e\n" }' "$FO/$id.json" | head -2 >&2
    rm -rf "$stage"
  }
  log "--- FDR4: baselines (gate) ---"
  for s in lre sranger chemical_detector; do fdr_one "baseline-$s" "$s"; done
  for s in lre sranger chemical_detector; do
    st=$(grep "^baseline-$s," "$RES" | tail -1 | cut -d, -f3); [ "$st" = verdict ] || { echo "FATAL: baseline-$s FDR4 gave $st (harness fault); fix before the mutants"; exit 1; }
  done
  log "--- FDR4: mutants whose CSP changed ---"
  "$PY" - "$OUT/results.csv" <<'PY' | while IFS=, read -r id study; do fdr_one "$id" "$study"; done
import csv, sys
for r in csv.DictReader(open(sys.argv[1])):
    if not r["mutant_id"].startswith("baseline-") and r["csp_changed"] == "yes": print(f'{r["mutant_id"]},{r["study"]}')
PY
  log "FDR4 column done: $(grep -c ',verdict,' "$RES") verdicts, $(grep -cE ',(TIMEOUT|MEMKILL|LOADERR|corrfail|nocsp),' "$RES") environment rows"
fi

# --------------------------------------------------------------------- Isabelle
if [ "$ONLY" = both ] || [ "$ONLY" = isabelle ]; then
  [ -x "$ISA" ] || { echo "FATAL: isabelle wrapper not found at $ISA"; exit 1; }
  if [ -z "${CV_SKIP_DOCKER_CHECK:-}" ]; then docker info >/dev/null 2>&1 || { echo "FATAL: Docker is not running"; exit 1; }; fi
  IO="$D/isabelle"; mkdir -p "$IO"; IRES="$IO/results.csv"
  [ -f "$IRES" ] || echo "mutant_id,study,theory,status,exit,seconds" > "$IRES"
  isa_one(){  # isa_one <id> <study> <thyname>
    local id="$1" study="$2" thy="$3"
    grep -q "^$id,$study,$thy," "$IRES" && return
    local d="$IO/$id-$thy"; rm -rf "$d"; mkdir -p "$d"
    cp "$OUT/$id/isabelle/$thy.thy" "$d/"
    # one session name per (subject, theory): the Isabelle home (heaps/, log/*.db) is shared
    # by every container, so builds that share a session name collide when run in parallel.
    local sess; sess="$(printf '%s' "${id}__${thy}_Check" | tr -c 'A-Za-z0-9_\n' '_')"   # Isabelle session names: letters, digits, underscore
    printf 'session %s = "Z_Machines" +\n  theories\n    %s\n' "$sess" "$thy" > "$d/ROOT"
    local t0 t1 rc status
    t0=$(date +%s)
    perl -e 'alarm 1800; exec @ARGV' bash "$ISA" build -v -o timeout=600 -D "$d" > "$d/build.log" 2>&1; rc=$?
    t1=$(date +%s)
    if [ "$rc" -eq 142 ] || [ "$rc" -eq 124 ]; then status=TIMEOUT
    elif [ "$rc" -eq 137 ]; then status=MEMKILL
    elif grep -q "^Finished ${sess}" "$d/build.log"; then status=verified
    elif grep -q "^\*\*\* Timeout" "$d/build.log"; then status=goal_timeout
    elif grep -qE "Failed to finish proof|\*\*\* " "$d/build.log"; then status=proof_failed
    elif grep -qiE "Cannot connect to the Docker daemon|docker: error" "$d/build.log"; then status=DOCKERDOWN
    else status=UNKNOWN; fi
    echo "$id,$study,$thy,$status,$rc,$((t1-t0))" >> "$IRES"
    log "  isabelle $id/$thy: $status ($((t1-t0))s)"
    [ "$status" = DOCKERDOWN ] && { log "Docker went away; stopping (rerun to resume)"; exit 3; }
    rm -f "$d/$thy.thy"
  }
  log "--- Isabelle: baselines (gate) ---"
  for s in lre sranger chemical_detector; do for t in "$OUT/baseline-$s"/isabelle/*_Beh.thy; do isa_one "baseline-$s" "$s" "$(basename "$t" .thy)"; done; done
  for s in lre sranger chemical_detector; do
    grep "^baseline-$s," "$IRES" | grep -vq ",verified," && { echo "FATAL: a baseline-$s theory did not verify (harness or environment fault); fix before the mutants"; exit 1; }
  done
  log "--- Isabelle: theories that differ from the baseline (CV_PAR=${CV_PAR:-2}, Movement serial) ---"
  export -f isa_one log; export OUT IO IRES ISA D
  "$PY" - "$OUT" <<'PY' > "$IO/plan.txt"
import csv, glob, os, re, sys
OUT=sys.argv[1]
def strip(t): return " ".join(re.sub(r"\(\*.*?\*\)","",t,flags=re.S).split())
base={s:{os.path.basename(p):strip(open(p).read()) for p in glob.glob(f"{OUT}/baseline-{s}/isabelle/*_Beh.thy")} for s in ("lre","sranger","chemical_detector")}
for r in csv.DictReader(open(f"{OUT}/results.csv")):
    mid=r["mutant_id"]
    if mid.startswith("baseline-"): continue
    for p in sorted(glob.glob(f"{OUT}/{mid}/isabelle/*_Beh.thy")):
        n=os.path.basename(p)
        if strip(open(p).read()) != base[r["study"]].get(n): print(f"{mid},{r['study']},{n[:-4]}")
PY
  # the Movement theories are the memory-heavy ones: run the rest at CV_PAR (default 2), then Movement serially
  grep -v ',MovementController_Beh$' "$IO/plan.txt" | tr ',' ' ' | xargs -P "${CV_PAR:-2}" -L 1 bash -c 'isa_one "$0" "$1" "$2"'
  grep ',MovementController_Beh$' "$IO/plan.txt" | tr ',' ' ' | xargs -P "${CV_PAR_MOVEMENT:-1}" -L 1 bash -c 'isa_one "$0" "$1" "$2"'
  log "Isabelle column done: $(grep -c ',verified,' "$IRES") verified, $(grep -c ',proof_failed,' "$IRES") failed, $(grep -c ',goal_timeout,' "$IRES") goal timeouts (inconclusive), $(grep -cE ',(TIMEOUT|MEMKILL|UNKNOWN|DOCKERDOWN),' "$IRES") environment rows"
fi
log "=== DONE $(date '+%F %H:%M:%S') ==="
log "  Paste $D/verifiers.log back. Environment rows are rerun by running this script again."
