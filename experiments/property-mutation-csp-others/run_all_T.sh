#!/usr/bin/env bash
# Final run: the pre-registered trace assertions of all five controllers in the traces model
# (:[has trace [T]]:). FDR4's default model for has-trace is failures, under which a trace
# that one branch of a nondeterministic choice can refuse counts as absent; the claims are
# trace claims and are checked as such. One line per controller: the generated assertions,
# then the trace checks.
cd "$(dirname "$0")"; FDR=/Applications/FDR4.app/Contents/MacOS/refines; R=$HOME/Gitee/formal_method_guided_vibe_coding/experiments
run() { ( cd "$1/defs" && "$FDR" --format framed_json "$2_tracechecks.csp" ) > "$3" 2> "${3%.json}.err"; echo "$2 exit=$? : $(grep -o '"result": *[01]' "$3" | grep -o '[01]' | tr '\n' ' ')"; }
run ~/Gitee/.fdr-stage SRangerController $R/property-mutation-sranger/results/fdr_tracechecks_T.json
run ~/Gitee/.fdr-stage-lre LreController $R/property-mutation-lre/results/fdr_tracechecks_T.json
run ~/Gitee/.fdr-stage-chem GasAnalysisController results/GasAnalysisController_T.json
run ~/Gitee/.fdr-stage-chem MovementController results/MovementController_T.json
run ~/Gitee/.fdr-stage-boiler SteamBoilerController results/SteamBoilerController_T.json
