#!/usr/bin/env bash
# Run the pre-registered trace assertions for the gas-analysis, movement and Steam Boiler
# controllers (staged trees under ~/Gitee/.fdr-stage-chem and ~/Gitee/.fdr-stage-boiler).
# One line per controller: the generated assertions' results, then the six trace checks.
cd "$(dirname "$0")"; FDR=/Applications/FDR4.app/Contents/MacOS/refines
run() { ( cd "$1/defs" && "$FDR" --format framed_json "$2_tracechecks.csp" ) > "results/$2.json" 2> "results/$2.err"; echo "$2 exit=$? : $(grep -o '"result": *[01]' "results/$2.json" | grep -o '[01]' | tr '\n' ' ')"; }
run ~/Gitee/.fdr-stage-chem GasAnalysisController
run ~/Gitee/.fdr-stage-chem MovementController
run ~/Gitee/.fdr-stage-boiler SteamBoilerController
