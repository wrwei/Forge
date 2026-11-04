# Reference v3, Chemical Detector run 4 — transcript audit

At the Amendment 7 pin `56cb43d7` (the first Chemical Detector Reference session
there; runs 1 to 3 ran at `d526ea5e`, which differs only in the scaffold's project
and group names and the leakage gate); set up under Amendments 3, 4, 7 and 8.
Three iterations.

Sources: `transcript.md` (Claude Code `/export`, 1312 lines; 23 Bash, 29 Write and
2 Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 46
logged commands, 2 Read calls; tool listing Bash 46, Write 29, Edit 4, Read 2,
Edits and the Read logged twice; the hook and skill listing). A mid-run export,
`transcript (2).md`, was taken during iteration 2 and is superseded by the final
export.

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 154 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice. The operator's mid-run `/export` is a Claude Code command and adds no turn. |
| Hooks and skills | the `superpowers` SessionStart hook (text hash `c0b542fbe3cb`); no Skill call. Five further injected records: the VS Code Java extension's classpath diagnostic for files the session edited, each naming the neutral project `controller-java.generated.project` |
| Setup | scaffold names `controller`; `.gitignore` and `pipeline.yaml` comments name no study; no root `README.md`; `docs/` holds only `corpus/` and `superpowers/specs/pipeline-manifest.schema.json` (the manifest schema Amendment 4 keeps) |
| Reads before iteration 1 | the Chemical Detector requirements (Read tool, logged twice), the other Chemical Detector inputs, the files `CLAUDE.md` names, layer prompts, Gradle scaffold (by shell), its own Java |
| Reads between iterations | after iteration 1 (command-log line 229): `post_fdr4.md` and `post_isabelle_verify.md`, the two failing phases (lines 233, 235). After iteration 2 (line 348): `post_isabelle_verify.md`, the one failing phase (line 352). No other report, and no generated model (`.rct`, `.csp`, `.thy`, `.dfy`, `.xmi`). |
| Never read | pipeline source, other study, archive, staging folder, other worktree, git history, removed file; `pipeline.yaml` only through the preflight line; `.gitignore` not read |
| How the session wrote | Java with the Write tool and one inline Python edit of its own `MovementController.java` (line 237); the trace and the Layer 5 report by two scratchpad scripts (`gen_trace.py`, `gen_review.py`) whose only input is the study's `requirement_all.json` and the session's own Java tree, patched by inline scripts between iterations; three inline trace checks that read only the session's trace and Java; `ITER_SUMMARY.md` with the Write tool and Edits. No `cp`, `rsync`, `curl` or `wget` anywhere. |
| Iteration 1 → 2 | FDR4 failed (CSP parse error) and Isabelle failed (type coercion). The session recorded in `ITER_SUMMARY.md` that it believes the cause is a pipeline limitation (four operation classes, each with a `compute(...)` method taking arguments, appear in the model as one operation `compute`) and, as the prompt directs for that case, worked around it in its Java: the movement controller sends Vehicle commands as output events, carried out by `MovementActuator` (3 Java files). No report directed a change to the instrument. |
| Iteration 2 → 3 | Isabelle failed with the same three errors (marked recurring); the session edited `MovementActuator`, `Move` and `ChangeDirection`. |
| Runner | three calls, iterations 1, 2 and 3 (command-log lines 229, 348, 399), within the 7-iteration cap |
| After the last runner | one read of the runner's background-task output. Checked after the export: the worktree's Java (22 files) and trace are byte-identical to iteration 3's staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` and `.gitignore` edits and the reports, besides setup's removals |

**Verdict: valid.**

**Result: converged at iteration 3** at `56cb43d7`. Iteration 3: all twelve phases
pass; FDR4 9 of 9 assertions; Dafny 2 files, 22 verified, 0 errors; Isabelle
2 theories, 78 lemmas (3 min 45 s); vacuity passed with three advisory findings
(one per controller, and the `Idle_Analysis` idle operation unreachable). The
profile check passes with two rule-8 warnings. The Java keeps four operation
classes (`Move`, `ChangeDirection`, `RandomWalk`, `ShortRandomWalk`). No
`MAX_VALUE` or scientific-notation literal in the Java.

**To examine after the collection (not a validity question).** Whether the
extractor maps several operation classes whose method is named `compute` and
takes arguments to a single model operation, as the session reported. LRE
sessions use argument-free `compute()` methods and convert correctly.
