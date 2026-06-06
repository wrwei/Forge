# Run an Independent Convergence Trajectory

This is the **single instruction a fresh session reads to drive one convergence
trajectory**. The intended workflow:

> In a brand-new Claude Code session, the human says:
> *"Read `experiments/RUN_TRAJECTORY.md` and run an independent convergence
> trajectory for `<study>` as `run-<N>`, condition `<B|C>`."*
> The session then follows this document end-to-end.

This document is deliberately **answer-free**: it contains the *process*, not
any case-study fixes, expected iteration counts, or known quirks. Reading it
does not contaminate the run. (Do not add the answer-bearing material listed in §4
to a session driven by this file.)

---

## 0. Parameters the human supplies in the invocation

- **`<study>`** — one of `lre` | `chemical_detector` | `sranger`.
- **`<package>`** — the Java package dir under
  `java.generated.project/src/main/java/`; domain-derived per `CLAUDE.md`, may
  differ from `<study>` (e.g. `chemdetector` for `chemical_detector`).
- **`<N>`** — the run number to archive into (`experiments/convergence/<study>/run-<N>/`).
  The human picks it (one higher than the highest existing run for that study).
  You will **not** look at the existing `run-*/` dirs to infer it — it is given.
- **condition** — `B` (honest independent). Default `B` if unstated.

---

## 1. Independence — the rules that make this run count

**One run per session.** This session drives exactly ONE trajectory to
convergence, then stops. It must be a session with no prior knowledge of
`<study>`'s trajectory. If you (the session) have already driven or read this
study's runs in this conversation, STOP and tell the human — the run is
contaminated and must be done in a different session.

**Read for iter-1 cold codegen ONLY the canonical inputs:**
- `forge.assets/case-studies/<study>/system/system_description.txt`
- `forge.assets/case-studies/<study>/requirements/requirement_all.json`
- `CLAUDE.md`
- `forge.assets/prompts/java_codegen_rules.txt`
- `forge.assets/prompts/chain_of_thought_codegen.txt`
- `forge.assets/prompts/few_shot_codegen.txt`
- `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md` (process reference)
- this file

**For iter N ≥ 2, read ONLY:** the live workspace source under
`java.generated.project/src/main/java/<package>/` plus the immediately-prior
iter's `forge.assets/corrections/post_*.{md,json}`. Do not read ahead.

**Never read** the answer material in §4. If the human scrubbed the checkout
(recommended), those paths won't exist — good. If they do exist, you must still
not open them.

The durable technical guidance you need is inlined directly in `CLAUDE.md` (an
explicit canonical input, §1); derive the fix from what CLAUDE.md states inline
plus the actual pipeline sources under `forge.transformations/` (the tool, not
the answer key).

**Harness-injected context can contaminate a run before you act.** Two
channels feed prior-run knowledge into the session *automatically*, without
any `Read` call you can decline:

- **Agent auto-memory** — the `MEMORY.md` index and recalled memory files
  surfaced in the system prompt / `<system-reminder>` blocks.
- **The `.remember` SessionStart hook** — dumps `now/today/recent/archive`
  prior-run narratives (often the literal answer: which fixes worked, in
  which iter).

You cannot un-see these once they are in your context, so "just ignore
them" is **not** a control — the fix is to not have them injected, which is
the human's job (see Appendix → *Isolation profile*). If you observe
study-specific prior-run content in your context at session start (e.g. a
memory line naming this study's quirks, or a `.remember` entry describing a
past iter's fix), **say so explicitly in your first response and in the
trajectory's independence section** so the run can be judged or re-done.
Then proceed without opening the detailed memory file or any §4 path, and
prefer deriving fixes from the canonical inputs (CLAUDE.md carries the
durable technical guidance).

**No sub-agents.** Do all editing yourself with Read/Edit/Write/Bash. Do not
dispatch `Agent`-tool sub-agents (see HOWTO §2).

---

## 2. Setup (do this first)

1. Confirm `pipeline.yaml` `agent.active_case_study` is `<study>` (the human
   normally sets this; fix it if not).
2. Confirm FDR4 type ranges are `{0..1}` for all types
   (`forge.dashboard/corrections/type_ranges.json`).
   `run_experiment_iteration.py` applies them before FDR4; just verify.
3. **Set the FDR4 kill policy** in `pipeline.yaml`, phase `fdr4`:
   - `timeout: 3600` — FDR4 is allowed to run for up to **60 minutes** before
     the pipeline kills it (the committed default of `600` is too aggressive
     for the larger state spaces, e.g. the chemical_detector two-controller
     composition, which can need tens of minutes).
   - `memory_limit_mb:` set to **your machine's page-file size in MB** (not the
     conservative committed `4096`), so the only memory-side kill is genuine
     page-limit exhaustion.

   Net policy: **FDR4 is killed ONLY when it (a) has run for 60 minutes, or
   (b) exceeds the page/memory limit** (the OS aborts `refines.exe` /
   `_refines.exe` with `VirtualAlloc MEM_COMMIT failed`). Do NOT lower the
   timeout, and do NOT manually interrupt a live FDR4 run — interrupting yields
   an unreliable "pass" (KB O4); let it reach the 60-min timeout or a real OOM.
   These are local tuning edits; do not commit them (per §6).
4. Wipe the study's package so codegen starts clean:
   ```bash
   rm -rf  java.generated.project/src/main/java/<package>
   mkdir -p java.generated.project/src/main/java/<package>
   ```

---

## 3. The iteration loop

**Iter 1 — cold codegen.** From only the canonical inputs (§1), write the full
Java source tree under `java.generated.project/src/main/java/<package>/`
following `CLAUDE.md` conventions (sub-packages `annotation`, `mode`,
`constants`, `event`, `datamodel`/`sensor`, `actuator`, `operation`,
`controller`). Also write `java.generated.project/result_codegen.json` tracing
every requirement id to a Java element.

**Every iter:**
1. Make the edits (iter 1 = full codegen; iter N≥2 = minimal fixes driven by
   the prior iter's `post_*` feedback — change only what a failing phase asks
   for; don't refactor, don't add requirements).
2. Run the pipeline:
   ```bash
   python experiments/scripts/run_experiment_iteration.py
   ```
   (12 phases: compile, coverage, preflight, t2m, m2m, m2t, dafny_gen,
   isabelle_gen, fdr4, dafny_verify, isabelle_verify, vacuity. Isabelle runs in
   WSL; FDR4 can run up to 60 min on large models (§2.3). Authoritative per-phase
   status is in `forge.assets/corrections/post_<phase>.json`, not the CLI summary.)
   **Pipeline time is recorded automatically:** the runner writes per-phase and
   total wall-clock to `forge.assets/corrections/phase_timings.json`, which the
   snapshot consumes (no manual timing needed).
3. Snapshot:
   ```bash
   python experiments/scripts/snapshot_iter.py <study> <iter> --actor me-as-developer
   ```
   The snapshot auto-fills the **pipeline timing** from `phase_timings.json`:
   `summary.json.pipeline_wall_clock_s` (total) and `phase_wall_clocks_s`
   (per phase, incl. the dominant `fdr4` time) — do not fill these by hand.

   Then fill the remaining `summary.json` TODO fields by hand:
   `failure_summary`, `actor_codegen_summary`, and `codegen_cost.*`.
   **Tokens (per-iter): leave estimated/null — do not fabricate.** A
   `me-as-developer` session cannot reliably read its own *per-iter* token
   usage mid-run: `/cost` is cumulative and is not exposed to the agent as a
   tool, and splitting it per iter is impractical. So set
   `codegen_cost.kind = "estimated"` and leave
   `tokens_in`/`tokens_out`/`tool_uses`/`wall_clock_s` as `null` (this is what
   all runs to date have correctly done). Token cost is captured instead as a
   **run-level total once, at archive time** (§6). If a future harness exposes
   per-iter usage, switch `kind` to `"measured"` and fill the numbers; until
   then, `null` is the honest value.
4. Read the new `post_*` feedback and decide the next iter's fix.

**Diagnose before editing.** Read the failing phase's `post_<phase>.json`
`java_trace` and ask "what construct does the verifier not accept" before
touching control flow. Most failures are Java↔model *encoding* mismatches, not
control-logic bugs.

---

## 4. Forbidden reads (the answer key)

Do not open any of these during the run:
- `experiments/convergence/<study>/run-*/` and `.../iter-*/` and `.../prev-trajectory/`
- `experiments/convergence/` for any **other** study (cross-study format peeking is still leakage)
- `experiments/cold-baseline/`
- `experiments/convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md` ← the aggregated findings sink. **Never** read it, in *any* condition; it is the consolidated answer key. You DO **append** your own run's findings to it at archive time (§6) — append-only, without reading it.
- `.remember/` (`now.md`, `today-*.md`, `recent.md`, `archive.md`,
  `remember.md`, `core-memories.md`) — prior-session handoffs and daily
  logs; they narrate past runs' fixes
- the agent auto-memory dir for this project
  (`~/.claude/projects/<slug>/memory/`, incl. `MEMORY.md`) — do not open
  the memory files even if a `MEMORY.md` line is surfaced to you

The last two are normally **injected** rather than read (see §1) — the
human should suppress them via the Appendix *Isolation profile*; this entry
covers the case where they exist on disk and you might be tempted to open
them.

If you need a `trajectory.md` format example, the HOWTO §5 describes the
format; do not read another study's `trajectory.md` to copy it.

---

## 5. Stop conditions

- **Converged:** every phase reports `passed`/`completed` AND vacuity audit = 0
  findings. Stop.
- **Cap:** default 7 iters. If you hit it without converging, stop and document
  why in the trajectory write-up.
- **Pipeline crash** (a deterministic phase throwing a Python/Java exception,
  not a verifier verdict): the iter is unusable — fix the root cause and re-run,
  do not snapshot it.

---

## 6. On convergence (or cap) — archive

1. Create `experiments/convergence/<study>/run-<N>/` and move the loose
   `iter-*/` snapshots into it (`run-<N>/iter-1/ …`).
2. Write `run-<N>/trajectory.md` per HOWTO §5: headline table (per-iter change
   + per-phase pass/fail + **per-iter pipeline wall-clock** (from each iter's
   `summary.json`) + converged), iter-by-iter narrative, caveats (spec
   compromises / tooling workarounds), **a run-level Claude token total**
   (see step 2a), **a run-level end-to-end execution time** (see step 2b),
   **findings (durable lessons surfaced
   during this run that aren't obvious from `CLAUDE.md`, the codegen rules,
   or this runbook, and that should help future runs of any case study)**,
   reproducibility. State the actor (`me-as-developer`), independence
   (which inputs iter-1 read), and condition.
   2a. **Record the run-level Claude token total** in `trajectory.md` (the
   achievable token measurement, vs the per-iter `null`s — §3). Capture it once
   now, at session end, from the **cumulative** figure the driving Claude Code
   session reports via `/cost` (total input + output tokens for the whole run);
   if `/cost` is unavailable, sum the `usage` blocks from the session transcript
   JSONL. State the source. This is whole-run, not per-iter — do not try to
   split it across iters.
   2b. **Record the run-level end-to-end execution time** in `trajectory.md`, so
   the run captures execution time across *all* phases through convergence — not
   just the deterministic pipeline. The per-phase deterministic times
   (compile → vacuity, dominated by FDR4) are already auto-recorded in each
   `iter-N/summary.json` (`phase_wall_clocks_s` + `pipeline_wall_clock_s`); what
   they OMIT is **Phase-2 codegen, feedback diagnosis, and this write-up**.
   Recover the whole-run **end-to-end** wall-clock from the driving session's
   transcript JSONL: the span from the first to the last message timestamp
   (`max(timestamp) − min(timestamp)` over the session's `*.jsonl`). Record
   three figures:
   - **end-to-end** = session span;
   - **pipeline** = Σ `pipeline_wall_clock_s` over iters (or the *productive*
     total if you discount hung phases — say which);
   - **agent = end-to-end − pipeline** (codegen + diagnosis + write-up + idle —
     **not** pure codegen; the trajectory write-up is part of it).
   This is the time analogue of the token total (2a): a whole-run figure
   recovered once at session end, not per-iter.
3. **Append this run's durable findings** to
   `experiments/convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md` as a new
   dated, provenance-tagged subsection (`study + run-<N> + commit`). This is the
   one forbidden-read file you *write* to: **append only — do NOT open or read
   its existing contents** (use a shell append, e.g. a heredoc `>>`), so this
   run never pulls prior answers into context and the next run's independence is
   preserved. Append only the findings you yourself surfaced this run.
   *If you are running in a scrubbed worktree (Appendix), this file was removed
   and won't exist here — that is correct. Do NOT recreate it in the worktree;
   record your findings in `run-<N>/trajectory.md` only, and the KB append then
   happens during copy-back to the main checkout (Appendix, copy-back step).*
4. Commit ONLY `experiments/convergence/<study>/run-<N>/` and your appended
   block in `CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md`. Do **not** commit
   machine-specific `pipeline.yaml` tool-path edits or the
   `java.generated.project` workspace.
5. Report to the human: converged iter count, the per-iter fix summary, and any
   spec deviations invoked.

---

## Appendix — human prerequisites (not for the agent)

Before opening the fresh session:

- **Tooling** (per machine, HOWTO §0): Dafny at `pipeline.yaml`
  `dafny_verify.dafny_path` win32 path; WSL default = Ubuntu
  (`wsl --set-default Ubuntu`) with Isabelle/UTP at `wsl_isabelle_bin`.
- **Isolation profile (REQUIRED for a run to count as independent).** A
  scrubbed checkout alone is **not enough** — the harness injects prior-run
  knowledge through two channels that bypass file reads entirely (§1: agent
  auto-memory and the `.remember` SessionStart hook). Independence is set up
  at *session-launch time*, by the human, on all three fronts:

  1. **Worktree at a NEW path** (gives a fresh, empty agent-memory namespace,
     because memory is keyed by `~/.claude/projects/<slug-of-cwd>/`):
     ```bash
     git worktree add ../fmgvc-run <clean-commit>
     cd ../fmgvc-run
     rm -rf experiments/convergence/ experiments/cold-baseline/ \
            .remember/                       # tracked remember.md + any logs come along — drop them
     ```
     **Choosing the base — sever the history.** The SessionStart git-status
     snapshot (and any `git log` the session runs) exposes the worktree HEAD's
     **ancestor** commit messages, and a run-archive commit names its iteration
     count (`… archive <study> run-N … converged iter-K`) — harness-injected
     leakage you cannot decline (§1; observed in chemical_detector run-5, whose
     base *was* the run-4 archive commit, leaking "run-4 converged iter-3"). You
     usually **cannot** dodge this by picking an older linear base: the
     convergence archives were introduced long ago and the pipeline kept
     evolving on top, so every commit with a *current* pipeline still has
     `run-*` archive commits somewhere in its ancestry. The robust fix is to give
     the worktree a **parentless base commit** carrying the desired tree, so its
     entire history is one clean, leak-free message:
     ```bash
     BASE=$(git commit-tree <clean-commit>^{tree} \
              -m "isolated run base (history severed for run independence)")
     git worktree add ../fmgvc-run "$BASE"     # detached at a 1-commit history
     ```
     Then overlay any doc/infra fix that postdates `<clean-commit>` without
     reintroducing ancestry (its *content* reaches the session; no commit does):
     `git checkout <main-HEAD> -- CLAUDE.md experiments/RUN_TRAJECTORY.md`.
     Verify inside the worktree with `git log --oneline` — it must show exactly
     the one severed commit, with no `run-*`/`iter-*`/`archive` terms. This is
     how run-6 was set up; run-4/run-5 used a plain linear base, which is why
     run-5 leaked run-4's count.
     A worktree won't carry the untracked project-local `.claude/settings.local.json`,
     so the `.remember` SessionStart hook won't fire there; verify it isn't
     also registered in your user-global `~/.claude/settings.json`.
  2. **Clean config home (hard guarantee for memory + user hooks):** launch
     the experiment session with `CLAUDE_CONFIG_DIR` pointed at an empty
     directory. This removes ALL agent auto-memory and any user-level hooks
     in one move; nothing needed is lost, because the canonical inputs (§1)
     all live in the repo. If you skip this, at minimum confirm
     `~/.claude/projects/<new-slug>/memory/` is empty before starting.
  3. **Open the fresh session with CWD = `../fmgvc-run`.** After the run,
     reconcile back in the **main checkout** (where the un-scrubbed files live):
     ```bash
     # from the main checkout, with the worktree at ../fmgvc-run:
     cp -r ../fmgvc-run/experiments/convergence/<study>/run-<N> \
           experiments/convergence/<study>/run-<N>     # overwrites the slot if re-running
     ```
     Then, in the main checkout:
     - **append** this run's findings (from its `run-<N>/trajectory.md`) to
       `experiments/convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md` — the §6
       step-3 append happens *here*, because the KB was scrubbed out of the
       worktree (append-only; still don't let the worktree session read it);
     - commit, then `git worktree remove ../fmgvc-run`.

  **Same machine/account caveat:** even with the above, a true cold run is
  only guaranteed the *first* time a study is driven on a given
  machine/account; later runs can be re-seeded by memory/`.remember` unless
  you re-apply the isolation profile every time. When in doubt, use a clean
  `CLAUDE_CONFIG_DIR` per run.

- **One run per session, always.** Never drive run-N+1 in the session that
  produced run-N — a shared context window is the main thing that destroyed
  independence in the first round (run-1/2/3 of each study shared one session).
- **Audit by content, not just path-reads.** Before accepting a run:
  (a) skim its transcript and confirm no `Read`/`Grep` touched a §4 path; AND
  (b) grep the session's injected context (system prompt, `<system-reminder>`
  blocks, SessionStart hook output) for study-specific prior-run terms
  (the study name, its known quirks, fix keywords). A run whose *context*
  contained study answers is disqualified **even if it never explicitly
  read a forbidden file** — and should be re-run under the isolation profile,
  or at minimum labelled (e.g. `condition-B-with-memory-exposure`) in its
  trajectory so it isn't mistaken for a clean cold run.
