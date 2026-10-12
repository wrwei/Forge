# Prose-to-formula audit table: Chemical Detector, GasAnalysisController

Compiled from `forge.assets/case-studies/chemical_detector/requirements/requirement_all.txt (CD-GA-Beh1..7, CD-GA-FR1..4, CD-GA-Var1..4)`.
Each row shows the requirement sentence verbatim beside the transition entry compiled from it.
Semantics: Set-valued; staying is admitted exactly when no behavioural transition is enabled.

| Req | Formula | Prose (verbatim) |
|---|---|---|
| CD-GA-Beh1 | `init --> Reading` | On system startup, the gas-analysis subsystem transitions from its initial state to Reading. No guard, no trigger, no action. |
| CD-GA-Beh2 | `Reading --[gas \| true]--> Analysis / -` | In Reading, on receiving a gas event, the gas-analysis subsystem adopts the carried reading and begins classifying it (the Analysis behaviour). |
| CD-GA-Beh3 | `NoGas --[(step) \| true]--> Reading / -` | In NoGas, the gas-analysis subsystem transitions back to Reading. No guard, no trigger, no action. This is an autonomous transition that allows the next reading to be collected. |
| CD-GA-Beh4 | `Analysis --[(step) \| !stsIsGasD]--> NoGas / resume` | In Analysis, when the classification is noGas, the gas-analysis subsystem emits the resume event (so the movement subsystem returns to searching) and proceeds to await the next reading. |
| CD-GA-Beh5 | `Analysis --[(step) \| stsIsGasD]--> GasDetected / -` | In Analysis, when the classification is gasD, the gas-analysis subsystem proceeds to evaluate the detected signal's intensity (the GasDetected behaviour). |
| CD-GA-Beh6 | `GasDetected --[(step) \| insAboveThr]--> Final / stop` | In GasDetected, when the peak intensity is at or above the threshold thr, the gas-analysis subsystem emits the stop event (so the movement subsystem halts) and concludes its search, processing no further readings. |
| CD-GA-Beh7 | `GasDetected --[(step) \| !insAboveThr]--> Reading / turn(anl)` | In GasDetected, when the peak intensity is below the threshold thr, the gas-analysis subsystem chooses the direction of the strongest signal and emits a turn event carrying that direction, then returns to Reading to continue searching. |
