#!/bin/bash
# run_mutation_isabelle_modelB.sh -- the Isabelle column of the 186-mutant campaign
# (experiments/mutation-testing) re-run on theories the repaired generator
# (environment model B) produces from the campaign's archived per-mutant
# robochart_model.xmi. Builds only the theories that differ from the study's
# baseline (plan.tsv: 131 builds for 125 mutants) plus the three baselines.
#
# Run in YOUR terminal with Docker Desktop up. Sequential it is ~10 h at the
# probe's timings; two shards in two terminals halve that if memory allows
# (each Isabelle ~5 GB):
#     B6_SHARD=0/2 bash .../run_mutation_isabelle_modelB.sh
#     B6_SHARD=1/2 bash .../run_mutation_isabelle_modelB.sh
# It resumes: a build with a complete log is not repeated. Results:
#     /tmp/b6-mut/out/results.csv   (mutant_id, study, theory, outcome, seconds)
# Paste results.csv back (or the SUMMARY lines).
set -u
KIT="$(cd "$(dirname "$0")" && pwd)"; C_REPO="$(cd "$KIT/../../.." && pwd)"
CAP="${B6_CAP:-1800}"; THREADS="${B6_THREADS:-4}"
ONLY="${B6_ONLY_LIST:-}"
MINFREE_GB="${B6_MINFREE:-6}"
ISA="${B6_ISA:-$C_REPO/scripts/isabelle-docker.sh}"
SHARD="${B6_SHARD:-0/1}"; SI=${SHARD%/*}; SN=${SHARD#*/}
T=/tmp/b6-mut; OUT=$T/out; mkdir -p "$OUT"; RES="$OUT/results.csv"
[ -f "$RES" ] || echo "mutant_id,study,theory,outcome,seconds" > "$RES"
log(){ echo "$*" | tee -a "$OUT/SUMMARY.txt" >&2; }
free_gb(){ vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}'; }
if [ -z "${B6_SKIP_DOCKER_CHECK:-}" ] && ! docker info >/dev/null 2>&1; then echo "Docker is not running"; exit 2; fi
if [ -n "${B6_REQUIRE_MAXHEAP:-}" ]; then
  eff=$("$ISA" getenv ML_OPTIONS 2>/dev/null | tail -1)
  echo "effective $eff"
  case "$eff" in *maxheap*) ;; *) echo "FATAL: the Poly/ML heap ceiling is not in effect -- not starting"; exit 4;; esac
fi
log "=== MUTATION CAMPAIGN, ISABELLE COLUMN UNDER MODEL B  shard $SHARD  start $(date '+%F %H:%M:%S')  cap=${CAP}s ==="
build(){ # mutant study theoryfile
  mid="$1"; study="$2"; thy="$3"; name="${thy%.thy}"; sess="${name}_$(echo "$mid" | tr - _)"   # Isabelle session names take no hyphen
  d="$OUT/$mid/$name"
  if [ -f "$d/build.log" ] && grep -qE "^Finished $sess|^\*\*\*|Timeout" "$d/build.log" && ! grep -q "ROOT\"\] error" "$d/build.log" && ! grep -qE "Killed.*POLYML_EXE" "$d/build.log" && ! grep -qiE "Out of memory" "$d/build.log"; then return; fi
  rm -rf "$d"; mkdir -p "$d"; cp "$KIT/theories/$mid/$thy" "$d/"
  printf 'session %s = "Z_Machines" +\n  theories\n    %s\n' "$sess" "$name" > "$d/ROOT"
  if [ -z "${B6_SKIP_DOCKER_CHECK:-}" ] && ! docker info >/dev/null 2>&1; then log "  $mid/$name: DOCKER DOWN -- stopping"; exit 3; fi
  while awk -v f="$(free_gb)" -v m="$MINFREE_GB" 'BEGIN{exit !(f<m)}'; do log "  waiting: free memory $(free_gb) GB < ${MINFREE_GB} GB"; sleep 60; done
  start=$(date +%s)
  ( cd "$d" && exec "$ISA" build -D . -o "timeout=$CAP" -o "threads=$THREADS" -o quick_and_dirty=false -v "$sess" ) > "$d/build.log" 2>&1 &
  pid=$!; el=0; why=""
  while kill -0 $pid 2>/dev/null; do
    if awk -v f="$(free_gb)" 'BEGIN{exit !(f<1.5)}'; then kill -9 $pid 2>/dev/null; why=MEMKILL; break; fi
    [ $el -ge $((CAP + 900)) ] && { kill -9 $pid 2>/dev/null; why=HARDKILL; break; }
    sleep 2; el=$((el+2))
  done
  wait $pid 2>/dev/null; rc=$?; dur=$(( $(date +%s) - start )); L="$d/build.log"
  if [ -n "$why" ]; then v="$why";
  elif grep -q "^Finished $sess" "$L"; then v="verified";
  elif grep -qE "Killed.*POLYML_EXE" "$L"; then v="OOMKILL";
  elif grep -qiE "Out of memory|exception Interrupt raised|Poly/ML.*(memory|heap)" "$L"; then v="ML_OOM";   # heap ceiling reached: environment outcome   # Poly/ML killed inside the Docker VM: environment outcome
  elif grep -qE "^\*\*\* (Type unification failed|Extra variables on rhs|Undefined|Inner syntax error)" "$L"; then v="ill_formed";   # theory does not typecheck: no proof was attempted
  elif grep -qiE "Timeout|timed out" "$L"; then v="timeout";
  elif grep -q '\*\*\*' "$L"; then v="proof_failed";
  else v="unknown_rc=$rc"; fi
  echo "$mid,$study,$name,$v,$dur" >> "$RES"
  log "  $mid/$name: $v (${dur}s)"
}
i=0
# baselines first (shard 0 only)
if [ "$SI" = "0" ]; then for s in lre sranger chemical_detector; do for f in "$KIT/theories/baseline-$s"/*.thy; do build "baseline-$s" "$s" "$(basename "$f")"; done; done; fi
while IFS=$'\t' read -r mid study thy; do
  if [ -n "$ONLY" ] && ! grep -qx "$mid$(printf '\t')${thy%.thy}" "$ONLY"; then i=$((i+1)); continue; fi
  if [ -n "$ONLY" ] || [ $((i % SN)) = "$SI" ]; then build "$mid" "$study" "$thy"; fi
  i=$((i+1))
done < "$KIT/plan.tsv"
log "=== DONE shard $SHARD $(date '+%F %H:%M:%S') ===  verified/proof_failed are verdicts; timeout/MEMKILL/HARDKILL are environment outcomes to rerun."
