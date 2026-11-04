# Reference v3, Steam Boiler run 1 (collected under Amendment 9; superseded by Amendment 10) — transcript audit

At the pin `56cb43d7`; set up under Amendments 3, 4, 7, 8 and 9, at the FDR4 budget
then in force (9,000 s / 10,240 MB). Worktree `forge-v3r9-run1-steam_boiler`.

Sources: `transcript.md` (Claude Code `/export`, 1874 lines; 16 Bash and 24 Write
calls shown) and the session log (`scripts/session_audit_logs.sh`: 31 logged
commands, 26 Read calls; tool listing Bash 31, Read 26, Write 24, ToolSearch 1,
Reads logged twice; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 145 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | one user turn: the pasted prompt text; no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook (text hash `c0b542fbe3cb`); no Skill call. Three further injected records: the VS Code Java extension's classpath diagnostic for three Java files, each naming the neutral project `controller-java.generated.project` |
| Setup | the recorder accepted the study; `forge.assets/corrections/csp_overrides.csp` removed; scaffold neutral; `.gitignore` and `pipeline.yaml` comments name no study |
| Reads before the runner | the Steam Boiler requirements and README, the five files `CLAUDE.md` names and the five layer prompts (Read tool), the other Steam Boiler inputs and Gradle scaffold (by shell), its own Java; inline checks of the study's requirements and the session's own trace and Java. No pipeline source, other study, archive, staging folder, other worktree, git history or removed file; `pipeline.yaml` only through the preflight line; `.gitignore` not read. |
| How the session wrote | the 16 Java files, the trace, the Layer 5 report and `ITER_SUMMARY.md` with the Write tool. No `cp`, `rsync`, `curl` or `wget` anywhere. |
| Runner | one call, iteration 1 (command-log lines 223 and 225: one call logged twice); last Write at transcript line 1748, runner shown running in the background at line 1774 |
| After the runner | the runner log, then only the reports of the three failing phases (`post_fdr4.md`, `post_dafny_verify.md`, `post_isabelle_verify.md`), which the Reference loop permits; no other file. No Java changed and no second runner call: the session stopped on the FDR4 report's directive to raise `phases.fdr4.memory_limit_mb`, as the prompt requires, and reported it. |
| Instrument | `git diff pipeline.yaml` shows only setup's edits (active study, budget, the removed comment lines); no other instrument file changed. The worktree's Java (16 files) is byte-identical to the staged record. |

**Verdict: valid as collected; superseded by Amendment 10**, which raises the FDR4
memory budget the session stopped on.

**Result as collected: not converged, stopped at iteration 1.** Nine phases pass;
FDR4 was killed at the 10,240 MB budget (peak 10,308 MB); Dafny fails with
`Ambiguous use of && and ||` in the generated model; Isabelle fails with `"ℝ" is not a
subtype of "𝔹"` (the generated theory types guard predicates as reals).
