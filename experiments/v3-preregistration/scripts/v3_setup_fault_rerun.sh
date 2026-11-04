#!/bin/bash
# v3_setup_fault_rerun.sh <cold|reference> <study> <k> -- re-run iteration 1 of ONE v3
# session whose runner stopped before any phase because of a setup fault
# (review/prereg-v3-study-free.md, Amendment 4). Run by the operator in the terminal;
# the session itself is not resumed and gets no message.
#   1. the worktree's Java and trace must be byte-identical to the staged record,
#      and the staged record must have no phase verdict at all;
#   2. the faulted record is moved aside to iter-1-setup-fault/ (kept);
#   3. the manifest schema is restored in the worktree from the pin;
#   4. the runner command the prompt gives is run once.
set -u
COND="${1:?cold|reference}"; STUDY="${2:?study}"; K="${3:?number}"
PIN=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
case "$COND" in
  cold)      COLL=v3-cold;      PFX=forge-v3c9; STG=/Users/ranwei/Gitee/v3c9-staging ;;
  reference) COLL=v3-reference; PFX=forge-v3r10; STG=/Users/ranwei/Gitee/v3r10-staging ;;
  *) echo "FAIL condition must be cold or reference"; exit 1 ;;
esac
WT=${V3_WT:-/Users/ranwei/Gitee/$PFX-run$K-$STUDY}; S=${V3_STG:-$STG}/$STUDY/run-$K   # V3_* for the stub test only
SCHEMA=docs/superpowers/specs/pipeline-manifest.schema.json
[ -d "$S/iter-1" ] || { echo "FAIL no staged iter-1 at $S"; exit 1; }
[ ! -e "$S/iter-1-setup-fault" ] || { echo "FAIL $S/iter-1-setup-fault already exists (re-run already prepared)"; exit 1; }
[ -z "${V3_STUB:-}" ] && { [ "$(git -C "$WT" rev-parse HEAD 2>/dev/null)" = "$PIN" ] || { echo "FAIL $WT is not at $PIN"; exit 1; }; }
python3 - "$S/iter-1/summary.json" <<'PY' || exit 1
import json, sys
d = json.load(open(sys.argv[1])); n = d.get("phases_without_verdict") or []
if len(n) != 12: print(f"FAIL the staged record has verdicts ({12 - len(n)} phase(s)); this is not a setup fault"); sys.exit(1)
print("OK   staged record: 12 phases without a verdict")
PY
PKG=$(ls "$WT/java.generated.project/src/main/java")
diff -rq "$S/iter-1/java" "$WT/java.generated.project/src/main/java/$PKG" >/dev/null || { echo "FAIL the worktree's Java differs from the staged record"; exit 1; }
echo "OK   Java byte-identical to the staged record"
if [ -f "$S/iter-1/traces/result_codegen.json" ]; then
  cmp -s "$S/iter-1/traces/result_codegen.json" "$WT/java.generated.project/result_codegen.json" || { echo "FAIL the worktree's trace differs from the staged record"; exit 1; }
  echo "OK   trace byte-identical to the staged record"
fi
[ -f "$WT/$SCHEMA" ] && { echo "FAIL the schema is already present; nothing to repair"; exit 1; }
mv "$S/iter-1" "$S/iter-1-setup-fault" && echo "OK   faulted record moved to iter-1-setup-fault/"
if [ -n "${V3_STUB:-}" ]; then mkdir -p "$(dirname "$WT/$SCHEMA")"; echo '{}' > "$WT/$SCHEMA"
else git -C "$WT" checkout "$PIN" -- "$SCHEMA" || { echo "FAIL could not restore the schema"; exit 1; }; fi
echo "OK   restored $SCHEMA from the pin"
[ -n "${V3_STUB:-}" ] && { echo "STUB stop before the runner"; exit 0; }
export FORGE_ARM_DIR=$C/experiments/$COLL FORGE_WT_PREFIX=$PFX FORGE_STAGE_DIR=$STG FORGE_SHA=$PIN
cd "$WT" && bash "$C/scripts/run_claude_iteration.sh" "$STUDY" "$K" 1 unavailable 2>&1 | tee "$C/experiments/$COLL/$STUDY/run-$K/setup_fault_rerun.log" | sed -n '/=== iteration 1 : what to do next ===/,$p' | head -6
