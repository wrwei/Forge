#!/usr/bin/env bash
# Same builds as run_isabelle.sh (same wrapper, timeout and per-mutant sessions), run three at a
# time; each build is an independent docker run. The verdict of a build does not depend on the
# others. Results are assembled into results_isabelle.csv in catalogue order at the end.
#   usage: run_isabelle_parallel.sh [jobs]     (default 3)
cd "$(dirname "$0")"; J=${1:-3}
export ISA=${ISA:-/Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/isabelle-docker.sh} TO=${TO:-600} PFX=${PFX:-propmut_lre}
mkdir -p results/parts
build() { id=$1; dir=$2; sess="${PFX}_${id}"
  sed -i '' -E "s/^session +[A-Za-z0-9_]+/session $sess/" "$dir/ROOT"
  t0=$(date +%s); "$ISA" build -D "$dir" -v -o timeout=$TO > "results/$id.isabelle.log" 2>&1; rc=$?; t=$(( $(date +%s) - t0 ))
  "$ISA" build_log -H Error "$sess" > "results/$id.isabelle.errors" 2>/dev/null
  v=FAIL; [ $rc -eq 0 ] && v=PASS
  lines=$(grep -oE '\(line [0-9]+ of' "results/$id.isabelle.errors" | grep -oE '[0-9]+' | sort -n | uniq | tr '\n' ' ')
  kinds=$(grep -oE 'Timeout|Failed to finish proof|Failed to apply (initial )?proof method|Interrupt' "results/$id.isabelle.errors" | sort | uniq -c | tr -s ' \n' ' ')
  echo "$id,$v,$t,\"lines: $lines\",\"$kinds\"" > "results/parts/$id.csv"; echo "done $id $v ${t}s"; }
export -f build
{ echo "baseline baseline/isabelle"; for d in mutants/PI*/; do echo "$(basename $d) $d/isabelle"; done; } | xargs -P "$J" -L 1 bash -c 'build "$0" "$1"'
{ cat results/parts/baseline.csv; for d in mutants/PI*/; do cat "results/parts/$(basename $d).csv"; done; } > results_isabelle.csv
echo "=== DONE: $(grep -c ,PASS, results_isabelle.csv) PASS, $(grep -c ,FAIL, results_isabelle.csv) FAIL of $(wc -l < results_isabelle.csv)"
