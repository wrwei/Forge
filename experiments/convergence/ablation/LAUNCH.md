# Launch one Compile-Only Ablation run

This is the **run doc** for a single compile-only ablation trajectory. It is
self-contained for a fresh session. The ablation is identical to an honest
independent convergence run as defined in
[`../../RUN_TRAJECTORY.md`](../../RUN_TRAJECTORY.md) **except for one rule**
(the feedback restriction in §B below). Follow `RUN_TRAJECTORY.md` for
everything it covers — independence (§1), setup (§2), the iteration loop
mechanics (§3), forbidden reads (§4) — and apply the overrides here.

## A. Parameters the human supplies in the invocation

- **`<study>`** — `lre` | `chemical_detector` | `sranger`.
- **`<package>`** — Java package dir (domain-derived; e.g. `chemdetector`).
- **`<N>`** — the ablation run number to archive into
  (`experiments/convergence/ablation/<study>/run-<N>/`). Given by the human.
- **condition** — fixed: `compile-only-ablation`.

## B. THE ONE OVERRIDE — restricted feedback

During the iteration loop you may read and act on **only the eight
compile-and-extraction feedback files**:

```
post_compile  post_coverage  post_preflight
post_t2m  post_m2m  post_m2t  post_dafny_gen  post_isabelle_gen
```

The **four behavioural-verifier feedback files are a forbidden answer key**
for this run — do **not** open them while iterating, in any iteration:

```
post_fdr4  post_dafny_verify  post_isabelle_verify  post_vacuity
```

(Add these four to the `RUN_TRAJECTORY.md` §4 forbidden-reads list for the
duration of this run. Also forbidden, as usual: anything under `docs/`, any
`run-*`/`iter-*` of any study, the findings knowledge-base, and **this
directory's `README.md`** — it quotes full-loop outcomes.)

**Stop rule.** STOP the loop at the first iteration where all **eight**
visible phases are `passed`. That is the *compile-only stop point*. Do not
iterate further even though the run may not have reached 12/12 — the whole
point is to measure what the verifiers would have said at this point without
having used them as feedback.

Everything else is unchanged: iter-1 is cold codegen from the canonical
inputs only; iter N≥2 makes the *minimal* fix the visible feedback asks for;
run `run_experiment_iteration.py` then `snapshot_iter.py` every iteration
(the runner still executes all 12 phases and writes all `post_*` files — you
simply must not read the four verifier files).

**Decide the stop condition without seeing the verifiers.** After each
snapshot, run:

```bash
python experiments/convergence/ablation/scripts/check_visible_phases.py
```

It prints only the eight visible phases' statuses and exits 0 when all are
green. Read the eight VISIBLE `post_*.md`/`.json` for actionable feedback.
**Do NOT open** `post_fdr4/_dafny_verify/_isabelle_verify/_vacuity`, and
**do NOT open `summary.json` during the loop** — `snapshot_iter.py` records
all twelve phase results there, including the withheld verifiers. The helper
exists so you never need to.

## C. Setup (same as RUN_TRAJECTORY §2)

1. `pipeline.yaml` `agent.active_case_study: <study>`.
2. FDR4 type ranges `[0..1]` (`forge.dashboard/corrections/type_ranges.json`).
3. FDR4 kill policy: phase `fdr4` `timeout: 3600`, `memory_limit_mb:` = your
   page-file MB. (Local tuning; do not commit.)
4. Wipe the package:
   ```bash
   rm -rf  java.generated.project/src/main/java/<package>
   mkdir -p java.generated.project/src/main/java/<package>
   ```

## D. On stop — archive + record

1. Move the iteration snapshots into the ablation run dir:
   `experiments/convergence/ablation/<study>/run-<N>/iter-*/`
   (same layout the main runs use).
2. Write `trajectory.md` (what each visible-feedback iteration fixed).
3. Emit the measurement deterministically:
   ```bash
   python experiments/convergence/ablation/scripts/record_ablation_result.py <study> <N>
   ```
   This reads the archived `iter-*/summary.json`, confirms the eight visible
   phases are green at the stop point, extracts the four withheld verifiers'
   outcomes, and writes `ablation_result.json` (+ refreshes `RESULTS.md`).
4. In your final message report: `compile_only_iters`, the four verifiers'
   pass/fail at the stop point, and `ships_broken` (true iff any verifier
   failed). Do **not** then "fix" the verifier failures — the run ends here.

## E. Independence reminder

One run per session. This session must have no prior knowledge of `<study>`'s
trajectory and must not have driven another run. If study-specific prior-run
content is already in your context (auto-memory, `.remember` hook), say so
explicitly in your first message and in `trajectory.md` so the run can be
judged or re-done. No sub-agents.
