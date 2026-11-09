#!/usr/bin/env bash
# Run every pipeline phase on each subject of the representativeness checks (paper Section 5.4):
# the unmodified LRE reference (A0) and its two API/I-O variants (A1, A2) and the five synthetic controllers. Works in a
# throw-away export of the research repository at HEAD (~/Gitee/forge-reptargets), so nothing
# here touches the repository's working tree. Budgets are the committed pipeline.yaml's.
# Results: experiments/representative-targets/results/<subject>/{feedback,artefacts,run.log}.
#   usage: run_all.sh [subject ...]      (default: A0 A1 A2 m04 m08 m12 m16 m20)
set -u
REPO=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
E=$REPO/experiments/representative-targets
WT=/Users/ranwei/Gitee/forge-reptargets
# The driver needs yaml, jsonschema and ruamel.yaml: take the first interpreter that has them
# (the same probe as scripts/run_claude_iteration.sh).
PY=${PY:-}
if [ -z "$PY" ]; then
  for cand in python3 /opt/homebrew/bin/python3 /usr/local/bin/python3 /usr/bin/python3 "$HOME/.claude-science/conda/envs/python/bin/python"; do
    if "$cand" -c "import yaml, jsonschema, ruamel.yaml" >/dev/null 2>&1; then PY=$(command -v "$cand"); break; fi
  done
fi
[ -n "$PY" ] || { echo "no python with yaml, jsonschema and ruamel.yaml found"; exit 1; }
echo "python: $PY"
export JAVA_HOME=${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home}
export PATH="$JAVA_HOME/bin:/Applications/FDR4.app/Contents/MacOS:/usr/local/bin:/opt/homebrew/bin:$PATH"
SUBJECTS=${*:-A0 A1 A2 m04 m08 m12 m16 m20}

echo "=== export of the research repository at $(git -C $REPO rev-parse --short HEAD)"
rm -rf "$WT"; mkdir -p "$WT"; git -C "$REPO" archive HEAD | tar -x -C "$WT"
cp -R "$REPO/forge.transformations/build" "$WT/forge.transformations/" 2>/dev/null || true   # built generator classes, if present
cp -R "$REPO/forge.transformations/lib" "$WT/forge.transformations/" 2>/dev/null || true
cd "$WT"
$PY - <<'PY'
import re; p="pipeline.yaml"; s=open(p).read(); print("budgets:", re.findall(r"memory_limit_mb:\s*(\d+)", s), re.findall(r"^\s*timeout:\s*(\d+)", s, re.M)[:3])
PY

for S in $SUBJECTS; do
  echo; echo "===================== $S ====================="
  OUT=$E/results/$S; rm -rf "$OUT"; mkdir -p "$OUT/feedback" "$OUT/artefacts"
  rm -rf java.generated.project/src/main/java; mkdir -p java.generated.project/src/main/java
  rm -rf forge.assets/corrections; mkdir -p forge.assets/corrections
  rm -rf forge.transformations/output
  if [ "$S" = A0 ] || [ "$S" = A1 ] || [ "$S" = A2 ]; then
    cp -R "$E/api-io/$S/java/." java.generated.project/src/main/java/
    cp "$E/api-io/$S/traces/result_codegen.json" java.generated.project/result_codegen.json
    STUDY=lre
    grep -q "gson" java.generated.project/build.gradle || sed -i '' "s|^dependencies {|dependencies {\n    implementation 'com.google.code.gson:gson:2.10.1'|" java.generated.project/build.gradle
  else
    N=${S#m}; D=$E/synthetic/synthetic_m$N
    cp -R "$D/java/." java.generated.project/src/main/java/
    cp "$D/traces/result_codegen.json" java.generated.project/result_codegen.json
    STUDY=synthetic_m$N
    mkdir -p forge.assets/case-studies/$STUDY/requirements; cp "$D"/requirements/* forge.assets/case-studies/$STUDY/requirements/
    sed -i '' "/gson/d" java.generated.project/build.gradle
  fi
  $PY - "$STUDY" <<'PY'
import re,sys; p="pipeline.yaml"; s=open(p).read()
s2,n=re.subn(r"(active_case_study:\s*)\S+", r"\g<1>"+sys.argv[1], s); assert n==1; open(p,"w").write(s2)
PY
  t0=$(date +%s)
  $PY experiments/scripts/run_experiment_iteration.py > "$OUT/run.log" 2>&1
  echo "exit=$? wall=$(( $(date +%s) - t0 ))s"
  cp forge.assets/corrections/post_*.md forge.assets/corrections/post_*.json forge.assets/corrections/phase_timings.json "$OUT/feedback/" 2>/dev/null
  cp -R forge.transformations/output/. "$OUT/artefacts/" 2>/dev/null
  $PY - "$OUT/feedback/phase_timings.json" <<'PY'
import json,sys
try:
    d=json.load(open(sys.argv[1]))["phases"]
    print("  " + "; ".join(f"{k}={v.get('status')}({round(v.get('wall_clock_s',0))}s)" for k,v in d.items()))
except Exception as e: print("  (no phase_timings.json:", e, ")")
PY
done
echo; echo "=== ALL DONE"
