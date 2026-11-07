#!/bin/bash
# v3_rescore_a19.sh compileonly <study> <k> <iteration>
# Pre-registration v3, Amendment 19: re-score ONE recorded iteration of a committed
# Compile-only session at the Amendment 19 pin (the two generator fixes), with the same
# FDR4 settings the collection ran with. Run in YOUR terminal (Docker running; FDR4
# licence; no session running). The iteration's RECORDED Java and code-generation trace
# go through all twelve phases once, with the pipeline's own iteration driver (Forge's
# experiments/scripts/replay_pass.py). The result is written beside the original record,
# which is not touched:
#     experiments/v3-compile-only/<study>/run-<k>/rescore-a19/iter-<i>/
# The worktree is removed at the end.
set -u
COND="${1:?compileonly}"; STUDY="${2:?study}"; K="${3:?run number}"; IT="${4:?iteration}"
NEW="${A19_PIN:-1ad9301e00feebca5dedec8078d047a275ba45c4}"
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
FORGE=${FORGE_REPO:-$HOME/Git/Forge}
case "$COND" in compileonly) COLL=v3-compile-only;; *) echo "FAIL condition must be compileonly"; exit 1;; esac
RUN=$C/experiments/$COLL/$STUDY/run-$K
REC=$RUN/iter-$IT
OUT=$RUN/rescore-a19/iter-$IT
WT=/Users/ranwei/Gitee/forge-v3-rescore-a19-$COND-$STUDY-$K-$IT
[ -d "$REC/java" ] || { echo "FAIL no recorded Java at $REC/java (is the session committed?)"; exit 1; }
[ -f "$REC/traces/result_codegen.json" ] || { echo "FAIL no recorded trace at $REC/traces/result_codegen.json"; exit 1; }
[ -f "$FORGE/experiments/scripts/replay_pass.py" ] || { echo "FAIL no replay_pass.py in $FORGE"; exit 1; }
[ ! -e "$OUT/phase_status.json" ] || { echo "FAIL $OUT already holds a re-score"; exit 1; }
[ ! -e "$WT" ] || { echo "FAIL $WT exists; remove it first (git worktree remove --force $WT)"; exit 1; }
PY=""
for cand in ${PYTHON:-} python3 /opt/homebrew/bin/python3 /usr/local/bin/python3 /usr/bin/python3 "$HOME/.claude-science/conda/envs/python/bin/python"; do
  [ -n "$cand" ] || continue
  if "$cand" -c 'import yaml, jsonschema' >/dev/null 2>&1; then PY="$cand"; break; fi
done
[ -n "$PY" ] || { echo "FAIL no python3 with pyyaml and jsonschema found"; exit 1; }
if [ -n "${V3_DRY:-}" ]; then echo "DRY rec=$REC out=$OUT wt=$WT pin=$NEW python=$PY"; exit 0; fi
docker info >/dev/null 2>&1 || { echo "FAIL Docker is not running"; exit 1; }
git -C "$C" worktree add --detach "$WT" "$NEW" >/dev/null 2>&1 || { echo "FAIL could not create the worktree at $NEW"; exit 1; }
[ "$(git -C "$WT" rev-parse HEAD)" = "$(git -C "$C" rev-parse "$NEW")" ] || { echo "FAIL worktree is not at $NEW"; exit 1; }
sed -i '' "s/^  active_case_study: .*/  active_case_study: $STUDY/" "$WT/pipeline.yaml"
cp "$FORGE/experiments/scripts/replay_pass.py" "$WT/experiments/scripts/replay_pass.py"
for d in /Applications/FDR4.app/Contents/MacOS /Applications/FDR4.app/Contents/Resources/bin; do [ -x "$d/refines" ] && { export PATH="$d:$PATH"; break; }; done
export PATH="/opt/homebrew/bin:$PATH"
[ -z "${JAVA_HOME:-}" ] && [ -d /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home ] && export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
[ -n "${JAVA_HOME:-}" ] && export PATH="$JAVA_HOME/bin:$PATH"
mkdir -p "$OUT"
START=$(date -u +"%Y-%m-%d %H:%M:%S")
( cd "$WT" && REPLAY_TRACE="$REC/traces/result_codegen.json" "$PY" experiments/scripts/replay_pass.py "$STUDY" "$REC/java" "$OUT" ) 2>&1 | tee "$OUT/rescore.log" | grep -E "^\[replay\]|PASSED|FAILED"
python3 - "$OUT" "$(git -C "$C" rev-parse "$NEW")" "$REC" "$FORGE/experiments/scripts/replay_pass.py" "$START" <<'PY'
import json, sys, hashlib, datetime
out, pin, rec, rp, start = sys.argv[1:6]
json.dump({"pin": pin, "recorded_source": rec, "replay_pass_sha256": hashlib.sha256(open(rp, "rb").read()).hexdigest(),
           "started_utc": start, "stamped_utc": datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S"),
           "fdr4_budget": "the collection's pipeline.yaml at the pin",
           "amendment": "prereg-v3-study-free.md, Amendment 19"}, open(f"{out}/rescore.json", "w"), indent=1)
PY
[ -f "$OUT/phase_status.json" ] && { echo "== verdicts at ${NEW:0:8}"; cat "$OUT/phase_status.json"; } || echo "FAIL no phase_status.json written"
git -C "$C" worktree remove --force "$WT" && echo "worktree removed"
