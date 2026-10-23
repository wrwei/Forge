#!/bin/bash
# run_b6_probe.sh -- Isabelle probe for the pre-registered environment model B
# (../preregistration.md).
#
# Run in YOUR terminal (Docker Desktop must be running; the pipeline's Isabelle
# is the linux/amd64 container behind scripts/isabelle-docker.sh):
#     bash /tmp/b6-probe/run_b6_probe.sh
# One study only:      B6_ONLY=lre bash /tmp/b6-probe/run_b6_probe.sh
# Skip baseline arm:   B6_BASELINE=0 bash /tmp/b6-probe/run_b6_probe.sh
# Proof cap (s/build): B6_CAP=1800 (default)
# Paste /tmp/b6-probe/out/SUMMARY.txt back.
#
# What runs, per study (lre sranger gas movement boiler):
#   A0     the UNMODIFIED model-A theory as shipped (toolchain + heap sanity;
#          a fail here is an environment fault for the whole study)
#   NoDlf  model B, deadlock-freedom lemma removed  (invariant lemmas only)
#   Dg     DIAGNOSTIC: `apply deadlock_free` then `oops` -- times the method alone; never a pass
#   S1     explicit case split (case_tac st; simp; elim disjE; simp) then `blast+`   (first real arm)
#   S2     same split, then `metis ...+`
#   Au     deadlock-freedom closed by `using St.exhaust_disc Evt.exhaust_disc by auto`
#   M      closed by `metis St.exhaust_disc Evt.exhaust_disc Evt.distinct singletonD insertCI`
#   Bl     closed by `by blast`
# Rows already measured (PASS/TIMEOUT) in $B6_PREV (default: results/latest in this
# folder) are reused, not rerun. Theories are staged into /tmp/b6-probe by the rewriter.
# Arms Bl/M/Au stop early once one of them passes for a study (the decision
# rule needs only one).
#
# Reading the rows: PASS / FAIL(goal) / TIMEOUT / MEMKILL / ENV-BROKEN.
# TIMEOUT, MEMKILL and ENV-BROKEN are environment outcomes, not verdicts.
set -u
CAP="${B6_CAP:-1800}"
ONLY="${B6_ONLY:-}"
DO_BASE="${B6_BASELINE:-1}"
MINFREE_GB="${B6_MINFREE:-6}"
THREADS="${B6_THREADS:-4}"
KIT="$(cd "$(dirname "$0")" && pwd)"
PREV="${B6_PREV:-$KIT/results/latest}"
C_REPO=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
ISA="${B6_ISA:-$C_REPO/scripts/isabelle-docker.sh}"
F=/Users/ranwei/Git/Forge/experiments
T=/tmp/b6-probe
OUT=$T/out
src_of(){ case "$1" in
  lre)      echo "$F/convergence-v3-frozen/lre/run-5/iter-1/formal-artefacts/isabelle";;
  sranger)  echo "$F/convergence-v3-frozen/sranger/run-1/iter-1/formal-artefacts/isabelle";;
  gas)      echo "$F/convergence-v3-frozen/chemical_detector/run-1/iter-1/formal-artefacts/isabelle";;
  movement) echo "$F/multicontroller-e/chemical_detector/run-1/iter-1/formal-artefacts/isabelle";;
  boiler)   echo "$F/steam-boiler/rerun-2026-09-28/formal-artefacts/isabelle";;
esac; }
thy_of(){ case "$1" in
  lre) echo LreController_Beh;; sranger) echo SRangerController_Beh;; gas) echo GasAnalysisController_Beh;;
  movement) echo Movement_Beh;; boiler) echo BoilerController_Beh;; esac; }
STUDIES="lre sranger gas movement boiler"
[ -n "$ONLY" ] && STUDIES="$ONLY"

mkdir -p "$OUT"
log(){ echo "$*" | tee -a "$OUT/SUMMARY.txt" >&2; }
free_gb(){ vm_stat | awk '/Pages free/{f=$3} /Pages inactive/{i=$3} END{printf "%.1f", (f+i)*16384/1e9}'; }

log ""
log "=== ENVIRONMENT-MODEL PROBE  start $(date '+%F %H:%M:%S')  cap=${CAP}s/build  studies: $STUDIES ==="
[ -x "$ISA" ] || { log "  FAIL: no isabelle wrapper at $ISA"; exit 1; }
if [ -z "${B6_SKIP_DOCKER_CHECK:-}" ] && ! docker info >/dev/null 2>&1; then
  log "  FAIL: Docker is not running -- start Docker Desktop, then rerun."; exit 1
fi
log "  docker: running | free memory now: $(free_gb) GB"

# toolchain gate: a trivial HOL theory must build (same gate as the September probe)
mkdir -p "$OUT/gate"
printf 'theory Probe_Gate\n  imports Main\nbegin\nlemma g: "(1::nat) + 1 = 2" by simp\nend\n' > "$OUT/gate/Probe_Gate.thy"
printf 'session Probe_Gate = "HOL" +\n  theories\n    Probe_Gate\n' > "$OUT/gate/ROOT"
( cd "$OUT/gate" && exec "$ISA" build -D . -v ) > "$OUT/gate/build.log" 2>&1
if [ $? -ne 0 ]; then
  log "  GATE FAIL: Isabelle cannot build a trivial HOL theory. ENVIRONMENT fault; log head:"
  sed -n '1,6p' "$OUT/gate/build.log" | sed 's/^/     /' | tee -a "$OUT/SUMMARY.txt"; exit 1
fi
log "  toolchain gate: OK"

# build_one <dir> <session> <label>  -> echoes verdict word, logs the row, writes <dir>/build.log
build_one(){
  d="$1"; sess="$2"; label="$3"
  prevlog="$PREV/${d#$OUT/}/build.log"
  prevlog2="$PREV/${s}-$(basename "$d").build.log"; [ -f "$prevlog" ] || prevlog="$prevlog2"
  if [ -f "$prevlog" ] && grep -q "^Finished $sess" "$prevlog"; then
    log "  $label: PASS  (measured in previous run)"; cp "$prevlog" "$d/build.log"; echo PASS; return; fi
  if [ -f "$prevlog" ] && grep -q "Running $sess " "$prevlog" && grep -q "^\*\*\* Timeout" "$prevlog"; then
    log "  $label: TIMEOUT  (measured in previous run at ${CAP}s; not rerun)"; cp "$prevlog" "$d/build.log"; echo TIMEOUT; return; fi
  if [ -z "${B6_SKIP_DOCKER_CHECK:-}" ] && ! docker info >/dev/null 2>&1; then
    log "  $label: DOCKER DOWN -- stopping the run here. Restart Docker Desktop and rerun; finished rows are kept."; exit 3; fi
  if awk -v f="$(free_gb)" -v m="$MINFREE_GB" 'BEGIN{exit !(f<m)}'; then
    log "  $label: SKIPPED -- free memory $(free_gb) GB < ${MINFREE_GB} GB (close apps or set B6_MINFREE)"; echo SKIP; return
  fi
  start=$(date +%s)
  ( cd "$d" && exec "$ISA" build -D . -o "timeout=$CAP" -o "threads=$THREADS" -o quick_and_dirty=false -v "$sess" ) > "$d/build.log" 2>&1 &
  pid=$!; el=0; why=""
  while kill -0 $pid 2>/dev/null; do
    if awk -v f="$(free_gb)" 'BEGIN{exit !(f<1.5)}'; then
      kill -9 $pid 2>/dev/null
      docker ps -q --filter ancestor=forge/isabelle:2023-cyphyassure 2>/dev/null | xargs docker kill >/dev/null 2>&1
      why=MEMKILL; break
    fi
    [ $el -ge $((CAP + 900)) ] && { kill -9 $pid 2>/dev/null; why=HARDKILL; break; }
    sleep 5; el=$((el+5))
  done
  wait $pid 2>/dev/null; rc=$?
  dur=$(( $(date +%s) - start ))
  L="$d/build.log"
  if [ -n "$why" ]; then v="$why";
  elif grep -q "^Finished $sess" "$L"; then v="PASS";
  elif grep -qiE "Timeout|timed out" "$L"; then v="TIMEOUT";
  elif grep -q '\*\*\*' "$L"; then
    g=$(grep -m1 -oE '\(line [0-9]+ of "[^"]*"\)' "$L" | sed -E 's/\(line ([0-9]+) of "[^"]*\/([^"/]*)"\)/\2:\1/')
    msg=$(grep -m1 -A1 '^\*\*\*' "$L" | tail -1 | cut -c1-90)
    v="FAIL(${g:-?}) ${msg}";
  elif [ "$rc" = "127" ] || ! grep -qE "Session|Running|Building|theory" "$L"; then v="ENV-BROKEN rc=$rc";
  else v="UNKNOWN rc=$rc"; fi
  log "  $label: $v  (${dur}s)"
  echo "$v" | cut -d' ' -f1 | cut -d'(' -f1
}

for s in $STUDIES; do
  src="$(src_of $s)"; thy="$(thy_of $s)"
  log ""; log "--- study $s ($thy)"
  [ -f "$src/$thy.thy" ] || { log "  FAIL: missing $src/$thy.thy"; continue; }
  # stage the model-B theories from the rewriter if this machine has none yet
  if [ ! -f "$T/$s/${thy}_B3_Bl.thy" ]; then
    mkdir -p "$T/$s"
    python3 "$KIT/envmodel_b_rewrite.py" "$src/$thy.thy" "$T/$s" > "$T/$s/rewrite.log" 2>&1 \
      || { log "  FAIL: rewriter failed for $s (see $T/$s/rewrite.log)"; continue; }
    log "  staged model-B theories: $(sed -n '$p' "$T/$s/rewrite.log")"
  fi
  if [ "$DO_BASE" = "1" ]; then
    d="$OUT/$s/A0"; rm -rf "$d"; mkdir -p "$d"; cp "$src/$thy.thy" "$d/"
    printf 'session %s_A0 = "Z_Machines" +\n  theories\n    %s\n' "$thy" "$thy" > "$d/ROOT"
    r=$(build_one "$d" "${thy}_A0" "A0 (shipped model-A theory, unmodified)")
    [ "$r" = "PASS" ] || log "  !! A0 did not pass: treat every B arm of '$s' as unmeasured until this is understood."
  fi
  d="$OUT/$s/NoDlf"; rm -rf "$d"; mkdir -p "$d"; cp "$T/$s/${thy}_B3_NoDlf.thy" "$d/"
  printf 'session %s_B3_NoDlf_Check = "Z_Machines" +\n  theories\n    %s_B3_NoDlf\n' "$thy" "$thy" > "$d/ROOT"
  build_one "$d" "${thy}_B3_NoDlf_Check" "NoDlf (model B, invariant lemmas only)" >/dev/null
  for arm in Dg S1 S2 Au M Bl; do
    d="$OUT/$s/$arm"; rm -rf "$d"; mkdir -p "$d"; cp "$T/$s/${thy}_B3_$arm.thy" "$d/"
    printf 'session %s_B3_%s_Check = "Z_Machines" +\n  theories\n    %s_B3_%s\n' "$thy" "$arm" "$thy" "$arm" > "$d/ROOT"
    if [ "$arm" = "Dg" ]; then
      build_one "$d" "${thy}_B3_Dg_Check" "Dg (DIAGNOSTIC: deadlock_free method only, lemma left open -- not a proof)" >/dev/null
      continue
    fi
    r=$(build_one "$d" "${thy}_B3_${arm}_Check" "$arm (model B, deadlock-freedom via $arm)")
    [ "$r" = "PASS" ] && { log "  -> deadlock-freedom closes under model B for $s (arm $arm); skipping remaining arms"; break; }
  done
done

log ""; log "=== DONE $(date '+%F %H:%M:%S') ==="
log "  Decision rule (pre-registered): ADOPT B iff every study has NoDlf=PASS and one of S1/S2/Au/M/Bl=PASS (Dg never counts)."
log "  TIMEOUT / MEMKILL / SKIPPED / ENV-BROKEN rows are environment outcomes: rerun them, do not read them as verdicts."
log "  Paste this file back."
