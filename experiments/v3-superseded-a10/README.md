# Session superseded by Amendment 10

Pre-registration v3, Amendment 10 (`review/prereg-v3-study-free.md` in the paper
repository, commits `99f9db0` and `2fa83d5`).

| Session | Result as collected | Why superseded |
|---|---|---|
| `v3-reference/steam_boiler/run-1` (collected under Amendment 9, worktree `forge-v3r9-run1-steam_boiler`) | stopped at iteration 1, not converged: FDR4 killed at the 10,240 MB budget (peak 10,308 MB), Dafny and Isabelle failing | the session stopped, as the prompt requires, on the FDR4 report's directive to raise the memory budget; Amendment 10 raises the budget to 40,960 MB and the run is re-collected |

The record is the iteration-1 record the runner staged (`iter-1/`), with the
transcript and its audit added when they are exported.
