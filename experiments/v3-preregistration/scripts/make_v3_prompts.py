#!/usr/bin/env python3
"""Generate every prompt of pre-registration v3 (review/prereg-v3-study-free.md).

    python3 review/make_v3_prompts.py

Writes review/v3-prompts/v3-cold-<study>-run<k>.md (k = 1..10, three studies) and
v3-reference-<study>-run<k>.md (k = 1..5, three studies; Steam Boiler run 1), then
checks that, within a condition, the prompts differ only in the study, the run
number and the paths derived from them, and runs the leakage gate on each prompt
with that study's own name and identifiers allowed (a prompt must name its study).
"""
import os, re, subprocess, sys, difflib

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "v3-prompts")
RESEARCH = "/Users/ranwei/Gitee/formal_method_guided_vibe_coding"
PIN = "56cb43d7835b43e8a986e6ba4c0e82f35083d6f1"
PIN_A14 = "aaddf2225b38eb1eb749e816f6f5874d7ec93eaa"   # Amendments 14-16 (Steam Boiler): the Amendment 16 pin
COLL = {"cold": ("v3-cold", "forge-v3c9", "/Users/ranwei/Gitee/v3c9-staging"),
        "reference": ("v3-reference", "forge-v3r10", "/Users/ranwei/Gitee/v3r10-staging"),
        "compileonly": ("v3-compile-only", "forge-v3o18", "/Users/ranwei/Gitee/v3o18-staging")}   # Amendment 18
RUNS = {"cold": {**{s: range(1, 11) for s in ("sranger", "lre", "chemical_detector")}, "steam_boiler": range(1, 11)},   # Amendments 11 and 12: cold Steam Boiler passes 1-10
        "reference": {"sranger": range(1, 6), "lre": range(1, 6), "chemical_detector": range(1, 6), "steam_boiler": range(1, 6)},
        "compileonly": {"sranger": range(1, 6), "lre": range(1, 6), "chemical_detector": range(1, 6)}}   # Amendment 18   # Amendment 11: Steam Boiler runs 2-5
# Replacements for void sessions (Validity section): the next number of the same study
# and collection. Added when a session is voided, before the replacement runs.
# Withdrawn unused by Amendment 5 (their prompts, v3-cold-lre-run11.md and
# v3-reference-lre-run6.md, stay in v3-prompts/, marked withdrawn):
#   {"cold": {"lre": [11]}, "reference": {"lre": [6]}}
REPLACEMENTS = {}
for _c, _m in REPLACEMENTS.items():
    for _s, _ks in _m.items():
        RUNS[_c][_s] = list(RUNS[_c][_s]) + _ks

COMMON = """You are an independent developer implementing a reactive safety controller in
Java from a specification. A downstream toolchain extracts a formal model from your
Java and verifies it.

This session drives **exactly one {unit}** — this one — and nothing else. If
anything earlier in this conversation concerned another run of this or any other
system, **stop and tell the operator**: the {unit} is contaminated and must be
started in a new session.

## Read exactly these

{sysdesc}- `forge.assets/case-studies/{study}/requirements/requirement_all.json` — the requirements
- `forge.assets/case-studies/{study}/requirements/README.md` — the requirement-ID conventions
- `CLAUDE.md` (repository root) and the files it names under `forge.assets/prompts/`
- the five layer prompts in `forge.assets/vibe-coding-prompts/`

You may also read `docs/corpus/` (the Z-Machine framework and generic examples)
if you need background on Isabelle. Nothing else under `docs/`.

## Forbidden — a violation voids this {unit}

Reading any of these voids the {unit}; the transcript and session log are audited.

- `forge.transformations/` and `forge.dashboard/` — the source of the phases that
  judge your Java. Write the Java from the requirements and the rules, not from the
  checkers. If you believe you cannot proceed without knowing what a phase does
  internally, **stop and report that to the operator**.
- `experiments/`, `reference-runs/`, and `docs/` outside `docs/corpus/`;
- any other folder under `forge.assets/case-studies/` (setup removes them);
- the source of the scripts you run, other worktrees, the staging directory
  `{stage}/`, and the git history (`git log`, `git show`, `git diff`).

## Hard constraints

- Do not change anything under `forge.transformations/`, `forge.dashboard/`,
  `forge.assets/prompts/`, `forge.assets/vibe-coding-prompts/`,
  `forge.assets/case-studies/`, `pipeline.yaml` or `CLAUDE.md`. These are the
  instrument and the input, not your code.
- Write Java only under `java.generated.project/src/main/java/{study}/`, plus the
  trace file and the files the Output section names.
- Run the pipeline only through the runner named below. Do not invoke the pipeline
  driver or any phase directly. Do not commit.

## What to do

`java.generated.project` contains a Gradle scaffold and no Java. If you find any
Java under `java.generated.project/src/main/java/`, delete it before writing and
say so in your summary.

1. Work the five layers in order — Architecture, Skeleton, Leaves, Controller,
   Review — without pausing between them. You may compile your code with `javac`
   or Gradle while you work.
2. Write `java.generated.project/result_codegen.json`, the map from every
   requirement to the Java elements that implement it, exactly as
   `forge.assets/prompts/codegen_trace_rules.txt` describes. Every requirement
   needs at least one entry, and every Java file you write needs at least one entry
   naming it. A requirement type that the trace rules do not list maps to the Java
   element that realises it (a transition to `step`, a whole-machine constraint to
   the controller class).
3. Write `forge.assets/corrections/ITER_SUMMARY.md` (at most 300 words): what you
   wrote and why, every decision the requirements did not settle and how you
   resolved it, and anything you could not resolve.

Do not paste Java into the chat.

## Where to work

Your working directory is

```
{wt}
```

`cd` there first. If it does not exist, **stop and say so**; no other worktree is a
substitute. Export these four variables in your shell before running the runner:

```
export FORGE_ARM_DIR={research}/experiments/{coll}
export FORGE_WT_PREFIX={prefix}
export FORGE_STAGE_DIR={stage}
export FORGE_SHA={pin}
```

Run this check before you write any Java, and paste its output. If any line reads
FAIL, stop.

```
cd {wt} 2>/dev/null || {{ echo "FAIL no worktree"; exit 1; }}
[ "$(git rev-parse HEAD)" = "$FORGE_SHA" ] && echo "OK   pinned commit" || echo "FAIL wrong commit"
[ "$(find java.generated.project/src -name '*.java' 2>/dev/null | wc -l | tr -d ' ')" = "0" ] && echo "OK   no Java present" || echo "FAIL Java present"
[ "$(grep -m1 active_case_study pipeline.yaml | awk '{{print $2}}')" = "{study}" ] && echo "OK   study is {study}" || echo "FAIL wrong study"
[ "$(ls forge.assets/case-studies)" = "{study}" ] && echo "OK   only this study's inputs" || echo "FAIL other case studies present"
[ ! -e docs/archive ] && echo "OK   no archive" || echo "FAIL docs/archive present"
```

**Cost.** Pass your own token figure as the last argument of the runner if your
session shows one (`/cost`); otherwise pass `unavailable`. Do not estimate.
"""

COLD = """# Single cold pass — {study}, pass {k}

""" + COMMON + """
## Run the pipeline once, then stop

This is a **single cold pass**: you write the Java once, run the pipeline once, and
stop, whatever the result.

1. When the Java, the trace and the summary are written, run, **once**:
   `bash {research}/scripts/run_claude_iteration.sh {study} {k} 1 <tokens|unavailable>`
2. When it finishes, **stop**. Report the line it prints after
   `=== iteration 1 : what to do next ===`, verbatim. Do **not** open any
   `forge.assets/corrections/post_*.md` file, do **not** change the Java, and do
   **not** run the runner again: a pass whose Java changes after the runner starts,
   or that reads a report, is void.

Then finish with the line `ITER_1_COMPLETE: <N> files written`.
"""

REFERENCE = """# Reference run — {study}, run {k}

""" + COMMON + """
## The loop: you drive it yourself

1. Write the Java, the trace and the summary (iteration 1).
2. Run `bash {research}/scripts/run_claude_iteration.sh {study} {k} <iteration> <tokens|unavailable>`.
   Use the runner for **every** iteration; it records each one.
3. If it prints **ALL PHASES PASSED**, stop: the run has converged. Report the
   iteration number.
4. Otherwise read **only** `forge.assets/corrections/post_<phase>.md` for each
   failing phase, change **your Java** (and the trace, if the Java's elements
   changed), update `ITER_SUMMARY.md`, and go to 2 with the next iteration number.
{stoprule}
If a report tells you to change part of the instrument (a template, the extractor,
the rules, `pipeline.yaml`), **stop and report it to the operator verbatim**: that
is a defect of the instrument, and a run that edits the instrument is void. If you
believe a failure is the pipeline's and not your code's, say so in the summary and
work around it in your Java.

End each iteration's chat reply with `ITER_<n>_COMPLETE: <N> files edited`.
"""

COMPILEONLY = (REFERENCE.replace("# Reference run — {study}, run {k}", "# Compile-only run — {study}, run {k}")
               .replace("3. If it prints **ALL PHASES PASSED**, stop: the run has converged. Report the\n   iteration number.",
                        "3. If it prints **ALL VISIBLE PHASES PASSED**, stop: this is the run's stop point.\n   Report the iteration number."))   # Amendment 18
assert COMPILEONLY != REFERENCE and COMPILEONLY.count("ALL VISIBLE PHASES PASSED") == 1

CAP_RULE = """5. **The cap is iteration 7.** At the cap, stop and report non-convergence with the
   failing phases.
"""
# Amendment 13: no cap for Steam Boiler Reference runs 3-5 (run 2 is continued, not regenerated).
NOCAP_RULE = """5. **There is no iteration cap.** Stop when the runner prints ALL PHASES PASSED, or
   when you conclude that the remaining failures cannot be repaired in your Java
   without changing the pipeline; then stop and report non-convergence with the
   failing phases and your reason.
"""
NOCAP_RUNS = {("reference", "steam_boiler", k) for k in (1, 2, 3, 4, 5)}   # Amendment 15: all five

SYSDESC = "- `forge.assets/case-studies/{study}/system/system_description.txt` — the system\n"


def render(cond, study, k):
    coll, prefix, stage = COLL[cond]
    pin = PIN
    if study == "steam_boiler":   # Amendment 15: re-collected at the Amendment 14 pin, new names
        prefix, stage = {"cold": ("forge-v3c16", "/Users/ranwei/Gitee/v3c16-staging"),
                         "reference": ("forge-v3r16", "/Users/ranwei/Gitee/v3r16-staging")}[cond]
        pin = PIN_A14
    wt = f"/Users/ranwei/Gitee/{prefix}-run{k}-{study}"
    sd = "" if study == "steam_boiler" else SYSDESC.format(study=study)
    tpl = {"cold": COLD, "reference": REFERENCE, "compileonly": COMPILEONLY}[cond]
    if cond == "compileonly": pin = PIN_A14   # Amendment 18: the current pin
    return tpl.format(study=study, k=k, unit="pass" if cond == "cold" else "run", sysdesc=sd, wt=wt,
                      coll=coll, prefix=prefix, stage=stage, pin=pin, research=RESEARCH,
                      stoprule=NOCAP_RULE if (cond, study, k) in NOCAP_RUNS else CAP_RULE)


def normalise(text, study, k, cond=None):
    t = text.replace(study, "<S>")
    if cond == "compileonly": t = t.replace(PIN_A14, PIN)   # Amendment 18: the pin, a permitted difference
    t = re.sub(rf"\brun{k}\b", "run<K>", t)
    t = re.sub(rf"\b(pass|run) {k}\b", r"\1 <K>", t)
    t = re.sub(rf"(<S> ){k}( 1| <iteration>)", rf"\1<K>\2", t)
    t = t.replace(SYSDESC.replace("{study}", "<S>"), "")
    t = t.replace(NOCAP_RULE, CAP_RULE)   # Amendment 13: the only permitted difference
    if study == "steam_boiler":   # Amendment 15: names and pin, the only other permitted differences
        t = t.replace(PIN_A14, PIN)
        for new, old in (("forge-v3c16", COLL["cold"][1]), ("forge-v3r16", COLL["reference"][1]),
                         ("/Users/ranwei/Gitee/v3c16-staging", COLL["cold"][2]), ("/Users/ranwei/Gitee/v3r16-staging", COLL["reference"][2])):
            t = t.replace(new, old)
    return t


def main():
    os.makedirs(OUT, exist_ok=True)
    sys.path.insert(0, f"{RESEARCH}/scripts"); import instruction_leak_gate as G
    v = G.vocabulary(); bad = 0; n = 0
    for cond, studies in RUNS.items():
        canon = None
        for study, ks in studies.items():
            for k in ks:
                text = render(cond, study, k); n += 1
                path = os.path.join(OUT, f"v3-{cond}-{study}-run{k}.md")
                open(path, "w").write(text)
                norm = normalise(text, study, k, cond)
                if canon is None: canon = norm
                elif norm != canon:
                    bad += 1; print(f"DIFFERS {path}")
                    print("".join(list(difflib.unified_diff(canon.splitlines(1), norm.splitlines(1)))[:12]))
                for i, line in enumerate(text.splitlines(), 1):
                    # a prompt names its own study; any OTHER study's content is a leak
                    hits = [h for h in G.check_line(line, v) if not h.startswith(study + ":")]
                    if hits: bad += 1; print(f"LEAK {path}:{i}: {hits[:2]} | {line[:100]}")
    print(f"{n} prompts written to {OUT}; {'OK' if bad == 0 else f'{bad} problem(s)'}")
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
