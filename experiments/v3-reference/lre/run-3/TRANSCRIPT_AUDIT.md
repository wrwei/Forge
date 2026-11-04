# Reference v3, LRE run 3 (re-collection) — transcript audit

At the Amendment 7 pin `56cb43d7`; set up under Amendments 3, 4, 7 and 8. The run
collected before Amendment 7 is kept in `experiments/v3-superseded-a7/`.

Sources: `transcript.md` (Claude Code `/export`, 320 lines; 12 Bash and 1 Write
calls shown) and the session log (`scripts/session_audit_logs.sh`: 31 logged
commands, no Read call; tool listing Bash 31, Write 1; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 60 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook only (text hash `c0b542fbe3cb`); no Skill call |
| Setup | scaffold names `controller`; `.gitignore` and `pipeline.yaml` comments name no study; no root `README.md`; `docs/` holds only `corpus/` and the manifest schema; `forge.assets/case-studies/` holds only `lre` |
| Reads | all by shell (`cat`): the three LRE inputs, the five files `CLAUDE.md` names under `forge.assets/prompts/`, the layer prompts in `forge.assets/vibe-coding-prompts/`, the Gradle scaffold, and directory listings of `java.generated.project/`, `forge.assets/corrections/`, the two prompt folders and (the prompt's preflight line) `forge.assets/case-studies/`, which lists only `lre`. No pipeline source, other study, archive, staging folder, other worktree, git history, report or removed file; `pipeline.yaml` only through the preflight line; `.gitignore` not read. |
| How the session wrote | unlike earlier sessions, it wrote the 15 Java files with shell heredocs (`cat > $B/... <<'EOF'`, content inline; no `cp`, `rsync`, `curl` or `git` command anywhere). The prompt does not prescribe a tool. It generated the trace and the Layer 5 report with two scratchpad Python scripts whose rows are inline and which open no file for reading, and wrote `ITER_SUMMARY.md` with the Write tool. |
| Self-checks before the runner | compiled the Java with `javac` into the scratchpad, then compiled and ran a short scratchpad driver (`Smoke.java`) against those classes; both read only the session's own code |
| Writes | all under `java.generated.project/src/main/java/lre/`, `java.generated.project/result_codegen.json`, `forge.assets/corrections/` and the session's scratchpad; last write at command-log line 1841, runner at line 1847 |
| Runner | one call (logged once), iteration 1, as the session's last command. No iteration 2 was needed. |
| After the runner | nothing. Checked after the export: the worktree's Java (15 files) and trace are byte-identical to the staged record, and no source file is newer than its `summary.json` |
| Working tree | unrestricted `git status`, after the export: setup's `pipeline.yaml` and `.gitignore` edits and the reports, besides setup's removals |

**Verdict: valid.**

**Result: converged at iteration 1** at `56cb43d7`. All twelve phases pass: FDR4
3 of 3 assertions; Dafny 1 file, 12 verified, 0 errors; Isabelle 1 theory, 47 lemmas
(2 min 22 s); vacuity passed with the three LRE advisories (CAM, HCM and MOM
unreachable and 18 transitions unfireable under the checked CSP instantiation;
the guard constant `cpaepsilon` rounded in the CSP model). The profile check
passes with nine rule-8 and two rule-6 warnings. No `MAX_VALUE` or
scientific-notation literal in the Java.
