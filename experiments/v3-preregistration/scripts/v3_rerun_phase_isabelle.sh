#!/bin/bash
# v3_rerun_phase_isabelle.sh <cold|reference> <study> <k> [iteration] -- re-run ONLY the Isabelle
# phase of one recorded iteration after an environment fault (pre-registration v3, Amendment 1).
# Uses the recorded theory and ROOT, the pipeline's own command and limits, and the pinned
# worktree's Docker wrapper. Writes feedback/isabelle_rerun_<n>.log and .json beside the original
# report, which it never changes. Run on a quiet machine.
set -u
COND="${1:?cold|reference}"; STUDY="${2:?study}"; K="${3:?number}"; IT="${4:-1}"
case "$COND" in
  cold)      PFX=forge-v3c9; STG=/Users/ranwei/Gitee/v3c9-staging ;;
  reference) PFX=forge-v3r10; STG=/Users/ranwei/Gitee/v3r10-staging ;;
  *) echo "FAIL condition must be cold or reference"; exit 1 ;;
esac
WT=${V3_WT:-/Users/ranwei/Gitee/$PFX-run$K-$STUDY}; STG=${V3_STG:-$STG}; I=$STG/$STUDY/run-$K/iter-$IT   # V3_* overrides are for the stub test only
# The worktree may be at either v3 pin (Amendment 2): sessions up to row 12 ran at
# 5280e26e, rows 13-26 at 2db4c031, later ones at d526ea5e (Amendment 6). The re-run uses the worktree's own pin.
PIN=$(git -C "$WT" rev-parse HEAD 2>/dev/null)
[ -d "$I/formal-artefacts/isabelle" ] || { echo "FAIL no recorded theory at $I/formal-artefacts/isabelle"; exit 1; }
[ -n "${V3_STUB:-}" ] || case "$PIN" in 5280e26e828cec41338485a44f71433436c7b55e|2db4c03116e00b82c1c4186c171a50ad26574e33|d526ea5e3c0563f4019238d6ff1ee32cdc10f5f8|56cb43d7835b43e8a986e6ba4c0e82f35083d6f1) ;; *) echo "FAIL $WT is not at a v3 pin"; exit 1;; esac
W="$WT/scripts/isabelle-docker.sh"; [ -x "$W" ] || { echo "FAIL no Docker wrapper at $W"; exit 1; }
[ -n "${V3_STUB:-}" ] || docker info >/dev/null 2>&1 || { echo "FAIL Docker is not running; start Docker Desktop and retry"; exit 1; }
busy=$(pgrep -fl "isabelle build|refines|fdr4|dafny verify|run_experiment_iteration" | grep -v pgrep | wc -l | tr -d ' ')
[ "$busy" = "0" ] || { echo "FAIL another verifier or pipeline is running ($busy process(es)); wait until the machine is quiet"; exit 1; }
pgrep -x claude >/dev/null && echo "WARN a Claude Code process is running; make sure no pass or run is in progress"
n=1; while [ -e "$I/feedback/isabelle_rerun_$n.log" ]; do n=$((n+1)); done
T=$(mktemp -d /tmp/isarerun.XXXXXX); cp "$I"/formal-artefacts/isabelle/* "$T/"
echo "re-run $n of $COND $STUDY $K iteration $IT; session copied to $T; load: $(uptime | sed 's/.*load averages*: //')"
start=$(date -u '+%Y-%m-%dT%H:%M:%SZ')
( cd "$WT" && perl -e 'alarm 1800; exec @ARGV' "$W" build -D "$T" -v -o timeout=600 ) > "$I/feedback/isabelle_rerun_$n.log" 2>&1; rc=$?
end=$(date -u '+%Y-%m-%dT%H:%M:%SZ'); L="$I/feedback/isabelle_rerun_$n.log"
if grep -qE "error waiting for container|Cannot connect to the Docker daemon|unexpected EOF" "$L" || [ $rc = 125 ] || [ $rc = 137 ] || [ $rc = 142 ]; then out=environment_fault
elif [ $rc = 0 ] && grep -qE "^Finished [A-Za-z0-9_]+_Check" "$L"; then out=passed
elif grep -qE "^\*\*\*|Failed to finish proof|Failed to apply|Type unification failed|Undefined" "$L"; then out=failed
else out=unclassified; fi
cat > "$I/feedback/isabelle_rerun_$n.json" <<JSON
{"rerun": $n, "condition": "$COND", "study": "$STUDY", "run": $K, "iteration": $IT, "pin": "$PIN",
 "command": "scripts/isabelle-docker.sh build -D <copy of formal-artefacts/isabelle> -v -o timeout=600 (1800 s wall clock)",
 "start_utc": "$start", "end_utc": "$end", "exit_code": $rc, "outcome": "$out",
 "lemmas_line": "$(grep -m1 -oE 'Finished [A-Za-z0-9_]+_Check[^\"]*' "$L" | head -c 120)",
 "protocol": "review/prereg-v3-study-free.md, Amendment 1"}
JSON
rm -rf "$T"
echo "outcome: $out (exit $rc); log: $L"; tail -3 "$L"
