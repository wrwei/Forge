# post_codegen — iteration 1

**Status:** complete

Iteration 1 cold codegen: two controllers (GasAnalysisController, MovementController) communicating through turn/stop/resume, flag to the Vehicle; 20 Java files, result_codegen.json with 116 rows covering all 81 requirements.

## Issues

- **design_choice** (CD-GA-Beh6, CD-MV-Beh9): Terminal behaviour modelled without a Final mode: gas analysis enters Concluded and movement stays in Found, each with a Tick-triggered self-loop so the mode keeps an outgoing transition.
- **design_choice** (CD-GA-FR3, CD-GA-FR4, CD-MV-Beh17, CD-MV-Beh18): Complementary guards written as p / !p (sts == noGas vs its negation; goreq(ins, THR) vs its negation; progress vs its De Morgan complement) so each autonomous choice is a total cover.
- **design_choice** (CD-Evt3): Odometer modelled as a zero-arg sensor reading (ChemSensor.odometer) sampled into d0/d1, not as an input event.
- **invented_default** (CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6): Constant values are not given by the spec: THR=3.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1 (small integers to keep the timed model small).
- **invented_default** (CD-DM4, CD-Fn1, CD-Fn3): Chem is an enum {TARGET, OTHER}; analysis = some value with c == TARGET and i > 0; angle(x) maps 1-based position to Angle in declaration order (Left, Right, Back, Front), cyclically; ties in location go to the first peak.
- **ambiguous_requirement** (CD-Fn2): intensity is the maximum over all values in the reading, including non-target chemicals, as the spec literally states.
- **ambiguous_requirement** (CD-MV-FR6): AvoidingAgain has no turn transition in the spec; a turn arriving there is not consumed.

## Next step

Run the pipeline (iteration 1) and act on the first failing phase's post_<phase>.md.
