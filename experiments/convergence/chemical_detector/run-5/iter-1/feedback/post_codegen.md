# Post-Codegen Review — chemical_detector (run-8, iter 1, condition B)

**Status:** complete

**Summary:** Iter-1 cold codegen: 17 Java files under `chemdetector/` implementing
two controllers (`GasAnalysis`, `Movement`), `Vehicle`/`Actuator` surfaces,
`Telemetry` sensor service (`@SensorService`), sealed event hierarchies, datamodel
enums/record, and constants. All 81 requirement IDs traced in `result_codegen.json`.

## Design choices

1. **Final states rerouted to live modes.** CD-GA-Beh6 / CD-MV-Beh9 target final
   state j1, but CLAUDE.md requires the theory-generated controller to have no
   Final state. GasAnalysis: GasDetected → `Done` (self-loop on `gas`), still
   sending `stop`. Movement: `Found` is terminal-but-live (self-loop on `stop`),
   entry still emits `flag` + `move(0, Front)`.
2. **Chem/Intensity as primitives** (int nat / double real) — wrapper types would
   force banned method-chain guard expressions.
3. **odometer as sensor method** — spec uses `odometer ? dN` as an *action*, which
   the pipeline's action set can't express; realised as `dN = telemetry.odometer()`.
4. **Vehicle operations as methods** — CD-OP1..4 are platform motion operations,
   not compute() operation classes.
5. **Guard totality via predicate + negation** — two-way autonomous splits use
   `b` / `!b` so guard cover is trivially total (Analysis, GasDetected,
   AvoidingAgain).
6. **Waiting during-action** — `vehicle.randomWalk()` at head of Waiting block.

## Invented defaults

- `analysis()`: target indicated iff `c == targetChem && i > 0.0`.
- `angle()`: 1→Front, 2→Right, 3→Back, 4→Left, else Front.
- Constants all 1 / 1.0 (within FDR4 {0..1} ranges).
- Empty-reading safe defaults: intensity 0.0, location Front, analysis noGas.

## Ambiguous

- Threshold guard written `ins >= thr` instead of `goreq(ins, thr)` to keep the
  extracted guard a simple comparison; `goreq` lives on Telemetry and is used
  inside `intensity()`/`location()` (CD-Fn4).

**Next step:** run the pipeline, iterate on feedback.
