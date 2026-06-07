# Compile-Only Feedback Ablation

> **For humans / analysis only — NOT a canonical input for a running session.**
> This file quotes full-loop run outcomes (which runs ship broken) as
> motivation; that is answer-key material under the independence rules. A
> session *driving* an ablation run must follow [`LAUNCH.md`](LAUNCH.md)
> (the clean run doc) and treat this README as a forbidden read for the
> duration of the run, exactly like the findings knowledge-base.

This directory holds the **compile-only ablation** of the convergence
experiment. Its purpose is to isolate the causal contribution of the
**formal-verifier feedback** to convergence, separating it from the
contribution of the ordinary compile/lint feedback — i.e. to retire
threat-to-validity (d) ("we did not isolate verifier feedback from
compile-error feedback") with a controlled measurement rather than a
retrospective argument.

## Why this experiment

A retrospective log-mining of the 15 canonical full-loop trajectories
(`../<study>/run-4..8`, with chemical_detector's 5th = `run-9`) shows:

| outcome a *compile-only* loop would have produced | runs |
|---|---|
| **A. ships verification-failing code** (iter-1 already compiled + passed the extraction lint, yet a verifier failed) | **4 / 15** — `sranger-4`, `sranger-6`, `chem-4`, `chem-6` |
| **B. compile-only fix suffices** (verifiers pass once the lint is green) | **1 / 15** — `chem-9` |
| **C. indeterminate** (iter-1 bundled a lint fix *and* a verifier fix, so the logs cannot adjudicate) | **10 / 15** |

So the logs *prove* the loop did necessary work in 4 runs and *disprove*
it in 1, but leave **10 / 15 undecided**. This ablation decides them by
construction: it re-runs each trajectory with the verifier feedback
**withheld**, and measures whether the artefact a compile-only loop stops
at is verification-clean.

(Also from the logs: 17 of the 18 inter-iteration transitions across the
15 trajectories involved at least one behavioural-verifier failure; the
sole exception is `chem-9`'s lint-only first iteration.)

## The single controlled variable

Everything is held identical to a condition-B full-loop run as defined in
[`../../RUN_TRAJECTORY.md`](../../RUN_TRAJECTORY.md) — same canonical
inputs, same model (Claude Opus 4.8), same `CLAUDE.md`, same isolation
profile (§1 + Appendix there), same no-sub-agents rule, same FDR4 `[0..1]`
type ranges and kill policy (§2) — **except the feedback signal**:

> **During the iteration loop the developer may read and act on ONLY the
> compile-and-extraction feedback files** — the eight phases up to and
> including artefact generation: `post_compile.*`, `post_coverage.*`,
> `post_preflight.*`, `post_t2m.*`, `post_m2m.*`, `post_m2t.*`,
> `post_dafny_gen.*`, `post_isabelle_gen.*`.
> **The four behavioural verifiers are the withheld answer key:**
> `post_fdr4.*`, `post_dafny_verify.*`, `post_isabelle_verify.*`,
> `post_vacuity.*` MUST NOT be opened while iterating.

This is the **generous null model**: the compile-only loop is allowed not
just to compile and pass the structural lint but to extract a complete
model and generate every formal artefact (Dafny, CSP, Isabelle theory) —
it stops only short of *checking* those artefacts. The measured delta
therefore lands entirely on **behavioural verification** (deadlock/divergence,
design-by-contract, structural invariants + vacuity), which is exactly the
contribution we want to attribute. (Empirically the choice barely matters:
across the 32 logged full-loop iterations no extraction phase ever failed
once `preflight` was green, so "stop at extraction-clean" and "stop at
lint-clean" almost always coincide — but the generous form is the more
defensible null model and removes any objection that an extraction failure,
not a verification failure, is what the loop fixed.)

The pipeline still runs all 12 phases every iteration (the runner is
unchanged — do **not** modify it); the discipline is purely on *which
`post_*` files the developer reads*. This keeps the runner identical to
the main experiment and makes the only difference the information the code
generator is allowed to use.

### What this isolates — and the role of the standing guidance

- **Isolates:** the *feedback signal* — does seeing verifier failures
  change the converged artefact, given the method as designed? This is
  threat (d).
- **Held constant (NOT stripped):** the generic codegen conventions in
  `CLAUDE.md`, including the accumulated *structural* rules (no `Final`
  state on the theory-generated controller, bare-precondition cover, total
  guard cover, Tick/τ self-loop guidance). These are **domain-independent
  design rules** — crucial findings distilled across many experiments and a
  fixed part of the method, on the same footing as the banned-language list
  — **not** per-case fixes: they name no study and no class (`CLAUDE.md` is
  "intentionally domain-independent"). A confound is something that *varies
  with the treatment*; these rules are held *identical* in both the
  compile-only and full-loop conditions, so they cannot explain any
  difference between them. We therefore deliberately do **not** strip them
  — a "FORGE minus its own conventions" configuration is not one anyone
  would deploy, so its convergence number would answer no real question.
- **One honest consequence for the magnitude (not the validity).** Because
  those generic conventions let the generator avoid some deadlock-freedom
  (Isabelle/FDR4) defects *prospectively*, at cold-codegen time, the delta
  this ablation measures is a **conservative lower bound** on the verifier
  loop's value, and is expected to be **Dafny-dominated** —
  design-by-contract is the one withheld class `CLAUDE.md` gives no baked
  answer for. The retrospective decoupling already points this way (all
  four unambiguous ships-broken runs failed `dafny_verify`). The
  preemption is in any case *leaky*: full-loop runs still hit Isabelle
  failures (`chem-6`, `lre-6`) despite the no-`Final` rule being present,
  so a structural delta can still surface.

## Procedure (per run)

1. **Setup** exactly as `RUN_TRAJECTORY.md` §2 (active_case_study, `[0..1]`
   ranges, FDR4 kill policy, wipe the study package). One run per session;
   honour the isolation profile.
2. **Iter 1 — cold codegen** from the canonical inputs only (§1 there).
3. **Run the pipeline** (`run_experiment_iteration.py`) and **snapshot**
   (`snapshot_iter.py <study> <iter> --actor me-as-developer`). All 12
   phases execute and all `post_*` files are written as usual.
4. **Read ONLY the eight compile-and-extraction feedback files**
   (`post_compile/_coverage/_preflight/_t2m/_m2m/_m2t/_dafny_gen/_isabelle_gen`).
   If all eight are `passed` → **STOP** (this is the compile-only stop
   point). Otherwise make the *minimal* fix they ask for and go to step 3.
   Do **not** open the four behavioural-verifier `post_*` files.
5. **At the stop point**, record the measurement: the full `phase_results`
   of the stopping iteration (already in its `summary.json`) — in
   particular the behavioural verifiers `{fdr4, dafny_verify,
   isabelle_verify, vacuity}`. These were computed by the runner but were
   not visible to the generator during the loop.

## Layout

```
ablation/
  README.md                 <- this file
  <study>/run-<N>/
    iter-1/ ... iter-k/      <- same snapshot format as the main runs
    trajectory.md
    ablation_result.json     <- see schema below
```

`ablation_result.json` (one per run):

```json
{
  "study": "lre",
  "run": 1,
  "condition": "compile-only-ablation",
  "compile_only_iters": 1,
  "stop_phase_results": { "...": "passed|failed" },
  "verifiers_at_stop": {
    "fdr4": "passed|failed",
    "dafny_verify": "passed|failed",
    "isabelle_verify": "passed|failed",
    "vacuity": "passed|failed"
  },
  "ships_broken": true,
  "full_loop_baseline": { "run": 4, "iters_to_converge": 2 },
  "notes": ""
}
```

## Plan

5 runs per study (15 total), to match the full-loop set and the paper's
presented sample. The headline comparison is per study and pooled:

> *fraction of compile-only stop points that are verification-clean* (this
> condition) **vs** *1.0* (every full-loop run reaches 12/12 by
> definition).

The complement — *fraction shipping a verification-failing artefact* — is
the marginal value of the verifier feedback loop, and the resolution of the
10 indeterminate runs above.
