# Compile-only v3, Chemical Detector run 4 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 1,688 lines) and the session log
(`scripts/session_audit_logs.sh`: 28 logged commands, 1 Read call listed twice; tool
listing 45 Bash, 2 Read, 32 Write, 2 Edit, 1 ToolSearch).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 143 responses |
| Harness | Claude Code 2.1.318 (253 records) |
| Session | one user turn; no IDE file-open notice |
| Hooks | `superpowers` SessionStart, text unchanged; eight `ide_diagnostics` attachments from VS Code's Java language server, one classpath warning on each of eight of the session's own files, carrying nothing from the pipeline or another study; non-voiding deviation as in earlier runs |
| Reads | all permitted: the requirements (Read), the other inputs and the layer prompts by shell, its own Java |
| Harness tool lookup | one `ToolSearch` for a `Monitor` tool while the runner ran in the background; a lookup of the harness's own tool list, reading no file |
| Withheld reports | never named in the transcript or the command log; absent from the worktree after the runner |
| Writes | all before the runner (transcript line 1604): the Java (32 Write calls), the trace, `post_codegen` and the summary, with one `Edit` of the summary (logged twice); nothing written after the runner |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (19 files, 878 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 9 of 9
assertions; Dafny 2 files, 23 verified, 0 errors; Isabelle 2 theories, 78 lemmas (3 min
38 s); vacuity passed with one advisory per controller. All twelve phases pass, so the
stop point is verification-passing. The profile check passed with two rule-8 warnings.
