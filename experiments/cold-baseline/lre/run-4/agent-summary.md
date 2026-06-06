# LRE Baseline Run 4 — Agent Summary

Cold-baseline single-shot generation. No prior iteration, no verifier feedback.

## Design decisions

- **Package layout**: `lre.{annotation,mode,constants,sensor,event,actuator,operation,controller}`. Constants extracted to `LreConstants` (public static final doubles, all defaulting to 1.0 per LRE-DM4).
- **Obstacle/ObstacleRegister**: immutable record + copy-on-write register backed by `HashMap<Integer,Obstacle>`. Filtering by static/dynamic done via imperative for-loops (no streams).
- **Sensor sentinel handling**: distance functions return `Double.MAX_VALUE` and field accessors return `0.0` when `index == -1` or unknown, per LRE-DM5.
- **Operations**: `CalcVel`, `CalcCStc`, `CalcCDyn`, `CalcCPA`, `CheckOPEZ`. Each `compute()` uses only `this.field = expr` (no locals). `CheckOPEZ` reads `cstc` from `CalcCStc` to make the dependency explicit (LRE-OP2). `CalcCPA` inlines the CPA formula entirely into two assignments per LRE-OP5 (a tiny `1e-9` denominator term guards against div-by-zero when relative velocity is zero).
- **Controller**: single `step(InputEvent)` method. Operations run unconditionally first, then ~14 named boolean predicates, then a pure two-level if-else nested on `currentMode == X`. Within each mode, operator-event overrides come first (highest priority), then autonomous guards. Entry actions (`AdvVel(1.0)` for MOM, `AdvVel(0.0)` for HCM, `AdvVel(0.0)` on `endTask`/safe exit) are inlined at the transition.
- **No language violations**: no lambdas, no streams, no ternaries, no pattern-matching instanceof.
