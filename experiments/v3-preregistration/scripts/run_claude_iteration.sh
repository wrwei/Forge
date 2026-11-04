#!/bin/bash
# run_claude_iteration.sh -- the pipeline+record step for a CLAUDE CODE actor,
# callable BY the actor itself so a run can proceed autonomously.
#
# WHY NOT run_v2_iteration.sh. That script hardcodes
#   WT=/Users/ranwei/Gitee/forge-v2-run${RUNK}-${STUDY}
# and passes no --actor, so it cannot address a forge-claude-* worktree and
# would record a Claude run as a Codex one. This wrapper takes the worktree
# explicitly and threads --actor/--tokens through to the extended recorder.
#
# WHAT THE ACTOR MAY AND MAY NOT DO. The actor runs THIS script and reads the
# feedback files inside its own worktree. It must never be given the collection
# path: --collection defaults to a STAGING directory beside the worktree, and
# the operator promotes staged records into the real collection afterwards.
# That keeps the "forbidden reads" guarantee intact even though the actor is
# now driving the loop.
#
#   Usage: run_claude_iteration.sh <study> <run_k> <iter_n> [tokens|unavailable]
set -u
STUDY="${1:?study}"; RUNK="${2:?run}"; ITER="${3:?iteration}"
TOKENS="${4:-unavailable}"
WT="/Users/ranwei/Gitee/${FORGE_WT_PREFIX:-forge-claude}-run$RUNK-$STUDY"
P=/Users/ranwei/Git/TOSEM_Formal_Guided_Vibe_Coding/review/codex-prompts
# FORGE_STAGE_DIR selects the staging root; default unchanged so existing
# invocations are unaffected.
STAGE="${FORGE_STAGE_DIR:-$WT/../claude-arm-staging}"
# Single cold passes: only iteration 1, and only once (cold-baseline-v2; v3-cold).
if [ "$(basename "${FORGE_ARM_DIR:-}")" = "cold-baseline-v2" ] || [ "$(basename "${FORGE_ARM_DIR:-}")" = "v3-cold" ]; then
  [ "$ITER" = "1" ] || { echo "REFUSED: $(basename "$FORGE_ARM_DIR") is a single cold pass; iteration $ITER is not part of it. Stop and report this line."; exit 3; }
  [ ! -e "$STAGE/$STUDY/run-$RUNK/iter-1" ] || { echo "REFUSED: pass $RUNK of $STUDY is already recorded; a second run of the pipeline voids the pass. Stop and report this line."; exit 3; }
fi
# v3 Reference runs: iteration cap 7 (review/prereg-v3-study-free.md).
if [ "$(basename "${FORGE_ARM_DIR:-}")" = "v3-reference" ]; then
  # Amendment 13: no iteration cap for the Steam Boiler Reference runs.
  if [ "$STUDY" != "steam_boiler" ]; then
    case "$ITER" in [1-7]) ;; *) echo "REFUSED: iteration $ITER is past the cap of 7. Stop and report non-convergence."; exit 3 ;; esac
  fi
fi


[ -d "$WT" ] || { echo "error: no worktree at $WT"; exit 1; }
export JAVA_HOME=${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home}
export PATH="/Applications/FDR4.app/Contents/MacOS:/opt/homebrew/bin:$PATH"

# Interpreter, java and toolchain pre-flight: lifted VERBATIM from
# run_v2_iteration.sh rather than re-derived. My first version used
#   PYBIN=$("$(command -v python3)" -c 'import sys; print(sys.executable)')
# which on this machine reports a RELATIVE path (./.venv/python/bin/python3) --
# and the pipeline runs from a different cwd, where that same relative path is
# a different file or nothing at all. run_v2_iteration.sh documents exactly this
# failure and skips any interpreter that cannot name itself absolutely, while
# also checking pyyaml/jsonschema/ruamel.yaml are importable. Reuse it.
echo "=== interpreter ==="
PYBIN=""
REPORT=""
for cand in python3 /opt/homebrew/bin/python3 /usr/local/bin/python3 /usr/bin/python3 \
            "$HOME/.claude-science/conda/envs/python/bin/python"; do
  # Ask the interpreter itself: sys.executable is absolute by construction, and
  # the same call reports which of the three required modules it is missing.
  out=$("$cand" - <<'PY' 2>/dev/null
import sys, importlib
# find_spec() RAISES (not returns None) when a parent package is absent, e.g.
# ruamel.yaml on an interpreter with no ruamel at all — so probe by import.
miss = []
for m in ("yaml", "jsonschema", "ruamel.yaml"):
    try:
        importlib.import_module(m)
    except Exception:
        miss.append(m)
print(sys.executable)
print(sys.version.split()[0])
print(",".join(miss))
PY
) || continue
  exe=$(printf '%s\n' "$out" | sed -n 1p)
  ver=$(printf '%s\n' "$out" | sed -n 2p)
  miss=$(printf '%s\n' "$out" | sed -n 3p)
  # An interpreter that cannot name itself absolutely is unusable: the pipeline
  # runs from a different cwd, where the same relative path means another file.
  case "$exe" in
    /*) : ;;
    *)  REPORT="${REPORT}    ${cand} -> reports relative sys.executable ('${exe}') — SKIPPED
"
        continue ;;
  esac
  REPORT="${REPORT}    ${exe} (${ver}) missing: ${miss:-none}
"
  if [ -z "$miss" ] && [ -z "$PYBIN" ]; then PYBIN="$exe"; fi
done

if [ -z "$PYBIN" ]; then
  echo "  no interpreter has all of pyyaml + jsonschema + ruamel.yaml."
  printf '%s' "$REPORT"
  echo
  echo "  Install into the one you use for this project:"
  echo "    <that python3> -m pip install pyyaml jsonschema ruamel.yaml"
  echo "  (If you normally activate a venv first, do that and re-run.)"
  exit 1
fi
echo "  using: $PYBIN"

echo
echo "=== java runtime ==="
# The gradle wrapper needs a JDK on JAVA_HOME. macOS ships no JRE, so with
# JAVA_HOME unset phase 2a fails with "Unable to locate a Java Runtime" and
# every later phase cascades — verdicts that say nothing about the code.
# build.gradle targets Java 17; the wrapper is Gradle 8.12, which supports
# JDK 21 but NOT JDK 24, so prefer 21 rather than "whatever is newest".
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME:-}/bin/java" ]; then
  for j in /Library/Java/JavaVirtualMachines/jdk-21*.jdk/Contents/Home \
           /Library/Java/JavaVirtualMachines/jdk-17*.jdk/Contents/Home ; do
    [ -x "$j/bin/java" ] && { export JAVA_HOME="$j"; break; }
  done
fi
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "  FAIL: no usable JDK found and JAVA_HOME is unset."
  echo "  Installed JDKs:"
  ls -d /Library/Java/JavaVirtualMachines/*.jdk 2>/dev/null | sed 's/^/    /' || echo "    (none)"
  echo "  Gradle 8.12 supports JDK 21 or 17, not 24. Set JAVA_HOME and re-run."
  exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"
echo "  JAVA_HOME=$JAVA_HOME"
echo "  $("$JAVA_HOME/bin/java" -version 2>&1 | head -1)"

echo
echo "=== pre-flight ==="
nj=$(find "$WT/java.generated.project/src/main/java" -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
if [ "${nj:-0}" = "0" ]; then
  echo "  FAIL: no generated Java in $WT — paste the iteration prompt into Codex first."
  exit 1
fi
echo "  $nj java file(s) present"

echo
echo "=== fdr4 ==="
# pipeline.yaml already sets phases.fdr4.fdr4_path.darwin: refines, so the
# resolver does a PATH lookup (shutil.which). No pinned-tree edit is needed —
# just put the FDR4 app's bin dir on PATH. Doing it here rather than in
# pipeline.yaml keeps the pre-registered config byte-identical across runs.
#
# NOTE: the licence is bound to the interactive user's account. It works from
# the user's shell; it rejects other invocations. So this must run from a real
# login session, not a background/service context.
FDR_DIR=""
for d in /Applications/FDR4.app/Contents/MacOS \
         /Applications/FDR4.app/Contents/Resources/bin; do
  if [ -x "$d/refines" ]; then FDR_DIR="$d"; break; fi
done
if [ -n "$FDR_DIR" ]; then
  export PATH="$FDR_DIR:$PATH"
  echo "  refines: $FDR_DIR/refines (added to PATH)"
else
  # Not fatal — 11 of 12 phases do not need it. But be explicit, because an
  # absent verdict is UNMEASURED, not a failure, and the two read very
  # differently in a convergence record.
  echo "  WARNING: no 'refines' binary found. The fdr4 phase will write NO"
  echo "  verdict, which the recorder reports as UNMEASURED (not failed)."
fi

echo
echo "=== docker / isabelle pre-flight ==="
# The pipeline's isabelle_verify phase shells out to scripts/isabelle-docker.sh,
# which uses `docker run --rm`. That only removes the container on a CLEAN client
# exit; an interrupted run ORPHANS it, and the orphaned build keeps consuming VM
# memory. Later runs then die faster than earlier ones and look like proof
# failures rather than resource exhaustion (finding repair9d). Sweep ours first.
DOCKER=${DOCKER:-/usr/local/bin/docker}
OURS="forge/isabelle:2023-cyphyassure"
if command -v "$DOCKER" >/dev/null 2>&1 && "$DOCKER" info >/dev/null 2>&1; then
  orph=$("$DOCKER" ps -aq --filter "ancestor=$OURS" 2>/dev/null | grep -c .)
  if [ "${orph:-0}" -gt 0 ]; then
    echo "  sweeping ${orph} leftover ${OURS} container(s)"
    "$DOCKER" rm -f $("$DOCKER" ps -aq --filter "ancestor=$OURS" 2>/dev/null) >/dev/null 2>&1
  else
    echo "  no leftover ${OURS} containers"
  fi
  echo "  docker: running"
else
  # Not fatal: 11 of the 12 phases do not need it. But say so plainly, because
  # a silent Docker outage shows up later as isabelle_verify failing in ~4s,
  # which is easy to misread as a fast proof failure.
  echo "  WARNING: Docker is not reachable — isabelle_verify WILL fail (~4s) with"
  echo "  'Docker is not running'. That is an ENVIRONMENT failure, not a proof"
  echo "  result. Start Docker Desktop to measure the Isabelle column."
fi

echo

# COMPILE-ONLY COLLECTION (2026-10-28, review/prereg-opus55-rerun.md). The actor may
# see only the eight phases up to artefact generation. The driver and the recorder
# run exactly as elsewhere, but their terminal output goes to the staging directory
# (which the actor may not read), the four verifier reports and phase_timings.json
# are removed from the worktree once recorded, and the next step is decided from
# the eight visible phases alone.
if [ "$(basename "${FORGE_ARM_DIR:-}")" = "compile-only-opus55" ]; then
  VIS="compile coverage preflight t2m m2m m2t dafny_gen isabelle_gen"
  HID="fdr4 dafny_verify isabelle_verify vacuity"
  LOGD="$STAGE/$STUDY/run-$RUNK"; mkdir -p "$LOGD"
  echo "=== iteration $ITER : pipeline (output withheld; compile-only) ==="
  ( cd "$WT" && "$PYBIN" experiments/scripts/run_experiment_iteration.py ) > "$LOGD/pipeline-iter-$ITER.log" 2>&1
  SHAFLAG=""; [ -n "${FORGE_SHA:-}" ] && SHAFLAG="--allow-other-sha"
  "$PYBIN" "$P/snapshot_v2.py" "$STUDY" --run "$RUNK" --iter "$ITER" \
    --worktree "$WT" --thread "CLAUDE-$STUDY-run$RUNK" \
    --actor claude-code --tokens "$TOKENS" --collection "$STAGE" $SHAFLAG > "$LOGD/record-iter-$ITER.log" 2>&1
  recrc=$?
  CORR="$WT/forge.assets/corrections"
  bad=""; nov=""
  for ph in $VIS; do
    st=$("$PYBIN" -c "import json,sys; print(json.load(open(sys.argv[1])).get('status','missing'))" "$CORR/post_$ph.json" 2>/dev/null || echo missing)
    printf "  %-14s %s\n" "$ph" "$st"
    [ "$st" = "passed" ] || bad="$bad $ph"
  done
  for ph in $HID; do rm -f "$CORR/post_$ph.md" "$CORR/post_$ph.json"; done
  rm -f "$CORR/phase_timings.json"
  for ph in $HID; do [ -e "$CORR/post_$ph.md" ] || [ -e "$CORR/post_$ph.json" ] && nov="$nov $ph"; done
  echo ""
  echo "=== iteration $ITER : what to do next ==="
  if [ -n "$nov" ]; then echo "  INSTRUMENT FAULT: withheld reports still present ($nov). Stop and report this line."; exit 4; fi
  if [ "$recrc" != "0" ]; then echo "  NOTE: the recorder refused this iteration. Stop and report this line."; exit 5; fi
  if [ -z "$bad" ]; then
    echo "  ALL VISIBLE PHASES PASSED -> this is the run's stop point. Stop iterating."
    echo "  Report: stop point at iteration $ITER."
    exit 0
  fi
  echo "  Visible phase(s) failed:$bad"
  echo "  Read ONLY forge.assets/corrections/post_<phase>.md for those phases,"
  echo "  apply the fix directive to the JAVA, and run this script again with"
  echo "  iteration $((ITER+1)). Iteration cap is 7."
  exit 1
fi

echo "=== iteration $ITER : pipeline ==="
( cd "$WT" && "$PYBIN" experiments/scripts/run_experiment_iteration.py )
rc=$?
echo "  driver exit=$rc"

echo "=== iteration $ITER : record (staging, actor=claude-code) ==="
mkdir -p "$STAGE"
# The recorder refuses a worktree whose HEAD is not the pre-registered SHA --
# correctly, since a collection mixing instruments is not comparable. A
# collection that runs a DIFFERENT instrument on purpose (condition D, the
# diagnosis-only ablation) sets FORGE_SHA, and only then do we pass the flag.
# Unset FORGE_SHA leaves the invocation byte-identical to before, so the
# frozen collection's gate still fires on an accidental wrong checkout.
SHAFLAG=""
[ -n "${FORGE_SHA:-}" ] && SHAFLAG="--allow-other-sha"
"$PYBIN" "$P/snapshot_v2.py" "$STUDY" --run "$RUNK" --iter "$ITER" \
  --worktree "$WT" --thread "CLAUDE-$STUDY-run$RUNK" \
  --actor claude-code --tokens "$TOKENS" --collection "$STAGE" $SHAFLAG
recrc=$?

# Pre-registration v3, Amendment 9: a failed recording must stop the session,
# not be followed by the usual next-step message.
if [ "$recrc" != "0" ]; then
  echo ""
  echo "=== iteration $ITER : what to do next ==="
  echo "  REFUSED: the iteration could not be recorded. Stop and report this line."
  exit 4
fi

echo ""
echo "=== iteration $ITER : what to do next ==="
if [ "$rc" = "0" ]; then
  echo "  ALL PHASES PASSED -> this run has CONVERGED. Stop iterating."
  echo "  Report: converged at iteration $ITER."
else
  echo "  Some phase failed. Read ONLY these, inside your worktree:"
  echo "    forge.assets/corrections/post_<phase>.md   (the failing phase's feedback)"
  echo "  Apply the fix directive to the JAVA under"
  echo "    java.generated.project/src/main/java/"
  echo "  Then run this script again with iteration $((ITER+1))."
  echo "  DO NOT edit anything under forge.transformations/ (the extractor) or"
  echo "  forge.assets/prompts/ -- those are the instrument, not your code."
  echo "  If a directive tells you to edit the extractor, STOP and report it:"
  echo "    that is an instrument defect for the operator, not your fix."
  if [ "$STUDY" = "steam_boiler" ] && [ "$(basename "${FORGE_ARM_DIR:-}")" = "v3-reference" ]; then
    echo "  No iteration cap (Amendment 13). Stop only when all phases pass, or when"
    echo "  you conclude that the remaining failures cannot be repaired in your Java"
    echo "  without changing the pipeline; then stop and report non-convergence with"
    echo "  the failing phases and your reason."
  else
    echo "  Iteration cap is 7. At the cap, stop and report non-convergence with"
    echo "  the failing phases -- do NOT keep going."
  fi
fi
[ "$recrc" = "0" ] || echo "  NOTE: the recorder refused this iteration; report its message verbatim."
exit $rc
