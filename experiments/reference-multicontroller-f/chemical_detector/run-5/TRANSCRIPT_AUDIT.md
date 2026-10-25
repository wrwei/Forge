# Run 5 — transcript audit for forbidden reads

Source: `transcript.md` (Claude Code `/export` of the run-5 session, 390 lines,
15 tool calls with result markers). Audited 2026-10-25.

Forbidden for the actor: `forge.transformations/`, `forge.dashboard/`,
`docs/fixes|design|research|experiments`, `experiments/convergence`,
`experiments/cold-baseline`, `reference-runs/`, `docs/CONVERGENCE_PLAYBOOK.md`,
`docs/evaluation/*-experiment-*.md`, `.remember`.

Findings: no tool call read, listed, searched or executed anything under a
forbidden path.
- 13 `Bash` calls: the runner (preflight; iteration 1); `cat` of
  `forge.assets/vibe-coding-prompts/*.md` and the requirements README; a
  toolchain check (`javac`/`java -version`; `ls` of the runner script's path,
  existence only); three heredoc blocks writing the 20 Java files, every target
  under `java.generated.project/src/main/java/chemical_detector/`; a smoke test
  in the Claude scratchpad under `/private/tmp`; three `python3` report
  scripts; one `grep` and one `sed -n` over the actor's own Java.
- 2 `Write` calls: `forge.assets/corrections/ITER_SUMMARY.md`.
- 5 collapsed read/list calls: the specification files; the corrections
  directory; the actor's own summary (twice); the runner's result.
- `forge.transformations/` and `docs/...` occur only in the prompt's own
  prohibition text (L39-40, L86).

Naming note: this actor named its controllers `GasAnalysis` and `Movement`,
so the theories are `GasAnalysis_Beh.thy` and `Movement_Beh.thy` (10 and 30
operations, as in runs 1-4).

Limitation: as for runs 1-4, collapsed read paths are established from the
surrounding narration.

Lemma-count note (applies to every run of this collection): the Isabelle
feedback report's summary line counts all lemma statements in the two theories
(86 = 23 + 63), while its per-theory itemisation lists only the named
invariant-preservation and deadlock-freedom lemmas (44 = 12 + 32); the other 42
are unnamed R-series requirement lemmas ("Init establishes R1", "... preserves
R1 under <inv>"). Isabelle proves every statement; both figures are correct for
what they count.
