#!/bin/bash
# Diagnose the 11 Isabelle builds that did not close within budget in
# isabelle_arm.sh, by ISOLATING which obligation is responsible.
#
#   bash scripts/isabelle_timeouts.sh
#
# Why isolation rather than a bigger budget: `metis` hangs are not reliably
# bounded, so raising the cap can burn hours and still end in "inconclusive".
# The question worth answering is narrower and cheap: are the invariant and
# R1 obligations discharging, with only the deadlock-freedom proof failing to
# close? If so, the experiment can report the incomplete builds precisely
# ("the deadlock-freedom obligation did not close; all N invariant
# obligations did") instead of as an opaque timeout.
#
# Evidence this is the right cut (measured 2026-08-31, all 11 checked):
#   * 9 of the 11 are dispatch-inversion mutations (`currentMode == X` -> `!=`)
#     and every one of those restructures the extracted machine rather than
#     perturbing a single guard — zoperation counts against their baselines:
#       lre_cmp_swap_000/001/002        16 -> 13 / 10 / 14
#       sranger_cmp_swap_000/001/002     8 ->  5 /  5 /  7
#       chemical_detector_cmp_swap_1/2/3 11 -> 14 /  8 / 10
#   * The remaining 2 do NOT restructure: lre_relop_flip_008 (a threshold flip,
#     `cda >= minSafeDist` -> `cda <`) and lre_branch_swap_002 (a swapped mode
#     constant, `== LreMode.MOM` -> `== LreMode.HCM`) both leave the machine at
#     16 zoperations / 32 invariant obligations, identical to baseline. Their
#     timeouts therefore have a DIFFERENT and unestablished cause — guard
#     arithmetic being the obvious candidate, since the generator's FORK note
#     records `auto` timing out on LRE's real-arithmetic guards. This script
#     does not assume the two groups share a mechanism; it runs the same two
#     variants over both so the isolation result is per-mutant.
#   * The generator's own FORK comment in the emitted theory records that
#     `by (metis St.exhaust_disc)` is the LRE-style closing tactic for
#     `deadlock_free` and that it HANGS (>10 min) when a state's only
#     transition consumes a typed payload — i.e. the known failure mode of
#     this proof is a hang, not a refutation.
#
# Variants per mutant (each in its own scratch copy; staged trees untouched):
#   A  no-dlf   — deadlock_free lemma commented out. If this verifies, the
#                 invariant/R1 obligations are fine and the cost is localized.
#   B  alt-tac  — deadlock_free closing tactic swapped to the other FORK
#                 branch (`using St.exhaust_disc by auto`). Distinguishes
#                 "tactic selection is fragile under restructuring" from
#                 "the goal is genuinely out of reach". NOT a proposed fix to
#                 the generator: the FORK comment states each branch is
#                 required for its own study, so this is diagnostic only.
#
# Output: /tmp/campaign/isabelle-timeouts/results.csv
#   mutant_id,variant,exit,finished,errors,seconds,outcome
set -u
REPO="$(cd "$(dirname "$0")/.." && pwd)"
ISA="$REPO/scripts/isabelle-docker.sh"
CAMP=/tmp/campaign
ARM=$CAMP/isabelle-arm
OUT=$CAMP/isabelle-timeouts
mkdir -p "$OUT"
RES="$OUT/results.csv"
[ -f "$RES" ] || echo "mutant_id,variant,exit,finished,errors,seconds,outcome" > "$RES"
log () { echo "$@"; }

GOAL_TIMEOUT=${GOAL_TIMEOUT:-300}
WALL_CAP=${WALL_CAP:-420}

[ -x "$ISA" ] || { echo "missing $ISA"; exit 2; }
if ! docker info >/dev/null 2>&1; then
  log "!! Docker is not running — start Docker Desktop and re-run."
  exit 2
fi
[ -f "$ARM/results.csv" ] || { echo "no arm results at $ARM/results.csv"; exit 2; }

# The timeout set is READ from the arm's own results, not hardcoded.
TIMEOUTS=$(awk -F, 'NR>1 && $6=="timeout" {print $1}' "$ARM/results.csv")
n=$(echo "$TIMEOUTS" | grep -c . || true)
log "=== $n timed-out mutants read from $ARM/results.csv ==="
[ "$n" -gt 0 ] || { log "nothing to diagnose"; exit 0; }

run_variant () {   # run_variant <mutant_id> <variant> <sed-program>
  local mid="$1" variant="$2" prog="$3"
  local label="$mid.$variant"
  if [ -f "$OUT/$label.log" ]; then log "  $variant: log exists — skipped (resume)"; return; fi
  local src="$CAMP/outputs/$mid/isabelle"
  [ -f "$src/ROOT" ] || { log "  $variant: no ROOT — skipped"; return; }
  local work="$OUT/work-$label"
  rm -rf "$work"; mkdir -p "$work"
  cp "$src"/*.thy "$src"/ROOT "$work"/ 2>/dev/null
  # Apply the variant edit to the Beh theory in the scratch copy only.
  local thy
  thy=$(ls "$work"/*_Beh.thy 2>/dev/null | head -1)
  [ -n "$thy" ] || { log "  $variant: no _Beh.thy — skipped"; return; }
  perl -0pi -e "$prog" "$thy"
  local t0 t1 rc fin err outcome
  t0=$(date +%s)
  ( cd "$work" && perl -e 'alarm shift; exec @ARGV' "$WALL_CAP" \
      "$ISA" build -D . -v -o "timeout=$GOAL_TIMEOUT" ) > "$OUT/$label.log" 2>&1
  rc=$?
  t1=$(date +%s)
  fin=$(grep -c "^Finished " "$OUT/$label.log" 2>/dev/null || true)
  err=$(grep -c "^\*\*\*" "$OUT/$label.log" 2>/dev/null || true)
  if   [ "$rc" -eq 0 ] && [ "${fin:-0}" -gt 0 ];                then outcome=verified
  elif grep -q "^\*\*\* Timeout" "$OUT/$label.log" 2>/dev/null; then outcome=timeout
  elif [ "$rc" -eq 142 ] || [ "$rc" -eq 124 ];                  then outcome=wall_capped
  elif [ "${err:-0}" -gt 0 ];                                   then outcome=proof_failed
  else                                                               outcome=unknown
  fi
  echo "$mid,$variant,$rc,${fin:-0},${err:-0},$((t1-t0)),$outcome" >> "$RES"
  log "  $variant: $outcome (exit=$rc finished=$fin errors=$err, $((t1-t0))s)"
  rm -rf "$work"
}

# Variant A: comment out the deadlock_free lemma and its proof lines.
# The lemma is the last one in the theory, followed by the FORK comment and
# `end`; commenting the three proof lines plus the statement is sufficient.
PROG_A='s/^(lemma\s+\w*deadlock_free.*?\n(?:\s+(?:unfolding|apply|by|using).*\n)+)/join("", map { "(* diag-A: " . $_ . " *)\n" } split(\/\n\/, $1))/gme'
# Variant B: swap the closing tactic to the other FORK branch.
PROG_B='s/^(\s*)by \(metis St\.exhaust_disc\)\s*$/$1using St.exhaust_disc by auto/gm'

for mid in $TIMEOUTS; do
  log ""
  log "=== $mid ==="
  run_variant "$mid" "no-dlf"  "$PROG_A"
  run_variant "$mid" "alt-tac" "$PROG_B"
done

log ""
log "==== results: $RES ===="
log "Done. Tell the assistant results are in $OUT/"
