#!/bin/bash
# setup_claude_run.sh -- set up a worktree for the AUTONOMOUS CLAUDE CODE arm
# (condition B-claude-auto).
#
# HOW THIS WORKS NOW (rewritten 2026-09-17 after the author caught the bug).
#
# v1 re-derived the setup from scratch and SILENTLY OMITTED four things
# setup_v2_run.sh does:
#   * removing forge.rtl* (a SEPARATE project: 5 modules + rtl-pipeline.yaml,
#     nothing in FORGE references them) -- they were left in the actor's
#     reading surface
#   * rewriting pipeline.yaml's active_case_study, so a worktree set up for
#     sranger still pointed the pipeline at chemical_detector
#   * clearing stale phase verdicts (gitignored post_*.json survive a worktree
#     add and make a COLD run report issues as "RECURRING")
#   * stripping study-named paths out of docs/archive
#
# So v2 DELEGATES to setup_v2_run.sh via FORGE_WT_OVERRIDE and adds only this
# arm's delta on top. One implementation of the strip logic, not two.
#
# THE DELTA over setup_v2_run.sh:
#   1. stricter strip -- also deletes docs/fixes (which holds
#      convergence_war_stories.md, called "the study-specific answer key" by the
#      cold prompt itself), docs/research, docs/design, docs/experiments.
#      ENFORCEMENT upgrade, not a condition change: a compliant actor was never
#      permitted to read these. An agent that greps a repo by reflex makes an
#      honour-system control materially weaker than deletion.
#   2. actor.json in the run directory, so the record can never pool this arm
#      with the Codex arm.
#
#   Usage: setup_claude_run.sh <study> <run_k>
set -u
STUDY="${1:?study: sranger | lre | chemical_detector}"
RUNK="${2:?run number}"
C=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
# Defaulted -- see the note in setup_v2_run.sh. FORGE_SHA is set only by a
# collection that runs a deliberately different instrument (condition D).
SHA=${FORGE_SHA:-b808424c7c1b51157f4f14eac6e76e00d5beed90}
# FROGE_WT_PREFIX selects the worktree family so a second collection cannot
# reuse the first's worktree names. Default unchanged.
WT=/Users/ranwei/Gitee/${FORGE_WT_PREFIX:-forge-claude}-run$RUNK-$STUDY
# Actor-facing prompts for THIS arm live in claude-prompts/, not
# codex-prompts/. The latter is named for the Codex arm and still holds the
# shared operator tooling (snapshot_v2.py, setup_v2_run.sh, the probes);
# only the prompts a Claude Code actor is handed are split out, so the
# instruction this script prints cannot point an actor at the wrong arm.
P=/Users/ranwei/Git/TOSEM_Formal_Guided_Vibe_Coding/review/claude-prompts
# The autonomous arm records into its OWN collection tree, numbered from 1
# independently of the Codex arm. Sharing convergence-v2/ put this arm's first
# run at sranger/run-5, i.e. numbered as though it continued the Codex
# sequence -- which is precisely the pooling Amendment 1 forbids. Separate
# trees make a cross-arm sum impossible to write by accident.
# FROZEN-INSTRUMENT COLLECTION (2026-09-20). FORGE_ARM_DIR selects the
# collection root so a second collection cannot write into the first.
# Default is unchanged, so every existing invocation behaves identically.
ARM=${FORGE_ARM_DIR:-$C/experiments/convergence-v2-claude-auto}
PYBIN_SETUP=/Users/ranwei/.claude-science/conda/envs/python/bin/python
[ -x "$PYBIN_SETUP" ] || PYBIN_SETUP=$(command -v python3)

case "$STUDY" in sranger|lre|chemical_detector) ;;
  steam_boiler) case "$(basename "${FORGE_ARM_DIR:-}")" in v3-reference|v3-cold) true ;; *) false ;; esac || { echo "FAIL: steam_boiler runs only in v3-reference"; exit 1; } ;;
  *) echo "FAIL: unknown study"; exit 1;; esac
# OPUS 5.5 RERUN (review/prereg-opus55-rerun.md): each collection runs at its original's pin.
case "$(basename "${FORGE_ARM_DIR:-}")" in
  convergence-c-opus55)      WANT55=b808424c7c1b51157f4f14eac6e76e00d5beed90; OK55="sranger lre" ;;
  ablation-d2-opus55)        WANT55=35f6d5570fb857c94cda4bdf58a9b5a08a4264da; OK55="sranger lre" ;;
  multicontroller-e-opus55)  WANT55=99952b9b16eab1ec09877efdbb2d5efa69ad9d48; OK55="chemical_detector" ;;
  compile-only-opus55)       OK55="sranger lre chemical_detector"
    if [ "$STUDY" = chemical_detector ]; then WANT55=5f403301f0e9f469d79dcaf9dc4b421adc885625; else WANT55=b808424c7c1b51157f4f14eac6e76e00d5beed90; fi ;;
  *) WANT55="" ;;
esac
if [ -n "$WANT55" ]; then
  case " $OK55 " in *" $STUDY "*) ;; *) echo "FAIL: $(basename "$FORGE_ARM_DIR") does not include $STUDY"; exit 1;; esac
  [ "${FORGE_SHA:-$SHA}" = "$WANT55" ] || { echo "FAIL: $(basename "$FORGE_ARM_DIR") $STUDY must run at $WANT55 (FORGE_SHA=${FORGE_SHA:-unset})"; exit 1; }
fi
# PRE-REGISTRATION v3 (review/prereg-v3-study-free.md): one pin for every study.
case "$(basename "${FORGE_ARM_DIR:-}")" in v3-cold|v3-reference|v3-compile-only)
  WANTV3=56cb43d7835b43e8a986e6ba4c0e82f35083d6f1
  [ "$STUDY" = steam_boiler ] && WANTV3=aaddf2225b38eb1eb749e816f6f5874d7ec93eaa   # Amendments 14-16
  [ "$(basename "$FORGE_ARM_DIR")" = v3-compile-only ] && WANTV3=aaddf2225b38eb1eb749e816f6f5874d7ec93eaa   # Amendment 18
  [ "${FORGE_SHA:-}" = "$WANTV3" ] || { echo "FAIL: $(basename "$FORGE_ARM_DIR") must run at $WANTV3 (FORGE_SHA=${FORGE_SHA:-unset})"; exit 1; }
  # Amendments 11 and 12: cold Steam Boiler passes 1-10 (11, 12, ... only as void replacements;
  # the order, replacements included, is enforced by v3_start.sh).
  true ;;
esac
# COLD BASELINE v2: the pin follows the study (review/prereg-cold-baseline-v2.md).
if [ "$(basename "${FORGE_ARM_DIR:-}")" = "cold-baseline-v2" ]; then
  case "$STUDY" in
    chemical_detector) WANT=5f403301f0e9f469d79dcaf9dc4b421adc885625 ;;
    *)                 WANT=b808424c7c1b51157f4f14eac6e76e00d5beed90 ;;
  esac
  [ "${FORGE_SHA:-}" = "$WANT" ] || { echo "FAIL: cold-baseline-v2 $STUDY must run at $WANT (FORGE_SHA=${FORGE_SHA:-unset})"; exit 1; }
fi
# A run is TAKEN only when it has a recorded ITERATION. A run-dir holding just
# actor.json was merely PREPARED -- this script writes that marker before the
# actor starts -- so refusing on directory existence made a prepared-but-unrun
# number unusable forever. That is exactly what stranded sranger run-4/run-5
# and the chem/lre worktrees when they were removed for a re-cut: the marker
# survived, setup refused, and the run could never be re-prepared. Same rule as
# prepare_claude_arm_runs.sh, so the driver and this script cannot disagree.
if ls -d "$ARM/$STUDY/run-$RUNK"/iter-* >/dev/null 2>&1; then
  echo "FAIL: run-$RUNK already has recorded iterations for $STUDY -- pick a free number"
  exit 1
fi
if [ -d "$ARM/$STUDY/run-$RUNK" ]; then
  echo "note: run-$RUNK was prepared before but never recorded an iteration; re-preparing it"
fi

echo "########## delegating to setup_v2_run.sh (one strip implementation) ##########"
FORGE_WT_OVERRIDE="$WT" bash "$C/scripts/setup_v2_run.sh" "$STUDY" "$RUNK" || {
  echo "FAIL: base setup failed -- nothing further attempted"; exit 1; }

echo ""
echo "########## claude-arm delta ##########"
echo "== A. stricter strip (answer-key docs the Codex arm only forbade)"
( cd "$WT" && rm -rf docs/fixes docs/research docs/design docs/experiments ) || {
  echo "FAIL: extra strip"; exit 1; }
echo "   deleted docs/fixes, docs/research, docs/design, docs/experiments"
echo "   (CLAUDE.md links into docs/fixes 8x; those links are intentionally"
echo "    dangling -- the cold prompt says not to follow them and states this)"

echo "== B. verify the delta"
fail=0
for p in docs/fixes docs/research docs/design docs/experiments .remember; do
  [ -e "$WT/$p" ] && { echo "   FAIL still present: $p"; fail=1; } || echo "   OK   absent: $p"
done
# re-assert the inherited invariants that matter most for THIS arm
r=$(ls -d "$WT"/forge.rtl* 2>/dev/null | wc -l | tr -d ' ')
[ "${r:-0}" = "0" ] && echo "   OK   forge.rtl* absent (inherited)" \
  || { echo "   FAIL ${r} forge.rtl* module(s) present"; fail=1; }
grep -q "^  active_case_study: ${STUDY}" "$WT/pipeline.yaml" \
  && echo "   OK   active_case_study: ${STUDY} (inherited)" \
  || { echo "   FAIL pipeline.yaml not pointed at ${STUDY}"; fail=1; }
n=$(find "$WT/java.generated.project/src/main/java" -name "*.java" 2>/dev/null | wc -l | tr -d ' ')
[ "${n:-0}" = "0" ] && echo "   OK   no Java (cold run)" \
  || { echo "   FAIL ${n} java file(s) present"; fail=1; }

echo "== B2. pre-registered fdr4 budget"
# Prereg s4 fixes the collection's fdr4 budget at timeout 9000 s / memory 10240 MB;
# pre-registration v3, Amendment 10 raises the memory to 40960 MB (40 GiB of 48).
# The pinned pipeline.yaml carries 600 s / 4096 MB, so every run so far has had the
# budget applied by hand in its worktree -- chem run-2 converged only at 9000 s
# (its fdr4 took 801.79 s, well over the 600 s default). Applying it here makes the
# budget a property of the SETUP rather than an operator memory, so a run cannot
# quietly execute at a different budget from the ones it will be compared against.
"$PYBIN_SETUP" - "$WT/pipeline.yaml" "$STUDY" <<'PYBUDGET'
import re, sys
p = sys.argv[1]
# Amendment 17: the Steam Boiler gets 80 GiB and FDR4's disk store (scripts/fdr4_paged.sh).
sb = len(sys.argv) > 2 and sys.argv[2] == "steam_boiler"
MEM = 81920 if sb else 40960
s = open(p).read()
i = s.find("\n  fdr4:")
assert i > 0, "no fdr4 phase block in pipeline.yaml"
# Bound the edit by the NEXT phase key at the same indent, not a guessed window.
# A first version used s[i:i+2000] and silently missed memory_limit_mb, which sits
# at +2418 in this file -- the timeout was rewritten and the memory limit was not.
m = re.search(r"\n  [A-Za-z_][A-Za-z0-9_]*:", s[i+1:])
end = (i + 1 + m.start()) if m else len(s)
blk = s[i:end]
blk, n_t = re.subn(r"(\n\s+)timeout: *\d+", r"\g<1>timeout: 9000", blk, count=1)
blk, n_m = re.subn(r"(\n\s+)memory_limit_mb: *\d+", rf"\g<1>memory_limit_mb: {MEM}", blk, count=1)
assert n_t == 1 and n_m == 1, f"fdr4 budget keys not both found (timeout={n_t}, memory={n_m})"
if sb:
    blk, n_p = re.subn(r"(\n\s+darwin: *)refines\b", r"\g<1>/Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/fdr4_paged.sh", blk, count=1)
    assert n_p == 1, "fdr4_path darwin entry not found"
open(p, "w").write(s[:i] + blk + s[end:])
PYBUDGET
b=$("$PYBIN_SETUP" - "$WT/pipeline.yaml" <<'PYCHECK'
import sys
try:
    import yaml
    f = yaml.safe_load(open(sys.argv[1]))["phases"]["fdr4"]
    fp = f.get('fdr4_path'); fp = fp.get('darwin') if isinstance(fp, dict) else fp
    print(f"{f.get('timeout')} {f.get('memory_limit_mb')} {fp}")
except Exception as e:
    print(f"ERR {e}")
PYCHECK
)
if [ "$STUDY" = steam_boiler ]; then want="9000 81920 /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/fdr4_paged.sh"; else want="9000 40960 refines"; fi
if [ "$b" = "$want" ]; then
  echo "   OK   fdr4 budget $b (prereg s4, Amendments 10 and 17)"
else
  echo "   FAIL fdr4 budget is '$b', expected '$want'"; fail=1
fi

echo "== C. actor marker"
mkdir -p "$ARM/$STUDY/run-$RUNK"

# The CONDITION follows the COLLECTION, exactly as the prompt set does.
# 2026-09-22: this block hardcoded "B-claude-auto" for every collection, so 21
# of 36 actor.json files across four collections claimed a condition they are
# not: the frozen collection is C, the two ablation collections are D and D2.
# The field is documentation (no script reads it), but it is the per-run record
# of which condition produced the run, and a reader pooling by it would pool
# four conditions into one.
case "$(basename "${FORGE_ARM_DIR:-}")" in
  convergence-v3-frozen)
    COND="C-frozen"
    CNOTE="frozen unablated instrument; the reference arm the ablations are compared against." ;;
  ablation-d-diagnostic)
    COND="D-ablated-feedback"
    CNOTE="feedback layer stripped of prescriptive remedies. NEVER pool with C or D2." ;;
  ablation-d2-diagnostic)
    COND="D2-ablated-feedback-and-codegen-rules"
    CNOTE="feedback layer, codegen rule assets and structural linter all stripped of prescriptive remedies. NEVER pool with C or D." ;;
  multicontroller-e)
    COND="E-multicontroller"
    CNOTE="multi-controller generators (one Dafny file / Isabelle theory per controller, 99952b9b); D2 ablation inherited. NEVER pool with B, C, D or D2." ;;
  convergence-c-opus55)
    COND="C-frozen-opus55"
    CNOTE="condition C re-run on Claude Opus 5.5 (review/prereg-opus55-rerun.md). Never pool with convergence-v3-frozen." ;;
  ablation-d2-opus55)
    COND="D2-ablated-feedback-and-codegen-rules-opus55"
    CNOTE="condition D2 re-run on Claude Opus 5.5 (review/prereg-opus55-rerun.md). Never pool with ablation-d2-diagnostic." ;;
  multicontroller-e-opus55)
    COND="E-multicontroller-opus55"
    CNOTE="condition E re-run on Claude Opus 5.5 (review/prereg-opus55-rerun.md). Never pool with multicontroller-e." ;;
  compile-only-opus55)
    COND="compile-only-opus55"
    CNOTE="compile-only feedback (eight visible phases; verifier reports withheld by the runner) at the Reference pins, Claude Opus 5.5 (review/prereg-opus55-rerun.md)." ;;
  v3-cold)
    COND="V3-cold-single-pass"
    CNOTE="one cold pass, no iteration, study-free instructions (review/prereg-v3-study-free.md). Never pool with loop runs or earlier collections." ;;
  v3-compile-only)
    COND="V3-compile-only-loop"
    CNOTE="Compile-only feedback loop (eight visible phases; verifier reports withheld by the runner), up to 7 iterations, study-free instructions (review/prereg-v3-study-free.md, Amendment 18). Never pool with earlier collections." ;;
  v3-reference)
    COND="V3-reference-loop"
    CNOTE="Reference (full) feedback loop, up to 7 iterations, study-free instructions (review/prereg-v3-study-free.md). Never pool with earlier collections." ;;
  cold-baseline-v2)
    COND="CB2-cold-single-pass"
    CNOTE="one cold pass, no iteration: iteration 1 of C (SRanger, LRE) or F (Chemical Detector) at the same pin (see review/prereg-cold-baseline-v2.md). Never pool with loop runs." ;;
  reference-multicontroller-f)
    COND="F-reference-multicontroller"
    CNOTE="Reference (full) feedback with multi-controller generators at the published pipeline (see review/prereg-reference-multicontroller-f.md). Replaces the Reference chemical_detector row only. NEVER pool with B, C, D, D2 or E." ;;
  "")
    COND="B-claude-auto"
    CNOTE="autonomous arm on the submitted instrument." ;;
  *)
    echo "   FAIL unknown collection $(basename "${FORGE_ARM_DIR}") -- add its condition above"
    exit 1 ;;
esac
cat > "$ARM/$STUDY/run-$RUNK/actor.json" <<JSON
{
 "actor": "claude-code",
 "harness": "VS Code + Claude Code, autonomous loop",
 "condition": "$COND",
 "condition_note": "$CNOTE",
 "note": "DIFFERENT CONDITION from the Codex runs, and two factors differ at once (model/harness AND operator-driven vs autonomous). Never pool with Codex runs into one convergence rate; see prereg Amendment 1.",
 "instrument_sha": "$SHA",
 "worktree": "$WT",
 "base_setup": "setup_v2_run.sh via FORGE_WT_OVERRIDE (same strip/config as the Codex arm)",
 "extra_strip": "docs/fixes, docs/research, docs/design, docs/experiments -- enforcement upgrade, not a condition change",
 "setup_utc": "$(date -u '+%Y-%m-%d %H:%M:%S')"
}
JSON
echo "   wrote actor.json (actor=claude-code, condition=$COND)"

# The prompt set follows the COLLECTION, derived from FORGE_ARM_DIR's basename --
# not from "is FORGE_ARM_DIR set at all". That earlier test assumed every override
# meant the frozen collection, so a condition-D2 run was told to paste the FROZEN
# run-2 prompt. That prompt exports FORGE_WT_PREFIX=forge-frozen and
# FORGE_STAGE_DIR=frozen-staging and carries no FORGE_SHA, so the actor would have
# been routed into the frozen collection's ALREADY-PUBLISHED run-2 (15 Java files
# present, not cold) at the wrong instrument. Caught 2026-09-22 before any run.
case "$(basename "${FORGE_ARM_DIR:-}")" in
  convergence-v3-frozen)    PROMPT="$(dirname "$P")/frozen-prompts/frozen-cold-$STUDY-run$RUNK.md" ;;
  ablation-d2-diagnostic)   PROMPT="$(dirname "$P")/ablation-prompts/d2-cold-$STUDY-run$RUNK.md" ;;
  multicontroller-e)        PROMPT="$(dirname "$P")/ablation-prompts/e-cold-$STUDY-run$RUNK.md" ;;
  reference-multicontroller-f)  PROMPT="$(dirname "$P")/reference-f-prompts/f-cold-$STUDY-run$RUNK.md" ;;
  cold-baseline-v2)         PROMPT="$(dirname "$P")/cold-v2-prompts/cold-v2-$STUDY-run$RUNK.md" ;;
  v3-cold)                  PROMPT="$(dirname "$P")/v3-prompts/v3-cold-$STUDY-run$RUNK.md" ;;
  v3-reference)             PROMPT="$(dirname "$P")/v3-prompts/v3-reference-$STUDY-run$RUNK.md" ;;
  v3-compile-only)          PROMPT="$(dirname "$P")/v3-prompts/v3-compileonly-$STUDY-run$RUNK.md" ;;
  convergence-c-opus55)     PROMPT="$(dirname "$P")/opus55-prompts/c55-cold-$STUDY-run$RUNK.md" ;;
  ablation-d2-opus55)       PROMPT="$(dirname "$P")/opus55-prompts/d255-cold-$STUDY-run$RUNK.md" ;;
  multicontroller-e-opus55) PROMPT="$(dirname "$P")/opus55-prompts/e55-cold-$STUDY-run$RUNK.md" ;;
  compile-only-opus55)      PROMPT="$(dirname "$P")/opus55-prompts/co55-cold-$STUDY-run$RUNK.md" ;;
  ablation-d-diagnostic)    PROMPT="$(dirname "$P")/ablation-prompts/ablate-cold-$STUDY-run$RUNK.md" ;;
  "")                       PROMPT="$P/auto-cold-$STUDY-run$RUNK.md" ;;
  *)  echo "   FAIL unknown collection $(basename "$FORGE_ARM_DIR") -- add its prompt set to"
      echo "        setup_claude_run.sh rather than letting it fall back to another condition's"
      fail=1; PROMPT="" ;;
esac
[ -n "$PROMPT" ] && [ -f "$PROMPT" ] || { echo "   FAIL prompt not found: ${PROMPT:-<unset>}"; fail=1; }

echo ""
if [ "$fail" = "0" ]; then
  echo "READY -- AUTONOMOUS ARM."
  echo "  1. code $WT"
  echo "  2. start Claude Code IN THAT FOLDER"
  echo "  3. paste: $PROMPT"
  echo ""
  echo "  The actor then loops on its own:"
  echo "    bash $C/scripts/run_claude_iteration.sh $STUDY $RUNK <iter> <tokens|unavailable>"
  echo "  recording into ${FORGE_STAGE_DIR:-$WT/../claude-arm-staging} until 12/12 or the cap of 7."
  echo ""
  echo "  Do NOT use run_v2_iteration.sh for this arm (wrong path, wrong actor label)."
  # SUCCESS-ONLY STAMP (2026-09-22). actor.json is written BEFORE the last
  # checks, so its presence does NOT mean setup succeeded -- a run whose prompt
  # was missing still had one. verify_ablation_instrument.sh gates on THIS file,
  # which is written only here, inside the all-checks-passed branch.
  cat > "$ARM/$STUDY/run-$RUNK/setup_ok.json" <<OKJSON
{
 "setup": "complete, all checks passed",
 "study": "$STUDY",
 "run": $RUNK,
 "worktree": "$WT",
 "instrument_sha": "$SHA",
 "prompt": "$PROMPT",
 "stamped_utc": "$(date -u '+%Y-%m-%d %H:%M:%S')"
}
OKJSON
else
  echo "SETUP FAILED -- do not run. Remove:"
  echo "  git -C $C worktree remove --force $WT"
  # and make sure no stale success stamp survives a later failed re-setup
  rm -f "$ARM/$STUDY/run-$RUNK/setup_ok.json" 2>/dev/null
fi
