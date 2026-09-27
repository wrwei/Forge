# FORGE convergence experiment — iteration {iter_n} (feedback-driven refinement)

You are doing **iteration {iter_n}** of the FORGE convergence experiment for
the **{study}** case study. Iteration {prev_iter} produced Java source that
went through the pipeline; the per-phase verifier feedback is now waiting at
`forge.assets/corrections/post_*.md`. Your job is to apply the fix
directives.

## What to read

**Required inputs:**

1. `java.generated.project/src/main/java/{study}/**` — the current Java source (output of iter {prev_iter}). This is the pipeline's workspace, not under `experiments/`; the agent has to read it to know what code it's editing.
2. `forge.assets/corrections/post_compile.md` — Phase 2a (Java build) feedback
3. `forge.assets/corrections/post_coverage.md` — Phase 2b (requirement ↔ Java trace) feedback
4. `forge.assets/corrections/post_preflight.md` — Phase 2c (structural lint) feedback
5. `forge.assets/corrections/post_t2m.md` — Phase 3 (Spoon discovery) feedback
6. `forge.assets/corrections/post_m2m.md` — Phase 4 (Java EMF → RoboChart) feedback
7. `forge.assets/corrections/post_m2t.md` — Phase 5b (RoboChart → CSP) feedback
8. `forge.assets/corrections/post_dafny_gen.md` — Phase 5a (Java EMF → Dafny) feedback
9. `forge.assets/corrections/post_isabelle_gen.md` — Phase 5c (RoboChart → Isabelle theory) feedback
10. `forge.assets/corrections/post_fdr4.md` — Phase 6b (FDR4 CSP refinement) feedback
11. `forge.assets/corrections/post_dafny_verify.md` — Phase 6a (Dafny verification) feedback
12. `forge.assets/corrections/post_isabelle_verify.md` — Phase 6c (Isabelle build) feedback
13. `CLAUDE.md` — project-level conventions (at the repo root)
14. `forge.assets/prompts/java_codegen_rules.txt` — the rule set
15. `forge.assets/prompts/fdr4_system.txt` — guide for interpreting FDR4 results (note: determinism is not verified — the pipeline strips the `:[deterministic]` assertion, so FDR4 checks only deadlock- and divergence-freedom)

**Do NOT read** (would leak structure):

- `experiments/convergence/{study}/iter-*/` — prior iters' snapshots
- The repo-root `reference-runs/{study}/java/` — the paper-companion converged source (the answer)

## What to do

For each `post_*.md` whose `status` is not `passed`:

1. Read the `fix_directive` and `java_trace` entries in the issue list.
2. Locate the corresponding Java file(s) under `java.generated.project/src/main/java/{study}/`.
3. Apply the directive **minimally** — change only what the directive asks for. Do not rewrite for style, do not refactor unrelated code, do not add new requirements.
4. Respect the codegen rules at all times. The M2M will re-run after your edits; any rule violation breaks subsequent phases.

**Special cases:**

- **Determinism is not verified.** The pipeline strips the official generator's `:[deterministic]` assertion before invoking FDR (`pipeline.yaml` `phases.fdr4.expected_failures: []`), so `post_fdr4.md` will not report a determinism failure. FDR4 checks only deadlock- and divergence-freedom.
- **`post_coverage.md` reports `over_implementation` issues** for elements without a requirement ID. If the element is structurally necessary (the controller class itself, the mode enum, the InputEvent sealed interface, etc.), leave it. If it's a genuinely unused helper, remove it.
- **`post_codegen.md`** (if present) is the prior iter's self-reported design choices and uncertainties — useful context but not a directive list.

## What to output

Apply edits via the Edit tool. **Do not output Java in the chat.** End your response with a single line `ITER_{iter_n}_COMPLETE: <N> files edited` and nothing else.
