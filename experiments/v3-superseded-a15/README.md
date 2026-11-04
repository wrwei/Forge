# Sessions superseded by Amendment 15

Pre-registration v3, Amendments 14 and 15 (`review/prereg-v3-study-free.md` in the paper
repository). All four Steam Boiler sessions collected at the pin `56cb43d7`, before the
Amendment 14 pin `d714365c`, are superseded by rule (the pin a session ran at, not its
verdict) and re-collected at `d714365c`. None of them is counted in the analysis.

| Session | Result as collected | Defects of the old pin it met |
|---|---|---|
| `v3-cold/steam_boiler/run-1` | not converged; counted under FDR4, class (e) (FDR4 ended by macOS at 79 GB); Dafny and Isabelle also failed | Dafny brackets dropped; Isabelle function typed `real`; FDR4 kill reported as a parse error |
| `v3-cold/steam_boiler/run-2` | not converged; counted under FDR4, class (c) | output-event value dropped (FDR4 type error); Isabelle function typed `real` |
| `v3-reference/steam_boiler/run-1` | converged at iteration 4 | Isabelle functions typed `real` (iterations 1-2); FDR4 kill reported as a parse error (iteration 3) |
| `v3-reference/steam_boiler/run-2` | not converged at the cap of 7, and not converged at iteration 14 after continuing past the cap under Amendment 13 (FDR4 no verdict; Isabelle `real` typing, then `exception Empty`, then deadlock-freedom timeouts) | Isabelle functions typed `real`; Dafny brackets dropped (iteration 3); FDR4 kills reported as parse errors |

Run 2 was not audited: Amendment 15 superseded it before its audit. Its record is the 14
iterations the runner staged (`iter-1` to `iter-14`), as staged, with `actor.json` and
`setup_ok.json` from setup; its transcript is added if exported. The other three keep
their committed records and audits, unchanged.
