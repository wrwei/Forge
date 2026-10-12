# Prose-to-formula audit table: SRanger

Compiled from `forge.assets/case-studies/sranger/requirements/requirement_all.txt (SR-Beh1..SR-Beh7, SR-GP1, SR-GP2, SR-Var1, SR-FR1..3, SR-DM3)`.
Each row shows the requirement sentence verbatim beside the transition entry compiled from it.
Semantics: Set-valued: per (mode, valuation, event) the relation defines the SET of admitted next modes; no priority among simultaneously enabled transitions is stated in the Beh prose.

| Req | Formula | Prose (verbatim) |
|---|---|---|
| SR-Beh1 | `init --> Moving` | On power-up, the SRanger controller begins in Moving. |
| SR-Beh2 | `Moving --[obstacle \| obstacleDetected]--> Turning / Move(0,turnVel)` | The SRanger controller transitions from Moving to Turning when the obstacle event is received. On this transition, set clockResetTime to the current time and issue Move(0, turnVel) as the entry action of Turning (per SR-FR2). |
| SR-Beh3 | `Moving --[endTask \| true]--> Final / Move(0,0)` | The SRanger controller transitions from Moving to Final when the endTask event is received. On this transition, issue Move(0, 0) as the entry action of Final (per SR-FR3). |
| SR-Beh4 | `Moving --[tick \| true]--> Moving / -` | The SRanger controller transitions from Moving to Moving (no mode change) when the tick event is received. The controller performs no action on this transition. |
| SR-Beh5 | `Turning --[(step) \| turnDurationElapsed]--> Moving / Move(moveVel,0)` | The SRanger controller transitions from Turning to Moving when the turnDurationElapsed condition becomes true — i.e., when the time since clockResetTime is at or above turnDuration (autonomous, no event required). On this transition, issue Move(moveVel, 0) as the entry action of Moving (per SR-FR1). |
| SR-Beh6 | `Turning --[endTask \| true]--> Final / Move(0,0)` | The SRanger controller transitions from Turning to Final when the endTask event is received. On this transition, issue Move(0, 0) as the entry action of Final (per SR-FR3). |
| SR-Beh7 | `Turning --[tick \| true]--> Turning / -` | The SRanger controller transitions from Turning to Turning (no mode change) when the tick event is received. The controller performs no action on this transition. |
