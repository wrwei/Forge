# Single-shot vibe-coding convergence experiment — protocol

This document is the per-case-study playbook used for the LRE, Chemical
Detector, and SRanger experiments (see the five independent runs per
study under [`convergence/<study>/run-1..run-5/`](convergence/) and the
synthesis in
[CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md](convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md)).

The protocol measures **how many iterations of single-shot LLM codegen +
deterministic-pipeline feedback are needed before all three formal
verifications (FDR4, Dafny, Isabelle) pass with non-vacuous obligations**.

It is reproducible per case study and intentionally light: each iteration
is one fresh LLM invocation (no shared session) plus one pipeline run.

---

## Convergence criterion

An iteration *converges* when **all** of the following hold:

1. All twelve deterministic pipeline phases (compile, coverage, preflight,
   t2m, m2m, m2t, dafny_gen, isabelle_gen, fdr4, dafny_verify,
   isabelle_verify, vacuity) report `completed`.
2. The vacuity audit phase (6d — implementation at
   `forge.dashboard/web/vacuity.py`, runs automatically after
   `isabelle_verify`) reports zero findings.
3. FDR4 (deadlock- and divergence-freedom) passes. Determinism is not
   checked — `run_fdr4` strips the official generator's
   `:[deterministic]` assertion before invoking FDR, and
   `pipeline.yaml`'s `phases.fdr4.expected_failures` is `[]`.
4. The Dafny `ghost predicate Valid()` body OR at least one transition
   method's `ensures` clause is non-trivial (the vacuity audit's D1
   signal).
5. The Isabelle zstore `where inv:` clause is non-trivial (the vacuity
   audit's I1 signal).

If any of (1)-(5) fails, that iteration has **not** converged. Proceed to
the next iteration.

---

## One-time setup per case study

Before the first iteration on a new case study:

1. **Author the case-study assets** under
   `forge.assets/case-studies/<study>/`:
   - `system/system_description.txt` — implementation-detail-free prose
     describing the system, modes, sensors, actuators, and operating
     environment. **Must not** mention Java syntax, package layout,
     Dafny/Isabelle/FDR4, codegen rules, or verifier justifications.
   - `requirements/requirement_all.json` — the canonical requirement
     set. Each requirement has `kind`, `name`, `id`, `description`,
     `priority`, `types`. ID prefix conventions are study-specific —
     document them in a sibling `requirements/README.md`.
   - Optional: a `requirements/README.md` describing the ID scheme and
     any tier files.

2. **Verify the spec is implementation-detail-free.** Grep the system
   description and requirement file for: package names, the words
   `Java` / `Dafny` / `Isabelle` / `FDR4` / `RoboChart`, codegen rule
   references, verifier justifications. If any appear, rewrite them out.
   The LLM should see *what* the system does, not *how* the pipeline
   should encode it.

3. **Set the active case study** in `pipeline.yaml`:
   ```yaml
   agent:
     active_case_study: <study>
   ```

4. **Confirm pipeline-config invariants are at baseline.** No
   case-study should require changes to:
   - `pipeline.yaml`'s `phases.fdr4.expected_failures` (must remain
     `[]` — determinism is stripped by `run_fdr4`, not tolerated as an
     expected failure).
   - The M2M / M2T templates under
     `forge.transformations/src/main/resources/transformations/*`.
   - The codegen prompts under `forge.assets/prompts/*`.
   - The vacuity audit `forge.dashboard/web/vacuity.py` (pipeline phase 6d).

   If any of these need a change, that is **infrastructure work**, not
   case-study work. Make and commit the change on `main` (or a feature
   branch) *before* starting the case-study experiment, so all
   iterations share the same infrastructure version.

5. **Record the infrastructure version.** In the experiment doc,
   capture the commit SHA of the repo at the start of iter 1. All
   iterations of one case study must share this SHA — if the
   infrastructure changes mid-experiment, you must restart from iter 1
   on the new SHA or document the change as a methodological caveat.

---

## Per-iteration steps

### Step 1 — Spawn a fresh codegen agent

The protocol requires **single-shot** invocation: each iteration is a
fresh LLM subprocess with no memory of prior iterations. The LLM's only
differentiator across iterations is the contents of the prompt bundle.

Spawn an Agent (subagent_type: `general-purpose`) with a prompt that
includes:

1. **The case-study spec** (absolute paths):
   - `forge.assets/case-studies/<study>/system/system_description.txt`
   - `forge.assets/case-studies/<study>/requirements/requirement_all.json`

2. **The codegen prompt assets**:
   - `forge.assets/prompts/java_codegen_rules.txt`
   - `forge.assets/prompts/chain_of_thought_codegen.txt`
   - `forge.assets/prompts/few_shot_codegen.txt`

3. **Project-level conventions**:
   - `CLAUDE.md` (especially the "Naming conventions consumed by the
     M2M (ETL)" table and the τ-self-loop guidance under "Generated
     RoboChart structure")

4. **From iter 2 onward** — the prior iteration's feedback:
   - `forge.assets/corrections/post_*.md` for any phase that failed
     (do **not** include `post_codegen.md` — that's authored by Claude
     in interactive sessions and not part of this protocol)
   - The current Java sources under
     `java.generated.project/src/main/java/<study>/`

5. **Hard constraints** in the prompt (reproduced verbatim each
   iteration to enforce the methodological guard rails):
   - Do NOT touch the M2M/M2T templates (`*.etl`, `*.egl`). The
     experiment measures convergence of *Java codegen*, not of the
     transformation chain.
   - Do NOT touch `pipeline.yaml` or any `expected_failures` list.
     Verifier outcomes must be earned, not configured away.
   - Do NOT touch the case-study requirements (immutable input).
   - Do NOT modify other case studies or the experiment script.
   - Do NOT consult other Java sources outside
     `java.generated.project/src/main/java/<study>/`.
   - Do NOT run the pipeline (the orchestrator does that).
   - Do NOT commit (the orchestrator does that).
   - Do NOT edit the experiment doc.

6. **Deliverable instruction**: the agent edits Java sources in place
   and returns a ≤300-word summary of what changed and why.

### Step 2 — Run the pipeline

```bash
python experiments/scripts/run_experiment_iteration.py
```

This runs all eleven phases in order plus the vacuity audit, and writes
`forge.assets/corrections/post_*.md` after each phase.

### Step 3 — Inspect the outcome

Read the relevant `post_*.md` files for any phase that failed, plus
`post_vacuity.md` regardless. Check:

- Did the change close the iter (N-1) failures?
- Did it introduce new failures (regression)?
- Is the vacuity audit clean?
- Are the Dafny `ensures` clauses non-trivial (inspect
  `forge.transformations/output/<Stm>.dfy`)?
- Is the Isabelle inv non-trivial (grep `inv:` in
  `forge.transformations/output/isabelle/<Stm>_Beh.thy`)?

### Step 4 — Update the experiment record

Append a section to the run's `convergence/<study>/run-<N>/trajectory.md`:
- "LLM output summary" — what the agent changed and why (from the
  agent's report)
- "Pipeline phase outcomes (iter N)" — table of phase / status / time
  / notes
- "Iter (N+1) input" — what the next iteration's feedback will be

### Step 5 — Decide: converge or iterate

If the convergence criterion is met, go to "On convergence" below.

Otherwise, return to Step 1 for iteration N+1.

### Stop criteria for non-convergence

If the LLM stops making meaningful progress (two iterations in a row
with no diff that closes any failure, or token usage growing while
verifier outcomes are static), **pause** and inspect manually. This is
the "judgment call" point the manual experiments preserved. Possible
remedies:

- The infrastructure has a bug (M2M classifier, M2T template, vacuity
  audit signal) that the LLM cannot work around. Fix it on `main`,
  restart from iter 1, document as a methodological caveat in the
  experiment doc.
- The prompt bundle is incomplete (a relevant codegen rule or CLAUDE.md
  convention isn't being surfaced). Update the prompt assets, restart
  from iter 1, document.
- The case study itself is genuinely too hard for the current
  pipeline. Document the wall and the failure shape; the experiment's
  outcome is "did not converge within K iterations" with the residual
  failures recorded.

---

## On convergence

After the iteration that satisfies the convergence criterion:

1. **Create a snapshot branch** preserving the converged Java source
   (which is gitignored on `main`):
   ```bash
   git checkout -b <study>-iter<N>-converged
   git add -f java.generated.project/src/main/java/<study>/ \
              java.generated.project/result_codegen.json
   git commit -m "snapshot: <study> iter-<N> converged state"
   git checkout main
   ```
   This matches the per-run snapshot pattern (one converged-state
   branch per run).

2. **Finalise the as-executed experiment record**
   (`convergence/<study>/run-<N>/trajectory.md`):
   - Convergence trajectory table (one row per iteration with
     FDR4 / Dafny / Isabelle / Vacuity columns and a brief "notes"
     column)
   - Comparative cost row against the other case studies
   - Any semantic caveats specific to the iter-N artefact (e.g.
     SRanger hiding the wall-clock semantics behind the
     `TurnTimer` Operation)

3. **Fold the run into the cross-run synthesis**
   in [CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md](convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md),
   keeping a paper-ready framing:
   - Setup (case study + protocol pointer)
   - Iteration trajectory table — *no* "problems / fixes" framing,
     just "Feedback received / LLM-driven change / Verifier outcome"
   - Convergence summary — the sequence of design decisions
   - Cost table (tokens / tool calls / LLM wall-clock / pipeline
     wall-clock per iteration where measured)
   - Final state — the post-convergence verification table
   - Comparison row against the other case studies

   The paper-ready synthesis must not mention bugs, workarounds, or
   infrastructure fixes — those are recorded in the per-run
   trajectory, not the synthesis.

---

## Methodological guard rails (the things that have bitten us)

These are the surfaces where prior experiments have introduced
methodologically unsound shortcuts. They are repeated in the per-iter
codegen prompt; they also need monitoring at the orchestrator level.

1. **Vacuity check is non-negotiable.** A "passing" verifier with a
   trivial obligation (`Valid() ≡ true`, `inv: True`) is not
   convergence. The vacuity audit catches the two specific signals; if
   it flags, the iteration has not converged regardless of phase
   statuses. The audit is also weak — it only checks D1 and I1 — so
   spot-check the generated Dafny and Isabelle artefacts manually after
   apparent convergence.

2. **No `expected_failures` patches.** If a verifier reports failures,
   the response is "change the Java to close them" — never "add the
   failure to the expected list". An early chemdetector trajectory
   included a stretch where we did this, then reverted it on
   methodological grounds and re-converged via the M2M boundary-STM
   fix.

3. **Requirements are immutable.** The LLM does not edit
   `forge.assets/case-studies/<study>/requirements/*`. If the LLM
   reports the requirements as ambiguous or wrong, that's a
   `post_codegen.md` issue for human review, not a license to rewrite
   the spec.

4. **Infrastructure changes apply symmetrically.** When the M2M or M2T
   templates need a fix (e.g. the chemdetector boundary-STM emission
   change), that change applies to *every* case study from
   that point forward. Re-run prior case studies' converged sources
   against the new infrastructure to confirm they still converge — or
   document the regression and restart the affected experiment.

5. **The codegen agent is single-shot.** No shared session, no
   conversation history across iterations. The dashboard's Claude chat
   is **not** suitable for this protocol because it resumes a persistent
   session — use `Agent` subagent spawns or a `claude -p` headless
   subprocess instead.

6. **The orchestrator does the pipeline run and the commit, not the
   agent.** The agent only edits Java. This keeps the agent's
   surface area small and prevents it from masking failures by
   selectively running phases.

---

## Interpreting convergence cost across case studies

Iteration count does **not** track case-study size (requirement count,
mode count, transition count, LOC). Across the five runs per study, all
fifteen runs converged with a median of 2 iterations (range 1-3). The
LRE/Chemical Detector/SRanger size metrics illustrate the lack of
correlation:

| Case study | Requirements | Modes | Transitions |
|---|---:|---:|---:|
| LRE               | 51 | 4  | 18 |
| Chemical Detector | 81 | 13 | 30 |
| SRanger           | 23 | 3  | 7  |

SRanger is the smallest by every size metric yet does not converge in
systematically fewer iterations. What predicts iteration count is
**feedback specificity** — the granularity at which the verifier's
output names the defect:

- **Verifier-output specificity** (cheap to converge): the failure
  comes out of FDR4 / Dafny / Isabelle with a named transition, named
  postcondition, or concrete state-space number. The LLM gets a
  precise, actionable signal with a clean local fix. LRE's
  trajectories were typically this shape (divergence chains naming
  transitions, Dafny postcondition failures naming branches, state-space
  size as a concrete number) — converging quickly.

- **Intermediate-pipeline specificity** (expensive to converge): the
  failure originates inside the M2M / M2T transformation chain (a
  classifier mis-classification, a template emission corner case) and
  surfaces as moving downstream symptoms across iterations. The LLM
  sees one symptom at a time and tends to apply local fixes that don't
  address the root cause; only after the same shape recurs across two
  or three iterations does the LLM read cumulatively and pivot to a
  structural refactor. SRanger's harder runs were this shape: one
  underlying M2M classifier interaction, several surface signatures,
  rounds of failed local renames, and finally an Operation-class
  refactor.

Two practical consequences for the protocol:

1. **Expect dispersion in convergence cost** that is not correlated
   with case-study size. When a new case study takes longer than its
   size suggests, that is usually informative about the
   transformation chain rather than about the LLM's competence.

2. **The codegen prompt must include all prior iterations' feedback,
   not just the most recent.** This is what allows the LLM to
   recognise a recurring shape and restructure rather than rename.
   The protocol bundles `forge.assets/corrections/post_*.md` plus
   the current Java source each iteration, but the agent should be
   explicitly reminded (in the iter prompt from iter 3 onward) to
   read the trajectory's prior-iter sections if the same failure
   shape recurs. The harder SRanger prompts did this and worked.

See [CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md](convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md)
on "iteration count does not track case-study size" for the worked example.

---

## Adding a fourth case study

1. Source the case study from a published reference (RoboChart /
   RoboStar / textbook) rather than designing it yourself — this
   addresses the "cherry-picked case studies" reviewer concern.
2. Run the one-time setup (above).
3. Run iterations until convergence or the documented stop criterion.
4. Record the run under `convergence/<study>/run-<N>/trajectory.md` and
   update the synthesis in
   `convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md`.

---

## Known limitations of the evaluation as conducted

The LRE / Chemical Detector / SRanger experiments documented above
established the protocol and produced the convergence numbers. Two
methodological limitations were identified; both have since been
addressed by additional runs. Recording them here, with their
remediations, so the paper can describe them as resolved caveats.

### Limitation (a) — single trajectory per case study (addressed by re-runs)

The original write-ups iterated each case study to convergence only
once, which (because the LLM is non-deterministic — each `claude -p`
invocation is a fresh sample from a distribution conditioned on the
prompt) gave observations rather than means. A reviewer could fairly
ask whether a single reported count was typical or an outlier.

**Remediation (executed).** Each case study was re-run K=5 times from
cold using the same protocol — same case-study spec, same prompts, same
infrastructure version, different LLM samples. All five runs per study
converged (15/15), with a median of 2 iterations and a range of 1-3.
The per-run trajectories live under
`convergence/<study>/run-1..run-5/trajectory.md`; the distribution is
synthesised in
[CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md](convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md).
This converts the iteration counts from single observations into a
small per-study sample.

**Note.** Setting LLM temperature to 0 reduces but does not eliminate
non-determinism (kernel-level variation, tokenisation, hardware
differences); it is **not** a substitute for re-runs.

### Limitation (b) — no baseline comparison

The experiments demonstrate that the iteration loop **converges**. They
do not demonstrate that the iteration loop is **necessary** — i.e. that
the formal-method-guided feedback is doing work that simpler
alternatives (no feedback at all, or only compile-error feedback) would
not also do. Without a baseline, a convergence-iteration count lacks a
comparison point against which it is meaningful.

**Two baselines we can run within the existing protocol.**

1. **No-feedback / cold baseline.** Run the codegen agent K times with
   the standard prompt bundle *except* the prior-iteration feedback and
   the existing Java source. Each cold run is a single one-shot
   generation: spawn a fresh codegen agent → run the pipeline → record
   which phases pass → do **not** iterate. Compare against the
   iteration-1 results of the normal protocol (which are also cold).
   If most cold runs converge — i.e. pass all eleven phases plus the
   vacuity audit on the first try — then the iteration loop is
   demonstrably *not* doing work, and the case study isn't a good test
   bed. If most cold runs fail across multiple phases, then the
   iteration loop's value is the convergence work it does on top of
   what cold codegen can already achieve.

2. **Compile-only feedback baseline.** Same as the normal protocol,
   except each iteration's prompt bundle includes only `post_compile.md`
   (and not the formal-verification feedback `post_fdr4.md` /
   `post_dafny_verify.md` / `post_isabelle_verify.md`). This isolates
   the *formal-method-specific* value-add from generic LLM coding
   loops. If a compile-only loop reaches the same convergence as the
   full-feedback loop, the formal-verification feedback is not doing
   work the LLM couldn't do from compile errors alone.

**Cost.** Each cold baseline run is one codegen call + one pipeline run
(~5-30 min agent + ~3 min pipeline). K=5 cold runs per case study =
~5 hours total at K=5, ~1 hour at K=3. The compile-only baseline is
proportionally bigger because each is a full iteration trajectory.

**Recommendation.** Do the cold baseline first, on all three case
studies, because it is cheap and the most directly informative for the
"is the loop doing work?" question. Defer compile-only baseline unless
the cold baseline shows non-trivial cold convergence rates that warrant
the deeper isolation.

#### Cold baseline results (executed 2026-05-22 / 2026-05-23)

Ran K=10 cold codegen + pipeline passes for each of LRE, Chemical
Detector, and SRanger. Per-run artefacts under
[`cold-baseline/`](cold-baseline/); aggregate in
[`cold-baseline/README.md`](cold-baseline/README.md); per-study breakdowns in
each study's `results-summary.md`.

| Study | Runs | Compiled | Converged cold |
|---|---:|---:|---:|
| LRE               | 10 | 9  | **0** |
| Chemical Detector | 10 | 0  | **0** |
| SRanger           | 10 | 10 | **0** |
| **Total**         | **30** | **19** | **0** |

**Result: 0 of 30 cold runs converged.** The iteration loop is doing
real work — without it, no case study reaches full verification on a
single shot.

Per-study failure shapes from the per-phase tables:

- **LRE**: 9/10 cold codegens compile; the pipeline reaches FDR4 on
  10/10 and Dafny verify on 10/10; Dafny verify *fails* on 10/10
  (the framing pattern documented in the LRE iter-3 transition is not
  found cold). FDR4 passes on 8/10 cold runs — the deadlock-freedom
  fixes the LRE main experiment needed across iter-1 → iter-2 are
  hit-or-miss in the cold sample.
- **Chemical Detector**: 0/10 cold codegens compile. The two-
  controller architecture, typed event payloads, and shared-event
  routing produce Java that the compiler rejects on every cold run —
  every later phase is therefore vacuously "failed" because the input
  artefact never makes it past phase 2a.
- **SRanger**: 10/10 compile; the pipeline reaches every phase; FDR4
  passes on 10/10; Dafny verify fails on 10/10 (Turning postcondition
  framing not in cold output); Isabelle verify times out on 10/10
  (the `clock` reserved-word collision is in every cold run, causing
  the Isabelle build to spin past the 7-minute safety timeout).

The breakdown is itself informative for the paper: **different case
studies fail at different pipeline phases under cold codegen**, which
is what makes the iteration-loop's value-add visible. A reviewer asking
"is the formal-method feedback specifically necessary, or would a
basic compile-error loop suffice?" can be answered by pointing at
LRE/SRanger (where the cold output compiles fine — the value-add is
purely in the formal-verification phases) versus Chemical Detector
(where even compile fails — the loop reaches into Java-language
shaping). The same loop architecture handles both.

**Storage layout for baseline experiments.** Each cold run produces a
full Java source set + a phase-outcome record. Unlike the main
experiment's converged sources (gitignored on `main`, preserved on
`<study>-iterN-converged` branches), the baseline runs are *small* (no
iteration history needed) and *should be inspectable side-by-side* — so
they live directly under `experiments/cold-baseline/` on `main`:

```
experiments/cold-baseline/
├── README.md                                # protocol + aggregate results
├── <study>/
│   ├── results-summary.md                   # K rows: per-run pass/fail + convergence
│   ├── run-1/
│   │   ├── src/                             # cold codegen output (full Java tree)
│   │   ├── agent-summary.md                 # LLM's own report from this run
│   │   ├── pipeline-results.md              # per-phase status + timing
│   │   └── post_*.md                        # copies of corrections artefacts
│   ├── run-2/
│   │   └── ... (same shape)
│   └── run-K/
└── compile-only/                            # if/when the second baseline runs
    └── ... (same shape, per case study + per run)
```

This makes the baseline experiments fully version-controlled, diffable
across runs, and reviewer-inspectable without checking out branches.
Each run's `pipeline-results.md` records: which phases passed, which
failed, which `post_*.md` was produced, and whether the vacuity audit
fired — enough for the per-run pass/fail count to be reproduced from
the stored artefacts alone.

### Domain limitation

All three case studies are reactive safety controllers in the broad
robotics domain (AUV / mobile robot / ground robot). This is
intentional for a paper about safety-critical systems and matches
RoboStar's focus. The narrower concern is that all three exercise the
same *pipeline shape*: discrete mode state machine + sensor +
actuator + single-cycle reactive control. A case study outside this
shape (process control, medical device, communication protocol) would
test whether the convergence story generalises. Strong candidates from
the formal-methods literature that fit the safety-critical frame but
are *not* RoboStar-style robotics:

- **Boston Scientific Pacemaker Challenge (2007)** — medical, mode-
  based state machine.
- **Steam Boiler Specification** (Abrial, 1996) — process control.
- **Mine Pump Controller** (Spivey, Joseph) — process control.

None of these are in the Isabelle reference corpus, so they avoid the
"LLM has seen the converged Isabelle artefact" leak that would arise
from picking `DwarfSignal` or `Incubator` (which are in the corpus as
reference theories).

---

## Pointers

- **As-executed records (what actually happened, including failures and
  caveats):** the per-run trajectories under
  [`convergence/<study>/run-1..run-5/trajectory.md`](convergence/).
- **Synthesis across the five runs per study (15 runs total):**
  [CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md](convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md).
- **Pipeline reference:** [CLAUDE.md](../CLAUDE.md) (snapshot kept in sync with repo-root `../CLAUDE.md`).
- **Vacuity audit:** [../forge.dashboard/web/vacuity.py](../forge.dashboard/web/vacuity.py) (pipeline phase 6d).
- **Iteration driver:**
  [scripts/run_experiment_iteration.py](scripts/run_experiment_iteration.py).
