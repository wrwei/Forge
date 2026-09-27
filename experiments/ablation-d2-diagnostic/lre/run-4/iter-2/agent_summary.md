# LRE run 6 — iteration 2

## What changed and why

Iteration 1 wrote the whole tree (15 files, 51/51 requirements traced) and
passed 10 of 12 phases. Both failures traced to one construct: the epsilon
constant `MIN_REL_SPEED_SQ = 0.000001` I had added to `CalcCPA` to keep `tcpa`
finite at zero relative velocity.

- `post_vacuity.md` issue 5 (BLOCKING): `minrelspeedsq` diverges — Java `1e-06`,
  generated Dafny `1`. A fractional constant below 1e-3 does not survive
  emission.
- `post_dafny_verify.md`: parse error `LreController.dfy(13,35): this symbol not
  expected in Dafny` — the same declaration, at the same place in the file.

**Fix:** removed the constant entirely rather than retuning it. The degenerate
case now lives in the sensor layer, where the codegen rules put safe defaults:
new `Sensor.relSpeedSq(index)` returns the squared relative speed floored at 1,
so it is never zero and `CalcCPA` divides by it unguarded. With no relative
motion `closingRate` is 0, so `tcpa = 0` and `cda = hdist(cdyn)` — the same
answer the epsilon gave, with no constant to diverge. No other numeric literal
in the tree is fractional below 1e-3. Build clean; trace refreshed (101 entries).

## Decisions the requirements did not settle

Unchanged from iteration 1, and still open for human review:

1. **LRE-DM5 is internally inconsistent** — `nsRelDist`/`ewRelDist` must return
   zero with no obstacle while `hdist` must return a safe large distance, yet
   LRE-SF1 defines `hdist` from those accessors. Distance functions were given
   their own no-obstacle branch, and `cda²` expanded as
   `hdist(cdyn)² + 2·tcpa·closingRate + tcpa²·relSpeedSq` so the safe default
   carries through instead of yielding `cda = 0` and a false MOM→CAM.
2. `SAFE_DISTANCE = 1000.0` m is invented; LRE-DM5 says only "safe large".
3. LRE-Beh4's bare "1" reads as `MIN_SAFE_DIST` for distances, literal for `vel`.
4. CAM gets no entry action — LRE-FR4 names no output value.

## Unresolved

`cstc`/`cdyn` are natural numbers carrying a `-1` sentinel; `int` maps to
RoboChart `nat` unconditionally. Implemented as specified.

The four advisory vacuity findings (modes MOM/HCM/CAM unreachable, 15
transitions unfireable, `(OCM,{reqMOM})` and `(OCM,{})` dead) are marked
"no action for the codegen agent" — they follow from the CSP domain narrowing,
not the Java — so I made no change for them.
