#!/usr/bin/env bash
# Run every pending Isabelle property-mutation batch: for each study directory named below,
# the baseline first (must PASS, else the batch is skipped), then all mutants three at a time,
# build timeout 300 s (pre-registration amendment of 9 November 2026). Prints each study's
# summary line; results are in <study>/results_isabelle.csv and <study>/results/.
cd "$(dirname "$0")"
export TO=${TO:-600} J=${J:-2}   # 600 s / two-wide: the Chemical Detector and Steam Boiler baselines build in 244 s and 189 s
export ISA=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/isabelle-docker.sh
for S in property-mutation-chemical_detector property-mutation-steam_boiler; do   # the LRE batch is complete
  [ -d "$S/mutants" ] || continue
  echo "===== $S (timeout ${TO}s, $J at a time)"
  ( cd "$S"
    export PFX="propmut_${S#property-mutation-}"
    mkdir -p results/parts
    build() { id=$1; dir=$2; sess="${PFX}_${id}"
      sed -i '' -E "s/^session +[A-Za-z0-9_]+/session $sess/" "$dir/ROOT"
      t0=$(date +%s); "$ISA" build -D "$dir" -v -o timeout=$TO > "results/$id.isabelle.log" 2>&1; rc=$?; t=$(( $(date +%s) - t0 ))
      "$ISA" build_log -H Error "$sess" > "results/$id.isabelle.errors" 2>/dev/null
      v=FAIL; [ $rc -eq 0 ] && v=PASS
      lines=$(grep -oE '\(line [0-9]+ of' "results/$id.isabelle.errors" | grep -oE '[0-9]+' | sort -n | uniq | tr '\n' ' ')
      kinds=$(grep -oE 'Timeout|Failed to finish proof|Failed to apply (initial )?proof method|Interrupt' "results/$id.isabelle.errors" | sort | uniq -c | tr -s ' \n' ' ')
      echo "$id,$v,$t,\"lines: $lines\",\"$kinds\"" > "results/parts/$id.csv"; echo "  done $id $v ${t}s"; }
    export -f build
    build baseline baseline/isabelle
    if ! grep -q ",PASS," results/parts/baseline.csv; then echo "  baseline did not pass at ${TO}s; batch skipped"; cat results/parts/baseline.csv; exit 0; fi
    for d in mutants/*/; do echo "$(basename $d) $d/isabelle"; done | xargs -P "$J" -L 1 bash -c 'build "$0" "$1"'
    { cat results/parts/baseline.csv; for d in mutants/*/; do cat "results/parts/$(basename $d).csv"; done; } > results_isabelle.csv
    echo "=== $S DONE: $(grep -c ,PASS, results_isabelle.csv) PASS, $(grep -c ,FAIL, results_isabelle.csv) FAIL of $(wc -l < results_isabelle.csv)" )
done
echo "=== ALL DONE"
