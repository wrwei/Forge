# Prose-to-formula audit table: Chemical Detector, MovementController

Compiled from `forge.assets/case-studies/chemical_detector/requirements/requirement_all.txt (CD-MV-Beh1..23, CD-MV-FR1..7, CD-MV-Var1..4)`.
Each row shows the requirement sentence verbatim beside the transition entry compiled from it.
Semantics: Set-valued; staying is admitted exactly when no behavioural transition is enabled.

| Req | Formula | Prose (verbatim) |
|---|---|---|
| CD-MV-Beh1 | `init --> Waiting` | On system startup, the movement subsystem transitions from its initial state to Waiting. No guard, no trigger, no action. |
| CD-MV-Beh2 | `Waiting --[turn \| true]--> Going / move(lv,a)` | In Waiting, on a turn event, the movement subsystem begins moving (the Going behaviour) in the carried direction. |
| CD-MV-Beh3 | `Waiting --[resume \| true]--> Waiting / -` | In Waiting, on receiving the resume event, the movement subsystem self-loops back to Waiting. Trigger: resume. No guard, no action. (Resume in Waiting is effectively a no-op but is permitted so cross-state resume transitions are uniformly handled.) |
| CD-MV-Beh4 | `Waiting --[stop \| true]--> Found / flag, halt` | In Waiting, on receiving the stop event, the movement subsystem transitions to Found. Trigger: stop. No guard, no action. |
| CD-MV-Beh5 | `Going --[turn \| true]--> Going / move(lv,a)` | In Going, on a turn event, the movement subsystem stays in Going and re-issues its move in the newly carried direction. |
| CD-MV-Beh6 | `Going --[stop \| true]--> Found / flag, halt` | In Going, on receiving the stop event, the movement subsystem transitions to Found. Trigger: stop. No guard, no action. |
| CD-MV-Beh7 | `Going --[obstacle \| true]--> Avoiding / changeDirection` | In Going, on an obstacle event, the movement subsystem begins avoiding the obstacle (the Avoiding behaviour) and starts timing the new evasion sequence. |
| CD-MV-Beh8 | `Going --[resume \| true]--> Waiting / -` | In Going, on receiving the resume event, the movement subsystem transitions to Waiting. Trigger: resume. No guard, no action. |
| CD-MV-Beh9 | `Found stays (terminal); admits Final (MV-A1)` | Once in Found, the movement subsystem stays halted; it has already emitted the flag event and stopped the platform on entry. |
| CD-MV-Beh10 | `Avoiding --[turn \| true]--> TryingAgain / move(lv,a)` | In Avoiding, on a turn event, the movement subsystem proceeds to TryingAgain (resuming motion). |
| CD-MV-Beh11 | `Avoiding --[stop \| true]--> Found / flag, halt` | In Avoiding, on receiving the stop event, the movement subsystem transitions to Found. Trigger: stop. No guard, no action. |
| CD-MV-Beh12 | `Avoiding --[resume \| true]--> Waiting / -` | In Avoiding, on receiving the resume event, the movement subsystem transitions to Waiting. Trigger: resume. No guard, no action. |
| CD-MV-Beh13 | `TryingAgain --[turn \| true]--> TryingAgain / move(lv,a)` | In TryingAgain, on a turn event, the movement subsystem stays in TryingAgain and re-issues its move in the newly carried direction. |
| CD-MV-Beh14 | `TryingAgain --[stop \| true]--> Found / flag, halt` | In TryingAgain, on receiving the stop event, the movement subsystem transitions to Found. Trigger: stop. No guard, no action. |
| CD-MV-Beh15 | `TryingAgain --[resume \| true]--> Waiting / -` | In TryingAgain, on receiving the resume event, the movement subsystem transitions to Waiting. Trigger: resume. No guard, no action. |
| CD-MV-Beh16 | `TryingAgain --[obstacle \| true]--> AvoidingAgain / -` | In TryingAgain, on a fresh obstacle event, the movement subsystem proceeds to AvoidingAgain, recording the current distance travelled to seed stuck detection. |
| CD-MV-Beh17 | `AvoidingAgain --[(step) \| makingProgress]--> Avoiding / -` | In AvoidingAgain, when the robot is judged to be making progress (less than stuckPeriod has elapsed since the evasion began, or it has advanced more than stuckDist), the movement subsystem returns to Avoiding and restarts the evasion timer. |
| CD-MV-Beh18 | `AvoidingAgain --[(step) \| !makingProgress]--> GettingOut / shortRandomWalk, wait(outPeriod)` | In AvoidingAgain, when the robot is judged to be stuck (at least stuckPeriod has elapsed since the evasion began and it has advanced no more than stuckDist), the movement subsystem proceeds to GettingOut. |
| CD-MV-Beh19 | `AvoidingAgain --[stop \| true]--> Found / flag, halt` | In AvoidingAgain, on receiving the stop event, the movement subsystem transitions to Found. Trigger: stop. No guard, no action. |
| CD-MV-Beh20 | `AvoidingAgain --[resume \| true]--> Waiting / -` | In AvoidingAgain, on receiving the resume event, the movement subsystem transitions to Waiting. Trigger: resume. No guard, no action. |
| CD-MV-Beh21 | `GettingOut --[turn \| true]--> Going / move(lv,a)` | In GettingOut, on a turn event, the movement subsystem resumes moving (the Going behaviour) in the carried direction. |
| CD-MV-Beh22 | `GettingOut --[stop \| true]--> Found / flag, halt` | In GettingOut, on receiving the stop event, the movement subsystem transitions to Found. Trigger: stop. No guard, no action. |
| CD-MV-Beh23 | `GettingOut --[resume \| true]--> Waiting / -` | In GettingOut, on receiving the resume event, the movement subsystem transitions to Waiting. Trigger: resume. No guard, no action. |
