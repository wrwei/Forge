# Post-Codegen Review — LRE iter 3

**Status:** complete — minimal fix driven by iter-2 `post_dafny_verify`.

## Changes

1. **LreController.java** — the 8 computed quantities of LRE-Var1..8 (`inOpez`, `hvel`, `vvel`, `vel`, `cstc`, `cdyn`, `cda`, `tcpa`) are now **controller fields**, assigned at the top of `step()` from the operation getters, and the named guard predicates read the fields instead of calling the operation getters. Root cause of iter-2 failures: guard calls became uninterpreted Dafny functions with `reads this`, so a `mode := X` assignment (`modifies this`) destabilised every other transition's ensures premise. Field reads are frame-tracked precisely, making the premises heap-stable. This also matches LRE-Var1..8's wording ("the controller maintains a … variable") — supersedes iter-1 design choice #4.
2. **result_codegen.json** — LRE-Var1..8 now trace to the LreController fields.

## Issues for user review

- Carried: `tick` event (design_choice), priority negations (design_choice), CAM entry action undefined (ambiguous_requirement), CPA math (invented_default).
- **[design_choice]** `cstc`/`cdyn` annotated `nat` per LRE-Var5/6 wording though the -1 sentinel is representable only as int; sentinel handling stays in the Sensor layer.
