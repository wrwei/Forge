#!/bin/bash
# v3_rescore_a17.sh <cold|reference> steam_boiler <k> <iteration>
# Pre-registration v3, Amendment 17: re-score ONE recorded iteration of a committed Steam
# Boiler session whose FDR4 check was stopped at the 40,960 MB budget, with the new FDR4
# settings (80 GiB; FDR4's disk store through scripts/fdr4_paged.sh) at the same pin
# aaddf222. Run in YOUR terminal (Docker running; FDR4 licence; no session running; close
# other heavy applications). The iteration's RECORDED Java and code-generation trace go
# through all twelve phases once, with the pipeline's own iteration driver (Forge's
# experiments/scripts/replay_pass.py). The result is written beside the original record,
# which is not touched:
#     experiments/v3-<cond>/steam_boiler/run-<k>/rescore-a17/iter-<i>/
# The worktree is removed at the end.
set -u
COND="${1:?cold|reference}"; STUDY="${2:?study}"; K="${3:?run number}"; IT="${4:?iteration}"
[ "$STUDY" = steam_boiler ] || { echo "FAIL Amendment 17 re-scores Steam Boiler sessions only"; exit 1; }
NEW="${A17_PIN:-aaddf2225b38eb1eb749e816f6f5874d7ec93eaa}"
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
FORGE=${FORGE_REPO:-$HOME/Git/Forge}
case "$COND" in cold) COLL=v3-cold;; reference) COLL=v3-reference;; *) echo "FAIL condition must be cold or reference"; exit 1;; esac
RUN=$C/experiments/$COLL/$STUDY/run-$K
REC=$RUN/iter-$IT
OUT=$RUN/rescore-a17/iter-$IT
WT=/Users/ranwei/Gitee/forge-v3-rescore-a17-$COND-$STUDY-$K-$IT
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
python3 - "$WT/pipeline.yaml" <<'PY'
import re, sys
p = sys.argv[1]; s = open(p).read(); i = s.find("\n  fdr4:"); assert i > 0
m = re.search(r"\n  [A-Za-z_][A-Za-z0-9_]*:", s[i+1:]); end = (i + 1 + m.start()) if m else len(s); blk = s[i:end]
blk, a = re.subn(r"(\n\s+)timeout: *\d+", r"\g<1>timeout: 9000", blk, count=1)
blk, b = re.subn(r"(\n\s+)memory_limit_mb: *\d+", r"\g<1>memory_limit_mb: 81920", blk, count=1)
blk, c = re.subn(r"(\n\s+darwin: *)refines\b", r"\g<1>/Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/fdr4_paged.sh", blk, count=1); assert c == 1
assert a == 1 and b == 1; open(p, "w").write(s[:i] + blk + s[end:])
PY
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
           "fdr4_budget": {"timeout_s": 9000, "memory_limit_mb": 81920, "disk_store": "scripts/fdr4_paged.sh"},
           "amendment": "prereg-v3-study-free.md, Amendment 17"}, open(f"{out}/rescore.json", "w"), indent=1)
PY
[ -f "$OUT/phase_status.json" ] && { echo "== verdicts at ${NEW:0:8}"; cat "$OUT/phase_status.json"; } || echo "FAIL no phase_status.json written"
echo "kernel-log window for FDR4 (if it ran long): $START UTC to $(date -u +"%Y-%m-%d %H:%M:%S") UTC"
git -C "$C" worktree remove --force "$WT" && echo "worktree removed"
