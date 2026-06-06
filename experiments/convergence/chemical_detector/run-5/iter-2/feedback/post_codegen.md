# Post-codegen review — chemical_detector, run-5 iter-1 (condition B)

**Status:** complete — 17 Java files under `chemdetector/`, plus `result_codegen.json` tracing all 81 requirements.

## Design choices

1. **No Final state in either controller.** Spec final transitions (GA `GasDetected -> j1`, MV `Found -> j1`) rerouted to live states per CLAUDE.md's no-Final rule for the theory-generated controller: GA sends `stop` then returns to `Reading`; MV's `Found` self-loops on `stop`/`turn`/`resume`.
2. **Complementary p/¬p guards** for the autonomous-only modes (`Analysis`: `stsIsNoGas`/`!stsIsNoGas`; `GasDetected`: `insAtOrAboveThr`/`!insAtOrAboveThr`; `AvoidingAgain`: `makingProgress`/`!makingProgress`) — total guard cover, no self-loops, propositionally complete for the Isabelle deadlock-freedom disjunction.
3. **goreq not a guard function** (M2M infers 1-param signatures only); threshold check inlined as `ins >= thr`. `goreq` lives in `ChemSensorService` (CD-Fn4) and is used by `intensity()`.
4. **Odometer as Sensors variable** (`d0/d1 = vehicle.odometer()`) — the generated action language has no input communication, so the `odometer ? dN` spec actions become sensor reads.
5. **Entry actions on incoming transitions** (lifted by the M2T into `entry`); `Waiting`'s `randomWalk()` is top-of-mode.
6. **changeDirection** emitted via single-field record `VehicleEvent.ChangeDirection(Loc)` so the event is typed `Loc`.

## Invented defaults

- Constants: `THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=0.0, OUT_PERIOD=1` (spec gives no values; chosen for the `{0..1}` FDR ranges).
- `Chem` = enum `{chemA, chemB}`; `angle(i)`: 1→Front, 2→Left, 3→Right, else Back.
- `analysis(gs)`: gasD iff some element has `c == targetChem && i > 0`.

## Ambiguities / caveats

- `Found` re-entry (on repeated `stop`/`turn`/`resume`) re-fires the lifted `flag` entry action in the model; Java-side `flag()` is idempotent.
