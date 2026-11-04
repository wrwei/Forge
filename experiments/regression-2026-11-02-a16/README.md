# Regression: Amendment 16 (2 November 2026)

Pre-registration v3, Amendment 16 (`review/prereg-v3-study-free.md` in the paper
repository). Pin before: `d714365c`. Pin after: `aaddf222`.

Changes: (1) the FDR4 range step gives an event's channel its own value set when the model
sends an integer literal outside the event type's checked range on it; every numeric type
stays at {0, 1}; (2) Isabelle record fields named after a keyword of Isabelle/Pure or
Isabelle/HOL (the `keywords` clauses of `Pure.thy` and `HOL.thy` in the Isabelle2023
distribution) get a trailing underscore.

Method, as for Amendment 14: 391 Java sources (Amendment 14's 390 sources, with the Steam
Boiler records at their locations after Amendment 15, and cold Steam Boiler pass 1 of the
Amendment 15 re-collection) extracted by the five extraction phases with the resources at
`d714365c` and at `aaddf222` (same compiled Java; the Java sources did not change).

| Result | Sources |
|---|---|
| identical | 390 |
| Isabelle record field renamed (`open` to `open_`) | 1 (cold Steam Boiler pass 1 of the Amendment 15 re-collection) |
| outside the permitted kinds | 0 |

Change 1 acts in the FDR4 phase. Its reach, measured on the extracted models (column
`literal_sends_outside_0_1`): two sources send an integer literal outside {0, 1} on an
event, both Steam Boiler sessions that are not counted (cold pass 2 in
`v3-superseded-a15`, cold pass 1 in `v3-superseded-a16`), each sending the pump numbers 2-4
on `openPump` and `closePump`. No counted session and no mutant is affected, so no verdict
is re-scored.

Check of change 1 on cold pass 1's model (operator's terminal, FDR4 4.2.7): with the two
channels given the values {0..4} and every type at {0..1}, FDR4 no longer rejects the model
on loading; it explored the state space until macOS's memory-pressure killer ended it at
76,041 MB (system log, 12:48:07 UTC). Before the change it stopped within a second on
"openPump.out.2 ... not a member of the set {0, 1}".

Files: `regression.tsv` (one row per source), `changed_lines.json` (every changed line).
