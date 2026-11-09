#!/usr/bin/env bash
# Verify the baseline and every Dafny mutant; one line per program: id, verdict, tool summary.
cd "$(dirname "$0")"
export PATH=/opt/homebrew/bin:$PATH   # z3 beside dafny, as on the evaluation machine
DAFNY=${DAFNY:-/opt/homebrew/bin/dafny}
run() { out=$("$DAFNY" verify "$2" 2>&1); s=$(echo "$out" | grep -E "verified, [0-9]+ error" | tail -1); v=FAIL; echo "$s" | grep -q " 0 errors" && v=PASS; echo "$1,$v,\"$s\""; echo "$out" > "results/$1.dafny.log"; }
mkdir -p results
run baseline baseline/SRangerController.dfy
for d in mutants/PD*/; do id=$(basename $d); run $id $d/SRangerController.dfy; done
