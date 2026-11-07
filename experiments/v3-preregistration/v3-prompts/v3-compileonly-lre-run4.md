# Compile-only run — lre, run 4

You are an independent developer implementing a reactive safety controller in
Java from a specification. A downstream toolchain extracts a formal model from your
Java and verifies it.

This session drives **exactly one run** — this one — and nothing else. If
anything earlier in this conversation concerned another run of this or any other
system, **stop and tell the operator**: the run is contaminated and must be
started in a new session.

## Read exactly these

- `forge.assets/case-studies/lre/system/system_description.txt` — the system
- `forge.assets/case-studies/lre/requirements/requirement_all.json` — the requirements
- `forge.assets/case-studies/lre/requirements/README.md` — the requirement-ID conventions
- `CLAUDE.md` (repository root) and the files it names under `forge.assets/prompts/`
- the five layer prompts in `forge.assets/vibe-coding-prompts/`

You may also read `docs/corpus/` (the Z-Machine framework and generic examples)
if you need background on Isabelle. Nothing else under `docs/`.

## Forbidden — a violation voids this run

Reading any of these voids the run; the transcript and session log are audited.

- `forge.transformations/` and `forge.dashboard/` — the source of the phases that
  judge your Java. Write the Java from the requirements and the rules, not from the
  checkers. If you believe you cannot proceed without knowing what a phase does
  internally, **stop and report that to the operator**.
- `experiments/`, `reference-runs/`, and `docs/` outside `docs/corpus/`;
- any other folder under `forge.assets/case-studies/` (setup removes them);
- the source of the scripts you run, other worktrees, the staging directory
  `/Users/ranwei/Gitee/v3o18-staging/`, and the git history (`git log`, `git show`, `git diff`).

## Hard constraints

- Do not change anything under `forge.transformations/`, `forge.dashboard/`,
  `forge.assets/prompts/`, `forge.assets/vibe-coding-prompts/`,
  `forge.assets/case-studies/`, `pipeline.yaml` or `CLAUDE.md`. These are the
  instrument and the input, not your code.
- Write Java only under `java.generated.project/src/main/java/lre/`, plus the
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
/Users/ranwei/Gitee/forge-v3o18-run4-lre
```

`cd` there first. If it does not exist, **stop and say so**; no other worktree is a
substitute. Export these four variables in your shell before running the runner:

```
export FORGE_ARM_DIR=/Users/ranwei/Gitee/formal_method_guided_vibe_coding/experiments/v3-compile-only
export FORGE_WT_PREFIX=forge-v3o18
export FORGE_STAGE_DIR=/Users/ranwei/Gitee/v3o18-staging
export FORGE_SHA=aaddf2225b38eb1eb749e816f6f5874d7ec93eaa
```

Run this check before you write any Java, and paste its output. If any line reads
FAIL, stop.

```
cd /Users/ranwei/Gitee/forge-v3o18-run4-lre 2>/dev/null || { echo "FAIL no worktree"; exit 1; }
[ "$(git rev-parse HEAD)" = "$FORGE_SHA" ] && echo "OK   pinned commit" || echo "FAIL wrong commit"
[ "$(find java.generated.project/src -name '*.java' 2>/dev/null | wc -l | tr -d ' ')" = "0" ] && echo "OK   no Java present" || echo "FAIL Java present"
[ "$(grep -m1 active_case_study pipeline.yaml | awk '{print $2}')" = "lre" ] && echo "OK   study is lre" || echo "FAIL wrong study"
[ "$(ls forge.assets/case-studies)" = "lre" ] && echo "OK   only this study's inputs" || echo "FAIL other case studies present"
[ ! -e docs/archive ] && echo "OK   no archive" || echo "FAIL docs/archive present"
```

**Cost.** Pass your own token figure as the last argument of the runner if your
session shows one (`/cost`); otherwise pass `unavailable`. Do not estimate.

## The loop: you drive it yourself

1. Write the Java, the trace and the summary (iteration 1).
2. Run `bash /Users/ranwei/Gitee/formal_method_guided_vibe_coding/scripts/run_claude_iteration.sh lre 4 <iteration> <tokens|unavailable>`.
   Use the runner for **every** iteration; it records each one.
3. If it prints **ALL VISIBLE PHASES PASSED**, stop: this is the run's stop point.
   Report the iteration number.
4. Otherwise read **only** `forge.assets/corrections/post_<phase>.md` for each
   failing phase, change **your Java** (and the trace, if the Java's elements
   changed), update `ITER_SUMMARY.md`, and go to 2 with the next iteration number.
5. **The cap is iteration 7.** At the cap, stop and report non-convergence with the
   failing phases.

If a report tells you to change part of the instrument (a template, the extractor,
the rules, `pipeline.yaml`), **stop and report it to the operator verbatim**: that
is a defect of the instrument, and a run that edits the instrument is void. If you
believe a failure is the pipeline's and not your code's, say so in the summary and
work around it in your Java.

End each iteration's chat reply with `ITER_<n>_COMPLETE: <N> files edited`.
