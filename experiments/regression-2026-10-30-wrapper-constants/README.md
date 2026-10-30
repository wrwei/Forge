# Regression: Dafny rendering of the wrapper types' MAX_VALUE / MIN_VALUE (30 October 2026)

Change: `java2dafny.egl` renders `MAX_VALUE` / `MIN_VALUE` of `Integer`, `Long`,
`Short`, `Byte` (exact integers) and `Float` (Java's printed decimal), as it
already did for `Double`. Pre-registered as Amendment 6 of
`review/prereg-v3-study-free.md` (paper repository), before the change.

Every Java source the paper and letter report, plus every iteration collected so
far under pre-registration v3, was extracted (each phase in its own process, as
the pipeline runs them) with the previous pin `2db4c031` and with the change.
324 sources; all extract with both. RoboChart, Dafny and Isabelle artefacts
are compared (Isabelle with set and disjunction order normalised).

Result: one source changes, iteration 1 of Reference LRE run 3, in one Dafny line:
`const NO_INDEX: int := MAX_VALUE` becomes `const NO_INDEX: int := 2147483647`.
No other artefact of any source changes. Per-source results: `regression.tsv`.
