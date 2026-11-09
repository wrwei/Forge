#!/usr/bin/env bash
# Build the baseline theory and every Isabelle mutant with the pipeline's wrapper and
# per-goal timeout. Each build gets its own session name (ROOT rewritten in place), so its
# full error log survives and can be read back with `isabelle build_log -H Error <session>`;
# the build's own output truncates long error lists from the front.
# One CSV line per build: id, verdict, seconds, failing lemma lines (from the full log).
#   usage: run_isabelle.sh [mutant-id ...]      (default: baseline and every mutants/PI*)
cd "$(dirname "$0")"
ISA=${ISA:-/Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/isabelle-docker.sh}
TO=${TO:-600}   # pipeline.yaml proof_timeout
PFX=${PFX:-propmut_lre}
mkdir -p results
build() { id=$1; dir=$2; sess="${PFX}_${id}"
  base=$(grep -oE '^session +[A-Za-z0-9_]+' "$dir/ROOT" | awk '{print $2}')
  sed -i '' -E "s/^session +[A-Za-z0-9_]+/session $sess/" "$dir/ROOT"
  t0=$(date +%s)
  "$ISA" build -D "$dir" -v -o timeout=$TO > "results/$id.isabelle.log" 2>&1; rc=$?
  t=$(( $(date +%s) - t0 ))
  "$ISA" build_log -H Error "$sess" > "results/$id.isabelle.errors" 2>/dev/null
  v=FAIL; [ $rc -eq 0 ] && v=PASS
  lines=$(grep -oE '\(line [0-9]+ of' "results/$id.isabelle.errors" | grep -oE '[0-9]+' | sort -n | uniq | tr '\n' ' ')
  kinds=$(grep -oE 'Timeout|Failed to finish proof|Failed to apply (initial )?proof method|Interrupt' "results/$id.isabelle.errors" | sort | uniq -c | tr -s ' \n' ' ')
  echo "$id,$v,$t,\"lines: $lines\",\"$kinds\""; }
OUT=results_isabelle.csv
if [ $# -eq 0 ]; then
  build baseline baseline/isabelle | tee $OUT
  for d in mutants/PI*/; do id=$(basename $d); build $id $d/isabelle | tee -a $OUT; done
else
  for id in "$@"; do d=mutants/$id; [ $id = baseline ] && d=baseline; build $id $d/isabelle | tee -a $OUT; done
fi
echo "=== DONE: $(grep -c ,PASS, $OUT) PASS, $(grep -c ,FAIL, $OUT) FAIL of $(wc -l < $OUT)"
