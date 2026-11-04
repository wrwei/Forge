# Sessions superseded by Amendment 9

Pre-registration v3, Amendment 9 (`review/prereg-v3-study-free.md` in the paper
repository, commit `401a08d`). Each session below was re-collected after the
amendment under the same pin `56cb43d7`, with new worktree and staging names
(`forge-v3c9`, `forge-v3r9`).

| Session | Result as collected | Why superseded |
|---|---|---|
| `v3-cold/sranger/run-1` | converged | read `forge.assets/corrections/csp_overrides.csp`, a file the pipeline does not read, whose header names the LRE controller and lists LRE's safety constants |
| `v3-reference/sranger/run-4` | converged at iteration 1 | read the same file (`cat`) |
| `v3-reference/steam_boiler/run-1` | stopped at iteration 3, not converged (FDR4 killed at the 10,240 MB limit; Isabelle type error) | the recorder refused the study name, so no iteration was recorded |

The Steam Boiler run has no `iter-*` record. `unrecorded-evidence/` holds what was
preserved from its worktree and scratchpad after the session ended: the four runner
outputs (`runner-outputs/`; three iterations plus one aborted start), the final
iteration's reports, Java (19 files), trace and formal artefacts
(`final-worktree-state/`), the session's scratchpad scripts, and a record of the final
worktree state made afterwards with the corrected recorder
(`iter-3-recorded-after-the-fact/`). Iterations 1 and 2 survive only as the runner's
per-phase summary lines.
