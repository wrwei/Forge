# Post-Codegen Review — LRE iter 2

**Status:** complete — minimal fixes driven by iter-1 `post_preflight` + `post_dafny_verify`.

## Changes

1. **Sensor.java** — `@RoboChartType("real")` on `SAFE_LARGE_DISTANCE` (preflight rule4).
2. **InputEvent.java** — new `tick()` record: the periodic control-cycle event.
3. **LreController.java** — all autonomous (guard-only) transitions gated on `event instanceof InputEvent.tick`, with explicit mutual-exclusion negations (`!inOpez`, `!camCondition`) between differently-targeted guards. Root cause: the Dafny generator emits autonomous guards as unconditional `ensures`, which higher-priority event branches preempt (iter-1 errors at LreController.dfy 95/99/135/138).

## Issues for user review

- **[design_choice]** `tick` is not in LRE-DM6 (it models the control cycle, not an operator command).
- **[design_choice]** Priority negations are redundant in Java but make the extracted guards mutually exclusive.
- Carried from iter 1: CAM entry action undefined (ambiguous_requirement); CPA math invented (invented_default).
