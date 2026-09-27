# Prose → Formula table (LRE behavioural requirements)

Compiled ONLY from LRE-Beh1..Beh19 (+ variable declarations LRE-Var1..Var8) of `forge.assets/case-studies/lre/requirements/requirement_all.json`. LRE-GP* excluded. Every formula is paired with the exact prose span it was compiled from.

| Req / conjunct | Formula | Verbatim prose span |
|---|---|---|
| LRE-Beh1 | `initial mode := OCM` | On power-up, the LRE begins in OCM. |
| LRE-Beh2 | `OCM --[reqVel | true]--> OCM  / outputs: advVel(v)` | In OCM, when the operator controller sends reqVel with a velocity value v, the LRE passes it through to the autopilot controller as advVel with the same value v. The LRE remains in OCM. |
| LRE-Beh3 | `OCM --[reqHdng | true]--> OCM  / outputs: advHdng(h)` | In OCM, when the operator controller sends reqHdng with a heading value h, the LRE passes it through to the autopilot controller as advHdng with the same value h. The LRE remains in OCM. |
| LRE-Beh4 | `OCM --[reqMOM | vel <= 1 AND NOT inOpez AND odist(cdyn) > 1 AND odist(cstc) > 1]--> MOM  / outputs: -` | The LRE transitions from OCM to MOM when the operator controller sends reqMOM AND all of the following are true: the velocity of the AUV (vel) is less than or equal to 1, the AUV is not in OPEZ, the overall Euclidean distance (the odist function) to cdyn is greater than 1, and the overall Euclidean distance (the odist function) to cstc is greater than 1. |
| LRE-Beh4 (conjunct) | `vel <= 1` | the velocity of the AUV (vel) is less than or equal to 1 |
| LRE-Beh4 (conjunct) | `NOT inOpez` | the AUV is not in OPEZ |
| LRE-Beh4 (conjunct) | `odist(cdyn) > 1` | the overall Euclidean distance (the odist function) to cdyn is greater than 1 |
| LRE-Beh4 (conjunct) | `odist(cstc) > 1` | the overall Euclidean distance (the odist function) to cstc is greater than 1 |
| LRE-Beh5 | `MOM --[(step) | inOpez]--> OCM  / outputs: -` | The LRE transitions from MOM to OCM when inOpez is true. The guard is inOpez alone; the depth and distance conditions are subsumed by CheckOPEZ's definition of inOpez. |
| LRE-Beh5 (conjunct) | `inOpez` | when inOpez is true |
| LRE-Beh6 | `MOM --[reqOCM | true]--> OCM  / outputs: -` | The LRE transitions from MOM to OCM when the operator controller sends reqOCM. |
| LRE-Beh7 | `MOM --[endTask | true]--> OCM  / outputs: advVel(0)` | The LRE transitions from MOM to OCM when endTask is received. On this transition, send advice velocity to 0 (advVel(0)). |
| LRE-Beh8 | `MOM --[(step) | cda < minSafeDist AND tcpa >= 0]--> CAM  / outputs: -` | The LRE transitions from MOM to CAM when the closest distance of approach (cda) is below the minSafeDist threshold AND the time to closest point of approach (tcpa) is non-negative. |
| LRE-Beh8 (conjunct) | `cda < minSafeDist` | the closest distance of approach (cda) is below the minSafeDist threshold |
| LRE-Beh8 (conjunct) | `tcpa >= 0` | the time to closest point of approach (tcpa) is non-negative |
| LRE-Beh9 | `MOM --[(step) | hvel >= 1 AND hdist(cstc) <= staticObsHorizDist]--> HCM  / outputs: -` | The LRE transitions from MOM to HCM when the AUV's horizontal velocity (hvel) is at or above 1 m/s AND the horizontal distance (hdist) to the closest static obstacle (cstc) is at or below the staticObsHorizDist threshold. |
| LRE-Beh9 (conjunct) | `hvel >= 1` | the AUV's horizontal velocity (hvel) is at or above 1 m/s |
| LRE-Beh9 (conjunct) | `hdist(cstc) <= staticObsHorizDist` | the horizontal distance (hdist) to the closest static obstacle (cstc) is at or below the staticObsHorizDist threshold |
| LRE-Beh10 | `MOM --[(step) | vdist(cstc) <= staticObsDfltVertDist]--> HCM  / outputs: -` | The LRE transitions from MOM to HCM when the vertical distance (vdist) to the closest static obstacle (cstc) is at or below the staticObsDfltVertDist threshold, regardless of velocity. |
| LRE-Beh10 (conjunct) | `vdist(cstc) <= staticObsDfltVertDist` | the vertical distance (vdist) to the closest static obstacle (cstc) is at or below the staticObsDfltVertDist threshold |
| LRE-Beh11 | `MOM --[(step) | vvel >= 1 AND vdist(cstc) <= staticObsVertDist]--> HCM  / outputs: -` | The LRE transitions from MOM to HCM when the AUV's vertical velocity (vvel) is at or above 1 m/s AND the vertical distance (vdist) to the closest static obstacle (cstc) is at or below the staticObsVertDist threshold. |
| LRE-Beh11 (conjunct) | `vvel >= 1` | the AUV's vertical velocity (vvel) is at or above 1 m/s |
| LRE-Beh11 (conjunct) | `vdist(cstc) <= staticObsVertDist` | the vertical distance (vdist) to the closest static obstacle (cstc) is at or below the staticObsVertDist threshold |
| LRE-Beh12 | `MOM --[reqHCM | true]--> HCM  / outputs: -` | The LRE transitions from MOM to HCM when the operator controller sends reqHCM. |
| LRE-Beh13 | `HCM --[(step) | hdist(cstc) > staticObsHorizDist AND vdist(cstc) > staticObsVertDist]--> MOM  / outputs: -` | The LRE transitions from HCM to MOM when the horizontal distance (hdist) to the closest static obstacle (cstc) exceeds the staticObsHorizDist threshold AND the vertical distance (vdist) to the closest static obstacle (cstc) exceeds the staticObsVertDist threshold. |
| LRE-Beh13 (conjunct) | `hdist(cstc) > staticObsHorizDist` | the horizontal distance (hdist) to the closest static obstacle (cstc) exceeds the staticObsHorizDist threshold |
| LRE-Beh13 (conjunct) | `vdist(cstc) > staticObsVertDist` | the vertical distance (vdist) to the closest static obstacle (cstc) exceeds the staticObsVertDist threshold |
| LRE-Beh14 | `HCM --[(step) | cda < minSafeDist AND tcpa >= 0]--> CAM  / outputs: -` | The LRE transitions from HCM to CAM when the closest distance of approach (cda) is below the minSafeDist threshold AND the time to closest point of approach (tcpa) is non-negative. |
| LRE-Beh14 (conjunct) | `cda < minSafeDist` | the closest distance of approach (cda) is below the minSafeDist threshold |
| LRE-Beh14 (conjunct) | `tcpa >= 0` | the time to closest point of approach (tcpa) is non-negative |
| LRE-Beh15 | `HCM --[reqOCM | true]--> OCM  / outputs: -` | The LRE transitions from HCM to OCM when the operator controller sends reqOCM. |
| LRE-Beh16 | `HCM --[(step) | inOpez]--> OCM  / outputs: -` | The LRE transitions from HCM to OCM when inOpez is true. The guard is inOpez alone; the depth and distance conditions are subsumed by CheckOPEZ's definition of inOpez. |
| LRE-Beh16 (conjunct) | `inOpez` | when inOpez is true |
| LRE-Beh17 | `CAM --[reqOCM | true]--> OCM  / outputs: -` | The LRE transitions from CAM to OCM when the operator controller sends reqOCM. |
| LRE-Beh18 | `CAM --[(step) | cda >= minSafeDist]--> OCM  / outputs: advVel(0)` | The LRE transitions from CAM to OCM when the closest distance of approach (cda) is at or above the minSafeDist threshold. On this transition, advise velocity to 0 (advVel(0)). |
| LRE-Beh18 (conjunct) | `cda >= minSafeDist` | the closest distance of approach (cda) is at or above the minSafeDist threshold |
| LRE-Beh19 | `OCM handles ONLY {reqVel, reqHdng, reqMOM}; other events in OCM: no transition/output; reqVel/reqHdng ignored outside OCM` | In OCM, the LRE handles exactly three operator inputs: reqVel (pass through velocity), reqHdng (pass through heading), and reqMOM (transition to MOM if guards allow). OCM does not respond to reqOCM since the LRE is already in OCM. In all other modes, operator velocity and heading inputs are overridden by the LRE. |
