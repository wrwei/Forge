# post_codegen — status: uncertain

Cold codegen of two controllers (`GasAnalysis`, `Movement`) in `chemical_detector.controller`. All 81 requirements are traced in `result_codegen.json`.

## Issues

1. **invented_default — constant values** (CD-Const1..6). THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1. They are small integers so CSP-gen's integer ceiling and default type ranges leave every guard able to be both true and false. `Constants.java:13-33`.
2. **design_choice — Tick event** (CD-GA-Beh6, CD-MV-Beh9). `InputEvent.Tick` is the control cycle with no other event. `CONCLUDED` and `FOUND` self-loop on Tick only, so there is no Final state and no untriggered self-loop, and the flag is not re-sent. `CONCLUDED` is used in place of a state named Final.
3. **design_choice — odometer as a sensor query** (CD-Evt3). `d0`/`d1` are assigned from `Sensor.odometer()` inside the actions, as CD-MV-FR4 and CD-MV-Beh16 describe. `Movement.java:105,144`.
4. **ambiguous_requirement — "indicates the target chemical"** (CD-Fn1/Fn2). `analysis` returns gasD iff some value is the configured target `Chem` with i > 0. `intensity`/`location` take the maximum over all values, as CD-Fn2/Fn3 say. `Sensor.java:41-78`.
5. **invented_default — position→direction** (CD-Fn3, CD-DM7). `angle(x)` maps positions 1..4 to Left, Right, Back, Front, cycling beyond 4. `Sensor.java:81-84`.
6. **design_choice — opaque types** (CD-DM4/DM5). Intensity is a `double` (real) so `thr` can be a numeric constant. Chem is a record with a nat identity.
7. **design_choice — complementary guards** (CD-GA-Beh4/5, CD-GA-Beh6/7, CD-MV-Beh17/18). Each guard pair is written `p` / `!p`, so each pair covers every case.
8. **invented_default — initial values**. sts=noGas, ins=0, anl=Front, a=Front, d0=d1=0, l=front, T=0.
9. **design_choice — Waiting's during action**. `vehicle.randomWalk()` runs at the head of the WAITING block. `Movement.java:76`.

## Next step

Run the pipeline.
