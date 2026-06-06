# LRE cold baseline — run 1 agent summary

## Architecture

Standard layered layout: `lre.{annotation, mode, constants, event, sensor, actuator, operation, controller}`. `RoboChartType` annotates fields/params whose RoboChart type isn't implied by the Java type (`nat` for non-negative ints, `real` for doubles).

## State machine shape

Single-method `LreController.step(InputEvent event)`, four-mode enum `OCM, MOM, HCM, CAM` (initial OCM). Outer if-else dispatches by `currentMode ==` only; inner branches order transitions by priority. Each mode block leads with event-triggered branches (giving bare-precondition coverage for deadlock-freedom: OCM has `ReqVel`/`ReqHdng`/`ReqMOM`; MOM/HCM/CAM each have a bare `ReqOCM`). Within MOM, the high-priority safety branch `CAM` (Beh8) precedes mode-overrides; HCM mirrors this with Beh14 first among autonomous transitions.

## Predicate naming

Predicates encode {function or quantity, reference index, comparison direction}: e.g. `velBelowOrAtOne`, `odistCdynAboveOne`, `cdaBelowMinSafe`, `hdistCstcAtOrBelowStaticHoriz`. Negation uses `!inOpez`, never a separate variable.

## CalcCPA

The spec mandates exactly two field assignments in `compute()` with no intermediate fields. I inlined the 2D-CPA formula (relative position/velocity dot products) as two single expressions with `1e-9` denominator stabilisation. Sentinel `cdyn == -1` is absorbed by the Sensor zero-velocity / max-distance defaults.

## Ambiguities

Spec says `cstc/cdyn` are "natural number"; I kept `-1` sentinel per Sensor contract and annotated as `nat`. Beh10 ("regardless of velocity") is encoded as a single guard `vdist(cstc) <= staticObsDfltVertDist`. CAM-on-CAM autonomous re-entry is left implicit (no else-fallback; bare `ReqOCM` provides deadlock cover).
