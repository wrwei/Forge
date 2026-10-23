#!/bin/bash
# verify_regenerated.sh -- build every theory the PATCHED generator produced
# (scripts/envmodel_b/regenerated/<study>/*.thy) in the pipeline's Docker
# Isabelle, one session each. Run in YOUR terminal with Docker Desktop up:
#     bash ~/Gitee/formal_method_guided_vibe_coding/scripts/envmodel_b/verify_regenerated.sh
# One study:   B6_ONLY=lre bash .../verify_regenerated.sh
# Paste /tmp/b6-regen/out/SUMMARY.txt back. Cap per build: B6_CAP (default 2400 s;
# the Boiler needed 1793 s in the probe).
set -u
KIT="$(cd "$(dirname "$0")" && pwd)"; C_REPO="$(cd "$KIT/../.." && pwd)"
CAP="${B6_CAP:-2400}"; THREADS="${B6_THREADS:-4}"; MINFREE_GB="${B6_MINFREE:-6}"
ISA="${B6_ISA:-$C_REPO/scripts/isabelle-docker.sh}"
T=/tmp/b6-regen; OUT=$T/out; mkdir -p "$OUT"
log(){ echo "$*" | tee -a "$OUT/SUMMARY.txt" >&2; }
free_gb(){ vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}'; }
if [ -z "${B6_SKIP_DOCKER_CHECK:-}" ] && ! docker info >/dev/null 2>&1; then echo "Docker is not running"; exit 2; fi
log ""; log "=== REGENERATED-THEORY VERIFICATION  start $(date '+%F %H:%M:%S')  cap=${CAP}s/build ==="
for s in ${B6_ONLY:-lre sranger gas movement boiler}; do
  log ""; log "--- study $s"
  for thy in "$KIT/regenerated/$s"/*.thy; do
    name=$(basename "$thy" .thy); sess="${name}_Regen"
    d="$OUT/$s/$name"; rm -rf "$d"; mkdir -p "$d"; cp "$thy" "$d/"
    printf 'session %s = "Z_Machines" +\n  theories\n    %s\n' "$sess" "$name" > "$d/ROOT"
    if [ -z "${B6_SKIP_DOCKER_CHECK:-}" ] && ! docker info >/dev/null 2>&1; then log "  $name: DOCKER DOWN -- stopping"; exit 3; fi
    if awk -v f="$(free_gb)" -v m="$MINFREE_GB" 'BEGIN{exit !(f<m)}'; then log "  $name: SKIPPED -- free memory $(free_gb) GB < ${MINFREE_GB} GB"; continue; fi
    start=$(date +%s)
    ( cd "$d" && exec "$ISA" build -D . -o "timeout=$CAP" -o "threads=$THREADS" -o quick_and_dirty=false -v "$sess" ) > "$d/build.log" 2>&1 &
    pid=$!; el=0; why=""
    while kill -0 $pid 2>/dev/null; do
      if awk -v f="$(free_gb)" 'BEGIN{exit !(f<1.5)}'; then kill -9 $pid 2>/dev/null; docker ps -q --filter ancestor=forge/isabelle:2023-cyphyassure 2>/dev/null | xargs docker kill >/dev/null 2>&1; why=MEMKILL; break; fi
      [ $el -ge $((CAP + 900)) ] && { kill -9 $pid 2>/dev/null; why=HARDKILL; break; }
      sleep 5; el=$((el+5))
    done
    wait $pid 2>/dev/null; rc=$?; dur=$(( $(date +%s) - start )); L="$d/build.log"
    if [ -n "$why" ]; then v="$why";
    elif grep -q "^Finished $sess" "$L"; then v="PASS  ($(grep -c '^lemma' "$thy") lemmas)";
    elif grep -qiE "Timeout|timed out" "$L"; then v="TIMEOUT";
    elif grep -q '\*\*\*' "$L"; then g=$(grep -m1 -oE '\(line [0-9]+ of "[^"]*"\)' "$L" | sed -E 's/\(line ([0-9]+) of "[^"]*\/([^"/]*)"\)/\2:\1/'); msg=$(grep -m1 -A1 '^\*\*\*' "$L" | tail -1 | cut -c1-90); v="FAIL(${g:-?}) ${msg}";
    else v="UNKNOWN rc=$rc"; fi
    log "  $name: $v  (${dur}s)"
  done
done
log ""; log "=== DONE $(date '+%F %H:%M:%S') ===  Every row PASS = the patched generator reproduces the probe result. TIMEOUT/MEMKILL/SKIPPED are environment outcomes."
