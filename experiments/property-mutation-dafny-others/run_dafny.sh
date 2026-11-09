#!/usr/bin/env bash
# Verify the three baselines and every Dafny mutant; one line per program: id, verdict, tool summary.
cd "$(dirname "$0")"
export PATH=/opt/homebrew/bin:$PATH
DAFNY=${DAFNY:-/opt/homebrew/bin/dafny}
run() { out=$("$DAFNY" verify "$2" 2>&1); s=$(echo "$out" | grep -E "verified, [0-9]+ error" | tail -1); v=FAIL; echo "$s" | grep -q " 0 errors" && v=PASS; echo "$out" > "results/$1.dafny.log"; echo "$1,$v,\"$s\""; }
mkdir -p results
for p in GasAnalysisController MovementController SteamBoilerController; do run baseline-$p baseline/$p.dfy; done
for d in mutants/PD*/; do id=$(basename $d); run $id $d/*.dfy; done
