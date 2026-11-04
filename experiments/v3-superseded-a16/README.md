# Session superseded by Amendment 16

Pre-registration v3, Amendment 16 (`review/prereg-v3-study-free.md` in the paper
repository). The one Steam Boiler session collected at the Amendment 14 pin `d714365c`
is superseded by the rule of Amendment 15 (the pin a session ran at, not its verdict) and
the Steam Boiler is re-collected at the Amendment 16 pin. It is not counted.

| Session | Result as collected | Defects of its pin it met |
|---|---|---|
| `v3-cold/steam_boiler/run-1` (worktree `forge-v3c15-run1-steam_boiler`) | valid, not converged; counted under FDR4, class (c) | FDR4: the literal pump numbers 2-4 sent on `openPump`/`closePump` lie outside the checked range {0, 1} (every assertion "inconclusive"); Isabelle: a record field named `open`, an Isabelle/Pure keyword (theory does not parse) |

The record is the iteration-1 record the runner staged (`iter-1/`), with setup's
`actor.json` and `setup_ok.json`, the transcript and its audit (`TRANSCRIPT_AUDIT.md`,
written before Amendment 16).
