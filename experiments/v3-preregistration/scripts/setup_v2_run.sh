#!/bin/bash
# v2 convergence collection — set up a fresh worktree for ONE run.
#
#   Usage:  bash setup_v2_run.sh <study> <run_k>
#   e.g.    bash setup_v2_run.sh sranger 2
#
# Creates /Users/ranwei/Gitee/forge-v2-run<k>-<study>, strips the answer paths,
# points pipeline.yaml at the study, and verifies every gate before declaring
# READY. Read-only on the source repo apart from `git worktree add`.
#
# NOT `set -e`/`pipefail`: a find/grep that legitimately matches nothing exits
# non-zero, and a verification step must REPORT that rather than abort the
# script mid-check (the first version of this did exactly that).
set -u

STUDY="${1:-}"
RUNK="${2:-}"
if [ -z "$STUDY" ] || [ -z "$RUNK" ]; then
  echo "usage: bash $0 <study> <run_k>    e.g. bash $0 sranger 2"
  echo "  study must be one of: sranger lre chemical_detector"
  exit 2
fi
case "$STUDY" in
  sranger|lre|chemical_detector) ;;
  steam_boiler) case "$(basename "${FORGE_ARM_DIR:-}")" in v3-reference|v3-cold) ;; *) echo "steam_boiler runs only in v3-reference and v3-cold (Amendment 11)"; exit 2 ;; esac ;;
  *) echo "unknown study '$STUDY' (expected sranger|lre|chemical_detector)"; exit 2 ;;
esac

SRC=/Users/ranwei/Gitee/formal_method_guided_vibe_coding
# The frozen-instrument pin. DEFAULTED so every existing invocation is
# unchanged: the frozen collection's remaining runs keep checking out
# b808424c. FORGE_SHA overrides it for a collection that deliberately runs a
# DIFFERENT instrument -- currently only condition D (the diagnosis-only
# ablation) at ec63bc5e. Never repoint the default to serve one collection:
# the frozen collection's 12 promoted runs are recorded against b808424c and
# a replay must reproduce them.
SHA=${FORGE_SHA:-b808424c7c1b51157f4f14eac6e76e00d5beed90}
# Worktree path. FORGE_WT_OVERRIDE lets a sibling arm (e.g. the autonomous
# Claude Code arm) reuse ALL of this script's strip/config logic at a different
# worktree name instead of re-deriving it -- re-derivation is exactly how the
# claude arm's first setup ended up missing the forge.rtl* removal, the
# active_case_study rewrite and the stale-verdict clear. Unset => unchanged.
WT="${FORGE_WT_OVERRIDE:-/Users/ranwei/Gitee/forge-v2-run${RUNK}-${STUDY}}"

export GIT_CONFIG_GLOBAL=/dev/null GIT_CONFIG_SYSTEM=/dev/null

echo "== run ${RUNK} of ${STUDY}  ->  $WT"

# If the worktree already exists, do NOT just bail: a previous invocation may
# have created it and then stopped part-way (e.g. this guard firing on a second
# run). Distinguish "already set up correctly" from "someone else's directory"
# by re-running the verification instead of guessing.
RESUME=0
if [ -e "$WT" ]; then
  if [ -e "$WT/.git" ] && [ "$(git -C "$WT" rev-parse HEAD 2>/dev/null)" = "$SHA" ]; then
    nj=$(find "$WT/java.generated.project/src" -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
    if [ "${nj:-0}" = "0" ]; then
      echo "   $WT already exists at the v2 SHA with no generated Java."
      echo "   Treating this as a partially-completed setup: re-applying the"
      echo "   remaining steps and re-verifying rather than starting over."
      RESUME=1
    else
      echo "   $WT exists and ALREADY CONTAINS ${nj} generated Java file(s)."
      echo "   That looks like a run in progress — refusing to touch it."
      echo "   Use a different run number, or remove it deliberately first."
      exit 1
    fi
  else
    echo "   $WT exists but is not a worktree at the v2 SHA."
    echo "   Refusing to modify an unrecognised directory."
    exit 1
  fi
fi

if [ "$RESUME" = "0" ]; then
  echo "== 1. worktree at the v2 SHA"
  git -C "$SRC" worktree add --detach "$WT" "$SHA" || exit 1
else
  echo "== 1. worktree already present at the v2 SHA — skipped"
fi

cd "$WT" || exit 1
echo "== 2. delete the answer paths"
# CONTAMINATION FIX (2026-09-17, chem run-2 attempt 1): strip experiments/
# WHOLESALE. The old list named the v1 dirs (experiments/convergence,
# cold-baseline) and missed (a) experiments/convergence-v2 — tracked in git
# since adf641ff, so every checkout materialises all prior runs' Java and
# artefacts — and (b) experiments/experiment-protocol.md and
# experiments/RUN_TRAJECTORY.md, which carry the full run history. A chem
# run-2 Codex session ran a repo-wide search, read both, and correctly
# self-declared contamination. The actor needs NOTHING from experiments/.
# EXCEPT experiments/scripts/ (2026-09-17, chem run-2 attempt-2 recording
# failure): the pipeline driver (run_experiment_iteration.py) and pinned
# recorder (snapshot_iter.py) live there and are executed FROM the worktree
# by run_v2_iteration.sh. They are operator tooling with no run results in
# them; restore exactly that subdir from HEAD after the wholesale strip.
rm -rf experiments \
   reference-runs \
       docs/CONVERGENCE_PLAYBOOK.md
rm -f docs/evaluation/*-experiment-*.md 2>/dev/null

# 1b. The extractor's own TEST TREE. Measured 2026-09-22: these are regression
# fixtures for extractor fixes, and they carry case-study solution content --
# StructuralLinterPriorityRulesTest.java contains 'hcmActive && !camActive',
# which is verbatim the worked example the condition-D2 ablation strips from the
# linter's directive, and Java2RoboChartTransformTest.java carries 49
# mode-dispatch / step() constructs. Forbidding the read was not enough: a
# prompt rule rests on instruction plus audit, whereas deleting removes the
# opportunity. Verified the extractor still builds: javac over src/main/java
# alone, with src/test absent, compiles with 0 errors and produces
# StructuralLinter.class. The compile phase builds java.generated.project, not
# this tree, so no phase needs it.
rm -rf forge.transformations/src/test \
       forge.assurance/src/test 2>/dev/null

# 1c. The dashboard's own test fixtures. forge.dashboard/tests/fixtures/lre/
# holds LreController_coreassertions.csp plus trace_m2m / trace_dafny /
# trace_isabelle / trace_m2t_rct / result_codegen for a CONVERGED lre run --
# i.e. a worked answer for one of the three studies, reachable by an actor that
# reads forge.dashboard. No phase reads them; they exist for the dashboard's
# pytest suite, which the pipeline never invokes.
rm -rf forge.dashboard/tests 2>/dev/null
# .remember/ holds session-memory summaries of EARLIER RUNS (which fixes made
# which study converge, in how many iterations). It is TRACKED in the repository
# -- so every `git worktree add` restores it -- AND listed in .gitignore, which
# has no effect on tracked paths; that is why it went unnoticed. A SessionStart
# hook (the Remember plugin) injects it into the actor's context, and the prompt's
# read restriction covers only Java sources, so neither guard stopped it. Found
# when the D2 sranger run-6 actor refused to start (2026-09-23). No phase reads it.
rm -rf .remember 2>/dev/null
# restore ONLY the operator tooling subdir (driver + pinned recorder); its
# prompts/ templates carry no run results either, and snapshot_v2 imports the
# pinned snapshot_iter.py from exactly this path inside the worktree.
git checkout HEAD -- experiments/scripts 2>/dev/null \
  || { echo "FATAL: could not restore experiments/scripts (driver+recorder)"; exit 1; }

# 2a-archive. Per-study answer material inside docs/archive. The archive stays
# readable (pre-registered input, and the sranger runs 1-3 ran with it readable
# — the effective condition "archive readable, no answers for the ACTIVE study
# present" must stay constant across studies). But the archive ships prior
# published work on lre and chemical_detector: solved Isabelle theories AND
# hand-built RoboChart models (.rct/.model) — a ready-made RoboChart model of
# the study is a stronger answer key than the theories, since producing that
# model is the extraction chain's whole job. The first LRE setup caught only
# the .thy files because the gate grepped theories alone; the sweep below
# removes any file or directory under docs/archive whose NAME matches the
# active study, and the widened gate verifies nothing content-matching stays.
case "$STUDY" in
  lre)               anames='-iname *lre*' ;;
  chemical_detector) anames='-iname *gasanalysis* -o -iname *chemical*' ;;
  sranger)           anames='-iname *sranger* -o -iname *s-ranger*' ;;
  *)                 anames='' ;;
esac
if [ -n "$anames" ] && [ -d docs/archive ]; then
  _n=$(find docs/archive \( $anames \) 2>/dev/null | wc -l | tr -d ' ')
  # delete directories first (removes their contents), then stray files
  find docs/archive -type d \( $anames \) -prune -exec rm -rf {} + 2>/dev/null
  find docs/archive -type f \( $anames \) -delete 2>/dev/null
  echo "   stripped ${_n} ${STUDY}-named path(s) from docs/archive (archive itself stays readable)"
fi

# Pre-registration v3: docs/archive holds models and theories of the evaluation
# studies, so every v3-* collection removes it entirely (not just the active
# study's files); the leakage gate below checks that it is gone.
case "$(basename "${FORGE_ARM_DIR:-}")" in v3-*)
  [ -d docs/archive ] && rm -rf docs/archive && echo "   removed docs/archive entirely (pre-registration v3)"
  # and every case study but the active one: the pipeline reads only the
  # study pipeline.yaml names (forge.dashboard/web/runners.py)
  for _cs in forge.assets/case-studies/*/; do
    _cs=$(basename "$_cs"); [ "$_cs" = "$STUDY" ] && continue
    rm -rf "forge.assets/case-studies/$_cs" && echo "   removed case study $_cs (pre-registration v3)"
  done
  # Amendment 3: files no session may read and no pipeline step uses, which name the studies
  for _p in README.md .claude/memory .claude/commands .claude/skills scripts/envmodel_b; do
    [ -e "$_p" ] && rm -rf "$_p" && echo "   removed $_p (pre-registration v3, Amendment 3)"
  done
  # Amendment 9: an unread file naming LRE (the CSP correction step reads
  # forge.dashboard/corrections/, not this folder, so the instrument is unchanged)
  _f=forge.assets/corrections/csp_overrides.csp
  [ -e "$_f" ] && rm -f "$_f" && echo "   removed $_f (pre-registration v3, Amendment 9)"
  # Amendment 4: keep the manifest schema, the one file outside docs/corpus/ the
  # pipeline reads (forge.dashboard/web/manifest.py); it names no study.
  _keep=docs/superpowers/specs/pipeline-manifest.schema.json
  [ -f "$_keep" ] && { mkdir -p /tmp/v3keep.$$; cp "$_keep" /tmp/v3keep.$$/; }
  for _d in docs/*; do
    [ -e "$_d" ] || continue; [ "$_d" = "docs/corpus" ] && continue
    rm -rf "$_d" && echo "   removed $_d (pre-registration v3, Amendment 3)"
  done
  if [ -f /tmp/v3keep.$$/pipeline-manifest.schema.json ]; then
    mkdir -p "$(dirname "$_keep")" && mv /tmp/v3keep.$$/pipeline-manifest.schema.json "$_keep" && rmdir /tmp/v3keep.$$
    echo "   kept $_keep (the pipeline's manifest schema; Amendment 4)"
  fi
  # Amendments 3 and 8: .gitignore lines and pipeline.yaml comment lines that name a study
  bash "$(cd "$(dirname "$0")" && pwd)/v3_strip_study_names.sh" "$WT" ;;
esac

echo "== 2b. remove forge.rtl* (a separate project, not FORGE paper work)"
# Verified before adding this: nothing in FORGE's build, pipeline.yaml, dashboard,
# CLAUDE.md or prompt assets references forge.rtl* — the only reference is
# rtl-pipeline.yaml, which is that project's own config. FORGE's modules are
# forge.assets / forge.assurance / forge.dashboard / forge.transformations.
# Removing them keeps the run's reading surface to FORGE alone.
_before=$(ls -d forge.rtl* 2>/dev/null | wc -l | tr -d ' ')
rm -rf forge.rtl forge.rtl.cnl forge.rtl.cnl.web forge.rtl.dashboard forge.rtl.transformations
rm -f rtl-pipeline.yaml 2026-07-28-trust-boundary-mde-rtl-formal-loop-design.md
_after=$(ls -d forge.rtl* 2>/dev/null | wc -l | tr -d ' ')
# Report what was actually removed, not a hardcoded "5". The literal claimed a
# count the script never checked: if the pinned tree ever carries four or six
# forge.rtl* modules the line would still say 5. (The post-removal count is
# separately asserted in VERIFICATION below.)
echo "   removed $(( _before - _after )) forge.rtl* module(s) (of ${_before} present), plus rtl-pipeline.yaml and the rtl design note if present"

echo "== 2c. clear stale phase verdicts"
# post_*.json / post_*.md / phase_timings.json are gitignored (.gitignore:60),
# so `git worktree add` does NOT bring them and they are never cleared by the
# pipeline driver either. If anything has already run in this directory, the
# previous verdicts sit on disk and the recorder cannot tell them from fresh
# ones. Worse, the feedback layer diffs against post_<phase>.json to compute
# new/recurring/resolved, so a leftover file makes a COLD run report issues as
# "RECURRING x6" and claim it "resolved" a failure from a run that never
# happened here.
rm -f forge.assets/corrections/post_*.json forge.assets/corrections/post_*.md \
      forge.assets/corrections/phase_timings.json
echo "   cleared post_*.{json,md} and phase_timings.json"

echo "== 3. point the pipeline at ${STUDY}"
/usr/bin/sed -i '' "s/^  active_case_study: .*/  active_case_study: ${STUDY}/" pipeline.yaml
grep -n "active_case_study" pipeline.yaml | sed 's/^/   /'

echo
echo "== VERIFICATION"
fail=0

n=$(find java.generated.project/src -name '*.java' 2>/dev/null | wc -l | tr -d ' ')
if [ "${n:-0}" = "0" ]; then echo "   OK   codebase starts empty (0 java files)"
else echo "   FAIL ${n} java file(s) present"; fail=1; fi

# experiments/ must contain ONLY the restored scripts/ tooling: verify the
# answer material is gone AND the tooling is back (recording depends on it).
x=$(find experiments -mindepth 1 -maxdepth 1 2>/dev/null | grep -v "experiments/scripts$" | head -3)
if [ -z "$x" ]; then echo "   OK   experiments/ carries only scripts/ (tooling)"
else echo "   FAIL experiments/ still carries: $x"; fail=1; fi
for t in experiments/scripts/run_experiment_iteration.py experiments/scripts/snapshot_iter.py; do
  [ -f "$t" ] && echo "   OK   present: $t" || { echo "   FAIL tooling missing: $t"; fail=1; }
done
for p in experiments/experiment-protocol.md experiments/RUN_TRAJECTORY.md experiments/convergence-v2 reference-runs docs/CONVERGENCE_PLAYBOOK.md .remember; do
  if [ ! -e "$p" ]; then echo "   OK   absent: $p"
  else echo "   FAIL still present: $p"; fail=1; fi
done

v=$(ls forge.assets/corrections/post_*.json forge.assets/corrections/post_*.md 2>/dev/null | wc -l | tr -d ' ')
if [ "${v:-0}" = "0" ]; then echo "   OK   no stale phase verdicts (cold start)"
else echo "   FAIL ${v} leftover post_* verdict file(s) — a cold run would diff against them"; fail=1; fi

# CONDITION F (2026-10-25). The reference-multicontroller-f collection runs the
# PUBLISHED pipeline (the Forge repository's trees, byte-identical), whose
# comments carry no internal defect labels. The checks below that identify a
# repair by its label ("Fnn FIX") therefore fall back to the repair's CODE
# pattern under this collection; every other collection is checked exactly as
# before. F18 is special: the published generator retired the `offered =
# listens` environment assumption on 2026-10-20 (per-state offer invariant,
# Idle operations, split-then-blast closer), so for F the check is for the
# replacement, and the OLD pin is a FAIL.
LABEL_SCRUBBED=0
[ "$(basename "${FORGE_ARM_DIR:-}")" = "reference-multicontroller-f" ] && LABEL_SCRUBBED=1
# Pre-registration v3: the published (label-free) pipeline with study-free instructions.
V3=0; case "$(basename "${FORGE_ARM_DIR:-}")" in v3-*) V3=1; LABEL_SCRUBBED=1 ;; esac
# COLD BASELINE v2 (2026-10-28). Each pass is iteration 1 of its study's Table 7
# condition: the Chemical Detector at condition F's pin (label-scrubbed published
# trees), SRanger and the LRE at the frozen pin (labelled). See
# review/prereg-cold-baseline-v2.md.
[ "$(basename "${FORGE_ARM_DIR:-}")" = "cold-baseline-v2" ] && [ "$STUDY" = "chemical_detector" ] && LABEL_SCRUBBED=1
# OPUS 5.5 RERUN: the compile-only Chemical Detector runs at condition F's pin.
[ "$(basename "${FORGE_ARM_DIR:-}")" = "compile-only-opus55" ] && [ "$STUDY" = "chemical_detector" ] && LABEL_SCRUBBED=1
ETL=forge.transformations/src/main/resources/transformations/java2robochart.etl
EGL=forge.transformations/src/main/resources/transformations/thy_generation_rule.egl
c=$(grep -c 'prependNegatedGuards(' "$ETL" 2>/dev/null)
if [ "${c:-0}" -ge 1 ]; then echo "   OK   extractor carries C1 repair (${c} call sites)"
else echo "   FAIL C1 repair absent — wrong SHA"; fail=1; fi
i=$(grep -c 'invTrigCount' "$EGL" 2>/dev/null)
if [ "${i:-0}" -ge 1 ]; then echo "   OK   extractor carries C2 repair (${i} sites)"
else echo "   FAIL C2 repair absent — wrong SHA"; fail=1; fi
# D3 (2435b66f): derived Dafny contracts encode branch priority — the gate list
# must grow with each repair the pre-registered SHA carries, or setup verifies
# yesterday's extractor. 'priors' is the negated-conjunction accumulator.
DFY=forge.transformations/src/main/resources/transformations/java2dafny.egl
d=$(grep -c 'priors' "$DFY" 2>/dev/null)
if [ "${d:-0}" -ge 2 ]; then echo "   OK   extractor carries D3 repair (${d} sites)"
else echo "   FAIL D3 repair absent — wrong SHA"; fail=1; fi

ETL=forge.transformations/src/main/resources/transformations/java2robochart.etl
THY=forge.transformations/src/main/resources/transformations/thy_generation_rule.egl
f6=$(grep -c 'getEAllReferences' "$ETL" 2>/dev/null)
f7=$(grep -c 'zsafeThyName' "$THY" 2>/dev/null)
f8=$(grep -c 'trueExp.value = "true"' "$ETL" 2>/dev/null)
f9=$(grep -cE 'isTypeOf\((Seq|Set)Type\)' "$THY" 2>/dev/null)
f9b=$(grep -c 'F9b FIX' "$DFY" 2>/dev/null); [ "$LABEL_SCRUBBED" = 1 ] && f9b=$(grep -c 'ensures old(' "$DFY" 2>/dev/null)
f9c=$(grep -c 'ensures old(' "$DFY" 2>/dev/null)
if [ "${f6:-0}" -ge 1 ] && [ "${f7:-0}" -ge 1 ] && [ "${f8:-0}" -ge 1 ] && \
   [ "${f9:-0}" -ge 2 ] && [ "${f9b:-0}" -ge 1 ] && [ "${f9c:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F6-F9c repairs (${f6}/${f7}/${f8}/${f9}/${f9b}/${f9c} sites)"
else
  echo "   FAIL F6-F9c repair set incomplete (${f6}/${f7}/${f8}/${f9}/${f9b}/${f9c}) — wrong SHA"; fail=1
fi

# F15-F18 (fa4241f8 re-cut, 2026-09-16): ranked method lookup; terminal-state
# warning gate; `until` emission; the offered=listens environment assumption
# with computed exhaust_disc tactic. The gate list grows with the fix set or
# setup verifies yesterday's extractor.
f15=$(grep -c 'findJavaMethodForFunction' "$ETL" 2>/dev/null)
f16=$(grep -c 'F16 WARNING' "$THY" 2>/dev/null); [ "$LABEL_SCRUBBED" = 1 ] && f16=$(grep -c 'WARNING --' "$THY" 2>/dev/null)
f17=$(grep -c 'until "\[%=untilTerm%\]"' "$THY" 2>/dev/null)
f18a=$(grep -c 'offered = listens' "$THY" 2>/dev/null)
f18b=$(grep -c 'exhaustFacts' "$THY" 2>/dev/null)
if [ "$LABEL_SCRUBBED" = 1 ]; then
  # model B: the pin must be ABSENT from code (comments may mention it as history)
  # and the per-state offer invariant plus Idle operations present.
  f18a=$(grep -vE '^[[:space:]]*//' "$THY" | grep -c 'offered = listens'); f18a=$(( f18a == 0 ? 1 : 0 ))
  f18b=$(grep -c 'offerInvConjuncts' "$THY" 2>/dev/null)
fi
f16p=$(grep -c 'MUST HAVE AN OUTGOING' forge.assets/prompts/java_codegen_rules.txt 2>/dev/null)
if [ "$V3" = 1 ]; then
  # v3 replaced the false-obligation warning by a terminal-state note, and the
  # study-free rules state that a terminal mode is valid instead of requiring a
  # tick self-loop (review/prereg-v3-study-free.md).
  f16=$(grep -c 'exempts them from the deadlock-freedom' "$THY" 2>/dev/null)
  f16p=$(grep -c 'a mode with no outgoing transition is a terminal mode' forge.assets/prompts/java_codegen_rules.txt 2>/dev/null)
fi
if [ "${f15:-0}" -ge 2 ] && [ "${f16:-0}" -ge 1 ] && [ "${f17:-0}" -ge 1 ] && \
   [ "${f18a:-0}" -ge 1 ] && [ "${f18b:-0}" -ge 2 ] && [ "${f16p:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F15-F18 repairs (${f15}/${f16}/${f17}/${f18a}/${f18b}/${f16p} sites)"
else
  echo "   FAIL F15-F18 repair set incomplete (${f15}/${f16}/${f17}/${f18a}/${f18b}/${f16p}) — wrong SHA"; fail=1
fi

# F20 (fa4241f8, tenth cut, 2026-09-17): negated-conjunction parenthesisation
# in both theory printers + the vacuity parser's UNKNOWN classification.
f20a=$(grep -c 'F20 FIX' "$THY" 2>/dev/null)
f20b=$(grep -c 'F20' forge.dashboard/web/vacuity_sat.py 2>/dev/null)
if [ "$LABEL_SCRUBBED" = 1 ]; then
  # site 2: the Not case in the main printer; site 1: its twin in notString()
  f20a=$(( $(grep -c 'binds TIGHTER' "$THY" 2>/dev/null) + $(grep -c 'binds tighter' "$THY" 2>/dev/null) ))
  # the parser's negated-conjunction branch yields an explicit unknown atom
  f20b=$(grep -c 'startswith("!") and "&&" in stripped' forge.dashboard/web/vacuity_sat.py 2>/dev/null)
fi
if [ "${f20a:-0}" -ge 2 ] && [ "${f20b:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F20 repair (${f20a} printer sites, parser guarded)"
else
  echo "   FAIL F20 repair absent (${f20a}/${f20b}) — wrong SHA"; fail=1
fi

# F21 (fa4241f8, eleventh cut, 2026-09-17): enum-typed operation fields must
# reference the Enumeration, not fall through to `real` (chem run-2 fdr4 load
# error "expected Angle, actual Int" at CALL__move).
f21=$(grep -c 'enumTypeMap.containsKey(javaTypeName)' "$ETL" 2>/dev/null)
f21b=$(grep -c 'F21b FIX' "$ETL" 2>/dev/null); [ "$LABEL_SCRUBBED" = 1 ] && f21b=$(grep -c 'enumTypeMap.containsKey(javaTypeName)' "$ETL" 2>/dev/null)
if [ "${f21:-0}" -ge 1 ] && [ "${f21b:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F21+F21b (enum typing and placeholders)"
else
  echo "   FAIL F21/F21b incomplete (${f21:-0}/${f21b:-0}) — wrong SHA"; fail=1
fi

# F22 (194e5f5b, twelfth cut, 2026-09-18): enum-typed METHOD PARAMETERS resolve
# for the Phase 6b zero-arg lift, so they type as their Enumeration instead of
# the `nat` default. The scope check is load-bearing, not cosmetic: an unscoped
# CtParameter lookup silently retyped chem run-2's lv/stuckDist/stuckPeriod by
# shadowing constantTypeMap's deliberate int->nat, because those accessor names
# collide with the constants class's constructor parameter names.
f22a=$(grep -c 'enumNamesForParamLookup' "$ETL" 2>/dev/null)
f22b=$(grep -c 'Spoon!CtParameter.all' "$ETL" 2>/dev/null)
f22c=$(grep -c 'enumNamesForParamLookup.includes' "$ETL" 2>/dev/null)
if [ "${f22a:-0}" -ge 3 ] && [ "${f22b:-0}" -ge 1 ] && [ "${f22c:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F22 repair (enum-parameter typing, enum-scoped)"
else
  echo "   FAIL F22 repair absent or not enum-scoped (${f22a}/${f22b}/${f22c}) — wrong SHA"; fail=1
fi

# F23/F23b (85e6ee51, thirteenth cut, 2026-09-19): csp_corrections.py's
# communicated-constant scan keys rct_consts LOWER-CASED but matched the raw
# action line case-sensitively, so a camelCase constant never registered and the
# domain widening never fired -- FDR aborted "not a member of {0,1}" and every
# assertion came back inconclusive. Found by the autonomous arm's sranger run-2,
# which worked around it in its own Java rather than editing the instrument.
# The wait-strip (F23b) is load-bearing, not cosmetic: the case-fix ALONE would
# have widened chem claude run-1, whose `wait ( outPeriod )` carries a 2 yet
# whose fdr4 passed 9/9 at real={0..1} -- a wait argument is a timed delay, not
# a communicated payload.
CSPC=forge.dashboard/web/csp_corrections.py
f23a=$(grep -c 'scan_line, re.I' "$CSPC" 2>/dev/null)
f23b=$(grep -c -F 'wait\s*\([^)]*\)' "$CSPC" 2>/dev/null)
if [ "${f23a:-0}" -ge 1 ] && [ "${f23b:-0}" -ge 1 ]; then
  echo "   OK   dashboard carries F23 repair (case-insensitive scan, wait-args excluded)"
else
  echo "   FAIL F23 repair absent or missing the wait-strip (${f23a}/${f23b}) -- wrong SHA"; fail=1
fi

# F24 (535099f5, fourteenth cut, 2026-09-19): the ZERO-ARGUMENT abstracted-call branch in
# java2dafny.egl registered the method but recorded no return type, so emission
# fell to the "real" default. A Java `int cstc()` called NESTED --
# `sensor.odist(calcCStc.cstc())` -- emitted `function cstc(): real` and Dafny
# rejected the call ("expected int, found real"). Found by the autonomous arm's
# lre run-3, which hoisted the call into an int local to work around it.
DFY=forge.transformations/src/main/resources/transformations/java2dafny.egl
f24=$(grep -c 'F24 FIX' "$DFY" 2>/dev/null); [ "$LABEL_SCRUBBED" = 1 ] && f24=$(grep -c 'zero-argument branch' "$DFY" 2>/dev/null)
if [ "${f24:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F24 repair (zero-arg return typing)"
else
  echo "   FAIL F24 repair absent -- wrong SHA"; fail=1
fi

# F25 (358232f5, fifteenth cut, 2026-09-19): the FloatExp render site emitted Java's
# SCIENTIFIC notation verbatim (`1.0E-9`). Isabelle/HOL has no `E` exponent, so
# the theory read it as the application `(1.0) (-9)` -- "Type error in
# application: operator not of function type", 54 occurrences in one theory.
# Found by the autonomous arm's lre run-4, which worked around it by moving the
# literal behind an abstracted sensor method.
THY=forge.transformations/src/main/resources/transformations/thy_generation_rule.egl
f25a=$(grep -c 'operation normaliseFloatLiteral' "$THY" 2>/dev/null)
f25b=$(grep -c 'normaliseFloatLiteral(self.value.toString())' "$THY" 2>/dev/null)
if [ "${f25a:-0}" -ge 1 ] && [ "${f25b:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F25 repair (scientific-notation literals)"
else
  echo "   FAIL F25 repair absent or not wired (${f25a}/${f25b}) -- wrong SHA"; fail=1
fi

# F26 (b57f185a, sixteenth cut, 2026-09-19): buildInferredFunction read only
# javaParams.first() and added exactly ONE RoboChart Parameter, so every
# multi-argument Java method was modelled as unary. The theory generator's own
# CASE 2 (`a \<times> b \<Rightarrow> r`) was unreachable from inferred
# functions: `double tcpaFrom(double, double)` emitted
# `consts tcpaFrom :: "real \<Rightarrow> real"` against a two-argument call
# site, and Isabelle failed with "Type unification failed: Clash of types
# _ x _ and R". Found by the autonomous arm's lre run-5.
ETL2=forge.transformations/src/main/resources/transformations/java2robochart.etl
f26a=$(grep -c 'F26 FIX' "$ETL2" 2>/dev/null); [ "$LABEL_SCRUBBED" = 1 ] && f26a=$(grep -c 'while (pi < nParams)' "$ETL2" 2>/dev/null)
f26b=$(grep -c 'while (pi < nParams)' "$ETL2" 2>/dev/null)
if [ "${f26a:-0}" -ge 1 ] && [ "${f26b:-0}" -ge 1 ]; then
  echo "   OK   extractor carries F26 repair (multi-parameter inferred functions)"
else
  echo "   FAIL F26 repair absent or not looping (${f26a}/${f26b}) -- wrong SHA"; fail=1
fi

# F27/F28 (seventeenth cut, 2026-09-20), both found by chemical_detector run-5:
#  F27 - the params guard and the bare-Assignment update path both required a
#        SeqStatement action, so a transition whose action is a SINGLE assignment
#        emitted no `<sv>_input` param while still rendering the lens read as
#        `v()`: "Extra variables on rhs: v".
#  F28 - the payload-set `definition <Set> = UNIV` was emitted once per
#        rebinding transition rather than once per theory: "Duplicate constant
#        declaration".
THY2=forge.transformations/src/main/resources/transformations/thy_generation_rule.egl
f27=$(grep -c 'F27 FIX' "$THY2" 2>/dev/null); [ "$LABEL_SCRUBBED" = 1 ] && f27=$(grep -c 'bareSkip' "$THY2" 2>/dev/null)
f27b=$(grep -c 'bareSkip' "$THY2" 2>/dev/null)
f28=$(grep -c 'emittedPayloadSets' "$THY2" 2>/dev/null)
if [ "${f27:-0}" -ge 2 ] && [ "${f27b:-0}" -ge 1 ] && [ "${f28:-0}" -ge 2 ]; then
  echo "   OK   extractor carries F27+F28 repairs (single-assignment params, payload-set dedup)"
else
  echo "   FAIL F27/F28 repair absent (${f27}/${f27b}/${f28}) -- wrong SHA"; fail=1
fi

k=$(find . \( -name '*blind_reference_spec*' -o -name 'LRE_REQUIREMENTS_REFERENCE_SPEC*' \) 2>/dev/null | wc -l | tr -d ' ')
if [ "${k:-0}" = "0" ]; then echo "   OK   no answer keys in the tree"
else echo "   FAIL ${k} answer-key file(s) present"; fail=1; fi

# docs/archive is EXPLICITLY readable by the run (both iteration prompts say
# "You may also read docs/corpus/ and docs/archive/ freely"). It ships the
# ICECCS2023 companion material, which includes COMPLETED, HAND-VERIFIED
# Isabelle theories for two of the three studies:
#   LRE_Beh_*.thy, LRE_R1ToR3.thy, LRE_Z_Machine_deadlock_free.thy   -> lre
#   GasAnalysis_*.thy                                                -> chemical_detector
#                                                                       (its controller is GasAnalysisController)
# Those carry the very lemma names the isabelle_gen/isabelle_verify phases must
# produce and discharge (Init_inv, <Transition>_inv, deadlock_free), so a run of
# lre or chemical_detector can read its own target obligations from a permitted
# path. sranger has no counterpart there and is unaffected.
#
# This is REPORTED, not auto-deleted: docs/archive is a pre-registered input and
# what to do about it (narrow the prompt's permission, or strip the subtree for
# these two studies) is the operator's call, not this script's.
leak=0
# Widened after the first LRE setup: the .thy-only grep missed hand-built
# RoboChart models (.rct/.model) of the study sitting in the same archive —
# a stronger answer key than the theories. Gate on NAME across all file
# kinds, matching the sweep in step 2a-archive.
case "$STUDY" in
  lre)               gpat='lre' ;;
  chemical_detector) gpat='gasanalysis|chemical' ;;
  sranger)           gpat='sranger|s-ranger' ;;
  steam_boiler)      gpat='steam|boiler' ;;
  *)                 gpat='' ;;
esac
if [ -n "$gpat" ] && [ -d docs/archive ]; then
  leak=$(find docs/archive -type f 2>/dev/null | grep -icE "/[^/]*(${gpat})[^/]*$")
fi

# SOLUTION-CONTENT GATE (added 2026-09-22). The forbidden-reads list rests on
# instruction plus transcript audit; this gate rests on absence. It fails setup
# if any file the actor may still read carries controller-shaped logic -- a
# mode-dispatch comparison or a negated-conjunction else-if chain -- outside a
# comment. That is the shape the ablated linter rules are about, so a surviving
# copy would hand the actor the construct the condition removed.
#
# Counted, never grep -q: `grep -qv` on EMPTY input differs across greps and
# inverts the test, reporting FAIL exactly when the strip SUCCEEDED.
_leak=0
for _t in forge.transformations forge.assurance forge.dashboard; do
  [ -d "$_t" ] || continue
  while IFS= read -r _f; do
    # CONDITION C EXEMPTION (2026-09-26). C (convergence-v3-frozen) is the
    # UNABLATED reference arm: its structural linter carries the remedy text the
    # ablations remove, by design. This gate was added after C's first 12 runs
    # were set up with this same linter; exempting exactly that one file for C
    # keeps C runs 3-5 identical to runs 1-2. Every other file, and every other
    # collection, is still gated.
    if [ "$(basename "${FORGE_ARM_DIR:-}")" = "convergence-v3-frozen" ] && \
       [ "$_f" = "forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java" ]; then
      echo "   EXEMPT $_f (condition C is the unablated reference; linter unchanged from C runs 1-2)"
      continue
    fi
    # CONDITION F EXEMPTION (2026-10-25). F is the unablated Reference arm for
    # the Chemical Detector, run on the published pipeline, whose linter carries
    # the same remedy text as C's. Unlike C, F's prompts also forbid the actor
    # from reading forge.transformations/ and forge.dashboard/ at all.
    # COLD BASELINE v2 EXEMPTION (2026-10-28): the same file, for the same reason as
    # C (SRanger, LRE) and F (Chemical Detector), whose iteration 1 each pass repeats.
    # OPUS 5.5 RERUN EXEMPTION: C and compile-only run the unablated Reference linter,
    # as C and F did; D2 and E stay gated, as their originals were.
    if { [ "$(basename "${FORGE_ARM_DIR:-}")" = "convergence-c-opus55" ] || [ "$(basename "${FORGE_ARM_DIR:-}")" = "compile-only-opus55" ]; } && \
       [ "$_f" = "forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java" ]; then
      echo "   EXEMPT $_f (Opus 5.5 rerun of the unablated Reference condition)"
      continue
    fi
    if [ "$(basename "${FORGE_ARM_DIR:-}")" = "cold-baseline-v2" ] && \
       [ "$_f" = "forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java" ]; then
      echo "   EXEMPT $_f (cold baseline v2 repeats iteration 1 of the unablated C/F runs)"
      continue
    fi
    if [ "$V3" = 1 ] && \
       [ "$_f" = "forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java" ]; then
      echo "   EXEMPT $_f (pre-registration v3: the actor is forbidden to read this tree)"
      continue
    fi
    if [ "$(basename "${FORGE_ARM_DIR:-}")" = "reference-multicontroller-f" ] && \
       [ "$_f" = "forge.transformations/src/main/java/forge/transformations/preflight/StructuralLinter.java" ]; then
      echo "   EXEMPT $_f (condition F is the unablated Reference arm on the published pipeline; the actor is forbidden to read this tree)"
      continue
    fi
    _n=$(grep -nE "currentMode[[:space:]]*==[[:space:]]*[A-Za-z_]|else[[:space:]]+if[[:space:]]*\([[:space:]]*!?[A-Za-z_][A-Za-z0-9_]*[[:space:]]*&&" "$_f" 2>/dev/null \
         | grep -vcE "^[0-9]+:[[:space:]]*(//|/\*|\*|#|--)")
    if [ "${_n:-0}" -gt 0 ]; then
      echo "   FAIL solution-shaped logic survives in $_f ($_n line(s))"
      _leak=$((_leak + _n))
    fi
  done <<EOF
$(find "$_t" -type f \( -name "*.java" -o -name "*.etl" -o -name "*.egl" -o -name "*.py" \) 2>/dev/null)
EOF
done
if [ "$_leak" = "0" ]; then
  echo "   OK   no solution-shaped logic in any tree the actor may read"
else
  echo "        the actor may read these trees; add the path to the strip above"
  fail=1
fi
if [ "${leak:-0}" = "0" ]; then
  echo "   OK   docs/archive carries no ${STUDY} answer material (any file kind)"
else
  echo "   FAIL docs/archive carries ${leak} ${STUDY}-named file(s),"
  echo "        and the iteration prompt permits reading docs/archive freely:"
  find docs/archive -type f 2>/dev/null | grep -iE "/[^/]*(${gpat})[^/]*$" | head -8 | sed 's/^/          /'
  echo "        Step 2a-archive should have stripped these — investigate."
  fail=1
fi

if grep -q "^  active_case_study: ${STUDY}" pipeline.yaml; then
  echo "   OK   active_case_study: ${STUDY}"
else echo "   FAIL pipeline.yaml not pointed at ${STUDY}"; fail=1; fi

r=$(ls -d forge.rtl* 2>/dev/null | wc -l | tr -d ' ')
if [ "${r:-0}" = "0" ]; then echo "   OK   forge.rtl* absent (separate project, not in scope)"
else echo "   FAIL ${r} forge.rtl* module(s) still present"; fail=1; fi

# FORGE's own four modules must survive the removal above.
missing=""
for m in forge.assets forge.assurance forge.dashboard forge.transformations; do
  [ -d "$m" ] || missing="$missing $m"
done
if [ -z "$missing" ]; then echo "   OK   FORGE's four modules intact"
else echo "   FAIL missing FORGE module(s):$missing"; fail=1; fi

h=$(git -C "$WT" rev-parse HEAD 2>/dev/null)
if [ "$h" = "$SHA" ]; then echo "   OK   HEAD is the v2 SHA"
else echo "   FAIL HEAD is ${h:-unknown}"; fail=1; fi

# The study's own inputs must be the committed ones — no leftover de-prescribed
# drafts or hand edits (requirements are an immutable input, per the protocol).
d=$(git -C "$WT" status --porcelain -- "forge.assets/case-studies/${STUDY}" 2>/dev/null | wc -l | tr -d ' ')
if [ "${d:-0}" = "0" ]; then echo "   OK   ${STUDY} inputs are the committed ones (unmodified)"
else echo "   FAIL ${STUDY} inputs modified in the worktree:"; git -C "$WT" status --porcelain -- "forge.assets/case-studies/${STUDY}" | sed 's/^/        /'; fail=1; fi

# INSTRUCTION LEAKAGE GATE (pre-registration v3, 29 October 2026). For every
# collection run under pre-registration v3 (directory name v3-*), nothing a
# generation session reads, and no feedback text the pipeline writes, may carry
# any evaluation study's name, declared identifiers or domain words.
if [ "${FORGE_ARM_DIR:-}" != "" ] && case "$(basename "$FORGE_ARM_DIR")" in v3-*) true;; *) false;; esac; then
  _gate="$(cd "$(dirname "$0")" && pwd)/instruction_leak_gate.py"
  if python3 "$_gate" "$WT" --feedback --no-archive --summary > /tmp/leakgate.$$ 2>&1; then
    echo "   OK   instructions and feedback text carry no study content (leakage gate)"
    _others=$(ls -d forge.assets/case-studies/*/ 2>/dev/null | grep -v "/${STUDY}/$" | wc -l | tr -d ' ')
    if [ "$_others" = "0" ] && [ -d "forge.assets/case-studies/${STUDY}" ]; then echo "   OK   only the ${STUDY} case study is present"
    else echo "   FAIL ${_others} other case-study folder(s) present, or ${STUDY} missing"; fail=1; fi
    _left=""
    for _p in README.md .claude/memory .claude/commands .claude/skills scripts/envmodel_b; do [ -e "$_p" ] && _left="$_left $_p"; done
    for _f in $(find docs -type f 2>/dev/null | grep -vE '^docs/corpus/|^docs/superpowers/specs/pipeline-manifest\.schema\.json$'); do _left="$_left $_f"; done
    if [ -z "$_left" ]; then echo "   OK   no unread file naming a study (Amendment 3)"
    else echo "   FAIL present:$_left (Amendment 3)"; fail=1; fi
    if bash "$(cd "$(dirname "$0")" && pwd)/v3_strip_study_names.sh" "$WT" --check; then echo "   OK   .gitignore and pipeline.yaml comments name no study (Amendments 3 and 8)"
    else echo "   FAIL .gitignore or a pipeline.yaml comment names a study (Amendments 3 and 8)"; fail=1; fi
    # Amendment 4: the pipeline must be able to load its manifest in this worktree,
    # with the interpreter the runner would choose (first that imports the three).
    _py=""
    for _c in python3 /opt/homebrew/bin/python3 /usr/local/bin/python3 /usr/bin/python3 "$HOME/.claude-science/conda/envs/python/bin/python"; do
      "$_c" -c 'import yaml, jsonschema, ruamel.yaml' >/dev/null 2>&1 && { _py="$_c"; break; }
    done
    if [ -n "$_py" ] && ( cd forge.dashboard && "$_py" -c 'import sys; from pathlib import Path; sys.path.insert(0, "."); from web.manifest import Manifest; m = Manifest.load(Path("../pipeline.yaml")); ids = m.phase_ids() if callable(m.phase_ids) else m.phase_ids; assert len(m.ordered()) >= 12 and "isabelle_verify" in ids, ids' ) >/tmp/v3mf.$$ 2>&1; then
      echo "   OK   the pipeline loads its manifest in this worktree (Amendment 4)"
    else echo "   FAIL the pipeline cannot load its manifest here (Amendment 4): $(tail -1 /tmp/v3mf.$$ | cut -c1-120)"; fail=1; fi
    rm -f /tmp/v3mf.$$
  else
    echo "   FAIL leakage gate: study content in what the actor reads"
    sed 's/^/        /' /tmp/leakgate.$$ | head -12
    fail=1
  fi
  rm -f /tmp/leakgate.$$
fi

echo
if [ "$fail" = "0" ]; then
  if [ -n "${FORGE_WT_OVERRIDE:-}" ]; then
    # Called by setup_claude_run.sh: every current collection is driven by
    # Claude Code, and the caller prints the actor handover after its own
    # checks. The Codex handover below applied only to the archived Codex arm.
    echo "BASE SETUP OK -- continuing with the Claude Code arm checks below."
  else
    echo "READY."
    echo "  1. open a FRESH Codex session with cwd = $WT"
    echo "  2. note its thread id (one session per run — that is the independence mechanism)"
    echo "  3. paste the iteration-1 cold prompt:"
    PRE="/Users/ranwei/Git/TOSEM_Formal_Guided_Vibe_Coding/review/codex-prompts/v2-run${RUNK}-${STUDY}-iter1.md"
    if [ -f "$PRE" ]; then
      echo "     ready-made (no editing needed): $PRE"
    else
      echo "     no instantiated prompt for this run yet — use the template and"
      echo "     substitute {study}=${STUDY} and {run_k}=${RUNK} yourself:"
      echo "     review/codex-prompts/iter-1-cold.md"
    fi
  fi
else
  echo "NOT READY — fix the FAIL lines above before starting run ${RUNK}."
  exit 1
fi
