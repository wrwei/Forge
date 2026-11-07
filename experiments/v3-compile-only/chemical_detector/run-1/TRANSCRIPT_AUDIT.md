# Compile-only v3, Chemical Detector run 1 — transcript audit

At the Amendment 18 pin `aaddf222`; set up under Amendments 3 and 4.

Sources: `transcript.md` (Claude Code `/export`, 830 lines) and the session log
(`scripts/session_audit_logs.sh`: 22 logged commands, 2 Read calls listed twice; tool
listing 35 Bash, 4 Read, 5 Write).

| Check | Result |
| ----- | ------ |
| Model | `claude-opus-5-5` in all 99 responses |
| Harness | Claude Code 2.1.318 (174 records) |
| Session | one user turn; no IDE file-open notice |
| Hooks | `superpowers` SessionStart, text unchanged; two `ide_diagnostics` attachments from VS Code's Java language server, each a classpath warning on one of the session's own files ("not on the classpath … only syntax errors are reported"). They concern the session's own code and carry nothing from the pipeline or another study; recorded as a non-voiding deviation |
| Reads | all permitted: the requirements and the codegen rules (each twice), its own Java, `post_codegen.md`, the visible reports, the runner's output |
| Withheld reports | never named as files in the transcript or the command log; absent from the worktree after the runner. The closing summary notes that the runner hid the verifier output, as the runner's banner states; no verdict was seen |
| Writes | all before the runner; most Java written by shell heredoc (the 35 Bash calls). The closing "22 files edited" recounts the 18 Java files plus the trace, the two `post_codegen` files and the summary; nothing was written after the runner |
| Runner | called once (iteration 1) |
| Stop | ALL VISIBLE PHASES PASSED; the session stopped and reported the stop point at iteration 1 |
| After the export | Java (18 files, 799 lines) and `result_codegen.json` byte-identical to the record; no source newer than the record; `git status` shows setup's edits (`.gitignore`, `pipeline.yaml`) and the corrections directory only |

**Verdict: valid. Stop point at iteration 1.**

Withheld verdicts at the stop point (recorded, not shown to the session): FDR4 9 of 9
assertions (the composed system and each controller); Dafny 2 files, 23 verified, 0
errors; Isabelle 2 theories, 78 lemmas (4 min 2 s); vacuity passed with one advisory per
controller. All twelve phases pass, so the stop point is verification-passing. The
profile check passed with two rule-8 warnings.
