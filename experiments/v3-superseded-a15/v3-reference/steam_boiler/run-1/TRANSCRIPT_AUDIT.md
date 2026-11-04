# Reference v3, Steam Boiler run 1 (re-collected under Amendment 10) — transcript audit

At the pin `56cb43d7`; set up under Amendments 3, 4, 7, 8, 9 and 10 (FDR4 budget
9,000 s / 40,960 MB). Worktree `forge-v3r10-run1-steam_boiler`. The runs it replaces
are kept in `experiments/v3-superseded-a9/` and `experiments/v3-superseded-a10/`.
Four iterations.

Sources: `transcript.md` (Claude Code `/export`, 2413 lines; 47 Bash, 22 Write and
6 Update calls shown) and the session log (`scripts/session_audit_logs.sh`: 76
logged commands, 18 Read calls; tool listing Bash 76, Read 18, Write 22, Edit 12,
ScheduleWakeup 4, Reads and Edits logged twice; the hook and skill listing).

| Check | Result |
|---|---|
| Model | Claude Opus 5.5 (`claude-opus-5-5`), all 305 responses |
| Claude Code | 2.1.314 throughout |
| Operator input | two user turns: the pasted prompt text, and at transcript line 1601 the operator's question "still running?", typed while iteration 3's runner was in the background (see Deviation); no IDE notice |
| Hooks and skills | the `superpowers` SessionStart hook (text hash `c0b542fbe3cb`); no Skill call. Two further injected records: the VS Code Java extension's classpath diagnostic for `SteamBoilerController.java`, naming the neutral project `controller-java.generated.project` |
| Setup | `setup_ok.json`: complete, all checks passed; the recorder accepted the study; FDR4 budget 9,000 s / 40,960 MB; `csp_overrides.csp` removed; scaffold neutral; `.gitignore` and `pipeline.yaml` comments name no study |
| Reads | the Steam Boiler requirements and README, the five files `CLAUDE.md` names, the layer prompts, the Gradle scaffold and its own Java; `docs/corpus/zmachine-framework/Z_Machine.thy` once after iteration 3 (the prompt permits `docs/corpus/`; no corpus file names a study); `pipeline.yaml` through the preflight line and once through its `agent` section (`grep -n -A3 'agent' pipeline.yaml`, permitted by Amendment 5); between iterations only the reports of failing phases (iteration 1: `post_preflight.md`, `post_m2t.md`, `post_isabelle_verify.md`; iterations 2 and 3: `post_fdr4.md`, `post_isabelle_verify.md`) and the runner's own output. No pipeline source, other study, archive, staging folder, other worktree, git history or removed file; `.gitignore` not read. |
| How the session wrote | Java with the Write tool and Edit calls; the trace and the Layer 5 report by two scratchpad scripts (`gen_trace.py`, `gen_post_codegen.py`) that read only the session's own Java, trace and `post_codegen.json`; a scratchpad simulator (`sim/`) run on its own compiled classes. No `cp`, `rsync`, `curl` or `wget` anywhere. |
| Runner | four calls, one per iteration (command-log lines 374, 501, 621, 885; line 376 repeats the first), each in the background (transcript lines 713, 947, 1570, 2304), within the cap of 7 |
| Writes while a runner ran | none to the Java or the trace. One: `ITER_SUMMARY.md` trimmed from 324 to under 300 words at transcript line 1612, during iteration 3's runner, after the operator's question; the iteration-3 record holds the trimmed summary (275 words) |
| Wake-ups | four `ScheduleWakeup` calls: fallback reminders while runners ran, the last cancelled at the end; none reads anything |
| After the last runner | one read of the runner output, then the report. Checked after the export: the worktree's Java (15 files) and trace are byte-identical to the iteration-4 record, and no source file is newer than its `summary.json` |
| Instrument | `git diff pipeline.yaml` shows only setup's edits (active study, FDR4 budget, the removed comment lines); no other instrument file changed |

**Deviation (non-voiding).** The operator asked "still running?" in the session while
iteration 3's runner was in the background. The question carries no information about
the task. In reply the session checked the runner's output, trimmed its own
`ITER_SUMMARY.md` (not Java, not part of any verified artefact), and said the run was
still going. It changed no Java and made no runner call until iteration 3 had
finished. None of the pre-registered void conditions applies.

**Iterations.**

| Iteration | Failing phases | FDR4 | Repair before the next iteration (from the session's summaries) |
|---|---|---|---|
| 1 | preflight, M2T, Isabelle | not run (M2T failed) | the profile and M2T failures fixed |
| 2 | FDR4, Isabelle | about 1 s: CSP type error (an `int` argument on actuator commands such as `regulatePumps(n)` became a typed event sent with no value); 0 of 1 checked | argument-free actuator commands; compound guard predicates replaced by single-call predicates (Isabelle typed a predicate used inside another as real) |
| 3 | FDR4, Isabelle | 961 s: killed by macOS, not by the pipeline. The kernel log shows `memorystatus: killing largest compressed process _refines [12750] 98418 MB` at 12:17:53; the pipeline's monitor, which reads resident memory (`ps -o rss`, 5 to 7 GiB when sampled), never reached its 40,960 MB limit. With no verdict and no pipeline kill, the runner's fallback labels the failure "CSP-M parse error" (see note below). Isabelle: tactic timeout in `SteamBoilerController_deadlock_free` (597 s) | controller reduced from 37 to 20 transitions, with mutually exclusive guards per mode; behaviour checked unchanged on the session's simulator |
| 4 | none | 19 s, 3 of 3 assertions | — |

**Verdict: valid.**

**Result: converged at iteration 4** at `56cb43d7`. All twelve phases pass: FDR4
3 of 3 assertions (19.3 s); Dafny 1 file, 11 verified, 0 errors; Isabelle 1 theory,
53 lemmas (3 min 18 s); vacuity passed with one advisory finding (a state with no
enabled operation when the environment offers nothing). Java: 15 files, 1,344 lines.
The session records a fidelity gap that fails no phase: the extractor drops the mode
value carried by the MODE message.

**Note on iteration 3's FDR4 failure** (added 1 November 2026, from the operator's
`log show` of 12:15–12:20). On macOS the pipeline's FDR4 memory monitor reads the
resident set size of `refines` (`ps -o rss`, `forge.dashboard/web/runners.py`,
`_get_process_memory_mb`), which leaves out pages the system has compressed. In
iteration 3 the check's footprint grew to about 96 GiB (98,418 MB in the kernel's
kill message) on a 48 GiB machine while its resident size stayed between 5 and 7 GiB,
so the 40,960 MB budget never fired and the kernel's memory-pressure killer ended the
process. The FDR4 check therefore ran until the machine had no memory left, which is
the most Amendment 10 could give it. The report's "CSP-M parse error" label and its
directive (an EGL template or model shape) are wrong for this failure: the runner
treats a run with no parsed result as a parse error before it looks at the exit
status. The session did not follow that directive; it read the failure as a
state-space blow-up and reduced the controller. No pipeline change is made.
